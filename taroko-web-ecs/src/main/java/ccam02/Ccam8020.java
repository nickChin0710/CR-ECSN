/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import ofcapp.BaseAction;

public class Ccam8020 extends BaseAction {
  String sysId = "", sysKey = "";

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
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_sys_id"), "sys_id");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " sys_id, " + " sys_key, " + " sys_data1, " + " sys_data2, "
        + " sys_data3, " + " mod_user, " + " to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "cca_sys_parm2";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    logSql();
    pageQuery();
    // list_wk_data();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    sysId = wp.itemStr("data_k1");
    sysKey = wp.itemStr("data_k2");
    dataRead();


  }

  @Override
  public void dataRead() throws Exception {
    if (empty(sysId)) {
      sysId = itemkk("sys_id");
    }

    if (empty(sysKey)) {
      sysKey = itemkk("sys_key");
    }

    wp.selectSQL = "" + " sys_id , " + " sys_key , " + " sys_data1 , " + " sys_data2 , "
        + " sys_data3 , " + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date , "
        + " crt_date , " + " crt_user ," + " mod_seqno," + " hex(rowid) as rowid";
    wp.daoTable = "cca_sys_parm2";
    wp.whereStr = "where 1=1" + sqlCol(sysId, "sys_id") + sqlCol(sysKey, "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + sysId);
    }

  }

  @Override
  public void saveFunc() throws Exception {
    Ccam8020Func func = new Ccam8020Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
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
    // TODO Auto-generated method stub

  }

}
