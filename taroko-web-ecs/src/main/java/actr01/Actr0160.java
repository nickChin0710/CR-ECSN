/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-25  V1.00.00  Max Lin    program initial                            *
 * 109-06-18  V1.00.01  Andy       update:Mantis3641                          *
 * 111/10/24  V1.00.02  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0160 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "actr0160";

  String condWhere  = "";
  String hsWhere1   = "";
  String hsWhere1g  = "";
  String hsWhere2   = "";
  String sumWhere   = ""; //金額合計 (Linda, 20180912)
  String sumWhere1  = "";
  String sumWhere1g = "";
  String sumWhere2  = "";
  String sumSqlCmd  = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);

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
  public void initPage() {
    wp.colSet("ex_sort", "1");
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
    String exDate = wp.itemStr2("ex_date");
    String exBankId = wp.itemStr2("ex_bank_id");
    String exAmtLimit = wp.itemStr2("ex_amt_limit");
    String exSampleCnt = wp.itemStr2("ex_sample_cnt");

    //固定條件
    //String ls_where = " where 1=1  ";
    hsWhere1  = " where 1=1  ";
    hsWhere1g = " where 1=1  ";
    hsWhere2  = " where 1=1  ";

    if (empty(exDate) == false){
      hsWhere1 += " and AOA1.enter_acct_date = :ex_date ";
      setString("ex_date", exDate);
      hsWhere1g += " and enter_acct_date = :ex_date ";
      setString("ex_date", exDate);
      hsWhere2 += " and AOA2.enter_acct_date = :ex_date ";
      setString("ex_date", exDate);
    }

    if (empty(exBankId) == false){
      if (exBankId.substring(0, 3).equals("700")){
        hsWhere1 += " and AOA1.acct_bank like '700%' ";
        hsWhere1g += " and acct_bank like '700%' ";
        hsWhere2 += " and AOA2.acct_bank like '700%' ";
      }
      else {
        hsWhere1 += " and AOA1.acct_bank = :ex_bank_id ";
        setString("ex_bank_id", exBankId);
        hsWhere1g += " and acct_bank = :ex_bank_id ";
        setString("ex_bank_id", exBankId);
        hsWhere2 += " and AOA2.acct_bank = :ex_bank_id ";
        setString("ex_bank_id", exBankId);
      }
    }

    /***
     if (ex_amt_limit.equals("1")) {
     hs_where1 += " and AOAT.trans_total >= 1500000 ";
     }
     ***/

    //金額合計 (Linda, 20180912)--------------start
    //固定條件
    //sum_where  = " where 1=1 ";
    sumWhere1  = " where 1=1 ";
    sumWhere1g = " where 1=1 ";
    sumWhere2  = " where 1=1 ";
    if (empty(exDate) == false){
      sumWhere1  += sqlCol(exDate,"AOA1.enter_acct_date","=");
      sumWhere1g += sqlCol(exDate,"enter_acct_date","=");
      sumWhere2  += sqlCol(exDate,"AOA2.enter_acct_date","=");
    }

    if (empty(exBankId) == false){
      if (exBankId.substring(0, 3).equals("700")){
        sumWhere1  += "and AOA1.acct_bank like '700%' ";
        sumWhere1g += "and acct_bank like '700%' ";
        sumWhere2  += "and AOA2.acct_bank like '700%' ";
      }
      else {
        sumWhere1  += sqlCol(exBankId,"AOA1.acct_bank","=");
        sumWhere1g += sqlCol(exBankId,"acct_bank","=");
        sumWhere2  += sqlCol(exBankId,"AOA2.acct_bank","=");
      }
    }

    /***
     if (ex_amt_limit.equals("1")) {
     sum_where += " and AOAT.trans_total >= 1500000 ";
     }
     ***/
    //金額合計------------------------------------end


    //wp.whereStr = ls_where;


    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    //if (getWhereStr() == false)
    //	return;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    String exBankId = wp.itemStr2("ex_bank_id");
    String exAmtLimit = wp.itemStr2("ex_amt_limit");

    wp.pageControl();

    if (getWhereStr() == false)
      return;

    /***
     wp.sqlCmd = "select AOA.enter_acct_date, "
     + "(AOA.acct_bank || ' ' || AAB.bank_name) as acct_bank, "
     //+ "(AOA.acct_type || '-' || AA.acct_key) as acct_type_key, "
     + "(AOA.acct_type || '-' ||UF_ACNO_KEY(AOA.p_seqno)) as acct_type_key, "
     + "AOA.stmt_cycle, "
     //+ "UF_IDNO_ID(AOA.id_p_seqno) as id_no_code, "
     + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = AOA.id_p_seqno) as id_no_code, "
     + "UF_IDNO_NAME(AOA.id_p_seqno) as chi_name, "
     + "AOA.autopay_acct_no, "
     + "AOA.transaction_amt as trans_amt, "
     + "AOA.autopay_id "
     + "from act_other_apay as AOA ";

     if (ex_amt_limit.equals("1")) {
     wp.sqlCmd += "inner join ( "
     + "	select acct_bank, autopay_acct_no, sum(transaction_amt) as trans_total "
     + "	from act_other_apay "
     + "	group by acct_bank, autopay_acct_no "
     + ") as AOAT on AOA.acct_bank = AOAT.acct_bank and AOA.autopay_acct_no = AOAT.autopay_acct_no ";
     }
     ***/

    if (exAmtLimit.equals("1")) {
      wp.sqlCmd = "select AOA1.enter_acct_date, "
              //+ "(AOA1.acct_bank || ' ' || AAB.bank_name) as acct_bank, "
              //+ " (select bank_no||' '||bank_name from act_ach_bank where bank_no = AOA1.acct_bank) as acct_bank, "
              + " (select bank_no||' '||bank_name from act_ach_bank where bank_no = "
              + " decode(AOA1.acct_bank,'700','7000000',AOA1.acct_bank)) as acct_bank, "
              + "(AOA1.acct_type || '-' ||UF_ACNO_KEY(AOA1.p_seqno)) as acct_type_key, "
              + "AOA1.stmt_cycle, "
              + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = AOA1.id_p_seqno) as id_no_code, "
              + "UF_IDNO_NAME(AOA1.id_p_seqno) as chi_name, "
              + "AOA1.autopay_acct_no, "
              + "AOA1.transaction_amt as trans_amt, "
              + "AOA1.autopay_id, "
              + "RAND() as IDX  "
              + "from act_other_apay as AOA1 "
              + "inner join ( "
              + "	select acct_bank, autopay_acct_no, sum(transaction_amt) as trans_total "
              + "	from act_other_apay "
              + hsWhere1g
              + "	group by acct_bank, autopay_acct_no "
              + " having sum(transaction_amt) >= 1500000 "
              + ") as AOAT on AOA1.acct_bank = AOAT.acct_bank and AOA1.autopay_acct_no = AOAT.autopay_acct_no "
              + hsWhere1;
    }	else {
      wp.sqlCmd = "select AOA2.enter_acct_date, "
              //+ "(AOA1.acct_bank || ' ' || AAB.bank_name) as acct_bank, "
              //+ " (select bank_no||' '||bank_name from act_ach_bank where bank_no = AOA2.acct_bank) as acct_bank, "
              + " (select bank_no||' '||bank_name from act_ach_bank where bank_no = "
              + " decode(AOA2.acct_bank,'700','7000000',AOA2.acct_bank)) as acct_bank, "
              + "(AOA2.acct_type || '-' ||UF_ACNO_KEY(AOA2.p_seqno)) as acct_type_key, "
              + "AOA2.stmt_cycle, "
              + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = AOA2.id_p_seqno) as id_no_code, "
              + "UF_IDNO_NAME(AOA2.id_p_seqno) as chi_name, "
              + "AOA2.autopay_acct_no, "
              + "AOA2.transaction_amt as trans_amt, "
              + "AOA2.autopay_id, "
              + "RAND() as IDX  "
              + "from act_other_apay as AOA2 "
              + hsWhere2;
    }


    if (exAmtLimit.equals("1")) {
      sumSqlCmd = "select  "
              + "AOA1.transaction_amt as trans_amt "
              + "from act_other_apay as AOA1 "
              + "inner join ( "
              + "	select acct_bank, autopay_acct_no, sum(transaction_amt) as trans_total "
              + "	from act_other_apay "
              + sumWhere1g
              + "	group by acct_bank, autopay_acct_no "
              + " having sum(transaction_amt) >= 1500000 "
              + ") as AOAT on AOA1.acct_bank = AOAT.acct_bank and AOA1.autopay_acct_no = AOAT.autopay_acct_no "
              + sumWhere1;
    }	else {
      sumSqlCmd = "select  "
              + "AOA2.transaction_amt as trans_amt "
              + "from act_other_apay as AOA2 "
              + sumWhere2;
    }

    /***
     //金額合計where (Linda, 20180912)--------start
     sum_where = wp.sqlCmd
     + "left join act_ach_bank as AAB on AOA.acct_bank = AAB.bank_no "
     //+ "left join act_acno as AA on AOA.p_seqno = AA.p_seqno "
     //+ "left join crd_idno as CI on AOA.id_p_seqno = CI.id_p_seqno "
     + sum_where;
     //----------------------------------------end

     wp.sqlCmd +=
     "left join act_ach_bank as AAB on AOA.acct_bank = AAB.bank_no "
     //+ "left join act_acno as AA on AOA.p_seqno = AA.p_seqno "
     //+ "left join crd_idno as CI on AOA.id_p_seqno = CI.id_p_seqno "
     +wp.whereStr;
     ***/


    /*** 2021/06/10
     if (empty(wp.item_ss("ex_sample_cnt"))== true) {
     if (empty(ex_bank_id) == false && ex_bank_id.substring(0, 3).equals("009")) {
     if (ex_amt_limit.equals("1")) {
     wp.sqlCmd += " order by AOA1.transaction_amt desc ";
     } else {
     wp.sqlCmd += " order by AOA2.transaction_amt desc ";
     }
     }
     else {
     if (ex_amt_limit.equals("1")) {
     wp.sqlCmd += " order by AOA1.acct_bank, AOA1.enter_acct_date ";
     } else {
     wp.sqlCmd += " order by AOA2.acct_bank, AOA2.enter_acct_date ";
     }
     }
     }
     ***/
    //if (empty(wp.item_ss("ex_sample_cnt"))== true) {
    if (wp.itemNum("ex_sample_cnt") == 0) {
      if (wp.itemEq("ex_sort", "1")) {
        wp.sqlCmd += " order by acct_bank, transaction_amt, id_no_code ";
      }
      else if (wp.itemEq("ex_sort", "2")){
        wp.sqlCmd += " order by acct_bank, id_no_code, transaction_amt ";
      }
      else {
        wp.colSet("ex_sort", "1");
        wp.sqlCmd += " order by acct_bank, transaction_amt, id_no_code ";
      }
    }

    //[隨機抽取](Linda, 20180717)
    //if(empty(wp.item_ss("ex_sample_cnt"))== false){
    if (wp.itemNum("ex_sample_cnt") > 0) {
      wp.colSet("ex_sort", "0");
      //wp.sqlCmd +=" FETCH FIRST "+wp.item_ss("ex_sample_cnt")+" ROWS ONLY ";
      wp.sqlCmd +=" ORDER BY IDX FETCH FIRST "+wp.itemStr2("ex_sample_cnt")+" ROWS ONLY ";
      //sum_where +=" FETCH FIRST "+wp.item_ss("ex_sample_cnt")+" ROWS ONLY ";//金額合計where (Linda, 20180912)
      sumSqlCmd +=" FETCH FIRST "+wp.itemStr2("ex_sample_cnt")+" ROWS ONLY ";//金額合計where (Linda, 20180912)
    }


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
    wp.colSet("ft_cnt", wp.dataCnt);

    //金額合計 (Linda, 20180912)
    sum();

    wp.setPageValue();
    //list_wkdata();
  }

  //金額合計 (Linda, 20180912)
  void sum() throws Exception{
    //String sql1 ="select sum(trans_amt) as tot_amt from ("+ sum_where +")";
    String sql1 ="select sum(trans_amt) as tot_amt from ("+ sumSqlCmd +")";
    sqlSelect(sql1);

    wp.colSet("tot_amt", sqlStr("tot_amt"));


  }

  void listWkdata() throws Exception{
    int rowCt = 0;
    //int sum_trans_amt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      //計算欄位
      rowCt += 1;
      //sum_trans_amt += Integer.parseInt(wp.col_ss(ii, "trans_amt"));
    }

    wp.colSet("row_ct", intToStr(rowCt));
    //wp.col_set("sum_trans_amt", int_2Str(sum_trans_amt));
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // -cond-
      String exDate = wp.itemStr2("ex_date");
      String exBankId = wp.itemStr2("ex_bank_id");
      String exAmtLimit = wp.itemStr2("ex_amt_limit");
      String exSampleCnt = wp.itemStr2("ex_sample_cnt");

      String cond1 = "提出/扣款日期: " + exDate + "  銀行代號: " + exBankId;
      wp.colSet("cond_1", cond1);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      //====================================
      xlsx.sheetName[0] ="它行自動扣款抽樣檢核表";
      queryFunc();
      wp.setListCount(1);
      log("Summ: rowcnt:" + wp.listCount[1]);
      xlsx.processExcelSheet(wp);

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
    String ss = "PDFTEST: ";
    wp.colSet("cond_1", ss);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 30;
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
      // 銀行代號
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_bank_id");
      dddwList("dddw_bank_id", "act_ach_bank", "bank_no", "bank_name", "where 1=1 order by bank_no");
    } catch (Exception ex) {
    }
  }

}

