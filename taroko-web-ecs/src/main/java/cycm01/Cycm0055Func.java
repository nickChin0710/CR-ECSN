package cycm01;

import busi.FuncAction;

public class Cycm0055Func extends FuncAction {
	String acctType = "" , currCode = "";
	@Override
	public void dataCheck() {
		if(ibAdd) {
			acctType = wp.itemStr("kk_acct_type");
			currCode = wp.itemStr("kk_curr_code");
		}	else	{
			acctType = wp.itemStr("acct_type");
			currCode = wp.itemStr("curr_code");
		}
		
		if(empty(acctType)) {
			errmsg("帳戶類別 : 不可空白");
			return ;
		}
		
		if(empty(currCode)) {
			errmsg("幣別 : 不可空白");
			return ;
		}
		
		if(ibDelete)	return ;
		
		if(wp.itemNum("purch_bal_wave")>0 && wp.itemNum("total_bal")>0) {
			errmsg("「消費本金」參數和「總應繳款」不可同時大於 0 ");
			return ;
		}
				
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1)	return rc;
		
		strSql = " insert into ptr_curr_general ( "
				+ " acct_type ,"
				+ " curr_code ,"
				+ " purch_bal_wave ,"
				+ " total_bal ,"
				+ " min_payment ,"
				+ " crt_date , "
				+ " crt_user , "
				+ " mod_user , "
				+ " mod_time , "
				+ " mod_pgm , "
				+ " mod_seqno "
				+ " ) values ( "
                + " :acct_type ,"
				+ " :curr_code ,"
				+ " :purch_bal_wave ,"
				+ " :total_bal ,"
				+ " :min_payment ,"
				+ " to_char(sysdate,'yyyymmdd') , "
				+ " :crt_user , "
				+ " :mod_user , "
				+ " sysdate , "
				+ " :mod_pgm , "
				+ " 1 "
				+ " ) "
				;
		
		setString("acct_type",acctType);
		setString("curr_code",currCode);
		item2ParmNum("purch_bal_wave");
		item2ParmNum("total_bal");
		item2ParmNum("min_payment");
		setString("crt_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("insert ptr_curr_general error !");			
		}
		
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1)	return rc;
		
		strSql = " update ptr_curr_general set "
				+ " purch_bal_wave =:purch_bal_wave ,"
				+ " total_bal =:total_bal ,"
				+ " min_payment =:min_payment ,"
				+ " mod_user =:mod_user , "
				+ " mod_time = sysdate , "
				+ " mod_pgm = mod_pgm , "
				+ " mod_seqno = nvl(mod_seqno,0)+1 "
				+ " where acct_type = :acct_type "
				+ " and curr_code =:curr_code "
				;
				
		item2ParmNum("purch_bal_wave");
		item2ParmNum("total_bal");
		item2ParmNum("min_payment");		
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		setString("acct_type",acctType);
		setString("curr_code",currCode);
		
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update ptr_curr_general error !");			
		}
		
		return rc;		
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1)	return rc;
		
		strSql = "delete ptr_curr_general where acct_type = :acct_type and curr_code =:curr_code ";
		setString("acct_type",acctType);
		setString("curr_code",currCode);
		
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("delete ptr_curr_general error !");			
		}
		
		return rc;
	}

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

}
