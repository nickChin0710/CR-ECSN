/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/05/14  V1.00.01   Allen Ho      Initial                              *
* 111/12/02  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp02;

import java.util.*;

import busi.ecs.CommFunction;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp3130Func extends busi.FuncProc
{
 private final String PROGNAME = "媒體檔案上傳作業處理程式111/12/02  V1.00.02";
  String approveTabName = "mkt_uploadfile_ctl";
  String controlTabName = "mkt_uploadfile_ctl";

 public Mktp3130Func(TarokoCommon wr)
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
 public int dbInsertA4(String tableName, String columnCol, String columnDat) throws Exception
 {
  String[] columnData = new String[300];
  String   stra="",strb="";
  int      arrSize  = 0;
  int      skipLine= 0,intk=0;
  long     listCnt   = 0;
  CommFunction comm = new CommFunction();
  listCnt = columnCol.chars().filter(ch -> ch =='|').count();
  arrSize = (int)listCnt - 1;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = comm.getStr(columnCol, inti+1 ,"|");
     if (stra.length()==0) continue;
     if (inti<(listCnt-1))
        strSql = strSql + stra + ",";
     else
        strSql = strSql + stra + "";
    }

  strSql = strSql
         + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = comm.getStr(columnCol, inti+1 ,"|");
     if (stra.length()==0) continue;
     strb = comm.getStr(columnDat, inti+1 ,"|");
     strb = comm.getStr(columnDat, inti+1 ,"|");
     if  (strb.equals("\"\"")) strb="";

     intk = strb.toUpperCase().indexOf("DATE_DATATYPE=");
     if (intk!=-1)
       {
        if (inti==(listCnt-1))
           strSql = strSql
                  + "timestamp_format(?,'yyyymmddhh24miss'))";
        else 
           strSql = strSql
                  + "timestamp_format(?,'yyyymmddhh24miss'),";
       }
     else if ((Arrays.asList("APR_DATE","MOD_TIME").contains(stra.toUpperCase()))&&
         (strb.toUpperCase().equals("SYSTEMDEFAULT")))
        {
         if (stra.toUpperCase().equals("APR_DATE"))
            {
             if (inti==(listCnt-1))
                strSql = strSql
                       + "to_char(sysdate,'yyyymmdd'))";
             else 
                strSql = strSql
                       + "to_char(sysdate,'yyyymmdd'),";
            }
         if (stra.toUpperCase().equals("MOD_TIME"))
            {
             if (inti==(listCnt-1))
                strSql = strSql
                       + "timestamp_format(?,'yyyymmddhh24miss'))";
             else 
                strSql = strSql
                       + "timestamp_format(?,'yyyymmddhh24miss'),";
            }
        }
     else
        {
         if (inti<(listCnt-1))
            strSql = strSql + "?," ;
         else
            strSql = strSql + "?)" ;
        }
    }
  Object[] param1 =new Object[300];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = comm.getStr(columnCol, inti+1 ,"|");
     if (stra.length()==0) continue;
     strb = comm.getStr(columnDat, inti+1 ,"|");
     if ((Arrays.asList("APR_DATE").contains(stra.toUpperCase()))&&
         (strb.toUpperCase().equals("SYSTEMDEFAULT"))) continue;

     intk = strb.toUpperCase().indexOf("DATE_DATATYPE=");
     if (intk!=-1)
       {
        param1[skipLine]= strb.substring(intk+14);
       }
     else if ((Arrays.asList("APR_FLAG","APR_USER","MOD_USER","MOD_TIME","MOD_PGM").contains(stra.toUpperCase()))&&
         (strb.toUpperCase().equals("SYSTEMDEFAULT")))
        {
         if (stra.toUpperCase().equals("APR_FLAG"))
            param1[skipLine]= "Y";
         if ((stra.toUpperCase().equals("APR_USER"))||
             (stra.toUpperCase().equals("MOD_USER")))
             param1[skipLine]= wp.loginUser;
         if (stra.toUpperCase().equals("MOD_PGM"))
            param1[skipLine]= wp.modPgm();
         if (stra.toUpperCase().equals("MOD_TIME"))
            param1[skipLine]= wp.sysDate + wp.sysTime;
        }
     else
        param1[skipLine]= strb ;
     skipLine++;
    }
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.logSql = false;
  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增3 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertD4(String tableName, String deleteCond) throws Exception
 {
  strSql = "delete " + tableName + " " 
         + " " + deleteCond 
         ;

  Object[] param =new Object[] {};

  sqlExec(strSql, param);

  return rc;
 }
// ************************************************************************
 public int dbUpdateU4(String transSeqno, int uploadFlag) throws Exception
 {
  String aprFlag = "Y";
  if (uploadFlag==1) aprFlag = "X";
  strSql= "update " + controlTabName + " set "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_flag  = ?, "
         + "mod_user  = ?, "
         + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "mod_pgm   = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1 "
         + "where trans_seqno     = ? " 
         ;

  Object[] param =new Object[]
    {
     wp.loginUser,
     aprFlag,
     wp.loginUser,
     wp.sysDate + wp.sysTime,
     wp.modPgm(),
     transSeqno

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
 public int dbDelete(String transSeqno) throws Exception
 {
  strSql = "delete mkt_uploadfile_data " 
         + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     transSeqno
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
