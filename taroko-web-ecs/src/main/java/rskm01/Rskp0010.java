package rskm01;
/**
 * 2021-1116   JH    9051: bil_contract.Query error
 * 2021-0810.8258    JH    請更改負項交易(TC06.TC26) 不能執行調單.問交(整批).扣款(整批).預備依從.預備仲裁
 * 2021-0316   JH    CR-1304.qr_flag
 * 2020-0828   JH    帳單明細,問交: 查分期帳單
 * 2020-0515   JH    modify
 * 2020-0408   Alex  add auth_query
 * 2019-1206   Alex  add initButton
 * 2019-1127   JH    UAT-bug
 * 2019-1017   JH    ++qr_flag
 */

import busi.func.ColFunc;
import ofcapp.BaseAction;
import taroko.base.CommDate;

public class Rskp0010 extends BaseAction {
String kk1 = "", isReferNo = "", isCtrlSeqno = "";
taroko.base.CommDate zzdate = new CommDate();
int procClose = 0;
int iiCurrTerm=0;

@Override
public void userAction() throws Exception {

   strAction =wp.buttonCode;
   if (eqIgno(wp.requHtml,"rskp0010")) {
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
         case "C1": //-整批列問交-
            doBatchProblem(); break;
         case "C3": //-整批扣款-
            doBatchChgback(); break;
      }

      if (wp.respHtml.indexOf("_arbit") > 0) {
         if (wp.colEq("bin_type", "V")) {
            wp.respHtml = "rskp0010_arbit_v";
         }
         else wp.respHtml = "rskp0010_arbit";
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
   if (wp.requHtml.indexOf("_compl") >0) {
      switch (wp.buttonCode) {
         case "R":
            dataReadCompl(); break;
         case "A":
         case "U":
         case "D":
            dbSaveCompl(); break;
         case "U2":
         case "D2": //依從修改
            dbSaveCompl(); break;
         case "C4": //-預備依從-結案-
            procClose = 1;
            procPreCompl(); break;
         case "C5": // -依從權-結案-
            procClose = 2;
            procPreCompl(); break;
      }

      return;
   }

}

void getCtrlSeqno() throws Exception  {
   RskCtrlseqno ooLog = new RskCtrlseqno();
   ooLog.setConn(wp);
   isCtrlSeqno = ooLog.getCtrlSeqno(wp.itemStr("reference_no"));
   if (empty(isCtrlSeqno)) {
      alertErr("無法取得 [控制流水號]");
   }
}

void dbSaveProblem() throws Exception {
   rskm01.Rskm0010Func func = new rskm01.Rskm0010Func();
   func.setConn(wp);
   if (eqIgno(strAction, "A")) {
      rc = func.dbInsert();
      if (rc == 1) {
         alertMsg("問交新增成功; 控制流水號=" + wp.colStr("ctrl_seqno"));
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
   //-理由碼-
//	if (wp.itemEmpty("db_reason_code")==false) {
//		wp.item_set("reason_code",wp.itemStr("db_reason_code"));
//	}

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
   saveAfter(true);
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
   // String[] aa_ctrl_seqno =wp.getInBuffer("rsk_ctrl_seqno");
   String[] opt = wp.itemBuff("opt");

   //int rr = (int) this.to_Num(opt[0]) - 1;
   int rr = this.optToIndex(opt[0]);

   if (opt == null || opt.length == 0 || rr < 0) {
      alertErr("未點選列問交資料");
      wp.notFound = "Y";
      return -1;
   }

   isReferNo = aa_refno[rr];
   isCtrlSeqno = wp.colStr(rr, "rsk_ctrl_seqno");
   iiCurrTerm =(int)wp.itemNum(rr,"install_curr_term");

   if (wp.respHtml.indexOf("_rept") > 0 ||
         wp.respHtml.indexOf("_chgb") > 0 ||
         wp.respHtml.indexOf("_arbit") > 0 ||
         wp.respHtml.indexOf("_compl") > 0 ||
         wp.buttonCode.equals("C1") ||
         wp.buttonCode.equals("C3")) {
      if (iiCurrTerm >1) {
         alertErr("分期帳單: 不是首期, 不可[調單, 扣款, 仲裁, 依從權, 整批列問交, 整批扣款]");
         return -1;
      }
   }
   //--
   //請更改負項交易(TC06.TC26);不能執行調單.問交(整批).扣款(整批).預備依從.預備仲裁
   String ls_sign =wp.itemStr(rr,"sign_flag");
   if (wp.respHtml.indexOf("_rept")>0 ||
           wp.respHtml.indexOf("_arbit")>0 || wp.respHtml.indexOf("_compl")>0 ) {
      if (eqIgno(ls_sign,"-")) {
         alertErr("負項交易, 不可[調單, 仲裁, 依從權, 整批列問交, 整批扣款]");
         return -1;
      }
   }
   if (commString.strIn("|"+wp.buttonCode+"|","|C1|C3|")) {
      for (int ii=0; ii<opt.length; ii++) {
         rr =optToIndex(opt[ii]);
         if (rr <0) continue;
         ls_sign =wp.itemStr(rr,"sign_flag");
         if (eqIgno(ls_sign,"-")) {
            alertErr("負項交易, 不可[調單, 仲裁, 依從權, 整批列問交, 整批扣款]");
            return -1;
         }
      }
   }
   return 1;
}

void data_check() {
   String ls_merge_flag = wp.colStr("BL_merge_flag");
   String ls_bill_type = wp.colStr("BL_bill_type");
   String ls_txn_code = wp.colStr("BL_txn_code");

   if (eqIgno(ls_merge_flag, "Y")) {
      alertErr("合併帳戶之帳單, 不可[調單/扣款/列問交]");
      return;
   }
   if (pos("|OK|OS|I1|I2", strMid(ls_bill_type, 0, 2)) > 0) {
      alertErr("請選擇正確的帳單類別碼, ["+ls_bill_type+"]");
      return;
   }
   if (eqIgno(ls_bill_type, "FIFC")) {
      alertErr("國外手續費[FIFC]: 不可 列問交, 調單, 扣款");
      return;
   }

   if (pos("|06|25|27|28|29|RI", ls_txn_code) > 0) {
      alertErr("請選擇正確的交易碼, ["+ls_txn_code+"]");
      return;
   }
}

void dataReadPrbl() throws Exception {
   if (select_BilBill(false) != 1) {
      wp.notFound = "Y";
      return;
   }

   data_check();
   if (rc != 1)
      return;

   select_rsk_problem();
   if (wp.colEmpty("bin_type"))
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));

   return;
}

void dataReadRept() throws Exception {
   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);
   func.varsSet("reference_no", isReferNo);

//   if (func.rept_Select() != 1) {
//      alertErr(func.getMsg());
//      return;
//   }

   if (select_BilBill(true) != 1) {
      wp.notFound = "Y";
      return;
   }

   select_rsk_receipt();
   if (wp.colEmpty("bin_type"))
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));

   detl_wkdata();
   wf_get_ori_totamt();

   return;
}

