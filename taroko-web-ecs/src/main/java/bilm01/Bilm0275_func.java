/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-02  V1.00.00  yash       program initial                            *
* 108-05-21  V1.00.01  Andy       Update                                     *
* 109-04-20  V1.00.02  Amber      Update:Add throws Exception                *
* 109-05-28  V1.00.03  Andy       Update:Mantis3515                          *
* 109-07-01  V1.00.04  Amber      Update:Mantis 0003702                      *
* 109-07-07  V1.00.05  Amber      Update BUG   						         *
* 109-07-16  V1.00.07  Amber      Update    						         *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0275_func extends FuncEdit {
	String m_kk_rowid = "";
	String m_kk_telephone_no = "";
	String m_kk_office_m_code = "";
	String m_kk_office_code = "";
	String m_kk_card_no = "";
	String m_uniform_no = "";
	String ls_transaction_type = "", ls_end_flag = "", ls_feed_back_tx_flag = "";

	String ls_card_no = "", ls_id_p_seqno = "", ls_auth_batch_no = "", ls_error_code = "", ls_error_code1 = "";
	String ls_bank_no = "", ls_confirm_date = "", ls_feed_back_date = "", ls_master_rowid = "", ls_tel_chkmark = "";
	String ls_computer_no = "", ls_effc_date = "", ls_city_x160 = "", ls_uniform_no = "", ls_remark = "";
	String ls_db_action = "";

	public Bilm0275_func(TarokoCommon wr) {
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
		ls_db_action = wp.itemStr("db_action");		//作業代號:1.新增 2.修改 3.終止
		m_kk_rowid = wp.itemStr("rowid");
		m_kk_telephone_no = wp.itemStr("telephone_no");
		m_kk_office_m_code = wp.itemStr("office_m_code");
		m_kk_office_code = wp.itemStr("office_code");
		m_kk_card_no = wp.itemStr("card_no");
		m_uniform_no = wp.itemStr("uniform_no");
		ls_remark =  wp.itemStr("remark");
		
		sqlWhere = " where card_no = ? and telephone_no = ? and office_m_code=? and office_code=? and uniform_no=?  and nvl(mod_seqno,0) = ?";
		
		if(empty(m_kk_office_m_code)){
			m_kk_office_m_code = wp.itemStr("kk_office_m_code");
		}
		
		if (ls_db_action.equals("1")) {
			ls_transaction_type = "1";
			ls_end_flag = "N";
			ls_feed_back_tx_flag = "P";
		}	
		if (wp.itemStr("db_action").equals("2")) {
			ls_transaction_type = "2";
			ls_end_flag = "N";
			ls_feed_back_tx_flag = "P";

		} else if (wp.itemStr("db_action").equals("3")) {
			ls_transaction_type = "3";
			ls_end_flag = "Y";
			ls_feed_back_tx_flag = "P";
		}
		
		if (this.isAdd()) {
			// 檢查新增資料是否重複
	
		}else {
			//System.out.println("m_uniform_no="+m_uniform_no);
			// -other modify-
//			sqlWhere = " where card_no = ? and telephone_no = ? and office_m_code=? and office_code=? and uniform_no=?  and nvl(mod_seqno,0) = ?";
//			Object[] param = new Object[] { m_kk_card_no, m_kk_telephone_no, m_kk_office_m_code, m_kk_office_code, m_uniform_no, wp.mod_seqno() };			
//			if(other_modify("bil_chtmain_t", sqlWhere, param)) {
//				errmsg("已有待覆核資料，請至「未放行」做修改 !");
//				return;
//			}
		}
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		
		busi.SqlPrepare sp = new SqlPrepare();		
		sp.sql2Insert("bil_chtmain");
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("id_p_seqno",  wp.itemStr("id_p_seqno"));
		sp.ppstr("telephone_no",  wp.itemStr("telephone_no"));
		sp.ppstr("auth_batch_no", "");
		sp.ppstr("office_m_code",  m_kk_office_m_code);// 20200701 update by Amber
		sp.ppstr("office_code",  wp.itemStr("office_code"));
		sp.ppstr("end_flag", ls_end_flag);		
		sp.ppstr("transaction_type", ls_transaction_type);
		sp.ppstr("error_code", wp.itemStr("error_code"));
		sp.ppstr("error_code1", wp.itemStr("error_code1"));
		sp.ppstr("bank_no", wp.itemStr("bank_no")); // 20190418 update by Andy
		sp.ppstr("confirm_date", wp.sysDate ); // 20200702 update by Amber
		sp.ppstr("confirm_flag", "Y");
		sp.ppstr("feed_back_tx_flag", ls_feed_back_tx_flag);
		//sp.ppstr("master_rowid", wp.itemStr("rowid"));		
		sp.ppstr("tel_chkmark", wp.itemStr("tel_chkmark"));
		sp.ppstr("computer_no", wp.itemStr("computer_no"));
		sp.ppstr("effc_date", wp.itemStr("effc_date"));
		sp.ppstr("city_x160", wp.itemStr("uniform_no"));// 20200707 update by Amber
		sp.ppstr("uniform_no", wp.itemStr("uniform_no"));		
		sp.ppstr("remark",  wp.itemStr("remark"));   // 20190521 update by Andy
		sp.ppstr("mod_user", wp.loginUser);
		sp.addsql(", mod_time ", ", sysdate ");		
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 1);
//		System.out.println("card_no :"+wp.itemStr("card_no"));
//		System.out.println("telephone_no :"+wp.itemStr("telephone_no"));
//		System.out.println("office_m_code :"+wp.itemStr("office_m_code"));
//		System.out.println("office_code :"+wp.itemStr("office_code"));
//		System.out.println("uniform_no :"+wp.itemStr("uniform_no"));
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}
		
