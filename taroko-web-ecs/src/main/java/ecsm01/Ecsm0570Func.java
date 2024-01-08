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
public class Ecsm0570Func extends FuncEdit
{
  private String progname = "轉檔執行紀錄檔處理程式108/01/29 V1.00.01";
  String kkExecuteType, kk2, kk3;
  String controlTabName = "sys_db2_exelog";

  public Ecsm0570Func(TarokoCommon wr) {
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
      kkExecuteType = wp.itemStr("kk_execute_type");
      kk2 = wp.itemStr("kk_execute_name");
      kk3 = wp.itemStr("kk_execute_seq");
    } else {
      kkExecuteType = wp.itemStr("execute_type");
      kk2 = wp.itemStr("execute_name");
      kk3 = wp.itemStr("execute_seq");
    }
    if (this.ibAdd)
      if (kkExecuteType.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where execute_type = ? "
            + " and   execute_name = ? " + " and   execute_seq = ? ";
        Object[] param = new Object[] {kkExecuteType, kk2, kk3};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[轉檔類別:][資料代碼:][執行順序:] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " execute_type, " + " execute_name, "
        + " execute_seq, " + " execute_cond, " + " create_flag, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?,?," + "sysdate,?,?)";

    Object[] param = new Object[] {kkExecuteType, kk2, kk3, wp.itemStr("execute_cond"),
        wp.itemStr("create_flag"), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "execute_cond = ?, " + "create_flag = ?, "
        + "mod_user  = ?, " + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? ";

    Object[] param = new Object[] {wp.itemStr("execute_cond"), wp.itemStr("create_flag"),
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
