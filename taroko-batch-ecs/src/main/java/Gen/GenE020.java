/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/09/12  V1.00.00    JeffKung  program initial                           *
 *  112/11/12  V1.00.01    JeffKung  將調整類交易分開處理
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

/*每日帳務科目餘額處理(入帳)*/
public class GenE020 extends AccessDAO {

	public static final boolean DEBUG_MODE = false;

	private final String PROGNAME = "每日帳務科目餘額處理(入帳)  112/11/12  V1.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String prgmId = "GenE020";
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

			selectActJrnl();
			
			selectActJrnlPayment();
			
			selectActJrnlPayItem();
			
			selectActJrnlPaymentReversal();
			
			selectActJrnlOPAdj();
			
			selectActJrnlNormalAdj();
			
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

	void selectActJrnl() throws Exception {

		sqlCmd = "select decode(curr_code,'','901',curr_code) as curr_code, ";
		sqlCmd += " acct_code, dr_cr, ";
		sqlCmd += " sum(transaction_amt) as sum_txn_amt, ";
		sqlCmd += " sum(dc_transaction_amt) as sum_dc_txn_amt ";
		sqlCmd += "from act_jrnl ";
		sqlCmd += "where acct_date = ?  ";
		sqlCmd += " and tran_class in ('B','D') ";
		sqlCmd += " and acct_code <> 'OP' ";           //排除溢付款
		sqlCmd += " group by decode(curr_code,'','901',curr_code),acct_code,dr_cr ";
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLog();
		}
		closeCursor(cursorIndex);
	}
	
	/*繳款+溢付款沖銷+退貨沖銷  --> 入到OP的本日新增加項(DR) */
	void selectActJrnlPayment() throws Exception {

		sqlCmd = "select decode(curr_code,'','901',curr_code) as curr_code, ";
		sqlCmd += " 'OP' as acct_code, 'D' as dr_cr, ";
		sqlCmd += " sum(transaction_amt) as sum_txn_amt, ";
		sqlCmd += " sum(dc_transaction_amt) as sum_dc_txn_amt ";
		sqlCmd += "from act_jrnl ";
		sqlCmd += "where acct_date = ?  ";
		sqlCmd += " and  tran_class = 'P' ";
		sqlCmd += " group by decode(curr_code,'','901',curr_code) ";
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLogOPPay();
		}
		closeCursor(cursorIndex);
	}
	
	/*繳款/溢繳/退貨沖銷科目金額加總  --> 入到OP的本日新增減項(CR) */
	void selectActJrnlPayItem() throws Exception {

		sqlCmd = "select decode(curr_code,'','901',curr_code) as curr_code, ";
		sqlCmd += " 'OP' as acct_code, 'C' as dr_cr, ";
		sqlCmd += " sum(transaction_amt) as sum_txn_amt, ";
		sqlCmd += " sum(dc_transaction_amt) as sum_dc_txn_amt ";
		sqlCmd += "from act_jrnl ";
		sqlCmd += "where acct_date = ?  ";
		sqlCmd += " and  tran_class = 'D' ";
		sqlCmd += " group by decode(curr_code,'','901',curr_code) ";
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLogOPPay();
		}
		closeCursor(cursorIndex);
	}
	
	/*繳款reversal  --> 入到OP的本日調整加項 */
	void selectActJrnlPaymentReversal() throws Exception {

		sqlCmd = "select decode(curr_code,'','901',curr_code) as curr_code, ";
		sqlCmd += " 'OP' as acct_code, 'D' as dr_cr, ";
		sqlCmd += " sum(transaction_amt) as sum_txn_amt, ";
		sqlCmd += " sum(dc_transaction_amt) as sum_dc_txn_amt ";
		sqlCmd += "from act_jrnl ";
		sqlCmd += "where acct_date = ?  ";
		sqlCmd += " and tran_class = 'A' ";
		sqlCmd += " and tran_type  = 'DR11' ";      //payment reversal
		sqlCmd += " group by decode(curr_code,'','901',curr_code) ";
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLogOPAdj();
		}
		closeCursor(cursorIndex);
	}
	
	/*退溢繳  --> 入到OP的本日調整減項 */
	void selectActJrnlOPAdj() throws Exception {

		sqlCmd = "select decode(curr_code,'','901',curr_code) as curr_code, ";
		sqlCmd += " 'OP' as acct_code, 'C' as dr_cr, ";
		sqlCmd += " sum(transaction_amt) as sum_txn_amt, ";
		sqlCmd += " sum(dc_transaction_amt) as sum_dc_txn_amt ";
		sqlCmd += "from act_jrnl ";
		sqlCmd += "where acct_date = ?  ";
		sqlCmd += " and tran_class = 'A' ";
		sqlCmd += " and acct_code  = 'OP' ";     
		sqlCmd += " group by decode(curr_code,'','901',curr_code) ";
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLogOPAdj();
		}
		closeCursor(cursorIndex);
	}
	
	/*調帳交易同時退入溢付款  --> 入到本日調整減項 */
	void selectActJrnlNormalAdj() throws Exception {

		sqlCmd = "select decode(a.curr_code,'','901',a.curr_code) as curr_code, ";
		sqlCmd += " a.acct_code, a.dr_cr, ";
		sqlCmd += " a.transaction_amt as sum_txn_amt, ";
		sqlCmd += " a.dc_transaction_amt as sum_dc_txn_amt, ";
		sqlCmd += " nvl(b.aft_amt,0) as aft_amt , nvl(b.dc_aft_amt,0) as dc_aft_amt ";
		sqlCmd += "from act_jrnl a ";
		sqlCmd += "left join act_acaj b on a.p_seqno = b.p_seqno and a.reference_no = b.reference_no ";
		sqlCmd += "where 1=1 ";
		sqlCmd += " and a.acct_date = ? ";
		sqlCmd += " and a.tran_class = 'A' ";
		sqlCmd += " and a.acct_code  <> 'OP' ";     
		setString(1,hBusiVouchDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			updateGenPostLogNormalAdj();
		}
		closeCursor(cursorIndex);
	}

	/***********************************************************************/
	void updateGenPostLog() throws Exception {
		daoTable = "gen_post_log";
		updateSQL = "this_vouch_dr_amt = this_vouch_dr_amt + ? , mod_pgm = 'GenE020', ";
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
		} else {
			if ("C".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_dc_txn_amt"));
				setDouble(2, 0);
			} else {
				setDouble(1, 0);
				setDouble(2, getValueDouble("sum_dc_txn_amt"));
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
	void updateGenPostLogOPPay() throws Exception {
		daoTable = "gen_post_log";
		updateSQL = "this_vouch_dr_amt = this_vouch_dr_amt + ? , mod_pgm = 'GenE020', ";
		updateSQL += "this_vouch_cr_amt = this_vouch_cr_amt + ? ";
		whereStr = "where curr_code = ?  ";
		whereStr += "and vouch_date = ?  ";
		whereStr += "and ac_no = 'OP' ";
		if ("901".equals(getValue("curr_code"))) {
			if ("D".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_txn_amt"));
				setDouble(2, 0);
			} else {
				setDouble(1, 0);
				setDouble(2, getValueDouble("sum_txn_amt"));
			}
		} else {
			if ("D".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_dc_txn_amt"));
				setDouble(2, 0);
			} else {
				setDouble(1, 0);
				setDouble(2, getValueDouble("sum_dc_txn_amt"));
			}
		}
		setString(3, getValue("curr_code"));
		setString(4, hBusiVouchDate);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log (PAY) ERROR " + hBusiVouchDate + "," + getValue("curr_code") + ","
					+ "OP" );
		}

	}
	
	/***********************************************************************/
	void updateGenPostLogOPAdj() throws Exception {
		daoTable = "gen_post_log";
		updateSQL = "adj_vouch_dr_amt = adj_vouch_dr_amt + ? , mod_pgm = 'GenE020', ";
		updateSQL += "adj_vouch_cr_amt = adj_vouch_cr_amt + ? ";
		whereStr = "where curr_code = ?  ";
		whereStr += "and vouch_date = ?  ";
		whereStr += "and ac_no = 'OP' ";
		if ("901".equals(getValue("curr_code"))) {
			if ("D".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_txn_amt"));
				setDouble(2, 0);
			} else {
				setDouble(1, 0);
				setDouble(2, getValueDouble("sum_txn_amt"));
			}
		} else {
			if ("D".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_dc_txn_amt"));
				setDouble(2, 0);
			} else {
				setDouble(1, 0);
				setDouble(2, getValueDouble("sum_dc_txn_amt"));
			}
		}
		setString(3, getValue("curr_code"));
		setString(4, hBusiVouchDate);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log (ADJ) ERROR " + hBusiVouchDate + "," + getValue("curr_code") + ","
					+ "OP" );
		}

	}
	
	/************************************************************************
	 * 20231112 將調整類分開處理
	 * **********************************************************************/
	
	void updateGenPostLogNormalAdj() throws Exception {
		daoTable = "gen_post_log";
		updateSQL = "adj_vouch_dr_amt = adj_vouch_dr_amt + ? , mod_pgm = 'GenE020', ";
		updateSQL += "adj_vouch_cr_amt = adj_vouch_cr_amt + ? ";
		whereStr = "where curr_code = ?  ";
		whereStr += "and vouch_date = ?  ";
		whereStr += "and ac_no = ? ";
		if ("901".equals(getValue("curr_code"))) {
			if ("C".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_txn_amt"));
				setDouble(2, 0);
			} else {
				if (getValueDouble("aft_amt") < 0) {
					setDouble(1, Math.abs(getValueDouble("aft_amt")));
				} else {
					setDouble(1, 0);
				}
				setDouble(2, getValueDouble("sum_txn_amt"));
			}
		} else {
			if ("C".equals(getValue("dr_cr"))) {
				setDouble(1, getValueDouble("sum_dc_txn_amt"));
				setDouble(2, 0);
			} else {
				if (getValueDouble("dc_aft_amt") < 0) {
					setDouble(1, Math.abs(getValueDouble("dc_aft_amt")));
				} else {
					setDouble(1, 0);
				}
				setDouble(2, getValueDouble("sum_dc_txn_amt"));
			}
		}
		setString(3, getValue("curr_code"));
		setString(4, hBusiVouchDate);
		setString(5, getValue("acct_code"));
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log (ADJ) ERROR " + hBusiVouchDate + "," + getValue("curr_code") + ","
					+ getValue("acct_code") );
		}
		
		//20231112若有調整轉回溢付款時要多執行下面這一段
		
		if (getValueDouble("aft_amt") < 0 || getValueDouble("dc_aft_amt") < 0) {

			daoTable = "gen_post_log";
			updateSQL = "adj_vouch_dr_amt = adj_vouch_dr_amt + ? , mod_pgm = 'GenE020' ";
			whereStr = "where curr_code = ?  ";
			whereStr += "and vouch_date = ?  ";
			whereStr += "and ac_no = 'OP' ";
			if ("901".equals(getValue("curr_code"))) {
				setDouble(1, Math.abs(getValueDouble("aft_amt")));
			} else {
				setDouble(1, Math.abs(getValueDouble("dc_aft_amt")));
			}

			setString(2, getValue("curr_code"));
			setString(3, hBusiVouchDate);

			updateTable();

			if (notFound.equals("Y")) {
				showLogMessage("E", "", "update_gen_post_log (ADJ) ERROR " + hBusiVouchDate + "," + getValue("curr_code") + ","
						+ "OP" );
			}
		}
	}
	
	/***********************************************************************/
	void resetGenPostLog() throws Exception {
		daoTable   = "gen_post_log";
		updateSQL  = "this_vouch_dr_amt = 0 , this_vouch_cr_amt = 0 , ";
		updateSQL += "adj_vouch_dr_amt = 0 , adj_vouch_cr_amt = 0 ";
		whereStr   = "where vouch_date = ?  ";
		whereStr  += "and length(ac_no)= 2 ";

		setString(1, hBusiVouchDate);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update_gen_post_log (RESET) ERROR: " + hBusiVouchDate );
		}

	}
	
	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		GenE020 proc = new GenE020();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
