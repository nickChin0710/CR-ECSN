/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm05;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;


public class Colm5810 extends BaseEdit {
  Colm5810Func func;
  String paramType = "1", acctType = "", validDate = "", execMode = "2";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
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
    if (wp.respHtml.indexOf("_detl") > 0) {
      if (empty(strAction)) {
        wp.colSet("apr_flag", "N");
      }
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "colm5810")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "colm5810_detl")) {
        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where param_type='1' and exec_mode='2'";
    if (empty(wp.itemStr("ex_acct_type")) == false) {
      wp.whereStr += " and acct_type ='" + wp.itemStr("ex_acct_type") + "'";
    }

    if (empty(wp.itemStr("ex_valid_date")) == false) {
      wp.whereStr += " and valid_date >='" + wp.itemStr("ex_valid_date") + "'";
    }

    wp.whereOrder = " order by acct_type , valid_date ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "acct_type,   " + "valid_date, " + "apr_flag," + "pause_flag," + "exec_mode,"
        + "exec_cycle_nday," + "mcode_value1," + "debt_amt1," + "mcode_value2," + "debt_amt2,"
        + "mcode_value3," + "exec_date";
    wp.daoTable = "ptr_blockparam";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  void listWkdata() {
    String wkData = "";
    String[] cde = new String[] {"1", "2", "3", "9"};
    String[] txt = new String[] {"每月固定一天", "CYCLE後N日", "每天", "暫不執行"};

    String sql1 = " select " + " pause_flag " + " from ptr_blockparam " + " where param_type = '1' "
        + " and acct_type = ? " + " and valid_date = ? " + " and exec_mode = '1' ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "exec_mode");
      wp.colSet(ii, "tt_exec_mode", commString.decode(wkData, cde, txt));
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "acct_type"), wp.colStr(ii, "valid_date")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "pause_flag_2", sqlStr("pause_flag"));
      }
    }
  }


  @Override
  public void querySelect() throws Exception {
    // kk1=wp.item_ss("data_k1");
    acctType = wp.itemStr("data_k2");
    validDate = wp.itemStr("data_k3");
    // kk4=wp.item_ss("data_k4");

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acctType)) {
      acctType = itemKk("acct_type");
    }
    if (empty(validDate)) {
      validDate = itemKk("valid_date");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "acct_type,   " + "valid_date, "
        + "apr_flag, " + "exec_mode," + "exec_day," + "exec_cycle_nday," + "exec_date,"
        + "n0_month," + "n1_cycle," + "mcode_value1," + "debt_amt1," + "mcode_value2,"
        + "debt_amt2," + "mcode_value3," + "debt_amt3," + "mcode_value4," + "debt_amt4,"
        + "pause_flag," + "exec_flag_m1," + "exec_flag_m2," + "exec_flag_m3," + "debt_fee3,"
        + "exec_flag_m14," + "mcode_value14," + "debt_amt14," + "block_reason14," + "exec_flag_m24,"
        + "mcode_value24," + "debt_amt24," + "block_reason24," + "exec_flag_m34," + "mcode_value34,"
        + "debt_amt34," + "debt_fee34," + "block_reason34," + "rowid," + "mod_user," + "mod_time,"
        + "mod_pgm," + "mod_seqno";
    wp.daoTable = "ptr_blockparam";
    wp.whereStr = "where 1=1" + sqlCol(paramType, "param_type") + sqlCol(acctType, "acct_type")
        + sqlCol(validDate, "valid_date") + sqlCol(execMode, "exec_mode");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + paramType + ", " + acctType + "," + validDate + "," + execMode);
      return;
    }

    // -read-
    dataRead2();
    detlWk();

  }

  void detlWk() {
    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }

    if (wp.colEq("pause_flag", "Y")) {
      wp.colSet("tt_pause_flag", "執行中");
    } else if (wp.colEq("pause_flag", "N")) {
      wp.colSet("tt_pause_flag", "暫停執行");
    }

    if (wp.colEq("apr_flag", "Y")) {
      wp.colSet("tt_apr_flag", "放行");
    } else if (wp.colEq("apr_flag", "N")) {
      wp.colSet("tt_apr_flag", "取消放行");
    }

    if (wp.colEq("B_pause_flag", "Y")) {
      wp.colSet("B_tt_pause_flag", "執行中");
    } else if (wp.colEq("B_pause_flag", "N")) {
      wp.colSet("B_tt_pause_flag", "暫停執行");
    }

    if (wp.colEq("B_apr_flag", "Y")) {
      wp.colSet("B_tt_apr_flag", "放行");
    } else if (wp.colEq("B_apr_flag", "N")) {
      wp.colSet("B_tt_apr_flag", "取消放行");
    }

  }

  void dataRead2() {
    this.daoTid = "B_";
    wp.selectSQL = " A.*" + ", hex(rowid) as rowid";
    wp.daoTable = "ptr_blockparam A";
    wp.whereStr = "where 1=1" + sqlCol(paramType, "A.param_type") + sqlCol(acctType, "A.acct_type")
        + sqlCol(validDate, "A.valid_date") + sqlCol("1", "A.exec_mode");

    pageSelect();
    if (sqlRowNum <= 0)
      wp.notFound = "N";
  }


  @Override
  public void saveFunc() throws Exception {
    func = new colm05.Colm5810Func();
    func.setConn(wp);
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

}
