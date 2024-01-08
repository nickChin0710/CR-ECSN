/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm3010Func extends FuncEdit {
  String mKkVendor = "";

  public Ptrm3010Func(TarokoCommon wr) {
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
      mKkVendor = wp.itemStr("kk_vendor");
    } else {
      mKkVendor = wp.itemStr("vendor");
    }

    log(this.actionCode + ", vendor = " + mKkVendor + ", mod_seqno=" + wp.modSeqno());

    if (isEmpty(mKkVendor)) {
      errmsg("廠商代碼不可空白");
      return;
    }
    if (empty(wp.itemStr("vendor_name"))) {
      errmsg("廠商名稱不可空白");
      return;
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_vendor_setting where vendor = ?";
      Object[] param = new Object[] {mKkVendor};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where vendor = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkVendor, wp.modSeqno()};
      isOtherModify("ptr_vendor_setting", sqlWhere, param);
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_vendor_setting (" + " vendor, " + " vendor_name, " + " vendor_tscc, "
        + " crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?, ?, ?" + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate, ?, ?, 1" + " )";
    // -set ? value-
    Object[] param = new Object[] {mKkVendor, wp.itemStr("vendor_name"), wp.itemStr("vendor_tscc"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};

    this.log("vendor=" + mKkVendor);

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

    strSql = "update ptr_vendor_setting set " + " vendor_name =?, " + " vendor_tscc =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("vendor_name"), wp.itemStr("vendor_tscc"),
        wp.loginUser, wp.itemStr("mod_pgm"), mKkVendor, wp.modSeqno()};

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
    strSql = "delete ptr_vendor_setting " + sqlWhere;
    Object[] param = new Object[] {mKkVendor, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
