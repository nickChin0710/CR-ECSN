package Rsk;
/**   
 * 2020-0120   V1.00.00	  JH		onbat.mod_pgm
 * 2020-0115   V1.00.01   JH        個繳戶(acno_flag=1,2)
 * 2020-0108   V1.00.02   JH        modify
 * 2019-0625:  V1.00.03   JH        p_xxx >>acno_p_xxx
 * 2018-0118:  V1.00.04	  JH		modify insert_rsk_acnolog
 * 2017-1229:  V1.00.05	  JH		initial
 * 109-11-19   V1.00.06   tanwei    updated for project coding standard
*  109/12/31  V1.00.07  yanghan       修改了部分无意义的變量名稱          *
*  2022-1208   V1.00.08   Alex      新增寫入卡片停掛檔、報送Outgoing
 * */

import com.Parm2sql;
import com.SqlParm;
import com.BaseBatch;
import Cca.CcaOutGoing;

public class RskP735 extends BaseBatch {
private String progname = "整批強停連動處理程式   111/12/08 V1.00.08";
//=============================================================================
hdata.CrdCard hCard=new hdata.CrdCard();
hdata.ActAcno hAcno=new hdata.ActAcno();
hdata.RskAcnoLog hAclg=new hdata.RskAcnoLog();
CcaOutGoing ccaOutGoing = null;
//-----------------------------------------------------------------------------
private long hStecTAcctCnt = 0;
private long hStecTStopCnt = 0;
private long hStecTStopnot1Cnt = 0;
private long hStecTStopnot2Cnt = 0;
private long hStecTStopnot3Cnt = 0;
private long hStecTStopnot4Cnt = 0;

private String hTempStopWhy = "";
private String hTempNotstopWhy = "";
private String lsKindFlag;
private String lsCardNo;
private String hCdjcRowid;
private String hRelaPSeqno = "";
private String hFiscReason = "";
//=============================================================
//private SqlParm t_aclg=null;
private SqlParm tJcic1=null;
private SqlParm tSlog=null;
private SqlParm tCrdStop=null;
private SqlParm tApsc=null;
//private com.SqlParm t_onbat=null;
//------------------------------------------------------------
private int tidAcnoU=-1;
private int tidCardS=-1;
private int tidCardU=-1;
private int tidCcasAcctS=-1;
private int tidJcicS1=-1;
private int tidJcicU=-1;
private int tidIdnoS=-1;

//=****************************************************************************
public static void main(String[] args) {
	RskP735 proc = new RskP735();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit(0);
}

//=**********************************************************************************
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);
	
	if (args.length > 1) {
		printf("Usage : rsk_p730 [business_date]");
		this.errExit(1);
	}
	
	dbConnect();
	
	if (args.length > 0) {
		setBusiDate(args[0]);
	}
	
	ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
	
	selectActAcno();
	// 記錄 rsk_stopexec 之累計及結尾記錄(目前逐筆)
	insertRskStopexec();
	
	sqlCommit();
	ccaOutGoing.finalCnt2();
	endProgram();
}

//=****************************************************************************
void selectActAcno() throws Exception {
	this.fetchExtend = "aa.";
	sqlCmd = "select a.acno_p_seqno, A.p_seqno,"
			+ " a.id_p_seqno ," 
			+ " a.acct_type ,"
			+ " a.card_indicator ," 
			+ " a.rc_use_indicator ," 
			+ " a.acno_flag ,"
			+ " a.stop_status ," 
			+" A.line_of_credit_amt,"
			+" A.payment_rate1,"
			+ " b.acno_p_seqno as rela_p_seqno"
			+ " from act_acno A join rsk_acnolog B"
			+ " on a.id_p_seqno = b.id_p_seqno " 
			+ " where 1=1 "
			+ " and b.log_date = ? " 
			+ " and b.kind_flag ='A' "
			+ " and nvl(b.log_mode,'') ='2' " 
			+ " and nvl(b.log_type,'') ='2' "
			+ " and nvl(b.log_reason,'')='S' " 
			+ " and nvl(b.log_not_reason,'') ='' "
			+ " and nvl(b.relate_code,'') ='' " 
			+ " and nvl(a.stop_status,'N') <>'Y' "
			+ " and nvl(a.acno_flag,'N') in ('1','3') ";  //-個繳戶-
	ppp(1, hBusiDate);
	
//	ddd(sqlCmd,get_sqlParm());
	daoTable="openCursor-1";
	this.openCursor();
	
	while (fetchTable()) {
		totalCnt++;
		
		hAcno.pSeqno = colSs("aa.p_seqno");
		hAcno.acnoPSeqno =colSs("aa.acno_p_seqno");
		hAcno.acctType = colSs("aa.acct_type");
		hAcno.idPSeqno = colSs("aa.id_p_seqno");
		hAcno.stopStatus = colSs("aa.stop_status");
		hAcno.rcUseIndicator = colSs("aa.rc_user_indicator");
		hAcno.lineOfCreditAmt =colNum("aa.line_of_credit_amt");
		hAcno.payRateSet(1,colSs("aa.payment_rate1"));
		hAcno.acnoFlag = colSs("aa.acno_flag");
		hRelaPSeqno = colSs("aa.rela_p_seqno");
		
//		if (debug) {
//			ddd("rela_p_seqno[%s]",h_rela_p_seqno);
////			continue;
//		}
		
		/*--採購卡 不強停--*/
		if (eqIgno(hAcno.acctType, "06"))
			continue;
		/*--總繳戶--*/
		if (eqIgno(hAcno.acnoFlag, "Y"))
			continue;
		/*--已停用--*/
		if (eqIgno(hAcno.stopStatus, "Y"))
			continue;
		
		hTempStopWhy = "S";
		hTempNotstopWhy = "";
		
		hStecTAcctCnt++; // --應強停筆數--
		hStecTStopCnt++; // 應強停真強停戶
		
		updateActAcno();
		insertRskAcnolog(0);
		
		selectCrdCard();
	}
	
	closeCursor();
}

