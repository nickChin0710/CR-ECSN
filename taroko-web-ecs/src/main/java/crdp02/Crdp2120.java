/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-25  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
* 111-12-29  V1.00.04  Wilson     crd_nccc_card => crd_card_item 	
******************************************************************************/
package crdp02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.text.SimpleDateFormat;

public class Crdp2120 extends BaseProc {
  Crdp2120Func func;
  int rr = -1;
  String msg = "";
  String datakk1 = "", datakk2 = "", liRecno = "";
  int ilOk = 0;
  int ilErr = 0;
  String[] aaModSeqno, aaTmpModSeqno, aaChangeReason, aaIdPSeqno, aaCardType, aaUnitCode,
      aaPpCardNo, aaIdNo, aaIdNoCode, aaGroupCode, aaSourceCode, aaEngName, aaCardItem, aaValidTo,
      aaValidFm, aaCurBegDate, aaCurEndDate, aaKindType, aaProcessKind;

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
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_date1");
    String lsDate2 = wp.itemStr("ex_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return -1;
    }
    wp.whereStr = " where 1=1 and c.kind_type = '120' and c.apr_user = '' ";

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
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
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
        + " c.process_kind, " + " c.change_reason, " + " c.change_status as tmp_change_status, "
        + " c.change_date, " + " c.change_reason_old, " + " c.change_status_old, "
        + " c.change_date_old, " + " c.old_end_date, " + " c.cur_end_date, " + " c.crt_user, "
        + " c.crt_date, " + " a.group_code, " + " b.chi_name, " + " c.apr_user as tmp_apr_user, "
        + " c.apr_date, " + " a.card_type, " + " a.unit_code, " + " a.source_code, "
        + " a.eng_name, " + " a.new_beg_date, " + " a.new_end_date, " + " c.cur_beg_date, "
        + " c.kind_type, " + " c.expire_chg_flag, " + " c.expire_reason, "
        + " lpad(' ',20,' ') db_expire_reason, " + " a.change_status, " + " c.mod_time, "
        + " a.valid_fm, " + " a.valid_to, " + " a.card_item, " + " a.mod_seqno, "
        + " c.id_p_seqno, " + " c.mod_seqno as tmp_mod_seqno ";
    wp.daoTable = " crd_card_pp as a join crd_idno as b on a.id_p_seqno = b.id_p_seqno "
        + " join crd_card_pp_tmp as c on a.pp_card_no = c.pp_card_no "
        + " left join crd_idno as d on c.id_p_seqno = d.id_p_seqno ";

