/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-22  V1.00.01  ryan       program initial                            *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2130Func extends FuncEdit {

	public Actm2130Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TOD11111
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
		if (this.isAdd()) {
			// 檢查新增資料是否重複
			String lsSql = "select count(*) as tot_cnt from act_class_ex where p_seqno = ? ";
			Object[] param = new Object[] {varsStr("ls_p_seqno")};
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增,請從新查詢");
				return;
			}
		
		} else {
			// -other modify-
			sqlWhere = " where 1=1 and p_seqno = ?  and nvl(mod_seqno,0) = ?";
			Object[] param = new Object[] { wp.itemStr("p_seqno"), wp.itemStr("mod_seqno") };
			if (this.isOtherModify("act_class_ex", sqlWhere, param)) {
				errmsg("請重新查詢 !");
			}
		}
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_class_ex");
		sp.ppstr("p_seqno", varsStr("ls_p_seqno"));
		sp.ppstr("acct_type", wp.itemStr("kk_acct_type"));
		sp.ppstr("class_code", wp.itemStr("class_code"));
		sp.ppstr("value_s_date", wp.itemStr("value_s_date"));
		sp.ppstr("value_e_date", wp.itemStr("value_e_date"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 1);
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_class_ex");
		sp.ppstr("class_code", wp.itemStr("class_code"));
		sp.ppstr("value_s_date", wp.itemStr("value_s_date"));
		sp.ppstr("value_e_date", wp.itemStr("value_e_date"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", ", mod_seqno =nvl(mod_seqno,0)+1 ");
		sp.sql2Where(" where p_seqno=?", wp.itemStr("p_seqno"));
		sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;

	}

	@Override
	public int dbDelete() {
		return rc;
	}

}
