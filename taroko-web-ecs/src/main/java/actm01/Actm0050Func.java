/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-10  V1.00.00  Andy Liu      program initial                            *
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard                                                                            *
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0050Func extends FuncEdit {
	String mKkMchtNo = "";

	public Actm0050Func(TarokoCommon wr) {
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
		// check PK

	}

	@Override
	public int dbInsert() {
		actionInit("A");
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("ACT_MODDATA_TMP");
		sp.ppstr("act_modtype", varsStr("act_modtype"));
		sp.ppstr("p_seqno", varsStr("p_seqno"));
		sp.ppstr("curr_code", varsStr("curr_code"));
		sp.ppstr("acct_type", varsStr("acct_type"));		
		sp.ppstr("acct_data", varsStr("acct_data"));
		sp.addsql(", update_date ",", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("update_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_seqno", "1");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");

		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		
		String colPSeqno = wp.itemStr2("q_p_seqno");
		
		strSql = "delete ACT_MODDATA_TMP where p_seqno = ? and act_modtype='01'";
		Object[] param = new Object[] { colPSeqno };
		rc = sqlExec(strSql, param);
		if (sqlRowNum < 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int deleteOne() throws Exception {
		actionInit("D");
						
		strSql = "delete ACT_MODDATA_TMP where 1=1 "+commSqlStr.whereRowid(varsStr("rowid"));
		
		sqlExec(strSql);
		
		if (sqlRowNum <= 0) {
			errmsg("delete act_moddata_tmp error ");
		}
		return rc;
	}
	
}
