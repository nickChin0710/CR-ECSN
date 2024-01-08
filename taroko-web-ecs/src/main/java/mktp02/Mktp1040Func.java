/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1040Func extends busi.FuncProc {
  private String PROGNAME = "高鐵車廂升等卡類覆核處理程式108/11/05 V1.00.01";
 // String kk1;
  String approveTabName = "mkt_thsr_upmode";
  String controlTabName = "mkt_thsr_upmode_t";

  public Mktp1040Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " card_mode, " + " mode_desc, "
        + " card_type_sel, " + " group_code_sel, " + " max_ticket_cnt, " + " ticket_pnt_cond, "
        + " ticket_pnt_cnt, " + " ticket_pnt, " + " ticket_amt_cond, " + " ticket_amt_cnt, "
        + " ticket_amt, " + " ex_ticket_amt, " + " apr_flag, " + " apr_date, " + " apr_user, "
        + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, "
        + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("card_mode"), colStr("mode_desc"),
        colStr("card_type_sel"), colStr("group_code_sel"), colStr("max_ticket_cnt"),
        colStr("ticket_pnt_cond"), colStr("ticket_pnt_cnt"), colStr("ticket_pnt"),
        colStr("ticket_amt_cond"), colStr("ticket_amt_cnt"), colStr("ticket_amt"),
        colStr("ex_ticket_amt"), "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"),
        wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " card_mode, " + " mode_desc, " + " card_type_sel, " + " group_code_sel, "
        + " max_ticket_cnt, " + " ticket_pnt_cond, " + " ticket_pnt_cnt, " + " ticket_pnt, "
        + " ticket_amt_cond, " + " ticket_amt_cnt, " + " ticket_amt, " + " ex_ticket_amt, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "mode_desc = ?, " + "card_type_sel = ?, "
        + "group_code_sel = ?, " + "max_ticket_cnt = ?, " + "ticket_pnt_cond = ?, "
        + "ticket_pnt_cnt = ?, " + "ticket_pnt = ?, " + "ticket_amt_cond = ?, "
        + "ticket_amt_cnt = ?, " + "ticket_amt = ?, " + "ex_ticket_amt = ?, " + "crt_user  = ?, "
        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   card_mode  = ? ";

    Object[] param = new Object[] {colStr("mode_desc"), colStr("card_type_sel"),
        colStr("group_code_sel"), colStr("max_ticket_cnt"), colStr("ticket_pnt_cond"),
        colStr("ticket_pnt_cnt"), colStr("ticket_pnt"), colStr("ticket_amt_cond"),
        colStr("ticket_amt_cnt"), colStr("ticket_amt"), colStr("ex_ticket_amt"), colStr("crt_user"),
        colStr("crt_date"), wp.loginUser, apr_flag, colStr("mod_user"), colStr("mod_time"),
        colStr("mod_pgm"), colStr("card_mode")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and card_mode = ? ";

    Object[] param = new Object[] {colStr("card_mode")};

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
  public int dbDeleteD4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_bn_data " + "where 1 = 1 " + "and table_name  =  'MKT_THSR_UPMODE' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("card_mode"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_bn_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_THSR_UPMODE' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("card_mode"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_bn_data " + "select * " + "from  mkt_bn_data_t " + "where 1 = 1 "
        + "and table_name  =  'MKT_THSR_UPMODE' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("card_mode"),};

    sqlExec(strSql, param);

    return 1;
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
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }
  // ************************************************************************

} // End of class
