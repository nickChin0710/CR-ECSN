package rskm01;
/**
 * 調單作業維護
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-0329:	JH		modify
 */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm0110 extends BaseEdit {

String kk1 = "", kk2 = "", kk3 = "";
Rskm0110Func func;

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
         alertErr("不可由此, 新增調單");
         break;
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
//      case "C":
//         procFunc(); break;
//      case "PDF":
//         pdfPrint(); break;
//      case "XLS":  //-Excel-
//         is_action = "XLS";
//         xlsPrint(); break;
      default:
    	  alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();
}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("reason_code");
         dddwList("dddw_reason_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type ='RECEIPT-REASON-CODE'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") < 0) {
      return;
   }
   btnModeAud();
}

@Override
public void dataRead() throws Exception {
   if (isEmpty(kk1)) {
      kk1 = itemKk("reference_no");
   }
   if (isEmpty(kk2)) {
      kk2 = itemKk("reference_seq");
   }

   if (isEmpty(kk1) || isEmpty(kk2)) {
      alertErr("[帳單參考號, 調單序號] 不可空白");
      return;
   }

//	wp.selectSQL = " A.*,"
//			+sqlcond.ufunc("uf_nvl(debit_flag,'N') as debit_flag,")
//			+sqlcond.ufunc("uf_dc_curr(curr_code) as curr_code,")
//			+sqlcond.ufunc("uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt,")
//			+sqlcond.ufunc("uf_idno_name(id_p_seqno) as db_chi_name,")
//			+" hex(rowid) as rowid"
//			;
//	wp.daoTable = "rsk_receipt A";
//	wp.whereStr = "where 1=1"
//			+ sql_col(kk1, "reference_no")
//			+ sql_col(kk2, "reference_seq")
//			;
//	pageSelect();

   rskm01.RskReceipt rept = new rskm01.RskReceipt();
   rept.setConn(wp);
   sqlRowNum = rept.dataSelect(kk1, (int) this.toNum(kk2));
   if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + kk1 + "; " + kk2);
      return;
   }

   selectBilBill(true);
}

//void select_bil_bill() throws Exception  {
//   Bil_bill bill = new Bil_bill();
//   bill.setConn(wp);
//
//   bill.vars_set("reference_no", kk1);
//   bill.vars_set("debit_flag", wp.colStr("debit_flag"));
//   if (bill.dataSelect() != 1) {
//      wp.notFound = "Y";
//      err_alert(bill.getMsg());
//      return;
//   }
//}
int selectBilBill(boolean abRefOrg) throws Exception  {
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
   if (abRefOrg) {
      String ls_refno_ori = wp.colStr("BL_reference_no_ori");
      if (notEmpty(ls_refno_ori) && !eqIgno(lsRefno,ls_refno_ori)) {
         bill.billDataOri(ls_refno_ori);
      }
   }

   return 1;
}

@Override
public void saveFunc() throws Exception  {
   //-理由碼-
   if (wp.itemEmpty("db_reason_code") == false) {
      wp.itemSet("reason_code", wp.itemStr("db_reason_code"));
   }

   rskm01.Rskm0110Func func = new rskm01.Rskm0110Func();
   func.setConn(wp);

   if (eqIgno(strAction, "U")) {
      rc = func.dbUpdate();
      if (rc == 1) {
         this.alertMsg("調單修改成功; 控制流水號=" + wp.itemStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "D")) {
      rc = func.dbDelete();
      if (rc == 1) {
         this.alertMsg("調單刪除成功; 控制流水號=" + wp.itemStr("ctrl_seqno"));
      }
   }

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
}

@Override
public void queryFunc() throws Exception {
   String lsKey = "";
   String lsCardNo = wp.itemStr("ex_card_no");
   String lsAcctMonth = wp.itemStr("ex_acct_month");

   String lsSql = sqlCol(lsCardNo, "card_no", "like%")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(lsAcctMonth, "purchase_date", "like%");

   if (empty(wp.itemStr("ex_acct_key")) == false) {
	   lsKey = commString.acctKey(wp.itemStr("ex_acct_key"));
      if (lsKey.length() != 11) {
         errmsg("身分證字號輸入錯誤");
         return;
      }
      lsSql += " and p_seqno in "
            + " ( select p_seqno from act_acno where 1=1 " + sqlCol(lsKey, "acct_key") + sqlCol(wp.itemNvl("ex_acct_type", "01"), "acct_type")
            + " union "
            + " select p_seqno from dba_acno where 1=1 " + sqlCol(lsKey, "acct_key")
            + " ) "
      ;
   }

   if (isEmpty(lsSql)) {
      alertErr("請輸入查詢條件");
      return;
   }

   wp.whereStr = "WHERE 1=1" + lsSql;

   // -page control-
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " bin_type,"
         + " ctrl_seqno,"
         + " rept_seqno,"
         + " rept_status,"
         + " card_no,"
         + " uf_nvl(debit_flag,'N') as debit_flag,"
         + " purchase_date,"
         + " dest_amt,"
         + " uf_dc_curr(curr_code) as curr_code,"
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt,"
         + commSqlStr.mchtName("", "") + " as mcht_name,"
         + " rept_type, "
         + " reason_code , "
         + " add_date , "
         + " add_user , "
         + " reference_no , "
         + " reference_seq"
   ;
   wp.daoTable = "rsk_receipt";
   wp.whereOrder = " order by ctrl_seqno, bin_type";

   pageQuery();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");      //reference_no
   kk2 = wp.itemStr("data_k2");      //reference_seq
   kk3 = wp.itemStr("data_k3");      //crtl_seqno

   dataRead();
}

}
