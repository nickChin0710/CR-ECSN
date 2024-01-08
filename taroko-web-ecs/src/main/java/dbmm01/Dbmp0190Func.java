/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                          *
* 112-05-08  V1.00.03  Zuwei Su       增加"一般消費群組"    *
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmp0190Func extends busi.FuncProc {
  private final String PROGNAME = "Visa金融卡紅利加贈參數覆核-新發卡處理程式108/08/06 V1.00.01";
  /* String kk1; */
  String approveTabName = "dbm_bpis";
  String controlTabName = "dbm_bpis_t";

  public Dbmp0190Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " active_code, " + " active_name, "
        + " give_code, " + " new_card_cond, " + " active_s_date, " + " active_e_date, "
        + " re_months, " + " acct_type_sel, " + " group_code_sel, " + " merchant_sel, "
        + " mcht_group_sel, "+ " platform_kind_sel, " + " mcc_code_sel, " + " pos_entry_sel, " + " tax_flag, " + " bp_amt, "
        + " bp_pnt, " + " add_times, " + " add_point, " + " give_name, " + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?," + "?," + "to_char(sysdate,'yyyymmdd'),"
        + "?," + "?," + "?," + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("active_code"), colStr("active_name"),
        colStr("give_code"), colStr("new_card_cond"), colStr("active_s_date"),
        colStr("active_e_date"), colStr("re_months"), colStr("acct_type_sel"),
        colStr("group_code_sel"), colStr("merchant_sel"), colStr("mcht_group_sel"), colStr("platform_kind_sel"),
        colStr("mcc_code_sel"), colStr("pos_entry_sel"), colStr("tax_flag"), colStr("bp_amt"),
        colStr("bp_pnt"), colStr("add_times"), colStr("add_point"), colStr("give_name"), "Y",
        wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " active_code, " + " active_name, " + " give_code, " + " new_card_cond, "
        + " active_s_date, " + " active_e_date, " + " re_months, " + " acct_type_sel, "
        + " group_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + " platform_kind_sel, " + " mcc_code_sel, "
        + " pos_entry_sel, " + " tax_flag, " + " bp_amt, " + " bp_pnt, " + " add_times, "
        + " add_point, " + " give_name, " + " apr_date, " + " apr_user, " + " crt_date, "
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
    strSql = "update " + approveTabName + " set " + "active_name = ?, " + "give_code = ?, "
        + "new_card_cond = ?, " + "active_s_date = ?, " + "active_e_date = ?, " + "re_months = ?, "
        + "acct_type_sel = ?, " + "group_code_sel = ?, " + "merchant_sel = ?, "
        + "mcht_group_sel = ?, " + "platform_kind_sel = ?, " + "mcc_code_sel = ?, " + "pos_entry_sel = ?, " + "tax_flag = ?, "
        + "bp_amt = ?, " + "bp_pnt = ?, " + "add_times = ?, " + "add_point = ?, "
        + "give_name = ?, " + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   active_code  = ? ";

    Object[] param = new Object[] {colStr("active_name"), colStr("give_code"),
        colStr("new_card_cond"), colStr("active_s_date"), colStr("active_e_date"),
        colStr("re_months"), colStr("acct_type_sel"), colStr("group_code_sel"),
        colStr("merchant_sel"), colStr("mcht_group_sel"), colStr("platform_kind_sel"), colStr("mcc_code_sel"),
        colStr("pos_entry_sel"), colStr("tax_flag"), colStr("bp_amt"), colStr("bp_pnt"),
        colStr("add_times"), colStr("add_point"), colStr("give_name"), colStr("crt_user"),
        colStr("crt_date"), wp.loginUser, aprFlag, colStr("mod_user"), colStr("mod_time"),
        colStr("mod_pgm"), colStr("active_code")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and active_code = ? ";

    Object[] param = new Object[] {colStr("active_code")};

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
    strSql = "delete dbm_bn_data " + "where 1 = 1 " + "and table_name  =  'DBM_BPIS' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("ACTIVE_CODE"),};

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
    strSql = "delete dbm_bn_data_t " + "where 1 = 1 " + "and table_name  =  'DBM_BPIS' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("ACTIVE_CODE"),};

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
        + "and table_name  =  'DBM_BPIS' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("ACTIVE_CODE"),};

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

} // End of class
