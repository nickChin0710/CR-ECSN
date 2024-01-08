/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/02  V1.00.02   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                            *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4075Func extends FuncEdit
{
 private final String PROGNAME = "專案基金匯入外部單位明細檔處理程式111-11-28  V1.00.01";
  String kk1;
  String controlTabName = "mkt_extern_unit";

 public Mktm4075Func(TarokoCommon wr)
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
      kk1 = wp.itemStr2("kk_extern_id");
     }
  else
     {
      kk1 = wp.itemStr2("extern_id");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where extern_id = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[外部單位代碼] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if ((this.ibAdd)||(this.ibUpdate))
     {
/*
      is_sql = "select data_code "
             + "from mkt_bn_data "
             + "where table_name = 'MKT_EXTERN_UNIT' "
             + "and   data_key   = ?   "
             + "and   data_type  = '1' "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(is_sql,param);
*/
      if (kk1.length()==0)
         {
          errmsg("[基金代碼選擇必須輸入!");

         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("extern_name"))
     {
      errmsg("外部單位名稱: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("disable_flag"))
     {
      errmsg("停用註記: 不可空白");
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
          + " extern_id, "
          + " extern_name, "
          + " disable_flag, "
          + " in_ref_ip_code, "
          + " out_ref_ip_code, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("extern_name"),
        wp.itemStr2("disable_flag"),
        wp.itemStr2("in_ref_ip_code"),
        wp.itemStr2("out_ref_ip_code"),
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
         + "extern_name = ?, "
         + "disable_flag = ?, "
         + "in_ref_ip_code = ?, "
         + "out_ref_ip_code = ?, "
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
     wp.itemStr2("extern_name"),
     wp.itemStr2("disable_flag"),
     wp.itemStr2("in_ref_ip_code"),
     wp.itemStr2("out_ref_ip_code"),
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

  dbDeleteAddonProc();

  return rc;
 }
// ************************************************************************
 public int dbInsertI2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm4075_fund"))
      dataType = "1" ;
  strSql = "insert into MKT_BN_DATA ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "data_code2,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'MKT_EXTERN_UNIT', "
          + "?, "
          + "?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr2("extern_id"),
      varsStr("data_code"),
      varsStr("data_code2"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm4075_fund"))
      dataType = "1" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("extern_id")
     };
   if (sqlRowcount("MKT_BN_DATA" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_EXTERN_UNIT' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_DATA "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_EXTERN_UNIT'  "
          ;
   sqlExec(strSql,param,false);


   return 1;

 }
// ************************************************************************
 public int dbDeleteAddonProc() 
 {
  String dataType="1";
  //如果沒有資料回傳成功2
  Object[] param = new Object[]
    {
     dataType,
     wp.itemStr2("extern_id")
    };
  if (sqlRowcount("MKT_BN_DATA"
                   , "where data_type = ? "
                  + "and   data_key = ? "
                   + "and   table_name = 'MKT_EXTERN_UNIT' "
                   , param) <= 0)
      return 1;

  strSql = "delete MKT_BN_DATA "
         + "where data_type = ? "
         + "and   data_key = ?  "
         + "and   table_name = 'MKT_EXTERN_UNIT'  "
         ;
  sqlExec(strSql,param,false);

  return 1;
 }

// ************************************************************************

}  // End of class
