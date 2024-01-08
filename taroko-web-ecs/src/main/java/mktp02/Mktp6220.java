/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/02/16  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard   
* 112-05-05  V1.00.03  Ryan    新增國內外消費欄位維護，特店中文名稱、特店英文名稱參數維護，[消費回饋比例]區塊新增多個欄位維護   *
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *
***************************************************************************/
package mktp02;

import mktp02.Mktp6220Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6220 extends BaseProc
{
 private final String PROGNAME = "刷卡金參數檔覆核作業處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6220Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "ptr_fundp_t";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batchNo     = "";
  int errorCnt=0,recCnt=0,notifyCnt=0,colNum=0;
  int[]  datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String   upGroupType= "0";

// ************************************************************************
 @Override
 public void actionFunction(TarokoCommon wr) throws Exception
 {
  super.wp = wr;
  rc = 1;

  strAction = wp.buttonCode;
  if (eqIgno(wp.buttonCode, "X"))
     {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
     }
  else if (eqIgno(wp.buttonCode, "Q"))
     {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
     }
  else if (eqIgno(wp.buttonCode, "R"))
     {//-資料讀取-
      strAction = "R";
      dataRead();
     }
  else if (eqIgno(wp.buttonCode, "C"))
     {// 資料處理 -/
      strAction = "A";
      dataProcess();
     }
  else if (eqIgno(wp.buttonCode, "R2"))
     {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
     }
  else if (eqIgno(wp.buttonCode, "R3"))
     {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
     }
  else if (eqIgno(wp.buttonCode, "R4"))
     {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
     }
  else if (eqIgno(wp.buttonCode, "R5")) {
      strAction = "R5";
      dataReadR5();
     }
  else if (eqIgno(wp.buttonCode, "M"))
     {/* 瀏覽功能 :skip-page*/
      queryRead();
     }
  else if (eqIgno(wp.buttonCode, "S"))
     {/* 動態查詢 */
      querySelect();
     }
  else if (eqIgno(wp.buttonCode, "L"))
     {/* 清畫面 */
      strAction = "";
      clearFunc();
     }
  else if (eqIgno(wp.buttonCode, "NILL"))
     {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
     }

  funcSelect();
  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code")
              + sqlCol(wp.itemStr2("ex_crt_user"), "a.crt_user")
              + " and apr_flag='N'     "
              ;

  //-page control-
  wp.queryWhere = wp.whereStr;
  wp.setQueryMode();

  queryRead();
 }
// ************************************************************************
 @Override
 public void queryRead() throws Exception
 {
  if (wp.colStr("org_tab_name").length()>0)
     controlTabName = wp.colStr("org_tab_name");
  else
     controlTabName = orgTabName;

  wp.pageControl();

  wp.selectSQL = " "
               + "hex(a.rowid) as rowid, "
               + "nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.aud_type,"
               + "a.fund_code,"
               + "a.fund_name,"
               + "a.tran_base,"
               + "a.fund_crt_date_s,"
               + "a.fund_crt_date_e,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by fund_code"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commCrtUser("comm_crt_user");

  commTranBase("comm_tran_base");
  commfuncAudType("aud_type");

  wp.setPageValue();
 }
// ************************************************************************
 @Override
  public void querySelect() throws Exception
 {

  kk1 = itemKk("data_k1");
  qFrom=1;
  dataRead();
 }
// ************************************************************************
 @Override
 public void dataRead() throws Exception
 {
  if (qFrom==0)
  if (wp.itemStr2("kk_fund_code").length()==0)
     { 
      alertErr("查詢鍵必須輸入");
      return; 
     } 
  if (controlTabName.length()==0)
     {
      if (wp.colStr("control_tab_name").length()==0)
         controlTabName=orgTabName;
      else
         controlTabName=wp.colStr("control_tab_name");
     }
  else
     {
      if (wp.colStr("control_tab_name").length()!=0)
         controlTabName=wp.colStr("control_tab_name");
     }
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.aud_type,"
               + "a.fund_code as fund_code,"
               + "a.crt_user,"
               + "a.fund_name,"
               + "a.tran_base,"
               + "a.fund_crt_date_s,"
               + "a.fund_crt_date_e,"
               + "a.effect_type,"
               + "a.effect_months,"
               + "a.effect_years,"
               + "a.effect_fix_month,"
               + "a.stop_flag,"
               + "a.stop_date,"
               + "a.stop_desc,"
               + "a.bin_type_sel,"
               + "a.acct_type_sel,"
               + "a.group_code_sel,"
               + "a.card_type_sel,"
               + "a.new_hldr_cond,"
               + "a.new_hldr_flag,"
               + "a.new_card_days,"
               + "a.new_hldr_days,"
               + "a.new_group_cond,"
               + "a.new_hldr_card,"
               + "a.new_hldr_sup,"
               + "a.apply_age_cond,"
               + "a.apply_age_s,"
               + "a.apply_age_e,"
               + "a.activate_cond,"
               + "a.activate_flag,"
               + "a.valid_period,"
               + "a.cobrand_code,"
               + "a.source_code_sel,"
               + "a.merchant_sel,"
               + "a.mcht_group_sel,"
               + "a.platform_kind_sel,"
               + "a.currency_sel,"
               + "a.ex_currency_sel,"
               + "a.pos_entry_sel,"
               + "a.pos_merchant_sel,"
               + "a.pos_mcht_group_sel,"
               + "a.bl_cond,"
               + "a.ca_cond,"
               + "a.id_cond,"
               + "a.ao_cond,"
               + "a.it_cond,"
               + "a.ot_cond,"
               + "a.purch_feed_flag,"
               + "a.purch_date_s,"
               + "a.purch_date_e,"
               + "a.purch_reclow_cond,"
               + "a.purch_reclow_amt,"
               + "a.purch_rec_amt_cond,"
               + "a.purch_rec_amt,"
               + "a.purch_tol_amt_cond,"
               + "a.purch_tol_amt,"
               + "a.purch_tol_time_cond,"
               + "a.purch_tol_time,"
               + "a.purch_feed_type,"
               + "a.purch_type,"
               + "a.purch_feed_amt,"
               + "a.purch_feed_rate,"
               + "a.fund_feed_flag,"
               + "a.threshold_sel,"
               + "a.purchase_type_sel,"
               + "a.fund_s_amt_1,"
               + "a.fund_e_amt_1,"
               + "a.fund_rate_1,"
               + "a.fund_amt_1,"
               + "a.fund_s_amt_2,"
               + "a.fund_e_amt_2,"
               + "a.fund_rate_2,"
               + "a.fund_amt_2,"
               + "a.fund_s_amt_3,"
               + "a.fund_e_amt_3,"
               + "a.fund_rate_3,"
               + "a.fund_amt_3,"
               + "a.fund_s_amt_4,"
               + "a.fund_e_amt_4,"
               + "a.fund_rate_4,"
               + "a.fund_amt_4,"
               + "a.fund_s_amt_5,"
               + "a.fund_e_amt_5,"
               + "a.fund_rate_5,"
               + "a.fund_amt_5,"
               + "a.rc_sub_amt,"
               + "a.rc_sub_rate,"
               + "a.program_exe_type,"
               + "a.unlimit_start_month,"
               + "a.cal_s_month,"
               + "a.cal_e_month,"
               + "a.card_feed_date_s,"
               + "a.card_feed_date_e,"
               + "a.card_feed_flag,"
               + "a.cal_months,"
               + "a.card_feed_months2,"
               + "a.card_feed_days,"
               + "a.new_hldr_sel,"
               + "a.feedback_type,"
               + "a.card_feed_run_day,"
               + "a.feedback_months,"
               + "a.feedback_cycle_flag,"
               + "a.feedback_lmt,"
               + "a.purch_feed_times,"
               + "a.autopay_flag,"
               + "a.mp_flag,"
               + "a.valid_card_flag,"
               + "a.valid_afi_flag,"
               + "a.ebill_flag,"
               + "a.autopay_digit_cond,"
               + "a.d_txn_cond,"
               + "a.d_txn_amt,"
               + "a.cancel_period,"
               + "a.cancel_s_month,"
               + "a.cancel_scope,"
               + "a.d_mcc_code_sel,"
               + "a.d_merchant_sel,"
               + "a.d_mcht_group_sel,"
               + "a.d_ucaf_sel,"
               + "a.d_eci_sel,"
               + "a.d_pos_entry_sel,"
               + "a.cancel_event,"
               + "a.min_mcode,"
               + "a.cancel_high_amt,"
               + "a.foreign_code,"
               + "a.mcht_cname_sel,"
               + "a.mcht_ename_sel,"
               +"a.hapcare_trust_cond,"
               +"a.hapcare_trust_rate,"
               +"a.housing_endow_cond,"
               +"a.housing_endow_rate,"
               +"a.happycare_fblmt,"
               +"a.mortgage_cond,"
               +"a.mortgag_rate,"
               +"a.mortgage_fblmt,"
               +"a.util_entrustded_cond,"
               +"a.util_entrustded_rate,"
               +"a.util_entrustded_fblmt,"
               +"a.twpay_cond,"
               +"a.twpay_rate,"
               +"a.tcblife_ec_cond,"
               +"a.tcblife_ec_rate,"
               +"a.eco_fblmt,"
               +"a.extratwpay_cond,"
               +"a.onlyaddon_calcond "
//               + "a.extratwpay_rate,"
//               + "a.extratwpay_fblmt"
               ;

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.fund_code")
                   ;
     }
  else if (qFrom==1)
     {
       wp.whereStr = wp.whereStr
                   +  sqlRowId(kk1, "a.rowid")
                   ;
     }

  pageSelect();
  if (sqlNotFind())
     {
      return;
     }
  commFundBase("comm_tran_base");
  commEffectType("comm_effect_type");
  commSelect2("comm_bin_type_sel");
  commSelect4("comm_acct_type_sel");
  commSelect3("comm_group_code_sel");
  commSelect5("comm_card_type_sel");
  commNewHldrFlag("comm_new_hldr_flag");
  commActivate("comm_activate_flag");
  commCobrand("comm_valid_period");
  commSelectA("comm_source_code_sel");
  commSelect1("comm_merchant_sel");
  commSelectB("comm_mcht_group_sel");
  commSelectB("comm_platform_kind_sel");
  commSelect7("comm_currency_sel");
  commSelect9("comm_ex_currency_sel");
  commSelectD("comm_pos_entry_sel");
  commSelectK("comm_pos_merchant_sel");
  commSelectM("comm_pos_mcht_group_sel");
  commFeedType("comm_purch_feed_type");
  commPurchType("comm_purch_type");
  commThresholdSel("comm_threshold_sel");
  commPurchaseType("comm_purchase_type_sel");
  commExeType("comm_program_exe_type");
  commCardFeedFlag("comm_card_feed_flag");
  commNewHldrSel("comm_new_hldr_sel");
  commFeedbackType("comm_feedback_type");
  commFeedbackCycleFlag("comm_feedback_cycle_flag");
  commCancelMethod("comm_cancel_period");
  commCancelScope("comm_cancel_scope");
  commSelect8("comm_d_mcc_code_sel");
  commSelect6("comm_d_merchant_sel");
  commSelectC("comm_d_mcht_group_sel");
  commSelectF("comm_d_ucaf_sel");
  commSelectG("comm_d_eci_sel");
  commSelectE("comm_d_pos_entry_sel");
  commCancelEvent("comm_cancel_event");
  commCrtUser("comm_crt_user");
  commForeignCode("comm_foreign_code");
  commMchtCname("comm_mcht_cname_sel");
  commMchtEname("comm_mcht_ename_sel");
  checkButtonOff();
  km1 = wp.colStr("fund_code");
  listWkdataAft();
  if (!wp.colStr("aud_type").equals("A")) dataReadR3R();
  else
    {
     commfuncAudType("aud_type");
     listWkdataSpace();
    }
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = "ptr_fundp";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.fund_code as fund_code,"
               + "a.crt_user as bef_crt_user,"
               + "a.fund_name as bef_fund_name,"
               + "a.tran_base as bef_tran_base,"
               + "a.fund_crt_date_s as bef_fund_crt_date_s,"
               + "a.fund_crt_date_e as bef_fund_crt_date_e,"
               + "a.effect_type as bef_effect_type,"
               + "a.effect_months as bef_effect_months,"
               + "a.effect_years as bef_effect_years,"
               + "a.effect_fix_month as bef_effect_fix_month,"
               + "a.stop_flag as bef_stop_flag,"
               + "a.stop_date as bef_stop_date,"
               + "a.stop_desc as bef_stop_desc,"
               + "a.bin_type_sel as bef_bin_type_sel,"
               + "a.acct_type_sel as bef_acct_type_sel,"
               + "a.group_code_sel as bef_group_code_sel,"
               + "a.card_type_sel as bef_card_type_sel,"
               + "a.new_hldr_cond as bef_new_hldr_cond,"
               + "a.new_hldr_flag as bef_new_hldr_flag,"
               + "a.new_card_days as bef_new_card_days,"
               + "a.new_hldr_days as bef_new_hldr_days,"
               + "a.new_group_cond as bef_new_group_cond,"
               + "a.new_hldr_card as bef_new_hldr_card,"
               + "a.new_hldr_sup as bef_new_hldr_sup,"
               + "a.apply_age_cond as bef_apply_age_cond,"
               + "a.apply_age_s as bef_apply_age_s,"
               + "a.apply_age_e as bef_apply_age_e,"
               + "a.activate_cond as bef_activate_cond,"
               + "a.activate_flag as bef_activate_flag,"
               + "a.valid_period as bef_valid_period,"
               + "a.cobrand_code as bef_cobrand_code,"
               + "a.source_code_sel as bef_source_code_sel,"
               + "a.merchant_sel as bef_merchant_sel,"
               + "a.mcht_group_sel as bef_mcht_group_sel,"
               + "a.platform_kind_sel as bef_platform_kind_sel,"
               + "a.currency_sel as bef_currency_sel,"
               + "a.ex_currency_sel as bef_ex_currency_sel,"
               + "a.pos_entry_sel as bef_pos_entry_sel,"
               + "a.pos_merchant_sel as bef_pos_merchant_sel,"
               + "a.pos_mcht_group_sel as bef_pos_mcht_group_sel,"
               + "a.bl_cond as bef_bl_cond,"
               + "a.ca_cond as bef_ca_cond,"
               + "a.id_cond as bef_id_cond,"
               + "a.ao_cond as bef_ao_cond,"
               + "a.it_cond as bef_it_cond,"
               + "a.ot_cond as bef_ot_cond,"
               + "a.purch_feed_flag as bef_purch_feed_flag,"
               + "a.purch_date_s as bef_purch_date_s,"
               + "a.purch_date_e as bef_purch_date_e,"
               + "a.purch_reclow_cond as bef_purch_reclow_cond,"
               + "a.purch_reclow_amt as bef_purch_reclow_amt,"
               + "a.purch_rec_amt_cond as bef_purch_rec_amt_cond,"
               + "a.purch_rec_amt as bef_purch_rec_amt,"
               + "a.purch_tol_amt_cond as bef_purch_tol_amt_cond,"
               + "a.purch_tol_amt as bef_purch_tol_amt,"
               + "a.purch_tol_time_cond as bef_purch_tol_time_cond,"
               + "a.purch_tol_time as bef_purch_tol_time,"
               + "a.purch_feed_type as bef_purch_feed_type,"
               + "a.purch_type as bef_purch_type,"
               + "a.purch_feed_amt as bef_purch_feed_amt,"
               + "a.purch_feed_rate as bef_purch_feed_rate,"
               + "a.fund_feed_flag as bef_fund_feed_flag,"
               + "a.threshold_sel as bef_threshold_sel,"
               + "a.purchase_type_sel as bef_purchase_type_sel,"
               + "a.fund_s_amt_1 as bef_fund_s_amt_1,"
               + "a.fund_e_amt_1 as bef_fund_e_amt_1,"
               + "a.fund_rate_1 as bef_fund_rate_1,"
               + "a.fund_amt_1 as bef_fund_amt_1,"
               + "a.fund_s_amt_2 as bef_fund_s_amt_2,"
               + "a.fund_e_amt_2 as bef_fund_e_amt_2,"
               + "a.fund_rate_2 as bef_fund_rate_2,"
               + "a.fund_amt_2 as bef_fund_amt_2,"
               + "a.fund_s_amt_3 as bef_fund_s_amt_3,"
               + "a.fund_e_amt_3 as bef_fund_e_amt_3,"
               + "a.fund_rate_3 as bef_fund_rate_3,"
               + "a.fund_amt_3 as bef_fund_amt_3,"
               + "a.fund_s_amt_4 as bef_fund_s_amt_4,"
               + "a.fund_e_amt_4 as bef_fund_e_amt_4,"
               + "a.fund_rate_4 as bef_fund_rate_4,"
               + "a.fund_amt_4 as bef_fund_amt_4,"
               + "a.fund_s_amt_5 as bef_fund_s_amt_5,"
               + "a.fund_e_amt_5 as bef_fund_e_amt_5,"
               + "a.fund_rate_5 as bef_fund_rate_5,"
               + "a.fund_amt_5 as bef_fund_amt_5,"
               + "a.rc_sub_amt as bef_rc_sub_amt,"
               + "a.rc_sub_rate as bef_rc_sub_rate,"
               + "a.program_exe_type as bef_program_exe_type,"
               + "a.unlimit_start_month as bef_unlimit_start_month,"
               + "a.cal_s_month as bef_cal_s_month,"
               + "a.cal_e_month as bef_cal_e_month,"
               + "a.card_feed_date_s as bef_card_feed_date_s,"
               + "a.card_feed_date_e as bef_card_feed_date_e,"
               + "a.card_feed_flag as bef_card_feed_flag,"
               + "a.cal_months as bef_cal_months,"
               + "a.card_feed_months2 as bef_card_feed_months2,"
               + "a.card_feed_days as bef_card_feed_days,"
               + "a.new_hldr_sel as bef_new_hldr_sel,"
               + "a.feedback_type as bef_feedback_type,"
               + "a.card_feed_run_day as bef_card_feed_run_day,"
               + "a.feedback_months as bef_feedback_months,"
               + "a.feedback_cycle_flag as bef_feedback_cycle_flag,"
               + "a.feedback_lmt as bef_feedback_lmt,"
               + "a.purch_feed_times as bef_purch_feed_times,"
               + "a.autopay_flag as bef_autopay_flag,"
               + "a.mp_flag as bef_mp_flag,"
               + "a.valid_card_flag as bef_valid_card_flag,"
               + "a.valid_afi_flag as bef_valid_afi_flag,"
               + "a.ebill_flag as bef_ebill_flag,"
               + "a.autopay_digit_cond as bef_autopay_digit_cond,"
               + "a.d_txn_cond as bef_d_txn_cond,"
               + "a.d_txn_amt as bef_d_txn_amt,"
               + "a.cancel_period as bef_cancel_period,"
               + "a.cancel_s_month as bef_cancel_s_month,"
               + "a.cancel_scope as bef_cancel_scope,"
               + "a.d_mcc_code_sel as bef_d_mcc_code_sel,"
               + "a.d_merchant_sel as bef_d_merchant_sel,"
               + "a.d_mcht_group_sel as bef_d_mcht_group_sel,"
               + "a.d_ucaf_sel as bef_d_ucaf_sel,"
               + "a.d_eci_sel as bef_d_eci_sel,"
               + "a.d_pos_entry_sel as bef_d_pos_entry_sel,"
               + "a.cancel_event as bef_cancel_event,"
               + "a.min_mcode as bef_min_mcode,"
               + "a.cancel_high_amt as bef_cancel_high_amt,"
               + "a.foreign_code as bef_foreign_code,"
               + "a.mcht_cname_sel as bef_mcht_cname_sel,"
               + "a.mcht_ename_sel as bef_mcht_ename_sel,"
               +"a.hapcare_trust_cond as bef_hapcare_trust_cond,"
               +"a.hapcare_trust_rate as bef_hapcare_trust_rate,"
               +"a.housing_endow_cond as bef_housing_endow_cond,"
               +"a.housing_endow_rate as bef_housing_endow_rate,"
               +"a.happycare_fblmt as bef_happycare_fblmt,"
               +"a.mortgage_cond as bef_mortgage_cond,"
               +"a.mortgag_rate as  bef_mortgag_rate,"
               +"a.mortgage_fblmt as bef_mortgage_fblmt,"
               +"a.util_entrustded_cond as bef_util_entrustded_cond,"
               +"a.util_entrustded_rate as bef_util_entrustded_rate,"
               +"a.util_entrustded_fblmt as bef_util_entrustded_fblmt,"
               +"a.twpay_cond as bef_twpay_cond,"
               +"a.twpay_rate as bef_twpay_rate,"
               +"a.tcblife_ec_cond as bef_tcblife_ec_cond,"
               +"a.tcblife_ec_rate as bef_tcblife_ec_rate,"
               +"a.eco_fblmt as bef_eco_fblmt,"
               +"a.extratwpay_cond as bef_extratwpay_cond,"
               +"a.onlyaddon_calcond as bef_onlyaddon_calcond "
//               +"a.extratwpay_rate as bef_extratwpay_rate,"
//               +"a.extratwpay_fblmt as bef_extratwpay_fblmt"
               ;

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.fund_code")
              ;

  pageSelect();
  if (sqlNotFind())
     {
      wp.notFound ="";
      return;
     }
  wp.colSet("control_tab_name",controlTabName); 

  if (wp.respHtml.indexOf("_detl") > 0) 
     wp.colSet("btnStore_disable","");   
  commCrtUser("comm_crt_user");
  commFundBase("comm_tran_base");
  commEffectType("comm_effect_type");
  commSelect2("comm_bin_type_sel");
  commSelect4("comm_acct_type_sel");
  commSelect3("comm_group_code_sel");
  commSelect5("comm_card_type_sel");
  commNewHldrFlag("comm_new_hldr_flag");
  commActivate("comm_activate_flag");
  commCobrand("comm_valid_period");
  commSelectA("comm_source_code_sel");
  commSelect1("comm_merchant_sel");
  commSelectB("comm_mcht_group_sel");
  commSelectB("comm_platform_kind_sel");
  commSelect7("comm_currency_sel");
  commSelect9("comm_ex_currency_sel");
  commSelectD("comm_pos_entry_sel");
  commSelectK("comm_pos_merchant_sel");
  commSelectM("comm_pos_mcht_group_sel");
  commFeedType("comm_purch_feed_type");
  commPurchType("comm_purch_type");
  commThresholdSel("comm_threshold_sel");
  commPurchaseType("comm_purchase_type_sel");
  commExeType("comm_program_exe_type");
  commCardFeedFlag("comm_card_feed_flag");
  commNewHldrSel("comm_new_hldr_sel");
  commFeedbackType("comm_feedback_type");
  commFeedbackCycleFlag("comm_feedback_cycle_flag");
  commCancelMethod("comm_cancel_period");
  commCancelScope("comm_cancel_scope");
  commSelect8("comm_d_mcc_code_sel");
  commSelect6("comm_d_merchant_sel");
  commSelectC("comm_d_mcht_group_sel");
  commSelectF("comm_d_ucaf_sel");
  commSelectG("comm_d_eci_sel");
  commSelectE("comm_d_pos_entry_sel");
  commCancelEvent("comm_cancel_event");
  commForeignCode("comm_foreign_code");
  commMchtCname("comm_mcht_cname_sel");
  commMchtEname("comm_mcht_ename_sel");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("bin_type_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"2"));
  wp.colSet("acct_type_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"4"));
  wp.colSet("group_code_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"3"));
  wp.colSet("card_type_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"5"));
  wp.colSet("new_gp_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"0"));
  wp.colSet("source_code_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"A"));
  wp.colSet("merchant_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"1"));
  wp.colSet("mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"H"));
  wp.colSet("platform_kind_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"P"));
  wp.colSet("currency_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"7"));
  wp.colSet("ex_currency_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"9"));
  wp.colSet("pos_entry_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"B"));
  wp.colSet("pos_merchant_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"C"));
  wp.colSet("pos_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"M"));
  wp.colSet("d_mcc_code_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"8"));
  wp.colSet("d_merchant_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"6"));
  wp.colSet("d_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"K"));
  wp.colSet("d_ucaf_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"F"));
  wp.colSet("d_eci_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"G"));
  wp.colSet("d_pos_entry_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"E"));
  wp.colSet("mcht_cname_sel_cnt" , listPtrFundCData("ptr_fund_cdata_t","PTR_FUNDP",wp.colStr("fund_code"),"A"));
  wp.colSet("mcht_ename_sel_cnt" , listPtrFundCData("ptr_fund_cdata_t","PTR_FUNDP",wp.colStr("fund_code"),"B"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("fund_name").equals(wp.colStr("bef_fund_name")))
     wp.colSet("opt_fund_name","Y");

  if (!wp.colStr("tran_base").equals(wp.colStr("bef_tran_base")))
     wp.colSet("opt_tran_base","Y");
  commFundBase("comm_tran_base");
  commFundBase("comm_bef_tran_base");

  if (!wp.colStr("fund_crt_date_s").equals(wp.colStr("bef_fund_crt_date_s")))
     wp.colSet("opt_fund_crt_date_s","Y");

  if (!wp.colStr("fund_crt_date_e").equals(wp.colStr("bef_fund_crt_date_e")))
     wp.colSet("opt_fund_crt_date_e","Y");

  if (!wp.colStr("effect_type").equals(wp.colStr("bef_effect_type")))
     wp.colSet("opt_effect_type","Y");
  commEffectType("comm_effect_type");
  commEffectType("comm_bef_effect_type");

  if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
     wp.colSet("opt_effect_months","Y");

  if (!wp.colStr("effect_years").equals(wp.colStr("bef_effect_years")))
     wp.colSet("opt_effect_years","Y");

  if (!wp.colStr("effect_fix_month").equals(wp.colStr("bef_effect_fix_month")))
     wp.colSet("opt_effect_fix_month","Y");

  if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
     wp.colSet("opt_stop_flag","Y");

  if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
     wp.colSet("opt_stop_date","Y");

  if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
     wp.colSet("opt_stop_desc","Y");

  if (!wp.colStr("bin_type_sel").equals(wp.colStr("bef_bin_type_sel")))
     wp.colSet("opt_bin_type_sel","Y");
  commSelect2("comm_bin_type_sel");
  commSelect2("comm_bef_bin_type_sel");

  wp.colSet("bef_bin_type_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"2"));
  if (!wp.colStr("bin_type_sel_cnt").equals(wp.colStr("bef_bin_type_sel_cnt")))
     wp.colSet("opt_bin_type_sel_cnt","Y");

  if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
     wp.colSet("opt_acct_type_sel","Y");
  commSelect4("comm_acct_type_sel");
  commSelect4("comm_bef_acct_type_sel");

  wp.colSet("bef_acct_type_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"4"));
  if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
     wp.colSet("opt_acct_type_sel_cnt","Y");

  if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
     wp.colSet("opt_group_code_sel","Y");
  commSelect3("comm_group_code_sel");
  commSelect3("comm_bef_group_code_sel");

  wp.colSet("bef_group_code_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"3"));
  if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
     wp.colSet("opt_group_code_sel_cnt","Y");

  if (!wp.colStr("card_type_sel").equals(wp.colStr("bef_card_type_sel")))
     wp.colSet("opt_card_type_sel","Y");
  commSelect5("comm_card_type_sel");
  commSelect5("comm_bef_card_type_sel");

  wp.colSet("bef_card_type_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"5"));
  if (!wp.colStr("card_type_sel_cnt").equals(wp.colStr("bef_card_type_sel_cnt")))
     wp.colSet("opt_card_type_sel_cnt","Y");

  if (!wp.colStr("new_hldr_cond").equals(wp.colStr("bef_new_hldr_cond")))
     wp.colSet("opt_new_hldr_cond","Y");

  if (!wp.colStr("new_hldr_flag").equals(wp.colStr("bef_new_hldr_flag")))
     wp.colSet("opt_new_hldr_flag","Y");
  commNewHldrFlag("comm_new_hldr_flag");
  commNewHldrFlag("comm_bef_new_hldr_flag");

  if (!wp.colStr("new_card_days").equals(wp.colStr("bef_new_card_days")))
     wp.colSet("opt_new_card_days","Y");

  if (!wp.colStr("new_hldr_days").equals(wp.colStr("bef_new_hldr_days")))
     wp.colSet("opt_new_hldr_days","Y");

  if (!wp.colStr("new_group_cond").equals(wp.colStr("bef_new_group_cond")))
     wp.colSet("opt_new_group_cond","Y");

  wp.colSet("bef_new_gp_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"0"));
  if (!wp.colStr("new_gp_cnt").equals(wp.colStr("bef_new_gp_cnt")))
     wp.colSet("opt_new_gp_cnt","Y");

  if (!wp.colStr("new_hldr_card").equals(wp.colStr("bef_new_hldr_card")))
     wp.colSet("opt_new_hldr_card","Y");

  if (!wp.colStr("new_hldr_sup").equals(wp.colStr("bef_new_hldr_sup")))
     wp.colSet("opt_new_hldr_sup","Y");

  if (!wp.colStr("apply_age_cond").equals(wp.colStr("bef_apply_age_cond")))
     wp.colSet("opt_apply_age_cond","Y");

  if (!wp.colStr("apply_age_s").equals(wp.colStr("bef_apply_age_s")))
     wp.colSet("opt_apply_age_s","Y");

  if (!wp.colStr("apply_age_e").equals(wp.colStr("bef_apply_age_e")))
     wp.colSet("opt_apply_age_e","Y");

  if (!wp.colStr("activate_cond").equals(wp.colStr("bef_activate_cond")))
     wp.colSet("opt_activate_cond","Y");

  if (!wp.colStr("activate_flag").equals(wp.colStr("bef_activate_flag")))
     wp.colSet("opt_activate_flag","Y");
  commActivate("comm_activate_flag");
  commActivate("comm_bef_activate_flag");

  if (!wp.colStr("valid_period").equals(wp.colStr("bef_valid_period")))
     wp.colSet("opt_valid_period","Y");
  commCobrand("comm_valid_period");
  commCobrand("comm_bef_valid_period");

  if (!wp.colStr("cobrand_code").equals(wp.colStr("bef_cobrand_code")))
     wp.colSet("opt_cobrand_code","Y");

  if (!wp.colStr("source_code_sel").equals(wp.colStr("bef_source_code_sel")))
     wp.colSet("opt_source_code_sel","Y");
  commSelectA("comm_source_code_sel");
  commSelectA("comm_bef_source_code_sel");

  wp.colSet("bef_source_code_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"A"));
  if (!wp.colStr("source_code_sel_cnt").equals(wp.colStr("bef_source_code_sel_cnt")))
     wp.colSet("opt_source_code_sel_cnt","Y");

  if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
     wp.colSet("opt_merchant_sel","Y");
  commSelect1("comm_merchant_sel");
  commSelect1("comm_bef_merchant_sel");

  wp.colSet("bef_merchant_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"1"));
  if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
     wp.colSet("opt_merchant_sel_cnt","Y");

  if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
     wp.colSet("opt_mcht_group_sel","Y");
  commSelectB("comm_mcht_group_sel");
  commSelectB("comm_bef_mcht_group_sel");
  
  wp.colSet("bef_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"H"));
  if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
     wp.colSet("opt_mcht_group_sel_cnt","Y");
  
  if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
	     wp.colSet("opt_platform_kind_sel","Y");
	  commSelectB("comm_platform_kind_sel");
	  commSelectB("comm_bef_platform_kind_sel");
  
  wp.colSet("bef_platform_kind_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"P"));
  if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
     wp.colSet("opt_platform_kind_sel_cnt","Y");

  if (!wp.colStr("currency_sel").equals(wp.colStr("bef_currency_sel")))
     wp.colSet("opt_currency_sel","Y");
  commSelect7("comm_currency_sel");
  commSelect7("comm_bef_currency_sel");

  wp.colSet("bef_currency_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"7"));
  if (!wp.colStr("currency_sel_cnt").equals(wp.colStr("bef_currency_sel_cnt")))
     wp.colSet("opt_currency_sel_cnt","Y");

  if (!wp.colStr("ex_currency_sel").equals(wp.colStr("bef_ex_currency_sel")))
     wp.colSet("opt_ex_currency_sel","Y");
  commSelect9("comm_ex_currency_sel");
  commSelect9("comm_bef_ex_currency_sel");

  wp.colSet("bef_ex_currency_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"9"));
  if (!wp.colStr("ex_currency_sel_cnt").equals(wp.colStr("bef_ex_currency_sel_cnt")))
     wp.colSet("opt_ex_currency_sel_cnt","Y");

  if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
     wp.colSet("opt_pos_entry_sel","Y");
  commSelectD("comm_pos_entry_sel");
  commSelectD("comm_bef_pos_entry_sel");

  wp.colSet("bef_pos_entry_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"B"));
  if (!wp.colStr("pos_entry_sel_cnt").equals(wp.colStr("bef_pos_entry_sel_cnt")))
     wp.colSet("opt_pos_entry_sel_cnt","Y");

  if (!wp.colStr("pos_merchant_sel").equals(wp.colStr("bef_pos_merchant_sel")))
     wp.colSet("opt_pos_merchant_sel","Y");
  commSelectK("comm_pos_merchant_sel");
  commSelectK("comm_bef_pos_merchant_sel");

  wp.colSet("bef_pos_merchant_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"C"));
  if (!wp.colStr("pos_merchant_sel_cnt").equals(wp.colStr("bef_pos_merchant_sel_cnt")))
     wp.colSet("opt_pos_merchant_sel_cnt","Y");

  if (!wp.colStr("pos_mcht_group_sel").equals(wp.colStr("bef_pos_mcht_group_sel")))
     wp.colSet("opt_pos_mcht_group_sel","Y");
  commSelectM("comm_pos_mcht_group_sel");
  commSelectM("comm_bef_pos_mcht_group_sel");

  wp.colSet("bef_pos_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"M"));
  if (!wp.colStr("pos_mcht_group_sel_cnt").equals(wp.colStr("bef_pos_mcht_group_sel_cnt")))
     wp.colSet("opt_pos_mcht_group_sel_cnt","Y");

  if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
     wp.colSet("opt_bl_cond","Y");

  if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
     wp.colSet("opt_ca_cond","Y");

  if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
     wp.colSet("opt_id_cond","Y");

  if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
     wp.colSet("opt_ao_cond","Y");

  if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
     wp.colSet("opt_it_cond","Y");

  if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
     wp.colSet("opt_ot_cond","Y");

  if (!wp.colStr("purch_feed_flag").equals(wp.colStr("bef_purch_feed_flag")))
     wp.colSet("opt_purch_feed_flag","Y");

  if (!wp.colStr("purch_date_s").equals(wp.colStr("bef_purch_date_s")))
     wp.colSet("opt_purch_date_s","Y");

  if (!wp.colStr("purch_date_e").equals(wp.colStr("bef_purch_date_e")))
     wp.colSet("opt_purch_date_e","Y");

  if (!wp.colStr("purch_reclow_cond").equals(wp.colStr("bef_purch_reclow_cond")))
     wp.colSet("opt_purch_reclow_cond","Y");

  if (!wp.colStr("purch_reclow_amt").equals(wp.colStr("bef_purch_reclow_amt")))
     wp.colSet("opt_purch_reclow_amt","Y");

  if (!wp.colStr("purch_rec_amt_cond").equals(wp.colStr("bef_purch_rec_amt_cond")))
     wp.colSet("opt_purch_rec_amt_cond","Y");

  if (!wp.colStr("purch_rec_amt").equals(wp.colStr("bef_purch_rec_amt")))
     wp.colSet("opt_purch_rec_amt","Y");

  if (!wp.colStr("purch_tol_amt_cond").equals(wp.colStr("bef_purch_tol_amt_cond")))
     wp.colSet("opt_purch_tol_amt_cond","Y");

  if (!wp.colStr("purch_tol_amt").equals(wp.colStr("bef_purch_tol_amt")))
     wp.colSet("opt_purch_tol_amt","Y");

  if (!wp.colStr("purch_tol_time_cond").equals(wp.colStr("bef_purch_tol_time_cond")))
     wp.colSet("opt_purch_tol_time_cond","Y");

  if (!wp.colStr("purch_tol_time").equals(wp.colStr("bef_purch_tol_time")))
     wp.colSet("opt_purch_tol_time","Y");

  if (!wp.colStr("purch_feed_type").equals(wp.colStr("bef_purch_feed_type")))
     wp.colSet("opt_purch_feed_type","Y");
  commFeedType("comm_purch_feed_type");
  commFeedType("comm_bef_purch_feed_type");

  if (!wp.colStr("purch_type").equals(wp.colStr("bef_purch_type")))
     wp.colSet("opt_purch_type","Y");
  commPurchType("comm_purch_type");
  commPurchType("comm_bef_purch_type");

  if (!wp.colStr("purch_feed_amt").equals(wp.colStr("bef_purch_feed_amt")))
     wp.colSet("opt_purch_feed_amt","Y");

  if (!wp.colStr("purch_feed_rate").equals(wp.colStr("bef_purch_feed_rate")))
     wp.colSet("opt_purch_feed_rate","Y");

  if (!wp.colStr("fund_feed_flag").equals(wp.colStr("bef_fund_feed_flag")))
     wp.colSet("opt_fund_feed_flag","Y");

  if (!wp.colStr("threshold_sel").equals(wp.colStr("bef_threshold_sel")))
     wp.colSet("opt_threshold_sel","Y");
  commThresholdSel("comm_threshold_sel");
  commThresholdSel("comm_bef_threshold_sel");

  if (!wp.colStr("purchase_type_sel").equals(wp.colStr("bef_purchase_type_sel")))
     wp.colSet("opt_purchase_type_sel","Y");
  commPurchaseType("comm_purchase_type_sel");
  commPurchaseType("comm_bef_purchase_type_sel");

  if (!wp.colStr("fund_s_amt_1").equals(wp.colStr("bef_fund_s_amt_1")))
     wp.colSet("opt_fund_s_amt_1","Y");

  if (!wp.colStr("fund_e_amt_1").equals(wp.colStr("bef_fund_e_amt_1")))
     wp.colSet("opt_fund_e_amt_1","Y");

  if (!wp.colStr("fund_rate_1").equals(wp.colStr("bef_fund_rate_1")))
     wp.colSet("opt_fund_rate_1","Y");

  if (!wp.colStr("fund_amt_1").equals(wp.colStr("bef_fund_amt_1")))
     wp.colSet("opt_fund_amt_1","Y");

  if (!wp.colStr("fund_s_amt_2").equals(wp.colStr("bef_fund_s_amt_2")))
     wp.colSet("opt_fund_s_amt_2","Y");

  if (!wp.colStr("fund_e_amt_2").equals(wp.colStr("bef_fund_e_amt_2")))
     wp.colSet("opt_fund_e_amt_2","Y");

  if (!wp.colStr("fund_rate_2").equals(wp.colStr("bef_fund_rate_2")))
     wp.colSet("opt_fund_rate_2","Y");

  if (!wp.colStr("fund_amt_2").equals(wp.colStr("bef_fund_amt_2")))
     wp.colSet("opt_fund_amt_2","Y");

  if (!wp.colStr("fund_s_amt_3").equals(wp.colStr("bef_fund_s_amt_3")))
     wp.colSet("opt_fund_s_amt_3","Y");

  if (!wp.colStr("fund_e_amt_3").equals(wp.colStr("bef_fund_e_amt_3")))
     wp.colSet("opt_fund_e_amt_3","Y");

  if (!wp.colStr("fund_rate_3").equals(wp.colStr("bef_fund_rate_3")))
     wp.colSet("opt_fund_rate_3","Y");

  if (!wp.colStr("fund_amt_3").equals(wp.colStr("bef_fund_amt_3")))
     wp.colSet("opt_fund_amt_3","Y");

  if (!wp.colStr("fund_s_amt_4").equals(wp.colStr("bef_fund_s_amt_4")))
     wp.colSet("opt_fund_s_amt_4","Y");

  if (!wp.colStr("fund_e_amt_4").equals(wp.colStr("bef_fund_e_amt_4")))
     wp.colSet("opt_fund_e_amt_4","Y");

  if (!wp.colStr("fund_rate_4").equals(wp.colStr("bef_fund_rate_4")))
     wp.colSet("opt_fund_rate_4","Y");

  if (!wp.colStr("fund_amt_4").equals(wp.colStr("bef_fund_amt_4")))
     wp.colSet("opt_fund_amt_4","Y");

  if (!wp.colStr("fund_s_amt_5").equals(wp.colStr("bef_fund_s_amt_5")))
     wp.colSet("opt_fund_s_amt_5","Y");

  if (!wp.colStr("fund_e_amt_5").equals(wp.colStr("bef_fund_e_amt_5")))
     wp.colSet("opt_fund_e_amt_5","Y");

  if (!wp.colStr("fund_rate_5").equals(wp.colStr("bef_fund_rate_5")))
     wp.colSet("opt_fund_rate_5","Y");

  if (!wp.colStr("fund_amt_5").equals(wp.colStr("bef_fund_amt_5")))
     wp.colSet("opt_fund_amt_5","Y");

  if (!wp.colStr("rc_sub_amt").equals(wp.colStr("bef_rc_sub_amt")))
     wp.colSet("opt_rc_sub_amt","Y");

  if (!wp.colStr("rc_sub_rate").equals(wp.colStr("bef_rc_sub_rate")))
     wp.colSet("opt_rc_sub_rate","Y");

  if (!wp.colStr("program_exe_type").equals(wp.colStr("bef_program_exe_type")))
     wp.colSet("opt_program_exe_type","Y");
  commExeType("comm_program_exe_type");
  commExeType("comm_bef_program_exe_type");

  if (!wp.colStr("unlimit_start_month").equals(wp.colStr("bef_unlimit_start_month")))
     wp.colSet("opt_unlimit_start_month","Y");

  if (!wp.colStr("cal_s_month").equals(wp.colStr("bef_cal_s_month")))
     wp.colSet("opt_cal_s_month","Y");

  if (!wp.colStr("cal_e_month").equals(wp.colStr("bef_cal_e_month")))
     wp.colSet("opt_cal_e_month","Y");

  if (!wp.colStr("card_feed_date_s").equals(wp.colStr("bef_card_feed_date_s")))
     wp.colSet("opt_card_feed_date_s","Y");

  if (!wp.colStr("card_feed_date_e").equals(wp.colStr("bef_card_feed_date_e")))
     wp.colSet("opt_card_feed_date_e","Y");

  if (!wp.colStr("card_feed_flag").equals(wp.colStr("bef_card_feed_flag")))
     wp.colSet("opt_card_feed_flag","Y");
  commCardFeedFlag("comm_card_feed_flag");
  commCardFeedFlag("comm_bef_card_feed_flag");

  if (!wp.colStr("cal_months").equals(wp.colStr("bef_cal_months")))
     wp.colSet("opt_cal_months","Y");

  if (!wp.colStr("card_feed_months2").equals(wp.colStr("bef_card_feed_months2")))
     wp.colSet("opt_card_feed_months2","Y");

  if (!wp.colStr("card_feed_days").equals(wp.colStr("bef_card_feed_days")))
     wp.colSet("opt_card_feed_days","Y");

  if (!wp.colStr("new_hldr_sel").equals(wp.colStr("bef_new_hldr_sel")))
     wp.colSet("opt_new_hldr_sel","Y");
  commNewHldrSel("comm_new_hldr_sel");
  commNewHldrSel("comm_bef_new_hldr_sel");

  if (!wp.colStr("feedback_type").equals(wp.colStr("bef_feedback_type")))
     wp.colSet("opt_feedback_type","Y");
  commFeedbackType("comm_feedback_type");
  commFeedbackType("comm_bef_feedback_type");

  if (!wp.colStr("card_feed_run_day").equals(wp.colStr("bef_card_feed_run_day")))
     wp.colSet("opt_card_feed_run_day","Y");

  if (!wp.colStr("feedback_months").equals(wp.colStr("bef_feedback_months")))
     wp.colSet("opt_feedback_months","Y");

  if (!wp.colStr("feedback_cycle_flag").equals(wp.colStr("bef_feedback_cycle_flag")))
     wp.colSet("opt_feedback_cycle_flag","Y");
  commFeedbackCycleFlag("comm_feedback_cycle_flag");
  commFeedbackCycleFlag("comm_bef_feedback_cycle_flag");

  if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
     wp.colSet("opt_feedback_lmt","Y");

  if (!wp.colStr("purch_feed_times").equals(wp.colStr("bef_purch_feed_times")))
     wp.colSet("opt_purch_feed_times","Y");

  if (!wp.colStr("autopay_flag").equals(wp.colStr("bef_autopay_flag")))
     wp.colSet("opt_autopay_flag","Y");

  if (!wp.colStr("mp_flag").equals(wp.colStr("bef_mp_flag")))
     wp.colSet("opt_mp_flag","Y");

  if (!wp.colStr("valid_card_flag").equals(wp.colStr("bef_valid_card_flag")))
     wp.colSet("opt_valid_card_flag","Y");

  if (!wp.colStr("valid_afi_flag").equals(wp.colStr("bef_valid_afi_flag")))
     wp.colSet("opt_valid_afi_flag","Y");

  if (!wp.colStr("ebill_flag").equals(wp.colStr("bef_ebill_flag")))
     wp.colSet("opt_ebill_flag","Y");

  if (!wp.colStr("autopay_digit_cond").equals(wp.colStr("bef_autopay_digit_cond")))
     wp.colSet("opt_autopay_digit_cond","Y");

  if (!wp.colStr("d_txn_cond").equals(wp.colStr("bef_d_txn_cond")))
     wp.colSet("opt_d_txn_cond","Y");

  if (!wp.colStr("d_txn_amt").equals(wp.colStr("bef_d_txn_amt")))
     wp.colSet("opt_d_txn_amt","Y");

  if (!wp.colStr("cancel_period").equals(wp.colStr("bef_cancel_period")))
     wp.colSet("opt_cancel_period","Y");
  commCancelMethod("comm_cancel_period");
  commCancelMethod("comm_bef_cancel_period");

  if (!wp.colStr("cancel_s_month").equals(wp.colStr("bef_cancel_s_month")))
     wp.colSet("opt_cancel_s_month","Y");

  if (!wp.colStr("cancel_scope").equals(wp.colStr("bef_cancel_scope")))
     wp.colSet("opt_cancel_scope","Y");
  commCancelScope("comm_cancel_scope");
  commCancelScope("comm_bef_cancel_scope");

  if (!wp.colStr("d_mcc_code_sel").equals(wp.colStr("bef_d_mcc_code_sel")))
     wp.colSet("opt_d_mcc_code_sel","Y");
  commSelect8("comm_d_mcc_code_sel");
  commSelect8("comm_bef_d_mcc_code_sel");

  wp.colSet("bef_d_mcc_code_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"8"));
  if (!wp.colStr("d_mcc_code_sel_cnt").equals(wp.colStr("bef_d_mcc_code_sel_cnt")))
     wp.colSet("opt_d_mcc_code_sel_cnt","Y");

  if (!wp.colStr("d_merchant_sel").equals(wp.colStr("bef_d_merchant_sel")))
     wp.colSet("opt_d_merchant_sel","Y");
  commSelect6("comm_d_merchant_sel");
  commSelect6("comm_bef_d_merchant_sel");

  wp.colSet("bef_d_merchant_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"6"));
  if (!wp.colStr("d_merchant_sel_cnt").equals(wp.colStr("bef_d_merchant_sel_cnt")))
     wp.colSet("opt_d_merchant_sel_cnt","Y");

  if (!wp.colStr("d_mcht_group_sel").equals(wp.colStr("bef_d_mcht_group_sel")))
     wp.colSet("opt_d_mcht_group_sel","Y");
  commSelectC("comm_d_mcht_group_sel");
  commSelectC("comm_bef_d_mcht_group_sel");

  wp.colSet("bef_d_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"K"));
  if (!wp.colStr("d_mcht_group_sel_cnt").equals(wp.colStr("bef_d_mcht_group_sel_cnt")))
     wp.colSet("opt_d_mcht_group_sel_cnt","Y");

  if (!wp.colStr("d_ucaf_sel").equals(wp.colStr("bef_d_ucaf_sel")))
     wp.colSet("opt_d_ucaf_sel","Y");
  commSelectF("comm_d_ucaf_sel");
  commSelectF("comm_bef_d_ucaf_sel");

  wp.colSet("bef_d_ucaf_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"F"));
  if (!wp.colStr("d_ucaf_sel_cnt").equals(wp.colStr("bef_d_ucaf_sel_cnt")))
     wp.colSet("opt_d_ucaf_sel_cnt","Y");

  if (!wp.colStr("d_eci_sel").equals(wp.colStr("bef_d_eci_sel")))
     wp.colSet("opt_d_eci_sel","Y");
  commSelectG("comm_d_eci_sel");
  commSelectG("comm_bef_d_eci_sel");

  wp.colSet("bef_d_eci_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"G"));
  if (!wp.colStr("d_eci_sel_cnt").equals(wp.colStr("bef_d_eci_sel_cnt")))
     wp.colSet("opt_d_eci_sel_cnt","Y");

  if (!wp.colStr("d_pos_entry_sel").equals(wp.colStr("bef_d_pos_entry_sel")))
     wp.colSet("opt_d_pos_entry_sel","Y");
  commSelectE("comm_d_pos_entry_sel");
  commSelectE("comm_bef_d_pos_entry_sel");

  wp.colSet("bef_d_pos_entry_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"E"));
  if (!wp.colStr("d_pos_entry_sel_cnt").equals(wp.colStr("bef_d_pos_entry_sel_cnt")))
     wp.colSet("opt_d_pos_entry_sel_cnt","Y");

  if (!wp.colStr("cancel_event").equals(wp.colStr("bef_cancel_event")))
     wp.colSet("opt_cancel_event","Y");
  commCancelEvent("comm_cancel_event");
  commCancelEvent("comm_bef_cancel_event");

  if (!wp.colStr("min_mcode").equals(wp.colStr("bef_min_mcode")))
     wp.colSet("opt_min_mcode","Y");

  if (!wp.colStr("cancel_high_amt").equals(wp.colStr("bef_cancel_high_amt")))
     wp.colSet("opt_cancel_high_amt","Y");
  
  if (!wp.colStr("foreign_code").equals(wp.colStr("bef_foreign_code")))
	     wp.colSet("opt_foreign_code","Y");
  commForeignCode("comm_foreign_code");
  commForeignCode("comm_bef_foreign_code");
  
  if (!wp.colStr("hapcare_trust_cond").equals(wp.colStr("bef_hapcare_trust_cond")))
	     wp.colSet("opt_hapcare_trust_cond","Y");

  if (!wp.colStr("hapcare_trust_rate").equals(wp.colStr("bef_hapcare_trust_rate")))
	     wp.colSet("opt_hapcare_trust_rate","Y");
  
  if (!wp.colStr("housing_endow_cond").equals(wp.colStr("bef_housing_endow_cond")))
	     wp.colSet("opt_housing_endow_cond","Y");
  
  if (!wp.colStr("housing_endow_rate").equals(wp.colStr("bef_housing_endow_rate")))
	     wp.colSet("opt_housing_endow_rate","Y");
  
  if (!wp.colStr("happycare_fblmt").equals(wp.colStr("bef_happycare_fblmt")))
	     wp.colSet("opt_happycare_fblmt","Y");
  
  if (!wp.colStr("mortgage_cond").equals(wp.colStr("bef_mortgage_cond")))
	     wp.colSet("opt_mortgage_cond","Y");
  
  if (!wp.colStr("mortgag_rate").equals(wp.colStr("bef_mortgag_rate")))
	     wp.colSet("opt_mortgag_rate","Y");
  
  if (!wp.colStr("mortgage_fblmt").equals(wp.colStr("bef_mortgage_fblmt")))
	     wp.colSet("opt_mortgage_fblmt","Y");
  
  if (!wp.colStr("util_entrustded_cond").equals(wp.colStr("bef_util_entrustded_cond")))
	     wp.colSet("opt_util_entrustded_cond","Y");
  
  if (!wp.colStr("util_entrustded_rate").equals(wp.colStr("bef_util_entrustded_rate")))
	     wp.colSet("opt_util_entrustded_rate","Y");
  
  if (!wp.colStr("util_entrustded_fblmt").equals(wp.colStr("bef_util_entrustded_fblmt")))
	     wp.colSet("opt_util_entrustded_fblmt","Y");
  
  if (!wp.colStr("twpay_cond").equals(wp.colStr("bef_twpay_cond")))
	     wp.colSet("opt_twpay_cond","Y");
  
  if (!wp.colStr("twpay_rate").equals(wp.colStr("bef_twpay_rate")))
	     wp.colSet("opt_twpay_rate","Y");
  
  if (!wp.colStr("tcblife_ec_cond").equals(wp.colStr("bef_tcblife_ec_cond")))
	     wp.colSet("opt_tcblife_ec_cond","Y");
  
  if (!wp.colStr("tcblife_ec_rate").equals(wp.colStr("bef_tcblife_ec_rate")))
	     wp.colSet("opt_tcblife_ec_rate","Y");
  
  if (!wp.colStr("eco_fblmt").equals(wp.colStr("bef_eco_fblmt")))
	     wp.colSet("opt_eco_fblmt","Y");
  
  if (!wp.colStr("extratwpay_cond").equals(wp.colStr("bef_extratwpay_cond")))
	     wp.colSet("opt_extratwpay_cond","Y");
  
  if (!wp.colStr("onlyaddon_calcond").equals(wp.colStr("bef_onlyaddon_calcond")))
	     wp.colSet("opt_onlyaddon_calcond","Y");
  
//  if (!wp.colStr("extratwpay_rate").equals(wp.colStr("bef_extratwpay_rate")))
//	     wp.colSet("opt_extratwpay_rate","Y");
//  
//  if (!wp.colStr("extratwpay_fblmt").equals(wp.colStr("bef_extratwpay_fblmt")))
//	     wp.colSet("opt_extratwpay_fblmt","Y");
  
  if (!wp.colStr("mcht_cname_sel").equals(wp.colStr("bef_mcht_cname_sel")))
      wp.colSet("opt_mcht_cname_sel", "Y");
    commMchtCname("comm_mcht_cname_sel");
    commMchtCname("comm_bef_mcht_cname_sel");

    wp.colSet("bef_mcht_cname_sel_cnt",
    		listPtrFundCData("ptr_fund_cdata", "PTR_FUNDP", wp.colStr("fund_code"), "A"));
    if (!wp.colStr("mcht_cname_sel_cnt").equals(wp.colStr("bef_mcht_cname_sel_cnt")))
      wp.colSet("opt_mcht_cname_sel_cnt", "Y");

    if (!wp.colStr("mcht_ename_sel").equals(wp.colStr("bef_mcht_ename_sel")))
      wp.colSet("opt_mcht_ename_sel", "Y");
    commMchtEname("comm_mcht_ename_sel");
    commMchtEname("comm_bef_mcht_ename_sel");

    wp.colSet("bef_mcht_ename_sel_cnt",
    		listPtrFundCData("ptr_fund_cdata", "PTR_FUNDP", wp.colStr("fund_code"), "B"));
    if (!wp.colStr("mcht_ename_sel_cnt").equals(wp.colStr("bef_mcht_ename_sel_cnt")))
      wp.colSet("opt_mcht_ename_sel_cnt", "Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("fund_name","");
       wp.colSet("tran_base","");
       wp.colSet("fund_crt_date_s","");
       wp.colSet("fund_crt_date_e","");
       wp.colSet("effect_type","");
       wp.colSet("effect_months","");
       wp.colSet("effect_years","");
       wp.colSet("effect_fix_month","");
       wp.colSet("stop_flag","");
       wp.colSet("stop_date","");
       wp.colSet("stop_desc","");
       wp.colSet("bin_type_sel","");
       wp.colSet("bin_type_sel_cnt","");
       wp.colSet("acct_type_sel","");
       wp.colSet("acct_type_sel_cnt","");
       wp.colSet("group_code_sel","");
       wp.colSet("group_code_sel_cnt","");
       wp.colSet("card_type_sel","");
       wp.colSet("card_type_sel_cnt","");
       wp.colSet("new_hldr_cond","");
       wp.colSet("new_hldr_flag","");
       wp.colSet("new_card_days","");
       wp.colSet("new_hldr_days","");
       wp.colSet("new_group_cond","");
       wp.colSet("new_gp_cnt","");
       wp.colSet("new_hldr_card","");
       wp.colSet("new_hldr_sup","");
       wp.colSet("apply_age_cond","");
       wp.colSet("apply_age_s","");
       wp.colSet("apply_age_e","");
       wp.colSet("activate_cond","");
       wp.colSet("activate_flag","");
       wp.colSet("valid_period","");
       wp.colSet("cobrand_code","");
       wp.colSet("source_code_sel","");
       wp.colSet("source_code_sel_cnt","");
       wp.colSet("merchant_sel","");
       wp.colSet("merchant_sel_cnt","");
       wp.colSet("mcht_group_sel","");
       wp.colSet("mcht_group_sel_cnt","");
       wp.colSet("platform_kind_sel","");
       wp.colSet("platform_kind_sel_cnt","");
       wp.colSet("currency_sel","");
       wp.colSet("currency_sel_cnt","");
       wp.colSet("ex_currency_sel","");
       wp.colSet("ex_currency_sel_cnt","");
       wp.colSet("pos_entry_sel","");
       wp.colSet("pos_entry_sel_cnt","");
       wp.colSet("pos_merchant_sel","");
       wp.colSet("pos_merchant_sel_cnt","");
       wp.colSet("pos_mcht_group_sel","");
       wp.colSet("pos_mcht_group_sel_cnt","");
       wp.colSet("bl_cond","");
       wp.colSet("ca_cond","");
       wp.colSet("id_cond","");
       wp.colSet("ao_cond","");
       wp.colSet("it_cond","");
       wp.colSet("ot_cond","");
       wp.colSet("purch_feed_flag","");
       wp.colSet("purch_date_s","");
       wp.colSet("purch_date_e","");
       wp.colSet("purch_reclow_cond","");
       wp.colSet("purch_reclow_amt","");
       wp.colSet("purch_rec_amt_cond","");
       wp.colSet("purch_rec_amt","");
       wp.colSet("purch_tol_amt_cond","");
       wp.colSet("purch_tol_amt","");
       wp.colSet("purch_tol_time_cond","");
       wp.colSet("purch_tol_time","");
       wp.colSet("purch_feed_type","");
       wp.colSet("purch_type","");
       wp.colSet("purch_feed_amt","");
       wp.colSet("purch_feed_rate","");
       wp.colSet("fund_feed_flag","");
       wp.colSet("threshold_sel","");
       wp.colSet("purchase_type_sel","");
       wp.colSet("fund_s_amt_1","");
       wp.colSet("fund_e_amt_1","");
       wp.colSet("fund_rate_1","");
       wp.colSet("fund_amt_1","");
       wp.colSet("fund_s_amt_2","");
       wp.colSet("fund_e_amt_2","");
       wp.colSet("fund_rate_2","");
       wp.colSet("fund_amt_2","");
       wp.colSet("fund_s_amt_3","");
       wp.colSet("fund_e_amt_3","");
       wp.colSet("fund_rate_3","");
       wp.colSet("fund_amt_3","");
       wp.colSet("fund_s_amt_4","");
       wp.colSet("fund_e_amt_4","");
       wp.colSet("fund_rate_4","");
       wp.colSet("fund_amt_4","");
       wp.colSet("fund_s_amt_5","");
       wp.colSet("fund_e_amt_5","");
       wp.colSet("fund_rate_5","");
       wp.colSet("fund_amt_5","");
       wp.colSet("rc_sub_amt","");
       wp.colSet("rc_sub_rate","");
       wp.colSet("program_exe_type","");
       wp.colSet("unlimit_start_month","");
       wp.colSet("cal_s_month","");
       wp.colSet("cal_e_month","");
       wp.colSet("card_feed_date_s","");
       wp.colSet("card_feed_date_e","");
       wp.colSet("card_feed_flag","");
       wp.colSet("cal_months","");
       wp.colSet("card_feed_months2","");
       wp.colSet("card_feed_days","");
       wp.colSet("new_hldr_sel","");
       wp.colSet("feedback_type","");
       wp.colSet("card_feed_run_day","");
       wp.colSet("feedback_months","");
       wp.colSet("feedback_cycle_flag","");
       wp.colSet("feedback_lmt","");
       wp.colSet("purch_feed_times","");
       wp.colSet("autopay_flag","");
       wp.colSet("mp_flag","");
       wp.colSet("valid_card_flag","");
       wp.colSet("valid_afi_flag","");
       wp.colSet("ebill_flag","");
       wp.colSet("autopay_digit_cond","");
       wp.colSet("d_txn_cond","");
       wp.colSet("d_txn_amt","");
       wp.colSet("cancel_period","");
       wp.colSet("cancel_s_month","");
       wp.colSet("cancel_scope","");
       wp.colSet("d_mcc_code_sel","");
       wp.colSet("d_mcc_code_sel_cnt","");
       wp.colSet("d_merchant_sel","");
       wp.colSet("d_merchant_sel_cnt","");
       wp.colSet("d_mcht_group_sel","");
       wp.colSet("d_mcht_group_sel_cnt","");
       wp.colSet("d_ucaf_sel","");
       wp.colSet("d_ucaf_sel_cnt","");
       wp.colSet("d_eci_sel","");
       wp.colSet("d_eci_sel_cnt","");
       wp.colSet("d_pos_entry_sel","");
       wp.colSet("d_pos_entry_sel_cnt","");
       wp.colSet("cancel_event","");
       wp.colSet("min_mcode","");
       wp.colSet("cancel_high_amt","");
       wp.colSet("foreign_code","");
       wp.colSet("mcht_cname_sel","");
       wp.colSet("mcht_ename_sel","");
       wp.colSet("hapcare_trust_cond","");
       wp.colSet("hapcare_trust_rate","");
       wp.colSet("housing_endow_cond","");
       wp.colSet("housing_endow_rate","");
       wp.colSet("happycare_fblmt","");
       wp.colSet("mortgage_cond","");
       wp.colSet("mortgag_rate","");
       wp.colSet("mortgage_fblmt","");
       wp.colSet("util_entrustded_cond","");
       wp.colSet("util_entrustded_rate","");
       wp.colSet("util_entrustded_fblmt","");
       wp.colSet("twpay_cond","");
       wp.colSet("twpay_rate","");
       wp.colSet("tcblife_ec_cond","");
       wp.colSet("tcblife_ec_rate","");
       wp.colSet("eco_fblmt","");
       wp.colSet("extratwpay_cond","");
       wp.colSet("onlyaddon_calcond", "");
//       wp.colSet("extratwpay_rate","");
//       wp.colSet("extratwpay_fblmt","");
       wp.colSet("mcht_cname_sel","");
       wp.colSet("mcht_cname_sel_cnt","");
       wp.colSet("mcht_ename_sel","");
       wp.colSet("mcht_ename_sel_cnt","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("fund_name").length()==0)
     wp.colSet("opt_fund_name","Y");

  if (wp.colStr("tran_base").length()==0)
     wp.colSet("opt_tran_base","Y");

  if (wp.colStr("fund_crt_date_s").length()==0)
     wp.colSet("opt_fund_crt_date_s","Y");

  if (wp.colStr("fund_crt_date_e").length()==0)
     wp.colSet("opt_fund_crt_date_e","Y");

  if (wp.colStr("effect_type").length()==0)
     wp.colSet("opt_effect_type","Y");

  if (wp.colStr("effect_months").length()==0)
     wp.colSet("opt_effect_months","Y");

  if (wp.colStr("effect_years").length()==0)
     wp.colSet("opt_effect_years","Y");

  if (wp.colStr("effect_fix_month").length()==0)
     wp.colSet("opt_effect_fix_month","Y");

  if (wp.colStr("stop_flag").length()==0)
     wp.colSet("opt_stop_flag","Y");

  if (wp.colStr("stop_date").length()==0)
     wp.colSet("opt_stop_date","Y");

  if (wp.colStr("stop_desc").length()==0)
     wp.colSet("opt_stop_desc","Y");

  if (wp.colStr("bin_type_sel").length()==0)
     wp.colSet("opt_bin_type_sel","Y");


  if (wp.colStr("acct_type_sel").length()==0)
     wp.colSet("opt_acct_type_sel","Y");


  if (wp.colStr("group_code_sel").length()==0)
     wp.colSet("opt_group_code_sel","Y");


  if (wp.colStr("card_type_sel").length()==0)
     wp.colSet("opt_card_type_sel","Y");


  if (wp.colStr("new_hldr_cond").length()==0)
     wp.colSet("opt_new_hldr_cond","Y");

  if (wp.colStr("new_hldr_flag").length()==0)
     wp.colSet("opt_new_hldr_flag","Y");

  if (wp.colStr("new_card_days").length()==0)
     wp.colSet("opt_new_card_days","Y");

  if (wp.colStr("new_hldr_days").length()==0)
     wp.colSet("opt_new_hldr_days","Y");

  if (wp.colStr("new_group_cond").length()==0)
     wp.colSet("opt_new_group_cond","Y");


  if (wp.colStr("new_hldr_card").length()==0)
     wp.colSet("opt_new_hldr_card","Y");

  if (wp.colStr("new_hldr_sup").length()==0)
     wp.colSet("opt_new_hldr_sup","Y");

  if (wp.colStr("apply_age_cond").length()==0)
     wp.colSet("opt_apply_age_cond","Y");

  if (wp.colStr("apply_age_s").length()==0)
     wp.colSet("opt_apply_age_s","Y");

  if (wp.colStr("apply_age_e").length()==0)
     wp.colSet("opt_apply_age_e","Y");

  if (wp.colStr("activate_cond").length()==0)
     wp.colSet("opt_activate_cond","Y");

  if (wp.colStr("activate_flag").length()==0)
     wp.colSet("opt_activate_flag","Y");

  if (wp.colStr("valid_period").length()==0)
     wp.colSet("opt_valid_period","Y");

  if (wp.colStr("cobrand_code").length()==0)
     wp.colSet("opt_cobrand_code","Y");

  if (wp.colStr("source_code_sel").length()==0)
     wp.colSet("opt_source_code_sel","Y");


  if (wp.colStr("merchant_sel").length()==0)
     wp.colSet("opt_merchant_sel","Y");


  if (wp.colStr("mcht_group_sel").length()==0)
     wp.colSet("opt_mcht_group_sel","Y");
  
  if (wp.colStr("platform_kind_sel").length()==0)
	     wp.colSet("opt_platform_kind_sel","Y");

  if (wp.colStr("currency_sel").length()==0)
     wp.colSet("opt_currency_sel","Y");


  if (wp.colStr("ex_currency_sel").length()==0)
     wp.colSet("opt_ex_currency_sel","Y");


  if (wp.colStr("pos_entry_sel").length()==0)
     wp.colSet("opt_pos_entry_sel","Y");


  if (wp.colStr("pos_merchant_sel").length()==0)
     wp.colSet("opt_pos_merchant_sel","Y");


  if (wp.colStr("pos_mcht_group_sel").length()==0)
     wp.colSet("opt_pos_mcht_group_sel","Y");


  if (wp.colStr("bl_cond").length()==0)
     wp.colSet("opt_bl_cond","Y");

  if (wp.colStr("ca_cond").length()==0)
     wp.colSet("opt_ca_cond","Y");

  if (wp.colStr("id_cond").length()==0)
     wp.colSet("opt_id_cond","Y");

  if (wp.colStr("ao_cond").length()==0)
     wp.colSet("opt_ao_cond","Y");

  if (wp.colStr("it_cond").length()==0)
     wp.colSet("opt_it_cond","Y");

  if (wp.colStr("ot_cond").length()==0)
     wp.colSet("opt_ot_cond","Y");

  if (wp.colStr("purch_feed_flag").length()==0)
     wp.colSet("opt_purch_feed_flag","Y");

  if (wp.colStr("purch_date_s").length()==0)
     wp.colSet("opt_purch_date_s","Y");

  if (wp.colStr("purch_date_e").length()==0)
     wp.colSet("opt_purch_date_e","Y");

  if (wp.colStr("purch_reclow_cond").length()==0)
     wp.colSet("opt_purch_reclow_cond","Y");

  if (wp.colStr("purch_reclow_amt").length()==0)
     wp.colSet("opt_purch_reclow_amt","Y");

  if (wp.colStr("purch_rec_amt_cond").length()==0)
     wp.colSet("opt_purch_rec_amt_cond","Y");

  if (wp.colStr("purch_rec_amt").length()==0)
     wp.colSet("opt_purch_rec_amt","Y");

  if (wp.colStr("purch_tol_amt_cond").length()==0)
     wp.colSet("opt_purch_tol_amt_cond","Y");

  if (wp.colStr("purch_tol_amt").length()==0)
     wp.colSet("opt_purch_tol_amt","Y");

  if (wp.colStr("purch_tol_time_cond").length()==0)
     wp.colSet("opt_purch_tol_time_cond","Y");

  if (wp.colStr("purch_tol_time").length()==0)
     wp.colSet("opt_purch_tol_time","Y");

  if (wp.colStr("purch_feed_type").length()==0)
     wp.colSet("opt_purch_feed_type","Y");

  if (wp.colStr("purch_type").length()==0)
     wp.colSet("opt_purch_type","Y");

  if (wp.colStr("purch_feed_amt").length()==0)
     wp.colSet("opt_purch_feed_amt","Y");

  if (wp.colStr("purch_feed_rate").length()==0)
     wp.colSet("opt_purch_feed_rate","Y");

  if (wp.colStr("fund_feed_flag").length()==0)
     wp.colSet("opt_fund_feed_flag","Y");

  if (wp.colStr("threshold_sel").length()==0)
     wp.colSet("opt_threshold_sel","Y");

  if (wp.colStr("purchase_type_sel").length()==0)
     wp.colSet("opt_purchase_type_sel","Y");

  if (wp.colStr("fund_s_amt_1").length()==0)
     wp.colSet("opt_fund_s_amt_1","Y");

  if (wp.colStr("fund_e_amt_1").length()==0)
     wp.colSet("opt_fund_e_amt_1","Y");

  if (wp.colStr("fund_rate_1").length()==0)
     wp.colSet("opt_fund_rate_1","Y");

  if (wp.colStr("fund_amt_1").length()==0)
     wp.colSet("opt_fund_amt_1","Y");

  if (wp.colStr("fund_s_amt_2").length()==0)
     wp.colSet("opt_fund_s_amt_2","Y");

  if (wp.colStr("fund_e_amt_2").length()==0)
     wp.colSet("opt_fund_e_amt_2","Y");

  if (wp.colStr("fund_rate_2").length()==0)
     wp.colSet("opt_fund_rate_2","Y");

  if (wp.colStr("fund_amt_2").length()==0)
     wp.colSet("opt_fund_amt_2","Y");

  if (wp.colStr("fund_s_amt_3").length()==0)
     wp.colSet("opt_fund_s_amt_3","Y");

  if (wp.colStr("fund_e_amt_3").length()==0)
     wp.colSet("opt_fund_e_amt_3","Y");

  if (wp.colStr("fund_rate_3").length()==0)
     wp.colSet("opt_fund_rate_3","Y");

  if (wp.colStr("fund_amt_3").length()==0)
     wp.colSet("opt_fund_amt_3","Y");

  if (wp.colStr("fund_s_amt_4").length()==0)
     wp.colSet("opt_fund_s_amt_4","Y");

  if (wp.colStr("fund_e_amt_4").length()==0)
     wp.colSet("opt_fund_e_amt_4","Y");

  if (wp.colStr("fund_rate_4").length()==0)
     wp.colSet("opt_fund_rate_4","Y");

  if (wp.colStr("fund_amt_4").length()==0)
     wp.colSet("opt_fund_amt_4","Y");

  if (wp.colStr("fund_s_amt_5").length()==0)
     wp.colSet("opt_fund_s_amt_5","Y");

  if (wp.colStr("fund_e_amt_5").length()==0)
     wp.colSet("opt_fund_e_amt_5","Y");

  if (wp.colStr("fund_rate_5").length()==0)
     wp.colSet("opt_fund_rate_5","Y");

  if (wp.colStr("fund_amt_5").length()==0)
     wp.colSet("opt_fund_amt_5","Y");

  if (wp.colStr("rc_sub_amt").length()==0)
     wp.colSet("opt_rc_sub_amt","Y");

  if (wp.colStr("rc_sub_rate").length()==0)
     wp.colSet("opt_rc_sub_rate","Y");

  if (wp.colStr("program_exe_type").length()==0)
     wp.colSet("opt_program_exe_type","Y");

  if (wp.colStr("unlimit_start_month").length()==0)
     wp.colSet("opt_unlimit_start_month","Y");

  if (wp.colStr("cal_s_month").length()==0)
     wp.colSet("opt_cal_s_month","Y");

  if (wp.colStr("cal_e_month").length()==0)
     wp.colSet("opt_cal_e_month","Y");

  if (wp.colStr("card_feed_date_s").length()==0)
     wp.colSet("opt_card_feed_date_s","Y");

  if (wp.colStr("card_feed_date_e").length()==0)
     wp.colSet("opt_card_feed_date_e","Y");

  if (wp.colStr("card_feed_flag").length()==0)
     wp.colSet("opt_card_feed_flag","Y");

  if (wp.colStr("cal_months").length()==0)
     wp.colSet("opt_cal_months","Y");

  if (wp.colStr("card_feed_months2").length()==0)
     wp.colSet("opt_card_feed_months2","Y");

  if (wp.colStr("card_feed_days").length()==0)
     wp.colSet("opt_card_feed_days","Y");

  if (wp.colStr("new_hldr_sel").length()==0)
     wp.colSet("opt_new_hldr_sel","Y");

  if (wp.colStr("feedback_type").length()==0)
     wp.colSet("opt_feedback_type","Y");

  if (wp.colStr("card_feed_run_day").length()==0)
     wp.colSet("opt_card_feed_run_day","Y");

  if (wp.colStr("feedback_months").length()==0)
     wp.colSet("opt_feedback_months","Y");

  if (wp.colStr("feedback_cycle_flag").length()==0)
     wp.colSet("opt_feedback_cycle_flag","Y");

  if (wp.colStr("feedback_lmt").length()==0)
     wp.colSet("opt_feedback_lmt","Y");

  if (wp.colStr("purch_feed_times").length()==0)
     wp.colSet("opt_purch_feed_times","Y");

  if (wp.colStr("autopay_flag").length()==0)
     wp.colSet("opt_autopay_flag","Y");

  if (wp.colStr("mp_flag").length()==0)
     wp.colSet("opt_mp_flag","Y");

  if (wp.colStr("valid_card_flag").length()==0)
     wp.colSet("opt_valid_card_flag","Y");

  if (wp.colStr("valid_afi_flag").length()==0)
     wp.colSet("opt_valid_afi_flag","Y");

  if (wp.colStr("ebill_flag").length()==0)
     wp.colSet("opt_ebill_flag","Y");

  if (wp.colStr("autopay_digit_cond").length()==0)
     wp.colSet("opt_autopay_digit_cond","Y");

  if (wp.colStr("d_txn_cond").length()==0)
     wp.colSet("opt_d_txn_cond","Y");

  if (wp.colStr("d_txn_amt").length()==0)
     wp.colSet("opt_d_txn_amt","Y");

  if (wp.colStr("cancel_period").length()==0)
     wp.colSet("opt_cancel_period","Y");

  if (wp.colStr("cancel_s_month").length()==0)
     wp.colSet("opt_cancel_s_month","Y");

  if (wp.colStr("cancel_scope").length()==0)
     wp.colSet("opt_cancel_scope","Y");

  if (wp.colStr("d_mcc_code_sel").length()==0)
     wp.colSet("opt_d_mcc_code_sel","Y");


  if (wp.colStr("d_merchant_sel").length()==0)
     wp.colSet("opt_d_merchant_sel","Y");


  if (wp.colStr("d_mcht_group_sel").length()==0)
     wp.colSet("opt_d_mcht_group_sel","Y");


  if (wp.colStr("d_ucaf_sel").length()==0)
     wp.colSet("opt_d_ucaf_sel","Y");


  if (wp.colStr("d_eci_sel").length()==0)
     wp.colSet("opt_d_eci_sel","Y");


  if (wp.colStr("d_pos_entry_sel").length()==0)
     wp.colSet("opt_d_pos_entry_sel","Y");


  if (wp.colStr("cancel_event").length()==0)
     wp.colSet("opt_cancel_event","Y");

  if (wp.colStr("min_mcode").length()==0)
     wp.colSet("opt_min_mcode","Y");

  if (wp.colStr("cancel_high_amt").length()==0)
     wp.colSet("opt_cancel_high_amt","Y");

 }
// ************************************************************************
 public void dataReadR2() throws Exception
 {
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "ptr_fund_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               + " and table_name  =  'PTR_FUNDP' "
               ;
   if (wp.respHtml.equals("mktp6220_bint"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktp6220_acty"))
      wp.whereStr  += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktp6220_gpcd"))
      wp.whereStr  += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktp6220_dype"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktp6220_gnce"))
      wp.whereStr  += " and data_type  = '0' ";
   if (wp.respHtml.equals("mktp6220_srcd"))
      wp.whereStr  += " and data_type  = 'A' ";
   if (wp.respHtml.equals("mktp6220_aaa1"))
      wp.whereStr  += " and data_type  = 'H' ";
   if (wp.respHtml.equals("mktp6220_aaa3"))
	  wp.whereStr  += " and data_type  = 'P' ";
   if (wp.respHtml.equals("mktp6220_aaam"))
      wp.whereStr  += " and data_type  = 'M' ";
   if (wp.respHtml.equals("mktp6220_mccd"))
      wp.whereStr  += " and data_type  = '8' ";
   if (wp.respHtml.equals("mktp6220_aaa2"))
      wp.whereStr  += " and data_type  = 'K' ";
   if (wp.respHtml.equals("mktp6220_ucaf"))
      wp.whereStr  += " and data_type  = 'F' ";
   if (wp.respHtml.equals("mktp6220_deci"))
      wp.whereStr  += " and data_type  = 'G' ";
   if (wp.respHtml.equals("mktp6220_posd"))
      wp.whereStr  += " and data_type  = 'E' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7005)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp6220_acty"))
    commAcctType("comm_data_code");
   if (wp.respHtml.equals("mktp6220_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp6220_dype"))
    commDataCode02("comm_data_code");
   if (wp.respHtml.equals("mktp6220_gnce"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp6220_srcd"))
    commSrcCode("comm_data_code");
   if (wp.respHtml.equals("mktp6220_aaa1"))
    commMechtGp("comm_data_code","1");
   if (wp.respHtml.equals("mktp6220_aaa3"))
	commMechtGp("comm_data_code","2");
   if (wp.respHtml.equals("mktp6220_aaam"))
    commMechtGp("comm_data_code","1");
   if (wp.respHtml.equals("mktp6220_mccd"))
    commDataCode08("comm_data_code");
   if (wp.respHtml.equals("mktp6220_aaa2"))
    commMechtGp("comm_data_code","1");
   if (wp.respHtml.equals("mktp6220_posd"))
    commEntryModed("comm_data_code");
  }
// ************************************************************************
 public void dataReadR3() throws Exception
 {
  dataReadR3(0);
 }
// ************************************************************************
 public void dataReadR3(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "ptr_fund_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "data_code2, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               + " and table_name  =  'PTR_FUNDP' "
               ;
   if (wp.respHtml.equals("mktp6220_mrch"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktp6220_mrck"))
      wp.whereStr  += " and data_type  = 'C' ";
   if (wp.respHtml.equals("mktp6220_mrcd"))
      wp.whereStr  += " and data_type  = '6' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7005)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp6220_mrch"))
    commDataType2("comm_data_code2");
   if (wp.respHtml.equals("mktp6220_mrck"))
    commDataType2("comm_data_code2");
   if (wp.respHtml.equals("mktp6220_mrcd"))
    commDataType2("comm_data_code2");
  }
// ************************************************************************
 public void dataReadR4() throws Exception
 {
  dataReadR4(0);
 }
// ************************************************************************
 public void dataReadR4(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "ptr_fund_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "data_code2, "
                + "data_code3, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               + " and table_name  =  'PTR_FUNDP' "
               ;
   if (wp.respHtml.equals("mktp6220_cocq"))
      wp.whereStr  += " and data_type  = '7' ";
   if (wp.respHtml.equals("mktp6220_cocd"))
      wp.whereStr  += " and data_type  = '9' ";
   if (wp.respHtml.equals("mktp6220_pose"))
      wp.whereStr  += " and data_type  = 'B' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7005)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp6220_cocq"))
    commDataCodeCocq("comm_data_code");
   if (wp.respHtml.equals("mktp6220_cocq"))
    commDataCode2Cocq("comm_data_code2");
   if (wp.respHtml.equals("mktp6220_cocq"))
    commDataCode3Cocq("comm_data_code3");
   if (wp.respHtml.equals("mktp6220_cocd"))
    commDataCodeCocd("comm_data_code");
   if (wp.respHtml.equals("mktp6220_cocd"))
    commDataCode2Cocd("comm_data_code2");
   if (wp.respHtml.equals("mktp6220_cocd"))
    commDataCode3Cocd("comm_data_code3");
   if (wp.respHtml.equals("mktp6220_pose"))
    commEntryMode("comm_data_code2");
   if (wp.respHtml.equals("mktp6220_pose"))
    commEntryMode("comm_data_code");
  }
 public void dataReadR5() throws Exception
 {
 dataReadR5(0);
 }
 //************************************************************************
     public void dataReadR5(int fromType) throws Exception {
         String bnTable = "";

         if ((wp.itemStr("fund_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
             alertErr("鍵值為空白或主檔未新增 ");
             return;
         }
         wp.selectCnt = 1;
         this.selectNoLimit();
         if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
             buttonOff("btnUpdate_disable");
             buttonOff("newDetail_disable");
             bnTable = "ptr_fund_cdata";
         } else {
             wp.colSet("btnUpdate_disable", "");
             wp.colSet("newDetail_disable", "");
             bnTable = "ptr_fund_cdata_t";
         }

         wp.selectSQL = "hex(rowid) as r2_rowid, "
                 + "ROW_NUMBER()OVER() as ser_num, "
                 + "0 as r2_mod_seqno, "
                 + "data_key, "
                 + "data_code, "
                 + "mod_user as r2_mod_user ";
         wp.daoTable = bnTable;
         wp.whereStr = "where 1=1" + " and table_name  =  'PTR_FUNDP' ";
         if (wp.respHtml.equals("mktp6220_namc"))
             wp.whereStr += " and data_type  = 'A' ";
         if (wp.respHtml.equals("mktp6220_name"))
             wp.whereStr += " and data_type  = 'B' ";
         String whereCnt = wp.whereStr;
         wp.whereStr += " and  data_key = :data_key ";
         setString("data_key", wp.itemStr("fund_code"));
         whereCnt += " and  data_key = '" + wp.itemStr("fund_code") + "'";
         wp.whereStr += " order by 4,5,6 ";
         int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
         if (cnt1 > 300) {
             alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
             buttonOff("btnUpdate_disable");
             buttonOff("newDetail_disable");
             return;
         }

         pageQuery();
         wp.setListCount(1);
         wp.notFound = "";

         wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
     }
// ************************************************************************
 public int selectBndataCount(String bndataTable,String whereStr ) throws Exception
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndataTable
               + " " + whereStr
               ;

   sqlSelect(sql1);

   return((int)sqlNum("bndataCount"));
 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  mktp02.Mktp6220Func func =new mktp02.Mktp6220Func(wp);

  String[] lsFundCode = wp.itemBuff("fund_code");
  String[] lsAudType  = wp.itemBuff("aud_type");
  String[] lsCrtUser  = wp.itemBuff("crt_user");
  String[] lsRowid     = wp.itemBuff("rowid");
  String[] opt =wp.itemBuff("opt");
  wp.listCount[0] = lsAudType.length;

  int rr = -1;
  wp.selectCnt = lsAudType.length;
  for (int ii = 0; ii < opt.length; ii++)
    {
     if (opt[ii].length()==0) continue;
     rr = (int) (this.toNum(opt[ii])%20 - 1);
     if (rr==-1) rr = 19;
     if (rr<0) continue;

     wp.colSet(rr,"ok_flag","-");
     if (lsCrtUser[rr].equals(wp.loginUser))
        {
         ilAuth++;
         wp.colSet(rr,"ok_flag","F");
         continue;
        }

     lsUser=lsCrtUser[rr];
     if (!apprBankUnit(lsUser,wp.loginUser))
        {
         ilAuth++;
         wp.colSet(rr,"ok_flag","B");
         continue;
        }

     func.varsSet("fund_code", lsFundCode[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        {
        rc =func.dbInsertA4();
        if (rc==1) rc = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc == 1)
        	rc = func.dbInsertA4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
        }
     else if (lsAudType[rr].equals("U"))
        {
        rc =func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4Bndata();
        if (rc==1) rc  = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc == 1)
            rc = func.dbDeleteD4BnCdata();
        if (rc == 1)
            rc = func.dbInsertA4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
        }
     else if (lsAudType[rr].equals("D"))
        {
         rc =func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc == 1)
            rc = func.dbDeleteD4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
        }

     if (rc!=1) alertErr(func.getMsg());
     if (rc == 1)
        {
         commCrtUser("comm_crt_user");
         commTranBase("comm_tran_base");
         commfuncAudType("aud_type");

         wp.colSet(rr,"ok_flag","V");
         ilOk++;
         func.dbDelete();
         this.sqlCommit(rc);
         continue;
        }
     ilErr++;
     wp.colSet(rr,"ok_flag","X");
     this.sqlCommit(0);
    }

  alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr+"; 權限問題=" + ilAuth);
  buttonOff("btnAdd_disable");
 }
// ************************************************************************
 @Override
 public void initButton()
 {
  if (wp.respHtml.indexOf("_detl") > 0)
     {
      this.btnModeAud();
     }
  int rr = 0;                       
  rr = wp.listCount[0];             
  wp.colSet(0, "IND_NUM", "" + rr);
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktp6220")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_fund_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_fund_code");
             }
          lsSql = "";
          lsSql =  procDynamicDddwFundCode1(wp.colStr("ex_fund_code"));
          wp.optionKey = wp.colStr("ex_fund_code");
          dddwList("dddw_fund_code_1", lsSql);
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_crt_user").length()>0)
             {
             wp.optionKey = wp.colStr("ex_crt_user");
             }
          lsSql = "";
          lsSql =  procDynamicDddwCrtUser1(wp.colStr("ex_crt_user"));
          wp.optionKey = wp.colStr("ex_crt_user");
          dddwList("dddw_crt_user_1", lsSql);
         }
       if ((wp.respHtml.equals("mktp6220_srcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_source_code3"
                 ,"ptr_src_code"
                 ,"trim(source_code)"
                 ,"trim(source_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_mrch")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_type1"
                 ,"mkt_rcv_bin"
                 ,"trim(bank_no)"
                 ,"trim(ica_desc)"
                 ," where bank_no !=''");
         }
       if ((wp.respHtml.equals("mktp6220_mrck")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_type1"
                 ,"bil_auto_ica"
                 ,"trim(bank_no)"
                 ,"trim(ica_desc)"
                 ," where bank_no !=''");
         }
       if ((wp.respHtml.equals("mktp6220_mrcd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_type1"
                 ,"bil_auto_ica"
                 ,"trim(bank_no)"
                 ,"trim(ica_desc)"
                 ," where bank_no !=''");
         }
       if ((wp.respHtml.equals("mktp6220_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and  platform_flag != '2' ");
         }
       if ((wp.respHtml.equals("mktp6220_aaa3")))
       {
        wp.initOption ="";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp"
               ,"mkt_mcht_gp"
               ,"trim(mcht_group_id)"
               ,"trim(mcht_group_desc)"
               ," where 1 = 1 and platform_flag = '2' ");
       }
       if ((wp.respHtml.equals("mktp6220_aaam")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and platform_flag != '2' ");
         }
       if ((wp.respHtml.equals("mktp6220_aaa2")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp1"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and platform_flag != '2' ");
         }
       if ((wp.respHtml.equals("mktp6220_cocq")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_code_071"
                 ,""
                        );
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_currcode"
                 ,"ptr_currcode"
                 ,"trim(curr_code)"
                 ,"trim(curr_chi_name)"
                 ," where curr_chi_name!='' order by curr_code");
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_cocd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_code_071"
                 ,""
                        );
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_currcode"
                 ,"ptr_currcode"
                 ,"trim(curr_code)"
                 ,"trim(curr_chi_name)"
                 ," where curr_chi_name!='' order by curr_code");
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07d"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_pose")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_bin_typeB"
                 ,"ptr_bintable"
                 ,"trim(bin_type)"
                 ,""
                 ," group by bin_type");
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_entry_modeB"
                 ,"cca_entry_mode"
                 ,"trim(entry_mode)"
                 ,"trim(mode_desc)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_bint")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_bin_typeB"
                 ,"ptr_bintable"
                 ,"trim(bin_type)"
                 ,""
                 ," group by bin_type");
         }
       if ((wp.respHtml.equals("mktp6220_posd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_entry_mode"
                 ,"cca_entry_mode"
                 ,"trim(entry_mode)"
                 ,"trim(mode_desc)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_acty")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_gnce")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_dype")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type1"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_mccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp6220_pose")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_entry_mode"
                 ,"cca_entry_mode"
                 ,"trim(entry_mode)"
                 ,"trim(mode_desc)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
  void commfuncAudType(String s1)
   {
    if (s1==null || s1.trim().length()==0) return;
    String[] cde = {"Y","A","U","D"};
    String[] txt = {"未異動","新增待覆核","更新待覆核","刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++)
      {
        wp.colSet(ii,"comm_func_"+s1, "");
        for (int inti=0;inti<cde.length;inti++)
           if (wp.colStr(ii,s1).equals(cde[inti]))
              {
               wp.colSet(ii,"commfunc_"+s1, txt[inti]);
               break;
              }
      }
   }
// ************************************************************************
 public void commCrtUser(String s1) throws Exception 
 {
  commCrtUser(s1,0);
  return;
 }
// ************************************************************************
 public void commCrtUser(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
            ;
       if (wp.colStr(ii,befStr+"crt_user").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_usr_cname"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commSrcCode(String s1) throws Exception 
 {
  commSrcCode(s1,0);
  return;
 }
// ************************************************************************
 public void commSrcCode(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " source_name as column_source_name "
            + " from ptr_src_code "
            + " where 1 = 1 "
            + " and   source_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_source_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataType2(String s1) throws Exception 
 {
  commDataType2(s1,0);
  return;
 }
// ************************************************************************
 public void commDataType2(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " ica_desc as column_ica_desc "
            + " from mkt_rcv_bin "
            + " where 1 = 1 "
            + " and   bank_no = '"+wp.colStr(ii,befStr+"data_code2")+"'"
            + " and   bank_no != '' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_ica_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commMechtGp(String s1,String s2) throws Exception 
 {	
  if("1".equals(s2)) {
	  commMechtGp(s1,0);
  }else {
	  commMechtGp2(s1,0);
  }
  return;
 }
// ************************************************************************
 public void commMechtGp(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 and  platform_flag != '2' "
            + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcht_group_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
//************************************************************************
public void commMechtGp2(String s1,int befType) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (befType==1) befStr="bef_";
 for (int ii = 0; ii < wp.selectCnt; ii++)
    {
     columnData="";
     sql1 = "select "
          + " mcht_group_desc as column_mcht_group_desc "
          + " from mkt_mcht_gp "
          + " where 1 = 1 and  platform_flag = '2' "
          + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
          ;
     if (wp.colStr(ii,befStr+"data_code").length()==0)
        {
         wp.colSet(ii, s1, columnData);
         continue;
        }
     sqlSelect(sql1);

     if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_mcht_group_desc"); 
     wp.colSet(ii, s1, columnData);
    }
 return;
}
// ************************************************************************
 public void commDataCodeCocq(String s1) throws Exception 
 {
  commDataCodeCocq(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCodeCocq(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chi_name as column_chi_name "
            + " from mkt_country "
            + " where 1 = 1 "
            + " and   country_code_2 = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode2Cocq(String s1) throws Exception 
 {
  commDataCode2Cocq(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode2Cocq(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " curr_chi_name as column_curr_chi_name "
            + " from ptr_currcode "
            + " where 1 = 1 "
            + " and   curr_code = '"+wp.colStr(ii,befStr+"data_code2")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code2").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_curr_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode3Cocq(String s1) throws Exception 
 {
  commDataCode3Cocq(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode3Cocq(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcc_remark as column_mcc_remark "
            + " from cca_mcc_risk "
            + " where 1 = 1 "
            + " and   mcc_code = '"+wp.colStr(ii,befStr+"data_code3")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code3").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcc_remark"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCodeCocd(String s1) throws Exception 
 {
  commDataCodeCocd(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCodeCocd(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chi_name as column_chi_name "
            + " from mkt_country "
            + " where 1 = 1 "
            + " and   country_code_2 = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode2Cocd(String s1) throws Exception 
 {
  commDataCode2Cocd(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode2Cocd(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " curr_chi_name as column_curr_chi_name "
            + " from ptr_currcode "
            + " where 1 = 1 "
            + " and   curr_code = '"+wp.colStr(ii,befStr+"data_code2")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code2").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_curr_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode3Cocd(String s1) throws Exception 
 {
  commDataCode3Cocd(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode3Cocd(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcc_remark as column_mcc_remark "
            + " from cca_mcc_risk "
            + " where 1 = 1 "
            + " and   mcc_code = '"+wp.colStr(ii,befStr+"data_code3")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code3").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcc_remark"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commEntryMode(String s1) throws Exception 
 {
  commEntryMode(s1,0);
  return;
 }
// ************************************************************************
 public void commEntryMode(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mode_desc as column_mode_desc "
            + " from cca_entry_mode "
            + " where 1 = 1 "
            + " and   entry_mode = '"+wp.colStr(ii,befStr+"data_code2")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code2").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mode_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commEntryModed(String s1) throws Exception 
 {
  commEntryModed(s1,0);
  return;
 }
// ************************************************************************
 public void commEntryModed(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mode_desc as column_mode_desc "
            + " from cca_entry_mode "
            + " where 1 = 1 "
            + " and   entry_mode = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mode_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commAcctType(String s1) throws Exception 
 {
  commAcctType(s1,0);
  return;
 }
// ************************************************************************
 public void commAcctType(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chin_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode04(String s1) throws Exception 
 {
  commDataCode04(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode04(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " group_name as column_group_name "
            + " from ptr_group_code "
            + " where 1 = 1 "
            + " and   group_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_group_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode02(String s1) throws Exception 
 {
  commDataCode02(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode02(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " name as column_name "
            + " from ptr_card_type "
            + " where 1 = 1 "
            + " and   card_type = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode08(String s1) throws Exception 
 {
  commDataCode08(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode08(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcc_remark as column_mcc_remark "
            + " from cca_mcc_risk "
            + " where 1 = 1 "
            + " and   mcc_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcc_remark"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commFundBase(String s1) throws Exception 
 {
  String[] cde = {"A","B","C"};
  String[] txt = {"A.RC balance","B.消費本金類","C.已繳之循環利息"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commEffectType(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"永久有效","指定月數","指定期限"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect2(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect4(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect3(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect5(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commNewHldrFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"全新卡友","於核卡日前N日"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commActivate(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"正、附卡都須開卡","正卡開卡即可","正、附卡有一開卡即可"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commCobrand(String s1) throws Exception 
 {
  String[] cde = {"Y","E","S"};
  String[] txt = {"Y.本行刷卡金","E.聯名主紅利","S.聯名主計算"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectA(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect1(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectB(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect7(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"不判斷","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect9(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"不判斷","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectD(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectK(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectM(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commFeedType(String s1) throws Exception 
 {
  String[] cde = {"2","1"};
  String[] txt = {"累積金額","筆消費金額"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commPurchType(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"回饋金額","回饋比例"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commThresholdSel(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"條件式","級距式"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commPurchaseType(String s1) throws Exception 
 {
  String[] cde = {"1","2","5"};
  String[] txt = {"1.累積金額","累積筆數","次消費金額"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commExeType(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"1.起始年月","2.一段期間","3.發卡條件"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commCardFeedFlag(String s1) throws Exception 
 {
  String[] cde = {"0","1","2","3"};
  String[] txt = {"不設定日期限制","發卡後N1月內(含發卡當月)","發卡N2個月後","發卡N3個月後"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commNewHldrSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"不判斷","指定【執行對象之團代】之首張卡為全新正卡","排除【執行對象之團代】之其他正卡"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commFeedbackType(String s1) throws Exception 
 {
  String[] cde = {"2","1"};
  String[] txt = {"帳單週期","每月"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commFeedbackCycleFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"入帳日","關帳日"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commCancelMethod(String s1) throws Exception 
 {
  String[] cde = {"0","1","2","3"};
  String[] txt = {"每月","每季","每半年","每一年"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commCancelScope(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"當期簽帳款(六大本金)","當期全部信用卡款(所有本金+費用+利息)","全部簽帳款(六大本金)","全部信用卡款(所有本金+費用+利息)"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect8(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelect6(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectC(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectF(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectG(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commSelectE(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commCancelEvent(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"不限定","有有效卡","有聯名卡有有效卡","不抵用"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
// ************************************************************************
 public void commTranBase(String s1) throws Exception 
 {
  String[] cde = {"A","B","C"};
  String[] txt = {"RC balance","消費本金類","已繳之循環利息"};
  String columnData="";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       for (int inti=0;inti<cde.length;inti++)
         {
          String s2 = s1.substring(5,s1.length());
          if (wp.colStr(ii,s2).equals(cde[inti]))
             {
               wp.colSet(ii, s1, txt[inti]);
               break;
             }
         }
      }
   return;
 }
//************************************************************************
public void commForeignCode(String s1) throws Exception 
{
String[] cde = {"1","2","3"};
String[] txt = {"國內刷卡","國外刷卡","國內外刷卡"};
String columnData="";
 for (int ii = 0; ii < wp.selectCnt; ii++)
    {
     for (int inti=0;inti<cde.length;inti++)
       {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
           {
             wp.colSet(ii, s1, txt[inti]);
             break;
           }
       }
    }
 return;
}

// ************************************************************************
public void commMchtCname(String cde1) throws Exception {
  String[] cde = {"0", "1", "2"};
  String[] txt = {"全部", "指定", "排除"};
  String columnData = "";
  for (int ii = 0; ii < wp.selectCnt; ii++) {
    for (int inti = 0; inti < cde.length; inti++) {
      String txt1 = cde1.substring(5, cde1.length());
      if (wp.colStr(ii, txt1).equals(cde[inti])) {
        wp.colSet(ii, cde1, txt[inti]);
        break;
      }
    }
  }
  return;
}

// ************************************************************************
public void commMchtEname(String cde1) throws Exception {
  String[] cde = {"0", "1", "2"};
  String[] txt = {"全部", "指定", "排除"};
  String columnData = "";
  for (int ii = 0; ii < wp.selectCnt; ii++) {
    for (int inti = 0; inti < cde.length; inti++) {
      String txt1 = cde1.substring(5, cde1.length());
      if (wp.colStr(ii, txt1).equals(cde[inti])) {
        wp.colSet(ii, cde1, txt[inti]);
        break;
      }
    }
  }
  return;
}
// ************************************************************************
 public void checkButtonOff() throws Exception
  {
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
  buttonOff("btnAdd_disable");
  return;
 }
// ************************************************************************
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************
 String  listPtrFundData(String s1,String s2,String s3,String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where 1 = 1 "
              + " and   table_name = '"+s2+"'"
              + " and   data_key   = '"+s3+"'"
              + " and   data_type  = '"+s4+"'"
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }
// ************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,ptr_fundp_t b "
          + " where a.usr_id = b.crt_user "
          + " group by b.crt_user "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwFundCode1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.fund_code as db_code, "
          + " max(b.fund_code||' '||b.fund_name) as db_desc "
          + " from  ptr_fundp_t b "
          + " where   b.apr_flag = 'N' "
          + " group by b.fund_code "
          ;

   return lsSql;
 }
 // ************************************************************************
 String listPtrFundCData(String table, String tableName, String dataKey, String dataType) throws Exception {
   String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
       + " where 1 = 1 " + " and   table_name = ? " + " and   data_key   = ? "
       + " and   data_type  = ? ";
   sqlSelect(sql1, new Object[] { tableName, dataKey, dataType });

   if (sqlRowNum > 0)
     return (sqlStr("column_data_cnt"));

   return ("0");
 }

// ************************************************************************

}  // End of class
