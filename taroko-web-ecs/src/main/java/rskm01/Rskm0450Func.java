package rskm01;
/**
 * 2020-0506   JH    modify
 * 2019-1211   JH    UAT
 * 2019-0907   JH    --rsk_ctrlseqno_log
 * 2018-0213:	JH		modify
 * 預備依從/依從權維護
 */

import busi.FuncAction;

public class Rskm0450Func extends FuncAction {
String isCtrlSeqno = "";
private boolean idDebitFlag = false;
private String isRefno = "";
private String isRefnoOri = "";

@Override
public void dataCheck() {
   idDebitFlag = eqAny(wp.itemStr("debit_flag"), "Y");
   isCtrlSeqno = wp.itemStr("ctrl_seqno");
   isRefno = wp.itemStr("reference_no");
   isRefnoOri = wp.itemStr("reference_no_ori");

   if (wp.itemEmpty("reference_no")) {
      errmsg("帳單參考序號: 不可空白");
      return;
   }

   if (this.isAdd()) {
      return;
   }

   selectRskCompl();
   if (rc != 1)
      return;

   //-預備依從-
   if (colNum("A.compl_times") != 1) {
      errmsg("進度不是[預備仲裁], 不可修改 OR 刪除");
      return;
   }
   if (colNeq("A.pre_status", "10")) {
      errmsg("預備依從: 不是[待覆核], 不可修改 OR 刪除");
      return;
   }
   if (colEmpty("A.com_apr_date") == false) {
      errmsg("主管己覆核, 不可異動");
      return;
   }

   //-OK-
   if (wp.itemEmpty("pre_clo_result") == false) {
      if (wp.itemEmpty("pre_close_date")) {
         wp.itemSet("pre_close_date", this.getSysDate());
      }
      if (wp.itemEmpty("pre_close_user")) {
         wp.itemSet("pre_close_user", modUser);
      }
   }
   else {
      wp.itemSet("pre_close_date", "");
      wp.itemSet("pre_close_user", "");
   }

   return;
}

void selectRskCompl() {

   strSql = "select ctrl_seqno, bin_type, compl_times, pre_status, com_status"
         + ", com_apr_date, com_apr_user"
         + ", mod_seqno"
         + " from rsk_precompl"
         + " where reference_seq =0"
         ;   
   daoTid = "A.";
   
   if(empty(isCtrlSeqno) == false) {
	   strSql += " and ctrl_seqno = ? ";
	   setString(1,isCtrlSeqno);
   }
   
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("控制流水號: 查無資料");
      return;
   }

