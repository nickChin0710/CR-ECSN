/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/09/29  V1.00.01   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import busi.DataSet;
import busi.ecs.CommFunction;
import busi.ecs.CommRoutine;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0258Func extends busi.FuncProc
{
 private final String PROGNAME = "紅利贈品廠商維護作業處理程式111/12/01  V1.00.02";
  String kk1;
  String approveTabName = "mkt_gift_smsresend";
  String controlTabName = "mkt_gift_smsresend_t";

 public Mktp0258Func(TarokoCommon wr)
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
 public int dbInsertA4()
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql= " insert into  " + approveTabName + " ("
          + " tran_seqno, "
          + " ecoupon_bno, "
          + " acct_type, "
          + " chi_name, "
          + " from_mark, "
          + " tran_date, "
          + " card_no, "
          + " exchg_cnt, "
          + " gift_no, "
          + " sms_date, "
          + " cellar_phone, "
          + " sms_resend_desc, "
          + " new_chi_name, "
          + " new_cellar_phone, "
          + " new_sms_resend_desc, "
          + " create_date, "
          + " create_time, "
          + " id_p_seqno, "
          + " gift_group, "
          + " new_sms_date, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,"
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
        colStr("tran_seqno"),
        colStr("ecoupon_bno"),
        colStr("acct_type"),
        colStr("chi_name"),
        colStr("from_mark"),
        colStr("tran_date"),
        colStr("card_no"),
        colStr("exchg_cnt"),
        colStr("gift_no"),
        colStr("sms_date"),
        colStr("cellar_phone"),
        colStr("sms_resend_desc"),
        colStr("new_chi_name"),
        colStr("new_cellar_phone"),
        colStr("new_sms_resend_desc"),
        colStr("create_date"),
        colStr("create_time"),
        colStr("id_p_seqno"),
        colStr("gift_group"),
        wp.sysDate,
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
   if (updateMktGiftBpexchgT()!=1) return(0);
   if (updateMktGiftBpexchg()!=1) return(0);
   if (selectMktGiftEcoupon()!=1) return(0);
   rc =1;


  return rc;
 }
