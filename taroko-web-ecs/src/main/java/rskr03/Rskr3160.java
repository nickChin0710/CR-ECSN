package rskr03;

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskr3160 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   //log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
//				strAction="new";
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
   else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
   }
   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("3160") > 0) {
         wp.optionKey = wp.colStr("ex_trans_unit");
         dddwList("dddw_trans_unit", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_TRANS_UNIT'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if (wp.itemEmpty("ex_secnd_date1") && wp.itemEmpty("ex_secnd_date2")) {
      alertErr("移轉二線日期不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_secnd_date1"), wp.itemStr("ex_secnd_date2")) == false) {
      alertErr("移轉二線日期起迄：輸入錯誤");
      return;
   }


   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_secnd_date1"), "secnd_date", ">=")
         + sqlCol(wp.itemStr("ex_secnd_date2"), "secnd_date", "<=")
         + sqlCol(wp.itemStr("ex_trans_unit"), "trans_unit")
         + sqlCol(wp.itemStr("ex_remark"), "proc_remark", "%like%")
         ;
      
   setSqlParmNoClear(true);
   sum(wp.whereStr);
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

void sum(String ls_where) throws Exception  {
   String sql1 = " select "
         + " sum(credit_limit) as tl_credit_limit , "
         + " sum(otb_amt) as tl_otb_amt , "
         + " sum(attm_cnt) as tl_attm_cnt , "
         + " sum(fraud_ok_cnt) as tl_fraud_ok_cnt , "
         + " sum(attm_amt) as tl_attm_amt , "
         + " sum(fraud_ok_amt) as tl_fraud_ok_amt "
         + " from rsk_ctfg_secnd "
         + ls_where;

   sqlSelect(sql1);

   wp.colSet("tl_credit_limit", sqlStr("tl_credit_limit"));
   wp.colSet("tl_otb_amt", sqlStr("tl_otb_amt"));
   wp.colSet("tl_attm_cnt", sqlStr("tl_attm_cnt"));
   wp.colSet("tl_fraud_ok_cnt", sqlStr("tl_fraud_ok_cnt"));
   wp.colSet("tl_attm_amt", sqlStr("tl_attm_amt"));
//		wp.colSet("tl_fraud_ok_amt", sqlStr("tl_fraud_ok_amt"));

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " ctfg_seqno , "
         + " secnd_date , "
         + " secnd_flag , "
         + " trans_unit , "
         + " case_type , "
         + " trans_date , "
         + " otb_amt , "
         + " credit_limit , "
         + " proc_remark , "
         + " attm_cnt , "
         + " attm_amt , "
         + " fraud_ok_cnt , "
         + " fraud_ok_amt , "
         + " replace(uf_card_name(card_no),'　','') as chi_name "
   ;
   wp.daoTable = "rsk_ctfg_secnd";
   wp.whereOrder = "order by secnd_date Asc , card_no Asc ";

   pageQuery();
   //list_wkdata();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(1);
   wp.totalRows = wp.dataCnt;
   wp.setPageValue();

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
   wp.reportId = "rskr3160";
   wp.pageRows = 9999;
   String ss = "";
   ss = "移轉二線日期:" + commString.strToYmd(wp.itemStr("ex_secnd_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_secnd_date2"));
   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3160.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}
}
