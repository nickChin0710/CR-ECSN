/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-28  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0077 extends BaseEdit {
  String mExEcsPvk2 = "";

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

    wp.selectSQL = " ecs_pvk2" + ", ecs_pvk2_chk " + ", ecs_pvka_chk " + ", ecs_pvk4"
        + ", ecs_pvk4_chk" + ", ecs_pvkb_chk " + ", ecs_pvk6" + ", ecs_pvk6_chk" + ", ecs_pvkc_chk "
        + ", ecs_pvk8 " + ", ecs_pvk8_chk " + ", ecs_pvkd_chk " + ", ecs_pvk10  "
        + ", ecs_pvk10_chk " + ", ecs_pvke_chk " + ", ecs_pvk12" + ", ecs_pvk12_chk "
        + ", ecs_pvkf_chk " + ", ecs_cvk2 " + ", ecs_cvk2_chk " + ", ebk_zek " + ", ebk_zek_chk "
        + ", ebk_dek " + ", ebk_dek_chk" + ", ebk_hmack " + ", mod_time" + ", mod_user";

    wp.daoTable = "ptr_keys_table";
    wp.whereOrder = " order by ecs_pvk2";

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
    mExEcsPvk2 = wp.itemStr("ecs_pvk2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExEcsPvk2 = itemKk("data_k1");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", ecs_pvk2 " + ", ecs_pvk2_chk "
        + ", ecs_pvka_chk " + ", ecs_pvk4" + ", ecs_pvk4_chk" + ", ecs_pvkb_chk " + ", ecs_pvk6"
        + ", ecs_pvk6_chk" + ", ecs_pvkc_chk " + ", ecs_pvk8 " + ", ecs_pvk8_chk "
        + ", ecs_pvkd_chk " + ", ecs_pvk10  " + ", ecs_pvk10_chk " + ", ecs_pvke_chk "
        + ", ecs_pvk12" + ", ecs_pvk12_chk " + ", ecs_pvkf_chk " + ", ecs_cvk2 " + ", ecs_cvk2_chk "
        + ", ebk_zek " + ", ebk_zek_chk " + ", ebk_dek " + ", ebk_dek_chk" + ", ebk_hmack "
        + ", mod_time" + ", mod_user";
    wp.daoTable = "ptr_keys_table";
    wp.whereStr = "where 1=1";


    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, ecs_pvk2=" + mExEcsPvk2);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    // if (!check_approve(wp.item_ss("apr_user"), wp.item_ss("apr_passwd")))
    // {
    // return;
    // }

    Ptrm0077Func func = new Ptrm0077Func(wp);

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
