package colm05;
/** 整批強停參數覆核-主管作業
 * 2019-1204   JH    --init_button
 * 2019-0520:  JH    modify
 * 109-05-06  V1.00.03  Tanwei       updated for project coding standard
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Colp5720 extends BaseEdit {

  String acctType = "";
  String validDate = "";
  Colp5720Func func;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    msgOK();

    strAction = wp.buttonCode;
    log("action=" + strAction);
    if (wp.buttonCode.equals("X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (wp.buttonCode.equals("Q")) {
      /* 查詢功能 */
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      /* 資料讀取 */
      dataRead();
    } else if (wp.buttonCode.equals("A")) {
      /* 新增功能 */
      insertFunc();
    } else if (wp.buttonCode.equals("U")) {
      /* 更新功能 */
      updateFunc();
    } else if (wp.buttonCode.equals("D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (wp.buttonCode.equals("M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (wp.buttonCode.equals("S")) {
      /* 動態查詢 */
      querySelect();
    } else if (wp.buttonCode.equals("L")) {
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
      if (eqIgno(wp.respHtml, "colp5720")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }

    } catch (Exception ex) {
    }
  }

  @Override
  public void initButton() {

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acctType)) {
      acctType = itemKk("acct_type");
    }
    if (empty(validDate)) {
      validDate = itemKk("valid_date");
    }

    if (empty(acctType) || empty(validDate)) {
      alertErr2("[卡別, 生效日期] 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid , " + " mod_seqno, " + " valid_date ,  " + " param_type ,  "
        + " apr_flag ," + " exec_mode ,   " + " exec_day ,    " + " exec_cycle_nday , "
        + " exec_date ,   " + " acct_type ,   " + " mcode_value , " + " debt_amt ,    "
        + " n1_cycle ,    " + " pause_flag ,  " + " non_af , " + " non_ri , " + " non_pn ,"
        + " non_pf ," + " non_lf ," + " mod_user ," + " to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "ptr_stopparam";
    wp.whereStr = "where 1=1" + sqlCol("1", "param_type") + sqlCol(acctType, "acct_type")
        + sqlCol(validDate, "valid_date");
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key=" + acctType + "; " + validDate);
      return;
    }
    detlWkdata();
  }

  void detlWkdata() {
    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }
  }

  void funcItemSet() {
    if (eqAny(strAction, "A")) {
      acctType = wp.itemStr("acct_type");
      validDate = wp.itemStr("valid_date");
    } else {
      acctType = wp.itemStr("kk_acct_type");
      validDate = wp.itemStr("kk_valid_date");
    }
    func.varsSet("acct_type", acctType);
    func.varsSet("valid_date", validDate);
    func.varsSet("apr_flag", wp.itemStr("apr_flag"));
    func.varsSet("pause_flag", wp.itemStr("pause_flag"));
    func.setModxxx(this.loginUser(), "colm5720", wp.itemStr("mod_seqno"));
  }

  @Override
  public void queryFunc() throws Exception {
    String lsAcctType = wp.itemStr("ex_acct_type");
    String lsValidDate = wp.itemStr("ex_valid_date");

    wp.whereStr = "WHERE 1=1" + sqlCol(lsAcctType, "acct_type")
        + sqlCol(lsValidDate, "valid_date", "like%") + " and param_type ='1'"
        + " order by acct_type, apr_flag, valid_date DESC";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " valid_date ,  " + " param_type ,  " + " apr_flag ," + " exec_mode ,   "
        + " exec_day ,    " + " exec_cycle_nday , " + " exec_date ,   " + " acct_type ,   "
        + " mcode_value , " + " debt_amt ,    " + " n1_cycle ,    " + " pause_flag    ";
    wp.daoTable = "ptr_stopparam";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY acct_type, apr_flag, valid_date desc";
    }

    pageQuery();
    listWkdata();

    wp.setListCount(1);
    if (sqlNotFind()) {
      // wp.respHtml = errPage;
      alertErr2("此條件查無資料");
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.setPageValue();

  }

  void listWkdata() {
    String wkData = "";

    //
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "pause_flag");
      wp.colSet(ii, "tt_pause_flag", wkData);
      if (eqAny(wkData, "Y")) {
        wp.colSet(ii, "tt_pause_flag", "暫停");
      } else if (eqAny(wkData, "N")) {
        wp.colSet(ii, "tt_pause_flag", "執行中");
      }
      // --
      wkData = wp.colStr(ii, "exec_mode");
      wp.colSet(ii, "tt_exec_mode", wkData);
      if (eqAny(wkData, "2")) {
        wp.colSet(ii, "tt_exec_mode", "CYCLE後N日");
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
  public void saveFunc() throws Exception {
    func = new Colp5720Func();
    func.setConn(wp);

    // func_item_set();
    rc = func.dbUpdate();
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
  }

}
