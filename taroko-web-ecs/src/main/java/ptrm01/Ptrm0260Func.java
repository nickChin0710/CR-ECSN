/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23  V1.00.01   Justin        parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0260Func extends FuncEdit {
  String rowid = "";

  public Ptrm0260Func(TarokoCommon wr) {
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
    rowid = wp.itemStr("rowid");
    if (empty(wp.itemStr("business_date"))) {
      errmsg("營業日：不可空白");
      return;
    }
    if (empty(wp.itemStr("online_date"))) {
      errmsg("線上營業日：不可空白");
      return;
    }
    if (empty(wp.itemStr("vouch_date"))) {
      errmsg("系統起帳日：不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere =
        " where hex(rowid) = ? and nvl(mod_seqno,0) = ? ";
    log("sql-where=" + sqlWhere);
    if (this.isOtherModify("ptr_businday", sqlWhere, new Object[] {rowid, wp.modSeqno()})) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "update ptr_businday set " + " business_date =?, " + " online_date =?, "
        + " vouch_date =?, " + " vouch_chk_flag =?, " + " vouch_close_flag =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("business_date"), wp.itemStr("online_date"),
        wp.itemStr("vouch_date"), wp.itemStr("vouch_chk_flag"), wp.itemStr("vouch_close_flag"),
        wp.loginUser, wp.itemStr("mod_pgm"), rowid, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

}
