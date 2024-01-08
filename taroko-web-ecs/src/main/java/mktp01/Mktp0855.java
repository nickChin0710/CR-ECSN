/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/10/15  V1.00.01   Allen Ho      Initial                              *
* 112/02/09  V1.00.02   Zuwei Su      naming rule update                   *
*                                                                          *
***************************************************************************/
package mktp01;

import mktp01.Mktp0855Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0855 extends BaseProc
{
 private final String PROGNAME = "行銷通路活動登錄明細覆核處理程式110/10/15 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0855Func func = null;
  String kk1,kk2;
  String km1,km2;
  String fstAprFlag = "";
  String orgTabName = "mkt_chanrec_parm_t";
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
              + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user")
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
               + "a.active_code,"
               + "a.active_seq,"
               + "a.record_group_no,"
               + "a.record_date_sel,"
               + "a.pur_date_sel,"
               + "a.week_cond,"
               + "a.month_cond,"
               + "a.purchase_type_sel,"
               + "a.threshold_sel,"
               + "a.crt_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by active_code,active_seq"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commActiveName("comm_active_code");
  commRecordGroupNo("comm_record_group_no");
  commCrtUser("comm_crt_user");

  commRecordDateSel("comm_record_date_sel");
  commPdateSel("comm_pur_date_sel");
  commPurcTypeSel("comm_purchase_type_sel");
  commThresholdSel("comm_threshold_sel");
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
  if (wp.itemStr("kk_active_code").length()==0)
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
               + "a.active_seq as active_seq,"
               + "a.crt_user,"
               + "a.record_group_no,"
               + "a.record_date_sel,"
               + "a.pur_date_sel,"
               + "a.purchase_date_s,"
               + "a.purchase_date_e,"
               + "a.week_cond,"
               + "a.month_cond,"
               + "a.cap_sel,"
               + "a.bl_cond,"
               + "a.ca_cond,"
               + "a.it_cond,"
               + "a.id_cond,"
               + "a.ao_cond,"
               + "a.ot_cond,"
               + "a.purchase_type_sel,"
               + "a.per_amt_cond,"
               + "a.per_amt,"
               + "a.max_cnt_cond,"
               + "a.max_cnt,"
               + "a.perday_cnt_cond,"
               + "a.perday_cnt,"
               + "a.sum_amt_cond,"
               + "a.sum_amt,"
               + "a.sum_cnt_cond,"
               + "a.sum_cnt,"
               + "a.above_cond,"
               + "a.above_amt,"
               + "a.above_cnt,"
               + "a.b_feedback_limit,"
               + "a.f_feedback_limit,"
               + "a.s_feedback_limit,"
               + "a.l_feedback_limit,"
               + "a.b_feedback_cnt_limit,"
               + "a.f_feedback_cnt_limit,"
               + "a.s_feedback_cnt_limit,"
               + "a.threshold_sel,"
               + "a.purchase_amt_s1,"
               + "a.purchase_amt_e1,"
               + "a.active_type_1,"
               + "a.feedback_rate_1,"
               + "a.feedback_amt_1,"
               + "a.feedback_lmt_cnt_1,"
               + "a.feedback_lmt_amt_1,"
               + "a.purchase_amt_s2,"
               + "a.purchase_amt_e2,"
               + "a.active_type_2,"
               + "a.feedback_rate_2,"
               + "a.feedback_amt_2,"
               + "a.feedback_lmt_cnt_2,"
               + "a.feedback_lmt_amt_2,"
               + "a.purchase_amt_s3,"
               + "a.purchase_amt_e3,"
               + "a.active_type_3,"
               + "a.feedback_rate_3,"
               + "a.feedback_amt_3,"
               + "a.feedback_lmt_cnt_3,"
               + "a.feedback_lmt_amt_3,"
               + "a.purchase_amt_s4,"
               + "a.purchase_amt_e4,"
               + "a.active_type_4,"
               + "a.feedback_rate_4,"
               + "a.feedback_amt_4,"
               + "a.feedback_lmt_cnt_4,"
               + "a.feedback_lmt_amt_4,"
               + "a.purchase_amt_s5,"
               + "a.purchase_amt_e5,"
               + "a.active_type_5,"
               + "a.feedback_rate_5,"
               + "a.feedback_amt_5,"
               + "a.feedback_lmt_cnt_5,"
               + "a.feedback_lmt_amt_5";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.active_code")
                   + sqlCol(km2, "a.active_seq")
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
  commRdateSel("comm_record_date_sel");
  commPdateSel("comm_pur_date_sel");
  commCapSel("comm_cap_sel");
  commTypeSel("comm_purchase_type_sel");
  commThreshold("comm_threshold_sel");
  commActiveType1("comm_active_type_1");
  commActiveType2("comm_active_type_2");
  commActiveType3("comm_active_type_3");
  commActiveType4("comm_active_type_4");
  commActiveType5("comm_active_type_5");
  commCrtUser("comm_crt_user");
  commActiveName("comm_active_code");
  commRecordGroupNo("comm_record_group_no");
  checkButtonOff();
  km1 = wp.colStr("active_code");
  km2 = wp.colStr("active_seq");
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
  controlTabName = "MKT_CHANREC_PARM";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.active_code as active_code,"
               + "a.active_seq as active_seq,"
               + "a.crt_user as bef_crt_user,"
               + "a.record_group_no as bef_record_group_no,"
               + "a.record_date_sel as bef_record_date_sel,"
               + "a.pur_date_sel as bef_pur_date_sel,"
               + "a.purchase_date_s as bef_purchase_date_s,"
               + "a.purchase_date_e as bef_purchase_date_e,"
               + "a.week_cond as bef_week_cond,"
               + "a.month_cond as bef_month_cond,"
               + "a.cap_sel as bef_cap_sel,"
               + "a.bl_cond as bef_bl_cond,"
               + "a.ca_cond as bef_ca_cond,"
               + "a.it_cond as bef_it_cond,"
               + "a.id_cond as bef_id_cond,"
               + "a.ao_cond as bef_ao_cond,"
               + "a.ot_cond as bef_ot_cond,"
               + "a.purchase_type_sel as bef_purchase_type_sel,"
               + "a.per_amt_cond as bef_per_amt_cond,"
               + "a.per_amt as bef_per_amt,"
               + "a.max_cnt_cond as bef_max_cnt_cond,"
               + "a.max_cnt as bef_max_cnt,"
               + "a.perday_cnt_cond as bef_perday_cnt_cond,"
               + "a.perday_cnt as bef_perday_cnt,"
               + "a.sum_amt_cond as bef_sum_amt_cond,"
               + "a.sum_amt as bef_sum_amt,"
               + "a.sum_cnt_cond as bef_sum_cnt_cond,"
               + "a.sum_cnt as bef_sum_cnt,"
               + "a.above_cond as bef_above_cond,"
               + "a.above_amt as bef_above_amt,"
               + "a.above_cnt as bef_above_cnt,"
               + "a.b_feedback_limit as bef_b_feedback_limit,"
               + "a.f_feedback_limit as bef_f_feedback_limit,"
               + "a.s_feedback_limit as bef_s_feedback_limit,"
               + "a.l_feedback_limit as bef_l_feedback_limit,"
               + "a.b_feedback_cnt_limit as bef_b_feedback_cnt_limit,"
               + "a.f_feedback_cnt_limit as bef_f_feedback_cnt_limit,"
               + "a.s_feedback_cnt_limit as bef_s_feedback_cnt_limit,"
               + "a.threshold_sel as bef_threshold_sel,"
               + "a.purchase_amt_s1 as bef_purchase_amt_s1,"
               + "a.purchase_amt_e1 as bef_purchase_amt_e1,"
               + "a.active_type_1 as bef_active_type_1,"
               + "a.feedback_rate_1 as bef_feedback_rate_1,"
               + "a.feedback_amt_1 as bef_feedback_amt_1,"
               + "a.feedback_lmt_cnt_1 as bef_feedback_lmt_cnt_1,"
               + "a.feedback_lmt_amt_1 as bef_feedback_lmt_amt_1,"
               + "a.purchase_amt_s2 as bef_purchase_amt_s2,"
               + "a.purchase_amt_e2 as bef_purchase_amt_e2,"
               + "a.active_type_2 as bef_active_type_2,"
               + "a.feedback_rate_2 as bef_feedback_rate_2,"
               + "a.feedback_amt_2 as bef_feedback_amt_2,"
               + "a.feedback_lmt_cnt_2 as bef_feedback_lmt_cnt_2,"
               + "a.feedback_lmt_amt_2 as bef_feedback_lmt_amt_2,"
               + "a.purchase_amt_s3 as bef_purchase_amt_s3,"
               + "a.purchase_amt_e3 as bef_purchase_amt_e3,"
               + "a.active_type_3 as bef_active_type_3,"
               + "a.feedback_rate_3 as bef_feedback_rate_3,"
               + "a.feedback_amt_3 as bef_feedback_amt_3,"
               + "a.feedback_lmt_cnt_3 as bef_feedback_lmt_cnt_3,"
               + "a.feedback_lmt_amt_3 as bef_feedback_lmt_amt_3,"
               + "a.purchase_amt_s4 as bef_purchase_amt_s4,"
               + "a.purchase_amt_e4 as bef_purchase_amt_e4,"
               + "a.active_type_4 as bef_active_type_4,"
               + "a.feedback_rate_4 as bef_feedback_rate_4,"
               + "a.feedback_amt_4 as bef_feedback_amt_4,"
               + "a.feedback_lmt_cnt_4 as bef_feedback_lmt_cnt_4,"
               + "a.feedback_lmt_amt_4 as bef_feedback_lmt_amt_4,"
               + "a.purchase_amt_s5 as bef_purchase_amt_s5,"
               + "a.purchase_amt_e5 as bef_purchase_amt_e5,"
               + "a.active_type_5 as bef_active_type_5,"
               + "a.feedback_rate_5 as bef_feedback_rate_5,"
               + "a.feedback_amt_5 as bef_feedback_amt_5,"
               + "a.feedback_lmt_cnt_5 as bef_feedback_lmt_cnt_5,"
               + "a.feedback_lmt_amt_5 as bef_feedback_lmt_amt_5";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.active_code")
              + sqlCol(km2, "a.active_seq")
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
  commActiveName("comm_active_code");
  commRecordGroupNo("comm_record_group_no");
  commRdateSel("comm_record_date_sel");
  commPdateSel("comm_pur_date_sel");
  commCapSel("comm_cap_sel");
  commTypeSel("comm_purchase_type_sel");
  commThreshold("comm_threshold_sel");
  commActiveType1("comm_active_type_1");
  commActiveType2("comm_active_type_2");
  commActiveType3("comm_active_type_3");
  commActiveType4("comm_active_type_4");
  commActiveType5("comm_active_type_5");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("week_cond_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"5"));
  wp.colSet("month_cond_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"6"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("record_group_no").equals(wp.colStr("bef_record_group_no")))
     wp.colSet("opt_record_group_no","Y");
  commRecordGroupNo("comm_record_group_no");
  commRecordGroupNo("comm_bef_record_group_no",1);

  if (!wp.colStr("record_date_sel").equals(wp.colStr("bef_record_date_sel")))
     wp.colSet("opt_record_date_sel","Y");
  commRdateSel("comm_record_date_sel");
  commRdateSel("comm_bef_record_date_sel");

  if (!wp.colStr("pur_date_sel").equals(wp.colStr("bef_pur_date_sel")))
     wp.colSet("opt_pur_date_sel","Y");
  commPdateSel("comm_pur_date_sel");
  commPdateSel("comm_bef_pur_date_sel");

  if (!wp.colStr("purchase_date_s").equals(wp.colStr("bef_purchase_date_s")))
     wp.colSet("opt_purchase_date_s","Y");

  if (!wp.colStr("purchase_date_e").equals(wp.colStr("bef_purchase_date_e")))
     wp.colSet("opt_purchase_date_e","Y");

  if (!wp.colStr("week_cond").equals(wp.colStr("bef_week_cond")))
     wp.colSet("opt_week_cond","Y");

  wp.colSet("bef_week_cond_cnt" , listMktBnData("mkt_bn_data","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"5"));
  if (!wp.colStr("week_cond_cnt").equals(wp.colStr("bef_week_cond_cnt")))
     wp.colSet("opt_week_cond_cnt","Y");

  if (!wp.colStr("month_cond").equals(wp.colStr("bef_month_cond")))
     wp.colSet("opt_month_cond","Y");

  wp.colSet("bef_month_cond_cnt" , listMktBnData("mkt_bn_data","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"6"));
  if (!wp.colStr("month_cond_cnt").equals(wp.colStr("bef_month_cond_cnt")))
     wp.colSet("opt_month_cond_cnt","Y");

  if (!wp.colStr("cap_sel").equals(wp.colStr("bef_cap_sel")))
     wp.colSet("opt_cap_sel","Y");
  commCapSel("comm_cap_sel");
  commCapSel("comm_bef_cap_sel");

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

  if (!wp.colStr("purchase_type_sel").equals(wp.colStr("bef_purchase_type_sel")))
     wp.colSet("opt_purchase_type_sel","Y");
  commTypeSel("comm_purchase_type_sel");
  commTypeSel("comm_bef_purchase_type_sel");

  if (!wp.colStr("per_amt_cond").equals(wp.colStr("bef_per_amt_cond")))
     wp.colSet("opt_per_amt_cond","Y");

  if (!wp.colStr("per_amt").equals(wp.colStr("bef_per_amt")))
     wp.colSet("opt_per_amt","Y");

  if (!wp.colStr("max_cnt_cond").equals(wp.colStr("bef_max_cnt_cond")))
     wp.colSet("opt_max_cnt_cond","Y");

  if (!wp.colStr("max_cnt").equals(wp.colStr("bef_max_cnt")))
     wp.colSet("opt_max_cnt","Y");

  if (!wp.colStr("perday_cnt_cond").equals(wp.colStr("bef_perday_cnt_cond")))
     wp.colSet("opt_perday_cnt_cond","Y");

  if (!wp.colStr("perday_cnt").equals(wp.colStr("bef_perday_cnt")))
     wp.colSet("opt_perday_cnt","Y");

  if (!wp.colStr("sum_amt_cond").equals(wp.colStr("bef_sum_amt_cond")))
     wp.colSet("opt_sum_amt_cond","Y");

  if (!wp.colStr("sum_amt").equals(wp.colStr("bef_sum_amt")))
     wp.colSet("opt_sum_amt","Y");

  if (!wp.colStr("sum_cnt_cond").equals(wp.colStr("bef_sum_cnt_cond")))
     wp.colSet("opt_sum_cnt_cond","Y");

  if (!wp.colStr("sum_cnt").equals(wp.colStr("bef_sum_cnt")))
     wp.colSet("opt_sum_cnt","Y");

  if (!wp.colStr("above_cond").equals(wp.colStr("bef_above_cond")))
     wp.colSet("opt_above_cond","Y");

  if (!wp.colStr("above_amt").equals(wp.colStr("bef_above_amt")))
     wp.colSet("opt_above_amt","Y");

  if (!wp.colStr("above_cnt").equals(wp.colStr("bef_above_cnt")))
     wp.colSet("opt_above_cnt","Y");

  if (!wp.colStr("b_feedback_limit").equals(wp.colStr("bef_b_feedback_limit")))
     wp.colSet("opt_b_feedback_limit","Y");

  if (!wp.colStr("f_feedback_limit").equals(wp.colStr("bef_f_feedback_limit")))
     wp.colSet("opt_f_feedback_limit","Y");

  if (!wp.colStr("s_feedback_limit").equals(wp.colStr("bef_s_feedback_limit")))
     wp.colSet("opt_s_feedback_limit","Y");

  if (!wp.colStr("l_feedback_limit").equals(wp.colStr("bef_l_feedback_limit")))
     wp.colSet("opt_l_feedback_limit","Y");

  if (!wp.colStr("b_feedback_cnt_limit").equals(wp.colStr("bef_b_feedback_cnt_limit")))
     wp.colSet("opt_b_feedback_cnt_limit","Y");

  if (!wp.colStr("f_feedback_cnt_limit").equals(wp.colStr("bef_f_feedback_cnt_limit")))
     wp.colSet("opt_f_feedback_cnt_limit","Y");

  if (!wp.colStr("s_feedback_cnt_limit").equals(wp.colStr("bef_s_feedback_cnt_limit")))
     wp.colSet("opt_s_feedback_cnt_limit","Y");

  if (!wp.colStr("threshold_sel").equals(wp.colStr("bef_threshold_sel")))
     wp.colSet("opt_threshold_sel","Y");
  commThreshold("comm_threshold_sel");
  commThreshold("comm_bef_threshold_sel");

  if (!wp.colStr("purchase_amt_s1").equals(wp.colStr("bef_purchase_amt_s1")))
     wp.colSet("opt_purchase_amt_s1","Y");

  if (!wp.colStr("purchase_amt_e1").equals(wp.colStr("bef_purchase_amt_e1")))
     wp.colSet("opt_purchase_amt_e1","Y");

  if (!wp.colStr("active_type_1").equals(wp.colStr("bef_active_type_1")))
     wp.colSet("opt_active_type_1","Y");
  commActiveType1("comm_active_type_1");
  commActiveType1("comm_bef_active_type_1");

  if (!wp.colStr("feedback_rate_1").equals(wp.colStr("bef_feedback_rate_1")))
     wp.colSet("opt_feedback_rate_1","Y");

  if (!wp.colStr("feedback_amt_1").equals(wp.colStr("bef_feedback_amt_1")))
     wp.colSet("opt_feedback_amt_1","Y");

  if (!wp.colStr("feedback_lmt_cnt_1").equals(wp.colStr("bef_feedback_lmt_cnt_1")))
     wp.colSet("opt_feedback_lmt_cnt_1","Y");

  if (!wp.colStr("feedback_lmt_amt_1").equals(wp.colStr("bef_feedback_lmt_amt_1")))
     wp.colSet("opt_feedback_lmt_amt_1","Y");

  if (!wp.colStr("purchase_amt_s2").equals(wp.colStr("bef_purchase_amt_s2")))
     wp.colSet("opt_purchase_amt_s2","Y");

  if (!wp.colStr("purchase_amt_e2").equals(wp.colStr("bef_purchase_amt_e2")))
     wp.colSet("opt_purchase_amt_e2","Y");

  if (!wp.colStr("active_type_2").equals(wp.colStr("bef_active_type_2")))
     wp.colSet("opt_active_type_2","Y");
  commActiveType2("comm_active_type_2");
  commActiveType2("comm_bef_active_type_2");

  if (!wp.colStr("feedback_rate_2").equals(wp.colStr("bef_feedback_rate_2")))
     wp.colSet("opt_feedback_rate_2","Y");

  if (!wp.colStr("feedback_amt_2").equals(wp.colStr("bef_feedback_amt_2")))
     wp.colSet("opt_feedback_amt_2","Y");

  if (!wp.colStr("feedback_lmt_cnt_2").equals(wp.colStr("bef_feedback_lmt_cnt_2")))
     wp.colSet("opt_feedback_lmt_cnt_2","Y");

  if (!wp.colStr("feedback_lmt_amt_2").equals(wp.colStr("bef_feedback_lmt_amt_2")))
     wp.colSet("opt_feedback_lmt_amt_2","Y");

  if (!wp.colStr("purchase_amt_s3").equals(wp.colStr("bef_purchase_amt_s3")))
     wp.colSet("opt_purchase_amt_s3","Y");

  if (!wp.colStr("purchase_amt_e3").equals(wp.colStr("bef_purchase_amt_e3")))
     wp.colSet("opt_purchase_amt_e3","Y");

  if (!wp.colStr("active_type_3").equals(wp.colStr("bef_active_type_3")))
     wp.colSet("opt_active_type_3","Y");
  commActiveType3("comm_active_type_3");
  commActiveType3("comm_bef_active_type_3");

  if (!wp.colStr("feedback_rate_3").equals(wp.colStr("bef_feedback_rate_3")))
     wp.colSet("opt_feedback_rate_3","Y");

  if (!wp.colStr("feedback_amt_3").equals(wp.colStr("bef_feedback_amt_3")))
     wp.colSet("opt_feedback_amt_3","Y");

  if (!wp.colStr("feedback_lmt_cnt_3").equals(wp.colStr("bef_feedback_lmt_cnt_3")))
     wp.colSet("opt_feedback_lmt_cnt_3","Y");

  if (!wp.colStr("feedback_lmt_amt_3").equals(wp.colStr("bef_feedback_lmt_amt_3")))
     wp.colSet("opt_feedback_lmt_amt_3","Y");

  if (!wp.colStr("purchase_amt_s4").equals(wp.colStr("bef_purchase_amt_s4")))
     wp.colSet("opt_purchase_amt_s4","Y");

  if (!wp.colStr("purchase_amt_e4").equals(wp.colStr("bef_purchase_amt_e4")))
     wp.colSet("opt_purchase_amt_e4","Y");

  if (!wp.colStr("active_type_4").equals(wp.colStr("bef_active_type_4")))
     wp.colSet("opt_active_type_4","Y");
  commActiveType4("comm_active_type_4");
  commActiveType4("comm_bef_active_type_4");

  if (!wp.colStr("feedback_rate_4").equals(wp.colStr("bef_feedback_rate_4")))
     wp.colSet("opt_feedback_rate_4","Y");

  if (!wp.colStr("feedback_amt_4").equals(wp.colStr("bef_feedback_amt_4")))
     wp.colSet("opt_feedback_amt_4","Y");

  if (!wp.colStr("feedback_lmt_cnt_4").equals(wp.colStr("bef_feedback_lmt_cnt_4")))
     wp.colSet("opt_feedback_lmt_cnt_4","Y");

  if (!wp.colStr("feedback_lmt_amt_4").equals(wp.colStr("bef_feedback_lmt_amt_4")))
     wp.colSet("opt_feedback_lmt_amt_4","Y");

  if (!wp.colStr("purchase_amt_s5").equals(wp.colStr("bef_purchase_amt_s5")))
     wp.colSet("opt_purchase_amt_s5","Y");

  if (!wp.colStr("purchase_amt_e5").equals(wp.colStr("bef_purchase_amt_e5")))
     wp.colSet("opt_purchase_amt_e5","Y");

  if (!wp.colStr("active_type_5").equals(wp.colStr("bef_active_type_5")))
     wp.colSet("opt_active_type_5","Y");
  commActiveType5("comm_active_type_5");
  commActiveType5("comm_bef_active_type_5");

  if (!wp.colStr("feedback_rate_5").equals(wp.colStr("bef_feedback_rate_5")))
     wp.colSet("opt_feedback_rate_5","Y");

  if (!wp.colStr("feedback_amt_5").equals(wp.colStr("bef_feedback_amt_5")))
     wp.colSet("opt_feedback_amt_5","Y");

  if (!wp.colStr("feedback_lmt_cnt_5").equals(wp.colStr("bef_feedback_lmt_cnt_5")))
     wp.colSet("opt_feedback_lmt_cnt_5","Y");

  if (!wp.colStr("feedback_lmt_amt_5").equals(wp.colStr("bef_feedback_lmt_amt_5")))
     wp.colSet("opt_feedback_lmt_amt_5","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("record_group_no","");
       wp.colSet("record_date_sel","");
       wp.colSet("pur_date_sel","");
       wp.colSet("purchase_date_s","");
       wp.colSet("purchase_date_e","");
       wp.colSet("week_cond","");
       wp.colSet("week_cond_cnt","");
       wp.colSet("month_cond","");
       wp.colSet("month_cond_cnt","");
       wp.colSet("cap_sel","");
       wp.colSet("bl_cond","");
       wp.colSet("ca_cond","");
       wp.colSet("it_cond","");
       wp.colSet("id_cond","");
       wp.colSet("ao_cond","");
       wp.colSet("ot_cond","");
       wp.colSet("purchase_type_sel","");
       wp.colSet("per_amt_cond","");
       wp.colSet("per_amt","");
       wp.colSet("max_cnt_cond","");
       wp.colSet("max_cnt","");
       wp.colSet("perday_cnt_cond","");
       wp.colSet("perday_cnt","");
       wp.colSet("sum_amt_cond","");
       wp.colSet("sum_amt","");
       wp.colSet("sum_cnt_cond","");
       wp.colSet("sum_cnt","");
       wp.colSet("above_cond","");
       wp.colSet("above_amt","");
       wp.colSet("above_cnt","");
       wp.colSet("b_feedback_limit","");
       wp.colSet("f_feedback_limit","");
       wp.colSet("s_feedback_limit","");
       wp.colSet("l_feedback_limit","");
       wp.colSet("b_feedback_cnt_limit","");
       wp.colSet("f_feedback_cnt_limit","");
       wp.colSet("s_feedback_cnt_limit","");
       wp.colSet("threshold_sel","");
       wp.colSet("purchase_amt_s1","");
       wp.colSet("purchase_amt_e1","");
       wp.colSet("active_type_1","");
       wp.colSet("feedback_rate_1","");
       wp.colSet("feedback_amt_1","");
       wp.colSet("feedback_lmt_cnt_1","");
       wp.colSet("feedback_lmt_amt_1","");
       wp.colSet("purchase_amt_s2","");
       wp.colSet("purchase_amt_e2","");
       wp.colSet("active_type_2","");
       wp.colSet("feedback_rate_2","");
       wp.colSet("feedback_amt_2","");
       wp.colSet("feedback_lmt_cnt_2","");
       wp.colSet("feedback_lmt_amt_2","");
       wp.colSet("purchase_amt_s3","");
       wp.colSet("purchase_amt_e3","");
       wp.colSet("active_type_3","");
       wp.colSet("feedback_rate_3","");
       wp.colSet("feedback_amt_3","");
       wp.colSet("feedback_lmt_cnt_3","");
       wp.colSet("feedback_lmt_amt_3","");
       wp.colSet("purchase_amt_s4","");
       wp.colSet("purchase_amt_e4","");
       wp.colSet("active_type_4","");
       wp.colSet("feedback_rate_4","");
       wp.colSet("feedback_amt_4","");
       wp.colSet("feedback_lmt_cnt_4","");
       wp.colSet("feedback_lmt_amt_4","");
       wp.colSet("purchase_amt_s5","");
       wp.colSet("purchase_amt_e5","");
       wp.colSet("active_type_5","");
       wp.colSet("feedback_rate_5","");
       wp.colSet("feedback_amt_5","");
       wp.colSet("feedback_lmt_cnt_5","");
       wp.colSet("feedback_lmt_amt_5","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("record_group_no").length()==0)
     wp.colSet("opt_record_group_no","Y");

  if (wp.colStr("record_date_sel").length()==0)
     wp.colSet("opt_record_date_sel","Y");

  if (wp.colStr("pur_date_sel").length()==0)
     wp.colSet("opt_pur_date_sel","Y");

  if (wp.colStr("purchase_date_s").length()==0)
     wp.colSet("opt_purchase_date_s","Y");

  if (wp.colStr("purchase_date_e").length()==0)
     wp.colSet("opt_purchase_date_e","Y");

  if (wp.colStr("week_cond").length()==0)
     wp.colSet("opt_week_cond","Y");


  if (wp.colStr("month_cond").length()==0)
     wp.colSet("opt_month_cond","Y");


  if (wp.colStr("cap_sel").length()==0)
     wp.colSet("opt_cap_sel","Y");

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

  if (wp.colStr("purchase_type_sel").length()==0)
     wp.colSet("opt_purchase_type_sel","Y");

  if (wp.colStr("per_amt_cond").length()==0)
     wp.colSet("opt_per_amt_cond","Y");

  if (wp.colStr("per_amt").length()==0)
     wp.colSet("opt_per_amt","Y");

  if (wp.colStr("max_cnt_cond").length()==0)
     wp.colSet("opt_max_cnt_cond","Y");

  if (wp.colStr("max_cnt").length()==0)
     wp.colSet("opt_max_cnt","Y");

  if (wp.colStr("perday_cnt_cond").length()==0)
     wp.colSet("opt_perday_cnt_cond","Y");

  if (wp.colStr("perday_cnt").length()==0)
     wp.colSet("opt_perday_cnt","Y");

  if (wp.colStr("sum_amt_cond").length()==0)
     wp.colSet("opt_sum_amt_cond","Y");

  if (wp.colStr("sum_amt").length()==0)
     wp.colSet("opt_sum_amt","Y");

  if (wp.colStr("sum_cnt_cond").length()==0)
     wp.colSet("opt_sum_cnt_cond","Y");

  if (wp.colStr("sum_cnt").length()==0)
     wp.colSet("opt_sum_cnt","Y");

  if (wp.colStr("above_cond").length()==0)
     wp.colSet("opt_above_cond","Y");

  if (wp.colStr("above_amt").length()==0)
     wp.colSet("opt_above_amt","Y");

  if (wp.colStr("above_cnt").length()==0)
     wp.colSet("opt_above_cnt","Y");

  if (wp.colStr("b_feedback_limit").length()==0)
     wp.colSet("opt_b_feedback_limit","Y");

  if (wp.colStr("f_feedback_limit").length()==0)
     wp.colSet("opt_f_feedback_limit","Y");

  if (wp.colStr("s_feedback_limit").length()==0)
     wp.colSet("opt_s_feedback_limit","Y");

  if (wp.colStr("l_feedback_limit").length()==0)
     wp.colSet("opt_l_feedback_limit","Y");

  if (wp.colStr("b_feedback_cnt_limit").length()==0)
     wp.colSet("opt_b_feedback_cnt_limit","Y");

  if (wp.colStr("f_feedback_cnt_limit").length()==0)
     wp.colSet("opt_f_feedback_cnt_limit","Y");

  if (wp.colStr("s_feedback_cnt_limit").length()==0)
     wp.colSet("opt_s_feedback_cnt_limit","Y");

  if (wp.colStr("threshold_sel").length()==0)
     wp.colSet("opt_threshold_sel","Y");

  if (wp.colStr("purchase_amt_s1").length()==0)
     wp.colSet("opt_purchase_amt_s1","Y");

  if (wp.colStr("purchase_amt_e1").length()==0)
     wp.colSet("opt_purchase_amt_e1","Y");

  if (wp.colStr("active_type_1").length()==0)
     wp.colSet("opt_active_type_1","Y");

  if (wp.colStr("feedback_rate_1").length()==0)
     wp.colSet("opt_feedback_rate_1","Y");

  if (wp.colStr("feedback_amt_1").length()==0)
     wp.colSet("opt_feedback_amt_1","Y");

  if (wp.colStr("feedback_lmt_cnt_1").length()==0)
     wp.colSet("opt_feedback_lmt_cnt_1","Y");

  if (wp.colStr("feedback_lmt_amt_1").length()==0)
     wp.colSet("opt_feedback_lmt_amt_1","Y");

  if (wp.colStr("purchase_amt_s2").length()==0)
     wp.colSet("opt_purchase_amt_s2","Y");

  if (wp.colStr("purchase_amt_e2").length()==0)
     wp.colSet("opt_purchase_amt_e2","Y");

  if (wp.colStr("active_type_2").length()==0)
     wp.colSet("opt_active_type_2","Y");

  if (wp.colStr("feedback_rate_2").length()==0)
     wp.colSet("opt_feedback_rate_2","Y");

  if (wp.colStr("feedback_amt_2").length()==0)
     wp.colSet("opt_feedback_amt_2","Y");

  if (wp.colStr("feedback_lmt_cnt_2").length()==0)
     wp.colSet("opt_feedback_lmt_cnt_2","Y");

  if (wp.colStr("feedback_lmt_amt_2").length()==0)
     wp.colSet("opt_feedback_lmt_amt_2","Y");

  if (wp.colStr("purchase_amt_s3").length()==0)
     wp.colSet("opt_purchase_amt_s3","Y");

  if (wp.colStr("purchase_amt_e3").length()==0)
     wp.colSet("opt_purchase_amt_e3","Y");

  if (wp.colStr("active_type_3").length()==0)
     wp.colSet("opt_active_type_3","Y");

  if (wp.colStr("feedback_rate_3").length()==0)
     wp.colSet("opt_feedback_rate_3","Y");

  if (wp.colStr("feedback_amt_3").length()==0)
     wp.colSet("opt_feedback_amt_3","Y");

  if (wp.colStr("feedback_lmt_cnt_3").length()==0)
     wp.colSet("opt_feedback_lmt_cnt_3","Y");

  if (wp.colStr("feedback_lmt_amt_3").length()==0)
     wp.colSet("opt_feedback_lmt_amt_3","Y");

  if (wp.colStr("purchase_amt_s4").length()==0)
     wp.colSet("opt_purchase_amt_s4","Y");

  if (wp.colStr("purchase_amt_e4").length()==0)
     wp.colSet("opt_purchase_amt_e4","Y");

  if (wp.colStr("active_type_4").length()==0)
     wp.colSet("opt_active_type_4","Y");

  if (wp.colStr("feedback_rate_4").length()==0)
     wp.colSet("opt_feedback_rate_4","Y");

  if (wp.colStr("feedback_amt_4").length()==0)
     wp.colSet("opt_feedback_amt_4","Y");

  if (wp.colStr("feedback_lmt_cnt_4").length()==0)
     wp.colSet("opt_feedback_lmt_cnt_4","Y");

  if (wp.colStr("feedback_lmt_amt_4").length()==0)
     wp.colSet("opt_feedback_lmt_amt_4","Y");

  if (wp.colStr("purchase_amt_s5").length()==0)
     wp.colSet("opt_purchase_amt_s5","Y");

  if (wp.colStr("purchase_amt_e5").length()==0)
     wp.colSet("opt_purchase_amt_e5","Y");

  if (wp.colStr("active_type_5").length()==0)
     wp.colSet("opt_active_type_5","Y");

  if (wp.colStr("feedback_rate_5").length()==0)
     wp.colSet("opt_feedback_rate_5","Y");

  if (wp.colStr("feedback_amt_5").length()==0)
     wp.colSet("opt_feedback_amt_5","Y");

  if (wp.colStr("feedback_lmt_cnt_5").length()==0)
     wp.colSet("opt_feedback_lmt_cnt_5","Y");

  if (wp.colStr("feedback_lmt_amt_5").length()==0)
     wp.colSet("opt_feedback_lmt_amt_5","Y");

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
               + " and table_name  =  'MKT_CHANREC_PARM' "
               ;
   if (wp.respHtml.equals("mktp0855_week"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktp0855_mont"))
      wp.whereStr  += " and data_type  = '6' ";
   String whereCnt = wp.whereStr;
   whereCnt += " and  data_key = '"+wp.itemStr("active_code")+wp.itemStr("active_seq")+"'";
   wp.whereStr  += " and  data_key = :data_key ";
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key",wp.itemStr("active_code")+wp.itemStr("active_seq"));
   wp.whereStr  += " order by 4,5 ";
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
   if (wp.respHtml.equals("mktp0855_week"))
    commDataCodeb("comm_data_code");
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
  mktp01.Mktp0855Func func =new mktp01.Mktp0855Func(wp);

  String[] lsActiveCode = wp.itemBuff("active_code");
  String[] lsActiveSeq = wp.itemBuff("active_seq");
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
     func.varsSet("active_seq", lsActiveSeq[rr]);
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
         commActiveName("comm_active_code");
         commRecordGroupNo("comm_record_group_no");
         commCrtUser("comm_crt_user");
         commRecordDateSel("comm_record_date_sel");
         commPdateSel("comm_pur_date_sel");
         commPurcTypeSel("comm_purchase_type_sel");
         commThresholdSel("comm_threshold_sel");
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
       if ((wp.respHtml.equals("mktp0855")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_active_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_active_code");
             }
          this.dddwList("dddw_active_code"
                 ,"mkt_channel_parm"
                 ,"trim(active_code)"
                 ,"trim(active_name)"
                 ," where active_code in (select active_code from mkt_chanrec_parm_t where apr_flag='N')");
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
 public void commActiveName(String s1) throws Exception 
 {
  commActiveName(s1,0);
  return;
 }
// ************************************************************************
 public void commActiveName(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " active_name as column_active_name "
            + " from mkt_channel_parm "
            + " where 1 = 1 "
            + " and   active_code = '"+wp.colStr(ii,befStr+"active_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"active_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_active_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commRecordGroupNo(String s1) throws Exception 
 {
  commRecordGroupNo(s1,0);
  return;
 }
// ************************************************************************
 public void commRecordGroupNo(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " record_group_name as column_record_group_name "
            + " from web_record_group "
            + " where 1 = 1 "
            + " and   record_group_no = '"+wp.colStr(ii,befStr+"record_group_no")+"'"
            ;
       if (wp.colStr(ii,befStr+"record_group_no").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_record_group_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commRdateSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"有登錄","登錄前之消費(最後登錄日","登錄後之消費"};
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
 public void commPdateSel(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"依原消費期間","一段期間"};
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
 public void commCapSel(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"依原消費本金類","重新指定"};
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
 public void commTypeSel(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4","5"};
  String[] txt = {"累積金額","累積筆數","日累積金額","日累積筆數","筆消費金額"};
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
 public void commThreshold(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"1.級距式","2.條件式"};
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
 public void commActiveType1(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"1.紅利","2.基金","3.贈品","4.名單"};
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
 public void commActiveType2(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"1.紅利","2.基金","3.贈品","4.名單"};
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
 public void commActiveType3(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"1.紅利","2.基金","3.贈品","4.名單"};
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
 public void commActiveType4(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"1.紅利","2.基金","3.贈品","4.名單"};
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
 public void commActiveType5(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"1.紅利","2.基金","3.贈品","4.名單"};
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
 public void commRecordDateSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"有登錄","登錄>前之消費","登錄後之消費"};
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
 public void commPurcTypeSel(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4","5"};
  String[] txt = {"累積金額","累>積>筆數","日累積金額","日累積筆數","筆消費金額"};
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
 public void commDataCodeb(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"星期日","星期一","星期二"};
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
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
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
          + " from sec_user a,mkt_chanrec_parm_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
