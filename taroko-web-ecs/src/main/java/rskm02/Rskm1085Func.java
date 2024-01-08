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

public class Rskm1085Func extends FuncAction {

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
    insertData();
    return rc;
  }

  public int deleteData() {
    msgOK();
    strSql = " delete rsk_score_level where score_type=:score_type ";
    setString("score_type", wp.itemStr("kk_score_type"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete rsk_score_level error !");
    } else {
      rc = 1;
    }
    return rc;
  }

  public int insertData() {
    msgOK();
    strSql = " insert into rsk_score_level ( " + " score_type ," + " trial_level ,"
        + " trial_score ," + " crt_user ," + " crt_date ," + " apr_flag ," + " apr_date ,"
        + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno "
        + " ) values ( " + " :score_type ," + " :trial_level ," + " :trial_score ," + " :crt_user ,"
        + " to_char(sysdate,'yyyymmdd') ," + " 'Y' ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " )";
    setString("score_type", wp.itemStr("kk_score_type"));
    var2ParmNum("trial_level");
    var2ParmStr("trial_score");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm1085");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_score_level error !");
    }
    return rc;
  }

}
