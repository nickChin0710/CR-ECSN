/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/11/16  V1.00.10   Allen Ho      Initial                              *
* 111-10-26  V1.00.03    Machao      sync from mega & updated for project coding standard                                                                          *
***************************************************************************/
package cycm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycm0050Func extends FuncEdit
{
 private final String PROGNAME = "利息折扣參數維護處理程式111-10-26  V1.00.03";
  String kk1;
  String controlTabName = "ptr_actgeneral_n";

 public Cycm0050Func(TarokoCommon wr)
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
  if (this.ibAdd)
     {
      kk1 = wp.itemStr2("kk_acct_type");
      if (empty(kk1))
         {
          errmsg("帳戶類別 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr2("acct_type");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where acct_type = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[帳戶類別] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  int checkInt = check_decnum(wp.itemStr2("rc_rate"),3,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("1. 格式超出範圍 : 整數[3]位 小數[2]位");
      if (checkInt==2) 
         errmsg("1. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("1. 非數值");
      return;
     }

  checkInt = check_decnum(wp.itemStr2("rc_rate_limit"),3,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("2. 格式超出範圍 : 整數[3]位 小數[2]位");
      if (checkInt==2) 
         errmsg("2. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("2. 非數值");
      return;
     }

  checkInt = check_decnum(wp.itemStr2("purch_bal_parm"),8,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("3. 格式超出範圍 : 整數[8]位 小數[2]位");
      if (checkInt==2) 
         errmsg("3. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("3. 非數值");
      return;
     }

  checkInt = check_decnum(wp.itemStr2("waive_penauty"),8,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("4. 格式超出範圍 : 整數[8]位 小數[2]位");
      if (checkInt==2) 
         errmsg("4. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("4. 非數值");
      return;
     }


  if (this.isAdd()) return;

 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName+ " ("
          + " acct_type, "
          + " rc_rate, "
          + " rc_rate_limit, "
          + " purch_bal_parm, "
          + " waive_penauty, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemNum("rc_rate"),
        wp.itemNum("rc_rate_limit"),
        wp.itemNum("purch_bal_parm"),
        wp.itemNum("waive_penauty"),
        wp.itemStr2("zz_apr_user"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

  return rc;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "rc_rate = ?, "
         + "rc_rate_limit = ?, "
         + "purch_bal_parm = ?, "
         + "waive_penauty = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemNum("rc_rate"),
     wp.itemNum("rc_rate_limit"),
     wp.itemNum("purch_bal_parm"),
     wp.itemNum("waive_penauty"),
     wp.loginUser,
     wp.itemStr2("zz_apr_user"),
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (sqlRowNum <= 0) 
     {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
     }

  return rc;
 }
// ************************************************************************
 public int check_decnum(String decStr,int colLength,int colScale)
 {
  if (decStr.length()==0) return(0);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
  decStr = decStr.replace("-","");
  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>colLength)||
      (parts.length==2&&
       (parts[0].length()>colLength||parts[1].length()>colScale)))
      return(1);
  return(0);
 }
// ************************************************************************
 public int dbInsertI2() throws Exception
 {
   msgOK();

  strSql = "insert into PTR_CURR_GENERAL ( "
          + "acct_type,"
          + "curr_code,"
          + "purch_bal_wave,"
          + "total_bal,"
          + "purch_bal_parm,"
          + "min_payment,"
          + "crt_date, "
          + "crt_user, "
          + "apr_date, "
          + "apr_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "?,?,?,?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr2("acct_type"),
      varsStr("curr_code"),
      varsStr("purch_bal_wave"),
      varsStr("total_bal"),
      varsStr("purch_bal_parm"),
      varsStr("min_payment"),
      wp.loginUser,
        wp.itemStr2("zz_apr_user"),
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 PTR_CURR_GENERAL_T 錯誤");
   else dbUpdateMainU2();

   return rc;
 }
// ************************************************************************
 public int dbUpdateMainU2() throws Exception
  {
   // TODO Auto-update main 

  strSql= "update ptr_actgeneral_n set "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where acct_type = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("zz_apr_user"),
     wp.loginUser,
     wp.modPgm(),
     wp.colStr("acct_type")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 ptr_actgeneral_n 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;

  }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr2("acct_type")
     };
   if (sqlRowcount("PTR_CURR_GENERAL" 
                   , "where acct_type = ? "
                    , param) <= 0)
       return 1;

   strSql = "delete PTR_CURR_GENERAL "
          + "where acct_type = ?  "
          ;
   sqlExec(strSql,param);


   return 1;

 }
// ************************************************************************

}  // End of class
