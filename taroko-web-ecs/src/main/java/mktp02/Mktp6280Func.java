/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6280Func extends busi.FuncProc {
  private String PROGNAME = "帳戶基金(現金回饋)明細檔覆核處理程式108/10/22 V1.00.01";
  //String kk1;
  String approveTabName = "mkt_cashback_dtl";
  String controlTabName = "mkt_cashback_dtl_t";

  public Mktp6280Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " tran_seqno, " + " fund_code, "
        + " fund_name, " + " acct_type, " + " tran_code, " + " beg_tran_amt, " + " effect_e_date, "
        + " mod_reason, " + " mod_desc, " + " mod_memo, " + " tran_date, " + " tran_time, "
        + " tran_pgm, " + " p_seqno, " + " id_p_seqno, " + " end_tran_amt, " + " acct_date, "
        + " acct_month, " + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("tran_seqno"), colStr("fund_code"), colStr("fund_name"),
        colStr("acct_type"), colStr("tran_code"), colStr("beg_tran_amt"), colStr("effect_e_date"),
        colStr("mod_reason"), colStr("mod_desc"), colStr("mod_memo"), colStr("tran_pgm"),
        colStr("p_seqno"), colStr("id_p_seqno"), colStr("end_tran_amt"), comr.getBusinDate(),
        comr.getBusinDate().substring(0, 6), "Y", wp.loginUser, colStr("crt_date"),
        colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"),
        wp.modPgm()};

    sqlExec(strSql, param);
    busi.ecs.MktCashback comc = new busi.ecs.MktCashback();
    comc.setConn(wp);
    comc.cashbackFunc(colStr("tran_seqno"));

//    if (colNum("end_tran_amt") > 0)
//      insertCycFundDtl(0);
//    else
//      insertCycFundDtl(1);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " tran_seqno, " + " fund_code, " + " fund_name, " + " acct_type, "
        + " tran_code, " + " beg_tran_amt, " + " effect_e_date, " + " mod_reason, " + " mod_desc, "
        + " mod_memo, " + " tran_date, " + " tran_time, " + " tran_pgm, " + " p_seqno, "
        + " id_p_seqno, " + " end_tran_amt, " + " acct_date, " + " acct_month, " + " apr_date, "
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
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "fund_code = ?, " + "fund_name = ?, "
        + "acct_type = ?, " + "tran_code = ?, " + "beg_tran_amt = ?, " + "effect_e_date = ?, "
        + "mod_reason = ?, " + "mod_desc = ?, " + "mod_memo = ?, " + "crt_user  = ?, "
        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   tran_seqno  = ? ";

    Object[] param = new Object[] {colStr("fund_code"), colStr("fund_name"), colStr("acct_type"),
        colStr("tran_code"), colStr("beg_tran_amt"), colStr("effect_e_date"), colStr("mod_reason"),
        colStr("mod_desc"), colStr("mod_memo"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, apr_flag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("tran_seqno")};

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
//  public int insertCycFundDtl(int cdType) {
//
//    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
//    comr.setConn(wp);
//
//    if (cdType == 0)
//      colSet("cd_kind", "H001");
//    else
//      colSet("cd_kind", "H003");
//
//    strSql = " insert into cyc_fund_dtl (" + " business_date, " + " create_date, "
//        + " create_time, " + " id_p_seqno, " + " p_seqno, " + " acct_type, " + " fund_code, "
//        + " tran_code, " + " vouch_type, " + " cd_kind, " + " memo1_type, " + " fund_amt, "
//        + " other_amt, " + " proc_flag, " + " proc_date, " + " execute_date, " + " fund_cnt, "
//        + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
//        + "?,to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss')," + "?,?,?,?,?,?,?,?,"
//        + "?,?,?,?,?,?," + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";
//
//    Object[] param = new Object[] {comr.getBusinDate(), colStr("id_p_seqno"), colStr("p_seqno"),
//        colStr("acct_type"), colStr("fund_code"), colStr("tran_code"), "3", colStr("cd_kind"), "1",
//        Math.abs(colNum("end_tran_amt")), 0, "N", "", comr.getBusinDate(), 1,
//        wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};
//
//    sqlExec(strSql, param);
//    if (sqlRowNum <= 0) {
//      errmsg(sqlErrtext);
//      return (0);
//    }
//
//    return (1);
//  }

  // ************************************************************************

}  // End of class
