package ccam01;
/**
 * 2019-0925   JH    adj_inst_pct <=tot_amt_month
 * 19-0611:    JH    p_seqno >>acno_p_xxx
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * */

import busi.FuncAction;
import taroko.base.CommSqlStr;

public class Ccam2120Func extends FuncAction {
	String cardAcctIdx = "", lsStartDate = "", lsEndDate = "", adjRiskType = "";
	int lmPayAmt = 0, lmPayAmt2 = 0, lmAmt = 0;

	@Override
	public void dataCheck() {
		cardAcctIdx = wp.itemStr("card_acct_idx");
		lsStartDate = wp.itemStr("adj_eff_start_date");
		lsEndDate = wp.itemStr("adj_eff_end_date");

		String lsOriStartDate = "";
		String lsOriEndDate = "";

		lsOriStartDate = wp.itemStr("ori_start_date");
		lsOriEndDate = wp.itemStr("ori_end_date");

		if (this.ibDelete)
			return;

		if (wp.itemNum("adj_inst_pct") > wp.itemNum("tot_amt_month")) {
			errmsg("調整分期月限額: 不可 > 調整一般月限額");
			return;
		}
		if (empty(lsStartDate) || empty(lsEndDate)) {
			errmsg("有效日期 : 不可空白");
			return;
		}

		if (this.chkStrend(lsStartDate, lsEndDate) == -1) {
			errmsg("有效日期  起迄 錯誤");
			return;
		}
		if (this.ibAdd || !eqIgno(lsStartDate, lsOriStartDate) || !eqIgno(lsEndDate, lsOriEndDate)) {
			if (lsStartDate.compareTo(this.getSysDate()) < 0 || lsEndDate.compareTo(this.getSysDate()) < 0) {
				errmsg("有效日期 起迄 須大於等於 系統日期");
				return;
			}
		}

		if (empty(wp.itemStr("adj_area"))) {
			errmsg("適用地區別 : 不可空白");
			return;
		}

		if (empty(wp.itemStr("adj_reason"))) {
			errmsg("調整原因 : 不可空白");
			return;
		}
		if (checkParm() == false)
			return;
	}

	boolean checkParm() {

		int listCnt = wp.itemRows("risk_type");
		String[] listRisk = wp.itemBuff("risk_type");
		String[] listStartDate = wp.itemBuff("adj_date1");
		String[] listEndDate = wp.itemBuff("adj_date2");

		for (int ii = 0; ii < listCnt; ii++) {
			if (empty(adjRiskType))
				adjRiskType += listRisk[ii];
			else
				adjRiskType += "," + listRisk[ii];

			if (this.chkStrend(listStartDate[ii], listEndDate[ii]) == -1) {
				errmsg("風險分類:" + listRisk[ii] + " 有效日期  起迄 錯誤");
				return false;
			}

			if (commString.strToInt(listStartDate[ii]) >= commString.strToInt(lsStartDate) == false
					|| commString.strToInt(listStartDate[ii]) <= commString.strToInt(lsEndDate) == false) {
				errmsg("風險分類:" + listRisk[ii] + " 有效日期(起)需在臨調效期內");
				return false;
			}

			if (commString.strToInt(listEndDate[ii]) >= commString.strToInt(lsStartDate) == false
					|| commString.strToInt(listEndDate[ii]) <= commString.strToInt(lsEndDate) == false) {
				errmsg("風險分類:" + listRisk[ii] + " 有效日期(迄)需在臨調效期內");
				return false;
			}

		}

		return true;
	}

	@Override
	public int dbInsert() {

		return rc;
	}

	@Override
	public int dbUpdate() {
		// actionInit("U");
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		updateCcaCardAcct();
		if (rc == 1 && wp.itemEq("notice_flag", "Y")) {
			varsSet("org_tot_consume", wp.itemStr("line_credit_amt"));
			varsSet("lmt_tot_consume", wp.itemStr("tot_amt_month"));
			varsSet("adj_eff_start_date", wp.itemStr("adj_eff_start_date"));
			varsSet("adj_eff_end_date", wp.itemStr("adj_eff_end_date"));
			varsSet("adj_reason", wp.itemStr("adj_reason"));
			varsSet("adj_remark", wp.itemStr("adj_remark"));
			varsSet("adj_area", wp.itemStr("adj_area"));

			insertAdjNotice();
		}
		if (rc == 1) {
			insertLimitAdjLog();
		}
		if (rc == 1) {
			deleteCcaAdjParm();
		}

		return rc;
	}

