package ccam01;
/**臨時調整額度覆核（依卡戶）
 * 19-0611:    JH    p_xxx >> acno_p_xxx
 * V.2018-0928
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-10-16 V1.00.01  tanwei           updated for project coding standard
 * */

import busi.FuncAction;

public class Ccap2052Func extends FuncAction {
	double cardAcctIdx = 0;
	public int llProc = -1;

	busi.DataSet dsParm = new busi.DataSet();

	@Override
	public void dataCheck() {
		selectCardAcctTmp();
		if (rc != 1)
			return;

		if (colEmpty("adj_area")) {
			errmsg("適用地區別不可空白");
			return;
		}
		if (colEmpty("adj_reason")) {
			errmsg("調高原因不可空白");
			return;
		}
		if (colNum("tot_amt_month") <= 0) {
			errmsg("調整一般月限額:須>0");
			return;
		}
		if (colNum("adj_inst_pct") <= 0) {
			errmsg("分期總月限金額:須>0");
			return;
		}

		if (colEmpty("adj_eff_start_date") || colEmpty("adj_eff_end_date")) {
			errmsg("有效日期輸入錯誤");
			return;
		}
		if (commString.strComp(colStr("adj_eff_start_date"), colStr("adj_eff_end_date")) > 0) {
			errmsg("有效日期輸入錯誤");
			return;
		}
		// --
		String lsDate = this.getSysDate();
		if (commString.strComp(colStr("adj_eff_start_date"), lsDate) < 0
				&& commString.strComp(colStr("adj_eff_end_date"), lsDate) < 0) {
			errmsg("有效期間輸入錯誤");
			return;
		}

//		if (checkAmount() == false)
//			return;

	}

	boolean checkAmount() {
		double ldAmt = 0.0, ldAprAmt = 0.0;
		String sql1 = " select sum(line_of_credit_amt) as ld_amt from act_acno "
				+ " where acno_p_seqno in (select acno_p_seqno from crd_card where current_code ='0' and acno_p_seqno = ?) "
				+ " and acct_type <> ? ";

		sqlSelect(sql1, new Object[] { wp.itemStr2("acno_p_seqno"), wp.itemStr2("acct_type") });

		ldAmt = colNum("ld_amt");

		String sql2 = " select al_amt, al_amt02 from sec_amtlimit where al_level in (select usr_amtlevel from sec_user where usr_id =?)"
				+ " and al_level<>'' ";

		sqlSelect(sql2, new Object[] { wp.loginUser });

		if (sqlRowNum <= 0) {
			errmsg("查無[%s]額度層級", wp.loginUser);
			return false;
		}

		ldAprAmt = colNum("al_amt");

		ldAmt += colNum("tot_amt_month");

		if (ldAmt > ldAprAmt) {
			errmsg("覆核人員[%s], 額度層級不足", wp.loginUser);
			return false;
		}

		return true;
	}

