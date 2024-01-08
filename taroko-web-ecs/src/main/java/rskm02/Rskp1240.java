package rskm02;
/**
 * 商務卡覆審 JCIC查詢及統計作業
 * 2019-1002   JH    call RskB107
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 * V.2018-0820.jh
 */

import ofcapp.BaseAction;

public class Rskp1240 extends BaseAction {

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
   else if (eqIgno(wp.buttonCode, "C2")) {
      //-JCIC媒體檔-
      procFunc2();
   }
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
//			xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "C4")) {
      // -資料處理-
      procFunc4();
   }
   else if (eqIgno(wp.buttonCode, "CB")) {
      // -資料處理-
      proc4CallBatch();
   }
}

@Override
public void dddwSelect() {
//		try {
//   		wp.optionKey = wp.colStr("ex_jcic_no");
//   		dddw_list("dddw_jcic_no","col_jcic_query_mast"
//   				,"jcic_no","contract_desc","where 1=1");
//		}
//		catch(Exception ex) {}

}

@Override
public void queryFunc() throws Exception {
   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%");

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
         + " batch_no ,"
         + " batch_desc ,"
         + " group_date1 ,"
         + " jcic_query_date ,"
         + " jcic_proc_date ,"
         + " 0 as db_corp_cnt ,"
         + " 0 as db_idno_cnt ,"
         + " '' as db_jcic_resp_date ";
   wp.daoTable = " rsk_trcorp_mast ";
   wp.whereOrder = " order by group_date1 Desc ";
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(1);
   wp.setPageValue();
}

