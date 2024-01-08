/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/18  V1.00.00   phopho     program initial                           *
*  108/12/19  V1.00.01   phopho     change table: prt_branch -> gen_brn       *
*  109-05-06  V1.00.02  Tanwei       updated for project coding standard      *
*  110-03-31  V1.00.04  Justin       fix XSS                                  *
*  112-10-16  V1.00.05  sunny       移除承辦分行預設值                                                         * 
******************************************************************************/

package colr01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr1120 extends BaseReport {
  CommString commString = new CommString();
  String mProgName = "colr1120";

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

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
//    if (empty(strAction)) {
//      wp.colSet("exRegBankNo", "109");
//    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("exBankCode");
      dddwList("ColLiabBankList", "col_liab_bank", "bank_code", "bank_code||' '||bank_name",
          "where 1=1 order by bank_code ");

      wp.optionKey = wp.colStr("exRegBankNo");
      // dddw_list("PtrBranchNameList", "ptr_branch", "branch", "branch||'['||branch_name||']'",
      // "where 1=1 order by branch ");
      dddwList("PtrBranchNameList", "gen_brn", "branch", "branch||' ['||full_chi_name||']'",
          "where 1=1 order by branch ");

      // wp.optionKey = wp.col_ss("exEndReason");
      // dddw_list("ColLiabIdtabList", "col_liab_idtab", "id_code", "id_code||'['||id_desc||']'",
      // "where id_key='5' order by id_key, id_code ");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("exStatDate")) == false) {
      wp.whereStr += " and static_date = :static_date ";
      setString("static_date", wp.itemStr("exStatDate"));
    }
    if (empty(wp.itemStr("exLiacStatus")) == false) {
      wp.whereStr += " and liac_status = :liac_status ";
      setString("liac_status", wp.itemStr("exLiacStatus"));
    }
    if (empty(wp.itemStr("exAcctStatus")) == false) {
      wp.whereStr += " and acct_status = :acct_status ";
      setString("acct_status", wp.itemStr("exAcctStatus"));
    }
    if (empty(wp.itemStr("exBankCode")) == false) {
      wp.whereStr += " and bank_code = :bank_code ";
      setString("bank_code", wp.itemStr("exBankCode"));
    }
    if (empty(wp.itemStr("exRegBankNo")) == false) {
      wp.whereStr += " and reg_bank_no = :reg_bank_no ";
      setString("reg_bank_no", wp.itemStr("exRegBankNo"));
    }
    if (empty(wp.itemStr("exEndReason")) == false) {
      wp.whereStr += " and end_reason = :end_reason ";
      setString("end_reason", wp.itemStr("exEndReason"));
    }
    if (empty(wp.itemStr("exContractDate")) == false) {
      wp.whereStr += " and contract_date = :contract_date ";
      setString("contract_date", wp.itemStr("exContractDate"));
    }
    if (empty(wp.itemStr("exInstallSDate")) == false) {
      wp.whereStr += " and install_s_date = :install_s_date ";
      setString("install_s_date", wp.itemStr("exInstallSDate"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " liac_status, " + " acct_status, " + " count(*) as count, "
        + " sum(per_allocate_amt) as sum_per_allocate_amt, "
        + " sum(tot_amt_apply) as sum_tot_amt_apply, "
        + " sum(tot_amt_runbatch) as sum_tot_amt_runbatch ";

    wp.daoTable = " col_liac_stat_bystatus ";

    wp.whereOrder = " group by liac_status, acct_status ";
    wp.whereOrder += " order by liac_status, acct_status ";

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
  }

  void listWkdata() throws Exception {
    String wkData = "";
    int allCntId = 0, gp1CntId = 0, rowCntId = 0;
    long allPerAllocateAmt = 0, gp1PerAllocateAmt = 0;
    long allTotAmtApply = 0, gp1TotAmtApply = 0;
    long allTotAmtRunbatch = 0, gp1TotAmtRunbatch = 0;
    long allAmt = 0, gp1Amt = 0, rowAmt = 0;
    String gp1LiacStatus = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "liac_status");
      gp1LiacStatus = commString.decode(wkData, ",1,2,3,4,5,6", ",受理申請,停催通知,簽約完成,結案/復催,結案/毀諾,結案/結清");
      wp.colSet(ii, "tt_liac_status", gp1LiacStatus);

      wkData = wp.colStr(ii, "acct_status");
      wp.colSet(ii, "tt_acct_status", commString.decode(wkData, ",1,4", ",正常(含逾催),呆帳"));

      rowCntId = (int) wp.colNum(ii, "count");
      wp.colSet(ii, "row_cnt_id", rowCntId + "");
      rowAmt = (long) (wp.colNum(ii, "sum_per_allocate_amt") + wp.colNum(ii, "sum_tot_amt_apply")
          + wp.colNum(ii, "sum_tot_amt_runbatch"));
      wp.colSet(ii, "row_amt", rowAmt + "");

      gp1CntId += rowCntId;
      gp1PerAllocateAmt += wp.colNum(ii, "sum_per_allocate_amt");
      gp1TotAmtApply += wp.colNum(ii, "sum_tot_amt_apply");
      gp1TotAmtRunbatch += wp.colNum(ii, "sum_tot_amt_runbatch");
      gp1Amt += rowAmt;
      if (!wp.colStr(ii, "liac_status").equals(wp.colStr(ii + 1, "liac_status"))) {
//        wp.colSet(ii, "tr",
//            "<tr><td nowrap colspan=\"2\" class=\"list_rr\">" + gp1LiacStatus + "&nbsp;小計：</td>"
//                + "<td nowrap class=\"list_rr\" style=\"color:blue\"> " + numToStr(gp1CntId, "")
//                + "&nbsp;</td>" + "<td nowrap class=\"list_rr\" style=\"color:blue\"> "
//                + numToStr(gp1PerAllocateAmt, "") + "&nbsp;</td>"
//                + "<td nowrap class=\"list_rr\" style=\"color:blue\"> "
//                + numToStr(gp1TotAmtApply, "") + "&nbsp;</td>"
//                + "<td nowrap class=\"list_rr\" style=\"color:blue\"> "
//                + numToStr(gp1TotAmtRunbatch, "") + "&nbsp;</td>"
//                + "<td nowrap class=\"list_rr\" style=\"color:blue\"> " + numToStr(gp1Amt, "")
//                + "&nbsp;</td></tr>");
        
        wp.colSet(ii, "gp1LiacStatus", gp1LiacStatus);
        wp.colSet(ii, "gp1CntId", numToStr(gp1CntId, ""));
        wp.colSet(ii, "gp1PerAllocateAmt", numToStr(gp1PerAllocateAmt, ""));
        wp.colSet(ii, "gp1TotAmtApply", numToStr(gp1TotAmtApply, ""));
        wp.colSet(ii, "gp1TotAmtRunbatch", numToStr(gp1TotAmtRunbatch, ""));
        wp.colSet(ii, "gp1Amt", numToStr(gp1Amt, ""));
        
        gp1CntId = 0;
        gp1PerAllocateAmt = 0;
        gp1TotAmtApply = 0;
        gp1TotAmtRunbatch = 0;
        gp1Amt = 0;
      }else {
    	  wp.colSet(ii, "trStyle", "display:none");
      }

      allCntId += rowCntId;
      allPerAllocateAmt += wp.colNum(ii, "sum_per_allocate_amt");
      allTotAmtApply += wp.colNum(ii, "sum_tot_amt_apply");
      allTotAmtRunbatch += wp.colNum(ii, "sum_tot_amt_runbatch");
      allAmt += rowAmt;
    }
    wp.colSet("all_cnt_id", allCntId + "");
    wp.colSet("all_per_allocate_amt", allPerAllocateAmt + "");
    wp.colSet("all_tot_amt_apply", allTotAmtApply + "");
    wp.colSet("all_tot_amt_runbatch", allTotAmtRunbatch + "");
    wp.colSet("all_amt", allAmt + "");
  }

  @Override
  public void querySelect() throws Exception {

  }

  void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String cond1 = "統計日期： " + commString.strToYmd(wp.itemStr("exStatDate"));
      wp.colSet("cond_1", cond1);
      wp.colSet("loginUser", wp.loginUser);
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
      wp.colSet("IdUser", wp.loginUser);
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
    wp.pageRows = 9999;

    String cond1 = "統計日期： " + commString.strToYmd(wp.itemStr("exStatDate"));
    wp.colSet("cond_1", cond1);
    wp.colSet("reportName", mProgName.toUpperCase());
    wp.colSet("IdUser", wp.loginUser);
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
