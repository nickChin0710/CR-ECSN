/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-07-20  V1.00.04  Simon      取消銷帳鍵值、借方科目                     *
* 112-12-18  V1.00.05  Simon      1.各科目調整設定獨立調整類別               *
*                                 2.新增減免分期付款利息調整類別             *
*                                 3.act_acaj.crt_date, update_date 以        *
*                                   business_date 取代 sysDate               *
******************************************************************************/

package actm01;

import java.math.BigDecimal;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm0030Func extends FuncEdit {
	CommString commString = new CommString();
	String hSql = "", hJob1 = "", hJob2 = "", hBusiDate="";
	byte[] kkRowid = null;
	String kkTable = "";
	String adjtype = "";
	String debititem = "";

	public Actm0030Func(TarokoCommon wr) {
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
		hSql = "Select usr_deptno, gl_code from sec_user, ptr_dept_code where usr_deptno = dept_code "
			+ " and usr_id = :userid ";
		setString("userid", wp.loginUser);
		sqlSelect(hSql);
		if (sqlRowNum > 0) {
      hJob1 = commString.mid(colStr("usr_deptno"), 0,2);
			hJob2 = empty(colStr("gl_code")) ? "0" : "0" + colStr("gl_code").substring(0, 1);
		}else {
			errmsg("無法取得使用者之 [起帳部門代碼]!");
			return;
		}

		hSql = "Select business_date from ptr_businday fetch first 1 rows only ";
		sqlSelect(hSql);
		if (sqlRowNum > 0) {
      hBusiDate = colStr("business_date");
		}else {
			errmsg("無法取得系統營業日!");
			return;
		}

		// check PK
		kkRowid = wp.itemRowId("rowid");
		kkTable = wp.itemStr2("db_table");
		
		if(wp.itemEq("apr_flag", "Y")){
			errmsg("此筆已放行不可再調整!!");
			return ;
		}
		
		if (eqIgno(kkTable,"acaj")) {
			//-other modify-
			sqlWhere = "where rowid = ? "
					  + "and nvl(mod_seqno,0) = ? ";
			Object[] param = new Object[] { kkRowid, wp.modSeqno() };
			isOtherModify("act_acaj", sqlWhere, param);
		}

	//debititem = wp.item_ss("debit_item");//maincontrol 會同時搬到 inputhash & outputhash
	//debititem = wp.colStr("debit_item");
		
		String val1 = wp.itemStr2("acct_code");
	//String val2 = wp.itemStr2("bill_type").substring(1,2);
		String val2 = wp.itemStr2("bill_type");
/***
		if(val1.equals("ID")) {
			if(val2.equals("1")) {
				adjtype = "DE01";
			}else if(val2.equals("2")) {
				adjtype = "DE04";
			}else {
				adjtype = "DE07";
			}
		}else {
			if(val1.equals("DB") || val1.equals("CB")) {
				debititem = "55030700";
			}
		}
***/		
		if(val1.equals("BL")) { 
			adjtype = "DE01";
		}else if(val1.equals("CA")) { 
			adjtype = "DE02";
		}else if(val1.equals("IT")) { 
			adjtype = "DE03";
		}else if(val1.equals("ID")) { 
			adjtype = "DE04";
		}else if(val1.equals("AO")) { 
			adjtype = "DE05";
		}else if(val1.equals("OT")) { 
			adjtype = "DE06";
		}else if(val1.equals("CB")) { 
			adjtype = "DE07";
		}else if(val1.equals("DB")) { 
			adjtype = "DE08";
		}else if(val1.equals("PF")) { 
			adjtype = "DE09";
		}else if(val1.equals("CF")) { 
			adjtype = "DE19";
		}else if(val1.equals("CC")) { 
			adjtype = "DE29";
		}else if(val1.equals("SF")) { 
			adjtype = "DE39";
		}else if(val1.equals("AF")) {  
			adjtype = "DE10";
		}else if(val1.equals("LF")) {  
			adjtype = "DE20";
		}else if(val1.equals("RI")) { 
		  if(val2.equals("OSSG")) {
  			adjtype = "DE13";//減免循環息
		  } else {
  			adjtype = "DE23";//減免分期付款利息
		  }
		}else if(val1.equals("CI")) {
			adjtype = "DE33";
		}else if(val1.equals("AI")) {
			adjtype = "DE43";
		}else if(val1.equals("PN")) {
			adjtype = "DE14";
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
		
		//form phopho//
		double idcdramt = wp.itemNum("dc_dr_amt");
		double idcbefamt = wp.itemNum("dc_bef_amt");
		double idcbefdamt = wp.itemNum("dc_bef_d_amt");
		
		double itdramt = 0;
		double itbefamt = wp.itemNum("bef_amt");
		double itbefdamt = wp.itemNum("bef_d_amt");
		if(idcbefdamt != 0) {
		//itdramt = (itbefdamt / idcbefdamt) * idcdramt;
			itdramt = Math.round( (itbefdamt / idcbefdamt) * idcdramt );
		}
		
		sp.ppstr("adjust_type", adjtype);
		sp.ppstr("job_code", hJob1);
		sp.ppstr("vouch_job_code",  hJob2);
		sp.ppstr("debit_item", debititem);
		sp.ppnum("dc_aft_amt", sub(idcbefamt , idcdramt));
		sp.ppnum("dc_aft_d_amt", sub(idcbefdamt , idcdramt));

		sp.ppnum("dr_amt", itdramt);
		sp.ppnum("aft_amt", sub(itbefamt , itdramt));
		sp.ppnum("aft_d_amt", sub(itbefdamt , itdramt));

		sp.ppnum("dc_dr_amt", wp.itemNum("dc_dr_amt"));
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
		sp.ppnum("dc_cr_amt", wp.itemNum("dc_cr_amt"));
		sp.ppnum("dc_bef_amt", wp.itemNum("dc_bef_amt"));
		sp.ppnum("dc_bef_d_amt", wp.itemNum("dc_bef_d_amt"));
		sp.ppstr("value_type", wp.itemStr2("value_type"));
		sp.ppstr("adj_reason_code", wp.itemStr2("adj_reason_code"));
		sp.ppstr("adj_comment", wp.itemStr2("adj_comment"));
	//sp.ppstr("c_debt_key", wp.itemStr2("c_debt_key"));
		sp.ppstr("apr_flag", "N");
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
		//end phopho//

//		//Oris recalculate
//		int idcdramt = Integer.parseInt(wp.item_ss("dc_dr_amt"));
//		int idcbefamt = Integer.parseInt(wp.item_ss("dc_bef_amt"));
//		//int idcaftamt = Integer.parseInt(wp.item_ss("dc_aft_amt"));
//		int idcbefdamt = Integer.parseInt(wp.item_ss("dc_bef_d_amt"));
//		
//		int itdramt = 0;
//		
//		int itbefamt = Integer.parseInt(wp.item_ss("bef_amt"));
//		//int itaftdamt = Integer.parseInt(wp.item_ss("bef_amt"));
//		int itbefdamt = Integer.parseInt(wp.item_ss("bef_d_amt"));
//		if(idcbefdamt != 0) {
//			itdramt = Integer.parseInt(String.valueOf((itbefdamt / idcbefdamt) * idcdramt));
//		}
//		
//		DateFormat df = new SimpleDateFormat("yyyyMMdd");
//		String sysdate = df.format(new Date());
//		sp.ppss("adjust_type", adjtype);
//		sp.ppss("debit_item", debititem);
//		sp.ppss("dc_aft_amt", String.valueOf(idcbefamt - idcdramt));
//		sp.ppss("dc_aft_d_amt", String.valueOf(idcbefdamt - idcdramt));
//
//		sp.ppss("dr_amt", String.valueOf(itdramt));
//		sp.ppss("aft_amt", String.valueOf(itbefamt - itdramt));
//		sp.ppss("aft_d_amt", String.valueOf(itbefdamt - itdramt));
//
//		sp.ppss("dc_dr_amt", empty(wp.item_ss("dc_dr_amt"))?"0":wp.item_ss("dc_dr_amt"));
//
//		sp.ppss("p_seqno", wp.item_ss("p_seqno"));
//		sp.ppss("acct_type", wp.item_ss("acct_type"));
//		sp.ppss("reference_no", wp.item_ss("reference_no"));
//		//sp.ppss("adjust_type", adjtype);
//		//sp.ppss("debit_item", debititem);
//		sp.ppss("post_date", wp.item_ss("post_date"));
//		sp.ppss("orginal_amt", wp.item_ss("orginal_amt"));
//		//sp.ppss("dr_amt", wp.item_ss("dr_amt"));
//		sp.ppss("cr_amt", wp.item_ss("cr_amt"));
//		sp.ppss("bef_amt", wp.item_ss("bef_amt"));
//		//sp.ppss("aft_amt", wp.item_ss("aft_amt"));
//		sp.ppss("bef_d_amt", wp.item_ss("bef_d_amt"));
//		//sp.ppss("aft_d_amt", wp.item_ss("aft_d_amt"));
//		sp.ppss("acct_code", wp.item_ss("acct_code"));
//		sp.ppss("function_code", wp.item_ss("function_code"));
//		sp.ppss("card_no", wp.item_ss("card_no"));
//		sp.ppss("interest_date", wp.item_ss("interest_date"));
//		sp.ppss("curr_code", wp.item_ss("curr_code"));
//		sp.ppss("dc_orginal_amt", wp.item_ss("dc_orginal_amt"));
//		//sp.ppss("dc_dr_amt", isEmpty(wp.item_ss("dc_dr_amt")) ? "0" : wp.item_ss("dc_dr_amt"));
//		sp.ppss("dc_cr_amt", wp.item_ss("dc_cr_amt"));
//		sp.ppss("dc_bef_amt", wp.item_ss("dc_bef_amt"));
//		//sp.ppss("dc_aft_amt", wp.item_ss("dc_aft_amt"));
//		sp.ppss("dc_bef_d_amt", wp.item_ss("dc_bef_d_amt"));
//		//sp.ppss("dc_aft_d_amt", wp.item_ss("dc_aft_d_amt"));
//		sp.ppss("value_type", wp.item_ss("value_type"));
//		sp.ppss("adj_reason_code", wp.item_ss("adj_reason_code"));
//		sp.ppss("adj_comment", wp.item_ss("adj_comment"));
//		sp.ppss("c_debt_key", wp.item_ss("c_debt_key"));
//		//sp.ppss("debit_item", wp.item_ss("debit_item"));
//		sp.addsql(", crt_date ",", to_char(sysdate,'yyyymmdd') ");
//		sp.addsql(", crt_time ",", to_char(sysdate,'hhmiss') ");
//		sp.ppss("crt_user", wp.loginUser);
//		sp.ppss("mod_user", wp.loginUser);
//		sp.ppss("mod_pgm", wp.mod_pgm());
//		sp.addsql(", mod_time ",", sysdate ");
//		sp.ppss("mod_seqno", "1");
		
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
		if (rc != 1) {
			return rc;
		}
		
		if (!eqIgno(kkTable,"acaj")) {
			rc = dbInsert();
		} else {
			SqlPrepare sp = new SqlPrepare();
			sp.sql2Update("act_acaj");
//			lm_dr_amt =dec(data)
//					this.object.dc_aft_amt[1] = DEC(this.object.dc_bef_amt[1]) - lm_dr_amt
//					this.object.dc_aft_d_amt[1] = DEC(this.object.dc_bef_d_amt[1]) - lm_dr_amt
//					//-TWD:比率如何算???-
//					lm_num =dec(this.object.dc_bef_d_amt[1])
//					if lm_num<>0 then
//						lm_dr_amt =round((dec(data) / lm_num) * DEC(this.object.bef_d_amt[1]),0)
//					else
//						lm_dr_amt =0
//					end if
//					this.object.dr_amt[row] =lm_dr_amt
//					this.object.aft_amt[1] = DEC(this.object.bef_amt[1]) - lm_dr_amt
//					this.object.aft_d_amt[1] = DEC(this.object.bef_d_amt[1]) - lm_dr_amt
			
			//form phopho//
			double idcdramt = wp.itemNum("dc_dr_amt");
			double idcbefamt = wp.itemNum("dc_bef_amt");
			double idcbefdamt = wp.itemNum("dc_bef_d_amt");
			
			double itdramt = 0;
			double itbefamt = wp.itemNum("bef_amt");
			double itbefdamt = wp.itemNum("bef_d_amt");
			if(idcbefdamt != 0) {
			//itdramt = (itbefdamt / idcbefdamt) * idcdramt;
			  itdramt = Math.round( (itbefdamt / idcbefdamt) * idcdramt );
			}
			
			sp.ppstr("adjust_type", adjtype);
  	//sp.ppss("job_code", vars_ss("job_code"));
	  //sp.ppss("vouch_job_code", vars_ss("vouch_job_code"));
	  	sp.ppstr("job_code", hJob1);
		  sp.ppstr("vouch_job_code",  hJob2);
			sp.ppstr("debit_item", debititem);
			sp.ppnum("dc_aft_amt", sub(idcbefamt , idcdramt));
			sp.ppnum("dc_aft_d_amt", sub(idcbefdamt , idcdramt));

			sp.ppnum("dr_amt", itdramt);
			sp.ppnum("aft_amt", sub(itbefamt , itdramt));
			sp.ppnum("aft_d_amt", sub(itbefdamt , itdramt));

			sp.ppnum("dc_dr_amt", wp.itemNum("dc_dr_amt"));
			sp.ppstr("value_type", wp.itemStr2("value_type"));
			sp.ppstr("adj_reason_code", wp.itemStr2("adj_reason_code"));
			sp.ppstr("adj_comment", wp.itemStr2("adj_comment"));
		//sp.ppstr("c_debt_key", wp.itemStr2("c_debt_key"));
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
			sp.sql2Where(" where rowid = ?", kkRowid);
			sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
			//end phopho//
			
//			//Oris recalculate
//			int idcdramt = Integer.parseInt(wp.item_ss("dc_dr_amt"));
//			int idcbefamt = Integer.parseInt(wp.item_ss("dc_bef_amt"));
//			//int idcaftamt = Integer.parseInt(wp.item_ss("dc_aft_amt"));
//			int idcbefdamt = Integer.parseInt(wp.item_ss("dc_bef_d_amt"));
//			
//			int itdramt = 0;
//			
//			int itbefamt = Integer.parseInt(wp.item_ss("bef_amt"));
//			//int itaftdamt = Integer.parseInt(wp.item_ss("bef_amt"));
//			int itbefdamt = Integer.parseInt(wp.item_ss("bef_d_amt"));
//			if(idcbefdamt != 0) {
//				itdramt = Integer.parseInt(String.valueOf((itbefdamt / idcbefdamt) * idcdramt));
//			}
//			
//			DateFormat df = new SimpleDateFormat("yyyyMMdd");
//			String sysdate = df.format(new Date());
//
//			sp.ppss("adjust_type", adjtype);
//			sp.ppss("debit_item", debititem);
//			sp.ppss("dc_aft_amt", String.valueOf(idcbefamt - idcdramt));
//			sp.ppss("dc_aft_d_amt", String.valueOf(idcbefdamt - idcdramt));
//
//			sp.ppss("dr_amt", String.valueOf(itdramt));
//			sp.ppss("aft_amt", String.valueOf(itbefamt - itdramt));
//			sp.ppss("aft_d_amt", String.valueOf(itbefdamt - itdramt));
//
////			String aftamt = String.valueOf(Integer.parseInt(wp.item_ss("dc_aft_amt")) - Integer.parseInt(wp.item_ss("dc_dr_amt")));
////			sp.ppss("dc_aft_amt", aftamt);
////			String aftdamt = String.valueOf(Integer.parseInt(wp.item_ss("dc_bef_d_amt")) - Integer.parseInt(wp.item_ss("dc_dr_amt")));
////			sp.ppss("dc_aft_d_amt", aftdamt);
//
//			sp.ppss("dc_dr_amt", empty(wp.item_ss("dc_dr_amt"))?"0":wp.item_ss("dc_dr_amt"));
//			//sp.ppss("dr_amt", empty(wp.item_ss("dc_dr_amt"))?"0":wp.item_ss("dc_dr_amt"));
//			sp.ppss("value_type", wp.item_ss("value_type"));
//			sp.ppss("adj_reason_code", wp.item_ss("adj_reason_code"));
//			sp.ppss("adj_comment", wp.item_ss("adj_comment"));
//			sp.ppss("c_debt_key", wp.item_ss("c_debt_key"));
//			//sp.ppss("debit_item", wp.item_ss("debit_item"));
//			sp.ppss("update_date", sysdate);
//			sp.ppss("update_user", wp.loginUser);
//			sp.ppss("mod_user", wp.loginUser);
//			sp.ppss("mod_pgm", wp.mod_pgm());
//			sp.addsql(", mod_time = sysdate ", "");
//			sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
//			sp.sql2Where(" where hex(rowid) = ?", kk1);
//			sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.mod_seqno());
			
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
		Object[] param = new Object[] { kkRowid, wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	//double 相減:
	double sub(double d1,double d2){    
		BigDecimal bd1 = new BigDecimal(Double.toString(d1));            
		BigDecimal bd2 = new BigDecimal(Double.toString(d2));  
		return bd1.subtract(bd2).doubleValue();  
	}

}