	public int insertAdjNotice() {
		String lsPSeqno = "", lsIdPSeqno = "", lsSysDate = "";
		int liSelectCnt = 0;
		double ldCardAcctIdx = 0;
		lsPSeqno = wp.itemStr2("acno_p_seqno");
		ldCardAcctIdx = wp.itemNum("card_acct_idx");
		lsSysDate = getSysDate();
		log("p_seqno:" + lsPSeqno);
		if (empty(lsPSeqno)) {
			errmsg("帳戶流水號: 不可空白");
			return rc;
		}

		String sql1 = " select id_p_seqno from crd_card where p_seqno = ? and current_code ='0' and sup_flag ='1' ";
		sqlSelect(sql1, new Object[] { lsPSeqno });
		liSelectCnt = sqlRowNum;

		if (liSelectCnt <= 0) {
			rc = 1;
			return rc;
		}

		String sql2 = " select send_date , card_acct_idx , crt_date , id_no , id_code from cca_adj_notice "
				+ " where card_acct_idx = ? and crt_date = ? and id_p_seqno = ? ";

		for (int ii = 0; ii < liSelectCnt; ii++) {
			lsIdPSeqno = colStr(ii, "id_p_seqno");
			sqlSelect(sql2, new Object[] { ldCardAcctIdx, lsSysDate, lsIdPSeqno });
			int ilSelectCnt2 = sqlRowNum;
			for (int aa = 0; aa < ilSelectCnt2; aa++) {
				if (!empty(colStr(aa, "send_date")))
					continue;
				deleteNotice(colNum(aa, "card_acct_idx"), colStr(aa, "crt_date"), colStr(aa, "id_no"),
						colStr(aa, "id_code"));
				if (rc != 1)
					return rc;
			}

			insertNotice(lsIdPSeqno, ldCardAcctIdx);
			if (rc != 1)
				return rc;

		}

		return rc;
	}

