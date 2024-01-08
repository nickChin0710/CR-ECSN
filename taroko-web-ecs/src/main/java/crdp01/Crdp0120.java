/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-23  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdp0120 extends BaseProc {
  Crdp0120Func func;
  int rr = -1;
  String msg = "";
 // String kk1 = "";
  int ilOk = 0;
  int ilErr = 0, liExtn = 0, liActCreditAmt = 0;

  String[] aaGroupCode, aaCardNo, aaRowid, opt, aaCrdCardModSeqno, aaCrdCardTmpModSeqno, aaCrtDate,
      aaKindType;
  String[] aaProcessKind, aaExpireChgFlag, aaChangeStatus, aaCardType, aaUnitCode, aaMemberNote,
      aaRegBankNo, aaChangeReason, aaIdNo, aaIdCode, aaIcFlag, aaEmbossData, aaSourceCode, aaCorpNo,
      aaCorpNoCode, aaAcctType, aaEngName, aaForceFlag, aaNewBegDate, aaNewEndDate, aaSupFlag,
      aaIdPSeqno, aaMajorCardNo, aaCurBegDate, aaCurEndDate, aaChangeStatus2 ,aaMajorIdPSeqno;

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

  private void getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_date1");
    String lsDate2 = wp.itemStr("ex_date2");

    wp.whereStr = "where  a.kind_type ='120' and  a.apr_user ='' ";

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return;
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
    }
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        "hex(a.rowid) as rowid, " + " a.card_no, " + " c.id_no||'_'||c.id_no_code as wk_id, "
            + " c.id_no, " + " c.id_no_code, " + " a.corp_no, " + " a.process_kind, "
            + " c.chi_name, " + " b.group_code, " + " a.expire_chg_flag, " + " a.expire_reason, "
            + " a.change_reason, " + " a.expire_chg_flag_old, " + " a.expire_reason_old, "
            + " a.change_status, " + " b.change_status as change_status2, " + " a.cur_end_date, "
            + " a.cur_beg_date, " + " a.crt_user, " + " a.crt_date, " + " b.sup_flag, "
            + " b.major_card_no, " + " b.new_end_date, " + " a.id_p_seqno, " + " b.acno_p_seqno, "
            + " b.card_type, " + " b.unit_code, " + " b.member_note, " + " b.reg_bank_no, "
            + " b.ic_flag, " + " b.source_code, " + " b.corp_no as crd_card_corp_no, "
            + " b.corp_no_code, " + " b.acct_type, " + " UF_ACNO_KEY(b.acno_p_seqno) as acct_key, "
            + " b.eng_name, " + " b.new_beg_date, " + " b.new_end_date, " + " b.force_flag, "
            + " b.combo_indicator, " + " b.emboss_data, " + " b.mod_seqno as crd_card_mod_seqno, "
            + " a.mod_seqno as crd_card_tmp_mod_seqno, " + " a.kind_type, " + " a.old_end_date, "
            + " a.change_reason_old, " + " b.bin_no," + " b.curr_code, " + " b.major_id_p_seqno";

    wp.daoTable = "crd_card_tmp as a  join crd_card as b on a.card_no = b.card_no "
        + " join crd_idno as c on b.id_p_seqno = c.id_p_seqno";

    wp.whereOrder = "";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    wp.colSet("exCnt", intToStr(wp.selectCnt));
    listWkdata();

  }

  void listWkdata() throws Exception {
    String wkData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {

      wkData = wp.colStr(ii, "process_kind");
      wp.colSet(ii, "tt_aprocess_kind", commString.decode(wkData, ",1,2,3", ",線上續卡,取消線上續卡,系統續卡改不續卡"));

      wkData = wp.colStr(ii, "change_reason");
      wp.colSet(ii, "tt_change_reason", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      wkData = wp.colStr(ii, "change_status");
      wp.colSet(ii, "tt_change_status", commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));

      wkData = wp.colStr(ii, "change_reason_old");
      wp.colSet(ii, "tt_change_reason_old", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      wkData = wp.colStr(ii, "expire_chg_flag");
      wp.colSet(ii, "tt_expire_chg_flag", commString.decode(wkData, ",1,4,5", ",預約不續卡,人工不續卡,系統不續卡"));

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
    func = new Crdp0120Func(wp);

    aaGroupCode = wp.itemBuff("group_code");
    aaCardNo = wp.itemBuff("card_no");
    aaRowid = wp.itemBuff("rowid");
    opt = wp.itemBuff("opt");
    aaCrdCardTmpModSeqno = wp.itemBuff("crd_card_tmp_mod_seqno");
    aaCrdCardModSeqno = wp.itemBuff("crd_card_mod_seqno");
    aaCrtDate = wp.itemBuff("crt_date");
    aaKindType = wp.itemBuff("kind_type");
    aaProcessKind = wp.itemBuff("process_kind");
    aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    aaChangeStatus = wp.itemBuff("change_status");
    aaChangeStatus2 = wp.itemBuff("change_status2");
    aaCardType = wp.itemBuff("card_type");
    aaUnitCode = wp.itemBuff("unit_code");
    aaMemberNote = wp.itemBuff("member_note");
    aaRegBankNo = wp.itemBuff("reg_bank_no");
    aaChangeReason = wp.itemBuff("change_reason");
    aaIdNo = wp.itemBuff("id_no");
    aaIdCode = wp.itemBuff("id_no_code");
    aaIcFlag = wp.itemBuff("ic_flag");
    aaEmbossData = wp.itemBuff("emboss_data");
    aaSourceCode = wp.itemBuff("source_code");
    aaCorpNo = wp.itemBuff("corp_no");
    aaCorpNoCode = wp.itemBuff("corp_no_code");
    aaAcctType = wp.itemBuff("acct_type");
    aaEngName = wp.itemBuff("eng_name");
    aaForceFlag = wp.itemBuff("force_flag");
    aaNewBegDate = wp.itemBuff("new_beg_date");
    aaNewEndDate = wp.itemBuff("new_end_date");
    aaSupFlag = wp.itemBuff("sup_flag");
    aaIdPSeqno = wp.itemBuff("id_p_seqno");
    aaMajorCardNo = wp.itemBuff("major_card_no");
    aaCurBegDate = wp.itemBuff("cur_beg_date");
    aaCurEndDate = wp.itemBuff("cur_end_date");
    aaMajorIdPSeqno = wp.itemBuff("major_id_p_seqno");
    String[] aaAcctKey = wp.itemBuff("acct_key");
    String[] aaBinNo = wp.itemBuff("bin_no");
    String[] aaCurrCode = wp.itemBuff("curr_code");
    wp.listCount[0] = aaRowid.length;

    // -update-

    for (rr = 0; rr < aaRowid.length; rr++) {
      if (!checkBoxOptOn(rr, opt)) {
        continue;
      }
      func.varsSet("aa_group_code", aaGroupCode[rr]);
      func.varsSet("aa_card_no", aaCardNo[rr]);
      func.varsSet("aa_rowid", aaRowid[rr]);
      func.varsSet("aa_crt_date", aaCrtDate[rr]);
      func.varsSet("aa_crd_card_tmp_mod_seqno", aaCrdCardTmpModSeqno[rr]);
      func.varsSet("aa_crd_card_mod_seqno", aaCrdCardModSeqno[rr]);
      func.varsSet("aa_kind_type", aaKindType[rr]);
      func.varsSet("acct_key", aaAcctKey[rr]);
      func.varsSet("bin_no", aaBinNo[rr]);
      func.varsSet("curr_code", aaCurrCode[rr]);
      if (func.updateFunc4() < 0) {
        alertErr("update crd_card_tmp err");
        sqlCommit(0);
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      switch (aaProcessKind[rr]) {

        case "1":
          if (wfMoveEmbossTmp() != 1) {
            ilErr++;
          }
          break;
        case "2":
          if (wfCancelChg() != 1) {
            ilErr++;
          }
          break;
        case "3":
          if (wfProcessChg() != 1) {
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
      alertMsg("處理失敗," + msg);
      return;
    } else {
      if (func.insertFunc2() < 1) {
        wp.alertMesg =
            "<script language='javascript'> alert('Insert crd_card_tmp_h 錯誤 !!')</script>";
      }
      if (func.dbDelete3() < 1) {
        wp.alertMesg = "<script language='javascript'> alert('delete crd_card_tmp 錯誤 !!')</script>";
      }
      sqlCommit(1);
      queryFunc();
      errmsg("處理完成");
    }

  }

  int wfProcessChg() {

    String lsCardNo = "";
    lsCardNo = aaCardNo[rr];

    if (func.dbDelete(lsCardNo) != 1) {
      msg = "已製卡完成不在暫存檔內";
      return -1;
    }

    if (func.updateFunc2(lsCardNo) != 1) {
      msg = "寫入不續卡註記失敗";
      return -1;
    }
    return 1;
  }

  int wfCancelChg() throws Exception {

    String lsCardNo = "";
    String lsChangeStatus2 = "";
    lsCardNo = aaCardNo[rr];
    lsChangeStatus2 = aaChangeStatus2[rr];
    if (lsChangeStatus2.equals("2")) {
      msg = "此卡片已送製卡,不可作任何異動";
      return -1;
    }

    if (lsChangeStatus2.equals("1")) {
      if (func.dbDelete2(lsCardNo) != 1) {
        msg = "此卡片已送製卡,不可取消提前續卡";
        return -1;
      }
    }
    if (func.updateFunc3(lsCardNo) != 1) {
      msg = "寫入不續卡註記失敗";
      return -1;
    }

    return 1;
  }

  int wfMoveEmbossTmp() throws Exception {
    double liRecno = 0;
    String lsSql =
        " select id_p_seqno,acno_p_seqno " + " from crd_card " + " where card_no = :ls_cardno ";
    setString("ls_cardno", aaCardNo[rr]);
    sqlSelect(lsSql);
    String lsAcnoPSeqno = sqlStr("acno_p_seqno");
    String lsIdPSeqno = sqlStr("id_p_seqno");
    if (sqlRowNum <= 0) {
      msg = "找取不到卡檔資料";
      return -1;
    }

    String lsSql1 =
        " select risk_bank_no " + " from act_acno " + " where acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(lsSql1);
    String lsRiskBankNo = sqlStr("risk_bank_no");
    if (sqlRowNum <= 0) {
      msg = "無法抓取到此卡號帳戶資料";
      return -1;
    }
    String lsCreateDate = getSysDate();
    int liSystemDd = this.toInt(strMid(lsCreateDate, 6, 2));

    String lsSql2 = " select new_beg_date,new_end_date,current_code " + " from crd_card "
        + " where card_no = :ls_major_cardno ";
    setString("ls_major_cardno", wp.colStr("major_card_no"));

    sqlSelect(lsSql2);
    String lsMajorValidFm = sqlStr("new_beg_date");
    String lsMajorValidTo = sqlStr("new_end_date");
    if (sqlRowNum <= 0) {
      msg = "找取不到正卡資料";
      return -1;
    }
    if (!sqlStr("current_code").equals("0")) {
      msg = "正卡不為正常卡,不可做線上續卡";
      return -1;
    }

    String lsBatchno1 = strMid(lsCreateDate, 2, 6);

    String lsSql3 = " select max(batchno) as batchno from crd_emboss_tmp "
        + " where substr(batchno,1,6) = :ls_batchno1 ";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(lsSql3);
    String lsBatchno = sqlStr("batchno");
    if (empty(lsBatchno) == true) {
      lsBatchno = lsBatchno1 + "01";
    } else {
      String lsSql4 =
          " select max(recno)+1 as recno from crd_emboss_tmp " + " where batchno = :ls_batchno ";
      setString("ls_batchno", lsBatchno);
      sqlSelect(lsSql4);
      liRecno = sqlNum("recno");
    }

    if (liRecno == 0) {
      liRecno = 1;
    }

    String lsSql5 = " select chi_name,birthday  from crd_idno  where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdPSeqno);
    sqlSelect(lsSql5);
    String lsIdnoValue = sqlStr("chi_name");
    String lsIdnoValue2 = sqlStr("birthday");
    if (sqlRowNum <= 0) {
      msg = "抓取卡人檔失敗";
      return -1;
    }

    String lsSql6 = " select line_of_credit_amt,chg_addr_date  from act_acno "
        + " where acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(lsSql6);
    int liActCreditAmt = (int) sqlNum("line_of_credit_amt");
    if (sqlRowNum <= 0) {
      return -1;
    }

    String cardType = aaCardType[rr];
    String unitCode = aaUnitCode[rr];
    String memberNote = aaMemberNote[rr];
    String regBankNo = aaRegBankNo[rr];
    String changeReason = aaChangeReason[rr];
    String idNo = aaIdNo[rr];
    String idNoCode = aaIdCode[rr];
    String icFlag = aaIcFlag[rr];
    String pmId = "", pmIdCode = "", majorCardNo = "", majorValidFm = "", majorValidTo = "";
    String emboss4thData = aaEmbossData[rr];
    String sourceCode = aaSourceCode[rr];
    String corpNo = aaCorpNo[rr];
    String corpNoCode = aaCorpNoCode[rr];
    String acctType = aaAcctType[rr];
    String chiName = lsIdnoValue;
    String engName = aaEngName[rr];
    String birthday = lsIdnoValue2;
    int creditLmt = liActCreditAmt;
    String forceFlag = aaForceFlag[rr];
    String oldBegDate = aaNewBegDate[rr];
    String oldEndDate = aaNewEndDate[rr];

    if (aaSupFlag[rr].equals("1")) {
      String lsSql10 =
          " select id_no, id_no_code from crd_idno where id_p_seqno = :major_id_p_seqno ";
      setString("major_id_p_seqno", aaMajorIdPSeqno[rr]);
      sqlSelect(lsSql10);
      pmId = sqlStr("id_no");
      pmIdCode = sqlStr("id_no_code");
      majorCardNo = aaMajorCardNo[rr];
      majorValidFm = lsMajorValidFm;
      majorValidTo = lsMajorValidTo;
    } else {
      pmId = idNo;
      pmIdCode = idNoCode;
    }
    String lsSql7 =
        "select combo_indicator  from ptr_group_code  where group_code = :ls_group_code ";
    String lsGroupCode = aaGroupCode[rr];
    if (empty(lsGroupCode) == true) {
      lsGroupCode = "0000";
    }
    setString("ls_group_code", lsGroupCode);

    sqlSelect(lsSql7);

    String comboIndicator = sqlStr("combo_indicator");
    if (sqlRowNum <= 0) {
      msg = "抓取ptr_group_code檔失敗";
      return -1;
    }

    String lsDateFm = aaCurBegDate[rr];
    String lsDateTo = aaCurEndDate[rr];
    if (liSystemDd >= 25) {
      String lsSql8 =
          "select to_char(add_months(to_date(:ls_date_fm,'yyyymmdd'),1),'yyyymm')||'01' as ls_date_fm "
              + " from dual ";
      setString("ls_date_fm", lsDateFm);
      sqlSelect(lsSql8);
      lsDateFm = sqlStr("ls_date_fm");
      if (sqlRowNum <= 0) {
        msg = "日期資料轉換錯誤 !!";
        return -1;
      }
    }
    String sqlSelect = "select service_type from ptr_group_card "
        + " where group_code = :ls_group_code " + " and card_type = :ls_card_type  ";
    setString("ls_group_code", lsGroupCode);
    setString("ls_card_type", cardType);
    sqlSelect(sqlSelect);
    String lsServiceType = sqlStr("service_type");
    func.varsSet("service_type", lsServiceType);
    String supFlag = aaSupFlag[rr];
    func.varsSet("ls_batchno", lsBatchno);
    func.varsSet("li_recno", Double.toString(liRecno));
    func.varsSet("ls_risk_bank_no", lsRiskBankNo);
    func.varsSet("card_type", cardType);
    func.varsSet("unit_code", unitCode);
    func.varsSet("member_note", memberNote);
    func.varsSet("reg_bank_no", regBankNo);
    func.varsSet("change_reason", changeReason);
    func.varsSet("id_no", idNo);
    func.varsSet("id_no_code", idNoCode);
    func.varsSet("ic_flag", icFlag);
    func.varsSet("pm_id", pmId);
    func.varsSet("pm_id_code", pmIdCode);
    func.varsSet("major_card_no", majorCardNo);
    func.varsSet("major_valid_fm", majorValidFm);
    func.varsSet("major_valid_to", majorValidTo);
    func.varsSet("emboss_4th_data", emboss4thData);
    func.varsSet("corp_no", corpNo);
    func.varsSet("source_code", sourceCode);
    func.varsSet("corp_no_code", corpNoCode);
    func.varsSet("acct_type", acctType);
    func.varsSet("chi_name", chiName);
    func.varsSet("birthday", birthday);
    func.varsSet("eng_name", engName);
    func.varsSet("credit_lmt", Integer.toString(creditLmt));
    func.varsSet("old_beg_date", oldBegDate);
    func.varsSet("old_end_date", oldEndDate);
    func.varsSet("combo_indicator", comboIndicator);
    func.varsSet("ls_date_fm", lsDateFm);
    func.varsSet("ls_date_to", lsDateTo);
    func.varsSet("sup_flag", supFlag);
    func.varsSet("force_flag", forceFlag);
    if (func.insertFunc() != 1) {
      msg = "此資料無法搬到續卡檔內";
      return -1;
    }

    if (func.updateFunc1(changeReason) < 1) {
      msg = "寫入卡片檔錯誤~";
      return -1;
    }

    return 1;

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
