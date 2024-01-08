package rskm01;
/**
 * 2021-1123   JH    9090: 排序造成同一筆資料出現在不同頁面
 * 2021-0810.8258    JH    請更改負項交易(TC06.TC26) 不能執行調單.問交(整批).扣款(整批).預備依從.預備仲裁
 * 2021-0718   JH    bug:card_no
 * 2021-0115.5593    JH    ++合計
 * 2020-1015   JH    modify
 * 2020-0515   JH    modify
 * 2020-0506   JH    html.change
 * 2020-0505   JH    acct_key like%
 * 2020-0430   JH    PRBL.clo_result
 * Debit卡消費帳單查詢及處理{列問交}
 */

import busi.func.ColFunc;
import ofcapp.BaseAction;

public class Rskp1010 extends BaseAction {
String kk1 = "", isReferNo = "", isCtrlSeqno = "";
int _proc_close = 0;

@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   if (eqIgno(wp.requHtml,"rskp1010")) {
      switch (wp.buttonCode) {
         case "S":
            if (wp.respHtml.indexOf("_bill") > 0) {
               querySelect();  //--
               return;
            }
            strAction = "R";
            //--
            if (procCheckOpt() != 1) {
               return;
            }
            if (wp.respHtml.indexOf("_prbl") > 0)
               dataReadPrbl();
            else if (wp.respHtml.indexOf("_rept") > 0)
               dataReadRept();
            else if (wp.respHtml.indexOf("_chgb") > 0)
               dataReadChgb();
            else if (wp.respHtml.indexOf("_arbit") > 0)
               dataReadArbit();
            else if (wp.respHtml.indexOf("_compl") > 0)
               dataReadCompl();
            break;
         case "Q": //-查詢功能-
            queryFunc(); break;
         case "M": //瀏覽功能 :skip-page--
            queryRead(); break;
         case "L": //-清畫面-
            strAction ="";
            clearFunc(); break;
      }
      if (wp.respHtml.indexOf("_arbit") > 0) {
         wp.respHtml = "rskp1010_arbit_v";
      }
      return;
   }
   //--
   if (wp.requHtml.indexOf("_bill") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataRead(); break;
      }
      return;
   }
   //--
   if (wp.requHtml.indexOf("_prbl") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataReadPrbl(); break;
         case "A":
         case "U":
         case "D":
            dbSaveProblem(); break;
      }
      return;
   }
   //--
   if (wp.requHtml.indexOf("_rept") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataReadRept(); break;
         case "A":
         case "U":
         case "D":
            dbSaveReceipt(); break;
      }
      return;
   }
   //--
   if (wp.requHtml.indexOf("_chgb") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataReadChgb(); break;
         case "A":
         case "U":
         case "D":
            dbSaveChgback(); break;
      }
      return;
   }
   //--
   if (wp.requHtml.indexOf("_arbit") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataReadArbit(); break;
         case "A":
         case "U":
         case "D":
            dbSaveArbit(); break;
         case "U2":
         case "D2": //依從修改
            dbSaveArbit(); break;
      }
      return;
   }
   //--
   if (wp.requHtml.indexOf("_compl") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataReadCompl();
            break;
         case "A":
         case "U":
         case "D":
            dbSaveCompl();
            break;
         case "U2":
         case "D2": //依從修改
            dbSaveCompl();
            break;
         case "C4": //-預備依從-結案-
            _proc_close = 1;
            procPreCompl();
            break;
         case "C5": // -依從權-結案-
            _proc_close = 2;
            procPreCompl();
            break;
      }
      return;
   }
}

