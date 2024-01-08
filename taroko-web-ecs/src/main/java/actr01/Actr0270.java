/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-25  V1.00.00  Max Lin    program initial                            *
* 109-04-23  V1.00.01  shiyuqi    updated for project coding standard        *
* 109-05-13  V1.00.02  tanwei     updated for project coding standard        *   
* 109-01-04  V1.00.03  shiyuqi    修改无意义命名                             *
* 111-10-27  V1.00.04  Simon      sync codes with mega                       *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0270 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "actr0270";

  String condWhere = "";
  String sumWhere = ""; // 金額合計 (Linda, 20180912)

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

  private boolean getWhereStr() throws Exception {
    String exDateS = wp.itemStr("ex_date_S");
    String exDateE = wp.itemStr("ex_date_E");

    if (empty(exDateS) == true && empty(exDateE) == true) {
      alertErr2("請輸入入帳日期");
      return false;
    }

    // 固定條件
    String lsWhere = " where 1=1 ";

    if (empty(exDateS) == false) {
      lsWhere += " and APE.crt_date >= :ex_date_S ";
      setString("ex_date_S", exDateS);
    }

    if (empty(exDateE) == false) {
      lsWhere += " and APE.crt_date <= :ex_date_E ";
      setString("ex_date_E", exDateE);
    }

    // 金額合計 (Linda, 20180912)--------------start
    // 固定條件
    sumWhere = " where 1=1 ";
    if (empty(exDateS) == false) {
      sumWhere += sqlCol(exDateS, "APE.crt_date", ">=");
    }

    if (empty(exDateE) == false) {
      sumWhere += sqlCol(exDateE, "APE.crt_date", "<=");
    }
    // 金額合計------------------------------------end


    wp.whereStr = lsWhere;

    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.sqlCmd = "select (APE.batch_no || '-' || APE.serial_no) as batch_no_serial, "
        + "(APE.acct_type || '-' || AA.acct_key) as acct_type_key, " + "APE.pay_card_no, "
        + "APE.pay_amt, " + "APE.pay_date, " + "APE.crt_date, " + "APE.payment_type, "
        + "APE.error_reason, " + "APE.error_remark, " + "APE.confirm_flag, "
        + "APE.duplicate_mark, " + "APE.id_no, " + "APE.branch, "
        + "nvl(CI.chi_name, CC.chi_name) as chi_name " + "from act_pay_error as APE "
        + "left join act_acno as AA on APE.p_seqno = AA.acno_p_seqno "
        + "left join crd_idno as CI on AA.id_p_seqno = CI.id_p_seqno "
        + "left join crd_corp as CC on AA.corp_p_seqno = CC.corp_p_seqno " + wp.whereStr
        + " order by APE.crt_date, APE.batch_no, APE.serial_no ";

    wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";

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

    // --
    // 金額合計 (Linda, 20180912)
    sum();

    wp.setPageValue();
    listWkdata();
  }

  // 金額合計 (Linda, 20180912)
  void sum() throws Exception {
    String sql1 = "select sum(APE.pay_amt) as tot_amt " + "from act_pay_error as APE "
        + "left join act_acno as AA on APE.p_seqno = AA.p_seqno "
        + "left join crd_idno as CI on AA.id_p_seqno = CI.id_p_seqno "
        + "left join crd_corp as CC on AA.corp_p_seqno = CC.corp_p_seqno " + sumWhere;
    sqlSelect(sql1);

    wp.colSet("tot_amt", sqlStr("tot_amt"));

  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    // int sum_pay_amt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      // sum_pay_amt += Integer.parseInt(wp.col_ss(ii, "pay_amt"));
    }

    wp.colSet("row_ct", intToStr(rowCt));
    // wp.col_set("sum_pay_amt", int_2Str(sum_pay_amt));
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // -cond-
      String exDateS = wp.itemStr("ex_date_S");
      String exDateE = wp.itemStr("ex_date_E");

      String cond1 = "入帳日期: " + exDateS + " ~ " + exDateE;
      wp.colSet("cond_1", cond1);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      xlsx.sheetName[0] = "繳款資料查核錯誤報表";
      queryFunc();
      wp.setListCount(1);
      log("Summ: rowcnt:" + wp.listCount[1]);
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
    String cond1 = "PDFTEST: ";
    wp.colSet("cond_1", cond1);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 30;
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
      // dddw_bank_id
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_bank_id");
      dddwList("dddw_bank_id", " PTR_BANKCODE", "BC_BANKCODE", "BC_ABNAME",
          "where 1=1 order by BC_BANKCODE");

    } catch (Exception ex) {
    }
  }

}

