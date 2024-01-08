package rskm01;
/**
 * 2020-0518   JH    ++approve_all
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-0323:	JH		modify
 * 問題交易維護作業-主管覆核
 */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Rskp0020 extends BaseProc {
String kk1 = "", kk2 = "", kk3 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   msgOK();

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc();
         break;
      case "Q": //查詢功能
         queryFunc();
         break;
      case "R": // -資料讀取-
         dataRead();
         break;
      case "M": //瀏覽功能 :skip-page-
         queryRead();
         break;
      case "S": //動態查詢--
         querySelect();
         break;
      case "L": //清畫面--
         strAction = "";
         clearFunc();
         break;
      case "C1": // -資料處理-
         dataProcess();
         break;
      case "C2": //-取消覆核-
         cancelProcess();
         break;
      case "C3": //-全部覆核-
         doApproveAll();
         break;
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskp0020")) {
         wp.optionKey = wp.colStr("ex_curr_code");
         dddwList("dddw_dc_curr_code_tw",
               "ptr_sys_idtab", "wf_id", "wf_desc",
               "where wf_type = 'DC_CURRENCY'");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("clo_result");
         dddwList("dddw_clo_result"
               , "ptr_sys_idtab", "wf_id", "wf_desc"
               , "where wf_type='PRBQ_CLO_RESULT'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {

   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("新增日期起迄輸入錯誤");
      return;
   }

   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_add_date1"), "add_date", ">=")
         + sqlCol(wp.itemStr("ex_add_date2"), "add_date", "<=")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_add_user"), "add_user")
         + sqlCol(wp.itemStr("ex_curr_code"), "uf_nvl(curr_code,'901')")
   ;
   if (wp.itemEq("ex_apr_flag", "Y")) {
      wp.whereStr += " and prb_status in ('30','50') ";
      wp.whereStr += sqlCol(getSysDate(), "add_apr_date");
   }
   else if (wp.itemEq("ex_apr_flag", "N")) {
      wp.whereStr += " and prb_status in ('10','40') ";
   }
   if (wp.itemEq("ex_debit_flag", "Y")) {
      wp.whereStr += " and   debit_flag ='Y' ";
   }
   else if (wp.itemEq("ex_debit_flag", "N")) {
      wp.whereStr += " and   debit_flag <>'Y' ";
   }
   
   sum();
   wp.queryWhere = wp.whereStr;

   wp.setQueryMode();
   queryRead();

}

void sum() throws Exception {
	sqlParm.setSqlParmNoClear(true);
   String sql1 = " select "
         + " count(*) as tl_cnt "
         + " from rsk_problem "
         + wp.whereStr;
   sqlSelect(sql1);

   if (sqlNum("tl_cnt") <= 0) {
      wp.colSet("tl_cnt", "" + 0);
      wp.colSet("tl_amount", "" + 0);
      return;
   }
   wp.colSet("tl_cnt", "" + sqlNum("tl_cnt"));
   sqlParm.setSqlParmNoClear(true);
   String sql2 = " select "
         + " sum(uf_dc_amt(curr_code,prb_amount,dc_prb_amount)) as tl_amount "
         + " from rsk_problem "
         + wp.whereStr;

   sqlSelect(sql2);
   wp.colSet("tl_amount", "" + sqlNum("tl_amount"));

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "ctrl_seqno, "
         + "post_date, "
         + "acquire_date, "
         + commSqlStr.mchtName("", "") + " as mcht_chi_name, "
         + "source_amt, "
         + "source_curr, "
         + "dest_amt, "
         + "reference_no, "
         + "reference_seq, "
         + "add_date, "
         + "prb_status, "
         + "add_user, "
         + "prb_reason_code, "
         + "card_no, "
         + "purchase_date, "
         + "bin_type, "
         + "prb_mark, "
         + "prb_amount, "
         + "dest_curr, "
         + "p_seqno, "
         + "acct_type, "
         + "txn_code, "
         + commSqlStr.sqlDebitFlag + ","
         + "mcht_no, "
         + "film_no, "
         + " uf_dc_curr(curr_code) as curr_code, "
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt, "
         + " uf_dc_amt(curr_code,prb_amount,dc_prb_amount) as dc_prb_amount, "
         + " mod_seqno, "
         + " uf_tt_idtab('PRBL-REASON-CODE',prb_reason_code) as tt_reason_code,"
         + " '0' as can_appr, hex(rowid) as rowid ";
   wp.daoTable = "rsk_problem";
   wp.whereOrder = " order by add_user Asc, bin_type Asc, ctrl_seqno Asc ";

   pageQuery();
   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.setPageValue();
   for(int ll=0; ll<wp.listCount[0]; ll++) {
      if (apprBankUnit(wp.colStr(ll,"add_user"),wp.loginUser)) {
         wp.colSet(ll,"can_appr","1");
      }
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");
   dataRead();
}

@Override
public void dataRead() throws Exception {

   RskProblem pblm = new RskProblem();
   pblm.setConn(wp);
   rc = pblm.dataSelect(kk1, kk2);
   if (rc != 1) {
      alertErr(pblm.getMsg());
      return;
   }

   //wp.ddd("debit=%s, fraud=%s",wp.colStr("debit_flag"),wp.colStr("prb_fraud_rpt"));

   rskp0020Bill();
}

void rskp0020Bill() throws Exception {
   BilBill bill = new BilBill();
   bill.setConn(wp);

   bill.varsSet("reference_no", kk3);
   bill.varsSet("debit_flag", wp.colStr("debit_flag"));
   if (bill.dataSelect() != 1) {
      wp.notFound = "Y";
      alertErr(bill.getMsg());
      return;
   }
}

@Override
public void dataProcess() throws Exception {
   int ll_ok = 0, ll_err = 0;
   Rskp0020Func func = new rskm01.Rskp0020Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModSeq = wp.itemBuff("mod_seqno");

   wp.listCount[0] = aaRowid.length;
   if (optToIndex(aaOpt[0])<0) {
      alertErr(appMsg.optApprove);
      return;
   }
   optNumKeep(aaRowid.length, aaOpt);

   for (int ii=0; ii < aaOpt.length; ii++) {
      int rr =optToIndex(aaOpt[ii]);
      if (rr<0) continue;

      wp.colSet(rr, "opt_on", "checked");
      wp.colSet(rr, "ok_flag", "X");
      if (!apprBankUnit(wp.itemStr(rr,"add_user"),"")) {
         alertErr("異動經辦與覆核主管 須同單位 且 不可同一人");
         break;
      }

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModSeq[rr]);

      if (func.dataProc() != 1) {
         ll_err++;
         wp.colSet(rr, "ok_flag", "X");
         alertErr(func.getMsg());
         sqlCommit(-1);
         break;
      }

      //-OK-
      ll_ok++;
      sqlCommit(1);
      wp.colSet(rr, "ok_flag", "V");
   }

   alertMsg("覆核處理:成功筆數="+ll_ok+" 及 失敗筆數="+ll_err);
}

