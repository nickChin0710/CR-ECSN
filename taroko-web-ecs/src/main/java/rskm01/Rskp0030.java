package rskm01;
/**
 * 2020-0518   JH    ++all_approve
 * 2020-0414	JH		++contract_no
 * 2019-1216   JH    UAT
 * 2019-0703   JH    modify
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 問交/特交/不合格結案登錄-主管覆核
 */

import ofcapp.BaseAction;

public class Rskp0030 extends BaseAction {
String kk1 = "", kk2 = "", kk3 = "";
String lsWhere = "";

@Override
public void userAction() throws Exception {

   switch (wp.buttonCode) {
      case "C1":
         procFunc_Cancel(); break;
      case "C3":
         doApprove_all(); break;
      default:
         defaultAction();
   }
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskp0030")) {

         wp.optionKey = wp.colStr(0, "ex_curr_code");
         dddwList("dddw_dc_curr_code_tw"
               , "ptr_sys_idtab", "wf_id", "wf_desc"
               , "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
/*
	if (eq_igno(wp.itemStr("ex_apr_flag"), "Y")) {
		if (wp.item_empty("ex_ctrl_seqno") &&
				empty(wp.itemStr("ex_date1")) && empty(wp.itemStr("ex_date2"))) {
			alertErr("結案登錄日期 : 不可空白");
			return;
		}
	}
*/
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("結案登錄日期起迄：輸入錯誤");
      return;
   }

   lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_type"), "prb_mark")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_user"),"close_add_user","like%")
         ;


   lsWhere += sqlCol(wp.itemStr("ex_curr_code"), "uf_dc_curr(curr_code)");
   if (!eqIgno(wp.itemStr("ex_debit_flag"), "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_debit_flag"), "debit_flag");
   }

   String lsDate1 =wp.itemStr("ex_date1");
   String lsDate2 =wp.itemStr("ex_date2");
   if (eqIgno(wp.itemStr("ex_apr_flag"), "Y")) {
      lsWhere += " and close_apr_date = to_char(sysdate,'yyyymmdd') ";
      lsWhere += "and prb_status='80' ";
   }
   else {
      lsWhere += "and prb_status='60'"+
      sqlCol(lsDate1,"close_add_date",">=")+
      sqlCol(lsDate2,"close_add_date","<=");
   }
   
   setSqlParmNoClear(true);
   sum(lsWhere);

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

void sum(String lsWhere) throws Exception {
   String sql1 = " select "
         + " count(*) as tl_cnt , "
         + " sum(dc_mcht_repay) as tl_dc_mcht_repay "
         ;
   sql1 += " from rsk_problem "
         + lsWhere
   ;
   sqlSelect(sql1);

   if (sqlNum("tl_cnt") == 0) {
      wp.colSet("tl_cnt", "" + 0);
      wp.colSet("tl_dc_mcht_repay", "" + 0);
   }

   wp.colSet("tl_cnt", "" + sqlNum("tl_cnt"));
   wp.colSet("tl_dc_mcht_repay", "" + sqlNum("tl_dc_mcht_repay"));
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no ,"
         + " debit_flag ,"
         + " ctrl_seqno ,"
         + " prb_status,"
         + " prb_amount ,"
         + " dc_prb_amount ,"
         + " dest_amt ,"
         + " prb_reason_code ,"
         + " uf_dc_curr(curr_code) as curr_code ,"
         + " dc_dest_amt ,"
         + " reference_no ,"
         + " reference_seq,"
         + " bin_type ,"
         + " uf_tt_idtab('PRBL-REASON-CODE',prb_reason_code) as tt_reason_code,"
         + " mod_seqno,"
         + " hex(rowid) as rowid,"
         + " dc_mcht_repay , dc_mcht_repay_2, contract_no, "
         + " 1 as clo_type,"
            + " close_add_date,"
            + " close_add_user,"
            + " clo_result,"
            + " mcht_close_fee ,"
            + " uf_tt_idtab(decode(debit_flag,'Y','DBP','PRB')||prb_mark||'-CLO-RESULT',clo_result) as tt_clo_result ,"
            + " '' as xxx"
      ;

   wp.daoTable = "rsk_problem";
   wp.whereOrder = " order by ctrl_seqno";

   //sql_ddd();
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
   for(int ll=0; ll<wp.listCount[0]; ll++) {
      if (apprBankUnit(wp.colStr(ll,"close_add_user"),wp.loginUser))
         wp.colSet(ll,"can_appr","1");
      else wp.colSet(ll,"can_appr","0");
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
   RskProblem Pro = new RskProblem();
   Pro.setConn(wp);
   Pro.dataSelect(kk1, kk2, kk3);

   BilBill Bil = new BilBill();
   Bil.setConn(wp);
   Bil.varsSet("reference_no", kk3);
   Bil.varsSet("debit_flag", wp.colStr("debit_flag"));
   Bil.dataSelect();
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   int ll_ok = 0, ll_err = 0;

   Rskp0030Func func = new Rskp0030Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModseq = wp.itemBuff("mod_seqno");
   String[] aaClo = wp.itemBuff("clo_type");
   wp.listCount[0] = aaRowid.length;
   this.optNumKeep(aaRowid.length, aaOpt);
   if (optToIndex(aaOpt[0]) < 0) {
      alertErr("未點選欲覆核資料");
      return;
   }

   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0) continue;
      wp.colSet(rr, "ok_flag", "X");
      if (!apprBankUnit(wp.itemStr(rr,"close_add_user"),wp.loginUser)) {
         ll_err++;
         break;
      }

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModseq[rr]);

      if (eqAny(aaClo[rr], "2")) {
         rc = func.dataProc2();
      }
      else rc = func.dataProc();
      sqlCommit(rc);
      if (rc != 1) {
         ll_err++;
         alertErr(func.getMsg());
         break;
      }

      //-OK-
      ll_ok++;
      wp.colSet(rr, "ok_flag", "V");
   }

   alertMsg("覆核處理:成功筆數="+ll_ok+" 及 失敗筆數="+ll_err);
}

