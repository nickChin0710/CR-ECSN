/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/04/01  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.02    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/03  V1.00.03    shiyuqi      updated for project coding standard     *
*  109/07/23  V1.00.04    shiyuqi      coding standard, rename field method & format  
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  109/11/17  V1.00.08    JeffKung     日期格式錯誤修正, 檔案內容為民國年月日,誤認為西元年月日                              *
*  111/02/14  V1.00.09    Ryan         big5 to MS950                        *
*  111/09/22  V1.00.10    JeffKung     substring改成共用func                *
*  112/12/13  V1.00.11    JeffKung     非本行卡不處理
*****************************************************************************/
package Bil;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.HashMap;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;
import com.CommTxBill.FiscTxData;
import com.CommTxBill.PlatformData;

import Dxc.Util.SecurityUtil;

public class BilE173 extends AccessDAO {
    private  final String progname = "FISC-INSTQQN分期付款交易請款資料入bil_fiscdtl處理 112/12/13 V1.00.11";
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
	            
	            businessDate = getBusiDate();
	            
	            // fileDate:傳檔日期FILE_DATE
				// filePathFromDB: ECS_FTP_LOG.local_dir
				// fileNameD:通知檔案名稱FILE_NAME
				// comcr.hCallBatchSeqno
		        fileDate = args[0];
		        filePathFromDB = args[1];
				fileNameD = args[2];
				comcr.hCallBatchSeqno = args[3];

