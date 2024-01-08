/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/24  V1.01.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-26  V1.00.01  Zuwei Su        AJAX調用方式修改                                                                         *
* 112-05-05  V1.00.03  Ryan    新增國內外消費欄位維護，特店中文名稱、特店英文名稱參數維護，[消費回饋比例]區塊新增多個欄位維護   *
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *
***************************************************************************/
package mktm02;

import mktm02.Mktm6220Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6220 extends BaseEdit
{
 private final String PROGNAME = "刷卡金參數檔維護處理程式111-11-28  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6220Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "ptr_fundp";
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
  else if (eqIgno(wp.buttonCode, "A"))
     {// 新增功能 -/
      strAction = "A";
      wp.itemSet("aud_type","A");
      insertFunc();
     }
  else if (eqIgno(wp.buttonCode, "U"))
     {/* 更新功能 */
      strAction = "U3";
      updateFuncU3R();
     }
  else if (eqIgno(wp.buttonCode, "I"))
     {/* 單獨新鄒功能 */
      strAction = "I";
/*
      kk1 = item_kk("data_k1");
      kk2 = item_kk("data_k2");
      kk3 = item_kk("data_k3");
*/
      clearFunc();
     }
  else if (eqIgno(wp.buttonCode, "D"))
     {/* 刪除功能 */
      deleteFuncD3R();
     }
  else if (eqIgno(wp.buttonCode, "R2"))
     {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
     }
  else if (eqIgno(wp.buttonCode, "U2"))
     {/* 明細更新 */
      strAction = "U2";
      updateFuncU2();
     }
  else if (eqIgno(wp.buttonCode, "R3"))
     {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
     }
  else if (eqIgno(wp.buttonCode, "U3"))
     {/* 明細更新 */
      strAction = "U3";
      updateFuncU3();
     }
  else if (eqIgno(wp.buttonCode, "R4"))
     {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
     }
  else if (eqIgno(wp.buttonCode, "U4"))
     {/* 明細更新 */
      strAction = "U4";
      updateFuncU4();
     } 
  else if (eqIgno(wp.buttonCode, "R5"))
     {// 明細查詢 -/
       strAction = "R5";
       dataReadR5();
     }
  else if (eqIgno(wp.buttonCode, "U5"))
     {/* 明細更新 */
       strAction = "U5";
       updateFuncU5();
     }
  else if (eqIgno(wp.buttonCode, "M"))
     {/* 瀏覽功能 :skip-page*/
      queryRead();
     }
  else if (eqIgno(wp.buttonCode, "S"))
     {/* 動態查詢 */
      querySelect();
     }
  else if (eqIgno(wp.buttonCode, "UPLOAD2"))
     {/* 匯入檔案 */
      procUploadFile(2);
      checkButtonOff();
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
  else if (eqIgno(wp.buttonCode, "AJAX"))
  {/* nothing to do */
   strAction = "";
   switch (wp.itemStr("methodName")) {
    case "wf_ajax_func_1":
        wfAjaxFunc1(wp);
        break;
    case "wf_ajax_func_3":
        wfAjaxFunc3(wp);
        break;

    default:
        break;
}
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
              + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code", "like%")
              + sqlChkEx(wp.itemStr2("ex_apr_flag"), "2", "")
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
  if (wp.itemStr2("ex_apr_flag").equals("N"))
     controlTabName = orgTabName +"_t";

  wp.pageControl();

  wp.selectSQL = " "
               + "hex(a.rowid) as rowid, "
               + "nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.fund_code,"
               + "a.fund_name,"
               + "a.tran_base,"
               + "a.fund_crt_date_s,"
               + "a.fund_crt_date_e,"
               + "a.cancel_period,"
               + "a.cancel_scope,"
               + "a.purch_feed_flag,"
               + "a.fund_feed_flag,"
               + "a.feedback_type,"
               + "a.apr_user,"
               + "a.apr_date";

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
      return;
     }

  commAprUser("comm_apr_user");

  commTranBase("comm_tran_base");
  commCancelPeriod("comm_cancel_period");
  commCancelScope("comm_cancel_scope");
  commFeedbackType("comm_feedback_type");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 @Override
  public void querySelect() throws Exception
 {
  fstAprFlag= wp.itemStr2("ex_apr_flag");
  if (wp.itemStr2("ex_apr_flag").equals("N"))
     controlTabName = orgTabName +"_t";

  kk1 = itemKk("data_k1");
  qFrom=1;
  dataRead();
 }
// ************************************************************************
 @Override
 public void dataRead() throws Exception
 {
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
               + "a.fund_code as fund_code,"
               + "a.apr_flag,"
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
               + "'' as bin_type_sel_cnt,"
               + "a.acct_type_sel,"
               + "'' as acct_type_sel_cnt,"
               + "a.group_code_sel,"
               + "'' as group_code_sel_cnt,"
               + "a.card_type_sel,"
               + "'' as card_type_sel_cnt,"
               + "a.new_hldr_cond,"
               + "a.new_hldr_flag,"
               + "a.new_hldr_days,"
               + "a.new_group_cond,"
               + "'' as new_group_cond_cnt,"
               + "a.new_hldr_card,"
               + "a.new_hldr_sup,"
               + "a.new_card_days,"
               + "a.apply_age_cond,"
               + "a.apply_age_s,"
               + "a.apply_age_e,"
               + "a.activate_cond,"
               + "a.activate_flag,"
               + "a.valid_period,"
               + "a.cobrand_code,"
               + "a.source_code_sel,"
               + "'' as source_code_sel_cnt,"
               + "a.merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.platform_kind_sel,"
               + "'' as platform_kind_sel_cnt,"
               + "a.currency_sel,"
               + "'' as currency_sel_cnt,"
               + "a.ex_currency_sel,"
               + "'' as ex_currency_sel_cnt,"
               + "a.pos_entry_sel,"
               + "'' as pos_entry_sel_cnt,"
               + "a.pos_merchant_sel,"
               + "'' as pos_merchant_sel_cnt,"
               + "a.pos_mcht_group_sel,"
               + "'' as pos_mcht_group_sel_cnt,"
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
               + "'' as d_mcc_code_sel_cnt,"
               + "a.d_merchant_sel,"
               + "'' as d_merchant_sel_cnt,"
               + "a.d_mcht_group_sel,"
               + "'' as d_mcht_group_sel_cnt,"
               + "a.d_ucaf_sel,"
               + "'' as d_ucaf_sel_cnt,"
               + "a.d_eci_sel,"
               + "'' as d_eci_sel_cnt,"
               + "a.d_pos_entry_sel,"
               + "'' as d_pos_entry_sel_cnt,"
               + "a.cancel_event,"
               + "a.min_mcode,"
               + "a.cancel_high_amt,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user,"
               + "a.foreign_code,"
               + "a.mcht_cname_sel,"
               + "'' as mcht_cname_sel_cnt,"
               + "a.mcht_ename_sel,"
               + "'' as mcht_ename_sel_cnt,"
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
   if (qFrom==0)
      {
       wp.colSet("aud_type","Y");
      }
   else
      {
       wp.colSet("aud_type",wp.itemStr2("ex_apr_flag"));
       wp.colSet("fst_apr_flag",wp.itemStr2("ex_apr_flag"));
      }
  datareadWkdata();
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("fund_code");
  listWkdata();
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
//   wp.col_set("card_feed_run_day" , "1");

 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("bin_type_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"2"));
  wp.colSet("acct_type_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"4"));
  wp.colSet("group_code_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"3"));
  wp.colSet("card_type_sel_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"5"));
  wp.colSet("new_group_cond_cnt" , listPtrFundData("ptr_fund_data_t","PTR_FUNDP",wp.colStr("fund_code"),"0"));
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
  wp.colSet("mcht_cname_sel_cnt" , listMktBnCdata("ptr_fund_cdata_t","PTR_FUNDP",wp.colStr("fund_code"),"A"));
  wp.colSet("mcht_ename_sel_cnt" , listMktBnCdata("ptr_fund_cdata_t","PTR_FUNDP",wp.colStr("fund_code"),"B"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  wp.colSet("bin_type_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"2"));
  wp.colSet("acct_type_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"4"));
  wp.colSet("group_code_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"3"));
  wp.colSet("card_type_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"5"));
  wp.colSet("new_group_cond_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"0"));
  wp.colSet("source_code_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"A"));
  wp.colSet("merchant_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"1"));
  wp.colSet("mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"H"));
  wp.colSet("platform_kind_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"P"));
  wp.colSet("currency_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"7"));
  wp.colSet("ex_currency_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"9"));
  wp.colSet("pos_entry_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"B"));
  wp.colSet("pos_merchant_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"C"));
  wp.colSet("pos_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"M"));
  wp.colSet("d_mcc_code_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"8"));
  wp.colSet("d_merchant_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"6"));
  wp.colSet("d_mcht_group_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"K"));
  wp.colSet("d_ucaf_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"F"));
  wp.colSet("d_eci_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"G"));
  wp.colSet("d_pos_entry_sel_cnt" , listPtrFundData("ptr_fund_data","PTR_FUNDP",wp.colStr("fund_code"),"E"));
  wp.colSet("mcht_cname_sel_cnt" , listMktBnCdata("ptr_fund_cdata","PTR_FUNDP",wp.colStr("fund_code"),"A"));
  wp.colSet("mcht_ename_sel_cnt" , listMktBnCdata("ptr_fund_cdata","PTR_FUNDP",wp.colStr("fund_code"),"B"));
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.fund_code as fund_code,"
               + "a.apr_flag as apr_flag,"
               + "a.fund_name as fund_name,"
               + "a.tran_base as tran_base,"
               + "a.fund_crt_date_s as fund_crt_date_s,"
               + "a.fund_crt_date_e as fund_crt_date_e,"
               + "a.effect_type as effect_type,"
               + "a.effect_months as effect_months,"
               + "a.effect_years as effect_years,"
               + "a.effect_fix_month as effect_fix_month,"
               + "a.stop_flag as stop_flag,"
               + "a.stop_date as stop_date,"
               + "a.stop_desc as stop_desc,"
               + "a.bin_type_sel as bin_type_sel,"
               + "'' as bin_type_sel_cnt,"
               + "a.acct_type_sel as acct_type_sel,"
               + "'' as acct_type_sel_cnt,"
               + "a.group_code_sel as group_code_sel,"
               + "'' as group_code_sel_cnt,"
               + "a.card_type_sel as card_type_sel,"
               + "'' as card_type_sel_cnt,"
               + "a.new_hldr_cond as new_hldr_cond,"
               + "a.new_hldr_flag as new_hldr_flag,"
               + "a.new_hldr_days as new_hldr_days,"
               + "a.new_group_cond as new_group_cond,"
               + "'' as new_group_cond_cnt,"
               + "a.new_hldr_card as new_hldr_card,"
               + "a.new_hldr_sup as new_hldr_sup,"
               + "a.new_card_days as new_card_days,"
               + "a.apply_age_cond as apply_age_cond,"
               + "a.apply_age_s as apply_age_s,"
               + "a.apply_age_e as apply_age_e,"
               + "a.activate_cond as activate_cond,"
               + "a.activate_flag as activate_flag,"
               + "a.valid_period as valid_period,"
               + "a.cobrand_code as cobrand_code,"
               + "a.source_code_sel as source_code_sel,"
               + "'' as source_code_sel_cnt,"
               + "a.merchant_sel as merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel as mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.platform_kind_sel as platform_kind_sel,"
               + "'' as platform_kind_sel_cnt,"
               + "a.currency_sel as currency_sel,"
               + "'' as currency_sel_cnt,"
               + "a.ex_currency_sel as ex_currency_sel,"
               + "'' as ex_currency_sel_cnt,"
               + "a.pos_entry_sel as pos_entry_sel,"
               + "'' as pos_entry_sel_cnt,"
               + "a.pos_merchant_sel as pos_merchant_sel,"
               + "'' as pos_merchant_sel_cnt,"
               + "a.pos_mcht_group_sel as pos_mcht_group_sel,"
               + "'' as pos_mcht_group_sel_cnt,"
               + "a.bl_cond as bl_cond,"
               + "a.ca_cond as ca_cond,"
               + "a.id_cond as id_cond,"
               + "a.ao_cond as ao_cond,"
               + "a.it_cond as it_cond,"
               + "a.ot_cond as ot_cond,"
               + "a.purch_feed_flag as purch_feed_flag,"
               + "a.purch_date_s as purch_date_s,"
               + "a.purch_date_e as purch_date_e,"
               + "a.purch_reclow_cond as purch_reclow_cond,"
               + "a.purch_reclow_amt as purch_reclow_amt,"
               + "a.purch_rec_amt_cond as purch_rec_amt_cond,"
               + "a.purch_rec_amt as purch_rec_amt,"
               + "a.purch_tol_amt_cond as purch_tol_amt_cond,"
               + "a.purch_tol_amt as purch_tol_amt,"
               + "a.purch_tol_time_cond as purch_tol_time_cond,"
               + "a.purch_tol_time as purch_tol_time,"
               + "a.purch_feed_type as purch_feed_type,"
               + "a.purch_type as purch_type,"
               + "a.purch_feed_amt as purch_feed_amt,"
               + "a.purch_feed_rate as purch_feed_rate,"
               + "a.fund_feed_flag as fund_feed_flag,"
               + "a.threshold_sel as threshold_sel,"
               + "a.purchase_type_sel as purchase_type_sel,"
               + "a.fund_s_amt_1 as fund_s_amt_1,"
               + "a.fund_e_amt_1 as fund_e_amt_1,"
               + "a.fund_rate_1 as fund_rate_1,"
               + "a.fund_amt_1 as fund_amt_1,"
               + "a.fund_s_amt_2 as fund_s_amt_2,"
               + "a.fund_e_amt_2 as fund_e_amt_2,"
               + "a.fund_rate_2 as fund_rate_2,"
               + "a.fund_amt_2 as fund_amt_2,"
               + "a.fund_s_amt_3 as fund_s_amt_3,"
               + "a.fund_e_amt_3 as fund_e_amt_3,"
               + "a.fund_rate_3 as fund_rate_3,"
               + "a.fund_amt_3 as fund_amt_3,"
               + "a.fund_s_amt_4 as fund_s_amt_4,"
               + "a.fund_e_amt_4 as fund_e_amt_4,"
               + "a.fund_rate_4 as fund_rate_4,"
               + "a.fund_amt_4 as fund_amt_4,"
               + "a.fund_s_amt_5 as fund_s_amt_5,"
               + "a.fund_e_amt_5 as fund_e_amt_5,"
               + "a.fund_rate_5 as fund_rate_5,"
               + "a.fund_amt_5 as fund_amt_5,"
               + "a.rc_sub_amt as rc_sub_amt,"
               + "a.rc_sub_rate as rc_sub_rate,"
               + "a.program_exe_type as program_exe_type,"
               + "a.unlimit_start_month as unlimit_start_month,"
               + "a.cal_s_month as cal_s_month,"
               + "a.cal_e_month as cal_e_month,"
               + "a.card_feed_date_s as card_feed_date_s,"
               + "a.card_feed_date_e as card_feed_date_e,"
               + "a.card_feed_flag as card_feed_flag,"
               + "a.cal_months as cal_months,"
               + "a.card_feed_months2 as card_feed_months2,"
               + "a.card_feed_days as card_feed_days,"
               + "a.new_hldr_sel as new_hldr_sel,"
               + "a.feedback_type as feedback_type,"
               + "a.card_feed_run_day as card_feed_run_day,"
               + "a.feedback_months as feedback_months,"
               + "a.feedback_lmt as feedback_lmt,"
               + "a.purch_feed_times as purch_feed_times,"
               + "a.autopay_flag as autopay_flag,"
               + "a.mp_flag as mp_flag,"
               + "a.valid_card_flag as valid_card_flag,"
               + "a.valid_afi_flag as valid_afi_flag,"
               + "a.ebill_flag as ebill_flag,"
               + "a.autopay_digit_cond as autopay_digit_cond,"
               + "a.d_txn_cond as d_txn_cond,"
               + "a.d_txn_amt as d_txn_amt,"
               + "a.cancel_period as cancel_period,"
               + "a.cancel_s_month as cancel_s_month,"
               + "a.cancel_scope as cancel_scope,"
               + "a.d_mcc_code_sel as d_mcc_code_sel,"
               + "'' as d_mcc_code_sel_cnt,"
               + "a.d_merchant_sel as d_merchant_sel,"
               + "'' as d_merchant_sel_cnt,"
               + "a.d_mcht_group_sel as d_mcht_group_sel,"
               + "'' as d_mcht_group_sel_cnt,"
               + "a.d_ucaf_sel as d_ucaf_sel,"
               + "'' as d_ucaf_sel_cnt,"
               + "a.d_eci_sel as d_eci_sel,"
               + "'' as d_eci_sel_cnt,"
               + "a.d_pos_entry_sel as d_pos_entry_sel,"
               + "'' as d_pos_entry_sel_cnt,"
               + "a.cancel_event as cancel_event,"
               + "a.min_mcode as min_mcode,"
               + "a.cancel_high_amt as cancel_high_amt,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user,"
               + "a.foreign_code as foreign_code, "
               + "a.mcht_cname_sel as mcht_cname_sel,"
               + "'' as mcht_cname_sel_cnt,"
               + "a.mcht_ename_sel as mcht_ename_sel,"
               + "'' as mcht_ename_sel_cnt,"
               + "a.hapcare_trust_cond as hapcare_trust_cond,"
               + "a.hapcare_trust_rate as hapcare_trust_rate,"
               + "a.housing_endow_cond as housing_endow_cond,"
               + "a.housing_endow_rate as housing_endow_rate,"
               + "a.happycare_fblmt as happycare_fblmt,"
               + "a.mortgage_cond as mortgage_cond,"
               + "a.mortgag_rate as mortgag_rate,"
               + "a.mortgage_fblmt as mortgage_fblmt,"
               + "a.util_entrustded_cond as util_entrustded_cond,"
               + "a.util_entrustded_rate as util_entrustded_rate,"
               + "a.util_entrustded_fblmt as util_entrustded_fblmt,"
               + "a.twpay_cond as twpay_cond,"
               + "a.twpay_rate as twpay_rate,"
               + "a.tcblife_ec_cond as tcblife_ec_cond,"
               + "a.tcblife_ec_rate as tcblife_ec_rate,"
               + "a.eco_fblmt as eco_fblmt,"
               + "a.extratwpay_cond as extratwpay_cond,"
               + "a.onlyaddon_calcond as onlyaddon_calcond "
//               + "a.extratwpay_rate as extratwpay_rate,"
//               + "a.extratwpay_fblmt as extratwpay_fblmt"
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
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdataAft();
  datareadWkdata();
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("fund_code");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("fund_code");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y"))
         {
          qFrom=0;
          controlTabName = orgTabName;
         }
     }
  else
     {
      strAction = "A";
      wp.itemSet("aud_type","D");
      insertFunc();
     }
  dataRead();
  wp.colSet("fst_apr_flag",fstAprFlag);
 }
// ************************************************************************
 public void updateFuncU3R()  throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("fund_code");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1)
         {
          dataReadR3R();;
          datareadWkdata();
         }
     }
  else
     {
      km1 = wp.itemStr2("fund_code");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag",fstAprFlag);
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

   if ((wp.itemStr2("fund_code").length()==0)||
       (wp.itemStr2("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr2("aud_type").equals("Y"))||
       (wp.itemStr2("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "ptr_fund_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "ptr_fund_data_t";
      }

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
   if (wp.respHtml.equals("mktm6220_bint"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktm6220_acty"))
      wp.whereStr  += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktm6220_gpcd"))
      wp.whereStr  += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktm6220_dype"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktm6220_gnce"))
      wp.whereStr  += " and data_type  = '0' ";
   if (wp.respHtml.equals("mktm6220_srcd"))
      wp.whereStr  += " and data_type  = 'A' ";
   if (wp.respHtml.equals("mktm6220_aaa1"))
      wp.whereStr  += " and data_type  = 'H' ";
   if (wp.respHtml.equals("mktm6220_aaa3"))
	  wp.whereStr  += " and data_type  = 'P' ";
   if (wp.respHtml.equals("mktm6220_aaam"))
      wp.whereStr  += " and data_type  = 'M' ";
   if (wp.respHtml.equals("mktm6220_mccd"))
      wp.whereStr  += " and data_type  = '8' ";
   if (wp.respHtml.equals("mktm6220_aaa2"))
      wp.whereStr  += " and data_type  = 'K' ";
   if (wp.respHtml.equals("mktm6220_ucaf"))
      wp.whereStr  += " and data_type  = 'F' ";
   if (wp.respHtml.equals("mktm6220_deci"))
      wp.whereStr  += " and data_type  = 'G' ";
   if (wp.respHtml.equals("mktm6220_posd"))
      wp.whereStr  += " and data_type  = 'E' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm6220_acty"))
    commAcctType("comm_data_code");
   if (wp.respHtml.equals("mktm6220_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktm6220_dype"))
    commDataCode02("comm_data_code");
   if (wp.respHtml.equals("mktm6220_gnce"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktm6220_srcd"))
    commSrcCode("comm_data_code");
   if (wp.respHtml.equals("mktm6220_aaa1"))
    commMechtGp("comm_data_code");
   if (wp.respHtml.equals("mktm6220_aaa3"))
	commMechtGp2("comm_data_code");
   if (wp.respHtml.equals("mktm6220_aaam"))
    commMechtGp("comm_data_code");
   if (wp.respHtml.equals("mktm6220_mccd"))
    commDataCode08("comm_data_code");
   if (wp.respHtml.equals("mktm6220_aaa2"))
    commMechtGp("comm_data_code");
   if (wp.respHtml.equals("mktm6220_posd"))
    commEntryModed("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);
   int llOk = 0, llErr = 0;

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("data_code");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       for (int intm=ll+1;intm<key1Data.length; intm++)
         if ((key1Data[ll].equals(key1Data[intm]))) 
            {
             for (int intx=0;intx<optData.length;intx++) 
              { 
               if (optData[intx].length()!=0) 
               if (((ll+1)==Integer.valueOf(optData[intx]))||
                   ((intm+1)==Integer.valueOf(optData[intx])))
                  {
                   del2Flag=1;
                   break;
                  }
              }
             if (del2Flag==1) break;

             wp.colSet(ll, "ok_flag", "!");
             llErr++;
             continue;
            }
      }

   if (llErr > 0)
      {
       alertErr("資料值重複 : " + llErr);
       return;
      }

   //-delete no-approve-
   if (func.dbDeleteD2() < 0)
      {
       alertErr(func.getMsg());
       return;
      }

   //-insert-
   int deleteFlag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       deleteFlag=0;
       //KEY 不可同時為空字串
       if ((empty(key1Data[ll])))
           continue;

       //-option-ON-
       for (int intm=0;intm<optData.length;intm++)
         {
          if (optData[intm].length()!=0)
          if ((ll+1)==Integer.valueOf(optData[intm]))
             {
              deleteFlag=1;
              break;
             }
          }
       if (deleteFlag==1) continue;

       func.varsSet("data_code", key1Data[ll]); 

       if (func.dbInsertI2() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR2(1);
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

   if ((wp.itemStr2("fund_code").length()==0)||
       (wp.itemStr2("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr2("aud_type").equals("Y"))||
       (wp.itemStr2("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "ptr_fund_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "ptr_fund_data_t";
      }

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
   if (wp.respHtml.equals("mktm6220_mrch"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktm6220_mrck"))
      wp.whereStr  += " and data_type  = 'C' ";
   if (wp.respHtml.equals("mktm6220_mrcd"))
      wp.whereStr  += " and data_type  = '6' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm6220_mrch"))
    commDataType2("comm_data_code2");
   if (wp.respHtml.equals("mktm6220_mrck"))
    commDataType2("comm_data_code2");
   if (wp.respHtml.equals("mktm6220_mrcd"))
    commDataType2("comm_data_code2");
  }
// ************************************************************************
 public void updateFuncU3() throws Exception
 {
   mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);
   int llOk = 0, llErr = 0;

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("data_code");
   String[] key2Data = wp.itemBuff("data_code2");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       for (int intm=ll+1;intm<key1Data.length; intm++)
         if ((key1Data[ll].equals(key1Data[intm])) &&
             (key2Data[ll].equals(key2Data[intm]))) 
            {
             for (int intx=0;intx<optData.length;intx++) 
              { 
               if (optData[intx].length()!=0) 
               if (((ll+1)==Integer.valueOf(optData[intx]))||
                   ((intm+1)==Integer.valueOf(optData[intx])))
                  {
                   del2Flag=1;
                   break;
                  }
              }
             if (del2Flag==1) break;

             wp.colSet(ll, "ok_flag", "!");
             llErr++;
             continue;
            }
      }

   if (llErr > 0)
      {
       alertErr("資料值重複 : " + llErr);
       return;
      }

   //-delete no-approve-
   if (func.dbDeleteD3() < 0)
      {
       alertErr(func.getMsg());
       return;
      }

   //-insert-
   int deleteFlag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       deleteFlag=0;
       //KEY 不可同時為空字串
           if ((empty(key1Data[ll])) &&
              (empty(key2Data[ll])))
           continue;

       //-option-ON-
       for (int intm=0;intm<optData.length;intm++)
         {
          if (optData[intm].length()!=0)
          if ((ll+1)==Integer.valueOf(optData[intm]))
             {
              deleteFlag=1;
              break;
             }
          }
       if (deleteFlag==1) continue;

       func.varsSet("data_code", key1Data[ll]); 
       func.varsSet("data_code2", key2Data[ll]); 

       if (func.dbInsertI3() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR3(1);
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

   if ((wp.itemStr2("fund_code").length()==0)||
       (wp.itemStr2("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr2("aud_type").equals("Y"))||
       (wp.itemStr2("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "ptr_fund_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "ptr_fund_data_t";
      }

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
   if (wp.respHtml.equals("mktm6220_cocq"))
      wp.whereStr  += " and data_type  = '7' ";
   if (wp.respHtml.equals("mktm6220_cocd"))
      wp.whereStr  += " and data_type  = '9' ";
   if (wp.respHtml.equals("mktm6220_pose"))
      wp.whereStr  += " and data_type  = 'B' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm6220_cocq"))
    commDataCodeCocq("comm_data_code");
   if (wp.respHtml.equals("mktm6220_cocq"))
    commDataCode2Cocq("comm_data_code2");
   if (wp.respHtml.equals("mktm6220_cocq"))
    commDataCode3Cocq("comm_data_code3");
   if (wp.respHtml.equals("mktm6220_cocd"))
    commDataCodeCocd("comm_data_code");
   if (wp.respHtml.equals("mktm6220_cocd"))
    commDataCode2Cocd("comm_data_code2");
   if (wp.respHtml.equals("mktm6220_cocd"))
    commDataCode3Cocd("comm_data_code3");
   if (wp.respHtml.equals("mktm6220_pose"))
    commEntryMode("comm_data_code2");
   if (wp.respHtml.equals("mktm6220_pose"))
    commEntryMode("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU4() throws Exception
 {
   mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);
   int llOk = 0, llErr = 0;

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("data_code");
   String[] key2Data = wp.itemBuff("data_code2");
   String[] key3Data = wp.itemBuff("data_code3");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       for (int intm=ll+1;intm<key1Data.length; intm++)
         if ((key1Data[ll].equals(key1Data[intm])) &&
             (key2Data[ll].equals(key2Data[intm])) &&
             (key3Data[ll].equals(key3Data[intm]))) 
            {
             for (int intx=0;intx<optData.length;intx++) 
              { 
               if (optData[intx].length()!=0) 
               if (((ll+1)==Integer.valueOf(optData[intx]))||
                   ((intm+1)==Integer.valueOf(optData[intx])))
                  {
                   del2Flag=1;
                   break;
                  }
              }
             if (del2Flag==1) break;

             wp.colSet(ll, "ok_flag", "!");
             llErr++;
             continue;
            }
      }

   if (llErr > 0)
      {
       alertErr("資料值重複 : " + llErr);
       return;
      }

   //-delete no-approve-
   if (func.dbDeleteD4() < 0)
      {
       alertErr(func.getMsg());
       return;
      }

   //-insert-
   int deleteFlag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       deleteFlag=0;
       //KEY 不可同時為空字串
           if ((empty(key1Data[ll])) &&
                  (empty(key2Data[ll])) &&
              (empty(key3Data[ll])))
           continue;

       //-option-ON-
       for (int intm=0;intm<optData.length;intm++)
         {
          if (optData[intm].length()!=0)
          if ((ll+1)==Integer.valueOf(optData[intm]))
             {
              deleteFlag=1;
              break;
             }
          }
       if (deleteFlag==1) continue;

       func.varsSet("data_code", key1Data[ll]); 
       func.varsSet("data_code2", key2Data[ll]); 
       func.varsSet("data_code3", key3Data[ll]); 

       if (func.dbInsertI4() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR4(1);
 }
//************************************************************************
public void dataReadR5() throws Exception
{
dataReadR5(0);
}
//************************************************************************
public void dataReadR5(int fromType) throws Exception
{
 String bnTable="";

 if ((wp.itemStr("fund_code").length()==0) || (wp.itemStr("aud_type").length() == 0))
    {
     alertErr("鍵值為空白或主檔未新增 ");
     return;
    }
 wp.selectCnt=1;
 this.selectNoLimit();
 if ((wp.itemStr("aud_type").equals("Y"))||
     (wp.itemStr("aud_type").equals("D")))
    {
     buttonOff("btnUpdate_disable");
     buttonOff("newDetail_disable");
     bnTable = "PTR_FUND_CDATA";
    }
 else
    {
     wp.colSet("btnUpdate_disable","");
     wp.colSet("newDetail_disable","");
     bnTable = "PTR_FUND_CDATA_T";
    }

 wp.selectSQL = "hex(rowid) as r2_rowid, "
              + "ROW_NUMBER()OVER() as ser_num, "
              + "0 as r2_mod_seqno, "
              + "data_key, "
              + "data_code, "
              + "mod_user as r2_mod_user "
              ;
 wp.daoTable = bnTable ;
 wp.whereStr = "where 1=1"
             + " and table_name  =  'PTR_FUNDP' "
             ;
 if (wp.respHtml.equals("mktm6220_namc"))
    wp.whereStr  += " and data_type  = 'A' ";
 if (wp.respHtml.equals("mktm6220_name"))
    wp.whereStr  += " and data_type  = 'B' ";
 String whereCnt = wp.whereStr;
 wp.whereStr  += " and  data_key = :data_key ";
 setString("data_key", wp.itemStr("fund_code"));
 whereCnt += " and  data_key = '"+ wp.itemStr("fund_code") +  "'";
 wp.whereStr  += " order by 4,5,6 ";
 int cnt1=selectBndataCount(wp.daoTable,whereCnt);
 if (cnt1>300)
    {
     alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
     buttonOff("btnUpdate_disable");
     buttonOff("newDetail_disable");
     return;
    }

 pageQuery();
 wp.setListCount(1);
 wp.notFound = "";

 wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
}
//************************************************************************
public void updateFuncU5() throws Exception
{
 mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);
 int llOk = 0, llErr = 0;

 String[] optData  = wp.itemBuff("opt");
 String[] key1Data = wp.itemBuff("data_code");

 wp.listCount[0] = key1Data.length;
 wp.colSet("IND_NUM", "" + key1Data.length);
 //-check duplication-

 int del2Flag=0;
 for (int ll = 0; ll < key1Data.length; ll++)
    {
     del2Flag=0;
     wp.colSet(ll, "ok_flag", "");

     for (int intm=ll+1;intm<key1Data.length; intm++)
       if ((key1Data[ll].equals(key1Data[intm]))) 
          {
           for (int intx=0;intx<optData.length;intx++) 
            { 
             if (optData[intx].length()!=0) 
             if (((ll+1)==Integer.valueOf(optData[intx]))||
                 ((intm+1)==Integer.valueOf(optData[intx])))
                {
                 del2Flag=1;
                 break;
                }
            }
           if (del2Flag==1) break;

           wp.colSet(ll, "ok_flag", "!");
           llErr++;
           continue;
          }
    }

 if (llErr > 0)
    {
     alertErr("資料值重複 : " + llErr);
     return;
    }

 //-delete no-approve-
 if (func.dbDeleteD5() < 0)
    {
     alertErr(func.getMsg());
     return;
    }

 //-insert-
 int deleteFlag=0;
 for (int ll = 0; ll < key1Data.length; ll++)
    {
     deleteFlag=0;
     //KEY 不可同時為空字串
     if ((empty(key1Data[ll])))
         continue;

     //-option-ON-
     for (int intm=0;intm<optData.length;intm++)
       {
        if (optData[intm].length()!=0)
        if ((ll+1)==Integer.valueOf(optData[intm]))
           {
            deleteFlag=1;
            break;
           }
        }
     if (deleteFlag==1) continue;

     func.varsSet("data_code", key1Data[ll]); 

     if (func.dbInsertI5() == 1) llOk++;
     else llErr++;

     //有失敗rollback，無失敗commit
     sqlCommit(llOk > 0 ? 1 : 0);
    }
 alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

 //SAVE後 SELECT
 dataReadR5(1);
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
 public void saveFunc() throws Exception
 {
  mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);

  if (wp.respHtml.indexOf("_detl") > 0)
     if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
  else
    {
     if (wp.respHtml.indexOf("_nadd") > 0)
        alertMsg("明細資料, 請於主檔新增後維護!");
    }
  this.sqlCommit(rc);
 }
// ************************************************************************
 @Override
 public void initButton()
 {
  if ((wp.respHtml.indexOf("_detl") > 0)||
      (wp.respHtml.indexOf("_nadd") > 0))
     {
      wp.colSet("btnUpdate_disable","");
      wp.colSet("btnDelete_disable","");
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
       if ((wp.respHtml.equals("mktm6220_srcd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("sel_data_codea").length()>0)
             {
             wp.optionKey = wp.colStr("sel_data_codea");
             wp.initOption ="";
             }
          this.dddwList("dddw_ptr_group_code"
                 ,"ptr_group_code"
                 ,"trim(group_abbr_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql = "";
          lsSql =  procDynamicDddwDataCodea(wp.itemStr2("sel_data_codea"));
          wp.colSet("sel_data_codea" , wp.itemStr2("sel_data_codea"));
          dddwList("dddw_data_codea", lsSql);

         }
       if ((wp.respHtml.equals("mktm6220_mrch")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql = "";
          lsSql =  procDynamicDddwDataType1();
//          wp.col_set("sel_data_codea" , wp.item_ss("sel_data_codea"));
          dddwList("dddw_data_type1", lsSql);

         }
       if ((wp.respHtml.equals("mktm6220_mrck")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql = "";
          lsSql =  procDynamicDddwDataType1();
//          wp.col_set("sel_data_codea" , wp.item_ss("sel_data_codea"));
          dddwList("dddw_data_type1", lsSql);

         }
       if ((wp.respHtml.equals("mktm6220_mrcd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql = "";
          lsSql =  procDynamicDddwDataType1();
//          wp.col_set("sel_data_codea" , wp.item_ss("sel_data_codea"));
          dddwList("dddw_data_type1", lsSql);

         }
       if ((wp.respHtml.equals("mktm6220_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and platform_flag != '2' ");
         }
       
       if ((wp.respHtml.equals("mktm6220_aaa3")))
       {
        wp.initOption ="";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp"
               ,"mkt_mcht_gp"
               ,"trim(mcht_group_id)"
               ,"trim(mcht_group_desc)"
               ," where 1 = 1 and platform_flag = '2' ");
       }
       if ((wp.respHtml.equals("mktm6220_aaam")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and platform_flag != '2' ");
         }
       if ((wp.respHtml.equals("mktm6220_aaa2")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp1"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and platform_flag != '2' ");
         }
       if ((wp.respHtml.equals("mktm6220_cocq")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql =  procDynamicDddwDataCode071();

          dddwList("dddw_data_code_071", lsSql);
          wp.colSet("ex_data_key", "");
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
       if ((wp.respHtml.equals("mktm6220_cocd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql =  procDynamicDddwDataCode071();

          dddwList("dddw_data_code_071", lsSql);
          wp.colSet("ex_data_key", "");
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
       if ((wp.respHtml.equals("mktm6220_pose")))
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
       if ((wp.respHtml.equals("mktm6220_bint")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_bin_typeB"
                 ,"ptr_bintable"
                 ,"trim(bin_type)"
                 ,""
                 ," group by bin_type");
         }
       if ((wp.respHtml.equals("mktm6220_posd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_entry_mode"
                 ,"cca_entry_mode"
                 ,"trim(entry_mode)"
                 ,"trim(mode_desc)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm6220_acty")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm6220_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm6220_gnce")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm6220_dype")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type1"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm6220_mccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm6220_pose")))
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
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  return "";
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"crt_user")+"'"
            ;
       if (wp.colStr(ii,"crt_user").length()==0)
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
 public void commAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"apr_user")+"'"
            ;
       if (wp.colStr(ii,"apr_user").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " source_name as column_source_name "
            + " from ptr_src_code "
            + " where 1 = 1 "
            + " and   source_code = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " ica_desc as column_ica_desc "
            + " from mkt_rcv_bin "
            + " where 1 = 1 "
            + " and   bank_no = '"+wp.colStr(ii,"data_code2")+"'"
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
 public void commMechtGp(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 and platform_flag != '2'"
            + " and   mcht_group_id = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
public void commMechtGp2(String s1) throws Exception 
{
String columnData="";
String sql1 = "";
 for (int ii = 0; ii < wp.selectCnt; ii++)
    {
     columnData="";
     sql1 = "select "
          + " mcht_group_desc as column_mcht_group_desc "
          + " from mkt_mcht_gp "
          + " where 1 = 1 and platform_flag = '2' "
          + " and   mcht_group_id = '"+wp.colStr(ii,"data_code")+"'"
          ;
     if (wp.colStr(ii,"data_code").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";

       sql1 = "select "
            + " (chi_name||'(幣別:'||curr_code||')') as column_chi_name "
            + " from mkt_country "
            + " where 1 = 1 "
            + " and   country_code_2 = '"+wp.colStr(ii,"data_code")+"'"
            ;

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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " curr_chi_name as column_curr_chi_name "
            + " from ptr_currcode "
            + " where 1 = 1 "
            + " and   curr_code = '"+wp.colStr(ii,"data_code2")+"'"
            ;
       if (wp.colStr(ii,"data_code2").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcc_remark as column_mcc_remark "
            + " from cca_mcc_risk "
            + " where 1 = 1 "
            + " and   mcc_code = '"+wp.colStr(ii,"data_code3")+"'"
            ;
       if (wp.colStr(ii,"data_code3").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";

       sql1 = "select "
            + " (chi_name||'(幣別:'||curr_code||')') as column_chi_name "
            + " from mkt_country "
            + " where 1 = 1 "
            + " and   country_code_2 = '"+wp.colStr(ii,"data_code")+"'"
            ;

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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " curr_chi_name as column_curr_chi_name "
            + " from ptr_currcode "
            + " where 1 = 1 "
            + " and   curr_code = '"+wp.colStr(ii,"data_code2")+"'"
            ;
       if (wp.colStr(ii,"data_code2").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcc_remark as column_mcc_remark "
            + " from cca_mcc_risk "
            + " where 1 = 1 "
            + " and   mcc_code = '"+wp.colStr(ii,"data_code3")+"'"
            ;
       if (wp.colStr(ii,"data_code3").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mode_desc as column_mode_desc "
            + " from cca_entry_mode "
            + " where 1 = 1 "
            + " and   entry_mode = '"+wp.colStr(ii,"data_code2")+"'"
            ;
       if (wp.colStr(ii,"data_code2").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mode_desc as column_mode_desc "
            + " from cca_entry_mode "
            + " where 1 = 1 "
            + " and   entry_mode = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " group_name as column_group_name "
            + " from ptr_group_code "
            + " where 1 = 1 "
            + " and   group_code = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " name as column_name "
            + " from ptr_card_type "
            + " where 1 = 1 "
            + " and   card_type = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcc_remark as column_mcc_remark "
            + " from cca_mcc_risk "
            + " where 1 = 1 "
            + " and   mcc_code = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
 public void commAprFlag2(String s1) throws Exception 
 {
  String[] cde = {"N","U","Y"};
  String[] txt = {"待覆核","暫緩覆核","已覆核"};
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
// ************************************************************************
 public void commCancelPeriod(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
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
  String[] cde = {"1","2","3"};
  String[] txt = {"當期Posting簽帳款","當期Posting全部信用卡帳","全部簽帳款"};
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
  String[] cde = {"1","2"};
  String[] txt = {"每月","帳單週期"};
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
 public void wfAjaxFunc1(TarokoCommon wr) throws Exception
 {
  super.wp = wr;


  if (selectAjaxFunc10(
                    wp.itemStr2("ax_win_fund_code"))!=0) 
     {
      wp.addJSON("payment_type","");
      return;
     }

  wp.addJSON("payment_type",sqlStr("payment_type"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1) throws Exception
  {
   if (s1.length()<4)
      {
       alertErr("刷卡金代碼長度至少i4碼!");
       return 1;
      }

   wp.sqlCmd = " select "
             + " a.payment_type as payment_type "
             + " from  ptr_payment a "
             + " where a.payment_type = substr('"+s1+"',1,4) "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("刷卡金前4碼在ptrm0030繳款類別參數查無資料!");
       return 1;
      }

   return 0;
 }

// ************************************************************************
 public void wfAjaxFunc3(TarokoCommon wr) throws Exception
 {
  String ajaxjDataCode = "";
  super.wp = wr;


  if (selectAjaxFunc30(
                    wp.itemStr2("ax_win_data_codea"))!=0) 
     {
      wp.addJSON("ajaxj_data_code", "");
      wp.addJSON("ajaxj_source_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_data_code", sqlStr(ii, "data_code"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_source_name", sqlStr(ii, "source_name"));
 }
// ************************************************************************
 int selectAjaxFunc30(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " '' as data_code,  "
             + " '' as source_name  "
             + " from  ptr_businday "
             + " union "
             + " select "
             + " source_code as data_code,"
             + " source_name "
             + " from  ptr_src_code "
             ;
   if (s1.length()>0)
      wp.sqlCmd = wp.sqlCmd
                + " where source_code like upper('"+ s1 +"')||'%' ";

   wp.sqlCmd = wp.sqlCmd
             + " order by 1 ";
   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("來源代號:["+s1+"]查無資料");
       return 1;
      }

   return 0;
 }

// ************************************************************************
public void procUploadFile(int loadType) throws Exception
 {
  if (wp.colStr(0,"ser_num").length()>0)
     wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (wp.itemStr2("zz_file_name").indexOf(".xls")!=-1) 
     {
      alertErr("上傳格式: 不可為 excel 格式");
      return;
     }
  if (empty("zz_file_name"))
     {
      alertErr("上傳檔名: 不可空白");
      return;
     }

  if (loadType==2) fileDataImp2();
 }
// ************************************************************************
int fileUpLoad()
 {
  TarokoUpload func = new TarokoUpload();
  try {
       func.actionFunction(wp);
       wp.colSet("zz_file_name", func.fileName);
      }
   catch(Exception ex)
      {
       return -1;
      }

   return func.rc;
}
// ************************************************************************
void fileDataImp2() throws Exception
 {
  TarokoFileAccess tf = new TarokoFileAccess(wp);

  String inputFile = wp.itemStr2("zz_file_name");
  int fi = tf.openInputText(inputFile,"MS950");

  if (fi == -1) return;

  String sysUploadType  = wp.itemStr2("sys_upload_type");
  String sysUploadAlias = wp.itemStr2("sys_upload_alias");

  mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);

  if (sysUploadAlias.equals("aaa1"))
     func.dbDeleteD2Aaa1("PTR_FUND_DATA_T");
  if (sysUploadAlias.equals("aaa3"))
     func.dbDeleteD2Aaa3("PTR_FUND_DATA_T");
  if (sysUploadAlias.equals("aaak"))
     func.dbDeleteD2Aaak("PTR_FUND_DATA_T");
  if (sysUploadAlias.equals("aaa2"))
     func.dbDeleteD2Aaa2("PTR_FUND_DATA_T");

  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  tranSeqStr = comr.getSeqno("MKT_MODSEQ");

  String ss="";
  int llOk=0, llCnt=0,llErr=0,llChkErr=0;
  int lineCnt =0;
  while (true)
   {
    ss = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y")) break;
    lineCnt++;
    if (sysUploadAlias.equals("aaa1"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("aaa3"))
    {
     if (lineCnt<=0) continue;
     if (ss.length() < 2) continue;
    }
    if (sysUploadAlias.equals("aaak"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("aaa2"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }

    llCnt++; 

    for (int inti=0;inti<10;inti++) logMsg[inti]="";
    logMsg[10]=String.format("%02d",lineCnt);

    if (sysUploadAlias.equals("aaa1"))
       if (checkUploadfileAaa1(ss)!=0) continue;
    if (sysUploadAlias.equals("aaa3"))
        if (checkUploadfileAaa3(ss)!=0) continue;
    if (sysUploadAlias.equals("aaak"))
       if (checkUploadfileAaak(ss)!=0) continue;
    if (sysUploadAlias.equals("aaa2"))
       if (checkUploadfileAaa2(ss)!=0) continue;
   llOk++;

   if (notifyCnt==0)
      {
       if (sysUploadAlias.equals("aaa1"))
          {
           if (func.dbInsertI2Aaa1("PTR_FUND_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
       if (sysUploadAlias.equals("aaa3"))
       {
        if (func.dbInsertI2Aaa3("PTR_FUND_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
       }
       if (sysUploadAlias.equals("aaak"))
          {
           if (func.dbInsertI2Aaak("PTR_FUND_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
       if (sysUploadAlias.equals("aaa2"))
          {
           if (func.dbInsertI2Aaa2("PTR_FUND_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
      }
   }

  if (llErr!=0) notifyCnt=1;
  if (notifyCnt==1)
     {
      if (sysUploadAlias.equals("aaa1"))
         func.dbDeleteD2Aaa1("PTR_FUND_DATA_T");
      if (sysUploadAlias.equals("aaa3"))
          func.dbDeleteD2Aaa3("PTR_FUND_DATA_T");
      if (sysUploadAlias.equals("aaak"))
         func.dbDeleteD2Aaak("PTR_FUND_DATA_T");
      if (sysUploadAlias.equals("aaa2"))
         func.dbDeleteD2Aaa2("PTR_FUND_DATA_T");
      func.dbInsertEcsNotifyLog(tranSeqStr,(llErr+llChkErr));
     }

  sqlCommit(1);  // 1:commit else rollback

  if (notifyCnt==0)
     alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 轉入");
  else
     alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 不轉入");

  tf.closeInputText(fi);
  tf.deleteFile(inputFile);


  return;
 }
// ************************************************************************
int  checkUploadfileAaa1(String ss) throws Exception
 {
  mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "data_code";
  uploadFileCol[1]  = "data_code2";

  // ========  [I]nsert table column  ========
  uploadFileCol[2]  = "table_name";
  uploadFileCol[3]  = "data_key";
  uploadFileCol[4]  = "data_type";
  uploadFileCol[5]  = "crt_date";
  uploadFileCol[6]  = "crt_user";

  // ==== insert table content default =====
  uploadFileDat[2]  = "PTR_FUNDP";
  uploadFileDat[3]  = wp.itemStr2("fund_code");
  uploadFileDat[4]  = "1";
  uploadFileDat[5]  = wp.sysDate;
  uploadFileDat[6]  = wp.loginUser;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<2;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);

  return 0;
 }

//************************************************************************
int  checkUploadfileAaa3(String ss) throws Exception
{
mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);

for (int inti=0;inti<50;inti++)
 {
  uploadFileCol[inti] = "";
  uploadFileDat[inti] = "";
 }
// ===========  [M]edia layout =============
uploadFileCol[0]  = "data_code";
uploadFileCol[1]  = "data_code2";

// ========  [I]nsert table column  ========
uploadFileCol[2]  = "table_name";
uploadFileCol[3]  = "data_key";
uploadFileCol[4]  = "data_type";
uploadFileCol[5]  = "crt_date";
uploadFileCol[6]  = "crt_user";

// ==== insert table content default =====
uploadFileDat[2]  = "PTR_FUNDP";
uploadFileDat[3]  = wp.itemStr2("fund_code");
uploadFileDat[4]  = "P";
uploadFileDat[5]  = wp.sysDate;
uploadFileDat[6]  = wp.loginUser;

int okFlag=0;
int errFlag=0;
int[] begPos = {1};

for (int inti=0;inti<2;inti++)
   {
    uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
    if (uploadFileDat[inti].length()!=0) okFlag=1;
   }
if (okFlag==0) return(1);

return 0;
}
// ************************************************************************
int  checkUploadfileAaak(String ss) throws Exception
 {
  mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "data_code";
  uploadFileCol[1]  = "data_code2";

  // ========  [I]nsert table column  ========
  uploadFileCol[2]  = "table_name";
  uploadFileCol[3]  = "data_key";
  uploadFileCol[4]  = "data_type";
  uploadFileCol[5]  = "crt_date";
  uploadFileCol[6]  = "crt_user";

  // ==== insert table content default =====
  uploadFileDat[2]  = "PTR_FUNDP";
  uploadFileDat[3]  = wp.itemStr2("fund_code");
  uploadFileDat[4]  = "C";
  uploadFileDat[5]  = wp.sysDate;
  uploadFileDat[6]  = wp.loginUser;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<2;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);

  return 0;
 }
// ************************************************************************
int  checkUploadfileAaa2(String ss) throws Exception
 {
  mktm02.Mktm6220Func func =new mktm02.Mktm6220Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "data_code";
  uploadFileCol[1]  = "data_code2";

  // ========  [I]nsert table column  ========
  uploadFileCol[2]  = "table_name";
  uploadFileCol[3]  = "data_key";
  uploadFileCol[4]  = "data_type";
  uploadFileCol[5]  = "crt_date";
  uploadFileCol[6]  = "crt_user";

  // ==== insert table content default =====
  uploadFileDat[2]  = "PTR_FUNDP";
  uploadFileDat[3]  = wp.itemStr2("fund_code");
  uploadFileDat[4]  = "6";
  uploadFileDat[5]  = wp.sysDate;
  uploadFileDat[6]  = wp.loginUser;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<2;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);

  return 0;
 }
// ************************************************************************
// ************************************************************************
 public void checkButtonOff() throws Exception
  {
  if (wp.colStr("bin_type_sel").length()==0)
      wp.colSet("bin_type_sel" , "0");

  if (wp.colStr("bin_type_sel").equals("0"))
     {
      buttonOff("btnbint_disable");
     }
  else
     {
      wp.colSet("btnbint_disable","");
     }

  if (wp.colStr("acct_type_sel").length()==0)
      wp.colSet("acct_type_sel" , "0");

  if (wp.colStr("acct_type_sel").equals("0"))
     {
      buttonOff("btnacty_disable");
     }
  else
     {
      wp.colSet("btnacty_disable","");
     }

  if (wp.colStr("group_code_sel").length()==0)
      wp.colSet("group_code_sel" , "0");

  if (wp.colStr("group_code_sel").equals("0"))
     {
      buttonOff("btngpcd_disable");
     }
  else
     {
      wp.colSet("btngpcd_disable","");
     }

  if (wp.colStr("card_type_sel").length()==0)
      wp.colSet("card_type_sel" , "0");

  if (wp.colStr("card_type_sel").equals("0"))
     {
      buttonOff("btndype_disable");
     }
  else
     {
      wp.colSet("btndype_disable","");
     }

  if (wp.colStr("new_hldr_cond").length()==0)
      wp.colSet("new_hldr_cond" , "N");

  if (wp.colStr("new_hldr_cond").equals("N"))
     {
      buttonOff("btngnce_disable");
     }
  else
     {
      wp.colSet("btngnce_disable","");
     }

  if (wp.colStr("new_group_cond").length()==0)
      wp.colSet("new_group_cond" , "");

  if (wp.colStr("new_group_cond").equals(""))
     {
      buttonOff("btngnce_disable");
     }
  else
     {
      wp.colSet("btngnce_disable","");
     }

  if (wp.colStr("source_code_sel").length()==0)
      wp.colSet("source_code_sel" , "0");

  if (wp.colStr("source_code_sel").equals("0"))
     {
      buttonOff("btnsrcd_disable");
     }
  else
     {
      wp.colSet("btnsrcd_disable","");
     }

  if (wp.colStr("merchant_sel").length()==0)
      wp.colSet("merchant_sel" , "0");

  if (wp.colStr("merchant_sel").equals("0"))
     {
      buttonOff("btnmrch_disable");
      buttonOff("uplaaa1_disable");
     }
  else
     {
      wp.colSet("btnmrch_disable","");
      wp.colSet("uplaaa1_disable","");
     }
  
  if (wp.colStr("platform_kind_sel").equals("0"))
  {
   buttonOff("uplaaa3_disable");
  }
else
  {
   wp.colSet("uplaaa3_disable","");
  }

  if (wp.colStr("mcht_group_sel").length()==0)
      wp.colSet("mcht_group_sel" , "0");

  if (wp.colStr("mcht_group_sel").equals("0"))
     {
      buttonOff("btnaaa1_disable");
     }
  else
     {
      wp.colSet("btnaaa1_disable","");
     }
  
  if (wp.colStr("platform_kind_sel").length()==0)
      wp.colSet("platform_kind_sel" , "0");

  if (wp.colStr("platform_kind_sel").equals("0"))
     {
      buttonOff("btnaaa3_disable");
     }
  else
     {
      wp.colSet("btnaaa3_disable","");
     }

  if (wp.colStr("currency_sel").length()==0)
      wp.colSet("currency_sel" , "0");

  if (wp.colStr("currency_sel").equals("0"))
     {
      buttonOff("btncocq_disable");
     }
  else
     {
      wp.colSet("btncocq_disable","");
     }

  if (wp.colStr("ex_currency_sel").length()==0)
      wp.colSet("ex_currency_sel" , "0");

  if (wp.colStr("ex_currency_sel").equals("0"))
     {
      buttonOff("btncocd_disable");
     }
  else
     {
      wp.colSet("btncocd_disable","");
     }

  if (wp.colStr("pos_entry_sel").length()==0)
      wp.colSet("pos_entry_sel" , "0");

  if (wp.colStr("pos_entry_sel").equals("0"))
     {
      buttonOff("btnpose_disable");
     }
  else
     {
      wp.colSet("btnpose_disable","");
     }

  if (wp.colStr("pos_merchant_sel").length()==0)
      wp.colSet("pos_merchant_sel" , "0");

  if (wp.colStr("pos_merchant_sel").equals("0"))
     {
      buttonOff("btnmrck_disable");
      buttonOff("uplaaak_disable");
     }
  else
     {
      wp.colSet("btnmrck_disable","");
      wp.colSet("uplaaak_disable","");
     }

  if (wp.colStr("pos_mcht_group_sel").length()==0)
      wp.colSet("pos_mcht_group_sel" , "0");

  if (wp.colStr("pos_mcht_group_sel").equals("0"))
     {
      buttonOff("btnaaam_disable");
     }
  else
     {
      wp.colSet("btnaaam_disable","");
     }

  if (wp.colStr("d_mcc_code_sel").length()==0)
      wp.colSet("d_mcc_code_sel" , "0");

  if (wp.colStr("d_mcc_code_sel").equals("0"))
     {
      buttonOff("btnmccd_disable");
     }
  else
     {
      wp.colSet("btnmccd_disable","");
     }

  if (wp.colStr("d_merchant_sel").length()==0)
      wp.colSet("d_merchant_sel" , "0");

  if (wp.colStr("d_merchant_sel").equals("0"))
     {
      buttonOff("btnmrcd_disable");
      buttonOff("uplaaa2_disable");
     }
  else
     {
      wp.colSet("btnmrcd_disable","");
      wp.colSet("uplaaa2_disable","");
     }

  if (wp.colStr("d_mcht_group_sel").length()==0)
      wp.colSet("d_mcht_group_sel" , "0");

  if (wp.colStr("d_mcht_group_sel").equals("0"))
     {
      buttonOff("btnaaa2_disable");
     }
  else
     {
      wp.colSet("btnaaa2_disable","");
     }

  if (wp.colStr("d_ucaf_sel").length()==0)
      wp.colSet("d_ucaf_sel" , "0");

  if (wp.colStr("d_ucaf_sel").equals("0"))
     {
      buttonOff("btnucaf_disable");
     }
  else
     {
      wp.colSet("btnucaf_disable","");
     }

  if (wp.colStr("d_eci_sel").length()==0)
      wp.colSet("d_eci_sel" , "0");

  if (wp.colStr("d_eci_sel").equals("0"))
     {
      buttonOff("btndeci_disable");
     }
  else
     {
      wp.colSet("btndeci_disable","");
     }

  if (wp.colStr("d_pos_entry_sel").length()==0)
      wp.colSet("d_pos_entry_sel" , "0");

  if (wp.colStr("d_pos_entry_sel").equals("0"))
     {
      buttonOff("btnposd_disable");
     }
  else
     {
      wp.colSet("btnposd_disable","");
     }

  if ((wp.colStr("aud_type").equals("Y"))||
      (wp.colStr("aud_type").equals("D")))
     {
      buttonOff("uplaaa1_disable");
      buttonOff("uplaaa3_disable");
      buttonOff("uplaaak_disable");
      buttonOff("uplaaa2_disable");
     }
  else
     {
      wp.colSet("uplaaa1_disable","");
      wp.colSet("uplaaa3_disable","");
      wp.colSet("uplaaak_disable","");
      wp.colSet("uplaaa2_disable","");
     }
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
   if (wp.respHtml.equals("mktm6220_nadd"))
      {
       wp.colSet("purch_feed_type"   , "2");
       wp.colSet("purchase_type_sel" , "1");
       wp.colSet("new_card_days" , "180");
       wp.colSet("new_hldr_days" , "99999");
       wp.colSet("new_hldr_card" , "Y");
      }
  wp.colSet("card_feed_run_day" , "1");
  wp.colSet("feedback_months" , "1");

  buttonOff("btnbint_disable");
  buttonOff("btnacty_disable");
  buttonOff("btngpcd_disable");
  buttonOff("btndype_disable");
  buttonOff("btngnce_disable");
  buttonOff("btngnce_disable");
  buttonOff("btnsrcd_disable");
  buttonOff("btnmrch_disable");
  buttonOff("btnaaa1_disable");
  buttonOff("btnaaa3_disable");
  buttonOff("btncocq_disable");
  buttonOff("btncocd_disable");
  buttonOff("btnpose_disable");
  buttonOff("btnmrck_disable");
  buttonOff("btnaaam_disable");
  buttonOff("btnmccd_disable");
  buttonOff("btnmrcd_disable");
  buttonOff("btnaaa2_disable");
  buttonOff("btnucaf_disable");
  buttonOff("btndeci_disable");
  buttonOff("btnposd_disable");
  return;
 }
// ************************************************************************
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************
 String procDynamicDddwDataCode071()  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " country_code_2 as db_code, "
          + " country_code_2||'-('||country_code_3||'-'||number_code||'-'||chi_name||')'  as db_desc "
          + " from mkt_country "
          + " where disable_code !='Y' "
          + " order by  country_code_2 "
          ;

   return lsSql;
 }
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
//************************************************************************
String  listMktBnCdata(String s1,String s2,String s3,String s4) throws Exception
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
 String procDynamicDddwDataCodea(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " source_code as db_code, "
          + " source_code||' '||source_name as db_desc "
          + " from ptr_src_code ";

   if (s1.length()>0)
       lsSql =  lsSql
              + " where source_code like  '" + s1 +"%' "
          ;
   lsSql =  lsSql
          + " order by source_code "
          + " fetch first 999 rows only ";

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwDataType1()  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " bank_no as db_code, "
          + " max(bank_no||' '||ica_desc) as db_desc "
          + " from bil_auto_ica "
          + " group by bank_no "
          + " order  by bank_no "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
