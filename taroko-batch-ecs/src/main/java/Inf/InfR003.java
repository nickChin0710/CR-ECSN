/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  110/08/24  V1.00.00   machao     create                        *
*  112/03/13  V1.00.01   Zuwei Su   日期轉民國年(7碼),欄位長度調整                      *
*  112/07/19  V1.00.02   Ryan       By cycle結帳日 +1日  產生各cycle資料, 檔名為結帳日, 其他非結帳日出空檔                *     
*  112/08/09  V1.00.03   Ryan       加讀CYC_PYAJ整合產出Textfile:  By 雙幣卡分別整合               *
*  112/08/25  V1.00.04   Ryan       修改檔案格式                                                    *
*  112/08/29  V1.00.05   Ryan       修改檔案格式                                                    *
*  112/09/12  V1.00.06   Ryan       空檔檔名日期-1                          *
*  112/09/16  V1.00.07   Simon      中文特店名稱空白時抓中文特店名稱        *
*  112/09/20  V1.00.08   Ryan       讀取CYC_PYAJ 增加CARD_NO                  *
*****************************************************************************/
package Inf;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import com.BaseBatch;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

public class InfR003 extends BaseBatch{

	private final String progname = "每天產生送CRM [計算歸戶每月帳單明細]資料檔 112/09/20 V1.00.08";

	CommCrd comc = new CommCrd();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommTxInf commTxInf = null;
	CommCol commCol = null;
	CommString commStr = new CommString();
	private static final String CRM_FOLDER = "media/crm/";
	private static final String DATA_FORM = "CCTOLS2";
	private String isFileName = "";
	private int ilFile003 ;

	String hOpenDate = "";
	String hSysDate = "";
	String hIdNo = "";
	String hCardNo = "";
	String hPostDate = "";
	String hMchtchiName = "";
	String hPurchareDate = "";
	String hTxnCode = "";
	double hBegBal = 0.0;
	String hMchtCity = "";
	String hAcctType = "";
	String hMccCode = "";
	String hAuthCode = "";
	String hSourceCurr = "";
	double hSourceAmt = 0.0;
	String hStmtCycle = "";
	String hPSeqno = "";
	String hThisAcctMonth = "";
	String hCurrCode = "";
	String paymentType = "";
	String billDesc = "";
	HashMap<String,Integer> pSeqnoTmp = new HashMap<String,Integer>();
	
// =****************************************************************************
	public static void main(String[] args) {
		InfR003 proc = new InfR003();
		proc.mainProcess(args);
		proc.systemExit();
	}

// =============================================================================	

	@Override
	protected void dataProcess(String[] args) throws Exception {
		// TODO Auto-generated method stub
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfR003 [business_date]");
			errExit(1);
		}
		dbConnect();
		if (liArg == 1) {
			hSysDate = args[0];
		}	

		if (empty(hSysDate)) {
			hSysDate =  hBusiDate;
		}

		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		commTxInf = new CommTxInf(getDBconnect(), getDBalias());
		commCol = new CommCol(getDBconnect(), getDBalias());
		
		// 產生Header檔
		dateTime(); // update the system date and time
		String searchDate = (args.length == 0) ? hSysDate : args[0].trim();
//		String searchDate2 = searchDate;
		searchDate = getProgDate(searchDate, "D");
//		searchDate2 = getProgDate(searchDate2, "M");
		
		boolean isWorkDay = selectPtrWorkday(searchDate);
		
		if(isWorkDay == true)
			searchDate = commString.left(searchDate, 6) + hStmtCycle;

		// convert YYYYMMDD into YYMMDD
		String fileNameSearchDate = searchDate.substring(2);
		// convert YYYYMM into YYMM
//		String fileNameSearchDate2 = searchDate2.substring(2);
		

		
		if(isWorkDay == true) {
			showLogMessage("I", "",String.format("今日結帳日 + 1日 = [%s]",hSysDate));
			showLogMessage("I", "",String.format("cycle = [%s]",hStmtCycle));
		}else {
			showLogMessage("I", "",String.format("今日為非結帳 +1日 = [%s],產生空檔",hSysDate));
			fileNameSearchDate = commDate.dateAdd(searchDate, 0, 0, -1).substring(2);
		}
			
		isFileName = "CCTOLS2_" + fileNameSearchDate + ".DAT";
		checkOpen();
		if(isWorkDay == true)
			selectDataType();
		closeOutputText(ilFile003);
		
		String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
//		String datFileName2 = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate2, CommTxInf.DAT_EXTENSION);
		
		String fileFolder =  Paths.get(comc.getECSHOME(), CRM_FOLDER).toString();
		String fileFolder2 =  Paths.get(comc.getECSHOME(), CRM_FOLDER).toString();
		
		boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), totalCnt);
		if (isGenerated == false) {
			comc.errExit("產生HDR檔錯誤!", "");
		}
		String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
//		String hdrFileName2 = datFileName2.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
		//檢核searchDate是否為每月最後一天營業日
//		boolean isLastBusinday = commCol.isLastBusinday(searchDate);
		
		//每月最後一個營業日多產生一份
//		if(isLastBusinday) {
//			copyFile(datFileName,fileFolder,datFileName2,fileFolder2);
//			boolean isGenerated2 = commTxInf.generateTxtCrmHdr(fileFolder2, datFileName2, searchDate, sysDate, sysTime.substring(0,4), totalCnt);
//			if (isGenerated2 == false) {
//				comc.errExit("每月最後一個營業日產生HDR檔錯誤!", "");
//			}
//		}
		
		procFTP(fileFolder, datFileName, hdrFileName);
//		if(isLastBusinday)
//			procFTP(fileFolder2, datFileName2, hdrFileName2);
		endProgram();
	}
/* = ************************************************************************/
	void selectDataType() throws Exception {
		initailData();
		sqlCmd = "SELECT "
				+ "I.ID_NO AS id_no,"
				+ "D.CARD_NO AS card_no,"
				+ "D.POST_DATE AS post_date,"
				+ "B.MCHT_CHI_NAME AS mcht_chi_name,"
				+ "B.MCHT_ENG_NAME AS mcht_eng_name,"
				+ "D.PURCHASE_DATE AS purchase_date, "
//				+ "decode(D.TXN_CODE,'IF', '12','AF','60','CF','37','DF','60','05','40','06', '41','07','30','25','43','26','42','27','31', D.txn_code) AS txn_code, "
//				+ "decode(D.TXN_CODE,'IN', '40','LF', '60','RI', '12','TL', '16','IF', '12','AF','60','CF','37','DF','60','05','40','06', '41','07','30','25','43','26','42','27','31', D.txn_code) AS txn_code, "
				+ "decode(D.TXN_CODE,'IN', '40','LF', '60','RI', '12','TL', '16','IF', '12','AF','60','CF','37','DF','60','05',(decode(D.ACCT_CODE,'PF','16','40')),'06', '41','07','30','25','43','26','42','27','31', D.txn_code) AS txn_code, "
				+ "D.DC_BEG_BAL AS beg_bal,"
				+ "B.MCHT_CITY AS mcht_city,"
//				+ "SUBSTR(D.ACCT_TYPE, 2, 1)||'06' AS acct_type, "
				+ "decode(D.CURR_CODE,'840', '606','392', '607', decode(D.ACCT_TYPE,'01','106','306')) as acct_type, "
				+ "M.MCC_CODE AS mcc_code, "
				+ "B.AUTH_CODE AS auth_code, "
				+ "B.SOURCE_CURR AS source_curr,"
				+ "B.SOURCE_AMT AS source_amt, "
				+ "D.p_seqno, "
				+ "D.curr_code "
				+ "FROM act_debt D "
//				+ "INNER JOIN ptr_businday A ON D.post_date=A.business_date "				
				+ "left OUTER JOIN bil_bill B ON D.reference_no=B.reference_no "
				+ "left OUTER JOIN bil_merchant M ON M.mcht_no=B.mcht_no "
				+ "left OUTER JOIN crd_idno I ON I.id_p_seqno=B.major_id_p_seqno "
				+ "WHERE D.ACCT_MONTH = ? "
				+ "and D.stmt_cycle = ? "
				+ "ORDER BY D.acct_type, D.stmt_cycle, D.p_seqno";
		setString(1,new CommString().left(hSysDate, 6));
		setString(2,hStmtCycle);
		openCursor();
		while (fetchTable()){
			hIdNo = commString.rpad(colSs("id_no"),16);
			hCardNo = commString.rpad(colSs("card_no"),16);
			hPostDate = commString.rpad(commDate.toTwDate(colSs("post_date")),7);
			hMchtchiName = getValue("mcht_chi_name");
			if (hMchtchiName.length() != 0) {
  			hMchtchiName = commString.rpad(colSs("mcht_chi_name"),40);
			} else {
  			hMchtchiName = commString.rpad(colSs("mcht_eng_name"),40);
			}

			hPurchareDate = commString.rpad(commDate.toTwDate(colSs("purchase_date")),7);
			hTxnCode = commString.rpad(colSs("txn_code"),2);
			hBegBal = colNum("beg_bal");
			hMchtCity = commString.rpad(colSs("mcht_city"),13);
			hAcctType = commString.rpad(colSs("acct_type"),3);
			hMccCode = commString.rpad(colSs("mcc_code"), 5);
			hAuthCode = commString.rpad(colSs("auth_code"),6);
			hSourceCurr = commString.rpad(colSs("source_curr"),3);
			hSourceAmt = colNum("source_amt");
			hPSeqno = colSs("p_seqno");
			hCurrCode = colSs("curr_code");
			writeTextFile(hMchtchiName);
			String keyTmp = hPSeqno + "#" + hCurrCode;
			if(pSeqnoTmp.get(keyTmp) == null) {
				selectCycPyaj();
				pSeqnoTmp.put(keyTmp, 0);
			}
			if(totalCnt % 100000 == 0) {
				showLogMessage("I", "",String.format("已處理 [%d] 筆",totalCnt));
			}
		}
		closeCursor();
	}
	
	
