/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/09/12  V1.00.00    JeffKung  program initial                           *
 ******************************************************************************/

package Gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*每日帳務科目餘額處理(批次前)*/
public class GenE010 extends AccessDAO {

	public static final boolean DEBUG_MODE = false;

	private final String PROGNAME = "每日帳務科目餘額處理(批次前)  112/09/12  V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String prgmId = "GenE010";
	String hModUser = "";

	String hBusiBusinessDate = "";
	String hBusiVouchDate = "", hPreVouchDate = "";
	String hPcceCurrCode = "";
	String hPcceCurrChiName = "";

	String[] aPcceCurrCode = new String[10];
	String[] aPcceCurrChiName = new String[10];
	int ptrCurrcodeCnt = 0;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			selectPtrCurrcode();

			selectPtrActCode();

			selectActDebt();
			
			selectActAcctCurr();
			
			selectDbaDebt();

			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		hBusiVouchDate = "";
		hPreVouchDate = "";

		sqlCmd = "select business_date ";
		sqlCmd += "  from ptr_businday  ";
		sqlCmd += " fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
			hBusiVouchDate = getValue("business_date");
			hPreVouchDate = comm.lastDate(hBusiVouchDate);
		}
	}

	void selectDbaDebt() throws Exception {

		sqlCmd = "select '901' as curr_code, ";
		sqlCmd += " 'VD-'||acct_code as acct_code, ";
		sqlCmd += " sum(end_bal) as sum_end_bal, ";
		sqlCmd += " sum(end_bal) as sum_dc_end_bal ";
		sqlCmd += "from dba_debt ";
		sqlCmd += "where end_bal > 0  ";
		sqlCmd += " group by acct_code ";
		sqlCmd += " order by acct_code ";
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLog();
		}
		closeCursor(cursorIndex);
	}
	
	/***********************************************************************/
    void selectActAcctCurr() throws Exception {

        sqlCmd =  "select curr_code, 'OP' as acct_code, ";
        sqlCmd += "       sum(end_bal_op) as sum_end_bal , ";
        sqlCmd += "       sum(dc_end_bal_op) as sum_dc_end_bal "; 
        sqlCmd += " from act_acct_curr  ";
        sqlCmd += " group by curr_code ";
        int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLog();
		}
		closeCursor(cursorIndex);
	}

	void selectActDebt() throws Exception {

		sqlCmd = "select decode(curr_code,'','901',curr_code) as curr_code, ";
		sqlCmd += " acct_code, ";
		sqlCmd += " sum(end_bal) as sum_end_bal, ";
		sqlCmd += " sum(dc_end_bal) as sum_dc_end_bal ";
		sqlCmd += "from act_debt ";
		sqlCmd += "where end_bal > 0  ";
		sqlCmd += "   or dc_end_bal > 0 ";
		sqlCmd += " group by decode(curr_code,'','901',curr_code),acct_code ";
		sqlCmd += " order by curr_code,acct_code ";
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLog();
		}
		closeCursor(cursorIndex);
	}

	/***********************************************************************/
	void updateGenPostLog() throws Exception {
		daoTable = "gen_post_log";
		updateSQL = "pre_master_bal = ? ";
		whereStr = "where curr_code = ?  ";
		whereStr += "and vouch_date = ?  ";
		whereStr += "and ac_no = ? ";
		if ("901".equals(getValue("curr_code"))) {
			setDouble(1, getValueDouble("sum_end_bal"));
		} else {
			setDouble(1, getValueDouble("sum_dc_end_bal"));
		}
		setString(2, getValue("curr_code"));
		setString(3, hBusiVouchDate);
		setString(4, getValue("acct_code"));
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log ERROR " + hBusiVouchDate + "," + getValue("curr_code") + ","
					+ getValue("acct_code"));
		}

	}

	void selectPtrActCode() throws Exception {

		sqlCmd = "select acct_code ";
		sqlCmd += "from ptr_actcode ";
		sqlCmd += "where 1=1 ";
		sqlCmd += "ORDER BY decode(acct_code, 'BL', 1,'CA',2, 'ID',3 ,'IT',4 ,'AO',5 ,'OT',6 , 'LF',7 ,'RI',8 ,'PN',8 ,'AF',10,'CF',11,'PF',12, 'CB',13,'CI',14,'CC',15,'DB',16,'SF',17,'AI',18,'DP',19) ";
		int recordCnt = selectTable();

		for (int inti = 0; inti < ptrCurrcodeCnt; inti++) {
			hPcceCurrCode = aPcceCurrCode[inti];
			for (int i = 0; i < recordCnt; i++) {
				insertGenPostLog(getValue("acct_code", i));
			}
			/*另外新增一筆OP (溢付款) */
			insertGenPostLog("OP");
		}
		
		/*VD card*/
		hPcceCurrCode = "901";
		for (int i = 0; i < recordCnt; i++) {
			insertGenPostLog("VD-" + getValue("acct_code", i));
		}
	}

	/***********************************************************************/
	void insertGenPostLog(String txVoucAcNo) throws Exception {

		daoTable = "gen_post_log";
		extendField = "post.";
		setValue("post.CURR_CODE", hPcceCurrCode);
		setValue("post.VOUCH_DATE", hBusiVouchDate);
		setValue("post.AC_NO", txVoucAcNo);
		setValue("post.PRE_VOUCH_DATE", hPreVouchDate);
		setValueDouble("post.PRE_MASTER_BAL", 0);
		setValueDouble("post.THIS_MASTER_BAL", 0);
		setValueDouble("post.THIS_VOUCH_DR_AMT", 0);
		setValueDouble("post.THIS_VOUCH_CR_AMT", 0);
		setValueDouble("post.ADJ_VOUCH_DR_AMT", 0);
		setValueDouble("post.ADJ_VOUCH_CR_AMT", 0);
		setValue("post.CRT_DATE", hBusiBusinessDate);
		setValue("post.CRT_USER", "system");
		setValue("post.APR_DATE", hBusiBusinessDate);
		setValue("post.APR_USER", "system");
		setValue("post.MOD_USER", "system");
		setValue("post.MOD_PGM", javaProgram);
		setValue("post.MOD_TIME", sysDate + sysTime);
		setValue("post.MOD_PGM", javaProgram);
		setValueLong("post.MOD_SEQNO", 1);
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "insert_gen_post_log ERROR " + getValue("post.vouch_date") + ","
					+ getValue("post.curr_code") + "," + getValue("post.ac_no"));
		}

	}

	/***********************************************************************/
	void selectPtrCurrcode() throws Exception {
		sqlCmd = "select curr_code,";
		sqlCmd += " curr_chi_name ";
		sqlCmd += "  from ptr_currcode b  ";
		sqlCmd += " where bill_sort_seq != '' ORDER BY bill_sort_seq ";
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			aPcceCurrCode[i] = getValue("curr_code", i);
			aPcceCurrChiName[i] = getValue("curr_chi_name", i);
		}

		ptrCurrcodeCnt = recordCnt;

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		GenE010 proc = new GenE010();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
