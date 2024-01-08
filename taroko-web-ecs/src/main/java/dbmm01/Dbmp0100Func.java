/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/11/03  V1.00.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard          
* 112-10-30  V1.00.02  Ryan    insert 增加 p_senqo                                                                      *
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import busi.ecs.DbmBonus;

import java.util.*;

import taroko.base.CommString;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmp0100Func extends busi.FuncProc
{
 private final String PROGNAME = "帳戶DEBT紅利明細檔線上覆核處理程式111-11-28  V1.00.01";
  String kk1;
  String approveTabName = "dbm_bonus_dtl";
  String controlTabName = "dbm_bonus_dtl_t";
  CommString comms = new CommString();

 public Dbmp0100Func(TarokoCommon wr)
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
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  strSql= " insert into  " + approveTabName+ " ("
          + " tran_seqno, "
          + " acct_type, "
          + " active_code, "
          + " active_name, "
          + " tran_code, "
          + " beg_tran_bp, "
          + " tax_flag, "
          + " effect_e_date, "
          + " mod_reason, "
          + " mod_desc, "
          + " mod_memo, "
          + " tran_date, "
          + " tran_time, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " tran_pgm, "
          + " end_tran_bp, "
          + " acct_date, "
          + " acct_month, "
          + " bonus_type, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),?,?,?,?,?,?,"
          + "?,?,"
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
        comms.lpad(colStr("tran_seqno"),10,"0"),
        colStr("acct_type"),
        colStr("active_code"),
        colStr("active_name"),
        colStr("tran_code"),
        colStr("beg_tran_bp"),
        colStr("tax_flag"),
        colStr("effect_e_date"),
        colStr("mod_reason"),
        colStr("mod_desc"),
        colStr("mod_memo"),
        colStr("id_p_seqno"),
        colStr("p_seqno"),
        colStr("tran_pgm"),
        colStr("end_tran_bp"),
        comr.getBusinDate(),
        comr.getBusinDate().substring(0,6),
        "BONU",
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

  DbmBonus comc = new DbmBonus();
  comc.setConn(wp);
  comc.bonusFunc(colStr("tran_seqno"));

  return rc;
 }
// ************************************************************************
 public int dbSelectS4() throws Exception
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " tran_seqno, "
          + " acct_type, "
          + " active_code, "
          + " active_name, "
          + " tran_code, "
          + " beg_tran_bp, "
          + " tax_flag, "
          + " effect_e_date, "
          + " mod_reason, "
          + " mod_desc, "
          + " mod_memo, "
          + " tran_date, "
          + " tran_time, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " tran_pgm, "
          + " end_tran_bp, "
          + " acct_date, "
          + " acct_month, "
          + " bonus_type, "
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
         + "acct_type = ?, "
         + "active_code = ?, "
         + "active_name = ?, "
         + "tran_code = ?, "
         + "beg_tran_bp = ?, "
         + "tax_flag = ?, "
         + "effect_e_date = ?, "
         + "mod_reason = ?, "
         + "mod_desc = ?, "
         + "mod_memo = ?, "
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
         + "and   tran_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("acct_type"),
     colStr("active_code"),
     colStr("active_name"),
     colStr("tran_code"),
     colStr("beg_tran_bp"),
     colStr("tax_flag"),
     colStr("effect_e_date"),
     colStr("mod_reason"),
     colStr("mod_desc"),
     colStr("mod_memo"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("tran_seqno")
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
         + "and tran_seqno = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("tran_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
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
