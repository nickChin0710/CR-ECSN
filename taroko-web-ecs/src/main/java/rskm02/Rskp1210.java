/*
 * 2020-0204  V1.00.01  Alex  ajax fix
 *
 */
package rskm02;

import ecsfunc.EcsCallbatch;
import ofcapp.BaseAction;

public class Rskp1210 extends BaseAction {

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
         + sqlCol(wp.itemStr("ex_group_date"), "group_date1", ">=");

   if (wp.itemEq("ex_group_flag", "Y")) {
      ls_where += " and group_proc_date1 <> '' ";
   }
   else if (wp.itemEq("ex_group_flag", "N")) {
      ls_where += " and nvl(group_proc_date1,'')='' ";
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
         + " group_date1,"
         + " group_proc_date1,"
         + " group_proc_date2,"
         + " 0 as db_cnt_group,"
         + " 0 as db_cnt_list,"
         + " hex(rowid) as rowid,"
         + " mod_seqno"

   ;
   wp.daoTable = "rsk_trcorp_mast ";
   wp.whereOrder = " order by data_yymm Desc ";
   pageQuery();
   liskWkdata();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();

}

void liskWkdata() throws Exception  {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (empty(wp.colStr(ii, "group_proc_date1"))) {
         continue;
      }
      count_db_cnt_list(wp.colStr(ii, "data_yymm"));
      wp.colSet(ii, "db_cnt_list", sqlStr("ll_cnt1"));
      count_db_cnt_group(wp.colStr(ii, "batch_no"));
      wp.colSet(ii, "db_cnt_group", sqlStr("ll_cnt2"));
   }
}

void count_db_cnt_list(String ls_data_yymm) throws Exception  {
   String sql1 = "select count(*) as ll_cnt1 "
         + " from rsk_trcorp_bank_stat"
         + " where data_yymm =?";
   sqlSelect(sql1, new Object[]{
         ls_data_yymm
   });
}

void count_db_cnt_group(String ls_batch_no) throws Exception  {
   String sql1 = "select sum(decode(nvl(risk_group1,'Z9'),'Z9',0,1)) as ll_cnt2 "
         + " from rsk_trcorp_list"
         + " where batch_no =?";
   sqlSelect(sql1, new Object[]{
         ls_batch_no
   });
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

   Rskp1210Func func = new Rskp1210Func();
   func.setConn(wp);

   ecsfunc.EcsCallbatch oocall = new EcsCallbatch(wp);
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("batch_no");
//
//		for (int ii = 0; ii < opt.length; ii++) {
   int rr = optToIndex(opt[0]);
   if (rr < 0) {
      alertErr("未點選欲處理資料");
      return;
   }

   String ls_batch_no = wp.itemStr(rr, "batch_no");
   func.varsSet("batch_no", ls_batch_no);

   optOkflag(rr);
   rc = func.dataProc();
   if (rc != 1) {
      il_err++;
      wp.colSet(rr, "ok_flag", "X");
      alertErr(func.getMsg());
      return;
   }

   //sql_commit(rc);
   //--
   rc = oocall.callBatch("RskB105 " + ls_batch_no);
   if (rc == 1) {
      wp.colSet(rr, "ok_flag", "V");
      il_ok++;
//				continue;
   }
   alertMsg(oocall.getMesg());
//		}

//		alert_msg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);


}

public void ajaxConfirm() throws Exception {

   String ls_batch_no = wp.itemStr("ax_batch_no");
   String ls_proc_date = wp.itemStr("ax_proc_date");
   wp.jsonCode = "Y";

   if (empty(ls_proc_date) == false) {
      wp.addJSON("conf_flag", "Y1");
      wp.log("conf_flag=Y1");
   }
   else {
      wp.addJSON("conf_flag", "Y2");
      wp.log("conf_flag=Y2");
   }
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
