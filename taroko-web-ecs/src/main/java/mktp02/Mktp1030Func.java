/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/07  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard  
* 111-08-03  V1.00.03   machao       更新覆核過程中數據庫存儲不完整bug     *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1030Func extends busi.FuncProc {
  private String PROGNAME = "高鐵車廂請款明細檔覆核作業處理程式108/11/07 V1.00.01";
 // String kk1;
  String approveTabName = "mkt_thsr_uptxn";
  String controlTabName = "mkt_thsr_uptxn_t";

  public Mktp1030Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " ( " + " file_date, " + " file_time, " + " file_name, " + " serial_no, " + " pay_cardid, "
        + " auth_flag, " + " error_desc, " + " trans_date, " + " trans_time, " + " card_no, "
        + " authentication_code, " + " proc_flag, " + " major_id_p_seqno, " + " major_card_no, "
        + " id_p_seqno, " + " p_seqno, " + " acct_type, " + " group_code, " + " card_type, "
        + " card_mode, " + " crt_time, " + " issue_date, " + " promote_emp_no, " + " trans_type, " + " corp_no, "
        + " reg_bank_no, " + " apr_flag, " 
        + " station_id, " + " pnr, " + " ticket_id, " + " trans_amount, " + " train_no, " + " departure_station_id, " + " arrival_station_id, " + " seat_no, "
        + " depart_date, " + " issue_station_id, " + " deduct_bp, " + " pay_type, " + "proc_date, " + " error_code, "
        + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values ( " + " ?,?,?," + "?,?,?,?,?,?,?,?," + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] { colStr("file_date"),colStr("file_time"),colStr("file_name"), colStr("serial_no"), colStr("pay_cardid"), colStr("auth_flag"),
        colStr("error_desc"), colStr("trans_date"), colStr("trans_time"), colStr("card_no"),
        colStr("authentication_code"), colStr("proc_flag"), colStr("major_id_p_seqno"),
        colStr("major_card_no"), colStr("id_p_seqno"), colStr("p_seqno"), colStr("acct_type"),
        colStr("group_code"), colStr("card_type"), colStr("card_mode"), colStr("crt_time"),
        colStr("issue_date"), colStr("promote_emp_no"),colStr("trans_type"), colStr("corp_no"), colStr("reg_bank_no"),
        "Y", 
        colStr("station_id"),colStr("pnr"),colStr("ticket_id"),colStr("trans_amount"),colStr("train_no"),colStr("departure_station_id"),colStr("arrival_station_id"),
        colStr("seat_no"),colStr("depart_date"),colStr("issue_station_id"),colStr("deduct_bp"),colStr("pay_type"),colStr("proc_date"),colStr("error_code"),
        wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime,
        wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " +  " file_date, " + " file_time, " + " file_name, " +  " serial_no, " + " pay_cardid, " + " auth_flag, " + " error_desc, "
        + " trans_date, " + " trans_time, " + " card_no, " + " authentication_code, "
        + " proc_flag, " + " major_id_p_seqno, " + " major_card_no, " + " id_p_seqno, "
        + " p_seqno, " + " acct_type, " + " group_code, " + " card_type, " + " card_mode, "
        + " crt_time, " + " issue_date, " + " promote_emp_no, " + " trans_type, " + " corp_no, " + " reg_bank_no, "
        + " station_id, " + " pnr, " + " ticket_id, " + " trans_amount, " + " train_no, " + " departure_station_id, " + " arrival_station_id, " + " seat_no, "
        + " depart_date, " + " issue_station_id, " + " deduct_bp, " + " pay_type, " + "proc_date, " + " error_code, "
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
  public int dbUpdate_U4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    if (colStr("auth_flag").equals("Y"))
      colSet("proc_flag", "1");

    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "auth_flag = ?, " + "error_desc = ?, "
        + "trans_date = ?, " + "trans_time = ?, " + "card_no = ?, " + "authentication_code = ?, "
        + "proc_flag = '0', " + "major_id_p_seqno = ?, " + "major_card_no = ?, "
        + "id_p_seqno = ?, " + "p_seqno = ?, " + "acct_type = ?, " + "group_code = ?, "
        + "card_type = ?, " + "card_mode = ?, " + "crt_time = ?, " + "issue_date = ?, "
        + "promote_emp_no = ?, " + "corp_no = ?, " + "reg_bank_no = ?, " + "crt_user  = ?, "
        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   serial_no  = ? ";

    Object[] param = new Object[] {colStr("auth_flag"), colStr("error_desc"), colStr("trans_date"),
        colStr("trans_time"), colStr("card_no"), colStr("authentication_code"),
        colStr("major_id_p_seqno"), colStr("major_card_no"), colStr("id_p_seqno"),
        colStr("p_seqno"), colStr("acct_type"), colStr("group_code"), colStr("card_type"),
        colStr("card_mode"), colStr("crt_time"), colStr("issue_date"), colStr("promote_emp_no"),
        colStr("corp_no"), colStr("reg_bank_no"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, aprFlag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("serial_no")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and serial_no = ? ";

    Object[] param = new Object[] {colStr("serial_no")};

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
