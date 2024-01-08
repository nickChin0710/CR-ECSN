/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-01-05  V1.00.05  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         * 
******************************************************************************/
package rskr02;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Rskr0950 extends BaseAction implements InfaceExcel, InfacePdf {

  taroko.base.CommDate commDate = new taroko.base.CommDate();

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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
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
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_adj_yymm"), "adj_yymm")
        + sqlCol(wp.itemStr("ex_adj_loc_flag"), "adj_loc_flag");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " * ";
    wp.daoTable = " rsk_r001_data1 ";
    wp.whereOrder = " order by adj_yymm, adj_loc_flag, rec_no";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

    queryAfter();

  }

  void queryAfter() {

    double tlAftLocAmt = 0, tlBefLocAmt = 0, tlBefPurchAmt = 0, tlAftPurchAmt = 0;

    String sql0 = " select " + " purch_mm1 , " + " purch_mm2 " + " from rsk_r001_parm "
        + " where adj_yymm = ? " + " and adj_loc_flag = ? ";

    sqlSelect(sql0, new Object[] {wp.itemStr("ex_adj_yymm"), wp.itemStr("ex_adj_loc_flag")});

    wp.colSet("purch_mm1", sqlStr("purch_mm1"));
    wp.colSet("purch_mm2", sqlStr("purch_mm2"));

    String lsDate1 = "", lsDate2 = "";
    lsDate1 = commString.mid(
        commDate.dateAdd(wp.itemStr2("ex_adj_yymm") + "01", 0, -wp.colInt("purch_mm1"), 0), 0, 6);
    lsDate2 = commString
        .mid(commDate.dateAdd(wp.itemStr2("ex_adj_yymm") + "01", 0, wp.colInt("purch_mm2"), 0), 0, 6);
    wp.colSet("ex_purch1", lsDate1);
    wp.colSet("ex_purch2", lsDate2);

    // String sql1 = " select "
    // + " (sum(aft_loc_amt) - sum(bef_loc_amt)) / sum(bef_loc_amt) as li_loc_amt_pct , "
    // + " sum(bef_purch_amt) / 6 / sum(bef_loc_amt) as li_bef_limit_pct , "
    // + " sum(aft_purch_amt) / 6 / sum(aft_loc_amt) as li_aft_limit_pct , "
    // + " (sum(aft_purch_amt) - sum(bef_purch_amt)) / sum(aft_purch_amt) as li_purch_grow_pct "
    // + " from rsk_r001_data1 "
    // + " where adj_yymm = ? "
    // + " and adj_loc_flag = ? "
    // ;

    String sql1 = " select " + " sum(aft_loc_amt) as tl_aft_loc_amt , "
        + " sum(bef_loc_amt) as tl_bef_loc_amt , " + " sum(aft_purch_amt) as tl_aft_purch_amt , "
        + " sum(bef_purch_amt) as tl_bef_purch_amt " + " from rsk_r001_data1 "
        + " where adj_yymm = ? " + " and adj_loc_flag = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("ex_adj_yymm"), wp.itemStr("ex_adj_loc_flag")});

    tlAftLocAmt = sqlNum("tl_aft_loc_amt");
    tlBefLocAmt = sqlNum("tl_bef_loc_amt");
    tlBefPurchAmt = sqlNum("tl_bef_purch_amt");
    tlAftPurchAmt = sqlNum("tl_aft_purch_amt");
    if (tlBefLocAmt == 0) {
      wp.colSet(0, "hi_loc_amt_pct", "0");
      wp.colSet(0, "hi_bef_limit_pct", "0");
    } else {
      wp.colSet(0, "hi_loc_amt_pct", (tlAftLocAmt - tlBefLocAmt) / tlBefLocAmt * 100);
      wp.colSet(0, "hi_bef_limit_pct", tlBefPurchAmt / 6 / tlBefLocAmt * 100);
    }
    if (tlAftLocAmt == 0) {
      wp.colSet(0, "hi_aft_limit_pct", "0");
    } else {
      wp.colSet(0, "hi_aft_limit_pct", tlAftPurchAmt / 6 / tlAftLocAmt * 100);
    }
    if (tlAftPurchAmt == 0) {
      wp.colSet(0, "hi_purch_grow_pct", "0");
    } else {
      wp.colSet(0, "hi_purch_grow_pct",
          (tlAftPurchAmt - tlBefPurchAmt) / tlAftPurchAmt * 100);
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
    wp.reportId = "rskr0950";
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();
    String allStr = "額度覆核月份: " + commString.strToYmd(wp.itemStr("ex_adj_yymm")) + " 消費統計起訖: "
        + commString.strToYmd(wp.colStr("ex_purch1")) + " -- " + commString.strToYmd(wp.colStr("ex_purch2"))
        + " 調整類別: ";
    if (wp.itemEq("ex_adj_loc_flag", "1")) {
      allStr += "調高";
    } else if (wp.itemEq("ex_adj_loc_flag", "2")) {
      allStr += "調低";
    }
    wp.colSet("cond1", allStr);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0950.xlsx";
    pdf.pageCount = 28;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "rskr0950";
      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "rskr0950.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      String allStr = "額度覆核月份: " + commString.strToYmd(wp.itemStr("ex_adj_yymm")) + " 消費統計起訖: "
          + commString.strToYmd(wp.colStr("ex_purch1")) + " -- " + commString.strToYmd(wp.colStr("ex_purch2"))
          + " 調整類別: ";
      if (wp.itemEq("ex_adj_loc_flag", "1")) {
        allStr += "調高";
      } else if (wp.itemEq("ex_adj_loc_flag", "2")) {
        allStr += "調低";
      }
      wp.colSet("cond1", allStr);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}
