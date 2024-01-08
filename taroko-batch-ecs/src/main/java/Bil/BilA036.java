/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/24  V1.00.01   shiyuqi       updated for project coding standard    *
*  111/05/27  V1.00.02    JeffKung   改成opencursor()                                 *
******************************************************************************/

package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*逾期卡友來電自動分期轉歷史*/
public class BilA036 extends AccessDAO {
	private String progname = "逾期卡友來電自動分期轉歷史  111/05/27  V1.00.02 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	long hModSeqno = 0;
	String hModUser = "";
	String hModTime = "";
	String hModPgm = "";
	String hCallBatchSeqno = "";
	String iFileName = "";
	String iPostDate = "";
	String hCurpModPgm = "";
	String hCurpModTime = "";
	String hCurpModUser = "";
	long hCurpModSeqno = 0;
	String hCallRProgramCode = "";

	String hBusinessDate = "";
	int liDelMm = 0;
	String hSystemDate = "";
	String hSystemTime = "";
	String hSystemDateF = "";
	String isDeleteDate = "";
	String hAutxCardNo = "";
	String hAutxMerchantNo = "";
	String hAutxAuthorization = "";
	String hAutxPurchaseDate = "";
	double hAutxDestinationAmt = 0;
	String hAutxTxDate = "";
	String hAutxReferenceNo = "";
	String hAutxTotTerm = "";
	String hAutxErrorDesc = "";
	String hAutxAprUser1 = "";
	String hAutxCloseFlag = "";
	String hAutxRowid = "";
	String hPrintName = "";
	String hRptName = "";

	int totalCnt = 0;
	int totCnt1 = 0;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : BilA036 ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			commonRtn();

			hModPgm = javaProgram;

			selectBilAutoTx();
			showLogMessage("I", "", String.format("程式執行結束,總筆數=[%d],[%d]", totalCnt, totCnt1));

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

	/***********************************************************************/
	void commonRtn() throws Exception {
		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusinessDate = getValue("business_date");
		}
		/*-JH:R104027: 移除月數 -*/
		sqlCmd = "select wf_value6 ";
		sqlCmd += " from ptr_sys_parm  ";
		sqlCmd += "where wf_parm ='SYSPARM'  ";
		sqlCmd += " and wf_key ='BIL_A036' ";
		recordCnt = selectTable();
		if (recordCnt > 0) {
			liDelMm = getValueInt("wf_value6");
		} else {
			showLogMessage("I", "", "未設定系統參數; parm=[SYSPARM],key=[BIL_A036], 移除月數預設值為[3]個月");
			liDelMm = 3;
		}
		if (liDelMm == 0) {
			liDelMm = 3;
		}

		isDeleteDate = "";
		hSystemDateF = "";
		sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
		sqlCmd += "to_char(sysdate,'hh24miss') h_system_time,";
		sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f,";
		sqlCmd += "to_char(add_months(sysdate, ? * -1),'yyyymmdd') as is_delete_date ";
		sqlCmd += " from dual ";
		setInt(1, liDelMm);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hSystemDate = getValue("h_system_date");
			hSystemTime = getValue("h_system_time");
			hSystemDateF = getValue("h_system_date_f");
			isDeleteDate = getValue("is_delete_date");
		}

		hModSeqno = comcr.getModSeq();
		hModUser = comc.commGetUserID();
		hModTime = hSystemDate;

		showLogMessage("I", "", String.format("資料移除月數=[%d], 日期=[%s]", liDelMm, isDeleteDate));
	}

	/***********************************************************************/
	void selectBilAutoTx() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "a.card_no,";
		sqlCmd += "a.mcht_no,";
		sqlCmd += "a.authorization,";
		sqlCmd += "a.purchase_date,";
		sqlCmd += "a.dest_amt,";
		sqlCmd += "a.tx_date,";
		sqlCmd += "a.reference_no,";
		sqlCmd += "a.tot_term,";
		sqlCmd += "a.error_desc,";
		sqlCmd += "a.apr_user_1,";
		sqlCmd += "a.close_flag,";
		sqlCmd += "a.rowid  as rowid ";
		sqlCmd += "from bil_auto_tx a ";
		sqlCmd += "where 1=1 ";
		sqlCmd += "  and a.tx_date < ? ";
		sqlCmd += "order by card_no,tx_date ";
		setString(1, isDeleteDate);

		openCursor();
		while (fetchTable()) {
			hAutxCardNo = getValue("card_no");
			hAutxMerchantNo = getValue("mcht_no");
			hAutxAuthorization = getValue("authorization");
			hAutxPurchaseDate = getValue("purchase_date");
			hAutxDestinationAmt = getValueDouble("dest_amt");
			hAutxTxDate = getValue("tx_date");
			hAutxReferenceNo = getValue("reference_no");
			hAutxTotTerm = getValue("tot_term");
			hAutxErrorDesc = getValue("error_desc");
			hAutxAprUser1 = getValue("apr_user_1");
			hAutxCloseFlag = getValue("close_flag");
			hAutxRowid = getValue("rowid");

			totalCnt++;

			if (hAutxAprUser1.length() == 0) {
				hAutxErrorDesc = "主管未覆核";
			}

			if (hAutxCloseFlag.equals("Y") == false) {
				hAutxErrorDesc = "條件未通過";
			}

			if (hAutxErrorDesc.length() > 0) {
				daoTable = "bil_auto_tx";
				updateSQL = "error_desc = ?";
				whereStr = "where rowid   = ? ";
				setString(1, hAutxErrorDesc);
				setRowId(2, hAutxRowid);
				updateTable();
				if (notFound.equals("Y")) {
					showLogMessage("E", "", "update_bil_auto_tx not found! card_no,auth_code=[" + hAutxCardNo + "],["
							+ hAutxAuthorization + "]");
				}
			}

			sqlCmd = "insert into bil_auto_tx_hst ";
			sqlCmd += " select * from bil_auto_tx ";
			sqlCmd += "  where rowid = ? ";
			setRowId(1, hAutxRowid);
			insertTable();
			if (dupRecord.equals("Y")) {
				showLogMessage("E", "", "insert_bil_auto_tx_hst error! card_no,auth_code=[" + hAutxCardNo + "],["
						+ hAutxAuthorization + "]");
			}

			daoTable = "bil_auto_tx";
			whereStr = "where rowid   = ? ";
			setRowId(1, hAutxRowid);
			deleteTable();
			if (notFound.equals("Y")) {
				showLogMessage("E", "", "delete_bil_auto_tx not found!! card_no,auth_code=[" + hAutxCardNo + "],["
						+ hAutxAuthorization + "]");
			}
		}

		closeCursor();
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		BilA036 proc = new BilA036();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
