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

/*每日帳務科目餘額處理(VD入帳)*/
public class GenE030 extends AccessDAO {

	public static final boolean DEBUG_MODE = false;

	private final String PROGNAME = "每日帳務科目餘額處理(VD入帳)  112/09/12  V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String prgmId = "GenE030";
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

			selectDbaJrnl();
			
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

	void selectDbaJrnl() throws Exception {

		sqlCmd = "select '901' as curr_code, ";
		sqlCmd += " ('VD-'||acct_code) as acct_code, dr_cr, ";
		sqlCmd += " sum(transaction_amt) as sum_txn_amt ";
		sqlCmd += "from dba_jrnl ";
		sqlCmd += "where acct_date = ?  ";
		sqlCmd += " and tran_class in ('B','A','D') ";
		sqlCmd += " and acct_code <> 'OP' ";           //排除溢付款
		sqlCmd += " group by acct_code,dr_cr ";
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLog();
		}
		closeCursor(cursorIndex);
	}
	
	/***********************************************************************/
	void updateGenPostLog() throws Exception {
		daoTable = "gen_post_log";
		updateSQL = "this_vouch_dr_amt = this_vouch_dr_amt + ? , mod_pgm = 'GenE030', ";
		updateSQL += "this_vouch_cr_amt = this_vouch_cr_amt + ? ";
		whereStr = "where curr_code = ?  ";
		whereStr += "and vouch_date = ?  ";
		whereStr += "and ac_no = ? ";
		if ("901".equals(getValue("curr_code"))) {
			if ("C".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_txn_amt"));
				setDouble(2, 0);
			} else {
				setDouble(1, 0);
				setDouble(2, getValueDouble("sum_txn_amt"));
			}
		} 
		setString(3, getValue("curr_code"));
		setString(4, hBusiVouchDate);
		setString(5, getValue("acct_code"));
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log ERROR " + hBusiVouchDate + "," + getValue("curr_code") + ","
					+ getValue("acct_code"));
		}

	}

	/***********************************************************************/
	void resetGenPostLog() throws Exception {
		daoTable   = "gen_post_log";
		updateSQL  = "this_vouch_dr_amt = 0 , this_vouch_cr_amt = 0 , ";
		updateSQL += "adj_vouch_dr_amt = 0 , adj_vouch_cr_amt = 0 ";
		whereStr   = "where vouch_date = ?  ";
		whereStr  += "and length(ac_no) > 2 ";

		setString(1, hBusiVouchDate);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log (RESET) ERROR: " + hBusiVouchDate );
		}

	}
	
	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		GenE030 proc = new GenE030();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
