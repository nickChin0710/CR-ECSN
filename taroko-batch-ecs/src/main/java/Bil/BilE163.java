/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/30  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109-07-03  V1.00.04   shiyuqi       updated for project coding standard     *
*  109/07/23  V1.00.05    shiyuqi      coding standard, rename field method & format                   * 
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  109/11/17  V1.00.08    JeffKung     日期格式錯誤修正, 檔案內容為民國年月日,誤認為西元年月日
*  111/09/22  V1.00.09    JeffKung     substring改成共用func                 *
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
import com.AStar.TBConvert.Customize.*;;


public class BilE163 extends AccessDAO {
    private final String progname = "FISC-ICACQQN採購卡請款資料入bil_fiscdtl處理 111/09/22  V1.00.09";
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

				//open  F00600000.XXXXXXD.XXXXX file ==================
				filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
				fileNameD = SecurityUtil.verifyPath(fileNameD);
				filePath = Paths.get(filePathFromDB, fileNameD ).toString();
//				inputFileD  = openInputText(filePath, "big5");
				if(openBinaryInput(filePath) == false) {
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
				byte[] bytes = new byte[400];
				int readCnt = 0;
				while(true) {
					readCnt = readBinFile(bytes);
					if(readCnt < 5)
						break;
					
	                ConvertCNS_Big5 cns2big5  = new ConvertCNS_Big5();
	                byte[] tempbyte = new byte[20];
	                System.arraycopy(bytes, 137, tempbyte, 0, 20);
	                cns2big5.convert(null, tempbyte, tempbyte.length, false);
	                //showLogMessage("I", "", new String(cns2big5.getResult(), "big5"));
	                byte[] returnBytes = comc.fixLeft(new String(cns2big5.getResult(), "big5"), 20).getBytes("big5");
	                System.arraycopy(returnBytes, 0, bytes, 137, 20);
	                
	                tempbyte = new byte[110];
	                System.arraycopy(bytes, 201, tempbyte, 0, 110);
	                cns2big5.convert(null, tempbyte, tempbyte.length, false);
	                //showLogMessage("I", "", new String(cns2big5.getResult(), "big5"));
	                returnBytes = comc.fixLeft(new String(cns2big5.getResult(), "big5"), 110).getBytes("big5");
	                System.arraycopy(returnBytes, 0, bytes, 201, 110);
	                
					
					if(bytes.length != 0 ) {
						
						String acquireIndicator = CommTxBill.subByteToStr(bytes, 395, 396); // 收單識別欄位

						// ICACQQND明細通知檔,內有二種資料格式,依收單識別欄位區別:
						// 收單識別欄位=1:共同供應契約收單交易,即採購卡交易
						// 收單識別欄位=2:分期付款收單交易,但本支程式應沒有這塊資料請款.若有,則為錯誤: '不應有分期付款收單交易'
						if (acquireIndicator.equals("2")) {
							showLogMessage("E", "", "不應有分期付款收單交易'");
							return -1;
						}
						
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
		
		// ================================
		//sourceAndDestAmt後2位為小數
		BigDecimal sourceAndDestAmtDouble = 
				commTxBill.getBigDecimalDividedBy100(dataDMap.get("sourceAndDestAmt"));
		//procureOrigAmt後4位為小數
		BigDecimal procureOrigAmtDouble =
				commTxBill.getBigDecimalDivided(dataDMap.get("procureOrigAmt"), 10000);

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
				commTxBill.getONUSTxData(dataDMap.get("fiscTxCode"), dataDMap.get("cardNo"), "", "", "", "");
		
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
		setValue("PROCURE_UNIFORM", dataDMap.get("procureUniform"));
		setValue("MCHT_CHI_NAME", trimAll(dataDMap.get("procureName")));
		setValue("PROCURE_NAME", trimAll(dataDMap.get("procureName")));
		setValue("PROCURE_VOUCHER_NO", dataDMap.get("procureVoucherNo"));
		setValue("PROCURE_RECEIPT_NO", dataDMap.get("procureReceiptNo"));
		setValue("PROCURE_ORIG_CURR", dataDMap.get("procureOrigCurr"));
		setValueDouble("PROCURE_ORIG_AMT", procureOrigAmtDouble.doubleValue());
		setValue("PROCURE_PLAN", dataDMap.get("procurePlan"));
		setValue("PROCURE_LEVEL_1", dataDMap.get("procureLevel1"));
		setValue("PROCURE_LEVEL_2", dataDMap.get("procureLevel2"));
		setValue("PROCURE_TX_NUM", dataDMap.get("procureTxNum"));
		setValue("PROCURE_BANK_FEE", dataDMap.get("procureBankFee"));
		setValue("PROCURE_CHT_FEE", dataDMap.get("procureChtFee"));
		setValue("PROCURE_PAY_AMT", dataDMap.get("procurePayAmt"));
		setValue("PROCURE_TOT_TERM", dataDMap.get("procureTotTerm"));
		setValue("EMV_ERROR_CODE", dataDMap.get("emvErrorCode"));	
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
			
			hashMap.put("onusTerNo", CommTxBill.subByteToStr(bytesArr, 0, 16) ); // 端末機代號
			hashMap.put("teSendDate", CommTxBill.subByteToStr(bytesArr, 16, 22) ); // 端末機傳送資料日期
			hashMap.put("teBatchNo", CommTxBill.subByteToStr(bytesArr, 22, 26) ); // 資料批次號碼
			hashMap.put("teTxNum", CommTxBill.subByteToStr(bytesArr, 26, 32) ); // 端末機交易序號
			hashMap.put("fiscTxCode", CommTxBill.subByteToStr(bytesArr, 32, 36) ); // 交易代號
			hashMap.put("purchaseDateAndTime", CommTxBill.subByteToStr(bytesArr, 36, 48) ); // 交易日期時間
			hashMap.put("sourceAndDestAmt", CommTxBill.subByteToStr(bytesArr, 48, 60) ); // 交易金額
			hashMap.put("cardNo", CommTxBill.subByteToStr(bytesArr, 60, 76) ); // 卡片號碼
			hashMap.put("reserveData1", CommTxBill.subByteToStr(bytesArr, 76, 80) ); // 保留欄位
			hashMap.put("orderNo", CommTxBill.subByteToStr(bytesArr, 80, 99) ); // 訂單號碼
			hashMap.put("settlFlag", CommTxBill.subByteToStr(bytesArr, 99, 100) ); // 清算識別碼
			hashMap.put("authCode", CommTxBill.subByteToStr(bytesArr, 100, 106) ); // 授權碼
			hashMap.put("filmNo", CommTxBill.subByteToStr(bytesArr, 106, 129) ); // 微縮影片代號
			hashMap.put("procureUniform", CommTxBill.subByteToStr(bytesArr, 129, 137) ); // 立約商統一編號
			hashMap.put("procureName", CommTxBill.subByteToStr(bytesArr, 137, 157) ); // 立約商名稱
			hashMap.put("procureVoucherNo", CommTxBill.subByteToStr(bytesArr, 157, 172) ); // 傳票號碼
			hashMap.put("procureReceiptNo", CommTxBill.subByteToStr(bytesArr, 172, 182) ); // 發票號碼
			hashMap.put("procureOrigCurr", CommTxBill.subByteToStr(bytesArr, 182, 185) ); // 原始幣別
			hashMap.put("procureOrigAmt", CommTxBill.subByteToStr(bytesArr, 185, 201) ); // 原始金額
			hashMap.put("procurePlan", CommTxBill.subByteToStr(bytesArr, 201, 251) ); // 工作計劃或下訂機關購案編號
			hashMap.put("procureLevel1", CommTxBill.subByteToStr(bytesArr, 251, 281) ); // 一級用途別
			hashMap.put("procureLevel2", CommTxBill.subByteToStr(bytesArr, 281, 311) ); // 二級用途別
			hashMap.put("procureTxNum", CommTxBill.subByteToStr(bytesArr, 311, 331) ); // 交易編號
			hashMap.put("procureBankFee", CommTxBill.subByteToStr(bytesArr, 331, 341) ); // 銀行手續費
			hashMap.put("procureChtFee", CommTxBill.subByteToStr(bytesArr, 341, 351) ); // 中華電信手續費
			hashMap.put("procurePayAmt", CommTxBill.subByteToStr(bytesArr, 351, 361) ); // 撥付金額
			hashMap.put("procureTotTerm", CommTxBill.subByteToStr(bytesArr, 361, 363) ); // 訂單支付期數
			hashMap.put("reserveData2", CommTxBill.subByteToStr(bytesArr, 363, 395) ); // 保留欄位
			hashMap.put("acquireIndicator", CommTxBill.subByteToStr(bytesArr, 395, 396) ); // 收單識別欄位
			hashMap.put("emvErrorCode", CommTxBill.subByteToStr(bytesArr, 396, 400) ); // 本筆處理狀況

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
		BilE163 proc = new BilE163();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}

}
