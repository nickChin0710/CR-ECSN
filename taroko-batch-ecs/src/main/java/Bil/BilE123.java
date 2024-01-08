/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/16  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/23  V1.00.04    shiyuqi     coding standard, rename field method & format                   *  
*  109/09/04  V1.00.06    Zuwei     code scan issue    
*  109/09/14  V1.00.06    Zuwei     code scan issue    
*  109-10-19  V1.00.07    shiyuqi       updated for project coding standard     *
*  111-09-22  V1.00.08    JeffKung     加轉特店分期及紅利折抵資料                                           *
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


public class BilE123 extends AccessDAO {
    private final String progname = "FISC-ICFXJQB磁條卡請款資料入bil_fiscdtl處理 111/09/22 V1.00.08";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		 HashMap<String,String> dataDMap = null;
		 String text1 = "";
		 String fileNameD="", filePathFromDB="", fileDate="", bankNo="";
		 String filePath = "";
		 byte[] bytesArr1 = null;
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
				// comcr.hCallBatchSeqno
		        fileDate = args[0];
		        filePathFromDB = args[1];
				fileNameD = args[2];
				comcr.hCallBatchSeqno = args[3];
         
		        
				// select the bank number
				bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
				
				// check whether file name's 2nd to 10th characters equals to the bank number
				if( ! fileNameD.substring(1, 9).equalsIgnoreCase(bankNo) ) {
					showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
					return -1;
				}
				
				//open  F00600000.ICFXJQB.XXXXX file ==================
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
				 
				 byte[] bytes = new byte[360];
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
						
						// ======================================
						
						// check whether the characters from 6st to 13th equals to the bank number
						if( ! CommTxBill.subByteToStr(bytes, 5,13).equalsIgnoreCase(bankNo)  ) {
							showLogMessage("E", "", "參加單位代號錯誤");
							return -1;
						}
						
						//轉中文碼
						ConvertCNS_Big5 cns2big5  = new ConvertCNS_Big5();
			            byte[] tempbyte = new byte[40];
			            System.arraycopy(bytes, 254, tempbyte, 0, 40);
			            cns2big5.convert(null, tempbyte, tempbyte.length, false);
			            byte[] returnBytes = comc.fixLeft(new String(cns2big5.getResult(), "big5"), 40).getBytes("big5");
			            System.arraycopy(returnBytes, 0, bytes, 254, 40);
			            
						// ===========================================
						
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
		String purchaseDate = "", localDateReceived = "", fxjExDate = "";
		String binType = "";
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
		
		//============= turn MMddyyyy into yyyyMMdd===================
		
		fxjExDate = CommTxBill.convertStrDateFormat(dataDMap.get("fxjExDate"), "MMddyyyy", "yyyyMMdd");
		
		//====================================================
		
		FiscTxData fiscTxData = commTxBill.getFiscTxData(dataDMap.get("transactionType"), dataDMap.get("usageCode"), 
				dataDMap.get("cardNumber"), dataDMap.get("reimbursementAttribute"), dataDMap.get("acceptorID"), 
				dataDMap.get("merchantCategoryCode"), "", dataDMap.get("settlementFlag"));
		
		PlatformData platformData = fiscTxData.getPlatformData();
		
		binType = fiscTxData.getBinType();
		
		// ====================================================
		
		daoTable = "bil_fiscdtl";
		
