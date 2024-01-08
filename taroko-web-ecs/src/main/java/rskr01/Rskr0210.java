package rskr01;

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Rskr0210 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskr0210")) {
         wp.optionKey = wp.colStr("ex_curr_code");
         dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("新增登錄日期起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where chg_stage in ('1','3')   "
         + sqlCol(wp.itemStr("ex_date1"), "fst_add_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "fst_add_date", "<=")
         + sqlCol(wp.itemStr("ex_id1"), "fst_add_user")
         + sqlCol(wp.itemStr("ex_curr_code"), "uf_nvl(curr_code,'901')");
   if (wp.itemEq("ex_apr_flag", "Y")) {
      if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2")) {
         alertErr("新增登錄日期不可空白");
         return;
      }
      lsWhere += " and fst_apr_date <>'' and sub_stage = '30'";
   }
   else {
      lsWhere += " and fst_apr_date = '' and sub_stage = '10'";
   }

   if (wp.itemEq("ex_dbcard_flag", "1")) {
      lsWhere += " and debit_flag <> 'Y' and bill_type <> 'TSCC' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "2")) {
      lsWhere += " and debit_flag = 'Y' and bill_type <> 'TSCC' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "3")) {
      lsWhere += " and bill_type = 'TSCC' ";
   }
   
   setSqlParmNoClear(true);
   list_sum(lsWhere);

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {

   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " purchase_date , "
         + " ctrl_seqno , "
         + " chg_times , "
         + " fst_doc_mark,"
         + " fst_amount,"
         + " fst_msg,"
         + " fst_add_date,"
         + " fst_add_user,"
         + " bin_type,"
         + " source_amt,"
         + " source_curr,"
         + " acct_type,"
         + " fst_reason_code,"
         + " bill_type,"
         + " uf_dc_curr(curr_code) as curr_code , "
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt , "
         + " uf_dc_amt2(fst_twd_amt,fst_dc_amt) as fst_twd_amt , "
//			+ " uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_twd_amt,"
//			+ " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dest_amt,"
         + " v_card_no,"
         + " decode(bill_type,'TSCC',1,0) as db_tscc_cnt, "
         + " decode(bill_type,'TSCC',0,1) as db_card_cnt,"
         + " decode(bill_type,'TSCC',dest_amt,0) as db_tscc_destamt, "
         + " decode(bill_type,'TSCC',0,dest_amt) as db_card_destamt,"
         + " decode(bill_type,'TSCC',fst_twd_amt,0) as db_tscc_fsttwdamt, "
         + " decode(bill_type,'TSCC',0,fst_twd_amt) as db_card_fsttwdamt,"
         + " decode(bill_type,'TSCC',source_amt,0) as db_tscc_sourceamt, "
         + " decode(bill_type,'TSCC',0,source_amt) as db_card_sourceamt,"
         + "uf_hi_cardno(substr(card_no,1,16)) as wk_card_no";
   wp.daoTable = "rsk_chgback";
   //wp.whereOrder = " order by card_no, fst_add_date  ";
   wp.whereOrder = " order by fst_add_user, fst_add_date, bin_type, ctrl_seqno  ";
   pageQuery();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      list_sum("");
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();
}


void list_sum(String lsWhere) throws Exception {
   wp.colSet("wk_A", 0);
   wp.colSet("wk_A1", 0);
   wp.colSet("wk_A2", 0);
   wp.colSet("wk_A3", 0);
   if (empty(lsWhere)) {
      return;
   }
   String sql1 = "select"
         + " count(*) as wk_A,"
         + " sum(uf_dc_amt2(dest_amt,dc_dest_amt)) as wk_A1,"
         + " sum(uf_dc_amt2(fst_twd_amt,fst_dc_amt)) as wk_A2,"
         + " sum(source_amt) as wk_A3" +
         " from rsk_chgback"
         + lsWhere;
   sqlSelect(sql1);
   if (sqlRowNum <= 0)
      return;

   sql2wp("wk_A");
   sql2wp("wk_A1");
   sql2wp("wk_A2");
   sql2wp("wk_A3");
}

void wk_add_date_user() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_add_date_user", commString.strToYmd(wp.colStr(ii, "fst_add_date"))
            + " " + wp.colStr(ii, "fst_add_user"));
   }
}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskr0210";
   String ss;
   ss = "  新增登錄日期 : "
         + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
         + commString.strToYmd(wp.itemStr("ex_date2"))
         + " 新增登錄者: " + wp.itemStr("ex_id1");
   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();
   wk_add_date_user();
   for (int ll=0; ll<wp.listCount[0]; ll++) {
      wp.colSet(ll,"card_no",commString.hideCardNo(wp.colStr(ll,"card_no")));
   }

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0210.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
