/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-10  V1.00.00  Andy Liu   program initial                            *
* 107-08-24  V1.00.01  Alex       bug fixed                                  *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
* 111-11-17  V1.00.03  Simon      remove column seq_no from act_acag_curr & act_acag*
******************************************************************************/

package actp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Actp0020Func extends FuncEdit {
	String mKkMchtNo = "";

	public Actp0020Func(TarokoCommon wr) {
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
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		strSql = "";
		// -set ?value-
		Object[] param = new Object[] {};
//		Object[] param1 = param;
//		System.out.println("is_sql:" + is_sql);
//		for (int i = 0; i <= param1.length; i++) {
//			System.out.println(param1[i] + ",");
//		}
		sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}

		return rc;
	}

	public int dbInsert(String pSql, Object[] pParam) throws Exception {
		actionInit("A");
		if (rc != 1) {
			return rc;
		}

		strSql = pSql;

		Object[] param = pParam;

		sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
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

		strSql = "";

		Object[] param = new Object[] {};

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int dbUpdate(String pSQL, Object[] pParam) throws Exception {
		actionInit("U");
		if (rc != 1) {
			return rc;
		}

		strSql = pSQL;

		Object[] param = pParam;

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		return rc;
	}

	public int dbDelete(String pSQL, Object[] pParam) throws Exception {
		actionInit("D");

		strSql = pSQL;

		Object[] param = pParam;

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int deleteAcagCurr() throws Exception{
		msgOK();
		
		strSql = " delete act_acag_curr where p_seqno =:p_seqno ";
		
		var2ParmStr("p_seqno");
		sqlExec(strSql);
		
		if(sqlRowNum<0){
			errmsg("delete act_acag_curr error");
		}	else	rc=1;
		
		return rc;
	}
	
	public int deleteAcag() throws Exception{
		msgOK();
		
		strSql = " delete act_acag where p_seqno =:p_seqno ";
		var2ParmStr("p_seqno");
		
		sqlExec(strSql);
		
		if(sqlRowNum<0){
			errmsg("delete ACT_ACAG error");
		}	else rc=1;
		return rc;
	}
	
	public int insertAcagCurr() throws Exception{
		msgOK();
		
		strSql = " insert into act_acag_curr ("
				 + " p_seqno ,"
				 + " curr_code ,"
				 + " acct_type ,"
			 //+ " seq_no ,"
				 + " acct_month ,"
				 + " stmt_cycle ,"
				 + " pay_amt ,"
				 + " dc_pay_amt ,"
				 + " mod_user ,"
				 + " mod_time ,"
				 + " mod_pgm ,"
				 + " mod_seqno "
				 + " ) values ( "
				 + " :p_seqno ,"
				 + " :curr_code ,"
				 + " :acct_type ,"
			 //+ " :seq_no ,"
				 + " :acct_month ,"
				 + " :stmt_cycle ,"
				 + " :pay_amt ,"
				 + " :dc_pay_amt ,"
				 + " :mod_user ,"
				 + " sysdate ,"
				 + " :mod_pgm ,"
				 + " 1 "
				 + " ) "
				 ;
		
		var2ParmStr("p_seqno");
		var2ParmStr("curr_code");
		var2ParmStr("acct_type");
	//var2ParmNum("seq_no");
		var2ParmStr("acct_month");
		var2ParmStr("stmt_cycle");
		var2ParmNum("pay_amt");
		var2ParmNum("dc_pay_amt");
		var2ParmStr("mod_user");
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("insert ACT_ACAG_CURR error  acct_month:"+varsStr("acct_month")+"; curr_code:"+varsStr("curr_code"));			
		}
		
		return rc;
	}
	
	public int insertAcag() throws Exception{
		msgOK();
		strSql = " insert into act_acag ("
				 + " p_seqno , "
			 //+ " seq_no , "
				 + " acct_type , "
				 + " acct_month , "
				 + " stmt_cycle , "
				 + " pay_amt , "
				 + " mod_user , "
				 + " mod_time , "
				 + " mod_pgm , "
				 + " mod_seqno "
				 + " ) values ( "
				 + " :p_seqno , "
			 //+ " :seq_no , "
				 + " :acct_type , "
				 + " :acct_month , "
				 + " :stmt_cycle , "
				 + " :pay_amt , "
				 + " :mod_user , "
				 + " sysdate , "
				 + " :mod_pgm , "
				 + " 1 "
				 + " ) "
				 ;
		
		var2ParmStr("p_seqno");
	//var2ParmNum("seq_no");
		var2ParmStr("acct_type");
		var2ParmStr("acct_month");
		var2ParmStr("stmt_cycle");
		var2ParmNum("pay_amt");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("insert ACT_ACAG error");
		}
		
		return rc;
	}
	
	public int insertMRK() throws Exception{
		msgOK();
		strSql = " insert into act_acct_mrk ( "
				 + " acct_type , "
			   + " p_seqno , "
				 + " corp_p_seqno , "
				 + " corp_no , "
				 + " id_p_seqno , "
				 + " mod_audcode , "
				 + " min_pay_bal , "
				 + " m_code , "
				 + " mod_pgm , "
				 + " mod_user , "
				 + " mod_time "				 
				 + " ) values ( "
			   + " :acct_type , "
			   + " :acct_p_seqno , "
				 + " :corp_p_seqno , "
				 + " :corp_no , "
				 + " :id_p_seqno , "
				 + " :mod_audcode , "
				 + " :min_pay_bal , "
				 + " :m_code , "
				 + " :mod_pgm , "
				 + " :mod_user , "
			 //+ " sysdate  "				 
				 + " to_char(sysdate,'yyyymmdd')||to_char(sysdate,'hh24miss')  "				 
				 + " ) "
				 ;
		
		var2ParmStr("acct_type");
		var2ParmStr("acct_p_seqno");
		var2ParmStr("corp_p_seqno");
		var2ParmStr("corp_no");
		var2ParmStr("id_p_seqno");
		var2ParmStr("mod_audcode");
		var2ParmNum("min_pay_bal");
		var2ParmStr("m_code");
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("insert act_acct_mrk error");
		}
		return rc;
	}
	
	public int updateActAcct() throws Exception{
		msgOK();
		
		strSql = " update act_acct set "
				 + " min_pay_bal =:min_pay_bal , "
				 + " mod_user =:mod_user , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_time = sysdate , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "
				 ;
		
		var2ParmNum("min_pay_bal");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		var2ParmStr("p_seqno");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error !");
		}
		
		return rc;
	}
	
	public int updateActAcctCurr() throws Exception{
		msgOK();
		
		strSql = " update act_acct_curr set "
				 + " min_pay_bal =:min_pay_bal , "
				 + " dc_min_pay_bal =:dc_min_pay_bal , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "
				 + " and curr_code =:curr_code "
				 ;
		
		var2ParmNum("min_pay_bal");
		var2ParmNum("dc_min_pay_bal");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		var2ParmStr("p_seqno");
		var2ParmStr("curr_code");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update ACT_ACCT_CURR error; curr="+varsStr("curr_code"));
		}
		
		return rc;
	}
	
	public int deleteTmp() throws Exception{
		msgOK();
		strSql = "delete act_moddata_tmp where p_seqno =:p_seqno and act_modtype ='01' ";
		var2ParmStr("p_seqno");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("delete act_moddata_tmp error ");
		}
		
		return rc;
	}
	
}
