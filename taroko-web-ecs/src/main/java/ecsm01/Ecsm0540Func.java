/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/27  V1.00.01   Allen Ho      Initial                              *
* 109-04-27 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0540Func extends FuncEdit
{
  private String progname = "處理程式108/09/27 V1.00.01";
  String kkEcsTableName, kkDb2TableName;
  String controlTabName = "sys_db2_userdef";

  public Ecsm0540Func(TarokoCommon wr) {
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
      kkDb2TableName = wp.itemStr("kk_db2_table_name");
    } else {
      kkEcsTableName = wp.itemStr("ecs_table_name");
      kkDb2TableName = wp.itemStr("db2_table_name");
    }
    if (this.ibAdd)
      if (kkEcsTableName.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName
            + " where ecs_table_name = ? " + " and   db2_table_name = ? ";
        Object[] param = new Object[] {kkEcsTableName, kkDb2TableName};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[ECS表格代碼][DB2表格代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("cycle_drop_code").equals("Y"))
      wp.itemSet("cycle_drop_code", "N");


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
        + " db2_table_chi_name, " + " table_owner, " + " crnoind_flag, " + " prior_code, "
        + " new_table_flag, " + " use_compgm_flag, " + " no_tran_flag, " + " pre_tran_flag, "
        + " condition_flag, " + " cycle_flag, " + " cycle_drop_code, " + " comp_flag, "
        + " addon_pgm, " + " depend_table, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "sysdate,?,?)";

    Object[] param = new Object[] {kkEcsTableName, kkDb2TableName, wp.itemStr("db2_table_chi_name"),
        wp.itemStr("table_owner"), wp.itemStr("crnoind_flag"), wp.itemStr("prior_code"),
        wp.itemStr("new_table_flag"), wp.itemStr("use_compgm_flag"), wp.itemStr("no_tran_flag"),
        wp.itemStr("pre_tran_flag"), wp.itemStr("condition_flag"), wp.itemStr("cycle_flag"),
        wp.itemStr("cycle_drop_code"), wp.itemStr("comp_flag"), wp.itemStr("addon_pgm"),
        wp.itemStr("depend_table"), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "db2_table_chi_name = ?, "
        + "table_owner = ?, " + "crnoind_flag = ?, " + "prior_code = ?, " + "new_table_flag = ?, "
        + "use_compgm_flag = ?, " + "no_tran_flag = ?, " + "pre_tran_flag = ?, "
        + "condition_flag = ?, " + "cycle_flag = ?, " + "cycle_drop_code = ?, " + "comp_flag = ?, "
        + "addon_pgm = ?, " + "depend_table = ?, " + "mod_user  = ?, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemStr("db2_table_chi_name"), wp.itemStr("table_owner"),
        wp.itemStr("crnoind_flag"), wp.itemStr("prior_code"), wp.itemStr("new_table_flag"),
        wp.itemStr("use_compgm_flag"), wp.itemStr("no_tran_flag"), wp.itemStr("pre_tran_flag"),
        wp.itemStr("condition_flag"), wp.itemStr("cycle_flag"), wp.itemStr("cycle_drop_code"),
        wp.itemStr("comp_flag"), wp.itemStr("addon_pgm"), wp.itemStr("depend_table"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid")};

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
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
