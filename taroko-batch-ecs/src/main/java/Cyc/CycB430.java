/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/07/04  V1.00.01  JeffKung   Initial program                            *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycB430 extends AccessDAO {
	private String progname = "免年費-頂級客群處理程式  112/07/04 V1.00.01";
	CommFunction comm = new CommFunction();
	CommRoutine comr = null;
	CommBonus comb = null;

	String hBusiBusinessDate = "";
	String hWdayStmtCycle = "";
	String hWdayThisAcctMonth = "";
	String hWdayLastAcctMonth = "";
	String hCfeeReasonCode = "";

	long totalCnt = 0, updateCnt = 0;

// ************************************************************************
	public static void main(String[] args) throws Exception {
		CycB430 proc = new CycB430();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
				return (0);
			}

			if (args.length > 1) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [business_date]");
				return (1);
			}

			if (args.length == 1) {
				hBusiBusinessDate = args[0];
			}

			if (!connectDataBase())
				exitProgram(1);

			comr = new CommRoutine(getDBconnect(), getDBalias());
			comb = new CommBonus(getDBconnect(), getDBalias());

			selectPtrBusinday();

			if (selectPtrWorkday() != 0) {
				showLogMessage("I", "", "本日非關帳日次一日, 不需執行");
				return (0);
			}

			showLogMessage("I", "", "this_acct_month[" + hWdayThisAcctMonth + "]");
			showLogMessage("I", "", "處理月份: [" + comm.nextMonth(hWdayThisAcctMonth, -12) + "]["
					+ comm.nextMonth(hWdayThisAcctMonth, -1) + "]");
			showLogMessage("I", "", "=========================================");

			selectCycAfee();

			showLogMessage("I", "", "處理 [" + totalCnt + "] 筆, 更新 [" + updateCnt + "] 筆");

			finalProcess();
			return 0;
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
// ************************************************************************

	public void selectPtrBusinday() throws Exception {
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

		if (hBusiBusinessDate.length() == 0)
			hBusiBusinessDate = getValue("BUSINESS_DATE");
		
		showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "]");
	}

// ************************************************************************ 
	public int selectPtrWorkday() throws Exception {
		selectSQL = "";
		daoTable = "ptr_workday";
		whereStr = "where this_close_date = ? ";

		setString(1, comm.lastDate(hBusiBusinessDate));

		int recCnt = selectTable();

		if (notFound.equals("Y"))
			return (1);

		hWdayStmtCycle = getValue("STMT_CYCLE");
		hWdayThisAcctMonth = getValue("this_acct_month");
		hWdayLastAcctMonth = getValue("last_acct_month");

		return (0);
	}

// ************************************************************************
	public void selectCycAfee() throws Exception {
		selectSQL = "a.card_no, ";
		selectSQL += "b.id_no, ";
		selectSQL += "a.id_p_seqno, ";
		selectSQL += "a.rowid as rowid ";
		daoTable = "cyc_afee a, crd_idno b";
		whereStr = "where 1=1 ";
		whereStr += "and   a.maintain_code != 'Y' ";
		whereStr += "and   a.id_p_seqno = b.id_p_seqno ";
		whereStr += "and   a.group_code = '1622' "; // 只處理VISA無限金鑽卡
		whereStr += "and   (a.stmt_cycle = ? ";
		whereStr += "  or   a.old_stmt_cycle = ?) ";
		whereStr += "and   a.rcv_annual_fee >  0 ";

		setString(1, hWdayStmtCycle);
		setString(2, hWdayStmtCycle);

		openCursor();

		int cnt1 = 0;
		totalCnt = 0;
		while (fetchTable()) {

			totalCnt++;

			// 附卡免年費,不需要再另外讀取資料
			String dataType = selectCycAfeeAumid(getValue("id_p_seqno"));
			if (dataType.length() > 0) {
				updateCycAfee(dataType);
				continue;
			}

			dataType = selectMktPbmbatm(getValue("id_no"));
			if (dataType.length() > 0) {
				updateCycAfee(dataType);
				continue;
			}

		}
		closeCursor();
		return;
	}

// ************************************************************************
	void updateCycAfee(String dataType) throws Exception {
		dateTime();
		updateSQL = "rcv_annual_fee = 0, " 
				  + "reason_code    = ?, " 
				  + "mod_pgm        = ?, "
				  + "mod_time       = timestamp_format(?,'yyyymmddhh24miss')";
		daoTable  = "cyc_afee";
		whereStr  = "WHERE  rowid   = ? ";

		setString(1, dataType);
		setString(2, javaProgram);
		setString(3, sysDate + sysTime);
		setRowId(4, getValue("rowid"));

		updateTable();
		if (!notFound.equals("Y"))
			updateCnt++;

		return;
	}

// ************************************************************************
	String selectCycAfeeAumid(String idPSeqno) throws Exception {
		String dataType = "";
		selectSQL = "data_type,id_no ";
		daoTable = "cyc_afee_aumid";
		whereStr = "WHERE data_month = ? and id_p_seqno = ?";

		setString(1, hWdayLastAcctMonth);
		setString(2, idPSeqno);

		int n = selectTable();
		if (!notFound.equals("Y")) {
			dataType = getValue("data_type");
		}

		return dataType;
	}

	// ************************************************************************
	String selectMktPbmbatm(String idNo) throws Exception {
		String dataType = "";
		selectSQL = "nvl(sum(avg6m_amt),0) as avg6m_amt ";
		daoTable = "mkt_pbmbatm";
		whereStr = "WHERE data_month = ? and id_no = ?";

		setString(1, hWdayLastAcctMonth);
		setString(2, idNo);

		int n = selectTable();

		if (getValueDouble("avg6m_amt") >= 5000000) {
			dataType = "V2";
		}

		return dataType;
	}

// ************************************************************************

} // End of class FetchSample
