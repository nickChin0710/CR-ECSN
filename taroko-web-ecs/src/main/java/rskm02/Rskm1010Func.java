/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package rskm02;
/** 期中覆審查詢名單產生參數維護
 * 2019-1121:  Alex  bugfix
 * 2019-0516:  JH    modify
 * 2019-0311:  JH    bugfix
 * 2018-0525:	JH		dataCheck()
 * */

public class Rskm1010Func extends busi.FuncAction {

  String batchNo;

  @Override
  public void dataCheck() {

    if (this.ibAdd) {
      batchNo = wp.itemStr("kk_batch_no");
    } else {
      batchNo = wp.itemStr("batch_no");
    }

    if (empty(batchNo)) {
      errmsg("查詢批號：不可空白");
      return;
    }

    if (this.ibAdd) {
      return;
    }

    if (this.ibUpdate) {
      if (wp.itemEq("apr_flag", "Y")) {
        errmsg("已覆核,不可異動");
        return;
      }
    }

    if (wp.itemEq("sample_flag", "Y")) {
      errmsg("此資料為樣板參數, 不可在此異動; 可由[rskm1012]修改");
      return;
    }

    if (wp.itemEq("regist_type", "2") == false) {
      errmsg("非批次產生之名單, 不可異動");
      return;
    }

    if (wp.itemEq("assign_list_cond", "Y")) {
      if (empty(wp.itemStr("card_since_s_date")) == false
          || empty(wp.itemStr("card_since_e_date")) == false) {
        errmsg("已指定覆審名單, 不可再設定其他條件");
        return;
      }
    }

    if (ibDelete || ibUpdate) {
      if (trialList(batchNo)) {
        errmsg("已產生名單不可修改或刪除");
      }
    }

    if (ibDelete) {
      return;
    }

    if (wp.itemEq("assign_list_cond", "Y")) {
      if (wp.itemEmpty("card_since_s_date") == false
          || wp.itemEmpty("card_since_e_date") == false) {
        errmsg("已指定覆審名單, 不可再設定其他條件");
        return;
      }

      String allStr = "|" + wp.itemStr("payment_rate1_cond") + wp.itemStr("all_credit_limit_cond")
          + wp.itemStr("credit_limit_cond") + wp.itemStr("class_code_cond")
          + wp.itemStr("pd_rating_cond") + wp.itemStr("reg_bank_no_cond")
          + wp.itemStr("adj_limit_cond") + wp.itemStr("risk_group_cond")
          + wp.itemStr("rc_avg_rate_cond") + wp.itemStr("prepay_cash_cond")
          + wp.itemStr("limit_avguse_rate_cond") + wp.itemStr("payment_rate_cond")
          + wp.itemStr("no_debt_cond") + wp.itemStr("payment_integ_cond")
          + wp.itemStr("acct_jrnl_bal_cond") + wp.itemStr("block_reason_cond")
          + wp.itemStr("has_card_cond") + wp.itemStr("asset_cond") + wp.itemStr("card_issue_cond");

      if (pos(allStr, "Y") > 0) {
        errmsg("已指定覆審名單, 不可再設定其他條件");
        return;
      }
    }



    if (this.ibUpdate) {
      if (wp.itemEmpty("imp_jcic_date") == false) {
        errmsg("資料已轉入 JCIC 查詢, 不可修改");
        return;
      }
    }

    if (this.chkStrend(wp.itemStr("card_since_s_date"), wp.itemStr("card_since_e_date")) == -1) {
      errmsg("有效卡最早發卡日 起迄輸入錯誤");
      return;
    }

    if (wp.itemEq("payment_rate1_cond", "Y")) {
      if (pos(wp.itemStr("payment_rate1"), "Y") < 0) {
        errmsg("請指定最近一期之繳款評等");
        return;
      }
    }

    if (wp.itemEq("all_credit_limit_cond", "Y")) {
      if (wp.itemNum("all_credit_limit_e") == 0) {
        errmsg("總歸戶額度迄 不可為 0");
        return;
      }
      if (wp.itemNum("all_credit_limit_s") > wp.itemNum("all_credit_limit_e")) {
        errmsg("總歸戶額度 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("all_credit_limit_s", "" + 0);
      wp.itemSet("all_credit_limit_e", "" + 0);
    }

    if (wp.itemEq("credit_limit_cond", "Y")) {
      if (wp.itemNum("credit_limit_e") == 0) {
        errmsg("帳戶額度迄 不可為 0");
        return;
      }
      if (wp.itemNum("credit_limit_s") > wp.itemNum("credit_limit_e")) {
        errmsg("帳戶額度 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("credit_limit_s", "" + 0);
      wp.itemSet("credit_limit_e", "" + 0);
    }

    if (wp.itemEq("rc_avg_rate_cond", "Y")) {
      if (wp.itemNum("rc_avg_rate_mm") == 0) {
        errmsg("RC平均使用率: 檢查月數不可為 0");
        return;
      }
      if (wp.itemNum("rc_avg_rate1") == 0 && wp.itemNum("rc_avg_rate2") == 0) {
        errmsg("RC平均使用率 及 最近一期 不可同時為 0");
        return;
      }
    } else {
      wp.itemSet("rc_avg_rate_mm", "" + 0);
      wp.itemSet("rc_avg_rate1", "" + 0);
      wp.itemSet("rc_avg_rate2", "" + 0);
    }

    if (wp.itemEq("prepay_cash_cond", "Y")) {
      if (wp.itemNum("prepay_cash_mm") == 0) {
        errmsg("預借現金使用: 檢查月數不可為 0");
        return;
      }
      if (wp.itemNum("prepay_cash_num") == 0) {
        errmsg("預借現金使用: 次數不可為 0");
        return;
      }
    } else {
      wp.itemSet("prepay_cash_mm", "" + 0);
      wp.itemSet("prepay_cash_num", "" + 0);
    }

    if (wp.itemEq("limit_avguse_rate_cond", "Y")) {
      if (wp.itemNum("limit_avguse_rate_mm") == 0) {
        errmsg("額度平均動用率: 檢查月數不可為 0");
        return;
      }
      if (wp.itemNum("limit_avguse_rate") == 0) {
        errmsg("額度平均動用率 不可為 0");
        return;
      }
    } else {
      wp.itemSet("limit_avguse_rate_mm", "" + 0);
      wp.itemSet("limit_avguse_rate", "" + 0);
    }

    if (wp.itemEq("payment_rate_cond", "Y")) {
      if (wp.itemNum("payment_rate_mm") == 0) {
        errmsg("帳戶逾期(M1): 檢查月數不可為 0");
        return;
      }
      if (wp.itemNum("payment_rate_num") == 0) {
        errmsg("帳戶逾期(M1) 次數不可為 0");
        return;
      }
    } else {
      wp.itemSet("payment_rate_mm", "" + 0);
      wp.itemSet("payment_rate_num", "" + 0);
    }

    if (wp.itemEq("no_debt_cond", "Y")) {
      if (wp.itemNum("no_debt_mm") == 0) {
        errmsg("無消費且無欠款: 檢查月數不可為 0");
        return;
      }
    } else {
      wp.itemSet("no_debt_mm", "" + 0);
    }

    if (wp.itemEq("acct_jrnl_bal_cond", "Y")) {
      if (wp.itemNum("acct_jrnl_bal_s") == 0 && wp.itemNum("acct_jrnl_bal_e") == 0) {
        errmsg("欠款金額: 不可為同時為 0");
        return;
      }
      if (wp.itemNum("acct_jrnl_bal_s") > wp.itemNum("acct_jrnl_bal_e")) {
        errmsg("欠款金額: 起迄輸入錯誤");
        return;
      }
    } else {
      wp.itemSet("acct_jrnl_bal_s", "" + 0);
      wp.itemSet("acct_jrnl_bal_e", "" + 0);
    }

    if (wp.itemEq("excl_retrial_cond", "Y")) {
      if (wp.itemNum("excl_retrial_mm") == 0) {
        errmsg("覆審作業: 檢查月數不可為 0");
        return;
      }
    } else {
      wp.itemSet("excl_retrial_mm", "" + 0);
    }

    if (wp.itemNum("rc_avg_rate_mm") > 24 || wp.itemNum("prepay_cash_mm") > 24
        || wp.itemNum("limit_avguse_rate_mm") > 24 || wp.itemNum("payment_rate_mm") > 24
        || wp.itemNum("no_debt_mm") > 24 || wp.itemNum("excl_m1_mm") > 24
        || wp.itemNum("excl_0d_mm") > 24 || wp.itemNum("excl_m2_mm") > 24) {
      errmsg("帳戶月數 需 <= 24 月");
      return;
    }

    if (wp.itemEq("excl_m1_cond", "Y")) {
      if (wp.itemNum("excl_m1_mm") <= 0 || wp.itemNum("excl_m1_cnt") <= 0) {
        errmsg("請輸入 排除條件[M1] 之月數及次數");
        return;
      }
    }

    if (wp.itemEq("excl_0d_cond", "Y")) {
      if (wp.itemNum("excl_0d_mm") <= 0 || wp.itemNum("excl_0d_cnt") <= 0) {
        errmsg("請輸入 排除條件[0D] 之月數及次數");
        return;
      }
    }

    if (wp.itemEq("excl_m2_cond", "Y")) {
      if (wp.itemNum("excl_m2_mm") <= 0 || wp.itemNum("excl_m2_cnt") <= 0) {
        errmsg("請輸入 排除條件[M2] 之月數及次數");
        return;
      }
    }

    // if(wp.item_eq("assign_list_cond", "Y")){
    // if(wp.item_num("assign_list_mm")<=0){
    // errmsg("指定名單間隔月數 須大於 0");
    // return;
    // }
    // }

    if (wp.itemEq("asset_cond", "Y")) {
      if (wp.itemEq("asset_ch_flag", "0") && wp.itemEq("asset_value_flag", "0")) {
        errmsg("有無擔保條件 設定錯誤, 不可同時為 [全體卡戶+全部]");
        return;
      }
    }

    if (wp.itemEmpty("action_batch_no")) {
      errmsg("ACTION 版本號碼 不可空白");
      return;
    }

    String lsAction = wp.itemStr("action_batch_no");
    if (actionAatchNo(lsAction) == false) {
      errmsg("ACTION版本號碼 不存在");
      return;
    }

    if (wp.itemEq("card_issue_cond", "Y")) {
      if (wp.itemNum("card_issue_mm") == 0) {
        errmsg("核發個人卡月數 須大於0");
        return;
      }
    } else {
      wp.itemSet("card_issue_mm", "" + 0);
    }

    if (this.ibAdd) {
      return;
    }

    sqlWhere =
        " where nvl(sample_flag,'N')<>'Y' " + " and batch_no =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {batchNo, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("rsk_trial_parm", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }

  }

  public int mastDetlSynch(String aBatchNo) {
    msgOK();
    // if (wp.col_eq("apr_flag","Y")) {
    // errmsg("己覆核資料, 不可異動");
    // return rc;
    // }

    strSql = "select sum(decode(data_type,'01',1,0)) as db_01_cnt"
        + ", sum(decode(data_type,'02',1,0)) as db_02_cnt"
        + ", sum(decode(data_type,'03',1,0)) as db_03_cnt"
        + ", sum(decode(data_type,'04',1,0)) as db_04_cnt"
        + ", sum(decode(data_type,'05',1,0)) as db_05_cnt"
        + ", sum(decode(data_type,'06',1,0)) as db_06_cnt"
        + ", sum(decode(data_type,'07',1,0)) as db_07_cnt"
        + ", sum(decode(data_type,'08',1,0)) as db_08_cnt"
        + ", sum(decode(data_type,'09',1,0)) as db_09_cnt"
        + ", sum(decode(data_type,'10',1,0)) as db_10_cnt" + " from rsk_trial_parmdtl"
        + " where batch_no =? and apr_flag <>'Y'";
    setString2(1, aBatchNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return 1;

    String[] aaCond = new String[10];
    aaCond[0] = "N"; // (col_int("db_01_cnt")>0 ? "Y" : "N");
    aaCond[1] = "N"; // (col_int("db_02_cnt")>0 ? "Y" : "N");
    aaCond[2] = (colInt("db_03_cnt") > 0 ? "Y" : "N");
    aaCond[3] = (colInt("db_04_cnt") > 0 ? "Y" : "N");
    aaCond[4] = (colInt("db_05_cnt") > 0 ? "Y" : "N");
    aaCond[5] = (colInt("db_06_cnt") > 0 ? "Y" : "N");
    aaCond[6] = (colInt("db_07_cnt") > 0 ? "Y" : "N");
    aaCond[7] = (colInt("db_08_cnt") > 0 ? "Y" : "N");
    aaCond[8] = (colInt("db_09_cnt") > 0 ? "Y" : "N");
    aaCond[9] = (colInt("db_10_cnt") > 0 ? "Y" : "N");

    sql2Update("rsk_trial_parm");
    addsqlParm("class_code_cond =?", aaCond[2]);
    addsqlParm(", pd_rating_cond =?", aaCond[3]);
    addsqlParm(", reg_bank_no_cond =?", aaCond[4]);
    addsqlParm(", adj_limit_cond =?", aaCond[5]);
    addsqlParm(", risk_group_cond =?", aaCond[6]);
    addsqlParm(", excl_block_cond =?", aaCond[7]);
    addsqlParm(", block_reason_cond =?", aaCond[8]);
    addsqlParm(", excl_group_code_cond =?", aaCond[9]);
    sqlWhere(" where batch_no =?", wp.colStr("batch_no"));
    sqlWhere(" and apr_flag <>?", "Y");
    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_parm error");
    }

    return rc;
  }

  boolean trialList(String batchNo) {
    if (empty(batchNo))
      return false;

    String sql1 = "select count(*) as db_cnt " + " from rsk_trial_list" + " where batch_no =?";
    sqlSelect(sql1, new Object[] {batchNo});
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  boolean actionAatchNo(String lsAction) {
    if (empty(lsAction))
      return false;

    String sql1 = "select count(*) as db_cnt " + " from rsk_trial_action" + " where batch_no =?";
    sqlSelect(sql1, new Object[] {lsAction});
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

    String PR1 = wp.itemNvl("db_payrate01", "N") + wp.itemNvl("db_payrate02", "N")
        + wp.itemNvl("db_payrate03", "N") + wp.itemNvl("db_payrate04", "N")
        + wp.itemNvl("db_payrate05", "N") + wp.itemNvl("db_payrate06", "N")
        + wp.itemNvl("db_payrate07", "N");

    sql2Insert("rsk_trial_parm");
    addsqlParm("?", "batch_no", batchNo); // 1
    addsqlParm(",?", ", query_date", sysDate);
    addsqlParm(",?", ", trial_reason", wp.itemStr2("trial_reason"));
    addsqlParm(",?", ", jcic_no", wp.itemStr2("jcic_no"));
    addsqlParm(",?", ", card_since_s_date", wp.itemStr2("card_since_s_date"));
    addsqlParm(",?", ", card_since_e_date", wp.itemStr2("card_since_e_date"));
    addsqlParm(",?", ", payment_rate1_cond", wp.itemStr2("payment_rate1_cond"));
    addsqlParm(",?", ", payment_rate1", PR1);
    addsqlParm(",?", ", all_credit_limit_cond", wp.itemStr2("all_credit_limit_cond"));
    addsqlParm(",?", ", all_credit_limit_s", wp.colNum("all_credit_limit_s"));
    addsqlParm(",?", ", all_credit_limit_e", wp.colNum("all_credit_limit_e"));
    addsqlParm(",?", ", credit_limit_cond", wp.itemNvl("credit_limit_cond", "N"));
    addsqlParm(",?", ", credit_limit_s", wp.colNum("credit_limit_s"));
    addsqlParm(",?", ", credit_limit_e", wp.colNum("credit_limit_e"));
    addsqlParm(",?", ", group_code_cond", wp.itemNvl("group_code_cond", "N"));
    addsqlParm(",?", ", class_code_cond", wp.itemNvl("class_code_cond", "N"));
    addsqlParm(",?", ", pd_rating_cond", wp.itemNvl("pd_rating_cond", "N"));
    addsqlParm(",?", ", reg_bank_no_cond", wp.itemNvl("reg_bank_no_cond", "N"));
    addsqlParm(",?", ", adj_limit_cond", wp.itemNvl("adj_limit_cond", "N"));
    addsqlParm(",?", ", adj_limit_s_date", wp.itemStr2("adj_limit_s_date"));
    addsqlParm(",?", ", adj_limit_e_date", wp.itemStr2("adj_limit_e_date"));
    addsqlParm(",?", ", risk_group_cond", wp.itemNvl("risk_group_cond", "N"));
    addsqlParm(",?", ", rc_avg_rate_cond", wp.itemNvl("rc_avg_rate_cond", "N"));
    addsqlParm(",?", ", rc_avg_rate_mm", wp.colNum("rc_avg_rate_mm"));
    addsqlParm(",?", ", rc_avg_rate1", wp.colNum("rc_avg_rate1"));
    addsqlParm(",?", ", rc_avg_rate2", wp.colNum("rc_avg_rate2"));
    addsqlParm(",?", ", prepay_cash_cond", wp.itemNvl("prepay_cash_cond", "N"));
    addsqlParm(",?", ", prepay_cash_mm", wp.colInt("prepay_cash_mm"));
    addsqlParm(",?", ", prepay_cash_num", wp.colNum("prepay_cash_num"));
    addsqlParm(",?", ", limit_avguse_rate_cond", wp.itemNvl("limit_avguse_rate_cond", "N"));
    addsqlParm(",?", ", limit_avguse_rate_mm", wp.colNum("limit_avguse_rate_mm"));
    addsqlParm(",?", ", limit_avguse_rate", wp.colNum("limit_avguse_rate"));
    addsqlParm(",?", ", payment_rate_cond", wp.itemNvl("payment_rate_cond", "N"));
    addsqlParm(",?", ", payment_rate_mm", wp.colInt("payment_rate_mm"));
    addsqlParm(",?", ", payment_rate_num", wp.colNum("payment_rate_num"));
    addsqlParm(",?", ", no_debt_cond", wp.itemNvl("no_debt_cond", "N"));
    addsqlParm(",?", ", no_debt_mm", wp.colInt("no_debt_mm"));
    addsqlParm(",?", ", payment_integ_cond", wp.itemNvl("payment_integ_cond", "N"));
    addsqlParm(",?", ", acct_jrnl_bal_cond", wp.itemNvl("acct_jrnl_bal_cond", "N"));
    addsqlParm(",?", ", acct_jrnl_bal_s", wp.colNum("acct_jrnl_bal_s"));
    addsqlParm(",?", ", acct_jrnl_bal_e", wp.colNum("acct_jrnl_bal_e"));
    addsqlParm(",?", ", excl_retrial_cond", wp.itemNvl("excl_retrial_cond", "N"));
    addsqlParm(",?", ", excl_retrial_mm", wp.colInt("excl_retrial_mm"));
    addsqlParm(",?", ", excl_block_cond", wp.itemNvl("excl_block_cond", "N"));
    addsqlParm(",?", ", regist_type", "2");
    addsqlParm(",?", ", score_type", wp.itemStr2("score_type"));
    addsqlParm(",?", ", excl_asset_cond", wp.itemNvl("excl_asset_cond", "N"));
    addsqlParm(",?", ", block_reason_cond", wp.itemNvl("block_reason_cond", "N"));
    addsqlParm(",?", ", excl_m1_cond", wp.itemNvl("excl_m1_cond", "N"));
    addsqlParm(",?", ", excl_m1_mm", wp.colInt("excl_m1_mm"));
    addsqlParm(",?", ", excl_m1_cnt", wp.colInt("excl_m1_cnt"));
    addsqlParm(",?", ", excl_0d_cond", wp.itemNvl("excl_0d_cond", "N"));
    addsqlParm(",?", ", excl_0d_mm", wp.colInt("excl_0d_mm"));
    addsqlParm(",?", ", excl_0d_cnt", wp.colInt("excl_0d_cnt"));
    addsqlParm(",?", ", excl_m2_cond", wp.itemNvl("excl_m2_cond", "N"));
    addsqlParm(",?", ", excl_m2_mm", wp.colNum("excl_m2_mm"));
    addsqlParm(",?", ", excl_m2_cnt", wp.colNum("excl_m2_cnt"));
    addsqlParm(",?", ", excl_group_code_cond", wp.itemNvl("excl_group_code_cond", "N"));
    addsqlParm(",?", ", has_card_cond", wp.itemNvl("has_card_cond", "N"));
    addsqlParm(",?", ", has_card_value", wp.itemStr2("has_card_value"));
    addsqlParm(",?", ", assign_list_cond", wp.itemNvl("assign_list_cond", "N"));
    addsqlParm(",?", ", assign_list_mm", wp.colNum("assign_list_mm"));
    addsqlParm(",?", ", asset_cond", wp.itemNvl("asset_cond", "N"));
    addsqlParm(",?", ", asset_ch_flag", wp.itemNvl("asset_ch_flag", "N"));
    addsqlParm(",?", ", asset_value_flag", wp.itemNvl("asset_value_flag", "N"));
    addsqlParm(",?", ", copy_batch_no", wp.itemStr2("copy_batch_no"));
    addsqlParm(",?", ", sample_flag", "N");
    addsqlParm(",?", ", action_batch_no", wp.itemStr2("action_batch_no"));
    addsqlParm(",?", ", trans_type", wp.itemStr2("trans_type"));
    addsqlParm(",?", ", card_issue_cond", wp.itemNvl("card_issue_cond", "N"));
    addsqlParm(",?", ", card_issue_mm", wp.colInt("card_issue_mm"));
    addsqlParm(",?", ", apr_flag", "N");
    addsqlParm(",?", ", crt_date", wp.sysDate);
    addsqlParm(",?", ", crt_user", modUser);
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    String PR1 = wp.itemNvl("db_payrate01", "N") + wp.itemNvl("db_payrate02", "N")
        + wp.itemNvl("db_payrate03", "N") + wp.itemNvl("db_payrate04", "N")
        + wp.itemNvl("db_payrate05", "N") + wp.itemNvl("db_payrate06", "N")
        + wp.itemNvl("db_payrate07", "N");

    strSql = "update RSK_TRIAL_PARM set " + " trial_reason =:trial_reason, "
        + " jcic_no =:jcic_no, " + " card_since_s_date =:card_since_s_date, "
        + " card_since_e_date =:card_since_e_date, " + " payment_rate1_cond =:payment_rate1_cond , "
        + " payment_rate1 =:PR1, " + " all_credit_limit_cond =:all_credit_limit_cond, "
        + " all_credit_limit_s =:all_credit_limit_s, "
        + " all_credit_limit_e =:all_credit_limit_e, " + " credit_limit_cond =:credit_limit_cond , "
        + " credit_limit_s =:credit_limit_s, " + " credit_limit_e =:credit_limit_e, "
        + " group_code_cond =:group_code_cond, " + " class_code_cond =:class_code_cond, "
        + " pd_rating_cond =:pd_rating_cond, " + " reg_bank_no_cond =:reg_bank_no_cond, "
        + " adj_limit_cond =:adj_limit_cond, " + " adj_limit_s_date =:adj_limit_s_date, "
        + " adj_limit_e_date =:adj_limit_e_date, " + " risk_group_cond =:risk_group_cond, "
        + " rc_avg_rate_cond =:rc_avg_rate_cond, " + " rc_avg_rate_mm =:rc_avg_rate_mm, "
        + " rc_avg_rate1 =:rc_avg_rate1, " + " rc_avg_rate2 =:rc_avg_rate2, "
        + " prepay_cash_cond =:prepay_cash_cond, " + " prepay_cash_mm =:prepay_cash_mm, "
        + " prepay_cash_num =:prepay_cash_num, "
        + " limit_avguse_rate_cond =:limit_avguse_rate_cond, "
        + " limit_avguse_rate_mm =:limit_avguse_rate_mm , "
        + " limit_avguse_rate =:limit_avguse_rate, " + " payment_rate_cond =:payment_rate_cond, "
        + " payment_rate_mm =:payment_rate_mm, " + " payment_rate_num =:payment_rate_num, "
        + " no_debt_cond =:no_debt_cond, " + " no_debt_mm =:no_debt_mm, "
        + " payment_integ_cond =:payment_integ_cond, "
        + " acct_jrnl_bal_cond =:acct_jrnl_bal_cond, " + " acct_jrnl_bal_s =:acct_jrnl_bal_s, "
        + " acct_jrnl_bal_e =:acct_jrnl_bal_e, " + " excl_retrial_cond =:excl_retrial_cond, "
        + " excl_retrial_mm =:excl_retrial_mm, " + " excl_block_cond =:excl_block_cond, "
        + " score_type =:score_type, " + " excl_asset_cond =:excl_asset_cond , "
        + " block_reason_cond =:block_reason_cond , " + " excl_m1_cond =:excl_m1_cond , "
        + " excl_m1_mm =:excl_m1_mm , " + " excl_m1_cnt =:excl_m1_cnt, "
        + " excl_0d_cond =:excl_0d_cond , " + " excl_0d_mm =:excl_0d_mm , "
        + " excl_0d_cnt =:excl_0d_cnt , " + " excl_m2_cond =:excl_m2_cond , "
        + " excl_m2_mm =:excl_m2_mm , " + " excl_m2_cnt =:excl_m2_cnt , "
        + " excl_group_code_cond =:excl_group_code_cond , " + " has_card_cond =:has_card_cond, "
        + " has_card_value =:has_card_value , " + " assign_list_cond =:assign_list_cond, "
        + " assign_list_mm =:assign_list_mm , " + " asset_cond =:asset_cond , "
        + " asset_ch_flag =:asset_ch_flag , " + " asset_value_flag =:asset_value_flag , "
        + " action_batch_no =:action_batch_no , " + " trans_type =:trans_type , "
        + " card_issue_cond =:card_issue_cond , " + " card_issue_mm =:card_issue_mm , "
        + " apr_flag =:apr_flag , " + " apr_date =:apr_date , " + " apr_user =:apr_user , "
        + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1" + " where batch_no =:kk "
        + " and nvl(mod_seqno,0) =:mod_seqno ";;

    setString("kk", batchNo);
    item2ParmStr("trial_reason");
    item2ParmStr("jcic_no");
    item2ParmStr("card_since_s_date");
    item2ParmStr("card_since_e_date");
    item2ParmNvl("payment_rate1_cond", "N");
    setString("PR1", PR1);
    item2ParmNvl("all_credit_limit_cond", "N");
    item2ParmNum("all_credit_limit_s");
    item2ParmNum("all_credit_limit_e");
    item2ParmNvl("credit_limit_cond", "N");
    item2ParmNum("credit_limit_s");
    item2ParmNum("credit_limit_e");
    item2ParmNvl("group_code_cond", "N");
    item2ParmNvl("class_code_cond", "N");
    item2ParmStr("pd_rating_cond");
    item2ParmNvl("reg_bank_no_cond", "N");
    item2ParmNvl("adj_limit_cond", "N");
    item2ParmStr("adj_limit_s_date");
    item2ParmStr("adj_limit_e_date");
    item2ParmNvl("risk_group_cond", "N");
    item2ParmNvl("rc_avg_rate_cond", "N");
    item2ParmNum("rc_avg_rate_mm");
    item2ParmNum("rc_avg_rate1");
    item2ParmNum("rc_avg_rate2");
    item2ParmNvl("prepay_cash_cond", "N");
    item2ParmNum("prepay_cash_mm");
    item2ParmNum("prepay_cash_num");
    item2ParmNvl("limit_avguse_rate_cond", "N");
    item2ParmNum("limit_avguse_rate_mm");
    item2ParmNum("limit_avguse_rate");
    item2ParmNvl("payment_rate_cond", "N");
    item2ParmNum("payment_rate_mm");
    item2ParmNum("payment_rate_num");
    item2ParmNvl("no_debt_cond", "N");
    item2ParmNum("no_debt_mm");
    item2ParmNvl("payment_integ_cond", "N");
    item2ParmNvl("acct_jrnl_bal_cond", "N");
    item2ParmNum("acct_jrnl_bal_s");
    item2ParmNum("acct_jrnl_bal_e");
    item2ParmNvl("excl_retrial_cond", "N");
    item2ParmNum("excl_retrial_mm");
    item2ParmNvl("excl_block_cond", "N");
    item2ParmStr("score_type");
    item2ParmNvl("excl_asset_cond", "N");
    item2ParmNvl("block_reason_cond", "N");
    item2ParmNvl("excl_m1_cond", "N");
    item2ParmNum("excl_m1_mm");
    item2ParmNum("excl_m1_cnt");
    item2ParmNvl("excl_0d_cond", "N");
    item2ParmNum("excl_0d_mm");
    item2ParmNum("excl_0d_cnt");
    item2ParmNvl("excl_m2_cond", "N");
    item2ParmNum("excl_m2_mm");
    item2ParmNum("excl_m2_cnt");
    item2ParmNvl("excl_group_code_cond", "N");
    item2ParmNvl("has_card_cond", "N");
    item2ParmStr("has_card_value");
    item2ParmNvl("assign_list_cond", "N");
    item2ParmNum("assign_list_mm");
    item2ParmNvl("asset_cond", "N");
    item2ParmStr("asset_ch_flag");
    item2ParmStr("asset_value_flag");
    item2ParmStr("action_batch_no");
    item2ParmStr("trans_type");
    item2ParmNvl("card_issue_cond", "N");
    item2ParmNum("card_issue_mm");
    item2ParmStr("apr_flag");
    item2ParmStr("apr_date");
    item2ParmStr("apr_user");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }


  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete RSK_TRIAL_PARM " + " where batch_no =:kk1" + " and apr_flag =:apr_flag "
        + " and nvl(mod_seqno,0) =:mod_seqno ";
    setString2("kk1", batchNo);
    setString2("apr_flag", wp.colStr("apr_flag"));
    setDouble2("mod_seqno", wp.colNum("mod_seqno"));
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    // dbDelete_detl();

    strSql = "delete rsk_trial_parmdtl" + " where batch_no =:kk1" + " and apr_flag =:apr_flag";
    setString2("kk1", batchNo);
    setString2("apr_flag", wp.colNvl("apr_flag", "N"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete rsk_trial_parmdtl error; " + this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dataProc() {
    return 0;
  }

  public int batchCopyProc() {
    String lsBatchNo = wp.itemStr("kk_batch_no");
    String lsCopyNo = wp.itemStr("db_copy_no");
    if (empty(lsBatchNo) || empty(lsCopyNo)) {
      errmsg("查詢批號, 指定樣板批號: 不可空白");
      return rc;
    }

    // is_sql ="select count(*) from rsk_trial_parm"
    // +" where batch_no ='"+ls_batch_no+"'";
    sqlWhere = " where batch_no=:ls_batch_no";
    setString("ls_batch_no", lsBatchNo);
    if (sqlRowcount("rsk_trial_parm", sqlWhere) > 0) {
      errmsg("查詢批號: 已存在, 不可複製");
      return rc;
    }
    sqlWhere = " where batch_no=:ls_copy_no" + " and nvl(sample_flag,'n')='Y'";
    setString("ls_copy_no", lsCopyNo);
    if (sqlRowcount("rsk_trial_parm", sqlWhere) <= 0) {
      errmsg("指定樣板批號: 不存在, 無法複製");
      return rc;
    }

    insertRskTrialParm(lsBatchNo, lsCopyNo);
    if (rc == 1) {
      insertRskTrialParm2(lsBatchNo, lsCopyNo);
    }
    if (rc != 1) {
      errmsg("參數複製失敗");
    }
    return rc;
  }

  void insertRskTrialParm(String batchNo, String copyNo) {
    strSql = "" + "insert into rsk_trial_parm (" + "    batch_no" + "  , query_date"
        + "  , trial_reason" + "  , jcic_no" + "  , card_since_s_date" + "  , card_since_e_date"
        + "  , payment_rate1_cond" + "  , payment_rate1" + "  , all_credit_limit_cond "
        + "  , all_credit_limit_s" + "  , all_credit_limit_e" + "  , credit_limit_cond"
        + "  , credit_limit_s" + "  , credit_limit_e" + "  , group_code_cond"
        + "  , class_code_cond" + "  , pd_rating_cond" + "  , reg_bank_no_cond"
        + "  , adj_limit_cond" + "  , adj_limit_s_date" + "  , adj_limit_e_date"
        + "  , risk_group_cond" + "  , rc_avg_rate_cond" + "  , rc_avg_rate_mm" + "  , rc_avg_rate1"
        + "  , rc_avg_rate2" + "  , prepay_cash_cond" + "  , prepay_cash_mm" + "  , prepay_cash_num"
        + "  , limit_avguse_rate_cond" + "  , limit_avguse_rate_mm" + "  , limit_avguse_rate"
        + "  , payment_rate_cond" + "  , payment_rate_mm" + "  , payment_rate_num"
        + "  , no_debt_cond" + "  , no_debt_mm" + "  , payment_integ_cond"
        + "  , acct_jrnl_bal_cond" + "  , acct_jrnl_bal_s" + "  , acct_jrnl_bal_e"
        + "  , excl_retrial_cond" + "  , excl_retrial_mm" + "  , excl_block_cond"
        + "  , regist_type " + "  , imp_file_name" + "  , list_crt_date" + "  , list_crt_rows"
        + "  , imp_jcic_date" + "  , imp_jcic_user" + "  , imp_jcic_rows" + "  , batch_seqno"
        + "  , score_type" + "  , excl_asset_cond" + "  , block_reason_cond" + "  , excl_m1_cond "
        + "  , excl_m1_mm" + "  , excl_m1_cnt" + "  , excl_0d_cond" + "  , excl_0d_mm"
        + "  , excl_0d_cnt" + "  , excl_m2_cond" + "  , excl_m2_mm" + "  , excl_m2_cnt"
        + "  , excl_group_code_cond" + "  , has_card_cond" + "  , has_card_value "
        + "  , assign_list_cond" + "  , assign_list_mm " + "  , asset_cond " + "  , asset_ch_flag"
        + "  , asset_value_flag" + "  , copy_batch_no" + "  , sample_flag" + "  , parm3_proc_date"
        + "  , action_batch_no " + "  , trans_type" + "  , card_issue_cond" + "  , card_issue_mm"
        + "  , crt_user" + "  , crt_date" + "  , apr_flag" + "  , apr_date" + "  , apr_user"
        + "  , mod_user" + "  , mod_time" + "  , mod_pgm " + "  , mod_seqno" + " ) " + "select"
        + "    :batch_no" // batch_no
        + "  , :query_date "// query_date
        + "  , trial_reason" + "  , jcic_no" + "  , card_since_s_date" + "  , card_since_e_date"
        + "  , payment_rate1_cond " + "  , payment_rate1" + "  , all_credit_limit_cond "
        + "  , all_credit_limit_s" + "  , all_credit_limit_e " + "  , credit_limit_cond"
        + "  , credit_limit_s " + "  , credit_limit_e " + "  , group_code_cond"
        + "  , class_code_cond" + "  , pd_rating_cond " + "  , reg_bank_no_cond "
        + "  , adj_limit_cond  " + "  , adj_limit_s_date" + "  , adj_limit_e_date"
        + "  , risk_group_cond " + "  , rc_avg_rate_cond" + "  , rc_avg_rate_mm  "
        + "  , rc_avg_rate1    " + "  , rc_avg_rate2    " + "  , prepay_cash_cond "
        + "  , prepay_cash_mm   " + "  , prepay_cash_num  " + "  , limit_avguse_rate_cond "
        + "  , limit_avguse_rate_mm " + "  , limit_avguse_rate " + "  , payment_rate_cond"
        + "  , payment_rate_mm " + "  , payment_rate_num " + "  , no_debt_cond  "
        + "  , no_debt_mm " + "  , payment_integ_cond " + "  , acct_jrnl_bal_cond "
        + "  , acct_jrnl_bal_s " + "  , acct_jrnl_bal_e " + "  , excl_retrial_cond  "
        + "  , excl_retrial_mm" + "  , excl_block_cond " + "  , '2'" // regist_type "
        + "  , ''" // imp_file_name
        + "  , ''" // list_crt_date
        + "  , 0 "// list_crt_rows
        + "  , ''" // imp_jcic_date
        + "  , ''" // imp_jcic_user
        + "  , 0 "// imp_jcic_rows
        + "  , ''" // batch_seqno
        + "  , score_type " + "  , excl_asset_cond " + "  , block_reason_cond "
        + "  , excl_m1_cond " + "  , excl_m1_mm " + "  , excl_m1_cnt " + "  , excl_0d_cond "
        + "  , excl_0d_mm " + "  , excl_0d_cnt " + "  , excl_m2_cond " + "  , excl_m2_mm "
        + "  , excl_m2_cnt " + "  , excl_group_code_cond " + "  , has_card_cond "
        + "  , has_card_value " + "  , assign_list_cond " + "  , assign_list_mm "
        + "  , asset_cond  " + "  , asset_ch_flag " + "  , asset_value_flag "
        + "  , :copy_batch_no " // copy_batch_no
        + "  , 'N'" // sample_flag
        + "  , ''" // parm3_proc_date
        + "  , action_batch_no " + "  , trans_type " + "  , card_issue_cond " + "  , card_issue_mm "
        + "  , :crt_user  " // crt_user
        + "  , to_char(sysdate,'yyyymmdd')  " + "  , 'N' " // apr_flag
        + "  , ''" // apr_date
        + "  , ''" // apr_user
        + "  , :mod_user "// mod_user
        + "  , sysdate "// mod_time
        + "  , :mod_pgm "// mod_pgm
        + "  , 1 "// mod_seqno
        + " from rsk_trial_parm " + " where batch_no=:kk and apr_flag='Y'";

    setString("batch_no", batchNo);
    setString("query_date", this.getSysDate());
    setString("copy_batch_no", copyNo);
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk", copyNo);

    if (sqlExec(strSql) != 1 || sqlRowNum <= 0) {
      errmsg("Insert:" + this.sqlErrtext);
    }

  }

  void insertRskTrialParm2(String batchNo, String copyNo) {
    strSql = "" + " insert into rsk_trial_parmdtl ( " + " apr_flag," + " batch_no," + " data_code,"
        + " data_code2," + " data_type," + " mod_pgm," + " mod_time," + " mod_user," + " type_desc"
        + ") " + " select " + " 'N'" // apr_flag "
        + " ,:batch " // batch_no "
        + " ,data_code  " + " ,data_code2 " + " ,data_type " + " ,mod_pgm " // mod_pgm "
        + " ,sysdate " // mod_time "
        + " ,:mod_user " // mod_user "
        + " ,type_desc  " + " from rsk_trial_parmdtl " + " where batch_no =:copy_no"
        + " and nvl(apr_flag,'N')='Y'";
    setString("batch", batchNo);
    setString("mod_user", wp.loginUser);
    setString("copy_no", copyNo);

    if (sqlExec(strSql) == -1 || sqlRowNum < 0) {
      errmsg("Insert-detl:" + this.sqlErrtext);
    }
    rc = 1;
  }

  void dataCheckDetl() {
    String sql1 =
        "select * from rsk_trial_parmdtl where apr_flag <> 'Y' and batch_no = ? and data_type = ? and data_code = ?";
    sqlSelect(sql1, new Object[] {wp.itemStr2("batch_no"), wp.itemStr2("data_type"),
        wp.itemStr2("ex_data_code")});

    if (sqlRowNum > 0) {
      errmsg("資料已存在");
      return;
    }
  }

  public int dbInsertDetl() {
    msgOK();

    dataCheckDetl();
    if (rc != 1)
      return rc;

    strSql = "insert into rsk_trial_parmdtl (" + " batch_no, " // 1
        + " data_type, " + " data_code, " + " data_code2, " + " apr_flag, " // 5
        + " type_desc, " + " mod_user, mod_time, mod_pgm" + " ) values (" + " :batch_no"
        + ", :data_type" + ", :data_code" + ", :data_code2" + ", 'N'" + ", :type_desc"
        + ",:mod_user,sysdate,:mod_pgm" + " )";
    item2ParmStr("batch_no");
    item2ParmStr("data_type");
    setString("data_code", wp.itemStr2("ex_data_code"));
    setString("data_code2", wp.itemStr2("data_code2"));
    setString("type_desc", wp.itemStr2("ex_type_desc"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert rsk_trial_parmdtl error, " + getMsg());
    }

    return rc;

  }

  public int dbDeleteDetl() {
    msgOK();
    strSql = "Delete rsk_trial_parmdtl" + " where nvl(apr_flag,'N')<>'Y'"
        + commSqlStr.col(varsStr("batch_no"), "batch_no") + commSqlStr.col(varsStr("data_type"), "data_type")
        + commSqlStr.col(varsStr("data_code"), "data_code") + " and nvl(apr_flag,'N') =:apr_flag";
    var2ParmNvl("apr_flag", "N");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete rsk_trial_parmdtl err=" + getMsg());
      rc = -1;
    }

    return rc;
  }


  // public int dbInsert_detl() {
  // msgOK();
  // is_sql ="insert into rsk_trial_parmdtl ("
  // +" batch_no, " //1
  // +" data_type, "
  // +" data_code, "
  // +" data_code2, "
  // +" apr_flag, " //5
  // +" type_desc, "
  // +" mod_user, mod_time, mod_pgm"
  // +" ) values ("
  // +" ?,?,?,?,?"
  // +",?,?,sysdate,?"
  // +" )"
  // ;
  // Object[] param =new Object[] {
  // wp.item_ss("batch_no"), //1
  // wp.item_ss("data_type"),
  // vars_ss("data_code"),
  // vars_ss("data_code2"),
  // "N", //5
  // wp.item_ss("type_desc"),
  // wp.loginUser,
  // //mod_time,
  // wp.mod_pgm()
  // };
  //
  // this.sqlExec(is_sql, param);
  // if (sql_nrow<=0) {
  // errmsg("Insert parmdtl error; "+getMsg());
  // }
  //
  // return rc;
  // }
  // public int dbDelete_detl() {
  // msgOK();
  // is_sql ="Delete rsk_trial_parmdtl"
  // +" where batch_no =:batch_no "
  // +" and data_type =:data_type "
  // +" and nvl(apr_flag,'N') =:apr_flag"
  // ;
  // item2Parm_ss("batch_no");
  // item2Parm_ss("data_type");
  // item2Parm_ss("apr_flag");
  // sqlExec(is_sql);
  //// ddd("batch_no"+wp.item_ss("batch_no")+" apr_flag"+wp.item_ss("apr_flag"));
  //// ddd("sql_nrow:"+sql_nrow);
  // if (sql_nrow<0) {
  // errmsg("Delete parmdtl err; "+getMsg());
  // rc =-1;
  // } else rc=1;
  //
  // return rc;
  // }

  public int dataApprove() {

    String lsBatchNo = varsStr("batch_no");
    if (empty(lsBatchNo)) {
      errmsg("覆審批號: 不可空白");
      return -1;
    }

    // -delete-Master-
    strSql = "delete rsk_trial_parm" + " where batch_no =?" + " and apr_flag='Y'";
    sqlExec(strSql, new Object[] {this.varsStr("batch_no")});

    strSql = "update rsk_trial_parm set" + " apr_flag ='Y'" + ", apr_date =" + this.sqlYmd
        + ", apr_user =?" + ", mod_seqno =nvl(mod_seqno,0)+1" + " where batch_no =?"
        + " and apr_flag<>'Y'";
    sqlExec(strSql, new Object[] {varsStr("apr_user"), this.varsStr("batch_no")});
    if (sqlRowNum <= 0) {
      errmsg("update RSK_TRIAL_PARM error");
      return rc;
    }

    // -delete-Detail---
    strSql = "delete rsk_trial_parmdtl" + " where batch_no =? and apr_flag='Y' "
        + " and data_type in ( select distinct data_type" + " from rsk_trial_parmdtl "
        + " where batch_no =? " + " and apr_flag<>'Y' )";
    sqlExec(strSql, new Object[] {lsBatchNo, lsBatchNo});
    // -parm-detl: N>>Y-
    strSql = "update rsk_trial_parmdtl set " + " apr_flag ='Y' " + " where batch_no =?"
        + " and apr_flag <>'Y'";
    sqlExec(strSql, new Object[] {lsBatchNo, lsBatchNo});
    sqlExec(strSql, new Object[] {lsBatchNo});
    if (sqlRowNum < 0) {
      errmsg("update rsk_trial_parmdtl error");
      return rc;
    } else
      rc = 1;
    return rc;
  }

  public int copyProc() {
    msgOK();
    batchNo = wp.itemStr2("batch_no");

    if (trialList(batchNo)) {
      errmsg("已產生名單, 不可異動");
      return rc;
    }

    deleteCopyN();
    if (rc != 1)
      return rc;

    insertCopyN();

    return rc;
  }

  int deleteCopyN() {
    msgOK();
    // --刪除主檔
    strSql =
        "delete rsk_trial_parm where sample_flag <> 'Y' and apr_flag <> 'Y' and batch_no =:kk1 ";
    setString("kk1", batchNo);

    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete rsk_trial_parm error !");
      return rc;
    } else
      rc = 1;

    // --刪除明細
    strSql = "delete rsk_trial_parmdtl where apr_flag <>'Y' and batch_no =:kk1 ";
    setString("kk1", batchNo);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete rsk_trial_parmdtl error");
      return rc;
    } else
      rc = 1;

    return rc;
  }

  int insertCopyN() {
    msgOK();

    // --查詢已覆核主檔
    String sql1 =
        "select * from rsk_trial_parm where sample_flag <> 'Y' and apr_flag = 'Y' and batch_no = ? ";
    sqlSelect(sql1, new Object[] {batchNo});

    if (sqlRowNum <= 0) {
      errmsg("已覆核資料不存在 , 樣板批號:" + batchNo);
      return rc;
    }

    // --複製主檔
    strSql = " insert into rsk_trial_parm ( " + " batch_no ," + " query_date ," + " trial_reason ,"
        + " jcic_no ," + " card_since_s_date ," + " card_since_e_date ," + " payment_rate1_cond ,"
        + " payment_rate1 ," + " all_credit_limit_cond ," + " all_credit_limit_s ,"
        + " all_credit_limit_e ," + " credit_limit_cond ," + " credit_limit_s ,"
        + " credit_limit_e ," + " group_code_cond ," + " class_code_cond ," + " pd_rating_cond ,"
        + " reg_bank_no_cond ," + " adj_limit_cond ," + " adj_limit_s_date ,"
        + " adj_limit_e_date ," + " risk_group_cond ," + " rc_avg_rate_cond ," + " rc_avg_rate_mm ,"
        + " rc_avg_rate1 ," + " rc_avg_rate2 ," + " prepay_cash_cond ," + " prepay_cash_mm ,"
        + " prepay_cash_num ," + " limit_avguse_rate_cond ," + " limit_avguse_rate_mm ,"
        + " limit_avguse_rate ," + " payment_rate_cond ," + " payment_rate_mm ,"
        + " payment_rate_num ," + " no_debt_cond ," + " no_debt_mm ," + " payment_integ_cond ,"
        + " acct_jrnl_bal_cond ," + " acct_jrnl_bal_s ," + " acct_jrnl_bal_e ,"
        + " excl_retrial_cond ," + " excl_retrial_mm ," + " excl_block_cond ," + " regist_type ,"
        + " imp_file_name ," + " list_crt_date ," + " list_crt_rows ," + " imp_jcic_date ,"
        + " imp_jcic_user ," + " imp_jcic_rows ," + " batch_seqno ," + " score_type ,"
        + " excl_asset_cond ," + " block_reason_cond ," + " excl_m1_cond ," + " excl_m1_mm ,"
        + " excl_m1_cnt ," + " excl_0d_cond ," + " excl_0d_mm ," + " excl_0d_cnt ,"
        + " excl_m2_cond ," + " excl_m2_mm ," + " excl_m2_cnt ," + " excl_group_code_cond ,"
        + " has_card_cond ," + " has_card_value ," + " assign_list_cond ," + " assign_list_mm ,"
        + " asset_cond ," + " asset_ch_flag ," + " asset_value_flag ," + " copy_batch_no ,"
        + " sample_flag ," + " parm3_proc_date ," + " action_batch_no ," + " trans_type ,"
        + " card_issue_cond ," + " card_issue_mm ," + " crt_user ," + " crt_date ," + " apr_flag ,"
        + " apr_date ," + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " :batch_no ," + " :query_date ," + " :trial_reason ,"
        + " :jcic_no ," + " :card_since_s_date ," + " :card_since_e_date ,"
        + " :payment_rate1_cond ," + " :payment_rate1 ," + " :all_credit_limit_cond ,"
        + " :all_credit_limit_s ," + " :all_credit_limit_e ," + " :credit_limit_cond ,"
        + " :credit_limit_s ," + " :credit_limit_e ," + " :group_code_cond ,"
        + " :class_code_cond ," + " :pd_rating_cond ," + " :reg_bank_no_cond ,"
        + " :adj_limit_cond ," + " :adj_limit_s_date ," + " :adj_limit_e_date ,"
        + " :risk_group_cond ," + " :rc_avg_rate_cond ," + " :rc_avg_rate_mm ," + " :rc_avg_rate1 ,"
        + " :rc_avg_rate2 ," + " :prepay_cash_cond ," + " :prepay_cash_mm ," + " :prepay_cash_num ,"
        + " :limit_avguse_rate_cond ," + " :limit_avguse_rate_mm ," + " :limit_avguse_rate ,"
        + " :payment_rate_cond ," + " :payment_rate_mm ," + " :payment_rate_num ,"
        + " :no_debt_cond ," + " :no_debt_mm ," + " :payment_integ_cond ,"
        + " :acct_jrnl_bal_cond ," + " :acct_jrnl_bal_s ," + " :acct_jrnl_bal_e ,"
        + " :excl_retrial_cond ," + " :excl_retrial_mm ," + " :excl_block_cond ,"
        + " :regist_type ," + " :imp_file_name ," + " :list_crt_date ," + " :list_crt_rows ,"
        + " :imp_jcic_date ," + " :imp_jcic_user ," + " :imp_jcic_rows ," + " :batch_seqno ,"
        + " :score_type ," + " :excl_asset_cond ," + " :block_reason_cond ," + " :excl_m1_cond ,"
        + " :excl_m1_mm ," + " :excl_m1_cnt ," + " :excl_0d_cond ," + " :excl_0d_mm ,"
        + " :excl_0d_cnt ," + " :excl_m2_cond ," + " :excl_m2_mm ," + " :excl_m2_cnt ,"
        + " :excl_group_code_cond ," + " :has_card_cond ," + " :has_card_value ,"
        + " :assign_list_cond ," + " :assign_list_mm ," + " :asset_cond ," + " :asset_ch_flag ,"
        + " :asset_value_flag ," + " :copy_batch_no ," + " :sample_flag ," + " :parm3_proc_date ,"
        + " :action_batch_no ," + " :trans_type ," + " :card_issue_cond ," + " :card_issue_mm ,"
        + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ," + " 'N' ," + " '' ," + " '' ,"
        + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    setString("batch_no", batchNo);
    col2ParmStr("query_date");
    col2ParmStr("trial_reason");
    col2ParmStr("jcic_no");
    col2ParmStr("card_since_s_date");
    col2ParmStr("card_since_e_date");
    col2ParmNvl("payment_rate1_cond", "N");
    col2ParmStr("payment_rate1");
    col2ParmNvl("all_credit_limit_cond", "N");
    col2ParmNum("all_credit_limit_s");
    col2ParmNum("all_credit_limit_e");
    col2ParmNvl("credit_limit_cond", "N");
    col2ParmNum("credit_limit_s");
    col2ParmNum("credit_limit_e");
    col2ParmNvl("group_code_cond", "N");
    col2ParmNvl("class_code_cond", "N");
    col2ParmNvl("pd_rating_cond", "N");
    col2ParmNvl("reg_bank_no_cond", "N");
    col2ParmNvl("adj_limit_cond", "N");
    col2ParmStr("adj_limit_s_date");
    col2ParmStr("adj_limit_e_date");
    col2ParmNvl("risk_group_cond", "N");
    col2ParmNvl("rc_avg_rate_cond", "N");
    col2ParmNum("rc_avg_rate_mm");
    col2ParmNum("rc_avg_rate1");
    col2ParmNum("rc_avg_rate2");
    col2ParmNvl("prepay_cash_cond", "N");
    col2ParmNum("prepay_cash_mm");
    col2ParmNum("prepay_cash_num");
    col2ParmNvl("limit_avguse_rate_cond", "N");
    col2ParmNum("limit_avguse_rate_mm");
    col2ParmNum("limit_avguse_rate");
    col2ParmNvl("payment_rate_cond", "N");
    col2ParmNum("payment_rate_mm");
    col2ParmNum("payment_rate_num");
    col2ParmNvl("no_debt_cond", "N");
    col2ParmNum("no_debt_mm");
    col2ParmNvl("payment_integ_cond", "N");
    col2ParmNvl("acct_jrnl_bal_cond", "N");
    col2ParmNum("acct_jrnl_bal_s");
    col2ParmNum("acct_jrnl_bal_e");
    col2ParmNvl("excl_retrial_cond", "N");
    col2ParmNum("excl_retrial_mm");
    col2ParmNvl("excl_block_cond", "N");
    col2ParmStr("regist_type");
    col2ParmStr("imp_file_name");
    col2ParmStr("list_crt_date");
    col2ParmNum("list_crt_rows");
    col2ParmStr("imp_jcic_date");
    col2ParmStr("imp_jcic_user");
    col2ParmNum("imp_jcic_rows");
    col2ParmStr("batch_seqno");
    col2ParmStr("score_type");
    col2ParmStr("excl_asset_cond");
    col2ParmNvl("block_reason_cond", "N");
    col2ParmNvl("excl_m1_cond", "N");
    col2ParmNum("excl_m1_mm");
    col2ParmNum("excl_m1_cnt");
    col2ParmNvl("excl_0d_cond", "N");
    col2ParmNum("excl_0d_mm");
    col2ParmNum("excl_0d_cnt");
    col2ParmNvl("excl_m2_cond", "N");
    col2ParmNum("excl_m2_mm");
    col2ParmNum("excl_m2_cnt");
    col2ParmNvl("excl_group_code_cond", "N");
    col2ParmNvl("has_card_cond", "N");
    col2ParmStr("has_card_value");
    col2ParmNvl("assign_list_cond", "N");
    col2ParmNum("assign_list_mm");
    col2ParmNvl("asset_cond", "N");
    col2ParmStr("asset_ch_flag");
    col2ParmStr("asset_value_flag");
    col2ParmStr("copy_batch_no");
    col2ParmStr("sample_flag");
    col2ParmStr("parm3_proc_date");
    col2ParmStr("action_batch_no");
    col2ParmStr("trans_type");
    col2ParmNvl("card_issue_cond", "N");
    col2ParmNum("card_issue_mm");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    // --查詢已覆核明細檔

    String sql2 = "select * from rsk_trial_parmdtl where batch_no = ? and apr_flag = 'Y' ";
    sqlSelect(sql2, new Object[] {batchNo});

    if (sqlRowNum < 0) {
      errmsg("select rsk_trial_parmdtl error ");
      return rc;
    } else if (sqlRowNum == 0) {
      rc = 1;
      return rc;
    }

    int ilSelectCnt = 0;
    ilSelectCnt = sqlRowNum;

    // --複製明細檔
    strSql = " insert into rsk_trial_parmdtl ( " + " batch_no ," + " data_type ," + " data_code ,"
        + " data_code2 ," + " apr_flag ," + " type_desc ," + " mod_user ," + " mod_time ,"
        + " mod_pgm " + " ) values ( " + " :batch_no ," + " :data_type ," + " :data_code ,"
        + " :data_code2 ," + " 'N' ," + " :type_desc ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm " + " ) ";

    for (int ii = 0; ii < ilSelectCnt; ii++) {
      setString("batch_no", batchNo);
      setString("data_type", colStr(ii, "data_type"));
      setString("data_code", colStr(ii, "data_code"));
      setString("data_code2", colStr(ii, "data_code2"));
      setString("type_desc", colStr(ii, "type_desc"));
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
        break;
      }

    }

    return rc;
  }

}
