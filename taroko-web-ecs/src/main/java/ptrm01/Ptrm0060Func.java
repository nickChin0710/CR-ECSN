/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0060Func extends FuncEdit {
  String mKkInstallmentType = "";

  public Ptrm0060Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

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
      mKkInstallmentType = wp.itemStr("kk_installment_type");
    } else {
      mKkInstallmentType = wp.itemStr("installment_type");
    }

    log(this.actionCode + ", installment_type = " + mKkInstallmentType + ", mod_seqno="
        + wp.modSeqno());

    if (isEmpty(mKkInstallmentType)) {
      errmsg("分期付款種類不可空白");
      return;
    }
    if (empty(wp.itemStr("installment_limit"))) {
      errmsg("分期付款額度不可空白");
      return;
    }
    if (empty(wp.itemStr("confirm_over_day"))) {
      errmsg("放行超過天數不可空白");
      return;
    }
    if (empty(wp.itemStr("inst_m_code"))) {
      errmsg("分期付款帳齡不可空白");
      return;
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_instlmt where installment_type = ?";
      Object[] param = new Object[] {mKkInstallmentType};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;

    } else {
      // -other modify-
      sqlWhere = " where installment_type = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkInstallmentType, wp.modSeqno()};
      if (this.isOtherModify("ptr_instlmt", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_instlmt (" + " installment_type, " + " installment_limit, "
        + " confirm_over_day, " + " inst_m_code, " + " crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ?, ?, ? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate, ?, ?, 1" + " )";
    // -set ? value-
    Object[] param = new Object[] {mKkInstallmentType, wp.itemStr("installment_limit"),
        wp.itemStr("confirm_over_day"), wp.itemStr("inst_m_code"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm")};

    this.log("installment_type=" + mKkInstallmentType);

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

    strSql = "update ptr_instlmt set " + " installment_limit =?, " + " confirm_over_day =?, "
        + " inst_m_code =?, " + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("installment_limit"), wp.itemStr("confirm_over_day"),
        wp.itemStr("inst_m_code"), wp.loginUser, wp.itemStr("mod_pgm"), mKkInstallmentType,
        wp.modSeqno()};

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
    strSql = "delete ptr_instlmt " + sqlWhere;
    Object[] param = new Object[] {mKkInstallmentType, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
