/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.010  yanghan  修改了變量名稱和方法名稱								 *
* 109-06-04  V1.00.01  Alex     remove nccc_pickup							     *
* 109-12-25   V1.00.02 Justin   parameterize sql
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5110Func extends FuncEdit {
  String binType = "", excCode = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      binType = wp.itemStr("kk_bin_type");
    } else {
      binType = wp.itemStr("bin_type");
    }
    if (this.ibAdd) {
      excCode = wp.itemStr("kk_exc_code");
    } else {
      excCode = wp.itemStr("exc_code");
    }
    if (empty(binType)) {
      errmsg("卡別 不可空白");
      return;
    }
    if (empty(excCode)) {
      errmsg("原因碼 不可空白");
      return;
    }
    if (wp.itemEmpty("exc_desc")) {
      errmsg("說明 不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where sys_id= ? and sys_key = ? and nvl(mod_seqno,0) = ? ";
    if (this.isOtherModify("CCA_SYS_PARM1", sqlWhere, new Object[] {binType, excCode, wp.modSeqno()})) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into CCA_SYS_PARM1 (" + " sys_id, " // 1
        + " sys_key, " + " sys_data1, " // 3
        + " crt_date, crt_user, " + " apr_date, apr_user  "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {binType // 1
        , excCode, wp.itemStr("exc_desc"), wp.loginUser, wp.loginUser,
        wp.loginUser, wp.itemStr("mod_pgm") // 9
    };
    this.log("kk1=" + binType);
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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
    strSql = "update CCA_SYS_PARM1 set " + " sys_data1 =?, "
        + " apr_date =to_char(sysdate,'yyyymmdd'), apr_user =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("exc_desc"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), binType, excCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
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
    strSql = "delete CCA_SYS_PARM1 " + sqlWhere;
    log("del-sql=" + strSql);
    rc = sqlExec(strSql, new Object[] {binType, excCode, wp.modSeqno()});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
