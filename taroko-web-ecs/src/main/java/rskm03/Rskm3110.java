package rskm03;
/**
 * 卡號控管記錄維護
 * 2020-0108:  Alex  queryRead add id_no
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 * 2019-0517:  JH    繳款截止日
 */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm3110 extends BaseEdit {
Rskm3110Func func;
String kk1 = "", kk2 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   //ddd("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      setSysdate();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      //-資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "R2")) {
      //-資料讀取-
      strAction = "R2";
      readDetl2();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page*/
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();

   }
   else if (eqIgno(wp.buttonCode, "S2")) {
      /* 動態查詢 */
      readSmsFlag();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
      wp.colSet("warn_date", wp.sysDate);
      wp.colSet("warn_user", selectUser());
   }
   else if (eqIgno(wp.buttonCode, "Z")) {
      /* 簡訊 */
      selectSmsData();
   } else if (eqIgno(wp.buttonCode, "AJAX")) {
	   switch (wp.getValue("ajaxCode")) {
       case "1":
         wfAjaxFunc1();
         break;
       case "2":
         wfAjaxCardno();
         break;
     }
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("find_type");
         dddwList("dddw_find_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_FIND_TYPE'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("rels_code");
         dddwList("dddw_rels_code", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_RELS_CODE'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("trans_unit");
         dddwList("dddw_trans_unit", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_TRANS_UNIT'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("case_type");
         dddwList("dddw_case_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_CASE_TYPE'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if ((wp.respHtml.equals("rskm3110_nadd"))) {
         wp.optionKey = "";
         wp.initOption = "";
         if (wp.colStr("msg_dept").length() > 0) {
            wp.optionKey = wp.colStr("msg_dept");
         }
         this.dddwList("dddw_dept_code"
               , "ptr_dept_code"
               , "trim(dept_code)"
               , "trim(dept_name)"
               , " where 1 = 1 ");
         wp.optionKey = "";
         wp.initOption = "";
         if (wp.colStr("ex_id").length() > 0) {
            wp.optionKey = wp.colStr("ex_id");
         }
         this.dddwList("dddw_msg_ex"
               , "sms_msg_ex"
               , "trim(ex_id)"
               , "trim(ex_desc)"
               , " where stop_flag!='Y'");
      }
   }
   catch (Exception ex) {
   }

}

void readDetl2() throws Exception {
   wp.pageRows = 999;
   kk1 = wp.itemStr("card_no");
   if (empty(kk1)) {
      alertErr("卡號不可空白");
      return;
   }
   wp.selectSQL = " mod_seqno, "
         + "card_no,   "
         + "proc_date, "
         + "proc_time, "
         + "ctfg_seqno,"
         + "proc_type,"
         + "tel_type,"
         + "tel_no,"
         + "contr_result,"
         + "proc_user,"
         + "cntl_way,"
         + "proc_remark,"
         + "mail_date"
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "card_no")
   ;
   wp.whereOrder = " order by proc_date Desc , proc_time Desc ";

   pageQuery();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }

   wp.setListCount(1);
   wp.colSet("db_tot_cnt", sqlRowNum);
   deltReadAfter();
}

