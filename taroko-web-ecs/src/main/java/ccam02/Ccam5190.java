package ccam02;

/* ccam5190	帳單比對參數維護　auo7010
 * Table: cca_sys_parm1.REPORT
 * ----------------------------------------------------------------------------
 * V00.1		Alex     2019-1210:  add initButton
 * V00.0		Alex		2017-0823:
 *  V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5190 extends BaseEdit {
  Ccam5190Func func;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    rc = 1;
    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
      strAction = "U";
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
  public void initPage() {
    try {
      dataRead();
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // -amt1-
    wp.selectSQL = "sys_data1 as amt1";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("AMT1", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("amt1", "20000");
    }
    // -授權碼相符帳單比對下限百分比-

    wp.selectSQL = "sys_data1 as l_limit";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("L_LIMIT", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("l_limit", "100");
    }
    // 授權碼相符帳單比對上限百分比

    wp.selectSQL = "sys_data1 as u_limit";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("U_LIMIT", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("u_limit", "100");
    }
    // 授權碼不正確帳單比對上下限百分比

    wp.selectSQL = "sys_data1 as rate1";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("RATE1", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("rate1", "6");
    }
    // Logical delert授權交易記錄檔

    wp.selectSQL = "sys_data1 as logic_day";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("LOGIC_DAY", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("logic_day", "30");
    }
    // Physical delert授權交易記錄檔 number

    wp.selectSQL = "sys_data1 as physical";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("PHYSICAL", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("physical", "60");
    }
    // Physical delete db_log

    wp.selectSQL = "sys_data1 as db_log";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("DB_LOG", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("DB_LOG", "30");
    }
    
    //-- VD 下限百分比
    wp.selectSQL = "sys_data1 as vd_l_limit";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("VD_L_LIMIT", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("vd_l_limit", "5");
    }
    
    //-- VD 上限百分比
    wp.selectSQL = "sys_data1 as vd_u_limit";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("VD_U_LIMIT", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("vd_u_limit", "5");
    }
    
    //-- VD 天數+
    wp.selectSQL = "sys_data1 as vd_u_day";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("VD_U_DAY", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("vd_u_day", "1");
    }
    
    //-- VD 天數-
    wp.selectSQL = "sys_data1 as vd_l_day";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol("REPORT", "sys_id") + sqlCol("VD_L_DAY", "sys_key");
    pageSelect();
    if (sqlNotFind()) {
      wp.colSet("vd_l_day", "8");
    }
    
  }

  @Override
  public void saveFunc() throws Exception {
    this.updateRetrieve = true;
    if (checkApproveZz() == false) {
      return;
    }
    func = new Ccam5190Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    this.sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");
    return;
  }

}
