package ccam02;
/* 風險分類維護　risk_type
 * Table: Vcca_risk_type
 * -----------------------------------------------------------------------
 * V00.00	Alex		2017-05xx
 * V00.01   yanghan  2020-0420: 修改了變量名稱和方法名稱
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5010 extends BaseEdit {
  Ccam5010Func func;

  String riskType = "";

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
    }
    // else if (eq_igno(wp.buttonCode, "U2")) {
    // /* 更新功能2 */
    // ii_confirm =1;
    // wp.buttonCode ="U";
    // is_action ="U";
    // updateFunc();
    // }
    else if (eqIgno(wp.buttonCode, "U")) {
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
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("ex_risk_type"), "risk_type");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " risk_type ," + " risk_desc ," + " crt_date ," + " crt_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " mod_user ," + " apr_date ,"
        + " apr_user , " + " uf_nvl(high_risk_flag,'N') as high_risk_flag ";
    wp.daoTable = "Vcca_risk_type";
    wp.whereOrder = " order by risk_type";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
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

    if (isEmpty(riskType)) {
      alertErr("風險類別 : 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "risk_type,   " + "risk_desc, " + "uf_nvl(high_risk_flag,'N') as high_risk_flag, "
        + "crt_date, " + "mod_user, " + "uf_2ymd(mod_time) as mod_date " + ", crt_user";
    wp.daoTable = "Vcca_risk_type";
    wp.whereStr = "where 1=1" + sqlCol(riskType, "risk_type");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + riskType);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    if (this.checkApproveZz() == false)
      return;

    func = new Ccam5010Func(wp);

    rc = func.dbSave(strAction);
    // ddd(func.getMsg());
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
