/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 * ---------  --------- ----------- ------------------------------------------------- *
 * 112/08/08  V1.00.00   NickChin    initial                                          *
 * 112/08/12  V1.00.01   Sunny       修改讀取催收資料的條件                                                                 *
 **************************************************************************************/

package Tmp;

import com.AccessDAO;
import com.CommCrd;

public class TmpC006 extends AccessDAO {
	public final boolean debug = false;
	private final String progname = "轉催日期程式 112/08/12  V1.00.01";
//	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
//	CommCrdRoutine comcr = null;
//	CommDate commDate = new CommDate();
//	CommRoutine comr = null;
//	CcaOutGoing ccaOutGoing = null;
//	String modUser = "";
	int n = 0;	
	int totalCnt = 0;
	int updateCnt = 0;
	int notFoundCnt = 0;
	boolean ibDebit = true;
	
	
	private String csDate = "";
	private String hIdNo = "";
	private String hPSeqno = "";
	private String hCorpNo = "";
	private String hCorpPSeqno = "";

	public static void main(String[] args) throws Exception {
		TmpC006 proc = new TmpC006();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

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

			//處理個人卡
			showLogMessage("I", "","****處理個人卡*******");
			updateCnt=0;
			notFoundCnt=0;
			selectActAcnoCrd();
			showLogMessage("I", "", String.format(" 處理個人卡結束 records[%d] notFound[%d]", updateCnt,notFoundCnt));
						
			//處理商務卡
			showLogMessage("I", "","****處理商務卡*******");
			updateCnt=0;
			notFoundCnt=0;
			selectActAcnoCorp();
			showLogMessage("I", "", String.format(" 處理商務卡結束 records[%d] notFound[%d]", updateCnt,notFoundCnt));
			
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,筆數 = %s", totalCnt));
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
//			ccaOutGoing.finalCnt2();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	
	private void selectActAcnoCrd() throws Exception {
		
//		getCsDate();
//		if(csDate.equals("")) {
//			return;
//		}
		
		sqlCmd = "select a.id_no,b.p_seqno,b.acct_status,b.STATUS_CHANGE_DATE ";
		sqlCmd += "from crd_idno a,act_acno b ";
		sqlCmd += "where a.id_p_seqno=b.id_p_seqno ";
		sqlCmd += "and b.acct_status='3' ";
		sqlCmd += "and b.acct_type='01' ";
		sqlCmd += "AND b.STATUS_CHANGE_DATE=''";
		
		this.openCursor();

		while (fetchTable()) {
			hIdNo = getValue("id_no");
			hPSeqno = getValue("p_seqno");
			
			if(debug) {
			System.out.println("hIdNo:"+hIdNo);
			System.out.println("hPSeqno:"+hPSeqno);
			}
			
			//取得個人卡-轉催日期
			getCsDate();
//			if(csDate.equals("")) {
//				return;
//			}
			
			if(hIdNo.length()>0 && csDate.length()==8) {			
			updateActAcnoCrd();
			totalCnt ++;
			}
			
			commitDataBase();
		}
		this.closeCursor();
		
	}
	
	private void selectActAcnoCorp() throws Exception {			
		
		sqlCmd = "select a.corp_no,b.p_seqno,b.acct_status,b.STATUS_CHANGE_DATE,a.corp_p_seqno ";
		sqlCmd += "from crd_corp a,act_acno b ";
		sqlCmd += "where a.corp_p_seqno=b.corp_p_seqno ";
		sqlCmd += "and b.acct_status='3' ";
		sqlCmd += "and b.acct_type='03' "; //商務卡
		sqlCmd += "AND b.STATUS_CHANGE_DATE=''";
		
		this.openCursor();

		while (fetchTable()) {
			hCorpNo = getValue("corp_no");
			hPSeqno = getValue("p_seqno");
			hCorpPSeqno = getValue("corp_p_seqno");
			
			//取得商務卡-轉催日期
			getCsDateCorp();
//			if(csDate.equals("")) {
//				return;
//			}
			
			if(hCorpNo.length()>0 && csDate.length()==8) {			
				updateActAcnoCorp();
				totalCnt ++;
			}
			
			if(debug) {
			System.out.println("hCorpNo:"+hCorpNo);
			System.out.println("hPSeqno:"+hPSeqno);
			}
			
			totalCnt ++;
			commitDataBase();
		}
		this.closeCursor();
		
	}

	private void updateActAcnoCrd() throws Exception {
		daoTable = "act_acno";
		updateSQL += "status_change_date = ? ,mod_time = sysdate ,mod_pgm = 'TmpC006' ";
		whereStr = "where p_seqno = ? and status_change_date <> ? ";
		setString(1, csDate);
		setString(2, hPSeqno);
		setString(3, csDate);
		
//		System.out.println("csDate:"+csDate);
//		System.out.println("hPSeqno:"+hPSeqno);
		
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update error -- 一般卡 act_acno, id_no[%s],p_seqno[%s],csDate[%s]", hIdNo,hPSeqno,csDate ));
			notFoundCnt++;
		}
		
		showLogMessage("I", "", String.format("update 一般卡 act_acno , id_no[%s],p_seqno[%s],csDate[%s]", hIdNo,hPSeqno,csDate ));
		updateCnt++;
	}

