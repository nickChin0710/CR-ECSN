package actm01;
/**
 * 2022-0428	JH		++OT adjust_type=DR06
 * 2018-0809  JH		bugfix
 * 106-12-20  V1.00.00  Oris Chang program initial
 * 111-10-21  V1.00.03  Machao     sync from mega & updated for project coding standard*
 * 112-07-22  V1.00.04  Simon      取消銷帳鍵值、借方科目                     *
 * 112-12-18  V1.00.05  Simon      1.各科目獨立調整類別                       *
 *                                 2.分期付款利息調整還原設定獨立調整類別     *
 *                                 3.act_acaj.crt_date, update_date 以        *
 *                                   business_date 取代 sysDate               *
 */

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0120Func extends FuncEdit {

	String kk1 = "";
	byte[] kkk = null;
	String hSql = "", hBusiDate="";

	public Actm0120Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
		printVersion("v22.0428");
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
		kkk = wp.itemRowId("rowid");

		//-other modify-
//		sql_where = "where hex(rowid) = ? "
//				  + "and nvl(mod_seqno,0) = ? ";
//		Object[] param = new Object[] { kk1, wp.mod_seqno() };
		if (!this.isAdd()){
		  sqlWhere = "where rowid = ? "
			     	   + "and nvl(mod_seqno,0) = ? ";
		  Object[] param = new Object[] { kkk, wp.modSeqno() };
		  isOtherModify("act_acaj", sqlWhere, param);
	  }

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
		if(rc!=1) return rc;
		
		String adjtype = "";
		String val1 = wp.itemStr2("acct_code");
	//String val2 = wp.item_ss("bill_type").substring(0,1);
  //String val2 = wp.itemStr2("bill_type").substring(1,2);
		String val2 = wp.itemStr2("bill_type");
/***
		if(val1.equals("ID")) {
			if(val2.equals("1")) {
				adjtype = "DR01";
			}else if(val2.equals("2")) {
				adjtype = "DR03";
			}else {
				adjtype = "DR05";
			}
		}
		if(val1.equals("BL") 
				|| val1.equals("CB")
				|| val1.equals("CA")
				|| val1.equals("IT")
				|| val1.equals("AO")
				|| val1.equals("DB")
				|| val1.equals("OT")) {
			adjtype = "DR06";
		}else if(val1.equals("AF") 
				|| val1.equals("LF")
				|| val1.equals("CF")
				|| val1.equals("PF")
				|| val1.equals("SF")
				|| val1.equals("CC")) {
			adjtype = "DR07";
		}else if(val1.equals("RI") 
				|| val1.equals("AI")
				|| val1.equals("CI")) {
			adjtype = "DR08";
		}else if(val1.equals("PN")) {
			adjtype = "DR09";
		}	
***/		
		if(val1.equals("BL")) { 
			adjtype = "DR01";
		}else if(val1.equals("CA")) { 
			adjtype = "DR02";
		}else if(val1.equals("IT")) { 
			adjtype = "DR03";
		}else if(val1.equals("ID")) { 
			adjtype = "DR04";
		}else if(val1.equals("AO")) { 
			adjtype = "DR05";
		}else if(val1.equals("OT")) { 
			adjtype = "DR06";
		}else if(val1.equals("CB")) { 
			adjtype = "DR07";
		}else if(val1.equals("DB")) { 
			adjtype = "DR08";
		}else if(val1.equals("PF")) { 
			adjtype = "DR09";
		}else if(val1.equals("CF")) { 
			adjtype = "DR19";
		}else if(val1.equals("CC")) { 
			adjtype = "DR29";
		}else if(val1.equals("SF")) { 
			adjtype = "DR39";
		}else if(val1.equals("AF")) {  
			adjtype = "DR10";
		}else if(val1.equals("LF")) {  
			adjtype = "DR20";
		}else if(val1.equals("RI")) { 
		  if(val2.equals("OSSG")) {
  			adjtype = "DR13";//減免循環息
		  } else {
  			adjtype = "DR23";//減免分期付款利息
		  }
		}else if(val1.equals("CI")) {
			adjtype = "DR33";
		}else if(val1.equals("AI")) {
			adjtype = "DR43";
		}else if(val1.equals("PN")) {
			adjtype = "DR14";
		}		

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_acaj");
//    	sp.ppnum("dr_amt", vars_num("dr_amt"));
//    	sp.ppnum("dc_aft_amt", vars_num("dc_aft_amt"));
//    	sp.ppnum("dc_aft_d_amt", vars_num("dc_aft_d_amt"));
//    	sp.ppnum("aft_amt", vars_num("aft_amt"));
//    	sp.ppnum("aft_d_amt", vars_num("aft_d_amt"));
    	sp.ppnum("dr_amt", wp.itemNum("dr_amt"));
    	sp.ppnum("dc_aft_amt", wp.itemNum("dc_aft_amt"));
    	sp.ppnum("dc_aft_d_amt", wp.itemNum("dc_aft_d_amt"));
    	sp.ppnum("aft_amt", wp.itemNum("aft_amt"));
    	sp.ppnum("aft_d_amt", wp.itemNum("aft_d_amt"));
    	
    	sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
		
		sp.ppstr("p_seqno", wp.itemStr2("p_seqno"));
		sp.ppstr("acct_type", wp.itemStr2("acct_type"));
		sp.ppstr("reference_no", wp.itemStr2("reference_no"));
		sp.ppstr("post_date", wp.itemStr2("post_date"));
		sp.ppnum("orginal_amt", wp.itemNum("orginal_amt"));
		sp.ppnum("cr_amt", wp.itemNum("cr_amt"));
		sp.ppnum("bef_amt", wp.itemNum("bef_amt"));
		sp.ppnum("bef_d_amt", wp.itemNum("bef_d_amt"));
		sp.ppstr("acct_code", wp.itemStr2("acct_code"));
		sp.ppstr("function_code", wp.itemStr2("function_code"));
		sp.ppstr("card_no", wp.itemStr2("card_no"));
		sp.ppstr("interest_date", wp.itemStr2("interest_date"));
		sp.ppstr("curr_code", wp.itemStr2("curr_code"));
		sp.ppnum("dc_orginal_amt", wp.itemNum("dc_orginal_amt"));
		sp.ppnum("dc_dr_amt", wp.itemNum("dc_dr_amt"));
		sp.ppnum("dc_cr_amt", wp.itemNum("dc_cr_amt"));
		sp.ppnum("dc_bef_amt", wp.itemNum("dc_bef_amt"));
		sp.ppnum("dc_bef_d_amt", wp.itemNum("dc_bef_d_amt"));
		sp.ppstr("adjust_type", adjtype);
		sp.ppstr("apr_flag", "N");
		sp.ppstr("value_type", wp.itemStr2("value_type"));
		sp.ppstr("adj_reason_code", wp.itemStr2("adj_reason_code"));
		sp.ppstr("adj_comment", wp.itemStr2("adj_comment"));
	//sp.ppstr("c_debt_key", wp.itemStr2("c_debt_key"));
	//sp.ppstr("debit_item", wp.itemStr2("debit_item"));
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
		kk1 = wp.itemStr2("rowid");
		
		if (empty(kk1)) {
			rc = dbInsert();
		} else {
			actionInit("U");
			dataCheck();
			if (rc != 1) return rc;
			
			SqlPrepare sp = new SqlPrepare();
			sp.sql2Update("act_acaj");
			sp.ppstr("value_type", wp.itemStr2("value_type"));
			sp.ppstr("adj_reason_code", wp.itemStr2("adj_reason_code"));
			sp.ppstr("adj_comment", wp.itemStr2("adj_comment"));
		//sp.ppstr("c_debt_key", wp.itemStr2("c_debt_key"));
		//sp.ppstr("debit_item", wp.itemStr2("debit_item"));
		//sp.ppstr("update_date", wp.sysDate);
			sp.ppstr("update_date", hBusiDate);
			sp.ppstr("update_user", wp.loginUser);
    //sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
	  //sp.addsql(", crt_time = to_char(sysdate,'hh24miss') ", "");
		//sp.ppstr("crt_user", wp.loginUser);
			sp.ppstr("mod_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.addsql(", mod_time = sysdate ", "");
			sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
			sp.sql2Where(" where rowid = ?", kkk);
			sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
			rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
			if (sqlRowNum == 0) {
				rc = -1;
			}
		}
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1) return rc;
		
		strSql = "delete act_acaj "
				+ sqlWhere;
		Object[] param = new Object[] { kkk, wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
//	public int update_acct_jrnl(String as_rev_flag) {
//		msgOK();
//		
////		kk1 = wp.item_ss("jrnl_rowid");
//		kkk = wp.item_RowId("jrnl_rowid");
//		busi.sqlPrepare sp = new sqlPrepare();
//		sp.sql2Update("act_jrnl");
//		sp.ppss("reversal_flag", as_rev_flag);
//		sp.ppss("mod_user", wp.loginUser);
//		sp.ppss("mod_pgm", wp.mod_pgm());
//		sp.addsql(", mod_time = sysdate ", "");
//		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
//		sp.sql2Where(" where rowid = ?", kkk);
//		rc = sqlExec(sp.sql_stmt(), sp.sql_parm());
//		if (sql_nrow == 0) {
//			rc = -1;
//		}
//		
//		return rc;
//	}
	
	public int updateAcctJrnl(String asRevFlag) throws Exception {
		msgOK();
		
		kk1 = wp.itemStr2("jrnl_rowid");
		strSql ="update act_jrnl set"
				+" reversal_flag =?,"
				+commSqlStr.setModxxx(wp.loginUser,wp.modPgm())
				+" where rowid =?";
		setString2(1,asRevFlag);
		this.setRowId(2,kk1);
		sqlExec(strSql);
		if (sqlRowNum == 0) {
			errmsg("update act_jrnl error");
		}
		
		return rc;
	}
	

}
