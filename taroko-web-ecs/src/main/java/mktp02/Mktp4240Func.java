/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp4240Func extends busi.FuncProc {
  private String PROGNAME = "影城訂票商店參數檔處理程式108/08/06 V1.00.01";
 // String kk1, kk2, kk3;
  String approveTabName = "mkt_ticket_parm2";
  String controlTabName = "mkt_ticket_parm2_t";

  public Mktp4240Func(TarokoCommon wr) {
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
  public void dataCheck() {}

  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }

  // ************************************************************************
  public int dbInsertA4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + approveTabName + " (" + " active_code, " + " store_no, "
        + " store_date_s, " + " store_date_e, " + " store_name, " + " ez_purchase_amt, "
        + " origin_amt, " + " selfpay_amt, " + " p_deduct_bp, " + " p_deduct_amt, " + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param =
        new Object[] {colStr("active_code"), colStr("store_no"), colStr("store_date_s"),
            colStr("store_date_e"), colStr("store_name"), colStr("ez_purchase_amt"),
            colStr("origin_amt"), colStr("selfpay_amt"), colStr("p_deduct_bp"),
            colStr("p_deduct_amt"), "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"),
            wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " active_code, " + " store_no, " + " store_date_s, " + " store_date_e, "
        + " store_name, " + " ez_purchase_amt, " + " origin_amt, " + " selfpay_amt, "
        + " p_deduct_bp, " + " p_deduct_amt, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + procTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "store_date_e = ?, " + "store_name = ?, "
        + "ez_purchase_amt = ?, " + "origin_amt = ?, " + "selfpay_amt = ?, " + "p_deduct_bp = ?, "
        + "p_deduct_amt = ?, " + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   active_code  = ? "
        + "and   store_no  = ? " + "and   store_date_s  = ? ";

    Object[] param = new Object[] {colStr("store_date_e"), colStr("store_name"),
        colStr("ez_purchase_amt"), colStr("origin_amt"), colStr("selfpay_amt"),
        colStr("p_deduct_bp"), colStr("p_deduct_amt"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, aprFlag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("active_code"), colStr("store_no"), colStr("store_date_s")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and active_code = ? "
        + "and store_no = ? " + "and store_date_s = ? ";

    Object[] param =
        new Object[] {colStr("active_code"), colStr("store_no"), colStr("store_date_s")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDelete() {
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
