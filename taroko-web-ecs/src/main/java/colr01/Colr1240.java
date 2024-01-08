/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/20  V1.00.00   phopho     program initial                           *
*  109-05-06  V1.00.01  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package colr01;

import ofcapp.AppMsg;
import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr1240 extends BaseReport {
  CommString commString = new CommString();
  String mProgName = "colr1240";

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
    if (empty(wp.itemStr("exReportYm"))) {
      alertErr2("統計年月 不可空白");
      return false;
    }

    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("exReportYm")) == false) {
      wp.whereStr += " and report_month = :report_month ";
      setString("report_month", wp.itemStr("exReportYm"));
    }
    if (empty(wp.itemStr("exReportType")) == false) {
      wp.whereStr += " and report_type = :report_type ";
      setString("report_type", wp.itemStr("exReportType"));
    }

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

    wp.selectSQL = " report_month, " + " report_type, " + " report_status, " + " acct_jrnl_bal, "
        + " status_cnt, " + " report_type || '_' || report_status db_status ";

    wp.daoTable = " col_jcic_s0_sum ";

    wp.whereOrder = " order by report_type, report_status ";

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
    String dbStatus = "";
    int sumStatusCnt = 0;
    long sumAcctJrnlBal = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      dbStatus = wp.colStr(ii, "db_status");
      wp.colSet(ii, "tt_db_status", wfStatusDesc(dbStatus));

      sumStatusCnt += wp.colNum(ii, "status_cnt");
      sumAcctJrnlBal += wp.colNum(ii, "acct_jrnl_bal");
    }
    wp.colSet("sum_status_cnt", sumStatusCnt + "");
    wp.colSet("sum_acct_jrnl_bal", sumAcctJrnlBal + "");
  }

  String wfStatusDesc(String idcode) throws Exception {
    String rtn = "";

    switch (idcode) {
      case "1_1":
        rtn = "1.消金債務協商覆約中(含喘息)";
        break;
      case "1_2":
        rtn = "2.前置協商申請，尚未達成協議";
        break;
      case "1_3":
        rtn = "3.前置協商簽約完成覆約中(含喘息)";
        break;
      case "1_4":
        rtn = "4.個別協商簽約完成覆約中";
        break;
      case "1_5":
        rtn = "5.毀諾後一致性個別協商覆約中";
        break;
      case "1_6":
        rtn = "6.更生戶";
        break;
      case "1_7":
        rtn = "7.清算戶";
        break;
      case "1_8":
        rtn = "8.個別協商申請中";
        break;
      case "1_9":
        rtn = "9.失聯";
        break;
      case "1_A":
        rtn = "A.未失聯亦未參與各項協商機制(強制執行中)";
        break;
      case "1_B":
        rtn = "B.未失聯亦未參與各項協商機制、更生或清算(還款意願低落)";
        break;
      case "1_C":
        rtn = "C.未失聯亦未參與各項協商機制或更生、清算(還款能力不足)";
        break;
      case "1_D":
        rtn = "D.未失聯亦未參與各項協商機制或更生、清算(暫不催理)";
        break;
      case "1_E":
        rtn = "E.曾參與機制，惟毀諾(撤回、駁回)後未另達成協議或更生、清算(強制執行中)";
        break;
      case "1_F":
        rtn = "F.曾參與機制，惟毀諾(撤回、駁回)後未另達成協議或更生、清算(還款意願低落)";
        break;
      case "1_G":
        rtn = "G.曾參與機制，惟毀諾(撤回、駁回)後未另達成協議或更生、清算(還款能力不足)";
        break;
      case "1_H":
        rtn = "H.曾參與機制，惟毀諾(撤回、駁回)後未另達成協議或更生、清算(暫不催理)";
        break;
      case "1_I":
        rtn = "I.其他";
        break;
      case "2_1":
        rtn = "1.本筆債權清償";
        break;
      case "2_2":
        rtn = "2.個別協商覆約中";
        break;
      case "2_3":
        rtn = "3.毀諾後一致性個別協商覆約中";
        break;
      case "2_4":
        rtn = "4.更生戶";
        break;
      case "2_5":
        rtn = "5.清算戶";
        break;
      case "2_6":
        rtn = "6.本筆債權已轉售AMC";
        break;
      case "2_7":
        rtn = "7.失聯";
        break;
      case "2_8":
        rtn = "8.其他";
        break;
      default:
        rtn = idcode;
        break;
    }

    return rtn;
  }

  @Override
  public void querySelect() throws Exception {

  }

  void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_mod_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_mod_date2"));
      wp.colSet("cond_1", cond1);

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
    if (getWhereStr() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = mProgName;
    wp.pageRows = 9999;

    String cond1 = "統計年月: " + wp.itemStr("exReportYm") + "    狀態類別: "
        + commString.decode(wp.itemStr("exReportType"), ",1,2", ",S01,S02");
    wp.colSet("cond_1", cond1);
    wp.colSet("reportName", mProgName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
    queryFunc();

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
