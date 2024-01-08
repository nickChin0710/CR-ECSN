/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-28  V1.00.02  Justin         parameterize sql
******************************************************************************/
package ichm01;

import busi.FuncAction;

public class Ichp0110Func extends FuncAction {

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

    strSql = " update ich_a04b_exception set " + " clo_apr_date = to_char(sysdate,'yyyymmdd') , "
        + " clo_apr_user =:clo_apr_user , " + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 and rowid =x:rowid "
        + " and mod_seqno =:mod_seqno ";

    setString("clo_apr_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("rowid", varsStr("rowid"));
    var2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ich_a04b_exception error !!");
    }

    return rc;
  }

}
