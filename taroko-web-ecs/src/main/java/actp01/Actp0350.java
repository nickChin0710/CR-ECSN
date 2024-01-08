/*
 * 2019-1218  V1.00.01  Alex  fix queryWhere
 * 109-04-21  V1.00.02  shiyuqi       updated for project coding standard     * 
 */
package actp01;

import ofcapp.BaseAction;

public class Actp0350 extends BaseAction {
  String lsAcctKey = "";

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
    try {
      if (eqIgno(wp.respHtml, "Actp0350")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    lsAcctKey = wp.itemStr("ex_acct_key");
    if (!empty(lsAcctKey)) {
      if (lsAcctKey.length() < 8) {
        errmsg("帳戶帳號至少輸入8碼");
        return;
      }
      lsAcctKey = commString.acctKey(lsAcctKey);
      if (lsAcctKey.length() != 11) {
        errmsg("帳戶帳號輸入錯誤 !");
        return;
      }
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_user"), "crt_user");

    if (!empty(lsAcctKey)) {
      lsWhere += sqlCol(wp.itemStr("ex_acct_type"), "acct_type") + sqlCol(lsAcctKey, "acct_key");
    }

    if (wp.itemEq("ex_apr_flag", "Y")) {
      lsWhere += " and uf_nvl(apr_flag,'N') = 'Y' ";
    } else {
      lsWhere += " and uf_nvl(apr_flag,'N') <> 'Y' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " p_seqno ," + " acct_type ," + " acct_key ," + " name_40 ,"
        + " curr_code ," + " autopay_acct_bank ," + " autopay_acct_no ," + " acct_status ,"
        + " stmt_cycle ," + " acct_month ," + " ttl_amt_bal ," + " autopay_bal ," + " debit_amt ,"
        + " proc_flag ," + " crt_user ," + " crt_date ," + " apr_flag ," + " apr_date ,"
        + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno ," + " memo ,"
        + " hex(rowid) as rowid ";
    wp.daoTable = "act_manu_debit";

    this.pageQuery();
    if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.colSet("cond_apr_flag", wp.itemStr("ex_apr_flag"));
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
    int llOk = 0, llErr = 0;
    String[] lsRowid = wp.itemBuff("rowid");
    String[] lsModSeqno = wp.itemBuff("mod_seqno");
    String[] lsAprFlag = wp.itemBuff("apr_flag");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("rowid");

    Actp0350Func func = new Actp0350Func();
    func.setConn(wp);

    for (int ii = 0; ii < wp.itemRows("rowid"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;

      func.varsSet("rowid", lsRowid[ii]);
      func.varsSet("apr_flag", lsAprFlag[ii]);
      func.varsSet("mod_seqno", lsModSeqno[ii]);

      if (func.dataProc() == 1) {
        llOk++;
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        llErr++;
        wp.colSet(ii, "ok_flag", "X");
        continue;
      }
    }

    if (llOk > 0) {
      sqlCommit(1);
    }

    alertMsg("執行完成 , 成功:" + llOk + " 失敗:" + llErr);

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
