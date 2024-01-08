/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-18  V1.00.00  David FU   program initial                            *
* 106-07-06  V1.00.01  Andy Liu   program update                             *
* 107-05-15  V1.00.02  Andy Liu   Update UI                                  *
* 108-12-30  V1.00.03  Andy Liu   Update dddw_card_item change               *
* 109-04-28  V1.00.04  YangFang   updated for project coding standard        *
* 109-07-31  V1.00.05 shiyuqi       修改登入帳號及密碼     *
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0009 extends BaseEdit {
  String mExUnitCode = "";
  String mExCardType = "";
  String mExCardItem = "";

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

  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_unit_code")) == false) {
      wp.whereStr += " and  unit_code = :unit_code ";
      setString("unit_code", wp.itemStr("ex_unit_code"));
    }
    if (empty(wp.itemStr("ex_card_type")) == false) {
      wp.whereStr += " and  card_type = :card_type ";
      setString("card_type", wp.itemStr("ex_card_type"));
    }
    if (empty(wp.itemStr("ex_card_item")) == false) {
      wp.whereStr += " and  card_item = :card_item ";
      setString("card_item", wp.itemStr("ex_card_item"));
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

    wp.selectSQL = " unit_code" + ", card_type" + ", card_item" + ", virtual_flag"
        + ", electronic_code" + ", ips_kind" + ", service_code" + ", card_code" + ", ic_flag"
        + ", ic_kind" + ", check_key_expire" + ", key_id" + ", deriv_key" + ", l_offln_lmt"
        + ", u_offln_lmt" + ", extn_year" + ", new_extn_mm" + ", reissue_extn_mm" + ", new_vendor"
        + ", mku_vendor" + ", chg_vendor" + ", apr_date" + ", apr_user" + ", crt_date"
        + ", crt_user";

    wp.daoTable = "crd_item_unit";
    // wp.whereOrder=" order by card_item";

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
    mExUnitCode = wp.itemStr("unit_code");
    mExCardType = wp.itemStr("card_type");
    mExCardItem = wp.itemStr("card_item");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExUnitCode = wp.itemStr("kk_unit_code");
    if (empty(mExUnitCode)) {
      mExUnitCode = itemKk("data_k1");
    }

    mExCardType = wp.itemStr("kk_card_type");
    if (empty(mExCardType)) {
      mExCardType = itemKk("data_k2");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", unit_code " + ", card_type"
        + ", card_item" + ", virtual_flag" + ", electronic_code" + ", card_code" + ", ips_kind"
        + ", service_code" + ", ic_flag" + ", ic_kind" + ", check_key_expire" + ", key_id"
        + ", deriv_key" + ", l_offln_lmt" + ", u_offln_lmt" + ", extn_year" + ", new_extn_mm"
        + ", reissue_extn_mm" + ", new_vendor" + ", mku_vendor" + ", chg_vendor" + ", service_id" // 20170928
        + ", issuer_configuration_id "                                                                                          // add
        + ", apr_date" + ", apr_user" + ", crt_date" + ", crt_user";
    wp.daoTable = "crd_item_unit";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  unit_code = :unit_code and card_type = :card_type ";
    setString("unit_code", mExUnitCode);
    setString("card_type", mExCardType);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, unit_code = " + mExUnitCode + " and card_type = " + mExCardType);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Crdm0009Func func = new Crdm0009Func(wp);

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
      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.itemStr("kk_card_type");
      else
        wp.optionKey = wp.itemStr("ex_card_type");
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.colStr("card_item");
      else
        wp.optionKey = wp.itemStr("ex_card_item");
      this.dddwList("dddw_card_item", "crd_card_item", "card_item", "name",
          "where 1=1 order by card_item");

      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.colStr("key_id");
      this.dddwList("dddw_key_id", "ptr_ickey", "key_id", "key_type", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.colStr("new_vendor");
      this.dddwList("dddw_new_vendor", "ptr_vendor_setting", "vendor", "vendor_name", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.colStr("mku_vendor");
      this.dddwList("dddw_mku_vendor", "ptr_vendor_setting", "vendor", "vendor_name", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.colStr("chg_vendor");
      this.dddwList("dddw_chg_vendor", "ptr_vendor_setting", "vendor", "vendor_name", "where 1=1 ");


      wp.initOption = "--";
      wp.optionKey = wp.colStr("card_code");
      this.dddwList("dddw_ich_card_parm", "ich_card_parm", "card_code", "card_name", "where 1=1 ");


    } catch (Exception ex) {
    }
  }

}
