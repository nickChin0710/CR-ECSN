/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/15  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
* 111/12/22  V1.00.02   Zuwei         輸出sql log         
* 112-08-31  V1.00.03  Machao      list_flag新增TCB_ID(生日禮)栏位                                                                      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp4070Func extends busi.FuncProc
{
 private final String PROGNAME = "專案現金回饋參數檔覆核處理程式111-11-30  V1.00.01";
  String kk1;
  String approveTabName = "mkt_loan_parm";
  String controlTabName = "mkt_loan_parm_t";

 public Mktp4070Func(TarokoCommon wr)
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
          + " fund_code, "
          + " fund_name, "
          + " effect_months, "
          + " stop_flag, "
          + " list_cond, "
          + " list_flag, "
          + " add_vouch_no, "
          + " rem_vouch_no, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " group_oppost_cond, "
          + " feedback_lmt, "
          + " res_flag, "
          + " exec_s_months, "
          + " res_total_cnt, "
          + " move_cond, "
          + " bil_mcht_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " issue_a_months, "
          + " mcode, "
          + " cancel_scope, "
          + " cancel_rate, "
          + " cancel_event, "
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
        colStr("fund_code"),
        colStr("fund_name"),
        colStr("effect_months"),
        colStr("stop_flag"),
        colStr("list_cond"),
        colStr("list_flag"),
        colStr("add_vouch_no"),
        colStr("rem_vouch_no"),
        colStr("acct_type_sel"),
        colStr("group_code_sel"),
        colStr("group_oppost_cond"),
        colStr("feedback_lmt"),
        colStr("res_flag"),
        colStr("exec_s_months"),
        colStr("res_total_cnt"),
        colStr("move_cond"),
        colStr("bil_mcht_cond"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("issue_a_months"),
        colStr("mcode"),
        colStr("cancel_scope"),
        colStr("cancel_rate"),
        colStr("cancel_event"),
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
          + " fund_code, "
          + " fund_name, "
          + " effect_months, "
          + " stop_flag, "
          + " list_cond, "
          + " list_flag, "
          + " add_vouch_no, "
          + " rem_vouch_no, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " group_oppost_cond, "
          + " feedback_lmt, "
          + " res_flag, "
          + " exec_s_months, "
          + " res_total_cnt, "
          + " move_cond, "
          + " bil_mcht_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " issue_a_months, "
          + " mcode, "
          + " cancel_scope, "
          + " cancel_rate, "
          + " cancel_event, "
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
         + "fund_name = ?, "
         + "effect_months = ?, "
         + "stop_flag = ?, "
         + "list_cond = ?, "
         + "list_flag = ?, "
         + "add_vouch_no = ?, "
         + "rem_vouch_no = ?, "
         + "acct_type_sel = ?, "
         + "group_code_sel = ?, "
         + "group_oppost_cond = ?, "
         + "feedback_lmt = ?, "
         + "res_flag = ?, "
         + "exec_s_months = ?, "
         + "res_total_cnt = ?, "
         + "move_cond = ?, "
         + "bil_mcht_cond = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "issue_a_months = ?, "
         + "mcode = ?, "
         + "cancel_scope = ?, "
         + "cancel_rate = ?, "
         + "cancel_event = ?, "
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
         + "and   fund_code  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_name"),
     colStr("effect_months"),
     colStr("stop_flag"),
     colStr("list_cond"),
     colStr("list_flag"),
     colStr("add_vouch_no"),
     colStr("rem_vouch_no"),
     colStr("acct_type_sel"),
     colStr("group_code_sel"),
     colStr("group_oppost_cond"),
     colStr("feedback_lmt"),
     colStr("res_flag"),
     colStr("exec_s_months"),
     colStr("res_total_cnt"),
     colStr("move_cond"),
     colStr("bil_mcht_cond"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("issue_a_months"),
     colStr("mcode"),
     colStr("cancel_scope"),
     colStr("cancel_rate"),
     colStr("cancel_event"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("fund_code")
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
         + "and fund_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code")
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
  strSql = "delete mkt_parm_data " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_LOAN_PARM' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_parm_data 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TBndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_parm_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_LOAN_PARM' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_parm_data_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into mkt_parm_data "
         + "select * "
         + "from  mkt_parm_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_LOAN_PARM' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code"), 
    };

  sqlExec(strSql, param,true);

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4Imloan() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_imloan_list " 
         + "where 1 = 1 "
         + "and fund_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code")
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_imloan_list 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TImloan() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_imloan_list_t " 
         + "where 1 = 1 "
         + "and fund_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code")
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_imloan_list_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Imloan() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into mkt_imloan_list "
         + "select * "
         + "from  mkt_imloan_list_t " 
         + "where 1 = 1 "
         + "and fund_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code")
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
