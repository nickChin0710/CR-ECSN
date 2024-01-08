/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-25  V1.00.1   Alex        dddw not in 99999                         *
* 109-04-28  V1.00.02  shiyuqi       updated for project coding standard    
* 111-11-08  V1.00.03  machao      頁面bug調整* 
******************************************************************************/
package cmsr02;

import ofcapp.BaseAction;

public class Cmsr6010 extends BaseAction {

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
        + sqlCol(wp.itemStr("ex_cms_date_e"), "case_date", "<=")
        + sqlCol(wp.itemStr("ex_user"), "case_user", "like%");
    String lsType = "";
    if (wp.itemEmpty("ex_casetype1") == false) {
//      lsType += " and (case_type = '" + wp.itemStr2("ex_casetype1") + "'";
      lsType += sqlCol(wp.itemStr2("ex_casetype1")," case_type ",">=");
    }
    if (wp.itemEmpty("ex_casetype2") == false) {
      if (empty(lsType)) {
//        lsType += " and (case_type = '" + wp.itemStr2("ex_casetype2") + "'";
    	  lsType += sqlCol(wp.itemStr2("ex_casetype2")," case_type " ,">=");
      }
      else
//        lsType += " or case_type = '" + wp.itemStr2("ex_casetype2") + "'";
    	  lsType += sqlCol(wp.itemStr2("ex_casetype2")," case_type " , "<=");
    }
    if (empty(lsType) == false) {
//      lsType += ")";
      lsWhere += lsType;
    }

    if (wp.itemEq("ex_type_a", "") && wp.itemEq("ex_type_b", "") && wp.itemEq("ex_type_c", "")
        && wp.itemEq("ex_type_d", "") && wp.itemEq("ex_type_e", "") && wp.itemEq("ex_type_f", "")
        && wp.itemEq("ex_type_g", "") && wp.itemEq("ex_type_h", "") && wp.itemEq("ex_type_i", "")
        && wp.itemEq("ex_type_z", "")) {

    } else {
      lsWhere += " and (1=2";

      if (wp.itemEq("ex_type_a", "Y"))
        lsWhere += " or case_type like 'A%' ";

      if (wp.itemEq("ex_type_b", "Y"))
        lsWhere += " or case_type like 'B%' ";

      if (wp.itemEq("ex_type_c", "Y"))
        lsWhere += " or case_type like 'C%' ";

      if (wp.itemEq("ex_type_d", "Y"))
        lsWhere += " or case_type like 'D%' ";

      if (wp.itemEq("ex_type_e", "Y"))
        lsWhere += " or case_type like 'E%' ";

      if (wp.itemEq("ex_type_f", "Y"))
        lsWhere += " or case_type like 'F%' ";

      if (wp.itemEq("ex_type_g", "Y"))
        lsWhere += " or case_type like 'G%' ";

      if (wp.itemEq("ex_type_h", "Y"))
        lsWhere += " or case_type like 'H%' ";

      if (wp.itemEq("ex_type_i", "Y"))
        lsWhere += " or case_type like 'I%' ";

      if (wp.itemEq("ex_type_z", "Y"))
        lsWhere += " or case_type like 'Z%' ";

      lsWhere += " )";
    }
    
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

    wp.selectSQL = "case_type," + " count(*) as db_count ";
    wp.daoTable = "cms_casemaster ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " group by case_type order by case_type ";
    logSql();
    pageQuery();
    sqlParm.clear();
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
    String sql1 = "select case_desc from CMS_CASETYPE where case_id = ? ";
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "case_type")});
      if (sqlRowNum > 0)
        wp.colSet(ii, "case_desc", sqlStr("case_desc"));
    }
  }

  void set0() {
    wp.colSet("sum_total", "0");
  }

  void sumTotal(String lsWhere) {
    wp.selectSQL = "" + " count(*) as sum_total";
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
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsr6010")) {
        wp.optionKey = wp.colStr("ex_casetype1");
        dddwList("d_dddw_casetype1", "CMS_CASETYPE", "case_id", "substr(case_desc,1,10)",
            "where 1=1 and case_type = '1'");
        wp.optionKey = wp.colStr("ex_casetype2");
        dddwList("d_dddw_casetype2", "CMS_CASETYPE", "case_id", "substr(case_desc,1,10)",
            "where 1=1 and case_type = '1'");
        wp.optionKey = wp.colStr("ex_user");
        dddwList("d_dddw_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
      }


    } catch (Exception ex) {
    }

  }

}
