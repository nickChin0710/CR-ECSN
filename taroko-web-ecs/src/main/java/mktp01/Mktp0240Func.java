/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/14  V1.00.01   Allen Ho      Initial                              *
*                                                                          *
***************************************************************************/
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0240Func extends busi.FuncProc {
  private String PROGNAME = "紅利積點兌換贈品登錄檔維護作業處理程式108/10/14 V1.00.01";
 // String kk1;
  String approveTabName = "mkt_gift";
  String controlTabName = "mkt_gift_t";

  public Mktp0240Func(TarokoCommon wr) {
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
    strSql =
        " insert into  " + approveTabName + " (" + " gift_no, " + " bonus_type, " + " gift_typeno, "
            + " gift_name, " + " disable_flag, " + " gift_type, " + " effect_months, "
            + " redem_days, " + " fund_code, " + " air_type, " + " cal_mile, " + " cash_value, "
            + " supply_count, " + " use_count, " + " net_count, " + " max_limit_count, "
            + " use_limit_count, " + " net_limit_count, " + " web_sumcnt, " + " limit_last_date, "
            + " vendor_no, " + " exchg_type, " + " apr_flag, " + " apr_date, " + " apr_user, "
            + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, "
            + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
            + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("gift_no"), colStr("bonus_type"), colStr("gift_typeno"),
        colStr("gift_name"), colStr("disable_flag"), colStr("gift_type"), colStr("effect_months"),
        colStr("redem_days"), colStr("fund_code"), colStr("air_type"), colStr("cal_mile"),
        colStr("cash_value"), colStr("supply_count"), colStr("use_count"), colStr("net_count"),
        colStr("max_limit_count"), colStr("use_limit_count"), colStr("net_limit_count"),
        colStr("web_sumcnt"), colStr("limit_last_date"), colStr("vendor_no"), colStr("exchg_type"),
        "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime,
        wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " gift_no, " + " bonus_type, " + " gift_typeno, " + " gift_name, "
        + " disable_flag, " + " gift_type, " + " effect_months, " + " redem_days, " + " fund_code, "
        + " air_type, " + " cal_mile, " + " cash_value, " + " supply_count, " + " use_count, "
        + " net_count, " + " max_limit_count, " + " use_limit_count, " + " net_limit_count, "
        + " web_sumcnt, " + " limit_last_date, " + " vendor_no, " + " exchg_type, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, "
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
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "bonus_type = ?, " + "gift_typeno = ?, "
        + "gift_name = ?, " + "disable_flag = ?, " + "gift_type = ?, " + "effect_months = ?, "
        + "redem_days = ?, " + "fund_code = ?, " + "air_type = ?, " + "cal_mile = ?, "
        + "cash_value = ?, " + "supply_count = ?, " + "use_count = ?, " + "net_count = ?, "
        + "max_limit_count = ?, " + "use_limit_count = ?, " + "net_limit_count = ?, "
        + "web_sumcnt = ?, " + "limit_last_date = ?, " + "vendor_no = ?, " + "crt_user  = ?, "
        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   gift_no  = ? ";

    Object[] param = new Object[] {colStr("bonus_type"), colStr("gift_typeno"), colStr("gift_name"),
        colStr("disable_flag"), colStr("gift_type"), colStr("effect_months"), colStr("redem_days"),
        colStr("fund_code"), colStr("air_type"), colStr("cal_mile"), colStr("cash_value"),
        colStr("supply_count"), colStr("use_count"), colStr("net_count"), colStr("max_limit_count"),
        colStr("use_limit_count"), colStr("net_limit_count"), colStr("web_sumcnt"),
        colStr("limit_last_date"), colStr("vendor_no"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, aprFlag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("gift_no")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and gift_no = ? ";

    Object[] param = new Object[] {colStr("gift_no")};

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
  public int dbDeleteD4Mgec() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete MKT_GIFT_EXCHGDATA " + "where 1 = 1 " + "and gift_no = ? ";

    Object[] param = new Object[] {colStr("gift_no")};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 MKT_GIFT_EXCHGDATA 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TMgec() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete MKT_GIFT_EXCHGDATA_t " + "where 1 = 1 " + "and gift_no = ? ";

    Object[] param = new Object[] {colStr("gift_no")};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 MKT_GIFT_EXCHGDATA_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Mgec() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into MKT_GIFT_EXCHGDATA " + "select * " + "from  MKT_GIFT_EXCHGDATA_t "
        + "where 1 = 1 " + "and gift_no = ? ";

    Object[] param = new Object[] {colStr("gift_no")};

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

}  // End of class
