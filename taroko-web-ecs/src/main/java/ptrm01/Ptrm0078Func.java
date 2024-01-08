/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-27  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0078Func extends FuncEdit {
  String mKkNetZmk1 = "";

  public Ptrm0078Func(TarokoCommon wr) {
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
      // 檢查新增資料是否超過一筆
      String lsSql = "select count(*) as tot_cnt from ptr_keys_table ";
      sqlSelect(lsSql);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;
    }
    if (this.isAdd()) {

    }

    // -other modify-
    sqlWhere = " where " + " nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {wp.modSeqno()};
    if (this.isOtherModify("ptr_keys_table", sqlWhere, param)) {
      errmsg("請重新查詢 !");
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
    strSql = "insert into ptr_keys_table (" + "  net_zmk1 " + ", net_zmk1_chk " + ", net_zmk2"
        + ", net_zmk2_chk" + ", net_zpk1" + ", net_zpk1_chk" + ", mod_time " + ", mod_user "
        + ", mod_pgm " + ", mod_seqno" + " ) values (" + " ?,?,?,?,?,?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("net_zmk1"), wp.itemStr("net_zmk1_chk"),
        wp.itemStr("net_zmk2"), wp.itemStr("net_zmk2_chk"), wp.itemStr("net_zpk1"),
        wp.itemStr("net_zpk1_chk"), wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_keys_table set " + "  net_zmk1 =? " + ", net_zmk1_chk =? "
        + ", net_zmk2 =? " + ", net_zmk2_chk =? " + ", net_zpk1 =? " + ", net_zpk1_chk =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("net_zmk1"), wp.itemStr("net_zmk1_chk"),
        wp.itemStr("net_zmk2"), wp.itemStr("net_zmk2_chk"), wp.itemStr("net_zpk1"),
        wp.itemStr("net_zpk1_chk"), wp.loginUser, wp.itemStr("mod_pgm"), wp.modSeqno()};
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
    strSql = "delete ptr_keys_table " + sqlWhere;
    Object[] param = new Object[] {wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
