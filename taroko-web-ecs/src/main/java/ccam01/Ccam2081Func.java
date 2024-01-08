package ccam01;

/** 預製卡啟用
 * 2021/11/08 V1.00.00   ryan   program initial
 * 2021/11/15 V1.00.01   ryan   增加欄位
 * 2021/11/25 V1.00.02   Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
 * 2021/12/15 V1.00.03   Ryan   insert cca_card_base 增加欄位 card_indicator = 1
 * 2022/03/01 V1.00.04   Jusrin add resetMsg()
 * 2022/04/06 V1.00.05   Justin add birthday and cellar_phone in the activation page
 * */
import busi.FuncAction;
import busi.ecs.CommBusiCrd;

public class Ccam2081Func extends FuncAction {
	CommBusiCrd comcrd = new CommBusiCrd();
	String icFlag = "", electronicCode = "", binType = "", cardIndicator = "", stmtCycle = "", idPSeqno = "";
	String eNews = "", acceptMbullet = "", acceptCallSell = "", acceptDm = "", dmFromMark = "", dmChgDate = "",
			acceptSms = "";
	String cardAcctIdx = "", acnoPSeqno = "";

	@Override
	public void dataCheck() {

		if (comcrd.checkId(wp.itemStr("apply_id"))) {
			wp.alertMesg("輸入的身分證字號邏輯有誤");
			errmsg("輸入的身分證字號邏輯有誤");
			return;
		}
		
		
		strSql = " select count(*) cnt from dba_acno where acct_no = ?";
		setString(1, wp.itemStr("act_no"));
		sqlSelect(strSql);
		if (colInt("cnt") > 0) {
			wp.alertMesg("金融帳號已存在帳戶資料檔");
			errmsg("金融帳號已存在帳戶資料檔");
			return;
		}
		
		if(!wp.itemEmpty("in_main_date")) {
			wp.alertMesg("卡片已啟用，不可重複啟用");
			errmsg("卡片已啟用，不可重複啟用");
			return;
		}
		
		if(wp.itemEq("prefab_cancel_flag","Y")) {
			wp.alertMesg("卡片已註銷");
			errmsg("卡片已註銷");
			return;
		}
		
		if (chkStrend(getSysDate(), wp.itemStr("valid_to"))==-1) {
			wp.alertMesg("卡片效期小於系統日");
			errmsg("卡片效期小於系統日");
			return;
		}

		// 取得IC_FLAG、ELECTRONIC_CODE
		strSql = " select ic_flag,electronic_code from crd_item_unit where card_type = ? and unit_code = ? ";
		setString(1, wp.itemStr("card_type"));
		setString(2, wp.itemStr("unit_code"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			icFlag = colStr("ic_flag");
			electronicCode = colStr("electronic_code");
		}

		//取得BIN_TYPE
		strSql = " select bin_type from ptr_bintable where bin_no || bin_no_2_fm || '0000' <= ? and bin_no || bin_no_2_to || '9999' >= ? ";
		setString(1, wp.itemStr("card_no"));
		setString(2, wp.itemStr("card_no"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			binType = colStr("bin_type");
		}

		// 取得CARD_INDICATOR 、STMT_CYCLE
		strSql = " select a.card_indicator,a.stmt_cycle from dbp_acct_type a,dbp_prod_type b where b.group_code = ? and b.card_type = '' and a.acct_type = b.acct_type ";
		setString(1, wp.itemStr("group_code"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			cardIndicator = colStr("card_indicator");
			stmtCycle = colStr("stmt_cycle");
		} else {
			strSql = " select a.card_indicator,a.stmt_cycle from dbp_acct_type a,dbp_prod_type b where b.card_type = ? and a.acct_type = b.acct_type ";
			setString(1, wp.itemStr("card_type"));
			sqlSelect(strSql);
			if (sqlRowNum > 0) {
				cardIndicator = colStr("card_indicator");
				stmtCycle = colStr("stmt_cycle");
			}
		}

		// 確認DBC_IDNO是否已存在
		if (!chkDbcIdno())
			return;

		// 寫入 DBA_ACNO
		if(!chkDbaAcno()) 
			return;
		
		// 寫入 DBC_CARD
		if(insertDbcCard()!=1)
			return;
		
		// 寫入 CCA_CARD_BASE
		if(insertCcaCardBase()!=1)
			return;
		
		// 異動DBC_EMBOSS
		strSql = "update dbc_emboss set apply_id = ? ,act_no = ? , in_main_date = to_char(sysdate,'yyyymmdd') "
				+ ",rtn_nccc_date = to_char(sysdate,'yyyymmdd') ,mod_time = sysdate ,mod_user = ? ,mod_pgm = ? "
				+ ", BIRTHDAY = ? , CELLAR_PHONE = ? "
				+ " where batchno = ? and recno = ? ";
		sqlExec(strSql, new Object[] { wp.itemStr("apply_id"), wp.itemStr("act_no"), 
				modUser, modPgm, 
				wp.itemStr("birthday"), wp.itemStr("cellar_phone"),
				wp.itemStr("batchno"), wp.itemStr("recno") });
		if (sqlRowNum <= 0) {
			errmsg("update dbc_emboss error");
			return;
		}
		
	}

	boolean chkDbcIdno() {

		strSql = " select id_p_seqno,id_no_code from dbc_idno where id_no = ? and id_no_code = '0' fetch first 1 rows only ";
		setString(1, wp.itemStr("apply_id"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			idPSeqno = colStr("id_p_seqno");
			return true;
		}

		strSql = " select id_p_seqno from crd_idno_seqno where id_no = ? ";
		setString(1, wp.itemStr("apply_id"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			idPSeqno = colStr("id_p_seqno");
			if (insertDbcIdno() != 1)
				return false;
			return true;
		}

		strSql = " select to_char(ecs_acno.nextval,'0000000000') id_p_seqno from dual ";
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			wp.alertMesg("無法取得ID_P_SEQNO");
			errmsg("無法取得ID_P_SEQNO");
			return false;
		}
		idPSeqno = colStr("id_p_seqno");

		strSql = " insert into crd_idno_seqno (id_no,id_p_seqno,id_flag,bill_apply_flag,debit_idno_flag) values (?,?,'','','Y') ";
		setString(1, wp.itemStr("apply_id"));
		setString(2, idPSeqno);
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("insert crd_idno_seqno error");
			return false;
		}

		strSql = "select e_news,accept_mbullet,accept_call_sell,accept_dm,dm_from_mark,dm_chg_date,accept_sms from crd_idno where id_no = ? and id_no_code = '0' ";
		setString(1, wp.itemStr("apply_id"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			eNews = colStr("e_news");
			acceptMbullet = colStr("accept_mbullet");
			acceptCallSell = colStr("accept_call_sell");
			acceptDm = colStr("accept_dm");
			dmFromMark = colStr("dm_from_mark");
			dmChgDate = colStr("dm_chg_date");
			acceptSms = colStr("accept_sms");
		}

		if (insertDbcIdno() != 1)
			return false;

		return true;
	}

	boolean chkDbaAcno() {

		strSql = " select to_char(ecs_acno.nextval,'0000000000') acno_seqno from dual ";
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			wp.alertMesg("無法取得acno_seqno");
			errmsg("無法取得acno_seqno");
			return false;
		}
		acnoPSeqno = colStr("acno_seqno");
		
		if(insertDbaAcno()!=1)
			return false;
		
		// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//		strSql = " select substr(to_char(ecs_card_acct_idx.nextval,'0000000000'), 2,10) as new_card_acct_idx from dual ";
//		sqlSelect(strSql);
//		if (sqlRowNum <= 0) {
//			wp.alertMesg("無法取得CARD_ACCT_IDX(2)");
//			errmsg("無法取得CARD_ACCT_IDX(2)");
//			return false;
//		}
//		cardAcctIdx = colStr("new_card_acct_idx");	
		cardAcctIdx = Integer.toString(Integer.parseInt(acnoPSeqno));
		
		if(insertCcaCardAcct(acnoPSeqno) != 1)
			return false;

		return true;
	}

	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {

		dataCheck();
		if (rc != 1) {
			return rc;
		}

		return rc;
	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

	int insertDbcIdno() {
		msgOK();
		sql2Insert("dbc_idno");
		addsqlParm("?", "id_p_seqno", idPSeqno);
		addsqlParm(",?", ", id_no", wp.itemStr("apply_id"));
		addsqlParm(", id_no_code", ", '0'");
		addsqlYmd(", card_since");
		addsqlParm(",?", ", fst_stmt_cycle", stmtCycle);
		addsqlYmd(", crt_date");
		addsqlParm(",?", ", e_news", nvl(eNews, "N"));
		addsqlParm(",?", ", accept_mbullet", nvl(acceptMbullet, "Y"));
		addsqlParm(",?", ", accept_call_sell", nvl(acceptCallSell, "Y"));
		addsqlParm(",?", ", accept_dm", nvl(acceptDm, "Y"));
		addsqlParm(",?", ", dm_from_mark", nvl(dmFromMark, "A"));
		addsqlParm(",?", ", dm_chg_date", nvl(dmChgDate, getSysDate()));
		addsqlParm(",?", ", accept_sms", nvl(acceptSms, "Y"));
		addsqlParm(",?", ", BIRTHDAY", wp.itemStr("birthday")); // 2022/04/06 Justin
		addsqlParm(",?", ", CELLAR_PHONE", wp.itemStr("cellar_phone")); // 2022/04/06 Justin
		addsqlParm(", msg_flag", ", 'Y'");
		addsqlParm(",?", ", mod_user", modUser);
		addsqlDate2(", mod_time");
		addsqlParm(",?", ", mod_pgm", modPgm);
		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert dbc_idno error");
		}
		return rc;
	}

	int insertCcaCardAcct(String pSeqno) {
		msgOK();
		strSql = " insert into cca_card_acct  (" + " acno_p_seqno, " + " debit_flag, " + " acno_flag, " + " acct_type, "
				+ " id_p_seqno, " + " p_seqno, " + " card_acct_idx, " + " mod_user, " + " mod_time, " + " mod_pgm, "
				+ " mod_seqno " + " ) values ( ?,'Y','1',?,?,?,?,?,sysdate,?,0)";
		sqlExec(strSql,
				new Object[] { pSeqno, wp.itemStr("acct_type"), idPSeqno, pSeqno, cardAcctIdx, modUser, modPgm });
		if (sqlRowNum <= 0) {
			errmsg("insert cca_card_acct error");
			return rc;
		}

		strSql = " insert into cca_consume  (" + " card_acct_idx, " + " p_seqno, " + " mod_user, " + " mod_time, "
				+ " mod_pgm " + " ) values ( ?,?,?,sysdate,?)";
		sqlExec(strSql, new Object[] { cardAcctIdx, pSeqno, modUser, modPgm });
		if (sqlRowNum <= 0) {
			errmsg("insert cca_consume error");
			return rc;
		}
		
		return rc;
	}
	
	int insertDbaAcno() {
		msgOK();
		sql2Insert("dba_acno");
		addsqlParm("?", "p_seqno", acnoPSeqno);
		addsqlParm(",?", ", acct_type", wp.itemStr("acct_type"));
		addsqlParm(",?", ", acct_key", wp.itemStr("apply_id") + "0");
		addsqlParm(", acct_status", ", '1'");
		addsqlParm(", acct_sub_status", ", '1'");
		addsqlParm(",?", ", stmt_cycle", stmtCycle);
		addsqlParm(",?", ", id_p_seqno", idPSeqno);
		addsqlParm(",?", ", acct_holder_id", wp.itemStr("apply_id"));
		addsqlParm(", acct_holder_id_code", ", '0'");
		addsqlParm(",?", ", card_indicator", cardIndicator);
		addsqlParm(", rc_use_b_adj", ", '1'");
		addsqlParm(", autopay_indicator", ", '1'");
		addsqlParm(", worse_mcode", ", '0'");
		addsqlParm(", legal_delay_code", ", '9'");
		addsqlParm(", inst_auth_loc_amt", ", '0'");
		addsqlParm(", special_stat_code", ", '5'");
		addsqlParm(",?", ", acct_no", wp.itemStr("act_no"));
		addsqlParm(", new_vdchg_flag", ", 'Y'");
		addsqlParm(",?", ", class_code", wp.itemStr("class_code"));
		addsqlYmd(", crt_date");
		addsqlParm(",?", ", mod_user", modUser);
		addsqlDate2(", mod_time");
		addsqlParm(",?", ", mod_pgm", modPgm);
		addsqlParm(", mod_seqno", ", 0");
		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert dba_acno error");
		}
		return rc;
	}
	
	int insertDbcCard() {
		msgOK();
		sql2Insert("dbc_card");
		addsqlParm("?", "card_no", wp.itemStr("card_no"));
		addsqlParm(",?", ", id_p_seqno", idPSeqno);
		addsqlParm(",?", ", group_code", wp.itemStr("group_code"));
		addsqlParm(",?", ", source_code", wp.itemStr("source_code"));
		addsqlParm(",?", ", unit_code", wp.itemStr("unit_code"));
		addsqlParm(",?", ", bin_no", wp.itemStr("bin_no"));
		addsqlParm(",?", ", bin_type", binType);
		addsqlParm(",?", ", sup_flag", wp.itemStr("sup_flag"));
		addsqlParm(", acno_flag", ", '1'");
		addsqlParm(",?", ", major_id_p_seqno", idPSeqno);
		addsqlParm(",?", ", major_card_no", wp.itemStr("card_no"));
		addsqlParm(", current_code", ", '0'");
		addsqlParm(",?", ", new_beg_date", wp.itemStr("valid_fm"));
		addsqlParm(",?", ", new_end_date", wp.itemStr("valid_to"));
		addsqlYmd(", issue_date");
		addsqlParm(",?", ", acct_type", wp.itemStr("acct_type"));
		addsqlParm(",?", ", p_seqno", acnoPSeqno);
		addsqlParm(",?", ", stmt_cycle", stmtCycle);
		addsqlParm(", activate_type", ", 'O'");
		addsqlParm(", activate_flag", ", '2'");
		addsqlYmd(", activate_date");
		addsqlParm(",?", ", acct_no", wp.itemStr("act_no"));
		addsqlParm(", beg_bal", ", 0");
		addsqlParm(", end_bal", ", 0");
		addsqlParm(",?", ", ic_flag", icFlag);
		addsqlParm(",?", ", card_ref_num", wp.itemStr("card_ref_num"));
		addsqlParm(",?", ", electronic_code", electronicCode);
		addsqlParm(", prefab_flag", ", 'Y'");
		addsqlParm(", prefab_use_code", ", '1'");
		addsqlParm(",?", ", card_type", wp.itemStr("card_type"));
		addsqlYmd(", crt_date");
		addsqlParm(",?", ", crt_user", modUser);
		addsqlYmd(", apr_date");
		addsqlParm(",?", ", apr_user", modUser);
		addsqlParm(",?", ", mod_user", modUser);
		addsqlDate2(", mod_time");
		addsqlParm(",?", ", mod_pgm", modPgm);
		addsqlParm(", mod_seqno", ", 0");
		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			wp.alertMesg("卡號已存在，無法啟用");
			errmsg("卡號已存在，無法啟用");
		}
		return rc;
	}
	
	int insertCcaCardBase() {
		msgOK();
		sql2Insert("cca_card_base");
		addsqlParm("?", "card_no", wp.itemStr("card_no"));
		addsqlParm(", debit_flag", ", 'Y'");
		addsqlParm(",?", ", bin_type", binType);
		addsqlParm(",?", ", id_p_seqno", idPSeqno);
		addsqlParm(",?", ", acno_p_seqno", acnoPSeqno);
		addsqlParm(",?", ", p_seqno", acnoPSeqno);
		addsqlParm(",?", ", major_id_p_seqno", idPSeqno);
		addsqlParm(", acno_flag", ", '1'");
		addsqlParm(",?", ", acct_type", wp.itemStr("acct_type"));
		addsqlParm(",?", ", sup_flag", wp.itemStr("sup_flag"));
		addsqlParm(",?", ", card_acct_idx", cardAcctIdx);
		addsqlParm(",?", ", card_indicator", "1");
		addsqlParm(",?", ", mod_user", modUser);
		addsqlDate2(", mod_time");
		addsqlParm(",?", ", mod_pgm", modPgm);
		addsqlParm(", mod_seqno", ", 0");
		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert cca_card_base error");
		}
		
		return rc;
	}
	
	public void resetMsg() {
		msgOK();
	}

}
