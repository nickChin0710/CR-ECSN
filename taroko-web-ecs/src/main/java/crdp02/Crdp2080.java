/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-20  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdp02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdp2080 extends BaseProc {
  Crdp2080Func func;
  int rr = -1;
  String msg = "";
  String dataKK1 = "", dataKK2 = "", lsBatchno = "", liRecno = "";
  int ilOk = 0;
  int ilErr = 0;
  String[] aaTmpChangeStatus, aaPpCardNo, aaNewEndDate, aaIdNo, aaIdPSeqno,
      aaIdNoCode, aaGroupCode, aaCardType, aaUnitCode, aaCardItem, aaSourceCode,
      aaEngName, aaValidTo, aaValidFm, aaModSeqno, aaTmpModSeqno, aaExpireChgFlag,
      aaExpireReason, aaProcessKind, aaKindType;
  SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

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

  }

  @Override
  public void dddwSelect() {
    try {

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_aprid");
      dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_cname", " where 1=1 ");

    } catch (Exception ex) {
    }
  }

  // for query use only
  private void getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_date1");
    String lsDate2 = wp.itemStr("ex_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return;
    }
    wp.whereStr = " where 1=1 and c.kind_type = '080' and c.apr_user = '' ";

    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and c.crt_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and c.crt_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }
    if (empty(wp.itemStr("ex_aprid")) == false) {
      wp.whereStr += " and c.crt_user = :ex_aprid ";
      setString("ex_aprid", wp.itemStr("ex_aprid"));
    }
    switch (wp.itemStr("ex_crdtype")) {
      case "1":
        wp.whereStr += " and c.process_kind = '1' ";
        break;
      case "2":
        wp.whereStr += " and c.process_kind = '2' ";
        break;
      case "3":
        wp.whereStr += " and c.process_kind = '3' ";
        break;
      case "4":
        wp.whereStr += " and c.process_kind = '4' ";
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

    wp.selectSQL = " hex(c.rowid) as rowid, " + " c.pp_card_no, " + " c.id_p_seqno, "
        + " d.id_no_code, " + " d.id_no, " + " d.id_no||'_'||d.id_no_code as wk_id, "
        + " c.process_kind, " + " c.old_end_date, " + " c.cur_end_date, " + " c.crt_user, "
        + " c.crt_date, " + " a.group_code, " + " b.chi_name, " + " a.card_type, "
        + " a.source_code, " + " a.new_end_date, " + " c.cur_beg_date, " + " c.expire_reason, "
        + " c.expire_chg_flag, " + " c.expire_chg_date, " + " lpad(' ',20,' ') db_expire_reason, "
        + " c.change_status as tmp_change_status, " + " c.kind_type, " + " c.expire_chg_flag_old, "
        + " c.expire_reason_old, " + " c.change_reason as tmp_change_reason, "
        + " lpad(' ',20,' ') db_expire_reason_old, " + " a.change_reason, " + " a.change_status, "
        + " a.new_beg_date, " + " a.unit_code, " + " a.eng_name, " + " a.valid_to, "
        + " a.card_item, " + " a.valid_fm, " + " c.mod_time, " + " c.apr_user, " + " c.apr_date, "
        + " a.mod_seqno, " + " c.mod_seqno as tmp_mod_seqno ";
    wp.daoTable = " crd_card_pp as a join crd_idno as b on a.id_p_seqno = b.id_p_seqno "
        + " join crd_card_pp_tmp as c on a.pp_card_no = c.pp_card_no "
        + " left join crd_idno as d on c.id_p_seqno = d.id_p_seqno ";

    wp.whereOrder = "";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // wp.col_set("exCnt", int_2Str(wp.selectCnt));
    listWkdata();

  }

  void listWkdata() throws Exception {
    String wkData = "";
    String lsExpireChgFlag = "", lsExpireChgFlagOld = "", lsExpireReason = "",
        lsExpireReasonOld = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {

      wkData = wp.colStr(ii, "process_kind");
      wp.colSet(ii, "tt_process_kind",
          commString.decode(wkData, ",0,1,4,2,3", ",取消不續卡(放行前),預約不續卡,人工不續卡,取消不續卡(放行後),系統不續卡改續卡"));

      wkData = wp.colStr(ii, "expire_chg_flag");
      wp.colSet(ii, "tt_expire_chg_flag",
          commString.decode(wkData, ",1,4,2,3,5", ",預約不續卡,人工不續卡,取消不續卡,系統不續卡改續卡,系統不續卡"));

      wkData = wp.colStr(ii, "change_reason");
      wp.colSet(ii, "tt_change_reason", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      wkData = wp.colStr(ii, "change_status");
      wp.colSet(ii, "tt_change_status", commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));

      wkData = wp.colStr(ii, "expire_chg_flag_old");
      wp.colSet(ii, "tt_expire_chg_flag_old", commString.decode(wkData, ",1,4,5", ",預約不續卡,人工不續卡,系統不續卡"));

      lsExpireChgFlag = wp.colStr(ii, "expire_chg_flag");
      lsExpireChgFlagOld = wp.colStr(ii, "expire_chg_flag_old");
      lsExpireReason = wp.colStr(ii, "expire_reason");
      lsExpireReasonOld = wp.colStr(ii, "expire_reason_old");

      if (lsExpireChgFlag.equals("1")) {
        String sql9 =
            " select wf_desc from PTR_SYS_IDTAB where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason", lsExpireReason + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlag.equals("4")) {
        String sql9 =
            " select wf_desc from PTR_SYS_IDTAB where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason", lsExpireReason + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlag.equals("5")) {
        String sql9 =
            " select wf_desc from PTR_SYS_IDTAB where wf_type = 'NOTCHG_KIND_S_P' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason", lsExpireReason + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlagOld.equals("1")) {
        String sql9 =
            " select wf_desc from PTR_SYS_IDTAB where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason_old ";
        setString("ls_expire_reason_old", lsExpireReasonOld);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason_old", lsExpireReasonOld + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlagOld.equals("4")) {
        String sql9 =
            " select wf_desc from PTR_SYS_IDTAB where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason_old ";
        setString("ls_expire_reason_old", lsExpireReasonOld);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason_old", lsExpireReasonOld + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlagOld.equals("5")) {
        String sql9 =
            " select wf_desc from PTR_SYS_IDTAB where wf_type = 'NOTCHG_KIND_S_P' and wf_id = :ls_expire_reason_old ";
        setString("ls_expire_reason_old", lsExpireReasonOld);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason_old", lsExpireReasonOld + sqlStr("wf_desc"));
      }
    }
  }

  int wfMoveEmbossTmp() throws Exception {

    String lsChk = "0";
    if (aaTmpChangeStatus[rr].equals("1")) {
      msg = "續卡製卡中，不可改為不續卡";
      return -1;
    }
    if (aaTmpChangeStatus[rr].equals("2")) {
      msg = "此卡片送製卡中,不可再做提前續卡";
      return -1;
    }
    String sql1 =
        " select count(*) as li_cnt from crd_emboss_pp_tmp where old_card_no = :ls_cardno ";
    setString("ls_cardno", aaPpCardNo[rr]);
    sqlSelect(sql1);
    if (this.toInt(sqlStr("li_cnt")) > 0) {
      msg = "續卡製卡中，不可改為不續卡 2";
      return -1;
    }
    // -- 改為抓自己之效期最展期
    if (!empty(aaNewEndDate[rr])) {
      Calendar cal = Calendar.getInstance();
      Date date = format.parse(aaNewEndDate[rr]);
      cal.setTime(date);
      cal.add(Calendar.MARCH, -6);
      lsChk = format.format(cal.getTime());
    }
    if (this.toInt(lsChk) > this.toInt(wp.sysDate)) {
      msg = "效期需在系統日六個月內";
      return -1;
    }

    String lsBatchno1 = strMid(getSysDate(), 2, 6);
    String sql2 =
        " select max(batchno) as ls_batchno from crd_emboss_pp_tmp where substr(batchno,1,6) = :ls_batchno1 ";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(sql2);
    lsBatchno = sqlStr("ls_batchno");
    if (empty(lsBatchno)) {
      lsBatchno = lsBatchno1 + "01";
    } else {
      String sql3 =
          " select max(recno)+1 as li_recno from crd_emboss_pp_tmp where batchno = :ls_batchno ";
      setString("ls_batchno", lsBatchno);
      sqlSelect(sql3);
    }
    liRecno = sqlStr("li_recno");
    if (empty(liRecno) || liRecno.equals("0")) {
      liRecno = "1";
    }

    String sql4 = " select chi_name,birthday from crd_idno where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", aaIdPSeqno[rr]);
    sqlSelect(sql4);
    if (sqlRowNum <= 0) {
      msg = "抓取卡人檔失敗";
      return -1;
    }
    // -- 抓取展期年
    int liExtn = wfGetExtnYear(aaUnitCode[rr], aaCardType[rr]);

    // -- 寫入crd_emboss_pp_tmp
    func.varsSet("batchno", lsBatchno);
    func.varsSet("recno", liRecno);
    func.varsSet("emboss_source", "4");
    func.varsSet("card_type", aaCardType[rr]);
    func.varsSet("unit_code", aaUnitCode[rr]);

    String sql10 =
        " select card_item from crd_nccc_card where card_type = :ls_card_type and unit_code = :ls_unit_code ";
    setString("id_p_seqno", aaIdPSeqno[rr]);
    sqlSelect(sql10);
    func.varsSet("card_item", sqlStr(0, "card_item"));
    if (empty(sqlStr(0, "card_item"))) {
      func.varsSet("card_item", aaCardItem[rr]);
    }
    func.varsSet("change_reason", "1");
    func.varsSet("pp_card_no", aaPpCardNo[rr]);
    func.varsSet("old_card_no", aaPpCardNo[rr]);
    func.varsSet("id_no", aaIdNo[rr]);
    func.varsSet("id_no_code", aaIdNoCode[rr]);
    func.varsSet("mod_seqno", aaModSeqno[rr]);
    func.varsSet("tmp_mod_seqno", aaTmpModSeqno[rr]);
    // --- 附卡寫入正卡資料
    func.varsSet("group_code", aaGroupCode[rr]);
    func.varsSet("source_code", aaSourceCode[rr]);
    func.varsSet("eng_name", aaEngName[rr]);
    String lsEndVal = "";
    String lsBegVal = strMid(getSysDate(), 0, 6) + "01";
    if (!empty(aaValidTo[rr])) {
      Calendar cal2 = Calendar.getInstance();
      Date date2 = format.parse(aaValidTo[rr]);
      cal2.setTime(date2);
      cal2.add(Calendar.YEAR, liExtn);
      lsEndVal = format.format(cal2.getTime());
    }
    func.varsSet("valid_fm", lsBegVal);
    func.varsSet("valid_to", lsEndVal);
    func.varsSet("old_beg_date", aaValidFm[rr]);
    func.varsSet("old_end_date", aaValidTo[rr]);
    rc = func.updateFunc();
    if (rc != 1) {
      msg = "寫入卡片檔錯誤~";
      return -1;
    }
    rc = func.dataProc();
    if (rc != 1) {
      msg = "此資料無法搬到續卡檔內";
      return -1;
    }
    return 1;
  }

  int wfUpdCrdCardPp() {
    if (aaTmpChangeStatus[rr].equals("2")) {
      msg = "此卡片已送製卡,不可作任何異動";
      return -1;
    }
    if (aaTmpChangeStatus[rr].equals("1")) {
      rc = func.dbDelete();
      if (rc != 1) {
        msg = "續卡製卡中，不可改為不續卡";
        return -1;
      }
    }
    func.varsSet("expire_chg_flag", aaExpireChgFlag[rr]);
    func.varsSet("expire_reason", aaExpireReason[rr]);
    func.varsSet("pp_card_no", aaPpCardNo[rr]);
    func.varsSet("mod_seqno", aaModSeqno[rr]);

    rc = func.updateFunc2();
    if (rc != 1) {
      msg = "寫入不續卡註記失敗";
      return -1;
    }
    return 1;
  }

  int wfCancelRxpire() throws Exception {
    func.varsSet("pp_card_no", aaPpCardNo[rr]);
    func.varsSet("mod_seqno", aaModSeqno[rr]);
    String sql7 =
        " select expire_chg_flag,change_status from crd_card_pp where pp_card_no = :ls_pp_card_no ";
    setString("ls_pp_card_no", aaPpCardNo[rr]);
    sqlSelect(sql7);

    if (sqlStr("change_status").equals("2")) {
      msg = "此卡片已送製卡,不可作任何異動";
      return -1;
    }
    if (empty(sqlStr("expire_chg_flag"))) {
      msg = "此筆資料本身並無不續卡註記";
      return -1;
    }
    if (sqlStr("change_status").equals("1")) {
      rc = func.dbDelete2();
      if (rc != 1) {
        msg = "此卡片已送製卡,不可做預約不續卡";
        return -1;
      }
    }
    rc = func.updateFunc3();
    if (rc != 1) {
      msg = "取消不續卡註記失敗";
      return -1;
    }
    return 1;
  }

  int wfGetExtnYear(String asUnitCode, String asCardType) throws Exception {
    String lsKey = "0000";

    if (!empty(asUnitCode)) {
      lsKey = asUnitCode;
    }
    String sql5 =
        " select extn_year from crd_item_unit where unit_code = :as_unit_code and card_type = :as_card_type ";
    setString("as_unit_code", lsKey);
    setString("as_card_type", asCardType);
    sqlSelect(sql5);
    int liYear = this.toInt(sqlStr("EXTN_YEAR"));
    if (sqlRowNum <= 0) {
      msg = "抓取不到展期年~";
      return -1;
    }
    return liYear;
  }

  int wfDelNotchg() throws Exception {
    // --- combo卡在取消預約不續卡時需檢核是否已存在crd_notchg
    // --- 若存在則需刪除
    func.varsSet("pp_card_no", aaPpCardNo[rr]);
    String sql8 = " select hex(rowid) as rowid from crd_notchg where pp_card_no = :ls_pp_card_no ";
    setString("ls_pp_card_no", aaPpCardNo[rr]);
    sqlSelect(sql8);
    if (sqlRowNum > 0) {
      rc = func.dbDelete3();
      if (rc != 1) {
        msg = "無法刪除combo卡不續卡資料";
        return -1;
      }
    }
    return 1;
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    func = new Crdp2080Func(wp);

    String[] opt = wp.itemBuff("opt");
    String[] aaRowid = wp.itemBuff("rowid");
    aaTmpChangeStatus = wp.itemBuff("tmp_change_status");
    aaPpCardNo = wp.itemBuff("pp_card_no");
    aaNewEndDate = wp.itemBuff("new_end_date");
    aaIdNo = wp.itemBuff("id_no");
    aaIdPSeqno = wp.itemBuff("id_p_seqno");
    aaIdNoCode = wp.itemBuff("id_no_code");
    aaGroupCode = wp.itemBuff("group_code");
    aaCardType = wp.itemBuff("card_type");
    aaUnitCode = wp.itemBuff("unit_code");
    aaCardItem = wp.itemBuff("card_item");
    aaSourceCode = wp.itemBuff("source_code");
    aaEngName = wp.itemBuff("eng_name");
    aaValidTo = wp.itemBuff("valid_to");
    aaValidFm = wp.itemBuff("valid_fm");
    aaModSeqno = wp.itemBuff("mod_seqno");
    aaTmpModSeqno = wp.itemBuff("tmp_mod_seqno");
    aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    aaExpireReason = wp.itemBuff("expire_reason");
    aaProcessKind = wp.itemBuff("process_kind");
    aaKindType = wp.itemBuff("kind_type");
    wp.listCount[0] = aaRowid.length;

    // -dataProcess-
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      func.varsSet("pp_card_no", aaPpCardNo[rr]);
      func.varsSet("kind_type", aaKindType[rr]);
      func.varsSet("tmp_mod_seqno", aaTmpModSeqno[rr]);
      if (func.updateFunc4() != 1) {
        wp.colSet(rr, "ok_flag", "!");
        alertErr("update crd_card_pp_tmp err");
        sqlCommit(0);
        return;
      }
      switch (aaProcessKind[rr]) {
        case "1":
          if (wfUpdCrdCardPp() != 1) {
            ilErr++;
          }
          break;
        case "2":
          if (wfCancelRxpire() != 1) {
            ilErr++;
          }
          break;
        case "3":
          if (wfMoveEmbossTmp() != 1) {
            ilErr++;
          }
          break;
        case "4":
          if (wfUpdCrdCardPp() != 1) {
            ilErr++;
          }
          break;
      }
      if (ilErr > 0) {
        alertMsg("處理失敗," + msg);
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      }
    }
    if (func.insertFunc2() != 1) {
      wp.alertMesg =
          "<script language='javascript'> alert('insert crd_card_pp_tmp_h err')</script>";
    }
    if (func.dbDelete4() != 1) {
      wp.alertMesg = "<script language='javascript'> alert('delete crd_card_pp_tmp err')</script>";
    }
    sqlCommit(1);
    queryFunc();
    errmsg("處理完成");

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
