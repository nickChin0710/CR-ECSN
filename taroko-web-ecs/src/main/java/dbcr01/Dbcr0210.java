/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-07-12  V1.00.00  Ryan       program initial                            *
******************************************************************************/
package dbcr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbcr0210 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "dbcr0210";

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
    String exBatchno = wp.itemStr("ex_batchno");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exCardNo1 = wp.itemStr("ex_card_no1");
    String exCardNo2 = wp.itemStr("ex_card_no2");
    if (empty(exBatchno) && empty(exDateS) && empty(exDateE) && empty(exCardNo1) && empty(exCardNo2)) {
      alertErr("請至少輸入一項查詢條件");
      return false;
    }
    String lsWhere = "where 1=1  ";
    // 固定搜尋條件
    lsWhere += " and decode(a.in_main_error,'','0',a.in_main_error) != '0' and a.in_main_date = '' ";
    // user搜尋條件
    if (empty(exBatchno) == false) {
      lsWhere += sqlCol(exBatchno, "a.batchno");
    }
    lsWhere += sqlStrend(exDateS, exDateE, "a.to_nccc_date");
    lsWhere += sqlStrend(exCardNo1, exCardNo2, "a.card_no");

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {

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

    wp.selectSQL = "" + "a.vendor, " + "b.vendor_name, " + "a.batchno, " + "a.recno, "
        + "a.emboss_source, " + "a.emboss_reason, " + "a.resend_note, " + "a.card_no, "
        + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:卡號
        + "a.card_type, " + "a.apply_id, " + "a.apply_id_code, " + "a.birthday, " + "a.chi_name, "
        + "a.error_code, " + "a.to_nccc_date, " + "a.in_main_msg as reject_msg, " + "a.group_code, "
        + "a.apply_no, " + "a.aps_batchno, " + "a.pm_id, " + "a.pm_id_code, " + "a.acct_type, "
        + "a.aps_recno, " + "a.mail_zip, " + "a.reject_code, " + "a.emboss_4th_data, "
        + "a.act_no, " + "a.mail_seqno, " + "a.mail_type, " + "a.branch, " + "a.unit_code, "
        + "'' wk_temp ";
    wp.daoTable = " dbc_emboss a left join ptr_vendor_setting b on a.vendor = b.vendor ";
    wp.whereOrder = " order by vendor,card_no ";

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

    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsSql = "";
    String wkBatchno = "", wkApplyId = "", wkPmId = "", wkRejectMsg = "";
    String wpEmbossSource = "", wpEmbossReason = "", wpRejectCode = "";
    String dbApplyId = "", dbPmId = "";
    wp.logSql = false;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_cnt", "1");
      // wk_batchno批號 batchno +"-"+ recno
      wkBatchno = wp.colStr(ii, "batchno") + "-" + wp.colStr(ii, "recno");
      wp.colSet(ii, "wk_batchno", wkBatchno);

      // emboss_source製卡來源中文
      wpEmbossSource = wp.colStr(ii, "emboss_source");
      String wpEmbossReason1 = wpEmbossSource;
      String[] cde = new String[] {"1", "2", "3", "4", "5", "7"};
      String[] txt = new String[] {"新製卡", "普昇金卡", "整批續卡", "提前續卡", "重製", "緊急補發"};
      wp.colSet(ii, "emboss_source", commString.decode(wpEmbossReason1, cde, txt));

      // emboss_reason製卡原因
      wpEmbossReason = wp.colStr(ii, "emboss_reason");
      String wpEmbossReason2 = wpEmbossReason;
      String[] cde1 = new String[] {"0", "1", "2", "3", "4"};
      String[] txt1 = new String[] {"", "掛失", "損毀", "偽卡", "星座卡重製"};
      wp.colSet(ii, "emboss_reason", commString.decode(wpEmbossReason2, cde1, txt1));

      // 持卡人身分證號
      wkApplyId = wp.colStr(ii, "apply_id") + "-" + wp.colStr(ii, "apply_id_code");
      wp.colSet(ii, "wk_apply_id", wkApplyId);

      // db_apply_id持卡人身分證字號隱第4~7碼(給報表使用)
      dbApplyId = commString.hideIdno(wkApplyId);
      wp.colSet(ii, "db_apply_id", dbApplyId);

      // wk_pm_id正卡身分證號
      wkPmId = wp.colStr(ii, "pm_id") + "-" + wp.colStr(ii, "pm_id_code");
      wp.colSet(ii, "wk_pm_id", wkPmId);

      // db_pm_id正卡身分證字號隱第4~7碼(給報表使用)
      dbPmId = commString.hideIdno(wkPmId);
      wp.colSet(ii, "db_pm_id", dbPmId);
    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String exBatchno = wp.itemStr("ex_batchno");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exCardNo1 = wp.itemStr("ex_card_no1");
    String exCardNo2 = wp.itemStr("ex_card_no2");
    String title = "";
    if (empty(exBatchno) == false) {
      title += " 批號 :" + exBatchno;
    }
    if (empty(exDateS) == false || empty(exDateE) == false) {
      title += " 送製卡日期 :";
      if (empty(exDateS) == false) {
        title += " " + exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        title += exDateE + " 迄 ";
      }
    }
    if (empty(exCardNo1) == false || empty(exCardNo2) == false) {
      title += " 卡號 : ";
      if (empty(exCardNo1) == false) {
        title += "起號 " + exCardNo1 + " ~ ";
      }
      if (empty(exCardNo2) == false) {
        title += "迄號 " + exCardNo2;
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
    // ===========================
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
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

    } catch (Exception ex) {
    }
  }

}

