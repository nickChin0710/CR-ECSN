/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03  shiyuqi       修改无意义命名                          *
* 110-03-31  V1.00.04  Justin       fix XSS                                  *   
******************************************************************************/
package colr01;

import java.util.Arrays;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colr0040 extends BaseReport {
  CommString commString = new CommString();

  String mProgName = "colr0040";
  String procDate = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      // dataProcess();
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

    // dddw_select();
    initButton();
  }

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exDateS");
    String lsDate2 = wp.itemStr("exDateE");

    if (empty(wp.itemStr("exDateS"))) {
      alertErr2("請輸入[查詢年月]");
      return false;
    }

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[查詢年月-起迄]  輸入錯誤");
      return false;
    }

    wp.whereStr = "where 1=1 ";

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and proc_month >= :proc_months ";
      setString("proc_months", wp.itemStr("exDateS"));
    }
    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and proc_month <= :proc_monthe ";
      setString("proc_monthe", wp.itemStr("exDateE"));
    }

    wp.whereOrder = "order by proc_month, trans_type, sub_trans_type ";
    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "proc_month, " + "trans_type, " + "sub_trans_type, "
        + "trans_type || decode(sub_trans_type, '', '0', sub_trans_type) db_type, "
        + "sub_jrnl_bal, " + "sub_acct_amt, " + "sub_acct_cnt, " + "add_acct_amt, "
        + "add_acct_cnt, " + "acct_jrnl_bal, " + "acct_cnt, " + "last_jrnl_bal, "
        + "sub_jrnl_bal_actual, " + "del_amt ";

    wp.daoTable = "col_staticrate";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String wkData = "";
    String[] cde = new String[] {"20", "30", "31", "32", "33", "40", "41", "50", "60", "70"};
    String[] txt = new String[] {"逾放款", "催收款- 小計", "　　　- 小於一年", "　　　- 一至二年", "　　　- 大於二年",
        "呆帳-未追索債權", "呆帳-追索債權", "呆帳(合計)", "呆帳轉列數", "帳外息回收金額"};

    double sumAccJrnlBal = 0, allAcctJrnlBal = 0;
    double[] totSubJrnlBal = new double[cde.length];
    double[] totSubAcctAmt = new double[cde.length];
    double[] totSubAcctCnt = new double[cde.length];
    double[] totAddAcctAmt = new double[cde.length];
    double[] totAddAcctCnt = new double[cde.length];
    double[] totAcctJrnlBal = new double[cde.length];
    double[] totAcctCnt = new double[cde.length];
    for (int i = 0; i < cde.length; i++) {
      totSubJrnlBal[i] = 0;
      totSubAcctAmt[i] = 0;
      totSubAcctCnt[i] = 0;
      totAddAcctAmt[i] = 0;
      totAddAcctCnt[i] = 0;
      totAcctJrnlBal[i] = 0;
      totAcctCnt[i] = 0;
    }

    int llRowcnt = wp.selectCnt;
    for (int ii = 0; ii < llRowcnt; ii++) {
      wkData = wp.colStr(ii, "db_type");
      wp.colSet(ii, "tt_db_type", commString.decode(wkData, cde, txt));
      // 年月縮排
      wkData = "";
      if (!wp.colStr(ii - 1, "proc_month").equals(wp.colStr(ii, "proc_month"))) {
        wkData = wp.colStr(ii, "proc_month");
        wp.colSet(ii, "tt_proc_month", wkData);
      }
      // 合計總計
      sumAccJrnlBal += wp.colNum(ii, "acct_jrnl_bal");
      allAcctJrnlBal += wp.colNum(ii, "acct_jrnl_bal");
      if (!wp.colStr(ii, "proc_month").equals(wp.colStr(ii + 1, "proc_month"))) {
    	  wp.colSet(ii, "sumAccJrnlBal", numToStr(sumAccJrnlBal, "#,##0"));
//        wp.colSet(ii, "tr",
//            "<tr><td nowrap colspan=\"8\" class=\"list_rr\" style=\"color:#CC0000\">&nbsp;合    計：</td>"
//                + "<td nowrap class=\"list_rr\" style=\"color:blue\">"
//                + numToStr(sumAccJrnlBal, "#,##0") + "</td>"
//                + "<td nowrap class=\"list_ll\">&nbsp;</td></tr>");
        sumAccJrnlBal = 0;
      }else {
    	  wp.colSet(ii, "trStyle", "display:none");
      }

      // 合計區塊
      wkData = wp.colStr(ii, "db_type");
      int rr = Arrays.asList(cde).indexOf(wkData.trim());
      totSubJrnlBal[rr] += wp.colNum(ii, "sub_jrnl_bal");
      totSubAcctAmt[rr] += wp.colNum(ii, "sub_acct_amt");
      totSubAcctCnt[rr] += wp.colNum(ii, "sub_acct_cnt");
      totAddAcctAmt[rr] += wp.colNum(ii, "add_acct_amt");
      totAddAcctCnt[rr] += wp.colNum(ii, "add_acct_cnt");
      totAcctJrnlBal[rr] += wp.colNum(ii, "acct_jrnl_bal");
      totAcctCnt[rr] += wp.colNum(ii, "acct_cnt");
    }

    if (llRowcnt > 0) {
      String strTotal = "";
      for (int i = 0; i < cde.length; i++) {
        String totProcMonth = "";
        if (i == 0) {
        	totProcMonth = "合  計";
        }

//        strTotal +=
//            "<tr><td nowrap class=\"list_no\">&nbsp; </td>" + "<td nowrap class=\"list_cc\">&nbsp;"
//                + totProcMonth + " </td>" + "<td nowrap class=\"list_ll\">&nbsp;" + txt[i]
//                + " </td>" + "<td nowrap class=\"list_rr\">&nbsp;"
//                + numToStr(totSubJrnlBal[i], "#,##0") + " </td>"
//                + "<td nowrap class=\"list_rr\">&nbsp;" + numToStr(totSubAcctAmt[i], "#,##0")
//                + " </td>" + "<td nowrap class=\"list_rr\">&nbsp;"
//                + numToStr(totSubAcctCnt[i], "#,##0") + " </td>"
//                + "<td nowrap class=\"list_rr\">&nbsp;" + numToStr(totAddAcctAmt[i], "#,##0")
//                + " </td>" + "<td nowrap class=\"list_rr\">&nbsp;"
//                + numToStr(totAddAcctCnt[i], "#,##0") + " </td>"
//                + "<td nowrap class=\"list_rr\">&nbsp;" + numToStr(totAcctJrnlBal[i], "#,##0")
//                + " </td>" + "<td nowrap class=\"list_rr\">&nbsp;"
//                + numToStr(totAcctCnt[i], "#,##0") + " </td></tr>";
        
        wp.colSet(i, "totProcMonth", totProcMonth);
        wp.colSet(i, "totalTxt", txt[i]);
        wp.colSet(i, "totSubJrnlBal", numToStr(totSubJrnlBal[i], "#,##0"));
        wp.colSet(i, "totSubAcctAmt", numToStr(totSubAcctAmt[i], "#,##0"));
        wp.colSet(i, "totSubAcctCnt", numToStr(totSubAcctCnt[i], "#,##0"));
        wp.colSet(i, "totAddAcctAmt", numToStr(totAddAcctAmt[i], "#,##0"));
        wp.colSet(i, "totAddAcctCnt", numToStr(totAddAcctCnt[i], "#,##0"));
        wp.colSet(i, "totAcctJrnlBal", numToStr(totAcctJrnlBal[i], "#,##0"));
        wp.colSet(i, "totAcctCnt", numToStr(totAcctCnt[i], "#,##0"));
        
        //// FOR PRINT
        wp.colSet("tt_db_type_" + i, txt[i]);
        wp.colSet("tot_sub_jrnl_bal_" + i, totSubJrnlBal[i]);
        wp.colSet("tot_sub_acct_amt_" + i, totSubAcctAmt[i]);
        wp.colSet("tot_sub_acct_cnt_" + i, totSubAcctCnt[i]);
        wp.colSet("tot_add_acct_amt_" + i, totAddAcctAmt[i]);
        wp.colSet("tot_add_acct_cnt_" + i, totAddAcctCnt[i]);
        wp.colSet("tot_acct_jrnl_bal_" + i, totAcctJrnlBal[i]);
        wp.colSet("tot_acct_cnt_" + i, totAcctCnt[i]);
      }
      wp.selectCnt = cde.length;
      wp.setListCount(2);
      
//      strTotal +=
//          "<tr><td nowrap colspan=\"8\" class=\"list_rr\" style=\"color:#CC0000\">&nbsp;總    計：</td>"
//              + "<td nowrap class=\"list_rr\" style=\"color:blue\">"
//              + numToStr(allAcctJrnlBal, "#,##0") + "</td>"
//              + "<td nowrap class=\"list_ll\">&nbsp;</td></tr>";

      wp.colSet("total", strTotal);
      //// FOR PRINT
      wp.colSet("allAcctJrnlBal", numToStr(allAcctJrnlBal, "#,##0"));
      wp.colSet("all_acct_jrnl_bal", allAcctJrnlBal);
      wp.selectCnt = 1;
      wp.setListCount(3);
    }
  }

  @Override
  public void querySelect() throws Exception {
    procDate = wp.itemStr("data_k1");
    dataRead();
  }

  public void dataRead() throws Exception {
    this.selectNoLimit();
    wp.selectSQL = "hex(col_stage_log.rowid) as rowid, " + "col_stage_log.proc_date, "
        + "col_stage_log.proc_time, " + "col_stage_log.acct_type, " + "col_stage_log.corp_no, "
        + "crd_idno.id_no ";
    wp.daoTable =
        "col_stage_log left join crd_idno on col_stage_log.id_p_seqno = crd_idno.id_p_seqno ";
    wp.whereStr = "where col_stage_log.proc_date = :proc_date ";
    setString("proc_date", procDate);
    wp.whereOrder = "order by 1";

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
  }

  void xlsPrint() throws Exception {
    // try {
    // System.out.println("xlsFunction: started--------");
    // wp.reportId = m_progName;
    // //-cond-
    // String ss = "顯示項目：" + commString.decode(wp.item_ss("exFailFlag"), ",0,1,2",
    // ",0.全部顯示,1.筆數不符,2.資料錯誤");
    // wp.col_set("cond_1", ss);
    // wp.col_set("loginUser", wp.loginUser);
    //
    // //===================================
    // TarokoExcel xlsx = new TarokoExcel();
    // wp.fileMode = "N";
    // //xlsx.report_id ="rskr0020";
    // xlsx.excelTemplate = m_progName + ".xlsx";
    //
    // //====================================
    // //-明細-
    // xlsx.sheetName[0] ="明細";
    // queryFunc();
    // wp.setListCount(1);
    // ddd("Detl: rowcnt:" + wp.listCount[0]);
    // xlsx.processExcelSheet(wp);
    // /*
    // //-合計-
    // xlsx.sheetName[1] ="合計";
    // query_Summary(cond_where);
    // wp.listCount[1] =sql_nrow;
    // ddd("Summ: rowcnt:" + wp.listCount[1]);
    // //xlsx.sheetNo = 1;
    // xlsx.processExcelSheet(wp);
    // */
    // xlsx.outputExcel();
    // xlsx = null;
    // ddd("xlsFunction: ended-------------");
    //
    // } catch (Exception ex) {
    // wp.expMethod = "xlsPrint";
    // wp.expHandle(ex);
    // }
  }

  void pdfPrint() throws Exception {
    if (getWhereStr() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = mProgName;
    wp.pageRows = 9999;

    String cond1 = "顯示項目：" + commString.decode(wp.itemStr("exFailFlag"), ",0,1,2", ",0.全部顯示,1.筆數不符,2.資料錯誤");
    wp.colSet("cond_1", cond1);
    wp.colSet("reportName", mProgName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
    queryFunc();

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

}
