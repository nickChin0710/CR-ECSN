/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/17  V1.00.00    Ryan                    program initial             *
*  112/06/16  V1.00.01    Bo Yang            參數未輸入時，取bussiness_date     *
*  112/06/20  V1.00.02    Grace Huang        調整 selectCycPyaj(), 不做act_acno.AUTOPAY_ACCT_S_DATE自動扣繳生效日判讀  *
*  112/12/04  V1.00.03    Zuwei Su           errExit改為 show message & exit program  *  
*  112/12/19  V1.00.04	  Zuwei Su		errRtn改為 show message & exit program  *  
*  113/01/05  V1.00.05	  Grace Huang   變更 FILE_NAME_AP4 ("AW-AP4-CRYYYYMMDD.01") 檔案目錄至/CRDATACREA  *
*  										變更 FILE_NAME_MPP500 ("MPP500_YYYYMMDD.TXT") 檔案目錄至/CREDITCARD  *
*  113/01/05  V1.00.06    Zuwei Su      如果讀不到mkt_goldbill_parm程式不再處理  *  
******************************************************************************/

package Mkt;

import java.text.Normalizer;
import java.util.ArrayList;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class MktC925 extends AccessDAO {

	public final boolean debugD = false;

	private String progname = "金庫幣活動-全新戶自動扣繳 回饋篩選處理 112/06/16  V1.00.01 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommDate comDate = new CommDate();
	CommCrdRoutine comcr = null;

	private static final String PATH_FOLDER = "/media/mkt";
	private static final String FILE_NAME_AP4 = "AW-AP4-CRYYYYMMDD.01";
	private static final String FILE_NAME_MPP500 = "MPP500_YYYYMMDD.TXT";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	private final static String MOD_PGM = "MktC925";
	int debug = 0;

	Buf1 data = new Buf1();

	private String hProcDate = "";
	private String hLastSysdate = "";
	private String hLast2Sysdate = "";
	private String hLastSysdateDay1 = "";
	private String hLast2SysdateDay1 = "";
	
	private int fptr1 = -1;
	private long totCnt = 0;

	private String fmtFileNameAP4 = "";
	private String fmtFileNameMPP500 = "";
	
	String headerTmpBuf = "";
	ArrayList<String> bodyTmpBuf = new ArrayList<String>();
	String footerTmpBuf = "";
	
	private String activeType = "2";
	private String activeCode = "";

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
//				comc.errExit("Usage : MktC925 [sysdate ex:yyyymmdd] ", "");
                showLogMessage("I", "", "Usage : MktC925 [sysdate ex:yyyymmdd] ");
                exitProgram(1);
			}

			// 固定要做的

			if (!connectDataBase()) {
//				comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error !!");
                exitProgram(-1);
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			String sGArgs0 = "";
			if (args.length == 0){
				hProcDate = selectPtrBusinDay();
			} else if (args.length == 1 && args[0].length() == 8) {
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hProcDate = sGArgs0;
			}
			selectLastSysDate();

			showLogMessage("I", "", String.format("輸入參數1 = [%s] ", sGArgs0));
			showLogMessage("I", "", String.format("取得系統日 = [%s] ", hProcDate));
			showLogMessage("I", "", String.format("取得系統日上個月1日 = [%s] ", hLastSysdateDay1));
			showLogMessage("I", "", String.format("取得系統日上個月最後一日 = [%s] ", hLastSysdate));
			showLogMessage("I", "", String.format("取得系統日上上個月1日 = [%s] ", hLast2SysdateDay1));
			showLogMessage("I", "", String.format("取得系統日上上個月最後一日 = [%s] ", hLast2Sysdate));

			fileOpenAP4();
			fileOpen2MPP500();
			if (!selectMktGoldbillParm()) {
				showLogMessage("I","","selectMktGoldbillParm 查無符合資料 !! ");
				return 0;
			}
			selectCycPyaj();
			showLogMessage("I","","=========================================");
			showLogMessage("I","","writeTextAP4() 開始 !! ");
			writeTextAP4();
			copyFileAP4();
			closeOutputText(fptr1);
			ftpProcAP4();
			ftpProcMPP500();
			renameFileAP4();
			renameFileMPP500();
			
			showLogMessage("I", "", String.format("Process records = [%d]", totCnt));

			// ==============================================
			// 固定要做的
			comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
			comcr.callbatchEnd();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	private String selectPtrBusinDay() throws Exception {
		sqlCmd = " select business_date from ptr_businday where 1=1 ";

		if (selectTable() <= 0) {
//			comc.errExit("PTR_BUSINDAY 無资料!!", "");
            showLogMessage("I", "", "PTR_BUSINDAY 查無資料 !!");
            exitProgram(-1);
		}

		return getValue("business_date");
	}

	/***
	 * 1.讀取付費/調整記錄(cyc_pyaj)
	 * 2.取得帳戶的自動扣繳生效日 (ACT_ACNO)
	 * 3.剔除已回饋者(mkt_goldbill_list)
	 ***/
	private void selectCycPyaj() throws Exception {
		
		sqlCmd = "SELECT B.ID_P_SEQNO, ";
		sqlCmd += " B.P_SEQNO, ";
		sqlCmd += " UF_IDNO_ID(B.ID_P_SEQNO) AS ID_NO ";
		sqlCmd += " FROM CYC_PYAJ A LEFT JOIN ACT_ACNO B ON A.P_SEQNO = B.ACNO_P_SEQNO ";
		sqlCmd += " WHERE A.ACCT_TYPE = '01' ";
		sqlCmd += " AND A.PAYMENT_TYPE = 'AUT1' ";
		sqlCmd += " AND A.PAYMENT_DATE BETWEEN ? AND ? ";
		//sqlCmd += " AND B.AUTOPAY_ACCT_S_DATE >= ?  ";
		//sqlCmd += " AND B.AUTOPAY_ACCT_S_DATE <= ? ";
		sqlCmd += " AND B.ID_P_SEQNO NOT IN (SELECT ID_P_SEQNO FROM MKT_GOLDBILL_LIST WHERE ACTIVE_TYPE = '2') ";
		setString(1, hLastSysdateDay1);
		setString(2, hLastSysdate);
		//setString(3, hLast2SysdateDay1);
		//setString(4, hLast2Sysdate);
		openCursor();
		while (fetchTable()) {
			data.initData();
			data.idNo = getValue("ID_NO");
			data.idPSeqno = getValue("ID_P_SEQNO");
		    data.pSeqno = getValue("P_SEQNO"); 
		    
		    bodyTmpBuf.add(data.bodyText());
			
			insertMktGoldbillList();

			totCnt++;
			if (totCnt % 1000 == 0 || totCnt == 1)
				showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

		}
		showLogMessage("I", "", String.format("after selectCycPyaj(), records =[%d]\n", totCnt));
		closeCursor();
	}
	
	private void writeTextAP4() throws Exception {
	    //表頭
	    headerTmpBuf = data.headerText();
		writeTextFile(fptr1, headerTmpBuf);
		
		//明細
		for(int i = 0; i<bodyTmpBuf.size() ;i++)
			writeTextFile(fptr1, bodyTmpBuf.get(i));
		
		//表尾
		footerTmpBuf = data.footerText();
		writeTextFile(fptr1, footerTmpBuf);
	}
	
	private void writeTextMPP500() throws Exception {
	    //表頭
		writeTextFile(fptr1, headerTmpBuf);
		
		//明細
		for(int i = 0; i<bodyTmpBuf.size() ;i++)
			writeTextFile(fptr1, bodyTmpBuf.get(i));
		
		//表尾
		writeTextFile(fptr1, footerTmpBuf);
	}

	/***
	 * 取得mkt_goldbill_parm的參數設定
	 * 
	 * @throws Exception
	 */
	private boolean selectMktGoldbillParm() throws Exception {
		sqlCmd = " SELECT ACTIVE_TYPE,ACTIVE_CODE ";
		sqlCmd += " FROM MKT_GOLDBILL_PARM ";
		sqlCmd += " WHERE (STOP_FLAG <> 'Y' OR STOP_DATE >= ?) AND ACTIVE_TYPE = '2' ";
		sqlCmd += " AND (FEEDBACK_CYCLE= 'M' and (FEEDBACK_DD) = ?) ";
		sqlCmd += " AND (?) between ACTIVE_DATE_S and ACTIVE_DATE_E ";
		setString(1, hLastSysdate);
		setString(2, comStr.right(hProcDate, 2));
		setString(3, hLastSysdate);
		int selectCnt = selectTable();
		if(selectCnt>0) {
			activeType = getValue("ACTIVE_TYPE");
			activeCode = getValue("ACTIVE_CODE");
			return true;
		}
		return false;
	}
	
	private void selectLastSysDate() throws Exception {
		sqlCmd = "SELECT TO_CHAR(LAST_DAY((TO_DATE(?,'YYYYMMDD')-1 MONTHS)),'YYYYMMDD') AS H_LAST_SYSDATE ";
		sqlCmd += " ,TO_CHAR(LAST_DAY((TO_DATE(?,'YYYYMMDD')-2 MONTHS)),'YYYYMMDD') AS H_LAST2_SYSDATE ";
		sqlCmd += " ,TO_CHAR((TO_DATE(?,'YYYYMMDD')-1 MONTHS),'YYYYMM') || '01' AS H_LAST_SYSDATE_DAY1 ";
		sqlCmd += " ,TO_CHAR((TO_DATE(?,'YYYYMMDD')-2 MONTHS),'YYYYMM') || '01' AS H_LAST2_SYSDATE_DAY1 ";
		sqlCmd += " FROM DUAL ";
		setString(1, hProcDate);
		setString(2, hProcDate);
		setString(3, hProcDate);
		setString(4, hProcDate);
		if (selectTable() > 0)
			hLastSysdate = getValue("H_LAST_SYSDATE");//上個月最後一天
			hLast2Sysdate = getValue("H_LAST2_SYSDATE");//上上個月最後一天
			hLastSysdateDay1 = getValue("H_LAST_SYSDATE_DAY1");//上個月第一天
			hLast2SysdateDay1 = getValue("H_LAST2_SYSDATE_DAY1");//上上個月第一天
	}
	
	
	void insertMktGoldbillList() throws Exception{
    	extendField = "MKT_GOLDBILL_LIST.";
        setValue("MKT_GOLDBILL_LIST.ACTIVE_TYPE", activeType);
        setValue("MKT_GOLDBILL_LIST.ACTIVE_CODE", activeCode);
        setValue("MKT_GOLDBILL_LIST.ID_P_SEQNO", data.idPSeqno);  
        setValue("MKT_GOLDBILL_LIST.P_SEQNO", data.pSeqno);  
        setValue("MKT_GOLDBILL_LIST.ID_NO", data.idNo);  
        setValue("MKT_GOLDBILL_LIST.FEEDBACK_DATE", sysDate);  
        setValue("MKT_GOLDBILL_LIST.mod_time", sysDate + sysTime);
        setValue("MKT_GOLDBILL_LIST.mod_user", MOD_PGM);
        setValue("MKT_GOLDBILL_LIST.mod_pgm", MOD_PGM);
        setValueInt("MKT_GOLDBILL_LIST.MOD_SEQNO", 1);
        daoTable = "MKT_GOLDBILL_LIST";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "insert MKT_GOLDBILL_LIST error");
			return;
		}
	}

	/*******************************************************************/
	private void fileOpenAP4() throws Exception {
		fmtFileNameAP4 = FILE_NAME_AP4.replace("YYYYMMDD", hProcDate);

		String temstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
		String fileName = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		fptr1 = openOutputText(fileName, "MS950");
		if (fptr1 == -1) {
//			comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
            showLogMessage("I", "", String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName));
            exitProgram(1);
		}
	}
	
	/*******************************************************************/
	private void fileOpen2MPP500() throws Exception {
		fmtFileNameMPP500 = FILE_NAME_MPP500.replace("YYYYMMDD", hProcDate);

//		String temstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500);
//		String fileName = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
//		fptr1 = openOutputText(fileName, "MS950");
//		if (fptr1 == -1) {
//			comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
//		}
	}

	/*******************************************************************/
	private void ftpProcAP4() throws Exception {

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		/**********
		 * COMM_FTP common function usage
		 ****************************************/
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
		for (int inti = 0; inti < 1; inti++) {
			//commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */	//20240105 grace
			commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
			commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), PATH_FOLDER);
			commFTP.hEflgModPgm = javaProgram;
			
			showLogMessage("I","","=========================================");
			showLogMessage("I", "", "mput " + fmtFileNameAP4 + " 開始傳送....");
			//int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFileNameAP4);
			int errCode = commFTP.ftplogName("CRDATACREA", "mput " + fmtFileNameAP4);	//20240105 grace

			if (errCode != 0) {
				showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileNameAP4 + " 資料" + " errcode:" + errCode);
				if (inti == 0)
					break;
			}

		}
	}
	
	/*******************************************************************/
	private void ftpProcMPP500() throws Exception {

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		/**********
		 * COMM_FTP common function usage
		 ****************************************/
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
		for (int inti = 0; inti < 1; inti++) {
			//commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
			commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), PATH_FOLDER);
			commFTP.hEflgModPgm = javaProgram;

			showLogMessage("I","","=========================================");
			showLogMessage("I", "", "mput " + fmtFileNameMPP500 + " 開始傳送....");
			//int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fmtFileNameMPP500);
			int errCode = commFTP.ftplogName("CREDITCARD", "mput " + fmtFileNameMPP500);			

			if (errCode != 0) {
				showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileNameMPP500 + " 資料" + " errcode:" + errCode);
				if (inti == 0)
					break;
			}

		}
	}
	
	void copyFileAP4() throws Exception {
		String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
		String tmpstr2 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500);

		if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameAP4 + "]COPY失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fmtFileNameAP4 + "] 已COPY至 [" + tmpstr2 + "]");
	}

	void renameFileAP4() throws Exception {
		String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
		String tmpstr2 = String.format("%s%s/backup/%s.%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4,sysDate+sysTime);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameAP4 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fmtFileNameAP4 + "] 已移至 [" + tmpstr2 + "]");
	}
	
	void renameFileMPP500() throws Exception {
		String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500);
		String tmpstr2 = String.format("%s%s/backup/%s.%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500,sysDate+sysTime);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameMPP500 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fmtFileNameMPP500 + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		MktC925 proc = new MktC925();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String idNo;
		String idPSeqno = "";
		String pSeqno = "";

		void initData() {
			idNo = "";
			idPSeqno = "";
			pSeqno = "";
		}

		/***
		 * AW-AP4-CRYYYYMMDD.01 表頭
		 * 
		 * @return
		 * @throws Exception
		 */
		String headerText() throws Exception {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft("TXH", 3)) // 檔頭字串 固定值’TXH’
					.append(comc.fixLeft(sysDate, 8)) // 資料日期(產檔日) 系統日期YYYYMMDD
					.append(comc.fixLeft("AP4", 5)) // 處理的資訊單位代碼 固定值’AP4’
					.append(comc.fixLeft("CR", 5)) // 需求部門代碼 固定值’CR’
					.append(comc.fixLeft("01", 2)) // 檔名的序號 固定值’01’
					.append(comc.fixLeft(" ", 377)) // 保留欄位 空白
				    .append(LINE_SEPERATOR);
			return strBuf.toString();
		}

		/***
		 * AW-AP4-CRYYYYMMDD.01 明細
		 * 
		 * @return
		 * @throws Exception
		 */
		String bodyText() throws Exception {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft("TXD", 3)) // 資料字串 固定值’TXD’
					.append(comc.fixLeft("CRATBP01", 10)) // 交易類型 固定值’CRATBP01’
					.append(comc.fixLeft(sysDate, 8)) // 交易日期 系統日期YYYYMMDD
					.append(comc.fixLeft(String.format("%06d", 0), 6)) // 交易時間 固定值’000000’
					.append(comc.fixLeft(idNo, 30)) // 交易序號 mkt_goldbill_list.id_no
					.append(comc.fixLeft(idNo, 10)) // 身份證號或護照號 mkt_goldbill_list.id_no
					.append(comc.fixLeft(" ", 46)) // 銀行轉出入帳戶 空白
					.append(comc.fixLeft(String.format("%016d", 0), 16)) // 交易金額 固定值, 16個’0’
					.append(comc.fixLeft(String.format("%030d", 0), 30)) // 原幣金額 固定值, 30個’0’
					.append(comc.fixLeft(" ", 21)) // 匯款、信託 空白
					.append(comc.fixLeft("Y", 1)) // NEW_YN全新戶 固定值, ‘Y’
					.append(comc.fixLeft(" ", 61)) // 手續費、關鍵代號、家族相關欄位 空白
					.append(comc.fixLeft(String.format("%024d", 0), 24)) // 信託資產 固定值, 24個’0’
					.append(comc.fixLeft(String.format("%024d", 0), 24)) // 整戶往來總資產 固定值, 24個’0’
					.append(comc.fixLeft(" ", 55)) // 保留欄位 空白
					.append(comc.fixLeft(String.format("%07d500", 0), 10)) // 專案紅利點數 固定值, 7個’0’+’500’
					.append(comc.fixLeft(String.format("%010d", 0), 10)) // Income收入 固定值, 10個’0’
					.append(comc.fixLeft(" ", 1)) // DG_FLAG 空白
					.append(comc.fixLeft(" ", 34)) // Data_spaces 空白
					.append(LINE_SEPERATOR);
			return strBuf.toString();
		}

		/***
		 * AW-AP4-CRYYYYMMDD.01 表尾
		 * 
		 * @return
		 * @throws Exception
		 */
		String footerText() throws Exception {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft("TXE", 3)) // 檔尾字串 固定值’TXE’
					.append(comc.fixLeft("0000", 4)) // 紅利帳號 固定值’0000’
					.append(comc.fixLeft("0000", 4)) // 固定值’0000’
					.append(comc.fixLeft(" ", 389)); // 保留欄位 空白
			return strBuf.toString();
		}
	}
}
