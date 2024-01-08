/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-07-21  V1.00.04  Simon      取消銷帳鍵值、借方科目                     *
* 112-12-18  V1.00.05  Simon      1.各科目調整設定獨立調整類別               *
*                                 2.新增減免分期付款利息調整類別             *
*                                 3.act_acaj.crt_date, update_date 以        *
*                                   business_date 取代 sysDate               *
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0032Func extends FuncEdit {
	String kk1 = "";
	String kkTable = "";
	byte[] kkk = null;
	String hSql = "", hBusiDate="";

	public Actm0032Func(TarokoCommon wr) {
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
//		kk1 = wp.item_ss("rowid");
//		kk_table = wp.item_ss("db_table");
//		
//		if (eq_igno(kk_table,"acaj")) {
//			//-other modify-
//			sql_where = "where hex(rowid) = ? "
//					  + "and nvl(mod_seqno,0) = ? ";
//			Object[] param = new Object[] { kk1, wp.mod_seqno() };
//			other_modify("act_acaj", sql_where, param);
//		}
		hSql = "Select business_date from ptr_businday fetch first 1 rows only ";
		sqlSelect(hSql);
		if (sqlRowNum > 0) {
      hBusiDate = colStr("business_date");
		}else {
			errmsg("無法取得系統營業日!");
			return;
		}

	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
 		if (rc != 1) return rc; 
	  String tAcctCode = "", tAdjType = "", tBillType = "";
		tAcctCode = varsStr("acct_code");
		tBillType = varsStr("bill_type");

		if(tAcctCode.equals("BL")) { 
			tAdjType = "DE01";
		}else if(tAcctCode.equals("CA")) { 
			tAdjType = "DE02";
		}else if(tAcctCode.equals("IT")) { 
			tAdjType = "DE03";
		}else if(tAcctCode.equals("ID")) { 
			tAdjType = "DE04";
		}else if(tAcctCode.equals("AO")) { 
			tAdjType = "DE05";
		}else if(tAcctCode.equals("OT")) { 
			tAdjType = "DE06";
		}else if(tAcctCode.equals("CB")) { 
			tAdjType = "DE07";
		}else if(tAcctCode.equals("DB")) { 
			tAdjType = "DE08";
		}else if(tAcctCode.equals("PF")) { 
			tAdjType = "DE09";
		}else if(tAcctCode.equals("CF")) { 
			tAdjType = "DE19";
		}else if(tAcctCode.equals("CC")) { 
			tAdjType = "DE29";
		}else if(tAcctCode.equals("SF")) { 
			tAdjType = "DE39";
		}else if(tAcctCode.equals("AF")) {  
			tAdjType = "DE10";
		}else if(tAcctCode.equals("LF")) {  
			tAdjType = "DE20";
		}else if(tAcctCode.equals("RI")) { 
		  if(tBillType.equals("OSSG")) {
  			tAdjType = "DE13";//減免循環息
		  } else {
  			tAdjType = "DE23";//減免分期付款利息
		  }
		}else if(tAcctCode.equals("CI")) {
			tAdjType = "DE33";
		}else if(tAcctCode.equals("AI")) {
			tAdjType = "DE43";
		}else if(tAcctCode.equals("PN")) {
			tAdjType = "DE14";
		}		
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_acaj");
		sp.ppstr("p_seqno", varsStr("p_seqno"));
		sp.ppstr("acct_type", varsStr("acct_type"));
		sp.ppstr("reference_no", varsStr("reference_no"));
		sp.ppstr("post_date", varsStr("post_date"));
		sp.ppstr("orginal_amt", varsStr("orginal_amt"));
		sp.ppstr("dr_amt", varsStr("dr_amt"));
		sp.ppstr("cr_amt", varsStr("cr_amt"));
		sp.ppstr("bef_amt", varsStr("bef_amt"));
		sp.ppstr("aft_amt", varsStr("aft_amt"));
		sp.ppstr("bef_d_amt", varsStr("bef_d_amt"));
		sp.ppstr("aft_d_amt", varsStr("aft_d_amt"));
		sp.ppstr("acct_code", varsStr("acct_code"));
		sp.ppstr("function_code", varsStr("function_code"));
		sp.ppstr("card_no", varsStr("card_no"));
		sp.ppstr("interest_date", varsStr("interest_date"));
		sp.ppstr("curr_code", varsStr("curr_code"));
		sp.ppstr("dc_orginal_amt", varsStr("dc_orginal_amt"));
		sp.ppstr("dc_dr_amt", varsStr("dc_dr_amt"));
		sp.ppstr("dc_cr_amt", varsStr("dc_cr_amt"));
		sp.ppstr("dc_bef_amt", varsStr("dc_bef_amt"));
		sp.ppstr("dc_aft_amt", varsStr("dc_aft_amt"));
		sp.ppstr("dc_bef_d_amt", varsStr("dc_bef_d_amt"));
		sp.ppstr("dc_aft_d_amt", varsStr("dc_aft_d_amt"));
		sp.ppstr("value_type", varsStr("value_type"));
		sp.ppstr("adj_reason_code", varsStr("adj_reason_code"));
		sp.ppstr("adj_comment", varsStr("adj_comment"));
	//sp.ppstr("c_debt_key", varsStr("c_debt_key"));
	//sp.ppstr("debit_item", varsStr("debit_item"));
	//sp.ppstr("adjust_type", varsStr("adjust_type"));//DE32 in Actm0032.java
		sp.ppstr("adjust_type", tAdjType);
		sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
	//sp.ppstr("update_date", wp.sysDate);
		sp.ppstr("update_date", hBusiDate);
		sp.ppstr("update_user", wp.loginUser);
	//sp.addsql(", crt_date ",", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("crt_date", hBusiDate);
		sp.addsql(", crt_time ",", to_char(sysdate,'hh24miss') ");
		sp.ppstr("crt_user", wp.loginUser);
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
		dataCheck();
 		if (rc != 1) return rc; 
		
		kk1 = varsStr("rowid");
		kkk = wp.hexStrToByteArr(kk1);
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_acaj");
		sp.ppstr("dr_amt", varsStr("dr_amt"));
		sp.ppstr("aft_amt", varsStr("aft_amt"));
		sp.ppstr("aft_d_amt", varsStr("aft_d_amt"));
		sp.ppstr("dc_dr_amt", varsStr("dc_dr_amt"));
		sp.ppstr("dc_aft_amt", varsStr("dc_aft_amt"));
		sp.ppstr("dc_aft_d_amt", varsStr("dc_aft_d_amt"));
		sp.ppstr("value_type", varsStr("value_type"));
//		sp.ppss("adj_reason_code", vars_ss("adj_reason_code"));  //畫面不維護此欄位
		sp.ppstr("adj_comment", varsStr("adj_comment"));
	//sp.ppstr("c_debt_key", varsStr("c_debt_key"));
	//sp.ppstr("debit_item", varsStr("debit_item"));
		sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
	//sp.ppstr("update_date", wp.sysDate);
		sp.ppstr("update_date", hBusiDate);
		sp.ppstr("update_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where rowid = ?", kkk);
//		sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.mod_seqno());
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
//		if(rc!=1) return rc;
		
		kk1 = varsStr("rowid");
		strSql = "delete act_acaj "
				+ "where rowid = ? ";
		setRowId(1,kk1);
		rc = sqlExec(strSql);
		
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
