/** 每月檢核免費道路救援續用處理程式 
 * 2018-0709:	JH		test OK
 * 109/02/15  V1.00.01     Pino     customized for TCB
 *  109/12/07  V1.00.01    shiyuqi       updated for project coding standard   *
 *  2023-0413  V1.00.02		JH		排除一般消費(特店)
 * */
package Cms;

import com.SqlParm;
import com.BaseBatch;

public class CmsA013 extends BaseBatch {
private String progname = "每月檢核免費道路救援續用處理程式  2023-0413  V1.00.02 ";
//CommFunction comm = new CommFunction();
//CommRoutine comr = null;
//=========================================
hdata.CmsRoadmaster hRoad =new hdata.CmsRoadmaster();
hdata.CrdCard hCard =new hdata.CrdCard();

//----------------------------
private int hCmc3FstMm = 0;
private double hRodeRdSeqno = 0;
private String hCrltMajorId = "";
private String hCrltMajorCardNo = "";
private String hCrltCardNo = "";
private String hCrltAcnoPSeqno = "";
private String hCrltProjNo = "";
private String hCrltYearType = "";
private String hCrltRmStatus = "";
private String hCrltProcFlag = "";
private double hCrltPurchAmt = 0;
private double hCrltPurchRow = 0;
private double hCrltTolAmt = 0;
private String hRdp2FstAmtCond = "";
private double hRdp2FstPurchAmt = 0;
private String hRdp2FstRowCond = "";
private long hRdp2FstPurchRow = 0;
private double hRdp2LstTolAmt = 0;
private double hMcceConsumeBlAmt = 0;
private String hPctpRdsPcard = "";
private String isLastYyyy = "";
private int totalCnt = 0;
private int recordCnt = 0;
private int hiBillDataCnt = 0;
private int hBillDestAmt = 0;
private String hRdp2AcnoPSeqno;
private String hRdp2YearType;
private String hRdp2ProjNo;
//private String h_rdp2_amt_sum_flag;
//private String h_rdp2_fst_cond;
private String hRdp2AcctCodeBl;
private String hRdp2AcctCodeIt;
private String hRdp2AcctCodeCa;
private String hRdp2AcctCodeId;
private String hRdp2AcctCodeAo;
private String hRdp2AcctCodeOt;
private double hRdp2OneLowAmt;
//private double h_rdp2_lst_tot_amt;
private String hRdp2FstMcht;
private int hRdp2FstMm;

String isMajorIdno ="";
//===========================================
private SqlParm ttRdsdetl =null;
private SqlParm ttRoadlist =null;
//---------------------------------
private int tiRdsmastU =-1;
private int tiParm2Dtl =-1;
private int tiList2A =-1;
private int tiRoadparm2 =-1;
private int tiList1U =-1;
private int tiBill =-1;
private int tiBndata2 =-1;
private int tiList2U =-1;
private int tiList3U =-1;

int commit =1;
//=****************************************************************************
public static void main(String[] args) {
	CmsA013 proc = new CmsA013();
	
//	proc.debug = true;
//	proc.ddd_sql(true);
	proc.mainProcess(args);
	proc.systemExit();
}

//==============================================================================
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg =args.length;
	if (liArg > 1) {
		printf("Usage : CmsA013 [business_date, batch_seqno]");
		errExit(1);
	}
	
	dbConnect();
//	comr = new CommRoutine(getDBconnect(), getDBalias());

	if (args.length == 1) {
		setBusiDate(args[0]);
		callBatchSeqno(args[liArg -1]);
	}
	callBatch(0,0,0);

	if (!eq(commString.right(hBusiDate,2), "01")) {
		errmsg("本程式為每月一日執行");
		okExit(0);
	}

//	is_new_card_date = commDate.dateAdd(h_busi_date, 0, -12, 0);
	isLastYyyy = commDate.dateAdd(hBusiDate, 0, -12, 0).substring(0, 4);

	deleteCmsRoadlist();
	//-----------------------------------------
	totalCnt =0;
	recordCnt = 0;
	printf("基本資料篩選處理開始.....");
	selectCmsRoadmaster();
	printf("累計卡號筆數 : [%s] 名單筆數 : [%s]", totalCnt, recordCnt);
	
	//-----------------------------------------------------------------
	totalCnt = 0;
	printf("新增舊卡號資料處理開始.....");
	insertCmsRoadlist2();
	printf("累計卡號筆數 : [%s]", totalCnt);
	//-----------------------------------------------------------------
	totalCnt =0;
	recordCnt = 0;
	printf("專案方案設定(正卡ID)處理開始.....");
	selectCmsRpadlist1();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------
	totalCnt =0;
	recordCnt = 0;
	printf("當年度消費資格判斷(正卡帳戶)處理開始.....");
	selectBilBill1();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------
	totalCnt =0;
	recordCnt = 0;
	printf("以前年度消費資格判斷(正卡帳戶)處理開始.....");
	selectMktPostConsume1();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------
	printf("當年度消費資格判斷(正卡卡號)處理開始.....");
	printf("  刪除暫存檔(cms_a013_mcard)...");
	totalCnt = 0;
	deleteCmsa013Mcard();
	printf("  刪除暫存檔筆數 : [%s] ", totalCnt);
	printf("  新增暫存檔(cms_a013_mcard)...");
	totalCnt = 0;
	insertCmsA013Mcard();
	printf("  新增暫存檔筆數 : [%s] ", totalCnt);
	selectCmsA013Mcard();
	printf("  刪除暫存檔(cms_a013_bill)...");
	totalCnt = 0;
	deleteCmsA013Bill();
	printf("  刪除暫存檔筆數 : [%s] ", totalCnt);
	printf("  新增暫存檔(cms_a013_bill)...");
	totalCnt = 0;
	insertCmsA013Bill();
	printf("  新增暫存檔筆數 : [%s] ", totalCnt);
	totalCnt =0;
	recordCnt = 0;
	selectBilBill2();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------
	printf("以前年度消費資格判斷(正卡卡號)處理開始.....");
	totalCnt =0;
	recordCnt = 0;
	selectMktPostConsume2();
	printf("累計筆數 : [%s] ", totalCnt);
	///////-----------------------------------------------------------------
	printf("當年度消費資格判斷(正附卡分開計算)處理開始.....");
	printf("  刪除暫存檔(cms_a013_mcard)...");
	totalCnt = 0;
	deleteCmsa013Mcard();
	printf("  刪除暫存檔筆數 : [%s] ", totalCnt);
	printf("  新增暫存檔(cms_a013_mcard)...");
	totalCnt = 0;
	insertCmsA013Mcard3();
	printf("  新增暫存檔筆數 : [%s] ", totalCnt);
	selectCmsA013Mcard();
	printf("  刪除暫存檔(cms_a013_bill)...");
	totalCnt = 0;
	deleteCmsA013Bill();
	printf("  刪除暫存檔筆數 : [%s] ", totalCnt);
	printf("  新增暫存檔(cms_a013_bill)...");
	totalCnt = 0;
	insertCmsA013Bill3();
	printf("  新增暫存檔筆數 : [%s] ", totalCnt);
	totalCnt =0;
	recordCnt = 0;
	selectBilBill3();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------
	printf("以前年度消費資格判斷(正附卡分開計算)處理開始.....");
	totalCnt =0;
	recordCnt = 0;
	selectMktPostConsume3();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------
	selectCmsRoaddetail();
	totalCnt =0;
	recordCnt = 0;
	printf("救援資格異動(卡號)處理開始.....");
	selectCmsRoadlist4();
	printf("累計筆數 : [%s] ", totalCnt);
	//-----------------------------------------------------------------

	sqlCommit(commit);
	endProgram();
}

//==============================================================================
void deleteCmsRoadlist() throws Exception {
	sqlCmd = " delete cms_roadlist where 1=1 ";
	sqlExec(sqlCmd);

	if (sqlNrow < 0) {
		errmsg("delete cms_roadlist error");
		errExit(1);
	}
}

