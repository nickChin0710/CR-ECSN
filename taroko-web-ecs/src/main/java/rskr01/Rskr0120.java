/*
 * 2019-1209  V1.00.01  Alex  update queryWhere
 *
 */
package rskr01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0120 extends BaseAction implements InfacePdf {

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
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
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
   else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
//			xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
   }


}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskr0120")) {
         wp.optionKey = wp.colStr(0, "ex_curr_code");
         dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("結案登錄日期 起迄錯誤");
      return;
   }

   String lsWhere = " where 1=1 "
         + " and rept_status >= 60 "
//							 + " and rept_status in('80','85') "
         + sqlCol(wp.itemStr("ex_date1"), "close_add_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "close_add_date", "<=")
         + sqlCol(wp.itemStr("ex_user"), "close_add_user", "like%")
         + sqlCol(wp.itemStr("ex_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_curr_code"), commSqlStr.ufunc("uf_dc_curr(curr_code)"));

   if (!eqIgno(wp.itemStr("ex_debit_flag"), "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_debit_flag"), "debit_flag");
   }

   if (!eqIgno(wp.itemStr("ex_mark"), "0")) {
      lsWhere += " and exists (select ctrl_seqno from rsk_problem where reference_no = A.reference_no " + sqlCol(wp.itemStr("ex_mark"), "prb_mark") + ")";
//			lsWhere += " and exists (select ctrl_seqno from rsk_ctrlseqno_log where ctrl_seqno =A.ctrl_seqno and prbl_mark='"+wp.itemStr("ex_mark")+"')";
   }

   if (wp.itemEq("ex_apr_flag", "1")) {
      lsWhere += " and close_apr_date <> '' ";
   }
   else if (wp.itemEq("ex_apr_flag", "2")) {
      lsWhere += " and close_apr_date = '' ";
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
         + " card_no , "
         + " uf_hi_cardno(card_no) as hh_card_no , "
         + " purchase_date ,"
         + " uf_dc_curr(curr_code) as curr_code , "
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt , "
         + " add_date ,"
         + " reason_code ,"
         + " ctrl_seqno ,"
         + " rept_type ,"
         + " proc_result ,"
         + " recv_date ,"
         + " add_user ,"
         + " apr_date , "
         + " add_date , "
         + " close_apr_date "
   ;
   wp.daoTable = " rsk_receipt A ";
   pageQuery();   
   if (sqlRowNum <= 0) {
      alertErr("查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(1);
   wp.setPageValue();
}

void queryAfter() {
	boolean lbPdf = eqIgno(wp.buttonCode, "PDF");
	for(int ii=0;ii<wp.selectCnt;ii++) {
		if(lbPdf) {
			wp.colSet(ii,"card_no", wp.colStr(ii,"hh_card_no"));
		}
	}
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

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "Rskr0120";
   wp.pageRows = 9999;
   String tt, ss;
   if (eqIgno(wp.itemStr("debit_flag"), "Y")) {
      ss = "Debit Card 調單結案清單";
   }
   else {
      ss = "調單結案清單";
   }
   wp.colSet("title", ss);
   tt = "結果登錄日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
         + commString.strToYmd(wp.itemStr("ex_date2"))
         + " 結果登錄者: " + wp.itemStr("ex_user") + " 結算幣別:" + wp.itemStr("ex_curr_code")
   ;

   if (eqIgno(wp.itemStr("ex_mark"), "0")) {
      tt += "  【全部調單】";
   }
   else if (eqIgno(wp.itemStr("ex_mark"), "N")) {
      tt += "  【正常交易】";
   }
   else if (eqIgno(wp.itemStr("ex_mark"), "Q")) {
      tt += "  【問題交易】";
   }
   else if (eqIgno(wp.itemStr("ex_mark"), "S")) {
      tt += "  【特殊交易】";
   }
   else if (eqIgno(wp.itemStr("ex_mark"), "E")) {
      tt += "  【不合格帳單】";
   }

   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0120.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
