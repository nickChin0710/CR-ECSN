/*
 * 2020-0211  V1.00.01  Alex  fix ajax
 *
 */
package rskm02;

import ecsfunc.EcsCallbatch;
import ofcapp.BaseAction;

public class Rskp1230 extends BaseAction {

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
   else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
   else if (eqIgno(wp.buttonCode, "ajax")) {
      strAction = "AJAX";
      ajaxConfirm();
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%")
         + sqlCol(wp.itemStr("ex_group_date"), "group_date2", ">=");

   if (wp.itemEq("ex_group_flag", "Y")) {
      ls_where += " and group_proc_date2 <> '' ";
   }
   else if (wp.itemEq("ex_group_flag", "N")) {
      ls_where += " and nvl(group_proc_date2,'')='' ";
   }

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "batch_no,"
         + " batch_desc,"
         + " data_yymm,"
         + " group_date2,"
         + " group_proc_date1,"
         + " group_proc_date2,"
         + " 0 as db_cnt_group,"
         + " 0 as db_cnt_list,"
         + " hex(rowid) as rowid,"
         + " mod_seqno"

   ;
   wp.daoTable = "rsk_trcorp_mast ";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   wp.whereOrder = " order by batch_no Desc";
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   listWkdata();
   wp.setPageValue();

}

void listWkdata() throws Exception  {
//      String sql1 ="select count(*) as ll_cnt1 "
//            +" from rsk_trcorp_bank_stat"
//            +" where data_yymm =?";
//      sqlSelect(sql1,new Object[]{
//            ls_data_yymm
//      });

   String sql1 = "select sum(decode(risk_group1,'',0,'Z9',0,1)) as ll_cnt1 " +
         ", sum(decode(risk_group2,'',0,'Z9',0,1)) as ll_cnt2"
         + " from rsk_trcorp_list"
         + " where batch_no =?";

   int ll_nrow = wp.listCount[0];
   for (int ii = 0; ii < ll_nrow; ii++) {
      String ls_batch_no = wp.colStr(ii, "batch_no");
      sqlSelect(sql1, new Object[]{ls_batch_no});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "db_cnt_group1", sqlInt("ll_cnt1"));
         wp.colSet(ii, "db_cnt_group2", sqlInt("ll_cnt2"));
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
   int il_ok = 0;
   int il_err = 0;

   Rskp1230Func func = new Rskp1230Func();
   func.setConn(wp);
   wp.listCount[0] = wp.itemRows("batch_no");

   String opt = wp.itemStr("opt");
   int rr = optToIndex(opt);
   if (rr < 0) {
      alertErr("未點選欲處理資料");
      return;
   }

   String ls_batch_no = wp.itemStr(rr, "batch_no");
   optOkflag(rr);
   func.varsSet("batch_no", ls_batch_no);
   rc = func.dataProc();
   if (rc != 1) {
      optOkflag(rr, -1);
      alertErr(func.getMsg());
      return;
   }

   ecsfunc.EcsCallbatch oo_batch = new EcsCallbatch(wp);
   rc = oo_batch.callBatch("RskB106 " + ls_batch_no);
   optOkflag(rr, rc);
   if (rc == -1) {
      alertErr(oo_batch.getMesg());
      return;
   }

   alertMsg(oo_batch.getMesg());
}

public void ajaxConfirm() throws Exception {

   String ls_batch_no = wp.itemStr("ax_batch_no");
   String ls_proc_date = wp.itemStr("ax_proc_date");

   wp.jsonCode = "Y";

   wp.resetJSON();
   if (close_date(ls_batch_no)) {
      wp.addJSON("conf_flag", "Y3");
   }
   if (empty(ls_proc_date) == false) {
      wp.addJSON("conf_flag", "Y1");
   }
   else {
      wp.addJSON("conf_flag", "Y2");
   }
}

boolean close_date(String ls_batch_no) throws Exception  {
   String sql1 = "select count(*) as ll_cnt "
         + " from rsk_trcorp_list"
         + " where batch_no =?"
         + " and close_date <> ''";
   sqlSelect(sql1, new Object[]{
         ls_batch_no
   });
   return sqlNum("ll_cnt") > 0;
}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
