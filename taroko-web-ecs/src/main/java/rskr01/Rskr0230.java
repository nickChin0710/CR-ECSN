package rskr01;
/**
 * 2020-0907   JH    改為=扣款覆核日
 * 2020-0803   JH    modify, fst_apr_date>=cond
 * 2020-0507   JH    bug
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskr0230 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "R":
         dataRead(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "L":
         strAction = "";
         clearFunc(); break;
      case "PDF":
         pdfPrint(); break;
   }

   dddwSelect();
   initButton();

}

@Override
public void queryFunc() throws Exception {

   String lsWhere =
         " where chg_stage in ('1','3') and sub_stage='30' and fst_disb_yn <>'Y'  "
               +sqlCol(wp.itemStr("ex_date1"), "fst_apr_date")
         +sqlCol(wp.itemStr("ex_user"),"fst_add_user")
         ;

   String lsExprDate =wp.itemStr("ex_expr_date");
   if (notEmpty(lsExprDate)) {
      lsWhere +=commSqlStr.strend("19000101",lsExprDate,"fst_expire_date");
   }
   if (wp.itemEq("ex_dbcard_flag", "Y")) {
      lsWhere += " and debit_flag='Y' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "N")) {
      lsWhere += " and debit_flag <>'Y' ";
   }
   
   setSqlParmNoClear(true);
   sumFstAmt(lsWhere);

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
         + " ctrl_seqno , "
         + " uf_idno_name2(card_no,debit_flag) as db_idno_name, "
         + " purchase_date , "
         + " fst_twd_amt,"
         + " txn_code,"
         + " chg_times,"
         + " fst_add_date,"
         + " acct_type,"
         + " uf_acno_key2(card_no,acct_type) as acct_key,"
         + " dest_curr,"
         + " source_amt,"
         + " source_curr,"
         + " fst_reason_code,"
         + " mcht_no, fst_add_user as add_user,"
         + " decode(acct_type,'',0,1) as db_cnt, "
         + " decode(acct_type,'',0,fst_twd_amt) as db_fst_twd_amt, "
         + " '' as xxx"
   ;
   wp.daoTable = "Vrsk_chgback";

   wp.whereOrder = " order by acct_type , purchase_date  ";
   pageQuery();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      set0();
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();
}

void set0() {
   wp.colSet("sum_count", "0");
   wp.colSet("sumFstAmt", "0");
}

void sumFstAmt(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as sum_count,"
         + " sum(fst_twd_amt) as sum_fst_amt";
   wp.daoTable = "Vrsk_chgback";
   wp.whereStr = lsWhere;
   pageSelect();
}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

private void hideData() {
   int ll_nrow = wp.listCount[0];
   for (int ii = 0; ii < ll_nrow; ii++) {
      wp.colSet(ii, "wk_acct_key", commString.hideIdno(wp.colStr(ii, "acct_key")));
      wp.colSet(ii, "wk_card_no", commString.hideCardNo(wp.colStr(ii, "card_no")));
      wp.colSet(ii, "hh_chi_name", commString.hideIdnoName(wp.colStr(ii, "db_idno_name")));
   }
}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskr0230";
   String ss, tt;
   if (wp.itemEq("ex_dbcard_flag", "Y")) {
      tt = " Debit Card 扣款逾期未撥款通知報表 ";
   }
   else {
      tt = " 扣款逾期未撥款通知報表 ";
   }
   ss = "扣款覆核日期 : " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
         + commString.strToYmd(wp.itemStr("ex_date2"));
   wp.colSet("cond1", ss);
   wp.colSet("title", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();

   hideData();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0230.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
