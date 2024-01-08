/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/02/12  V1.00.01   Ray Ho        Initial                              *
* 109-04-27  V1.00.02   yanghan       修改了變量名稱和方法名稱                             * 
* 112/07/13  V1.00.03   JeffKung      ecs_ftp_log沒有mod_user欄位,修改bug   *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0210Func extends FuncEdit {
  private String progname = "檔案傳輸紀錄檔維護處理程式112/07/13 V1.00.01";
  String controlTabName = "ecs_ftp_log";

  public Ecsm0210Func(TarokoCommon wr) {
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
    } else {
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


    strSql = " insert into  " + controlTabName + " (" + " trans_seqno, " + " file_name, "
        + " file_date, " + " trans_resp_code, " + " proc_code, " + " group_id, " + " source_from, "
        + " crt_date, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + "sysdate,?,?)";

    Object[] param =
        new Object[] {wp.itemStr("trans_seqno"), wp.itemStr("file_name"), wp.itemStr("file_date"),
            wp.itemStr("trans_resp_code"), wp.itemStr("proc_code"), wp.itemStr("group_id"),
            wp.itemStr("source_from"), wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "trans_seqno = ?, " + "file_name = ?, "
        + "file_date = ?, " + "trans_resp_code = ?, " + "proc_code = ?, " + "group_id = ?, "
        + "source_from = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, "
        + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("trans_seqno"), wp.itemStr("file_name"),
        wp.itemStr("file_date"), wp.itemStr("trans_resp_code"), wp.itemStr("proc_code"),
        wp.itemStr("group_id"), wp.itemStr("source_from"), wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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
