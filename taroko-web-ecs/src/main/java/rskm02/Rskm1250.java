package rskm02;
/**
 * 2023-1030   JH    bugfix
 * 2020-0206:  Alex  queryAfter fix
 * 2019-1206:  Alex  add initButton
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 */

import ofcapp.BaseAction;

public class Rskm1250 extends BaseAction {

   String kk1 = "", kk2 = "";

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
      // -資料處理-
      procFuncCancel();
   }
   else if (eqIgno(wp.buttonCode, "C3")) {
      // -全部原額用卡結案-
      procFuncAll();
   }
   else if (eqIgno(wp.buttonCode, "C4")) {
      // -全部取消原額用卡結案-
      procFuncCancelAll();
   }
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm1250")) {
         wp.optionKey = wp.colStr("ex_action_code");
         ddlbList("dddw_action_code", wp.colStr("ex_action_code"), "ecsfunc.DeCodeRsk.trialAction");
      }

   }
   catch (Exception ex) {
   }

   try {
      if (eqIgno(wp.respHtml, "rskm1250_detl")) {
         wp.optionKey = wp.colStr("action_code");
         ddlbList("dddw_action_code", wp.colStr("action_code"), "ecsfunc.DeCodeRsk.trialAction");
      }

   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
/*		
		if(empty(wp.itemStr("ex_batch_no"))&&
			empty(wp.itemStr("ex_risk_group1"))&&
			empty(wp.itemStr("ex_risk_group2"))&&
			empty(wp.itemStr("ex_corp_no"))){
			errmsg("請指定查詢條件");
			return;
		}
*/
   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_risk_group1"), "risk_group1")
         + sqlCol(wp.itemStr("ex_risk_group2"), "risk_group2")
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
         + sqlCol(wp.itemStr("ex_corp_no"), "corp_no", "like%")
         + sqlCol(wp.itemStr("ex_action_code"), "action_code");
   
   setSqlParmNoClear(true);
   sumTotal(lsWhere);

   if (wp.itemEq("ex_apr_flag", "1")) {
      lsWhere += " and close_date ='' ";
   }
   else if (wp.itemEq("ex_apr_flag", "N")) {
      lsWhere += " and close_date<>'' and close_apr_date='' ";
   }
   else if (wp.itemEq("ex_apr_flag", "Y")) {
      lsWhere += " and close_apr_date<>''";
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

void sumTotal(String lsWhere) throws Exception  {
   if (wp.itemEmpty("ex_batch_no")) return;

   //--原額用卡結案
   String sql1 = " select count(*) as db_cnt1 from rsk_trcorp_list " + lsWhere + " and action_code ='0' and close_apr_date = '' ";
   sqlSelect(sql1);
   wp.colSet("tl_action_code0", sqlStr("db_cnt1"));
   
   setSqlParmNoClear(true);
   //--未結案
   String sql2 = " select count(*) as db_cnt2 from rsk_trcorp_list " + lsWhere + " and action_code = '' and close_date = '' ";
   sqlSelect(sql2);
   wp.colSet("tl_action_code_empty", sqlStr("db_cnt2"));

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "batch_no,"
         + " corp_no,"
         + " corp_no_code,"
         + " risk_group2,"
         + " trial_remark||trial_remark2||trial_remark3 as trial_remark,"
         + " substr(trial_remark||trial_remark2||trial_remark3,1,30) as trial_remark_30 , "
         + " close_date,"
         + " close_user,"
         + " close_apr_date,"
         + " corp_p_seqno,"
         + " action_code,"
         + " risk_group1,"
         + " hex(rowid) as rowid ,"
         + " mod_seqno,"
         + " uf_corp_name(corp_p_seqno) as corp_cname"
   ;
   wp.daoTable = "rsk_trcorp_list ";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   wp.whereOrder = " order by batch_no Asc, corp_no Asc  ";
   pageQuery();


   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   queryAfter();
   wp.setListCount(1);
   wp.setPageValue();
}

void queryAfter() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {

      if (wp.colEq(ii, "action_code", "0")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".原額用卡");
      }
      else if (wp.colEq(ii, "action_code", "1")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-未降足額度者凍結");
      }
      else if (wp.colEq(ii, "action_code", "2")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-未降足額度者維護特指");
      }
      else if (wp.colEq(ii, "action_code", "3")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調整額度");
      }
      else if (wp.colEq(ii, "action_code", "4")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-卡戶凍結(個繳)");
      }
      else if (wp.colEq(ii, "action_code", "5")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-維護特指");
      }
      else if (wp.colEq(ii, "action_code", "6")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".卡戶凍結[4]");
      }
      else if (wp.colEq(ii, "action_code", "7")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".卡片維護特指");
      }
      else if (wp.colEq(ii, "action_code", "8")) {
         wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".額度內用卡");
      }
      else if (wp.colEmpty(ii, "action_code")) {
         wp.colSet(ii, "tt_action_code", "");
      }
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   dataRead();

}

@Override
public void dataRead() throws Exception {
   if (empty(kk1))
      kk1 = wp.itemStr("batch_no");
   if (empty(kk2))
      kk2 = wp.itemStr("corp_no");

   wp.selectSQL = "batch_no,"
         + " risk_group1,"
         + " risk_group2,"
         + " corp_no,"
         + " uf_corp_name(corp_p_seqno) as corp_cname,"
         + " action_code,"
         + " close_date,"
         + " close_apr_date,"
         + " trial_remark,"
         + " trial_remark2,"
         + " trial_remark3,"
         + " corp_no_code,"
         + " corp_p_seqno,"
         + " close_user,"
         + " to_char(mod_time,'yyyymmdd') as mod_date,"
         + " mod_user,"
         + " hex(rowid) as rowid,mod_seqno";
   wp.daoTable = "rsk_trcorp_list";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "batch_no")
         + sqlCol(kk2, "corp_no")
   ;
   this.logSql();
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }

}

