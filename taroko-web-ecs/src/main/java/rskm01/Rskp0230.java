package rskm01;
/**
 * 2020-1019   JH    all_appr.bankunit
 * 2020-0528   JH    ++[all-approve]
 * 2019-0926   JH    合計
 * 再提示整批登錄覆核-主管作業
 */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Rskp0230 extends BaseProc {
Rskp0230Func func=null;
int ilOk = 0;
int ilErr = 0;
String kk1 = "", kk2 = "", kk3 = "";
String isSumWhere = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "C":
         // -資料處理-
         dataProcess(); break;
      case "Q": // 查詢功能 --
         strAction = "Q";
         queryFunc(); break;
      case "M": //瀏覽功能 :skip-page --
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "L":  //清畫面--
         strAction = "";
         clearFunc(); break;
      case "C2":
         doProcCancel(); break;
      case "C3":
         doApproveAll(); break;
   }

   dddwSelect();
   initButton();
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskp0220")) {
         wp.optionKey = wp.colStr("ex_curr_code");
         dddwList(
               "dddw_dc_curr_code_tw",
               "ptr_sys_idtab", "wf_id", "wf_desc",
               "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
	/*
	if (wp.item_eq("ex_apr_flag", "2")) {
		if (wp.item_empty("ex_add_date1") && wp.item_empty("ex_add_date2")
				&& wp.item_empty("ex_ctrl_seqno")) {
			alertErr("已覆核: [再提示登錄日期,控制流水號] 不可空白");
			return;
		}
	}
	*/
   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("再提示登錄日期起迄：輸入錯誤");
      return;
   }
   if (wp.itemLen("ex_ctrl_seqno") > 0 && wp.itemLen("ex_ctrl_seqno") < 4) {
      alertErr("控制流號: 不可少於4碼");
      return;
   }

   String ls_where = "";
   wp.whereStr = " where 1=1 and chg_stage ='2' "
         + sqlCol(wp.itemStr("ex_add_user"), "rep_add_user", "like%")         
         + sqlCol(wp.itemStr("ex_add_date1"),"rep_add_date",">=")
         + sqlCol(wp.itemStr("ex_add_date2"),"rep_add_date","<=")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
   ;

   if (wp.itemEq("ex_apr_flag", "1")) {
      wp.whereStr += " and rep_add_date<>'' and rep_apr_date=''";
   }
   else {
      wp.whereStr += " and rep_add_date<>'' and rep_apr_date = to_char(sysdate,'yyyymmdd') ";
   }
   
   setSqlParmNoClear(true);
   listSum(wp.whereStr);

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "hex(rowid) as rowid , mod_seqno,"
         + " reference_no,"
         + " reference_seq,"
         + " bin_type,"
         + " debit_flag,"
         + " ctrl_seqno,"
         + " card_no,"
         + " purchase_date,"
         + " uf_dc_curr(curr_code) as curr_code ,"
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt,"
         + " chg_times,"
         + " repsent_date,"
         + " rep_doc_mark,"
         + " uf_dc_amt2(rep_amt_twd,rep_dc_amt) as rep_dc_amt,"
         + " source_amt,"
         + " source_curr,"
         + " rep_msg,"
         + " rep_add_date,"
         + " rep_add_user, rep_add_user as can_user,"
         + " uf_dc_amt2(fst_twd_amt,fst_dc_amt) as fst_dc_amt,"
         + " fst_disb_dc_amt,"
         + " decode(rep_apr_date,'','N','Y') as db_apr_flag"
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by bin_type,ctrl_seqno";

   pageQuery();
   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.setPageValue();
//   set_canAppr("can_user");
}

