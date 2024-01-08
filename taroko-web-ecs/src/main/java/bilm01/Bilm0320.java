/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-08-13  V1.00.00  yash       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                               *
******************************************************************************/
package bilm01;

import ofcapp.BaseAction;

public class Bilm0320 extends BaseAction {
  Bilm0320Func func;
  String cardNo = "";

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
  public void queryFunc() throws Exception {

    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建立日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "function_code," + " bin_type , " + " card_no," + " film_no," + " purchase_date,"
        + " fraud_type," + " fraud_amt," + " mcht_category," + " mcht_eng_name," + " mcht_city,"
        + " mcht_zip," + " pos_entry_mode," + " crt_date," + " apr_flag,"
        + " hex(rowid) as rowid,mod_seqno";
    wp.daoTable = "bil_fraud_report ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " order by card_type,card_no";
    }
    // wp.whereOrder=" order by id_no Asc ";
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  void queryAfter() {
    // try {
    // if (eq_igno(wp.respHtml,"rskm1014")) {
    // wp.optionKey = wp.col_ss("ex_asig_reason");
    // dddw_list("dddw_rskid_desc2","ptr_sys_idtab"
    // ,"wf_id","wf_desc","where wf_type ='TRIAL_ASIG_REASON'");
    // }
    //
    // }
    // catch(Exception ex) {}

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    cardNo = wp.itemStr("kk_card_no");
    if (empty(cardNo)) {
      cardNo = itemkk("data_k1");

    }

    if (empty(cardNo)) {
      cardNo = wp.colStr("crt_date");

    }

    wp.selectSQL = "function_code," + " bin_type , " + " card_no," + " film_no," + " purchase_date,"
        + " fraud_type," + " fraud_amt," + " mcht_category," + " mcht_eng_name," + " mcht_city,"
        + " mcht_zip," + " pos_entry_mode," + " mod_seqno," + " crt_date," + " apr_flag,"
        + " hex(rowid) as rowid,mod_seqno ";


    wp.daoTable = "bil_fraud_report";
    wp.whereStr =
        " where 1=1" + sqlCol(wp.itemStr("kk_card_no"), "card_no") + sqlCol(cardNo, "hex(rowid)");


    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNo);
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {

    func = new Bilm0320Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());

    this.sqlCommit(rc);

    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      saveAfter(false);
    }


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    // try {
    // if (eq_igno(wp.respHtml,"rskm1014")) {
    // wp.optionKey = wp.col_ss("ex_asig_reason");
    // dddw_list("dddw_rskid_desc2","ptr_sys_idtab"
    // ,"wf_id","wf_desc","where wf_type ='TRIAL_ASIG_REASON'");
    // }
    //
    // }
    // catch(Exception ex) {}
    //
    //
    // try {
    // if (eq_igno(wp.respHtml,"rskm1014_detl")) {
    // wp.optionKey = wp.col_ss("asig_reason");
    // dddw_list("dddw_rskid_desc2","ptr_sys_idtab"
    // ,"wf_id","wf_desc","where wf_type ='TRIAL_ASIG_REASON'");
    // }
    //
    // }
    // catch(Exception ex) {}
  }



}
