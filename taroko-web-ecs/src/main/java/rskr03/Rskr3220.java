package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3220 extends BaseQuery {

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
   if (empty(wp.itemStr("ex_warn_date1")) &&
         empty(wp.itemStr("ex_warn_date2")) &&
         empty(wp.itemStr("ex_rels_date1")) &&
         empty(wp.itemStr("ex_rels_date2"))
   ) {
      alertErr("管制/解控日期 不可全部空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_warn_date1"), wp.itemStr("ex_warn_date2")) == false) {
      alertErr("管制日期起迄：輸入錯誤");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_rels_date1"), wp.itemStr("ex_rels_date2")) == false) {
      alertErr("解控日期起迄：輸入錯誤");
      return;
   }

   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_warn_date1"), "warn_date", ">=")
         + sqlCol(wp.itemStr("ex_warn_date2"), "warn_date", "<=")
         + sqlCol(wp.itemStr("ex_rels_date1"), "rels_date", ">=")
         + sqlCol(wp.itemStr("ex_rels_date2"), "rels_date", "<=")
         + sqlCol(wp.itemStr("ex_warn_user"), "warn_user")
         + sqlCol(wp.itemStr("ex_rels_user"), "rels_user")
   ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " find_type , "
         + " warn_date ,  "
         + " warn_time ,  "
         + " warn_user ,  "
         + " rels_code ,  "
         + " rels_date ,  "
         + " rels_time ,  "
         + " rels_user   "
   ;
   wp.daoTable = "rsk_ctfg_mast";
   wp.whereOrder = " order by warn_date Asc , warn_time Asc  ";

   pageQuery();

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
