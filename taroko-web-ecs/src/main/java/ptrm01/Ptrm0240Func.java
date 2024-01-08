/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-02  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Tanwei     updated for project coding standard        *
* 109-07-31  V1.00.02  Sunny      fix conf_flag update                       *
*                                                                            *
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0240Func extends FuncEdit {
  String mKkBillUnit = "";

  public Ptrm0240Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
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
      mKkBillUnit = wp.itemStr("kk_bill_unit");
    } else {
      mKkBillUnit = wp.itemStr("bill_unit");
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_billunit where bill_unit = ?";
      Object[] param = new Object[] {mKkBillUnit};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where bill_unit = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkBillUnit, wp.modSeqno()};
      isOtherModify("ptr_billunit", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_billunit (" + " bill_unit " + ", short_title" + ", describe"
        + ", conf_flag" + ", indelv_mx" + ", model1_mx" + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ?, ?, ?, ?, ? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkBillUnit // 1
        , wp.itemStr("short_title"), wp.itemStr("describe"),
        wp.itemStr("conf_flag").equals("Y") ? wp.itemStr("conf_flag") : "N",
        wp.itemStr("indelv_mx"), wp.itemStr("model1_mx"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_billunit set " + "short_title = ?" + ", describe = ?" + ", conf_flag = ?"
        + ", indelv_mx = ?" + ", model1_mx = ?" + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
        + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("short_title"), wp.itemStr("describe"),
        wp.itemStr("conf_flag").equals("Y") ? wp.itemStr("conf_flag") : "N", wp.itemStr("indelv_mx"), wp.itemStr("model1_mx"), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkBillUnit, wp.modSeqno()};
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
    strSql = "delete ptr_billunit " + sqlWhere;
    Object[] param = new Object[] {mKkBillUnit, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
