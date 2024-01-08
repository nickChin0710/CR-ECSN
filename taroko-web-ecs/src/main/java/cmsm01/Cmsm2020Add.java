package cmsm01;
/** 
 * 2021-1125:    JustinWu ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
 * 2020-0818:    JustinWu remove useless code
 * 2020-0212:    JustinWu modify insertCrdCorp, and add insertCrdCorpExt
 * 2019-1231:    Alex  cancel cca_card_acct_index , add act_acct , act_acct_curr
 * 2019-1225:    Alex  fix  card_acct_class
 * 2019-1224:    Alex  add insert cca_card_acct_index
 * 2019-0613:    JH    p_xxx >>acno_p_xxx
 * 109-04-19    shiyuqi       updated for project coding standard
 * 110-10-08     Justin       add zip2
 * */
import busi.FuncAction;


public class Cmsm2020Add extends FuncAction {
  //String dataKK1 = "", dataKK2 = "";

  @Override
  public void dataCheck() {
    if (empty(wp.itemStr("acct_type"))) {
      errmsg("帳戶類別: 不可空白");
      return;
    }
    if (wp.itemEq("no_interest_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("no_interest_s_month"),
          wp.itemStr("no_interest_e_month")) != 1) {
        errmsg("免收 RC 息 之起迄期間輸入錯誤");
        return;
      }
    }
    if (wp.itemEq("no_penalty_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("no_penalty_s_month"), wp.itemStr("no_penalty_e_month")) != 1) {
        errmsg("免收違約金 之起迄期間輸入錯誤");
        return;
      }
    }
    if (!empty(wp.itemStr("revolve_int_sign"))) {
      if (this.chkStrend(wp.itemStr("revolve_rate_s_month"),
          wp.itemStr("revolve_rate_e_month")) != 1) {
        errmsg("RC 利率加減 之起迄期間輸入錯誤");
        return;
      }
    }
    if (!empty(wp.itemStr("autopay_id"))) {
      if (this.chkStrend(wp.itemStr("autopay_acct_s_date"),
          wp.itemStr("autopay_acct_e_date	")) != 1) {
        errmsg("自動扣繳帳號 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("stat_send_paper", "Y")) {
      if (this.chkStrend(wp.itemStr("stat_send_s_month"), wp.itemStr("stat_send_e_month")) != 1) {
        errmsg("對帳單寄送 之起迄期間輸入錯誤");
        return;
      }
    } else {
      errmsg("對帳單寄送需勾選");
      return;
    }

    if (!wp.itemEmpty("special_stat_code")) {
      if (this.chkStrend(wp.itemStr("special_stat_s_month"),
          wp.itemStr("special_stat_e_month")) != 1) {
        errmsg("特殊對帳單 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("stat_unprint_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("stat_unprint_s_month"),
          wp.itemStr("stat_unprint_e_month")) != 1) {
        errmsg("不列印對帳單 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("no_tel_coll_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("no_tel_coll_s_date"), wp.itemStr("no_tel_coll_e_date")) != 1) {
        errmsg("不可電催 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("no_per_coll_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("no_per_coll_s_date"), wp.itemStr("no_per_coll_e_date")) != 1) {
        errmsg("不可語催 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("no_delinquent_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("no_delinquent_s_date"),
          wp.itemStr("no_delinquent_e_date")) != 1) {
        errmsg("不可逾放 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("no_collection_flag", "Y")) {
      if (this.chkStrend(wp.itemStr("no_collection_s_date"),
          wp.itemStr("no_collection_e_date")) != 1) {
        errmsg("不可催收 之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("no_adj_loc_high", "Y")) {
      if (this.chkStrend(wp.itemStr("no_adj_loc_high_s_date"),
          wp.itemStr("no_adj_loc_high_e_date")) != 1) {
        errmsg("不可調高之起迄期間輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("no_adj_loc_low", "Y")) {
      if (this.chkStrend(wp.itemStr("no_adj_loc_low_s_date"),
          wp.itemStr("no_adj_loc_low_e_date")) != 1) {
        errmsg("不可調低之起迄期間輸入錯誤");
        return;
      }
    }

  }

  boolean crdCorpIsExist() {
    String sql1 = "select corp_p_seqno from crd_corp where corp_no =?";
    sqlSelect(sql1, new Object[] {wp.itemStr("corp_no")});
    if (sqlRowNum <= 0) {
      return false;
    }
    wp.itemSet("corp_p_seqno", colStr("corp_p_seqno"));
    return true;
  }

  boolean checkCorpExt() {
    String sql1 = " select count(*) as db_cnt from crd_corp_ext where corp_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("corp_p_seqno")});
    if (colNum("db_cnt") <= 0) {
      return false;
    }
    return true;
  }

  boolean actAcnoIsExist() {
    String sql1 = "select count(*) as db_cnt " + " from act_acno " + " where acct_type =? "
        + " and corp_p_seqno = ?";
    sqlSelect(sql1, new Object[] {wp.itemStr("acct_type"), wp.itemStr("corp_p_seqno")});
    if (colNum("db_cnt") > 0) {
      return true;
    }
    return false;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();

    // -不存在新增crd_corp-
    if (crdCorpIsExist() == false) {
      insertCrdCorp();
      if (rc != 1) {
        return rc;
      }
    }
    if (checkCorpExt() == false) {
      insertCrdCorpExt();
    }

    if (actAcnoIsExist()) {
      errmsg("帳號類別: 已存在!");
      return rc;
    }

    insertActAcno();
    if (rc != 1)
      return rc;

    insertCcaCardAcct();
    if (rc != 1)
      return rc;
    insertActAcct();
    if (rc != 1)
      return rc;
    insertActAcctCurr();
    // insertCcaCardAcctIndex(); 決議取消此Table

    return rc;
  }

  @Override
  public int dbUpdate() {
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  void insertCrdCorp() {
    // -get corp_p_seqno-
    strSql = "select lpad(ecs_acno.nextval,10,'0') as corp_p_seqno" + " from " + commSqlStr.sqlDual;
    this.sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("無法取得統編流水號[corp_p_seqno]");
      return;
    }
    wp.itemSet("corp_p_seqno", colStr("corp_p_seqno"));

    // --
    /*
     * if (!empty(wp.item_ss("e_mail_addr"))) { item2Parm_ss("e_mail_from_mark");
     * setString("e_mail_chg_date", this.get_sysDate()); } else { setString("e_mail_from_mark",
     * "M"); item2Parm_ss("e_mail_chg_date"); }
     */
    if (wp.itemEmpty("e_mail_addr") == false) {
      wp.itemSet("e_mail_chg_date", this.getSysDate());
      wp.itemSet("e_mail_from_mark", "M");
    }

    busi.SqlPrepare spp = new busi.SqlPrepare();
    spp.sql2Insert("crd_corp");
    spp.ppstr("corp_p_seqno   ", wp.itemStr("corp_p_seqno"));
    spp.ppstr("corp_no        ", wp.itemStr("corp_no"));
    spp.ppstr("chi_name       ", wp.itemStr("chi_name"));
    spp.ppstr("abbr_name      ", wp.itemStr("abbr_name"));
    spp.ppstr("eng_name       ", wp.itemStr("eng_name"));
    spp.ppstr("reg_zip        ", wp.itemStr("reg_zip") + wp.itemStr("reg_zip2"));
    spp.ppstr("reg_addr1      ", wp.itemStr("reg_addr1"));
    spp.ppstr("reg_addr2      ", wp.itemStr("reg_addr2"));
    spp.ppstr("reg_addr3      ", wp.itemStr("reg_addr3"));
    spp.ppstr("reg_addr4      ", wp.itemStr("reg_addr4"));
    spp.ppstr("reg_addr5      ", wp.itemStr("reg_addr5"));
    spp.ppstr("corp_tel_zone1 ", wp.itemStr("corp_tel_zone1"));
    spp.ppstr("corp_tel_no1   ", wp.itemStr("corp_tel_no1"));
    spp.ppstr("corp_tel_ext1  ", wp.itemStr("corp_tel_ext1"));
    spp.ppstr("corp_tel_zone2 ", wp.itemStr("corp_tel_zone2"));
    spp.ppstr("corp_tel_no2   ", wp.itemStr("corp_tel_no2"));
    spp.ppstr("corp_tel_ext2  ", wp.itemStr("corp_tel_ext2"));
    spp.ppstr("charge_id      ", wp.itemStr("charge_id"));
    spp.ppstr("charge_name    ", wp.itemStr("charge_name"));
    spp.ppstr("charge_tel_zone", wp.itemStr("charge_tel_zone"));
    spp.ppstr("charge_tel_no  ", wp.itemStr("charge_tel_no"));
    spp.ppstr("charge_tel_ext ", wp.itemStr("charge_tel_ext"));
    spp.ppnum("capital        ", wp.itemNum("capital"));
    spp.ppstr("business_code  ", wp.itemStr("business_code"));
    spp.ppstr("setup_date     ", wp.itemStr("setup_date"));
    spp.ppstr("force_flag     ", wp.itemStr("force_flag"));
    // spp.ppss("card_since ",wp.item_ss(""));
    spp.ppstr("contact_name   ", wp.itemStr("contact_name"));
    spp.ppstr("contact_area_code", wp.itemStr("contact_area_code"));
    spp.ppstr("contact_tel_no  ", wp.itemStr("contact_tel_no"));
    spp.ppstr("contact_tel_ext ", wp.itemStr("contact_tel_ext"));
    spp.ppstr("contact_zip     ", wp.itemStr("contact_zip") + wp.itemStr("contact_zip2"));
    spp.ppstr("contact_addr1   ", wp.itemStr("contact_addr1"));
    spp.ppstr("contact_addr2   ", wp.itemStr("contact_addr2"));
    spp.ppstr("contact_addr3   ", wp.itemStr("contact_addr3"));
    spp.ppstr("contact_addr4   ", wp.itemStr("contact_addr4"));
    spp.ppstr("contact_addr5   ", wp.itemStr("contact_addr5"));
    spp.ppstr("emboss_data     ", wp.itemStr("emboss_data"));
    // spp.ppss("max_rcv_fee_cnt ",wp.item_int(""));
    spp.ppnum("assure_value    ", wp.itemNum("assure_value"));
    spp.ppstr("organ_id        ", wp.itemStr("organ_id"));
    // spp.ppss("send_visa_flag ",wp.item_ss(""));
    spp.ppstr("e_mail_addr     ", wp.itemStr("e_mail_addr"));
    spp.ppstr("e_mail_from_mark", wp.itemStr("e_mail_from_mark"));
    spp.ppstr("e_mail_chg_date ", wp.itemStr("e_mail_chg_date"));
    // spp.ppss("e_mail_addr2 ",wp.item_ss(""));
    // spp.ppss("e_mail_addr3 ",wp.item_ss(""));
    // spp.ppss("asig_inq1_cname ",wp.item_ss(""));
    // spp.ppss("asig_inq1_idno ",wp.item_ss(""));
    // spp.ppss("asig_inq1_telno ",wp.item_ss(""));
    // spp.ppss("asig_inq1_acct ",wp.item_ss(""));
    // spp.ppss("asig_inq1_card ",wp.item_ss(""));
    // spp.ppss("asig_inq1_limit ",wp.item_ss(""));
    // spp.ppss("asig_inq1_chk_data",wp.item_ss(""));
    // spp.ppss("asig_inq2_cname ",wp.item_ss(""));
    // spp.ppss("asig_inq2_idno ",wp.item_ss(""));
    // spp.ppss("asig_inq2_telno ",wp.item_ss(""));
    // spp.ppss("asig_inq2_acct ",wp.item_ss(""));
    // spp.ppss("asig_inq2_card ",wp.item_ss(""));
    // spp.ppss("asig_inq2_limit ",wp.item_ss(""));
    // spp.ppss("asig_inq2_chk_data",wp.item_ss(""));
    // spp.ppss("asig_inq3_cname ",wp.item_ss(""));
    // spp.ppss("asig_inq3_idno ",wp.item_ss(""));
    // spp.ppss("asig_inq3_telno ",wp.item_ss(""));
    // spp.ppss("asig_inq3_acct ",wp.item_ss(""));
    // spp.ppss("asig_inq3_card ",wp.item_ss(""));
    // spp.ppss("asig_inq3_limit ",wp.item_ss(""));
    // spp.ppss("asig_inq3_chk_data",wp.item_ss(""));

    // 2020-02-13 JustinWu
    spp.ppstr("corp_act_type ", wp.itemStr("corp_act_type"));
    spp.ppstr("empoly_type ", wp.itemStr("empoly_type"));
    spp.ppstr("obu_id ", wp.itemStr("obu_id"));
    spp.ppstr("comm_zip ", wp.itemStr("comm_zip") + wp.itemStr("comm_zip2"));
    spp.ppstr("comm_addr1 ", wp.itemStr("comm_addr1"));
    spp.ppstr("comm_addr2 ", wp.itemStr("comm_addr2"));
    spp.ppstr("comm_addr3 ", wp.itemStr("comm_addr3"));
    spp.ppstr("comm_addr4 ", wp.itemStr("comm_addr4"));
    spp.ppstr("comm_addr5 ", wp.itemStr("comm_addr5"));
    // 2020-02-13 JustinWu

    spp.ppymd("crt_date");
    spp.ppstr("crt_user", modUser);
    spp.ppymd("apr_date");
    spp.ppstr("apr_user", modUser);
    spp.modxxx(modUser, modPgm);

    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert crd_corp error, corp_no=" + wp.itemStr("corp_no"));
      return;
    }

    return;
  }

  void insertCrdCorpExt() {
    msgOK();

    strSql = " insert into crd_corp_ext (  corp_p_seqno , mod_user , mod_time ,"
        + " mod_pgm , mod_seqno  ) values (  :corp_p_seqno , :mod_user ,"
        + " sysdate , :mod_pgm , 1  ) ";
    item2ParmStr("corp_p_seqno");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_corp_ext error !");
    }
  }

  void insertActAcno() {
    strSql = "select lpad(ecs_acno.nextval,10,'0') as acno_p_seqno" + " from " + commSqlStr.sqlDual;
    this.sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("無法取得帳戶流水號[acno_p_seqno]");
      return;
    }
    wp.itemSet("acno_p_seqno", colStr("acno_p_seqno"));

    // busi.SqlPrepare spp=new busi.SqlPrepare();
    sql2Insert("act_acno");

    addsqlParm("?", "acno_p_seqno", wp.itemStr("acno_p_seqno")); // -X(10) 帳戶流水號
    addsqlParm(",?", ", p_seqno", wp.itemStr("acno_p_seqno")); // -X(10) 總繳戶流水號碼
    addsqlParm(",?", ", acct_type", wp.itemStr("acct_type")); // -X(2) 帳戶帳號類別
    addsqlParm(",?", ", acct_key", wp.itemStr("corp_no")); // 帳戶查詢碼
    addsqlParm(",?", ", corp_p_seqno", wp.itemStr("corp_p_seqno")); // -X(10) 統一編號流水號碼
    addsqlParm(",?", ", corp_act_flag", "Y");
    addsqlParm(",?", ", acno_flag", "2");
    addsqlParm(",?", ", acct_status", "1"); // -X(1) 帳戶往來狀態: 1:正常 2:逾放 3.催收 4.呆帳 5.結清(write off)
    // spp.ppss("status_change_date",wp.item_ss("")); //-X(8) 帳戶狀態改變之日期
    // spp.ppss("org_delinquent_date",wp.item_ss("")); //-X(8) 帳戶原始逾放日期
    addsqlParm(",?", ", stmt_cycle", wp.itemStr("stmt_cycle")); // -X(2) 關帳週期
    // spp.ppss("id_p_seqno",wp.item_ss("")); //-X(10) /* 身分證流水號碼
    // spp.ppss("stop_status",wp.item_ss("")); //-X(1) /* 強制停用旗標
    // spp.ppss("stop_reason",wp.item_ss("")); //-X(6) /* 強制停用原因碼
    // spp.ppss("no_block_flag",wp.item_ss("")); //-X(1) /* 帳戶不可禁超期標
    // spp.ppss("no_block_s_date",wp.item_ss("")); //-X(8) /* 帳戶不可禁超生效起日
    // spp.ppss("no_block_e_date",wp.item_ss("")); //-X(8) /* 帳戶不可禁超生效迄日
    // spp.ppss("no_unblock_flag",wp.item_ss("")); //-X(1) /* 帳戶不可解超期標
    // spp.ppss("no_unblock_s_date",wp.item_ss("")); //-X(8) /* 帳戶不可解超生效起日
    // spp.ppss("no_unblock_e_date",wp.item_ss("")); //-X(8) /* 帳戶不可解超生效迄日
    // spp.ppss("rc_use_b_adj",wp.item_ss("")); //-X(1) /* 帳戶調整前之允用rc碼
    // spp.ppss("rc_use_bs_date",wp.item_ss("")); //-X(8) /* 帳戶調整前之允用rc碼生效起日
    // spp.ppss("rc_use_be_date",wp.item_ss("")); //-X(8) /* 帳戶調整前之允用rc碼生效迄日
    // spp.ppss("rc_use_indicator",wp.item_ss("")); //-X(1) /* 帳戶允用rc碼
    // spp.ppss("rc_use_s_date",wp.item_ss("")); //-X(8) /* 例外允用rc生效起日
    // spp.ppss("rc_use_e_date",wp.item_ss("")); //-X(8) /* 例外允用rc生效迄日
    // spp.ppss("rc_use_change_date",wp.item_ss("")); //-X(8) /* 允用rc碼改變之日期
    // spp.ppss("rc_use_reason_code",wp.item_ss("")); //-X(2) /* 允用rc原因碼
    addsqlParm(",?", ", no_interest_flag", wp.itemNvl("no_interest_flag", "N")); // -X(1) 帳戶免收循環息旗標
    addsqlParm(",?", ", no_interest_s_month", wp.itemStr("no_interest_s_month")); // -X(6)
                                                                                  // 帳戶免收循環息生效起月
    addsqlParm(",?", ", no_interest_e_month", wp.itemStr("no_interest_e_month")); // -X(6)
                                                                                  // 帳戶免收循環息生效迄月
    addsqlParm(",?", ", no_penalty_flag", wp.itemNvl("no_penalty_flag", "N")); // -X(1) 帳戶免收違約金旗標
    addsqlParm(",?", ", no_penalty_s_month", wp.itemStr("no_penalty_s_month")); // -X(6) 帳戶免收違約金生效起月
    addsqlParm(",?", ", no_penalty_e_month", wp.itemStr("no_penalty_e_month")); // -X(6) 帳戶免收違約金生效迄月
    addsqlParm(",?", ", special_stat_code", wp.itemStr("special_stat_code")); // -X(5) 帳戶特殊對帳單指示碼
    // spp.ppss("special_stat_division",wp.item_ss("")); //-X(2) 帳戶特殊對帳單登錄科別
    // spp.ppss("special_stat_fee",wp.item_num("")); decimal(9,2) 帳戶特殊對帳單月處理手續費
    addsqlParm(",?", ", special_stat_s_month", wp.itemStr("special_stat_s_month")); // -X(6)
                                                                                    // 帳戶特殊對帳單指示碼生效起月
    addsqlParm(",?", ", special_stat_e_month", wp.itemStr("special_stat_e_month")); // -X(6)
                                                                                    // 帳戶特殊對帳單指示碼生效迄月
    addsqlParm(",?", ", stat_unprint_flag", wp.itemStr("stat_unprint_flag")); // -X(1) 帳戶對帳單不列印旗標
    addsqlParm(",?", ", stat_unprint_s_month", wp.itemStr("stat_unprint_s_month")); // -X(6)
                                                                                    // 帳戶對帳單不列印指示碼生效起月
    addsqlParm(",?", ", stat_unprint_e_month", wp.itemStr("stat_unprint_e_month")); // -X(6)
                                                                                    // 帳戶對帳單不列印指示碼生效迄月
    addsqlParm(",?", ", stat_send_paper", wp.itemStr("stat_send_paper")); // -X(1) 對帳單寄送方式旗標-報表
    // spp.ppss("stat_send_internet",wp.item_ss("")); //-X(1) 對帳單寄送方式旗標-網際網路
    // spp.ppss("stat_send_fax",wp.item_ss("")); //-X(1) 對帳單寄送方式旗標-傳真
    addsqlParm(",?", ", stat_send_s_month", wp.itemStr("stat_send_s_month")); // -X(6)
                                                                              // 對帳單列印與寄送方式生效起日
    addsqlParm(",?", ", stat_send_e_month", wp.itemStr("stat_send_e_month")); // -X(6)
                                                                              // 對帳單列印與寄送方式生效迄日
    // aaa(",?",", no_cancel_debt_flag",wp.item_ss("")); //-X(1) 呆帳戶不自動銷帳旗標
    // aaa(",?",", no_cancel_debt_s_date",wp.item_ss("")); //-X(8) 呆帳戶不自動銷帳生效起日
    // aaa(",?",", no_cancel_debt_e_date",wp.item_ss("")); //-X(8) 呆帳戶不自動銷帳生效迄日
    // aaa(",?",", pay_by_stage_flag",wp.item_ss("")); //-X(2) 分期還款戶的旗標兼欠款期數
    addsqlParm(",?", ", no_tel_coll_flag", wp.itemStr("no_tel_coll_flag")); // -X(1) 帳戶不可電催旗標
    addsqlParm(",?", ", no_tel_coll_s_date", wp.itemStr("no_tel_coll_s_date")); // -X(8) 帳戶不可電催生效起日
    addsqlParm(",?", ", no_tel_coll_e_date", wp.itemStr("no_tel_coll_e_date")); // -X(8) 帳戶不可電催生效迄日
    addsqlParm(",?", ", no_per_coll_flag", wp.itemStr("no_per_coll_flag")); // -X(1) 帳戶不可語催旗標
    addsqlParm(",?", ", no_per_coll_s_date", wp.itemStr("no_per_coll_s_date")); // -X(8) 帳戶不可語催生效起日
    addsqlParm(",?", ", no_per_coll_e_date", wp.itemStr("no_per_coll_e_date")); // -X(8) 帳戶不可語催生效迄日
    // aaa(",?",", no_acct_no",wp.item_ss("")); //-X(11) 虛帳號
    addsqlParm(",?", ", no_delinquent_flag", wp.itemStr("no_delinquent_flag")); // -X(1) 帳戶不可轉逾放旗標
    addsqlParm(",?", ", no_delinquent_s_date", wp.itemStr("no_delinquent_s_date")); // -X(8)
                                                                                    // 帳戶不可轉逾放生效起日
    addsqlParm(",?", ", no_delinquent_e_date", wp.itemStr("no_delinquent_e_date")); // -X(8)
                                                                                    // 帳戶不可轉逾放生效迄日
    addsqlParm(",?", ", no_collection_flag", wp.itemStr("no_collection_flag")); // -X(1) 帳戶不可轉催收旗標
    addsqlParm(",?", ", no_collection_s_date", wp.itemStr("no_collection_s_date")); // -X(8)
                                                                                    // 帳戶不可轉催收生效起日
    addsqlParm(",?", ", no_collection_e_date", wp.itemStr("no_collection_e_date")); // -X(8)
                                                                                    // 帳戶不可轉催收生效迄日
    // spp.ppss("legal_delay_code",wp.item_ss("")); //-X(1) 帳戶法定逾期代碼
    // spp.ppss("legal_delay_code_date",wp.item_ss("")); //-X(8) 帳戶法定逾期代碼發生日
    // spp.ppss("lawsuit_mark",wp.item_ss("")); //-X(1) 帳戶訴追註記
    // spp.ppss("lawsuit_mark_date",wp.item_ss("")); //-X(8) 帳戶訴追註記發生日
    // spp.ppss("recourse_mark",wp.item_ss("")); //-X(1) 帳戶追索債權註記
    // spp.ppss("recourse_mark_date",wp.item_ss("")); //-X(8) 帳戶追索債權註記發生日
    // spp.ppss("lawsuit_process_log",wp.item_ss("")); vargraphic(40) /* 帳戶訴追處理情形
    // spp.ppss("credit_act_no",wp.item_ss("")); //-X(6) 帳戶授信帳號
    // spp.ppss("last_acct_status",wp.item_ss("")); //-X(1) 帳戶上月往來狀態
    // spp.ppss("last_acct_sub_status",wp.item_ss("")); //-X(1) 帳戶上月狀態輔助碼
    addsqlParm(",?", ", no_adj_loc_high", wp.itemStr("no_adj_loc_high")); // -X(1) 帳戶不可調高信用額度旗標
    addsqlParm(",?", ", no_adj_loc_high_s_date", wp.itemStr("no_adj_loc_high_s_date")); // -X(8)
                                                                                        // 帳戶不可調高信用額度生效起日
    addsqlParm(",?", ", no_adj_loc_high_e_date", wp.itemStr("no_adj_loc_high_e_date")); // -X(8)
                                                                                        // 帳戶不可調高信用額度生效迄日
    addsqlParm(",?", ", no_adj_loc_low", wp.itemStr("no_adj_loc_low")); // -X(1) 帳戶不可調低信用額度旗標
    addsqlParm(",?", ", no_adj_loc_low_s_date", wp.itemStr("no_adj_loc_low_s_date")); // -X(8)
                                                                                      // 帳戶不可調低信用額度生效起日
    addsqlParm(",?", ", no_adj_loc_low_e_date", wp.itemStr("no_adj_loc_low_e_date")); // -X(8)
                                                                                      // 帳戶不可調低信用額度生效迄日
    // spp.ppss("no_f_stop_flag",wp.item_ss("")); //-X(1) 帳戶不可強制停用旗標
    // spp.ppss("no_f_stop_s_date",wp.item_ss("")); //-X(8) 帳戶不可強制停用生效起日
    // spp.ppss("no_f_stop_e_date",wp.item_ss("")); //-X(8) 帳戶不可強制停用生效迄日
    // spp.ppss("h_adj_loc_high_date",wp.item_ss("")); //-X(8) 人工調高信用額度之日期
    // spp.ppss("h_adj_loc_low_date",wp.item_ss("")); //-X(8) 人工調低信用額度之日期
    // spp.ppss("s_adj_loc_high_date",wp.item_ss("")); //-X(8) 系統調高信用額度之日期
    // spp.ppss("s_adj_loc_low_date",wp.item_ss("")); //-X(8) 系統調低信用額度之日期
    // spp.ppss("adj_loc_high_t",wp.item_ss("")); //-X(1) 最近調高額度之說明
    // spp.ppss("adj_loc_low_t",wp.item_ss("")); //-X(1) 最近調低額度之說明
    addsqlParm(",?", ", line_of_credit_amt", wp.itemNum("line_of_credit_amt")); // -int 帳戶循環信用額度
    // spp.ppss("last_credit_amt",wp.item_num("")); int 帳戶期初信用額度
    // spp.ppss("inst_auth_loc_amt",wp.item_num("")); int 帳戶分期付款授權額度
    // spp.ppss("adj_before_loc_amt",wp.item_num("")); int 帳戶調整前總月限額
    // spp.ppss("revolve_int_sign",wp.item_ss("")); //-X(1) 帳戶循環信用利率加減指示碼
    // spp.ppss("revolve_int_rate",wp.item_num("")); decimal(5,3) 帳戶循環信用利率加減利率
    // spp.ppss("revolve_rate_s_month",wp.item_ss("")); //-X(6) 帳戶循環信用利率生效起月
    // spp.ppss("revolve_rate_e_month",wp.item_ss("")); //-X(6) 帳戶循環信用利率生效迄月
    // spp.ppss("revolve_reason_2",wp.item_ss("")); //-X(1) rc利率加減碼原因2
    // spp.ppss("revolve_int_sign_2",wp.item_ss("")); //-X(1) rc利率加減指示碼2
    // spp.ppss("revolve_int_rate_2",wp.item_num("")); decimal(5,3) rc利率加減利率2
    // spp.ppss("revolve_rate_s_month_2",wp.item_ss("")); //-X(6) rc利率生效起月2
    // spp.ppss("revolve_rate_e_month_2",wp.item_ss("")); //-X(6) rc利率生效迄月2
    // spp.ppss("revolve_proc_code_2",wp.item_ss("")); //-X(2) rc利率處理碼2
    // spp.ppss("revolve_proc_date_2",wp.item_ss("")); //-X(8) rc利率處理日期2
    // aaa(",?",", revolve_reason",wp.item_ss("")); //-X(1) 利率加減碼原因
    // spp.ppss("revolve_proc_code",wp.item_ss("")); //-X(2) 利率加減碼處理代碼
    // spp.ppss("revolve_proc_date",wp.item_ss("")); //-X(8) 利率加減碼處理日期
    // spp.ppss("penalty_sign",wp.item_ss("")); //-X(1) 帳戶違約金加減指示碼
    // spp.ppss("penalty_rate",wp.item_num("")); decimal(4,2)帳戶違約金加減利率
    // spp.ppss("penalty_rate_s_month",wp.item_ss("")); //-X(6) 帳戶違約金生效起月
    // spp.ppss("penalty_rate_e_month",wp.item_ss("")); //-X(6) 帳戶違約金生效迄月
    addsqlParm(",?", ", min_pay_rate", (int) wp.itemNum("min_pay_rate")); // -int 帳戶mp百分比
    addsqlParm(",?", ", min_pay_rate_s_month", wp.itemStr("min_pay_rate_s_month")); // -X(6)
                                                                                    // 帳戶mp百分比生效起月
    addsqlParm(",?", ", min_pay_rate_e_month", wp.itemStr("min_pay_rate_e_month")); // -X(6)
                                                                                    // 帳戶mp百分比生效迄月
    addsqlParm(",?", ", autopay_indicator", wp.itemStr("autopay_indicator")); // -X(1) 帳戶自動扣繳指示碼
    addsqlParm(",?", ", autopay_rate", (int) wp.itemNum("autopay_rate")); // -int 帳戶自動扣繳比率
    addsqlParm(",?", ", autopay_fix_amt", wp.itemNum("autopay_fix_amt")); // -decimal(11,2) /*
                                                                          // 帳戶自動扣繳固定金額
    addsqlParm(",?", ", autopay_acct_bank", wp.itemStr("autopay_acct_bank")); // -X(4) 帳戶自動扣繳行庫
    addsqlParm(",?", ", autopay_acct_no", wp.itemStr("autopay_acct_no")); // -X(16) 帳戶自動扣繳帳號
    addsqlParm(",?", ", autopay_id", wp.itemStr("autopay_id")); // -X(20) 自動扣繳帳號歸屬id
    addsqlParm(",?", ", autopay_id_code", wp.itemStr("autopay_id_code")); // -X(1) 自動扣繳帳號歸屬id識別碼
    addsqlParm(",?", ", autopay_acct_s_date", wp.itemStr("autopay_acct_s_date")); // -X(8)
                                                                                  // 帳戶自動扣繳帳號生效起日
    addsqlParm(",?", ", autopay_acct_e_date", wp.itemStr("autopay_acct_e_date")); // -X(8)
                                                                                  // 帳戶自動扣繳帳號生效迄日
    // spp.ppss("class_code",wp.item_ss("")); //-X(1) 帳戶卡人等級
    // spp.ppss("vip_code",wp.item_ss("")); //-X(2) 帳戶vip等級
    // spp.ppss("payment_rate1",wp.item_ss("")); //-X(2) 帳戶繳款評等1
    // spp.ppss("payment_rate2",wp.item_ss("")); //-X(2) 帳戶繳款評等2
    // spp.ppss("payment_rate3",wp.item_ss("")); //-X(2) 帳戶繳款評等3
    // spp.ppss("payment_rate4",wp.item_ss("")); //-X(2) 帳戶繳款評等4
    // spp.ppss("payment_rate5",wp.item_ss("")); //-X(2) 帳戶繳款評等5
    // spp.ppss("payment_rate6",wp.item_ss("")); //-X(2) 帳戶繳款評等6
    // spp.ppss("payment_rate7",wp.item_ss("")); //-X(2) 帳戶繳款評等7
    // spp.ppss("payment_rate8",wp.item_ss("")); //-X(2) 帳戶繳款評等8
    // spp.ppss("payment_rate9",wp.item_ss("")); //-X(2) 帳戶繳款評等9
    // spp.ppss("payment_rate10",wp.item_ss("")); //-X(2) 帳戶繳款評等10
    // spp.ppss("payment_rate11",wp.item_ss("")); //-X(2) 帳戶繳款評等11
    // spp.ppss("payment_rate12",wp.item_ss("")); //-X(2) 帳戶繳款評等12
    // spp.ppss("payment_rate13",wp.item_ss("")); //-X(2) 帳戶繳款評等13
    // spp.ppss("payment_rate14",wp.item_ss("")); //-X(2) 帳戶繳款評等14
    // spp.ppss("payment_rate15",wp.item_ss("")); //-X(2) 帳戶繳款評等15
    // spp.ppss("payment_rate16",wp.item_ss("")); //-X(2) 帳戶繳款評等16
    // spp.ppss("payment_rate17",wp.item_ss("")); //-X(2) 帳戶繳款評等17
    // spp.ppss("payment_rate18",wp.item_ss("")); //-X(2) 帳戶繳款評等18
    // spp.ppss("payment_rate19",wp.item_ss("")); //-X(2) 帳戶繳款評等19
    // spp.ppss("payment_rate20",wp.item_ss("")); //-X(2) 帳戶繳款評等20
    // spp.ppss("payment_rate21",wp.item_ss("")); //-X(2) 帳戶繳款評等21
    // spp.ppss("payment_rate22",wp.item_ss("")); //-X(2) 帳戶繳款評等22
    // spp.ppss("payment_rate23",wp.item_ss("")); //-X(2) 帳戶繳款評等23
    // spp.ppss("payment_rate24",wp.item_ss("")); //-X(2) 帳戶繳款評等24
    // spp.ppss("payment_rate25",wp.item_ss("")); //-X(2) 帳戶繳款評等25
    // spp.ppss("last_pay_amt",wp.item_num("")); decimal(11,2)帳戶最後一次繳款金額
    // spp.ppss("last_pay_date",wp.item_ss("")); //-X(8) 帳戶最後一次繳款日期
    // spp.ppss("last_stmt_date",wp.item_ss("")); //-X(8) 帳戶最後一次產生對帳單日期
    // spp.ppss("worse_mcode",wp.item_ss("")); //-X(2) 帳戶最差逾欠記錄
    // spp.ppss("auth_billed_bal",wp.item_num("")); decimal(11,2)帳戶已通知未繳款餘額
    // spp.ppss("auth_unbill_bal",wp.item_num("")); decimal(11,2)帳戶未通知付款餘額
    // spp.ppss("auth_not_deposit",wp.item_num("")); decimal(11,2)帳戶已授權未請款餘額
    // spp.ppss("auth_cash_billed_bal",wp.item_num("")); decimal(11,2)帳戶已通知未繳款預借現金餘額
    // spp.ppss("auth_cash_unbill_bal",wp.item_num("")); decimal(11,2)帳戶未通知付款預借現金餘額
    // spp.ppss("auth_cash_not_deposit",wp.item_num("")); decimal(11,2)帳戶已授權未請款預借現金餘額
    addsqlParm(",?", ", bill_apply_flag", wp.itemStr("bill_apply_flag")); // -X(1) 帳單申請註記
    addsqlParm(",?", ", bill_sending_zip", wp.itemStr("bill_sending_zip") + wp.itemStr("bill_sending_zip2")); // -X(5) 帳戶對帳單寄送地址郵遞區號
    addsqlParm(",?", ", bill_sending_addr1", wp.itemStr("bill_sending_addr1")); // -vargraphic(10)
                                                                                // /* 帳戶對帳單寄送地址1
    addsqlParm(",?", ", bill_sending_addr2", wp.itemStr("bill_sending_addr2")); // -vargraphic(10)
                                                                                // /* 帳戶對帳單寄送地址2
    addsqlParm(",?", ", bill_sending_addr3", wp.itemStr("bill_sending_addr3")); // -vargraphic(12)
                                                                                // /* 帳戶對帳單寄送地址3
    addsqlParm(",?", ", bill_sending_addr4", wp.itemStr("bill_sending_addr4")); // -vargraphic(12)
                                                                                // /* 帳戶對帳單寄送地址4
    addsqlParm(",?", ", bill_sending_addr5", wp.itemStr("bill_sending_addr5")); // -vargraphic(56)
                                                                                // /* 帳戶對帳單寄送地址5
    addsqlParm(",?", ", reg_bank_no", wp.itemStr("reg_bank_no")); // -X(3) 受理行
    addsqlParm(",?", ", risk_bank_no", wp.itemStr("risk_bank_no")); // -X(3) 風險行
    // spp.ppss("chg_addr_date",wp.item_ss("")); //-X(8) 更改地址日期
    // spp.ppss("accept_dm",wp.item_ss("")); //-X(1) 接受行銷-實體郵件
    // spp.ppss("corp_assure_flag",wp.item_ss("")); //-X(1) 公司保証/共同申請註記
    // spp.ppss("special_comment",wp.item_ss("")); vargraphic(100) /* 特殊事項註記備註
    addsqlParm(",?", ", card_indicator", "2"); // -X(1) 商務卡類旗標
    // spp.ppss("lost_fee_flag",wp.item_ss("")); //-X(1) 免收掛失費旗標
    // spp.ppss("f_currency_flag",wp.item_ss("")); //-X(1) 外幣卡戶旗標
    // spp.ppss("month_purchase_lmt",wp.item_int("")); int 月平均消費額
    // spp.ppss("bank_rel_flag",wp.item_ss("")); //-X(1) 銀行利害關係人旗標
    // spp.ppss("debt_close_date",wp.item_ss("")); //-X(8) 欠款結清日
    // spp.ppss("atm_pay_flag",wp.item_ss("")); //-X(1) 跨行轉帳退手續費旗標
    // spp.ppss("combo_flag",wp.item_ss("")); //-X(1) combo_flag
    // spp.ppss("combo_indicator",wp.item_ss("")); //-X(1) combo卡指示碼
    // spp.ppss("combo_acct_no",wp.item_ss("")); //-X(16) combo金融卡帳號
    // spp.ppss("combo_cash_limit",wp.item_int("")); int combo卡預借現金額度
    // spp.ppss("prev_int_sign",wp.item_ss("")); //-X(1) 前次循環信用利率加減指示碼
    // spp.ppss("prev_int_rate",wp.item_num("")); decimal(4,2) 前次循環信用利率加減利率
    // spp.ppss("prev_rate_s_month",wp.item_ss("")); //-X(6) 前次循環信用利率生效起月
    // spp.ppss("prev_rate_e_month",wp.item_ss("")); //-X(6) 前次循環信用利率生效迄月
    // spp.ppss("batch_int_sign",wp.item_ss("")); //-X(1) 系統循環信用利率加減指示碼
    // spp.ppss("batch_int_rate",wp.item_num("")); decimal(5,3) 系統循環信用利率加減利率
    // spp.ppss("batch_rate_s_month",wp.item_ss("")); //-X(6) 系統循環信用利率生效起月
    // spp.ppss("batch_rate_e_month",wp.item_ss("")); //-X(6) 系統循環信用利率生效迄月
    // spp.ppss("new_cycle_month",wp.item_ss("")); //-X(6) 變更cycle施行月份
    // spp.ppss("last_interest_date",wp.item_ss("")); //-X(8) 最後計息日期
    // spp.ppss("group_int_sign",wp.item_ss("")); //-X(1) 團代循環信用利率加減指示碼
    // spp.ppss("group_int_rate",wp.item_num("")); decimal(5,3) 團代循環信用利率加減利率
    // spp.ppss("group_rate_s_month",wp.item_ss("")); //-X(6) 團代循環信用利率生效起月
    // spp.ppss("group_rate_e_month",wp.item_ss("")); //-X(6) 團代循環信用利率生效迄月
    // spp.ppss("ao_int_rate",wp.item_num("")); decimal(5,3) 代償循環信用利率
    // spp.ppss("ao_rate_s_month",wp.item_ss("")); //-X(6) 代償循環信用利率生效起月
    // spp.ppss("ao_rate_e_month",wp.item_ss("")); //-X(6) 代償循環信用利率生效迄月
    // spp.ppss("aox_int_rate",wp.item_num("")); decimal(5,3) 代償其他帳單利率
    // spp.ppss("aox_rate_s_month",wp.item_ss("")); //-X(6) 代償其他帳單適用月份起
    // spp.ppss("aox_rate_e_month",wp.item_ss("")); //-X(6) 代償其他帳單適用月份迄
    // spp.ppss("new_bill_flag",wp.item_ss("")); //-X(1) 適用新增消費旗標
    // spp.ppss("ao_posting_date",wp.item_ss("")); //-X(8) 代償入帳日期
    // spp.ppss("no_adj_h_cash",wp.item_ss("")); //-X(1) 預借現金不可調高註記
    // spp.ppss("no_adj_h_s_date_cash",wp.item_ss("")); //-X(8) 預借現金不可調高起日
    // spp.ppss("no_adj_h_e_date_cash",wp.item_ss("")); //-X(8) 預借現金不可調高迄日
    // spp.ppss("line_of_credit_amt_cash",wp.item_int("")); int 預借現金額度
    // spp.ppss("no_sms_flag",wp.item_ss("")); //-X(1) 暫不發簡訊旗標
    // spp.ppss("no_sms_s_date",wp.item_ss("")); //-X(8) 暫不發簡訊開始日期
    // spp.ppss("no_sms_e_date",wp.item_ss("")); //-X(8) 暫不發簡訊結束日期
    // spp.ppss("aox_rate_date",wp.item_ss("")); //-X(8) 代償新增消費利率處理日期
    // spp.ppss("payment_no",wp.item_ss("")); //-X(15) 繳款單編號
    // spp.ppss("auto_installment",wp.item_ss("")); //-X(1) 自動分期付款
    // spp.ppss("sale_flag",wp.item_ss("")); //-X(1) 出售帳戶旗標
    // spp.ppss("sale_date",wp.item_ss("")); //-X(8) 出售帳戶日期
    // spp.ppss("deposit_flag",wp.item_ss("")); //-X(1) 存款戶註記
    // spp.ppss("loan_flag",wp.item_ss("")); //-X(1) 授信戶註記
    // spp.ppss("mp_1_amt",wp.item_num("")); decimal(14,3)特殊固定mp金額
    // spp.ppss("mp_1_s_month",wp.item_ss("")); //-X(6) 特殊固定mp起月
    // spp.ppss("mp_1_e_month",wp.item_ss("")); //-X(6) 特殊固定mp迄月
    // spp.ppss("mp_flag",wp.item_ss("")); //-X(1) 特殊mp百分比類別
    // spp.ppss("spec_flag_month",wp.item_ss("")); //-X(6) 特殊mp最近使用月份
    // spp.ppss("curr_pd_rating",wp.item_ss("")); //-X(2) 違約預測評等
    // spp.ppss("noauto_balance_flag",wp.item_ss("")); //-X(1) 不自動抵銷旗標
    // spp.ppss("noauto_balance_date1",wp.item_ss("")); //-X(8) 不自動抵銷起日
    // spp.ppss("noauto_balance_date2",wp.item_ss("")); //-X(8) 不自動抵銷迄日
    // spp.ppss("liab_status",wp.item_ss("")); //-X(1) 債協狀態
    // spp.ppss("liab_end_date",wp.item_ss("")); //-X(8) 債協結案日期
    // spp.ppss("tel_off_flag",wp.item_ss("")); //-X(1) 隱藏代扣註記
    // spp.ppss("stat_send_s_month2",wp.item_ss("")); //-X(6) 網路對帳單生效起日
    // spp.ppss("stat_send_e_month2",wp.item_ss("")); //-X(6) 網路對帳單生效迄日
    // spp.ppss("vip_remark",wp.item_ss("")); //-X(60) vip備註
    // spp.ppss("new_acct_flag",wp.item_ss("")); //-X(1) 新舊戶註記
    // spp.ppss("int_rate_mcode",wp.item_int("")); int 計算利率時mcode
    // spp.ppss("internet_upd_user",wp.item_ss("")); //-X(10) 帳單email寄送變更人員
    // spp.ppss("internet_upd_date",wp.item_ss("")); //-X(8) 帳單email寄送變更日期
    // spp.ppss("paper_upd_user",wp.item_ss("")); //-X(10) 帳單紙本寄送變更人員
    // spp.ppss("paper_upd_date",wp.item_ss("")); //-X(8) 帳單紙本寄送變更日期
    // spp.ppss("online_update_date",wp.item_ss("")); //-X(8) 帳戶線上更正日期
    addsqlParm(",?", ", apr_flag", "Y"); // -X(1) 覆核註記
    addsqlYmd(", apr_date"); // -X(8) 主管覆核日期
    addsqlParm(",?", ", apr_user", modUser); // -X(10) 主管覆核人員
    addsqlYmd(", crt_date"); // -X(8) 建檔日期
    addsqlTime(", crt_time"); // -X(20) 建檔時間
    addsqlParm(",?", ", crt_user", modUser); // -X(10) 建檔人員
    // spp.ppss("update_date",wp.item_ss("")); //-X(8) 帳戶維護日期
    // spp.ppss("update_user",wp.item_ss("")); //-X(8) 帳戶維護人員
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert act_acno error, KK=" + wp.itemStr("corp_no") + "," + wp.itemStr("acct_type"));
      return;
    }

    return;
  }

  void insertCcaCardAcctIndex() {
    msgOK();

    busi.SqlPrepare spp = new busi.SqlPrepare();
    spp.sql2Insert("cca_card_acct_index");

    spp.ppnum("card_acct_idx", wp.itemNum("card_acct_idx"));
    spp.ppstr2("card_acct_id", wp.itemStr("corp_no"));
    spp.ppstr2("card_acct_id_seq", "");
    spp.ppstr2("card_corp_id", wp.itemStr("corp_no"));
    spp.ppstr2("card_corp_id_seq", "");
    if (wp.itemEq("acct_type", "02")) {
      spp.ppstr2("card_acct_class", "B");
    } else
      spp.ppstr2("card_acct_class", "C");

    spp.ppstr2("acct_type", wp.itemStr("acct_type"));
    spp.ppnum("acct_parent_index", 0);
    spp.ppstr2("acct_no", "");

    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cca_card_acct_index error, KK=" + wp.itemStr("corp_no") + ","
          + wp.itemStr("acct_type"));
      return;
    }

    return;
  }

  void insertCcaCardAcct() {
	  // 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//    strSql = "select ecs_card_acct_idx.nextval as card_acct_idx from " + commSqlStr.sqlDual;
//    this.sqlSelect(strSql);
//    if (sqlRowNum <= 0) {
//      errmsg("無法取得授權帳戶流水號[card_acct_idx]");
//      return;
//    }
//    wp.itemSet("card_acct_idx", colStr("card_acct_idx"));
	String cardAcctIdx = Integer.toString(Integer.parseInt(wp.itemStr("acno_p_seqno")));
	colSet("card_acct_idx", cardAcctIdx);
	wp.itemSet("card_acct_idx", cardAcctIdx);
	  
    busi.SqlPrepare spp = new busi.SqlPrepare();
    spp.sql2Insert("cca_card_acct");

    spp.ppstr("acno_p_seqno", wp.itemStr("acno_p_seqno")); // -x(10) 帳戶流水號(new)
    spp.ppstr2("p_seqno", wp.itemStr2("acno_p_seqno"));
    spp.ppstr("debit_flag", "N"); // -x(1) debit卡註記
    spp.ppstr("acno_flag", "2"); // -x(1) 帳戶主檔類別
    spp.ppstr("acct_type", wp.itemStr("acct_type")); // -x(2) 帳戶帳號類別
    spp.ppstr("id_p_seqno", ""); // -x(10) 身分證流水號碼(new)
    spp.ppstr("corp_p_seqno", wp.itemStr("corp_p_seqno")); // -x(10) 統一編號流水號碼
    spp.ppnum("card_acct_idx", wp.itemNum("card_acct_idx")); // -9(10) 卡戶基本檔index no#
    spp.ppymd("crt_date"); // -x(8) 鍵檔日期
    spp.ppstr("crt_user", modUser); // -x(10) 建檔人員
    spp.ppymd("apr_date"); // -x(8) 主管覆核日期
    spp.ppstr("apr_user", modUser); // -x(10) 主管覆核人員
    spp.modxxx(modUser, modPgm);

    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cca_card_acct error, KK=" + wp.itemStr("corp_no") + ","
          + wp.itemStr("acct_type"));
      return;
    }