void dataReadChgb() throws Exception {
   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);
   func.varsSet("reference_no", isReferNo);

//   if (func.chgb_Select() != 1) {
//      alertErr(func.getMsg());
//      return;
//   }

   if (select_BilBill(true) != 1) {
      wp.notFound = "Y";
      return;
   }

   select_rsk_chgback();

   wf_get_ori_totamt();
   return;
}

void dataReadArbit() throws Exception {
   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);

   if (empty(isReferNo))
      isReferNo = wp.itemStr("reference_no");

//   func.varsSet("reference_no", isReferNo);
//   if (func.arbit_Select() != 1) {
//      alertErr(func.getMsg());
//      return;
//   }

   if (select_BilBill(true) != 1) {
      wp.notFound = "Y";
//		this.selectOK();
      return;
   }

   wp.sqlCmd = "select A.*"
         + ", hex(rowid) as rowid"
         + " from rsk_prearbit A"
         + " where 1=1"
         + sqlCol(isReferNo, "reference_no")
         + sqlCol("0", "reference_seq")
   ;
   this.pageSelect();
   if (sqlRowNum <= 0) {
      selectOK();
      //-ref.no-
      wp.colSet("reference_no ", isReferNo);
      wp.colSet("reference_seq", "0");
      wp.colSet("ctrl_seqno   ", wp.colStr("BL_rsk_ctrl_seqno"));
      wp.colSet("acct_month   ", wp.colStr("BL_acct_month"));

      wp.colSet("reference_no_ori ", wp.colStr("BL_reference_no_ori"));
      wp.colSet("bin_type     ", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag   ", "N");
      wp.colSet("card_no      ", wp.colStr("BL_card_no"));
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

      wp.colSet("arbit_times", "1");
      wp.colSet("pre_status", "10");
      wp.colSet("ctrl_seqno", set_Ctrl_seqno());
      wp.colSet("pre_event_date",wp.sysDate);

//      rskFunc func2 = new rskFunc();
//      func2.setConn(wp.getConn());
//      String ls_expr_date = func2.arbit_expire_date(wp.colStr("bin_type"), wp.colStr("BL_txn_code"), "");
//      wp.colSet("pre_expire_date", ls_expr_date);

   }

   return;
}

