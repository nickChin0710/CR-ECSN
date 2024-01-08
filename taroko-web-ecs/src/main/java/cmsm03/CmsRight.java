package cmsm03;
/** 卡友權益資格參數
 * 2019-1216   JH    UAT
 * 2019-1029   JH    modify
 * 2019-1009   JH    modify
 *  109-04-20  shiyuqi       updated for project coding standard     *
 * 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
 * */


public class CmsRight extends busi.FuncBase {
  public boolean ibEmploy = false; // 1.行員,2.卡友
  public int iiSuccessCnt = 0;
  private String cardNo = "";
  private String idNo = "";
  private String itemNo = "";
  private String cardHldr = "";
  private String projCode = "";
  private String idPSeqno = "";
  private String isCardType = "";
  private String isGroupCode = "";
  private String isAcctType = "";
  private String isIssueDate = "";
  private String debutType = "";

  double thiConsumeAmt = 0, maxConsumeAmt = 0;
  double lastConsumeAmt = 0;
  double projThisAmt = 0, projLastAmt = 0;
  int freeCnt = 0, projOkCnt = 0;
  private String[] isLastYm = new String[2];
  private String[] isCurrYm = new String[2];
  private String[] isProjYm = new String[2];

  taroko.base.CommDate commDate = new taroko.base.CommDate();
  busi.DataSet dsParm = new busi.DataSet();

  public boolean bankEmployee(String aData) {
    // -crd_employee.status_id in (1,7)-
    if (aData.length() == 16) {
      String sql1 = " select sup_flag , major_card_no from crd_card where card_no = ? ";
      sqlSelect(sql1, new Object[] {aData});
      if (sqlRowNum <= 0) {
        errmsg("此卡非流通卡");
        return false;
      }

      if (eqIgno(colStr("sup_flag"), "1"))
        cardNo = colStr("major_card_no");
      else
        cardNo = aData;

      String sql2 = "select uf_idno_id(?) as id_no from dual where 1=1 ";
      sqlSelect(sql2, new Object[] {aData});
      if (sqlRowNum <= 0) {
        errmsg("此卡號非本行卡友");
        return false;
      }
      idNo = colStr("id_no");
    } else if (aData.length() == 10) {
      idNo = aData;
    } else {
      errmsg("身分證或卡號資料輸入錯誤 !");
      return false;
    }

    String sql3 = "select count(*) as db_cnt from crd_idno where id_no = ? ";
    sqlSelect(sql3, new Object[] {idNo});
    if (sqlRowNum < 0 || colNum("db_cnt") <= 0) {
      errmsg("此ID非本行卡友");
      return false;
    }

    String sql4 =
        "select count(*) as xx_cnt from crd_employee where status_id in ('1','7') and id = ? ";
    sqlSelect(sql4, new Object[] {idNo});
    if (sqlRowNum < 0 || colNum("xx_cnt") <= 0) {
      ibEmploy = false;
      cardHldr = "2";
    } else {
      ibEmploy = true;
      cardHldr = "1";
    }

    return ibEmploy;
  }

  public void checkCardRight(String aCardNo) {
    checkCardRight(aCardNo, "08");
    checkCardRight(aCardNo, "09");
    checkCardRight(aCardNo, "10");
    checkCardRight(aCardNo, "13");
  }

