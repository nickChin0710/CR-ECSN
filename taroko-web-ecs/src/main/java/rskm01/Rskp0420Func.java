package rskm01;
/** 調單/扣款/雜項費用-待傳送資料製作處理
 * 2020-0421   JH    VM.國外不送
 */

import busi.FuncAction;

public class Rskp0420Func extends FuncAction {

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
   // TODO Auto-generated method stub
   return 0;
}

public int updateReceipt() throws Exception  {
   msgOK();
   strSql = " update rsk_receipt set "
         + " send_apr_flag = 'Y' ,"
         + " send_flag =:send_flag , "
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =:rowid ";
   var2ParmStr("send_flag");
   setRowId("rowid", varsStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0)
      errmsg("update rsk_receipt error ! ");

   return rc;
}

public int updateChgback() throws Exception  {
   msgOK();
   strSql = "update rsk_chgback set "
         + " send_apr_flag = 'Y' , "
         + " send_flag =:send_flag , "
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =:rowid ";
   var2ParmStr("send_flag");
   setRowId("rowid", varsStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0)
      errmsg("update rsk_chgback error ! ");
   return rc;
}

public int updateChgbackNoTW(String a_date1, String a_date2) throws Exception  {
   strSql = "update rsk_chgback set" +
         " send_flag ='0'" +
         ", send_apr_flag ='Y'" +
         " where send_apr_flag <>'Y' and send_flag='1'" +
         " and bill_type like 'NC%'" +
         " and bin_type in ('V','M') and mcht_country not in ('','TW','TWN')" +
         commSqlStr.strend(a_date1, a_date2, "fst_add_date")
   ;
   sqlExec(strSql);
   return 1;
}
//	public int updateMiscfee2(){
//[雜費取消]
//		msgOK();
//		strSql = " update rsk_miscfee2 set "
//				+ " send_apr_flag = 'Y' , "
//				+ " send_flag =:send_flag , "
//				+ " mod_user =:mod_user , "
//				+ " mod_time =sysdate , "
//				+ " mod_pgm =:mod_pgm , "
//				+ " mod_seqno =nvl(mod_seqno,0)+1 "
//				+ " where rowid =:rowid ";
//		var2Parm_ss("send_flag");
//		setString("mod_user",wp.loginUser);
//		setString("mod_pgm","rskp0420");
//		setRowId("rowid", vars_ss("rowid"));
//		sqlExec(strSql);
//		if(sqlRowNum<=0)
//			errmsg("update rsk_miscfee2 error ! ");
//		return rc;
//	}

}