//=****************************************************************************
void insertRskStopexec() throws Exception {
		sqlCmd ="insert into rsk_stopexec (" 
				+ " param_type,    " 
				+ " acct_type,     "
				+ " valid_date,    " 
				+ " exec_date,     " 
				+ " exec_mode,     "
				+ " exec_times,    " 
				+ " t_acct_cnt,    " 
				+ " t_stop_cnt,    "
				+ " t_stopnot1_cnt," 
				+ " t_stopnot2_cnt," 
				+ " t_stopnot3_cnt,"
				+ " t_stopnot4_cnt," 
				+ " exec_msg, " 
				+ " mod_user,      "
				+ " mod_time,      " 
				+ " mod_pgm        " 
				+ " ) values ( " 
				+ "  '0'" // '0',
				+ ", '00'" // '00',
				+ ", ?" // :valid_date,
				+ ", " + commSqlStr.sysYYmd +
				", '0'" // exec_mode,
				+ ", 1" // exec_times,
				+ ", ?" // :h_stec_t_acct_cnt,
				+ ", ?" // :h_stec_t_stop_cnt,
				+ ", ?" // :h_stec_t_stopnot1_cnt,
				+ ", ?" // :h_stec_t_stopnot2_cnt,
				+ ", ?" // :h_stec_t_stopnot3_cnt,
				+ ", ?" // :h_stec_t_stopnot4_cnt,
				+ ", '連動強停'" // execmsg
				+ ", ?" // :h_mod_user,
				+ ", " + commSqlStr.sysDTime // sysdate,
				+ ", ?" // :h_mod_pgm,
				+ " )";
	sqlExec(new Object[] {
		hBusiDate, 
		hStecTAcctCnt, 
		hStecTStopCnt, 
		hStecTStopnot1Cnt, 
		hStecTStopnot2Cnt, 
		hStecTStopnot3Cnt, 
		hStecTStopnot4Cnt, 
		hModUser, hModPgm
	});
	
	//-insert error-
	if (sqlNrow == 0) {
		updateRskStopexec();
	}
}

//=****************************************************************************
void updateRskStopexec() throws Exception {
	sqlCmd = "update rsk_stopexec set"
		+ " exec_times =exec_times +1, "
		+ " t_acct_cnt = t_acct_cnt + ?, " // :h_stec_t_acct_cnt,"
		+ " t_stop_cnt = t_stop_cnt + ?, " // :h_stec_t_stop_cnt,
		+ " t_stopnot1_cnt = t_stopnot1_cnt + ?, " // :h_stec_t_stopnot1_cnt,
		+ " t_stopnot2_cnt = t_stopnot2_cnt +?, " // :h_stec_t_stopnot2_cnt,
		+ " t_stopnot3_cnt = t_stopnot3_cnt +?, " // :h_stec_t_stopnot3_cnt,
		+ " t_stopnot4_cnt = t_stopnot4_cnt + ?, " // :h_stec_t_stopnot4_cnt,
		+ commSqlStr.setModXxx(hModUser, hModPgm)
		+ " Where param_type = '0'"
		+ " and   acct_type = '00'"
		+ " and   valid_date = ?" // :h_busi_business_date
		+ " and   exec_date = "+commSqlStr.sysYYmd;

	ppp(1, hStecTAcctCnt);
	ppp(hStecTStopCnt);
	ppp(hStecTStopnot1Cnt);
	ppp(hStecTStopnot2Cnt);
	ppp(hStecTStopnot3Cnt);
	ppp(6,hStecTStopnot4Cnt);
	ppp(hBusiDate);
	
	sqlExec(sqlCmd);
	if (sqlNrow <= 0) {
		printf("update rsk_stopexec error");
		errExit(0);
	}
	
}

