package secm01;

import busi.FuncAction;

public class Secm0030Func extends FuncAction {

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

  public int delGroup() {
    msgOK();
    strSql = "delete sec_group_win where group_id =:group_id and wf_winid =:wf_winid ";
    var2ParmStr("group_id");
    var2ParmStr("wf_winid");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_group error !");
    } else
      rc = 1;

    return rc;
  }

  public int insertGroup() {
    msgOK();
    strSql = " insert into sec_group_win ( " + " group_id , " + " seq_no , " + " wf_winid , "
        + " mod_user , " + " mod_pgm , " + " mod_time , " + " mod_seqno " + " ) values ( "
        + " :group_id , " + " :seq_no , " + " :wf_winid , " + " :mod_user , " + " :mod_pgm , "
        + " sysdate , " + " 1 " + " ) ";
    var2ParmStr("group_id");
    var2ParmStr("wf_winid");
    var2ParmNum("seq_no");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "secm0030");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert sec_group_win error !");
    }
    return rc;
  }

}
