package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3170 extends BaseQuery {

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
      if (wp.respHtml.indexOf("3170") > 0) {
         wp.optionKey = wp.colStr("ex_proc_type");
         dddwList("dddw_proc_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_TYPE'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_date1")) || empty(wp.itemStr("ex_date2"))) {
      alertErr("處理日期不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("處理日期起迄：輸入錯誤");
      return;
   }


   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_date1"), "proc_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "proc_date", "<=")
         + sqlCol(wp.itemStr("ex_user_id"), "proc_user")
         + sqlCol(wp.itemStr("ex_proc_type"), "proc_type")
   ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " proc_type ,    "
         + " proc_user, count(*) as db_cnt "
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereOrder = " group by proc_type, proc_user order by proc_type, proc_user ";
   wp.pageCountSql = " select count(*) from (select distinct proc_type , proc_user from rsk_ctfg_proc " + wp.whereStr + ")";

   pageQuery();
   //list_wkdata();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(1);
   sumTotCntRead();
   wp.setPageValue();

}
	
/*	void db_cntRead() throws Exception{
		wp.selectSQL = ""
				+ " count(*) db_cnt"				
		      ;
		wp.daoTable = "rsk_ctfg_proc";
		wp.whereStr="where 1=1"
		+sqlCol(wp.itemStr("ex_date1"),"proc_date",">=")
		+sqlCol(wp.itemStr("ex_date2"),"proc_date","<=")
		+sqlCol(wp.itemStr("ex_user_id"),"proc_user")
		+sqlCol(wp.itemStr("ex_proc_type"),"proc_type");
		pageQuery();
	}*/

void sumTotCntRead() throws Exception {
   wp.selectSQL = ""
         + " count(*) sum_tot_cnt"
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereStr = "where 1=1"
         + sqlCol(wp.itemStr("ex_date1"), "proc_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "proc_date", "<=")
         + sqlCol(wp.itemStr("ex_user_id"), "proc_user")
         + sqlCol(wp.itemStr("ex_proc_type"), "proc_type");
   pageSelect();
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
