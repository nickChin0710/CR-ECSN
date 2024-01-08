/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/10/29  V1.00.08   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                          *
* 112-01-18  V1.00.02  Zuwei Su       新增增加重複核查                                                                         *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0030Func extends FuncEdit
{
 private final String PROGNAME = "交易來源群組代碼處理程式111-11-28  V1.00.01";
  String kk1,kk2;
  String controlTabName = "mkt_bonus_src";

 public Mktm0030Func(TarokoCommon wr)
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
      kk1 = wp.itemStr2("kk_tran_src_code");
      kk2 = wp.itemStr2("kk_stat_type");
     }
  else
     {
      kk1 = wp.itemStr2("tran_src_code");
      kk2 = wp.itemStr2("stat_type");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where tran_src_code = ? and stat_type = ? "
             ;
      Object[] param = new Object[] {kk1, kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
//          errmsg("[統計類別] 不可重複("+controlTabName+") ,請重新輸入!");
          errmsg("[交易來源群組] [統計類別] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("tran_src_desc"))
     {
      errmsg("交易來源群組說明: 不可空白");
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
          + " tran_src_code, "
          + " stat_type, "
          + " tran_src_desc, "
          + " stat_flag, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        kk2,
        wp.itemStr2("tran_src_desc"),
        wp.itemStr2("stat_flag"),
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
         + "tran_src_desc = ?, "
         + "stat_flag = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("tran_src_desc"),
     wp.itemStr2("stat_flag"),
     wp.loginUser,
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

  rc = dbDeleteD2();
  
  return rc;
 }
// ************************************************************************
 public int dbInsertI2()
 {
   msgOK();

  strSql = "insert into MKT_BONUS_SRCDTL ( "
          + "tran_src_code,"
          + "stat_type,"
          + "tran_pgm,"
          + "tran_code,"
          + "sign_flag,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_pgm "
          + ") values ("
          + "?,?,?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr2("tran_src_code"),
      wp.itemStr2("stat_type"),
      varsStr("tran_pgm"),
      varsStr("tran_code"),
      varsStr("sign_flag"),
      wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BONUS_SRCDTL_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2()
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr2("tran_src_code"),
      wp.itemStr2("stat_type")
     };
   if (sqlRowcount("MKT_BONUS_SRCDTL" 
                   , "where tran_src_code = ? "
                   + "and   stat_type = ? "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BONUS_SRCDTL "
          + "where tran_src_code = ?  "
          + "and   stat_type = ?  "
          ;
   sqlExec(strSql,param,false);


   return 1;

 }
// ************************************************************************

}  // End of class
