/** ***************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-19  V1.00.00  David FU   program initial                            *
* 2018-0718	 V1.00.01  JH			 space not trim	
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
* 110-01-08  V1.00.03  Justin           fix a select bug
* 112-12-29  V1.00.04  Ryan           add for CRM68 excel
******************************************************************************/

package ptrm01;

import java.util.Locale;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDFBatch;

public class Ptrr0000 extends BaseEdit {
  String mExProgramCode = "";
  String mExStartDate = "";
  String mExStartTime = "";
  String mProgName = "ptrr0000";
  private static final String CRM68 = "CRM68";

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
      pdfPrint();
    }

    initButton();
  }

  @Override
  public void initPage() {
    if (empty(strAction)) {
      wp.colSet("ex_start_date", wp.sysDate);
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();  
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_program_code")) == false) {
      wp.whereStr += " and  upper(program_code) like ? ";
      setString(("%" + wp.itemStr("ex_program_code") + "%").toUpperCase(Locale.TAIWAN));
    }
    if (empty(wp.itemStr("ex_report_name")) == false) {
        wp.whereStr += " and  rptname like ? ";
        setString("%" + wp.itemStr("ex_report_name") + "%");
    }
    // if(empty(wp.item_ss("ex_start_date")) == false){
    // wp.whereStr += " and start_date like :start_date ";
    // setString("start_date", wp.item_ss("ex_start_date")+"%");
    // }
    wp.whereStr += sqlStrend(wp.itemStr("ex_start_date"), wp.itemStr("ex_end_date"), "start_date");
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
	  sqlParm.clear();
    wp.pageControl();

    wp.selectSQL = " DISTINCT program_code" + ", rptname " + ", start_date" + ", start_time";

    wp.daoTable = "ptr_batch_rpt";
    wp.whereOrder = " order by start_date desc,start_time desc";
    getWhereStr();

    wp.pageCountSql = "select count(program_code)  from ( "
        + "  SELECT DISTINCT program_code,rptname,start_date,start_time FROM ptr_batch_rpt "
        + wp.whereStr + ")";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }



    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExProgramCode = wp.itemStr("program_code");
    mExStartDate = wp.itemStr("start_date");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mExProgramCode)) {
      mExProgramCode = itemKk("data_k1");
    }
    if (empty(mExStartDate)) {
      mExStartDate = itemKk("data_k2");
    }
    if (empty(mExStartTime)) {
      mExStartTime = itemKk("data_k3");
    }

    wp.selectSQL = "hex(rowid) as rowid " + ", program_code " + ", rptname " + ", start_date"
        + ", seq" + ", kind" + ", report_content";
    wp.daoTable = "ptr_batch_rpt";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  program_code = :program_code ";
    wp.whereStr += " and  start_date = :start_date ";
    setString("program_code", mExProgramCode);
    setString("start_date", mExStartDate);
    setString("start_time", mExStartTime);
    wp.whereOrder = "order by seq";

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, program_code=" + mExProgramCode);
    }
    String report_content = "";
    int sel_ct = wp.selectCnt;
    for (int i = 0; i < sel_ct; i++) {
      report_content += wp.getValue2("report_content", i);
      report_content += "\n";
    }
    wp.colSet("report_content_all", report_content);

  }



  @Override
  public void saveFunc() throws Exception {


  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      // this.btnMode_aud();
    }
  }



  void pdfPrint() throws Exception {
    String lsSql = "";
    if (empty(mExProgramCode)) {
      mExProgramCode = itemKk("data_k1");
    }
    if (empty(mExStartDate)) {
      mExStartDate = itemKk("data_k2");
    }
    if (empty(mExStartTime)) {
      mExStartTime = itemKk("data_k3");
    }
    
    if(wp.itemEq("ex_program_code",CRM68))
    	mProgName = String.format("%s_%s", mProgName,CRM68);
    
    wp.reportId = mProgName;
    // -cond-
    // subTitle();
    // wp.col_set("cond_1", report_subtitle);
    // ===========================
    wp.pageRows = 99999;
    // queryFunc();
    wp.sqlCmd = "select report_content,start_date,seq from ptr_batch_rpt where 1=1 ";
    wp.sqlCmd += sqlCol(mExProgramCode, "program_code");
    wp.sqlCmd += sqlCol(mExStartDate, "start_date");
    wp.sqlCmd += sqlCol(mExStartTime, "start_time");
    wp.sqlCmd += " order by start_date, seq ";
    // System.out.println(wp.sqlCmd);
    // wp.pageCount_sql ="select count(*) from ("+wp.sqlCmd+")";
    pageQuery();
    wp.setListCount(1);
    // int sel_ct = wp.selectCnt;
    // for(int ll=0; ll<sel_ct; ll++) {
    // wp.ddd("[%s]",wp.getValue2("report_content", ll));
    // }
    // wp.setListCount(1);

    TarokoPDFBatch pdf = new TarokoPDFBatch();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 53;
    pdf.pageVert = false; // 直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

}
