/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncAction;

public class Ccam5410Func extends FuncAction {
  String entryType = "", entryMode = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      entryMode = wp.itemStr("kk_entry_mode");
    } else {

      entryMode = wp.itemStr("entry_mode");
    }
    entryType = wp.itemStr("entry_type");
    if (empty(entryType)) {
      this.errmsg("EntryMode類別:不可空白");
      return;
    }

    if (empty(entryMode)) {
      this.errmsg("Entry Mode:不可空白");
      return;
    }
    if (this.ibAdd) {
      return;
    }

    sqlWhere = " where 1=1 and entry_mode= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] { entryMode, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_entry_mode", sqlWhere,parms)) {
      return;
    }

    if (isEmpty(wp.itemStr("risk_factor"))) {
      wp.itemSet("risk_factor", "0");
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into cca_entry_mode (" + " entry_type ," + " entry_mode ," + " mode_desc ,"
        + " risk_factor ," + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ("
        + " :entry_type ," + " :kk2 ," + " :mode_desc ," + " :risk_factor ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " '1' " + " )";

    item2ParmStr("entry_type");
    setString("kk2", entryMode);
    item2ParmStr("mode_desc");
    item2ParmStr("risk_factor");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_pgm", "ccam5410");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql =
        "update cca_entry_mode set " + " mode_desc =:mode_desc ," + " entry_type =:entry_type ,"
            + " risk_factor =:risk_factor ," + " apr_date = to_char(sysdate,'yyyymmdd') ,"
            + " apr_user =:apr_user ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
            + " mod_pgm =:mod_pgm ," + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 "
            + " and entry_mode =:kk2 " + " and mod_seqno=:mod_seqno";

    item2ParmStr("risk_factor");
    item2ParmStr("entry_type");
    setString("kk2", entryMode);
    item2ParmStr("mode_desc");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5410");
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cca_entry_mode error !");
      return rc;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "delete cca_entry_mode " + " where entry_mode =:kk2 " + " and mod_seqno=:mod_seqno";

    setString("kk2", entryMode);
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete cca_entry_mode error !");
      return rc;
    }
    return rc;

  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
