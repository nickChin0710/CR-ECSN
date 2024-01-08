/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package colq01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colq1220 extends BaseEdit {
  CommString commString = new CommString();

  String tscCardNo = "";

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
    }

    dddwSelect();
    initButton();
  }

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exCrtDateS");
    String lsDate2 = wp.itemStr("exCrtDateE");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return false;
    }

    if (empty(wp.itemStr("exIdCorpNo")) && empty(wp.itemStr("exSendYm"))) {
      alertErr2("請輸入 身分證ID/統編 或 報送年月");
      return false;
    }

    wp.whereStr = "where 1=1 ";
    if (wp.itemStr("exSendFlag").equals("0") == false) {
      if (wp.itemStr("exSendFlag").equals("Y")) {
        wp.whereStr += " and crt_901_date <> '' ";
      } else {
        wp.whereStr += " and crt_901_date = '' ";
      }
    }
    if (wp.itemStr("exFromType").equals("0") == false) {
      wp.whereStr += " and from_type = :from_type ";
      setString("from_type", wp.itemStr("exFromType"));
    }
    if (wp.itemStr("exCloseFlag").equals("0") == false) {
      wp.whereStr += " and close_flag = :close_flag ";
      setString("close_flag", wp.itemStr("exCloseFlag"));
    }
    if (empty(wp.itemStr("exIdCorpNo")) == false) {
      wp.whereStr += " and id_corp_no like :id_corp_no ";
      setString("id_corp_no", wp.itemStr("exIdCorpNo") + "%");
    }
    if (empty(wp.itemStr("exLgdReason")) == false) {
      wp.whereStr += " and lgd_reason = :lgd_reason ";
      setString("lgd_reason", wp.itemStr("exLgdReason"));
    }
    if (empty(wp.itemStr("exSendYm")) == false) {
      wp.whereStr += " and substr(crt_901_date,1,6) = :crt_901_date ";
      setString("crt_901_date", wp.itemStr("exSendYm"));
    }
    if (empty(wp.itemStr("exCrtDateS")) == false) {
      wp.whereStr += " and crt_date >= :crt_dates ";
      setString("crt_dates", wp.itemStr("exCrtDateS"));
    }
    if (empty(wp.itemStr("exCrtDateE")) == false) {
      wp.whereStr += " and crt_date <= :crt_datee ";
      setString("crt_datee", wp.itemStr("exCrtDateE"));
    }
    if (empty(wp.itemStr("exDataTable")) == false) {
      wp.whereStr += " and data_table = :data_table ";
      setString("data_table", wp.itemStr("exDataTable"));
    }

    wp.whereOrder = " order by lgd_seqno";
    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "lgd_seqno, " + "id_corp_no, " + "lgd_early_ym, " + "lgd_reason, "
    // + "deCode(lgd_reason,'A','本息延滯90日','B1','前置協商,更生清算','B2','強制停用','') AS tt_lgd_reason, "
        + "data_table, "
        // + "deCode(data_table,'acno','帳戶資料','nego','前置協商','renew','更生','liqu','清算','') AS
        // tt_data_table, "
        + "notify_date, " + "from_type, "
        // + "deCode(from_type,'1','人工','2','批次','') AS tt_from_type, "
        + "crt_date, " + "crt_901_date, " + "close_flag, "
        // + "deCode(close_flag,'Y','已結案','N','未結案','') AS tt_close_flag, "
        + "close_date, " + "risk_amt AS db_risk_amt, " + "overdue_ym AS db_overdue_ym, "
        + "overdue_amt AS db_overdue_amt, " + "coll_ym AS db_coll_ym, "
        + "coll_amt AS db_coll_amt, " + "lgd_remark ";

    wp.daoTable = "col_lgd_901";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() {
    String wkData = "";
    String[] cde = new String[] {"A", "B1", "B2"};
    String[] txt = new String[] {"本息延滯90日", "前置協商,更生清算", "強制停用"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "lgd_reason");
      wp.colSet(ii, "tt_lgd_reason", commString.decode(wkData, cde, txt));

      wkData = wp.colStr(ii, "data_table");
      wp.colSet(ii, "tt_data_table", commString.decode(wkData, ",acno,nego,renew,liqu", ",帳戶資料,前置協商,更生,清算"));

      wkData = wp.colStr(ii, "from_type");
      wp.colSet(ii, "tt_from_type", commString.decode(wkData, ",1,2", ",人工,批次"));

      wkData = wp.colStr(ii, "close_flag");
      wp.colSet(ii, "tt_close_flag", commString.decode(wkData, ",Y,N", ",已結案,未結案"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    tscCardNo = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(tscCardNo)) {
      tscCardNo = itemKk("tsc_card_no");
    }

    if (isEmpty(tscCardNo)) {
      alertErr("TSC卡號 : 不可空白");
      return;
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno, " + "tsc_card_no, " + "card_no, " + "new_beg_date, "
            + "new_end_date, " + "crt_date, " + "mod_user, " + "uf_2ymd(mod_time) as mod_date ";

    wp.daoTable = "tsc_card";
    wp.whereStr = "where 1=1" + sqlCol(tscCardNo, "tsc_card_no");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + tscCardNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    /*
     * func =new busi.col01.Colq1220_func(wp);
     * 
     * rc = func.dbSave(is_action); ddd(func.getMsg()); if (rc!=1) { err_alert(func.getMsg()); }
     * this.sql_commit(rc);
     */
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

}
