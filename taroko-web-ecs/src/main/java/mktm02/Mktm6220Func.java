/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/24  V1.01.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.02  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-16  V1.00.03  Zuwei Su       fix issue 覆核狀態: 不可空白         *
* 111/12/22  V1.00.04   Zuwei         輸出sql log                                                                     *
* 112-05-05  V1.00.05  Ryan    新增國內外消費欄位維護，特店中文名稱、特店英文名稱參數維護，[消費回饋比例]區塊新增多個欄位維護   *
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6220Func extends FuncEdit
{
  String kk1;
  String orgControlTabName = "ptr_fundp";
  String controlTabName = "ptr_fundp_t";

 public Mktm6220Func(TarokoCommon wr)
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
  String procTabName="";
  procTabName = wp.itemStr2("control_tab_name");
  if (procTabName.length()==0) return(1);
  strSql= " select "
          + " apr_flag, "
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
          + " new_hldr_days, "
          + " new_group_cond, "
          + " new_hldr_card, "
          + " new_hldr_sup, "
          + " new_card_days, "
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
          + " feedback_cycle_flag, "
          + " card_feed_run_day, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mcht_cname_sel, "
          + " mcht_ename_sel, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno,"
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
          + " onlyaddon_calcond "
//          + " extratwpay_rate,"
//          + " extratwpay_fblmt "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck() 
 {
  if (!this.ibDelete)
     {
      if (wp.colStr("storetype").equals("Y"))
        {
         errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
         return;
        }
     }
  if (this.ibAdd)
     {
      kk1 = wp.itemStr2("fund_code");
      if (empty(kk1))
         {
          errmsg("刷卡金代碼 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr2("fund_code");
     }
  if (this.ibUpdate)
     {
      if ((wp.itemStr2("bin_type_sel").equals("1"))||
          (wp.itemStr2("bin_type_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"2")==0)
             {
              errmsg("[卡別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("acct_type_sel").equals("1"))||
          (wp.itemStr2("acct_type_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"4")==0)
             {
              errmsg("[帳戶類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("group_code_sel").equals("1"))||
          (wp.itemStr2("group_code_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"3")==0)
             {
              errmsg("[團體代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("card_type_sel").equals("1"))||
          (wp.itemStr2("card_type_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"5")==0)
             {
              errmsg("[卡種] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("source_code_sel").equals("1"))||
          (wp.itemStr2("source_code_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"A")==0)
             {
              errmsg("[來源代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("merchant_sel").equals("1"))||
          (wp.itemStr2("merchant_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"1")==0)
             {
              errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("mcht_group_sel").equals("1"))||
          (wp.itemStr2("mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"H")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      
      if ((wp.itemStr2("platform_kind_sel").equals("1"))||
              (wp.itemStr2("platform_kind_sel").equals("2")))
             {
              if (listParmDataCnt("ptr_fund_data_t"
                                    ,"PTR_FUNDP"
                                    ,wp.colStr("fund_code")
                                    ,"P")==0)
                 {
                  errmsg("[一般消費群組] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
      if ((wp.itemStr2("currency_sel").equals("1"))||
          (wp.itemStr2("currency_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"7")==0)
             {
              errmsg("[交易幣別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("ex_currency_sel").equals("1"))||
          (wp.itemStr2("ex_currency_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"9")==0)
             {
              errmsg("[排除幣別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("pos_entry_sel").equals("1"))||
          (wp.itemStr2("pos_entry_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"B")==0)
             {
              errmsg("[POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("pos_merchant_sel").equals("1"))||
          (wp.itemStr2("pos_merchant_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"C")==0)
             {
              errmsg("[特殊特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("pos_mcht_group_sel").equals("1"))||
          (wp.itemStr2("pos_mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"M")==0)
             {
              errmsg("[特殊特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_mcc_code_sel").equals("1"))||
          (wp.itemStr2("d_mcc_code_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"8")==0)
             {
              errmsg("[特店類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_merchant_sel").equals("1"))||
          (wp.itemStr2("d_merchant_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"6")==0)
             {
              errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_mcht_group_sel").equals("1"))||
          (wp.itemStr2("d_mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"K")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_ucaf_sel").equals("1"))||
          (wp.itemStr2("d_ucaf_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"F")==0)
             {
              errmsg("[UCAF] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_eci_sel").equals("1"))||
          (wp.itemStr2("d_eci_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"G")==0)
             {
              errmsg("[ECI] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_pos_entry_sel").equals("1"))||
          (wp.itemStr2("d_pos_entry_sel").equals("2")))
         {
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"E")==0)
             {
              errmsg("[POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr("mcht_cname_sel").equals("1"))||
              (wp.itemStr("mcht_cname_sel").equals("2")))
             {
              if (listParmDataCnt("ptr_fund_cdata_t"
                                    ,"PTR_FUNDP"
                                    ,wp.colStr("fund_code")
                                    ,"A")==0)
                 {
                  errmsg("[特店中文名稱] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
          if ((wp.itemStr("mcht_ename_sel").equals("1"))||
              (wp.itemStr("mcht_ename_sel").equals("2")))
             {
              if (listParmDataCnt("ptr_fund_cdata_t"
                                    ,"PTR_FUNDP"
                                    ,wp.colStr("fund_code")
                                    ,"B")==0)
                 {
                  errmsg("[特店英文名稱] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
     }
  if (!wp.itemStr2("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
  if (!wp.itemStr2("new_hldr_cond").equals("Y")) wp.itemSet("new_hldr_cond","N");
  if (!wp.itemStr2("new_group_cond").equals("Y")) wp.itemSet("new_group_cond","N");
  if (!wp.itemStr2("new_hldr_card").equals("Y")) wp.itemSet("new_hldr_card","N");
  if (!wp.itemStr2("new_hldr_sup").equals("Y")) wp.itemSet("new_hldr_sup","N");
  if (!wp.itemStr2("apply_age_cond").equals("Y")) wp.itemSet("apply_age_cond","N");
  if (!wp.itemStr2("activate_cond").equals("Y")) wp.itemSet("activate_cond","N");
  if (!wp.itemStr2("bl_cond").equals("Y")) wp.itemSet("bl_cond","N");
  if (!wp.itemStr2("ca_cond").equals("Y")) wp.itemSet("ca_cond","N");
  if (!wp.itemStr2("id_cond").equals("Y")) wp.itemSet("id_cond","N");
  if (!wp.itemStr2("ao_cond").equals("Y")) wp.itemSet("ao_cond","N");
  if (!wp.itemStr2("it_cond").equals("Y")) wp.itemSet("it_cond","N");
  if (!wp.itemStr2("ot_cond").equals("Y")) wp.itemSet("ot_cond","N");
  if (!wp.itemStr2("purch_feed_flag").equals("Y")) wp.itemSet("purch_feed_flag","N");
  if (!wp.itemStr2("purch_reclow_cond").equals("Y")) wp.itemSet("purch_reclow_cond","N");
  if (!wp.itemStr2("purch_rec_amt_cond").equals("Y")) wp.itemSet("purch_rec_amt_cond","N");
  if (!wp.itemStr2("purch_tol_amt_cond").equals("Y")) wp.itemSet("purch_tol_amt_cond","N");
  if (!wp.itemStr2("purch_tol_time_cond").equals("Y")) wp.itemSet("purch_tol_time_cond","N");
  if (!wp.itemStr2("fund_feed_flag").equals("Y")) wp.itemSet("fund_feed_flag","N");
  if (!wp.itemStr2("autopay_flag").equals("Y")) wp.itemSet("autopay_flag","N");
  if (!wp.itemStr2("mp_flag").equals("Y")) wp.itemSet("mp_flag","N");
  if (!wp.itemStr2("valid_card_flag").equals("Y")) wp.itemSet("valid_card_flag","N");
  if (!wp.itemStr2("valid_afi_flag").equals("Y")) wp.itemSet("valid_afi_flag","N");
  if (!wp.itemStr2("ebill_flag").equals("Y")) wp.itemSet("ebill_flag","N");
  if (!wp.itemStr2("autopay_digit_cond").equals("Y")) wp.itemSet("autopay_digit_cond","N");
  if (!wp.itemStr2("d_txn_cond").equals("Y")) wp.itemSet("d_txn_cond","N");
  if (!wp.itemStr2("hapcare_trust_cond").equals("Y")) wp.itemSet("hapcare_trust_cond","N");
  if (!wp.itemStr2("housing_endow_cond").equals("Y")) wp.itemSet("housing_endow_cond","N");
  if (!wp.itemStr2("mortgage_cond").equals("Y")) wp.itemSet("mortgage_cond","N");
  if (!wp.itemStr2("util_entrustded_cond").equals("Y")) wp.itemSet("util_entrustded_cond","N");
  if (!wp.itemStr2("twpay_cond").equals("Y")) wp.itemSet("twpay_cond","N");
  if (!wp.itemStr2("tcblife_ec_cond").equals("Y")) wp.itemSet("tcblife_ec_cond","N");
  if (!wp.itemStr2("extratwpay_cond").equals("Y")) wp.itemSet("extratwpay_cond","N");
  if (!wp.itemStr2("onlyaddon_calcond").equals("Y")) wp.itemSet("onlyaddon_calcond","N");

   if (this.ibUpdate)
      {
       if ((wp.itemStr2("new_hldr_cond").equals("Y"))&&
           (wp.itemStr2("new_group_cond").equals("Y")))
          {
           if (wp.colNum("new_group_cond_cnt")==0)
          if (listParmDataCnt("ptr_fund_data_t"
                                ,"PTR_FUNDP"
                                ,wp.colStr("fund_code")
                                ,"0")==0)
              {
               errmsg("[新卡友-未持有團代] 明細沒有設定, 筆數不可為 0  !");
               return;
              }
          }
      }

   wp.colSet("feedback_cycle_flag", "2");
   wp.colSet("card_feed_run_day" , "1");
   wp.itemSet("card_feed_run_day" , "1");

   if (wp.itemStr2("aud_type").equals("A"))
      {
       if (wp.itemStr2("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }
   else
      {
       if (wp.itemStr2("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }
   if (wp.itemEmpty("apr_flag")) {
       wp.colSet("apr_flag", "N");
       wp.itemSet("apr_flag", "N");
   }

   if ((this.ibDelete)||
       (wp.itemStr2("aud_type").equals("D"))) return;

  if ((!wp.itemStr2("control_tab_name").equals(orgControlTabName))&&
      (wp.itemStr2("aud_type").equals("A")))

    {

  if (wp.itemStr2("fund_code").length()<4)
     {
      errmsg("刷卡金代碼長度至少要4碼!");
      return;
     }
      strSql = "select type_name "
             + " from vmkt_fund_name "
             + " where fund_code =  ? "
             ;
      Object[] param = new Object[] {wp.itemStr2("fund_code")};
      sqlSelect(strSql,param);

      if (sqlRowNum > 0)
         {
          errmsg("["+colStr("type_name")+"] 已使用本刷卡金代碼!");
          return;
         }
  strSql = "select payment_type "
         + " from ptr_payment "
         + " where payment_type =  ? "
         ;
  param = new Object[] {wp.itemStr2("fund_code").substring(0,4)};
  sqlSelect(strSql,param);

  if (sqlRowNum <= 0)
     {
      errmsg("刷卡金前4碼在ptrm0030繳款類別參數查無資料！");
      return;
     }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if ((wp.itemStr2("autopay_flag").equals("Y"))&&
          (wp.itemStr2("autopay_digit_cond").equals("S")))
         {
          errmsg("[其它回饋條件] 本行自轉戶與本行數位帳戶自扣 只可二擇一!");
          return;
         }
      if ((wp.itemStr2("valid_period").equals("E"))||
          (wp.itemStr2("valid_period").equals("S")))
         {
          if (wp.itemStr2("cobrand_code").length()==0)
             {
              errmsg("[聯名主代碼] 必須輸入!");
              return;
             }
         }
      if ((!wp.itemStr2("fund_feed_flag").equals("Y"))&&
          (!wp.itemStr2("purch_feed_flag").equals("Y")))
         {
          errmsg("[使用消費門檻回饋][使用刷卡金金額回饋] 至少要選取一項!");
          return;
         }
      if ((wp.itemStr2("stop_flag").equals("Y"))&&
          (wp.itemStr2("stop_date").length()==0))
         {
          errmsg("[取消日期] 取消日期必須輸入!");
          return;
         }
      if (wp.itemStr2("new_hldr_cond").equals("Y"))
         {
          if (wp.itemStr2("new_hldr_flag").equals("1"))
             {
              if (wp.itemStr2("new_card_days").length()==0) wp.itemSet("new_card_days" , "0");
              if (wp.itemNum("new_card_days")==0)
                 {
                  errmsg("[新卡友判斷] 全新卡友核卡日期前 n 日必須大於 0!");
                  return;
                 }
             }
          else
             {
              if (wp.itemStr2("new_group_cond").equals("Y"))
              if ((!wp.itemStr2("new_hldr_card").equals("Y"))&&
                 (!wp.itemStr2("new_hldr_sip").equals("Y")))
                 {
                  errmsg("[新卡友判斷] 正卡,附卡必須擇一勾選!");
                  return;
                 }
              if (wp.itemStr2("new_hldr_days").length()==0) wp.itemSet("new_hldr_days" , "0");
              if (wp.itemNum("new_hldr_days")==0)
                 {
                  errmsg("[新卡友判斷] 核卡日期前 n 日必須大於 0!");
                  return;
                 }
             }
         }
      if (wp.itemStr2("apply_age_cond").equals("Y"))
         {
          if (wp.itemStr2("apply_age_s").length()==0) wp.itemSet("apply_age_s" , "0");
          if (wp.itemStr2("apply_age_e").length()==0) wp.itemSet("apply_age_e" , "0");
          if ((wp.itemNum("apply_age_s")==0)||
              (wp.itemNum("apply_age_e")==0))
             {
              errmsg("[正卡年齡判斷] 年齡起迄必須大於 0!");
              return;
             }
         }
      if ((!wp.itemStr2("bl_cond").equals("Y"))&&
          (!wp.itemStr2("ot_cond").equals("Y"))&&
          (!wp.itemStr2("it_cond").equals("Y"))&&
          (!wp.itemStr2("ca_cond").equals("Y"))&&
          (!wp.itemStr2("id_cond").equals("Y"))&&
          (!wp.itemStr2("ao_cond").equals("Y")))
         {
          errmsg("[消費本金類] 至少要選一個!");
          return;
         }
      if (wp.itemStr2("purch_feed_flag").equals("Y"))
         {
/*
          if ((wp.item_ss("purch_date_s").length()==0)||
              (wp.item_ss("purch_date_e").length()==0))
             {
              errmsg("[消費期間]　消費日期起迄必須輸入!");
              return;
             }
*/
          if ((!wp.itemStr2("purch_rec_amt_cond").equals("Y"))&&
              (!wp.itemStr2("purch_tol_amt_cond").equals("Y"))&&
              (!wp.itemStr2("purch_tol_time_cond").equals("Y")))
             {
              errmsg("[消費期間] 消費門檻2 至少要選一個!");
              return;
             }
          if (wp.itemStr2("purch_rec_amt_cond").equals("Y"))
             {
              if (wp.itemStr2("purch_rec_amt").length()==0) wp.itemSet("purch_rec_amt" , "0");
              if (wp.itemNum("purch_rec_amt")==0)
                 {
                  errmsg("[消費期間] 單筆金額 必須大於 0!");
                  return;
                 }
             }
          if (wp.itemStr2("purch_tol_amt_cond").equals("Y"))
             {
              if (wp.itemStr2("purch_tol_amt").length()==0) wp.itemSet("purch_tol_amt" , "0");
              if (wp.itemNum("purch_tol_amt")==0)
                 {
                  errmsg("[消費期間] 累計金額 必須大於 0!");
                  return;
                 }
             }
          if (wp.itemStr2("purch_tol_time_cond").equals("Y"))
             {
              if (wp.itemStr2("purch_tol_time").length()==0) wp.itemSet("purch_tol_time" , "0");
              if (wp.itemNum("purch_tol_time")==0)
                 {
                  errmsg("[消費期間] 累計次數 必須大於 0!");
                  return;
                 }
             }
          if (wp.itemStr2("purch_type").equals("1"))
             {
              if (wp.itemStr2("purch_feed_amt").length()==0) wp.itemSet("purch_feed_amt" , "0");
              if (wp.itemNum("purch_feed_amt")==0)
                 {
                  errmsg("[符合消費門檻] 回饋金額 必須大於 0!");
                  return;
                 }
             }
          if (wp.itemStr2("purch_type").equals("2"))
             {
              if (wp.itemStr2("purch_feed_rate").length()==0) wp.itemSet("purch_feed_rate" , "0");
              if (wp.itemNum("purch_feed_rate")==0)
                 {
                  errmsg("[符合消費門檻] 回饋比例 必須大於 0!");
                  return;
                 }
             }
         }   
      if ((wp.itemStr2("program_exe_type").equals("1"))&&
          (wp.itemStr2("unlimit_start_month").length()==0))
         {
          errmsg("[回饋(產生)期間] 起始年月 必須輸入!");
          return;
         }
      if (wp.itemStr2("program_exe_type").equals("2"))
         {
          if ((wp.itemStr2("cal_s_month").length()==0)||
              (wp.itemStr2("cal_e_month").length()==0))
             {
              errmsg("[回饋(產生)期間] 一段期間起迄 必須輸入!");
              return;
             }
         }
      if (wp.itemStr2("program_exe_type").equals("3"))
         {
          if ((wp.itemStr2("card_feed_date_s").length()==0)||
              (wp.itemStr2("card_feed_date_e").length()==0))
             {
              errmsg("[回饋(產生)期間] 發卡條件 ～發卡期間起迄 必須輸入!");
              return;
             }
          if (wp.itemStr2("card_feed_flag").equals("1"))
             {
              if (wp.itemStr2("cal_months").length()==0) wp.itemSet("cal_months" , "0");
              if (wp.itemNum("cal_months")==0)
                 {
                  errmsg("[符合消費門檻] 發卡後N1月內(含發卡當月) 必須大於 0!");
                  return;
                 }
             }
          if (wp.itemStr2("card_feed_flag").equals("2"))
             {
              if (wp.itemStr2("card_feed_months2").length()==0) wp.itemSet("card_feed_months2" , "0");
              if (wp.itemNum("card_feed_months2")==0)
                 {
                  errmsg("[符合消費門檻] 發卡N2個月後 必須大於 0!");
                  return;
                 }
             }
          if (wp.itemStr2("card_feed_flag").equals("3"))
             {
              if (wp.itemStr2("card_feed_days").length()==0) wp.itemSet("card_feed_days" , "0");
              if (wp.itemNum("card_feed_days")==0)
                 {
                  errmsg("[符合消費門檻] 發卡N3天後 必須大於 0!");
                  return;
                 }
             }
         }
      if (wp.itemStr2("feedback_type").equals("1"))
         {
          if (wp.itemStr2("card_feed_run_day").length()==0) wp.itemSet("card_feed_run_day" , "0");
          if (wp.itemStr2("feedback_months").length()==0) wp.itemSet("feedback_months" , "0");
          if ((wp.itemNum("card_feed_run_day")==0)||
              (wp.itemNum("feedback_months")==0))
             {
              errmsg("[feedback_months] 每月n日執行，每n月執行一次  必須大於 0!");
              return;
             }
         }
     }
  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("fund_crt_date_s")&&(!wp.itemEmpty("FUND_CRT_DATE_E")))
      if (wp.itemStr2("fund_crt_date_s").compareTo(wp.itemStr2("FUND_CRT_DATE_E"))>0)
         {
          errmsg("刷卡金產生期間：["+wp.itemStr2("fund_crt_date_s")+"]>["+wp.itemStr2("FUND_CRT_DATE_E")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("apply_age_s").length()==0)
          wp.itemSet("apply_age_s","0");
      if (wp.itemStr2("APPLY_AGE_E").length()==0)
          wp.itemSet("APPLY_AGE_E","0");
      if (Double.parseDouble(wp.itemStr2("apply_age_s"))>Double.parseDouble(wp.itemStr2("APPLY_AGE_E"))&&
          (Double.parseDouble(wp.itemStr2("APPLY_AGE_E"))!=0))
         {
          errmsg("區間申請時年齡(足歲)("+wp.itemStr2("apply_age_s")+ ")~(" + wp.itemStr2("APPLY_AGE_E")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("purch_date_s")&&(!wp.itemEmpty("purch_datE_e")))
      if (wp.itemStr2("purch_date_s").compareTo(wp.itemStr2("purch_datE_e"))>0)
         {
          errmsg("消費期間：　消費日期：["+wp.itemStr2("purch_date_s")+"]>["+wp.itemStr2("purch_datE_e")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_s_amt_1").length()==0)
          wp.itemSet("fund_s_amt_1","0");
      if (wp.itemStr2("FUND_E_AMT_1").length()==0)
          wp.itemSet("FUND_E_AMT_1","0");
      if (Double.parseDouble(wp.itemStr2("fund_s_amt_1"))>=Double.parseDouble(wp.itemStr2("FUND_E_AMT_1"))&&
          (Double.parseDouble(wp.itemStr2("FUND_E_AMT_1"))!=0))
         {
          errmsg("區間1:("+wp.itemStr2("fund_s_amt_1")+ ")~(" + wp.itemStr2("FUND_E_AMT_1")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_e_amt_1").length()==0)
          wp.itemSet("fund_e_amt_1","0");
      if (wp.itemStr2("fund_s_amt_2").length()==0)
          wp.itemSet("fund_s_amt_2","0");
      if (Double.parseDouble(wp.itemStr2("fund_e_amt_1"))>=Double.parseDouble(wp.itemStr2("fund_s_amt_2"))&&
          (Double.parseDouble(wp.itemStr2("fund_s_amt_2"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr2("fund_e_amt_1")+ ")~(" + wp.itemStr2("fund_s_amt_2")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_s_amt_2").length()==0)
          wp.itemSet("fund_s_amt_2","0");
      if (wp.itemStr2("FUND_E_AMT_2").length()==0)
          wp.itemSet("FUND_E_AMT_2","0");
      if (Double.parseDouble(wp.itemStr2("fund_s_amt_2"))>=Double.parseDouble(wp.itemStr2("FUND_E_AMT_2"))&&
          (Double.parseDouble(wp.itemStr2("FUND_E_AMT_2"))!=0))
         {
          errmsg("區間2:("+wp.itemStr2("fund_s_amt_2")+ ")~(" + wp.itemStr2("FUND_E_AMT_2")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_e_amt_2").length()==0)
          wp.itemSet("fund_e_amt_2","0");
      if (wp.itemStr2("FUND_S_AMT_3").length()==0)
          wp.itemSet("FUND_S_AMT_3","0");
      if (Double.parseDouble(wp.itemStr2("fund_e_amt_2"))>=Double.parseDouble(wp.itemStr2("FUND_S_AMT_3"))&&
          (Double.parseDouble(wp.itemStr2("FUND_S_AMT_3"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr2("fund_e_amt_2")+ ")~(" + wp.itemStr2("FUND_S_AMT_3")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_s_amt_3").length()==0)
          wp.itemSet("fund_s_amt_3","0");
      if (wp.itemStr2("FUND_E_AMT_3").length()==0)
          wp.itemSet("FUND_E_AMT_3","0");
      if (Double.parseDouble(wp.itemStr2("fund_s_amt_3"))>=Double.parseDouble(wp.itemStr2("FUND_E_AMT_3"))&&
          (Double.parseDouble(wp.itemStr2("FUND_E_AMT_3"))!=0))
         {
          errmsg("區間3:("+wp.itemStr2("fund_s_amt_3")+ ")~(" + wp.itemStr2("FUND_E_AMT_3")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_e_amt_3").length()==0)
          wp.itemSet("fund_e_amt_3","0");
      if (wp.itemStr2("FUND_S_AMT_4").length()==0)
          wp.itemSet("FUND_S_AMT_4","0");
      if (Double.parseDouble(wp.itemStr2("fund_e_amt_3"))>=Double.parseDouble(wp.itemStr2("FUND_S_AMT_4"))&&
          (Double.parseDouble(wp.itemStr2("FUND_S_AMT_4"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr2("fund_e_amt_3")+ ")~(" + wp.itemStr2("FUND_S_AMT_4")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_s_amt_4").length()==0)
          wp.itemSet("fund_s_amt_4","0");
      if (wp.itemStr2("FUND_E_AMT_4").length()==0)
          wp.itemSet("FUND_E_AMT_4","0");
      if (Double.parseDouble(wp.itemStr2("fund_s_amt_4"))>=Double.parseDouble(wp.itemStr2("FUND_E_AMT_4"))&&
          (Double.parseDouble(wp.itemStr2("FUND_E_AMT_4"))!=0))
         {
          errmsg("區間4:("+wp.itemStr2("fund_s_amt_4")+ ")~(" + wp.itemStr2("FUND_E_AMT_4")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_e_amt_4").length()==0)
          wp.itemSet("fund_e_amt_4","0");
      if (wp.itemStr2("FUND_S_AMT_5").length()==0)
          wp.itemSet("FUND_S_AMT_5","0");
      if (Double.parseDouble(wp.itemStr2("fund_e_amt_4"))>=Double.parseDouble(wp.itemStr2("FUND_S_AMT_5"))&&
          (Double.parseDouble(wp.itemStr2("FUND_S_AMT_5"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr2("fund_e_amt_4")+ ")~(" + wp.itemStr2("FUND_S_AMT_5")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("fund_s_amt_5").length()==0)
          wp.itemSet("fund_s_amt_5","0");
      if (wp.itemStr2("FUND_E_AMT_5").length()==0)
          wp.itemSet("FUND_E_AMT_5","0");
      if (Double.parseDouble(wp.itemStr2("fund_s_amt_5"))>=Double.parseDouble(wp.itemStr2("FUND_E_AMT_5"))&&
          (Double.parseDouble(wp.itemStr2("FUND_E_AMT_5"))!=0))
         {
          errmsg("區間5:("+wp.itemStr2("fund_s_amt_5")+ ")~(" + wp.itemStr2("FUND_E_AMT_5")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("cal_s_month")&&(!wp.itemEmpty("cak_e_month")))
      if (wp.itemStr2("cal_s_month").compareTo(wp.itemStr2("cak_e_month"))>0)
         {
          errmsg("：["+wp.itemStr2("cal_s_month")+"]>["+wp.itemStr2("cak_e_month")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("card_feed_date_s")&&(!wp.itemEmpty("card_feed_date_e")))
      if (wp.itemStr2("card_feed_date_s").compareTo(wp.itemStr2("card_feed_date_e"))>0)
         {
          errmsg("～發卡期間：["+wp.itemStr2("card_feed_date_s")+"]>["+wp.itemStr2("card_feed_date_e")+"] 起迄值錯誤!");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr2("purch_reclow_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("purch_rec_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("purch_tol_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("purch_tol_time"),12,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[12]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("purch_feed_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("回饋金額: 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("回饋金額: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("回饋金額: 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("purch_feed_rate"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("回饋比例: 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg("回饋比例: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("回饋比例: 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_s_amt_1"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　1. 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("　1. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　1. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_e_amt_1"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_rate_1"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_amt_1"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg("　　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_s_amt_2"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　2. 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("　2. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　2. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_e_amt_2"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_rate_2"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_amt_2"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg("　　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_s_amt_3"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　3. 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("　3. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　3. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_e_amt_3"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_rate_3"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_amt_3"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg("　　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_s_amt_4"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　4. 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("　4. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　4. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_e_amt_4"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_rate_4"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_amt_4"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg("　　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_s_amt_5"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　5. 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("　5. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　5. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_e_amt_5"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_rate_5"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("fund_amt_5"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg("　　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("rc_sub_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("加贈刷卡金：卡友當月使用RC>= 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("加贈刷卡金：卡友當月使用RC>= 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("加贈刷卡金：卡友當月使用RC>= 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("rc_sub_rate"),4,5);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[4]位 小數[5]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("feedback_lmt"),12,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("回饋上限： 格式超出範圍 : 整數[12]位 小數[2]位");
      if (checkInt==2) 
         errmsg("回饋上限： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("回饋上限： 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_txn_amt"),12,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[12]位 小數[2]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("cancel_high_amt"),12,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("每次抵用上限： 格式超出範圍 : 整數[12]位 小數[2]位");
      if (checkInt==2) 
         errmsg("每次抵用上限： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("每次抵用上限： 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("tran_base"))
     {
      errmsg("產生對象： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("valid_period"))
     {
      errmsg("聯名主刷卡金類型: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("cancel_s_month"))
     {
      errmsg("抵用起始月份： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate)) {
	  if(wp.itemEq("hapcare_trust_cond", "Y")) {
		  if(wp.itemNum("hapcare_trust_rate")==0) {
			  errmsg("請輸入安養信託加碼回饋率");
			  return;
		  }
	  }
	  if(wp.itemEq("housing_endow_cond", "Y")) {
		  if(wp.itemNum("housing_endow_rate")==0) {
			  errmsg("請輸入安養信託加碼回饋率");
			  return;
		  }
	  }
	  if(wp.itemEq("hapcare_trust_cond", "Y") && wp.itemEq("housing_endow_cond", "Y")) {
		  if(wp.itemNum("happycare_fblmt")<0) {
			  errmsg("請輸入樂活安養加碼回饋上限為0或正整數");
			  return;
		  }
	  }
	  if(wp.itemEq("mortgage_cond", "Y")) {
		  if(wp.itemNum("mortgag_rate")==0) {
			  errmsg("請輸入房貸繳息正常加碼回饋率");
			  return;
		  }
		  if(wp.itemNum("mortgage_fblmt")<0) {
			  errmsg("請輸入房貸繳息正常加碼回饋上限為0或正整數");
			  return;
		  }
	  }
	  if(wp.itemEq("util_entrustded_cond", "Y")) {
		  if(wp.itemNum("util_entrustded_rate")==0) {
			  errmsg("請輸入公共事業委扣代繳加碼回饋率");
			  return;
		  }
		  if(wp.itemNum("util_entrustded_fblmt")<0) {
			  errmsg("請輸入公共事業委扣代繳加碼回饋上限為0或正整數");
			  return;
		  }
	  }
	  if(wp.itemEq("twpay_cond", "Y")) {
		  if(wp.itemNum("twpay_rate")==0) {
			  errmsg("請輸入台灣Pay加碼回饋率");
			  return;
		  }
	  }
	  if(wp.itemEq("tcblife_ec_cond", "Y")) {
		  if(wp.itemNum("tcblife_ec_rate")==0) {
			  errmsg("請輸入合庫人壽網路投保加碼回饋回饋率");
			  return;
		  }
	  }
	  if(wp.itemEq("twpay_cond", "Y") && wp.itemEq("tcblife_ec_cond", "Y")) {
		  if(wp.itemNum("eco_fblmt")<0) {
			  errmsg("請輸入樂活環保加碼回饋上限為0或正整數");
			  return;
		  }
	  }
//	  if(wp.itemEq("extratwpay_cond", "Y")) {
//		  if(wp.itemNum("extratwpay_rate")==0) {
//			  errmsg("請輸入額外台灣Pay加碼回饋率");
//			  return;
//		  }
//		  if(wp.itemNum("extratwpay_fblmt")<0) {
//			  errmsg("請輸入額外台灣Pay加碼回饋上限為0或正整數");
//			  return;
//		  }
//	  }
  }

  if (this.isAdd()) return;

  if (this.ibDelete)
     {
      wp.colSet("storetype" , "N");
     }
 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;

  dbInsertD2T();
  dbInsertI2T();
  dbInsertD5T();
  dbInsertI5T();

  strSql= " insert into  " + controlTabName+ " ("
          + " fund_code, "
          + " apr_flag, "
          + " aud_type, "
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
          + " new_hldr_days, "
          + " new_group_cond, "
          + " new_hldr_card, "
          + " new_hldr_sup, "
          + " new_card_days, "
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
          + " feedback_months, "
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
          + " feedback_cycle_flag, "
          + " card_feed_run_day, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm,"
          + " foreign_code,"
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
          + "'2',?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?,?,?,?,"
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("apr_flag"),
        wp.itemStr2("aud_type"),
        wp.itemStr2("fund_name"),
        wp.itemStr2("tran_base"),
        wp.itemStr2("fund_crt_date_s"),
        wp.itemStr2("fund_crt_date_e"),
        wp.itemStr2("effect_type"),
        wp.itemNum("effect_months"),
        wp.itemNum("effect_years"),
        wp.itemNum("effect_fix_month"),
        wp.itemStr2("stop_flag"),
        wp.itemStr2("stop_date"),
        wp.itemStr2("stop_desc"),
        wp.itemStr2("bin_type_sel"),
        wp.itemStr2("acct_type_sel"),
        wp.itemStr2("group_code_sel"),
        wp.itemStr2("card_type_sel"),
        wp.itemStr2("new_hldr_cond"),
        wp.itemStr2("new_hldr_flag"),
        wp.itemNum("new_hldr_days"),
        wp.itemStr2("new_group_cond"),
        wp.itemStr2("new_hldr_card"),
        wp.itemStr2("new_hldr_sup"),
        wp.itemNum("new_card_days"),
        wp.itemStr2("apply_age_cond"),
        wp.itemNum("apply_age_s"),
        wp.itemNum("apply_age_e"),
        wp.itemStr2("activate_cond"),
        wp.itemStr2("activate_flag"),
        wp.itemStr2("valid_period"),
        wp.itemStr2("cobrand_code"),
        wp.itemStr2("source_code_sel"),
        wp.itemStr2("merchant_sel"),
        wp.itemStr2("mcht_group_sel"),
        wp.itemStr2("platform_kind_sel"),
        wp.itemStr2("currency_sel"),
        wp.itemStr2("ex_currency_sel"),
        wp.itemStr2("pos_entry_sel"),
        wp.itemStr2("pos_merchant_sel"),
        wp.itemStr2("pos_mcht_group_sel"),
        wp.itemStr2("bl_cond"),
        wp.itemStr2("ca_cond"),
        wp.itemStr2("id_cond"),
        wp.itemStr2("ao_cond"),
        wp.itemStr2("it_cond"),
        wp.itemStr2("ot_cond"),
        wp.itemStr2("purch_feed_flag"),
        wp.itemStr2("purch_date_s"),
        wp.itemStr2("purch_date_e"),
        wp.itemStr2("purch_reclow_cond"),
        wp.itemNum("purch_reclow_amt"),
        wp.itemStr2("purch_rec_amt_cond"),
        wp.itemNum("purch_rec_amt"),
        wp.itemStr2("purch_tol_amt_cond"),
        wp.itemNum("purch_tol_amt"),
        wp.itemStr2("purch_tol_time_cond"),
        wp.itemNum("purch_tol_time"),
        wp.itemStr2("purch_feed_type"),
        wp.itemStr2("purch_type"),
        wp.itemNum("purch_feed_amt"),
        wp.itemNum("purch_feed_rate"),
        wp.itemStr2("fund_feed_flag"),
        wp.itemStr2("threshold_sel"),
        wp.itemStr2("purchase_type_sel"),
        wp.itemNum("fund_s_amt_1"),
        wp.itemNum("fund_e_amt_1"),
        wp.itemNum("fund_rate_1"),
        wp.itemNum("fund_amt_1"),
        wp.itemNum("fund_s_amt_2"),
        wp.itemNum("fund_e_amt_2"),
        wp.itemNum("fund_rate_2"),
        wp.itemNum("fund_amt_2"),
        wp.itemNum("fund_s_amt_3"),
        wp.itemNum("fund_e_amt_3"),
        wp.itemNum("fund_rate_3"),
        wp.itemNum("fund_amt_3"),
        wp.itemNum("fund_s_amt_4"),
        wp.itemNum("fund_e_amt_4"),
        wp.itemNum("fund_rate_4"),
        wp.itemNum("fund_amt_4"),
        wp.itemNum("fund_s_amt_5"),
        wp.itemNum("fund_e_amt_5"),
        wp.itemNum("fund_rate_5"),
        wp.itemNum("fund_amt_5"),
        wp.itemNum("rc_sub_amt"),
        wp.itemNum("rc_sub_rate"),
        wp.itemStr2("program_exe_type"),
        wp.itemStr2("unlimit_start_month"),
        wp.itemStr2("cal_s_month"),
        wp.itemStr2("cal_e_month"),
        wp.itemStr2("card_feed_date_s"),
        wp.itemStr2("card_feed_date_e"),
        wp.itemStr2("card_feed_flag"),
        wp.itemNum("cal_months"),
        wp.itemNum("card_feed_months2"),
        wp.itemNum("card_feed_days"),
        wp.itemStr2("new_hldr_sel"),
        wp.itemStr2("feedback_type"),
        wp.itemNum("feedback_months"),
        wp.itemNum("feedback_lmt"),
        wp.itemNum("purch_feed_times"),
        wp.itemStr2("autopay_flag"),
        wp.itemStr2("mp_flag"),
        wp.itemStr2("valid_card_flag"),
        wp.itemStr2("valid_afi_flag"),
        wp.itemStr2("ebill_flag"),
        wp.itemStr2("autopay_digit_cond"),
        wp.itemStr2("d_txn_cond"),
        wp.itemNum("d_txn_amt"),
        wp.itemStr2("cancel_period"),
        wp.itemStr2("cancel_s_month"),
        wp.itemStr2("cancel_scope"),
        wp.itemStr2("d_mcc_code_sel"),
        wp.itemStr2("d_merchant_sel"),
        wp.itemStr2("d_mcht_group_sel"),
        wp.itemStr2("d_ucaf_sel"),
        wp.itemStr2("d_eci_sel"),
        wp.itemStr2("d_pos_entry_sel"),
        wp.itemStr2("cancel_event"),
        wp.itemNum("min_mcode"),
        wp.itemNum("cancel_high_amt"),
        wp.colStr("card_feed_run_day"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm(),
        wp.itemStr2("foreign_code"),
        wp.itemStr2("mcht_cname_sel"),
        wp.itemStr2("mcht_ename_sel"),
        wp.itemStr2("hapcare_trust_cond"),
        wp.itemNum("hapcare_trust_rate"),
        wp.itemStr2("housing_endow_cond"),
        wp.itemNum("housing_endow_rate"),
        wp.itemNum("happycare_fblmt"),
        wp.itemStr2("mortgage_cond"),
        wp.itemNum("mortgag_rate"),
        wp.itemNum("mortgage_fblmt"),
        wp.itemStr2("util_entrustded_cond"),
        wp.itemNum("util_entrustded_rate"),
        wp.itemNum("util_entrustded_fblmt"),
        wp.itemStr2("twpay_cond"),
        wp.itemNum("twpay_rate"),
        wp.itemStr2("tcblife_ec_cond"),
        wp.itemNum("tcblife_ec_rate"),
        wp.itemNum("eco_fblmt"),
        wp.itemStr2("extratwpay_cond"),
        wp.itemStr2("onlyaddon_calcond")
//        wp.itemNum("extratwpay_rate"),
//        wp.itemNum("extratwpay_fblmt")
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI2T() 
 {
   msgOK();

  strSql = "insert into PTR_FUND_DATA_T "
         + "select * "
         + "from PTR_FUND_DATA "
         + "where table_name  =  'PTR_FUNDP' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr2("fund_code"),
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , true);


   return 1;
 }
//************************************************************************
public int dbInsertI5T()
{
 msgOK();

strSql = "insert into PTR_FUND_CDATA_T "
       + "select * "
       + "from PTR_FUND_CDATA "
       + "where table_name  =  'PTR_FUNDP' "
       + "and   data_key = ? "
       + "";

 Object[] param =new Object[]
   {
    wp.itemStr2("fund_code"),
   };

wp.dupRecord = "Y";
sqlExec(strSql, param , false);


 return 1;
}
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "apr_flag = ?, "
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
         + "new_hldr_days = ?, "
         + "new_group_cond = ?, "
         + "new_hldr_card = ?, "
         + "new_hldr_sup = ?, "
         + "new_card_days = ?, "
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
         + "feedback_months = ?, "
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
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ?, "
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
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("apr_flag"),
     wp.itemStr2("fund_name"),
     wp.itemStr2("tran_base"),
     wp.itemStr2("fund_crt_date_s"),
     wp.itemStr2("fund_crt_date_e"),
     wp.itemStr2("effect_type"),
     wp.itemNum("effect_months"),
     wp.itemNum("effect_years"),
     wp.itemNum("effect_fix_month"),
     wp.itemStr2("stop_flag"),
     wp.itemStr2("stop_date"),
     wp.itemStr2("stop_desc"),
     wp.itemStr2("bin_type_sel"),
     wp.itemStr2("acct_type_sel"),
     wp.itemStr2("group_code_sel"),
     wp.itemStr2("card_type_sel"),
     wp.itemStr2("new_hldr_cond"),
     wp.itemStr2("new_hldr_flag"),
     wp.itemNum("new_hldr_days"),
     wp.itemStr2("new_group_cond"),
     wp.itemStr2("new_hldr_card"),
     wp.itemStr2("new_hldr_sup"),
     wp.itemNum("new_card_days"),
     wp.itemStr2("apply_age_cond"),
     wp.itemNum("apply_age_s"),
     wp.itemNum("apply_age_e"),
     wp.itemStr2("activate_cond"),
     wp.itemStr2("activate_flag"),
     wp.itemStr2("valid_period"),
     wp.itemStr2("cobrand_code"),
     wp.itemStr2("source_code_sel"),
     wp.itemStr2("merchant_sel"),
     wp.itemStr2("mcht_group_sel"),
     wp.itemStr2("platform_kind_sel"),
     wp.itemStr2("currency_sel"),
     wp.itemStr2("ex_currency_sel"),
     wp.itemStr2("pos_entry_sel"),
     wp.itemStr2("pos_merchant_sel"),
     wp.itemStr2("pos_mcht_group_sel"),
     wp.itemStr2("bl_cond"),
     wp.itemStr2("ca_cond"),
     wp.itemStr2("id_cond"),
     wp.itemStr2("ao_cond"),
     wp.itemStr2("it_cond"),
     wp.itemStr2("ot_cond"),
     wp.itemStr2("purch_feed_flag"),
     wp.itemStr2("purch_date_s"),
     wp.itemStr2("purch_date_e"),
     wp.itemStr2("purch_reclow_cond"),
     wp.itemNum("purch_reclow_amt"),
     wp.itemStr2("purch_rec_amt_cond"),
     wp.itemNum("purch_rec_amt"),
     wp.itemStr2("purch_tol_amt_cond"),
     wp.itemNum("purch_tol_amt"),
     wp.itemStr2("purch_tol_time_cond"),
     wp.itemNum("purch_tol_time"),
     wp.itemStr2("purch_feed_type"),
     wp.itemStr2("purch_type"),
     wp.itemNum("purch_feed_amt"),
     wp.itemNum("purch_feed_rate"),
     wp.itemStr2("fund_feed_flag"),
     wp.itemStr2("threshold_sel"),
     wp.itemStr2("purchase_type_sel"),
     wp.itemNum("fund_s_amt_1"),
     wp.itemNum("fund_e_amt_1"),
     wp.itemNum("fund_rate_1"),
     wp.itemNum("fund_amt_1"),
     wp.itemNum("fund_s_amt_2"),
     wp.itemNum("fund_e_amt_2"),
     wp.itemNum("fund_rate_2"),
     wp.itemNum("fund_amt_2"),
     wp.itemNum("fund_s_amt_3"),
     wp.itemNum("fund_e_amt_3"),
     wp.itemNum("fund_rate_3"),
     wp.itemNum("fund_amt_3"),
     wp.itemNum("fund_s_amt_4"),
     wp.itemNum("fund_e_amt_4"),
     wp.itemNum("fund_rate_4"),
     wp.itemNum("fund_amt_4"),
     wp.itemNum("fund_s_amt_5"),
     wp.itemNum("fund_e_amt_5"),
     wp.itemNum("fund_rate_5"),
     wp.itemNum("fund_amt_5"),
     wp.itemNum("rc_sub_amt"),
     wp.itemNum("rc_sub_rate"),
     wp.itemStr2("program_exe_type"),
     wp.itemStr2("unlimit_start_month"),
     wp.itemStr2("cal_s_month"),
     wp.itemStr2("cal_e_month"),
     wp.itemStr2("card_feed_date_s"),
     wp.itemStr2("card_feed_date_e"),
     wp.itemStr2("card_feed_flag"),
     wp.itemNum("cal_months"),
     wp.itemNum("card_feed_months2"),
     wp.itemNum("card_feed_days"),
     wp.itemStr2("new_hldr_sel"),
     wp.itemStr2("feedback_type"),
     wp.itemNum("feedback_months"),
     wp.itemNum("feedback_lmt"),
     wp.itemNum("purch_feed_times"),
     wp.itemStr2("autopay_flag"),
     wp.itemStr2("mp_flag"),
     wp.itemStr2("valid_card_flag"),
     wp.itemStr2("valid_afi_flag"),
     wp.itemStr2("ebill_flag"),
     wp.itemStr2("autopay_digit_cond"),
     wp.itemStr2("d_txn_cond"),
     wp.itemNum("d_txn_amt"),
     wp.itemStr2("cancel_period"),
     wp.itemStr2("cancel_s_month"),
     wp.itemStr2("cancel_scope"),
     wp.itemStr2("d_mcc_code_sel"),
     wp.itemStr2("d_merchant_sel"),
     wp.itemStr2("d_mcht_group_sel"),
     wp.itemStr2("d_ucaf_sel"),
     wp.itemStr2("d_eci_sel"),
     wp.itemStr2("d_pos_entry_sel"),
     wp.itemStr2("cancel_event"),
     wp.itemNum("min_mcode"),
     wp.itemNum("cancel_high_amt"),
     wp.loginUser,
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemStr2("foreign_code"),
     wp.itemStr("mcht_cname_sel"),
     wp.itemStr("mcht_ename_sel"),
     wp.itemStr2("hapcare_trust_cond"),
     wp.itemNum("hapcare_trust_rate"),
     wp.itemStr2("housing_endow_cond"),
     wp.itemNum("housing_endow_rate"),
     wp.itemNum("happycare_fblmt"),
     wp.itemStr2("mortgage_cond"),
     wp.itemNum("mortgag_rate"),
     wp.itemNum("mortgage_fblmt"),
     wp.itemStr2("util_entrustded_cond"),
     wp.itemNum("util_entrustded_rate"),
     wp.itemNum("util_entrustded_fblmt"),
     wp.itemStr2("twpay_cond"),
     wp.itemNum("twpay_rate"),
     wp.itemStr2("tcblife_ec_cond"),
     wp.itemNum("tcblife_ec_rate"),
     wp.itemNum("eco_fblmt"),
     wp.itemStr2("extratwpay_cond"),
     wp.itemStr2("onlyaddon_calcond"),
//     wp.itemNum("extratwpay_rate"),
//     wp.itemNum("extratwpay_fblmt"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  dbInsertD2T();
  dbInsertD5T();

  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
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
 public int dbInsertD2T() 
 {
   msgOK();

   strSql = "delete PTR_FUND_DATA_T "
         + " where table_name  =  'PTR_FUNDP' "
          + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr2("fund_code"),
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 PTR_FUND_DATA_T 錯誤");

   return rc;

 }
//************************************************************************
public int dbInsertD5T()
{
 msgOK();

 strSql = "delete PTR_FUND_CDATA_T "
       + " where table_name  =  'PTR_FUNDP' "
        + "and   data_key = ? "
        + "";
 //如果沒有資料回傳成功1
 Object[] param = new Object[]
   {
    wp.itemStr2("fund_code"),
   };

 sqlExec(strSql,param,false);
 if (sqlRowNum <= 0) rc=0;else rc=1;

 if (rc!=1) errmsg("刪除 PTR_FUND_CDATA_T 錯誤");

 return rc;

}
// ************************************************************************
 public int checkDecnum(String decStr,int colLength,int colScale)
 {
  if (decStr.length()==0) return(0);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
  decStr = decStr.replace("-","");
  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>colLength)||
      (parts.length==2&&
       (parts[0].length()>colLength||parts[1].length()>colScale)))
      return(1);
  return(0);
 }
// ************************************************************************
 public int dbInsertI2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm6220_bint"))
      dataType = "2" ;
   if (wp.respHtml.equals("mktm6220_acty"))
      dataType = "4" ;
   if (wp.respHtml.equals("mktm6220_gpcd"))
      dataType = "3" ;
   if (wp.respHtml.equals("mktm6220_dype"))
      dataType = "5" ;
   if (wp.respHtml.equals("mktm6220_gnce"))
      dataType = "0" ;
   if (wp.respHtml.equals("mktm6220_srcd"))
      dataType = "A" ;
   if (wp.respHtml.equals("mktm6220_aaa1"))
      dataType = "H" ;
   if (wp.respHtml.equals("mktm6220_aaa3"))
	  dataType = "P" ;
   if (wp.respHtml.equals("mktm6220_aaam"))
      dataType = "M" ;
   if (wp.respHtml.equals("mktm6220_mccd"))
      dataType = "8" ;
   if (wp.respHtml.equals("mktm6220_aaa2"))
      dataType = "K" ;
   if (wp.respHtml.equals("mktm6220_ucaf"))
      dataType = "F" ;
   if (wp.respHtml.equals("mktm6220_deci"))
      dataType = "G" ;
   if (wp.respHtml.equals("mktm6220_posd"))
      dataType = "E" ;
  strSql = "insert into PTR_FUND_DATA_T ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'PTR_FUNDP', "
          + "?, "
          + "?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code"),
      varsStr("data_code"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 PTR_FUND_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm6220_bint"))
      dataType = "2" ;
   if (wp.respHtml.equals("mktm6220_acty"))
      dataType = "4" ;
   if (wp.respHtml.equals("mktm6220_gpcd"))
      dataType = "3" ;
   if (wp.respHtml.equals("mktm6220_dype"))
      dataType = "5" ;
   if (wp.respHtml.equals("mktm6220_gnce"))
      dataType = "0" ;
   if (wp.respHtml.equals("mktm6220_srcd"))
      dataType = "A" ;
   if (wp.respHtml.equals("mktm6220_aaa1"))
      dataType = "H" ;
   if (wp.respHtml.equals("mktm6220_aaa3"))
	  dataType = "P" ;
   if (wp.respHtml.equals("mktm6220_aaam"))
      dataType = "M" ;
   if (wp.respHtml.equals("mktm6220_mccd"))
      dataType = "8" ;
   if (wp.respHtml.equals("mktm6220_aaa2"))
      dataType = "K" ;
   if (wp.respHtml.equals("mktm6220_ucaf"))
      dataType = "F" ;
   if (wp.respHtml.equals("mktm6220_deci"))
      dataType = "G" ;
   if (wp.respHtml.equals("mktm6220_posd"))
      dataType = "E" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code")
     };
   if (sqlRowcount("PTR_FUND_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'PTR_FUNDP' "
                    , param) <= 0)
       return 1;

   strSql = "delete PTR_FUND_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'PTR_FUNDP'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm6220_mrch"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm6220_mrck"))
      dataType = "C" ;
   if (wp.respHtml.equals("mktm6220_mrcd"))
      dataType = "6" ;
  strSql = "insert into PTR_FUND_DATA_T ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "data_code2,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'PTR_FUNDP', "
          + "?, "
          + "?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code"),
      varsStr("data_code"),
      varsStr("data_code2"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 PTR_FUND_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm6220_mrch"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm6220_mrck"))
      dataType = "C" ;
   if (wp.respHtml.equals("mktm6220_mrcd"))
      dataType = "6" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code")
     };
   if (sqlRowcount("PTR_FUND_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'PTR_FUNDP' "
                    , param) <= 0)
       return 1;

   strSql = "delete PTR_FUND_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'PTR_FUNDP'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI4() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm6220_cocq"))
      dataType = "7" ;
   if (wp.respHtml.equals("mktm6220_cocd"))
      dataType = "9" ;
   if (wp.respHtml.equals("mktm6220_pose"))
      dataType = "B" ;
  strSql = "insert into PTR_FUND_DATA_T ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "data_code2,"
          + "data_code3,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'PTR_FUNDP', "
          + "?, "
          + "?,?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code"),
      varsStr("data_code"),
      varsStr("data_code2"),
      varsStr("data_code3"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 PTR_FUND_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD4() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm6220_cocq"))
      dataType = "7" ;
   if (wp.respHtml.equals("mktm6220_cocd"))
      dataType = "9" ;
   if (wp.respHtml.equals("mktm6220_pose"))
      dataType = "B" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code")
     };
   if (sqlRowcount("PTR_FUND_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'PTR_FUNDP' "
                    , param) <= 0)
       return 1;

   strSql = "delete PTR_FUND_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'PTR_FUNDP'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
//************************************************************************
public int dbInsertI5() throws Exception
{
 msgOK();

 String dataType="";
 if (wp.respHtml.equals("mktm6220_namc"))
    dataType = "A" ;
 if (wp.respHtml.equals("mktm6220_name"))
    dataType = "B" ;
strSql = "insert into PTR_FUND_CDATA_T ( "
        + "table_name, "
        + "data_type, "
        + "data_key,"
        + "data_code,"
        + "crt_date, "
        + "crt_user, "
        + " mod_time, "
        + " mod_user, "
        + " mod_pgm "
        + ") values ("
        + "'PTR_FUNDP', "
        + "?, "
        + "?,?," 
        + "to_char(sysdate,'yyyymmdd'),"
        + "?,"
        + " sysdate, "
        + "?,"
        + " ? "
        + ")";

 Object[] param =new Object[]
   {
    dataType, 
    wp.itemStr("fund_code"),
    varsStr("data_code"),
    wp.loginUser,
      wp.loginUser,
    wp.modPgm()
   };

 wp.dupRecord = "Y";
 sqlExec(strSql, param , false);
 if (sqlRowNum <= 0) rc=0;else rc=1;

 if (rc!=1) errmsg("新增8 PTR_FUND_CDATA_T 錯誤");
 else dbUpdateMainU5();

 return rc;
}
//************************************************************************
public int dbUpdateMainU5() throws Exception
{
// TODO Auto-update main 
return rc;
}
//************************************************************************
public int dbDeleteD5()
{
msgOK();

String dataType="";
if (wp.respHtml.equals("mktm6220_namc"))
   dataType = "A" ;
if (wp.respHtml.equals("mktm6220_name"))
   dataType = "B" ;
//如果沒有資料回傳成功2
Object[] param = new Object[]
  {
   dataType, 
   wp.itemStr("fund_code")
  };
if (sqlRowcount("PTR_FUND_CDATA_T" 
                 , "where data_type = ? "
                + "and   data_key = ? "
                 + "and   table_name = 'PTR_FUNDP' "
                 , param) <= 0)
    return 1;

strSql = "delete PTR_FUND_CDATA_T "
       + "where data_type = ? "
       + "and   data_key = ?  "
       + "and   table_name = 'PTR_FUNDP'  "
       ;
sqlExec(strSql,param,false);


return 1;

}

// ************************************************************************
 public int dbInsertI2Aaa1(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
 
//************************************************************************
	public int dbInsertI2Aaa3(String tableName, String[] columnCol, String[] columnDat) throws Exception {
		String[] columnData = new String[50];
		String stra = "", strb = "";
		int skipLine = 0;
		long listCnt = 50;
		strSql = " insert into  " + tableName + " (";
		for (int inti = 0; inti < listCnt; inti++) {
			stra = columnCol[inti];
			if (stra.length() == 0)
				continue;
			strSql = strSql + stra + ",";
		}

		strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
		for (int inti = 0; inti < listCnt; inti++) {
			stra = columnCol[inti];
			if (stra.length() == 0)
				continue;
			strSql = strSql + "?,";
		}
		strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

		Object[] param1 = new Object[50];
		for (int inti = 0; inti < listCnt; inti++) {
			stra = columnCol[inti];
			if (stra.length() == 0)
				continue;
			stra = columnDat[inti];
			param1[skipLine] = stra;
			skipLine++;
		}
		param1[skipLine++] = wp.loginUser;
		param1[skipLine++] = wp.sysDate + wp.sysTime;
		param1[skipLine++] = wp.modPgm();
		Object[] param = Arrays.copyOf(param1, skipLine);
		wp.dupRecord = "Y";
		sqlExec(strSql, param, true);
		if (sqlRowNum <= 0)
			rc = 0;
		else
			rc = 1;

		return rc;
	}
// ************************************************************************
 public int dbDeleteD2Aaa1(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "PTR_FUNDP",
      wp.itemStr2("fund_code"),
     "1"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
//************************************************************************
public int dbDeleteD2Aaa3(String tableName) throws Exception
{
strSql = "delete  "+tableName+" " 
       + "where table_name = ? "
       + "and   data_key = ? "
       + "and   data_type = ? "
       ;

Object[] param =new Object[]
  {
    "PTR_FUNDP",
    wp.itemStr2("fund_code"),
   "P"
  };

sqlExec(strSql, param, true);
if (sqlRowNum <= 0) rc=0;else rc=1;
if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

return rc;
}
// ************************************************************************
 public int dbInsertI2Aaak(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2Aaak(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "PTR_FUNDP",
      wp.itemStr2("fund_code"),
     "C"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI2Aaa2(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2Aaa2(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "PTR_FUNDP",
      wp.itemStr2("fund_code"),
     "6"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsMediaErrlog(String tranSeqStr,String[] errMsg ) throws Exception
 {
  dateTime();
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  comr.setConn(wp);

  if (!comm.isNumber(errMsg[10])) errMsg[10]="0";
  if (!comm.isNumber(errMsg[1])) errMsg[1]="0";
  if (!comm.isNumber(errMsg[2])) errMsg[2]="0";

  strSql= " insert into ecs_media_errlog ("
          + " crt_date, "
          + " crt_time, "
          + " file_name, "
          + " unit_code, "
          + " main_desc, "
          + " error_seq, "
          + " error_desc, "
          + " line_seq, "
          + " column_seq, "
          + " column_data, "
          + " trans_seqno, "
          + " column_desc, "
          + " program_code, "
          + " mod_time, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?," // 10 record
          + "?,?,?,"               // 4 trvotfd
          + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param =new Object[]
       {
        wp.sysDate,
        wp.sysTime,
        wp.itemStr2("zz_file_name"),
        comr.getObjectOwner("3",wp.modPgm()),
        errMsg[0],
        Integer.valueOf(errMsg[1]),
        errMsg[4],
        Integer.valueOf(errMsg[10]),
        Integer.valueOf(errMsg[2]),
        errMsg[3],
        tranSeqStr,
        errMsg[5],
        wp.modPgm(),
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) errmsg("新增4 ecs_media_errlog 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsNotifyLog(String tranSeqStr,int errorCnt ) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  dateTime();
  strSql= " insert into ecs_notify_log ("
          + " crt_date, "
          + " crt_time, "
          + " unit_code, "
          + " obj_type, "
          + " notify_head, "
          + " notify_name, "
          + " notify_desc1, "
          + " notify_desc2, "
          + " trans_seqno, "
          + " mod_time, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?," // 9 record
          + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param =new Object[]
       {
        wp.sysDate,
        wp.sysTime,
        comr.getObjectOwner("3",wp.modPgm()),
        "3",
        "媒體檔轉入資料有誤(只記錄前100筆)",
        "媒體檔名:"+wp.itemStr2("zz_file_name"),
        "程式 "+wp.modPgm()+" 轉 "+wp.itemStr2("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤",
        tranSeqStr,
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) errmsg("新增5 ecs_modify_log 錯誤");
  return rc;
 }
// ************************************************************************
 int listParmDataCnt(String s1,String s2,String s3,String s4) 
 {
  String isSql = "select count(*) as data_cnt "
                + "from  " + s1 +" "
                + " where table_name = ? "
                + " and   data_key   = ? "
                + " and   data_type  = ? "
                ;
  Object[] param = new Object[] {s2,s3,s4};
  sqlSelect(isSql,param);

 return(Integer.parseInt(colStr("data_cnt")));
 }

// ************************************************************************

}  // End of class
