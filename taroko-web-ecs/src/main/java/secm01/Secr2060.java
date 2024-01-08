package secm01;
/**
 * 2019-0822   JH    ++cond
* 109-04-20   shiyuqi       updated for project coding standard  
* 2020-08-28  Alex   add column
* 2020-12-22  Justin  parameterize sql
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoPDF;

import java.lang.reflect.Array;

public class Secr2060 extends BaseAction implements InfacePdf {

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
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "secr2060")) {
        wp.optionKey = wp.colStr(0, "ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "secr2060")) {
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
      }
    } catch (Exception ex) {
    }

  }

  String[] getUserGroup() throws Exception {
    String[] aaGroup = null;
    wp.colSet("kk_user_level", "");

    if (wp.itemEmpty("ex_user_id"))
      return aaGroup;

    String sql1 = "select usr_group, usr_level from sec_user" + " where usr_id =?";
    sqlSelect(sql1, new Object[] {wp.itemStr2("ex_user_id")});

    if (sqlRowNum > 0) {
      aaGroup = sqlStr("usr_group").toUpperCase().split(",");
      wp.colSet("kk_user_level", sqlStr("usr_level"));
    }

    return aaGroup;
  }

  @Override
  public void queryFunc() throws Exception {
    if (itemallEmpty("ex_winid,ex_user_id,ex_group_id,ex_user_level")) {
      alertErr2("條件不可全部空白");
      return;
    }

    String[] aaGroup = getUserGroup();

    String lsWhere = sqlCol(wp.itemStr("ex_winid"), "B.wf_winid", "like%");

    if (wp.itemEmpty("ex_group_id") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_group_id"), "B.group_id")
          + sqlCol(wp.itemStr("ex_user_level"), "B.user_level");
    } else if (aaGroup != null) {
      lsWhere += sqlColIn("B.group_id", aaGroup) 
                    + sqlCol(wp.colStr("kk_user_level"), "B.user_level");
    }

    if (empty(lsWhere)) {
      alertErr2("條件: 不可全部空白");
      return;
    }

    wp.whereStr = "where 1=1 " + lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.daoTable = "sec_window A join sec_authority B on A.wf_winid = B.wf_winid ";
    if (eqIgno(strAction, "xls")) {
      wp.selectSQL = "B.group_id, B.user_level, " + " B.wf_winid , " + " A.wf_name , "
          + " B.aut_query as db_query , " + " B.aut_update as db_update , "
          + " B.aut_approve as db_approve";
      wp.whereOrder = " order by B.wf_winid, B.group_id, B.user_level";
    } else {
      wp.selectSQL = "" + "B.group_id, B.user_level, "
          + " B.wf_winid , " + " A.wf_name , " + " B.aut_query as db_query , "
          + " B.aut_update as db_update , " + " B.aut_approve as db_approve";
      wp.whereOrder = " order by B.wf_winid";
    }
    // sql_ddd();
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
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
    wp.reportId = "Secr2060";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "子系統代號 : " + wp.itemStr("ex_group_id") + " 使用者層級: " + wp.itemStr("ex_user_level")
        + " 使用者代號: " + wp.itemStr2("ex_user_id");

    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "secr2060.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

  void xlsPrint() throws Exception {
    wp.pageRows = 9999;
    queryFunc();
    int llNrow = wp.selectCnt;
    if (llNrow <= 0) {
      alertErr2("無資料可列印");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.listCount[0] = llNrow;

    TarokoFileAccess tf = new TarokoFileAccess(wp);
    String fileName = "secr2060-" + commString.mid(wp.sysDate + wp.sysTime, 4) + ".csv";

    // int file =tf.openOutputText(file_name,"UTF-8");
    int file = tf.openOutputText(fileName, "MS950");
    // String ls_data = "子系統: "+wp.sss("ex_group_id")+
    // ", 使用者層級: "+wp.sss("ex_user_level")+
    // ", 使用者代號: "+wp.sss("ex_user_id");
    // tf.writeTextFile(file,ls_data+wp.newLine);
    String lsCond = "子系統代號 : " + wp.itemStr("ex_group_id")+"," + " 使用者層級: " + wp.itemStr("ex_user_level")+","+ " 使用者代號: " + wp.itemStr2("ex_user_id");
    tf.writeTextFile(file, lsCond + wp.newLine);
    String lsData = "工作群組,使用者層級,程式代碼,作業說明,查詢,維護,線上覆核";
    tf.writeTextFile(file, lsData + wp.newLine);

    for (int ii = 0; ii < llNrow; ii++) {
      lsData = wp.colStr(ii, "group_id") + "," + wp.colStr(ii, "user_level") + ","
          + wp.colStr(ii, "wf_winid") + "," + wp.colStr(ii, "wf_name") + ","
          + wp.colStr(ii, "db_query") + "," + wp.colStr(ii, "db_update") + ","
          + wp.colStr(ii, "db_approve");
      tf.writeTextFile(file, lsData + wp.newLine);
    }
    tf.closeOutputText(file);
    wp.setDownload(fileName);
  }

}
