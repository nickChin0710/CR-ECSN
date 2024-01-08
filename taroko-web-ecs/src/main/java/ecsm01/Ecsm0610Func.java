/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/09/07  V1.00.01   Justin        Initial                              *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import busi.SqlPrepare;

import java.util.*;


import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0610Func extends FuncEdit {
  private static final String PROG_NAME = "Ecsm0610";
private String progname = "批次排程及執行結果查詢 110/09/07 V1.00.01";

  public Ecsm0610Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
	 return -1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    
    SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("ECS_BATCH_CTL");
    sp.addsqlParm(", wait_flag = ? ", wp.itemStr("wait_flag"));
    sp.addsqlParm(", rerun_proc = ? ", wp.itemStr("rerun_proc"));
    sp.addsqlParm(", key_parm1 = ? ", wp.itemStr("key_parm1"));
    sp.addsqlParm(", key_parm2 = ? ", wp.itemStr("key_parm2"));
    sp.addsqlParm(", key_parm3 = ? ", wp.itemStr("key_parm3"));
    sp.addsqlParm(", key_parm4 = ? ", wp.itemStr("key_parm4"));
    sp.addsqlParm(", key_parm5 = ? ", wp.itemStr("key_parm5"));
    sp.addsqlParm(", mod_user = ? ", wp.loginUser);
    sp.addsqlParm(", mod_pgm = ? ", PROG_NAME);
    sp.addsql(", mod_time = sysdate ");
    sp.rowid2Where(wp.itemStr("rowid"));
    sp.sql2Where(" and decode(MOD_TIME, NULL , '' , to_char(MOD_TIME , 'YYYY/MM/DD HH24:MI:SS')) = ? ", wp.itemStr("mod_time"));

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0)
      errmsg("更新 ECS_BATCH_CTL 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
//    actionInit("D");
//    dataCheck();
//    if (rc != 1)
//      return rc;
//
//    strSql = "delete ECS_BATCH_CTL where rowid = ? and decode(MOD_TIME, NULL , '' , to_char(MOD_TIME , 'YYYY/MM/DD HH24:MI:SS')) = ? ";
//
//    Object[] param = new Object[] {wp.itemRowId("rowid"), wp.itemStr("mod_time")};
//
//    rc = sqlExec(strSql, param);
//    if (sqlRowNum <= 0)
//      errmsg("刪除 ECS_BATCH_CTL 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