public void cancelProcess() throws Exception {
   int ll_ok = 0, ll_err = 0;
   rskm01.Rskp0020Func func = new rskm01.Rskp0020Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModSeq = wp.itemBuff("mod_seqno");

   wp.listCount[0] = aaRowid.length;
   optNumKeep(aaRowid.length, aaOpt);

   for (int rr = 0; rr < aaRowid.length; rr++) {
      if (!checkBoxOptOn(rr, aaOpt)) {
         continue;
      }
      wp.colSet(rr, "opt_on", "checked");
      wp.colSet(rr, "ok_flag", "X");

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModSeq[rr]);

      if (func.cancelProc() != 1) {
         ll_err++;
         wp.colSet(rr, "ok_flag", "X");
         alertErr(func.getMsg());
         sqlCommit(-1);
         break;
      }

      //-OK-
      ll_ok++;
      sqlCommit(1);
      wp.colSet(rr, "ok_flag", "V");
   }

   alertMsg("[解覆核處理]:成功筆數="+ll_ok+" 及 失敗筆數="+ll_err);

}

void doApproveAll() throws Exception {
   wp.listCount[0] =wp.itemRows("rowid");
   wp.pageControl();
   if (empty(wp.queryWhere)) {
      alertErr("未查詢待覆核資料, 不可執行全部覆核");
      return;
   }

   String sql1 ="select hex(rowid) as rowid, mod_seqno"+
         ", reference_no, ctrl_seqno, prb_status, add_user"+
         " from rsk_problem"+wp.queryWhere+
         " and prb_status in ('10','40')"+  //-待覆核條-
         " order by ctrl_seqno";
   sqlSelect(sql1);
   int ll_nrow =sqlRowNum;
   if (ll_nrow <=0) {
      alertErr("查無資料可覆核");
      return;
   }

   int ll_ok = 0, ll_err = 0;
   rskm01.Rskp0020Func func = new rskm01.Rskp0020Func();
   func.setConn(wp);

   for (int rr = 0; rr < ll_nrow; rr++) {
      String lsStatus =sqlStr(rr,"prb_status");
      if (commString.strIn(lsStatus,",10,40")==false) continue;
      //-appr-rule-
      if (!apprBankUnit(sqlStr(rr,"add_user"),"")) {
         ll_err++;
         continue;
      }

      func.varsSet("rowid", sqlStr(rr,"rowid"));
      func.varsSet("mod_seqno", sqlStr(rr,"mod_seqno"));

      int liRc =func.dataProc();
      sqlCommit(liRc);
      if (liRc != 1) {
         wp.log("-->%s, err=%s",rr,func.getMsg());
         ll_err++;
      }
      else ll_ok++;
   }

   queryFunc();
//   wp.alertClear();
   wp.alertMesg("覆核處理:成功筆數="+ll_ok+" 及 失敗筆數="+ll_err);
}

@Override
public void initButton() {
   this.btnUpdateOn(wp.itemEq("ex_apr_flag", "N"));
   this.btnDeleteOn(wp.itemEq("ex_apr_flag", "Y"));
   if ( wp.colNum("tl_cnt")<=wp.pageRows
         || !wp.itemEq("ex_apr_flag","N")) {
      buttonOff("btnproc3_off");
   }
}

}
