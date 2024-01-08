package Cms;
/**
 * 2019-0718   JH    p_xxx >>acno_p_xxx
 * 107/03/08  V1.00.00     Alex     initial
 * 109/02/18  V1.00.01     Pino     customized for TCB
 *  109/12/07  V1.00.02    shiyuqi       updated for project coding standard   *
 *  2023-0413  V1.00.02		JH		排除一般消費(特店)
 * */

import com.BaseBatch;
import com.SqlParm;

public class CmsA014 extends BaseBatch {
private String progname = "道路救援新發卡自動登錄處理程式 2023-0413  V1.00.02";
//CommFunction comm = new CommFunction();
//CommRoutine comr = null;
//CommCrd comc = new CommCrd();

//=======================================
hdata.CrdCard hCard =new hdata.CrdCard();
hdata.CmsRoadmaster hRoad =new hdata.CmsRoadmaster();

// ---------------------------------------------------------------------------
private double hBillDestAmt = 0;
private double hRodeRdSeqno = 0;
private String hCrltMajorIdpSeqno = "";
private String hCrltMajorCardNo = "";
private String hCrltCardNo = "";
private String hCrltAcnoPSeqno = "";
private String hCrltProjNo = "";
private String hCrltYearType = "";
private String hCrltProcFlag = "";
private String hCrltIdPSeqno = "";
private String hCrltEndCardNo = "";
private String hCrltRdsPcard = "";
private double hCrltPurchAmt = 0;
private double hCrltPurchRow = 0;
private double hCrltTolAmt = 0;
private String hRdp2FstAmtCond = "";
private double hRdp2FstPurchAmt = 0;
private String hRdp2FstRowCond = "";
private long hRdp2FstPurchRow = 0;
private double hRdp2LstTolAmt = 0;
private String hPctpCardNote = "";
private String hWdayThisAcctMonth = "";
// ---------------------------------------------------------------------------
private int recordCnt = 0;
private int totalCnt = 0;
private String hTempBusiDate = "";
private String hTempNewCardDate = "";
private String hTempLastYear = "";
private String hTempIssueDate = "";
private int[] hMCount = new int[250];
private String[] hMCardNo = new String[250];
private String[] hMIssueDate = new String[250];
private String[] hMOldCardNo = new String[250];
private String[] hMOppostDate = new String[250];
private String[] hMCurrentCode = new String[250];
private String hTempOppostDate = "";
private double hMcceConsumeBlAmt =0;
private int hTempDataCnt =0;

//========================================
private SqlParm ttList =null;
private SqlParm ttDetl1 =null;
private SqlParm ttMast =null;
private SqlParm ttDetl =null;
//-------------------------
private int tiRoadparm2 =-1;
private int tiCard =-1;
private int tiParm2 =-1;
private int tiList1U =-1;
private int tiMaster =-1;
private int tiMastU =-1;
private int tiMaster0 =-1;
private int tiListU2 =-1;
private int tiCard1 =-1;
private int tiCard13 =-1;
private int tiList3U =-1;
private int tiList3U3 =-1;

int commit =1;

//=*****************************************************************************
public static void main(String[] args) {
	CmsA014 proc = new CmsA014();

//	proc.debug = true;
//	proc.ddd_sql(proc.debug);

	proc.mainProcess(args);
	proc.systemExit();
}


@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg =args.length;
	if (liArg >2) {
		printf("Usage : CmsA014 [business_date,batch_seqno]");
		errExit(1);
	}

	dbConnect();

	if (liArg >0) {
		setBusiDate(args[0]);
		callBatchSeqno(args[liArg -1]);
	}
	callBatch(0,0,0);