void deltReadAfter() {
   int il_select = 0;
   il_select = wp.selectCnt;

   for (int ii = 0; ii < il_select; ii++) {
      if (wp.colEq(ii, "contr_result", "本人消費")) {
         wp.colSet(ii, "result_color", "style='background-color:deepskyblue'");
      }
      else if (wp.colEq(ii, "contr_result", "非本人消費")) {
         wp.colSet(ii, "result_color", "style='background-color:red'");
      }
   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_warn_date1"), wp.itemStr("ex_warn_date2")) == false) {
      alertErr("管制日期起迄：輸入錯誤");
      return;
   }

   if (wp.itemEmpty("ex_card_no") &&
         wp.itemEmpty("ex_warn_date1") &&
         wp.itemEmpty("ex_warn_date2") &&
         wp.itemEmpty("ex_warn_user") &&
         wp.itemEmpty("ex_idno") &&
         wp.itemEmpty("ex_bin_1") &&
         wp.itemEmpty("ex_bin_2") &&
         wp.itemEmpty("ex_bin_3") &&
         wp.itemEmpty("ex_bin_4") &&
         wp.itemEmpty("ex_bin_5") &&
         wp.itemEmpty("ex_bin_6")) {
      alertErr("條件不可全部空白");
      return;
   }

   wp.whereStr = " where 1=1"
         + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
         + sqlCol(wp.itemStr("ex_warn_date1"), "A.warn_date", ">=")
         + sqlCol(wp.itemStr("ex_warn_date2"), "A.warn_date", "<=")
         + sqlCol(wp.itemStr("ex_warn_user"), "A.warn_user", "like%")
   ;

   if (!wp.itemEmpty("ex_idno")) {
      wp.whereStr += " and "
            + " A.card_no in (select C.card_no from crd_idno I join crd_card C on C.id_p_seqno = I.id_p_seqno where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "I.id_no") + ""
            + " union "
            + " select E.card_no from dbc_idno F join dbc_card E on E.id_p_seqno = F.id_p_seqno where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "F.id_no") + ") ";
   }

   if (wp.itemEq("ex_unlock", "Y")) {
      wp.whereStr += " and length(nvl(rels_code,'')) =0";
   }

   if (wp.itemEq("ex_unmail", "Y")) {
      wp.whereStr += " and A.ctfg_seqno not in (select ctfg_seqno from rsk_ctfg_proc where mail_date <>'' and ctfg_seqno = A.ctfg_seqno and card_no = A.card_no) ";
   }

   String ls_bin = "";
   if (!wp.itemEmpty("ex_bin_1")) {
      if (empty(ls_bin)) ls_bin += " and substr(A.card_no,1,6) in ('" + wp.itemStr("ex_bin_1") + "'";
      else ls_bin += ",'" + wp.itemStr("ex_bin_1") + "'";
   }

   if (!wp.itemEmpty("ex_bin_2")) {
      if (empty(ls_bin)) ls_bin += " and substr(A.card_no,1,6) in ('" + wp.itemStr("ex_bin_2") + "'";
      else ls_bin += ",'" + wp.itemStr("ex_bin_2") + "'";
   }

   if (!wp.itemEmpty("ex_bin_3")) {
      if (empty(ls_bin)) ls_bin += " and substr(A.card_no,1,6) in ('" + wp.itemStr("ex_bin_3") + "'";
      else ls_bin += ",'" + wp.itemStr("ex_bin_3") + "'";
   }

   if (!wp.itemEmpty("ex_bin_4")) {
      if (empty(ls_bin)) ls_bin += " and substr(A.card_no,1,6) in ('" + wp.itemStr("ex_bin_4") + "'";
      else ls_bin += ",'" + wp.itemStr("ex_bin_4") + "'";
   }

   if (!wp.itemEmpty("ex_bin_5")) {
      if (empty(ls_bin)) ls_bin += " and substr(A.card_no,1,6) in ('" + wp.itemStr("ex_bin_5") + "'";
      else ls_bin += ",'" + wp.itemStr("ex_bin_5") + "'";
   }

   if (!wp.itemEmpty("ex_bin_6")) {
      if (empty(ls_bin)) ls_bin += " and substr(A.card_no,1,6) in ('" + wp.itemStr("ex_bin_6") + "'";
      else ls_bin += ",'" + wp.itemStr("ex_bin_6") + "'";
   }

   if (!empty(ls_bin)) {
      ls_bin += ")";
      wp.whereStr += ls_bin;
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();


}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "A.card_no,   "
         + "A.ctfg_seqno, "
         + "A.warn_date,"
         + "A.warn_time,"
         + "A.find_type,"
         + "A.warn_user,"
         + "A.pay_date,"
         + "A.rels_code,"
         + "A.rels_date,"
         + "A.rels_time,"
         + "A.rels_user,"
         + "A.rels_code,"
         + "A.warn_date,"
         + "'9' as db_sort1,"
         + "B.spec_remark, "
         + "substr(B.spec_remark,1,30) as spec_remark_30 ,"
         + " uf_date_diff(to_char(sysdate,'yyyymmdd'),A.warn_date) as db_pend_days , "
         + " A.id_p_seqno , "
         + " uf_idno_id2(A.card_no,'') as id_no  "
   ;
   wp.daoTable = "rsk_ctfg_mast A left join rsk_ctfg_card B on A.card_no = B.card_no";
   wp.whereOrder = " order by warn_date Desc, warn_time Desc ";
   pageQuery();
   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   list_wkdata();
   wp.totalRows = wp.dataCnt;
   wp.listCount[1] = wp.dataCnt;
   wp.setPageValue();

}

