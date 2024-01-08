
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-27  V1.00.01  Ryan       program initial
* 109-05-06  V1.00.02  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package colr01;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.base.CommString;

public class Colr0130 extends BaseReport {
  CommString commString = new CommString();
  String mProgName = "colr0130";
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
  public void initPage() {}

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("cycle");
      dddwList("PtrWorkdayCycleList", "ptr_workday", "stmt_cycle", "",
          " where 1=1 order by stmt_cycle");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {

    wp.whereStr = "where 1=1 ";

    if (empty(wp.itemStr("send_date1")) == false) {
      wp.whereStr += " and acct_month = :send_date1 ";
      setString("send_date1", wp.itemStr("send_date1"));
    }
    if (empty(wp.itemStr("cycle")) == false) {
      wp.whereStr += " and stmt_cycle = :cycle ";
      setString("cycle", wp.itemStr("cycle"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " col_payment_rate.acct_month " + ", col_payment_rate.this_month_mcode "
        + ", col_payment_rate.last_month_mcode " + ", sum (col_payment_rate.static_cnt) static_cnt "
        + ", sum (col_payment_rate.static_amt) static_amt ";

    wp.daoTable = " col_payment_rate ";
    wp.whereOrder = " GROUP BY acct_month, this_month_mcode, last_month_mcode "
        + "ORDER BY acct_month, this_month_mcode, last_month_mcode ";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wp.colSet("rowid", "");
      return;
    }
    listWkdata();
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
      String cycle = "";
      if (empty(wp.itemStr("cycle"))) {
        cycle = "總表";
      } else {
        cycle = wp.itemStr("cycle");
      }
      String cond1 = "資料年月: " + commString.strToYmd(wp.itemStr("send_date1")) + " 週期: " + cycle;
      // + " -- " + commString.ss_2ymd(wp.item_ss("ex_yymm2"))
      wp.colSet("cond_1", cond1);
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
    // phopho add check
    if (empty(wp.itemStr("send_date1"))) {
      alertErr2("請輸入查詢月份");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }

    wp.reportId = mProgName;
    // -cond-
    String cycle = "";
    if (empty(wp.itemStr("cycle"))) {
      cycle = "總表";
    } else {
      cycle = wp.itemStr("cycle");
    }
    // String ss = "資料年月: " + commString.ss_2ymd(wp.item_ss("send_date1"))+" 週期: "+cycle;
    // + " -- " + commString.ss_2ymd(wp.item_ss("ex_yymm2"));
    String cond1 = "資料年月: " + commString.strToYmd(wp.itemStr("send_date1")) + " 週期: " + cycle;
    wp.colSet("cond_1", cond1);
    wp.colSet("IdUser", wp.loginUser);
    /*
     * String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_send_date1")); wp.col_set("cond_2", ss2);
     */
    wp.pageRows = 999;
    queryFunc();
    wp.listCount[0] = 1;
    // wp.setListCount(1);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata() {
    String m0 = "";
    long tol2StaticCnt = 0;
    long tol2StaticAmt = 0;
    String[] staticCnt = new String[wp.selectCnt + 1000];
    String[] staticAmt = new String[wp.selectCnt + 1000];
    int ii = 0;
    wp.logSql = false;
    String sqlSelect = " select  max(col_payment_rate.this_month_mcode) this_month_mcode_max, "
        + " max(col_payment_rate.last_month_mcode) last_month_mcode_max "
        + " from col_payment_rate ";
    sqlSelect(sqlSelect);
    int lastMonthMcodeMax = this.toInt(sqlStr("last_month_mcode_max"));
    int thisMonthMcodeMax = this.toInt(sqlStr("this_month_mcode_max"));
    int thisMax = thisMonthMcodeMax + 1;
    for (int i = 0; i <= lastMonthMcodeMax; i++) {
      m0 = "M0" + wp.colStr(i, "last_month_mcode");
      wp.colSet("last_mcode" + i, m0);
    }
    m0 = "";
    for (int i = 0; i <= thisMonthMcodeMax; i++) {
      m0 = "M0" + wp.colStr(ii, "this_month_mcode");
      wp.colSet("this_mcode" + i, m0);
      ii = ii + thisMax;
    }
    int r = 0;
    int rr = 0;
    int a = 0;
    String lsStaticCnt = "";
    String lsStaticAmt = "";
    long tolStaticCnt = 0;
    long tolStaticAmt = 0;
    long totalStaticCnt = 0;
    long totalStaticAmt = 0;
    for (int i = 0; i <= thisMonthMcodeMax; i++) {
      tolStaticCnt = 0;
      tolStaticAmt = 0;
      for (int y = 0; y <= lastMonthMcodeMax; y++) {
        String sqlSelect1 = "select  sum (col_payment_rate.static_cnt) static_cnt "
            + ", sum (col_payment_rate.static_amt) static_amt  " + "from  col_payment_rate  "
            + "where 1=1  and acct_month = :send_date1 "
            + "and this_month_mcode = :this_month_mcode "
            + "and last_month_mcode = :last_month_mcode ";
        if (empty(wp.itemStr("cycle")) == false) {
          sqlSelect1 += " and stmt_cycle = :cycle ";
          setString("cycle", wp.itemStr("cycle"));
        }
        setString("send_date1", wp.itemStr("send_date1"));
        setString("this_month_mcode", i + "");
        setString("last_month_mcode", y + "");
        sqlSelect(sqlSelect1);
        staticCnt[r] = sqlStr("static_cnt");
        staticAmt[r] = sqlStr("static_amt");
        lsStaticCnt = sqlStr("static_cnt");
        lsStaticAmt = sqlStr("static_amt");
        tolStaticCnt += (long) this.toNum(sqlStr("static_cnt"));
        tolStaticAmt += (long) this.toNum(sqlStr("static_amt"));
        wp.colSet("static_cnt" + r, lsStaticCnt);
        wp.colSet("static_amt" + r, lsStaticAmt);
        wp.colSet("tol_static_cnt" + i, tolStaticCnt + "");
        wp.colSet("tol_static_amt" + i, tolStaticAmt + "");
        r++;
      }
      totalStaticCnt += tolStaticCnt;
      totalStaticAmt += tolStaticAmt;
    }
    wp.colSet("total_static_cnt", totalStaticCnt + "");
    wp.colSet("total_static_amt", totalStaticAmt + "");
    for (int x = 0; x <= thisMonthMcodeMax; x++) {
      tol2StaticCnt = 0;
      tol2StaticAmt = 0;
      rr = 0 + a;
      for (int i = 0; i <= thisMonthMcodeMax; i++) {

        tol2StaticCnt += (long) this.toNum(staticCnt[rr]);
        tol2StaticAmt += (long) this.toNum(staticAmt[rr]);
        rr = rr + thisMax;
      }
      wp.colSet("tol2_static_cnt" + a, tol2StaticCnt + "");
      wp.colSet("tol2_static_amt" + a, tol2StaticAmt + "");
      a++;
    }
    wp.colSet("rowid", "1");
  }

}