	//------------------------------------------------------------------
	hTempBusiDate = commDate.dateAdd(hBusiDate, 0, 0, -1);
	hTempNewCardDate = commDate.dateAdd(hBusiDate, 0, -12, 0);
	hTempLastYear = hTempNewCardDate.substring(0, 4);
	printf("-->issue_date[%s], new_card_date[%s], last_year[%s]"
			, hTempBusiDate, hTempNewCardDate, hTempLastYear);
	//------------------------------------------------------------------
	deleteCmsRoadlist();
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("新發卡資料篩選處理開始.....");
	selectPtrAcctType();
	printf("核卡日 : [%s] ", hTempBusiDate);
	printf("累計卡號筆數 : [%s] 名單筆數 : [%s]", totalCnt, recordCnt);
	//--------------------------------------------------------------------------
	totalCnt = 0;
	printf("新增舊卡號資料處理開始.....");
	insertCmsRoadlist2();
	printf("累計卡號筆數 : [%s]", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("專案方案設定(正卡ID)處理開始.....");
	selectCmsRoadlist1();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("當年度消費資格判斷(正卡帳戶)處理開始.....");
	selectBilBill1();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("以前年度消費資格判斷(正卡帳戶)處理開始.....");
	selectMktCardConsume1();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("當年度消費資格判斷(正卡卡號)處理開始.....");
	selectBilBill2();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("以前年度消費資格判斷(正卡卡號)處理開始.....");
	selectMktCardConsume2();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("當年度消費資格判斷(正附卡分開計算)處理開始.....");
	selectBilBill3();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	totalCnt = recordCnt = 0;
	printf("以前年度消費資格判斷(正附卡分開計算)處理開始.....");
	selectMktCardConsume3();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	selectCmsRoaddetail();
	totalCnt = recordCnt = 0;
	printf("救援資格(卡號)處理開始.....");
	selectCmsRoadlist4();
	printf("累計筆數 : [%s] ", totalCnt);
	//--------------------------------------------------------------------------
	printf("程式執行結束");
	
	sqlCommit(commit);
	endProgram();
}

void deleteCmsRoadlist() throws Exception {
	sqlCmd = "delete cms_roadlist";
	sqlExec(sqlCmd);

	if (sqlNrow < 0) {
		printf("delete cms_roadlist error !! ");
		errExit(1);
	}
}
//==****************************************************************************
void selectPtrAcctType() throws Exception {
	sqlCmd = " select "
		+ " b.major_id_p_seqno , "
		+ " b.acno_p_seqno , "
		+ " b.major_card_no , "
		+ " b.card_no , "
		+ " b.acct_type , "
		+ " b.card_type , "
		+ " b.group_code , "
		//+ " b.corp_no , "
		+ " d.this_acct_month ,"
		+ " b.id_p_seqno ,"
		+ " b.end_card_no ,"
		+ " a.rds_pcard "
		+ " from crd_card B join ptr_workday D on D.stmt_cycle =B.stmt_cycle , ptr_card_type A"
		+ " where b.card_type in (select card_type from ptr_card_type where rds_pcard ='A')"
		+ " and b.acct_type in (select acct_type from ptr_acct_type where car_service_flag ='Y')"
		+ " and b.issue_date = ? "
		+ " and a.card_type = b.card_type "
//		+ " and b.issue_date like '201801%' "
		;

	ppp(1, hTempBusiDate);
	openCursor();
	while(fetchTable()) {
		hCrltMajorIdpSeqno = colSs("major_id_p_seqno");
		hCard.acnoPSeqno = colSs("acno_p_seqno");
		hCard.majorCardNo = colSs("major_card_no");
		hCrltCardNo = colSs("card_no");
		hCard.acctType = colSs("acct_type");
		hCard.cardType = colSs("card_type");
		hCard.groupCode = colSs("group_code");
		//h_card.corp_no = col_ss(ll, "corp_no");
		hWdayThisAcctMonth = colSs("this_acct_month");
		hCrltIdPSeqno = colSs("id_p_seqno");
		hCrltEndCardNo = colSs("end_card_no");
		hCrltRdsPcard = colSs("rds_pcard");

		selectCmsRoadparm2Dtl();
		totalCnt++;
	}
	
	closeCursor();
}

//==****************************************************************************
void selectCmsRoadparm2Dtl() throws Exception {
	if (tiRoadparm2 <=0) {
		sqlCmd = " select distinct "
			+ " a.proj_no "
			+ " from cms_roadparm2_dtl A join cms_roadparm2 B on A.proj_no =B.proj_no"
			+ " where 1=1"
			+ " and a.acct_type = ? "
			+ " and a.card_type = ? "
			+ " and uf_nvl(a.group_code,?) = ? "
//			+ " and uf_nvl(a.corp_no,?) = ? "
			+ " and uf_nvl(b.valid_end_date,'99991231') > ? "
			+ " and b.apr_flag = 'Y' ";
		tiRoadparm2 =ppStmtCrt("ti_roadparm2","");
	}
	sqlSelect(tiRoadparm2, new Object[] {
		hCard.acctType,
		hCard.cardType,
		hCard.groupCode,
		hCard.groupCode,
//		h_card.corp_no, 
//		h_card.corp_no, 
		hBusiDate
	});

	if (sqlNrow < 0) {
		sqlerr("select_cms_roadparm2_dtl( error, kk="+ hCrltCardNo);
		errExit(1);
	}
	if (sqlNrow ==0)
		return;

//	ddd("select_cms_roadparm2_dtl:proj_no[%s], card_no[%s]",col_ss("proj_no"),h_crlt_card_no);
	int llNrow = sqlNrow;

	for (int ll=0; ll<llNrow; ll++) {
		hCrltProjNo = colSs(ll, "proj_no");
		insertCmsRoadlist();
		recordCnt++;
	}
}

//==============================================================================
void insertCmsRoadlist() throws Exception {
	if (ttList ==null) {
		ttList =new com.SqlParm();
		ttList.sqlFrom = " insert into cms_roadlist ( "
			+ " major_id_p_seqno , "
			+ " acno_p_seqno , "
			+ " major_card_no , "
			+ " end_card_no , "
			+ " card_no , "
			+ " proj_no , "
			+ " year_type , "
			+ " rm_status , "
			+ " purch_amt , "
			+ " purch_row , "
			+ " tol_amt , "
			+ " proc_flag , "
			+ " mod_pgm , "
			+ " mod_time , "
			+ " rds_pcard , "
			+ " id_p_seqno "
			+ " ) values ( "
			+ ttList.kkk(" ?","major_id_p_seqno")
			+ ttList.kkk(",?","acno_p_seqno")
			+ ttList.kkk(",?","major_card_no")
			+ ttList.kkk(",?","end_card_no")
			+ ttList.kkk(",?","card_no")
			+ ttList.kkk(",?","proj_no")
			+ ttList.kkk(",?","year_type")
			+ ", '0'"
			+ ", 0"	//purch_amt")
			+ ", 0"	//purch_row")
			+ ", 0"	//tol_amt")
			+ ", 'N'"
			+ ttList.kkk(",?","mod_pgm")
			+ ","+commSqlStr.sysDTime
			+ ttList.kkk(",?","rds_pcard")
			+ ttList.kkk(",?","id_p_seqno")
			+ " )";
		ttList.pfidx =ppStmtCrt("tt_list-A", ttList.sqlFrom);
	}
	String a1 = hCard.majorCardNo;
	ttList.ppp("major_id_p_seqno", hCrltMajorIdpSeqno);
	ttList.ppp("acno_p_seqno", hCard.acnoPSeqno);
	ttList.ppp("major_card_no", hCard.majorCardNo);
	ttList.ppp("end_card_no", hCrltEndCardNo);
	ttList.ppp("card_no", hCard.majorCardNo);
	ttList.ppp("proj_no", hCrltProjNo);
	ttList.ppp("year_type", hCrltYearType);
	ttList.ppp("mod_pgm",hModPgm);
	ttList.ppp("rds_pcard", hCrltRdsPcard);
	ttList.ppp("id_p_seqno", hCrltIdPSeqno);
	sqlExec(ttList.pfidx, ttList.getConvParm());

	if (sqlNrow <= 0) {
		sqlerr("insert cms_roadlist erroren, kk="+ hCard.cardNo);
		errExit(1);
	}
}

//==****************************************************************************
void insertCmsRoadlist2() throws Exception {
	String tableB =""
			+" select end_card_no, min(major_card_no) as major_card_no "
			+" from crd_card"
			+" group by end_card_no"
			+" having count(*) >1 and min(current_code) = '0' "
			+" and min(card_no) = min(major_card_no)"
			;
	sqlCmd ="select "
			+ " major_id_p_seqno , "
			+ " b.major_card_no , "
			+ " a.end_card_no , "
			+ " a.card_no , "
			+ " a.acno_p_seqno , "
			+ " proj_no , "
			+ " year_type , "
			+ " rm_status , "
			+ " proc_flag , "
			+ " proc_date , "
			+ " purch_amt , "
			+ " purch_row , "
			+ " tol_amt , "
			+ " mod_time, mod_pgm ,"
			+ " a.id_p_seqno , "
			+ " a.rds_pcard "
			+ " from cms_roadlist A , ("+tableB+") B"
			+ " where A.end_card_no = B.end_card_no "
			+ " and A.major_card_no <> B.major_card_no ";
			;
	openCursor();
	while(fetchTable()) {
		totalCnt++;
		daoTable ="cms_roadlist";
		this.insertTable();
	}
	closeCursor();
}

//==****************************************************************************
void selectCmsRoadlist1() throws Exception {
	sqlCmd = " select "
		+ " a.proj_no , "
		+ " a.acno_p_seqno "
		+ " from cms_roadlist A join cms_roadparm2 B on A.proj_no =B.proj_no "
		+ " where 1=1 "
		+ " and uf_nvl(b.valid_end_date,'99991231') > ? "
		+ " and b.apr_flag = 'Y' "
		+ " group by a.proj_no , a.acno_p_seqno ";
	
	ppp(1, hBusiDate);
	openCursor();

	while (fetchTable()) {
		hCrltProjNo = colSs("proj_no");
		hCrltAcnoPSeqno = colSs("acno_p_seqno");
		hCrltProcFlag = "0";

		hCrltYearType = ""+ selectCrdCard();
		selectCmsRoadparm2();
		if (ssComp(hCrltYearType,"3") > 0)
			hCrltProcFlag = "X";
		totalCnt++;
		updateCmsRoadlist1();
	}
	closeCursor();
}

//=*****************************************************************************
void updateCmsRoadlist2() throws Exception {
	if (tiListU2 <=0) {
		sqlCmd ="update cms_roadlist set"
				+" proc_flag =?,"
				+" proc_date =?,"
				+" purch_amt =purch_amt +?,"
				+" purch_row =purch_row +?,"
				+" mod_time =sysdate,"
				+" mod_pgm =?"
				+" where acno_p_seqno =?"
				+" and year_type =?"
				+" and proj_no =?";
		tiListU2 =ppStmtCrt("ti_list_U2","");
	}
	
	ppp(hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hBillDestAmt);
	ppp(hTempDataCnt);
	ppp(hMcceConsumeBlAmt);
	ppp(hModPgm);
	//--
	ppp(hCrltAcnoPSeqno);
	ppp(hCrltYearType);
	ppp(hCrltProjNo);

	if (sqlNrow <=0) {
   	sqlerr("update cms_roadlist_2 error, kk="+ hCrltAcnoPSeqno);
      errExit(1);
   }
 }

//==****************************************************************************
int selectCrdCard() throws Exception {
	int iiCardCnt = 0, oppostFlag = 0;
	int firstYearFlag = 0;
	int oldCardTag = 0, validCardTag = 0;

	hMCardNo =new String[250];
	hMIssueDate =new String[250];
	hMOldCardNo =new String[250];
	hMOppostDate =new String[250];
	hMCurrentCode =new String[250];
	hMCount =new int[250];

	if (tiCard <=0) {
		sqlCmd = " select "
			+ " card_no , "
			+ " issue_date , "
			+ " old_card_no , "
			+ " oppost_date , "
			+ " decode(current_code,'5','2',current_code) as current_code "
			+ " from crd_card "
			+ " where acno_p_seqno = ? "
			+ " order by issue_date desc ";
		tiCard =ppStmtCrt("ti_card","");
	}

	daoTid ="card.";
	sqlSelect(tiCard, new Object[] {hCrltAcnoPSeqno});

	if (sqlNrow < 0) {
		sqlerr("select_crd_card error, kk="+ hCrltAcnoPSeqno);
		errExit(1);
	}

	iiCardCnt = sqlNrow;

	for (int ii = 0; ii < iiCardCnt; ii++) {
		hMCardNo[ii] = colSs(ii, "card.card_no");
		hMIssueDate[ii] = colSs(ii, "card.issue_date");
		hMOldCardNo[ii] = colSs(ii, "card.old_card_no");
		hMOppostDate[ii] = colSs(ii, "card.oppost_date");
		hMCurrentCode[ii] = colSs(ii, "card.current_code");
		hMCount[ii] = commDate.monthsBetween(colSs(ii, "card.oppost_date"), hBusiDate);
	}

	for (int ii = 0; ii < iiCardCnt; ii++) {
		if (eq(hMCurrentCode[ii], "0")
			|| (eq(hMCurrentCode[ii], "2") && hMCount[ii] <= 3)) {
			validCardTag = 1;
			if (eq(hMCurrentCode[ii], "2")) {
				hMCurrentCode[ii] = "0";
				hMOppostDate[ii] = "";
			}
		}
	}

	//-old卡無效, 有辦new.卡----
	for (int ii=0; ii<iiCardCnt - 1; ii++) {
		for (int kk=ii+1; kk<iiCardCnt; kk++) {
			if ( noEmpty(hMOldCardNo[ii])
				&& eq(hMCurrentCode[ii], "0")
				&& eq(hMCardNo[kk], hMOldCardNo[ii]) ) {
				hMCurrentCode[kk] = "0";
			}
		}
	}

	// 舊卡友 --
	for (int ii=0; ii<iiCardCnt; ii++) {
		if ( eq(hMCurrentCode[ii], "0")
			&& ssComp(hTempNewCardDate, hMIssueDate[ii])>0 ) {
			oldCardTag = 1; 
			break;
		}
	}

	// 活卡最早發卡日 --
	hTempIssueDate = "";
	for (int ii = (iiCardCnt-1); ii >= 0; ii--) {
		if (eq(hMCurrentCode[ii], "0")) {
			hTempIssueDate = hMIssueDate[ii]; 
			hTempOppostDate = "00000000";
			break;
		}
	}

	// 活卡最早發卡日之前最晚停卡日 --
	if (oldCardTag == 0) {
		for (int ii = 0; ii < iiCardCnt; ii++) {
			if (ssComp(hMIssueDate[ii], hTempIssueDate) >= 0)
				continue;
			if ( noEmpty(hMOppostDate[ii])
				&& ssComp(hMOppostDate[ii], hTempOppostDate)>0 ) {
				hTempOppostDate = hMOppostDate[ii]; 
				oppostFlag = 1;
			}
		}

		// 最近 12 個月(新舊卡友 --
		if (oppostFlag == 1) {
			if (commDate.monthsBetween(
					commString.left(hTempIssueDate,6),
					commString.left(hTempOppostDate,6)) < 12) {
				oldCardTag = 1; 
			}
		}
	}

	firstYearFlag = 0;
	if (eq(hTempIssueDate, hBusiDate.substring(0, 4)))
		firstYearFlag = 1;

	if (oldCardTag ==1 && validCardTag ==1 && firstYearFlag ==0 )
		return 2; /* 非首年舊卡友 */
	if ( oldCardTag ==1 && validCardTag ==1 && firstYearFlag ==1 )
		return 3; /* 首年舊卡友 */
	if ( oldCardTag ==1 && validCardTag ==0 )
		return 5; /* 無有效卡 */
	if ( oldCardTag ==0 && validCardTag ==1 && firstYearFlag ==0 )
		return 2; /* 非首年新卡友 */
	if ( oldCardTag ==0 && validCardTag ==1 && firstYearFlag ==1 )
		return 1; /* 首年新卡友 */
	if ( oldCardTag ==0 && validCardTag ==0 )
		return 5; /* 無有效卡 */
	return 9;
}

//==****************************************************************************
void selectCmsRoadparm2() throws Exception {
	if (tiParm2 <=0) {
		sqlCmd = " select "
			+ " decode(decode(cast(? as char(1)),'2',lst_tol_amt,0),0,'Y','2') as xx_proc_flag "
			+ " from cms_roadparm2 "
			+ " where proj_no = ? "
			+ " and decode(cast(? as char(1)),'1',fst_one_low_amt,'2',lst_one_low_amt,'3',cur_one_low_amt) = 0 "
			+ " and decode(cast(? as char(1)),'1',fst_purch_amt,'2',lst_purch_amt,'3',cur_purch_amt) = 0 "
			+ " and decode(cast(? as char(1)),'1',fst_purch_row,'2',lst_purch_row,'3',cur_purch_row) = 0 "
			+commSqlStr.rownum(1)
			;
		tiParm2 =ppStmtCrt("ti_parm2","");
	}
	
	//ddd_sql();
	sqlSelect(tiParm2, new Object[] {
			hCrltYearType,
			hCrltProjNo,
			hCrltYearType,
			hCrltYearType,
			hCrltYearType, 
	});

	if (sqlNrow < 0) {
		sqlerr("select cms_roadparm2 error !! kk="+ hCrltProjNo);
		errExit(1);
	}

	if (sqlNrow == 0)
		hCrltProcFlag = "0";
	else
		hCrltProcFlag = colSs("xx_proc_flag");
}

//==****************************************************************************
void updateCmsRoadlist1() throws Exception {
	if (tiList1U <= 0) {
		sqlCmd = " update cms_roadlist set "
			+ " year_type = ? , "
			+ " proc_flag = ? , "
			+ " proc_date = ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where acno_p_seqno = ? "
			+ " and proj_no = ? ";
		tiList1U =ppStmtCrt("ti_list1_U","");
	}

	ppp(hCrltYearType);
	ppp(hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hModPgm);
	//
	ppp(hCrltAcnoPSeqno);
	ppp(hCrltProjNo);
	
	sqlExec(tiList1U);
	if (sqlNrow <= 0) {
		sqlerr("update cms_roadlist_1 error, kk[%s,%s]",
				hCrltMajorIdpSeqno, hCrltProjNo);
		errExit(1);
	}
}

//==****************************************************************************
void selectBilBill1() throws Exception {
	String tableE ="SELECT c.acno_p_seqno,"
			+" c.year_type,"
			+" c.proj_no,"
			+" max(d.amt_sum_flag) as amt_sum_flag,"
			+" max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond,"
			+" max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as fst_acct_code_bl,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as fst_acct_code_it,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as fst_acct_code_ca," 
			+" max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as fst_acct_code_id," 
			+" max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as fst_acct_code_ao," 
			+" max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as fst_acct_code_ot," 
			+" max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt,"
			+" max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond," 
			+" max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt," 
			+" max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond," 
			+" max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row,"
			+" max(d.lst_tol_amt) as lst_tol_amt,"
			+" max(decode(c.year_type,'1',d.fst_mcht,'2',d.lst_mcht,'3',d.cur_mcht)) as fst_mcht"
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+" WHERE  1=1"
			+" AND    d.amt_sum_flag = '1'"
			+" AND    c.proc_flag    = '0'"
			+" AND    c.year_type <= '3'"
			+" GROUP BY c.acno_p_seqno,c.year_type,c.proj_no"
			;
    selectSQL =" sum(decode(A.sign_flag,'-',0,1)) as bill_cnt,"
			+" sum(decode(A.acct_code,'IT',"
			+"     	decode(A.install_curr_term,1,decode(B.refund_apr_flag,'Y',0,b.tot_amt),0),"
			+"     	decode(A.sign_flag,'-',-1,1) * A.dest_amt)) as dest_amt,"
			+" E.acno_p_seqno,"
			+" E.year_type,"
			+" E.proj_no,"
			+" max(E.fst_amt_cond) as fst_amt_cond,"
			+" max(E.fst_purch_amt) as fst_purch_amt,"
			+" max(E.fst_row_cond) as fst_row_cond,"
			+" max(E.fst_purch_row) as fst_purch_row,"
			+" max(E.lst_tol_amt) as lst_tol_amt";
	daoTable  =" bil_bill A left join bil_contract B on A.contract_no =B.contract_no and A.contract_seq_no =B.contract_seq_no"
			+" 	join ("+tableE+") E on A.p_seqno =E.acno_p_seqno";
    whereStr  =" where 1=1"
			//--消費資料 六大本金類--
			+" AND A.acct_code in (decode(E.fst_acct_code_bl,'Y','BL','XX'),"
			+"      	decode(E.fst_acct_code_it,'Y','IT','XX'),"
			+"			decode(E.fst_acct_code_id,'Y','ID','XX'),"
			+"			decode(E.fst_acct_code_ca,'Y','CA','XX'),"
			+"			decode(E.fst_acct_code_ao,'Y','AO','XX'),"
			+"			decode(E.fst_acct_code_ot,'Y','OT','XX'))"
			+" AND ( A.sign_flag ='-' "
			+"       or ( (uf_tx_sign(A.txn_code) * A.dest_amt) >E.fst_one_low_amt))"
			//--** 消費資料 消費期間[:h_wday_this_acct_month]--
			+" AND acct_month between uf_month_add(?,1+E.fst_mm*-1) and ?"
			//--** 排除特店 ----
			+" AND ( E.fst_mcht ='0'"
			+"       or ( E.fst_mcht ='1'"
			+"            and A.mcht_no in (select data_code from cms_roadparm2_bn_data"
			+"                               where proj_no  = E.proj_no"
			+"                               and data_type = '0'||E.year_type))"
			+"       or ( E.fst_mcht ='2'"
         +"            and a.mcht_no not in (select data_code from cms_roadparm2_bn_data"
			+"												where proj_no  = E.proj_no"
         +"                                  and data_type = '0'||E.year_type))"
         +"      )"
			 +" AND A.ecs_cus_mcht_no NOT IN (" +
			" SELECT data_code FROM MKT_MCHTGP_DATA" +
			" WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
			" )"
         +" GROUP BY E.acno_p_seqno,E.year_type,E.proj_no"
			;
	
    setString(1, hWdayThisAcctMonth);
    setString(2, hWdayThisAcctMonth);
	
	int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {

      hTempDataCnt = getValueInt("bill_cnt", i);
      hBillDestAmt = getValueDouble("dest_amt", i);
      hCrltAcnoPSeqno = getValue("acno_p_seqno", i);
      hCrltYearType = getValue("year_type", i);
      hCrltProjNo = getValue("proj_no", i);
      hRdp2FstAmtCond = getValue("fst_amt_cond", i);
      hRdp2FstPurchAmt = getValueDouble("fst_purch_amt", i);
      hRdp2FstRowCond = getValue("fst_row_cond", i);
      hRdp2FstPurchRow = getValueInt("fst_purch_row", i);
      hRdp2LstTolAmt = getValueDouble("lst_tol_amt", i);
      
      hCrltProcFlag ="1";
      hMcceConsumeBlAmt = 0;

      if ( (eq(hRdp2FstAmtCond,"Y") && hBillDestAmt >= hRdp2FstPurchAmt)
      		|| (eq(hRdp2FstRowCond,"Y") && hTempDataCnt >= hRdp2FstPurchRow) ) {
      	hCrltProcFlag ="Y";
      	if ( eq(hCrltYearType,"2") && hRdp2LstTolAmt >0) 
      		hCrltProcFlag ="2"; 
      }
      
      updateCmsRoadlist2();
      totalCnt++;
      if ((totalCnt % 1000)==0) 
      	printf("Process record [%s]",totalCnt);
	}

}

//==****************************************************************************
void selectMktCardConsume1() throws Exception {
	String tableE="SELECT c.acno_p_seqno,"
		   +" c.year_type, c.proj_no,"
		   +" max(d.amt_sum_flag) as amt_sum_flag,"
		   +" max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond,"
		   +" max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm,"
		   +" max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as fst_acct_code_bl,"
		   +" max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as fst_acct_code_it,"
		   +" max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as fst_acct_code_ca,"
		   +" max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as fst_acct_code_id,"
		   +" max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as fst_acct_code_ao,"
		   +" max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as fst_acct_code_ot,"
		   +" max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt,"
		   +" max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond," 
		   +" max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt," 
		   +" max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond," 
		   +" max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row,"
		   +" max(d.lst_tol_amt) as lst_tol_amt"
		   +" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
		   +" WHERE  D.amt_sum_flag = '1'"
		   +" AND    C.year_type    = '2'"
		   +" AND    C.proc_flag    = '2'"
		   +" GROUP BY c.acno_p_seqno,c.year_type,c.proj_no"
		   ;
    selectSQL =" sum(decode(e.fst_acct_code_bl,'Y',consume_bl_amt,0)+"
			+" decode(e.fst_acct_code_it,'Y',consume_it_amt,0)+"
			+" decode(e.fst_acct_code_id,'Y',consume_id_amt,0)+"
			+" decode(e.fst_acct_code_ca,'Y',consume_ca_amt,0)+"
			+" decode(e.fst_acct_code_ao,'Y',consume_ao_amt,0)+"
			+" decode(e.fst_acct_code_ot,'Y',consume_ot_amt,0)) as consume_amt,"
			+" e.acno_p_seqno,"
			+" e.year_type,"
			+" e.proj_no,"
			+" max(e.lst_tol_amt) as lst_tol_amt";
	daoTable  =" mkt_card_consume A join ("+tableE+") E on A.p_seqno =E.acno_p_seqno";
    whereStr  =" where  A.acct_month like ? "	//:h_temp_last_year||'01' and :h_temp_last_year||'12'
			+" GROUP BY e.acno_p_seqno,e.year_type,e.proj_no"
			;
	
    setString(1, hTempLastYear +"%");
	
	int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {

      hMcceConsumeBlAmt = getValueDouble("consume_amt", i);
      hCrltAcnoPSeqno = getValue("acno_p_seqno", i);
      hCrltYearType = getValue("year_type", i);
      hCrltProjNo = getValue("proj_no", i);
      hRdp2LstTolAmt = getValueDouble("lst_tol_amt", i);
      hTempDataCnt = 0;
      hBillDestAmt = 0;

      hCrltProcFlag ="3";

      if (hMcceConsumeBlAmt >= hRdp2LstTolAmt) 
      	hCrltProcFlag ="Y";

      updateCmsRoadlist2();
      totalCnt++;
      if ((totalCnt % 1000)==0) 
      	printf("Process record [%s]",totalCnt);
	}
	
}

//==****************************************************************************
void selectBilBill2() throws Exception {
	String tableE="SELECT c.acno_p_seqno,"
			+" c.major_card_no,"
			+" c.year_type,"
			+" c.proj_no as proj_no,"
			+" max(d.amt_sum_flag) as amt_sum_flag,"
			+" max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond,"
			+" max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as fst_acct_code_bl,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as fst_acct_code_it,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as fst_acct_code_ca,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as fst_acct_code_id,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as fst_acct_code_ao,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as fst_acct_code_ot,"
			+" max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt,"
			+" max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt,"
			+" max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row,"
			+" max(d.lst_tol_amt) as lst_tol_amt,"
			+" max(decode(c.year_type,'1',d.fst_mcht,'2',d.lst_mcht,'3',d.cur_mcht)) as fst_mcht "
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+" WHERE  D.amt_sum_flag = '2'"
			+" AND    C.proc_flag    = '0'"
			+" AND    C.year_type <= '3'"
			+" GROUP BY C.acno_p_seqno,C.major_card_no,C.year_type,C.proj_no";
	
    selectSQL =" sum(decode(A.sign_flag,'-',0,1)) as bill_cnt,"
			+" sum(decode(a.acct_code,'IT',"
			+" decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),"
			+" decode(A.sign_flag,'-',-1,1) * A.dest_amt)) as dest_amt,"
			+" e.major_card_no,"
			+" e.year_type,"
			+" e.proj_no,"
			+" max(e.fst_amt_cond) as fst_amt,"
			+" max(e.fst_purch_amt) as fst_purch_amt,"
			+" max(e.fst_row_cond) as fst_row_cond,"
			+" max(e.fst_purch_row) as fst_purch_row,"
			+" max(e.lst_tol_amt) as lst_tol_amt";
	daoTable  =" bil_bill a Left Join bil_contract b on A.contract_no =B.contract_no"
			+" join ("+tableE+") E on A.p_seqno =E.acno_p_seqno and A.major_card_no =E.major_card_no";
    whereStr  =" where 1=1"
			//--** 消費資料 六大本金類 --
			+" AND   A.acct_code in (decode(e.fst_acct_code_bl,'Y','BL','XX'),"
			+"           decode(e.fst_acct_code_it,'Y','IT','XX'),"
			+"           decode(e.fst_acct_code_id,'Y','ID','XX'),"
			+"           decode(e.fst_acct_code_ca,'Y','CA','XX'),"
			+"           decode(e.fst_acct_code_ao,'Y','AO','XX'),"
			+"           decode(e.fst_acct_code_ot,'Y','OT','XX'))"
			//--** 消費資料 最低單筆金額 **--
			+" AND  ( A.sign_flag ='-'"
			+"      or (A.sign_flag='+' and A.dest_amt >e.fst_one_low_amt) )"
			//--** 消費資料 消費期間 **-:h_wday_this_acct_month-
			+" AND   acct_month between uf_month_add(?,1+e.fst_mm*-1) and ?"
			//--** 排除特店 **--
			+" AND  ( E.fst_mcht = '0'"
			+"       or (E.fst_mcht ='1' and a.mcht_no in ("
			+"            select data_code from cms_roadparm2_bn_data"
			+"            where proj_no  = e.proj_no and data_type = '0'||e.year_type) )"
			+"       or (E.fst_mcht ='2' and a.mcht_no not in ("
			+"            select data_code from cms_roadparm2_bn_data"
			+"            where proj_no  = e.proj_no and data_type = '0'||e.year_type)) )"
			 +" AND A.ecs_cus_mcht_no NOT IN (" +
			 " SELECT data_code FROM MKT_MCHTGP_DATA" +
			 " WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
			 " )"
			+" GROUP BY e.major_card_no,e.year_type,e.proj_no"
			;

    setString(1, hWdayThisAcctMonth);
    setString(2, hWdayThisAcctMonth);
	
	int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {

      hTempDataCnt = getValueInt("bill_cnt", i);
      hBillDestAmt = getValueDouble("dest_amt", i);
      hCrltMajorCardNo = getValue("major_card_no", i);
      hCrltYearType = getValue("year_type", i);
      hCrltProjNo = getValue("proj_no", i);
      hRdp2FstAmtCond = getValue("fst_amt_cond", i);
      hRdp2FstPurchAmt = getValueDouble("fst_purch_amt", i);
      hRdp2FstRowCond = getValue("fst_row_cond", i);
      hRdp2FstPurchRow = getValueInt("fst_purch_row", i);
      hRdp2LstTolAmt = getValueDouble("lst_tol_amt", i);

      hCrltProcFlag ="1";
      hMcceConsumeBlAmt = 0;

      if ( (eq(hRdp2FstAmtCond,"Y") && hBillDestAmt >= hRdp2FstPurchAmt) ||
      		(eq(hRdp2FstRowCond,"Y") && hTempDataCnt >= hRdp2FstPurchRow) ) {
      	hCrltProcFlag ="Y";
      	if ( eq(hCrltYearType,"2") && hRdp2LstTolAmt >0) 
      		hCrltProcFlag ="2"; 
      }

      selectCrdCard1();
      updateCmsRoadlist3();
      totalCnt++;
      if ((totalCnt % 1000)==0) 
      	printf("Process record [%s]",totalCnt);
	}
	
}
//==****************************************************************************
void selectBilBill3() throws Exception {
	String tableE="SELECT c.acno_p_seqno,"
			+" c.card_no,"
			+" c.year_type,"
			+" c.proj_no as proj_no,"
			+" max(d.amt_sum_flag) as amt_sum_flag,"
			+" max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond,"
			+" max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as fst_acct_code_bl,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as fst_acct_code_it,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as fst_acct_code_ca,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as fst_acct_code_id,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as fst_acct_code_ao,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as fst_acct_code_ot,"
			+" max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt,"
			+" max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt,"
			+" max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row,"
			+" max(d.lst_tol_amt) as lst_tol_amt,"
			+" max(decode(c.year_type,'1',d.fst_mcht,'2',d.lst_mcht,'3',d.cur_mcht)) as fst_mcht "
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+" WHERE  D.amt_sum_flag = '3'"
			+" AND    C.proc_flag    = '0'"
			+" AND    C.year_type <= '3'"
			+" GROUP BY C.acno_p_seqno,C.card_no,C.year_type,C.proj_no";
	
    selectSQL =" sum(decode(A.sign_flag,'-',0,1)) as bill_cnt,"
			+" sum(decode(a.acct_code,'IT',"
			+" decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),"
			+" decode(A.sign_flag,'-',-1,1) * A.dest_amt)) as dest_amt,"
			+" e.card_no,"
			+" e.year_type,"
			+" e.proj_no,"
			+" max(e.fst_amt_cond) as fst_amt,"
			+" max(e.fst_purch_amt) as fst_purch_amt,"
			+" max(e.fst_row_cond) as fst_row_cond,"
			+" max(e.fst_purch_row) as fst_purch_row,"
			+" max(e.lst_tol_amt) as lst_tol_amt";
    daoTable  =" bil_bill a Left Join bil_contract b on A.contract_no =B.contract_no"
			+" join ("+tableE+") E on A.p_seqno =E.acno_p_seqno and A.card_no =E.card_no";
    whereStr  =" where 1=1"
			//--** 消費資料 六大本金類 --
			+" AND   A.acct_code in (decode(e.fst_acct_code_bl,'Y','BL','XX'),"
			+"           decode(e.fst_acct_code_it,'Y','IT','XX'),"
			+"           decode(e.fst_acct_code_id,'Y','ID','XX'),"
			+"           decode(e.fst_acct_code_ca,'Y','CA','XX'),"
			+"           decode(e.fst_acct_code_ao,'Y','AO','XX'),"
			+"           decode(e.fst_acct_code_ot,'Y','OT','XX'))"
			//--** 消費資料 最低單筆金額 **--
			+" AND  ( A.sign_flag ='-'"
			+"      or (A.sign_flag='+' and A.dest_amt >e.fst_one_low_amt) )"
			//--** 消費資料 消費期間 **-:h_wday_this_acct_month-
			+" AND   acct_month between uf_month_add(?,1+e.fst_mm*-1) and ?"
			//--** 排除特店 **--
			+" AND  ( E.fst_mcht = '0'"
			+"       or (E.fst_mcht ='1' and a.mcht_no in ("
			+"            select data_code from cms_roadparm2_bn_data"
			+"            where proj_no  = e.proj_no and data_type = '0'||e.year_type) )"
			+"       or (E.fst_mcht ='2' and a.mcht_no not in ("
			+"            select data_code from cms_roadparm2_bn_data"
			+"            where proj_no  = e.proj_no and data_type = '0'||e.year_type)) )"
			 +" AND A.ecs_cus_mcht_no NOT IN (" +
			 " SELECT data_code FROM MKT_MCHTGP_DATA" +
			 " WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
			 " )"
			+" GROUP BY e.card_no,e.year_type,e.proj_no"
			;
    setString(1, hWdayThisAcctMonth);
    setString(2, hWdayThisAcctMonth);
	
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {

    hTempDataCnt = getValueInt("bill_cnt", i);
    hBillDestAmt = getValueDouble("dest_amt", i);
    hCrltCardNo = getValue("card_no", i);
    String a1 = getValue("year_type", i);
    hCrltYearType = getValue("year_type", i);
    hCrltProjNo = getValue("proj_no", i);
    hRdp2FstAmtCond = getValue("fst_amt_cond", i);
    hRdp2FstPurchAmt = getValueDouble("fst_purch_amt", i);
    hRdp2FstRowCond = getValue("fst_row_cond", i);
    hRdp2FstPurchRow = getValueInt("fst_purch_row", i);
    hRdp2LstTolAmt = getValueDouble("lst_tol_amt", i);

    hCrltProcFlag ="1";
    hMcceConsumeBlAmt = 0;

    if ( (eq(hRdp2FstAmtCond,"Y") && hBillDestAmt >= hRdp2FstPurchAmt) ||
    		(eq(hRdp2FstRowCond,"Y") && hTempDataCnt >= hRdp2FstPurchRow) ) {
    	hCrltProcFlag ="Y";
    	if ( eq(hCrltYearType,"2") && hRdp2LstTolAmt >0) 
    		hCrltProcFlag ="2"; 
    }

    selectCrdCard13();
    updateCmsRoadlist33();
    totalCnt++;
    if ((totalCnt % 1000)==0) 
    	printf("Process record [%s]",totalCnt);
	}
	

}
//==============================================================================
void selectMktCardConsume3() throws Exception {
	String tableE="SELECT c.acno_p_seqno,"
			+" c.card_no,"
			+" c.year_type,"
			+" c.proj_no,"
			+" max(d.amt_sum_flag) as amt_sum_flag,"
			+" max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond,"
			+" max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as fst_acct_code_bl,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as fst_acct_code_it,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as fst_acct_code_ca,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as fst_acct_code_id,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as fst_acct_code_ao,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as fst_acct_code_ot,"
			+" max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt,"
			+" max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt,"
			+" max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row,"
			+" max(d.lst_tol_amt) as lst_tol_amt"
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+"	WHERE  D.amt_sum_flag = '3'"
			+"	AND    C.year_type    = '2'"
			+"	AND    C.proc_flag    = '2'"
			+"	GROUP BY c.acno_p_seqno,c.card_no,c.year_type,c.proj_no"
			;
	//------------------------
    selectSQL =" sum(decode(e.fst_acct_code_bl,'Y',consume_bl_amt,0)+"
			+" decode(e.fst_acct_code_it,'Y',consume_it_amt,0)+"
			+" decode(e.fst_acct_code_id,'Y',consume_id_amt,0)+"
			+" decode(e.fst_acct_code_ca,'Y',consume_ca_amt,0)+"
			+" decode(e.fst_acct_code_ao,'Y',consume_ao_amt,0)+"
			+" decode(e.fst_acct_code_ot,'Y',consume_ot_amt,0)) as consume_amt,"
			+" e.card_no, e.year_type, e.proj_no,"
			+" max(e.lst_tol_amt) as lst_tol_amt";
	daoTable  =" mkt_card_consume a join ("+tableE+") E on A.card_no =E.card_no";
    whereStr  =" where  1=1"
			+" AND    a.acct_month like ?" //:h_temp_last_year||'01' and :h_temp_last_year||'12'
			+" GROUP BY e.card_no,e.year_type,e.proj_no"
			;
    setString(1, hTempLastYear +"%");
	
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {

    hMcceConsumeBlAmt = getValueDouble("consume_amt", i);
    hCrltCardNo = getValue("card_no", i);
    hCrltYearType = getValue("year_type", i);
    hCrltProjNo = getValue("proj_no", i);
    hRdp2LstTolAmt = getValueDouble("lst_tol_amt", i);
    
    hTempDataCnt = 0;
    hBillDestAmt = 0;
    hCrltProcFlag ="3";

    if (hMcceConsumeBlAmt >= hRdp2LstTolAmt) 
    	hCrltProcFlag ="Y";

    selectCrdCard13();
    updateCmsRoadlist33();
    totalCnt++;
    if ((totalCnt % 1000)==0) 
    	printf("Process record [%s]",totalCnt);		
	}

}

//==============================================================================
void selectCrdCard1() throws Exception {
	String lsCardNo = hCrltMajorCardNo;

	if (tiCard1 <=0) {
		sqlCmd ="select end_card_no from crd_card"
				+" where card_no =?";
		tiCard1 =ppStmtCrt("ti_card1","");
	}
	ppp(1,lsCardNo);
	
	sqlSelect(tiCard1);
	if (sqlNrow <=0) {
		sqlerr("select_crd_card_1 error, kk="+lsCardNo);
		errExit(1);
	}
	
	hCrltMajorCardNo =colSs("end_card_no");
}
//==============================================================================
void selectCrdCard13() throws Exception {
	String lsCardNo = hCrltCardNo;

	if (tiCard13 <=0) {
		sqlCmd ="select end_card_no from crd_card"
				+" where card_no =?";
		tiCard13 =ppStmtCrt("ti_card1_3","");
	}
	ppp(1,lsCardNo);
	
	sqlSelect(tiCard13);
	if (sqlNrow <=0) {
		sqlerr("select_crd_card_1 error, kk="+lsCardNo);
		errExit(1);
	}
	
	hCrltCardNo =colSs("end_card_no");
}

//==============================================================================
void updateCmsRoadlist3() throws Exception {
	if (tiList3U <=0) {
		sqlCmd ="update cms_roadlist set"
				+" proc_flag =?,"
				+" proc_date =?,"
				+" purch_amt =purch_amt +?,"
				+" purch_row =purch_row+?,"
				+" tol_amt =tol_amt +?,"
				+" mod_time =sysdate,"
				+" mod_pgm =?"
				+" where major_card_no =?"
				+" and major_card_no =end_card_no"
				+" and year_type =?"
				+" and proj_no =?";
		tiList3U =ppStmtCrt("ti_list3_U","");
	}
	
	ppp(1, hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hBillDestAmt);
	ppp(hTempDataCnt);
	ppp(hMcceConsumeBlAmt);
	ppp(hModPgm);
	//--
	ppp(hCrltMajorCardNo);
	ppp(hCrltYearType);
	ppp(hCrltProjNo);
	if (sqlNrow <=0) {
      sqlerr("update cms_roadlist_3 error, kk="+ hCrltMajorCardNo);
      errExit(1);
   }
}
//==============================================================================
void updateCmsRoadlist33() throws Exception {
	if (tiList3U3 <=0) {
		sqlCmd ="update cms_roadlist set"
				+" proc_flag =?,"
				+" proc_date =?,"
				+" purch_amt =purch_amt +?,"
				+" purch_row =purch_row+?,"
				+" tol_amt =tol_amt +?,"
				+" mod_time =sysdate,"
				+" mod_pgm =?"
				+" where card_no =?"
				+" and card_no =end_card_no"
				+" and year_type =?"
				+" and proj_no =?";
		tiList3U3 =ppStmtCrt("ti_list3_U_3","");
	}
	
	ppp(1, hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hBillDestAmt);
	ppp(hTempDataCnt);
	ppp(hMcceConsumeBlAmt);
	ppp(hModPgm);
	//--
	ppp(hCrltCardNo);
	ppp(hCrltYearType);
	ppp(hCrltProjNo);
	if (sqlNrow <=0) {
    sqlerr("update cms_roadlist_3 error, kk="+ hCrltCardNo);
    errExit(1);
 }
}
//==============================================================================
void selectMktCardConsume2() throws Exception {
	String tableE="SELECT c.acno_p_seqno,"
			+" c.major_card_no,"
			+" c.year_type,"
			+" c.proj_no,"
			+" max(d.amt_sum_flag) as amt_sum_flag,"
			+" max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond,"
			+" max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as fst_acct_code_bl,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as fst_acct_code_it,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as fst_acct_code_ca,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as fst_acct_code_id,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as fst_acct_code_ao,"
			+" max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as fst_acct_code_ot,"
			+" max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt,"
			+" max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt,"
			+" max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond,"
			+" max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row,"
			+" max(d.lst_tol_amt) as lst_tol_amt"
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+"	WHERE  D.amt_sum_flag = '2'"
			+"	AND    C.year_type    = '2'"
			+"	AND    C.proc_flag    = '2'"
			+"	GROUP BY c.acno_p_seqno,c.major_card_no,c.year_type,c.proj_no"
			;
	//------------------------
    selectSQL =" sum(decode(e.fst_acct_code_bl,'Y',consume_bl_amt,0)+"
			+" decode(e.fst_acct_code_it,'Y',consume_it_amt,0)+"
			+" decode(e.fst_acct_code_id,'Y',consume_id_amt,0)+"
			+" decode(e.fst_acct_code_ca,'Y',consume_ca_amt,0)+"
			+" decode(e.fst_acct_code_ao,'Y',consume_ao_amt,0)+"
			+" decode(e.fst_acct_code_ot,'Y',consume_ot_amt,0)) as consume_amt,"
			+" e.major_card_no, e.year_type, e.proj_no,"
			+" max(e.lst_tol_amt) as lst_tol_amt";
	daoTable  =" mkt_card_consume a join ("+tableE+") E on A.major_card_no =E.major_card_no";
	whereStr  =" where  1=1"
			+" AND    a.acct_month like ?" //:h_temp_last_year||'01' and :h_temp_last_year||'12'
			+" GROUP BY e.major_card_no,e.year_type,e.proj_no"
			;
    setString(1, hTempLastYear +"%");
	
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {

      hMcceConsumeBlAmt = getValueDouble("consume_amt", i);
      hCrltMajorCardNo = getValue("major_card_no", i);
      hCrltYearType = getValue("year_type", i);
      hCrltProjNo = getValue("proj_no", i);
      hRdp2LstTolAmt = getValueDouble("lst_tol_amt", i);
      
      hTempDataCnt = 0;
      hBillDestAmt = 0;
      hCrltProcFlag ="3";

      if (hMcceConsumeBlAmt >= hRdp2LstTolAmt) 
      	hCrltProcFlag ="Y";

      selectCrdCard1();
      updateCmsRoadlist3();
      totalCnt++;
      if ((totalCnt % 1000)==0) 
      	printf("Process record [%s]",totalCnt);		
	}

}

//==============================================================================
void selectCmsRoaddetail() throws Exception {
	sqlCmd = " select "
		+ " max(rd_seqno) as db_rd_seqno "
		+ " from cms_roaddetail "
		+ " where rd_moddate = ? ";
	sqlSelect(sqlCmd, new Object[] {
		hBusiDate
	});

	if (sqlNrow < 0) {
		sqlerr("select cms_roaddetail error !! ");
		errExit(1);
	}

	if (sqlNrow == 0)
		hRodeRdSeqno = 0;
	else
	hRodeRdSeqno = colInt("db_rd_seqno");
}

//=*****************************************************************************
void selectCmsRoadlist4() throws Exception {
	String lsCardNo = "";

	sqlCmd = " select "
		+ " a.major_card_no , "
		+ " a.card_no , "
		+ " a.acno_p_seqno , "
		+ " decode(a.proc_flag,'Y','Y','N') as proc_flag , "
		+ " a.year_type , "
		+ " a.proj_no , "
		+ " a.purch_amt , "
		+ " a.purch_row , "
		+ " a.id_p_seqno , "
		+ " a.rds_pcard , "
		+ " a.tol_amt , "
		+ " B.card_note "
		+ " from cms_roadlist A join crd_card B on B.card_no =A.card_no "
		+ " where 1=1 "
		+ " and A.major_card_no = A.end_card_no "
		+ " order by 2 , 4 desc ";

	openCursor();
	while (fetchTable()) {
		hCrltMajorCardNo = colSs("major_card_no");
		hCrltCardNo = colSs("card_no");
		hCrltAcnoPSeqno = colSs("acno_p_seqno");
		hCrltProcFlag = colSs("proc_flag");
		hCrltYearType = colSs("year_type");
		hCrltProjNo = colSs("proj_no");
		hCrltPurchAmt = colInt("purch_amt");
		hCrltPurchRow = colInt("purch_row");
		hCrltIdPSeqno = colSs("id_p_seqno");
		hCrltRdsPcard = colSs("rds_pcard");
		hCrltTolAmt = colInt("tol_amt");
		hPctpCardNote = colSs("card_note");
		if (eq(lsCardNo, hCrltCardNo))
			continue;

		lsCardNo = hCrltCardNo;

		selectCmsRoadmaster();
		if (selectCmsRoadmaster0() == 0)
			insertCmsRoadmaster();
		insertCmsRoaddetail();
		totalCnt++;
	}
	closeCursor();
}

//=*****************************************************************************
void selectCmsRoadmaster() throws Exception {
	hRoad.rmCarno = "";
	hRoad.rmCarmanname = "";
	hRoad.rmCarmanid = "";
	if (eq(hPctpCardNote, "G"))
		return;
	if (eq(hCrltCardNo, hCrltMajorCardNo))
		return;

	if (tiMaster <=0) {
		sqlCmd = " select "
			+ " a.rm_carno , "
			+ " a.rm_carmanname , "
			+ " a_rm_carmanid , "
			+ " hex(a.rowid) as road_rowid , "
			+" hex(B.rowid) as card_rowid"
			+ " from cms_roadmaster A join crd_card B on B.card_no =A.card_no"
			+ " where 1=1 "
			+ " and a.rm_type = 'F' "
			+ " and ((a.rm_status <> 0') or (a.rm_status = '0' and a.rm_reason = '2')) "
			+ " and B.card_note = 'G' "
			+ " and b.card_no <> ? "
			+ " and b.acno_p_seqno = ? "
			+ " order by decode(a.rm_status,'0','1','2') Desc ";
		tiMaster =ppStmtCrt("ti_master","");
	}

	daoTid ="mast.";
	sqlSelect(tiMaster, new Object[] {
			hCrltCardNo, hCrltAcnoPSeqno
	});

	if (sqlNrow <= 0) {
		sqlerr("select_cms_roadmaster open 失敗, kk="+ hCrltCardNo);
		errExit(1);
	}

	int llNrow = sqlNrow;
	for (int ll=0; ll<llNrow; ll++) {
		hRoad.rmCarno = colSs("mast.rm_carno");
		hRoad.rmCarmanname = colSs("mast.rm_carmanname");
		hRoad.rmCarmanid = colSs("mast.a_rm_carmanid");
		hRoad.rowid = colSs("mast.road_rowid");
		hCard.rowid =colSs("mast.card_rowid");

		updateCmsRoadmaster1();
		insertCmsRoaddetail1();
	}
}

//=*****************************************************************************
void updateCmsRoadmaster1() throws Exception {
	if (tiMastU <=0) {
		sqlCmd = " update cms_roadmaster set "
			+ " rm_status = '0' , "
			+ " rm_reason = '5' , "
			+ " rm_moddate = ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where rowid = ? ";
		tiMastU =ppStmtCrt("ti_mast_U","");
	}

	ppp(1, hBusiDate);
	ppp(hModPgm);
	this.ppRowId(hRoad.rowid);

	sqlExec(tiMastU);
	if (sqlNrow <= 0) {
		sqlerr("update cms_roadmaster_1 error, kk="+ hCrltCardNo);
		errExit(1);
	}
}

//=*****************************************************************************
void insertCmsRoaddetail1() throws Exception {
	hRodeRdSeqno++;
	if (ttDetl1 ==null) {
		ttDetl1 =new com.SqlParm();
		sqlCmd = " insert into cms_roaddetail ( "
			+ " rd_moddate , " // 1
			+ " rd_seqno , "
			+ " rd_modtype , "
			+ " card_no , "
			+ " rd_type , " // 5
			+ " appl_card_no , "
			+ " group_code , "
			+ " rd_carno , "
			+ " rd_carmanname , "
			+ " rd_carmanid , " // 10
			+ " rd_htelno1 , "
			+ " rd_htelno2 , "
			+ " rd_htelno3 , "
			+ " rd_otelno1 , "
			+ " rd_otelno2 , " // 15
			+ " rd_otelno3 , "
			+ " cellar_phone , "
			+ " rd_validdate , "
			+ " rd_status , "
			+ " rd_stoprsn , " // 20
			+ " crt_user , "
			+ " crt_date , "
			+ " apr_user , "
			+ " apr_date , "
			+" mod_user, mod_time, mod_pgm, mod_seqno"
			+ " ) "
			+ " select "
			+ ttDetl1.kkk(" ?,","rd_moddate") // 1
			+ ttDetl1.kkk(" ?,","rd_seqno")
			+ " 'V' , "
			+ " b.major_card_no , "
			+ " 'F' , " // 5
			+ " b.card_no , "
			+ " b.group_code , "
			+ ttDetl1.kkk(" ?,","car_no")  //rd_carno
			+ ttDetl1.kkk(" uf_nvl(cast(? as vargraphic(20)),a.chi_name),","car_name")
			+ ttDetl1.kkk(" uf_val(cast(? as varchar(20)),a.id_no),","car_idno")
			+ " a.home_area_code1 , "
			+ " a.home_tel_no1 , "
			+ " a.home_tel_ext1 , "
			+ " a.office_area_code1 , "
			+ " a.office_tel_no1 , " // 15
			+ " a.office_tel_ext1 , "
			+ " a.cellar_phone , "
			+ " substr(b.new_end_date,1,6) , "
			+ " '0' , "	//rd_status
			+ " '5' , " //rd_stoprsn
			+ " 'CmsA014' , "
			+ " to_char(sysdate,'yyyymmdd') , "
			+ " 'CmsA014' , "
			+ " to_char(sysdate,'yyyymmdd') , "
			+ commSqlStr.modxxxInsert(hModUser, hModPgm)
			+ " from crd_idno A join crd_card B on A.id_p_seqno =B.id_p_seqno "
			+ " where 1=1"
			+ ttDetl1.kkk(" and B.rowid =?","kk_rowid");
		ttDetl1.sqlFrom =sqlCmd;
		ttDetl1.pfidx =ppStmtCrt("tt-detl1-A","");
	}
	ttDetl1.ppp("rd_moddate", hBusiDate);
	ttDetl1.ppp("rd_seqno", hRodeRdSeqno);
	ttDetl1.ppp("car_no", hRoad.rmCarno);
	ttDetl1.ppp("car_name", hRoad.rmCarmanname);
	ttDetl1.ppp("car_idno", hRoad.rmCarmanid);
	ttDetl1.setRowId("kk_rowid", hCard.rowid);

	sqlExec(ttDetl1.pfidx, ttDetl1.getConvParm());
	if (sqlNrow <= 0) {
		sqlerr("insert cms_roaddetail_1 error, kk="+ hCrltCardNo);
		errExit(1);
	}
}

//=*****************************************************************************
int selectCmsRoadmaster0() throws Exception {
	if (tiMaster0 <=0) {
		sqlCmd = " select count(*) as xx_cnt "
			+ " from cms_roadmaster "
			+ " where card_no = ? "
			+ " and rm_type = 'F' ";
		tiMaster0 =ppStmtCrt("ti_master_0","");
	}
	sqlSelect(tiMaster0, new Object[] {
			hCrltCardNo
	});

	if (sqlNrow < 0) {
		printf("select_cms_roadmaster_0 error, kk="+ hCrltCardNo);
		errExit(1);
	}
	if (sqlNrow == 0)
		return 0;
	return colInt("xx_cnt");
}

//=*****************************************************************************
int insertCmsRoadmaster() throws Exception {
	if (ttMast ==null) {
		ttMast =new com.SqlParm();
		sqlCmd = " insert into cms_roadmaster ( "
			+ " card_no , " // 1
			+ " rm_type , "
			+ " rm_carno , "
			+ " group_code , "
			+ " rm_carmanname , " // 5
			+ " rm_carmanid , "
			+ " rm_htelno1 , "
			+ " rm_htelno2 , "
			+ " rm_htelno3 , "
			+ " rm_otelno1 , " // 10
			+ " rm_otelno2 , "
			+ " rm_otelno3 , "
			+ " cellar_phone , "
			+ " rm_status , "
			+ " rm_validdate , "
			+ " rm_moddate , "
			+ " rm_reason , " // 15
			+ " crt_user, crt_date , "
			+" apr_user, apr_date, "
			+ " mod_user, mod_time, mod_pgm, mod_seqno"
			+ " ) "
			+ " select "
			+ " b.card_no , " // 1
			+ " 'F' , "
			+ ttMast.kkk(" ?,","car_no")
			+ " b.group_code , "
			+ ttMast.kkk(" uf_nvl(cast(? as vargraphic(20)),a.chi_name),","car_idname") // 5
			+ ttMast.kkk(" uf_nvl(cast(? as varchar(20)),a.id_no),","car_idno")
			+ " a.home_area_code1 , "
			+ " a.home_tel_no1 , "
			+ " a.home_tel_ext1 , "
			+ " a.office_area_code1 , " // 10
			+ " a.office_tel_no1 , "
			+ " a.office_tel_ext1 , "
			+ " a.cellar_phone , "
			+ ttMast.kkk(" decode(cast(? as varchar(1)),'Y','1','0'),","rm_status")
			+ " substr(b.new_end_date,1,6) , "
			+ ttMast.kkk(" ?,","rm_moddate")
			+ ttMast.kkk(" decode(cast(? as varchar(1)),'Y','','2'),","rm_reason")
			+" 'CmsA014', "+commSqlStr.sysYYmd+","	//crt-xxx
			+" 'CmsA014', "+commSqlStr.sysYYmd+","	//apr-xxx
			+commSqlStr.modxxxInsert(hModUser, hModPgm)
			+ " from crd_idno A join crd_card B on A.id_p_seqno =B.id_p_seqno"
			+ ttMast.kkk(" where b.card_no = ?","kk_card_no");
		ttMast.sqlFrom =sqlCmd;
		ttMast.pfidx =ppStmtCrt("tt-mast-A","");
	}

	ttMast.ppp("car_no", hRoad.rmCarno);
	ttMast.ppp("car_idname", hRoad.rmCarmanname);
	ttMast.ppp("car_idno", hRoad.rmCarmanid);
	ttMast.ppp("rm_status", hCrltProcFlag);
	ttMast.ppp("rm_moddate",hBusiDate);
	ttMast.ppp("rm_reason", hCrltProcFlag);
	ttMast.ppp("kk_card_no", hCrltCardNo);

	sqlExec(ttMast.pfidx, ttMast.getConvParm());

	if (sqlNrow < 0) {
		sqlerr("insert cms_roadmaster error, kk="+ hCrltCardNo);
		errExit(1);
	}
	
	if (sqlNrow == 0)
		return 1;
	return 0;
}

//=*****************************************************************************
void insertCmsRoaddetail() throws Exception {
	hRodeRdSeqno++;

	if (ttDetl ==null) {
		ttDetl =new com.SqlParm();
		sqlCmd = "insert into cms_roaddetail ("
				+" rd_moddate , "
				+" rd_seqno , "
				+" rd_modtype , "
				+" card_no , "
				+" rd_type , "
				+" appl_card_no , "
				+" group_code , "
				+" rd_carno , "
				+" rd_carmanname , "
				+" rd_carmanid , "
				+" rd_htelno1 , "
				+" rd_htelno2 , "
				+" rd_htelno3 , "
				+" rd_otelno1 , "
				+" rd_otelno2 , "
				+" rd_otelno3 , "
				+" cellar_phone , "
				+" rd_validdate , "
				+" rd_status , "
				+" rd_stoprsn , "
				+" crt_user , "
				+" crt_date , "
				+" apr_user , "
				+" apr_date , "
				+" proj_no , "
				+" purch_amt , "
				+" purch_cnt , "
				+" purch_amt_lyy , "
				+" cardholder_type , "
				+" rds_pcard , "
				+" id_p_seqno , "
				+" mod_user, mod_time, mod_pgm, mod_seqno"
				+ " ) "
				+ " select "
				+ ttDetl.kkk(" ?,","rm_moddate")
				+ ttDetl.kkk(" ?,","rd_seqno")
				+ " 'V' , "
				+ " B.major_card_no , "
				+ " 'F' , "
				+ " B.card_no , "
				+ " B.group_code , "
				+ ttDetl.kkk(" ?,","rd_carno")
				+ ttDetl.kkk(" uf_nvl(cast(? as vargraphic(20)),A.chi_name),","rd_carmanname")
				+ ttDetl.kkk(" uf_nvl(cast(? as char(20)),A.id_no),","rm_carmanid")
				+ " A.home_area_code1 , "
				+ " A.home_tel_no1 , "
				+ " A.home_tel_ext1 , "
				+ " A.office_area_code1 , "
				+ " A.office_tel_no1 , "
				+ " A.office_tel_ext1 , "
				+ " A.cellar_phone , "
				+ " substr(B.new_end_date,1,6) , "
				+ ttDetl.kkk(" ?,","rd_status")
				+ ttDetl.kkk(" ?,","rd_stoprsn")
				+ " 'CmsA014' , "
				+ " to_char(sysdate,'yyyymmdd') , "
				+ " 'CmsA014' , "
				+ " to_char(sysdate,'yyyymmdd') , "
				+ ttDetl.kkk(" ?,","proj_no")
				+ ttDetl.kkk(" ?,","purch_amt")
				+ ttDetl.kkk(" ?,","purch_cnt")
				+ ttDetl.kkk(" ?,","purch_amt_lyy")
				+ ttDetl.kkk(" ?,","cardholder_type")
				+ ttDetl.kkk(" ?,","rds_pcard")
				+ ttDetl.kkk(" ?,","id_p_seqno")
				+commSqlStr.modxxxInsert(hModUser, hModPgm)
				+ " from crd_idno A join crd_card B on A.id_p_seqno =B.id_p_seqno"
				+ ttDetl.kkk(" where B.card_no = ?","kk_card_no");
		ttDetl.sqlFrom =sqlCmd;		
		ttDetl.pfidx =ppStmtCrt("tt_detl-A","");
	}
	
	ttDetl.ppp("rm_moddate", hBusiDate);
	ttDetl.ppp("rd_seqno", hRodeRdSeqno);
	ttDetl.ppp("rd_carno", hRoad.rmCarno);
	ttDetl.ppp("rd_carmanname", hRoad.rmCarmanname);
	ttDetl.ppp("rm_carmanid", hRoad.rmCarmanid);
	if (eq(hCrltProcFlag,"Y")) {
		ttDetl.ppp("rd_status","1");
		ttDetl.ppp("rd_stoprsn","");
	}
	else {
		ttDetl.ppp("rd_status","0");
		ttDetl.ppp("rd_stoprsn","2");
	}
	ttDetl.ppp("proj_no", hCrltProjNo);
	ttDetl.ppp("purch_amt", hCrltPurchAmt);
	ttDetl.ppp("purch_cnt", hCrltPurchRow);
	ttDetl.ppp("purch_amt_lyy", hCrltTolAmt);
	if (eq(hCrltYearType,"1"))
		ttDetl.ppp("cardholder_type","A");
	else if (eq(hCrltYearType,"2"))
		ttDetl.ppp("cardholder_type","B");
	else if (eq(hCrltYearType,"3"))
		ttDetl.ppp("cardholder_type","C");
	else ttDetl.ppp("cardholder_type","D");
	ttDetl.ppp("rds_pcard", hCrltRdsPcard);
	ttDetl.ppp("id_p_seqno", hCrltIdPSeqno);
	ttDetl.ppp("kk_card_no", hCrltCardNo);
	sqlExec(ttDetl.pfidx, ttDetl.getConvParm());
	if (sqlNrow <= 0) {
		sqlerr("insert cms_roaddetail_1 error, kk="+ hCrltCardNo);
		errExit(1);
	}
}

}
