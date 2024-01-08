/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package rdsm01;

import busi.FuncAction;

public class Rdsm0010Func extends FuncAction {

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
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update cms_roadparm set " + " recv_amt =:recv_amt ," + " stop_days =:stop_days ,"
        + " fstop_days =:fstop_days ," + " lost_days =:lost_days ," + " false_days =:false_days ,"
        + " amt_sum_flag =:amt_sum_flag ," + " chg_user =:chg_user ,"
        + " chg_date =to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " mod_user =:mod_user ,"
        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where rowid =:rowid ";
    item2ParmNum("recv_amt");
    item2ParmNum("stop_days");
    item2ParmNum("fstop_days");
    item2ParmNum("lost_days");
    item2ParmNum("false_days");
    item2ParmNvl("amt_sum_flag", "1");
    setString("chg_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rdsm0010");
    setRowId("rowid", wp.itemStr("rowid"));

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update cms_roadparm error !");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
