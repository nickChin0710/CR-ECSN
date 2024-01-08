/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-10-07  V1.00.00     Ryan     initial                                            *
 **************************************************************************************/

package Tmp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;


public class TmpC004 extends AccessDAO {
	private final String progname = "票證效期日更正程式 111/10/07 V.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommRoutine comr = null;
	CommString commString = new CommString();
	String modUser = "";
	private int totalCnt = 0;
	private String modPgm = "";
	private String hElectronicType = "";
	private String hNewEndDate = "";
	private String hElectronicCardNo = "";

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
			modPgm = this.getClass().getSimpleName();
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			selectCcaOpposition();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,處理筆數 = %s", totalCnt));
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
	
	
	void selectCcaOpposition() throws Exception {

		sqlCmd = " SELECT 1 AS ELECTRONIC_TYPE, " + 
				"TSC_CARD_NO AS ELECTRONIC_CARD_NO, " + 
				"NEW_END_DATE  " + 
				"FROM TSC_CARD " + 
				"WHERE ((SUBSTRING(NEW_END_DATE,5,2) IN ('01','03','05','07','08','10','12') " + 
				"AND SUBSTRING(NEW_END_DATE,7,2) <> '31'))  " + 
				"OR (SUBSTRING(NEW_END_DATE,5,2) = '02'AND SUBSTRING(NEW_END_DATE,7,2) NOT IN  " + 
				"('28','29')) " + 
				"UNION " + 
				"SELECT 2 AS ELECTRONIC_TYPE, " + 
				"IPS_CARD_NO AS ELECTRONIC_CARD_NO, " + 
				"NEW_END_DATE  " + 
				"FROM IPS_CARD " + 
				"WHERE ((SUBSTRING(NEW_END_DATE,5,2) IN ('01','03','05','07','08','10','12') " + 
				"AND SUBSTRING(NEW_END_DATE,7,2) <> '31'))  " + 
				"OR (SUBSTRING(NEW_END_DATE,5,2) = '02'AND SUBSTRING(NEW_END_DATE,7,2) NOT IN  " + 
				"('28','29')) " + 
				"UNION " + 
				"SELECT 3 AS ELECTRONIC_TYPE, " + 
				"ICH_CARD_NO AS ELECTRONIC_CARD_NO, " + 
				"NEW_END_DATE  " + 
				"FROM ICH_CARD " + 
				"WHERE ((SUBSTRING(NEW_END_DATE,5,2) IN ('01','03','05','07','08','10','12') " + 
				"AND SUBSTRING(NEW_END_DATE,7,2) <> '31'))  " + 
				"OR (SUBSTRING(NEW_END_DATE,5,2) = '02'AND SUBSTRING(NEW_END_DATE,7,2) NOT IN  " + 
				"('28','29')) " ;
				
		
		this.openCursor();

		while (fetchTable()) {
			initData();
			hElectronicType = getValue("ELECTRONIC_TYPE");
			hNewEndDate = lastdateOfmonth(getValue("NEW_END_DATE"));
			hElectronicCardNo = getValue("ELECTRONIC_CARD_NO");
			
			if(commString.empty(hNewEndDate)) {
				showLogMessage("E", "", String.format("NEW_END_DATE格式有誤,  ELECTRONIC_CARD_NO= [%s]", hElectronicCardNo));
				continue;
			}
			
			boolean updateResult = false;
			switch(hElectronicType) {
			case "1":
				updateResult = updateTscCard();
				break;
			case "2":
				updateResult = updateIpsCard();
				break;
			case "3":
				updateResult = updateIchCard();
				break;
			}
			if(!updateResult)
				continue;
			totalCnt ++;
			commitDataBase();
			if ((totalCnt % 5000) == 0) {
				showLogMessage("I", "", String.format("UPDATE ROW = [%s]", totalCnt));
			}
		}
		this.closeCursor();
	}
	
	
	/***********************************************************************/
	boolean updateTscCard() throws Exception {
		daoTable = "TSC_CARD";
		updateSQL = " new_end_date = ? ";
		updateSQL += ", mod_user = ? ";
		updateSQL += ", mod_time = sysdate ";
		updateSQL += ", mod_pgm = ? ";
		whereStr = " where tsc_card_no = ? ";
		setString(1, hNewEndDate);
		setString(2, modPgm);
		setString(3, modPgm);
		setString(4, hElectronicCardNo);

		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("updateTscCard not found ,tsc_card_no = [%s]", hElectronicCardNo));
			return false;
		}
		return true;
	}
	
	
	/***********************************************************************/
	boolean updateIpsCard() throws Exception {
		daoTable = "IPS_CARD";
		updateSQL = " new_end_date = ? ";
		updateSQL += ", mod_user = ? ";
		updateSQL += ", mod_time = sysdate ";
		updateSQL += ", mod_pgm = ? ";
		whereStr = " where ips_card_no = ? ";
		setString(1, hNewEndDate);
		setString(2, modPgm);
		setString(3, modPgm);
		setString(4, hElectronicCardNo);

		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("updateIpsCard not found ,ips_card_no = [%s]", hElectronicCardNo));
			return false;
		}
		return true;
	}
	
	
	/***********************************************************************/
	boolean updateIchCard() throws Exception {
		daoTable = "ICH_CARD";
		updateSQL = " new_end_date = ? ";
		updateSQL += ", mod_user = ? ";
		updateSQL += ", mod_time = sysdate ";
		updateSQL += ", mod_pgm = ? ";
		whereStr = " where ich_card_no = ? ";
		setString(1, hNewEndDate);
		setString(2, modPgm);
		setString(3, modPgm);
		setString(4, hElectronicCardNo);

		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("updateIchCard not found ,ich_card_no = [%s]", hElectronicCardNo));
			return false;
		}
		return true;
	}
	
	/***********************************************************************/
	String lastdateOfmonth(String date) {
		if (date.length() == 6)
			date = date + "01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		try {
			LocalDate parsedDate = LocalDate.parse(date, formatter);
			LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
			return lastDay.format(formatter);
		}catch(Exception ex) {
			return "";
		}
	}
	
	/***********************************************************************/
	public void initData() {
		hElectronicType = "";
		hNewEndDate = "";
		hElectronicCardNo = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TmpC004 proc = new TmpC004();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
