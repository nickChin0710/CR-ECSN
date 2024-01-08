/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-18  V1.00.02  Alex       add cond1                                  *
* 108-12-12  V1.00.01  Alex       add Excel                                  *
* 109-04-21  V1.00.03  YangFang   updated for project coding standard        *
 * 109-01-04  V1.00.03  shiyuqi       修改无意义命名
* 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  *
******************************************************************************/
package misr01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Misr100110 extends BaseAction implements InfacePdf, InfaceExcel {

  taroko.base.CommDate commDate = new taroko.base.CommDate();
  double tlMp = 0, tlMpp = 0, tlYp = 0, tlYpp = 0;

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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "misr100110")) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type",
            "select acct_type as db_code , acct_type||'_'||chin_name as db_desc from ptr_acct_type where 1=1"
                + " union "
                + "select acct_type as db_code , acct_type||'_'||chin_name as db_desc from dbp_acct_type where 1=1");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_stat_month"), "stat_month")
        + sqlCol(wp.itemStr("ex_acct_type"), "acct_type");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " bp_area_s , " + " bp_area_e , " + " acct_cnt_a as acct_cnt_a , "
        + " acct_rate_a as acct_rate_a , " + " bonus_cnt_a as point_cnt_a , " + " acct_type , "
        + " stat_month , " + " seq_no ";

    wp.daoTable = " mkt_bonus_stat3 ";
    wp.whereOrder = " order by acct_type Asc ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);
    wp.setPageValue();

    queryAfter();
  }

  void queryAfter() {
    wp.logSql = false;
    String lsYM = "", lsLastMM = "", lsLastYY = "", lsAcctType = "";

    lsYM = wp.itemStr("ex_stat_month");
    lsLastMM = commDate.dateAdd(lsYM + "01", 0, -1, 0);
    lsLastYY = commDate.dateAdd(lsYM + "01", -1, 0, 0);
    lsLastMM = commString.mid(lsLastMM, 0, 6);
    lsLastYY = commString.mid(lsLastYY, 0, 6);

    double llSeqNo = 0;
    double ldCnt1 = 0, ldRate1 = 0, ldPp1 = 0;
    double ldCnt2 = 0, ldRate2 = 0, ldPp2 = 0;

    wp.colSet("last_month", lsLastMM);
    wp.colSet("last_year", lsLastYY);

    String sql1 = " select " + " sum(acct_cnt_a) as ld_cnt1 , " + " sum(acct_rate_a) as ld_rate1 , "
        + " sum(bonus_cnt_a) as ld_pp1 " + " from mkt_bonus_stat3 " + " where stat_month = ? "
        + " and acct_type = ? " + " and seq_no = ? ";

    String sql2 = " select " + " sum(acct_cnt_a) as ld_cnt2 , " + " sum(acct_rate_a) as ld_rate2 , "
        + " sum(bonus_cnt_a) as ld_pp2 " + " from mkt_bonus_stat3 " + " where stat_month = ? "
        + " and acct_type = ? " + " and seq_no = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsAcctType = wp.colStr(ii, "acct_type");
      llSeqNo = wp.colNum(ii, "seq_no");

      ldCnt1 = 0;
      ldRate1 = 0;
      ldPp1 = 0;
      ldCnt2 = 0;
      ldRate2 = 0;
      ldPp2 = 0;

      wp.colSet(ii, "wk_bp_area", wp.colStr(ii, "bp_area_s") + " -- " + wp.colStr(ii, "bp_area_e"));

      if (!eqIgno(commString.mid(lsYM, 5, 2), "01")) {
        sqlSelect(sql1, new Object[] {lsLastMM, lsAcctType, llSeqNo});
        ldCnt1 = sqlNum("ld_cnt1");
        ldRate1 = sqlNum("ld_rate1");
        ldPp1 = sqlNum("ld_pp1");
      }

      // --上年度
      sqlSelect(sql2, new Object[] {lsLastYY, lsAcctType, llSeqNo});
      ldCnt2 = sqlNum("ld_cnt2");
      ldRate2 = sqlNum("ld_rate2");
      ldPp2 = sqlNum("ld_pp2");

      wp.colSet(ii, "db_mm_cnt", "" + ldCnt1);
      wp.colSet(ii, "db_mm_rate", "" + ldRate1);
      wp.colSet(ii, "db_mm_point", "" + ldPp1);
      wp.colSet(ii, "db_yy_cnt", "" + ldCnt2);
      wp.colSet(ii, "db_yy_rate", "" + ldRate2);
      wp.colSet(ii, "db_yy_point", "" + ldPp2);

      // --月份差異數量及比率

      if (eqIgno(commString.mid(wp.colStr(ii, "stat_month"), 5, 2), "01")) {
        wp.colSet(ii, "month_cnt", "0");
        wp.colSet(ii, "month_point", "0");
      } else {
        wp.colSet(ii, "month_cnt", wp.colNum(ii, "acct_cnt_a") - wp.colNum(ii, "db_mm_cnt"));
        wp.colSet(ii, "month_point", wp.colNum(ii, "point_cnt_a") - wp.colNum(ii, "db_mm_point"));
      }

      if (wp.colNum(ii, "db_mm_cnt") == 0) {
        wp.colSet(ii, "month_percent", "0");
      } else {
        wp.colSet(ii, "month_percent", wp.colNum(ii, "month_cnt") / wp.colNum(ii, "db_mm_cnt"));
      }

      if (wp.colNum(ii, "db_mm_point") == 0) {
        wp.colSet(ii, "month_p_percent", "0");
      } else {
        wp.colSet(ii, "month_p_percent",
            wp.colNum(ii, "month_point") / wp.colNum(ii, "db_mm_point"));
      }

      // --年度差異數量及比率

      wp.colSet(ii, "year_cnt", wp.colNum(ii, "acct_cnt_a") - wp.colNum(ii, "db_yy_cnt"));

      if (wp.colNum(ii, "db_yy_cnt") == 0) {
        wp.colSet(ii, "year_percent", "0");
      } else {
        wp.colSet(ii, "year_percent", wp.colNum(ii, "year_cnt") / wp.colNum(ii, "db_yy_cnt"));
      }

      wp.colSet(ii, "year_point", wp.colNum(ii, "point_cnt_a") - wp.colNum(ii, "db_yy_point"));

      if (wp.colNum(ii, "db_yy_point") == 0) {
        wp.colSet(ii, "year_p_percent", "0");
      } else {
        wp.colSet(ii, "year_p_percent", wp.colNum(ii, "year_point") / wp.colNum(ii, "db_yy_point"));
      }

      if (ii == 0) {
        tl_count(wp.colStr(ii, "acct_type"));
        wp.colSet(ii, "tl_mp", tlMp);
        wp.colSet(ii, "tl_mpp", tlMpp);
        wp.colSet(ii, "tl_yp", tlYp);
        wp.colSet(ii, "tl_ypp", tlYpp);
        continue;
      }

      if (!eqIgno(wp.colStr(ii, "acct_type"), wp.colStr(ii - 1, "acct_type"))) {
        tl_count(wp.colStr(ii, "acct_type"));
        wp.colSet(ii, "tl_mp", tlMp);
        wp.colSet(ii, "tl_mpp", tlMpp);
        wp.colSet(ii, "tl_yp", tlYp);
        wp.colSet(ii, "tl_ypp", tlYpp);
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
    wp.reportId = "misr100110";


    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();
    String cond1 = "統計月份:" + commString.strToYmd(wp.colStr("stat_month"));
    wp.colSet("cond1", cond1);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "misr100110.xlsx";
    pdf.pageCount = 25;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  public void tl_count(String ls_acct_type) {
    // --前一月
    String sql1 =
        " select " + " sum(acct_cnt_a) as tl_mm_cnt , " + " sum(bonus_cnt_a) as tl_mm_point "
            + " from mkt_bonus_stat3 " + " where stat_month = ? " + " and acct_type = ? ";
    // --去年同期
    String sql2 =
        " select " + " sum(acct_cnt_a) as tl_yy_cnt , " + " sum(bonus_cnt_a) as tl_yy_point "
            + " from mkt_bonus_stat3 " + " where stat_month = ? " + " and acct_type = ? ";

    String sql3 =
        " select " + " sum(acct_cnt_a) as tl_acct_cnt_a , " + " sum(bonus_cnt_a) as tl_point_cnt_a "
            + " from mkt_bonus_stat3 " + " where stat_month = ? " + " and acct_type = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("last_month"), ls_acct_type});
    sqlSelect(sql2, new Object[] {wp.itemStr("last_year"), ls_acct_type});
    sqlSelect(sql3, new Object[] {wp.itemStr("ex_stat_month"), ls_acct_type});

    if (sqlNum("tl_mm_cnt") == 0) {
      tlMp = 0;
    } else {
      tlMp = (sqlNum("tl_acct_cnt_a") - sqlNum("tl_mm_cnt")) / sqlNum("tl_mm_cnt");
    }

    if (sqlNum("tl_mm_point") == 0) {
      tlMpp = 0;
    } else {
      tlMpp = (sqlNum("tl_point_cnt_a") - sqlNum("tl_mm_point")) / sqlNum("tl_mm_point");
    }

    if (sqlNum("tl_yy_cnt") == 0) {
      tlYp = 0;
    } else {
      tlYp = (sqlNum("acct_cnt_a") - sqlNum("tl_yy_cnt")) / sqlNum("tl_yy_cnt");
    }

    if (sqlNum("tl_point_cnt_a") == 0) {
      tlYpp = 0;
    } else {
      tlYpp = (sqlNum("point_cnt_a") - sqlNum("tl_point_cnt_a")) / sqlNum("tl_point_cnt_a");
    }
  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "misr100110";

      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "misr100110_excel.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      String cond1 = "統計月份:" + commString.strToYmd(wp.colStr("stat_month"));
      wp.colSet("cond1", cond1);
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
