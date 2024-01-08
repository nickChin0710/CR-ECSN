package rskm01;
/**
 * 2020-0512   JH    modify
 * 預備仲裁/仲裁登錄覆核 V.2018-0409
 * */

import busi.FuncAction;

public class Rskp0250Func extends FuncAction {
String kk1 = "";

void selectRskPrearbit() {
   strSql = "select mod_seqno"
         + ", ctrl_seqno, bin_type, reference_no"
         + ", arbit_times, pre_apr_date, pre_status"
         +", arb_apr_date, arb_status"
         + " from rsk_prearbit"
         + " where rowid =?"
   ;
   setRowId(1, kk1);
   daoTid = "A.";
   sqlSelect(strSql);

   if (sqlRowNum <= 0) {
      sqlErr("rsk_prearbit.Select");
      return;
   }
   if (colNum("A.mod_seqno") != varsNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }
}

@Override
public void dataCheck() {
   selectRskPrearbit();
   if (rc != 1)
      return;

   int liTimes =colInt("A.arbit_times");
   if (liTimes==1 && colEmpty("A.pre_apr_date") == false) {
      errmsg("主管已覆核, 不須執行此作業");
      return;
   }
   if (liTimes==2 && colEmpty("A.arb_apr_date") == false) {
      errmsg("主管已覆核, 不須執行此作業");
      return;
   }
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
   kk1 = varsStr("rowid");
   dataCheck();
   if (rc != 1)
      return rc;

   int liTimes =colInt("A.arbit_times");
   if (liTimes ==1) {
      strSql ="update rsk_prearbit set"+
            " pre_status ='30'"+
            ", pre_apr_date ="+commSqlStr.sysYYmd+
            ", pre_apr_user =?"+
            " where rowid =?";
   }
   else if (liTimes ==2) {
      strSql ="update rsk_prearbit set"+
            " arb_status ='30'"+
            ", arb_apr_date ="+commSqlStr.sysYYmd+
            ", arb_apr_user =?"+
            " where rowid =?";
   }
   else {
      return 1;
   }

   setString(modUser);
   setRowId(kk1);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("覆核失敗");
      return rc;
   }

   return rc;
}

}
