package rskm01;
/**
 * 2020-1019   JH    all_approve.check_bankunit
 * 2020-0518   JH    ++approve_all
 * 2020-0413	JH		modify
 * 2020-0210	JH		--結案條件
 */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Rskp0210 extends BaseProc {
Rskp0210Func func;
int il_ok = 0;
int il_err = 0;
String kk1 = "", kk2 = "", kk3 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "Q": //查詢功能
         queryFunc();
         break;
      case "R": //
         dataRead(); break;
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
      case "C": // -資料處理-
         dataProcess();
         break;
      case "C2": //approve_ALL--
         doApproveAll(); break;
   }

   dddwSelect();
   initButton();
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("[新增登錄日期]：起迄輸入錯誤");
      return;
   }

   wp.whereStr = " where chg_stage in ('1','3') and sub_stage in ('10')"
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_add_user"), "fst_add_user", "like%")
         + sqlCol(wp.itemStr("ex_add_date1"),"fst_add_date",">=")
         + sqlCol(wp.itemStr("ex_add_date2"),"fst_add_date","<=")
         ;
   
   if (wp.itemEq("ex_debit_flag", "Y")) {
      wp.whereStr += " and debit_flag = 'Y'";
   }
   else if (wp.itemEq("ex_debit_flag", "N")) {
      wp.whereStr += " and debit_flag <> 'Y'";
   }
   setSqlParmNoClear(true);
   sum(wp.whereStr);

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

void sum(String lsWhere) throws Exception  {
   String sql1 = " select "
         + " count(*) as tl_cnt , "
         + " sum(fst_dc_amt) as tl_fst_dc_amt "
         + " from rsk_chgback "
         + lsWhere;
   sqlSelect(sql1);

   if (sqlNum("tl_cnt") == 0) {
      wp.colSet("tl_cnt", "0");
      wp.colSet("tl_fst_dc_amt", "0");
   }
   else {
      wp.colSet("tl_cnt", "" + sqlNum("tl_cnt"));
      wp.colSet("tl_fst_dc_amt", "" + sqlNum("tl_fst_dc_amt"));
   }

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "card_no ,"
         + " purchase_date,"
         + " ctrl_seqno,"
         + " chg_times,"
         + " fst_reason_code,"
         + " fst_doc_mark,"
         + " fst_amount,"
         + " fst_msg,"
         + " fst_add_date,"
         + " fst_add_user,"
         + " reference_no,"
         + " reference_seq,"
         + " fst_status,"
         + " fst_apr_date,"
         + " fst_apr_user,"
         + " sub_stage,"
         + " chg_stage,"
         + " bin_type,"
         + " source_amt,"
         + " source_curr,"
         + " debit_flag,"
         + " uf_nvl(curr_code,'901') as curr_code,"
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt,"
         + " fst_twd_amt,"
         + " fst_dc_amt,"
         + " v_card_no,"
         + " txn_code,"
         + " hex(rowid) as rowid, mod_seqno"
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by ctrl_seqno";
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
      return;
   }

   wp.setPageValue();
//   set_canAppr("fst_add_user");
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
   RskChgback func = new RskChgback();
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
   func.getRefnoOri(kk1);
   BilBill ooBill = new BilBill();
   ooBill.setConn(wp);
   ooBill.varsSet("reference_no", func.refnoOri);
   ooBill.varsSet("bin_type", kk3);
   ooBill.varsSet("debit_flag", wp.colStr("CB_debit_flag"));
   ooBill.dataSelect();

   wp.actionCode = "";
}

@Override
public void dataProcess() throws Exception {
   func = new Rskp0210Func();
   func.setConn(wp);

   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModseq = wp.itemBuff("mod_seqno");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("rowid");
   if (optToIndex(opt[0])<0) {
      alertErr(appMsg.optApprove);
      return;
   }

   this.optNumKeep(wp.itemRows("rowid"), opt);

   for (int ii = 0; ii < opt.length; ii++) {
      int rr=optToIndex(opt[ii]);
      if (rr < 0)
         continue;

      wp.colSet(rr, "ok_flag", "X");
      if (!apprBankUnit(wp.itemStr(rr,"fst_add_user"),wp.loginUser)) {
         il_err++;
         break;
      }

      func.varsSet("kk-rowid", aaRowid[rr]);
      func.varsSet("kk-mod_seqno", aaModseq[rr]);
      func.varsSet("kk-ctrl_seqno", wp.colStr(rr, "ctrl_seqno"));
      func.varsSet("kk-reference_no", wp.colStr(rr, "reference_no"));

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         il_ok++;
         continue;
      }
      il_err++;
      wp.colSet(rr, "ok_flag", "X");
      wp.log("err[%s], kk[%s]", func.getMsg(), wp.colStr(rr, "ctrl_seqno"));
   }

   // -re-Query-
   // queryRead();
   alertMsg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

void doApproveAll() throws Exception {
   wp.listCount[0] =wp.itemRows("rowid");
   wp.pageControl();
   log("where="+wp.queryWhere);

   String sql1 ="select hex(rowid) as rowid, mod_seqno"+
         ", ctrl_seqno, reference_no, fst_add_user as can_user"+
         " from rsk_chgback"+wp.queryWhere+
         " and chg_stage in ('1','3') and sub_stage in ('10')"+  //待覆核--
         " order by ctrl_seqno";
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      alertErr("無資料可覆核");
      return;
   }

   func = new Rskp0210Func();
   func.setConn(wp);

   int ll_nrow =sqlRowNum;
   for (int ii = 0; ii < ll_nrow; ii++) {
      if (!apprBankUnit(sqlStr(ii,"can_user"),wp.loginUser)) {
         il_err++;
         continue;
      }

      func.varsSet("kk-rowid", sqlStr(ii,"rowid"));
      func.varsSet("kk-mod_seqno", sqlStr(ii,"mod_seqno"));
      func.varsSet("kk-ctrl_seqno", sqlStr(ii, "ctrl_seqno"));
      func.varsSet("kk-reference_no", sqlStr(ii, "reference_no"));
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         il_ok++;
         continue;
      }
      il_err++;
   }
   //--
   queryFunc();
//   wp.alert_Clear();
   wp.alertMesg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

@Override
public void initButton() {
   if (wp.colNum("tl_cnt") <=wp.pageRows) {
      buttonOff("btnproc2_off");
   }
}

}
