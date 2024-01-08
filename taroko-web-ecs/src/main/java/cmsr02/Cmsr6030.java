/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-25  V1.00.01  Alex       dept_name fix                              *
* 109-04-28  V1.00.02  shiyuqi       updated for project coding standard     *
* 109-07-27  V1.00.03   JustinWu change cms_proc_dept into ptr_dept_code
* 111-11-22  V1.00.01  Machao     頁面bug調整
******************************************************************************/
package cmsr02;

import ofcapp.BaseAction;

public class Cmsr6030 extends BaseAction {

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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {

    if (this.chkStrend(wp.itemStr("ex_cms_date_s"), wp.itemStr("ex_cms_date_e")) == false) {
      alertErr2("統計期間起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_cms_date_s"), "case_date", ">=")
        + sqlCol(wp.itemStr("ex_cms_date_e"), "case_date", "<=");

    sqlParm.setSqlParmNoClear(true);
    sumTotal(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
    sqlParm.clear();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "proc_deptno," + " sum(decode(proc_result,'9',1,0)) as db_finish_cnt,"
        + " sum(decode(proc_result,'9',0,1)) as db_no_finish_cnt, " + " count(*) as db_count  ";
    wp.daoTable = "cms_casedetail ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " group by proc_deptno ";
    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      set0();
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();


  }

  void queryAfter() {
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;
    String sql1 = "select dept_name from ptr_dept_code where dept_code = ?";
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "proc_deptno")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "dept_name", sqlStr("dept_name"));
      }
    }
  }

  void set0() {
    wp.colSet("sum_db_count", "0");
    wp.colSet("sum_db_finish_cnt", "0");
    wp.colSet("sum_db_no_finish_cnt", "0");
  }

  void sumTotal(String lsWhere) {
    wp.selectSQL = "" + " sum(decode(proc_result,'9',1,0)) as sum_db_finish_cnt,"
        + " sum(decode(proc_result,'9',0,1)) as sum_db_no_finish_cnt, "
        + " count(*) as sum_db_count  ";
    wp.daoTable = "cms_casedetail";
    wp.whereStr = lsWhere;
    pageSelect();
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsr6010")) {
        wp.optionKey = wp.colStr("ex_casetype");
        dddwList("d_dddw_casetype", "CMS_CASETYPE", "case_id", "case_desc", "where 1=1");
      }

    } catch (Exception ex) {
    }

  }

}
