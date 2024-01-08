/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/14  V1.00.02   Allen Ho      Initial                              *
* 111/12/07  V1.00.03  Machao    sync from mega & updated for project coding standard   
* 111/12/16  V1.00.04   Machao        命名规则调整后测试修改                                                                          *
* 112/04/06  V1.00.05   JiangYingdong        program update                *
***************************************************************************/
package mktp01;

import mktp01.Mktp0360Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0360 extends BaseProc
{
 private final String PROGNAME = "紅利特惠(五)-特店刷卡加贈點數參數覆核處理程式111/12/07  V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0360Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_bpmh3_t";
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
              + sqlCol(wp.itemStr2("ex_active_code"), "a.active_code", "like%")
              + sqlCol(wp.itemStr2("ex_crt_user"), "a.crt_user", "like%")
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
               + "a.active_name,"
               + "a.proc_date,"
               + "a.active_date_s,"
               + "a.stop_flag,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by stop_flag,active_code,proc_date desc"
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
  if (wp.itemStr2("kk_active_code").length()==0)
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
               + "a.active_code as active_code,"
               + "a.crt_user,"
               + "a.active_name,"
               + "a.bonus_type,"
               + "a.tax_flag,"
               + "a.active_date_s,"
               + "a.active_date_e,"
               + "a.proc_date,"
               + "a.effect_months,"
               + "a.stop_flag,"
               + "a.stop_date,"
               + "a.stop_desc,"
               + "a.list_cond,"
               + "a.bpmh3_file,"
               + "a.cond_imp_desc,"
               + "a.purch_cond,"
               + "a.run_start_cond,"
               + "a.vd_flag,"
               + "a.issue_cond,"
               + "a.issue_date_s,"
               + "a.issue_date_e,"
               + "a.card_re_days,"
               + "a.purch_s_date,"
               + "a.purch_e_date,"
               + "a.run_start_month,"
               + "a.run_time_mm,"
               + "a.run_time_type,"
               + "a.run_time_dd,"
               + "a.acct_type_sel,"
               + "a.vd_corp_flag,"
               + "a.group_card_sel,"
//               + "a.group_oppost_cond,"
               + "a.merchant_sel,"
               + "a.mcht_group_sel,"
               + "a.platform_kind_sel,"
               + "a.mcc_code_sel,"
               + "a.per_point_amt,"
               + "a.bl_cond,"
               + "a.ca_cond,"
               + "a.it_cond,"
               + "a.it_flag,"
               + "a.id_cond,"
               + "a.ao_cond,"
               + "a.ot_cond,"
               + "a.bill_type_sel,"
               + "a.currency_sel,"
               + "a.feedback_lmt,"
               + "a.add_type,"
               + "a.add_item_flag,"
               + "a.add_item_amt,"
               + "a.add_amt_s1,"
               + "a.add_amt_e1,"
               + "a.add_times1,"
               + "a.add_point1,"
               + "a.add_amt_s2,"
               + "a.add_amt_e2,"
               + "a.add_times2,"
               + "a.add_point2,"
               + "a.add_amt_s3,"
               + "a.add_amt_e3,"
               + "a.add_times3,"
               + "a.add_point3,"
               + "a.add_amt_s4,"
               + "a.add_amt_e4,"
               + "a.add_times4,"
               + "a.add_point4,"
               + "a.add_amt_s5,"
               + "a.add_amt_e5,"
               + "a.add_times5,"
               + "a.add_point5,"
               + "a.add_amt_s6,"
               + "a.add_amt_e6,"
               + "a.add_times6,"
               + "a.add_point6,"
               + "a.add_amt_s7,"
               + "a.add_amt_e7,"
               + "a.add_times7,"
               + "a.add_point7,"
               + "a.add_amt_s8,"
               + "a.add_amt_e8,"
               + "a.add_times8,"
               + "a.add_point8,"
               + "a.add_amt_s9,"
               + "a.add_amt_e9,"
               + "a.add_times9,"
               + "a.add_point9,"
               + "a.add_amt_s10,"
               + "a.add_amt_e10,"
               + "a.add_times10,"
               + "a.add_point10,"
               + "a.doorsill_flag,"
               + "a.d_group_card_sel,"
               + "a.d_merchant_sel,"
               + "a.d_mcht_group_sel,"
               + "a.platform2_kind_sel,"
               + "a.d_mcc_code_sel,"
               + "a.d_card_type_sel,"
               + "a.d_bl_cond,"
               + "a.d_ca_cond,"
               + "a.d_it_cond,"
               + "a.d_it_flag,"
               + "a.d_id_cond,"
               + "a.d_ao_cond,"
               + "a.d_ot_cond,"
               + "a.d_bill_type_sel,"
               + "a.d_currency_sel,"
               + "a.d_pos_entry_sel,"
               + "a.d_ucaf_sel,"
               + "a.d_eci_sel,"
               + "a.d_tax_flag,"
               + "a.d_add_item_flag,"
               + "a.d_add_item_amt,"
               + "a.d_add_amt_s1,"
               + "a.d_add_amt_e1,"
               + "a.d_add_point1,"
               + "a.d_add_amt_s2,"
               + "a.d_add_amt_e2,"
               + "a.d_add_point2,"
               + "a.d_add_amt_s3,"
               + "a.d_add_amt_e3,"
               + "a.d_add_point3,"
               + "a.d_add_amt_s4,"
               + "a.d_add_amt_e4,"
               + "a.d_add_point4,"
               + "a.d_add_amt_s5,"
               + "a.d_add_amt_e5,"
               + "a.d_add_point5,"
               + "a.d_add_amt_s6,"
               + "a.d_add_amt_e6,"
               + "a.d_add_point6,"
               + "a.d_add_amt_s7,"
               + "a.d_add_amt_e7,"
               + "a.d_add_point7,"
               + "a.d_add_amt_s8,"
               + "a.d_add_amt_e8,"
               + "a.d_add_point8,"
               + "a.d_add_amt_s9,"
               + "a.d_add_amt_e9,"
               + "a.d_add_point9,"
               + "a.d_add_amt_s10,"
               + "a.d_add_amt_e10,"
               + "a.d_add_point10,"
               + "a.crt_date";

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
  commListCond("comm_list_cond");
  commRunTimeType("comm_run_time_type");
  commAcctTypeSel("comm_acct_type_sel");
  commGroupCardSel("comm_group_card_sel");
  commMerchantSel("comm_merchant_sel");
  commMchtGruopSel("comm_mcht_group_sel");
  commMchtGruopSel("comm_platform_kind_sel");
  commMccCodeSle("comm_mcc_code_sel");
  commItFlag("comm_it_flag");
  commBillTypeSell("comm_bill_type_sel");
  commCurrencySel("comm_currency_sel");
  commAddType("comm_add_type");
  commAddItemFlag("comm_add_item_flag");
  commDGroupCrdSel("comm_d_group_card_sel");
  commDMerchantSel("comm_d_merchant_sel");
  commDMchtGroup("comm_d_mcht_group_sel");
  commDMchtGroup("comm_platform2_kind_sel");
  commDMccCode("comm_d_mcc_code_sel");
  commDCardType("comm_d_card_type_sel");
  commDItFlag("comm_d_it_flag");
  commDBillTypeSel("comm_d_bill_type_sel");
  commDCurrencySel("comm_d_currency_sel");
  commDPosEntrySel("comm_d_pos_entry_sel");
  commDUcfaSel("comm_d_ucaf_sel");
  commDEciSel("comm_d_eci_sel");
  dAddItemFlag("comm_d_add_item_flag");
  commCrtUser("comm_crt_user");
  commBonusType("comm_bonus_type");
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
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = "MKT_BPMH3";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.active_code as active_code,"
               + "a.crt_user as bef_crt_user,"
               + "a.active_name as bef_active_name,"
               + "a.bonus_type as bef_bonus_type,"
               + "a.tax_flag as bef_tax_flag,"
               + "a.active_date_s as bef_active_date_s,"
               + "a.active_date_e as bef_active_date_e,"
               + "a.proc_date as bef_proc_date,"
               + "a.effect_months as bef_effect_months,"
               + "a.stop_flag as bef_stop_flag,"
               + "a.stop_date as bef_stop_date,"
               + "a.stop_desc as bef_stop_desc,"
               + "a.list_cond as bef_list_cond,"
               + "a.bpmh3_file as bef_bpmh3_file,"
               + "a.cond_imp_desc as bef_cond_imp_desc,"
               + "a.purch_cond as bef_purch_cond,"
               + "a.run_start_cond as bef_run_start_cond,"
               + "a.vd_flag as bef_vd_flag,"
               + "a.issue_cond as bef_issue_cond,"
               + "a.issue_date_s as bef_issue_date_s,"
               + "a.issue_date_e as bef_issue_date_e,"
               + "a.card_re_days as bef_card_re_days,"
               + "a.purch_s_date as bef_purch_s_date,"
               + "a.purch_e_date as bef_purch_e_date,"
               + "a.run_start_month as bef_run_start_month,"
               + "a.run_time_mm as bef_run_time_mm,"
               + "a.run_time_type as bef_run_time_type,"
               + "a.run_time_dd as bef_run_time_dd,"
               + "a.acct_type_sel as bef_acct_type_sel,"
               + "a.vd_corp_flag as bef_vd_corp_flag,"
               + "a.group_card_sel as bef_group_card_sel,"
//               + "a.group_oppost_cond as bef_group_oppost_cond,"
               + "a.merchant_sel as bef_merchant_sel,"
               + "a.mcht_group_sel as bef_mcht_group_sel,"
               + "a.platform_kind_sel as bef_platform_kind_sel,"
               + "a.mcc_code_sel as bef_mcc_code_sel,"
               + "a.per_point_amt as bef_per_point_amt,"
               + "a.bl_cond as bef_bl_cond,"
               + "a.ca_cond as bef_ca_cond,"
               + "a.it_cond as bef_it_cond,"
               + "a.it_flag as bef_it_flag,"
               + "a.id_cond as bef_id_cond,"
               + "a.ao_cond as bef_ao_cond,"
               + "a.ot_cond as bef_ot_cond,"
               + "a.bill_type_sel as bef_bill_type_sel,"
               + "a.currency_sel as bef_currency_sel,"
               + "a.feedback_lmt as bef_feedback_lmt,"
               + "a.add_type as bef_add_type,"
               + "a.add_item_flag as bef_add_item_flag,"
               + "a.add_item_amt as bef_add_item_amt,"
               + "a.add_amt_s1 as bef_add_amt_s1,"
               + "a.add_amt_e1 as bef_add_amt_e1,"
               + "a.add_times1 as bef_add_times1,"
               + "a.add_point1 as bef_add_point1,"
               + "a.add_amt_s2 as bef_add_amt_s2,"
               + "a.add_amt_e2 as bef_add_amt_e2,"
               + "a.add_times2 as bef_add_times2,"
               + "a.add_point2 as bef_add_point2,"
               + "a.add_amt_s3 as bef_add_amt_s3,"
               + "a.add_amt_e3 as bef_add_amt_e3,"
               + "a.add_times3 as bef_add_times3,"
               + "a.add_point3 as bef_add_point3,"
               + "a.add_amt_s4 as bef_add_amt_s4,"
               + "a.add_amt_e4 as bef_add_amt_e4,"
               + "a.add_times4 as bef_add_times4,"
               + "a.add_point4 as bef_add_point4,"
               + "a.add_amt_s5 as bef_add_amt_s5,"
               + "a.add_amt_e5 as bef_add_amt_e5,"
               + "a.add_times5 as bef_add_times5,"
               + "a.add_point5 as bef_add_point5,"
               + "a.add_amt_s6 as bef_add_amt_s6,"
               + "a.add_amt_e6 as bef_add_amt_e6,"
               + "a.add_times6 as bef_add_times6,"
               + "a.add_point6 as bef_add_point6,"
               + "a.add_amt_s7 as bef_add_amt_s7,"
               + "a.add_amt_e7 as bef_add_amt_e7,"
               + "a.add_times7 as bef_add_times7,"
               + "a.add_point7 as bef_add_point7,"
               + "a.add_amt_s8 as bef_add_amt_s8,"
               + "a.add_amt_e8 as bef_add_amt_e8,"
               + "a.add_times8 as bef_add_times8,"
               + "a.add_point8 as bef_add_point8,"
               + "a.add_amt_s9 as bef_add_amt_s9,"
               + "a.add_amt_e9 as bef_add_amt_e9,"
               + "a.add_times9 as bef_add_times9,"
               + "a.add_point9 as bef_add_point9,"
               + "a.add_amt_s10 as bef_add_amt_s10,"
               + "a.add_amt_e10 as bef_add_amt_e10,"
               + "a.add_times10 as bef_add_times10,"
               + "a.add_point10 as bef_add_point10,"
               + "a.doorsill_flag as bef_doorsill_flag,"
               + "a.d_group_card_sel as bef_d_group_card_sel,"
               + "a.d_merchant_sel as bef_d_merchant_sel,"
               + "a.d_mcht_group_sel as bef_d_mcht_group_sel,"
               + "a.platform2_kind_sel as bef_platform2_kind_sel,"
               + "a.d_mcc_code_sel as bef_d_mcc_code_sel,"
               + "a.d_card_type_sel as bef_d_card_type_sel,"
               + "a.d_bl_cond as bef_d_bl_cond,"
               + "a.d_ca_cond as bef_d_ca_cond,"
               + "a.d_it_cond as bef_d_it_cond,"
               + "a.d_it_flag as bef_d_it_flag,"
               + "a.d_id_cond as bef_d_id_cond,"
               + "a.d_ao_cond as bef_d_ao_cond,"
               + "a.d_ot_cond as bef_d_ot_cond,"
               + "a.d_bill_type_sel as bef_d_bill_type_sel,"
               + "a.d_currency_sel as bef_d_currency_sel,"
               + "a.d_pos_entry_sel as bef_d_pos_entry_sel,"
               + "a.d_ucaf_sel as bef_d_ucaf_sel,"
               + "a.d_eci_sel as bef_d_eci_sel,"
               + "a.d_tax_flag as bef_d_tax_flag,"
               + "a.d_add_item_flag as bef_d_add_item_flag,"
               + "a.d_add_item_amt as bef_d_add_item_amt,"
               + "a.d_add_amt_s1 as bef_d_add_amt_s1,"
               + "a.d_add_amt_e1 as bef_d_add_amt_e1,"
               + "a.d_add_point1 as bef_d_add_point1,"
               + "a.d_add_amt_s2 as bef_d_add_amt_s2,"
               + "a.d_add_amt_e2 as bef_d_add_amt_e2,"
               + "a.d_add_point2 as bef_d_add_point2,"
               + "a.d_add_amt_s3 as bef_d_add_amt_s3,"
               + "a.d_add_amt_e3 as bef_d_add_amt_e3,"
               + "a.d_add_point3 as bef_d_add_point3,"
               + "a.d_add_amt_s4 as bef_d_add_amt_s4,"
               + "a.d_add_amt_e4 as bef_d_add_amt_e4,"
               + "a.d_add_point4 as bef_d_add_point4,"
               + "a.d_add_amt_s5 as bef_d_add_amt_s5,"
               + "a.d_add_amt_e5 as bef_d_add_amt_e5,"
               + "a.d_add_point5 as bef_d_add_point5,"
               + "a.d_add_amt_s6 as bef_d_add_amt_s6,"
               + "a.d_add_amt_e6 as bef_d_add_amt_e6,"
               + "a.d_add_point6 as bef_d_add_point6,"
               + "a.d_add_amt_s7 as bef_d_add_amt_s7,"
               + "a.d_add_amt_e7 as bef_d_add_amt_e7,"
               + "a.d_add_point7 as bef_d_add_point7,"
               + "a.d_add_amt_s8 as bef_d_add_amt_s8,"
               + "a.d_add_amt_e8 as bef_d_add_amt_e8,"
               + "a.d_add_point8 as bef_d_add_point8,"
               + "a.d_add_amt_s9 as bef_d_add_amt_s9,"
               + "a.d_add_amt_e9 as bef_d_add_amt_e9,"
               + "a.d_add_point9 as bef_d_add_point9,"
               + "a.d_add_amt_s10 as bef_d_add_amt_s10,"
               + "a.d_add_amt_e10 as bef_d_add_amt_e10,"
               + "a.d_add_point10 as bef_d_add_point10,"
               + "a.crt_date as bef_crt_date";

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
  wp.colSet("control_tab_name",controlTabName); 

  if (wp.respHtml.indexOf("_detl") > 0) 
     wp.colSet("btnStore_disable","");   
  commCrtUser("comm_crt_user");
  commBonusType("comm_bonus_type");
  commListCond("comm_list_cond");
  commRunTimeType("comm_run_time_type");
  commAcctTypeSel("comm_acct_type_sel");
  commGroupCardSel("comm_group_card_sel");
  commMerchantSel("comm_merchant_sel");
  commMchtGruopSel("comm_mcht_group_sel");
  commMchtGruopSel("comm_platform_kind_sel");
  commMccCodeSle("comm_mcc_code_sel");
  commItFlag("comm_it_flag");
  commBillTypeSell("comm_bill_type_sel");
  commCurrencySel("comm_currency_sel");
  commAddType("comm_add_type");
  commAddItemFlag("comm_add_item_flag");
  commDGroupCrdSel("comm_d_group_card_sel");
  commDMerchantSel("comm_d_merchant_sel");
  commDMchtGroup("comm_d_mcht_group_sel");
  commDMchtGroup("comm_platform2_kind_sel");
  commDMccCode("comm_d_mcc_code_sel");
  commDCardType("comm_d_card_type_sel");
  commDItFlag("comm_d_it_flag");
  commDBillTypeSel("comm_d_bill_type_sel");
  commDCurrencySel("comm_d_currency_sel");
  commDPosEntrySel("comm_d_pos_entry_sel");
  commDUcfaSel("comm_d_ucaf_sel");
  commDEciSel("comm_d_eci_sel");
  dAddItemFlag("comm_d_add_item_flag");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("list_cond_cnt" , listMktBpmh3List("mkt_bpmh3_list_t","mkt_bpmh3_list",wp.colStr("active_code"),""));
  wp.colSet("acct_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"1"));
  wp.colSet("group_card_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"2"));
  wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"3"));
  wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"7"));
  wp.colSet("platform_kind_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"P"));
  wp.colSet("mcc_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"4"));
  wp.colSet("bill_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"5"));
  wp.colSet("currency_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"6"));
  wp.colSet("d_group_card_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"A"));
  wp.colSet("d_merchant_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"B"));
  wp.colSet("d_mcht_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"G"));
  wp.colSet("platform2_kind_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"P2"));
  wp.colSet("d_mcc_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"C"));
  wp.colSet("d_card_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"F"));
  wp.colSet("d_bill_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"D"));
  wp.colSet("d_currency_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"E"));
  wp.colSet("d_pos_entry_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"H"));
  wp.colSet("d_ucaf_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"I"));
  wp.colSet("d_eci_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_BPMH3",wp.colStr("active_code"),"J"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
     wp.colSet("opt_active_name","Y");

  if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
     wp.colSet("opt_bonus_type","Y");
  commBonusType("comm_bonus_type");
  commBonusType("comm_bef_bonus_type",1);

  if (!wp.colStr("tax_flag").equals(wp.colStr("bef_tax_flag")))
     wp.colSet("opt_tax_flag","Y");

  if (!wp.colStr("active_date_s").equals(wp.colStr("bef_active_date_s")))
     wp.colSet("opt_active_date_s","Y");

  if (!wp.colStr("active_date_e").equals(wp.colStr("bef_active_date_e")))
     wp.colSet("opt_active_date_e","Y");

  if (!wp.colStr("proc_date").equals(wp.colStr("bef_proc_date")))
     wp.colSet("opt_proc_date","Y");

  if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
     wp.colSet("opt_effect_months","Y");

  if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
     wp.colSet("opt_stop_flag","Y");

  if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
     wp.colSet("opt_stop_date","Y");

  if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
     wp.colSet("opt_stop_desc","Y");

  if (!wp.colStr("list_cond").equals(wp.colStr("bef_list_cond")))
     wp.colSet("opt_list_cond","Y");
  commListCond("comm_list_cond");
  commListCond("comm_bef_list_cond");

  wp.colSet("bef_list_cond_cnt" , listMktBpmh3List("mkt_bpmh3_list","mkt_bpmh3_list",wp.colStr("active_code"),""));
  if (!wp.colStr("list_cond_cnt").equals(wp.colStr("bef_list_cond_cnt")))
     wp.colSet("opt_list_cond_cnt","Y");

  if (!wp.colStr("bpmh3_file").equals(wp.colStr("bef_bpmh3_file")))
     wp.colSet("opt_bpmh3_file","Y");

  if (!wp.colStr("cond_imp_desc").equals(wp.colStr("bef_cond_imp_desc")))
     wp.colSet("opt_cond_imp_desc","Y");

  if (!wp.colStr("purch_cond").equals(wp.colStr("bef_purch_cond")))
     wp.colSet("opt_purch_cond","Y");

  if (!wp.colStr("run_start_cond").equals(wp.colStr("bef_run_start_cond")))
     wp.colSet("opt_run_start_cond","Y");

  if (!wp.colStr("vd_flag").equals(wp.colStr("bef_vd_flag")))
     wp.colSet("opt_vd_flag","Y");

  if (!wp.colStr("issue_cond").equals(wp.colStr("bef_issue_cond")))
     wp.colSet("opt_issue_cond","Y");

  if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
     wp.colSet("opt_issue_date_s","Y");

  if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
     wp.colSet("opt_issue_date_e","Y");

  if (!wp.colStr("card_re_days").equals(wp.colStr("bef_card_re_days")))
     wp.colSet("opt_card_re_days","Y");

  if (!wp.colStr("purch_s_date").equals(wp.colStr("bef_purch_s_date")))
     wp.colSet("opt_purch_s_date","Y");

  if (!wp.colStr("purch_e_date").equals(wp.colStr("bef_purch_e_date")))
     wp.colSet("opt_purch_e_date","Y");

  if (!wp.colStr("run_start_month").equals(wp.colStr("bef_run_start_month")))
     wp.colSet("opt_run_start_month","Y");

  if (!wp.colStr("run_time_mm").equals(wp.colStr("bef_run_time_mm")))
     wp.colSet("opt_run_time_mm","Y");

  if (!wp.colStr("run_time_type").equals(wp.colStr("bef_run_time_type")))
     wp.colSet("opt_run_time_type","Y");
  commRunTimeType("comm_run_time_type");
  commRunTimeType("comm_bef_run_time_type");

  if (!wp.colStr("run_time_dd").equals(wp.colStr("bef_run_time_dd")))
     wp.colSet("opt_run_time_dd","Y");

  if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
     wp.colSet("opt_acct_type_sel","Y");
  commAcctTypeSel("comm_acct_type_sel");
  commAcctTypeSel("comm_bef_acct_type_sel");

  wp.colSet("bef_acct_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"1"));
  if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
     wp.colSet("opt_acct_type_sel_cnt","Y");

  if (!wp.colStr("vd_corp_flag").equals(wp.colStr("bef_vd_corp_flag")))
     wp.colSet("opt_vd_corp_flag","Y");

  if (!wp.colStr("group_card_sel").equals(wp.colStr("bef_group_card_sel")))
     wp.colSet("opt_group_card_sel","Y");
  commGroupCardSel("comm_group_card_sel");
  commGroupCardSel("comm_bef_group_card_sel");

  wp.colSet("bef_group_card_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"2"));
  if (!wp.colStr("group_card_sel_cnt").equals(wp.colStr("bef_group_card_sel_cnt")))
     wp.colSet("opt_group_card_sel_cnt","Y");

//  if (!wp.colStr("group_oppost_cond").equals(wp.colStr("bef_group_oppost_cond")))
//     wp.colSet("opt_group_oppost_cond","Y");

  if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
     wp.colSet("opt_merchant_sel","Y");
  commMerchantSel("comm_merchant_sel");
  commMerchantSel("comm_bef_merchant_sel");

  wp.colSet("bef_merchant_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"3"));
  if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
     wp.colSet("opt_merchant_sel_cnt","Y");

  if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
     wp.colSet("opt_mcht_group_sel","Y");
  commMchtGruopSel("comm_mcht_group_sel");
  commMchtGruopSel("comm_bef_mcht_group_sel");

  wp.colSet("bef_mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"7"));
  if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
     wp.colSet("opt_mcht_group_sel_cnt","Y");

  if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
     wp.colSet("opt_platform_kind_sel","Y");
  commMchtGruopSel("comm_platform_kind_sel");
  commMchtGruopSel("comm_bef_platform_kind_sel");

  wp.colSet("bef_platform_kind_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"P"));
  if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
     wp.colSet("opt_platform_kind_sel_cnt","Y");

  if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
     wp.colSet("opt_mcc_code_sel","Y");
  commMccCodeSle("comm_mcc_code_sel");
  commMccCodeSle("comm_bef_mcc_code_sel");

  wp.colSet("bef_mcc_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"4"));
  if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
     wp.colSet("opt_mcc_code_sel_cnt","Y");

  if (!wp.colStr("per_point_amt").equals(wp.colStr("bef_per_point_amt")))
     wp.colSet("opt_per_point_amt","Y");

  if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
     wp.colSet("opt_bl_cond","Y");

  if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
     wp.colSet("opt_ca_cond","Y");

  if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
     wp.colSet("opt_it_cond","Y");

  if (!wp.colStr("it_flag").equals(wp.colStr("bef_it_flag")))
     wp.colSet("opt_it_flag","Y");
  commItFlag("comm_it_flag");
  commItFlag("comm_bef_it_flag");

  if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
     wp.colSet("opt_id_cond","Y");

  if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
     wp.colSet("opt_ao_cond","Y");

  if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
     wp.colSet("opt_ot_cond","Y");

  if (!wp.colStr("bill_type_sel").equals(wp.colStr("bef_bill_type_sel")))
     wp.colSet("opt_bill_type_sel","Y");
  commBillTypeSell("comm_bill_type_sel");
  commBillTypeSell("comm_bef_bill_type_sel");

  wp.colSet("bef_bill_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"5"));
  if (!wp.colStr("bill_type_sel_cnt").equals(wp.colStr("bef_bill_type_sel_cnt")))
     wp.colSet("opt_bill_type_sel_cnt","Y");

  if (!wp.colStr("currency_sel").equals(wp.colStr("bef_currency_sel")))
     wp.colSet("opt_currency_sel","Y");
  commCurrencySel("comm_currency_sel");
  commCurrencySel("comm_bef_currency_sel");

  wp.colSet("bef_currency_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"6"));
  if (!wp.colStr("currency_sel_cnt").equals(wp.colStr("bef_currency_sel_cnt")))
     wp.colSet("opt_currency_sel_cnt","Y");

  if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
     wp.colSet("opt_feedback_lmt","Y");

  if (!wp.colStr("add_type").equals(wp.colStr("bef_add_type")))
     wp.colSet("opt_add_type","Y");
  commAddType("comm_add_type");
  commAddType("comm_bef_add_type");

  if (!wp.colStr("add_item_flag").equals(wp.colStr("bef_add_item_flag")))
     wp.colSet("opt_add_item_flag","Y");
  commAddItemFlag("comm_add_item_flag");
  commAddItemFlag("comm_bef_add_item_flag");

  if (!wp.colStr("add_item_amt").equals(wp.colStr("bef_add_item_amt")))
     wp.colSet("opt_add_item_amt","Y");

  if (!wp.colStr("add_amt_s1").equals(wp.colStr("bef_add_amt_s1")))
     wp.colSet("opt_add_amt_s1","Y");

  if (!wp.colStr("add_amt_e1").equals(wp.colStr("bef_add_amt_e1")))
     wp.colSet("opt_add_amt_e1","Y");

  if (!wp.colStr("add_times1").equals(wp.colStr("bef_add_times1")))
     wp.colSet("opt_add_times1","Y");

  if (!wp.colStr("add_point1").equals(wp.colStr("bef_add_point1")))
     wp.colSet("opt_add_point1","Y");

  if (!wp.colStr("add_amt_s2").equals(wp.colStr("bef_add_amt_s2")))
     wp.colSet("opt_add_amt_s2","Y");

  if (!wp.colStr("add_amt_e2").equals(wp.colStr("bef_add_amt_e2")))
     wp.colSet("opt_add_amt_e2","Y");

  if (!wp.colStr("add_times2").equals(wp.colStr("bef_add_times2")))
     wp.colSet("opt_add_times2","Y");

  if (!wp.colStr("add_point2").equals(wp.colStr("bef_add_point2")))
     wp.colSet("opt_add_point2","Y");

  if (!wp.colStr("add_amt_s3").equals(wp.colStr("bef_add_amt_s3")))
     wp.colSet("opt_add_amt_s3","Y");

  if (!wp.colStr("add_amt_e3").equals(wp.colStr("bef_add_amt_e3")))
     wp.colSet("opt_add_amt_e3","Y");

  if (!wp.colStr("add_times3").equals(wp.colStr("bef_add_times3")))
     wp.colSet("opt_add_times3","Y");

  if (!wp.colStr("add_point3").equals(wp.colStr("bef_add_point3")))
     wp.colSet("opt_add_point3","Y");

  if (!wp.colStr("add_amt_s4").equals(wp.colStr("bef_add_amt_s4")))
     wp.colSet("opt_add_amt_s4","Y");

  if (!wp.colStr("add_amt_e4").equals(wp.colStr("bef_add_amt_e4")))
     wp.colSet("opt_add_amt_e4","Y");

  if (!wp.colStr("add_times4").equals(wp.colStr("bef_add_times4")))
     wp.colSet("opt_add_times4","Y");

  if (!wp.colStr("add_point4").equals(wp.colStr("bef_add_point4")))
     wp.colSet("opt_add_point4","Y");

  if (!wp.colStr("add_amt_s5").equals(wp.colStr("bef_add_amt_s5")))
     wp.colSet("opt_add_amt_s5","Y");

  if (!wp.colStr("add_amt_e5").equals(wp.colStr("bef_add_amt_e5")))
     wp.colSet("opt_add_amt_e5","Y");

  if (!wp.colStr("add_times5").equals(wp.colStr("bef_add_times5")))
     wp.colSet("opt_add_times5","Y");

  if (!wp.colStr("add_point5").equals(wp.colStr("bef_add_point5")))
     wp.colSet("opt_add_point5","Y");

  if (!wp.colStr("add_amt_s6").equals(wp.colStr("bef_add_amt_s6")))
     wp.colSet("opt_add_amt_s6","Y");

  if (!wp.colStr("add_amt_e6").equals(wp.colStr("bef_add_amt_e6")))
     wp.colSet("opt_add_amt_e6","Y");

  if (!wp.colStr("add_times6").equals(wp.colStr("bef_add_times6")))
     wp.colSet("opt_add_times6","Y");

  if (!wp.colStr("add_point6").equals(wp.colStr("bef_add_point6")))
     wp.colSet("opt_add_point6","Y");

  if (!wp.colStr("add_amt_s7").equals(wp.colStr("bef_add_amt_s7")))
     wp.colSet("opt_add_amt_s7","Y");

  if (!wp.colStr("add_amt_e7").equals(wp.colStr("bef_add_amt_e7")))
     wp.colSet("opt_add_amt_e7","Y");

  if (!wp.colStr("add_times7").equals(wp.colStr("bef_add_times7")))
     wp.colSet("opt_add_times7","Y");

  if (!wp.colStr("add_point7").equals(wp.colStr("bef_add_point7")))
     wp.colSet("opt_add_point7","Y");

  if (!wp.colStr("add_amt_s8").equals(wp.colStr("bef_add_amt_s8")))
     wp.colSet("opt_add_amt_s8","Y");

  if (!wp.colStr("add_amt_e8").equals(wp.colStr("bef_add_amt_e8")))
     wp.colSet("opt_add_amt_e8","Y");

  if (!wp.colStr("add_times8").equals(wp.colStr("bef_add_times8")))
     wp.colSet("opt_add_times8","Y");

  if (!wp.colStr("add_point8").equals(wp.colStr("bef_add_point8")))
     wp.colSet("opt_add_point8","Y");

  if (!wp.colStr("add_amt_s9").equals(wp.colStr("bef_add_amt_s9")))
     wp.colSet("opt_add_amt_s9","Y");

  if (!wp.colStr("add_amt_e9").equals(wp.colStr("bef_add_amt_e9")))
     wp.colSet("opt_add_amt_e9","Y");

  if (!wp.colStr("add_times9").equals(wp.colStr("bef_add_times9")))
     wp.colSet("opt_add_times9","Y");

  if (!wp.colStr("add_point9").equals(wp.colStr("bef_add_point9")))
     wp.colSet("opt_add_point9","Y");

  if (!wp.colStr("add_amt_s10").equals(wp.colStr("bef_add_amt_s10")))
     wp.colSet("opt_add_amt_s10","Y");

  if (!wp.colStr("add_amt_e10").equals(wp.colStr("bef_add_amt_e10")))
     wp.colSet("opt_add_amt_e10","Y");

  if (!wp.colStr("add_times10").equals(wp.colStr("bef_add_times10")))
     wp.colSet("opt_add_times10","Y");

  if (!wp.colStr("add_point10").equals(wp.colStr("bef_add_point10")))
     wp.colSet("opt_add_point10","Y");

  if (!wp.colStr("doorsill_flag").equals(wp.colStr("bef_doorsill_flag")))
     wp.colSet("opt_doorsill_flag","Y");

  if (!wp.colStr("d_group_card_sel").equals(wp.colStr("bef_d_group_card_sel")))
     wp.colSet("opt_d_group_card_sel","Y");
  commDGroupCrdSel("comm_d_group_card_sel");
  commDGroupCrdSel("comm_bef_d_group_card_sel");

  wp.colSet("bef_d_group_card_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"A"));
  if (!wp.colStr("d_group_card_sel_cnt").equals(wp.colStr("bef_d_group_card_sel_cnt")))
     wp.colSet("opt_d_group_card_sel_cnt","Y");

  if (!wp.colStr("d_merchant_sel").equals(wp.colStr("bef_d_merchant_sel")))
     wp.colSet("opt_d_merchant_sel","Y");
  commDMerchantSel("comm_d_merchant_sel");
  commDMerchantSel("comm_bef_d_merchant_sel");

  wp.colSet("bef_d_merchant_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"B"));
  if (!wp.colStr("d_merchant_sel_cnt").equals(wp.colStr("bef_d_merchant_sel_cnt")))
     wp.colSet("opt_d_merchant_sel_cnt","Y");

  if (!wp.colStr("d_mcht_group_sel").equals(wp.colStr("bef_d_mcht_group_sel")))
     wp.colSet("opt_d_mcht_group_sel","Y");
  commDMchtGroup("comm_d_mcht_group_sel");
  commDMchtGroup("comm_bef_d_mcht_group_sel");

  wp.colSet("bef_d_mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"G"));
  if (!wp.colStr("d_mcht_group_sel_cnt").equals(wp.colStr("bef_d_mcht_group_sel_cnt")))
     wp.colSet("opt_d_mcht_group_sel_cnt","Y");

  if (!wp.colStr("platform2_kind_sel").equals(wp.colStr("bef_platform2_kind_sel")))
     wp.colSet("opt_platform2_kind_sel","Y");
  commDMchtGroup("comm_platform2_kind_sel");
  commDMchtGroup("comm_bef_platform2_kind_sel");

  wp.colSet("bef_platform2_kind_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"P2"));
  if (!wp.colStr("platform2_kind_sel_cnt").equals(wp.colStr("bef_platform2_kind_sel_cnt")))
     wp.colSet("opt_platform2_kind_sel_cnt","Y");

  if (!wp.colStr("d_mcc_code_sel").equals(wp.colStr("bef_d_mcc_code_sel")))
     wp.colSet("opt_d_mcc_code_sel","Y");
  commDMccCode("comm_d_mcc_code_sel");
  commDMccCode("comm_bef_d_mcc_code_sel");

  wp.colSet("bef_d_mcc_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"C"));
  if (!wp.colStr("d_mcc_code_sel_cnt").equals(wp.colStr("bef_d_mcc_code_sel_cnt")))
     wp.colSet("opt_d_mcc_code_sel_cnt","Y");

  if (!wp.colStr("d_card_type_sel").equals(wp.colStr("bef_d_card_type_sel")))
     wp.colSet("opt_d_card_type_sel","Y");
  commDCardType("comm_d_card_type_sel");
  commDCardType("comm_bef_d_card_type_sel");

  wp.colSet("bef_d_card_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"F"));
  if (!wp.colStr("d_card_type_sel_cnt").equals(wp.colStr("bef_d_card_type_sel_cnt")))
     wp.colSet("opt_d_card_type_sel_cnt","Y");

  if (!wp.colStr("d_bl_cond").equals(wp.colStr("bef_d_bl_cond")))
     wp.colSet("opt_d_bl_cond","Y");

  if (!wp.colStr("d_ca_cond").equals(wp.colStr("bef_d_ca_cond")))
     wp.colSet("opt_d_ca_cond","Y");

  if (!wp.colStr("d_it_cond").equals(wp.colStr("bef_d_it_cond")))
     wp.colSet("opt_d_it_cond","Y");

  if (!wp.colStr("d_it_flag").equals(wp.colStr("bef_d_it_flag")))
     wp.colSet("opt_d_it_flag","Y");
  commDItFlag("comm_d_it_flag");
  commDItFlag("comm_bef_d_it_flag");

  if (!wp.colStr("d_id_cond").equals(wp.colStr("bef_d_id_cond")))
     wp.colSet("opt_d_id_cond","Y");

  if (!wp.colStr("d_ao_cond").equals(wp.colStr("bef_d_ao_cond")))
     wp.colSet("opt_d_ao_cond","Y");

  if (!wp.colStr("d_ot_cond").equals(wp.colStr("bef_d_ot_cond")))
     wp.colSet("opt_d_ot_cond","Y");

  if (!wp.colStr("d_bill_type_sel").equals(wp.colStr("bef_d_bill_type_sel")))
     wp.colSet("opt_d_bill_type_sel","Y");
  commDBillTypeSel("comm_d_bill_type_sel");
  commDBillTypeSel("comm_bef_d_bill_type_sel");

  wp.colSet("bef_d_bill_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"D"));
  if (!wp.colStr("d_bill_type_sel_cnt").equals(wp.colStr("bef_d_bill_type_sel_cnt")))
     wp.colSet("opt_d_bill_type_sel_cnt","Y");

  if (!wp.colStr("d_currency_sel").equals(wp.colStr("bef_d_currency_sel")))
     wp.colSet("opt_d_currency_sel","Y");
  commDCurrencySel("comm_d_currency_sel");
  commDCurrencySel("comm_bef_d_currency_sel");

  wp.colSet("bef_d_currency_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"E"));
  if (!wp.colStr("d_currency_sel_cnt").equals(wp.colStr("bef_d_currency_sel_cnt")))
     wp.colSet("opt_d_currency_sel_cnt","Y");

  if (!wp.colStr("d_pos_entry_sel").equals(wp.colStr("bef_d_pos_entry_sel")))
     wp.colSet("opt_d_pos_entry_sel","Y");
  commDPosEntrySel("comm_d_pos_entry_sel");
  commDPosEntrySel("comm_bef_d_pos_entry_sel");

  wp.colSet("bef_d_pos_entry_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"H"));
  if (!wp.colStr("d_pos_entry_sel_cnt").equals(wp.colStr("bef_d_pos_entry_sel_cnt")))
     wp.colSet("opt_d_pos_entry_sel_cnt","Y");

  if (!wp.colStr("d_ucaf_sel").equals(wp.colStr("bef_d_ucaf_sel")))
     wp.colSet("opt_d_ucaf_sel","Y");
  commDUcfaSel("comm_d_ucaf_sel");
  commDUcfaSel("comm_bef_d_ucaf_sel");

  wp.colSet("bef_d_ucaf_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"I"));
  if (!wp.colStr("d_ucaf_sel_cnt").equals(wp.colStr("bef_d_ucaf_sel_cnt")))
     wp.colSet("opt_d_ucaf_sel_cnt","Y");

  if (!wp.colStr("d_eci_sel").equals(wp.colStr("bef_d_eci_sel")))
     wp.colSet("opt_d_eci_sel","Y");
  commDEciSel("comm_d_eci_sel");
  commDEciSel("comm_bef_d_eci_sel");

  wp.colSet("bef_d_eci_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"J"));
  if (!wp.colStr("d_eci_sel_cnt").equals(wp.colStr("bef_d_eci_sel_cnt")))
     wp.colSet("opt_d_eci_sel_cnt","Y");

  if (!wp.colStr("d_tax_flag").equals(wp.colStr("bef_d_tax_flag")))
     wp.colSet("opt_d_tax_flag","Y");

  if (!wp.colStr("d_add_item_flag").equals(wp.colStr("bef_d_add_item_flag")))
     wp.colSet("opt_d_add_item_flag","Y");
  dAddItemFlag("comm_d_add_item_flag");
  dAddItemFlag("comm_bef_d_add_item_flag");

  if (!wp.colStr("d_add_item_amt").equals(wp.colStr("bef_d_add_item_amt")))
     wp.colSet("opt_d_add_item_amt","Y");

  if (!wp.colStr("d_add_amt_s1").equals(wp.colStr("bef_d_add_amt_s1")))
     wp.colSet("opt_d_add_amt_s1","Y");

  if (!wp.colStr("d_add_amt_e1").equals(wp.colStr("bef_d_add_amt_e1")))
     wp.colSet("opt_d_add_amt_e1","Y");

  if (!wp.colStr("d_add_point1").equals(wp.colStr("bef_d_add_point1")))
     wp.colSet("opt_d_add_point1","Y");

  if (!wp.colStr("d_add_amt_s2").equals(wp.colStr("bef_d_add_amt_s2")))
     wp.colSet("opt_d_add_amt_s2","Y");

  if (!wp.colStr("d_add_amt_e2").equals(wp.colStr("bef_d_add_amt_e2")))
     wp.colSet("opt_d_add_amt_e2","Y");

  if (!wp.colStr("d_add_point2").equals(wp.colStr("bef_d_add_point2")))
     wp.colSet("opt_d_add_point2","Y");

  if (!wp.colStr("d_add_amt_s3").equals(wp.colStr("bef_d_add_amt_s3")))
     wp.colSet("opt_d_add_amt_s3","Y");

  if (!wp.colStr("d_add_amt_e3").equals(wp.colStr("bef_d_add_amt_e3")))
     wp.colSet("opt_d_add_amt_e3","Y");

  if (!wp.colStr("d_add_point3").equals(wp.colStr("bef_d_add_point3")))
     wp.colSet("opt_d_add_point3","Y");

  if (!wp.colStr("d_add_amt_s4").equals(wp.colStr("bef_d_add_amt_s4")))
     wp.colSet("opt_d_add_amt_s4","Y");

  if (!wp.colStr("d_add_amt_e4").equals(wp.colStr("bef_d_add_amt_e4")))
     wp.colSet("opt_d_add_amt_e4","Y");

  if (!wp.colStr("d_add_point4").equals(wp.colStr("bef_d_add_point4")))
     wp.colSet("opt_d_add_point4","Y");

  if (!wp.colStr("d_add_amt_s5").equals(wp.colStr("bef_d_add_amt_s5")))
     wp.colSet("opt_d_add_amt_s5","Y");

  if (!wp.colStr("d_add_amt_e5").equals(wp.colStr("bef_d_add_amt_e5")))
     wp.colSet("opt_d_add_amt_e5","Y");

  if (!wp.colStr("d_add_point5").equals(wp.colStr("bef_d_add_point5")))
     wp.colSet("opt_d_add_point5","Y");

  if (!wp.colStr("d_add_amt_s6").equals(wp.colStr("bef_d_add_amt_s6")))
     wp.colSet("opt_d_add_amt_s6","Y");

  if (!wp.colStr("d_add_amt_e6").equals(wp.colStr("bef_d_add_amt_e6")))
     wp.colSet("opt_d_add_amt_e6","Y");

  if (!wp.colStr("d_add_point6").equals(wp.colStr("bef_d_add_point6")))
     wp.colSet("opt_d_add_point6","Y");

  if (!wp.colStr("d_add_amt_s7").equals(wp.colStr("bef_d_add_amt_s7")))
     wp.colSet("opt_d_add_amt_s7","Y");

  if (!wp.colStr("d_add_amt_e7").equals(wp.colStr("bef_d_add_amt_e7")))
     wp.colSet("opt_d_add_amt_e7","Y");

  if (!wp.colStr("d_add_point7").equals(wp.colStr("bef_d_add_point7")))
     wp.colSet("opt_d_add_point7","Y");

  if (!wp.colStr("d_add_amt_s8").equals(wp.colStr("bef_d_add_amt_s8")))
     wp.colSet("opt_d_add_amt_s8","Y");

  if (!wp.colStr("d_add_amt_e8").equals(wp.colStr("bef_d_add_amt_e8")))
     wp.colSet("opt_d_add_amt_e8","Y");

  if (!wp.colStr("d_add_point8").equals(wp.colStr("bef_d_add_point8")))
     wp.colSet("opt_d_add_point8","Y");

  if (!wp.colStr("d_add_amt_s9").equals(wp.colStr("bef_d_add_amt_s9")))
     wp.colSet("opt_d_add_amt_s9","Y");

  if (!wp.colStr("d_add_amt_e9").equals(wp.colStr("bef_d_add_amt_e9")))
     wp.colSet("opt_d_add_amt_e9","Y");

  if (!wp.colStr("d_add_point9").equals(wp.colStr("bef_d_add_point9")))
     wp.colSet("opt_d_add_point9","Y");

  if (!wp.colStr("d_add_amt_s10").equals(wp.colStr("bef_d_add_amt_s10")))
     wp.colSet("opt_d_add_amt_s10","Y");

  if (!wp.colStr("d_add_amt_e10").equals(wp.colStr("bef_d_add_amt_e10")))
     wp.colSet("opt_d_add_amt_e10","Y");

  if (!wp.colStr("d_add_point10").equals(wp.colStr("bef_d_add_point10")))
     wp.colSet("opt_d_add_point10","Y");

  if (!wp.colStr("crt_date").equals(wp.colStr("bef_crt_date")))
     wp.colSet("opt_crt_date","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("active_name","");
       wp.colSet("bonus_type","");
       wp.colSet("tax_flag","");
       wp.colSet("active_date_s","");
       wp.colSet("active_date_e","");
       wp.colSet("proc_date","");
       wp.colSet("effect_months","");
       wp.colSet("stop_flag","");
       wp.colSet("stop_date","");
       wp.colSet("stop_desc","");
       wp.colSet("list_cond","");
       wp.colSet("list_cond_cnt","");
       wp.colSet("bpmh3_file","");
       wp.colSet("cond_imp_desc","");
       wp.colSet("purch_cond","");
       wp.colSet("run_start_cond","");
       wp.colSet("vd_flag","");
       wp.colSet("issue_cond","");
       wp.colSet("issue_date_s","");
       wp.colSet("issue_date_e","");
       wp.colSet("card_re_days","");
       wp.colSet("purch_s_date","");
       wp.colSet("purch_e_date","");
       wp.colSet("run_start_month","");
       wp.colSet("run_time_mm","");
       wp.colSet("run_time_type","");
       wp.colSet("run_time_dd","");
       wp.colSet("acct_type_sel","");
       wp.colSet("acct_type_sel_cnt","");
       wp.colSet("vd_corp_flag","");
       wp.colSet("group_card_sel","");
       wp.colSet("group_card_sel_cnt","");
//       wp.colSet("group_oppost_cond","");
       wp.colSet("merchant_sel","");
       wp.colSet("merchant_sel_cnt","");
       wp.colSet("mcht_group_sel","");
       wp.colSet("mcht_group_sel_cnt","");
       wp.colSet("platform_kind_sel","");
       wp.colSet("platform_kind_sel_cnt","");
       wp.colSet("mcc_code_sel","");
       wp.colSet("mcc_code_sel_cnt","");
       wp.colSet("per_point_amt","");
       wp.colSet("bl_cond","");
       wp.colSet("ca_cond","");
       wp.colSet("it_cond","");
       wp.colSet("it_flag","");
       wp.colSet("id_cond","");
       wp.colSet("ao_cond","");
       wp.colSet("ot_cond","");
       wp.colSet("bill_type_sel","");
       wp.colSet("bill_type_sel_cnt","");
       wp.colSet("currency_sel","");
       wp.colSet("currency_sel_cnt","");
       wp.colSet("feedback_lmt","");
       wp.colSet("add_type","");
       wp.colSet("add_item_flag","");
       wp.colSet("add_item_amt","");
       wp.colSet("add_amt_s1","");
       wp.colSet("add_amt_e1","");
       wp.colSet("add_times1","");
       wp.colSet("add_point1","");
       wp.colSet("add_amt_s2","");
       wp.colSet("add_amt_e2","");
       wp.colSet("add_times2","");
       wp.colSet("add_point2","");
       wp.colSet("add_amt_s3","");
       wp.colSet("add_amt_e3","");
       wp.colSet("add_times3","");
       wp.colSet("add_point3","");
       wp.colSet("add_amt_s4","");
       wp.colSet("add_amt_e4","");
       wp.colSet("add_times4","");
       wp.colSet("add_point4","");
       wp.colSet("add_amt_s5","");
       wp.colSet("add_amt_e5","");
       wp.colSet("add_times5","");
       wp.colSet("add_point5","");
       wp.colSet("add_amt_s6","");
       wp.colSet("add_amt_e6","");
       wp.colSet("add_times6","");
       wp.colSet("add_point6","");
       wp.colSet("add_amt_s7","");
       wp.colSet("add_amt_e7","");
       wp.colSet("add_times7","");
       wp.colSet("add_point7","");
       wp.colSet("add_amt_s8","");
       wp.colSet("add_amt_e8","");
       wp.colSet("add_times8","");
       wp.colSet("add_point8","");
       wp.colSet("add_amt_s9","");
       wp.colSet("add_amt_e9","");
       wp.colSet("add_times9","");
       wp.colSet("add_point9","");
       wp.colSet("add_amt_s10","");
       wp.colSet("add_amt_e10","");
       wp.colSet("add_times10","");
       wp.colSet("add_point10","");
       wp.colSet("doorsill_flag","");
       wp.colSet("d_group_card_sel","");
       wp.colSet("d_group_card_sel_cnt","");
       wp.colSet("d_merchant_sel","");
       wp.colSet("d_merchant_sel_cnt","");
       wp.colSet("d_mcht_group_sel","");
       wp.colSet("d_mcht_group_sel_cnt","");
       wp.colSet("platform2_kind_sel","");
       wp.colSet("platform2_kind_sel_cnt","");
       wp.colSet("d_mcc_code_sel","");
       wp.colSet("d_mcc_code_sel_cnt","");
       wp.colSet("d_card_type_sel","");
       wp.colSet("d_card_type_sel_cnt","");
       wp.colSet("d_bl_cond","");
       wp.colSet("d_ca_cond","");
       wp.colSet("d_it_cond","");
       wp.colSet("d_it_flag","");
       wp.colSet("d_id_cond","");
       wp.colSet("d_ao_cond","");
       wp.colSet("d_ot_cond","");
       wp.colSet("d_bill_type_sel","");
       wp.colSet("d_bill_type_sel_cnt","");
       wp.colSet("d_currency_sel","");
       wp.colSet("d_currency_sel_cnt","");
       wp.colSet("d_pos_entry_sel","");
       wp.colSet("d_pos_entry_sel_cnt","");
       wp.colSet("d_ucaf_sel","");
       wp.colSet("d_ucaf_sel_cnt","");
       wp.colSet("d_eci_sel","");
       wp.colSet("d_eci_sel_cnt","");
       wp.colSet("d_tax_flag","");
       wp.colSet("d_add_item_flag","");
       wp.colSet("d_add_item_amt","");
       wp.colSet("d_add_amt_s1","");
       wp.colSet("d_add_amt_e1","");
       wp.colSet("d_add_point1","");
       wp.colSet("d_add_amt_s2","");
       wp.colSet("d_add_amt_e2","");
       wp.colSet("d_add_point2","");
       wp.colSet("d_add_amt_s3","");
       wp.colSet("d_add_amt_e3","");
       wp.colSet("d_add_point3","");
       wp.colSet("d_add_amt_s4","");
       wp.colSet("d_add_amt_e4","");
       wp.colSet("d_add_point4","");
       wp.colSet("d_add_amt_s5","");
       wp.colSet("d_add_amt_e5","");
       wp.colSet("d_add_point5","");
       wp.colSet("d_add_amt_s6","");
       wp.colSet("d_add_amt_e6","");
       wp.colSet("d_add_point6","");
       wp.colSet("d_add_amt_s7","");
       wp.colSet("d_add_amt_e7","");
       wp.colSet("d_add_point7","");
       wp.colSet("d_add_amt_s8","");
       wp.colSet("d_add_amt_e8","");
       wp.colSet("d_add_point8","");
       wp.colSet("d_add_amt_s9","");
       wp.colSet("d_add_amt_e9","");
       wp.colSet("d_add_point9","");
       wp.colSet("d_add_amt_s10","");
       wp.colSet("d_add_amt_e10","");
       wp.colSet("d_add_point10","");
       wp.colSet("crt_date","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("active_name").length()==0)
     wp.colSet("opt_active_name","Y");

  if (wp.colStr("bonus_type").length()==0)
     wp.colSet("opt_bonus_type","Y");

  if (wp.colStr("tax_flag").length()==0)
     wp.colSet("opt_tax_flag","Y");

  if (wp.colStr("active_date_s").length()==0)
     wp.colSet("opt_active_date_s","Y");

  if (wp.colStr("active_date_e").length()==0)
     wp.colSet("opt_active_date_e","Y");

  if (wp.colStr("proc_date").length()==0)
     wp.colSet("opt_proc_date","Y");

  if (wp.colStr("effect_months").length()==0)
     wp.colSet("opt_effect_months","Y");

  if (wp.colStr("stop_flag").length()==0)
     wp.colSet("opt_stop_flag","Y");

  if (wp.colStr("stop_date").length()==0)
     wp.colSet("opt_stop_date","Y");

  if (wp.colStr("stop_desc").length()==0)
     wp.colSet("opt_stop_desc","Y");

  if (wp.colStr("list_cond").length()==0)
     wp.colSet("opt_list_cond","Y");


  if (wp.colStr("bpmh3_file").length()==0)
     wp.colSet("opt_bpmh3_file","Y");

  if (wp.colStr("cond_imp_desc").length()==0)
     wp.colSet("opt_cond_imp_desc","Y");

  if (wp.colStr("purch_cond").length()==0)
     wp.colSet("opt_purch_cond","Y");

  if (wp.colStr("run_start_cond").length()==0)
     wp.colSet("opt_run_start_cond","Y");

  if (wp.colStr("vd_flag").length()==0)
     wp.colSet("opt_vd_flag","Y");

  if (wp.colStr("issue_cond").length()==0)
     wp.colSet("opt_issue_cond","Y");

  if (wp.colStr("issue_date_s").length()==0)
     wp.colSet("opt_issue_date_s","Y");

  if (wp.colStr("issue_date_e").length()==0)
     wp.colSet("opt_issue_date_e","Y");

  if (wp.colStr("card_re_days").length()==0)
     wp.colSet("opt_card_re_days","Y");

  if (wp.colStr("purch_s_date").length()==0)
     wp.colSet("opt_purch_s_date","Y");

  if (wp.colStr("purch_e_date").length()==0)
     wp.colSet("opt_purch_e_date","Y");

  if (wp.colStr("run_start_month").length()==0)
     wp.colSet("opt_run_start_month","Y");

  if (wp.colStr("run_time_mm").length()==0)
     wp.colSet("opt_run_time_mm","Y");

  if (wp.colStr("run_time_type").length()==0)
     wp.colSet("opt_run_time_type","Y");

  if (wp.colStr("run_time_dd").length()==0)
     wp.colSet("opt_run_time_dd","Y");

  if (wp.colStr("acct_type_sel").length()==0)
     wp.colSet("opt_acct_type_sel","Y");


  if (wp.colStr("vd_corp_flag").length()==0)
     wp.colSet("opt_vd_corp_flag","Y");

  if (wp.colStr("group_card_sel").length()==0)
     wp.colSet("opt_group_card_sel","Y");


//  if (wp.colStr("group_oppost_cond").length()==0)
//     wp.colSet("opt_group_oppost_cond","Y");

  if (wp.colStr("merchant_sel").length()==0)
     wp.colSet("opt_merchant_sel","Y");


  if (wp.colStr("mcht_group_sel").length()==0)
     wp.colSet("opt_mcht_group_sel","Y");


  if (wp.colStr("platform_kind_sel").length()==0)
     wp.colSet("opt_platform_kind_sel","Y");


  if (wp.colStr("mcc_code_sel").length()==0)
     wp.colSet("opt_mcc_code_sel","Y");


  if (wp.colStr("per_point_amt").length()==0)
     wp.colSet("opt_per_point_amt","Y");

  if (wp.colStr("bl_cond").length()==0)
     wp.colSet("opt_bl_cond","Y");

  if (wp.colStr("ca_cond").length()==0)
     wp.colSet("opt_ca_cond","Y");

  if (wp.colStr("it_cond").length()==0)
     wp.colSet("opt_it_cond","Y");

  if (wp.colStr("it_flag").length()==0)
     wp.colSet("opt_it_flag","Y");

  if (wp.colStr("id_cond").length()==0)
     wp.colSet("opt_id_cond","Y");

  if (wp.colStr("ao_cond").length()==0)
     wp.colSet("opt_ao_cond","Y");

  if (wp.colStr("ot_cond").length()==0)
     wp.colSet("opt_ot_cond","Y");

  if (wp.colStr("bill_type_sel").length()==0)
     wp.colSet("opt_bill_type_sel","Y");


  if (wp.colStr("currency_sel").length()==0)
     wp.colSet("opt_currency_sel","Y");


  if (wp.colStr("feedback_lmt").length()==0)
     wp.colSet("opt_feedback_lmt","Y");

  if (wp.colStr("add_type").length()==0)
     wp.colSet("opt_add_type","Y");

  if (wp.colStr("add_item_flag").length()==0)
     wp.colSet("opt_add_item_flag","Y");

  if (wp.colStr("add_item_amt").length()==0)
     wp.colSet("opt_add_item_amt","Y");

  if (wp.colStr("add_amt_s1").length()==0)
     wp.colSet("opt_add_amt_s1","Y");

  if (wp.colStr("add_amt_e1").length()==0)
     wp.colSet("opt_add_amt_e1","Y");

  if (wp.colStr("add_times1").length()==0)
     wp.colSet("opt_add_times1","Y");

  if (wp.colStr("add_point1").length()==0)
     wp.colSet("opt_add_point1","Y");

  if (wp.colStr("add_amt_s2").length()==0)
     wp.colSet("opt_add_amt_s2","Y");

  if (wp.colStr("add_amt_e2").length()==0)
     wp.colSet("opt_add_amt_e2","Y");

  if (wp.colStr("add_times2").length()==0)
     wp.colSet("opt_add_times2","Y");

  if (wp.colStr("add_point2").length()==0)
     wp.colSet("opt_add_point2","Y");

  if (wp.colStr("add_amt_s3").length()==0)
     wp.colSet("opt_add_amt_s3","Y");

  if (wp.colStr("add_amt_e3").length()==0)
     wp.colSet("opt_add_amt_e3","Y");

  if (wp.colStr("add_times3").length()==0)
     wp.colSet("opt_add_times3","Y");

  if (wp.colStr("add_point3").length()==0)
     wp.colSet("opt_add_point3","Y");

  if (wp.colStr("add_amt_s4").length()==0)
     wp.colSet("opt_add_amt_s4","Y");

  if (wp.colStr("add_amt_e4").length()==0)
     wp.colSet("opt_add_amt_e4","Y");

  if (wp.colStr("add_times4").length()==0)
     wp.colSet("opt_add_times4","Y");

  if (wp.colStr("add_point4").length()==0)
     wp.colSet("opt_add_point4","Y");

  if (wp.colStr("add_amt_s5").length()==0)
     wp.colSet("opt_add_amt_s5","Y");

  if (wp.colStr("add_amt_e5").length()==0)
     wp.colSet("opt_add_amt_e5","Y");

  if (wp.colStr("add_times5").length()==0)
     wp.colSet("opt_add_times5","Y");

  if (wp.colStr("add_point5").length()==0)
     wp.colSet("opt_add_point5","Y");

  if (wp.colStr("add_amt_s6").length()==0)
     wp.colSet("opt_add_amt_s6","Y");

  if (wp.colStr("add_amt_e6").length()==0)
     wp.colSet("opt_add_amt_e6","Y");

  if (wp.colStr("add_times6").length()==0)
     wp.colSet("opt_add_times6","Y");

  if (wp.colStr("add_point6").length()==0)
     wp.colSet("opt_add_point6","Y");

  if (wp.colStr("add_amt_s7").length()==0)
     wp.colSet("opt_add_amt_s7","Y");

  if (wp.colStr("add_amt_e7").length()==0)
     wp.colSet("opt_add_amt_e7","Y");

  if (wp.colStr("add_times7").length()==0)
     wp.colSet("opt_add_times7","Y");

  if (wp.colStr("add_point7").length()==0)
     wp.colSet("opt_add_point7","Y");

  if (wp.colStr("add_amt_s8").length()==0)
     wp.colSet("opt_add_amt_s8","Y");

  if (wp.colStr("add_amt_e8").length()==0)
     wp.colSet("opt_add_amt_e8","Y");

  if (wp.colStr("add_times8").length()==0)
     wp.colSet("opt_add_times8","Y");

  if (wp.colStr("add_point8").length()==0)
     wp.colSet("opt_add_point8","Y");

  if (wp.colStr("add_amt_s9").length()==0)
     wp.colSet("opt_add_amt_s9","Y");

  if (wp.colStr("add_amt_e9").length()==0)
     wp.colSet("opt_add_amt_e9","Y");

  if (wp.colStr("add_times9").length()==0)
     wp.colSet("opt_add_times9","Y");

  if (wp.colStr("add_point9").length()==0)
     wp.colSet("opt_add_point9","Y");

  if (wp.colStr("add_amt_s10").length()==0)
     wp.colSet("opt_add_amt_s10","Y");

  if (wp.colStr("add_amt_e10").length()==0)
     wp.colSet("opt_add_amt_e10","Y");

  if (wp.colStr("add_times10").length()==0)
     wp.colSet("opt_add_times10","Y");

  if (wp.colStr("add_point10").length()==0)
     wp.colSet("opt_add_point10","Y");

  if (wp.colStr("doorsill_flag").length()==0)
     wp.colSet("opt_doorsill_flag","Y");

  if (wp.colStr("d_group_card_sel").length()==0)
     wp.colSet("opt_d_group_card_sel","Y");


  if (wp.colStr("d_merchant_sel").length()==0)
     wp.colSet("opt_d_merchant_sel","Y");


  if (wp.colStr("d_mcht_group_sel").length()==0)
     wp.colSet("opt_d_mcht_group_sel","Y");


  if (wp.colStr("platform2_kind_sel").length()==0)
     wp.colSet("opt_platform2_kind_sel","Y");


  if (wp.colStr("d_mcc_code_sel").length()==0)
     wp.colSet("opt_d_mcc_code_sel","Y");


  if (wp.colStr("d_card_type_sel").length()==0)
     wp.colSet("opt_d_card_type_sel","Y");


  if (wp.colStr("d_bl_cond").length()==0)
     wp.colSet("opt_d_bl_cond","Y");

  if (wp.colStr("d_ca_cond").length()==0)
     wp.colSet("opt_d_ca_cond","Y");

  if (wp.colStr("d_it_cond").length()==0)
     wp.colSet("opt_d_it_cond","Y");

  if (wp.colStr("d_it_flag").length()==0)
     wp.colSet("opt_d_it_flag","Y");

  if (wp.colStr("d_id_cond").length()==0)
     wp.colSet("opt_d_id_cond","Y");

  if (wp.colStr("d_ao_cond").length()==0)
     wp.colSet("opt_d_ao_cond","Y");

  if (wp.colStr("d_ot_cond").length()==0)
     wp.colSet("opt_d_ot_cond","Y");

  if (wp.colStr("d_bill_type_sel").length()==0)
     wp.colSet("opt_d_bill_type_sel","Y");


  if (wp.colStr("d_currency_sel").length()==0)
     wp.colSet("opt_d_currency_sel","Y");


  if (wp.colStr("d_pos_entry_sel").length()==0)
     wp.colSet("opt_d_pos_entry_sel","Y");


  if (wp.colStr("d_ucaf_sel").length()==0)
     wp.colSet("opt_d_ucaf_sel","Y");


  if (wp.colStr("d_eci_sel").length()==0)
     wp.colSet("opt_d_eci_sel","Y");


  if (wp.colStr("d_tax_flag").length()==0)
     wp.colSet("opt_d_tax_flag","Y");

  if (wp.colStr("d_add_item_flag").length()==0)
     wp.colSet("opt_d_add_item_flag","Y");

  if (wp.colStr("d_add_item_amt").length()==0)
     wp.colSet("opt_d_add_item_amt","Y");

  if (wp.colStr("d_add_amt_s1").length()==0)
     wp.colSet("opt_d_add_amt_s1","Y");

  if (wp.colStr("d_add_amt_e1").length()==0)
     wp.colSet("opt_d_add_amt_e1","Y");

  if (wp.colStr("d_add_point1").length()==0)
     wp.colSet("opt_d_add_point1","Y");

  if (wp.colStr("d_add_amt_s2").length()==0)
     wp.colSet("opt_d_add_amt_s2","Y");

  if (wp.colStr("d_add_amt_e2").length()==0)
     wp.colSet("opt_d_add_amt_e2","Y");

  if (wp.colStr("d_add_point2").length()==0)
     wp.colSet("opt_d_add_point2","Y");

  if (wp.colStr("d_add_amt_s3").length()==0)
     wp.colSet("opt_d_add_amt_s3","Y");

  if (wp.colStr("d_add_amt_e3").length()==0)
     wp.colSet("opt_d_add_amt_e3","Y");

  if (wp.colStr("d_add_point3").length()==0)
     wp.colSet("opt_d_add_point3","Y");

  if (wp.colStr("d_add_amt_s4").length()==0)
     wp.colSet("opt_d_add_amt_s4","Y");

  if (wp.colStr("d_add_amt_e4").length()==0)
     wp.colSet("opt_d_add_amt_e4","Y");

  if (wp.colStr("d_add_point4").length()==0)
     wp.colSet("opt_d_add_point4","Y");

  if (wp.colStr("d_add_amt_s5").length()==0)
     wp.colSet("opt_d_add_amt_s5","Y");

  if (wp.colStr("d_add_amt_e5").length()==0)
     wp.colSet("opt_d_add_amt_e5","Y");

  if (wp.colStr("d_add_point5").length()==0)
     wp.colSet("opt_d_add_point5","Y");

  if (wp.colStr("d_add_amt_s6").length()==0)
     wp.colSet("opt_d_add_amt_s6","Y");

  if (wp.colStr("d_add_amt_e6").length()==0)
     wp.colSet("opt_d_add_amt_e6","Y");

  if (wp.colStr("d_add_point6").length()==0)
     wp.colSet("opt_d_add_point6","Y");

  if (wp.colStr("d_add_amt_s7").length()==0)
     wp.colSet("opt_d_add_amt_s7","Y");

  if (wp.colStr("d_add_amt_e7").length()==0)
     wp.colSet("opt_d_add_amt_e7","Y");

  if (wp.colStr("d_add_point7").length()==0)
     wp.colSet("opt_d_add_point7","Y");

  if (wp.colStr("d_add_amt_s8").length()==0)
     wp.colSet("opt_d_add_amt_s8","Y");

  if (wp.colStr("d_add_amt_e8").length()==0)
     wp.colSet("opt_d_add_amt_e8","Y");

  if (wp.colStr("d_add_point8").length()==0)
     wp.colSet("opt_d_add_point8","Y");

  if (wp.colStr("d_add_amt_s9").length()==0)
     wp.colSet("opt_d_add_amt_s9","Y");

  if (wp.colStr("d_add_amt_e9").length()==0)
     wp.colSet("opt_d_add_amt_e9","Y");

  if (wp.colStr("d_add_point9").length()==0)
     wp.colSet("opt_d_add_point9","Y");

  if (wp.colStr("d_add_amt_s10").length()==0)
     wp.colSet("opt_d_add_amt_s10","Y");

  if (wp.colStr("d_add_amt_e10").length()==0)
     wp.colSet("opt_d_add_amt_e10","Y");

  if (wp.colStr("d_add_point10").length()==0)
     wp.colSet("opt_d_add_point10","Y");

  if (wp.colStr("crt_date").length()==0)
     wp.colSet("opt_crt_date","Y");

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
              + " and table_name  =  'MKT_BPMH3' "
                ;
   if (wp.respHtml.equals("mktp0360_acty"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktp0360_aaa1"))
      wp.whereStr  += " and data_type  = '7' ";
   if (wp.respHtml.equals("mktp0360_pmkd"))
      wp.whereStr  += " and data_type  = 'P' ";
   if (wp.respHtml.equals("mktp0360_mccd"))
      wp.whereStr  += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktp0360_acsr"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktp0360_aaa2"))
      wp.whereStr  += " and data_type  = 'G' ";
   if (wp.respHtml.equals("mktp0360_pmkd1"))
      wp.whereStr  += " and data_type  = 'P2' ";
   if (wp.respHtml.equals("mktp0360_dccd"))
      wp.whereStr  += " and data_type  = 'C' ";
   if (wp.respHtml.equals("mktp0360_dype"))
      wp.whereStr  += " and data_type  = 'F' ";
   if (wp.respHtml.equals("mktp0360_desr"))
      wp.whereStr  += " and data_type  = 'D' ";
   if (wp.respHtml.equals("mktp0360_pose"))
      wp.whereStr  += " and data_type  = 'H' ";
   if (wp.respHtml.equals("mktp0360_ucaf"))
      wp.whereStr  += " and data_type  = 'I' ";
   if (wp.respHtml.equals("mktp0360_deci"))
      wp.whereStr  += " and data_type  = 'J' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("active_code") +  "'";
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
   if (wp.respHtml.equals("mktp0360_acty"))
    commAcctType("comm_data_code");
   if (wp.respHtml.equals("mktp0360_aaa1"))
    commMechtGroup("comm_data_code");
   if (wp.respHtml.equals("mktp0360_pmkd"))
       commPlatformKind("comm_data_code");
   if (wp.respHtml.equals("mktp0360_mccd"))
    commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktp0360_acsr"))
    commBillType1("comm_data_code");
   if (wp.respHtml.equals("mktp0360_aaa2"))
    commMechtGp("comm_data_code");
   if (wp.respHtml.equals("mktp0360_pmkd1"))
       commPlatformKind("comm_data_code");
   if (wp.respHtml.equals("mktp0360_dccd"))
    commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktp0360_dype"))
    commDataCode02("comm_data_code");
   if (wp.respHtml.equals("mktp0360_pose"))
    commEntryMode("comm_data_code");
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
              + " and table_name  =  'MKT_BPMH3' "
                ;
   if (wp.respHtml.equals("mktp0360_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktp0360_mrch"))
      wp.whereStr  += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktp0360_cocq"))
      wp.whereStr  += " and data_type  = '6' ";
   if (wp.respHtml.equals("mktp0360_dpcd"))
      wp.whereStr  += " and data_type  = 'A' ";
   if (wp.respHtml.equals("mktp0360_drch"))
      wp.whereStr  += " and data_type  = 'B' ";
   if (wp.respHtml.equals("mktp0360_docq"))
      wp.whereStr  += " and data_type  = 'E' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("active_code") +  "'";
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
   if (wp.respHtml.equals("mktp0360_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0360_gpcd"))
    commCardType("comm_data_code2");
   if (wp.respHtml.equals("mktp0360_cocq"))
    commDataCode05("comm_data_code2");
   if (wp.respHtml.equals("mktp0360_dpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0360_dpcd"))
    commCardType("comm_data_code2");
   if (wp.respHtml.equals("mktp0360_docq"))
    commDataCode0e("comm_data_code2");
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
  mktp01.Mktp0360Func func =new mktp01.Mktp0360Func(wp);

  String[] lsActiveCode = wp.itemBuff("active_code");
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

     func.varsSet("active_code", lsActiveCode[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        {
        rc =func.dbInsertA4();
        if (rc==1) rc = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc==1) rc = func.dbInsertA4Bpmh3list();
        if (rc==1) rc = func.dbDeleteD4TBpmh3list();
        }
     else if (lsAudType[rr].equals("U"))
        {
        rc =func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4Bndata();
        if (rc==1) rc  = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc==1) rc  = func.dbDeleteD4Bpmh3list();
        if (rc==1) rc  = func.dbInsertA4Bpmh3list();
        if (rc==1) rc = func.dbDeleteD4TBpmh3list();
        }
     else if (lsAudType[rr].equals("D"))
        {
         rc =func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc==1) rc = func.dbDeleteD4Bpmh3list();
        if (rc==1) rc = func.dbDeleteD4TBpmh3list();
        }

     if (rc!=1) alertErr(func.getMsg());
     if (rc == 1)
        {
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
       if ((wp.respHtml.equals("mktp0360")))
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
       if ((wp.respHtml.equals("mktp0360_acty")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"vmkt_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_dpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_mccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_dccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_cocq")))
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
       if ((wp.respHtml.equals("mktp0360_docq")))
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
       if ((wp.respHtml.equals("mktp0360_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_pmkd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_platform_group"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where platform_flag='2' ");
         }
       if ((wp.respHtml.equals("mktp0360_aaa2")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_dgp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
         }
      if ((wp.respHtml.equals("mktp0360_pmkd1")))
      {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_platform_group"
                  ,"mkt_mcht_gp"
                  ,"trim(mcht_group_id)"
                  ,"trim(mcht_group_desc)"
                  ," where platform_flag='2' ");
      }
       if ((wp.respHtml.equals("mktp0360_dype")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type1"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0360_pose")))
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
 public void commBonusType(String s1) throws Exception 
 {
  commBonusType(s1,0);
  return;
 }
// ************************************************************************
 public void commBonusType(String s1,int befType) throws Exception 
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
 public void commCardType(String s1) throws Exception 
 {
  commCardType(s1,0);
  return;
 }
// ************************************************************************
 public void commCardType(String s1,int befType) throws Exception 
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
            + " and   card_type = '"+wp.colStr(ii,befStr+"data_code2")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code2").length()==0)
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
 public void commDataCode07(String s1) throws Exception 
 {
  commDataCode07(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode07(String s1,int befType) throws Exception 
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
 public void commBillType1(String s1) throws Exception 
 {
  commBillType1(s1,0);
  return;
 }
// ************************************************************************
 public void commBillType1(String s1,int befType) throws Exception 
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
 public void commDataCode05(String s1) throws Exception 
 {
  commDataCode05(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode05(String s1,int befType) throws Exception 
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
 public void commDataCode0e(String s1) throws Exception 
 {
  commDataCode0e(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode0e(String s1,int befType) throws Exception 
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
 public void commMechtGroup(String s1) throws Exception 
 {
  commMechtGroup(s1,0);
  return;
 }
// ************************************************************************
 public void commMechtGroup(String s1,int befType) throws Exception 
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
 public void commMechtGp(String s1) throws Exception 
 {
  commMechtGp(s1,0);
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
            + " where 1 = 1 "
            + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
            + " and   data_code = '' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcht_group_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
    // ************************************************************************
    public void commPlatformKind(String columnData1) throws Exception {
        String columnData = "";
        String sql1 = "";
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            columnData = "";
            sql1 = "select "
                    + " MCHT_GROUP_ID "
                    + " ,MCHT_GROUP_DESC "
                    + " from MKT_MCHT_GP "
                    + " where 1 = 1 "
                    + sqlCol(wp.colStr(ii, "data_code"), "MCHT_GROUP_ID");
            if (wp.colStr(ii, "data_code").length() == 0) {
                wp.colSet(ii, columnData1, columnData);
                continue;
            }
            sqlSelect(sql1);
            sqlParm.clear();
            if (sqlRowNum > 0) {
                columnData = columnData + sqlStr("MCHT_GROUP_DESC");
            }
            wp.colSet(ii, columnData1, columnData);
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
 public void commListCond(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"身分證號","帳戶流水號","卡號"};
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
 public void commRunTimeType(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"每Cycle執行","每月"};
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
 public void commAcctTypeSel(String s1) throws Exception 
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
 public void commGroupCardSel(String s1) throws Exception 
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
 public void commMerchantSel(String s1) throws Exception 
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
 public void commMchtGruopSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除0"};
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
 public void commMccCodeSle(String s1) throws Exception 
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
 public void commItFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"1.合約金額","2.入帳金額"};
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
 public void commBillTypeSell(String s1) throws Exception 
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
 public void commCurrencySel(String s1) throws Exception 
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
 public void commAddType(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"級距式","條件式"};
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
 public void commAddItemFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"累積金額","累積筆數"};
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
 public void commDGroupCrdSel(String s1) throws Exception 
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
 public void commDMerchantSel(String s1) throws Exception 
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
 public void commDMchtGroup(String s1) throws Exception 
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
 public void commDMccCode(String s1) throws Exception 
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
 public void commDCardType(String s1) throws Exception 
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
 public void commDItFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"1.合約金額","2.入帳金額"};
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
 public void commDBillTypeSel(String s1) throws Exception 
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
 public void commDCurrencySel(String s1) throws Exception 
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
 public void commDPosEntrySel(String s1) throws Exception 
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
 public void commDUcfaSel(String s1) throws Exception 
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
 public void commDEciSel(String s1) throws Exception 
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
 public void dAddItemFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"累積金額","累積筆數"};
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
 String  listMktBnData(String s1,String s2,String s3,String s4) throws Exception
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
          + " from sec_user a,mkt_bpmh3_t b "
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
          + " from mkt_bpmh3_t b "
          + " where   b.apr_flag = 'N' "
          + " group by b.active_code "
          ;

   return lsSql;
 }
// ************************************************************************
 String  listMktBpmh3List(String s1,String s2,String s3,String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where  active_code = '" + s3 + "' "
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }

// ************************************************************************

}  // End of class
