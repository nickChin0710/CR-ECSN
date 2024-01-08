/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/07  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 111-11-28  V1.00.03  Zuwei      Sync from mega                           *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1070Func extends FuncEdit {
 private final String PROGNAME = "媒體檔案上傳作業處理程式111/11/28 V1.00.03";
  String controlTabName = "mkt_uploadfile_ctl";

 public Mktm1070Func(TarokoCommon wr)
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
  if (!this.ibAdd)
     {
     }


  if (this.ibDelete)
     {
      if (wp.itemStr("apr_flag").equals("Y"))
         {
          errmsg("該筆資料已覆核, 不可刪除!");
          return;
         }
     }

  if (this.isAdd()) return;

  //-other modify-
  sqlWhere = "where rowid = x'" + wp.itemStr("rowid") +"'"
            + " and nvl(mod_seqno,0)=" + wp.modSeqno();

  if (this.isOtherModify(controlTabName, sqlWhere))
     {
      errmsg("請重新查詢 !");
      return;
     }
 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  return 1 ;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  return rc;
}
// ************************************************************************
 @Override
 public int dbDelete()
 {
  return 1;
 }
// ************************************************************************
 public int dbInsertMktUploadfileData(String tranSeqStr,String[] uploadFileCol,String[] uploadfileDat,int colNum,String groupType,int lineCnt) throws Exception
 {
  String columnCol="";
  String columnDat="";
  int inti=0;
  for (inti=0;inti<colNum;inti++)
    {
     if (uploadFileCol[inti].length()==0) continue;
     columnCol=columnCol+"\""+uploadFileCol[inti]+"\"|";
     columnDat=columnDat+"\""+uploadfileDat[inti]+"\"|";
    }

  String colName = "",colData="";
  String[] colNarr = new String[6];
  String[] colDarr = new String[6];
  String[] colEsixt = {"N","N","N","N","N","N"};

  colNarr[0]="apr_flag";
  colNarr[1]="apr_date";
  colNarr[2]="apr_user";
  colNarr[3]="mod_time";
  colNarr[4]="mod_user";
  colNarr[5]="mod_pgm";
  for (int intm=0;intm<6;intm++)
     if (selectAllTabColumns("MKT_THSR_UPIDNO",colNarr[intm])!=1)
        colEsixt[intm]="Y";

  colDarr[0]="systemdefault";
  colDarr[1]="systemdefault";
  colDarr[2]="systemdefault";
  colDarr[3]="systemdefault";
  colDarr[4]="systemdefault";
  colDarr[5]="systemdefault";

  for (int intm=0;intm<6;intm++)
    for (inti=0;inti<colNum;inti++)
      if (uploadFileCol[inti].toUpperCase().equals(colNarr[intm].toUpperCase()))
         {
          colEsixt[intm]="Y";
          break;
         }

  for (int intm=0;intm<6;intm++)
    {
     if (colEsixt[intm].equals("Y")) continue;
     columnCol=columnCol+"\""+colNarr[intm]+"\"|";
     columnDat=columnDat+"\""+colDarr[intm]+"\"|";
    }
  dateTime();
  strSql= " insert into mkt_uploadfile_data ("
          + " file_type, "
          + " group_type, "
          + " file_date, "
          + " file_name, "
          + " table_name, "
          + " trans_seqno, "
          + " data_column01, "
          + " data_data01, "
          + " mod_time,mod_pgm,mod_seqno "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,"
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?)";

  Object[] param =new Object[]
       {
        "MKT_THSR_UPIDNO_2",
        groupType,
        wp.sysDate,
        wp.itemStr("zz_file_name"),
        "MKT_THSR_UPIDNO",
        tranSeqStr,
        columnCol,
        columnDat,
        wp.sysDate + wp.sysTime,
        wp.modPgm(),
        lineCnt
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , false);
  if (sqlRowNum <= 0) return(-1);

  return rc;
 }