// ************************************************************************
 public int dbSelectS4()
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " tran_seqno, "
          + " ecoupon_bno, "
          + " acct_type, "
          + " chi_name, "
          + " from_mark, "
          + " tran_date, "
          + " card_no, "
          + " exchg_cnt, "
          + " gift_no, "
          + " sms_date, "
          + " cellar_phone, "
          + " sms_resend_desc, "
          + " new_chi_name, "
          + " new_cellar_phone, "
          + " new_sms_resend_desc, "
          + " create_date, "
          + " create_time, "
          + " id_p_seqno, "
          + " gift_group, "
          + " new_sms_date, "
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
 public int dbUpdateU4()
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  String aprFlag = "Y";
  strSql= "update " + approveTabName + " set "
         + "new_chi_name = ?, "
         + "new_cellar_phone = ?, "
         + "new_sms_resend_desc = ?, "
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
         ;

  Object[] param =new Object[]
    {
     colStr("new_chi_name"),
     colStr("new_cellar_phone"),
     colStr("new_sms_resend_desc"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),

    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD4()
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete " + approveTabName + " " 
         + "where 1 = 1 "
         ;

  Object[] param =new Object[]
    {

    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDelete()
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
 int updateMktGiftBpexchg()
 {
  strSql= "update mkt_gift_bpexchg "
         + "set   sms_date        = to_char(sysdate,'yyyymmdd'),  "
         + "      chi_name        = ?,  "
         + "      cellar_phone    = ?,  "
         + "      sms_resend_desc = ?,  "
         + "      mod_user  = ?, "
         + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "      mod_pgm   = ?, "
         + "      mod_seqno = nvl(mod_seqno,0)+1 "
         + "where tran_seqno    = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("new_chi_name"),
     colStr("new_cellar_phone"),
     colStr("new_sms_resend_desc"),
     wp.loginUser,
     wp.sysDate+wp.sysTime,
     wp.modPgm(),
     colStr("tran_seqno"),
    };

  sqlExec(strSql, param);

  rc = sqlRowNum;
  return rc;
 }
// ************************************************************************
 int updateMktGiftBpexchgT()
 {
  strSql= "update mkt_gift_bpexchg_t "
         + "set   sms_date        = to_char(sysdate,'yyyymmdd'),  "
         + "      chi_name        = ?,  "
         + "      cellar_phone    = ?,  "
         + "      sms_resend_desc = ?  "
         + "where tran_seqno    = ? "
         ;
  
  Object[] param =new Object[]
    {
     colStr("new_chi_name"),
     colStr("new_cellar_phone"),
     colStr("new_sms_resend_desc"),
     colStr("tran_seqno"),
    };
  
  rc = sqlExec(strSql, param);
  
  return 1;
 }
// ************************************************************************
 int selectMktGiftEcoupon()
 {
  String sqlStr="";
  String msgSeqno="";

  selectSmsMsgId();
  selectMktGift();

  CommRoutine comr = new CommRoutine();
  comr.setConn(wp);

  sqlStr = " select "
         + " sms_send_date, "
         + " msg_seqno,"
         + " expire_date,"
         + " http_url,"
         + " auth_code "
         + " from mkt_gift_ecoupon "
         + " where tran_seqno ='" + colStr("tran_seqno") +"' "    
         ;

  DataSet ds1 =new DataSet();
  ds1.colList = this.sqlQuery(sqlStr,new Object[]{});

  int rowNum= sqlRowNum;

  for ( int inti=0; inti<rowNum; inti++ )
    {
     ds1.listFetch(inti);

     msgSeqno = comr.getSeqno("ECS_MODSEQ");

     insertSmsMsgDtl( ds1.colStr("expire_date"),
                        ds1.colStr("http_url"),
                        ds1.colStr("auth_code"),
                        msgSeqno);

     updateMktGiftEcoupon(ds1.colStr("http_url"),
                             ds1.colStr("auth_code"),
                             msgSeqno);

    }
  return(1);
 }
// ************************************************************************
 int selectSmsMsgId()
 {
  strSql= " select "
          + " msg_id, "
          + " msg_pgm, "
          + " msg_dept, "
          + " msg_userid "
          + " from sms_msg_id "
          + " where msg_pgm  = ? "
          + " and   msg_send_flag ='Y' "
          ;

  Object[] param =new Object[]
       {
        "MktF020"
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) return 0;

  return 1;
 }
// ************************************************************************
 int selectMktGift()
 {
  strSql= " select "
          + " gift_name "
          + " from mkt_gift "
          + " where gift_no  = ? "
          ;

  Object[] param =new Object[]
       {
        colStr("gift_no")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) return 0;

  return 1;
 }
// ************************************************************************
 int insertSmsMsgDtl(String expireDate, String httpUrl, String authCode, String msgSeqno)
 {
  CommFunction comm = new CommFunction();

  String msgDesc="";
  msgDesc = colStr("msg_userid") + ","
           + colStr("msg_id") + ","
           + colStr("new_cellar_phone") + ","
           + colStr("new_chi_name") + ","
           + colStr("gift_name") + ","
           + expireDate.substring(0,4)+"."
           + expireDate.substring(4,6)+"."
           + expireDate.substring(6,8) + ","
           + authCode + ","
           + httpUrl 
           ;

  strSql= " insert into  sms_msg_dtl ("
          + " msg_seqno, "
          + " cellar_phone, "
          + " chi_name, "
          + " msg_dept, "
          + " msg_userid, "
          + " msg_pgm, "
          + " msg_id, "
          + " msg_desc, "
          + " id_p_seqno,"
          + " p_seqno,"
          + " acct_type,"
          + " card_no,"
          + " cellphone_check_flag,"
          + " add_mode,"
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
          + "?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + "?,"
          + "?,"
          + " ?) ";

  Object[] param =new Object[]
       {
        msgSeqno,
        colStr("new_cellar_phone"),
        colStr("new_chi_name"),
        colStr("msg_dept"),
        colStr("msg_userid"),
        colStr("msg_pgm"),
        colStr("msg_id"),
        msgDesc,
        colStr("id_p_seqno"),
        colStr("p_seqno"),
        colStr("acct_type"),
        colStr("card_no"),
        "Y",
        "B",
        "Y",
        wp.loginUser,
        wp.loginUser,
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        colStr("mod_seqno"),
        wp.modPgm()
       };

  sqlExec(strSql, param);

  return rc;
 }
// ************************************************************************
 int updateMktGiftEcoupon(String httpUrl, String authCode, String msgSeqno)
 {
  strSql= "update mkt_gift_ecoupon "
         + "set   sms_send_date   = to_char(sysdate,'yyyymmdd'),  "
         + "      msg_seqno     = ?  "
         + "where tran_seqno    = ? "
         + "and   http_url      = ? "
         + "and   auth_code     = ? "
         ;

  Object[] param =new Object[]
    {
     msgSeqno,
     colStr("tran_seqno"),
     httpUrl,
     authCode,
    };

  rc = sqlExec(strSql, param);

  return 1;
 }
// ************************************************************************

}  // End of class
