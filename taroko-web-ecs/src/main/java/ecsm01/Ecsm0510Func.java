/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-27 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0510Func extends FuncEdit
{
  private String progname = "處理程式108/01/29 V1.00.01";
  String kkDb2TableName, kkDb2ColumnName;
  String controlTabName = "sys_db2_columns";

  public Ecsm0510Func(TarokoCommon wr) {
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
      kkDb2TableName = wp.itemStr("kk_db2_table_name");
      kkDb2ColumnName = wp.itemStr("kk_db2_column_name");
    } else {
      kkDb2TableName = wp.itemStr("db2_table_name");
      kkDb2ColumnName = wp.itemStr("db2_column_name");
    }
    if (this.ibAdd)
      if (kkDb2TableName.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName
            + " where db2_table_name = ? " + " and   db2_column_name = ? ";
        Object[] param = new Object[] {kkDb2TableName, kkDb2ColumnName};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[DB2_TABLE_NAME][DB2_COLUMN_NAME] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " db2_table_name, " + " db2_column_name, "
        + " db2_data_type, " + " db2_data_desc, " + " db2_data_comment, " + " ecs_table_name, "
        + " ecs_column_name, " + " db2_default_value, " + " data_flag, " + " comp_flag, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," + "sysdate,?,?)";

    Object[] param = new Object[] {kkDb2TableName, kkDb2ColumnName, wp.itemStr("db2_data_type"),
        wp.itemStr("db2_data_desc"), wp.itemStr("db2_data_comment"), wp.itemStr("ecs_table_name"),
        wp.itemStr("ecs_column_name"), wp.itemStr("db2_default_value"), wp.itemStr("data_flag"),
        wp.itemStr("comp_flag"), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "db2_data_type = ?, " + "db2_data_desc = ?, "
        + "db2_data_comment = ?, " + "ecs_table_name = ?, " + "ecs_column_name = ?, "
        + "db2_default_value = ?, " + "data_flag = ?, " + "comp_flag = ?, " + "mod_user  = ?, "
        + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemStr("db2_data_type"), wp.itemStr("db2_data_desc"),
        wp.itemStr("db2_data_comment"), wp.itemStr("ecs_table_name"), wp.itemStr("ecs_column_name"),
        wp.itemStr("db2_default_value"), wp.itemStr("data_flag"), wp.itemStr("comp_flag"),
        wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

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

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
