/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-25  V1.00.00  Andy Liu   program initial                            *
* 107-05-03  V1.00.01  Andy		  update : DE BUG SQL                        *
* 108-12-19  V1.00.02  Amber	  update : ptr_branch==>gen_brn 		     *
* 109-04-23  V1.00.04  yanghan  修改了變量名稱和方法名稱*
* 109-12-25  V1.00.05  Justin        parameterize sql
******************************************************************************/
package dbcr01;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import java.util.Calendar;
import java.util.Date;

public class Dbcr0025 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbcr0025";

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
    // 本表用SQL Union語法讀取資料 where條件均寫在queryRead()

    // wp.whereStr = ls_where;
    // setParameter();
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

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String exBankNo = wp.itemStr("ex_bank_no");
    String exBatchno1 = "", ex_batchno2 = "";

    String lsWhere = "", lsWhere1 = "", lsWhere2 = "", lsWhere3 = "", lsWhere4 = "",
        lsWhere5 = "";
    // 固定搜尋條件
    lsWhere += " and e.recno > 0 ";

    // user搜尋條件
    // **共用條件部分start*******
    // ex_date1製卡日期1==>轉batchno
    if (empty(date1) == false) {
      Calendar c = Calendar.getInstance();
      SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
      String sdate = date1;
      Date date = null;
      try {
        date = dateformat.parse(sdate);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      c.setTime(date);
      c.add(Calendar.DATE, -5);
      dateformat.format(c.getTime());
      exBatchno1 = dateformat.format(c.getTime());

      lsWhere += "and e.batchno >= :e.batchno ";
      setString("e.batchno", strMid(ex_batchno2, 2, 6)+"99");
    }
    // ex_date2製卡日期2==>轉batchno
    if (empty(date2) == false) {
      Calendar c = Calendar.getInstance();
      SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
      String sdate = date2;
      Date date = null;
      try {
        date = dateformat.parse(sdate);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      c.setTime(date);
      c.add(Calendar.DATE, +5);
      dateformat.format(c.getTime());
      ex_batchno2 = dateformat.format(c.getTime());

      lsWhere += "and e.batchno <= :e.batchno ";
      setString("e.batchno", strMid(ex_batchno2, 2, 6)+"99");
    }

    // ex_bank_no分行
    if (empty(exBankNo) == false) {
      lsWhere += " and e.reg_bank_no = :e.reg_bank_no ";
      setString("e.reg_bank_no",exBankNo);
    }

    // **共用條件部分end*******

    // 以下為各自where條件
    
    lsWhere1 = " where 1=1 ";
    lsWhere2 = " where 2=2 and (b.card_no = e.card_no) ";
    lsWhere3 = " where 3=3 ";
    lsWhere4 = " where 4=4 ";
    lsWhere5 = " where 5=5 ";
    
    if (empty(date1) == false) {
    	lsWhere1 += " and e.in_main_date <= :date1 ";
    	lsWhere2 += " and b.rtn_ibm_date <= :date1 ";
    	lsWhere3 += " and e.crt_date <= :date1 ";
    	lsWhere4 += " and e.crt_date <= :date1 ";
    	lsWhere5 += " and e.crt_date <= :date1 ";
    	setString("date1", date1);
	}
    if (empty(date2) == false) {
    	lsWhere1 += " and e.in_main_date >= :date2 ";
    	lsWhere2 += " and b.rtn_ibm_date >= :date2 ";
    	lsWhere3 += " and e.crt_date >= :date2 ";
    	lsWhere4 += " and e.crt_date >= :date2 ";
    	lsWhere5 += " and e.crt_date >= :date2 ";
    	setString("date2", date2);
	}
    
    // dbc_emboss ls_where1
    lsWhere1 += lsWhere;
    lsWhere1 += " and e.apply_source <> 'P' " + " and e.in_main_error = '0' "
        + " and e.apply_source <> 'C' ";
    lsWhere1 += " group by e.batchno, e.in_main_date, e.reg_bank_no ";

    // dbc_debit ls_where2
    lsWhere2 += lsWhere;
    lsWhere2 += " and e.apply_source <> 'P' "
        + " and ((decode(e.in_main_date,'','N', e.in_main_date) = 'N' and b.rtn_code <> '00') or e.in_main_error <> '0') "
        + " and e.apply_source <> 'C' ";
    lsWhere2 += " group by e.batchno, b.rtn_ibm_date, e.reg_bank_no ";

    // dbc_emboss ls_where3
    lsWhere3 += lsWhere;
    lsWhere3 +=
        " and e.error_code = 'N' and e.apply_source = 'P' and e.in_main_error = '0' and e.apply_source <> 'C' ";
    lsWhere3 += " group by e.batchno, e.crt_date, e.reg_bank_no ";

    // dbc_emboss ls_where4
    lsWhere4 += lsWhere;
    lsWhere4 += " and (e.error_code <> 'N' or e.in_main_error <> '0') "
        + " and e.apply_source = 'P' " + " and e.apply_source <> 'C' ";
    lsWhere4 += " group by e.batchno, e.crt_date, e.reg_bank_no ";
    
    // dbc_emap_tmp ls_where5
    lsWhere5 += lsWhere;
    lsWhere5 +=
        " and e.apply_source <> 'P' " + " and e.check_code <> '0' " + " and e.apply_source <> 'C' ";
    lsWhere5 += " group by e.batchno, e.crt_date, e.reg_bank_no ";

    
    wp.sqlCmd = " select db_date, " + "reg_bank_no, " + "sum(okcnt) okcnt_ct, "
        + "sum (asp) asp_ct, " + "sum (asao) asao_ct, " + "sum (asv) asv_ct, "
        + "sum (asd) asd_ct, " + "sum (asp+asao+asv+asd) success_ct, " + "sum (errcnt) errct_ct, "
        + "sum (tmp_all) temp_all_ct," + "'' wk_temp " + "from (";
    wp.sqlCmd += "select 'c1' db_1, " + "e.batchno, " + "e.in_main_date db_date, "
        + "e.reg_bank_no, " + "count (e.bank_actno) as okcnt, " + "0 as asP, "
        + "count (decode (e.apply_source,'A',1,'0',1)) as asAO, "
        + "count (decode (e.apply_source,'V',1,' ',1)) as asV, "
        + "count (decode (e.apply_source, 'D', 1)) as asD, " + "0 as errcnt, " + "1 as tmp_all "
        + "from dbc_emboss e ";
    wp.sqlCmd += lsWhere1;
    wp.sqlCmd += " union " + "select 'c2' db_1, " + "e.batchno, " + "b.rtn_ibm_date db_date, "
        + "e.reg_bank_no, " + "count (e.bank_actno) as okcnt, " + "0 as asP, " + "0 as asAO, "
        + "0 as asV, " + "0 as asD, " + "count (e.bank_actno) as errcnt, " + "1 as tmp_all "
        + "from dbc_debit b, dbc_emboss e ";
    wp.sqlCmd += lsWhere2;
    wp.sqlCmd += "union " + "select 'p1' db_1, " + "e.batchno, " + "e.crt_date db_date, "
        + "e.reg_bank_no, " + "count (e.bank_actno) as okcnt, "
        + "count (decode (e.apply_source, 'P', 1)) as asP, " + "0 as asAO, " + "0 as asV, "
        + "0 as asD, " + "0 as errcnt, " + "1 as tmp_all " + "from dbc_emboss e ";
    wp.sqlCmd += lsWhere3;
    wp.sqlCmd += "union " + "select 'p2' db_1, " + "e.batchno, " + "e.crt_date db_date, "
        + "e.reg_bank_no, " + "count (e.bank_actno) as okcnt, " + "0 as asP, " + "0 as asAO, "
        + "0 as asV, " + "0 as asD, " + "count (e.bank_actno) as errcnt, " + "1 as tmp_all "
        + "from dbc_emboss e";
    wp.sqlCmd += lsWhere4;
    wp.sqlCmd += "union " + "select 'c2' db_1, " + "e.batchno, " + "e.crt_date db_date, "
        + "e.reg_bank_no, " + "count (e.bank_actno) as okcnt, " + "0 as asP, " + "0 as asAO, "
        + "0 as asV, " + "0 as asD, " + "count (e.bank_actno) as errcnt, " + "1 as tmp_all "
        + "from dbc_emap_tmp e";
    wp.sqlCmd += lsWhere5;
    wp.sqlCmd += ") " + "group by db_date,reg_bank_no " + "order by db_date,reg_bank_no ";
    // System.out.println("sqlCmd :"+wp.sqlCmd);

    // 頁面筆數計算
    wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";

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
    int row = 0;
    String lsSql = "";
    String dbRegBankNo = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row += 1;
      wp.colSet(ii, "group_ct", "1");

      // db_reg_bank_no分行中文
      dbRegBankNo = wp.colStr(ii, "reg_bank_no");
      lsSql = "select full_chi_name " + "from gen_brn ";
      lsSql += "where 1=1 ";
      lsSql += sqlCol(dbRegBankNo, "branch");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbRegBankNo = dbRegBankNo + "[" + sqlStr("full_chi_name") + "]";
      }
      wp.colSet(ii, "db_reg_bank_no", dbRegBankNo);
    }
    wp.colSet("row_ct", intToStr(row));
  }


  void subTitle() {
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String exBankNo = wp.itemStr("ex_bank_no");

    String all = "";

    // ex_date製卡日期
    if (empty(date1) == false || empty(date2) == false) {
      all += "製卡日期 : ";
      if (empty(date1) == false) {
        all += date1;
      }
      if (empty(date2) == false) {
        all += " ~ " + date2;
      }
    }
    // ex_bank_no分行
    if (empty(exBankNo) == false) {
      all += " 分行 : " + exBankNo;
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
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 40;
    pdf.pageVert = true; // 直印
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
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_bank_no");
      dddwList("dddw_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");

      // dddw_apply_source
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_apply_source");
      dddwList("dddw_apply_source", "dbc_apply_source", "apply_source", "apply_source_name",
          "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

