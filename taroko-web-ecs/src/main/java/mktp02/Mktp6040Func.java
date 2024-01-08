/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/14  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6040Func extends busi.FuncProc {
  private String PROGNAME = "專案免年費參數覆核作業處理程式108/05/14 V1.00.01";
  //String kk1;
  String approveTabName = "cyc_anul_project";
  String controlTabName = "cyc_anul_project_t";

  public Mktp6040Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " project_no, " + " project_name, "
        + " acct_type_sel, " + " group_code_sel, " + " source_code_sel, " + " recv_month_tag, "
        + " recv_s_date, " + " recv_e_date, " + " issue_date_tag, " + " issue_date_s, "
        + " issue_date_e, " + " mcard_cond, " + " scard_cond, " + " cnt_months_tag, "
        + " cnt_months, " + " accumulate_cnt, " + " average_amt, " + " cnt_bl_flag, "
        + " cnt_ca_flag, " + " cnt_it_flag, " + " cnt_ao_flag, " + " cnt_id_flag, "
        + " cnt_ot_flag, " + " amt_months_tag, " + " amt_months, " + " accumulate_amt, "
        + " amt_bl_flag, " + " amt_ca_flag, " + " amt_it_flag, " + " amt_ao_flag, "
        + " amt_id_flag, " + " amt_ot_flag, " + " adv_months_tag, " + " adv_months, " + " adv_cnt, "
        + " adv_amt, " + " mcode, " + " free_fee_cnt, " + " apr_flag, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("project_no"), colStr("project_name"),
        colStr("acct_type_sel"), colStr("group_code_sel"), colStr("source_code_sel"),
        colStr("recv_month_tag"), colStr("recv_s_date"), colStr("recv_e_date"),
        colStr("issue_date_tag"), colStr("issue_date_s"), colStr("issue_date_e"),
        colStr("mcard_cond"), colStr("scard_cond"), colStr("cnt_months_tag"), colStr("cnt_months"),
        colStr("accumulate_cnt"), colStr("average_amt"), colStr("cnt_bl_flag"),
        colStr("cnt_ca_flag"), colStr("cnt_it_flag"), colStr("cnt_ao_flag"), colStr("cnt_id_flag"),
        colStr("cnt_ot_flag"), colStr("amt_months_tag"), colStr("amt_months"),
        colStr("accumulate_amt"), colStr("amt_bl_flag"), colStr("amt_ca_flag"),
        colStr("amt_it_flag"), colStr("amt_ao_flag"), colStr("amt_id_flag"), colStr("amt_ot_flag"),
        colStr("adv_months_tag"), colStr("adv_months"), colStr("adv_cnt"), colStr("adv_amt"),
        colStr("mcode"), colStr("free_fee_cnt"), "Y", wp.loginUser, colStr("crt_date"),
        colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"),
        wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql =
        " select " + " project_no, " + " project_name, " + " acct_type_sel, " + " group_code_sel, "
            + " source_code_sel, " + " recv_month_tag, " + " recv_s_date, " + " recv_e_date, "
            + " issue_date_tag, " + " issue_date_s, " + " issue_date_e, " + " mcard_cond, "
            + " scard_cond, " + " cnt_months_tag, " + " cnt_months, " + " accumulate_cnt, "
            + " average_amt, " + " cnt_bl_flag, " + " cnt_ca_flag, " + " cnt_it_flag, "
            + " cnt_ao_flag, " + " cnt_id_flag, " + " cnt_ot_flag, " + " amt_months_tag, "
            + " amt_months, " + " accumulate_amt, " + " amt_bl_flag, " + " amt_ca_flag, "
            + " amt_it_flag, " + " amt_ao_flag, " + " amt_id_flag, " + " amt_ot_flag, "
            + " adv_months_tag, " + " adv_months, " + " adv_cnt, " + " adv_amt, " + " mcode, "
            + " free_fee_cnt, " + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
            + " from " + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + procTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "project_name = ?, " + "acct_type_sel = ?, "
        + "group_code_sel = ?, " + "source_code_sel = ?, " + "recv_month_tag = ?, "
        + "recv_s_date = ?, " + "recv_e_date = ?, " + "issue_date_tag = ?, " + "issue_date_s = ?, "
        + "issue_date_e = ?, " + "mcard_cond = ?, " + "scard_cond = ?, " + "cnt_months_tag = ?, "
        + "cnt_months = ?, " + "accumulate_cnt = ?, " + "average_amt = ?, " + "cnt_bl_flag = ?, "
        + "cnt_ca_flag = ?, " + "cnt_it_flag = ?, " + "cnt_ao_flag = ?, " + "cnt_id_flag = ?, "
        + "cnt_ot_flag = ?, " + "amt_months_tag = ?, " + "amt_months = ?, " + "accumulate_amt = ?, "
        + "amt_bl_flag = ?, " + "amt_ca_flag = ?, " + "amt_it_flag = ?, " + "amt_ao_flag = ?, "
        + "amt_id_flag = ?, " + "amt_ot_flag = ?, " + "adv_months_tag = ?, " + "adv_months = ?, "
        + "adv_cnt = ?, " + "adv_amt = ?, " + "mcode = ?, " + "free_fee_cnt = ?, "
        + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   project_no  = ? ";

    Object[] param = new Object[] {colStr("project_name"), colStr("acct_type_sel"),
        colStr("group_code_sel"), colStr("source_code_sel"), colStr("recv_month_tag"),
        colStr("recv_s_date"), colStr("recv_e_date"), colStr("issue_date_tag"),
        colStr("issue_date_s"), colStr("issue_date_e"), colStr("mcard_cond"), colStr("scard_cond"),
        colStr("cnt_months_tag"), colStr("cnt_months"), colStr("accumulate_cnt"),
        colStr("average_amt"), colStr("cnt_bl_flag"), colStr("cnt_ca_flag"), colStr("cnt_it_flag"),
        colStr("cnt_ao_flag"), colStr("cnt_id_flag"), colStr("cnt_ot_flag"),
        colStr("amt_months_tag"), colStr("amt_months"), colStr("accumulate_amt"),
        colStr("amt_bl_flag"), colStr("amt_ca_flag"), colStr("amt_it_flag"), colStr("amt_ao_flag"),
        colStr("amt_id_flag"), colStr("amt_ot_flag"), colStr("adv_months_tag"),
        colStr("adv_months"), colStr("adv_cnt"), colStr("adv_amt"), colStr("mcode"),
        colStr("free_fee_cnt"), colStr("crt_user"), colStr("crt_date"), wp.loginUser, apr_flag,
        colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"), colStr("project_no")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and project_no = ? ";

    Object[] param = new Object[] {colStr("project_no")};

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
    strSql = "delete cyc_bn_data " + "where 1 = 1 " + "and table_name  =  'CYC_ANUL_PROJECT' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("project_no"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 cyc_bn_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete cyc_bn_data_t " + "where 1 = 1 " + "and table_name  =  'CYC_ANUL_PROJECT' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("project_no"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 cyc_bn_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into cyc_bn_data " + "select * " + "from  cyc_bn_data_t " + "where 1 = 1 "
        + "and table_name  =  'CYC_ANUL_PROJECT' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("project_no"),};

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

} // End of class
