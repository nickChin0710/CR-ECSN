/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-24  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0380Func extends FuncEdit {
  private String PROGNAME = "雙幣卡外幣刷卡金回饋參數檔維護處理程式108/12/12 V1.00.01";
  String fundCode;
  String orgControlTabName = "cyc_dc_fund_parm";
  String controlTabName = "cyc_dc_fund_parm_t";

  public Mktm0380Func(TarokoCommon wr) {
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
    strSql = " select " + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, "
        + " stop_flag, " + " stop_date, " + " stop_desc, " + " curr_code, " + " feedback_month_s, "
        + " feedback_month_e, " + " effect_months, " + " new_hldr_cond, " + " new_hldr_days, "
        + " new_group_cond, " + " new_hldr_card, " + " new_hldr_sup, " + " source_code_sel, "
        + " merchant_sel, " + " mcht_group_sel, " + " platform_kind_sel, " + " group_card_sel, " + " group_code_sel, "
        + " purchase_amt_s1, " + " purchase_amt_e1, " + " feedback_rate_1, " + " purchase_amt_s2, "
        + " purchase_amt_e2, " + " feedback_rate_2, " + " purchase_amt_s3, " + " purchase_amt_e3, "
        + " feedback_rate_3, " + " purchase_amt_s4, " + " purchase_amt_e4, " + " feedback_rate_4, "
        + " purchase_amt_s5, " + " purchase_amt_e5, " + " feedback_rate_5, " + " feedback_lmt, "
        + " issue_cond, " + " issue_date_s, " + " issue_date_e, " + " issue_flag, "
        + " issue_num_1, " + " issue_num_2, " + " issue_num_3, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
        + " group_oppost_cond "
        + " from "
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
      fundCode = wp.itemStr("fund_code");
    } else {
      fundCode = wp.itemStr("fund_code");
    }
    if (!wp.itemStr("group_oppost_cond").equals("Y")) wp.itemSet("group_oppost_cond","N");
    if (!wp.itemStr("stop_flag").equals("Y"))
      wp.itemSet("stop_flag", "N");
    if (!wp.itemStr("new_hldr_cond").equals("Y"))
      wp.itemSet("new_hldr_cond", "N");
    if (!wp.itemStr("new_group_cond").equals("Y"))
      wp.itemSet("new_group_cond", "N");
    if (!wp.itemStr("new_hldr_card").equals("Y"))
      wp.itemSet("new_hldr_card", "N");
    if (!wp.itemStr("new_hldr_sup").equals("Y"))
      wp.itemSet("new_hldr_sup", "N");
    if (!wp.itemStr("issue_cond").equals("Y"))
      wp.itemSet("issue_cond", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("issue_cond").equals("Y"))
        if ((wp.itemStr("issue_date_s").length() == 0)
            || (wp.itemStr("issue_date_e").length() == 0)) {
          errmsg("發卡條件：發卡期間 不可空白!");
          return;
        }
      if (wp.itemStr("fund_name").length() == 0) {
        errmsg("刷卡金名稱：刷卡金名稱 不可空白!");
        return;
      }
      if (wp.itemStr("fund_crt_date_s").length() == 0) {
        errmsg("活動期間：活動期間-起 不可空白!");
        return;
      }
      if (wp.itemStr("feedback_month_s").length() == 0) {
        errmsg("刷卡金產生年月：刷卡金產生年月-起 不可空白!");
        return;
      }



    }

    if ((!wp.itemStr("control_tab_name").equals(orgControlTabName))
        && (wp.itemStr("aud_type").equals("A"))) {
      strSql = "select type_name " + " from vmkt_fund_name " + " where fund_code =  ? ";
      Object[] param = new Object[] {wp.itemStr("kk_fund_code")};
      sqlSelect(strSql, param);

      if (sqlRowNum > 0) {
        errmsg("[" + colStr("type_name") + "] 已使用本刷卡金代碼!");
        return;
      }
    }
    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("fund_crt_date_s") && (!wp.itemEmpty("FUND_CRT_DATE_E")))
        if (wp.itemStr("fund_crt_date_s").compareTo(wp.itemStr("FUND_CRT_DATE_E")) > 0) {
          errmsg("活動期間：[" + wp.itemStr("fund_crt_date_s") + "]>[" + wp.itemStr("FUND_CRT_DATE_E")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("feedback_month_s") && (!wp.itemEmpty("FEEDBACK_MONTH_E")))
        if (wp.itemStr("feedback_month_s").compareTo(wp.itemStr("FEEDBACK_MONTH_E")) > 0) {
          errmsg("刷卡金產生年月：[" + wp.itemStr("feedback_month_s") + "]>["
              + wp.itemStr("FEEDBACK_MONTH_E") + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s1").length() == 0)
        wp.itemSet("purchase_amt_s1", "0");
      if (wp.itemStr("purchase_amt_e1").length() == 0)
        wp.itemSet("purchase_amt_e1", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s1")) > Double
          .parseDouble(wp.itemStr("purchase_amt_e1"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_e1")) != 0)) {
        errmsg("1.(" + wp.itemStr("purchase_amt_s1") + ")>purchase_amt_e1("
            + wp.itemStr("purchase_amt_e1") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e1").length() == 0)
        wp.itemSet("purchase_amt_e1", "0");
      if (wp.itemStr("purchase_amt_s2").length() == 0)
        wp.itemSet("purchase_amt_s2", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e1")) >= Double
          .parseDouble(wp.itemStr("purchase_amt_s2"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_s2")) != 0)) {
        errmsg("purchase_amt_e1(" + wp.itemStr("purchase_amt_e1") + ")>=purchase_amt_s2("
            + wp.itemStr("purchase_amt_s2") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s2").length() == 0)
        wp.itemSet("purchase_amt_s2", "0");
      if (wp.itemStr("purchase_amt_e2").length() == 0)
        wp.itemSet("purchase_amt_e2", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s2")) > Double
          .parseDouble(wp.itemStr("purchase_amt_e2"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_e2")) != 0)) {
        errmsg("2.(" + wp.itemStr("purchase_amt_s2") + ")>purchase_amt_e2("
            + wp.itemStr("purchase_amt_e2") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e2").length() == 0)
        wp.itemSet("purchase_amt_e2", "0");
      if (wp.itemStr("purchase_amt_s3").length() == 0)
        wp.itemSet("purchase_amt_s3", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e2")) >= Double
          .parseDouble(wp.itemStr("purchase_amt_s3"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_s3")) != 0)) {
        errmsg("purchase_amt_e2(" + wp.itemStr("purchase_amt_e2") + ")>=purchase_amt_s3("
            + wp.itemStr("purchase_amt_s3") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s3").length() == 0)
        wp.itemSet("purchase_amt_s3", "0");
      if (wp.itemStr("purchase_amt_e3").length() == 0)
        wp.itemSet("purchase_amt_e3", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s3")) > Double
          .parseDouble(wp.itemStr("purchase_amt_e3"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_e3")) != 0)) {
        errmsg("3.(" + wp.itemStr("purchase_amt_s3") + ")>purchase_amt_e3("
            + wp.itemStr("purchase_amt_e3") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e3").length() == 0)
        wp.itemSet("purchase_amt_e3", "0");
      if (wp.itemStr("purchase_amt_s4").length() == 0)
        wp.itemSet("purchase_amt_s4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e3")) >= Double
          .parseDouble(wp.itemStr("purchase_amt_s4"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_s4")) != 0)) {
        errmsg("purchase_amt_e3(" + wp.itemStr("purchase_amt_e3") + ")>=purchase_amt_s4("
            + wp.itemStr("purchase_amt_s4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s4").length() == 0)
        wp.itemSet("purchase_amt_s4", "0");
      if (wp.itemStr("purchase_amt_e4").length() == 0)
        wp.itemSet("purchase_amt_e4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s4")) > Double
          .parseDouble(wp.itemStr("purchase_amt_e4"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_e4")) != 0)) {
        errmsg("4.(" + wp.itemStr("purchase_amt_s4") + ")>purchase_amt_e4("
            + wp.itemStr("purchase_amt_e4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e4").length() == 0)
        wp.itemSet("purchase_amt_e4", "0");
      if (wp.itemStr("purchase_amt_s5").length() == 0)
        wp.itemSet("purchase_amt_s5", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e4")) >= Double
          .parseDouble(wp.itemStr("purchase_amt_s5"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_s5")) != 0)) {
        errmsg("purchase_amt_e4(" + wp.itemStr("purchase_amt_e4") + ")>=purchase_amt_s5("
            + wp.itemStr("purchase_amt_s5") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s5").length() == 0)
        wp.itemSet("purchase_amt_s5", "0");
      if (wp.itemStr("purchase_amt_e5").length() == 0)
        wp.itemSet("purchase_amt_e5", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s5")) > Double
          .parseDouble(wp.itemStr("purchase_amt_e5"))
          && (Double.parseDouble(wp.itemStr("purchase_amt_e5")) != 0)) {
        errmsg("5.(" + wp.itemStr("purchase_amt_s5") + ")>purchase_amt_e5("
            + wp.itemStr("purchase_amt_e5") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("issue_date_s") && (!wp.itemEmpty("ISSUE_DATE_E")))
        if (wp.itemStr("issue_date_s").compareTo(wp.itemStr("ISSUE_DATE_E")) > 0) {
          errmsg(
              "[" + wp.itemStr("issue_date_s") + "]>[" + wp.itemStr("ISSUE_DATE_E") + "] 起迄值錯誤!");
          return;
        }
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s1"), 11, 3) != 0) {
      errmsg("1. 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e1"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_rate_1"), 3, 2) != 0) {
      errmsg(" 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s2"), 11, 3) != 0) {
      errmsg("2. 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e2"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_rate_2"), 3, 2) != 0) {
      errmsg(" 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s3"), 11, 3) != 0) {
      errmsg("3. 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e3"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_rate_3"), 3, 2) != 0) {
      errmsg(" 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s4"), 11, 3) != 0) {
      errmsg("4. 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e4"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_rate_4"), 3, 2) != 0) {
      errmsg(" 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s5"), 11, 3) != 0) {
      errmsg("5. 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e5"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_rate_5"), 3, 2) != 0) {
      errmsg(" 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_lmt"), 12, 2) != 0) {
      errmsg("回饋上限： 格式超出範圍 : [12][2]");
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

    strSql = " insert into  " + controlTabName + " (" + " fund_code, " + " aud_type, "
        + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, " + " stop_flag, "
        + " stop_date, " + " stop_desc, " + " curr_code, " + " feedback_month_s, "
        + " feedback_month_e, " + " effect_months, " + " new_hldr_cond, " + " new_hldr_days, "
        + " new_group_cond, " + " new_hldr_card, " + " new_hldr_sup, " + " source_code_sel, "
        + " merchant_sel, " + " mcht_group_sel, " + " platform_kind_sel, " + " group_card_sel, " + " group_code_sel, "
        + " purchase_amt_s1, " + " purchase_amt_e1, " + " feedback_rate_1, " + " purchase_amt_s2, "
        + " purchase_amt_e2, " + " feedback_rate_2, " + " purchase_amt_s3, " + " purchase_amt_e3, "
        + " feedback_rate_3, " + " purchase_amt_s4, " + " purchase_amt_e4, " + " feedback_rate_4, "
        + " purchase_amt_s5, " + " purchase_amt_e5, " + " feedback_rate_5, " + " feedback_lmt, "
        + " issue_cond, " + " issue_date_s, " + " issue_date_e, " + " issue_flag, "
        + " issue_num_1, " + " issue_num_2, " + " issue_num_3, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm, " 
        + " group_oppost_cond "
        +" ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?,?)";

    Object[] param = new Object[] {fundCode, wp.itemStr("aud_type"), wp.itemStr("fund_name"),
        wp.itemStr("fund_crt_date_s"), wp.itemStr("fund_crt_date_e"), wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"), wp.itemStr("stop_desc"), wp.itemStr("curr_code"),
        wp.itemStr("feedback_month_s"), wp.itemStr("feedback_month_e"), wp.itemNum("effect_months"),
        wp.itemStr("new_hldr_cond"), wp.itemNum("new_hldr_days"), wp.itemStr("new_group_cond"),
        wp.itemStr("new_hldr_card"), wp.itemStr("new_hldr_sup"), wp.itemStr("source_code_sel"),
        wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"), wp.itemStr("platform_kind_sel"), wp.itemStr("group_card_sel"),
        wp.itemStr("group_code_sel"), wp.itemNum("purchase_amt_s1"), wp.itemNum("purchase_amt_e1"),
        wp.itemNum("feedback_rate_1"), wp.itemNum("purchase_amt_s2"), wp.itemNum("purchase_amt_e2"),
        wp.itemNum("feedback_rate_2"), wp.itemNum("purchase_amt_s3"), wp.itemNum("purchase_amt_e3"),
        wp.itemNum("feedback_rate_3"), wp.itemNum("purchase_amt_s4"), wp.itemNum("purchase_amt_e4"),
        wp.itemNum("feedback_rate_4"), wp.itemNum("purchase_amt_s5"), wp.itemNum("purchase_amt_e5"),
        wp.itemNum("feedback_rate_5"), wp.itemNum("feedback_lmt"), wp.itemStr("issue_cond"),
        wp.itemStr("issue_date_s"), wp.itemStr("issue_date_e"), wp.itemStr("issue_flag"),
        wp.itemNum("issue_num_1"), wp.itemNum("issue_num_2"), wp.itemNum("issue_num_3"),
        wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm(),
        wp.itemStr("group_oppost_cond")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_PARM_DATA_T " + "select * " + "from MKT_PARM_DATA "
        + "where table_name  =  'CYC_DC_FUND_PARM' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("fund_code"),};

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

    strSql = "update " + controlTabName + " set " + "fund_name = ?, " + "fund_crt_date_s = ?, "
        + "fund_crt_date_e = ?, " + "stop_flag = ?, " + "stop_date = ?, " + "stop_desc = ?, "
        + "curr_code = ?, " + "feedback_month_s = ?, " + "feedback_month_e = ?, "
        + "effect_months = ?, " + "new_hldr_cond = ?, " + "new_hldr_days = ?, "
        + "new_group_cond = ?, " + "new_hldr_card = ?, " + "new_hldr_sup = ?, "
        + "source_code_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "platform_kind_sel = ?, "
        + "group_card_sel = ?, " + "group_code_sel = ?, " + "purchase_amt_s1 = ?, "
        + "purchase_amt_e1 = ?, " + "feedback_rate_1 = ?, " + "purchase_amt_s2 = ?, "
        + "purchase_amt_e2 = ?, " + "feedback_rate_2 = ?, " + "purchase_amt_s3 = ?, "
        + "purchase_amt_e3 = ?, " + "feedback_rate_3 = ?, " + "purchase_amt_s4 = ?, "
        + "purchase_amt_e4 = ?, " + "feedback_rate_4 = ?, " + "purchase_amt_s5 = ?, "
        + "purchase_amt_e5 = ?, " + "feedback_rate_5 = ?, " + "feedback_lmt = ?, "
        + "issue_cond = ?, " + "issue_date_s = ?, " + "issue_date_e = ?, " + "issue_flag = ?, "
        + "issue_num_1 = ?, " + "issue_num_2 = ?, " + "issue_num_3 = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ?, "
        + "group_oppost_cond = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("fund_name"), wp.itemStr("fund_crt_date_s"),
        wp.itemStr("fund_crt_date_e"), wp.itemStr("stop_flag"), wp.itemStr("stop_date"),
        wp.itemStr("stop_desc"), wp.itemStr("curr_code"), wp.itemStr("feedback_month_s"),
        wp.itemStr("feedback_month_e"), wp.itemNum("effect_months"), wp.itemStr("new_hldr_cond"),
        wp.itemNum("new_hldr_days"), wp.itemStr("new_group_cond"), wp.itemStr("new_hldr_card"),
        wp.itemStr("new_hldr_sup"), wp.itemStr("source_code_sel"), wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"), wp.itemStr("platform_kind_sel"), wp.itemStr("group_card_sel"), wp.itemStr("group_code_sel"),
        wp.itemNum("purchase_amt_s1"), wp.itemNum("purchase_amt_e1"), wp.itemNum("feedback_rate_1"),
        wp.itemNum("purchase_amt_s2"), wp.itemNum("purchase_amt_e2"), wp.itemNum("feedback_rate_2"),
        wp.itemNum("purchase_amt_s3"), wp.itemNum("purchase_amt_e3"), wp.itemNum("feedback_rate_3"),
        wp.itemNum("purchase_amt_s4"), wp.itemNum("purchase_amt_e4"), wp.itemNum("feedback_rate_4"),
        wp.itemNum("purchase_amt_s5"), wp.itemNum("purchase_amt_e5"), wp.itemNum("feedback_rate_5"),
        wp.itemNum("feedback_lmt"), wp.itemStr("issue_cond"), wp.itemStr("issue_date_s"),
        wp.itemStr("issue_date_e"), wp.itemStr("issue_flag"), wp.itemNum("issue_num_1"),
        wp.itemNum("issue_num_2"), wp.itemNum("issue_num_3"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"),wp.itemStr("group_oppost_cond"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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

    strSql = "delete MKT_PARM_DATA_T " + " where table_name  =  'CYC_DC_FUND_PARM' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("fund_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_PARM_DATA_T 錯誤");

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
    if (wp.respHtml.equals("mktm0380_gncd"))
      dataType = "5";
    if (wp.respHtml.equals("mktm0380_srcd"))
      dataType = "3";
    if (wp.respHtml.equals("mktm0380_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm0380_aaa2"))
      dataType = "P";
    if (wp.respHtml.equals("mktm0380_grcd"))
      dataType = "2";
    strSql = "insert into MKT_PARM_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'CYC_DC_FUND_PARM', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("fund_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_PARM_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0380_gncd"))
      dataType = "5";
    if (wp.respHtml.equals("mktm0380_srcd"))
      dataType = "3";
    if (wp.respHtml.equals("mktm0380_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm0380_aaa2"))
      dataType = "P";
    if (wp.respHtml.equals("mktm0380_grcd"))
      dataType = "2";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("fund_code")};
    if (sqlRowcount("MKT_PARM_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'CYC_DC_FUND_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_PARM_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'CYC_DC_FUND_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0380_mrch"))
      dataType = "4";
    if (wp.respHtml.equals("mktm0380_gpcd"))
      dataType = "1";
    strSql = "insert into MKT_PARM_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'CYC_DC_FUND_PARM', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("fund_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_PARM_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0380_mrch"))
      dataType = "4";
    if (wp.respHtml.equals("mktm0380_gpcd"))
      dataType = "1";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("fund_code")};
    if (sqlRowcount("MKT_PARM_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'CYC_DC_FUND_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_PARM_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'CYC_DC_FUND_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long list_cnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < list_cnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < list_cnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < list_cnt; inti++) {
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

    Object[] param = new Object[] {"CYC_DC_FUND_PARM", wp.itemStr("fund_code"), "4"};

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

}  // End of class
