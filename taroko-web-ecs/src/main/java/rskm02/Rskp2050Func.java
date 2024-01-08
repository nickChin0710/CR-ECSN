/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2018-0731:           JH      modify      期中覆審-風險族群評估參數主管覆核 V.2018-0731
* 109-04-27  V1.00.01  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskp2050Func extends FuncAction {

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
    strSql = "update rsk_trial_parm2 set " + " apr_flag = 'Y', "
        + " apr_date = to_char(sysdate,'yyyymmdd'), " + " apr_user = :apr_user, "
        + " mod_user = :mod_user," + " mod_time=sysdate," + " mod_pgm =:mod_pgm,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where risk_group =:risk_group"
        + " and nvl(apr_flag,'N') <> 'Y' ";;

    this.var2ParmStr("risk_group");
    setString("apr_user", modUser);
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);


    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int deleteDate() {
    strSql =
        "delete rsk_trial_parm2 " + " where risk_group =:risk_group " + " and  apr_flag = 'Y' ";
    this.var2ParmStr("risk_group");
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;

    return rc;
  }
}
