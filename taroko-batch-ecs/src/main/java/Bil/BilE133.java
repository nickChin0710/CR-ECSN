/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/09  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/23  V1.00.04    shiyuqi      coding standard, rename field method & format                   * 
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard  *
*  109-10-27  V1.00.08    JeffKung     record1 : space or 'R'               *
*  111-09-22  V1.00.09    JeffKung     加轉特店分期及紅利折抵資料                       *
*****************************************************************************/
package Bil;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.HashMap;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;
import com.CommTxBill.FiscTxData;
import com.CommTxBill.PlatformData;

import Dxc.Util.SecurityUtil;

import com.AStar.TBConvert.CNS.*;
import com.AStar.TBConvert.Customize.*;


public class BilE133 extends AccessDAO {
    private final  String progname = "FISC-ICEMVQBD晶片卡請款資料入bil_fiscdtl處理 111/09/22  V1.00.09";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		 HashMap<String,String> dataDMap = null;
		 String text1 = "", text2 = "";
		 String fileNameD="", filePathFromDB="", fileDate="", bankNo="";
		 String filePath = "";
		 byte[] bytesArr1 = null, bytesArr2 = null;
		 int inputFileD;
		 inputFileD = -1;
            // ====================================
            // 固定要做的 
            try {
            	dateTime();
				setConsoleMode("Y");
	            javaProgram = this.getClass().getName();
	            showLogMessage("I", "", javaProgram + " " + progname);

	            if (!connectDataBase()) {
	                comc.errExit("connect DataBase error", "");
	            }
	            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
	            commTxBill = new CommTxBill(getDBconnect(), getDBalias(), false);
	            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
	            
	            int ret = TBConvertTCB.init(String.format("%s/TBConvert/TBConvert.conf", Normalizer.normalize(System.getenv("PROJ_HOME"), Normalizer.Form.NFD)));
	            
	            businessDate = getBusiDate();
	            
	            // fileDate:傳檔日期FILE_DATE
				// filePathFromDB: ECS_FTP_LOG.local_dir
				// fileNameD:通知檔案名稱FILE_NAME
				// comcr.h_call_batch_seqno
		        fileDate = args[0];
		        filePathFromDB = args[1];
				fileNameD = args[2];
				comcr.hCallBatchSeqno = args[3];
         
		        
				// select the bank number
				bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
				
				// check whether file name's 2nd to 10th characters equals to the bank number
				if( ! comc.getSubString(fileNameD,1, 9).equalsIgnoreCase(bankNo) ) {
					showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
					return -1;
				}

				//open  F00600000.ICEMVQBD.XXXXX file ==================
				filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
				fileNameD = SecurityUtil.verifyPath(fileNameD);
				filePath = Paths.get(filePathFromDB, fileNameD ).toString();
				if (openBinaryInput(filePath) == false) {
					showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
		            return -1;
		        }
								
				// check whether fctl_no  exist in the bil_fiscctl and select fctl_no
				String fctlNo = commTxBill.getFctlNoFromBilFiscctl(fileNameD);
				 if(  commTxBill.isEmpty(fctlNo)) {
					 showLogMessage("E", ""
		            			, String.format("此批資料未經過檢核={%s:%s}", fileNameD, businessDate) );
					 return -1;
				 }else {
					 commTxBill.printDebugString("檢核成功");
					 commTxBill.deleteBilFiscdtl(fctlNo);// delete previous inserting data
				 }
											
				 byte[] bytes = new byte[450]; //record1
				 byte[] bytes2 = new byte[450];  //record2
				 int readCnt = 0;
				 int readCnt2 = 0;
				 int recCnt = 0;
				
				 while(true) {
					 readCnt = readBinFile(bytes);
					 if(readCnt < 5)
							break;
					 
					 //recCnt++;
					 //showLogMessage("I", "", "record cnt="+recCnt);
					 
					 if(bytes.length != 0) {		
						
						//排除通知檔內第一筆，因為第一筆是美元匯率資訊
						if( commTxBill.isCurrRateTx( CommTxBill.subByteToStr(bytes, 0,4) ) ) {
							continue;
						}
						
						// =============Record1=========================
						// if the this record is empty, this data is record1
						if(  commTxBill.isEmpty(CommTxBill.subByteToStr(bytes, 4,5) ) || CommTxBill.subByteToStr(bytes, 4,5).equalsIgnoreCase("R") ) {
							;
						}
						else {
							showLogMessage("E", "", "資料格式錯誤：此筆資料應為record1");
							return -1;
						}	
						
						// check whether the characters from 6st to 13th equals to the bank number
						if( ! CommTxBill.subByteToStr(bytes, 5,13).equalsIgnoreCase(bankNo)  ) {
							showLogMessage("E", "", "參加單位代號錯誤");
							return -1;
						}
						
						//轉中文碼
						ConvertCNS_Big5 cns2big5  = new ConvertCNS_Big5();
			            byte[] tempbyte = new byte[40];
			            System.arraycopy(bytes, 248, tempbyte, 0, 40);
			            cns2big5.convert(null, tempbyte, tempbyte.length, false);
			            byte[] returnBytes = comc.fixLeft(new String(cns2big5.getResult(), "big5"), 40).getBytes("big5");
			            System.arraycopy(returnBytes, 0, bytes, 248, 40);
			            
						// ============Record1==========================
						// ============Record2==========================
						
						//讀第二筆資料
						readCnt2 = readBinFile(bytes2);
						if(readCnt < 5) {
							showLogMessage("E", "", "資料格式錯誤：此筆資料應為record2");
							return -1;
						}
						
						// if this record is empty, this data is record2
						if( ! CommTxBill.subByteToStr(bytes2, 4,5).equals("2")  ) {
							showLogMessage("E", "", "資料格式錯誤：此筆資料應為record2");
							return -1;
						}
						
						// ============Record2==========================
						// ===========================================
						
						dataDMap = getDataD(bytes, bytes2);  // separate the columns in file
						
						insertBilFiscdtl(dataDMap, fileNameD, fctlNo);
						
					}
				}	
				
				closeBinaryInput();
				commTxBill.updateProcCodeFromBilFiscctl(fctlNo);
	            
			} catch (Exception e) {
				expMethod = "mainProcess";  
	            expHandle(e); 
	            return  exceptExit; 
			}
            //=====================================       
            finalProcess();
			return 0;
	}
	 /**
	 * insert data into bil_fiscdtl
	 * @param dataDMap
	 * @param fileName
	 * @param fctlNo
	 * @throws Exception
	 */
	private void insertBilFiscdtl(HashMap<String, String> dataDMap, String fileName, String fctlNo) throws Exception {
		String purchaseDate = "", localDateReceived = "", transactionDate = "", teSendDate = "";
		String binType = "";
		String prepaidCardIndicator = "", vROLCaseNumber = "", paymentFacilitatorID = "";
		//==================================================
		// 以下字串最後兩位皆為小數，因此需做轉換
		BigDecimal sourceAmountDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("sourceAmount"));
		BigDecimal destinationAmountDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("destinationAmount"));
		BigDecimal usdSettlementAmountDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("USDSettlementAmount"));
		BigDecimal bonusPayCashDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("bonusPayCash"));

		//==================================================
		
		String merchantChineseName = CommTxBill.subByteToStr(
				dataDMap.get("messageTextOrMerchantChineseName").getBytes("big5"), 0, 40);

		//============= turn MMddyy into yyyyMMdd===================
		
		purchaseDate = CommTxBill.convertStrDateFormat(dataDMap.get("purchaseDate"), "MMddyy", "yyyyMMdd");	
		localDateReceived = CommTxBill.convertStrDateFormat(dataDMap.get("localDateReceived"), "MMddyy", "yyyyMMdd"); 
		
		//============= turn yyMMdd into yyyyMMdd===================
		teSendDate = CommTxBill.convertStrDateFormat(dataDMap.get("teSendDate"), "yyMMdd", "yyyyMMdd");
		transactionDate = CommTxBill.convertStrDateFormat(dataDMap.get("transactionDate"), "yyMMdd", "yyyyMMdd"); 
		
		//==================================================
		
		FiscTxData fiscTxData = commTxBill.getFiscTxData(dataDMap.get("transactionType"), dataDMap.get("usageCode"), 
					dataDMap.get("cardNumber"), dataDMap.get("reimbursementAttribute"), dataDMap.get("acceptorID"), 
					dataDMap.get("merchantCategoryCode"), "", dataDMap.get("settlementFlag"));
		
		PlatformData platformData = fiscTxData.getPlatformData();
		
		binType = fiscTxData.getBinType();
		
		//==================================================
		
		switch(binType) {
		case "V":
			prepaidCardIndicator = dataDMap.get("prepaidCardIndicator");
			vROLCaseNumber = dataDMap.get("vROLCaseNumber");
			break;
		case "M":
			paymentFacilitatorID = dataDMap.get("VROLCaseNumberOrPaymentFacilitatorId");
			break;
		case "J":
			
			break;
		default:
			
			break;
		}
		
		// ====================================================
		
		daoTable = "bil_fiscdtl";
		
		// ====================================================
		setValue("ECS_REFERENCE_NO",commTxBill.getReferenceNo() );
		setValue("ECS_FCTL_NO", fctlNo); 
		setValue("ECS_TX_CHI_NAME", fiscTxData.getTxChiName());
		setValue("ECS_TX_ENG_NAME", fiscTxData.getTxEngName());
		setValue("ECS_BIN_TYPE", binType);
		setValue("ECS_DC_FLAG", fiscTxData.getDcFlag());
		setValue("ECS_DEBIT_FLAG", fiscTxData.getDebitFlag());
		setValue("ECS_DC_CURR", fiscTxData.getDcCurr());
		setValue("ECS_TX_CODE", fiscTxData.getTxCode());
		setValue("ECS_CB_CODE",  fiscTxData.getCbCode());
		setValue("ECS_PLATFORM_KIND", platformData.getPlatformKind());
		setValue("ECS_CUS_MCHT_NO", platformData.getCusMchtNo());
		setValue("ECS_BILL_TYPE", fiscTxData.getBillType());
		setValue("ECS_ACCT_CODE", fiscTxData.getAcctCode());
		setValue("ECS_SIGN_CODE", fiscTxData.getSignCode());
		setValue("ECS_REAL_CARD_NO", fiscTxData.getRealCardNo());
		setValue("ECS_V_CARD_NO", fiscTxData.getvCardNo());
		setValue("MEDIA_NAME", fileName);
		// ==================請款檔資料============================
		setValue("FISC_TX_CODE", dataDMap.get("transactionType"));
		setValue("EMV_REVERSAL_IND", dataDMap.get("reversalIndicator"));
		setValue("EMV_BANK_NUM", dataDMap.get("bankNumber"));
		setValue("SOURCE_BIN", dataDMap.get("sourceBINOrICANumber"));
		setValue("DEST_BIN", dataDMap.get("destinationBINOrICANo"));
		setValue("ACQ_BUSINESS_ID", dataDMap.get("acquirerBusinessID"));
		setValue("CARD_NO", dataDMap.get("cardNumber"));
		setValue("EMV_LOCAL_DATE", localDateReceived);
		setValue("EMV_LOCAL_TIME", dataDMap.get("localTimeReceived"));
		setValue("EMV_INPUT_SEQ", dataDMap.get("inputSequenceNumber"));
		setValueDouble("SOURCE_AMT", sourceAmountDouble.doubleValue());
		setValue("SOURCE_CURR", dataDMap.get("sourceCurrency"));
		setValueDouble("DEST_AMT", destinationAmountDouble.doubleValue());
		setValueDouble("SETL_AMT", usdSettlementAmountDouble.doubleValue());
		setValue("MCHT_NO", dataDMap.get("acceptorID"));
		setValue("MCC_CODE", dataDMap.get("merchantCategoryCode"));
		setValue("MCHT_ENG_NAME", dataDMap.get("merchantName"));
		setValue("MCHT_CITY", dataDMap.get("merchantCityOrLocation"));
		setValue("MCHT_STATE", dataDMap.get("merchantState"));
		setValue("MCHT_ZIP", dataDMap.get("merchantZipCode"));
		setValue("MCHT_COUNTRY", dataDMap.get("merchantCountryCode"));
		setValue("SPECIAL_COND_IND", dataDMap.get("specialConditionIndicator"));
		setValue("PURCHASE_DATE", purchaseDate);
		setValue("AUTH_CODE", dataDMap.get("authorisationResponseID"));
		setValue("EC_IND", dataDMap.get("mailOrTelephoneECIIndicator"));
		setValue("PREPAID_CARD_IND", prepaidCardIndicator);
		setValue("CAT_IND", dataDMap.get("CAT"));
		setValue("TERMINAL_ID", dataDMap.get("terminalID"));
		setValue("POS_TE_CAP", dataDMap.get("POSTerminalCapability"));
		setValue("POS_ENTRY_MODE", dataDMap.get("POSEntryMode"));
		setValue("PROCESS_DAY", dataDMap.get("processDate"));
		setValue("FILM_NO", dataDMap.get("microfilmReferenceNoAcquirersReferenceNo"));
		setValue("USAGE_CODE", dataDMap.get("usageCode"));
		setValue("REASON_CODE", dataDMap.get("chargebackRepresentOrRetrievalReasonCode"));
		setValue("CB_REF_NO", dataDMap.get("chargebackReferenceNo"));
		setValue("VCRFS_IND", dataDMap.get("specialChargebackIndicatorOrVCRFSOrMASTERCOMIndicator"));
		setValue("CWB_IND", dataDMap.get("cardWarningBulletinIndicator"));
		setValue("DOC_IND", dataDMap.get("documentationIndicator"));
		setValue("FEE_REASON_CODE", dataDMap.get("feeReasonCode"));
		setValue("MESSAGE_TEXT", dataDMap.get("messageTextOrMerchantChineseName"));
		setValue("MCHT_CHI_NAME", merchantChineseName);
		setValue("SERVICE_CODE", dataDMap.get("serviceCode"));
		setValue("UCAF", dataDMap.get("UCAF"));
		setValue("DCC_IND", dataDMap.get("DCCIndicator"));
		setValue("FRAUD_NOTIFY_DATE", dataDMap.get("fraudNotificationDate"));
		setValue("ACQ_RESP_CODE", dataDMap.get("acquirerResponseCode"));
		setValue("FLOOR_LIMIT_IND", dataDMap.get("floorLimitIndicator"));
		setValue("PCAS_IND", dataDMap.get("PCASIndicator"));
		setValue("INTERFACE_TRACE_NUM", dataDMap.get("interfaceTraceNumber"));
		setValue("EMV_CASHBACK_AMT", dataDMap.get("cashbackAmount"));
		setValue("ISSUE_CTRL_NUM", dataDMap.get("issuerControlNumber"));
		setValue("IRD", dataDMap.get("interchangeRateDesignator"));
		setValue("REIMB_ATTR", dataDMap.get("reimbursementAttribute"));
		setValue("SETTL_FLAG", dataDMap.get("settlementFlag"));
		setValue("ORIG_TX_TYPE", dataDMap.get("originalTransactionType"));
		setValue("RTN_REASON_CODE", dataDMap.get("returnedItemReasonCode"));
		setValue("TRANSACTION_ID", dataDMap.get("transactionID"));
		setValue("CARD_PRODUCT_ID", dataDMap.get("cardLevelId"));
		setValue("AUTH_VALID_CODE", dataDMap.get("authorizativeCheckNo"));
		setValue("PAYMENT_TYPE", dataDMap.get("payType"));
		setValue("DEST_CURR", dataDMap.get("destinationCurrency"));
		setValue("MUTI_CLEARING_SEQ", dataDMap.get("multipleClearingSequenceNumber"));
		setValue("QPS_CB_IND", dataDMap.get("QPSOrPayPassChargebackEligibilityIndicator"));
		setValue("FRAUD_CB_CNT", dataDMap.get("fraudNotificationServiceChargebackCounter"));
		setValue("ADDN_AMT", dataDMap.get("surchargeAmount"));
		setValue("ADDN_AMT_SIGN", dataDMap.get("surchargeCreditOrDebitIndicator"));
		setValue("ADDN_BILL_CURR", dataDMap.get("surchargeAmountincardholderbillingcurrency"));
		setValue("TOKEN_ASSURE_LEVEL", dataDMap.get("tokenAssuranceLevel"));
		setValue("TOKEN_REQUESTOR_ID", dataDMap.get("tokenRequestorID"));
		setValue("EMV_ERROR_CODE", dataDMap.get("errorReplyNo"));
		setValue("EMV_CASHBACK_AMT2", dataDMap.get("cashbackAmount1"));
		setValueDouble("AC_CRYPTO_AMT", comc.str2double(dataDMap.get("cryptoAmount")));
		setValue("TE_TX_CURR", dataDMap.get("transactionCurrencyCode"));
		setValue("AC_TX_TYPE", dataDMap.get("transactionTypeAC"));
		setValue("PAN_SEQ_NUM", dataDMap.get("PANSequenceNumber"));
		setValue("TE_TX_DATE", transactionDate );
		setValue("TE_PROFILE", dataDMap.get("terminalCapabilitiesProfile"));
		setValue("TE_COUNTRY", dataDMap.get("terminalCountryCode"));
		setValue("INTERFACE_DEV_NUM", dataDMap.get("interfaceDeviceSerialNumber"));
		setValue("AC_UNPRED_NUM", dataDMap.get("unpredictableNumber "));
		setValue("AP_TX_NUM", dataDMap.get("applicationTransactionNumber"));
		setValue("AP_PROFILE", dataDMap.get("applicationInterchangeProfile"));
		setValue("AC", dataDMap.get("applicationCryptogram"));
		setValue("IAD", dataDMap.get("issuerApplicationData"));
		setValue("TVR", dataDMap.get("terminalVerificationResult"));
		setValue("CVM_RESULT", dataDMap.get("cardholderVerificationMethodResult"));
		setValue("CRYPTOGRAM_INFO", dataDMap.get("cryptogramInformationData"));
		setValue("AP_EXPIRE_DATE", dataDMap.get("applicationExpirationDate"));
		setValue("AUTH_RESP_CODE", dataDMap.get("AUTHRespCode"));
		setValue("POST_ISSUE_RESULT", dataDMap.get("postIssuanceResult"));
		setValue("CHIP_COND_CODE", dataDMap.get("chipConditionCode"));
		setValue("TE_ENTRY_CAP", dataDMap.get("terminalEntryCapability"));
		setValue("CARD_VERFY_RESULT", dataDMap.get("cardVerificationResult"));
		setValue("EMV_JCB_INSTALL_TERM", dataDMap.get("emvJCBInstallTerm"));
		setValue("EMV_JCB_MCHT_NO", dataDMap.get("emvJCBMchtNo"));
		setValue("EMV_JCB_MCHT_STATE", dataDMap.get("emvJCBMchtState"));
		setValue("EMV_JCB_TE_ID", dataDMap.get("emvJCBTeId"));
		setValue("EMV_JCB_CHEQUE_FLAG", dataDMap.get("emvJCBChequeFlag"));
		setValue("EMV_JCB_CB_DATE", dataDMap.get("emvJCBCbDate"));
		setValue("EMV_JCB_REASON_CODE", dataDMap.get("emvJCBReasonCode"));
		setValue("EMV_JCB_EX_RATE", dataDMap.get("emvJCBExRate"));
		setValue("EMV_JCB_DEST_CURR", dataDMap.get("emvJCBDestCurr"));
		setValue("EMV_ACQ_TE_ID", dataDMap.get("teAcqTeId"));
		setValue("TE_SEND_DATE", teSendDate);
		setValue("TE_BATCH_NO", dataDMap.get("teBatchNo"));
		setValue("TE_TX_NUM", dataDMap.get("teTxNum"));
		setValue("EMV_ACQ_DATA", dataDMap.get("emvAcqData"));
		setValue("PURCHASE_TIME", dataDMap.get("purchaseTime"));
		setValue("EMV_IAD2", dataDMap.get("issuerApplicationData2"));
		setValue("SUBMERCHANT_ID", dataDMap.get("subMerchantId"));
		setValue("VROL_CASE_NUM", vROLCaseNumber );
		setValue("PAYMENT_FA_ID", paymentFacilitatorID);
		setValue("FFI", dataDMap.get("formFactorIndicator"));
		setValue("EMV_ERROR_CODE2", dataDMap.get("errorReplyNo2"));
		
		
		setValueDouble("BONUS_PAY_CASH", bonusPayCashDouble.doubleValue());
		setValueDouble("BONUS_TRANS_BP", comc.str2double(dataDMap.get("bonusTransBp")));
		
		setValueDouble("INSTALL_FEE", comc.str2double(dataDMap.get("installCharges")));
		setValueDouble("INSTALL_FIRST_AMT", comc.str2double(dataDMap.get("installFirstAmt")));
		setValueDouble("INSTALL_PER_AMT", comc.str2double(dataDMap.get("installPerAmt")));
		setValueDouble("INSTALL_TOT_TERM", comc.str2double(dataDMap.get("installTotTerm")));

		// ==============================================
		setValue("BATCH_FLAG", "");
		setValue("BATCH_DATE", "");
		setValue("BATCH_NO", "");
		setValue("PROG_NAME", javaProgram);
		setValue("MOD_USER", javaProgram);
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		// ==============================================
		
		insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert bil_fiscdtl duplicate", "", comcr.hCallBatchSeqno);
        }
		commTxBill.printDebugString("新增bil_fiscdtl成功!!!!!!!!!!!!!!!!!!");		
	}
		/**
		 * separate each column form the bytesArr 
		 * @throws UnsupportedEncodingException *
		 **/
		private HashMap<String,String> getDataD(byte[] bytesArr1, byte[] bytesArr2) throws UnsupportedEncodingException {
			
			HashMap<String,String> hashMap = new HashMap<String,String>();
			
			// ===========================record1===========================
			hashMap.put("transactionType", CommTxBill.subByteToStr(bytesArr1,0,4));  // 財金交易代號
			hashMap.put("reversalIndicator", CommTxBill.subByteToStr(bytesArr1,4,5));  // 更正識別碼
			hashMap.put("bankNumber", CommTxBill.subByteToStr(bytesArr1,5,13));  // 參加單位代號
			hashMap.put("sourceBINOrICANumber", CommTxBill.subByteToStr(bytesArr1,13,19));  // 傳送端BIN/ICA
			hashMap.put("destinationBINOrICANo", CommTxBill.subByteToStr(bytesArr1,19,25));  // 接收端BIN/ICA
			hashMap.put("acquirerBusinessID", CommTxBill.subByteToStr(bytesArr1,25,33));  // 代理單位代號
			hashMap.put("cardNumber", CommTxBill.subByteToStr(bytesArr1,33,49));  // 卡號
			hashMap.put("localDateReceived", CommTxBill.subByteToStr(bytesArr1,49,55));  // 當地接收日期
			hashMap.put("localTimeReceived", CommTxBill.subByteToStr(bytesArr1,55,61));  // 當地接收時間(hhmmss)
			hashMap.put("inputSequenceNumber", CommTxBill.subByteToStr(bytesArr1,61,67));  // 輸入序號(財金編流水號)
			hashMap.put("sourceAmount", CommTxBill.subByteToStr(bytesArr1,67,79));  // 購買地金額
			hashMap.put("sourceCurrency", CommTxBill.subByteToStr(bytesArr1,79,82));  // 購買地幣別
			hashMap.put("destinationAmount", CommTxBill.subByteToStr(bytesArr1,82,94));  // 當地金額
			hashMap.put("USDSettlementAmount", CommTxBill.subByteToStr(bytesArr1,94,106));  // 清算金額
			hashMap.put("acceptorID", CommTxBill.subByteToStr(bytesArr1,106,121));  // 特店代號
			hashMap.put("merchantCategoryCode", CommTxBill.subByteToStr(bytesArr1,121,125));  // 特店行業別
			hashMap.put("merchantName", CommTxBill.subByteToStr(bytesArr1,125,150));  // 特店英文名稱
			hashMap.put("merchantCityOrLocation", CommTxBill.subByteToStr(bytesArr1,150,163));  // 特店city
			hashMap.put("merchantState", CommTxBill.subByteToStr(bytesArr1,163,165));  // 特店省份
			hashMap.put("merchantZipCode", CommTxBill.subByteToStr(bytesArr1,165,170));  // 特店ZIP
			hashMap.put("merchantCountryCode", CommTxBill.subByteToStr(bytesArr1,170,173));  // 特店國家代號
			hashMap.put("specialConditionIndicator", CommTxBill.subByteToStr(bytesArr1,173,175));  // 特殊條件識別碼
			hashMap.put("purchaseDate", CommTxBill.subByteToStr(bytesArr1,175,181));  // 購貨日期(西元YYYYMMDD)
			hashMap.put("authorisationResponseID", CommTxBill.subByteToStr(bytesArr1,181,187));  // 授權碼
			hashMap.put("mailOrTelephoneECIIndicator", CommTxBill.subByteToStr(bytesArr1,187,188));  // MAIL/PHONE ECI識別碼
			hashMap.put("prepaidCardIndicator", CommTxBill.subByteToStr(bytesArr1,188,189));  // 購買預付卡識別碼(Only VISA)
			hashMap.put("CAT", CommTxBill.subByteToStr(bytesArr1,189,190));  // 自助端末機識別碼
			hashMap.put("terminalID", CommTxBill.subByteToStr(bytesArr1,190,198));  // 端末機代碼
			hashMap.put("POSTerminalCapability", CommTxBill.subByteToStr(bytesArr1,198,199));  // POS端末機性能
			hashMap.put("POSEntryMode", CommTxBill.subByteToStr(bytesArr1,199,201));  // POS輸入型態
			hashMap.put("processDate", CommTxBill.subByteToStr(bytesArr1,201,205));  // VISA/Master處理日(西元YDDD)
			hashMap.put("microfilmReferenceNoAcquirersReferenceNo", CommTxBill.subByteToStr(bytesArr1,205,228));  // 微縮影編號
			hashMap.put("usageCode", CommTxBill.subByteToStr(bytesArr1,228,229));  // 使用碼
			hashMap.put("chargebackRepresentOrRetrievalReasonCode", CommTxBill.subByteToStr(bytesArr1,229,231));  // 沖正駁回/調單理由碼
			hashMap.put("chargebackReferenceNo", CommTxBill.subByteToStr(bytesArr1,231,241));  // 沖正參考號碼/跨境支付手續費整數8位小數2位
			hashMap.put("specialChargebackIndicatorOrVCRFSOrMASTERCOMIndicator", CommTxBill.subByteToStr(bytesArr1,241,242));  // 特殊沖正/VCRFS識別碼
			hashMap.put("cardWarningBulletinIndicator", CommTxBill.subByteToStr(bytesArr1,242,243));  // 黑名單識別碼
			hashMap.put("documentationIndicator", CommTxBill.subByteToStr(bytesArr1,243,244));  // 附寄文件識別碼
			hashMap.put("feeReasonCode", CommTxBill.subByteToStr(bytesArr1,244,248));  // EMV_費用/訊息原因碼
			hashMap.put("messageTextOrMerchantChineseName", CommTxBill.subByteToStr(bytesArr1,248,318));  // 特店中文名稱
			hashMap.put("serviceCode", CommTxBill.subByteToStr(bytesArr1,318,321));  // Service Code
			hashMap.put("UCAF", CommTxBill.subByteToStr(bytesArr1,321,322));  // UCAF
			hashMap.put("DCCIndicator", CommTxBill.subByteToStr(bytesArr1,322,323));  // DCC識別碼
			hashMap.put("fraudNotificationDate", CommTxBill.subByteToStr(bytesArr1,323,327));  // FRAUD NOTIFICATION DATE(YDDD)
			hashMap.put("acquirerResponseCode", CommTxBill.subByteToStr(bytesArr1,327,328));  // 代理單位回覆碼
			hashMap.put("floorLimitIndicator", CommTxBill.subByteToStr(bytesArr1,328,329));  // 特店限額識別碼
			hashMap.put("PCASIndicator", CommTxBill.subByteToStr(bytesArr1,329,330));  // PCAS識別碼
			hashMap.put("interfaceTraceNumber", CommTxBill.subByteToStr(bytesArr1,330,336));  // 介面追蹤號碼
			hashMap.put("cashbackAmount", CommTxBill.subByteToStr(bytesArr1,336,345));  // 找回現金(V:小數二位,M:依幣別決定小數位)
			hashMap.put("issuerControlNumber", CommTxBill.subByteToStr(bytesArr1,345,355));  // 發卡單位控制碼
			hashMap.put("interchangeRateDesignator", CommTxBill.subByteToStr(bytesArr1,355,357));  // Interchange Rate Designator
			hashMap.put("reimbursementAttribute", CommTxBill.subByteToStr(bytesArr1,357,358));  // 交易處理費屬性
			hashMap.put("settlementFlag", CommTxBill.subByteToStr(bytesArr1,358,359));  // 清算識別碼
			hashMap.put("originalTransactionType", CommTxBill.subByteToStr(bytesArr1,359,363));  // 交易退回時原交易代號
			hashMap.put("firstChargebackMessageReasonCode", CommTxBill.subByteToStr(bytesArr1,359,363));  // 原沖正交易訊息理由碼
			hashMap.put("returnedItemReasonCode", CommTxBill.subByteToStr(bytesArr1,363,365));  // 交易退回理由碼
			hashMap.put("transactionID", CommTxBill.subByteToStr(bytesArr1,365,380));  // 交易識別碼
			hashMap.put("cardLevelId", CommTxBill.subByteToStr(bytesArr1,380,382));  // 卡片級別識別碼
			hashMap.put("authorizativeCheckNo", CommTxBill.subByteToStr(bytesArr1,382,386));  // 授權欄位驗證碼
			hashMap.put("payType", CommTxBill.subByteToStr(bytesArr1,386,387));  // 支付型態
			hashMap.put("destinationCurrency", CommTxBill.subByteToStr(bytesArr1,387,390));  // Destination curr
			hashMap.put("multipleClearingSequenceNumber", CommTxBill.subByteToStr(bytesArr1,390,392));  // 多筆清算交易對應序號
			hashMap.put("reserveData1", CommTxBill.subByteToStr(bytesArr1,392,399));  // 保留欄位
			hashMap.put("QPSOrPayPassChargebackEligibilityIndicator", CommTxBill.subByteToStr(bytesArr1,399,400));  // QPS/PayPass交易後續是否可沖正
			hashMap.put("fraudNotificationServiceChargebackCounter", CommTxBill.subByteToStr(bytesArr1,400,402));  // 偽冒卡chargeback次數
			hashMap.put("surchargeAmount", CommTxBill.subByteToStr(bytesArr1,402,410));  // ADDN/Surcharge/找回現金(V:整數10位,小數二位;M/J:依幣別決定小數位)
			hashMap.put("surchargeCreditOrDebitIndicator", CommTxBill.subByteToStr(bytesArr1,410,411));  // ADDN/Surcharge正負Sign
			hashMap.put("surchargeAmountincardholderbillingcurrency", CommTxBill.subByteToStr(bytesArr1,411,419));  // ADDN/Surcharge(持卡人billing幣別)
			hashMap.put("tokenAssuranceLevel", CommTxBill.subByteToStr(bytesArr1,419,421));  // Token Assurance Level
			hashMap.put("tokenRequestorID", CommTxBill.subByteToStr(bytesArr1,421,432));  // Token Requestor ID
			hashMap.put("reserveData2", CommTxBill.subByteToStr(bytesArr1,432,446));  // 保留欄位
			hashMap.put("errorReplyNo", CommTxBill.subByteToStr(bytesArr1,446,450));  // 錯誤回覆碼
			
			/*分期付款資料  */
			if ("I".equals(hashMap.get("payType")) ||  "E".equals(hashMap.get("payType"))) {
				hashMap.put("installTotTerm", CommTxBill.subByteToStr(bytesArr1,293,295));  // 分期付款期數
				hashMap.put("installFirstAmt", CommTxBill.subByteToStr(bytesArr1,295,303));  // 分期付款首期金額
				hashMap.put("installPerAmt", CommTxBill.subByteToStr(bytesArr1,303,311));  // 分期付款每期金額
				hashMap.put("installCharges", CommTxBill.subByteToStr(bytesArr1,311,317));  // 分期付款手續費
			} else {
				hashMap.put("installTotTerm", "00");  // 分期付款期數
				hashMap.put("installFirstAmt", "00000000");  // 分期付款首期金額
				hashMap.put("installPerAmt", "00000000");  // 分期付款每期金額
				hashMap.put("installCharges", "000000");  // 分期付款手續費
			}
			
			/*紅利折抵資料  */
			if ("1".equals(hashMap.get("payType")) ||  "2".equals(hashMap.get("payType")) ||  "3".equals(hashMap.get("payType")) ) {
				hashMap.put("bonusTransBp", CommTxBill.subByteToStr(bytesArr1,293,301));  // 紅利折抵點數 
				hashMap.put("bonusPayCash", CommTxBill.subByteToStr(bytesArr1,301,313));  // 紅利折抵後之支付金額 
			} else {
				hashMap.put("bonusTransBp", "00000000");  // 紅利折抵點數 
				hashMap.put("bonusPayCash", "000000000000");  // 紅利折抵後之支付金額 
			}
			
			/*QR payment  (merchantPan & Tpan先不轉)*/

			// ===========================record1===========================
			// ===========================record2===========================
			
			hashMap.put("transactionType2", CommTxBill.subByteToStr(bytesArr2,0,4));  //  
			hashMap.put("recordIndicator", CommTxBill.subByteToStr(bytesArr2,4,5));  //  
			hashMap.put("cashbackAmount1", CommTxBill.subByteToStr(bytesArr2,5,17));  // 
			hashMap.put("cryptoAmount", CommTxBill.subByteToStr(bytesArr2,17,29));  // 產生AC時的授權金額
			hashMap.put("transactionCurrencyCode", CommTxBill.subByteToStr(bytesArr2,29,32));  // 端末機交易幣別碼
			hashMap.put("transactionTypeAC", CommTxBill.subByteToStr(bytesArr2,32,34));  // 產生AC時的交易類別
			hashMap.put("PANSequenceNumber", CommTxBill.subByteToStr(bytesArr2,34,37));  // 晶片卡卡片序號
			hashMap.put("transactionDate", CommTxBill.subByteToStr(bytesArr2,37,43));  // 端末機交易日期
			hashMap.put("terminalCapabilitiesProfile", CommTxBill.subByteToStr(bytesArr2,43,49));  // 端末機功能表
			hashMap.put("terminalCountryCode", CommTxBill.subByteToStr(bytesArr2,49,52));  // 端末機國別碼
			hashMap.put("interfaceDeviceSerialNumber", CommTxBill.subByteToStr(bytesArr2,52,60));  // 端末機唯一序號
			hashMap.put("unpredictableNumber ", CommTxBill.subByteToStr(bytesArr2,60,68));  // 產生AC時所需亂數
			hashMap.put("applicationTransactionNumber", CommTxBill.subByteToStr(bytesArr2,68,72));  // 卡片交易序號
			hashMap.put("applicationInterchangeProfile", CommTxBill.subByteToStr(bytesArr2,72,76));  // 卡片應用程式功能表
			hashMap.put("applicationCryptogram", CommTxBill.subByteToStr(bytesArr2,76,92));  // 交易驗證資料(AC)
			hashMap.put("issuerApplicationData", CommTxBill.subByteToStr(bytesArr2,92,124));  // 發卡行產生AC的資訊
			hashMap.put("terminalVerificationResult", CommTxBill.subByteToStr(bytesArr2,124,134));  // 端末機所有交易行為
			hashMap.put("cardholderVerificationMethodResult", CommTxBill.subByteToStr(bytesArr2,134,140));  // 持卡人認證結果
			hashMap.put("cryptogramInformationData", CommTxBill.subByteToStr(bytesArr2,140,142));  // 回傳Approve/Online/Denied的type
			hashMap.put("applicationExpirationDate", CommTxBill.subByteToStr(bytesArr2,142,150));  // 晶片卡應用程式失效日期
			hashMap.put("AUTHRespCode", CommTxBill.subByteToStr(bytesArr2,150,152));  // 授權回應碼(含離線回應碼)
			hashMap.put("postIssuanceResult", CommTxBill.subByteToStr(bytesArr2,152,162));  // POST ISSUANCE結果
			hashMap.put("chipConditionCode", CommTxBill.subByteToStr(bytesArr2,162,163));  // Chip condition code
			hashMap.put("terminalEntryCapability", CommTxBill.subByteToStr(bytesArr2,163,164));  // 端末機功能(0:Non-EMV,5:EMV)
			hashMap.put("cardVerificationResult", CommTxBill.subByteToStr(bytesArr2,164,176));  // Card 紀錄所有交易行為
			hashMap.put("reserveForChip", CommTxBill.subByteToStr(bytesArr2,176,204));  //  
			hashMap.put("emvJCBInstallTerm", CommTxBill.subByteToStr(bytesArr2,204,206));  // JCB_分期付款期數
			hashMap.put("emvJCBMchtNo", CommTxBill.subByteToStr(bytesArr2,206,222));  // JCB_特店代號
			hashMap.put("emvJCBMchtState", CommTxBill.subByteToStr(bytesArr2,222,225));  // JCB_特店省份
			hashMap.put("emvJCBTeId", CommTxBill.subByteToStr(bytesArr2,225,240));  // JCB_端末機代碼
			hashMap.put("emvJCBChequeFlag", CommTxBill.subByteToStr(bytesArr2,240,241));  // JCB_旅行支票標示
			hashMap.put("emvJCBCbDate", CommTxBill.subByteToStr(bytesArr2,241,249));  // JCB_更正/沖正/駁回日期
			hashMap.put("emvJCBReasonCode", CommTxBill.subByteToStr(bytesArr2,249,252));  // JCB_沖正/駁回理由碼
			hashMap.put("emvJCBExRate", CommTxBill.subByteToStr(bytesArr2,252,260));  // JCB_交換匯率
			hashMap.put("emvJCBDestCurr", CommTxBill.subByteToStr(bytesArr2,260,263));  // JCB_當地幣別
			hashMap.put("teAcqTeId", CommTxBill.subByteToStr(bytesArr2,263,279));  // 收單端末機代號
			hashMap.put("teSendDate", CommTxBill.subByteToStr(bytesArr2,279,285));  // 端末機傳送資料日期
			hashMap.put("teBatchNo", CommTxBill.subByteToStr(bytesArr2,285,289));  // 端末機上傳資料批次號碼
			hashMap.put("teTxNum", CommTxBill.subByteToStr(bytesArr2,289,295));  // 端末機交易序號
			hashMap.put("emvAcqData", CommTxBill.subByteToStr(bytesArr2,295,335));  // 收單相關資料
			hashMap.put("purchaseTime", CommTxBill.subByteToStr(bytesArr2,335,341));  // 購貨時間(HHMMSS)
			hashMap.put("issuerApplicationData2", CommTxBill.subByteToStr(bytesArr2,341,373));  // 發卡行產生AC的資訊2
			hashMap.put("subMerchantId", CommTxBill.subByteToStr(bytesArr2,373,381));  // 次特約商代碼
			hashMap.put("VROLCaseNumberOrPaymentFacilitatorId", CommTxBill.subByteToStr(bytesArr2,381,392));  // Payment Facilitator ID
			hashMap.put("formFactorIndicator", CommTxBill.subByteToStr(bytesArr2,392,404));  // 識別交易載具
			hashMap.put("reserveData3", CommTxBill.subByteToStr(bytesArr2,404,446));  //  
			hashMap.put("errorReplyNo2", CommTxBill.subByteToStr(bytesArr2,446,450));  // 錯誤回覆碼(Record2)

			// ===========================record2===========================

			return hashMap;

		}
	

	public static void main(String[] args) {
		BilE133 proc = new BilE133();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}
	

}