//=****************************************************************************
void updateActAcno() throws Exception {
	if (tidAcnoU < 0) {
		sqlCmd = " update act_acno set "
			+ " stop_reason ='J2' ,"
			+ " stop_status ='Y' ,"
			+ commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where acno_p_seqno = ?";
		daoTable ="tid_acno_U";
		tidAcnoU = ppStmtCrt();
	}
	Object[] pp = new Object[] {
		hAcno.acnoPSeqno
	};
	sqlExec(tidAcnoU,pp);
	
	if (sqlNrow <= 0) {
		printf("update act_acno error, kk[%s]",hAcno.acnoPSeqno);
		errExit(0);
	}
}

//=****************************************************************************
void selectCrdCard() throws Exception {
	if (tidCardS <= 0) {
		sqlCmd = "select card_no ," 
				+ " decode(oppost_date,'','99991231',oppost_date) as oppost_date," 
				+ " current_code,"
//				+ " oppost_reason ," 
				+ " major_id_p_seqno ,"
				+ " sup_flag ," 
				+ " id_p_seqno ,"
				+ " new_end_date ,"
				+ " reissue_date ," 
				+ " mail_type ," 
				+ " mail_no ," 
				+ " mail_branch ,"
				+ " mail_proc_date ," 
				+ " card_type ," 
				+ " group_code ,"
				+ " combo_indicator ," 
				+ " hex(rowid) as rowid" 
				+ " from crd_card "
				+ " where acno_p_seqno = ?"
				+ " order by 2 desc ";
		daoTable ="tid_card_S";
		tidCardS = ppStmtCrt();
	}
	ppp(1, hAcno.acnoPSeqno);
	daoTid = "card.";
	sqlSelect(tidCardS);
	
	int sqlNrowNum = sqlNrow;
	String lsMaxOppoDate="";
	
	for (int ii = 0; ii < sqlNrowNum; ii++) {

		hCard.cardNo = colSs(ii, "card.card_no");
		hCard.idPSeqno = colSs(ii, "card.id_p_seqno");
		hCard.cardType = colSs(ii, "card.card_type");
		hCard.groupCode = colSs(ii, "card.group_code");
		hCard.supFlag = colSs(ii, "card.sup_flag");
		hCard.majorIdPSeqno = colSs(ii, "card.major_id_p_seqno");
		hCard.currentCode = colSs(ii, "card.current_code");
		hCard.newEndDate = colSs(ii, "card.new_end_date");
		hCard.reissueDate = colSs(ii, "card.reissue_date");
		hCard.oppostDate = colSs(ii, "card.oppost_date");
		hCard.mailType = colSs(ii, "card.mail_type");
		hCard.mailNo = colSs(ii, "card.mail_no");
		hCard.mailBranch = colSs(ii, "card.mail_branch");
		hCard.mailProcDate = colSs(ii, "card.mail_proc_date");
		hCard.comboIndicator = colSs(ii, "card.combo_indicator");
		
		//-update 流通卡OR最近停用卡片-
		if (empty(lsMaxOppoDate)) {
			lsMaxOppoDate = hCard.oppostDate;
		}
		
		if (!eqIgno(hCard.oppostDate, lsMaxOppoDate))
			break;
		
		//-- 表有有效卡 --
		if (eqIgno(hCard.currentCode, "0"))
			hCard.oppostDate = "";
		
		updateCrdCard();
		insertRskAcnolog(5);
		insertCrdJcic();
		
		/* 正卡 For col_stop_log & crd_apscard */
		if (eqIgno(hCard.supFlag, "0")) {
			if (eq(hCard.comboIndicator,"Y") && eq(hCard.currentCode, "0")) {
				insertCrdStopLog();
			}
		}
//		else {
//			insertOnbat2ccas();
//		}
		
		if (eq(hCard.currentCode, "0")) {
			insertCrdApscard();
		}
		insertColStopLog();
	}
}

//=****************************************************************************
void updateCrdCard() throws Exception {
	// 20030402 修改oppost_date 若原oppost_date是空白的才帶入busi_business_date,若是有值則不變 --
	// RECS-s980929-049 正卡強停，附卡申停 --
	if (tidCardU <= 0) {
		sqlCmd = " update crd_card set "
			+ " current_code = decode(sup_flag,'0','3','1') ,"
			+ " oppost_reason = decode(sup_flag,'0','J2','Q3') ,"
			+ " oppost_date = decode(oppost_date,'',?,oppost_date) ,"
			+ commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where card_no =? ";
		tidCardU = ppStmtCrt("crd_card-U1","");
	}
	
	ppp(1, hBusiDate);
	ppp(hCard.cardNo);
	sqlExec(tidCardU);
	if (sqlNrow < 0) {
		printf("update crd_card error [%s]", hCard.cardNo);
		this.errExit(0);
	}
	
	//--寫入停掛檔
	hCard.oppostReason ="J2";
	if (eq(hCard.supFlag,"1")) hCard.oppostReason="Q1";
	hFiscReason = getFiscReason(hCard.oppostReason);
	if (empty(hCard.oppostDate)) hCard.oppostDate =hBusiDate;
	insertCcaOpposition();
	//--寫入送Outgoing
	ccaOutGoing.InsertCcaOutGoing(hCard.cardNo, hCard.currentCode, sysDate, hCard.oppostReason);
}


