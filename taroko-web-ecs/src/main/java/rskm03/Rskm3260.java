package rskm03;
/**
 * 2020-1015   JH    modify
 * 2020-0108   Alex    bug fixed
 * 2019-1206   Alex    add init Button
 * 2019-0908   JH      xxx_ctrlseqno_log
 */

import ofcapp.BaseAction;
import rskm01.RskCtrlseqno;
import taroko.com.TarokoCommon;

public class Rskm3260 extends BaseAction {
String kk1 = "", kk2 = "", ls_ctrl_seqno = "", ls_bin_type = "", dddw_bin_type = "";
int ss = 0;

@Override
public void userAction() throws Exception {
   if (wp.respHtml.indexOf("m3260_proc")>0) {
      if (eqIgno(wp.buttonCode,"U")) {
            doProc_update();
            return;
      }
      if (eqIgno(wp.buttonCode,"D")) {
            doProc_delete();
            return;
      }
   }
   else if (wp.respHtml.indexOf("m3260_call")>0) {
      if (eqIgno(wp.buttonCode,"U")) {
         doCall_insert();
         return;
      }
   }

   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         //clearFunc();
         wp.colSet("case_no", wp.itemStr("case_no"));
         break;
      case "L": //清畫面--
         strAction = "";
         clearFunc(); break;
      case "Q": //查詢功能
         queryFunc(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "R": // -資料讀取-
         dataRead(); break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
         saveFunc(); break;
      case "C": // -資料處理-
         procFunc(); break;
      case "S1":
         querySelect_Txn(); break;
      case "S2":
         querySelect_Proc(); break;
      case "R2":
         dataRead2(); break;
      case "AJAX":
    	 wf_ajax_ctrl_seqno(wp); 
    	 break;
      default:
         alertErr("actionCode 未指定對應功能, action[%s]",wp.buttonCode);
   }

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm3260_proc")) {
         wp.optionKey = wp.colStr(0, "hold_reason");
         dddwList("dddw_hold_reason", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type='CTFC-HOLD-CODE'");

         dddw_bin_type = wp.colStr("bin_type");
         wp.optionKey = wp.colStr(0, "cb_reason_1st");
         dddwList("dddw_reason_1st", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type like 'CTFC-CB-CODE-" + dddw_bin_type + "%'");

         dddw_bin_type = wp.colStr("bin_type");
         wp.optionKey = wp.colStr(0, "cb_reason_2nd");
         dddwList("dddw_reason_2nd", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type like 'CTFC-CB-CODE-" + dddw_bin_type + "%'");
      }
   }
   catch (Exception ex) {
   }

//   try {
//      if (eqIgno(wp.respHtml, "rskm3260_proc")) {
//      }
//   }
//   catch (Exception ex) {
//   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("Assign Date 起迄：輸入錯誤");
      return;
   }

   if (!empty(wp.itemStr("ex_idno"))) {
      if (wp.itemStr("ex_idno").length() < 8) {
         alertErr("ID 長度不可小於 8");
         return;
      }
   }

   if (!empty(wp.itemStr("ex_card_no"))) {
      if (wp.itemStr("ex_card_no").length() < 12) {
         alertErr("Card No 長度不可小於 12");
         return;
      }
   }

   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_idno"), "idno", "like%")
         + sqlCol(wp.itemStr("ex_case_no"), "case_no", "like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user")
         + sqlCol(wp.itemStr("ex_date1"), "assign_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "assign_date", "<=");

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " case_no ,"
         + " idno ,"
         + " assign_date ,"
         + " card_no ,"
         + " proc_user ,"
         + " source_type ,"
         + wp.sqlID + "uf_ctfc_idname(card_no) as db_idno_name "
   ;
   wp.daoTable = "rsk_ctfc_mast";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   wp.whereOrder = " order by idno Asc, assign_date Desc ";
   pageQuery();

   wp.setListCount(1);
//		queryAfter();
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();

}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) kk1 = itemkk("case_no");
   if (empty(kk1)) {
      alertErr("案件流水號: 不可空白");
      return;
   }

   wp.selectSQL = ""
         + " A.* ,"
         + " uf_ctfc_idname(A.card_no) as db_cname ,"
         + " uf_bin_type(A.card_no) as bin_type "
   ;
   wp.daoTable = "rsk_ctfc_mast A ";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "A.case_no");

   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
   queryTxn();
}

