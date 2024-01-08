/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* ???        V1.00.01  ???        program initial                            *
* 20210113   V1.00.02  Andy       update Mantis5480                          *
* 111-10-25  v1.00.03  Yang Bo    Sync code from mega                        *
*****************************************************************************/
package actp01;

import busi.FuncAction;

public class Actp0120Func extends FuncAction {
	String lsAprFlag = "Y";
	@Override
	public void dataCheck() {
		if(varEq("apr_flag","Y"))	lsAprFlag = "N";
		
		sqlWhere =" where 1=1"
				+commSqlStr.whereRowid(varsStr("rowid"))
				+" and nvl(mod_seqno,0) =?";
		Object[] parms =new Object[]{varsNum("mod_seqno")};
		if (this.isOtherModify("act_acaj", sqlWhere,parms) ) {
			wp.log(sqlWhere,parms);
			return;
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
		if(rc!=1)	return rc;
		
		strSql = " update act_acaj set "
				 + " apr_flag =:apr_flag , "
				 + " apr_date =:apr_date , "
				 + " apr_user =:apr_user , "
				 + " mod_user =:mod_user , "
				 + " mod_time =sysdate , "
				 + " mod_pgm ='actp0120' , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where 1=1 "
				 +commSqlStr.whereRowid(varsStr("rowid"))
				 + " and mod_seqno =:mod_seqno "
				 ;
		setString("apr_flag", lsAprFlag);
		setString("apr_date",wp.sysDate);
		setString("apr_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		var2ParmNum("mod_seqno");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acaj error");			
		}
				
		return rc;
	}

}
