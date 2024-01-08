
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-06  V1.00.01  Ryan       program initial                            *
* 109-05-06  V1.00.02  Tanwei     updated for project coding standard        *
* 109-01-04  V1.00.03  shiyuqi    修改无意义命名                             *
* 110-03-31  V1.00.04  Justin     fix XSS                                    *
******************************************************************************/

package colr01;

import java.text.DecimalFormat;

import ofcapp.AppMsg;
import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr1110 extends BaseReport {
  CommString commString = new CommString();
  String ttCloseReason = "";
  String mProgName = "colr1110";
  // int ii = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_bank_code");
      dddwList("ColLiabBankList", "col_liab_bank", "bank_code", "bank_code||' '||bank_name",
          "where 1=1 order by bank_code ");

    } catch (Exception ex) {
    }
  }

  int getWhereStr() {
    String lsDate1 = wp.itemStr("ex_stat_ym1");
    String lsDate2 = wp.itemStr("ex_stat_ym2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[通知月份-起迄]  輸入錯誤");
      return -1;
    }

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_stat_date")) == false) {
      wp.whereStr += " and static_day = :ex_stat_date ";
      setString("ex_stat_date", wp.itemStr("ex_stat_date"));
    }
    if (empty(wp.itemStr("ex_stat_ym1")) == false) {
      wp.whereStr += " and static_month >= :ex_stat_ym1 ";
      setString("ex_stat_ym1", wp.itemStr("ex_stat_ym1"));
    }
    if (empty(wp.itemStr("ex_stat_ym2")) == false) {
      wp.whereStr += " and static_month <= :ex_stat_ym2 ";
      setString("ex_stat_ym2", wp.itemStr("ex_stat_ym2"));
    }
    if (empty(wp.itemStr("ex_acct_sts")) == false) {
      wp.whereStr += " and acct_status = :ex_acct_sts ";
      setString("ex_acct_sts", wp.itemStr("ex_acct_sts"));
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    if (getWhereStr() != 1) {
      return;
    }

    wp.pageControl();

    wp.selectSQL = " static_month, static_day, acct_status, mcode, liac_id_cnt, tot_amt ";

    wp.daoTable = " col_liac_stat_apply ";

    wp.whereOrder = " order by static_month ";

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
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  void queryRead2() throws Exception {
    if (getWhereStr() != 1) {
      return;
    }
    wp.pageControl();

    wp.selectSQL =
        " static_month , " + " acct_status , " + " decode (mcode,'0','1','1','1',mcode) AS mcode, "
            + " SUM (liac_id_cnt) AS liac_id_cnt, " + " SUM (tot_amt) AS tot_amt ";

    wp.daoTable = " col_liac_stat_apply ";

    wp.whereOrder = " GROUP BY static_month, acct_status "
        + " ,decode (mcode,'0','1','1','1',mcode) " + " order by static_month ";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata2();
    wp.setPageValue();

  }

  void listWkdata() throws Exception {
    DecimalFormat df = new DecimalFormat("###,##0");
    String acctStatus = "", strGp1Amt = "";
    int allCnt = 0, gp1Cnt = 0;
    long allAmt = 0, gp1Amt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      acctStatus = wp.colStr(ii, "acct_status");
      wp.colSet(ii, "tt_acct_status", commString.decode(acctStatus, ",1,2,3,4,5", ",正常,逾放,催收,呆帳,結清(Write Off)"));

      if (wp.colStr(ii, "mcode").equals("2")) {
        wp.colSet(ii, "wk_mcode", "2 以上");
      } else if (wp.colStr(ii, "mcode").equals("0") && wp.colStr(ii, "acct_status").equals("4")) {
        wp.colSet(ii, "wk_mcode", "ALL");
      } else {
        wp.colSet(ii, "wk_mcode", wp.colStr(ii, "mcode"));
      }


      if (wp.colStr(ii, "static_month").equals(wp.colStr(ii + 1, "static_month"))) {
        gp1Cnt += wp.colNum(ii, "liac_id_cnt");
        gp1Amt += wp.colNum(ii, "tot_amt");
      } else {
        gp1Cnt += wp.colNum(ii, "liac_id_cnt");
        gp1Amt += wp.colNum(ii, "tot_amt");
      }
      if (!wp.colStr(ii, "static_month").equals(wp.colStr(ii + 1, "static_month"))
          && !wp.colStr(ii - 1, "static_month").equals(wp.colStr(ii, "static_month"))) {
        gp1Cnt = (int) wp.colNum(ii, "liac_id_cnt");
        gp1Amt = (long) wp.colNum(ii, "tot_amt");
      }
      if (!wp.colStr(ii, "static_month").equals(wp.colStr(ii + 1, "static_month"))) {
        strGp1Amt = df.format(gp1Amt);
//        wp.colSet(ii, "tr",
//            "<tr><td ></td><td></td><td></td><td nowrap class=\"td_data\" align=\"right\">月份小計:</td><td nowrap class=\"td_data\"><span class=\"dsp_number\">"
//                + gp1Cnt
//                + "</span> </td><td nowrap class=\"td_data\" align=\"right\"><span class=\"dsp_number\">"
//                + strGp1Amt + "</span> </td></tr>");
        wp.colSet(ii, "trGp1Cnt", gp1Cnt);
        wp.colSet(ii, "trStrGp1Amt", strGp1Amt);
        gp1Cnt = 0;
        gp1Amt = 0;
      }else {
    	wp.colSet(ii, "trStyle", "display:none");
      }
      allCnt += wp.colNum(ii, "liac_id_cnt");
      allAmt += wp.colNum(ii, "tot_amt");
    }
    wp.colSet("gp1_cnt", "");
    wp.colSet("gp1_amt", "");
    wp.colSet("all_cnt", allCnt + "");
    wp.colSet("all_amt", allAmt + "");

  }

  void listWkdata2() {
    int i = 0, r = 0, num = 0;
    long tolLiacIdCnt = 0, tolTotAmt = 0, tol2LiacIdCnt = 0, tol2TotAmt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {

      if (wp.colStr(ii, "static_month").equals(wp.colStr(ii + 1, "static_month"))) {

        if (wp.colStr(ii, "acct_status").equals("1") && wp.colStr(ii, "mcode").equals("1")) {
          wp.colSet(ii - i - r, "liac_id_cnt1", wp.colStr(ii, "liac_id_cnt"));
          wp.colSet(ii - i - r, "tot_amt1", wp.colStr(ii, "tot_amt"));
        }
        if (wp.colStr(ii, "acct_status").equals("1") && wp.colStr(ii, "mcode").equals("2")) {
          wp.colSet(ii - i - r, "liac_id_cnt2", wp.colStr(ii, "liac_id_cnt"));
          wp.colSet(ii - i - r, "tot_amt2", wp.colStr(ii, "tot_amt"));
        }
        if (wp.colStr(ii, "acct_status").equals("4")) {
          wp.colSet(ii - i - r, "liac_id_cnt3", wp.colStr(ii, "liac_id_cnt"));
          wp.colSet(ii - i - r, "tot_amt3", wp.colStr(ii, "tot_amt"));
        }
        tolLiacIdCnt += wp.colNum(ii, "liac_id_cnt");
        tolTotAmt += wp.colNum(ii, "tot_amt");
        wp.colSet(ii - i - r, "tol_liac_id_cnt", tolLiacIdCnt + "");
        wp.colSet(ii - i - r, "tol_tot_amt", tolTotAmt + "");
        i++;

      } else {
        if (wp.colStr(ii, "acct_status").equals("1") && wp.colStr(ii, "mcode").equals("1")) {
          wp.colSet(ii - i - r, "liac_id_cnt1", wp.colStr(ii, "liac_id_cnt"));
          wp.colSet(ii - i - r, "tot_amt1", wp.colStr(ii, "tot_amt"));
        }
        if (wp.colStr(ii, "acct_status").equals("1") && wp.colStr(ii, "mcode").equals("2")) {
          wp.colSet(ii - i - r, "liac_id_cnt2", wp.colStr(ii, "liac_id_cnt"));
          wp.colSet(ii - i - r, "tot_amt2", wp.colStr(ii, "tot_amt"));
        }
        if (wp.colStr(ii, "acct_status").equals("4")) {
          wp.colSet(ii - i - r, "liac_id_cnt3", wp.colStr(ii, "liac_id_cnt"));
          wp.colSet(ii - i - r, "tot_amt3", wp.colStr(ii, "tot_amt"));
        }
        tolLiacIdCnt += wp.colNum(ii, "liac_id_cnt");
        tolTotAmt += wp.colNum(ii, "tot_amt");
        tol2LiacIdCnt += tolLiacIdCnt;
        tol2TotAmt += tolTotAmt;
        wp.colSet(ii - i - r, "tol_liac_id_cnt", tolLiacIdCnt + "");
        wp.colSet(ii - i - r, "tol_tot_amt", tolTotAmt + "");
        wp.colSet(ii - i - r, "tt_static_month", wp.colStr(ii, "static_month"));
        r += i;
        i = 0;
        tolLiacIdCnt = 0;
        tolTotAmt = 0;
      }

    }
    wp.listCount[0] = wp.selectCnt - r;
    wp.colSet("tol2_liac_id_cnt", Long.toString(tol2LiacIdCnt));
    wp.colSet("tol2_tot_amt", Long.toString(tol2TotAmt));
  }

  @Override
  public void querySelect() throws Exception {

  }

  public void ptrSysIdtabDesc() {

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
      String cond2 = " 統計日期 :" + commString.strToYmd(wp.itemStr("ex_stat_date"));
      wp.colSet("cond_1", cond2);
      cond2 = " 通知月份 :" + commString.strToYmd(wp.itemStr("ex_stat_ym1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_stat_ym2"));
      wp.colSet("cond_2", cond2);
      wp.colSet("IdUser", wp.loginUser);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

      // queryFunc();
      queryRead2();
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
    String cond2 = " 統計日期 :" + commString.strToYmd(wp.itemStr("ex_stat_date"));
    wp.colSet("cond_1", cond2);
    cond2 = " 通知月份: " + commString.strToYmd(wp.itemStr("ex_stat_ym1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_stat_ym2"));
    wp.colSet("cond_2", cond2);
    wp.colSet("IdUser", wp.loginUser);
    wp.pageRows = 9999;
    // queryFunc();
    queryRead2();
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

}
