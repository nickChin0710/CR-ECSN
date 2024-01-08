/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/11/15  V1.00.03  Allen Ho   cyc_a000 PROD compare OK                   *
* 109/07/10  V1.00.04  yanghan    修改了變量名稱和方法名稱         					 *
* 109/11/09  V1.00.05  Alex       非關帳日執行正常結束							         *
* 109-12-17  V1.00.06  tanwei     updated for project coding standard        *
* 112-07-19  V1.00.07  Simon      新增 更新ptr_businday.this_close_date      *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA020 extends AccessDAO
{
	private String progname = "關帳-關帳探頭(變更關帳日)處理程式 112/07/19 V1.00.07";
	CommFunction comm = new CommFunction();
	CommRoutine comr = null;

	String hBusiBusinessDate = "";
	String hWdayNextAcctMonth = "";
	String hWdayNextCloseDate = "";
	String hWdayNextInterestDate = "";
	String hWdayLastpayStand = "";
	int hWdayLastpayNextNDays = 0;
	String hWdayRowid = "";
	String hBusidayRowid = "";
	String hWdayNextLastpayDate = "";
	String hWdayNextDelaypayDate = "";
	String hWdayNextBillingDate = "";

	int checkCnt = 0, updateCnt = 0;
	long totalCnt = 0;

// ************************************************************************
	public static void main(String[] args) throws Exception {
		CycA020 proc = new CycA020();
		proc.mainProcess(args);
		return;
	}

// ************************************************************************
	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (args.length > 1) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [business_date]");
				exitProgram(1);
			}

			if (args.length == 1) {
				hBusiBusinessDate = args[0];
			}

			if (!connectDataBase())
				exitProgram(1);

			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			if (selectPtrWorkday1() != 0) {
				showLogMessage("I", "", "本日非關帳日, 不需執行");				
				finalProcess();
//				exitProgram(0);
				return ;
			}

			String tmpStr = "";
			if (hWdayLastpayNextNDays > 0) {
				comr.increaseDays(hWdayNextLastpayDate, 1);
				if (comr.increaseNewDate.compareTo(hWdayNextDelaypayDate) > 0)
					hWdayNextDelaypayDate = comr.increaseNewDate;
			} else {
				comr.increaseDays(hWdayNextLastpayDate, -1);
				tmpStr = comr.increaseNewDate;
				comr.increaseDays(tmpStr, 1);
				if (comr.increaseNewDate.compareTo(hWdayNextDelaypayDate) > 0)
					hWdayNextDelaypayDate = comr.increaseNewDate;
			}

			updatePtrWorkday();
/*** 新增更新 ptr_businday.this_close_date start ***/
      updatePtrBusinday();
/*** 新增更新 ptr_businday.this_close_date end   ***/

			selectPtrWorkday();

			finalProcess();
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return;
		}

	} // End of mainProcess
// ************************************************************************

	public void selectPtrBusinday() throws Exception {

    selectSQL = "business_date, "
              + "rowid as rowid";
		daoTable  = "PTR_BUSINDAY";
		whereStr  = "FETCH FIRST 1 ROW ONLY";

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		} else {
			hBusidayRowid = getValue("rowid");
		}

		if (hBusiBusinessDate.length() == 0)
			hBusiBusinessDate = getValue("BUSINESS_DATE");
		showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "]");
	}

