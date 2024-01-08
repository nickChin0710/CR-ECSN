package rskm01;
/**
 * 2020-0515   JH    依從HTML
 * 2020-0414	JH		--取消結案維護
 * 2018-04xx:	JH		modify
 * 2017-xxxx:	Alex	initial
 * 預備依從/依從權維護
 */

import ofcapp.BaseAction;

public class Rskm0450 extends BaseAction {
String kk1 = "";
taroko.base.CommDate zzdate = new taroko.base.CommDate();
int _proc_close = 0;

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面--
         strAction = "new";
         if (!empty(wp.itemStr("ex_ctrl_seqno"))) {
            selectData1();
            BilBill bil = new BilBill();
            bil.setConn(wp);
            bil.dataSelect(wp.colStr("reference_no"), wp.colStr("debit_flag"));
            selectEndDay();
         }
         break;
      case "Q": // 查詢功能 --
         queryFunc(); break;
      case "R":
         dataRead(); break;
      case "A":
      case "D":
         saveFunc(); break;
      case "U":
         doUpdate1(); break;
      case "M":   // 瀏覽功能 :skip-page --
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "L": //清畫面--
         strAction = "";
         clearFunc(); break;
      case "U2":
         strAction = "U";
         doUpdate2(); break;
      case "D2":
         strAction = "U";
         doDelete2(); break;
   }

}

void selectData1() throws Exception  {
   wp.colSet("pre_statue", "10"); // -- default = 10
   wp.colSet("ctrl_seqno", wp.itemStr("ex_ctrl_seqno"));

   RskCtrlseqno ooLog = new RskCtrlseqno();
   ooLog.setConn(wp);
   boolean lbOk = ooLog.checkCtrlSeqNo(wp.itemStr("ex_ctrl_seqno"));
   if (lbOk == false) {
      errmsg("控制流水號輸入錯誤");
      return;
   }

   wp.colSet("debit_flag", ooLog.debitFlag);
   wp.colSet("reference_no", ooLog.referenceNo);
   wp.colSet("reference_no_ori", ooLog.referenceNo);
   if (eqIgno(ooLog.debitFlag, "Y")) {
      String sql2 = " select "
            + " bin_type ,"
            + " card_no ,"
            + " mcht_country ,"
            + " acct_month ,"
            + " purchase_date ,"
            + " film_no ,"
            + " auth_code ,"
            + " source_amt ,"
            + " source_curr "
            + " from dbb_bill "
            + " where reference_no = ? ";
      sqlSelect(sql2, new Object[]{ooLog.referenceNo});
      if (sqlRowNum <= 0) {
         errmsg("查無資料");
         return;
      }
      wp.colSet("card", sqlStr("card_no"));
      wp.colSet("mcht_country", sqlStr("mcht_country"));
      wp.colSet("acct_month", sqlStr("acct_month"));
      wp.colSet("purchase_date", sqlStr("purchase_date"));
      wp.colSet("film_no", sqlStr("film_no"));
      wp.colSet("auth_code", sqlStr("auth_code"));
      wp.colSet("source_amt", sqlStr("source_amt"));
      wp.colSet("source_curr", sqlStr("source_curr"));
      wp.colSet("dest_amt", sqlStr("dest_amt"));
      wp.colSet("settl_amt", sqlStr("settl_amt"));
      wp.colSet("bin_type", sqlStr("bin_type"));
   }
   else if (eqIgno(ooLog.debitFlag, "N")) {
      String sql3 = " select "
            + " bin_type ,"
            + " card_no ,"
            + " mcht_country ,"
            + " acct_month ,"
            + " purchase_date ,"
            + " film_no ,"
            + " auth_code ,"
            + " source_amt ,"
            + " source_curr ,"
            + " dc_dest_amt ,"
            + " curr_code ,"
            + " dest_amt ,"
            + " settl_amt "
            + " from bil_bill"
            + " where reference_no = ? ";
      sqlSelect(sql3, new Object[]{
            ooLog.referenceNo
      });
      if (sqlRowNum <= 0) {
         errmsg("查無資料");
         return;
      }
      wp.colSet("card_no", sqlStr("card_no"));
      wp.colSet("mcht_country", sqlStr("mcht_country"));
      wp.colSet("acct_month", sqlStr("acct_month"));
      wp.colSet("purchase_date", sqlStr("purchase_date"));
      wp.colSet("film_no", sqlStr("film_no"));
      wp.colSet("auth_code", sqlStr("auth_code"));
      wp.colSet("source_amt", sqlStr("source_amt"));
      wp.colSet("source_curr", sqlStr("source_curr"));
      wp.colSet("dc_dest_amt", sqlStr("dc_dest_amt"));
      wp.colSet("curr_code", sqlStr("curr_code"));
      wp.colSet("dest_amt", sqlStr("dest_amt"));
      wp.colSet("settl_amt", sqlStr("settl_amt"));
      wp.colSet("bin_type", sqlStr("bin_type"));
   }
}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl")>0) {
         wp.optionKey =wp.colStr("pre_clo_result");
         wp.colSet("ddlb_pre_clo_result",ecsfunc.DeCodeRsk.complPreResult(wp.optionKey,true));

         wp.optionKey =wp.colStr("com_clo_result");
         wp.colSet("ddlb_com_clo_result",ecsfunc.DeCodeRsk.complPreResult(wp.optionKey,true));
      }
   }
   catch (Exception e) {}
}

