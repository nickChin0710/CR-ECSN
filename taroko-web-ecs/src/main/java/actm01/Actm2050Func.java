/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial    
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard                        *
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2050Func extends FuncEdit {
	String kk1 = "";

	public Actm2050Func(TarokoCommon wr) {
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
		kk1 = varsStr("dual_no");

//		//-other modify-
//		sql_where = "where hex(rowid) = ? "
//				  + "and nvl(mod_seqno,0) = ? ";
//		Object[] param = new Object[] { kk1, wp.mod_seqno() };
//		other_modify("act_jrnl", sql_where, param);
	}

	@Override
	public int dbInsert() {
		//No use..
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1) return rc;

		rc = deleteFunc();
		if (rc != 1) return rc;
		
		rc = insertFunc();
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if (rc != 1) return rc;

		rc = deleteFunc();
		
		return rc;
	}
	
	int insertFunc() {

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_dual");
		sp.ppstr("func_code", "0703");
		sp.ppstr("dual_key", kk1);
		sp.ppstr("aud_type", "U");
		sp.ppstr("log_data", varsStr("log_data"));
		sp.ppstr("chg_user", wp.loginUser);
		sp.addsql(", chg_date ",", to_char(sysdate,'yyyymmdd') ");		
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_seqno", "0");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) { 
        	rc = -1; 
        }
		return rc;
	}
	
	int deleteFunc() {

		strSql = "delete act_dual "
				+ "where dual_key = ? "
				+ "and   func_code = '0703' ";
		Object[] param = new Object[] { kk1 };
		rc = sqlExec(strSql, param);
		if (sqlRowNum < 0) {
			rc = -1;
		}	else rc =1;
		
		return rc;
	}

}
