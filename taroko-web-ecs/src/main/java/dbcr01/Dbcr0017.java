/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-13  V1.00.00  Andy Liu   program initial                            *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*                               
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *                                            *	
******************************************************************************/
package dbcr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Dbcr0017 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbcr0017";

  String condWhere = "";
  String reportSubtitle1 = "";
  String reportSubtitle2 = "";

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
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

  private void getWhereStr() throws Exception {
    wp.whereStr = "where 1=1 " + " and a.in_main_date <> '' " + " and a.reject_code = '' "
        + " and a.in_main_error = '0' ";
    if (empty(wp.itemStr("ex_reg_bank_no")) == false) {
      wp.whereStr += " and  a.reg_bank_no = :reg_bank_no ";
      setString("reg_bank_no", wp.itemStr("ex_reg_bank_no"));
    }
    if (empty(wp.itemStr("ex_to_nccc_date")) == false) {
      wp.whereStr += " and  a.to_nccc_date = :to_nccc_date ";
      setString("to_nccc_date", wp.itemStr("ex_to_nccc_date"));
    }
    if (empty(wp.itemStr("ex_emboss_source")) == false) {
      wp.whereStr += " and  a.emboss_source = :emboss_source ";
      setString("emboss_source", wp.itemStr("ex_emboss_source"));
    }
    if ("Y".equals(wp.itemStr("ex_apply_id_flag"))) {
      wp.whereStr += " and  b.employ_no is not null ";
    } else if ("N".equals(wp.itemStr("ex_apply_id_flag"))) {
      wp.whereStr += " and  b.employ_no is null ";
    }
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + "a.reg_bank_no, " + "a.group_code, " + "a.card_no, " + "a.apply_id, "
        + "a.chi_name, " + "b.chi_name as employ_name, " + "a.act_no, "
        + "(c.home_area_code1 || '-' || c.home_tel_no1 || '-' || c.home_tel_ext1) as home_tel, "
        + "(c.office_area_code1 || '-' || c.office_tel_no1 || '-' || c.office_tel_ext1) as office_tel, "
        + "c.cellar_phone, " + "a.crt_bank_no, " + "a.to_nccc_date ";
    wp.daoTable = "dbc_emboss a " + "left join crd_employee b on a.apply_id = b.employ_no "
        + "left join dbc_idno c on a.apply_id = c.id_no ";

    getWhereStr();

    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  void subTitle() {
    String title1 = "";
    String title2 = "";

    title1 += "分行代號: " + wp.itemStr("ex_reg_bank_no");
    title2 += "中華民國" + String.valueOf(Integer.parseInt(wp.sysDate.substring(0, 4)) - 1911) + "年"
        + wp.sysDate.substring(4, 6) + "月" + wp.sysDate.substring(6) + "日　";
    if (wp.currPage < 10) {
      title2 += "第00" + wp.currPage + "頁";
    } else if (wp.currPage < 100) {
      title2 += "第0" + wp.currPage + "頁";
    } else {
      title2 += "第" + wp.currPage + "頁";
    }

    reportSubtitle1 = title1;
    reportSubtitle2 = title2;
  }

  void pdfPrint() throws Exception {
    wp.reportId = progName;

    subTitle();
    wp.colSet("cond_1", reportSubtitle1);
    wp.colSet("cond_2", reportSubtitle2);

    wp.pageRows = 99999;
    queryFunc();

    wp.colSet("user_id", wp.loginUser);
    wp.colSet("row_ct", intToStr(wp.selectCnt));

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = progName + ".xlsx";
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
      wp.optionKey = wp.colStr("ex_reg_bank_no");
      this.dddwList("dddw_reg_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

