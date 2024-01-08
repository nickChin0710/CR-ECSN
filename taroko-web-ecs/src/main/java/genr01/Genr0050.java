/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-07  V1.00.00  Andy Liu   program initial                            *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 	、
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package genr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Genr0050 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "genr0050";

  String condWhere = "";

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

  private boolean getWhereStr() throws Exception {
    String branchf = wp.itemStr("ex_branch_f");
    String branchu = wp.itemStr("ex_branch_u");

    String lsWhere = "where 1=1";


    if (empty(branchf) == false) {
      lsWhere += " and branch >= :branch_f";
      setString("branch_f", branchf);
    }

    if (empty(branchu) == false) {
      lsWhere += " and branch <= :branch_u";
      setString("branch_u", branchu);
    }

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
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

    wp.selectSQL = "" + "v.branch , " + "p.curr_chi_name, " + "v.brief_chi_name , "
        + "v.brief_eng_name , " + "v.comp_addr , " + "v.comp_name , " + "v.user_code , "
        + "v.user_pass , " + "v.south_flag , " + "v.area_flag , " + "v.connect_flag , "
        + "v.brn_test , " + "v.chi_addr_1 , " + "v.chi_addr_2 , " + "v.chi_addr_3 , "
        + "v.chi_addr_4 , " + "v.chi_addr_5  ";

    wp.daoTable = "gen_brn v left join ptr_currcode p on v.curr_code=p.curr_code_gl ";
    wp.whereOrder = " order by v.branch";

    // setParameter();

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
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String branchf = wp.itemStr("ex_branch_f");
      String branchu = wp.itemStr("ex_branch_u");
      String tmpStr = "分行代碼: " + branchf + " ~ " + branchu;
      wp.colSet("cond_1", tmpStr);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      wp.pageRows = 99999;
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
    String tmpStr = "PDFTEST: ";
    wp.colSet("cond_1", tmpStr);
    wp.pageRows = 9999;
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
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

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_branch_f");
      this.dddwList("dddw_branch_f", "gen_brn", "branch", "brief_chi_name",
          "where 1=1 group by branch,brief_chi_name order by branch");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_branch_u");
      this.dddwList("dddw_branch_u", "gen_brn", "branch", "brief_chi_name",
          "where 1=1 group by branch,brief_chi_name order by branch");

    } catch (Exception ex) {
    }
  }

}
