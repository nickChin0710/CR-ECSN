/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/02  V1.00.02   Allen Ho      Initial                              *
* 111/12/07  V1.00.03  Machao    sync from mega & updated for project coding standard   
* 111/12/16  V1.00.04   Machao        命名规则调整后测试修改                                                                         *
* 112/04/06  V1.00.05   JiangYingdong        program update                *
***************************************************************************/
package mktm01;

import mktm01.Mktm0360Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0360 extends BaseEdit
{
 private final String PROGNAME = "紅利特惠(五)-特店刷卡加贈點數參數維護處理程式111/12/16  V1.00.04";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0360Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_bpmh3";
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

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr2("ex_crt_user"), "a.crt_user", "like%")
              + sqlCol(wp.itemStr2("ex_crt_date"), "a.crt_date", "like%")
              + sqlChkEx(wp.itemStr2("ex_active_name"), "1", "")
              + sqlCol(wp.itemStr2("ex_active_code"), "a.active_code", "like%")
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
               + "a.active_code,"
               + "a.active_name,"
               + "a.proc_date,"
               + "a.active_date_s,"
               + "a.stop_flag,"
               + "a.crt_user,"
               + "a.apr_user,"
               + "a.apr_date";

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
      return;
     }

  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");


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
               + "a.active_code as active_code,"
               + "a.apr_flag,"
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
               + "a.run_start_cond,"
               + "a.run_start_month,"
               + "a.run_time_mm,"
               + "a.run_time_type,"
               + "a.run_time_dd,"
               + "a.per_point_amt,"
               + "a.feedback_lmt,"
               + "a.list_cond,"
               + "'' as list_cond_cnt,"
               + "a.vd_flag,"
               + "a.acct_type_sel,"
               + "'' as acct_type_sel_cnt,"
               + "a.vd_corp_flag,"
               + "a.issue_cond,"
               + "a.issue_date_s,"
               + "a.issue_date_e,"
               + "a.card_re_days,"
               + "a.purch_cond,"
               + "a.purch_s_date,"
               + "a.purch_e_date,"
               + "a.group_card_sel,"
               + "'' as group_card_sel_cnt,"
