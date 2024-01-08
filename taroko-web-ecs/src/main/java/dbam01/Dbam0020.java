/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package dbam01;

import ofcapp.BaseAction;

public class Dbam0020 extends BaseAction {
  String txnCode = "";

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
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_txn_code"), "txn_code");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " txn_code , " + " txn_desc , " + " summary_code , " + " comment_code , "
        + " decode(comment_code,'1','交易日+特店名稱','2','交易日+備註','') as comment_code_exp , "
        + " txn_comment ";
    wp.daoTable = "dbp_txn_code";
    wp.whereOrder = " order by txn_code ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    txnCode = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(txnCode))
      txnCode = itemkk("txn_code");

    if (empty(txnCode)) {
      alertErr2("交易代碼不可空白");
      return;
    }

    wp.selectSQL = " txn_code , " + " txn_desc , " + " summary_code , " + " summary_desc , "
        + " comment_code , "
        + " decode(comment_code,'1','交易日+特店名稱','2','交易日+備註','') as comment_code_exp , "
        + " txn_comment , " + " crt_user , " + " crt_date , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " hex(rowid) as rowid ";
    wp.daoTable = "dbp_txn_code";
    wp.whereStr = "where 1=1 " + sqlCol(txnCode, "txn_code");
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {
    dbam01.Dbam0020Func func = new dbam01.Dbam0020Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "dbam0020_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
