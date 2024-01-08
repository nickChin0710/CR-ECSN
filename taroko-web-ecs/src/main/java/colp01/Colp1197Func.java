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

public class Colp1197Func extends FuncProc {
	String liadDocNo;
	String liadDocSeqno;

	public Colp1197Func(TarokoCommon wr) {
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
		liadDocSeqno = varsStr("liad_doc_seqno");
		log(this.actionCode + ", kk1=" + liadDocNo + ", kk2=" + liadDocSeqno + ", mod_seqno=" + varModseqno());

		// -other modify-
		sqlWhere = "where liad_doc_no = ? " + "and liad_doc_seqno = ? " + "and mod_seqno = ? ";
		Object[] param = new Object[] { liadDocNo, liadDocSeqno, varModseqno() };
		if (isOtherModify("col_liad_liquidate_court", sqlWhere, param)) {
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
		sp.sql2Update("col_liad_liquidate_court");
		sp.ppstr("unit_doc_no", varsStr("unit_doc_no"));
		sp.ppstr("recv_date", varsStr("recv_date"));
		sp.ppstr("key_note", varsStr("key_note"));
		sp.ppstr("case_letter_desc", varsStr("case_letter_desc"));
		sp.ppstr("case_date", varsStr("case_date"));
		sp.ppstr("apr_flag", "Y");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where liad_doc_no = ?", liadDocNo);
		sp.sql2Where(" and liad_doc_seqno = ?", liadDocSeqno);
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

		strSql = "delete col_liad_liquidate_court " + sqlWhere;
		Object[] param = new Object[] { liadDocNo, liadDocSeqno, varModseqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = 0;
		}
		return rc;
	}

	public int deleteColLiadModTmp() {
		String kkDataKey = varsStr("liad_doc_no") + varsStr("liad_doc_seqno");
		strSql = "delete col_liad_mod_tmp where data_type ='LIQUICOURT' and data_key = ? ";
		Object[] param = new Object[] { kkDataKey };
		rc = sqlExec(strSql, param);
		if (sqlRowNum == 0) {
			rc = 0;
		}

		return rc;
	}

	public int updateColLiadLiquidateCaseDate() {
		strSql = "update col_liad_liquidate set case_date = "
				+ " (select nvl(max(case_date),'') as case_date from col_liad_liquidate_court "
				+ " where liad_doc_no = col_liad_liquidate.liad_doc_no and apr_flag = 'Y'), "
				+ "mod_user = ?, mod_time=sysdate, mod_pgm = ?, mod_seqno =nvl(mod_seqno,0)+1 "
				+ "where liad_doc_no = ? ";
		Object[] param = new Object[] { wp.loginUser, wp.modPgm(), varsStr("liad_doc_no") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum == 0) {
			rc = 0;
		}

		return rc;
	}

}
