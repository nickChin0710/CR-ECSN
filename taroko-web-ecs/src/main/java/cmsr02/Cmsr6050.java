/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.03   shiyuqi     updated for project coding standard     * 
* 108-12-23  V1.00.2     Alex           dept name fix                             *
* 108-11-25  V1.00.1     Alex           dddw fix                                  *
* 109-07-27  V1.00.04   JustinWu change cms_proc_dept into ptr_dept_code
* 109-07-28  V1.00.2   Sunny       db_day & db_master_day          
* 111-11-22  V1.00.01  Machao     頁面bug調整          *
******************************************************************************/
package cmsr02;

import ofcapp.BaseAction;

public class Cmsr6050 extends BaseAction {

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
    String lsWhere = " where ( A.case_date = B.case_date ) and ( A.case_seqno = B.case_seqno ) "
        + sqlCol(wp.itemStr("ex_cms_date_s"), "A.case_date", ">=")
        + sqlCol(wp.itemStr("ex_cms_date_e"), "A.case_date", "<=")
        + sqlCol(wp.itemStr("ex_casetype"), "B.case_type")
        + sqlCol(wp.itemStr("ex_proc_deptno"), "A.proc_deptno");

    if (wp.itemEq("ex_case_result", "0")) {
      lsWhere += " and A.proc_result in ('0','5','9') ";
    } else if (wp.itemEq("ex_case_result", "1")) {
      lsWhere += " and A.proc_result = '0' ";
    } else if (wp.itemEq("ex_case_result", "2")) {
      lsWhere += " and A.proc_result = '5' ";
    } else if (wp.itemEq("ex_case_result", "3")) {
      lsWhere += " and A.proc_result = '9' ";
    }
    
    sqlParm.setSqlParmNoClear(true);
    sum(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
    sqlParm.clear();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.proc_deptno ," + "A.proc_result ," + "B.case_type ,"
        + "sum(days(decode(A.finish_date,'',sysdate,to_date(A.finish_date,'yyyymmdd'))) - days(to_date(A.case_date,'yyyymmdd')) - (select count(*) from ptr_holiday where holiday >=A.case_date and holiday<=decode(A.finish_date,'',to_char(sysdate,'yyyymmdd'),A.finish_date)) ) as db_day,"
		+ "sum(days(decode(B.finish_date,'',sysdate,to_date(B.finish_date,'yyyymmdd'))) - days(to_date(A.case_date,'yyyymmdd')) - (select count(*) from ptr_holiday where holiday >=A.case_date and holiday<=decode(B.finish_date,'',to_char(sysdate,'yyyymmdd'),B.finish_date)) ) as db_master_day,"
        + "count(*) as db_count "

    ;
    wp.daoTable = "cms_casedetail A, cms_casemaster B  ";

    wp.whereOrder = " group by A.proc_deptno,A.proc_result,B.CASE_TYPE order by A.proc_deptno ";
    logSql();
    wp.pageCountSql =
        "" + "select count(*) from ( " + " select distinct A.proc_deptno,A.proc_result,B.CASE_TYPE "
            + " from cms_casedetail A, cms_casemaster B " + wp.whereStr
            + " group by A.proc_deptno,A.proc_result,B.CASE_TYPE)";
    pageQuery();


    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() {
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;
    String sql1 = "select case_desc from cms_casetype where case_id = ?";
    String sql2 = "select dept_name from ptr_dept_code where dept_code = ?";
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      wp.colSet(ii, "db_day", wp.colNum(ii, "db_day") / wp.colNum(ii, "db_count"));
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "case_type")});
      if (sqlRowNum > 0)
        wp.colSet(ii, "case_desc", sqlStr("case_desc"));
      sqlSelect(sql2, new Object[] {wp.colStr(ii, "proc_deptno")});
      if (sqlRowNum > 0)
        wp.colSet(ii, "dept_name", sqlStr("dept_name"));
      if (wp.colEq(ii, "proc_result", "0")) {
        wp.colSet(ii, "proc_result", "未處理");
      } else if (wp.colEq(ii, "proc_result", "5")) {
        wp.colSet(ii, "proc_result", "處理中");
      } else if (wp.colEq(ii, "proc_result", "9")) {
        wp.colSet(ii, "proc_result", "完成");
      }
    }
  }

  void sum(String lsWhere) {
    String sql1 = "select count(*) as db_cnt from cms_casedetail A , cms_casemaster B " + lsWhere;
    sqlSelect(sql1);

    wp.colSet("tl_total_cnt", sqlNum("db_cnt"));
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
      if (eqIgno(wp.respHtml, "Cmsr6050")) {
        wp.optionKey = wp.colStr("ex_casetype");
        dddwList("d_dddw_casetype", "CMS_CASETYPE", "case_id", "case_desc",
            "where 1=1 and case_type ='1' ");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "Cmsr6050")) {
        wp.optionKey = wp.colStr("ex_proc_deptno");
        dddwList("dddw_proc_dept", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
      }

    } catch (Exception ex) {
    }
  }


}
