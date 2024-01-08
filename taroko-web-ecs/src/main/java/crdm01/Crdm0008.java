/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-18  V1.00.00  David FU   program initial                            *
* 108-12-13  V1.00.01  Andy Liu   add col default_place                      *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0008 extends BaseEdit {
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

    wp.selectSQL = " card_item" + ", name" + ", qty_add_percent" + ", safe_qty" + ", apr_date"
        + ", apr_user" + ", crt_date" + ", crt_user" + ", default_place "
        + ", (default_place||'_'||(select wf_desc from ptr_sys_idtab where wf_type = 'WH_LOC' and wf_id = crd_card_item.default_place )) as db_default_place ";

    wp.daoTable = "crd_card_item";
    wp.whereOrder = " order by card_item";

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
    mExCardItem = wp.itemStr("card_item");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExCardItem = wp.itemStr("kk_card_item");
    if (empty(mExCardItem)) {
      mExCardItem = itemKk("data_k1");
    }

    if (empty(mExCardItem)) {
      mExCardItem = wp.colStr("card_item");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", card_item " + ", name"
        + ", qty_add_percent" + ", safe_qty" + ", apr_date" + ", apr_user" + ", crt_date"
        + ", crt_user " + ", default_place ";
    wp.daoTable = "crd_card_item";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  card_item = :card_item ";
    setString("card_item", mExCardItem);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, card_item=" + mExCardItem);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    Crdm0008Func func = new Crdm0008Func(wp);

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
        wp.optionKey = wp.itemStr("kk_card_item");
      else
        wp.optionKey = wp.itemStr("ex_card_item");
      this.dddwList("dddw_card_item", "crd_card_item", "card_item", "name",
          "where 1=1 order by card_item");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("default_place");
      }
      this.dddwList("dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' ");
    } catch (Exception ex) {
    }
  }

}
