/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
/*期中覆審-風險族群評估參數維護 V.2018-0731.jh
 * 2018-0731:	JH		++insert_rsk_parm2_log()
 * 
 * */
package rskm02;

import busi.FuncAction;

public class Rskm1030Func extends FuncAction {

  String riskGroup = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      riskGroup = wp.itemStr("kk_risk_group");
    } else {
      riskGroup = wp.itemStr("risk_group");
    }

    if (empty(riskGroup)) {
      errmsg("風險族群不可空白");
      return;
    }

    if (this.ibAdd) {
      if (checkData(riskGroup)) {
        errmsg("資料已存在,不可新增");
        return;
      }
    }


    if (this.ibDelete) {
      return;
    }

    if (this.ibAdd || this.ibUpdate) {
      if (wp.itemEq(riskGroup, "Z9")) {
        errmsg("Z9 為預設值, 不可新增或修改");
        return;
      }
    }



    if (wp.itemEq("no_assure_cond", "Y")) {
      double assureAmts = wp.itemNum("no_assure_amt_s");
      double assureAmte = wp.itemNum("no_assure_amt_e");

      if (assureAmts > assureAmte) {
        errmsg("無擔授信總計 起迄值輸入錯誤~");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("no_assure_amt_s"), "0");
      wp.itemSet(wp.itemStr("no_assure_amt_e"), "0");
    }

    if (wp.itemEq("k34_estimate_rcbal_cond", "Y")) {
      double rcbals = wp.itemNum("k34_estimate_rcbal_s");
      double rcbale = wp.itemNum("k34_estimate_rcbal_e");
      if (rcbals < 0 || rcbale < 0) {
        errmsg("推估RC金額 需大於 0");
        return;
      }
      if (rcbals > rcbale) {
        errmsg("推估RC金額 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_estimate_rcbal_s"), "0");
      wp.itemSet(wp.itemStr("k34_estimate_rcbal_e"), "0");
    }

    if (wp.itemEq("k34_use_rc_rate_cond", "Y")) {
      double rates = wp.itemNum("k34_use_rc_rate_s");
      double ratee = wp.itemNum("k34_use_rc_rate_e");
      if (rates < 0 || ratee < 0) {
        errmsg("RC動用率 需大於 0");
        return;
      }
      if (rates > ratee) {
        errmsg("RC動用率 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_use_rc_rate_s"), "0");
      wp.itemSet(wp.itemStr("k34_use_rc_rate_e"), "0");
    }

    if (wp.itemEq("k34_overdue_banks_cond", "Y")) {
      double banks = wp.itemNum("k34_overdue_banks_s");
      double bankse = wp.itemNum("k34_overdue_banks_e");
      if (banks < 0 || bankse < 0) {
        errmsg("當期逾期銀行數 需大於 0");
        return;
      }
      if (banks > bankse) {
        errmsg("當期逾期銀行數 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_overdue_banks_s"), "0");
      wp.itemSet(wp.itemStr("k34_overdue_banks_e"), "0");
    }

    if (wp.itemEq("k34_overdue_6mm_cond", "Y")) {
      double overdues = wp.itemNum("k34_overdue_6mm_s");
      double overduee = wp.itemNum("k34_overdue_6mm_e");
      if (overdues < 0 || overduee < 0) {
        errmsg("六個月內逾期次數 需大於 0");
        return;
      }
      if (overdues > overduee) {
        errmsg("六個月內逾期次數 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_overdue_6mm_s"), "0");
      wp.itemSet(wp.itemStr("k34_overdue_6mm_e"), "0");
    }

    if (wp.itemEq("k34_overdue_12mm_cond", "Y")) {
      double overdue12mmS = wp.itemNum("k34_overdue_12mm_s");
      double overdue12mmE = wp.itemNum("k34_overdue_12mm_e");
      if (overdue12mmS < 0 || overdue12mmE < 0) {
        errmsg("1 年內逾期次數 需大於 0");
        return;
      }
      if (overdue12mmS > overdue12mmE) {
        errmsg("1 年內逾期次數 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_overdue_12mm_s"), "0");
      wp.itemSet(wp.itemStr("k34_overdue_12mm_e"), "0");
    }

    if (wp.itemEq("k34_use_cash_6mm_cond", "Y")) {
      double cash6mmS = wp.itemNum("k34_use_cash_6mm_s");
      double cash6mmE = wp.itemNum("k34_use_cash_6mm_e");
      if (cash6mmS < 0 || cash6mmE < 0) {
        errmsg("六個月內預借現金次數 需大於 0");
        return;
      }
      if (cash6mmS > cash6mmE) {
        errmsg("六個月內預借現金次數 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_use_cash_6mm_s"), "0");
      wp.itemSet(wp.itemStr("k34_use_cash_6mm_e"), "0");
    }

    if (wp.itemEq("k34_use_cash_12mm_cond", "Y")) {
      double cash12mmS = wp.itemNum("k34_use_cash_12mm_s");
      double cash12mmE = wp.itemNum("k34_use_cash_12mm_e");
      if (cash12mmS < 0 || cash12mmE < 0) {
        errmsg("1 年內預借現金次數 需大於 0");
        return;
      }
      if (cash12mmS > cash12mmE) {
        errmsg("1 年內預借現金次數 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("k34_use_cash_12mm_s"), "0");
      wp.itemSet(wp.itemStr("k34_use_cash_12mm_e"), "0");
    }

    if (wp.itemEq("b63_no_overdue_amt_cond", "Y")) {
      double overdueAmtS = wp.itemNum("b63_no_overdue_amt_s");
      double overdueAmtE = wp.itemNum("b63_no_overdue_amt_e");
      if (overdueAmtS < 0 || overdueAmtE < 0) {
        errmsg("未逾期金額 需大於 0");
        return;
      }
      if (overdueAmtS > overdueAmtE) {
        errmsg("未逾期金額 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("b63_no_overdue_amt_s"), "0");
      wp.itemSet(wp.itemStr("b63_no_overdue_amt_e"), "0");
    }

    if (wp.itemEq("b63_overdue_nopay_cond", "Y")) {
      double overdueNopayS = wp.itemNum("b63_overdue_nopay_s");
      double overdueNopayE = wp.itemNum("b63_overdue_nopay_e");
      if (overdueNopayS < 0 || overdueNopayE < 0) {
        errmsg("逾期未還金額 需大於 0");
        return;
      }
      if (overdueNopayS > overdueNopayE) {
        errmsg("逾期未還金額 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("b63_overdue_nopay_s"), "0");
      wp.itemSet(wp.itemStr("b63_overdue_nopay_e"), "0");
    }

    if (wp.itemEq("b63_cash_due_amt_cond", "Y")) {
      double dueAmtS = wp.itemNum("b63_cash_due_amt_s");
      double dueAmtE = wp.itemNum("b63_cash_due_amt_e");
      if (dueAmtS < 0 || dueAmtE < 0) {
        errmsg("現金卡未逾期總金額 需大於 0");
        return;
      }
      if (dueAmtS > dueAmtE) {
        errmsg("現金卡未逾期總金額 起迄值輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("b63_cash_due_amt_s"), "0");
      wp.itemSet(wp.itemStr("b63_cash_due_amt_e"), "0");
    }

    if (wp.itemEq("credit_limit_cond", "Y")) {
      if (empty(wp.itemStr("credit_limit_s_date")) || empty(wp.itemStr("credit_limit_e_date"))) {
        errmsg("調額期間 不可空白");
        return;
      }
      if (this.chkStrend(wp.itemStr("credit_limit_s_date"),
          wp.itemStr("credit_limit_e_date")) == -1) {
        errmsg("調額期間 起迄值輸入錯誤~");
        return;
      }

      if (wp.itemEmpty("credit_limit_code")) {
        errmsg("請維護調額代碼");
        return;
      }

    } else {
      wp.itemSet("credit_limit_s_date", "");
      wp.itemSet("credit_limit_e_date", "");
    }

    if (wp.itemEq("rc_avguse_cond", "Y")) {
      double avguseMM = wp.itemNum("rc_avguse_mm");
      if (avguseMM < 0) {
        errmsg("RC平均使用率: 檢查月數需大於 0");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("rc_avguse_mm"), "0");
      wp.itemSet(wp.itemStr("rc_avguse_rate"), "0");
    }

    if (wp.itemEq("cash_use_cond", "Y")) {
      double useMm = wp.itemNum("cash_use_mm");
      if (useMm < 0) {
        errmsg("預借現金使用: 檢查月數需大於 0");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("cash_use_mm"), "0");
      wp.itemSet(wp.itemStr("cash_use_times"), "0");
    }

    if (wp.itemEq("limit_avguse_cond", "Y")) {
      double avguseMm = wp.itemNum("limit_avguse_mm");
      if (avguseMm < 0) {
        errmsg("額度平均動用率: 檢查月數需大於 0");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("limit_avguse_mm"), "0");
      wp.itemSet(wp.itemStr("limit_avguse_rate"), "0");
    }

    if (wp.itemEq("payment_rate_cond", "Y")) {
      double rateMm = wp.itemNum("payment_rate_mm");
      if (rateMm < 0) {
        errmsg("帳戶逾期(M1): 檢查月數不可為 0");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("payment_rate_mm"), "0");
      wp.itemSet(wp.itemStr("payment_rate_times"), "0");
    }

    if (wp.itemEq("no_debt_cond", "Y")) {
      double debtMm = wp.itemNum("no_debt_mm");
      if (debtMm < 0) {
        errmsg("無消費且無欠款: 檢查月數不可為 0");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("no_debt_mm"), "0");
    }

    if (wp.itemEq("acct_jrnl_bal_cond", "Y")) {
      double balS = wp.itemNum("acct_jrnl_bal_s");
      double balE = wp.itemNum("acct_jrnl_bal_e");
      if (balS > balE) {
        errmsg("欠款金額: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("acct_jrnl_bal_s", "0");
      wp.itemSet("acct_jrnl_bal_e", "0");
    }

    if (wp.itemEq("no_assure_add_cond", "Y")) {
      double addAmt = wp.itemNum("no_assure_add_amt");
      double addAmt2 = wp.itemNum("no_assure_add_amt2");
      if (addAmt < 0 && addAmt2 < 0) {
        errmsg("請輸入 無擔負債增加金額");
        return;
      }
      if (addAmt > addAmt2) {
        errmsg("無擔負債增加金額 區間輸入錯誤");
        return;
      }
    } else {
      wp.itemSet(wp.itemStr("no_assure_add_amt"), "0");
    }

    if (wp.itemEq("trial_score_cond", "Y")) {
      double scoreS = wp.itemNum("trial_score_s");
      double scoreE = wp.itemNum("trial_score_e");
      if (scoreS > scoreE) {
        errmsg("總評分分數: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("trial_score_s", "0");
      wp.itemSet("trial_score_e", "0");
    }

    if (wp.itemEq("jcic028_cond", "Y")) {
      double jcic028S = wp.itemNum("jcic028_s");
      double jcic028E = wp.itemNum("jcic028_e");
      if (jcic028S > jcic028E) {
        errmsg("[28]循環總餘額: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic028_s", "0");
      wp.itemSet("jcic028_e", "0");
    }

    if (wp.itemEq("jcic029_cond", "Y")) {
      double jcic029S = wp.itemNum("jcic029_s");
      double jcic029E = wp.itemNum("jcic029_e");
      if (jcic029S > jcic029E) {
        errmsg("[29]循環餘額總使用率: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic029_s", "0");
      wp.itemSet("jcic029_e", "0");
    }

    if (wp.itemEq("jcic023_03_cond", "Y")) {
      double jcic02303 = wp.itemNum("jcic023_03");
      double jcic02303E = wp.itemNum("jcic023_03_e");
      if (jcic02303 > jcic02303E) {
        errmsg("[23-03]授信(不含現金卡)近6個月同一銀行連兩期逾期未繳總家數: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic023_03", "0");
      wp.itemSet("jcic023_03_e", "0");
    }

    if (wp.itemEq("jcic025_01_cond", "Y")) {
      double jcic02501 = wp.itemNum("jcic025_01");
      double jcic025E = wp.itemNum("jcic025_01_e");
      if (jcic02501 > jcic025E) {
        errmsg("[25-1]現金卡近6個月同一銀行連兩期逾期未繳總家數: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic025_01", "0");
      wp.itemSet("jcic025_01_e", "0");
    }

    if (wp.itemEq("jcic004_01_cond", "Y")) {
      double jcic00401 = wp.itemNum("jcic004_01");
      double jcic00401E = wp.itemNum("jcic004_01_e");
      if (jcic00401 > jcic00401E) {
        errmsg("[04-1]近6個月信用卡循環信用增加金額: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic004_01", "0");
      wp.itemSet("jcic004_01_e", "0");
    }

    if (wp.itemEq("jcic009_cond", "Y")) {
      double jcic009 = wp.itemNum("jcic009");
      double jcic009E = wp.itemNum("jcic009_e");
      if (jcic009 > jcic009E) {
        errmsg("[09]近6個月信用卡循環餘額成長率【％】: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic009", "0");
      wp.itemSet("jcic009_e", "0");
    }

    if (wp.itemEq("jcic010_02_cond", "Y")) {
      double jcic01002 = wp.itemNum("jcic010_02");
      double jcic01002E = wp.itemNum("jcic010_02_e");
      if (jcic01002 > jcic01002E) {
        errmsg("[10-2]使用信用卡循環信用總家數: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic010_02", "0");
      wp.itemSet("jcic010_02_e", "0");
    }

    if (wp.itemEq("jcic023_01_cond", "Y")) {
      double jcic02301 = wp.itemNum("jcic023_01");
      double jcic02301E = wp.itemNum("jcic023_01_e");
      if (jcic02301 > jcic02301E) {
        errmsg("[23-1]信用貸款在過去6個月之延遲次數: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic023_01", "0");
      wp.itemSet("jcic023_01_e", "0");
    }

    if (wp.itemEq("jcic023_02_cond", "Y")) {
      double jcic02302 = wp.itemNum("jcic023_02");
      double jcic02302E = wp.itemNum("jcic023_02_e");
      if (jcic02302 > jcic02302E) {
        errmsg("[23-2]抵押性貸款在過去6個月之延遲次數: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("jcic023_02", "0");
      wp.itemSet("jcic023_02_e", "0");
    }

    if (wp.itemEq("block_reason_cond", "0") == false) {
      if (wp.itemEmpty("acno_block_reason")) {
        errmsg("請維護 指定/排除 凍結碼");
        return;
      }
    }

    if (ibAdd) {
      return;
    }
    sqlWhere =
        " where 1=1 " + " and risk_group =?" + " and apr_flag =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {riskGroup, wp.itemStr("apr_flag"), wp.itemNum("mod_seqno")};
    if (this.isOtherModify("rsk_trial_parm2", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }

  }

  boolean checkData(String riskGroup) {
    if (empty(riskGroup))
      return false;

    String sql1 = "select count(*) as db_cnt " + " from rsk_trial_parm2" + " where risk_group =?";
    sqlSelect(sql1, new Object[] {riskGroup});
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    insertData();
    return rc;
  }

  public int insertData() {
    strSql = "insert into RSK_TRIAL_PARM2 (" + " risk_group," + " apr_flag," + " risk_group_desc,"
        + " rskgp_remark," + " no_assure_cond," + " no_assure_amt_s," + " no_assure_amt_e,"
        + " dbr_cond," + " dbr_s," + " dbr_e," + " k34_estimate_rcbal_cond,"
        + " k34_estimate_rcbal_s," + " k34_estimate_rcbal_e," + " k34_use_rc_rate_cond,"
        + " k34_use_rc_rate_s," + " k34_use_rc_rate_e," + " k34_overdue_cond,"
        + " k34_overdue_flag," + " k34_overdue_banks_cond," + " k34_overdue_banks_s,"
        + " k34_overdue_banks_e," + " k34_overdue_6mm_cond," + " k34_overdue_6mm_s,"
        + " k34_overdue_6mm_e," + " k34_overdue_12mm_cond," + " k34_overdue_12mm_s,"
        + " k34_overdue_12mm_e," + " k34_use_cash_cond," + " k34_use_cash_flag,"
        + " k34_use_cash_6mm_cond," + " k34_use_cash_6mm_s," + " k34_use_cash_6mm_e,"
        + " k34_use_cash_12mm_cond," + " k34_use_cash_12mm_s," + " k34_use_cash_12mm_e,"
        + " k34_debt_code_cond," + " k34_debt_code," + " b63_no_overdue_amt_cond,"
        + " b63_no_overdue_amt_s," + " b63_no_overdue_amt_e," + " b63_overdue_cond,"
        + " b63_overdue_flag," + " b63_overdue_nopay_cond," + " b63_overdue_nopay_s,"
        + " b63_overdue_nopay_e," + " b63_cash_due_amt_cond," + " b63_cash_due_amt_s,"
        + " b63_cash_due_amt_e," + " jcic028_cond," + " jcic028_s," + " jcic028_e,"
        + " jcic029_cond," + " jcic029_s," + " jcic029_e," + " no_assure_add_cond,"
        + " no_assure_add_amt," + " no_assure_add_amt2," + " jcic036_cond," + " jcic036,"
        + " jcic030_cond," + " jcic030," + " jcic031_cond," + " jcic031," + " jcic023_03_cond,"
        + " jcic023_03," + " jcic023_03_e," + " jcic025_01_cond," + " jcic025_01,"
        + " jcic025_01_e," + " jcic030_01_cond," + " jcic030_02_cond," + " jcic031_01_cond,"
        + " jcic031_02_cond," + " jcic034_cond," + " jcic034," + " jcic032_cond," + " jcic032,"
        + " jcic004_01_cond," + " jcic004_01," + " jcic004_01_e," + " jcic009_cond," + " jcic009,"
        + " jcic009_e," + " jcic010_02_cond," + " jcic010_02," + " jcic010_02_e," + " jcic013_cond,"
        + " jcic013," + " jcic023_01_cond," + " jcic023_01," + " jcic023_01_e,"
        + " jcic023_02_cond ," + " jcic023_02," + " jcic023_02_e," + " credit_limit_cond,"
        + " credit_limit_s_date," + " credit_limit_e_date," + " rc_avguse_cond," + " rc_avguse_mm,"
        + " rc_avguse_rate," + " cash_use_cond," + " cash_use_mm," + " cash_use_times,"
        + " limit_avguse_cond," + " limit_avguse_mm," + " limit_avguse_rate,"
        + " payment_rate_cond," + " payment_rate_mm," + " payment_rate_times," + " no_debt_cond,"
        + " no_debt_mm," + " payment_int_cond," + " acct_jrnl_bal_cond," + " acct_jrnl_bal_s,"
        + " acct_jrnl_bal_e," + " trial_score_cond," + " trial_score_s," + " trial_score_e,"
        + " block_reason_cond," + " crt_user," + " crt_date," + " apr_user," + " apr_date,"
        + " acno_block_reason," + " jcic031_02," + " jcic030_01," + " jcic031_01," + " jcic030_02,"
        + " credit_limit_code," + " mod_time," + " mod_user," + " mod_pgm," + " mod_seqno"
        + " ) values (" + " :kk," + " 'N'," + " :risk_group_desc," + " :rskgp_remark,"
        + " :no_assure_cond," + " :no_assure_amt_s," + " :no_assure_amt_e," + " :dbr_cond,"
        + " :dbr_s," + " :dbr_e," + " :k34_estimate_rcbal_cond," + " :k34_estimate_rcbal_s,"
        + " :k34_estimate_rcbal_e," + " :k34_use_rc_rate_cond," + " :k34_use_rc_rate_s,"
        + " :k34_use_rc_rate_e," + " :k34_overdue_cond," + " :k34_overdue_flag,"
        + " :k34_overdue_banks_cond," + " :k34_overdue_banks_s," + " :k34_overdue_banks_e,"
        + " :k34_overdue_6mm_cond," + " :k34_overdue_6mm_s," + " :k34_overdue_6mm_e,"
        + " :k34_overdue_12mm_cond," + " :k34_overdue_12mm_s," + " :k34_overdue_12mm_e,"
        + " :k34_use_cash_cond," + " :k34_use_cash_flag," + " :k34_use_cash_6mm_cond,"
        + " :k34_use_cash_6mm_s," + " :k34_use_cash_6mm_e," + " :k34_use_cash_12mm_cond,"
        + " :k34_use_cash_12mm_s," + " :k34_use_cash_12mm_e," + " :k34_debt_code_cond,"
        + " :k34_debt_code," + " :b63_no_overdue_amt_cond," + " :b63_no_overdue_amt_s,"
        + " :b63_no_overdue_amt_e," + " :b63_overdue_cond," + " :b63_overdue_flag,"
        + " :b63_overdue_nopay_cond," + " :b63_overdue_nopay_s," + " :b63_overdue_nopay_e,"
        + " :b63_cash_due_amt_cond," + " :b63_cash_due_amt_s," + " :b63_cash_due_amt_e,"
        + " :jcic028_cond," + " :jcic028_s," + " :jcic028_e," + " :jcic029_cond," + " :jcic029_s,"
        + " :jcic029_e," + " :no_assure_add_cond," + " :no_assure_add_amt,"
        + " :no_assure_add_amt2," + " :jcic036_cond," + " :jcic036," + " :jcic030_cond,"
        + " :jcic030," + " :jcic031_cond," + " :jcic031," + " :jcic023_03_cond," + " :jcic023_03,"
        + " :jcic023_03_e," + " :jcic025_01_cond," + " :jcic025_01," + " :jcic025_01_e,"
        + " :jcic030_01_cond," + " :jcic030_02_cond," + " :jcic031_01_cond," + " :jcic031_02_cond,"
        + " :jcic034_cond," + " :jcic034," + " :jcic032_cond," + " :jcic032," + " :jcic004_01_cond,"
        + " :jcic004_01," + " :jcic004_01_e," + " :jcic009_cond," + " :jcic009," + " :jcic009_e,"
        + " :jcic010_02_cond," + " :jcic010_02," + " :jcic010_02_e," + " :jcic013_cond,"
        + " :jcic013," + " :jcic023_01_cond," + " :jcic023_01," + " :jcic023_01_e,"
        + " :jcic023_02_cond ," + " :jcic023_02," + " :jcic023_02_e," + " :credit_limit_cond,"
        + " :credit_limit_s_date," + " :credit_limit_e_date," + " :rc_avguse_cond,"
        + " :rc_avguse_mm," + " :rc_avguse_rate," + " :cash_use_cond," + " :cash_use_mm,"
        + " :cash_use_times," + " :limit_avguse_cond," + " :limit_avguse_mm,"
        + " :limit_avguse_rate," + " :payment_rate_cond," + " :payment_rate_mm,"
        + " :payment_rate_times," + " :no_debt_cond," + " :no_debt_mm," + " :payment_int_cond,"
        + " :acct_jrnl_bal_cond," + " :acct_jrnl_bal_s," + " :acct_jrnl_bal_e,"
        + " :trial_score_cond," + " :trial_score_s," + " :trial_score_e," + " :block_reason_cond,"
        + " :crt_user," + " to_char(sysdate,'yyyymmdd')," + " ''," + " ''," + " :acno_block_reason,"
        + " :jcic031_02," + " :jcic030_01," + " :jcic031_01," + " :jcic030_02,"
        + " :credit_limit_code," + " sysdate," + " :mod_user," + " :mod_pgm," + " 1" + " )";
    // -set ?value-
    try {
      setString("kk", riskGroup);
      item2ParmStr("risk_group_desc");
      item2ParmStr("rskgp_remark");
      item2ParmNvl("no_assure_cond", "N");
      item2ParmNum("no_assure_amt_s");
      item2ParmNum("no_assure_amt_e");
      item2ParmNvl("dbr_cond", "N");
      item2ParmNum("dbr_s");
      item2ParmNum("dbr_e");
      item2ParmNvl("k34_estimate_rcbal_cond", "N");
      item2ParmNum("k34_estimate_rcbal_s");
      item2ParmNum("k34_estimate_rcbal_e");
      item2ParmNvl("k34_use_rc_rate_cond", "N");
      item2ParmNum("k34_use_rc_rate_s");
      item2ParmNum("k34_use_rc_rate_e");
      item2ParmNvl("k34_overdue_cond", "N");
      item2ParmNvl("k34_overdue_flag", "N");
      item2ParmNvl("k34_overdue_banks_cond", "N");
      item2ParmNum("k34_overdue_banks_s");
      item2ParmNum("k34_overdue_banks_e");
      item2ParmNvl("k34_overdue_6mm_cond", "N");
      item2ParmNum("k34_overdue_6mm_s");
      item2ParmNum("k34_overdue_6mm_e");
      item2ParmNvl("k34_overdue_12mm_cond", "N");
      item2ParmNum("k34_overdue_12mm_s");
      item2ParmNum("k34_overdue_12mm_e");
      item2ParmNvl("k34_use_cash_cond", "N");
      item2ParmNvl("k34_use_cash_flag", "N");
      item2ParmNvl("k34_use_cash_6mm_cond", "N");
      item2ParmNum("k34_use_cash_6mm_s");
      item2ParmNum("k34_use_cash_6mm_e");
      item2ParmNvl("k34_use_cash_12mm_cond", "N");
      item2ParmNum("k34_use_cash_12mm_s");
      item2ParmNum("k34_use_cash_12mm_e");
      item2ParmNvl("k34_debt_code_cond", "N");
      item2ParmNvl("k34_debt_code", "N");
      item2ParmNvl("b63_no_overdue_amt_cond", "N");
      item2ParmNum("b63_no_overdue_amt_s");
      item2ParmNum("b63_no_overdue_amt_e");
      item2ParmNvl("b63_overdue_cond", "N");
      item2ParmNvl("b63_overdue_flag", "N");
      item2ParmNvl("b63_overdue_nopay_cond", "N");
      item2ParmNum("b63_overdue_nopay_s");
      item2ParmNum("b63_overdue_nopay_e");
      item2ParmNvl("b63_cash_due_amt_cond", "N");
      item2ParmNum("b63_cash_due_amt_s");
      item2ParmNum("b63_cash_due_amt_e");
      item2ParmNvl("jcic028_cond", "N");
      item2ParmNum("jcic028_s");
      item2ParmNum("jcic028_e");
      item2ParmNvl("jcic029_cond", "N");
      item2ParmNum("jcic029_s");
      item2ParmNum("jcic029_e");
      item2ParmNvl("no_assure_add_cond", "N");
      item2ParmNum("no_assure_add_amt");
      item2ParmNum("no_assure_add_amt2");
      item2ParmNvl("jcic036_cond", "N");
      item2ParmNvl("jcic036", "A");
      item2ParmNvl("jcic030_cond", "N");
      item2ParmNvl("jcic030", "N");
      item2ParmNvl("jcic031_cond", "N");
      item2ParmNvl("jcic031", "N");
      item2ParmNvl("jcic023_03_cond", "N");
      item2ParmNum("jcic023_03");
      item2ParmNum("jcic023_03_e");
      item2ParmNvl("jcic025_01_cond", "N");
      item2ParmNum("jcic025_01");
      item2ParmNum("jcic025_01_e");
      item2ParmNvl("jcic030_01_cond", "N");
      item2ParmNvl("jcic030_02_cond", "N");
      item2ParmNvl("jcic031_01_cond", "N");
      item2ParmNvl("jcic031_02_cond", "N");
      item2ParmNvl("jcic034_cond", "N");
      item2ParmNvl("jcic034", "N");
      item2ParmNvl("jcic032_cond", "N");
      item2ParmNvl("jcic032", "N");
      item2ParmNvl("jcic004_01_cond", "N");
      item2ParmNum("jcic004_01");
      item2ParmNum("jcic004_01_e");
      item2ParmNvl("jcic009_cond", "N");
      item2ParmNum("jcic009");
      item2ParmNum("jcic009_e");
      item2ParmNvl("jcic010_02_cond", "N");
      item2ParmNum("jcic010_02");
      item2ParmNum("jcic010_02_e");
      item2ParmNvl("jcic013_cond", "N");
      item2ParmNvl("jcic013", "N");
      item2ParmNvl("jcic023_01_cond", "N");
      item2ParmNum("jcic023_01");
      item2ParmNum("jcic023_01_e");
      item2ParmNvl("jcic023_02_cond", "N");
      item2ParmNum("jcic023_02");
      item2ParmNum("jcic023_02_e");
      item2ParmNvl("credit_limit_cond", "N");
      item2ParmStr("credit_limit_s_date");
      item2ParmStr("credit_limit_e_date");
      item2ParmNvl("rc_avguse_cond", "N");
      item2ParmNum("rc_avguse_mm");
      item2ParmNum("rc_avguse_rate");
      item2ParmNvl("cash_use_cond", "N");
      item2ParmNum("cash_use_mm");
      item2ParmNum("cash_use_times");
      item2ParmNvl("limit_avguse_cond", "N");
      item2ParmNum("limit_avguse_mm");
      item2ParmNum("limit_avguse_rate");
      item2ParmNvl("payment_rate_cond", "N");
      item2ParmNum("payment_rate_mm");
      item2ParmNum("payment_rate_times");
      item2ParmNvl("no_debt_cond", "N");
      item2ParmNum("no_debt_mm");
      item2ParmNvl("payment_int_cond", "N");
      item2ParmNvl("acct_jrnl_bal_cond", "N");
      item2ParmNum("acct_jrnl_bal_s");
      item2ParmNum("acct_jrnl_bal_e");
      item2ParmNvl("trial_score_cond", "N");
      item2ParmNum("trial_score_s");
      item2ParmNum("trial_score_e");
      item2ParmNvl("block_reason_cond", "N");
      item2ParmStr("acno_block_reason");
      item2ParmStr("jcic031_02");
      item2ParmStr("jcic030_01");
      item2ParmStr("jcic031_01");
      item2ParmStr("jcic030_02");
      item2ParmStr("credit_limit_code");
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  public int deletData() {
    strSql = "delete RSK_TRIAL_PARM2 " + " where risk_group =:kk1 " + " and apr_flag ='N'"
        + " and nvl(mod_seqno,0) =:mod_seqno ";
    setString("kk1", riskGroup);
    item2ParmStr("apr_flag");
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else {
      rc = 1;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    deletData();
    insertData();
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    insertRskTrialParm2Log();

    strSql = "delete RSK_TRIAL_PARM2 " + " where risk_group =:kk1 " + " and apr_flag =:apr_flag"
        + " and nvl(mod_seqno,0) =:mod_seqno ";
    setString("kk1", riskGroup);
    item2ParmStr("apr_flag");
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  void insertRskTrialParm2Log() {
    if (!wp.itemEq("apr_flag", "Y") || wp.itemEmpty("approval_user"))
      return;
    String sql1 = "";
    sql1 = "select * from rsk_trial_parm2" + " where risk_group =?" + " and apr_flag ='Y'";
    setString2(1, riskGroup);
    sqlSelect(sql1);
    if (sqlRowNum == 0) {
      rc = 1;
      return;
    }

    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("rsk_trial_parm2_log");
    sp.addsqlParm("log_date", commSqlStr.sysYYmd);
    sp.addsqlParm(", log_time", "," + commSqlStr.sysTime);
    sp.addsqlParm(",aud_flag", ",'D'");
    sp.addsqlParm(",risk_group	            ", ",?", colStr("risk_group"));
    sp.addsqlParm(",risk_group_desc         ", ",?", colStr("risk_group_desc"));
    sp.addsqlParm(",riskgp_remark	  	      ", ",?", colStr("rskgp_remark"));
    sp.addsqlParm(",dbr_cond	               ", ",?", colStr("dbr_cond"));
    sp.addsqlParm(",dbr_s	                  ", ",?", colNum("dbr_s"));
    sp.addsqlParm(",dbr_e	                  ", ",?", colNum("dbr_e"));
    sp.addsqlParm(",no_assure_cond	         ", ",?", colStr("no_assure_cond"));
    sp.addsqlParm(",no_assure_amt_s	      ", ",?", colNum("no_assure_amt_s"));
    sp.addsqlParm(",no_assure_amt_e	      ", ",?", colNum("no_assure_amt_e"));
    sp.addsqlParm(",k34_estimate_rcbal_cond	", ",?", colStr("k34_estimate_rcbal_cond"));
    sp.addsqlParm(",k34_estimate_rcbal_s	   ", ",?", colNum("k34_estimate_rcbal_s"));
    sp.addsqlParm(",k34_estimate_rcbal_e	   ", ",?", colNum("k34_estimate_rcbal_e"));
    sp.addsqlParm(",k34_use_rc_rate_cond	   ", ",?", colStr("k34_use_rc_rate_cond"));
    sp.addsqlParm(",k34_use_rc_rate_s	      ", ",?", colNum("k34_use_rc_rate_s"));
    sp.addsqlParm(",k34_use_rc_rate_e	      ", ",?", colNum("k34_use_rc_rate_e"));
    sp.addsqlParm(",k34_overdue_cond	      ", ",?", colStr("k34_overdue_cond"));
    sp.addsqlParm(",k34_overdue_flag	      ", ",?", colStr("k34_overdue_flag"));
    sp.addsqlParm(",k34_overdue_banks_cond	", ",?", colStr("k34_overdue_banks_cond"));
    sp.addsqlParm(",k34_overdue_banks_s	   ", ",?", colNum("k34_overdue_banks_s"));
    sp.addsqlParm(",k34_overdue_banks_e	   ", ",?", colNum("k34_overdue_banks_e"));
    sp.addsqlParm(",k34_overdue_6mm_cond	   ", ",?", colStr("k34_overdue_6mm_cond"));
    sp.addsqlParm(",k34_overdue_6mm_s	      ", ",?", colInt("k34_overdue_6mm_s"));
    sp.addsqlParm(",k34_overdue_6mm_e	      ", ",?", colInt("k34_overdue_6mm_e"));
    sp.addsqlParm(",k34_overdue_12mm_cond	", ",?", colStr("k34_overdue_12mm_cond"));
    sp.addsqlParm(",k34_overdue_12mm_s	   ", ",?", colInt("k34_overdue_12mm_s"));
    sp.addsqlParm(",k34_overdue_12mm_e	   ", ",?", colInt("k34_overdue_12mm_e"));
    sp.addsqlParm(",k34_use_cash_cond	      ", ",?", colStr("k34_use_cash_cond"));
    sp.addsqlParm(",k34_use_cash_flag	      ", ",?", colStr("k34_use_cash_flag"));
    sp.addsqlParm(",k34_use_cash_6mm_cond	", ",?", colStr("k34_use_cash_6mm_cond"));
    sp.addsqlParm(",k34_use_cash_6mm_s	   ", ",?", colInt("k34_use_cash_6mm_s"));
    sp.addsqlParm(",k34_use_cash_6mm_e	   ", ",?", colInt("k34_use_cash_6mm_e"));
    sp.addsqlParm(",k34_use_cash_12mm_cond	", ",?", colStr("k34_use_cash_12mm_cond"));
    sp.addsqlParm(",k34_use_cash_12mm_s	   ", ",?", colInt("k34_use_cash_12mm_s"));
    sp.addsqlParm(",k34_use_cash_12mm_e	   ", ",?", colInt("k34_use_cash_12mm_e"));
    sp.addsqlParm(",k34_debt_code_cond	   ", ",?", colStr("k34_debt_code_cond"));
    sp.addsqlParm(",k34_debt_code	         ", ",?", colStr("k34_debt_code"));
    sp.addsqlParm(",b63_no_overdue_amt_cond	", ",?", colStr("b63_no_overdue_amt_cond"));
    sp.addsqlParm(",b63_no_overdue_amt_s	   ", ",?", colNum("b63_no_overdue_amt_s"));
    sp.addsqlParm(",b63_no_overdue_amt_e	   ", ",?", colNum("b63_no_overdue_amt_e"));
    sp.addsqlParm(",b63_overdue_cond	      ", ",?", colStr("b63_overdue_cond"));
    sp.addsqlParm(",b63_overdue_flag	      ", ",?", colStr("b63_overdue_flag"));
    sp.addsqlParm(",b63_overdue_nopay_cond	", ",?", colStr("b63_overdue_nopay_cond"));
    sp.addsqlParm(",b63_overdue_nopay_s	   ", ",?", colNum("b63_overdue_nopay_s"));
    sp.addsqlParm(",b63_overdue_nopay_e	   ", ",?", colNum("b63_overdue_nopay_e"));
    sp.addsqlParm(",b63_cash_due_amt_cond	", ",?", colStr("b63_cash_due_amt_cond"));
    sp.addsqlParm(",b63_cash_due_amt_s	   ", ",?", colNum("b63_cash_due_amt_s"));
    sp.addsqlParm(",b63_cash_due_amt_e	   ", ",?", colStr("b63_cash_due_amt_e"));
    sp.addsqlParm(",credit_limit_cond	      ", ",?", colStr("credit_limit_cond"));
    sp.addsqlParm(",credit_limit_s_date	   ", ",?", colStr("credit_limit_s_date"));
    sp.addsqlParm(",credit_limit_e_date	   ", ",?", colStr("credit_limit_e_date"));
    sp.addsqlParm(",credit_limit_code	      ", ",?", colStr("credit_limit_code"));
    sp.addsqlParm(",rc_avguse_cond	         ", ",?", colStr("rc_avguse_cond"));
    sp.addsqlParm(",rc_avguse_mm	         ", ",?", colInt("rc_avguse_mm"));
    sp.addsqlParm(",rc_avguse_rate	         ", ",?", colNum("rc_avguse_rate"));
    sp.addsqlParm(",cash_use_cond	         ", ",?", colStr("cash_use_cond"));
    sp.addsqlParm(",cash_use_mm	            ", ",?", colInt("cash_use_mm"));
    sp.addsqlParm(",cash_use_times	         ", ",?", colInt("cash_use_times"));
    sp.addsqlParm(",limit_avguse_cond	      ", ",?", colStr("limit_avguse_cond"));
    sp.addsqlParm(",limit_avguse_mm	      ", ",?", colInt("limit_avguse_mm"));
    sp.addsqlParm(",limit_avguse_rate	      ", ",?", colNum("limit_avguse_rate"));
    sp.addsqlParm(",payment_rate_cond	      ", ",?", colStr("payment_rate_cond"));
    sp.addsqlParm(",payment_rate_mm	      ", ",?", colInt("payment_rate_mm"));
    sp.addsqlParm(",payment_rate_times	   ", ",?", colInt("payment_rate_times"));
    sp.addsqlParm(",no_debt_cond	         ", ",?", colStr("no_debt_cond"));
    sp.addsqlParm(",no_debt_mm	            ", ",?", colInt("no_debt_mm"));
    sp.addsqlParm(",payment_int_cond	      ", ",?", colStr("payment_int_cond"));
    sp.addsqlParm(",acct_jrnl_bal_cond	   ", ",?", colStr("acct_jrnl_bal_cond"));
    sp.addsqlParm(",acct_jrnl_bal_s	      ", ",?", colNum("acct_jrnl_bal_s"));
    sp.addsqlParm(",acct_jrnl_bal_e	      ", ",?", colNum("acct_jrnl_bal_e"));
    sp.addsqlParm(",trial_score_cond	      ", ",?", colStr("trial_score_cond"));
    sp.addsqlParm(",trial_score_s	         ", ",?", colNum("trial_score_s"));
    sp.addsqlParm(",trial_score_e	         ", ",?", colNum("trial_score_e"));
    sp.addsqlParm(",no_assure_add_cond	   ", ",?", colStr("no_assure_add_cond"));
    sp.addsqlParm(",no_assure_add_amt	      ", ",?", colNum("no_assure_add_amt"));
    sp.addsqlParm(",jcic028_cond	         ", ",?", colStr("jcic028_cond"));
    sp.addsqlParm(",jcic028_s	            ", ",?", colNum("jcic028_s"));
    sp.addsqlParm(",jcic028_e	            ", ",?", colNum("jcic028_e"));
    sp.addsqlParm(",jcic029_cond	         ", ",?", colStr("jcic029_cond"));
    sp.addsqlParm(",jcic029_s	            ", ",?", colNum("jcic029_s"));
    sp.addsqlParm(",jcic029_e	            ", ",?", colNum("jcic029_e"));
    sp.addsqlParm(",no_assure_add_amt2	   ", ",?", colNum("no_assure_add_amt2"));
    sp.addsqlParm(",jcic036_cond	         ", ",?", colStr("jcic036_cond"));
    sp.addsqlParm(",jcic036	               ", ",?", colStr("jcic036"));
    sp.addsqlParm(",jcic030_cond	         ", ",?", colStr("jcic030_cond"));
    sp.addsqlParm(",jcic030	               ", ",?", colStr("jcic030"));
    sp.addsqlParm(",jcic031_cond	         ", ",?", colStr("jcic031_cond"));
    sp.addsqlParm(",jcic031	               ", ",?", colStr("jcic031"));
    sp.addsqlParm(",jcic023_03_cond	      ", ",?", colStr("jcic023_03_cond"));
    sp.addsqlParm(",jcic023_03	            ", ",?", colInt("jcic023_03"));
    sp.addsqlParm(",jcic025_01_cond	      ", ",?", colStr("jcic025_01_cond"));
    sp.addsqlParm(",jcic025_01	            ", ",?", colInt("jcic025_01"));
    sp.addsqlParm(",jcic030_01_cond	      ", ",?", colStr("jcic030_01_cond"));
    sp.addsqlParm(",jcic030_01	            ", ",?", colStr("jcic030_01"));
    sp.addsqlParm(",jcic030_02_cond	      ", ",?", colStr("jcic030_02_cond"));
    sp.addsqlParm(",jcic030_02	            ", ",?", colStr("jcic030_02"));
    sp.addsqlParm(",jcic031_01_cond	      ", ",?", colStr("jcic031_01_cond"));
    sp.addsqlParm(",jcic031_01	            ", ",?", colStr("jcic031_01"));
    sp.addsqlParm(",jcic031_02_cond	      ", ",?", colStr("jcic031_02_cond"));
    sp.addsqlParm(",jcic031_02	            ", ",?", colStr("jcic031_02"));
    sp.addsqlParm(",jcic034_cond	         ", ",?", colStr("jcic034_cond"));
    sp.addsqlParm(",jcic034	               ", ",?", colStr("jcic034"));
    sp.addsqlParm(",jcic032_cond	         ", ",?", colStr("jcic032_cond"));
    sp.addsqlParm(",jcic032	               ", ",?", colStr("jcic032"));
    sp.addsqlParm(",jcic004_01_cond	      ", ",?", colStr("jcic004_01_cond"));
    sp.addsqlParm(",jcic004_01	            ", ",?", colNum("jcic004_01"));
    sp.addsqlParm(",jcic009_cond	         ", ",?", colStr("jcic009_cond"));
    sp.addsqlParm(",jcic009	               ", ",?", colNum("jcic009"));
    sp.addsqlParm(",jcic010_02_cond	      ", ",?", colStr("jcic010_02_cond"));
    sp.addsqlParm(",jcic010_02	            ", ",?", colInt("jcic010_02"));
    sp.addsqlParm(",jcic013_cond	         ", ",?", colStr("jcic013_cond"));
    sp.addsqlParm(",jcic013	               ", ",?", colStr("jcic013"));
    sp.addsqlParm(",jcic023_01_cond	      ", ",?", colStr("jcic023_01_cond"));
    sp.addsqlParm(",jcic023_01	            ", ",?", colInt("jcic023_01"));
    sp.addsqlParm(",jcic023_02_cond	      ", ",?", colStr("jcic023_02_cond"));
    sp.addsqlParm(",jcic023_02	            ", ",?", colInt("jcic023_02"));
    sp.addsqlParm(",jcic023_03_e	         ", ",?", colInt("jcic023_03_e"));
    sp.addsqlParm(",jcic025_01_e	         ", ",?", colInt("jcic025_01_e"));
    sp.addsqlParm(",jcic004_01_e	         ", ",?", colNum("jcic004_01_e"));
    sp.addsqlParm(",jcic009_e	            ", ",?", colNum("jcic009_e"));
    sp.addsqlParm(",jcic010_02_e	         ", ",?", colInt("jcic010_02_e"));
    sp.addsqlParm(",jcic023_01_e	         ", ",?", colInt("jcic023_01_e"));
    sp.addsqlParm(",jcic023_02_e	         ", ",?", colInt("jcic023_02_e"));
    sp.addsqlParm(",block_reason_cond	      ", ",?", colStr("block_reason_cond"));
    sp.addsqlParm(",acno_block_reason	      ", ",?", colStr("acno_block_reason"));
    sp.addsqlParm(",crt_user	               ", ",?", colStr("crt_user"));
    sp.addsqlParm(",crt_date	               ", ",?", colStr("crt_date"));
    sp.addsqlParm(",apr_flag	               ", ",'Y'");
    sp.addsqlParm(",apr_date	               ", "," + commSqlStr.sysYYmd);
    sp.addsqlParm(",apr_user	               ", ",?", wp.itemStr("approval_user"));
    sp.addsqlParm(",mod_user	               ", ",?", modUser);
    sp.addsqlParm(",mod_time	               ", "," + commSqlStr.sysdate);
    sp.addsqlParm(",mod_pgm	               ", ",?", modPgm);
    sp.addsqlParm(",mod_seqno	            ", ",?", colNum("mod_seqno"));

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = 1;
    }

  }

}
