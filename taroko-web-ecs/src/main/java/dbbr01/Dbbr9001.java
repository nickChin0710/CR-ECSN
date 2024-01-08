/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-25  V1.00.00  Andy Liu   program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*           
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package dbbr01;

import java.io.InputStream;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbbr9001 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbbr9001";

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
  public void initPage() {
    // 設定初始搜尋條件值
    wp.colSet("ex_accttype", "90");
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

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
    String coldate = wp.itemStr("ex_coldate");
    String accttype = wp.itemStr("ex_accttype");
    String prelend = wp.itemStr("ex_prelend");
    String coldatePreMonth = preMonth(coldate);
    String coldatePreYear = preYear(coldate);

    wp.sqlCmd = "select " + "col_date, " + "acct_type, " + "rang_code, " + "rang_desc, "
        + "expn_qty, " + "crt_date, " + "db_qtyyear, " + "cash_adv, " + "sum_expn_qty, "
        + "(round(expn_qty /sum_expn_qty,4)*100 ) as wk_per, " + "db_qtymon, "
        + "(expn_qty - db_qtymon) wk_1 , " + "sum_db_qtymon, "
        + "(sum_expn_qty - sum_db_qtymon) as sum_eqmon, "
        + "(round((expn_qty  -  db_qtymon) / (sum_expn_qty -sum_db_qtymon) ,4)*100) as wk_permon, "
        + "(expn_qty - db_qtyyear) wk_2 , " + "sum_db_qtyyear, "
        + "(sum_expn_qty - sum_db_qtyyear) as sum_eqyear, "
        + "(round((expn_qty  -  db_qtyyear) / (sum_expn_qty -sum_db_qtyyear) ,4)*100) as wk_peryear "
        + "from ( " + "SELECT a.col_date, " + "a.acct_type, " + "a.rang_code, " + "a.rang_desc, "
        + "a.expn_qty, " + "a.crt_date, " + "a.cash_adv, "
        + "(select sum(expn_qty) from dbb_m001 where col_date = a.col_date AND	acct_type = a.acct_type and cash_adv =a.cash_adv ) as sum_expn_qty, "
        + "(SELECT b.expn_qty	FROM dbb_m001 b WHERE b.col_date =:ex_coldate_pre_month AND	b.acct_type = a.acct_type and b.rang_code = a.rang_code AND b.cash_adv = a.cash_adv) as db_qtymon, "
        + "(select sum(expn_qty) from dbb_m001 where col_date =:ex_coldate_pre_month AND acct_type = a.acct_type and cash_adv =a.cash_adv ) as sum_db_qtymon, "
        + "(SELECT c.expn_qty	FROM dbb_m001 c WHERE c.col_date =:ex_coldate_pre_year AND	c.acct_type = a.acct_type and c.rang_code = a.rang_code AND c.cash_adv = a.cash_adv) as db_qtyyear, "
        + "(select sum(expn_qty) from dbb_m001 where col_date =:ex_coldate_pre_year AND	acct_type = a.acct_type and cash_adv =a.cash_adv ) as sum_db_qtyyear "
        + "FROM dbb_m001 a " + "where col_date =:ex_coldate " + "and acct_type =:ex_accttype "
        + "and cash_adv =:ex_prelend " + "Order by acct_type, rang_code " + ")";
    setString("ex_coldate", coldate);
    setString("ex_accttype", accttype);
    setString("ex_prelend", prelend);
    setString("ex_coldate_pre_month", coldatePreMonth);
    setString("ex_coldate_pre_year", coldatePreYear);

    wp.pageCountSql = "select count(*) from ( " + wp.sqlCmd + " )";
    // setParameter();
    // System.out.println(wp.sqlCmd);
    // wp.daoTable);

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
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowct = 0;
    String txnCode = "", dbDeductProcType = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowct += 1;

    }
    wp.colSet("sum_expn_qty", wp.colStr(1, "sum_expn_qty"));
    wp.colSet("sum_eqmon", wp.colStr(1, "sum_eqmon"));
    wp.colSet("sum_eqyear", wp.colStr(1, "sum_eqyear"));
    wp.colSet("wk_per_total", "100");
    wp.colSet("row_ct", intToStr(rowct));
    wp.colSet("user_id", wp.loginUser);
  }

  // 往前推1個月
  public String preMonth(String date) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    Date startdate = (Date) sdf.parse(date);
    Calendar start = Calendar.getInstance();
    start.setTime(startdate);
    start.add(Calendar.MONTH, -1);
    String db_pre_month_date = sdf.format(start.getTime());
    return db_pre_month_date;
  }

  // 同期前1年
  public String preYear(String date) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    Date startdate = (Date) sdf.parse(date);
    Calendar start = Calendar.getInstance();
    start.setTime(startdate);
    start.add(Calendar.YEAR, -1);
    String db_pre_month_date = sdf.format(start.getTime());
    return db_pre_month_date;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      String batchno1 = wp.itemStr("ex_batchno1");
      String eonline = wp.itemStr("ex_online");
      String comboFlag = wp.itemStr("ex_combo_flag");
      String source = wp.itemStr("ex_source");
      String groupCode = wp.itemStr("ex_group_code");
      String exDateS = wp.itemStr("exDateS");
      String exDateE = wp.itemStr("exDateE");
      String checkResult = wp.itemStr("ex_check_result");
      switch (source) {
        case "0":
          source = "全部";
          break;
        case "1":
          source = "新製卡";
          break;
        case "2":
          source = "普昇金";
          break;
      }
      switch (checkResult) {
        case "0":
          checkResult = "全部";
          break;
        case "1":
          checkResult = "成功";
          break;
        case "2":
          checkResult = "不成功";
          break;
      }
      String cond1 = "批號 :" + batchno1 + "~" + batchno1 + " 線上製卡: " + eonline + " COMBO卡: "
          + comboFlag;
      wp.colSet("cond_1", cond1);
      String cond2 = "製卡來源: " + source + " 團體代碼: " + groupCode + "產生日期:" + exDateS + " ~ "
          + exDateE + "檢核結果:" + checkResult;
      wp.colSet("cond_2", cond2);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = progName + ".xlsx";

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
    wp.reportId = progName;
    // -cond-
    String coldate = wp.itemStr("ex_coldate");
    String accttype = wp.itemStr("ex_accttype");
    String prelend = wp.itemStr("ex_prelend");
    String cond1 = "統計期間：  ";
    String cond2 = "含國外提現： ";
    String cond3 = "帳戶類別： ";
    cond1 += coldate;
    cond2 += prelend;
    cond3 += accttype;
    wp.colSet("cond_1", cond1);
    wp.colSet("cond_2", cond2);
    wp.colSet("cond_3", cond3);
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y"; // server 產生PDF file 再由user下載或瀏覽
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageVert = true; // 直印
    pdf.pageCount = 25; // 頁面LIST筆數跳頁
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
      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");
    } catch (Exception ex) {
    }
  }

}
