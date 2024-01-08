/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/11/26  V1.00.10   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0260Func extends busi.FuncProc
{
 private final String PROGNAME = "紅利贈品廠商維護作業處理程式111/12/01  V1.00.02";
  String kk1;
  String approveTabName = "mkt_vendor";
  String controlTabName = "mkt_vendor_t";

 public Mktp0260Func(TarokoCommon wr)
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
          + " vendor_no, "
          + " vendor_name, "
          + " disable_flag, "
          + " id_no, "
          + " name, "
          + " sub_cname, "
          + " out_days, "
          + " tel_no, "
          + " contact_id, "
          + " contact_tel, "
          + " area_code, "
          + " address1, "
          + " address2, "
          + " address3, "
          + " address4, "
          + " address5, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
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
        colStr("vendor_no"),
        colStr("vendor_name"),
        colStr("disable_flag"),
        colStr("id_no"),
        colStr("name"),
        colStr("sub_cname"),
        colStr("out_days"),
        colStr("tel_no"),
        colStr("contact_id"),
        colStr("contact_tel"),
        colStr("area_code"),
        colStr("address1"),
        colStr("address2"),
        colStr("address3"),
        colStr("address4"),
        colStr("address5"),
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
          + " vendor_no, "
          + " vendor_name, "
          + " disable_flag, "
          + " id_no, "
          + " name, "
          + " sub_cname, "
          + " out_days, "
          + " tel_no, "
          + " contact_id, "
          + " contact_tel, "
          + " area_code, "
          + " address1, "
          + " address2, "
          + " address3, "
          + " address4, "
          + " address5, "
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
         + "vendor_name = ?, "
         + "disable_flag = ?, "
         + "id_no = ?, "
         + "name = ?, "
         + "sub_cname = ?, "
         + "out_days = ?, "
         + "tel_no = ?, "
         + "contact_id = ?, "
         + "contact_tel = ?, "
         + "area_code = ?, "
         + "address1 = ?, "
         + "address2 = ?, "
         + "address3 = ?, "
         + "address4 = ?, "
         + "address5 = ?, "
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
         + "and   vendor_no  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("vendor_name"),
     colStr("disable_flag"),
     colStr("id_no"),
     colStr("name"),
     colStr("sub_cname"),
     colStr("out_days"),
     colStr("tel_no"),
     colStr("contact_id"),
     colStr("contact_tel"),
     colStr("area_code"),
     colStr("address1"),
     colStr("address2"),
     colStr("address3"),
     colStr("address4"),
     colStr("address5"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("vendor_no")
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
         + "and vendor_no = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("vendor_no")
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
