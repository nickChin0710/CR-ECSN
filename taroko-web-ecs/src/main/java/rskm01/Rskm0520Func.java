package rskm01;
/*
 * 風管代號對照說明檔維護
 * */

public class Rskm0520Func extends busi.FuncAction {
String kk1, kk2;
int ilRskId=0;

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}


@Override
public void dataCheck() {
   if (isAdd()) {
      kk1 = wp.colStr("kk_wf_type");
      kk2 = wp.colStr("kk_wf_id");
   }
   else {
      kk1 = wp.colStr("wf_type");
      kk2 = wp.colStr("wf_id");
   }
   if (isEmpty(kk1) || isEmpty(kk2)) {
      errmsg("[代碼類別, 代碼值] 不可空白");
      return;
   }

   if (this.isAdd()) {
      return;
   }

   sqlWhere = " where wf_type=? and wf_id=? and nvl(mod_seqno,0) =? ";
   Object[] parms = new Object[]{kk1, kk2, wp.itemNum("mod_seqno")};
   if (this.isOtherModify("ptr_sys_idtab", sqlWhere, parms)) {
      //wp.ddd(sql_where,parms);
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

   busi.SqlPrepare ttt = new busi.SqlPrepare();
   ttt.sql2Insert("ptr_sys_idtab");
   ttt.ppstr("wf_type", kk1);
   ttt.ppstr("wf_id", kk2);
   ttt.ppstr("wf_desc", wp.colStr("wf_desc"));
   ttt.ppstr("id_code", wp.itemStr("id_code"));
   ttt.ppstr("id_code2", wp.itemStr("id_code2"));
   ttt.ppstr("id_desc2", wp.itemStr("id_desc2"));
   ttt.modxxx(modUser, modPgm);

   sqlExec(ttt.sqlStmt(), ttt.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("insert error: " + sqlErrtext);
      return rc;
   }

   return rc;
}


@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "update ptr_sys_idtab set " +
         " wf_desc =?, " +
         " id_code =?, " +
         " id_code2 =?, " +
         " id_desc2 =?, " +
         commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1"
         ;
         
   setString(wp.itemStr("wf_desc"));
   setString(wp.itemStr("id_code"));
   setString(wp.itemStr("id_code2"));
   setString(wp.itemStr("id_desc2"));
   
   if(empty(kk1) == false) {	  
	   strSql += " and wf_type = ? ";
	   setString(kk1);
   }
   
   if(empty(kk2) == false) {	   
	   strSql += " and wf_id = ? ";
	   setString(kk2);
   }
   
   if(wp.itemEmpty("mod_seqno") == false) {   
	   strSql += " and nvl(mod_seqno,0) = ? ";
	   setString(wp.itemStr("mod_seqno"));
   }
   
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update error; " + this.sqlErrtext);
      return rc;
   }

   return rc;
}

@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "delete ptr_sys_idtab where 1=1 ";
   
   int ii = 0;
   if(empty(kk1) == false) {
	   ii++;
	   strSql += " and wf_type = ? ";	   
	   setString(ii,kk1);
   }
   
   if(empty(kk2) == false) {
	   ii++;
	   strSql += " and wf_id = ? ";
	   setString(ii,kk2);
   }
   
   if(wp.itemEmpty("mod_seqno") == false) {
	   ii++;
	   strSql += " and nvl(mod_seqno,0) = ? ";
	   setString(ii,wp.itemStr("mod_seqno"));
   }   
   
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("delete error: " + sqlErrtext);
      return rc;
   }

   return rc;
}

@Override
public int dataProc() {
   return 0;
}

}
