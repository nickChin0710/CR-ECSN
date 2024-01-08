package rskm01;
/**
 * 2020-0515   JH    table.change
 * 2020-0506   JH    table.change
 * 2019-1211   JH    UAT
 * 2019-0907   JH    --rsk_ctrlseqno_log
 * 2018-0122:	JH		modify
 * 預備仲裁/仲裁維護
 */

import busi.FuncAction;

public class Rskm0250Func extends FuncAction {
private boolean ibDebitFlag = false;
private String isRefno = "";
private String isRefnoOri = "";
private String isCtrlSeqno = "", kk2 = "";

@Override
public void dataCheck() {
   ibDebitFlag = eqAny(wp.itemStr("debit_flag"), "Y");
   isCtrlSeqno = wp.colStr("ctrl_seqno");
   kk2 = wp.itemStr("bin_type");
   isRefno = wp.itemStr("reference_no");
   isRefnoOri = wp.itemStr("reference_no_ori");

   if (wp.itemEmpty("reference_no")) {
      errmsg("帳單參考序號: 不可空白");
      return;
   }

   if (this.isAdd())
      return;

   strSql = "select arbit_times, pre_status, arb_status, mod_seqno"
         + " from rsk_prearbit"
         + " where reference_seq =0"
         ;
   
   if(empty(isCtrlSeqno) == false) {
	   strSql += " and ctrl_seqno = ? ";
	   setString(1,isCtrlSeqno);
   }
   
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("控制流水號: 查無資料");
      return;
   }

   if (colNum("mod_seqno") != wp.itemNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }

   //-預備仲裁-
   if (colNum("arbit_times") != 1) {
      errmsg("進度不是[預備仲裁], 不可修改 OR 刪除");
      return;
   }

   return;
}

void getCtrlSeqno() {
   rskm01.RskCtrlseqno ooLog = new rskm01.RskCtrlseqno();
   ooLog.setConn(wp);

   ooLog.getCtrlSeqno(isRefno);
   isCtrlSeqno = ooLog.ctrlSeqno;
   if (empty(isCtrlSeqno)) {
      errmsg(ooLog.getMsg());
   }
}

@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (empty(isCtrlSeqno)) {
      getCtrlSeqno();
   }

   insertPrearbit();
   if (rc != 1) {
      return -1;
   }

   //-update bil_bill-
   updateBilBill();
   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", isCtrlSeqno);
   return rc;
}

@Override
public int dbUpdate() {

   return 0;
}

@Override
public int dbDelete() {
   //-預備仲裁-
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (!wp.itemEmpty("pre_apr_date")) {
      errmsg("已覆核 不可刪除");
      return rc;
   }
   if (!wp.itemEq("arbit_times", "1") || !wp.itemEq("pre_status", "10")) {
      errmsg("不是[預備仲裁] 且為 [新增待覆核]; 不可刪除");
      return rc;
   }

   strSql = " delete rsk_prearbit where ctrl_seqno =? and reference_seq=0";
   setString(1, isCtrlSeqno);

   sqlExec(strSql);
   if (rc != 1) {
      errmsg("delete rsk_prearbit error [%s]!", isCtrlSeqno);
      return rc;
   }

   return rc;
}

@Override
public int dataProc() {
   return 0;
}

