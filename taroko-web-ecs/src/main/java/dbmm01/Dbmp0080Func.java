/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/26  V1.00.01   Allen Ho      Initial                              *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                          *
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmp0080Func extends busi.FuncProc
{
  private final String PROGNAME = "紅利積點兌換參數覆核處理程式108/11/26 V1.00.01";
  // String kk1,kk2,kk3;
  String approveTabName = "dbm_bpid";
  String controlTabName = "dbm_bpid_t";

  public Dbmp0080Func(TarokoCommon wr) {
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
  public int dbInsertA4() throws Exception {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + approveTabName + " (" + " years, " + " acct_type, " + " item_code, "
        + " bp_type, " + " give_bp, " + " bp_amt, " + " bp_pnt, " + " pos_entry_sel, "
        + " group_code_sel, " + " mcc_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + " platform_kind_sel, "
        + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param =
        new Object[] {colStr("years"), colStr("acct_type"), colStr("item_code"), colStr("bp_type"),
            colStr("give_bp"), colStr("bp_amt"), colStr("bp_pnt"), colStr("pos_entry_sel"),
            colStr("group_code_sel"), colStr("mcc_code_sel"), colStr("merchant_sel"),
            colStr("mcht_group_sel"), colStr("platform_kind_sel"), "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"),
            wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String proc_tab_name = "";
    proc_tab_name = controlTabName;
    strSql = " select " + " years, " + " acct_type, " + " item_code, " + " bp_type, " + " give_bp, "
        + " bp_amt, " + " bp_pnt, " + " pos_entry_sel, " + " group_code_sel, " + " mcc_code_sel, "
        + " merchant_sel, " + " mcht_group_sel, " + "platform_kind_sel, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + proc_tab_name + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() throws Exception {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String apr_flag = "Y";
    strSql =
        "update " + approveTabName + " set " + "bp_type = ?, " + "give_bp = ?, " + "bp_amt = ?, "
            + "bp_pnt = ?, " + "pos_entry_sel = ?, " + "group_code_sel = ?, " + "mcc_code_sel = ?, "
            + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "platform_kind_sel = ?," + "crt_user  = ?, " + "crt_date  = ?, "
            + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
            + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
            + "and   years  = ? " + "and   acct_type  = ? " + "and   item_code  = ? ";

    Object[] param = new Object[] {colStr("bp_type"), colStr("give_bp"), colStr("bp_amt"),
        colStr("bp_pnt"), colStr("pos_entry_sel"), colStr("group_code_sel"), colStr("mcc_code_sel"),
        colStr("merchant_sel"), colStr("mcht_group_sel"), colStr("platform_kind_sel"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, apr_flag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("years"), colStr("acct_type"), colStr("item_code")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and years = ? "
        + "and acct_type = ? " + "and item_code = ? ";

    Object[] param = new Object[] {colStr("years"), colStr("acct_type"), colStr("item_code")};

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
    strSql = "delete dbm_bn_data " + "where 1 = 1 " + "and table_name  =  'DBM_BPID' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("years") + colStr("acct_type") + colStr("item_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 dbm_bn_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete dbm_bn_data_t " + "where 1 = 1 " + "and table_name  =  'DBM_BPID' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("years") + colStr("acct_type") + colStr("item_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 dbm_bn_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into dbm_bn_data " + "select * " + "from  dbm_bn_data_t " + "where 1 = 1 "
        + "and table_name  =  'DBM_BPID' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("years") + colStr("acct_type") + colStr("item_code"),};

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
