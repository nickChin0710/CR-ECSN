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

public class Colp0130Func extends FuncProc {
	String referenceNo;

	public Colp0130Func(TarokoCommon wr) {
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
		referenceNo = varsStr("reference_no");

		// -other modify-
		sqlWhere = " where reference_no = ? " + "and mod_seqno = ?";
		Object[] param = new Object[] { referenceNo, varModseqno() };
		if (isOtherModify("col_debt_t", sqlWhere, param)) {
			return;
		}
	}

	@Override
	public int dataProc() {
		dataCheck();
		if (rc != 1)
			return rc;

		rc = updateFunc();

		return rc;
	}

	int updateFunc() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_debt_t");
		sp.ppstr("aud_code", "2");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where reference_no = ? ", referenceNo);
		sp.sql2Where(" and mod_seqno = ?", varModseqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

}