  public void checkCardRight(String aCardNo, String aItemNo) {
    msgOK();

    cardNo = aCardNo;
    itemNo = aItemNo;

    isCurrYm[0] = sysDate.substring(0, 4) + "01";
    isCurrYm[1] = sysDate.substring(0, 6);
    isLastYm[0] = commDate.dateAdd(sysDate, -1, 0, 0).substring(0, 4) + "01";
    isLastYm[1] = isLastYm[0].substring(0, 4) + "12";

    getIdPseqno(cardNo);

    // -已存在, update今年消費-
    if (checkCurrYY()) {
      checkCardRightCurr();
      iiSuccessCnt++;
      return;
    }

    // -不存在, 查核專案----------
    int liOkCnt = getProjCode();
    if (liOkCnt <= 0) {
      errmsg("此卡號不符合任何權益專案");
      return;
    }

    int llNrow = dsParm.listRows();
    for (int ii = 0; ii < llNrow; ii++) {
      dsParm.listToCol(ii);
      // -不符合-
      if (dsParm.colEmpty("ok_debut"))
        continue;

      projCode = dsParm.colStr("proj_code");
      debutType = dsParm.colStr("ok_debut");
      freeCnt = 0;
      if (empty(projCode))
        break;

      thiConsumeAmt = selectCardConsume(isCurrYm[0], isCurrYm[1]);
      lastConsumeAmt = selectCardConsume(isLastYm[0], isLastYm[1]);
      projLastAmt = lastConsumeAmt;
      projThisAmt = thiConsumeAmt;
      if (dsParm.colEq("curr_cond", "Y") && dsParm.colNum("curr_mon") > 0) {
        isProjYm[0] = isLastYm[0].substring(0, 4) + wp.itemStr2("curr_mon");
        isProjYm[1] = isCurrYm[1];
        projThisAmt = selectCardConsume(isProjYm[0], isProjYm[1]);
      }

      cardFreeCnt1();
      cardFreeCnt2();
      cardFreeCnt3();
      cardFreeCnt6();
    }
  }

  boolean checkCurrYY() {
    strSql = "select count(*) as xx_cnt from cms_right_cal" + " where id_p_seqno =?"
        + " and curr_year =? and item_no =?";
    setString2(1, idPSeqno);
    setString(isCurrYm[0].substring(0, 4));
    setString(itemNo);

    sqlSelect(strSql);
    if (sqlRowNum > 0 && colInt("xx_cnt") > 0)
      return true;

    return false;
  }

  void cardFreeCnt1() {
    // -0.不計算消費-
    // --消費金額累積方式: 0.不計算消費 1.By ID計算 2.正附卡合併計算 3.正附卡分開計算--
    String lsConsumeType = dsParm.colStr("consume_type");

    freeCnt = 0;
    // --不計算消費
    if (eqIgno(lsConsumeType, "0")) {
      freeCnt += dsParm.colInt("consume_00_cnt");
    }
    if (freeCnt == 0)
      return;

    insertCmsRightCal("1", 0);
  }

  void cardFreeCnt2() {
    // 當年消費門檻: (當年消費累積期間:前一年第 個月起+本年度); 累積刷卡金額達 元(含)以上，享有 次/日
    if (!dsParm.colEq("curr_cond", "Y"))
      return;
    if (projThisAmt <= 0)
      return;

    freeCnt = dsParm.colInt("curr_cnt");
    if (freeCnt <= 0)
      return;

    double lmCurrAmt = dsParm.colNum("curr_amt");
    if (projThisAmt < lmCurrAmt)
      return;

    double lmPerAmt = lmCurrAmt; // Math.round(lm_curr_amt / _free_cnt);

    int liRc = updateCmsRightCal("2", lmPerAmt);
    if (liRc == 0) {
      insertCmsRightCal("2", lmPerAmt);
    }
  }

  void cardFreeCnt3() {
    // 前一年消費門檻(舊卡友)
    if (dsParm.colEq("consume_type", "0") || !dsParm.colEq("last_cond", "Y"))
      return;
    if (projLastAmt <= 0)
      return;

    // --只有舊卡友有前一年消費可計算次數
    freeCnt = 0;
    for (int ii = 1; ii <= 6; ii++) {
      if (dsParm.colNum("last_amt" + ii) == 0)
        break;

      maxConsumeAmt = dsParm.colNum("last_amt" + ii);
      if (projLastAmt >= dsParm.colNum("last_amt" + ii)) {
        freeCnt = dsParm.colInt("last_cnt" + ii);
        continue;
      }
      break;
    }
    if (freeCnt <= 0)
      return;
    double lmPerAmt = maxConsumeAmt; // Math.round(_max_consume_amt / _free_cnt);
    insertCmsRightCal("3", lmPerAmt);

    // --刷卡金額每增加 元，再多享 次/日--
    freeCnt = 0;
    if (dsParm.colEq("cond_per", "Y") && dsParm.colEq("debut_year_flag", "3")) {
      if (dsParm.colInt("per_cnt") <= 0)
        return;
      if (projLastAmt <= maxConsumeAmt)
        return;

      freeCnt = (int) Math.floor(
          ((projLastAmt - maxConsumeAmt) / dsParm.colNum("per_amt")) * dsParm.colNum("per_cnt"));

      lmPerAmt = dsParm.colNum("per_amt"); // Math.round(ds_parm.col_num("per_amt") /
                                           // ds_parm.col_num("per_cnt"));
      insertCmsRightCal("4", lmPerAmt);
    }
  }