void list_wkdata() throws Exception  {
   String sql1 = " select card_note from cca_card_base where card_no = ? ";
   for (int ii = 0; ii <= wp.selectCnt; ii++) {
      if (empty(wp.colStr(ii, "rels_code")) == false) {
         wp.colSet(ii, "db_pend_days", "0");
      }

      sqlSelect(sql1, new Object[]{wp.colStr(ii, "card_no")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "db_card_note", sqlStr("card_note"));
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
   if (empty(kk1)) {
      kk1 = itemKk("card_no");
   }
   if (empty(kk2)) {
      kk2 = itemKk("ctfg_seqno");
   }

   if (empty(kk1)) {
      alertErr("卡號 : 不可空白");
      return;
   }

   if (empty(kk2)) {
      alertErr("管制序號: 不可空白");
      return;
   }

   wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno, "
         + "A.card_no,   "
         + "A.ctfg_seqno, "
         + "A.warn_date, "
         + "A.warn_time,"
         + "A.find_type,"
         + "A.warn_user,"
         + "A.pay_date,"
         + "A.rels_code,"
         + "A.rels_date,"
         + "A.rels_time,"
         + "A.rels_user,"
         + " A.sms_flag, "
         + " A.email_flag, "
         + "A.mod_user,"
         + "to_char(A.mod_time,'yyyymmdd') as mod_date,"
         + "uf_idno_id2(A.card_no,'') as id_no ,"
         + "uf_idno_name2(A.card_no,'') as chi_name ,"
         + "C.secnd_flag,"
         + "C.secnd_date,"
         + "C.trans_unit,"
         + "C.trans_date,"
         + "C.case_type,"
         + "C.otb_amt,"
         + "C.credit_limit,"
         + "C.proc_remark,"
         + "substr(C.proc_remark,1,60) as proc_remark_1 , "
         + "substr(C.proc_remark,61,60) as proc_remark_2 , "
         + "substr(C.proc_remark,121,60) as proc_remark_3 , "
         + "substr(C.proc_remark,181,60) as proc_remark_4 , "
         + "substr(C.proc_remark,241,60) as proc_remark_5 , "
         + "C.attm_cnt,"
         + "C.attm_amt,"
         + "C.fraud_ok_cnt,"
         + "C.close_flag,"
         + "B.spec_remark,"
         + "substr(B.spec_remark,1,60) as spec_remark_1 , "
         + "substr(B.spec_remark,61,60) as spec_remark_2 , "
         + "substr(B.spec_remark,121,60) as spec_remark_3 , "
         + "substr(B.spec_remark,181,60) as spec_remark_4 , "
         + "substr(B.spec_remark,241,60) as spec_remark_5  "
   ;
   wp.daoTable = "rsk_ctfg_mast A left join  rsk_ctfg_secnd C on A.card_no = C.card_no and A.CTFG_SEQNO = C.CTFG_SEQNO    left join   rsk_ctfg_card B      on A.card_no = B.CARD_NO";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "A.card_no")
         + sqlCol(kk2, "A.ctfg_seqno")
   ;
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1 + ", " + kk2);
      return;
   }
   setSysdate();
   dataReadAfter();

}

void dataReadAfter() throws Exception  {
   String sql1 = " select debit_flag , acno_p_seqno from cca_card_base where card_no = ? ";
   sqlSelect(sql1, new Object[]{wp.colStr("card_no")});

   if (sqlRowNum <= 0) return;

   String ls_debit_flag = "", ls_p_seqno = "";
   ls_debit_flag = sqlStr("debit_flag");
   ls_p_seqno = sqlStr("acno_p_seqno");
   if (eqIgno(ls_debit_flag, "Y")) {
      String sql3 = " select line_of_credit_amt from dba_acno where 1=1 and p_seqno = ? ";
      sqlSelect(sql3, new Object[]{ls_p_seqno});
   }
   else {
      String sql3 = " select line_of_credit_amt from act_acno where 1=1 and acno_p_seqno = ? ";
      sqlSelect(sql3, new Object[]{ls_p_seqno});
   }

   wp.colSet("credit_limit", sqlInt("line_of_credit_amt"));

}

