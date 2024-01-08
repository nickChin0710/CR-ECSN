/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-26  V1.00.00  Andy Liu   program initial                            *
* 108-12-20  v1.00.03  Andy       Update ptr_branch=>gen_brn                 *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-07-27  V1.00.05  Bo Yang    修復查詢錯誤BUG                              *
******************************************************************************/
package mktq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktq0245 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "mktq0245";

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
    }

    dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    // wp.col_set("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
    String exGroupcode = wp.itemStr("ex_groupcode");
    String exPurchType = wp.itemStr("ex_purch_type");
    String exYymm1 = wp.itemStr("ex_yymm1");
    String exYymm2 = wp.itemStr("ex_yymm2");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件
    lsWhere += "and reward_type='2' ";
    // user搜尋條件
    lsWhere += sqlCol(exGroupcode, "group_code");
    lsWhere += sqlCol(exPurchType, "purch_date_type");
    lsWhere += sqlStrend(exYymm1, exYymm2, "rew_yyyymm");


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

    wp.selectSQL = "group_code, " + "(SELECT group_code || '_' || group_name "
        + " FROM ptr_group_code " + " WHERE group_code = mkt_re_group.group_code) "
        + " AS db_group_code, "
        + "mcht_group_id||' '||nvl((select mcht_group_desc from mkt_mcht_gp where MCHT_GROUP_ID =mkt_re_group.MCHT_GROUP_ID),'') as mcht_group_id, "
        + "sum (total_purch_cnt) AS db_total_purch_cnt, "
        + "sum (total_purch_amt) AS db_total_purch_amt, "
        + "sum (avg_purch_amt) AS db_avg_purch_amt, " + "sum (discount_amt) AS db_discount_amt, "
        + "sum (bl_amt_in) AS db_bl_amt_in, " + "sum (bl_amt - bl_amt_in) AS db_bl_amt_out, "
        + "sum (ca_amt) AS db_ca_amt, " + "sum (id_amt) AS db_id_amt, "
        + "sum (ao_amt) AS db_ao_amt, " + "sum (it_amt_in) AS db_it_amt_in, "
        + "sum (it_amt - it_amt_in) AS db_it_amt_out, " + "sum (ot_amt) AS db_ot_amt, "
        + "sum (exp_tax_amt) AS db_exp_tax_amt, "
        + "sum (bl_amt_in) + sum (it_amt_in) AS db_bl_it_in, "
        + "sum (bl_amt - bl_amt_in) + sum (it_amt - it_amt_in) AS db_bl_it_out ";
    wp.daoTable = " mkt_re_group ";
    wp.whereOrder = " group by group_code,mcht_group_id " + " order by group_code,mcht_group_id ";

    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);

    // 頁面重新計算筆數
    wp.pageCountSql = "select count(*) from (select " + wp.selectSQL + " from " + wp.daoTable
        + wp.whereStr + wp.whereOrder + ")";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

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
    double sumTotalPurchCnt = 0, sumTotalPurchAmt = 0, sumAvgPurchAmt = 0, sum_discountAmt = 0,
        sumBlAmtIn = 0, sumBlAmtOut = 0, sumCaAmt = 0, sumIdAmt = 0, sumAoAmt = 0, sumItAmtIn = 0,
        sumItAmtOut = 0, sumOtAmt = 0, sumExpTaxAmt = 0, sumBlItIn = 0, sumBlItOut = 0;
    int sel_ct = wp.selectCnt;
    for (int ii = 0; ii < sel_ct; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");
      sumTotalPurchCnt += wp.colNum(ii, "db_total_purch_cnt");
      sumTotalPurchAmt += wp.colNum(ii, "db_total_purch_amt");
      sumAvgPurchAmt += wp.colNum(ii, "db_avg_purch_amt");
      sum_discountAmt += wp.colNum(ii, "db_discount_amt");
      sumBlAmtIn += wp.colNum(ii, "db_bl_amt_in");
      sumBlAmtOut += wp.colNum(ii, "db_bl_amt_out");
      sumCaAmt += wp.colNum(ii, "db_ca_amt");
      sumIdAmt += wp.colNum(ii, "db_id_amt");
      sumAoAmt += wp.colNum(ii, "db_ao_amt");
      sumItAmtIn += wp.colNum(ii, "db_it_amt_in");
      sumItAmtOut += wp.colNum(ii, "db_it_amt_out");
      sumOtAmt += wp.colNum(ii, "db_ot_amt");
      sumExpTaxAmt += wp.colNum(ii, "db_exp_tax_amt");
      sumBlItIn += wp.colNum(ii, "db_bl_it_in");
      sumBlItOut += wp.colNum(ii, "db_bl_it_out");
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("sum_total_purch_cnt", numToStr(sumTotalPurchCnt, "###"));
    wp.colSet("sum_total_purch_amt", numToStr(sumTotalPurchAmt, "###"));
    wp.colSet("sum_avg_purch_amt", numToStr(sumAvgPurchAmt, "###"));
    wp.colSet("sum_discount_amt", numToStr(sum_discountAmt, "###"));
    wp.colSet("sum_bl_amt_in", numToStr(sumBlAmtIn, "###"));
    wp.colSet("sum_bl_amt_out", numToStr(sumBlAmtOut, "###"));
    wp.colSet("sum_ca_amt", numToStr(sumCaAmt, "###"));
    wp.colSet("sum_id_amt", numToStr(sumIdAmt, "###"));
    wp.colSet("sum_ao_amt", numToStr(sumAoAmt, "###"));
    wp.colSet("sum_it_amt_in", numToStr(sumItAmtIn, "###"));
    wp.colSet("sum_it_amt_out", numToStr(sumItAmtOut, "###"));
    wp.colSet("sum_ot_amt", numToStr(sumOtAmt, "###"));
    wp.colSet("sum_exp_tax_amt", numToStr(sumExpTaxAmt, "###"));
    wp.colSet("sum_bl_it_in", numToStr(sumBlItIn, "###"));
    wp.colSet("sum_bl_it_out", numToStr(sumBlItOut, "###"));

  }

  void subTitle() {
    String exGroupcode = wp.itemStr("ex_groupcode");
    String exPurchType = wp.itemStr("ex_purch_type");
    String exYymm1 = wp.itemStr("ex_yymm1");
    String eYymm2 = wp.itemStr("ex_yymm2");

    String allStr = "列表條件 : ";
    // 批號
    if (empty(exGroupcode) == false) {
      allStr += " [團體代號] : " + exGroupcode;
    }
    String[] cde1 = new String[] {"0", "1"};
    String[] txt1 = new String[] {"依關帳年月", "依消費日"};
    allStr += " " + commString.decode(exPurchType, cde1, txt1);

    // 統計期間
    if (empty(exYymm1) == false || empty(eYymm2) == false) {
      allStr += " [統計期間] : ";
      if (empty(exYymm1) == false) {
        allStr += exYymm1 + " 起 ";
      }
      if (empty(eYymm2) == false) {
        allStr += " ~ " + eYymm2 + " 迄 ";
      }
    }

    reportSubtitle = allStr;
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
      wp.fileMode = "N";
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
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 30;
    // pdf.pageVert= true; //直印
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
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_groupcode");
      dddwList("dddw_groupcode", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");

      // dddw_branch
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_branch");
      // dddw_list("dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

