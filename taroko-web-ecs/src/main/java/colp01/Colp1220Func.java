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

import taroko.com.TarokoCommon;

public class Colp1220Func extends FuncProc {
	String rowid;
	byte[] rowidId = null;

	public Colp1220Func(TarokoCommon wr) {
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
		rowid = varsStr("rowid");
		rowidId = wp.hexStrToByteArr(rowid);

		// -other modify-
		sqlWhere = "where rowid = ? " + "and mod_seqno = ?";
		Object[] param = new Object[] { rowidId, varModseqno() };
		if (isOtherModify("col_lgd_jrnl", sqlWhere, param)) {
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

	int updateFunc() {
		if (rc != 1)
			return rc;
		strSql = "update col_lgd_jrnl set " + " apr_date = to_char(sysdate,'yyyymmdd'), " + " apr_user = ?, "
				+ " mod_user = ?, " + " mod_time=sysdate, mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;

		Object[] param = new Object[] { wp.loginUser, wp.loginUser, wp.modPgm(), rowidId, varModseqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
