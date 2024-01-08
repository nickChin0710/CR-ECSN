/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/09/30  V1.00.01   Allen Ho      Initial                              *
* 111/12/07  V1.00.02  Machao    sync from mega & updated for project coding standard                                                                         *
* 111/12/22  V1.00.03   Zuwei         輸出sql log                                                                     *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0180Func extends busi.FuncProc
{
 private final String PROGNAME = "紅利特惠(二)新發卡/介紹人加贈點數參數檔維護處理程式111/12/07  V1.00.02";
  String kk1;
  String approveTabName = "mkt_bpnw";
  String controlTabName = "mkt_bpnw_t";

 public Mktp0180Func(TarokoCommon wr)
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
          + " bonus_type, "
          + " tax_flag, "
          + " active_date_s, "
          + " active_date_e, "
          + " effect_months, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " apply_date_type, "
          + " apply_date_s, "
          + " apply_date_e, "
          + " acct_type_sel, "
          + " group_card_sel, "
          + " platform_kind_sel, "
          + " applicant_cond, "
          + " new_card_cond, "
          + " major_cond, "
          + " major_point, "
          + " sub_cond, "
          + " sub_point, "
          + " app_purch_cond, "
          + " app_months, "
          + " add_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " add_times, "
          + " add_point, "
          + " purch_reclow_cond, "
          + " purch_reclow_amt, "
          + " purch_tol_amt_cond, "
          + " purch_tol_amt, "
          + " purch_tol_time_cond, "
          + " purch_tol_time, "
          + " feedback_lmt, "
          + " limit_1_beg, "
          + " limit_1_end, "
          + " exchange_1, "
          + " limit_2_beg, "
          + " limit_2_end, "
          + " exchange_2, "
          + " limit_3_beg, "
          + " limit_3_end, "
          + " exchange_3, "
          + " limit_4_beg, "
          + " limit_4_end, "
          + " exchange_4, "
          + " limit_5_beg, "
          + " limit_5_end, "
          + " exchange_5, "
          + " limit_6_beg, "
          + " limit_6_end, "
          + " exchange_6, "
          + " introducer_cond, "
//          + " new_card_cond1, "
          + " intro_point, "
          + " intro_purch_cond, "
          + " intro_months, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//          + "?,"
          + "'Y',"
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
        colStr("bonus_type"),
        colStr("tax_flag"),
        colStr("active_date_s"),
        colStr("active_date_e"),
        colStr("effect_months"),
        colStr("stop_flag"),
        colStr("stop_date"),
        colStr("stop_desc"),
        colStr("apply_date_type"),
        colStr("apply_date_s"),
        colStr("apply_date_e"),
        colStr("acct_type_sel"),
        colStr("group_card_sel"),
        colStr("platform_kind_sel"),
        colStr("applicant_cond"),
        colStr("new_card_cond"),
        colStr("major_cond"),
        colStr("major_point"),
        colStr("sub_cond"),
        colStr("sub_point"),
        colStr("app_purch_cond"),
        colStr("app_months"),
        colStr("add_months"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("add_times"),
        colStr("add_point"),
        colStr("purch_reclow_cond"),
        colStr("purch_reclow_amt"),
        colStr("purch_tol_amt_cond"),
        colStr("purch_tol_amt"),
        colStr("purch_tol_time_cond"),
        colStr("purch_tol_time"),
        colStr("feedback_lmt"),
        colStr("limit_1_beg"),
        colStr("limit_1_end"),
        colStr("exchange_1"),
        colStr("limit_2_beg"),
        colStr("limit_2_end"),
        colStr("exchange_2"),
        colStr("limit_3_beg"),
        colStr("limit_3_end"),
        colStr("exchange_3"),
        colStr("limit_4_beg"),
        colStr("limit_4_end"),
        colStr("exchange_4"),
        colStr("limit_5_beg"),
        colStr("limit_5_end"),
        colStr("exchange_5"),
        colStr("limit_6_beg"),
        colStr("limit_6_end"),
        colStr("exchange_6"),
        colStr("introducer_cond"),
//        colStr("new_card_cond1"),
        colStr("intro_point"),
        colStr("intro_purch_cond"),
        colStr("intro_months"),
        wp.loginUser,
        colStr("crt_date"),
        colStr("crt_user"),
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        colStr("mod_seqno"),  
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增資料 "+ controlTabName +" 失敗");

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
          + " bonus_type, "
          + " tax_flag, "
          + " active_date_s, "
          + " active_date_e, "
          + " effect_months, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " apply_date_type, "
          + " apply_date_s, "
          + " apply_date_e, "
          + " acct_type_sel, "
          + " group_card_sel, "
          + " platform_kind_sel, "
          + " applicant_cond, "
          + " new_card_cond, "
          + " major_cond, "
          + " major_point, "
          + " sub_cond, "
          + " sub_point, "
          + " app_purch_cond, "
          + " app_months, "
          + " add_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " add_times, "
          + " add_point, "
          + " purch_reclow_cond, "
          + " purch_reclow_amt, "
          + " purch_tol_amt_cond, "
          + " purch_tol_amt, "
          + " purch_tol_time_cond, "
          + " purch_tol_time, "
          + " feedback_lmt, "
          + " limit_1_beg, "
          + " limit_1_end, "
          + " exchange_1, "
          + " limit_2_beg, "
          + " limit_2_end, "
          + " exchange_2, "
          + " limit_3_beg, "
          + " limit_3_end, "
          + " exchange_3, "
          + " limit_4_beg, "
          + " limit_4_end, "
          + " exchange_4, "
          + " limit_5_beg, "
          + " limit_5_end, "
          + " exchange_5, "
          + " limit_6_beg, "
          + " limit_6_end, "
          + " exchange_6, "
          + " introducer_cond, "
//          + " new_card_cond1, "
          + " intro_point, "
          + " intro_purch_cond, "
          + " intro_months, "
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
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

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
         + "bonus_type = ?, "
         + "tax_flag = ?, "
         + "active_date_s = ?, "
         + "active_date_e = ?, "
         + "effect_months = ?, "
         + "stop_flag = ?, "
         + "stop_date = ?, "
         + "stop_desc = ?, "
         + "apply_date_type = ?, "
         + "apply_date_s = ?, "
         + "apply_date_e = ?, "
         + "acct_type_sel = ?, "
         + "group_card_sel = ?, "
         + "platform_kind_sel = ?, "
         + "applicant_cond = ?, "
         + "new_card_cond = ?, "
         + "major_cond = ?, "
         + "major_point = ?, "
         + "sub_cond = ?, "
         + "sub_point = ?, "
         + "app_purch_cond = ?, "
         + "app_months = ?, "
         + "add_months = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "add_times = ?, "
         + "add_point = ?, "
         + "purch_reclow_cond = ?, "
         + "purch_reclow_amt = ?, "
         + "purch_tol_amt_cond = ?, "
         + "purch_tol_amt = ?, "
         + "purch_tol_time_cond = ?, "
         + "purch_tol_time = ?, "
         + "feedback_lmt = ?, "
         + "limit_1_beg = ?, "
         + "limit_1_end = ?, "
         + "exchange_1 = ?, "
         + "limit_2_beg = ?, "
         + "limit_2_end = ?, "
         + "exchange_2 = ?, "
         + "limit_3_beg = ?, "
         + "limit_3_end = ?, "
         + "exchange_3 = ?, "
         + "limit_4_beg = ?, "
         + "limit_4_end = ?, "
         + "exchange_4 = ?, "
         + "limit_5_beg = ?, "
         + "limit_5_end = ?, "
         + "exchange_5 = ?, "
         + "limit_6_beg = ?, "
         + "limit_6_end = ?, "
         + "exchange_6 = ?, "
         + "introducer_cond = ?, "
//         + "new_card_cond1 = ?, "
         + "intro_point = ?, "
         + "intro_purch_cond = ?, "
         + "intro_months = ?, "
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
     colStr("bonus_type"),
     colStr("tax_flag"),
     colStr("active_date_s"),
     colStr("active_date_e"),
     colStr("effect_months"),
     colStr("stop_flag"),
     colStr("stop_date"),
     colStr("stop_desc"),
     colStr("apply_date_type"),
     colStr("apply_date_s"),
     colStr("apply_date_e"),
     colStr("acct_type_sel"),
     colStr("group_card_sel"),
     colStr("platform_kind_sel"),
     colStr("applicant_cond"),
     colStr("new_card_cond"),
     colStr("major_cond"),
     colStr("major_point"),
     colStr("sub_cond"),
     colStr("sub_point"),
     colStr("app_purch_cond"),
     colStr("app_months"),
     colStr("add_months"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("add_times"),
     colStr("add_point"),
     colStr("purch_reclow_cond"),
     colStr("purch_reclow_amt"),
     colStr("purch_tol_amt_cond"),
     colStr("purch_tol_amt"),
     colStr("purch_tol_time_cond"),
     colStr("purch_tol_time"),
     colStr("feedback_lmt"),
     colStr("limit_1_beg"),
     colStr("limit_1_end"),
     colStr("exchange_1"),
     colStr("limit_2_beg"),
     colStr("limit_2_end"),
     colStr("exchange_2"),
     colStr("limit_3_beg"),
     colStr("limit_3_end"),
     colStr("exchange_3"),
     colStr("limit_4_beg"),
     colStr("limit_4_end"),
     colStr("exchange_4"),
     colStr("limit_5_beg"),
     colStr("limit_5_end"),
     colStr("exchange_5"),
     colStr("limit_6_beg"),
     colStr("limit_6_end"),
     colStr("exchange_6"),
     colStr("introducer_cond"),
//     colStr("new_card_cond1"),
     colStr("intro_point"),
     colStr("intro_purch_cond"),
     colStr("intro_months"),
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
 public int dbUpdateMktUploadfileCtlProcFlag(String transSeqno) throws Exception
 {
  strSql= "update mkt_uploadfile_ctl set "
        + " proc_flag = 'Y', "
        + " proc_date = to_char(sysdate,'yyyymmdd') "
        + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     transSeqno
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

  return 1;
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
  strSql = "delete mkt_bn_data " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_BPNW' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
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
         + "and table_name  =  'MKT_BPNW' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
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
         + "and table_name  =  'MKT_BPNW' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"), 
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
