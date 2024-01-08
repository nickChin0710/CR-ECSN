/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-02-13  V1.00.00  yash       program initial                            *
* 108-12-20  v1.00.01  Andy       Update ptr_branch=>gen_brn                 *
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdq01;

import java.io.InputStream;
import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;


public class Crdq0511 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdq0910";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    wp.colSet("ex_chg_date1", getSysDate());
  }

  private boolean getWhereStr() throws Exception {
    String exOldId = wp.itemStr("ex_old_id");
    String exId = wp.itemStr("ex_id");
    String exChgDate1 = wp.itemStr("ex_chg_date1");
    String exChgDate2 = wp.itemStr("ex_chg_date2");

    // 固定搜尋條件
    String lsWhere = "where 1=1  ";



    // user搜尋條件

    // ex_old_id
    if (empty(exOldId) == false) {
      lsWhere += sqlCol(exOldId, "old_id_no");
    }

    // ID
    if (empty(exId) == false) {
      lsWhere += sqlCol(exId, "id_no");
    }

    // 登入日期：
    lsWhere += sqlStrend(exChgDate1, exChgDate2, "chg_date");



    if (empty(exOldId) && empty(exId)) {
      if (empty(exChgDate1)) {
        alertErr2("登入日期不可空白!");
        return false;
      }
    }

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

    wp.selectSQL = "" + "old_id_no, " + "id_no, " + "chi_name, " + "chg_date ";
    wp.daoTable = " crd_chg_id ";
    wp.whereOrder = " order by old_id_no ";

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

  }

  void subTitle() {
    String exEmbossDate1 = wp.itemStr("ex_emboss_date1");
    String exEmbossDate2 = wp.itemStr("ex_emboss_date2");
    String exType = wp.itemStr("ex_type");
    String exBankNo = wp.itemStr("ex_bank_no");
    String exIdNo = wp.itemStr("ex_id_no");
    String contadition = "列表條件 : ";
    // 進件日期
    if (empty(exEmbossDate1) == false || empty(exEmbossDate2) == false) {
      contadition += "進件日期 : ";
      if (empty(exEmbossDate1) == false) {
        contadition += exEmbossDate1 + " 起 ";
      }
      if (empty(exEmbossDate2) == false) {
        contadition += " ~ " + exEmbossDate2 + " 迄 ";
      }
    }

    // 列印種類
    contadition += "列印種類 : ";
    if (exType.equals("1")) {
      contadition += " 成功 ";
    } else {
      contadition += " 失敗 ";
    }

    // 分行
    if (empty(exBankNo) == false) {
      contadition += " 分行 : " + exBankNo;
    }

    // 身分證號
    if (empty(exIdNo) == false) {
      contadition += " 身分證號 : " + exIdNo;
    }

    reportSubtitle = contadition;
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
    // try {
    // //dddw_bank_no
    // wp.initOption = "--";
    // wp.optionKey = wp.col_ss("ex_bank_no");
    // dddw_list("dddw_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    // } catch (Exception ex) {
    // }
  }

}

