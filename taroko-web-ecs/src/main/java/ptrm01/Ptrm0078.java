/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-27  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0078 extends BaseEdit {
  String mExNetZmk1 = "";

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

    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 ";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " net_zmk1" + ", net_zmk1_chk " + ", net_zmk2" + ", net_zmk2_chk" + ", net_zpk1"
        + ", net_zpk1_chk" + ", mod_time" + ", mod_user";

    wp.daoTable = "ptr_keys_table";
    wp.whereOrder = " order by net_zmk1";

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
    mExNetZmk1 = wp.itemStr("net_zmk1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExNetZmk1 = itemKk("data_k1");

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", net_zmk1 " + ", net_zmk1_chk " + ", net_zmk2"
            + ", net_zmk2_chk" + ", net_zpk1" + ", net_zpk1_chk" + ", mod_time" + ", mod_user";
    wp.daoTable = "ptr_keys_table";
    wp.whereStr = "where 1=1";


    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, net_zmk1=" + mExNetZmk1);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    // if (!check_approve(wp.item_ss("apr_user"), wp.item_ss("apr_passwd")))
    // {
    // return;
    // }

    Ptrm0078Func func = new Ptrm0078Func(wp);

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
  public void initPage() {
    try {
      queryRead();
    } catch (Exception ex) {
    }
  }

}
