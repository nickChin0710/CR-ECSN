/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111/10/28  V1.00.00  Yang Bo        sync code from mega                  *
******************************************************************************/
package cycq01;


import busi.FuncAction;

public class Cycq0020Func extends FuncAction {	
	@Override
	public void dataCheck() {
		// TODO Auto-generated method stub

	}

	@Override
	public int dbInsert() {		
		return rc;
	}

	@Override
	public int dbUpdate() {		
		return rc;
	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		msgOK();
		updateCtiAcmm();
		if(rc!=1)	return rc;
		insertCtiAcmm();		
		return rc;
	}

	int updateCtiAcmm() {
		msgOK();
		
		strSql = " update cti_acmm set "
				 + " proc_flag = '3' , "
				 + " mod_pgm =:mod_pgm ,  "
				 + " mod_time = sysdate , "
				 + " mod_user = :mod_user "
				 + " where p_seqno =:p_seqno "
				 + " and acct_month =:acct_month "
				 + " and from_mark ='07' "
				 + " and print_month =:print_month "
				 + " and uf_nvl(proc_flag,'2') not in ('0','1','2','3') "				 				 
				 ;
		
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		var2ParmStr("p_seqno");
		var2ParmStr("acct_month");
		var2ParmStr("print_month");
		
		sqlExec(strSql);
		if(sqlRowNum<0){
			errmsg("update cti_acmm error ");			
		}	else rc = 1 ;
		
		return rc;
	}
	
	int insertCtiAcmm() {
		msgOK();
		
		strSql = " insert into cti_acmm ( "
				 + " p_seqno ,"
				 + " acct_month ,"
				 + " print_month ,"
				 + " proc_flag ,"
				 + " from_mark ,"
				 + " from_type ,"
				 + " create_date ,"
				 + " acct_type ,"
				 + " id_p_seqno ,"
				 + " mod_pgm ,"
				 + " mod_time ,"
				 + " mod_user "
				 + " ) values ( "
				 + " :p_seqno ,"
				 + " :acct_month ,"
				 + " :print_month ,"
				 + " '0' ,"
				 + " '07' ,"
				 + " '01' ,"
				 + " to_char(sysdate,'yyyymmdd') ,"
				 + " :acct_type ,"
				 + " :id_p_seqno ,"
				 + " :mod_pgm ,"
				 + " sysdate ,"
				 + " :mod_user "
				 + " ) "
				 ;
		
		var2ParmStr("p_seqno");
		var2ParmStr("acct_month");
		var2ParmStr("print_month");
		var2ParmStr("acct_type");
		var2ParmStr("id_p_seqno");
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("insert cti_acmm error ");
		}
		
		return rc;
	}
	
}
