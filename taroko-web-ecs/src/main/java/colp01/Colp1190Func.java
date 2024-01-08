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

public class Colp1190Func extends FuncProc {
	String idNo, caseLetter;
	String kkIdPSeqno;

	public Colp1190Func(TarokoCommon wr) {
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
		idNo = varsStr("id_no");
		caseLetter = varsStr("case_letter");
		kkIdPSeqno = varsStr("id_p_seqno");

		// -other modify-
//		sql_where = "where id_no = ? "
		sqlWhere = "where id_p_seqno = ? " + "and case_letter = ? " + "and mod_seqno = ? ";
//		Object[] param = new Object[] { kk1,kk2, var_modseqno() };
		Object[] param = new Object[] { kkIdPSeqno, caseLetter, varModseqno() };
		if (isOtherModify("col_liad_570", sqlWhere, param)) {
			return;
		}

		return;
	}

	@Override
	public int dataProc() {
		dataCheck();
		rc = updateFunc();
		return rc;
	}

	public int insertFunc() {

		if (rc != 1)
			return rc;

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_liad_57x_log");
		sp.ppstr("data_type", "571");
		sp.addsql(", aud_code", ", decode(" + varsNum("send_cnt") + ",0,'A','C')");
		sp.ppstr("send_bank_no", "017");
		sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
		sp.ppstr("id_no", varsStr("id_no"));
		sp.ppstr("case_letter", varsStr("case_letter"));
		sp.ppstr("apply_date", varsStr("coll_apply_date"));
		sp.ppstr("bank_no", varsStr("apply_bank_no"));
		sp.ppstr("is_renew_flag", "Y");
		sp.ppstr("pay_normal_flag", varsStr("pay_normal_flag_2"));
		sp.ppstr("tot_debt_amt", varsStr("debt_amt2"));
		sp.ppstr("alloc_debt_amt", varsStr("alloc_debt_amt"));
		sp.ppstr("unalloc_debt_amt", varsStr("unalloc_debt_amt"));
		sp.ppstr("idno_name", varsStr("idno_name"));
		sp.ppstr("bank_name", varsStr("bank_name"));
		sp.ppstr("send_proc_flag", "Y");
		sp.ppstr("crt_user", varsStr("crt_date"));
		sp.ppstr("crt_date", varsStr("crt_date"));
		sp.addsql(", apr_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;

	}

	int updateFunc() {

		if (rc != 1)
			return rc;

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_liad_570");
		sp.ppstr("pay_normal_flag_2", varsStr("pay_normal_flag_2"));
		sp.ppstr("debt_amt2", varsStr("debt_amt2"));
		sp.ppstr("alloc_debt_amt", varsStr("alloc_debt_amt"));
		sp.ppstr("unalloc_debt_amt", varsStr("unalloc_debt_amt"));
		sp.ppstr("coll_remark", varsStr("coll_remark"));
		sp.ppstr("send_flag_571", varsStr("send_flag_571"));
		sp.ppstr("crt_date", varsStr("crt_date"));
		sp.ppstr("crt_user", varsStr("crt_user"));
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
//		sp.sql2Where(" where id_no = ?", kk1);
		sp.sql2Where(" where id_p_seqno = ?", kkIdPSeqno);
		sp.sql2Where(" and case_letter = ?", caseLetter);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", varModseqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	public int deleteColLiadLiad570Tmp() {
		strSql = "delete col_liad_mod_tmp where data_type ='COLL-DATA' "
				+ "and data_key = rpad (?, 10, ' ') || rpad (?, 10, ' ') ";
		Object[] param = new Object[] { varsStr("id_no"), varsStr("case_letter") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum == 0) {
			rc = 0;
		}
		return rc;
	}

}