@Override
public void saveFunc() throws Exception {
   if (eqIgno(wp.respHtml, "rskm3110_detl")) {
      func = new Rskm3110Func(wp);
      rc = func.dbSave(strAction);
      sqlCommit(rc);
      if (rc != 1) {
         alertErr(func.getMsg());
      }
   }
   else if (eqIgno(wp.respHtml, "rskm3110_nadd")) {
      func = new Rskm3110Func(wp);
      rc = func.insertSms();
      sqlCommit(rc);
      if (rc != 1) {
         alertErr(func.getMsg());
      }
   }


}

@Override
public void initPage()  {
   if (wp.respHtml.indexOf("_detl") > 0 && eqIgno(strAction, "new")) {
      wp.colSet("warn_date", wp.sysDate);
      wp.colSet("warn_user", selectUser());
   }
}

String selectUser() {

   String sql1 = " select usr_cname from sec_user where usr_id = ? ";
   sqlSelect(sql1, new Object[]{wp.loginUser});
   if (sqlRowNum > 0) return sqlStr("usr_cname");

   return "";
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
   }
}

public void wfAjaxCardno() throws Exception {   

   // String ls_winid =
   select_card(wp.itemStr("ax_card_no"));
   if (rc != 1) {
      return;
   }

   wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
   wp.addJSON("chi_name", sqlStr("chi_name"));
   wp.addJSON("id_no", sqlStr("id_no"));
   wp.addJSON("spec_remark", sqlStr("spec_remark"));
   wp.addJSON("spec_remark_1", sqlStr("spec_remark_1"));
   wp.addJSON("spec_remark_2", sqlStr("spec_remark_2"));
   wp.addJSON("spec_remark_3", sqlStr("spec_remark_3"));
   wp.addJSON("spec_remark_4", sqlStr("spec_remark_4"));
   wp.addJSON("spec_remark_5", sqlStr("spec_remark_5"));
   wp.addJSON("pay_date", sqlStr("pay_date"));
   wp.addJSON("line_of_credit_amt", sqlStr("line_of_credit_amt"));
}

void select_card(String s1) throws Exception  {

   String sql1 = " select "
         + " debit_flag, acno_p_seqno "
         + " from cca_card_base "
         + " where card_no = ? ";
   sqlSelect(sql1, new Object[]{s1});
   if (sqlRowNum <= 0) {
      alertErr("查無 卡號: card_no=" + s1);
      return;
   }

   boolean lb_debit = eqIgno(sqlStr("debit_flag"), "Y");

   if (lb_debit) {
      String sql2 = " select A.p_seqno as acno_p_seqno , A.id_p_seqno , uf_idno_id2(A.id_p_seqno,'Y') as id_no , uf_idno_name2(A.card_no,'') as chi_name , "
            + " B.spec_remark , "
            + " substr(B.spec_remark,1,60) as spec_remark_1 , "
            + " substr(B.spec_remark,61,60) as spec_remark_2 , "
            + " substr(B.spec_remark,121,60) as spec_remark_3 , "
            + " substr(B.spec_remark,181,60) as spec_remark_4 , "
            + " substr(B.spec_remark,241,60) as spec_remark_5  "
            + " from dbc_card A left join rsk_ctfg_card B on A.card_no = B.card_no "
            + " where A.card_no = ?  ";
      sqlSelect(sql2, new Object[]{s1});
   }
   else {
      String ls_sql = " select A.acno_p_seqno , A.id_p_seqno, uf_idno_id(A.id_p_seqno) as id_no , "
            + " B.spec_remark , "
            + " substr(B.spec_remark,1,60) as spec_remark_1 , "
            + " substr(B.spec_remark,61,60) as spec_remark_2 , "
            + " substr(B.spec_remark,121,60) as spec_remark_3 , "
            + " substr(B.spec_remark,181,60) as spec_remark_4 , "
            + " substr(B.spec_remark,241,60) as spec_remark_5 , "
            + " uf_idno_name2(A.card_no,'') as chi_name "
            + " from crd_card A left join rsk_ctfg_card B on A.card_no=B.card_no "
            + " where A.card_no = ? ";
      sqlSelect(ls_sql, new Object[]{s1});
   }
   if (sqlRowNum <= 0) {
      alertErr("查無 卡號: card_no=" + s1);
      return;
   }

   if (lb_debit) {
      String sql3 = " select line_of_credit_amt from dba_acno where p_seqno = ? ";
      sqlSelect(sql3, new Object[]{sqlStr("acno_p_seqno")});
   }
   else {
      String sql3 = "select A.line_of_credit_amt"
            + ", B.this_lastpay_date as pay_date"
            + " from act_acno A left join ptr_workday B on B.stmt_cycle=A.stmt_cycle"
            + " where A.acno_p_seqno = ? ";
      sqlSelect(sql3, new Object[]{sqlStr("acno_p_seqno")});
   }

   if (sqlRowNum <= 0) {
      alertErr("查無 卡號: card_no=" + s1);
      return;
   }

   return;
}