void queryTxn() throws Exception {
   daoTid = "A.";
   wp.sqlCmd = " select "
         + " case_no ,"
         + " ctrl_seqno ,"
         + " on_us_flag ,"
         + " on_us_mcht_name ,"
         + " txn_date ,"
         + " arn_year ,"
         + " arn_ddd ,"
         + " txn_amt ,"
         + " proc_status ,"
         + " ecs_close_date ,"
         + " ecs_close_reason ,"
         + " hex(rowid) as rowid ,"
         + " reference_no ,"
         + " reference_seq "
         + " from rsk_ctfc_txn "
         + " where 1=1 "
         + sqlCol(kk1, "case_no")
         + " order by case_no Asc, txn_date desc ";
   pageQuery();
   wp.notFound = "N";
   wp.setListCount(1);
}

void dataRead2() throws Exception {
   kk1 = wp.itemStr("case_no");
   wp.selectSQL = ""
         + " A.case_no ,"
         + " A.idno ,"
         + " A.assign_date ,"
         + " A.card_no , "
         + " decode(nvl(B.chi_name,''),'',uf_ctfc_idname(A.card_no),B.chi_name) as chi_name ,"
//				+ " B.chi_name ,"
         + " B.zip_code ,"
         + " B.addr ,"
         + " rpad(' ',20,' ') db_telno_h ,"
         + " rpad(' ',20,' ') db_telno_o "
   ;
   wp.daoTable = "rsk_ctfc_mast A left join rsk_ctfc_idno B on A.idno = B.id_no";
   wp.whereStr = "where 1=1 "
         + sqlCol(kk1, "A.case_no");
   pageSelect();

   if (sqlNotFind()) {
      alertMsg("查無資料, key=" + kk1);
      selectOK();
   }
   else {
      selectTel();
   }
   queryCall();
}

void selectTel() throws Exception  {
   if (wp.colEmpty("card_no")) return;
   String sql1 = " select "
         + " A.office_area_code1||'-'||A.office_tel_no1||'-'||A.office_tel_ext1 as office_tel , "
         + " A.home_area_code1||'-'||A.home_tel_no1||'-'||A.home_tel_ext1 as home_tel "
         + " from crd_idno A join crd_card B on A.id_p_seqno = B.id_p_seqno "
         + " where B.card_no = ? "
         + " union "
         + " select "
         + " A.office_area_code1||'-'||A.office_tel_no1||'-'||A.office_tel_ext1 as office_tel , "
         + " A.home_area_code1||'-'||A.home_tel_no1||'-'||A.home_tel_ext1 as home_tel "
         + " from dbc_idno A join dbc_card B on A.id_p_seqno = B.id_p_seqno "
         + " where B.card_no = ? ";
   sqlSelect(sql1, new Object[]{wp.colStr("card_no"), wp.colStr("card_no")});
   if (sqlRowNum > 0) {
      wp.colSet("db_telno_o", sqlStr("office_tel"));
      wp.colSet("db_telno_h", sqlStr("home_tel"));
   }
}

void queryCall() throws Exception {
   wp.sqlCmd = " select "
         + " hex(rowid) as rowid ,"
         + " case_no ,"
         + " call_date ,"
         + " call_time ,"
         + " tel_no ,"
         + " attn_man ,"
         + " call_desc ,"
         + " proc_user ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " from rsk_ctfc_call "
         + " where 1=1 "
         + sqlCol(kk1, "case_no")
         + " order by call_date Desc , call_time Asc ";
   pageQuery();
   wp.notFound = "N";
   wp.setListCount(1);
   wp.colSet("ind_num", "" + sqlRowNum);
}

void querySelect_Txn() throws Exception {
   kk2 = wp.itemStr("data_k1");
   dataRead_txn();
}

void dataRead_txn() throws Exception  {
   kk1 = wp.itemStr("case_no");
   if (empty(kk2)) {
      kk2 = itemkk("ctrl_seqno");
   }

   wp.selectSQL = ""
         + " case_no ,"
         + " ctrl_seqno ,"
         + " on_us_flag ,"
         + " on_us_mcht_name ,"
         + " txn_date ,"
         + " arn_year ,"
         + " arn_ddd ,"
         + " txn_amt ,"
         + " proc_status ,"
         + " ecs_close_date ,"
         + " ecs_close_reason ,"
         + " reference_no ,"
         + " reference_seq , "
         + " hex(rowid) as rowid "
   ;
   wp.daoTable = " rsk_ctfc_txn ";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "case_no")
         + sqlCol(kk2, "ctrl_seqno");
   pageSelect();

   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }

}

