/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi   coding standard, rename                  * 
******************************************************************************/
package ptrm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0460Func extends FuncEdit {
  String rowid = "";

  public Ptrm0460Func(TarokoCommon wr) {
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
    sqlWhere =	" where 1=1 and rowid= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};
    if (this.isOtherModify("PTR_STAT_COEXIST", sqlWhere,parms)) {
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
    strSql = "update PTR_STAT_COEXIST set " + " paper2email_mm =?, " + " email2paper_mm =?, "
        + " apr_flag =?, " + " apr_date =?, " + " apr_user =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + " where 1=1 and rowid = ? ";
    Object[] param = new Object[] {wp.itemNum("paper2email_mm"), wp.itemNum("email2paper_mm"), "Y",
        wp.itemStr("apr_date"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),wp.itemRowId("rowid")};
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