//==============================================================================
void selectCmsRoadmaster() throws Exception {
	sqlCmd = " select "
		+" uf_idno_id(b.major_id_p_seqno) as major_id ," 
		+ " b.major_id_p_seqno , "
		+ " b.p_seqno , "
		+ " b.major_card_no , "
		+ " b.card_no , "
		+ " b.acct_type , "
		+ " b.card_type , "
		+ " b.group_code , "
		+ " uf_corp_no(b.corp_p_seqno) as corp_no , " 
		+ " a.rm_status , "
		+ " a.rm_carno , "
		+ " a.rds_pcard , "
		+ " a.id_p_seqno , "
		+ " uf_nvl(c.rds_pcard,'N') as aa_rds_pcard "
		+ " from cms_roadmaster A join crd_card B on A.card_no =B.card_no"
		+ " 	join ptr_card_type C on C.card_type =B.card_type "
		+ " where 1=1"
		+ " and A.rm_type = 'F' "
		+ " and ( A.rm_status <>'0' "
		+ "       or (A.rm_status ='0' and A.rm_reason ='2') ) "
		+ " and a.never_check =''"
		;
	if (debug) {
		sqlCmd +=commSqlStr.rownum(5);
	}
	
	this.openCursor();
	while (fetchTable()) {
		isMajorIdno = colSs("major_id");
		hCard.majorIdPSeqno = colSs("major_id_p_seqno");
		hCard.pSeqno = colSs("p_seqno");
		hCard.majorCardNo = colSs("major_card_no");
		hCard.cardNo = colSs("card_no");
		hCard.acctType = colSs("acct_type");
		hCard.cardType = colSs("card_type");
		hCard.groupCode = colSs("group_code");
		hCard.corpNo = colSs("corp_no");
		hRoad.rmStatus = colSs("rm_status");
		hRoad.rmCarno = colSs("rm_carno");
		hRoad.rdsPcard = colSs("rds_pcard");
		hRoad.idPSeqno = colSs("id_p_seqno");
		hPctpRdsPcard = colSs("aa_rds_pcard");

		if ( eq(hPctpRdsPcard,"A") && empty(hRoad.rmCarno)) {
			hCrltMajorCardNo = hCard.majorCardNo;
			hCrltCardNo = hCard.cardNo;
			hCrltProcFlag = "7";
			updateCmsRoadmaster();
			insertCmsRoaddetail();
			continue;
		}
		
		selectCmsRoadparm2Dtl();
		totalCnt++;
	}
	closeCursor();
}

//==============================================================================
void updateCmsRoadmaster() throws Exception {
	String lsStatus = "", lsReason = "";
	
	if (eq(hCrltProcFlag, "Y")) {
		lsStatus = "1";
		lsReason = "";
	}
	else if (eq(hCrltProcFlag, "X")) {
		lsStatus = "0";
		lsReason = "3";
	}
	else if (eq(hCrltProcFlag, "7")) {
		lsStatus = "0";
		lsReason = "7";
	}
	else {
		lsStatus = "0";
		lsReason = "2";
	}

	if (tiRdsmastU <=0) {
		sqlCmd = " update cms_roadmaster set "
			+ " rm_status = ? , "
			+ " rm_reason = ? , "
			+ " rm_moddate = ? , "
			+modxxxSet()  //commSqlStr.modxxx_Set(h_mod_user, h_mod_pgm)
			+ " where card_no = ? "
			+ " and rm_type ='F' ";
		tiRdsmastU = ppStmtCrt("ti_rdsmast_U","");
	}
	ppp(1, lsStatus);
	ppp(lsReason);
	ppp(hBusiDate);
	ppp(hCrltCardNo);
	sqlExec(tiRdsmastU);

	if (sqlNrow <= 0) {
		errmsg("update cms_roadmaster error, kk[%s]", hCrltCardNo);
		errExit(1);
	}
}

//==============================================================================
void insertCmsRoaddetail() throws Exception {
	hRodeRdSeqno++;
	// --
	String lsStatus = "", lsStopdate = "", lsStoprsn = "", lsType = "";

	if (eq(hCrltProcFlag, "Y")) {
		lsStatus = "1";
	}
	else if (eq(hCrltProcFlag, "X")) {
		lsStatus = "0";
		lsStoprsn = "3";
		lsStopdate = hBusiDate;
	}
	else if (eq(hCrltProcFlag, "7")) {
		lsStatus = "0";
		lsStoprsn = "7";
		lsStopdate = hBusiDate;
	}
	else {
		lsStatus = "0";
		lsStoprsn = "2";
		lsStopdate = hBusiDate;
	}

	if (eq(hCrltYearType, "1")) {
		lsType = "A";
	}
	else if (eq(hCrltYearType, "2")) {
		lsType = "B";
	}
	else if (eq(hCrltYearType, "3")) {
		lsType = "C";
	}
	else {
		lsType = "D";
	}

	// --
	if (ttRdsdetl ==null) {
		ttRdsdetl =new com.SqlParm();
		sqlCmd = " insert into cms_roaddetail ( "
				+"  rd_moddate    "
				+", rd_seqno      "
				+", rd_modtype    "
				+", card_no       "
				//+", new_card_no   "
				+", rd_type       "
				+", appl_card_no  "
				+", group_code    "
				+", rd_carno      "
				+", rd_carmanname "
				+", rd_carmanid   "
				//+", rd_newcarno   "
				+", rd_htelno1    "
				+", rd_htelno2    "
				+", rd_htelno3    "
				+", rd_otelno1    "
				+", rd_otelno2    "
				+", rd_otelno3    "
				+", cellar_phone  "
				+", rd_validdate  "
				+", rd_status     "
				+", rd_payamt     "
				//+", rd_payno      "
				//+", rd_paydate    "
				+", rd_stopdate   "
				+", rd_stoprsn    "
				+", crt_user      "
				+", crt_date      "
				+", apr_user      "
				+", apr_date      "
				//+", rd_senddate   "
				//+", rd_sendsts    "
				//+", rd_sendyn     "
				//+", rd_sendadd    "
				//+", rd_sendstop   "
				+", proj_no       "
				+", purch_amt     "
				+", purch_cnt     "
				+", purch_amt_lyy "
				+", cardholder_type " 
				+", rds_pcard " 
				+", id_p_seqno " 
				+", mod_user, mod_time, mod_pgm, mod_seqno "
			+ " ) select "
			+ ttRdsdetl.kkk(" ?,","rd_moddate") // 1.busi_date
			+ ttRdsdetl.kkk(" ?,","rd_seqno")		//2.rd_seqno
			+ " 'B' ,"
			+ ttRdsdetl.kkk(" ?,","card_no")	//3.m_card_no
			//+tt_rdsdetl.kkk(" ?,","new_card_no") //4.card-no
			+ " 'F' ,"     //rd_type
			+ ttRdsdetl.kkk(" ?,","appl_card_no")
			+ " group_code , "
			+ " rm_carno , "
			+ " rm_carmanname , "
			+ " rm_carmanid , "
			//+ " rm_carno,"     //rd_newcarno
			+ " rm_htelno1 , "
			+ " rm_htelno2 , "
			+ " rm_htelno3 , "
			+ " rm_otelno1 , "
			+ " rm_otelno2 , "
			+ " rm_otelno3 , "
			+ " cellar_phone , "
			+ " rm_validdate ,"
			+ ttRdsdetl.kkk(" ?,","rd_status") // 5.status
			+ ttRdsdetl.kkk(" ?,","rd_payamt") // 
			//rd_payno
			//rd_paydate
			+ ttRdsdetl.kkk(" ?,","rd_stopdate") //6.stop-date
			+ ttRdsdetl.kkk(" ?,","rd_stoprsn") //7.stop-reason
			+ " 'SYSTEM' ,"   //crt_user
			+ ttRdsdetl.kkk(" ?,","crt_date")  //8.busi-date
			+ " 'SYSTEM' ,"
			+ ttRdsdetl.kkk(" ?,","apr_date") //9.busi-date
			//+", rd_senddate   "
			//+", rd_sendsts    "
			//+", rd_sendyn     "
			//+", rd_sendadd    "
			//+", rd_sendstop   "
			+ ttRdsdetl.kkk(" ?,","proj_no") //11.proj-no
			+ ttRdsdetl.kkk(" ?,","purch_amt") //12.purch-amt
			+ ttRdsdetl.kkk(" ?,","purch_cnt") //13.tol_amt
			+ ttRdsdetl.kkk(" ?,","purch_amt_type")  //14.year-type
			+ ttRdsdetl.kkk(" ?,","cardholder_type")
			+ " rds_pcard , "
			+ " id_p_seqno , "
			+commSqlStr.modxxxInsert(hModUser,hModPgm)
			+ " from cms_roadmaster "
			+ ttRdsdetl.kkk(" where card_no = ? ","kk1") // 15
			+ " and rm_type = 'F' ";
		ttRdsdetl.pfidx = ppStmtCrt("tt_rdsdetl-A","");
		ttRdsdetl.sqlFrom =sqlCmd;
	}
	
	ttRdsdetl.ppp("rd_moddate", hBusiDate);
	ttRdsdetl.ppp("rd_seqno", hRodeRdSeqno);
	ttRdsdetl.ppp("card_no", hCrltMajorCardNo);
	ttRdsdetl.ppp("appl_card_no", hCrltCardNo);
	ttRdsdetl.ppp("rd_status",lsStatus);
	ttRdsdetl.ppp("rd_payamt",0);
	ttRdsdetl.ppp("rd_stopdate",lsStopdate);
	ttRdsdetl.ppp("rd_stoprsn",lsStoprsn);
	ttRdsdetl.ppp("crt_date",hBusiDate);
	ttRdsdetl.ppp("apr_date",hBusiDate);
	ttRdsdetl.ppp("proj_no", hCrltProjNo);
	ttRdsdetl.ppp("purch_amt", hCrltPurchAmt);
	ttRdsdetl.ppp("purch_cnt", hCrltPurchRow);
	ttRdsdetl.ppp("purch_amt_type", hCrltTolAmt);
	ttRdsdetl.ppp("cardholder_type",lsType);
	ttRdsdetl.ppp("kk1", hCrltCardNo);

	sqlExec(ttRdsdetl.pfidx, ttRdsdetl.getConvParm());

	if (sqlNrow <= 0) {
		errmsg("insert cms_roaddetail error, kk[%s]", hCrltCardNo);
		errExit(1);
	}
}

