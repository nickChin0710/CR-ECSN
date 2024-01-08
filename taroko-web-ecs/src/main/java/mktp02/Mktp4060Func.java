/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp4060Func extends busi.FuncProc {
  private String PROGNAME = "發卡/續卡/停卡參數維護處理程式108/09/03 V1.00.01";
  //String kk1;
  String approveTabName = "mkt_rept_par";
  String controlTabName = "mkt_rept_par_t";

  public Mktp4060Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " batch_no, " + " description, "
        + " filename_beg, " + " filename_end, " + " acct_type_sel, " + " group_code_sel, "
        + " source_code_sel, " + " carddate_sel, " + " issue_date_s, " + " issue_date_e, "
        + " change_date_s, " + " change_date_e, " + " stop_date_s, " + " stop_date_e, "
        + " apply_date_s, " + " apply_date_e, " + " subissue_date_s, " + " subissue_date_e, "
        + " reissue_date_s, " + " reissue_date_e, " + " text_form, " + " validate_card, "
        + " invalidate_card, " + " format_form, " + " zip_passwd, " + " apr_flag, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param =
        new Object[] {colStr("batch_no"), colStr("description"), colStr("filename_beg"),
            colStr("filename_end"), colStr("acct_type_sel"), colStr("group_code_sel"),
            colStr("source_code_sel"), colStr("carddate_sel"), colStr("issue_date_s"),
            colStr("issue_date_e"), colStr("change_date_s"), colStr("change_date_e"),
            colStr("stop_date_s"), colStr("stop_date_e"), colStr("apply_date_s"),
            colStr("apply_date_e"), colStr("subissue_date_s"), colStr("subissue_date_e"),
            colStr("reissue_date_s"), colStr("reissue_date_e"), colStr("text_form"),
            colStr("validate_card"), colStr("invalidate_card"), colStr("format_form"),
            colStr("zip_passwd"), "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"),
            wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " batch_no, " + " description, " + " filename_beg, " + " filename_end, "
        + " acct_type_sel, " + " group_code_sel, " + " source_code_sel, " + " carddate_sel, "
        + " issue_date_s, " + " issue_date_e, " + " change_date_s, " + " change_date_e, "
        + " stop_date_s, " + " stop_date_e, " + " apply_date_s, " + " apply_date_e, "
        + " subissue_date_s, " + " subissue_date_e, " + " reissue_date_s, " + " reissue_date_e, "
        + " text_form, " + " validate_card, " + " invalidate_card, " + " format_form, "
        + " zip_passwd, " + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
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
    strSql = "update " + approveTabName + " set " + "description = ?, " + "filename_beg = ?, "
        + "filename_end = ?, " + "acct_type_sel = ?, " + "group_code_sel = ?, "
        + "source_code_sel = ?, " + "carddate_sel = ?, " + "issue_date_s = ?, "
        + "issue_date_e = ?, " + "change_date_s = ?, " + "change_date_e = ?, " + "stop_date_s = ?, "
        + "stop_date_e = ?, " + "apply_date_s = ?, " + "apply_date_e = ?, "
        + "subissue_date_s = ?, " + "subissue_date_e = ?, " + "reissue_date_s = ?, "
        + "reissue_date_e = ?, " + "text_form = ?, " + "validate_card = ?, "
        + "invalidate_card = ?, " + "format_form = ?, " + "zip_passwd = ?, " + "crt_user  = ?, "
        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   batch_no  = ? ";

    Object[] param = new Object[] {colStr("description"), colStr("filename_beg"),
        colStr("filename_end"), colStr("acct_type_sel"), colStr("group_code_sel"),
        colStr("source_code_sel"), colStr("carddate_sel"), colStr("issue_date_s"),
        colStr("issue_date_e"), colStr("change_date_s"), colStr("change_date_e"),
        colStr("stop_date_s"), colStr("stop_date_e"), colStr("apply_date_s"),
        colStr("apply_date_e"), colStr("subissue_date_s"), colStr("subissue_date_e"),
        colStr("reissue_date_s"), colStr("reissue_date_e"), colStr("text_form"),
        colStr("validate_card"), colStr("invalidate_card"), colStr("format_form"),
        colStr("zip_passwd"), colStr("crt_user"), colStr("crt_date"), wp.loginUser, aprFlag,
        colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"), colStr("batch_no")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and batch_no = ? ";

    Object[] param = new Object[] {colStr("batch_no")};

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
  public int dbDeleteD4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_bn_data " + "where 1 = 1 " + "and table_name  =  'MKT_REPT_PAR' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("batch_no"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_bn_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_REPT_PAR' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("batch_no"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_bn_data " + "select * " + "from  mkt_bn_data_t " + "where 1 = 1 "
        + "and table_name  =  'MKT_REPT_PAR' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("batch_no"),};

    sqlExec(strSql, param);

    return 1;
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
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
