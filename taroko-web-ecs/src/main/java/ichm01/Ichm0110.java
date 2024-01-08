/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-02  V1.00.01  Alex       add initButton                             *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;

public class Ichm0110 extends BaseAction {

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
    if (wp.itemEmpty("ex_txn_date1") && wp.itemEmpty("ex_close_date1")) {
      alertErr2("請輸入 日期期間");
      return;
    }

    if (chkStrend(wp.itemStr2("ex_txn_date1"), wp.itemStr2("ex_txn_date2")) == false) {
      alertErr2("交易日期:起迄錯誤");
      return;
    }

    if (chkStrend(wp.itemStr2("ex_close_date1"), wp.itemStr2("ex_close_date2")) == false) {
      alertErr2("結案日期:起迄錯誤");
      return;
    }

    String lsWhere =
        " where 1=1 and clo_apr_date ='' " + sqlCol(wp.itemStr2("ex_txn_date1"), "txn_date", ">=")
            + sqlCol(wp.itemStr2("ex_txn_date2"), "txn_date", "<=")
            + sqlCol(wp.itemStr2("ex_close_date1"), "close_date", ">=")
            + sqlCol(wp.itemStr2("ex_close_date2"), "close_date", "<=")
            + sqlCol(wp.itemStr2("ex_close_user"), "close_user");

    if (wp.itemEq("ex_close_flag", "Y")) {
      lsWhere += " and close_date <> ''" + sqlCol(wp.itemStr2("ex_close_reason"), "close_reason");
    } else if (wp.itemEq("ex_close_flag", "N")) {
      lsWhere += " and close_date = ''";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " ich_card_no ," + " txnlog_seq ," + " exception_cd ," + " txn_code ,"
        + " txn_date ," + " txn_time ," + " agency_cd ," + " store_cd ," + " merchine_no ,"
        + " txn_amt ," + " auth_amt ," + " close_reason ," + " close_remark ," + " close_user ,"
        + " close_date ," + " clo_apr_date ," + " clo_apr_user ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno ," + " hex(rowid) as rowid ";

    wp.daoTable = " ich_a04b_exception ";
    wp.whereOrder = " order by txn_date Desc ";

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
    String lsModUser = "", lsModDate = "";
    ichm01.Ichm0110Func func = new ichm01.Ichm0110Func();
    func.setConn(wp);

    String[] aa_opt = wp.itemBuff("opt");
    String[] ls_rowid = wp.itemBuff("rowid");
    String[] ls_clo_apr_date = wp.itemBuff("clo_apr_date");
    String[] ls_close_reason = wp.itemBuff("close_reason");
    String[] ls_close_remark = wp.itemBuff("close_remark");
    wp.listCount[0] = wp.itemRows("rowid");

    int rr = 0;
    for (int ii = 0; ii < aa_opt.length; ii++) {
      rr = optToIndex(aa_opt[ii]);
      if (rr < 0)
        continue;
      llCnt++;

      if (!empty(ls_clo_apr_date[rr])) {
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }

      func.varsSet("rowid", ls_rowid[rr]);
      func.varsSet("close_reason", ls_close_reason[rr]);
      func.varsSet("close_remark", ls_close_remark[rr]);
      if (empty(ls_close_reason[rr])) {
        lsModUser = "";
        lsModDate = "";
      } else {
        lsModUser = wp.loginUser;
        lsModDate = getSysDate();
      }
      func.varsSet("close_user", lsModUser);
      func.varsSet("close_date", lsModDate);

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

    alertMsg("處理完成 , 成功:" + llOk + " 失敗:" + llErr);

  }

  @Override
  public void initButton() {
    btnModeAud();
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
