package rskr01;
/**
 * 2020-0805   JH    bug-fix
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3020 extends BaseAction implements InfacePdf {
taroko.base.CommDate zzdate = new taroko.base.CommDate();

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
      case "PDF": //-PDF-
         strAction = "PDF";
         pdfPrint(); break;
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_add_date1,ex_add_date2,ex_expr_date1,ex_expr_date2,ex_card_no,ex_idno,ex_ctrl_seqno")) {
      alertErr("請輸入查詢條件");
      return;
   }
   if (!condStrend(wp.itemStr("ex_add_date1"),wp.itemStr("ex_add_date2"))) {
      alertErr(appMsg.errStrend+": 登錄日期");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_expr_date1"), wp.itemStr("ex_expr_date2")) == false) {
      alertErr("到期日起迄：輸入錯誤");
      return;
   }

   if (wp.itemEq("ex_pre_type","1")) {
      wp.whereStr =" where event_date>'' and pre_apply_date='' and pre_clo_result=''"
            +sqlStrend(wp.itemStr("ex_add_date1"),wp.itemStr("ex_add_date2"),"event_date")
            +sqlStrend(wp.itemStr("ex_expr_date1"),wp.itemStr("ex_expr_date2"),"pre_expire_date");
      wp.colSet("hh_pre_type","1");
      wp.itemSet("hh_pre_type","1");
   }
   else {
      wp.whereStr = " where pre_event_date>'' and pre_apply_date='' and pre_result=''"
      +sqlStrend(wp.itemStr("ex_add_date1"),wp.itemStr("ex_add_date2"),"pre_event_date")
      +sqlStrend(wp.itemStr("ex_expr_date1"),wp.itemStr("ex_expr_date2"),"pre_expire_date");
      wp.colSet("hh_pre_type","2");
      wp.itemSet("hh_pre_type","2");
   }

   wp.whereStr += sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%");
   if (!wp.itemEmpty("ex_card_no")) {
      wp.whereStr += sqlCol(wp.itemStr("ex_card_no"), "card_no");
   }
   else if (!wp.itemEmpty("ex_idno")) {      
      wp.whereStr +=" and card_no in (select card_no from Vcard_idno where 1=1 ";
      wp.whereStr += sqlCol(wp.itemStr("ex_idno"),"id_no") +" ) ";
//      wp.whereStr += " and card_no in ( select card_no from crd_card where id_p_seqno ='" + ls_id_pseqno[0] + "'"+
//      " union select card_no from dbc_card where id_p_seqno ='"+ls_id_pseqno[1]+"' )";
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL ="";

   if (wp.colEq("hh_pre_type","1")) {
      wp.daoTable ="rsk_precompl";
      wp.whereOrder =" order by event_date, ctrl_seqno";
      wp.selectSQL ="'預備依從' as db_pre_type" +
            ", card_no, debit_flag, uf_idno_id2(card_no,'') as id_no, ctrl_seqno" +
            ", event_date as pre_event_date, pre_expire_date, pre_add_user" +
            ", purchase_date, vcr_case_no, dc_dest_amt";
   }
   else {
      wp.daoTable ="rsk_prearbit";
      wp.whereOrder =" order by pre_event_date, ctrl_seqno";
      wp.selectSQL ="'預備仲裁' as db_pre_type" +
            ", card_no, debit_flag, uf_idno_id2(card_no,'') as id_no, ctrl_seqno" +
            ", pre_event_date as pre_event_date, pre_expire_date, pre_add_user" +
            ", purchase_date, vcr_case_no, dc_dest_amt";
   }

   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }   
   wp.setPageValue();
   queryAfter();
}

void queryAfter() {
	boolean lbPdf = eqIgno(wp.buttonCode, "PDF");
	for(int ii=0;ii<wp.selectCnt;ii++) {
		if(lbPdf) {
			wp.colSet(ii,"card_no", commString.hideCardNo(wp.colStr(ii,"card_no")));
			wp.colSet(ii,"id_no", commString.hideIdno(wp.colStr(ii,"id_no")));			
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
   wp.reportId = "Rskr3020";
   wp.pageRows = 9999;
   queryFunc();

   //--
//   ecsfunc.hiData oohh = new ecsfunc.hiData();
//   oohh.hh_cardno("card_no", "");
//   oohh.hh_idno("id_no", "");
//   oohh.hidata_wp(wp);

   TarokoPDF pdf = new TarokoPDF();
   pdf.pageCount = 35;
   String tt = "";
   tt = "登錄日期: " + commString.strToYmd(wp.itemStr("ex_add_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_add_date2"))
   +"  到期日: " + commString.strToYmd(wp.itemStr("ex_expr_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_expr_date2"));
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3020.xlsx";
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
