/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-24  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzstr                     *
* 107-05-28  V1.00.02  Andy		  update : UI,Dbug                           *
* 107-08-03  V1.00.03  Andy		  update : Dbug                              *
*  109-04-22  V1.00.04  yanghan  修改了變量名稱和方法名稱*
* 110-10-19  V1.00.05  YangBo	  joint sql replace to parameters way        *
* 112-02-02  V1.00.06  Wilson     移除card_no <> ''                           *
******************************************************************************/

package dbcp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbcp0045 extends BaseEdit {
  String mExBatchno = "";
  String mExEmbossSource = "";
  String mExEmbossReason = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    String batchno1 = wp.itemStr("ex_batchno1");
    String batchno2 = wp.itemStr("ex_batchno2");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String embossSource = wp.itemStr("ex_emboss_source");
    String reason = wp.itemStr("ex_reason");
    String keyin = wp.itemStr("ex_keyin");
    String lsSql = "", nextBatchno = "";
    lsSql = "select to_char(sysdate+1,'yymmdd') as ls_next_batchno " + "from dual";
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      nextBatchno = sqlStr("ls_next_batchno") + "01";
    } else {
      alertErr("無法取得ls_next_batchno");
    }

    wp.whereStr = " where 1=1  ";
    // 固定條件
//    wp.whereStr += "and card_no <> '' ";
    // 自定條件
    if (empty(batchno1)) {
//      wp.whereStr += "and batchno < ? ";
//      setString(nextBatchno);
    } else {
      wp.whereStr += sqlStrend(batchno1, batchno2, "batchno");
    }
    wp.whereStr += sqlStrend(exDateS, exDateE, "crt_date");
    wp.whereStr += sqlCol(embossSource, "emboss_source");
    wp.whereStr += sqlCol(reason, "emboss_reason");
    wp.whereStr += sqlCol(keyin, "crt_user");

    return true;
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
    String exOrder = wp.itemStr("ex_order");
    wp.selectSQL = "hex (rowid) AS rowid, mod_seqno, " + "batchno, " + "recno, "
        + "(batchno || '_' || recno) db_batchno, " + "emboss_source, " + "emboss_reason, "
        + "card_type, " + "unit_code, " + "card_no, " + "apply_id, " + "apply_id_code, "
        + "(apply_id || '_' || apply_id_code) db_apply_id, " + "pm_id, " + "pm_id_code, "
        + "(pm_id || '_' || pm_id_code) db_pm_id, " + "group_code, " + "corp_no, "
        + "corp_no_code, " + "decode(corp_no,'','',(corp_no || '_' || corp_no_code)) db_corp_no, "
        + "chi_name, " + "eng_name, " + "birthday, " + "valid_fm, " + "valid_to, " + "resend_note, "
        + "to_nccc_code, " + "acct_type, " + "acct_key, " + "member_note, " + "old_card_no, "
        + "reason_code, " + "change_reason, " + "status_code, " + "major_card_no, "
        + "source_code, " + "error_code, " + "decode (confirm_date,'','N','Y') db_mark, "
        + "confirm_date, " + "confirm_user, " + "apr_date, " + "apr_user, " + "emboss_date, "
        + "third_rsn ";
    wp.daoTable = " dbc_emboss_tmp ";
    if (empty(exOrder)) {
      wp.whereOrder += "order by batchno,recno,card_no ";
    } else {
      wp.whereOrder += " order by :ex_order";
      setString("ex_order", exOrder);
    }

    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowct = 0;
    String embossSource = "", wpEmbossReason = "";
    String wpConfirmDate = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowct += 1;
      // emboss_source製卡來源中文
      embossSource = wp.colStr(ii, "emboss_source");
      String[] cde = new String[] {"1", "2", "3", "4", "5", "7"};
      String[] txt = new String[] {"新製卡", "普昇金卡", "整批續卡", "線上續卡", "重製", "緊急補發"};
      wp.colSet(ii, "db_emboss_source", commString.decode(embossSource, cde, txt));

      // emboss_reason製卡原因
      wpEmbossReason = wp.colStr(ii, "emboss_reason");
      String[] cde1 = new String[] {"0", "1", "2", "3", "4"};
      String[] txt1 = new String[] {"", "掛失", "損毀", "偽卡", "星座卡重製"};
      wp.colSet(ii, "db_emboss_reason", commString.decode(wpEmbossReason, cde1, txt1));
      //
      wpConfirmDate = wp.colStr(ii, "confirm_date");
      if (!empty(wpConfirmDate)) {
        wp.colSet(ii, "set_opt", "checked");
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    String[] rowid = wp.itemBuff("rowid");
    String[] modSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");

    String[] batchno = wp.itemBuff("batchno");
    String[] aaEmbossSource = wp.itemBuff("emboss_source");
    String[] aaEmbossReason = wp.itemBuff("emboss_reason");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaCardType = wp.itemBuff("card_type");
    String[] aaChiName = wp.itemBuff("chi_name");
    String[] birthday = wp.itemBuff("birthday");
    String[] aaEngName = wp.itemBuff("eng_name");
    String[] validFm = wp.itemBuff("valid_fm");
    String[] validTo = wp.itemBuff("valid_to");
    String[] groupCode = wp.itemBuff("group_code");
    String[] dbMark = wp.itemBuff("db_mark");
    String[] aaConfirmDate = wp.itemBuff("confirm_date");
    String[] confirmUser = wp.itemBuff("confirm_user");
    String[] embossDate = wp.itemBuff("emboss_date");

    wp.listCount[0] = batchno.length;
    String confirmDate = "", wpConfirmUser = "";
    String usSql = "";
    // check && update
    // int rr = -1;
    int ll_ok = 0, ll_err = 0, rr = 0;
    String mRowid = "", mModSeqno = "";
    for (int ii = 0; ii < batchno.length; ii++) {
      if (checkBoxOptOn(ii, opt)) {
        if (empty(aaConfirmDate[ii]) == false) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "此批號已放行,不可再做放行");
          wp.colSet(ii, "set_opt", "checked");
          ll_err++;
          sqlCommit(0);
          continue;
        }
        confirmDate = getSysDate();
        wpConfirmUser = wp.loginUser;
        wp.colSet(ii, "set_opt", "checked");
      }
      if (!checkBoxOptOn(ii, opt)) {
        if (empty(aaConfirmDate[ii]) == false) {
          if (empty(embossDate[ii]) == false) {
            wp.colSet(ii, "ok_flag", "X");
            wp.colSet(ii, "err_msg", "此批號已送製卡,不可做未放行");
            ll_err++;
            sqlCommit(0);
            continue;
          }
        }
        confirmDate = "";
        wpConfirmUser = "";
      }
      // update
      mRowid = rowid[ii];
      mModSeqno = modSeqno[ii];
      usSql = "update dbc_emboss_tmp set " + "confirm_date = :confirm_date, "
          + "confirm_user = :confirm_user, " + "mod_user = :mod_user, " + "mod_time = sysdate, "
          + "mod_pgm = 'dbcp0055', " + "mod_seqno = nvl(mod_seqno,0) + 1 "
          + " where 1=1 and hex(rowid) = :db_rowid and mod_seqno = :mod_seqno ";
      setString("confirm_date", confirmDate);
      setString("confirm_user", wpConfirmUser);
      setString("mod_user", wp.loginUser);
      setString("db_rowid", mRowid);
      setString("mod_seqno", mModSeqno);
//      usSql += "where 1=1 ";
//      usSql += sqlCol(mRowid, "hex(rowid)");
//      usSql += sqlCol(, "mod_seqno");
      sqlExec(usSql);
      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "處理失敗!!");
        sqlCommit(0);
        ll_err++;

      } else {
        ll_ok++;
        wp.colSet(ii, "ok_flag", "V");
        sqlCommit(1);
      }
    }
    alertMsg("覆核處理成功!!");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_keyin
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_keyin");
      dddwList("dddw_keyin", "sec_user", "usr_id", "usr_cname", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}