@Override
public void queryFunc() throws Exception {
   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no")
         + sqlCol(wp.colStr("ex_compl_times"), "compl_times");

   if (!wp.itemEmpty("ex_user_id")) {
      lsWhere +=
            " and (pre_add_user ='" + wp.itemStr("ex_user_id") + "'"
                  + " or com_add_user='" + wp.itemStr("ex_user_id") + "') ";
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " ctrl_seqno, bin_type, compl_times"
         + ", card_no"
         + ", purchase_date"
         + ", mcht_country"
         + ", film_no"
         + ", uf_dc_curr(curr_code) as curr_code"
         + ", uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt"
         + ", event_date"
         + ", viol_date"
         + ", pre_apply_date"
         + ", pre_clo_result"
         + ", pre_add_user"
         + ", com_apply_date"
         + ", com_clo_result"
         + ", com_add_user"
         + ", debit_flag";
   wp.daoTable = "rsk_precompl";
   wp.whereOrder = " order by ctrl_seqno ";

   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr(this.appMsg.errNotFind);
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
   if (empty(kk1)) {
      kk1 = itemkk("ctrl_seqno");
   }

   wp.selectSQL = "hex(A.rowid) as rowid ,"
         + " A.* "
         + ", uf_dc_curr(A.curr_code) as curr_code "
         + ", uf_dc_amt2(A.dest_amt,A.dc_dest_amt) as dc_dest_amt "
   ;
   wp.daoTable = "rsk_precompl A ";
   wp.whereStr = "where reference_seq=0"
         + sqlCol(kk1, "A.ctrl_seqno");
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
   }

   BilBill bil = new BilBill();
   bil.setConn(wp);
   bil.dataSelect(wp.colStr("reference_no"), wp.colStr("debit_flag"));

   selectEndDay();

}

void selectEndDay() throws Exception  {
   String sql1 = " select "
         + " pre_comp_day2  "
         + " from ptr_rskinterval "
         + " where bin_type = ? "
         + " and trans_type = decode(cast(? as varchar(2)),'05','1','2') "
         + " and acq_type = 'NC' ";
   sqlSelect(sql1, new Object[]{wp.colStr("bin_type"), wp.colStr("BL_txn_code")});
   if (sqlRowNum <= 0) {
      wp.colSet("wk_pre_com_day_end", zzdate.dateAdd(wp.colStr("purchase_date"), 0, 0, 0));
   }
   else {
      wp.colSet("wk_pre_com_day_end", zzdate.dateAdd(wp.colStr("purchase_date"), 0, 0, sqlInt("pre_comp_day2")));
   }
}

@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0450Func func = new rskm01.Rskm0450Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   else {
      if (this.isUpdate()) this.saveAfter(true);
      if (this.isDelete()) {
         alertMsg("預備依從刪除成功");
         this.saveAfter(false);
      }
   }
}

@Override
public void procFunc() throws Exception {
//   Rskm0450_func func =new Rskm0450_func();
//   func.setConn(wp);
//
//   rc =func.close_Proce(_proc_close);
//   sql_commit(rc);
//   if (rc !=1) {
//      alertErr(func.mesg());
//      return;
//   }	else	{
//   	if(_proc_close==1)	alert_msg("預備依從權結案成功");
//   	if(_proc_close==2)	alert_msg("依從權結案成功");
//   }
//
//   //--
//   kk1 =wp.colStr("ctrl_seqno");
//   dataRead();
}

@Override
public void initButton() {
   this.btnModeAud();

   if (wp.respHtml.indexOf("_detl") > 0) {
      int li_status = wp.colInt("pre_status");
      if (li_status < 30)
         this.buttonOff("btnProc1_off");
      if (wp.colInt("com_status") < 30)
         this.buttonOff("btnProc2_off");
   }
}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

public void doUpdate1() throws Exception {
   rskm01.Rskm0450Func func = new rskm01.Rskm0450Func();
   func.setConn(wp);
   rc = func.updateU1();
   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   else {
      alertMsg("預備依從存檔成功");
      this.saveAfter(true);
   }
}

public void doUpdate2() throws Exception {
   rskm01.Rskm0450Func func = new rskm01.Rskm0450Func();
   func.setConn(wp);
   rc = func.updateU2();
   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   else {
      this.saveAfter(true);
      wp.respMesg = "[依從權]: 修改成功";
   }
}

public void doDelete2() throws Exception {
   rskm01.Rskm0450Func func = new rskm01.Rskm0450Func();
   func.setConn(wp);
   rc = func.deleteD2();
   sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   else {
      this.saveAfter(true);
      wp.respMesg = "[依從權]: 取消成功";
   }
}

}
