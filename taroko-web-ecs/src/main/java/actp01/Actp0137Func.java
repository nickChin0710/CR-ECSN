/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 111-10-25  v1.00.00  Yang Bo    Sync code from mega                        *
 ******************************************************************************/
package actp01;

import busi.FuncAction;

public class Actp0137Func extends FuncAction {
	String lsAprFlag = "";
	@Override
	public void dataCheck() {
		if(varEq("apr_flag","N")||varEmpty("apr_flag")){
			lsAprFlag = "Y";
		}

	}

	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		dataCheck();
		if(eqIgno(lsAprFlag,"Y")){
			strSql = " update act_pay_rev set "
					 + " apr_flag = 'Y' , "
					 + " apr_user =:apr_user , "
					 + " apr_date =to_char(sysdate,'yyyymmdd') , "
					 + " proc_mark = 'N' ,"
					 + " mod_user =:mod_user , "
					 + " mod_pgm =:mod_pgm , "
					 + " mod_time = sysdate , "
					 + " mod_seqno = nvl(mod_seqno,0)+1 "
					 + " where 1=1 "
					 +commSqlStr.whereRowid(varsStr("rowid"))
					 + " and mod_seqno =:mod_seqno "
					 ;
			setString("apr_user",wp.loginUser);
			setString("mod_user",wp.loginUser);
			setString("mod_pgm",wp.modPgm());
			var2ParmNum("mod_seqno");
		}	else	{
			strSql = " update act_pay_rev set "
					 + " apr_flag = '' , "
					 + " apr_user = '' , "
					 + " apr_date = '' , "
					 + " proc_mark = '' ,"
					 + " mod_user =:mod_user , "
					 + " mod_pgm =:mod_pgm , "
					 + " mod_time = sysdate , "
					 + " mod_seqno = nvl(mod_seqno,0)+1 "
					 + " where 1=1 "
					 +commSqlStr.whereRowid(varsStr("rowid"))
					 + " and mod_seqno =:mod_seqno "
					 ;
			setString("mod_user",wp.loginUser);
			setString("mod_pgm",wp.modPgm());
			var2ParmNum("mod_seqno");
		}
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_pay_rev error !");
		}
		
		return rc;
	}

}
