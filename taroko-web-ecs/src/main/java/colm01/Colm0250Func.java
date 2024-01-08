/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/04/20  V1.00.01    phopho    update                                    *
*  112/03/31  V1.00.02    Zuwei Su  naming rule update                        *
*  112/04/16  V1.00.03    Simon     callbatch changed into online proc        *
*  112/04/17  V1.00.04    Simon     dbInsert() bug fixed                      *
*  112/09/16  V1.00.05    Simon     新增維護臨調額度                          *
******************************************************************************/

package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm0250Func extends FuncEdit {
	String kk1;
	String kk2;

  public Colm0250Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
  // TODO Auto-generated method 
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
		kk1 = wp.itemStr("p_seqno");
		kk2 = wp.itemStr("acct_month");

    if (this.isAdd()) {
		  //檢查新增資料是否重複
		  String lsSql = "select count(*) as tot_cnt from act_jcic_cmp where 1=1 "
		               + "and p_seqno = ? and acct_month = ? ";
			Object[] param = new Object[] { kk1,kk2 };
			sqlSelect(lsSql, param);
		  if (colNum("tot_cnt") > 0) {
			  errmsg("資料已存在，無法新增");
		  }
		}	else {
			//-other modify-
			sqlWhere = "where p_seqno = ? and acct_month = ? "
					  + "and nvl(mod_seqno,0) = ?";
			Object[] param = new Object[] { kk1, kk2, wp.modSeqno() };
			isOtherModify("act_jcic_cmp", sqlWhere, param);
		}
		return;
  }

  @Override
  public int dbInsert() {
  	actionInit("A");
		dataCheck();
		if(rc!=1) return rc;

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_jcic_cmp");
		sp.ppstr("p_seqno", varsStr("p_seqno"));
		sp.ppstr("acct_month", varsStr("acct_month"));

		sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
		sp.ppstr("acct_type", varsStr("acct_type"));
		sp.ppstr("corp_id_p_seqno", varsStr("corp_id_p_seqno"));
		sp.ppstr("stmt_cycle", varsStr("stmt_cycle"));
		sp.ppstr("corp_no", varsStr("corp_no"));
		sp.ppstr("stmt_cycle_date", varsStr("stmt_cycle_date"));
		sp.ppnum("line_of_credit_amt", varsNum("line_of_credit_amt"));		
		sp.ppnum("temp_of_credit_amt", varsNum("temp_of_credit_amt"));		
		sp.ppnum("cca_temp_credit_amt", varsNum("cca_temp_credit_amt"));		
		sp.ppstr("cca_adj_eff_start_date", varsStr("cca_adj_eff_start_date"));
		sp.ppstr("cca_adj_eff_end_date", varsStr("cca_adj_eff_end_date"));
		sp.ppstr("stmt_last_payday", varsStr("stmt_last_payday"));
		sp.ppstr("bin_type", varsStr("bin_type"));
		sp.ppnum("cashadv_limit", varsNum("cashadv_limit"));		
		sp.ppnum("stmt_this_ttl_amt", varsNum("stmt_this_ttl_amt"));		
		sp.ppnum("stmt_mp", varsNum("stmt_mp"));		
		sp.ppnum("billed_end_bal_bl", varsNum("billed_end_bal_bl"));		
		sp.ppnum("billed_end_bal_it", varsNum("billed_end_bal_it"));		
		sp.ppnum("billed_end_bal_id", varsNum("billed_end_bal_id"));		
		sp.ppnum("billed_end_bal_ot", varsNum("billed_end_bal_ot"));		
		sp.ppnum("billed_end_bal_ca", varsNum("billed_end_bal_ca"));		
		sp.ppnum("billed_end_bal_ao", varsNum("billed_end_bal_ao"));		
		sp.ppnum("billed_end_bal_af", varsNum("billed_end_bal_af"));		
		sp.ppnum("billed_end_bal_lf", varsNum("billed_end_bal_lf"));		
		sp.ppnum("billed_end_bal_pf", varsNum("billed_end_bal_pf"));		
		sp.ppnum("billed_end_bal_ri", varsNum("billed_end_bal_ri"));		
		sp.ppnum("billed_end_bal_pn", varsNum("billed_end_bal_pn"));		
		sp.ppnum("ttl_amt_bal", varsNum("ttl_amt_bal"));		
		sp.ppnum("bill_interest", varsNum("bill_interest"));		
		sp.ppnum("stmt_adjust_amt", varsNum("stmt_adjust_amt"));		
		sp.ppnum("unpost_inst_fee", varsNum("unpost_inst_fee"));		
		sp.ppnum("unpost_card_fee", varsNum("unpost_card_fee"));		
		sp.ppnum("stmt_last_ttl", varsNum("stmt_last_ttl"));		
		sp.ppstr("payment_amt_rate", varsStr("payment_amt_rate"));
		sp.ppstr("payment_time_rate", varsStr("payment_time_rate"));
		sp.ppnum("stmt_payment_amt", varsNum("stmt_payment_amt"));		
		sp.ppstr("jcic_acct_status", varsStr("jcic_acct_status"));
		sp.ppstr("jcic_acct_status_flag", varsStr("jcic_acct_status_flag"));
		sp.ppstr("bill_type_flag", varsStr("bill_type_flag"));
		sp.ppstr("proc_date", varsStr("proc_date"));
		sp.ppstr("proc_desc", varsStr("proc_desc"));
		sp.ppstr("status_change_date", varsStr("status_change_date"));
		sp.ppstr("last_payment_date", varsStr("last_payment_date"));
		sp.ppstr("last_min_pay_date", varsStr("last_min_pay_date"));
		sp.ppstr("debt_close_date", varsStr("debt_close_date"));
		sp.ppstr("sale_date", varsStr("sale_date"));
		sp.ppstr("jcic_remark", varsStr("jcic_remark"));
		sp.ppstr("npl_corp_no", varsStr("npl_corp_no"));
		sp.ppstr("crt_user", wp.loginUser);
		sp.addsql(", crt_date ",", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppstr("mod_seqno", "1");
		sp.ppnum("ecs_ttl_amt_bal", varsNum("ecs_ttl_amt_bal"));		
		sp.ppnum("acct_jrnl_bal", varsNum("acct_jrnl_bal"));		
		sp.ppstr("report_reason", varsStr("report_reason"));
		sp.ppstr("acct_status", varsStr("acct_status"));
		sp.ppint("valid_cnt", varsInt("valid_cnt"));		
		sp.ppstr("stop_flag", varsStr("stop_flag"));
		sp.ppnum("unpost_inst_stage_fee", varsNum("unpost_inst_stage_fee"));		
		sp.ppnum("oversea_cashadv_limit", varsNum("oversea_cashadv_limit"));		
		sp.ppnum("year_revolve_int_rate", varsNum("year_revolve_int_rate"));		
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
  }

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc != 1) return rc;
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_jcic_cmp");
		sp.ppstr("sub_log_type", wp.itemStr("sub_log_type"));
		sp.ppstr("stmt_cycle_date", wp.itemStr("stmt_cycle_date"));
		sp.ppstr("stmt_last_payday", wp.itemStr("stmt_last_payday"));
		sp.ppstr("corp_no", wp.itemStr("corp_no"));
		sp.ppstr("bin_type", wp.itemStr("bin_type"));
		sp.ppstr("bill_type_flag", wp.itemStr("bill_type_flag"));
		sp.ppstr("report_reason", wp.itemStr("report_reason"));
		sp.ppnum("line_of_credit_amt", wp.itemNum("line_of_credit_amt"));
		sp.ppnum("temp_of_credit_amt", wp.itemNum("temp_of_credit_amt"));
	//sp.ppnum("cash_lmt_balance", wp.itemNum("cash_lmt_balance"));
		sp.ppnum("cashadv_limit", wp.itemNum("cashadv_limit"));
		sp.ppnum("stmt_this_ttl_amt", wp.itemNum("stmt_this_ttl_amt"));
		sp.ppnum("stmt_mp", wp.itemNum("stmt_mp"));
		sp.ppnum("billed_end_bal_bl", wp.itemNum("billed_end_bal_bl"));
		sp.ppnum("billed_end_bal_it", wp.itemNum("billed_end_bal_it"));
	//sp.ppnum("billed_end_bal_ri", wp.itemNum("billed_end_bal_ri"));
		sp.ppnum("billed_end_bal_id", wp.itemNum("billed_end_bal_id"));
		sp.ppnum("billed_end_bal_ot", wp.itemNum("billed_end_bal_ot"));
		sp.ppnum("billed_end_bal_ca", wp.itemNum("billed_end_bal_ca"));
		sp.ppnum("billed_end_bal_ao", wp.itemNum("billed_end_bal_ao"));
		sp.ppnum("bill_interest", wp.itemNum("bill_interest"));
		sp.ppnum("billed_end_bal_af", wp.itemNum("billed_end_bal_af"));
		sp.ppnum("billed_end_bal_lf", wp.itemNum("billed_end_bal_lf"));
		sp.ppnum("billed_end_bal_pf", wp.itemNum("billed_end_bal_pf"));
		sp.ppnum("billed_end_bal_pn", wp.itemNum("billed_end_bal_pn"));
		sp.ppnum("stmt_adjust_amt", wp.itemNum("stmt_adjust_amt"));
		sp.ppnum("unpost_inst_fee", wp.itemNum("unpost_inst_fee"));
		sp.ppnum("unpost_card_fee", wp.itemNum("unpost_card_fee"));
		sp.ppnum("stmt_last_ttl", wp.itemNum("stmt_last_ttl"));
		sp.ppnum("stmt_payment_amt", wp.itemNum("stmt_payment_amt"));
		sp.ppnum("ttl_amt_bal", wp.itemNum("ttl_amt_bal"));
		sp.ppstr("payment_amt_rate", wp.itemStr("payment_amt_rate"));
		sp.ppstr("payment_time_rate", wp.itemStr("payment_time_rate"));
		sp.ppstr("jcic_acct_status", wp.itemStr("jcic_acct_status"));
		sp.ppstr("status_change_date", wp.itemStr("status_change_date"));
		sp.ppstr("jcic_acct_status_flag", wp.itemStr("jcic_acct_status_flag"));
		sp.ppstr("debt_close_date", wp.itemStr("debt_close_date"));
		sp.ppstr("sale_date", wp.itemStr("sale_date"));
		sp.ppstr("npl_corp_no", wp.itemStr("npl_corp_no"));
		sp.ppstr("jcic_remark", wp.itemStr("jcic_remark"));
		sp.ppstr("apr_flag", varsStr("needSign"));
		if (varsStr("needSign").equals("Y")) {
			sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
			sp.ppstr("apr_user", wp.itemStr("zz_apr_user"));
		} else {
			sp.ppstr("apr_date", "");
			sp.ppstr("apr_user", "");
		}
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where p_seqno = ?", kk1);
		sp.sql2Where("   and acct_month = ?", kk2);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
			errmsg(this.sqlErrtext);
		}
		return rc;
  }

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc != 1) return rc;

		strSql = "delete act_jcic_cmp " + sqlWhere;
		Object[] param = new Object[] { kk1, kk2, wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = -1;
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
