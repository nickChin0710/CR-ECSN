/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-07-23  V1.00.04  Simon      remove no-use content in dbInsert()、dbUpdate()、dbDelete()*
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0137Func extends FuncEdit {
	String alldata = "";
	
	public void setalldata(String alldata) {
		this.alldata = alldata;
	}


	public Actm0137Func(TarokoCommon wr) {
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
/***
		actionInit("A");
		dataCheck();
***/
		return rc;
	}

	public int dbInsert(SqlPrepare sp) throws Exception {		
/***
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());			

		this.sqlCommit(rc);
***/
		return rc;
	}

	@Override
	public int dbUpdate() {
/***
		actionInit("U");
		dataCheck();
		if(rc!=1) return rc;
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_pay_detail");
		sp.ppstr("pay_card_no", varsStr("pay_card_no"));
		sp.ppstr("payment_no", varsStr("payment_no"));
		sp.ppstr("acct_type", varsStr("acct_type"));
		sp.ppstr("pay_amt", varsStr("pay_amt"));
		sp.ppstr("pay_date", varsStr("pay_date"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where hex(rowid) = ?", varsStr("rowid"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
***/
		return rc;
	}

	@Override
	public int dbDelete() {
/***
		actionInit("D");
		dataCheck();
		if(rc!=1) return rc;
		
		strSql = "delete act_pay_detail where hex(act_pay_detail.rowid) = ? ";

		Object[] param = new Object[] { varsStr("rowid") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
***/	
		return rc;
	}
	
	public int updateJrnlTab1() throws Exception{
		msgOK();
		strSql = " update act_jrnl set "
				 + " payment_rev_amt =:payment_rev_amt , "
				 + " reversal_flag ='Y' , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where mod_seqno =:mod_seqno "
				 +commSqlStr.whereRowid(varsStr("rowid"));
				 ;
				 
		var2ParmNum("payment_rev_amt");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		var2ParmNum("mod_seqno");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_jrnl error");
		}
				 
		return rc;
	}
	
	public int deleteAcaj() throws Exception{
		msgOK();
		strSql = "delete act_acaj where 1=1 "+commSqlStr.whereRowid(varsStr("acaj_rowid"));
		
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("delete act_acaj error !");			
		}
		
		return rc;
	}
	
	public int insertActAcaj() throws Exception{
		msgOK();
		
		strSql = " insert into act_acaj ( "
				 + " crt_date ,"
				 + " crt_time ,"
				 + " crt_user ,"
				 + " p_seqno ,"
				 + " acct_type ,"
				 + " adjust_type ,"
				 + " reference_no ,"
				 + " post_date ,"
				 + " acct_code ,"
				 + " job_code ,"
				 + " vouch_job_code ,"
				 + " update_date ,"
				 + " update_user ,"
				 + " debit_item ,"
				 + " adj_reason_code ,"
				 + " adj_comment ,"
				 + " c_debt_key ,"
				 + " value_type ,"
				 + " dc_orginal_amt ,"
				 + " dc_cr_amt ,"
				 + " dc_bef_amt ,"
				 + " dc_aft_amt ,"
				 + " dc_bef_d_amt ,"
				 + " dc_aft_d_amt ,"
				 + " curr_code ,"
				 + " interest_date ,"
				 + " function_code ,"
				 + " apr_flag ,"
				 + " orginal_amt ,"
				 + " cr_amt ,"
				 + " bef_amt ,"
				 + " aft_amt ,"
				 + " bef_d_amt ,"
				 + " aft_d_amt , "
				 + " mod_user , "
				 + " mod_pgm , "
				 + " mod_time , "
				 + " mod_seqno "
				 + " ) values ( "
				 + " to_char(sysdate,'yyyymmdd') ,"
				 + " to_char(sysdate,'hh24miss') ,"
				 + " :crt_user ,"
				 + " :p_seqno ,"
				 + " :acct_type ,"
				 + " 'DR11' ,"
				 + " :reference_no ,"
				 + " :post_date ,"
				 + " :acct_code ,"
				 + " :job_code ,"
				 + " :vouch_job_code ,"
				 + " to_char(sysdate,'yyyymmdd') ,"
				 + " :update_user ,"
				 + " :debit_item ,"
				 + " :adj_reason_code ,"
				 + " :adj_comment ,"
				 + " :c_debt_key ,"
				 + " :value_type ,"
				 + " :dc_orginal_amt ,"
				 + " :dc_cr_amt ,"
				 + " :dc_bef_amt ,"
				 + " :dc_aft_amt ,"
				 + " :dc_bef_d_amt ,"
				 + " :dc_aft_d_amt ,"
				 + " :curr_code ,"
				 + " :interest_date ,"
				 + " 'U' ,"
				 + " 'N' ,"
				 + " :orginal_amt ,"
				 + " :cr_amt ,"
				 + " :bef_amt ,"
				 + " :aft_amt ,"
				 + " :bef_d_amt ,"
				 + " :aft_d_amt , "
				 + " :mod_user , "
				 + " :mod_pgm , "
				 + " sysdate , "
				 + " 1 "
				 + " ) "
				 ;
		
		var2ParmStr("p_seqno");
		var2ParmStr("acct_type");
		var2ParmStr("reference_no");
		var2ParmStr("post_date");
		var2ParmStr("acct_code");
		var2ParmStr("job_code");
		var2ParmStr("vouch_job_code");
		setString("update_user",wp.loginUser);
		var2ParmStr("debit_item");
		var2ParmStr("adj_reason_code");
		var2ParmStr("adj_comment");
		var2ParmStr("c_debt_key");
		var2ParmStr("value_type");
		var2ParmNum("dc_orginal_amt");
		var2ParmNum("dc_cr_amt");
		var2ParmNum("dc_bef_amt");
		var2ParmNum("dc_aft_amt");
		var2ParmNum("dc_bef_d_amt");
		var2ParmNum("dc_aft_d_amt");
		var2ParmStr("curr_code");
		var2ParmStr("interest_date");
		var2ParmNum("orginal_amt");
		var2ParmNum("cr_amt");
		var2ParmNum("bef_amt");
		var2ParmNum("aft_amt");
		var2ParmNum("bef_d_amt");
		var2ParmNum("aft_d_amt");
		setString("crt_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("insert act_acaj error !");
		}
		
		return rc ;
	}
	
	public int deletePayrev() throws Exception{
		msgOK();
		strSql = "delete act_pay_rev where jrnl_seqno =:jrnl_seqno ";
		var2ParmStr("jrnl_seqno");
		sqlExec(strSql);
		
		if(sqlRowNum<0){
			errmsg("delete act_pay_rev error !");
		}	else rc=1;
		return rc;
	}
	
	public int insertPayrev() throws Exception{
		msgOK();
		
		strSql = " insert into act_pay_rev ( "
				 + " jrnl_seqno ,"
				 + " enq_seqno ,"
				 + " p_seqno ,"
				 + " acct_type ,"
				 + " acct_date ,"
				 + " tran_type ,"
				 + " curr_code ,"
				 + " transaction_amt ,"
				 + " dc_transaction_amt ,"
				 + " payment_rev_amt ,"
				 + " this_reversal_amt ,"
				 + " interest_date ,"
				 + " adj_reason_code ,"
				 + " adj_comment ,"
				 + " c_debt_key ,"
				 + " cr_item ,"
				 + " job_code ,"
				 + " vouch_job_code ,"
				 + " value_type ,"
				 + " crt_user ,"
				 + " crt_date ,"
				 + " crt_time ,"
				 + " mod_user ,"
				 + " mod_pgm , "
				 + " mod_time ,"
				 + " mod_seqno "
				 + " ) values ( "
				 + " :jrnl_seqno ,"
				 + " :enq_seqno ,"
				 + " :p_seqno ,"
				 + " :acct_type ,"
				 + " :acct_date ,"
				 + " :tran_type ,"
				 + " :curr_code ,"
				 + " :transaction_amt ,"
				 + " :dc_transaction_amt ,"
				 + " :payment_rev_amt ,"
				 + " :this_reversal_amt ,"
				 + " :interest_date ,"
				 + " :adj_reason_code ,"
				 + " :adj_comment ,"
				 + " :c_debt_key ,"
				 + " :cr_item ,"
				 + " :job_code ,"
				 + " :vouch_job_code ,"
				 + " :value_type ,"
				 + " :crt_user ,"
				 + " to_char(sysdate,'yyyymmdd') ,"
				 + " to_char(sysdate,'hh24miss') ,"
				 + " :mod_user ,"
				 + " :mod_pgm , "
				 + " sysdate ,"
				 + " 1 "
				 + " ) "
				 ;
		
		var2ParmStr("jrnl_seqno");
		var2ParmStr("enq_seqno");
		var2ParmStr("p_seqno");
		var2ParmStr("acct_type");
		var2ParmStr("acct_date");
		var2ParmStr("tran_type");
		var2ParmStr("curr_code");
		var2ParmNum("transaction_amt");
		var2ParmNum("dc_transaction_amt");
		var2ParmNum("payment_rev_amt");
		var2ParmNum("this_reversal_amt");
		var2ParmStr("interest_date");
		var2ParmStr("adj_reason_code");
		var2ParmStr("adj_comment");
		var2ParmStr("c_debt_key");
		var2ParmStr("cr_item");
		var2ParmStr("job_code");
		var2ParmStr("vouch_job_code");
		var2ParmStr("value_type");
		setString("crt_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("insert act_pay_rev error !");
		}
		
		return rc;
	}
	
}
