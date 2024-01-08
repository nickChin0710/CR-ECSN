/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/24  V1.00.01   Allen Ho      Initial                              *
* 111/12/02  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp02;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp3120Func extends busi.FuncProc
{
 private final String PROGNAME = "IBON商品資料維護作業處理程式111/12/02  V1.00.02";
  String kk1,kk2,kk3;
  String approveTabName = "ibn_prog_gift";
  String controlTabName = "ibn_prog_gift_t";

 public Mktp3120Func(TarokoCommon wr)
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
  strSql= " insert into  " + approveTabName + " ("
          + " prog_code, "
          + " gift_no, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_name, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " gift_typeno, "
          + " prd_price, "
          + " exchange_pnt, "
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
          + "?,?,?,?,?,?,?,?,?,?,"
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
        colStr("prog_code"),
        colStr("gift_no"),
        colStr("prog_s_date"),
        colStr("prog_e_date"),
        colStr("gift_name"),
        colStr("gift_s_date"),
        colStr("gift_e_date"),
        colStr("gift_typeno"),
        colStr("prd_price"),
        colStr("exchange_pnt"),
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
          + " prog_code, "
          + " gift_no, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_name, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " gift_typeno, "
          + " prd_price, "
          + " exchange_pnt, "
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
  strSql= "update " + approveTabName + " set "
         + "gift_name = ?, "
         + "gift_s_date = ?, "
         + "gift_e_date = ?, "
         + "gift_typeno = ?, "
         + "prd_price = ?, "
         + "exchange_pnt = ?, "
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
         + "and   prog_code  = ? "
         + "and   gift_no  = ? "
         + "and   prog_s_date  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("gift_name"),
     colStr("gift_s_date"),
     colStr("gift_e_date"),
     colStr("gift_typeno"),
     colStr("prd_price"),
     colStr("exchange_pnt"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("prog_code"),
     colStr("gift_no"),
     colStr("prog_s_date")
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
  strSql = "delete " + approveTabName + " " 
         + "where 1 = 1 "
         + "and prog_code = ? "
         + "and gift_no = ? "
         + "and prog_s_date = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("prog_code"),
     colStr("gift_no"),
     colStr("prog_s_date")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDelete() throws Exception
 {
  strSql = "delete " + controlTabName + " " 
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
