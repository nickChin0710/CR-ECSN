package rskm01;
/**
 * 2020-1019   JH    all_appr.bankunit
 * 2020-0528   JH    ++[all-approve]
 * 2019-0926   JH    合計
 * 扣款撥款整批登錄覆核-主管作業
 */

public class Rskp0220 extends ofcapp.BaseAction {
Rskp0220Func func;
int il_ok = 0;
int il_err = 0;
String kk1 = "", kk2 = "", kk3 = "";


@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "C":
         // -資料處理-
         procFunc(); break;
      case "Q":
         /* 查詢功能 */
         strAction = "Q";
         queryFunc(); break;
      case "M":
         /* 瀏覽功能 :skip-page */
         queryRead(); break;
      case "S":
         /* 動態查詢 */
         querySelect(); break;
      case "L":
         /* 清畫面 */
         strAction = "";
         clearFunc(); break;
      case "C2":
         cancelFunc(); break;
      case "C3":
         doApproveAll(); break;
   }

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
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("撥款登錄日期起迄：輸入錯誤");
      return;
   }
/*	
	if (wp.item_eq("ex_apr_flag","Y") && wp.item_empty("ex_date1")) {
		alertErr("[撥款登錄日期起]: 不可空白");
		return;
	}
*/
   wp.whereStr =
         " where 1=1 and chg_stage in ('1','3') and sub_stage ='30' and fst_disb_amt >0  "
               + sqlCol(wp.itemStr("ex_add_user"), "fst_disb_add_user", "like%")
//               + commSqlStr.strend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "fst_disb_add_date")
               + sqlCol(wp.itemStr("ex_date1"),"fst_disb_add_date",">=")
               + sqlCol(wp.itemStr("ex_date2"),"fst_disb_add_date","<=")
               + sqlCol(wp.itemStr("ex_curr_code"), "curr_code")
               + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
   ;

   if (wp.itemEq("ex_apr_flag", "Y")) {
      wp.whereStr += " and fst_disb_apr_date = to_char(sysdate,'yyyymmdd') ";
   }
   else {
      wp.whereStr += " and fst_disb_apr_date =''";
   }
   
   setSqlParmNoClear(true);
   queryTotal(wp.whereStr);
//   if (wp.colInt("th_cnt") == 0)
//      return;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();
   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "hex(rowid) as rowid , mod_seqno,"
         + " ctrl_seqno, bin_type,"
         + " reference_no,"
         + " reference_seq,"
         + " chg_stage,"
         + " sub_stage,"
         + " chg_times,"
         + " card_no,"
         + " purchase_date,"
         + " dest_amt,"
         + " fst_disb_amt,"
         + " fst_disb_add_date,"
         + " fst_disb_add_user, fst_disb_add_user as can_user,"
         + " debit_flag,"
         + " fst_disb_apr_date,"
         + " fst_gl_date,"
         + " uf_dc_curr(curr_code) as curr_code, "
         + " uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt,"
         + " uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_dc_amt,"
         + " fst_disb_apr_date,"
         + " uf_dc_amt2(fst_disb_amt,fst_disb_dc_amt) - uf_dc_amt2(fst_twd_amt,fst_dc_amt) as db_diff_amt,"
         + " '' as xxx"
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by bin_type,ctrl_seqno";
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr(appMsg.errDataNodata);
      return;
   }
   wp.setPageValue();
//   set_canAppr("can_user");
}

void queryTotal(String aWhere) {
   wp.colSet("th_cnt", 0);
   wp.colSet("th_dc_amt", 0);
   wp.colSet("th_disb_amt", 0);
   wp.colSet("th_diff_amt", 0);

   String sql1 = "select count(*) as th_cnt" +
         ", sum(uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt)) as th_disb_amt" +
         ", sum(uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt)) as th_dc_amt" +
         " from rsk_chgback" + aWhere;

   sqlSelect(sql1);
   if (sqlRowNum > 0) {
      wp.colSet("th_cnt", sqlInt("th_cnt"));
      wp.colSet("th_dc_amt", sqlNum("th_dc_amt"));
      wp.colSet("th_disb_amt", sqlNum("th_disb_amt"));
      wp.colSet("th_diff_amt", sqlNum("th_dc_amt") - sqlNum("th_disb_amt"));
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

//-取消覆核-
void cancelFunc() throws Exception {
   func = new Rskp0220Func();
   func.setConn(wp);

   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaOpt = wp.itemBuff("opt");
   int llRowCnt = wp.itemRows("rowid");
   wp.listCount[0] = llRowCnt;
   keepListOption(llRowCnt, "opt");

   int rr = -1;
   for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = optToIndex(aaOpt[ii]);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", wp.itemStr(rr, "mod_seqno"));
      rc = func.cancelProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         il_ok++;
         continue;
      }
      il_err++;
      wp.colSet(rr, "ok_flag", "X");
   }

   alertMsg("解覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   func = new Rskp0220Func();
   func.setConn(wp);

   String[] aaRowid = wp.itemBuff("rowid");
   String[] aa_mod_seqno = wp.itemBuff("mod_seqno");
   String[] aaOpt = wp.itemBuff("opt");
   int ll_row_cnt = wp.itemRows("rowid");
   wp.listCount[0] = ll_row_cnt;
   if (ll_row_cnt <= 0)
      return;
   this.checkBoxOptOn(ll_row_cnt, aaOpt);

   int rr = -1;
   for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = optToIndex(aaOpt[ii]);
      if (rr < 0) {
         continue;
      }

      wp.colSet(rr, "ok_flag", "X");
//      if (!appr_bankUnit(wp.itemStr(rr,"can_user"))) {
//         il_err++;
//         break;
//      }

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aa_mod_seqno[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         il_ok++;
         continue;
      }

      il_err++;
   }

   alertMsg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

@Override
public void initButton() {
   if (wp.respHtml.equals("rskp0220")) {
      this.btnAddOn(wp.itemEq("ex_apr_flag", "N"));
      this.btnDeleteOn(wp.itemEq("ex_apr_flag", "Y"));
   }
   if (wp.colNum("th_cnt") <=wp.pageRows || wp.itemEq("ex_apr_flag","Y") ) {
      buttonOff("btnproc3_off");
   }
}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

void doApproveAll() throws Exception {
   wp.listCount[0] =wp.itemRows("rowid");
   wp.pageControl();
   log("where="+wp.queryWhere);

   String sql1 ="select hex(rowid) as rowid, mod_seqno"+
         ", ctrl_seqno, reference_no, fst_disb_add_user as can_user"+
         " from rsk_chgback "+wp.queryWhere+
         " and chg_stage in ('1','3') and sub_stage ='30' and fst_disb_amt >0  "+  //待覆核--
         " order by ctrl_seqno";
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      alertErr("無資料可覆核");
      return;
   }

   func = new Rskp0220Func();
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