void queryAfter() throws Exception {
   String sql1 = " select "
         + " sum(decode(id_type,'1',1,0)) as db_idno_cnt , "
         + " sum(decode(id_type,'2',1,0)) as db_corp_cnt , "
         + " min(send_date) as ls_send_date , "
         + " max(resp_date) as ls_resp_date "
         + " from col_jcic_query_req "
         + " where batch_no = ? ";
   String sql2 = "select count(*) as jcic_cnt"
         + " from col_jcic_query_req"
         + " where batch_no =?";

   for (int ii = 0; ii < wp.selectCnt; ii++) {
      String ls_batch_no = wp.colStr(ii, "batch_no");
      sqlSelect(sql1, new Object[]{ls_batch_no});

      wp.colSet(ii, "db_corp_cnt", sqlStr("db_corp_cnt"));
      wp.colSet(ii, "db_idno_cnt", sqlStr("db_idno_cnt"));
      wp.colSet(ii, "db_jcic_resp_date", sqlStr("ls_resp_date"));

      if (empty(wp.colStr(ii, "jcic_query_date")) && !empty(sqlStr("ls_send_date"))) {
         wp.colSet(ii, "jcic_query_date", sqlStr("ls_send_date"));
         updateMast(wp.colStr(ii, "batch_no"), sqlStr("ls_send_date"));
      }

      setString(1, ls_batch_no);
      sqlSelect(sql2);
      if (sqlRowNum > 0) {
         wp.colSet(ii, "db_jcic_cnt", sqlStr("jcic_cnt"));
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
   wp.listCount[0] = wp.itemRows("batch_no");
   int rr = optToIndex(wp.itemStr("opt"));
   if (rr < 0) {
      alertErr("請點選欲處理資料");
      return;
   }
   String ls_batch_no = wp.itemStr(rr, "batch_no");
   if (empty(ls_batch_no)) {
      alertErr("覆審批號: 不可空白");
      return;
   }

   Rskp1240Func func = new Rskp1240Func();
   func.setConn(wp);

   func.varsSet("batch_no", ls_batch_no);
   optOkflag(rr);
   rc = func.dbInsert();
   optOkflag(rr, rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }
   //-Call-Batch: rskB104--------------------
   ecsfunc.EcsCallbatch oo_call = new ecsfunc.EcsCallbatch(wp);
   rc = oo_call.callBatch("RskB104 " + ls_batch_no);
   if (rc != 1) {
      alertErr("callBatch error; " + oo_call.getMesg());
   }
   else {
      alertMsg("callBatch OK; Batch-seqno=" + oo_call.batchSeqno());
   }

//		this.ok_msg(zzstr.format("資料匯入處理完成, 成功筆數=%s; 失敗筆數=%s"
//				,func.varsStr("ok_cnt"),func.varsStr("err_cnt")));
}

public void procFunc4() throws Exception {
   Rskp1240Func func = new Rskp1240Func();
   func.setConn(wp);
   int ll_ok = 0, ll_err = 0;
   String[] aa_batch_no = wp.itemBuff("batch_no");
   wp.listCount[0] = aa_batch_no.length;
   int rr = optToIndex(wp.itemStr("opt"));
   if (rr < 0) {
      errmsg("請點選欲處理資料");
      return;
   }
   this.optNumKeep(aa_batch_no.length);

   String ls_batch_no = aa_batch_no[rr];

   String sql1 = " select count(*) as db_cnt "
         + " from col_jcic_query_req "
         + " where batch_no = ? "
         + " and nvl(resp_date,'') <> '' ";
   sqlSelect(sql1, new Object[]{ls_batch_no});
   if (sqlRowNum > 0 && sqlNum("db_cnt") <= 0) {
      errmsg("查詢媒體檔, JCIC 未回覆");
      return;
   }

   func.varsSet("batch_no", ls_batch_no);
   if (func.updateLogProc4() > 0) {
      ll_ok++;
   }
   else {
      ll_err++;
      this.dbRollback();
   }
   if (ll_ok > 0) {
      sqlCommit(1);
   }
   else {
      return;
   }

   wp.colSet("proc4_mesg", "|| 1==1");
}

void proc4CallBatch() throws Exception {
   String[] aa_batch_no = wp.itemBuff("batch_no");
   wp.listCount[0] = aa_batch_no.length;
   int rr = optToIndex(wp.itemStr("opt"));
   if (rr < 0) {
      errmsg("請點選欲處理資料");
      return;
   }
   this.optNumKeep(aa_batch_no.length);

   String ls_batch_no = aa_batch_no[rr];

   ecsfunc.EcsCallbatch oo_call = new ecsfunc.EcsCallbatch(wp);
   rc = oo_call.callBatch("RskB103 " + ls_batch_no);
   if (rc != 1) {
      alertErr("callBatch error; " + oo_call.getMesg());
   }
   else {
      alertMsg("callBatch OK; Batch-seqno=" + oo_call.batchSeqno());
   }
}

void procFunc2() throws Exception {
   wp.listCount[0] = wp.itemRows("batch_no");
   int rr = optToIndex(wp.itemStr("opt"));
   if (rr < 0) {
      errmsg("請點選欲處理資料");
      return;
   }
   this.optNumKeep(wp.listCount[0]);

   String ls_batch_no = wp.itemStr(rr, "batch_no");

   ecsfunc.EcsCallbatch oo_call = new ecsfunc.EcsCallbatch(wp);
   rc = oo_call.callBatch("RskB107 " + ls_batch_no + " " + userDeptNo());
   if (rc != 1) {
      alertErr("callBatch error; " + oo_call.getMesg());
   }
   else {
      alertMsg("callBatch OK; Batch-seqno=" + oo_call.batchSeqno());
   }
}

@Override
public void initButton() {


}

@Override
public void initPage() {
   // TODO Auto-generated method stub
}

int updateMast(String ls_batch_no, String ls_send_date) throws Exception {
   msgOK();
   String sql1 = " update rsk_trcorp_mast set "
         + " jcic_query_date =:jcic_query_date "
         + " where batch_no =:batch_no ";

   setString("jcic_query_date", ls_send_date);
   setString("batch_no", ls_batch_no);
   sqlExec(sql1);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_trcorp_mast error ");
   }
   return rc;
}

}
