/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/02/20  V1.00.01   Ray Ho        Initial                              *
* 109-04-23  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0110Func extends FuncEdit {
  private String PROGNAME = "DM參數資料檔維護作業處理程式108/02/20 V1.00.01";
  String listBatchNo;
  String orgControlTabName = "mkt_dm_parm";
  String controlTabName = "mkt_dm_parm_t";

  public Mktm0110Func(TarokoCommon wr) {
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
    String proTabName = "";
    proTabName = wp.itemStr("control_tab_name");
    strSql = " select " + " list_desc, " + " list_sel, " + " acct_type_sel, " + " vd_flag, "
        + " card_type_sel, " + " bin_type_sel, " + " group_code_sel, " + " id_dup_cond, "
        + " source_code_sel, " + " valid_card_flag, " + " valid_check_cond, " + " valid_stop_days, "
        + " excl_card_cond, " + " sup_check_flag, " + " expire_chg_cond, " + " apply_date_cond, "
        + " apply_date_s, " + " apply_date_e, " + " apply_excl_cond, " + " apply_renew_cond, "
        + " rcv_date_cond, " + " rcv_date_s, " + " rcv_date_e, " + " new_hldr_cond, "
        + " new_hldr_days, " + " new_hldr_card, " + " new_hldr_sup, " + " activate_chk_flag, "
        + " bir_mm_cond, " + " bir_mm01, " + " bir_mm02, " + " bir_mm03, " + " bir_mm04, "
        + " bir_mm05, " + " bir_mm06, " + " bir_mm07, " + " bir_mm08, " + " bir_mm09, "
        + " bir_mm10, " + " bir_mm11, " + " bir_mm12, " + " age_cond, " + " age_s, " + " age_e, "
        + " sex_flag, " + " credit_limit_cond, " + " credit_limit_s, " + " credit_limit_e, "
        + " use_limit_cond, " + " use_limit_s, " + " use_limit_e, " + " rc_credit_bal_cond, "
        + " rc_credit_bal_s, " + " rc_credit_bal_e, " + " rc_bal_rate_cond, " + " rc_bal_rate_s, "
        + " rc_bal_rate_e, " + " bonus_cond, " + " bonus_bp_s, " + " bonus_bp_e, " + " fund_cond, "
        + " fund_amt_s, " + " fund_amt_e, " + " owe_amt_cond, " + " owe_amt_months, "
        + " owe_amt_condition, " + " owe_amt, " + " credit_cond, " + " credit_month, "
        + " credit_type, " + " credit_condition, " + " credit_mcode, " + " block_code_sel, "
        + " block_code_cond, " + " class_code_sel, " + " addr_area_sel, " + " purch_date_cond, "
        + " purch_date_s, " + " purch_date_e, " + " purch_issue_mm, " + " system_date_mm, "
        + " purch_issue_dd, " + " bl_cond, " + " ca_cond, " + " it_cond, " + " id_cond, "
        + " ao_cond, " + " ot_cond, " + " dest_amt_cond, " + " dest_amt_type, " + " dest_amt_s, "
        + " dest_amt_e, " + " dest_time_cond, " + " purch_times_s, " + " purch_times_e, "
        + " mcc_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + " ucaf_sel, " + " eci_sel, "
        + " pos_entry_sel, " + " purch_rcd_flag, " + " currency_sel, " + " record_group_no, "
        + " excl_foreigner_cond, " + " excl_no_tm_cond, " + " excl_no_dm_cond, "
        + " excl_bank_emp_cond, " + " excl_no_edm_cond, " + " excl_no_sms_cond, "
        + " excl_no_mbullet_cond, " + " excl_list_cond, " + " excl_chgphone_cond, "
        + " chgphone_date_s, " + " chgphone_date_e, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + proTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + proTabName + " 錯誤");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      listBatchNo = wp.itemStr("list_batch_no");
    } else {
      listBatchNo = wp.itemStr("list_batch_no");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (listBatchNo.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where list_batch_no = ? ";
          Object[] param = new Object[] {listBatchNo};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[名單批號:] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (listBatchNo.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where list_batch_no = ? ";
        Object[] param = new Object[] {listBatchNo};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[名單批號:] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("vd_flag").equals("Y"))
      wp.itemSet("vd_flag", "N");
    if (!wp.itemStr("id_dup_cond").equals("Y"))
      wp.itemSet("id_dup_cond", "N");
    if (!wp.itemStr("valid_check_cond").equals("Y"))
      wp.itemSet("valid_check_cond", "N");
    if (!wp.itemStr("excl_card_cond").equals("Y"))
      wp.itemSet("excl_card_cond", "N");
    if (!wp.itemStr("expire_chg_cond").equals("Y"))
      wp.itemSet("expire_chg_cond", "N");
    if (!wp.itemStr("apply_date_cond").equals("Y"))
      wp.itemSet("apply_date_cond", "N");
    if (!wp.itemStr("apply_excl_cond").equals("Y"))
      wp.itemSet("apply_excl_cond", "N");
    if (!wp.itemStr("apply_renew_cond").equals("Y"))
      wp.itemSet("apply_renew_cond", "N");
    if (!wp.itemStr("rcv_date_cond").equals("Y"))
      wp.itemSet("rcv_date_cond", "N");
    if (!wp.itemStr("new_hldr_cond").equals("Y"))
      wp.itemSet("new_hldr_cond", "N");
    if (!wp.itemStr("new_hldr_card").equals("Y"))
      wp.itemSet("new_hldr_card", "N");
    if (!wp.itemStr("new_hldr_sup").equals("Y"))
      wp.itemSet("new_hldr_sup", "N");
    if (!wp.itemStr("bir_mm_cond").equals("Y"))
      wp.itemSet("bir_mm_cond", "N");
    if (!wp.itemStr("bir_mm01").equals("Y"))
      wp.itemSet("bir_mm01", "N");
    if (!wp.itemStr("bir_mm02").equals("Y"))
      wp.itemSet("bir_mm02", "N");
    if (!wp.itemStr("bir_mm03").equals("Y"))
      wp.itemSet("bir_mm03", "N");
    if (!wp.itemStr("bir_mm04").equals("Y"))
      wp.itemSet("bir_mm04", "N");
    if (!wp.itemStr("bir_mm05").equals("Y"))
      wp.itemSet("bir_mm05", "N");
    if (!wp.itemStr("bir_mm06").equals("Y"))
      wp.itemSet("bir_mm06", "N");
    if (!wp.itemStr("bir_mm07").equals("Y"))
      wp.itemSet("bir_mm07", "N");
    if (!wp.itemStr("bir_mm08").equals("Y"))
      wp.itemSet("bir_mm08", "N");
    if (!wp.itemStr("bir_mm09").equals("Y"))
      wp.itemSet("bir_mm09", "N");
    if (!wp.itemStr("bir_mm10").equals("Y"))
      wp.itemSet("bir_mm10", "N");
    if (!wp.itemStr("bir_mm11").equals("Y"))
      wp.itemSet("bir_mm11", "N");
    if (!wp.itemStr("bir_mm12").equals("Y"))
      wp.itemSet("bir_mm12", "N");
    if (!wp.itemStr("age_cond").equals("Y"))
      wp.itemSet("age_cond", "N");
    if (!wp.itemStr("credit_limit_cond").equals("Y"))
      wp.itemSet("credit_limit_cond", "N");
    if (!wp.itemStr("use_limit_cond").equals("Y"))
      wp.itemSet("use_limit_cond", "N");
    if (!wp.itemStr("rc_credit_bal_cond").equals("Y"))
      wp.itemSet("rc_credit_bal_cond", "N");
    if (!wp.itemStr("rc_bal_rate_cond").equals("Y"))
      wp.itemSet("rc_bal_rate_cond", "N");
    if (!wp.itemStr("bonus_cond").equals("Y"))
      wp.itemSet("bonus_cond", "N");
    if (!wp.itemStr("fund_cond").equals("Y"))
      wp.itemSet("fund_cond", "N");
    if (!wp.itemStr("owe_amt_cond").equals("Y"))
      wp.itemSet("owe_amt_cond", "N");
    if (!wp.itemStr("credit_cond").equals("Y"))
      wp.itemSet("credit_cond", "N");
    if (!wp.itemStr("bl_cond").equals("Y"))
      wp.itemSet("bl_cond", "N");
    if (!wp.itemStr("ca_cond").equals("Y"))
      wp.itemSet("ca_cond", "N");
    if (!wp.itemStr("it_cond").equals("Y"))
      wp.itemSet("it_cond", "N");
    if (!wp.itemStr("id_cond").equals("Y"))
      wp.itemSet("id_cond", "N");
    if (!wp.itemStr("ao_cond").equals("Y"))
      wp.itemSet("ao_cond", "N");
    if (!wp.itemStr("ot_cond").equals("Y"))
      wp.itemSet("ot_cond", "N");
    if (!wp.itemStr("dest_amt_cond").equals("Y"))
      wp.itemSet("dest_amt_cond", "N");
    if (!wp.itemStr("dest_time_cond").equals("Y"))
      wp.itemSet("dest_time_cond", "N");
    if (!wp.itemStr("excl_foreigner_cond").equals("Y"))
      wp.itemSet("excl_foreigner_cond", "N");
    if (!wp.itemStr("excl_no_tm_cond").equals("Y"))
      wp.itemSet("excl_no_tm_cond", "N");
    if (!wp.itemStr("excl_no_dm_cond").equals("Y"))
      wp.itemSet("excl_no_dm_cond", "N");
    if (!wp.itemStr("excl_bank_emp_cond").equals("Y"))
      wp.itemSet("excl_bank_emp_cond", "N");
    if (!wp.itemStr("excl_no_edm_cond").equals("Y"))
      wp.itemSet("excl_no_edm_cond", "N");
    if (!wp.itemStr("excl_no_sms_cond").equals("Y"))
      wp.itemSet("excl_no_sms_cond", "N");
    if (!wp.itemStr("excl_no_mbullet_cond").equals("Y"))
      wp.itemSet("excl_no_mbullet_cond", "N");
    if (!wp.itemStr("excl_list_cond").equals("Y"))
      wp.itemSet("excl_list_cond", "N");
    if (!wp.itemStr("excl_chgphone_cond").equals("Y"))
      wp.itemSet("excl_chgphone_cond", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("apply_date_s") && (!wp.itemEmpty("APPLY_DATE_E")))
        if (wp.itemStr("apply_date_s").compareTo(wp.itemStr("APPLY_DATE_E")) > 0) {
          errmsg("09.核卡期間:[" + wp.itemStr("apply_date_s") + "]>[" + wp.itemStr("APPLY_DATE_E")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("rcv_date_s") && (!wp.itemEmpty("RCV_DATE_E")))
        if (wp.itemStr("rcv_date_s").compareTo(wp.itemStr("RCV_DATE_E")) > 0) {
          errmsg("25.收件日期:[" + wp.itemStr("rcv_date_s") + "]>[" + wp.itemStr("RCV_DATE_E")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("age_s").length() == 0)
        wp.itemSet("age_s", "0");
      if (wp.itemStr("AGE_E").length() == 0)
        wp.itemSet("AGE_E", "0");
      if (Double.parseDouble(wp.itemStr("age_s")) > Double.parseDouble(wp.itemStr("AGE_E"))
          && (Double.parseDouble(wp.itemStr("AGE_E")) != 0)) {
        errmsg("11.年齡區間:          　(" + wp.itemStr("age_s") + ")>age_e(" + wp.itemStr("AGE_E")
            + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("credit_limit_s").length() == 0)
        wp.itemSet("credit_limit_s", "0");
      if (wp.itemStr("CREDIT_LIMIT_E").length() == 0)
        wp.itemSet("CREDIT_LIMIT_E", "0");
      if (Double.parseDouble(wp.itemStr("credit_limit_s")) > Double
          .parseDouble(wp.itemStr("CREDIT_LIMIT_E"))
          && (Double.parseDouble(wp.itemStr("CREDIT_LIMIT_E")) != 0)) {
        errmsg("credit_limit_s(" + wp.itemStr("credit_limit_s") + ")>credit_limit_e("
            + wp.itemStr("CREDIT_LIMIT_E") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("use_limit_s").length() == 0)
        wp.itemSet("use_limit_s", "0");
      if (wp.itemStr("USE_LIMIT_E").length() == 0)
        wp.itemSet("USE_LIMIT_E", "0");
      if (Double.parseDouble(wp.itemStr("use_limit_s")) > Double
          .parseDouble(wp.itemStr("USE_LIMIT_E"))
          && (Double.parseDouble(wp.itemStr("USE_LIMIT_E")) != 0)) {
        errmsg("use_limit_s(" + wp.itemStr("use_limit_s") + ")>use_limit_e("
            + wp.itemStr("USE_LIMIT_E") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("rc_credit_bal_s").length() == 0)
        wp.itemSet("rc_credit_bal_s", "0");
      if (wp.itemStr("RC_CREDIT_BAL_E").length() == 0)
        wp.itemSet("RC_CREDIT_BAL_E", "0");
      if (Double.parseDouble(wp.itemStr("rc_credit_bal_s")) > Double
          .parseDouble(wp.itemStr("RC_CREDIT_BAL_E"))
          && (Double.parseDouble(wp.itemStr("RC_CREDIT_BAL_E")) != 0)) {
        errmsg("rc_credit_bal_s(" + wp.itemStr("rc_credit_bal_s") + ")>rc_credit_bal_e("
            + wp.itemStr("RC_CREDIT_BAL_E") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("rc_bal_rate_s").length() == 0)
        wp.itemSet("rc_bal_rate_s", "0");
      if (wp.itemStr("RC_BAL_RATE_E").length() == 0)
        wp.itemSet("RC_BAL_RATE_E", "0");
      if (Double.parseDouble(wp.itemStr("rc_bal_rate_s")) > Double
          .parseDouble(wp.itemStr("RC_BAL_RATE_E"))
          && (Double.parseDouble(wp.itemStr("RC_BAL_RATE_E")) != 0)) {
        errmsg("rc_bal_rate_s(" + wp.itemStr("rc_bal_rate_s") + ")>rc_bal_rate_e("
            + wp.itemStr("RC_BAL_RATE_E") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("bonus_bp_s").length() == 0)
        wp.itemSet("bonus_bp_s", "0");
      if (wp.itemStr("BONUS_BP_E").length() == 0)
        wp.itemSet("BONUS_BP_E", "0");
      if (Double.parseDouble(wp.itemStr("bonus_bp_s")) > Double
          .parseDouble(wp.itemStr("BONUS_BP_E"))
          && (Double.parseDouble(wp.itemStr("BONUS_BP_E")) != 0)) {
        errmsg("bonus_bp_s(" + wp.itemStr("bonus_bp_s") + ")>bonus_bp_e(" + wp.itemStr("BONUS_BP_E")
            + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("fund_amt_s").length() == 0)
        wp.itemSet("fund_amt_s", "0");
      if (wp.itemStr("FUND_AMT_E").length() == 0)
        wp.itemSet("FUND_AMT_E", "0");
      if (Double.parseDouble(wp.itemStr("fund_amt_s")) > Double
          .parseDouble(wp.itemStr("FUND_AMT_E"))
          && (Double.parseDouble(wp.itemStr("FUND_AMT_E")) != 0)) {
        errmsg(" 　(" + wp.itemStr("fund_amt_s") + ")>fund_amt_e(" + wp.itemStr("FUND_AMT_E")
            + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("purch_date_s") && (!wp.itemEmpty("PURCH_DATE_E")))
        if (wp.itemStr("purch_date_s").compareTo(wp.itemStr("PURCH_DATE_E")) > 0) {
          errmsg(
              "[" + wp.itemStr("purch_date_s") + "]>[" + wp.itemStr("PURCH_DATE_E") + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("chgphone_date_s") && (!wp.itemEmpty("CHGPHONE_DATE_E")))
        if (wp.itemStr("chgphone_date_s").compareTo(wp.itemStr("CHGPHONE_DATE_E")) > 0) {
          errmsg("排除[" + wp.itemStr("chgphone_date_s") + "]>[" + wp.itemStr("CHGPHONE_DATE_E")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if (checkDecnum(wp.itemStr("rc_bal_rate_s"), 3, 4) != 0) {
      errmsg(" 格式超出範圍 : [3][4]");
      return;
    }

    if (checkDecnum(wp.itemStr("rc_bal_rate_e"), 3, 4) != 0) {
      errmsg(" 格式超出範圍 : [3][4]");
      return;
    }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD4T();
    dbInsertI4T();
    dbInsertD2T();
    dbInsertI2T();

    strSql = " insert into  " + controlTabName + " (" + " list_batch_no, " + " aud_type, "
        + " list_desc, " + " list_sel, " + " acct_type_sel, " + " vd_flag, " + " card_type_sel, "
        + " bin_type_sel, " + " group_code_sel, " + " id_dup_cond, " + " source_code_sel, "
        + " valid_card_flag, " + " valid_check_cond, " + " valid_stop_days, " + " excl_card_cond, "
        + " sup_check_flag, " + " expire_chg_cond, " + " apply_date_cond, " + " apply_date_s, "
        + " apply_date_e, " + " apply_excl_cond, " + " apply_renew_cond, " + " rcv_date_cond, "
        + " rcv_date_s, " + " rcv_date_e, " + " new_hldr_cond, " + " new_hldr_days, "
        + " new_hldr_card, " + " new_hldr_sup, " + " activate_chk_flag, " + " bir_mm_cond, "
        + " bir_mm01, " + " bir_mm02, " + " bir_mm03, " + " bir_mm04, " + " bir_mm05, "
        + " bir_mm06, " + " bir_mm07, " + " bir_mm08, " + " bir_mm09, " + " bir_mm10, "
        + " bir_mm11, " + " bir_mm12, " + " age_cond, " + " age_s, " + " age_e, " + " sex_flag, "
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
        + " dest_time_cond, " + " purch_times_s, " + " purch_times_e, " + " mcc_code_sel, "
        + " merchant_sel, " + " mcht_group_sel, " + " ucaf_sel, " + " eci_sel, "
        + " pos_entry_sel, " + " purch_rcd_flag, " + " currency_sel, " + " record_group_no, "
        + " excl_foreigner_cond, " + " excl_no_tm_cond, " + " excl_no_dm_cond, "
        + " excl_bank_emp_cond, " + " excl_no_edm_cond, " + " excl_no_sms_cond, "
        + " excl_no_mbullet_cond, " + " excl_list_cond, " + " excl_chgphone_cond, "
        + " chgphone_date_s, " + " chgphone_date_e, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {listBatchNo, wp.itemStr("aud_type"), wp.itemStr("list_desc"),
        wp.itemStr("list_sel"), wp.itemStr("acct_type_sel"), wp.itemStr("vd_flag"),
        wp.itemStr("card_type_sel"), wp.itemStr("bin_type_sel"), wp.itemStr("group_code_sel"),
        wp.itemStr("id_dup_cond"), wp.itemStr("source_code_sel"), wp.itemStr("valid_card_flag"),
        wp.itemStr("valid_check_cond"), wp.itemNum("valid_stop_days"), wp.itemStr("excl_card_cond"),
        wp.itemStr("sup_check_flag"), wp.itemStr("expire_chg_cond"), wp.itemStr("apply_date_cond"),
        wp.itemStr("apply_date_s"), wp.itemStr("apply_date_e"), wp.itemStr("apply_excl_cond"),
        wp.itemStr("apply_renew_cond"), wp.itemStr("rcv_date_cond"), wp.itemStr("rcv_date_s"),
        wp.itemStr("rcv_date_e"), wp.itemStr("new_hldr_cond"), wp.itemNum("new_hldr_days"),
        wp.itemStr("new_hldr_card"), wp.itemStr("new_hldr_sup"), wp.itemStr("activate_chk_flag"),
        wp.itemStr("bir_mm_cond"), wp.itemStr("bir_mm01"), wp.itemStr("bir_mm02"),
        wp.itemStr("bir_mm03"), wp.itemStr("bir_mm04"), wp.itemStr("bir_mm05"),
        wp.itemStr("bir_mm06"), wp.itemStr("bir_mm07"), wp.itemStr("bir_mm08"),
        wp.itemStr("bir_mm09"), wp.itemStr("bir_mm10"), wp.itemStr("bir_mm11"),
        wp.itemStr("bir_mm12"), wp.itemStr("age_cond"), wp.itemNum("age_s"), wp.itemNum("age_e"),
        wp.itemStr("sex_flag"), wp.itemStr("credit_limit_cond"), wp.itemNum("credit_limit_s"),
        wp.itemNum("credit_limit_e"), wp.itemStr("use_limit_cond"), wp.itemNum("use_limit_s"),
        wp.itemNum("use_limit_e"), wp.itemStr("rc_credit_bal_cond"), wp.itemNum("rc_credit_bal_s"),
        wp.itemNum("rc_credit_bal_e"), wp.itemStr("rc_bal_rate_cond"), wp.itemNum("rc_bal_rate_s"),
        wp.itemNum("rc_bal_rate_e"), wp.itemStr("bonus_cond"), wp.itemNum("bonus_bp_s"),
        wp.itemNum("bonus_bp_e"), wp.itemStr("fund_cond"), wp.itemNum("fund_amt_s"),
        wp.itemNum("fund_amt_e"), wp.itemStr("owe_amt_cond"), wp.itemNum("owe_amt_months"),
        wp.itemStr("owe_amt_condition"), wp.itemNum("owe_amt"), wp.itemStr("credit_cond"),
        wp.itemNum("credit_month"), wp.itemStr("credit_type"), wp.itemStr("credit_condition"),
        wp.itemNum("credit_mcode"), wp.itemStr("block_code_sel"), wp.itemStr("block_code_cond"),
        wp.itemStr("class_code_sel"), wp.itemStr("addr_area_sel"), wp.itemStr("purch_date_cond"),
        wp.itemStr("purch_date_s"), wp.itemStr("purch_date_e"), wp.itemNum("purch_issue_mm"),
        wp.itemNum("system_date_mm"), wp.itemNum("purch_issue_dd"), wp.itemStr("bl_cond"),
        wp.itemStr("ca_cond"), wp.itemStr("it_cond"), wp.itemStr("id_cond"), wp.itemStr("ao_cond"),
        wp.itemStr("ot_cond"), wp.itemStr("dest_amt_cond"), wp.itemStr("dest_amt_type"),
        wp.itemNum("dest_amt_s"), wp.itemNum("dest_amt_e"), wp.itemStr("dest_time_cond"),
        wp.itemNum("purch_times_s"), wp.itemNum("purch_times_e"), wp.itemStr("mcc_code_sel"),
        wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"), wp.itemStr("ucaf_sel"),
        wp.itemStr("eci_sel"), wp.itemStr("pos_entry_sel"), wp.itemStr("purch_rcd_flag"),
        wp.itemStr("currency_sel"), wp.itemStr("record_group_no"),
        wp.itemStr("excl_foreigner_cond"), wp.itemStr("excl_no_tm_cond"),
        wp.itemStr("excl_no_dm_cond"), wp.itemStr("excl_bank_emp_cond"),
        wp.itemStr("excl_no_edm_cond"), wp.itemStr("excl_no_sms_cond"),
        wp.itemStr("excl_no_mbullet_cond"), wp.itemStr("excl_list_cond"),
        wp.itemStr("excl_chgphone_cond"), wp.itemStr("chgphone_date_s"),
        wp.itemStr("chgphone_date_e"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI4T() {
    msgOK();

    strSql = "insert into MKT_DMBATCH_LIST_T " + "select * " + "from MKT_DMBATCH_LIST "
        + "where list_batch_no = ? " + "and   list_sel = ? " + "";

    Object[] param = new Object[] {wp.itemStr("list_batch_no"), wp.itemStr("list_sel"),};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("新增 MKT_DMBATCH_LIST_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_DM_BN_DATA_T " + "select * " + "from MKT_DM_BN_DATA "
        + "where table_name  =  'MKT_DM_PARM' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("list_batch_no"),};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("新增 MKT_DM_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "aud_type = ?, " + "list_desc = ?, "
        + "list_sel = ?, " + "acct_type_sel = ?, " + "vd_flag = ?, " + "card_type_sel = ?, "
        + "bin_type_sel = ?, " + "group_code_sel = ?, " + "id_dup_cond = ?, "
        + "source_code_sel = ?, " + "valid_card_flag = ?, " + "valid_check_cond = ?, "
        + "valid_stop_days = ?, " + "excl_card_cond = ?, " + "sup_check_flag = ?, "
        + "expire_chg_cond = ?, " + "apply_date_cond = ?, " + "apply_date_s = ?, "
        + "apply_date_e = ?, " + "apply_excl_cond = ?, " + "apply_renew_cond = ?, "
        + "rcv_date_cond = ?, " + "rcv_date_s = ?, " + "rcv_date_e = ?, " + "new_hldr_cond = ?, "
        + "new_hldr_days = ?, " + "new_hldr_card = ?, " + "new_hldr_sup = ?, "
        + "activate_chk_flag = ?, " + "bir_mm_cond = ?, " + "bir_mm01 = ?, " + "bir_mm02 = ?, "
        + "bir_mm03 = ?, " + "bir_mm04 = ?, " + "bir_mm05 = ?, " + "bir_mm06 = ?, "
        + "bir_mm07 = ?, " + "bir_mm08 = ?, " + "bir_mm09 = ?, " + "bir_mm10 = ?, "
        + "bir_mm11 = ?, " + "bir_mm12 = ?, " + "age_cond = ?, " + "age_s = ?, " + "age_e = ?, "
        + "sex_flag = ?, " + "credit_limit_cond = ?, " + "credit_limit_s = ?, "
        + "credit_limit_e = ?, " + "use_limit_cond = ?, " + "use_limit_s = ?, "
        + "use_limit_e = ?, " + "rc_credit_bal_cond = ?, " + "rc_credit_bal_s = ?, "
        + "rc_credit_bal_e = ?, " + "rc_bal_rate_cond = ?, " + "rc_bal_rate_s = ?, "
        + "rc_bal_rate_e = ?, " + "bonus_cond = ?, " + "bonus_bp_s = ?, " + "bonus_bp_e = ?, "
        + "fund_cond = ?, " + "fund_amt_s = ?, " + "fund_amt_e = ?, " + "owe_amt_cond = ?, "
        + "owe_amt_months = ?, " + "owe_amt_condition = ?, " + "owe_amt = ?, " + "credit_cond = ?, "
        + "credit_month = ?, " + "credit_type = ?, " + "credit_condition = ?, "
        + "credit_mcode = ?, " + "block_code_sel = ?, " + "block_code_cond = ?, "
        + "class_code_sel = ?, " + "addr_area_sel = ?, " + "purch_date_cond = ?, "
        + "purch_date_s = ?, " + "purch_date_e = ?, " + "purch_issue_mm = ?, "
        + "system_date_mm = ?, " + "purch_issue_dd = ?, " + "bl_cond = ?, " + "ca_cond = ?, "
        + "it_cond = ?, " + "id_cond = ?, " + "ao_cond = ?, " + "ot_cond = ?, "
        + "dest_amt_cond = ?, " + "dest_amt_type = ?, " + "dest_amt_s = ?, " + "dest_amt_e = ?, "
        + "dest_time_cond = ?, " + "purch_times_s = ?, " + "purch_times_e = ?, "
        + "mcc_code_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "ucaf_sel = ?, "
        + "eci_sel = ?, " + "pos_entry_sel = ?, " + "purch_rcd_flag = ?, " + "currency_sel = ?, "
        + "record_group_no = ?, " + "excl_foreigner_cond = ?, " + "excl_no_tm_cond = ?, "
        + "excl_no_dm_cond = ?, " + "excl_bank_emp_cond = ?, " + "excl_no_edm_cond = ?, "
        + "excl_no_sms_cond = ?, " + "excl_no_mbullet_cond = ?, " + "excl_list_cond = ?, "
        + "excl_chgphone_cond = ?, " + "chgphone_date_s = ?, " + "chgphone_date_e = ?, "
        + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("aud_type"), wp.itemStr("list_desc"),
        wp.itemStr("list_sel"), wp.itemStr("acct_type_sel"), wp.itemStr("vd_flag"),
        wp.itemStr("card_type_sel"), wp.itemStr("bin_type_sel"), wp.itemStr("group_code_sel"),
        wp.itemStr("id_dup_cond"), wp.itemStr("source_code_sel"), wp.itemStr("valid_card_flag"),
        wp.itemStr("valid_check_cond"), wp.itemNum("valid_stop_days"), wp.itemStr("excl_card_cond"),
        wp.itemStr("sup_check_flag"), wp.itemStr("expire_chg_cond"), wp.itemStr("apply_date_cond"),
        wp.itemStr("apply_date_s"), wp.itemStr("apply_date_e"), wp.itemStr("apply_excl_cond"),
        wp.itemStr("apply_renew_cond"), wp.itemStr("rcv_date_cond"), wp.itemStr("rcv_date_s"),
        wp.itemStr("rcv_date_e"), wp.itemStr("new_hldr_cond"), wp.itemNum("new_hldr_days"),
        wp.itemStr("new_hldr_card"), wp.itemStr("new_hldr_sup"), wp.itemStr("activate_chk_flag"),
        wp.itemStr("bir_mm_cond"), wp.itemStr("bir_mm01"), wp.itemStr("bir_mm02"),
        wp.itemStr("bir_mm03"), wp.itemStr("bir_mm04"), wp.itemStr("bir_mm05"),
        wp.itemStr("bir_mm06"), wp.itemStr("bir_mm07"), wp.itemStr("bir_mm08"),
        wp.itemStr("bir_mm09"), wp.itemStr("bir_mm10"), wp.itemStr("bir_mm11"),
        wp.itemStr("bir_mm12"), wp.itemStr("age_cond"), wp.itemNum("age_s"), wp.itemNum("age_e"),
        wp.itemStr("sex_flag"), wp.itemStr("credit_limit_cond"), wp.itemNum("credit_limit_s"),
        wp.itemNum("credit_limit_e"), wp.itemStr("use_limit_cond"), wp.itemNum("use_limit_s"),
        wp.itemNum("use_limit_e"), wp.itemStr("rc_credit_bal_cond"), wp.itemNum("rc_credit_bal_s"),
        wp.itemNum("rc_credit_bal_e"), wp.itemStr("rc_bal_rate_cond"), wp.itemNum("rc_bal_rate_s"),
        wp.itemNum("rc_bal_rate_e"), wp.itemStr("bonus_cond"), wp.itemNum("bonus_bp_s"),
        wp.itemNum("bonus_bp_e"), wp.itemStr("fund_cond"), wp.itemNum("fund_amt_s"),
        wp.itemNum("fund_amt_e"), wp.itemStr("owe_amt_cond"), wp.itemNum("owe_amt_months"),
        wp.itemStr("owe_amt_condition"), wp.itemNum("owe_amt"), wp.itemStr("credit_cond"),
        wp.itemNum("credit_month"), wp.itemStr("credit_type"), wp.itemStr("credit_condition"),
        wp.itemNum("credit_mcode"), wp.itemStr("block_code_sel"), wp.itemStr("block_code_cond"),
        wp.itemStr("class_code_sel"), wp.itemStr("addr_area_sel"), wp.itemStr("purch_date_cond"),
        wp.itemStr("purch_date_s"), wp.itemStr("purch_date_e"), wp.itemNum("purch_issue_mm"),
        wp.itemNum("system_date_mm"), wp.itemNum("purch_issue_dd"), wp.itemStr("bl_cond"),
        wp.itemStr("ca_cond"), wp.itemStr("it_cond"), wp.itemStr("id_cond"), wp.itemStr("ao_cond"),
        wp.itemStr("ot_cond"), wp.itemStr("dest_amt_cond"), wp.itemStr("dest_amt_type"),
        wp.itemNum("dest_amt_s"), wp.itemNum("dest_amt_e"), wp.itemStr("dest_time_cond"),
        wp.itemNum("purch_times_s"), wp.itemNum("purch_times_e"), wp.itemStr("mcc_code_sel"),
        wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"), wp.itemStr("ucaf_sel"),
        wp.itemStr("eci_sel"), wp.itemStr("pos_entry_sel"), wp.itemStr("purch_rcd_flag"),
        wp.itemStr("currency_sel"), wp.itemStr("record_group_no"),
        wp.itemStr("excl_foreigner_cond"), wp.itemStr("excl_no_tm_cond"),
        wp.itemStr("excl_no_dm_cond"), wp.itemStr("excl_bank_emp_cond"),
        wp.itemStr("excl_no_edm_cond"), wp.itemStr("excl_no_sms_cond"),
        wp.itemStr("excl_no_mbullet_cond"), wp.itemStr("excl_list_cond"),
        wp.itemStr("excl_chgphone_cond"), wp.itemStr("chgphone_date_s"),
        wp.itemStr("chgphone_date_e"), wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD4T();
    dbInsertD2T();

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertD4T() {
    msgOK();

    strSql = "delete MKT_DMBATCH_LIST_T " + "WHERE list_batch_no = ? " + "and   list_sel = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("list_batch_no"), wp.itemStr("list_sel"),};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("刪除 MKT_DMBATCH_LIST_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_DM_BN_DATA_T " + " where table_name  =  'MKT_DM_PARM' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("list_batch_no"),};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("刪除 MKT_DM_BN_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }

  // ************************************************************************
  public int dbInsert_I4() throws Exception {
    msgOK();

    strSql = "insert into MKT_DMBATCH_LIST_T ( " + "list_batch_no," + "list_sel," + "data_code,"
        + "tx_amt," + " mod_time, " + " mod_pgm " + ") values (" + "?,?,?,?," + " sysdate, " + " ? "
        + ")";

    Object[] param = new Object[] {wp.itemStr("list_batch_no"), wp.itemStr("list_sel"),
        varsStr("data_code"), varsStr("tx_amt"), wp.modPgm()};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("新增 MKT_DMBATCH_LIST_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDelete_D4() {
    msgOK();

    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("list_batch_no"), wp.itemStr("list_sel")};
    if (sqlRowcount("MKT_DMBATCH_LIST_T", "where list_batch_no = ? " + "and   list_sel = ? ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_DMBATCH_LIST_T " + "where list_batch_no = ?  " + "and   list_sel = ?  ";
    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("刪除 MKT_DMBATCH_LIST_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0110_acty"))
      dataType = "01";
    if (wp.respHtml.equals("mktm0110_cdtp"))
      dataType = "02";
    if (wp.respHtml.equals("mktm0110_cdno"))
      dataType = "03";
    if (wp.respHtml.equals("mktm0110_gpcd"))
      dataType = "04";
    if (wp.respHtml.equals("mktm0110_srcd"))
      dataType = "05";
    if (wp.respHtml.equals("mktm0110_grcd"))
      dataType = "11";
    if (wp.respHtml.equals("mktm0110_pymt"))
      dataType = "19";
    if (wp.respHtml.equals("mktm0110_fzcd"))
      dataType = "06";
    if (wp.respHtml.equals("mktm0110_cdmn"))
      dataType = "09";
    if (wp.respHtml.equals("mktm0110_mccd"))
      dataType = "07";
    if (wp.respHtml.equals("mktm0110_aaa1"))
      dataType = "34";
    if (wp.respHtml.equals("mktm0110_ucaf"))
      dataType = "35";
    if (wp.respHtml.equals("mktm0110_deci"))
      dataType = "36";
    if (wp.respHtml.equals("mktm0110_posn"))
      dataType = "37";
    if (wp.respHtml.equals("mktm0110_sfbb"))
      dataType = "21";
    if (wp.respHtml.equals("mktm0110_exli"))
      dataType = "12";
    strSql = "insert into MKT_DM_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_DM_PARM', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("list_batch_no"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("新增 MKT_DM_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDelete_D2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0110_acty"))
      dataType = "01";
    if (wp.respHtml.equals("mktm0110_cdtp"))
      dataType = "02";
    if (wp.respHtml.equals("mktm0110_cdno"))
      dataType = "03";
    if (wp.respHtml.equals("mktm0110_gpcd"))
      dataType = "04";
    if (wp.respHtml.equals("mktm0110_srcd"))
      dataType = "05";
    if (wp.respHtml.equals("mktm0110_grcd"))
      dataType = "11";
    if (wp.respHtml.equals("mktm0110_pymt"))
      dataType = "19";
    if (wp.respHtml.equals("mktm0110_fzcd"))
      dataType = "06";
    if (wp.respHtml.equals("mktm0110_cdmn"))
      dataType = "09";
    if (wp.respHtml.equals("mktm0110_mccd"))
      dataType = "07";
    if (wp.respHtml.equals("mktm0110_aaa1"))
      dataType = "34";
    if (wp.respHtml.equals("mktm0110_ucaf"))
      dataType = "35";
    if (wp.respHtml.equals("mktm0110_deci"))
      dataType = "36";
    if (wp.respHtml.equals("mktm0110_posn"))
      dataType = "37";
    if (wp.respHtml.equals("mktm0110_sfbb"))
      dataType = "21";
    if (wp.respHtml.equals("mktm0110_exli"))
      dataType = "12";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("list_batch_no")};
    if (sqlRowcount("MKT_DM_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_DM_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_DM_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_DM_PARM'  ";
    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("刪除 MKT_DM_BN_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInserI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0110_psar"))
      dataType = "10";
    if (wp.respHtml.equals("mktm0110_mrcd"))
      dataType = "08";
    strSql = "insert into MKT_DM_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_DM_PARM', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("list_batch_no"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("新增 MKT_DM_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0110_psar"))
      dataType = "10";
    if (wp.respHtml.equals("mktm0110_mrcd"))
      dataType = "08";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("list_batch_no")};
    if (sqlRowcount("MKT_DM_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_DM_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_DM_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_DM_PARM'  ";
    rc = sqlExec(strSql, param);

    if (rc != 1)
      errmsg("刪除 MKT_DM_BN_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertI2List(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2List(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where list_batch_no = ? " + "and   list_sel = ? ";

    Object[] param = new Object[] {wp.itemStr("list_batch_no"), wp.itemStr("list_sel")};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa1(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_DM_PARM", wp.itemStr("list_batch_no"), "08"};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2Aaa3(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa3(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_DM_PARM", wp.itemStr("list_batch_no"), "12"};

    rc = sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg) {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10]))
      errMsg[10] = "0";
    if (!comm.isNumber(errMsg[1]))
      errMsg[1] = "0";
    if (!comm.isNumber(errMsg[2]))
      errMsg[2] = "0";

    strSql = " insert into ecs_media_errlog (" + " crt_date, " + " crt_time, " + " file_name, "
        + " unit_code, " + " main_desc, " + " error_seq, " + " error_desc, " + " line_seq, "
        + " column_seq, " + " column_data, " + " trans_seqno, " + " column_desc, "
        + " program_code, " + " mod_time, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," // 10
                                                                                                   // record
        + "?,?,?," // 4 trvotfd
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, wp.itemStr("zz_file_name"),
        comr.getObjectOwner("3", wp.modPgm()), errMsg[0], Integer.valueOf(errMsg[1]), errMsg[4],
        Integer.valueOf(errMsg[10]), Integer.valueOf(errMsg[2]), errMsg[3], tranSeqStr, errMsg[5],
        wp.modPgm(), wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_media_errlog 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt) {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql = " insert into ecs_notify_log (" + " crt_date, " + " crt_time, " + " unit_code, "
        + " obj_type, " + " notify_head, " + " notify_name, " + " notify_desc1, "
        + " notify_desc2, " + " trans_seqno, " + " mod_time, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?," // 9 record
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, comr.getObjectOwner("3", wp.modPgm()),
        "3", "媒體檔轉入資料有誤(只記錄前100筆)", "媒體檔名:" + wp.itemStr("zz_file_name"),
        "程式 " + wp.modPgm() + " 轉 " + wp.itemStr("zz_file_name") + " 有" + errorCnt + " 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤", tranSeqStr, wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_modify_log 錯誤");
    return rc;
  }
  // ************************************************************************

}  // End of class
