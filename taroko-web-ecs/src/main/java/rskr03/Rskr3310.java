package rskr03;
/**
 * 2020-0203:  Alex  cond check fix
 * 2020-0108:  Alex  cond check fix
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;

public class Rskr3310 extends BaseAction implements InfacePdf {

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
   if (chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr("立案日期: 起迄錯誤");
      return;
   }

   if (chkStrend(wp.itemStr("ex_call_date1"), wp.itemStr("ex_call_date2")) == false) {
      alertErr("敘實日期: 起迄錯誤");
      return;
   }

   if (!wp.itemEmpty("ex_call_desc") && wp.itemEmpty("ex_call_date1") && wp.itemEmpty("ex_call_date2")) {
      alertErr("連絡內容 有值 : 敘實日期不可空白 ");
      return;
   }
   //--有輸入卡號時不check日期是否全部空白
   if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_case_date1") && wp.itemEmpty("ex_case_date2") && wp.itemEmpty("ex_call_date1") && wp.itemEmpty("ex_call_date2")) {
      alertErr("立案/敘實日期 : 不可全部空白");
      return;
   }

   String lsWhere = " where 1=1 "
		 + sqlCol(wp.itemStr("ex_case_date1"),"B.case_date",">=")
		 + sqlCol(wp.itemStr("ex_case_date2"),"B.case_date","<=")         
         + sqlCol(wp.itemStr("ex_case_user"), "B.case_user")
         + sqlCol(wp.itemStr("ex_call_date1"),"A.call_date",">=")
         + sqlCol(wp.itemStr("ex_call_date2"),"A.call_date","<=")         
         + sqlCol(wp.itemStr("ex_survey_user"), "B.survey_user")
         + sqlCol(wp.itemStr("ex_call_desc"), "A.call_desc", "%like%")
         + sqlCol(wp.itemStr("ex_card_no"), "A.card_no", "like%");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = " A.card_no ,"
         + " A.call_date ,"
         + " A.call_time ,"
         + " A.tel_no ,"
         + " A.tel_no2 ,"
         + " A.call_man ,"
         + " A.call_desc ,"
         + " A.call_desc02 ,"
         + " A.call_desc03 ,"
         + " A.call_telno ,"
         + " B.case_seqno ,"
         + " uf_idno_id(B.id_p_seqno) as id_no ,"
         + " rpad(uf_idno_name(B.card_no),20) as chi_name , "
         + " decode((select usr_cname from sec_user where usr_id =A.mod_user),'',A.mod_user,(select usr_cname from sec_user where usr_id =A.mod_user)) as tt_mod_user "
   ;
   wp.daoTable = " rsk_ctfi_call_log A join rsk_ctfi_case B on A.card_no=B.card_no ";
   wp.whereOrder = " order by A.card_no, A.call_date, A.call_time ";

   pageQuery();

   if (sqlNotFind()) {
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
   // TODO Auto-generated method stub

}

}
