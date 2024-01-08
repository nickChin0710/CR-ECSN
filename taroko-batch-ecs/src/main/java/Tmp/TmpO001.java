/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 112-11-21  V1.00.00     Ryan     initial                                            *
 **************************************************************************************/

package Tmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;

public class TmpO001 extends AccessDAO {
	private final String progname = "舊案委外資料轉入委外主檔處理程式  112/11/21 V.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	CommString comStr = new CommString();
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int okCnt = 0;
	int readCnt = 0;
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	int reportSeq = 0;
	private String reportId = "TmpO001";
	private String reportName = "舊案委外資料轉入委外主檔錯誤報表";
	private String skipCntFlag = "";
	private int errCnt = 0;
	private int pageCnt = 0;

	/********** TABLE : COL_BAD_OUTSOURCE ************/
	private String hIdCorpPSeqno = "";
	private String hIdCorpNo = "";
	private String hCardFlag = "";
	private String hOsCmpId = "";
	private String hOsCmpNo = "";
	private String hOsCmpName = "";
	private String hOsAmt = "";
	private String hOsDate = "";
	private String hHandType = "";
	private String hBackCode = "";
	private String hBackDate = "";
	private String hAprUser = "";
	private String hModUser = "";
	String debugFlag = "";

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
			showLogMessage("I", "", "-->connect DB: " + getDBalias()[0]);

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());

			// 增加執行參數，delall表轉全檔前先刪除
			if (args.length > 0) {
				debugFlag = args[0].toLowerCase(); // 參數value一律轉小寫
				System.out.println(String.format("有開啟參數 debugFlag = [%s]", debugFlag));
			}

			if (debugFlag.equals("alldel")) {
				deletePtrBatchRpt();
				deleteColBadOutSource();
				deleteColBadOutSourceHst();
			}

			selectColLap942Bad();
			printTailer(); //表尾統計筆數
			if (lpar1.size() > 0)
				comcr.insertPtrBatchRpt(lpar1);		
			lpar1.clear();			
			commitDataBase();

			showLogMessage("I", "", String.format("程式處理結果 ,錯誤筆數 = %s", errCnt));
			showLogMessage("I", "", String.format("程式處理結果 ,總筆數 = %s", readCnt));
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

	void selectColLap942Bad() throws Exception {
		sqlCmd = " SELECT distinct decode(a.ACCT_TYPE,'01','1','03',2) AS card_flag, ";
		sqlCmd += " decode(a.ACCT_TYPE,'01',b.ID_P_SEQNO,'03',c.CORP_P_SEQNO) AS ID_CORP_P_SEQNO, ";
		sqlCmd += " decode(a.ACCT_TYPE,'01',b.ID_NO,'03',c.CORP_NO) AS ID_CORP_NO, ";
		sqlCmd += " decode(a.OUTSOURCE,'聯合','001','聯立','002','高柏','003','') AS os_cmp_no, ";
		sqlCmd += " a.OUTSOURCE AS os_cmp_name, ";
		sqlCmd += " a.OUTSOURCEDAY AS os_date, ";
		sqlCmd += " a.MANAGER AS mod_user, ";
		sqlCmd += " a.approv AS apr_user, ";
		sqlCmd += " a.RECALL AS hand_type ";
		sqlCmd += " FROM COL_LAP942_BAD a,crd_idno b ";
		sqlCmd += " LEFT JOIN crd_corp c ON a.CORP_P_SEQNO=c.corp_p_seqno ";
		sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";
		sqlCmd += " AND OUTSOURCEDAY<>'' AND OUTSOURCEDAY IS NOT NULL AND OUTSOURCE<>'NULL' ";
		sqlCmd += " ORDER BY ID_CORP_P_SEQNO,OUTSOURCEDAY ";
//		sqlCmd += " fetch first 10 rows only "; // for test
		this.openCursor();

		while (fetchTable()) {
			readCnt++;
			initData();
			hIdCorpPSeqno = getValue("ID_CORP_P_SEQNO");
			hIdCorpNo = getValue("ID_CORP_NO");
			hCardFlag = getValue("card_flag");
			hOsCmpNo = getValue("os_cmp_no");
			hOsCmpName = getValue("os_cmp_name");
			hOsDate = getValue("os_date");
			hHandType = getValue("hand_type");
			hModUser = getValue("mod_user");
			hAprUser = getValue("apr_user");
			procData();
		}
		this.closeCursor();
	}

	int selectColBadOutsource() throws Exception {
		sqlCmd = " select count(*) col_cnt from COL_BAD_OUTSOURCE where id_corp_p_seqno = ? ";
		setString(1, hIdCorpPSeqno);
		selectTable();
		return getValueInt("col_cnt");
	}

	void insertPtrBatchRpt() throws Exception {
		if (errCnt == 0)
			printHeader();		
		printDetail();
		errCnt++;		
	}

	void insertColBadOutsource() throws Exception {
		this.daoTable = "COL_BAD_OUTSOURCE";
		setValue("id_corp_no", hIdCorpNo);
		setValue("id_corp_p_seqno", hIdCorpPSeqno);
		setValue("card_flag", hCardFlag);
		setValue("os_cmp_id", hOsCmpId);
		setValue("os_cmp_no", hOsCmpNo);
		setValue("os_cmp_name", hOsCmpName);
		setValue("hand_type", hHandType);
		setValue("os_amt", hOsAmt);
		setValue("os_date", hOsDate);
		setValue("back_code", hBackCode);
		setValue("back_date", hBackDate);
		setValue("crt_user", javaProgram);
		setValue("crt_time", sysDate + sysTime);
		setValue("apr_date", sysDate);
		setValue("apr_user",javaProgram);
		setValue("mod_user",javaProgram);		
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 0);
		setValue("mod_time", sysDate + sysTime);
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("I", "", String.format("insertColBadOutsource ERROR , id_corp_no=[%s]", hIdCorpNo));
			skipCntFlag = "Y";
		}
	}

	void insertColBadOutsourceHst() throws Exception {
		if ("Y".equals(skipCntFlag))
			return;
		this.daoTable = "COL_BAD_OUTSOURCE_HST";
		setValue("id_corp_no", hIdCorpNo);
		setValue("id_corp_p_seqno", hIdCorpPSeqno);
		setValue("card_flag", hCardFlag);
		setValue("os_cmp_id", hOsCmpId);
		setValue("os_cmp_no", hOsCmpNo);
		setValue("os_cmp_name", hOsCmpName);
		setValue("hand_type", hHandType);
		setValue("os_amt", hOsAmt);
		setValue("os_date", hOsDate);
		setValue("back_code", hBackCode);
		setValue("back_date", hBackDate);
		setValue("crt_user", javaProgram);
		setValue("crt_time", sysDate + sysTime);
		setValue("apr_user", javaProgram);
		setValue("apr_date", sysDate);
		setValue("mod_user", javaProgram);
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 0);
		setValue("mod_time", sysDate + sysTime);
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("I", "",
					String.format("insertColBadOutsourceHst ERROR , id_corp_no=[%s]", hIdCorpNo));
			skipCntFlag = "Y";
		}
	}

	void printAdd(String buff) throws Exception {
		reportSeq++;
		lpar1.add(comcr.putReport(reportId, reportName, "", reportSeq, "0", buff));
	}

	private void procData() throws Exception {
		int resultCnt = selectColBadOutsource();
		if (resultCnt > 0) {
			insertPtrBatchRpt();			
		} else {
			okCnt++;
			insertColBadOutsource();
			insertColBadOutsourceHst();
		}
		if ((readCnt % 1000) == 0) {
			showLogMessage("I", "", "  Read w/ ROW " + readCnt);
		}
	}

	void printHeader() throws Exception {
		pageCnt++;
		StringBuffer tt = new StringBuffer();
		comStr.insertCenter(tt, "合作金庫商業銀行", 130);
		printAdd(tt.toString());

		tt = new StringBuffer();
		comStr.insertCenter(tt, "舊案委外資料轉入委外主檔錯誤報表", 130);
		printAdd(tt.toString());

		tt = new StringBuffer();
		comStr.insert(tt, "報表編號:TmpO001", 1);
		comStr.insert(tt, "頁    次:", 119);
		comStr.insert(tt, comStr.int2Str(pageCnt), 128);
		printAdd(tt.toString());

		tt = new StringBuffer();
		comStr.insert(tt, "列印日期:", 119);
		comStr.insert(tt, commDate.dspDate(sysDate), 128);
		printAdd(tt.toString());

		tt = new StringBuffer();
		comStr.insert(tt, " ", 1);
		printAdd(tt.toString());

		tt = new StringBuffer();
		comStr.insert(tt, "身份證字號/統編", 1);
		comStr.insert(tt, "委外公司", 20);
		comStr.insert(tt, "委外日期", 30);
		comStr.insert(tt, "委外手別", 40);
		comStr.insert(tt, "錯誤原因", 50);
		printAdd(tt.toString());

		tt = new StringBuffer();
		comStr.insert(tt, comStr.rpad("=", 136, "="), 1);
		printAdd(tt.toString());
	}

	void printDetail() throws Exception {
		StringBuffer tt = new StringBuffer();
		comStr.insert(tt, hIdCorpNo, 1);
		comStr.insert(tt, hOsCmpName, 20);
		comStr.insert(tt, hOsDate, 30);
		comStr.insert(tt, hHandType, 40);
		comStr.insert(tt, "資料已存在(如:委案資訊不一致)", 50);
		printAdd(tt.toString());
	}

	void printTailer() throws Exception {		
		StringBuffer tt = new StringBuffer();		
		comStr.insert(tt, "失  敗: ",  5);
		comStr.insert(tt, comStr.int2Str(errCnt), 12);
		printAdd(tt.toString());
	}
	/***********************************************************************/
	public void initData() {
		skipCntFlag = "";
		hIdCorpPSeqno = "";
		hIdCorpNo = "";
		hCardFlag = "";
		hOsCmpId = "";
		hOsCmpNo = "";
		hOsCmpName = "";
		hOsAmt = "";
		hOsDate = "";
		hHandType = "";
		hBackCode = "";
		hBackDate = "";
	}

	/***********************************************************************/
	int deleteColBadOutSource() throws Exception {
		daoTable = "col_bad_outsource";
		int recCnt = deleteTable();

		showLogMessage("I", "", "delete col_bad_outsource cnt :[" + recCnt + "]");

		return (0);
	}

	/***********************************************************************/
	int deleteColBadOutSourceHst() throws Exception {
		daoTable = "col_bad_outsource_hst";
		int recCnt = deleteTable();

		showLogMessage("I", "", "delete col_bad_outsource_hst cnt :[" + recCnt + "]");

		return (0);
	}
	
	/***********************************************************************/
	 int deletePtrBatchRpt() throws Exception
	 {
	  daoTable  = "ptr_batch_rpt";
	  whereStr  = "where program_code like 'TmpO001%' "
	            + "and   start_date = ? ";

	  setString(1 , sysDate);

	  int recCnt = deleteTable();

	  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

	  return(0);
	 }

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TmpO001 proc = new TmpO001();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
