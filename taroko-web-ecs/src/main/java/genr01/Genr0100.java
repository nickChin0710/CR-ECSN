/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-22  V1.00.01  yash        program initial                           *
* 107-01-29  V1.00.02  andy        update                                    *
* 109-04-21  V1.00.03  YangFang   updated for project coding standard        *
******************************************************************************/

package genr01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Genr0100 extends BaseEdit {
  String mExProgramCode = "";
  String mExStartDate = "";
  String mExStartTime = "";
  String mProgName = "genr0100";

  @Override
  public void initPage() {
    if (empty(strAction)) {
      wp.colSet("ex_start_date", wp.sysDate);
    }
  }

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_program_code")) == false) {
      wp.whereStr += " and  program_code like :program_code ";
      setString("program_code", wp.itemStr("ex_program_code") + "%");
    }
    if (empty(wp.itemStr("ex_start_date")) == false) {
      wp.whereStr += " and  start_date = :start_date ";
      setString("start_date", wp.itemStr("ex_start_date"));
    }
    // wp.whereStr += "group by PROGRAM_CODE, start_date, rptname,start_time";
    return true;
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

    wp.selectSQL = " DISTINCT program_code" + ", rptname " + ", start_date" + ", start_time";

    wp.daoTable = "ptr_batch_memo3";
    wp.whereOrder = " order by start_date,start_time ";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.selectCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExProgramCode = wp.itemStr("program_code");
    mExStartDate = wp.itemStr("start_date");
    mExStartTime = wp.itemStr("start_date");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {


  }

  void pdfPrint() throws Exception {
    String ls_sql = "";
    if (empty(mExProgramCode)) {
      mExProgramCode = itemKk("data_k1");
    }
    if (empty(mExStartDate)) {
      mExStartDate = itemKk("data_k2");
    }
    if (empty(mExStartTime)) {
      mExStartTime = itemKk("data_k3");
    }
    wp.reportId = mProgName;
    // -cond-
    // ===========================
    wp.pageRows = 99999;
    // queryFunc();
    wp.sqlCmd = "select txt_content from ptr_batch_memo3 where 1=1 ";
    wp.sqlCmd += sqlCol(mExProgramCode, "program_code");
    wp.sqlCmd += sqlCol(mExStartDate, "start_date");
    wp.sqlCmd += sqlCol(mExStartTime, "start_time");
    wp.sqlCmd += " order by start_date, seq ";
    // System.out.println(wp.sqlCmd);
    wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";
    pageQuery();
    wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 54;
    pdf.pageVert = true; // 直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void initButton() {

    if (wp.respHtml.indexOf("_detl") > 0) {
      // this.btnMode_aud();
    }
  }

}
