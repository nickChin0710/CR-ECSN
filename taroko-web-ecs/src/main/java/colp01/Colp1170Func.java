/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Zhanghuheng     updated for project coding standard *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package colp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colp1170Func extends FuncProc {
	String liadDocNo;

	public Colp1170Func(TarokoCommon wr) {
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
		if (isOtherModify("col_liad_liquidate", sqlWhere, param)) {
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
		sp.sql2Update("col_liad_liquidate");
		sp.ppstr("recv_date", varsStr("recv_date"));
		sp.ppstr("liqu_status", varsStr("liqu_status"));
		sp.ppstr("branch_comb_flag", varsStr("branch_comb_flag"));
		sp.ppstr("credit_branch", varsStr("credit_branch"));
		sp.ppstr("max_bank_flag", varsStr("max_bank_flag"));
		sp.ppnum("org_debt_amt", varsNum("org_debt_amt"));
		sp.ppnum("org_debt_amt_bef", varsNum("org_debt_amt_bef"));
		sp.ppnum("org_debt_amt_bef_base", varsNum("org_debt_amt_bef_base"));
		sp.ppnum("liqu_lose_amt", varsNum("liqu_lose_amt"));
		sp.ppstr("court_id", varsStr("court_id"));
		sp.ppstr("court_name", varsStr("court_name"));
		sp.ppstr("doc_chi_name", varsStr("doc_chi_name"));
		sp.ppstr("court_dept", varsStr("court_dept"));
		sp.ppstr("court_status", varsStr("court_status"));
		sp.ppstr("case_year", varsStr("case_year"));
		sp.ppstr("case_letter", varsStr("case_letter"));
		sp.ppstr("case_letter_desc", varsStr("case_letter_desc"));
		sp.ppstr("judic_avoid_flag", varsStr("judic_avoid_flag"));
		sp.ppstr("judic_avoid_sure_flag", varsStr("judic_avoid_sure_flag"));
		sp.ppstr("judic_avoid_no", varsStr("judic_avoid_no"));
		sp.ppstr("judic_date", varsStr("judic_date"));
		sp.ppstr("judic_action_flag", varsStr("judic_action_flag"));
		sp.ppstr("action_date_s", varsStr("action_date_s"));
		sp.ppstr("judic_cancel_flag", varsStr("judic_cancel_flag"));
		sp.ppstr("cancel_date", varsStr("cancel_date"));
		sp.ppstr("law_133_flag", wp.itemStr("law_133_flag"));
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

		strSql = "delete col_liad_liquidate " + sqlWhere;
		Object[] param = new Object[] { liadDocNo, varModseqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = 0;
		}
		return rc;
	}

	public int deleteColLiadModTmp() {
		strSql = "delete col_liad_mod_tmp where data_type ='LIQUIDATE' and data_key = ? ";
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
		sp.ppstr("liad_status", varsStr("liqu_status"));
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
		sp.ppstr("liad_type", "2");
		sp.ppstr("event_type", "S");
		sp.ppstr("holder_id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("holder_id", wp.itemStr("id_no"));
		sp.ppstr("renew_liqu_status", varsStr("liqu_status"));
		sp.ppstr("proc_flag", "N");
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		return rc;
	}

	int deleteColLiadLog() {
		strSql = "delete col_liad_log where doc_no = ? " + " and liad_type = '2' " + " and event_type = 'S' "
				+ " and proc_flag <> 'Y' ";
		Object[] param = new Object[] { wp.itemStr("liad_doc_no") };
		rc = sqlExec(strSql, param);
		return rc;
	}

}
