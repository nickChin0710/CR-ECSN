/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/10/30  V1.00.01   Allen Ho      Initial                              *
* 112-02-18  V1.00.02  Machao     sync from mega & updated for project coding standard                        *                                                                                                     *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0660Func extends busi.FuncProc
{
 private final String PROGNAME = "WEB登錄代碼群組檔覆核處理程式112-02-18  V1.00.02";
  String kk1;
  String approveTabName = "web_record_group";
  String controlTabName = "web_record_group_t";

 public Mktp0660Func(TarokoCommon wr)
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
 }
// ************************************************************************
 @Override
 public int dataProc()
 {
  return rc;
 }
// ************************************************************************
 public int dbInsertA4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql= " insert into  " + approveTabName+ " ("
          + " record_group_no, "
          + " record_group_name, "
          + " active_date_s, "
          + " active_date_e, "
          + " voice_record_sel, "
          + " web_record_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " record_cnt_cond, "
          + " record_cnt, "
          + " record_id_cond, "
          + " purchase_cond, "
          + " sup_cond, "
          + " apr_flag, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + "?,"
          + "?,"
          + " ?) ";

  Object[] param =new Object[]
       {
        colStr("record_group_no"),
        colStr("record_group_name"),
        colStr("active_date_s"),
        colStr("active_date_e"),
        colStr("voice_record_sel"),
        colStr("web_record_sel"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("record_cnt_cond"),
        colStr("record_cnt"),
        colStr("record_id_cond"),
        colStr("purchase_cond"),
        colStr("sup_cond"),
        "Y",
        wp.loginUser,
        colStr("crt_date"),
        colStr("crt_user"),
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        colStr("mod_seqno"),  
        wp.modPgm()
       };

  sqlExec(strSql, param);

  return rc;
 }
// ************************************************************************
 public int dbSelectS4() throws Exception
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " record_group_no, "
          + " record_group_name, "
          + " active_date_s, "
          + " active_date_e, "
          + " voice_record_sel, "
          + " web_record_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " record_cnt_cond, "
          + " record_cnt, "
          + " record_id_cond, "
          + " purchase_cond, "
          + " sup_cond, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("wprowid")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return rc;
 }
// ************************************************************************
 public int dbUpdateU4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  String aprFlag = "Y";
  strSql= "update " +approveTabName + " set "
         + "record_group_name = ?, "
         + "active_date_s = ?, "
         + "active_date_e = ?, "
         + "voice_record_sel = ?, "
         + "web_record_sel = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "record_cnt_cond = ?, "
         + "record_cnt = ?, "
         + "record_id_cond = ?, "
         + "purchase_cond = ?, "
         + "sup_cond = ?, "
         + "crt_user  = ?, "
         + "crt_date  = ?, "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_flag  = ?, "
         + "mod_user  = ?, "
         + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "mod_pgm   = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1 "
         + "where 1     = 1 " 
         + "and   record_group_no  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("record_group_name"),
     colStr("active_date_s"),
     colStr("active_date_e"),
     colStr("voice_record_sel"),
     colStr("web_record_sel"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("record_cnt_cond"),
     colStr("record_cnt"),
     colStr("record_id_cond"),
     colStr("purchase_cond"),
     colStr("sup_cond"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("record_group_no")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete " +approveTabName + " " 
         + "where 1 = 1 "
         + "and record_group_no = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("record_group_no")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDeleteD4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_bn_data " 
         + "where 1 = 1 "
         + "and table_name  =  'WEB_RECORD_GROUP' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("record_group_no"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, false);
  if (rc!=1) errmsg("刪除 mkt_bn_data 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TBndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_bn_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'WEB_RECORD_GROUP' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("record_group_no"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, false);
  if (rc!=1) errmsg("刪除 mkt_bn_data_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into mkt_bn_data "
         + "select * "
         + "from  mkt_bn_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'WEB_RECORD_GROUP' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("record_group_no"), 
    };

  sqlExec(strSql, param,false);

  return 1;
 }
// ************************************************************************
 public int dbDelete() throws Exception
 {
  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("wprowid")
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
