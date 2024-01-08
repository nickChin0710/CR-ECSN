package rskm01;

/* 預備依從/依從權主管覆核
   2019-0507:  JH    ++ pre_apr_date
 * 
 * */

import busi.FuncAction;

public class Rskp0450Func extends FuncAction {
String kk1 = "";
public int ilProcRow = -1;

@Override
public void dataCheck() {
   selectRskCompl();
   if (rc != 1)
      return;

   if (colNum("A.mod_seqno") != varsNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }

//	if (col_empty("A.com_clo_result")) {
//		errmsg("[依從權結果] 空白, 不可覆核");
//		return;
//	}
   if (colInt("A.compl_times") == 1) {
      if (colEmpty("A.pre_apr_date") == false) {
         errmsg("預備依從已覆核, 不須處理");
         return;
      }
   }
   else {
      if (colEmpty("A.com_apr_date") == false) {
         errmsg("依從權已覆核, 不需處理");
         return;
      }
   }

}

void selectRskCompl() {
   strSql = "select mod_seqno, bin_type, reference_no, ctrl_seqno, compl_times" +
         ", pre_status, pre_apr_date"
         + ", com_status, com_clo_result, com_close_date"
         + ", com_apr_date"
         + " from rsk_precompl"
         + " where rowid =?";
   setRowId(1, kk1);
   daoTid = "A.";
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      sqlErr("rsk_precompl.select kk=" + kk1);
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
   msgOK();
   kk1 = varsStr("rowid");
   dataCheck();
   if (rc != 1)
      return rc;

   int li_compl_times = colInt("A.compl_times");
   if (li_compl_times == 1) {
      strSql = "update rsk_precompl set"
            + " pre_status =decode(pre_status,'10','30',pre_status)"
            + ", pre_apr_date =" + commSqlStr.sysYYmd
            + ", pre_apr_user =?"
            + "," + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =?";
   }
   else {
      strSql = " update rsk_precompl set "
            + " com_status =decode(com_status,'10','30',com_status)"
            + ", com_apr_date =" + commSqlStr.sysYYmd
            + ", com_apr_user =?"
            + "," + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =?";
   }
   setString(modUser);
   setRowId(kk1);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      sqlErr("覆核失敗, kk=" + kk1);
      return rc;
   }

   return rc;
}

}
