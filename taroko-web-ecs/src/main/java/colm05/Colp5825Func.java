/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package colm05;

public class Colp5825Func extends busi.FuncAction {
  String acctType = "", validDate = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    acctType = wp.itemStr("acct_type");
    validDate = wp.itemStr("valid_date");

    if (ibDelete)
      return;
    if (eqIgno(wp.itemStr("apr_flag"), "N") && eqIgno(wp.itemStr("pause_flag"), "Y")) {
      errmsg("未放行, 不可設定[執行中]");
      return;
    }

    sqlWhere = " where param_type = '2' " + " and exec_mode ='3' " + " and acct_type =:kk2 "
        + " and valid_date =:kk3 " + " and nvl(mod_seqno,0) =:mod_seqno ";
    setString("kk2", acctType);
    setString("kk3", validDate);
    item2ParmNum("mod_seqno");
    if (isOtherModify("ptr_blockparam", sqlWhere)) {
      return;
    }
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

    strSql = " update ptr_blockparam set " + " apr_flag =:apr_flag , apr_date = to_char(sysdate,'yyyymmdd') , "
    	+ " apr_user =:apr_user ,"
        + " pause_flag =:pause_flag , " + " mod_user =:mod_user , " + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm " + " where 1=1 " + " and param_type = '2' " + " and exec_mode ='3' "
        + " and acct_type =:kk2 " + " and valid_date =:kk3 ";

    item2ParmStr("apr_flag");
    setString("apr_user",wp.loginUser);
    item2ParmStr("pause_flag");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5825");
    setString("kk2", acctType);
    setString("kk3", validDate);
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update ptr_blockparam error !");
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
