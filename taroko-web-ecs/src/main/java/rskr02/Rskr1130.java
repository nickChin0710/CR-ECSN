/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package rskr02;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Rskr1130 extends BaseAction implements InfaceExcel, InfacePdf {

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
      if (eqIgno(wp.respHtml, "rskr1130")) {
        wp.optionKey = wp.colStr("ex_risk_group");
        dddwList("dddw_risk_group", "rsk_trial_parm2", "risk_group", "risk_group", "where 1=1");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskr1130")) {
        wp.optionKey = wp.colStr("ex_action_code");
        ddlbList("dddw_action_code", wp.colStr("ex_action_code"),
            "ecsfunc.DeCodeRsk.trialAction");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_query_date1")) && empty(wp.itemStr("ex_query_date2"))
        && empty(wp.itemStr("ex_trial_date1")) && empty(wp.itemStr("ex_trial_date2"))
        && empty(wp.itemStr("ex_risk_group")) && empty(wp.itemStr("ex_action_code"))
        && empty(wp.itemStr("ex_batch_no1")) && empty(wp.itemStr("ex_batch_no2"))
        && empty(wp.itemStr("ex_batch_no3")) && empty(wp.itemStr("ex_batch_no4"))
        && empty(wp.itemStr("ex_batch_no5")) && empty(wp.itemStr("ex_batch_no6"))
        && empty(wp.itemStr("ex_apr_flag"))) {
      alertErr2("條件不可全部空白！");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_query_date1"), wp.itemStr("ex_query_date2")) == false) {
      alertErr2("查詢日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_trial_date1"), wp.itemStr("ex_trial_date2")) == false) {
      alertErr2("覆審日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_risk_group"), "risk_group")
        + sqlCol(wp.itemStr("ex_action_code"), "action_code")
        + sqlCol(wp.itemStr("ex_query_date1"), "query_date", ">=")
        + sqlCol(wp.itemStr("ex_query_date2"), "query_date", "<=")
        + sqlCol(wp.itemStr("ex_trial_date1"), "trial_date", ">=")
        + sqlCol(wp.itemStr("ex_trial_date2"), "trial_date", "<=");

    if (empty(wp.itemStr("ex_batch_no1")) == false || empty(wp.itemStr("ex_batch_no2")) == false
        || empty(wp.itemStr("ex_batch_no3")) == false || empty(wp.itemStr("ex_batch_no4")) == false
        || empty(wp.itemStr("ex_batch_no5")) == false
        || empty(wp.itemStr("ex_batch_no6")) == false) {
      lsWhere += " and batch_no in (''";
      if (empty(wp.itemStr("ex_batch_no1")) == false) {
        lsWhere += ",'" + wp.itemStr("ex_batch_no1") + "'";
      }
      if (empty(wp.itemStr("ex_batch_no2")) == false) {
        lsWhere += ",'" + wp.itemStr("ex_batch_no2") + "'";
      }
      if (empty(wp.itemStr("ex_batch_no3")) == false) {
        lsWhere += ",'" + wp.itemStr("ex_batch_no3") + "'";
      }
      if (empty(wp.itemStr("ex_batch_no4")) == false) {
        lsWhere += ",'" + wp.itemStr("ex_batch_no4") + "'";
      }
      if (empty(wp.itemStr("ex_batch_no5")) == false) {
        lsWhere += ",'" + wp.itemStr("ex_batch_no5") + "'";
      }
      if (empty(wp.itemStr("ex_batch_no6")) == false) {
        lsWhere += ",'" + wp.itemStr("ex_batch_no6") + "'";
      }
      lsWhere += ")";
    }

    if (wp.itemEq("ex_apr_flag", "Y")) {
      lsWhere += " and apr_date <> '' ";
    } else if (wp.itemEq("ex_apr_flag", "N")) {
      lsWhere += " and apr_date = '' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " batch_no ," + " query_date ," + " risk_group ," + " action_code ,"
        + " count(*) as db_cnt "
    // + " sum(adj_credit_limit_remain) as db_adj_remain"
    ;

    wp.daoTable = " rsk_trial_list ";
    wp.whereOrder =
        " group by batch_no , risk_group , action_code , query_date  order by batch_no , risk_group ";

    wp.pageCountSql = "" + "select count(*) from ( "
        + " select distinct batch_no , risk_group , action_code , query_date "
        + " from rsk_trial_list " + wp.whereStr + " )";

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

    String sql1 = " select " + " sum(credit_limit_bef - credit_limit_aft) as tl_limit "
        + " from rsk_trial_action_log " + " where batch_no = ? " + " and id_p_seqno in "
        + " (select id_p_seqno from rsk_trial_list where batch_no =? "
        + " and risk_group = ? and action_code = ? ) ";

    for (int ii = 0; ii < ilSelectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "batch_no"), wp.colStr(ii, "batch_no"),
          wp.colStr(ii, "risk_group"), wp.colStr(ii, "action_code")});

      wp.colSet(ii, "db_adj_remain", sqlStr("tl_limit"));

      if (wp.colEq(ii, "action_code", "0")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".原額用卡");
      } else if (wp.colEq(ii, "action_code", "1")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".調降額度-未降足額度者凍結");
      } else if (wp.colEq(ii, "action_code", "2")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".調降額度-未降足額度者維護特指");
      } else if (wp.colEq(ii, "action_code", "3")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".調整額度");
      } else if (wp.colEq(ii, "action_code", "4")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".調降額度-卡戶凍結(個繳)");
      } else if (wp.colEq(ii, "action_code", "5")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".調降額度-維護特指");
      } else if (wp.colEq(ii, "action_code", "6")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".卡戶凍結[4]");
      } else if (wp.colEq(ii, "action_code", "7")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".卡片維護特指");
      } else if (wp.colEq(ii, "action_code", "8")) {
        wp.colSet(ii, "wk_action_code", wp.colStr(ii, "action_code") + ".額度內用卡");
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
    wp.reportId = "rskr1130-PDF";
    String tmpStr = "覆審日期: " + commString.strToYmd(wp.itemStr("ex_trial_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_trial_date"));
    wp.colSet("cond1", tmpStr);
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr1130-PDF.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "rskr1130-EXCEL";
      String tmpStr = "覆審日期: " + commString.strToYmd(wp.itemStr("ex_trial_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_trial_date"));
      wp.colSet("user_id", wp.loginUser);
      wp.colSet("cond1", tmpStr);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "rskr1130-EXCEL.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}
