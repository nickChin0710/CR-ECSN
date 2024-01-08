/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-12-09  V1.00.00     Ryan     initial                                            *
* 112-05-23  V1.00.01     Ryan     修正系統日減一個月                                                                                                           *
* 112-06-30  V1.00.02     Wilson   update hce_apply_data條件增加wallet_id                *
 **************************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;


public class CrdD068 extends AccessDAO {
	private final String progname = "產生HCE續卡資料程式 112/06/30 V.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalCnt = 0;
	private String hCardNo = "";
	private String hWalletId = "";
	private String hVCardNo = "";
	private String hIdPSeqno = "";
	private String hAcnoPSeqno = "";
	private String hNewEndDate = "";
	private String hSysDate = "";
	

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
			
			hSysDate = getBeforeSysDate();
			if(args.length == 1) {
				if(!commDate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				hSysDate = args[0];
				showLogMessage("I", "", String.format("有輸入參數日期 = [%s]", hSysDate));
			}else {
				showLogMessage("I", "", String.format("系統日前一個月 = [%s]", hSysDate));
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			selectCrdCard();
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
	
	
	void selectCrdCard() throws Exception {

		sqlCmd = " select b.card_no ";
		sqlCmd += " ,b.wallet_id ";
		sqlCmd += " ,b.v_card_no ";
		sqlCmd += " ,a.id_p_seqno ";
		sqlCmd += " ,a.acno_p_seqno ";
		sqlCmd += " ,a.new_end_date ";
		sqlCmd += " from crd_card a,hce_card b ";
		sqlCmd += " where a.card_no = b.card_no and a.old_end_date = b.new_end_date and b.status_code = '0' and a.change_date = ? ";
		setString(1,hSysDate);
		this.openCursor();

		while (fetchTable()) {
			initData();
			hCardNo = getValue("card_no");
			hWalletId = getValue("wallet_id");
			hVCardNo = getValue("v_card_no");
			hIdPSeqno = getValue("id_p_seqno");
			hAcnoPSeqno = getValue("acno_p_seqno");
			hNewEndDate = getValue("new_end_date");
			hSysDate = "";
	
			updateHceApplyData();
			commitDataBase();
		}
		this.closeCursor();
	}

	void updateHceApplyData() throws Exception {
		daoTable = "hce_apply_data";
		updateSQL = " idnv_type = '1' ,v_card_no = ? ,reset_pswd_date = '' ,proc_flag = 'A' ";
		updateSQL += " ,new_end_date = ? ,mod_time = sysdate ,mod_pgm = ? ";
		whereStr = " where card_no = ? and wallet_id = ? ";
		setString(1, hVCardNo);
		setString(2, hNewEndDate);
		setString(3, modPgm);
		setString(4, hCardNo);
		setString(5, hWalletId);
		updateTable();
		if (notFound.equals("Y")) {
			daoTable = "hce_apply_data";
			setValue("card_no", hCardNo);
			setValue("wallet_id", hWalletId);
			setValue("id_p_seqno", hIdPSeqno);
			setValue("acno_p_seqno", hAcnoPSeqno);
			setValue("v_card_no", hVCardNo);
			setValue("new_end_date", hNewEndDate);
			setValue("idnv_type", "1");
			setValue("new_apply_date", sysDate);
			setValue("send_opt_flag", "N");
			setValue("proc_flag", "A");
			setValue("crt_date", sysDate);
			setValue("crt_time", sysTime);
			setValue("mod_time", modTime);
			setValue("mod_pgm", modPgm);
			try {
				insertTable();
			} catch (Exception ex) {
				showLogMessage("E", "", "insert hce_apply_data error");
				return;
			}
		}
		totalCnt ++;
	}
	
	private String getBeforeSysDate() throws Exception {
		sqlCmd = "select to_char(to_date(to_char(sysdate,'yyyymmdd'),'yyyymmdd')-1 months, 'yyyymmdd') as before_sysdate from DUAL";
		selectTable();
		return getValue("before_sysdate");
	}
	
	
	/***********************************************************************/
	public void initData() {
		hCardNo = "";
		hWalletId = "";
		hVCardNo = "";
		hIdPSeqno = "";
		hAcnoPSeqno = "";
		hNewEndDate = "";
		hSysDate = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdD068 proc = new CrdD068();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
