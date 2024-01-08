/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/29  V1.00.08   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0243Func extends busi.FuncProc
{
 private final String PROGNAME = "處理程式111/12/01  V1.00.02";
  String kk1;
  String approveTabName = "mkt_gift_stock";
  String controlTabName = "mkt_gift_stock_t";

 public Mktp0243Func(TarokoCommon wr)
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
   selectMktGift();

   colSet("p_supply_cnt", colStr("gift_supply_count"));
   colSet("p_use_cnt"   , colStr("gift_use_count"));
   colSet("p_web_cnt"   , colStr("gift_web_count"));
   colSet("p_net_cnt"   , String.format("%d", 
                           (int)colNum("gift_supply_count")
                         - (int)colNum("gift_use_count")
                         - (int)colNum("gift_web_count")));

   if (colStr("adjust_flag").equals("1"))
      {
       colSet("a_supply_cnt",  String.format("%d",
                                (int)colNum("gift_supply_count")
                             +  (int)colNum("adjust_count")));
       colSet("a_use_cnt"   ,  colStr("gift_use_count"));
       colSet("a_web_cnt"   ,  String.format("%d",
                                (int)colNum("gift_web_count")
                             +  (int)colNum("web_count")));
       colSet("a_net_cnt"   ,  String.format("%d",
                                (int)colNum("a_supply_cnt") 
                             -  (int)colNum("a_use_cnt")
                             -  (int)colNum("a_web_cnt")));
      }
   else
      {
       colSet("a_supply_cnt",  String.format("%d",
                                (int)colNum("adjust_count")));
       colSet("a_use_cnt"   ,  "0");
       colSet("a_web_cnt"   ,  String.format("%d",
                                (int)colNum("web_count")));
       colSet("a_net_cnt"   ,  String.format("%d",
                                (int)colNum("adjust_count")
                             -  (int)colNum("web_count")));
      }

  strSql= " insert into  " + approveTabName + " ("
          + " gift_no, "
          + " adjust_flag, "
          + " adjust_count, "
          + " web_count, "
          + " stock_desc, "
          + " stock_comment, "
          + " a_supply_cnt, "
          + " a_use_cnt, "
          + " a_web_cnt, "
          + " p_supply_cnt, "
          + " p_use_cnt, "
          + " p_web_cnt, "
          + " m_supply_cnt, "
          + " m_use_cnt, "
          + " m_web_cnt, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,"
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
        colStr("gift_no"),
        colStr("adjust_flag"),
        colStr("adjust_count"),
        colStr("web_count"),
        colStr("stock_desc"),
        colStr("stock_comment"),
        colStr("a_supply_cnt"),
        colStr("a_use_cnt"),
        colStr("a_web_cnt"),
        colStr("p_supply_cnt"),
        colStr("p_use_cnt"),
        colStr("p_web_cnt"),
        colStr("m_supply_cnt"),
        colStr("m_use_cnt"),
        colStr("m_web_cnt"),
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
   if (selectMktGift()!=1) return(0);


   if (colStr("adjust_flag").equals("1"))
      {
       colSet("a_supply_cnt",  String.format("%d",
                                   (int)colNum("gift_supply_count")
                                +  (int)colNum("adjust_count")));
       colSet("a_web_cnt"   ,  String.format("%d",
                                (int)colNum("gift_web_count")
                             +  (int)colNum("web_count")));
      colSet("a_use_cnt"   ,  colStr("gift_use_count"));
     }
   else
     {
       colSet("a_supply_cnt",  String.format("%d",
                             +  (int)colNum("adjust_count")));
       colSet("a_web_cnt"   ,  String.format("%d",
                                (int)colNum("web_count")));
      colSet("a_use_cnt"    ,  "0");
     }

   if (colStr("gift_gift_type").equals("1"))
      {
       if (updateMktGift1()!=1) return(0);
      }
   else
      {
      if (updateMktGift3()!=1) return(0);
      }

  return rc;
 }