String getFiscReason(String oppostReason) throws Exception {
	
	String sql1 = "select fisc_opp_code from cca_opp_type_reason where opp_status = ? ";
	sqlSelect(sql1,new Object[] {oppostReason});
	
	if(sqlNrow > 0 ) {
		return colSs("fisc_opp_code");
	}
	
	return "";
}

com.Parm2sql ttOppo=null;
void insertCcaOpposition() throws Exception {
	if (ttOppo ==null) {
		ttOppo =new com.Parm2sql();
		ttOppo.insert("cca_opposition");
	}

	String lsOppType="3";
	if (eq(hCard.supFlag,"1")) {
		lsOppType ="1";
	}

	double llCcasIndx =getCcasIndex(hCard.cardNo);

	ttOppo.aaa("card_no", hCard.cardNo);
	ttOppo.aaa("card_acct_idx", llCcasIndx);
	ttOppo.aaa("debit_flag", "N");
	ttOppo.aaa("card_type", hCard.cardType);
	ttOppo.aaa("bin_type", hCard.binType);
	ttOppo.aaa("group_code", hCard.groupCode);
	ttOppo.aaa("from_type", "0");
	ttOppo.aaa("oppo_type", lsOppType);
	ttOppo.aaa("oppo_status", hCard.oppostReason);
	ttOppo.aaa("oppo_user", hModUser);
	ttOppo.aaa("oppo_date", hCard.oppostDate);
	ttOppo.aaa("oppo_time", sysTime);
	ttOppo.aaa("neg_del_date", hCard.newEndDate);
	ttOppo.aaa("renew_flag", "");
	ttOppo.aaa("renew_urgen", "");
	ttOppo.aaa("opp_remark", "欠款強停");
//	ttOppo.aaa("outgo_proc", "0");
	ttOppo.aaa("fisc_reason_code", hFiscReason);
	ttOppo.aaaYmd("crt_date");
	ttOppo.aaaTime("crt_time");
	ttOppo.aaa("crt_user", hModPgm);
	ttOppo.aaaModxxx(hModUser,hModPgm);

	if (ttOppo.ti <=0) {
		ttOppo.ti =ppStmtCrt("tt_oppo.A",ttOppo.getConvSQL());
	}

	sqlExec(ttOppo.ti,ttOppo.getConvParm());
	if (sqlNrow <=0) {
		if (sqlDuplRecord) {
			updateCcaOpposition(lsOppType);
			return;
		}
		sqlerr("insert cca_opposition error");
		errExit();
	}
}

com.Parm2sql ttOppo2=null;
void updateCcaOpposition(String aOppoType) throws Exception {
	if (ttOppo2 ==null) {
		ttOppo2 =new Parm2sql();
		ttOppo2.update("cca_opposition");
	}

	ttOppo2.aaa("from_type", "0");
	ttOppo2.aaa("oppo_type", aOppoType);
	ttOppo2.aaa("oppo_status", hCard.oppostReason);
	ttOppo2.aaa("oppo_user", hModUser);
	//tt_oppo2.aaa("oppo_date", h_card.oppost_date);
	//tt_oppo2.aaa("oppo_time", hh.oppo_time);
	ttOppo2.aaa("logic_del", "");
	ttOppo2.aaa("logic_del_date", "");
	ttOppo2.aaa("logic_del_time", "");
	ttOppo2.aaa("logic_del_user", "");
//	ttOppo2.aaa("outgo_proc", "0");
	ttOppo2.aaa("fisc_reason_code", hFiscReason);
	ttOppo2.aaaYmd("chg_date");
	ttOppo2.aaaTime("chg_time");
	ttOppo2.aaa("chg_user", hModUser);
	ttOppo2.aaaModxxx(hModUser,hModPgm);
	ttOppo2.aaaWhere(" where card_no =?", hCard.cardNo);

	if (ttOppo2.ti <=0) {
		ttOppo2.ti =ppStmtCrt("tt_oppo2.U",ttOppo2.getConvSQL());
	}

	sqlExec(ttOppo2.ti,ttOppo2.getConvParm());
	if (sqlNrow <=0) {
		printf("update cca_opposition error, kk[%s]", hCard.cardNo);
	}
}

int tiCcidx=-1;
double getCcasIndex(String aCardNo) throws Exception {
	if (tiCcidx <=0) {
		sqlCmd ="select card_acct_idx from cca_card_base"+
				" where card_no =?";
		tiCcidx =ppStmtCrt("ti_ccidx.S",sqlCmd);
	}

	ppp(1,aCardNo);
	sqlSelect(tiCcidx);
	if (sqlNrow <=0)
		return 0;

	return colNum("card_acct_idx");
}

