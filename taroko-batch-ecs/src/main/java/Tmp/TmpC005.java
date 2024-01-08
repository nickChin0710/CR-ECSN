/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-10-26  V1.00.00     Ryan     initial                                            *
 **************************************************************************************/

package Tmp;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

import Cca.CcaOutGoing;

import com.CommDate;


public class TmpC005 extends AccessDAO {
	private final String progname = "補寫CrdF074、CrdG008掛凍結、特指未寫入outgoing的資料處理程式 111/10/26 V.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	CcaOutGoing ccaOutGoing = null;
	String modUser = "";
	int n = 0;	
	int totalCnt = 0;
	boolean ibDebit = true;
	private String hCardNo = "";
	private String hCurrentCode = "";
	private String hBlacklistDate = "";
	private String hBlacklistReason = "";

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
			comr = new CommRoutine(getDBconnect(), getDBalias());
			ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
			
			selectCcaOpposition();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,筆數 = %s", totalCnt));
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			ccaOutGoing.finalCnt2();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	
	void selectCcaOpposition() throws Exception {

		sqlCmd = "SELECT A.CARD_NO, " + 
				"       A.CURRENT_CODE, " + 
				"       B.BLOCK_DATE AS BLACKLIST_DATE, " + 
				"       B.BLOCK_REASON1 AS BLACKLIST_REASON " + 
				"FROM CRD_CARD A, CCA_CARD_ACCT B " + 
				"WHERE A.ACNO_P_SEQNO = B.P_SEQNO " + 
				"AND A.CURRENT_CODE ='0' " + 
				"AND B.BLOCK_REASON1 <>'' " + 
				"AND (B.UNBLOCK_DATE =''OR B.UNBLOCK_DATE > TO_CHAR(SYSDATE,'YYYYMMDD')) " + 
				"AND (A.CARD_NO NOT IN (SELECT C.CARD_NO FROM CCA_OUTGOING C WHERE C.KEY_TABLE ='CARD_BASE_SPEC') "+
				"OR A.CARD_NO IN (SELECT C.CARD_NO FROM CCA_OUTGOING C WHERE C.KEY_TABLE ='CARD_BASE_SPEC'AND C.RESP_CODE <>'00')) " + 
				"UNION " + 
				"SELECT A.CARD_NO, " + 
				"       A.CURRENT_CODE, " + 
				"       B.BLOCK_DATE AS BLACKLIST_DATE, " + 
				"       B.BLOCK_REASON1 AS BLACKLIST_REASON " + 
				"FROM DBC_CARD A, CCA_CARD_ACCT B " + 
				"WHERE A.P_SEQNO = B.P_SEQNO " + 
				"AND A.CURRENT_CODE ='0' " + 
				"AND B.BLOCK_REASON1 <>'' " + 
				"AND (B.UNBLOCK_DATE =''OR B.UNBLOCK_DATE > TO_CHAR(SYSDATE,'YYYYMMDD')) " + 
				"AND (A.CARD_NO NOT IN (SELECT C.CARD_NO FROM CCA_OUTGOING C WHERE C.KEY_TABLE ='CARD_BASE_SPEC') "+
				"OR A.CARD_NO IN (SELECT C.CARD_NO FROM CCA_OUTGOING C " + 
				"WHERE C.KEY_TABLE ='CARD_BASE_SPEC'AND C.RESP_CODE <>'00') ) " + 
				"UNION " + 
				"SELECT A.CARD_NO, " + 
				"       A.CURRENT_CODE, " + 
				"       B.SPEC_DATE AS BLACKLIST_DATE, " + 
				"       B.SPEC_STATUS AS BLACKLIST_REASON " + 
				"  FROM CRD_CARD A, CCA_CARD_BASE B " + 
				"WHERE A.CARD_NO = B.CARD_NO " + 
				"AND A.CURRENT_CODE ='0' " + 
				"AND B.SPEC_STATUS <>'' " + 
				"AND (B.SPEC_DEL_DATE =''OR B.SPEC_DEL_DATE > TO_CHAR(SYSDATE,'YYYYMMDD')) " + 
				"AND (A.CARD_NO NOT IN (SELECT C.CARD_NO FROM CCA_OUTGOING C WHERE C.KEY_TABLE ='CARD_BASE_SPEC') "+
				"OR A.CARD_NO IN (SELECT C.CARD_NO FROM CCA_OUTGOING C " + 
				"WHERE C.KEY_TABLE ='CARD_BASE_SPEC'AND C.RESP_CODE <>'00') ) " + 
				"UNION " + 
				"SELECT A.CARD_NO, " + 
				"       A.CURRENT_CODE, " + 
				"       B.SPEC_DATE AS BLACKLIST_DATE, " + 
				"       B.SPEC_STATUS AS BLACKLIST_REASON " + 
				"  FROM DBC_CARD A, CCA_CARD_BASE B " + 
				"WHERE A.CARD_NO = B.CARD_NO " + 
				"AND A.CURRENT_CODE ='0' " + 
				"AND B.SPEC_STATUS <>'' " + 
				"AND (B.SPEC_DEL_DATE =''OR B.SPEC_DEL_DATE > TO_CHAR(SYSDATE,'YYYYMMDD')) " + 
				"AND (A.CARD_NO NOT IN (SELECT C.CARD_NO FROM CCA_OUTGOING C WHERE C.KEY_TABLE ='CARD_BASE_SPEC') "+
				"OR A.CARD_NO IN (SELECT C.CARD_NO FROM CCA_OUTGOING C " + 
				"WHERE C.KEY_TABLE ='CARD_BASE_SPEC'AND C.RESP_CODE <>'00') ) " ;
		
		this.openCursor();

		while (fetchTable()) {
			initData();
			hCardNo = getValue("CARD_NO");
			hCurrentCode = getValue("CURRENT_CODE");
			hBlacklistDate = getValue("BLACKLIST_DATE");
			hBlacklistReason = getValue("BLACKLIST_REASON");
			ccaOutGoing.InsertCcaOutGoingBlock(hCardNo, hCurrentCode, hBlacklistDate, hBlacklistReason);
			totalCnt ++;
			commitDataBase();
		}
		this.closeCursor();
	}
	
	
	/***********************************************************************/
	public void initData() {
		hCardNo = "";
		hCurrentCode = "";
		hBlacklistDate = "";
		hBlacklistReason = "";

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TmpC005 proc = new TmpC005();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