// ************************************************************************
 public int selectAllTabColumns(String tabName,String colName) throws Exception
 {
  strSql= " select "
          + " colname "
          + " from syscat.columns"
          + " where tabname = ? "
          + " and   colname = ? ";

  Object[] param =new Object[]
       {
        tabName.toUpperCase(), 
        colName.toUpperCase(), 
       };

  wp.logSql = false;
  sqlSelect(strSql, param);

   if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbInsertMktUploadfileCtl(String tranSeqStr,int fileCnt,int errorCnt,String[] hideStr,int[] datachkCnt) throws Exception
 {
  String fileFlag="Y";
  String aprFlag="N";
  if ((errorCnt>0)||
      (datachkCnt[0]!=0))
     {
      fileFlag="N";
      aprFlag="T";
      if (datachkCnt[0]==2)
         {
          fileFlag="W";
          aprFlag="N";
         }
      else if (datachkCnt[0]==3)
         {
          fileFlag="B";
          aprFlag="N";
         }
     }
  dbSelectPtrSysIdtab();
  dateTime();
  strSql= " insert into mkt_uploadfile_ctl ("
          + " file_type, "
          + " file_date, "
          + " file_time, "
          + " file_name, "
          + " trans_seqno, "
          + " table_name, "
          + " file_flag, "
          + " file_cnt, "
          + " error_cnt, "
          + " apr_flag, "
          + " error_desc, "
          + " file_amt1, "
          + " delete_flag, "
          + " amt_name1, "
          + " callbatch_parm, "
          + " type_name, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time,mod_user,mod_pgm,mod_seqno "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?," // 9 record
          + "?,?,'N','累計減免次數',?,'高鐵車廂減免名單(請至mktq1070查詢)',"
          + "?,?,"                 // 2 records
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

  Object[] param =new Object[]
       {
        "MKT_THSR_UPIDNO_2",
        wp.sysDate,
        wp.sysTime,
        wp.itemStr("zz_file_name"),
        tranSeqStr,
        "MKT_THSR_UPIDNO",
        fileFlag,
        fileCnt+errorCnt,
        errorCnt,
        aprFlag,
        hideStr[1],
        datachkCnt[3],
        tranSeqStr,
        wp.sysDate,
        wp.loginUser,
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        wp.modPgm(),
        0
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) return(-1);

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsMediaErrlog(String tranSeqStr,String[] errMsg ) throws Exception
 {
  dateTime();
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  comr.setConn(wp);

  if (!comm.isNumber(errMsg[10])) errMsg[10]="0";
  if (!comm.isNumber(errMsg[1])) errMsg[1]="0";
  if (!comm.isNumber(errMsg[2])) errMsg[2]="0";

  strSql= " insert into ecs_media_errlog ("
          + " crt_date, "
          + " crt_time, "
          + " file_name, "
          + " unit_code, "
          + " main_desc, "
          + " error_seq, "
          + " error_desc, "
          + " line_seq, "
          + " column_seq, "
          + " column_data, "
          + " trans_seqno, "
          + " column_desc, "
          + " program_code, "
          + " mod_time, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?," // 10 record
          + "?,?,?,"               // 4 trvotfd
          + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param =new Object[]
       {
        wp.sysDate,
        wp.sysTime,
        wp.itemStr("zz_file_name"),
        comr.getObjectOwner("3",wp.modPgm()),
        errMsg[0],
        Integer.valueOf(errMsg[1]),
        errMsg[4],
        Integer.valueOf(errMsg[10]),
        Integer.valueOf(errMsg[2]),
        errMsg[3],
        tranSeqStr,
        errMsg[5],
        wp.modPgm(),
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, false);
  if (sqlRowNum <= 0) errmsg("新增4 ecs_media_errlog 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsNotifyLog(String tranSeqStr,int errorCnt ) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  dateTime();
  strSql= " insert into ecs_notify_log ("
          + " crt_date, "
          + " crt_time, "
          + " unit_code, "
          + " obj_type, "
          + " notify_head, "
          + " notify_name, "
          + " notify_desc1, "
          + " notify_desc2, "
          + " trans_seqno, "
          + " mod_time, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?," // 9 record
          + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param =new Object[]
       {
        wp.sysDate,
        wp.sysTime,
        comr.getObjectOwner("3",wp.modPgm()),
        "3",
        "媒體檔轉入資料有誤(只記錄前100筆)",
        "媒體檔名:"+wp.itemStr("zz_file_name"),
        "程式 "+wp.modPgm()+" 轉 "+wp.itemStr("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤",
        tranSeqStr,
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, false);
  if (sqlRowNum <= 0) errmsg("新增5 ecs_modify_log 錯誤");
  return rc;
 }
// ************************************************************************
 public int dbDeleteAddonProc() throws Exception
 {
  String isSql = "";


  return rc;
 }
// ************************************************************************
 public int dbDeleteMktUploadfileData(String tranSeqStr) throws Exception
 {
  strSql = "delete  mkt_uploadfile_data "
         + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     tranSeqStr
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 mkt_uploadfile_data 錯誤");

  return rc;
 }
// ************************************************************************
  public int dbDeleteMktUploadfileDataP(String groupType) throws Exception
 {
  dateTime();
  strSql = "delete  mkt_uploadfile_data "
         + "where file_type = ? "
         + "and   group_type = ? "
         + "and   file_date  = ? "
         + "and   file_name  = ? ";

  Object[] param =new Object[]
    {
     "MKT_THSR_UPIDNO_2",
      groupType,
      wp.sysDate,
      wp.itemStr("zz_file_name")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 mkt_uploadfile_data 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbSelectPtrSysIdtab() throws Exception
 {
  strSql= " select "
          + " wf_desc "
          + " from ptr_sys_idtab "
          + " where wf_type = 'MKT_UPLOADFILE_CTL' "
          + " and   wf_id   = ? ";

  Object[] param =new Object[]
       {
        "MKT_THSR_UPIDNO_2",
       };

  sqlSelect(strSql, param);

  return rc;
 }
 public  int dbupdateMktUploadfileCtl(String fileFlag,String procFlag) throws Exception
 {
  String errorMemo = "";
  if (procFlag.equals("C"))
      errorMemo = "無異常資料, 已確認待覆核";
  else
      errorMemo = "無異常資料, 可執行確認";
  strSql= "update mkt_uploadfile_ctl set "
        + " proc_flag = ?, "
        + " error_memo = ?, "
        + " apr_flag = 'N', "
        + " apr_date = '', "
        + " apr_user = '' "
        + "where rowid = ?";

  Object[] param =new Object[]
    {
     procFlag,
     errorMemo,
     wp.itemRowId("rowid")
    };


  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteMktUploadfileCtl() throws Exception
 {
  if (wp.itemStr("apr_flag").equals("Y"))
     {
      errmsg("該筆資料已發簡訊, 不可刪除!");
      return(-1);
     }
  if (wp.itemStr("proc_flag").equals("C"))
     {
      errmsg("該筆資料已確認, 不可刪除, 請先解確認處理!");
      return(-1);
     }
  dbDeleteMktUploadfileData(wp.itemStr("trans_seqno"));

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
 public int dbDeleteMktUploadfileCtl(String transSeqno) throws Exception
 {
  strSql = "delete mkt_uploadfile_ctl "
         + "where trans_seqno = ? ";
  
  Object[] param =new Object[]
    {
     transSeqno
    };
  
  sqlExec(strSql, param);
  if (sqlRowNum <= 0)
     {
      errmsg("刪除 mkt_uploadfile_ctl_1  錯誤");
      return(-1);
     }
  
  return rc;
 }
// ************************************************************************
 public int dbDeleteMktThsrUpidno(String transSeqno) throws Exception
 {
  strSql = "delete mkt_thsr_upidno "
         + "where trans_seqno = ? "
         ;

  Object[] param =new Object[]
    {
     transSeqno
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0)
     {
      errmsg("刪除 mkt_thsr_upidno  錯誤");
      return(-1);
     }

  return rc;
 }
  // ************************************************************************

} // End of class