// ************************************************************************
 public int dbSelectS4() throws Exception
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " gift_no, "
          + " adjust_flag, "
          + " adjust_count, "
          + " web_count, "
          + " stock_desc, "
          + " stock_comment, "
          + " a_supply_cnt, "
          + " a_use_cnt, "
          + " a_web_cnt, "
          + " p_supply_cnt, "
          + " p_use_cnt, "
          + " p_web_cnt, "
          + " m_supply_cnt, "
          + " m_use_cnt, "
          + " m_web_cnt, "
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
         + "adjust_flag = ?, "
         + "adjust_count = ?, "
         + "web_count = ?, "
         + "stock_desc = ?, "
         + "stock_comment = ?, "
         + "a_supply_cnt = ?, "
         + "a_use_cnt = ?, "
         + "a_web_cnt = ?, "
         + "a_supply_cnt-a_use_cnt-a_web_cnt = ?, "
         + "p_supply_cnt = ?, "
         + "p_use_cnt = ?, "
         + "p_web_cnt = ?, "
         + "p_supply_cnt-p_use_cnt-p_web_cnt = ?, "
         + "m_supply_cnt = ?, "
         + "m_use_cnt = ?, "
         + "m_web_cnt = ?, "
         + "m_supply_cnt-m_use_cnt-m_web_cnt = ?, "
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
         + "and   gift_no  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("adjust_flag"),
     colStr("adjust_count"),
     colStr("web_count"),
     colStr("stock_desc"),
     colStr("stock_comment"),
     colStr("a_supply_cnt"),
     colStr("a_use_cnt"),
     colStr("a_web_cnt"),
     colStr("a_supply_cnt-a_use_cnt-a_web_cnt"),
     colStr("p_supply_cnt"),
     colStr("p_use_cnt"),
     colStr("p_web_cnt"),
     colStr("p_supply_cnt-p_use_cnt-p_web_cnt"),
     colStr("m_supply_cnt"),
     colStr("m_use_cnt"),
     colStr("m_web_cnt"),
     colStr("m_supply_cnt-m_use_cnt-m_web_cnt"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("gift_no")
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
         + "and gift_no = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("gift_no")
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
// ************************************************************************
 int selectMktGift()
 {
   strSql = " select "
        + "gift_type as gift_gift_type,"
        + "decode(gift_type,'3',max_limit_count,SUPPLY_COUNT) as gift_supply_count,"
        + "decode(gift_type,'3',use_limit_count,USE_COUNT) as gift_use_count,"
        + "web_sumcnt as gift_web_count, "
        + "mod_seqno as gift_mod_seqno"
        + " from mkt_gift "
        + " where gift_no = ? ";

  Object[] param =new Object[]
       {
        colStr("gift_no")
       };

  sqlSelect(strSql, param);

  if (sqlRowNum <= 0) return(0);

  return 1;
 }
// ************************************************************************
 int updateMktGift3()
 {
  strSql= "update mkt_gift "
         + "set   max_limit_count = ?,  "
         + "      use_limit_count = ?,  "
         + "      web_sumcnt      = ?,  "
         + "      mod_user  = ?, "
         + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "      mod_pgm   = ?, "
         + "      mod_seqno = nvl(mod_seqno,0)+1 "
         + "where gift_no    = ? "
         + "and   mod_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     colNum("a_supply_cnt"),
     colNum("a_use_cnt"),
     colNum("a_web_cnt"),
     wp.loginUser,
     wp.sysDate+wp.sysTime,
     wp.modPgm(),
     colStr("gift_no"),
     colNum("gift_mod_seqno")
    };

  rc = sqlExec(strSql, param);

  return rc;
 }
// ************************************************************************
 int updateMktGift1()
 {
  strSql= "update mkt_gift "
         + "set   supply_count    = ?,  "
         + "      use_count       = ?,  "
         + "      web_sumcnt      = ?,  "
         + "      mod_user  = ?, "
         + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "      mod_pgm   = ?, "
         + "      mod_seqno = nvl(mod_seqno,0)+1 "
         + "where gift_no    = ? "
         + "and   mod_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     colNum("a_supply_cnt"),
     colNum("a_use_cnt"),
     colNum("a_web_cnt"),
     wp.loginUser,
     wp.sysDate+wp.sysTime,
     wp.modPgm(),
     colStr("gift_no"),
     colNum("gift_mod_seqno")
    };

  rc = sqlExec(strSql, param);

  return rc;
 }
// ************************************************************************

// ************************************************************************

}  // End of class
