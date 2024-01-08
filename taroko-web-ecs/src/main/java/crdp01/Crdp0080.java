/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-14  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdp0080 extends BaseProc {
  Crdp0080Func func;
  int rr = -1;
  String msg = "";
  //String kk1 = "";
  int ilOk = 0;
  int ilErr = 0, liExtn = 0, liActCreditAmt = 0;
  String lsBatchno = "", lsRiskBankNo = "", cardType = "", unitCode = "", memberNote = "",
      regBankNo = "", icFlag = "", idNo = "", idNoCode = "", majorCardNo = "", majorValidFm = "",
      majorValidTo = "", pmId = "", pmIdCode = "", emboss4thData = "", lsComboIndicator = "",
      sourceCode = "", corpNo = "", corpNoCode = "", acctType = "", chiName = "", birthday = "",
      engName = "", lsEndVal = "", lsBegVal = "", validFm = "", validTo = "", supFlag = "",
      oldBegDate = "", oldEndDate = "", forceFlag = "";
  double liRecno = 0;
  String[] aaGroupCode, aaCardNo, aaRowid, opt, aaCrdCardModSeqno, aaCrdAardTmpModSeqno, aaCrtDate,
      aaKindType;
  String[] aaChangeStatus, aaExpireChgFlag, aaExpireReason, aaSupFlag, aaMajorCardNo, aaNewEndDate,
      aaIdPSeqno, aaAcnoPSeqno, aaCardType, aaUnitCode, aaRegBankNo, aaIcFlag, aaIdNo, aaIdNoCode,
      aaEmbossData, aaSourceCode, aaCorpNo, aaCorpNoCode, aaAcctType, aaEngName, aaNewBegDate,
      aaForceFlag, aaComboIndicator, aaProcessKind;

  SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyyMMdd");
  String date = nowdate.format(new java.util.Date());
  SimpleDateFormat now = new java.text.SimpleDateFormat("yyyyMM");
  String dateYm = now.format(new java.util.Date());
  Calendar cal = Calendar.getInstance();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_aprid", wp.loginUser);

  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr(0, "ex_crt_user");
      dddwList("ex_crt_user", "sec_user", "usr_id", "usr_id||'['||usr_cname||']'",
          "where 1=1 order by usr_id");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_date1");
    String lsDate2 = wp.itemStr("ex_date2");
    wp.whereStr = "where  a.kind_type ='080' and a.apr_user='' ";

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return -1;
    }

    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and a.crt_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and a.crt_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and a.crt_user = :crt_user ";
      setString("crt_user", wp.itemStr("ex_crt_user"));
    }
    switch (wp.itemStr("ex_crdtype")) {
      case "1":
        wp.whereStr += " and a.process_kind = '1' ";
        break;
      case "2":
        wp.whereStr += " and a.process_kind = '2' ";
        break;
      case "3":
        wp.whereStr += " and a.process_kind = '3' ";
        break;
      case "4":
        wp.whereStr += " and a.process_kind = '4' ";
        break;
    }
    return 1;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        "hex(a.rowid) as rowid, " + " a.card_no, " + " c.id_no||'_'||c.id_no_code as wk_id, "
            + " c.id_no, " + " c.id_no_code, " + " a.corp_no, " + " a.process_kind, "
            + " c.chi_name, " + " b.group_code, " + " a.expire_chg_flag, " + " a.expire_reason, "
            + " b.change_reason, " + " a.expire_chg_flag_old, " + " a.expire_reason_old, "
            + " a.change_status, " + " a.cur_end_date, " + " a.crt_user, " + " a.crt_date, "
            + " b.sup_flag, " + " b.major_card_no, " + " b.new_end_date, " + " a.id_p_seqno, "
            + " b.acno_p_seqno, " + " b.card_type, " + " b.unit_code, " + " b.member_note, "
            + " b.reg_bank_no, " + " b.ic_flag, " + " b.source_code, "
            + " b.corp_no as crd_card_corp_no, " + " b.corp_no_code, " + " b.acct_type, "
            + " b.eng_name, " + " b.new_beg_date, " + " b.new_end_date, " + " b.force_flag, "
            + " b.combo_indicator, " + " b.emboss_data, " + " b.mod_seqno as crd_card_mod_seqno, "
            + " a.mod_seqno as crd_card_tmp_mod_seqno, " + " a.kind_type ";

    wp.daoTable = "crd_card_tmp as a  join crd_card as b on a.card_no = b.card_no "
        + " join crd_idno as c on b.id_p_seqno = c.id_p_seqno";

    wp.whereOrder = "";
    if (getWhereStr() != 1)
      return;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata(wp.selectCnt);
  }

  void listWkdata(int selectCnt) throws Exception {
    String sql = "", sql2 = "";
    String wkData = "";
    for (int ii = 0; ii < selectCnt; ii++) {
      if (wp.colStr(ii, "expire_chg_flag").equals("1")) {
        sql =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :expire_reason";
        setString("expire_reason", wp.colStr(ii, "expire_reason"));
        sqlSelect(sql);
      }

      if (wp.colStr(ii, "expire_chg_flag").equals("4")) {
        sql =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :expire_reason";
        setString("expire_reason", wp.colStr(ii, "expire_reason"));
        sqlSelect(sql);
      }
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_expire_reason", wp.colStr(ii, "expire_reason") + sqlStr("wf_desc"));
      } else {
        wp.colSet(ii, "db_expire_reason", wp.colStr(ii, "expire_reason"));
      }
      if (wp.colStr(ii, "expire_chg_flag_old").equals("1")) {
        sql2 =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :expire_reason_old";
        setString("expire_reason_old", wp.colStr(ii, "expire_reason_old"));
        sqlSelect(sql2);
      }
      if (wp.colStr(ii, "expire_chg_flag_old").equals("4")) {
        sql2 =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :expire_reason_old";
        setString("expire_reason_old", wp.colStr(ii, "expire_reason_old"));
        sqlSelect(sql2);
      }
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_expire_reason_old",
            wp.colStr(ii, "expire_reason_old") + sqlStr("wf_desc"));
      } else {
        wp.colSet(ii, "db_expire_reason_old", wp.colStr(ii, "expire_reason_old"));
      }

      wkData = wp.colStr(ii, "process_kind");
      wp.colSet(ii, "tt_aprocess_kind",
          commString.decode(wkData, ",0,1,4,2,3", ",取消不續卡(放行前),預約不續卡,人工不續卡,取消不續卡(放行後),系統不續卡改續卡"));

      wkData = wp.colStr(ii, "expire_chg_flag");
      wp.colSet(ii, "tt_expire_chg_flag",
          commString.decode(wkData, ",1,4,2,3", ",預約不續卡,人工不續卡,取消不續卡,系統不續卡改續卡"));

      wkData = wp.colStr(ii, "change_status");
      wp.colSet(ii, "tt_change_status", commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));

      wkData = wp.colStr(ii, "expire_chg_flag_old");
      wp.colSet(ii, "tt_expire_chg_flag_old", commString.decode(wkData, ",1,4,5", ",預約不續卡,人工不續卡,系統不續卡"));

      wkData = wp.colStr(ii, "change_reason");
      wp.colSet(ii, "tt_change_reason", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

    }

  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    func = new Crdp0080Func(wp);
    aaCrdAardTmpModSeqno = wp.itemBuff("crd_card_tmp_mod_seqno");
    aaCrdCardModSeqno = wp.itemBuff("crd_card_mod_seqno");
    aaGroupCode = wp.itemBuff("group_code");
    aaCardNo = wp.itemBuff("card_no");
    aaRowid = wp.itemBuff("rowid");
    opt = wp.itemBuff("opt");
    aaCrtDate = wp.itemBuff("crt_date");
    aaKindType = wp.itemBuff("kind_type");
    aaChangeStatus = wp.itemBuff("change_status");
    aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    aaExpireReason = wp.itemBuff("expire_reason");
    aaSupFlag = wp.itemBuff("sup_flag");
    aaMajorCardNo = wp.itemBuff("major_card_no");
    aaNewEndDate = wp.itemBuff("new_end_date");
    aaIdPSeqno = wp.itemBuff("id_p_seqno");
    aaAcnoPSeqno = wp.itemBuff("acno_p_seqno");
    aaCardType = wp.itemBuff("card_type");
    aaUnitCode = wp.itemBuff("unit_code");
    aaRegBankNo = wp.itemBuff("reg_bank_no");
    aaIcFlag = wp.itemBuff("ic_flag");
    aaIdNo = wp.itemBuff("id_no");
    aaIdNoCode = wp.itemBuff("id_no_code");
    aaEmbossData = wp.itemBuff("emboss_data");
    aaSourceCode = wp.itemBuff("source_code");
    aaCorpNo = wp.itemBuff("corp_no");
    aaCorpNoCode = wp.itemBuff("corp_no_code");
    aaAcctType = wp.itemBuff("acct_type");
    aaEngName = wp.itemBuff("eng_name");
    aaNewBegDate = wp.itemBuff("new_beg_date");
    aaForceFlag = wp.itemBuff("force_flag");
    aaComboIndicator = wp.itemBuff("combo_indicator");
    aaProcessKind = wp.itemBuff("process_kind");

    wp.listCount[0] = aaRowid.length;

    // dataCheck
    String lsBatchno1 = strMid(date, 2, 6);
    String lsSql3 = " select max(batchno) as ls_batchno " + " from crd_emboss_tmp "
        + " where substr(batchno,1,6) = :ls_batchno1 ";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(lsSql3);
    lsBatchno = sqlStr("ls_batchno");
    if (empty(lsBatchno)) {
      lsBatchno = lsBatchno1 + "01";
    } else {
      String lsSql4 = "select max(recno)+1 as li_recno " + " from crd_emboss_tmp "
          + " where batchno = :ls_batchno ";
      setString("ls_batchno", lsBatchno);
      sqlSelect(lsSql4);
    }
    liRecno = sqlNum("li_recno");
    if (liRecno == 0) {
      liRecno = 1;
    }

    // -update-
    for (rr = 0; rr < aaCardNo.length; rr++) {
      if (!checkBoxOptOn(rr, opt)) {
        continue;
      }

      func.varsSet("aa_group_code", aaGroupCode[rr]);
      func.varsSet("aa_card_no", aaCardNo[rr]);
      func.varsSet("aa_rowid", aaRowid[rr]);
      func.varsSet("aa_crt_date", aaCrtDate[rr]);
      func.varsSet("aa_crd_card_tmp_mod_seqno", aaCrdAardTmpModSeqno[rr]);
      func.varsSet("aa_crd_card_mod_seqno", aaCrdCardModSeqno[rr]);
      func.varsSet("aa_kind_type", aaKindType[rr]);

      if (func.updateFunc4() != 1) {
        alertErr("update crd_card_tmp err");
        sqlCommit(0);
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      switch (aaProcessKind[rr]) {
        case "1":
          if (wfUpdCrdCard() != 1) {
            ilErr++;
          }
          break;
        case "2":
          if (wfCancelExpire() != 1) {
            ilErr++;
          } else {
            if (aaComboIndicator[rr].equals("Y")) {
              if (wfDelNotchg() != 1) {
                ilErr++;
              }
            }
          }
          break;
        case "3":
          if (wfMoveEmbossTmp() != 1) {
            ilErr++;
          } else {
            if (aaComboIndicator[rr].equals("Y")) {
              if (wfDelNotchg() != 1) {
                ilErr++;
              }
            }
          }
          break;
        case "4":
          if (wfUpdCrdCard() != 1) {
            ilErr++;
          }
          break;
      }
      if (ilErr > 0) {
        break;
      }
    }
    if (ilErr > 0) {
      sqlCommit(0);
      wp.colSet(rr, "ok_flag", "!");
      alertMsg("處理失敗," + msg);
    } else {
      if (func.insertFunc2() != 1) {
        wp.alertMesg =
            "<script language='javascript'> alert('Insert crd_card_tmp_h 錯誤 !!')</script>";
      }
      if (func.dbDelete4() != 1) {
        wp.alertMesg = "<script language='javascript'> alert('delete crd_card_tmp 錯誤 !!')</script>";
      }
      sqlCommit(1);
      queryFunc();
      errmsg("處理完成");
    }
  }

  int wfUpdCrdCard() {
    String lsCardNo = "", lsExpireChgFlag = "", tempExpireChgFlag = "";
    String lsChangeStatus = "", lsExpireReason = "";
    lsCardNo = aaCardNo[rr];
    lsExpireChgFlag = aaExpireChgFlag[rr];
    lsChangeStatus = aaChangeStatus[rr];
    lsExpireReason = aaExpireReason[rr];

    if (lsChangeStatus.equals("2")) {
      msg = "此卡片已送製卡,不可作任何異動";
      return -1;
    }
    if (lsChangeStatus.equals("1")) {
      if (func.dbDelete(lsCardNo) != 1) {
        msg = "續卡製卡中，不可改為不續卡";
        return -1;
      }
    }
    if (lsExpireChgFlag.equals("1"))
      tempExpireChgFlag = "2";
    if (lsExpireChgFlag.equals("4"))
      tempExpireChgFlag = "3";
    func.varsSet("ls_card_no", lsCardNo);
    func.varsSet("temp_expire_chg_flag", tempExpireChgFlag);
    func.varsSet("ls_expire_reason", lsExpireReason);
    if (func.updateFunc2() != 1) {
      msg = "寫入不續卡註記失敗";
      return -1;
    }
    return 1;
  }

  int wfCancelExpire() throws Exception {

    String lsCardNo = "", lsExpireChgFlag = "";
    String lsChangeStatus = "";
    lsCardNo = aaCardNo[rr];
    func.varsSet("ls_card_no", lsCardNo);

    String lsSql9 =
        "select expire_chg_flag,change_status  from crd_card  where card_no = :ls_card_no ";
    setString("ls_card_no", lsCardNo);
    sqlSelect(lsSql9);
    lsChangeStatus = sqlStr("change_status");
    lsExpireChgFlag = sqlStr("expire_chg_flag");

    if (lsChangeStatus.equals("2")) {
      msg = "此卡片已送製卡,不可作任何異動";
      return -1;
    }
    if (empty(lsExpireChgFlag) == true) {
      msg = "此筆資料本身並無不續卡註記";
      return -1;
    }
    if (lsChangeStatus.equals("1")) {
      if (func.dbDelete2() != 1) {
        msg = "此卡片已送製卡,不可做預約不續卡";
        return -1;
      }
    }
    if (func.updateFunc3() != 1) {
      msg = "取消不續卡註記失敗";
      return -1;
    }

    return 1;
  }

  int wfDelNotchg() throws Exception {

    String lsCardNo = "";
    lsCardNo = aaCardNo[rr];
    String lsSql = " select hex(a.rowid) as ls_rowid  from crd_notchg  where card_no = :card_no ";
    setString("card_no", lsCardNo);
    sqlSelect(lsSql);

    if (sqlRowNum != 0) {
      if (func.dbDelete3() != 1) {
        msg = "無法刪除combo卡不續卡資料";
        return -1;
      }
    }
    return 1;
  }

  int wfMoveEmbossTmp() throws Exception {

    if (aaChangeStatus[rr].equals("1")) {
      msg = "續卡製卡中，不可改為不續卡";
      return -1;
    } else if (aaChangeStatus[rr].equals("2")) {
      msg = "此卡片送製卡中,不可再做提前續卡";
      return -1;
    }
    String lsSql = " select count (*) as count  from crd_emboss_tmp  where old_card_no = :card_no ";
    setString("card_no", aaCardNo[rr]);
    sqlSelect(lsSql);
    if (sqlNum("count") > 0) {
      msg = "續卡製卡中，不可改為不續卡";
      return -1;
    }
    if (aaSupFlag[rr].equals("1")) {
      String lsSql2 = "select new_beg_date,new_end_date,current_code  from crd_card "
          + " where card_no = :major_card_no ";

      setString("major_card_no", aaMajorCardNo[rr]);
      sqlSelect(lsSql2);
      majorValidFm = sqlStr("new_beg_date");
      majorValidTo = sqlStr("new_end_date");
      if (sqlRowNum == 0) {
        msg = "找取不到正卡資料";
        return -1;
      }
      if (!sqlStr("current_code").equals("0")) {
        msg = "正卡不為正常卡,不可做線上續卡";
        return -1;
      }
    }
    if (empty(aaNewEndDate[rr]) == false) {
      Date date = nowdate.parse(aaNewEndDate[rr]);
      cal.setTime(date);
      cal.add(Calendar.MARCH, -6);
      String d = nowdate.format(cal.getTime());
      int newEndDate = this.toInt(d);
      int sysdate = this.toInt(this.date);
      if (newEndDate > sysdate) {
        msg = "效期需在系統日六個月內" + "(" + newEndDate + ")";
        return -1;
      }
    }
    String lsSql5 = "select chi_name,birthday  from crd_idno  where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", aaIdPSeqno[rr]);
    sqlSelect(lsSql5);
    chiName = sqlStr("chi_name");
    birthday = sqlStr("birthday");
    if (sqlRowNum == 0) {
      msg = "抓取卡人檔失敗," + sqlRowNum;
      return -1;
    }
    String lsSql6 = "select line_of_credit_amt,chg_addr_date,risk_bank_no  from act_acno "
        + " where acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", aaAcnoPSeqno[rr]);
    sqlSelect(lsSql6);
    liActCreditAmt = this.toInt(sqlStr("line_of_credit_amt"));

    lsRiskBankNo = sqlStr("risk_bank_no");

    if (sqlRowNum == 0) {
      return -1;
    }
    liExtn = wfGetExtnYear(aaUnitCode[rr], aaCardType[rr]);

    if (liExtn == -1) {
      msg = "抓取不到展期年~";
      return -1;
    }
    if (empty(aaGroupCode[rr])) {
      aaGroupCode[rr] = "0000";
    }
    String lsSql7 =
        "select combo_indicator " + " from ptr_group_code " + " where group_code = :ls_group_code ";
    setString("ls_group_code", aaGroupCode[rr]);
    sqlSelect(lsSql7);
    lsComboIndicator = sqlStr("combo_indicator");

    if (sqlRowNum == 0) {
      msg = "抓取ptr_group_code檔失敗" + sqlRowNum;
      return -1;
    }

    String lsMajorValidFm = "";
    String lsMajorValidTo = "";
    String lsSql8 = " select acct_key from act_acno where acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", aaAcnoPSeqno[rr]);
    sqlSelect(lsSql8);

    func.varsSet("acct_key", sqlStr("acct_key"));
    cardType = aaCardType[rr];
    unitCode = aaUnitCode[rr];
    regBankNo = aaRegBankNo[rr];
    icFlag = aaIcFlag[rr];
    idNo = aaIdNo[rr];
    idNoCode = aaIdNoCode[rr];
    emboss4thData = aaEmbossData[rr];
    sourceCode = aaSourceCode[rr];
    corpNo = aaCorpNo[rr];
    corpNoCode = aaCorpNoCode[rr];
    acctType = aaAcctType[rr];
    engName = aaEngName[rr];

    if (aaSupFlag[rr].equals("1")) {
      String lsSql10 =
          " select major_id, major_id_code from crd_idno where id_p_seqno = :id_p_seqno ";
      setString("id_p_seqno", aaIdPSeqno[rr]);
      sqlSelect(lsSql10);
      pmId = sqlStr("major_id");
      pmIdCode = sqlStr("major_id_code");
      majorCardNo = aaMajorCardNo[rr];
      lsMajorValidFm = majorValidFm;
      lsMajorValidTo = majorValidTo;
      lsEndVal = majorValidTo;

    } else {
      lsEndVal = aaNewEndDate[rr];
    }
    lsBegVal = dateYm + "01";
    lsEndVal = ofRelativeymd(lsEndVal);
    validFm = lsBegVal;
    validTo = lsEndVal;
    supFlag = aaSupFlag[rr];
    oldBegDate = aaNewBegDate[rr];
    oldEndDate = aaNewEndDate[rr];
    forceFlag = aaForceFlag[rr];
    func.varsSet("ls_batchno", lsBatchno);
    func.varsSet("li_recno", Double.toString(liRecno));
    func.varsSet("ls_risk_bank_no", lsRiskBankNo);
    func.varsSet("card_type", cardType);
    func.varsSet("unit_code", unitCode);
    func.varsSet("reg_bank_no", regBankNo);
    func.varsSet("ic_flag", icFlag);
    func.varsSet("id_no", idNo);
    func.varsSet("id_no_code", idNoCode);
    func.varsSet("major_card_no", majorCardNo);
    func.varsSet("ls_major_valid_fm", lsMajorValidFm);
    func.varsSet("ls_major_valid_to", lsMajorValidTo);
    func.varsSet("pm_id", pmId);
    func.varsSet("pm_id_code", pmIdCode);
    func.varsSet("emboss_4th_data", emboss4thData);
    func.varsSet("ls_combo_indicator", lsComboIndicator);
    func.varsSet("source_code", sourceCode);
    func.varsSet("corp_no", corpNo);
    func.varsSet("corp_no_code", corpNoCode);
    func.varsSet("acct_type", acctType);
    func.varsSet("chi_name", chiName);
    func.varsSet("birthday", birthday);
    func.varsSet("eng_name", engName);
    func.varsSet("valid_fm", validFm);
    func.varsSet("valid_to", validTo);
    func.varsSet("sup_flag", supFlag);
    func.varsSet("li_act_credit_amt", Integer.toString(liActCreditAmt));
    func.varsSet("old_beg_date", oldBegDate);
    func.varsSet("old_end_date", oldEndDate);
    func.varsSet("force_flag", forceFlag);

    if (func.insertFunc() != 1) {
      msg = "此資料無法搬到續卡檔內";
      return -1;
    }
    if (func.updateFunc1() != 1) {
      msg = "寫入卡片檔錯誤~";
      return -1;
    }

    return 1;

  }

  int wfGetExtnYear(String crdCardUnitCode, String crdCardCardType) throws Exception {
    String lsSql8 = "select extn_year " + " from crd_item_unit "
        + " where unit_code = :ls_unit_code  and card_type = :as_card_type ";
    setString("ls_unit_code", empty(crdCardUnitCode) ? "0000" : crdCardUnitCode);
    setString("as_card_type", crdCardCardType);
    sqlSelect(lsSql8);
    int liYear = (int) sqlNum("extn_year");
    if (sqlRowNum == 0) {
      return -1;
    } else
      return liYear;
  }

  String ofRelativeymd(String lsEndVal) throws ParseException {
    Date date = nowdate.parse(lsEndVal);
    cal.setTime(date);
    cal.add(Calendar.YEAR, +liExtn);
    String d = nowdate.format(cal.getTime());
    return d;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
