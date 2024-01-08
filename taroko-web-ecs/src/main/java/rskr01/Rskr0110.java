package rskr01;
/**
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0110 extends BaseAction implements InfacePdf {

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
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
   }

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskr0110")) {
         wp.optionKey = wp.colStr(0, "ex_curr_code");
         dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {

   if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("登錄日期:起迄錯誤");
      return;
   }

   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_date1"), "add_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "add_date", "<=")
         + sqlCol(wp.itemStr("ex_user"), "add_user", "like%")
         + sqlCol(wp.itemStr("ex_curr_code"), "uf_dc_curr(curr_code)")
         + sqlCol(wp.itemStr("ex_seqno"), "ctrl_seqno", "like%");

   if (!eqIgno(wp.itemStr("ex_debit_flag"), "0")) {
      ls_where += sqlCol(wp.itemStr("ex_debit_flag"), "debit_flag");
   }

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();


}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " uf_hi_cardno(card_no) as hh_card_no ,"
         + " ctrl_seqno ,"
         + " purchase_date ,"
         + " uf_dc_curr(curr_code) as curr_code , "
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt , "
         + " rept_type ,"
         + " reason_code ,"
         + " add_date ,"
         + " add_user ,"
         + " v_card_no ,"
         + " uf_idno_name2(id_p_seqno,debit_flag) as db_idno_name , "
         + " uf_hi_cname(uf_idno_name2(id_p_seqno,debit_flag)) as hh_idno_name "
   ;
   wp.daoTable = "rsk_receipt";

   wp.whereOrder = " order by add_date, ctrl_seqno ";
   pageQuery();

   wp.setListCount(1);
	queryAfter();
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();

}

void queryAfter() {
	
	boolean lb_pdf = eqIgno(wp.buttonCode, "PDF");
	for(int ii=0;ii<wp.selectCnt;ii++) {
		if(lb_pdf) {
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
   wp.reportId = "rskr0110";
   wp.pageRows = 9999;

   String ss;
   ss = "登錄日期: " + commString.strToYmd(wp.itemStr("ex_date1"))
         + " -- " + commString.strToYmd(wp.itemStr("ex_date2"));

   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();

   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0110.xlsx";
   pdf.pageCount = 35;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
