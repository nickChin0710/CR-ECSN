package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3210 extends BaseQuery {

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
   dddwSelect();
   initButton();

}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_send_date1")) || empty(wp.itemStr("ex_send_date2"))) {
      alertErr("郵寄日期不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_send_date1"), wp.itemStr("ex_send_date2")) == false) {
      alertErr("郵寄日期起迄：輸入錯誤");
      return;
   }


   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_send_date1"), "send_date", ">=")
         + sqlCol(wp.itemStr("ex_send_date2"), "send_date", "<=")
         + sqlCol(wp.itemStr("ex_chi_name"), "chi_name", "like%")
         + sqlCol(wp.itemStr("ex_remark"), "proc_remark", "like%")
   ;
   if (empty(wp.itemStr("ex_zip_code")) == false) {
	  wp.whereStr += sqlCol(wp.itemStr("ex_zip_code"),"zip_code","like%");      
   }
   else {
      wp.whereStr += sqlCol(wp.itemStr("ex_addr1"), "addr1")
            + sqlCol(wp.itemStr("ex_addr2"), "addr2");
   }


   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no ,"
         + " find_type ,"
         + " send_date ,"
         + " chi_name ,"
         + " id_no ,"
         + " zip_code ,"
         + " addr1||addr2||addr3||addr4||addr5 as db_addr "
   ;
   wp.daoTable = "rsk_ctfg_mail_reg";
   wp.whereOrder = " order by send_date ";

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

}
