/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 112-10-12  V1.00.00     Ryan     initial                                            *
 **************************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;


public class ColF040 extends AccessDAO {
	private final String progname = "中斷時效日期到期日狀態處理程式 112/10/12 V.00.00";
	private final static int COMMIT_CNT = 1000;
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommString commStr = new CommString();
	CommRoutine comr = null;
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int tolalCnt = 0;


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
			
			
			String parm1 = "";
			String hBusindayDate = "";
			if(args.length == 1) {
				if(!commDate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				parm1 = args[0];
				hBusindayDate = parm1;
			}
			showLogMessage("I", "", String.format("輸入參數日期1 = [%s]", parm1));
			hBusindayDate = getProgDate(hBusindayDate,"D");

			selectColBadCertinfo(hBusindayDate);

			commitDataBase();

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
	
	
	void selectColBadCertinfo(String hBusindayDate) throws Exception {
		int tolCnt = 0;
		sqlCmd = " select id_corp_p_seqno,cert_end_date from col_bad_certinfo where cert_status in ('0','1') and cert_end_date < ? ";
		setString(1,hBusindayDate);
		this.openCursor();
		while (fetchTable()) {
			String idCorpPSeqno = getValue("id_corp_p_seqno");
			String certEndDate = getValue("cert_end_date");
			if(commStr.empty(certEndDate)) {
				showLogMessage("I", "", String.format("cert_end_date is empty = [%s]", idCorpPSeqno));
				continue;
			}
			int result = updateColBadCertinfo(idCorpPSeqno);
			if(result > 0)
				tolCnt ++;
			if(tolCnt % COMMIT_CNT == 0) {
				showLogMessage("I", "", String.format("已處理%d筆資料", tolCnt));
				commitDataBase();
			}
		}
		showLogMessage("I", "", String.format("憑證已到期，更新筆數:%d筆", tolCnt));
		this.closeCursor();
	}

	int updateColBadCertinfo(String idCorpPSeqno) throws Exception {
		daoTable = "col_bad_certinfo";
		updateSQL = "cert_status = ?, ";
		updateSQL += " mod_time = sysdate, ";
		updateSQL += " mod_user = ?, ";
		updateSQL += " mod_pgm = ?, ";
		updateSQL += " mod_seqno = mod_seqno + 1 ";
		whereStr = "where id_corp_p_seqno = ? ";
		setString(1, "3");
		setString(2, modPgm);
		setString(3, modPgm);
		setString(4, idCorpPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("updateColBadCertinfo notFound id_corp_p_seqno = [%s]", idCorpPSeqno));
			return 0;
		}
		return 1;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ColF040 proc = new ColF040();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