//=**********************************************************************************
void selectCcaCardAcct() throws Exception {
	hAclg.acctJrnlBal =0;
	hAclg.blockReason ="";
	hAclg.blockReason2 ="";
	hAclg.blockReason3 ="";
	hAclg.blockReason4 ="";
	hAclg.blockReason5 ="";
	hAclg.specStatus ="";
	
	if (tidCcasAcctS<=0) {
		sqlCmd ="select block_reason1,"
				+" block_reason2,"
				+" block_reason3,"
				+" block_reason4,"
				+" block_reason5,"
				+" spec_status,"
				+" acct_jrnl_bal"
				+" from cca_card_acct A left join act_acct B"
				+"       on A.p_seqno =B.p_seqno and A.debit_flag<>'Y'"
				+" where A.acno_flag<>'Y'"
				+" and A.p_seqno =?"
				+" and A.debit_flag <>'Y'"
				;
		tidCcasAcctS =ppStmtCrt("tid_ccas_acct_S","");
	}
	
	ppp(1,hAcno.pSeqno);
	daoTid ="ccat.";
	sqlSelect(tidCcasAcctS);
	if (sqlNrow<=0)
		return;
	
	hAclg.acctJrnlBal =colNum("ccat.acct_jrnl_bal");
	hAclg.blockReason =colSs("ccat.block_reason1");
	hAclg.blockReason2 =colSs("ccat.block_reason1");
	hAclg.blockReason3 =colSs("ccat.block_reason1");
	hAclg.blockReason4 =colSs("ccat.block_reason1");
	hAclg.blockReason5 =colSs("ccat.block_reason1");
	hAclg.specStatus =colSs("ccat.spec_status");
}

private com.Parm2sql ttAclg=null;
//=======================================================
void insertRskAcnolog(int aiFlag) throws Exception {
	// relate_code: S.連動強停, B.連動凍結
	lsKindFlag = "";
	lsCardNo = "";
	if (aiFlag == 5) {
		lsKindFlag = "C";
		lsCardNo = hAclg.cardNo;
	}
	else {
		lsKindFlag = "A";
		lsCardNo = "";
		//--
		selectCcaCardAcct();
	}
	
	if (ttAclg==null) {
		ttAclg = new Parm2sql();
		ttAclg.insert("rsk_acnolog");
	}
	ttAclg.aaa("kind_flag",lsKindFlag);
	ttAclg.aaa("card_no",lsCardNo);
	ttAclg.aaa("acno_p_seqno",hAcno.acnoPSeqno);
	ttAclg.aaa("acct_type",hAcno.acctType);
	ttAclg.aaa("id_p_seqno",hAcno.idPSeqno);
	ttAclg.aaa("log_date",hBusiDate);
	ttAclg.aaa("log_mode","2");
	ttAclg.aaa("log_type","2");
	ttAclg.aaa("log_reason",hTempStopWhy);
	ttAclg.aaa("log_not_reason",hTempNotstopWhy);
	if (empty(hTempNotstopWhy))
		ttAclg.aaa("fit_cond","N");
	else ttAclg.aaa("fit_cond","Y");
	ttAclg.aaa("relate_code","S");
	ttAclg.aaa("rela_p_seqno", hRelaPSeqno);
	ttAclg.aaa("aft_loc_amt",hAcno.lineOfCreditAmt);
	ttAclg.aaa("acct_jrnl_bal",hAclg.acctJrnlBal);
	ttAclg.aaa("ccas_mcode_aft",hAcno.payRateGet(1));
	ttAclg.aaa("block_reason",hAclg.blockReason);
	ttAclg.aaa("block_reason2",hAclg.blockReason2);
	ttAclg.aaa("block_reason3",hAclg.blockReason3);
	ttAclg.aaa("block_reason4",hAclg.blockReason4);
	ttAclg.aaa("block_reason5",hAclg.blockReason5);
	ttAclg.aaa("spec_status",hAclg.specStatus);
	ttAclg.aaaModxxx(hModUser,hModPgm);
	
	if (ttAclg.ti <=0) {
		ttAclg.ti =ppStmtCrt("tt_aclg-A",ttAclg.getConvSQL());
	}
	
	Object[] pps = ttAclg.getConvParm();
	//ddd(t_aclg.sql_from,pps);
	sqlExec(ttAclg.ti, pps);
	if (sqlNrow <= 0) {
		printf("insert rsk_acnolog error");
		errExit(0);
	}
}

