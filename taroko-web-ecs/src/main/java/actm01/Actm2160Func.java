/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-24  V1.00.01  ryan       program initial                            *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2160Func extends FuncEdit {

	public Actm2160Func(TarokoCommon wr) {
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
			/*String lsSql = "select count(*) as tot_cnt from act_acaj where hex(rowid) = ? ";
			Object[] param = new Object[] {vars_ss("ls_p_seqno")};
			sqlSelect(lsSql, param);
			if (col_num("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增,請從新查詢");
				return;
			}*/
		
		} else {
			// -other modify-
			sqlWhere = " where 1=1 and hex(rowid) = ?  and mod_seqno = ?";
			Object[] param = new Object[] { wp.itemStr("rowid"), wp.itemStr("mod_seqno") };
			if (this.isOtherModify("act_acaj", sqlWhere, param)) {
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
		sp.sql2Insert("act_acaj");
	//sp.ppss("acct_type", wp.item_ss("kp_acct_type")); insert 前 先從 item_ss(kp_acct_type) 搬到 item_ss(kk_acct_type)
		sp.ppstr("acct_type", wp.itemStr("kk_acct_type")); 
		sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
		sp.ppstr("card_no", wp.itemStr("kp_card_no"));
		sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("curr_code", wp.itemStr("curr_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
		sp.ppstr("post_date", wp.sysDate);
		sp.ppstr("crt_date", wp.sysDate);
		sp.ppstr("crt_time", wp.sysTime);
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("Update_date", wp.sysDate);
		sp.ppstr("Update_user", wp.loginUser);
		sp.ppstr("adjust_type", "AI01");
		sp.ppnum("orginal_amt", wp.itemNum("bef_amt"));
		sp.ppnum("bef_amt", wp.itemNum("end_amt"));
		sp.ppnum("bef_d_amt", wp.itemNum("bef_d_amt"));
		sp.ppnum("aft_amt", varsNum("aft_amt"));
		sp.ppnum("aft_d_amt", varsNum("aft_d_amt"));
		sp.ppnum("dr_amt", wp.itemNum("dr_amt"));
		sp.ppnum("cr_amt", wp.itemNum("cr_amt"));
		sp.ppnum("dc_orginal_amt", wp.itemNum("bef_amt"));
		sp.ppnum("dc_bef_amt", wp.itemNum("end_amt"));
		sp.ppnum("dc_bef_d_amt", wp.itemNum("bef_d_amt"));
		sp.ppnum("dc_dr_amt", wp.itemNum("dr_amt"));
		sp.ppnum("dc_cr_amt", wp.itemNum("cr_amt"));
		sp.ppnum("dc_aft_amt", varsNum("aft_amt"));
		sp.ppnum("dc_aft_d_amt", varsNum("aft_d_amt"));
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
		sp.sql2Update("act_acaj");
	//sp.ppss("acct_type", wp.item_ss("kp_acct_type")); insert 前 先從 item_ss(kp_acct_type) 搬到 item_ss(kk_acct_type)
		sp.ppstr("acct_type", wp.itemStr("kk_acct_type")); 
		sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("curr_code", wp.itemStr("curr_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
		sp.ppstr("post_date", wp.sysDate);
		sp.ppstr("update_date", wp.sysDate);
		sp.ppstr("update_user", wp.loginUser);
  	sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
	  sp.addsql(", crt_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("adjust_type", "AI01");
		sp.ppnum("orginal_amt", wp.itemNum("bef_amt"));
		sp.ppnum("bef_amt", wp.itemNum("end_amt"));
		sp.ppnum("bef_d_amt", wp.itemNum("bef_d_amt"));
		sp.ppnum("aft_amt", varsNum("aft_amt"));
		sp.ppnum("aft_d_amt", varsNum("aft_d_amt"));
		sp.ppnum("dr_amt", wp.itemNum("dr_amt"));
		sp.ppnum("cr_amt", wp.itemNum("cr_amt"));
		sp.ppnum("dc_orginal_amt", wp.itemNum("bef_amt"));
		sp.ppnum("dc_bef_amt", wp.itemNum("end_amt"));
		sp.ppnum("dc_bef_d_amt", wp.itemNum("bef_d_amt"));
		sp.ppnum("dc_dr_amt", wp.itemNum("dr_amt"));
		sp.ppnum("dc_cr_amt", wp.itemNum("cr_amt"));
		sp.ppnum("dc_aft_amt", varsNum("aft_amt"));
		sp.ppnum("dc_aft_d_amt", varsNum("aft_d_amt"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", ", mod_seqno =nvl(mod_seqno,0)+1 ");
		sp.sql2Where(" where hex(rowid)=?", wp.itemStr("rowid"));
		sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;

	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		 	strSql = "delete act_acaj " +sqlWhere;
	        Object[] param = new Object[] { wp.itemStr("rowid"), wp.itemStr("mod_seqno")};
	   
	        rc = sqlExec(strSql, param);
	      
	        if (sqlRowNum < 0) {
		        errmsg(this.sqlErrtext);
	        }
	        return rc;
	}

}
