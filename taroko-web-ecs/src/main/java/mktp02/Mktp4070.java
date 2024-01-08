/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/15  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard  
* 112-08-31  V1.00.02  Machao      list_flag新增TCB_ID(生日禮)栏位                                                                 *
***************************************************************************/
package mktp02;

import mktp02.Mktp4070Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp4070 extends BaseProc
{
 private final String PROGNAME = "專案現金回饋參數檔覆核處理程式110/01/15 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp4070Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_loan_parm_t";
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
              + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code", "like%")
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
               + "a.fund_code,"
               + "a.fund_name,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by fund_code desc"
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
               + "a.effect_months,"
               + "a.stop_flag,"
               + "a.list_cond,"
               + "a.list_flag,"
               + "a.add_vouch_no,"
               + "a.rem_vouch_no,"
               + "a.acct_type_sel,"
               + "a.group_code_sel,"
               + "a.group_oppost_cond,"
               + "a.feedback_lmt,"
               + "a.res_flag,"
               + "a.exec_s_months,"
               + "a.res_total_cnt,"
               + "a.move_cond,"
               + "a.bil_mcht_cond,"
               + "a.merchant_sel,"
               + "a.mcht_group_sel,"
               + "a.issue_a_months,"
               + "a.mcode,"
               + "a.cancel_scope,"
               + "a.cancel_rate,"
               + "a.cancel_event";

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
  commListFlag("comm_list_flag");
  commAcctTypeSel("comm_acct_type_sel");
  commGroupCodeSel("comm_group_code_sel");
  commResFlag("comm_res_flag");
  commMoveCond("comm_move_cond");
  commSelect1("comm_merchant_sel");
  commSelectB("comm_mcht_group_sel");
  commCancelScope("comm_cancel_scope");
  commCancelEvent("comm_cancel_event");
  commCrtUser("comm_crt_user");
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
  controlTabName = "MKT_LOAN_PARM";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.fund_code as fund_code,"
               + "a.crt_user as bef_crt_user,"
               + "a.fund_name as bef_fund_name,"
               + "a.effect_months as bef_effect_months,"
               + "a.stop_flag as bef_stop_flag,"
               + "a.list_cond as bef_list_cond,"
               + "a.list_flag as bef_list_flag,"
               + "a.add_vouch_no as bef_add_vouch_no,"
               + "a.rem_vouch_no as bef_rem_vouch_no,"
               + "a.acct_type_sel as bef_acct_type_sel,"
               + "a.group_code_sel as bef_group_code_sel,"
               + "a.group_oppost_cond as bef_group_oppost_cond,"
               + "a.feedback_lmt as bef_feedback_lmt,"
               + "a.res_flag as bef_res_flag,"
               + "a.exec_s_months as bef_exec_s_months,"
               + "a.res_total_cnt as bef_res_total_cnt,"
               + "a.move_cond as bef_move_cond,"
               + "a.bil_mcht_cond as bef_bil_mcht_cond,"
               + "a.merchant_sel as bef_merchant_sel,"
               + "a.mcht_group_sel as bef_mcht_group_sel,"
               + "a.issue_a_months as bef_issue_a_months,"
               + "a.mcode as bef_mcode,"
               + "a.cancel_scope as bef_cancel_scope,"
               + "a.cancel_rate as bef_cancel_rate,"
               + "a.cancel_event as bef_cancel_event";

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
  commListFlag("comm_list_flag");
  commAcctTypeSel("comm_acct_type_sel");
  commGroupCodeSel("comm_group_code_sel");
  commResFlag("comm_res_flag");
  commMoveCond("comm_move_cond");
  commSelect1("comm_merchant_sel");
  commSelectB("comm_mcht_group_sel");
  commCancelScope("comm_cancel_scope");
  commCancelEvent("comm_cancel_event");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("list_flag_cnt" , listMktImloanList("mkt_imloan_list_t","mkt_imloan_list",wp.colStr("fund_code"),""));
  wp.colSet("acct_type_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"1"));
  wp.colSet("group_code_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"2"));
  wp.colSet("merchant_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"3"));
  wp.colSet("mcht_group_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"4"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("fund_name").equals(wp.colStr("bef_fund_name")))
     wp.colSet("opt_fund_name","Y");

  if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
     wp.colSet("opt_effect_months","Y");

  if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
     wp.colSet("opt_stop_flag","Y");

  if (!wp.colStr("list_cond").equals(wp.colStr("bef_list_cond")))
     wp.colSet("opt_list_cond","Y");

  if (!wp.colStr("list_flag").equals(wp.colStr("bef_list_flag")))
     wp.colSet("opt_list_flag","Y");
  commListFlag("comm_list_flag");
  commListFlag("comm_bef_list_flag");

  wp.colSet("bef_list_flag_cnt" , listMktImloanList("mkt_imloan_list","mkt_imloan_list",wp.colStr("fund_code"),""));
  if (!wp.colStr("list_flag_cnt").equals(wp.colStr("bef_list_flag_cnt")))
     wp.colSet("opt_list_flag_cnt","Y");

  if (!wp.colStr("add_vouch_no").equals(wp.colStr("bef_add_vouch_no")))
     wp.colSet("opt_add_vouch_no","Y");

  if (!wp.colStr("rem_vouch_no").equals(wp.colStr("bef_rem_vouch_no")))
     wp.colSet("opt_rem_vouch_no","Y");

  if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
     wp.colSet("opt_acct_type_sel","Y");
  commAcctTypeSel("comm_acct_type_sel");
  commAcctTypeSel("comm_bef_acct_type_sel");

  wp.colSet("bef_acct_type_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"1"));
  if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
     wp.colSet("opt_acct_type_sel_cnt","Y");

  if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
     wp.colSet("opt_group_code_sel","Y");
  commGroupCodeSel("comm_group_code_sel");
  commGroupCodeSel("comm_bef_group_code_sel");

  wp.colSet("bef_group_code_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"2"));
  if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
     wp.colSet("opt_group_code_sel_cnt","Y");

  if (!wp.colStr("group_oppost_cond").equals(wp.colStr("bef_group_oppost_cond")))
     wp.colSet("opt_group_oppost_cond","Y");

  if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
     wp.colSet("opt_feedback_lmt","Y");

  if (!wp.colStr("res_flag").equals(wp.colStr("bef_res_flag")))
     wp.colSet("opt_res_flag","Y");
  commResFlag("comm_res_flag");
  commResFlag("comm_bef_res_flag");

  if (!wp.colStr("exec_s_months").equals(wp.colStr("bef_exec_s_months")))
     wp.colSet("opt_exec_s_months","Y");

  if (!wp.colStr("res_total_cnt").equals(wp.colStr("bef_res_total_cnt")))
     wp.colSet("opt_res_total_cnt","Y");

  if (!wp.colStr("move_cond").equals(wp.colStr("bef_move_cond")))
     wp.colSet("opt_move_cond","Y");
  commMoveCond("comm_move_cond");
  commMoveCond("comm_bef_move_cond");

  if (!wp.colStr("bil_mcht_cond").equals(wp.colStr("bef_bil_mcht_cond")))
     wp.colSet("opt_bil_mcht_cond","Y");

  if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
     wp.colSet("opt_merchant_sel","Y");
  commSelect1("comm_merchant_sel");
  commSelect1("comm_bef_merchant_sel");

  wp.colSet("bef_merchant_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"3"));
  if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
     wp.colSet("opt_merchant_sel_cnt","Y");

  if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
     wp.colSet("opt_mcht_group_sel","Y");
  commSelectB("comm_mcht_group_sel");
  commSelectB("comm_bef_mcht_group_sel");

  wp.colSet("bef_mcht_group_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"4"));
  if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
     wp.colSet("opt_mcht_group_sel_cnt","Y");

  if (!wp.colStr("issue_a_months").equals(wp.colStr("bef_issue_a_months")))
     wp.colSet("opt_issue_a_months","Y");

  if (!wp.colStr("mcode").equals(wp.colStr("bef_mcode")))
     wp.colSet("opt_mcode","Y");

  if (!wp.colStr("cancel_scope").equals(wp.colStr("bef_cancel_scope")))
     wp.colSet("opt_cancel_scope","Y");
  commCancelScope("comm_cancel_scope");
  commCancelScope("comm_bef_cancel_scope");

  if (!wp.colStr("cancel_rate").equals(wp.colStr("bef_cancel_rate")))
     wp.colSet("opt_cancel_rate","Y");

  if (!wp.colStr("cancel_event").equals(wp.colStr("bef_cancel_event")))
     wp.colSet("opt_cancel_event","Y");
  commCancelEvent("comm_cancel_event");
  commCancelEvent("comm_bef_cancel_event");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("fund_name","");
       wp.colSet("effect_months","");
       wp.colSet("stop_flag","");
       wp.colSet("list_cond","");
       wp.colSet("list_flag","");
       wp.colSet("list_flag_cnt","");
       wp.colSet("add_vouch_no","");
       wp.colSet("rem_vouch_no","");
       wp.colSet("acct_type_sel","");
       wp.colSet("acct_type_sel_cnt","");
       wp.colSet("group_code_sel","");
       wp.colSet("group_code_sel_cnt","");
       wp.colSet("group_oppost_cond","");
       wp.colSet("feedback_lmt","");
       wp.colSet("res_flag","");
       wp.colSet("exec_s_months","");
       wp.colSet("res_total_cnt","");
       wp.colSet("move_cond","");
       wp.colSet("bil_mcht_cond","");
       wp.colSet("merchant_sel","");
       wp.colSet("merchant_sel_cnt","");
       wp.colSet("mcht_group_sel","");
       wp.colSet("mcht_group_sel_cnt","");
       wp.colSet("issue_a_months","");
       wp.colSet("mcode","");
       wp.colSet("cancel_scope","");
       wp.colSet("cancel_rate","");
       wp.colSet("cancel_event","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("fund_name").length()==0)
     wp.colSet("opt_fund_name","Y");

  if (wp.colStr("effect_months").length()==0)
     wp.colSet("opt_effect_months","Y");

  if (wp.colStr("stop_flag").length()==0)
     wp.colSet("opt_stop_flag","Y");

  if (wp.colStr("list_cond").length()==0)
     wp.colSet("opt_list_cond","Y");

  if (wp.colStr("list_flag").length()==0)
     wp.colSet("opt_list_flag","Y");


  if (wp.colStr("add_vouch_no").length()==0)
     wp.colSet("opt_add_vouch_no","Y");

  if (wp.colStr("rem_vouch_no").length()==0)
     wp.colSet("opt_rem_vouch_no","Y");

  if (wp.colStr("acct_type_sel").length()==0)
     wp.colSet("opt_acct_type_sel","Y");


  if (wp.colStr("group_code_sel").length()==0)
     wp.colSet("opt_group_code_sel","Y");


  if (wp.colStr("group_oppost_cond").length()==0)
     wp.colSet("opt_group_oppost_cond","Y");

  if (wp.colStr("feedback_lmt").length()==0)
     wp.colSet("opt_feedback_lmt","Y");

  if (wp.colStr("res_flag").length()==0)
     wp.colSet("opt_res_flag","Y");

  if (wp.colStr("exec_s_months").length()==0)
     wp.colSet("opt_exec_s_months","Y");

  if (wp.colStr("res_total_cnt").length()==0)
     wp.colSet("opt_res_total_cnt","Y");

  if (wp.colStr("move_cond").length()==0)
     wp.colSet("opt_move_cond","Y");

  if (wp.colStr("bil_mcht_cond").length()==0)
     wp.colSet("opt_bil_mcht_cond","Y");

  if (wp.colStr("merchant_sel").length()==0)
     wp.colSet("opt_merchant_sel","Y");


  if (wp.colStr("mcht_group_sel").length()==0)
     wp.colSet("opt_mcht_group_sel","Y");


  if (wp.colStr("issue_a_months").length()==0)
     wp.colSet("opt_issue_a_months","Y");

  if (wp.colStr("mcode").length()==0)
     wp.colSet("opt_mcode","Y");

  if (wp.colStr("cancel_scope").length()==0)
     wp.colSet("opt_cancel_scope","Y");

  if (wp.colStr("cancel_rate").length()==0)
     wp.colSet("opt_cancel_rate","Y");

  if (wp.colStr("cancel_event").length()==0)
     wp.colSet("opt_cancel_event","Y");

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
   bnTable = "mkt_parm_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'MKT_LOAN_PARM' "
                ;
   if (wp.respHtml.equals("mktp4070_actp"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktp4070_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktp4070_aaa1"))
      wp.whereStr  += " and data_type  = '4' ";
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
   if (wp.respHtml.equals("mktp4070_actp"))
    commAcctType("comm_data_code");
   if (wp.respHtml.equals("mktp4070_gpcd"))
    commGroupCode("comm_data_code");
   if (wp.respHtml.equals("mktp4070_aaa1"))
    commMechtGp("comm_data_code");
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
   bnTable = "mkt_parm_data_t";

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
              + " and table_name  =  'MKT_LOAN_PARM' "
                ;
   if (wp.respHtml.equals("mktp4070_mrch"))
      wp.whereStr  += " and data_type  = '3' ";
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
  mktp02.Mktp4070Func func =new mktp02.Mktp4070Func(wp);

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
        if (rc==1) rc = func.dbInsertA4Imloan();
        if (rc==1) rc = func.dbDeleteD4TImloan();
        }
     else if (lsAudType[rr].equals("U"))
        {
        rc =func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4Bndata();
        if (rc==1) rc  = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc==1) rc  = func.dbDeleteD4Imloan();
        if (rc==1) rc  = func.dbInsertA4Imloan();
        if (rc==1) rc = func.dbDeleteD4TImloan();
        }
     else if (lsAudType[rr].equals("D"))
        {
         rc =func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        if (rc==1) rc = func.dbDeleteD4Imloan();
        if (rc==1) rc = func.dbDeleteD4TImloan();
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
       if ((wp.respHtml.equals("mktp4070")))
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
       if ((wp.respHtml.equals("mktp4070_actp")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp4070_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp4070_aaa1")))
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
  comCrtUser(s1,0);
  return;
 }
// ************************************************************************
 public void comCrtUser(String s1,int befType) throws Exception 
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
 public void commGroupCode(String s1) throws Exception 
 {
  commGroupCode(s1,0);
  return;
 }
// ************************************************************************
 public void commGroupCode(String s1,int befType) throws Exception 
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
 public void commListFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4","5","6"};
  String[] txt = {"身分證號","卡號","一卡通卡號","悠遊卡號","愛金卡號","TCB_ID(生日禮)"};
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
 public void commGroupCodeSel(String s1) throws Exception 
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
 public void commResFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"分次贈送期(月)數","抵用生效起始月數"};
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
 public void commMoveCond(String s1) throws Exception 
 {
  String[] cde = {"N","Y"};
  String[] txt = {"繼續存於現金回饋檔","撥入溢付款"};
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
 public void commCancelEvent(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"不限","有有效卡","有聯名卡有效卡"};
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
 String  listMktParmData(String s1,String s2,String s3,String s4) throws Exception
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
 String  listMktImloanList(String s1,String s2,String s3,String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where  fund_code = '" + s3 +"' "
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
          + " from sec_user a,mkt_loan_parm_t b "
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
          + " from mkt_loan_parm_t b "
          + " where   b.apr_flag = 'N' "
          + " group by b.fund_code "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
