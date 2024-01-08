/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 112/03/08  V1.00.00   Yang Bo                   Initial                  *
 ***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktp0090Func extends busi.FuncProc {
 private final String PROGNAME = "紅利利率轉換檔參數覆核處理程式112/03/08  V1.00.00";
 String kk1, kk2, kk3, kk4;
 String approveTabName = "cyc_bpid2";
 String controlTabName = "cyc_bpid2_t";

 public Mktp0090Func(TarokoCommon wr) {
  wp = wr;
  conn = wp.getConn();
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
 }

 // ************************************************************************
 @Override
 public int dataProc() {
  return rc;
 }

 // ************************************************************************
 public int dbInsertA4() {
  rc = dbSelectS4();
  if (rc != 1) {
   return rc;
  }
  strSql = " insert into  " + approveTabName + " ("
          + " active_code, "
          + " active_name, "
          + " bonus_type, "
          + " acct_type, "
          + " foreign_code, "
          + " active_month_s, "
          + " active_month_e, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " bl_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " ca_cond, "
          + " effect_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " group_card_sel, "
          + " group_merchant_sel, "
          + " limit_1_beg, "
          + " limit_1_end, "
          + " exchange_1, "
          + " limit_2_beg, "
          + " limit_2_end, "
          + " exchange_2, "
          + " limit_3_beg, "
          + " limit_3_end, "
          + " exchange_3, "
          + " limit_4_beg, "
          + " limit_4_end, "
          + " exchange_4, "
          + " limit_5_beg, "
          + " limit_5_end, "
          + " exchange_5, "
          + " limit_6_beg, "
          + " limit_6_end, "
          + " exchange_6, "
          + " feedback_lmt, "
          + " limit_amt, "
          + " apr_flag, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + "?,"
          + "?,"
          + "?) ";

  Object[] param = new Object[]
          {
                  colStr("active_code"),
                  colStr("active_name"),
                  colStr("bonus_type"),
                  colStr("acct_type"),
                  colStr("foreign_code"),
                  colStr("active_month_s"),
                  colStr("active_month_e"),
                  colStr("stop_flag"),
                  colStr("stop_date"),
                  colStr("stop_desc"),
                  colStr("bl_cond"),
                  colStr("it_cond"),
                  colStr("id_cond"),
                  colStr("ao_cond"),
                  colStr("ot_cond"),
                  colStr("ca_cond"),
                  colStr("effect_months"),
                  colStr("merchant_sel"),
                  colStr("mcht_group_sel"),
                  colStr("platform_kind_sel"),
                  colStr("group_card_sel"),
                  colStr("group_merchant_sel"),
                  colStr("limit_1_beg"),
                  colStr("limit_1_end"),
                  colStr("exchange_1"),
                  colStr("limit_2_beg"),
                  colStr("limit_2_end"),
                  colStr("exchange_2"),
                  colStr("limit_3_beg"),
                  colStr("limit_3_end"),
                  colStr("exchange_3"),
                  colStr("limit_4_beg"),
                  colStr("limit_4_end"),
                  colStr("exchange_4"),
                  colStr("limit_5_beg"),
                  colStr("limit_5_end"),
                  colStr("exchange_5"),
                  colStr("limit_6_beg"),
                  colStr("limit_6_end"),
                  colStr("exchange_6"),
                  colStr("feedback_lmt"),
                  colStr("limit_amt"),
                  "Y",
                  wp.loginUser,
                  colStr("crt_date"),
                  colStr("crt_user"),
                  wp.sysDate + wp.sysTime,
                  wp.loginUser,
                  colStr("mod_seqno"),
                  wp.modPgm()
          };

  sqlExec(strSql, param);

  return rc;
 }

 // ************************************************************************
 public int dbSelectS4() {
  String procTabName = "";
  procTabName = controlTabName;
  strSql = " select "
          + " active_code, "
          + " active_name, "
          + " bonus_type, "
          + " acct_type, "
          + " foreign_code, "
          + " active_month_s, "
          + " active_month_e, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " bl_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " ca_cond, "
          + " effect_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " group_card_sel, "
          + " group_merchant_sel, "
          + " limit_1_beg, "
          + " limit_1_end, "
          + " exchange_1, "
          + " limit_2_beg, "
          + " limit_2_end, "
          + " exchange_2, "
          + " limit_3_beg, "
          + " limit_3_end, "
          + " exchange_3, "
          + " limit_4_beg, "
          + " limit_4_end, "
          + " exchange_4, "
          + " limit_5_beg, "
          + " limit_5_end, "
          + " exchange_5, "
          + " limit_6_beg, "
          + " limit_6_end, "
          + " exchange_6, "
          + " feedback_lmt, "
          + " limit_amt, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName
          + " where rowid = ? ";

  Object[] param = new Object[]
          {
                  wp.itemRowId("wprowid")
          };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) {
   errmsg("查無資料，讀取 " + controlTabName + " 失敗");
  }

  return rc;
 }

 // ************************************************************************
 public int dbUpdateU4() {
  rc = dbSelectS4();
  if (rc != 1) {
   return rc;
  }
  String aprFlag = "Y";
  strSql = "update " + approveTabName + " set "
          + "active_month_s = ?, "
          + "active_month_e = ?, "
          + "stop_flag = ?, "
          + "stop_date = ?, "
          + "stop_desc = ?, "
          + "bl_cond = ?, "
          + "it_cond = ?, "
          + "id_cond = ?, "
          + "ao_cond = ?, "
          + "ot_cond = ?, "
          + "ca_cond = ?, "
          + "effect_months = ?, "
          + "merchant_sel = ?, "
          + "mcht_group_sel = ?, "
          + "platform_kind_sel = ?, "
          + "group_card_sel = ?, "
          + "group_merchant_sel = ?, "
          + "limit_1_beg = ?, "
          + "limit_1_end = ?, "
          + "exchange_1 = ?, "
          + "limit_2_beg = ?, "
          + "limit_2_end = ?, "
          + "exchange_2 = ?, "
          + "limit_3_beg = ?, "
          + "limit_3_end = ?, "
          + "exchange_3 = ?, "
          + "limit_4_beg = ?, "
          + "limit_4_end = ?, "
          + "exchange_4 = ?, "
          + "limit_5_beg = ?, "
          + "limit_5_end = ?, "
          + "exchange_5 = ?, "
          + "limit_6_beg = ?, "
          + "limit_6_end = ?, "
          + "exchange_6 = ?, "
          + "feedback_lmt = ?, "
          + "limit_amt = ?, "
          + "crt_user  = ?, "
          + "crt_date  = ?, "
          + "apr_user  = ?, "
          + "apr_date  = to_char(sysdate,'yyyymmdd'), "
          + "apr_flag  = ?, "
          + "mod_user  = ?, "
          + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
          + "mod_pgm   = ?, "
          + "mod_seqno = nvl(mod_seqno,0)+1 "
          + "where 1     = 1 "
          + "and   active_code  = ? "
          + "and   bonus_type  = ? "
          + "and   acct_type  = ? "
          + "and   foreign_code  = ? "
  ;

  Object[] param = new Object[]
          {
                  colStr("active_month_s"),
                  colStr("active_month_e"),
                  colStr("stop_flag"),
                  colStr("stop_date"),
                  colStr("stop_desc"),
                  colStr("bl_cond"),
                  colStr("it_cond"),
                  colStr("id_cond"),
                  colStr("ao_cond"),
                  colStr("ot_cond"),
                  colStr("ca_cond"),
                  colStr("effect_months"),
                  colStr("merchant_sel"),
                  colStr("mcht_group_sel"),
                  colStr("platform_kind_sel"),
                  colStr("group_card_sel"),
                  colStr("group_merchant_sel"),
                  colStr("limit_1_beg"),
                  colStr("limit_1_end"),
                  colStr("exchange_1"),
                  colStr("limit_2_beg"),
                  colStr("limit_2_end"),
                  colStr("exchange_2"),
                  colStr("limit_3_beg"),
                  colStr("limit_3_end"),
                  colStr("exchange_3"),
                  colStr("limit_4_beg"),
                  colStr("limit_4_end"),
                  colStr("exchange_4"),
                  colStr("limit_5_beg"),
                  colStr("limit_5_end"),
                  colStr("exchange_5"),
                  colStr("limit_6_beg"),
                  colStr("limit_6_end"),
                  colStr("exchange_6"),
                  colStr("feedback_lmt"),
                  colStr("limit_amt"),
                  colStr("crt_user"),
                  colStr("crt_date"),
                  wp.loginUser,
                  aprFlag,
                  colStr("mod_user"),
                  colStr("mod_time"),
                  colStr("mod_pgm"),
                  colStr("active_code"),
                  colStr("bonus_type"),
                  colStr("acct_type"),
                  colStr("foreign_code")
          };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
   rc = 0;
  } else {
   rc = 1;
  }

  return rc;
 }

 // ************************************************************************
 public int dbDeleteD4() {
  rc = dbSelectS4();
  if (rc != 1) {
   return rc;
  }
  strSql = "delete " + approveTabName + " "
          + "where 1 = 1 "
          + "and active_code = ? "
          + "and bonus_type = ? "
          + "and acct_type = ? "
          + "and foreign_code = ? "
  ;

  Object[] param = new Object[]
          {
                  colStr("active_code"),
                  colStr("bonus_type"),
                  colStr("acct_type"),
                  colStr("foreign_code")
          };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
   rc = 0;
  } else {
   rc = 1;
  }
  if (rc != 1) {
   errmsg("刪除 " + approveTabName + " 錯誤");
  }

  return rc;
 }

 // ************************************************************************
 public int dbDeleteD4Bndata() {
  rc = dbSelectS4();
  if (rc != 1) {
   return rc;
  }
  strSql = "delete cyc_bn_data "
          + "where 1 = 1 "
          + "and table_name  =  'CYC_BPID2' "
          + "and data_key  = ?  "
  ;

  Object[] param = new Object[]
          {
                  colStr("active_code"),
          };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc != 1) {
   errmsg("刪除 cyc_bn_data 錯誤");
  }

  return 1;
 }

 // ************************************************************************
 public int dbDeleteD4TBndata() {
  rc = dbSelectS4();
  if (rc != 1) {
   return rc;
  }
  strSql = "delete cyc_bn_data_t "
          + "where 1 = 1 "
          + "and table_name  =  'CYC_BPID2' "
          + "and data_key  = ?  "
  ;

  Object[] param = new Object[]
          {
                  colStr("active_code"),
          };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc != 1) {
   errmsg("刪除 cyc_bn_data_T 錯誤");
  }

  return 1;
 }

 // ************************************************************************
 public int dbInsertA4Bndata() {
  rc = dbSelectS4();
  if (rc != 1) {
   return rc;
  }
  strSql = "insert into cyc_bn_data "
          + "select * "
          + "from  cyc_bn_data_t "
          + "where 1 = 1 "
          + "and table_name  =  'CYC_BPID2' "
          + "and data_key  = ?  "
  ;

  Object[] param = new Object[]
          {
                  colStr("active_code"),
          };

  sqlExec(strSql, param, true);

  return 1;
 }

 // ************************************************************************
 public int dbDelete() {
  strSql = "delete " + controlTabName + " "
          + "where rowid = ?";

  Object[] param = new Object[]
          {
                  wp.itemRowId("wprowid")
          };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
   rc = 0;
  } else {
   rc = 1;
  }
  if (sqlRowNum <= 0) {
   errmsg("刪除 " + controlTabName + " 錯誤");
   return (-1);
  }

  return rc;
 }
// ************************************************************************

}  // End of class
