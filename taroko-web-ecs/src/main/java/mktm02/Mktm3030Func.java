/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *   DATE        Version    AUTHOR              DESCRIPTION                   *
 * ---------    --------  ----------   -------------------------------------- *
 * 110/09/17    V1.00.00   Yang Bo                initial                     *
 * 111/12/09    V1.00.01   Zuwei        按下修改有error & 調整獎金產生執行日,不可大於28                     *
 * 112/03/20    V1.00.02   Yang Bo           add approval feature             *
 * 112/03/20    V1.00.03   Yang Bo           fix insert error                 *
 ******************************************************************************/
package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm3030Func extends FuncEdit {
  String projCode = "";

  public Mktm3030Func(TarokoCommon wr) {
    wp = wr;
    this.setConn(wp);
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    return 1;
  }

  @Override
  public int dbInsert() {
    actionInit("A");

    dataCheck();
    dataVerify();
    if (rc != 1) {
      return rc;
    }

    String tableName = "mkt_intr_fund";
    strSql = "insert into " + tableName + " " +
            "( program_code, chi_name, apply_date_s, apply_date_e, reward_bank, " +
            "reward_finance, exclude_bank, exclude_finance, acct_type_flag, card_type_flag, group_code_flag, " +
            "debut_sup_flag_0, debut_sup_flag_1, debut_year_flag, debut_month1, consume_type, item_ename_bl, " +
            "item_ename_ca, item_ename_it, item_ename_id, item_ename_ot, item_ename_ao, consume_flag, curr_month, " +
            "next_month, curr_amt, curr_tot_cond, curr_tot_cnt, feedback_type, feedback_rate, feedback_amt, " +
            "feedback_score, feedback_score_amt, sale_cond, current_cond, unit_cond, consume_cnt, current_cnt, " +
            "current_score, unit_curr_amt, unit_score, memo, cal_date_type, cal_date_days, crt_user, crt_date, " +
            "apr_flag, apr_user, apr_date, mod_user, mod_time, mod_pgm ) " +
            "values " +
            "( :kk_proj_code, :proj_desc, :apply_date_s, :apply_date_e, :reward_bank, " +
            ":reward_finance, :exclude_bank, :exclude_finance, :acct_type_flag, :card_type_flag, :group_code_flag, " +
            ":debut_sup_flag_0, :debut_sup_flag_1, :debut_year_flag, :debut_month1, :consume_type, :item_ename_bl, " +
            ":item_ename_ca, :item_ename_it, :item_ename_id, :item_ename_ot, :item_ename_ao, :consume_flag, :curr_month, " +
            ":next_month, :curr_amt, :curr_tot_cond, :curr_tot_cnt, :feedback_type, :feedback_rate, :feedback_amt, " +
            ":feedback_score, :feedback_score_amt, :sale_cond, :current_cond, :unit_cond, :consume_cnt, :current_cnt, " +
            ":current_score, :unit_curr_amt, :unit_score, :memo, :cal_date_type, :cal_date_days, :crt_user, " +
            ":crt_date, :apr_flag, :apr_user, :apr_date, :mod_user, sysdate, :mod_pgm )";
    item2ParmStr("kk_proj_code");
    item2ParmStr("proj_desc");
    item2ParmStr("apply_date_s");
    item2ParmStr("apply_date_e");
    item2ParmStr("reward_bank");
    item2ParmStr("reward_finance");
    item2ParmStr("exclude_bank");
    item2ParmStr("exclude_finance");
    item2ParmStr("acct_type_flag");
    item2ParmStr("card_type_flag");
    item2ParmStr("group_code_flag");
    item2ParmStr("debut_sup_flag_0");
    item2ParmStr("debut_sup_flag_1");
    item2ParmStr("debut_year_flag");
    item2ParmInt("debut_month1");
    item2ParmStr("consume_type");
    item2ParmStr("item_ename_bl");
    item2ParmStr("item_ename_ca");
    item2ParmStr("item_ename_it");
    item2ParmStr("item_ename_id");
    item2ParmStr("item_ename_ot");
    item2ParmStr("item_ename_ao");
    item2ParmStr("consume_flag");
    item2ParmInt("curr_month");
    item2ParmInt("next_month");
    item2ParmNum("curr_amt");
    item2ParmStr("curr_tot_cond");
    item2ParmInt("curr_tot_cnt");
    item2ParmStr("feedback_type");
    item2ParmNum("feedback_rate");
    item2ParmNum("feedback_amt");
    item2ParmNum("feedback_score");
    item2ParmNum("feedback_score_amt");
    item2ParmStr("sale_cond");
    item2ParmStr("current_cond");
    item2ParmStr("unit_cond");
    item2ParmInt("consume_cnt");
    item2ParmInt("current_cnt");
    item2ParmNum("current_score");
    item2ParmNum("unit_curr_amt");
    item2ParmNum("unit_score");
    item2ParmStr("memo");
    item2ParmStr("cal_date_type");
    item2ParmInt("cal_date_days");
    setString("crt_user", wp.loginUser);
    setString("crt_date", getSysDate());
    item2ParmStr("apr_flag");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("apr_date", getSysDate());
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");

    dataCheck();
    dataVerify();
    if (rc != 1) {
      return rc;
    }

    if (wp.itemEmpty("approval_user") && wp.itemEmpty("approval_passwd") && wp.itemEq("apr_flag", "Y")) {
      errmsg("已覆核不可修改");
      return rc;
    } else {
      String tableName = "mkt_intr_fund";
      strSql = "update " + tableName + " set " +
              "program_code = :proj_code, chi_name = :proj_desc, apply_date_s = :apply_date_s, " +
              "apply_date_e = :apply_date_e, reward_bank = :reward_bank, reward_finance = :reward_finance, " +
              "exclude_bank = :exclude_bank, exclude_finance = :exclude_bank, acct_type_flag = :acct_type_flag, " +
              "card_type_flag = :card_type_flag, group_code_flag = :group_code_flag, debut_sup_flag_0 = :debut_sup_flag_0, " +
              "debut_sup_flag_1 = :debut_sup_flag_1, debut_year_flag = :debut_year_flag, debut_month1 = :debut_month1, " +
              "consume_type = :consume_type, item_ename_bl = :item_ename_bl, item_ename_ca = :item_ename_ca, " +
              "item_ename_it = :item_ename_it, item_ename_id = :item_ename_id, item_ename_ot = :item_ename_ot, " +
              "item_ename_ao = :item_ename_ao, consume_flag = :consume_flag, curr_month = :curr_month, " +
              "next_month = :next_month, curr_amt = :curr_amt, curr_tot_cond = :curr_tot_cond, curr_tot_cnt = :curr_tot_cnt, " +
              "feedback_type = :feedback_type, feedback_rate = :feedback_rate, feedback_amt = :feedback_amt, " +
              "feedback_score = :feedback_score, feedback_score_amt = :feedback_score_amt, sale_cond = :sale_cond, " +
              "current_cond = :current_cond, unit_cond = :unit_cond, consume_cnt = :consume_cnt, current_cnt = :current_cnt, " +
              "current_score = :current_score, unit_curr_amt = :unit_curr_amt, unit_score = :unit_score, memo = :memo, " +
              "cal_date_type = :cal_date_type, cal_date_days = :cal_date_days, crt_user = :crt_user, crt_date = :crt_date, " +
              "apr_flag = :apr_flag, apr_user = :apr_user, apr_date = :apr_date, mod_user = :mod_user, mod_time = sysdate, " +
              "mod_pgm = :mod_pgm, mod_seqno = :mod_seqno " +
              "where " +
              "program_code = :proj_code and hex(rowid) = :rowid";
      item2ParmStr("proj_code");
      item2ParmStr("proj_desc");
      if (wp.itemEmpty("apply_date_s")) {
          setString("apply_date_s", "20100101");
      } else {
          item2ParmStr("apply_date_s");
      }
//      item2ParmStr("apply_date_s");
      if (wp.itemEmpty("apply_date_e")) {
          setString("apply_date_e", "99991231");
      } else {
          item2ParmStr("apply_date_e");
      }
//      item2ParmStr("apply_date_e");
      item2ParmStr("reward_bank");
      item2ParmStr("reward_finance");
      item2ParmStr("exclude_bank");
      item2ParmStr("exclude_finance");
      item2ParmStr("acct_type_flag");
      item2ParmStr("card_type_flag");
      item2ParmStr("group_code_flag");
      item2ParmStr("debut_sup_flag_0");
      item2ParmStr("debut_sup_flag_1");
      item2ParmStr("debut_year_flag");
      item2ParmInt("debut_month1");
      item2ParmStr("consume_type");
      item2ParmStr("item_ename_bl");
      item2ParmStr("item_ename_ca");
      item2ParmStr("item_ename_it");
      item2ParmStr("item_ename_id");
      item2ParmStr("item_ename_ot");
      item2ParmStr("item_ename_ao");
      item2ParmStr("consume_flag");
      item2ParmInt("curr_month");
      item2ParmInt("next_month");
      item2ParmNum("curr_amt");
      item2ParmStr("curr_tot_cond");
      item2ParmInt("curr_tot_cnt");
      item2ParmStr("feedback_type");
      item2ParmNum("feedback_rate");
      item2ParmNum("feedback_amt");
      item2ParmNum("feedback_score");
      item2ParmNum("feedback_score_amt");
      item2ParmStr("sale_cond");
      item2ParmStr("current_cond");
      item2ParmStr("unit_cond");
      item2ParmInt("consume_cnt");
      item2ParmInt("current_cnt");
      item2ParmNum("current_score");
      item2ParmNum("unit_curr_amt");
      item2ParmNum("unit_score");
      item2ParmStr("memo");
      item2ParmStr("cal_date_type");
      item2ParmInt("cal_date_days");
      setString("crt_user", wp.loginUser);
      setString("crt_date", getSysDate());
      item2ParmStr("apr_flag");
      setString("apr_user", wp.itemStr("approval_user"));
      setString("apr_date", getSysDate());
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
      setNumber("mod_seqno", wp.itemNum("mod_seqno") + 1);
      item2ParmStr("proj_code");
      item2ParmStr("rowid");

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
      }
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    if (rc != 1) {
      return rc;
    }

    strSql = "delete mkt_intr_fund "
            + "where 1 = 1 and program_code = :proj_code and hex(rowid) = :rowid and mod_seqno = :mod_seqno";
    item2ParmStr("proj_code");
    item2ParmStr("rowid");
    item2ParmStr("mod_seqno");

    log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("[mkt_intr_fund] delete error , rowid=" + wp.itemStr("rowid"));
    } else {
      delRelatedDtl();
    }
    return rc;
  }

  /**
    * 删除相关数据
    */
  public void delRelatedDtl() {
    Object[] param = new Object[] {wp.itemStr("proj_code")};
    if (sqlRowcount("mkt_intr_dtl", "program_code = ? ", param) <= 0) {
      return;
    }

    strSql = "delete mkt_intr_dtl "
            + "where 1 = 1 and program_code = :proj_code";
    item2ParmStr("proj_code");

    log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("[mkt_intr_dtl] delete error , program_code=" + wp.itemStr("proj_code"));
    }
  }

  @Override
  public void dataCheck() {
    if (ibDelete) {
      return;
    }
    if (this.ibAdd) {
      projCode = wp.itemStr2("kk_proj_code");
    } else {
      projCode = wp.itemStr2("proj_code");
    }

    if (empty(projCode)) {
      errmsg("獎勵專案代碼：不可空白");
      if (this.ibAdd) {
        wp.colSet("kk_proj_code_pink", "pink");
      } else {
        wp.colSet("proj_code_pink", "pink");
      }
      return;
    }


    // --專案說明
    if (wp.itemEmpty("proj_desc")) {
      errmsg("專案說明：不可空白");
      wp.colSet("proj_desc_pink", "pink");
      return;
    }

    // --獎勵類別
    if (wp.itemNe("reward_bank", "Y") && wp.itemNe("reward_finance", "Y")) {
      errmsg("獎勵類別：至少要選一項");
      return;
    }

    // --正卡/附卡
    if (wp.itemNe("debut_sup_flag_0", "Y") && wp.itemNe("debut_sup_flag_1", "Y")) {
      errmsg("正卡/附卡：至少要選一項");
      return;
    }

    // --首年認定
    if (wp.itemEq("debut_year_flag", "1")) {
      if (wp.itemEmpty("debut_month1")) {
        errmsg("首年認定-新申辦卡：核卡日前幾個月不可空白");
        wp.colSet("debut_month1_pink", "pink");
        return;
      } else if (wp.itemNum("debut_month1") <= 0) {
        errmsg("首年認定-新申辦卡：核卡日前幾個月不可小於等於 0 ");
        wp.colSet("debut_month1_pink", "pink");
        return;
      }
    }

    // --消費金額本金類
    String lsConsume;
    lsConsume = wp.itemNvl("item_ename_bl", "N") + wp.itemNvl("item_ename_ca", "N")
            + wp.itemNvl("item_ename_it", "N") + wp.itemNvl("item_ename_ao", "N")
            + wp.itemNvl("item_ename_id", "N") + wp.itemNvl("item_ename_ot", "N");

    if (eqIgno(lsConsume, "NNNNNN")) {
      errmsg("消費金額本金類：不可皆為空白");
      return;
    }

    // --消費門檻
    if (wp.itemEq("consume_flag", "1")) {
      if (wp.itemEmpty("curr_month")) {
        errmsg("消費門檻：幾個月內刷卡消費不可空白");
        wp.colSet("curr_month_pink", "pink");
        return;
      } else if (wp.itemNum("curr_month") <= 0) {
        errmsg("消費門檻：核卡後幾個月內刷卡消費不可小於等於 0 ");
        wp.colSet("curr_month_pink", "pink");
        return;
      }
    }

    if (wp.itemEmpty("curr_amt")) {
      errmsg("消費門檻：累積消費金額不可空白");
      wp.colSet("curr_amt_pink", "pink");
      return;
    } else if (wp.itemNum("curr_amt") <= 0) {
      errmsg("消費門檻：累積消費金額不可小於等於 0 ");
      wp.colSet("curr_amt_pink", "pink");
      return;
    }

    if (wp.itemEq("curr_tot_cond", "Y")) {
      if (wp.itemEmpty("curr_tot_cnt")) {
        errmsg("消費門檻：累積消費筆數不可空白");
        wp.colSet("curr_tot_cnt_pink", "pink");
        return;
      } else if (wp.itemNum("curr_tot_cnt") <= 0) {
        errmsg("消費門檻：累積消費筆數不可小於等於 0 ");
        wp.colSet("curr_tot_cnt_pink", "pink");
        return;
      }
    }

    // --回饋方式
    if (wp.itemEq("feedback_type", "1")) {
      // 回饋比例
      if (wp.itemEmpty("feedback_rate")) {
        errmsg("回饋方式：回饋比例不可空白");
        wp.colSet("feedback_rate_pink", "pink");
        return;
      } else if (wp.itemNum("feedback_rate") <= 0) {
        errmsg("回饋方式：回饋比例不可小於等於 0.0000");
        wp.colSet("feedback_rate_pink", "pink");
        return;
      }
    } else if (wp.itemEq("feedback_type", "2")) {
      // 每卡回饋金額
      if (wp.itemEmpty("feedback_amt")) {
        errmsg("回饋方式：每卡回饋金額不可空白");
        wp.colSet("feedback_amt_pink", "pink");
        return;
      } else if (wp.itemNum("feedback_amt") <= 0) {
        errmsg("回饋方式：每卡回饋金額不可小於等於 0 ");
        wp.colSet("feedback_amt_pink", "pink");
        return;
      }
    } else if (wp.itemEq("feedback_type", "3")) {
      // 回饋分數
      if (wp.itemEmpty("feedback_score")) {
        errmsg("回饋方式：回饋分數不可空白");
        wp.colSet("feedback_score_pink", "pink");
        return;
      } else if (wp.itemNum("feedback_score") <= 0) {
        errmsg("回饋方式：回饋分數不可小於等於 0 ");
        wp.colSet("feedback_score_pink", "pink");
        return;
      }

      // 回饋金額
      if (wp.itemEmpty("feedback_score_amt")) {
        errmsg("回饋方式：回饋金額不可空白");
        wp.colSet("feedback_score_amt_pink", "pink");
        return;
      } else if (wp.itemNum("feedback_score_amt") <= 0) {
        errmsg("回饋方式：回饋金額不可小於等於 0 ");
        wp.colSet("feedback_score_amt_pink", "pink");
        return;
      }
    }

    // --員工推卡獎勵
    if (wp.itemEq("sale_cond", "Y")) {
      if (wp.itemEmpty("consume_cnt")) {
        errmsg("員工推卡獎勵：流通卡數不可空白");
        wp.colSet("consume_cnt_pink", "pink");
        return;
      } else if (wp.itemNum("consume_cnt") <= 0) {
        errmsg("員工推卡獎勵：流通卡數不可小於等於 0 ");
        wp.colSet("consume_cnt_pink", "pink");
        return;
      }

      if (wp.itemEmpty("current_cnt")) {
        errmsg("員工推卡獎勵：有效卡數不可空白");
        wp.colSet("current_cnt_pink", "pink");
        return;
      } else if (wp.itemNum("current_cnt") <= 0) {
        errmsg("員工推卡獎勵：有效卡數不可小於等於 0 ");
        wp.colSet("current_cnt_pink", "pink");
        return;
      }
    }

    // --有效卡
    if (wp.itemEq("current_cond", "Y")) {
      if (wp.itemEmpty("current_score")) {
        errmsg("員工推卡獎勵：有效卡每卡得幾分不可空白");
        wp.colSet("current_score_pink", "pink");
        return;
      } else if (wp.itemNum("current_score") <= 0) {
        errmsg("員工推卡獎勵：有效卡每卡得幾分不可小於等於 0 ");
        wp.colSet("current_score_pink", "pink");
        return;
      }
    }

    // --營業單位
    if (wp.itemEq("unit_cond", "Y")) {
      if (wp.itemEmpty("unit_curr_amt")) {
        errmsg("營業單位：累積消費金額不可空白");
        wp.colSet("unit_curr_amt_pink", "pink");
        return;
      } else if (wp.itemNum("unit_curr_amt") <= 0) {
        errmsg("營業單位：累積消費金額不可小於等於 0 ");
        wp.colSet("unit_curr_amt_pink", "pink");
        return;
      }

      if (wp.itemEmpty("unit_score")) {
        errmsg("營業單位：有效卡每卡得幾分不可空白");
        wp.colSet("unit_score_pink", "pink");
        return;
      } else if (wp.itemNum("unit_score") <= 0) {
        errmsg("營業單位：有效卡每卡得幾分不可小於等於 0 ");
        wp.colSet("unit_score_pink", "pink");
        return;
      }
    }

    // --獎金產生執行日
    if (wp.itemEmpty("cal_date_days")) {
      errmsg("獎金產生執行日不可空白");
      wp.colSet("cal_date_days_pink", "pink");
    } else if (wp.itemNum("cal_date_days") <= 0) {
      errmsg("獎金產生執行日必須大於 0 ");
      wp.colSet("cal_date_days_pink", "pink");
    } else if (wp.itemNum("cal_date_days") > 31) {
      errmsg("獎金產生執行日不可大於 31 ");
      wp.colSet("cal_date_days_pink", "pink");
    }
  }

  /**
    * 數據標準化
    */
  private void dataVerify() {
    // --獎勵類別：行員
    if (wp.itemNe("reward_bank", "Y")) {
      wp.itemSet("reward_bank", "N");
    }

    // --獎勵類別：金控行員
    if (wp.itemNe("reward_finance", "Y")) {
      wp.itemSet("reward_finance", "N");
    }

    // --卡友身分：排除行員
    if (wp.itemNe("exclude_bank", "Y")) {
      wp.itemSet("exclude_bank", "N");
    }

    // --卡友身分：排除金控行員
    if (wp.itemNe("exclude_finance", "Y")) {
      wp.itemSet("exclude_finance", "N");
    }

    // --首年認定：正卡
    if (wp.itemNe("debut_sup_flag_0", "Y")) {
      wp.itemSet("debut_sup_flag_0", "N");
    }

    // --首年認定：附卡
    if (wp.itemNe("debut_sup_flag_1", "Y")) {
      wp.itemSet("debut_sup_flag_1", "N");
    }

    // --消費金額本金類：簽帳款(BL)
    if (wp.itemNe("item_ename_bl", "Y")) {
      wp.itemSet("item_ename_bl", "N");
    }

    // --消費金額本金類：預借現金(CA)
    if (wp.itemNe("item_ename_ca", "Y")) {
      wp.itemSet("item_ename_ca", "N");
    }

    // --消費金額本金類：分期付款(IT)
    if (wp.itemNe("item_ename_it", "Y")) {
      wp.itemSet("item_ename_it", "N");
    }

    // --消費金額本金類：餘額代償(AO)
    if (wp.itemNe("item_ename_ao", "Y")) {
      wp.itemSet("item_ename_ao", "N");
    }

    // --消費金額本金類：代收款(ID)
    if (wp.itemNe("item_ename_id", "Y")) {
      wp.itemSet("item_ename_id", "N");
    }

    // --消費金額本金類：其他應收款(OT)
    if (wp.itemNe("item_ename_ot", "Y")) {
      wp.itemSet("item_ename_ot", "N");
    }

    // --消費門檻：累積消費筆數
    if (wp.itemNe("curr_tot_cond", "Y")) {
      wp.itemSet("curr_tot_cond", "N");
    }

    // --員工推卡獎勵
    if (wp.itemNe("sale_cond", "Y")) {
      wp.itemSet("sale_cond", "N");
    }

    // --有效卡
    if (wp.itemNe("current_cond", "Y")) {
      wp.itemSet("current_cond", "N");
    }

    // --營業單位
    if (wp.itemNe("unit_cond", "Y")) {
      wp.itemSet("unit_cond", "N");
    }

    // --適用起日
    if (wp.itemEq("apply_date_s", "")) {
      wp.itemSet("apply_date_s", "20100101");
    }

    // --適用迄日
    if (wp.itemEq("apply_date_e", "")) {
      wp.itemSet("apply_date_e", "99991231");
    }
  }

  public int insertDetl(String dataType) {
    msgOK();

    strSql = "insert into mkt_intr_dtl ( program_code, data_type, data_code1, mod_time, mod_pgm ) " +
            "values ( ?, ?, ?, sysdate, 'Mktm3030' )";

    Object[] param = new Object[] { wp.itemStr("proj_code"), dataType, varsStr("ex_data_code1") };

    sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg("Insert mkt_intr_dtl.%s error, program_code = %s", dataType, wp.itemStr("proj_code"));
    }

    return rc;
  }

  public int deleteAllDetl(String dataType) {
    msgOK();

    strSql = "delete mkt_intr_dtl where program_code = ? and data_type = ?";

    setString(wp.itemStr("proj_code"));
    setString(dataType);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_right_parm_detl error !");
      return rc;
    } else {
      rc = 1;
    }

    return rc;
  }

  public int dataApprove() {
    msgOK();

    // --覆核
    strSql = " update mkt_intr_fund set " + " apr_flag = 'Y' ,  "
            + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user = :apr_user "
            + " where program_code = :proj_code ";

    setString("apr_user", wp.itemStr2("approval_user"));
    var2ParmStr("proj_code");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("approve cms_right_parm error !");
    }

    return rc;
  }
}
