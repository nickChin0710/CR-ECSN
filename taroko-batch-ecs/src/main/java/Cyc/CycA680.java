/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 112-04-20  V1.00.00     Ryan     initial                                            *
* 112-05-09  V1.00.01     Ryan     無法取得STMT_CYCLE,
* 								        程式正常執行結束,新增處理UNPRINT_FLAG_REGULAR欄位                                *
* 112-10-05  V1.00.02     Ryan     delete 增加STMT_CYCLE 條件                                                                            *
* 112-12-12  V1.00.03     Ryan     調整log訊息                                                                                                                      *
 **************************************************************************************/

package Cyc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;


public class CycA680 extends AccessDAO {
	private final String progname = "產生信用卡對帳單歷史檔 112/12/12 V.00.03";
	private final static int COMMIT_CNT = 20000;
	private final static int DELETE_CNT = 1000000;
	private static final int OUTPUT_BUFF_SIZE = 100000;
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalAcmmCnt = 0;
	int totalCurrCnt = 0;
	int totalAbemCnt = 0;
	int tolalCnt = 0;
	private String hThisAcctMonth = "";
	private String hStmtCycle = "";
	private String hUnprintFlagRegular = "";
	private int abemCnt = 0;
	private int acmmCnt = 0;
	private int acmmCurrCnt = 0;

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			showLogMessage("I", "","-->connect DB: " + getDBalias()[0]);

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			
			String parm1 = "";
			String hBusindayDate = "";
			if(args.length == 1) {
				if(!commDate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				parm1 = args[0];
				hBusindayDate = parm1;
			}
			showLogMessage("I", "", String.format("輸入參數日期1 = [%s]", parm1));
			hBusindayDate = getProgDate(hBusindayDate,"D");

			if(!selectPtrWorkday(hBusindayDate)) {
//				comc.errExit("無法取得STMT_CYCLE", "");
				showLogMessage("E", "", "本日非關帳日,程式執行結束");
				finalProcess();
				return 0;
			}
			showLogMessage("I", "", String.format("取得SYCLE = [%s]", hStmtCycle));
			showLogMessage("I", "", "");

			selectCycAcmmHstCnt(hStmtCycle);
			deleteCycAcmmHst();
			showLogMessage("I", "", String.format("開始處理CYC_ACMM_%s", hStmtCycle));
			selectCycAcmm(hStmtCycle);
			showLogMessage("I", "", "");
	
			selectCycAcmmCurrHstCnt(hStmtCycle);
			deleteCycAcmmCurrHst();
			showLogMessage("I", "", String.format("開始處理CYC_ACMM_CURR_%s", hStmtCycle));
			selectCycAcmmCurr(hStmtCycle);
			showLogMessage("I", "", "");
			
			selectCycAbemHstCnt(hStmtCycle);
			deleteCycAbemHst();
			showLogMessage("I", "", String.format("開始處理CYC_ABEM_%s", hStmtCycle));
			selectCycAbem(hStmtCycle);
			commitDataBase();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	
	void selectCycAcmm(String stmtCycle) throws Exception {
		int tolCnt = 0;
		String tableName = String.format("CYC_ACMM_%s", stmtCycle);
		sqlCmd = " SELECT * FROM ";
		sqlCmd += tableName;
		this.openCursor();
		int cnt = 1;
		while (fetchTable()) {
			tolCnt ++;
			hUnprintFlagRegular = getValue("UNPRINT_FLAG_REGULAR");
			insertCycAcmmHst();
			if(cnt++ % COMMIT_CNT == 0)
				commitDataBase();
			
			if (tolCnt % OUTPUT_BUFF_SIZE == 0) {
				showLogMessage("I", "", String.format("已處理%d筆資料", tolCnt));
			}
		}
		showLogMessage("I", "", String.format("共處理%d筆資料", tolCnt));
		this.closeCursor();
	}
	
	void selectCycAcmmCurr(String stmtCycle) throws Exception {
		int tolCnt = 0;
		String tableName = String.format("CYC_ACMM_CURR_%s", stmtCycle);
		sqlCmd = " SELECT * FROM ";
		sqlCmd += tableName;
		this.openCursor();
		int cnt = 1;
		while (fetchTable()) {
			tolCnt ++;
			insertCycAcmmCurrHst();
			if(cnt++ % COMMIT_CNT == 0)
				commitDataBase();
			
			if (tolCnt % OUTPUT_BUFF_SIZE == 0) {
				showLogMessage("I", "", String.format("已處理%d筆資料", tolCnt));
			}
		}
		showLogMessage("I", "", String.format("共處理%d筆資料", tolCnt));
		this.closeCursor();
	}

	void selectCycAbem(String stmtCycle) throws Exception {
		int tolCnt = 0;
		String tableName = String.format("CYC_ABEM_%s", stmtCycle);
		sqlCmd = " SELECT * FROM ";
		sqlCmd += tableName;
		this.openCursor();
		int cnt = 1;
		while (fetchTable()) {
			tolCnt ++;
			insertCycAbemHst();
			if(cnt++ % COMMIT_CNT == 0)
				commitDataBase();
			
			if (tolCnt % OUTPUT_BUFF_SIZE == 0) {
				showLogMessage("I", "", String.format("已處理%d筆資料", tolCnt));
			}
		}
		showLogMessage("I", "", String.format("共處理%d筆資料", tolCnt));
		this.closeCursor();
	}
	

	void selectCycAbemHstCnt(String stmtCycle) throws Exception {
		sqlCmd = " select count(*) ABEM_CNT from CYC_ABEM_HST A WHERE EXISTS (SELECT 1 FROM ACT_ACNO B WHERE B.P_SEQNO = A.P_SEQNO AND A.ACCT_MONTH = ? AND B.STMT_CYCLE = ? ) ";
		setString(1,hThisAcctMonth);
		setString(2,hStmtCycle);
		selectTable();
		abemCnt = getValueInt("ABEM_CNT");
//		showLogMessage("I", "", String.format("CYC_ABEM_HST共%d筆資料", abemCnt));
		showLogMessage("I", "", "");
		showLogMessage("I", "", "CYC_ABEM_HST開始刪除資料...");
	}
	
	void selectCycAcmmHstCnt(String stmtCycle) throws Exception {
		sqlCmd = " select count(*) ACMM_CNT from CYC_ACMM_HST A  WHERE EXISTS (SELECT 1 FROM ACT_ACNO B WHERE B.P_SEQNO = A.P_SEQNO AND A.ACCT_MONTH = ? AND B.STMT_CYCLE = ? ) ";
		setString(1,hThisAcctMonth);
		setString(2,hStmtCycle);
		selectTable();
		acmmCnt = getValueInt("ACMM_CNT");
//		showLogMessage("I", "", String.format("CYC_ACMM_HST共%d筆資料", acmmCnt));
		showLogMessage("I", "", "");
		showLogMessage("I", "", "CYC_ACMM_HST開始刪除資料...");
	}
	
	void selectCycAcmmCurrHstCnt(String stmtCycle) throws Exception {
		sqlCmd = " select count(*) ACMM_CURR_CNT from CYC_ACMM_CURR_HST A WHERE EXISTS (SELECT 1 FROM ACT_ACNO B WHERE B.P_SEQNO = A.P_SEQNO AND A.ACCT_MONTH = ? AND B.STMT_CYCLE = ? ) ";
		setString(1,hThisAcctMonth);
		setString(2,hStmtCycle);
		selectTable();
		acmmCurrCnt = getValueInt("ACMM_CURR_CNT");
//		showLogMessage("I", "", String.format("CYC_ACMM_CURR_HST共%d筆資料", acmmCurrCnt));
		showLogMessage("I", "", "");
		showLogMessage("I", "", "CYC_ACMM_CURR_HST開始刪除資料...");
	}
	
	void deleteCycAcmmHst() throws Exception {
		int i = DELETE_CNT;
		while (true) {
			daoTable = "CYC_ACMM_HST A";
			whereStr = " WHERE EXISTS (SELECT 1 FROM ACT_ACNO B WHERE B.P_SEQNO = A.P_SEQNO AND A.ACCT_MONTH = ? AND B.STMT_CYCLE = ? ) ";
			whereStr += String.format("fetch first %d rows only ",
					i < acmmCnt ? DELETE_CNT : acmmCnt - (i - DELETE_CNT));
			setString(1, hThisAcctMonth);
			setString(2, hStmtCycle);
			deleteTable();
			if (i >= acmmCnt) {
				showLogMessage("I", "", String.format("CYC_ACMM_HST資料刪除完成 ,筆數 = [%d]", acmmCnt));
				commitDataBase();
				break;
			}
			showLogMessage("I", "", String.format("刪除筆數 = [%d]", i));
			i += DELETE_CNT;
			commitDataBase();
		}
	}
	
	void deleteCycAcmmCurrHst() throws Exception {
		int i = DELETE_CNT;
		while (true) {
			daoTable = "CYC_ACMM_CURR_HST A";
			whereStr = " WHERE EXISTS (SELECT 1 FROM ACT_ACNO B WHERE B.P_SEQNO = A.P_SEQNO AND A.ACCT_MONTH = ? AND B.STMT_CYCLE = ? ) ";
			whereStr += String.format("fetch first %d rows only ",
					i < acmmCurrCnt ? DELETE_CNT : acmmCurrCnt - (i - DELETE_CNT));
			setString(1, hThisAcctMonth);
			setString(2, hStmtCycle);
			deleteTable();
			if (i >= acmmCurrCnt) {
				showLogMessage("I", "", String.format("CYC_ACMM_CURR_HST資料刪除完成 ,筆數 = [%d]", acmmCurrCnt));
				commitDataBase();
				break;
			}
			showLogMessage("I", "", String.format("刪除筆數 = [%d]", i));
			i += DELETE_CNT;
			commitDataBase();
		}
	}
	
	void deleteCycAbemHst() throws Exception {
		int i = DELETE_CNT;
		while(true) {
		     daoTable = "CYC_ABEM_HST A";
		     whereStr = " WHERE EXISTS (SELECT 1 FROM ACT_ACNO B WHERE B.P_SEQNO = A.P_SEQNO AND A.ACCT_MONTH = ? AND B.STMT_CYCLE = ? ) ";
		     whereStr += String.format("fetch first %d rows only ", i<abemCnt?DELETE_CNT:abemCnt-(i-DELETE_CNT));
		     setString(1,hThisAcctMonth);
		     setString(2,hStmtCycle);
		     deleteTable();
		     if(i>=abemCnt) {
		    	 showLogMessage("I", "", String.format("CYC_ABEM_HST資料刪除完成 ,筆數 = [%d]", abemCnt));
		    	 commitDataBase();
		    	 break;
		     }
		 	 showLogMessage("I", "", String.format("刪除筆數 = [%d]", i));
		     i += DELETE_CNT;
		     commitDataBase();
		}
	}
	
	void insertCycAcmmCurrHst() throws Exception {
		daoTable = "CYC_ACMM_CURR_HST";
		setValue("ACCT_MONTH", hThisAcctMonth);
		setValue("MOD_TIME", sysDate + sysTime);
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "INSERT CYC_ACMM_CURR_HST ERROR");
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("E", "", String.format("INSERT CYC_ACMM_CURR_HST DUPLICATE ,P_SEQNO = [%s] ,CURR_CODE = [%s] ,ACCT_MONTH = [%s]", getValue("P_SEQNO"),getValue("CURR_CODE"),hThisAcctMonth));
			return;
		}
		totalCurrCnt ++;
	}
	
	void insertCycAbemHst() throws Exception {
		daoTable = "CYC_ABEM_HST";
		setValue("ACCT_MONTH", hThisAcctMonth);
		setValue("MOD_TIME", sysDate + sysTime);
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "INSERT CYC_ABEM_HST ERROR");
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("E", "", "INSERT CYC_ABEM_HST DUPLICATE");
			return;
		}
		totalAbemCnt ++;
	}
	
	
	void insertCycAcmmHst() throws Exception {
		daoTable = "CYC_ACMM_HST";
		setValue("ACCT_MONTH", hThisAcctMonth);
		setValue("UNPRINT_FLAG_REGULAR",hUnprintFlagRegular);
		setValue("MOD_TIME", sysDate + sysTime);
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "INSERT CYC_ACMM_HST ERROR");
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("E", "", String.format("INSERT CYC_ACMM_HST DUPLICATE ,P_SEQNO = [%s] ,ACCT_MONTH = [%s]", getValue("P_SEQNO"),hThisAcctMonth));
			return;
		}
		totalAcmmCnt ++;
	}
	
	
	
	private boolean selectPtrWorkday(String hBusindayDate) throws Exception {
		sqlCmd = "SELECT STMT_CYCLE,THIS_ACCT_MONTH,THIS_CLOSE_DATE FROM PTR_WORKDAY ";
		sqlCmd += " WHERE THIS_CLOSE_DATE = ? ";
		setString(1,hBusindayDate);
		int selectCnt = selectTable();
		if(selectCnt == 0)
			return false;
		hStmtCycle = getValue("STMT_CYCLE");
		hThisAcctMonth = getValue("THIS_ACCT_MONTH");
		return true;
	}
	
	
	/***********************************************************************/
	public int tolalCnt() {
		return tolalCnt ++;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CycA680 proc = new CycA680();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
