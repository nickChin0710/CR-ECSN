/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-02  V1.00.00  Andy Liu   program initial                * 
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*                             *
* 109-08-12  V1.00.03  JeffKung   修改rsk_type的種類                               *
* 109-09-14  V1.00.04  JeffKung   移除post_flag的條件                             *
* 109-09-21  V1.00.05  JeffKung   order by cardNo+referenceNo      *
******************************************************************************/
package dbbr01;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import java.util.Calendar;
import java.util.Date;

public class Dbbr0010 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbbr0010";

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
	sqlParm.clear();
    String cardNo = wp.itemStr("ex_card_no");
    String billType = wp.itemStr("ex_bill_type");
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String batchno = wp.itemStr("ex_batchno");
    String kind = wp.itemStr("ex_kind");
    // 固定條件
    wp.whereStr = "where 1=1 ";
    //wp.whereStr += "and decode(curr_post_flag,'','N',curr_post_flag) = 'N' ";     //2020/09/14就算是入到dbb_bill還是要可以查詢列印
    // 自選條件
    wp.whereStr += sqlCol(cardNo, "card_no", "like%");
    wp.whereStr += sqlCol(billType, "bill_type", "like%");
    wp.whereStr += sqlCol(batchno, "batch_no", "like%");
    wp.whereStr += sqlStrend(date1, date2, "this_close_date");
    switch (kind) {
      case "0":
        wp.whereStr += "and decode(doubt_type,'','K',doubt_type) != 'K' "
            + "and decode(rsk_type,'','K',rsk_type) = 'K'";
        break;
      case "1":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '1' ";
        break;
      case "2":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '2' ";
        break;
      case "3":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '3' ";
        break;
      case "4":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '4' ";
        break;
      case "5":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '5' ";
        break;
      case "6":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '6' ";
        break;
      case "7":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '7' ";
        break;
      case "8":
        wp.whereStr += "and decode(rsk_type,'','K',rsk_type) = '8' ";
        break;
      case "a":
        wp.whereStr += "and (decode(doubt_type,'','K',doubt_type) != 'K' "
            + "or (decode(rsk_type,'','K',rsk_type) != 'K' "
            + "and decode(rsk_type,'','K',rsk_type) != '9')) ";
        break;
      case "b":
        wp.whereStr += "and decode(doubt_type,'','K',doubt_type) = 'K' "
            + "and decode(rsk_type,'','K',rsk_type) = 'K' ";
        break;
      case "c":
        wp.whereStr += "and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) = 'Y' "
            + "and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) = 'Y' "
            + "and decode(err_chk_ok_flag,'','N',err_chk_ok_flag) = 'Y' ";
        break;
    }

    // wp.whereStr = ls_where;
    // setParameter();
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

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;
    wp.selectSQL = "card_no, " + "reference_no, " + "purchase_date, " + "source_amt, "
        + "mcht_chi_name, " + "mcht_city, " + "mcht_country, " + "dest_amt, " + "bill_type, "
        + "substr(bill_type,1,2) as db_bill_type, " + "batch_no, " + "mcht_eng_name, "
        + "this_close_date, " + "rsk_type," + "txn_code ";
    wp.daoTable = " dbb_curpost ";
    wp.whereOrder = " order by card_no , reference_no ";

    // System.out.println(" select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);

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
    int rowct = 0;
    String txnCode = "";
    double destAmt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowct += 1;
      wp.colSet(ii, "group_ct", "1");
      txnCode = wp.colStr(ii, "txn_code");
      destAmt = wp.colNum(ii, "dest_amt");
      if (txnCode.equals("06") || txnCode.equals("17") || txnCode.equals("29")
          || txnCode.equals("25") || txnCode.equals("27") || txnCode.equals("28")) {
        destAmt = 0 - destAmt;
      }
      wp.colSet(ii, "wk_dest_amt", numToStr(destAmt, "###"));
    }
    wp.colSet("row_ct", intToStr(rowct));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String cardNo = wp.itemStr("ex_card_no");
    String billType = wp.itemStr("ex_bill_type");
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String batchno = wp.itemStr("ex_batchno");
    String kind = wp.itemStr("ex_kind");

    String aLL = "列表條件  ";
    // ex_bank_no分行
    if (empty(cardNo) == false) {
      aLL += " [卡號 ]: " + cardNo;
    }
    // ex_date入帳日期
    if (empty(date1) == false || empty(date2) == false) {
      aLL += "  [入帳日期] : ";
      if (empty(date1) == false) {
        aLL += date1 + " 起 ";
      }
      if (empty(date2) == false) {
        aLL += " ~ " + date2 + " 迄 ";
      }
    }
    // ex_bill_type請款單位
    if (empty(billType) == false) {
      aLL += " [請款單位] : " + billType;
    }

    // ex_batchno批號
    if (empty(batchno) == false) {
      aLL += " [批號] : " + batchno;
    }

    // ex_kind類別
    aLL += " [類別]: " + kind;

    reportSubtitle = aLL;

  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
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
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = progName + ".xlsx";
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
    try {
      // dddw_bank_no
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_bill_type");
      dddwList("dddw_billuint", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");

    } catch (Exception ex) {
    }
  }

}
