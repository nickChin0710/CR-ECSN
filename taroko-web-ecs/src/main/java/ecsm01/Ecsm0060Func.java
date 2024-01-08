/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0060Func extends FuncEdit {
  private String progname = "MIS報表參數說明維護處理程式108/01/29 V1.00.01";
  String parmPgm;
  String controlTabName = "ptr_rpt_parm";

  public Ecsm0060Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (!this.ibAdd) {
      parmPgm = wp.itemStr("parm_pgm");
    }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "a1 = ?, " + "a2 = ?, " + "a3 = ?, "
        + "a4 = ?, " + "a5 = ?, " + "a6 = ?, " + "a7 = ?, " + "a8 = ?, " + "a9 = ?, " + "a10 = ?, "
        + "a11 = ?, " + "a12 = ?, " + "a13 = ?, " + "a14 = ?, " + "a15 = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("a1"), wp.itemStr("a2"), wp.itemStr("a3"),
        wp.itemStr("a4"), wp.itemStr("a5"), wp.itemStr("a6"), wp.itemStr("a7"), wp.itemStr("a8"),
        wp.itemStr("a9"), wp.itemStr("a10"), wp.itemStr("a11"), wp.itemStr("a12"),
        wp.itemStr("a13"), wp.itemStr("a14"), wp.itemStr("a15"), wp.itemStr("apr_user"),
        wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    return 1;
  }
  // ************************************************************************

} // End of class