// ************************************************************************
	public int selectPtrWorkday1() throws Exception {
		selectSQL = "case when close_stand < stmt_cycle "
				+ "     then to_char(add_months(to_date(next_acct_month,'yyyymm'),2),'yyyymm') "
				+ "     else to_char(add_months(to_date(next_acct_month,'yyyymm'),1),'yyyymm') "
				+ "end AS NEXT_ACCT_MONTH0," + "case when interest_stand > close_stand "
				+ "     then to_char(add_months(to_date(next_acct_month,'yyyymm'),2),'yyyymm') "
				+ "     else to_char(add_months(to_date(next_acct_month,'yyyymm'),1),'yyyymm') "
				+ "end AS NEXT_ACCT_MONTH1," + "case when lastpay_stand < close_stand "
				+ "     then to_char(add_months(to_date(next_acct_month,'yyyymm'),2),'yyyymm') "
				+ "     else to_char(add_months(to_date(next_acct_month,'yyyymm'),1),'yyyymm') "
				+ "end AS NEXT_ACCT_MONTH2," + "close_stand," + "interest_stand," + "lastpay_stand,"
				+ "lastpay_next_n_days," + "ROWID as ROWID";
		daoTable = "PTR_WORKDAY";
		whereStr = "WHERE NEXT_CLOSE_DATE = ? ";

		setString(1, hBusiBusinessDate);

		int recCnt = selectTable();

		if (notFound.equals("Y"))
			return (1);

		String nextMonth = "";
		hWdayNextAcctMonth = getValue("NEXT_ACCT_MONTH0");
		hWdayNextCloseDate = hWdayNextAcctMonth + getValue("CLOSE_STAND");
		hWdayNextBillingDate = hWdayNextCloseDate;
		hWdayNextInterestDate = getValue("NEXT_ACCT_MONTH1") + getValue("INTEREST_STAND");
		nextMonth = getValue("NEXT_ACCT_MONTH2");
		hWdayLastpayStand = getValue("LASTPAY_STAND");
		hWdayLastpayNextNDays = getValueInt("LASTPAY_NEXT_N_DAYS");
		hWdayRowid = getValue("ROWID");

		if (hWdayLastpayNextNDays < 0)
			hWdayLastpayNextNDays = 0;

		String lastDateStr = comm.lastdateOfmonth(nextMonth + "01").substring(6, 8);

		if (lastDateStr.compareTo(hWdayLastpayStand) < 0)
			hWdayNextLastpayDate = nextMonth + lastDateStr;
		else
			hWdayNextLastpayDate = nextMonth + hWdayLastpayStand;

		hWdayNextDelaypayDate = comm.nextNDate(hWdayNextLastpayDate, hWdayLastpayNextNDays);
		return (0);
	}

// ************************************************************************
	public void updatePtrWorkday() throws Exception {
		updateSQL = "ll_acct_month          = last_acct_month," + "last_acct_month        = this_acct_month,"
				+ "this_acct_month        = next_acct_month," + "next_acct_month        = ?,"
				+ "ll_close_date          = last_close_date," + "last_close_date        = this_close_date,"
				+ "this_close_date        = next_close_date," + "next_close_date        = ?,"
				+ "ll_billing_date        = last_billing_date," + "last_billing_date      = this_billing_date,"
				+ "this_billing_date      = next_billing_date," + " next_billing_date     = ?,"
				+ "ll_interest_date       = last_interest_date," + "last_interest_date     = this_interest_date,"
				+ "this_interest_date     = next_interest_date," + "next_interest_date     = ?,"
				+ "ll_lastpay_date        = last_lastpay_date," + "last_lastpay_date      = this_lastpay_date,"
				+ "this_lastpay_date      = next_lastpay_date," + "next_lastpay_date      = ?,"
				+ "ll_delaypay_date       = last_delaypay_date," + "last_delaypay_date     = this_delaypay_date,"
				+ "this_delaypay_date     = next_delaypay_date," + "next_delaypay_date     = ?,"
				+ "ll_1st_del_notice_date = l_1st_del_notice_date," + "l_1st_del_notice_date  = t_1st_del_notice_date,"
				+ "t_1st_del_notice_date  = n_1st_del_notice_date," + "n_1st_del_notice_date  = '',"
				+ "ll_2st_del_notice_date = l_2st_del_notice_date," + "l_2st_del_notice_date  = t_2st_del_notice_date,"
				+ "t_2st_del_notice_date  = n_2st_del_notice_date," + "n_2st_del_notice_date  = '',"
				+ "ll_3th_del_notice_date = l_3th_del_notice_date," + "l_3th_del_notice_date  = t_3th_del_notice_date,"
				+ "t_3th_del_notice_date  = n_3th_del_notice_date," + "n_3th_del_notice_date  = ''";
		daoTable = "PTR_WORKDAY";
		whereStr = "WHERE ROWID = ? ";

		showLogMessage("I", "", "rowid=[" + hWdayRowid + "]");
		setString(1, hWdayNextAcctMonth);
		setString(2, hWdayNextCloseDate);
		setString(3, hWdayNextBillingDate);
		setString(4, hWdayNextInterestDate);
		setString(5, hWdayNextLastpayDate);
		setString(6, hWdayNextDelaypayDate);
		setRowId(7, hWdayRowid);

		int recCnt = updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "update_ptr_workday error!");
			showLogMessage("I", "", "rowid=[" + hWdayRowid + "]");
			exitProgram(1);
		}
		return;
	}

