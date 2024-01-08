package cmsm03;

import busi.FuncAction;

public class Cmsm4220Func extends FuncAction {

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

  public int insertRighList() {
    msgOK();

    strSql = " insert into cms_right_list ( " + " id_p_seqno , " + " item_no , " + " right_cnt , "
        + " right_date , " + " use_cnt , " + " last_use_date , " + " imp_file_name , "
        + " crt_date , " + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , "
        + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ( " + " :id_p_seqno , "
        + " :item_no , " + " :right_cnt , " + " :right_date , " + " 0 , " + " '' , "
        + " :imp_file_name , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " '' , "
        + " '' , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    item2ParmStr("id_p_seqno");
    item2ParmStr("item_no");
    item2ParmNum("right_cnt");
    item2ParmStr("right_date");
    item2ParmStr("imp_file_name");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Import cms_right_list error !");
    }

    return rc;
  }

}
