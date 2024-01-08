/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/10/15  V1.00.01   Allen Ho      Initial                              *
* 112/02/09  V1.00.02   Zuwei Su      naming rule update                   *
*                                                                          *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0855Func extends busi.FuncProc
{
 private  String PROGNAME = "紅利利率轉換檔維護作業處理程式110/10/15 V1.00.01";
  String kk1,kk2;
  String approveTabName = "mkt_chanrec_parm";
  String controlTabName = "mkt_chanrec_parm_t";

 public Mktp0855Func(TarokoCommon wr)
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
          + " active_seq, "
          + " record_group_no, "
          + " record_date_sel, "
          + " pur_date_sel, "
          + " purchase_date_s, "
          + " purchase_date_e, "
          + " week_cond, "
          + " month_cond, "
          + " cap_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " purchase_type_sel, "
          + " per_amt_cond, "
          + " per_amt, "
          + " max_cnt_cond, "
          + " max_cnt, "
          + " perday_cnt_cond, "
          + " perday_cnt, "
          + " sum_amt_cond, "
          + " sum_amt, "
          + " sum_cnt_cond, "
          + " sum_cnt, "
          + " above_cond, "
          + " above_amt, "
          + " above_cnt, "
          + " b_feedback_limit, "
          + " f_feedback_limit, "
          + " s_feedback_limit, "
          + " l_feedback_limit, "
          + " b_feedback_cnt_limit, "
          + " f_feedback_cnt_limit, "
          + " s_feedback_cnt_limit, "
          + " threshold_sel, "
          + " purchase_amt_s1, "
          + " purchase_amt_e1, "
          + " active_type_1, "
          + " feedback_rate_1, "
          + " feedback_amt_1, "
          + " feedback_lmt_cnt_1, "
          + " feedback_lmt_amt_1, "
          + " purchase_amt_s2, "
          + " purchase_amt_e2, "
          + " active_type_2, "
          + " feedback_rate_2, "
          + " feedback_amt_2, "
          + " feedback_lmt_cnt_2, "
          + " feedback_lmt_amt_2, "
          + " purchase_amt_s3, "
          + " purchase_amt_e3, "
          + " active_type_3, "
          + " feedback_rate_3, "
          + " feedback_amt_3, "
          + " feedback_lmt_cnt_3, "
          + " feedback_lmt_amt_3, "
          + " purchase_amt_s4, "
          + " purchase_amt_e4, "
          + " active_type_4, "
          + " feedback_rate_4, "
          + " feedback_amt_4, "
          + " feedback_lmt_cnt_4, "
          + " feedback_lmt_amt_4, "
          + " purchase_amt_s5, "
          + " purchase_amt_e5, "
          + " active_type_5, "
          + " feedback_rate_5, "
          + " feedback_amt_5, "
          + " feedback_lmt_cnt_5, "
          + " feedback_lmt_amt_5, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
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
        colStr("active_seq"),
        colStr("record_group_no"),
        colStr("record_date_sel"),
        colStr("pur_date_sel"),
        colStr("purchase_date_s"),
        colStr("purchase_date_e"),
        colStr("week_cond"),
        colStr("month_cond"),
        colStr("cap_sel"),
        colStr("bl_cond"),
        colStr("ca_cond"),
        colStr("it_cond"),
        colStr("id_cond"),
        colStr("ao_cond"),
        colStr("ot_cond"),
        colStr("purchase_type_sel"),
        colStr("per_amt_cond"),
        colStr("per_amt"),
        colStr("max_cnt_cond"),
        colStr("max_cnt"),
        colStr("perday_cnt_cond"),
        colStr("perday_cnt"),
        colStr("sum_amt_cond"),
        colStr("sum_amt"),
        colStr("sum_cnt_cond"),
        colStr("sum_cnt"),
        colStr("above_cond"),
        colStr("above_amt"),
        colStr("above_cnt"),
        colStr("b_feedback_limit"),
        colStr("f_feedback_limit"),
        colStr("s_feedback_limit"),
        colStr("l_feedback_limit"),
        colStr("b_feedback_cnt_limit"),
        colStr("f_feedback_cnt_limit"),
        colStr("s_feedback_cnt_limit"),
        colStr("threshold_sel"),
        colStr("purchase_amt_s1"),
        colStr("purchase_amt_e1"),
        colStr("active_type_1"),
        colStr("feedback_rate_1"),
        colStr("feedback_amt_1"),
        colStr("feedback_lmt_cnt_1"),
        colStr("feedback_lmt_amt_1"),
        colStr("purchase_amt_s2"),
        colStr("purchase_amt_e2"),
        colStr("active_type_2"),
        colStr("feedback_rate_2"),
        colStr("feedback_amt_2"),
        colStr("feedback_lmt_cnt_2"),
        colStr("feedback_lmt_amt_2"),
        colStr("purchase_amt_s3"),
        colStr("purchase_amt_e3"),
        colStr("active_type_3"),
        colStr("feedback_rate_3"),
        colStr("feedback_amt_3"),
        colStr("feedback_lmt_cnt_3"),
        colStr("feedback_lmt_amt_3"),
        colStr("purchase_amt_s4"),
        colStr("purchase_amt_e4"),
        colStr("active_type_4"),
        colStr("feedback_rate_4"),
        colStr("feedback_amt_4"),
        colStr("feedback_lmt_cnt_4"),
        colStr("feedback_lmt_amt_4"),
        colStr("purchase_amt_s5"),
        colStr("purchase_amt_e5"),
        colStr("active_type_5"),
        colStr("feedback_rate_5"),
        colStr("feedback_amt_5"),
        colStr("feedback_lmt_cnt_5"),
        colStr("feedback_lmt_amt_5"),
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
          + " active_seq, "
          + " record_group_no, "
          + " record_date_sel, "
          + " pur_date_sel, "
          + " purchase_date_s, "
          + " purchase_date_e, "
          + " week_cond, "
          + " month_cond, "
          + " cap_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " purchase_type_sel, "
          + " per_amt_cond, "
          + " per_amt, "
          + " max_cnt_cond, "
          + " max_cnt, "
          + " perday_cnt_cond, "
          + " perday_cnt, "
          + " sum_amt_cond, "
          + " sum_amt, "
          + " sum_cnt_cond, "
          + " sum_cnt, "
          + " above_cond, "
          + " above_amt, "
          + " above_cnt, "
          + " b_feedback_limit, "
          + " f_feedback_limit, "
          + " s_feedback_limit, "
          + " l_feedback_limit, "
          + " b_feedback_cnt_limit, "
          + " f_feedback_cnt_limit, "
          + " s_feedback_cnt_limit, "
          + " threshold_sel, "
          + " purchase_amt_s1, "
          + " purchase_amt_e1, "
          + " active_type_1, "
          + " feedback_rate_1, "
          + " feedback_amt_1, "
          + " feedback_lmt_cnt_1, "
          + " feedback_lmt_amt_1, "
          + " purchase_amt_s2, "
          + " purchase_amt_e2, "
          + " active_type_2, "
          + " feedback_rate_2, "
          + " feedback_amt_2, "
          + " feedback_lmt_cnt_2, "
          + " feedback_lmt_amt_2, "
          + " purchase_amt_s3, "
          + " purchase_amt_e3, "
          + " active_type_3, "
          + " feedback_rate_3, "
          + " feedback_amt_3, "
          + " feedback_lmt_cnt_3, "
          + " feedback_lmt_amt_3, "
          + " purchase_amt_s4, "
          + " purchase_amt_e4, "
          + " active_type_4, "
          + " feedback_rate_4, "
          + " feedback_amt_4, "
          + " feedback_lmt_cnt_4, "
          + " feedback_lmt_amt_4, "
          + " purchase_amt_s5, "
          + " purchase_amt_e5, "
          + " active_type_5, "
          + " feedback_rate_5, "
          + " feedback_amt_5, "
          + " feedback_lmt_cnt_5, "
          + " feedback_lmt_amt_5, "
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
         + "record_group_no = ?, "
         + "record_date_sel = ?, "
         + "pur_date_sel = ?, "
         + "purchase_date_s = ?, "
         + "purchase_date_e = ?, "
         + "week_cond = ?, "
         + "month_cond = ?, "
         + "cap_sel = ?, "
         + "bl_cond = ?, "
         + "ca_cond = ?, "
         + "it_cond = ?, "
         + "id_cond = ?, "
         + "ao_cond = ?, "
         + "ot_cond = ?, "
         + "purchase_type_sel = ?, "
         + "per_amt_cond = ?, "
         + "per_amt = ?, "
         + "max_cnt_cond = ?, "
         + "max_cnt = ?, "
         + "perday_cnt_cond = ?, "
         + "perday_cnt = ?, "
         + "sum_amt_cond = ?, "
         + "sum_amt = ?, "
         + "sum_cnt_cond = ?, "
         + "sum_cnt = ?, "
         + "above_cond = ?, "
         + "above_amt = ?, "
         + "above_cnt = ?, "
         + "b_feedback_limit = ?, "
         + "f_feedback_limit = ?, "
         + "s_feedback_limit = ?, "
         + "l_feedback_limit = ?, "
         + "b_feedback_cnt_limit = ?, "
         + "f_feedback_cnt_limit = ?, "
         + "s_feedback_cnt_limit = ?, "
         + "threshold_sel = ?, "
         + "purchase_amt_s1 = ?, "
         + "purchase_amt_e1 = ?, "
         + "active_type_1 = ?, "
         + "feedback_rate_1 = ?, "
         + "feedback_amt_1 = ?, "
         + "feedback_lmt_cnt_1 = ?, "
         + "feedback_lmt_amt_1 = ?, "
         + "purchase_amt_s2 = ?, "
         + "purchase_amt_e2 = ?, "
         + "active_type_2 = ?, "
         + "feedback_rate_2 = ?, "
         + "feedback_amt_2 = ?, "
         + "feedback_lmt_cnt_2 = ?, "
         + "feedback_lmt_amt_2 = ?, "
         + "purchase_amt_s3 = ?, "
         + "purchase_amt_e3 = ?, "
         + "active_type_3 = ?, "
         + "feedback_rate_3 = ?, "
         + "feedback_amt_3 = ?, "
         + "feedback_lmt_cnt_3 = ?, "
         + "feedback_lmt_amt_3 = ?, "
         + "purchase_amt_s4 = ?, "
         + "purchase_amt_e4 = ?, "
         + "active_type_4 = ?, "
         + "feedback_rate_4 = ?, "
         + "feedback_amt_4 = ?, "
         + "feedback_lmt_cnt_4 = ?, "
         + "feedback_lmt_amt_4 = ?, "
         + "purchase_amt_s5 = ?, "
         + "purchase_amt_e5 = ?, "
         + "active_type_5 = ?, "
         + "feedback_rate_5 = ?, "
         + "feedback_amt_5 = ?, "
         + "feedback_lmt_cnt_5 = ?, "
         + "feedback_lmt_amt_5 = ?, "
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
         + "and   active_seq  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("record_group_no"),
     colStr("record_date_sel"),
     colStr("pur_date_sel"),
     colStr("purchase_date_s"),
     colStr("purchase_date_e"),
     colStr("week_cond"),
     colStr("month_cond"),
     colStr("cap_sel"),
     colStr("bl_cond"),
     colStr("ca_cond"),
     colStr("it_cond"),
     colStr("id_cond"),
     colStr("ao_cond"),
     colStr("ot_cond"),
     colStr("purchase_type_sel"),
     colStr("per_amt_cond"),
     colStr("per_amt"),
     colStr("max_cnt_cond"),
     colStr("max_cnt"),
     colStr("perday_cnt_cond"),
     colStr("perday_cnt"),
     colStr("sum_amt_cond"),
     colStr("sum_amt"),
     colStr("sum_cnt_cond"),
     colStr("sum_cnt"),
     colStr("above_cond"),
     colStr("above_amt"),
     colStr("above_cnt"),
     colStr("b_feedback_limit"),
     colStr("f_feedback_limit"),
     colStr("s_feedback_limit"),
     colStr("l_feedback_limit"),
     colStr("b_feedback_cnt_limit"),
     colStr("f_feedback_cnt_limit"),
     colStr("s_feedback_cnt_limit"),
     colStr("threshold_sel"),
     colStr("purchase_amt_s1"),
     colStr("purchase_amt_e1"),
     colStr("active_type_1"),
     colStr("feedback_rate_1"),
     colStr("feedback_amt_1"),
     colStr("feedback_lmt_cnt_1"),
     colStr("feedback_lmt_amt_1"),
     colStr("purchase_amt_s2"),
     colStr("purchase_amt_e2"),
     colStr("active_type_2"),
     colStr("feedback_rate_2"),
     colStr("feedback_amt_2"),
     colStr("feedback_lmt_cnt_2"),
     colStr("feedback_lmt_amt_2"),
     colStr("purchase_amt_s3"),
     colStr("purchase_amt_e3"),
     colStr("active_type_3"),
     colStr("feedback_rate_3"),
     colStr("feedback_amt_3"),
     colStr("feedback_lmt_cnt_3"),
     colStr("feedback_lmt_amt_3"),
     colStr("purchase_amt_s4"),
     colStr("purchase_amt_e4"),
     colStr("active_type_4"),
     colStr("feedback_rate_4"),
     colStr("feedback_amt_4"),
     colStr("feedback_lmt_cnt_4"),
     colStr("feedback_lmt_amt_4"),
     colStr("purchase_amt_s5"),
     colStr("purchase_amt_e5"),
     colStr("active_type_5"),
     colStr("feedback_rate_5"),
     colStr("feedback_amt_5"),
     colStr("feedback_lmt_cnt_5"),
     colStr("feedback_lmt_amt_5"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("active_code"),
     colStr("active_seq")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbUpdate_mkt_uploadfile_ctl_proc_flag(String trans_seqno) throws Exception
 {
  strSql= "update mkt_uploadfile_ctl set "
        + " proc_flag = 'Y', "
        + " proc_date = to_char(sysdate,'yyyymmdd') "
        + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     trans_seqno
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
         + "and active_seq = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"),
     colStr("active_seq")
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
         + "and table_name  =  'MKT_CHANREC_PARM' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")+colStr("active_seq"), 
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
         + "and table_name  =  'MKT_CHANREC_PARM' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")+colStr("active_seq"), 
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
         + "and table_name  =  'MKT_CHANREC_PARM' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")+colStr("active_seq"), 
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
