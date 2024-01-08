/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Zhanghuheng     updated for project coding standard   *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package colp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colp1160Func extends FuncProc {
	String liadDocNo;

	public Colp1160Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
		liadDocNo = varsStr("liad_doc_no");
		log(this.actionCode + ", kk1=" + liadDocNo + ", mod_seqno=" + varModseqno());

		// -other modify-
		sqlWhere = "where liad_doc_no = ? " + "and mod_seqno = ? ";
		Object[] param = new Object[] { liadDocNo, varModseqno() };
		if (isOtherModify("col_liad_renew", sqlWhere, param)) {
			return;
		}

		return;
	}

	@Override
	public int dataProc() {
		dataCheck();

		if (varsStr("aud_code").equals("D"))
			rc = deleteFunc();
		else
			rc = updateFunc();

		return rc;
	}

	int updateFunc() {
		if (rc != 1)
			return rc;

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_liad_renew");
		sp.ppstr("recv_date", varsStr("recv_date"));
		sp.ppstr("branch_comb_flag", varsStr("branch_comb_flag"));
		sp.ppstr("max_bank_flag", varsStr("max_bank_flag"));
		sp.ppstr("court_id", varsStr("court_id"));
		sp.ppstr("renew_status", varsStr("renew_status"));
		sp.ppstr("court_status", varsStr("court_status"));
		sp.ppnum("case_year", varsNum("case_year"));
		sp.ppstr("case_letter_desc", varsStr("case_letter_desc"));
		sp.ppstr("judic_date", varsStr("judic_date"));
		sp.ppstr("confirm_date", varsStr("confirm_date"));
		sp.ppnum("payoff_amt", varsNum("payoff_amt"));
		sp.ppstr("run_renew_flag", varsStr("run_renew_flag"));
		sp.ppstr("doc_chi_name", varsStr("doc_chi_name"));
		sp.ppstr("credit_branch", varsStr("credit_branch"));
		sp.ppnum("org_debt_amt", varsNum("org_debt_amt"));
		sp.ppnum("org_debt_amt_bef", varsNum("org_debt_amt_bef"));
		sp.ppnum("org_debt_amt_bef_base", varsNum("org_debt_amt_bef_base"));
		sp.ppstr("case_letter", varsStr("case_letter"));
		sp.ppstr("renew_cancel_date", varsStr("renew_cancel_date"));
		sp.ppstr("renew_first_date", varsStr("renew_first_date"));
		sp.ppnum("renew_int", varsNum("renew_int"));
		sp.ppstr("deliver_date", varsStr("deliver_date"));
		sp.ppstr("renew_damage_date", varsStr("renew_damage_date"));
		sp.ppstr("court_dept", varsStr("court_dept"));
		sp.ppstr("judic_action_flag", varsStr("judic_action_flag"));
		sp.ppstr("action_date_s", varsStr("action_date_s"));
		sp.ppstr("judic_cancel_flag", varsStr("judic_cancel_flag"));
		sp.ppstr("cancel_date", varsStr("cancel_date"));
		sp.ppstr("renew_last_date", varsStr("renew_last_date"));
		sp.ppstr("payment_day", varsStr("payment_day"));
		sp.ppnum("renew_rate", varsNum("renew_rate"));
		sp.ppnum("renew_lose_amt", varsNum("renew_lose_amt"));
		sp.ppstr("super_name", varsStr("super_name"));
		sp.ppstr("apr_flag", "Y");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where liad_doc_no=?", liadDocNo);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", varModseqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	int deleteFunc() {
		if (rc != 1)
			return rc;

		strSql = "delete col_liad_renew " + sqlWhere;
		Object[] param = new Object[] { liadDocNo, varModseqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = 0;
		}
		return rc;
	}

	public int deleteColLiadModTmp() {
		strSql = "delete col_liad_mod_tmp where data_type ='RENEW' and data_key = ? ";
		Object[] param = new Object[] { varsStr("liad_doc_no") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum == 0) {
			rc = 0;
		}
		// System.out.println("delete_col_liad_z60_rc:"+rc);
		return rc;
	}

	public int insertColLiadRemod() {
		msgOK();

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_liad_remod");
		sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
		sp.ppstr("id_no", varsStr("id_no"));
		sp.ppstr("liad_doc_no", varsStr("liad_doc_no"));
		sp.ppstr("liad_type", "3");
		sp.ppstr("liad_status", varsStr("renew_status"));
		sp.ppstr("proc_flag", "N");
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}

		return rc;
	}

	int insertColLiadLog() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_liad_log");
		sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
		sp.ppstr("doc_no", wp.itemStr("liad_doc_no"));
		sp.ppstr("liad_type", "1");
		sp.ppstr("event_type", "S");
		sp.ppstr("holder_id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("holder_id", wp.itemStr("id_no"));
		sp.ppstr("renew_liqu_status", wp.itemStr("renew_status"));
		sp.ppstr("proc_flag", "N");
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		return rc;
	}

	int deleteColLiadLog() {
		strSql = "delete col_liad_log where doc_no = ? " + " and liad_type = '1' " + " and event_type = 'S' "
				+ " and proc_flag <> 'Y' ";
		Object[] param = new Object[] { wp.itemStr("liad_doc_no") };
		rc = sqlExec(strSql, param);
		return rc;
	}
}
