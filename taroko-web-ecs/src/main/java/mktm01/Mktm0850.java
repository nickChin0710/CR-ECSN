/*************************************************************************************************
*                                                                                                *
*                              MODIFICATION LOG                                                  *
*                                                                                                *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                                      *
* ---------  --------  -----------    ---------------------------------------------------------- *
* 108/12/12  V1.00.01   Allen Ho      Initial                                                    *
* 109-04-27  V1.00.02  YangFang       updated for project coding standard                        *
* 109-08-12  V1.00.03   JustinWu      GetStr -> getStr                                           *
* 109-12-30  V1.00.05  shiyuqi        修改无意义命名                                                                                                                                                    *
* 110/1/4  V1.00.04  yanghan          修改了變量名稱和方法名稱                                                                                                                                  *
* 110/8/26 V1.00.05    Wendy Lu       修改與更新程式                                                                                                                                                    *
* 110-11-19  V1.00.05  Yangbo         joint sql replace to parameters way                        *    
* 111-07-26  V1.00.05  Machao         Bug處理                                                                                                                                                               *    
* 111-07-26  V1.00.05  Machao         详情页面bug处理                                                                                                                                               *
* 112-01-06  V1.00.06  Zuwei Su       增[交易平台種類], 增[特店名稱]                                     *
* 112-01-07  V1.00.07  Zuwei Su       [交易平台種類]取值改為Y和空                                                                                                                       *
* 112-01-17  V1.00.08  Zuwei Su       非新增刪除異常，[交易平台種類]取值改為[全部,指定,排除]                     *
* 112-02-02  V1.00.09  Zuwei Su       新增method commMchtCname,commMchtEname,listMktBnCData       *
* 112-02-16  V1.00.10  Zuwei Su       刪除[交易平台種類]選項，增加 [一般消費群組]選項                                                                              *
* 112-03-17  V1.00.11  Machao         增加 [通路類別]選項                                                                                                                                          *
* 112-05-16  V1.00.12  Ryan           增一般名單產檔格式、回饋周期 的參數設定                                                                                                    *
* 112-08-21  V1.00.13  Grace Huang    原ptr_sys_parm.wf_parm='OUTFILE_PARM', 改為 'INOUTFILE_PARM'  *
* 112-10-13  V1.00.14  Zuwei Su       增[消費累計基礎],增[當期帳單(年月)]  *
* 112-10-17  V1.00.15  Zuwei Su       [消費累計基礎]選消費期間需清空[當期帳單(年月)]  *
***************************************************************************************************/
package mktm01;

import mktm01.Mktm0850Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import java.util.Locale;

import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0850 extends BaseEdit {
  private String PROGNAME = "行銷通路活動回饋參數檔維護處理程式 112-10-17 V1.00.15";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0850Func func = null;
  String kk1;
  String km1;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_channel_parm";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[350];
  String[] uploadFileDat = new String[350];
  String[] logMsg = new String[20];
  String upGroupType = "0";

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      wp.itemSet("aud_type", "A");
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U3";
      updateFuncU3R();
    } else if (eqIgno(wp.buttonCode, "I")) {/* 單獨新鄒功能 */
      strAction = "I";
      /*
       * kk1 = item_kk("data_k1"); kk2 = item_kk("data_k2"); kk3 = item_kk("data_k3");
       */
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFuncD3R();
    } else if (eqIgno(wp.buttonCode, "R4")) {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
    } else if (eqIgno(wp.buttonCode, "U4")) {/* 明細更新 */
      strAction = "U4";
      updateFuncU4();
    } else if (eqIgno(wp.buttonCode, "R2")) {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
    } else if (eqIgno(wp.buttonCode, "U2")) {/* 明細更新 */
      strAction = "U2";
      updateFuncU2();
    } else if (eqIgno(wp.buttonCode, "R3")) {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
    } else if (eqIgno(wp.buttonCode, "U3")) {/* 明細更新 */
      strAction = "U3";
      updateFuncU3();
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
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD2")) {/* 匯入檔案 */
      procUploadFile(2);
      checkButtonOff();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    } else if (eqIgno(wp.buttonCode, "AJAX")) {/* AJAX */
        strAction = "AJAX";
        getWfValue2();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code,"
            + "a.active_name,"
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.record_cond,"
            + "a.cal_def_date,"
            + "a.feedback_apr_date,"
            + "a.crt_user,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by ACTIVE_CODE desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    kk1 = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    

    	wp.selectSQL = "hex(a.rowid) as rowid,"
                + " nvl(a.mod_seqno,0) as mod_seqno, "
                + "a.active_code as active_code,"
                + "a.apr_flag,"
                + "a.active_name,"
                + "a.stop_flag,"
                + "a.stop_date,"
                + "a.feedback_apr_date,"
                + "a.feedback_date,"
                + "a.bonus_type_cond,"
                + "a.bonus_type,"
                + "a.tax_flag,"
                + "a.b_effect_months,"
//                + "a.bonus_date,"
                + "a.fund_code_cond,"
                + "a.fund_code,"
//                + "a.fund_date,"
                + "a.f_effect_months,"
                + "a.other_type_cond,"
                + "a.spec_gift_no,"
                + "a.gift_date,"
                + "a.send_msg_pgm,"
                + "a.lottery_cond,"
                + "a.lottery_type,"
//                + "a.lottery_date,"
                + "a.prog_msg_pgm,"
                + "a.purchase_date_s,"
                + "a.purchase_date_e,"
//                + "a.cal_def_date,"
                + "a.list_cond,"
                + "a.list_flag,"
                + "a.list_use_sel,"
                + "'' as list_flag_cnt,"
                + "a.acct_type_sel,"
                + "'' as acct_type_sel_cnt,"
                + "a.group_code_sel,"
                + "'' as group_code_sel_cnt,"
                + "a.mcc_code_sel,"
                + "'' as mcc_code_sel_cnt,"
                + "a.merchant_sel,"
                + "'' as merchant_sel_cnt,"
                + "a.mcht_group_sel,"
                + "'' as mcht_group_sel_cnt,"
                + "a.mcht_cname_sel,"
                + "'' as mcht_cname_sel_cnt,"
                + "a.mcht_ename_sel,"
                + "'' as mcht_ename_sel_cnt,"
                + "a.it_term_sel,"
                + "'' as it_term_sel_cnt,"
                + "a.terminal_id_sel,"
                + "'' as terminal_id_sel_cnt,"
                + "a.pos_entry_sel,"
                + "'' as pos_entry_sel_cnt,"
                + "a.platform_kind_sel,"
                + "'' as platform_kind_sel_cnt,"
                + "a.platform_group_sel,"
                + "'' as platform_group_sel_cnt,"
                + "a.channel_type_sel,"
                + "'' as channel_type_sel_cnt,"
                + "a.bl_cond,"
                + "a.ca_cond,"
                + "a.it_cond,"
                + "a.it_flag,"
                + "a.id_cond,"
                + "a.ao_cond,"
                + "a.ot_cond,"
                + "a.minus_txn_cond,"
                + "a.block_cond,"
                + "a.oppost_cond,"
                + "a.payment_rate_cond,"
                + "a.record_cond,"
                + "a.feedback_key_sel,"
                + "a.per_amt_cond,"
                + "a.per_amt,"
                + "a.perday_cnt_cond,"
                + "a.perday_cnt,"
                + "a.sum_amt_cond,"
                + "a.sum_amt,"
                + "a.sum_cnt_cond,"
                + "a.sum_cnt,"
                + "a.above_cond,"
                + "a.above_amt,"
                + "a.above_cnt,"
                + "a.max_cnt_cond,"
                + "a.max_cnt,"
                + "a.purchase_type_sel,"
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
                + "a.feedback_lmt_amt_5,"
                + "a.b_feedback_limit,"
                + "a.f_feedback_limit,"
                + "a.s_feedback_limit,"
                + "a.l_feedback_limit,"
                + "a.b_feedback_cnt_limit,"
                + "a.f_feedback_cnt_limit,"
                + "a.s_feedback_cnt_limit,"
                + "a.l_feedback_cnt_limit,"
                + "a.crt_date,"
                + "a.crt_user,"
                + "a.apr_date,"
                + "a.apr_user,"
                + "a.prog_code,"
                + "a.accumulate_term_sel,"
                + "a.acct_month,"
                + "decode(a.feedback_cycle,'M',a.cal_def_date,'') as cal_def_date_m, "
                + "decode(a.feedback_cycle,'D',a.cal_def_date,'') as cal_def_date, "
                + "feedback_cycle, "
                + "a.feedback_dd,"
                + "a.outfile_type ";
    	
    if (wp.itemStr("ex_apr_flag").equals("Y")) {
    	wp.selectSQL = wp.selectSQL + ", a.bonus_date," + "a.fund_date," + "a.lottery_date " ;
    }
    
    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(km1, "a.active_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(kk1, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }


    commAprFlag2("comm_apr_flag");
    listWkdata();
    checkButtonOff();
    km1 = wp.colStr("active_code");
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commfuncAudType("aud_type");
    dataReadR3R();
    getWfValue2();
//    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    String sql1 = "";
    if (wp.colStr("prog_code").length() > 0) {
      sql1 = "select " + " prog_code||'-'||prog_s_date as prog_code1 " + " from ibn_prog "
//          + " where prog_code='" + wp.colStr("prog_code") + "' ";
          + " where 1 = 1 " + sqlCol(wp.colStr("prog_code"), "prog_code");

      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        wp.colSet("prog_code1", sqlStr("prog_code1"));
      }
    }

    return;
  }

