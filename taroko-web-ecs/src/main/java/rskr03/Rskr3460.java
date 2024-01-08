package rskr03;


import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3460 extends BaseAction implements InfacePdf {

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
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (!empty(wp.itemStr("ex_idno"))) {
      if (wp.itemStr("ex_idno").length() < 5) {
         alertErr("ID 長度不可小於 5");
         return;
      }
   }

   String lsWhere = " where 1=1 and C.ctrl_seqno = B.ctrl_seqno and A.case_no = B.case_no "
         + " and B.ecs_close_date ='' "
         + sqlCol(wp.itemStr("ex_proc_user"), "A.proc_user", "like%")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "B.ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_idno"), "A.idno", "like%");
   if (eqIgno(wp.itemStr("ex_proc_status"), "3")) {
      lsWhere += " and C.cb_date_1st < to_char(sysdate - 48,'yyyymmdd') "
            + " and C.rp_date ='' "
            + " and C.cb_date_2nd ='' "
            + " and C.pre_arbi_date ='' "
            + " and C.pre_comp_date ='' ";
   }
   else if (eqIgno(wp.itemStr("ex_proc_status"), "4")) {
      lsWhere += " and C.cb_date_2nd < to_char(sysdate - 48,'yyyymmdd') "
            + " and C.pre_arbi_date ='' "
            + " and C.pre_comp_date ='' ";
   }
   else if (eqIgno(wp.itemStr("ex_proc_status"), "5")) {
      lsWhere += " and C.pre_arbi_date < to_char(sysdate - 30,'yyyymmdd') ";
   }
   else if (eqIgno(wp.itemStr("ex_proc_status"), "6")) {
      lsWhere += " and C.pre_comp_date < to_char(sysdate - 30,'yyyymmdd') ";
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
         + " A.proc_user ,"
         + " decode(substr(B.ctrl_seqno,1),'M',1,'V',2,'J',3,'N',4,9) as db_sort_col2 ,"
         + " A.idno ,"
         + " A.card_no ,"
         + " B.ctrl_seqno ,"
         + " B.txn_date ,"
         + " B.txn_amt ,"
         + " C.hold_amt ,"
         + " C.cb_amt_1st ,"
         + " C.cb_amt_2nd ,"
         + " C.pre_arbi_flag ,"
         + " C.pre_comp_amt ,"
         + " C.cb_date_1st ,"
         + " C.cb_date_2nd ,"
         + " C.pre_arbi_date ,"
         + " C.pre_comp_date ,"
         + "uf_ctfc_idname(A.card_no) as db_id_name , "
         + "uf_hi_cname(uf_ctfc_idname(A.card_no)) as db_cname "
   ;
   wp.daoTable = "rsk_ctfc_mast A, rsk_ctfc_txn B, rsk_ctfc_proc C ";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   wp.whereOrder = " order by 1,2 ";
   pageQuery();
//		queryAfter();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
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
   wp.reportId = "rskr3460";
   wp.varRows = 999999;
//		wp.pageRows =9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   String tt = "";
   if (eqIgno(wp.itemStr("ex_proc_status"), "3")) {
      tt += "列印類別 : 3.1st CB  ";
   }
   else if (eqIgno(wp.itemStr("ex_proc_status"), "4")) {
      tt += "列印類別 : 4.2nd CB  ";
   }
   else if (eqIgno(wp.itemStr("ex_proc_status"), "5")) {
      tt += "列印類別 : 5.Pre-Arb ";
   }
   else if (eqIgno(wp.itemStr("ex_proc_status"), "6")) {
      tt += "列印類別 : 6.Pre-Comp ";
   }

   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3460.xlsx";
   pdf.pageCount = 35;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;


}

}