//               + "a.group_oppost_cond,"
               + "a.merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.platform_kind_sel,"
               + "'' as platform_kind_sel_cnt,"
               + "a.mcc_code_sel,"
               + "'' as mcc_code_sel_cnt,"
               + "a.bl_cond,"
               + "a.ca_cond,"
               + "a.it_cond,"
               + "a.it_flag,"
               + "a.id_cond,"
               + "a.ao_cond,"
               + "a.ot_cond,"
               + "a.bill_type_sel,"
               + "'' as bill_type_sel_cnt,"
               + "a.currency_sel,"
               + "'' as currency_sel_cnt,"
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
               + "'' as d_group_card_sel_cnt,"
               + "a.d_merchant_sel,"
               + "'' as d_merchant_sel_cnt,"
               + "a.d_mcht_group_sel,"
               + "'' as d_mcht_group_sel_cnt,"
               + "a.platform2_kind_sel,"
               + "'' as platform2_kind_sel_cnt,"
               + "a.d_mcc_code_sel,"
               + "'' as d_mcc_code_sel_cnt,"
               + "a.d_card_type_sel,"
               + "'' as d_card_type_sel_cnt,"
               + "a.d_bl_cond,"
               + "a.d_ca_cond,"
               + "a.d_it_cond,"
               + "a.d_it_flag,"
               + "a.d_id_cond,"
               + "a.d_ao_cond,"
               + "a.d_ot_cond,"
               + "a.d_bill_type_sel,"
               + "'' as d_bill_type_sel_cnt,"
               + "a.d_currency_sel,"
               + "'' as d_currency_sel_cnt,"
               + "a.d_pos_entry_sel,"
               + "'' as d_pos_entry_sel_cnt,"
               + "a.d_ucaf_sel,"
               + "'' as d_ucaf_sel_cnt,"
               + "a.d_eci_sel,"
               + "'' as d_eci_sel_cnt,"
               + "a.d_add_item_flag,"
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
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user";

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
   if (qFrom==0)
      {
       wp.colSet("aud_type","Y");
      }
   else
      {
       wp.colSet("aud_type",wp.itemStr2("ex_apr_flag"));
       wp.colSet("fst_apr_flag",wp.itemStr2("ex_apr_flag"));
      }
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("active_code");
  listWkdata();
  commfuncAudType("aud_type");
  dataReadR3R();
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
  wp.colSet("list_cond_cnt" , listMktBpmh3List("mkt_bpmh3_list","mkt_bpmh3_list",wp.colStr("active_code"),""));
  wp.colSet("acct_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"1"));
  wp.colSet("group_card_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"2"));
  wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"3"));
  wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"7"));
  wp.colSet("platform_kind_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"P"));
  wp.colSet("mcc_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"4"));
  wp.colSet("bill_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"5"));
  wp.colSet("currency_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"6"));
  wp.colSet("d_group_card_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"A"));
  wp.colSet("d_merchant_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"B"));
  wp.colSet("d_mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"G"));
  wp.colSet("platform2_kind_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"P2"));
  wp.colSet("d_mcc_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"C"));
  wp.colSet("d_card_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"F"));
  wp.colSet("d_bill_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"D"));
  wp.colSet("d_currency_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"E"));
  wp.colSet("d_pos_entry_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"H"));
  wp.colSet("d_ucaf_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"I"));
  wp.colSet("d_eci_sel_cnt" , listMktBnData("mkt_bn_data","MKT_BPMH3",wp.colStr("active_code"),"J"));
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.active_code as active_code,"
               + "a.apr_flag as apr_flag,"
               + "a.active_name as active_name,"
               + "a.bonus_type as bonus_type,"
               + "a.tax_flag as tax_flag,"
               + "a.active_date_s as active_date_s,"
               + "a.active_date_e as active_date_e,"
               + "a.proc_date as proc_date,"
               + "a.effect_months as effect_months,"
               + "a.stop_flag as stop_flag,"
               + "a.stop_date as stop_date,"
               + "a.stop_desc as stop_desc,"
               + "a.run_start_cond as run_start_cond,"
               + "a.run_start_month as run_start_month,"
               + "a.run_time_mm as run_time_mm,"
               + "a.run_time_type as run_time_type,"
               + "a.run_time_dd as run_time_dd,"
               + "a.per_point_amt as per_point_amt,"
               + "a.feedback_lmt as feedback_lmt,"
               + "a.list_cond as list_cond,"
               + "'' as list_cond_cnt,"
               + "a.vd_flag as vd_flag,"
               + "a.acct_type_sel as acct_type_sel,"
               + "'' as acct_type_sel_cnt,"
               + "a.vd_corp_flag as vd_corp_flag,"
               + "a.issue_cond as issue_cond,"
               + "a.issue_date_s as issue_date_s,"
               + "a.issue_date_e as issue_date_e,"
               + "a.card_re_days as card_re_days,"
               + "a.purch_cond as purch_cond,"
               + "a.purch_s_date as purch_s_date,"
               + "a.purch_e_date as purch_e_date,"
               + "a.group_card_sel as group_card_sel,"
               + "'' as group_card_sel_cnt,"
//               + "a.group_oppost_cond as group_oppost_cond,"
               + "a.merchant_sel as merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel as mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.platform_kind_sel as platform_kind_sel,"
               + "'' as platform_kind_sel_cnt,"
               + "a.mcc_code_sel as mcc_code_sel,"
               + "'' as mcc_code_sel_cnt,"
               + "a.bl_cond as bl_cond,"
               + "a.ca_cond as ca_cond,"
               + "a.it_cond as it_cond,"
               + "a.it_flag as it_flag,"
               + "a.id_cond as id_cond,"
               + "a.ao_cond as ao_cond,"
               + "a.ot_cond as ot_cond,"
               + "a.bill_type_sel as bill_type_sel,"
               + "'' as bill_type_sel_cnt,"
               + "a.currency_sel as currency_sel,"
               + "'' as currency_sel_cnt,"
               + "a.add_type as add_type,"
               + "a.add_item_flag as add_item_flag,"
               + "a.add_item_amt as add_item_amt,"
               + "a.add_amt_s1 as add_amt_s1,"
               + "a.add_amt_e1 as add_amt_e1,"
               + "a.add_times1 as add_times1,"
               + "a.add_point1 as add_point1,"
               + "a.add_amt_s2 as add_amt_s2,"
               + "a.add_amt_e2 as add_amt_e2,"
               + "a.add_times2 as add_times2,"
               + "a.add_point2 as add_point2,"
               + "a.add_amt_s3 as add_amt_s3,"
               + "a.add_amt_e3 as add_amt_e3,"
               + "a.add_times3 as add_times3,"
               + "a.add_point3 as add_point3,"
               + "a.add_amt_s4 as add_amt_s4,"
               + "a.add_amt_e4 as add_amt_e4,"
               + "a.add_times4 as add_times4,"
               + "a.add_point4 as add_point4,"
               + "a.add_amt_s5 as add_amt_s5,"
               + "a.add_amt_e5 as add_amt_e5,"
               + "a.add_times5 as add_times5,"
               + "a.add_point5 as add_point5,"
               + "a.add_amt_s6 as add_amt_s6,"
               + "a.add_amt_e6 as add_amt_e6,"
               + "a.add_times6 as add_times6,"
               + "a.add_point6 as add_point6,"
               + "a.add_amt_s7 as add_amt_s7,"
               + "a.add_amt_e7 as add_amt_e7,"
               + "a.add_times7 as add_times7,"
               + "a.add_point7 as add_point7,"
               + "a.add_amt_s8 as add_amt_s8,"
               + "a.add_amt_e8 as add_amt_e8,"
               + "a.add_times8 as add_times8,"
               + "a.add_point8 as add_point8,"
               + "a.add_amt_s9 as add_amt_s9,"
               + "a.add_amt_e9 as add_amt_e9,"
               + "a.add_times9 as add_times9,"
               + "a.add_point9 as add_point9,"
               + "a.add_amt_s10 as add_amt_s10,"
               + "a.add_amt_e10 as add_amt_e10,"
               + "a.add_times10 as add_times10,"
               + "a.add_point10 as add_point10,"
               + "a.doorsill_flag as doorsill_flag,"
               + "a.d_group_card_sel as d_group_card_sel,"
               + "'' as d_group_card_sel_cnt,"
               + "a.d_merchant_sel as d_merchant_sel,"
               + "'' as d_merchant_sel_cnt,"
               + "a.d_mcht_group_sel as d_mcht_group_sel,"
               + "'' as d_mcht_group_sel_cnt,"
               + "a.platform2_kind_sel as platform2_kind_sel,"
               + "'' as platform2_kind_sel_cnt,"
               + "a.d_mcc_code_sel as d_mcc_code_sel,"
               + "'' as d_mcc_code_sel_cnt,"
               + "a.d_card_type_sel as d_card_type_sel,"
               + "'' as d_card_type_sel_cnt,"
               + "a.d_bl_cond as d_bl_cond,"
               + "a.d_ca_cond as d_ca_cond,"
               + "a.d_it_cond as d_it_cond,"
               + "a.d_it_flag as d_it_flag,"
               + "a.d_id_cond as d_id_cond,"
               + "a.d_ao_cond as d_ao_cond,"
               + "a.d_ot_cond as d_ot_cond,"
               + "a.d_bill_type_sel as d_bill_type_sel,"
               + "'' as d_bill_type_sel_cnt,"
               + "a.d_currency_sel as d_currency_sel,"
               + "'' as d_currency_sel_cnt,"
               + "a.d_pos_entry_sel as d_pos_entry_sel,"
               + "'' as d_pos_entry_sel_cnt,"
               + "a.d_ucaf_sel as d_ucaf_sel,"
               + "'' as d_ucaf_sel_cnt,"
               + "a.d_eci_sel as d_eci_sel,"
               + "'' as d_eci_sel_cnt,"
               + "a.d_add_item_flag as d_add_item_flag,"
               + "a.d_add_amt_s1 as d_add_amt_s1,"
               + "a.d_add_amt_e1 as d_add_amt_e1,"
               + "a.d_add_point1 as d_add_point1,"
               + "a.d_add_amt_s2 as d_add_amt_s2,"
               + "a.d_add_amt_e2 as d_add_amt_e2,"
               + "a.d_add_point2 as d_add_point2,"
               + "a.d_add_amt_s3 as d_add_amt_s3,"
               + "a.d_add_amt_e3 as d_add_amt_e3,"
               + "a.d_add_point3 as d_add_point3,"
               + "a.d_add_amt_s4 as d_add_amt_s4,"
               + "a.d_add_amt_e4 as d_add_amt_e4,"
               + "a.d_add_point4 as d_add_point4,"
               + "a.d_add_amt_s5 as d_add_amt_s5,"
               + "a.d_add_amt_e5 as d_add_amt_e5,"
               + "a.d_add_point5 as d_add_point5,"
               + "a.d_add_amt_s6 as d_add_amt_s6,"
               + "a.d_add_amt_e6 as d_add_amt_e6,"
               + "a.d_add_point6 as d_add_point6,"
               + "a.d_add_amt_s7 as d_add_amt_s7,"
               + "a.d_add_amt_e7 as d_add_amt_e7,"
               + "a.d_add_point7 as d_add_point7,"
               + "a.d_add_amt_s8 as d_add_amt_s8,"
               + "a.d_add_amt_e8 as d_add_amt_e8,"
               + "a.d_add_point8 as d_add_point8,"
               + "a.d_add_amt_s9 as d_add_amt_s9,"
               + "a.d_add_amt_e9 as d_add_amt_e9,"
               + "a.d_add_point9 as d_add_point9,"
               + "a.d_add_amt_s10 as d_add_amt_s10,"
               + "a.d_add_amt_e10 as d_add_amt_e10,"
               + "a.d_add_point10 as d_add_point10,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

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
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdataAft();
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("active_code");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("active_code");
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
   km1 = wp.itemStr2("active_code");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr2("active_code");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag",fstAprFlag);
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

   if ((wp.itemStr2("active_code").length()==0)||
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
       bnTable = "mkt_bpmh3_list";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_bpmh3_list_t";
      }

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "0 as r2_mod_seqno, "
                + "active_code, "
                + "mod_pgm  as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
                ;
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  active_code = :active_code ";
   setString("active_code", wp.itemStr2("active_code"));
   whereCnt += " and  active_code = '"+ wp.itemStr2("active_code") +  "'";
   wp.whereStr  += " order by 4,5 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
  }
// ************************************************************************
 public void updateFuncU4() throws Exception
 {
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

   if ((wp.itemStr2("active_code").length()==0)||
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
       bnTable = "mkt_bn_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_bn_data_t";
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
              + " and table_name  =  'MKT_BPMH3' "
                ;
   if (wp.respHtml.equals("mktm0360_acty"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktm0360_aaa1"))
      wp.whereStr  += " and data_type  = '7' ";
   if (wp.respHtml.equals("mktm0360_pmkd"))
      wp.whereStr  += " and data_type  = 'P' ";
   if (wp.respHtml.equals("mktm0360_mccd"))
      wp.whereStr  += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktm0360_acsr"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktm0360_aaa2"))
      wp.whereStr  += " and data_type  = 'G' ";
   if (wp.respHtml.equals("mktm0360_pmkd1"))
      wp.whereStr  += " and data_type  = 'P2' ";
   if (wp.respHtml.equals("mktm0360_dccd"))
      wp.whereStr  += " and data_type  = 'C' ";
   if (wp.respHtml.equals("mktm0360_dype"))
      wp.whereStr  += " and data_type  = 'F' ";
   if (wp.respHtml.equals("mktm0360_desr"))
      wp.whereStr  += " and data_type  = 'D' ";
   if (wp.respHtml.equals("mktm0360_pose"))
      wp.whereStr  += " and data_type  = 'H' ";
   if (wp.respHtml.equals("mktm0360_ucaf"))
      wp.whereStr  += " and data_type  = 'I' ";
   if (wp.respHtml.equals("mktm0360_deci"))
      wp.whereStr  += " and data_type  = 'J' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("active_code") +  "'";
   wp.whereStr  += " order by 4,5,6 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm0360_acty"))
    commAcctType("comm_data_code");
   if (wp.respHtml.equals("mktm0360_aaa1"))
    commMechtGroup("comm_data_code");
   if (wp.respHtml.equals("mktm0360_mccd"))
    commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktm0360_acsr"))
    commBillType("comm_data_code");
   if (wp.respHtml.equals("mktm0360_aaa2"))
    commMechtGp("comm_data_code");
   if (wp.respHtml.equals("mktm0360_pmkd1"))
       commPlatformKind("comm_data_code");
   if (wp.respHtml.equals("mktm0360_dccd"))
    commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktm0360_dype"))
    commDataCode02("comm_data_code");
   if (wp.respHtml.equals("mktm0360_pose"))
    commEntryMode("comm_data_code");
   if (wp.respHtml.equals("mktm0360_pmkd"))
       commPlatformKind("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);
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

   if ((wp.itemStr2("active_code").length()==0)||
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
       bnTable = "mkt_bn_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_bn_data_t";
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
              + " and table_name  =  'MKT_BPMH3' "
                ;
   if (wp.respHtml.equals("mktm0360_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktm0360_mrch"))
      wp.whereStr  += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktm0360_cocq"))
      wp.whereStr  += " and data_type  = '6' ";
   if (wp.respHtml.equals("mktm0360_dpcd"))
      wp.whereStr  += " and data_type  = 'A' ";
   if (wp.respHtml.equals("mktm0360_drch"))
      wp.whereStr  += " and data_type  = 'B' ";
   if (wp.respHtml.equals("mktm0360_docq"))
      wp.whereStr  += " and data_type  = 'E' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("active_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm0360_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktm0360_gpcd"))
    commCardType("comm_data_code2");
   if (wp.respHtml.equals("mktm0360_cocq"))
    commDataCode05("comm_data_code2");
   if (wp.respHtml.equals("mktm0360_dpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktm0360_dpcd"))
    commCardType("comm_data_code2");
   if (wp.respHtml.equals("mktm0360_docq"))
    commDataCode0e("comm_data_code2");
  }
// ************************************************************************
 public void updateFuncU3() throws Exception
 {
   mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);
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
  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

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
       if ((wp.respHtml.equals("mktm0360_nadd"))||
           (wp.respHtml.equals("mktm0360_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("bonus_type").length()>0)
             {
             wp.optionKey = wp.colStr("bonus_type");
             }
          this.dddwList("dddw_bonus_type"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='BONUS_NAME'");
         }
       if ((wp.respHtml.equals("mktm0360_acty")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"vmkt_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_gpcd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_card_type"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_dpcd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_card_type"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_mccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_dccd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_code07"
                 ,"cca_mcc_risk"
                 ,"trim(mcc_code)"
                 ,"trim(mcc_remark)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_acsr")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_bill_type"
                 ,"ptr_billtype"
                 ,"trim(bill_type)"
                 ,"trim(inter_desc)"
                 ," group by bill_type,inter_desc");
         }
       if ((wp.respHtml.equals("mktm0360_cocq")))
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
       if ((wp.respHtml.equals("mktm0360_docq")))
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
       if ((wp.respHtml.equals("mktm0360_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
         }
      if ((wp.respHtml.equals("mktm0360_pmkd")))
      {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_platform_group"
                  ,"mkt_mcht_gp"
                  ,"trim(mcht_group_id)"
                  ,"trim(mcht_group_desc)"
                  ," where platform_flag='2' ");
      }
       if ((wp.respHtml.equals("mktm0360_aaa2")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_dgp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_pmkd1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_platform_group"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where platform_flag='2' ");
         }
       if ((wp.respHtml.equals("mktm0360_dype")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type1"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0360_pose")))
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
  if (sqCond.equals("1"))
      return " and active_name like '%"+wp.itemStr2("ex_active_name")+"%' ";

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
 public void commCardType(String s1) throws Exception 
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
            + " and   card_type = '"+wp.colStr(ii,"data_code2")+"'"
            ;
       if (wp.colStr(ii,"data_code2").length()==0)
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
 public void commBillType(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " inter_desc as column_inter_desc "
            + " from ptr_billtype "
            + " where 1 = 1 "
            + " and   bill_type = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
 public void commDataCode0e(String s1) throws Exception 
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
 public void commMechtGroup(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 "
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
            + " where 1 = 1 "
            + " and   mcht_group_id = '"+wp.colStr(ii,"data_code")+"'"
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
 public void wfAjaxFunc4(TarokoCommon wr) throws Exception
 {
  String ajaxjDataCode2 = "";
  super.wp = wr;


  if (selectAjaxFunc40(
                    wp.itemStr2("ax_win_data_code"))!=0) 
     {
      wp.addJSON("ajaxj_data_code2", "");
      wp.addJSON("ajaxj_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_data_code2", sqlStr(ii, "data_code2"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_name", sqlStr(ii, "name"));
 }
// ************************************************************************
 int selectAjaxFunc40(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " '' as data_code2,  "
             + " '' as name  "
             + " from  ptr_businday "
             + " union "
             + " select "
             + " b.card_type  as data_code2,"
             + " b.name "
             + " from  ptr_group_card a,ptr_card_type b "
             + " where a.card_type  = b.card_type "
             + " and   a.group_code = '"+ s1 +"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr("團體代號:["+s1+"]查無資料");
       return 1;
      }

   return 0;
 }

// ************************************************************************
 public void wfAjaxFunc5(TarokoCommon wr) throws Exception
 {
  String ajaxjDataCode2 = "";
  super.wp = wr;


  if (selectAjaxFunc50(
                    wp.itemStr2("ax_win_data_code"))!=0) 
     {
      wp.addJSON("ajaxj_data_code2", "");
      wp.addJSON("ajaxj_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_data_code2", sqlStr(ii, "data_code2"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_name", sqlStr(ii, "name"));
 }
// ************************************************************************
 int selectAjaxFunc50(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " '' as data_code2,  "
             + " '' as name  "
             + " from  ptr_businday "
             + " union "
             + " select "
             + " b.card_type  as data_code2,"
             + " b.name "
             + " from  ptr_group_card a,ptr_card_type b "
             + " where a.card_type  = b.card_type "
             + " and   a.group_code = '"+ s1 +"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr("團體代號:["+s1+"]查無資料");
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

  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

  if (sysUploadAlias.equals("list"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2List("MKT_BPMH3_LIST_T");
      if (!Arrays.asList("1","2","3").contains(wp.itemStr2("list_cond")))
         {
          alertErr("指定對像未選擇, 上傳不可執行");
          return;
         }
     }
  if (sysUploadAlias.equals("aaa1"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
     }
  if (sysUploadAlias.equals("pmkd"))
     {
      // if has pre check procudure, write in here
      func.dbDeleteD2Pmkd("MKT_BN_DATA_T");
     }
  if (sysUploadAlias.equals("aaa2"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Aaa2("MKT_BN_DATA_T");
     }
  if (sysUploadAlias.equals("pmkd1"))
     {
      // if has pre check procudure, write in here
      func.dbDeleteD2Pmkd1("MKT_BN_DATA_T");
     }

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
    if (sysUploadAlias.equals("list"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("aaa1"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("pmkd"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("aaa2"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("pmkd1"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }

    llCnt++; 

    for (int inti=0;inti<10;inti++) logMsg[inti]="";
    logMsg[10]=String.format("%02d",lineCnt);

    if (sysUploadAlias.equals("list"))
       if (checkUploadfileList(ss)!=0) continue;
    if (sysUploadAlias.equals("aaa1"))
       if (checkUploadfileAaa1(ss)!=0) continue;
    if (sysUploadAlias.equals("pmkd"))
       if (checkUploadfilePmkd(ss)!=0) continue;
    if (sysUploadAlias.equals("aaa2"))
       if (checkUploadfileAaa2(ss)!=0) continue;
    if (sysUploadAlias.equals("pmkd1"))
       if (checkUploadfilePmkd1(ss)!=0) continue;
   llOk++;

   if (notifyCnt==0)
      {
       if (sysUploadAlias.equals("list"))
          {
           if (func.dbInsertI2List("MKT_BPMH3_LIST_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
       if (sysUploadAlias.equals("aaa1"))
          {
           if (func.dbInsertI2Aaa1("MKT_BN_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
       if (sysUploadAlias.equals("aaa2"))
          {
           if (func.dbInsertI2Aaa2("MKT_BN_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
      }
   }

  if (llErr!=0) notifyCnt=1;
  if (notifyCnt==1)
     {
      if (sysUploadAlias.equals("list"))
         func.dbDeleteD2List("MKT_BPMH3_LIST_T");
      if (sysUploadAlias.equals("aaa1"))
         func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
      if (sysUploadAlias.equals("aaa2"))
         func.dbDeleteD2Aaa2("MKT_BN_DATA_T");
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
int  checkUploadfileList(String ss) throws Exception
 {
  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "data_code";

  // ========  [I]nsert table column  ========
  uploadFileCol[1]  = "active_code";
  uploadFileCol[2]  = "list_cond";
  uploadFileCol[3]  = "vd_flag";
  uploadFileCol[4]  = "acct_type";
  uploadFileCol[5]  = "id_no";
  uploadFileCol[6]  = "p_seqno";

  // ==== insert table content default =====
  uploadFileDat[1]  = wp.itemStr2("ACTIVE_CODE");
  uploadFileDat[2]  = wp.itemStr2("list_cond");

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<1;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);
  //******************************************************************
/*
  if (wp.item_ss("list_cond").equals("1"))
     {
      uploadFileDat[5] = uploadFileDat[0];

      if (select_act_acno_id()!=0)
         if (select_dba_acno_id()!=0)
         {
          error_cnt++;
          logMsg[0]               = "資料檢核錯誤";
          logMsg[1]               = "1";
          logMsg[2]               = "1";
          logMsg[3]               = "身份檢核";
          logMsg[4]               = "無此 ID";
          logMsg[5]               = "帳戶檔無此 ID 資料";
          func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
          return(0);
         }
     }
*/
  //******************************************************************
  if (wp.itemStr2("list_cond").equals("2"))
     {
      uploadFileDat[6] = uploadFileDat[0];
      uploadFileDat[3] = wp.itemStr2("VD_FLAG");

      if (wp.itemStr2("VD_FLAG").equals("Y"))
         {
          if (selectDbaAcnoP()!=0)
             {
              errorCnt++;
              logMsg[0]               = "資料內容錯誤";         // 原因說明
              logMsg[1]               = "1";                    // 錯誤類別
              logMsg[2]               = "1";                    // 欄位位置
              logMsg[3]               = "("+uploadFileDat[0];   // 欄位內容
              logMsg[4]               = "VD卡無此帳戶";          // 錯誤說明
              logMsg[5]               = "帳戶檔無此 P_SEQNO 資料";     // 欄位說明

              func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
              return(0);
             }
         }
      else 
         {
          if (selectActAcnoP()!=0)
             {
              errorCnt++;
              logMsg[0]               = "資料內容錯誤";         // 原因說明
              logMsg[1]               = "1";                    // 錯誤類別
              logMsg[2]               = "1";                    // 欄位位置
              logMsg[3]               = "("+uploadFileDat[0];   // 欄位內容
              logMsg[4]               = "一般卡無此帳戶";          // 錯誤說明
              logMsg[5]               = "帳戶檔無此 P_SEQNO 資料";     // 欄位說明

              func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
              return(0);
             }
         }
     }
  //******************************************************************
  if (wp.itemStr2("list_cond").equals("3"))
     {
      if (selectCrdCard()!=0)
         if (selectDbcCard()!=0)
         {
          errorCnt++;
          logMsg[0]               = "資料內容錯誤";         // 原因說明
          logMsg[1]               = "1";                    // 錯誤類別
          logMsg[2]               = "1";                    // 欄位位置
          logMsg[3]               = "("+uploadFileDat[0];   // 欄位內容
          logMsg[4]               = "無此卡號";          // 錯誤說明
          logMsg[5]               = "卡檔無此卡號資料";     // 欄位說明

          func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
          return(0);
         }
     }

  return 0;
 }
// ************************************************************************
// ************************************************************************
 int  selectCrdCard() throws Exception 
 {
  wp.sqlCmd = " select "
              + " acct_type as acct_type,"
              + " p_seqno   as p_seqno "
              + " from crd_card a,crd_idno b"
              + " where card_no      = '"+uploadFileDat[0]+"' "
              + " and   a.id_p_seqno = b.id_p_seqno "
              ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = this.sqlStr("acct_type");
  uploadFileDat[6] = this.sqlStr("p_seqno");

  return(0);
 }
// ************************************************************************
 int  selectDbcCard() throws Exception 
 {
  wp.sqlCmd = " select "
              + " acct_type as acct_type,"
              + " p_seqno   as p_seqno "
              + " from  dbc_card a,dbc_idno b"
              + " where card_no      = '"+uploadFileDat[0]+"' "
              + " and   a.id_p_seqno = b.id_p_seqno "
              ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = this.sqlStr("acct_type");
  uploadFileDat[6] = this.sqlStr("p_seqno");

  return(0);
 }
// ************************************************************************
 int  selectDbaAcnoId() throws Exception 
 {
  wp.sqlCmd = " select "
              + " acct_type as acct_type,"
              + " p_seqno   as p_seqno   "
              + " from dba_acno a,dbc_idno b"
              + " where b.id_no      = '"+uploadFileDat[0]+"' "
              + " and   a.id_p_seqno = b.id_p_seqno "
              ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[3] = "Y";
  uploadFileDat[4] = this.sqlStr("acct_type");
  uploadFileDat[6] = this.sqlStr("p_seqno");

  return(0);
 }
// ************************************************************************
 int  selectActAcnoId() throws Exception 
 {
  wp.sqlCmd = " select "
              + " acct_type as acct_type,"
              + " p_seqno   as p_seqno   "
              + " from act_acno a,crd_idno b"
              + " where b.id_no      = '"+uploadFileDat[0]+"' "
              + " and   a.id_p_seqno = b.id_p_seqno "
              ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[3] = "N";
  uploadFileDat[4] = this.sqlStr("acct_type");
  uploadFileDat[6] = this.sqlStr("p_seqno");

  return(0);
 }
// ************************************************************************
 int  selectDbaAcnoP() throws Exception 
 {
  wp.sqlCmd = " select "
              + " acct_type as acct_type,"
              + " id_no     as id_no  "
              + " from dba_acno a,dbc_idno b"
              + " where p_seqno      = '"+uploadFileDat[0]+"' "
              + " and   a.id_p_seqno = b.id_p_seqno "
              ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[3] = "Y";
  uploadFileDat[4] = this.sqlStr("acct_type");
  uploadFileDat[5] = this.sqlStr("id_no");

  return(0);
 }
// ************************************************************************
 int  selectActAcnoP() throws Exception 
 {
  wp.sqlCmd = " select "
              + " acct_type as acct_type,"
              + " id_no     as id_no   "
              + " from act_acno a,crd_idno b"
              + " where p_seqno      = '"+uploadFileDat[0]+"' "
              + " and   a.id_p_seqno = b.id_p_seqno "
              ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[3] = "N";
  uploadFileDat[4] = this.sqlStr("acct_type");
  uploadFileDat[5] = this.sqlStr("id_no");

  return(0);
 }
// ************************************************************************
int  checkUploadfileAaa1(String ss) throws Exception
 {
  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

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
  uploadFileDat[2]  = "MKT_BPMH3";
  uploadFileDat[3]  = wp.itemStr2("active_code");
  uploadFileDat[4]  = "3";
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
  //******************************************************************
  if ((uploadFileDat[1].length()!=0)&&
      (uploadFileDat[1].length()<8))

  if (uploadFileDat[1].length()!=0)
      uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
                       + uploadFileDat[1];


  return 0;
 }
int  checkUploadfilePmkd(String ss) throws Exception
 {
  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

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
  uploadFileDat[2]  = "MKT_BPMH3";
  uploadFileDat[3]  = wp.itemStr2("active_code");
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
  //******************************************************************
  if ((uploadFileDat[1].length()!=0)&&
      (uploadFileDat[1].length()<8))

  if (uploadFileDat[1].length()!=0)
      uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
                       + uploadFileDat[1];


  return 0;
 }
// ************************************************************************
int  checkUploadfileAaa2(String ss) throws Exception
 {
  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

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
  uploadFileDat[2]  = "MKT_BPMH3";
  uploadFileDat[3]  = wp.itemStr2("active_code");
  uploadFileDat[4]  = "B";
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
  //******************************************************************
  if ((uploadFileDat[1].length()!=0)&&
      (uploadFileDat[1].length()<8))

  if (uploadFileDat[1].length()!=0)
      uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
                       + uploadFileDat[1];


  return 0;
 }
// ************************************************************************
int  checkUploadfilePmkd1(String ss) throws Exception
 {
  mktm01.Mktm0360Func func =new mktm01.Mktm0360Func(wp);

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
  uploadFileDat[2]  = "MKT_BPMH3";
  uploadFileDat[3]  = wp.itemStr2("active_code");
  uploadFileDat[4]  = "P2";
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
  //******************************************************************
  if ((uploadFileDat[1].length()!=0)&&
      (uploadFileDat[1].length()<8))

  if (uploadFileDat[1].length()!=0)
      uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
                       + uploadFileDat[1];


  return 0;
 }
// ************************************************************************
// ************************************************************************
 public void checkButtonOff() throws Exception
  {
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

  if (wp.colStr("group_card_sel").length()==0)
      wp.colSet("group_card_sel" , "0");

  if (wp.colStr("group_card_sel").equals("0"))
     {
      buttonOff("btngpcd_disable");
     }
  else
     {
      wp.colSet("btngpcd_disable","");
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
      buttonOff("btnpmkd_disable");
     }
  else
     {
      wp.colSet("btnpmkd_disable","");
     }

  if (wp.colStr("mcc_code_sel").length()==0)
      wp.colSet("mcc_code_sel" , "0");

  if (wp.colStr("mcc_code_sel").equals("0"))
     {
      buttonOff("btnmccd_disable");
     }
  else
     {
      wp.colSet("btnmccd_disable","");
     }

  if (wp.colStr("bill_type_sel").length()==0)
      wp.colSet("bill_type_sel" , "0");

  if (wp.colStr("bill_type_sel").equals("0"))
     {
      buttonOff("btnacsr_disable");
     }
  else
     {
      wp.colSet("btnacsr_disable","");
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

  if (wp.colStr("d_group_card_sel").length()==0)
      wp.colSet("d_group_card_sel" , "0");

  if (wp.colStr("d_group_card_sel").equals("0"))
     {
      buttonOff("btndpcd_disable");
     }
  else
     {
      wp.colSet("btndpcd_disable","");
     }

  if (wp.colStr("d_merchant_sel").length()==0)
      wp.colSet("d_merchant_sel" , "0");

  if (wp.colStr("d_merchant_sel").equals("0"))
     {
      buttonOff("btndrch_disable");
      buttonOff("uplaaa2_disable");
     }
  else
     {
      wp.colSet("btndrch_disable","");
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

  if (wp.colStr("platform2_kind_sel").length()==0)
      wp.colSet("platform2_kind_sel" , "0");

  if (wp.colStr("platform2_kind_sel").equals("0"))
     {
      buttonOff("btnpmkd1_disable");
     }
  else
     {
      wp.colSet("btnpmkd1_disable","");
     }

  if (wp.colStr("d_mcc_code_sel").length()==0)
      wp.colSet("d_mcc_code_sel" , "0");

  if (wp.colStr("d_mcc_code_sel").equals("0"))
     {
      buttonOff("btndccd_disable");
     }
  else
     {
      wp.colSet("btndccd_disable","");
     }

  if (wp.colStr("d_card_type_sel").length()==0)
      wp.colSet("d_card_type_sel" , "0");

  if (wp.colStr("d_card_type_sel").equals("0"))
     {
      buttonOff("btndype_disable");
     }
  else
     {
      wp.colSet("btndype_disable","");
     }

  if (wp.colStr("d_bill_type_sel").length()==0)
      wp.colSet("d_bill_type_sel" , "0");

  if (wp.colStr("d_bill_type_sel").equals("0"))
     {
      buttonOff("btndesr_disable");
     }
  else
     {
      wp.colSet("btndesr_disable","");
     }

  if (wp.colStr("d_currency_sel").length()==0)
      wp.colSet("d_currency_sel" , "0");

  if (wp.colStr("d_currency_sel").equals("0"))
     {
      buttonOff("btndocq_disable");
     }
  else
     {
      wp.colSet("btndocq_disable","");
     }

  if (wp.colStr("d_pos_entry_sel").length()==0)
      wp.colSet("d_pos_entry_sel" , "0");

  if (wp.colStr("d_pos_entry_sel").equals("0"))
     {
      buttonOff("btnpose_disable");
     }
  else
     {
      wp.colSet("btnpose_disable","");
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

  if ((wp.colStr("aud_type").equals("Y"))||
      (wp.colStr("aud_type").equals("D")))
     {
      buttonOff("upllist_disable");
      buttonOff("uplaaa1_disable");
      buttonOff("uplaaa2_disable");
     }
  else
     {
      wp.colSet("upllist_disable","");
      wp.colSet("uplaaa1_disable","");
      wp.colSet("uplpmkd_disable","");
      wp.colSet("uplaaa2_disable","");
     }
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
  String sql1 = "select "
              + " effect_months "
              + " from cyc_bpid "
              + " where years    =  '" +wp.sysDate.substring(0,4) +"' "
              + " and   bonus_type = 'BONU' "
              + " and   item_code  = '1'  "
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0)
      wp.colSet("effect_months",sqlStr("effect_months"));

   if (wp.respHtml.equals("mktm0360_nadd"))
      {
       wp.colSet("tax_flag"          , "N");
       wp.colSet("d_tax_flag"        , "N");
      }

  buttonOff("btnacty_disable");
  buttonOff("btngpcd_disable");
  buttonOff("btnmrch_disable");
  buttonOff("btnaaa1_disable");
  buttonOff("btnpmkd_disable");
  buttonOff("btnmccd_disable");
  buttonOff("btnacsr_disable");
  buttonOff("btncocq_disable");
  buttonOff("btndpcd_disable");
  buttonOff("btndrch_disable");
  buttonOff("btnaaa2_disable");
  buttonOff("btnpmkd1_disable");
  buttonOff("btndccd_disable");
  buttonOff("btndype_disable");
  buttonOff("btndesr_disable");
  buttonOff("btndocq_disable");
  buttonOff("btnpose_disable");
  buttonOff("btnucaf_disable");
  buttonOff("btndeci_disable");
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
    public void commPlatformKind(String columnData1) throws Exception {
        String columnData = "";
        String sql1 = "";
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            columnData = "";
            sql1 = "select "
                    + " MCHT_GROUP_DESC "
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

}  // End of class
