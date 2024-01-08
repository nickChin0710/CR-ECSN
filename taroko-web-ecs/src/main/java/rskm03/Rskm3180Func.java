package rskm03;

import busi.FuncEdit;
import taroko.base.Parm2Sql;

public class Rskm3180Func extends FuncEdit {
Parm2Sql tt=new Parm2Sql();

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
   // TODO Auto-generated method stub

}

@Override
public int dbInsert()  {
   msgOK();

   strSql = "insert into rsk_ctfi_call_log ("
         + " card_no, "      //1
         + " call_date, "
         + " call_time, "
         + " tel_no,"
         + " call_man, "
         + " call_desc, "
         + " call_desc02, "
         + " call_desc03, "
         + " call_telno, "
         + " tel_no2, "
         + " mod_user, mod_time, mod_pgm, mod_seqno "
         + " ) values ("
         + " :card_no, "      //1
         + " :call_date, "
         + " :call_time, "
         + " :tel_no,"
         + " :call_man, "
         + " :call_desc, "
         + " :call_desc02, "
         + " :call_desc03, "
         + " :call_telno, "
         + " :tel_no2, "
         + " :mod_user, sysdate, :mod_pgm, 1 "
         + " )";
   item2ParmStr("card_no");
   item2ParmStr("call_date", "ex_call_date");
   item2ParmStr("call_time", "ex_call_time");
   item2ParmStr("tel_no", "ex_call_telno");
   item2ParmStr("call_man", "ex_call_man");
   item2ParmStr("call_desc", "ex_call_desc");
   item2ParmStr("call_desc02", "ex_call_desc2");
   item2ParmStr("call_desc03", "ex_call_desc3");
   item2ParmStr("call_telno", "ex_call_telno");
   item2ParmStr("tel_no2", "ex_tel_no2");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert rks_ctfi_call_log error; " + getMsg());
   }
   return rc;
}

@Override
public int dbUpdate()  {
   msgOK();

   tt.update("rsk_ctfi_call_log");
   tt.parmSet("call_man", varsStr("call_man"));
   tt.parmSet("call_telno", varsStr("call_telno"));
   tt.parmSet("tel_no2", varsStr("tel_no2"));
   tt.parmSet("call_desc", varsStr("call_desc"));
   tt.parmSet("call_desc02", varsStr("call_desc02"));
   tt.parmSet("call_desc03", varsStr("call_desc03"));

   tt.whereRowid(varsStr("rowid"));

   sqlExec(tt.getSql(),tt.getParms());

   if (sqlRowNum <= 0) {
      errmsg("update rks_ctfi_call_log error; " + getMsg());
   }

   return rc;
}

@Override
public int dbDelete()  {
   msgOK();
   strSql = "Delete rsk_ctfi_call_log"
         + " where 1=1" +commSqlStr.whereRowid
   ;

   setParm(1, varsStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("Delete rsk_ctfi_call_log err; " + getMsg());
      rc = -1;
   }
   else rc = 1;

   return rc;
}

}
