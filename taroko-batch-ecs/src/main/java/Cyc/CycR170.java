/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-12-22  V1.00.00     Ryan     initial                                            *
 **************************************************************************************/

package Cyc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;


public class CycR170 extends AccessDAO {
	private final String progname = "每季更新第二段差別利率處理程式  111/12/22 V.00.00";
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
	
	/**********TABLE : CYC_DIFF_RCRATE************/
	private String hAcnoPSeqno = "";
	private String hAcctMonth = "";
	private double hRevolveIntRate = 0;
	private String hRevolveRateSMonth = "";
	private String hRevolveRateEMonth = "";
	

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
			getBusiDate();
			selectCycRcrateParm();
			if(comStr.pos(procDates.toString(),businessDate)<0) {
				showLogMessage("I", "", String.format("營業日[%s],非執行日",businessDate));
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
		sqlCmd = " select acct_month,acno_p_seqno,revolve_int_rate ,revolve_rate_s_month ,revolve_rate_e_month ";
		sqlCmd += " from cyc_diff_rcrate ";
		sqlCmd += " where acct_month = ? and diff_step = '10' and is_conform = 'Y' ";
		setString(1,commDate.monthAdd(businessDate, 4));
		this.openCursor();

		while (fetchTable()) {
			readCnt++;
			initData();
			hAcnoPSeqno = getValue("acno_p_seqno");
			hAcctMonth = getValue("acct_month");
			hRevolveIntRate = getValueDouble("revolve_int_rate");
			hRevolveRateSMonth = getValue("revolve_rate_s_month");
			hRevolveRateEMonth = getValue("revolve_rate_e_month");
			procData();
			commitDataBase();
		}
		this.closeCursor();
	}
	
	void updateActAcno() throws Exception {
		try {
			int i = 1;
			daoTable = "act_acno";
			updateSQL = " revolve_reason_2 = ? ";
			updateSQL += ",revolve_int_sign_2 = ? ";
			updateSQL += ",revolve_int_rate_2 = ? ";
			updateSQL += ",revolve_rate_s_month_2 = ? ";
			updateSQL += ",revolve_rate_e_month_2 = ? ";
			updateSQL += ",mod_time = sysdate ";
			updateSQL += ",mod_pgm = ? ";
			whereStr = " where acno_p_seqno = ? ";
			setString(i++, "J");
			setString(i++, "-");
			setDouble(i++, hRevolveIntRate);
			setString(i++, hRevolveRateSMonth);
			setString(i++, hRevolveRateEMonth);
			setString(i++, modPgm);
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
	}
	
	private void updateCycDiffRcrate(){
		if(skipCntFlag.equals("Y"))
			return;
		try {
			int i = 1;
			daoTable = "cyc_diff_rcrate";
			updateSQL = " diff_step = ? ";
			updateSQL += ",mod_time = sysdate ";
			updateSQL += ",mod_pgm = ? ";
			whereStr = " where acno_p_seqno = ? and acct_month = ? ";
			setString(i++, "20");
			setString(i++, modPgm);
			setString(i++, hAcnoPSeqno);
			setString(i++, hAcctMonth);
			updateTable();
			if (notFound.equals("Y")) {
				showLogMessage("E", "", "update cyc_diff_rcrate not fund ");
				return;
			}
		} catch (Exception ex) {
			showLogMessage("E", "", "update cyc_diff_rcrate error ," + ex.getMessage());
			return;
		}
		totalCnt++;
	}
	
	//利率差異化明細檔cycm0200
	private void selectCycRcrateParm() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select run_month,run_day3,penalty_month,penalty_month2,use_rcmonth from cyc_rcrate_parm where 1=1 ";
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select cyc_rcrate_parm not found!");
			skipCntFlag = "Y";
			return;
		}

		hRunMonth = getValue("run_month");
		hRunDay = getValueInt("run_day3");

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
	
	
	private void procData() throws Exception {
		updateActAcno();
		updateCycDiffRcrate();
		if ((readCnt % 100000) == 0) {
			showLogMessage("I", "","  Read w/ ROW " + readCnt);
		}
	}
	
	/***********************************************************************/
	public void initData() {
		skipCntFlag = "";
		hAcnoPSeqno = "";
		hAcctMonth = "";
		hRevolveIntRate = 0;
		hRevolveRateSMonth = "";
		hRevolveRateEMonth = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CycR170 proc = new CycR170();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
