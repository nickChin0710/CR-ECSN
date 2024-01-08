package rskm01;
/**
 * 扣款沖銷作業處理
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;

public class Rskm0260 extends BaseAction {

String kk1 = "", kk2 = "", kk3 = "";

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
      //-資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      strAction = "A";
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      //is_action ="U";
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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

   if (wp.respHtml.indexOf("_detl") > 0) {
      //-顯示美金金額-
      if (wp.colEq("BL_source_curr", "901")) {
         wp.colSet("dsp_fst_amount", "none");
      }
   }

}

@Override
public void dddwSelect() {
   return;
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") < 0) {
      return;
   }

   wp.disabledKey = "N";
   btnModeAud();
}

@Override
public void dataRead() throws Exception {
   kk3 = wp.itemStr("ctrl_seqno");
   kk2 = wp.itemStr("reference_seq");
   kk1 = wp.itemStr("reference_no");

   if (empty(kk1) || isEmpty(kk2)) {
      alertErr("[控制流水號] 不可空白");
      return;
   }

   rskm01.Rskm0260Func func = new rskm01.Rskm0260Func();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("reference_seq", kk2);
   sqlRowNum = func.dataSelect();
   if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + kk1 + "; " + kk2);
      return;
   }

   select_bil_bill();
   detl_wkdata();
   return;

}

void select_bil_bill() {
   BilBill bill = new BilBill();
   bill.setConn(wp);

   bill.varsSet("reference_no", kk1);
   bill.varsSet("debit_flag", wp.colStr("debit_flag"));
   if (bill.dataSelect() != 1) {
      wp.notFound = "Y";
      alertErr(bill.getMsg());
      return;
   }
}

void detl_wkdata() {


}

@Override
public void queryFunc() throws Exception {
   String lsAcctKey = wp.itemStr("ex_acct_key");
   String lsCardNo = wp.itemStr("ex_card_no");
   String lsDate1 = wp.itemStr("ex_acct_date1");
   String lsDate2 = wp.itemStr("ex_acct_date2");
   if (!chkStrend(lsDate1, lsDate2)) {
      alertErr("[消費日期]: 起迄輸入錯誤");
      return;
   }

//   String ls_sql = zzsql.in_idno("", lsAcctKey, "like%")
//         + sqlCol(lsCardNo, "card_no", "like%")
//         + sqlStrend(lsDate1, lsDate2, "purchase_date")
//         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%");
//   if (empty(ls_sql)) {
//      alertErr("請輸入查詢條件");
//      return;
//   }
//

   
   if(empty(lsAcctKey) && empty(lsCardNo) && empty(lsDate1) && empty(lsDate2) && wp.itemEmpty("ex_ctrl_seqno")) {
	   alertErr("請輸入查詢條件");
	   return ;
   }         
   
   wp.whereStr = "WHERE 1=1 " 
		   + sqlCol(lsCardNo,"card_no","like%")
		   + sqlStrend(lsDate1,lsDate2,"purchase_date")
		   + sqlCol(wp.itemStr("ex_ctrl_seqno"),"ctrl_seqno","like%")
		   ;
   
   if(empty(lsAcctKey) == false) {
	   wp.whereStr += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
			   + sqlCol(lsAcctKey,"id_no")
			   + " union select id_p_seqno from dbc_idno where 1=1 "
			   + sqlCol(lsAcctKey,"id_no")
			   + " ) ";
   }
   
   switch (wp.colStr("ex_debit")) {   	
	case "1":		
	    wp.whereStr += " and debit_flag <>'Y'";
	    break;
	case "2":
	    wp.whereStr += " and debit_flag ='Y'";
	    break;
   }
   
   //-page control-
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " reference_no,"
         + " reference_seq,"
         + " ctrl_seqno,"
         + " bin_type,"
         + " card_no,"
         + " acct_type,"
         + " uf_acno_key(card_no) as acct_key,"
         + " chg_stage,"
         + " sub_stage,"
         + " fst_add_date,"
         + " chg_times,"
         + " final_close,"
         + " fst_apr_date,"
         + " fst_disb_add_date,fst_disb_apr_date,"
         + " rep_add_date,"
         + " debit_flag,"
         + " purchase_date , "
         + " fst_reverse_mark , "
         + " fst_reverse_date ";
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by ctrl_seqno, bin_type";

   pageQuery();
//	list_wkdata();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("查無資料");
   }
   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
   wp.itemSet("reference_no", wp.itemStr("data_k1"));
   wp.itemSet("reference_seq", wp.itemStr("data_k2"));
   wp.itemSet("ctrl_seqno", wp.itemStr("data_k3"));

   dataRead();

   return;
}

@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0260Func func = new rskm01.Rskm0260Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   else {
      dataRead();
      if (eqIgno(strAction, "D"))
         okMsg("[取消沖銷] 成功");
   }
}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {


}

}
