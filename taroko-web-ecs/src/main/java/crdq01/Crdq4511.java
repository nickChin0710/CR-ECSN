/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-05  V1.00.00  Andy Liu      program initial                         *
* 106-12-14            Andy		  update : program name : Crdi4511==>Crdq4511*
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdq4511 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdq4511";

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
    String exResidentZip = wp.itemStr("ex_resident_zip");
    String exDate1 = wp.itemStr("ex_date1");
    String exDate2 = wp.itemStr("ex_date2");

    String lsWhere = " where 1=1  ";
    // 固定搜尋條件

    // user搜尋條件
    // ex_resident_zip虛擬卡號
    if (empty(exResidentZip) == false) {
      lsWhere += sqlCol(exResidentZip, "a.resident_zip");
    }

    // ex_date修改日期：
    lsWhere += sqlStrend(exDate1, exDate2, "substr(a.mod_time, 1,8)");

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    if (getWhereStr() == false)
//      return;
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

    wp.selectSQL = "" + "substr(a.mod_time, 1, 8) as mod_date, " + "a.mod_pgm, " + "a.mod_audcode, "
        + "a.sex, " + "a.birthday, " + "a.education, " + "a.resident_zip, " + "a.annual_income, "
        + "a.business_code, " + "a.id_p_seqno, " + "b.id_no, " + "b.chi_name, "
        + "(b.resident_addr1||b.resident_addr2||b.resident_addr3||b.resident_addr4||b.resident_addr5 ) db_addr ";
    wp.daoTable = " dbc_idno_tonccc a left join dbc_idno b on a.id_p_seqno = b.id_p_seqno  ";
    wp.whereOrder = " order by a.mod_time ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
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
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String exResidentZip = wp.itemStr("ex_resident_zip");
    String exDate1 = wp.itemStr("ex_date1");
    String exDate2 = wp.itemStr("ex_date2");

    String title = "列表條件 : ";
    // ex_resident_zip虛擬卡號
    if (empty(exResidentZip) == false) {
      title += " 郵遞區號 : " + exResidentZip;
    }
    // ex_date日期
    if (empty(exDate1) == false || empty(exDate2) == false) {
      title += "  修改日期 : ";
      if (empty(exDate1) == false) {
        title += exDate1 + " 起 ";
      }
      if (empty(exDate2) == false) {
        title += " ~ " + exDate2 + " 迄 ";
      }
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

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.pageVert = true; // 直印
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
      // dddw_expire_reason
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_expire_reason");
      // dddw_list("dddw_expire_reason", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type
      // = 'NOTCHG_KIND_S_P' order by wf_id");

      // dddw_rtn_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_rtn_code");
      // dddw_list("dddw_rtn_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type =
      // 'IBM_RTNCODE' and wf_id <> '00' and wf_id <>'99' order by wf_id");
    } catch (Exception ex) {
    }
  }

}