//		
//		if (ls_db_action.equals("1")) {
//			busi.SqlPrepare sp = new SqlPrepare();
//			sp.sql2Insert("bil_chtmain_t");
//			sp.ppstr("id_p_seqno", vars_ss("aa_id_p_seqno"));
//			sp.ppstr("office_m_code", vars_ss("aa_office_m_code"));
//			sp.ppstr("office_code", vars_ss("aa_office_code"));
//			sp.ppstr("telephone_no", vars_ss("aa_telephone_no"));
//			sp.ppstr("card_no", vars_ss("aa_card_no"));
//			sp.ppstr("transaction_type", "1");
//			sp.ppstr("feed_back_tx_flag", "N");
//			sp.ppstr("end_flag", "N");
//			sp.ppstr("remark", vars_ss("aa_remark"));   // 20190521 update by Andy
//			sp.ppstr("bank_no", vars_ss("aa_bank_no")); // 20190418 update by Andy
//			sp.addsql(", mod_time ", ", sysdate ");
//			sp.ppstr("mod_user", wp.loginUser);
//			sp.ppstr("mod_pgm", wp.mod_pgm());
//			sp.ppnum("mod_seqno", 1);
//			sqlExec(sp.sql_stmt(), sp.sql_parm());
//			if (sqlRowNum <= 0) {
//				errmsg(sqlErrtext);
//			}
//		} else {			
//			busi.SqlPrepare sp = new SqlPrepare();
//			sp.sql2Insert("bil_chtmain_t");
//			sp.ppstr("card_no", ls_card_no);
//			sp.ppstr("id_p_seqno", ls_id_p_seqno);
//			sp.ppstr("telephone_no", m_kk_telephone_no);
//			sp.ppstr("auth_batch_no", ls_auth_batch_no);
//			sp.ppstr("office_m_code", m_kk_office_m_code);
//			sp.ppstr("office_code", m_kk_office_code);
//			sp.ppstr("end_flag", ls_end_flag);
//			sp.ppstr("transaction_type", ls_transaction_type);
//			sp.ppstr("error_code", ls_error_code);
//			sp.ppstr("error_code1", ls_error_code1);
//			sp.ppstr("bank_no", ls_bank_no);
//			sp.ppstr("confirm_date", ls_confirm_date);
//			sp.ppstr("confirm_flag", "N");
//			sp.ppstr("feed_back_date", ls_feed_back_date);
//			sp.ppstr("feed_back_tx_flag", ls_feed_back_tx_flag);
//			sp.ppstr("master_rowid", ls_master_rowid);
//			sp.ppstr("tel_chkmark", ls_tel_chkmark);
//			sp.ppstr("effc_date", ls_effc_date);
//			sp.ppstr("city_x160", ls_city_x160);
//			sp.ppstr("uniform_no", ls_uniform_no);
//			sp.ppstr("remark", ls_remark);
//			sp.ppstr("computer_no", ls_computer_no);
//			sp.ppstr("mod_user", wp.loginUser);
//			sp.addsql(", mod_time ", ", sysdate ");			
//			sp.ppstr("mod_pgm", wp.mod_pgm());
//			sp.ppnum("mod_seqno", 1);
//			
//			
//			
//			sqlExec(sp.sql_stmt(), sp.sql_parm());
//			if (sqlRowNum <= 0) {
//				errmsg(sqlErrtext);
//			}
//		}

		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("bil_chtmain");
		sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("transaction_type", ls_transaction_type);
		sp.ppstr("confirm_flag","Y");
		sp.ppstr("confirm_date", wp.sysDate);
		sp.ppstr("error_code1", wp.itemStr("error_code1"));
		sp.ppstr("end_flag", ls_end_flag);
		sp.ppstr("feed_back_tx_flag", ls_feed_back_tx_flag);
		sp.ppstr("remark", ls_remark);
		sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1 ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.sql2Where(" where hex(rowid)=?", m_kk_rowid);
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
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
		strSql = "delete bil_chtmain " + sqlWhere;
		Object[] param = new Object[] {
				m_kk_card_no, m_kk_telephone_no, m_kk_office_m_code, m_kk_office_code, m_uniform_no, wp.itemStr("mod_seqno") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