public void procFunc_Cancel() throws Exception {
   int ll_ok = 0, ll_err = 0;

   Rskp0030Func func = new Rskp0030Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModseq = wp.itemBuff("mod_seqno");
   String[] aaClo = wp.itemBuff("clo_type");
   wp.listCount[0] = aaRowid.length;

   this.optNumKeep(aaRowid.length, aaOpt);
   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0) continue;

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModseq[rr]);
      if (eqAny(aaClo[rr], "2")) {
         rc = func.cancel_Proc_2();
      }
      else rc = func.cancelProc();
      sqlCommit(rc);
      if (rc != 1) {
         alertErr(func.getMsg());
         ll_err++;
         wp.colSet(rr, "ok_flag", "X");
         break;
      }
      //--OK-
      ll_ok++;
      wp.colSet(rr, "ok_flag", "V");
   }

   alertMsg("執行完成; 成功筆數=" + ll_ok + ", 錯誤筆數=" + ll_err);

}

void doApprove_all() throws Exception {
   wp.listCount[0] =wp.itemRows("rowid");
   wp.pageControl();
   if (empty(wp.queryWhere)) {
      alertErr("無查詢條件, 不可全部覆核");
      return;
   }

   String sql1 ="select hex(rowid) as rowid, mod_seqno"+
         ", ctrl_seqno, reference_no"+
         " from rsk_problem"+wp.queryWhere+
         " and prb_status in ('60','83')"+
         " order by ctrl_seqno";
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      alertErr("查無待覆核資料");
      return;
   }

   //--
   int ll_ok = 0, ll_err = 0;

   Rskp0030Func func = new Rskp0030Func();
   func.setConn(wp);

   int ll_nrow =sqlRowNum;
   for (int ii = 0; ii < ll_nrow; ii++) {

      func.varsSet("rowid", sqlStr(ii,"rowid"));
      func.varsSet("mod_seqno", sqlStr(ii,"mod_seqno"));

      int li_rc = func.dataProc();
      sqlCommit(li_rc);
      if (li_rc != 1) {
         wp.log("-->%s: err=%s", ii,func.getMsg());
         ll_err++;
      }
      else ll_ok++;
   }

   queryFunc();
//   wp.alert_Clear();
   wp.alertMesg("覆核處理:成功筆數="+ll_ok+" 及 失敗筆數="+ll_err);
}

@Override
public void initButton() {
   this.btnUpdateOn(wp.itemEq("ex_apr_flag", "N"));
   this.btnDeleteOn(wp.itemEq("ex_apr_flag", "Y"));
   if (!wp.itemEq("ex_apr_flag","N") || wp.colNum("tl_cnt")<=wp.pageRows) {
      buttonOff("btnproc3_off");
   }
}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
