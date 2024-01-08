/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-03  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-08-04  V1.00.02  Jeff       order by                                   *
*                                                                            *
******************************************************************************/

package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0160 extends BaseEdit {
  String mExBillType = "";
  String mKkTxnCode = "";

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

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    if (wp.respHtml.indexOf("_detl") > 0) {
      if (strAction.equals("new")) {
        wp.colSet("fees_max", "999999");
      }
    }

  }

  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_bill_type")) == false) {
      wp.whereStr += " and  bill_type = :bill_type ";
      setString("bill_type", wp.itemStr("ex_bill_type"));
    }
    if (empty(wp.itemStr("ex_txn_code")) == false) {
      wp.whereStr += " and  txn_code = :txn_code ";
      setString("txn_code", wp.itemStr("ex_txn_code"));
    }
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

    wp.selectSQL = " bill_type" + ", inter_desc" + ", exter_desc" + ", txn_code" + ", acct_code"
        + ", send_m_flag" + ", send_v_flag";

    wp.daoTable = "ptr_billtype";
    wp.whereOrder = " order by bill_type,txn_code";
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

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExBillType = wp.itemStr("kk_bill_type");
    if (empty(mExBillType)) {
      mExBillType = itemKk("data_k1");
    }

    if (empty(mExBillType)) {
      mExBillType = wp.colStr("bill_type");

    }

    mKkTxnCode = wp.itemStr("kk_txn_code");
    if (empty(mKkTxnCode)) {
      mKkTxnCode = itemKk("data_k2");
    }

    if (empty(mKkTxnCode)) {
      mKkTxnCode = wp.colStr("txn_code");

    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", bill_type " + ", inter_desc"
        + ", txn_code" + ", acct_code" + ", exter_desc" + ", cash_adv_state" + ", fees_state"
        + ", fees_fix_amt" + ", fees_percent" + ", fees_min" + ", fees_max" + ", interest_mode"
        + ", adv_wkday" + ", auto_installment" + ", send_m_flag" + ", entry_acct" + ", chk_err_bill"
        + ", double_chk" + ", format_chk" + ", block_rsn_x100" + ", sign_flag" + ", send_v_flag"
        + ", send_nccc_flag";
    wp.daoTable = "ptr_billtype";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  bill_type = :bill_type ";
    setString("bill_type", mExBillType);
    wp.whereStr += " and  txn_code = :txn_code ";
    setString("txn_code", mKkTxnCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, bill_type=" + mExBillType + "  txn_code=" + mKkTxnCode);
    }
    wp.colSet("block_rsn_ct", "" + wp.colStr("block_rsn_x100").length() / 2);
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0160Func func = new Ptrm0160Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (empty(wp.colStr("fees_fix_amt"))) {
      wp.colSet("fees_fix_amt", "0");
    }

    if (empty(wp.colStr("fees_percent"))) {
      wp.colSet("fees_percent", "0");
    }

    if (empty(wp.colStr("fees_min"))) {
      wp.colSet("fees_min", "0");
    }

    if (empty(wp.colStr("fees_max"))) {
      wp.colSet("fees_max", "0");
    }


    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.itemStr("kk_bill_type");
      } else {
        wp.optionKey = wp.itemStr("ex_bill_type");
      }

      this.dddwList("dddw_bill_type", "ptr_billtype", "bill_type", "inter_desc",
          "where 1=1 group by bill_type,inter_desc order by bill_type");

      wp.initOption = "--";
      wp.optionKey = wp.colStr("acct_code");
      this.dddwList("dddw_acct_code", "ptr_actcode", "acct_code", "",
          "where 1=1 group by acct_code order by acct_code");
    } catch (Exception ex) {
    }
  }

}