    return;

  }

  void insertActAcct() {
    busi.SqlPrepare spp = new busi.SqlPrepare();
    spp.sql2Insert("act_acct");
    spp.ppstr("p_seqno", wp.itemStr("acno_p_seqno"));
    spp.ppstr("corp_p_seqno", wp.itemStr("corp_p_seqno"));
    spp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
    spp.ppstr("acct_type", wp.itemStr("acct_type"));
    spp.ppstr("apr_flag", "Y");
    spp.ppstr("apr_date", getSysDate());
    spp.ppstr("apr_user", wp.itemStr("approval_user"));
    spp.ppstr("crt_date", getSysDate());
    spp.ppstr("crt_user", wp.loginUser);
    spp.ppstr("update_date", getSysDate());
    spp.ppstr("update_user", wp.loginUser);
    spp.modxxx(modUser, modPgm);
    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert act_acct error, KK=" + wp.itemStr("corp_no") + "," + wp.itemStr("acct_type"));
      return;
    }
  }

  void insertActAcctCurr() {
    busi.SqlPrepare spp = new busi.SqlPrepare();
    spp.sql2Insert("act_acct_curr");
    spp.ppstr("p_seqno", wp.itemStr("acno_p_seqno"));
    spp.ppstr("acct_type", wp.itemStr("acct_type"));
    spp.ppstr("curr_code", "901");
    spp.ppstr("autopay_indicator", wp.itemStr("autopay_indicator"));
    spp.ppstr("autopay_acct_bank", wp.itemStr("autopay_acct_bank"));
    spp.ppstr("autopay_acct_no", wp.itemStr("autopay_acct_no"));
    spp.ppstr("autopay_id", wp.itemStr("autopay_id"));
    spp.ppstr("autopay_id_code", wp.itemStr("autopay_id_code"));
    spp.ppstr("autopay_dc_flag", "N");
    spp.ppstr("crt_user", wp.loginUser);
    spp.ppstr("crt_date", getSysDate());
    spp.ppstr("apr_flag", "Y");
    spp.ppstr("apr_date", getSysDate());
    spp.ppstr("apr_user", wp.itemStr("approval_user"));
    spp.modxxx(modUser, modPgm);
    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert act_acct_curr error, KK=" + wp.itemStr("corp_no") + ","
          + wp.itemStr("acct_type"));
      return;
    }
  }

}
