/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-03  V1.00.00  Andy Liu   program initial                        
* 109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
******************************************************************************/
package dbbr01;

import java.io.InputStream;
import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbbr0030 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbbr0030";

  String condWhere = "";
  String reportSubtitle = "";

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

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    // wp.col_set("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    // 固定條件
    wp.whereStr = "where 1=1 ";
    // 自選條件
    wp.whereStr += sqlStrend(date1, date2, "effc_yyyymm");

    // wp.whereStr = ls_where;
    // setParameter();
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

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;
    wp.selectSQL = "effc_yyyymm, " + "sum(lf_cnt) as sum_lf_cnt, " + "sum(lf_amt) as sum_lf_amt, "
        + "sum(rr_cnt) as sum_rr_cnt, " + "sum(rr_amt) as sum_rr_amt, "
        + "sum(uc_cnt) as sum_uc_cnt, " + "sum(uc_amt) as sum_uc_amt, "
        + "sum(oi_cnt) as sum_oi_cnt, " + "sum(oi_amt) as sum_oi_amt, "
        + "sum(rf_cnt) as sum_rf_cnt, " + "sum(rf_amt) as sum_rf_amt, "
        + "sum(lf_cnt_d) as sum_lf_cnt_d, " + "sum(lf_amt_d) as sum_lf_amt_d, "
        + "sum(rr_cnt_d) as sum_rr_cnt_d, " + "sum(rr_amt_d) as sum_rr_amt_d, "
        + "sum(uc_cnt_d) as sum_uc_cnt_d, " + "sum(uc_amt_d) as sum_uc_amt_d, "
        + "sum(oi_cnt_d) as sum_oi_cnt_d, " + "sum(oi_amt_d) as sum_oi_amt_d, "
        + "sum(rf_cnt_d) as sum_rf_cnt_d, " + "sum(rf_amt_d) as sum_rf_amt_d, " + "'' wk_temp";
    wp.daoTable = " dbb_sum_all ";
    wp.whereOrder = " group by effc_yyyymm " + " order by effc_yyyymm ";

    // System.out.println(" select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);

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
    int rowct = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowct += 1;
    }
    wp.colSet("row_ct", intToStr(rowct));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");

    String all = "統計月份:  ";
    // ex_bank_no分行
    if (empty(date1) == false || empty(date2) == false) {
      if (empty(date1) == false) {
        all += date1 + " 起 ";
      }
      if (empty(date2) == false) {
        all += " ~ " + date2 + " 迄 ";
      }
    }

    reportSubtitle = all;

  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = progName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
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
    wp.reportId = progName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 20;
    // pdf.pageVert= true; //直印
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
      // dddw_bank_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_bill_type");
      // dddw_list("dddw_billuint", "ptr_billunit", "bill_unit", "short_title", "where 1=1 order by
      // bill_unit");

    } catch (Exception ex) {
    }
  }

}
