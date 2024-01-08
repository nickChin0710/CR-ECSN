/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;

public class Ichp0110 extends BaseAction {

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
    if (chkStrend(wp.itemStr2("ex_txn_date1"), wp.itemStr2("ex_txn_date2")) == false) {
      alertErr2("交易日期:起迄錯誤");
      return;
    }

    if (chkStrend(wp.itemStr2("ex_close_date1"), wp.itemStr2("ex_close_date2")) == false) {
      alertErr2("結案日期:起迄錯誤");
      return;
    }

    String ls_where = " where 1=1 and close_date <> '' and clo_apr_date = '' "
        + sqlCol(wp.itemStr2("ex_txn_date1"), "txn_date", ">=")
        + sqlCol(wp.itemStr2("ex_txn_date2"), "txn_date", "<=")
        + sqlCol(wp.itemStr2("ex_close_date1"), "close_date", ">=")
        + sqlCol(wp.itemStr2("ex_close_date2"), "close_date", "<=")
        + sqlCol(wp.itemStr2("ex_close_user"), "close_user");
    wp.whereStr = ls_where;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " ich_card_no ," + " txnlog_seq ," + " exception_cd ," + " txn_code ,"
        + " txn_date ," + " txn_time ," + " agency_cd ," + " store_cd ," + " txn_amt ,"
        + " close_reason ,"
        + " decode(close_reason,'1','信用卡加值','2','信用卡減值','3','現金加值','4','現金減值') as tt_close_reason ,"
        + " close_remark ," + " close_user ," + " close_date ," + " clo_apr_date ,"
        + " clo_apr_user ," + " mod_seqno ," + " hex(rowid) as rowid ";

    wp.daoTable = "ich_a04b_exception";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int llCnt = 0, llOk = 0, llErr = 0;

    ichm01.Ichp0110Func func = new ichm01.Ichp0110Func();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] lsCloseReason = wp.itemBuff("close_reason");
    String[] lsModSeqno = wp.itemBuff("mod_seqno");
    wp.listCount[0] = wp.itemRows("rowid");

    int rr = 0;
    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;
      llCnt++;

      if (pos("|1|2|3|4", lsCloseReason[rr]) <= 0) {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        return;
      }

      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("mod_seqno", lsModSeqno[rr]);

      if (func.dataProc() != 1) {
        llErr++;
        dbRollback();
        wp.colSet(rr, "ok_flag", "X");
        continue;
      } else {
        llOk++;
        sqlCommit(1);
        wp.colSet(rr, "ok_flag", "V");
        continue;
      }

    }

    if (llCnt == 0) {
      errmsg("請選擇處理資料 !");
      return;
    }

    alertMsg("覆核完成 , 成功:" + llOk + " 失敗:" + llErr);

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
