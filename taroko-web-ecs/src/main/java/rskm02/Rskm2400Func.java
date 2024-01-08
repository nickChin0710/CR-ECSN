package rskm02;
/**
 * 2023-0919   JH    ++dbDelete()
 * */
import busi.FuncAction;

public class Rskm2400Func extends FuncAction {

@Override
public void dataCheck() {
   // TODO Auto-generated method stub

}

@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "insert into rsk_review_block (acct_type,acno_p_seqno,id_p_seqno,block_code,print_flag,review_date,"
       +"apr_flag,apr_user,apr_date,crt_user,crt_date,crt_time,mod_user,mod_time,mod_pgm,mod_seqno) values ("
       +":acct_type,:acno_p_seqno,:id_p_seqno,:block_code,'N',:review_date,'N','','',:crt_user,to_char(sysdate,'yyyymmdd'),"
       +"to_char(sysdate,'hh24miss'),:mod_user,sysdate,:mod_pgm,1) "
   ;

   setString("acct_type", wp.itemStr("acct_type"));
   setString("acno_p_seqno", wp.itemStr("acno_p_seqno"));
   setString("id_p_seqno", wp.itemStr("id_p_seqno"));
   setString("block_code", wp.itemStr("block_code"));
   setString("review_date", wp.itemStr("review_date"));
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return -1;
   }

   return rc;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   String lsRowid = wp.itemStr("rowid_delete");
   if (empty(lsRowid)) return 0;

   String sql1 = "delete rsk_review_block"
       +" where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)"
       +" and apr_date ='' ";
   setParm(1, lsRowid);
   sqlExec(sql1);
   if (sqlRowNum <= 0) {
      return -1;
   }

   return 1;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

}