//************************************************************************
  void updatePtrBusinday() throws Exception
  {
    updateSQL = "this_close_date  = ?,"
              + "mod_pgm          = ?,"
              + "mod_time         = timestamp_format(?,'yyyymmddhh24miss')";
    daoTable  = "ptr_businday";
    whereStr  = "WHERE ROWID = ? ";
  
    setString(1 , hBusiBusinessDate);
    setString(2 , javaProgram);
    setString(3 , sysDate+sysTime);
    setRowId(4  , hBusidayRowid);
  
    int recCnt = updateTable();
  
    if ( notFound.equals("Y") )
       {
        showLogMessage("I","","update_ptr_businday error!" );
        showLogMessage("I","","rowid=["+getValue("rowid")+"]");
        exitProgram(1);
       }
    return;
  }
//************************************************************************
	public void selectPtrWorkday() throws Exception {
		selectSQL = "";
		daoTable = "PTR_WORKDAY";
		whereStr = "WHERE ROWID = ? ";

		setRowId(1, hWdayRowid);

		int recCnt = selectTable();

		showLogMessage("I", "", "             本次     下次     上次    上上次 ");
		showLogMessage("I", "", "----------------------------------------------");
		showLogMessage("I", "",
				"關帳月  " + String.format("    %6s", getValue("this_acct_month"))
						+ String.format("   %6s", getValue("next_acct_month"))
						+ String.format("   %6s", getValue("last_acct_month"))
						+ String.format("   %6s", getValue("ll_acct_month")));
		showLogMessage("I", "",
				"關帳日  " + String.format("   %8.8s", getValue("this_close_date"))
						+ String.format(" %8.8s", getValue("next_close_date"))
						+ String.format(" %8.8s", getValue("last_close_date"))
						+ String.format(" %8.8s", getValue("ll_close_date")));
		showLogMessage("I", "",
				"入帳日  " + String.format("   %8.8s", getValue("this_billing_date"))
						+ String.format(" %8.8s", getValue("next_billing_date"))
						+ String.format(" %8.8s", getValue("last_billing_date"))
						+ String.format(" %8.8s", getValue("ll_billing_date")));
		showLogMessage("I", "",
				"利息起算日" + String.format(" %8.8s", getValue("this_interest_date"))
						+ String.format(" %8.8s", getValue("next_interest_date"))
						+ String.format(" %8.8s", getValue("last_interest_date"))
						+ String.format(" %8.8s", getValue("ll_interest_date")));
		showLogMessage("I", "",
				"繳款截止日" + String.format(" %8.8s", getValue("this_lastpay_date"))
						+ String.format(" %8.8s", getValue("next_lastpay_date"))
						+ String.format(" %8.8s", getValue("last_lastpay_date"))
						+ String.format(" %8.8s", getValue("ll_lastpay_date")));
		showLogMessage("I", "",
				"寬延期限日" + String.format(" %8.8s", getValue("this_delaypay_date"))
						+ String.format(" %8.8s", getValue("next_delaypay_date"))
						+ String.format(" %8.8s", getValue("last_delaypay_date"))
						+ String.format(" %8.8s", getValue("ll_delaypay_date")));
		showLogMessage("I", "", "----------------------------------------------");
		showLogMessage("I", "", "作業日已更改完成");
	}
// ************************************************************************

} // End of class FetchSample
