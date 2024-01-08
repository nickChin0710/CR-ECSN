package rskm01;
/**
 * m01 問交/特交/不合格作業結案維護
 * 2020-0428:  Alex  add sign_flag
 * 2020-0319	JH		結案金額
 * 2018-0418:	JH		modify
 */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm0020 extends BaseEdit {

String kk1 = "", kk2 = "", kk3 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "R":
         dataRead(); break;
      case "A":
         insertFunc(); break;
      case "U":
         updateRetrieve = true;
         updateFunc(); break;
      case "D":
         deleteFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "L":
         strAction = "";
         clearFunc(); break;
   }

   dddwSelect();
   initButton();
}

@Override
public void dddwSelect() {
   String ls_where = "";
   try {
      if (eqIgno(wp.respHtml, "rskm0020")) {
         dddwList("dddw_ex_accttype", "ptr_acct_type"
               , "acct_type", "acct_type", "where 1=1");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("clo_result");
         if (!wp.colEq("debit_flag", "Y")) {
            ls_where = "where wf_type ='PRB" + wp.colStr("prb_mark") + "-CLO-RESULT' and wf_id not in ('04','31','32','33','61','62','63','65','66','67','71','72','73','81','82','83','I1','I2','I3') ";
            dddwList("dddw_clo_result", "ptr_sys_idtab"
                  , "wf_id", "wf_desc", ls_where);
         }
         else {
            ls_where = "where wf_type ='DBP" + wp.colStr("prb_mark") + "-CLO-RESULT'";
            dddwList("dddw_clo_result", "ptr_sys_idtab"
                  , "wf_id", "wf_desc", ls_where);
         }
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
   }
}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) {
      kk1 = wp.itemStr("ctrl_seqno");
   }
   if (empty(kk2)) {
      kk2 = wp.itemStr("bin_type");
   }
   if (empty(kk3)) {
      kk3 = wp.itemStr("reference_no");
   }

   if (empty(kk1) || empty(kk2)) {
      alertErr("[控制流水號, 卡別] 不可空白; kk=" + kk1 + "|" + kk2);
      return;
   }

   wp.selectSQL = "A.*, hex(A.rowid) as rowid, "
         + " uf_dc_curr(A.curr_code) as db_curr_code, "
         + " uf_dc_amt(A.curr_code,A.prb_amount,A.dc_prb_amount) as dc_prb_amount, "
         + " uf_dc_amt(A.curr_code,A.dest_amt,A.dc_dest_amt) as dc_dest_amt, "
         + " uf_tt_idtab('PRBL-REASON-CODE',A.prb_reason_code) as tt_reason_code , "
         + " decode(A.back_status,'','','S','成功','F','失敗') as tt_back_status "
   ;
   wp.daoTable = "rsk_problem A";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "a.ctrl_seqno")
         + sqlCol(kk2, "a.bin_type");

   pageSelect();
   if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + kk1 + "; " + kk2);
      return;
   }

   rskp0010Bill();

   //-deft:-
   if (wp.colNum("dc_mcht_repay") == 0) {
      wp.colSet("dc_mcht_repay", wp.colStr("dc_prb_amount"));
      wp.colSet("mcht_repay", wp.colStr("prb_amount"));
   }

   //detl_wkdata();
}

void rskp0010Bill() throws Exception {
   BilBill bill = new BilBill();
   bill.setConn(wp);

   bill.varsSet("reference_no", kk3);
   bill.varsSet("debit_flag", wp.colStr("debit_flag"));
   if (bill.dataSelect() != 1) {
      wp.notFound = "Y";
      alertErr(bill.getMsg());
      return;
   }
}

//
//void detl_wkdata() {
//	String ss = "";
//
//	ss = deCode_rsk.prb_src_code(wp.colStr("prb_src_code"));
//	wp.col_set(0, "tt_prb_src_code", ss);
//
//	ss = deCode_rsk.prb_status(wp.colStr("prb_status"));
//	wp.col_set(0, "tt_prb_status", ss);
//}

@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0020Func func = new rskm01.Rskm0020Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);
   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }
   //-OK-
   if (this.isDelete()) {
      this.alertMsg("取消結案成功");
   }

   //--
   userAction = true;
   dataRead();
}

@Override
public void queryFunc() throws Exception {
   String lsAcctKey = wp.itemStr("ex_acct_key");
   String lsCardNo = wp.itemStr("ex_card_no");
   String lsType = wp.itemStr("ex_prb_mark");
   String lsPurchaseDate = wp.itemStr("ex_purch_ym");
   
   if(empty(lsAcctKey) && empty(lsCardNo) && empty(lsPurchaseDate) && wp.itemEmpty("ex_ctrl_seqno")) {
	   alertErr("請輸入查詢條件");
	   return ;
   }        
   
   wp.whereStr = "WHERE 1=1"
         + sqlCol(lsCardNo,"card_no","like%")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"),"ctrl_seqno","like%")
         + sqlCol(lsPurchaseDate,"purchase_date","like%")
         + " and nvl(prb_status,'') in ('30','50','60') "
         ;
   
   if(empty(lsAcctKey) == false) {
	   wp.whereStr += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
			   + sqlCol(lsAcctKey,"id_no")
			   + " union select id_p_seqno from dbc_idno where 1=1 "
			   + sqlCol(lsAcctKey,"id_no")
			   + " ) ";
   }
   
   if(wp.itemEq("ex_prb_mark", "0") == false) {
	   wp.whereStr += sqlCol(wp.itemStr("ex_prb_mark"),"prb_mark");
   }
   
   wp.whereOrder = " order by ctrl_seqno";

   //-page control-
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " ctrl_seqno , "
         + " bin_type , "
         + " add_date , "
         + commSqlStr.mchtName("", "") + " as mcht_chi_name , "
         + " source_amt , "
         + " source_curr , "
         + " dest_amt , "
         + " reference_no , "
         + " reference_seq , "
         + " acct_month , "
         + " purchase_date , "
         + " prb_mark , "
         + " prb_reason_code , "
         + " prb_status , "
         + " uf_dc_amt(curr_code,prb_amount,dc_prb_amount) as prb_amount , "
         + " uf_tt_idtab('PRBQ_REASON_CODE',prb_reason_code) as tt_prb_reason_code, "
         + " uf_nvl(debit_flag,'N') as debit_flag,"
         + " uf_dc_curr(curr_code) as curr_code , "
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt,"
         + " uf_tt_idtab('PRBL-REASON-CODE',prb_reason_code) as tt_reason_code , "
         + " sign_flag "
   ;
   wp.daoTable = "rsk_problem";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }

   pageQuery();
   //list_wkdata();

   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }

   //wp.totalRows = wp.dataCnt;
   wp.setPageValue();
}

//void list_wkdata() {
//	String ss = "";
//	//prb_mark: Q.問交 S.特交 E.不合格
//	for (int ii = 0; ii < wp.selectCnt; ii++) {
//		ss = wp.colStr(ii, "prb_mark");
//		wp.col_set(ii, "tt_prb_mark", deCode_rsk.prb_mark(ss));
//		ss = wp.colStr("prb_status");
//		wp.col_set(ii, "tt_prb_status", deCode_rsk.prb_status(ss));
//	}
//}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");

   dataRead();
}

}
