package rskm02;

import busi.FuncAction;

public class Rskm1230Func extends FuncAction {
String kk1 = "";

@Override
public void dataCheck() {
   if (this.ibAdd) {
      kk1 = wp.itemStr("kk_batch_no");
   }
   else {
      kk1 = wp.itemStr("batch_no");
   }

   if (this.ibDelete) {
      if (empty(wp.itemStr("group_proc_date2")) == false) {
         errmsg("第二分群處理己執行 不可刪除");
         return;
      }
      if (checkJCIC(kk1)) {
         errmsg("名單已匯入JCIC查詢, 不可刪除");
         return;
      }
   }
   if (this.ibDelete) {
      return;
   }

   if (this.ibAdd) {
      if (checkBatchNo(kk1)) {
         errmsg("覆審批號 已存在");
         return;
      }
   }

   if (checkDataYymm() == false) {
      errmsg("往來資料年月不存在");
      return;
   }

   if (empty(wp.itemStr("group_proc_date2")) == false) {
      errmsg("第二次分群已處理, 不可再修改");
      return;
   }

   if (wp.itemEmpty("db_group1") && wp.itemEmpty("db_group2")) {
      errmsg("請勾選 維護分群一 or 維護分群二");
      return;
   }

}

boolean checkJCIC(String kk1)  {
   String sql1 = "select count(*) as db_cnt "
         + " from col_jcic_query_req"
         + " where batch_no =?";
   sqlSelect(sql1, new Object[]{
         kk1
   });
   return colNum("db_cnt") > 0;
}

boolean checkBatchNo(String kk1)  {
   String sql1 = "select count(*) as db_cnt "
         + " from rsk_trial_parm a, rsk_trcorp_mast b "
         + " where 1=1 "
         + " and a.batch_no = ?"
         + " or b.batch_no = ?";
   sqlSelect(sql1, new Object[]{
         kk1, kk1
   });
   return colNum("db_cnt") > 0;
}

boolean checkDataYymm()  {
   String sql1 = "select count(*) as db_cnt "
         + " from rsk_trcorp_bank_stat "
         + " where 1=1 "
         + " and data_yymm = ?";
   sqlSelect(sql1, new Object[]{
         wp.itemStr("data_yymm")
   });
   return colNum("db_cnt") > 0;
}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (wp.itemEq("db_group2", "Y")) {
      strSql = "insert into RSK_TRCORP_MAST ("
            + " batch_no, " //1
            + " batch_desc, "
            + " data_yymm,"
            + " group_date1,"
            + " crt_user1,"//5
            + " apr_user1,"
            + " group_date2,"
            + " corp_jcic_no,"
            + " idno_jcic_no,"
            + " crt_user2, "
            + " apr_user2,"
            + " mod_time,"//10
            + " mod_user,"
            + " mod_pgm,"
            + " mod_seqno"//13
            + " ) values ("
            + " :batch_no,"//1
            + " :batch_desc,"
            + " :data_yymm,"
            + " to_char(sysdate,'yyyymmdd'),"
            + " :crt_user1,"//5
            + " :apr_user1,"
            + " to_char(sysdate,'yyyymmdd'), "
            + " :corp_jcic_no ,"
            + " :idno_jcic_no ,"
            + " :crt_user2,"
            + " :apr_user2,"
            + " sysdate,"//10
            + " :mod_user,"
            + " :mod_pgm,"
            + " 1"//13
            + " )";
      // -set ?value-
      try {
         setString("batch_no", kk1);
         item2ParmStr("batch_desc");
         item2ParmStr("data_yymm");
         item2ParmStr("corp_jcic_no");
         item2ParmStr("idno_jcic_no");
         setString("crt_user1", wp.loginUser);
         setString("crt_user2", wp.loginUser);
         setString("apr_user1", wp.itemStr("zz_apr_user"));
         setString("apr_user2", wp.itemStr("zz_apr_user"));
         setString("mod_user", wp.loginUser);
         setString("mod_pgm", wp.modPgm());
      }
      catch (Exception ex) {
         wp.expHandle("sqlParm", ex);
      }
   }
   else {
      strSql = "insert into RSK_TRCORP_MAST ("
            + " batch_no, " //1
            + " batch_desc, "
            + " data_yymm,"
            + " group_date1,"
            + " crt_user1,"//5
            + " apr_user1,"
            + " group_date2,"
            + " corp_jcic_no,"
            + " idno_jcic_no,"
            + " crt_user2, "
            + " apr_user2,"
            + " mod_time,"
            + " mod_user,"
            + " mod_pgm,"
            + " mod_seqno"
            + " ) values ("
            + " :batch_no,"
            + " :batch_desc,"
            + " :data_yymm,"
            + " to_char(sysdate,'yyyymmdd'),"
            + " :crt_user1,"
            + " :apr_user1,"
            + " '', "
            + " :corp_jcic_no ,"
            + " :idno_jcic_no ,"
            + " '',"
            + " '',"
            + " sysdate,"
            + " :mod_user,"
            + " :mod_pgm,"
            + " 1"
            + " )";
      // -set ?value-
      try {
         setString("batch_no", kk1);
         item2ParmStr("batch_desc");
         item2ParmStr("data_yymm");
         item2ParmStr("corp_jcic_no");
         item2ParmStr("idno_jcic_no");
         setString("crt_user1", wp.loginUser);
         setString("apr_user1", wp.itemStr("zz_apr_user"));
         setString("mod_user", wp.loginUser);
         setString("mod_pgm", wp.modPgm());
      }
      catch (Exception ex) {
         wp.expHandle("sqlParm", ex);
      }
   }


   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
   }

   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "update RSK_TRCORP_MAST set "
         + " batch_desc = :batch_desc, "
         + " data_yymm = :data_yymm, "
         + " corp_jcic_no =:corp_jcic_no , "
         + " idno_jcic_no =:idno_jcic_no , "
   ;
   if (wp.itemEq("db_group1", "Y")) {
      strSql += " group_date1 = to_char(sysdate,'yyyymmdd') , "
            + " crt_user1 = :crt_user1	, "
            + " apr_user1 = :apr_user1,";
   }
   if (wp.itemEq("db_group2", "Y")) {
      strSql += " group_date2 = to_char(sysdate,'yyyymmdd') ,"
            + " crt_user2 = :crt_user2, "
            + " apr_user2 = :apr_user2,"
      ;
   }
   strSql += " mod_user = :mod_user,"
         + " mod_time=sysdate,"
         + " mod_pgm =:mod_pgm,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where batch_no =:kk "
         + " and nvl(mod_seqno,0) =:mod_seqno "
   ;


   setString("kk", kk1);
   item2ParmStr("batch_desc");
   item2ParmStr("data_yymm");
   item2ParmStr("corp_jcic_no");
   item2ParmStr("idno_jcic_no");
   setString("crt_user1", wp.loginUser);
   setString("apr_user1", wp.itemStr("zz_apr_user"));
   setString("crt_user2", wp.loginUser);
   setString("apr_user2", wp.itemStr("zz_apr_user"));
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
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
   strSql = "delete RSK_TRCORP_MAST "
         + " where batch_no =:kk1"
         + " and nvl(mod_seqno,0) =:mod_seqno ";
   setString("kk1", kk1);
   item2ParmNum("mod_seqno");
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   deleteGroup1();
   deleteGroup2();
   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

