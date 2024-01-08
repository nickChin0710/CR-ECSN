package rskm02;
/**
 * 2020-0207:  Alex  cancel count cnt
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;

public class Rskp1250 extends BaseAction {

@Override
public void userAction() throws Exception {
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
   } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
   } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
   } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   } else if (eqIgno(wp.buttonCode, "C")) {
      // -覆核-
      procFunc();
   } else if (eqIgno(wp.buttonCode, "NC")) {
      // -取消覆核-
      OffprocFunc();
   } else if (eqIgno(wp.buttonCode, "C3")) {
      // -覆核-
      procFuncAll();
   } else if (eqIgno(wp.buttonCode, "C4")) {
      // -覆核-
      procCancelAll();
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_close_date1")) &&
         empty(wp.itemStr("ex_close_date2")) &&
         empty(wp.itemStr("ex_batch_no")) &&
         empty(wp.itemStr("ex_risk_group1")) &&
         empty(wp.itemStr("ex_risk_group2")) &&
         empty(wp.itemStr("ex_corp_no"))
   ) {
      alertErr("條件不可全部空白");
      return;
   }

   if (wp.itemEq("ex_apr_flag", "2")) {
      if (empty(wp.itemStr("ex_close_date1")) || empty(wp.itemStr("ex_close_date2"))) {
         alertErr("覆審日期[起迄] 不可空白");
         return;
      }
   }


   if (this.chkStrend(wp.itemStr("ex_close_date1"), wp.itemStr("ex_close_date2")) == false) {
      alertErr("覆審日期起迄：輸入錯誤");
      return;
   }

   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_corp_no"), "corp_no", "like%")
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
         + sqlCol(wp.itemStr("ex_risk_group1"), "risk_group1")
         + sqlCol(wp.itemStr("ex_risk_group2"), "risk_group2")
         + sqlCol(wp.itemStr("ex_close_date1"), "close_date", ">=")
         + sqlCol(wp.itemStr("ex_close_date2"), "close_date", "<=");

//		sum_total(ls_where);

   if (wp.itemEq("ex_apr_flag", "1")) {
      ls_where += " and nvl(close_apr_date,'') ='' and nvl(close_date,'')<>'' ";
   }
   else {
      ls_where += " and nvl(close_apr_date,'') <>'' ";
   }

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

//	void sum_total(String ls_where){		
//		//--未覆核
//		String sql1 = " select count(*) as db_cnt1 from rsk_trcorp_list "+ls_where+" and nvl(close_apr_date,'') ='' and nvl(close_date,'')<>'' ";
//		sqlSelect(sql1);
//		wp.colSet("tl_apr_flag_N", sqlStr("db_cnt1"));
//		
//		//--已覆核
//		String sql2 = " select count(*) as db_cnt2 from rsk_trcorp_list "+ls_where+" and nvl(close_apr_date,'') <>'' ";
//		sqlSelect(sql2);
//		wp.colSet("tl_apr_flag_Y", sqlStr("db_cnt2"));
//	}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " batch_no ,"
         + " corp_no ,"
         + " risk_group2 ,"
         + " trial_remark ,"
         + " trial_remark2 ,"
         + " trial_remark3 ,"
         + " close_date ,"
         + " close_user ,"
         + " close_apr_date ,"
         + " corp_p_seqno ,"
         + " action_code ,"
         + " risk_group1,"
         + " uf_corp_name(corp_no) as corp_name ,"
         + " hex(rowid) as rowid ,"
         + " mod_seqno ,"
         + " mod_time "

   ;
   wp.daoTable = "rsk_trcorp_list ";
   wp.whereOrder = " order by batch_no ";

   pageQuery();
   listWkdata();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setPageValue();
   //appr_Disabled("close_user");

}

void listWkdata() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_corp_no_cname", wp.colStr(ii, "corp_no") + "_" + wp.colStr(ii, "corp_name"));
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

   Rskp1250Func func = new Rskp1250Func();
   func.setConn(wp);

   String[] ls_rowid = wp.itemBuff("rowid");
   String[] ls_mod_seqno = wp.itemBuff("mod_seqno");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = ls_rowid.length;
   if (optToIndex(opt[0]) < 0) {
      alertErr("請點覆核資料");
      return;
   }

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
      rr = optToIndex(opt[ii]);
      if (rr < 0) {
         continue;
      }

      optOkflag(rr);

      func.varsSet("rowid", ls_rowid[rr]);
      func.varsSet("mod_seqno", ls_mod_seqno[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      optOkflag(rr, rc);
      if (rc == 1) {
         il_ok++;
         continue;
      }
      il_err++;
   }
   alertMsg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

public void OffprocFunc() throws Exception {
   int il_ok = 0;
   int il_err = 0;

   Rskp1250Func func = new Rskp1250Func();
   func.setConn(wp);

   String[] ls_rowid = wp.itemBuff("rowid");
   String[] ls_mod_seqno = wp.itemBuff("mod_seqno");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = ls_rowid.length;

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("rowid", ls_rowid[rr]);
      func.varsSet("mod_seqno", ls_mod_seqno[rr]);


      rc = func.OffdataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         il_ok++;
         continue;
      }
      il_err++;
      wp.colSet(rr, "ok_flag", "X");
   }
   alertMsg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

public void procFuncAll()  throws Exception {
   int il_err = 0;
   wp.listCount[0] = wp.itemRows("rowid");

   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_corp_no"), "corp_no", "like%")
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
         + sqlCol(wp.itemStr("ex_risk_group1"), "risk_group1")
         + sqlCol(wp.itemStr("ex_risk_group2"), "risk_group2")
         + sqlCol(wp.itemStr("ex_close_date1"), "close_date", ">=")
         + sqlCol(wp.itemStr("ex_close_date2"), "close_date", "<=");

   String sql1 = " select hex(rowid) as rowid , mod_seqno, close_user"+
         " from rsk_trcorp_list " +
         ls_where +
         " and nvl(close_apr_date,'') ='' and nvl(close_date,'')<>'' ";
   sqlSelect(sql1);

   int il_select_cnt = 0;
   il_select_cnt = sqlRowNum;

   if (il_select_cnt == 0) {
      alertErr("無資料可覆核");
      return;
   }

   Rskp1250Func func = new Rskp1250Func();
   func.setConn(wp);

   for (int ii = 0; ii < il_select_cnt; ii++) {
      if (!apprBankUnit(wp.colStr("close_user"),"")) {
         il_err++;
         continue;
      }
      func.varsSet("rowid", sqlStr(ii, "rowid"));
      func.varsSet("mod_seqno", sqlStr(ii, "mod_seqno"));

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc != 1) il_err++;
   }

   okAlert("資料全部覆核完成,請重新讀取資料; 失敗筆數:"+il_err);
}

public void procCancelAll() throws Exception  {
   int il_err = 0;
   wp.listCount[0] = wp.itemRows("rowid");

   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_corp_no"), "corp_no", "like%")
         + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
         + sqlCol(wp.itemStr("ex_risk_group1"), "risk_group1")
         + sqlCol(wp.itemStr("ex_risk_group2"), "risk_group2")
         + sqlCol(wp.itemStr("ex_close_date1"), "close_date", ">=")
         + sqlCol(wp.itemStr("ex_close_date2"), "close_date", "<=");

   String sql1 = " select hex(rowid) as rowid , mod_seqno, close_user"+
         " from rsk_trcorp_list " +
         ls_where +
         " and close_apr_date <> '' ";
   sqlSelect(sql1);

   int il_select_cnt = 0;
   il_select_cnt = sqlRowNum;

   if (il_select_cnt == 0) {
      alertErr("無資料可取消覆核");
      return;
   }

   Rskp1250Func func = new Rskp1250Func();
   func.setConn(wp);

   for (int ii = 0; ii < il_select_cnt; ii++) {
      func.varsSet("rowid", sqlStr(ii, "rowid"));
      func.varsSet("mod_seqno", sqlStr(ii, "mod_seqno"));

      rc = func.OffdataProc();
      sqlCommit(rc);
      if (rc != 1) il_err++;
   }

   if (il_err > 0) {
      alertErr(func.getMsg());
      return;
   }
   okAlert("資料全部取消覆核完成,請重新讀取資料");
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
