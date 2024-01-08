package secm01;

import busi.FuncAction;

public class Secp2061Func extends FuncAction {

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

    strSql = "delete sec_authority where group_id=:group_id and user_level=:user_level ";
    var2ParmStr("group_id");
    var2ParmStr("user_level");
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete sec_authority error !");
    } else
      rc = 1;

    return rc;
  }

  public int insertAuthority() {
    msgOK();

    strSql = " insert into sec_authority ( " + " group_id , " + " user_level , " + " wf_winid , "
        + " aut_query , " + " aut_update , " + " aut_approve , " + " aut_print , " + " crt_date , "
        + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , " + " mod_time , "
        + " mod_pgm , " + " mod_seqno " + " ) values ( " + " :group_id , " + " :user_level , "
        + " :wf_winid , " + " :aut_query , " + " :aut_update , " + " :aut_approve , "
        + " :aut_print , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " 1 " + " ) ";

    var2ParmStr("group_id");
    var2ParmStr("user_level");
    var2ParmStr("wf_winid");
    var2ParmStr("aut_query");
    var2ParmStr("aut_update");
    var2ParmStr("aut_approve");
    var2ParmStr("aut_print");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "secp2061");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert sec_authority error ! ");
    }

    return rc;
  }

}
