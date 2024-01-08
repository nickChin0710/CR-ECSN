/**
 * 2020-0109  V1.00.01  Alex  card_type chinese
 */
package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3360 extends BaseQuery {
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
   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("3360") > 0) {
         wp.optionKey = wp.colStr("ex_card_type");
         dddwList("dddw_card_type", "ptr_card_type"
               , "card_type", "name", "where 1=1");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (eqIgno(wp.respHtml, "rskr3360")) {
         ddlbList("dddw_bin_type", wp.colStr("ex_bin_type"), "ecsfunc.deCode_ptr.bin_type");
      }
   }
   catch (Exception ex) {
   }

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


   String lsWhere =
         " where 1=1 "
               + sqlCol(wp.itemStr("ex_debit_date1"), "debit_card_date", ">=")
               + sqlCol(wp.itemStr("ex_debit_date2"), "debit_card_date", "<=")
               + sqlCol(wp.itemStr("ex_case_date1"), "case_date", ">=")
               + sqlCol(wp.itemStr("ex_case_date2"), "case_date", "<=")
               + sqlCol(wp.itemStr("ex_card_type"), "decode(nvl(B.card_type,''),'',C.card_type,B.card_type)")
               + sqlCol(wp.itemStr("ex_bin_type"), "decode(nvl(B.bin_type,''),'',C.bin_type,B.bin_type)");

   if (wp.itemEq("ex_source_99_flag", "Y")) {
      lsWhere += " and case_source <> '法院起訴或判決'";
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
         + " decode(nvl(B.card_type,''),'',C.card_type,B.card_type) as card_type , "
         + " A.case_date , "
         + " A.debit_card_date, "
         + " uf_idno_id2(A.card_no,'') as id_no,  "
         + " A.card_no ,  "
         + " A.case_type ,"
         + " A.fraud_ok_cnt,"
         + " A.fraud_ok_amt ,  "
         + " A.actual_amt,  "
         + " A.miscell_unpay_amt,  "
         + " A.recov_cash_amt,"
         + " A.recov_unpay_amt,"
         + " A.fraud_unpay_amt,"
         + " A.oth_unpay_amt,"
         + " A.case_seqno,"
         + " A.ch_deuct_amt,"
         + " A.for_mcht_amt,"
         + " A.cb_ok_amt,"
         + " A.case_file_flag,"
         + " A.acct_close_flag,"
         + " decode(nvl(B.group_code,''),'',C.group_code,B.group_code) as group_code , "
         + " (select name from ptr_card_type where card_type = decode(nvl(B.card_type,''),'',C.card_type,B.card_type)) as tt_card_type "
   ;
   wp.daoTable = "rsk_ctfi_case A left join crd_card B on A.card_no =B.card_no left join dbc_card C on A.card_no = C.card_no";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }

   if (wp.itemEq("ex_sort", "1")) {
      wp.whereOrder = " order by card_no ";
   }
   else if (wp.itemEq("ex_sort", "2")) {
      wp.whereOrder = " order by 1 ";
   }
   else if (wp.itemEq("ex_sort", "3")) {
      wp.whereOrder = " order by A.case_seqno ";
   }


   pageQuery();
   // list_wkdata();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();

}

void sumRead(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as sum_tot_cnt,"
         + " sum(A.fraud_ok_cnt) as sum_ok_cnt,"
         + " sum(A.fraud_ok_amt) as sum_ok_amt,"
         + " sum(A.actual_amt) as sum_act_amt,"
         + " sum(A.miscell_unpay_amt) as sum_miscell_amt,"
         + " sum(A.recov_cash_amt) as sum_recov_cash,"
         + " sum(A.recov_unpay_amt) as sum_recov_unpay,"
         + " sum(A.fraud_unpay_amt) as sum_fraud_unpay,"
         + " sum(A.oth_unpay_amt) as sum_oth_unpay,"
         + " sum(A.ch_deuct_amt) as sum_ch_deuct,"
         + " sum(A.for_mcht_amt) as sum_for_mcht,"
         + " sum(A.cb_ok_amt) as sum_cb_ok"
   ;
   wp.daoTable = "rsk_ctfi_case A left join crd_card B on A.card_no =B.card_no left join dbc_card C on A.card_no = C.card_no";
   wp.whereStr = lsWhere;
   list_wkdata();
   pageSelect();
}

void list_wkdata() {
   wp.colSet("sum_tot_cnt", "筆數:"
         + commString.numFormat(wp.colNum(0, "sum_tot_cnt"), "#,##0"));
}


@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

}