  void cardFreeCnt6() {
    // /**
    // 06.團體代號 正卡 附卡，核卡後 日內刷團費或機票 (MCC Code)
    // 消費金額: 1.單筆金額 2.累積金額 達 (含)元以上，享有 次/日
    // * */
    // --核卡後幾日內消費機票團費
    if (dsParm.colEq("air_cond", "Y") == false)
      return;

    if (checkDetl(projCode, itemNo, "06", isGroupCode, isCardType) == false) {
      return;
    }
    String lsSup0 = dsParm.colStr("air_sup_flag_0");
    String lsSup1 = dsParm.colStr("air_sup_flag_1");
    int liAirDay = dsParm.colInt("air_day");
    String lsMccGroup07 = dsParm.colStr("air_mcc_group07");
    String lsAmtType = dsParm.colStr("air_amt_type");
    double lmAirAmt = dsParm.colNum("air_amt");
    int liAirCnt = dsParm.colInt("air_cnt");
    if (!eq(lsSup0, "Y") && !eq(lsSup1, "Y"))
      return;

    boolean lbCnt = false;
    double lmCntAmt = 0;
    String lsCardNo = "", lsDate1 = "", lsDate2 = "", sql1 = "";
    // -正卡-
    if (eq(lsSup0, "Y")) {
      sql1 = "select major_card_no as card_no, issue_date from crd_card" + " where card_no =?";
      sqlSelect(sql1, new Object[] {cardNo});
      // -單筆金額-
      if (sqlRowNum > 0 && eq(lsAmtType, "1")) {
        lsCardNo = colStr("card_no");
        lsDate1 = colStr("issue_date");
        lsDate2 = commDate.dateAdd(lsDate1, 0, 0, liAirDay);
        lmCntAmt = selectBillCnt(lsCardNo, lsDate1, lsDate2, lmAirAmt);
        if (lmCntAmt > 0)
          lbCnt = true;
      } else if (sqlRowNum > 0 && eq(lsAmtType, "2")) {
        lmCntAmt = selectBillAmt(lsCardNo, lsDate1, lsDate2);
        if (lmCntAmt >= lmAirAmt)
          lbCnt = true;
      }
    }
    if (lbCnt == false && eq(lsSup1, "Y")) {
      sql1 = "select card_no as card_no, issue_date from crd_card"
          + " where (card_no =? or major_card_no =?) and sup_flag ='1'";
      sqlSelect(sql1, new Object[] {cardNo});
      int llNrow = sqlRowNum;
      for (int ii = 0; ii < llNrow; ii++) {
        // -單筆金額-
        if (eq(lsAmtType, "1")) {
          lsCardNo = colStr(ii, "card_no");
          lsDate1 = colStr(ii, "issue_date");
          lsDate2 = commDate.dateAdd(lsDate1, 0, 0, liAirDay);
          lmCntAmt += selectBillCnt(lsCardNo, lsDate1, lsDate2, lmAirAmt);
          if (lmCntAmt > 0) {
            lbCnt = true;
            break;
          }
        } else if (eq(lsAmtType, "2")) {
          // -累計金額-
          lmCntAmt += selectBillAmt(lsCardNo, lsDate1, lsDate2);
          if (lmCntAmt >= lmAirAmt) {
            lbCnt = true;
            break;
          }
        }
      }
    }

    if (!lbCnt)
      return;
    if (liAirCnt <= 0)
      return;

    freeCnt = liAirCnt;
    double lmPerAmt = lmAirAmt; // Math.round(lm_air_amt / li_air_cnt);
    int liRc = updateCmsRightCal("6", lmPerAmt);
    if (liRc == 1) {
      insertCmsRightCal("6", lmPerAmt);
    }
  }

