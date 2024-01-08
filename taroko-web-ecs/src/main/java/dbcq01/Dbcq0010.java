/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-05  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 108-12-19  V1.00.02  Amber	  update : ptr_branch==>gen_brn 		     *
* 109-04-23  V1.00.03  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package dbcq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;


public class Dbcq0010 extends BaseReport {

	InputStream inExcelFile = null;
	String progName = "Dbcq0010";

	String condWhere = "";
	String reportSubtitle ="";
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

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    // wp.col_set("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
	  sqlParm.clear();
    String exCardNo = wp.itemStr("ex_card_no");

    String lsWhere = "where 1=1 ";
    // 固定搜尋條件
    lsWhere += "and  a.apply_source = 'P' ";

    // user搜尋條件
    // ex_card_no
    lsWhere += sqlCol(exCardNo, "a.card_no", "like%");

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

    wp.selectSQL = "" + "a.card_no, " + "a.act_no, " + "b.full_chi_name, " + "a.bank_actno  ";
    wp.daoTable = " dbc_emboss a left join gen_brn b on a.reg_bank_no = b.branch ";
    wp.whereOrder = " order by a.card_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // wp.daoTable);

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
    list_wkdata();
  }

  void list_wkdata() throws Exception {
    int row = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row += 1;
      wp.colSet(ii, "group_ct", "1");

    }
    wp.colSet("row_ct", intToStr(row));
  }

  void subTitle() {
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String exActNo = wp.itemStr("ex_act_no");
    String id = wp.itemStr("ex_id");
    String exChangeId = wp.itemStr("ex_change_id");

    String all = "列表條件 : ";

    // ex_date異動日期
    if (empty(date1) == false || empty(date2) == false) {
      all += "  進件日期 : ";
      if (empty(date1) == false) {
        all += date1 + " 起 ";
      }
      if (empty(date2) == false) {
        all += " ~ " + date2 + " 迄 ";
      }
    }
    // ex_act_no存款帳號
    if (empty(exActNo) == false) {
      all += " 存款帳號 : " + exActNo;
    }

    // ex_id異動前ID
    if (empty(id) == false) {
      all += " 異動前ID : " + id;
    }

    // ex_change_id異動後ID
    if (empty(exChangeId) == false) {
      all += " 異動後ID : " + exChangeId;
    }

    reportSubtitle = all;

  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = progName + ".xlsx";

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
    wp.reportId = progName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);

    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);

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
      // dddw_bank_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_bank_no");
      // dddw_list("dddw_bank_no", "ptr_branch", "branch", "branch_name", "where 1=1 ");

      // dddw_apply_source
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_apply_source");
      // dddw_list("dddw_apply_source", "dbc_apply_source", "apply_source", "apply_source_name",
      // "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

