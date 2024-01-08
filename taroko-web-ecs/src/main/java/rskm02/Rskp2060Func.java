/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskp2060Func extends FuncAction {

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
    deleteDate();
    if (rc != 1)
      return rc;
    strSql = "update rsk_trial_action set " + " apr_flag = 'Y', "
        + " apr_date = to_char(sysdate,'yyyymmdd'), " + " apr_user = :apr_user, "
        + " mod_user = :mod_user," + " mod_time=sysdate," + " mod_pgm =:mod_pgm,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where batch_no =:batch_no"
        + " and risk_group =:risk_group " + " and nvl(apr_flag,'N') <> 'Y' ";;

    this.var2ParmStr("batch_no");
    this.var2ParmStr("risk_group");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "w_rskm2060");


    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int deleteDate() {
    msgOK();
    strSql = "delete rsk_trial_action " + " where batch_no =:batch_no "
        + " and risk_group=:risk_group" + " and  apr_flag = 'Y' ";
    this.var2ParmStr("batch_no");
    this.var2ParmStr("risk_group");
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;

    return rc;
  }

}
