/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-0628:           JH    cond-check
* 109-04-28  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-24  V1.00.02  Justin         parameterize sql
******************************************************************************/
package rskr02;/**

 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0612 extends BaseAction implements InfacePdf {
  String isCond = "", isReportName = "信用額度調整表";

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
      strAction = "U";
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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  public void listSum(String lsWhere) {
    String sql1 = " select " + " count(*) as tl_cnt "
        + " from rsk_acnolog A left join sec_user B on A.mod_user = B.usr_id " + lsWhere;

    sqlSelect(sql1);

    wp.colSet("tl_cnt", "" + sqlStr("tl_cnt"));
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (icondStrend("ex_date1", "ex_date2") == false) {
      alertErr2("日期起迄: 輸入錯誤");
      return;
    }

    String lsWhere = getWhereStr();

    setSqlParmNoClear(true);
    listSum(lsWhere);

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

private String getWhereStr() {
	String lsWhere = " where A.log_type ='1' and A.kind_flag = 'A' ";
	isCond = "";
    if (wp.itemEq("ex_log_mode", "1")) {
      lsWhere += " and A.log_mode='1' and A.apr_flag='Y'";
      isCond += "調整方式: 線上 ";
    } else if (wp.itemEq("ex_log_mode", "2")) {
      lsWhere += " and A.log_mode ='2'";
      isCond += "調整方式: 整批 ";
    } else {
      isCond += "調整方式: 全部 ";
    }

    if (wp.itemEq("ex_ask_mode", "1")) {
      lsWhere += sqlCol(wp.itemStr("ex_date1"), "A.log_date", ">=")
          + sqlCol(wp.itemStr("ex_date2"), "A.log_date", "<=");
      isCond += " 調整日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_date2"));
    } else if (wp.itemEq("ex_ask_mode", "2")) {
      lsWhere += sqlCol(wp.itemStr("ex_date1"), "A.apr_date", ">=")
          + sqlCol(wp.itemStr("ex_date2"), "A.apr_date", "<=");
      isCond += " 覆核日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_date2"));
    }
	return lsWhere;
}

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " B.usr_deptno as dept_no , " + " count(*) as ll_cnt , "
        + " sum(decode(A.adj_loc_flag,1,0,1)) as low_cnt , "
        + " sum(decode(A.adj_loc_flag,1,1,0)) as high_cnt ";
    wp.daoTable = " rsk_acnolog A left join sec_user B on A.mod_user = B.usr_id ";
    wp.whereOrder = " group by B.usr_deptno ";
    wp.pageCountSql = " select count(*) from (select distinct B.usr_deptno from "
        + " rsk_acnolog A left join sec_user B on A.mod_user = B.usr_id " + wp.whereStr + " )";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(0);
    wp.setPageValue();

  }

  void queryAfter() {
    String sql1 = " select " + " dept_name " + " from ptr_dept_code " + " where dept_code = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "dept_no")});
      if (sqlRowNum <= 0)
        continue;

      wp.colSet(ii, "dept_no", wp.colStr(ii, "dept_no") + "_" + sqlStr("dept_name"));
    }
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
  public void pdfPrint() throws Exception {
    wp.reportId = "Rskr0612";
    wp.pageRows = 9999;
    // if(wp.item_eq("ex_ask_mode", "1")){
    // is_cond = "調整日期:"+commString.ss_2ymd(wp.item_ss("ex_date1"))+" --
    // "+commString.ss_2ymd(wp.item_ss("ex_date2"));
    // } else if (wp.item_eq("ex_ask_mode", "2")){
    // is_cond = "覆核日期:"+commString.ss_2ymd(wp.item_ss("ex_date1"))+" --
    // "+commString.ss_2ymd(wp.item_ss("ex_date2"));
    // }
    queryFunc();
    wp.colSet("user_id", wp.loginUser);
    wp.colSet("cond1", isCond);
    wp.colSet("report_name", isReportName);
    TarokoPDF pdf = new TarokoPDF();
    pdf.pageVert = true;
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0612.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
