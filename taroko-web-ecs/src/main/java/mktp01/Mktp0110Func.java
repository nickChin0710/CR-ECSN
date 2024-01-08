/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0110Func extends busi.FuncProc {
  private String PROGNAME = "DM參數資料檔覆核作業處理程式108/01/29 V1.00.01";
 // String kk1;
  String approveTabName = "mkt_dm_parm";
  String controlTabName = "mkt_dm_parm_t";

  public Mktp0110Func(TarokoCommon wr) {
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
    strSql = " insert into  " + controlTabName + " (" + " list_batch_no, " + " list_desc, "
        + " list_sel, " + " acct_type_sel, " + " vd_flag, " + " card_type_sel, " + " bin_type_sel, "
        + " group_code_sel, " + " id_dup_cond, " + " source_code_sel, " + " valid_card_flag, "
        + " valid_check_cond, " + " valid_stop_days, " + " excl_card_cond, " + " sup_check_flag, "
        + " expire_chg_cond, " + " apply_date_cond, " + " apply_date_s, " + " apply_date_e, "
        + " apply_excl_cond, " + " apply_renew_cond, " + " rcv_date_cond, " + " rcv_date_s, "
        + " rcv_date_e, " + " new_hldr_cond, " + " new_hldr_days, " + " new_hldr_card, "
        + " new_hldr_sup, " + " activate_chk_flag, " + " bir_mm_cond, " + " bir_mm01, "
        + " bir_mm02, " + " bir_mm03, " + " bir_mm04, " + " bir_mm05, " + " bir_mm06, "
        + " bir_mm07, " + " bir_mm08, " + " bir_mm09, " + " bir_mm10, " + " bir_mm11, "
        + " bir_mm12, " + " age_cond, " + " age_s, " + " age_e, " + " sex_flag, "
        + " credit_limit_cond, " + " credit_limit_s, " + " credit_limit_e, " + " use_limit_cond, "
        + " use_limit_s, " + " use_limit_e, " + " rc_credit_bal_cond, " + " rc_credit_bal_s, "
        + " rc_credit_bal_e, " + " rc_bal_rate_cond, " + " rc_bal_rate_s, " + " rc_bal_rate_e, "
        + " bonus_cond, " + " bonus_bp_s, " + " bonus_bp_e, " + " fund_cond, " + " fund_amt_s, "
        + " fund_amt_e, " + " owe_amt_cond, " + " owe_amt_months, " + " owe_amt_condition, "
        + " owe_amt, " + " credit_cond, " + " credit_month, " + " credit_type, "
        + " credit_condition, " + " credit_mcode, " + " block_code_sel, " + " block_code_cond, "
        + " class_code_sel, " + " addr_area_sel, " + " purch_date_cond, " + " purch_date_s, "
        + " purch_date_e, " + " purch_issue_mm, " + " system_date_mm, " + " purch_issue_dd, "
        + " bl_cond, " + " ca_cond, " + " it_cond, " + " id_cond, " + " ao_cond, " + " ot_cond, "
        + " dest_amt_cond, " + " dest_amt_type, " + " dest_amt_s, " + " dest_amt_e, "
        + " dest_time_cond, " + " purch_times_s, " + " purch_times_e, " + " ucaf_sel, "
        + " record_group_no, " + " excl_foreigner_cond, " + " excl_no_tm_cond, "
        + " excl_no_dm_cond, " + " excl_bank_emp_cond, " + " excl_no_edm_cond, "
        + " excl_no_sms_cond, " + " excl_no_mbullet_cond, " + " excl_list_cond, "
        + " excl_chgphone_cond, " + " chgphone_date_s, " + " chgphone_date_e, " + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("list_batch_no"), colStr("list_desc"), colStr("list_sel"),
        colStr("acct_type_sel"), colStr("vd_flag"), colStr("card_type_sel"), colStr("bin_type_sel"),
        colStr("group_code_sel"), colStr("id_dup_cond"), colStr("source_code_sel"),
        colStr("valid_card_flag"), colStr("valid_check_cond"), colStr("valid_stop_days"),
        colStr("excl_card_cond"), colStr("sup_check_flag"), colStr("expire_chg_cond"),
        colStr("apply_date_cond"), colStr("apply_date_s"), colStr("apply_date_e"),
        colStr("apply_excl_cond"), colStr("apply_renew_cond"), colStr("rcv_date_cond"),
        colStr("rcv_date_s"), colStr("rcv_date_e"), colStr("new_hldr_cond"),
        colStr("new_hldr_days"), colStr("new_hldr_card"), colStr("new_hldr_sup"),
        colStr("activate_chk_flag"), colStr("bir_mm_cond"), colStr("bir_mm01"), colStr("bir_mm02"),
        colStr("bir_mm03"), colStr("bir_mm04"), colStr("bir_mm05"), colStr("bir_mm06"),
        colStr("bir_mm07"), colStr("bir_mm08"), colStr("bir_mm09"), colStr("bir_mm10"),
        colStr("bir_mm11"), colStr("bir_mm12"), colStr("age_cond"), colStr("age_s"),
        colStr("age_e"), colStr("sex_flag"), colStr("credit_limit_cond"), colStr("credit_limit_s"),
        colStr("credit_limit_e"), colStr("use_limit_cond"), colStr("use_limit_s"),
        colStr("use_limit_e"), colStr("rc_credit_bal_cond"), colStr("rc_credit_bal_s"),
        colStr("rc_credit_bal_e"), colStr("rc_bal_rate_cond"), colStr("rc_bal_rate_s"),
        colStr("rc_bal_rate_e"), colStr("bonus_cond"), colStr("bonus_bp_s"), colStr("bonus_bp_e"),
        colStr("fund_cond"), colStr("fund_amt_s"), colStr("fund_amt_e"), colStr("owe_amt_cond"),
        colStr("owe_amt_months"), colStr("owe_amt_condition"), colStr("owe_amt"),
        colStr("credit_cond"), colStr("credit_month"), colStr("credit_type"),
        colStr("credit_condition"), colStr("credit_mcode"), colStr("block_code_sel"),
        colStr("block_code_cond"), colStr("class_code_sel"), colStr("addr_area_sel"),
        colStr("purch_date_cond"), colStr("purch_date_s"), colStr("purch_date_e"),
        colStr("purch_issue_mm"), colStr("system_date_mm"), colStr("purch_issue_dd"),
        colStr("bl_cond"), colStr("ca_cond"), colStr("it_cond"), colStr("id_cond"),
        colStr("ao_cond"), colStr("ot_cond"), colStr("dest_amt_cond"), colStr("dest_amt_type"),
        colStr("dest_amt_s"), colStr("dest_amt_e"), colStr("dest_time_cond"),
        colStr("purch_times_s"), colStr("purch_times_e"), colStr("ucaf_sel"),
        colStr("record_group_no"), colStr("excl_foreigner_cond"), colStr("excl_no_tm_cond"),
        colStr("excl_no_dm_cond"), colStr("excl_bank_emp_cond"), colStr("excl_no_edm_cond"),
        colStr("excl_no_sms_cond"), colStr("excl_no_mbullet_cond"), colStr("excl_list_cond"),
        colStr("excl_chgphone_cond"), colStr("chgphone_date_s"), colStr("chgphone_date_e"), "Y",
        wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " list_batch_no, " + " list_desc, " + " list_sel, " + " acct_type_sel, "
        + " vd_flag, " + " card_type_sel, " + " bin_type_sel, " + " group_code_sel, "
        + " id_dup_cond, " + " source_code_sel, " + " valid_card_flag, " + " valid_check_cond, "
        + " valid_stop_days, " + " excl_card_cond, " + " sup_check_flag, " + " expire_chg_cond, "
        + " apply_date_cond, " + " apply_date_s, " + " apply_date_e, " + " apply_excl_cond, "
        + " apply_renew_cond, " + " rcv_date_cond, " + " rcv_date_s, " + " rcv_date_e, "
        + " new_hldr_cond, " + " new_hldr_days, " + " new_hldr_card, " + " new_hldr_sup, "
        + " activate_chk_flag, " + " bir_mm_cond, " + " bir_mm01, " + " bir_mm02, " + " bir_mm03, "
        + " bir_mm04, " + " bir_mm05, " + " bir_mm06, " + " bir_mm07, " + " bir_mm08, "
        + " bir_mm09, " + " bir_mm10, " + " bir_mm11, " + " bir_mm12, " + " age_cond, " + " age_s, "
        + " age_e, " + " sex_flag, " + " credit_limit_cond, " + " credit_limit_s, "
        + " credit_limit_e, " + " use_limit_cond, " + " use_limit_s, " + " use_limit_e, "
        + " rc_credit_bal_cond, " + " rc_credit_bal_s, " + " rc_credit_bal_e, "
        + " rc_bal_rate_cond, " + " rc_bal_rate_s, " + " rc_bal_rate_e, " + " bonus_cond, "
        + " bonus_bp_s, " + " bonus_bp_e, " + " fund_cond, " + " fund_amt_s, " + " fund_amt_e, "
        + " owe_amt_cond, " + " owe_amt_months, " + " owe_amt_condition, " + " owe_amt, "
        + " credit_cond, " + " credit_month, " + " credit_type, " + " credit_condition, "
        + " credit_mcode, " + " block_code_sel, " + " block_code_cond, " + " class_code_sel, "
        + " addr_area_sel, " + " purch_date_cond, " + " purch_date_s, " + " purch_date_e, "
        + " purch_issue_mm, " + " system_date_mm, " + " purch_issue_dd, " + " bl_cond, "
        + " ca_cond, " + " it_cond, " + " id_cond, " + " ao_cond, " + " ot_cond, "
        + " dest_amt_cond, " + " dest_amt_type, " + " dest_amt_s, " + " dest_amt_e, "
        + " dest_time_cond, " + " purch_times_s, " + " purch_times_e, " + " ucaf_sel, "
        + " record_group_no, " + " excl_foreigner_cond, " + " excl_no_tm_cond, "
        + " excl_no_dm_cond, " + " excl_bank_emp_cond, " + " excl_no_edm_cond, "
        + " excl_no_sms_cond, " + " excl_no_mbullet_cond, " + " excl_list_cond, "
        + " excl_chgphone_cond, " + " chgphone_date_s, " + " chgphone_date_e, " + " apr_date, "
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
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "list_desc = ?, " + "list_sel = ?, "
        + "acct_type_sel = ?, " + "vd_flag = ?, " + "card_type_sel = ?, " + "bin_type_sel = ?, "
        + "group_code_sel = ?, " + "id_dup_cond = ?, " + "source_code_sel = ?, "
        + "valid_card_flag = ?, " + "valid_check_cond = ?, " + "valid_stop_days = ?, "
        + "excl_card_cond = ?, " + "sup_check_flag = ?, " + "expire_chg_cond = ?, "
        + "apply_date_cond = ?, " + "apply_date_s = ?, " + "apply_date_e = ?, "
        + "apply_excl_cond = ?, " + "apply_renew_cond = ?, " + "rcv_date_cond = ?, "
        + "rcv_date_s = ?, " + "rcv_date_e = ?, " + "new_hldr_cond = ?, " + "new_hldr_days = ?, "
        + "new_hldr_card = ?, " + "new_hldr_sup = ?, " + "activate_chk_flag = ?, "
        + "bir_mm_cond = ?, " + "bir_mm01 = ?, " + "bir_mm02 = ?, " + "bir_mm03 = ?, "
        + "bir_mm04 = ?, " + "bir_mm05 = ?, " + "bir_mm06 = ?, " + "bir_mm07 = ?, "
        + "bir_mm08 = ?, " + "bir_mm09 = ?, " + "bir_mm10 = ?, " + "bir_mm11 = ?, "
        + "bir_mm12 = ?, " + "age_cond = ?, " + "age_s = ?, " + "age_e = ?, " + "sex_flag = ?, "
        + "credit_limit_cond = ?, " + "credit_limit_s = ?, " + "credit_limit_e = ?, "
        + "use_limit_cond = ?, " + "use_limit_s = ?, " + "use_limit_e = ?, "
        + "rc_credit_bal_cond = ?, " + "rc_credit_bal_s = ?, " + "rc_credit_bal_e = ?, "
        + "rc_bal_rate_cond = ?, " + "rc_bal_rate_s = ?, " + "rc_bal_rate_e = ?, "
        + "bonus_cond = ?, " + "bonus_bp_s = ?, " + "bonus_bp_e = ?, " + "fund_cond = ?, "
        + "fund_amt_s = ?, " + "fund_amt_e = ?, " + "owe_amt_cond = ?, " + "owe_amt_months = ?, "
        + "owe_amt_condition = ?, " + "owe_amt = ?, " + "credit_cond = ?, " + "credit_month = ?, "
        + "credit_type = ?, " + "credit_condition = ?, " + "credit_mcode = ?, "
        + "block_code_sel = ?, " + "block_code_cond = ?, " + "class_code_sel = ?, "
        + "addr_area_sel = ?, " + "purch_date_cond = ?, " + "purch_date_s = ?, "
        + "purch_date_e = ?, " + "purch_issue_mm = ?, " + "system_date_mm = ?, "
        + "purch_issue_dd = ?, " + "bl_cond = ?, " + "ca_cond = ?, " + "it_cond = ?, "
        + "id_cond = ?, " + "ao_cond = ?, " + "ot_cond = ?, " + "dest_amt_cond = ?, "
        + "dest_amt_type = ?, " + "dest_amt_s = ?, " + "dest_amt_e = ?, " + "dest_time_cond = ?, "
        + "purch_times_s = ?, " + "purch_times_e = ?, " + "ucaf_sel = ?, " + "record_group_no = ?, "
        + "excl_foreigner_cond = ?, " + "excl_no_tm_cond = ?, " + "excl_no_dm_cond = ?, "
        + "excl_bank_emp_cond = ?, " + "excl_no_edm_cond = ?, " + "excl_no_sms_cond = ?, "
        + "excl_no_mbullet_cond = ?, " + "excl_list_cond = ?, " + "excl_chgphone_cond = ?, "
        + "chgphone_date_s = ?, " + "chgphone_date_e = ?, " + "crt_user  = ?, " + "crt_date  = ?, "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
        + "and   list_batch_no  = ? ";

    Object[] param = new Object[] {colStr("list_desc"), colStr("list_sel"), colStr("acct_type_sel"),
        colStr("vd_flag"), colStr("card_type_sel"), colStr("bin_type_sel"),
        colStr("group_code_sel"), colStr("id_dup_cond"), colStr("source_code_sel"),
        colStr("valid_card_flag"), colStr("valid_check_cond"), colStr("valid_stop_days"),
        colStr("excl_card_cond"), colStr("sup_check_flag"), colStr("expire_chg_cond"),
        colStr("apply_date_cond"), colStr("apply_date_s"), colStr("apply_date_e"),
        colStr("apply_excl_cond"), colStr("apply_renew_cond"), colStr("rcv_date_cond"),
        colStr("rcv_date_s"), colStr("rcv_date_e"), colStr("new_hldr_cond"),
        colStr("new_hldr_days"), colStr("new_hldr_card"), colStr("new_hldr_sup"),
        colStr("activate_chk_flag"), colStr("bir_mm_cond"), colStr("bir_mm01"), colStr("bir_mm02"),
        colStr("bir_mm03"), colStr("bir_mm04"), colStr("bir_mm05"), colStr("bir_mm06"),
        colStr("bir_mm07"), colStr("bir_mm08"), colStr("bir_mm09"), colStr("bir_mm10"),
        colStr("bir_mm11"), colStr("bir_mm12"), colStr("age_cond"), colStr("age_s"),
        colStr("age_e"), colStr("sex_flag"), colStr("credit_limit_cond"), colStr("credit_limit_s"),
        colStr("credit_limit_e"), colStr("use_limit_cond"), colStr("use_limit_s"),
        colStr("use_limit_e"), colStr("rc_credit_bal_cond"), colStr("rc_credit_bal_s"),
        colStr("rc_credit_bal_e"), colStr("rc_bal_rate_cond"), colStr("rc_bal_rate_s"),
        colStr("rc_bal_rate_e"), colStr("bonus_cond"), colStr("bonus_bp_s"), colStr("bonus_bp_e"),
        colStr("fund_cond"), colStr("fund_amt_s"), colStr("fund_amt_e"), colStr("owe_amt_cond"),
        colStr("owe_amt_months"), colStr("owe_amt_condition"), colStr("owe_amt"),
        colStr("credit_cond"), colStr("credit_month"), colStr("credit_type"),
        colStr("credit_condition"), colStr("credit_mcode"), colStr("block_code_sel"),
        colStr("block_code_cond"), colStr("class_code_sel"), colStr("addr_area_sel"),
        colStr("purch_date_cond"), colStr("purch_date_s"), colStr("purch_date_e"),
        colStr("purch_issue_mm"), colStr("system_date_mm"), colStr("purch_issue_dd"),
        colStr("bl_cond"), colStr("ca_cond"), colStr("it_cond"), colStr("id_cond"),
        colStr("ao_cond"), colStr("ot_cond"), colStr("dest_amt_cond"), colStr("dest_amt_type"),
        colStr("dest_amt_s"), colStr("dest_amt_e"), colStr("dest_time_cond"),
        colStr("purch_times_s"), colStr("purch_times_e"), colStr("ucaf_sel"),
        colStr("record_group_no"), colStr("excl_foreigner_cond"), colStr("excl_no_tm_cond"),
        colStr("excl_no_dm_cond"), colStr("excl_bank_emp_cond"), colStr("excl_no_edm_cond"),
        colStr("excl_no_sms_cond"), colStr("excl_no_mbullet_cond"), colStr("excl_list_cond"),
        colStr("excl_chgphone_cond"), colStr("chgphone_date_s"), colStr("chgphone_date_e"),
        colStr("crt_user"), colStr("crt_date"), wp.loginUser, aprFlag, colStr("mod_user"),
        colStr("mod_time"), colStr("mod_pgm"), colStr("list_batch_no")};

    rc = sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and list_batch_no = ? ";

    Object[] param = new Object[] {colStr("list_batch_no")};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4bBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_dm_bn_data " + "where 1 = 1 " + "and table_name  =  'MKT_DM_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("list_batch_no"),};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_dm_bn_data 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_dm_bn_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_DM_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("list_batch_no"),};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_dm_bn_data 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_dm_bn_data " + "select * " + "from  mkt_dm_bn_data_t "
        + "where 1 = 1 " + "and table_name  =  'MKT_DM_PARM' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("list_batch_no"),};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("新增 mkt_dm_bn_data 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4Dmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_dmbatch_list " + "where 1 = 1 " + "and list_batch_no = ? ";

    Object[] param = new Object[] {colStr("list_batch_no")};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_dmbatch_list 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4TDmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_dmbatch_list_t " + "where 1 = 1 " + "and list_batch_no = ? ";

    Object[] param = new Object[] {colStr("list_batch_no")};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_dmbatch_list 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertA4Dmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_dmbatch_list " + "select * " + "from  mkt_dmbatch_list_t "
        + "where 1 = 1 " + "and list_batch_no = ? ";

    Object[] param = new Object[] {colStr("list_batch_no")};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("新增 mkt_dmbatch_list 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDelete() {
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
