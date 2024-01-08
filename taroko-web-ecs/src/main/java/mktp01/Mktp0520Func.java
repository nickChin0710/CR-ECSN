/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-01-02 V1.00.01  Machao        Initial                              *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0520Func extends busi.FuncProc {
  private String PROGNAME = "推廣人員推廣人員資料覆核2023/01/03 V1.00.01";
  //String kk1, kk2;
  String approveTabName = "crd_employee_a";
  String controlTabName = "crd_employee_a_t";

  public Mktp0520Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" 
    		+ "employ_no, "
            + "chi_name, "
            + "id, "
            + "id_code, "
            + "acct_no, "
            + "unit_no, "
            + "unit_name, "
            + "subunit_no, "
            + "subunit_name, "
            + "position_id, "
            + "position_name, "
            + "status_id, "
            + "status_name, "
            + "file_name, "
            + "corp_no, "
            + "subsidiary_no, "
            + "aud_type, "
            + "description, "
            + "apr_date, "
            + "apr_user, "
            + "apr_flag, "
            + "crt_date, "
            + "crt_user, "
            + "mod_user, "
            + "mod_time, "
            + "mod_pgm"
            + ") " + 
            "values("
            + "?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, "
            + "'', '', "
            + "to_char(sysdate, 'yyyymmdd'), ?, 'Y', ?, ?, ?, sysdate,'mktp0520' "
            + ")";

    Object[] param = new Object[] {
            colStr("employ_no"), 
            colStr("chi_name"), 
            colStr("id"), 
            colStr("id_code"), 
            colStr("acct_no"), 
            colStr("unit_no"), 
            colStr("unit_name"), 
            colStr("subunit_no"), 
            colStr("subunit_name"), 
            colStr("position_id"), 
            colStr("position_name"), 
            colStr("status_id"), 
            colStr("status_name"), 
            colStr("file_name"), 
            colStr("corp_no"), 
            colStr("subsidiary_no"), 
            wp.loginUser, 
            colStr("crt_date"),
            colStr("crt_user"),
            wp.loginUser
            };

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + "corp_no, "
    		+ "aud_type, "
            + "subsidiary_no, "
            + "employ_no, "
            + "chi_name, "
            + "id, "
            + "acct_no, "
            + "unit_no, "
            + "unit_name, "
            + "subunit_no, "
            + "subunit_name, "
            + "position_id, "
            + "position_name, "
            + "status_id, "
            + "status_name, "
            + "description, "
            + "file_name, "
            + "apr_user, "
            + "apr_flag, "
            + "apr_date, "
            + "crt_user, "
            + "crt_date, "
            + "mod_user, "
            + "mod_time" 
            + " from "
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
    strSql = "update " + approveTabName + " set " 
    		+ "employ_no = ?,  "
            + "chi_name = ?,  "
            + "id = ?,  "
            + "id_code = ?,  "
            + "acct_no = ?,  "
            + "unit_no = ?,  "
            + "unit_name = ?,  "
            + "subunit_no = ?,  "
            + "subunit_name = ?,  "
            + "position_id = ?,  "
            + "position_name = ?,  "
            + "status_id = ?,  "
            + "status_name = ?,  "
            + "file_name = ?,  "
            + "corp_no = ?,  "
            + "subsidiary_no = ?,  "
            + "aud_type = '',  "
            + "description = '',  "
            + "apr_date = to_char(sysdate,'yyyymmdd'), "
            + "apr_user = ?, "
            + "apr_flag = ?, "
            + "crt_date = ?, "
            + "crt_user = ?, "
            + "mod_user = ?, "
            + "mod_time = timestamp_format(?,'yyyymmdd'), "
            + "mod_pgm =? "
    ;

    Object[] param = new Object[] {
            colStr("employ_no"), 
            colStr("chi_name"), 
            colStr("id"), 
            colStr("id_code"), 
            colStr("acct_no"), 
            colStr("unit_no"), 
            colStr("unit_name"), 
            colStr("subunit_no"), 
            colStr("subunit_name"), 
            colStr("position_id"), 
            colStr("position_name"), 
            colStr("status_id"), 
            colStr("status_name"), 
            colStr("file_name"), 
            colStr("corp_no"), 
            colStr("subsidiary_no"), 
            wp.loginUser, 
            colStr("crt_date"),
            colStr("crt_user"),
            wp.loginUser
            };

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
    strSql = "delete " + approveTabName + " "  + "where 1 = 1 " + "and corp_no = ? "
            + "and employ_no = ? " + "and chi_name = ? " + "and subunit_no = ? "
            + "and status_id = ? ";

        Object[] param = new Object[] {colStr("corp_no"), colStr("employ_no"), colStr("chi_name"),
            colStr("subunit_no"), colStr("status_id")};

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
  public int dbDeleteD4T() {
    strSql = "delete " + controlTabName + " " + "where 1 = 1 " + "and corp_no = ? "
        + "and employ_no = ? " + "and chi_name = ? " + "and subunit_no = ? "
        + "and status_id = ? ";

    Object[] param = new Object[] {colStr("corp_no"), colStr("employ_no"), colStr("chi_name"),
        colStr("subunit_no"), colStr("status_id")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
