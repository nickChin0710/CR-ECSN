/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-12  V1.00.00  Max Lin    program initial                            *
* 108-11-26  V1.00.00  Amber      CDR.id_no -→ CI.id_no                      *
* 109-04-23  V1.00.01  shiyuqi    updated for project coding standard        *
* 109-01-04  V1.00.01  shiyuqi    修改无意义命名                             *
* 111-10-27  V1.00.02  Simon      sync codes with mega                       *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr3010 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "actr3010";

  String condWhere = "";

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
    String exAcctMonth = wp.itemStr("ex_acct_month");
    String exID = wp.itemStr("ex_id");

    // 帳戶年月或身分證字號 擇一即可(Linda, 20180907)
    if (empty(exAcctMonth) == true && empty(exID) == true) {
      alertErr2("請指定查詢條件");
      return false;
    }

    /*
     * if (empty(ex_acct_month) == true) { err_alert("請輸入帳戶年月"); return false; }
     */

    // 固定條件
    String lsWhere = " where 1=1 ";

    if (empty(exAcctMonth) == false) {
      lsWhere += " and CDR.acct_month = :ex_acct_month ";
      setString("ex_acct_month", exAcctMonth);
    }

    if (empty(exID) == false) {
      lsWhere += " and CI.id_no = :ex_id ";
      setString("ex_id", exID);
    }

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

    wp.sqlCmd = "select CDR.crt_date, " + "CDR.acct_type, " + "AA.acct_key, " + "CI.chi_name, "
        + "(case when CDR.revolve_int_sign = '-' then '減' else '加' end) || ' ' || CDR.revolve_int_rate as revolve_int_rate, "
        + "(CDR.revolve_rate_s_month || '--' || CDR.revolve_rate_e_month) as revolve_mm, "
        + "(case when CDR.new_revolve_int_sign = '-' then '減' else '加' end) || ' ' || CDR.new_revolve_int_rate as new_revolve_int_rate, "
        + "(CDR.new_revolve_rate_s_month || '--' || CDR.new_revolve_rate_e_month) as new_revolve_mm "
        + "from cyc_duplicate_rate as CDR "
        + "left join act_acno as AA on CDR.p_seqno = AA.acno_p_seqno "
        + "left join crd_idno as CI on AA.id_p_seqno = CI.id_p_seqno " + wp.whereStr
        + " order by CDR.crt_date, CDR.acct_type, AA.acct_key ";

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
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
    }

    wp.colSet("row_ct", intToStr(rowCt));
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // -cond-
      String exAcctMonth = wp.itemStr("ex_acct_month");
      String exID = wp.itemStr("ex_id");

      String cond1 = "帳戶年月: " + exAcctMonth + "  身分證字號: " + exID;
      wp.colSet("cond_1", cond1);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      xlsx.sheetName[0] = "帳戶利率不一致明細表";
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
      // dddw_acct_type
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

    } catch (Exception ex) {
    }
  }

}

