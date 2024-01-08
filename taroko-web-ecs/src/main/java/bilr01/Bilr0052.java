/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-21  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-03-14  V1.00.01  Andy       Update dddw_list merchant UI               *
* 109-04-27  V1.00.02  shiyuqi       updated for project coding standard     *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *
* 110-10-18  V1.00.04  Yang Bo    joint sql replace to parameters way        *
* 111-05-26  V1.00.05  Ryan       dddwSelect up bil_prod union bil_prod_nccc *
******************************************************************************/
/*bilr0052 PowerBuilder原程式利用呼叫DB package pkg_bilr0052.sql處理計算欄位,  *
* 本程式改寫為用JAVA處理計算欄位,												 *
* pageQuery(),pageSelect(),sqlSelect()均有使用次數限制                                                 *
******************************************************************************/
package bilr01;

import java.io.InputStream;
import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF2;

public class Bilr0052 extends BaseReport {
  InputStream inExcelFile = null;
  String mProgName = "bilr0052";

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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    } else if (eqIgno(wp.buttonCode, "ItemChange")) {
      /* TEST */
      strAction = "ItemChange";
      itemChange();
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
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // System.out.println("getWhereStr == true");
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
    // System.out.println("queryRead OK!!");
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    // if (getWhereStr() == false)
    // return;
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exMchtNo = wp.itemStr("ex_merchant");
    String exOp = wp.itemStr("ex_op");
    String exProductNo = wp.itemStr("ex_product_no");
    String exKind = wp.itemStr("ex_kind");
    if (this.chkStrend(exDateS, exDateE) == false) {
      alertErr2("[首期入帳日期-起迄]  輸入錯誤");
      return;
    }

    wp.sqlCmd = "select mcht_no, " + "mcht_chi_name, " + "product_no, " + "product_name, "
    // + "appropriation_bank, "
    // + "appropriation_account, "
        + "db_cnt, " + "db_tot_amt, " + "db_extra_fees, " + "db_redeem_amt, " + "db_redeem_point, "
        + "db_exchg_amt, " + "db_prod_fees, " + "db_refu_cnt, " + "db_refu_tot_amt, "
        + "db_refu_extra_fees, " + "db_refu_redeem_amt, " + "db_refu_redeem_point, "
        + "db_refu_exchg_amt, " + "db_refu_prod_fees, "
        + "(db_tot_amt - db_redeem_amt - db_exchg_amt + db_extra_fees) as wk_act_tot_amt, "
        + "(db_refu_tot_amt + db_refu_extra_fees - db_refu_redeem_amt - db_refu_exchg_amt) as wk_refu_act_tot_amt, "
        + "(db_cnt - db_refu_cnt) as gp1_tot_cnt, "
        + "(db_tot_amt - db_refu_tot_amt) as gp1_tot_amt, "
        + "(db_extra_fees - db_refu_extra_fees) as gp1_extra_fee, "
        + "(db_redeem_amt - db_refu_redeem_amt) as gp1_redeem_amt, "
        + "(db_redeem_point - db_refu_redeem_point) as gp1_redeem_point, "
        + "(db_exchg_amt - db_refu_exchg_amt) as gp1_exchg_amt, "
        + "((db_tot_amt - db_redeem_amt - db_exchg_amt + db_extra_fees) - (db_refu_tot_amt + db_refu_extra_fees - db_refu_redeem_amt - db_refu_exchg_amt)) as gp1_act_tot_amt, "
        + "(db_prod_fees - db_refu_prod_fees) as gp1_prod_fees " + "from ( " + "select mcht_no, "
        + "mcht_chi_name, " + "product_no, " + "product_name, " + "appropriation_bank, "
        + "appropriation_account, " + "count(*) db_cnt, " + "sum(qty * tot_amt) db_tot_amt, "
        + "sum(extra_fees) db_extra_fees, " + "sum(redeem_amt + redeem_amt1) db_redeem_amt, "
        + "sum(redeem_point + redeem_point1) db_redeem_point, " + "sum(exchange_amt) db_exchg_amt, "
        + "sum(db_prod_fees * qty) db_prod_fees, " + "sum(db_refu_cnt) db_refu_cnt, "
        + "sum(db_refu_tot_amt) db_refu_tot_amt, " + "sum(db_refu_extra_fees) db_refu_extra_fees, "
        + "sum(db_refu_redeem_amt) db_refu_redeem_amt, "
        + "sum(db_refu_redeem_point) db_refu_redeem_point, "
        + "sum(db_refu_exchg_amt) db_refu_exchg_amt, " + "sum(db_refu_prod_fees) db_refu_prod_fees "
        + "from ( " + "select first_post_date, " + "refund_apr_date, " + "contract_no, "
        + "contract_seq_no, " + "refund_apr_flag, " + "mcht_no, " + "mcht_chi_name, "
        + "product_no, " + "product_name, " + "appropriation_bank, " + "appropriation_account, "
        + "tot_amt, " + "fees_rate, " + "fees_fix_amt, " + "redeem_amt, " + "redeem_point, "
        + "qty, " + "refund_qty, " + "extra_fees, " + "exchange_amt, " + "wk_fee_amt, "
        + "redeem_amt1, " + "redeem_point1, " + "db_fees_max_amt, " + "db_fees_min_amt, "
        + "decode(contract_no,'',0,decode(contract_seq_no,0,0,redeem_amt + redeem_amt1)) as db_redeem_amt, "
        + "decode(contract_no,'',0,decode(contract_seq_no,0,0,redeem_point + redeem_point1)) as db_redeem_point, "
        + "case when db_fees_max_amt > 0 then (case when wk_prod_fee >= db_fees_max_amt then db_fees_max_amt else (case when db_fees_min_amt > 0 then (case when wk_prod_fee <= db_fees_min_amt then db_fees_min_amt else 0 end ) else 0 end) end ) else 0 end db_prod_fees, "
        + "decode(refund_apr_date,'',0,case when refund_apr_date = first_post_date then (case when qty = refund_qty then 1 else 0 end) else 0 end) db_refu_cnt, "
        + "decode(refund_apr_date,'',0,case when refund_apr_date = first_post_date then round(refund_qty*tot_amt,0) else 0 end) db_refu_tot_amt, "
        + "decode(refund_apr_date,'',0,case when refund_apr_date = first_post_date then (case when qty = refund_qty then extra_fees else 0 end) else 0 end) db_refu_extra_fees, "
        + "decode(refund_apr_date,'',0,case when refund_apr_date != first_post_date then 0 else db_refu_redeem_amt end) db_refu_redeem_amt, "
        + "decode(refund_apr_date,'',0,case when refund_apr_date != first_post_date then 0 else db_refu_redeem_point end) db_refu_redeem_point, "
        + "decode(refund_apr_date,'',0,case when refund_apr_date = first_post_date then (case when qty = refund_qty then exchange_amt else 0 end) else 0 end) db_refu_exchg_amt, "
        + "case when db_fees_max_amt > 0 then (case when wk_prod_fee >= db_fees_max_amt then db_fees_max_amt else (case when db_fees_min_amt > 0 then (case when wk_prod_fee <= db_fees_min_amt then db_fees_min_amt else 0 end ) else 0 end) end ) else 0 end db_refu_prod_fees "
        + "from ( " + "select a.first_post_date, " + "a.refund_apr_date, " + "a.contract_no, "
        + "a.contract_seq_no, "
        + "decode(a.refund_apr_flag,'','N',a.refund_apr_flag) refund_apr_flag, " + "a.mcht_no, "
        + "substr(a.mcht_chi_name,1,10) mcht_chi_name, " + "(a.mcht_no||' '||a.mcht_chi_name) mcht_no_name, "
        + "a.product_no, " + "a.product_name, "
        // + "a.appropriation_bank, "
        // + "a.appropriation_account, " //無資料改抓bil_merchant他行匯款行庫及帳號
        + "nvl(d.oth_bank_name,'') as appropriation_bank, "
        + "decode(nvl(d.oth_bank_id,''),'','',(d.oth_bank_id||'_'||d.oth_bank_acct)) as appropriation_account, "
        + "a.tot_amt, " + "a.fees_rate, " + "a.fees_fix_amt, " + "a.redeem_amt, "
        + "a.redeem_point, " + "a.qty, " + "a.refund_qty, " + "a.extra_fees, " + "a.exchange_amt, "
        + "case when a.tot_amt > 0 then (case when a.fees_rate > 0 then round((a.tot_amt * a.fees_rate / 100) + a.fees_fix_amt,0) else 0 end) else 0 end as wk_prod_fee, "
        + "case when a.tot_amt > 0 then (case when a.qty > 0 then (case when a.fees_rate > 0 then round(((a.tot_amt * a.qty) * a.fees_rate / 100) + a.fees_fix_amt,0) else 0 end) else 0 end) else 0 end as wk_fee_amt, "
        + "nvl((select sum(b.redeem_amt) from bil_back_log as b where b.contract_no = a.contract_no and b.contract_seq_no = a.contract_seq_no ),0) as redeem_amt1, "
        + "nvl((select sum(b.redeem_amt) from bil_back_log as b where b.contract_no = a.contract_no and b.contract_seq_no = a.contract_seq_no  ),0) as db_refu_redeem_amt, "
        + "nvl((select sum(b.redeem_point) from bil_back_log as b where b.contract_no = a.contract_no and b.contract_seq_no = a.contract_seq_no  ),0) as redeem_point1, "
        + "nvl((select sum(b.redeem_point) from bil_back_log as b where b.contract_no = a.contract_no and b.contract_seq_no = a.contract_seq_no),0) as db_refu_redeem_point, "
        + "nvl((select fees_max_amt from bil_prod as c where c.mcht_no = a.mcht_no and c.product_no = a.product_no  ),0) as db_fees_max_amt, "
        + "nvl((select fees_min_amt from bil_prod as c where c.mcht_no = a.mcht_no and c.product_no = a.product_no  ),0) as db_fees_min_amt "
        + "from bil_contract as a " + "left join bil_merchant d on a.mcht_no = d.mcht_no "
        + "where 1=1 ";
    wp.sqlCmd += sqlStrend(exDateS, exDateE, "a.first_post_date");
    wp.sqlCmd += sqlCol(exMchtNo, "a.mcht_no");
    wp.sqlCmd += sqlCol(exProductNo, "a.product_no");
    switch (exOp) {
      case "1":
        wp.sqlCmd += " and a.cps_flag = 'Y' ";
        break;
      case "2":
        wp.sqlCmd += " and a.cps_flag = 'C' ";
        break;
      case "3":
        wp.sqlCmd += " and a.cps_flag = 'N' ";
        break;
    }

    switch (exKind) {
      case "1":
        wp.sqlCmd += " and a.contract_kind = '1' ";
        break;
      case "2":
        wp.sqlCmd += " and a.contract_kind = '1' and a.redeem_kind = '2' ";
        break;
      case "3":
        wp.sqlCmd += " and a.contract_kind = '1' and a.redeem_kind = '1' ";
        break;
      case "4":
        wp.sqlCmd += " and a.contract_kind = '1' and a.redeem_kind = '0' ";
        break;
      case "5":
        wp.sqlCmd += " and a.contract_kind = '2'";
        break;
      case "6":
        wp.sqlCmd += " and a.contract_kind = '2' and a.redeem_kind = '2' ";
        break;
      case "7":
        wp.sqlCmd += " and a.contract_kind = '2' and a.redeem_kind = '1' ";
        break;
      case "8":
        wp.sqlCmd += " and a.contract_kind = '2' and a.redeem_kind = '0' ";
        break;
    }

    wp.sqlCmd += ") " + ") " + "where 1=1 "
        + "group by mcht_no, mcht_chi_name, product_no, product_name,appropriation_bank, appropriation_account "
        + ") ";
    // System.out.println(wp.sqlCmd);
    wp.pageCountSql = "select count(*) from ( " + wp.sqlCmd + " )";

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
    // wp.col_set("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    wp.colSet("user_id", wp.loginUser);
    // list_wkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;

    rowCt = wp.selectCnt;
    wp.colSet("ft_cnt", intToStr(rowCt));

  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String mchtNo = wp.itemStr("ex_mcht_no");
    String op = wp.itemStr("ex_op");
    String exKind = wp.itemStr("ex_kind");
    String title = "";

    if (empty(exDateS) == false || empty(exDateE) == false) {
      title += "首期入帳日期 : ";
      if (empty(exDateS) == false) {
        title += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        title += " ~ " + exDateE + " 迄 ";
      }
      title += "  ";
    }
    if (empty(mchtNo) == false) {
      title += "特店代號 : " + mchtNo;
      title += "  ";
    }
    if (empty(op) == false) {
      title += "選項 : ";
      title += commString.decode(op, ",1,4,2,3,", ",ONUS,NCCC,人工,全部");
      title += "  ";
    }
    if (empty(exKind) == false) {
      title += "類別 : ";
      title += commString.decode(exKind, ",1,2,3,4,5,6,7,8,9",
          ",分期-全部,分期-折抵,分期-全折,分期-無折,郵購-全部,郵購-折抵,郵購-全折,郵購-無折,全部");
    }
    reportSubtitle = title;
  }

  void xlsPrint() {
    // System.out.println("xlsPrint in ");
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String exDateS = wp.itemStr("exDateS");
      String exDateE = wp.itemStr("exDateE");
      String mchtNo = wp.itemStr("ex_mcht_no");
      String op = wp.itemStr("ex_op");
      String exProductNo = wp.itemStr("ex_product_no");
      String exKind = wp.itemStr("ex_kind");
      // String std_vouch_cd_f = wp.item_ss("ex_std_vouch_cd_f");
      // String std_vouch_cd_u = wp.item_ss("ex_std_vouch_cd_u");
      // String curr = wp.item_ss("ex_curr");
      String cond1 = "首期入帳日期： " + exDateS + "~" + exDateE + " 特店代號 :" + mchtNo + "選項:" + op + "商品代號"
          + exProductNo + "類別:" + exKind;
      wp.colSet("cond_1", cond1);
      // System.out.println("ss "+ss);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
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
      System.out.println("ex:" + ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF2 pdf = new TarokoPDF2();
    // 表頭固定欄位
    pdf.fixHeader[0] = "user_id";
    pdf.fixHeader[1] = "cond_1";

    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);
    pdf = null;
    System.out.println("EES");

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_mcht_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_mcht_no");
      // dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no",
      // "mcht_chi_name", "where 1=1 and loan_flag = 'N' order by
      // mcht_no");

      String exMchtNo = wp.itemStr("ex_merchant");
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_product_no");
      if(!empty(exMchtNo)) {
			String sqlStr = "select product_no,product_no||'_'||product_name as product_name from bil_prod_nccc where mcht_no = :mcht_no1 "
					+ " union select product_no,product_no||'_'||product_name as product_name from bil_prod where mcht_no = :mcht_no2 "
					+ " order by product_no ";
			setString("mcht_no1", exMchtNo);
			setString("mcht_no2", exMchtNo);
			sqlSelect(sqlStr);
			wp.colSet("dddw_product_no", this.dddwOption("product_no", "product_name"));
      }
      // 為下面dddwList方法傳參數
//      setString("mcht_no", exMchtNo);
//      this.dddwList("dddw_product_no", "bil_prod", "product_no", "product_name",
//          "where 1=1 and mcht_no = :mcht_no order by product_no");
    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }

  void itemChange() throws Exception {

  }
}
