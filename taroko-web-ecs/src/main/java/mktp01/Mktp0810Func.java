/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/06/24  V1.00.04   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0810Func extends busi.FuncProc
{
 private final String PROGNAME = "IBON專案卡友可兌贈品資料覆核　處理程式111/12/01  V1.00.02";
  String kk1;
  String approveTabName = "ibn_prog_list";
  String controlTabName = "ibn_prog_list_t";

 public Mktp0810Func(TarokoCommon wr)
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
          + " txn_seqno, "
          + " group_type, "
          + " prog_code, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_no, "
          + " id_no, "
          + " card_no, "
          + " gift_cnt, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " data_type, "
          + " vd_flag, "
          + " proc_flag, "
          + " from_type, "
          + " up_flag, "
          + " create_date, "
          + " create_time, "
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
          + "?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,?,?,?,?,?,"
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
        colStr("txn_seqno"),
        colStr("group_type"),
        colStr("prog_code"),
        colStr("prog_s_date"),
        colStr("prog_e_date"),
        colStr("gift_no"),
        colStr("id_no"),
        colStr("card_no"),
        colStr("gift_cnt"),
        colStr("p_seqno"),
        colStr("id_p_seqno"),
        colStr("gift_s_date"),
        colStr("gift_e_date"),
        colStr("data_type"),
        colStr("vd_flag"),
        colStr("proc_flag"),
        colStr("from_type"),
        colStr("up_flag"),
        colStr("create_date"),
        colStr("create_time"),
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
  if (sqlRowNum <= 0) errmsg("新增資料 "+ controlTabName +" 失敗");

  return rc;
 }
// ************************************************************************
 public int dbSelectS4() throws Exception
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " txn_seqno, "
          + " group_type, "
          + " prog_code, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_no, "
          + " id_no, "
          + " card_no, "
          + " gift_cnt, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " data_type, "
          + " vd_flag, "
          + " proc_flag, "
          + " from_type, "
          + " up_flag, "
          + " create_date, "
          + " create_time, "
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
         + "group_type = ?, "
         + "prog_code = ?, "
         + "prog_s_date = ?, "
         + "prog_e_date = ?, "
         + "gift_no = ?, "
         + "id_no = ?, "
         + "card_no = ?, "
         + "gift_cnt = ?, "
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
         + "and   txn_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("group_type"),
     colStr("prog_code"),
     colStr("prog_s_date"),
     colStr("prog_e_date"),
     colStr("gift_no"),
     colStr("id_no"),
     colStr("card_no"),
     colStr("gift_cnt"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("txn_seqno")
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
  strSql = "delete " + approveTabName + " " 
         + "where 1 = 1 "
         + "and txn_seqno = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("txn_seqno")
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
