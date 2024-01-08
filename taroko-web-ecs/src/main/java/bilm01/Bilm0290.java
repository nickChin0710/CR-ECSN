/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-10  V1.00.00  yash       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                             *
******************************************************************************/

package bilm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0290 extends BaseEdit {
  String mExMerchantNo = "";

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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* mcht_no */
      strAction = "AJAX";
      processAjaxOption1();
    } else if (eqIgno(wp.buttonCode, "itemchanged")) {
      /* 清畫面 */
      strAction = "";
      itemchanged();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("dom_max_amt", "999999");
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    // if( empty(wp.item_ss("ex_merchant_no"))&&
    // empty(wp.item_ss("ex_fees_bill_type"))&&
    // empty(wp.item_ss("ex_fees_txn_code"))
    // ){
    // alert_err("至少輸入一個查詢條件");
    // return false;
    // }
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_merchant_no")) == false) {
      wp.whereStr += " and  a.merchant_no = :merchant_no ";
      setString("merchant_no", wp.itemStr("ex_merchant_no"));
    }

    if (empty(wp.itemStr("ex_fees_bill_type")) == false) {
      wp.whereStr += " and  a.fees_bill_type = :fees_bill_type ";
      setString("fees_bill_type", wp.itemStr("ex_fees_bill_type"));
    }

    if (empty(wp.itemStr("ex_fees_txn_code")) == false) {
      wp.whereStr += " and  a.fees_txn_code = :fees_txn_code ";
      setString("fees_txn_code", wp.itemStr("ex_fees_txn_code"));
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    // getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " a.merchant_no" + ", a.fees_bill_type" + ", a.fees_txn_code" + ", b.inter_desc"
    		+ ", b.exter_desc" + ", a.nor_fix_amt" + ", a.nor_percent" + ", a.spe_fix_amt" + ", a.spe_percent";

    wp.daoTable = "ptr_prepaidfee_m a "
        + "left join ptr_billtype b on a.fees_bill_type = b.bill_type and a.fees_txn_code = b.txn_code ";
    wp.whereOrder = " order by a.merchant_no";
    if (getWhereStr() != true)
      return;

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExMerchantNo = wp.itemStr("merchant_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExMerchantNo = wp.itemStr("kk_merchant_no");
    if (empty(mExMerchantNo)) {
      mExMerchantNo = itemKk("data_k1");
    }

    if (empty(mExMerchantNo)) {
      mExMerchantNo = wp.colStr("merchant_no");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", merchant_no " + ", fees_bill_type"
        + ", fees_txn_code" + ", (select exter_desc "
        + "from ptr_billtype where bill_type = ptr_prepaidfee_m.fees_bill_type "
        + "and txn_code = ptr_prepaidfee_m.fees_txn_code) exter_desc " + ", nor_amt"
        + ", nor_fix_amt" + ", nor_percent" + ", dom_fix_amt" + ", dom_percent" + ", dom_min_amt"
        + ", dom_max_amt" + ", tx_date_f" + ", tx_date_e" + ", spe_amt" + ", spe_fix_amt"
        + ", spe_percent" + ", int_fix_amt" + ", int_percent" + ", int_min_amt" + ", int_max_amt";
    wp.daoTable = "ptr_prepaidfee_m";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  merchant_no = :merchant_no ";
    setString("merchant_no", mExMerchantNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, merchant_no=" + mExMerchantNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    Bilm0290Func func = new Bilm0290Func(wp);

    // check
    if (strAction.equals("A")) {
      if (checkBilltype(wp.itemStr("fees_bill_type"), wp.itemStr("fees_txn_code")) <= 0) {

        alertErr("交易代碼不存在");
        return;
      }
    }

    // if(empty(wp.item_ss("dom_max_amt")) || empty(wp.item_ss("int_max_amt"))){
    // alert_err("收取最高金額尚未輸入!!");
    // return ;
    // }
    if (toNum(wp.itemStr("spe_amt")) > 0
        && (empty(wp.itemStr("int_max_amt")) || toNum(wp.itemStr("int_max_amt")) == 0)) {
      alertErr("特殊收取最高金額尚未輸入!!");
      return;
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
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
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("fees_bill_type");
      }

      this.dddwList("dddw_bill_type", "ptr_billtype", "bill_type", "",
          " where 1=1 group by bill_type order by bill_type");
    } catch (Exception ex) {
    }
  }

  public int checkBilltype(String feesBillType, String feesTxnCode) throws Exception {
    String lsSql =
        "select * from ptr_billtype where bill_type =:bill_type and txn_code =:txn_code ";
    setString("bill_type", feesBillType);
    setString("txn_code", feesTxnCode);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {

      return -1;
    }

    return 1;
  }

  public void processAjaxOption1() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    // String ls_sql = "select mcht_no,mcht_chi_name "
    // + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
    // + " from bil_merchant "
    // + " where mcht_status = '1' and mcht_no like :mcht_no "
    // + " order by mcht_no ";
    String lsSql = "select merchant_no " + " from ptr_prepaidfee_m "
        + " where 1=1 and merchant_no like :merchant_no " + " order by merchant_no ";

    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("merchant_no", wp.getValue("kk_merchant_no", 0) + "%");
    } else {
      setString("merchant_no", wp.getValue("ex_merchant_no", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "merchant_no"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "merchant_no"));
    }
    wp.addJSON("ex", "merchant_no");
    return;
  }

  void itemchanged() {
    String kkFeesBillType = wp.itemStr("fees_bill_type");
    String kkFeesTxnCode = wp.itemStr("fees_txn_code");
    String lsSql = "";
    if (empty(kkFeesBillType)) {
      alertMsg("請先選擇帳單來源!!");
      return;
    }
    lsSql = "select exter_desc from ptr_billtype " + "where 1=1 " + "and bill_type =:bill_type "
        + "and txn_code =:txn_code ";
    setString("bill_type", kkFeesBillType);
    setString("txn_code", kkFeesTxnCode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("exter_desc", sqlStr("exter_desc"));
    }

    return;
  }
}
