/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23  V1.00.01   Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0030Func extends FuncEdit {
  String paymentType = "";

  // public ptrm0030Func(
  // TarokoCommon wr) {
  // wp = wr;
  // this.conn = wp.getConn();
  // }

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
      paymentType = wp.itemStr("kk_payment_type");
    } else {
      paymentType = wp.itemStr("payment_type");
    }

    if (empty("chi_chort_name")) {
      errmsg("中文說明：不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }

    sqlWhere = " where payment_type= ? and nvl(mod_seqno,0) = ? ";

    if (this.isOtherModify("ptr_payment", sqlWhere, new Object[] {paymentType, wp.modSeqno()})) {
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

    strSql = "insert into ptr_payment (" + " payment_type, " // 1
        + " chi_name, " + " bill_desc, " + " pay_note," + " fund_flag," + " bill_flag, "
        + " crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {paymentType, wp.itemStr("chi_name"), wp.itemStr("bill_desc"),
        wp.itemStr("pay_note"), wp.itemNvl("fund_flag", "N"), wp.itemNvl("bill_flag", "N"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

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
    strSql = "update ptr_payment set " + " chi_name =?, " + " bill_desc =?, " + " pay_note =?,"
        + " fund_flag =?," + " bill_flag =?, " + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param =
        new Object[] {wp.itemStr("chi_name"), wp.itemStr("bill_desc"), wp.itemStr("pay_note"),
            wp.itemNvl("fund_flag", "N"), wp.itemNvl("bill_flag", "N"), wp.loginUser, wp.modPgm(), paymentType, wp.modSeqno()};
    wp.log(strSql, param);
    sqlExec(strSql, param);
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
    strSql = "delete ptr_payment " + sqlWhere;
    sqlExec(strSql, new Object[] {paymentType, wp.modSeqno()});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