// =============================================================================		
	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crm/%s", comc.getECSHOME(), isFileName);
//		File file = checkFileExistence(lsTemp); // 目錄不存在則創建
		ilFile003 = openOutputText(lsTemp, "big5");
		if (ilFile003 < 0) {
			printf("CCTOLS2 產檔失敗 ! ");
			errExit(1);
		}
	}

// =============================================================================
	public void readSysdate() throws Exception {

		sqlCmd = " select to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
		sqlCmd += "from dual";
		sqlSelect();
		hOpenDate = colSs("sysdate1");

	}
	
// =============================================================================
	/**
	 * 產生檔案，如下格式<br>
	 * 01	正卡人ID		X(16)	1-16	　<br>
	 * 		區隔符號		X(1)	17		區隔符號「!」<br>
	 * 02	卡號			9(16)	18-33	 <br>
	 * 		區隔符號		X(1)	34		區隔符號「!」<br>
	 * 03	入賬日期		X(07)	35-41	<br>
	 * 		區隔符號		X(1)	42		區隔符號「!」<br>
	 * 04	賬務摘要敘述	x(40)	43-82	<br>
	 * 		區隔符號		X(1)	83		區隔符號「!」<br>
	 * 05	查登號碼		9(11)	84-94
	 * 		區隔符號		X(1)	95		區隔符號「!」<br>
	 * 06	消費日期		9(07)	96-102	<br>
	 * 		區隔符號		X(1)	103		區隔符號「!」<br>
	 * 07	TXCD		9(02)	104-105	<br> 
	 * 		區隔符號		X(1)	106		區隔符號「!」<br>
	 * 08				X(01)	107		負號欄位<br>
	 * 		區隔符號		X(1)	108		區隔符號「!」<br>
	 * 09	賬單金額		9(07)	109-117	<br>
	 * 		區隔符號		X(1)	118		區隔符號「!」<br>
	 * 10	消費地		X(13)	119-131	<br>
	 * 		區隔符號		X(1)	132		區隔符號「!」<br>
	 * 11	銀行別		9(03)	133-135 +06"<br>
	 * 		區隔符號		X(1)	136		區隔符號「!」<br>
	 * 12	FILLER		X(125)	137-261	 <br>
	 * 		區隔符號		X(1)	262		區隔符號「!」<br>
	 * 13	MCC Code	9(05)	263-267	<br>
	 * 		區隔符號		X(1)	268		區隔符號「!」<br>
	 * 14	授權碼		X(06)	269-274	<br>
	 * 		區隔符號		X(1)	275		區隔符號「!」<br>
	 * 15	原始幫別		9(03)	276-278	<br>
	 * 		區隔符號		X(1)	279		區隔符號「!」<br>
	 * 16	原始金額		S9(10)	280-291	<br>
	 * 		區隔符號		X(1)	292		區隔符號「!」<br>
	 * @param idNo
	 * @param blockCode
	 * @param marketAgreeBase
	 * @return
	 * @throws Exception 
	 */	

	void writeTextFile(String str40) throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String newLine = "\r\n";
		tempBuf.append(comc.fixLeft(hIdNo, 16));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(hCardNo, 16));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(commDate.toTwDate(hPostDate), 7));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(str40, 40));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(" ", 11));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(commDate.toTwDate(hPurchareDate), 7));
		tempBuf.append(comc.fixLeft("\006", 1));
		// txcd
		tempBuf.append(comc.fixLeft(hTxnCode, 2));
		tempBuf.append(comc.fixLeft("\006", 1));
		// 負號欄位
		tempBuf.append(comc.fixLeft("", 1));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(String.format("%.2f", hBegBal), 14));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(hMchtCity, 13));
		tempBuf.append(comc.fixLeft("\006", 1));
		// 銀行別
		tempBuf.append(comc.fixLeft(hAcctType, 3));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(" ", 125));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(hMccCode, 5));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(hAuthCode, 6));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(hSourceCurr, 3));
		tempBuf.append(comc.fixLeft("\006", 1));
		tempBuf.append(comc.fixLeft(String.format("%.2f", hSourceAmt), 14));
		tempBuf.append(comc.fixLeft("\006", 2));
		totalCnt++;
		tempBuf.append(newLine);
		this.writeTextFile(ilFile003, tempBuf.toString());
	}
	
	