//************************************************************************
void listWkdataAft() throws Exception
{
 wp.colSet("list_flag_cnt" , listMktImchannelList("mkt_imchannel_list_t","mkt_imchannel_list",wp.colStr("active_code"),""));
 wp.colSet("acct_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"1"));
 wp.colSet("group_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"2"));
 wp.colSet("mcc_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"5"));
 wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"3"));
 wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"6"));
 wp.colSet("mcht_cname_sel_cnt" , listMktBnCdata("mkt_bn_cdata_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"A"));
 wp.colSet("mcht_ename_sel_cnt" , listMktBnCdata("mkt_bn_cdata_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"B"));
 wp.colSet("it_term_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"4"));
 wp.colSet("terminal_id_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"7"));
 wp.colSet("pos_entry_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"8"));
 wp.colSet("platform_kind_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"9"));
 wp.colSet("platform_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"10"));
 wp.colSet("channel_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"15"));
}
//************************************************************************
void listWkdata() throws Exception
{
 wp.colSet("list_flag_cnt" , listMktImchannelList("mkt_imchannel_list","mkt_imchannel_list",wp.colStr("active_code"),""));
 wp.colSet("acct_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"1"));
 wp.colSet("group_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"2"));
 wp.colSet("mcc_code_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"5"));
 wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"3"));
 wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"6"));
 wp.colSet("mcht_cname_sel_cnt" , listMktBnCdata("mkt_bn_cdata","MKT_CHANNEL_PARM",wp.colStr("active_code"),"A"));
 wp.colSet("mcht_ename_sel_cnt" , listMktBnCdata("mkt_bn_cdata","MKT_CHANNEL_PARM",wp.colStr("active_code"),"B"));
 wp.colSet("it_term_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"4"));
 wp.colSet("terminal_id_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"7"));
 wp.colSet("pos_entry_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"8"));
 wp.colSet("platform_kind_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"9"));
 wp.colSet("platform_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"10"));
 wp.colSet("channel_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"15"));
}

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
//    controlTabName = orgTabName ;
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + " a.aud_type as aud_type, "
            + "a.active_code as active_code,"
            + "a.apr_flag as apr_flag,"
            + "a.active_name as active_name,"
            + "a.stop_flag as stop_flag,"
            + "a.stop_date as stop_date,"
            + "a.feedback_apr_date as feedback_apr_date,"
            + "a.feedback_date as feedback_date,"
            + "a.bonus_type_cond as bonus_type_cond,"
            + "a.bonus_type as bonus_type,"
            + "a.tax_flag as tax_flag,"
            + "a.b_effect_months as b_effect_months,"
//            + "a.bonus_date as bonus_date,"
            + "a.fund_code_cond as fund_code_cond,"
            + "a.fund_code as fund_code,"
//            + "a.fund_date as fund_date,"
            + "a.f_effect_months as f_effect_months,"
            + "a.other_type_cond as other_type_cond,"
            + "a.spec_gift_no as spec_gift_no,"
            + "a.gift_date as gift_date,"
            + "a.send_msg_pgm as send_msg_pgm,"
            + "a.lottery_cond as lottery_cond,"
            + "a.lottery_type as lottery_type,"
//            + "a.lottery_date as lottery_date,"
            + "a.prog_msg_pgm as prog_msg_pgm,"
            + "a.purchase_date_s as purchase_date_s,"
            + "a.purchase_date_e as purchase_date_e,"
//            + "a.cal_def_date as cal_def_date,"
            + "a.list_cond as list_cond,"
            + "a.list_flag as list_flag,"
            + "a.list_use_sel as list_use_sel,"
            + "'' as list_flag_cnt,"
            + "a.acct_type_sel as acct_type_sel,"
            + "'' as acct_type_sel_cnt,"
            + "a.group_code_sel as group_code_sel,"
            + "'' as group_code_sel_cnt,"
            + "a.mcc_code_sel as mcc_code_sel,"
            + "'' as mcc_code_sel_cnt,"
            + "a.merchant_sel as merchant_sel,"
            + "'' as merchant_sel_cnt,"
            + "a.mcht_group_sel as mcht_group_sel,"
            + "'' as mcht_group_sel_cnt,"
            + "a.mcht_cname_sel as mcht_cname_sel,"
            + "'' as mcht_cname_sel_cnt,"
            + "a.mcht_ename_sel as mcht_ename_sel,"
            + "'' as mcht_ename_sel_cnt,"
            + "a.it_term_sel as it_term_sel,"
            + "'' as it_term_sel_cnt,"
            + "a.terminal_id_sel as terminal_id_sel,"
            + "'' as terminal_id_sel_cnt,"
            + "a.pos_entry_sel as pos_entry_sel,"
            + "'' as pos_entry_sel_cnt,"
            + "a.platform_kind_sel as platform_kind_sel,"
            + "'' as platform_kind_sel_cnt,"
            + "a.platform_group_sel as platform_group_sel,"
            + "'' as platform_group_sel_cnt,"
            + "a.channel_type_sel as channel_type_sel,"
            + "'' as channel_type_sel_cnt,"
            + "a.bl_cond as bl_cond,"
            + "a.ca_cond as ca_cond,"
            + "a.it_cond as it_cond,"
            + "a.it_flag as it_flag,"
            + "a.id_cond as id_cond,"
            + "a.ao_cond as ao_cond,"
            + "a.ot_cond as ot_cond,"
            + "a.minus_txn_cond as minus_txn_cond,"
            + "a.block_cond as block_cond,"
            + "a.oppost_cond as oppost_cond,"
            + "a.payment_rate_cond as payment_rate_cond,"
            + "a.record_cond as record_cond,"
            + "a.feedback_key_sel as feedback_key_sel,"
            + "a.per_amt_cond as per_amt_cond,"
            + "a.per_amt as per_amt,"
            + "a.perday_cnt_cond as perday_cnt_cond,"
            + "a.perday_cnt as perday_cnt,"
            + "a.sum_amt_cond as sum_amt_cond,"
            + "a.sum_amt as sum_amt,"
            + "a.sum_cnt_cond as sum_cnt_cond,"
            + "a.sum_cnt as sum_cnt,"
            + "a.above_cond as above_cond,"
            + "a.above_amt as above_amt,"
            + "a.above_cnt as above_cnt,"
            + "a.max_cnt_cond as max_cnt_cond,"
            + "a.max_cnt as max_cnt,"
            + "a.purchase_type_sel as purchase_type_sel,"
            + "a.threshold_sel as threshold_sel,"
            + "a.purchase_amt_s1 as purchase_amt_s1,"
            + "a.purchase_amt_e1 as purchase_amt_e1,"
            + "a.active_type_1 as active_type_1,"
            + "a.feedback_rate_1 as feedback_rate_1,"
            + "a.feedback_amt_1 as feedback_amt_1,"
            + "a.feedback_lmt_cnt_1 as feedback_lmt_cnt_1,"
            + "a.feedback_lmt_amt_1 as feedback_lmt_amt_1,"
            + "a.purchase_amt_s2 as purchase_amt_s2,"
            + "a.purchase_amt_e2 as purchase_amt_e2,"
            + "a.active_type_2 as active_type_2,"
            + "a.feedback_rate_2 as feedback_rate_2,"
            + "a.feedback_amt_2 as feedback_amt_2,"
            + "a.feedback_lmt_cnt_2 as feedback_lmt_cnt_2,"
            + "a.feedback_lmt_amt_2 as feedback_lmt_amt_2,"
            + "a.purchase_amt_s3 as purchase_amt_s3,"
            + "a.purchase_amt_e3 as purchase_amt_e3,"
            + "a.active_type_3 as active_type_3,"
            + "a.feedback_rate_3 as feedback_rate_3,"
            + "a.feedback_amt_3 as feedback_amt_3,"
            + "a.feedback_lmt_cnt_3 as feedback_lmt_cnt_3,"
            + "a.feedback_lmt_amt_3 as feedback_lmt_amt_3,"
            + "a.purchase_amt_s4 as purchase_amt_s4,"
            + "a.purchase_amt_e4 as purchase_amt_e4,"
            + "a.active_type_4 as active_type_4,"
            + "a.feedback_rate_4 as feedback_rate_4,"
            + "a.feedback_amt_4 as feedback_amt_4,"
            + "a.feedback_lmt_cnt_4 as feedback_lmt_cnt_4,"
            + "a.feedback_lmt_amt_4 as feedback_lmt_amt_4,"
            + "a.purchase_amt_s5 as purchase_amt_s5,"
            + "a.purchase_amt_e5 as purchase_amt_e5,"
            + "a.active_type_5 as active_type_5,"
            + "a.feedback_rate_5 as feedback_rate_5,"
            + "a.feedback_amt_5 as feedback_amt_5,"
            + "a.feedback_lmt_cnt_5 as feedback_lmt_cnt_5,"
            + "a.feedback_lmt_amt_5 as feedback_lmt_amt_5,"
            + "a.b_feedback_limit as b_feedback_limit,"
            + "a.f_feedback_limit as f_feedback_limit,"
            + "a.s_feedback_limit as s_feedback_limit,"
            + "a.l_feedback_limit as l_feedback_limit,"
            + "a.b_feedback_cnt_limit as b_feedback_cnt_limit,"
            + "a.f_feedback_cnt_limit as f_feedback_cnt_limit,"
            + "a.s_feedback_cnt_limit as s_feedback_cnt_limit,"
            + "a.l_feedback_cnt_limit as l_feedback_cnt_limit,"
            + "a.crt_date as crt_date,"
            + "a.crt_user as crt_user,"
            + "a.apr_date as apr_date,"
            + "a.apr_user as apr_user,"
            + "a.prog_code as prog_code,"
            + "a.accumulate_term_sel,"
            + "a.acct_month,"
            + "decode(a.feedback_cycle,'M',a.cal_def_date,'') as cal_def_date_m, "
            + "decode(a.feedback_cycle,'D',a.cal_def_date,'') as cal_def_date, "
            + "a.feedback_cycle,"
            + "a.feedback_dd,"
            + "a.outfile_type ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(km1, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    if (wp.respHtml.indexOf("_detl") > 0)
        wp.colSet("btnStore_disable","");
     commAprFlag2("comm_apr_flag");
    wp.colSet("control_tab_name", controlTabName);
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdataAft();
    datareadWkdata();
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    km1 = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      km1 = wp.itemStr("active_code");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y")) {
        qFrom = 0;
        controlTabName = orgTabName;
      }
    } else {
      strAction = "A";
      wp.itemSet("aud_type", "D");
      insertFunc();
    }
    dataRead();
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void updateFuncU3R() throws Exception {
    qFrom = 0;
    km1 = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1) {
        dataReadR3R();
        datareadWkdata();
      }
    } else {
      km1 = wp.itemStr("active_code");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

//************************************************************************
public void dataReadR4() throws Exception
{
 dataReadR4(0);
}


// ************************************************************************
  public void dataReadR4(int fromType) throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_imchannel_list";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_imchannel_list_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, "
            + "ROW_NUMBER()OVER() as ser_num, "
            + "0 as r2_mod_seqno, "
            + "active_code, "
            + "mod_pgm  as r2_mod_user "
            ;
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  active_code = '" + wp.itemStr("active_code") + "'";
    whereCnt += sqlCol(wp.itemStr("active_code"), "active_code");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  active_code = :active_code ";
    setString("active_code", wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
  }

  // ************************************************************************
  public void updateFuncU4() throws Exception {}

//************************************************************************
  public void dataReadR2() throws Exception
{
  dataReadR2(0);
}

  // ************************************************************************
  public void dataReadR2(int fromType) throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, "
            + "ROW_NUMBER()OVER() as ser_num, "
            + "mod_seqno as r2_mod_seqno, "
            + "data_key, "
            + "data_code, "
            + "mod_user as r2_mod_user "
            ;
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_CHANNEL_PARM' ";
    if (wp.respHtml.equals("mktm0850_actp"))
      wp.whereStr += " and data_type  = '1' ";
    if (wp.respHtml.equals("mktm0850_gpcd"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("mktm0850_mccd"))
      wp.whereStr += " and data_type  = '5' ";
    if (wp.respHtml.equals("mktm0850_aaa1"))
      wp.whereStr += " and data_type  = '6' ";
    if (wp.respHtml.equals("mktm0850_ittr"))
      wp.whereStr += " and data_type  = '4' ";
    if (wp.respHtml.equals("mktm0850_term"))
      wp.whereStr += " and data_type  = '7' ";
    if (wp.respHtml.equals("mktm0850_posn"))
      wp.whereStr += " and data_type  = '8' ";
    if (wp.respHtml.equals("mktm0850_platformn"))
        wp.whereStr += " and data_type  = '9' ";
    if (wp.respHtml.equals("mktm0850_platformg"))
        wp.whereStr += " and data_type  = '10' ";
    if (wp.respHtml.equals("mktm0850_channel"))
        wp.whereStr += " and data_type  = '15' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("active_code") + "'";
    whereCnt += sqlCol(wp.itemStr("active_code"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm0850_actp"))
      commDataCode01("comm_data_code");
    if (wp.respHtml.equals("mktm0850_gpcd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm0850_mccd"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm0850_aaa1"))
      commDataCode34("comm_data_code");
    if (wp.respHtml.equals("mktm0850_posn"))
      commEntryMode("comm_data_code");
    if (wp.respHtml.equals("mktm0850_platformn"))
        commPlatformKind("comm_data_code");
    if (wp.respHtml.equals("mktm0850_platformg"))
        commDataCode34("comm_data_code");
    if (wp.respHtml.equals("mktm0850_channel"))
        commChannelType("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm01.Mktm0850Func func = new mktm01.Mktm0850Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2_flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2_flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm]))) {
          for (int intx = 0; intx < optData.length; intx++) {
            if (optData[intx].length() != 0)
              if (((ll + 1) == Integer.valueOf(optData[intx]))
                  || ((intm + 1) == Integer.valueOf(optData[intx]))) {
                del2_flag = 1;
                break;
              }
          }
          if (del2_flag == 1)
            break;

          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }
    }

    if (llErr > 0) {
      alertErr("資料值重複 : " + llErr);
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteD2() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    int deleteFlag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      deleteFlag = 0;
      // KEY 不可同時為空字串
      if ((empty(key1Data[ll])))
        continue;

      // -option-ON-
      for (int intm = 0; intm < optData.length; intm++) {
        if (optData[intm].length() != 0)
          if ((ll + 1) == Integer.valueOf(optData[intm])) {
            deleteFlag = 1;
            break;
          }
      }
      if (deleteFlag == 1)
        continue;

      func.varsSet("data_code", key1Data[ll]);

      if (func.dbInsertI2() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR2(1);
  }

//************************************************************************
public void dataReadR3() throws Exception
{
 dataReadR3(0);
}

  // ************************************************************************
  public void dataReadR3(int fromType) throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
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
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_CHANNEL_PARM' ";
    if (wp.respHtml.equals("mktm0850_mrcd"))
      wp.whereStr += " and data_type  = '3' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("active_code") + "'";
    whereCnt += sqlCol(wp.itemStr("active_code"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6,7 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    mktm01.Mktm0850Func func = new mktm01.Mktm0850Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");
    String[] key2Data = wp.itemBuff("data_code2");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2Flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2Flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm])) && (key2Data[ll].equals(key2Data[intm]))) {
          for (int intx = 0; intx < optData.length; intx++) {
            if (optData[intx].length() != 0)
              if (((ll + 1) == Integer.valueOf(optData[intx]))
                  || ((intm + 1) == Integer.valueOf(optData[intx]))) {
                del2Flag = 1;
                break;
              }
          }
          if (del2Flag == 1)
            break;

          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }
    }

    if (llErr > 0) {
      alertErr("資料值重複 : " + llErr);
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteD3() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    int deleteFlag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      deleteFlag = 0;
      // KEY 不可同時為空字串
      if ((empty(key1Data[ll])) && (empty(key2Data[ll])))
        continue;

      // -option-ON-
      for (int intm = 0; intm < optData.length; intm++) {
        if (optData[intm].length() != 0)
          if ((ll + 1) == Integer.valueOf(optData[intm])) {
            deleteFlag = 1;
            break;
          }
      }
      if (deleteFlag == 1)
        continue;

      func.varsSet("data_code", key1Data[ll]);
      func.varsSet("data_code2", key2Data[ll]);

      if (func.dbInsertI3() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR3(1);
  }
// ************************************************************************
 public void dataReadR5() throws Exception
 {
  dataReadR5(0);
 }
// ************************************************************************
 public void dataReadR5(int fromType) throws Exception
 {
   String bnTable="";

   if ((wp.itemStr("active_code").length()==0)||
       (wp.itemStr("aud_type").length()==0))
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
       bnTable = "mkt_bn_cdata";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_bn_cdata_t";
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
               + " and table_name  =  'MKT_CHANNEL_PARM' "
               ;
   if (wp.respHtml.equals("mktm0850_namc"))
      wp.whereStr  += " and data_type  = 'A' ";
   if (wp.respHtml.equals("mktm0850_name"))
      wp.whereStr  += " and data_type  = 'B' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("active_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
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
// ************************************************************************
 public void updateFuncU5() throws Exception
 {
   mktm01.Mktm0850Func func =new mktm01.Mktm0850Func(wp);
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
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1);

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm01.Mktm0850Func func = new mktm01.Mktm0850Func(wp);

    if (wp.respHtml.indexOf("_detl") > 0)
        if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();
    
    String busiDate = busiDate();
    wp.itemSet("busi_date", busiDate);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
        alertErr2(func.getMsg());
    }
    else
    {
     if (wp.respHtml.indexOf("_nadd") > 0)
    	 alertMsg("明細資料(帳戶/團體/特店類別等回饋條件資料), 請於主檔新增後維護/修改!");
    }

    this.sqlCommit(rc);

  }

  // ************************************************************************
  @Override
  public void initButton() {
    if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud();
    }
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktm0850_nadd")) || (wp.respHtml.equals("mktm0850_detl"))) {
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("bonus_type");
        }
        this.dddwList("dddw_bonus_type", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='BONUS_NAME'");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("fund_code").length() > 0) {
          wp.optionKey = wp.colStr("fund_code");
        }
        this.dddwList("dddw_func_code", "mkt_loan_parm", "trim(fund_code)", "trim(fund_name)",
            " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("spec_gift_no").length() > 0) {
          wp.optionKey = wp.colStr("spec_gift_no");
        }
        this.dddwList("dddw_spec_gift_no", "mkt_spec_gift", "trim(gift_no)", "trim(gift_name)",
            " where gift_group='2' and disable_flag='N'");
        wp.optionKey = "";
        wp.initOption ="--";
        if (wp.colStr("send_msg_pgm").length()>0)
           {
           wp.optionKey = wp.colStr("send_msg_pgm");
           }
        this.dddwList("dddw_send_mgm_pgm"
               ,"ptr_sys_idtab"
               ,"trim(wf_id)"
               ,"trim(wf_desc)"
               ," where wf_type='SMS_MSG_PGM' and wf_id like 'MktC185_%'");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("prog_code1").length() > 0) {
          wp.optionKey = wp.colStr("prog_code1");
        }
        lsSql = "";
        lsSql = procDynamicDddwProgCode();

        dddwList("dddw_prog_code", lsSql);

        wp.optionKey = "";
        wp.initOption ="--";
        if (wp.colStr("prog_msg_pgm").length()>0)
           {
           wp.optionKey = wp.colStr("prog_msg_pgm");
           }
        this.dddwList("dddw_send_mgm_pgm"
               ,"ptr_sys_idtab"
               ,"trim(wf_id)"
               ,"trim(wf_desc)"
               ," where wf_type='SMS_MSG_PGM' and wf_id like 'MktC185_%'");
        
        wp.optionKey = "";
        wp.initOption ="--";
        if (wp.colStr("outfile_type").length()>0)
           {
           wp.optionKey = wp.colStr("outfile_type");
           }
        this.dddwList("dddw_outfile_type"
               ,"ptr_sys_parm"
               ,"trim(wf_key)"
               ,"trim(wf_desc)"
               ," where wf_parm='INOUTFILE_PARM' and wf_key like 'MKTCHAN%'");
       }
      if ((wp.respHtml.equals("mktm0850_actp"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_acct_type", "vmkt_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm0850_gpcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm0850_mccd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm0850_aaa1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_Code34", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm0850_posn"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_entry_mode", "cca_entry_mode", "trim(entry_mode)",
            "trim(substr(mode_desc,1,30))", " where 1 = 1 ");
      }
//      if ((wp.respHtml.equals("mktm0850_platformn"))) {
//          wp.initOption = "";
//          wp.optionKey = "";
//          this.dddwList("dddw_platform_kind", "bil_platform", "trim(platform_kind)",
//              "trim(platform_desc)", " where 1 = 1 order by platform_kind ");
//        }

      if ((wp.respHtml.equals("mktm0850_platformg"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_platform_group", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where platform_flag='2' ");
      }
      
      if ((wp.respHtml.equals("mktm0850_channel"))) {
          wp.initOption = "";
          wp.optionKey = "";
          this.dddwList("dddw_channel_type", "mkt_chantype_parm", "trim(channel_type_id)", "trim(channel_type_desc)",
              " where 1 = 1 ");
        }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    return "";
  }

  // ************************************************************************
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

  // ************************************************************************
  public void commDataCode01(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select "
              + " chin_name as column_chin_name "
              + " from vmkt_acct_type "
              + " where 1 = 1 "
//              + " and   acct_type = '"+wp.colStr(ii,"data_code")+"'"
              + sqlCol(wp.colStr(ii,"data_code"), "acct_type")
              ;
      if (wp.colStr(ii, "data_code").length() == 0){
    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode04(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select "
              + " group_name as column_group_name "
              + " from ptr_group_code "
              + " where 1 = 1 "
//              + " and   group_code = '"+ wp.colStr(ii,"data_code")+"'"
              + sqlCol(wp.colStr(ii,"data_code"), "group_code")
              ;
      if (wp.colStr(ii, "data_code").length() == 0){

    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode07(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select "
              + " mcc_remark as column_mcc_remark "
              + " from cca_mcc_risk "
              + " where 1 = 1 "
//              + " and   mcc_code = '"+wp.colStr(ii,"data_code")+"'"
              + sqlCol(wp.colStr(ii,"data_code"), "mcc_code")
              ;
      if (wp.colStr(ii, "data_code").length() == 0){

    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcc_remark");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode34(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select "
              + " mcht_group_desc as column_mcht_group_desc "
              + " from mkt_mcht_gp "
              + " where 1 = 1 "
//              + " and   mcht_group_id = '"+ wp.colStr(ii,"data_code")+"'"
              + sqlCol(wp.colStr(ii,"data_code"), "mcht_group_id")
              ;
      if (wp.colStr(ii, "data_code").length() == 0){

    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }
  
  // ************************************************************************
  public void commChannelType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select "
              + " channel_type_desc as column_channel_type_desc "
              + " from mkt_chantype_parm "
              + " where 1 = 1 "
              + sqlCol(wp.colStr(ii,"data_code"), "channel_type_id")
              ;
      if (wp.colStr(ii, "data_code").length() == 0){

    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_channel_type_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commEntryMode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select "
              + " mode_desc as column_mode_desc "
              + " from cca_entry_mode "
              + " where 1 = 1 "
//              + " and   entry_mode = '"+wp.colStr(ii,"data_code")+"'"
              + sqlCol(wp.colStr(ii,"data_code"), "entry_mode")
              ;
      if (wp.colStr(ii, "data_code").length() == 0){

    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mode_desc");
      wp.colSet(ii, columnData1, columnData);
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
                    + " platform_desc as column_mode_desc "
                    + " from bil_platform "
                    + " where 1 = 1 "
                    + sqlCol(wp.colStr(ii, "data_code"), "platform_kind");
            if (wp.colStr(ii, "data_code").length() == 0) {
                wp.colSet(ii, columnData1, columnData);
                continue;
            }
            sqlSelect(sql1);
            sqlParm.clear();
            if (sqlRowNum > 0) {
                columnData = columnData + sqlStr("column_mode_desc");
            }
            wp.colSet(ii, columnData1, columnData);
        }
        return;
    }

//************************************************************************
public void commAprFlag2(String s1) throws Exception
{
 String[] cde = {"Y","N","U"};
 String[] txt = {"已覆核","待覆核","暫緩覆核"};
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
  public void procUploadFile(int loadType) throws Exception {
    if (wp.colStr(0, "ser_num").length() > 0)
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    if (wp.itemStr("zz_file_name").indexOf(".xls")!=-1)
    {
     alertErr2("上傳格式: 不可為 excel 格式");
     return;
    }
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    if (loadType == 2)
      fileDataImp2();
  }

  // ************************************************************************
  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
    } catch (Exception ex) {

      return -1;
    }

    return func.rc;
  }

  // ************************************************************************
  void fileDataImp2() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");

    if (fi == -1)
      return;

    String sysUpload_ype = wp.itemStr("sys_upload_type");
    String sysUploadAlias = wp.itemStr("sys_upload_alias");

    mktm01.Mktm0850Func func =new mktm01.Mktm0850Func(wp);

    if (sysUploadAlias.equals("list"))
       {
        // if has pre check procudure, write in here
        func.dbDeleteD2List("MKT_IMCHANNEL_LIST_T");
       }
    if (sysUploadAlias.equals("aaa1"))
       {
        // if has pre check procudure, write in here
        func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
       }
    if (sysUploadAlias.equals("aaat"))
       func.dbDeleteD2Aaat("MKT_BN_DATA_T");

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    tranSeqStr = comr.getSeqno("MKT_MODSEQ");

    String ss="";
    StringBuffer bfErrId = new StringBuffer();
    int llOk=0, llCnt=0, llErr=0, llChkErr=0;
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
      if (sysUploadAlias.equals("aaat"))
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
      if (sysUploadAlias.equals("aaat"))
         if (checkUploadfileAaat(ss)!=0) continue;
     llOk++;

     if (notifyCnt==0)
        {
         if (sysUploadAlias.equals("list"))
            {
             if (func.dbInsertI2List("MKT_IMCHANNEL_LIST_T",uploadFileCol,uploadFileDat) != 1) {
            	 bfErrId.append(uploadFileDat[0]).append(",");
            	 llErr++;;
             }
            }
         if (sysUploadAlias.equals("aaa1"))
            {
             if (func.dbInsertI2Aaa1("MKT_BN_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
            }
         if (sysUploadAlias.equals("aaat"))
            {
             if (func.dbInsertI2Aaat("MKT_BN_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
            }
        }
     }

    if (llErr!=0) notifyCnt=1;
    if (notifyCnt==1)
       {
        if (sysUploadAlias.equals("list"))
           func.dbDeleteD2List("MKT_IMCHANNEL_LIST_T");
        if (sysUploadAlias.equals("aaa1"))
           func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
        if (sysUploadAlias.equals("aaat"))
           func.dbDeleteD2Aaat("MKT_BN_DATA_T");
        func.dbInsertEcsNotifyLog(tranSeqStr,(llErr + llChkErr));
       }

    sqlCommit(1);  // 1:commit else rollback

    if (notifyCnt==0) 
    	alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 轉入");
    else {
    	String msg = "匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 不轉入";
    	if(sysUploadAlias.equals("list")){
    		msg += ",失敗ID = " + bfErrId.toString();
    		alertErr(msg);
    	}else
    		alertMsg(msg);
    }

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);


    return;
   }

  // ************************************************************************
  int checkUploadfileList(String tmpStr) throws Exception {
    mktm01.Mktm0850Func func = new mktm01.Mktm0850Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "list_data";

    // ======== [I]nsert table column ========
    uploadFileCol[1]  = "active_code";
    uploadFileCol[2]  = "list_flag";
    uploadFileCol[3]  = "id_p_seqno";
    uploadFileCol[4]  = "acct_type";
    uploadFileCol[5]  = "p_seqno";
    uploadFileCol[6]  = "card_no";
    uploadFileCol[7]  = "ori_card_no";
    uploadFileCol[8]  = "vd_flag";

    // ==== insert table content default =====
    uploadFileDat[1]  = wp.itemStr("active_code");
    uploadFileDat[2]  = wp.itemStr("list_flag");

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti=0;inti<1;inti++)
    {
     uploadFileDat[inti] = comm.getStr(tmpStr, inti+1 ,",");
     if (uploadFileDat[inti].length()!=0) okFlag=1;
    }
    if (okFlag == 0)
      return (1);
    //******************************************************************
    errorCnt=0;
    if (wp.itemStr("list_flag").equals("1")) return(0);
  /*
       if (select_crd_idno()!=0)
          {
           error_cnt=1;
           logMsg[0]               = "資料檢核錯誤";           // 原因說明
           logMsg[1]               = "3" ;                     // 錯誤類別
           logMsg[2]               = "1";                      // 欄位位置
           logMsg[3]               = uploadFileDat[0];         // 欄位內容
           logMsg[4]               = "信用卡無此卡人";         // 錯誤說明
           logMsg[5]               = "信用卡人檔無此 ID 資料"; // 欄位說明
           func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
           return(0);
          }
  */
    //******************************************************************
  /*
    if (wp.item_ss("list_flag").equals("6"))
       if (select_dbc_idno()!=0)
          {
           error_cnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3" ;                 // 錯誤類別
           logMsg[2]               = "1";                  // 欄位位置
           logMsg[3]               = uploadFileDat[0];     // 欄位內容
           logMsg[4]               = "VD無此卡人";           // 錯誤說明
           logMsg[5]               = "VD卡人檔無此 ID 資料"; // 欄位說明
           func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
           return(0);
          }
  */

    //******************************************************************
    if (wp.itemStr("list_flag").equals("2"))
       if (selectCrdCard()!=0)
       if (selectDbcCard()!=0)
          {
           errorCnt=1;
           logMsg[0]  = "資料內容錯誤";         // 原因說明
           logMsg[1]  = "2";                    // 錯誤類別
           logMsg[2]  = "1";                    // 欄位位置
           logMsg[3]  = uploadFileDat[0];       // 欄位內容
           logMsg[4]  = "VD無此卡號";          // 錯誤說明
           logMsg[5]  = "卡片檔無此卡號資料";     // 欄位說明

           logMsg[3]  = uploadFileDat[0];     // 欄位內容
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("3"))
       if (selectIpsCard()!=0)
          {
           errorCnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3";                    // 錯誤類別
           logMsg[2]               = "1";                    // 欄位位置
           logMsg[3]               = uploadFileDat[0];       // 欄位內容
           logMsg[4]               = "無此一卡通卡號";          // 錯誤說明
           logMsg[5]               = "一卡通卡片檔無此卡號資料";     // 欄位說明

           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("4"))
       if (selectTscCard()!=0)
          {
           errorCnt=1;
           logMsg[0] = "資料檢核錯誤";       // 原因說明
           logMsg[1] = "3" ;                 // 錯誤類別
           logMsg[2] = "1";                  // 欄位位置
           logMsg[3] = uploadFileDat[0];     // 欄位內容
           logMsg[4] = "無此悠遊卡號";
           logMsg[5] = "悠遊卡片檔無此卡號資料";
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("5"))
       if (selectIchCard()!=0)
          {
           errorCnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3" ;                 // 錯誤類別
           logMsg[2]               = "1";                  // 欄位位置
           logMsg[3]               = uploadFileDat[0];     // 欄位內容
           logMsg[4]               = "無此愛金卡號";
           logMsg[5]               = "愛金卡片檔無此卡號資料";
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************

    return 0;
   }

  // ************************************************************************
  int selectCrdCard() throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " acct_type, "
            + " p_seqno, "
            + " card_no, "
            + " ori_card_no "
            + " from crd_card "
            + " where card_no     = '"+ uploadFileDat[0]+ "' "
            ;
    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "N";
    return (0);
  }

  // ************************************************************************
  int selectDbcCard() throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " acct_type, "
            + " p_seqno, "
            + " card_no, "
            + " ori_card_no "
            + " from dbc_card "
            + " where card_no     = '"+ uploadFileDat[0]+ "' "
            ;
    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "Y";
    return (0);
  }

  // ************************************************************************
  int selectActAcno() throws Exception {
    wp.sqlCmd = " select " + " acct_type as acct_type " + " from act_acno "
        + " where p_seqno     = '" + uploadFileDat[3] + "' ";;
    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);

    return (0);
  }

  // ************************************************************************
  int selectDbaAcno() throws Exception {
    wp.sqlCmd = " select " + " acct_type as acct_type " + " from dba_acno "
        + " where p_seqno     = '" + uploadFileDat[3] + "' ";;
    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);

    return (0);
  }

  // ************************************************************************
  int selectCrdIdno() throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno "
            + " from crd_idno "
            + " where id_no       = '"+ uploadFileDat[0]+"' "
            ;
    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);
    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[8] = "N";
    return (0);
  }

  // ************************************************************************
  int selectDbcIdno() throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno "
            + " from dbc_idno "
            + " where id_no       = '"+ uploadFileDat[0]+"' "
            ;
    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[8] = "Y";
    return (0);
  }
//************************************************************************
int  selectIchCard() throws Exception
{
 wp.sqlCmd = " select "
           + " a.id_p_seqno, "
           + " a.acct_type, "
           + " a.p_seqno, "
           + " a.card_no, "
           + " a.ori_card_no "
           + " from crd_card a,ich_card b "
           + " where a.card_no     = b.card_no "
           + " and   ich_card_no   = '"+ uploadFileDat[0]+"' "
           ;
 this.sqlSelect();

 if (sqlRowNum <= 0) return(1);

 uploadFileDat[3] = sqlStr("id_p_seqno");
 uploadFileDat[4] = sqlStr("acct_type");
 uploadFileDat[5] = sqlStr("p_seqno");
 uploadFileDat[6] = sqlStr("card_no");
 uploadFileDat[7] = sqlStr("ori_card_no");
 uploadFileDat[8] = "N";
 return(0);
}
//************************************************************************
int  selectIpsCard() throws Exception
{
 wp.sqlCmd = " select "
           + " a.id_p_seqno, "
           + " a.acct_type, "
           + " a.p_seqno, "
           + " a.card_no, "
           + " a.ori_card_no "
           + " from crd_card a,ips_card b "
           + " where a.card_no     = b.card_no "
           + " and   ips_card_no   = '"+ uploadFileDat[0]+ "' "
           ;
 this.sqlSelect();

 if (sqlRowNum <= 0) return(1);

 uploadFileDat[3] = sqlStr("id_p_seqno");
 uploadFileDat[4] = sqlStr("acct_type");
 uploadFileDat[5] = sqlStr("p_seqno");
 uploadFileDat[6] = sqlStr("card_no");
 uploadFileDat[7] = sqlStr("ori_card_no");
 uploadFileDat[8] = "N";
 return(0);
}
//************************************************************************
int  selectTscCard() throws Exception
{
 wp.sqlCmd = " select "
           + " a.id_p_seqno, "
           + " a.acct_type, "
           + " a.p_seqno, "
           + " a.card_no, "
           + " a.ori_card_no "
           + " from crd_card a,tsc_card b "
           + " where a.card_no     = b.card_no "
           + " and   tsc_card_no   = '"+ uploadFileDat[0]+ "' "
           ;
 this.sqlSelect();

 if (sqlRowNum <= 0) return(1);

 uploadFileDat[3] = sqlStr("id_p_seqno");
 uploadFileDat[4] = sqlStr("acct_type");
 uploadFileDat[5] = sqlStr("p_seqno");
 uploadFileDat[6] = sqlStr("card_no");
 uploadFileDat[7] = sqlStr("ori_card_no");
 uploadFileDat[8] = "N";
 return(0);
}

  // ************************************************************************

  int checkUploadfileAaa1(String tmpStr) throws Exception {
    mktm01.Mktm0850Func func = new mktm01.Mktm0850Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "data_code";
    uploadFileCol[1] = "data_code2";

    // ======== [I]nsert table column ========
    uploadFileCol[2] = "table_name";
    uploadFileCol[3] = "data_key";
    uploadFileCol[4] = "data_type";

    // ==== insert table content default =====
    uploadFileDat[2] = "MKT_CHANNEL_PARM";
    uploadFileDat[3] = wp.itemStr("active_code");
    uploadFileDat[4] = "3";

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);

    if ((uploadFileDat[1].length()!=0)&&
    	      (uploadFileDat[1].length()<8))

    	  if (uploadFileDat[1].length()!=0)
    	      uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
    	                       + uploadFileDat[1];
    return 0;
  }

  // ************************************************************************
  int checkUploadfileAaat(String tmpStr) throws Exception {
    mktm01.Mktm0850Func func = new mktm01.Mktm0850Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "data_code";
    uploadFileCol[1] = "data_code2";

    // ======== [I]nsert table column ========
    uploadFileCol[2] = "table_name";
    uploadFileCol[3] = "data_key";
    uploadFileCol[4] = "data_type";

    // ==== insert table content default =====
    uploadFileDat[2] = "MKT_CHANNEL_PARM";
    uploadFileDat[3] = wp.itemStr("active_code");
    uploadFileDat[4] = "7";

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);

    return 0;
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
	  if (wp.colStr("acct_type_sel").length()==0)
	      wp.colSet("acct_type_sel" , "0");

	  if (wp.colStr("acct_type_sel").equals("0"))
	     {
	      buttonOff("btnactp_disable");
	     }
	  else
	     {
	      wp.colSet("btnactp_disable","");
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

	  if (wp.colStr("merchant_sel").length()==0)
	      wp.colSet("merchant_sel" , "0");

	  if (wp.colStr("merchant_sel").equals("0"))
	     {
	      buttonOff("btnmrcd_disable");
	      buttonOff("uplaaa1_disable");
	     }
	  else
	     {
	      wp.colSet("btnmrcd_disable","");
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

  if (wp.colStr("mcht_cname_sel").length()==0)
      wp.colSet("mcht_cname_sel" , "0");

  if (wp.colStr("mcht_cname_sel").equals("0"))
     {
      buttonOff("btnnamc_disable");
     }
  else
     {
      wp.colSet("btnnamc_disable","");
     }

  if (wp.colStr("mcht_ename_sel").length()==0)
      wp.colSet("mcht_ename_sel" , "0");

  if (wp.colStr("mcht_ename_sel").equals("0"))
     {
      buttonOff("btnname_disable");
     }
  else
     {
      wp.colSet("btnname_disable","");
     }

	  if (wp.colStr("it_term_sel").length()==0)
	      wp.colSet("it_term_sel" , "0");

	  if (wp.colStr("it_term_sel").equals("0"))
	     {
	      buttonOff("btnittr_disable");
	     }
	  else
	     {
	      wp.colSet("btnittr_disable","");
	     }

	  if (wp.colStr("terminal_id_sel").length()==0)
	      wp.colSet("terminal_id_sel" , "0");

	  if (wp.colStr("terminal_id_sel").equals("0"))
	     {
	      buttonOff("btnterm_disable");
	      buttonOff("uplaaat_disable");
	     }
	  else
	     {
	      wp.colSet("btnterm_disable","");
	      wp.colSet("uplaaat_disable","");
	     }

	  if (wp.colStr("pos_entry_sel").length()==0)
	      wp.colSet("pos_entry_sel" , "0");

	  if (wp.colStr("pos_entry_sel").equals("0"))
	     {
	      buttonOff("btnposn_disable");
	     }
	  else
	     {
	      wp.colSet("btnposn_disable","");
	     }

//	  if (wp.colStr("platform_kind_sel").length()==0)
//	      wp.colSet("platform_kind_sel" , "0");
//
//        if (wp.colStr("platform_kind_sel").equals("0")) {
//            wp.colSet("btnplatformn_disable", "");
//        } else {
//            buttonOff("btnplatformn_disable");
//        }

        if (wp.colStr("platform_group_sel").length()==0)
            wp.colSet("platform_group_sel" , "0");

          if (wp.colStr("platform_group_sel").equals("0")) {
              wp.colSet("btnplatformg_disable", "");
          } else {
              buttonOff("btnplatformg_disable");
          }
          
          if (wp.colStr("channel_type_sel").length()==0)
              wp.colSet("channel_type_sel" , "0");

            if (wp.colStr("channel_type_sel").equals("0")) {
                wp.colSet("btnchannel_disable", "");
            } else {
                buttonOff("btnchannel_disable");
            }

	  if ((wp.colStr("aud_type").equals("Y"))||
	      (wp.colStr("aud_type").equals("D")))
	     {
	      buttonOff("upllist_disable");
	      buttonOff("uplaaa1_disable");
	      buttonOff("uplaaat_disable");
	     }
	  else
	     {
	      wp.colSet("upllist_disable","");
	      wp.colSet("uplaaa1_disable","");
	      wp.colSet("uplaaat_disable","");
	     }
	  return;
  }

  // ************************************************************************
  @Override
  public void initPage() {

   if (wp.respHtml.equals("mktm0850_nadd"))
	      {
	   wp.colSet("feedback_key_sel" , "2");
	   wp.colSet("tax_flag", "N");
	   String busiDate = busiDate();
	   wp.colSet("acct_month", busiDate.substring(0, 6));
	      }

    buttonOff("btnlist_disable");
    buttonOff("btnactp_disable");
    buttonOff("btngpcd_disable");
    buttonOff("btnmccd_disable");
    buttonOff("btnmrcd_disable");
    buttonOff("btnaaa1_disable");
    buttonOff("btnnamc_disable");
    buttonOff("btnname_disable");
    buttonOff("btnittr_disable");
    buttonOff("btnterm_disable");
    buttonOff("btnposn_disable");
    buttonOff("btnplatformn_disable");
    buttonOff("btnplatformg_disable");
    return;
  }

  // ************************************************************************
  String procDynamicDddwProgCode() throws Exception {
    String lsSql = "";

    lsSql = " select "
    		  + " prog_code||'-'||prog_s_date as db_code, "
              + " prog_code||'('||substr(prog_desc,1,4)||')-'||prog_s_date as db_desc "
              + " from ibn_prog "
              + " where 1 = 1   ";

       if (wp.respHtml.equals("mktm0850_nadd"))
          {
           lsSql = lsSql
                  + " and  prog_flag='Y'  "
                  + " and  to_char(sysdate,'yyyymmdd') < prog_e_date ";
          }
       lsSql = lsSql
              + " order by prog_code,prog_s_date  "
              ;

    return lsSql;
 }

//************************************************************************
String  listMktBnData(String s1,String s2,String s3,String s4) throws Exception
{
 String sql1 = "select "
             + " count(*) as column_data_cnt "
             + " from "+ s1 + " "
             + " where 1 = 1 "
//             + " and   table_name = '"+s2+"'"
//             + " and   data_key   = '"+s3+"'"
//             + " and   data_type  = '"+s4+"'"
             + sqlCol(s2, "table_name")
             + sqlCol(s3, "data_key")
             + sqlCol(s4, "data_type")
             ;
 sqlSelect(sql1);

 if (sqlRowNum > 0) return(sqlStr("column_data_cnt"));

  return("0");
}
// ************************************************************************
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
//************************************************************************
String  listMktImchannelList(String s1,String s2,String s3,String s4) throws Exception
{
 String sql1 = "select "
             + " count(*) as column_data_cnt "
             + " from "+ s1 + " "
//             + " where  active_code = '" + s3 +"' "
             + " where 1 = 1 " + sqlCol(s3, "active_code")
             ;
 sqlSelect(sql1);

 if (sqlRowNum > 0) return(sqlStr("column_data_cnt"));

  return("0");
}
//************************************************************************

public void commCrtuser(String s1) throws Exception
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
//           + " and   usr_id = '"+wp.colStr(ii,"crt_user")+"'"
           + sqlCol(wp.colStr(ii,"crt_user"), "usr_id")
           ;
      if (wp.colStr(ii,"crt_user").length()==0)
         {
          wp.colSet(ii, s1, columnData);
          continue;
         }
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname");
      wp.colSet(ii, s1, columnData);
     }
  return;
}
//************************************************************************
public void commApruser(String s1) throws Exception
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
//           + " and   usr_id = '"+wp.colStr(ii,"apr_user")+"'"
           + sqlCol(wp.colStr(ii,"apr_user"), "usr_id")
           ;
      if (wp.colStr(ii,"apr_user").length()==0)
         {
          wp.colSet(ii, s1, columnData);
          continue;
         }
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname");
      wp.colSet(ii, s1, columnData);
     }
  return;
}

private void getWfValue2() throws Exception {
	String sql1 = "select " + " wf_key , wf_value2 " + " from ptr_sys_parm " + " where 1 = 1 and wf_parm='INOUTFILE_PARM' "
			+ " and wf_key = ? ";
	setString(1,"AJAX".equals(strAction)?wp.itemStr("outfile_type_json"):wp.colStr("outfile_type"));
	sqlSelect(sql1);
	String wfValue2 = "";
	if (sqlRowNum>0) {
		wfValue2 = sqlStr("wf_value2")
				.replaceAll("YYYYMMDD", wp.sysDate)
				.replaceAll("YYYYMM", strMid(wp.sysDate, 0, 6))
				.replaceAll("YYYMMDD", toTwDate(wp.sysDate));
		if("AJAX".equals(strAction))
			wp.addJSON("wf_value2_json", wfValue2);
		else
			wp.colSet("wf_value2", wfValue2);
	}
}

private String toTwDate(String strName) {
    String lsDate = strName.trim();
    if (lsDate.length() != 8)
      return lsDate;

    return String.format("%03d%s", (Integer.parseInt(lsDate.substring(0, 4)) - 1911),lsDate.substring(4, 8));
  }

private String busiDate() {
    String lsSql = "select business_date from ptr_businday ";
    sqlSelect(lsSql);
    String lsDate = sqlStr("business_date");
    return lsDate;
}
//************************************************************************
}  // End of class
