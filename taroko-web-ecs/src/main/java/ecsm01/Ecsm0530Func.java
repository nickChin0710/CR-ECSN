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
public class Ecsm0530Func extends FuncEdit {
  private String progname = "處理程式108/01/29 V1.00.01";
  String tableName;
  String controlTabName = "sys_ecs_tables";

  public Ecsm0530Func(TarokoCommon wr) {
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
    } else {
      tableName = wp.itemStr("table_name");
    }
    if (this.ibAdd)
      if (tableName.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where table_name = ? ";
        Object[] param = new Object[] {tableName};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[TABLE_NAME] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " table_name, " + " table_chi_name, "
        + " column_name, " + " data_records, " + " data_flag, " + " comp_flag, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?," + "sysdate,?,?)";

    Object[] param = new Object[] {tableName, wp.itemStr("table_chi_name"), wp.itemStr("column_name"),
        wp.itemNum("data_records"), wp.itemStr("data_flag"), wp.itemStr("comp_flag"), wp.loginUser,
        wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "table_chi_name = ?, " + "column_name = ?, "
        + "data_records = ?, " + "data_flag = ?, " + "comp_flag = ?, " + "mod_user  = ?, "
        + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemStr("table_chi_name"), wp.itemStr("column_name"),
        wp.itemNum("data_records"), wp.itemStr("data_flag"), wp.itemStr("comp_flag"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid")};

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
