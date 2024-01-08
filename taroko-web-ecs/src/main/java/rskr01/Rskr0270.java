package rskr01;
/**
 * 2020-1116   JH    modify
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Rskr0270 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   msgOK();
   strAction =wp.buttonCode;

   switch (wp.buttonCode) {
      case "L":
         strAction = "";
         clearFunc(); break;
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "R":
         dataRead(); break;
      case "PDF":
         pdfPrint(); break;
      default:
         alertErr("未指定 actionCode 執行功能, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      wp.optionKey = wp.colStr(0, "ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("再提示登錄日期起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1"
         + sqlCol(wp.itemStr("ex_id1"), "rep_add_user", "like%")
         + sqlCol(wp.itemStr("ex_curr_code"), "decode(curr_code,'','901',curr_code)")
         ;
   if (wp.itemEq("ex_apr_flag","N")) {
      lsWhere +=" and chg_stage ='2' and sub_stage ='10'"
            + sqlCol(wp.itemStr("ex_date1"), "rep_add_date", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "rep_add_date", "<=");
   }
   else {
      lsWhere +=" and chg_stage >='2' and rep_apr_date<>''"
            + sqlCol(wp.itemStr("ex_date1"), "rep_apr_date", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "rep_apr_date", "<=");
   }

   if (wp.itemEq("ex_dbcard_flag", "Y")) {
      lsWhere += " and debit_flag='Y' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "N")) {
      lsWhere += " and debit_flag<>'Y' ";
   }
   
   setSqlParmNoClear(true);
   sumFstAmt(lsWhere);

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {

   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " purchase_date , "
         + " dest_amt , dest_curr, "
         + " ctrl_seqno,"
         + " repsent_date,"
         + " rep_doc_mark,"
         + " rep_amt_twd,"
         + " rep_amt,"
         + " uf_acno_key2(card_no,debit_flag) as acct_key, "
         + " rep_msg,"
         + " rep_add_date,"
         + " rep_add_user,"
         + " source_amt,"
         + " source_curr,"
         + " acct_type,"
         + " bin_type,"
         + " dest_amt as db_sum_dest_amt,"
         + " rep_amt_twd as db_sum_rep_amt,"
         + " source_amt as db_sum_source_amt,"
         +" rep_ac_no, rep_glmemo3, film_no"
         + ", 1 as db_sum"
//			+ " uf_hi_idno(uf_acno_key2(p_seqno,debit_flag)) as wk_acct_key,"
//			+ " uf_hi_cardno(card_no) as wk_card_no"
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by rep_add_date ASC,rep_add_user, card_no, ctrl_seqno ";
   pageQuery();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      wp.colSet("sum_count", "0");
      wp.colSet("sum_dest_amt", "0");
      wp.colSet("sum_rep_amt", "0");
      wp.colSet("sum_source_amt", "0");
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
   queryAfter(sqlRowNum);
}

void queryAfter(int ll_nrow) throws Exception {
   if (!eqIgno(strAction,"PDF")) return;
   for (int ll=0; ll<ll_nrow; ll++) {
      String ss =wp.colStr(ll,"acct_type")+"-"+wp.colStr(ll,"acct_key");
      wp.colSet(ll,"wk_acct_key",ss);
   }
}

void sumFstAmt(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as sum_count,"
         + " sum(dest_amt) as sum_dest_amt,"
         + " sum(rep_amt_twd) as sum_rep_amt,"
         + " sum(source_amt) as sum_source_amt";
   wp.daoTable = "rsk_chgback";
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
public void pdfPrint() throws Exception {
	
	if(checkApproveZz()==false) {
		wp.respHtml = "TarokoErrorPDF";
      return;
	}
	
   wp.reportId = "rskr0270";
   wp.pageRows = 9999;

   //   if (wp.item_eq("ex_dbcard_flag", "Y")) {
//      ss = "Debit Card 再提示新增登錄待覆核清單";
//   }
//   else {
//      ss = "再提示新增登錄待覆核清單";
//   }
   String tt = "卡別: " ;
//   +commString.decode(wp.itemStr("ex_dbcard_flag"),"0,全部,Y,VD卡,N,信用卡");
   
   if(wp.itemEq("ex_dbcard_flag", "0")) {
	   tt += "全部";
   } else if(wp.itemEq("ex_dbcard_flag", "Y")) {
	   tt += "VD卡";
   } else if(wp.itemEq("ex_dbcard_flag", "N")) {
	   tt += "信用卡";
   }
   
   tt += "    再提示登錄日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "	
	   + commString.strToYmd(wp.itemStr("ex_date2"))
	   + "    再提示登錄者: "+ wp.itemStr("ex_id1")
	   + "    結算幣別: "+wp.itemStr("ex_curr_code")
	   ;
   	if (wp.itemEq("ex_apr_flag","Y")) {
      tt +="    已覆核";
   }
   else tt +="    未覆核";
   wp.colSet("cond1", tt);

   queryFunc();
   if (wp.listCount[0]<=0) {
      wp.respHtml =errPagePDF;
   }
   
   for (int ll=0; ll<wp.listCount[0]; ll++) {
      wp.colSet(ll,"card_no",wp.colStr(ll,"card_no"));
      wp.colSet(ll, "wk_acct_key",wp.colStr(ll,"acct_type")+"-"+wp.colStr(ll,"acct_key"));
   }
   
   TarokoPDF pdf = new TarokoPDF();
   pdf.pageCount = 33;
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0270.xlsx";
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
