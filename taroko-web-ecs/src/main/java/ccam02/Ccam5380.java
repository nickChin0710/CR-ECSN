package ccam02;

/* DEBIT卡風險類別限額限次維護　Debit_Parm2
 * Table: cca_debit_parm2
 * ----------------------------------------------------------------------
 * V00.00	Alex	2017-08xx
 * V00.01   Alex  2018-0824
V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5380 extends BaseEdit {
  Ccam5380Func func;
  String riskType = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    rc = 1;
    wp.logActive();

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
      if (eqIgno(wp.respHtml, "ccam5380_detl")) {
        wp.optionKey = wp.colStr(0, "kk_risk_type");
        dddwList("dddw_risk_type", "Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }

      if (eqIgno(wp.respHtml, "ccam5380")) {
        wp.optionKey = wp.colStr(0, "ex_risk_type");
        dddwList("dddw_risk_type", "Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "where 1=1" + sqlCol(wp.itemStr("ex_risk_type"), "risk_type");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "risk_type,   " + wp.sqlID + "uf_tt_risk_type(risk_type) as tt_risk_type, " + "cnt_amt,"
            + "day_amt," + "day_cnt," + "month_amt," + "month_cnt," + sqlModDate + ", mod_user";
    wp.daoTable = "cca_debit_parm2";
    wp.whereOrder = " order by risk_type";
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    riskType = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(riskType)) {
      riskType = itemKk("risk_type");
    }
    wp.selectSQL = this.sqlRowid + ", mod_seqno, " + "risk_type, " + wp.sqlID
        + "uf_tt_risk_type(risk_type) as tt_risk_type, " + "cnt_amt, " + "day_amt, " + "day_cnt, "
        + "month_amt, " + "month_cnt, " + "crt_date," + "mod_user, " + sqlModDate + ", crt_user";
    wp.daoTable = "cca_debit_parm2";
    wp.whereStr = "where 1=1" + sqlCol(riskType, "risk_type");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + riskType);
    }

  }

  @Override
  public void saveFunc() throws Exception {
    if (this.checkApproveZz() == false) {
      return;
    }

    func = new Ccam5380Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    this.sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
