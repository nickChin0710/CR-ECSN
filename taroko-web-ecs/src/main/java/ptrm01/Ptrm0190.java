/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-04  V1.00.00  yash       program initial                            *
* 109-01-09  V1.00.01  Ru         add crd_makecard_fee                       *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 111-12-28  V1.00.03  Ryan       新增rcrate_day欄位 ,新增日利率計算邏輯                                      *
* 112-02-06  V1.00.04  Ryan       新增ibank_apply_flag欄位                                      *
******************************************************************************/
 
package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon; 

public class Ptrm0190 extends BaseEdit {
  String mExGroupCode = "";
  String mExGroupAbbrCode = "";

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


  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_group_code")) == false) {
      wp.whereStr += " and  group_code = :group_code ";
      setString("group_code", wp.itemStr("ex_group_code"));
    }
    if (empty(wp.itemStr("ex_group_abbr_code")) == false) {
      wp.whereStr += " and  group_abbr_code = :group_abbr_code ";
      setString("group_abbr_code", wp.itemStr("ex_group_abbr_code"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();// 執行SQL查詢
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " group_code" + ", group_abbr_code" + ", group_name" + ", group_order"
        + ", co_member_flag" + ", emboss_data" + ", auto_installment" + ", bill_form" + ", crt_date"
        + ", crt_user" + ", special_card_rate_flag" + ", revolve_int_rate_year" 
        + ", decode(purchase_card_flag,'N','',purchase_card_flag) as purchase_card_flag ,member_flag,member_corp_no"
        + ",ibank_apply_flag";

    wp.daoTable = "ptr_group_code";
    wp.whereOrder = " order by group_code";
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
    mExGroupCode = wp.itemStr("group_code");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExGroupCode = wp.itemStr("kk_group_code");
    if (empty(mExGroupCode)) {
      mExGroupCode = itemKk("data_k1");
    }

    if (empty(mExGroupCode)) {
      mExGroupCode = wp.colStr("group_code");
    }

 
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", group_code " + ", group_abbr_code"
        + ", group_name" + ", group_order" + ", combo_indicator" + ", co_member_flag"
        + ", emboss_data" + ", auto_installment" + ", co_member_type" + ", bill_form_seq"
        + ", assign_installment" + ", crt_date" + ", crt_user" + ", special_card_rate_flag"
        + ", revolve_int_rate_year" + ", crd_makecard_fee_flag" + ", crd_makecard_fee"
        + ", cca_group_mcht_chk " + ", purchase_card_flag ,rcrate_day,member_flag,member_corp_no"
        + ", ibank_apply_flag ";
    wp.daoTable = "ptr_group_code";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  group_code = :group_code ";
    setString("group_code", mExGroupCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, group_code=" + mExGroupCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    Ptrm0190Func func = new Ptrm0190Func(wp);

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

      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_group_code");
      this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "",
          "where 1=1 group by group_code order by group_code");
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("co_member_type");
        this.dddwList("dddw_bill_member", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where  wf_type='MEMBER' order by wf_id");
        wp.optionKey = wp.colStr("member_corp_no");
        this.dddwList("dddw_mkt_member", "mkt_member", "member_corp_no", "member_name",
            "where 1=1 ");
      }
    } catch (Exception ex) {
    }
  }

}
