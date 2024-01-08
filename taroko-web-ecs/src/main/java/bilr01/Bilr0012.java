/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-20  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-03-14  V1.00.01  Andy       Update dddw_list merchant UI               *
* 109-04-27  V1.00.02  shiyuqi       updated for project coding standard     *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
* 110-10-18  V1.00.04  Yang Bo    joint sql replace to parameters way        *
* 111-05-26  V1.00.05  Ryan       dddwSelect up bil_prod union bil_prod_nccc *
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF2;

public class Bilr0012 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "bilr0012";

  String condWhere = "";
  String reportSubtitle = "";

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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    } else if (eqIgno(wp.buttonCode, "ItemChange")) {
      /* TEST */
      strAction = "ItemChange";
      itemChange();
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
    String mchtNo = wp.itemStr("ex_merchant");
    String productNo = wp.itemStr("ex_product_no");

    String lsWhere = " where 1=1 ";

    if (empty(mchtNo) == false) {
      lsWhere += " and mcht_no = :mcht_no";
      setString("mcht_no", mchtNo);
    }

    if (empty(productNo) == false) {
      lsWhere += " and product_no = :product_no";
      setString("product_no", productNo);
    }

    wp.whereStr = lsWhere;
    setParameter();
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

//    if (getWhereStr() == false)
//      return;

    wp.selectSQL = "" + "mcht_no , " + "mcht_chi_name, " + "product_no , " + "product_name , "
        + " sum(tot_amt * qty -exchange_amt) db_tot_amt , "
        + " sum(unit_price * (install_tot_term - install_curr_term) + "
        + " remd_amt+decode(install_curr_term,0,first_remd_amt,0)) db_rem , "
        + " sum(decode(mcht_no,'',0,1)) mcht_cnt ";
    wp.daoTable = " bil_contract ";
    wp.whereOrder = " group by mcht_no, mcht_chi_name, product_no, product_name order by mcht_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr
    // +wp.whereOrder);
    // wp.daoTable);
    wp.sqlCmd = "select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr;
    wp.pageCountSql = "select count(*) from (";
    wp.pageCountSql += wp.sqlCmd + wp.whereOrder;
    wp.pageCountSql += ")";

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

  void listWkdata() {
    int rowCt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      // wp.col_set(ii,"row_ct", int_2Str(row_ct));
    }
    wp.colSet("user_id", wp.loginUser);
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String mchtNo = wp.itemStr("ex_merchant");
    String productNo = wp.itemStr("ex_product_no");
    String title = "";

    if (!empty(mchtNo)) {
      title += "特店代號 : " + mchtNo;
    }

    if (!empty(productNo)) {
      title += "商品代號 : " + productNo;
    }

    reportSubtitle = title;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
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
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF2 pdf = new TarokoPDF2();
    // 表頭固定欄位
    pdf.fixHeader[0] = "user_id";
    pdf.fixHeader[1] = "cond_1";


    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
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
      // dddw_mcht_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_merchant");
      // dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name", "where 1=1 and
      // loan_flag = 'N' order by mcht_no");
      String exMchtNo = wp.itemStr("ex_merchant");
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_product_no");
      if(!empty(exMchtNo)) {
			String sqlStr = "select product_no,product_no||'_'||product_name as product_name from bil_prod_nccc where mcht_no = :mcht_no1 "
					+ " union select product_no,product_no||'_'||product_name as product_name from bil_prod where mcht_no = :mcht_no2 "
					+ " order by product_no ";
			setString("mcht_no1", exMchtNo);
			setString("mcht_no2", exMchtNo);
			sqlSelect(sqlStr);
			wp.colSet("dddw_product_no", this.dddwOption("product_no", "product_name"));
      }
      // 為下面dddwList方法傳參數
//      setString("mcht_no", exMchtNo);
//      this.dddwList("dddw_product_no", "bil_prod", "product_no", "product_name",
//          "where 1=1 and mcht_no = :mcht_no order by product_no");
    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }

  void itemChange() throws Exception {

  }
}
