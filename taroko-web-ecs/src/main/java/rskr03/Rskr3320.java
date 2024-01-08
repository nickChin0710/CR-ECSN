package rskr03;
/**
 * 2020-0108:  Alex  order by fix
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import ofcapp.InfaceExcel;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;

public class Rskr3320 extends BaseQuery implements InfaceExcel {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // strAction="new";
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
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
      xlsPrint();
   }
   dddwSelect();
   initButton();

}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_case_date1"))
         && empty(wp.itemStr("ex_case_date2"))
         && empty(wp.itemStr("ex_debit_date1"))
         && empty(wp.itemStr("ex_debit_date2"))) {
      alertErr("立案/帳卡登錄日期 不可全部空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr("立案日期起迄：輸入錯誤");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_debit_date1"), wp.itemStr("ex_debit_date2")) == false) {
      alertErr("帳卡登錄日期起迄：輸入錯誤");
      return;
   }


   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_debit_date1"), "debit_card_date", ">=")
         + sqlCol(wp.itemStr("ex_debit_date2"), "debit_card_date", "<=")
         + sqlCol(wp.itemStr("ex_case_date1"), "case_date", ">=")
         + sqlCol(wp.itemStr("ex_case_date2"), "case_date", "<=")
         + sqlCol(wp.itemStr("ex_case_type"), "case_type");
   if (wp.itemEq("ex_close_flag", "1")) {
      lsWhere += " and debit_card_date <> '' ";
   }
   else if (wp.itemEq("ex_close_flag", "2")) {
      lsWhere += " and debit_card_date = '' ";
   }

   if (wp.itemEq("ex_case_source_99", "Y")) {
      lsWhere += " and case_source <> '法院起訴或判決' ";
   }

   if (wp.itemEq("ex_turn_miscell", "1")) {
      lsWhere += " and miscell_unpay_amt > 0  ";
   }
   else if (wp.itemEq("ex_turn_miscell", "2")) {
      lsWhere += " and miscell_unpay_amt = 0  ";
   }
   
   setSqlParmNoClear(true);
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
         + " id_p_seqno , "
         + " case_type, "
         + " survey_user,  "
         + " debit_card_date ,  "
         + " case_date ,"
         + " case_seqno,"
         + " card_no ,  "
         + " fraud_ok_amt,  "
         + " actual_amt,  "
         + " for_ch_amt,"
         + " ch_deuct_amt,"
         + " for_fraud_amt,"
         + " for_mcht_amt,"
         + " cb_ok_amt,"
         + " non_disput_amt,"
         + " unbill_amt,"
         + " turn_miscell_amt,"
         + " adj_limit,"
         // + "otb_amt,"
         + " fraud_ok_cnt,"
         + " recov_cash_amt,"
         + " recov_unpay_amt,"
         + " fraud_unpay_amt,"
         + " oth_unpay_amt,"
         + " miscell_unpay_amt,"
         + " turn_miscell_amt2,"
         + " turn_miscell_cnt2,"
         + " fraud_area,"
         + " uf_idno_name2(card_no,'') as db_chi_name , "
         + " for_ch_amt + ch_deuct_amt + for_fraud_amt + for_mcht_amt + cb_ok_amt + non_disput_amt + unbill_amt as rows_amt "
   ;
   wp.daoTable = "rsk_ctfi_case";

   if (wp.itemEq("ex_report", "1")) {
      wp.whereOrder = " order by case_type , case_seqno Asc  ";
   }
   else if (wp.itemEq("ex_report", "2")) {
      wp.whereOrder = " order by survey_user , case_seqno Asc ";
   }
   else {
      wp.whereOrder = " order by case_seqno Asc ";
   }

   pageQuery();
   // list_wkdata();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();
   queryAfter();
}

void queryAfter() throws Exception  {

   int ilSelectCnt = 0;
   ilSelectCnt = wp.selectCnt;

   String sql1 = " select d6_amt , otb_amt from rsk_ctfi_warn where card_no = ? ";

   for (int ii = 0; ii < ilSelectCnt; ii++) {
      //--卡片額度 , OTB金額
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "card_no")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "adj_limit", sqlStr("d6_amt"));
         wp.colSet(ii, "otb_amt", sqlStr("otb_amt"));
      }
   }

}

void sumRead(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " sum(fraud_ok_amt) as sum_fraud_ok_amt,"
         + " sum(actual_amt) as sum_actual_amt,"
         + " sum(for_ch_amt) as sum_for_ch_amt,"
         + " sum(ch_deuct_amt) as sum_ch_deuct_amt,"
         + " sum(for_fraud_amt) as sum_for_fraud_amt,"
         + " sum(for_mcht_amt) as sum_for_mcht_amt,"
         + " sum(cb_ok_amt) as sum_cb_ok_amt,"
         + " sum(non_disput_amt) as sum_non_disput_amt,"
         + " sum(unbill_amt) as sum_unbill_amt,"
         + " sum(turn_miscell_cnt2) as sum_turn_miscell_cnt2,"
         + " sum(fraud_ok_cnt) as sum_fraud_ok_cnt,"
         + " sum(recov_cash_amt) as sum_recov_cash_amt,"
         + " sum(recov_unpay_amt) as sum_recov_unpay_amt,"
         + " sum(fraud_unpay_amt) as sum_fraud_unpay_amt,"
         + " sum(oth_unpay_amt) as sum_oth_unpay_amt,"
         + " sum(miscell_unpay_amt) as sum_miscell_unpay_amt,"
         + " sum(for_ch_amt + ch_deuct_amt + for_fraud_amt + for_mcht_amt + cb_ok_amt + non_disput_amt + unbill_amt) as sum_row_amt";
   wp.daoTable = "rsk_ctfi_case";
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

@Override
public void dddwSelect() {
   try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_case_type");
      dddwList("dddw_case_type", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type='CTFI_CASE_TYPE'");
   }
   catch (Exception ex) {
   }
}

@Override
public void xlsPrint() throws Exception {
   try {
      log("xlsFunction: started--------");
      wp.reportId = "Rskr3320";
//		String ss = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1"))
//		  + " -- " + commString.strToYmd(wp.itemStr("ex_date2"));
//		wp.colSet("cond1", ss);
      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "rskr3320.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      wp.setListCount(1);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
   }
   catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
   }

}

@Override
public void logOnlineApprove() throws Exception {
   // TODO Auto-generated method stub

}
}
