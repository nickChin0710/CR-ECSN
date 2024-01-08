package rskm01;

/*再提示整批登錄覆核-主管作業 V.2018-0409
 *
 * */

import busi.FuncProc;

public class Rskp0230Func extends FuncProc {
String kk1 = "";
double modSeqno = 0;

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect() {
   // TODO Auto-generated method stub
   return 0;
}

void selectRskChgback() {
   strSql = "select ctrl_seqno,bin_type, reference_no,"
         + " chg_stage, sub_stage,"
         + " rep_apr_date, final_close,"
         + " txn_code, rep_gl_date,"
         + " uf_rsk_stage_days(bin_type,decode(txn_code,'05','1','2'),'2','') as db_expr_date,"
         + " mod_seqno"
         + " from rsk_chgback"
         + " where rowid =? and mod_seqno =?";

   setRowId(kk1);
   setDouble(modSeqno);
   daoTid = "A.";
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料已不存在 or 已被修改");
      return;
   }
}

//--覆核-------------------------------------------------------------------------
@Override
public void dataCheck() {
   selectRskChgback();
   if (rc != 1)
      return;

   if (colNum("A.mod_seqno") != varsNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }

   if (colEmpty("A.rep_apr_date") == false) {
      errmsg("再提示登錄: 已放行, 不可再覆核");
      return;
   }
   if (colEmpty("A.final_close") == false) {
      errmsg("扣款已結案，請取消[扣款結案]再覆核");
      return;
   }
}

@Override
public int dataProc() {
   kk1 = varsStr("rowid");
   modSeqno = varsNum("mod_seqno");

   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "update rsk_chgback set"
         + "  sub_stage ='30' "
         + ", rep_status ='30'"
         + ", rep_apr_user = ? "
         + ", rep_apr_date =" + commSqlStr.sysYYmd
         + ", rep_expire_date = ? "
         + ", gl_proc_flag   ='2'"
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1 "
         + " and rowid = ?  ";

   setString(modUser);
   setString(colStr("A.db_expr_date"));
   setRowId(kk1);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      sqlErr("update rsk_chgback error; kk=" + colStr("A.ctrl_seqno"));
      return -1;
   }

   return rc;
}

// --解覆核
public void dataCheck_cancel()  throws Exception {
   kk1 = varsStr("rowid");
   modSeqno = varsNum("mod_seqno");

   selectRskChgback();
   if (rc != 1)
      return;
   if (colNum("A.mod_seqno") != varsNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }

   if (!colEmpty("A.final_close")) {
      errmsg("扣款已結案，不可執行 [覆核/解覆核]");
      return;
   }
   if (colEmpty("A.rep_apr_date")) {
      errmsg("資料未放行, 不可取消放行");
      return;
   }
   this.dateTime();
   if (colNeq("A.rep_apr_date", this.sysDate)) {
      errmsg("資料不是當天放行, 不可取消放行");
      return;
   }
   if (colEmpty("A.rep_gl_date") == false) {
      errmsg("再提示已啟帳, 不可取消放行");
      return;
   }
}

public int cancelProc() throws Exception  {
   kk1 = varsStr("rowid");
   modSeqno = varsNum("mod_seqno");

   dataCheck_cancel();
   if (rc != 1) {
      return rc;
   }

   strSql = "update rsk_chgback set"
         + "  sub_stage ='10' "
         + ", rep_status ='10' "
         + ", rep_apr_user =''"
         + ", rep_apr_date = '' "
         + ", rep_expire_date = '' "
         + ", gl_proc_flag = '0'"
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1"
         + " and rowid = ? ";
   
   setRowId(1,kk1);
   
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      sqlErr("update rsk_chgback kk=" + colStr("A.ctrl_seqno"));
      return -1;
   }

   return rc;
}

}
