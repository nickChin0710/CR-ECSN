/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial   
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard                         *
* 112-12-05  V1.00.04  Simon      1.免違約金、循環息合併維護                 *
*                                 2.以ptr_businday.business_date update act_dual.chg_date*                 *
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2030Func extends FuncEdit {
	String kk1 = "";

	public Actm2030Func(TarokoCommon wr) {
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
		//No use..
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
//		dataCheck();
		if (rc != 1) return rc;

		String lsBusiDate = "";
		String sql1 = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql1);
		lsBusiDate = colStr("business_date");

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_dual");
		sp.ppstr("func_code", varsStr("func_code"));
		sp.ppstr("dual_key", varsStr("dual_key"));
		sp.ppstr("aud_type", "U");
		sp.ppstr("log_data", varsStr("log_data"));
//		2018.10.26 問題單0000957: 查詢錯誤.  原因 table改欄位 b.update_date ,b.update_time ,b.update_user --> chg_date, chg_user
//		sp.ppss("update_user", wp.loginUser);
//		sp.addsql(", update_date ",", to_char(sysdate,'yyyymmdd') ");
//		sp.addsql(", update_time ",", to_char(sysdate,'hh24miss') "); --> 移除
		sp.ppstr("chg_user", wp.loginUser);
	//sp.addsql(", chg_date ",", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("chg_date", lsBusiDate);
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
	public int dbDelete() {
		actionInit("D");
//		dataCheck();
		if(rc!=1) return rc;
		
		kk1 = varsStr("rowid");
		strSql = "delete act_dual "
				+ "where rowid = ? ";
		setRowId(1,kk1);
		rc = sqlExec(strSql);
		
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
