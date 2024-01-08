package colm05;
/**
 * 2019-0521:  JH    pgm-rename
 * 109-05-06  V1.00.01  Tanwei       updated for project coding standard
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* */


public class Colp5820Func extends busi.FuncAction {
//  /String kk1 = "1",
  String   acctType = "", validDate = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    String lsAprFlag = "";
    acctType = wp.itemStr("acct_type");
    validDate = wp.itemStr("valid_date");

    if (empty(acctType) || empty(validDate)) {
      errmsg("帳戶類別, 生效日期: 不可空白");
      return;
    }

    // if(eq_igno(wp.item_ss("apr_flag"),"N") && eq_igno(wp.item_ss("pause_flag"),"Y")){
    // errmsg("未放行, 不可[暫不執行]");
    // return ;
    // }
    //
    // if(eq_igno(wp.item_ss("apr_flag"),"N") && eq_igno(wp.item_ss("b_pause_flag"),"Y")){
    // errmsg("未放行, 不可[暫不執行]");
    // return ;
    // }

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
    if (rc != 1) {
      return rc;
    }

    if (updateMode2() != 1)
      return rc;
    if (updateMode1() != 1)
      return rc;

    return rc;
  }

  public int updateMode2() {
    msgOK();
    strSql =
        " update ptr_blockparam set " + " apr_flag =:apr_flag , " + " pause_flag =:pause_flag , "
            + " apr_date = to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user , "
            + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm =:mod_pgm , "
            + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 " + " and param_type = '1' "
            + " and exec_mode = '2' " + " and acct_type =:kk2 " + " and valid_date =:kk3 ";
    item2ParmStr("apr_flag");
    item2ParmStr("pause_flag");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk2", acctType);
    setString("kk3", validDate);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_blockparam(exec_mode='2') error !");
    }

    return rc;
  }

  public int updateMode1() {
    msgOK();
    strSql =
        " update ptr_blockparam set " + " apr_flag =:apr_flag , " + " pause_flag =:pause_flag , "
            + " apr_date = to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user , "
            + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm =:mod_pgm , "
            + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 " + " and param_type = '1' "
            + " and exec_mode = '1' " + " and acct_type =:kk2 " + " and valid_date =:kk3 ";
    item2ParmStr("apr_flag");
    item2ParmStr("pause_flag", "b_pause_flag");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk2", acctType);
    setString("kk3", validDate);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_blockparam(exec_mode='1') error !");
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
