/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/20  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6020Func extends busi.FuncProc {
  private String PROGNAME = "高階卡友參數維護處理程式108/08/20 V1.00.01";
//  /String kk1, kk2;
  String approveTabName = "cyc_anul_gp";
  String controlTabName = "cyc_anul_gp_t";

  public Mktp6020Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " group_code, " + " card_type, "
        + " card_fee, " + " sup_card_fee, " + " mer_cond, " + " mer_bl_flag, " + " mer_ca_flag, "
        + " mer_it_flag, " + " mer_ao_flag, " + " mer_id_flag, " + " mer_ot_flag, "
        + " major_flag, " + " sub_flag, " + " major_sub, " + " a_merchant_sel, "
        + " a_mcht_group_sel, " + " cnt_cond, " + " cnt_select, " + " month_cnt, "
        + " accumlate_cnt, " + " cnt_bl_flag, " + " cnt_ca_flag, " + " cnt_it_flag, "
        + " cnt_ao_flag, " + " cnt_id_flag, " + " cnt_ot_flag, " + " cnt_major_flag, "
        + " cnt_sub_flag, " + " cnt_major_sub, " + " b_mcc_code_sel, " + " b_merchant_sel, "
        + " b_mcht_group_sel, " + " amt_cond, " + " accumlate_amt, " + " amt_bl_flag, "
        + " amt_ca_flag, " + " amt_it_flag, " + " amt_ao_flag, " + " amt_id_flag, "
        + " amt_ot_flag, " + " amt_major_flag, " + " amt_sub_flag, " + " amt_major_sub, "
        + " c_mcc_code_sel, " + " c_merchant_sel, " + " c_mcht_group_sel, " + " mcode, "
        + " email_nopaper_flag, " 
        + " miner_half_flag, " + " g_cond_flag, " + " g_accumlate_amt, " + " h_cond_flag, " + " h_accumlate_amt, "
        + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("group_code"), colStr("card_type"), colStr("card_fee"),
        colStr("sup_card_fee"), colStr("mer_cond"), colStr("mer_bl_flag"), colStr("mer_ca_flag"),
        colStr("mer_it_flag"), colStr("mer_ao_flag"), colStr("mer_id_flag"), colStr("mer_ot_flag"),
        colStr("major_flag"), colStr("sub_flag"), colStr("major_sub"), colStr("a_merchant_sel"),
        colStr("a_mcht_group_sel"), colStr("cnt_cond"), colStr("cnt_select"), colStr("month_cnt"),
        colStr("accumlate_cnt"), colStr("cnt_bl_flag"), colStr("cnt_ca_flag"),
        colStr("cnt_it_flag"), colStr("cnt_ao_flag"), colStr("cnt_id_flag"), colStr("cnt_ot_flag"),
        colStr("cnt_major_flag"), colStr("cnt_sub_flag"), colStr("cnt_major_sub"),
        colStr("b_mcc_code_sel"), colStr("b_merchant_sel"), colStr("b_mcht_group_sel"),
        colStr("amt_cond"), colStr("accumlate_amt"), colStr("amt_bl_flag"), colStr("amt_ca_flag"),
        colStr("amt_it_flag"), colStr("amt_ao_flag"), colStr("amt_id_flag"), colStr("amt_ot_flag"),
        colStr("amt_major_flag"), colStr("amt_sub_flag"), colStr("amt_major_sub"),
        colStr("c_mcc_code_sel"), colStr("c_merchant_sel"), colStr("c_mcht_group_sel"),
        colStr("mcode"), colStr("email_nopaper_flag"), 
        colStr("miner_half_flag").equals("Y")?"Y":"N",
        colStr("g_cond_flag").equals("Y")?"Y":"N",
        colNum("g_accumlate_amt"),
        colStr("h_cond_flag").equals("Y")?"Y":"N",
        colNum("h_accumlate_amt"),
        "Y",wp.loginUser, colStr("crt_date"),
        colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"),
        wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " group_code, " + " card_type, " + " card_fee, " + " sup_card_fee, "
        + " mer_cond, " + " mer_bl_flag, " + " mer_ca_flag, " + " mer_it_flag, " + " mer_ao_flag, "
        + " mer_id_flag, " + " mer_ot_flag, " + " major_flag, " + " sub_flag, " + " major_sub, "
        + " a_merchant_sel, " + " a_mcht_group_sel, " + " cnt_cond, " + " cnt_select, "
        + " month_cnt, " + " accumlate_cnt, " + " cnt_bl_flag, " + " cnt_ca_flag, "
        + " cnt_it_flag, " + " cnt_ao_flag, " + " cnt_id_flag, " + " cnt_ot_flag, "
        + " cnt_major_flag, " + " cnt_sub_flag, " + " cnt_major_sub, " + " b_mcc_code_sel, "
        + " b_merchant_sel, " + " b_mcht_group_sel, " + " amt_cond, " + " accumlate_amt, "
        + " amt_bl_flag, " + " amt_ca_flag, " + " amt_it_flag, " + " amt_ao_flag, "
        + " amt_id_flag, " + " amt_ot_flag, " + " amt_major_flag, " + " amt_sub_flag, "
        + " amt_major_sub, " + " c_mcc_code_sel, " + " c_merchant_sel, " + " c_mcht_group_sel, "
        + " mcode, " + " email_nopaper_flag, " 
        + " miner_half_flag, " + " g_cond_flag, " + " g_accumlate_amt, " + " h_cond_flag, " + " h_accumlate_amt, "
        + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

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
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "card_fee = ?, " + "sup_card_fee = ?, "
        + "mer_cond = ?, " + "mer_bl_flag = ?, " + "mer_ca_flag = ?, " + "mer_it_flag = ?, "
        + "mer_ao_flag = ?, " + "mer_id_flag = ?, " + "mer_ot_flag = ?, " + "major_flag = ?, "
        + "sub_flag = ?, " + "major_sub = ?, " + "a_merchant_sel = ?, " + "a_mcht_group_sel = ?, "
        + "cnt_cond = ?, " + "cnt_select = ?, " + "month_cnt = ?, " + "accumlate_cnt = ?, "
        + "cnt_bl_flag = ?, " + "cnt_ca_flag = ?, " + "cnt_it_flag = ?, " + "cnt_ao_flag = ?, "
        + "cnt_id_flag = ?, " + "cnt_ot_flag = ?, " + "cnt_major_flag = ?, " + "cnt_sub_flag = ?, "
        + "cnt_major_sub = ?, " + "b_mcc_code_sel = ?, " + "b_merchant_sel = ?, "
        + "b_mcht_group_sel = ?, " + "amt_cond = ?, " + "accumlate_amt = ?, " + "amt_bl_flag = ?, "
        + "amt_ca_flag = ?, " + "amt_it_flag = ?, " + "amt_ao_flag = ?, " + "amt_id_flag = ?, "
        + "amt_ot_flag = ?, " + "amt_major_flag = ?, " + "amt_sub_flag = ?, "
        + "amt_major_sub = ?, " + "c_mcc_code_sel = ?, " + "c_merchant_sel = ?, "
        + "c_mcht_group_sel = ?, " + "mcode = ?, " + "email_nopaper_flag = ?, " 
        + " miner_half_flag = ?, " + " g_cond_flag = ?, " + " g_accumlate_amt = ?, " 
        + " h_cond_flag = ?, " + " h_accumlate_amt = ?, " 
        + "crt_user  = ?, "
        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   group_code  = ? "
        + "and   card_type  = ? ";

    Object[] param = new Object[] {colStr("card_fee"), colStr("sup_card_fee"), colStr("mer_cond"),
        colStr("mer_bl_flag"), colStr("mer_ca_flag"), colStr("mer_it_flag"), colStr("mer_ao_flag"),
        colStr("mer_id_flag"), colStr("mer_ot_flag"), colStr("major_flag"), colStr("sub_flag"),
        colStr("major_sub"), colStr("a_merchant_sel"), colStr("a_mcht_group_sel"),
        colStr("cnt_cond"), colStr("cnt_select"), colStr("month_cnt"), colStr("accumlate_cnt"),
        colStr("cnt_bl_flag"), colStr("cnt_ca_flag"), colStr("cnt_it_flag"), colStr("cnt_ao_flag"),
        colStr("cnt_id_flag"), colStr("cnt_ot_flag"), colStr("cnt_major_flag"),
        colStr("cnt_sub_flag"), colStr("cnt_major_sub"), colStr("b_mcc_code_sel"),
        colStr("b_merchant_sel"), colStr("b_mcht_group_sel"), colStr("amt_cond"),
        colStr("accumlate_amt"), colStr("amt_bl_flag"), colStr("amt_ca_flag"),
        colStr("amt_it_flag"), colStr("amt_ao_flag"), colStr("amt_id_flag"), colStr("amt_ot_flag"),
        colStr("amt_major_flag"), colStr("amt_sub_flag"), colStr("amt_major_sub"),
        colStr("c_mcc_code_sel"), colStr("c_merchant_sel"), colStr("c_mcht_group_sel"),
        colStr("mcode"), colStr("email_nopaper_flag"),
        colStr("miner_half_flag").equals("Y")?"Y":"N",
        colStr("g_cond_flag").equals("Y")?"Y":"N",
        colNum("g_accumlate_amt"),
        colStr("h_cond_flag").equals("Y")?"Y":"N",
        colNum("h_accumlate_amt"),
        colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, aprFlag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("group_code"), colStr("card_type")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and group_code = ? "
        + "and card_type = ? ";

    Object[] param = new Object[] {colStr("group_code"), colStr("card_type")};

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
    strSql = "delete mkt_bn_data " + "where 1 = 1 " + "and table_name  =  'CYC_ANUL_GP' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("group_code") + colStr("card_type"),};

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
    strSql = "delete mkt_bn_data_t " + "where 1 = 1 " + "and table_name  =  'CYC_ANUL_GP' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("group_code") + colStr("card_type"),};

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
        + "and table_name  =  'CYC_ANUL_GP' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("group_code") + colStr("card_type"),};

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
