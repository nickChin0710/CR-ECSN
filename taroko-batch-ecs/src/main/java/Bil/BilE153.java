/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/31  V1.00.00    JustinWu     program initial                       *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109-07-03  V1.00.04    shiyuqi      updated for project coding standard   *	
*  109/07/23  V1.00.05    shiyuqi      coding standard, rename field method & format    																			
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard   *
*  109/11/17  V1.00.08    JeffKung     日期格式錯誤修正, 檔案內容為民國年月日,誤認為西元年月日   
*  111/09/22  V1.00.09    JeffKung     getONUSTxData參數修改, getSubString修改  *
*  112/09/25  V1.00.10    JeffKung     paymentType="t"的處理
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

public class BilE153 extends AccessDAO {
    private  final String progname = "FISC-ICPTXQQ ONUS請款資料入bil_fiscdtl處理 112/09/25 V1.00.10";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		 HashMap<String,String> dataDMap = null;
		 String text = "";
		 String fileNameD="", filePathFromDB="", fileDate="", bankNo="";
		 String filePath = "";
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
	            commTxBill = new CommTxBill(getDBconnect(), getDBalias());
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
				
				// check whether fileNameD's 2nd to 10th characters equals to the bank number
				if( ! comc.getSubString(fileNameD,1, 9).equalsIgnoreCase(bankNo) ) {
					showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
					return -1;
				}
				
				//open  F00600000.ICFXGQBD.XXXXX file ==================
				filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
				fileNameD = SecurityUtil.verifyPath(fileNameD);
				filePath = Paths.get(filePathFromDB, fileNameD).toString();
				if (openBinaryInput(filePath) == false) {
					showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
		            return -1;
		        }

				// check whether fctl_no  exist in the bil_fiscctl and select fctl_no
				String fctlNo = commTxBill.getFctlNoFromBilFiscctl(fileNameD);
				 if(  commTxBill.isEmpty(fctlNo)) {
					 showLogMessage("E", ""
		            			, String.format("此批資料未經過檢核={%s:%s}", fileNameD,businessDate) );
					 return -1;
				 }else {
					 commTxBill.printDebugString("檢核成功");
					 commTxBill.deleteBilFiscdtl(fctlNo); // delete previous inserting data
				 }
				
				 byte[] bytes = new byte[300]; 
				 int readCnt = 0;
				 int corSponseCnt = 0;

				 while(true) {
					 readCnt = readBinFile(bytes);
					 if(readCnt < 5)
							break;
					 
					 if(bytes.length != 0) {		
						
						// 若為美元匯率交易，則排除此美元匯率資訊
						if( commTxBill.isCurrRateTx(CommTxBill.subByteToStr(bytes, 32, 36)) ) {
							continue;
						}
						
						// 排除政府採購卡的請款: 端末機代號(001-016)= 9697999760110001
						if( CommTxBill.subByteToStr(bytes, 0, 16).equals("9697999760110001") ) {
							corSponseCnt++;
							continue;
						}
						
						//轉中文碼
						ConvertCNS_Big5 cns2big5  = new ConvertCNS_Big5();
			            byte[] tempbyte = new byte[42];
			            System.arraycopy(bytes, 130, tempbyte, 0, 42);
			            cns2big5.convert(null, tempbyte, tempbyte.length, false);
			            byte[] returnBytes = comc.fixLeft(new String(cns2big5.getResult(), "big5"), 42).getBytes("big5");
			            System.arraycopy(returnBytes, 0, bytes, 130, 42);
			            						
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
		String binType = "";
		String teSendDate = "";
		
		//==================================================
		// 以下字串最後兩位皆為小數，因此需做轉換
		BigDecimal sourceAndDestAmtDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("sourceAndDestAmt"));
		BigDecimal bonusPayCashDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("bonusPayCash"));
		//============= separate MMddyyhhmmss into purchaseDate and purchaseTime===================
		
		String purchaseDate = comc.getSubString(dataDMap.get("purchaseDateAndTime"),0,6);
		String purchaseTime = comc.getSubString(dataDMap.get("purchaseDateAndTime"),6);
		
