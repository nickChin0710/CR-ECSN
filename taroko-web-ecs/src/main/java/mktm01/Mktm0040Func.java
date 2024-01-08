/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/10/29  V1.00.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0040Func extends FuncEdit
{
 private final String PROGNAME = "紅利統計來源群組明細維護處理程式111-11-28  V1.00.01";
  String kk1;
  String controlTabName = "mkt_bonus_srcdtl";

 public Mktm0040Func(TarokoCommon wr)
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
      kk1 = wp.itemStr2("kk_tran_pgm");
     }
  else
     {
      kk1 = wp.itemStr2("tran_pgm");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where tran_pgm = ? "
             + " and   tran_src_code  =  'XXX' "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[交易來源程式] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("tran_pgm_desc"))
     {
      errmsg("交易來源程式說明: 不可空白");
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
          + " tran_pgm, "
          + " tran_code, "
          + " tran_pgm_desc, "
          + " tran_src_code, "
          + " stat_type, "
          + " sign_flag, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,"
          + "?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("tran_code"),
        wp.itemStr2("tran_pgm_desc"),
        "XXX",
        "X",
        "X",
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
         + "tran_code = ?, "
         + "tran_pgm_desc = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("tran_code"),
     wp.itemStr2("tran_pgm_desc"),
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemRowId("rowid")
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

}  // End of class
