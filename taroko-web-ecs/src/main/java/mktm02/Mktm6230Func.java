/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/09/03  V1.00.03   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                        *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6230Func extends FuncEdit
{
 private final String PROGNAME = "媒體檔案上傳作業處理程式111-11-30  V1.00.01";
  String control_tab_name = "mkt_uploadfile_ctl";

 public Mktm6230Func(TarokoCommon wr)
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
      if (wp.itemStr2("apr_flag").equals("Y"))
         {
          errmsg("該筆資料已覆核, 不可刪除!");
          return;
         }
     }

  if (this.isAdd()) return;

  //-other modify-
  sqlWhere = "where rowid = x'" + wp.itemStr2("rowid") +"'"
            + " and nvl(mod_seqno,0)=" + wp.modSeqno();

  if (this.isOtherModify(control_tab_name, sqlWhere))
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
     if (selectAllTabColumns("MKT_LOAN",colNarr[intm])!=1)
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
        "MKT_LOAN",
        groupType,
        wp.sysDate,
        wp.itemStr2("zz_file_name"),
        "MKT_LOAN",
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
          + " error_memo, "
          + " callbatch_pgm, "
          + " callbatch_parm, "
          + " amt_name1, "
          + " amt_name2, "
          + " file_amt1, "
          + " type_name, "
          + " group_type, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time,mod_user,mod_pgm,mod_seqno "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?," // 9 record
          + "?,?,?,?,'回饋累計金額','失敗累計金額',?,'外部單位專案回饋金(人工上傳)','0',"
          + "?,?,"                 // 2 records
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

  Object[] param =new Object[]
       {
        "MKT_LOAN",
        wp.sysDate,
        wp.sysTime,
        wp.itemStr2("zz_file_name"),
        tranSeqStr,
        "MKT_LOAN",
        fileFlag,
        fileCnt+errorCnt,
        errorCnt,
        aprFlag,
        hideStr[1],
        hideStr[2],
        hideStr[3],
        tranSeqStr,
        datachkCnt[3],
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
        wp.itemStr2("zz_file_name"),
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
        "媒體檔名:"+wp.itemStr2("zz_file_name"),
        "程式 "+wp.modPgm()+" 轉 "+wp.itemStr2("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
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
     "MKT_LOAN",
      groupType,
      wp.sysDate,
      wp.itemStr2("zz_file_name")
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
        "MKT_LOAN",
       };

  sqlSelect(strSql, param);

  return rc;
 }
 public  int dbupdateMktUploadfileCtl(String fileFlag,String procFlag) throws Exception
 {
  String error_memo = "";
  if (procFlag.equals("C"))
      error_memo = "無異常資料, 已確認待覆核";
  else 
      error_memo = "無異常資料, 可執行確認";
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
     error_memo,
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteMktUploadfileCtl() throws Exception
 {
  if (wp.itemStr2("apr_flag").equals("Y"))
     {
      errmsg("該筆資料已發簡訊, 不可刪除!");
      return(-1);
     }
  if (wp.itemStr2("proc_flag").equals("C"))
     {
      errmsg("該筆資料已確認, 不可刪除, 請先解確認處理!");
      return(-1);
     }
  dbDeleteMktUploadfileData(wp.itemStr2("trans_seqno"));

  strSql = "delete " +control_tab_name + " "
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (sqlRowNum <= 0)
     {
      errmsg("刪除 "+ control_tab_name +" 錯誤");
      return(-1);
     }

  return rc;
 }
// ************************************************************************
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
 public int dbDeleteMktLoan(String transSeqno) throws Exception
 {
  strSql = "delete mkt_loan "
         + "where trans_seqno = ? "
         + "and tran_seqno='' "
         ;

  Object[] param =new Object[]
    {
     transSeqno
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0)
     {
      errmsg("刪除 mkt_loan  錯誤");
      return(-1);
     }

  return rc;
 }

// ************************************************************************

}  // End of class
