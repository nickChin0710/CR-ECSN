/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/11/03  V1.00.02   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                          *
* 111/12/22  V1.00.02   Zuwei         輸出sql log                                                                     *
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmp0170Func extends busi.FuncProc
{
 private final String PROGNAME = "VD紅利特惠(一)-特店刷卡參數覆核處理程式111-11-28  V1.00.01";
  String kk1;
  String approveTabName = "dbm_bpmh";
  String controlTabName = "dbm_bpmh_t";

 public Dbmp0170Func(TarokoCommon wr)
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
          + " active_code, "
          + " active_name, "
          + " give_code, "
          + " tax_flag, "
          + " feedback_sel, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " activate_s_date, "
          + " activate_e_date, "
          + " re_months, "
          + " in_bl_cond, "
          + " out_bl_cond, "
          + " out_ca_cond, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " pos_entry_sel, "
          + " bp_amt, "
          + " bp_pnt, "
          + " add_times, "
          + " add_point, "
          + " give_name, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,"
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
        colStr("active_code"),
        colStr("active_name"),
        colStr("give_code"),
        colStr("tax_flag"),
        colStr("feedback_sel"),
        colStr("purch_s_date"),
        colStr("purch_e_date"),
        colStr("activate_s_date"),
        colStr("activate_e_date"),
        colStr("re_months"),
        colStr("in_bl_cond"),
        colStr("out_bl_cond"),
        colStr("out_ca_cond"),
        colStr("acct_type_sel"),
        colStr("group_code_sel"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("platform_kind_sel"),
        colStr("mcc_code_sel"),
        colStr("pos_entry_sel"),
        colStr("bp_amt"),
        colStr("bp_pnt"),
        colStr("add_times"),
        colStr("add_point"),
        colStr("give_name"),
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
          + " active_code, "
          + " active_name, "
          + " give_code, "
          + " tax_flag, "
          + " feedback_sel, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " activate_s_date, "
          + " activate_e_date, "
          + " re_months, "
          + " in_bl_cond, "
          + " out_bl_cond, "
          + " out_ca_cond, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " pos_entry_sel, "
          + " bp_amt, "
          + " bp_pnt, "
          + " add_times, "
          + " add_point, "
          + " give_name, "
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
         + "active_name = ?, "
         + "give_code = ?, "
         + "tax_flag = ?, "
         + "feedback_sel = ?, "
         + "purch_s_date = ?, "
         + "purch_e_date = ?, "
         + "activate_s_date = ?, "
         + "activate_e_date = ?, "
         + "re_months = ?, "
         + "in_bl_cond = ?, "
         + "out_bl_cond = ?, "
         + "out_ca_cond = ?, "
         + "acct_type_sel = ?, "
         + "group_code_sel = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "platform_kind_sel = ?, "
         + "mcc_code_sel = ?, "
         + "pos_entry_sel = ?, "
         + "bp_amt = ?, "
         + "bp_pnt = ?, "
         + "add_times = ?, "
         + "add_point = ?, "
         + "give_name = ?, "
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
         + "and   active_code  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_name"),
     colStr("give_code"),
     colStr("tax_flag"),
     colStr("feedback_sel"),
     colStr("purch_s_date"),
     colStr("purch_e_date"),
     colStr("activate_s_date"),
     colStr("activate_e_date"),
     colStr("re_months"),
     colStr("in_bl_cond"),
     colStr("out_bl_cond"),
     colStr("out_ca_cond"),
     colStr("acct_type_sel"),
     colStr("group_code_sel"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("platform_kind_sel"),
     colStr("mcc_code_sel"),
     colStr("pos_entry_sel"),
     colStr("bp_amt"),
     colStr("bp_pnt"),
     colStr("add_times"),
     colStr("add_point"),
     colStr("give_name"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("active_code")
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
         + "and active_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")
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
  strSql = "delete dbm_bn_data " 
         + "where 1 = 1 "
         + "and table_name  =  'DBM_BPMH' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("ACTIVE_CODE"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 dbm_bn_data 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TBndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete dbm_bn_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'DBM_BPMH' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("ACTIVE_CODE"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 dbm_bn_data_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into dbm_bn_data "
         + "select * "
         + "from  dbm_bn_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'DBM_BPMH' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("ACTIVE_CODE"), 
    };

  sqlExec(strSql, param,true);

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