void dataReadCompl() throws Exception  {
   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);

   if (empty(isReferNo))
      isReferNo = wp.itemStr("reference_no");

   func.varsSet("reference_no", isReferNo);

//   if (func.compl_Select() != 1) {
//      alertErr(func.getMsg());
//      return;
//   }

   if (select_BilBill(true) != 1) {
      wp.notFound = "Y";
//		this.selectOK();
      return;
   }

   wp.sqlCmd = "select A.*"
         + ", hex(rowid) as rowid"
         + " from rsk_precompl A"
         + " where 1=1"
         + sqlCol(isReferNo, "reference_no")
         + sqlCol("0", "reference_seq");
   this.pageSelect();
   if (sqlRowNum <= 0) {
      selectOK();

      wp.colSet("reference_no ", wp.colStr("BL_reference_no"));
      wp.colSet("reference_no_ori ", wp.colStr("BL_reference_no_ori"));
      wp.colSet("reference_seq", 0);
      wp.colSet("ctrl_seqno   ", wp.colStr("BL_rsk_ctrl_seqno"));
      wp.colSet("bin_type     ", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag   ", "N");
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
      wp.colSet("ctrl_seqno", set_Ctrl_seqno());
      wp.colSet("event_date", wp.sysDate);

      //-expire_date-
//      rskFunc func2 = new rskFunc();
//      func2.setConn(wp.getConn());
//      String ls_expr_date = func2.compl_expire_date(wp.colStr("bin_type"), wp.colStr("BL_txn_code"), "");
//      wp.colSet("pre_expire_date", ls_expr_date);
   }

   return;
}

int select_rsk_problem() throws Exception {

   wp.whereStr = " where A.reference_no =? and reference_seq =0";
//			+ " order by reference_seq "+commSqlStr.rownum(1);
   wp.daoTable = "rsk_problem A";
   wp.selectSQL = "hex(A.rowid) as rowid"
         + ", A.*"
   ;
   setString(1, isReferNo);
   pageSelect();
   if (sqlRowNum <= 0) {
      wp.colSet("reference_no", isReferNo);
      wp.colSet("reference_seq", "0");
      wp.colSet("bin_type", wp.colStr("BL_bin_type"));
      wp.colSet("debit_flag", wp.colStr("BL_debit_flag"));
      wp.colSet("prb_mark", "Q");
      wp.colSet("prb_src_code", "RQ");
      wp.colSet("prb_status", "10");
      wp.colSet("prb_fraud_rpt", "0");
      wp.colSet("card_no", wp.colStr("BL_card_no"));
      wp.colSet("curr_code", wp.colStr("BL_curr_code"));
      //wp.colSet("ctrl_seqno",wp.colStr("BL_rsk_ctrl_seqno"));
      //-default-
      wp.colSet("prb_reason_code", "22");
      wp.colSet("dc_prb_amount", wp.colStr("BL_dc_dest_amt"));
      wp.colSet("prb_amount", wp.colStr("BL_dest_amt"));

      // --
      //wp.colSet("ctrl_seqno",set_Ctrl_seqno());
      wp.notFound = "";
   }

   return 1;
}

int select_rsk_receipt() throws Exception {

   wp.whereStr = " where A.reference_no =?"
         + " and A.reference_seq =0";
   wp.daoTable = "rsk_receipt A";
   wp.selectSQL = "hex(A.rowid) as rowid"
         + ", reason_code as db_reason_code"
         + ", A.*"
   ;
   setString(1, isReferNo);
   //sql_ddd();
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
      wp.colSet("id_p_seqno", wp.colStr("BL_id_p_seqno"));
      wp.colSet("p_seqno", wp.colStr("BL_p_seqno"));
      wp.colSet("corp_p_seqno", wp.colStr("BL_corp_p_seqno"));

      // --
      wp.colSet("rept_status", "10");
      wp.colSet("rept_seqno", "001");
      wp.colSet("add_user", this.loginUser());
      //-理由碼-
      if (wp.colEq("bin_type", "V")) {
         wp.colSet("reason_code", "30");
         wp.colSet("db_reason_code", "30");
      }
      else if (wp.colEq("bin_type", "M")) {
         wp.colSet("reason_code", "6321");
         wp.colSet("db_reason_code", "6321");
      }
      else if (wp.colEq("bin_type", "J")) {
         wp.colSet("reason_code", "0005");
         wp.colSet("db_reason_code", "0005");
      }
      //--
      wp.colSet("ctrl_seqno", set_Ctrl_seqno());
      wp.notFound = "";
   }

   //wp.ddd("add_user="+wp.itemStr("add_user"));
   return 1;
}