void querySelect_Proc() throws Exception  {
   kk1 = wp.itemStr("data_k1");
   dataRead_Proc();
}

void dataRead_Proc() throws Exception  {
   wp.selectSQL = ""
         + " A.* ,"
         + " hex(A.rowid) as rowid ,"
         + " '0' db_hold ,"
         + " '0' db_receipt ,"
         + " '0' db_cb_1st ,"
         + " '0' db_cb_2nd ,"
         + " '0' db_arbi "
   ;
   wp.daoTable = " rsk_ctfc_proc A ";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "A.ctrl_seqno")
   ;
   pageSelect();

   if (sqlNotFind()) {
      wp.notFound = "N";
      wp.colSet("ctrl_seqno", kk1);
   }
   dataReadAfterProc();
}

void dataReadAfterProc() throws Exception  {
   ss = wp.colStr("ctrl_seqno").length();
   if (ss == 10) {
      ls_ctrl_seqno = wp.colStr("ctrl_seqno");
      ls_bin_type = "";
   }
   else {
      ls_ctrl_seqno = wp.colStr("ctrl_seqno").substring(1, ss);
      ls_bin_type = wp.colStr("ctrl_seqno").substring(0, 1);
   }

   //**-問交-
   if (empty(wp.colStr("hold_date"))) {
      String sql1 = " select "
            + " add_apr_date , "
            + " nvl(prb_amount,0) as prb_amount , "
            + " prb_reason_code , "
            + " uf_tt_idtab('PRBQ_REASON_CODE',prb_reason_code) as tt_reason_code"
            + " from	rsk_problem "
            + " where 1=1 "
            + sqlCol(ls_ctrl_seqno, "ctrl_seqno");
      if (ss != 10)
         sql1 += sqlCol(ls_bin_type, "bin_type");

      sqlSelect(sql1);
      if (sqlRowNum > 0) {
         wp.colSet("hold_date", sqlStr("add_apr_date"));
         wp.colSet("hold_amt", sqlStr("prb_amount"));
         wp.colSet("hold_code", sqlStr("prb_reason_code"));
         wp.colSet("hold_reason", sqlStr("tt_reason_code"));
      }
   }

   //**-調單-
   if (empty(wp.itemStr("recv_date"))) {
      String sql2 = " select "
            + " send_date , "
            + " recv_date "
            + " from rsk_receipt "
            + " where 1=1 "
            + sqlCol(ls_ctrl_seqno, "ctrl_seqno");
      if (ss != 10)
         sql2 += sqlCol(ls_bin_type, "bin_type");
      sql2 += " fetch first 1 rows only ";
      sqlSelect(sql2);
      if (sqlRowNum > 0)
         wp.colSet("recv_date", sqlStr("send_date"));
   }

   //**-扣款-1ST-
   if (empty(wp.itemStr("cb_date_1st"))) {
      String sql3 = " select "
            + " decode(chg_times,2,sec_apr_date,fst_apr_date) as fst_apr_date ,"
            + " decode(chg_times,2,sec_twd_amt,fst_twd_amt) as fst_twd_amt , "
            + " decode(chg_times,2,sec_reason_code,fst_reason_code) as fst_reason_code ,"
            + " uf_tt_idtab('CTFC-CB-CODE-'||bin_type,decode(chg_times,2,sec_reason_code,fst_reason_code)) as tt_reason_code "
            + " from rsk_chgback "
            + " where 1=1 "
            + sqlCol(ls_ctrl_seqno, "ctrl_seqno");
      if (ss != 10)
         sql3 += sqlCol(ls_bin_type, "bin_type");
      sqlSelect(sql3);
      if (sqlRowNum > 0) {
         wp.colSet("cb_date_1st", sqlStr("fst_apr_date"));
         wp.colSet("cb_amt_1st", sqlStr("fst_twd_amt"));
         wp.colSet("cb_code_1st", sqlStr("fst_reason_code"));
         wp.colSet("cb_reason_1st", sqlStr("tt_reason_code"));
      }
   }

   //**-扣款-2nd-
   if (empty(wp.colStr("rp_date")) || empty(wp.colStr("cb_date_2nd"))) {
      String sql4 = " select "
            + " rep_add_date , "
            + " decode(chg_times,2,fst_apr_date,'') as fst_apr_date , "
            + " decode(chg_times,2,fst_twd_amt,0) as fst_twd_amt , "
            + " decode(chg_times,2,fst_reason_code,'') as fst_reason_code , "
            + " uf_tt_idtab('CTFC-CB-CODE-'||bin_type,decode(chg_times,2,sec_reason_code,fst_reason_code)) as tt_reason_code "
            + " from rsk_chgback "
            + " where 1=1 "
            + " and chg_stage in ('2','3') "
            + sqlCol(ls_ctrl_seqno, "ctrl_seqno");
      if (ss != 10)
         sql4 += sqlCol(ls_bin_type, "bin_type");
      sqlSelect(sql4);
      if (sqlRowNum > 0) {
         if (empty(wp.colStr("rp_date"))) {
            wp.colSet("rp_date", sqlStr("rep_add_date"));
         }
         if (empty(wp.colStr("cb_date_2nd"))) {
            wp.colSet("cb_date_2nd", sqlStr("fst_apr_date"));
            wp.colSet("cb_amt_2nd", sqlStr("fst_twd_amt"));
            wp.colSet("cb_code_2nd", sqlStr("fst_reason_code"));
            wp.colSet("cb_reason_2nd", sqlStr("tt_reason_code"));
         }
      }
   }


}

