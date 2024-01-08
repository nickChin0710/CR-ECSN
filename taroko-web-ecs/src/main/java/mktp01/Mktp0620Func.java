/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0620Func extends busi.FuncProc {
  private String PROGNAME = "網路辦卡專案人工登錄覆核作業處理程式108/09/03 V1.00.01";
//  String kk1;
  String approveTabName = "web_apply_idno";
  String controlTabName = "web_apply_idno_t";

  public Mktp0620Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " record_seqno, " + " project_no, "
        + " id_no, " + " record_date, " + " chi_name, " + " from_mark, " + " birthday, " + " sex, "
        + " office_area_code, " + " office_tel_no, " + " office_tel_ext, " + " home_area_code, "
        + " home_tel_no, " + " home_tel_ext, " + " cellar_phone, " + " e_mail_addr, "
        + " apply_type, " + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("record_seqno"), colStr("project_no"), colStr("id_no"),
        colStr("record_date"), colStr("chi_name"), colStr("from_mark"), colStr("birthday"),
        colStr("sex"), colStr("office_area_code"), colStr("office_tel_no"),
        colStr("office_tel_ext"), colStr("home_area_code"), colStr("home_tel_no"),
        colStr("home_tel_ext"), colStr("cellar_phone"), colStr("e_mail_addr"), colStr("apply_type"),
        "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime,
        wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " record_seqno, " + " project_no, " + " id_no, " + " record_date, "
        + " chi_name, " + " from_mark, " + " birthday, " + " sex, " + " office_area_code, "
        + " office_tel_no, " + " office_tel_ext, " + " home_area_code, " + " home_tel_no, "
        + " home_tel_ext, " + " cellar_phone, " + " e_mail_addr, " + " apply_type, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, "
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
    strSql = "update " + approveTabName + " set " + "project_no = ?, " + "id_no = ?, "
        + "record_date = ?, " + "chi_name = ?, " + "from_mark = ?, " + "birthday = ?, "
        + "sex = ?, " + "office_area_code = ?, " + "office_tel_no = ?, " + "office_tel_ext = ?, "
        + "home_area_code = ?, " + "home_tel_no = ?, " + "home_tel_ext = ?, " + "cellar_phone = ?, "
        + "e_mail_addr = ?, " + "apply_type = ?, " + "crt_user  = ?, " + "crt_date  = ?, "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
        + "and   record_seqno  = ? ";

    Object[] param = new Object[] {colStr("project_no"), colStr("id_no"), colStr("record_date"),
        colStr("chi_name"), colStr("from_mark"), colStr("birthday"), colStr("sex"),
        colStr("office_area_code"), colStr("office_tel_no"), colStr("office_tel_ext"),
        colStr("home_area_code"), colStr("home_tel_no"), colStr("home_tel_ext"),
        colStr("cellar_phone"), colStr("e_mail_addr"), colStr("apply_type"), colStr("crt_user"),
        colStr("crt_date"), wp.loginUser, aprFlag, colStr("mod_user"), colStr("mod_time"),
        colStr("mod_pgm"), colStr("record_seqno")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and record_seqno = ? ";

    Object[] param = new Object[] {colStr("record_seqno")};

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
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
