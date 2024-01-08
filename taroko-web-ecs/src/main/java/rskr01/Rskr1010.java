package rskr01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr1010 extends BaseAction implements InfacePdf {
taroko.base.CommDate zzdate = new taroko.base.CommDate();

@Override
public void userAction() throws Exception {
   defaultAction();

   switch (wp.buttonCode) {
      case "XLS":
      strAction = "XLS"; break;
      case "PDF":
      pdfPrint(); break;
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_apr_date1"), wp.itemStr("ex_apr_date2")) == false) {
      alertErr("覆核日期:起迄錯誤");
      return;
   }

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_apr_date1"), "add_apr_date", ">=")
         + sqlCol(wp.itemStr("ex_apr_date2"), "add_apr_date", "<=");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();


}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
         + " prb_reason_code, "
         + " uf_tt_idtab('PRBL-REASON-CODE',prb_reason_code) as tt_reason_code , "
         + " count(*) as db_cnt , "
         + " sum(prb_amount) as db_amt , "
         + " sum(decode(debit_flag,'Y',0,decode(B.card_indicator,'1',1,0))) as db_cnt1 , "
         + " sum(decode(debit_flag,'Y',0,decode(B.card_indicator,'1',prb_amount,0))) as db_amt1 , "
         + " sum(decode(debit_flag,'Y',0,decode(B.card_indicator,'2',1,0))) as db_cnt2 , "
         + " sum(decode(debit_flag,'Y',0,decode(B.card_indicator,'2',prb_amount,0))) as db_amt2 , "
         + " sum(decode(debit_flag,'Y',1,0)) as db_cnt3 , "
         + " sum(decode(debit_flag,'Y',prb_amount,0)) as db_amt3 ";
   wp.daoTable = " rsk_problem A left join ptr_acct_type B on A.acct_type =B.acct_type ";
   wp.whereOrder = " group by prb_reason_code,2 order by 1";
   wp.pageCountSql = "select count(*) from (select distinct prb_reason_code , uf_tt_idtab('PRBL_REASON_CODE',prb_reason_code) as tt_reason_code from rsk_problem A left join ptr_acct_type B on A.acct_type =B.acct_type " + wp.whereStr + ")";
   pageQuery();
   if (sqlRowNum <= 0) {
      wp.colSet("llCnt", "" + 0);
      wp.colSet("llAmt", "" + 0);
      wp.colSet("llCnt1", "" + 0);
      wp.colSet("llAmt1", "" + 0);
      wp.colSet("llCnt2", "" + 0);
      wp.colSet("llAmt2", "" + 0);
      wp.colSet("llCnt3", "" + 0);
      wp.colSet("llAmt3", "" + 0);
      alertErr("此條件查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(1);
   wp.setPageValue();
}

void queryAfter() {
   double llCnt = 0, llAmt = 0, llCnt1 = 0, llAmt1 = 0, llCnt2 = 0, llAmt2 = 0, llCnt3 = 0, llAmt3 = 0;
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      llCnt += wp.colNum(ii, "db_cnt");
      llAmt += wp.colNum(ii, "db_amt");
      llCnt1 += wp.colNum(ii, "db_cnt1");
      llAmt1 += wp.colNum(ii, "db_amt1");
      llCnt2 += wp.colNum(ii, "db_cnt2");
      llAmt2 += wp.colNum(ii, "db_amt2");
      llCnt3 += wp.colNum(ii, "db_cnt3");
      llAmt3 += wp.colNum(ii, "db_amt3");
   }
   wp.colSet("ll_cnt", "" + llCnt);
   wp.colSet("ll_amt", "" + llAmt);
   wp.colSet("ll_cnt1", "" + llCnt1);
   wp.colSet("ll_amt1", "" + llAmt1);
   wp.colSet("ll_cnt2", "" + llCnt2);
   wp.colSet("ll_amt2", "" + llAmt2);
   wp.colSet("ll_cnt3", "" + llCnt3);
   wp.colSet("ll_amt3", "" + llAmt3);
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
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "Rskr1010";
   wp.pageRows = 9999;
   String tt;
   tt = "覆核日期:" + zzdate.dspDate(wp.itemStr("ex_apr_date1")) + " -- " + zzdate.dspDate(wp.itemStr("ex_apr_date2"));
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr1010.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