void select_rsk_chgback() throws Exception {
   rskm01.Rskm0210Func chgb = new rskm01.Rskm0210Func();
   chgb.setConn(wp);
   chgb.varsSet("reference_no", isReferNo);
   sqlRowNum = chgb.dataSelect();

   //wp.sqlCmd =chgb.dataSelect();
   //pageSelect();

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

      wp.colSet("ctrl_seqno", set_Ctrl_seqno());
      wp.notFound = "";
   }
}

private String set_Ctrl_seqno() throws Exception  {
   String ss = wp.colStr("ctrl_seqno");
   if (notEmpty(ss))
      return ss;

   RskCtrlseqno ooLog = new RskCtrlseqno();
   ooLog.setConn(wp);
   String ls_ctrl_seqno = ooLog.getCtrlSeqno(wp.colStr("referecne_no"));
   return ls_ctrl_seqno;
}

int select_BilBill(boolean ab_refOrg) throws Exception  {
   BilBill bill = new BilBill();
   bill.setConn(wp);
   String ls_refno =isReferNo;
   if (empty(ls_refno))
      ls_refno = wp.itemStr("reference_no");
   bill.varsSet("reference_no", ls_refno);

   if (bill.dataSelect() == -1) {
      alertErr(bill.getMsg());
      return -1;
   }
   //-原始帳單-
   if (ab_refOrg) {
      String ls_refno_ori = wp.colStr("BL_reference_no_ori");
      if (notEmpty(ls_refno_ori) && !eqIgno(ls_refno,ls_refno_ori)) {
         bill.billDataOri(ls_refno_ori);
      }
   }
   
   return 1;
}

