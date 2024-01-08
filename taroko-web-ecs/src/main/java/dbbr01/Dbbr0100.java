/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-21  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr          
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

public class Dbbr0100 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbbr0100";

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
    // String sysdate1="";
    // sysdate1 = ss_mid(get_sysDate(),0,6)+"01";
    // //當月首日
    // wp.col_set("exDateS", sysdate1);
    // 系統日-1
    SimpleDateFormat befdate = new java.text.SimpleDateFormat("yyyyMMdd");
    Calendar cal2 = Calendar.getInstance();
    Date date2 = null;
    try {
      date2 = befdate.parse(getSysDate());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    cal2.setTime(date2);
    cal2.add(Calendar.DATE, -1);
    String exDateS = befdate.format(cal2.getTime());
    wp.colSet("exDateS", exDateS);
    wp.colSet("exDateE", getSysDate());
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String dbSign = wp.itemStr("ex_db_sign");

    wp.sqlCmd = "select " + "reference_no, " + "dbb_bill_dest_amt, " + "batch_no, " + "txn_code, "
        + "acq_member_id, " + "mcht_category, " + "dbb_ccas_dtl_dest_amt, " + "acq_id, "
        + "debit_acct_no, " + "dbb_ccas_dtl_rsk_type, " + "dbb_bill_rsk_type, "
        + "rsk_type_special, " + "db_sign, " + "dbb_ccas_dtl_purchase_date, " + "mcht_country, "
        + "post_date, " + "dbb_bill_purchase_date, " + "dbb_bill_auth_code, " + "dbb_bill_card_no, "
        + "dbc_card_card_no, " + "acct_no, " + "dbb_ccas_dtl_auth_code, "
        + "nvl(db_deduct_proc_type,'',db_deduct_proc_type) as db_deduct_proc_type " + "	from ( "
        + "select a.reference_no, " + "a.dest_amt as dbb_bill_dest_amt, " + "a.batch_no, "
        + "a.txn_code, " + "a.acq_member_id, " + "a.mcht_category, "
        + "b.dest_amt as dbb_ccas_dtl_dest_amt, " + "b.acq_id, " + "b.debit_acct_no, "
        + "b.rsk_type as dbb_ccas_dtl_rsk_type, " + "a.rsk_type as dbb_bill_rsk_type, "
        + "a.rsk_type_special, " + "'+' db_sign, "
        + "b.purchase_date as dbb_ccas_dtl_purchase_date, " + "a.mcht_country, " + "a.post_date, "
        + "a.purchase_date as dbb_bill_purchase_date, " + "a.auth_code as dbb_bill_auth_code, "
        + "a.card_no as dbb_bill_card_no, " + "c.card_no as dbc_card_card_no, " + "c.acct_no, "
        + "b.auth_code as dbb_ccas_dtl_auth_code, "
        + "(select deduct_proc_type from dba_deduct_txn d where d.card_no = a.card_no and d.reference_no = a.txn_code "
        + "and deduct_date =(select max(e.deduct_date) " + "from dba_deduct_txn e "
        + "where e.reference_no = d.reference_no)) as db_deduct_proc_type "
        + "from dbb_bill a left outer join dbb_ccas_dtl b on a.reference_no = b.reference_no "
        + ",dbc_card c ";
    wp.sqlCmd += " where ( a.card_no = c.card_no ) ";
    wp.sqlCmd += " and a.rsk_type_special != '' ";

    if (empty(exDateS) == false) {
      wp.sqlCmd += " and a.post_date >= :exDateS ";
      setString("exDateS", exDateS);
    }
    if (empty(exDateE) == false) {
      wp.sqlCmd += " and a.post_date <= :exDateE ";
      setString("exDateE", exDateE);
    }
    switch (dbSign) {
      case "1":
        wp.sqlCmd += " and a.txn_code not in ('06','25','27','28','29') ";
        break;
      case "2":
        wp.sqlCmd += " and a.txn_code in ('06','25','27','28','29')";
        break;
    }
    wp.sqlCmd += " ) ";
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

      txnCode = wp.colStr(ii, "txn_code");
      dbDeductProcType = wp.colStr(ii, "db_deduct_proc_type");
      wp.colSet(ii, "db_deduct_proc_type", "Y");

      if (txnCode.equals("06") || txnCode.equals("25") || txnCode.equals("27")
          || txnCode.equals("28") || txnCode.equals("29")) {
        wp.colSet(ii, "db_sign", "-");
        wp.colSet(ii, "db_deduct_proc_type", " ");
      } else {
        if (dbDeductProcType.equals("01")) {
          wp.colSet(ii, "db_deduct_proc_type", " ");
        }
      }
    }
    wp.colSet("row_ct", intToStr(rowct));
    wp.colSet("user_id", wp.loginUser);
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      String batchno1 = wp.itemStr("ex_batchno1");
      String online = wp.itemStr("ex_online");
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
      String all = "批號 :" + batchno1 + "~" + batchno1 + " 線上製卡: " + online + " COMBO卡: "
          + comboFlag;
      wp.colSet("cond_1", all);
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String cond1 = "入帳日期: ";
    cond1 += exDateS + " -- " + exDateE;
    wp.colSet("cond_1", cond1);
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
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
