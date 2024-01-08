/*
 * 2020-0210  V1.00.01  Alex  fix queryRead
 *
 */
package rskr03;

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskr3110 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   //log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
//			strAction="new";
//			clearFunc();
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
//		xlsPrint();
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
      if (wp.respHtml.indexOf("3110") > 0) {
         wp.optionKey = wp.colStr("ex_rels_code");
         dddwList("dddw_rels_code", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_RELS_CODE'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   String lsWdate1 = wp.itemStr("ex_warn_date1");
   String lsWdate2 = wp.itemStr("ex_warn_date2");
   String lsRdate1 = wp.itemStr("ex_rels_date1");
   String lsRdate2 = wp.itemStr("ex_rels_date2");

   if (this.chkStrend(lsWdate1, lsWdate2) == false) {
      alertErr("管制日期: 起迄錯誤 ");
      return;
   }

   if (this.chkStrend(lsRdate1, lsRdate2) == false) {
      alertErr("解控日期: 起迄錯誤 ");
      return;
   }

   if (wp.itemEmpty("ex_warn_date1") &&
         wp.itemEmpty("ex_warn_date2") &&
         wp.itemEmpty("ex_rels_date1") &&
         wp.itemEmpty("ex_rels_date2")) {
      alertErr("管制日期 , 解控日期 不可全部空白");
      return;
   }

   String lsWhere = "where 1=1"
		 + sqlCol(lsWdate1,"warn_date",">=")
		 + sqlCol(lsWdate2,"warn_date","<=")
		 + sqlCol(lsRdate1,"rels_date",">=")
		 + sqlCol(lsRdate2,"rels_date","<=")         
         + sqlCol(wp.itemStr("ex_warn_user"), "warn_user", "%like%")
         + sqlCol(wp.itemStr("ex_rels_user"), "rels_user", "%like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
         + sqlCol(wp.itemStr("ex_rels_code"), "rels_code");

   if (eqIgno(wp.itemStr("ex_rels_flag"), "1")) {
      lsWhere += " and nvl(rels_code,'')<>'' ";
   }
   else if (eqIgno(wp.itemStr("ex_rels_flag"), "2")) {
      lsWhere += " and nvl(rels_code,'')='' ";
   }


//	if (eq_any(strAction,"Q")) {
//		query_Summary(lsWhere);
//	}
//
//	cond_where = lsWhere
//		  + "";
   wp.whereStr = lsWhere;


   //-page control-
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " ctfg_seqno , "
         + " warn_date , "
         + " warn_time , "
         + " find_type , "
         + " warn_user , "
         + " rels_code , "
         + " rels_date , "
         + " rels_time , "
         + " rels_user "
   ;
   wp.daoTable = "rsk_ctfg_mast";

   wp.whereOrder = " order by warn_date, warn_time";
   pageQuery();
   //list_wkdata();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(1);
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
   wp.reportId = "rskr3110";
   //-cond-
   //管制日期: ex_warn_date1--ex_warn_date2    解控日期: ex_rels_date1--ex_rels_date2
   String ss = "管制日期: " + commString.strToYmd(wp.itemStr("ex_warn_date1"))
         + " -- " + commString.strToYmd(wp.itemStr("ex_warn_date2"))
         + "    解控日期: " + commString.strToYmd(wp.itemStr("ex_rels_date1"))
         + " -- " + commString.strToYmd(wp.itemStr("ex_rels_date2"));
   wp.colSet("cond_1", ss);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3110.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
}

}
