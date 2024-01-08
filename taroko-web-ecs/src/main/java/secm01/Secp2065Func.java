/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import busi.FuncAction;

public class Secp2065Func extends FuncAction {

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
    // TODO Auto-generated method stub
    return 0;
  }

  public int delAuthority() {
    msgOK();
    strSql = "Delete sec_authority " + " where group_id =:group_id "
        + " and user_level =:user_level " + " and wf_winid =:wf_winid ";
    var2ParmStr("group_id");
    var2ParmStr("user_level");
    var2ParmStr("wf_winid");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_authority_log err=" + getMsg());
      rc = -1;
    } else
      rc = 1;
    return rc;
  }

  public int insertAuthority() {
    msgOK();
    strSql = " insert into sec_authority ( " + " wf_winid , " + " group_id , " + " user_level ,"
        + " aut_query , " + " aut_update , " + " aut_approve , " + " aut_print , " + " crt_user , "
        + " crt_date , " + " apr_user , " + " apr_date , " + " mod_pgm , " + " mod_time , "
        + " mod_user , " + " mod_seqno " + " ) values ( " + " :wf_winid , " + " :group_id , "
        + " :user_level ," + " :aut_query , " + " :aut_update , " + " :aut_approve , " + " 'N' , "
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :mod_pgm , " + " sysdate , " + " :mod_user , "
        + " 1 " + " ) ";

    var2ParmStr("wf_winid");
    var2ParmStr("group_id");
    var2ParmStr("user_level");
    var2ParmStr("aut_query");
    var2ParmStr("aut_update");
    var2ParmStr("aut_approve");
    // var2Parm_ss("aut_print");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("mod_user", wp.loginUser);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert sec_authority error !");
    }

    return rc;
  }

  public int insertLog() {
    msgOK();
    strSql = " insert into sec_authority_log ( " + " wf_winid , " + " group_id , "
        + " user_level , " + " aut_query , " + " aut_update , " + " aut_approve , "
        + " aut_print , " + " apr_date , " + " apr_flag , " + " apr_user , " + " crt_user , "
        + " crt_date , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_audcode "
        + " ) values ( " + " :wf_winid , " + " :group_id , " + " :user_level , " + " :aut_query , "
        + " :aut_update , " + " :aut_approve , " + " 'N' , " + " to_char(sysdate,'yyyymmdd') , "
        + " 'Y' , " + " :apr_user , " + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , "
        + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 'A' " + " ) ";

    var2ParmStr("wf_winid");
    var2ParmStr("group_id");
    var2ParmStr("user_level");
    var2ParmStr("aut_query");
    var2ParmStr("aut_update");
    var2ParmStr("aut_approve");
    // var2Parm_ss("aut_print");
    setString("apr_user", wp.loginUser);
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert sec_authority_log error !");
    }

    return rc;
  }

  // public int update_authority(){
  // msgOK();
  // is_sql="update sec_authority set "
  // + " aut_update =:aut_update ,"
  // + " aut_approve =:aut_approve ,"
  // + " aut_print =:aut_print ,"
  // + " apr_date =to_char(sysdate,'yyyymmdd') ,"
  // + " apr_user =:apr_user ,"
  // + " mod_user =:mod_user ,"
  // + " mod_time =sysdate ,"
  // + " mod_pgm =:mod_pgm ,"
  // + " mod_seqno =nvl(mod_seqno,0)+1 "
  // + " where group_id =:group_id"
  // + " and user_level =:user_level"
  // + " and wf_winid =:wf_winid"
  // ;
  // var2Parm_nvl("aut_update","N");
  // var2Parm_nvl("aut_approve","N");
  // var2Parm_nvl("aut_print","N");
  // var2Parm_ss("group_id");
  // var2Parm_ss("user_level");
  // var2Parm_ss("wf_winid");
  // setString("mod_user",wp.loginUser);
  // setString("apr_user",wp.item_ss("approval_user"));
  // setString("mod_pgm","secp2065");
  // this.sqlExec(is_sql);
  // if (sql_nrow <= 0) {
  // errmsg("update sec_authority error, " + getMsg());
  // }
  // return rc;
  // }
  //
  // public int insert_log(){
  // msgOK();
  // is_sql = "insert into sec_authority_log ("
  // + " group_id , "
  // + " user_level , "
  // + " wf_winid , "
  // + " apr_flag ,"
  // + " apr_date ,"
  // + " apr_user , "
  // + " aut_query , "
  // + " aut_update , "
  // + " aut_approve , "
  // + " aut_print , "
  // + " crt_date , "
  // + " crt_user , "
  // + " mod_audcode , "
  // + " mod_time , "
  // + " mod_user , "
  // + " mod_pgm "
  // + " ) values ("
  // + " :group_id , "
  // + " :user_level , "
  // + " :wf_winid , "
  // + " 'Y' ,"
  // + " to_char(sysdate,'yyyymmdd') ,"
  // + " :apr_user , "
  // + " :aut_query , "
  // + " :aut_update , "
  // + " :aut_approve , "
  // + " :aut_print , "
  // + " to_char(sysdate,'yyyymmdd') , "
  // + " :crt_user , "
  // + " :mod_audcode , "
  // + " sysdate , "
  // + " :mod_user , "
  // + " :mod_pgm "
  // + " )";
  // var2Parm_ss("group_id");
  // var2Parm_ss("user_level");
  // var2Parm_ss("wf_winid");
  // var2Parm_nvl("aut_query","N");
  // var2Parm_nvl("aut_update","N");
  // var2Parm_nvl("aut_approve","N");
  // var2Parm_nvl("aut_print","N");
  // setString("apr_user",wp.item_ss("approval_user"));
  // setString("crt_user",wp.loginUser);
  // setString("mod_user",wp.loginUser);
  // setString("mod_pgm","secp2065");
  // if(eq_igno(vars_ss("aut_query"), "Y")){
  // setString("mod_audcode","U");
  // } else {
  // setString("mod_audcode","D");
  // }
  //
  // this.sqlExec(is_sql);
  // if (sql_nrow <= 0) {
  // errmsg("Insert sec_authority_log error, " + getMsg());
  // }
  //
  // return rc;
  // }

}
