/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-25  V1.00.00  Andy Liu   program initial                            *
* 109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*        
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
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

public class Dbbr9002 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbbr9002";

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
    // wp.col_set("ex_accttype","90");
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

    wp.sqlCmd = "SELECT bill_type, " + "total_amt, " + "total_cnt, " + "total_cnt_card, "
        + "total_cnt_acno, " + "total_fee," + "decode(total_cnt,0,0,round(total_amt / total_cnt,4)) as cnt_avg_amt, "
        + "decode(total_cnt_card,0,0,round(total_amt / total_cnt_card,4)) as card_avg_amt, "
        + "decode(total_cnt_acno,0,0,round(total_amt / total_cnt_acno,4)) as acno_avg_amt,"
        + "(select sum(total_cnt) from dbb_m002 where col_date =:col_date) as sum_total_cnt, "
        + "(select sum(total_amt) from dbb_m002 where col_date =:col_date) as sum_total_amt, "
        + "(select sum(total_fee) from dbb_m002 where col_date =:col_date) as sum_total_fee,"
        + "'' wk_temp " + "FROM dbb_m002 where 1=1 " + "and col_date =:col_date "
        + "order by bill_type ";
    setString("col_date", coldate);

    // wp.pageCount_sql = "select count(*) from ( " + wp.sqlCmd+" )";
    // setParameter();
    // System.out.println(wp.sqlCmd);

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
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String billType = wp.colStr(ii, "bill_type");
      String[] cde = new String[] {"1", "2", "3"};
      String[] txt = new String[] {"國內消費", "國外消費", "國外提現"};
      wp.colSet(ii, "bill_type", commString.decode(billType, cde, txt));
    }
    wp.colSet("sum_tot_cnt1", wp.colStr(1, "sum_tot_cnt"));
    wp.colSet("sum_tot_amt1", wp.colStr(1, "sum_tot_amt"));
    wp.colSet("sum_total_fee1", wp.colStr(1, "sum_total_fee"));
    wp.colSet("user_id", wp.loginUser);
  }

  void xlsPrint() {

  }

  void pdfPrint() throws Exception {
    wp.reportId = progName;
    // -cond-
    String ex_coldate = wp.itemStr("ex_coldate");
    String cond1 = "統計期間：  ";
    cond1 += ex_coldate;
    wp.colSet("cond_1", cond1);
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y"; // server 產生PDF file 再由user下載或瀏覽
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    // pdf.pageVert = true; // 直印
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
