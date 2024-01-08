package rskr03;

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3190 extends BaseQuery {

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
      if (wp.respHtml.indexOf("3190") > 0) {
         wp.optionKey = wp.colStr("ex_proc_type");
         dddwList("dddw_proc_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_TYPE'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("3190") > 0) {
         wp.optionKey = wp.colStr("ex_tel_type");
         dddwList("dddw_tel_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_TEL_TYPE'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_proc_date1")) || empty(wp.itemStr("ex_proc_date2"))) {
      alertErr("處理日期不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_proc_date1"), wp.itemStr("ex_proc_date2")) == false) {
      alertErr("處理日期起迄：輸入錯誤");
      return;
   }


   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_proc_date1"), "proc_date", ">=")
         + sqlCol(wp.itemStr("ex_proc_date2"), "proc_date", "<=")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user")
         + sqlCol(wp.itemStr("ex_proc_type"), "proc_type")
         + sqlCol(wp.itemStr("ex_tel_type"), "tel_type");
   
   setSqlParmNoClear(true);   
   sumTotCntRead();

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " proc_type ,    "
         + " tel_type,"
         + " proc_user, "
         + " count(*) as db_cnt_A"
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereOrder = " group by proc_type, tel_type, proc_user order by proc_type, proc_user ";

   wp.pageCountSql = " select count(*) from (select distinct proc_type,tel_type,proc_user"
         + " from " + wp.daoTable + wp.whereStr + " )";

   pageQuery();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();

}

void sumTotCntRead() throws Exception {
   String sql1 = "select count(*) as sum_tot_cnt"
         + " from rsk_ctfg_proc"
         + wp.whereStr
//				+" where 1=1"
//				+sqlCol(wp.itemStr("ex_proc_date1"),"proc_date",">=")
//				+sqlCol(wp.itemStr("ex_proc_date2"),"proc_date","<=")
//				+sqlCol(wp.itemStr("ex_proc_user"),"proc_user")
//				+sqlCol(wp.itemStr("ex_proc_type"),"proc_type")
         ;
   this.sqlSelect(sql1);
   wp.colSet("sum_tot_cnt", this.sqlStr("sum_tot_cnt"));
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
