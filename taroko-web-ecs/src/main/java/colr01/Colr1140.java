/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/19  V1.00.00   phopho     program initial                           *
*  108/12/19  V1.00.01   phopho     change table: prt_branch -> gen_brn       *
*  109-05-06  V1.00.02  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package colr01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr1140 extends BaseReport {
  CommString commString = new CommString();
  String mProgName = "colr1140";
  String[] strCde = null;
  String[] strTxt = null;

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

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    if (empty(strAction)) {
      // wp.col_set("exRegBankNo", "109");
    }
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
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exFileDateS");
    String lsDate2 = wp.itemStr("exFileDateE");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[資料轉入日期-起迄]  輸入錯誤");
      return false;
    }
    if (empty(wp.itemStr("exFileDateS")) && empty(wp.itemStr("exFileDateE"))
        && empty(wp.itemStr("exId"))) {
      alertErr2("請輸入 身分證ID 或 資料轉入日期");
      return false;
    }

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("exFileDateS")) == false) {
      wp.whereStr += " and pay_data.file_date >= :file_dates ";
      setString("file_dates", wp.itemStr("exFileDateS"));
    }
    if (empty(wp.itemStr("exFileDateE")) == false) {
      wp.whereStr += " and pay_data.file_date <= :file_datee ";
      setString("file_datee", wp.itemStr("exFileDateE"));
    }
    if (empty(wp.itemStr("exId")) == false) {
      wp.whereStr += " and pay_data.id_no = :id_no ";
      setString("id_no", wp.itemStr("exId"));
    }
    if (empty(wp.itemStr("exBankCode")) == false) {
      wp.whereStr += " and pay_data.bank_code = :bank_code ";
      setString("bank_code", wp.itemStr("exBankCode"));
    }
    if (empty(wp.itemStr("exRegBankNo")) == false) {
      wp.whereStr += " and pay_data.reg_bank_no = :reg_bank_no ";
      setString("reg_bank_no", wp.itemStr("exRegBankNo"));
    }
    if (empty(wp.itemStr("exAcctStatus")) == false) {
      wp.whereStr += " and act_acno.acct_status = :acct_status ";
      setString("acct_status", wp.itemStr("exAcctStatus"));
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

    wp.selectSQL = " pay_data.*, " + " decode(act_acno.acct_status, '4','4','1') AS acct_status ";

    wp.daoTable = "(SELECT col_liac_pay_dtl.p_seqno, " + " col_liac_pay_dtl.file_date, "
        + " col_liac_pay_dtl.liac_seqno, " + " col_liac_pay_dtl.id_no, "
        + " col_liac_pay_dtl.acct_type, " + " col_liac_pay_dtl.allocate_amt, "
        + " col_liac_pay_dtl.pay_date, " + " col_liac_pay_dtl.pay_seqno, "
        + " col_liac_pay_dtl.reg_bank_no, " + " col_liac_pay_dtl.per_allocate_amt, "
        + " decode(col_liac_pay_dtl.proc_flag,'1','Y-成功','N-失敗') as proc_flag, "
        + " col_liac_pay.bank_code " + " FROM col_liac_pay_dtl, col_liac_pay "
        + " WHERE (col_liac_pay_dtl.liac_seqno = col_liac_pay.liac_seqno) "
        + " AND (col_liac_pay_dtl.pay_seqno=col_liac_pay.pay_seqno) "              //20230511 sunny add
        + " AND (col_liac_pay_dtl.file_date = col_liac_pay.file_date)) pay_data "
        // + " LEFT JOIN act_acno ON pay_data.p_seqno = act_acno.p_seqno ";
        + " LEFT JOIN act_acno ON pay_data.p_seqno = act_acno.acno_p_seqno ";

    wp.whereOrder = " order by pay_data.file_date, pay_data.id_no ";

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
    wfColLiabBankName();
    String wkData = "";
    int sumIdCnt = 0;
    double sumAllocateAmt = 0;
    double totalPerAllocateAmt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "bank_code");
      // wp.col_set(ii,"tt_bank_code", wf_ColLiabBankName(ss));
      wp.colSet(ii, "tt_bank_code", commString.decode(wkData, strCde, strTxt));

      if (!wp.colStr(ii, "id_no").equals(wp.colStr(ii + 1, "id_no"))) {
        sumIdCnt++;
      }

      sumAllocateAmt += wp.colNum(ii, "allocate_amt");

      if (!wp.colStr(ii, "liam_seqno").equals(wp.colStr(ii + 1, "liam_seqno"))) {
        totalPerAllocateAmt += wp.colNum(ii, "per_allocate_amt");
      }
    }
    wp.colSet("sum_id_cnt", sumIdCnt + "");
    wp.colSet("sum_allocate_amt", sumAllocateAmt + "");
    wp.colSet("sum_tol_cnt", numToStr(wp.selectCnt, ""));
    wp.colSet("total_per_allocate_amt", totalPerAllocateAmt + "");
  }

  void wfColLiabBankName() throws Exception {
    String lsSql = "select bank_code, bank_code||' '||bank_name bank_name "
        + "from col_liab_bank order by bank_code ";
    sqlSelect(lsSql);
    if (sqlRowNum < 0)
      return;
    strCde = new String[sqlRowNum];
    strTxt = new String[sqlRowNum];
    for (int ii = 0; ii < sqlRowNum; ii++) {
      strCde[ii] = sqlStr(ii, "bank_code");
      strTxt[ii] = sqlStr(ii, "bank_name");
    }
  }

  // String wf_ColLiabBankName(String idcode) throws Exception {
  // String rtn="";
  // String ls_sql = "select bank_code||' '||bank_name bank_name from col_liab_bank "
  // + "where bank_code= :bank_code ";
  // setString("bank_code", idcode);
  //
  // sqlSelect(ls_sql);
  // if (sql_nrow == 0) {
  // rtn=idcode;
  // } else {
  // rtn=sql_ss("bank_name");
  // }
  // return rtn;
  // }

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

    String cond1 = "資料轉入日期: " + commString.strToYmd(wp.itemStr("exFileDateS")) + " -- "
        + commString.strToYmd(wp.itemStr("exFileDateE"));
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
