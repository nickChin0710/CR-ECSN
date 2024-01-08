/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-04-17  V1.00.01  Alex       新增VD悠遊卡問題交易結案程式                                                         *
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;

public class Tscm2270 extends BaseAction {
  String rowid = "";

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_tran_date1")) && empty(wp.itemStr("ex_tran_date2"))
        && empty(wp.itemStr("ex_close_date1")) && empty(wp.itemStr("ex_close_date2"))) {
      errmsg("交易日期, 結案日期: 不可同時空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_tran_date1"), wp.itemStr("ex_tran_date2")) == false) {
      alertErr2("交易日期起迄：輸入錯誤");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_close_date1"), wp.itemStr("ex_close_date2")) == false) {
      alertErr2("結案日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_tran_date1"), "tran_date", ">=")
        + sqlCol(wp.itemStr("ex_tran_date2"), "tran_date", "<=")
        + sqlCol(wp.itemStr("ex_close_date1"), "close_date", ">=")
        + sqlCol(wp.itemStr("ex_close_date2"), "close_date", "<=")
        + sqlCol(wp.itemStr("ex_close_user"), "close_user");

    if (wp.itemEq("ex_close_flag", "Y")) {
      lsWhere += " and close_date<>'' ";
      lsWhere += sqlCol(wp.itemStr("ex_close_reason"), "close_reason");
    } else if (wp.itemEq("ex_close_flag", "N")) {
      lsWhere += " and close_date='' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " crt_date ," + " crt_time ," + " tsc_card_no ," + " tran_date ,"
        + " tran_time ," + " tran_amt ," + " traff_code ," + " place_code ," + " traff_subname ,"
        + " place_subname ," + " close_reason ," + " close_remark ," + " close_user ,"
        + " close_date ," + " apr_flag ," + " apr_date ," + " apr_user ," + " hex(rowid) as rowid ";
    wp.daoTable = "tsc_dcpr_log ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by crt_date,crt_time ";
    logSql();
    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_traff_code_name",
          wp.colStr(ii, "traff_code") + "_" + wp.colStr(ii, "traff_subname"));
      wp.colSet(ii, "wk_place_name",
          wp.colStr(ii, "place_code") + "_" + wp.colStr(ii, "place_subname"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
	if(empty(rowid))
		rowid = wp.itemStr("rowid");    
    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " tsc_card_no , " + " tran_date , "
        + " tran_time , " + " tran_amt , " + " close_reason , " + " close_remark , "
        + " traff_code , " + " traff_subname , " + " place_code , " + " place_subname , "
        + " traff_code||'_'||traff_subname as wk_traff_code_name , "
        + " place_code||'_'||place_subname as wk_place_name , " + " close_user , "
        + " close_date , " + " apr_date , " + " apr_user ," + " crt_time , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "tsc_dcpr_log";
    wp.whereStr = " where 1=1 and rowid = ? ";
    setRowId(rowid);
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {
    tscm01.Tscm2270Func func = new tscm01.Tscm2270Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "tscm2270_detl")) {
      btnModeAud("XX");
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