		// ====================================================
		setValue("ECS_REFERENCE_NO",commTxBill.getReferenceNo() );
		setValue("ECS_FCTL_NO", fctlNo);  // the value is selected by checkFiscCtlProcCode()
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
		setValueDouble("SOURCE_AMT",sourceAmountDouble.doubleValue());
		//setValue("SOURCE_AMT", dataDMap.get("sourceAmount"));
		setValue("SOURCE_CURR", dataDMap.get("sourceCurrency"));
		setValue("INSTALL_TOT_TERM", dataDMap.get("installTotTerm"));
		setValue("MCHT_NO", dataDMap.get("acceptorID"));
		setValue("MCC_CODE", dataDMap.get("merchantCategoryCode"));
		setValue("MCHT_ENG_NAME", dataDMap.get("merchantName"));
		setValue("MCHT_CITY", dataDMap.get("merchantCityOrLocation"));
		setValue("MCHT_STATE", dataDMap.get("merchantState"));
		setValue("MCHT_COUNTRY", dataDMap.get("merchantCountryCode"));
		setValue("FXJ_TE_SOURCE", dataDMap.get("fxjTeSource"));
		setValue("EMV_ACQ_TE_ID", dataDMap.get("emvAcqTeId"));
		setValue("VCRFS_IND", dataDMap.get("specialChargebackIndicatorOrVCRFSOrMASTERCOMIndicator"));
		setValue("FILM_NO", dataDMap.get("microfilmReferenceNoAcquirersReferenceNo"));
		setValue("AUTH_CODE", dataDMap.get("authorisationResponseID"));
		setValue("PURCHASE_DATE", purchaseDate);
		setValueDouble("DEST_AMT", destinationAmountDouble.doubleValue());
		setValue("DEST_CURR", dataDMap.get("destinationCurrency"));
		setValue("EMV_JCB_EX_RATE", dataDMap.get("emvJCBExRate"));
		setValue("FXJ_EX_DATE", fxjExDate);
		setValue("USAGE_CODE", dataDMap.get("usageCode"));
		setValue("CB_REF_NO", dataDMap.get("chargebackReferenceNo"));
		setValue("EMV_JCB_CB_DATE", dataDMap.get("emvJCBCbCode"));
		setValue("EMV_JCB_REASON_CODE", dataDMap.get("emvJCBReasonCode"));
		setValue("MESSAGE_TEXT", dataDMap.get("messageTextOrMerchantChineseName"));
		setValue("MCHT_CHI_NAME", merchantChineseName);
		setValue("ISSUE_CTRL_NUM", dataDMap.get("issueCtrlNum"));
		setValue("SETTL_FLAG", dataDMap.get("settlementFlag"));
		setValue("POS_TE_CAP", dataDMap.get("POSTerminalCapability"));
		setValue("REIMB_ATTR", dataDMap.get("reimbursementAttribute"));
		setValueDouble("SETL_AMT", usdSettlementAmountDouble.doubleValue());
		setValue("EC_IND", dataDMap.get("mailTelephoneECIIndicator"));
		setValue("PAYMENT_TYPE", dataDMap.get("payType"));
		setValue("DOC_IND", dataDMap.get("documentationIndicator"));
		setValue("SERVICE_CODE", dataDMap.get("serviceCode"));
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
		 * separate each column form the bytesArr 
		 * @throws UnsupportedEncodingException *
		 **/
		private HashMap<String,String> getDataD(byte[] bytesArr1) throws UnsupportedEncodingException {
			
			HashMap<String,String> hashMap = new HashMap<String,String>();
			
			// ======================================================
			hashMap.put("transactionType", CommTxBill.subByteToStr(bytesArr1,0,4));  // 財金交易代號
			hashMap.put("reversalIndicator", CommTxBill.subByteToStr(bytesArr1,4,5));  // 更正識別碼
			hashMap.put("bankNumber", CommTxBill.subByteToStr(bytesArr1,5,13));  // 參加單位代號
			hashMap.put("sourceBINOrICANumber", CommTxBill.subByteToStr(bytesArr1,13,19));  // 傳送端BIN/ICA
			hashMap.put("destinationBINOrICANo", CommTxBill.subByteToStr(bytesArr1,19,25));  // 接收端BIN/ICA
			hashMap.put("acquirerBusinessID", CommTxBill.subByteToStr(bytesArr1,25,33));  // 代理單位代號
			hashMap.put("cardNumber", CommTxBill.subByteToStr(bytesArr1,33,49));  // 卡號
			hashMap.put("localDateReceived", CommTxBill.subByteToStr(bytesArr1,49,55));  // 當地接收日期(西元YYYYMMDD)
			hashMap.put("purchaseTime", CommTxBill.subByteToStr(bytesArr1,55,61));  // 購貨時間(HHMMSS)
			hashMap.put("inputSequenceNumber", CommTxBill.subByteToStr(bytesArr1,61,67));  // 輸入序號(財金編流水號)
			hashMap.put("sourceAmount", CommTxBill.subByteToStr(bytesArr1,67,79));  // 購買地金額
			hashMap.put("sourceCurrency", CommTxBill.subByteToStr(bytesArr1,79,82));  // 購買地幣別
			hashMap.put("installTotTerm", CommTxBill.subByteToStr(bytesArr1,82,84));  // 分期付款期數
			hashMap.put("acceptorID", CommTxBill.subByteToStr(bytesArr1,84,100));  // 特店代號
			hashMap.put("merchantCategoryCode", CommTxBill.subByteToStr(bytesArr1,100,104));  // 特店行業別
			hashMap.put("merchantName", CommTxBill.subByteToStr(bytesArr1,104,129));  // 特店英文名稱
			hashMap.put("merchantCityOrLocation", CommTxBill.subByteToStr(bytesArr1,129,142));  // 特店city
			hashMap.put("merchantState", CommTxBill.subByteToStr(bytesArr1,142,145));  // 特店省份
			hashMap.put("merchantCountryCode", CommTxBill.subByteToStr(bytesArr1,145,148));  // 特店國家代號
			hashMap.put("fxjTeSource", CommTxBill.subByteToStr(bytesArr1,148,150));  // 端末來源代碼
			hashMap.put("emvAcqTeId", CommTxBill.subByteToStr(bytesArr1,150,165));  // 端末機代號
			hashMap.put("specialChargebackIndicatorOrVCRFSOrMASTERCOMIndicator", CommTxBill.subByteToStr(bytesArr1,165,166));  // 特殊沖正/VCRFS識別碼
			hashMap.put("microfilmReferenceNoAcquirersReferenceNo", CommTxBill.subByteToStr(bytesArr1,166,189));  // 微縮影編號
			hashMap.put("authorisationResponseID", CommTxBill.subByteToStr(bytesArr1,189,195));  // 授權碼
			hashMap.put("purchaseDate", CommTxBill.subByteToStr(bytesArr1,195,203));  // 購貨日期(西元YYYYMMDD)
			hashMap.put("destinationAmount", CommTxBill.subByteToStr(bytesArr1,203,215));  // 當地金額
			hashMap.put("destinationCurrency", CommTxBill.subByteToStr(bytesArr1,215,218));  // Destination curr
			hashMap.put("emvJCBExRate", CommTxBill.subByteToStr(bytesArr1,218,226));  // JCB_交換匯率
			hashMap.put("fxjExDate", CommTxBill.subByteToStr(bytesArr1,226,234));  // JCB_交換日期(YYYYMMDD)
			hashMap.put("usageCode", CommTxBill.subByteToStr(bytesArr1,234,235));  // 使用碼
			hashMap.put("chargebackReferenceNo", CommTxBill.subByteToStr(bytesArr1,235,243));  // 沖正參考號碼/跨境支付手續費整數8位小數2位
			hashMap.put("emvJCBCbCode", CommTxBill.subByteToStr(bytesArr1,243,251));  // JCB_更正/沖正/駁回日期
			hashMap.put("emvJCBReasonCode", CommTxBill.subByteToStr(bytesArr1,251,254));  // JCB_沖正/駁回理由碼
			hashMap.put("messageTextOrMerchantChineseName", CommTxBill.subByteToStr(bytesArr1,254,324));  // 訊息(若FXG:則為原訊息/特名)
			hashMap.put("issueCtrlNum", CommTxBill.subByteToStr(bytesArr1,324,333));  // 發卡單位控制碼
			hashMap.put("settlementFlag", CommTxBill.subByteToStr(bytesArr1,333,334));  // 清算識別碼
			hashMap.put("POSTerminalCapability", CommTxBill.subByteToStr(bytesArr1,334,335));  // POS端末機性能
			hashMap.put("reimbursementAttribute", CommTxBill.subByteToStr(bytesArr1,335,336));  // 交易處理費屬性
			hashMap.put("USDSettlementAmount", CommTxBill.subByteToStr(bytesArr1,336,348));  // 美元清算金額
			hashMap.put("mailTelephoneECIIndicator", CommTxBill.subByteToStr(bytesArr1,348,349));  // MAIL/PHONE ECI識別碼
			hashMap.put("payType", CommTxBill.subByteToStr(bytesArr1,349,350));  // 支付型態
			hashMap.put("documentationIndicator", CommTxBill.subByteToStr(bytesArr1,350,351));  // 附寄文件識別碼
			hashMap.put("serviceCode", CommTxBill.subByteToStr(bytesArr1,351,354));  // Service Code
			hashMap.put("reserveData", CommTxBill.subByteToStr(bytesArr1,354,356));  // 保留欄位
			hashMap.put("errorReplyNo", CommTxBill.subByteToStr(bytesArr1,356,360));  // 錯誤回覆碼
			
			
			/*分期付款資料  */
			if ("I".equals(hashMap.get("payType")) ||  "E".equals(hashMap.get("payType"))) {
				hashMap.put("installTotTerm", CommTxBill.subByteToStr(bytesArr1,299,301));  // 分期付款期數
				hashMap.put("installFirstAmt", CommTxBill.subByteToStr(bytesArr1,301,309));  // 分期付款首期金額
				hashMap.put("installPerAmt", CommTxBill.subByteToStr(bytesArr1,309,317));  // 分期付款每期金額
				hashMap.put("installCharges", CommTxBill.subByteToStr(bytesArr1,317,323));  // 分期付款手續費
			} else {
				hashMap.put("installTotTerm", "00");  // 分期付款期數
				hashMap.put("installFirstAmt", "00000000");  // 分期付款首期金額
				hashMap.put("installPerAmt", "00000000");  // 分期付款每期金額
				hashMap.put("installCharges", "000000");  // 分期付款手續費
			}
			
			/*紅利折抵資料  */
			if ("1".equals(hashMap.get("payType")) ||  "2".equals(hashMap.get("payType")) ||  "3".equals(hashMap.get("payType")) ) {
				hashMap.put("bonusTransBp", CommTxBill.subByteToStr(bytesArr1,299,307));  // 紅利折抵點數 
				hashMap.put("bonusPayCash", CommTxBill.subByteToStr(bytesArr1,307,319));  // 紅利折抵後之支付金額 
			} else {
				hashMap.put("bonusTransBp", "00000000");  // 紅利折抵點數 
				hashMap.put("bonusPayCash", "000000000000");  // 紅利折抵後之支付金額 
			}
			
			/*QR payment  (merchantPan & Tpan先不轉)*/

			
			
			
			// ======================================================

			return hashMap;

		}

	public static void main(String[] args) {
		BilE123 proc = new BilE123();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}
	

}
