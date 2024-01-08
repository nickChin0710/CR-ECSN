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

public class Ptrm0235 extends BaseEdit {
  String mExCardProperty = "";
  String mExCurrCode = "";

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
    if (empty(wp.itemStr("ex_card_property")) == false) {
      wp.whereStr += " and  card_property = :card_property ";
      setString("card_property", wp.itemStr("ex_card_property"));
    }
    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  curr_code = :curr_code ";
      setString("curr_code", wp.itemStr("ex_curr_code"));
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

    wp.selectSQL = " card_property" + ", curr_code"
        + ", (select curr_eng_name from ptr_currcode where ptr_currcode.curr_code=ptr_foreign_fee.curr_code) as curr_code_name"
        + ", v_both_diff_rate" + ", v_country_diff_rate" + ", v_currency_diff_rate"
        + ", m_both_diff_rate" + ", m_country_diff_rate" + ", m_currency_diff_rate"
        + ", j_both_diff_rate" + ", j_country_diff_rate" + ", j_currency_diff_rate" + ", apr_date"
        + ", apr_user" + ", crt_date" + ", crt_user" + ", mod_time" + ", mod_user";

    wp.daoTable = "ptr_foreign_fee";
    // wp.whereOrder=" order by card_property";

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
    mExCardProperty = wp.itemStr("card_property");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExCardProperty = wp.itemStr("kk_card_property");
    if (empty(mExCardProperty)) {
      mExCardProperty = itemKk("data_k1");
    }

    mExCurrCode = wp.itemStr("kk_curr_code");
    if (empty(mExCurrCode)) {
      mExCurrCode = itemKk("data_k2");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", card_property " + ", curr_code"
        + ", v_both_diff_rate" + ", v_country_diff_rate" + ", v_currency_diff_rate"
        + ", m_both_diff_rate" + ", m_country_diff_rate" + ", m_currency_diff_rate"
        + ", j_both_diff_rate" + ", j_country_diff_rate" + ", j_currency_diff_rate" + ", apr_date"
        + ", apr_user" + ", crt_date" + ", crt_user" + ", mod_time" + ", mod_user";
    wp.daoTable = "ptr_foreign_fee";
    wp.whereStr = "where 1=1 and card_property = :card_property and curr_code = :curr_code";
    setString("card_property", mExCardProperty);
    setString("curr_code", mExCurrCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, card_property=" + mExCardProperty + " curr_code=" + mExCurrCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0235Func func = new Ptrm0235Func(wp);

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
        wp.optionKey = wp.itemStr("kk_curr_code");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_curr_code");
      }

      this.dddwList("dddw_curr_code", "ptr_currcode", "curr_code", "curr_eng_name",
          "where 1=1 order by curr_code");
    } catch (Exception ex) {
    }
  }

}