	void selectCardAcctTmp() {
		strSql = "select hex(rowid) as rowid, mod_seqno," + " adj_eff_start_date , " + " adj_eff_end_date , "
				+ " adj_area , " + " tot_amt_month , " + " adj_inst_pct , " + " adj_remark , " + " adj_reason , "
				+ " adj_sms_flag , " + " adj_user , " + " adj_date , " + " adj_quota , adj_risk_type ,adj_risk_flag , mod_user " + " from cca_card_acct_T"
				+ " where card_acct_idx =?" + " and mod_type ='ADJ-LIMIT'";
		setDouble2(1, cardAcctIdx);
		sqlSelect(strSql);
		if (sqlRowNum != 1) {
			errmsg("臨調資料: 已不存在");
			return;
		}

		// --
		double llModSeq = wp.itemNum("mod_seqno");
		if (llModSeq != colNum("mod_seqno")) {
			errmsg("資料已異動, 請重新讀取");
		}
		return;
	}

	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		cardAcctIdx = wp.itemNum("card_acct_idx");
		deleteTempData();
		return rc;
	}

	@Override
	public int dataProc() {
		cardAcctIdx = wp.itemNum("card_acct_idx");

		ccam01.Ccam2050Func func2050 = new ccam01.Ccam2050Func();
		func2050.setConn(wp);

		dataCheck();
		if (rc != 1)
			return rc;
		updateCardAcct();
		if (rc != 1)
			return rc;
		updateAdjParm();
		if (rc != 1)
			return rc;
		if (wp.itemEq("notice_flag", "Y")) {
			func2050.varsSet("org_tot_consume", wp.itemStr("line_credit_amt"));
			func2050.varsSet("lmt_tot_consume", wp.itemStr("tot_amt_month"));
			func2050.varsSet("adj_eff_start_date", wp.itemStr("adj_eff_start_date"));
			func2050.varsSet("adj_eff_end_date", wp.itemStr("adj_eff_end_date"));
			func2050.varsSet("adj_reason", wp.itemStr("adj_reason"));
			func2050.varsSet("adj_remark", wp.itemStr("adj_remark"));
			func2050.varsSet("adj_area", wp.itemStr("adj_area"));
			func2050.insertAdjNotice();
			if (rc != 1)
				return rc;
		}
		deleteTempData();
		if (rc != 1)
			return rc;
		insertLimitAdjLog();
		if (rc != 1)
			return rc;
		if (wp.itemEq("adj_sms_flag", "Y")) {
			busi.func.SmsMsgDetl func = new busi.func.SmsMsgDetl();
			func.setConn(wp);
			String lsMsgDesc = wp.itemStr2("chi_name");
			func.ccaM2050Adj(wp.itemNum("card_acct_idx"), lsMsgDesc);
		}
		return rc;
	}

	public double getIdnoLimit(String acctType) throws Exception {
		double lmAmt = 0;
		String lsIdPseqno = wp.itemStr("id_p_seqno");
		if (empty(acctType) || empty(lsIdPseqno))
			return 0;

		strSql = "select A.line_of_credit_amt" + ", B.ADJ_EFF_START_DATE, B.ADJ_EFF_END_DATE, B.tot_amt_month"
				+ " from act_acno A join cca_card_acct B" + " on A.acno_p_seqno=B.acno_p_seqno and B.debit_flag<>'Y'"
				+ " where A.acno_p_seqno in ("
				+ " select acno_p_seqno from crd_card where id_p_seqno =? and current_code='0' and acno_flag in ('1','3')"
				+ " ) and A.acct_type <>?";

		setString(1, lsIdPseqno);
		setString(2,acctType);
		sqlSelect(strSql);
		if (sqlRowNum <= 0)
			return 0;

		for (int ii = 0; ii < sqlRowNum; ii++) {
			String lsDate1 = colStr("adj_eff_start_date");
			String lsDate2 = colStr("adj_eff_end_date");
			if ((!empty(lsDate1) && commString.strComp(lsDate1, wp.sysDate) >= 0)
					&& (!empty(lsDate2) && commString.strComp(lsDate2, wp.sysDate) >= 0)) {
				lmAmt += colNum("tot_amt_month");
			} else {
				lmAmt += colNum("line_of_credit_amt");
			}
		}

		return lmAmt;
	}

	void updateCardAcct() {
		strSql = " update cca_card_acct set " + " adj_quota =:adj_quota , "
				+ " adj_eff_start_date =:adj_eff_start_date , " + " adj_eff_end_date =:adj_eff_end_date , "
				+ " adj_area =:adj_area , " + " tot_amt_month =:tot_amt_month , " + " adj_inst_pct =:adj_inst_pct , "
				+ " adj_remark =:adj_remark , " + " adj_user =:adj_user , " + " adj_date =:adj_date ,"
				+ " adj_reason =:adj_reason ," + " notice_flag =:notice_flag , " + " adj_risk_flag =:adj_risk_flag , "
				+ " adj_sms_flag =:adj_sms_flag , adj_risk_type =:adj_risk_type , "
				+ " mod_user =:mod_user , mod_time = sysdate , mod_pgm =:mod_pgm , mod_seqno = nvl(mod_seqno,0)+1 "
				+ " where card_acct_idx =:card_acct_idx ";
		col2ParmStr("adj_quota");
		col2ParmStr("adj_eff_start_date");
		col2ParmStr("adj_eff_end_date");
		col2ParmStr("adj_area");
		col2ParmNum("tot_amt_month");
		col2ParmNum("adj_inst_pct");
		col2ParmStr("adj_remark");
		col2ParmStr("adj_user");
		col2ParmStr("adj_date");
		col2ParmStr("adj_reason");
		col2ParmStr("notice_flag");
		col2ParmStr("adj_risk_flag");
		col2ParmStr("adj_risk_type");
		col2ParmNvl("adj_sms_flag", "N");
		col2ParmStr("mod_user");
		setString("mod_pgm",wp.modPgm());
		setDouble2("card_acct_idx", cardAcctIdx);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update cca_card_acct error !");
		}

		return;
	}

	void updateAdjParm() {
		strSql = " delete cca_adj_parm where card_acct_idx =? ";
		setDouble2(1, cardAcctIdx);
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			errmsg("delete cca_adj_parm error !");
			return;
		}

		selectTempParm();
		if (dsParm.listRows() <= 0)
			return;

		strSql = " insert into cca_adj_parm ( " + " card_acct_idx , " + " risk_type , " + " adj_month_amt , "
				+ " adj_month_cnt , " + " adj_day_amt , " + " adj_day_cnt , " + " card_note, risk_level,"
				+ " adj_eff_start_date ," + " adj_eff_end_date , spec_flag ," + " crt_date, crt_time, crt_user , "
				+ " mod_user, mod_time, mod_pgm, mod_seqno " + " ) values ( " + " :card_acct_idx , " + " :risk_type , "
				+ " :adj_month_amt , " + " :adj_month_cnt , " + " :adj_day_amt , " + " :adj_day_cnt , "
				+ " :card_note, :risk_level," + ":adj_eff_start_date , :adj_eff_end_date , :spec_flag ,"
				+ " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , " + " :crt_user"
				+ commSqlStr.modxxxValue(modUser, modPgm) + " )";

		for (int ii = 0; ii < dsParm.listRows(); ii++) {
			dsParm.listNext();
			setDouble2("card_acct_idx", cardAcctIdx);
			setString2("risk_type", dsParm.colStr("risk_type"));
			// setDouble("times_amt", ds_parm.col_num("times_amt"));
			// setDouble("times_cnt", ds_parm.col_num("times_cnt"));
			// setString("old_flag", ds_parm.col_ss("old_flag"));
			setDouble2("adj_month_amt", dsParm.colNum("adj_month_amt"));
			setInt2("adj_month_cnt", dsParm.colInt("adj_month_cnt"));
			setDouble2("adj_day_amt", dsParm.colNum("adj_day_amt"));
			setInt2("adj_day_cnt", dsParm.colInt("adj_day_cnt"));
			setString2("card_note", dsParm.colStr("card_note"));
			setString2("risk_level", dsParm.colStr("risk_level"));
			setString2("adj_eff_start_date", dsParm.colStr("adj_eff_start_date"));
			setString2("adj_eff_end_date", dsParm.colStr("adj_eff_end_date"));
			setString2("spec_flag", dsParm.colStr("spec_flag"));
			setString2("crt_user", modUser);

			sqlExec(strSql);
			if (sqlRowNum <= 0) {
				errmsg("insert cca_adj_parm error ");
				break;
			}
		}

	}

	void selectTempParm() {
		String sql1 = "select * from cca_adj_parm_t " + " where card_acct_idx = ? ";
		dsParm.colList = sqlQuery(sql1, new Object[] { cardAcctIdx });
	}

	void deleteTempData() {
		strSql = " delete cca_card_acct_t where card_acct_idx =? and mod_type='ADJ-LIMIT'";
		setDouble2(1, cardAcctIdx);
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			errmsg("delete cca_card_acct_T error !");
			return;
		}

		strSql = "delete cca_adj_parm_t where card_acct_idx =?";
		setDouble2(1, cardAcctIdx);
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			errmsg("delete cca_adj_parm_T error");
			return;
		}
		return;
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
		setDouble2("card_acct_idx", wp.itemNum("card_acct_idx"));
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
		setString2("adj_user", colStr("adj_user"));
		setString2("apr_user", modUser);
		setString2("mod_user", modUser);
		setString2("mod_pgm", modPgm);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("cca_limit_adj_log.ADD, err=" + sqlErrtext);
		}
		return rc;
	}

}