    wp.whereOrder = "";
    if (getWhereStr() != 1)
      return;
    System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " "
        + wp.whereOrder);
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
    String lsExpireChgFlag = "", lsExpireReason = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {

      wkData = wp.colStr(ii, "process_kind");
      wp.colSet(ii, "tt_process_kind", commString.decode(wkData, ",1,2,3", ",線上續卡,取消線上續卡,系統續卡改不續卡"));

      wkData = wp.colStr(ii, "change_reason");
      wp.colSet(ii, "tt_change_reason", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      wkData = wp.colStr(ii, "tmp_change_status");
      wp.colSet(ii, "tt_tmp_change_status",
          commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));

      wkData = wp.colStr(ii, "change_reason_old");
      wp.colSet(ii, "tt_change_reason_old", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      wkData = wp.colStr(ii, "expire_chg_flag");
      wp.colSet(ii, "tt_expire_chg_flag", commString.decode(wkData, ",1,4,5", ",預約不續卡,人工不續卡,系統不續卡"));

      lsExpireChgFlag = wp.colStr(ii, "expire_chg_flag");
      lsExpireReason = wp.colStr(ii, "expire_reason");

      if (lsExpireChgFlag.equals("1")) {
        String sql9 =
            " select wf_desc from ofw_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason", lsExpireReason + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlag.equals("4")) {
        String sql9 =
            " select wf_desc from ofw_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason", lsExpireReason + sqlStr("wf_desc"));
      }
      if (lsExpireChgFlag.equals("5")) {
        String sql9 =
            " select wf_desc from ofw_idtab where wf_type = 'NOTCHG_KIND_S_P' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sql9);
        wp.colSet(ii, "db_expire_reason", lsExpireReason + sqlStr("wf_desc"));
      }

    }
  }

  int wfMoveEmbossTmp() throws Exception {
    String lsChangeReason = "", lsBatchno = "";
    int liSystemDd = 0;
    String lsDateFm = "";

    lsChangeReason = aaChangeReason[rr];
    if (lsChangeReason.equals("1")) {
      lsChangeReason = "2";// 提前續卡
    }
    lsBatchno = wp.sysDate;
    // --- 檢核抓取進來之change_status (is_change_status)
    liSystemDd = this.toInt(strMid(lsBatchno, 6, 2));

    String sql1 =
        " select max(recno)+1 as li_recno from crd_emboss_pp_tmp where batchno = :ls_batchno ";
    setString("ls_batchno", lsBatchno);
    sqlSelect(sql1);

    liRecno = sqlStr("li_recno");
    if (empty(liRecno) || liRecno.equals("0")) {
      liRecno = "1";
    }

    String sql2 = " select chi_name,birthday from crd_idno where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", aaIdPSeqno[rr]);
    sqlSelect(sql2);
    if (sqlRowNum <= 0) {
      msg = "抓取卡人檔失敗";
      return -1;
    }

    func.varsSet("batchno", lsBatchno);
    func.varsSet("recno", liRecno);
    func.varsSet("emboss_source", "4");

    String sql3 = " select card_item from crd_card_item where card_item = :ls_card_item ";
	setString("ls_card_item",aaUnitCode[rr]+aaCardType[rr]);
	sqlSelect(sql3);
    func.varsSet("card_item", sqlStr(0, "card_item"));
    if (empty(sqlStr(0, "card_item"))) {
      func.varsSet("card_item", aaCardItem[rr]);
    }

    if (liSystemDd > 25) {
      String sql4 =
          " select to_char(add_months(to_date(:ls_date_fm,'yyyymmdd'),1),'yyyymm')||'01' as ls_date_fm from dual ";
      setString("ls_date_fm", aaCurBegDate[rr]);
      sqlSelect(sql4);
      lsDateFm = sqlStr("ls_date_fm");
      if (sqlRowNum <= 0) {
        msg = "日期資料轉換錯誤 !!";
        return -1;
      }
    }
    func.varsSet("valid_fm", lsDateFm);
    func.varsSet("valid_to", aaCurEndDate[rr]);
    func.varsSet("ls_change_reason", lsChangeReason);

    rc = func.updateFunc();
    if (rc < 1) {
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

  int wfCancelChg() {

    rc = func.dbDelete();
    if (rc < 1) {
      msg = "delete crd_emboss_pp_tmp err";
      return -1;
    }
    rc = func.updateFunc2();
    if (rc < 1) {
      msg = "寫入不續卡註記失敗";
      return -1;
    }
    return 1;
  }

  int wfProcessChg() {

    rc = func.dbDelete2();
    if (rc != 1) {
      msg = "已製卡完成不在暫存檔內";
      return -1;
    }
    rc = func.updateFunc3();
    if (rc < 1) {
      msg = "寫入不續卡註記失敗";
      return -1;
    }
    return 1;
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void dataProcess() throws Exception {

    func = new Crdp2120Func(wp);

    String[] opt = wp.itemBuff("opt");
    String[] aaRowid = wp.itemBuff("rowid");
    aaModSeqno = wp.itemBuff("mod_seqno");
    aaTmpModSeqno = wp.itemBuff("tmp_mod_seqno");
    aaChangeReason = wp.itemBuff("change_reason");
    aaIdPSeqno = wp.itemBuff("id_p_seqno");
    aaCardType = wp.itemBuff("card_type");
    aaUnitCode = wp.itemBuff("unit_code");
    aaPpCardNo = wp.itemBuff("pp_card_no");
    aaIdNo = wp.itemBuff("id_no");
    aaIdNoCode = wp.itemBuff("id_no_code");
    aaGroupCode = wp.itemBuff("group_code");
    aaSourceCode = wp.itemBuff("source_code");
    aaEngName = wp.itemBuff("eng_name");
    aaCardItem = wp.itemBuff("card_item");
    aaValidTo = wp.itemBuff("valid_to");
    aaValidFm = wp.itemBuff("valid_fm");
    aaCurBegDate = wp.itemBuff("cur_beg_date");
    aaCurEndDate = wp.itemBuff("cur_end_date");
    aaKindType = wp.itemBuff("kind_type");
    aaProcessKind = wp.itemBuff("process_kind");

    wp.listCount[0] = aaRowid.length;

    // -dataProcess-
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      func.varsSet("mod_seqno", aaModSeqno[rr]);
      func.varsSet("tmp_mod_seqno", aaTmpModSeqno[rr]);
      func.varsSet("eng_name", aaEngName[rr]);
      func.varsSet("card_type", aaCardType[rr]);
      func.varsSet("unit_code", aaUnitCode[rr]);
      func.varsSet("pp_card_no", aaPpCardNo[rr]);
      func.varsSet("old_card_no", aaPpCardNo[rr]);
      func.varsSet("change_reason", aaChangeReason[rr]);
      func.varsSet("id_no", aaIdNo[rr]);
      func.varsSet("id_no_code", aaIdNoCode[rr]);
      func.varsSet("group_code", aaGroupCode[rr]);
      func.varsSet("source_code", aaSourceCode[rr]);
      func.varsSet("old_beg_date", aaValidFm[rr]);
      func.varsSet("old_end_date", aaValidTo[rr]);
      func.varsSet("kind_type", aaKindType[rr]);

      if (func.updateFunc4() < 1) {
        alertErr("Update crd_card_pp_tmp err");
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
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
      wp.colSet(rr, "ok_flag", "!");
      alertErr("處理失敗," + msg);
      return;
    } else {
      func.insertFunc2();
      if (func.dbDelete3() < 1) {
        wp.alertMesg =
            "<script language='javascript'> alert('delete crd_card_pp_tmp 錯誤 !!')</script>";
      }
      sqlCommit(1);
      queryFunc();
      errmsg("處理完成");
    }
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
