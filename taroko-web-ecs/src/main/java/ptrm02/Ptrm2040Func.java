/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;

import busi.FuncAction;

public class Ptrm2040Func extends FuncAction {
  String acctType = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd)
      acctType = wp.itemStr("kk_acct_type");
    else
      acctType = wp.itemStr("acct_type");

    if (empty(acctType)) {
      errmsg("帳戶類別：不可空白！");
      return;
    }

    if (this.ibDelete)
      return;

    if (wp.itemNum("three_month") <= 0 || wp.itemNum("five_month") <= 0
        || wp.itemNum("seven_month") <= 0) {
      errmsg("月數不可小於等於0!!");
      return;
    }

    if (wp.itemNum("seven_month") < wp.itemNum("five_month")
        || wp.itemNum("five_month") < wp.itemNum("three_month")) {
      errmsg("不可小於之前月份!!");
      return;
    }


  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = "insert into ptr_print (" + " acct_type ," + " three_month ," + " five_month ,"
        + " seven_month ," + " three_fee ," + " five_fee ," + " seven_fee ," + " apr_flag ,"
        + " apr_user ," + " apr_date ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values (" + " :kk1 ," + " :three_month ," + " :five_month ,"
        + " :seven_month ," + " :three_fee ," + " :five_fee ," + " :seven_fee ," + " 'Y' ,"
        + " :apr_user ," + " to_char(sysdate,'yyyymmdd') ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " '1' " + " )";
    setString("kk1", acctType);
    item2ParmNum("three_month");
    item2ParmNum("five_month");
    item2ParmNum("seven_month");
    item2ParmNum("three_fee");
    item2ParmNum("five_fee");
    item2ParmNum("seven_fee");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("insert ptr_print error !");
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update ptr_print set " + " three_month =:three_month ,"
        + " five_month =:five_month ," + " seven_month =:seven_month ," + " three_fee =:three_fee ,"
        + " five_fee =:five_fee ," + " seven_fee =:seven_fee ," + " apr_flag ='Y' ,"
        + " apr_user =:apr_user ," + " apr_date =to_char(sysdate,'yyyymmdd') ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where acct_type =:kk1 ";
    item2ParmNum("three_month");
    item2ParmNum("five_month");
    item2ParmNum("seven_month");
    item2ParmNum("three_fee");
    item2ParmNum("five_fee");
    item2ParmNum("seven_fee");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk1", acctType);
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("update ptr_print error !");
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " delete ptr_print where acct_type=:kk1 ";
    setString("kk1", acctType);
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("delete ptr_print error !");
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
