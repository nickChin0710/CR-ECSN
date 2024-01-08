package colm05;
/**
 * 2019-1204   JH    --init_button()
 * 2019-0521:  JH    pgm-rename
 * 109-05-06  V1.00.02  Tanwei       updated for project coding standard
 ** 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 * */
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Colp5820 extends BaseEdit {
  String paramType = "1", acctType = "", validDate = "", execMode = "2";
  Colp5820Func func;

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
      if (eqIgno(wp.respHtml, "colp5820")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr =
        " where param_type='1' and exec_mode='2'" + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
            + commSqlStr.col(wp.itemStr("ex_valid_date"), "valid_date", "like%");

    wp.whereOrder = " order by acct_type, apr_flag , valid_date ";

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
    logSql();
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

    String sql1 = " select " + " pause_flag " + " from ptr_blockparam " + " where param_type = '1' "
        + " and acct_type = ? " + " and valid_date = ? " + " and exec_mode = '1' ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "acct_type"), wp.colStr(ii, "valid_date")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "pause_flag_2", sqlStr("pause_flag"));
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    acctType = wp.itemStr("data_k2");
    validDate = wp.itemStr("data_k3");
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
        + "debt_amt34," + "debt_fee34," + "block_reason34," + "rowid," + "mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_date , " + "mod_pgm," + "mod_seqno";
    wp.daoTable = "ptr_blockparam";
    wp.whereStr = "where 1=1" + sqlCol(paramType, "param_type") + sqlCol(acctType, "acct_type")
        + sqlCol(validDate, "valid_date") + sqlCol(execMode, "exec_mode");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + paramType + ", " + acctType + "," + validDate + "," + execMode);
      return;
    }

    dataWk();

    // -read-
    dataRead2();

  }

  void dataWk() {
    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }
  }

  void dataRead2() {
    this.daoTid = "B_";
    wp.selectSQL = " " + "acct_type,   " + "valid_date, " + "apr_flag, " + "exec_mode,"
        + "exec_day," + "exec_cycle_nday," + "exec_date," + "n0_month," + "n1_cycle,"
        + "mcode_value1," + "debt_amt1," + "mcode_value2," + "debt_amt2," + "mcode_value3,"
        + "debt_amt3," + "mcode_value4," + "debt_amt4," + "pause_flag," + "exec_flag_m1,"
        + "exec_flag_m2," + "exec_flag_m3," + "debt_fee3," + "exec_flag_m14," + "mcode_value14,"
        + "debt_amt14," + "block_reason14," + "exec_flag_m24," + "mcode_value24," + "debt_amt24,"
        + "block_reason24," + "exec_flag_m34," + "mcode_value34," + "debt_amt34," + "debt_fee34,"
        + "block_reason34," + "rowid," + "mod_user," + "mod_time," + "mod_pgm," + "mod_seqno";
    wp.daoTable = "ptr_blockparam";
    wp.whereStr = "where 1=1" + sqlCol(paramType, "param_type") + sqlCol(acctType, "acct_type")
        + sqlCol(validDate, "valid_date") + sqlCol("1", "exec_mode");

    pageSelect();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Colp5820Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {}

}