void selectSmsData() throws Exception  {
   String sql1 = " select debit_flag from cca_card_base where card_no = ? ";
   sqlSelect(sql1, new Object[]{wp.itemStr("data_k1")});
   if (sqlRowNum <= 0) {
      errmsg("卡號錯誤");
      return;
   }

   if (wp.itemEmpty("id_no")) {
      errmsg("身分證ID 不可空白");
      return;
   }

   if (eqIgno(sqlStr("debit_flag"), "Y")) {
      wp.sqlCmd = " select "
            + " a.cellar_phone as cellar_phone ,"
            + " a.chi_name as chi_name ,"
            + " a.id_p_seqno as id_p_seqno "
            + " from  dbc_idno a "
            + " where 1=1 "
            + sqlCol(wp.itemStr("id_no"), "a.id_no")
      ;
   }
   else {
      wp.sqlCmd = " select "
            + " a.cellar_phone as cellar_phone ,"
            + " a.chi_name as chi_name ,"
            + " a.id_p_seqno as id_p_seqno "
            + " from  crd_idno a "
            + " where 1=1 "
            + sqlCol(wp.itemStr("id_no"), "a.id_no")
      ;
   }


   this.sqlSelect();
   if (sqlRowNum <= 0)
      alertErr("持卡者ID:[" + wp.itemStr("id_no") + "]查無資料");

   wp.colSet("cellar_phone", sqlStr("cellar_phone"));
   wp.colSet("chi_name", sqlStr("chi_name"));
   wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));

   return;
}

public void wfAjaxFunc1() throws Exception {
//   super.wp = wr;

   if (wp.itemStr("ax_win_ex_id").length() == 0) return;

   select_ajax_func_1_0(wp.itemStr("ax_win_ex_id"));

   if (rc != 1) {
      wp.addJSON("msg_userid", "");
      wp.addJSON("msg_id", "");
      wp.addJSON("msg_desc", "");
      wp.addJSON("chi_name_flag", "");
      return;
   }

   wp.addJSON("msg_userid", sqlStr("msg_userid"));
   wp.addJSON("msg_id", sqlStr("msg_id"));
   wp.addJSON("msg_desc", sqlStr("msg_desc"));
   wp.addJSON("chi_name_flag", sqlStr("chi_name_flag"));
}

// ************************************************************************
void select_ajax_func_1_0(String s1) throws Exception  {
   wp.sqlCmd = " select "
         + " a.msg_userid as msg_userid ,"
         + " a.msg_id as msg_id ,"
         + " a.ex_desc as msg_desc ,"
         + " a.chi_name_flag as chi_name_flag "
         + " from  sms_msg_ex a "
         + " where a.ex_id ='" + s1 + "' "
   ;

   this.sqlSelect();
   if (sqlRowNum <= 0)
      alertErr("簡訊範例[" + s1 + "]查無資料");

   return;
}

void readSmsFlag() throws Exception  {
   wp.selectSQL = " sms_flag ";
   wp.daoTable = " rsk_ctfg_mast ";
   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("card_no"), "card_no")
         + sqlCol(wp.itemStr("ctfg_seqno"), "ctfg_seqno")
   ;

   pageSelect();
}

void setSysdate() {
   wp.colSet("sysdate", getSysDate());
}

}
