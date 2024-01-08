package rskm01;

/*預備依從/依從權主管覆核
   2019-0507:  JH    pre_comp.approve
 * 
 * */

import ofcapp.BaseAction;

public class Rskp0450 extends BaseAction {

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
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
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
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("提出日起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1"
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%");
   if (wp.itemEq("ex_compl_times", "1")) {
      lsWhere += " and compl_times ='1' and pre_apr_date ='' " +
            sqlCol(wp.itemStr("ex_user_id"), "pre_add_user") +
            sqlStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2"), "pre_add_date");
   }
   else {
      lsWhere += " and compl_times ='2' and com_apr_date ='' " +
            sqlCol(wp.itemStr("ex_user_id"), "com_add_user") +
            sqlStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2"), "com_add_date");
   }
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
         + " hex(rowid) as rowid "
         + ", ctrl_seqno, bin_type"
         + ", com_status"
         + ", mod_seqno"
         + ", card_no"
         + ", purchase_date"
         + ", mcht_country"
         + ", film_no"
         + ", uf_dc_curr(curr_code) as curr_code "
         + ", uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt "
         + ", compl_times, vcr_case_no"
         + ", event_date, pre_apply_date, pre_add_user"
         + ", viol_date"
         + ", pre_clo_result"
         + ", com_apply_date"
         + ", com_clo_result"
         + ", debit_flag "
         + ", decode(compl_times,2,com_add_user,pre_add_user) as add_user"
         + ", decode(compl_times,2,com_remark,pre_remark) as compl_remark"
   ;
   wp.daoTable = " rsk_precompl ";
   wp.whereOrder = "order by ctrl_seqno ";
//   wp.whereOrder ="order by compl_times, descode(compl_times,2,com_add_date,pre_add_date)";

   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
//   appr_Disabled("add_user");
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
   int ll_ok = 0, ll_err = 0;
   rskm01.Rskp0450Func func = new rskm01.Rskp0450Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModSeq = wp.itemBuff("mod_seqno");
   wp.listCount[0] = wp.itemRows("rowid");
   if (optToIndex(aaOpt[0]) < 0) {
      alertErr("請點選欲覆核資料");
      return;
   }

   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = this.optToIndex(aaOpt[ii]);
      if (rr < 0)
         continue;

      optOkflag(rr);
      func.ilProcRow = rr;
//      String ls_userid = wp.itemStr(rr, "wk_mod_user");
//      if (!appr_bankUnit(ls_userid)) {
//         opt_okflag(rr, -1);
//         ll_err++;
//         continue;
//      }

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModSeq[rr]);
      if (func.dataProc() == 1) {
         optOkflag(rr, 1);
         ll_ok++;
         sqlCommit(1);
      }
      else {
         optOkflag(rr, -1);
         ll_err++;
         wp.colSet(rr, "proc_mesg", func.getMsg());
         sqlCommit(-1);
      }
   }

   if (ll_err > 0) {
      alertErr("覆核完成; OK=" + ll_ok + ", ERR=" + ll_err);
   }
   else {
      alertMsg("覆核完成; OK=" + ll_ok + ", ERR=" + ll_err);
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
