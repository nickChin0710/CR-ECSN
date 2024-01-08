package ccam01;
/* 臨時調整額度維護-依產品類別　adj_prod_parm
 * V00.0		XX		106-0803: initial
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 * */
import ofcapp.BaseAction;

public class Ccam2060 extends BaseAction {
  Ccam2060Func func;
  String rowId = "";

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
    }

  }

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_card_note"), "card_note")
        + sqlCol(wp.itemStr("ex_area_type"), "area_type")
        + sqlCol(wp.itemStr("ex_mcc_code"), "mcc_code");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "card_note," + " area_type ," + " tot_amt_month ," + " adj_eff_date1 ,"
        + " adj_eff_date2 ," + " mcc_code ," + " times_amt ," + " times_cnt ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date," + " mod_user , " + " hex(rowid) as rowid ";
    wp.daoTable = "cca_adj_prod_parm";
    wp.whereOrder = " order by mod_date Desc ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    rowId = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(rowId)) {
      rowId = wp.itemStr("rowid");
    }


    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " card_note," + " area_type ,"
        + " tot_amt_month ," + " adj_eff_date1 ," + " adj_eff_date2," + " mcc_code,"
        + " times_amt, " + " times_cnt, " + " adj_remark, "
        + " to_char(mod_time,'yyyymmdd') as mod_date, " + " mod_pgm, " + " mod_user," + " crt_user,"
        + " crt_date," + " uf_tt_mcc_code(mcc_code) as tt_mcc_code";
    wp.daoTable = "CCA_ADJ_PROD_PARM";
    wp.whereStr = " where 1=1 and rowid = ? " ;
    setRowId(rowId);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowId);
      return;
    }

    // --
    String cardNote = wp.colStr("card_note");
    if (eqIgno(cardNote, "*")) {
      wp.colSet("tt_card_note", "通用");
    } else {
      wp.colSet("tt_card_note", ecsfunc.DeCodeCrd.cardNote(cardNote));
    }

  }

  @Override
  public void saveFunc() throws Exception {
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    func = new Ccam2060Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    this.sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.saveAfter(false);
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
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("times_cnt", "100");
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccam2060")) {    
    	  wp.optionKey = wp.colStr("ex_card_note");
    	  dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
    	  wp.optionKey = wp.colStr("kk_card_note");
    	  dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
    	  wp.optionKey = wp.itemStr2("kk_mcc_code");
    	  this.dddwList("dddw_mcc_code", "cca_mcc_risk", "mcc_code", "mcc_remark", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

}