				//open file ==================
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
		            			, String.format("此批資料未經過檢核={%s:%s}", fileNameD,businessDate) );
					 return -1;
				 }else {
					 commTxBill.printDebugString("檢核成功");
					 commTxBill.deleteBilFiscdtl(fctlNo); // delete previous inserting data
				 }
				
				while(true) {
					text = readTextFile(inputFileD);
					bytesArr = text.getBytes("MS950");
					if(text.trim().length() != 0 ) {
						
						//trialer不用處理
						if(CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("T")) {
							continue;
						}
						
						String acquireIndicator = CommTxBill.subByteToStr(bytesArr, 395, 396); // 收單識別欄位

						// ICACQQND明細通知檔,內有二種資料格式,依收單識別欄位區別:
						// 收單識別欄位=1:共同供應契約收單交易，但本支程式應沒有這塊資料請款.若有,則為錯誤: '不應有採購卡交易'
						// 收單識別欄位=2:分期付款收單交易
						if (acquireIndicator.equals("1")) {
							showLogMessage("E", "", "不應有採購卡交易'");
							return -1;
						}
						
						dataDMap = getDataD(bytesArr);  // separate the columns in file
						
						//非本行的Card Bin不處理
						String binNo = comc.getSubString(dataDMap.get("cardNo"),0,6);
						if (chkFromPtrBintable(binNo)==false) {
							continue;
						}
						
						insertBilFiscdtl(dataDMap, fileNameD, fctlNo);
						
					}
		            if (endFile[inputFileD].equals("Y")) 
		            	break;
				}	
				
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
		
		//============= separate MMddyyhhmmss into purchaseDate and purchaseTime===================
		
		String purchaseDate = comc.getSubString(dataDMap.get("purchaseDateAndTime"),0,6);
		String purchaseTime = comc.getSubString(dataDMap.get("purchaseDateAndTime"),6);
		
		//============= turn yyMMdd into yyyyMMdd===================
		// 20201117 : 檔案格式內容為民國RRMMDD, 程式配合修改
		// RR為民國年末兩碼 yyyy = RR+100+1911 (允許空白或日期格式)
		long tmpLong = comcr.str2long(purchaseDate.trim());
		purchaseDate = String.format("%8d", tmpLong +20110000);
		
		tmpLong = comcr.str2long(dataDMap.get("teSendDate").trim());
		teSendDate = String.format("%8d", tmpLong +20110000);
		
		//==================================================
		
		FiscTxData fiscTxData = 
				commTxBill.getONUSTxData(dataDMap.get("fiscTxCode"), dataDMap.get("cardNo"), "", 
						"", "", dataDMap.get("terminalId"));
		
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
		setValue("ECS_PLATFORM_KIND", "");
		setValue("ECS_CUS_MCHT_NO", "");
		setValue("ECS_BILL_TYPE", fiscTxData.getBillType());
		setValue("ECS_ACCT_CODE", fiscTxData.getAcctCode());
		setValue("ECS_SIGN_CODE", fiscTxData.getSignCode());
		setValue("ECS_REAL_CARD_NO", fiscTxData.getRealCardNo());
		setValue("ECS_V_CARD_NO", fiscTxData.getvCardNo());
		setValue("MEDIA_NAME", fileName);
		// ==================請款檔資料============================
		setValue("ONUS_TER_NO", dataDMap.get("onusTerNo"));
		setValue("MCHT_NO", comc.getSubString(dataDMap.get("onusTerNo"), 0,12));  //特店代號取前12碼
		setValue("TE_SEND_DATE", teSendDate);
		setValue("TE_BATCH_NO", dataDMap.get("teBatchNo"));
		setValue("TE_TX_NUM", dataDMap.get("teTxNum"));
		setValue("FISC_TX_CODE", dataDMap.get("fiscTxCode"));
		setValue("PURCHASE_DATE", purchaseDate);
		setValue("PURCHASE_TIME", purchaseTime);
		setValueDouble("SOURCE_AMT", sourceAndDestAmtDouble.doubleValue());
		setValueDouble("DEST_AMT", sourceAndDestAmtDouble.doubleValue());
		setValue("CARD_NO", dataDMap.get("cardNo"));
		setValue("ORDER_NO", dataDMap.get("orderNo"));
		setValue("SETTL_FLAG", dataDMap.get("settlFlag"));
		setValue("AUTH_CODE", dataDMap.get("authCode"));
		setValue("FILM_NO", dataDMap.get("filmNo"));
		setValue("PAYMENT_TYPE", "I");  //default為手續費內含
		setValue("INSTALL_TYPE", dataDMap.get("installType"));
		setValue("TERMINAL_ID", dataDMap.get("terminalId"));
		setValue("INSTALL_TOT_TERM", dataDMap.get("installTotTerm"));
		setValue("INSTALL_FIRST_AMT", dataDMap.get("installFirstAmt"));
		setValue("INSTALL_PER_AMT", dataDMap.get("installPerAmt"));
		setValue("INSTALL_LAST_AMT", dataDMap.get("installLastAmt"));
		setValue("INSTALL_CHARGES", dataDMap.get("installCharges"));
		setValue("INSTALL_ATM_NO", dataDMap.get("installAtmNo"));
		setValue("INSTALL_PROJ_NO", dataDMap.get("installProjNo"));
		setValue("INSTALL_SUPPLY_NO", dataDMap.get("installSupplyNo"));
		setValue("MCHT_CHI_NAME", trimAll(dataDMap.get("mchtChiName")));
		setValue("EMV_ERROR_CODE", dataDMap.get("emvErrorCode"));
		// ==============================================
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
		private HashMap<String,String> getDataD( byte[] bytesArr) throws UnsupportedEncodingException {
			
			HashMap<String,String> hashMap = new HashMap<String,String>();
			
			hashMap.put("onusTerNo", CommTxBill.subByteToStr(bytesArr, 0, 16));  // 端末機代號(001-016)
			hashMap.put("teSendDate", CommTxBill.subByteToStr(bytesArr, 16, 22));  // 端末機傳送資料日期
			hashMap.put("teBatchNo", CommTxBill.subByteToStr(bytesArr, 22, 26));  // 資料批次號碼
			hashMap.put("teTxNum", CommTxBill.subByteToStr(bytesArr, 26, 32));  // 端末機交易序號
			hashMap.put("fiscTxCode", CommTxBill.subByteToStr(bytesArr, 32, 36));  // 交易代號
			hashMap.put("purchaseDateAndTime", CommTxBill.subByteToStr(bytesArr, 36, 48));  // 交易日期時間
			hashMap.put("sourceAndDestAmt", CommTxBill.subByteToStr(bytesArr, 48, 60));  // 交易金額
			hashMap.put("cardNo", CommTxBill.subByteToStr(bytesArr, 60, 76));  // 卡片號碼
			hashMap.put("reserveData1", CommTxBill.subByteToStr(bytesArr, 76, 80) ); // 保留欄位
			hashMap.put("orderNo", CommTxBill.subByteToStr(bytesArr, 80, 99));  // 訂單號碼
			hashMap.put("settlFlag", CommTxBill.subByteToStr(bytesArr, 99, 100));  // 清算識別碼
			hashMap.put("authCode", CommTxBill.subByteToStr(bytesArr, 100, 106));  // 授權碼
			hashMap.put("filmNo", CommTxBill.subByteToStr(bytesArr, 106, 129));  // 微縮影片代號
			hashMap.put("installType", CommTxBill.subByteToStr(bytesArr, 129, 130));  // 交易類型
			hashMap.put("terminalId", CommTxBill.subByteToStr(bytesArr, 130, 138));  // 端末機代碼(131-138)
			hashMap.put("installTotTerm", CommTxBill.subByteToStr(bytesArr, 138, 140));  // 期數
			hashMap.put("installFirstAmt", CommTxBill.subByteToStr(bytesArr, 140, 150));  // 首期金額
			hashMap.put("installPerAmt", CommTxBill.subByteToStr(bytesArr, 150, 160));  // 每期金額
			hashMap.put("installLastAmt", CommTxBill.subByteToStr(bytesArr, 160, 170));  // 末期金額
			hashMap.put("installCharges", CommTxBill.subByteToStr(bytesArr, 170, 180));  // 分期管理費
			hashMap.put("installAtmNo", CommTxBill.subByteToStr(bytesArr, 180, 186));  // 櫃員機台代碼
			hashMap.put("installProjNo", CommTxBill.subByteToStr(bytesArr, 186, 192));  // 分期付款計劃專案代號
			hashMap.put("installSupplyNo", CommTxBill.subByteToStr(bytesArr, 192, 200));  // 來源供應商代碼
			hashMap.put("mchtChiName", CommTxBill.subByteToStr(bytesArr, 200, 230));  // 特約商店中文名稱
			hashMap.put("reserveData2", CommTxBill.subByteToStr(bytesArr, 230, 395) ); // 保留欄位
			hashMap.put("acquireIndicator", CommTxBill.subByteToStr(bytesArr, 395, 396) ); // 收單識別欄位
			hashMap.put("emvErrorCode", CommTxBill.subByteToStr(bytesArr, 396, 400));  // 本筆處理狀況			

			return hashMap;
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
	
	private String trimAll(String s) {
		String result = "";
		if (null != s && !"".equals(s)) {
			result = s.replaceAll("^[*|　*| *| *|//s*]*", "").replaceAll("[*|　*| *| *|//s*]*$", "");
		}
		return result;
	}

	public static void main(String[] args) {
		BilE173 proc = new BilE173();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}

}