@Override
public void saveFunc() throws Exception {
   Rskm1250Func func = new Rskm1250Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   this.sqlCommit(rc);
   this.saveAfter(false);

}

@Override
public void procFunc() throws Exception {
   int ilOk = 0;
   int ilErr = 0;

   Rskm1250Func func = new Rskm1250Func();
   func.setConn(wp);

   String[] lsRowid = wp.itemBuff("rowid");
   String[] liModSeqno = wp.itemBuff("mod_seqno");
   String[] lsCad = wp.itemBuff("close_apr_date");
   String[] opt = wp.itemBuff("opt");
   if (!empty(lsRowid[0])) {
      wp.listCount[0] = lsRowid.length;
   }
   else {
      wp.listCount[0] =0;
      alertErr("無資料可處理");
      return;
   }
   if (optToIndex(opt[0]) <0) {
      alertErr("未點選欲處理資料");
      return;
   }

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
//      rr = (int) this.toNum(opt[ii]) - 1;
//      rr = (int) (this.toNum(opt[ii]) - 1);
      rr =optToIndex(opt[ii]);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "-");
      func.varsSet("mod_seqno", liModSeqno[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("close_apr_date", lsCad[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
   }

   alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

}

public void procFuncCancel() throws Exception {
   int ilOk = 0;
   int ilErr = 0;

   Rskm1250Func func = new Rskm1250Func();
   func.setConn(wp);

   String[] lsRowid = wp.itemBuff("rowid");
   String[] liModSeqno = wp.itemBuff("mod_seqno");
   String[] lsCad = wp.itemBuff("close_apr_date");
   String[] opt = wp.itemBuff("opt");
   if (!empty(lsRowid[0])) {
      wp.listCount[0] = lsRowid.length;
   }
   else {
      wp.listCount[0] =0;
      alertErr("無資料可處理");
      return;
   }
   if (optToIndex(opt[0]) <0) {
      alertErr("未點選欲處理資料");
      return;
   }

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
//      rr = (int) this.toNum(opt[ii]) - 1;
      rr =optToIndex(opt[ii]);
      wp.log("" + ii + "-ON." + lsRowid[rr]);
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "-");
      func.varsSet("mod_seqno", liModSeqno[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("close_apr_date", lsCad[rr]);

      rc = func.dataProc2();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
   }

   alertMsg("取消原額用卡結案: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

}


@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
   }
   if (eqIgno(wp.respHtml, "rskm1250")) {
      wp.colSet("button_proc_off", "disabled");
      btnModeAud("XX");
      if (wp.autUpdate()==true && wp.itemEmpty("ex_batch_no") == false) {
         wp.colSet("button_proc_off", "");
      }
   }

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

void procFuncAll() throws Exception  {
//	wp.listCount[0] = wp.item_rows("rowid");
   setSelectLimit(9999);
   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_risk_group1"), "risk_group1")
         + sqlCol(wp.itemStr("ex_risk_group2"), "risk_group2")
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
         + sqlCol(wp.itemStr("ex_corp_no"), "corp_no", "like%")
         + sqlCol(wp.itemStr("ex_action_code"), "action_code");

   String sql1 = " select hex(rowid) as rowid , mod_seqno , close_apr_date from rsk_trcorp_list " + lsWhere + " and action_code ='' and close_date = '' ";
   sqlSelect(sql1);

   int ilSelectCnt = 0;
   ilSelectCnt = sqlRowNum;

   if (ilSelectCnt == 0) {
      alertErr("無資料可執行全部原額用卡結案");
      return;
   }

   Rskm1250Func func = new Rskm1250Func();
   func.setConn(wp);

   for (int ii = 0; ii <= ilSelectCnt; ii++) {
      func.varsSet("mod_seqno", sqlStr(ii, "mod_seqno"));
      func.varsSet("rowid", sqlStr(ii, "rowid"));
      func.varsSet("close_apr_date", sqlStr(ii, "close_apr_date"));

      rc = func.dataProc();
      sqlCommit(rc);
   }

   okAlert("全部原額用卡結案處理完畢,請重新查詢資料");
}

void procFuncCancelAll() throws Exception  {
//	wp.listCount[0] = wp.item_rows("rowid");
   setSelectLimit(9999);
   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_risk_group1"), "risk_group1")
         + sqlCol(wp.itemStr("ex_risk_group2"), "risk_group2")
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
         + sqlCol(wp.itemStr("ex_corp_no"), "corp_no", "like%")
         + sqlCol(wp.itemStr("ex_action_code"), "action_code");

   String sql1 = " select hex(rowid) as rowid , mod_seqno from rsk_trcorp_list " + lsWhere + " and action_code ='0' and close_apr_date = '' ";
   sqlSelect(sql1);

   int ilSelectCnt = 0;
   ilSelectCnt = sqlRowNum;

   if (ilSelectCnt == 0) {
      alertErr("無資料可執行全部取消原額用卡結案");
      return;
   }

   Rskm1250Func func = new Rskm1250Func();
   func.setConn(wp);

   for (int ii = 0; ii <= ilSelectCnt; ii++) {
      func.varsSet("mod_seqno", sqlStr(ii, "mod_seqno"));
      func.varsSet("rowid", sqlStr(ii, "rowid"));
      func.varsSet("close_apr_date", sqlStr(ii, "close_apr_date"));

      rc = func.dataProc2();
      sqlCommit(rc);
   }

   okAlert("全部取消原額用卡結案處理完畢,請重新查詢資料");
}

}
