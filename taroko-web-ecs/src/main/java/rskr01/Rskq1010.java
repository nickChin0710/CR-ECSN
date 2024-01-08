package rskr01;
/**
 * 2020-0512   JH    call rskq0010_func
 * 2020-0408   Alex  add auth_query
 * 2020-0106   Jh    UAT.bug
 */

import ofcapp.BaseAction;

public class Rskq1010 extends BaseAction {
String kk1 = "", kk2 = "", kk3 = "", kk4 = "", chg_status = "";

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc();
         break;
      case "Q": //查詢功能
         queryFunc();
         break;
      case "R": // -資料讀取-
         dataRead();
         break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
         saveFunc(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead();
         break;
      case "S": //動態查詢--
         querySelect();
         break;
      case "L": //清畫面--
         strAction = "";
         clearFunc();
         break;
      case "C": // -資料處理-
         procFunc();
         break;
   }

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskq1010")) {
         wp.optionKey = wp.colStr(0, "ex_acct_type");
         dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_acct_key,ex_card_no,ex_add_date1,ex_add_date2,ex_acct_month,ex_ctrl_seqno")) {
      alertErr("請輸入查詢條件");
      return;
   }

   busi.func.ColFunc func = new busi.func.ColFunc();
   func.setConn(wp);

   String lsWhere = " where A.rsk_ctrl_seqno<>''"
		   		  + sqlCol(wp.itemStr("ex_ctrl_seqno"), "A.rsk_ctrl_seqno", "like%");
   if (!empty(wp.itemStr("ex_card_no"))) {
      if (func.fAuthQuery(wp.modPgm(), wp.itemStr("ex_card_no")) != 1) {
         alertErr(func.getMsg());
         return;
      }
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "A.card_no");
   }
   else if (!empty(wp.itemStr("ex_acct_key"))) {
      if (func.fAuthQuery(wp.modPgm(), commString.mid(wp.itemStr("ex_acct_key"), 0, 10)) != 1) {
         alertErr(func.getMsg());
         return;
      }
      lsWhere += " and A.p_seqno in (select p_seqno from dba_acno"
    		  + " where 1=1 "
    		  + sqlCol(wp.itemStr("ex_acct_key"),"acct_key")
    		  + sqlCol(wp.itemNvl("ex_acct_type", "01"),"acct_type")
    		  + " ) "
    		  ;
   }

   if (!itemallEmpty("ex_add_date1,ex_add_date2")) {
	   lsWhere += sqlCol(wp.itemStr("ex_add_date1"),"B.add_date",">=")
			   +  sqlCol(wp.itemStr("ex_add_date2"),"B.add_date","<=")
			   ;
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " A.rsk_ctrl_seqno, 'Y' as debit_flag, A.reference_no,"
         + " A.card_no,"
         + " A.bin_type,"
         + " A.purchase_date,"
         + " A.dest_amt,"
         + " A.source_amt,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_name , "
         + " A.mcht_country,"
         + " A.txn_code, "
         + " B.prb_status as prbl_status, B.add_date as prbl_add_date, "
         +" decode(B.clo_result,'','','-'||B.clo_result) as prbl_clo_result,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_name, "
         + " '' as chgb_status, '' as rept_status"
   ;

   wp.daoTable = "dbb_bill A left join rsk_problem B on A.reference_no=B.reference_no and B.reference_seq=0";
   wp.whereOrder = " order by A.rsk_ctrl_seqno ";

   pageQuery();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
   queryAfter();
}

void queryAfter() throws Exception {
   String sql1 = "select chgb_stage1||chgb_stage2 as chgb_status, rept_status " +
         ", compl_mark as compl_status, arbit_mark as arbit_status" +
         " from vrsk_ctrlseqno_dbb" +
         " where reference_no =?";

   for (int ll = 0; ll < wp.listCount[0]; ll++) {
      if (wp.colEmpty(ll, "rsk_ctrl_seqno")) continue;
      String lsRefno = wp.colStr(ll, "reference_no");
      sqlSelect(sql1, lsRefno);
      if (sqlRowNum <= 0) continue;

      sql2wp(ll, "chgb_status");
      sql2wp(ll, "rept_status");
      sql2wp(ll, "compl_status");
      sql2wp(ll, "arbit_status");
   }
}

@Override
public void querySelect() throws Exception {
   //**--kk1=reference_no kk2=ctrl_seqno kk3=bin_type
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");
   kk4 = wp.itemStr("data_k4");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   rskr01.Rskq0010Func func = new rskr01.Rskq0010Func();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("ctrl_seqno", kk2);
   func.readData();

   wp.listCount[0] = (func.wpRr + 1);
   if (func.wpRr < 0) {
      alertMsg("查無資料");
   }

   //--說明 > 中文
   String sql1 = " select wf_desc from ptr_sys_idtab "
         + " where wf_type ='RSK-STATUS-DESC' "
         + " and wf_id = ? ";
   for (int ii = 0; ii < wp.listCount[0]; ii++) {
      setSerNum(ii);
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "ex_s5")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "ex_s5", sqlStr("wf_desc"));
      }
   }

   //--
   rskm01.BilBill Bil = new rskm01.BilBill();
   Bil.setConn(wp);
   Bil.dataSelect(kk1, "Y");
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

}
