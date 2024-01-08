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

/*每日帳務科目餘額處理(批次後)*/
public class GenE090 extends AccessDAO {

	public static final boolean DEBUG_MODE = false;

	private final String PROGNAME = "每日帳務科目餘額處理(批次後)  112/09/12  V1.00.01";
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
			
			//重跑前清空本日統計的金額
			resetGenPostLog();
			commitDataBase();

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
		updateSQL = "this_master_bal = ? , mod_pgm = 'GenE090' ";
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
	
	/***********************************************************************/
	void resetGenPostLog() throws Exception {
		daoTable   = "gen_post_log";
		updateSQL  = "this_master_bal = 0 ";
		whereStr   = "where vouch_date = ?  ";

		setString(1, hBusiVouchDate);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log (RESET) ERROR: " + hBusiVouchDate );
		}

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		GenE090 proc = new GenE090();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
