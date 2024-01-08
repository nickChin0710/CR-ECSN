/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/08  V1.00.01   Allen Ho      Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0100Func extends FuncEdit
{
  private String progname = "系統刪除資料庫表格參數維護處理程式108/11/08 V1.00.01";
  String tableName, rmtabMode;
  String controlTabName = "ecs_rmtab_parm";

  public Ecsm0100Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      tableName = wp.itemStr("kk_table_name");
      if (empty(tableName)) {
        errmsg("表格代碼 不可空白");
        return;
      }
      rmtabMode = wp.itemStr("kk_rmtab_mode");
    } else {
      tableName = wp.itemStr("table_name");
      rmtabMode = wp.itemStr("rmtab_mode");
    }
    if (this.ibAdd)
      if (tableName.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where table_name = ? "
            + " and   rmtab_mode = ? ";
        Object[] param = new Object[] {tableName, rmtabMode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[表格代碼][移檔模式] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("avoid_cycle").equals("Y"))
      wp.itemSet("avoid_cycle", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (rmtabMode.equals("2"))
        if (wp.itemStr("hst_table_name").length() == 0)
          wp.itemSet("hst_table_name", tableName + "_HST");
    }

    if ((this.ibAdd)) {
      strSql = "select data_key " + "from mkt_bn_data " + "where  table_name  =  'ECS_RMTAB_PARM' "
          + "and data_type  =  '6' " + "and data_code  =  ?";
      Object[] param = new Object[] {tableName};
      sqlSelect(strSql, param);
      if (sqlRowNum > 0) {
        errmsg(tableName + "已存在比照處理表格 "
            + colStr(0, "data_key").substring(0, colStr(0, "data_key").length() - 1));
        return;
      }
    }

    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  " + controlTabName + " (" + " table_name, " + " rmtab_mode, "
        + " rmtab_desc, " + " hst_table_name, " + " stop_date, " + " stop_desc, " + " rmtime_type, "
        + " cycle_day_flag, " + " date_type, " + " avoid_cycle, " + " commit_flag, "
        + " commit_rows, " + " rmtab_where, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {tableName, rmtabMode, wp.itemStr("rmtab_desc"), wp.itemStr("hst_table_name"),
        wp.itemStr("stop_date"), wp.itemStr("stop_desc"), wp.itemStr("rmtime_type"),
        wp.itemStr("cycle_day_flag"), wp.itemStr("date_type"), wp.itemStr("avoid_cycle"),
        wp.itemStr("commit_flag"), wp.itemNum("commit_rows"), wp.itemStr("rmtab_where"),
        wp.itemStr("approval_user"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "rmtab_desc = ?, " + "hst_table_name = ?, "
        + "stop_date = ?, " + "stop_desc = ?, " + "rmtime_type = ?, " + "cycle_day_flag = ?, "
        + "date_type = ?, " + "avoid_cycle = ?, " + "commit_flag = ?, " + "commit_rows = ?, "
        + "rmtab_where = ?, " + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("rmtab_desc"), wp.itemStr("hst_table_name"),
        wp.itemStr("stop_date"), wp.itemStr("stop_desc"), wp.itemStr("rmtime_type"),
        wp.itemStr("cycle_day_flag"), wp.itemStr("date_type"), wp.itemStr("avoid_cycle"),
        wp.itemStr("commit_flag"), wp.itemNum("commit_rows"), wp.itemStr("rmtab_where"),
        wp.loginUser, wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }

  // ************************************************************************
  public int dbInsertI5() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_same"))
      dataType = "6";
    strSql = "insert into MKT_BN_DATA ( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'ECS_RMTAB_PARM', " + "?, " + "?, " + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType,
        varsStr("data_code"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD5() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_same"))
      dataType = "6";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType};
    if (sqlRowcount("MKT_BN_DATA",
        "where data_key = ? " + "and   data_type = ? " + "and   table_name = 'ECS_RMTAB_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA " + "where data_key = ? " + "and   data_type = ? "
        + "and   table_name = 'ECS_RMTAB_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_week"))
      dataType = "2";
    if (wp.respHtml.equals("ecsm0100_mont"))
      dataType = "3";
    strSql = "insert into MKT_BN_DATA ( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'ECS_RMTAB_PARM', " + "?, " + "?, " + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType,
        varsStr("data_code"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_week"))
      dataType = "2";
    if (wp.respHtml.equals("ecsm0100_mont"))
      dataType = "3";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType};
    if (sqlRowcount("MKT_BN_DATA",
        "where data_key = ? " + "and   data_type = ? " + "and   table_name = 'ECS_RMTAB_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA " + "where data_key = ? " + "and   data_type = ? "
        + "and   table_name = 'ECS_RMTAB_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI4() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_year"))
      dataType = "4";
    strSql = "insert into MKT_BN_DATA ( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'ECS_RMTAB_PARM', " + "?, " + "?, "
        + "?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType,
        varsStr("data_code"), varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_year"))
      dataType = "4";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType};
    if (sqlRowcount("MKT_BN_DATA",
        "where data_key = ? " + "and   data_type = ? " + "and   table_name = 'ECS_RMTAB_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA " + "where data_key = ? " + "and   data_type = ? "
        + "and   table_name = 'ECS_RMTAB_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_varn"))
      dataType = "1";
    strSql = "insert into MKT_BN_DATA ( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code," + "data_code2," + "data_code3," + "crt_date, " + "crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + ") values (" + "'ECS_RMTAB_PARM', " + "?, "
        + "?, " + "?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1,"
        + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType,
        varsStr("data_code"), varsStr("data_code2"), varsStr("data_code3"), wp.loginUser,
        wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("ecsm0100_varn"))
      dataType = "1";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode"), dataType};
    if (sqlRowcount("MKT_BN_DATA",
        "where data_key = ? " + "and   data_type = ? " + "and   table_name = 'ECS_RMTAB_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA " + "where data_key = ? " + "and   data_type = ? "
        + "and   table_name = 'ECS_RMTAB_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbSelect_copy() {
    strSql = " select " + " table_name, " + " rmtab_mode, " + " rmtab_desc, " + " hst_table_name, "
        + " rmtime_type, " + " cycle_day_flag, " + " avoid_cycle, " + " commit_flag, "
        + " commit_rows, " + " rmtab_where  " + " from " + controlTabName
        + " where table_name = ? " + " and   rmtab_mode = ? ";

    Object[] param = new Object[] {wp.itemStr("table_name"), wp.itemStr("rmtab_mode")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(sqlErrtext);

    return rc;
  }

  // ************************************************************************
  public int dbInsertCopy() {
    rc = dbSelect_copy();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + controlTabName + " (" + " table_name, " + " rmtab_mode, "
        + " rmtab_desc, " + " hst_table_name, " + " rmtime_type, " + " cycle_day_flag, "
        + " avoid_cycle, " + " commit_flag, " + " commit_rows, " + " rmtab_where, "
        // + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,"
        // + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {wp.itemStr("copy_table_name"), colStr("rmtab_mode"),
        colStr("rmtab_desc"), colStr("hst_table_name"), colStr("rmtime_type"),
        colStr("cycle_day_flag"), colStr("avoid_cycle"), colStr("commit_flag"),
        colStr("commit_rows"), colStr("rmtab_where"),
        // "Y",
        wp.itemStr("apr_user"), wp.loginUser, wp.sysDate + wp.sysTime, wp.loginUser, 0,
        wp.modPgm()};


    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(sqlErrtext);

    return rc;
  }

  // ************************************************************************
  public int dbInsertCopyBnData(int skipFlag) {
    if (rc != 1)
      return rc;

    strSql = "insert into mkt_bn_data( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code, " + "data_code2, " + "data_code3, " + "data_code4, " + "crt_date, "
        + "crt_user, " + "mod_user, " + "mod_time, " + "mod_pgm) " + "select  " + "table_name, "
        + "?, " + "data_type, " + "data_code, " + "data_code2, " + "data_code3, " + "data_code4, "
        + "to_char(sysdate,'yyyymmdd')," + "?, " + "?, "
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "? " + "from  mkt_bn_data "
        + "where table_name = 'ECS_RMTAB_PARM' " + "and   data_key     = ? ";;
    if (skipFlag == 6)
      strSql = strSql + "and   data_type  != '6' ";

    Object[] param = new Object[] {wp.itemStr("copy_table_name") + wp.itemStr("rmtab_mode"),
        wp.loginUser, wp.loginUser, wp.sysDate + wp.sysTime, wp.modPgm(),
        wp.itemStr("table_name") + wp.itemStr("rmtab_mode")};

    rc = sqlExec(strSql, param);

    return rc;
  }


  // ************************************************************************

} // End of class
