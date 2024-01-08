/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/02/16  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard      
* 112-05-05  V1.00.03  Ryan    新增國內外消費欄位維護，特店中文名稱、特店英文名稱參數維護，[消費回饋比例]區塊新增多個欄位維護   *        
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *                                                           *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6220Func extends busi.FuncProc
{
 private final String PROGNAME = "刷卡金參數檔覆核作業處理程式111-11-30  V1.00.01";
  String kk1;
  String approveTabName = "ptr_fundp";
  String controlTabName = "ptr_fundp_t";

 public Mktp6220Func(TarokoCommon wr)
 {
  wp = wr;
  this.conn = wp.getConn();
 }
// ************************************************************************
 @Override
 public int querySelect()
 {
  // TODO Auto-generated method
  return 0;
 }
// ************************************************************************
 @Override
 public int dataSelect()
 {
  // TODO Auto-generated method stub
  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck() 
 {
 }
// ************************************************************************
 @Override
 public int dataProc()
 {
  return rc;
 }
// ************************************************************************
 public int dbInsertA4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql= " insert into  " + approveTabName+ " ("
          + " fund_code, "
          + " fund_name, "
          + " tran_base, "
          + " fund_crt_date_s, "
          + " fund_crt_date_e, "
          + " effect_type, "
          + " effect_months, "
          + " effect_years, "
          + " effect_fix_month, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " bin_type_sel, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " card_type_sel, "
          + " new_hldr_cond, "
          + " new_hldr_flag, "
          + " new_card_days, "
          + " new_hldr_days, "
          + " new_group_cond, "
          + " new_hldr_card, "
          + " new_hldr_sup, "
          + " apply_age_cond, "
          + " apply_age_s, "
          + " apply_age_e, "
          + " activate_cond, "
          + " activate_flag, "
          + " valid_period, "
          + " cobrand_code, "
          + " source_code_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " currency_sel, "
          + " ex_currency_sel, "
          + " pos_entry_sel, "
          + " pos_merchant_sel, "
          + " pos_mcht_group_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " it_cond, "
          + " ot_cond, "
          + " purch_feed_flag, "
          + " purch_date_s, "
          + " purch_date_e, "
          + " purch_reclow_cond, "
          + " purch_reclow_amt, "
          + " purch_rec_amt_cond, "
          + " purch_rec_amt, "
          + " purch_tol_amt_cond, "
          + " purch_tol_amt, "
          + " purch_tol_time_cond, "
          + " purch_tol_time, "
          + " purch_feed_type, "
          + " purch_type, "
          + " purch_feed_amt, "
          + " purch_feed_rate, "
          + " fund_feed_flag, "
          + " threshold_sel, "
          + " purchase_type_sel, "
          + " fund_s_amt_1, "
          + " fund_e_amt_1, "
          + " fund_rate_1, "
          + " fund_amt_1, "
          + " fund_s_amt_2, "
          + " fund_e_amt_2, "
          + " fund_rate_2, "
          + " fund_amt_2, "
          + " fund_s_amt_3, "
          + " fund_e_amt_3, "
          + " fund_rate_3, "
          + " fund_amt_3, "
          + " fund_s_amt_4, "
          + " fund_e_amt_4, "
          + " fund_rate_4, "
          + " fund_amt_4, "
          + " fund_s_amt_5, "
          + " fund_e_amt_5, "
          + " fund_rate_5, "
          + " fund_amt_5, "
          + " rc_sub_amt, "
          + " rc_sub_rate, "
          + " program_exe_type, "
          + " unlimit_start_month, "
          + " cal_s_month, "
          + " cal_e_month, "
          + " card_feed_date_s, "
          + " card_feed_date_e, "
          + " card_feed_flag, "
          + " cal_months, "
          + " card_feed_months2, "
          + " card_feed_days, "
          + " new_hldr_sel, "
          + " feedback_type, "
          + " card_feed_run_day, "
          + " feedback_months, "
          + " feedback_cycle_flag, "
          + " feedback_lmt, "
          + " purch_feed_times, "
          + " autopay_flag, "
          + " mp_flag, "
          + " valid_card_flag, "
          + " valid_afi_flag, "
          + " ebill_flag, "
          + " autopay_digit_cond, "
          + " d_txn_cond, "
          + " d_txn_amt, "
          + " cancel_period, "
          + " cancel_s_month, "
          + " cancel_scope, "
          + " d_mcc_code_sel, "
          + " d_merchant_sel, "
          + " d_mcht_group_sel, "
          + " d_ucaf_sel, "
          + " d_eci_sel, "
          + " d_pos_entry_sel, "
          + " cancel_event, "
          + " min_mcode, "
          + " cancel_high_amt, "
          + " apr_flag, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm,"
          + " foreign_code, "
          + " mcht_cname_sel, "
          + " mcht_ename_sel, "
          + " hapcare_trust_cond,"
          + " hapcare_trust_rate,"
          + " housing_endow_cond,"
          + " housing_endow_rate,"
          + " happycare_fblmt,"
          + " mortgage_cond,"
          + " mortgag_rate,"
          + " mortgage_fblmt,"
          + " util_entrustded_cond,"
          + " util_entrustded_rate,"
          + " util_entrustded_fblmt,"
          + " twpay_cond,"
          + " twpay_rate,"
          + " tcblife_ec_cond,"
          + " tcblife_ec_rate,"
          + " eco_fblmt,"
          + " extratwpay_cond,"
          + " onlyaddon_calcond "
//          + " extratwpay_rate,"
//          + " extratwpay_fblmt "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "'Y',"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + "?,"
          + "?,"
          + " ?,"
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

  Object[] param =new Object[]
       {
        colStr("fund_code"),
        colStr("fund_name"),
        colStr("tran_base"),
        colStr("fund_crt_date_s"),
        colStr("fund_crt_date_e"),
        colStr("effect_type"),
        colStr("effect_months"),
        colStr("effect_years"),
        colStr("effect_fix_month"),
        colStr("stop_flag"),
        colStr("stop_date"),
        colStr("stop_desc"),
        colStr("bin_type_sel"),
        colStr("acct_type_sel"),
        colStr("group_code_sel"),
        colStr("card_type_sel"),
        colStr("new_hldr_cond"),
        colStr("new_hldr_flag"),
        colStr("new_card_days"),
        colStr("new_hldr_days"),
        colStr("new_group_cond"),
        colStr("new_hldr_card"),
        colStr("new_hldr_sup"),
        colStr("apply_age_cond"),
        colStr("apply_age_s"),
        colStr("apply_age_e"),
        colStr("activate_cond"),
        colStr("activate_flag"),
        colStr("valid_period"),
        colStr("cobrand_code"),
        colStr("source_code_sel"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("platform_kind_sel"),
        colStr("currency_sel"),
        colStr("ex_currency_sel"),
        colStr("pos_entry_sel"),
        colStr("pos_merchant_sel"),
        colStr("pos_mcht_group_sel"),
        colStr("bl_cond"),
        colStr("ca_cond"),
        colStr("id_cond"),
        colStr("ao_cond"),
        colStr("it_cond"),
        colStr("ot_cond"),
        colStr("purch_feed_flag"),
        colStr("purch_date_s"),
        colStr("purch_date_e"),
        colStr("purch_reclow_cond"),
        colStr("purch_reclow_amt"),
        colStr("purch_rec_amt_cond"),
        colStr("purch_rec_amt"),
        colStr("purch_tol_amt_cond"),
        colStr("purch_tol_amt"),
        colStr("purch_tol_time_cond"),
        colStr("purch_tol_time"),
        colStr("purch_feed_type"),
        colStr("purch_type"),
        colStr("purch_feed_amt"),
        colStr("purch_feed_rate"),
        colStr("fund_feed_flag"),
        colStr("threshold_sel"),
        colStr("purchase_type_sel"),
        colStr("fund_s_amt_1"),
        colStr("fund_e_amt_1"),
        colStr("fund_rate_1"),
        colStr("fund_amt_1"),
        colStr("fund_s_amt_2"),
        colStr("fund_e_amt_2"),
        colStr("fund_rate_2"),
        colStr("fund_amt_2"),
        colStr("fund_s_amt_3"),
        colStr("fund_e_amt_3"),
        colStr("fund_rate_3"),
        colStr("fund_amt_3"),
        colStr("fund_s_amt_4"),
        colStr("fund_e_amt_4"),
        colStr("fund_rate_4"),
        colStr("fund_amt_4"),
        colStr("fund_s_amt_5"),
        colStr("fund_e_amt_5"),
        colStr("fund_rate_5"),
        colStr("fund_amt_5"),
        colStr("rc_sub_amt"),
        colStr("rc_sub_rate"),
        colStr("program_exe_type"),
        colStr("unlimit_start_month"),
        colStr("cal_s_month"),
        colStr("cal_e_month"),
        colStr("card_feed_date_s"),
        colStr("card_feed_date_e"),
        colStr("card_feed_flag"),
        colStr("cal_months"),
        colStr("card_feed_months2"),
        colStr("card_feed_days"),
        colStr("new_hldr_sel"),
        colStr("feedback_type"),
        colStr("card_feed_run_day"),
        colStr("feedback_months"),
        colStr("feedback_cycle_flag"),
        colStr("feedback_lmt"),
        colStr("purch_feed_times"),
        colStr("autopay_flag"),
        colStr("mp_flag"),
        colStr("valid_card_flag"),
        colStr("valid_afi_flag"),
        colStr("ebill_flag"),
        colStr("autopay_digit_cond"),
        colStr("d_txn_cond"),
        colStr("d_txn_amt"),
        colStr("cancel_period"),
        colStr("cancel_s_month"),
        colStr("cancel_scope"),
        colStr("d_mcc_code_sel"),
        colStr("d_merchant_sel"),
        colStr("d_mcht_group_sel"),
        colStr("d_ucaf_sel"),
        colStr("d_eci_sel"),
        colStr("d_pos_entry_sel"),
        colStr("cancel_event"),
        colStr("min_mcode"),
        colStr("cancel_high_amt"),
        wp.loginUser,
        colStr("crt_date"),
        colStr("crt_user"),
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        colStr("mod_seqno"),  
        wp.modPgm(),
        colStr("foreign_code"),
        colStr("mcht_cname_sel"),
        colStr("mcht_ename_sel"),
        colStr("hapcare_trust_cond"),
        colNum("hapcare_trust_rate"),
        colStr("housing_endow_cond"),
        colNum("housing_endow_rate"),
        colNum("happycare_fblmt"),
        colStr("mortgage_cond"),
        colNum("mortgag_rate"),
        colNum("mortgage_fblmt"),
        colStr("util_entrustded_cond"),
        colNum("util_entrustded_rate"),
        colNum("util_entrustded_fblmt"),
        colStr("twpay_cond"),
        colNum("twpay_rate"),
        colStr("tcblife_ec_cond"),
        colNum("tcblife_ec_rate"),
        colNum("eco_fblmt"),
        colStr("extratwpay_cond"),
        colStr("onlyaddon_calcond")
//        colNum("extratwpay_rate"),
//        colNum("extratwpay_fblmt")
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增資料 "+ controlTabName +" 失敗");

  return rc;
 }
// ************************************************************************
 public int dbSelectS4() throws Exception
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " fund_code, "
          + " fund_name, "
          + " tran_base, "
          + " fund_crt_date_s, "
          + " fund_crt_date_e, "
          + " effect_type, "
          + " effect_months, "
          + " effect_years, "
          + " effect_fix_month, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " bin_type_sel, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " card_type_sel, "
          + " new_hldr_cond, "
          + " new_hldr_flag, "
          + " new_card_days, "
          + " new_hldr_days, "
          + " new_group_cond, "
          + " new_hldr_card, "
          + " new_hldr_sup, "
          + " apply_age_cond, "
          + " apply_age_s, "
          + " apply_age_e, "
          + " activate_cond, "
          + " activate_flag, "
          + " valid_period, "
          + " cobrand_code, "
          + " source_code_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " currency_sel, "
          + " ex_currency_sel, "
          + " pos_entry_sel, "
          + " pos_merchant_sel, "
          + " pos_mcht_group_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " it_cond, "
          + " ot_cond, "
          + " purch_feed_flag, "
          + " purch_date_s, "
          + " purch_date_e, "
          + " purch_reclow_cond, "
          + " purch_reclow_amt, "
          + " purch_rec_amt_cond, "
          + " purch_rec_amt, "
          + " purch_tol_amt_cond, "
          + " purch_tol_amt, "
          + " purch_tol_time_cond, "
          + " purch_tol_time, "
          + " purch_feed_type, "
          + " purch_type, "
          + " purch_feed_amt, "
          + " purch_feed_rate, "
          + " fund_feed_flag, "
          + " threshold_sel, "
          + " purchase_type_sel, "
          + " fund_s_amt_1, "
          + " fund_e_amt_1, "
          + " fund_rate_1, "
          + " fund_amt_1, "
          + " fund_s_amt_2, "
          + " fund_e_amt_2, "
          + " fund_rate_2, "
          + " fund_amt_2, "
          + " fund_s_amt_3, "
          + " fund_e_amt_3, "
          + " fund_rate_3, "
          + " fund_amt_3, "
          + " fund_s_amt_4, "
          + " fund_e_amt_4, "
          + " fund_rate_4, "
          + " fund_amt_4, "
          + " fund_s_amt_5, "
          + " fund_e_amt_5, "
          + " fund_rate_5, "
          + " fund_amt_5, "
          + " rc_sub_amt, "
          + " rc_sub_rate, "
          + " program_exe_type, "
          + " unlimit_start_month, "
          + " cal_s_month, "
          + " cal_e_month, "
          + " card_feed_date_s, "
          + " card_feed_date_e, "
          + " card_feed_flag, "
          + " cal_months, "
          + " card_feed_months2, "
          + " card_feed_days, "
          + " new_hldr_sel, "
          + " feedback_type, "
          + " card_feed_run_day, "
          + " feedback_months, "
          + " feedback_cycle_flag, "
          + " feedback_lmt, "
          + " purch_feed_times, "
          + " autopay_flag, "
          + " mp_flag, "
          + " valid_card_flag, "
          + " valid_afi_flag, "
          + " ebill_flag, "
          + " autopay_digit_cond, "
          + " d_txn_cond, "
          + " d_txn_amt, "
          + " cancel_period, "
          + " cancel_s_month, "
          + " cancel_scope, "
          + " d_mcc_code_sel, "
          + " d_merchant_sel, "
          + " d_mcht_group_sel, "
          + " d_ucaf_sel, "
          + " d_eci_sel, "
          + " d_pos_entry_sel, "
          + " cancel_event, "
          + " min_mcode, "
          + " cancel_high_amt, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
          + " foreign_code, "
          + " hapcare_trust_cond,"
          + " hapcare_trust_rate,"
          + " housing_endow_cond,"
          + " housing_endow_rate,"
          + " happycare_fblmt,"
          + " mortgage_cond,"
          + " mortgag_rate,"
          + " mortgage_fblmt,"
          + " util_entrustded_cond,"
          + " util_entrustded_rate,"
          + " util_entrustded_fblmt,"
          + " twpay_cond,"
          + " twpay_rate,"
          + " tcblife_ec_cond,"
          + " tcblife_ec_rate,"
          + " eco_fblmt,"
          + " extratwpay_cond,"
          + " onlyaddon_calcond,"
//          + " extratwpay_rate,"
//          + " extratwpay_fblmt,"
          + " mcht_cname_sel,"
          + " mcht_ename_sel "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("wprowid")
       };

  sqlSelect(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return rc;
 }
// ************************************************************************
 public int dbUpdateU4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  String aprFlag = "Y";
  strSql= "update " +approveTabName + " set "
         + "fund_name = ?, "
         + "tran_base = ?, "
         + "fund_crt_date_s = ?, "
         + "fund_crt_date_e = ?, "
         + "effect_type = ?, "
         + "effect_months = ?, "
         + "effect_years = ?, "
         + "effect_fix_month = ?, "
         + "stop_flag = ?, "
         + "stop_date = ?, "
         + "stop_desc = ?, "
         + "bin_type_sel = ?, "
         + "acct_type_sel = ?, "
         + "group_code_sel = ?, "
         + "card_type_sel = ?, "
         + "new_hldr_cond = ?, "
         + "new_hldr_flag = ?, "
         + "new_card_days = ?, "
         + "new_hldr_days = ?, "
         + "new_group_cond = ?, "
         + "new_hldr_card = ?, "
         + "new_hldr_sup = ?, "
         + "apply_age_cond = ?, "
         + "apply_age_s = ?, "
         + "apply_age_e = ?, "
         + "activate_cond = ?, "
         + "activate_flag = ?, "
         + "valid_period = ?, "
         + "cobrand_code = ?, "
         + "source_code_sel = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "platform_kind_sel = ?, "
         + "currency_sel = ?, "
         + "ex_currency_sel = ?, "
         + "pos_entry_sel = ?, "
         + "pos_merchant_sel = ?, "
         + "pos_mcht_group_sel = ?, "
         + "bl_cond = ?, "
         + "ca_cond = ?, "
         + "id_cond = ?, "
         + "ao_cond = ?, "
         + "it_cond = ?, "
         + "ot_cond = ?, "
         + "purch_feed_flag = ?, "
         + "purch_date_s = ?, "
         + "purch_date_e = ?, "
         + "purch_reclow_cond = ?, "
         + "purch_reclow_amt = ?, "
         + "purch_rec_amt_cond = ?, "
         + "purch_rec_amt = ?, "
         + "purch_tol_amt_cond = ?, "
         + "purch_tol_amt = ?, "
         + "purch_tol_time_cond = ?, "
         + "purch_tol_time = ?, "
         + "purch_feed_type = ?, "
         + "purch_type = ?, "
         + "purch_feed_amt = ?, "
         + "purch_feed_rate = ?, "
         + "fund_feed_flag = ?, "
         + "threshold_sel = ?, "
         + "purchase_type_sel = ?, "
         + "fund_s_amt_1 = ?, "
         + "fund_e_amt_1 = ?, "
         + "fund_rate_1 = ?, "
         + "fund_amt_1 = ?, "
         + "fund_s_amt_2 = ?, "
         + "fund_e_amt_2 = ?, "
         + "fund_rate_2 = ?, "
         + "fund_amt_2 = ?, "
         + "fund_s_amt_3 = ?, "
         + "fund_e_amt_3 = ?, "
         + "fund_rate_3 = ?, "
         + "fund_amt_3 = ?, "
         + "fund_s_amt_4 = ?, "
         + "fund_e_amt_4 = ?, "
         + "fund_rate_4 = ?, "
         + "fund_amt_4 = ?, "
         + "fund_s_amt_5 = ?, "
         + "fund_e_amt_5 = ?, "
         + "fund_rate_5 = ?, "
         + "fund_amt_5 = ?, "
         + "rc_sub_amt = ?, "
         + "rc_sub_rate = ?, "
         + "program_exe_type = ?, "
         + "unlimit_start_month = ?, "
         + "cal_s_month = ?, "
         + "cal_e_month = ?, "
         + "card_feed_date_s = ?, "
         + "card_feed_date_e = ?, "
         + "card_feed_flag = ?, "
         + "cal_months = ?, "
         + "card_feed_months2 = ?, "
         + "card_feed_days = ?, "
         + "new_hldr_sel = ?, "
         + "feedback_type = ?, "
         + "card_feed_run_day = ?, "
         + "feedback_months = ?, "
         + "feedback_cycle_flag = ?, "
         + "feedback_lmt = ?, "
         + "purch_feed_times = ?, "
         + "autopay_flag = ?, "
         + "mp_flag = ?, "
         + "valid_card_flag = ?, "
         + "valid_afi_flag = ?, "
         + "ebill_flag = ?, "
         + "autopay_digit_cond = ?, "
         + "d_txn_cond = ?, "
         + "d_txn_amt = ?, "
         + "cancel_period = ?, "
         + "cancel_s_month = ?, "
         + "cancel_scope = ?, "
         + "d_mcc_code_sel = ?, "
         + "d_merchant_sel = ?, "
         + "d_mcht_group_sel = ?, "
         + "d_ucaf_sel = ?, "
         + "d_eci_sel = ?, "
         + "d_pos_entry_sel = ?, "
         + "cancel_event = ?, "
         + "min_mcode = ?, "
         + "cancel_high_amt = ?, "
         + "crt_user  = ?, "
         + "crt_date  = ?, "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_flag  = ?, "
         + "mod_user  = ?, "
         + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "mod_pgm   = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "foreign_code = ?, "
         + "mcht_cname_sel = ?, "
         + "mcht_ename_sel = ?,"
         + " hapcare_trust_cond = ?,"
         + " hapcare_trust_rate = ?,"
         + " housing_endow_cond = ?,"
         + " housing_endow_rate = ?,"
         + " happycare_fblmt = ?,"
         + " mortgage_cond = ?,"
         + " mortgag_rate = ?,"
         + " mortgage_fblmt = ?,"
         + " util_entrustded_cond = ?,"
         + " util_entrustded_rate = ?,"
         + " util_entrustded_fblmt = ?,"
         + " twpay_cond = ?,"
         + " twpay_rate = ?,"
         + " tcblife_ec_cond = ?,"
         + " tcblife_ec_rate = ?,"
         + " eco_fblmt = ?,"
         + " extratwpay_cond = ?,"
         + " onlyaddon_calcond = ? "
//         + " extratwpay_rate = ?,"
//         + " extratwpay_fblmt = ? "
         + "where 1     = 1 " 
         + "and   fund_code  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_name"),
     colStr("tran_base"),
     colStr("fund_crt_date_s"),
     colStr("fund_crt_date_e"),
     colStr("effect_type"),
     colStr("effect_months"),
     colStr("effect_years"),
     colStr("effect_fix_month"),
     colStr("stop_flag"),
     colStr("stop_date"),
     colStr("stop_desc"),
     colStr("bin_type_sel"),
     colStr("acct_type_sel"),
     colStr("group_code_sel"),
     colStr("card_type_sel"),
     colStr("new_hldr_cond"),
     colStr("new_hldr_flag"),
     colStr("new_card_days"),
     colStr("new_hldr_days"),
     colStr("new_group_cond"),
     colStr("new_hldr_card"),
     colStr("new_hldr_sup"),
     colStr("apply_age_cond"),
     colStr("apply_age_s"),
     colStr("apply_age_e"),
     colStr("activate_cond"),
     colStr("activate_flag"),
     colStr("valid_period"),
     colStr("cobrand_code"),
     colStr("source_code_sel"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("platform_kind_sel"),
     colStr("currency_sel"),
     colStr("ex_currency_sel"),
     colStr("pos_entry_sel"),
     colStr("pos_merchant_sel"),
     colStr("pos_mcht_group_sel"),
     colStr("bl_cond"),
     colStr("ca_cond"),
     colStr("id_cond"),
     colStr("ao_cond"),
     colStr("it_cond"),
     colStr("ot_cond"),
     colStr("purch_feed_flag"),
     colStr("purch_date_s"),
     colStr("purch_date_e"),
     colStr("purch_reclow_cond"),
     colStr("purch_reclow_amt"),
     colStr("purch_rec_amt_cond"),
     colStr("purch_rec_amt"),
     colStr("purch_tol_amt_cond"),
     colStr("purch_tol_amt"),
     colStr("purch_tol_time_cond"),
     colStr("purch_tol_time"),
     colStr("purch_feed_type"),
     colStr("purch_type"),
     colStr("purch_feed_amt"),
     colStr("purch_feed_rate"),
     colStr("fund_feed_flag"),
     colStr("threshold_sel"),
     colStr("purchase_type_sel"),
     colStr("fund_s_amt_1"),
     colStr("fund_e_amt_1"),
     colStr("fund_rate_1"),
     colStr("fund_amt_1"),
     colStr("fund_s_amt_2"),
     colStr("fund_e_amt_2"),
     colStr("fund_rate_2"),
     colStr("fund_amt_2"),
     colStr("fund_s_amt_3"),
     colStr("fund_e_amt_3"),
     colStr("fund_rate_3"),
     colStr("fund_amt_3"),
     colStr("fund_s_amt_4"),
     colStr("fund_e_amt_4"),
     colStr("fund_rate_4"),
     colStr("fund_amt_4"),
     colStr("fund_s_amt_5"),
     colStr("fund_e_amt_5"),
     colStr("fund_rate_5"),
     colStr("fund_amt_5"),
     colStr("rc_sub_amt"),
     colStr("rc_sub_rate"),
     colStr("program_exe_type"),
     colStr("unlimit_start_month"),
     colStr("cal_s_month"),
     colStr("cal_e_month"),
     colStr("card_feed_date_s"),
     colStr("card_feed_date_e"),
     colStr("card_feed_flag"),
     colStr("cal_months"),
     colStr("card_feed_months2"),
     colStr("card_feed_days"),
     colStr("new_hldr_sel"),
     colStr("feedback_type"),
     colStr("card_feed_run_day"),
     colStr("feedback_months"),
     colStr("feedback_cycle_flag"),
     colStr("feedback_lmt"),
     colStr("purch_feed_times"),
     colStr("autopay_flag"),
     colStr("mp_flag"),
     colStr("valid_card_flag"),
     colStr("valid_afi_flag"),
     colStr("ebill_flag"),
     colStr("autopay_digit_cond"),
     colStr("d_txn_cond"),
     colStr("d_txn_amt"),
     colStr("cancel_period"),
     colStr("cancel_s_month"),
     colStr("cancel_scope"),
     colStr("d_mcc_code_sel"),
     colStr("d_merchant_sel"),
     colStr("d_mcht_group_sel"),
     colStr("d_ucaf_sel"),
     colStr("d_eci_sel"),
     colStr("d_pos_entry_sel"),
     colStr("cancel_event"),
     colStr("min_mcode"),
     colStr("cancel_high_amt"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("foreign_code"),
     colStr("mcht_cname_sel"),
     colStr("mcht_ename_sel"),
     colStr("hapcare_trust_cond"),
     colNum("hapcare_trust_rate"),
     colStr("housing_endow_cond"),
     colNum("housing_endow_rate"),
     colNum("happycare_fblmt"),
     colStr("mortgage_cond"),
     colNum("mortgag_rate"),
     colNum("mortgage_fblmt"),
     colStr("util_entrustded_cond"),
     colNum("util_entrustded_rate"),
     colNum("util_entrustded_fblmt"),
     colStr("twpay_cond"),
     colNum("twpay_rate"),
     colStr("tcblife_ec_cond"),
     colNum("tcblife_ec_rate"),
     colNum("eco_fblmt"),
     colStr("extratwpay_cond"),
     colStr("onlyaddon_calcond"),
//     colNum("extratwpay_rate"),
//     colNum("extratwpay_fblmt"),
     colStr("fund_code")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbUpdateMktUploadfileCtlProcFlag(String transSeqno) throws Exception
 {
  strSql= "update mkt_uploadfile_ctl set "
        + " proc_flag = 'Y', "
        + " proc_date = to_char(sysdate,'yyyymmdd') "
        + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     transSeqno
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete " +approveTabName + " " 
         + "where 1 = 1 "
         + "and fund_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDeleteD4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete ptr_fund_data " 
         + "where 1 = 1 "
         + "and table_name  =  'PTR_FUNDP' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, false);
  if (rc!=1) errmsg("刪除 ptr_fund_data 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TBndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete ptr_fund_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'PTR_FUNDP' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, false);
  if (rc!=1) errmsg("刪除 ptr_fund_data_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into ptr_fund_data "
         + "select * "
         + "from  ptr_fund_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'PTR_FUNDP' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("fund_code"), 
    };

  sqlExec(strSql, param,false);

  return 1;
 }
 // ************************************************************************
 public int dbDeleteD4TBnCdata() throws Exception {
   rc = dbSelectS4();
   if (rc != 1)
     return rc;
 strSql = "delete from ptr_fund_cdata_t "
         + "where 1 = 1 "
         + "and table_name  =  'PTR_FUNDP' "
         + "and data_key  = ?  ";

   Object[] param = new Object[] 
   		{
   		colStr("fund_code")
   		};
   
   wp.dupRecord = "Y";
   sqlExec(strSql, param);
   if (rc != 1)
     errmsg("刪除 ptr_fund_cdata_t 錯誤");

   return 1;
 }

 // ************************************************************************
 public int dbInsertA4BnCdata() throws Exception {
   rc = dbSelectS4();
   if (rc != 1)
     return rc;
 strSql = "insert into ptr_fund_cdata "
         + "select * "
         + "from  ptr_fund_cdata_t "
         + "where 1 = 1 "
         + "and table_name  =  'PTR_FUNDP' "
         + "and data_key  = ?  ";

   Object[] param = new Object[] 
   		{
   		   colStr("fund_code"),
   		};

   sqlExec(strSql, param);

   return 1;
 }
 // ************************************************************************
 public int dbDeleteD4BnCdata() throws Exception {
   rc = dbSelectS4();
   if (rc != 1)
       return rc;
   strSql = "delete from ptr_fund_cdata "
           + "where 1 = 1 "
           + "and table_name  =  'PTR_FUNDP' "
           + "and data_key  = ?  ";

   Object[] param = new Object[] 
   		{
   		colStr("fund_code"),
   		};
   
   wp.dupRecord = "Y";
   sqlExec(strSql, param);
   if (rc != 1)
     errmsg("刪除 ptr_fund_cdata 錯誤");

   return 1;
 }
// ************************************************************************
 public int dbDelete() throws Exception
 {
  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("wprowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (sqlRowNum <= 0) 
     {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
     }

  return rc;
 }
// ************************************************************************

}  // End of class
