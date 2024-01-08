package ccam01;
/** 一般(卡片)特殊指示維護(spec_card_acct)
 * 2019-1230   JH    acno_Block: busi.func >>ecsfunc
 * 2019-1223   JH    insert onbat_2ecs
 * 2019-0611:  JH    p_seqno >>acno_pxxx
   2018-1213:  JH    insert.rsk_acnolog
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 110-01-05  V1.00.01  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *      
 * 111-11-10  V1.00.02  Alex       傳送Outgoing給NCCC、MASTERCARD時帶入刪除日期
 * */

import busi.FuncAction;
import busi.func.OutgoingBlock;
import busi.func.AcnoBlockReason;

public class Ccam2030Func extends FuncAction {
  String cardNo = "", kk2 = "";
  private String isNegReason = "";
  private String isVmReason = "";
  private String isSendIbm = "N";
  boolean ibDebit = false;
  private String isFiscRespCode = "";
  private String isOutgoArea = "";
  private String isSpecDept = "";
  
  boolean lbSpecial = true ;  
  String orgControlTabName = "sms_msg_dtl";
  String controlTabName = "sms_msg_dtl_t";

  busi.func.OutgoingBlock ooOutgo = null;

  void wfSetLblNM(String aType) {
    if (eqIgno(aType, "NEG") || empty(aType)) {
      wp.colSet("neg_resp_code", ooOutgo.negRespCode);
      wp.colSet("neg_reason_code", ooOutgo.negIncomeReason);
    }
    if (eqIgno(aType, "VMJ") || empty(aType)) {
      wp.colSet("vmj_resp_code", ooOutgo.vmjRespCode);
      wp.colSet("vmj_reason_code", ooOutgo.vmjIncomeReason);
    }
  }

  public void wfOutgoingQuery() {
    cardNo = wp.colStr("card_no");
    String lsBinType = wp.colStr("bin_type");
    if (empty(cardNo) || empty(lsBinType)) {
      return;
    }

    strSql = "select spec_neg_reason as neg_reason, spec_outgo_reason as vmj_reason"
        + " from cca_special_visa" + " where card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return;

    isNegReason = colStr("neg_reason");
    isVmReason = colStr("vmj_reason");
    if (empty(isNegReason) || empty(isVmReason))
      return;

    ooOutgo = new OutgoingBlock();
    ooOutgo.setConn(wp);

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = lsBinType;
    // -開始傳送["+is_card_no+"]至 NCCC......-
    ooOutgo.p4Reason = isNegReason;
    ooOutgo.blockNegId("5");
    // --
    ooOutgo.p4Reason = isVmReason;
    if (eqIgno(lsBinType, "V")) {
      ooOutgo.p5DelDate = wp.colStr("spec_del_date");
      // -開始傳送 ["+is_card_no+"] 至 VISA......-
      ooOutgo.blockVmjReq("5");
    } else if (eqIgno(lsBinType, "M")) {
      ooOutgo.p6VipAmt = wp.colStr("spec_mst_vip_amt");
      // -開始傳送 ["+is_card_no+"] 至 Master......-
      ooOutgo.blockVmjReq("5");
    } else if (eqIgno(lsBinType, "J")) {
      ooOutgo.p5DelDate = wp.colStr("spec_del_date");
      // 開始傳送 ["+is_card_no+"] 至 JCB......
      ooOutgo.blockVmjReq("5");
    }

    wfSetLblNM("");
    return;
  }

