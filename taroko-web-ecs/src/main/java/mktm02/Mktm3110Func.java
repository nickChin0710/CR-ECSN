/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/02/05  V1.00.04   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm3110Func extends FuncEdit
{
 private final String PROGNAME = "IBON專案基本資料維護作業處理程式111-11-30  V1.00.01";
  String kk1,kk2;
  String controlTabName = "ibn_prog";

 public Mktm3110Func(TarokoCommon wr)
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
  if (this.ibAdd)
     {
      kk1 = wp.itemStr2("kk_prog_code");
      kk2 = wp.itemStr2("kk_prog_s_date").replace("/","");
     }
  else
     {
      kk1 = wp.itemStr2("prog_code");
      kk2 = wp.itemStr2("prog_s_date").replace("/","");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where prog_code = ? "
             + " and   prog_s_date = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代碼][活動期間-起] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }

  if (!wp.itemStr2("prog_stop_flag").equals("Y")) wp.itemSet("prog_stop_flag","N");


  if (this.ibAdd)
     {
      if (wp.itemStr2("kk_prog_s_date").compareTo(wp.itemStr2("prog_e_date"))>0)
         { 
          errmsg("活動期間-迄日不可小於活動期間-起日");
          return;
         }
     }
  if (this.ibUpdate)
     {
      if (wp.itemStr2("prog_s_date").compareTo(wp.itemStr2("prog_e_date"))>0)
         { 
          errmsg("活動期間-迄日不可小於活動期間-起日");
          return;
         }
     }
  if ((this.ibAdd)||(this.ibUpdate))
     {
      if ((wp.itemStr2("prog_stop_flag").equals("Y"))&&
          (wp.itemStr2("prog_stop_date").length()==0))
         {
           errmsg("停止日期  不可空白!");
           return;
         }
      strSql = "select prog_s_date,prog_e_date "
             + "from ibn_prog "
             + "where prog_code  = ? "
             ;
      Object[] param = new Object[] {wp.itemStr2("kk_prog_code")};
      sqlSelect(strSql,param);
      
      int recCnt = sqlRowNum ;
      if (recCnt > 0)
         for (int ii=0;ii<recCnt;ii++)
           {
            if ((wp.itemStr2("prog_s_date").compareTo(colStr(ii,"prog_s_date"))>=0)&&
                (wp.itemStr2("prog_s_date").compareTo(colStr(ii,"prog_e_date"))<=0))
                {
                 errmsg("活動期間起重疊!");
                 return;
                }
            if ((wp.itemStr2("prog_e_date").compareTo(colStr(ii,"prog_s_date"))>=0)&&
                (wp.itemStr2("prog_e_date").compareTo(colStr(ii,"prog_e_date"))<=0))
                {
                 errmsg("活動期間迄重疊!");
                 return;
                }
           }
     }






  if (this.isAdd()) return;

 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName+ " ("
          + " prog_code, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " prog_flag, "
          + " prog_desc, "
          + " prog_stop_flag, "
          + " prog_stop_date, "
          + " prog_stop_desc, "
          + " apr_date, "
          + " apr_flag, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        kk2,
        wp.itemStr2("prog_e_date"),
        wp.itemStr2("prog_flag"),
        wp.itemStr2("prog_desc"),
        wp.itemStr2("prog_stop_flag"),
        wp.itemStr2("prog_stop_date"),
        wp.itemStr2("prog_stop_desc"),
        "Y",
        wp.itemStr2("zz_apr_user"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

  return rc;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "prog_e_date = ?, "
         + "prog_flag = ?, "
         + "prog_desc = ?, "
         + "prog_stop_flag = ?, "
         + "prog_stop_date = ?, "
         + "prog_stop_desc = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("prog_e_date"),
     wp.itemStr2("prog_flag"),
     wp.itemStr2("prog_desc"),
     wp.itemStr2("prog_stop_flag"),
     wp.itemStr2("prog_stop_date"),
     wp.itemStr2("prog_stop_desc"),
     wp.loginUser,
     wp.itemStr2("zz_apr_user"),
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
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
