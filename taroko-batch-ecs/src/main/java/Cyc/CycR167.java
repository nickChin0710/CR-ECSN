/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-12-22  V1.00.00     Ryan     initial                                            *
* 112-06-27  V1.00.01     Ryan     add update act_acno.rcrate_year                    *
* 112-10-31  V1.00.02     Ryan     add update PREV_INT_SIGN,PREV_INT_RATE,PREV_RATE_S_MONTH,PREV_RATE_E_MONTH                    *
 **************************************************************************************/

package Cyc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;


public class CycR167 extends AccessDAO {
	private final String progname = "每季差別利率第二段移至第一段處理程式  112/10/31 V.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	CommString comStr = new CommString();
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalCnt = 0;
	int readCnt = 0;
	StringBuffer  procDates  = new StringBuffer();

	private String hRunMonth = "";
	private int hRunDay = 0;
	private String skipCntFlag = ""; 
	private String hThisAcctMonth = "";
	
	/**********TABLE : ACT_ACNO************/
	private String hAcnoPSeqno = "";
	private double hRevolveIntRate1 = 0;
	private String hRevolveRateSMonth1 = "";
	private String hRevolveRateEMonth1 = "";
	private String hRevolveReason2 = "";
	private double hRevolveIntRate2 = 0;
	private String hRevolveRateSMonth2 = "";
	private String hRevolveRateEMonth2 = "";
	private double hRevolveIntRate = 0;
	private double hRcrateYear = 0;
	private String hBusinessDate = "";
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
			hBusinessDate = getBusiDate();
			selectCycRcrateParm();
			if(comStr.pos(procDates.toString(),hBusinessDate)<0) {
				showLogMessage("I", "", String.format("營業日[%s],非執行日",hBusinessDate));
				return(0);
			}
			selectActAcno();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,筆數 = %s", totalCnt));
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
	
	
	void selectActAcno() throws Exception {
		sqlCmd = " select a.revolve_int_rate,a.revolve_rate_s_month,a.revolve_rate_e_month ";
		sqlCmd += " ,a.acno_p_seqno ,a.revolve_reason_2 ,a.revolve_int_rate_2 ,a.revolve_int_rate ";
		sqlCmd += " ,a.revolve_rate_s_month_2 ,a.revolve_rate_e_month_2 ,b.this_acct_month ";
		sqlCmd += " from act_acno a, ptr_workday b ";
		sqlCmd += " where a.stmt_cycle = b.stmt_cycle and a.acct_status in ('1','2','3') and a.revolve_int_rate_2 > 0 ";
		sqlCmd += " and (a.revolve_rate_s_month_2 = to_char(to_date(b.this_acct_month,'yyyymm') + 1 month , 'yyyymm') or a.revolve_rate_s_month_2='') ";
		this.openCursor();

		while (fetchTable()) {
			readCnt++;
			initData();
			hAcnoPSeqno = getValue("acno_p_seqno");
			hRevolveIntRate1 = getValueDouble("revolve_int_rate");
			hRevolveRateSMonth1 = getValue("revolve_rate_s_month");
			hRevolveRateEMonth1 = getValue("revolve_rate_e_month");
			hRevolveReason2 = getValue("revolve_reason_2");
			hRevolveIntRate2 = getValueDouble("revolve_int_rate_2");
			hRevolveRateSMonth2 = getValue("revolve_rate_s_month_2");
			hRevolveRateEMonth2 = getValue("revolve_rate_e_month_2");
			hThisAcctMonth = getValue("this_acct_month");
			hRevolveIntRate = getValueDouble("revolve_int_rate");
			procData();
		}
		this.closeCursor();
	}
	
