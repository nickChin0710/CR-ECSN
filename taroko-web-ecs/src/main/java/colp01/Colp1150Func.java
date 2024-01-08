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

public class Colp1150Func extends FuncProc {
	String liadDocNo;

	public Colp1150Func(TarokoCommon wr) {
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
		if (isOtherModify("col_liad_z60", sqlWhere, param)) {
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
//		if (rc!=1) return rc;
//		is_sql = "update col_liac_plan set "
//				+ " apr_flag = 'Y', "
//				+ " apr_date = to_char(sysdate,'yyyymmdd'), "
//				+ " apr_user = ?, "
//				+ " mod_user = ?, "
//	            + " mod_time=sysdate, mod_pgm =?, "
//	            + " mod_seqno =nvl(mod_seqno,0)+1 "
//				+ sql_where;
//		Object[] param = new Object[] { 
//                 wp.loginUser
//               , wp.loginUser 
//               , wp.item_ss("mod_pgm")
//               , kk1 
//               , var_modseqno()
//        };
//        rc = sqlExec(is_sql, param);
//        if (sql_nrow <= 0) {
//			errmsg(this.sql_errtext);
//		}
//		return rc;

		if (rc != 1)
			return rc;

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_liad_z60");
		sp.ppstr("credit_branch", varsStr("credit_branch"));
		sp.ppstr("law_user_id", varsStr("law_user_id"));
		sp.ppstr("demand_user_id", varsStr("demand_user_id"));
		sp.ppstr("branch_comb_flag", varsStr("branch_comb_flag"));
		sp.ppstr("court_id", varsStr("court_id"));
		sp.ppstr("court_name", varsStr("court_name"));
		sp.ppstr("case_year", varsStr("case_year"));
		sp.ppstr("case_letter", varsStr("case_letter"));
		sp.ppstr("case_no", varsStr("case_no"));
		sp.ppstr("bullet_date", varsStr("bullet_date"));
		sp.ppstr("bullet_desc", varsStr("bullet_desc"));
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

		strSql = "delete col_liad_z60 " + sqlWhere;
		Object[] param = new Object[] { liadDocNo, varModseqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = 0;
		}
		return rc;
	}

	public int deleteColLiadModTmp() {
		strSql = "delete col_liad_mod_tmp where data_type ='Z60' and data_key = ? ";
		Object[] param = new Object[] { varsStr("liad_doc_no") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum == 0) {
			rc = 0;
		}
		// System.out.println("delete_col_liad_z60_rc:"+rc);
		return rc;
	}

}