		//============= turn yyMMdd into yyyyMMdd===================
		//20201117 : 檔案格式內容為民國RRMMDD, 程式配合修改
		// RR為民國年末兩碼 	yyyy = RR+100+1911  (允許空白或日期格式)
		long tmpLong = comcr.str2long(purchaseDate.trim());
		purchaseDate = String.format("%8d", tmpLong +20110000);
		
		tmpLong = comcr.str2long(dataDMap.get("teSendDate").trim());
		teSendDate = String.format("%8d", tmpLong +20110000);
				
		//==================================================
		
		FiscTxData fiscTxData = 
				commTxBill.getONUSTxData(dataDMap.get("transactionType"), dataDMap.get("cardNo"), dataDMap.get("reimbAttr"), 
						dataDMap.get("mchtNo"), "", dataDMap.get("settlFlag"));
		
		PlatformData platformData = fiscTxData.getPlatformData();
		
		binType = fiscTxData.getBinType();
		
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
		setValue("ECS_CB_CODE",  "");
		setValue("ECS_PLATFORM_KIND", platformData.getPlatformKind());
		setValue("ECS_CUS_MCHT_NO", platformData.getCusMchtNo());
		setValue("ECS_BILL_TYPE", fiscTxData.getBillType());
		setValue("ECS_ACCT_CODE", fiscTxData.getAcctCode());
		setValue("ECS_SIGN_CODE", fiscTxData.getSignCode());
		setValue("ECS_REAL_CARD_NO", fiscTxData.getRealCardNo());
		setValue("ECS_V_CARD_NO", fiscTxData.getvCardNo());
		
		//若是被掃交易取得tpan
		if ("t".equals(dataDMap.get("paymentType")) ) {
			if ("".equals(fiscTxData.getvCardNo())) {
				setValue("ECS_V_CARD_NO", dataDMap.get("tpan"));
			}
			setValue("ECS_PLATFORM_KIND","t1");
			setValue("ECS_CUS_MCHT_NO","006t100001");
		}
		
