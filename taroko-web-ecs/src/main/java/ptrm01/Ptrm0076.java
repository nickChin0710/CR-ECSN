/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-27  V1.00.00  yash       program initial                            *
* 109-04-19  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0076 extends BaseEdit {
  String mExEcsPvk1 = "";

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

    wp.selectSQL = " ecs_pvk1" + ", ecs_pvk1_chk " + ", ecs_pvk3" + ", ecs_pvk3_chk" + ", ecs_pvk5"
        + ", ecs_pvk5_chk" + ", ecs_pvk7 " + ", ecs_pvk7_chk " + ", ecs_pvk9  " + ", ecs_pvk9_chk "
        + ", ecs_pvk11" + ", ecs_pvk11_chk " + ", racal_ip_addr " + ", racal_port" + ", ecs_cvk1 "
        + ", ecs_cvk1_chk " + ", ecs_csck1" + ", mob_zek_kek1 " + ", mob_zek_kek1_chk"
        + ", mob_zek_dek1" + ", mob_zek_dek1_chk" + ", mob_ip_addr " + ", mob_ip_port"
        + ", mob_version_id" + ", acs_zek_kek1 " + ", acs_zek_kek1_chk" + ", acs_ip_addr"
        + ", acs_ip_port" + ", acs_version_id " + ", mod_time" + ", mod_user";

    wp.daoTable = "ptr_keys_table";
    wp.whereOrder = " order by ecs_pvk1";

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
    mExEcsPvk1 = wp.itemStr("ecs_pvk1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExEcsPvk1 = itemKk("data_k1");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", ecs_pvk1 " + ", ecs_pvk1_chk "
        + ", ecs_pvk3" + ", ecs_pvk3_chk" + ", ecs_pvk5" + ", ecs_pvk5_chk" + ", ecs_pvk7 "
        + ", ecs_pvk7_chk " + ", ecs_pvk9  " + ", ecs_pvk9_chk " + ", ecs_pvk11"
        + ", ecs_pvk11_chk " + ", racal_ip_addr " + ", racal_port" + ", ecs_cvk1 "
        + ", ecs_cvk1_chk " + ", ecs_csck1" + ", mob_zek_kek1 " + ", mob_zek_kek1_chk"
        + ", mob_zek_dek1" + ", mob_zek_dek1_chk" + ", mob_ip_addr " + ", mob_ip_port"
        + ", mob_version_id" + ", acs_zek_kek1 " + ", acs_zek_kek1_chk" + ", acs_ip_addr"
        + ", acs_ip_port" + ", acs_version_id " + ", mod_time" + ", mod_user";
    wp.daoTable = "ptr_keys_table";
    wp.whereStr = "where 1=1";

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, ecs_pvk1=" + mExEcsPvk1);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    // if (!check_approve(wp.item_ss("apr_user"), wp.item_ss("apr_passwd")))
    // {
    // return;
    // }

    Ptrm0076Func func = new Ptrm0076Func(wp);

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