// =============================================================================
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRM", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

// =============================================================================
	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}

// =============================================================================
	
	void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();

		if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
			comc.errExit("ERROR : 檔案[" + datFileName2 + "]copy失敗!", "");
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
	}

	/**
     * 检查文件的存在性,不存在则创建相应的路由，文件
     * @param filepath
     * @return 创建失败则返回null
     */
	public File checkFileExistence(String filepath){        
        File file = new File(filepath);
        try {
            if (!file.exists()){
                if (filepath.charAt(filepath.length()-1) == '/' || filepath.charAt(filepath.length()-1) == '\\') {
					file.mkdirs();
                } else {
                	String[] split = filepath.split("[^/\\\\]+$");
                    checkFileExistence(split[0]);
                    file.createNewFile();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }
	
	private boolean selectPtrWorkday(String searchDate) throws Exception {
		extendField = "workday.";
		sqlCmd = "select this_acct_month,stmt_cycle from ptr_workday where to_char(to_date(this_close_date,'yyyymmdd') + 1 days , 'yyyymmdd') = ? ";
		setString(1,searchDate);
		int n = selectTable();
		if(n > 0) {
			hStmtCycle = getValue("workday.stmt_cycle");
			hThisAcctMonth = getValue("workday.this_acct_month");
			return true;
		}
		return false;
	}
	
	private void selectCycPyaj() throws Exception {
		String classCode = "";
		hPurchareDate = "";
		hBegBal = 0.0;
		paymentType = "";
		billDesc = "";
		hTxnCode = "";
		hCardNo = "";
		hPostDate = "";
		hMchtCity = "";
    hMccCode  = "";
	  hAuthCode = "";
	  hSourceCurr = "";
	  hSourceAmt = 0.0;
		extendField = "pyaj.";
		sqlCmd = "select a.PAYMENT_DATE ,a.DC_PAYMENT_AMT ,a.CLASS_CODE ,a.PAYMENT_TYPE ,b.BILL_DESC "
				+ ",(SELECT CARD_NO FROM CRD_CARD WHERE P_SEQNO = a.P_SEQNO order by CURRENT_CODE FETCH FIRST 1 ROWS ONLY) AS CARD_NO "
				+ " FROM CYC_PYAJ a left join PTR_PAYMENT b on a.PAYMENT_TYPE = b.PAYMENT_TYPE ";
		sqlCmd += " WHERE a.P_SEQNO = ? AND a.CURR_CODE = ? AND a.SETTLE_FLAG = 'B' AND substr(a.SETTLE_DATE,1,6) = ? ";
		setString(1,hPSeqno);
		setString(2,hCurrCode);
		setString(3,hThisAcctMonth);
		int n = selectTable();
		for(int i = 0;i<n;i++) {
			hPurchareDate = getValue("pyaj.PAYMENT_DATE",i);
			hPostDate     = getValue("pyaj.PAYMENT_DATE",i);
			hBegBal = getValueDouble("pyaj.DC_PAYMENT_AMT",i);
			classCode = getValue("pyaj.CLASS_CODE",i);
			paymentType = getValue("pyaj.PAYMENT_TYPE",i);
			billDesc = getValue("pyaj.BILL_DESC",i);
			hCardNo = getValue("pyaj.CARD_NO",i);
			
			if("P".equals(classCode)) {
				if("OP02".equals(paymentType)) {
					hTxnCode = "27";
				}else if ("REFU".equals(paymentType)) {
					hTxnCode = "41";
				}else if ("0501".equals(paymentType)) {
					hTxnCode = "43";
				}else {
					hTxnCode = "20";
				}
			}
			if("B".equals(classCode)) {
				hTxnCode = "43";
			}
			if("A".equals(classCode)) {
				if("DE14".equals(paymentType)) {
					hTxnCode = "61";
				}else if ("DE09".equals(paymentType)) {
					hTxnCode = "61";
				}else if ("DE10".equals(paymentType)) {
					hTxnCode = "61";
				}else if ("DE13".equals(paymentType)) {
					hTxnCode = "13";
				}else {
					hTxnCode = "48";
				}
			}
			if(hBegBal != 0)
				writeTextFile(paymentType+billDesc);
		}
	}
	
	void initailData(){
		 hIdNo = "";
		 hCardNo = "";
		 hPostDate = "";
		 hMchtchiName = "";
		 hPurchareDate = "";
		 hTxnCode = "";
		 hBegBal = 0.0;
		 hMchtCity = "";
		 hAcctType = "";
		 hMccCode = "";
		 hAuthCode = "";
		 hSourceCurr = "";
		 hSourceAmt = 0.0;
		 hPSeqno = "";
		 hCurrCode = "";
		 paymentType = "";
		 billDesc = "";
	}
}