void listSum(String a_where) throws Exception  {
   String sql1 = "select count(*) as sum_cnt,"
         + " sum(uf_dc_amt2(dest_amt,dc_dest_amt)) as sum_dc_dest_amt,"
         + " sum(uf_dc_amt2(rep_amt_twd,rep_dc_amt)) as sum_rep_dc_amt,"
         + " sum(source_amt) as sum_source_amt "
         + " from rsk_chgback"
         + a_where;

//	wp.ddd(wp.sqlCmd);
   sqlSelect(sql1);
   if (sqlRowNum <= 0)
      return;

   wp.colSet("sum_cnt", sqlInt("sum_cnt"));
   wp.colSet("sum_dc_dest_amt", sqlNum("sum_dc_dest_amt"));
   wp.colSet("sum_rep_dc_amt", sqlNum("sum_rep_dc_amt"));
   wp.colSet("sum_source_amt", sqlNum("sum_source_amt"));
}
//
//void list_wkdate() {
//	for (int ii = 0; ii < wp.selectCnt; ii++) {
//		if (empty(wp.colStr(ii, "rep_apr_date")) == false) {
//			wp.col_set(ii, "wk_approve", "Y");
//		}
//		else {
//			wp.col_set(ii, "wk_approve", "N");
//		}
//	}
//}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   rskm01.RskChgback func = new rskm01.RskChgback();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("reference_seq", kk2);
   rc = func.dataSelect();
   if (rc != 1) {
      alertErr(func.getMsg());
      wp.actionCode = "";
      return;
   }

   // --
   BilBill ooBill = new BilBill();
   ooBill.setConn(wp);
   ooBill.varsSet("reference_no", kk1);
   ooBill.varsSet("bin_type", kk3);
   ooBill.varsSet("debit_flag", wp.colStr("CB_debit_flag"));
   ooBill.dataSelect();

   wp.actionCode = "";
}

@Override
public void dataProcess() throws Exception {
   func = new Rskp0230Func();
   func.setConn(wp);

   String[] aaRowId = wp.itemBuff("rowid");
   String[] aaModSeqno = wp.itemBuff("mod_seqno");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = aaRowId.length;
   if (optToIndex(opt[0])<0) {
      alertErr(appMsg.optApprove);
      return;
   }

   for (int ii = 0; ii < opt.length; ii++) {
      int rr =optToIndex(opt[ii]);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "X");
//      if (!appr_bankUnit(wp.itemStr(rr,"can_user"))) {
//         ilErr++;
//         break;
//      }

      func.varsSet("rowid", aaRowId[rr]);
      func.varsSet("mod_seqno", aaModSeqno[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_msg", func.getMsg());
   }

   alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
}

void doProcCancel() throws Exception  {
   func = new Rskp0230Func();
   func.setConn(wp);

   String[] aaRowId = wp.itemBuff("rowid");
   String[] aaModSeqno = wp.itemBuff("mod_seqno");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("rowid");

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("rowid", aaRowId[rr]);
      func.varsSet("mod_seqno", aaModSeqno[rr]);
      rc = func.cancelProc();

      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      wp.colSet(rr, "ok_msg", func.getMsg());
   }

   alertMsg("[解覆核]處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
}

@Override
public void initButton() {
   if (wp.respHtml.equals("rskp0230")) {
      this.btnAddOn(wp.itemEq("ex_apr_flag", "1"));
      this.btnDeleteOn(wp.itemEq("ex_apr_flag", "2"));
      if (wp.colNum("sum_cnt") <=wp.pageRows|| wp.itemEq("ex_apr_flag","2") ) {
         buttonOff("btnproc3_off");
      }
   }
}

void doApproveAll() throws Exception {
   wp.listCount[0] =wp.itemRows("rowid");
   wp.pageControl();

   String sql1 ="select hex(rowid) as rowid, mod_seqno"+
         ", rep_add_user as can_user"+
         " from rsk_chgback "+wp.queryWhere+
         " and chg_stage ='2' and rep_add_date<>'' and rep_apr_date=''"+  //待覆核--
         " order by ctrl_seqno";
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      alertErr("無資料可覆核");
      return;
   }

   func = new Rskp0230Func();
   func.setConn(wp);

   int ll_ok=0, ll_err=0;
   int ll_nrow =sqlRowNum;
   for (int ii = 0; ii < ll_nrow; ii++) {
      if (!apprBankUnit(sqlStr(ii,"can_user"),wp.loginUser)) {
         ll_err++;
         continue;
      }

      func.varsSet("rowid", sqlStr(ii,"rowid"));
      func.varsSet("mod_seqno", sqlStr(ii,"mod_seqno"));
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         ll_ok++;
         continue;
      }
      else ll_err++;
   }
   alertMsg("覆核處理: 成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err);
   //--
   wp.listCount[0] =0;
   queryFunc();
}

}
