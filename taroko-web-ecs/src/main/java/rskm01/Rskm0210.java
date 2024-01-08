package rskm01;
/**
 * 扣款作業維護
 * 2020-0218	JH		二次
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-1009:	JH		modify
 * 2018-0330:	JH		initial
 */

import ofcapp.BaseAction;

public class Rskm0210 extends BaseAction {

String kk1 = "", kk2 = "", kk3 = "";

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc(); break;
      case "Q": //查詢功能
         queryFunc(); break;
      case "R": // -資料讀取-
         dataRead(); break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
         saveFunc(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "L": //清畫面--
         strAction = "";
         clearFunc(); break;
      case "C": // -資料處理-
         procFunc(); break;
      default:
         alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }

   if (wp.respHtml.indexOf("_detl") > 0) {
      //-顯示美金金額-
      String lsCurr = wp.colStr("BL_source_curr");
      if (eqIgno(lsCurr, "901")) {
         wp.colSet("dsp_fst_amount", "none");
      }
      String ls_stage = wp.colStr("chg_stage");
      wp.colSet("tab_active2", "tab_active");
      wp.colSet("tab_active3", "");
      wp.colSet("tab_active4", "");
      if (eqIgno(ls_stage, "2")) {
         wp.colSet("tab_active3", "tab_active");
         wp.colSet("tab_active2", "");
         wp.colSet("tab_active4", "");
      }
      else if (eqIgno(ls_stage, "3")) {
         wp.colSet("tab_active4", "tab_active");
         wp.colSet("tab_active3", "");
         wp.colSet("tab_active2", "");
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

   if (empty(kk3) || isEmpty(kk2)) {
      alertErr("[控制流水號] 不可空白");
      return;
   }

   rskm01.Rskm0210Func func = new rskm01.Rskm0210Func();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("reference_seq", kk2);
   sqlRowNum = func.dataSelect();
   if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + kk1 + "; " + kk2);
      return;
   }

   selectBilBill(true);

   wp.colSetNum("fst_dc_amt", wp.colNum("fst_dc_amt"), 2);
   wp.colSetNum("sec_dc_amt", wp.colNum("sec_dc_amt"), 2);

   return;

}

//void select_bil_bill() throws Exception {
//   Bil_bill bill = new Bil_bill();
//   bill.setConn(wp);
//
//   bill.vars_set("reference_no", kk1);
//   bill.vars_set("debit_flag", wp.colStr("debit_flag"));
//
//   if (bill.dataSelect() != 1) {
//      wp.notFound = "Y";
//      alertErr(bill.getMsg());
//      return;
//   }
//}

int selectBilBill(boolean ab_refOrg) throws Exception {
   BilBill bill = new BilBill();
   bill.setConn(wp);
   String lsRefno =kk1;
   if (empty(lsRefno))
      lsRefno = wp.itemStr("reference_no");
   bill.varsSet("reference_no", lsRefno);
   bill.varsSet("debit_flag",wp.colStr("debit_flag"));

   if (bill.dataSelect() == -1) {
      alertErr(bill.getMsg());
      return -1;
   }
   //-原始帳單-
   if (ab_refOrg) {
      String lsRefnoOri = wp.colStr("BL_reference_no_ori");
      if (notEmpty(lsRefnoOri) && !eqIgno(lsRefno,lsRefnoOri)) {
         bill.billDataOri(lsRefnoOri);
      }
   }   
   return 1;
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
   if (!empty(lsAcctKey) && lsAcctKey.length() < 8) {
      alertErr("[身分證ID]: 至少要8碼");
      return;
   }
   
//   String ls_sql = zzsql.in_idno("", ls_acct_key, "like%")
//         + sql_col(ls_card_no, "card_no", "like%")
//         + sql_strend(ls_date1, ls_date2, "purchase_date")
//         + sql_col(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%");
   
   if(empty(lsAcctKey) && empty(lsCardNo) && empty(lsDate1) && empty(lsDate2) && wp.itemEmpty("ex_ctrl_seqno")) {
	   alertErr("請輸入查詢條件");
	   return ;
   }
   
   wp.whereStr = "WHERE 1=1" 
		   + sqlCol(lsCardNo,"card_no","like%")
		   + sqlStrend(lsDate1, lsDate2, "purchase_date")
		   + sqlCol(wp.itemStr("ex_ctrl_seqno"),"ctrl_seqno","like%")
		   ;     
   
   if(empty(lsAcctKey) == false) {
	   wp.whereStr += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
			   + sqlCol(lsAcctKey,"id_no")
			   + " union select id_p_seqno from dbc_idno where 1=1 "
			   + sqlCol(lsAcctKey,"id_no")
			   + " ) "
			   ;
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
         + " uf_acno_key2(card_no,acct_type) as acct_key,"
         + " chg_stage,"
         + " sub_stage,"
         + " fst_add_date,"
         + " chg_times,"
         + " final_close,"
         + " fst_apr_date,"
         + " fst_disb_add_date,fst_disb_apr_date,"
         + " rep_add_date,"
         + " uf_nvl(debit_flag,'N') as debit_flag,"
         + " purchase_date ";
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
   rskm01.Rskm0210Func func = new rskm01.Rskm0210Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   else {
      this.saveAfter(false);
      //dataRead();
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
