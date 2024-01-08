package rskm01;
/** 扣款撥款登錄
 * 2020-1005   JH    modify
 * 2019-12-06  V1.00.01  Alex   add initButon
 *
 */

public class Rskm0230 extends ofcapp.BaseAction {

rskm01.Rskm0230Func func;


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
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 查詢功能 */
      strAction = "U";
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      //-資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      /* 資料處理 */
      procFunc();
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
   else {
      alertErr("未指定程式執行代碼: actionCode[%s]", wp.buttonCode);
   }
}

@Override
public void queryFunc() throws Exception {
   String lsSeqNo = wp.itemStr("ex_ctrl_seqno");
   if (!empty(lsSeqNo) && lsSeqNo.length() < 4) {
      alertErr("控制流水號: 至少4碼");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("扣款日期起迄：輸入錯誤");
      return;
   }
   String lsWhere = sqlCol(lsSeqNo, "ctrl_seqno", "like%") 		   
		   + sqlCol(wp.itemStr("ex_date1"),"fst_add_date",">=")
		   + sqlCol(wp.itemStr("ex_date2"),"fst_add_date","<=")
		   + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
		   + sqlCol(wp.itemStr("ex_bin_type"), "bin_type");
   if (empty(lsWhere)) {
      alertErr("流水號, 扣款日期, 卡號: 不可全部空白");
      return;
   }
   wp.whereStr = " where 1=1 and chg_stage in ('1','3') and sub_stage='30'"
         + lsWhere
         + sqlCol(wp.itemStr("ex_film_no"), "film_no", "like%")
         + sqlCol(wp.itemStr("ex_curcode"), "curr_code");

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "chg_stage||sub_stage||decode(fst_disb_yn,'','N',fst_disb_yn) as db_chg_status, "
         + "bin_type, "
         + "ctrl_seqno, "
         + "hex(rowid) as rowid, mod_seqno,"
         + "card_no, "
         + " uf_dc_curr(curr_code) as curr_code,"
         + "uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt, "
         + "uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_dc_amt, "
         + "chg_times, "
         + "film_no, "
         + "'' as db_desc, "
         //+ "fst_disb_dc_amt - fst_dc_amt as db_diffamt, "
         + "debit_flag, "
         + "fst_add_date, "
         + "fst_disb_apr_date, "
         + "reference_seq, "
         + "reference_no "
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by 1,2,3 ";
   pageQuery();

   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   queryAfter();
   listWkdata();
   wp.setListCount(1);
   wp.setPageValue();
}

void queryAfter() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colNum(ii, "fst_disb_dc_amt") == 0) {
         wp.colSet(ii, "fst_disb_dc_amt", wp.colStr(ii, "fst_dc_amt"));
      }
   }
}

void listWkdata() {
   for (int ll = 0; ll < sqlRowNum; ll++) {
      double lm_diff = wp.colNum(ll, "fst_disb_dc_amt") - wp.colNum(ll, "fst_dc_amt");
      wp.colSet(ll, "db_diffamt", lm_diff);
   }
}

@Override
public void querySelect() throws Exception {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public void dataRead() throws Exception {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm0230")) {
         wp.optionKey = wp.colStr("ex_curcode");
         dddwList("dddw_dc_curr_code_tw",
               "ptr_sys_idtab", "wf_id", "wf_desc",
               "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0230Func func = new rskm01.Rskm0230Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
//	String[] modseq =wp.item_buff("mod_seqno");
   String[] dcamt = wp.itemBuff("fst_disb_dc_amt");
   int ll_rowcnt = wp.itemRows("rowid");
   wp.listCount[0] = ll_rowcnt;
   if (ll_rowcnt <= 0) {
      return;
   }

   this.checkBoxOptOn(ll_rowcnt, aaOpt);

   int li_ok = 0, li_err = 0;
   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr =optToIndex(aaOpt[ii]);
      if (rr < 0) {
         continue;
      }

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", wp.itemStr(rr, "mod_seqno"));
      func.varsSet("disb_dc_amt", dcamt[rr]);

      rc = func.dbUpdate();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
//			wp.col_set(rr, "opt_edit", "disabled"); // -ckbox:hide-
         li_ok++;
         continue;
      }
      li_err++;
      wp.colSet(rr, "ok_flag", "X");
      wp.colSet(rr, "db_desc", func.getMsg());
   }

   // -re-Query-
   // queryRead();
   alertMsg("執行處理: 成功筆數=" + li_ok + "; 失敗筆數=" + li_err);
}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   btnModeAud("XX");

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}


}