public int insertGroup1()  {
   msgOK();

   String kk = wp.itemStr("batch_no");
   if (empty(kk)) {
      kk = wp.itemStr("kk_batch_no");
   }

   strSql = "insert into RSK_TRCORP_MAST_GROUP ("
         + " batch_no, " // 1
         + " group_type, "
         + " risk_group, "
         + " seq_no, "
         + " mod_time, mod_pgm"
         + " ) values ("
         + "  :batch_no"
         + ", '1'"
         + ", :risk_group1"
         + ", '0'"
         + ",sysdate,:mod_pgm"
         + " )";
   this.var2ParmStr("batch_no");

   setString("batch_no", kk);
   var2ParmStr("risk_group1");
   setString("mod_pgm", "rskm1230");

   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert insertGroup1 error, " + getMsg());
   }

   return rc;
}

public int insertGroup2()  {
   msgOK();

   String kk = wp.itemStr("batch_no");
   if (empty(kk)) {
      kk = wp.itemStr("kk_batch_no");
   }

   strSql = "insert into RSK_TRCORP_MAST_GROUP ("
         + " batch_no, " // 1
         + " group_type, "
         + " risk_group, "
         + " seq_no, "
         + " mod_time, mod_pgm"
         + " ) values ("
         + "  :batch_no"
         + ", '2'"
         + ", :risk_group2"
         + ", '0'"
         + ",sysdate,:mod_pgm"
         + " )";
   this.var2ParmStr("batch_no");

   setString("batch_no", kk);
   var2ParmStr("risk_group2");
   setString("mod_pgm", "rskm1230");

   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert insertGroup1 error, " + getMsg());
   }

   return rc;
}

public int deleteGroup1()  {
   msgOK();
   String kk = wp.itemStr("batch_no");
   if (empty(kk)) {
      kk = wp.itemStr("kk_batch_no");
   }
   strSql =
         "Delete RSK_TRCORP_MAST_GROUP"
               + " where batch_no =:batch_no "
               + " and group_type='1'"
   ;
   setString("batch_no", kk);
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete deleteGroup1 err=" + getMsg());
      rc = -1;
   }

   return rc;
}

public int deleteGroup2()  {
   msgOK();
   String kk = wp.itemStr("batch_no");
   if (empty(kk)) {
      kk = wp.itemStr("kk_batch_no");
   }
   strSql =
         "Delete RSK_TRCORP_MAST_GROUP"
               + " where batch_no =:batch_no "
               + " and group_type='2'"
   ;
   setString("batch_no", kk);
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete deleteGroup2 err=" + getMsg());
      rc = -1;
   }
   return rc;
}
}