		setValue("MEDIA_NAME", fileName);
		// ==================請款檔資料============================
		setValue("FISC_TX_CODE", dataDMap.get("transactionType"));
		setValue("ONUS_TER_NO", dataDMap.get("onusTerNo"));
		setValue("TE_SEND_DATE", teSendDate);
		setValue("TE_BATCH_NO", dataDMap.get("teBatchNo"));
		setValue("TE_TX_NUM", dataDMap.get("teTxNum"));
		setValue("PURCHASE_DATE", purchaseDate);
		setValue("PURCHASE_TIME", purchaseTime);
		setValueDouble("SOURCE_AMT", sourceAndDestAmtDouble.doubleValue());
		setValueDouble("DEST_AMT", sourceAndDestAmtDouble.doubleValue());
		setValue("CARD_NO", dataDMap.get("cardNo"));
		setValue("ORDER_NO", dataDMap.get("orderNo"));
		setValue("SETTL_FLAG", dataDMap.get("settlFlag"));
		setValue("AUTH_CODE", dataDMap.get("authCode"));
		setValue("FILM_NO", dataDMap.get("filmNo"));
		setValue("EMV_ERROR_CODE", dataDMap.get("emvErrorCode"));
		setValue("MCHT_CHI_NAME", trimAll(dataDMap.get("mchtChiName")));
		setValue("PAYMENT_TYPE", dataDMap.get("paymentType"));
		setValueDouble("BONUS_PAY_CASH", bonusPayCashDouble.doubleValue());
		setValue("BONUS_TRANS_BP", dataDMap.get("bonusTransBp"));
		setValue("REIMB_ATTR", dataDMap.get("reimbAttr"));
		setValue("MCHT_NO", dataDMap.get("mchtNo"));
		setValue("TERMINAL_ID", dataDMap.get("terminalId"));
		setValue("SOURCE_CURR", "901");
		setValue("DEST_CURR", "901");
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
	private HashMap<String, String> getDataD(byte[] bytesArr) throws UnsupportedEncodingException {

		HashMap<String, String> hashMap = new HashMap<String, String>();

		hashMap.put("onusTerNo", CommTxBill.subByteToStr(bytesArr, 0, 16)); // 端末機代號
		hashMap.put("teSendDate", CommTxBill.subByteToStr(bytesArr, 16, 22)); // 端末機傳送資料日期
		hashMap.put("teBatchNo", CommTxBill.subByteToStr(bytesArr, 22, 26)); // 資料批次號碼
		hashMap.put("teTxNum", CommTxBill.subByteToStr(bytesArr, 26, 32)); // 端末機交易序號
		hashMap.put("transactionType", CommTxBill.subByteToStr(bytesArr, 32, 36)); // 交易代號
		// 若交易金額延伸註記碼為Z，則交易日期時間為從37到46、交易金額為從47到57
		// 否則，則交易日期時間為從37到48、交易金額為從49到57
		if (CommTxBill.subByteToStr(bytesArr, 76, 77).equalsIgnoreCase("Z")) {
			hashMap.put("purchaseDateAndTime", CommTxBill.subByteToStr(bytesArr, 36, 46)); // 交易日期時間
			hashMap.put("sourceAndDestAmt", CommTxBill.subByteToStr(bytesArr, 46, 57)); // 交易金額
		} else {
			hashMap.put("purchaseDateAndTime", CommTxBill.subByteToStr(bytesArr, 36, 48)); // 交易日期時間
			hashMap.put("sourceAndDestAmt", CommTxBill.subByteToStr(bytesArr, 48, 57)); // 交易金額
		}
		hashMap.put("cardNo", CommTxBill.subByteToStr(bytesArr, 57, 76)); // 卡號
		hashMap.put("transactionAmtExtensionMark", CommTxBill.subByteToStr(bytesArr, 76, 77)); // 交易金額延伸註記碼
		hashMap.put("orderNo", CommTxBill.subByteToStr(bytesArr, 77, 96)); // 訂單號碼
		hashMap.put("settlFlag", CommTxBill.subByteToStr(bytesArr, 96, 97)); // 清算識別碼
		hashMap.put("authCode", CommTxBill.subByteToStr(bytesArr, 97, 103)); // 授權碼
		hashMap.put("filmNo", CommTxBill.subByteToStr(bytesArr, 103, 126)); // 微縮影片代號
		hashMap.put("emvErrorCode", CommTxBill.subByteToStr(bytesArr, 126, 130)); // 本筆處理狀況
		hashMap.put("mchtChiName", CommTxBill.subByteToStr(bytesArr, 130, 198)); // 主特店中文名稱
		hashMap.put("paymentType", CommTxBill.subByteToStr(bytesArr, 198, 199)); // 支付型態
		hashMap.put("bonusPayCash", CommTxBill.subByteToStr(bytesArr, 199, 211)); // 紅利折抵後之支付金額
		hashMap.put("bonusTransBp", CommTxBill.subByteToStr(bytesArr, 211, 219)); // 紅利折抵點數
		hashMap.put("reimbAttr", CommTxBill.subByteToStr(bytesArr, 219, 220)); // 信用卡繳費註記
		hashMap.put("mchtNo", CommTxBill.subByteToStr(bytesArr, 220, 235)); // 特店代碼
		hashMap.put("terminalId", CommTxBill.subByteToStr(bytesArr, 235, 243)); // 端末機代碼
		hashMap.put("tpan", CommTxBill.subByteToStr(bytesArr, 243, 259)); // 如果是被掃放tpan
		hashMap.put("emptyCol", CommTxBill.subByteToStr(bytesArr, 259, 298)); // 空白欄位
		hashMap.put("endingMark", CommTxBill.subByteToStr(bytesArr, 298, 300)); // 結尾註記

		return hashMap;
	}

	private String trimAll(String s) {
		String result = "";
		if (null != s && !"".equals(s)) {
			result = s.replaceAll("^[*|　*| *| *|//s*]*", "").replaceAll("[*|　*| *| *|//s*]*$", "");
		}
		return result;
	}
	
	public static void main(String[] args) {
		BilE153 proc = new BilE153();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}

}
