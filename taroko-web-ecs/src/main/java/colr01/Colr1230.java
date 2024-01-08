/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/20  V1.00.00   phopho     program initial                           *
*  109-05-06  V1.00.01  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package colr01;

import ofcapp.AppMsg;
import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr1230 extends BaseReport {
  CommString commString = new CommString();
  String mProgName = "colr1230";


  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      // dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      // is_action = "R";
      // dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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

    // dddw_select();
    initButton();
  }

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exDateS");
    String lsDate2 = wp.itemStr("exDateE");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[報送日期-起迄]  輸入錯誤");
      return false;
    }
    String lsCode1 = wp.itemStr("exMcodeS");
    String lsCode2 = wp.itemStr("exMcodeE");
    if (this.chkStrend(lsCode1, lsCode2) == false) {
      alertErr2("[M-code 起迄]  輸入錯誤");
      return false;
    }
    if (empty(wp.itemStr("exDateS")) && empty(wp.itemStr("exDateE"))
        && empty(wp.itemStr("exMcodeS")) && empty(wp.itemStr("exMcodeE"))
        && empty(wp.itemStr("exId"))) {
      alertErr2("請輸入查詢條件");
      return false;
    }

    wp.whereStr = " where 1=1 " + " and (proc_flag = 'Y') ";

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and proc_date >= :proc_dates ";
      setString("proc_dates", wp.itemStr("exDateS"));
    }
    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and proc_date <= :proc_datee ";
      setString("proc_datee", wp.itemStr("exDateE"));
    }
    if (empty(wp.itemStr("exId")) == false) {
      wp.whereStr += " and id_no = :id_no ";
      setString("id_no", wp.itemStr("exId"));
    }
    if (empty(wp.itemStr("exMcodeS")) == false) {
      wp.whereStr +=
          " and decode(inst_s_mcode,'0A',0,'0B',0,'0C',0,'0D',0,'0E',0,to_number(nvl(inst_s_mcode,'0'))) >= :inst_s_mcodes ";
      setString("inst_s_mcodes", wp.itemStr("exMcodeS"));
    }
    if (empty(wp.itemStr("exMcodeE")) == false) {
      wp.whereStr +=
          " and decode(inst_s_mcode,'0A',0,'0B',0,'0C',0,'0D',0,'0E',0,to_number(nvl(inst_s_mcode,'0'))) <= :inst_s_mcodee ";
      setString("inst_s_mcodee", wp.itemStr("exMcodeE"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = " id_no, " + " id_code, " + " chi_name, " + " uf_hi_idno(id_no) hi_id_no, "
        + " uf_hi_cname(chi_name) hi_chi_name, " + " proc_date, " + " acct_type, "
        + " inst_s_mcode, " + " inst_s_date, " + " inst_e_date, " + " tran_type, " + " inst_flag, "
        + " inst_seqno ";

    wp.daoTable = " col_cs_instjcic ";

    wp.whereOrder = " order by proc_date, id_no ";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "tran_type");
      wp.colSet(ii, "tt_tran_type", commString.decode(wkData, ",A,C,D", ",A.新增,C.異動,D.刪除"));

      wkData = wp.colStr(ii, "inst_flag");
      wp.colSet(ii, "tt_inst_flag", commString.decode(wkData, ",1,2,3,4", ",1.達成個別協商,2.提前清償,3.毀諾,4.毀諾後清償"));

      wkData = wp.colStr(ii, "proc_date");
      wp.colSet(ii, "file_name", "017" + wkData.substring(4, 8) + "z.za2");
    }
    wp.colSet("sum_fisc_rows", numToStr(wp.selectCnt, ""));
  }

  @Override
  public void querySelect() throws Exception {

  }

  void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_mod_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_mod_date2"));
      wp.colSet("cond_1", cond1);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
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
    if (getWhereStr() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = mProgName;
    wp.pageRows = 9999;

    String cond1 = "報送日期: " + commString.strToYmd(wp.itemStr("exDateS")) + " -- "
        + commString.strToYmd(wp.itemStr("exDateE"));
    wp.colSet("cond_1", cond1);
    wp.colSet("reportName", mProgName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