//==============================================================================
void selectCmsRoadparm2Dtl() throws Exception {

	if (tiParm2Dtl <= 0) {
		sqlCmd = " select distinct a.proj_no "
			+ " from cms_roadparm2_dtl A join cms_roadparm2 B on A.proj_no =B.proj_no"
			+ " where 1=1"
			+ " and a.acct_type = ? "
			+ " and a.card_type = ? "
			+ " and decode(a.group_code,'',?,A.group_code) = ? "
			+ " and a.corp_no = ? "
			+ " and uf_nvl(b.valid_end_date,'99991231') > ? " 
			+ " and b.apr_flag = 'Y' ";
		tiParm2Dtl = this.ppStmtCrt("ti_parm2dtl","");
	}
	String a1 = hCard.acctType;
	String a2 = hCard.cardType;
	String a3 = hCard.groupCode;
	String a4 = hCard.groupCode;
	String a5 = hCard.corpNo;
	String a6 = hBusiDate;
	sqlSelect(tiParm2Dtl, new Object[] {
		hCard.acctType, 
		hCard.cardType, 
		hCard.groupCode, 
		hCard.groupCode, 
		hCard.corpNo, hBusiDate
	});
	if (sqlNrow<0) {
		sqlerr("select cms_roadparm2_dtl error");
		errExit(1);
	}

	int liNrow = sqlNrow;

	if (liNrow == 0) {
		hCrltMajorCardNo = hCard.majorCardNo;
		hCrltCardNo = hCard.cardNo;
		hCrltProcFlag = "N";
		if (eq(hRoad.rmStatus, hCrltProcFlag))
			return;
		updateCmsRoadmaster();
		insertCmsRoaddetail();
		return;
	}
	
	for (int ll=0; ll<liNrow; ll++) {
		hCrltProjNo = colSs(ll, "proj_no");
		insertCmsRoadlist();
		recordCnt++;
	}
}

//==============================================================================
void insertCmsRoadlist() throws Exception {
	if (ttRoadlist ==null) {
		ttRoadlist =new com.SqlParm();
		sqlCmd = " insert into cms_roadlist ("
			+ " major_id_p_seqno , " //1
			+ " acno_p_seqno , "
			+ " major_card_no , "
			+ " end_card_no , "
			+ " card_no , "	//5
			+ " proj_no , "
			+ " rm_status , "	//7
			+ " purch_amt , "
			+ " purch_row , "
			+ " tol_amt , "	//0
			+ " proc_flag , "	//N
			+ " mod_pgm , "	//8
			+ " mod_time ,"
			+ " id_p_seqno ,"
			+ " rds_pcard "
			+ " ) values ( "
			+ ttRoadlist.kkk(" ?,","maj_idp_seqno")
			+ ttRoadlist.kkk(" ?,","p_seqno")
			+ ttRoadlist.kkk(" ?,","maj_card_no")
			+ ttRoadlist.kkk(" ?,","end_card_no")
			+ ttRoadlist.kkk(" ?,","card_no")
			+ ttRoadlist.kkk(" ?,","proj_no")
			+ ttRoadlist.kkk(" ?,","rm_status")
			+ " 0, 0, 0, 'N', "
			+ ttRoadlist.kkk(" ?,","mod_pgm")
			+ " sysdate , "
			+ ttRoadlist.kkk(" ?,","id_p_seqno")
			+ ttRoadlist.kkk(" ?","rds_pcard")
			+" )";
		ttRoadlist.sqlFrom =sqlCmd;
		ttRoadlist.pfidx = ppStmtCrt("tt_roadlist","");
	}
	ttRoadlist.ppp("maj_idp_seqno", isMajorIdno);
	String a1 = hCard.pSeqno;
	String a2 = hCard.majorCardNo;
	String a3 = hCard.cardNo;
	String a4 = hRoad.rmStatus;
	String a5 = hRoad.idPSeqno;
	String a6 = hRoad.rdsPcard;
	ttRoadlist.ppp("p_seqno", hCard.pSeqno);
	ttRoadlist.ppp("maj_card_no", hCard.majorCardNo);
	ttRoadlist.ppp("end_card_no", hCard.majorCardNo);
	ttRoadlist.ppp("card_no", hCard.cardNo);
	ttRoadlist.ppp("proj_no", hCrltProjNo);
	ttRoadlist.ppp("rm_status", hRoad.rmStatus);
	ttRoadlist.ppp("mod_pgm", hModPgm);
	ttRoadlist.ppp("id_p_seqno", hRoad.idPSeqno);
	ttRoadlist.ppp("rds_pcard", hRoad.rdsPcard);
	
	sqlExec(ttRoadlist.pfidx, ttRoadlist.getConvParm());
	if (sqlNrow <= 0) {
		errmsg("insert cms_roadlist error, kk[%s]", hCard.cardNo);
		errExit(1);
	}
}

//==============================================================================
void insertCmsRoadlist2() throws Exception {
	if (tiList2A <=0) {
		sqlCmd = " insert into cms_roadlist ( "
			+ " major_card_no , "
			+ " card_no , "
			+ " major_id_p_seqno , "
			+ " acno_p_seqno , "
			+ " proj_no , "
			+ " year_type , "
			+ " rm_status , "
			+ " proc_flag , "
			+ " proc_date , "
			+ " purch_amt , "
			+ " purch_row , "
			+ " tol_amt , "
			+ " end_card_no , "
			+ " mod_time , "
			+ " mod_pgm , "
			+ " id_p_seqno , "
			+ " rds_pcard  "
			+ " ) select "
			+ " b.major_card_no , "
			+ " a.card_no , "
			+ " b.major_id_p_seqno , "
			+ " acno_p_seqno , "
			+ " proj_no , "
			+ " year_type , "
			+ " rm_status , "
			+ " proc_flag , "
			+ " proc_date , "
			+ " purch_amt , "
			+ " purch_row , "
			+ " tol_amt , "
			+ " a.end_card_no , "
			+ " mod_time , "
			+ " mod_pgm , "
			+ " id_p_seqno , "
			+ " rds_pcard "
			+ " from cms_roadlist a , ("
			+ " 	select min(major_card_no) as major_card_no , "
			+ " 	min(major_id_p_seqno) as major_id_p_seqno , "
			+ " 	end_card_no "
			+ " 	from crd_card "
			+ " 	group by end_card_no "
			+ " 	having count(*) > 1 "
			+ " 	and min(current_code) = '0' "
			+ " 	and min(card_no) = min(major_card_no)) b "
			+ " where a.end_card_no = b.end_card_no "
			+ " and a.major_card_no <> b.major_card_no ";
		tiList2A = this.ppStmtCrt("ti_list2_A","");
	}

	sqlExec(tiList2A);

	totalCnt = sqlNrow;
	if (sqlNrow <= 0) {
		errmsg("insert cms_roadlist_2 error");
		errExit(1);
	}
}

//==============================================================================
void selectCmsRpadlist1() throws Exception {
	this.fetchExtend = "m2.";
	sqlCmd = "select A.proj_no, "
		+ " A.acno_p_seqno "
		+ " from cms_roadlist A join cms_roadparm2 B on A.proj_no =B.proj_no "
		+ " where 1=1"
		+ " and uf_nvl(b.valid_end_date,'99991231') > ? " 
		+ " and b.apr_flag = 'Y' "
		+ " group by a.proj_no , a.acno_p_seqno ";
	
	ppp(1, hBusiDate);
	this.openCursor();
	while (fetchTable()) {
		hCrltProjNo = colSs("m2.proj_no");
		hCrltAcnoPSeqno = colSs("m2.acno_p_seqno");
		hCrltProcFlag = "0";

		// sprintf(tmpstr1,"%01d",select_crd_card());
		// str2var(h_crlt_year_type ,tmpstr1);
		selectCmsRoadparm2();
		if (this.ssComp(hCrltYearType,"3") > 0) {
			hCrltProcFlag = "X";
		}
			
		totalCnt++;
		updateCmsRoadlist1();
	}
	closeCursor();
}

