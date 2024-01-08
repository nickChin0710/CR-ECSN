/* 問交/特交/不合格作業維護
 * 2018-0418:	Alex		modify
 * */
package rskm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
//import taroko.com.TarokoTAB;

public class Rskm0010 extends BaseEdit {
taroko.base.CommDate zzdate = new taroko.base.CommDate();

String kk1 = "", kk2 = "", kk3 = "";
rskm01.Rskm0010Func func;

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
   // wp.respCode + ",rHtml=" + wp.respHtml);
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
      insertFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
   // if (wp.respHtml.indexOf("_detl") > 0) {
   // detlTab_proc();
   // }
}

// void detlTab_proc() throws Exception {
//
// TarokoTAB tab = new TarokoTAB();
// tab.tabType = "T"; // "S5";
// tab.tabWidth = 450;
// tab.tabHeight = 30;
// tab.setTabConent(1, "問交維護");
// tab.setTabConent(2, "帳單資料");
// tab.generateTab(wp);
// tab = null;
//
// }

// void wkdata() throws Exception {
// if (wp.col_eq("prb_mark", "Q")) {
// wp.col_set("wk_prb_mark", "問交");
// }
// else if (wp.col_eq("prb_mark", "S")) {
// wp.col_set("wk_prb_mark", "特交");
// }
// else if (wp.col_eq("prb_mark", "E")) {
// wp.col_set("wk_prb_mark", "不合格");
// }
// }

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm0010")) {
         wp.optionKey = wp.colStr("ex_acct_type");
         dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("prb_reason_code");
         dddwList(
               "dddw_prb_reason_code",
               "ptr_sys_idtab", "wf_id", "wf_desc",
               "where wf_type='PRBL-REASON-CODE'");
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
   
   if(wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_ctrl_seqno")) {
	   alertErr("請輸入查詢條件");
	   return;
   }
   
//   lsSql += commSqlStr.in_idno("", lsAcctKey, "like%")
//         + sqlCol(lsCardNo, "card_no", "like%")
//         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
//   ;
//
//   if (empty(lsSql)) {
//      alertErr("請輸入查詢條件");
//      return;
//   }

   wp.whereStr = " where 1=1 "
         + sqlCol(lsCardNo,"card_no","like%")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(zzdate.sysAdd(0, -120, 0), "add_date", ">=")
         + sqlCol(wp.itemStr("ex_purch_YM"), "purchase_date", "like%")
         ;
   
   if(wp.itemEmpty("ex_acct_key") == false) {
	   wp.whereStr += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
			   +sqlCol(wp.itemStr("ex_acct_key"),"id_no")
			   + " union select id_p_seqno from dbc_idno where 1=1"
			   +sqlCol(wp.itemStr("ex_acct_key"),"id_no")
			   + " ) "
			   ;
   }

   if (wp.itemEq("ex_prb_mark", "0") == false) {
      wp.whereStr += sqlCol(wp.itemStr("ex_prb_mark"), "prb_mark");
   }

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
         + " card_no, "
         + commSqlStr.sqlDebitFlag + ","
         + " acquire_date , "
         + commSqlStr.mchtName("", "") + " as mcht_name,"
         + " source_amt , "
         + " source_curr,"
         + " dest_amt,"
         + " reference_no,"
         + " acct_month,"
         + " reference_seq,"
         + " purchase_date,"
         + " prb_mark,"
         + " prb_amount,"
         + " prb_reason_code,"
         + " prb_status,"
         + " uf_nvl(curr_code,'901') as curr_code,"
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt"
   ;
   wp.daoTable = "rsk_problem";
   wp.whereOrder = " order by ctrl_seqno ASC ";
   
   pageQuery();


   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(1);
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
   // listTab_proc();

   wp.selectSQL = "hex(rowid) as rowid, "
         + "uf_nvl(A.curr_code,'901') as curr_code, "
         + "uf_dc_amt(curr_code,	dest_amt, dc_dest_amt) as dc_dest_amt "
         + ", A.* , decode(A.back_status,'','','S','成功','F','失敗') as tt_back_status "
         ;
   wp.daoTable = "rsk_problem A";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "A.ctrl_seqno")
         + sqlCol(kk2, "A.bin_type");
   pageSelect();
   // wkdata();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }

   rskp0010Bill();
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

@Override
public void saveFunc() throws Exception {
   func = new rskm01.Rskm0010Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   sqlCommit(rc);
   // wp.ddd(func.getMsg());
   if (rc == 1) {
      alertMsg(func.getMsg());
   }
   else {
      alertErr(func.getMsg());
   }

}

@Override
public void initButton() {
   this.btnModeAud();
}

}
