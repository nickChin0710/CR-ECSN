/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-23  V1.00.01  Justin        parameterize sql
* 111-05-25  V1.00.02  Ryan     取消更新 mod_user
******************************************************************************/
package dbbm01;

import busi.FuncAction;
import taroko.base.CommSqlStr;

public class Dbbp0010Func extends FuncAction {

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

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
    msgOK();

    strSql = " update dbb_othexp set " + " apr_flag ='Y' , " + " apr_user =:apr_user , "
//        + " mod_user =:mod_user , " 
    	+ " mod_pgm =:mod_pgm , " + " mod_time = sysdate , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 and rowid = :rowid " ;

    setString("apr_user", wp.loginUser);
//    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
//    setString("rowid", varsStr("rowid"));
    setRowId("rowid", varsStr("rowid"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update dbb_othexp error ");
    }

    return rc;
  }

}