//==============================================================================
void selectCmsRoadparm2() throws Exception {

	if (tiRoadparm2 <=0) {
		sqlCmd = " select "
			+ " decode(decode(cast(? as char(1)) ,'2',lst_tol_amt,0),0,'Y','2') as aa_proc_flag "
			+ " from cms_roadparm2 "
			+ " where proj_no = ? "
			+ " and decode(cast(? as char(1)) ,'1',fst_one_low_amt,'2',lst_one_low_amt,'3',cur_one_low_amt) = 0 "
			+ " and decode(cast(? as char(1)) ,'1',fst_purch_amt,'2',lst_purch_amt,'3',cur_purch_amt) = 0 "
			+ " and decode(cast(? as char(1)) ,'1',fst_purch_row,'2',lst_purch_row,'3',cur_purch_row) = 0 "
			;
		tiRoadparm2 = ppStmtCrt("ti_roadparm2","");
	}
	sqlSelect(tiRoadparm2, new Object[] {
			hCrltYearType,
			hCrltProjNo,
			hCrltYearType,
			hCrltYearType,
			hCrltYearType
	});

	if (sqlNrow < 0) {
		errmsg("select cms_roadparm2 error");
		errExit(1);
	}
	hCrltProcFlag = "0";
	if (sqlNrow > 0) {
		hCrltProcFlag = colSs("aa_proc_flag");
	}

}

//=============================================================================
void updateCmsRoadlist1() throws Exception {
	if (tiList1U <=0) {
		sqlCmd = "update cms_roadlist set "
			+ " year_type = ? , "
			+ " proc_flag = ? , "
			+ " proc_date = ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where acno_p_seqno = ? "
			+ " and proj_no = ? ";
		tiList1U =ppStmtCrt("ti_list1_U","");
	}
	
	ppp(1, hCrltYearType);
	ppp(hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hModPgm);
	ppp(hCrltAcnoPSeqno);
	ppp(hCrltProjNo);

	sqlExec(tiList1U);

	if (sqlNrow <= 0) {
		printf("update cms_roadlist_1 error, kk[%s,%s]", hCrltMajorId, hCrltProjNo);
		errExit(1);
	}
}

//=============================================================================
void selectBilBill1() throws Exception {

	fetchExtend = "EE.";
	sqlCmd = " select "
		+ " c.acno_p_seqno , "
		+ " c.year_type , "
		+ " c.proj_no , "
		+ " max(d.amt_sum_flag) as aa_sum_flag , "
		+ " max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as aa_cond , "
		+ " max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as aa_fst_mm , "
		+ " max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as acct_code_bl , "
		+ " max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as acct_code_it , "
		+ " max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as acct_code_ca , "
		+ " max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as acct_code_id , "
		+ " max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as acct_code_ao , "
		+ " max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as acct_code_ot , "
		+ " max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as aa_one_low_amt , "
		+ " max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as aa_amt_cond , "
		+ " max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as aa_purch_amt , "
		+ " max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as aa_row_cond , "
		+ " max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as aa_purch_row , "
		+ " max(d.lst_tol_amt) as aa_tol_amt , "
		+ " max(decode(c.year_type,'1',d.fst_mcht,'2',d.lst_mcht,'3',d.cur_mcht)) as aa_mcht "
		+ " from cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
		+ " where 1=1"
		+ " and d.amt_sum_flag = '1' "
		+ " and c.proc_flag = '0' "
		+ " and c.year_type <= '3' "
		+ " group by C.acno_p_seqno , C.year_type , C.proj_no ";

	dddSql();
	this.openCursor();

	while (fetchTable()) {
		hRdp2AcnoPSeqno = colSs("EE.acno_p_seqno");
		hRdp2YearType = colSs("EE.year_type");
		hRdp2ProjNo = colSs("EE.proj_no");
//		h_rdp2_amt_sum_flag =col_ss("EE.aa_sum_flag");
//		h_rdp2_fst_cond = col_ss("EE.aa_cond");
		hRdp2FstMm =colInt("EE.aa_fst_mm");
		hRdp2AcctCodeBl =colNvl("EE.acct_code_bl","N");
		hRdp2AcctCodeIt =colNvl("EE.acct_code_it","N");
		hRdp2AcctCodeCa =colNvl("EE.acct_code_ca","N");
		hRdp2AcctCodeId =colNvl("EE.acct_code_id","N");
		hRdp2AcctCodeAo =colNvl("EE.acct_code_ao","N");
		hRdp2AcctCodeOt =colNvl("EE.acct_code_ot","N");
		hRdp2OneLowAmt =colNum("EE.aa_one_low_amt");
		hRdp2FstAmtCond =colSs("EE.aa_amt_cond");
		hRdp2FstPurchAmt =colNum("EE.aa_purch_amt");
		hRdp2FstRowCond =colSs("EE.aa_row_cond");
		hRdp2FstPurchRow =colInt("EE.aa_purch_row");
//		h_rdp2_lst_tot_amt =col_num("EE.aa_tol_amt");
		hRdp2FstMcht =colSs("EE.aa_mcht");

		hBillDestAmt = 0;
		int liBillCnt =0;
		//-bil_bill+bil_contract-
		liBillCnt=selectBilBill();
		if (liBillCnt==0)
			continue;

		hCrltProcFlag = "1";
		hMcceConsumeBlAmt = 0;

		if ( (eq(hRdp2FstAmtCond,"Y") && hBillDestAmt >= hRdp2FstPurchAmt)
			|| (eq(hRdp2FstRowCond,"Y") && hiBillDataCnt >= hRdp2FstPurchRow) ) {
			hCrltProcFlag = "Y";
			if ( eq(hCrltYearType,"2") && hRdp2LstTolAmt >0 )
				hCrltProcFlag = "2";
		}

		updateCmsRoadlist2();
		totalCnt++;
		if ((totalCnt % 1000) == 0)
			printf("Process record [%s]", totalCnt);
	}
	
	closeCursor();
}

//==============================================================================
int selectBilBill() throws Exception {
	int liBillDataCnt =0;
	hBillDestAmt =0;
	
	if (tiBill <=0) {
		sqlCmd = " select "
				+ " decode(A.acct_code,'IT',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),"
				+ " case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end) as aa_dest_amt , "
			+ " a.acct_code , "
			+ " a.mcht_no , "
			+ " a.post_date "
			+ " from bil_bill a left join bil_contract b on "
			+ " a.contract_no = b.contract_no and a.contract_seq_no = b.contract_seq_no "
			+ " where 1=1 and a.p_seqno = ? "
			//-消費資料 消費期間--
			+" and post_date >= ? and post_date < ?"
			+" AND A.ecs_cus_mcht_no NOT IN (" +
				" SELECT data_code FROM MKT_MCHTGP_DATA" +
				" WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
				" )"
			;
		tiBill =ppStmtCrt("ti_bill","");
	}

	String lsDate1 =commDate.monthAdd(hBusiDate, 0 - hRdp2FstMm);
	ppp(1, hRdp2AcnoPSeqno);
	ppp(lsDate1);
	ppp(hBusiDate);
	daoTid ="bill.";
	sqlSelect(tiBill);
	int liNrow = sqlNrow;

	for (int ll=0; ll<liNrow; ll++) {
		// -消費資料 六大本金類-
		if (eq(hRdp2AcctCodeBl,"Y") && !colEq("bill.acct_code","BL"))
			continue;
		if (eq(hRdp2AcctCodeIt,"Y") && !colEq("bill.acct_code","IT"))
			continue;
		if (eq(hRdp2AcctCodeId,"Y") && !colEq("bill.acct_code","ID"))
			continue;
		if (eq(hRdp2AcctCodeCa,"Y") && !colEq("bill.acct_code","CA"))
			continue;
		if (eq(hRdp2AcctCodeAo,"Y") && !colEq("bill.acct_code","AO"))
			continue;
		if (eq(hRdp2AcctCodeOt,"Y") && !colEq("bill.acct_code","OT"))
			continue;
		// -消費資料 最低單筆金額-
		if (colNum("bill.aa_dest_amt") <= hRdp2OneLowAmt) {
			continue;
		}
		// -排除特店-
		int tid2=-1;
		if (strIN(hRdp2FstMcht,",1,2")) {
			if (tiBndata2 <=0) {
				sqlCmd = " select "
					+ " count(*) as db_cnt "
					+ " from cms_roadparm2_bn_data "
					+ " where proj_no = ? "
					+ " and data_type = ? "
					+ " and data_code = ? ";
				tiBndata2 =ppStmtCrt("ti_bndata2","");
			}
			
			sqlSelect(tiBndata2, new Object[] {
					hRdp2ProjNo, 
				"0" + hRdp2YearType,
				colSs("bill.mcht_no")
			});
			//-指定-
			if (eq(hRdp2FstMcht,"1") && colInt("db_cnt") <= 0)
				continue;
			//-排除-
			if (eq(hRdp2FstMcht,"2") && colInt("db_cnt") > 0)
				continue;
		}

		liBillDataCnt++;
		hBillDestAmt += colInt("dest_amt");
	}

	return liBillDataCnt;
}

