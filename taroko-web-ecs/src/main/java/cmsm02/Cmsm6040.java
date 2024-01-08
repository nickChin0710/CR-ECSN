/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-04  V1.00.01  Alex       dataRead , queryRead bug fixed             *
* 108-12-17  V1.00.02  Alex       fix querywhere                             *
* 109-04-27  V1.00.03  shiyuqi       updated for project coding standard     *
* 109-07-20  V1.00.04  sunny       fix bug,add cms_casetype.apr_flag ='Y'    *     
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *                                                                          *
******************************************************************************/
package cmsm02;

import ofcapp.BaseAction;

public class Cmsm6040 extends BaseAction {
  Cmsm6040Func func;
  String caseId = "", caseType = "";

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

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 and case_type ='1' " + sqlCol(wp.itemStr("ex_case_id"), "case_id");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "case_type," + " case_id , " + " replace(param1,'\"','') as param1 ,"
        + " replace(param2,'\"','') as param2 ," + " replace(param3,'\"','') as param3 ,"
        + " replace(param4,'\"','') as param4 ," + " replace(param5,'\"','') as param5 ,"
        + " replace(param6,'\"','') as param6 ," + " replace(param7,'\"','') as param7 ,"
        + " replace(param8,'\"','') as param8 ," + " replace(param9,'\"','') as param9 ,"
        + " replace(param10,'\"','') as param10 ," + " replace(param11,'\"','') as param11 ,"
        + " replace(param12,'\"','') as param12 ," + " replace(param13,'\"','') as param13 ,"
        + " replace(param14,'\"','') as param14 ," + " replace(param15,'\"','') as param15 ";
    wp.daoTable = "cms_casetype_msg ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by case_type, case_id ";
    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq("case_type", "1")) {
        wp.colSet(ii, "tt_case_type", "問題案件");
      } else if (wp.colEq("case_type", "2")) {
        wp.colSet(ii, "tt_case_type", "處理部門");
      }
    }

  }

  @Override
  public void querySelect() throws Exception {
    caseId = wp.itemStr("data_k1");
    caseType = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(caseId)) {
      caseId = wp.itemStr("kk_case_id");
    }
    if (empty(caseType)) {
      caseType = "1";
    }

    if (empty(caseId)) {
      alertErr2("案件類別代碼:不可空白");
      return;
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno," + " case_id , " + " replace(param1,'\"','') as param1 ,"
            + " replace(param2,'\"','') as param2 ," + " replace(param3,'\"','') as param3 ,"
            + " replace(param4,'\"','') as param4 ," + " replace(param5,'\"','') as param5 ,"
            + " replace(param6,'\"','') as param6 ," + " replace(param7,'\"','') as param7 ,"
            + " replace(param8,'\"','') as param8 ," + " replace(param9,'\"','') as param9 ,"
            + " replace(param10,'\"','') as param10 ," + " replace(param11,'\"','') as param11 ,"
            + " replace(param12,'\"','') as param12 ," + " replace(param13,'\"','') as param13 ,"
            + " replace(param14,'\"','') as param14 ," + " replace(param15,'\"','') as param15 ,"
            + " mod_user," + " to_char(mod_time,'yyyymmdd') as mod_date," + " mod_pgm,"
            + " apr_user," + " apr_date," + " apr_flag";
    wp.daoTable = "cms_casetype_msg";
    wp.whereStr = " where 1=1" + sqlCol(caseId, "case_id") + sqlCol(caseType, "case_type");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + caseId);
      return;
    }
    log("A:" + wp.colStr("param2"));
  }

  @Override
  public void saveFunc() throws Exception {

    if (this.checkApproveZz() == false) {
      return;
    }

    func = new cmsm02.Cmsm6040Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    this.saveAfter(false);
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

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsm6040")) {
        // wp.initOption = "--";
        wp.optionKey = wp.colStr("ex_case_id");
        dddwList("dddw_case_id",
            "select case_id as db_code , case_id||' '||case_desc as db_desc from cms_casetype where 1=1 and case_type ='1' and apr_flag ='Y' order by case_id");
      }

      if (eqIgno(wp.respHtml, "cmsm6040_detl")) {
        // wp.initOption = "--";
        wp.optionKey = wp.colStr("kk_case_id");
        dddwList("dddw_case_id",
            "select case_id as db_code , case_id||' '||case_desc as db_desc from cms_casetype where 1=1 and case_type ='1' and apr_flag ='Y' order by case_id");
      }

    } catch (Exception ex) {
    }

  }

}