//=****************************************************************************
void insertCrdJcic() throws Exception {
	String lsCurrent = "3";
	String lsOppoReason = "J2";
	if (!eq(hCard.supFlag, "0")) {
		lsCurrent = "1";
		lsOppoReason = "Q3";
	}
	
	hCdjcRowid = "";
	if (tidJcicS1 <= 0) {
		sqlCmd = " select hex(rowid) as h_cdjc_rowid "
			+ " from crd_jcic "
			+ " where card_no = ? "
			+ " and nvl(to_jcic_date,'') ='' "
			+ " and trans_type = 'C' ";
		daoTable ="tid_jcic_S1";
		tidJcicS1 = ppStmtCrt();
	}
	ppp(1, hCard.cardNo);
	sqlSelect(tidJcicS1);
	
	if (sqlNrow == 0) {
		if (tJcic1==null) {
			tJcic1 =new com.SqlParm();
			tJcic1.sqlFrom = "insert into crd_jcic ("
				+ " card_no ,"
				+ " crt_date ,"
				+ " crt_user ,"
				+ " trans_type ,"
				+ " current_code ,"
				+ " oppost_reason ,"
				+ " oppost_date ,"
				+ " is_rc ,"
				+ " mod_user ,"
				+ " mod_time ,"
				+ " mod_pgm ,"
				+ " mod_seqno "
				+ " ) values ("
				+ tJcic1.pmkk(0, ":card_no ,")
				+ " to_char(sysdate,'yyyymmdd') , "
				+ tJcic1.pmkk(":crt_user ,")
				+ " 'C' ,"
				+ tJcic1.pmkk(":current_code ,")
				+ tJcic1.pmkk(":oppost_reason ,")
				+ tJcic1.pmkk(":oppost_date ,")
				+ tJcic1.pmkk(":is_rc ,")
				+ tJcic1.pmkk(":mod_user ,")
				+ " sysdate , "
				+ tJcic1.pmkk(":mod_pgm ,")
				+ " '1' "
				+ " )";
			daoTable = "crd_jcic-A1";
			sqlCmd =tJcic1.sqlFrom;
			tJcic1.pfidx = ppStmtCrt();
			if (tJcic1.pfidx <=0) {
				errIndex(0);
			}
		}
		tJcic1.ppp("card_no", hCard.cardNo);
		tJcic1.ppp("crt_user", hModUser);
		tJcic1.ppp("current_code", lsCurrent);
		tJcic1.ppp("oppost_reason", lsOppoReason);
		tJcic1.ppp("oppost_date", hBusiDate);
		tJcic1.ppp("is_rc", hAcno.rcUseIndicator);
		tJcic1.ppp("mod_user", hModUser);
		tJcic1.ppp("mod_pgm", hModPgm);
		
		Object[] pps = tJcic1.getConvParm();
		//ddd(t_jcic1.sql_from, pps);
		sqlExec(tJcic1.pfidx, pps);
		if (sqlNrow <= 0) {
			printf("insert crd_jcic error");
			errExit(0);
		}
		return;
	}
	
	// -update---------------------------------------------
	hCdjcRowid = colSs("h_cdjc_rowid");
	if (tidJcicU <= 0) {
		sqlCmd = " update crd_jcic set "
			+ " current_code =? ,"
			+ " oppost_reason =? ,"
			+ " oppost_date = ? ,"
			+ " is_rc = ? ,"
			+ commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where rowid = ? ";
		daoTable ="tid_jcic_U";
		tidJcicU = this.ppStmtCrt();
	}
	Object[] pp = new Object[] {
		lsCurrent, 
		lsOppoReason, 
		hBusiDate, 
		hAcno.rcUseIndicator, 
		commSqlStr.ss2rowid(hCdjcRowid)
	};
	sqlExec(tidJcicU, pp);
	if (sqlNrow <= 0) {
		printf("update crd_jcic error ");
		errExit(0);
	}
}

