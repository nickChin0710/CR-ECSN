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
public class Ecsm0580Func extends FuncEdit {
  private String progname = "處理程式108/01/29 V1.00.01";
  String kkPgmId, kkPgmParm;
  String controlTabName = "sys_db2_pgm";

  public Ecsm0580Func(TarokoCommon wr) {
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
      kkPgmId = wp.itemStr("kk_pgm_id");
      kkPgmParm = wp.itemStr("kk_pgm_parm");
    } else {
      kkPgmId = wp.itemStr("pgm_id");
      kkPgmParm = wp.itemStr("pgm_parm");
    }
    if (this.ibAdd)
      if (kkPgmId.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where pgm_id = ? "
            + " and   pgm_parm = ? ";
        Object[] param = new Object[] {kkPgmId, kkPgmParm};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[程式代碼:][程式參數:] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " pgm_id, " + " pgm_parm, "
        + " pgm_name, " + " pgm_seq, " + " ecs_table_name, " + " db2_table_name, " + " redo_flag, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?," + "sysdate,?,?)";

    Object[] param = new Object[] {kkPgmId, kkPgmParm, wp.itemStr("pgm_name"), wp.itemNum("pgm_seq"),
        wp.itemStr("ecs_table_name"), wp.itemStr("db2_table_name"), wp.itemStr("redo_flag"),
        wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "pgm_name = ?, " + "pgm_seq = ?, "
        + "ecs_table_name = ?, " + "db2_table_name = ?, " + "redo_flag = ?, " + "mod_user  = ?, "
        + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemStr("pgm_name"), wp.itemNum("pgm_seq"),
        wp.itemStr("ecs_table_name"), wp.itemStr("db2_table_name"), wp.itemStr("redo_flag"),
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
