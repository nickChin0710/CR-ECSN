/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/19  V1.00.01   Allen Ho      Initial                              *
 * 111-12-07  V1.00.02 Yanghan sync from mega & updated for project coding standard *
 *111/12/16  V1.00.03   Machao        命名规则调整后测试修改 
***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0170 extends BaseProc
{
 private final String PROGNAME = "紅利特惠參數覆核處理程式111-12-16  V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp0170Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_bpmh2_t";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt =0, recCnt =0, notifyCnt =0,colNum=0;
  int[] datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String upGroupType = "0";

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

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
              + " and a.apr_flag='N'     "
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
               + "a.active_code,"
               + "a.bonus_type,"
               + "a.active_code,"
               + "a.active_name,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.active_code,a.crt_user"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      buttonOff("btnAdd_disable");
      return;
     }

  commBonusType("comm_bonus_type");
  commCrtUser("comm_crt_user");

  commfuncAudType("aud_type");

  //list_wkdata();
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
  if (wp.itemStr("kk_active_code").length()==0)
     { 
      alertErr("查詢鍵必須輸入");
      return; 
     } 
  if (controlTabName.length()==0)
     {
      if (wp.colStr("control_tab_name").length()==0)
         controlTabName = orgTabName;
      else
         controlTabName =wp.colStr("control_tab_name");
     }
  else
     {
      if (wp.colStr("control_tab_name").length()!=0)
         controlTabName =wp.colStr("control_tab_name");
     }
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.aud_type,"
               + "a.active_code as active_code,"
               + "a.crt_user,"
               + "a.active_name,"
               + "a.bonus_type,"
               + "a.active_month_s,"
               + "a.active_month_e,"
               + "a.stop_flag,"
               + "a.stop_date,"
               + "a.give_flag,"
               + "a.stop_desc,"
               + "a.effect_months,"
               + "a.issue_cond,"
               + "a.issue_date_s,"
               + "a.issue_date_e,"
               + "a.re_months,"
//               + "a.new_hldr_cond,"
//               + "a.new_hldr_days,"
//               + "a.new_group_cond,"
//               + "a.new_hldr_card,"
//               + "a.new_hldr_sup,"
               + "a.purch_cond,"
               + "a.purch_s_date,"
               + "a.purch_e_date,"
               + "a.pre_filter_flag,"
               + "a.run_time_amt,"
               + "a.acct_type_sel,"
               + "a.group_code_sel,"
               + "a.card_type_sel,"
               + "a.limit_amt,"
               + "a.currency_sel,"
               + "a.merchant_sel,"
               + "a.mcht_group_sel,"
               + "a.mcc_code_sel,"
               + "a.pos_entry_sel,"
               + "a.currencyb_sel,"
               + "a.bl_cond,"
               + "a.ca_cond,"
               + "a.it_cond,"
               + "a.id_cond,"
               + "a.ao_cond,"
               + "a.ot_cond,"
               + "a.bill_type_sel,"
               + "a.add_times,"
               + "a.add_point,"
               + "a.per_point_amt,"
               + "a.feedback_lmt";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.active_code")
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
  commPreFilter("comm_pre_filter_flag");
  commAcctType("comm_acct_type_sel");
  commGroupCode("comm_group_code_sel");
  commCardType("comm_card_type_sel");
  commCurrency("comm_currency_sel");
  commMerchant("comm_merchant_sel");
  commMchtGroup("comm_mcht_group_sel");
  commMccCode("comm_mcc_code_sel");
  commPosEntry("comm_pos_entry_sel");
  commCurrenc("comm_currencyb_sel");
  commBillType("comm_bill_type_sel");
  commCrtUser("comm_crt_user");
  checkButtonOff();
  km1 = wp.colStr("active_code");
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
  wp.colSet("control_tab_name", controlTabName);
  controlTabName = "mkt_bpmh2";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.active_code as active_code,"
               + "a.crt_user as bef_crt_user,"
               + "a.active_name as bef_active_name,"
               + "a.bonus_type as bef_bonus_type,"
               + "a.active_month_s as bef_active_month_s,"
               + "a.active_month_e as bef_active_month_e,"
               + "a.stop_flag as bef_stop_flag,"
               + "a.stop_date as bef_stop_date,"
               + "a.give_flag as bef_give_flag,"
               + "a.stop_desc as bef_stop_desc,"
               + "a.effect_months as bef_effect_months,"
               + "a.issue_cond as bef_issue_cond,"
               + "a.issue_date_s as bef_issue_date_s,"
               + "a.issue_date_e as bef_issue_date_e,"
               + "a.re_months as bef_re_months,"
