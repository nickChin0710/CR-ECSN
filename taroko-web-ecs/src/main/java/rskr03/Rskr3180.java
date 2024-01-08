package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3180 extends BaseQuery {
String isWhere = "";

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
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("3180") > 0) {
         wp.optionKey = wp.colStr("ex_proc_stas");
         dddwList("dddw_proc_stas", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_STATUS'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if ((empty(wp.itemStr("ex_proc_date1")) ||
         empty(wp.itemStr("ex_proc_date2"))
   ) && (empty(wp.itemStr("ex_view_date1")) ||
         empty(wp.itemStr("ex_view_date2")))) {
      alertErr("處理日期 or 檢視日期 不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_proc_date1"), wp.itemStr("ex_proc_date2")) == false) {
      alertErr("處理日期起迄：輸入錯誤");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_view_date1"), wp.itemStr("ex_view_date2")) == false) {
      alertErr("檢視日期起迄：輸入錯誤");
      return;
   }

   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_proc_date1"), "proc_date", ">=")
         + sqlCol(wp.itemStr("ex_proc_date2"), "proc_date", "<=")
         + sqlCol(wp.itemStr("ex_view_date1"), "view_date", ">=")
         + sqlCol(wp.itemStr("ex_view_date2"), "view_date", "<=")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user")
         + sqlCol(wp.itemStr("ex_proc_stas"), "proc_status")
   ;
   if (wp.itemEq("ex_close_flag", "Y")) {
      wp.whereStr += " and nvl(close_flag,'0')='1' ";
   }
   else if (wp.itemEq("ex_close_flag", "N")) {
      wp.whereStr += " and nvl(close_flag,'0')<>'1' ";
   }
   String in_card_no = "";

   if (!wp.itemEmpty("ex_card_no1")) {
      if (empty(in_card_no)) in_card_no = " and substr(card_no,1,6) in ('" + wp.itemStr("ex_card_no1") + "'";
      else in_card_no += ",'" + wp.itemStr("ex_card_no1") + "'";
   }

   if (!wp.itemEmpty("ex_card_no2")) {
      if (empty(in_card_no)) in_card_no = " and substr(card_no,1,6) in ('" + wp.itemStr("ex_card_no2") + "'";
      else in_card_no += ",'" + wp.itemStr("ex_card_no2") + "'";
   }

   if (!wp.itemEmpty("ex_card_no3")) {
      if (empty(in_card_no)) in_card_no = " and substr(card_no,1,6) in ('" + wp.itemStr("ex_card_no3") + "'";
      else in_card_no += ",'" + wp.itemStr("ex_card_no3") + "'";
   }

   if (!wp.itemEmpty("ex_card_no4")) {
      if (empty(in_card_no)) in_card_no = " and substr(card_no,1,6) in ('" + wp.itemStr("ex_card_no4") + "'";
      else in_card_no += ",'" + wp.itemStr("ex_card_no4") + "'";
   }

   if (!wp.itemEmpty("ex_card_no5")) {
      if (empty(in_card_no)) in_card_no = " and substr(card_no,1,6) in ('" + wp.itemStr("ex_card_no5") + "'";
      else in_card_no += ",'" + wp.itemStr("ex_card_no5") + "'";
   }

   if (!empty(in_card_no)) in_card_no += ")";
   wp.whereStr += in_card_no;

   wp.whereStr += " and proc_type='主動服務' ";
   
   isWhere = wp.whereStr;
   
   setSqlParmNoClear(true);
   sumData();


   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " proc_user ,    "
         + " proc_status, "
         + " count(*) as db_cnt,"
         + "sum(rejt_amt) as db_rejt_amt,"
         + "sum(ok_cnt) as db_ok_cnt,"
         + "sum(ok_amt) as db_ok_amt,"
         + "sum(sign(nvl(ok_amt,0))) as db_card_cnt "
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereOrder = " group by proc_user,proc_status order by proc_user,proc_status ";

   wp.pageCountSql = " select count(*) from (select distinct proc_user , proc_status from rsk_ctfg_proc " + wp.whereStr + wp.whereOrder + ")";

   pageQuery();
   //list_wkdata();

   if (sqlRowNum <= 0) {
      wp.colSet("obj_603480977", "0");
      wp.colSet("obj_603480978", "0");
      wp.colSet("obj_603480979", "0");
      wp.colSet("obj_603480980", "0");
      wp.colSet("obj_603480981", "0");
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();

}

void sumData() throws Exception  {
   String sql1 = " select "
         + " count(*) as obj_603480977 , "
         + " sum(rejt_amt) as obj_603480978 , "
         + " sum(ok_cnt) as obj_603480979 , "
         + " sum(sign(nvl(ok_amt,0))) as obj_603480980 , "
         + " sum(ok_amt) as obj_603480981 "
         + " from rsk_ctfg_proc "
         + isWhere;

   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      this.selectOK();
      return;
   }

   wp.colSet("obj_603480977", sqlNum("obj_603480977"));
   wp.colSet("obj_603480978", sqlNum("obj_603480978"));
   wp.colSet("obj_603480979", sqlNum("obj_603480979"));
   wp.colSet("obj_603480980", sqlNum("obj_603480980"));
   wp.colSet("obj_603480981", sqlNum("obj_603480981"));

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
