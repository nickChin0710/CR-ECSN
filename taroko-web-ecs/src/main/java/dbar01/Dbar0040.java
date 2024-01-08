/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-03  V1.00.00  Andy Liu   program initial                            *
* 109-01-03  V1.00.01  Justin Wu  updated for archit.  change
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *
* 110-01-15  V1.00.02   Justin          fix a bug  
******************************************************************************/
package dbar01;

import java.io.InputStream;
import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbar0040 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbar0040";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        // is_action="new";
        // clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      // case "R":
      // // -資料讀取-
      // is_action = "R";
      // dataRead();
      // break;
      // case "A":
      // /* 新增功能 */
      // saveFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // saveFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // saveFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "XLS":
        // -Excel-
        strAction = "XLS";
        // wp.setExcelMode();
        xlsPrint();
        break;
      case "PDF":
        // -PDF-
        strAction = "PDF";
        // wp.setExcelMode();
        pdfPrint();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        itemchanged();
        break;
      default:
        break;
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exIdNo = wp.itemStr("ex_id_no");
    // 固定條件
    wp.whereStr = "where 1=1 ";
    // 自選條件
//    wp.whereStr += sqlStrend(exDateS, exDateE, "deduct_proc_date");
//    if (!empty(exIdNo)) {
//      exIdNo = exIdNo + "0";
//      wp.whereStr += sqlCol(exIdNo, "c.acct_key");
//    }

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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exIdNo = wp.itemStr("ex_id_no");
    wp.sqlCmd = "SELECT z.acct_code, " + "z.chi_short_name, " + "z.db_acct_item_ename, "
        + "z.deduct_date, " + "z.acct_type, " + "z.acct_no, " + "z.card_no, " + "z.purchase_date, "
        + "z.item_post_date, " + "z.dr_amt, " + "z.orginal_amt, " + "z.cr_amt, " + "z.bef_amt, "
        + "z.aft_amt, " + "z.bef_d_amt, " + "z.aft_d_amt, " + "z.from_code, " + "z.adjust_type, "
        + "z.deduct_proc_date, " + "z.deduct_proc_code, " + "z.deduct_proc_type, "
        + "z.deduct_amt, " + "z.acct_key, " + "d.chi_name as db_chiname " + "from ( "
        + "SELECT a.acct_code, "
        + "nvl (b.chi_short_name, '', b.chi_short_name) AS chi_short_name, "
        + "decode (a.acct_code,'','',a.acct_code || '_' || nvl (b.chi_short_name, '', b.chi_short_name)) AS db_acct_item_ename, "
        + "a.deduct_date, " + "a.acct_type, " + "a.acct_no, " + "a.card_no, " + "a.purchase_date, "
        + "a.item_post_date, " + "a.dr_amt, " + "a.orginal_amt, " + "a.cr_amt, " + "a.bef_amt, "
        + "a.aft_amt, " + "a.bef_d_amt, " + "a.aft_d_amt, " + "a.from_code, " + "a.adjust_type, "
        + "a.deduct_proc_date, " + "a.deduct_proc_code, " + "a.deduct_proc_type, "
        + "a.deduct_amt, " + "nvl (c.acct_key, '') AS acct_key, "
        + "(case when nvl (c.acct_key, '') = '' then '' else substr(c.acct_key,1,10) end) as id_no "
        + "FROM dba_acaj a " + "LEFT JOIN ptr_actcode b ON a.acct_code = b.acct_code "
        + "LEFT JOIN dba_acno c ON a.acct_no = c.acct_no " + "WHERE 1 = 1 ";
    wp.sqlCmd += sqlStrend(exDateS, exDateE, "deduct_proc_date");
    if (!empty(exIdNo)) {
      exIdNo = exIdNo + "0";
      wp.sqlCmd += sqlCol(exIdNo, "c.acct_key");
    }
    wp.sqlCmd +=
        ") z left join dbc_idno d on z.ID_NO = d.ID_NO " + "ORDER BY z.deduct_proc_date, z.card_no";

    // System.out.println(wp.sqlCmd);
//    wp.pageCountSql = "select count(*) from (";
//    wp.pageCountSql += wp.sqlCmd;
//    wp.pageCountSql += ")";

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
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    String lsSql = "", wkIdNo = "";
    int row_ct = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row_ct += 1;
    }
    wp.colSet("row_ct", intToStr(row_ct));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");

    String title = "";

    if (empty(exDateS) == false || empty(exDateE) == false) {
      title = "交易日期:  ";
      if (empty(exDateS) == false) {
        title += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        title += " ~ " + exDateE + " 迄 ";
      }
    }

    reportSubtitle = title;

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
    wp.pageRows = 9999;
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
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_bill_type");
      // dddw_list("dddw_billuint", "ptr_billunit", "bill_unit",
      // "short_title", "where 1=1 order by bill_unit");

    } catch (Exception ex) {
    }
  }

  public int itemchanged() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change
    String ajaxName = "";
    String lsSql = "";
    ajaxName = wp.itemStr("ajaxName");
    switch (ajaxName) {
      case "IdnoName":
        String ex_id_no = wp.itemStr("id_no");
        lsSql = "select chi_name from dbc_idno " + "where id_no = :as_data ";
        setString("as_data", ex_id_no);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          wp.addJSON("db_chi_name", sqlStr("chi_name"));
        } else {
          wp.addJSON("db_chi_name", "Id無效");
        }
        break;
    }
    return 1;
  }
}
