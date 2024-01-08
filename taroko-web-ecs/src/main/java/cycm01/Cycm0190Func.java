/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/07  V1.00.01   Allen Ho      Initial                              *
* 111/10/28  V1.00.02  Yang Bo        sync code from mega                  *
***************************************************************************/
package cycm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycm0190Func extends FuncEdit
{
 private final String PROGNAME = "各項作業凍結碼參數維護處理程式110/07/07 V1.00.01";
  String controlTabName = "ptr_sys_idtab";

 public Cycm0190Func(TarokoCommon wr)
 {
  wp = wr;
  this.conn = wp.getConn();
 }
// ************************************************************************
 @Override
 public int querySelect()
 {
  // TODO Auto-generated method
  return 0;
 }
// ************************************************************************
 @Override
 public int dataSelect()
 {
  // TODO Auto-generated method stub
  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
  if (!this.ibAdd)
     {
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("wf_type"))
     {
      errmsg("資料類別 不可空白");
      return;
     }


  if (this.isAdd()) return;

 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  return 1 ;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  return 1 ;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  return 1;
 }
// ************************************************************************
 public int dbInsertI2() throws Exception
 {
   msgOK();

  strSql = "insert into PTR_SYS_IDTAB ( "
          + "wf_type, "
          + "wf_id,"
          + "wf_desc,"
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "?, "
          + "?,?," 
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr("wf_type"), 
      varsStr("wf_id"),
      varsStr("wf_desc"),
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 PTR_SYS_IDTAB_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
     };
   if (sqlRowcount("PTR_SYS_IDTAB" 
                    , "where wf_type = '"+wp.itemStr("wf_type")+"' "
                    , param) <= 0)
       return 1;

   strSql = "delete PTR_SYS_IDTAB "
          + "where wf_type = '"+ wp.itemStr("wf_type")+"' "
          ;
   sqlExec(strSql,param);


   return 1;

 }
// ************************************************************************

}  // End of class
