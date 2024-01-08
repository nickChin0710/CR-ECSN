/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6260Func extends FuncEdit {
  private String PROGNAME = "特店活動回饋參數檔處理程式108/12/12 V1.00.01";
  String activeCode;
  String orgControlTabName = "mkt_mcht_parm";
  String controlTabName = "mkt_mcht_parm_t";

  public Mktm6260Func(TarokoCommon wr) {
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
    String procTabName = "";
    procTabName = wp.itemStr("control_tab_name");
    strSql = " select " + " active_name, " + " stop_flag, " + " stop_date, " + " stop_desc, "
        + " active_type, " + " bonus_type, " + " tax_flag, " + " fund_code, " + " effect_months, "
        + " purchase_date_s, " + " purchase_date_e, " + " feedback_date, " + " feedback_key_sel, "
        + " issue_date_cond, " + " issue_date_s, " + " issue_date_e, " + " new_hldr_sel, "
        + " new_hldr_days, " + " new_group_cond, " + " new_hldr_card, " + " new_hldr_sup, "
        + " acct_type_sel, " + " group_code_sel, " + " record_cond, " + " record_group_no, "
        + " record_purc_flag, " + " record_n1_days, " + " record_n2_days, " + " bl_cond, "
        + " it_cond, " + " merchant_sel, " + " mcht_group_sel, " + " in_merchant_sel, "
        + " in_mcht_group_sel, " + " mcht_in_cond, " + " mcht_in_per_amt, " + " mcht_in_cnt, "
        + " mcht_in_amt, " + " mcc_code_sel, " + " pos_entry_sel, " + " per_amt_cond, "
        + " per_amt, " + " sum_cnt_cond, " + " sum_cnt, " + " sum_amt_cond, " + " sum_amt, "
        + " feedback_rate, " + " feedback_add_amt, " + " exchange_amt, " + " feedback_lmtamt_cond, "
        + " feedback_lmt_amt, " + " feedback_lmtcnt_cond, " + " feedback_lmt_cnt, "
        + " day_lmtamt_cond, " + " day_lmt_amt, " + " day_lmtcnt_cond, " + " day_lmt_cnt, "
        + " times_lmtamt_cond, " + " times_lmt_amt, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      activeCode = wp.itemStr("active_code");
    } else {
      activeCode = wp.itemStr("active_code");
    }
    if (!wp.itemStr("stop_flag").equals("Y"))
      wp.itemSet("stop_flag", "N");
    if (!wp.itemStr("issue_date_cond").equals("Y"))
      wp.itemSet("issue_date_cond", "N");
    if (!wp.itemStr("new_group_cond").equals("Y"))
      wp.itemSet("new_group_cond", "N");
    if (!wp.itemStr("new_hldr_card").equals("Y"))
      wp.itemSet("new_hldr_card", "N");
    if (!wp.itemStr("new_hldr_sup").equals("Y"))
      wp.itemSet("new_hldr_sup", "N");
    if (!wp.itemStr("record_cond").equals("Y"))
      wp.itemSet("record_cond", "N");
    if (!wp.itemStr("bl_cond").equals("Y"))
      wp.itemSet("bl_cond", "N");
    if (!wp.itemStr("it_cond").equals("Y"))
      wp.itemSet("it_cond", "N");
    if (!wp.itemStr("per_amt_cond").equals("Y"))
      wp.itemSet("per_amt_cond", "N");
    if (!wp.itemStr("sum_cnt_cond").equals("Y"))
      wp.itemSet("sum_cnt_cond", "N");
    if (!wp.itemStr("sum_amt_cond").equals("Y"))
      wp.itemSet("sum_amt_cond", "N");
    if (!wp.itemStr("feedback_lmtamt_cond").equals("Y"))
      wp.itemSet("feedback_lmtamt_cond", "N");
    if (!wp.itemStr("feedback_lmtcnt_cond").equals("Y"))
      wp.itemSet("feedback_lmtcnt_cond", "N");
    if (!wp.itemStr("day_lmtamt_cond").equals("Y"))
      wp.itemSet("day_lmtamt_cond", "N");
    if (!wp.itemStr("day_lmtcnt_cond").equals("Y"))
      wp.itemSet("day_lmtcnt_cond", "N");
    if (!wp.itemStr("times_lmtamt_cond").equals("Y"))
      wp.itemSet("times_lmtamt_cond", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_date_s").length() == 0) {
        errmsg("[消費期間:消費期間間起日必須輸入 !");
        return;
      }

      if (wp.itemStr("issue_date_cond").equals("Y")) {
        if ((wp.itemStr("issue_date_s").length() == 0)
            || (wp.itemStr("issue_date_e").length() == 0))
          errmsg("[發卡期間:發卡期間起迄日必須輸入 !");
        return;
      }

      if (wp.itemStr("feedback_date").length() == 0) {
        errmsg("[回饋日期:回饋日期必須輸入 !");
        return;
      }

      if (wp.itemStr("new_hldr_cond").equals("Y")) {
        if (wp.itemStr("new_hldr_days").length() == 0)
          wp.itemSet("new_hldr_days", "0");
        if (wp.itemNum("new_hldr_days") == 0) {
          errmsg("[新卡友 :核卡日期：N 日不可為 0 !");
          return;
        }
        if ((wp.itemStr("new_hldr_card").equals("N")) && (wp.itemStr("new_hldr_sup").equals("N"))) {
          errmsg("[新卡友 : 正卡, 附卡必須有一輸入 !");
          return;
        }
      }

      if (wp.itemStr("fst_apply_cond").equals("Y")) {
        if ((wp.itemStr("fst_applt_card").equals("N"))
            && (wp.itemStr("fst_apply_sup").equals("N"))) {
          errmsg("[首次申辦 : 正卡, 附卡必須有一輸入 !");
          return;
        }
      }


      if ((!wp.itemStr("bl_cond").equals("Y")) && (!wp.itemStr("it_cond").equals("Y"))) {
        errmsg("[消費本金類] 至少要選一個!");
        return;
      }

      if (wp.itemStr("record_cond").equals("Y")) {
        if (wp.itemStr("record_group_no").length() == 0) {
          errmsg("[登錄群組必須輸入!");
          return;
        }
        if (wp.itemStr("record_purc_flag").equals("2")) {
          if (wp.itemStr("record_n1_days").length() == 0)
            wp.itemSet("record_n1_days", "0");
          if (wp.itemNum("record_n1_days") == 0) {
            errmsg("[交易檢核：消費登錄: 消費日>=登錄日 N 日 不可為 0!");
            return;
          }
          if (wp.itemStr("record_n2_days").length() == 0)
            wp.itemSet("record_n2_days", "0");
          if (wp.itemNum("record_n2_days") == 0) {
            errmsg("[交易檢核：消費登錄: 消費日<=登錄日 N 日 不可為 0!");
            return;
          }
        }
      }

      if (wp.itemStr("active_type").equals("2")) {
        if (wp.itemStr("fund_code").length() == 0) {
          errmsg("[回饋型態: 基金代碼 必須輸入 !");
          return;
        }
      }

      if (wp.itemStr("per_amt_cond").equals("Y")) {
        if (wp.itemStr("per_amt").length() == 0)
          wp.itemSet("per_amt", "0");
        if (wp.itemNum("per_amt") == 0) {
          errmsg("[單筆最低消費金額] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("purch_rec_amt_cond").equals("Y")) {
        if (wp.itemStr("purch_rec_amt").length() == 0)
          wp.itemSet("purch_rec_amt", "0");
        if (wp.itemNum("purch_rec_amt") == 0) {
          errmsg("[累積最低消費筆數] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("sum_cnt_cond").equals("Y")) {
        if (wp.itemStr("sum_cnt").length() == 0)
          wp.itemSet("sum_cnt", "0");
        if (wp.itemNum("sum_cnt") == 0) {
          errmsg("[累積最低消費筆數] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("feedback_rate").length() == 0)
        wp.itemSet("feedback_rate", "0");
      if (wp.itemNum("feedback_rate") == 0) {
        errmsg("[回饋交易金額的% ] 不可為 0 !");
        return;
      }

      if (wp.itemStr("feedback_lmtamt_cond").equals("Y")) {
        if (wp.itemStr("feedback_lmt_amt").length() == 0)
          wp.itemSet("feedback_lmt_amt", "0");
        if (wp.itemNum("feedback_lmt_amt") == 0) {
          errmsg("[回饋金額上限] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("feedback_imtcnt_cond").equals("Y")) {
        if (wp.itemStr("feedback_lmt_cnt").length() == 0)
          wp.itemSet("feedback_lmt_cnt", "0");
        if (wp.itemNum("feedback_lmt_ant") == 0) {
          errmsg("[回饋筆數上限] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("day_lmtamt_cond").equals("Y")) {
        if (wp.itemStr("day_lmt_amt").length() == 0)
          wp.itemSet("day_lmt_amt", "0");
        if (wp.itemNum("day_lmt_amt") == 0) {
          errmsg("[ 每日回饋金額上限] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("day_imtcnt_cond").equals("Y")) {
        if (wp.itemStr("day_lmt_cnt").length() == 0)
          wp.itemSet("day_lmt_cnt", "0");
        if (wp.itemNum("day_lmt_ant") == 0) {
          errmsg("[ 每日回饋筆數上限] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("times_lmtamt_cond").equals("Y")) {
        if (wp.itemStr("times_lmt_amt").length() == 0)
          wp.itemSet("times_lmt_amt", "0");
        if (wp.itemNum("times_lmt_amt") == 0) {
          errmsg("[ 每筆回饋金額上限] 不可為 0 !");
          return;
        }
      }
    }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("purchase_date_s") && (!wp.itemEmpty("purchase_date_e")))
        if (wp.itemStr("purchase_date_s").compareTo(wp.itemStr("purchase_date_e")) > 0) {
          errmsg("消費日期：[" + wp.itemStr("purchase_date_s") + "]>[" + wp.itemStr("purchase_date_e")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("issue_date_s") && (!wp.itemEmpty("purchase_date_e")))
        if (wp.itemStr("issue_date_s").compareTo(wp.itemStr("purchase_date_e")) > 0) {
          errmsg("[" + wp.itemStr("issue_date_s") + "]>[" + wp.itemStr("purchase_date_e")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if (checkDecnum(wp.itemStr("mcht_in_per_amt"), 11, 3) != 0) {
      errmsg("　　　　　最低消費額 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("mcht_in_amt"), 11, 3) != 0) {
      errmsg("　累計消費額 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("per_amt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("sum_cnt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("sum_amt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_rate"), 3, 2) != 0) {
      errmsg("回饋交易金額的 格式超出範圍 : [3][2]");
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

    dbInsertD2T();
    dbInsertI2T();

    strSql = " insert into  " + controlTabName + " (" + " active_code, " + " aud_type, "
        + " active_name, " + " stop_flag, " + " stop_date, " + " stop_desc, " + " active_type, "
        + " bonus_type, " + " tax_flag, " + " fund_code, " + " effect_months, "
        + " purchase_date_s, " + " purchase_date_e, " + " feedback_date, " + " feedback_key_sel, "
        + " issue_date_cond, " + " issue_date_s, " + " issue_date_e, " + " new_hldr_sel, "
        + " new_hldr_days, " + " new_group_cond, " + " new_hldr_card, " + " new_hldr_sup, "
        + " acct_type_sel, " + " group_code_sel, " + " record_cond, " + " record_group_no, "
        + " record_purc_flag, " + " record_n1_days, " + " record_n2_days, " + " bl_cond, "
        + " it_cond, " + " merchant_sel, " + " mcht_group_sel, " + " in_merchant_sel, "
        + " in_mcht_group_sel, " + " mcht_in_cond, " + " mcht_in_per_amt, " + " mcht_in_cnt, "
        + " mcht_in_amt, " + " mcc_code_sel, " + " pos_entry_sel, " + " per_amt_cond, "
        + " per_amt, " + " sum_cnt_cond, " + " sum_cnt, " + " sum_amt_cond, " + " sum_amt, "
        + " feedback_rate, " + " feedback_add_amt, " + " exchange_amt, " + " feedback_lmtamt_cond, "
        + " feedback_lmt_amt, " + " feedback_lmtcnt_cond, " + " feedback_lmt_cnt, "
        + " day_lmtamt_cond, " + " day_lmt_amt, " + " day_lmtcnt_cond, " + " day_lmt_cnt, "
        + " times_lmtamt_cond, " + " times_lmt_amt, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {activeCode, wp.itemStr("aud_type"), wp.itemStr("active_name"),
        wp.itemStr("stop_flag"), wp.itemStr("stop_date"), wp.itemStr("stop_desc"),
        wp.itemStr("active_type"), wp.itemStr("bonus_type"), wp.itemStr("tax_flag"),
        wp.itemStr("fund_code"), wp.itemNum("effect_months"), wp.itemStr("purchase_date_s"),
        wp.itemStr("purchase_date_e"), wp.itemStr("feedback_date"), wp.itemStr("feedback_key_sel"),
        wp.itemStr("issue_date_cond"), wp.itemStr("issue_date_s"), wp.itemStr("issue_date_e"),
        wp.itemStr("new_hldr_sel"), wp.itemNum("new_hldr_days"), wp.itemStr("new_group_cond"),
        wp.itemStr("new_hldr_card"), wp.itemStr("new_hldr_sup"), wp.itemStr("acct_type_sel"),
        wp.itemStr("group_code_sel"), wp.itemStr("record_cond"), wp.itemStr("record_group_no"),
        wp.itemStr("record_purc_flag"), wp.itemNum("record_n1_days"), wp.itemNum("record_n2_days"),
        wp.itemStr("bl_cond"), wp.itemStr("it_cond"), wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"), wp.itemStr("in_merchant_sel"),
        wp.itemStr("in_mcht_group_sel"), wp.itemStr("mcht_in_cond"), wp.itemNum("mcht_in_per_amt"),
        wp.itemNum("mcht_in_cnt"), wp.itemNum("mcht_in_amt"), wp.itemStr("mcc_code_sel"),
        wp.itemStr("pos_entry_sel"), wp.itemStr("per_amt_cond"), wp.itemNum("per_amt"),
        wp.itemStr("sum_cnt_cond"), wp.itemNum("sum_cnt"), wp.itemStr("sum_amt_cond"),
        wp.itemNum("sum_amt"), wp.itemNum("feedback_rate"), wp.itemNum("feedback_add_amt"),
        wp.itemNum("exchange_amt"), wp.itemStr("feedback_lmtamt_cond"),
        wp.itemNum("feedback_lmt_amt"), wp.itemStr("feedback_lmtcnt_cond"),
        wp.itemNum("feedback_lmt_cnt"), wp.itemStr("day_lmtamt_cond"), wp.itemNum("day_lmt_amt"),
        wp.itemStr("day_lmtcnt_cond"), wp.itemNum("day_lmt_cnt"), wp.itemStr("times_lmtamt_cond"),
        wp.itemNum("times_lmt_amt"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_BN_DATA_T " + "select * " + "from MKT_BN_DATA "
        + "where table_name  =  'MKT_MCHT_PARM' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);


    return 1;
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

    strSql = "update " + controlTabName + " set " + "active_name = ?, " + "stop_flag = ?, "
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
        + "mcht_in_per_amt = ?, " + "mcht_in_cnt = ?, " + "mcht_in_amt = ?, " + "mcc_code_sel = ?, "
        + "pos_entry_sel = ?, " + "per_amt_cond = ?, " + "per_amt = ?, " + "sum_cnt_cond = ?, "
        + "sum_cnt = ?, " + "sum_amt_cond = ?, " + "sum_amt = ?, " + "feedback_rate = ?, "
        + "feedback_add_amt = ?, " + "exchange_amt = ?, " + "feedback_lmtamt_cond = ?, "
        + "feedback_lmt_amt = ?, " + "feedback_lmtcnt_cond = ?, " + "feedback_lmt_cnt = ?, "
        + "day_lmtamt_cond = ?, " + "day_lmt_amt = ?, " + "day_lmtcnt_cond = ?, "
        + "day_lmt_cnt = ?, " + "times_lmtamt_cond = ?, " + "times_lmt_amt = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("active_name"), wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"), wp.itemStr("stop_desc"), wp.itemStr("active_type"),
        wp.itemStr("bonus_type"), wp.itemStr("tax_flag"), wp.itemStr("fund_code"),
        wp.itemNum("effect_months"), wp.itemStr("purchase_date_s"), wp.itemStr("purchase_date_e"),
        wp.itemStr("feedback_date"), wp.itemStr("feedback_key_sel"), wp.itemStr("issue_date_cond"),
        wp.itemStr("issue_date_s"), wp.itemStr("issue_date_e"), wp.itemStr("new_hldr_sel"),
        wp.itemNum("new_hldr_days"), wp.itemStr("new_group_cond"), wp.itemStr("new_hldr_card"),
        wp.itemStr("new_hldr_sup"), wp.itemStr("acct_type_sel"), wp.itemStr("group_code_sel"),
        wp.itemStr("record_cond"), wp.itemStr("record_group_no"), wp.itemStr("record_purc_flag"),
        wp.itemNum("record_n1_days"), wp.itemNum("record_n2_days"), wp.itemStr("bl_cond"),
        wp.itemStr("it_cond"), wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"),
        wp.itemStr("in_merchant_sel"), wp.itemStr("in_mcht_group_sel"), wp.itemStr("mcht_in_cond"),
        wp.itemNum("mcht_in_per_amt"), wp.itemNum("mcht_in_cnt"), wp.itemNum("mcht_in_amt"),
        wp.itemStr("mcc_code_sel"), wp.itemStr("pos_entry_sel"), wp.itemStr("per_amt_cond"),
        wp.itemNum("per_amt"), wp.itemStr("sum_cnt_cond"), wp.itemNum("sum_cnt"),
        wp.itemStr("sum_amt_cond"), wp.itemNum("sum_amt"), wp.itemNum("feedback_rate"),
        wp.itemNum("feedback_add_amt"), wp.itemNum("exchange_amt"),
        wp.itemStr("feedback_lmtamt_cond"), wp.itemNum("feedback_lmt_amt"),
        wp.itemStr("feedback_lmtcnt_cond"), wp.itemNum("feedback_lmt_cnt"),
        wp.itemStr("day_lmtamt_cond"), wp.itemNum("day_lmt_amt"), wp.itemStr("day_lmtcnt_cond"),
        wp.itemNum("day_lmt_cnt"), wp.itemStr("times_lmtamt_cond"), wp.itemNum("times_lmt_amt"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
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

    dbInsertD2T();

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

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
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_BN_DATA_T " + " where table_name  =  'MKT_MCHT_PARM' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_BN_DATA_T 錯誤");

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
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6260_gncd"))
      dataType = "F";
    if (wp.respHtml.equals("mktm6260_actp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6260_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6260_aaa1"))
      dataType = "8";
    if (wp.respHtml.equals("mktm6260_aaat"))
      dataType = "10";
    if (wp.respHtml.equals("mktm6260_mccd"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6260_posn"))
      dataType = "11";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_MCHT_PARM', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6260_gncd"))
      dataType = "F";
    if (wp.respHtml.equals("mktm6260_actp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6260_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6260_aaa1"))
      dataType = "8";
    if (wp.respHtml.equals("mktm6260_aaat"))
      dataType = "10";
    if (wp.respHtml.equals("mktm6260_mccd"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6260_posn"))
      dataType = "11";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_MCHT_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_MCHT_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6260_mrcd"))
      dataType = "7";
    if (wp.respHtml.equals("mktm6260_inmc"))
      dataType = "9";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_MCHT_PARM', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6260_mrcd"))
      dataType = "7";
    if (wp.respHtml.equals("mktm6260_inmc"))
      dataType = "9";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_MCHT_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_MCHT_PARM'  ";
    sqlExec(strSql, param);


    return 1;

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
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa1(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_MCHT_PARM", wp.itemStr("active_code"), "7"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
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
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa3(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_MCHT_PARM", wp.itemStr("active_code"), "9"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
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

    wp.logSql = false;
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

} // End of class
