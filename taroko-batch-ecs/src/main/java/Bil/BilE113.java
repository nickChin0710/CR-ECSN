/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/09  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/23  V1.00.04    shiyuqi     coding standard, rename field method & format    
*  109/09/04  V1.00.05    Zuwei     code scan issue    
*  109/09/14  V1.00.06    Zuwei     code scan issue    
*  109-10-19  V1.00.07    shiyuqi       updated for project coding standard     *
*  111-09-22  V1.00.08    JeffKung     加轉特店分期及紅利折抵資料                        *
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


public class BilE113 extends AccessDAO {
    private final String progname = "FISC-ICFXGQB磁條卡請款資料入bil_fiscdtl處理 111/09/22  V1.00.08";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		 HashMap<String,String> dataDMap = null;
		 String text = "";
		 String fileNameD="",  fileDate="", bankNo="";
		 String filePath = "", filePathFromDB = "";
		 byte[] bytesArr = null;
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
	            commTxBill = new CommTxBill(getDBconnect(), getDBalias(),false);
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
				if( ! comc.getSubString(fileNameD, 1, 9).equalsIgnoreCase(bankNo) ) {
					showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
					return -1;
				}
				
				//open  F00600000.ICFXGQBD.XXXXX file ==================
				filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
				fileNameD = SecurityUtil.verifyPath(fileNameD);
				filePath = Paths.get(filePathFromDB, fileNameD ).toString();
				//inputFileD  = openInputText(filePath, "big5");
				if(openBinaryInput(filePath) == false) {
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
				
				 byte[] bytes = new byte[450];
				 int readCnt = 0;
				
				 while(true) {
					 readCnt = readBinFile(bytes);
					 if(readCnt < 5)
							break;
					 
					 
					 if(bytes.length != 0) {
						
						//排除通知檔內第一筆，因為第一筆是美元匯率資訊
						if( commTxBill.isCurrRateTx( CommTxBill.subByteToStr(bytes, 0,4) ) ) {
							continue;
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
						
						dataDMap = getDataD(bytes);  // separate the columns in file
						
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
		String purchaseDate = "", localDateReceived = "";
		String binType = "";
		String prepaidCardIndicator = "", vROLCaseNumber = "", paymentFacilitatorID = "";
		//==================================================
		// 以下字串最後兩位皆為小數
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
			paymentFacilitatorID = dataDMap.get("paymentFacilitatorID");
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
		setValue("PURCHASE_TIME", dataDMap.get("purchaseTime"));
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
		setValue("QPS_CB_IND", dataDMap.get("QPSOrPayPassChargebackEligibilityIndicator"));
		setValue("FRAUD_CB_CNT", dataDMap.get("fraudNotificationServiceChargebackCounter"));
		setValue("ADDN_AMT", dataDMap.get("surchargeAmount"));
		setValue("ADDN_AMT_SIGN", dataDMap.get("surchargeCreditOrDebitIndicator"));
		setValue("ADDN_BILL_CURR", dataDMap.get("surchargeAmountincardholderbillingcurrency"));
		setValue("PAYMENT_TYPE", dataDMap.get("payType"));
		setValue("DEST_CURR", dataDMap.get("destinationCurrency"));
		setValue("MUTI_CLEARING_SEQ", dataDMap.get("multipleClearingSequenceNumber"));
		setValue("TOKEN_ASSURE_LEVEL", dataDMap.get("tokenAssuranceLevel"));
		setValue("TOKEN_REQUESTOR_ID", dataDMap.get("tokenRequestorID"));
		setValue("SUBMERCHANT_ID", dataDMap.get("subMerchantID"));
		setValue("VROL_CASE_NUM", vROLCaseNumber);
		setValue("PAYMENT_FA_ID", paymentFacilitatorID);
		setValue("EMV_ERROR_CODE", dataDMap.get("errorReplyNo"));
		
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
		 * separate each column from bytesArr
		 * @throws UnsupportedEncodingException *
		 **/
		private HashMap<String,String> getDataD( byte[] bytesArr) throws UnsupportedEncodingException {
			
			HashMap<String,String> hashMap = new HashMap<String,String>();
			
			hashMap.put("transactionType", CommTxBill.subByteToStr(bytesArr,0,4));  // 財金交易代號
			hashMap.put("reversalIndicator", CommTxBill.subByteToStr(bytesArr,4,5));  // 更正識別碼
			hashMap.put("bankNumber", CommTxBill.subByteToStr(bytesArr,5,13));  // 參加單位代號
			hashMap.put("sourceBINOrICANumber", CommTxBill.subByteToStr(bytesArr,13,19));  // 傳送端BIN/ICA
			hashMap.put("destinationBINOrICANo", CommTxBill.subByteToStr(bytesArr,19,25));  // 接收端BIN/ICA
			hashMap.put("acquirerBusinessID", CommTxBill.subByteToStr(bytesArr,25,33));  // 代理單位代號
			hashMap.put("cardNumber", CommTxBill.subByteToStr(bytesArr,33,49));  // 卡號
			hashMap.put("localDateReceived", CommTxBill.subByteToStr(bytesArr,49,55));  // 當地接收日期
			hashMap.put("purchaseTime", CommTxBill.subByteToStr(bytesArr,55,61));  // 購貨時間(HHMMSS)
			hashMap.put("inputSequenceNumber", CommTxBill.subByteToStr(bytesArr,61,67));  // 輸入序號(財金編流水號)
			hashMap.put("sourceAmount", CommTxBill.subByteToStr(bytesArr,67,79));  // 購買地金額
			hashMap.put("sourceCurrency", CommTxBill.subByteToStr(bytesArr,79,82));  // 購買地幣別
			hashMap.put("destinationAmount", CommTxBill.subByteToStr(bytesArr,82,94));  // 當地金額
			hashMap.put("USDSettlementAmount", CommTxBill.subByteToStr(bytesArr,94,106));  // 清算金額
			hashMap.put("acceptorID", CommTxBill.subByteToStr(bytesArr,106,121));  // 特店代號
			hashMap.put("merchantCategoryCode", CommTxBill.subByteToStr(bytesArr,121,125));  // 特店行業別
			hashMap.put("merchantName", CommTxBill.subByteToStr(bytesArr,125,150));  // 特店英文名稱
			hashMap.put("feeCollectionControlNumber", CommTxBill.subByteToStr(bytesArr,125,150));  //  
			hashMap.put("merchantCityOrLocation", CommTxBill.subByteToStr(bytesArr,150,163));  // 特店city
			hashMap.put("merchantState", CommTxBill.subByteToStr(bytesArr,163,165));  // 特店省份
			hashMap.put("merchantZipCode", CommTxBill.subByteToStr(bytesArr,165,170));  // 特店ZIP
			hashMap.put("merchantCountryCode", CommTxBill.subByteToStr(bytesArr,170,173));  // 特店國家代號
			hashMap.put("specialConditionIndicator", CommTxBill.subByteToStr(bytesArr,173,175));  // 特殊條件識別碼
			hashMap.put("purchaseDate", CommTxBill.subByteToStr(bytesArr,175,181));  // 購貨日期
			hashMap.put("authorisationResponseID", CommTxBill.subByteToStr(bytesArr,181,187));  // 授權碼
			hashMap.put("mailOrTelephoneECIIndicator", CommTxBill.subByteToStr(bytesArr,187,188));  // MAIL/PHONE ECI識別碼
			hashMap.put("prepaidCardIndicator", CommTxBill.subByteToStr(bytesArr,188,189));  // 購買預付卡識別碼(Only VISA)
			hashMap.put("CAT", CommTxBill.subByteToStr(bytesArr,189,190));  // 自助端末機識別碼
			hashMap.put("terminalID", CommTxBill.subByteToStr(bytesArr,190,198));  // 端末機代碼
			hashMap.put("POSTerminalCapability", CommTxBill.subByteToStr(bytesArr,198,199));  // POS端末機性能
			hashMap.put("POSEntryMode", CommTxBill.subByteToStr(bytesArr,199,201));  // POS輸入型態
			hashMap.put("processDate", CommTxBill.subByteToStr(bytesArr,201,205));  // VISA/Master處理日
			hashMap.put("microfilmReferenceNoAcquirersReferenceNo", CommTxBill.subByteToStr(bytesArr,205,228));  // 微縮影編號
			hashMap.put("usageCode", CommTxBill.subByteToStr(bytesArr,228,229));  // 使用碼
			hashMap.put("chargebackRepresentOrRetrievalReasonCode", CommTxBill.subByteToStr(bytesArr,229,231));  // 沖正駁回/調單理由碼
			hashMap.put("chargebackReferenceNo", CommTxBill.subByteToStr(bytesArr,231,241));  // 沖正參考號碼/跨境支付手續費整數8位小數2位
			hashMap.put("specialChargebackIndicatorOrVCRFSOrMASTERCOMIndicator", CommTxBill.subByteToStr(bytesArr,241,242));  // 特殊沖正/VCRFS識別碼
			hashMap.put("cardWarningBulletinIndicator", CommTxBill.subByteToStr(bytesArr,242,243));  // 黑名單識別碼
			hashMap.put("documentationIndicator", CommTxBill.subByteToStr(bytesArr,243,244));  // 附寄文件識別碼
			hashMap.put("feeReasonCode", CommTxBill.subByteToStr(bytesArr,244,248));  // EMV_費用/訊息原因碼
			hashMap.put("messageReasonCode", CommTxBill.subByteToStr(bytesArr,244,248));  //  
			hashMap.put("messageTextOrMerchantChineseName", CommTxBill.subByteToStr(bytesArr,248,318));  // 特店中文名稱
			hashMap.put("serviceCode", CommTxBill.subByteToStr(bytesArr,318,321));  // Service Code
			hashMap.put("UCAF", CommTxBill.subByteToStr(bytesArr,321,322));  // UCAF
			hashMap.put("DCCIndicator", CommTxBill.subByteToStr(bytesArr,322,323));  // DCC識別碼
			hashMap.put("fraudNotificationDate", CommTxBill.subByteToStr(bytesArr,323,327));  // FRAUD NOTIFICATION DATE
			hashMap.put("acquirerResponseCode", CommTxBill.subByteToStr(bytesArr,327,328));  // 代理單位回覆碼
			hashMap.put("floorLimitIndicator", CommTxBill.subByteToStr(bytesArr,328,329));  // 特店限額識別碼
			hashMap.put("PCASIndicator", CommTxBill.subByteToStr(bytesArr,329,330));  // PCAS識別碼
			hashMap.put("interfaceTraceNumber", CommTxBill.subByteToStr(bytesArr,330,336));  // 介面追蹤號碼
			hashMap.put("cashbackAmount", CommTxBill.subByteToStr(bytesArr,336,345));  // 找回現金(V:小數二位,M:依幣別決定小數位)
			hashMap.put("issuerControlNumber", CommTxBill.subByteToStr(bytesArr,345,355));  // 發卡單位控制碼
			hashMap.put("interchangeRateDesignator", CommTxBill.subByteToStr(bytesArr,355,357));  // Interchange Rate Designator
			hashMap.put("reimbursementAttribute", CommTxBill.subByteToStr(bytesArr,357,358));  // 交易處理費屬性
			hashMap.put("settlementFlag", CommTxBill.subByteToStr(bytesArr,358,359));  // 清算識別碼
			hashMap.put("originalTransactionType", CommTxBill.subByteToStr(bytesArr,359,363));  // 交易退回時原交易代號
			hashMap.put("firstChargebackMessageReasonCode", CommTxBill.subByteToStr(bytesArr,359,363));  // 原沖正交易訊息理由碼
			hashMap.put("returnedItemReasonCode", CommTxBill.subByteToStr(bytesArr,363,365));  // 交易退回理由碼
			hashMap.put("transactionID", CommTxBill.subByteToStr(bytesArr,365,380));  // 交易識別碼
			hashMap.put("cardLevelId", CommTxBill.subByteToStr(bytesArr,380,382));  // 卡片級別識別碼
			hashMap.put("authorizativeCheckNo", CommTxBill.subByteToStr(bytesArr,382,386));  // 授權欄位驗證碼
			hashMap.put("QPSOrPayPassChargebackEligibilityIndicator", CommTxBill.subByteToStr(bytesArr,386,387));  // QPS/PayPass交易後續是否可沖正
			hashMap.put("fraudNotificationServiceChargebackCounter", CommTxBill.subByteToStr(bytesArr,387,389));  // 偽冒卡chargeback次數
			hashMap.put("surchargeAmount", CommTxBill.subByteToStr(bytesArr,389,397));  // ADDN/Surcharge/找回現金(V:整數10位,小數二位;M/J:依幣別決定小數位)
			hashMap.put("surchargeCreditOrDebitIndicator", CommTxBill.subByteToStr(bytesArr,397,398));  // ADDN/Surcharge正負Sign
			hashMap.put("surchargeAmountincardholderbillingcurrency", CommTxBill.subByteToStr(bytesArr,398,406));  // ADDN/Surcharge(持卡人billing幣別)
			hashMap.put("payType", CommTxBill.subByteToStr(bytesArr,406,407));  // 支付型態
			hashMap.put("destinationCurrency", CommTxBill.subByteToStr(bytesArr,407,410));  // Destination curr
			hashMap.put("multipleClearingSequenceNumber", CommTxBill.subByteToStr(bytesArr,410,412));  // 多筆清算交易對應序號
			hashMap.put("tokenAssuranceLevel", CommTxBill.subByteToStr(bytesArr,412,414));  // Token Assurance Level
			hashMap.put("tokenRequestorID", CommTxBill.subByteToStr(bytesArr,414,425));  // Token Requestor ID
			hashMap.put("reserveData", CommTxBill.subByteToStr(bytesArr,425,427));  // 保留欄位
			hashMap.put("subMerchantID", CommTxBill.subByteToStr(bytesArr,427,435));  // 次特約商代碼
			hashMap.put("VROLCaseNumber", CommTxBill.subByteToStr(bytesArr,435,446));  // VROL Case Number
			hashMap.put("paymentFacilitatorID", CommTxBill.subByteToStr(bytesArr,435,446));  // Payment Facilitator ID
			hashMap.put("errorReplyNo", CommTxBill.subByteToStr(bytesArr,446,450));  // 錯誤回覆碼
			
			/*分期付款資料  */
			if ("I".equals(hashMap.get("payType")) ||  "E".equals(hashMap.get("payType"))) {
				hashMap.put("installTotTerm", CommTxBill.subByteToStr(bytesArr,293,295));  // 分期付款期數
				hashMap.put("installFirstAmt", CommTxBill.subByteToStr(bytesArr,295,303));  // 分期付款首期金額
				hashMap.put("installPerAmt", CommTxBill.subByteToStr(bytesArr,303,311));  // 分期付款每期金額
				hashMap.put("installCharges", CommTxBill.subByteToStr(bytesArr,311,317));  // 分期付款手續費
			} else {
				hashMap.put("installTotTerm", "00");  // 分期付款期數
				hashMap.put("installFirstAmt", "00000000");  // 分期付款首期金額
				hashMap.put("installPerAmt", "00000000");  // 分期付款每期金額
				hashMap.put("installCharges", "000000");  // 分期付款手續費
			}
			
			/*紅利折抵資料  */
			if ("1".equals(hashMap.get("payType")) ||  "2".equals(hashMap.get("payType")) ||  "3".equals(hashMap.get("payType")) ) {
				hashMap.put("bonusTransBp", CommTxBill.subByteToStr(bytesArr,293,301));  // 紅利折抵點數 
				hashMap.put("bonusPayCash", CommTxBill.subByteToStr(bytesArr,301,313));  // 紅利折抵後之支付金額 
			} else {
				hashMap.put("bonusTransBp", "00000000");  // 紅利折抵點數 
				hashMap.put("bonusPayCash", "000000000000");  // 紅利折抵後之支付金額 
			}
			
			/*QR payment  (merchantPan & Tpan先不轉)*/
			
			return hashMap;
		}


	public static void main(String[] args) {
		BilE113 proc = new BilE113();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}

}
