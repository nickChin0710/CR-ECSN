/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0670Func extends busi.FuncProc {
  private String PROGNAME = "媒體檔案上傳作業處理程式108/09/03 V1.00.01";
  String approveTabName = "mkt_uploadfile_ctl";
  String controlTabName = "mkt_uploadfile_ctl";

  public Mktp0670Func(TarokoCommon wr) {
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
  public void dataCheck() {}

  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }

  // ************************************************************************
  public int dbInsertA4(String tableName, String columnCol, String columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int arrSize = 0;
    int skipLine = 0;
    long listCnt = 0;
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    listCnt = columnCol.chars().filter(ch -> ch == '|').count();
    arrSize = (int) listCnt - 1;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = comm.getStr(columnCol, inti + 1, "|");
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " apr_flag, " + " apr_date, " + " apr_user, " + " mod_time,mod_user,mod_pgm "
        + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = comm.getStr(columnCol, inti + 1, "|");
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "timestamp_format(?,'yyyymmddhh24miss'),?,?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = comm.getStr(columnCol, inti + 1, "|");
      if (stra.length() == 0)
        continue;
      stra = comm.getStr(columnDat, inti + 1, "|");
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = "Y";
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4(String transSeqno, int uploadFlag) {
    String aprFlag = "Y";
    if (uploadFlag == 1)
      aprFlag = "X";
    strSql = "update " + controlTabName + " set " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where trans_seqno     = ? ";

    Object[] param = new Object[] {wp.loginUser, aprFlag, wp.loginUser, wp.sysDate + wp.sysTime,
        wp.modPgm(), transSeqno

    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDelete(String trans_seqno) {
    strSql = "delete mkt_uploadfile_data " + "where trans_seqno = ?";

    Object[] param = new Object[] {trans_seqno};

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

} // End of class
