/*
 * 2020-0109  V1.00.01  Alex  add chinese code
 *
 */
package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3350 extends BaseQuery {

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
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_case_date1")) &&
         empty(wp.itemStr("ex_case_date2")) &&
         empty(wp.itemStr("ex_debit_date1")) &&
         empty(wp.itemStr("ex_debit_date2"))) {
      alertErr("立案日期 不可全部空白");
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
         " where 1=1 and A.card_no = B.card_no "
               + sqlCol(wp.itemStr("ex_debit_date1"), "A.debit_card_date", ">=")
               + sqlCol(wp.itemStr("ex_debit_date2"), "A.debit_card_date", "<=")
               + sqlCol(wp.itemStr("ex_case_date1"), "A.case_date", ">=")
               + sqlCol(wp.itemStr("ex_case_date2"), "A.case_date", "<=");
   if (wp.itemEq("ex_close_flag", "1")) {
      lsWhere += " and A.case_close_flag ='Y'";
   }
   else if (wp.itemEq("ex_close_flag", "2")) {
      lsWhere += " and A.case_close_flag <>'Y'";
   }
   if (wp.itemEq("ex_case_source_99", "Y")) {
      lsWhere += " and A.case_source<>'法院起訴或判決'";
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
         + " sum(nvl(B.txn_amt,0)) as db_txn_amt,  "
         + " count(*) db_cnt, "
         + " count(distinct A.card_no) db_cnt_card,  "
         + " sum(decode(B.resp_code,'00',1,0)) as db_ok_cnt,"
         + " sum(decode(B.resp_code,'00',nvl(B.txn_amt,0),0)) as db_ok_amt "
//			+ " sum(A.actual_amt)/count(*) as db_actual_amt,"
//			+ " sum(A.miscell_unpay_amt)/count(*) as db_miscell_unpay_amt,  "
//			+ " sum(A.actual_amt - A.miscell_unpay_amt)/count(*) as db_amt1"
   ;

   if (wp.itemEq("ex_report", "1")) {
      wp.daoTable = "rsk_ctfi_case A, rsk_ctfi_txn B";
      wp.colSet("db_report_type", "交易國別");
      wp.selectSQL += ", B.mcht_country as db_report_value";
      wp.whereOrder = "group by B.mcht_country "
            + "order by 1 desc, B.mcht_country ";
      wp.pageCountSql = " select count(*) from ("
            + " select distinct B.mcht_country from rsk_ctfi_case A, rsk_ctfi_txn B "
            + wp.whereStr
            + " ) "
      ;
   }
   else if (wp.itemEq("ex_report", "2")) {
      wp.daoTable = "rsk_ctfi_case A, rsk_ctfi_txn B";
      wp.colSet("db_report_type", "MCC");
      wp.selectSQL += ", B.mcht_category as db_report_value ";
      wp.whereOrder = " group by B.mcht_category "
            + " order by 1 desc, B.mcht_category ";
      wp.pageCountSql = " select count(*) from ("
            + " select distinct B.mcht_category from rsk_ctfi_case A, rsk_ctfi_txn B "
            + wp.whereStr
            + " ) "
      ;
   }
   else if (wp.itemEq("ex_report", "3")) {
      wp.daoTable = "rsk_ctfi_case A join rsk_ctfi_txn B on A.card_no = B.card_no "
            + " left join crd_card C on A.card_no = C.card_no left join dbc_card D on A.card_no = D.card_no "
      ;
      wp.colSet("db_report_type", "卡種");
      wp.selectSQL += ",decode(nvl(C.card_type,''),'',D.card_type,C.card_type) as db_report_value";
      wp.whereOrder = " group by decode(nvl(C.card_type,''),'',D.card_type,C.card_type) "
            + " order by 1 desc, decode(nvl(C.card_type,''),'',D.card_type,C.card_type) ";
      wp.pageCountSql = " select count(*) from ("
            + " select distinct decode(nvl(C.card_type,''),'',D.card_type,C.card_type) "
            + " from rsk_ctfi_case A join rsk_ctfi_txn B on A.card_no = B.card_no "
            + " left join crd_card C on A.card_no = C.card_no left join dbc_card D on A.card_no = D.card_no "
            + wp.whereStr
            + " ) "
      ;
   }
   else if (wp.itemEq("ex_report", "4")) {
      wp.daoTable = "rsk_ctfi_case A, rsk_ctfi_txn B";
      wp.colSet("db_report_type", "團代");
      wp.selectSQL += ",A.group_code as db_report_value";
      wp.whereOrder = " group by A.group_code "
            + " order by 1 desc, A.group_code ";
      wp.pageCountSql = " select count(*) from ("
            + " select distinct A.group_code from rsk_ctfi_case A, rsk_ctfi_txn B "
            + wp.whereStr
            + " ) "
      ;
   }

   pageQuery();
   // list_wkdata();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();

   queryReadAfter();

}

void queryReadAfter() throws Exception  {   
   int liSelectCnt = 0;
   liSelectCnt = wp.selectCnt;
   String sql1 = "";
   
   for (int ii = 0; ii < liSelectCnt; ii++) {
	   sql1 = getSql1();
		  if (wp.itemEq("ex_report", "3")) {
			  setString("db_report_value1",wp.colStr(ii, "db_report_value"));
			  setString("db_report_value2",wp.colStr(ii, "db_report_value"));
			  sqlSelect(sql1);
		  } else {
			  setString("db_report_value",wp.colStr(ii, "db_report_value"));
			  sqlSelect(sql1);
		  }
			  
		  wp.colSet(ii, "db_actual_amt", sqlStr("db_actual_amt"));
		  wp.colSet(ii, "db_miscell_unpay_amt", sqlStr("db_miscell_unpay_amt"));
		  wp.colSet(ii, "db_amt1", sqlStr("db_amt1"));
   }

}

   String getSql1() {	   
	   String lsWhere = "" , tempSql = "";
	   lsWhere = " where 1=1 " ;
	   if(wp.itemEmpty("ex_debit_date1") == false) {
		   lsWhere += " and debit_card_date >= :ex_debit_date1 ";
		   setString("ex_debit_date1",wp.itemStr("ex_debit_date1"));
	   }	  
	   if(wp.itemEmpty("ex_debit_date2") == false) {
		   lsWhere += " and debit_card_date <= :ex_debit_date2 ";
		   setString("ex_debit_date2",wp.itemStr("ex_debit_date2"));
	   }	  
	   if(wp.itemEmpty("ex_case_date1") == false) {
		   lsWhere += " and case_date >= :ex_case_date1 ";
		   setString("ex_case_date1",wp.itemStr("ex_case_date1"));
	   }	  
	   if(wp.itemEmpty("ex_case_date2") == false) {
		   lsWhere += " and case_date <= :ex_case_date2 ";
		   setString("ex_case_date2",wp.itemStr("ex_case_date2"));
	   }
	  
	   if (wp.itemEq("ex_close_flag", "1")) {
		   lsWhere += " and case_close_flag ='Y'";
	   } else if (wp.itemEq("ex_close_flag", "2")) {
		   lsWhere += " and case_close_flag <>'Y'";
	   }
	   
	   if (wp.itemEq("ex_case_source_99", "Y")) {
		   lsWhere += " and case_source <> '法院起訴或判決' ";
	   }
	   
	   if (wp.itemEq("ex_report", "1")) {		  
		   lsWhere += " and card_no in (select card_no from rsk_ctfi_txn where mcht_country = :db_report_value )";
	   } else if (wp.itemEq("ex_report", "2")) {
		   lsWhere += " and card_no in (select card_no from rsk_ctfi_txn where mcht_category = :db_report_value )";
	   } else if (wp.itemEq("ex_report", "3")) {
		   lsWhere += " and card_no in " + " (select card_no from crd_card where card_type = :db_report_value1" + " union"
				   + " select card_no from dbc_card where card_type = :db_report_value2)";
	   } else if (wp.itemEq("ex_report", "4")) {
		   lsWhere += " and group_code = :db_report_value ";
	   }	  
	  
	   tempSql = " select  " + " sum(actual_amt) as db_actual_amt,"
			   + " sum(miscell_unpay_amt) as db_miscell_unpay_amt,  "
			   + " sum(actual_amt - miscell_unpay_amt) as db_amt1 " + " from rsk_ctfi_case " + lsWhere;
	   
	   return tempSql ;	  
   }

void sumRead(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(distinct A.card_no) as sum_cnt_card,"
         + " count(*) as sum_txn_cnt,"
         + " sum(nvl(B.txn_amt,0)) as sum_txn_amt,"
         + " sum(decode(B.resp_code,'00',1,0)) as sum_ok_cnt,"
         + " sum(decode(B.resp_code,'00',nvl(B.txn_amt,0),0)) as sum_ok_amt"
   ;
   if (wp.itemEq("ex_report", "3")) {
      wp.daoTable = " rsk_ctfi_case A join rsk_ctfi_txn B on A.card_no = B.card_no "
            + " left join crd_card C on A.card_no = C.card_no left join dbc_card D on A.card_no = D.card_no "
      ;
      wp.whereStr = lsWhere;

   }
   else {
      wp.daoTable = "rsk_ctfi_case A, rsk_ctfi_txn B";
      wp.whereStr = lsWhere;
   }
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

}