public int updateU1() throws Exception  {
   msgOK();
   //-預備仲裁-
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

//   if (!empty(wp.itemStr("arb_apr_date"))) {
//      errmsg("已覆核 不可修改 / 刪除");
//      rc = -1;
//      return rc;
//   }
//   if (!eq_igno(wp.itemStr("arbit_times"), "1") || !wp.item_eq("pre_status", "10")) {
//      errmsg("預備仲裁: 狀態<>10.新增待覆核, 不可修改");
//      return rc;
//   }

   sql2Update("rsk_prearbit");
   addsqlParm("vcr_case_no = ? ",wp.itemStr("vcr_case_no"));
   addsqlParm(", arbit_times = ? ",1);
//   aaa("pre_status",lsStatus);
   addsqlParm(", pre_alloc_code = ? ", wp.itemStr("pre_alloc_code"));
   addsqlParm(", pre_event_date = ? ", wp.itemStr("pre_event_date"));
   addsqlParm(", pre_apply_date = ? ", wp.itemStr("pre_apply_date"));
   addsqlParm(", pre_reason = ? ", wp.itemStr("pre_reason"));
   addsqlParm(", pre_expire_date = ? ", wp.itemStr("pre_expire_date"));
   addsqlParm(", pre_amt = ? ", wp.num("pre_amt"));
   addsqlParm(", pre_result = ? ", wp.itemStr("pre_result"));
   addsqlParm(", pre_remark = ? ", wp.itemStr("pre_remark"));
   if (wp.itemEmpty("pre_result")) {
	   addsqlParm(", pre_close_date = ? ", "");
	   addsqlParm(", pre_close_user = ? ", "");
   }
   else {
	   addsql(", pre_close_date = to_char(sysdate,'yyyymmdd') ");
      addsqlParm(", pre_close_user = ? ", modUser);
   }
   addsqlModXXX(modUser,modPgm);
   sqlWhere(" where ctrl_seqno =?",isCtrlSeqno);
   sqlWhere(" and reference_seq =0","");

   sqlExec(sqlStmt(),sqlParms());
   if (sqlRowNum <= 0) {
      errmsg("update rsk_prearbit.預備仲裁 error; kk=" + isCtrlSeqno);
   }

   return rc;
}

public int updateU2() throws Exception  {
   msgOK();
//   if (!empty(wp.itemStr("arb_apr_date"))) {
//      errmsg("已覆核 不可修改 / 刪除");
//      rc = -1;
//      return rc;
//   }

   isCtrlSeqno = wp.itemStr("ctrl_seqno");
   String lsStatus =wp.itemStr("arb_status");
   if (empty(lsStatus)) {
      lsStatus ="10";
   }

   sql2Update("rsk_prearbit");
   addsqlParm("vcr_case_no = ? ", wp.itemStr("vcr_case_no"));
   addsqlParm(", arbit_times = ? ", 2);
   addsqlParm(", arb_status = ? ", lsStatus);
   addsqlParm(", arb_reason = ? ", wp.itemStr("arb_reason"));
   addsqlParm(", arb_expire_date = ? ",wp.itemStr("arb_expire_date"));
   addsqlParm(", arb_amt = ? ", wp.num("arb_amt"));
   addsqlParm(", arb_result = ? ", wp.itemStr("arb_result"));
   addsqlParm(", arb_remark = ? ", wp.itemStr("arb_remark"));
   addsqlParm(", arb_apply_date = ? ", wp.itemStr("arb_apply_date"));
   addsql(", arb_add_date = to_char(sysdate,'yyyymmdd') ");
   addsqlParm(", arb_add_user = ? ", modUser);
   if (empty(wp.itemStr("arb_result"))) {
	   addsqlParm(", arb_close_date = ? ", "");
	   addsqlParm(", arb_close_user = ? ", "");
   }
   else {
	   addsql(", arb_close_date = to_char(sysdate,'yyyymmdd') ");
      addsqlParm(", arb_close_user = ? ", modUser);
   }
   addsqlModXXX(modUser,modPgm);
   sqlWhere(" where ctrl_seqno =?",isCtrlSeqno);

   sqlExec(sqlStmt(),sqlParms());
   if (sqlRowNum <= 0) {
      errmsg("update rsk_prearbit.仲裁 error; kk=" + isCtrlSeqno);
   }

   return rc;
}

