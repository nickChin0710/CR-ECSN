package rskm02;

import busi.FuncAction;

public class Rskp1250Func extends FuncAction {

@Override
public void dataCheck() {
   // TODO Auto-generated method stub

}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataProc() {
   strSql = "update rsk_trcorp_list set"
         + "  close_apr_date = to_char(sysdate,'yyyymmdd') "
         + ", close_apr_user =? "
         + ", mod_time = sysdate"
         + ", mod_user =? "
         + ", mod_pgm =? "
         + ", mod_seqno =mod_seqno+1 "
         + " where 1=1"
         +commSqlStr.whereRowid
         + " and mod_seqno=?"
   ;
   int ii=1;
   setParm(ii++, wp.loginUser);
   setParm(ii++, wp.loginUser);
   setParm(ii++, modPgm);
   setParm(ii++, varsStr("rowid"));
   setParm(ii++, varsNum("mod_seqno"));

   sqlExec(strSql);
   log("sqlRowNum:" + sqlRowNum);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_trcorp_list error; " + this.getMsg());
   }

   return rc;

}

public int OffdataProc()  throws Exception {
   strSql = "update rsk_trcorp_list set"
         + "  close_apr_date = '' "
         + ", close_apr_user = '' "
         + ", mod_time = sysdate"
         + ", mod_user =? "
         + ", mod_pgm =? "
         + ", mod_seqno =mod_seqno+1 "
         + " where 1=1"+commSqlStr.whereRowid
         + " and mod_seqno =?"
   ;

   int ii=1;
   setParm(ii++, modUser);
   setParm(ii++, modPgm);
   setParm(ii++, varsStr("rowid"));
   setParm(ii++, varsNum("mod_seqno"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_trcorp_list error; " + this.getMsg());
   }

   return rc;

}

}
