package secm01;
/**
 * 2019-1122   JH    modify
 * 2019-1015   JH    modify
 * 109-04-20   shiyuqi       updated for project coding standard     *
 * 109-12-24   Justin         parameterize sql 
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Secr3030 extends BaseAction implements InfacePdf {
  String isGroupId = "", isUserLevel = "";

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEmpty("ex_user_id")) {
      errmsg("使用者代號: 不可空白");
      return;
    }

    wp.colSet("xx_user_id", wp.itemStr2("ex_user_id"));
    wp.itemSet("xx_user_id", wp.itemStr2("ex_user_id"));

    getUserLevelGroupId(wp.itemStr2("ex_user_id"));

    if (empty(isGroupId) || empty(isUserLevel))
      return;

    String lsWhere = " where 1=1 " + sqlCol(isUserLevel, "user_level")
        + sqlCol(wp.itemStr2("ex_win_id"), "wf_winid", "like%");

    lsWhere += " and locate(','||lcase(group_id)||',',','||lcase(?)||',')";
    setString(isGroupId);
    
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  void getUserLevelGroupId(String userId) {

    String sql1 = " select usr_level , usr_group from sec_user where usr_id = ? ";
    sqlSelect(sql1, new Object[] {userId});

    if (sqlRowNum <= 0) {
      errmsg("查無使用者");
      return;
    }

    isGroupId = sqlStr("usr_group");
    isUserLevel = sqlStr("usr_level");

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        " wf_winid , " + " group_id , " + " aut_query , " + " aut_update , " + " aut_approve ";
    wp.daoTable = " sec_authority ";
    wp.whereOrder = " order by wf_winid ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();

    queryAfter();
  }

  void queryAfter() {
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;

    String sql1 = " select wf_name from sec_window where wf_winid = ? ";

    for (int ii = 0; ii < ilSelectCnt; ii++) {

      wp.colSet(ii, "usr_id", wp.itemStr2("xx_user_id"));

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "wf_winid")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_wf_winid", sqlStr("wf_name"));
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
    wp.reportId = "Secr3030";
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDFLine pdf = new TarokoPDFLine();
    // pdf.vertical_print(55);
    // TarokoPDF pdf = new TarokoPDF ();
    // pdf.pageCount =30;
    wp.fileMode = "Y";
    pdf.excelTemplate = "secr3030.xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
