package rskr01;

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDFLine;

public class Rskr0020 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   //ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      //				is_action="new";
      //				clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) { //-資料讀取-
      strAction = "R";
      //         dataRead();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page*/
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
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
      //			xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
   }

   dddwSelect();
   initButton();

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("新增登錄日期起迄：輸入錯誤");
      return;
   }


   String lsWhere = " where 1=1 and prb_mark ='Q' "
         + sqlCol(wp.itemStr("ex_date1"), "add_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "add_date", "<=")
         + sqlCol(wp.itemStr("ex_id1"), "add_user", "like%");

   if (wp.itemEq("ex_apr_flag", "Y")) {
      lsWhere += " and add_apr_date <> ''";
   }
   else if (wp.itemEq("ex_apr_flag", "N")) {
      lsWhere += " and add_apr_date = ''";
   }

   if (wp.itemEq("ex_dbcard_flag", "1")) {
      lsWhere += " and debit_flag <> 'Y' and bill_type <>'TSCC' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "2")) {
      lsWhere += " and debit_flag = 'Y' and bill_type <>'TSCC' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "3")) {
      lsWhere += " and bill_type ='TSCC' ";
   }

   if (wp.itemEq("ex_prb_src_code", "AL")) {
      lsWhere += " and prb_src_code in ('RQ','SQ','SS','SE','CQ') ";
   }
   else if (wp.itemEq("ex_prb_src_code", "RQ")) {
      lsWhere += " and prb_src_code = 'RQ' ";
   }
   else if (wp.itemEq("ex_prb_src_code", "CQ")) {
      lsWhere += " and prb_src_code = 'CQ' ";
   }
   
   this.setSqlParmNoClear(true);
   sumRead(lsWhere);

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
         + " uf_hi_cardno(card_no) as hh_card_no , "
         + " replace(uf_idno_name(card_no),'　','') as db_idno_name , "
         + " uf_hi_cname(replace(uf_idno_name(card_no),'　','')) as hh_name , "
         + " ctrl_seqno , "
         + " purchase_date , "
         + " prb_amount,"
         + " prb_reason_code,"
         + " add_date,"
         + " add_user,"
         + " uf_dc_curr(curr_code) as curr_code , "
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt , "
         + " prb_src_code,"
         + " bill_type,"
         + " uf_dc_amt(curr_code,prb_amount,dc_prb_amount) as dc_prb_amount , "
         + " add_apr_date "
   ;
   wp.daoTable = "rsk_problem";
   //wp.whereOrder = " order by card_no , add_date ASC ";
   wp.whereOrder = " order by add_user , bin_type, ctrl_seqno ";
   pageQuery();
   wkNum();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();
}

void wkNum() throws Exception {
   double liNum = 0;
   boolean lbPdf = eqIgno(wp.buttonCode, "PDF");
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      liNum = wp.colNum(ii, "dc_dest_amt") - wp.colNum(ii, "dc_prb_amount");
      wp.colSet(ii, "wk_diff_amt", "" + liNum);
      if(lbPdf) {
      	wp.colSet(ii, "card_no",wp.colStr(ii,"hh_card_no"));
      	wp.colSet(ii, "hh_name",commString.mid(wp.colStr(ii,"hh_name"), 0,10));
      }
   }
}


void sumRead(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as wk_A,"
         + " sum(uf_dc_amt2(dest_amt,dc_dest_amt)) as wk_Ad,"
         + " sum(uf_dc_amt(curr_code,prb_amount,dc_prb_amount)) as wk_Ap,"
         + " sum(uf_dc_amt2(dest_amt,dc_dest_amt)) - sum(uf_dc_amt(curr_code,prb_amount,dc_prb_amount)) as wk_Adp"
   ;
   wp.daoTable = "rsk_problem";
   wp.whereStr = lsWhere;
   pageSelect();

   if (wp.colNum(0, "wk_A") == 0) {
      wp.colSet("wk_Ad", "" + 0);
      wp.colSet("wk_Ap", "" + 0);
      wp.colSet("wk_Adp", "" + 0);

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
   wp.reportId = "rskr0020";
   String ss = "";

   if (wp.itemEq("ex_dbcard_flag", "0")) {
      ss = "交易卡別 : 全部   ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "1")) {
      ss = "交易卡別 : 信用卡   ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "2")) {
      ss = "交易卡別 : VD 卡   ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "3")) {
      ss = "交易卡別 : 悠遊卡   ";
   }

   ss += "新增登錄日期 : " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_date2")) + " " + "新增登錄者: " + wp.itemStr("ex_id1");
   if (wp.itemEq("ex_apr_flag", "Y")) {
      ss += "  已覆核";
   }
   else if (wp.itemEq("ex_apr_flag", "N")) {
      ss += "  待覆核";
   }
   wp.colSet("cond1", ss);
   wp.pageRows = 9999;
   queryFunc();
   
   if(sqlNotFind()) {
	   wp.respHtml = this.errPagePDF;
	   return ;
   }
   
   TarokoPDFLine pdf = new TarokoPDFLine();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0020.xlsx";
   //pdf.pageCount = 35;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
