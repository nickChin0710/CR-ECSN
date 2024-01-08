/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/16  V1.00.00    JustinWu     program initial                       *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109-07-03  V1.00.04    shiyuqi      updated for project coding standard   *
*  109/07/23  V1.00.05    shiyuqi      coding standard, rename field method & format                   * 
*  109-09-04  V1.00.01    yanghan      解决Portability Flaw: Locale Dependent Comparison问题    * 
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard   *
*  109/11/17  V1.00.08    JeffKung     日期格式錯誤修正, 檔案內容為民國年月日,誤認為西元年月日                              *
*  111/02/14  V1.00.09    Ryan         big5 to MS950                         *
*  111/02/18  V1.00.10    JeffKung     識別碼錯誤, 不處理, 不abend              *
*  111/09/22  V1.00.11    JeffKung     FeeCollection 欄位起迄錯誤                            *
*  111/11/24  V1.00.12    JeffKung     F01/F02只取自行卡交易,其餘交易不寫入table *
*  112/09/25  V1.00.13    JeffKung     paymentType="t"的處理
*  112/10/30  V1.00.14    JeffKung     F01的特店中文名稱特殊處理
******************************************************************************/
package Bil;


import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;
import com.CommTxBill.FiscTxData;
import com.CommTxBill.PlatformData;

import Dxc.Util.SecurityUtil;

public class BilE143 extends AccessDAO {
    private  final String progname = "FISC-ICFnnQBD二代帳務交易資料入bil_fiscdtl處理 112/10/30 V1.00.14";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;
    
    int totalRecordCnt = 0;
	int f01CorSponseCnt = 0;
	int f02OnUsCnt = 0;
	int totalWriteCnt = 0;

	 int mainProcess(String[] args) {
		 OEMPayData oemPayData = null;
		 int transactionTypeNo = 0;  //交易類型 調單交易: 2、費用交易: 3、一般交易: 1
		 String text1 = "";
		 String fileNameD="", filePathFromDB="", fileDate="", bankNo="";
		 String filePath = "";
		 byte[] bytesArrTemp = null;
		 
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
	            
	            // fileDate:傳檔日期FILE_DATE
				// filePathFromDB: ECS_FTP_LOG.local_dir
				// fileNameD:通知檔案名稱FILE_NAME
				// comcr.hCallBatchSeqno
		        fileDate = args[0];
		        filePathFromDB = args[1];
				fileNameD = args[2];
				comcr.hCallBatchSeqno = args[3];
				
				businessDate = getBusiDate();
         
		        
				// select the bank number
				bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
				
				// check whether file name's 2nd to 10th characters equals to the bank number
				if( ! comc.getSubString(fileNameD,1, 9).equalsIgnoreCase(bankNo) ) {
					showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
					return -1;
				}

				//open  F00600000.ICFnnQBD.XXXXX file ==================
				filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
				fileNameD = SecurityUtil.verifyPath(fileNameD);
				filePath = Paths.get(filePathFromDB, fileNameD ).toString();
				inputFileD  = openInputText(filePath, "MS950");
				if(inputFileD == -1) {
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
							
				
				// ======================WHILE BEGIN=====================================
				while(true) {
					text1 = readTextFile(inputFileD);
					bytesArrTemp = text1.getBytes("MS950");
					if(text1.trim().length() != 0 ) {
						
						// ===========================================
						switch(CommTxBill.subByteToStr(bytesArrTemp, 0, 1).toUpperCase(Locale.TAIWAN)) {			
						case "H":
							break;
						case "T":
							// 讀到trailer時，若還有資料尚未新增，則執行新增
							if( oemPayData != null ) {
								insertBilFiscdtl(oemPayData, fileNameD, fctlNo);
								oemPayData = null;
							}
							break;
						default: 
							// =================DEFAULT BEGIN=======================================
							switch(CommTxBill.subByteToStr(bytesArrTemp, 4, 5)) {							
							case "1":  // RECORD1
								// 讀到record1時，若還有資料尚未新增，則執行新增
								if( oemPayData != null ) {
									insertBilFiscdtl(oemPayData, fileNameD, fctlNo);
								}
								oemPayData = new OEMPayData();
								switch(CommTxBill.subByteToStr(bytesArrTemp, 2, 4)) {
								// 交易類別: 調單交易: 2、費用交易: 3、一般交易: 1
								case "51":
								case "52": // 調單交易:即財金交易代號的後二位為51、52
									transactionTypeNo = 2;
									break;
								case "10":
								case "20": // 費用交易:即財金交易代號的後二位為10、20
									transactionTypeNo = 3;
									break;
								default:  // 一般交易:即財金交易代號的後二位不為51、52、10、20
									transactionTypeNo = 1;
									break;
								}
								getDataD(oemPayData, bytesArrTemp, transactionTypeNo, 1);
								break;
							case "2":  // RECORD2
								getDataD(oemPayData, bytesArrTemp, transactionTypeNo, 2);
								break;
							case "3":  // RECORD3
								getDataD(oemPayData, bytesArrTemp, transactionTypeNo, 3);
								break;
							case "4":  // RECORD4
								getDataD(oemPayData, bytesArrTemp, transactionTypeNo, 4);
								break;
							default:
								//showLogMessage("E", "", "RECORD 識別碼錯誤 : " + CommTxBill.subByteToStr(bytesArrTemp, 4, 5)) ;
								//return -1;  識別碼錯誤不處理, 僅顯示不abend
								break;
							}
							break;
						  // =================DEFAULT BEGIN=======================================
						}
						// ===========================================
					}
					
		            if (endFile[inputFileD].equals("Y")) 
		            	break;
		            
				}	
				// ======================WHILE END=====================================

				commTxBill.updateProcCodeFromBilFiscctl(fctlNo);
	            
			} catch (Exception e) {
				expMethod = "mainProcess";  
	            expHandle(e); 
	            return  exceptExit; 
			}
            //=====================================  
            
            showLogMessage("I", "", "****程式執行結果筆數統計****");
            showLogMessage("I", "", "============================");
            showLogMessage("I", "", "總讀取資料筆數          :" + totalRecordCnt);
            showLogMessage("I", "", "F01共同供應契約筆數:" + f01CorSponseCnt);
            showLogMessage("I", "", "F02自行預借現金筆數:" + f02OnUsCnt);
            showLogMessage("I", "", "總寫入資料筆數          :" + totalWriteCnt);

            //=====================================
            finalProcess();
			return 0;
	}
	

