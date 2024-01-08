
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import busi.FuncAction;

public class Secp2062Func extends FuncAction {

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
    msgOK();
    dataCheck();
    if (rc != 1)
      return rc;

    String[] laRowid = wp.itemBuff("rowid");
    strSql = " delete sec_authority_log where rowid =? and apr_flag<>'Y'";

    for (int ii = 0; ii < laRowid.length; ii++) {
      setString2(1, laRowid[ii]);
      sqlExec(strSql);
      if (sqlRowNum < 0) {
        errmsg("delete sec_authority_log error No[%s]", ii + 1);
      }
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int insertLog() {
    msgOK();

    strSql = " insert into sec_authority_log ( " + " group_id ," + " user_level ," + " wf_winid ,"
        + " apr_flag ," + " aut_query ," + " aut_update ," + " aut_approve ," + " aut_print ,"
        + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ," + " mod_audcode ,"
        + " mod_time ," + " mod_user ," + " mod_pgm " + " ) values ( " + " :group_id ,"
        + " :user_level ," + " :wf_winid ," + " 'N' ," + " :aut_query ," + " :aut_update ,"
        + " :aut_approve ," + " 'N' ," + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " '' ,"
        + " '' ," + " :mod_audcode ," + " sysdate ," + " :mod_user ," + " :mod_pgm " + " ) ";

    var2ParmStr("group_id");
    var2ParmStr("user_level");
    var2ParmStr("wf_winid");
    var2ParmNvl("aut_query", "N");
    var2ParmNvl("aut_update", "N");
    var2ParmNvl("aut_approve", "N");
    // var2Parm_nvl("aut_print","N");
    var2ParmStr("mod_audcode");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert sec_authority_log error ");
    }

    return rc;
  }

  public int updateLog() {
    msgOK();

    strSql = " update sec_authority_log set " + " aut_query =:aut_query , "
        + " aut_update =:aut_update , " + " aut_approve =:aut_approve , " + " aut_print ='N' , "
        + " mod_audcode = :mod_audcode , " + " mod_user =:mod_user , " + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm " + " where rowid =:rowid ";

    var2ParmNvl("aut_query", "N");
    var2ParmNvl("aut_update", "N");
    var2ParmNvl("aut_approve", "N");
    var2ParmStr("mod_audcode");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setRowId2("rowid", varsStr("rowid"));

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update sec_authority_log error ");
    }

    return rc;
  }

  public int deleteLog() {
    msgOK();
    strSql = "delete sec_authority_log" + " where rowid =? and apr_flag<>'Y'";

    setRowId2(1, varsStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_authority_log error");
    }
    return rc;
  }

}
