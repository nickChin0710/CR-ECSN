/*****************************************************************************
*                                                                            *
 *                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-31  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-04-27  V1.00.02  shiyuqi     updated for project coding standard       * 
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
* 111-05-26  V1.00.03   Ryan       移除重複getWhereStr()                        *   
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF2;

public class Bilr0200 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "bilr0200";
  String reportSubtitle = "";
  String condWhere = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // wp.setExcelMode();
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

    dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
    String exDateS = wp.itemStr("exDateS");


    String lsWhere = "where 1=1  ";

    if (empty(exDateS) == false) {
      lsWhere += " and settl_date = :exDateS ";
      setString("exDateS", exDateS);
    } else {
      alertErr("請款日期為必要條件!!請重新輸入.");
      return false;
    }

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

//    if (getWhereStr() == false)
//      return;

    wp.selectSQL = "" + "file_name, " + "line_no, " + "settl_date, " + "mcht_no, " + "txn_seqno, "
        + "purchase_date, " + "card_no, " + "card_no_ext, " + "txn_code, " + "auth_code, "
        + "settl_amt, " + "bank_fee_amt, " + "cht_fee_amt, " + "payment_amt, " + "order_no, "
        + "order_term, " + "mcht_id, " + "mcht_name, " + "summons_no, " + "invoice_no, "
        + "source_curr, " + "source_amt, " + "gov_order_no, " + "gov_order_name, " + "proc_code, "
        + "payment_date, " + "mod_time ";

    wp.daoTable = " bil_govpurchase ";
    wp.whereOrder = " order by file_name,line_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    int sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      sum1 += Integer.parseInt(wp.colStr(ii, "settl_amt"));
      sum2 += Integer.parseInt(wp.colStr(ii, "bank_fee_amt"));
      sum3 += Integer.parseInt(wp.colStr(ii, "cht_fee_amt"));
      sum4 += Integer.parseInt(wp.colStr(ii, "payment_amt"));
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("sum1", intToStr(sum1));
    wp.colSet("sum2", intToStr(sum2));
    wp.colSet("sum3", intToStr(sum3));
    wp.colSet("sum4", intToStr(sum4));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {

    String exDateS = wp.itemStr("exDateS");
    String title = "請款日期: " + exDateS;

    reportSubtitle = title;

  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      subTitle();
      wp.colSet("cond_1", reportSubtitle);
      // -cond-
      // String exDateS = wp.item_ss("exDateS");
      // String ss = "請款日期: " + exDateS ;
      // wp.col_set("cond_1", ss);


      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      // wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      /*
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
       */
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    // String ss = "PDFTEST: ";
    // wp.col_set("cond_1", ss);
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF2 pdf = new TarokoPDF2();
    // 表頭固定欄位
    pdf.fixHeader[0] = "user_id";
    pdf.fixHeader[1] = "cond_1";

    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_office_m_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_office");
      // dddw_list("dddw_office_m_code", "bil_office_m", "office_m_code", "office_m_name", "where
      // 1=1 group by office_m_code,office_m_name order by office_m_code");

    } catch (Exception ex) {
    }
  }

}

