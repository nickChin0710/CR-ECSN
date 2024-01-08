/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/14  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6290Func extends busi.FuncProc {
  private String PROGNAME = "帳戶紅利點數線上調整作業處理程式108/10/14 V1.00.01";
  //String kk1;
  String approveTabName = "mkt_bonus_dtl";
  String controlTabName = "mkt_bonus_dtl_t";

  public Mktp6290Func(TarokoCommon wr) {
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
  public int dbInsertA4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    strSql = " insert into  " + approveTabName + " (" + " tran_seqno, " + " acct_type, "
        + " bonus_type, " + " active_code, " + " active_name, " + " tran_code, " + " beg_tran_bp, "
        + " tax_flag, " + " effect_e_date, " + " mod_reason, " + " mod_desc, " + " mod_memo, "
        + " tran_date, " + " tran_time, " + " p_seqno, " + " id_p_seqno, " + " tran_pgm, "
        + " end_tran_bp, " + " acct_date, " + " apr_flag, " + " apr_date, " + " apr_user, "
        + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, "
        + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("tran_seqno"), colStr("acct_type"), colStr("bonus_type"),
        colStr("active_code"), colStr("active_name"), colStr("tran_code"), colStr("beg_tran_bp"),
        colStr("tax_flag"), colStr("effect_e_date"), colStr("mod_reason"), colStr("mod_desc"),
        colStr("mod_memo"), colStr("p_seqno"), colStr("id_p_seqno"), colStr("tran_pgm"),
        colStr("end_tran_bp"), comr.getBusinDate(), "Y", wp.loginUser, colStr("crt_date"),
        colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"),
        wp.modPgm()};

    sqlExec(strSql, param);

    busi.ecs.MktBonus comc = new busi.ecs.MktBonus();
    comc.setConn(wp);
    comc.bonusFunc(colStr("tran_seqno"));

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " tran_seqno, " + " acct_type, " + " bonus_type, " + " active_code, "
        + " active_name, " + " tran_code, " + " beg_tran_bp, " + " tax_flag, " + " effect_e_date, "
        + " mod_reason, " + " mod_desc, " + " mod_memo, " + " tran_date, " + " tran_time, "
        + " p_seqno, " + " id_p_seqno, " + " tran_pgm, " + " end_tran_bp, " + " acct_date, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "acct_type = ?, " + "bonus_type = ?, "
        + "active_code = ?, " + "active_name = ?, " + "tran_code = ?, " + "beg_tran_bp = ?, "
        + "tax_flag = ?, " + "effect_e_date = ?, " + "mod_reason = ?, " + "mod_desc = ?, "
        + "mod_memo = ?, " + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   tran_seqno  = ? ";

    Object[] param = new Object[] {colStr("acct_type"), colStr("bonus_type"), colStr("active_code"),
        colStr("active_name"), colStr("tran_code"), colStr("beg_tran_bp"), colStr("tax_flag"),
        colStr("effect_e_date"), colStr("mod_reason"), colStr("mod_desc"), colStr("mod_memo"),
        colStr("crt_user"), colStr("crt_date"), wp.loginUser, aprFlag, colStr("mod_user"),
        colStr("mod_time"), colStr("mod_pgm"), colStr("tran_seqno")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and tran_seqno = ? ";

    Object[] param = new Object[] {colStr("tran_seqno")};

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

}  // End of class
