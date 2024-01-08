package rskm02;
/**
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 */

import busi.FuncAction;

public class Rskm1250Func extends FuncAction {
String kk1 = "", kk2 = "";

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("batch_no");
   kk2 = wp.itemStr("corp_no");

   if (empty(wp.itemStr("action_code"))) {
      errmsg("Action Code 不可空白");
      return;
   }

   if (empty(wp.itemStr("close_apr_date")) == false) {
      errmsg("主管已覆核, 不可修改/刪除");
      return;
   }

   sqlWhere = " where 1=1 "
         + " and batch_no =?"
         + " and corp_no =?"
         + " and nvl(mod_seqno,0) =?";
   Object[] parms = new Object[]{kk1, kk2, wp.itemNum("mod_seqno")};
   if (this.isOtherModify("rsk_trcorp_list", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
   }

}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }


   strSql = "update rsk_trcorp_list set "
         + " close_date = to_char(sysdate,'yyyymmdd'), "
         + " close_user = :close_user, "
         + " action_code = :action_code, "
         + " trial_remark = :trial_remark, "
         + " trial_remark2 = :trial_remark2	, "
         + " trial_remark3 = :trial_remark3,"
         + " mod_user = :mod_user,"
         + " mod_time=sysdate,"
         + " mod_pgm =:mod_pgm,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where batch_no =:kk1 "
         + " and corp_no=:kk2"
         + " and nvl(mod_seqno,0) =:mod_seqno ";

   setString("kk1", kk1);
   setString("kk2", kk2);
   item2ParmStr("action_code");
   item2ParmStr("trial_remark");
   item2ParmStr("trial_remark2");
   item2ParmStr("trial_remark3");
   setString("close_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "w_rskm1250");
   item2ParmNum("mod_seqno");

   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;
}

@Override
public int dbDelete()  {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "delete rsk_trcorp_list "
         + " where batch_no =:kk1 "
         + " and corp_no=:kk2"
         + " and nvl(mod_seqno,0) =:mod_seqno ";
   setString("kk1", kk1);
   setString("kk2", kk2);
   item2ParmNum("mod_seqno");
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }

   return rc;
}

@Override
public int dataProc()  {
   dataCheck2();
   if (rc != 1) {
      return rc;
   }


   strSql = "update rsk_trcorp_list set "
         + " close_date = to_char(sysdate,'yyyymmdd'), "
         + " close_user = :close_user, "
         + " action_code = '0', "
         + " mod_user = :mod_user,"
         + " mod_time=sysdate,"
         + " mod_pgm =:mod_pgm,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where rowid =:rowid "
         + " and nvl(mod_seqno,0) =:mod_seqno ";


   setString("close_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm1250");
   setRowId("rowid", varsStr("rowid"));
   var2ParmNum("mod_seqno");

   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;
}

public int dataProc2()  {
   dataCheck2();
   if (rc != 1) {
      return rc;
   }


   strSql = "update rsk_trcorp_list set "
         + " close_date = '', "
         + " close_user = '', "
         + " action_code = '', "
         + " mod_user = ?,"
         + " mod_time=sysdate,"
         + " mod_pgm = ?,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where 1=1 "
         + " and nvl(mod_seqno,0) =? "
         + " and rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)";
   ;

   int ii=1;
   //setParm(ii++, wp.loginUser);
   setParm(ii++, wp.loginUser);
   setParm(ii++, modPgm);
   setParm(ii++, varsNum("mod_seqno"));
   setParm(ii++, varsStr("rowid"));

   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;
}

public void dataCheck2()  {


   if (empty(varsStr("close_apr_date")) == false) {
      errmsg("主管己覆核, 不可修改");
      return;
   }

   sqlWhere = " where 1=1 "
         + " and nvl(mod_seqno,0) =?"
         //+ " and rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)"
   + commSqlStr.whereRowid
   ;
   Object[] parms = new Object[]{varsNum("mod_seqno"), varsStr("rowid")};
   if (this.isOtherModify("rsk_trcorp_list", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
   }
}


}
