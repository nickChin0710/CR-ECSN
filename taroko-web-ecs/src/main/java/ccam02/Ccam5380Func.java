/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5380Func extends FuncEdit {
  String riskType = "";

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
      riskType = wp.itemStr("kk_risk_type");
    } else {
      riskType = wp.itemStr("risk_type");
    }

    if (empty(riskType)) {
      errmsg("風險類別群組 不可空白");
      return;
    }

    if (wp.itemEmpty("cnt_amt")) {
      errmsg("次限額  不可空白");
      return;
    }

    if (wp.itemEmpty("day_amt")) {
      errmsg("日限額  不可空白");
      return;
    }

    if (wp.itemEmpty("day_cnt")) {
      errmsg("日限次  不可空白");
      return;
    }

    if (wp.itemEmpty("month_amt")) {
      errmsg("月限額  不可空白");
      return;
    }

    if (wp.itemEmpty("month_cnt")) {
      errmsg("月限次  不可空白");
      return;
    }

    if (this.isAdd()) {
      return;
    }

    sqlWhere = " where risk_type= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {riskType,wp.itemNum("mod_seqno")};
    if (this.isOtherModify("CCA_debit_parm2", sqlWhere,parms)) {
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

    strSql = "insert into CCA_debit_parm2 (" + " risk_type, " // 1
        + " cnt_amt, " + " day_amt, " + " day_cnt, " + " month_amt, " + " month_cnt, "// 6
        + " crt_date, crt_user, " + " apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {riskType, wp.itemNum("cnt_amt"), wp.itemNum("day_amt"),
        wp.itemNum("day_cnt"), wp.itemNum("month_amt"), wp.itemNum("month_cnt"), wp.loginUser // crt-user
        , wp.itemStr("approval_user") // apr_user
        , modUser, modPgm};

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
    strSql = "update CCA_debit_parm2 set " + " cnt_amt =?, " + " day_amt =?, " + " day_cnt =?, "
        + " month_amt =?, " + " month_cnt =?, " + " apr_user =?, apr_date =" + this.sqlYmd
        + ", mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + " where risk_type= ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {wp.itemNum("cnt_amt"), wp.itemNum("day_amt"),
        wp.itemNum("day_cnt"), wp.itemNum("month_amt"), wp.itemNum("month_cnt"),
        wp.itemStr("approval_user"), modUser, modPgm,riskType,wp.itemNum("mod_seqno")};
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
    strSql = "delete CCA_debit_parm2 where risk_type= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {riskType,wp.itemNum("mod_seqno")};
    sqlExec(strSql,parms);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
