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

public class Colp1110Func extends FuncProc {
	String rowid;
	byte[] rowidByte = null;
	String kkOptname;

	public Colp1110Func(TarokoCommon wr) {
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
		String kkTable = "";
		rowid = varsStr("rowid");
		rowidByte = wp.hexStrToByteArr(rowid);
		kkOptname = wp.itemStr("optname");

		// -other modify-
		sqlWhere = "where rowid = ? " + "and mod_seqno = ?";
		Object[] param = new Object[] { rowidByte, varModseqno() };
		kkTable = "aopt".equals(kkOptname) ? "col_liac_debt" : "col_liac_debt_ch";
		if (isOtherModify(kkTable, sqlWhere, param)) {
			return;
		}

		return;
	}

	@Override
	public int dataProc() {
//		dataCheck();
//		rc = updateFunc();
		return rc;
	}

	public int dataProcA() {
		dataCheck();
		if (rc != 1)
			return rc;

		rc = updateFuncA();

		return rc;
	}

	public int dataProcB() {
		dataCheck();
		if (rc != 1)
			return rc;

		rc = updateFuncB();

		return rc;
	}

	int updateFuncA() {
		strSql = "update col_liac_debt set " + " apr_flag = 'Y', " + " apr_date = to_char(sysdate,'yyyymmdd'), "
				+ " apr_user = ?, " + " mod_user = ?, " + " mod_time=sysdate, mod_pgm =?, "
				+ " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
		Object[] param = new Object[] { wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), rowidByte, varModseqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	public int updateColLiacNego() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_liac_nego");
		sp.ppstr("liac_status", "4");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where liac_seqno = ? ", varsStr("liac_seqno"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	public int insertColLiacRemod() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_liac_remod");
		sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
		sp.ppstr("id_no", varsStr("id_no"));
		sp.ppstr("liac_status", "4");
		sp.ppstr("liac_seqno", varsStr("liac_seqno"));
		sp.ppstr("proc_flag", "N");
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	public int insertColLiacNegoHst() {

		strSql = "insert into col_liac_nego_hst "
				+ "select liac_seqno, to_char(sysdate,'yyyymmdd'), to_char(sysdate,'hh24miss'), "
				+ "file_date, liac_status, query_date, notify_date, payment_rate, id_p_seqno, id_no, "
				+ "chi_name, bank_code, bank_name, apply_date, nego_s_date, acct_status_apply, "
				+ "m_code_apply, stop_notify_date, interest_base_date, recol_reason, credit_flag, "
				+ "no_credit_flag, cash_card_flag, credit_card_flag, contract_date, liac_remark, "
				+ "end_date, end_user, end_reason, end_remark, liac_txn_code, reg_bank_no, "
				+ "id_data_date, court_agree_date, case_status, '', '', '', '', '', "
				// delay_agree_date, delay_1_month, delay_2_month, delay_reason, delay_desc,
				// 這些欄位不在col_liac_nego中
				+ "crt_date, crt_time, apr_flag, apr_date, apr_user, proc_flag, proc_date, "
				+ "mod_time, mod_pgm from col_liac_nego where liac_seqno = ? ";
		Object[] param = new Object[] { varsStr("liac_seqno") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	int updateFuncB() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_liac_debt_ch");
		sp.ppstr("proc_flag", "1");
		sp.addsql(", proc_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_flag", "Y");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where rowid = ? ", rowidByte);
		sp.sql2Where(" and mod_seqno = ? ", varModseqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

}
