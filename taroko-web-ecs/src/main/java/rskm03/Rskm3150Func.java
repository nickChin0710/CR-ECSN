package rskm03;
/* 系統參數對照表維護 V.2018-0322
 * 2018-0322:	JH		++id_code,id_code2
 *
 * */

import busi.FuncEdit;

public class Rskm3150Func extends FuncEdit {
String kk1 = "", kk2 = "";

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
public void dataCheck() {
}

@Override
public int dbInsert()  {
   msgOK();

   strSql = "insert into ptr_sys_idtab ("
         + " wf_type, "      //1
         + " wf_id, "
         + " wf_desc, "
         + " wf_useredit, "
         + " id_code, id_code2,"
         + " mod_time, mod_user, mod_pgm, mod_seqno"
         + " ) values ("
         + " ?,?,?,?"
         + ",?,?"
         + ",sysdate,?,?,1"
         + " )";
   Object[] param = new Object[]{
         wp.itemStr("A_wf_key"),      //1
         varsStr("wf_id"),
         varsStr("wf_desc"),
         "Y",
         varsStr("id_code"), varsStr("id_code2"),
         this.modUser,
         this.modPgm
   };

   this.sqlExec(strSql, param);
   if (sqlRowNum <= 0) {
      errmsg("Insert ptr_sys_idtab error; " + getMsg());
   }
   return rc;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete()  {
   msgOK();
   strSql = "Delete ptr_sys_idtab"
         + " where wf_type ='" + wp.itemStr("A_wf_key") + "'";
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("Delete ptr_sys_idtab err; " + getMsg());
      rc = -1;
   }

   return rc;
}

}