	void updateActAcno() throws Exception {
		try {
			int i = 1;
			daoTable = "act_acno";
			updateSQL = " prev_int_sign = ? ";
			updateSQL += ",prev_int_rate = ? ";
			updateSQL += ",prev_rate_s_month = ? ";
			updateSQL += ",prev_rate_e_month = ? ";
			updateSQL += ",revolve_reason = ? ";
			updateSQL += ",revolve_int_sign = ? ";
			updateSQL += ",revolve_int_rate = ? ";
			updateSQL += ",revolve_rate_s_month = ? ";
			updateSQL += ",revolve_rate_e_month = ? ";
			updateSQL += ",revolve_reason_2 = '' ";
			updateSQL += ",revolve_int_sign_2 = '' ";
			updateSQL += ",revolve_int_rate_2 = 0 ";
			updateSQL += ",revolve_rate_s_month_2 = '' ";
			updateSQL += ",revolve_rate_e_month_2 = '' ";
			updateSQL += ",mod_time = sysdate ";
			updateSQL += ",mod_pgm = ? ";
			if(hRcrateYear>0)
				updateSQL += ",rcrate_year = ? ";
			whereStr = " where acno_p_seqno = ?";
			setString(i++, "-");
			setDouble(i++, hRevolveIntRate1);
			setString(i++, hRevolveRateSMonth1);
			setString(i++, hRevolveRateEMonth1);
			setString(i++, comStr.empty(hRevolveReason2) ? "J" : hRevolveReason2);
			setString(i++, "-");
			setDouble(i++, hRevolveIntRate2);
			setString(i++,
					comStr.empty(hRevolveRateSMonth2) ? commDate.monthAdd(hThisAcctMonth, 1) : hRevolveRateSMonth2);
			setString(i++,
					comStr.empty(hRevolveRateEMonth2) ? commDate.monthAdd(hThisAcctMonth, 4) : hRevolveRateEMonth2);
			setString(i++, modPgm);
			if(hRcrateYear>0)
				setDouble(i++, hRcrateYear);
			setString(i++, hAcnoPSeqno);
			updateTable();
			if (notFound.equals("Y")) {
				showLogMessage("E", "", "update act_acno not fund ");
				skipCntFlag = "Y";
				return;
			}
		} catch (Exception ex) {
			showLogMessage("E", "", "update act_acno error ," + ex.getMessage());
			skipCntFlag = "Y";
			return;
		}
		totalCnt++;
	}
	
	//利率差異化明細檔cycm0200
	private void selectCycRcrateParm() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select run_month,run_day2,penalty_month,penalty_month2,use_rcmonth from cyc_rcrate_parm where 1=1 ";
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select cyc_rcrate_parm not found!");
			skipCntFlag = "Y";
			return;
		}

		hRunMonth = getValue("run_month");
		hRunDay = getValueInt("run_day2");

		String sysDateY = comStr.left(sysDate,4);
		for(int i = 0 ;i<12 ;i++) {
			char ch = hRunMonth.charAt(i);
			String charStr = String.valueOf(ch);
			if(charStr.equals("Y")) {
				procDates.append(",");
				procDates.append(sysDateY);
				procDates.append(String.format("%02d", i+1));
				procDates.append(String.format("%02d", hRunDay));
			}
		}
	}
	
	private double getRcrateYear() throws Exception {
		extendField = "rcrate.";
		sqlCmd = "select rcrate_year from ptr_rcrate where rcrate_day = ? fetch first 1 rows only ";
		setDouble(1,hRevolveIntRate2);
		int n = selectTable();
		if(n>0)
			return getValueDouble("rcrate.rcrate_year");
		return 0;
	}
	
	private void procData() throws Exception {
		
		if(hRevolveIntRate2 > 0 && hRevolveIntRate==hRevolveIntRate2) {
			hRcrateYear = getRcrateYear();
		}
		
		updateActAcno();
		
		if ((totalCnt % 5000) == 0) {
			commitDataBase();
		}
		if ((readCnt % 100000) == 0) {
			showLogMessage("I", "","  Read w/ ROW " + readCnt);
		}
	}
	
	/***********************************************************************/
	public void initData() {
		skipCntFlag = "";
		hAcnoPSeqno = "";
		hRevolveIntRate1 = 0;
		hRevolveRateSMonth1 = "";
		hRevolveRateEMonth1 = "";
		hRevolveReason2 = "";
		hRevolveIntRate2 = 0;
		hRevolveRateSMonth2 = "";
		hRevolveRateEMonth2 = "";
		hThisAcctMonth = "";
		hRevolveIntRate = 0;
		hRcrateYear = 0;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CycR167 proc = new CycR167();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
