/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei       updated for project coding standard
*                                                                          *
***************************************************************************/
package smsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsp0035Func extends busi.FuncProc {
  private String PROGNAME = "簡訊內容重新發送處理處理程式108/11/22 V1.00.01";
  String controlTabName = "sms_msg_dtl";

  public Smsp0035Func(TarokoCommon wr) {
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


    if (this.isAdd())
      return;

    // -other modify-
    sqlWhere = "where rowid = ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};
    if (this.isOtherModify(controlTabName, sqlWhere,parms)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  // ************************************************************************
  @Override
  public int dataProc() {
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "resend_flag = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_user = ?, " + "mod_user  = ?, "
        + "mod_time  = sysdate, " + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 "
        + "where rowid = ?";

    Object[] param = new Object[] {"Y", wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("wprowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
