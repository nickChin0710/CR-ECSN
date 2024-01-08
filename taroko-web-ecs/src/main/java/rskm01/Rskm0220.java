package rskm01;

/** 扣款作業結案維護 V.2019-0321
 *  19-1206:    Alex  add initButton
 *  19-0321:    JH    modify
 *
 * */
public class Rskm0220 extends ofcapp.BaseAction {

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
      // -資料讀取-
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
      strAction = "U";
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U2")) {
      /* 刪除功能 */
      strAction = "D";
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
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }

}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("CB_clo_result");
         dddwList("dddw_clo_result", "ptr_sys_idtab", "wf_id", "wf_desc"
               , "where wf_type='CHGBACK-CLO-RESULT'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   String lsAcctKey = wp.itemStr("ex_acct_key");
   String lsCardNo = wp.itemStr("ex_card_no");
   String lsSql = "";

//   ls_sql += zzsql.in_idno("", ls_acct_key, "like%")
//         + sql_col(ls_card_no, "card_no", "like%")
//         + sql_col(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
//   ;
//
//   if (empty(ls_sql)) {
//      alertErr("請輸入查詢條件");
//      return;
//   }
   
   if(empty(lsAcctKey) && empty(lsCardNo) && wp.itemEmpty("ex_ctrl_seqno") ) {
	   alertErr("請輸入查詢條件");
	   return ;
   }
   
   wp.whereStr = " where 1=1 "
		   + sqlCol(lsCardNo,"card_no","like%")
		   + sqlCol(wp.itemStr("ex_ctrl_seqno"),"ctrl_seqno","like%")
		   + sqlCol(wp.itemStr("ex_acct_month"), "purchase_date", "like%")
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

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "hex(rowid) as rowid, "
         + "ctrl_seqno, "
         + "bin_type, "
         + "card_no, "
         + "chg_stage, "
         + "sub_stage, "
         + "chg_times, "
         + "fst_add_date, "
         + "final_close, "
         + "reference_no, "
         + "reference_seq, "
         + "fst_add_date,"
         + "fst_disb_apr_date, "
         + "rep_add_date, debit_flag"
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by ctrl_seqno ASC  ";

   pageQuery();
   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }

   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) {
      kk1 = wp.itemStr("CB_reference_no");
   }
   if (empty(kk2)) {
      kk2 = wp.itemStr("CB_reference_seq");
   }
   if (empty(kk3)) {
      kk3 =wp.itemNvl("CB_debit_flag","N");
   }

   // listTab_proc();
   rskm01.RskChgback func = new rskm01.RskChgback();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("reference_seq", kk2);
   rc = func.dataSelect();
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }
   //-close_reason-
   wp.colSet("wk_close_reason", wp.colStr("CB_fst_reason_code"));
   if (wp.colEq("CB_chg_stage", "3")) {
      wp.colSet("wk_close_reason", wp.colStr("CB_sec_reason_code"));
   }

   // --
   BilBill ooBill = new BilBill();
   ooBill.setConn(wp);
   ooBill.varsSet("reference_no", kk1);
   ooBill.varsSet("debit_flag", kk3);
   ooBill.dataSelect();

   wp.actionCode = "";
}

@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0220Func func = new rskm01.Rskm0220Func();
   func.setConn(wp);

   wp.itemSet("rowid", wp.itemStr("CB_rowid"));
   wp.itemSet("mod_seqno", wp.itemStr("CB_mod_seqno"));
   if (eqIgno(strAction, "U")) {
      rc = func.dbUpdate();
      if (rc == 1) {
         wp.respMesg = "扣款[結案] 完成";
      }
   }
   else if (eqIgno(strAction, "D")) {
      rc = func.dbDelete();
      if (rc == 1) {
         wp.respMesg = "扣款[結案取消] 完成";
      }
   }

   this.sqlCommit(rc);
   if (rc == -1) {
      errmsg(func.getMsg());
      return;
   }

   dataRead();
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud("XX");
   }
}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
