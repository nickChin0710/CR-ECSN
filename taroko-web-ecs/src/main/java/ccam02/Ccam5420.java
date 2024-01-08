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

public class Ccam5420 extends BaseAction {
  String binType = "", eciVal = "", ucafVal = "";

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
    String isWhere =
        " where 1=1 and sys_id ='3D-WEB' " + sqlCol(wp.itemStr("ex_bin_type"), "sys_key", "like%");


    wp.whereStr = isWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " substr(sys_key,1,1) as bin_type ," + " sys_data2 as eci_val ,"
        + " sys_data3 as ucaf_val ," + " sys_data1 as tx_desc ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date," + " mod_user ";
    wp.daoTable = "cca_sys_parm2";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    pageQuery();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    binType = wp.itemStr("data_k1");
    eciVal = wp.itemStr("data_k2");
    ucafVal = wp.itemStr("data_k3");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(binType)) {
      binType = itemkk("bin_type");
    }
    if (empty(eciVal)) {
      eciVal = itemkk("eci_val");
    }
    if (empty(ucafVal)) {
      ucafVal = itemkk("ucaf_val");
    }
    String sysKey = binType + "-" + eciVal + "-" + ucafVal;

    wp.selectSQL =
        " substr(sys_key,1,1) as bin_type," + " sys_data2 as eci_val ," + " sys_data3 as ucaf_val ,"
            + " sys_data1 as tx_desc," + " to_char(mod_time,'yyyymmdd') as mod_date,"
            + " crt_user ," + " crt_date ," + " mod_seqno," + " mod_user," + " hex(rowid) as rowid";
    wp.daoTable = "cca_sys_parm2";
    wp.whereStr = "where 1=1 and sys_id ='3D-WEB'" + sqlCol(sysKey, "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + binType);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    ccam02.Ccam5420Func func = new ccam02.Ccam5420Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(true);

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
