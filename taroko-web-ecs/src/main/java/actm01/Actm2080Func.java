/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-20  V1.00.04  Simon      remove mobile_msg_xxx data update          *
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2080Func extends FuncEdit {
	String kk1 = "";

	public Actm2080Func(TarokoCommon wr) {
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
		kk1 = varsStr("acno_p_seqno");
		
		//-other modify-
		sqlWhere = "where acno_p_seqno = ? "
				  + "and nvl(mod_seqno,0) = ? ";
		Object[] param = new Object[] { kk1, wp.modSeqno() };
		isOtherModify("act_acno", sqlWhere, param);
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

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_acno");
		sp.ppstr("stat_send_paper", varsStr("stat_send_paper"));
		sp.ppstr("stat_send_internet", varsStr("stat_send_internet"));
		sp.ppstr("stat_send_s_month", varsStr("stat_send_s_month"));
		sp.ppstr("stat_send_e_month", varsStr("stat_send_e_month"));
		sp.ppstr("stat_send_s_month2", varsStr("stat_send_s_month2"));
		sp.ppstr("stat_send_e_month2", varsStr("stat_send_e_month2"));
		sp.ppstr("paper_upd_date", varsStr("paper_upd_date"));
		sp.ppstr("paper_upd_user", varsStr("paper_upd_user"));
		sp.ppstr("internet_upd_date", varsStr("internet_upd_date"));
		sp.ppstr("internet_upd_user", varsStr("internet_upd_user"));

	//sp.ppstr("mobile_msg", varsStr("mobile_msg"));
	//sp.ppstr("mobile_msg_smonth", varsStr("mobile_msg_smonth"));
	//sp.ppstr("mobile_msg_emonth", varsStr("mobile_msg_emonth"));
	//sp.ppstr("mobile_msg_upd_date", varsStr("mobile_msg_upd_date"));
	//sp.ppstr("mobile_msg_upd_user", varsStr("mobile_msg_upd_user"));

		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where acno_p_seqno = ?", kk1);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
    wp.colSet("acno_p_seqno", "");
		return rc;
	}

	@Override
	public int dbDelete() {
		//No use..
		return rc;
	}

}
