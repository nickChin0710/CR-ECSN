/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/15  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp3850Func extends busi.FuncProc {
  private String PROGNAME = "指定繳款方式基金參數維護處理程式108/08/15 V1.00.01";
 // String kk1;
  String approveTabName = "mkt_nfc_parm";
  String controlTabName = "mkt_nfc_parm_t";

  public Mktp3850Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " fund_code, " + " fund_name, "
        + " fund_crt_date_s, " + " fund_crt_date_e, " + " stop_flag, " + " stop_date, "
        + " stop_desc, " + " effect_months, " + " group_card_sel, " + " group_code_sel, "
        + " payment_sel, " + " merchant_sel, " + " mcht_group_sel, " + " mcc_code_sel, "
        + " bl_cond, " + " it_cond, " + " ca_cond, " + " id_cond, " + " ao_cond, " + " ot_cond, "
        + " feedback_rate, " + " feedback_lmt, " + " cancel_period, " + " cancel_scope, "
        + " cancel_event, " + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("fund_code"), colStr("fund_name"),
        colStr("fund_crt_date_s"), colStr("fund_crt_date_e"), colStr("stop_flag"),
        colStr("stop_date"), colStr("stop_desc"), colStr("effect_months"), colStr("group_card_sel"),
        colStr("group_code_sel"), colStr("payment_sel"), colStr("merchant_sel"),
        colStr("mcht_group_sel"), colStr("mcc_code_sel"), colStr("bl_cond"), colStr("it_cond"),
        colStr("ca_cond"), colStr("id_cond"), colStr("ao_cond"), colStr("ot_cond"),
        colStr("feedback_rate"), colStr("feedback_lmt"), colStr("cancel_period"),
        colStr("cancel_scope"), colStr("cancel_event"), "Y", wp.loginUser, colStr("crt_date"),
        colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"),
        wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " fund_code, " + " fund_name, " + " fund_crt_date_s, "
        + " fund_crt_date_e, " + " stop_flag, " + " stop_date, " + " stop_desc, "
        + " effect_months, " + " group_card_sel, " + " group_code_sel, " + " payment_sel, "
        + " merchant_sel, " + " mcht_group_sel, " + " mcc_code_sel, " + " bl_cond, " + " it_cond, "
        + " ca_cond, " + " id_cond, " + " ao_cond, " + " ot_cond, " + " feedback_rate, "
        + " feedback_lmt, " + " cancel_period, " + " cancel_scope, " + " cancel_event, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
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
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "fund_name = ?, " + "fund_crt_date_s = ?, "
        + "fund_crt_date_e = ?, " + "stop_flag = ?, " + "stop_date = ?, " + "stop_desc = ?, "
        + "effect_months = ?, " + "group_card_sel = ?, " + "group_code_sel = ?, "
        + "payment_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "mcc_code_sel = ?, "
        + "bl_cond = ?, " + "it_cond = ?, " + "ca_cond = ?, " + "id_cond = ?, " + "ao_cond = ?, "
        + "ot_cond = ?, " + "feedback_rate = ?, " + "feedback_lmt = ?, " + "cancel_period = ?, "
        + "cancel_scope = ?, " + "cancel_event = ?, " + "crt_user  = ?, " + "crt_date  = ?, "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
        + "and   fund_code  = ? ";

    Object[] param = new Object[] {colStr("fund_name"), colStr("fund_crt_date_s"),
        colStr("fund_crt_date_e"), colStr("stop_flag"), colStr("stop_date"), colStr("stop_desc"),
        colStr("effect_months"), colStr("group_card_sel"), colStr("group_code_sel"),
        colStr("payment_sel"), colStr("merchant_sel"), colStr("mcht_group_sel"),
        colStr("mcc_code_sel"), colStr("bl_cond"), colStr("it_cond"), colStr("ca_cond"),
        colStr("id_cond"), colStr("ao_cond"), colStr("ot_cond"), colStr("feedback_rate"),
        colStr("feedback_lmt"), colStr("cancel_period"), colStr("cancel_scope"),
        colStr("cancel_event"), colStr("crt_user"), colStr("crt_date"), wp.loginUser, apr_flag,
        colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"), colStr("fund_code")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and fund_code = ? ";

    Object[] param = new Object[] {colStr("fund_code")};

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
    strSql = "delete mkt_parm_data " + "where 1 = 1 " + "and table_name  =  'MKT_NFC_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("fund_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_parm_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_parm_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_NFC_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("fund_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_parm_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_parm_data " + "select * " + "from  mkt_parm_data_t " + "where 1 = 1 "
        + "and table_name  =  'MKT_NFC_PARM' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("fund_code"),};

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