@Override
public void saveFunc() throws Exception {
   Rskm3260Func func = new Rskm3260Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else saveAfter(false);

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {

   if (eqIgno(wp.respHtml, "rskm3260_txn")) {
      btnModeAud();
   }

   if (eqIgno(wp.respHtml, "rskm3260_call")) {
      btnModeAud("XX");
   }
   if (wp.respHtml.indexOf("m3260_proc")>0) {
      btnModeAud("XX");
   }

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

void doCall_insert() throws Exception {
   Rskm3260Func func = new Rskm3260Func();
   func.setConn(wp);
   String[] aa_code = wp.itemBuff("call_date");
   wp.listCount[0] = aa_code.length;

   rc = func.insert_call();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      dataRead2();
      wp.colSet("ex_call_date", "");
      wp.colSet("ex_call_time", "");
      wp.colSet("ex_tel_no", "");
      wp.colSet("ex_attn_man", "");
      wp.colSet("ex_call_desc", "");
      wp.colSet("ex_call_desc2", "");
   }
}

public void wf_ajax_ctrl_seqno(TarokoCommon wr) throws Exception {
   super.wp = wr;

   // String ls_winid =
   select_data(wp.itemStr("ax_ctrl_seqno"));
   if (rc != 1) {

      wp.addJSON("reference_no", "");
      wp.addJSON("reference_seq", "");
      wp.addJSON("txn_date", "");
      wp.addJSON("arn_year", "");
      wp.addJSON("arn_ddd", "");
      return;
   }
   wp.addJSON("reference_no", sqlStr("reference_no"));
   wp.addJSON("reference_seq", sqlStr("reference_seq"));
   wp.addJSON("txn_date", sqlStr("purchase_date"));
   wp.addJSON("arn_year", sqlStr("film_no").substring(8, 9));
   wp.addJSON("arn_ddd", sqlStr("film_no").substring(9, 12));
}

void select_data(String s1) throws Exception  {
   RskCtrlseqno ooctrl = new RskCtrlseqno();
   ooctrl.setConn(wp);
   if (ooctrl.checkCtrlSeqNo(s1) == false) {
      rc = -1;
      return;
   }

   if (ooctrl.ibDebit) {
      String sqlY = " select purchase_date , film_no from dbb_bill "
            + " where reference_no = ? ";
      sqlSelect(sqlY, new Object[]{ooctrl.referenceNo});
   }
   else {
      String sqlN = " select purchase_date , film_no from bil_bill "
            + " where reference_no = ? ";
      sqlSelect(sqlN, new Object[]{ooctrl.referenceNo});
   }
   if (sqlRowNum <= 0) {
      rc = -1;
      return;
   }

   return;
}

void doProc_update() throws Exception {
   Rskm3260Func func = new Rskm3260Func();
   func.setConn(wp);
   rc = func.saveProc();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else saveAfter(false);
}

void doProc_delete() throws Exception {
   Rskm3260Func func = new Rskm3260Func();
   func.setConn(wp);

   rc = func.delete_rsk_ctfc_Proc();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else saveAfter(false);
}

}
