
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-27  V1.00.01  Ryan       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/

package mktq01;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktq0240 extends BaseReport {
  String mProgName = "mktq0240";
  int ii = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";

      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      // is_action = "R";
      // dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("tol_db_total_purch_cnt", 0);
    wp.colSet("tol_db_total_purch_amt", 0);
    wp.colSet("tol_db_count_rew_amount", 0);
    wp.colSet("tol_db_avg_purch_amt", 0);
    wp.colSet("tol_db_discount_amt", 0);
    wp.colSet("tol_db_rew_amount", 0);
    wp.colSet("tol_db_rew_amount2", 0);
    wp.colSet("tol_db_sum_rew_amount", 0);
    wp.colSet("tol_db_rew_percentage", 0);
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_groupcode");
      dddwList("dddw_groupcode", "ptr_group_code", "group_code", "group_name",
          " where 1=1 order by group_code");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {

    String lsDate1 = wp.itemStr("ex_yymm1");
    String lsDate2 = wp.itemStr("ex_yymm2");


    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[統計期間-起迄]  輸入錯誤");
      return;
    }

    wp.whereStr = "where 1=1 and reward_type='2' ";
    wp.whereStr += sqlStrend(lsDate1, lsDate2, "rew_yyyymm");

    if (empty(wp.itemStr("ex_groupcode")) == false) {
      wp.whereStr += " and group_code = :ex_groupcode ";
      setString("ex_groupcode", wp.itemStr("ex_groupcode"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " group_code " + ", sum(total_purch_cnt) as db_total_purch_cnt "
        + ", sum(total_purch_amt) as db_total_purch_amt "
        + ", sum(total_purch_amt-exp_tax_amt) as db_count_rew_amount "
        + ", sum(avg_purch_amt) as db_avg_purch_amt " + ", sum(discount_amt) as db_discount_amt "
        + ", sum(rew_amount) as db_rew_amount " + ", sum(rew_amount2) as db_rew_amount2 "
        + ", sum(rew_amount+rew_amount2) as db_sum_rew_amount"
        + ", (sum(rew_amount+rew_amount2)*100.0/sum(total_purch_amt-exp_tax_amt)) as db_rew_percentage ";

    wp.daoTable = " mkt_re_group ";
    wp.whereOrder = " GROUP BY group_code ";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wp.colSet("tol_db_total_purch_cnt", 0);
      wp.colSet("tol_db_total_purch_amt", 0);
      wp.colSet("tol_db_count_rew_amount", 0);
      wp.colSet("tol_db_avg_purch_amt", 0);
      wp.colSet("tol_db_discount_amt", 0);
      wp.colSet("tol_db_rew_amount", 0);
      wp.colSet("tol_db_rew_amount2", 0);
      wp.colSet("tol_db_sum_rew_amount", 0);
      wp.colSet("tol_db_rew_percentage", 0);
      return;
    }
    list_wkdata();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {

  }


  public void dataProcess() throws Exception {
    queryFunc();
    if (wp.selectCnt == 0) {
      alertErr2("報表無資料可比對");
      return;
    }

  }

  void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String allStr = "統計期間: " + commString.strToYmd(wp.itemStr("ex_yymm1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_yymm2"));
      wp.colSet("cond_1", allStr);
      /*
       * String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1")) + " -- " +
       * commString.ss_2ymd(wp.item_ss("ex_send_date1")); wp.col_set("cond_2", ss2);
       */

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

      queryFunc();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);

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
    String allStr = "統計期間: " + commString.strToYmd(wp.itemStr("ex_yymm1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_yymm2"));
    wp.colSet("cond_1", allStr);
    wp.colSet("IdUser", wp.loginUser);
    /*
     * String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_send_date1")); wp.col_set("cond_2", ss2);
     */
    wp.pageRows = 9999;
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void list_wkdata() {
    double tolDbTotalPurchCnt = 0, tolDbTotalPurchAmt = 0, tolDbCountRewAmount = 0,
        tolDbAvgPurchAmt = 0, tolDbDiscountAmt = 0, tolDbRewAmount = 0, tolDbRewAmount2 = 0,
        tolDbSumRewAmount = 0, tolDbRewPercentage = 0;
    for (int i = 0; i < wp.selectCnt; i++) {
      tolDbTotalPurchCnt += wp.colNum(i, "db_total_purch_cnt");
      tolDbTotalPurchAmt += wp.colNum(i, "db_total_purch_amt");
      tolDbCountRewAmount += wp.colNum(i, "db_count_rew_amount");
      tolDbAvgPurchAmt += wp.colNum(i, "db_avg_purch_amt");
      tolDbDiscountAmt += wp.colNum(i, "db_discount_amt");
      tolDbRewAmount += wp.colNum(i, "db_rew_amount");
      tolDbRewAmount2 += wp.colNum(i, "db_rew_amount2");
      tolDbSumRewAmount += wp.colNum(i, "db_sum_rew_amount");
      tolDbRewPercentage += wp.colNum(i, "db_rew_percentage");
    }
    wp.colSet("tol_db_total_purch_cnt", tolDbTotalPurchCnt);
    wp.colSet("tol_db_total_purch_amt", tolDbTotalPurchAmt);
    wp.colSet("tol_db_count_rew_amount", tolDbCountRewAmount);
    wp.colSet("tol_db_avg_purch_amt", tolDbAvgPurchAmt);
    wp.colSet("tol_db_discount_amt", tolDbDiscountAmt);
    wp.colSet("tol_db_rew_amount", tolDbRewAmount);
    wp.colSet("tol_db_rew_amount2", tolDbRewAmount2);
    wp.colSet("tol_db_sum_rew_amount", tolDbSumRewAmount);
    wp.colSet("tol_db_rew_percentage", formatDouble1(tolDbRewPercentage));
  }

  public static double formatDouble1(double d) {
    return (double) Math.round(d * 100) / 100;
  }

}