	void insertNotice(String lsIdPSeqno, double ldCardAcctIdx) {
		msgOK();
		String sql1 = " select A.id_no , A.id_no_code , A.chi_name , uf_idno_name(B.major_id_p_seqno) as major_chi_name "
				+ " from crd_idno A join crd_card B on A.id_p_seqno = B.id_p_seqno where B.id_p_seqno = ? and sup_flag ='1' ";

		sqlSelect(sql1, new Object[] { lsIdPSeqno });

		strSql = " insert into cca_adj_notice ( " + " card_acct_idx ," + " crt_date ," + " id_p_seqno ,"
				+ " debit_flag ," + " sup_flag ," + " chi_name ," + " major_chi_name ," + " org_tot_consume ,"
				+ " lmt_tot_consume ," + " adj_eff_start_date ," + " adj_eff_end_date ," + " adj_reason ,"
				+ " adj_remark ," + " adj_area ," + " send_date ," + " del_adj_date ," + " id_no ," + " id_code ,"
				+ " crt_time ," + " crt_user ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno "
				+ " ) values ( " + " :card_acct_idx ," + " to_char(sysdate,'yyyymmdd') ," + " :id_p_seqno ," + " 'N' ,"
				+ " '0' ," + " :chi_name ," + " :major_chi_name ," + " :org_tot_consume ," + " :lmt_tot_consume ,"
				+ " :adj_eff_start_date ," + " :adj_eff_end_date ," + " :adj_reason ," + " :adj_remark ,"
				+ " :adj_area ," + " '' ," + " '' ," + " :id_no ," + " :id_code ," + " to_char(sysdate,'hh24miss') ,"
				+ " :crt_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

		setDouble("card_acct_idx", ldCardAcctIdx);
		setString("id_p_seqno", lsIdPSeqno);
		setString("chi_name", colStr("chi_name"));
		setString("major_chi_name", colStr("major_chi_name"));
		var2ParmNum("org_tot_consume");
		var2ParmNum("lmt_tot_consume");
		var2ParmStr("adj_eff_start_date");
		var2ParmStr("adj_eff_end_date");
		var2ParmStr("adj_reason");
		var2ParmStr("adj_remark");
		var2ParmStr("adj_area");
		setString("id_no", colStr("id_no"));
		setString("id_code", colStr("id_no_code"));
		setString("crt_user", wp.loginUser);
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", wp.modPgm());

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("insert cca_adj_notice error ");
		}
	}

	void deleteNotice(double ldCardAcctIdx, String lsCrtDate, String lsIdno, String lsIdCode) {
		msgOK();
		strSql = "delete cca_adj_notice where card_acct_idx =:card_acct_idx and crt_date =:crt_date and id_no =:id_no and id_code =:id_code";
		setDouble("card_acct_idx", ldCardAcctIdx);
		setString("crt_date", lsCrtDate);
		setString("id_no", lsIdno);
		setString("id_code", lsIdCode);

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("delete cca_adj_notice error !");
		}

		return;
	}

	void updateCcaCardAcct() {
		strSql = "update cca_card_acct set " + " adj_eff_start_date =:adj_eff_start_date ,"
				+ " adj_eff_end_date =:adj_eff_end_date ," + " adj_reason =:adj_reason ," + " adj_remark =:adj_remark ,"
				+ " tot_amt_month =:tot_amt_month ," + " adj_inst_pct =:adj_inst_pct ," + " adj_area =:adj_area ,"
				+ " adj_quota ='Y' ," + " adj_date =to_char(sysdate,'yyyymmdd') ,"
				+ " adj_time =to_char(sysdate,'hh24miss') ," + " adj_user =:adj_user ,"
				+ " adj_risk_flag =:adj_risk_flag ," + " notice_flag =:notice_flag , "
				+ " adj_risk_type =:adj_risk_type , adj_sms_flag =:adj_sms_flag ,"
				+ commSqlStr.setModxxx(modUser, modPgm) + " where card_acct_idx =:cardAcctIdx ";

		item2ParmStr("adj_eff_start_date");
		item2ParmStr("adj_eff_end_date");
		item2ParmStr("adj_reason");
		item2ParmStr("adj_remark");
		item2ParmNum("tot_amt_month");
		item2ParmNum("adj_inst_pct");
		item2ParmStr("adj_area");
		setString("adj_user", wp.loginUser);
		item2ParmNvl("adj_risk_flag", "N");
		item2ParmNvl("notice_flag", "N");
		setString("adj_risk_type", adjRiskType);
		item2ParmNvl("adj_sms_flag", "N");
		// setString("mod_user", wp.loginUser);
		// setString("mod_pgm", "ccam2120");
		// kk--
		setDouble2("cardAcctIdx", commString.strToNum(cardAcctIdx));

		sqlExec(strSql);
		if (sqlRowNum != 1) {
			errmsg("update cca_card_acct error, " + this.sqlErrtext);
		}
	}

	void deleteCcaAdjParm() {
		strSql = "delete cca_adj_parm where card_acct_idx =?";
		setDouble2(1, commString.strToNum(cardAcctIdx));
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			errmsg("delete cca_adj_parm error, kk[%s]", cardAcctIdx);
		}
	}

	@Override
	public int dbDelete() {
		dataCheck();
		if (rc != 1)
			return rc;

		strSql = " update cca_card_acct set " + " adj_eff_start_date = '' , " + " adj_eff_end_date = '' , "
				+ " adj_area = '' , " + " adj_reason = '' , " + " adj_remark = '' , " + " tot_amt_month = 0 , "
				+ " adj_inst_pct = 0 , " + " adj_quota = 'N' ," + " notice_flag = 'N' , " + " adj_risk_flag ='N' , "
				+ " adj_sms_flag = 'N' ," + commSqlStr.setModxxx(modUser, modPgm)
				+ " where card_acct_idx =:card_acct_idx ";

		setString2("mod_user", modUser);
		setString2("mod_pgm", modPgm);
		setDouble2("card_acct_idx", commString.strToNum(cardAcctIdx));

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update cca_card_acct error ");
			return rc;
		}

		sql2Insert("cca_limit_adj_log");
		addsqlParm("?", "log_date", sysDate);
		addsqlTime(", log_time");
		addsqlParm(",?", ", aud_code", "D");
		addsqlParm(",?", ", card_acct_idx", commString.strToNum(cardAcctIdx));
		addsqlParm(",?", ", mod_type", "1");
		addsqlParm(",?", ", rela_flag", wp.itemStr2("rela_flag"));
		addsqlParm(",?", ", lmt_tot_consume", wp.itemNum("line_credit_amt"));
		addsqlParm(",?", ", tot_amt_month_b", wp.itemNum("org_amt_mon"));
		addsqlParm(",?", ", tot_amt_month", wp.itemNum("line_credit_amt"));
		addsqlParm(",?", ", adj_inst_pct_b", wp.itemNum("ori_inst_pct"));
		addsqlParm(",?", ", adj_inst_pct", 100);
		addsqlParm(",?", ", adj_remark", "[Delete]");
		addsqlParm(",?", ", ecs_adj_rate", 0);
		addsqlParm(",?", ", adj_user", modUser);
		addsqlYmd(", adj_date");
		addsqlTime(", adj_time");
		addsqlParm(",?", ", debit_flag", "N");
		addsqlParm(",?", ", apr_user", "ccam2120");

		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert cca_limit_adj_log[Delete] error ");
			return rc;
		}

		deleteCcaAdjParm();
		if (rc != 1)
			return rc;

		return rc;
	}

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int insertDetlParm() {
		strSql = "insert into cca_adj_parm (" + " card_acct_idx, " // 1
				+ " risk_type, " + " adj_month_amt," + " adj_month_cnt," + " adj_day_amt," + " adj_day_cnt,"
				+ " card_note, risk_level," + " adj_eff_start_date ," + " adj_eff_end_date , spec_flag ," + " crt_date,"
				+ " crt_time," + " crt_user," + " mod_user," + " mod_time," + " mod_pgm," + " mod_seqno" + " ) values ("
				+ " :card_acct_idx," + " :risk_type," + " :adj_month_amt," + " :adj_month_cnt," + " :adj_day_amt,"
				+ " :adj_day_cnt," + " :card_note, :risk_level," + " :adj_eff_start_date ,"
				+ " :adj_eff_end_date , :spec_flag , " + " to_char(sysdate,'yyyymmdd'),"
				+ " to_char(sysdate,'hh24miss')," + " :crt_user, " + " :mod_user, " + " sysdate," + " :mod_pgm,"
				+ " '1'" + " )";

		// -set ?value-
		setDouble2("card_acct_idx", commString.strToNum(cardAcctIdx));
		var2ParmStr("risk_type");
		var2ParmNum("adj_month_amt");
		var2ParmNum("adj_month_cnt");
		var2ParmNum("adj_day_amt");
		var2ParmNum("adj_day_cnt");
		var2ParmStr("adj_eff_start_date");
		var2ParmStr("adj_eff_end_date");
		var2ParmStr("spec_flag");
		setString2("card_note", wp.itemStr2("card_note"));
		setString2("risk_level", wp.itemStr2("risk_level"));
		setString("crt_user", modUser);
		setString("mod_user", modUser);
		setString("mod_pgm", modPgm);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("insert cca_adj_parm error; " + sqlErrtext);
		}
		return rc;
	}

	int insertLimitAdjLog() {
		strSql = "insert into cca_limit_adj_log (" + " log_date, log_time ," + " aud_code,"
				+ " card_acct_idx, debit_flag," + " mod_type," + " rela_flag," + " lmt_tot_consume,"
				+ " tot_amt_month_b," + " tot_amt_month," + " adj_inst_pct_b," + " adj_inst_pct,"
				+ " adj_eff_date1, adj_eff_date2, adj_reason," + " adj_remark, adj_area ," + " ecs_return_code,"
				+ " ecs_adj_rate," + " adj_user, adj_date, adj_time, apr_user," + " adj_meno " + " ) values ("
				+ " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " :aud_code ,"
				+ " :card_acct_idx,'N'," + " '1'," + " :rela_flag ,"
				+ " :lmt_tot_consume, :tot_amt_month_b, :tot_amt_month,"
				+ " :adj_inst_pct_b, :adj_inst_pct, :adj_eff_date1, :adj_eff_date2,"
				+ " :adj_reason, :adj_remark, :adj_area ," + " '', '0',"
				+ " :adj_user, to_char(sysdate,'yyyymmdd'), to_char(sysdate,'hh24miss'), :apr_user ," + " '' " + " )";
		// -set ?value-
		setString2("aud_code", "U");
		if (wp.itemEmpty("org_start_date") && wp.itemEmpty("org_end_date")) {
			setString2("aud_code", "A");
		}
		setString2("card_acct_idx", cardAcctIdx);
		setString2("rela_flag", wp.itemStr2("rela_flag"));
		setDouble2("lmt_tot_consume", wp.itemNum("line_credit_amt"));
		setDouble2("tot_amt_month_b", wp.itemNum("org_amt_mon"));
		setDouble2("tot_amt_month", wp.itemNum("tot_amt_month"));
		setDouble2("adj_inst_pct_b", wp.itemNum("org_amt_mon"));
		setDouble2("adj_inst_pct", wp.itemNum("adj_inst_pct"));
		setString2("adj_eff_date1", wp.itemStr2("adj_eff_start_date"));
		setString2("adj_eff_date2", wp.itemStr2("adj_eff_end_date"));
		setString2("adj_reason", wp.itemStr2("adj_reason"));
		setString2("adj_remark", wp.itemStr2("adj_remark"));
		setString2("adj_area", wp.itemStr2("adj_area"));
		setString2("adj_user", modUser);
		setString2("apr_user", "ccam2120");
		setString2("mod_user", modUser);
		setString2("mod_pgm", modPgm);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("cca_limit_adj_log.ADD, err=" + sqlErrtext);
		}
		return rc;
	}

}
