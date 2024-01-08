/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-08-06  V1.00.00  yash       program initial        
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                      *
******************************************************************************/
package dbbr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbbr0040 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbbr0040";
  String lsWhere = "";
  Double lsqty = 0.0, amt = 0.0;
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
      // xlsPrint();
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String keyNo = wp.itemStr("ex_key_no");
    String exUser = wp.itemStr("ex_user");
    String exId = wp.itemStr("ex_id");


    lsWhere = "where 1=1  ";


    if (empty(keyNo) == false) {
      lsWhere += " and key_no like :ex_key_no ";
      setString("ex_key_no", keyNo + "%");
    }

    if (empty(exUser) == false) {
      lsWhere += " and mod_user like :ex_user ";
      setString("ex_user", exUser + "%");
    }

    if (empty(exId) == false) {
      lsWhere += " and UF_IDNO_ID2(id_p_seqno,'Y') = :ex_id ";
      setString("ex_id", exId);
    }


    if (empty(exDateS) == false) {
      lsWhere += " and to_char(dbb_othexp.mod_time,'yyyymmdd') >= :exDateS ";
      setString("exDateS", exDateS);
    }

    if (empty(exDateE) == false) {
      lsWhere += " and to_char(dbb_othexp.mod_time,'yyyymmdd') <= :exDateE ";
      setString("exDateE", exDateE);
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

    if (getWhereStr() == false)
      return;

    wp.selectSQL = " " + "UF_IDNO_NAME2(id_p_seqno, 'Y') as chi_name, "
        + "UF_IDNO_ID2(ID_P_SEQNO,'Y') as acct_holder_id , " + "bill_type, " + "txn_code, "
        + "add_item, " + "card_no, " + "acct_type, " + "corp_no, " + "dest_amt, "
        + "purchase_date, " + "chi_desc, " + "bill_desc, " + "dept_flag, " + "apr_user, "
        + "apr_flag, " + "post_flag, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, "
        + "seq_no, " + "dest_curr, " + "key_no  ," + " '1' as qty ";

    wp.daoTable = " dbb_othexp ";

    wp.whereOrder += " order by mod_time,mod_seqno ";

    // setParameter();

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

    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String keyNo = wp.itemStr("ex_key_no");
    String user = wp.itemStr("ex_user");
    String id = wp.itemStr("ex_id");



    String lsSql = "select count(*) as cut ,sum(dest_amt) as damt  from dbb_othexp ";
    lsSql += " where 1=1 ";


    if (empty(keyNo) == false) {
      lsSql += " and key_no like :ex_key_no ";
      setString("ex_key_no", keyNo + "%");
    }

    if (empty(user) == false) {
      lsSql += " and mod_user like :ex_user ";
      setString("ex_user", user + "%");
    }

    if (empty(id) == false) {
      lsSql += " and UF_IDNO_ID2(id_p_seqno,'Y') = :ex_id ";
      setString("ex_id", id);
    }


    if (empty(exDateS) == false) {
      lsSql += " and to_char(dbb_othexp.mod_time,'yyyymmdd') >= :exDateS ";
      setString("exDateS", exDateS);
    }

    if (empty(exDateE) == false) {
      lsSql += " and to_char(dbb_othexp.mod_time,'yyyymmdd') <= :exDateE ";
      setString("exDateE", exDateE);
    }


    sqlSelect(lsSql);

    wp.colSet("ls_qty", sqlStr("cut"));
    wp.colSet("ls_amt", sqlStr("damt"));

  }

  // void xlsPrint() {
  // try {
  // ddd("xlsFunction: started--------");
  // wp.reportId = m_progName;
  // // -cond-
  // String exDateS = wp.item_ss("exDateS");
  //
  // String ss = "撥款年月: " + exDateS ;
  // wp.col_set("cond_1", ss);
  //
  // // ===================================
  // TarokoExcel xlsx = new TarokoExcel();
  // wp.fileMode = "Y";
  // xlsx.excelTemplate = m_progName + ".xlsx";
  //
  // //====================================
  // //-明細-
  // xlsx.sheetName[0] ="明細";
  // queryFunc();
  // wp.setListCount(1);
  // ddd("Detl: rowcnt:" + wp.listCount[0]);
  // xlsx.processExcelSheet(wp);
  // /*
  // //-合計-
  // xlsx.sheetName[1] ="合計";
  // query_Summary(cond_where);
  // wp.listCount[1] =sql_nrow;
  // ddd("Summ: rowcnt:" + wp.listCount[1]);
  // //xlsx.sheetNo = 1;
  // xlsx.processExcelSheet(wp);
  // */
  // xlsx.outputExcel();
  // xlsx = null;
  // ddd("xlsFunction: ended-------------");
  //
  // } catch (Exception ex) {
  // wp.expMethod = "xlsPrint";
  // wp.expHandle(ex);
  // }
  // }

  void pdfPrint() throws Exception {
    wp.reportId = progName;
    String all = " ";
    // -cond-
    if (!empty(wp.itemStr("ex_key_no"))) {
      all += "登錄批號：" + wp.itemStr("ex_key_no");
    }

    if (!empty(wp.itemStr("ex_user"))) {
      all += "  登錄人員：" + wp.itemStr("ex_user");
    }

    if (!empty(wp.itemStr("ex_id"))) {
      all += "  身分證字號：" + wp.itemStr("ex_id");
    }

    if (!empty(wp.itemStr("exDateS")) || !empty(wp.itemStr("exDateE"))) {
      all += "  登入日期：" + wp.itemStr("exDateS") + " -- " + wp.itemStr("exDateE");
    }

    wp.colSet("cond_1", all);
    wp.pageRows = 999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = progName + ".xlsx";
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
      // dddw_office_m_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_office");
      // dddw_list("dddw_office_m_code", "bil_office_m", "office_m_code", "office_m_name", "where
      // 1=1 group by office_m_code,office_m_name order by office_m_code");

    } catch (Exception ex) {
    }
  }

}

