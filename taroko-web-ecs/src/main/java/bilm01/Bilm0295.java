/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-05  V1.00.00  David FU   program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                              *
******************************************************************************/

package bilm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0295 extends BaseEdit {
  String exTxnCode = "";


  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      // queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      queryRead();

    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      // queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      // querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      // clearFunc();
    }

    initButton();
  }

  // for query use only
  // private boolean getWhereStr() throws Exception {
  // wp.whereStr =" where 1=1 ";
  // if(empty(wp.item_ss("ex_txn_code")) == false){
  // wp.whereStr += " and fees_txn_code = :ex_txn_code ";
  // setString("ex_txn_code", wp.item_ss("ex_txn_code"));
  // }
  // return true;
  // }

  @Override
  public void queryFunc() throws Exception {}

  @Override
  public void queryRead() throws Exception {
    exTxnCode = wp.itemStr("ex_txn_code");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", merchant_no" + ", fees_bill_type"
        + ", fees_txn_code" + ", nor_fix_amt" + ", nor_percent" + ", spe_fix_amt" + ", spe_percent";
    wp.daoTable = "ptr_prepaidfee_m";
    // getWhereStr();

    wp.whereStr = "where 1=1";
    wp.whereStr += " and  fees_txn_code = :fees_txn_code ";
    setString("fees_txn_code", exTxnCode);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料");
      return;
    }
  }

  @Override
  public void querySelect() throws Exception {

    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    exTxnCode = wp.itemStr("fees_txn_code");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", merchant_no" + ", fees_bill_type"
        + ", fees_txn_code" + ", nor_fix_amt" + ", nor_percent" + ", spe_fix_amt" + ", spe_percent";
    wp.daoTable = "ptr_prepaidfee_m";
    // getWhereStr();

    wp.whereStr = "where 1=1";
    wp.whereStr += " and  fees_txn_code = :fees_txn_code ";
    setString("fees_txn_code", exTxnCode);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料");
      return;
    }
    clearNewData();

  }



  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }



    Bilm0295Func func = new Bilm0295Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    dataRead();
  }

  @Override
  public void initButton() {
    this.btnModeAud();
  }

  void clearNewData() {
    wp.colSet("new_nor_fix_amt", "0");
    wp.colSet("new_nor_percent", "0");
    wp.colSet("new_spe_fix_amt", "0");
    wp.colSet("new_spe_percent", "0");
  }

}