//=============================================================================
void updateCmsRoadlist2() throws Exception {
	daoTable = "update_cms_roadlist_2-U2";
	int tid = this.getTableId();
	if (tiList2U <=0) {
		sqlCmd = " update cms_roadlist set "
			+ " proc_flag = ? , "
			+ " proc_date = ? , "
			+ " purch_amt = purch_amt + ? , "
			+ " purch_row = purch_row + ? , "
			+ " tol_amt = tol_amt + ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where acno_p_seqno = ? "
			+ " and year_type = ? "
			+ " and proj_no = ?  ";
		tiList2U =ppStmtCrt("ti_list2_U","");
	}
	
	ppp(1, hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hBillDestAmt);
	ppp(hiBillDataCnt);
	ppp(hMcceConsumeBlAmt);
	ppp(hModPgm);
	ppp(hCrltAcnoPSeqno);
	ppp(hCrltYearType);
	ppp(hCrltProjNo);

	sqlExec(tiList2U);

	if (sqlNrow <= 0) {
		errmsg("update cms_roadlist_2 error, kk[%s]", hCrltAcnoPSeqno);
		errExit(1);
	}
}

//==============================================================================
void selectMktPostConsume1() throws Exception {
	String tableE=""
			+"     SELECT c.acno_p_seqno, "
			+"            c.year_type, "
			+"            c.proj_no, "
			+"            max(d.amt_sum_flag) as amt_sum_flag, "
			+"            max(d.lst_cond) as fst_cond, "
			+"            max(d.lst_mm) as fst_mm, "
			+"            max(d.lst_acct_code_bl) as acct_code_bl, "
			+"            max(d.lst_acct_code_it) as acct_code_it, "
			+"            max(d.lst_acct_code_ca) as acct_code_ca, "
			+"            max(d.lst_acct_code_id) as acct_code_id, "
			+"            max(d.lst_acct_code_ao) as acct_code_ao, "
			+"            max(d.lst_acct_code_ot) as acct_code_ot, "
			+"            max(d.lst_one_low_amt) as fst_one_low_amt, "
			+"            max(d.lst_amt_cond) as fst_amt_cond, " 
			+"            max(d.lst_purch_amt) as fst_purch_amt, " 
			+"            max(d.lst_row_cond) as fst_row_cond, " 
			+"            max(d.lst_purch_row) as fst_purch_row, "
			+"            max(d.lst_tol_amt) as lst_tol_amt "
			+"     FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no "
			+"     WHERE  1=1 "
			+"     AND    d.amt_sum_flag = '1' "
			+"     AND    c.year_type    = '2' "
			+"     AND    c.proc_flag    = '2' "
			+"     GROUP BY c.acno_p_seqno,c.year_type,c.proj_no";

	fetchExtend = "post1.";
	sqlCmd = " SELECT e.acno_p_seqno, "
		+ "         e.year_type, "
		+ "         e.proj_no, "
		+ "         max(e.lst_tol_amt) as ee_tot_amt, "
		+ " sum(decode(e.acct_code_bl,'Y',A.consume_bl_amt,0)+ "
		+ "             decode(e.acct_code_it,'Y',A.consume_it_amt,0)+ "
		+ "             decode(e.acct_code_id,'Y',A.consume_id_amt,0)+ "
		+ "             decode(e.acct_code_ca,'Y',A.consume_ca_amt,0)+ "
		+ "             decode(e.acct_code_ao,'Y',A.consume_ao_amt,0)+ "
		+ "             decode(e.acct_code_ot,'Y',A.consume_ot_amt,0)) as aa_consu_amt "
		+ " FROM    mkt_post_consume A, ( "+ tableE+ " ) E"
		+ " where  a.p_seqno = e.acno_p_seqno "
		+ " AND    a.acct_month between ? and ?" // :is_last_yyyy||'01' and :is_last_yyyy||'12' 
		// AND a.consume_type = 'C': post_date計算
		+ " GROUP BY E.acno_p_seqno,E.year_type,E.proj_no ";

	ppp(1, isLastYyyy +"01");
	ppp(2, isLastYyyy +"12");

	this.openCursor();
	while (fetchTable()) {
		hMcceConsumeBlAmt = colInt("post1.aa_consu_amt");
		hCrltAcnoPSeqno = colSs("post1.acno_p_seqno");
		hCrltYearType = colSs("post1.year_type");
		hCrltProjNo = colSs("post1.proj_no");
		hRdp2LstTolAmt = colInt("post1.ee_tot_amt");
		hiBillDataCnt = 0;
		hBillDestAmt = 0;

		hCrltProcFlag = "3";

		if (hMcceConsumeBlAmt >= hRdp2LstTolAmt)
			hCrltProcFlag = "Y";

		updateCmsRoadlist2();
		totalCnt++;
		if ((totalCnt % 1000) == 0)
			printf("Process record [%s]", totalCnt);
	}
	this.closeCursor();
}

//=============================================================================
void deleteCmsa013Mcard() throws Exception {
	sqlCmd = " delete cms_a013_mcard where 1=1 ";
	sqlExec(sqlCmd);

	totalCnt = sqlNrow;

	if (sqlNrow < 0) {
		errmsg("delete cms_a013_mcard error");
		errExit(1);
	}
}

//=============================================================================
void insertCmsA013Mcard() throws Exception {
//	sqlCmd = " insert into cms_a013_mcard ( "
//			+ " p_seqno ,"
//			+ " major_card_no ,"
//			+ " end_card_no ,"
//			+ " year_type ,"
//			+ " proj_no ,"
//			+ " amt_sum_flag ,"
//			+ " fst_cond ,"
//			+ " fst_mm ,"
//			+ " acct_code_bl ,"
//			+ " acct_code_it ,"
//			+ " acct_code_ca ,"
//			+ " acct_code_id ,"
//			+ " acct_code_ao ,"
//			+ " acct_code_ot ,"
//			+ " fst_one_low_amt ,"
//			+ " fst_amt_cond ,"
//			+ " fst_purch_amt ,"
//			+ " fst_row_cond ,"
//			+ " fst_purch_row ,"
//			+ " lst_tol_amt ,"
//			+ " fst_mcht "
//			+ " ) "
//			;
	sqlCmd =""
			+ " select "
			+ " c.acno_p_seqno , "
			+ " c.major_card_no , "
			+ " c.end_card_no , "
			+ " c.year_type , "
			+ " c.proj_no , "
			+ " max(d.amt_sum_flag) as amt_sum_flag, "
			+ " max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond, "
			+ " max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as acct_code_bl, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as acct_code_it , "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as acct_code_ca , "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as acct_code_id, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as acct_code_ao, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as acct_code_ot , "
			+ " max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt , "
			+ " max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond , "
			+ " max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt , "
			+ " max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond , "
			+ " max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row , "
			+ " max(d.lst_tol_amt) as lst_tol_amt , "
			+ " max(decode(c.year_type,'1',d.fst_mcht,'2',d.lst_mcht,'3',d.cur_mcht)) as fst_mcht , "
			+ " c.id_p_seqno , "
			+ " c.rds_pcard "
			+ " from cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+ " where 1=1"
			+ " and d.amt_sum_flag = '2' "
			+ " and c.proc_flag = '0' "
			+ " and c.year_type <= '3' "
			+ " group by c.acno_p_seqno,c.end_card_no,c.major_card_no,c.year_type,c.proj_no,c.id_p_seqno,c.rds_pcard ";
	openCursor();
	while(fetchTable()) {
		totalCnt++;
		daoTable ="cms_a013_mcard";
		insertTable();
	}
	
	closeCursor();
//
//	if (sql_nrow <0) {
//		sqlerr("insert cms_a013_mcard error");
//		err_exit(1);
//	}
}
//=============================================================================
void insertCmsA013Mcard3() throws Exception {

	sqlCmd =""
			+ " select "
			+ " c.acno_p_seqno , "
			+ " c.card_no , "
			+ " c.end_card_no , "
			+ " c.year_type , "
			+ " c.proj_no , "
			+ " max(d.amt_sum_flag) as amt_sum_flag, "
			+ " max(decode(c.year_type,'1',d.fst_cond,'2',d.lst_cond,'3',d.cur_cond)) as fst_cond, "
			+ " max(decode(c.year_type,'1',d.fst_mm,'2',d.lst_mm,'3',d.cur_mm)) as fst_mm, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_bl,'2',d.lst_acct_code_bl,'3',d.cur_acct_code_bl)) as acct_code_bl, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_it,'2',d.lst_acct_code_it,'3',d.cur_acct_code_it)) as acct_code_it , "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_ca,'2',d.lst_acct_code_ca,'3',d.cur_acct_code_ca)) as acct_code_ca , "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_id,'2',d.lst_acct_code_id,'3',d.cur_acct_code_id)) as acct_code_id, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_ao,'2',d.lst_acct_code_ao,'3',d.cur_acct_code_ao)) as acct_code_ao, "
			+ " max(decode(c.year_type,'1',d.fst_acct_code_ot,'2',d.lst_acct_code_ot,'3',d.cur_acct_code_ot)) as acct_code_ot , "
			+ " max(decode(c.year_type,'1',d.fst_one_low_amt,'2',d.lst_one_low_amt,'3',d.cur_one_low_amt)) as fst_one_low_amt , "
			+ " max(decode(c.year_type,'1',d.fst_amt_cond,'2',d.lst_amt_cond,'3',d.cur_amt_cond)) as fst_amt_cond , "
			+ " max(decode(c.year_type,'1',d.fst_purch_amt,'2',d.lst_purch_amt,'3',d.cur_purch_amt)) as fst_purch_amt , "
			+ " max(decode(c.year_type,'1',d.fst_row_cond,'2',d.lst_row_cond,'3',d.cur_row_cond)) as fst_row_cond , "
			+ " max(decode(c.year_type,'1',d.fst_purch_row,'2',d.lst_purch_row,'3',d.cur_purch_row)) as fst_purch_row , "
			+ " max(d.lst_tol_amt) as lst_tol_amt , "
			+ " max(decode(c.year_type,'1',d.fst_mcht,'2',d.lst_mcht,'3',d.cur_mcht)) as fst_mcht , "
			+ " c.id_p_seqno , "
			+ " c.rds_pcard "
			+ " from cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+ " where 1=1"
			+ " and d.amt_sum_flag = '3' "
			+ " and c.proc_flag = '0' "
			+ " and c.year_type <= '3' "
			+ " group by c.acno_p_seqno,c.end_card_no,c.card_no,c.year_type,c.proj_no,c.id_p_seqno,c.rds_pcard ";
	openCursor();
	while(fetchTable()) {
		totalCnt++;
		daoTable ="cms_a013_mcard";
		insertTable();
	}
	
	closeCursor();

}

