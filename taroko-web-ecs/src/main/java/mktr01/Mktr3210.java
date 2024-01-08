/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112/11/01  V1.00.00  Zuwei Su   program initial                            *
******************************************************************************/
package mktr01;

import java.io.InputStream;

import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktr3210 extends BaseEdit {
    CommDate commDate = new CommDate();
  InputStream inExcelFile = null;
  String mProgName = "mktr3210";

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

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    String exYyyymm = wp.itemStr("ex_yyyymm");
    String exIdNo = wp.itemStr("ex_id_no");

    if (empty(exYyyymm) == true) {
      alertErr2("查詢月份為必要輸入條件!!");
      return;
    }
    
    // count year data
    wp.sqlCmd = " SELECT sum(a.deduct_bp) AS deduct_bt_sum, sum(a.discount_value) AS discount_value_sum "
            + " FROM mkt_thsr_disc a                                 "
            + " LEFT JOIN crd_idno b ON a.id_p_seqno=b.id_p_seqno    "
            + " WHERE a.DEDUCT_TYPE = '1'         "
            + " AND a.trans_type !='R'            "
            + " AND a.error_code = '00'           ";
    wp.sqlCmd += sqlCol(exYyyymm.substring(0, 4), "SUBSTR(a.TRANS_DATE,1,4)");
    wp.sqlCmd += sqlCol(exYyyymm, "SUBSTR(a.TRANS_DATE,1,6)", "<=");
    pageQuery();
    int deductBtYearSum = 0;
    int discountValueYearSum = 0;
    if(sqlRowNum > 0) {
        deductBtYearSum += wp.colInt("deduct_bt_sum");
        discountValueYearSum += wp.colInt("discount_value_sum");
    }
    wp.colSet("deduct_bt_year_sum", deductBtYearSum);
    wp.colSet("discount_value_year_sum", discountValueYearSum);

    // query
    wp.sqlCmd = " SELECT a.trans_date, b.ID_NO, sum(a.deduct_bp) AS deduct_bt_sum, sum(a.discount_value) AS discount_value_sum "
            + " FROM mkt_thsr_disc a                                 "
            + " LEFT JOIN crd_idno b ON a.id_p_seqno=b.id_p_seqno    "
            + " WHERE a.DEDUCT_TYPE = '1'         "
            + " AND a.trans_type !='R'            "
            + " AND a.error_code = '00'           ";
    wp.sqlCmd += sqlCol(exYyyymm, "SUBSTR(a.TRANS_DATE,1,6)");
    if (empty(exIdNo) == false) {
      wp.sqlCmd += sqlCol(exIdNo, "b.id_no");
    }
    wp.sqlCmd += "group by a.trans_date, b.ID_NO ";
    wp.sqlCmd += "order by a.trans_date, b.ID_NO ";
    wp.pageCountSql = "select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql = ")";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      wp.colSet("deduct_bt_month_sum", 0);
      wp.colSet("discount_value_month_sum", 0);
      return;
    }
    wp.setPageValue();
    wp.colSet("user_id", wp.loginUser);
    
    // count month data
    int deductBtMonthSum = 0;
    int discountValueMonthSum = 0;
    for (int i = 0; i < sqlRowNum; i++) {
        deductBtMonthSum += wp.colInt(i, "deduct_bt_sum");
        discountValueMonthSum += wp.colInt(i, "discount_value_sum");
    }
    wp.colSet("deduct_bt_month_sum", deductBtMonthSum);
    wp.colSet("discount_value_month_sum", discountValueMonthSum);
  }

  void subTitle() {
      String twDate = commDate.twDate();
      String datetime = commDate.sysDatetime();
      String cndate = String.format("中華民國 %s年 %s月 %s日 %s", twDate.substring(0, twDate.length() - 4),
              twDate.substring(twDate.length() - 4, twDate.length() - 2),
              twDate.substring(twDate.length() - 2), datetime);
      wp.colSet("cndate", cndate);
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
      // 分頁欄位xlsx.breakField[0]-[9]
      // xlsx.breakField[0] ="corp_no";
      // xlsx.breakField[1] ="chi_name";

      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + "_excel.xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

      String[] opt = wp.itemBuff("opt");
      String opt_flag = "N";
      // 判斷是否有勾選資料
      if (!(opt == null) && !empty(opt[0]))
        opt_flag = "Y";

      if (opt_flag.equals("N")) {
        queryFunc();
      } else {
        pageQuery();
      }
      wp.setListCount(1);
      wp.colSet("user_id", wp.loginUser);
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
    wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    // queryRead();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
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
  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

}
