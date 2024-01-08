/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-04  V1.00.00  David FU   program initial                            *
* 106-11-28  V1.00.01  Ryan       update  ptr_actgeneral_n                   *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-07-31  V1.00.02  yanghan       修改了页面覆核栏位的名称      *
******************************************************************************/

package ptrm01;


import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0142 extends BaseEdit {

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
  public void dddwSelect() {

    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("kk_acct_type");
      dddwList("dddw_card_accttype", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");
    } catch (Exception ex) {
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    // wp.whereStr =" where 1=1 ";
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    /*
     * getWhereStr(); //-page control- wp.queryWhere = wp.whereStr; wp.setQueryMode(); queryRead();
     */
  }

  @Override
  public void queryRead() throws Exception {
    /*
     * wp.pageControl();
     * 
     * wp.selectSQL = " REVOLVING_INTEREST1" + ", REVOLVING_INTEREST2" + ", REVOLVING_INTEREST3" +
     * ", REVOLVING_INTEREST4" + ", REVOLVING_INTEREST5" + ", REVOLVING_INTEREST6" +
     * ", min_percent_payment" + ", mix_mp_balance" + ", autopay_b_due_days" +
     * ", autopay_deduct_days" + ", instpay_b_due_days" + ", instpay_deduct_days" + ", mi_d_mcode" +
     * ", atm_fee" + ", m12_d_b_days" + ", non_autopay_fee" + ", rc_max_rate" + ", payment_lmt" +
     * ", apr_date" + ", apr_user" ;
     * 
     * wp.daoTable = "ptr_actgeneral"; //wp.whereOrder=" order by REVOLVING_INTEREST1";
     * getWhereStr();
     * 
     * pageQuery();
     * 
     * wp.setListCount(1); if (sql_notFind()) { alert_err(AppMsg.err_condNodata); return; }
     * 
     * wp.listCount[1] = wp.dataCnt; //wp.setPageValue();
     */
  }

  @Override
  public void querySelect() throws Exception {
    // dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", acct_type  " + ", mp_1_rate "
        + ", mp_1_bl_flag  " + ", mp_1_ca_flag  " + ", mp_1_ot_flag  " + ", mp_1_ao_flag  "
        + ", mp_1_id_flag  " + ", mp_3_rate  " + ", mp_mcode  " + ", sms_deduct_days  "
        + ", ach_days  " + ", post_o_days  " + ", delmths  " + ", rc_use_indicator  "
        + ", REVOLVING_INTEREST1 " + ", REVOLVING_INTEREST2" + ", REVOLVING_INTEREST3"
        + ", REVOLVING_INTEREST4" + ", REVOLVING_INTEREST5" + ", REVOLVING_INTEREST6"
        + ", min_percent_payment" + ", mix_mp_balance" + ", autopay_b_due_days"
        + ", autopay_deduct_days" + ", instpay_b_due_days" + ", instpay_deduct_days"
        // + ", mi_d_mcode"
        + ", atm_fee" + ", m12_d_b_days" + ", rc_max_rate" + ", payment_lmt";
    wp.daoTable = "ptr_actgeneral_n";
    wp.whereStr = "where 1=1";
    if (!empty(wp.itemStr("kk_acct_type"))) {
      wp.whereStr += " and acct_type = :kk_acct_type ";
      setString("kk_acct_type", wp.itemStr("kk_acct_type"));
    }
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料");
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0142Func func = new Ptrm0142Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    this.btnModeAud();
    // }
  }

}
