/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-22  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                   *
* 112-02-02  V1.00.04  Wilson     移除card_no != ''                            *  
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Crdp0105 extends BaseProc {
  Crdp0105Func func;
  int rr = -1;
  String msg = "";
  //String kk1 = "";
  int ilOk = 0;
  int ilErr = 0;

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
      // wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_crt_user");
      dddwList("dddw_keyin", "sec_user", "usr_id", "usr_cname", "where 1=1 ");
      wp.optionKey = wp.itemStr("ex_card_type");
      dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 order by card_type");
      wp.optionKey = wp.itemStr("ex_group_code");
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 order by group_code");
    } catch (Exception ex) {
    }
  }

  private void getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");
    String sql = "select to_char(sysdate,'YYYYMMDD') as ls_next_batchno FROM DUAL";
    sqlSelect(sql);

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[產生日期-起迄]  輸入錯誤");
      return;
    }

    lsDate1 = wp.itemStr("ex_batchno1");
    lsDate2 = wp.itemStr("ex_batchno2");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[批號-起迄]  輸入錯誤");
      return;
    }

    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("ex_crt_date1")) == false) {
      wp.whereStr += " and crt_date >= :ex_crt_date1 ";
      setString("ex_crt_date1", wp.itemStr("ex_crt_date1"));
    }
    if (empty(wp.itemStr("ex_crt_date2")) == false) {
      wp.whereStr += " and crt_date <= :ex_crt_date2 ";
      setString("ex_crt_date2", wp.itemStr("ex_crt_date2"));
    }

    if (empty(wp.itemStr("ex_batchno1")) == false) {
      wp.whereStr += " and batchno >= :ex_batchno1 ";
      setString("ex_batchno1", wp.itemStr("ex_batchno1"));
    }
    if (empty(wp.itemStr("ex_batchno2")) == false) {
      wp.whereStr += " and batchno <= :ex_batchno2 ";
      setString("ex_batchno2", wp.itemStr("ex_batchno2"));
    }

    if (empty(wp.itemStr("ex_emboss_source")) == false) {
      wp.whereStr += " and emboss_source = :ex_emboss_source ";
      setString("ex_emboss_source", wp.itemStr("ex_emboss_source"));
    }
    if (empty(wp.itemStr("ex_emboss_reason")) == false) {
      wp.whereStr += " and emboss_reason = :ex_emboss_reason ";
      setString("ex_emboss_reason", wp.itemStr("ex_emboss_reason"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and card_no = :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no"));
    }
    if (empty(wp.itemStr("ex_group_code")) == false) {
      wp.whereStr += " and group_code = :ex_group_code ";
      setString("ex_group_code", wp.itemStr("ex_group_code"));
    }
    if (empty(wp.itemStr("ex_card_type")) == false) {
      wp.whereStr += " and card_type = :ex_card_type ";
      setString("ex_card_type", wp.itemStr("ex_card_type"));
    }
  }

  @Override
  public void queryFunc() throws Exception {

    getWhereStr();
    // wp.whereStr +=" and card_no is not null or card_no <>'' ";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  void listWkdata() {
    String emboss = "";
    String[] cde = new String[] {"1", "2", "3", "4", "5", "7"};
    String[] txt = new String[] {"新製卡", "普昇金卡", "整批續卡 ", "線上續卡", "重製", "緊急補發卡"};
    String[] cde2 = new String[] {"0", "1", "2", "3", "4"};
    String[] txt2 = new String[] {"", "掛失", "毀損", "偽卡", "星座卡重製"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {

      emboss = wp.colStr(ii, "emboss_source");
      wp.colSet(ii, "tt_emboss_source", commString.decode(emboss, cde, txt));

      emboss = wp.colStr(ii, "emboss_reason");
      wp.colSet(ii, "tt_emboss_reason", commString.decode(emboss, cde2, txt2));

      if (empty(wp.colStr(ii, "confirm_date")) == false)
        wp.colSet(ii, "check", "checked");
    }

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid, " + " batchno||'-'||recno as wk_batchno, " + " batchno, "
        + " recno, " + " emboss_source, " + " emboss_reason, " + " card_no, " + " old_card_no, "
        + " card_type, "
        + " apply_id||'-'||decode(apply_id_code,'','0',apply_id_code) as wk_applyid, "
        + " corp_no as wk_corpno, " + " chi_name, " + " birthday, " + " eng_name, " + " valid_fm, "
        + " valid_to, " + " pm_id||'-'||decode(pm_id_code,'','0',pm_id_code) as wk_pmid, "
        + " group_code, " + " mod_seqno, " + " apr_date, " + " confirm_date, " + " emboss_date, "
        + " crt_date, " + " mno_id," + " msisdn," + " service_type," + " se_id," + " unit_code ";

    wp.daoTable = "crd_emboss_tmp";

    wp.whereOrder = " order by batchno,recno,card_no ";
    if (wp.itemStr("card_flag").equals("Y")) {
      wp.whereOrder = " order by card_no,batchno,recno ";
    }
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    wp.colSet("exCnt", intToStr(wp.selectCnt));
    listWkdata();
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
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
    func = new Crdp0105Func(wp);

    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaBatchno = wp.itemBuff("batchno");
    String[] aaRecno = wp.itemBuff("recno");
    String[] opt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaCrtDate = wp.itemBuff("crt_date");
    wp.listCount[0] = aaRowid.length;

    // check
    for (int ii = 0; ii < aaRowid.length; ii++) {
      if (checkBoxOptOn(ii, opt)) {
        if (empty(wp.colStr(rr, "apr_date")) == false) {
          msg = "此批號已放行,不可再做放行";
          ilErr++;
        }
        if (empty(wp.colStr(rr, "emboss_date")) == false) {
          msg = "此批號已送製卡,不可做未放行";
          ilErr++;
        }
      }
      if (ilErr < 0) {
        wp.colSet(rr, "ok_flag", "!");
        alertMsg("資料存檔處理失敗," + msg);
        return;
      }
    }
    // -update-

    for (int ii = 0; ii < aaRowid.length; ii++) {
      func.varsSet("aa_batchno", aaBatchno[ii]);
      func.varsSet("aa_recno", aaRecno[ii]);
      func.varsSet("aa_rowid", aaRowid[ii]);
      func.varsSet("aa_crt_date", aaCrtDate[ii]);
      func.varsSet("aa_mod_seqno", aaModSeqno[ii]);

      // -option-ON-
      if (checkBoxOptOn(ii, opt)) {
        if (func.updateFunc() != 1) {
          ilErr++;
          break;
        }
      } else {
        if (func.updateFunc2() != 1) {
          ilErr++;
          break;
        }
      }
      if (ilErr < 0) {
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        alertMsg("update crd_emboss_tmp err");
        return;
      }
    }
    sqlCommit(1);
    alertMsg("資料存檔處理完成!");
    queryFunc();
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
