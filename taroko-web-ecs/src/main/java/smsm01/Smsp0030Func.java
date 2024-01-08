/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei       updated for project coding standard
*                                                                          *
***************************************************************************/
package smsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsp0030Func extends busi.FuncProc {
  private String PROGNAME = "簡訊內容明細檔覆核處理程式108/11/22 V1.00.01";
 // String kk1, kk2;
  String approveTabName = "sms_msg_dtl";
  String controlTabName = "sms_msg_dtl_t";

  public Smsp0030Func(TarokoCommon wr) {
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
  public void dataCheck() {}

  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }

  // ************************************************************************
  public int dbInsertA4() throws Exception {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + approveTabName + " (" + " msg_seqno, " + " cellar_phone, "
        + " chi_name, " + " msg_dept, " + " msg_userid, " + " msg_id, " + " msg_desc, "
        + " chi_name_flag, " + " id_p_seqno, " + " add_mode, " + " cellphone_check_flag, "
        + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("msg_seqno"), colStr("cellar_phone"), colStr("chi_name"),
        colStr("msg_dept"), colStr("msg_userid"), colStr("msg_id"), colStr("msg_desc"),
        colStr("chi_name_flag"), colStr("id_p_seqno"), colStr("add_mode"),
        colStr("cellphone_check_flag"), "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"),
        wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " msg_seqno, " + " cellar_phone, " + " chi_name, " + " msg_dept, "
        + " msg_userid, " + " msg_id, " + " msg_desc, " + " chi_name_flag, " + " id_p_seqno, "
        + " add_mode, " + " cellphone_check_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() throws Exception {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "cellar_phone = ?, " + "chi_name = ?, "
        + "msg_dept = ?, " + "msg_userid = ?, " + "msg_id = ?, " + "msg_desc = ?, "
        + "chi_name_flag = ?, " + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   id_no  = ? "
        + "and   msg_seqno  = ? ";

    Object[] param = new Object[] {colStr("cellar_phone"), colStr("chi_name"), colStr("msg_dept"),
        colStr("msg_userid"), colStr("msg_id"), colStr("msg_desc"), colStr("chi_name_flag"),
        colStr("crt_user"), colStr("crt_date"), wp.loginUser, apr_flag, colStr("mod_user"),
        colStr("mod_time"), colStr("mod_pgm"), colStr("id_no"), colStr("msg_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and id_no = ? "
        + "and msg_seqno = ? ";

    Object[] param = new Object[] {colStr("id_no"), colStr("msg_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDelete() {
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

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

} // End of class