	private void updateActAcnoCorp() throws Exception {
		daoTable = "act_acno";
		updateSQL += "status_change_date = ? ,mod_time = sysdate ,mod_pgm = 'TmpC006' ";
		whereStr = "where acct_type='03' AND corp_p_seqno = ? and status_change_date <> ? ";
		setString(1, csDate);
		setString(2, hCorpPSeqno);
		setString(3, csDate);
		
		if(debug) {
		System.out.println("csDate:"+csDate);
		System.out.println("hCorpPSeqno:"+hCorpPSeqno);
		System.out.println("hPSeqno:"+hPSeqno);
		}
			
		updateTable();
		if (notFound.equals("Y")) {
			//showLogMessage("I", "", String.format("update act_acno error,corp_p_seqno = [%s]", hCorpPSeqno));
			showLogMessage("I", "", String.format("update error -- 商務卡 act_acno , corp_no[%s],p_seqno[%s],csDate[%s]", hCorpNo,hPSeqno,csDate ));
			notFoundCnt++;
		}
		
		showLogMessage("I", "", String.format("update 商務卡 act_acno , corp_no[%s],p_seqno[%s],csDate[%s]", hCorpNo,hPSeqno,csDate ));
		updateCnt++;
	}
	
	private void getCsDate() throws Exception {
		csDate = "";
		String oCsDate = "";
//        sqlCmd = " SELECT IDN,CARD_ORG,CS_DATE FROM COL_LAP942_DELQ ";
//        sqlCmd += "where IDN = 'A121593467' ";
//        sqlCmd += "AND CARD_ORG='106' ";
//        sqlCmd += "ORDER BY IDN,CARD_ORG,CS_DATE ";
//        sqlCmd += "LIMIT 1";
		
		 sqlCmd = " SELECT C.ID_NO,B.ACCT_TYPE,B.CARD_NO,A.CS_DATE "; 
		 sqlCmd += "FROM COL_LAP942_DELQ A,CRD_CARD B,CRD_IDNO C ";
		 sqlCmd += "WHERE C.ID_P_SEQNO = B.ID_P_SEQNO ";
		 sqlCmd += "AND A.CARD_NMBR=B.CARD_NO ";
		 sqlCmd += "AND B.ACCT_TYPE='01' ";
		 sqlCmd += "AND C.ID_NO = ? ";
		 sqlCmd += "ORDER BY C.ID_NO,B.ACCT_TYPE,A.CS_DATE ";
		 sqlCmd += "LIMIT 1 ";

		 setString(1, hIdNo);
		 
        selectTable();
        if (notFound.equals("Y")) {
        	//showLogMessage("I", "", "SELECT COL_LAP942_DELQ NOT FOUND!");
        	showLogMessage("I", "", String.format("SELECT COL_LAP942_DELQ NOT FOUND! id_no[%s],p_seqno[%s]", hIdNo,hPSeqno));
        	return;
        }
        oCsDate = getValue("CS_DATE");
        if(oCsDate.length()==7) {
        	int formatYear = Integer.parseInt(oCsDate.substring(0, 3))+1911;
        	csDate = Integer.toString(formatYear)+oCsDate.substring(3);
        }else if(oCsDate.length()>7) {
        	csDate = oCsDate;
        }
	}
	
	private void getCsDateCorp() throws Exception {
		csDate = "";
		String oCsDate = "";
//        sqlCmd = " SELECT IDN,CARD_ORG,CS_DATE FROM COL_LAP942_DELQ ";
//        sqlCmd += "where IDN = 'A121593467' ";
//        sqlCmd += "AND CARD_ORG='106' ";
//        sqlCmd += "ORDER BY IDN,CARD_ORG,CS_DATE ";
//        sqlCmd += "LIMIT 1";
		
		 sqlCmd = " SELECT C.CORP_NO,B.ACCT_TYPE,B.CARD_NO,A.CS_DATE "; 
		 sqlCmd += "FROM COL_LAP942_DELQ A,CRD_CARD B,CRD_CORP C ";
		 sqlCmd += "WHERE C.CORP_P_SEQNO = B.CORP_P_SEQNO ";
		 sqlCmd += "AND A.CARD_NMBR=B.CARD_NO ";
		 sqlCmd += "AND B.ACCT_TYPE='03' ";
		 sqlCmd += "AND C.CORP_NO = ? ";
		 sqlCmd += "ORDER BY C.CORP_NO,B.ACCT_TYPE,A.CS_DATE ";
		 sqlCmd += "LIMIT 1 ";

		 setString(1, hCorpNo);
		 
        selectTable();
        if (notFound.equals("Y")) {
        	//showLogMessage("I", "", "SELECT COL_LAP942_DELQ NOT FOUND!");
        	showLogMessage("I", "", String.format("SELECT COL_LAP942_DELQ NOT FOUND! CorpNo[%s],p_seqno[%s]", hCorpNo,hPSeqno));
        	return;
        }
        oCsDate = getValue("CS_DATE");
        if(oCsDate.length()==7) {
        	int formatYear = Integer.parseInt(oCsDate.substring(0, 3))+1911;
        	csDate = Integer.toString(formatYear)+oCsDate.substring(3);
        }else if(oCsDate.length()>7) {
        	csDate = oCsDate;
        }
	}

}
