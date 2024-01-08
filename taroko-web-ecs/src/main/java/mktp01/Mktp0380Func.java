/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/09  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0380Func extends busi.FuncProc {
  private String PROGNAME = "雙幣卡外幣刷卡金回饋參數檔維護處理程式108/09/09 V1.00.01";
//  String kk1;
  String approveTabName = "cyc_dc_fund_parm";
  String controlTabName = "cyc_dc_fund_parm_t";

  public Mktp0380Func(TarokoCommon wr) {
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
        + " fund_crt_date_s, " + " fund_crt_date_e, " + " stop_date, " + " stop_desc, "
        + " curr_code, " + " feedback_month_s, " + " feedback_month_e, " + " effect_months, "
        + " new_hldr_cond, " + " new_hldr_days, " + " new_group_cond, " + " new_hldr_card, "
        + " new_hldr_sup, " + " source_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + " platform_kind_sel, "
        + " group_card_sel, " + " group_code_sel, " + " purchase_amt_s1, " + " purchase_amt_e1, "
        + " feedback_rate_1, " + " purchase_amt_s2, " + " purchase_amt_e2, " + " feedback_rate_2, "
        + " purchase_amt_s3, " + " purchase_amt_e3, " + " feedback_rate_3, " + " purchase_amt_s4, "
        + " purchase_amt_e4, " + " feedback_rate_4, " + " purchase_amt_s5, " + " purchase_amt_e5, "
        + " feedback_rate_5, " + " feedback_lmt, " + " issue_cond, " + " issue_date_s, "
        + " issue_date_e, " + " issue_num_1, " + " issue_num_2, " + " issue_num_3, " + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("fund_code"), colStr("fund_name"),
        colStr("fund_crt_date_s"), colStr("fund_crt_date_e"), colStr("stop_date"),
        colStr("stop_desc"), colStr("curr_code"), colStr("feedback_month_s"),
        colStr("feedback_month_e"), colStr("effect_months"), colStr("new_hldr_cond"),
        colStr("new_hldr_days"), colStr("new_group_cond"), colStr("new_hldr_card"),
        colStr("new_hldr_sup"), colStr("source_code_sel"), colStr("merchant_sel"),
        colStr("mcht_group_sel"), colStr("platform_kind_sel"), colStr("group_card_sel"), colStr("group_code_sel"),
        colStr("purchase_amt_s1"), colStr("purchase_amt_e1"), colStr("feedback_rate_1"),
        colStr("purchase_amt_s2"), colStr("purchase_amt_e2"), colStr("feedback_rate_2"),
        colStr("purchase_amt_s3"), colStr("purchase_amt_e3"), colStr("feedback_rate_3"),
        colStr("purchase_amt_s4"), colStr("purchase_amt_e4"), colStr("feedback_rate_4"),
        colStr("purchase_amt_s5"), colStr("purchase_amt_e5"), colStr("feedback_rate_5"),
        colStr("feedback_lmt"), colStr("issue_cond"), colStr("issue_date_s"),
        colStr("issue_date_e"), colStr("issue_num_1"), colStr("issue_num_2"), colStr("issue_num_3"),
        "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime,
        wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " fund_code, " + " fund_name, " + " fund_crt_date_s, "
        + " fund_crt_date_e, " + " stop_date, " + " stop_desc, " + " curr_code, "
        + " feedback_month_s, " + " feedback_month_e, " + " effect_months, " + " new_hldr_cond, "
        + " new_hldr_days, " + " new_group_cond, " + " new_hldr_card, " + " new_hldr_sup, "
        + " source_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + " platform_kind_sel, " + " group_card_sel, "
        + " group_code_sel, " + " purchase_amt_s1, " + " purchase_amt_e1, " + " feedback_rate_1, "
        + " purchase_amt_s2, " + " purchase_amt_e2, " + " feedback_rate_2, " + " purchase_amt_s3, "
        + " purchase_amt_e3, " + " feedback_rate_3, " + " purchase_amt_s4, " + " purchase_amt_e4, "
        + " feedback_rate_4, " + " purchase_amt_s5, " + " purchase_amt_e5, " + " feedback_rate_5, "
        + " feedback_lmt, " + " issue_cond, " + " issue_date_s, " + " issue_date_e, "
        + " issue_num_1, " + " issue_num_2, " + " issue_num_3, " + " apr_date, " + " apr_user, "
        + " crt_date, " + " crt_user, "
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
    strSql = "update " + approveTabName + " set " + "fund_name = ?, " + "fund_crt_date_s = ?, "
        + "fund_crt_date_e = ?, " + "stop_date = ?, " + "stop_desc = ?, " + "curr_code = ?, "
        + "feedback_month_s = ?, " + "feedback_month_e = ?, " + "effect_months = ?, "
        + "new_hldr_cond = ?, " + "new_hldr_days = ?, " + "new_group_cond = ?, "
        + "new_hldr_card = ?, " + "new_hldr_sup = ?, " + "source_code_sel = ?, "
        + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "platform_kind_sel = ?, " + "group_card_sel = ?, "
        + "group_code_sel = ?, " + "purchase_amt_s1 = ?, " + "purchase_amt_e1 = ?, "
        + "feedback_rate_1 = ?, " + "purchase_amt_s2 = ?, " + "purchase_amt_e2 = ?, "
        + "feedback_rate_2 = ?, " + "purchase_amt_s3 = ?, " + "purchase_amt_e3 = ?, "
        + "feedback_rate_3 = ?, " + "purchase_amt_s4 = ?, " + "purchase_amt_e4 = ?, "
        + "feedback_rate_4 = ?, " + "purchase_amt_s5 = ?, " + "purchase_amt_e5 = ?, "
        + "feedback_rate_5 = ?, " + "feedback_lmt = ?, " + "issue_cond = ?, " + "issue_date_s = ?, "
        + "issue_date_e = ?, " + "issue_num_1 = ?, " + "issue_num_2 = ?, " + "issue_num_3 = ?, "
        + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   fund_code  = ? ";

    Object[] param = new Object[] {colStr("fund_name"), colStr("fund_crt_date_s"),
        colStr("fund_crt_date_e"), colStr("stop_date"), colStr("stop_desc"), colStr("curr_code"),
        colStr("feedback_month_s"), colStr("feedback_month_e"), colStr("effect_months"),
        colStr("new_hldr_cond"), colStr("new_hldr_days"), colStr("new_group_cond"),
        colStr("new_hldr_card"), colStr("new_hldr_sup"), colStr("source_code_sel"),
        colStr("merchant_sel"), colStr("mcht_group_sel"), colStr("platform_kind_sel"), colStr("group_card_sel"),
        colStr("group_code_sel"), colStr("purchase_amt_s1"), colStr("purchase_amt_e1"),
        colStr("feedback_rate_1"), colStr("purchase_amt_s2"), colStr("purchase_amt_e2"),
        colStr("feedback_rate_2"), colStr("purchase_amt_s3"), colStr("purchase_amt_e3"),
        colStr("feedback_rate_3"), colStr("purchase_amt_s4"), colStr("purchase_amt_e4"),
        colStr("feedback_rate_4"), colStr("purchase_amt_s5"), colStr("purchase_amt_e5"),
        colStr("feedback_rate_5"), colStr("feedback_lmt"), colStr("issue_cond"),
        colStr("issue_date_s"), colStr("issue_date_e"), colStr("issue_num_1"),
        colStr("issue_num_2"), colStr("issue_num_3"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, apr_flag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("fund_code")};

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
    strSql = "delete mkt_parm_data " + "where 1 = 1 " + "and table_name  =  'CYC_DC_FUND_PARM' "
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
    strSql = "delete mkt_parm_data_t " + "where 1 = 1 " + "and table_name  =  'CYC_DC_FUND_PARM' "
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
        + "and table_name  =  'CYC_DC_FUND_PARM' " + "and data_key  = ?  ";

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
