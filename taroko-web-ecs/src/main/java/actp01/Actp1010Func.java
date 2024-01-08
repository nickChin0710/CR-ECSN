/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-07  V1.00.00  Andy       program initial                            *
* 111-10-25  v1.00.01  Yang Bo    Sync code from mega                        *
******************************************************************************/

package actp01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actp1010Func extends FuncEdit {
	String mProcessDate = "";
	String mOldAcctBank = "";


	public Actp1010Func(TarokoCommon wr) {
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
		
//		if (this.isAdd()) {
//			// 檢查新增資料是否重複
//			String lsSql = "select count(*) as tot_cnt from act_ach_chgbank where process_date = ? "
//					+ "and old_acct_bank = ?";
//			Object[] param = new Object[] { m_process_date ,m_old_acct_bank};
//			sqlSelect(lsSql, param);
//			if (col_num("tot_cnt") > 0) {
//				errmsg("資料已存在，無法新增");
//			}
//			return;
//		} else {
//			// -other modify-
//			sql_where = " where process_date = ? and old_acct_bank = ? and nvl(mod_seqno,0) = ?";
//			Object[] param = new Object[] { m_process_date, m_old_acct_bank, wp.mod_seqno() };
//			other_modify("act_ach_chgbank", sql_where, param);
//		}
	}

	@Override
	public int dbInsert() {
//		actionInit("A");
//		dataCheck();
//
//		if (rc != 1) {
//			return rc;
//		}
//		
//		busi.sqlPrepare sp = new sqlPrepare();
//		sp.sql2Insert("act_ach_chgbank");
//		sp.ppss("process_date", m_process_date);
//		sp.ppss("old_acct_bank", m_old_acct_bank);
//		sp.ppss("new_acct_bank", wp.item_ss("new_acct_bank"));		
//		sp.addsql(", mod_time ", ", sysdate ");
//		sp.ppss("mod_user", wp.loginUser);
//		sp.ppss("mod_pgm", wp.mod_pgm());
//		sp.ppss("mod_seqno", "1");
//		rc = sqlExec(sp.sql_stmt(), sp.sql_parm());
//		if (sql_nrow <= 0) {
//			errmsg(this.sql_errtext);
//			return -1;
//		}
//		String ls_sql = "select count(*) ct from act_ach_chgmst where process_date =:process_date ";
//		setString("process_date",m_process_date);
//		sqlSelect(ls_sql);
//		if(col_num("ct")==0){
//			rc = dbInsert1();				
//		}else{
//			return rc;
//		}
		return rc;
	}	

	@Override
	public int dbUpdate() {
//		busi.sqlPrepare sp = new sqlPrepare();
//		sp.sql2Update("act_ach_chgbank");
//		sp.ppss("new_acct_bank", wp.item_ss("new_acct_bank"));		
//		sp.addsql(", mod_time =sysdate", "");
//		sp.ppss("mod_user", wp.loginUser);
//		sp.ppss("mod_pgm", wp.mod_pgm());
//		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
//		sp.sql2Where("where hex(rowid)=?", wp.item_ss("rowid"));
//		rc = sqlExec(sp.sql_stmt(), sp.sql_parm());
//		if (sql_nrow <= 0) {
//			errmsg(this.sql_errtext);
//			return rc;
//		}
		return 1;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		strSql = "delete act_chg_cycle where 1=1 "
				+ "and hex(rowid) =:rowid "
				+ "and mod_seqno =:mod_seqno ";
		setString("rowid", wp.colStr("rowid"));
		setString("mod_seqno", wp.colStr("mod_seqno"));
		rc = sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
			return rc;
		}		
		return rc;
	}
	

}
