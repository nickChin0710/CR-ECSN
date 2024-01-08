/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
* 109-12-24   V1.00.02 Justin         parameterize sql
******************************************************************************/
package secm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Secr2020 extends BaseAction implements InfacePdf {

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
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "secr2020")) {
        wp.optionKey = wp.colStr(0, "ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "secr2020")) {
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_date1")) || empty(wp.itemStr("ex_date2"))) {
      alertErr2("異動日期:不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動期間起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_group_id"), "group_id")
        + sqlCol(wp.itemStr("ex_user_level"), "user_level")
        + sqlCol(wp.itemStr("ex_pgmid"), "wf_winid", "like%") 
        + " and mod_time >= to_date( ? ,'yyyymmdd') " 
        + " and mod_time <= to_date( ?, 'yyyymmddhh24miss') ";
    setString(wp.itemStr("ex_date1"));
    setString(wp.itemStr("ex_date2")+ "235959");
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " group_id , " + " user_level , " + " wf_winid , " + " mod_audcode , "
        + " to_char(mod_time,'yyyymmdd') as db_mod_date , "
        + " to_char(mod_time,'hh24miss') as db_mod_time , " + " aut_query , " + " aut_update , "
        + " aut_approve , " + " aut_print , " + " crt_user , " + " crt_date , " + " apr_user , "
        + " apr_date  ";
    wp.daoTable = "sec_authority_log ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by group_id , user_level , wf_winid";
    pageQuery();
    queryAfter();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "mod_audcode", "A")) {
        wp.colSet(ii, "mod_audcode", "A.指定");
      } else if (wp.colEq(ii, "mod_audcode", "U")) {
        wp.colSet(ii, "mod_audcode", "U.異動");
      } else if (wp.colEq(ii, "mod_audcode", "D")) {
        wp.colSet(ii, "mod_audcode", "D.取消");
      }
    }
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
    wp.reportId = "Secr2020";
    wp.pageRows = 9999;
    String cond1;

    cond1 = "異動期間: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));

    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "secr2020.xlsx";
    pdf.pageCount = 25;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