//=============================================================================
void selectCmsA013Mcard() throws Exception {
	hCmc3FstMm = 0;
	sqlCmd = " select max(fst_mm) as h_cmc3_fst_mm "
		+ " from cms_a013_mcard ";

	sqlSelect();
	if (sqlNrow <= 0) {
		errmsg("select cms_a013_mcard erpt error");
		errExit(1);
	}

	hCmc3FstMm = colInt("h_cmc3_fst_mm");
}

//=============================================================================
void deleteCmsA013Bill() throws Exception {
	sqlCmd = " delete cms_a013_bill where 1=1 ";
	sqlExec(sqlCmd);

	totalCnt = sqlNrow;

	if (sqlNrow < 0) {
		errmsg("delete cms_a013_bill erpt error");
		errExit(1);
	}
}

//=****************************************************************************
void insertCmsA013Bill() throws Exception {
	String lsPostDate = "";

	lsPostDate = commDate.monthAdd(hBusiDate, 0 - hCmc3FstMm)+"01";
	sqlCmd ="select "
		+ " a.p_seqno , "
		+ " major_card_no , "
		+ " decode(a.acct_code,'IT', "
		+ " decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0), "
		+ " (decode(A.sign_flag,'-',-1,1) * A.dest_amt) ) as dest_amt , "
		+ " a.acct_code , "
		+ " a.txn_code , "
		+ " a.post_date , "
		+ " a.mcht_no "
		+ " from bil_bill A left join bil_contract B on a.contract_no = a.contract_no"
		+ " where 1=1 "
		+ " and ( a.acct_code in ('BL','ID','CA','AO','OT') "
		+ "      or (a.acct_code = 'IT' and a.install_curr_term =1) )"
		+ " and post_date >= ? and post_date <= ? "
		+" and A.p_seqno in (select acno_p_seqno from cms_a013_mcard)"
			+" AND A.ecs_cus_mcht_no NOT IN (" +
			" SELECT data_code FROM MKT_MCHTGP_DATA" +
			" WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
			" )"
	;

	ppp(1, lsPostDate);
	ppp(2, hBusiDate);

	openCursor();
	while(fetchTable()) {
		totalCnt++;
		daoTable ="cms_a013_bill";
		this.insertTable();
	}
	
	closeCursor();
	
//	if (sql_nrow <= 0) {
//		errmsg("insert cms_a013_bill erpt error");
//		err_exit(1);
//	}
}
//=****************************************************************************
void insertCmsA013Bill3() throws Exception {
	String lsPostDate = "";

	lsPostDate = commDate.monthAdd(hBusiDate, 0 - hCmc3FstMm)+"01";
	sqlCmd ="select "
		+ " a.p_seqno , "
		+ " major_card_no , "
		+ " decode(a.acct_code,'IT', "
		+ " decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0), "
		+ " (decode(A.sign_flag,'-',-1,1) * a.dest_amt) ) as dest_amt , "
		+ " a.acct_code , "
		+ " a.txn_code , "
		+ " a.post_date , "
		+ " a.mcht_no "
		+ " from bil_bill A left join bil_contract B on a.contract_no = a.contract_no"
		+ " where 1=1 "
		+ " and ( a.acct_code in ('BL','ID','CA','AO','OT') "
		+ "      or (a.acct_code = 'IT' and a.install_curr_term =1) )"
		+ " and post_date >= ? "
		+ " and post_date <= ? "
		+" and A.p_seqno in (select acno_p_seqno from cms_a013_mcard)"
			+" AND A.ecs_cus_mcht_no NOT IN (" +
			" SELECT data_code FROM MKT_MCHTGP_DATA" +
			" WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
			" )"
	;

	ppp(1, lsPostDate);
	ppp(2, hBusiDate);

	openCursor();
	while(fetchTable()) {
		totalCnt++;
		daoTable ="cms_a013_bill";
		this.insertTable();
	}
	
	closeCursor();
	
}
//=****************************************************************************
void selectBilBill2() throws Exception {
	this.fetchExtend = "bill2.";
	sqlCmd =" SELECT  count(*) as aa_cnt, "
			+"        sum(a.dest_amt) as aa_dest_amt, "
			+"        e.end_card_no, "
			+"        e.year_type, "
			+"        e.proj_no, "
			+"        max(e.fst_amt_cond) as ee_amt_cond, "
			+"        max(e.fst_purch_amt) as ee_purch_amt, "
			+"        max(e.fst_row_cond) as ee_row_cond, "
			+"        max(e.fst_purch_row) as ee_purch_row, "
			+"        max(e.lst_tol_amt) as ee_tot_amt "
			+" FROM   cms_a013_bill A join cms_a013_mcard E"
				+" on A.acno_p_seqno =E.acno_p_seqno and A.major_card_no =E.major_card_no"
			+" where  1=1"
			+" AND    a.acct_code in (decode(e.acct_code_bl,'Y','BL','XX'), "
			+"                       decode(e.acct_code_it,'Y','IT','XX'), "
			+"                       decode(e.acct_code_id,'Y','ID','XX'), "
			+"                       decode(e.acct_code_ca,'Y','CA','XX'), "
			+"                       decode(e.acct_code_ao,'Y','AO','XX'), "
			+"                       decode(e.acct_code_ot,'Y','OT','XX')) "
			+" AND ( (A.txn_code in ('06','25','27','28','29'))  "
			+"    or (A.txn_code not in ('06','25','27','28','29') "
			+"        and A.dest_amt > e.fst_one_low_amt) )"
			+" AND    post_date >= uf_month_add(?,e.fst_mm*-1)||'01' " //h_busi_date
			+" AND ( e.fst_mcht = '0' "
			+"     or (e.fst_mcht = '1' "
			+"         and  exists (select 1 "
			+"                from   cms_roadparm2_bn_data "
			+"                where  proj_no  = e.proj_no "
			+"                and    data_type = '0'||e.year_type "
			+"                and    data_code =a.mcht_no)) "
			+"     or (e.fst_mcht = '2' and   not exists (select 1 "
			+"                     from   cms_roadparm2_bn_data "
			+"                     where  proj_no  = e.proj_no "
			+"                     and    data_type = '0'||e.year_type "
			+"                     and    data_code = a.mcht_no)) )"
			+" GROUP BY e.end_card_no,e.year_type,e.proj_no "
			;
	
	ppp(1,hBusiDate.substring(0,6));
	
	ddd(sqlCmd);
	this.openCursor();

	while (fetchTable()) {
		hiBillDataCnt = colInt("bill2.aa_cnt");
		hBillDestAmt = colInt("bill2.aa_dest_amt");
		hCrltMajorCardNo = colSs("bill2.end_card_no");
		hCrltYearType = colSs("bill2.year_type");
		hCrltProjNo = colSs("bill2.proj_no");
		hRdp2FstAmtCond = colSs("bill2.ee_amt_cond");
		hRdp2FstPurchAmt = colInt("bill2.ee_purch_amt");
		hRdp2FstRowCond = colSs("bill2.ee_row_cond");
		hRdp2FstPurchRow = colInt("bill2.ee_purch_row");
		hRdp2LstTolAmt = colInt("bill2.ee_tot_amt");
		hCrltProcFlag = "1";
		hMcceConsumeBlAmt = 0;

		if ( (eq(hRdp2FstAmtCond,"Y") && hBillDestAmt >= hRdp2FstPurchAmt)
			 || (eq(hRdp2FstRowCond, "Y") && hiBillDataCnt >= hRdp2FstPurchRow) ) {
			hCrltProcFlag = "Y";
			if ( eq(hCrltYearType, "2") && hRdp2LstTolAmt >0 )
				hCrltProcFlag = "2";
		}

		updateCmsRoadlist3();
		totalCnt++;
		if ((totalCnt % 10000) == 0)
			printf("Process record [%s]", totalCnt);
	}
	this.closeCursor();
}
//=****************************************************************************
void selectBilBill3() throws Exception {
	this.fetchExtend = "bill3.";
	sqlCmd =" SELECT  count(*) as aa_cnt, "
			+"        sum(a.dest_amt) as aa_dest_amt, "
			+"        e.end_card_no, "
			+"        e.year_type, "
			+"        e.proj_no, "
			+"        max(e.fst_amt_cond) as ee_amt_cond, "
			+"        max(e.fst_purch_amt) as ee_purch_amt, "
			+"        max(e.fst_row_cond) as ee_row_cond, "
			+"        max(e.fst_purch_row) as ee_purch_row, "
			+"        max(e.lst_tol_amt) as ee_tot_amt "
			+" FROM   cms_a013_bill A join cms_a013_mcard E"
				+" on A.acno_p_seqno =E.acno_p_seqno and A.card_no =E.card_no"
			+" where  1=1"
			+" AND    a.acct_code in (decode(e.acct_code_bl,'Y','BL','XX'), "
			+"                       decode(e.acct_code_it,'Y','IT','XX'), "
			+"                       decode(e.acct_code_id,'Y','ID','XX'), "
			+"                       decode(e.acct_code_ca,'Y','CA','XX'), "
			+"                       decode(e.acct_code_ao,'Y','AO','XX'), "
			+"                       decode(e.acct_code_ot,'Y','OT','XX')) "
			+" AND ( (A.txn_code in ('06','25','27','28','29'))  "
			+"    or (A.txn_code not in ('06','25','27','28','29') "
			+"        and A.dest_amt > e.fst_one_low_amt) )"
			+" AND    post_date >= uf_month_add(?,e.fst_mm*-1)||'01' " //h_busi_date
			+" AND ( e.fst_mcht = '0' "
			+"     or (e.fst_mcht = '1' "
			+"         and  exists (select 1 "
			+"                from   cms_roadparm2_bn_data "
			+"                where  proj_no  = e.proj_no "
			+"                and    data_type = '0'||e.year_type "
			+"                and    data_code =a.mcht_no)) "
			+"     or (e.fst_mcht = '2' and   not exists (select 1 "
			+"                     from   cms_roadparm2_bn_data "
			+"                     where  proj_no  = e.proj_no "
			+"                     and    data_type = '0'||e.year_type "
			+"                     and    data_code = a.mcht_no)) )"
			+" GROUP BY e.end_card_no,e.year_type,e.proj_no "
			;
	
	ppp(1,hBusiDate.substring(0,6));
	
	ddd(sqlCmd);
	this.openCursor();

	while (fetchTable()) {
		hiBillDataCnt = colInt("bill3.aa_cnt");
		hBillDestAmt = colInt("bill3.aa_dest_amt");
		hCrltCardNo = colSs("bill3.end_card_no");
		hCrltYearType = colSs("bill3.year_type");
		hCrltProjNo = colSs("bill3.proj_no");
		hRdp2FstAmtCond = colSs("bill3.ee_amt_cond");
		hRdp2FstPurchAmt = colInt("bill3.ee_purch_amt");
		hRdp2FstRowCond = colSs("bill3.ee_row_cond");
		hRdp2FstPurchRow = colInt("bill3.ee_purch_row");
		hRdp2LstTolAmt = colInt("bill3.ee_tot_amt");
		hCrltProcFlag = "1";
		hMcceConsumeBlAmt = 0;

		if ( (eq(hRdp2FstAmtCond,"Y") && hBillDestAmt >= hRdp2FstPurchAmt)
			 || (eq(hRdp2FstRowCond, "Y") && hiBillDataCnt >= hRdp2FstPurchRow) ) {
			hCrltProcFlag = "Y";
			if ( eq(hCrltYearType, "2") && hRdp2LstTolAmt >0 )
				hCrltProcFlag = "2";
		}

		updateCmsRoadlist33();
		totalCnt++;
		if ((totalCnt % 10000) == 0)
			printf("Process record [%s]", totalCnt);
	}
	this.closeCursor();
}
//=============================================================================
void updateCmsRoadlist3() throws Exception {
	if (tiList3U <=0) {
		sqlCmd = " update cms_roadlist set "
			+ " proc_flag = ? , "
			+ " proc_date = ? , "
			+ " purch_amt = purch_amt + ? , "
			+ " purch_row = purch_row + ? , "
			+ " tol_amt = tol_amt + ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where major_card_no = ? "
			+ " and major_card_no = end_card_no "
			+ " and year_type = ? "
			+ " and proj_no = ? ";
		tiList3U =ppStmtCrt("ti_list3_U","");
	}

	ppp(1, hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hBillDestAmt);
	ppp(hiBillDataCnt);
	ppp(hMcceConsumeBlAmt);
	ppp(hModPgm);
	ppp(hCrltMajorCardNo);
	ppp(hCrltYearType);
	ppp(hCrltProjNo);

	sqlExec(tiList3U);
	if (sqlNrow < 0) {
		errmsg("update cms_roadlist_3 error, kk[%s]", hCrltMajorCardNo);
		errExit(1);
	}
}
//=============================================================================
void updateCmsRoadlist33() throws Exception {
	if (tiList3U <=0) {
		sqlCmd = " update cms_roadlist set "
			+ " proc_flag = ? , "
			+ " proc_date = ? , "
			+ " purch_amt = purch_amt + ? , "
			+ " purch_row = purch_row + ? , "
			+ " tol_amt = tol_amt + ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where card_no = ? "
			+ " and card_no = end_card_no "
			+ " and year_type = ? "
			+ " and proj_no = ? ";
		tiList3U =ppStmtCrt("ti_list3_U","");
	}

	ppp(1, hCrltProcFlag);
	ppp(hBusiDate);
	ppp(hBillDestAmt);
	ppp(hiBillDataCnt);
	ppp(hMcceConsumeBlAmt);
	ppp(hModPgm);
	ppp(hCrltCardNo);
	ppp(hCrltYearType);
	ppp(hCrltProjNo);

	sqlExec(tiList3U);
	if (sqlNrow < 0) {
		errmsg("update cms_roadlist_3_3 error, kk[%s]", hCrltCardNo);
		errExit(1);
	}
}
//=****************************************************************************
void selectMktPostConsume2() throws Exception {
	String tableE="SELECT c.acno_p_seqno, "
			+" c.major_card_no, "
			+" c.year_type, "
			+" c.proj_no, "
			+" max(d.amt_sum_flag) as amt_sum_flag, "
			+" max(d.lst_cond) as fst_cond, "
			+" max(d.lst_mm) as fst_mm, "
			+" max(d.lst_acct_code_bl) as acct_code_bl, "
			+" max(d.lst_acct_code_it) as acct_code_it, "
			+" max(d.lst_acct_code_ca) as acct_code_ca, "
			+" max(d.lst_acct_code_id) as acct_code_id, "
			+" max(d.lst_acct_code_ao) as acct_code_ao, "
			+" max(d.lst_acct_code_ot) as acct_code_ot, "
			+" max(d.lst_one_low_amt)   as fst_one_low_amt, "
			+" max(d.lst_amt_cond)      as fst_amt_cond, " 
			+" max(d.lst_purch_amt)     as fst_purch_amt, " 
			+" max(d.lst_row_cond)      as fst_row_cond, " 
			+" max(d.lst_purch_row)     as fst_purch_row, "
			+" max(d.lst_tol_amt) as lst_tol_amt "
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+" WHERE  D.amt_sum_flag = '2' "
			+" AND    C.year_type    = '2' "
			+" AND    C.proc_flag    = '2' "
			+" GROUP BY C.acno_p_seqno,C.major_card_no,C.year_type,C.proj_no"
			;
	sqlCmd =" SELECT  sum(decode(e.acct_code_bl,'Y',consume_bl_amt,0)+ "
			+"             decode(e.acct_code_it,'Y',consume_it_amt,0)+ "
			+"             decode(e.acct_code_id,'Y',consume_id_amt,0)+ "
			+"             decode(e.acct_code_ca,'Y',consume_ca_amt,0)+ "
			+"             decode(e.acct_code_ao,'Y',consume_ao_amt,0)+ "
			+"             decode(e.acct_code_ot,'Y',consume_ot_amt,0)) as aa_consume_amt, "
			+"         e.major_card_no, "
			+"         e.year_type, "
			+"         e.proj_no, "
			+"         max(e.lst_tol_amt) as ee_tol_amt "
			+" FROM    mkt_post_consume a, ( "+tableE+" ) E"
			+" where  a.major_card_no = e.major_card_no "
			+" AND    a.acct_month between ? and ? "  //:is_last_yyyy||'01' and :is_last_yyyy||'12'
			//-  AND    a.consume_type = 'C'
			+" GROUP BY e.major_card_no,e.year_type,e.proj_no"
			;
	ppp(1, isLastYyyy + "01");
	ppp(2, isLastYyyy + "12");

	this.fetchExtend = "post2.";
	this.openCursor();

	while (fetchTable()) {
		hMcceConsumeBlAmt = colInt("post2.aa_consume_amt");
		hCrltMajorCardNo = colSs("post2.major_card_no");
		hCrltYearType = colSs("post2.year_type");
		hCrltProjNo = colSs("post2.proj_no");
		hRdp2LstTolAmt = colInt("post2.ee_tol_amt");
		hiBillDataCnt = 0;
		hBillDestAmt = 0;
		hCrltProcFlag = "3";

		if (hMcceConsumeBlAmt >= hRdp2LstTolAmt)
			hCrltProcFlag = "Y";

		updateCmsRoadlist3();
		totalCnt++;
		if ((totalCnt % 1000) == 0)
			printf("Process record [%s]", totalCnt);
	}
	this.closeCursor();
}
//=****************************************************************************
void selectMktPostConsume3() throws Exception {
	String tableE="SELECT c.acno_p_seqno, "
			+" c.card_no, "
			+" c.year_type, "
			+" c.proj_no, "
			+" max(d.amt_sum_flag) as amt_sum_flag, "
			+" max(d.lst_cond) as fst_cond, "
			+" max(d.lst_mm) as fst_mm, "
			+" max(d.lst_acct_code_bl) as acct_code_bl, "
			+" max(d.lst_acct_code_it) as acct_code_it, "
			+" max(d.lst_acct_code_ca) as acct_code_ca, "
			+" max(d.lst_acct_code_id) as acct_code_id, "
			+" max(d.lst_acct_code_ao) as acct_code_ao, "
			+" max(d.lst_acct_code_ot) as acct_code_ot, "
			+" max(d.lst_one_low_amt)   as fst_one_low_amt, "
			+" max(d.lst_amt_cond)      as fst_amt_cond, " 
			+" max(d.lst_purch_amt)     as fst_purch_amt, " 
			+" max(d.lst_row_cond)      as fst_row_cond, " 
			+" max(d.lst_purch_row)     as fst_purch_row, "
			+" max(d.lst_tol_amt) as lst_tol_amt "
			+" FROM   cms_roadlist C join cms_roadparm2 D on D.proj_no =C.proj_no"
			+" WHERE  D.amt_sum_flag = '3' "
			+" AND    C.year_type    = '2' "
			+" AND    C.proc_flag    = '2' "
			+" GROUP BY C.acno_p_seqno,C.card_no,C.year_type,C.proj_no"
			;
	sqlCmd =" SELECT  sum(decode(e.acct_code_bl,'Y',consume_bl_amt,0)+ "
			+"             decode(e.acct_code_it,'Y',consume_it_amt,0)+ "
			+"             decode(e.acct_code_id,'Y',consume_id_amt,0)+ "
			+"             decode(e.acct_code_ca,'Y',consume_ca_amt,0)+ "
			+"             decode(e.acct_code_ao,'Y',consume_ao_amt,0)+ "
			+"             decode(e.acct_code_ot,'Y',consume_ot_amt,0)) as aa_consume_amt, "
			+"         e.card_no, "
			+"         e.year_type, "
			+"         e.proj_no, "
			+"         max(e.lst_tol_amt) as ee_tol_amt "
			+" FROM    mkt_post_consume a, ( "+tableE+" ) E"
			+" where  a.card_no = e.card_no "
			+" AND    a.acct_month between ? and ? "  //:is_last_yyyy||'01' and :is_last_yyyy||'12'
			//-  AND    a.consume_type = 'C'
			+" GROUP BY e.card_no,e.year_type,e.proj_no"
			;
	ppp(1, isLastYyyy + "01");
	ppp(2, isLastYyyy + "12");

	this.fetchExtend = "post3.";
	this.openCursor();

	while (fetchTable()) {
		hMcceConsumeBlAmt = colInt("post3.aa_consume_amt");
		hCrltCardNo = colSs("post3.card_no");
		hCrltYearType = colSs("post3.year_type");
		hCrltProjNo = colSs("post3.proj_no");
		hRdp2LstTolAmt = colInt("post3.ee_tol_amt");
		hiBillDataCnt = 0;
		hBillDestAmt = 0;
		hCrltProcFlag = "3";

		if (hMcceConsumeBlAmt >= hRdp2LstTolAmt)
			hCrltProcFlag = "Y";

		updateCmsRoadlist33();
		totalCnt++;
		if ((totalCnt % 1000) == 0)
			printf("Process record [%s]", totalCnt);
	}
	this.closeCursor();
}
//=============================================================================
void selectCmsRoaddetail() throws Exception {
	sqlCmd = " select max(rd_seqno) as h_rode_rd_seqno "
		+ " from cms_roaddetail "
		+ " where rd_moddate = ? ";
	
	sqlSelect(sqlCmd,new Object[] {
		hBusiDate
	});

	if (sqlNrow < 0) {
		errmsg("select cms_roaddetail error");
		errExit(1);
	}
	
	hRodeRdSeqno =0;
	if (sqlNrow > 0) {
		hRodeRdSeqno = colInt("h_rode_rd_seqn");
	}

}

