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
public class Mktp6260Func extends busi.FuncProc {
  private String PROGNAME = "首刷禮活動回饋參數覆核處理程式108/08/15 V1.00.01";
 // String kk1;
  String approveTabName = "mkt_mcht_parm";
  String controlTabName = "mkt_mcht_parm_t";

  public Mktp6260Func(TarokoCommon wr) {
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
        + " stop_flag, " + " stop_date, " + " stop_desc, " + " active_type, " + " bonus_type, "
        + " tax_flag, " + " fund_code, " + " effect_months, " + " purchase_date_s, "
        + " purchase_date_e, " + " feedback_date, " + " feedback_key_sel, " + " issue_date_cond, "
        + " issue_date_s, " + " issue_date_e, " + " new_hldr_sel, " + " new_hldr_days, "
        + " new_group_cond, " + " new_hldr_card, " + " new_hldr_sup, " + " acct_type_sel, "
        + " group_code_sel, " + " record_cond, " + " record_group_no, " + " record_purc_flag, "
        + " record_n1_days, " + " record_n2_days, " + " bl_cond, " + " it_cond, "
        + " merchant_sel, " + " mcht_group_sel, " + " in_merchant_sel, " + " in_mcht_group_sel, "
        + " mcht_in_cond, " + " mcht_in_cnt, " + " mcht_in_per_amt, " + " mcht_in_amt, "
        + " mcc_code_sel, " + " pos_entry_sel, " + " per_amt_cond, " + " per_amt, "
        + " sum_cnt_cond, " + " sum_cnt, " + " sum_amt_cond, " + " sum_amt, " + " feedback_rate, "
        + " feedback_add_amt, " + " exchange_amt, " + " feedback_lmtamt_cond, "
        + " feedback_lmt_amt, " + " feedback_lmtcnt_cond, " + " feedback_lmt_cnt, "
        + " day_lmtamt_cond, " + " day_lmt_amt, " + " day_lmtcnt_cond, " + " day_lmt_cnt, "
        + " times_lmtamt_cond, " + " times_lmt_amt, " + " apr_flag, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("active_code"), colStr("active_name"),
        colStr("stop_flag"), colStr("stop_date"), colStr("stop_desc"), colStr("active_type"),
        colStr("bonus_type"), colStr("tax_flag"), colStr("fund_code"), colStr("effect_months"),
        colStr("purchase_date_s"), colStr("purchase_date_e"), colStr("feedback_date"),
        colStr("feedback_key_sel"), colStr("issue_date_cond"), colStr("issue_date_s"),
        colStr("issue_date_e"), colStr("new_hldr_sel"), colStr("new_hldr_days"),
        colStr("new_group_cond"), colStr("new_hldr_card"), colStr("new_hldr_sup"),
        colStr("acct_type_sel"), colStr("group_code_sel"), colStr("record_cond"),
        colStr("record_group_no"), colStr("record_purc_flag"), colStr("record_n1_days"),
        colStr("record_n2_days"), colStr("bl_cond"), colStr("it_cond"), colStr("merchant_sel"),
        colStr("mcht_group_sel"), colStr("in_merchant_sel"), colStr("in_mcht_group_sel"),
        colStr("mcht_in_cond"), colStr("mcht_in_cnt"), colStr("mcht_in_per_amt"),
        colStr("mcht_in_amt"), colStr("mcc_code_sel"), colStr("pos_entry_sel"),
        colStr("per_amt_cond"), colStr("per_amt"), colStr("sum_cnt_cond"), colStr("sum_cnt"),
        colStr("sum_amt_cond"), colStr("sum_amt"), colStr("feedback_rate"),
        colStr("feedback_add_amt"), colStr("exchange_amt"), colStr("feedback_lmtamt_cond"),
        colStr("feedback_lmt_amt"), colStr("feedback_lmtcnt_cond"), colStr("feedback_lmt_cnt"),
        colStr("day_lmtamt_cond"), colStr("day_lmt_amt"), colStr("day_lmtcnt_cond"),
        colStr("day_lmt_cnt"), colStr("times_lmtamt_cond"), colStr("times_lmt_amt"), "Y",
        wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " active_code, " + " active_name, " + " stop_flag, " + " stop_date, "
        + " stop_desc, " + " active_type, " + " bonus_type, " + " tax_flag, " + " fund_code, "
        + " effect_months, " + " purchase_date_s, " + " purchase_date_e, " + " feedback_date, "
        + " feedback_key_sel, " + " issue_date_cond, " + " issue_date_s, " + " issue_date_e, "
        + " new_hldr_sel, " + " new_hldr_days, " + " new_group_cond, " + " new_hldr_card, "
        + " new_hldr_sup, " + " acct_type_sel, " + " group_code_sel, " + " record_cond, "
        + " record_group_no, " + " record_purc_flag, " + " record_n1_days, " + " record_n2_days, "
        + " bl_cond, " + " it_cond, " + " merchant_sel, " + " mcht_group_sel, "
        + " in_merchant_sel, " + " in_mcht_group_sel, " + " mcht_in_cond, " + " mcht_in_cnt, "
        + " mcht_in_per_amt, " + " mcht_in_amt, " + " mcc_code_sel, " + " pos_entry_sel, "
        + " per_amt_cond, " + " per_amt, " + " sum_cnt_cond, " + " sum_cnt, " + " sum_amt_cond, "
        + " sum_amt, " + " feedback_rate, " + " feedback_add_amt, " + " exchange_amt, "
        + " feedback_lmtamt_cond, " + " feedback_lmt_amt, " + " feedback_lmtcnt_cond, "
        + " feedback_lmt_cnt, " + " day_lmtamt_cond, " + " day_lmt_amt, " + " day_lmtcnt_cond, "
        + " day_lmt_cnt, " + " times_lmtamt_cond, " + " times_lmt_amt, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, "
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
    strSql = "update " + approveTabName + " set " + "active_name = ?, " + "stop_flag = ?, "
        + "stop_date = ?, " + "stop_desc = ?, " + "active_type = ?, " + "bonus_type = ?, "
        + "tax_flag = ?, " + "fund_code = ?, " + "effect_months = ?, " + "purchase_date_s = ?, "
        + "purchase_date_e = ?, " + "feedback_date = ?, " + "feedback_key_sel = ?, "
        + "issue_date_cond = ?, " + "issue_date_s = ?, " + "issue_date_e = ?, "
        + "new_hldr_sel = ?, " + "new_hldr_days = ?, " + "new_group_cond = ?, "
        + "new_hldr_card = ?, " + "new_hldr_sup = ?, " + "acct_type_sel = ?, "
        + "group_code_sel = ?, " + "record_cond = ?, " + "record_group_no = ?, "
        + "record_purc_flag = ?, " + "record_n1_days = ?, " + "record_n2_days = ?, "
        + "bl_cond = ?, " + "it_cond = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, "
        + "in_merchant_sel = ?, " + "in_mcht_group_sel = ?, " + "mcht_in_cond = ?, "
        + "mcht_in_cnt = ?, " + "mcht_in_per_amt = ?, " + "mcht_in_amt = ?, " + "mcc_code_sel = ?, "
        + "pos_entry_sel = ?, " + "per_amt_cond = ?, " + "per_amt = ?, " + "sum_cnt_cond = ?, "
        + "sum_cnt = ?, " + "sum_amt_cond = ?, " + "sum_amt = ?, " + "feedback_rate = ?, "
        + "feedback_add_amt = ?, " + "exchange_amt = ?, " + "feedback_lmtamt_cond = ?, "
        + "feedback_lmt_amt = ?, " + "feedback_lmtcnt_cond = ?, " + "feedback_lmt_cnt = ?, "
        + "day_lmtamt_cond = ?, " + "day_lmt_amt = ?, " + "day_lmtcnt_cond = ?, "
        + "day_lmt_cnt = ?, " + "times_lmtamt_cond = ?, " + "times_lmt_amt = ?, "
        + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   active_code  = ? ";

    Object[] param = new Object[] {colStr("active_name"), colStr("stop_flag"), colStr("stop_date"),
        colStr("stop_desc"), colStr("active_type"), colStr("bonus_type"), colStr("tax_flag"),
        colStr("fund_code"), colStr("effect_months"), colStr("purchase_date_s"),
        colStr("purchase_date_e"), colStr("feedback_date"), colStr("feedback_key_sel"),
        colStr("issue_date_cond"), colStr("issue_date_s"), colStr("issue_date_e"),
        colStr("new_hldr_sel"), colStr("new_hldr_days"), colStr("new_group_cond"),
        colStr("new_hldr_card"), colStr("new_hldr_sup"), colStr("acct_type_sel"),
        colStr("group_code_sel"), colStr("record_cond"), colStr("record_group_no"),
        colStr("record_purc_flag"), colStr("record_n1_days"), colStr("record_n2_days"),
        colStr("bl_cond"), colStr("it_cond"), colStr("merchant_sel"), colStr("mcht_group_sel"),
        colStr("in_merchant_sel"), colStr("in_mcht_group_sel"), colStr("mcht_in_cond"),
        colStr("mcht_in_cnt"), colStr("mcht_in_per_amt"), colStr("mcht_in_amt"),
        colStr("mcc_code_sel"), colStr("pos_entry_sel"), colStr("per_amt_cond"), colStr("per_amt"),
        colStr("sum_cnt_cond"), colStr("sum_cnt"), colStr("sum_amt_cond"), colStr("sum_amt"),
        colStr("feedback_rate"), colStr("feedback_add_amt"), colStr("exchange_amt"),
        colStr("feedback_lmtamt_cond"), colStr("feedback_lmt_amt"), colStr("feedback_lmtcnt_cond"),
        colStr("feedback_lmt_cnt"), colStr("day_lmtamt_cond"), colStr("day_lmt_amt"),
        colStr("day_lmtcnt_cond"), colStr("day_lmt_cnt"), colStr("times_lmtamt_cond"),
        colStr("times_lmt_amt"), colStr("crt_user"), colStr("crt_date"), wp.loginUser, apr_flag,
        colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"), colStr("active_code")};

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
    strSql = "delete mkt_bn_data " + "where 1 = 1 " + "and table_name  =  'MKT_MCHT_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("active_code"),};

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
    strSql = "delete mkt_bn_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_MCHT_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("active_code"),};

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
        + "and table_name  =  'MKT_MCHT_PARM' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("active_code"),};

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
