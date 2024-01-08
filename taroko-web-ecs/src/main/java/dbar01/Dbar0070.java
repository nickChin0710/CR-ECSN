
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-04  V1.00.01  Ryan       program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱* 
* 109-12-23  V1.00.03  Justin       zz -> commString
******************************************************************************/

package dbar01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbar0070 extends BaseReport {
  String progName = "dbar0070";
  CommString commString = new CommString();

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
  public void dddwSelect() {
    /*
     * try { wp.initOption = "--"; wp.optionKey = wp.item_ss("ex_bno1");
     * dddw_list("dddw_branch1","ptr_branch" , "branch", "branch_name",
     * " where 1=1 order by branch"); wp.optionKey = wp.item_ss("ex_bno2");
     * dddw_list("dddw_branch2","ptr_branch" , "branch", "branch_name",
     * " where 1=1 order by branch"); } catch(Exception ex){}
     */
  }

  @Override
  public void initPage() {
    wp.colSet("tol_send_cnt", "0");
    wp.colSet("tol_send_amt", "0");
    wp.colSet("tol_receive_fal_cnt", "0");
    wp.colSet("tol_receive_fal_amt", "0");
    wp.colSet("tol_receive_suc_cnt", "0");
    wp.colSet("tol_receive_suc_amt", "0");
  }

  private int getWhereStr() throws Exception {
    String date1 = wp.itemStr("ex_crdate_s");
    String date2 = wp.itemStr("ex_crdate_e");

    if (this.chkStrend(date1, date2) == false) {
      alertErr2("[扣款日期-起迄]  輸入錯誤");
      return -1;
    }
    if (empty(wp.itemStr("ex_crdate_s")) && empty(wp.itemStr("ex_crdate_e"))) {
      alertErr("[扣款日期-起迄] 不能全部空白");
      return -1;
    }
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_type")) == false) {
      wp.whereStr += " and proc_type = :ex_type ";
      setString("ex_type", wp.itemStr("ex_type"));
    }

    if (empty(wp.itemStr("ex_crdate_s")) == false) {
      wp.whereStr += " and deduct_date >= :ex_crdate_s ";
      setString("ex_crdate_s", wp.itemStr("ex_crdate_s"));
    }
    if (empty(wp.itemStr("ex_crdate_e")) == false) {
      wp.whereStr += " and deduct_date <= :ex_crdate_e ";
      setString("ex_crdate_e", wp.itemStr("ex_crdate_e"));
    }

    return 1;
  }

  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " proc_type " + ", crt_time " + ", deduct_proc_type " + ", send_cnt "
        + ", send_amt " + ", receive_cnt " + ", receive_amt " + ", receive_time "
        + ", receive_suc_cnt " + ", receive_suc_amt " + ", receive_fal_cnt " + ", receive_fal_amt "
        + ", media_sum " + ", crt_date " + ", deduct_date " + ", receive_date ";

    wp.daoTable = " dba_deduct_ctl ";
    wp.whereOrder = "  ";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.colSet("tol_list", "0");
      alertErr(appMsg.errCondNodata);
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
      wp.reportId = progName;
      // -cond-
      /*
       * String ss = "生效年月: " + commString.ss_2ymd(wp.item_ss("ex_yymm1")) + " -- " +
       * commString.ss_2ymd(wp.item_ss("ex_yymm2")); wp.col_set("cond_1", ss);
       */
      /*
       * String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1")) + " -- " +
       * commString.ss_2ymd(wp.item_ss("ex_send_date1")); wp.col_set("cond_2", ss2);
       */

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
      xlsx.excelTemplate = progName + ".xlsx";

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
    wp.reportId = progName;
    // -cond-
    /*
     * String ss = "生效年月: " + commString.ss_2ymd(wp.item_ss("ex_yymm1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_yymm2")); wp.col_set("cond_1", ss);
     */
    String all = "扣款日期: " + commString.strToYmd(wp.itemStr("ex_crdate_s")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_crdate_e"));
    wp.colSet("cond_2", all);
    if (wp.itemStr("ex_type").equals("A")) {
      wp.colSet("cond_1", "查詢類別:扣款");
    } else {
      wp.colSet("cond_1", "查詢類別:回存");
    }
    wp.colSet("IdUser", wp.loginUser);
    if (getWhereStr() != 1) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.pageRows = 9999;
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = progName + ".xlsx";
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

  void listWkdata() {
    String procType = "";
    double tolSendAmt = 0, tolReceiveFalAmt = 0, tolReceiveSucAmt = 0;
    int tolSendCnt = 0, tolReceiveFalCnt = 0, tolReceiveSucCnt = 0;
    for (int i = 0; i < wp.selectCnt; i++) {

      procType = wp.colStr(i, "proc_type");
      wp.colSet(i, "tt_proc_type", commString.decode(procType, ",A,B", ",扣款,回存"));

      tolSendAmt += wp.colNum(i, "send_amt");
      tolReceiveFalAmt += wp.colNum(i, "receive_fal_amt");
      tolReceiveSucAmt += wp.colNum(i, "receive_suc_amt");
      tolSendCnt += wp.colNum(i, "send_cnt");
      tolReceiveFalCnt += wp.colNum(i, "receive_fal_cnt");
      tolReceiveSucCnt += wp.colNum(i, "receive_suc_cnt");
    }
    wp.colSet("tol_send_amt", tolSendAmt);
    wp.colSet("tol_receive_fal_amt", tolReceiveFalAmt);
    wp.colSet("tol_receive_suc_amt", tolReceiveSucAmt);
    wp.colSet("tol_send_cnt", tolSendCnt);
    wp.colSet("tol_receive_fal_cnt", tolReceiveFalCnt);
    wp.colSet("tol_receive_suc_cnt", tolReceiveSucCnt);
  }

}