	/**
	 * insert data into bil_fiscdtl
	 * @param oemPayData
	 * @param fileName
	 * @param fctlNo
	 * @throws Exception
	 */
	private void insertBilFiscdtl(OEMPayData oemPayData, String fileName, String fctlNo) throws Exception {
		String purchaseDate = "", teSendDate = "", transactionDate = "";
		String binType = "";
		String chargebackReferenceNo = "";
		Double eletronicalPlatFormFeeDouble = 0.0;
		Double surchargeAmountDouble = 0.0;
		//==================================================
		// 以下字串最後兩位皆為小數，因此需做轉換
		
		/*debug
		showLogMessage("I", "", "cardNo=["+ oemPayData.cardNumber + "]");
		showLogMessage("I", "", "sourceAmount=["+ oemPayData.sourceAmount + "]");
		showLogMessage("I", "", "destinationAmount=["+ oemPayData.destinationAmount + "]");
		showLogMessage("I", "", "settlementAmount=["+ oemPayData.settlementAmount + "]");
		showLogMessage("I", "", "bonusPayCash=["+ oemPayData.bonusPayCash + "]");
		showLogMessage("I", "", "surchargeAmountInCardholderBillingCurrency=["+ oemPayData.surchargeAmountInCardholderBillingCurrency + "]");
		*/
		
		totalRecordCnt++;
		
		//F01&F02為收單檔案
		//F01:只取共同供應契約的資料 (原來的ICACQQND範圍 , 只取特店代號="969799976011")
		//F02:ATM預借現金, 只取自行的CardBin (原來ICEMVQBD序號91)
		if ("F00600000.ICF01QBD".equals(comc.getSubString(fileName,0,18)) ) {
			
			if ("006969799976011".equals(oemPayData.acceptorID.trim()) == false) {
				return;
			}
			
			f01CorSponseCnt++;
			
		} else if ("F00600000.ICF02QBD".equals(comc.getSubString(fileName,0,18)) ) {
			//非本行的Card Bin
			String binNo = comc.getSubString(oemPayData.cardNumber,0,6);
			if (chkFromPtrBintable(binNo)==false) {
				return;
			}
			
			f02OnUsCnt++;
		}
		
		
		BigDecimal sourceAmountDouble = 
				commTxBill.getBigDecimalDividedBy100(oemPayData.sourceAmount);
	    BigDecimal destinationAmountDouble = 
				commTxBill.getBigDecimalDividedBy100(oemPayData.destinationAmount);
		BigDecimal settlementAmountDouble = 
				commTxBill.getBigDecimalDividedBy100(oemPayData.settlementAmount);
		BigDecimal bonusPayCashDouble = 
				commTxBill.getBigDecimalDividedBy100(oemPayData.bonusPayCash);
		BigDecimal surchargeAmountInCardholderBillingCurrencyDouble = 
				commTxBill.getBigDecimalDividedBy100(oemPayData.surchargeAmountInCardholderBillingCurrency);

		//==================================================
		// 以下字串最後四位皆為小數，因此需做轉換
		BigDecimal procureOrigAmtDouble = 
				commTxBill.getBigDecimalDivided(oemPayData.procureOrigAmt, 10000);
		//==================================================
		if(StringUtils.isNumeric(oemPayData.chargebackReferenceNo)) {
			// 若此字串為數字，則此字串為跨境電子支付平台手續費
			// 此字串最後兩位皆為小數，因此需做轉換
			chargebackReferenceNo = "";
			eletronicalPlatFormFeeDouble = 
					commTxBill.getBigDecimalDividedBy100(oemPayData.chargebackReferenceNo).doubleValue();
		}else {
			chargebackReferenceNo = oemPayData.chargebackReferenceNo;
			eletronicalPlatFormFeeDouble = 0.0;
		}
		//============= turn MMddyy into yyyyMMdd===================
		
		purchaseDate = CommTxBill.convertStrDateFormat(oemPayData.purchaseDate, "MMddyy", "yyyyMMdd");	
		
		//============= turn RRMMdd into yyyyMMdd===================
		//20201117 : 檔案格式內容為民國RRMMDD, 程式配合修改
		// RR為民國年末兩碼
		// yyyy = RR+100+1911  (允許空白或日期格式)
		
		if(oemPayData.teSendDate.length() > 2 && !commTxBill.isEmpty(comc.getSubString(oemPayData.teSendDate,0,2))) {
			long tmpLong = comcr.str2long(oemPayData.teSendDate.trim());
			teSendDate = String.format("%8d", tmpLong +20110000);
		}
		
		//============= turn yyMMdd into yyyyMMdd===================
		
		transactionDate = CommTxBill.convertStrDateFormat(oemPayData.transactionDate, "yyMMdd", "yyyyMMdd"); 
		
		//==================================================
		FiscTxData fiscTxData = commTxBill.getFiscTxData(oemPayData.transactionType, oemPayData.usageCode, 
				oemPayData.cardNumber, oemPayData.reimbursementAttribute, oemPayData.acceptorID.trim() , 
				oemPayData.mcc, oemPayData.reimbCode, oemPayData.settlementFlag);
		
		PlatformData platformData = fiscTxData.getPlatformData();
		
		
		//自繳稅
		if ("10".equals(platformData.getPlatformKind()) ) {
			if ("11".equals(comc.getSubString(oemPayData.reimbInfo, 5,7))) {
				platformData.setPlatformKind("11");
	   		 	platformData.setPlatformDesc("綜所稅申報自繳");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0061011001");
			} else if ("12".equals(comc.getSubString(oemPayData.reimbInfo, 5,7))){
				platformData.setPlatformKind("12");
	   		 	platformData.setPlatformDesc("營業稅申報自繳");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0061012001");
			} else if ("13".equals(comc.getSubString(oemPayData.reimbInfo, 5,7))){
				platformData.setPlatformKind("13");
	   		 	platformData.setPlatformDesc("外僑綜所稅申報自繳");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0061013001");
			} else if ("14".equals(comc.getSubString(oemPayData.reimbInfo, 5,7))){
				platformData.setPlatformKind("14");
	   		 	platformData.setPlatformDesc("營所稅申報自繳");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0061014001");
			}
			
		//查核定稅
		} else if ("20".equals(platformData.getPlatformKind()) ) {    
			if ("21".equals(comc.getSubString(oemPayData.reimbInfo, 3,5))) {
				platformData.setPlatformKind("21");
	   		 	platformData.setPlatformDesc("房屋稅");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0062021001");
			} else if ("22".equals(comc.getSubString(oemPayData.reimbInfo, 3,5))){
				platformData.setPlatformKind("22");
	   		 	platformData.setPlatformDesc("牌照稅");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0062022001");
			} else if ("23".equals(comc.getSubString(oemPayData.reimbInfo, 3,5))){
				platformData.setPlatformKind("23");
	   		 	platformData.setPlatformDesc("地價稅");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0062023001");
			} else if ("24".equals(comc.getSubString(oemPayData.reimbInfo, 3,5))){
				platformData.setPlatformKind("24");
	   		 	platformData.setPlatformDesc("營業稅查核定補徵稅款");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0062024001");
			} else if ("25".equals(comc.getSubString(oemPayData.reimbInfo, 3,5))){
				platformData.setPlatformKind("25");
	   		 	platformData.setPlatformDesc("綜所稅核定補徵稅款");
	   		 	platformData.setBillType("FISC");
	   		 	platformData.setCusMchtNo("0062025001");
			}
		}
		
		binType = fiscTxData.getBinType();
		
		//
		switch(binType) {
		case "M":
			surchargeAmountDouble = comc.str2double(oemPayData.surchargeAmount);
			break;
		case "J":
			surchargeAmountDouble = comc.str2double(oemPayData.surchargeAmount);
			break;
		case "V":
			// 最後兩位皆為小數，因此需做轉換
			surchargeAmountDouble = commTxBill.getBigDecimalDividedBy100(oemPayData.surchargeAmount).doubleValue();
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
		setValue("FISC_TX_CODE", oemPayData.transactionType);
		setValue("CARD_PLAN", oemPayData.cardPlan);
		setValue("SOURCE_BIN", oemPayData.sourceBINOrICANumber);
		setValue("DEST_BIN", oemPayData.destinationBINOrICANo);
		setValue("ACQ_BUSINESS_ID", oemPayData.acquirerBusinessID);
		setValue("CARD_NO", oemPayData.cardNumber);
		setValueDouble("SOURCE_AMT", sourceAmountDouble.doubleValue());
		setValue("SOURCE_CURR", oemPayData.sourceCurrency);
		setValueDouble("DEST_AMT", destinationAmountDouble.doubleValue());
		setValue("DEST_CURR", oemPayData.destinationCurrency);
		setValueDouble("SETL_AMT", settlementAmountDouble.doubleValue());
		setValue("SETL_CURR", oemPayData.settlementCurrency);
		setValue("SETL_RATE", oemPayData.settlementRate);
		setValue("MCHT_NO", oemPayData.acceptorID);
		setValue("MCC_CODE", oemPayData.mcc);
		String tmpStr = "";
		//特店中文名稱: 若是共同供應契約要取合約商名稱+訂單編號(最多20碼)
		if ("F00600000.ICF01QBD".equals(comc.getSubString(fileName,0,18)) ) {
			tmpStr = String.format("%s%s", oemPayData.procureName,
					comc.fixLeft(oemPayData.reimbInfo, 20));
		} else {
			tmpStr = oemPayData.mchtChiNameOrPlatfromChiName;
		}
		setValue("MCHT_CHI_NAME", tmpStr);
		setValue("MCHT_ENG_NAME", oemPayData.mchtEngName);
		setValue("MCHT_CITY", oemPayData.mchtCity);
		setValue("MCHT_STATE", oemPayData.mchtState);
		setValue("MCHT_ZIP", oemPayData.mchtZip);
		setValue("MCHT_COUNTRY", oemPayData.mchtCountry);
		setValue("SPECIAL_COND_IND", oemPayData.specialCondInd);
		setValue("SUBMERCHANT_ID", oemPayData.subMerchantID);
		setValue("PURCHASE_DATE", purchaseDate);
		setValue("PURCHASE_TIME", oemPayData.purchaseTime);
		setValue("AUTH_CODE", oemPayData.authorizeCode);
		setValue("EC_IND", oemPayData.mailOrTelephoneIndicator);
		setValue("POS_ENVIRONMENT", oemPayData.posEnvironment);
		setValue("CAT_IND", oemPayData.cat);
		setValue("TERMINAL_ID", oemPayData.terminalID);
		setValue("TE_SEND_DATE", teSendDate);
		setValue("TE_BATCH_NO", oemPayData.teBatchNo);
		setValue("TE_TX_NUM", oemPayData.teTxNum);
		setValue("POS_TE_CAP", oemPayData.posTerminalCapability);
		setValue("POS_ENTRY_MODE", oemPayData.posEntryMode);
		setValue("PROCESS_DAY", oemPayData.processDate);
		setValue("FILM_NO", oemPayData.microfilmReferenceNoAcquirer);
		setValue("USAGE_CODE", oemPayData.usageCode);
		setValue("REASON_CODE", oemPayData.chargebackOrRepresentCode);
		setValue("CB_REF_NO", chargebackReferenceNo);
		setValue("VCRFS_IND", oemPayData.vcrfsInd);
		setValue("DOC_IND", oemPayData.documentationIndicator);
		setValue("SERVICE_CODE", oemPayData.serviceCode);
		setValue("UCAF", oemPayData.ucaf);
		setValue("REIMB_INFO", oemPayData.reimbInfo);
		setValue("REIMB_CODE", oemPayData.reimbCode);
		setValue("REIMB_ATTR", oemPayData.reimbursementAttribute);
		setValue("SETTL_FLAG", oemPayData.settlementFlag);
		setValue("ORIG_TX_TYPE", oemPayData.originalTransactionType);
		setValue("RTN_REASON_CODE", oemPayData.returnedItemReasonCode);
		
		setValue("PAYMENT_TYPE", oemPayData.paymentType);
		if (" ".equals(oemPayData.paymentType) && (" ".equals(oemPayData.ebillPaymentType) == false) ) {
			setValue("PAYMENT_TYPE", oemPayData.ebillPaymentType);
		}
		
		//若是被掃交易取得tpan
		if ("t".equals(oemPayData.reimbursementAttribute) ) {
			if ("".equals(fiscTxData.getvCardNo())) {
				setValue("ECS_V_CARD_NO", oemPayData.tpanInfo);
			}
			if (" ".equals(oemPayData.paymentType)) {
				setValue("PAYMENT_TYPE","t");
			}
		}

		setValue("CROSS_PAY_CODE", oemPayData.crossPayCode);
		setValue("CROSS_PAY_STATUS", oemPayData.crossPayStatus);
		setValue("CROSS_PAY_TYPE", oemPayData.crossPayType);
		setValue("NCCC_BILL_TYPE", oemPayData.ncccBillType);
		setValue("NCCC_MCHT_TYPE", oemPayData.ncccMchtType);
		setValue("NCCC_ITEM", oemPayData.ncccItem);
		setValue("MESSAGE_TEXT", oemPayData.messageText);
		setValue("ISSUE_CTRL_NUM", oemPayData.issueCtrlNum);
		setValueDouble("AC_CRYPTO_AMT", comc.str2double(oemPayData.cryptoAmount));
		setValue("TE_TX_CURR", oemPayData.transactionCurrencyCode);
		setValue("AC_TX_TYPE", oemPayData.transactionTypeRecord2);
		setValue("PAN_SEQ_NUM", oemPayData.pANSequenceNumber);
		setValue("TE_TX_DATE", transactionDate);
		setValue("TE_PROFILE", oemPayData.terminalCapabilitiesProfile);
		setValue("TE_COUNTRY", oemPayData.terminalCountryCode);
		setValue("INTERFACE_DEV_NUM", oemPayData.interfaceDeviceSerialNumber);
		setValue("AC_UNPRED_NUM", oemPayData.unpredictableNumber );
		setValue("AP_TX_NUM", oemPayData.applicationTransactionNumber);
		setValue("AP_PROFILE", oemPayData.applicationInterchangeProfile);
		setValue("AC", oemPayData.applicationCryptogram);
		setValue("IAD", oemPayData.issuerApplicationData);
		setValue("TVR", oemPayData.terminalVerificationResult);
		setValue("CVM_RESULT", oemPayData.cardholderVerificationMethodCVMResult);
		setValue("CRYPTOGRAM_INFO", oemPayData.cryptogramInformationData);
		setValue("AP_EXPIRE_DATE", oemPayData.applicationExpirationDate);
		setValue("AUTH_RESP_CODE", oemPayData.authRespCode);
		setValue("POST_ISSUE_RESULT", oemPayData.postIssuanceResult);
		setValue("CHIP_COND_CODE", oemPayData.chipConditionCode);
		setValue("TE_ENTRY_CAP", oemPayData.terminalEntryCapability);
		setValue("CARD_VERFY_RESULT", oemPayData.cardVerificationResult);
		setValue("FFI", oemPayData.formFactorIndicator);
		setValue("PROCURE_UNIFORM", oemPayData.procureUniform);
		setValue("PROCURE_NAME", oemPayData.procureName);
		setValue("PROCURE_VOUCHER_NO", oemPayData.procureVoucherNo);
		setValue("PROCURE_RECEIPT_NO", oemPayData.procureReceiptNo);
		setValue("PROCURE_ORIG_CURR", oemPayData.procureOrigCurr);
		setValueDouble("PROCURE_ORIG_AMT", procureOrigAmtDouble.doubleValue());
		setValue("PROCURE_PLAN", oemPayData.procurePlan);
		setValue("PROCURE_LEVEL_1", oemPayData.procureLevel1);
		setValue("PROCURE_LEVEL_2", oemPayData.procureLevel2);
		setValue("PROCURE_TX_NUM", oemPayData.procureTxNum);
		setValue("PROCURE_BANK_FEE", oemPayData.procureBankFee);
		setValue("PROCURE_CHT_FEE", oemPayData.procureChtFee);
		setValue("PROCURE_PAY_AMT", oemPayData.procurePayAmt);
		setValue("PROCURE_TOT_TERM", oemPayData.procureTotTerm);
		setValue("INSTALL_TYPE", oemPayData.installType);
		setValue("INSTALL_TOT_TERM", oemPayData.installTotTerm);
		setValue("INSTALL_FIRST_AMT", oemPayData.installFirstAmt);
		setValue("INSTALL_PER_AMT", oemPayData.installPerAmt);
		setValue("INSTALL_LAST_AMT", oemPayData.installLastAmt);
		setValue("INSTALL_CHARGES", oemPayData.installCharges);
		setValue("INSTALL_ATM_NO", oemPayData.installAtmNo);
		setValue("INSTALL_PROJ_NO", oemPayData.installProjNo);
		setValue("INSTALL_SUPPLY_NO", oemPayData.installSupplyNo);
		setValue("BONUS_TRANS_BP", oemPayData.bonusTransBp);
		setValueDouble("BONUS_PAY_CASH", bonusPayCashDouble.doubleValue());
		setValue("IRD", oemPayData.interchangeRateDesignator);
		setValue("PREPAID_CARD_IND", oemPayData.prepaidCardIndicator);
		setValue("CWB_IND", oemPayData.cardWarningBulletinIndicator);
		setValue("FRAUD_NOTIFY_DATE", oemPayData.fraudNotificationDate);
		setValue("FRAUD_CB_CNT", oemPayData.fraudNotificationServiceChargebackCounter);
		setValue("FLOOR_LIMIT_IND", oemPayData.floorLimitIndicator);
		setValue("INTERFACE_TRACE_NUM", oemPayData.interfaceTraceNumber);
		setValue("TRANSACTION_ID", oemPayData.transactionID);
		setValue("CARD_PRODUCT_ID", oemPayData.cardProductId);
		setValue("AUTH_VALID_CODE", oemPayData.authValidCode);
		setValue("MUTI_CLEARING_SEQ", oemPayData.multipleClearingSequenceNumber);
		setValue("QPS_CB_IND", oemPayData.qpsOrPayPassChargebackEligibilityIndicator);
		setValue("ADDN_ACCT_TYPE", oemPayData.additionalAmountAccountType);
		setValue("DCC_IND", oemPayData.dccInd);
		setValue("ADDN_AMT_CURR", oemPayData.additionalAmountCurrencyCode);
		setValue("ADDN_AMT_SIGN", oemPayData.surchargeCreditOrDebitIndicator);
		setValueDouble("ADDN_AMT", surchargeAmountDouble);
		setValueDouble("ADDN_BILL_CURR", surchargeAmountInCardholderBillingCurrencyDouble.doubleValue());
		setValue("TOKEN_ASSURE_LEVEL", oemPayData.tokenAssuranceLevel);
		setValue("TOKEN_REQUESTOR_ID", oemPayData.tokenRequestorId);
		setValue("PAYMENT_FA_ID", oemPayData.paymentFacilitatorID);
		setValue("DE22_CARD_IN_CAP", oemPayData.de22CardDataInputCapability);
		setValue("DE22_CH_AUTH_CAP", oemPayData.de22CardholderAuthenticationCapability);
		setValue("DE22_CAPTURE_CAP", oemPayData.de22CardCaptureCapability);
		setValue("DE22_TE_OP_ENV", oemPayData.de22TerminalOperatingEnvironment);
		setValue("DE22_CH_DATA", oemPayData.de22CardholderPresentData);
		setValue("DE22_CARD_DATA", oemPayData.de22CardPresentData);
		setValue("DE22_INPUT_MODE", oemPayData.de22InputMode);
		setValue("DE22_CH_AUTH_METHOD", oemPayData.de22CardholderAuthenticationMethod);
		setValue("DE22_CH_AUTH_ENTITY", oemPayData.de22CardholderAuthenticationEntity);
		setValue("DE22_CARD_OUT_CAP", oemPayData.de22CardDataOutputCapability);
		setValue("DE22_TE_OUT_CAP", oemPayData.de22TerminalDataOutputCapability);
		setValue("DE22_PIN_CAPTURE_CAP", oemPayData.de22PINCaptureCapability);
		setValue("PCAS_IND", oemPayData.positiveCardholderAuthorizationServiceIndicator);
		setValue("VCIND", oemPayData.vcInd);
		setValue("ORIG_TX_AMT", oemPayData.origTxAmt);
		setValue("PAN_TOKEN", oemPayData.panToken);
		setValue("M_CROSS_IND", oemPayData.masterCardCrossBorderIndicator);
		setValue("M_CURR_IND", oemPayData.masterCardCurrencyIndicator);
		setValue("M_CCA", oemPayData.masterCardCurrencyConversionAssessment);
		setValue("ICCR", oemPayData.issuerCurrencyConversionRate);
		setValue("FPI", oemPayData.feeProgramIndicator);
		setValue("SC_TO_BC_RATE", oemPayData.sourceCurrencyToBaseCurrencyExchangeRate);
		setValue("BC_TO_DC_RATE", oemPayData.baseCurrencyToDestinationCurrencyExchangeRate);
		setValue("CHARGE_IND", oemPayData.chargeIndicator);
		setValue("BUS_FORMAT_CODE", oemPayData.businessFormatCode);
		setValue("ACCT_NUM_TYPE", oemPayData.accountNumberType);
		setValue("PAR", oemPayData.paymentAccountReference);
		setValue("VROL_CASE_NUM", oemPayData.vrolCaseNumber);
		setValue("RETRIEVAL_REQ_ID", oemPayData.retrievalRequestId);
		setValue("ACQ_RESP_CODE", oemPayData.acqRespCode);
		setValue("FEE_REASON_CODE", oemPayData.feeReasonCode);
		setValue("EMV_REVERSAL_IND", oemPayData.emvReversalInd);
		setValue("EMV_BANK_NUM", oemPayData.emvBankNum);
		setValue("EVENT_DATE", oemPayData.eventDate);
		setValue("FEE_CTRL_NUM", oemPayData.feeCtrlNum);
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
		
		totalWriteCnt++;
		
	}
		/**
		 * separate each column form the bytesArr 
		 * @param oemPayData
		 * @param bytesArr
		 * @param transactionTypeNo
		 * : int <br> 交易類型 2: 調單交易、3: 費用交易 、1: 一般交易
		 * @param recordNo
		 * : int <br> 第幾筆record
		 * @throws Exception 
		 */
		private void getDataD(OEMPayData oemPayData, byte[] bytesArr,  int transactionTypeNo, int recordNo) throws Exception {
			switch(transactionTypeNo) {
			case 1: //一般交易
				switch(recordNo) {
				case 1: //record1
					oemPayData.transactionType = CommTxBill.subByteToStr(bytesArr, 0,4);  // 財金交易代號
					oemPayData.cardPlan = CommTxBill.subByteToStr(bytesArr, 5,6);  // 卡片類型(V/M/J/E)
					oemPayData.sourceBINOrICANumber = CommTxBill.subByteToStr(bytesArr, 6,17);  // 傳送端BIN/ICA
					oemPayData.destinationBINOrICANo = CommTxBill.subByteToStr(bytesArr, 17,28);  // 接收端BIN/ICA
					oemPayData.acquirerBusinessID = CommTxBill.subByteToStr(bytesArr, 30,38);  // 代理單位代號
					oemPayData.cardNumber = CommTxBill.subByteToStr(bytesArr, 38,57);  // 卡號
					oemPayData.sourceAmount = CommTxBill.subByteToStr(bytesArr, 57,69);  // 購買地金額
					oemPayData.sourceCurrency = CommTxBill.subByteToStr(bytesArr, 69,72);  // 購買地幣別
					oemPayData.destinationAmount = CommTxBill.subByteToStr(bytesArr, 72,84);  // 當地金額
					oemPayData.destinationCurrency = CommTxBill.subByteToStr(bytesArr, 84,87);  // Destination curr
					oemPayData.settlementAmount = CommTxBill.subByteToStr(bytesArr, 87,99);  // 清算金額
					oemPayData.settlementCurrency = CommTxBill.subByteToStr(bytesArr, 99,102);  // 清算幣別
					oemPayData.settlementRate = CommTxBill.subByteToStr(bytesArr, 102,110);  // 清算匯率
					oemPayData.acceptorID = CommTxBill.subByteToStr(bytesArr, 110,130);  // 特店代號
					oemPayData.mcc = CommTxBill.subByteToStr(bytesArr, 130,134);  // 特店行業別
					oemPayData.mchtChiNameOrPlatfromChiName = CommTxBill.subByteToStr(bytesArr, 134,174);  // 特店中文名稱
					oemPayData.mchtChiNameOrPlatfromChiName = trimAll(oemPayData.mchtChiNameOrPlatfromChiName);
					oemPayData.mchtEngName = CommTxBill.subByteToStr(bytesArr, 174,199);  // 特店英文名稱
					oemPayData.mchtCity = CommTxBill.subByteToStr(bytesArr, 199,212);  // 特店city
					oemPayData.mchtState = CommTxBill.subByteToStr(bytesArr, 212,215);  // 特店省份
					oemPayData.mchtZip = CommTxBill.subByteToStr(bytesArr, 215,220);  // 特店ZIP
					oemPayData.mchtCountry = CommTxBill.subByteToStr(bytesArr, 220,223);  // 特店國家代號
					oemPayData.specialCondInd = CommTxBill.subByteToStr(bytesArr, 223,225);  // 特殊條件識別碼
					oemPayData.subMerchantID = CommTxBill.subByteToStr(bytesArr, 225,240);  // 次特約商代碼
					oemPayData.purchaseDate = CommTxBill.subByteToStr(bytesArr, 240,246);  // 購貨日期
					oemPayData.purchaseTime = CommTxBill.subByteToStr(bytesArr, 246,252);  // 購貨時間(HHMMSS)
					oemPayData.authorizeCode = CommTxBill.subByteToStr(bytesArr, 252,258);  // 授權碼
					oemPayData.mailOrTelephoneIndicator = CommTxBill.subByteToStr(bytesArr, 258,259);  // MAIL/PHONE ECI識別碼
					oemPayData.posEnvironment = CommTxBill.subByteToStr(bytesArr, 259,260);  // POS Environment
					oemPayData.cat = CommTxBill.subByteToStr(bytesArr, 260,261);  // 自助端末機識別碼
					oemPayData.terminalID = CommTxBill.subByteToStr(bytesArr, 261,276);  // 端末機代碼
					oemPayData.teSendDate = CommTxBill.subByteToStr(bytesArr, 276,282);  // 端末機傳送資料日期
					oemPayData.teBatchNo = CommTxBill.subByteToStr(bytesArr, 282,286);  // 端末機上傳資料批次號碼
					oemPayData.teTxNum = CommTxBill.subByteToStr(bytesArr, 286,292);  // 端末機交易序號
					oemPayData.posTerminalCapability = CommTxBill.subByteToStr(bytesArr, 292,293);  // POS端末機性能
					oemPayData.posEntryMode = CommTxBill.subByteToStr(bytesArr, 293,295);  // POS輸入型態
					oemPayData.processDate = CommTxBill.subByteToStr(bytesArr, 295,299);  // VISA/Master處理日(西元YDDD)
					oemPayData.microfilmReferenceNoAcquirer = CommTxBill.subByteToStr(bytesArr, 299,322);  // 微縮影編號
					oemPayData.usageCode = CommTxBill.subByteToStr(bytesArr, 322,323);  // 使用碼
					oemPayData.chargebackOrRepresentCode = CommTxBill.subByteToStr(bytesArr, 323,327);  // 沖正駁回/調單理由碼
					oemPayData.chargebackReferenceNo = CommTxBill.subByteToStr(bytesArr, 327,337);  // 沖正參考號碼/跨境支付手續費整數8位小數2位
					oemPayData.vcrfsInd = CommTxBill.subByteToStr(bytesArr, 337,338);  // 特殊沖正/VCRFS識別碼
					oemPayData.documentationIndicator = CommTxBill.subByteToStr(bytesArr, 338,339);  // 附寄文件識別碼
					oemPayData.serviceCode = CommTxBill.subByteToStr(bytesArr, 339,342);  // Service Code
					oemPayData.ucaf = CommTxBill.subByteToStr(bytesArr, 342,343);  // UCAF
					oemPayData.reimbInfo = CommTxBill.subByteToStr(bytesArr, 343,388);  // 跨境/繳費平台/繳稅交易資訊
					oemPayData.reimbCode = CommTxBill.subByteToStr(bytesArr, 388,390);  // 繳費平台/繳稅種類代碼
					oemPayData.reimbursementAttribute = CommTxBill.subByteToStr(bytesArr, 390,391);  // 交易處理費屬性
					oemPayData.settlementFlag = CommTxBill.subByteToStr(bytesArr, 391,392);  // 清算識別碼
					oemPayData.originalTransactionType = CommTxBill.subByteToStr(bytesArr, 392,396);  // 交易退回時原交易代號
					oemPayData.returnedItemReasonCode = CommTxBill.subByteToStr(bytesArr, 396,398);  // 交易退回理由碼
					oemPayData.paymentType = CommTxBill.subByteToStr(bytesArr, 398,399);  // 支付型態
					oemPayData.crossPayCode = CommTxBill.subByteToStr(bytesArr, 399,400);  // 跨境電子支付平台代碼
					oemPayData.crossPayStatus = CommTxBill.subByteToStr(bytesArr, 400,401);  // 跨境電子支付平台交易狀態
					oemPayData.crossPayType = CommTxBill.subByteToStr(bytesArr, 401,411);  // 跨境電子支付申報性質別
					oemPayData.ncccBillType = CommTxBill.subByteToStr(bytesArr, 411,413);  // NCCC ON-US繳費平台帳單類別
					oemPayData.ncccMchtType = CommTxBill.subByteToStr(bytesArr, 413,415);  // NCCC ON-US繳費平台特店類型
					oemPayData.ncccItem = CommTxBill.subByteToStr(bytesArr, 415,420);  // NCCC ON-US繳費平台繳費項目
					oemPayData.tpanInfo = CommTxBill.subByteToStr(bytesArr, 420,436);  // MerchantPAN/信用卡被掃TPAN
					oemPayData.ebillPaymentType = CommTxBill.subByteToStr(bytesArr, 436,437);  // 電子化繳費稅的支付型態
					
					break;
				case 2: //record2
					oemPayData.messageText = CommTxBill.subByteToStr(bytesArr, 6,76);  // 訊息(若FXG:則為原訊息/特名)
					oemPayData.cryptoAmount = CommTxBill.subByteToStr(bytesArr, 76,88);  // 產生AC時的授權金額
					oemPayData.transactionCurrencyCode = CommTxBill.subByteToStr(bytesArr, 88,91);  // 端末機交易幣別碼
					oemPayData.transactionTypeRecord2 = CommTxBill.subByteToStr(bytesArr, 91,93);  // 產生AC時的交易類別
					oemPayData.pANSequenceNumber = CommTxBill.subByteToStr(bytesArr, 93,96);  // 晶片卡卡片序號
					oemPayData.transactionDate = CommTxBill.subByteToStr(bytesArr, 96,102);  // 端末機交易日期(YYYYMMDD)
					oemPayData.terminalCapabilitiesProfile = CommTxBill.subByteToStr(bytesArr, 102,108);  // 端末機功能表
					oemPayData.terminalCountryCode = CommTxBill.subByteToStr(bytesArr, 108,111);  // 端末機國別碼
					oemPayData.interfaceDeviceSerialNumber = CommTxBill.subByteToStr(bytesArr, 111,119);  // 端末機唯一序號
					oemPayData.unpredictableNumber = CommTxBill.subByteToStr(bytesArr, 119,127);  // 產生AC時所需亂數
					oemPayData.applicationTransactionNumber = CommTxBill.subByteToStr(bytesArr, 127,131);  // 卡片交易序號
					oemPayData.applicationInterchangeProfile = CommTxBill.subByteToStr(bytesArr, 131,135);  // 卡片應用程式功能表
					oemPayData.applicationCryptogram = CommTxBill.subByteToStr(bytesArr, 135,151);  // 交易驗證資料(AC)
					oemPayData.issuerApplicationData = CommTxBill.subByteToStr(bytesArr, 151,215);  // 發卡行產生AC的資訊
					oemPayData.terminalVerificationResult = CommTxBill.subByteToStr(bytesArr, 215,225);  // 端末機所有交易行為
					oemPayData.cardholderVerificationMethodCVMResult = CommTxBill.subByteToStr(bytesArr, 225,231);  // 持卡人認證結果
					oemPayData.cryptogramInformationData = CommTxBill.subByteToStr(bytesArr, 231,233);  // 回傳Approve/Online/Denied的type
					oemPayData.applicationExpirationDate = CommTxBill.subByteToStr(bytesArr, 233,241);  // 晶片卡應用程式失效日期(YYYYMMDD)
					oemPayData.authRespCode = CommTxBill.subByteToStr(bytesArr, 241,243);  // 授權回應碼(含離線回應碼)
					oemPayData.postIssuanceResult = CommTxBill.subByteToStr(bytesArr, 243,253);  // POST ISSUANCE結果
					oemPayData.chipConditionCode = CommTxBill.subByteToStr(bytesArr, 253,254);  // Chip condition code
					oemPayData.terminalEntryCapability = CommTxBill.subByteToStr(bytesArr, 254,255);  // 端末機功能(0:Non-EMV,5:EMV)
					oemPayData.cardVerificationResult = CommTxBill.subByteToStr(bytesArr, 255,267);  // Card 紀錄所有交易行為
					oemPayData.formFactorIndicator = CommTxBill.subByteToStr(bytesArr, 267,275);  // 識別交易載具
					break;
				case 3: //record3
					oemPayData.procureUniform = CommTxBill.subByteToStr(bytesArr, 6,14);  // 共同供應契約:立約商統編
					oemPayData.procureName = CommTxBill.subByteToStr(bytesArr, 14,34);  // 共同供應契約:立約商名稱
					oemPayData.procureVoucherNo = CommTxBill.subByteToStr(bytesArr, 34,49);  // 共同供應契約:傳票號碼
					oemPayData.procureReceiptNo = CommTxBill.subByteToStr(bytesArr, 49,59);  // 共同供應契約:發票號碼
					oemPayData.procureOrigCurr = CommTxBill.subByteToStr(bytesArr, 59,62);  // 共同供應契約:原始幣別
					oemPayData.procureOrigAmt = CommTxBill.subByteToStr(bytesArr, 62,78);  // 共同供應契約:原始金額
					oemPayData.procurePlan = CommTxBill.subByteToStr(bytesArr, 78,128);  // 共同供應契約:工作計劃/購案編號
					oemPayData.procureLevel1 = CommTxBill.subByteToStr(bytesArr, 128,158);  // 共同供應契約:一級用途別
					oemPayData.procureLevel2 = CommTxBill.subByteToStr(bytesArr, 158,188);  // 共同供應契約:二級用途別
					oemPayData.procureTxNum = CommTxBill.subByteToStr(bytesArr, 188,208);  // 共同供應契約:交易編號
					oemPayData.procureBankFee = CommTxBill.subByteToStr(bytesArr, 208,218);  // 共同供應契約:銀行手續費
					oemPayData.procureChtFee = CommTxBill.subByteToStr(bytesArr, 218,228);  // 共同供應契約:中華電信手續費
					oemPayData.procurePayAmt = CommTxBill.subByteToStr(bytesArr, 228,238);  // 共同供應契約:撥付金額
					oemPayData.procureTotTerm = CommTxBill.subByteToStr(bytesArr, 238,240);  // 共同供應契約:訂單支付期數
					oemPayData.installType = CommTxBill.subByteToStr(bytesArr, 240,241);  // 分期付款:交易類型
					oemPayData.installTotTerm = CommTxBill.subByteToStr(bytesArr, 241,243);  // 分期付款:分期期數
					oemPayData.installFirstAmt = CommTxBill.subByteToStr(bytesArr, 243,253);  // 分期付款:首期金額
					oemPayData.installPerAmt = CommTxBill.subByteToStr(bytesArr, 253,263);  // 分期付款:每期金額
					oemPayData.installLastAmt = CommTxBill.subByteToStr(bytesArr, 263,273);  // 分期付款:末期金額
					oemPayData.installCharges = CommTxBill.subByteToStr(bytesArr, 273,283);  // 分期付款:分期管理費
					oemPayData.installAtmNo = CommTxBill.subByteToStr(bytesArr, 283,289);  // 分期付款:櫃員機台代碼
					oemPayData.installProjNo = CommTxBill.subByteToStr(bytesArr, 289,295);  // 分期付款:專案代號
					oemPayData.installSupplyNo = CommTxBill.subByteToStr(bytesArr, 295,303);  // 分期付款:來源供應商代碼
					oemPayData.bonusTransBp = CommTxBill.subByteToStr(bytesArr, 303,311);  // 紅利折抵點數
					oemPayData.bonusPayCash = CommTxBill.subByteToStr(bytesArr, 311,323);  // 紅利折抵後之支付金額
					break;
				case 4: //record4
					oemPayData.interchangeRateDesignator = CommTxBill.subByteToStr(bytesArr, 6,8);  // Interchange Rate Designator
					oemPayData.prepaidCardIndicator = CommTxBill.subByteToStr(bytesArr, 8,9);  // 購買預付卡識別碼(Only VISA)
					oemPayData.cardWarningBulletinIndicator = CommTxBill.subByteToStr(bytesArr, 9,10);  // 黑名單識別碼
					oemPayData.fraudNotificationDate = CommTxBill.subByteToStr(bytesArr, 10,14);  // FRAUD NOTIFICATION DATE(YDDD)
					oemPayData.fraudNotificationServiceChargebackCounter = CommTxBill.subByteToStr(bytesArr, 14,16);  // 偽冒卡chargeback次數
					oemPayData.floorLimitIndicator = CommTxBill.subByteToStr(bytesArr, 16,17);  // 特店限額識別碼
					oemPayData.interfaceTraceNumber = CommTxBill.subByteToStr(bytesArr, 17,23);  // 介面追蹤號碼
					oemPayData.transactionID = CommTxBill.subByteToStr(bytesArr, 23,38);  // 交易識別碼
					oemPayData.cardProductId = CommTxBill.subByteToStr(bytesArr, 38,40);  // 卡片級別識別碼
					oemPayData.authValidCode = CommTxBill.subByteToStr(bytesArr, 40,44);  // 授權欄位驗證碼
					oemPayData.multipleClearingSequenceNumber = CommTxBill.subByteToStr(bytesArr, 44,46);  // 多筆清算交易對應序號
					oemPayData.qpsOrPayPassChargebackEligibilityIndicator = CommTxBill.subByteToStr(bytesArr, 46,47);  // QPS/PayPass交易後續是否可沖正
					oemPayData.additionalAmountAccountType = CommTxBill.subByteToStr(bytesArr, 47,49);  // Additional Amount,Account Type
					oemPayData.dccInd = CommTxBill.subByteToStr(bytesArr, 49,51);  // DCC識別碼
					oemPayData.additionalAmountCurrencyCode = CommTxBill.subByteToStr(bytesArr, 51,54);  // Additional Amount 幣別
					oemPayData.surchargeCreditOrDebitIndicator = CommTxBill.subByteToStr(bytesArr, 54,55);  // ADDN/Surcharge正負Sign
					oemPayData.surchargeAmount = CommTxBill.subByteToStr(bytesArr, 55,67);  // ADDN/Surcharge/找回現金(V:整數10位,小數二位;M/J:依幣別決定小數位)
					oemPayData.surchargeAmountInCardholderBillingCurrency = CommTxBill.subByteToStr(bytesArr, 67,75);  // ADDN/Surcharge(持卡人billing幣別)
					oemPayData.tokenAssuranceLevel = CommTxBill.subByteToStr(bytesArr, 75,77);  // Token Assurance Level
					oemPayData.tokenRequestorId = CommTxBill.subByteToStr(bytesArr, 77,88);  // Token Requestor ID
					oemPayData.paymentFacilitatorID = CommTxBill.subByteToStr(bytesArr, 88,99);  // Payment Facilitator ID
					oemPayData.de22CardDataInputCapability = CommTxBill.subByteToStr(bytesArr, 99,100);  // DE22:Card Data Input Capability
					oemPayData.de22CardholderAuthenticationCapability = CommTxBill.subByteToStr(bytesArr, 100,101);  // DE22:持卡人認證Capability
					oemPayData.de22CardCaptureCapability = CommTxBill.subByteToStr(bytesArr, 101,102);  // DE22:Card Capture Capability
					oemPayData.de22TerminalOperatingEnvironment = CommTxBill.subByteToStr(bytesArr, 102,103);  // DE22:端末機OP Environment
					oemPayData.de22CardholderPresentData = CommTxBill.subByteToStr(bytesArr, 103,104);  // DE22:持卡人present data
					oemPayData.de22CardPresentData = CommTxBill.subByteToStr(bytesArr, 104,105);  // DE22:Card present data
					oemPayData.de22InputMode = CommTxBill.subByteToStr(bytesArr, 105,106);  // DE22:Input Mode
					oemPayData.de22CardholderAuthenticationMethod = CommTxBill.subByteToStr(bytesArr, 106,107);  // DE22:持卡人認證方式
					oemPayData.de22CardholderAuthenticationEntity = CommTxBill.subByteToStr(bytesArr, 107,108);  // DE22:持卡人認證Entity
					oemPayData.de22CardDataOutputCapability = CommTxBill.subByteToStr(bytesArr, 108,109);  // DE22:Card Data OUT Capability
					oemPayData.de22TerminalDataOutputCapability = CommTxBill.subByteToStr(bytesArr, 109,110);  // DE22:端末機資料OUT Capability
					oemPayData.de22PINCaptureCapability = CommTxBill.subByteToStr(bytesArr, 110,111);  // DE22:PIN Capture Capability
					oemPayData.positiveCardholderAuthorizationServiceIndicator = CommTxBill.subByteToStr(bytesArr, 111,112);  // PCAS識別碼
					oemPayData.vcInd = CommTxBill.subByteToStr(bytesArr, 112,117);  // VCIND
					oemPayData.origTxAmt = CommTxBill.subByteToStr(bytesArr, 117,129);  // 原始交易金額
					oemPayData.panToken = CommTxBill.subByteToStr(bytesArr, 129,148);  // PAN TOKEN
					oemPayData.masterCardCrossBorderIndicator = CommTxBill.subByteToStr(bytesArr, 148,149);  // M/C Cross-Border Indicator
					oemPayData.masterCardCurrencyIndicator = CommTxBill.subByteToStr(bytesArr, 149,150);  // M/C 幣別 Indicator
					oemPayData.masterCardCurrencyConversionAssessment = CommTxBill.subByteToStr(bytesArr, 150,162);  // M/C CCA
					oemPayData.issuerCurrencyConversionRate = CommTxBill.subByteToStr(bytesArr, 162,170);  // Issuer 幣別轉換匯率
					oemPayData.feeProgramIndicator = CommTxBill.subByteToStr(bytesArr, 170,173);  // Fee Program Indicator
					oemPayData.sourceCurrencyToBaseCurrencyExchangeRate = CommTxBill.subByteToStr(bytesArr, 173,181);  // 原始幣別對Base幣別匯率
					oemPayData.baseCurrencyToDestinationCurrencyExchangeRate = CommTxBill.subByteToStr(bytesArr, 181,189);  // Base幣別對原始幣別匯率
					oemPayData.chargeIndicator = CommTxBill.subByteToStr(bytesArr, 189,190);  // Charge Indicator
					oemPayData.businessFormatCode = CommTxBill.subByteToStr(bytesArr, 190,192);  // Bussiness Format Code
					oemPayData.accountNumberType = CommTxBill.subByteToStr(bytesArr, 192,194);  // Account Number Type
					oemPayData.paymentAccountReference = CommTxBill.subByteToStr(bytesArr, 194,223);  // Payment Account Ref.
					oemPayData.vrolCaseNumber = CommTxBill.subByteToStr(bytesArr, 223,233);  // VROL Case Number
					break;
				default:
					showLogMessage("E", "", "recordNo錯誤");
					throw new Exception("recordNo錯誤");
				}
				break;
			case 2: //調單交易
				switch(recordNo) {
				case 1: //record1
					oemPayData.transactionType = CommTxBill.subByteToStr(bytesArr, 0,4);  // 財金交易代號
					oemPayData.cardPlan = CommTxBill.subByteToStr(bytesArr, 5,6);  // 卡片類型(V/M/J/E)
					oemPayData.sourceBINOrICANumber = CommTxBill.subByteToStr(bytesArr, 6,17);  // 傳送端BIN/ICA
					oemPayData.destinationBINOrICANo = CommTxBill.subByteToStr(bytesArr, 17,28);  // 接收端BIN/ICA
					oemPayData.cardNumber = CommTxBill.subByteToStr(bytesArr, 28,47);  // 卡號
					oemPayData.purchaseDate = CommTxBill.subByteToStr(bytesArr, 47,53);  // 購貨日期
					oemPayData.purchaseTime = CommTxBill.subByteToStr(bytesArr, 53,59);  // 購貨時間(HHMMSS)
					oemPayData.sourceAmount = CommTxBill.subByteToStr(bytesArr, 59,71);  // 購買地金額
					oemPayData.sourceCurrency = CommTxBill.subByteToStr(bytesArr, 71,74);  // 購買地幣別
					oemPayData.destinationAmount = CommTxBill.subByteToStr(bytesArr, 74,86);  // 當地金額
					oemPayData.destinationCurrency = CommTxBill.subByteToStr(bytesArr, 86,89);  // Destination curr
					oemPayData.settlementAmount = CommTxBill.subByteToStr(bytesArr, 89,101);  // 清算金額
					oemPayData.acceptorID = CommTxBill.subByteToStr(bytesArr, 101,121);  // 特店代號
					oemPayData.mcc = CommTxBill.subByteToStr(bytesArr, 121,125);  // 特店行業別
					oemPayData.mchtEngName = CommTxBill.subByteToStr(bytesArr, 125,150);  // 特店英文名稱
					oemPayData.mchtCity = CommTxBill.subByteToStr(bytesArr, 150,163);  // 特店city
					oemPayData.mchtState = CommTxBill.subByteToStr(bytesArr, 163,166);  // 特店省份
					oemPayData.mchtZip = CommTxBill.subByteToStr(bytesArr, 166,171);  // 特店ZIP
					oemPayData.mchtCountry = CommTxBill.subByteToStr(bytesArr, 171,174);  // 特店國家代號
					oemPayData.authorizeCode = CommTxBill.subByteToStr(bytesArr, 174,180);  // 授權碼
					oemPayData.terminalID = CommTxBill.subByteToStr(bytesArr, 180,195);  // 端末機代碼
					oemPayData.processDate = CommTxBill.subByteToStr(bytesArr, 195,199);  // VISA/Master處理日(西元YDDD)
					oemPayData.microfilmReferenceNoAcquirer = CommTxBill.subByteToStr(bytesArr, 199,222);  // 微縮影編號
					oemPayData.usageCode = CommTxBill.subByteToStr(bytesArr, 222,223);  // 使用碼
					oemPayData.chargebackOrRepresentCode = CommTxBill.subByteToStr(bytesArr, 223,227);  // 沖正駁回/調單理由碼
					oemPayData.messageText = CommTxBill.subByteToStr(bytesArr, 227,297);  // 訊息(若FXG:則為原訊息/特名)
					oemPayData.reimbursementAttribute = CommTxBill.subByteToStr(bytesArr, 360,361);  // 交易處理費屬性
					oemPayData.issueCtrlNum = CommTxBill.subByteToStr(bytesArr, 297,307);  // 發卡單位控制碼
					oemPayData.settlementFlag = CommTxBill.subByteToStr(bytesArr, 307,308);  // 清算識別碼
					oemPayData.multipleClearingSequenceNumber = CommTxBill.subByteToStr(bytesArr, 308,310);  // 多筆清算交易對應序號
					oemPayData.retrievalRequestId = CommTxBill.subByteToStr(bytesArr, 310,322);  // Retrieval Request ID
					oemPayData.tokenAssuranceLevel = CommTxBill.subByteToStr(bytesArr, 347,349);  // Token Assurance Level
					oemPayData.tokenRequestorId = CommTxBill.subByteToStr(bytesArr, 349,360);  // Token Requestor ID
					oemPayData.documentationIndicator = CommTxBill.subByteToStr(bytesArr, 322,323);  // 附寄文件識別碼
					oemPayData.acqRespCode = CommTxBill.subByteToStr(bytesArr, 323,324);  // 代理單位回覆碼
					oemPayData.panToken = CommTxBill.subByteToStr(bytesArr, 324,343);  // PAN TOKEN
					oemPayData.businessFormatCode = CommTxBill.subByteToStr(bytesArr, 343,345);  // Bussiness Format Code
					oemPayData.accountNumberType = CommTxBill.subByteToStr(bytesArr, 345,347);  // Account Number Type
					break;
				default:
					showLogMessage("E", "", "recordNo錯誤");
					throw new Exception("recordNo錯誤");
				}
				break;
			case 3: //費用交易
				switch(recordNo) {
				case 1: //reccord1
					oemPayData.transactionType = CommTxBill.subByteToStr(bytesArr, 0,4);  // 財金交易代號
					oemPayData.cardPlan = CommTxBill.subByteToStr(bytesArr, 5,6);  // 卡片類型(V/M/J/E)
					oemPayData.sourceBINOrICANumber = CommTxBill.subByteToStr(bytesArr, 6,17);  // 傳送端BIN/ICA
					oemPayData.destinationBINOrICANo = CommTxBill.subByteToStr(bytesArr, 17,28);  // 接收端BIN/ICA
					oemPayData.cardNumber = CommTxBill.subByteToStr(bytesArr, 28,47);  // 卡號
					oemPayData.eventDate = CommTxBill.subByteToStr(bytesArr, 47,53);  // 費用發生日
					oemPayData.sourceAmount = CommTxBill.subByteToStr(bytesArr, 53,65);  // 購買地金額
					oemPayData.sourceCurrency = CommTxBill.subByteToStr(bytesArr, 65,68);  // 購買地幣別
					oemPayData.destinationAmount = CommTxBill.subByteToStr(bytesArr, 68,80);  // 當地金額
					oemPayData.destinationCurrency = CommTxBill.subByteToStr(bytesArr, 80,83);  // Destination curr
					oemPayData.feeReasonCode = CommTxBill.subByteToStr(bytesArr, 83,87);  // 費用/訊息原因碼
					oemPayData.feeCtrlNum = CommTxBill.subByteToStr(bytesArr, 87,107);  // 費用控制碼
					oemPayData.processDate = CommTxBill.subByteToStr(bytesArr, 107,111);  // VISA/Master處理日(西元YDDD)
					oemPayData.usageCode = CommTxBill.subByteToStr(bytesArr, 111,112);  // 使用碼
					oemPayData.messageText = CommTxBill.subByteToStr(bytesArr, 112,182);  // 訊息(若FXG:則為原訊息/特名)
					oemPayData.settlementFlag = CommTxBill.subByteToStr(bytesArr, 183,184);  // 清算識別碼
					oemPayData.settlementAmount = CommTxBill.subByteToStr(bytesArr, 184,196);  // 清算金額
					oemPayData.settlementCurrency = CommTxBill.subByteToStr(bytesArr, 196,199);  // 清算幣別
					oemPayData.mchtCountry = CommTxBill.subByteToStr(bytesArr, 199,202);  // 特店國家代號
					oemPayData.reimbursementAttribute = CommTxBill.subByteToStr(bytesArr, 202,203);  // 交易處理費屬性
					oemPayData.microfilmReferenceNoAcquirer = CommTxBill.subByteToStr(bytesArr, 203,226);  // 微縮影編號
					oemPayData.chargebackOrRepresentCode = CommTxBill.subByteToStr(bytesArr, 226,230);  // 沖正駁回/調單理由碼
					oemPayData.emvReversalInd = CommTxBill.subByteToStr(bytesArr, 230,231);  // 更正識別碼
					oemPayData.transactionID = CommTxBill.subByteToStr(bytesArr, 231,246);  // 交易識別碼
					if ("M".equals(oemPayData.cardPlan)) {
						oemPayData.documentationIndicator = CommTxBill.subByteToStr(bytesArr, 246,247);  // 附寄文件識別碼
					} else {
						oemPayData.documentationIndicator = CommTxBill.subByteToStr(bytesArr, 182,183);  // 附寄文件識別碼
					}
					break;
				default:
					showLogMessage("E", "", "recordNo錯誤");
					throw new Exception("recordNo錯誤");
				}
				break;
			default:
				showLogMessage("E", "", "transactionTypeNo錯誤");
				throw new Exception("transactionTypeNo錯誤");
			}
			
		}
		
	private boolean chkFromPtrBintable(String binNo) throws Exception {
		selectSQL = " bin_type ";
		daoTable = " ptr_bintable ";
		whereStr = " where 1=1 " + " and bin_no = ? ";

		setString(1, binNo);

		if (selectTable() <= 0) {
			return false;
		}

		return true;
	}
	
	private String trimAll(String s){
	        String result = "";
	        if(null!=s && !"".equals(s)){
	            result = s.replaceAll("^[*|　*| *| *|//s*]*", "").replaceAll("[*|　*| *| *|//s*]*$", "");
	        }
	        return result;
	}

	public static void main(String[] args) {
		BilE143 proc = new BilE143();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}


}

class OEMPayData {
     String transactionType = "";
	 String cardPlan = "";
	 String sourceBINOrICANumber = "";
	 String destinationBINOrICANo = "";
	 String acquirerBusinessID = "";
	 String cardNumber = "";
	 String sourceAmount = "";
	 String sourceCurrency = "";
	 String destinationAmount = "";
	 String destinationCurrency = "";
	 String settlementAmount = "";
	 String settlementCurrency = "";
	 String settlementRate = "";
	 String acceptorID = "";
	 String mcc = "";
	 String mchtChiNameOrPlatfromChiName = "";
	 String mchtEngName = "";
	 String mchtCity = "";
	 String mchtState = "";
	 String mchtZip = "";
	 String mchtCountry = "";
	 String specialCondInd = "";
	 String subMerchantID = "";
	 String purchaseDate = "";
	 String purchaseTime = "";
	 String authorizeCode = "";
	 String mailOrTelephoneIndicator = "";
	 String posEnvironment = "";
	 String cat = "";
	 String terminalID = "";
	 String teSendDate = "";
	 String teBatchNo = "";
	 String teTxNum = "";
	 String posTerminalCapability = "";
	 String posEntryMode = "";
	 String processDate = "";
	 String microfilmReferenceNoAcquirer = "";
	 String usageCode = "";
	 String chargebackOrRepresentCode = "";
	 String chargebackReferenceNo = "";
	 String vcrfsInd = "";
	 String documentationIndicator = "";
	 String serviceCode = "";
	 String ucaf = "";
	 String reimbInfo = "";
	 String reimbCode = "";
	 String reimbursementAttribute = "";
	 String settlementFlag = "";
	 String originalTransactionType = "";
	 String returnedItemReasonCode = "";
	 String paymentType = "";
	 String crossPayCode = "";
	 String crossPayStatus = "";
	 String crossPayType = "";
	 String ncccBillType = "";
	 String ncccMchtType = "";
	 String ncccItem = "";
	 String tpanInfo = "";
	 String ebillPaymentType = "";
	 String messageText = "";
	 String issueCtrlNum = "";
	 String cryptoAmount = "";
	 String transactionCurrencyCode = "";
	 String transactionTypeRecord2 = "";
	 String pANSequenceNumber = "";
	 String transactionDate = "";
	 String terminalCapabilitiesProfile = "";
	 String terminalCountryCode = "";
	 String interfaceDeviceSerialNumber = "";
	 String unpredictableNumber  = "";
	 String applicationTransactionNumber = "";
	 String applicationInterchangeProfile = "";
	 String applicationCryptogram = "";
	 String issuerApplicationData = "";
	 String terminalVerificationResult = "";
	 String cardholderVerificationMethodCVMResult = "";
	 String cryptogramInformationData = "";
	 String applicationExpirationDate = "";
	 String authRespCode = "";
	 String postIssuanceResult = "";
	 String chipConditionCode = "";
	 String terminalEntryCapability = "";
	 String cardVerificationResult = "";
	 String formFactorIndicator = "";
	 String procureUniform = "";
	 String procureName = "";
	 String procureVoucherNo = "";
	 String procureReceiptNo = "";
	 String procureOrigCurr = "";
	 String procureOrigAmt = "";
	 String procurePlan = "";
	 String procureLevel1 = "";
	 String procureLevel2 = "";
	 String procureTxNum = "";
	 String procureBankFee = "";
	 String procureChtFee = "";
	 String procurePayAmt = "";
	 String procureTotTerm = "";
	 String installType = "";
	 String installTotTerm = "";
	 String installFirstAmt = "";
	 String installPerAmt = "";
	 String installLastAmt = "";
	 String installCharges = "";
	 String installAtmNo = "";
	 String installProjNo = "";
	 String installSupplyNo = "";
	 String bonusTransBp = "";
	 String bonusPayCash = "";
	 String interchangeRateDesignator = "";
	 String prepaidCardIndicator = "";
	 String cardWarningBulletinIndicator = "";
	 String fraudNotificationDate = "";
	 String fraudNotificationServiceChargebackCounter = "";
	 String floorLimitIndicator = "";
	 String interfaceTraceNumber = "";
	 String transactionID = "";
	 String cardProductId = "";
	 String authValidCode = "";
	 String multipleClearingSequenceNumber = "";
	 String qpsOrPayPassChargebackEligibilityIndicator = "";
	 String additionalAmountAccountType = "";
	 String dccInd = "";
	 String additionalAmountCurrencyCode = "";
	 String surchargeCreditOrDebitIndicator = "";
	 String surchargeAmount = "";
	 String surchargeAmountInCardholderBillingCurrency = "";
	 String tokenAssuranceLevel = "";
	 String tokenRequestorId = "";
	 String paymentFacilitatorID = "";
	 String de22CardDataInputCapability = "";
	 String de22CardholderAuthenticationCapability = "";
	 String de22CardCaptureCapability = "";
	 String de22TerminalOperatingEnvironment = "";
	 String de22CardholderPresentData = "";
	 String de22CardPresentData = "";
	 String de22InputMode = "";
	 String de22CardholderAuthenticationMethod = "";
	 String de22CardholderAuthenticationEntity = "";
	 String de22CardDataOutputCapability = "";
	 String de22TerminalDataOutputCapability = "";
	 String de22PINCaptureCapability = "";
	 String positiveCardholderAuthorizationServiceIndicator = "";
	 String vcInd = "";
	 String origTxAmt = "";
	 String panToken = "";
	 String masterCardCrossBorderIndicator = "";
	 String masterCardCurrencyIndicator = "";
	 String masterCardCurrencyConversionAssessment = "";
	 String issuerCurrencyConversionRate = "";
	 String feeProgramIndicator = "";
	 String sourceCurrencyToBaseCurrencyExchangeRate = "";
	 String baseCurrencyToDestinationCurrencyExchangeRate = "";
	 String chargeIndicator = "";
	 String businessFormatCode = "";
	 String accountNumberType = "";
	 String paymentAccountReference = "";
	 String vrolCaseNumber = "";
	 String retrievalRequestId = "";
	 String feeReasonCode = "";
	 String emvReversalInd = "";
	 String emvBankNum = "";
	 String eventDate = "";
	 String feeCtrlNum = "";
	 String acqRespCode = "";
	
}