void dbSaveProblem() throws Exception {
   rskm01.Rskm0010Func func = new rskm01.Rskm0010Func();
   func.setConn(wp);

   if (eqIgno(strAction, "A")) {
      rc = func.dbInsert();
      if (rc == 1) {
         this.alertMsg("問交新增成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "U")) {
      rc = func.dbUpdate();
      if (rc == 1) {
         this.alertMsg("問交修改成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "D")) {
      rc = func.dbDelete();
      if (rc == 1) {
         this.alertMsg("問交刪除成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }

   isReferNo = wp.itemStr("reference_no");
   this.saveAfter(true);
}

void dbSaveReceipt() throws Exception {
   // -理由碼-
   if (wp.itemEmpty("db_reason_code") == false) {
      wp.itemSet("reason_code", wp.itemStr("db_reason_code"));
   }

   rskm01.Rskm0110Func func = new rskm01.Rskm0110Func();
   func.setConn(wp);
   if (eqIgno(strAction, "A")) {
      rc = func.dbInsert();
      if (rc == 1) {
         this.alertMsg("調單新增成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "U")) {
      rc = func.dbUpdate();
      if (rc == 1) {
         this.alertMsg("調單修改成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "D")) {
      rc = func.dbDelete();
      if (rc == 1) {
         this.alertMsg("調單刪除成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }

   isReferNo = wp.itemStr("reference_no");
   this.saveAfter(true);
}

void dbSaveChgback() throws Exception {
   rskm01.Rskm0210Func func = new rskm01.Rskm0210Func();
   func.setConn(wp);
   if (eqIgno(strAction, "A")) {
      rc = func.dbInsert();
      if (rc == 1) {
         this.alertMsg("扣款新增成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "U")) {
      rc = func.dbUpdate();
      if (rc == 1) {
         this.alertMsg("扣款修改成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }
   else if (eqIgno(strAction, "D")) {
      rc = func.dbDelete();
      if (rc == 1) {
         this.alertMsg("扣款刪除成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
      }
   }

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }

   isReferNo = wp.itemStr("reference_no");
   this.saveAfter(true);
}

void dbSaveArbit() throws Exception {
   rskm01.Rskm0250Func func = new rskm01.Rskm0250Func();
   func.setConn(wp);
   switch (strAction) {
      case "A":
         rc = func.dbInsert();
         if (rc == 1) {
            this.alertMsg("預備仲裁新增成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "U":
         rc = func.updateU1();
         if (rc == 1) {
            this.alertMsg("(預備)仲裁修改成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "D":
         rc = func.dbDelete();
         if (rc == 1) {
            this.alertMsg("(預備)仲裁刪除成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "U2":
         rc = func.updateU2();
         if (rc==1) {
            alertMsg("仲裁存檔成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "D2":
         rc = func.deleteD2();
         if (rc==1) {
            alertMsg("仲裁取消成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
   }

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }

   isReferNo = wp.itemStr("reference_no");
   dataReadArbit();
}

void dbSaveCompl() throws Exception {
   rskm01.Rskm0450Func func = new rskm01.Rskm0450Func();
   func.setConn(wp);
   switch (strAction) {
      case "A":
         rc = func.dbInsert();
         if (rc == 1) {
            this.alertMsg("預備依從: 新增成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "U":
         rc = func.updateU1();
         if (rc == 1) {
            this.alertMsg("(預備)依從: 修改成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "D":
         rc = func.dbDelete();
         if (rc == 1) {
            this.alertMsg("(預備)依從: 刪除成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "U2":
         rc =func.updateU2();
         if (rc == 1) {
            this.alertMsg("依從: 修改成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
      case "D2":
         rc = func.deleteD2();
         if (rc==1) {
            alertMsg("依從取消成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
         }
         break;
   }

   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }

   isReferNo = wp.itemStr("reference_no");
   dataReadCompl();
}

int procCheckOpt() {
   String[] aa_refno = wp.getInBuffer("reference_no");
   String[] opt = wp.itemBuff("opt");
   int rr = optToIndex(opt[0]);

   if (opt == null || opt.length == 0 || rr < 0) {
      alertErr("未點選列問交資料");
      wp.notFound = "Y";
      return -1;
   }

   //請更改負項交易(TC06.TC26);不能執行調單.問交(整批).扣款(整批).預備依從.預備仲裁
   String ls_sign =wp.itemStr(rr,"sign_flag");
   if (wp.respHtml.indexOf("_rept")>0 ||
           wp.respHtml.indexOf("_arbit")>0 || wp.respHtml.indexOf("_compl")>0 ) {
      if (eqIgno(ls_sign,"-")) {
         alertErr("負項交易, 不可[調單, 仲裁, 依從權]");
         return -1;
      }
   }

   isReferNo = aa_refno[rr];
   isCtrlSeqno = wp.itemStr(rr, "rsk_ctrl_seqno");

   return 1;
}

void dataReadPrbl() throws Exception {
   if (selectBilBill() != 1) {
      wp.notFound = "Y";
      return;
   }

   selectRskProblem();
   if (wp.colEmpty("bin_type"))
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));


   return;
}

void dataReadRept() throws Exception {
   rskm01.Rskp1010Func func = new rskm01.Rskp1010Func();
   func.setConn(wp);
   func.varsSet("reference_no", isReferNo);

   if (func.reptSelect() != 1) {
      alertErr(func.getMsg());
      return;
   }

   if (selectBilBill() != 1) {
      wp.notFound = "Y";
      return;
   }

   selectRskReceipt();

   return;
}

void dataReadChgb() throws Exception {
   rskm01.Rskp1010Func func = new rskm01.Rskp1010Func();
   func.setConn(wp);
   func.varsSet("reference_no", isReferNo);

   if (func.chgbSelect() != 1) {
      alertErr(func.getMsg());
      return;
   }

   if (selectBilBill() != 1) {
      wp.notFound = "Y";
      return;
   }

   selectRskChgback();

   return;
}

void dataReadArbit() throws Exception {
   rskm01.Rskp1010Func func = new rskm01.Rskp1010Func();
   func.setConn(wp);

   if (empty(isReferNo))
      isReferNo = wp.itemStr("reference_no");
   if (empty(isCtrlSeqno))
      isCtrlSeqno = wp.itemStr("ctrl_seqno");

   func.varsSet("reference_no", isReferNo);
   func.varsSet("ctrl_seqno", isCtrlSeqno);

   if (func.arbitSelect() != 1) {
      alertErr(func.getMsg());
      return;
   }

   if (selectBilBill() != 1) {
      wp.notFound = "Y";
      // this.select_OK();
      return;
   }

   wp.sqlCmd = "select A.*"
         + ", hex(rowid) as rowid"
         + " from rsk_prearbit A"
         + " where 1=1"
         + sqlCol(isReferNo, "reference_no")
         + sqlCol(isCtrlSeqno, "ctrl_seqno")
   ;
   this.pageSelect();
   if (sqlRowNum <= 0) {
      selectOK();
      wp.colSet("reference_no ", wp.colStr("BL_reference_no"));
      wp.colSet("reference_no_ori ", wp.colStr("BL_reference_no_ori"));
      wp.colSet("reference_seq", 0);
      wp.colSet("ctrl_seqno   ", wp.colStr("BL_rsk_ctrl_seqno"));
      wp.colSet("bin_type     ", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag   ", wp.colStr("BL_debit_flag"));
      wp.colSet("card_no      ", wp.colStr("BL_card_no"));
      wp.colSet("acct_month   ", wp.colStr("BL_acct_month"));
      wp.colSet("purchase_date", wp.colStr("BL_purchase_date"));
      wp.colSet("mcht_country ", wp.colStr("BL_mcht_country"));
      wp.colSet("film_no      ", wp.colStr("BL_film_no"));
      wp.colSet("auth_code    ", wp.colStr("BL_auth_code"));
      wp.colSet("source_curr  ", wp.colStr("BL_source_curr"));
      wp.colSet("source_amt   ", wp.colStr("BL_source_amt"));
      wp.colSet("curr_code    ", wp.colStr("BL_curr_code"));
      wp.colSet("dc_dest_amt  ", wp.colStr("BL_dc_dest_amt"));
      wp.colSet("dest_amt     ", wp.colStr("BL_dest_amt"));
      wp.colSet("settl_amt    ", wp.colStr("BL_settl_amt"));
      wp.colSet("reference_no_ori", wp.colStr("BL_reference_no_ori"));

      wp.colSet("arbit_times", "1");
      wp.colSet("pre_status", "10");
      wp.colSet("pre_event_date", wp.sysDate);

 //      rskFunc func2 = new rskFunc();
//      func2.setConn(wp.getConn());
//      String ls_expr_date =
//            func2.arbit_expire_date(wp.colStr("bin_type"), wp.colStr("BL_txn_code"), "");
//      wp.col_set("pre_expire_date", ls_expr_date);
   }

   return;
}

void dataReadCompl() throws Exception  {
   rskm01.Rskp1010Func func = new rskm01.Rskp1010Func();
   func.setConn(wp);

   if (empty(isReferNo))
      isReferNo = wp.itemStr("reference_no");

   func.varsSet("reference_no", isReferNo);

   if (func.complSelect() != 1) {
      alertErr(func.getMsg());
      return;
   }

   if (selectBilBill() != 1) {
      wp.notFound = "Y";
      // this.select_OK();
      return;
   }

   wp.sqlCmd = "select A.*"
         + ", hex(rowid) as rowid"
         + " from rsk_precompl A"
         + " where reference_seq =0"
         + sqlCol(isReferNo, "reference_no");

   this.pageSelect();
   if (sqlRowNum <= 0) {
      selectOK();

      wp.colSet("reference_no ", wp.colStr("BL_reference_no"));
      wp.colSet("reference_no_ori ", wp.colStr("BL_reference_no_ori"));
      wp.colSet("reference_seq", 0);
      wp.colSet("ctrl_seqno   ", wp.colStr("BL_rsk_ctrl_seqno"));
      wp.colSet("bin_type     ", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag   ", wp.colStr("BL_debit_flag"));
      wp.colSet("card_no      ", wp.colStr("BL_card_no"));
      wp.colSet("acct_month   ", wp.colStr("BL_acct_month"));
      wp.colSet("purchase_date", wp.colStr("BL_purchase_date"));
      wp.colSet("mcht_country ", wp.colStr("BL_mcht_country"));
      wp.colSet("film_no      ", wp.colStr("BL_film_no"));
      wp.colSet("auth_code    ", wp.colStr("BL_auth_code"));
      wp.colSet("source_curr  ", wp.colStr("BL_source_curr"));
      wp.colSet("source_amt   ", wp.colStr("BL_source_amt"));
      wp.colSet("curr_code    ", wp.colStr("BL_curr_code"));
      wp.colSet("dc_dest_amt  ", wp.colStr("BL_dc_dest_amt"));
      wp.colSet("dest_amt     ", wp.colStr("BL_dest_amt"));
      wp.colSet("settl_amt    ", wp.colStr("BL_settl_amt"));
      wp.colSet("reference_no_ori", wp.colStr("BL_reference_no_ori"));

      wp.colSet("compl_times", "1");
      wp.colSet("pre_status", "10");
      wp.colSet("event_date",wp.sysDate);

      // -expire_date-
//      rskFunc func2 = new rskFunc();
//      func2.setConn(wp.getConn());
//      String ls_expr_date =
//            func2.compl_expire_date(wp.colStr("bin_type"), wp.colStr("BL_txn_code"), "");
//      wp.col_set("pre_expire_date", ls_expr_date);
   }

   return;
}

int selectRskProblem() throws Exception {

   wp.whereStr = " where A.reference_no =?"
         + " and A.reference_seq =0";
   wp.daoTable = "rsk_problem A";
   wp.selectSQL = "hex(A.rowid) as rowid , A.* , decode(A.back_status,'','','S','成功','F','失敗') as tt_back_status ";

   setString(1, isReferNo);
   // sql_ddd();
   pageSelect();
   if (sqlRowNum <= 0) {
      wp.colSet("reference_no", isReferNo);
      wp.colSet("reference_seq", 0);
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag", wp.colStr("BL_debit_flag"));
      wp.colSet("prb_mark", "Q");
      wp.colSet("prb_src_code", "RQ");
      wp.colSet("prb_status", "10");
      wp.colSet("prb_fraud_rpt", "0");
      wp.colSet("card_no", wp.colStr("BL_card_no"));
      wp.colSet("curr_code", wp.colStr("BL_curr_code"));
      wp.colSet("dc_prb_amount", (int) (wp.colNum("BL_dest_amt")));
      wp.colSet("prb_reason_code", "4D");
      // --
      wp.notFound = "";
   }

   wp.log("add_user=" + wp.itemStr("add_user"));
   return 1;
}

int selectRskReceipt() throws Exception {

   wp.whereStr = " where A.reference_no =?"
         + " and A.reference_seq =0";
   wp.daoTable = "rsk_receipt A";
   wp.selectSQL = "hex(A.rowid) as rowid"
         + ", reason_code as db_reason_code"
         + ", A.*";
   setString(1, isReferNo);
   // sql_ddd();
   pageSelect();
   if (sqlRowNum <= 0) {
      wp.colSet("reference_no", isReferNo);
      wp.colSet("reference_seq", 0);
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag", wp.colNvl("BL_debit_flag", "N"));
      wp.colSet("card_no", wp.colStr("BL_card_no"));
      wp.colSet("curr_code", wp.colStr("BL_curr_code"));
      wp.colSet("ctrl_seqno", wp.colStr("BL_rsk_ctrl_seqno"));
      wp.colSet("post_date", wp.colStr("BL_post_date"));
      wp.colSet("purchase_date", wp.colStr("BL_purchase_date"));
      wp.colSet("film_no", wp.colStr("BL_film_no"));
      wp.colSet("reference_no_ori", wp.colStr("BL_reference_no_ori"));
      wp.colSet("contract_no", wp.colStr("BL_contract_no"));

      // --
      wp.colSet("rept_status", "10");
      wp.colSet("rept_seqno", "001");
      wp.colSet("add_user", this.loginUser());
      wp.colSet("reason_code","30");
      wp.colSet("db_reason_code","30");

      wp.notFound = "";
   }

   // wp.ddd("add_user="+wp.itemStr("add_user"));
   return 1;
}

void selectRskChgback() throws Exception {
   rskm01.Rskm0210Func chgb = new rskm01.Rskm0210Func();
   chgb.setConn(wp);
   chgb.varsSet("reference_no", isReferNo);
   sqlRowNum = chgb.dataSelect();

   // wp.sqlCmd =chgb.dataSelect();
   // pageSelect();
   if (sqlRowNum <= 0) {
      wp.colSet("reference_no", isReferNo);
      wp.colSet("reference_seq", 0);
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag", wp.colNvl("BL_debit_flag", "N"));
      wp.colSet("card_no", wp.colStr("BL_card_no"));
      wp.colSet("curr_code", wp.colStr("BL_curr_code"));
      wp.colSet("ctrl_seqno", wp.colStr("BL_rsk_ctrl_seqno"));
      wp.colSet("post_date", wp.colStr("BL_post_date"));
      wp.colSet("purchase_date", wp.colStr("BL_purchase_date"));
      wp.colSet("film_no", wp.colStr("BL_film_no"));
      wp.colSet("reference_no_ori", wp.colStr("BL_reference_no_ori"));
      wp.colSet("contract_no", wp.colStr("BL_contract_no"));

      // --
      wp.colSet("chg_stage", "1");
      wp.colSet("sub_stage", "10");
      wp.colSet("fst_status", "10");
      wp.colSet("fst_add_user", this.loginUser());

      wp.notFound = "";
   }
}

int selectBilBill() throws Exception  {
   BilBill bill = new BilBill();
   bill.setConn(wp);

   if (empty(wp.colStr("reference_no_ori")) == false) {
      bill.varsSet("reference_no", wp.colStr("reference_no_ori"));
   }
   else {
      bill.varsSet("reference_no", isReferNo);
   }

   bill.varsSet("debit_flag", "Y");
   if (bill.dataSelect() == -1) {
      alertErr(bill.getMsg());
      return -1;
   }

   return 1;
}

@Override
public void dddwSelect() {
   try {
      // -問交-
      if (eqIgno(wp.respHtml, "rskp1010_prbl")) {
         wp.optionKey = wp.colStr("prb_reason_code");
         dddwList(
               "dddw_prb_reason_code",
               "ptr_sys_idtab",
               "wf_id",
               "wf_desc",
               "where wf_type='PRBL-REASON-CODE' and id_code2 like 'VD%'");
      }

      // -調單-
      if (wp.respHtml.indexOf("_rept") > 0) {
         wp.optionKey = wp.colStr("reason_code");
         dddwList(
               "dddw_reason_code",
               "ptr_sys_idtab", "wf_id", "wf_desc",
               "where wf_type='RECEIPT-REASON-CODE' and id_code2 in ('','V')");
      }
      //-預備仲裁-
      if (wp.respHtml.indexOf("_arbit_v")>0) {
         wp.optionKey =wp.colStr("pre_result");
         wp.colSet("ddlb_pre_result",ecsfunc.DeCodeRsk.arbitPreResult(wp.optionKey,true));
         wp.optionKey =wp.colStr("arb_result");
         wp.colSet("ddlb_arb_result",ecsfunc.DeCodeRsk.arbitCloResult(wp.optionKey,true));
      }
      else if (eqIgno(wp.respHtml,"rskp0010_arbit")) {
         wp.optionKey =wp.colStr("pre_result");
         wp.colSet("ddlb_pre_result",ecsfunc.DeCodeRsk.arbitPreResult(wp.optionKey,true));
      }
      //-依從權-
      if (wp.respHtml.indexOf("_compl")>0) {
         wp.optionKey =wp.colStr("pre_clo_result");
         wp.colSet("ddlb_pre_clo_result",ecsfunc.DeCodeRsk.complPreResult(wp.optionKey,true));
         wp.optionKey =wp.colStr("com_clo_result");
         wp.colSet("ddlb_com_clo_result",ecsfunc.DeCodeRsk.complCloResult(wp.optionKey,true));
      }
   }
   catch (Exception ex) {
   }

}

void colAuthQuery() throws Exception {
   if (commString.strIn(wp.buttonCode,",Q,R")) {
      ColFunc colfunc=new ColFunc();
      colfunc.setConn(wp);

      String kk=wp.itemStr("ex_acct_key");
      if (empty(kk))
         kk =wp.itemStr("ex_card_no");
      if (colfunc.fAuthQuery(wp.modPgm(),kk) !=1) {
         alertErr(colfunc.getMsg());
         return;
      }
   }
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("入帳日 : 起迄錯誤");
      return;
   }
   String lsKey = "";
   int liLen = wp.itemLen("ex_acct_key");
   if (liLen > 0 && liLen < 8) {
      alertErr("[帳戶帳號] 不可小於 8 碼");
      return;
   }

   colAuthQuery();
   if (rc !=1) return;

   if (!empty(wp.itemStr("ex_acct_key"))) {
      lsKey = wp.itemStr("ex_acct_key");
      String sql1 = " select "
            + " p_seqno , "
            + " uf_vd_acno_name(p_seqno) as ex_name,"
            + " acct_key"
            + " from dba_acno "
            + " where 1=1"
            + sqlCol(lsKey, "acct_key", "like%")
            + commSqlStr.rownum(1);
      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         return;
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      wp.colSet("ex_name", sqlStr("ex_name"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
   }
   else if (!empty(wp.itemStr("ex_card_no"))) {
      lsKey =wp.itemStr("ex_card_no");
      String sql1 = " select "
            + " p_seqno,"
            + " uf_vd_acno_name(p_seqno) as ex_name,"
            + " uf_vd_acno_key(p_seqno) as acct_key"
            + " from dbc_card "
            + " where card_no =?";
      setString(1, lsKey);
      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         return;
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      wp.colSet("ex_name", sqlStr("ex_name"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
   }
   else {
      alertErr("帳戶帳號 , 卡號 : 不可同時空白 !");
      return;
   }

   if (logQueryIdno("Y", lsKey) == false) {
      return;
   }

   String lsWhere = " where 1=1 and A.txn_code not in "
         + "('65','66','67','69','85','86','87','89','CD','DF','HC','IF','LF',"
         + "'LP','LS','RB','RR','AF','AI','BF','CF','TX','VF','VP','VR','VT') "
         + " and ((A.rsk_type <>'' and A.bill_type<>'NCFC') or rsk_type='' ) "
//         + sql_col(wp.colStr("ex_p_seqno"), "A.p_seqno")
         + sqlCol(wp.itemStr("ex_date1"), "A.post_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "A.post_date", "<=");
   String ls_acct_key =wp.itemStr("ex_acct_key");
   if (empty(ls_acct_key)) {
      lsWhere +=sqlCol(wp.colStr("ex_p_seqno"),"A.p_seqno");
   }
   else {
//      lsWhere +=" and A.p_seqno in (select p_seqno from dba_acno where acct_key like '"+ls_acct_key+"%')";
	   lsWhere += " and A.p_seqno in (select p_seqno from dba_acno where 1=1 "
			   + sqlCol(ls_acct_key,"acct_key","like%")
			   + " ) "
			   ;
   }
   setSqlParmNoClear(true);
   queryTotal(lsWhere);
   
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;      
   wp.setQueryMode();

   queryRead();
}
void queryTotal(String a_where) throws Exception {
   if (empty(a_where)) return;
   String sql1="select count(*) as tot_cnt, sum(decode(A.sign_flag,'-',0 - A.dest_amt, A.dest_amt)) as tot_amt"+
         " from dbb_bill A"+a_where;
   sqlSelect(sql1);
   sql2wp("tot_cnt");
   sql2wp("tot_amt");
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
         + " A.post_date ,"
         + " A.purchase_date ,"
         + " A.card_no ,"
         + " A.source_amt ,"
         + " A.dest_amt ,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_name,"
         + " A.mcht_city ,"
         + " A.auth_code ,"
         + " A.txn_code ,"
         + " A.rsk_type ,"
         + " A.rsk_type_special ,"
         + " A.rsk_ctrl_seqno ,"
         + commSqlStr.nvl("A.bin_type", "'V'") + " as bin_type ,"
         + " A.reference_no , A.sign_flag, "
         + " '' as rept_mark," //B.rept_status as rsk_rept_mark,"
         + " '' as chgb_mark," //B.chgb_status||B.chgb_status2||B.chgb_status3 as rsk_chgb_mark,"
         + " '' as prbl_mark," //B.prbl_mark||B.prbl_status as rsk_prbl_mark,"
         + " '' as compl_mark,"  //B.precom_mark ,"
         + " '' as arbit_mark"  //B.prearb_mark "
   ;
   wp.daoTable = " dbb_bill A ";  // left join xxx_ctrlseqno_log B on A.reference_no =B.reference_no ";
   wp.whereOrder = " order by A.purchase_date, A.dest_amt, A.card_no, A.reference_no ";
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   queryAfter(wp.listCount[0]);

   wp.setPageValue();
}

void queryAfter(int ll_nrow) throws Exception  {
   RskCtrlseqno ooLog = new RskCtrlseqno();
   ooLog.setConn(wp);
   for (int rr = 0; rr < ll_nrow; rr++) {
      if (wp.colEmpty(rr, "rsk_ctrl_seqno")) continue;

      String lsRefNo = wp.colStr(rr, "reference_no");
      ooLog.selectXxxBill(lsRefNo, "Y");
      wp.colSet(rr, "rept_mark", ooLog.reptStatus);
      wp.colSet(rr, "chgb_mark", ooLog.chgbStage1+"-"+ooLog.chgbStage2+"-"+ooLog.chgbClose);
      wp.colSet(rr, "prbl_mark", ooLog.prblMark+"-"+ooLog.prblStatus+"-"+ooLog.prblCloResult);
      wp.colSet(rr, "compl_mark", ooLog.complStatus);
      wp.colSet(rr, "arbit_mark", ooLog.arbitStatus);
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(isReferNo)) {
      isReferNo = wp.itemStr("reference_no");
   }
   if (empty(isCtrlSeqno)) {
      isCtrlSeqno = wp.itemStr("ctrl_seqno");
   }

   if (wp.respHtml.indexOf("_prbl") > 0) {
      dataReadPrbl();
      return;
   }
   if (wp.respHtml.indexOf("_rept") > 0) {
      dataReadRept();
      return;
   }
   if (wp.respHtml.indexOf("_chgb") > 0) {
      dataReadChgb();
      return;
   }
   if (wp.respHtml.indexOf("_compl") > 0) {
      dataReadCompl();
      return;
   }
   if (wp.respHtml.indexOf("_arbit") > 0) {
      dataReadArbit();
      return;
   }

   // -bill-
   if (wp.respHtml.indexOf("_bill") > 0) {
      isReferNo = kk1;
      if (selectBilBill() == 1) {
         wp.actionCode = "";
      }
      else {
         wp.notFound = "Y";
         return;
      }
   }
}

@Override
public void initButton() {
   this.buttonOff("");
   if (eqIgno(wp.respHtml, "rskp1010")) {
      this.btnModeAud("XX");
   }
   if (eqIgno(wp.respHtml, "rskp1010_prbl")) {
      this.btnModeAud(wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_rept") > 0) {
      this.btnModeAud(wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_chgb") > 0) {
      this.btnModeAud(wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_arbit") > 0) {
      this.btnModeAud(wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_compl") > 0) {
      this.btnModeAud(wp.colStr("rowid"));
      int li_status = wp.colInt("pre_status");
      if (li_status < 30)
         this.buttonOff("btnProc1_off");
      if (wp.colInt("com_status") < 30)
         this.buttonOff("btnProc2_off");

   }
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {

}


void procPreCompl() throws Exception {
   Rskm0450Func func = new Rskm0450Func();
   func.setConn(wp);

   rc = func.closeProce(_proc_close);
   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.mesg());
      return;
   }

   //--
   kk1 = wp.colStr("ctrl_seqno");
   dataReadCompl();
}

}