void insertPrearbit() {
   sql2Insert("rsk_prearbit");
   addsqlParm("?","reference_no",wp.itemStr("reference_no"));
   addsqlParm(", ?",", reference_seq", 0);
   addsqlParm(", ?",", ctrl_seqno",isCtrlSeqno);
   addsqlParm(", ?",", bin_type", wp.itemStr("bin_type"));
   addsqlParm(", ?",", debit_flag", wp.itemStr("debit_flag"));
   addsqlParm(", ?",", card_no", wp.itemStr("card_no"));
   addsqlParm(", ?",", vcr_case_no", wp.itemStr("vcr_case_no"));
   addsqlParm(", ?",", acct_month", wp.itemStr("acct_month"));
   addsqlParm(", ?",", purchase_date", wp.itemStr("purchase_date"));
   addsqlParm(", ?",", mcht_country", wp.itemStr("mcht_country"));
   addsqlParm(", ?",", film_no", wp.itemStr("film_no"));
   addsqlParm(", ?",", auth_code", wp.itemStr("auth_code"));
   addsqlParm(", ?",", source_curr", wp.itemStr("source_curr"));
   addsqlParm(", ?",", source_amt", wp.num("source_amt"));
   addsqlParm(", ?",", curr_code", wp.itemStr("curr_code"));
   addsqlParm(", ?",", dc_dest_amt", wp.num("dc_dest_amt"));
   addsqlParm(", ?",", dest_amt", wp.num("dest_amt"));
   addsqlParm(", ?",", settl_amt", wp.num("settl_amt"));
   addsqlParm(", ?",", arbit_times", 1);
   addsqlParm(", ?",", pre_status", "10");
   addsqlParm(", ?",", pre_alloc_code", wp.itemStr("pre_alloc_code"));
   addsqlParm(", ?",", pre_event_date", wp.itemStr("pre_event_date"));
   addsqlParm(", ?",", pre_apply_date", wp.itemStr("pre_apply_date"));
   addsqlParm(", ?",", pre_reason", wp.itemStr("pre_reason"));
   addsqlParm(", ?",", pre_expire_date", wp.itemStr("pre_expire_date"));
   addsqlParm(", ?",", pre_amt", wp.num("pre_amt"));
   addsqlParm(", ?",", pre_result", wp.itemStr("pre_result"));
   addsqlParm(", ?",", pre_remark", wp.itemStr("pre_remark"));
   addsqlYmd(", pre_add_date");
   addsqlParm(", ?",", pre_add_user", modUser);
   addsqlParm(", ?",", reference_no_ori", wp.itemStr("reference_no_ori"));
   addsqlParm(", ?",", pre_close_date", wp.itemStr("pre_close_date"));
   addsqlParm(", ?",", pre_close_user", wp.itemStr("pre_close_user"));
   addsqlParm(", ?",", pre_apr_date", wp.itemStr("pre_apr_date"));
   addsqlParm(", ?",", pre_apr_user", wp.itemStr("pre_apr_user"));
   addsqlModXXX(modUser,modPgm);

   sqlExec(sqlStmt(),sqlParms());
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_prearbit error");
   }

   return;
}

public int deleteD2() throws Exception  {
   msgOK();
   if (!empty(wp.itemStr("arb_apr_date"))) {
      errmsg("已覆核 不可修改 / 刪除");
      return rc;
   }
   if (!eqIgno(wp.itemStr("arbit_times"), "2")) {
      errmsg("仲裁次數 : 2 時 才可 刪除仲裁 (D2)");
      return rc;
   }

   strSql = " update rsk_prearbit set "
         + " arbit_times = '1' , "
         + " arb_status = '' , "
         + " arb_reason = '' , "
         + " arb_result = '' , "
         + " arb_remark = '' , "
         + " arb_apply_date ='',"
         +" arb_expire_date='', arb_amt=0, "
         + " arb_add_date = '' , "
         + " arb_add_user = '' , "
         + " arb_apr_date = '' , "
         + " arb_apr_user = '' , "
         + " arb_close_date = '' , "
         + " arb_close_user = '',"
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where ctrl_seqno =? and reference_seq=0";
   setString(1, wp.itemStr("ctrl_seqno"));
   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("刪除仲裁 (D2) 錯誤  !");
   }

   return rc;
}

void updateBilBill() {
   if (ibDebitFlag) {
      strSql = "update dbb_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post), "
            + " rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   else {
      strSql = "update bil_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post), "
            + " rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   this.sqlExec(strSql, new Object[]{isCtrlSeqno, wp.itemStr("reference_no")});

   if (sqlRowNum != 1) {
      errmsg("update bil[dbb]_bill error; " + sqlErrtext);
   }
}

}
