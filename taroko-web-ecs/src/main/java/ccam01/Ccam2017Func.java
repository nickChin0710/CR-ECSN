package ccam01;
/**
* ???: outgoing
* */
/**非本行發行卡片停用
 * 2019-1227   JH    bug
 * 2019-0610:  JH    p_seqno >>acno_p_xxx
   20181107:   JH    modify: outgoing
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * */
import busi.FuncAction;
import busi.func.OutgoingOppo;

public class Ccam2017Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  String isOutgoArea = "", cardNo = "", isBinType = "";
  String isOutgoAreaOri = "";
  String isVmjReason = "", isVmjReasonOri = "";
  String isNegReason = "";
  String isFiscReason = "";
  String[] isMOutgoArea = new String[6];
  String[] isMOutgoAreaOri = new String[6];
  String isNegDelDate = "";

  OutgoingOppo ooOutgo = null;
  busi.SqlPrepare ttOppo = null;

  @Override
  public void dataCheck() {
    if (empty(wp.itemStr("neg_del_date"))) {
      errmsg("刪除日期不可空白");
      return;
    }

    cardNo = wp.itemStr2("card_no");
    if (empty(cardNo))
      cardNo = wp.itemStr2("kk_card_no");

    if (cardNo.length() < 15) {
      errmsg("卡號至少要 15 位");
      return;
    }

    if (this.ibDelete) {
      if (neIgno(wp.itemStr("oppo_date"), this.getSysDate())) {
        errmsg("非當日停掛卡,無法撤銷!");
        return;
      }
    }

    selectCcaCardBase();
    if (rc != 1)
      return;

    selectCcaOpposition();
    if (rc != 1)
      return;

    if (wp.itemEmpty("bin_type")) {
      errmsg("卡別不可空白");
      return;
    }
    if (ibDelete)
      return;

    // -Insert,Update-
    if (wp.itemEmpty("oppo_reason")) {
      errmsg("停掛原因不可空白");
      return;
    }
    wfGetOppoReason(wp.itemStr2("oppo_reason"));
    if (rc != 1)
      return;

    if (!empty(wp.itemStr("excep_flag"))) {
      if (wp.itemEq("bin_type", "V"))
        exceptionAreaVisa();
      if (wp.itemEq("bin_type", "J"))
        exceptionAreaJcb();
      if (wp.itemEq("bin_type", "M"))
        exceptionAreaMaster();
      if (rc != 1)
        return;
    }
  }

  void wfGetOppoReason(String aOppoReason) {
    isVmjReason = "";
    if (empty(aOppoReason))
      return;

    strSql = "select ncc_opp_type, neg_opp_reason as neg_reason,"
        + " vis_excep_code as visa_reason," + " mst_auth_code as mast_reason,"
        + " jcb_excp_code as jcb_reason" + " from cca_opp_type_reason" + " where opp_status =?";
    setString2(1, aOppoReason);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("讀取NEG原因失敗!");
      return;
    }

    isNegReason = colStr("neg_reason");

    if (wp.itemEq("bin_type", "V"))
      this.isVmjReason = colStr("visa_reason");
    else if (wp.itemEq("bin_type", "M"))
      isVmjReason = colStr("mast_reason");
    else if (wp.itemEq("bin_type", "J"))
      isVmjReason = colStr("jcb_reason");

  }

  void selectCcaCardBase() {
    strSql = "select count(*) as ll_cnt from cca_card_base" + " where card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("cca_card_base.select errot");
      return;
    }
    if (colNum("ll_cnt") > 0) {
      errmsg("卡號已存在, 請由其他作業停用");
      return;
    }

    daoTid = "AA1.";
    strSql = "select bin_type, debit_flag" + " from ptr_bintable" + " where 1=1" + commSqlStr.whereBinno
        + commSqlStr.rownum(1);
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      sqlErr("ptr_bintable.select");
      return;
    }
    if (sqlRowNum == 0) {
      errmsg("不是本行BIN, 不可執行停用");
      return;
    }

    if (this.isAdd()) {
      wp.itemSet("bin_type", colStr("AA1.bin_type"));
      wp.itemSet("debit_flag", colStr("AA1.debit_flag"));
    }
    // wp.item_set("card_acct_idx",col_ss("AA1.card_acct_idx"));

    return;
  }

  void selectCcaOpposition() {
    daoTid = "A.";
    strSql = "select * from cca_opposition" + " where card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);

    if (isDelete()) {
      if (sqlRowNum <= 0) {
        errmsg("卡號未停用, 不可撤掛");
        return;
      }

      if (wp.itemNum("mod_seqno") != colNum("A.mod_seqno")) {
        errmsg(errOtherModify);
        return;
      }
    }

    if (isAdd() && sqlRowNum > 0) {
      if (empty(colStr("A.logic_del_date"))) {
        errmsg("卡片已停用, 不可再執行停用");
        return;
      }
    }

    // --
    isOutgoAreaOri = "";
    for (int ii = 1; ii <= 6; ii++) {
      isOutgoAreaOri += colNvl("A.vis_area_" + ii, " ")
          + strMid(commString.rpad(colStr("vis_prug_date_" + ii), 8), 2, 6);
    }

    isNegReason = colStr("A.mst_reason_code");
    isVmjReason = colStr("A.vis_reason_code");
    isFiscReason = colStr("A.fisc_reason_code");
  }

  void exceptionAreaVisa() {
    if (wp.itemEmpty("excep_flag"))
      return;

    String lsDate = wp.itemStr("vis_purg_date_1");
    isOutgoArea = "";
    for (int ii = 1; ii <= 9; ii++) {
      isOutgoArea += wp.itemNvl("vis_area_" + ii, " ");
    }
    if (empty(isOutgoArea) == false && empty(lsDate)) {
      errmsg("VISA刪除日期: 不可空白");
      return;
    }

    if (!empty(lsDate) && commDate.sysComp(lsDate) > 0) {
      errmsg("VISA刪除日期不可小於今日");
      return;
    }
  }

  void exceptionAreaJcb() {
    if (wp.itemEmpty("excep_flag"))
      return;
    // -JCB-
    isOutgoArea = "";
    for (int ii = 1; ii <= 5; ii++) {
      isOutgoArea += wp.itemNvl("jcb_area_" + ii, "0");
    }
    String lsDate = wp.itemStr("jcb_date1");
    if (!eqIgno(isOutgoArea, "00000") && empty(lsDate)) {
      errmsg("JCB刪除日期不可空白");
      return;
    }
    if (!empty(lsDate) && commDate.sysComp(lsDate) > 0) {
      errmsg("JCB刪除日期不可小於今日");
      return;
    }
    return;
  }

  void exceptionAreaMaster() {
    // -MasterCard-
    if (wp.itemEmpty("excep_flag"))
      return;

    String lsDate = "", lsArea = "";
    isOutgoArea = "";
    for (int ii = 1; ii <= 6; ii++) {
      lsArea = wp.itemStr2("mast_area_" + ii);
      lsDate = wp.itemStr2("mast_date" + ii);

      if (!empty(lsArea)) {
        if (empty(lsDate)) {
          errmsg("MASTER對應地區之刪除日期不可空白");
          return;
        }
        if (commDate.sysComp(lsDate) > 0) {
          errmsg("MASTER刪除日期不可小於今日");
          return;
        }
      }
      isOutgoArea += lsArea + lsDate;
    }
    for (int ii = 0; ii < 6; ii++) {
      isMOutgoArea[ii] = wp.itemNvl("mast_area_" + ii, " ")
          + this.strMid(commString.rpad(wp.itemStr2("mast_date" + ii), 8), 2, 6);
    }

    if (!empty(isOutgoArea)) {
      if (this.pos(",C,F,O,X", isVmjReason) <= 0) {
        errmsg("MCC103 FORMAT: Master原因碼需為C/F/O/X!");
        return;
      }
    }

    return;
  }

  // void wf_set_lbl_NM(String a_type) {
  // if (eq_igno(a_type,"NEG")) {
  // wp.col_set("neg_resp_code",oo_outgo.neg_resp_code);
  // wp.col_set("neg_reason_code",oo_outgo.neg_income_reason);
  // }
  // else { //VMJ
  // wp.col_set("vmj_resp_code",oo_outgo.vmj_resp_code);
  // wp.col_set("vmj_reason_code",oo_outgo.vmj_income_reason);
  // }
  // }

  void wfOutgoingUpdate(String aAction) {
    String lsBinType = wp.itemStr2("bin_type");

    if (eqIgno(lsBinType, "M")) {
      if (notEmpty(isOutgoAreaOri) && empty(isOutgoArea)) {
        wfOutgoingDelete();
      }
    }

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p4Reason = isVmjReason;
    ooOutgo.p7Region = isOutgoArea;
    ooOutgo.p2BinType = lsBinType;

    if (eqIgno(lsBinType, "V")) {
      ooOutgo.p5DelDate = wp.itemStr2("vis_purg_date_1");
      // "開始傳送 ["+is_card_no+"] 至 VISA......"
      ooOutgo.oppoVisaReq(aAction);
      return;
    }
    if (eqIgno(lsBinType, "J")) {
      ooOutgo.p5DelDate = wp.itemStr2("jcb_date1");
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.oppoJcbReq(aAction);
      return;
    }
    if (eqIgno(lsBinType, "M")) {
      ooOutgo.p6VipAmt = "0";
      if (empty(isOutgoArea)) {
        ooOutgo.masterDate = wp.itemStr2("neg_del_date");
        // 開始傳送 ["+is_card_no+"] 至 Master......MCC102
        ooOutgo.oppoMasterReq2(aAction);
      } else {
        // -開始傳送 ["+is_card_no+"] 至 Master......MCC103-
        ooOutgo.oppoMasterReq(aAction, isMOutgoArea);
      }
    }
  }

  void wfOutgoingDelete() {
    String lsBinType = wp.itemStr2("bin_type");

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p4Reason = isVmjReasonOri;
    ooOutgo.p7Region = isOutgoAreaOri;
    ooOutgo.p2BinType = lsBinType;


    if (eqIgno(lsBinType, "M")) {
      ooOutgo.p6VipAmt = "0";
      if (empty(isOutgoAreaOri)) {
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        ooOutgo.oppoMasterReq2("3");
      } else {
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC103"
        ooOutgo.oppoMasterReq("3", isMOutgoAreaOri);
      }
    } else if (eqIgno(lsBinType, "J")) {
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.p5DelDate = isNegDelDate;
      ooOutgo.oppoJcbReq("0");
    } else if (eq(lsBinType, "V")) {
      ooOutgo.p5DelDate = isNegDelDate;
      ooOutgo.oppoVisaReq("3");
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    // -outgoing-
    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    //ooOutgo.isCallAutoAuth = false;
    ooOutgo.iscalltwmp = true;
    String lsBinType = wp.itemStr2("bin_type");
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = lsBinType;

    // -NCCC-
    if (this.notEmpty(isNegReason)) {
      ooOutgo.p4Reason = isNegReason;
      ooOutgo.fiscReason = isFiscReason;
      ooOutgo.p5DelDate = wp.colStr("neg_del_date");
      ooOutgo.oppoNegId("1");
      if (eqIgno(ooOutgo.respCode, "N4")) {
        ooOutgo.oppoNegId("2");
      }
      // wf_set_lbl_NM("NEG");
    }
    // -VMJ-
    wfOutgoingUpdate("1");
    if (eqIgno(lsBinType, "V") && eqIgno(ooOutgo.respCode, "N4")) {
      wfOutgoingUpdate("2");
    }
    if (eqIgno(lsBinType, "J") && eqIgno(ooOutgo.respCode, "04")) {
      wfOutgoingUpdate("2");
    }
    // wf_set_lbl_NM("VMJ");

    insertCcaOpposition();
    if (rc == 1) {
      insertCrdProhibit();
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
    String lsBinType = wp.itemStr2("bin_type");

    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    //ooOutgo.isCallAutoAuth = false;
    ooOutgo.iscalltwmp = true;
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = lsBinType;

    // -outgoing.NCCC-
    if (notEmpty(isNegReason)) {
      ooOutgo.p4Reason = isNegReason;
      ooOutgo.fiscReason = isFiscReason;
      ooOutgo.p5DelDate = wp.colStr("neg_del_date");
      ooOutgo.oppoNegId("2");
      if (eqIgno(ooOutgo.respCode, "N5")) {
        ooOutgo.oppoNegId("1");
      }
      // wf_set_lbl_NM("NEG");
    }

    // -outgoing.VMJ-
    // -delete-
    if (empty(isVmjReason) && notEmpty(colStr("A.vis_reason_code"))) {
      wfOutgoingDelete();
      // wf_set_lbl_NM("VMJ");
    }
    // -insert-
    if (notEmpty(isVmjReason) && colEmpty("A.vis_reason_code")) {
      wfOutgoingUpdate("1");
      if (eqIgno(lsBinType, "V") && eqIgno(ooOutgo.respCode, "N4")) {
        wfOutgoingUpdate("2");
      } else if (eqIgno(lsBinType, "J") && eqIgno(ooOutgo.respCode, "04")) {
        wfOutgoingUpdate("2");
      }
      // wf_set_lbl_NM("VMJ");
    }
    // -Update-
    if (notEmpty(isVmjReason) && !colEmpty("A.vis_reason_code")) {
      String lsAction = "2";
      if (eqIgno(lsBinType, "M"))
        lsAction = "1";
      wfOutgoingUpdate(lsAction);
      if (eqIgno(lsBinType, "V") && eqIgno(ooOutgo.respCode, "N5")) {
        wfOutgoingUpdate("1");
      } else if (eqIgno(lsBinType, "J") && eqIgno(ooOutgo.respCode, "25")) {
        wfOutgoingUpdate("1");
      }
      // wf_set_lbl_NM("VMJ");
    }

    // --
    insertCcaOpposition();

    return rc;
  }

  void insertCrdProhibit() {
    msgOK();
    strSql = " insert into crd_prohibit ( " + " card_no  , " + " prohibit_remark , "
        + " apr_date , " + " apr_user , " + " crt_date , " + " crt_user , "
        + " mod_pgm, mod_time, mod_user, mod_seqno " + " ) values ( " + " :card_no  , "
        + " '非本行卡停用' , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_pgm, sysdate, :mod_user, 1 "
        + " ) ";

    setString("card_no", cardNo);
    setString("apr_user", wp.loginUser);
    setString("crt_user", wp.loginUser);
    setString("mod_pgm", "ccam2017");
    setString("mod_user", wp.loginUser);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_prohibit error ");
    }
  }

  void insertCcaOpposition() {
    this.strSql = "select count(*) as db_cnt" + " from cca_opposition " + " where card_no =?";
    sqlSelect(strSql, new Object[] {cardNo});
    boolean lbInsert = (colNum("db_cnt") <= 0);

    String lsNegRespCode = "";
    String lsVmjRespCode = "";
    if (ooOutgo != null) {
      lsNegRespCode = ooOutgo.negRespCode;
      lsVmjRespCode = ooOutgo.vmjRespCode;
    }

    ttOppo = new busi.SqlPrepare();

    if (lbInsert) {
      ttOppo.sql2Insert("cca_opposition");
      ttOppo.ppstr("card_no", cardNo);
      // tt_oppo.ppp("card_acct_idx",wp.item_num("card_acct_idx"));
      ttOppo.ppstr("debit_flag", wp.itemStr2("debit_flag"));
      ttOppo.ppstr("bin_type", wp.itemStr2("bin_type"));
      // tt_oppo.ppss("group_code ",col_ss("card.group_code"));
      ttOppo.ppstr("from_type", "1");
      ttOppo.ppstr("oppo_type", wp.itemStr2("oppo_type"));
      ttOppo.ppstr("oppo_status", wp.itemStr2("oppo_reason"));
      ttOppo.ppstr("oppo_user", modUser);
      ttOppo.ppymd("oppo_date");
      ttOppo.pptime("oppo_time");
      ttOppo.ppstr("neg_del_date", wp.itemStr2("neg_del_date"));
      ttOppo.ppstr("renew_flag", "N");
      // tt_oppo.ppss("renew_urgen ");
      ttOppo.ppstr("cycle_credit", "");
      ttOppo.ppstr("opp_remark", wp.itemStr2("opp_remark"));
      // tt_oppo.ppss("mail_branch","");
      // tt_oppo.ppss("lost_fee_flag",wp.sss("lost_fee_code"));
      ttOppo.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
      ttOppo.ppstr("except_proc_flag", "0");
      setVmjArea();
      ttOppo.ppstr("neg_resp_code", lsNegRespCode);
      ttOppo.ppstr("visa_resp_code", lsVmjRespCode);
      ttOppo.ppstr2("mst_reason_code", isNegReason);
      ttOppo.ppstr2("vis_reason_code", isVmjReason);
      ttOppo.ppstr2("fisc_reason_code", isFiscReason);
      // tt_oppo.ppss("mcas_neg_resp_code");
      ttOppo.ppnum("curr_tot_tx_amt", 0);
      ttOppo.ppnum("curr_tot_cash_amt", 0);
      // tt_oppo.ppss("bank_acct_no",wp.sss("bank_acct_no"));
      // tt_oppo.ppss("logic_del ");
      // tt_oppo.ppss("logic_del_date ");
      // tt_oppo.ppss("logic_del_time ");
      // tt_oppo.ppss("logic_del_user ");
      ttOppo.ppymd("crt_date");
      ttOppo.pptime("crt_time"); // ,"to_char(sysdate,'hh24miss')");
      ttOppo.ppstr("crt_user", modUser);
      // --tt_oppo.ppss("in_main_flag","N");
      ttOppo.ppdate("mod_time"); // ,"sysdate");
      ttOppo.ppstr("mod_user", modUser);
      ttOppo.ppstr("mod_pgm", modPgm);
      ttOppo.ppnum("mod_seqno", 1);
    } else {
      ttOppo.sql2Update("cca_opposition");
      ttOppo.ppstr("from_type", "1");
      ttOppo.ppstr("oppo_type", wp.itemStr("oppo_type"));
      ttOppo.ppstr("oppo_status  ", wp.itemStr("oppo_reason"));
      ttOppo.ppstr("oppo_user", modUser);
      ttOppo.ppymd("oppo_date");
      ttOppo.pptime("oppo_time");
      ttOppo.ppstr("neg_del_date", wp.itemStr("neg_del_date"));
      // tt_oppo.ppss("renew_flag",wp.item_nvl("renew_flag","N"));
      // tt_oppo.ppss("cycle_credit ",wp.item_ss("cycle_credit"));
      ttOppo.ppstr("opp_remark", wp.itemStr("opp_remark"));
      // tt_oppo.ppss("mail_branch",wp.item_ss("mail_branch"));
      // tt_oppo.ppss("lost_fee_flag",wp.item_ss("lost_fee_code"));
      ttOppo.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
      ttOppo.ppstr2("except_proc_flag", "0");
      setVmjArea();
      ttOppo.ppstr("neg_resp_code", lsNegRespCode);
      ttOppo.ppstr("visa_resp_code", lsVmjRespCode);
      ttOppo.ppstr2("mst_reason_code", isNegReason);
      ttOppo.ppstr2("vis_reason_code", isVmjReason);
      ttOppo.ppstr2("fisc_reason_code", isFiscReason);
      // tt_oppo.ppss("mcas_neg_resp_code");
      ttOppo.ppnum("curr_tot_tx_amt", 0);
      ttOppo.ppnum("curr_tot_cash_amt", 0);
      // tt_oppo.ppss("bank_acct_no",wp.item_ss("bank_acct_no"));
      ttOppo.ppstr("logic_del", "");
      ttOppo.ppstr("logic_del_date", "");
      ttOppo.ppstr("logic_del_time", "");
      ttOppo.ppstr("logic_del_user", "");
      ttOppo.ppymd("chg_date");
      ttOppo.pptime("chg_time");
      ttOppo.ppstr("chg_user", modUser);
      ttOppo.modxxx(modUser, modPgm);
      ttOppo.sql2Where(" where card_no =?", cardNo);
    }

    sqlExec(ttOppo.sqlStmt(), ttOppo.sqlParm());
    if (sqlRowNum == 0) {
      errmsg("insert CCA_OPPOSITION error; " + this.sqlErrtext);
    }
  }

  void setVmjArea() {
    /*
     * if (wp.item_empty("excep_flag")) { for(int ii=1; ii<=9; ii++) {
     * tt_oppo.ppss("vis_area_"+ii,""); tt_oppo.ppss("vis_purg_date_"+ii,""); } return; }
     */
    if (wp.itemEq("bin_type", "V")) {
      for (int ii = 1; ii <= 9; ii++) {
        ttOppo.ppstr("vis_area_" + ii, wp.itemStr2("vis_area_" + ii));
      }
      ttOppo.ppstr("vis_purg_date_1", wp.itemStr2("vis_purg_date_1"));
      return;
    }
    if (wp.itemEq("bin_type", "M")) {
      for (int ii = 1; ii <= 6; ii++) {
        ttOppo.ppstr("vis_area_" + ii, wp.itemStr("mast_area_" + ii));
        ttOppo.ppstr("vis_purg_date_" + ii, wp.itemStr("mast_date" + ii));
      }
      return;
    }
    if (wp.itemEq("bin_type", "J")) {
      for (int ii = 1; ii <= 6; ii++) {
        ttOppo.ppstr("vis_area_" + ii, wp.itemNvl("jcb_area_" + ii, "N"));
      }
      ttOppo.ppstr("vis_purg_date_1", wp.itemStr("jcb_date1"));
      return;
    }
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    ooOutgo.parmClear();
    //ooOutgo.isCallAutoAuth = false;
    ooOutgo.iscalltwmp = true;
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.colStr("bin_type");

    // 開始傳送["+is_card_no+"]至 NCCC (刪除)......
    if (notEmpty(isNegReason)) {
      ooOutgo.oppoNegId("3");
    }

    if (notEmpty(isVmjReason)) {
      wfOutgoingDelete();
    }

    strSql = " update cca_opposition set " + " logic_del='Y'" + ", logic_del_date =" + commSqlStr.sysYYmd
        + ", logic_del_time =" + commSqlStr.sysTime + ", logic_del_user =:logic_del_user"
        + ", neg_resp_code =:neg_resp_code" + ", visa_resp_code =:visa_resp_code"
        // + " mcas_neg_resp_code =:mcas_neg_resp_code ,"
        + ", chg_date =" + commSqlStr.sysYYmd + ", chg_user =:chg_user" + ", opp_remark='' "
        + ", vis_area_1 ='' " + ", vis_area_2 ='' " + ", vis_area_3 ='' " + ", vis_area_4 ='' "
        + ", vis_area_5 ='' " + ", vis_area_6 ='' " + ", vis_area_7 ='' " + ", vis_area_8 ='' "
        + ", vis_area_9 =''" + ", vis_purg_date_1 ='' " + ", vis_purg_date_2 ='' "
        + ", vis_purg_date_3 ='' " + ", vis_purg_date_4 ='' " + ", vis_purg_date_5 ='' "
        + ", vis_purg_date_6 ='' " + ", vis_purg_date_7 ='' " + ", vis_purg_date_8 ='' "
        + ", vis_purg_date_9 ='' " + "," + commSqlStr.setModxxx(modUser, modPgm)
        + " where card_no =:card_no ";
    setString2("logic_del_user", modUser);
    setString2("neg_resp_code", "");
    setString2("visa_resp_code", "");
    setString2("chg_user", modUser);
    // KKK
    setString2("card_no", cardNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("撤掛失敗, kk[%s]", cardNo);
      return rc;
    }
    if (rc == 1) {
      deleteCrdProhibit();
    }

    return rc;
  }

  void deleteCrdProhibit() {
    strSql = " delete crd_prohibit where card_no =? ";
    setString(1, cardNo);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete crd_prohibit error ");
    } else {
      rc = 1;
    }
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
