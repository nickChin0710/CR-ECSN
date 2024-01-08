/* 每日卡片狀態異動道路救援終止處理程式 V.2018-0703.00
 * V.2018-0703		JH		test OK
 * V.2019-1210		Pino	新增ID_P_SEQNO、RDS_PCARD兩個欄位
*  109/12/04  V1.00.01    shiyuqi       updated for project coding standard   * 
 * */
package Cms;

import com.SqlParm;
import com.BaseBatch;

public class CmsA010 extends BaseBatch {
private String progname = "每日卡片狀態異動道路救援終止處理程式   109/12/04  V1.00.01";
//CommFunction comm = new CommFunction();
//CommRoutine comr = null;
//====================================================
hdata.CmsRoadmaster hRoad=new hdata.CmsRoadmaster();
hdata.CmsRoaddetail hRode=new hdata.CmsRoaddetail();
hdata.CrdCard hCard=new hdata.CrdCard();
//---------------------------------------------
private String hMlogRowid = "";
private int hRdspStopDays = 0;
private int hRdspFstopDays = 0;
private int hRdspLostDays = 0;
private int hRdspFalseDays = 0;
private String hRdspPcardFlag = "";
//private String h_pctp_card_note = "";
private String hPctpRdsPcard = "";
//---------------------------------------------------
private String wsCardNo = "";
private String wsRmType = "";
private String wsProcflag = "";
private String wsOppDate = "";
//private String ws_car_valdate = "";
//private String ws_audcode = "";
//private String ws_cardtype = "";
//private String ws_sendsts = "";
//private String ws_sendyn = "";
//private String is_car_service_flag = "";
//private int li_days = 0;
//private int wi_RC = 0;
//=========================================
private SqlParm ttRoad=null;
private SqlParm ttMastU=null;
//-------------
private int tiCard=-1;
private int tiMlogU=-1;
private int tiMaster=-1;
private int tiDetlS1=-1;

//int _commit=1;
//=*****************************************************************************
public static void main(String[] args) {
	CmsA010 proc = new CmsA010();

//	proc.debug = true;
//	proc.ddd_sql(true);
	
	proc.mainProcess(args);
	proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg =args.length;
	if (liArg > 1) {
		printf("Usage : CmsA010 [batch_seq]");
		errExit(1);
	}

	if (!connectDataBase())
		errExit(1);
	
//	comr = new CommRoutine(getDBconnect(), getDBalias());
	if (liArg >0) {
		callBatchSeqno(args[liArg -1]);
	}
	callBatch(0, 0, 0);
	
	selectPtrBusinday();
	setModXxx();
	
	selectCmsRoadparm();
	selectCmsModlogCard();

	sqlCommit();
	endProgram();
}

void selectCmsRoadparm() throws Exception {
	hRdspStopDays = 0;
	hRdspFstopDays = 0;
	hRdspFstopDays = 0;
	hRdspFalseDays = 0;
	hRdspPcardFlag = "";
	
	sqlCmd = "select *"
		+ " from cms_roadparm "
		+ " where 1=1 "
		+commSqlStr.rownum(1);
	sqlSelect();

	if (sqlNrow < 0) {
		printf("select cms_roadparm error");
		errExit(1);
	}
	else if (sqlNrow ==0)
		return;

	hRdspStopDays = colInt("stop_days");
	hRdspFstopDays = colInt("fstop_days");
	hRdspLostDays = colInt("lost_days");
	hRdspFalseDays = colInt("false_days");
	hRdspPcardFlag = colSs("pcard_flag");
}

//=****************************************************************************
void selectCmsModlogCard() throws Exception {
	//read cms_modlog_card, 這是crd_card的trigger, 只要動到current_code就會寫 
	sqlCmd = "select a.mod_time ,"
		+ " a.aud_code ,"
		+ " a.card_no ,"
		+ " a.card_type ,"
		+ " hex(a.rowid) as rowid ,"
		+ " nvl(b.rds_pcard,'N') as rds_pcard "
		+ " from ptr_card_type B join cms_modlog_card A on A.card_type =B.card_type "
		+" where 1=1"
		+ " and nvl(A.proc_rds,'N') <> 'Y' "
		+ " order by a.mod_time ";
	
//	this.fetchExtend = "";
	this.openCursor();
	while (fetchTable()) {
		hCard.initData();
		
		wsProcflag = "Y";
		hCard.cardNo = colSs("card_no");
//		ws_audcode = col_ss("aud_code");
//		ws_cardtype = col_ss("catd_type");
		hMlogRowid = colSs("rowid");
		hPctpRdsPcard = colSs("rds_pcard");
		
		totalCnt++;
//		if (debug) {
//			ddd("-->%s. card=[%s], rdsP=[%s]", totalCnt
//					, h_card.card_no, h_pctp_rds_pcard);
//		}
		
		//-停用取消---
		selectCrdCard(hCard.cardNo);
		if (eqIgno(hCard.currentCode, "0")) {
//			ddd("==>停用取消");
			updateCmsModlogCard();
			continue;
		}

		int liRC=0;
		//-自動登錄道路救援- 
		if (eqIgno(hPctpRdsPcard, "A")) {
			liRC = mainPvcardStop(); /*--停用--*/
			if (liRC == -1)
				continue;
		}

		//--一般救援======================================================== 
		if (!eqIgno(hPctpRdsPcard, "A")) {
			liRC = mainProcessFree(); //--有申請道路救援 - 免費-- 
			if (liRC == -1)
				continue;
			liRC = mainProcessExpn(); //--有申請道路救援 - 自費--
			if (liRC == -1)
				continue;
		}

		updateCmsModlogCard();
	} //--while--

	this.closeCursor();
}

//==============================================================================
void selectCrdCard(String aCardNo) throws Exception {
	/* strcpy(ws_card_no,(const char *)h_m_csca_card_no[row].arr); */

	if (tiCard <=0) {
		sqlCmd = "select a.current_code ,"
			+ " a.group_code ,"
			+ " a.major_card_no ,"
			+ " a.new_card_no ,"
			+ " a.oppost_date ,"
			+ " a.card_type ,"
			+ " substr(a.new_end_date,1,6) as card_new_end_date ,"
			+ " a.id_p_seqno ,"
			+ " a.old_card_no "
			+ " from crd_card a "
			+ " where 1=1 "
			+ " and a.card_no = ?";
		tiCard =ppStmtCrt("ti_card","");
	}
	
	ppp(1, aCardNo);
	sqlSelect(tiCard);
	if (sqlNrow < 0) {
		sqlerr("select crd_card error, kk="+aCardNo);
		errExit(1);
	}
	
	hCard.currentCode = colSs("current_code");
	hCard.groupCode = colSs("group_code");
	hCard.majorCardNo = colSs("major_card_no");
	hCard.newCardNo = colSs("new_card_no");
	hCard.oppostDate = colSs("oppost_date");
	hCard.cardType = colSs("card_type");
	hCard.newEndDate = colSs("card_new_end_date");
	hCard.idPSeqno = colSs("id_p_seqno");
	hCard.oldCardNo = colSs("old_card_no");
}

//=============================================================================
void updateCmsModlogCard() throws Exception {
	if (!eqIgno(wsProcflag, "Y"))
		return;

	if (tiMlogU <=0) {
		sqlCmd = " update cms_modlog_card set "
			+ " proc_rds = 'Y' ,"
			+ " rds_date ="+commSqlStr.sysYYmd
			+ " where rowid = ?";
		tiMlogU =ppStmtCrt("ti_mlog_U","");
	}

	sqlExec(tiMlogU,new Object[]{
		commSqlStr.ss2rowid(hMlogRowid)
	});

	if (sqlNrow <= 0) {
		sqlerr("select cms_modlog_card error");
		errExit(1);
	}
}

//=============================================================================
int mainPvcardStop() throws Exception {
	wsCardNo = hCard.cardNo;
	wsRmType = "F";

	if (selectRoadmaster() != 1)
		return (0);
	
//	ddd("==>main_pvcard_stop---");
	/* if (strcmp((const char *)h_road.rm_status.arr,"0") == 0) return(0); */

	hRoad.rmStatus = "0"; /* *停用* */
	hRoad.rmModdate = sysDate;
	hRoad.rmReason = "3"; /* *卡片已為無效卡* */
	
	if (updateCmsRpadmaster() != 1)
		return -1;

	/*-- move data from roadmaster to roaddetail --------------------------*/
	// cms_roaddetail_init();
	hRode.cardNo = hCard.majorCardNo;
	hRode.applCardNo = hCard.cardNo;
	hRode.rdType = "F";
	hRode.groupCode = hRoad.groupCode;
	hRode.rdCarno = hRoad.rmCarno;
	hRode.rdCarmanname = hRoad.rmCarmanname;
	hRode.rdCarmanid = hRoad.rmCarmanid;
	hRode.rdHtelno1 = hRoad.rmHtelno1;
	hRode.rdHtelno2 = hRoad.rmHtelno2;
	hRode.rdHtelno3 = hRoad.rmHtelno3;
	hRode.rdOtelno1 = hRoad.rmOtelno1;
	hRode.rdOtelno2 = hRoad.rmOtelno2;
	hRode.rdOtelno3 = hRoad.rmOtelno3;
	hRode.cellarPhone = hRoad.cellarPhone;
	hRode.rdValiddate = hRoad.rmValiddate;
	hRode.rdStatus = hRoad.rmStatus;
	hRode.rdStopdate = sysDate;
	hRode.rdStoprsn = "3";
	
	if (insertCmsRoaddetail() != 1)
		return -1;
	
	return 1;
}

//==============================================================================
int selectRoadmaster() throws Exception {
	hRoad.initData();
	
	hRoad.cardNo = wsCardNo;
	hRoad.rmType = wsRmType;

	if (tiMaster <=0) {
		sqlCmd = "select card_no,"
			+ " rm_type ,"
			+ " rm_carno ,"
			+ " group_code,"
			+ " rm_carmanname ,"
			+ " rm_carmanid ,"
			+ " rm_oldcarno ,"
			+ " rm_htelno1 ,"
			+ " rm_htelno2 ,"
			+ " rm_htelno3 ,"
			+ " rm_otelno1 ,"
			+ " rm_otelno2 ,"
			+ " rm_otelno3 ,"
			+ " cellar_phone ,"
			+ " rm_status ,"
			+ " rm_validdate ,"
			+ " rm_moddate ,"
			+ " rm_reason ,"
			+ " rm_payno ,"
			+ " rm_payamt ,"
			+ " rm_paydate ,"
			+ " hex(rowid) as rowid "
			+ " from cms_roadmaster "
			+ " where card_no = ? "
			+ " and rm_type = ? ";
		tiMaster =ppStmtCrt("ti_master","");
	}
	
	ppp(1, hRoad.cardNo);
	ppp(2, hRoad.rmType);
	
	sqlSelect(tiMaster);
	if (sqlNrow >0) {
		hRoad.cardNo = colSs("card_no");
		hRoad.rmType = colSs("rm_type");
		hRoad.rmCarno = colSs("rm_carno");
		hRoad.groupCode = colSs("group_code");
		hRoad.rmCarmanname = colSs("rm_carmanname");
		hRoad.rmCarmanid = colSs("rm_carmanid");
		hRoad.rmOldcarno = colSs("rm_oldcarno");
		hRoad.rmHtelno1 = colSs("rm_htelno1");
		hRoad.rmHtelno2 = colSs("rm_htelno2");
		hRoad.rmHtelno3 = colSs("rm_htelno3");
		hRoad.rmOtelno1 = colSs("rm_otelno1");
		hRoad.rmOtelno2 = colSs("rm_otelno2");
		hRoad.rmOtelno3 = colSs("rm_otelno3");
		hRoad.cellarPhone = colSs("cellar_phone");
		hRoad.rmStatus = colSs("rm_status");
		hRoad.rmValiddate = colSs("rm_validdate");
		hRoad.rmModdate = colSs("rm_moddate");
		hRoad.rmReason = colSs("rm_reason");
		hRoad.rmPayno = colSs("rm_payno");
		hRoad.rmPayamt = colInt("rm_payamt");
		hRoad.rmPaydate = colSs("rm_paydate");
		
		hRoad.rowid = colSs("rowid");
		return 1;
	}
	return 0;
}

// ****************************************************************************
int updateCmsRpadmaster() throws Exception {

	hRoad.crtUser = "SYSTEM";
	hRoad.crtDate = sysDate;
	hRoad.aprUser = "SYSTEM";
	hRoad.aprDate =sysDate;

	if (empty(hRoad.rmCarno)) {
		hRoad.rmCarmanname = "";
		hRoad.rmCarmanid = "";
	}
	
	if (ttMastU ==null) {
		ttMastU =new com.SqlParm();
		sqlCmd = " update cms_roadmaster set "
			+ ttMastU.kkk(" card_no = ?", "card_no")
			+ ttMastU.kkk(", rm_type = ?", "rm_type")
			+ ttMastU.kkk(", rm_carno = ?", "rm_carno")
			+ ttMastU.kkk(", group_code = ?", "group_code")
			+ ttMastU.kkk(", rm_carmanname = ?", "rm_carmanname")
			+ ttMastU.kkk(", rm_carmanid = ?", "rm_carmanid")
			+ ttMastU.kkk(", rm_oldcarno = ?", "rm_oldcarno")
			+ ttMastU.kkk(", rm_htelno1 = ? ", "rm_htelno1")
			+ ttMastU.kkk(", rm_htelno2 = ?", "rm_htelno2")
			+ ttMastU.kkk(", rm_htelno3 = ?", "rm_htelno3")
			+ ttMastU.kkk(", rm_otelno1 = ?", "rm_otelno1")
			+ ttMastU.kkk(", rm_otelno2 = ?", "rm_otelno2")
			+ ttMastU.kkk(", rm_otelno3 = ?", "rm_otelno3")
			+ ttMastU.kkk(", cellar_phone = ?", "cellar_phone")
			+ ttMastU.kkk(", rm_status = ?", "rm_status")
			+ ttMastU.kkk(", rm_validdate =?", "rm_validdate")
			+ ttMastU.kkk(", rm_moddate = ?", "rm_moddate")
			+ ttMastU.kkk(", rm_reason = ?", "rm_reason")
			+ ttMastU.kkk(", rm_payno = ?", "rm_payno")
			+ ttMastU.kkk(", rm_payamt = ?", "rm_payamt")
			+ ttMastU.kkk(", rm_paydate = ?", "rm_paydate")
			+ ttMastU.kkk(", crt_user = ?", "crt_user")
			+ ", crt_date ="+ commSqlStr.sysYYmd
			+ ttMastU.kkk(", apr_user = ?", "apr_user")
			+ ", apr_date ="+ commSqlStr.sysYYmd
			+ ttMastU.kkk(", mod_user = ?", "mod_user")
			+ ", mod_time ="+ commSqlStr.sqlDTime
			+ ttMastU.kkk(", mod_pgm = ?", "mod_pgm")
			+ ", mod_seqno = nvl(mod_seqno,0)+1 "
			+ ttMastU.kkk(", rds_pcard = ?", "rds_pcard")
			+ ttMastU.kkk(", id_p_seqno = ?", "id_p_seqno")
			+ ttMastU.kkk(" where rowid =? ", "rowid");
		ttMastU.sqlFrom =sqlCmd;
		ttMastU.pfidx =ppStmtCrt("tt-master-U","");
	}

	ttMastU.ppp("card_no",hRoad.cardNo);
	ttMastU.ppp("rm_type",hRoad.rmType);
	ttMastU.ppp("rm_carno",hRoad.rmCarno);
	ttMastU.ppp("group_code",hRoad.groupCode);
	ttMastU.ppp("rm_carmanname",hRoad.rmCarmanname);
	ttMastU.ppp("rm_carmanid",hRoad.rmCarmanid);
	ttMastU.ppp("rm_oldcarno",hRoad.rmOldcarno);
	ttMastU.ppp("rm_htelno1",hRoad.rmHtelno1);
	ttMastU.ppp("rm_htelno2",hRoad.rmHtelno2);
	ttMastU.ppp("rm_htelno3",hRoad.rmHtelno3);
	ttMastU.ppp("rm_otelno1",hRoad.rmOtelno1);
	ttMastU.ppp("rm_otelno2",hRoad.rmOtelno2);
	ttMastU.ppp("rm_otelno3",hRoad.rmOtelno3);
	ttMastU.ppp("cellar_phone",hRoad.cellarPhone);
	ttMastU.ppp("rm_status",hRoad.rmStatus);
	ttMastU.ppp("rm_validdate",hRoad.rmValiddate);
	ttMastU.ppp("rm_moddate",hRoad.rmModdate);
	ttMastU.ppp("rm_reason",hRoad.rmReason);
	ttMastU.ppp("rm_payno",hRoad.rmPayno);
	ttMastU.ppp("rm_payamt",hRoad.rmPayamt);
	ttMastU.ppp("rm_paydate",hRoad.rmPaydate);
	ttMastU.ppp("rds_pcard",hPctpRdsPcard);
	ttMastU.ppp("id_p_seqno",hCard.idPSeqno);
	ttMastU.ppp("crt_user",hModUser);
//	tt_mastU.ppp("",h_road.rm_crtdate);
	ttMastU.ppp("apr_user",hModUser);
//	tt_mastU.ppp("",h_road.rm_aprdate);
	ttMastU.ppp("mod_user",hModUser);
	ttMastU.ppp("mod_pgm",hModPgm);
	ttMastU.setRowId("rowid",hRoad.rowid);

//	ddd(tt_mastU.ddd_sql());
	
	sqlExec(ttMastU.pfidx, ttMastU.getConvParm());
	if (sqlNrow <= 0) {
		sqlerr("update cms_roaddetail error");
		errExit(1);
	}
	
	return 1;
}

//==============================================================================
int insertCmsRoaddetail() throws Exception {
	/*-- initial rd_seqno --*/
	if (tiDetlS1 <=0) {
		sqlCmd = "select max(rd_seqno) as rode_rd_seqno "
			+ " from cms_roaddetail "
			+ " where rd_moddate = to_char(sysdate,'yyyymmdd') ";
		tiDetlS1 =ppStmtCrt("ti_detl_s1","");
	}
	sqlSelect(tiDetlS1);
	if (sqlNrow < 0) {
		sqlerr("select cms_roaddetail error");
		errExit(1);
	}

	hRode.rdSeqno = colInt("rode_rd_seqno");
	hRode.rdSeqno++;

	if (empty(hRode.rdCarno)) {
		hRode.rdCarmanname = "";
		hRode.rdCarmanid = "";
	}

	//----------------
	if (ttRoad == null) {
		ttRoad = new com.SqlParm();
		sqlCmd = "insert into cms_roaddetail ("
			+ " rd_moddate ,"
			+ " rd_seqno ,"
			+ " rd_modtype ,"
			+ " card_no ,"
			+ " new_card_no ,"
			+ " rd_type ,"
			+ " appl_card_no ,"
			+ " group_code ,"
			+ " rd_carno ,"
			+ " rd_carmanname ,"
			+ " rd_carmanid ,"
			+ " rd_newcarno ,"
			+ " rd_htelno1 ,"
			+ " rd_htelno2 ,"
			+ " rd_htelno3 ,"
			+ " rd_otelno1 ,"
			+ " rd_otelno2 ,"
			+ " rd_otelno3 ,"
			+ " cellar_phone ,"
			+ " rd_validdate ,"
			+ " rd_status ,"
			+ " rd_payamt ,"
			+ " rd_payno ,"
			+ " rd_paydate ,"
			+ " rd_stopdate ,"
			+ " rd_stoprsn ,"
			+ " rds_pcard ,"
			+ " id_p_seqno ,"
			+ " crt_user ,"
			+ " crt_date ,"
			+ " apr_user ,"
			+ " apr_date ,"
			+ " mod_user ,mod_time ,mod_pgm ,mod_seqno "
			+ " ) values ("
			+ ttRoad.pmkk(0, ":rd_moddate ,")
			+ ttRoad.pmkk(":rd_seqno ,")
			+ ttRoad.pmkk(":rd_modtype ,")
			+ ttRoad.pmkk(":card_no ,")
			+ ttRoad.pmkk(":new_card_no ,")
			+ ttRoad.pmkk(":rd_type ,")
			+ ttRoad.pmkk(":appl_card_no ,")
			+ ttRoad.pmkk(":group_code ,")
			+ ttRoad.pmkk(":rd_carno ,")
			+ ttRoad.pmkk(":rd_carmanname ,")
			+ ttRoad.pmkk(":rd_carmanid ,")
			+ ttRoad.pmkk(":rd_newcarno ,")
			+ ttRoad.pmkk(":rd_htelno1 ,")
			+ ttRoad.pmkk(":rd_htelno2 ,")
			+ ttRoad.pmkk(":rd_htelno3 ,")
			+ ttRoad.pmkk(":rd_otelno1 ,")
			+ ttRoad.pmkk(":rd_otelno2 ,")
			+ ttRoad.pmkk(":rd_otelno3 ,")
			+ ttRoad.pmkk(":cellar_phone ,")
			+ ttRoad.pmkk(":rd_validdate ,")
			+ ttRoad.pmkk(":rd_status ,")
			+ ttRoad.pmkk(":rd_payamt ,")
			+ ttRoad.pmkk(":rd_payno ,")
			+ ttRoad.pmkk(":rd_paydate ,")
			+ ttRoad.pmkk(":rd_stopdate ,")
			+ ttRoad.pmkk(":rd_stoprsn ,")
			+ ttRoad.pmkk(":rds_pcard ,")
			+ ttRoad.pmkk(":id_p_seqno ,")
			+ ttRoad.pmkk(":crt_user ,")
			+ commSqlStr.sysYYmd+","
			+ ttRoad.pmkk(":apr_user ,")
			+ commSqlStr.sysYYmd+","
			+this.modxxxInsert()
			+ " )";
		ttRoad.pfidx =ppStmtCrt("tt_road-A",ttRoad.sqlFrom);
	}
	ttRoad.ppp("rd_moddate", hRode.rdModdate);
	ttRoad.ppp("rd_seqno", hRode.rdSeqno);
	ttRoad.ppp("rd_modtype", hRode.rdModtype);
	ttRoad.ppp("card_no", hRode.cardNo);
	ttRoad.ppp("new_card_no", hRode.newCardNo);
	ttRoad.ppp("rd_type", hRode.rdType);
	ttRoad.ppp("appl_card_no", hRode.applCardNo);
	ttRoad.ppp("group_code", hRode.groupCode);
	ttRoad.ppp("rd_carno", hRode.rdCarno);
	ttRoad.ppp("rd_carmanname", hRode.rdCarmanname);
	ttRoad.ppp("rd_carmanid", hRode.rdCarmanid);
	ttRoad.ppp("rd_newcarno", hRode.rdNewcarno);
	ttRoad.ppp("rd_htelno1", hRode.rdHtelno1);
	ttRoad.ppp("rd_htelno2", hRode.rdHtelno2);
	ttRoad.ppp("rd_htelno3", hRode.rdHtelno3);
	ttRoad.ppp("rd_otelno1", hRode.rdOtelno1);
	ttRoad.ppp("rd_otelno2", hRode.rdOtelno2);
	ttRoad.ppp("rd_otelno3", hRode.rdOtelno3);
	ttRoad.ppp("cellar_phone", hRode.cellarPhone);
	ttRoad.ppp("rd_validdate", hRode.rdValiddate);
	ttRoad.ppp("rd_status", hRode.rdStatus);
	ttRoad.ppp("rd_payamt", hRode.rdPayamt);
	ttRoad.ppp("rd_payno", hRode.rdPayno);
	ttRoad.ppp("rd_paydate", hRode.rdPaydate);
	ttRoad.ppp("rd_stopdate", hRode.rdStopdate);
	ttRoad.ppp("rd_stoprsn", hRode.rdStoprsn);
	ttRoad.ppp("rds_pcard", hPctpRdsPcard);
	ttRoad.ppp("id_p_seqno", hCard.idPSeqno);
	ttRoad.ppp("crt_user", hModUser);
	ttRoad.ppp("apr_user", hModUser);

	sqlExec(ttRoad.pfidx, ttRoad.getConvParm());
	if (sqlNrow <= 0) {
		sqlerr("insert cms_roaddetail error");
		errExit(1);
	}
	return 1;
}

//==============================================================================
int mainProcessFree() throws Exception {
	// Section: 處理 登錄道路救援 - 免費
	wsCardNo = hCard.cardNo;
	wsRmType = "F";
	/*-- not exist in CMS_ROADMASTER --*/
	if (selectRoadmaster() != 1)
		return 1;

//	ddd("==>main_process_free---");
	
	if (checkStopDate() != 1) {
		wsProcflag = "N";
		return 0;
	}

	/*-- Update CMS_ROADMASTER --------------------------------------------*/
	if (hCard.newCardNo.length() == 0) {
		hRoad.rmStatus = "0";
		hRoad.rmReason = "3";
		hRoad.rmModdate = sysDate;
	}
	else {
		hRoad.cardNo = hCard.newCardNo;
		hRoad.groupCode = hCard.groupCode;
	}

	if (updateCmsRpadmaster() != 1)
		return -1;

	/*-- Move data to CMS_ROADDETAIL --------------------------------------*/
	hRode.initData();
	hRode.rdModtype ="B";
	
	hRode.cardNo = hCard.majorCardNo;
	hRode.applCardNo = hCard.cardNo;
	hRode.rdType = "F";
	hRode.groupCode = hRoad.groupCode;
	hRode.rdCarno = hRoad.rmCarno;
	hRode.rdCarmanname = hRoad.rmCarmanname;
	hRode.rdCarmanid = hRoad.rmCarmanid;
	hRode.rdHtelno1 = hRoad.rmHtelno1;
	hRode.rdHtelno2 = hRoad.rmHtelno2;
	hRode.rdHtelno3 = hRoad.rmHtelno3;
	hRode.rdOtelno1 = hRoad.rmOtelno1;
	hRode.rdOtelno2 = hRoad.rmOtelno2;
	hRode.rdOtelno3 = hRoad.rmOtelno3;
	hRode.cellarPhone = hRoad.cellarPhone;
	hRode.rdValiddate = hRoad.rmValiddate;
	hRode.rdStatus = hRoad.rmStatus;
	
	if (empty(hCard.newCardNo)) {
		hRode.rdStoprsn = "3";
		hRode.rdStopdate = sysDate;
	}
	else {
		hRode.applCardNo = hCard.newCardNo;
		hRode.groupCode = hCard.groupCode;
	}
	
	if (insertCmsRoaddetail() != 1)
		return -1;
	return 1;
}

//==============================================================================
int checkStopDate() {
	//--no new cardno--------------------------------
	if (noEmpty(hCard.newCardNo))
		return 1;
	
	if (empty(hCard.oppostDate))
		return 1;
	
	wsOppDate = hCard.oppostDate;

	int liDays = commDate.daysBetween(wsOppDate, sysDate);

	switch (hCard.currentCode) {
	case "1":
		if (hRdspStopDays == 0)
			return 1;
		if (liDays <= hRdspStopDays)
			return 0;
	break;
	case "2":
		if (hRdspLostDays == 0)
			return 1;
		if (liDays <= hRdspLostDays)
			return 0;
	break;
	case "3":
		if (hRdspFstopDays == 0)
			return 1;
		if (liDays <= hRdspFstopDays)
			return 0;
	break;
	case "4":
		if (hRdspStopDays == 0)
			return 1;
		if (liDays <= hRdspStopDays)
			return 0;
	break;
	case "5":
		if (hRdspFalseDays == 0)
			return 1;
		if (liDays <= hRdspFalseDays)
			return 0;
	break;
	}

	return 1;
}

//==============================================================================
int mainProcessExpn() throws Exception {
	wsCardNo = hCard.cardNo;
	wsRmType = "E";
	if (selectRoadmaster() != 1)
		return 1;

//	ddd("==>main_process_expn---");
	
	//--無效卡停用-- 
	if (empty(hCard.newCardNo))
		return 1;

	hRoad.cardNo = hCard.newCardNo;
	hRoad.groupCode = hCard.groupCode;
	if (updateCmsRpadmaster() != 1)
		return 1;

	//-- Move data to CMS_ROADDETAIL -
	hRode.initData();
	hRode.rdModtype ="B";
	
	hRode.cardNo = hCard.majorCardNo;
	hRode.applCardNo = hCard.cardNo;
	hRode.rdType = "E";
	hRode.groupCode = hRoad.groupCode;
	hRode.rdCarno = hRoad.rmCarno;
	hRode.rdCarmanname = hRoad.rmCarmanname;
	hRode.rdCarmanid = hRoad.rmCarmanid;
	hRode.rdHtelno1 = hRoad.rmHtelno1;
	hRode.rdHtelno2 = hRoad.rmHtelno2;
	hRode.rdHtelno3 = hRoad.rmHtelno3;
	hRode.rdOtelno1 = hRoad.rmOtelno1;
	hRode.rdOtelno2 = hRoad.rmOtelno2;
	hRode.rdOtelno3 = hRoad.rmOtelno3;
	hRode.cellarPhone = hRoad.cellarPhone;
	hRode.rdValiddate = hRoad.rmValiddate;
	hRode.rdStatus = hRoad.rmStatus;
	hRode.applCardNo = hCard.newCardNo;
	hRode.groupCode = hCard.groupCode;
	if (insertCmsRoaddetail() != 1)
		return -1;

	return 1;
}

}
