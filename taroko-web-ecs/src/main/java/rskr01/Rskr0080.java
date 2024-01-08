package rskr01;
/**
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskr0080 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   //ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
//				is_action="new";
//				clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) { //-資料讀取-
      strAction = "R";
      //         dataRead();
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
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
//			xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskr0080")) {
         wp.optionKey = wp.colStr("ex_acct_type");
         dddwList("dddw_acct_type", "Vmkt_acct_type"
               , "acct_type", "acct_type", "where 1=1");
      }

   }
   catch (Exception ex) {
   }
}


@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("close_conf_date1"), wp.itemStr("close_conf_date2")) == false) {
      alertErr("問交結案放行日期起迄：輸入錯誤");
      return;
   }

   if (wp.itemEmpty("close_conf_date1") &&
         wp.itemEmpty("close_conf_date2") &&
         wp.itemEmpty("ex_acct_key") &&
         wp.itemEmpty("ex_card_no")) {
      alertErr("日期,卡號,帳戶帳號 不可全部空白");
      return;
   }

   String lsWhere = " where payment_type in ('I','E')  "
         + sqlCol(wp.itemStr("close_conf_date1"), "close_apr_date", ">=")
         + sqlCol(wp.itemStr("close_conf_date2"), "close_apr_date", "<=")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no");


   if (!wp.itemEmpty("ex_acct_key")) {
      String lsAcctKey = "";
      lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
      if (lsAcctKey.length() != 11) {
         alertErr("帳戶帳號輸入錯誤");
         return;
      }
      String lsActype = wp.itemStr("ex_acct_type");
      lsWhere += sqlCol(lsActype, "acct_type")
    		  + " and p_seqno in (select p_seqno from act_acno where 1=1 "
    		  + sqlCol(lsActype,"acct_type")
    		  + sqlCol(lsAcctKey,"acct_key")
    		  + " union "
    		  + " select p_seqno from dba_acno where 1=1 "
    		  + sqlCol(lsActype,"acct_type")
    		  + sqlCol(lsAcctKey,"acct_key")
    		  + " ) ";
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
         + " reference_no , "
         + " close_apr_date, "
         + " acct_type , "
         + " uf_acno_key2(card_no,acct_type) as acct_key ,"
         //+ " acct_type||'-'||uf_hi_idno(uf_acno_key2(card_no,acct_type)) as hh_acct_key , "
         + " uf_hi_cardno(card_no) as hh_card_no , "
         + " card_no ,"
         + " prb_amount,"
         + " clo_result , "
         + " clo_result||'.'||uf_tt_idtab('%-CLO-RESULT',clo_result) as tt_clo_result "
   ;
   wp.daoTable = "rsk_problem";
   wp.whereOrder = " order by close_apr_date ";
   pageQuery();


   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();
   wp.setListCount(1);
   queryAfter();
}

void queryAfter() throws Exception {

	boolean lbPdf = eqIgno(wp.buttonCode, "PDF");
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));
      if(lbPdf) {
      	wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + commString.hideIdno(wp.colStr(ii, "acct_key")));
      	wp.colSet(ii, "card_no", commString.hideCardNo(wp.colStr(ii,"card_no")));
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
public void pdfPrint() throws Exception {
   wp.reportId = "rskr0080";
   String ss;
   ss = "結案放行日期: " + commString.strToYmd(wp.itemStr("close_conf_date1"))
         + " -- " + commString.strToYmd(wp.itemStr("close_conf_date2"));

   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0080.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
