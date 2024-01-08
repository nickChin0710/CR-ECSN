/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-09  V1.00.00  Andy Liu   program initial                            *
* 107-07-12  V1.00.01  Andy       Update,debug                               *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *	
******************************************************************************/
package mktr01;

import java.io.InputStream;
import ofcapp.BaseEdit;

import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktr0025 extends BaseEdit {

  InputStream inExcelFile = null;
  String mProgName = "mktr0025";
  String reportSubtitle = "";
  String kk1Batchno = "";

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
    } else if (eqIgno(wp.buttonCode, "R1")) {
      // -資料讀取-
      strAction = "R1";
      dataRead();
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
    String exBatchno = wp.itemStr("ex_batchno");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件
    lsWhere += "and type_code ='2' ";
    lsWhere += "and file_date !='' ";
    lsWhere += "and batch_no = :ex_batch_no2";
    // user搜尋條件
    //lsWhere += sqlCol(exBatchno, "batch_no");
    setString("ex_batch_no2",exBatchno);
    
    //lsWhere += "and batch_no = '"+exBatchno+"'";
    

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
    String exBatchno = wp.itemStr("ex_batchno");
    wp.sqlCmd = "SELECT a.batch_no, " + "a.file_date, " + "a.card_no, "
        + "(SELECT count (*) FROM mkt_card_consume y WHERE y.card_no = a.card_no) AS db_consume_cnt, "
        + "substr (a.file_date, 1, 6) db_file_date, " + "a.id_no, "
        + "(SELECT count (*) AS crd_cnt FROM crd_card c WHERE c.id_p_seqno in (select id_p_seqno from crd_idno where id_no = a.id_no) ) as db_crd_cnt, "
+ "a.name, " /* + "a.line_of_credit_amt, " + "a.age, " */ + "a.sex, " + "a.card_rank, "
        + "0 db_pay_amt, " + "0 db_sub_amt, " + "0 db_total_amt, " + "0 db_total_cnt,"
/* + "'' wk_empty " */+"''" + "FROM mkt_list a " + "WHERE 1 = 1 " + "AND type_code = '2' "
        + "AND file_date != '' and batch_no = :ex_batch_no ";
    
    
    //wp.sqlCmd += sqlCol(exBatchno, "batch_no");
    
    wp.sqlCmd += "ORDER BY a.batch_no, db_file_date,a.id_no ";
    setString("ex_batch_no",exBatchno);
    // System.out.println(wp.sqlCmd);
    // gs_sql = "select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr+wp.whereOrder;
    // 頁面重新計算筆數
    // wp.sqlCmd = "select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr+wp.whereOrder;
    wp.pageCountSql = "select count(*) ct from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ")";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    
  
    pageQuery();

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
    String wkId = "", wkIdPSeqno = "", wkCardNo = "", wkFileDate = "";
    String lsSql = "";
    wp.colSet("user_id", wp.loginUser);
    wp.logSql = false;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      rowCt += 1;
      wkFileDate = wp.colStr(ii, "db_file_date");
      wkIdPSeqno = wp.colStr(ii, "wk_id_p_seqno");
      wkCardNo = wp.colStr(ii, "card_no");
      // 當月有(無)手收消費金額&消費總金額,消費總筆數
      if (!empty(wkCardNo)) {
        lsSql =
            "select (consume_bl_amt + consume_ca_amt + consume_it_amt  + consume_ao_amt + consume_id_amt + consume_ot_amt "
                + "-(sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt) "
                + ") as pay_amt "
                + ", (sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt) as sub_amt "
                + ", (consume_bl_amt + consume_ca_amt + consume_it_amt  + consume_ao_amt + consume_id_amt + consume_ot_amt) as total_amt "
                + ", (consume_bl_cnt + consume_ca_cnt + consume_it_cnt  + consume_ao_cnt + consume_id_cnt + consume_ot_cnt) as total_cnt "
                + "from mkt_card_consume " + "where card_no = :ex_card_no "
                + "and acct_month = :acct_month ";
        setString("ex_card_no", wkCardNo);
        setString("acct_month", wkFileDate);
        // System.out.println(wk_card_no);
        // System.out.println(wk_file_date);
        // System.out.println(ls_sql);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          wp.colSet(ii, "db_pay_amt", sqlStr("pay_amt"));
          wp.colSet(ii, "db_sub_amt", sqlStr("sub_amt"));
          wp.colSet(ii, "db_total_amt", sqlStr("total_amt"));
          wp.colSet(ii, "db_total_cnt", sqlStr("total_cnt"));
        }
      } else {
        wp.colSet(ii, "db_pay_amt", "0");
        wp.colSet(ii, "db_sub_amt", "0");
        wp.colSet(ii, "db_total_amt", "0");
        wp.colSet(ii, "db_total_cnt", "0");
      }
    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {

  }

  void subTitle() {
    String exBatchno1 = wp.itemStr("ex_batchno1");
    String exBatchno2 = wp.itemStr("ex_batchno2");

    String tmpStr = "列表條件 : ";
    // 分析期間
    if (empty(exBatchno1) == false || empty(exBatchno2) == false) {
      tmpStr += " [批號] : ";
      if (empty(exBatchno1) == false) {
        tmpStr += exBatchno1 + " 起 ";
      }
      if (empty(exBatchno2) == false) {
        tmpStr += " ~ " + exBatchno2 + " 迄 ";
      }
    }
    reportSubtitle = tmpStr;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      // subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
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
    // pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_acct_type
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 and card_indicator='2' order by acct_type ");
    } catch (Exception ex) {

    }
  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

}

