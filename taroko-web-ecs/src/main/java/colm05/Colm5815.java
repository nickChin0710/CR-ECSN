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

import ofcapp.BaseAction;

public class Colm5815 extends BaseAction {
  String acctType = "", validDate = "";

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
    try {
      if (eqIgno(wp.respHtml, "colm5815")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
      if (eqIgno(wp.respHtml, "colm5815_detl")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + " and param_type = '2' " + " and exec_mode = '3' "
        + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
        + sqlCol(wp.itemStr("ex_valid_date"), "valid_date", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " param_type ," + " valid_date ," + " apr_flag ," + " exec_mode ,"
        + " exec_day ," + " exec_cycle_nday ," + " exec_date ," + " acct_type ," + " mcode_value1 ,"
        + " debt_amt1 ," + " mcode_value2 ," + " debt_amt2 ," + " mcode_value3 ," + " debt_amt3 ,"
        + " pause_flag ";
    wp.daoTable = " ptr_blockparam ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by acct_type, apr_flag, valid_date Desc ";
    logSql();
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
    acctType = wp.itemStr("data_k1");
    validDate = wp.itemStr("data_k2");

    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acctType)) {
      acctType = itemkk("acct_type");      
    }
    
    if (empty(validDate)) {
    	validDate = itemkk("valid_date");
    }
    
    if(empty(acctType)) {
    	alertErr("帳戶類別: 不可空白");
    	return ;
    }
    
    if(empty(validDate)) {
    	alertErr("生效日期: 不可空白");
    	return ;
    }
    
    wp.selectSQL = " hex(rowid) as rowid ," + " param_type ," + " acct_type ," + " valid_date ,"
        + " apr_flag ," + " exec_mode ," + " exec_day ," + " exec_cycle_nday ," + " exec_date ,"
        + " n0_month ," + " n1_cycle ," + " mcode_value1 ," + " debt_amt1 ," + " mcode_value2 ,"
        + " debt_amt2 ," + " mcode_value3 ," + " debt_amt3 ," + " mcode_value4 ," + " debt_amt4 ,"
        + " pause_flag ," + " exec_flag_m1 ," + " exec_flag_m2 ," + " exec_flag_m3 ,"
        + " debt_fee3 ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date  ";
    wp.daoTable = " ptr_blockparam ";
    wp.whereStr = " where 1=1 " + " and param_type ='2' " + " and exec_mode ='3' "
        + sqlCol(acctType, "acct_type") + sqlCol(validDate, "valid_date");
    logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, acct_type=" + acctType + " , valid_date =" + validDate);
      return;
    }

    detlWk();

  }

  void detlWk() {
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

    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }
  }

  @Override
  public void saveFunc() throws Exception {
    colm05.Colm5815Func func = new colm05.Colm5815Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    this.btnModeAud();

  }

  @Override
  public void initPage() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("apr_flag", "N");
      wp.colSet("pause_flag", "N");
    }

  }

}
