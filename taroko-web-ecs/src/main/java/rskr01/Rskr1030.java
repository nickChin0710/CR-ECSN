package rskr01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr1030 extends BaseAction implements InfacePdf {
taroko.base.CommDate zzdate = new taroko.base.CommDate();

@Override
public void userAction() throws Exception {
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
//			xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
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

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.sqlCmd = " select "
         + " uf_nvl(B.id_code2,'xxx') as id_code2 , "
         + " B.id_desc2 , "
         + " clo_result , "
         + " B.wf_desc as tt_clo_result , "
         + " count(*) as db_cnt ,"
         + " sum(mcht_repay) as db_amt ,"
         + " sum(decode(debit_flag,'Y',0,decode(card_ind,'1',1,0))) as db_cnt1 ,"
         + " sum(decode(debit_flag,'Y',0,decode(card_ind,'1',mcht_repay,0))) as db_amt1 ,"
         + " sum(decode(debit_flag,'Y',0,decode(card_ind,'2',1,0))) as db_cnt2 ,"
         + " sum(decode(debit_flag,'Y',0,decode(card_ind,'2',mcht_repay,0))) as db_amt2 ,"
         + " sum(decode(debit_flag,'Y',1,0)) as db_cnt3 ,"
         + " sum(decode(debit_flag,'Y',mcht_repay,0)) as db_amt3 "
         + " from (select clo_result, uf_card_indicator(acct_type) as card_ind, mcht_repay, debit_flag "
         + " from rsk_problem "
         + " where 1=1 and close_apr_date <>'' "
         + sqlCol(wp.itemStr("ex_apr_date1"), "close_apr_date", ">=")
         + sqlCol(wp.itemStr("ex_apr_date2"), "close_apr_date", "<=")
         + " union "
         + " select clo_result_2 as clo_result, uf_card_indicator(acct_type) as card_ind, mcht_repay_2 as mcht_repay, debit_flag "
         + " from rsk_problem "
         + " where 1=1 and close_apr_date_2 <>'' "
         + sqlCol(wp.itemStr("ex_apr_date1"), "close_apr_date_2", ">=")
         + sqlCol(wp.itemStr("ex_apr_date2"), "close_apr_date_2", "<=")
         + " ) A left join ptr_sys_idtab B "
         + " on A.clo_result =B.wf_id and (B.wf_type like 'PRBQ-CLO-RESULT' or B.wf_type like 'DBPQ-CLO-RESULT') "
         + " where 1=1 group by  B.id_code2 , B.id_desc2, A.clo_result, B.wf_desc order by 1 "
   ;
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr("");
      wp.colSet("ll_cnt", "" + 0);
      wp.colSet("ll_amt", "" + 0);
      wp.colSet("ll_cnt1", "" + 0);
      wp.colSet("ll_amt1", "" + 0);
      wp.colSet("ll_cnt2", "" + 0);
      wp.colSet("ll_amt2", "" + 0);
      wp.colSet("ll_cnt3", "" + 0);
      wp.colSet("ll_amt3", "" + 0);
      alertErr("此條件查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(1);
//		wp.setPageValue();

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
   wp.reportId = "Rskr1030";
   wp.pageRows = 9999;
   String tt;
   tt = "覆核日期:" + zzdate.dspDate(wp.itemStr("ex_apr_date1")) + " -- " + zzdate.dspDate(wp.itemStr("ex_apr_date2"));
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr1030.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