//=****************************************************************************
void insertColStopLog() throws Exception {
//	String[] aa_midno=new String[]{"","","",""};
//	String[] aa_idno=new String[]{"","","",""};
//	select_crd_idno(h_card.major_id_p_seqno,aa_midno);
//	if (eq(h_card.sup_flag,"1") && No_empty(h_card.id_p_seqno)) {
//		select_crd_idno(h_card.id_p_seqno,aa_midno);
//	}
	
	if (tSlog ==null) {
		tSlog =new com.SqlParm();
		tSlog.sqlFrom = "insert into col_stop_log (" 
				+ " insert_date ,"
				+ " major_id_p_seqno ," 
//				+ " major_id ," 
//				+ " major_id_code ,"
				+ " id_p_seqno ," 
//				+ " id_no ," 
//				+ " id_code ," 
				+ " card_no ,"
				+ " oppost_date ," 
				+ " p_seqno ," 
				+ " mod_time "
				+ " ) values ("
				+ tSlog.pmkk(0, ":insert_date ,")
				+ tSlog.pmkk(":major_id_p_seqno ,")
//				+ t_slog.pmkk(":major_id ,") 
//				+ t_slog.pmkk(":major_id_code ,")
				+ tSlog.pmkk(":id_p_seqno ,") 
//				+ t_slog.pmkk(":id_no ,")
//				+ t_slog.pmkk(":id_code ,") 
				+ tSlog.pmkk(":card_no ,")
				+ tSlog.pmkk(":oppost_date ,") 
				+ tSlog.pmkk(":p_seqno ,")
				+ commSqlStr.sysDTime
				+ " )";
		daoTable = "col_stop_log-A1";
      sqlCmd =tSlog.sqlFrom;
		tSlog.pfidx =ppStmtCrt();
	}
	tSlog.ppp("insert_date", hBusiDate);
	tSlog.ppp("major_id_p_seqno", hCard.majorIdPSeqno);
//	t_slog.ppp("major_id", aa_midno[0]);
//	t_slog.ppp("major_id_code", aa_midno[1]);
	tSlog.ppp("id_p_seqno", hCard.idPSeqno);
//	t_slog.ppp("id_no", aa_idno[0]);
//	t_slog.ppp("id_code", aa_idno[1]);
	tSlog.ppp("card_no", hCard.cardNo);
	tSlog.ppp("oppost_date", hBusiDate);
	tSlog.ppp("p_seqno", hAcno.pSeqno);
	tSlog.ppp("mod_user", hModUser);
	tSlog.ppp("mod_pgm", hModPgm);
	
	Object[] pps = tSlog.getConvParm();
	//ddd(t_slog.sql_from, pps);
	sqlExec(tSlog.pfidx, pps);
	if (sqlNrow <= 0) {
		printf("insert col_stop_log error");
		this.errExit(0);
	}
}
//=*********************************************************************************
void insertCrdApscard() throws Exception {
	String[] aaIdno0=new String[]{"","","",""};
	String[] aaIdno1=new String[]{"","","",""};
	selectCrdIdno(hCard.majorIdPSeqno,aaIdno0);
	if (eq(hCard.supFlag,"1")) {
		selectCrdIdno(hCard.majorIdPSeqno,aaIdno1);
	}
	
	daoTable = "crd_apscard-A1";
	if (tApsc ==null) {
		tApsc =new com.SqlParm();
		tApsc.sqlFrom = "insert into crd_apscard (" 
				+ " crt_datetime ,"
				+ " card_no ," 
				+ " valid_date ," 
				+ " stop_date ," 
				+ " reissue_date ,"
				+ " stop_reason ," 
				+ " mail_type ," 
				+ " mail_no ," 
				+ " mail_branch ,"
				+ " mail_date ," 
				+ " pm_id ," 
				+ " pm_id_code ," 
				+ " pm_birthday ,"
				+ " sup_id ," 
				+ " sup_id_code ," 
				+ " sup_birthday ," 
				+ " corp_no ,"
				+ " corp_no_code ," 
				+ " card_type ," 
				+ " pm_name ," 
				+ " sup_name ,"
				+ " group_code ," 
				+ " mod_user, mod_time, mod_pgm, mod_seqno " 
				+ " ) values ("
				+ " to_char(sysdate,'yyyymmddhh24miss') ,"
				+ tApsc.pmkk(0, ":card_no ,") 
				+ tApsc.pmkk(":valid_date ,")
				+ tApsc.pmkk(":stop_date ,") 
				+ tApsc.pmkk(":reissue_date ,")
				+ " '1' ," 	//stop_reason
				+ tApsc.pmkk(":mail_type ,") 
				+ tApsc.pmkk(":mail_no ,")
				+ tApsc.pmkk(":mail_branch ,") 
				+ tApsc.pmkk(":mail_date ,")
				+ tApsc.pmkk(":pm_id ,") 
				+ tApsc.pmkk(":pm_id_code ,")
				+ tApsc.pmkk(":pm_birthday ,") 
				+ tApsc.pmkk(":sup_id ,")
				+ tApsc.pmkk(":sup_id_code ,") 
				+ tApsc.pmkk(":sup_birthday ,")
				+ tApsc.pmkk(":corp_no ,") 
				+ tApsc.pmkk(":corp_no_code ,")
				+ tApsc.pmkk(":card_type ,") 
				+ tApsc.pmkk(":pm_name ,")
				+ tApsc.pmkk(":sup_name ,") 
				+ tApsc.pmkk(":group_code ,")
				+ tApsc.pmkk(":mod_user ,") 
				+ " sysdate , "
				+ tApsc.pmkk(":mod_pgm ,") 
				+ " '1' " 
				+ " )";
		daoTable ="t_apsc-A";
		sqlCmd =tApsc.sqlFrom;
		tApsc.pfidx = ppStmtCrt();
	}
	tApsc.ppp("card_no", hCard.cardNo);
	tApsc.ppp("valid_date", hCard.newEndDate);
	tApsc.ppp("stop_date", hBusiDate);
	tApsc.ppp("reissue_date", hCard.reissueDate);
	tApsc.ppp("mail_type", hCard.mailType);
	tApsc.ppp("mail_no", hCard.mailNo);
	tApsc.ppp("mail_branch", hCard.mailBranch);
	tApsc.ppp("mail_date", hCard.mailProcDate);
	tApsc.ppp("pm_id", aaIdno0[0]);
	tApsc.ppp("pm_id_code", aaIdno0[1]);
	tApsc.ppp("pm_birthday", aaIdno0[3]);
	tApsc.ppp("sup_id", aaIdno1[0]);
	tApsc.ppp("sup_id_code", aaIdno1[1]);
	tApsc.ppp("sup_birthday", aaIdno1[3]);
	tApsc.ppp("corp_no", hCard.corpNo);
	tApsc.ppp("corp_no_code", hCard.corpNoCode);
	tApsc.ppp("card_type", hCard.cardType);
	tApsc.ppp("pm_name", aaIdno0[2]);
	tApsc.ppp("sup_name", aaIdno1[2]);
	tApsc.ppp("group_code", hCard.groupCode);
	tApsc.ppp("mod_user", hModUser);
	tApsc.ppp("mod_pgm", hModPgm);
	
	Object[] pps = tApsc.getConvParm();
	//ddd(t_apsc.sql_from, pps);
	sqlExec(tApsc.pfidx, pps);
	if (sqlNrow <= 0) {
		printf("insert crd_apscard error, card_no[%s]", hCard.cardNo);
		this.errExit(0);
	}
}

