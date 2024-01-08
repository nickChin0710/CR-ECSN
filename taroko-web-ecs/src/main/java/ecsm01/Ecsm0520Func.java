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
public class Ecsm0520Func extends FuncEdit {
  private String progname = "處理程式108/01/29 V1.00.01";
  String kkEcsTableName, db2TableName;
  String controlTabName = "sys_db2_tables";

  public Ecsm0520Func(TarokoCommon wr) {
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
      kkEcsTableName = wp.itemStr("kk_ecs_table_name");
      db2TableName = wp.itemStr("kk_db2_table_name");
    } else {
      kkEcsTableName = wp.itemStr("ecs_table_name");
      db2TableName = wp.itemStr("db2_table_name");
    }
    if (this.ibAdd)
      if (kkEcsTableName.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName
            + " where ecs_table_name = ? " + " and   db2_table_name = ? ";
        Object[] param = new Object[] {kkEcsTableName, db2TableName};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[ECS_TABLE_NAME][DB2_TABLE_NAME] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " ecs_table_name, " + " db2_table_name, "
        + " db2_table_chi_name, " + " data_flag, " + " comp_flag, " + " table_owner, "
        + " new_table_flag, " + " no_drop_flag, " + " isused_flag, " + " crnoind_flag, "
        + " depend_table, " + " use_compgm_flag, " + " addon_pgm, " + " table_comment, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {kkEcsTableName, db2TableName, wp.itemStr("db2_table_chi_name"),
        wp.itemStr("data_flag"), wp.itemStr("comp_flag"), wp.itemStr("table_owner"),
        wp.itemStr("new_table_flag"), wp.itemStr("no_drop_flag"), wp.itemStr("isused_flag"),
        wp.itemStr("crnoind_flag"), wp.itemStr("depend_table"), wp.itemStr("use_compgm_flag"),
        wp.itemStr("addon_pgm"), wp.itemStr("table_comment"), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "db2_table_chi_name = ?, " + "data_flag = ?, "
        + "comp_flag = ?, " + "table_owner = ?, " + "new_table_flag = ?, " + "no_drop_flag = ?, "
        + "isused_flag = ?, " + "crnoind_flag = ?, " + "depend_table = ?, "
        + "use_compgm_flag = ?, " + "addon_pgm = ?, " + "table_comment = ?, " + "mod_user  = ?, "
        + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemStr("db2_table_chi_name"), wp.itemStr("data_flag"),
        wp.itemStr("comp_flag"), wp.itemStr("table_owner"), wp.itemStr("new_table_flag"),
        wp.itemStr("no_drop_flag"), wp.itemStr("isused_flag"), wp.itemStr("crnoind_flag"),
        wp.itemStr("depend_table"), wp.itemStr("use_compgm_flag"), wp.itemStr("addon_pgm"),
        wp.itemStr("table_comment"), wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid")};

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
