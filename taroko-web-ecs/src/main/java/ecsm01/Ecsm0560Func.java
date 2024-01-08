/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
*  109-04-27 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                        *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0560Func extends FuncEdit
{
  private String progname = "處理程式108/01/29 V1.00.01";
  String sequenceName;
  String controlTabName = "sys_db2_sequence";

  public Ecsm0560Func(TarokoCommon wr) {
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
      sequenceName = wp.itemStr("kk_sequence_name");
    } else {
      sequenceName = wp.itemStr("sequence_name");
    }
    if (this.ibAdd)
      if (sequenceName.length() > 0) {
        strSql =
            "select count(*) as qua " + "from " + controlTabName + " where sequence_name = ? ";
        Object[] param = new Object[] {sequenceName};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[序列名稱:] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " sequence_name, " + " start_value, "
        + " increment_value, " + " maxvalue_value, " + " ref_sequence, " + " table_owner, "
        + " redo_flag, " + " create_flag, " + " cycle_flag, " + " comp_flag, " + " cache_value, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?," + "sysdate,?,?)";

    Object[] param = new Object[] {sequenceName, wp.itemNum("start_value"), wp.itemNum("increment_value"),
        wp.itemNum("maxvalue_value"), wp.itemStr("ref_sequence"), wp.itemStr("table_owner"),
        wp.itemStr("redo_flag"), wp.itemStr("create_flag"), wp.itemStr("cycle_flag"),
        wp.itemStr("comp_flag"), wp.itemNum("cache_value"), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "start_value = ?, " + "increment_value = ?, "
        + "maxvalue_value = ?, " + "ref_sequence = ?, " + "table_owner = ?, " + "redo_flag = ?, "
        + "create_flag = ?, " + "cycle_flag = ?, " + "comp_flag = ?, " + "cache_value = ?, "
        + "mod_user  = ?, " + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemNum("start_value"), wp.itemNum("increment_value"),
        wp.itemNum("maxvalue_value"), wp.itemStr("ref_sequence"), wp.itemStr("table_owner"),
        wp.itemStr("redo_flag"), wp.itemStr("create_flag"), wp.itemStr("cycle_flag"),
        wp.itemStr("comp_flag"), wp.itemNum("cache_value"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid")};

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