  void checkCardRightCurr() {

    int llProj = getProjCodeCurr();
    if (llProj == 0)
      return;

    int llNrow = dsParm.listRows();
    for (int ii = 0; ii < llNrow; ii++) {
      dsParm.listToCol(ii);
      // -不符合-
      if (dsParm.colEmpty("ok_debut"))
        continue;

      projCode = dsParm.colStr("proj_code");
      debutType = dsParm.colStr("ok_debut");
      freeCnt = 0;
      if (empty(projCode))
        break;

      thiConsumeAmt = selectCardConsume(isCurrYm[0], isCurrYm[1]);
      lastConsumeAmt = selectCardConsume(isLastYm[0], isLastYm[1]);
      projLastAmt = lastConsumeAmt;
      projThisAmt = thiConsumeAmt;
      if (dsParm.colEq("curr_cond", "Y") && dsParm.colNum("curr_mon") > 0) {
        isProjYm[0] = isLastYm[0].substring(0, 4) + wp.itemStr2("curr_mon");
        isProjYm[1] = isCurrYm[1];
        projThisAmt = selectCardConsume(isProjYm[0], isProjYm[1]);
      }

      // -當年消費-
      cardFreeCnt2();
      // -特殊消費-
      cardFreeCnt6();
    }
  }

  double selectCardConsume(String aYm1, String aYm2) {
    double lmAmt = 0;
    boolean lbAll = false;

    String sql1 = "select sum(consume_bl_amt - sub_bl_amt) as bl_amt"
        + ", sum(consume_ca_amt - sub_ca_amt) as ca_amt"
        + ", sum(consume_it_amt - sub_it_amt) as it_amt"
        + ", sum(consume_ao_amt - sub_ao_amt) as ao_amt"
        + ", sum(consume_id_amt - sub_id_amt) as id_amt"
        + ", sum(consume_ot_amt - sub_ot_amt) as ot_amt" + " from mkt_card_consume"
        + " where ori_card_no in (select ori_card_no from crd_card";
    // -ID-
    if (dsParm.colEq("consume_type", "1")) {
      sql1 += " where id_p_seqno =?)";
      setString2(1, idPSeqno);
    } else if (dsParm.colEq("consume_type", "2")) {
      sql1 += " where (card_no =? or major_card_no =?) )";
      setString2(1, cardNo);
      setString(cardNo);
    } else if (dsParm.colEq("consume_type", "3")) {
      sql1 += " where card_no =? )";
      setString2(1, cardNo);
    } else if (dsParm.colEq("consume_type", "0")) {
      sql1 += " where card_no =? )";
      setString2(1, cardNo);
      lbAll = true;
    }
    sql1 += " and acct_month between ? and ?";
    setString(aYm1);
    setString(aYm2);

    wp.logSql = true;
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return 0;

    if (lbAll || dsParm.colEq("consume_bl", "Y"))
      lmAmt += colNum("bl_amt");
    if (lbAll || dsParm.colEq("consume_ca", "Y"))
      lmAmt += colNum("ca_amt");
    if (lbAll || dsParm.colEq("consume_it", "Y"))
      lmAmt += colNum("it_amt");
    if (lbAll || dsParm.colEq("consume_ao", "Y"))
      lmAmt += colNum("ao_amt");
    if (lbAll || dsParm.colEq("consume_id", "Y"))
      lmAmt += colNum("id_amt");
    if (lbAll || dsParm.colEq("consume_ot", "Y"))
      lmAmt += colNum("ot_amt");

    return lmAmt;
  }


