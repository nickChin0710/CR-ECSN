/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.00  Tanwei       updated for project coding standard      *
* 110-01-06  V1.00.03  shiyuqi       修改无意义命名                                                                                     *    
******************************************************************************/
package rdsm01;
/**
 * 2019-1227   Alex  fix queryFunc
 * 2019-1028   JH    ++rd_newcarno
 * 2020-0116   Ru    加上卡號欄位、正卡姓名改為持卡人姓名、移除覆核
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;

import taroko.com.TarokoPDF;

public class Rdsr0020 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("ex_group_code");
      dddwList("d_dddw_groupcode", "ptr_group_code", "group_code", "group_name",
          "where group_code<>'0000' ");
    } catch (Exception ex) {
    }


  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("登錄日期 : 起迄錯誤");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_mod_time1"), wp.itemStr("ex_mod_time")) == false) {
      alertErr2("異動日期 : 起迄錯誤");
    }

    String lsWhere = " where 1=1 and rd_modtype like 'O%' " + sqlCol("F", "rd_type")
        + sqlCol(wp.itemStr("ex_group_code"), "group_code")
        + sqlCol(wp.itemStr("ex_crt_user"), "crt_user")
        + sqlBetween("ex_crt_date1", "ex_crt_date2", "crt_date")
        + sqlBetween("ex_mod_time1", "ex_mod_time2", "varchar_format(mod_time, 'yyyymmdd')")
        + sqlCol(wp.itemStr("ex_mod_user"), "mod_user");

    if (!eqIgno(wp.itemStr("ex_status"), "N")) {
      lsWhere += sqlCol(wp.itemStr("ex_status"), "rd_status");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " appl_card_no , "
        + " decode(new_card_no,'',card_no,new_card_no) as db_card_no , " + " group_code , "
        + " rd_carno as db_carno , " + " rd_carmanname , " + " crt_user , " + " crt_date , "
        + " mod_user , " + " varchar_format(mod_time, 'yyyymmdd') as mod_time , " + " rd_status , "
        + " new_card_no , " + " rd_newcarno, "
        + " uf_idno_name(decode(new_card_no,'',card_no,new_card_no)) as db_cname ";
    wp.daoTable = "cms_roaddetail";
    wp.whereOrder = " order by 1 ";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
    }
    wp.setListCount(0);
    wp.setPageValue();
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
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "rdsr0020";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "類別: F.免費 " + " 團體代號:" + wp.itemStr("group_code") + " 登錄日期 : "
        + commString.strToYmd(wp.itemStr("ex_crt_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_crt_date2")) + " 異動日期: "
        + commString.strToYmd(wp.itemStr("ex_mod_time1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_mod_time2"));
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rdsr0020.xlsx";
    pdf.pageCount = 32;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