//               + "a.new_hldr_cond as bef_new_hldr_cond,"
//               + "a.new_hldr_days as bef_new_hldr_days,"
//               + "a.new_group_cond as bef_new_group_cond,"
//               + "a.new_hldr_card as bef_new_hldr_card,"
//               + "a.new_hldr_sup as bef_new_hldr_sup,"
               + "a.purch_cond as bef_purch_cond,"
               + "a.purch_s_date as bef_purch_s_date,"
               + "a.purch_e_date as bef_purch_e_date,"
               + "a.pre_filter_flag as bef_pre_filter_flag,"
               + "a.run_time_amt as bef_run_time_amt,"
               + "a.acct_type_sel as bef_acct_type_sel,"
               + "a.group_code_sel as bef_group_code_sel,"
               + "a.card_type_sel as bef_card_type_sel,"
               + "a.limit_amt as bef_limit_amt,"
               + "a.currency_sel as bef_currency_sel,"
               + "a.merchant_sel as bef_merchant_sel,"
               + "a.mcht_group_sel as bef_mcht_group_sel,"
               + "a.mcc_code_sel as bef_mcc_code_sel,"
               + "a.pos_entry_sel as bef_pos_entry_sel,"
               + "a.currencyb_sel as bef_currencyb_sel,"
               + "a.bl_cond as bef_bl_cond,"
               + "a.ca_cond as bef_ca_cond,"
               + "a.it_cond as bef_it_cond,"
               + "a.id_cond as bef_id_cond,"
               + "a.ao_cond as bef_ao_cond,"
               + "a.ot_cond as bef_ot_cond,"
               + "a.bill_type_sel as bef_bill_type_sel,"
               + "a.add_times as bef_add_times,"
               + "a.add_point as bef_add_point,"
               + "a.per_point_amt as bef_per_point_amt,"
               + "a.feedback_lmt as bef_feedback_lmt";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.active_code")
              ;

  pageSelect();
  if (sqlNotFind())
     {
      wp.notFound ="";
      return;
     }
  wp.colSet("control_tab_name", controlTabName);

  if (wp.respHtml.indexOf("_detl") > 0) 
     wp.colSet("btnStore_disable","");   
  commCrtUser("comm_crt_user");
  commPreFilter("comm_pre_filter_flag");
  commAcctType("comm_acct_type_sel");
  commGroupCode("comm_group_code_sel");
  commCardType("comm_card_type_sel");
  commCurrency("comm_currency_sel");
  commMerchant("comm_merchant_sel");
  commMchtGroup("comm_mcht_group_sel");
  commMccCode("comm_mcc_code_sel");
  commPosEntry("comm_pos_entry_sel");
  commCurrenc("comm_currencyb_sel");
  commBillType("comm_bill_type_sel");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("new_group_cond_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"4"));
  wp.colSet("acct_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"3"));
  wp.colSet("group_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"2"));
  wp.colSet("card_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"8"));
  wp.colSet("currency_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"7"));
  wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"1"));
  wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"B"));
  wp.colSet("mcc_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"5"));
  wp.colSet("pos_entry_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"L"));
  wp.colSet("currencyb_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"M"));
  wp.colSet("bill_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH2",wp.colStr("active_code"),"6"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
     wp.colSet("opt_active_name","Y");

  if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
     wp.colSet("opt_bonus_type","Y");

  if (!wp.colStr("active_month_s").equals(wp.colStr("bef_active_month_s")))
     wp.colSet("opt_active_month_s","Y");

  if (!wp.colStr("active_month_e").equals(wp.colStr("bef_active_month_e")))
     wp.colSet("opt_active_month_e","Y");

  if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
     wp.colSet("opt_stop_flag","Y");

  if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
     wp.colSet("opt_stop_date","Y");

  if (!wp.colStr("give_flag").equals(wp.colStr("bef_give_flag")))
     wp.colSet("opt_give_flag","Y");

  if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
     wp.colSet("opt_stop_desc","Y");

  if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
     wp.colSet("opt_effect_months","Y");

  if (!wp.colStr("issue_cond").equals(wp.colStr("bef_issue_cond")))
     wp.colSet("opt_issue_cond","Y");

  if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
     wp.colSet("opt_issue_date_s","Y");

  if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
     wp.colSet("opt_issue_date_e","Y");

  if (!wp.colStr("re_months").equals(wp.colStr("bef_re_months")))
     wp.colSet("opt_re_months","Y");

//  if (!wp.colStr("new_hldr_cond").equals(wp.colStr("bef_new_hldr_cond")))
//     wp.colSet("opt_new_hldr_cond","Y");
//
//  if (!wp.colStr("new_hldr_days").equals(wp.colStr("bef_new_hldr_days")))
//     wp.colSet("opt_new_hldr_days","Y");
//
//  if (!wp.colStr("new_group_cond").equals(wp.colStr("bef_new_group_cond")))
//     wp.colSet("opt_new_group_cond","Y");

  wp.colSet("bef_new_group_cond_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"4"));
  if (!wp.colStr("new_group_cond_cnt").equals(wp.colStr("bef_new_group_cond_cnt")))
     wp.colSet("opt_new_group_cond_cnt","Y");

//  if (!wp.colStr("new_hldr_card").equals(wp.colStr("bef_new_hldr_card")))
//     wp.colSet("opt_new_hldr_card","Y");
//
//  if (!wp.colStr("new_hldr_sup").equals(wp.colStr("bef_new_hldr_sup")))
//     wp.colSet("opt_new_hldr_sup","Y");

  if (!wp.colStr("purch_cond").equals(wp.colStr("bef_purch_cond")))
     wp.colSet("opt_purch_cond","Y");

  if (!wp.colStr("purch_s_date").equals(wp.colStr("bef_purch_s_date")))
     wp.colSet("opt_purch_s_date","Y");

  if (!wp.colStr("purch_e_date").equals(wp.colStr("bef_purch_e_date")))
     wp.colSet("opt_purch_e_date","Y");

  if (!wp.colStr("pre_filter_flag").equals(wp.colStr("bef_pre_filter_flag")))
     wp.colSet("opt_pre_filter_flag","Y");
  commPreFilter("comm_pre_filter_flag");
  commPreFilter("comm_bef_pre_filter_flag");

  if (!wp.colStr("run_time_amt").equals(wp.colStr("bef_run_time_amt")))
     wp.colSet("opt_run_time_amt","Y");

  if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
     wp.colSet("opt_acct_type_sel","Y");
  commAcctType("comm_acct_type_sel");
  commAcctType("comm_bef_acct_type_sel");

  wp.colSet("bef_acct_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"3"));
  if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
     wp.colSet("opt_acct_type_sel_cnt","Y");

  if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
     wp.colSet("opt_group_code_sel","Y");
  commGroupCode("comm_group_code_sel");
  commGroupCode("comm_bef_group_code_sel");

  wp.colSet("bef_group_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"2"));
  if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
     wp.colSet("opt_group_code_sel_cnt","Y");

  if (!wp.colStr("card_type_sel").equals(wp.colStr("bef_card_type_sel")))
     wp.colSet("opt_card_type_sel","Y");
  commCardType("comm_card_type_sel");
  commCardType("comm_bef_card_type_sel");

  wp.colSet("bef_card_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"8"));
  if (!wp.colStr("card_type_sel_cnt").equals(wp.colStr("bef_card_type_sel_cnt")))
     wp.colSet("opt_card_type_sel_cnt","Y");

  if (!wp.colStr("limit_amt").equals(wp.colStr("bef_limit_amt")))
     wp.colSet("opt_limit_amt","Y");

  if (!wp.colStr("currency_sel").equals(wp.colStr("bef_currency_sel")))
     wp.colSet("opt_currency_sel","Y");
  commCurrency("comm_currency_sel");
  commCurrency("comm_bef_currency_sel");

  wp.colSet("bef_currency_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"7"));
  if (!wp.colStr("currency_sel_cnt").equals(wp.colStr("bef_currency_sel_cnt")))
     wp.colSet("opt_currency_sel_cnt","Y");

  if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
     wp.colSet("opt_merchant_sel","Y");
  commMerchant("comm_merchant_sel");
  commMerchant("comm_bef_merchant_sel");

  wp.colSet("bef_merchant_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"1"));
  if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
     wp.colSet("opt_merchant_sel_cnt","Y");

  if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
     wp.colSet("opt_mcht_group_sel","Y");
  commMchtGroup("comm_mcht_group_sel");
  commMchtGroup("comm_bef_mcht_group_sel");

  wp.colSet("bef_mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"B"));
  if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
     wp.colSet("opt_mcht_group_sel_cnt","Y");

  if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
     wp.colSet("opt_mcc_code_sel","Y");
  commMccCode("comm_mcc_code_sel");
  commMccCode("comm_bef_mcc_code_sel");

  wp.colSet("bef_mcc_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"5"));
  if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
     wp.colSet("opt_mcc_code_sel_cnt","Y");

  if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
     wp.colSet("opt_pos_entry_sel","Y");
  commPosEntry("comm_pos_entry_sel");
  commPosEntry("comm_bef_pos_entry_sel");

  wp.colSet("bef_pos_entry_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"L"));
  if (!wp.colStr("pos_entry_sel_cnt").equals(wp.colStr("bef_pos_entry_sel_cnt")))
     wp.colSet("opt_pos_entry_sel_cnt","Y");

  if (!wp.colStr("currencyb_sel").equals(wp.colStr("bef_currencyb_sel")))
     wp.colSet("opt_currencyb_sel","Y");
  commCurrenc("comm_currencyb_sel");
  commCurrenc("comm_bef_currencyb_sel");

  wp.colSet("bef_currencyb_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"M"));
  if (!wp.colStr("currencyb_sel_cnt").equals(wp.colStr("bef_currencyb_sel_cnt")))
     wp.colSet("opt_currencyb_sel_cnt","Y");

  if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
     wp.colSet("opt_bl_cond","Y");

  if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
     wp.colSet("opt_ca_cond","Y");

  if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
     wp.colSet("opt_it_cond","Y");

  if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
     wp.colSet("opt_id_cond","Y");

  if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
     wp.colSet("opt_ao_cond","Y");

  if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
     wp.colSet("opt_ot_cond","Y");

  if (!wp.colStr("bill_type_sel").equals(wp.colStr("bef_bill_type_sel")))
     wp.colSet("opt_bill_type_sel","Y");
  commBillType("comm_bill_type_sel");
  commBillType("comm_bef_bill_type_sel");

  wp.colSet("bef_bill_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH2",wp.colStr("active_code"),"6"));
  if (!wp.colStr("bill_type_sel_cnt").equals(wp.colStr("bef_bill_type_sel_cnt")))
     wp.colSet("opt_bill_type_sel_cnt","Y");

  if (!wp.colStr("add_times").equals(wp.colStr("bef_add_times")))
     wp.colSet("opt_add_times","Y");

  if (!wp.colStr("add_point").equals(wp.colStr("bef_add_point")))
     wp.colSet("opt_add_point","Y");

  if (!wp.colStr("per_point_amt").equals(wp.colStr("bef_per_point_amt")))
     wp.colSet("opt_per_point_amt","Y");

  if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
     wp.colSet("opt_feedback_lmt","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("active_name","");
       wp.colSet("bonus_type","");
       wp.colSet("active_month_s","");
       wp.colSet("active_month_e","");
       wp.colSet("stop_flag","");
       wp.colSet("stop_date","");
       wp.colSet("give_flag","");
       wp.colSet("stop_desc","");
       wp.colSet("effect_months","");
       wp.colSet("issue_cond","");
       wp.colSet("issue_date_s","");
       wp.colSet("issue_date_e","");
       wp.colSet("re_months","");
//       wp.colSet("new_hldr_cond","");
//       wp.colSet("new_hldr_days","");
//       wp.colSet("new_group_cond","");
       wp.colSet("new_group_cond_cnt","");
//       wp.colSet("new_hldr_card","");
//       wp.colSet("new_hldr_sup","");
       wp.colSet("purch_cond","");
       wp.colSet("purch_s_date","");
       wp.colSet("purch_e_date","");
       wp.colSet("pre_filter_flag","");
       wp.colSet("run_time_amt","");
       wp.colSet("acct_type_sel","");
       wp.colSet("acct_type_sel_cnt","");
       wp.colSet("group_code_sel","");
       wp.colSet("group_code_sel_cnt","");
       wp.colSet("card_type_sel","");
       wp.colSet("card_type_sel_cnt","");
       wp.colSet("limit_amt","");
       wp.colSet("currency_sel","");
       wp.colSet("currency_sel_cnt","");
       wp.colSet("merchant_sel","");
       wp.colSet("merchant_sel_cnt","");
       wp.colSet("mcht_group_sel","");
       wp.colSet("mcht_group_sel_cnt","");
       wp.colSet("mcc_code_sel","");
       wp.colSet("mcc_code_sel_cnt","");
       wp.colSet("pos_entry_sel","");
       wp.colSet("pos_entry_sel_cnt","");
       wp.colSet("currencyb_sel","");
       wp.colSet("currencyb_sel_cnt","");
       wp.colSet("bl_cond","");
       wp.colSet("ca_cond","");
       wp.colSet("it_cond","");
       wp.colSet("id_cond","");
       wp.colSet("ao_cond","");
       wp.colSet("ot_cond","");
       wp.colSet("bill_type_sel","");
       wp.colSet("bill_type_sel_cnt","");
       wp.colSet("add_times","");
       wp.colSet("add_point","");
       wp.colSet("per_point_amt","");
       wp.colSet("feedback_lmt","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("active_name").length()==0)
     wp.colSet("opt_active_name","Y");

  if (wp.colStr("bonus_type").length()==0)
     wp.colSet("opt_bonus_type","Y");

  if (wp.colStr("active_month_s").length()==0)
     wp.colSet("opt_active_month_s","Y");

  if (wp.colStr("active_month_e").length()==0)
     wp.colSet("opt_active_month_e","Y");

  if (wp.colStr("stop_flag").length()==0)
     wp.colSet("opt_stop_flag","Y");

  if (wp.colStr("stop_date").length()==0)
     wp.colSet("opt_stop_date","Y");

  if (wp.colStr("give_flag").length()==0)
     wp.colSet("opt_give_flag","Y");

  if (wp.colStr("stop_desc").length()==0)
     wp.colSet("opt_stop_desc","Y");

  if (wp.colStr("effect_months").length()==0)
     wp.colSet("opt_effect_months","Y");

  if (wp.colStr("issue_cond").length()==0)
     wp.colSet("opt_issue_cond","Y");

  if (wp.colStr("issue_date_s").length()==0)
     wp.colSet("opt_issue_date_s","Y");

  if (wp.colStr("issue_date_e").length()==0)
     wp.colSet("opt_issue_date_e","Y");

  if (wp.colStr("re_months").length()==0)
     wp.colSet("opt_re_months","Y");

//  if (wp.colStr("new_hldr_cond").length()==0)
//     wp.colSet("opt_new_hldr_cond","Y");
//
//  if (wp.colStr("new_hldr_days").length()==0)
//     wp.colSet("opt_new_hldr_days","Y");
//
//  if (wp.colStr("new_group_cond").length()==0)
//     wp.colSet("opt_new_group_cond","Y");
//
//
//  if (wp.colStr("new_hldr_card").length()==0)
//     wp.colSet("opt_new_hldr_card","Y");
//
//  if (wp.colStr("new_hldr_sup").length()==0)
//     wp.colSet("opt_new_hldr_sup","Y");

  if (wp.colStr("purch_cond").length()==0)
     wp.colSet("opt_purch_cond","Y");

  if (wp.colStr("purch_s_date").length()==0)
     wp.colSet("opt_purch_s_date","Y");

  if (wp.colStr("purch_e_date").length()==0)
     wp.colSet("opt_purch_e_date","Y");

  if (wp.colStr("pre_filter_flag").length()==0)
     wp.colSet("opt_pre_filter_flag","Y");

  if (wp.colStr("run_time_amt").length()==0)
     wp.colSet("opt_run_time_amt","Y");

  if (wp.colStr("acct_type_sel").length()==0)
     wp.colSet("opt_acct_type_sel","Y");


  if (wp.colStr("group_code_sel").length()==0)
     wp.colSet("opt_group_code_sel","Y");


  if (wp.colStr("card_type_sel").length()==0)
     wp.colSet("opt_card_type_sel","Y");


  if (wp.colStr("limit_amt").length()==0)
     wp.colSet("opt_limit_amt","Y");

  if (wp.colStr("currency_sel").length()==0)
     wp.colSet("opt_currency_sel","Y");


  if (wp.colStr("merchant_sel").length()==0)
     wp.colSet("opt_merchant_sel","Y");


  if (wp.colStr("mcht_group_sel").length()==0)
     wp.colSet("opt_mcht_group_sel","Y");


  if (wp.colStr("mcc_code_sel").length()==0)
     wp.colSet("opt_mcc_code_sel","Y");


  if (wp.colStr("pos_entry_sel").length()==0)
     wp.colSet("opt_pos_entry_sel","Y");


  if (wp.colStr("currencyb_sel").length()==0)
     wp.colSet("opt_currencyb_sel","Y");


  if (wp.colStr("bl_cond").length()==0)
     wp.colSet("opt_bl_cond","Y");

  if (wp.colStr("ca_cond").length()==0)
     wp.colSet("opt_ca_cond","Y");

  if (wp.colStr("it_cond").length()==0)
     wp.colSet("opt_it_cond","Y");

  if (wp.colStr("id_cond").length()==0)
     wp.colSet("opt_id_cond","Y");

  if (wp.colStr("ao_cond").length()==0)
     wp.colSet("opt_ao_cond","Y");

  if (wp.colStr("ot_cond").length()==0)
     wp.colSet("opt_ot_cond","Y");

  if (wp.colStr("bill_type_sel").length()==0)
     wp.colSet("opt_bill_type_sel","Y");


  if (wp.colStr("add_times").length()==0)
     wp.colSet("opt_add_times","Y");

  if (wp.colStr("add_point").length()==0)
     wp.colSet("opt_add_point","Y");

  if (wp.colStr("per_point_amt").length()==0)
     wp.colSet("opt_per_point_amt","Y");

  if (wp.colStr("feedback_lmt").length()==0)
     wp.colSet("opt_feedback_lmt","Y");

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
   bnTable = "mkt_bn_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'MKT_BPMH2' "
                ;
   if (wp.respHtml.equals("mktp0170_gncd"))
      wp.whereStr  += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktp0170_actp"))
      wp.whereStr  += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktp0170_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktp0170_caty"))
      wp.whereStr  += " and data_type  = '8' ";
   if (wp.respHtml.equals("mktp0170_aaa1"))
      wp.whereStr  += " and data_type  = 'B' ";
   if (wp.respHtml.equals("mktp0170_mccc"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktp0170_pose"))
      wp.whereStr  += " and data_type  = 'L' ";
   if (wp.respHtml.equals("mktp0170_bisr"))
      wp.whereStr  += " and data_type  = '6' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
   wp.whereStr  += " order by 4,5,6 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
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
   if (wp.respHtml.equals("mktp0170_gncd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0170_actp"))
    commDataCode01("comm_data_code");
   if (wp.respHtml.equals("mktp0170_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0170_caty"))
    commDataCode02("comm_data_code");
   if (wp.respHtml.equals("mktp0170_aaa1"))
    commMechtGroup("comm_data_code");
   if (wp.respHtml.equals("mktp0170_mccc"))
    commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktp0170_pose"))
    commEntryMode("comm_data_code");
   if (wp.respHtml.equals("mktp0170_bisr"))
    commBillType1("comm_data_code");
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
   bnTable = "mkt_bn_data_t";

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
              + " and table_name  =  'MKT_BPMH2' "
                ;
   if (wp.respHtml.equals("mktp0170_cocd"))
      wp.whereStr  += " and data_type  = '7' ";
   if (wp.respHtml.equals("mktp0170_mrch"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktp0170_mccd"))
      wp.whereStr  += " and data_type  = 'M' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
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
   if (wp.respHtml.equals("mktp0170_cocd"))
    commCurrcode("comm_data_code2");
   if (wp.respHtml.equals("mktp0170_mccd"))
    commCurrcode("comm_data_code2");
  }
// ************************************************************************
 public int selectBndataCount(String bndata_table, String whereStr ) throws Exception
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndata_table
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
  Mktp0170Func func =new Mktp0170Func(wp);

  String[] activeCodes = wp.itemBuff("active_code");
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

     func.varsSet("active_code", activeCodes[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        {
        rc =func.dbInsertA4();
        if (rc==1) rc = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }
     else if (lsAudType[rr].equals("U"))
        {
        rc =func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4Bndata();
        if (rc==1) rc  = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }
     else if (lsAudType[rr].equals("D"))
        {
         rc =func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }

     if (rc!=1) alertErr(func.getMsg());
     if (rc == 1)
        {
         commBonusType("comm_bonus_type");
         commCrtUser("comm_crt_user");
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
       if ((wp.respHtml.equals("mktp0170")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_active_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_active_code");
             }
          lsSql = "";
          lsSql =  procDynamicDddwActiveCode1(wp.colStr("ex_active_code"));
          wp.optionKey = wp.colStr("ex_active_code");
          dddwList("dddw_active_code_1", lsSql);
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
       if ((wp.respHtml.equals("mktp0170_actp")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_caty")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type1"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_cocd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_bin_type"
                 ,"ptr_bintable"
                 ,"trim(bin_type)"
                 ,""
                 ," group by bin_type");
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_currcode"
                 ,"ptr_currcode"
                 ,"trim(curr_code)"
                 ,"trim(curr_chi_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_mccc")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_pose")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_entry_mode"
                 ,"cca_entry_mode"
                 ,"trim(entry_mode)"
                 ,"trim(mode_desc)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_mccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_bin_type"
                 ,"ptr_bintable"
                 ,"trim(bin_type)"
                 ,""
                 ," group by bin_type");
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_currcode"
                 ,"ptr_currcode"
                 ,"trim(curr_code)"
                 ,"trim(curr_chi_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_gncd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0170_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
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
 public void commCrtUser(String s1, int befType) throws Exception
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
 public void commBonusType(String s1) throws Exception
 {
  commBonusType(s1,0);
  return;
 }
// ************************************************************************
 public void commBonusType(String s1, int befType) throws Exception
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " wf_desc as column_wf_desc "
            + " from ptr_sys_idtab "
            + " where 1 = 1 "
            + " and   wf_id = '"+wp.colStr(ii,befStr+"bonus_TYPE")+"'"
            + " and   wf_type = 'BONUS_NAME' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode01(String s1) throws Exception
 {
  commDataCode01(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode01(String s1, int befType) throws Exception
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
 public void commDataCode04(String s1, int befType) throws Exception
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
 public void commDataCode02(String s1, int befType) throws Exception
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
 public void commCurrcode(String s1) throws Exception
 {
  commCurrcode(s1,0);
  return;
 }
// ************************************************************************
 public void commCurrcode(String s1, int befType) throws Exception
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
 public void commDataCode07(String s1) throws Exception
 {
  commDataCode07(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode07(String s1, int befType) throws Exception
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
 public void commEntryMode(String s1) throws Exception
 {
  commEntryMode(s1,0);
  return;
 }
// ************************************************************************
 public void commEntryMode(String s1, int befType) throws Exception
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
 public void commBillType1(String s1) throws Exception
 {
  commBillType1(s1,0);
  return;
 }
// ************************************************************************
 public void commBillType1(String s1, int befType) throws Exception
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " inter_desc as column_inter_desc "
            + " from ptr_billtype "
            + " where 1 = 1 "
            + " and   bill_type = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_inter_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commMechtGroup(String s1) throws Exception
 {
  commMechtGroup(s1,0);
  return;
 }
// ************************************************************************
 public void commMechtGroup(String s1, int befType) throws Exception
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
            + " where 1 = 1 "
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
 public void commPreFilter(String s1) throws Exception
 {
  String[] cde = {"1","2"};
  String[] txt = {"A區符合給B區","A區"};
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
 public void commAcctType(String s1) throws Exception
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
 public void commGroupCode(String s1) throws Exception
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
 public void commCardType(String s1) throws Exception
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
 public void commCurrency(String s1) throws Exception
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
 public void commMerchant(String s1) throws Exception
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
 public void commMchtGroup(String s1) throws Exception
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
 public void commMccCode(String s1) throws Exception
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
 public void commPosEntry(String s1) throws Exception
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
 public void commCurrenc(String s1) throws Exception
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
 public void commBillType(String s1) throws Exception
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
 String listMktBnData(String s1, String s2, String s3, String s4) throws Exception
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
          + " from sec_user a,mkt_bpmh2_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwActiveCode1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.active_code as db_code, "
          + " max(b.active_code||' '||b.active_name) as db_desc "
          + " from mkt_bpmh2_t b "
          + " where   b.apr_flag = 'N' "
          + " group by b.active_code "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
