/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-03  V1.00.01  Ryan       program initial                            *
* 108/12/31  V1.00.02    phopho     add busi.func.ColFunc.f_auth_query()     *
* 109-05-06  V1.00.03  Tanwei       updated for project coding standard
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
* 112-07-21  V1.00.04  Ryan       PDF調整,移除線上覆核                                                                                *  
*****************************************************************************/

package colr01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr0030 extends BaseReport {
  CommString commString = new CommString();
  String ttCloseReason = "";
  int x = 1;
  String mProgName = "";
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
    /*
     * try { wp.initOption = "--"; wp.optionKey = wp.item_ss("ex_bank_code");
     * dddw_list("ColLiabBankList", "col_liab_bank", "bank_code", "bank_code||' '||bank_name",
     * "where 1=1 order by bank_code ");
     * 
     * } catch (Exception ex) { }
     */
  }

  @Override
  public void initPage() {
    wp.colSet("data_k1", "a");
    wp.colSet("tab_active1", "id='tab_active'");
  }

  @Override
  public void queryFunc() throws Exception {
    /*
     * if(empty(wp.item_ss("ex_fail_flag"))&&empty(wp.item_ss("ex_stage_flag"))
     * &&empty(wp.item_ss("ex_idno"))&&empty(wp.item_ss("ex_end_bal"))
     * &&empty(wp.item_ss("ex_liab_comp_status"))&&empty(wp.item_ss("ex_mcode"))
     * &&empty(wp.item_ss("ex_last_pay_date"))){ alert_err("至少輸入一個查詢條件!"); return; }
     */

    if ((wp.itemStr("ex_fail_flag").equals("3") || wp.itemStr("ex_fail_flag").equals("4"))
        && !empty(wp.itemStr("ex_stage_flag"))) {
      alertErr("項目選擇 為3.暫不轉催 或4.債務清理條例，不須選擇分期Flag");
      return;
    }

    // -page control-
    // wp.queryWhere = wp.whereStr;
    // wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    queryReadA();
    queryReadB();
    if (wp.itemStr("data_k1").equals("a")) {
      wp.colSet("tab_active1", "id='tab_active'");
      wp.colSet("data_k1", "a");
    }
    if (wp.itemStr("data_k1").equals("b")) {
      wp.colSet("tab_active2", "id='tab_active'");
      wp.colSet("data_k1", "b");
    }

  }

  void queryReadA() throws Exception {
    daoTid = "A-";
    wp.pageControl();
    wp.selectSQL = " id_p_seqno " + " ,id_no " + " ,chi_name " + " ,acct_type " + " ,p_seqno "
        + " ,mcode " + " ,acct_jrnl_bal " + " ,payment_rate1 " + " ,liab_comp_status "
        + " ,end_bal " + " ,acct_status " + " ,credit_act_no " + " ,err_type " + " ,crt_date "
        + " ,pay_by_stage_flag " + " ,last_pay_date ";
    getWhereStr();
    wp.daoTable = " col_b002r1 ";
    wp.whereOrder = " ";
    pageQuery();
    wp.setListCount(1);

    if (listAuthQuery("a-") != 1)
      return; // 查詢權限檢查，參考【f_auth_query】

    listWkdata();
    if (sqlNotFind()) {
      x = 0;
      return;
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    // wp.setPageValue();
    daoTid = "a-";
  }

  void queryReadB() throws Exception {
    daoTid = "B-";
    wp.pageControl();
    wp.selectSQL = " id_p_seqno " + " ,id_no " + " ,chi_name " + " ,acct_type " + " ,p_seqno "
        + " ,mcode " + " ,acct_jrnl_bal " + " ,payment_rate1 " + " ,liab_comp_status "
        + " ,end_bal " + " ,acct_status " + " ,credit_act_no " + " ,err_type " + " ,crt_date "
        + " ,pay_by_stage_flag " + " ,last_pay_date ";
    getWhereStr();
    wp.daoTable = " col_b001r1 ";
    wp.whereOrder = " ";
    pageQuery();
    wp.setListCount(2);

    if (listAuthQuery("b-") != 1)
      return; // 查詢權限檢查，參考【f_auth_query】

    listWkdata2();
    if (sqlNotFind()) {
      if (x == 1) {
        wp.notFound = "N";
      }
      return;
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    // wp.setPageValue();
    daoTid = "b-";
  }

  int listAuthQuery(String tid) throws Exception {
    String idNo = "";
    daoTid = tid;
    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      idNo = wp.colStr(ii, daoTid + "id_no");
      if (func.fAuthQuery(idNo) != 1) {
        wp.listCount[0] = 0;
        wp.listCount[1] = 0;
        alertErr2(func.getMsg());
        return -1;
      }
    }
    return 1;
  }

  void listWkdata() throws Exception {
    String wkData = "";
    long acctJrnlBal = 0, endBal = 0;
    daoTid = "a-";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "acct_status");
      wp.colSet(ii, daoTid + "tt_acct_status", commString.decode(wkData, ",1,2,3", ",正常,逾放,催收"));
      wkData = wp.colStr(ii, daoTid + "err_type");
      wp.colSet(ii, daoTid + "tt_err_type",
          commString.decode(wkData, ",1,2,3,4,5", ",TTL或結欠本金,尚未停卡,暫不轉催收,債務清理條例,分期還款"));
      wkData = wp.colStr(ii, daoTid + "liab_comp_status");
      wp.colSet(ii, daoTid + "tt_liab_comp_status", commString.decode(wkData,
          ",A1,A2,A3,B1,B2,B3,B4,R1,R2,R3,R4,R5,R6,R7,R8,R9,R10,R11,R12,R13,R14"
              + ",L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,L11",
          ",1.停催,2.復催,3.協商成功,1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,"
              + "1.更生開始,2.更生撤回,3.更生認可,4.更生調查,5.更生駁回,6.更生抗告,7.更生保全處份,8.更生抗告保全駁回,9.更生抗告駁回,10.更生終止"
              + ",11.更生終結,12.更生裁定開始,13.更生轉清算,14.更生認可確定,1.清算開始,2.清算撤回,3.清算免責,4.清算調查,5.清算駁回,6.清算不免責"
              + ",7.清算不免責抗告,8.清算開始並終止清算程序,9.清算程序終止 (結),10.清算保全處份,11.清算抗告駁回"));
      acctJrnlBal += wp.colNum(ii, daoTid + "acct_jrnl_bal");
      endBal += wp.colNum(ii, daoTid + "end_bal");
    }
    wp.colSet(daoTid + "count_all", wp.selectCnt + "");
    wp.colSet(daoTid + "sum_acct_jrnl_bal", acctJrnlBal + "");
    wp.colSet(daoTid + "sum_end_bal", endBal + "");
  }

  void listWkdata2() throws Exception {
    String wkData = "";
    long acctJrnlBal = 0, endBal = 0;
    daoTid = "b-";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "acct_status");
      wp.colSet(ii, daoTid + "tt_acct_status", commString.decode(wkData, ",1,2,3", ",正常,逾放,催收"));
      wkData = wp.colStr(ii, daoTid + "err_type");
      wp.colSet(ii, daoTid + "tt_err_type",
          commString.decode(wkData, ",1,2,3,4,5", ",TTL或結欠本金,尚未停卡,暫不轉逾放,債務清理條例,分期還款"));
      wkData = wp.colStr(ii, daoTid + "liab_comp_status");
      wp.colSet(ii, daoTid + "tt_liab_comp_status", commString.decode(wkData,
          ",A1,A2,A3,B1,B2,B3,B4,R1,R2,R3,R4,R5,R6,R7,R8,R9,R10,R11,R12,R13,R14"
              + ",L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,L11",
          ",1.停催,2.復催,3.協商成功,1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,"
              + "1.更生開始,2.更生撤回,3.更生認可,4.更生調查,5.更生駁回,6.更生抗告,7.更生保全處份,8.更生抗告保全駁回,9.更生抗告駁回,10.更生終止"
              + ",11.更生終結,12.更生裁定開始,13.更生轉清算,14.更生認可確定,1.清算開始,2.清算撤回,3.清算免責,4.清算調查,5.清算駁回,6.清算不免責"
              + ",7.清算不免責抗告,8.清算開始並終止清算程序,9.清算程序終止 (結),10.清算保全處份,11.清算抗告駁回"));
      acctJrnlBal += wp.colNum(ii, daoTid + "acct_jrnl_bal");
      endBal += wp.colNum(ii, daoTid + "end_bal");
    }
    wp.colSet(daoTid + "count_all", wp.selectCnt + "");
    wp.colSet(daoTid + "sum_acct_jrnl_bal", acctJrnlBal + "");
    wp.colSet(daoTid + "sum_end_bal", endBal + "");
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
    xlsPrintA();
    xlsPrintB();
  }

  void xlsPrintA() {
    mProgName = "colr0030a";
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      /*
       * String ss = "異動日期: " + commString.ss_2ymd(wp.item_ss("ex_mod_date1")) + " -- " +
       * commString.ss_2ymd(wp.item_ss("ex_mod_date2")); wp.col_set("cond_1", ss);
       */

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

      // queryFunc();
      queryReadA();
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

  void xlsPrintB() {
    mProgName = "colr0030b";
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      /*
       * String ss = "異動日期: " + commString.ss_2ymd(wp.item_ss("ex_mod_date1")) + " -- " +
       * commString.ss_2ymd(wp.item_ss("ex_mod_date2")); wp.col_set("cond_1", ss);
       */

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

      // queryFunc();
      queryReadB();
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
    // -check approve-
//    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
//      wp.respHtml = "TarokoErrorPDF";
//      return;
//    }
    if (wp.itemStr("data_k1").equals("a")) {
      pdfPrintA();
    }
    if (wp.itemStr("data_k1").equals("b")) {
      pdfPrintB();
    }
  }

  void pdfPrintA() throws Exception {
    mProgName = "colr0030a";
    wp.reportId = mProgName;
    // -cond-
    /*
     * String ss = "異動日期 " + commString.ss_2ymd(wp.item_ss("ex_mod_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_mod_date2")); wp.col_set("cond_1", ss);
     */
    wp.colSet("IdUser", wp.loginUser);
    wp.pageRows = 9999;
    // queryFunc();
    queryReadA();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  void pdfPrintB() throws Exception {
    mProgName = "colr0030b";
    wp.reportId = mProgName;
    // -cond-
    /*
     * String ss = "異動日期 " + commString.ss_2ymd(wp.item_ss("ex_mod_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_mod_date2")); wp.col_set("cond_1", ss);
     */
    wp.colSet("IdUser", wp.loginUser);
    wp.pageRows = 9999;
    // queryFunc();
    queryReadB();
    wp.setListCount(1);

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

  void getWhereStr() {

    wp.whereStr = " where 1=1 ";

    switch (wp.itemStr("ex_fail_flag")) {
      case "1":
        wp.whereStr += " and err_type = '1' ";
        break;
      case "2":
        wp.whereStr += " and err_type = '2' ";
        break;
      case "3":
        wp.whereStr += " and err_type = '3' and  pay_by_stage_flag = '' ";
        break;
      case "4":
        wp.whereStr += " and err_type = '3' and  pay_by_stage_flag = 'NE' ";
        break;
      case "5":
        wp.whereStr +=
            " and err_type = '3' and  pay_by_stage_flag <> 'NE' and  pay_by_stage_flag <> '' ";
        break;
    }
    if (!empty(wp.itemStr("ex_stage_flag"))) {
      if (wp.itemStr("ex_stage_flag").equals("02")) {
        wp.whereStr += " and pay_by_stage_flag >= '02'  and substr(pay_by_stage_flag,1,1) != 'N' ";
      } else {
        wp.whereStr += " and pay_by_stage_flag = :ex_stage_flag ";
        setString("ex_stage_flag", wp.itemStr("ex_stage_flag"));
      }
    }
    if (!empty(wp.itemStr("ex_idno"))) {
      wp.whereStr += " and id_no like :ex_idno ";
      setString("ex_idno", wp.itemStr("ex_idno") + "%");
    }
    if (!empty(wp.itemStr("ex_end_bal"))) {
      wp.whereStr += " and end_bal >= :ex_end_bal ";
      setString("ex_end_bal", wp.itemStr("ex_end_bal"));
    }
    if (!empty(wp.itemStr("ex_liab_comp_status"))) {
      if (wp.itemStr("ex_liab_comp_status").equals("R")) {
        wp.whereStr +=
            " and liab_comp_status in ('R1','R2','R3','R4','R5','R6','R7','R8','R9','R10','R11','R12','R13','R14') ";
      } else if (wp.itemStr("ex_liab_comp_status").equals("L")) {
        wp.whereStr +=
            " and liab_comp_status in ('L1','L2','L3','L4','L5','L6','L7','L8','L9','L10','L11') ";
      } else {
        wp.whereStr += " and liab_comp_status = :ex_liab_comp_status ";
        setString("ex_liab_comp_status", wp.itemStr("ex_liab_comp_status"));
      }
    }
    if (!empty(wp.itemStr("ex_mcode"))) {
      if (wp.itemStr("ex_mcode").equals("13")) {
        wp.whereStr += " and mcode > 12 ";
      } else {
        wp.whereStr += " and mcode = :ex_mcode ";
        setString("ex_mcode", wp.itemStr("ex_mcode"));
      }
    }
    if (!empty(wp.itemStr("ex_last_pay_date"))) {
      wp.whereStr += " and last_pay_date <= :ex_last_pay_date ";
      setString("ex_last_pay_date", wp.itemStr("ex_last_pay_date"));
    }
  }
}
