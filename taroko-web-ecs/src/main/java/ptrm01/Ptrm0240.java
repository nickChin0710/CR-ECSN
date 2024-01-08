/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-02  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/

package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0240 extends BaseEdit {
  String mExBillUnit = "";

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

  // for query use only
  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_bill_unit")) == false) {
      wp.whereStr += " and  bill_unit = :bill_unit ";
      setString("bill_unit", wp.itemStr("ex_bill_unit"));
    }
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

    wp.selectSQL = " bill_unit" + ", short_title" + ", describe" + ", conf_flag" + ", indelv_mx"
        + ", model1_mx" + ", crt_date" + ", crt_user";

    wp.daoTable = "ptr_billunit";
    wp.whereOrder = " order by bill_unit";

    getWhereStr();

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
    mExBillUnit = wp.itemStr("bill_unit");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExBillUnit = wp.itemStr("kk_bill_unit");
    if (empty(mExBillUnit)) {
      mExBillUnit = itemKk("data_k1");
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", bill_unit " + ", short_title" + ", describe"
            + ", conf_flag" + ", indelv_mx" + ", model1_mx" + ", crt_date" + ", crt_user";
    wp.daoTable = "ptr_billunit";
    wp.whereStr = "where 1=1 and bill_unit = :bill_unit ";
    setString("bill_unit", mExBillUnit);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, bill_unit=" + mExBillUnit);
    }
  }

  @Override
  public void saveFunc() throws Exception {


    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    Ptrm0240Func func = new Ptrm0240Func(wp);

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
        // wp.initOption="--";
        // wp.optionKey = wp.item_ss("kk_curr_code");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_bill_unit");
      }


      this.dddwList("dddw_bill_unit", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");
    } catch (Exception ex) {
    }
  }

}
