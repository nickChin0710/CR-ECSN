/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-04  V1.00.00  yash       program initial                            *
* 107-04-13  V1.00.01  Andy       Update UI,report format                    *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *	
* 112-05-03  V1.00.03  Ryan       增加科子細目類別查詢條件                                                                      *	
******************************************************************************/
package genr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Genr0020 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "genr0020";
  String mExAcNo = "";
  String mExAcNo1 = "";
  String condWhere = "";
  String reportSubtitle = "";

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
    } else if (eqIgno(wp.buttonCode, "R1")) { // 
        strAction = "R1";
        wp.listCount[0] = wp.itemBuffLen("SER_NUM");
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
    String exAcNo = wp.itemStr("ex_ac_no");
    String exAcNo1 = wp.itemStr("ex_ac_no1");
    // System.out.println("ex_ac_no:"+ex_ac_no+" ex_ac_no1:"+ex_ac_no1);
    String lsWhere = "where 1=1";

    if (empty(exAcNo) == false) {
    	lsWhere += " and ac_no >= :ac_no";
      setString("ac_no", exAcNo);
    }
    if (empty(exAcNo1) == false) {
    	lsWhere += " and ac_no <= :ac_no1";
      setString("ac_no1", exAcNo1);
    }
    if(!wp.itemEmpty("ex_acno")) {
    	lsWhere += " and ac_no like :ex_acno";
        setString("ex_acno", wp.itemStr("ex_acno") + "%");
    }

    // System.out.println("ls_where:"+ls_where);
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
    String exAcNo = wp.itemStr("ex_ac_no");
    setString("ac_no", exAcNo);
    String exAcNo1 = wp.itemStr("ex_ac_no1");
    setString("ac_no1", exAcNo1);

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "ac_no , " + "ac_full_name , " + "memo3_flag , " + "memo3_kind , "
        + "dr_flag , " + "cr_flag , " + "uf_2ymd(mod_time) as mod_date  ";

    wp.daoTable = "gen_acct_m";

    wp.whereOrder = " order by ac_no";

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
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsSql = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      // wp.col_set(ii,"group_cnt","1");
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String exAcNo = wp.itemStr("ex_ac_no");
    String exAcNo1 = wp.itemStr("ex_ac_no1");
    String tmpStr = "";
    // 傳送日期
    if (empty(exAcNo) == false || empty(exAcNo1) == false) {
      tmpStr += "科子細目代碼 : ";
      if (empty(exAcNo) == false) {
        tmpStr += exAcNo + " 起 ";
      }
      if (empty(exAcNo1) == false) {
        tmpStr += " ~ " + exAcNo1 + " 迄 ";
      }
    }
    reportSubtitle = tmpStr;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

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
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
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
    	
		StringBuffer bf = new StringBuffer();
		bf.append("where 1=1 ");

		if (!wp.itemEmpty("ex_acno")) {
			bf.append(" and ac_no like '").append(wp.itemStr("ex_acno")).append("%'");
		}
		bf.append(" group by ac_no,ac_full_name order by ac_no ");

		wp.initOption = "--";
		wp.optionKey = wp.itemStr("ex_ac_no");
		this.dddwList("dddw_ac_no", "gen_acct_m", "ac_no", "ac_full_name", bf.toString());
		wp.optionKey = wp.itemStr("ex_ac_no1");
		this.dddwList("dddw_ac_no1", "gen_acct_m", "ac_no", "ac_full_name", bf.toString());

    } catch (Exception ex) {
    }
  }

}