//=****************************************************************************
void selectCrdIdno(String aIdPseqno, String[] aIdno) throws Exception {
	
	aIdno =new String[]{"","","",""};
	
	if (tidIdnoS <= 0) {
		sqlCmd = "select id_no, id_no_code, chi_name,"
				+ " birthday" 
				+ " from crd_idno "
				+ " where id_p_seqno = ?";
		daoTable ="tid_idno_S";
		tidIdnoS =ppStmtCrt();
		if (tidIdnoS <=0) {
			errIndex(0);
		}
	}
	ppp(1, aIdPseqno);
	sqlSelect(tidIdnoS);
	if (sqlNrow < 0) {
		printf("select crd_idno error ");
		this.errExit(0);
	}
	if (sqlNrow==0) return;
	
	aIdno[0] =colSs("id_no");
	aIdno[1] =colNvl("id_no_code","0");
	aIdno[2] =colSs("chi_name");
	aIdno[3] =colSs("birthday");
}

//=****************************************************************************
void insertCrdStopLog() throws Exception {

	daoTable = "col_stop_log-A1";
	if (tCrdStop == null) {
		tCrdStop =new com.SqlParm();
		tCrdStop.sqlFrom = "insert into crd_stop_log (" 
				+ " proc_seqno ,"
				+ " crt_time ," 
				+ " card_no ," 
				+ " current_code ," 
				+ " oppost_reason ,"
				+ " oppost_date ," 
				+ " trans_type ," 
				+ " send_type ," 
				+ " mod_user, mod_time, mod_pgm, mod_seqno " 
				+ " ) values ("
				+ commSqlStr.seqEcsStop+","
				+ " to_char(sysdate,'yyyymmddhh24miss'), "
				+ tCrdStop.pmkk(":card_no ,") 
				+ " '3' ," 	//current_code
				+ " 'J2' ,"		//oppost_reason
				+ tCrdStop.pmkk(":oppost_date ,") 
				+ " '03' ," 	//-強停-trans_type
				+ " '2' ,"		//-MQUEUE: send_type
				+ tCrdStop.pmkk(":mod_user ,") 
				+ " sysdate , "
				+ tCrdStop.pmkk(":mod_pgm ,") 
				+ " '1' " 
				+ " )";
		daoTable ="t_crd_stop";
		sqlCmd =tCrdStop.sqlFrom;
		tCrdStop.pfidx =ppStmtCrt();
	}
//	t_cstop.ppp("proc_seqno", h_proc_seqno);
	tCrdStop.ppp("card_no", hCard.cardNo);
	tCrdStop.ppp("oppost_date", hBusiDate);
	tCrdStop.ppp("mod_user", hModUser);
	tCrdStop.ppp("mod_pgm", hModPgm);
	Object[] pps = tCrdStop.getConvParm();
	//ddd(t_crd_stop.sql_from, pps);
	sqlExec(tCrdStop.pfidx, pps);
	if (sqlNrow <= 0) {
		printf("insert crd_stop_log error");
		this.errExit(0);
	}
}

private com.Parm2sql ttOnba=null;
//==========================================
void insertOnbat2ccas() throws Exception {
	if (ttOnba ==null) {
		ttOnba = new Parm2sql();
		ttOnba.insert("onbat_2ccas");
	}
	ttOnba.aaa("trans_type","6");
	ttOnba.aaa("to_which",2);
	ttOnba.aaaDtime("dog");
	ttOnba.aaa("proc_mode","B");
	ttOnba.aaa("proc_status",0);
	ttOnba.aaa("card_no",hCard.cardNo);
	ttOnba.aaa("acct_type",hCard.acctType);
	ttOnba.aaa("acno_p_seqno", hCard.acnoPSeqno);
	ttOnba.aaa("id_p_seqno", hCard.idPSeqno);
	ttOnba.aaa("corp_p_seqno", hCard.corpPSeqno);
	ttOnba.aaa("card_catalog", hCard.comboIndicator);
	ttOnba.aaa("opp_type", "1");
	ttOnba.aaa("opp_reason","Q3");
	ttOnba.aaaYmd("opp_date");
	ttOnba.aaa("debit_flag","N");
	ttOnba.aaa("mod_pgm",hModPgm);

	if (ttOnba.ti <=0) {
		ttOnba.ti =ppStmtCrt("tt_onbat-A",ttOnba.getConvSQL());
	}

	sqlExec(ttOnba.ti,ttOnba.getConvParm());
	if (sqlNrow<=0) {
		printf("insert_onbat_2ccas error; kk[%s]",hCard.cardNo);
		errExit(0);
	}
}

}