  void selectCcaCardBase() {
    if (ibDebit) {
      strSql = "select A.spec_flag, A.spec_status, A.spec_del_date, A.spec_mst_vip_amt"
          + ", B.acct_type, B.p_seqno as acno_p_seqno, B.id_p_seqno, B.corp_p_seqno"
          + ", 'N' as combo_indicator, B.bank_actno"
          + " from cca_card_base A join dbc_card B on A.card_no=B.card_no" + " where B.card_no =?";
    } else {
      strSql = "select A.spec_flag, A.spec_status, A.spec_del_date, A.spec_mst_vip_amt"
          + ", B.acct_type, B.acno_p_seqno, B.id_p_seqno, B.corp_p_seqno"
          + ", B.combo_indicator, B.bank_actno"
          + " from cca_card_base A join crd_card B on A.card_no=B.card_no" + " where B.card_no =?";
    }

    setString2(1, cardNo);
    daoTid = "card.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("卡號: 不存在, kk[%s]", cardNo);
      return;
    }
  }

  void selectCcaSpecCode() {
    strSql = "select neg_reason, visa_reason, mast_reason, jcb_reason, send_ibm"
        + " from cca_spec_code" + " where spec_code =?";
    if (ibDelete) {
      setString2(1, colStr("card.spec_status"));
    } else
      setString2(1, wp.itemStr2("spec_status"));

    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("無法讀取指示原因碼(SPEC_CODE)");
      return;
    }

    isNegReason = colStr("neg_reason");
    if (wp.itemEq("bin_type", "V"))
      isVmReason = colStr("visa_reason");
    else if (wp.itemEq("bin_type", "M"))
      isVmReason = colStr("mast_reason");
    else if (wp.itemEq("bin_type", "J"))
      isVmReason = colStr("jcb_reason");
    isSendIbm = colNvl("send_ibm", "N");
  }

  @Override
  public void dataCheck() {
    cardNo = wp.itemStr("card_no");
    if (isEmpty(cardNo)) {
      errmsg("卡號: 不可空白");
      return;
    }
    ibDebit = wp.itemEq("debit_flag", "Y");
    isSpecDept = wp.itemStr2("spec_dept_no");
    if (empty(isSpecDept))
      isSpecDept = userDeptNo();


    selectCcaCardBase();
    if (rc != 1)
      return;
    if (ibDelete) {
      if (!wp.itemEq("spec_status", colStr("card.spec_status"))) {
        errmsg("刪除模式:指示原因不可變更");
        return;
      }
    }

    if (wp.itemEmpty("spec_status")) {
      errmsg("指示原因不可空白");
      return;
    }
    selectCcaSpecCode();
    if (rc != 1)
      return;
    if (ibDelete)
      return;

    if (notEmpty(isNegReason) || notEmpty(isVmReason)) {
      if (wp.itemEmpty("spec_del_date")) {
        errmsg("刪除日期: 不可空白");
        return;
      }
    }

    if (wp.itemEq("bin_type", "M") && eqIgno(isVmReason, "V")) {
      if (wp.itemNum("spec_mst_vip_amt") <= 0) {
        errmsg("Master Card (VIP) 金額必須大於零");
        return;
      }
    }

//    if (ibDebit && wp.itemEmpty("bank_actno") && eqIgno(isSendIbm, "Y")) {
//      errmsg("無金融卡號,不可做停掛");
//      return;
//    }

  }


  void wfOutgoingUpdate(String aFile) {
    String lsBinType = wp.itemStr2("bin_type");

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = lsBinType;
    ooOutgo.p4Reason = isVmReason;

    if (eq(lsBinType, "V")) {
      ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
      ooOutgo.p7Region = isOutgoArea;
    } else if (eq(lsBinType, "M")) {
      //-- 2022/11/10 傳送 Outgoing 至 Master 帶入刪除日期
      ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
      ooOutgo.p6VipAmt = wp.itemStr2("spec_mst_vip_amt");
    } else if (eq(lsBinType, "J")) {
      ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
      ooOutgo.p7Region = "00000";
    }
    // 開始傳送 ["+is_card_no+"] 至 VISA......
    ooOutgo.blockVmjReq(aFile);
  }

  void wfOutgoingDelete() {
    String lsBinType = wp.itemStr2("bin_type");

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = lsBinType;
    ooOutgo.p4Reason = isVmReason;

    if (eq(lsBinType, "V")) {
      ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
      ooOutgo.p7Region = commString.space(9);
      // 開始傳送 ["+is_card_no+"] 至 VISA......
      ooOutgo.blockVmjReq("3");
    } else if (wp.itemEq("bin_type", "M")) {
      //-- 2022/11/10 傳送 Outgoing 至Master須帶入刪除日期
      ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
      ooOutgo.p6VipAmt = wp.itemStr2("spec_mst_vip_amt");
      // 開始傳送 ["+is_card_no+"] 至 Master
      ooOutgo.blockVmjReq("3");
    } else if (wp.itemEq("bin_type", "J")) {
      ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
      ooOutgo.p7Region = "00000";
      // 開始傳送 ["+is_card_no+"] 至 JCB
      ooOutgo.blockVmjReq("0");
    }
  }

  @Override
  public int dbInsert() {
    return 0;
  }

  void updateCcaCardBase() {
    strSql = "update cca_card_base set" + " spec_flag =:spec_flag" + ", spec_status =:spec_status"
        + ", spec_date =:spec_date" + ", spec_time =:spec_time" + ", spec_user =:spec_user"
        + ", spec_del_date =:spec_del_date" + ", spec_mst_vip_amt =:mst_vip_amt"
        + ", spec_remark =:spec_remark" + ", spec_dept_no =:spec_dept_no" + ","
        + commSqlStr.setModxxx(modUser, modPgm) + " where card_no =:cardNo";
    if (ibDelete) {
      setString2("spec_flag", "N");
      setString2("spec_status", "");
      setString2("spec_date", "");
      setString2("spec_time", "");
      setString2("spec_user", "");
      setString2("spec_del_date", "");
      setInt2("mst_vip_amt", 0);
      setString2("spec_remark", "");
      setString2("spec_dept_no", "");
    } else {
      this.dateTime();
      setString2("spec_flag", "Y");
      setString2("spec_status", wp.itemStr2("spec_status"));
      setString2("spec_date", this.sysDate);
      setString2("spec_time", this.sysTime);
      setString2("spec_user", modUser);
      setString2("spec_del_date", wp.itemStr2("spec_del_date"));
      setDouble2("mst_vip_amt", wp.itemNum("spec_mst_vip_amt"));
      setString2("spec_remark", wp.itemStr2("spec_remark"));
      setString2("spec_dept_no", isSpecDept);
    }
    setString2("cardNo", cardNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cca_card_base error; kk[%s]", cardNo);
    }
    return;
  }

  void deleteCcaSpecialVisa() {
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Update("cca_special_visa");

    sp.ppstr2("from_type", "1");
    sp.ppymd("logic_del_date");
    sp.pptime("logic_del_time");
    sp.ppstr2("spec_del_user", modUser);
    sp.ppstr2("vm_del_resp_code", ooOutgo.vmjRespCode);
    sp.ppstr2("neg_del_resp_code", ooOutgo.negRespCode);
    sp.ppstr2("fisc_del_resp_code", isFiscRespCode);
    sp.ppstr2("logic_del", "Y");
    sp.ppymd("chg_date");
    sp.pptime("chg_time");
    sp.ppstr2("chg_user", modUser);
    sp.modxxx(modUser, modPgm);
    sp.sql2Where(" where card_no =?", cardNo);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
    	lbSpecial = false ;
    	rc = 1;
//      sqlErr("cca_special_visa.logic_del, kk=" + cardNo);
      return;
    }
  }

  void updateCcaSpecialVisa() {
    int llCnt = 0;
    strSql = "select count(*) as ll_cnt from cca_special_visa" + " where card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      llCnt = colInt("ll_cnt");
    if (llCnt == 0) {
      insertCcaSpecialVisa();
      return;
    }

    String lsNegReason = isNegReason;
    if (ibDebit)
      lsNegReason = isSendIbm;

    // --
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Update("cca_special_visa");

    // sp.ppp("card_no
    // sp.ppp("bin_type
    sp.ppstr2("from_type", "1");
    sp.ppstr2("spec_status", wp.itemStr2("spec_status"));
    sp.ppstr2("spec_del_date", wp.itemStr2("spec_del_date"));
    sp.ppdouble("spec_mst_vip_amt", wp.itemNum("spec_mst_vip_amt"));
    sp.ppstr2("spec_outgo_reason", isVmReason);
    sp.ppstr2("spec_neg_reason", lsNegReason);
    sp.ppstr2("vm_resp_code", ooOutgo.vmjRespCode);
    sp.ppstr2("neg_resp_code", ooOutgo.negRespCode);
    sp.ppstr2("fisc_resp_code", isFiscRespCode);
    sp.ppstr2("spec_remark", wp.itemStr2("spec_remark"));
    sp.ppstr2("spec_dept_no", wp.itemStr2("spec_dept_no"));
    sp.ppstr2("logic_del_date", "");
    sp.ppstr2("logic_del_time", "");
    sp.ppstr2("spec_del_user", "");
    sp.ppstr2("vm_del_resp_code", "");
    sp.ppstr2("neg_del_resp_code", "");
    // sp.ppp("fisc_del_resp_code
    sp.ppstr2("logic_del", "N");
    // sp.ppp("mcas_resp_code
    // sp.ppp("crt_date
    // sp.ppp("crt_time
    // sp.ppp("crt_user
    sp.ppymd("chg_date");
    sp.pptime("chg_time");
    sp.ppstr2("chg_user", modUser);
    sp.modxxx(modUser, modPgm);
    sp.sql2Where(" where card_no =?", cardNo);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      sqlErr("cca_special_visa.update, kk=" + cardNo);
      return;
    }
  }

  void insertCcaSpecHis(String aAud) {
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("cca_spec_his");

    sp.ppymd("log_date");
    sp.pptime("log_time");
    sp.ppstr2("card_no", cardNo);
    sp.ppstr2("bin_type", wp.itemStr2("bin_type"));
    sp.ppstr2("from_type", "1");
    sp.ppstr2("spec_status", wp.itemStr2("spec_status"));
    sp.ppstr2("spec_del_date", wp.itemStr2("spec_del_date"));
    sp.ppdouble("spec_mst_vip_amt", wp.itemNum("spec_mst_vip_amt"));
    sp.ppstr2("spec_neg_reason", isNegReason);
    sp.ppstr2("spec_outgo_reason", isVmReason);
    sp.ppstr2("vm_resp_code", ooOutgo.vmjRespCode);
    sp.ppstr2("neg_resp_code", ooOutgo.negRespCode);
    sp.ppstr2("fisc_resp_code", isFiscRespCode);
    sp.ppstr2("spec_remark", wp.itemStr2("spec_remark"));
    // sp.ppp("logic_del_date
    // sp.ppp("logic_del_time
    // sp.ppp("spec_del_user
    // sp.ppp("vm_del_resp_code
    // sp.ppp("neg_del_resp_code
    // sp.ppp("fisc_del_resp_code
    // sp.ppp("mcap_resp_code
    sp.ppstr2("logic_del", "N");
    sp.ppstr2("aud_code", aAud);
    sp.ppstr2("pgm_id", modPgm);
    sp.ppstr2("log_user", modUser);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      sqlErr("cca_spec_his.Insert");
      return;
    }
  }

  void insertCcaSpecialVisa() {
    String lsNegReason = isNegReason;
    if (ibDebit)
      lsNegReason = isSendIbm;

    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("cca_special_visa");

    sp.ppstr2("card_no", cardNo);
    sp.ppstr2("bin_type", wp.itemStr2("bin_type"));
    sp.ppstr2("from_type", "1");
    sp.ppstr2("spec_status", wp.itemStr2("spec_status"));
    sp.ppstr2("spec_del_date", wp.itemStr2("spec_del_date"));
    sp.ppdouble("spec_mst_vip_amt", wp.itemNum("spec_mst_vip_amt"));
    sp.ppstr2("spec_outgo_reason", isVmReason);
    sp.ppstr2("spec_neg_reason", lsNegReason);
    sp.ppstr2("spec_dept_no", wp.itemStr2("spec_dept_no"));
    sp.ppstr2("vm_resp_code", ooOutgo.vmjRespCode);
    sp.ppstr2("neg_resp_code", ooOutgo.negRespCode);
    sp.ppstr2("fisc_resp_code", isFiscRespCode);
    sp.ppstr2("spec_remark", wp.itemStr2("spec_remark"));
    // sp.ppp("logic_del_date
    // sp.ppp("logic_del_time
    // sp.ppp("spec_del_user
    // sp.ppp("vm_del_resp_code
    // sp.ppp("neg_del_resp_code
    // sp.ppp("fisc_del_resp_code
    sp.ppstr2("logic_del", "N");
    // sp.ppp("mcas_resp_code
    sp.ppymd("crt_date");
    sp.pptime("crt_time");
    sp.ppstr2("crt_user", modUser);
    // sp.ppp("chg_date
    // sp.ppp("chg_time
    // sp.ppp("chg_user
    sp.ppstr2("mod_user", modUser);
    sp.ppdate("mod_time");
    sp.ppstr2("mod_pgm", modPgm);
    sp.ppint2("mod_seqno", 1);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      errmsg("insert cca_special_visa error, kk[%s]", cardNo);
      return;
    }

  }

  void insertRskAcnolog(String aAud) {
    AcnoBlockReason ooAcnolog = new AcnoBlockReason();
    ooAcnolog.setConn(wp);
    if (ooAcnolog.ccaM2030Spec(cardNo, aAud) != 1) {
      errmsg(ooAcnolog.getMsg());
    }
    return;
  }

  @Override
  public int dbUpdate() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    ooOutgo = new OutgoingBlock();
    ooOutgo.setConn(wp);
    ooOutgo.parmClear();
    ooOutgo.isCallAutoAuth = false;
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.itemStr2("bin_type");
    ooOutgo.p3BankAcctno = colStr("card.bank_actno");
    ooOutgo.p4Reason = isNegReason;
    ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
    ooOutgo.blockReason = wp.itemStr2("spec_status");

    if (!ibDebit) {
      if (notEmpty(isNegReason)) {
        ooOutgo.p4Reason = isNegReason;
        // -開始傳送["+is_card_no+"]至 NCCC (新增)......-
        ooOutgo.blockNegId("1");
        if (eqIgno(ooOutgo.respCode, "N4")) {
          ooOutgo.blockNegId("2");
        }
      }
      if (notEmpty(isVmReason)) {
        wfOutgoingUpdate("1");
        if (wp.itemEq("bin_type", "V") && eqIgno(ooOutgo.respCode, "N4")) {
          // NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
          wfOutgoingUpdate("2");
        } else if (wp.itemEq("bin_type", "J") && eqIgno(ooOutgo.respCode, "04")) {
          // NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
          wfOutgoingUpdate("2");
        }
      }
      wfSetLblNM("");
    } else {
      // debit卡--
      if (eqIgno(isSendIbm, "Y") && !empty(ooOutgo.p3BankAcctno)) {
    	  //--TCB 目前沒有說要通知主機端  2022/03/8
//        rc = ooOutgo.blockIbmNegfile("1");
    	  rc = 1;
        if (rc != 1)
          errmsg(ooOutgo.getMsg());
      }
    }
    if (rc != 1)
      return rc;

    updateCcaCardBase();
    if (rc != 1)
      return rc;

    updateCcaSpecialVisa();
    if (rc != 1)
      return rc;

    insertCcaSpecHis("A");
    if (rc != 1)
      return rc;

    insertRskAcnolog("A");
    if (rc != 1)
      return rc;

    insertOnbat2ecs("A");
    if (rc != 1)
      return rc;

    return rc;
  }

  private int insertOnbat2ecs(String audCode) {
    // -combo卡送IBM-
    if (eq(isSendIbm, "Y") == false)
      return 1;
    // -combo-
    String lsCombo = colNvl("card.combo_indicator", "N");
    if (eq(lsCombo, "N"))
      return 1;

    String lsSpecStatus = "";
    if (eq(audCode, "D") == false)
      lsSpecStatus = wp.itemStr2("spec_status");

    Onbat2ecs ooOnbat = new Onbat2ecs();
    ooOnbat.setConn(wp);
    int liRc = ooOnbat.ccam2030Spec(cardNo, lsSpecStatus);
    if (liRc != 1) {
      errmsg(ooOnbat.mesg());
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    ooOutgo = new OutgoingBlock();
    ooOutgo.setConn(wp);
    ooOutgo.parmClear();

    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.itemStr2("bin_type");
    ooOutgo.blockReason = wp.itemStr2("spec_status");

    if (!ibDebit) {
      if (notEmpty(isNegReason)) {
        ooOutgo.p4Reason = isNegReason;
        //--2022/11/10 傳送NCCC OutGoing時放入刪除日期
        ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
        // -開始傳送["+is_card_no+"]至 NCCC (刪除)......
        ooOutgo.blockNegId("3");
      }
      if (notEmpty(isVmReason)) {
        wfOutgoingDelete();
      }
      wfSetLblNM("");
    } else {
      if (eqIgno(isSendIbm, "Y") && !wp.itemEmpty("bank_cardno")) {
        ooOutgo.p3BankAcctno = wp.itemStr2("bank_cardno");
        ooOutgo.p4Reason = isNegReason;
        ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
        //--TCB 沒有說要通知主機端 2022/03/08
//        ooOutgo.blockIbmNegfile("1");        
      }
    }

    updateCcaCardBase();
    if (rc != 1)
      return rc;

    deleteCcaSpecialVisa();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("cca_spec_his");
    sp.ppymd("log_date");
    sp.pptime("log_time");
    sp.ppstr2("card_no", cardNo);
    sp.ppstr2("bin_type", wp.itemStr2("bin_type"));
    sp.ppstr2("from_type", "1");
    sp.ppstr2("spec_status", colStr("card.spec_status"));
    sp.ppstr2("spec_del_date", colStr("card.spec_del_date"));
    sp.ppstr2("spec_mst_vip_amt", colStr("card.spec_mst_vip_amt"));
    sp.ppstr2("vm_del_resp_code", ooOutgo.vmjRespCode);
    sp.ppstr2("neg_del_resp_code", ooOutgo.negRespCode);
    sp.ppstr2("fisc_del_resp_code", isFiscRespCode);
    sp.ppstr2("aud_code", "D");
    sp.ppstr2("pgm_id", modPgm);
    sp.ppstr2("log_user", modUser);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      sqlErr("cca_spec_his.insert");
      return rc;
    }

    insertRskAcnolog("D");
    if (rc != 1)
      return rc;
    insertOnbat2ecs("D");
    if (rc != 1)
      return rc;
    
    if(lbSpecial) {
    	//--lbSpecial = true 表示 cca_special_visa 存在
    	deleteSmsFlag();
    }    

    return rc;
  }

  // void update_crd_card() {
  // String sql1 = "";
  // if (wp.item_eq("debit_flag", "Y")) {
  // // SPEC_STATUS VARCHAR(2) DEFAULT '' NOT NULL , 特指狀態 */ --
  // // SPEC_DEL_DATE VARCHAR(8) DEFAULT '' NOT NULL , /* 特指刪除日期 */ --
  // sql1 = "update dbc_card set "
  // + " spec_status =:spec_status "
  // + ", spec_date =:spec_date "
  // + ", spec_del_date =:del_date "
  // + ", mod_user =:mod_user"
  // + ", mod_time =sysdate"
  // + ", mod_pgm =:mod_pgm"
  // + ", mod_seqno =nvl(mod_seqno,0)+1"
  // + " where card_no =:card_no";
  // }
  // else {
  // // spec_status VARCHAR(2) DEFAULT '' NOT NULL , /* 特指狀態 */ --
  // // spec_date VARCHAR(8) DEFAULT '' NOT NULL , /* 特指日期 */ --
  // // spec_del_date VARCHAR(8) DEFAULT '' NOT NULL , /* 特指刪除日期 */ --
  // sql1 = "update crd_card set "
  // + " spec_status =:spec_status "
  // + " spec_date =:spec_date"
  // + ", spec_del_date =:del_date "
  // + ", mod_user =:mod_user"
  // + ", mod_time =sysdate"
  // + ", mod_pgm =:mod_pgm"
  // + ", mod_seqno =nvl(mod_seqno,0)+1"
  // + " where card_no =:card_no";
  //
  // }
  // }

  // public int updateCardBase() {
  // CBdataCheck();
  // if (rc != 1) {
  // return rc;
  // }
  // if (this.ib_add) {
  // is_sql = "update CCA_CARD_BASE set "
  // + " spec_flag = 'Y',"
  // + " spec_status =:spec_status,"
  // + " spec_date = "
  // + commSqlStr.sys_YYmd
  // + ","
  // + " spec_time = "
  // + commSqlStr.sys_Time
  // + ","
  // + " spec_user_id =:spec_user_id,"
  // + " spec_del_date =:spec_del_date,"
  // + " spec_mst_vip_amt=:spec_mst_vip_amt,"
  // + " spec_remark =:spec_remark, "
  // + " mod_seqno =nvl(mod_seqno,0)+1 "
  // + " where card_no =:kk "
  // + " and nvl(mod_seqno,0) =:mod_seqno ";
  // ;
  //
  // item2Parm_ss("spec_status");
  // item2Parm_ss("spec_user_id");
  // item2Parm_num("spec_mst_vip_amt");
  // item2Parm_ss("spec_remark");
  // item2Parm_ss("spec_del_date");
  // setString("kk", cardNo);
  // item2Parm_num("mod_seqno");
  // }
  // else if (this.ib_delete) {
  // is_sql = "update CCA_CARD_BASE set "
  // + " spec_flag = 'N',"
  // + " spec_status ='',"
  // + " spec_date ='',"
  // + " spec_time ='',"
  // + " spec_user ='',"
  // + " spec_del_date ='',"
  // + " spec_mst_vip_amt='0',"
  // + " spec_remark ='', "
  // + " mod_seqno =nvl(mod_seqno,0)+1 "
  // + " where card_no =:kk "
  // + " and nvl(mod_seqno,0) =:mod_seqno ";
  // ;
  //
  // setString("kk", cardNo);
  // item2Parm_num("mod_seqno");
  // }
  // rc = sqlExec(is_sql);
  // if (sql_nrow <= 0) {
  // errmsg(this.sql_errtext);
  // }
  // return rc;
  //
  // }

  int updateSmsFlag() {

    strSql = " update cca_special_visa set " + " sms_flag = 'Y' " + " where card_no =:card_no ";

    item2ParmStr("card_no");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update cca_special_visa error !");
    }

    return rc;
  }

  int deleteSmsFlag() {
    strSql = " update cca_special_visa set " + " sms_flag = 'N' " + " where card_no =:card_no ";

    item2ParmStr("card_no");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update cca_special_visa error !");
    }

    return rc; 
  }
  
  
  @Override
  public int dataProc() {		
	if(wp.itemEmpty("cellar_phone")) {
		errmsg("查無卡人手機號碼 , 無法發送簡訊");
		return rc;
	}
	
	busi.func.SmsMsgDetl sms = new busi.func.SmsMsgDetl();
	sms.setConn(wp);
	
	rc = sms.ccaM2030Sms();
	if(rc!=1) {
		errmsg("發送簡訊失敗 , "+sms.getMsg());
		return rc;
	}
	
	updateSmsFlag();
	
    return rc;
  }

}
