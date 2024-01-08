/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/10  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 112-03-31  V1.00.03  Ryan   修正dbInsertMktUploadfileData      *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1050Func extends FuncEdit {
  private String PROGNAME = "媒體檔案上傳作業處理程式108/09/10 V1.00.01";
  String controlTabName = "mkt_uploadfile_ctl";

  public Mktm1050Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (!this.ibAdd) {
    }


    if (this.ibDelete) {
      if (wp.itemStr("apr_flag").equals("Y")) {
        errmsg("該筆資料已覆核, 不可刪除!");
        return;
      }
    }

    if (this.isAdd())
      return;

    // -other modify-
    sqlWhere =
        "where rowid = x'" + wp.itemStr("rowid") + "'" + " and nvl(mod_seqno,0)=" + wp.modSeqno();

    if (this.isOtherModify(controlTabName, sqlWhere)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    dbDeleteMktUploadfileData(wp.itemStr("trans_seqno"));

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertMktUploadfileData(String tranSeqStr, String[] uploadFileCol,
      String[] uploadfileDat, int colNum) {
	  this.msgOK();
    String columnCol = "";
    String columnDat = "";
    int inti = 0;
    for (inti = 0; inti < colNum; inti++) {
      if (uploadFileCol[inti].length() == 0)
        continue;
      columnCol = columnCol + "\"" + uploadFileCol[inti] + "\"|";
      columnDat = columnDat + "\"" + uploadfileDat[inti] + "\"|";
    }
    dateTime();
    strSql = " insert into mkt_uploadfile_data (" + " file_type, " + " file_date, " + " file_name, "
        + " table_name, " + " trans_seqno, " + " data_column01, " + " data_data01, "
        + " mod_time,mod_pgm,mod_seqno " + " ) values (" + "?,?,?,?,?,?,?,"
        + "timestamp_format(?,'yyyymmddhh24miss'),?,?)";

    Object[] param =
        new Object[] {"MKT_THSR_REDEM", wp.sysDate, wp.itemStr("zz_file_name"), "MKT_THSR_REDEM",
            tranSeqStr, columnCol, columnDat, wp.sysDate + wp.sysTime, wp.modPgm(), 0};

    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      return (-1);

    return rc;
  }

  // ************************************************************************
  public int dbInsertMktUploadfileCtl(String tranSeqStr, int fileCnt, int errorCnt,
      String[] passStr, int[] datachkCnt) {
    String fileFlag = "Y";
    String aprFlag = "N";
    if (errorCnt > 0) {
      fileFlag = "N";
      aprFlag = "T";
    }
    dbSelectPtrSysIdtab();
    dateTime();
    strSql = " insert into mkt_uploadfile_ctl (" + " file_type, " + " type_name, " + " file_date, "
        + " file_time, " + " file_name, " + " trans_seqno, " + " table_name, " + " file_flag, "
        + " file_cnt, " + " error_cnt, " + " apr_flag, " + " error_desc, " + " callbatch_pgm, "
        + " crt_date, " + " crt_user, " + " mod_time,mod_user,mod_pgm,mod_seqno " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?," // 10 record
        + "?,'MktT200'," + "?,?," // 2 records
        + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

    Object[] param = new Object[] {"MKT_THSR_REDEM", colStr("wf_desc"), wp.sysDate, wp.sysTime,
        wp.itemStr("zz_file_name"), tranSeqStr, "MKT_THSR_REDEM", fileFlag, fileCnt + errorCnt,
        errorCnt, aprFlag, !empty(passStr[1])?String.format(passStr[1], passStr[2]):"", wp.sysDate, wp.loginUser, wp.sysDate + wp.sysTime,
        wp.loginUser, wp.modPgm(), 0};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      return (-1);

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg) {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10]))
      errMsg[10] = "0";
    if (!comm.isNumber(errMsg[1]))
      errMsg[1] = "0";
    if (!comm.isNumber(errMsg[2]))
      errMsg[2] = "0";

    strSql = " insert into ecs_media_errlog (" + " crt_date, " + " crt_time, " + " file_name, "
        + " unit_code, " + " main_desc, " + " error_seq, " + " error_desc, " + " line_seq, "
        + " column_seq, " + " column_data, " + " trans_seqno, " + " column_desc, "
        + " program_code, " + " mod_time, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," // 10
                                                                                                   // record
        + "?,?,?," // 4 trvotfd
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, wp.itemStr("zz_file_name"),
        comr.getObjectOwner("3", wp.modPgm()), errMsg[0], Integer.valueOf(errMsg[1]), errMsg[4],
        Integer.valueOf(errMsg[10]), Integer.valueOf(errMsg[2]), errMsg[3], tranSeqStr, errMsg[5],
        wp.modPgm(), wp.sysDate + wp.sysTime, wp.modPgm()};

    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_media_errlog 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt) {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql = " insert into ecs_notify_log (" + " crt_date, " + " crt_time, " + " unit_code, "
        + " obj_type, " + " notify_head, " + " notify_name, " + " notify_desc1, "
        + " notify_desc2, " + " trans_seqno, " + " mod_time, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?," // 9 record
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, comr.getObjectOwner("3", wp.modPgm()),
        "3", "媒體檔轉入資料有誤(只記錄前100筆)", "媒體檔名:" + wp.itemStr("zz_file_name"),
        "程式 " + wp.modPgm() + " 轉 " + wp.itemStr("zz_file_name") + " 有" + errorCnt + " 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤", tranSeqStr, wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_modify_log 錯誤");
    return rc;
  }

  // ************************************************************************
  public int dbDeleteMktUploadfileData(String tranSeqStr) {
    strSql = "delete  mkt_uploadfile_data " + "where trans_seqno = ?";

    Object[] param = new Object[] {tranSeqStr};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 mkt_uploadfile_data 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteMktUploadfileDataP() {
    dateTime();
    strSql = "delete  mkt_uploadfile_data " + "where file_type = ? " + "and  file_date  = ? "
        + "and  file_name  = ? ";

    Object[] param = new Object[] {"MKT_THSR_REDEM", wp.sysDate, wp.itemStr("zz_file_name")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 mkt_uploadfile_data 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbSelectPtrSysIdtab() {
    strSql = " select " + " wf_desc " + " from ptr_sys_idtab "
        + " where wf_type = 'MKT_UPLOADFILE_CTL' " + " and   wf_id   = ? ";

    Object[] param = new Object[] {"MKT_THSR_REDEM",};

    sqlSelect(strSql, param);

    return rc;
  }
  // ************************************************************************

} // End of class
