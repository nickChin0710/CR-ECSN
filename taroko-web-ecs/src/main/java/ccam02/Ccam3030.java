/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam3030 extends BaseEdit {
Ccam3030Func func;
String mchtRiskCode = "";

@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("resp_code");
        dddwList("dddw_resp_code", "cca_resp_code", "resp_code", "resp_remark", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_risk_code"), "A.mcht_risk_code", "like%")
        + sqlCol(wp.itemStr("ex_resp_code"), "A.resp_code");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + "A.mcht_risk_code, " + "A.resp_code, " + "A.risk_remark ,"
        + "uf_tt_resp_code(A.resp_code) as resp_desc ";
    wp.daoTable = "cca_mcht_risk_level A";
    wp.whereOrder = " order by A.mcht_risk_code ";
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
    mchtRiskCode = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mchtRiskCode)) {
      mchtRiskCode = itemKk("mcht_risk_code");
    }
    if (empty(mchtRiskCode)) {
      alertErr2("特店風險等級: 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "mcht_risk_code,   " + "resp_code, "
        + "risk_remark, " + "mod_user," + "to_char(mod_time ,'yyyymmdd') as mod_date," + "crt_user,"
        + "crt_date," + "mod_pgm";
    wp.daoTable = " CCA_MCHT_RISK_LEVEL";
    wp.whereStr = " where 1=1" + sqlCol(mchtRiskCode, "mcht_risk_code");

    this.logSql();

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + mchtRiskCode);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ccam3030Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);

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
}