//=============================================================================
void selectCmsRoadlist4() throws Exception {
	String lsKkCardNo = "";
		sqlCmd = " select "
			+ " major_card_no , "
			+ " card_no , "
			+ " decode(rm_status,'0','N','Y') as rm_status , "
			+ " decode(proc_flag,'Y','Y','X','X','N') as proc_flag, "
			+ " year_type , "
			+ " proj_no , "
			+ " purch_amt , "
			+ " purch_row , "
			+ " tol_amt "
			+ " from cms_roadlist "
			+ " where major_card_no = end_card_no "
			+ " order by card_no, decode(proc_flag,'Y','Y','N') DESC ";
	sqlSelect();
	int llNrow = sqlNrow;

	for (int ll = 0; ll<llNrow; ll++) {
		hCrltMajorCardNo = colSs(ll, "major_card_no");
		hCrltCardNo = colSs(ll, "card_no");
		hCrltRmStatus = colSs(ll, "rm_status");
		hCrltProcFlag = colSs(ll, "proc_flag");
		hCrltYearType = colSs(ll, "year_type");
		hCrltProjNo = colSs(ll, "proj_no");
		hCrltPurchAmt = colNum(ll, "purch_amt");
		hCrltPurchRow = colInt(ll, "purch_row");
		hCrltTolAmt = colNum(ll, "tol_amt");

		if (eq(lsKkCardNo, hCrltCardNo))
			continue;
		if (eq(hCrltRmStatus, hCrltProcFlag))
			continue;

		updateCmsRoadmaster();
		insertCmsRoaddetail();
		totalCnt++;
		lsKkCardNo = hCrltCardNo;
	}
}

}
