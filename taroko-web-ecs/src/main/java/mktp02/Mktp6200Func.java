/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/16  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 112/03/12  V1.00.03   Zuwei Su      增欄位:一般消費                                                                                     *   
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6200Func extends busi.FuncProc {
  private final String PROGNAME = "基金參數檔覆核作業處理程式  112/03/12  V1.00.03";
 // String kk1;
  String approveTabName = "mkt_mcht_gp";
  String controlTabName = "mkt_mcht_gp_t";

  public Mktp6200Func(TarokoCommon wr) {
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
  strSql = " insert into  "
          + approveTabName
          + " ("
          + " mcht_group_id, "
          + " mcht_group_desc, "
          + " platform_flag, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + "?,"
          + "?,"
          + " ?) ";

  Object[] param = new Object[] {
          colStr("mcht_group_id"),
          colStr("mcht_group_desc"),
          colStr("platform_flag"),
          wp.loginUser,
          colStr("crt_date"),
          colStr("crt_user"),
          wp.sysDate + wp.sysTime,
          wp.loginUser,
          colStr("mod_seqno"),
          wp.modPgm()
  };

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String proc_tab_name = "";
    proc_tab_name = controlTabName;
    strSql = " select "
            + " mcht_group_id, "
            + " mcht_group_desc, "
            + " platform_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
            + " from "
            + proc_tab_name
            + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + proc_tab_name + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String aprFlag = "Y";
    strSql = "update "
            + approveTabName
            + " set "
            + "mcht_group_desc = ?, "
            + "platform_flag = ?, "
            + "crt_user  = ?, "
            + "crt_date  = ?, "
            + "apr_user  = ?, "
            + "apr_date  = to_char(sysdate,'yyyymmdd'), "
            + "mod_user  = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "mod_pgm   = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1 "
            + "where 1     = 1 "
            + "and   mcht_group_id  = ? ";

    Object[] param = new Object[] {
            colStr("mcht_group_desc"),
            colStr("platform_flag"),
            colStr("crt_user"),
            colStr("crt_date"),
            wp.loginUser,
            colStr("mod_user"),
            colStr("mod_time"),
            colStr("mod_pgm"),
            colStr("mcht_group_id")
    };

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and mcht_group_id = ? ";

    Object[] param = new Object[] {colStr("mcht_group_id")};

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
    strSql = "delete mkt_mchtgp_data " + "where 1 = 1 " + "and table_name  =  'MKT_MCHT_GP' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("mcht_group_id"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_mchtgp_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_mchtgp_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_MCHT_GP' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("mcht_group_id"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_mchtgp_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_mchtgp_data " + "select * " + "from  mkt_mchtgp_data_t "
        + "where 1 = 1 " + "and table_name  =  'MKT_MCHT_GP' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("mcht_group_id"),};

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
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
