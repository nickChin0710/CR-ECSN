/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-09  V1.00.00  Andy Liu   program initial                            *
* 107-07-12  V1.00.01  Andy       Update,debug                               *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *	
* 110-12-08  V1.00.03  Ryan       fix a sql injection bug                    *
******************************************************************************/
package mktr01;

import java.io.InputStream;
import ofcapp.BaseEdit;

import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktr0020 extends BaseEdit {

  InputStream inExcelFile = null;
  String mProgName = "mktr0020";
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
    } else if (eqIgno(wp.buttonCode, "XLS1")) { // -Excel-
      strAction = "XLS1";
      // wp.setExcelMode();
      xlsPrint1();
    } else if (eqIgno(wp.buttonCode, "PDF1")) { // -PDF-
      strAction = "PDF1";
      // wp.setExcelMode();
      pdfPrint1();
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
    String exBatchno1 = wp.itemStr("ex_batchno1");
    String exBatchno2 = wp.itemStr("ex_batchno2");
    
//    System.out.println(exBatchno1+ exBatchno2);

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件
    lsWhere += "and type_code ='2' ";
    lsWhere += "and file_date !='' ";
    // user搜尋條件
 //   lsWhere += sqlStrend(exBatchno1, exBatchno2, "batch_no");
    
 //   System.out.println(sqlStrend(exBatchno1, exBatchno2, "batch_no")); 
    
    if(!empty(exBatchno1)) {
        lsWhere += " and batch_no >= :ex_batchno1";
        setString("ex_batchno1", exBatchno1);
    }
   
    if(!empty(exBatchno2)) {
    	lsWhere += " and  batch_no <= :ex_batchno2";   
    	setString("ex_batchno2", exBatchno2);
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

    wp.selectSQL = ""
			+ "batch_no, "
			+ "max(file_date) file_date,"
			+ "substr(file_date,1,6) db_file_date, "
			+ "0 db_cnt, "
			+ "0 db_cnt1, "
			+ "0 db_consume_cnt1, "
			+ "0 db_pay_amt, "
			+ "0 db_pay_amt1, "
			+ "0 db_sub_amt, "			
			+ "'' wk_empty ";   
	wp.daoTable = " mkt_list ";
    wp.whereOrder = " group by batch_no, substr(file_date,1,6) ";
    wp.whereOrder += " order by batch_no, substr(file_date,1,6) ";
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // gs_sql = "select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr+wp.whereOrder;
    // 頁面重新計算筆數
    //wp.sqlCmd = "select " + wp.selectSQL 
    //		               + " from " + wp.daoTable 
    //		               + wp.whereStr 
    //		               + wp.whereOrder;
    //wp.pageCountSql = "select count(*) ct from (";
    //wp.pageCountSql += wp.sqlCmd;
   // wp.pageCountSql += ")";

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
    String wpStatYm = "", wpCorpPSeqno = "";
    String lsSql = "";
    wp.colSet("user_id", wp.loginUser);
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      // 寄送戶數
      lsSql = "select count(*) as cnt " + "FROM mkt_list " + "where TYPE_CODE='2' "
          + "and file_date !='' ";
      lsSql += sqlCol(wp.colStr(ii, "batch_no"), "batch_no");
      // System.out.println(ls_sql);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_cnt", sqlStr("cnt"));
      }

      // 寄送卡數
      lsSql = "select count(*) as cnt " + "FROM mkt_list " + "where TYPE_CODE='2' "
          + "and file_date !='' ";
      lsSql += sqlCol(wp.colStr(ii, "batch_no"), "batch_no");
      // System.out.println(ls_sql);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_cnt1", sqlStr("cnt"));
      }
      // 消費卡數
      lsSql = "select count(*) as consume_cnt1 " + "from mkt_card_consume "
          + "where card_no in (select card_no " + "FROM mkt_list " + "where TYPE_CODE='2' "
          + "and batch_no = :batch_no " + "and file_date !='')";
      setString("batch_no", wp.colStr(ii, "batch_no"));
      sqlSelect(lsSql);
      // System.out.println(ls_sql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_consume_cnt1", sqlStr("consume_cnt1"));
      }
      // 近半年有手收消費金額
      lsSql =
          "select sum(consume_bl_amt + consume_ca_amt + consume_it_amt  + consume_ao_amt + consume_id_amt + consume_ot_amt "
              + "-(sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt)) as pay_amt1 "
              + "from mkt_card_consume " + "where card_no in (select card_no " + "FROM mkt_list "
              + "where type_code='2' " + "and batch_no = :batch_no " + "and file_date !='') "
              + "and ACCT_MONTH >= to_char(add_months(to_date(:file_date,'yyyymmdd'), -6),'yyyymm') "
              + "and ACCT_MONTH <= to_char(to_date(:file_date,'yyyymmdd'),'yyyymm')";
      setString("file_date", wp.colStr(ii, "file_date"));
      setString("batch_no", wp.colStr(ii, "batch_no"));
      // System.out.println(ls_sql);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_pay_amt", sqlStr("pay_amt1"));
      }
      // 近一年有(無)手收消費金額
      lsSql =
          "select sum(consume_bl_amt + consume_ca_amt + consume_it_amt  + consume_ao_amt + consume_id_amt + consume_ot_amt "
              + "-(sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt) "
              + ") as pay_amt "
              + ",sum(sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt) as sub_amt "
              + "from mkt_card_consume " + "where card_no in (select card_no " + "FROM mkt_list "
              + "where type_code='2' " + "and batch_no = :batch_no " + "and file_date<>'') "
              + "and  acct_month >= to_char(add_months(to_date(:file_date,'yyyymmdd'), -12),'yyyymm') "
              + "and acct_month <= to_char(to_date(:file_date,'yyyymmdd'),'yyyymm')";
      setString("file_date", wp.colStr(ii, "file_date"));
      setString("batch_no", wp.colStr(ii, "batch_no"));
      // System.out.println(ls_sql);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_pay_amt1", sqlStr("pay_amt"));
        wp.colSet(ii, "db_sub_amt", sqlStr("sub_amt"));
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
    kk1Batchno = itemKk("data_k1");
    if (empty(kk1Batchno)) {
      kk1Batchno = wp.itemStr("kk_batchno");
    }
    wp.sqlCmd = "select batch_no, "
			+ "file_date, "
			+ "substr (file_date, 1, 6) db_file_date, "
			+ "id_no, "
			+ "name, "
			+ "line_of_credit_amt, "
			+ "age, "
			+ "sex, "
			+ "card_rank, "
			+ "0 db_crd_cnt, "
			+ "0 consume_cnt, "
			+ "0 db_pay_amt, "
			+ "0 db_sub_amt, "
			+ "0 db_total_amt, "
			+ "0 db_total_cnt, "
			+ "'' wk_empty "
			+ "from mkt_list "
	        + "where 1 = 1 and type_code = '2' and file_date != '' ";
    wp.sqlCmd += sqlCol(kk1Batchno, "batch_no");
    wp.sqlCmd += " order by  line_of_credit_amt,age,sex,card_rank ";
    // setString("batch_no",kk1_batchno);
    // System.out.println(wp.sqlCmd);
    pageSelect();
    // pageQuery();

    wp.setListCount(1);
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.colSet("kk_batchno", wp.colStr("batch_no"));
    listWkdata1();
  }

  void listWkdata1() {
    int rowCt = 0;
    String wkId = "", wkIdPSeqno = "", wkCardNo = "", wkFileDate = "";
    String lsSql = "";
    wp.colSet("user_id", wp.loginUser);
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      rowCt += 1;
      wp.colSet(ii, "no", intToStr(rowCt));
      wkId = wp.colStr(ii, "id_no");
      lsSql = "select id_p_seqno from crd_idno where 1=1 ";
      lsSql += sqlCol(wkId, "id_no");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wkIdPSeqno = sqlStr("id_p_seqno");
      }
      // 卡數
      lsSql = "select count(*) as crd_cnt from crd_card where id_p_seqno=:id_p_seqno ";
      setString("id_p_seqno", wkIdPSeqno);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_crd_cnt", sqlStr("crd_cnt"));
      }
      // 消費卡數
      lsSql = "select count(*) as consume_cnt from mkt_card_consume "
          + "where card_no in (select card_no from crd_card where id_p_seqno=:id_p_seqno) ";
      setString("id_p_seqno", wkIdPSeqno);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_consume_cnt", sqlStr("consume_cnt"));
      }
      if (!sqlStr("consume_cnt").equals("0")) {
        lsSql = "select card_no from crd_card where id_p_seqno=:id_p_seqno";
        setString("id_p_seqno", wkIdPSeqno);
        sqlSelect(lsSql);
        wkCardNo = sqlStr("card_no");
      }
      // 當月有(無)手收消費金額&消費總金額,消費總筆數
      wkFileDate = wp.colStr(ii, "file_date");
      if (!empty(wkCardNo)) {
        lsSql =
            "select (consume_bl_amt + consume_ca_amt + consume_it_amt  + consume_ao_amt + consume_id_amt + consume_ot_amt "
                + "-(sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt) "
                + ") as pay_amt "
                + ", (sub_bl_amt + sub_ca_amt + sub_it_amt  + sub_ao_amt + sub_id_amt + sub_ot_amt) as sub_amt "
                + ", (consume_bl_amt + consume_ca_amt + consume_it_amt  + consume_ao_amt + consume_id_amt + consume_ot_amt) as total_amt "
                + ", (consume_bl_cnt + consume_ca_cnt + consume_it_cnt  + consume_ao_cnt + consume_id_cnt + consume_ot_cnt) as total_cnt "
                + "from mkt_card_consume " + "where card_no = :ex_card_no "
                + "and acct_month = to_char(to_date(:file_date,'yyyymmdd'),'yyyymm') ";
        setString("ex_card_no", wkCardNo);
        setString("file_date", wkFileDate);
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

  void xlsPrint1() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      // subTitle();
      kk1Batchno = itemKk("data_k1");
      if (empty(kk1Batchno)) {
        kk1Batchno = wp.itemStr("kk_batchno");
      }
      String tmpStr = "列表條件 : ";
      tmpStr += " [批號] : ";
      tmpStr += kk1Batchno;
      wp.colSet("cond_1", tmpStr);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + "list.xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      dataRead();
      // queryFunc();
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

  void pdfPrint1() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    // subTitle();
    // wp.col_set("cond_1", report_subtitle);
    kk1Batchno = itemKk("data_k1");
    if (empty(kk1Batchno)) {
      kk1Batchno = wp.itemStr("kk_batchno");
    }
    String tmpStr = "列表條件 : ";
    tmpStr += " [批號] : ";
    tmpStr += kk1Batchno;
    wp.colSet("cond_1", tmpStr);
    // ===========================
    wp.pageRows = 99999;
    dataRead();
    // queryFunc();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + "list.xlsx";
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