   if (colNum("A.mod_seqno") != wp.itemNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
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

   insertPrecompl();
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

void getCtrlSeqno() {
   rskm01.RskCtrlseqno ooLog = new rskm01.RskCtrlseqno();
   ooLog.setConn(wp);
   ooLog.getCtrlSeqno(isRefno);
   isCtrlSeqno = ooLog.ctrlSeqno;
}

void updateBilBill() {
   if (wp.itemEmpty("reference_no"))
      return;

   if (idDebitFlag) {
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
   this.sqlExec(strSql, new Object[]{
         isCtrlSeqno, wp.itemStr("reference_no")
   });

   if (sqlRowNum != 1) {
      errmsg("update bil[dbb]_bill error; " + sqlErrtext);
   }
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (!empty(wp.itemStr("com_apr_date"))) {
      errmsg("已覆核 不可修改 / 刪除");
      return rc;
   }
   if (!wp.itemEq("compl_times", "1") || !wp.itemEq("pre_status", "10")) {
      errmsg("不是[預備依從] 且為 [新增待覆核]; 不可刪除");
      return rc;
   }

   strSql = " delete rsk_precompl where ctrl_seqno =:ctrl_seqno and reference_seq=0 ";
   item2ParmStr("ctrl_seqno");
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("delete rsk_precompl error kk=[%s]!", isCtrlSeqno);
      return rc;
   }

   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

public int closeProce(int ai_time) throws Exception  {
   msgOK();
   isCtrlSeqno = wp.colStr("ctrl_seqno");
   isRefno = wp.colStr("reference_no");

   if (ai_time == 1) {
      closeUpdate1();
   }
   else if (ai_time == 2) {
      closeUpdate2();
   }

   return rc;
}

void closeUpdate1() {
   if (wp.colInt("pre_status") == 10) {
      errmsg("主管未覆核, 不可結案");
      return;
   }
   if (wp.colEmpty("pre_clo_result")) {
      errmsg("執行結果: 不可空白");
      return;
   }

   strSql = "update rsk_precompl set"
         + " pre_status ='80'"
         + ", pre_clo_result =?"
         + ", pre_remark =?"
         + ", pre_close_date =" + commSqlStr.sysYYmd
         + ", pre_close_user =?"
         + ", " + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?";
   setString(1, wp.colStr("pre_clo_result"));
   setString(wp.colStr("pre_remark"));
   setString(modUser);
   setRowId(wp.colStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum < 1) {
      sqlErr("update rsk_precompl err");
      return;
   }
}

void closeUpdate2() throws Exception  {
   if (wp.colInt("com_status") == 10) {
      errmsg("主管未覆核, 不可結案");
      return;
   }
   if (wp.colEmpty("com_clo_result")) {
      errmsg("執行結果: 不可空白");
      return;
   }

   strSql = "update rsk_precompl set"
         + " com_status ='80'"
         + ", com_clo_result =?"
         + ", com_remark =?"
         + ", com_close_date =" + commSqlStr.sysYYmd
         + ", com_close_user =?"
         + ", " + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?";
   setString(1, wp.colStr("com_clo_result"));
   setString(wp.colStr("com_remark"));
   setString(modUser);
   setRowId(wp.colStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum < 1) {
      sqlErr("update rsk_precompl err");
      return;
   }
}

void insertPrecompl() {
   sql2Insert("rsk_precompl");
   addsqlParm("?","ctrl_seqno",isCtrlSeqno);
   addsqlParm(", ?"," , compl_times",1);
   addsqlParm(", ?"," , bin_type",wp.itemStr("bin_type"));
   addsqlParm(", ?"," , debit_flag",wp.itemStr("debit_flag"));
   addsqlParm(", ?"," , card_no",wp.itemStr("card_no"));
   addsqlParm(", ?"," , vcr_case_no",wp.itemStr("vcr_case_no"));
   addsqlParm(", ?"," , acct_month",wp.itemStr("acct_month"));
   addsqlParm(", ?"," , purchase_date",wp.itemStr("purchase_date"));
   addsqlParm(", ?"," , mcht_country",wp.itemStr("mcht_country"));
   addsqlParm(", ?"," , film_no",wp.itemStr("film_no"));
   addsqlParm(", ?"," , auth_code",wp.itemStr("auth_code"));
   addsqlParm(", ?"," , source_curr",wp.itemStr("source_curr"));
   addsqlParm(", ?"," , source_amt",wp.num("source_amt"));
   addsqlParm(", ?"," , curr_code",wp.itemStr("curr_code"));
   addsqlParm(", ?"," , dc_dest_amt", wp.num("dc_dest_amt"));
   addsqlParm(", ?"," , dest_amt", wp.num("dest_amt"));
   addsqlParm(", ?"," , settl_amt", wp.num("settl_amt"));
   addsqlParm(", ?"," , event_date",wp.itemStr("event_date"));
   addsqlParm(", ?"," , viol_date",wp.itemStr("viol_date"));
   addsqlParm(", ?"," , pre_status",wp.itemStr("pre_status"));
   addsqlParm(", ?"," , pre_apply_date",wp.itemStr("pre_apply_date"));
   addsqlParm(", ?"," , pre_clo_result",wp.itemStr("pre_clo_result"));
   addsqlParm(", ?"," , pre_remark",wp.itemStr("pre_remark"));
   addsqlParm(", ?"," , pre_expire_date",wp.itemStr("pre_expire_date"));
   addsqlYmd(", pre_add_date");
   addsqlParm(", ?"," , pre_add_user", modUser);
   addsqlParm(", ?"," , pre_close_date",wp.itemStr("pre_close_date"));
   addsqlParm(", ?"," , pre_close_user",wp.itemStr("pre_close_user"));
   addsqlParm(", ?"," , reference_no",wp.itemStr("reference_no"));
   addsqlParm(", ?"," , reference_no_ori",wp.itemStr("reference_no_ori"));
   addsqlParm(", ?"," , reference_seq",0);
   addsqlParm(", ?"," , pre_amt", wp.num("pre_amt"));
   addsqlModXXX(modUser, modPgm);

   sqlExec(sqlStmt(),sqlParms());
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_precompl error !");
   }
   return;
}

public int updateU2() throws Exception  {
   msgOK();
   isCtrlSeqno = wp.itemStr("ctrl_seqno");
   selectRskCompl();
   if (rc != 1)
      return rc;

   if (colEmpty("A.com_apr_date") == false) {
      errmsg("主管己覆核, 不可異動[依從權]");
      return rc;
   }
   if (!colEmpty("A.com_status") && colNeq("A.com_status", "10")) {
      errmsg("依從權: 不是 [待覆核] 不可修改 / 刪除");
      return rc;
   }

   //-OK-
   if (wp.itemEmpty("com_clo_result") == false) {
      if (wp.itemEmpty("com_close_date")) {
         wp.itemSet("com_close_date", this.getSysDate());
      }
      if (wp.itemEmpty("com_close_user")) {
         wp.itemSet("com_close_user", modUser);
      }
   }
   else {
      wp.itemSet("com_close_date", "");
      wp.itemSet("com_close_user", "");
   }

   sql2Update("rsk_precompl");
   addsqlParm("compl_times = ? ",2);
   addsqlParm(" , com_status = ? ","10");
   addsqlParm(" , com_apply_date = ? ",wp.itemStr("com_apply_date"));
   addsqlParm(" , com_expire_date = ? ",wp.itemStr("com_expire_date"));
   addsqlParm(" , com_clo_result = ? ",wp.itemStr("com_clo_result"));
   addsqlParm(" , com_remark = ? ",wp.itemStr("com_remark"));
   addsql(" , com_add_date = to_char(sysdate,'yyyymmdd') ");
   addsqlParm(" , com_add_user = ? ", modUser);
   addsqlParm(" , com_close_date = ? ",wp.itemStr("com_close_date"));
   addsqlParm(" , com_close_user = ? ",wp.itemStr("com_close_user"));
   addsqlParm(" , com_amt = ? ", wp.num("com_amt"));
   addsqlModXXX(modUser,modPgm);
   sqlWhere(" where ctrl_seqno =?",isCtrlSeqno);
   sqlWhere(" and reference_seq =0","");

   sqlExec(sqlStmt(),sqlParms());
   if (sqlRowNum <= 0) {
      errmsg("update rsk_precompl (U2) error !");
      return rc;
   }

   return rc;
}

public int updateU1() throws Exception  {
   //-預備依從-
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (!empty(wp.itemStr("com_apr_date"))) {
      errmsg("已覆核 不可修改 / 刪除");
      rc = -1;
      return rc;
   }

   if (!wp.itemEq("compl_times", "1") || !wp.itemEq("pre_status", "10")) {
      errmsg("預備依從: 狀態<>10.新增待覆核, 不可修改");
      return rc;
   }
//   double lm_pre_amt = wp.item_num("pre_amt");
//   if (lm_pre_amt == 0) {
//      lm_pre_amt = wp.item_num("dc_dest_amt");
//   }

   sql2Update("rsk_precompl");
   addsqlParm("compl_times = ? ",1);
   addsqlParm(" , vcr_case_no = ? ",wp.itemStr("vcr_case_no"));
   addsqlParm(" , event_date = ? ",wp.itemStr("event_date"));
   addsqlParm(" , viol_date = ? ",wp.itemStr("viol_date"));
   addsqlParm(" , pre_status = ? ",wp.itemStr("pre_status"));
   addsqlParm(" , pre_apply_date = ? ",wp.itemStr("pre_apply_date"));
   addsqlParm(" , pre_clo_result = ? ",wp.itemStr("pre_clo_result"));
   addsqlParm(" , pre_remark = ? ",wp.itemStr("pre_remark"));
   addsqlParm(" , pre_expire_date = ? ",wp.itemStr("pre_expire_date"));
   addsqlParm(" , pre_close_date = ? ",wp.itemStr("pre_close_date"));
   addsqlParm(" , pre_close_user = ? ",wp.itemStr("pre_close_user"));
   addsqlParm(" , pre_amt = ? ", wp.num("pre_amt"));
   addsqlModXXX(modUser,modPgm);
   sqlWhere(" where ctrl_seqno =?", isCtrlSeqno);
   sqlWhere(" and reference_seq =0","");

   sqlExec(sqlStmt(), sqlParms());
   if (sqlRowNum <= 0) {
      errmsg("update rsk_precompl.預備依從 error kk[%s]!", isCtrlSeqno);
   }

   return rc;
}

public int deleteD2() throws Exception  {
   msgOK();
   isCtrlSeqno = wp.itemStr("ctrl_seqno");
   selectRskCompl();
   if (rc != 1)
      return rc;

   if (!colEmpty("A.com_apr_date")) {
      errmsg("[依從權]已覆核 不可取消");
      return rc;
   }
   if (colNeq("A.compl_times", "2")) {
      errmsg("依從權階段 : 不是[2.依從權] 不可取消(D2)");
      return rc;
   }
   if (colEmpty("A.com_apr_date") == false) {
      errmsg("主管己覆核, 不可[取消依從權]");
      return rc;
   }


   strSql = " update rsk_precompl set "
         + " compl_times ='1' ,"
         + " com_status ='' ,"
         + " com_apply_date ='' ,"
         + " com_expire_date ='' ,"
         + " com_clo_result ='' ,"
         + " com_remark ='' ,"
         + " com_add_date ='' ,"
         + " com_add_user ='' ,"
         + " com_close_date ='' ,"
         + " com_close_user ='' ,"
         +" com_amt =0, "
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where ctrl_seqno =? and reference_seq=0 ";

   setString(1, isCtrlSeqno);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("刪除依從 錯誤 !");
   }

   return rc;
}

}
