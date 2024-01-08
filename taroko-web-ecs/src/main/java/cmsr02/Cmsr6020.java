/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-25  V1.00.1   Alex        init sysdate                              *
* 109-04-28  V1.00.02  shiyuqi       updated for project coding standard     
* 111-11-22  V1.00.01  Machao     頁面bug調整* 
******************************************************************************/
package cmsr02;

import ofcapp.BaseAction;

public class Cmsr6020 extends BaseAction {

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

    wp.selectSQL = "case_user," + " sum(decode(send_code,'Y',1,0)) as send_cnt,"
        + " sum(decode(send_code,'Y',0,1)) as no_send_cnt," + " count(*) as db_cnt  ";
    wp.daoTable = "CMS_CASEMASTER ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " group by CASE_USER ";
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
    String sql1 = "select usr_cname from sec_user where usr_id = ? ";
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "case_user")});
      if (sqlRowNum > 0)
        wp.colSet(ii, "usr_cname", sqlStr("usr_cname"));
    }
  }

  void set0() {
    wp.colSet("sum_send", "0");
    wp.colSet("sum_no_send", "0");
    wp.colSet("sum_db_cnt", "0");
  }

  void sumTotal(String lsWhere) {
    wp.selectSQL = "" + " sum(decode(send_code,'Y',1,0)) as sum_send,"
        + " sum(decode(send_code,'Y',0,1)) as sum_no_send," + " count(*) as sum_db_cnt";
    wp.daoTable = "cms_casemaster";
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
    wp.colSet("ex_cms_date_e", getSysDate());

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
