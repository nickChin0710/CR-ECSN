package rskm03;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Rskm3130Func extends FuncEdit {
String kk1 = "", kk2 = "";

public Rskm3130Func(TarokoCommon wr) {
   wp = wr;
   this.conn = wp.getConn();
}

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

@Override
public void dataCheck()  {

   kk1 = wp.itemStr("rowid");

   sqlWhere = " where 1=1"
         + " and rowid = :rowid "
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   this.setRowId("rowid", kk1);
   if (this.isOtherModify("RSK_CTFG_proc", sqlWhere)) {
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

   strSql = " update rsk_ctfg_proc set "
         + " proc_date =:proc_date ,"
         + " proc_time =:proc_time ,"
         + " rejt_reason =:rejt_reason ,"
         + " rejt_amt =:rejt_amt ,"
         + " otb_amt =:otb_amt ,"
         + " ok_cnt =:ok_cnt ,"
         + " ok_amt =:ok_amt ,"
         + " view_date =:view_date ,"
         + " send_sms_flag =:send_sms_flag ,"
         + " close_flag =:close_flag ,"
         + " proc_user =:proc_user ,"
         + " contr_result =:contr_result ,"
         + " proc_status =:proc_status ,"
         + " mod_time =sysdate ,"
         + " mod_user =:mod_user ,"
         + " mod_pgm =:mod_pgm ,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where rowid =:rowid "
   ;

   item2ParmStr("proc_date");
   item2ParmStr("proc_time");
   item2ParmStr("rejt_reason");
   item2ParmNum("rejt_amt");
   item2ParmNum("otb_amt");
   item2ParmNum("ok_cnt");
   item2ParmNum("ok_amt");
   item2ParmStr("view_date");
   item2ParmStr("send_sms_flag");
   item2ParmStr("close_flag");
   item2ParmStr("proc_user");
   item2ParmStr("contr_result");
   item2ParmStr("proc_status");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3130");
   setRowId("rowid", kk1);

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfg_proc error " + this.sqlErrtext);
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
   strSql = "delete rsk_ctfg_proc where 1=1"+commSqlStr.whereRowid;
   setString(1, kk1);

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("delete rsk_ctfg_proc error " + sqlErrtext);
   }
   return rc;
}
}

