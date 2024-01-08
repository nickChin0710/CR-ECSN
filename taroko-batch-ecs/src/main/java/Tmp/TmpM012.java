package Tmp;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/08/02  V0.00.01     JeffKung  initial                                  *
*****************************************************************************/

import com.CommCrd;
import com.CommCrdRoutine;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class TmpM012 extends AccessDAO {
	private String PROGNAME = "補bil_bill的分期資訊 112/08/02 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate zzdate = new CommDate();
	private int iiFileNum = 0;

	String modUser = "";

	String hBusiBusinessDate = "";
	String allData = "";

	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();
			
			if (args.length == 1 && "ALL".equals(args[0])) {
				allData = "Y";
			}
			
			processBilContract();

			finalProcess();
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return;
		}

	}

	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += " fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}

	}
	
	/***********************************************************************/
	void processBilContract() throws Exception {

		int totalCnt = 0;

		sqlCmd  = " select ";
		sqlCmd += " card_no,mcht_chi_name,tot_amt,install_tot_term,install_curr_term, ";
		sqlCmd += " first_post_date,post_cycle_dd,purchase_date,auth_code, ";
		sqlCmd += " all_post_flag,unit_price,first_remd_amt, ";
		sqlCmd += " contract_no,contract_seq_no ";
		sqlCmd += " from bil_contract ";
		sqlCmd += " where 1=1 ";
		//sqlCmd += " and   new_proc_date <= '20230301' ";
		//sqlCmd += " fetch first 10 rows only ";  //DEBUG

		openCursor();
		while (fetchTable()) {
			totalCnt++;

			getBilBill();
			countCommit();

			if (totalCnt % 1000 == 0) {
				showLogMessage("I", "", "Process Count :  " + totalCnt);
			}

		}

		closeCursor();
		showLogMessage("I", "", "處理完畢,共: [" + totalCnt + "] 筆");

	}

	void getBilBill() throws Exception {

		extendField = "bilbill.";
		daoTable = "bil_bill";
		
		sqlCmd  = " select ";
		sqlCmd += " mcht_chi_name,dest_amt,purchase_date,post_date, ";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from  bil_bill ";
		sqlCmd += " where card_no = ? ";
		sqlCmd += " and   acct_code = 'IT' ";
		sqlCmd += " and   purchase_date = ? ";
		sqlCmd += " and   substr(mcht_chi_name,1,4) = ? ";
		if ("".equals(allData)) {
			sqlCmd += " and   contract_no = '' ";
		}
		
		setString(1, getValue("card_no"));
		setString(2, getValue("purchase_date"));
		setString(3, comc.getSubString(getValue("mcht_chi_name"),0,4));
		//setString(4, comc.getSubString(getValue("first_post_date"),6,8));
	
		int bilCnt = selectTable();
		
		if (notFound.equals("Y")) {
			//showLogMessage("I", "", String.format("bil_bill無分期資料,card_no=[%s]", getValue("card_no")));
			return;
		}
		
		int firstAmt = getValueInt("unit_price") + getValueInt("first_remd_amt");
		int unitPrice = getValueInt("unit_price");
		int installCurrTerm = 0;
		
		for (int i=0;i<bilCnt;i++) {
			
			//debug
			//showLogMessage("I", "", String.format("bil_bill分期資料,card_no=[%s]", getValue("card_no")));
			//showLogMessage("I", "", String.format("7-9 = [%s]", comc.getSubString(getValue("bilbill.mcht_chi_name",i),7,9)));
			//showLogMessage("I", "", String.format("4-7 = [%s]", comc.getSubString(getValue("bilbill.mcht_chi_name",i),4,7)));

			if ("／".equals(comc.getSubString(getValue("bilbill.mcht_chi_name",i),6,7))) {
				if (!comc.getSubString(getValue("bilbill.post_date",i),6,8).equals(comc.getSubString(getValue("first_post_date"),6,8))) {
					continue;
				} 
			}
			
			//首期金額要相等才update
			if ("０１／".equals(comc.getSubString(getValue("bilbill.mcht_chi_name",i),4,7))) {
				if ((firstAmt == getValueInt("bilbill.dest_amt",i)) == false) {
					continue;
				} else {
					updateBilBill(1,firstAmt,unitPrice,i);
				}
			} else {
				//每期金額要相等才update
				if ("／".equals(comc.getSubString(getValue("bilbill.mcht_chi_name",i),6,7)) 
					&& (unitPrice == getValueInt("bilbill.dest_amt",i)) == false ) {
					continue;
				} else {
					String term = comc.getSubString(getValue("bilbill.mcht_chi_name",i),4,6);
					switch(term) {
			 		case "０１":
			 			installCurrTerm = 1;
			 			break;
			 		case "０２":
			 			installCurrTerm = 2;
			 			break;
			 		case "０３":
			 			installCurrTerm = 3;
			 			break;
			 		case "０４":
			 			installCurrTerm = 4;
			 			break;
			 		case "０５":
			 			installCurrTerm = 5;
			 			break;
			 		case "０６":
			 			installCurrTerm = 6;
			 			break;
			 		case "０７":
			 			installCurrTerm = 7;
			 			break;
			 		case "０８":
			 			installCurrTerm = 8;
			 			break;
			 		case "０９":
			 			installCurrTerm = 9;
			 			break;
			 		case "１０":
			 			installCurrTerm = 10;
			 			break;
			 		case "１１":
			 			installCurrTerm = 11;
			 			break;
			 		case "１２":
			 			installCurrTerm = 12;
			 			break;
			 		case "１３":
			 			installCurrTerm = 13;
			 			break;
			 		case "１４":
			 			installCurrTerm = 14;
			 			break;
			 		case "１５":
			 			installCurrTerm = 15;
			 			break;
			 		case "１６":
			 			installCurrTerm = 16;
			 			break;
			 		case "１７":
			 			installCurrTerm = 17;
			 			break;
			 		case "１８":
			 			installCurrTerm = 18;
			 			break;
			 		case "１９":
			 			installCurrTerm = 19;
			 			break;
			 		case "２０":
			 			installCurrTerm = 20;
			 			break;
			 		case "２１":
			 			installCurrTerm = 21;
			 			break;
			 		case "２２":
			 			installCurrTerm = 22;
			 			break;
			 		case "２３":
			 			installCurrTerm = 23;
			 			break;
			 		case "２４":
			 			installCurrTerm = 24;
			 			break;
			 		case "２５":
			 			installCurrTerm = 25;
			 			break;
			 		case "２６":
			 			installCurrTerm = 26;
			 			break;
			 		case "２７":
			 			installCurrTerm = 27;
			 			break;
			 		case "２８":
			 			installCurrTerm = 28;
			 			break;
			 		case "２９":
			 			installCurrTerm = 29;
			 			break;
			 		case "３０":
			 			installCurrTerm = 30;
			 			break;
			 		case "３１":
			 			installCurrTerm = 31;
			 			break;
			 		case "３２":
			 			installCurrTerm = 32;
			 			break;
			 		case "３３":
			 			installCurrTerm = 33;
			 			break;
			 		case "３４":
			 			installCurrTerm = 34;
			 			break;
			 		case "３５":
			 			installCurrTerm = 35;
			 			break;
			 		case "３６":
			 			installCurrTerm = 36;
			 			break;
					}	
					updateBilBill(installCurrTerm,firstAmt,unitPrice,i);
				}
			}
		}
	}


	void updateBilBill(int installCurrTerm,int firstAmt, int unitPrice, int i) throws Exception {

		daoTable = "bil_bill";
		updateSQL  = "contract_no= ? ,";
		updateSQL += "contract_seq_no= ? ,";
		updateSQL += "contract_amt= ? ,";
		updateSQL += "install_tot_term= ? ,";
		updateSQL += "install_curr_term= ? ,";
		updateSQL += "install_tot_term1= ? ,";
		updateSQL += "install_first_amt= ? ,";
		updateSQL += "install_per_amt= ? ";
		whereStr = "where rowid = ? ";
		
		setString(1, getValue("contract_no"));
		setString(2, getValue("contract_seq_no"));
		setDouble(3, getValueDouble("tot_amt"));
		setInt(4, getValueInt("install_tot_term"));
		setInt(5, installCurrTerm);
		setInt(6, getValueInt("install_tot_term"));
		setInt(7, firstAmt);
		setInt(8, unitPrice);
		setRowId(9, getValue("bilbill.rowId",i));

		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("bil_bill異動失敗,card_no=[%s]", getValue("card_no")));
		}

	}

	public static void main(String[] args) throws Exception {
		TmpM012 proc = new TmpM012();
		proc.mainProcess(args);
		return;
	}
	// ************************************************************************

}