void wf_get_ori_totamt() throws Exception  {
   String ls_contr_no = wp.colStr("BL_contract_no");
   if (empty(ls_contr_no))
      return;

   String sql1 = "select A.tot_amt, A.reference_no as refno_ori"
         + ", B.txn_code"
         + " from bil_contract A left join bil_curpost B "
         + " on B.reference_no =A.reference_no"
         + " where A.contract_no =?"
         + " and A.contract_seq_no =1";
   setString(1, ls_contr_no);
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      alertErr("查無原始帳單資料, 不可調單/扣帳");
      return;
   }

   wp.colSet("BL_dest_amt", sqlNum("tot_amt"));
   wp.colSet("BL_dc_dest_amt", sqlNum("tot_amt"));
   wp.colSet("BL_source_amt", sqlNum("tot_amt"));
   wp.colSet("BL_fst_twd_amt", sqlNum("tot_amt"));
   wp.colSet("BL_fst_dc_amt", sqlNum("tot_amt"));
   wp.colSet("BL_txn_code", sqlStr("txn_code"));
   wp.colSet("reference_no_ori", sqlStr("refno_ori"));
   wp.colSet("BL_reference_no_ori", sqlStr("refno_ori"));
   return;
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskp0010")) {
         wp.optionKey = wp.colStr("ex_acct_type");
         dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
         wp.optionKey = wp.colStr("ex_curr_code");
         dddwList("dddw_dc_curr_code_tw","ptr_sys_idtab","wf_id","wf_desc","where wf_type = 'DC_CURRENCY'");
      }

      // -問交-
      if (eqIgno(wp.respHtml, "rskp0010_prbl")) {
         wp.optionKey = wp.colStr("prb_reason_code");
         dddwList("dddw_prb_reason_code", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='PRBL-REASON-CODE' and id_code2 not like 'VD%'");
      }

      // -調單-
      if (wp.respHtml.indexOf("_rept") > 0) {
         String ls_where = "where wf_type='RECEIPT-REASON-CODE'"+ sqlCol(wp.colStr("bin_type"), "id_code2");
         wp.optionKey = wp.colStr("reason_code");
         dddwList("dddw_reason_code", "ptr_sys_idtab", "wf_id", "wf_desc",ls_where);
      }
      //-預備仲裁-V-
      if (wp.respHtml.indexOf("_arbit_v")>0) {
         wp.optionKey =wp.colStr("pre_result");
         wp.colSet("ddlb_pre_result",ecsfunc.DeCodeRsk.arbitPreResult(wp.optionKey,true));
         wp.optionKey =wp.colStr("arb_result");
         wp.colSet("ddlb_arb_result",ecsfunc.DeCodeRsk.arbitCloResult(wp.optionKey,true));
      }
      else if (eqIgno(wp.respHtml,"rskp0010_arbit")) {
         wp.optionKey =wp.colStr("pre_result");
         wp.colSet("ddlb_pre_result",ecsfunc.DeCodeRsk.arbitPreResult(wp.optionKey,true));
         wp.optionKey =wp.colStr("arb_result");
         wp.colSet("ddlb_arb_result",ecsfunc.DeCodeRsk.arbitCloResult(wp.optionKey,true));
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

@Override
public void queryFunc() throws Exception {
   ColFunc func2 = new ColFunc();
   func2.setConn(wp);

   if (wp.itemEmpty("ex_acct_key") == false) {
      if (func2.fAuthQuery(wp.modPgm(), commString.mid(wp.itemStr("ex_acct_key"), 0, 10)) != 1) {
         alertErr(func2.getMsg());
         return;
      }
   }
   else if (wp.itemEmpty("ex_card_no") == false) {
      if (func2.fAuthQuery(wp.modPgm(), wp.itemStr("ex_card_no")) != 1) {
         alertErr(func2.getMsg());
         return;
      }
   }

   if (wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no")) {
      alertErr("帳戶帳號, 卡號: 不可同時空白");
      return;
   }
   if (wp.itemEq("ex_bill", "1")) {
      if (wp.itemEmpty("ex_acct_month") && wp.itemEmpty("ex_contr_no")) {
         alertErr("關帳年月, 申購書編號: 不可同時空白");
         return;
      }
   }
   String ls_key = "";
   if (wp.itemEmpty("ex_acct_key") == false) {
      ls_key = commString.acctKey(wp.itemStr("ex_acct_key"));
      if (ls_key.length() != 11) {
         alertErr("帳戶帳號輸入錯誤");
         return;
      }
      zzVipColor(wp.itemNvl("ex_acct_type", "01") + ls_key);
   }
   else if (wp.itemEmpty("ex_card_no") == false) {
      ls_key = wp.itemStr("ex_card_no");
      zzVipColor(ls_key);
   }

   if (logQueryIdno(ls_key) == false) {
      return;
   }
   
   //--判斷關帳日期
   if(checkAcctMonth() == false)
   	return;
   
   //--姓名
   getChiName();

   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);
   if (func.querySelect() == 1) {
	   wp.whereStr = getWhereStr();
//      wp.whereStr = func.sqlWhere;
   }
   else {
      alertErr(func.getMsg());
      return;
   }
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   //--
   setSqlParmNoClear(true);
   String ls_sql="select count(*) as wk_tot_cnt, sum(decode(A.sign_flag,'-',0 - A.dc_dest_amt,A.dc_dest_amt)) as wk_tot_amt"+
         " from bil_bill A "+wp.whereStr;
   sqlSelect(ls_sql);
   sql2wp("wk_tot_cnt");
   sql2wp("wk_tot_amt");

   queryRead();
}

String getWhereStr() {	
	String lsWhere = "";
	lsWhere = " where 1=1 "			
			+ sqlCol(wp.colStr("ex_p_seqno"),"A.p_seqno")
			+ sqlCol(wp.itemStr("ex_curr_code"),"uf_nvl(A.curr_code,'901')")
			+ sqlCol(wp.itemStr2("ex_contr_no"), "A.contract_no")
			;
	  
	if (wp.itemEq("ex_bill", "1")) {
		lsWhere += sqlCol(wp.itemStr("ex_acct_month"),"A.acct_month");
	}	else	{
		lsWhere += sqlCol(wp.colStr("ex_next_acct_month"),"A.acct_month")
				+ " and A.billed_date ='' ";
	}
	  
	lsWhere += " and A.txn_code not in ('65','66','67','69','85','86','87','89',"
	  		+ " 'CD','DF','HC','IF','LF','LP','LS','RB','RR','AF','AI','BF','CF','TX','VF','VP','VR','VT') "
	  		+ " and ( (A.rsk_type<>'' and A.bill_type<>'FIFC') or (A.rsk_type='4' and A.bill_type='FIFC') or A.rsk_type ='' )"
			;	  	  	  
	  
	return lsWhere;
}

boolean checkAcctMonth() throws Exception {
	String ls_card_no = "" , sql1 ="";
	if(wp.itemEmpty("ex_acct_key") == false) {
		if (wp.itemStr("ex_acct_key").length() != 8 && wp.itemStr("ex_acct_key").length() < 10) {
         errmsg("帳戶帳號: 輸入錯誤");
         return false;
      }
		
		sql1 = " select p_seqno as bef_p_seqno from act_acno where acct_key like ? and acct_type = ? " + commSqlStr.rownum(1);
		setString(1,wp.itemStr("ex_acct_key")+"%");
		setString(wp.itemNvl("ex_acct_type", "01"));				
	}	else if(wp.itemEmpty("ex_card_no") == false || wp.itemEmpty("ex_vcard_no") == false) {		
		if(wp.itemEmpty("ex_card_no") == false)
			ls_card_no = wp.itemStr("ex_card_no");
		else if(wp.itemEmpty("ex_vcard_no") == false) {
			ls_card_no = getCardNoFromTpan(wp.itemStr("ex_vcard_no"));
			if(empty(ls_card_no)) {
				errmsg("TPAN 輸入錯誤");
				return false;
			}
		}
			
		sql1 = " select p_seqno as bef_p_seqno from crd_card where card_no = ? " ;
		setString(1,ls_card_no);		
	}	else	{
		errmsg("[帳戶帳號, 卡號] 輸入錯誤, 查無帳戶流水號");
		return false;
	}
	
	sqlSelect(sql1);
	if(sqlRowNum <=0) {
		errmsg("[帳戶帳號, 卡號] 輸入錯誤, 查無帳戶流水號");
		return false ;
	}
	
	String ls_bef_p_seqno = sqlStr("bef_p_seqno");
	//--取關帳日期
	String sql2 = " select b.stmt_cycle, b.next_acct_month, A.acct_status , B.this_acct_month as is_this_mm"
         		+ " from act_acno a, ptr_workday B"
         		+ " where A.stmt_cycle = B.stmt_cycle"
         		+ " and A.p_seqno = ? ";
   sqlSelect(sql2, new Object[]{ls_bef_p_seqno});
   if (sqlRowNum <= 0) {
      errmsg("查無卡人之關帳周期[stmt_cycle]");
      return false;
   }
	
   String ls_acct_month = "", ls_next_acct_month = "", is_this_mm = "";
   ls_acct_month = wp.itemStr("ex_acct_month");
   ls_next_acct_month = sqlStr("next_acct_month");
   is_this_mm = sqlStr("is_this_mm");
   if (wp.itemEq("ex_bill", "1")) {
   	if (chkStrend(ls_acct_month, is_this_mm) == false) {
         alertErr("輸入關帳年月尚未發生!");
         return false;
      }
   }
   
	return true;
}

String getCardNoFromTpan(String a_vcard_no) throws Exception {
	String sql1 = "select card_no from hce_card where 1=1 and v_card_no = ? ";
	sqlSelect(sql1,new Object[]{a_vcard_no});	
	if(sqlRowNum > 0 )
		return sqlStr("card_no");	
	return "";
}

void getChiName() throws Exception  {
   String ls_key = "";
   if (wp.itemEmpty("ex_acct_key") == false) {
      ls_key = commString.acctKey(wp.itemStr("ex_acct_key"));
      if (ls_key.length() != 11) {
         alertErr("帳戶帳號輸入錯誤");
         return;
      }
      String sql1 = "select uf_idno_name(id_p_seqno) as chi_name from act_acno where acct_type = ? and acct_key = ? ";
      sqlSelect(sql1, new Object[]{wp.itemNvl("ex_acct_type", "01"), ls_key});
   }
   else if (wp.itemEmpty("ex_card_no") == false) {
      String sql1 = " select uf_idno_name(?) as chi_name from dual ";
      sqlSelect(sql1, new Object[]{wp.itemStr("ex_card_no")});
   }
   else if (wp.itemEmpty("ex_idno") == false) {
      String sql1 = " select uf_idno_name(?) as chi_name from dual ";
      sqlSelect(sql1, new Object[]{wp.itemStr("ex_idno")});
   }

   if (sqlRowNum > 0) {
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
   }

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " A.acct_date , A.post_date,"
         + " A.purchase_date , "
         + " A.interest_date , "
         + " A.card_no , "
         + " A.source_amt , "
         + " A.dest_amt,"
         + " A.curr_code,"
         + " A.dc_dest_amt,"
         + commSqlStr.mchtName("", "") + " as mcht_name,"
         + " A.mcht_city,"
         + " A.auth_code,"
         + " A.txn_code,"
         + " A.bin_type,"
         + " A.payment_type, uf_tt_idtab('BIL_PAYMENT_TYPE',A.payment_type) as tt_payment_type,"
         + " A.cash_pay_amt,"
         + " A.reference_no, A.contract_no, A.merge_flag,"
         + " A.v_card_no,"
         + " A.rsk_ctrl_seqno, A.install_curr_term, "
         + " '' as rept_mark , "
         + " '' as prbl_mark , "
         + " '' as chgb_mark , "
         + " '' as compl_mark , "
         + " '' as arbit_mark "
         +", A.reference_no_original as reference_no_ori"
         +", '' AS refer_no_cont"
//         +", A.qr_flag, "	qr_code 交易 非必要欄位先不新增欄位
         + ", A.sign_flag "
   ;
   wp.daoTable = "bil_bill A"; // left join Vrsk_ctrlseqno_bil B"+
//         " on A.reference_no =B.reference_no and A.rsk_ctrl_seqno=B.ctrl_seqno"
   wp.whereOrder = " order by A.purchase_date, A.card_no, A.post_date, A.install_curr_term, A.reference_no";

   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   query_After();

   wp.setPageValue();
}

void query_After() throws Exception {
   RskCtrlseqno ooLog = new RskCtrlseqno();
   ooLog.setConn(wp);

   String sql_cont ="SELECT reference_no as refer_no_cont FROM bil_contract"
           +" WHERE contract_no =? AND reference_no<>''"
           +commSqlStr.rownum(1);
//   String sql1 = "select qr_flag from bil_nccc300_dtl where reference_no =?";

   int ll_nrow = wp.listCount[0];
   for (int ii = 0; ii < ll_nrow; ii++) {
      String ls_refno_ori =wp.colStr(ii,"reference_no_ori");
      if (empty(ls_refno_ori)) {
         wp.colSet(ii,"reference_no_ori", wp.colStr(ii,"refer_no_cont"));
      }
      //--
      String ss=wp.colStr(ii,"post_date");
      wp.colSet(ii,"wk_post_date",commString.mid(ss,4,2)+"/"+commString.mid(ss,6,2));
      //--
      String ls_cont_no =wp.colStr(ii,"contract_no");
      if (notEmpty(ls_cont_no)) {
         sqlSelect(sql_cont,ls_cont_no);
         if (sqlRowNum >0) {
            ss =sqlStr("refer_no_cont");
            wp.colSet(ii,"refer_no_cont",ss);
         }
      }
      //--
      String ls_ref_no = wp.colStr(ii, "reference_no");
      ss ="";
      if (wp.colEmpty(ii, "rsk_ctrl_seqno")) continue;

      ooLog.selectXxxBill(ls_ref_no, "N");
      wp.colSet(ii, "rept_mark", ooLog.reptStatus);
      wp.colSet(ii, "prbl_mark", ooLog.prblMark + "-" + ooLog.prblStatus+"-"+ooLog.prblCloResult);
      wp.colSet(ii, "chgb_mark", ooLog.chgbStage1 + "-" + ooLog.chgbStage2 + "-" + ooLog.chgbClose);
      wp.colSet(ii, "compl_mark", ooLog.complStatus);
      wp.colSet(ii, "arbit_mark", ooLog.arbitStatus);
   }

}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");   
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (wp.respHtml.indexOf("_prbl") > 0) {
      dataReadPrbl();
      return;
   }
   //-bill-
   if (wp.respHtml.indexOf("_bill") > 0) {
      isReferNo = kk1;
      if (select_BilBill(false) == 1) {
         wp.actionCode = "";
      }
      else {
         wp.notFound = "Y";
         return;
      }
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

}

void detl_wkdata() throws Exception  {
   String sql1 = "select uf_idno_name(?) as wk_idno_name"
         + " from " + commSqlStr.sqlDual;
   setString(1, wp.colStr("card_no"));
   sqlSelect(sql1);
   if (sqlRowNum > 0) {
      wp.colSet("db_chi_name", sqlStr("wk_idno_name"));
   }
   else wp.colSet("db_chi_name", "");
}


@Override
public void initButton() {
   buttonOff("");
   if (eqIgno(wp.respHtml, "rskp0010_prbl")) {
      this.btnModeAud("rskm0010", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_rept") > 0) {
      this.btnModeAud("rskm0110", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_chgb") > 0) {
      this.btnModeAud("rskm0210", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_arbit") > 0) {
      this.btnModeAud("rskm0250", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_compl") > 0) {
      this.btnModeAud("rskm0450", wp.colStr("rowid"));
      int li_status = wp.colInt("pre_status");
      if (li_status < 30)
         this.buttonOff("btnProc1_off");
      if (wp.colInt("com_status") < 30)
         this.buttonOff("btnProc2_off");
   }
   else if (eqIgno(wp.respHtml, "rskp0010")) {
      btnModeAud("XX");
   }
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   //-整批調單-
   alertErr("整批調單: unOK");
}

void doBatchProblem() throws Exception {
   //整批列問交
   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);

   String[] aa_refno = wp.getInBuffer("reference_no");
   String[] opt = wp.itemBuff("opt");
   int li_select = wp.itemRows("opt");   //(int) this.to_Num(opt[0]) - 1;

   if (li_select <= 0) {
      alertErr("未點選列 [整批列問交] 資料");
      return;
   }
   
   if(procCheckOpt() !=1)
   	return ;
   
   int rr = 0;
   for (int ii = 0; ii < li_select; ii++) {
      rr = optToIndex(opt[ii]);
      wp.colSet(ii, "B-proc_num", opt[ii]);
      wp.colSet(ii, "B-reference_no", aa_refno[rr]);
      wp.colSet(ii, "B-card_no", wp.itemStr(rr, "card_no"));
      wp.colSet(ii, "ok_flag", "V");
      wp.colSet(ii, "B-err_msg", "OK");
      wp.itemSet("proc_indx", "" + ii);

      if (func.batchProblem(aa_refno[rr]) == 1) {
         wp.colSet(ii, "B-rsk_ctrl_seqno", func.varsStr("ctrl_seqno"));
         wp.colSet(ii, "B-rsk_prbl_mark", "Q10");
         this.dbCommit();
      }
      else {
         wp.colSet(ii, "ok_flag", "X");
         wp.colSet(ii, "B-err_msg", func.getMsg());
         wp.colSet(ii, "B-rsk_prbl_mark", "");
         wp.colSet(ii, "B-rsk_ctrl_seqno", wp.itemStr(rr, "rsk_ctrl_seqno"));
         this.dbRollback();
      }
   }
   
   wp.listCount[0] = opt.length;
   //isReferNo = aa_refno[rr];
}


void doBatchChgback() throws Exception {
   //alertErr("整批扣款: unOK");
   rskm01.Rskp0010Func func = new rskm01.Rskp0010Func();
   func.setConn(wp);

   //-整批扣款-
   String[] aa_refno = wp.itemBuff("reference_no");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("reference_no");

   int rr = optToIndex(opt[0]);
   if (rr < 0) {
      alertErr("未點選列 [整批扣款] 資料");
      return;
   }
   
   if(procCheckOpt() !=1)
   	return ;
   
   int ll_ok = 0, ll_err = 0;
   for (int ii = 0; ii < opt.length; ii++) {
      rr = optToIndex(opt[ii]);
      if (rr < 0) continue;

      String ls_refno = aa_refno[rr];
      String ls_card_no = wp.colStr(rr, "card_no");
      double lm_dest_amt = wp.colNum(rr, "dc_dest_amt");
      wp.colSet(ii, "proc_num", opt[ii]);
      wp.colSet(ii, "B-reference_no", ls_refno);
      wp.colSet(ii, "B-card_no", ls_card_no);
      wp.colSet(ii, "B-dc_dest_amt", lm_dest_amt);
      wp.colSet(ii, "ok_flag", "V");
      wp.colSet(ii, "B-err_msg", "OK");
      wp.itemSet("proc_indx", "" + ii);

      if (func.batchChgback(aa_refno[rr]) == 1) {
         String ss = func.varsStr("ctrl_seqno");
         wp.colSet(ii, "B-rsk_ctrl_seqno", func.varsStr("ctrl_seqno"));
         wp.colSet(ii, "B-rsk_chgb_mark", "110");
         wp.colSet(ii,"B-dc_dest_amt",wp.itemStr("xx_batch_dest_amt"));
         ll_ok++;
         this.dbCommit();
      }
      else {
         wp.colSet(ii, "ok_flag", "X");
         wp.colSet(ii, "B-err_msg", func.getMsg());
         wp.colSet(ii, "B-rsk_chgb_mark", "");
         ll_err++;
         this.dbRollback();
      }
   }

   wp.listCount[0] = opt.length;
   wp.respMesg = "處理完成: 成功筆數=" + ll_ok + ", 錯誤筆數=" + ll_err;
}

@Override
public void initPage() {
	if(eqIgno(wp.respHtml, "rskp0010"))
		wp.colSet("ex_acct_month", getSysDate().substring(0,6));
}

void procPreCompl() throws Exception {
   Rskm0450Func func = new Rskm0450Func();
   func.setConn(wp);

   rc = func.closeProce(procClose);
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
