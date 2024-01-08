/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package dbam01;

import busi.FuncAction;

public class Dbam0020Func extends FuncAction {
  String txnCode = "", lsComment = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      txnCode = wp.itemStr("kk_txn_code");
    } else {
      txnCode = wp.itemStr("txn_code");
    }

    if (empty(txnCode)) {
      errmsg("交易代碼 : 不可空白 ");
      return;
    }

    if (this.ibDelete)
      return;

    if (eqIgno(wp.itemStr("comment_code"), "1")) {
      lsComment = "交易日+特店名稱";
    } else {
      lsComment = wp.itemStr("txn_comment");
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into dbp_txn_code ( " + " txn_code , " + " txn_desc , " + " summary_code , "
        + " summary_desc , " + " comment_code , " + " txn_comment , " + " crt_user , "
        + " crt_date , " + " apr_user , " + " apr_date , " + " mod_user , " + " mod_time , "
        + " mod_pgm , " + " mod_seqno " + " ) values ( " + " :txn_code , " + " :txn_desc , "
        + " :summary_code , " + " :summary_desc , " + " :comment_code , " + " :txn_comment , "
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :mod_user , " + " sysdate , " + " :mod_pgm , "
        + " 1 " + " ) ";

    setString("txn_code", txnCode);
    item2ParmStr("txn_desc");
    item2ParmStr("summary_code");
    item2ParmStr("summary_desc");
    item2ParmNvl("comment_code", "0");
    setString("txn_comment", lsComment);
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "dbam0020");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert dbp_txn_code error ! err:" + getMsg());
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update dbp_txn_code set " + " txn_desc =:txn_desc , "
        + " summary_code =:summary_code , " + " summary_desc =:summary_desc , "
        + " comment_code =:comment_code , " + " txn_comment =:txn_comment , "
        + " apr_user =:apr_user , " + " apr_date =to_char(sysdate,'yyyymmdd') , "
        + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where txn_code =:txn_code ";

    item2ParmStr("txn_desc");
    item2ParmStr("summary_code");
    item2ParmStr("summary_desc");
    item2ParmNvl("comment_code", "0");
    setString("txn_comment", lsComment);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "dbam0020");
    setString("txn_code", txnCode);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update dbp_txn_code error ! err:" + getMsg());
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = "delete dbp_txn_code where txn_code =:txn_code ";
    setString("txn_code", txnCode);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete dbp_txn_code error ! err:" + getMsg());
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