  int selectBillCnt(String aCardNo, String aDate1, String aDate2, double amAmt) {
    int llCnt = 0;
    String sql1 = "select count(*) as xx_ant from bil_bill"
        + " where card_no =? and purchase_date between ? and ?"
        + " and dest_amt >=? and sign_flag='+' "
        + " and mcht_category in (select mcc_code from cms_mcc_group where mcc_group =?)";
    setString2(1, aCardNo);
    setString(aDate1);
    setString(aDate2);
    setDouble(amAmt);
    setString(dsParm.colStr("air_mcc_group07"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return 0;
    llCnt = colInt("xx_cnt");
    return llCnt;
  }

  double selectBillAmt(String aCardNo, String aDate1, String aDate2) {
    double lmAmt = 0;
    String sql1 = "select sum(dest_amt) as xx_amt from bil_bill"
        + " where card_no =? and purchase_date between ? and ?" + " and sign_flag ='+'"
        + " and mcht_category in (select mcc_code from cms_mcc_group where mcc_group =?)";
    setString2(1, aCardNo);
    setString(aDate1);
    setString(aDate2);
    setString(dsParm.colStr("air_mcc_group07"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return 0;
    lmAmt = colNum("xx_amt");

    return lmAmt;
  }

  void insertCmsRightCal(String aType, double amPerAmt) {
    String lsCurrYear = sysDate.substring(0, 4);

    // msgOK();
    busi.SqlPrepare ttCal = new busi.SqlPrepare();
    ttCal.sql2Insert("cms_right_cal");
    ttCal.addsqlParm(" ?", "id_p_seqno", idPSeqno);
    ttCal.addsqlParm(",?", ", curr_year", lsCurrYear);
    ttCal.addsqlParm(",?", ", card_no", cardNo);
    ttCal.addsqlParm(",?", ", item_no", itemNo);
    ttCal.addsqlParm(",?", ", proj_code", projCode);
    ttCal.addsqlParm(",?", ", free_type", aType);
    ttCal.addsqlParm(",?", ", free_cnt", freeCnt);
    ttCal.addsqlParm(",?", ", free_per_amt", amPerAmt);
    ttCal.addsqlParm(",?", ", curr_proj_amt", projThisAmt);
    ttCal.addsqlParm(",?", ", last_proj_amt", projLastAmt);
    ttCal.addsqlParm(",?", ", curr_year_consume", thiConsumeAmt);
    ttCal.addsqlParm(",?", ", last_year_consume", lastConsumeAmt);
    ttCal.addsqlYmd(", free_cal_date");
    ttCal.addsqlParm(",?", ", card_hldr_flag", cardHldr);
    ttCal.addsqlParm(",?", ", debut_type", debutType);
    ttCal.addsqlYmd(", crt_date");
    ttCal.addsqlParm(",?", ", crt_user", modUser);
    ttCal.modxxx(modUser, modUser);

    sqlExec(ttCal.sqlStmt(), ttCal.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cms_right_cal error ");
      return;
    }

    iiSuccessCnt++;
    return;
  }

  int updateCmsRightCal(String aType, double amPerAmt) {
    String lsCurrYear = sysDate.substring(0, 4);

    // msgOK();
    busi.SqlPrepare ttCal = new busi.SqlPrepare();
    ttCal.sql2Update("cms_right_cal");
    // tt_cal.aaa(" ?","id_p_seqno",_id_p_seqno);
    // tt_cal.aaa(",?",", curr_year", ls_curr_year);
    // tt_cal.aaa(",?",", card_no", _card_no);
    // tt_cal.aaa(",?",", item_no",_item_no);
    // tt_cal.aaa(",?",", proj_code", _proj_code);
    // tt_cal.aaa(",?",", free_type", a_type);
    ttCal.addsqlParm(" free_cnt =?", freeCnt);
    ttCal.addsqlParm(", free_per_amt =?", amPerAmt);
    ttCal.addsqlParm(", curr_proj_amt =?", projThisAmt);
    // tt_cal.aaa(",?",", last_proj_amt", _proj_last_amt);
    ttCal.addsqlParm(", curr_year_consume =?", thiConsumeAmt);
    ttCal.addsqlYmd(", free_cal_date");
    ttCal.modxxx(modUser, modUser);
    ttCal.addsqlParm(" where id_p_seqno =?", idPSeqno);
    ttCal.addsqlParm(" and curr_year =?", lsCurrYear);
    ttCal.addsqlParm(" and card_no =?", cardNo);
    ttCal.addsqlParm(" and item_no =?", itemNo);
    ttCal.addsqlParm(" and proj_code =?", projCode);
    ttCal.addsqlParm(" and free_type =?", aType);

    sqlExec(ttCal.sqlStmt(), ttCal.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("update cms_right_cal error ");
      return 0;
    }

    iiSuccessCnt++;
    return 1;
  }


  public void checkIdnoRight(String aIdno, String aItemNo) {

  }

  void getCardData() {

    String sql1 = " select acct_type, card_type, group_code , issue_date" + " from crd_card "
        + " where card_no =? ";

    sqlSelect(sql1, new Object[] {cardNo});

    if (sqlRowNum <= 0) {
      errmsg("get Card Data error !");
      return;
    }

    isCardType = colStr("card_type");
    isAcctType = colStr("acct_type");
    isGroupCode = colStr("group_code");
    isIssueDate = colStr("issue_date");

  }

  int getProjCodeCurr() {
    int liOk = 0;

    getCardData();
    if (rc != 1)
      return 0;
    String sql1 = " select * , '' as ok_debut from cms_right_parm " + " where 1=1 "
    // + " and apr_flag = 'Y' "
        + " and active_status = 'Y' " + " and item_no = ? and card_hldr_flag = ? "
        + " and (curr_cond ='Y' or air_cond='Y')" + " and consume_type <>'0'"
        + " order by debut_year_flag Asc ";

    dsParm.colList = sqlQuery(sql1, new Object[] {itemNo, cardHldr});

    int ilRows = dsParm.listRows();
    if (ilRows <= 0)
      return 0;

    int rr = 0;
    boolean lbDebut = false;
    for (int ii = 0; ii < ilRows; ii++) {
      dsParm.listToCol(ii);

      String lsProjCode = dsParm.colStr("proj_code");
      // --帳戶類別
      if (dsParm.colEq("acct_type_flag", "1")) {
        if (checkDetl(lsProjCode, itemNo, "01", isAcctType, "") == false) {
          continue;
        }
      }
      // --團代+卡種
      if (dsParm.colEq("group_card_flag", "Y")) {
        if (checkDetl(lsProjCode, itemNo, "02", isGroupCode, isCardType) == false) {
          continue;
        }
      }

      // --不檢核直接送不論幾個
      String lsDebutFlag = dsParm.colStr("debut_year_flag");
      if (eqIgno(lsDebutFlag, "0")) {
        dsParm.colSet("ok_debut", "0");
        liOk++;
        wp.log("-->[%s], [%s]", lsProjCode, "0");
        continue;
      }

      // --首年認定：檢核順序為新發卡→首辦卡→舊卡友 只送一個
      if (lbDebut)
        continue;
      if (eqIgno(lsDebutFlag, "1")) {
        if (checkDebut("1", dsParm.colInt("debut_month1"), lsProjCode, "03") == false) {
          continue;
        }
      } else if (eqIgno(lsDebutFlag, "2")) {
        if (checkDebut("2", dsParm.colInt("debut_month2"), lsProjCode, "03") == false) {
          continue;
        }
      } else if (eq(lsDebutFlag, "3")) {
        // --舊卡友:持卡第二年起--
        int liIssueYy = strToInt(isIssueDate.substring(0, 4));
        int liSysYy = strToInt(wp.sysDate.substring(0, 4));
        if (liIssueYy >= liSysYy)
          continue;
      }

      // is_proj_code[rr] = ls_debut_flag;
      lbDebut = true;
      dsParm.colSet("ok_debut", lsDebutFlag);
      liOk++;

      // wp.ddd("-->[%s], [%s]",ls_proj_code,ls_debut_flag);
    }

    return liOk;

  }

  int getProjCode() {
    int liOk = 0;

    getCardData();
    if (rc != 1)
      return 0;
    String sql1 = " select * , '' as ok_debut from cms_right_parm " + " where 1=1 "
    // + " and apr_flag = 'Y' "
        + " and active_status = 'Y' "
        // + " and consume_type <> '1'"
        + " and item_no = ? and card_hldr_flag = ?" + " order by debut_year_flag Asc ";
    // sqlSelect(sql1,new Object[]{_item_no,_card_hldr});
    dsParm.colList = sqlQuery(sql1, new Object[] {itemNo, cardHldr});

    int ilRows = dsParm.listRows();
    if (ilRows <= 0)
      return 0;

    int rr = 0;
    boolean lbDebut = false;
    for (int ii = 0; ii < ilRows; ii++) {
      dsParm.listToCol(ii);

      String lsProjCode = dsParm.colStr("proj_code");
      // --帳戶類別
      if (dsParm.colEq("acct_type_flag", "1")) {
        if (checkDetl(lsProjCode, itemNo, "01", isAcctType, "") == false) {
          continue;
        }
      }
      // --團代+卡種
      if (dsParm.colEq("group_card_flag", "Y")) {
        if (checkDetl(lsProjCode, itemNo, "02", isGroupCode, isCardType) == false) {
          continue;
        }
      }

      // --不檢核直接送不論幾個
      String lsDebutFlag = dsParm.colStr("debut_year_flag");
      if (eqIgno(lsDebutFlag, "0")) {
        dsParm.colSet("ok_debut", "0");
        liOk++;
        wp.log("-->[%s], [%s]", lsProjCode, "0");
        continue;
      }

      // --首年認定：檢核順序為新發卡→首辦卡→舊卡友 只送一個
      if (lbDebut)
        continue;
      if (eqIgno(lsDebutFlag, "1")) {
        if (checkDebut("1", dsParm.colInt("debut_month1"), lsProjCode, "03") == false) {
          continue;
        }
      } else if (eqIgno(lsDebutFlag, "2")) {
        if (checkDebut("2", dsParm.colInt("debut_month2"), lsProjCode, "03") == false) {
          continue;
        }
      } else if (eq(lsDebutFlag, "3")) {
        // --舊卡友:持卡第二年起--
        int liIssueYy = strToInt(isIssueDate.substring(0, 4));
        int liSysYy = strToInt(wp.sysDate.substring(0, 4));
        if (liIssueYy >= liSysYy)
          continue;
      }

      // is_proj_code[rr] = ls_debut_flag;
      lbDebut = true;
      dsParm.colSet("ok_debut", lsDebutFlag);
      liOk++;

      wp.log("-->[%s], [%s]", lsProjCode, lsDebutFlag);
    }

    return liOk;
  }

  boolean checkDetl(String projCode, String itemNo, String dataType, String dataCode,
      String dataCode2) {

    if (empty(dataCode) && empty(dataCode2))
      return false;

    String sql1 =
        " select count(*) as type_cnt from cms_right_parm_detl " + " where table_id ='RIGHT' "
        // + " and apr_flag = 'Y' "
            + " and proj_code = ? and item_no = ? and data_type = ? "
            + " and ? like data_code||'%' " + " and ? like data_code2||'%' ";
    sqlSelect(sql1, new Object[] {projCode, itemNo, dataType, dataCode, dataCode2});

    if (sqlRowNum <= 0 || colNum("type_cnt") <= 0)
      return false;

    return true;
  }

  boolean checkDebut(String type, int aiMonth, String projCode, String dataType) {
    // 首年認定：1. 新發卡:核卡日前 個月至本年度從未持有本行該群組任一信用卡
    // 2. 首辦卡:前一年度第 個月至本年度從未持有本行該群組任一信用卡
    String lsYymm = "", lsDebutYearCond = "";

    if (eqIgno(type, "1"))
      lsYymm = commDate.dateAdd(wp.sysDate, 0, 0 - aiMonth, 0);
    else if (eqIgno(type, "2")) {
      lsYymm = commDate.dateAdd(wp.sysDate.substring(0, 4), -1, aiMonth, 0);
    }

    lsDebutYearCond = dsParm.colStr("debut_year_flag");

    if (eqIgno(lsDebutYearCond, "0"))
      return true;

    String sql1 = " select count(*) as debut_cnt1 "
        + " from crd_idno A join crd_card B on A.id_p_seqno = B.id_p_seqno "
        + " where A.id_no = ? and B.issue_date >= ? ";

    if (eqIgno(lsDebutYearCond, "1")) {
      sql1 += " and group_code in "
          + " (select data_code from cms_right_parm_detl where table_id ='RIGHT' "
          // + " and apr_flag = 'Y' "
          + " and proj_code = ? and item_no = ? and data_type = ? " + " ) ";
    } else if (eqIgno(lsDebutYearCond, "2")) {
      sql1 += " and group_code not in "
          + " (select data_code from cms_right_parm_detl where table_id ='RIGHT' "
          // + " and apr_flag = 'Y' "
          + " and proj_code = ? and item_no = ? and data_type = ? " + " ) ";
    }

    sqlSelect(sql1, new Object[] {idNo, lsYymm, projCode, itemNo, dataType});

    if (sqlRowNum <= 0 || colNum("debut_cnt1") == 0)
      return false;

    return true;
  }

  String getIssueDate(String cardno) {
    String sql1 = "select issue_date from crd_card where card_no = ? ";
    sqlSelect(sql1, new Object[] {cardno});
    if (sqlRowNum > 0)
      return colStr("issue_date");

    return "";
  }

  boolean checkBilBill(String cardno, String purchaseDate1, String purchaseDate2, String dataType) {

    daoTid = "bil.";
    String sql1 = " select dest_amt from bil_bill where purchase_date >= ? and purchase_date <= ? "
        + " and card_no = ? " + " and mcht_category in "
        + " (select data_code from cms_right_parm_detl where " + " table_id ='RIGHT' "
        // + " and apr_flag = 'Y' "
        + " and proj_code = ? and item_no = ? and data_type = ? )  ";

    sqlSelect(sql1,
        new Object[] {purchaseDate1, purchaseDate2, cardno, projCode, itemNo, dataType});

    if (sqlRowNum < 0) {
      errmsg("select bil_bill error !");
      return false;
    } else if (sqlRowNum == 0)
      return false;

    int ilSelectRows = sqlRowNum;
    int liDestAmt = 0;
    if (eqIgno(colStr("parm.air_amt_type"), "1")) {
      // --單筆
      for (int ii = 0; ii < ilSelectRows; ii++) {
        if (colNum(ii, "bil.dest_amt") >= colNum("parm.air_amt"))
          return true;
      }
    } else if (eqIgno(colStr("parm.air_amt_type"), "2")) {
      // --多筆
      for (int ii = 0; ii < ilSelectRows; ii++) {
        liDestAmt += colNum(ii, "bil.dest_amt");
      }
      if (liDestAmt >= colNum("parm.air_amt"))
        return true;
    }

    return false;
  }

  void getIdPseqno(String data) {
    String sql1 = "";
    if (data.length() == 16) {
      sql1 = " select major_id_p_seqno as id_p_seqno from crd_card where card_no = ? ";
    } else if (data.length() == 10) {
      sql1 = " select id_p_seqno from crd_idno where id_no = ? ";
    }

    sqlSelect(sql1, new Object[] {data});
    if (sqlRowNum > 0) {
      idPSeqno = colStr("id_p_seqno");
    }
  }
  //
  // void clearStringBuff() {
  // for(int ii=0 ; ii<is_proj_code.length;ii++){
  // is_proj_code[ii] = "";
  // is_debut_type[ii] = "";
  // }
  // }

}
