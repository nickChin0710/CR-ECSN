package rskr03;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3410 extends BaseAction implements InfacePdf {

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
      if (eqIgno(wp.respHtml, "rskr3410")) {
         wp.optionKey = wp.colStr(0, "ex_proc_user");
         dddwList("dddw_rskid_desc2", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type='CB-USER'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_date1")) &&
         empty(wp.itemStr("ex_date2")) &&
         empty(wp.itemStr("ex_proc_user"))) {
      alertErr("條件不可全部空白 !");
      return;
   }

   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("Assign Date 起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_date1"), "assign_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "assign_date", "<=")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user", "like%");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " idno ,"
         + " assign_date ,"
         + " proc_user ,"
         + " card_no ,"
         + " case_no ,"
         + "uf_ctfc_idname(card_no) as db_id_name "
   ;
   wp.daoTable = "rsk_ctfc_mast";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   wp.whereOrder = " order by proc_user Asc, assign_date Asc ";
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
   wp.reportId = "rskr3410";
   wp.pageRows = 9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   String tt = "";
   tt = "分派日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_date2"))
   ;
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3410.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
