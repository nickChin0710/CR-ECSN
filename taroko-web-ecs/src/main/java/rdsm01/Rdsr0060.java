/*
 * 
 * 
 */
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1227  V1.00.01  Alex     keep tab                                     *
* 2020-0406  V1.00.02  shiyuqi  資料調整                                                                                                             *
* 109-04-22  V1.00.03  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package rdsm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Rdsr0060 extends BaseAction implements InfacePdf {

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
      tabClick();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
      strAction = "PDF";
      pdfPrint2();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_proc_mm1"), wp.itemStr("ex_proc_mm2")) == false) {
      alertErr2("處理年月起迄 錯誤");
      return;
    }

    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    daoTid = "A1_";
    wp.selectSQL = "" + " proc_month ," + " kind_type ," + " rd_status ," + " pcard_type ,"
        + " rsn_2_cnt ," + " rsn_3_cnt ," + " rsn_5_cnt ," + " rsn_8_cnt ," + " rsn_other_cnt ";
    wp.daoTable = "cms_road_sum";
    wp.whereStr = " where 1=1 " + " and kind_type = '1' " + " and rd_status = '0' "
        + " and pcard_type ='N' " + sqlCol(wp.itemStr("ex_proc_mm1"), "proc_month", ">=")
        + sqlCol(wp.itemStr("ex_proc_mm2"), "proc_month", "<=")
    // --??and rd_status =:ex_stoprsn
    ;
    wp.whereOrder = " order by 1 , 2 ";

    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
    }
    wp.setListCount(0);

    queryRead2();

    if (wp.listCount[0] == 0 && wp.listCount[1] == 0) {
      alertErr2("此條件查無資料");
      return;
    }


  }

  void queryRead2() throws Exception {
    wp.pageRows = 9999;
    daoTid = "A2_";
    wp.selectSQL = "" + " proc_month , " + " kind_type , " + " rd_status , " + " pcard_type , "
        + " rd_cardno , " + " uf_hi_cardno(rd_cardno) as hh_card_no , " + " rd_stoprsn , "
        + " last_st_date ";
    wp.daoTable = "cms_road_sum";
    wp.whereStr = " where 1=1 " + " and kind_type = '2' " + " and rd_status = '0' "
        + sqlCol(wp.itemStr("ex_proc_mm1"), "proc_month", ">=")
        + sqlCol(wp.itemStr("ex_proc_mm2"), "proc_month", "<=");
    if (!eqIgno(wp.itemStr("ex_stoprsn"), "0")) {
      wp.whereStr += sqlCol(wp.itemStr("ex_stoprsn"), "rd_stoprsn");
    }
    wp.whereOrder = " order by proc_month Asc, rd_stoprsn Asc, last_st_date Asc ";
    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
    }
    wp.setListCount(2);
    wp.colSet("tl_detl_cnt", wp.selectCnt);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

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
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    tabClick();

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "rdsr0060-1";
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rdsr0060-1.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

  public void pdfPrint2() throws Exception {
    wp.reportId = "rdsr0060-2";
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.setListIndex(2);
    pdf.excelTemplate = "rdsr0060-2.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

  void tabClick() {
    String lsClick = "";
    lsClick = wp.itemStr2("tab_click");
    if (eqIgno(wp.buttonCode, "Q"))
      wp.colSet("a_click_1", "");
    if (eqIgno(lsClick, "1")) {
      wp.colSet("a_click_1", "id='tab_active'");
    } else if (eqIgno(lsClick, "2")) {
      wp.colSet("a_click_2", "id='tab_active'");
    } else {
      wp.colSet("a_click_1", "id='tab_active'");
    }
  }

}
