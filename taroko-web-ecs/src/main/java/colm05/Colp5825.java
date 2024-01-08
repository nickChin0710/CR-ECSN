package colm05;
/**
 * 2019-1204   JH    --init_button()
 * 109-05-06  V1.00.01  Tanwei       updated for project coding standard
 ** 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 * */
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Colp5825 extends BaseEdit {
  Colp5825Func func;
  String paramType = "2", acctType = "", validDate = "", execMode = "3";

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
      if (eqIgno(wp.respHtml, "colp5825")) {
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
    wp.whereStr = " where param_type='2' and exec_mode='3'"
        + commSqlStr.col(wp.itemStr("ex_acct_type"), "acct_type")
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
        + "mcode_value1," + "debt_amt2," + "mcode_value2," + "debt_amt3," + "mcode_value3,"
        + "exec_date";
    wp.daoTable = "ptr_blockparam";
    wp.whereOrder = " order by acct_type, valid_date desc";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    // list_wkdata();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    acctType = itemKk("data_k2");
    validDate = itemKk("data_k3");
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

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "param_type,   " + "valid_date, "
        + "apr_flag, " + "exec_mode," + "exec_date," + "acct_type," + "mcode_value1,"
        + "mcode_value2," + "debt_amt2," + "mcode_value3," + "debt_amt3," + "pause_flag,"
        + "to_char(mod_time,'yyyymmdd') as mod_date , " + "mod_user";
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

  }

  void dataWk() {
    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new colm05.Colp5825Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {}

}
