package rskm03;

import busi.FuncAction;

public class Rskm3108Func extends FuncAction {
String kk1 = "";

@Override
public void dataCheck() {
   if (ibAdd) kk1 = wp.itemStr("kk_case_type");
   else kk1 = wp.itemStr("case_type");

   if (empty(kk1)) {
      errmsg("案件類型不可空白");
      return;
   }

}

@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1) return rc;

   strSql = " insert into rsk_ctfi_casetype ("
         + " case_type , "
         + " case_desc , "
         + " event_desc , "
         + " event_risk , "
         + " event_reason , "
         + " proc_code1 , "
         + " event_type , "
         + " loss_code , "
         + " loss_ac_no , "
         + " proc_code2 , "
         + " loss_recov_desc , "
         + " loss_flag1 ,"
         + " loss_amt1 ,"
         + " loss_ac_no1 ,"
         + " loss_flag2 ,"
         + " loss_amt2 ,"
         + " loss_ac_no2 ,"
         + " crt_date , "
         + " crt_user , "
         + " apr_flag , "
         + " mod_user , "
         + " mod_time , "
         + " mod_pgm , "
         + " mod_seqno "
         + " ) values ( "
         + " :kk1 , "
         + " :case_desc , "
         + " :event_desc , "
         + " :event_risk , "
         + " :event_reason , "
         + " :proc_code1 , "
         + " :event_type , "
         + " :loss_code , "
         + " :loss_ac_no , "
         + " :proc_code2 , "
         + " :loss_recov_desc , "
         + " :loss_flag1 ,"
         + " :loss_amt1 ,"
         + " :loss_ac_no1 ,"
         + " :loss_flag2 ,"
         + " :loss_amt2 ,"
         + " :loss_ac_no2 ,"
         + " to_char(sysdate,'yyyymmdd') , "
         + " :crt_user , "
         + " 'Y' , "
         + " :mod_user , "
         + " sysdate , "
         + " :mod_pgm , "
         + " 1 "
         + " )"
   ;

   setString("kk1", kk1);
   item2ParmStr("case_desc");
   item2ParmStr("event_desc");
   item2ParmStr("event_risk");
   item2ParmStr("event_reason");
   item2ParmStr("proc_code1");
   item2ParmStr("event_type");
   item2ParmStr("loss_code");
   item2ParmStr("loss_ac_no");
   item2ParmStr("proc_code2");
   item2ParmStr("loss_recov_desc");
   item2ParmStr("loss_flag1");
   item2ParmNum("loss_amt1");
   item2ParmStr("loss_ac_no1");
   item2ParmStr("loss_flag2");
   item2ParmNum("loss_amt2");
   item2ParmStr("loss_ac_no2");
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3108");

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfi_casetype error !");
   }

   return rc;
}

@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1) return rc;

   strSql = " update rsk_ctfi_casetype set "
         + " case_desc =:case_desc ,"
         + " event_desc =:event_desc ,"
         + " event_risk =:event_risk ,"
         + " event_reason =:event_reason ,"
         + " proc_code1 =:proc_code1 ,"
         + " event_type =:event_type ,"
         + " loss_code =:loss_code ,"
         + " loss_ac_no =:loss_ac_no ,"
         + " proc_code2 =:proc_code2 ,"
         + " loss_recov_desc =:loss_recov_desc ,"
         + " loss_flag1 =:loss_flag1 ,"
         + " loss_amt1 =:loss_amt1 ,"
         + " loss_ac_no1 =:loss_ac_no1 ,"
         + " loss_flag2 =:loss_flag2 ,"
         + " loss_amt2 =:loss_amt2 ,"
         + " loss_ac_no2 =:loss_ac_no2 ,"
         + " apr_flag ='Y' ,"
         + " mod_user =:mod_user ,"
         + " mod_time = sysdate ,"
         + " mod_pgm =:mod_pgm ,"
         + " mod_seqno = nvl(mod_seqno,0)+1 "
         + " where case_type =:kk1 "
         + " and mod_seqno =:mod_seqno "
   ;

   item2ParmStr("case_desc");
   item2ParmStr("event_desc");
   item2ParmStr("event_risk");
   item2ParmStr("event_reason");
   item2ParmStr("proc_code1");
   item2ParmStr("event_type");
   item2ParmStr("loss_code");
   item2ParmStr("loss_ac_no");
   item2ParmStr("proc_code2");
   item2ParmStr("loss_recov_desc");
   item2ParmStr("loss_flag1");
   item2ParmNum("loss_amt1");
   item2ParmStr("loss_ac_no1");
   item2ParmStr("loss_flag2");
   item2ParmNum("loss_amt2");
   item2ParmStr("loss_ac_no2");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3108");
   setString("kk1", kk1);
   item2ParmNum("mod_seqno");

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfi_casetype error ! ");
   }

   return rc;
}

@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1) return rc;
   strSql = " delete rsk_ctfi_casetype where case_type =:kk1 and mod_seqno =:mod_seqno ";
   setString("kk1", kk1);
   item2ParmNum("mod_seqno");

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("delete rsk_ctfi_casetype error ! ");
   }

   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

}
