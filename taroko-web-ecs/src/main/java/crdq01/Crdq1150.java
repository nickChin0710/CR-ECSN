/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-26  V1.00.00  Andy Liu      program initial                         *
* 106-12-14            Andy		  update : program name : Crdi1150==>Crdq1150*
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

public class Crdq1150 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "Crdq1150";

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
    String exEmbossSource = wp.itemStr("ex_emboss_source");
    String exBatchno1 = wp.itemStr("ex_batchno1");
    String exBatchno2 = wp.itemStr("ex_batchno2");
    String exGroupCode = wp.itemStr("ex_group_code");
    if (empty(exBatchno1) && empty(exBatchno2) && empty(exGroupCode)) {
      alertErr("批號或團代請至少輸入一項查詢條件");
      return false;
    }
    String lsWhere = "where 1=1 ";
    // 固定搜尋條件
    lsWhere += " and in_main_error = '0' ";

    // user搜尋條件
    // 送製卡來源
    if (exEmbossSource.equals("0") == false) {
      lsWhere += sqlCol(exEmbossSource, "emboss_source");
    }

    // ex_batchno批號：
    lsWhere += sqlStrend(exBatchno1, exBatchno2, "batchno");

    // ex_group_code團代
    if (empty(exGroupCode) == false) {
      lsWhere += sqlCol(exGroupCode, "group_code");
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
    String exOrder = wp.itemStr("ex_order");
    wp.selectSQL = "" + "batchno, " + "recno, " + "(batchno||'-'||recno) db_batchno, "
        + "emboss_source, " + "pp_card_no, " + "card_type, " + "group_code, " + "eng_name, "
        + "valid_to, " + "recno, " + "uf_idno_name(uf_idno_pseqno(id_no)) db_chi_name, " + "id_no, "
        + "id_no_code, " + "(id_no ||'_'|| id_no_code) db_id_no ";
    wp.daoTable = " crd_emboss_pp ";
    // ex_order排序方式
    if (exOrder.equals("0") == false) {
      switch (exOrder) {
        case "1":
          wp.whereOrder += " order by id_no ";
          break;
        case "2":
          wp.whereOrder += " order by pp_card_no ";
          break;
        case "3":
          wp.whereOrder += " order by batchno ";
          break;
      }
    }


    // setParameter();
    System.out
        .println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr + wp.whereOrder);
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
    // wp.col_set("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

      // emboss_source中文 另一種寫法比switch case 容易
      String embossSource = wp.colStr(ii, "emboss_source");;
      String[] cde = new String[] {"1", "3", "4", "5"};
      String[] txt = new String[] {"新製卡", "整批續卡", "線上續卡", "補發卡"};
      wp.colSet(ii, "wk_emboss_source", commString.decode(embossSource, cde, txt));

    }
    wp.colSet("ft_cnt", intToStr(rowCt));
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String exEmbossSource = wp.itemStr("ex_emboss_source");
    String exBatchno1 = wp.itemStr("ex_batchno1");
    String exBatchno2 = wp.itemStr("ex_batchno2");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exOrder = wp.itemStr("ex_order");

    String title = "列表條件 : ";
    // ex_emboss_source送製卡來源
    title += "送製卡來源 : ";
    switch (exEmbossSource) {
      case "0":
        title += " 全部 ";
        break;
      case "1":
        title += " 新製卡 ";
        break;
      case "3":
        title += " 整批續卡 ";
        break;
      case "4":
        title += " 線上續卡 ";
        break;
      case "5":
        title += " 補發卡 ";
        break;
    }

    // ex_batchno批號
    if (empty(exBatchno1) == false || empty(exBatchno2) == false) {
      title += " 批號  : ";
      if (empty(exBatchno1) == false) {
        title += exBatchno1 + " 起 ";
      }
      if (empty(exBatchno2) == false) {
        title += " ~ " + exBatchno2 + " 迄 ";
      }
    }

    // ex_group_code團代
    if (empty(exGroupCode) == false) {
      title += " 團體代碼 : " + exGroupCode;
    }

    // ex_order排序
    title += " 排序 : ";
    switch (exOrder) {
      case "0":
        title += " 不分 ";
        break;
      case "1":
        title += " ID ";
        break;
      case "2":
        title += " 卡號 ";
        break;
      case "3":
        title += " 批號  ";
        break;
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
      wp.optionKey = wp.colStr("ex_group_code");
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");
    } catch (Exception ex) {
    }
  }

}

