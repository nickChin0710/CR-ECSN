/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                                                                           *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ichr0120 extends BaseAction implements InfacePdf {
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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_crt_date1") && wp.itemEmpty("ex_crt_date2")
        && wp.itemEmpty("ex_close_date1") && wp.itemEmpty("ex_close_date2")) {
      alertErr2("請輸入 統計年月");
      return;
    }

    String lsCrtDate1 = "", lsCrtDate2 = "", lsCloseDate1 = "", lsCloseDate2 = "";
    lsCrtDate1 = wp.itemStr2("ex_crt_date1");
    lsCrtDate2 = wp.itemStr2("ex_crt_date2");
    lsCloseDate1 = wp.itemStr2("ex_close_date1");
    lsCloseDate2 = wp.itemStr2("ex_close_date2");

    if (!empty(lsCrtDate1))
      lsCrtDate1 += "01";
    if (!empty(lsCrtDate2))
      lsCrtDate2 += "31";
    if (!empty(lsCloseDate1))
      lsCloseDate1 += "01";
    if (!empty(lsCloseDate2))
      lsCloseDate2 += "31";

    if (chkStrend(lsCrtDate1, lsCrtDate2) == false) {
      alertErr2("檔案年月:起迄錯誤");
      return;
    }

    if (chkStrend(lsCloseDate1, lsCloseDate2) == false) {
      alertErr2("結案年月:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 and close_date = '' " + sqlCol(lsCrtDate1, "crt_date", ">=")
        + sqlCol(lsCrtDate2, "crt_date", "<=") + sqlCol(lsCloseDate1, "close_date", ">=")
        + sqlCol(lsCloseDate2, "close_date", "<=");

    if (wp.itemEq("ex_apr_flag", "N")) {
      lsWhere += " and clo_apr_date = '' ";
    } else {
      lsWhere += " and clo_apr_date <> '' ";
    }


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " crt_date , " + " sum(decode(close_reason,'1',1,0)) as wk_cnt1 , "
        + " sum(decode(close_reason,'1',txn_amt,0)) as wk_amt1 , "
        + " sum(decode(close_reason,'2',1,0)) as wk_cnt2 , "
        + " sum(decode(close_reason,'2',txn_amt,0)) as wk_amt2 , "
        + " sum(decode(close_reason,'1',1,'2',1,0)) as wk_cnt3 , "
        + " sum(decode(close_reason,'1',txn_amt,'2',txn_amt,0)) as wk_amt3 , "
        + " sum(decode(close_reason,'3',1,0)) as wk_cnt4 , "
        + " sum(decode(close_reason,'3',txn_amt,0)) as wk_amt4 , "
        + " sum(decode(close_reason,'4',1,0)) as wk_cnt5 , "
        + " sum(decode(close_reason,'4',txn_amt,0)) as wk_amt5 , "
        + " sum(decode(close_reason,'3',1,'4',1,0)) as wk_cnt6 , "
        + " sum(decode(close_reason,'3',txn_amt,'4',txn_amt,0)) as wk_amt6 , "
        + " sum(decode(close_reason,'1',1,'2',1,'3',1,'4',1,0)) as wk_cnt7 , "
        + " sum(decode(close_reason,'1',txn_amt,'2',txn_amt,'3',txn_amt,'4',txn_amt,0)) as wk_amt7 ";

    wp.daoTable = " ich_a04b_exception ";
    wp.whereOrder = " group by crt_date ";

    wp.pageCountSql = "select count(*) from (select distinct crt_date from ich_a04b_exception "
        + wp.whereStr + " ) ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();


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
    wp.reportId = "ichr0120";
    wp.pageRows = 9999;
    String tmpStr = "";
    if (wp.colEq("ex_apr_flag", "N")) {
      tmpStr += "覆核類別:未覆核   ";
    } else {
      tmpStr += "覆核類別:已覆核   ";
    }

    tmpStr += " 檔案年月:" + commString.strToYmd(wp.itemStr2("ex_crt_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_crt_date2"));
    tmpStr += " 結案年月:" + commString.strToYmd(wp.itemStr2("ex_close_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_close_date2"));

    wp.colSet("cond1", tmpStr);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ichr0120.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
