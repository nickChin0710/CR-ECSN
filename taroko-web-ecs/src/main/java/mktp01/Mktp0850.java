/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/26  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
* 110/11/22  V1.00.04  jiangyingdong       sql injection                                                                                *    
* 111-07-28  V1.00.05  machao          覆核Bug處理                                                                                       *
* 112-01-11  V1.00.06  Zuwei Su        配合mktm0850 [特店中文名稱]、[特店英文名稱]、[交易平台種類]  欄位增加而調整mktp0850明細資料               *
* 112-01-12  V1.00.07  Zuwei Su        增加Method commPlatformKind                                                                      *
* 112-01-17  V1.00.08  Zuwei Su        '基金' 字樣, 請改為'現金回饋', [交易平台種類]取值改為[全部,指定,排除]                                   *
* 112-02-02  V1.00.09  Zuwei Su        新增method commMchtCname,commMchtEname,listMktBnCData, dataProcess調用method dbDeleteD4BnCdata, dbDeleteD4TBnCdata, dbInsertA4BnCdata  *
* 112-02-16  V1.00.10  Zuwei Su        刪除[交易平台種類]選項，增加 [一般消費群組]選項                                                       *
* 112-03-16  V1.00.11  Machao         增加 [通路類別]選項                                                                                 *
* 112-04-24  V1.00.12  Grace Huang    commLotteryType(), 增'3.一般名單'                                                                  *
* 112-05-16  V1.00.13  Ryan           增一般名單產檔格式、回饋周期 的參數設定
* 112-10-13  V1.00.14  Zuwei Su       增[消費累計基礎],增[當期帳單(年月)]  *
****************************************************************************************************************************************/
package mktp01;

import mktp01.Mktp0850Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0850 extends BaseProc {
  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "行銷通路活動回饋參數檔覆核處理程式112/02/16 V1.00.10";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
//  mktp01.Mktp0850Func func = null;
  String kk1;
  String km1;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_channel_parm_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[350];
  String[] uploadFileDat = new String[350];
  String[] logMsg = new String[20];
  String upGroupType= "0";

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
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "R2"))
    {
    	strAction = "R2";
        dataReadR2();
    } else if (eqIgno(wp.buttonCode, "R3"))
    {
    	strAction = "R3";
        dataReadR3();
     } else if (eqIgno(wp.buttonCode, "R5")) {
         strAction = "R5";
         dataReadR5();
      } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")  + " and apr_flag='N'     ";

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

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.aud_type,"
            + "a.active_code,"
            + "a.active_name,"
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.stop_date,"
            + "a.record_cond,"
            + "a.crt_user,"
            + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by crt_date, crt_user, active_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      buttonOff("btnAdd_disable");
      return;
    }

    commCrtUser("comm_crt_user");
    commfuncAudType("aud_type");
    
    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

	kk1 = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (qFrom == 0)
      if (wp.itemStr("kk_active_code").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
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
            + "aud_type,"
            + "a.active_code as active_code,"
            + "a.crt_user,"
            + "a.active_name,"
            + "a.stop_flag,"
            + "a.stop_date,"
            + "a.bonus_type_cond,"
            + "a.bonus_type,"
            + "a.tax_flag,"
            + "a.b_effect_months,"
            + "a.fund_code_cond,"
            + "a.fund_code,"
            + "a.f_effect_months,"
            + "a.other_type_cond,"
            + "a.spec_gift_no,"
            + "a.send_msg_pgm,"
            + "a.lottery_cond,"
            + "a.lottery_type,"
            + "a.prog_code,"
            + "a.prog_msg_pgm, "
            + "a.accumulate_term_sel, "
            + "a.acct_month, "
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.cal_def_date,"
            + "a.list_cond,"
            + "a.list_flag,"
            + "a.list_use_sel,"
            + "a.acct_type_sel,"
            + "a.group_code_sel,"
            + "a.mcc_code_sel,"
            + "a.merchant_sel,"
            + "a.mcht_group_sel,"
            + "a.mcht_cname_sel,"
            + "a.mcht_ename_sel,"
            + "a.it_term_sel,"
            + "a.terminal_id_sel,"
            + "a.pos_entry_sel,"
            + "a.platform_kind_sel,"
            + "a.platform_group_sel,"
            + "a.channel_type_sel,"
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
            + "a.purchase_type_sel,"
            + "a.per_amt_cond,"
            + "a.per_amt,"
            + "a.perday_cnt_cond,"
            + "a.perday_cnt,"
            + "a.max_cnt_cond,"
            + "a.max_cnt,"
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
            + "a.l_feedback_cnt_limit,"
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
            + "a.feedback_cycle,"
            + "a.feedback_dd,"
            + "a.outfile_type ";

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
    commTaxFlag("comm_tax_flag");
    commLotteryType("comm_lottery_type");
    commListFlag("comm_list_flag");
    commListUse("comm_list_use_sel");
    commAcctType("comm_acct_type_sel");
    commGroupCode("comm_group_code_sel");
    commMccCode("comm_mcc_code_sel");
    commMerchant("comm_merchant_sel");
    commMchtGroup("comm_mcht_group_sel");
    commMchtCname("comm_mcht_cname_sel");
    commMchtEname("comm_mcht_ename_sel");
    commitItem("comm_it_term_sel");
    commTerminalId("comm_terminal_id_sel");
    commPosEntry("comm_pos_entry_sel");
    commPlatformKind("comm_platform_kind_sel");
    commMchtGroup("comm_platform_group_sel");
    commChannelType("comm_channel_type_sel");
    commItFlag("comm_it_flag");
    commFeedbackKey("comm_feedback_key_sel");
    commPurchaseType("comm_purchase_type_sel");
    commThreshold("comm_threshold_sel");
    commActiveType1("comm_active_type_1");
    commActiveType2("comm_active_type_2");
    commActiveType3("comm_active_type_3");
    commActiveType4("comm_active_type_4");
    commActiveType5("comm_active_type_5");
    commCrtUser("comm_crt_user");
    commBonusType("comm_bonus_type");
    commFundCode("comm_fund_code");
    commSpecGiftNo("comm_spec_gift_no");
    commSendMgmPgm("comm_send_msg_pgm");
    commProgCode("comm_prog_code");
    commProgMgmPgm("comm_prog_msg_pgm");    
    commFeedbackCycle("comm_feedback_cycle");  
    commOutfileType("comm_outfile_type");  
    commAccumulateTermSel("comm_accumulate_term_sel");  
    checkButtonOff();
    km1 = wp.colStr("active_code");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataReadR3R();
    else {
      commfuncAudType("aud_type");
      listWkdataSpace();
    }
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_CHANNEL_PARM";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code as active_code,"
            + "a.crt_user as bef_crt_user,"
            + "a.active_name as bef_active_name,"
            + "a.stop_flag as bef_stop_flag,"
            + "a.stop_date as bef_stop_date,"
            + "a.bonus_type_cond as bef_bonus_type_cond,"
            + "a.bonus_type as bef_bonus_type,"
            + "a.tax_flag as bef_tax_flag,"
            + "a.b_effect_months as bef_b_effect_months,"
            + "a.fund_code_cond as bef_fund_code_cond,"
            + "a.fund_code as bef_fund_code,"
            + "a.f_effect_months as bef_f_effect_months,"
            + "a.other_type_cond as bef_other_type_cond,"
            + "a.spec_gift_no as bef_spec_gift_no,"
            + "a.send_msg_pgm as bef_send_msg_pgm,"
            + "a.lottery_cond as bef_lottery_cond,"
            + "a.lottery_type as bef_lottery_type,"
            + "a.prog_code as bef_prog_code,"
            + "a.prog_msg_pgm as bef_prog_msg_pgm, "
            + "a.accumulate_term_sel as bef_accumulate_term_sel,"
            + "a.acct_month as bef_acct_month,"
            + "a.purchase_date_s as bef_purchase_date_s,"
            + "a.purchase_date_e as bef_purchase_date_e,"
            + "a.cal_def_date as bef_cal_def_date,"
            + "a.list_cond as bef_list_cond,"
            + "a.list_flag as bef_list_flag,"
            + "a.list_use_sel as bef_list_use_sel,"
            + "a.acct_type_sel as bef_acct_type_sel,"
            + "a.group_code_sel as bef_group_code_sel,"
            + "a.mcc_code_sel as bef_mcc_code_sel,"
            + "a.merchant_sel as bef_merchant_sel,"
            + "a.mcht_group_sel as bef_mcht_group_sel,"
            + "a.mcht_cname_sel as bef_mcht_cname_sel,"
            + "a.mcht_ename_sel as bef_mcht_ename_sel,"
            + "a.it_term_sel as bef_it_term_sel,"
            + "a.terminal_id_sel as bef_terminal_id_sel,"
            + "a.pos_entry_sel as bef_pos_entry_sel,"
            + "a.platform_kind_sel as bef_platform_kind_sel,"
            + "a.platform_group_sel as bef_platform_group_sel,"
            + "a.channel_type_sel as bef_channel_type_sel,"
            + "a.bl_cond as bef_bl_cond,"
            + "a.ca_cond as bef_ca_cond,"
            + "a.it_cond as bef_it_cond,"
            + "a.it_flag as bef_it_flag,"
            + "a.id_cond as bef_id_cond,"
            + "a.ao_cond as bef_ao_cond,"
            + "a.ot_cond as bef_ot_cond,"
            + "a.minus_txn_cond as bef_minus_txn_cond,"
            + "a.block_cond as bef_block_cond,"
            + "a.oppost_cond as bef_oppost_cond,"
            + "a.payment_rate_cond as bef_payment_rate_cond,"
            + "a.record_cond as bef_record_cond,"
            + "a.feedback_key_sel as bef_feedback_key_sel,"
            + "a.purchase_type_sel as bef_purchase_type_sel,"
            + "a.per_amt_cond as bef_per_amt_cond,"
            + "a.per_amt as bef_per_amt,"
            + "a.perday_cnt_cond as bef_perday_cnt_cond,"
            + "a.perday_cnt as bef_perday_cnt,"
            + "a.max_cnt_cond as bef_max_cnt_cond,"
            + "a.max_cnt as bef_max_cnt,"
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
            + "a.l_feedback_cnt_limit as bef_l_feedback_cnt_limit,"
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
            + "a.feedback_lmt_amt_5 as bef_feedback_lmt_amt_5,"
            + "a.feedback_cycle as bef_feedback_cycle,"
            + "a.feedback_dd as bef_feedback_dd,"
            + "a.outfile_type as bef_outfile_type ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(km1, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    if (wp.respHtml.indexOf("_detl") > 0) 
        wp.colSet("btnStore_disable",""); 
    
    commCrtUser("comm_crt_user");
    commBonusType("comm_bonus_type");
    commTaxFlag("comm_tax_flag");
    commFundCode("comm_fund_code");
    commSpecGiftNo("comm_spec_gift_no");
    commSendMgmPgm("comm_send_msg_pgm");
    commLotteryType("comm_lottery_type");
    commProgCode("comm_prog_code");
    commProgMgmPgm("comm_prog_msg_pgm");
    commListFlag("comm_list_flag");
    commListUse("comm_list_use_sel");
    commAcctType("comm_acct_type_sel");
    commGroupCode("comm_group_code_sel");
    commMccCode("comm_mcc_code_sel");
    commMerchant("comm_merchant_sel");
    commMchtGroup("comm_mcht_group_sel");
    commMchtCname("comm_mcht_cname_sel");
    commMchtEname("comm_mcht_ename_sel");
    commitItem("comm_it_term_sel");
    commTerminalId("comm_terminal_id_sel");
    commPosEntry("comm_pos_entry_sel");
    commPlatformKind("comm_platform_kind_sel");
    commMchtGroup("comm_platform_group_sel");
    commChannelType("comm_channel_type_sel");
    commItFlag("comm_it_flag");
    commFeedbackKey("comm_feedback_key_sel");
    commPurchaseType("comm_purchase_type_sel");
    commThreshold("comm_threshold_sel");
    commActiveType1("comm_active_type_1");
    commActiveType2("comm_active_type_2");
    commActiveType3("comm_active_type_3");
    commActiveType4("comm_active_type_4");
    commActiveType5("comm_active_type_5");
    commFeedbackCycle("comm_feedback_cycle");  
    commOutfileType("comm_outfile_type");  
    commAccumulateTermSel("comm_accumulate_term_sel");  
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
    listWkdataAft();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
	  wp.colSet("list_use_sel_cnt" , listMktImchannelList("mkt_imchannel_list_t","mkt_imchannel_list",wp.colStr("active_code"),""));
	  wp.colSet("acct_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"1"));
	  wp.colSet("group_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"2"));
	  wp.colSet("mcc_code_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"5"));
	  wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"3"));
	  wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"6"));
      wp.colSet("mcht_cname_sel_cnt" , listMktBnCData("mkt_bn_cdata_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"A"));
      wp.colSet("mcht_ename_sel_cnt" , listMktBnCData("mkt_bn_cdata_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"B"));
	  wp.colSet("it_term_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"4"));
	  wp.colSet("terminal_id_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"7"));
	  wp.colSet("pos_entry_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"8"));
      wp.colSet("platform_kind_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"9"));
      wp.colSet("platform_group_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"10"));
      wp.colSet("channel_type_sel_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANNEL_PARM",wp.colStr("active_code"),"15"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
      wp.colSet("opt_active_name", "Y");

    if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
      wp.colSet("opt_stop_flag", "Y");

    if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
      wp.colSet("opt_stop_date", "Y");

    if (!wp.colStr("bonus_type_cond").equals(wp.colStr("bef_bonus_type_cond")))
      wp.colSet("opt_bonus_type_cond", "Y");

    if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
      wp.colSet("opt_bonus_type", "Y");
    commBonusType("comm_bonus_type");
    commBonusType("comm_bef_bonus_type", 1);

    if (!wp.colStr("tax_flag").equals(wp.colStr("bef_tax_flag")))
      wp.colSet("opt_tax_flag", "Y");
    commTaxFlag("comm_tax_flag");
    commTaxFlag("comm_bef_tax_flag");

    if (!wp.colStr("b_effect_months").equals(wp.colStr("bef_b_effect_months")))
      wp.colSet("opt_b_effect_months", "Y");

    if (!wp.colStr("fund_code_cond").equals(wp.colStr("bef_fund_code_cond")))
      wp.colSet("opt_fund_code_cond", "Y");

    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code", "Y");
    commFundCode("comm_fund_code");
    commFundCode("comm_bef_fund_code", 1);

    if (!wp.colStr("f_effect_months").equals(wp.colStr("bef_f_effect_months")))
      wp.colSet("opt_f_effect_months", "Y");

    if (!wp.colStr("other_type_cond").equals(wp.colStr("bef_other_type_cond")))
      wp.colSet("opt_other_type_cond", "Y");

    if (!wp.colStr("spec_gift_no").equals(wp.colStr("bef_spec_gift_no")))
      wp.colSet("opt_spec_gift_no", "Y");
    commSpecGiftNo("comm_spec_gift_no");
    commSpecGiftNo("comm_bef_spec_gift_no", 1);

    if (!wp.colStr("send_msg_pgm").equals(wp.colStr("bef_send_msg_pgm")))
        wp.colSet("opt_send_msg_pgm","Y");
     commSendMgmPgm("comm_send_msg_pgm");
     commSendMgmPgm("comm_bef_send_msg_pgm",1);

    if (!wp.colStr("lottery_cond").equals(wp.colStr("bef_lottery_cond")))
      wp.colSet("opt_lottery_cond", "Y");

    if (!wp.colStr("lottery_type").equals(wp.colStr("bef_lottery_type")))
      wp.colSet("opt_lottery_type", "Y");
    commLotteryType("comm_lottery_type");
    commLotteryType("comm_bef_lottery_type");
    
    if (!wp.colStr("prog_code").equals(wp.colStr("bef_prog_code")))
        wp.colSet("opt_prog_code","Y");
     commProgCode("comm_prog_code");
     commProgCode("comm_bef_prog_code",1);

     if (!wp.colStr("prog_msg_pgm").equals(wp.colStr("bef_prog_msg_pgm")))
        wp.colSet("opt_prog_msg_pgm","Y");
     commProgMgmPgm("comm_prog_msg_pgm");
     commProgMgmPgm("comm_bef_prog_msg_pgm",1);
     
     if (!wp.colStr("accumulate_term_sel").equals(wp.colStr("bef_accumulate_term_sel")))
         wp.colSet("opt_accumulate_term_sel", "Y");
     commAccumulateTermSel("comm_accumulate_term_sel");  
     commAccumulateTermSel("comm_bef_accumulate_term_sel"); 
     if (!wp.colStr("acct_month").equals(wp.colStr("bef_acct_month")))
       wp.colSet("opt_acct_month", "Y");

    if (!wp.colStr("purchase_date_s").equals(wp.colStr("bef_purchase_date_s")))
      wp.colSet("opt_purchase_date_s", "Y");

    if (!wp.colStr("purchase_date_e").equals(wp.colStr("bef_purchase_date_e")))
      wp.colSet("opt_purchase_date_e", "Y");

    if (!wp.colStr("cal_def_date").equals(wp.colStr("bef_cal_def_date")))
      wp.colSet("opt_cal_def_date", "Y");

    if (!wp.colStr("list_cond").equals(wp.colStr("bef_list_cond")))
      wp.colSet("opt_list_cond", "Y");

    if (!wp.colStr("list_flag").equals(wp.colStr("bef_list_flag")))
      wp.colSet("opt_list_flag", "Y");
    commListFlag("comm_list_flag");
    commListFlag("comm_bef_list_flag");

    if (!wp.colStr("list_use_sel").equals(wp.colStr("bef_list_use_sel")))
      wp.colSet("opt_list_use_sel", "Y");
    commListUse("comm_list_use_sel");
    commListUse("comm_bef_list_use_sel");
    
    wp.colSet("bef_list_use_sel_cnt" , listMktImchannelList("mkt_imchannel_list","mkt_imchannel_list",wp.colStr("active_code"),""));
    if (!wp.colStr("list_use_sel_cnt").equals(wp.colStr("bef_list_use_sel_cnt")))
       wp.colSet("opt_list_use_sel_cnt","Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctType("comm_acct_type_sel");
    commAcctType("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "1"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCode("comm_group_code_sel");
    commGroupCode("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
      wp.colSet("opt_mcc_code_sel", "Y");
    commMccCode("comm_mcc_code_sel");
    commMccCode("comm_bef_mcc_code_sel");

    wp.colSet("bef_mcc_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "5"));
    if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
      wp.colSet("opt_mcc_code_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchant("comm_merchant_sel");
    commMerchant("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "3"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    commMchtGroup("comm_mcht_group_sel");
    commMchtGroup("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "6"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("mcht_cname_sel").equals(wp.colStr("bef_mcht_cname_sel")))
      wp.colSet("opt_mcht_cname_sel", "Y");
    commMchtCname("comm_mcht_cname_sel");
    commMchtCname("comm_bef_mcht_cname_sel");

    wp.colSet("bef_mcht_cname_sel_cnt",
    		listMktBnCData("mkt_bn_cdata", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "A"));
    if (!wp.colStr("mcht_cname_sel_cnt").equals(wp.colStr("bef_mcht_cname_sel_cnt")))
      wp.colSet("opt_mcht_cname_sel_cnt", "Y");

    if (!wp.colStr("mcht_ename_sel").equals(wp.colStr("bef_mcht_ename_sel")))
      wp.colSet("opt_mcht_ename_sel", "Y");
    commMchtEname("comm_mcht_ename_sel");
    commMchtEname("comm_bef_mcht_ename_sel");

    wp.colSet("bef_mcht_ename_sel_cnt",
    		listMktBnCData("mkt_bn_cdata", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "B"));
    if (!wp.colStr("mcht_ename_sel_cnt").equals(wp.colStr("bef_mcht_ename_sel_cnt")))
      wp.colSet("opt_mcht_ename_sel_cnt", "Y");
    
    if (!wp.colStr("it_term_sel").equals(wp.colStr("bef_it_term_sel")))
        wp.colSet("opt_it_term_sel","Y");
     commitItem("comm_it_term_sel");
     commitItem("comm_bef_it_term_sel");
     
     wp.colSet("bef_it_term_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"4"));
     if (!wp.colStr("it_term_sel_cnt").equals(wp.colStr("bef_it_term_sel_cnt")))
        wp.colSet("opt_it_term_sel_cnt","Y");

    if (!wp.colStr("terminal_id_sel").equals(wp.colStr("bef_terminal_id_sel")))
      wp.colSet("opt_terminal_id_sel", "Y");
    commTerminalId("comm_terminal_id_sel");
    commTerminalId("comm_bef_terminal_id_sel");

    wp.colSet("bef_terminal_id_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_CHANNEL_PARM", wp.colStr("active_code"), "7"));
    if (!wp.colStr("terminal_id_sel_cnt").equals(wp.colStr("bef_terminal_id_sel_cnt")))
      wp.colSet("opt_terminal_id_sel_cnt", "Y");
    
    if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
        wp.colSet("opt_pos_entry_sel","Y");
     commPosEntry("comm_pos_entry_sel");
     commPosEntry("comm_bef_pos_entry_sel");

     wp.colSet("bef_pos_entry_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"8"));
     if (!wp.colStr("pos_entry_sel_cnt").equals(wp.colStr("bef_pos_entry_sel_cnt")))
        wp.colSet("opt_pos_entry_sel_cnt","Y");
     
     if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
         wp.colSet("opt_platform_kind_sel","Y");
     commPlatformKind("comm_platform_kind_sel");
     commPlatformKind("comm_bef_platform_kind_sel");

      wp.colSet("bef_platform_kind_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"9"));
      if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
         wp.colSet("opt_platform_kind_sel_cnt","Y");
      
      if (!wp.colStr("platform_group_sel").equals(wp.colStr("bef_platform_group_sel")))
          wp.colSet("opt_platform_group_sel","Y");
      commPlatformKind("comm_platform_group_sel");
      commPlatformKind("comm_bef_platform_group_sel");

       wp.colSet("bef_platform_group_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"10"));
       if (!wp.colStr("platform_group_sel_cnt").equals(wp.colStr("bef_platform_group_sel_cnt")))
          wp.colSet("opt_platform_group_sel_cnt","Y");
       
       if (!wp.colStr("channel_type_sel").equals(wp.colStr("bef_channel_type_sel")))
           wp.colSet("opt_channel_type_sel","Y");
       commChannelType("comm_channel_type_sel");
       commChannelType("comm_bef_channel_type_sel");

        wp.colSet("bef_channel_type_sel_cnt" , listMktBnData("mkt_bn_data","MKT_CHANNEL_PARM",wp.colStr("active_code"),"15"));
        if (!wp.colStr("channel_type_sel_cnt").equals(wp.colStr("bef_channel_type_sel_cnt")))
           wp.colSet("opt_channel_type_sel_cnt","Y");

    if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
      wp.colSet("opt_bl_cond", "Y");

    if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
      wp.colSet("opt_ca_cond", "Y");

    if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
      wp.colSet("opt_it_cond", "Y");

    if (!wp.colStr("it_flag").equals(wp.colStr("bef_it_flag")))
      wp.colSet("opt_it_flag", "Y");
    commItFlag("comm_it_flag");
    commItFlag("comm_bef_it_flag");

    if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
      wp.colSet("opt_id_cond", "Y");

    if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
      wp.colSet("opt_ao_cond", "Y");

    if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
      wp.colSet("opt_ot_cond", "Y");

    if (!wp.colStr("minus_txn_cond").equals(wp.colStr("bef_minus_txn_cond")))
      wp.colSet("opt_minus_txn_cond", "Y");

    if (!wp.colStr("block_cond").equals(wp.colStr("bef_block_cond")))
      wp.colSet("opt_block_cond", "Y");

    if (!wp.colStr("oppost_cond").equals(wp.colStr("bef_oppost_cond")))
      wp.colSet("opt_oppost_cond", "Y");

    if (!wp.colStr("payment_rate_cond").equals(wp.colStr("bef_payment_rate_cond")))
      wp.colSet("opt_payment_rate_cond", "Y");

    if (!wp.colStr("record_cond").equals(wp.colStr("bef_record_cond")))
      wp.colSet("opt_record_cond", "Y");

    if (!wp.colStr("feedback_key_sel").equals(wp.colStr("bef_feedback_key_sel")))
      wp.colSet("opt_feedback_key_sel", "Y");
    commFeedbackKey("comm_feedback_key_sel");
    commFeedbackKey("comm_bef_feedback_key_sel");

    if (!wp.colStr("purchase_type_sel").equals(wp.colStr("bef_purchase_type_sel")))
      wp.colSet("opt_purchase_type_sel", "Y");
    commPurchaseType("comm_purchase_type_sel");
    commPurchaseType("comm_bef_purchase_type_sel");

    if (!wp.colStr("per_amt_cond").equals(wp.colStr("bef_per_amt_cond")))
      wp.colSet("opt_per_amt_cond", "Y");

    if (!wp.colStr("per_amt").equals(wp.colStr("bef_per_amt")))
      wp.colSet("opt_per_amt", "Y");

    if (!wp.colStr("perday_cnt_cond").equals(wp.colStr("bef_perday_cnt_cond")))
      wp.colSet("opt_perday_cnt_cond", "Y");

    if (!wp.colStr("perday_cnt").equals(wp.colStr("bef_perday_cnt")))
      wp.colSet("opt_perday_cnt", "Y");

    if (!wp.colStr("max_cnt_cond").equals(wp.colStr("bef_max_cnt_cond")))
      wp.colSet("opt_max_cnt_cond", "Y");

    if (!wp.colStr("max_cnt").equals(wp.colStr("bef_max_cnt")))
      wp.colSet("opt_max_cnt", "Y");

    if (!wp.colStr("sum_amt_cond").equals(wp.colStr("bef_sum_amt_cond")))
      wp.colSet("opt_sum_amt_cond", "Y");

    if (!wp.colStr("sum_amt").equals(wp.colStr("bef_sum_amt")))
      wp.colSet("opt_sum_amt", "Y");

    if (!wp.colStr("sum_cnt_cond").equals(wp.colStr("bef_sum_cnt_cond")))
      wp.colSet("opt_sum_cnt_cond", "Y");

    if (!wp.colStr("sum_cnt").equals(wp.colStr("bef_sum_cnt")))
      wp.colSet("opt_sum_cnt", "Y");

    if (!wp.colStr("above_cond").equals(wp.colStr("bef_above_cond")))
      wp.colSet("opt_above_cond", "Y");

    if (!wp.colStr("above_amt").equals(wp.colStr("bef_above_amt")))
      wp.colSet("opt_above_amt", "Y");

    if (!wp.colStr("above_cnt").equals(wp.colStr("bef_above_cnt")))
      wp.colSet("opt_above_cnt", "Y");

    if (!wp.colStr("b_feedback_limit").equals(wp.colStr("bef_b_feedback_limit")))
      wp.colSet("opt_b_feedback_limit", "Y");

    if (!wp.colStr("f_feedback_limit").equals(wp.colStr("bef_f_feedback_limit")))
      wp.colSet("opt_f_feedback_limit", "Y");

    if (!wp.colStr("s_feedback_limit").equals(wp.colStr("bef_s_feedback_limit")))
      wp.colSet("opt_s_feedback_limit", "Y");

    if (!wp.colStr("l_feedback_limit").equals(wp.colStr("bef_l_feedback_limit")))
      wp.colSet("opt_l_feedback_limit", "Y");

    if (!wp.colStr("b_feedback_cnt_limit").equals(wp.colStr("bef_b_feedback_cnt_limit")))
      wp.colSet("opt_b_feedback_cnt_limit", "Y");

    if (!wp.colStr("f_feedback_cnt_limit").equals(wp.colStr("bef_f_feedback_cnt_limit")))
      wp.colSet("opt_f_feedback_cnt_limit", "Y");

    if (!wp.colStr("s_feedback_cnt_limit").equals(wp.colStr("bef_s_feedback_cnt_limit")))
      wp.colSet("opt_s_feedback_cnt_limit", "Y");

    if (!wp.colStr("l_feedback_cnt_limit").equals(wp.colStr("bef_l_feedback_cnt_limit")))
      wp.colSet("opt_l_feedback_cnt_limit", "Y");

    if (!wp.colStr("threshold_sel").equals(wp.colStr("bef_threshold_sel")))
      wp.colSet("opt_threshold_sel", "Y");
    commThreshold("comm_threshold_sel");
    commThreshold("comm_bef_threshold_sel");

    if (!wp.colStr("purchase_amt_s1").equals(wp.colStr("bef_purchase_amt_s1")))
      wp.colSet("opt_purchase_amt_s1", "Y");

    if (!wp.colStr("purchase_amt_e1").equals(wp.colStr("bef_purchase_amt_e1")))
      wp.colSet("opt_purchase_amt_e1", "Y");

    if (!wp.colStr("active_type_1").equals(wp.colStr("bef_active_type_1")))
      wp.colSet("opt_active_type_1", "Y");
    commActiveType1("comm_active_type_1");
    commActiveType1("comm_bef_active_type_1");

    if (!wp.colStr("feedback_rate_1").equals(wp.colStr("bef_feedback_rate_1")))
      wp.colSet("opt_feedback_rate_1", "Y");

    if (!wp.colStr("feedback_amt_1").equals(wp.colStr("bef_feedback_amt_1")))
      wp.colSet("opt_feedback_amt_1", "Y");

    if (!wp.colStr("feedback_lmt_cnt_1").equals(wp.colStr("bef_feedback_lmt_cnt_1")))
      wp.colSet("opt_feedback_lmt_cnt_1", "Y");

    if (!wp.colStr("feedback_lmt_amt_1").equals(wp.colStr("bef_feedback_lmt_amt_1")))
      wp.colSet("opt_feedback_lmt_amt_1", "Y");

    if (!wp.colStr("purchase_amt_s2").equals(wp.colStr("bef_purchase_amt_s2")))
      wp.colSet("opt_purchase_amt_s2", "Y");

    if (!wp.colStr("purchase_amt_e2").equals(wp.colStr("bef_purchase_amt_e2")))
      wp.colSet("opt_purchase_amt_e2", "Y");

    if (!wp.colStr("active_type_2").equals(wp.colStr("bef_active_type_2")))
      wp.colSet("opt_active_type_2", "Y");
    commActiveType2("comm_active_type_2");
    commActiveType2("comm_bef_active_type_2");

    if (!wp.colStr("feedback_rate_2").equals(wp.colStr("bef_feedback_rate_2")))
      wp.colSet("opt_feedback_rate_2", "Y");

    if (!wp.colStr("feedback_amt_2").equals(wp.colStr("bef_feedback_amt_2")))
      wp.colSet("opt_feedback_amt_2", "Y");

    if (!wp.colStr("feedback_lmt_cnt_2").equals(wp.colStr("bef_feedback_lmt_cnt_2")))
      wp.colSet("opt_feedback_lmt_cnt_2", "Y");

    if (!wp.colStr("feedback_lmt_amt_2").equals(wp.colStr("bef_feedback_lmt_amt_2")))
      wp.colSet("opt_feedback_lmt_amt_2", "Y");

    if (!wp.colStr("purchase_amt_s3").equals(wp.colStr("bef_purchase_amt_s3")))
      wp.colSet("opt_purchase_amt_s3", "Y");

    if (!wp.colStr("purchase_amt_e3").equals(wp.colStr("bef_purchase_amt_e3")))
      wp.colSet("opt_purchase_amt_e3", "Y");

    if (!wp.colStr("active_type_3").equals(wp.colStr("bef_active_type_3")))
      wp.colSet("opt_active_type_3", "Y");
    commActiveType3("comm_active_type_3");
    commActiveType3("comm_bef_active_type_3");

    if (!wp.colStr("feedback_rate_3").equals(wp.colStr("bef_feedback_rate_3")))
      wp.colSet("opt_feedback_rate_3", "Y");

    if (!wp.colStr("feedback_amt_3").equals(wp.colStr("bef_feedback_amt_3")))
      wp.colSet("opt_feedback_amt_3", "Y");

    if (!wp.colStr("feedback_lmt_cnt_3").equals(wp.colStr("bef_feedback_lmt_cnt_3")))
      wp.colSet("opt_feedback_lmt_cnt_3", "Y");

    if (!wp.colStr("feedback_lmt_amt_3").equals(wp.colStr("bef_feedback_lmt_amt_3")))
      wp.colSet("opt_feedback_lmt_amt_3", "Y");

    if (!wp.colStr("purchase_amt_s4").equals(wp.colStr("bef_purchase_amt_s4")))
      wp.colSet("opt_purchase_amt_s4", "Y");

    if (!wp.colStr("purchase_amt_e4").equals(wp.colStr("bef_purchase_amt_e4")))
      wp.colSet("opt_purchase_amt_e4", "Y");

    if (!wp.colStr("active_type_4").equals(wp.colStr("bef_active_type_4")))
      wp.colSet("opt_active_type_4", "Y");
    commActiveType4("comm_active_type_4");
    commActiveType4("comm_bef_active_type_4");

    if (!wp.colStr("feedback_rate_4").equals(wp.colStr("bef_feedback_rate_4")))
      wp.colSet("opt_feedback_rate_4", "Y");

    if (!wp.colStr("feedback_amt_4").equals(wp.colStr("bef_feedback_amt_4")))
      wp.colSet("opt_feedback_amt_4", "Y");

    if (!wp.colStr("feedback_lmt_cnt_4").equals(wp.colStr("bef_feedback_lmt_cnt_4")))
      wp.colSet("opt_feedback_lmt_cnt_4", "Y");

    if (!wp.colStr("feedback_lmt_amt_4").equals(wp.colStr("bef_feedback_lmt_amt_4")))
      wp.colSet("opt_feedback_lmt_amt_4", "Y");

    if (!wp.colStr("purchase_amt_s5").equals(wp.colStr("bef_purchase_amt_s5")))
      wp.colSet("opt_purchase_amt_s5", "Y");

    if (!wp.colStr("purchase_amt_e5").equals(wp.colStr("bef_purchase_amt_e5")))
      wp.colSet("opt_purchase_amt_e5", "Y");

    if (!wp.colStr("active_type_5").equals(wp.colStr("bef_active_type_5")))
      wp.colSet("opt_active_type_5", "Y");
    commActiveType5("comm_active_type_5");
    commActiveType5("comm_bef_active_type_5");

    if (!wp.colStr("feedback_rate_5").equals(wp.colStr("bef_feedback_rate_5")))
      wp.colSet("opt_feedback_rate_5", "Y");

    if (!wp.colStr("feedback_amt_5").equals(wp.colStr("bef_feedback_amt_5")))
      wp.colSet("opt_feedback_amt_5", "Y");

    if (!wp.colStr("feedback_lmt_cnt_5").equals(wp.colStr("bef_feedback_lmt_cnt_5")))
      wp.colSet("opt_feedback_lmt_cnt_5", "Y");

    if (!wp.colStr("feedback_lmt_amt_5").equals(wp.colStr("bef_feedback_lmt_amt_5")))
      wp.colSet("opt_feedback_lmt_amt_5", "Y");

    if (!wp.colStr("feedback_cycle").equals(wp.colStr("bef_feedback_cycle")))
        wp.colSet("opt_feedback_cycle", "Y");
    commFeedbackCycle("comm_feedback_cycle");  
    commFeedbackCycle("comm_bef_feedback_cycle");  
    if (!wp.colStr("feedback_dd").equals(wp.colStr("bef_feedback_dd")))
        wp.colSet("opt_feedback_dd", "Y");
    if (!wp.colStr("outfile_type").equals(wp.colStr("bef_outfile_type")))
        wp.colSet("opt_outfile_type", "Y");
    commOutfileType("comm_outfile_type"); 
    commOutfileType("comm_bef_outfile_type",1); 
    
    if (wp.colStr("aud_type").equals("D")) {
    	wp.colSet("active_name","");
        wp.colSet("stop_flag","");
        wp.colSet("stop_date","");
        wp.colSet("bonus_type_cond","");
        wp.colSet("bonus_type","");
        wp.colSet("tax_flag","");
        wp.colSet("b_effect_months","");
        wp.colSet("fund_code_cond","");
        wp.colSet("fund_code","");
        wp.colSet("f_effect_months","");
        wp.colSet("other_type_cond","");
        wp.colSet("spec_gift_no","");
        wp.colSet("send_msg_pgm","");
        wp.colSet("lottery_cond","");
        wp.colSet("lottery_type","");
        wp.colSet("prog_code","");
        wp.colSet("prog_msg_pgm","");
        wp.colSet("accumulate_term_sel","");
        wp.colSet("acct_month","");
        wp.colSet("purchase_date_s","");
        wp.colSet("purchase_date_e","");
        wp.colSet("cal_def_date","");
        wp.colSet("list_cond","");
        wp.colSet("list_flag","");
        wp.colSet("list_use_sel","");
        wp.colSet("list_use_sel_cnt","");
        wp.colSet("acct_type_sel","");
        wp.colSet("acct_type_sel_cnt","");
        wp.colSet("group_code_sel","");
        wp.colSet("group_code_sel_cnt","");
        wp.colSet("mcc_code_sel","");
        wp.colSet("mcc_code_sel_cnt","");
        wp.colSet("merchant_sel","");
        wp.colSet("merchant_sel_cnt","");
        wp.colSet("mcht_group_sel","");
        wp.colSet("mcht_group_sel_cnt","");
        wp.colSet("mcht_cname_sel","");
        wp.colSet("mcht_cname_sel_cnt","");
        wp.colSet("mcht_ename_sel","");
        wp.colSet("mcht_ename_sel_cnt","");
        wp.colSet("it_term_sel","");
        wp.colSet("it_term_sel_cnt","");
        wp.colSet("terminal_id_sel","");
        wp.colSet("terminal_id_sel_cnt","");
        wp.colSet("pos_entry_sel","");
        wp.colSet("pos_entry_sel_cnt","");
        wp.colSet("platform_kind_sel","");
        wp.colSet("platform_kind_sel_cnt","");
        wp.colSet("platform_group_sel","");
        wp.colSet("platform_group_sel_cnt","");
        wp.colSet("channel_type_sel","");
        wp.colSet("channel_type_sel_cnt","");
        wp.colSet("bl_cond","");
        wp.colSet("ca_cond","");
        wp.colSet("it_cond","");
        wp.colSet("it_flag","");
        wp.colSet("id_cond","");
        wp.colSet("ao_cond","");
        wp.colSet("ot_cond","");
        wp.colSet("minus_txn_cond","");
        wp.colSet("block_cond","");
        wp.colSet("oppost_cond","");
        wp.colSet("payment_rate_cond","");
        wp.colSet("record_cond","");
        wp.colSet("feedback_key_sel","");
        wp.colSet("purchase_type_sel","");
        wp.colSet("per_amt_cond","");
        wp.colSet("per_amt","");
        wp.colSet("perday_cnt_cond","");
        wp.colSet("perday_cnt","");
        wp.colSet("max_cnt_cond","");
        wp.colSet("max_cnt","");
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
        wp.colSet("l_feedback_cnt_limit","");
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
        
        wp.colSet("feedback_cycle","");
        wp.colSet("feedback_dd","");
        wp.colSet("outfile_type","");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
	  if (wp.colStr("active_name").length()==0)
		     wp.colSet("opt_active_name","Y");

		  if (wp.colStr("stop_flag").length()==0)
		     wp.colSet("opt_stop_flag","Y");

		  if (wp.colStr("stop_date").length()==0)
		     wp.colSet("opt_stop_date","Y");

		  if (wp.colStr("bonus_type_cond").length()==0)
		     wp.colSet("opt_bonus_type_cond","Y");

		  if (wp.colStr("bonus_type").length()==0)
		     wp.colSet("opt_bonus_type","Y");

		  if (wp.colStr("tax_flag").length()==0)
		     wp.colSet("opt_tax_flag","Y");

		  if (wp.colStr("b_effect_months").length()==0)
		     wp.colSet("opt_b_effect_months","Y");

		  if (wp.colStr("fund_code_cond").length()==0)
		     wp.colSet("opt_fund_code_cond","Y");

		  if (wp.colStr("fund_code").length()==0)
		     wp.colSet("opt_fund_code","Y");

		  if (wp.colStr("f_effect_months").length()==0)
		     wp.colSet("opt_f_effect_months","Y");

		  if (wp.colStr("other_type_cond").length()==0)
		     wp.colSet("opt_other_type_cond","Y");

		  if (wp.colStr("spec_gift_no").length()==0)
		     wp.colSet("opt_spec_gift_no","Y");

		  if (wp.colStr("send_msg_pgm").length()==0)
		     wp.colSet("opt_send_msg_pgm","Y");

		  if (wp.colStr("lottery_cond").length()==0)
		     wp.colSet("opt_lottery_cond","Y");

		  if (wp.colStr("lottery_type").length()==0)
		     wp.colSet("opt_lottery_type","Y");

		  if (wp.colStr("prog_code").length()==0)
		     wp.colSet("opt_prog_code","Y");

		  if (wp.colStr("prog_msg_pgm").length()==0)
		     wp.colSet("opt_prog_msg_pgm","Y");
          
          if (wp.colStr("accumulate_term_sel").length()==0)
                 wp.colSet("opt_accumulate_term_sel","Y");
          if (wp.colStr("acct_month").length()==0)
              wp.colSet("opt_acct_month","Y");

		  if (wp.colStr("purchase_date_s").length()==0)
		     wp.colSet("opt_purchase_date_s","Y");

		  if (wp.colStr("purchase_date_e").length()==0)
		     wp.colSet("opt_purchase_date_e","Y");

		  if (wp.colStr("cal_def_date").length()==0)
		     wp.colSet("opt_cal_def_date","Y");

		  if (wp.colStr("list_cond").length()==0)
		     wp.colSet("opt_list_cond","Y");

		  if (wp.colStr("list_flag").length()==0)
		     wp.colSet("opt_list_flag","Y");

		  if (wp.colStr("list_use_sel").length()==0)
		     wp.colSet("opt_list_use_sel","Y");


		  if (wp.colStr("acct_type_sel").length()==0)
		     wp.colSet("opt_acct_type_sel","Y");


		  if (wp.colStr("group_code_sel").length()==0)
		     wp.colSet("opt_group_code_sel","Y");


		  if (wp.colStr("mcc_code_sel").length()==0)
		     wp.colSet("opt_mcc_code_sel","Y");


		  if (wp.colStr("merchant_sel").length()==0)
		     wp.colSet("opt_merchant_sel","Y");


		  if (wp.colStr("mcht_group_sel").length()==0)
		     wp.colSet("opt_mcht_group_sel","Y");

          if (wp.colStr("mcht_cname_sel").length()==0)
             wp.colSet("opt_mcht_cname_sel","Y");
          
          if (wp.colStr("mcht_ename_sel").length()==0)
              wp.colSet("opt_mcht_ename_sel","Y");

		  if (wp.colStr("it_term_sel").length()==0)
		     wp.colSet("opt_it_term_sel","Y");


		  if (wp.colStr("terminal_id_sel").length()==0)
		     wp.colSet("opt_terminal_id_sel","Y");


		  if (wp.colStr("pos_entry_sel").length()==0)
		     wp.colSet("opt_pos_entry_sel","Y");

          if (wp.colStr("platform_kind_sel").length()==0)
             wp.colSet("opt_platform_kind_sel","Y");

          if (wp.colStr("platform_group_sel").length()==0)
             wp.colSet("opt_platform_group_sel","Y");
          
          if (wp.colStr("channel_type_sel").length()==0)
              wp.colSet("opt_channel_type_sel","Y");


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

		  if (wp.colStr("minus_txn_cond").length()==0)
		     wp.colSet("opt_minus_txn_cond","Y");

		  if (wp.colStr("block_cond").length()==0)
		     wp.colSet("opt_block_cond","Y");

		  if (wp.colStr("oppost_cond").length()==0)
		     wp.colSet("opt_oppost_cond","Y");

		  if (wp.colStr("payment_rate_cond").length()==0)
		     wp.colSet("opt_payment_rate_cond","Y");

		  if (wp.colStr("record_cond").length()==0)
		     wp.colSet("opt_record_cond","Y");

		  if (wp.colStr("feedback_key_sel").length()==0)
		     wp.colSet("opt_feedback_key_sel","Y");

		  if (wp.colStr("purchase_type_sel").length()==0)
		     wp.colSet("opt_purchase_type_sel","Y");

		  if (wp.colStr("per_amt_cond").length()==0)
		     wp.colSet("opt_per_amt_cond","Y");

		  if (wp.colStr("per_amt").length()==0)
		     wp.colSet("opt_per_amt","Y");

		  if (wp.colStr("perday_cnt_cond").length()==0)
		     wp.colSet("opt_perday_cnt_cond","Y");

		  if (wp.colStr("perday_cnt").length()==0)
		     wp.colSet("opt_perday_cnt","Y");

		  if (wp.colStr("max_cnt_cond").length()==0)
		     wp.colSet("opt_max_cnt_cond","Y");

		  if (wp.colStr("max_cnt").length()==0)
		     wp.colSet("opt_max_cnt","Y");

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

		  if (wp.colStr("l_feedback_cnt_limit").length()==0)
		     wp.colSet("opt_l_feedback_cnt_limit","Y");

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
		  
		  if (wp.colStr("feedback_cycle").length()==0)
			     wp.colSet("opt_feedback_cycle","Y");
		  if (wp.colStr("feedback_dd").length()==0)
			     wp.colSet("opt_feedback_dd","Y");
		  if (wp.colStr("outfile_type").length()==0)
			     wp.colSet("opt_outfile_type","Y");
  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    String lsUser="";
    mktp01.Mktp0850Func func = new mktp01.Mktp0850Func(wp);

    String[] lsActiveCode = wp.itemBuff("active_code");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsCrtUser = wp.itemBuff("crt_user");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
    	if (opt[ii].length()==0) continue;
        rr = (int) (this.toNum(opt[ii])%20 - 1);
        if (rr==-1) rr = 19;
        if (rr<0) continue;

      wp.colSet(rr, "ok_flag", "-");
      if (lsCrtUser[rr].equals(wp.loginUser)) {
        ilAuth++;
        wp.colSet(rr, "ok_flag", "F");
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
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
        if (rc == 1)
            rc = func.dbInsertA4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
        if (rc == 1)
          rc = func.dbInsertA4Dmlist();
        if (rc == 1)
          rc = func.dbDeleteD4TDmlist();
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
        if (rc == 1)
            rc = func.dbDeleteD4BnCdata();
        if (rc == 1)
            rc = func.dbInsertA4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
        if (rc == 1)
          rc = func.dbDeleteD4Dmlist();
        if (rc == 1)
          rc = func.dbInsertA4Dmlist();
        if (rc == 1)
          rc = func.dbDeleteD4TDmlist();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
        if (rc == 1)
            rc = func.dbDeleteD4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
        if (rc == 1)
          rc = func.dbDeleteD4Dmlist();
        if (rc == 1)
          rc = func.dbDeleteD4TDmlist();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
    	commCrtUser("comm_crt_user");
        commfuncAudType("aud_type");
        
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        func.dbDelete();
        this.sqlCommit(rc);
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + ilAuth);
    buttonOff("btnAdd_disable");
    
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
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
      if ((wp.respHtml.equals("mktp0850"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code"
                ,"mkt_channel_parm_t"
                ,"trim(active_code)"
                ,"trim(active_name)"
                ," where apr_flag='N'");
        
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_crt_user").length()>0)
           {
           wp.optionKey = wp.colStr("ex_crt_user");
           }
        lsSql = "";
        lsSql =  procDynamicDddwCrtuser1(wp.colStr("ex_crt_user"));
        wp.optionKey = wp.colStr("ex_crt_user");
        dddwList("dddw_crt_user_1", lsSql);        
      }
      
      if ((wp.respHtml.equals("mktp0850_actp")))
      {
       wp.initOption ="";
       wp.optionKey = "";
       this.dddwList("dddw_acct_type"
              ,"vmkt_acct_type"
              ,"trim(acct_type)"
              ,"trim(chin_name)"
              ," where 1 = 1 ");
      }
    if ((wp.respHtml.equals("mktp0850_gpcd")))
      {
       wp.initOption ="";
       wp.optionKey = "";
       this.dddwList("dddw_group_code3"
              ,"ptr_group_code"
              ,"trim(group_code)"
              ,"trim(group_name)"
              ," where 1 = 1 ");
      }
    if ((wp.respHtml.equals("mktp0850_mccd")))
      {
       wp.initOption ="";
       wp.optionKey = "";
       this.dddwList("dddw_data_code07"
              ,"cca_mcc_risk"
              ,"trim(mcc_code)"
              ,"trim(mcc_remark)"
              ," where 1 = 1 ");
      }
    if ((wp.respHtml.equals("mktp0850_aaa1")))
      {
       wp.initOption ="";
       wp.optionKey = "";
       this.dddwList("dddw_data_Code34"
              ,"mkt_mcht_gp"
              ,"trim(mcht_group_id)"
              ,"trim(mcht_group_desc)"
              ," where 1 = 1 ");
      }
    if ((wp.respHtml.equals("mktp0850_posn")))
      {
       wp.initOption ="";
       wp.optionKey = "";
       this.dddwList("dddw_entry_mode"
              ,"cca_entry_mode"
              ,"trim(entry_mode)"
              ,"trim(substr(mode_desc,1,30))"
              ," where 1 = 1 ");
      }      
      
    } catch (Exception ex) {
    }
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
  public void commBonusType(String type) throws Exception {
    commBonusType(type, 0);
    return;
  }

  // ************************************************************************
  public void commBonusType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_id = ? ";
      if (wp.colStr(ii, befStr + "bonus_type").length() == 0){
    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "bonus_type") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commFundCode(String code) throws Exception {
    commFundCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commFundCode(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " fund_name as column_fund_name " + " from mkt_loan_parm "
          + " where 1 = 1 " + " and   fund_code = ? ";
      if (wp.colStr(ii, befStr + "fund_code").length() == 0){
    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "fund_code") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_fund_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commSpecGiftNo(String giftNo) throws Exception {
    commSpecGiftNo(giftNo, 0);
    return;
  }

  // ************************************************************************
  public void commSpecGiftNo(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from mkt_spec_gift "
          + " where 1 = 1 " + " and   gift_no = ? ";
      if (wp.colStr(ii, befStr + "spec_gift_no").length() == 0){
    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "spec_gift_no") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_gift_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }
  
//************************************************************************
public void commSendMgmPgm(String columnData1) throws Exception 
{
	commSendMgmPgm(columnData1,0);
 return;
}
//************************************************************************
public void commSendMgmPgm(String columnData1,int bef_type) throws Exception 
{
 String columnData="";
 String sql1 = "";
 String befStr="";
 if (bef_type==1) befStr="bef_";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " wf_desc as column_wf_desc "
           + " from ptr_sys_idtab "
           + " where 1 = 1 "
           + " and   wf_id = ? "
           ;
      if (wp.colStr(ii,befStr+"send_msg_pgm").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
       sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"send_msg_pgm") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_wf_desc"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************
public void commProgCode(String columnData1) throws Exception 
{
 commProgCode(columnData1,0);
 return;
}
//************************************************************************
public void commProgCode(String columnData1,int bef_type) throws Exception 
{
 String columnData="";
 String sql1 = "";
 String befStr="";
 if (bef_type==1) befStr="bef_";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " prog_desc as column_prog_desc "
           + " from ibn_prog "
           + " where 1 = 1 "
           + " and   prog_code = ? "
           ;
      if (wp.colStr(ii,befStr+"prog_code").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
       sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"prog_code") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_prog_desc"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************
public void commProgMgmPgm(String columnData1) throws Exception 
{
 commProgMgmPgm(columnData1,0);
 return;
}
//************************************************************************
public void commProgMgmPgm(String columnData1,int bef_type) throws Exception 
{
 String columnData="";
 String sql1 = "";
 String befStr="";
 if (bef_type==1) befStr="bef_";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " wf_desc as column_wf_desc "
           + " from ptr_sys_idtab "
           + " where 1 = 1 "
           + " and   wf_id = ? "
           ;
      if (wp.colStr(ii,befStr+"prog_msg_pgm").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
       sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"prog_msg_pgm") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_wf_desc"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************

  // ************************************************************************
  public void commTaxFlag(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"應稅", "免稅"};
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
  public void commLotteryType(String cde1) throws Exception {
	  String[] cde = {"1","2","3"};
	  String[] txt = {"1.抽獎名單","豐富點數","一般名單"};
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
  public void commListFlag(String cde1) throws Exception {
	  String[] cde = {"1","2","3","4","5"};
	  String[] txt = {"身分證號","卡號","一卡通卡號","悠遊卡號","愛金卡號"};
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
  public void commListUse(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"指定", "排除"};
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
  public void commAcctType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"指定", "排除"};
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
  public void commGroupCode(String cde1) throws Exception {
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
  public void commMccCode(String cde1) throws Exception {
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
  public void commMerchant(String cde1) throws Exception {
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
  public void commMchtGroup(String cde1) throws Exception {
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
  public void commChannelType(String cde1) throws Exception {
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
  public void commPlatformKind(String cde1) throws Exception {
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
  
//************************************************************************
public void commitItem(String cde1) throws Exception 
{
 String[] cde = {"0","1","2"};
 String[] txt = {"全部","指定","排除"};
 String columnData="";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      for (int inti=0;inti<cde.length;inti++)
        {
         String txt1 = cde1.substring(5,cde1.length());
         if (wp.colStr(ii,txt1).equals(cde[inti]))
            {
              wp.colSet(ii, cde1, txt[inti]);
              break;
            }
        }
     }
  return;
}

  // ************************************************************************
  public void commTerminalId(String cde1) throws Exception {
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
  
//************************************************************************
public void commPosEntry(String cde1) throws Exception 
{
 String[] cde = {"0","1","2"};
 String[] txt = {"全部","指定","排除"};
 String columnData="";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      for (int inti=0;inti<cde.length;inti++)
        {
         String txt1 = cde1.substring(5,cde1.length());
         if (wp.colStr(ii,txt1).equals(cde[inti]))
            {
              wp.colSet(ii, cde1, txt[inti]);
              break;
            }
        }
     }
  return;
}

  // ************************************************************************
  public void commItFlag(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"總額", "單筆"};
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
  public void commFeedbackKey(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1:身份證號", "2:帳戶流水號", "3:正卡卡號", "4:卡號"};
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
  public void commPurchaseType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4", "5"};
    String[] txt = {"1.累積金額", "2.累積筆數", "3.日累積金額", "4.日累積筆數", "5.次消費金額"};
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
  public void commThreshold(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"1.級距式", "2.條件式"};
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
  public void commActiveType1(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利", "2.基金", "3.贈品", "4.名單"};
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
  public void commActiveType2(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利", "2.基金", "3.贈品", "4.名單"};
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
  public void commActiveType3(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利", "2.基金", "3.贈品", "4.名單"};
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
  public void commActiveType4(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利", "2.基金", "3.贈品", "4.名單"};
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
  public void commActiveType5(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利", "2.基金", "3.贈品", "4.名單"};
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
  public void commFeedbackCycle(String cde1) throws Exception {
    String[] cde = {"M", "D"};
    String[] txt = {"按月回饋", "固定分析日期"};
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
  public void checkButtonOff() throws Exception {
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
  String listMktBnData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = ? " + " and   data_key   = ? "
        + " and   data_type  = ? ";
    sqlSelect(sql1, new Object[] { tableName, dataKey, dataType });

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }

  // ************************************************************************
  String listMktBnCData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = ? " + " and   data_key   = ? "
        + " and   data_type  = ? ";
    sqlSelect(sql1, new Object[] { tableName, dataKey, dataType });

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }
  
//************************************************************************
String  listMktImchannelList(String s1,String s2,String s3,String s4) throws Exception
{
 String sql1 = "select "
             + " count(*) as column_data_cnt "
             + " from "+ s1 + " "
             + " where  active_code = ? "
             ;
  sqlSelect(sql1, new Object[] { s3 });

 if (sqlRowNum > 0) return(sqlStr("column_data_cnt"));

  return("0");
}
//************************************************************************
public String procDynamicDddwCrtuser1(String s1)  throws Exception
{
  String lsSql = "";

  lsSql = " select "
         + " b.crt_user as db_code, "
         + " max(b.crt_user||' '||a.usr_cname) as db_desc "
         + " from sec_user a,mkt_channel_parm_t b "
         + " where a.usr_id = b.crt_user "
         + " and   b.apr_flag = 'N' "
         + " group by b.crt_user "
         ;

  return lsSql;
}

//************************************************************************
public void commCrtUser(String s1) throws Exception 
{
commCrtUser(s1,0);
return;
}
//************************************************************************
public void commCrtUser(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " usr_cname as column_usr_cname "
         + " from sec_user "
         + " where 1 = 1 "
         + " and   usr_id = ? "
         ;
    if (wp.colStr(ii,befStr+"crt_user").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"crt_user") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_usr_cname"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}
// ************************************************************************
public void commAccumulateTermSel(String cde1) throws Exception {
  String[] cde = {"1", "2"};
  String[] txt = {"消費期間", "當期帳單"};
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

//************************************************************************
public void dataReadR2() throws Exception
{
dataReadR2(0);
}
//************************************************************************
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
           + " and table_name  =  'MKT_CHANNEL_PARM' "
             ;
if (wp.respHtml.equals("mktp0850_actp"))
   wp.whereStr  += " and data_type  = '1' ";
if (wp.respHtml.equals("mktp0850_gpcd"))
   wp.whereStr  += " and data_type  = '2' ";
if (wp.respHtml.equals("mktp0850_mccd"))
   wp.whereStr  += " and data_type  = '5' ";
if (wp.respHtml.equals("mktp0850_aaa1"))
   wp.whereStr  += " and data_type  = '6' ";
if (wp.respHtml.equals("mktp0850_ittr"))
   wp.whereStr  += " and data_type  = '4' ";
if (wp.respHtml.equals("mktp0850_term"))
   wp.whereStr  += " and data_type  = '7' ";
if (wp.respHtml.equals("mktp0850_posn"))
    wp.whereStr += " and data_type  = '8' ";
if (wp.respHtml.equals("mktp0850_platformn"))
    wp.whereStr += " and data_type  = '9' ";
if (wp.respHtml.equals("mktp0850_platformg"))
    wp.whereStr += " and data_type  = '10' ";
if (wp.respHtml.equals("mktp0850_channel"))
    wp.whereStr += " and data_type  = '15' ";
String whereCnt = wp.whereStr;
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key", wp.itemStr("active_code"));
//whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
whereCnt += " and  data_key = ? ";
params.clear();
params.add(wp.itemStr("active_code"));
wp.whereStr  += " order by 4,5,6 ";
int cnt1=selectBndataCount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
    alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    return;
   }

pageQuery();
wp.setListCount(1);
wp.notFound = "";

wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
if (wp.respHtml.equals("mktp0850_actp"))
 commDatacode01("comm_data_code");
if (wp.respHtml.equals("mktp0850_gpcd"))
 commDataCode04("comm_data_code");
if (wp.respHtml.equals("mktp0850_mccd"))
 commDatacode07("comm_data_code");
if (wp.respHtml.equals("mktp0850_aaa1"))
 commDatacode34("comm_data_code");
if (wp.respHtml.equals("mktp0850_posn"))
 commEntryMode("comm_data_code");
}

//************************************************************************
public int selectBndataCount(String bndata_table,String whereStr ) throws Exception
{
String sql1 = "select count(*) as bndataCount"
            + " from " + bndata_table
            + " " + whereStr
            ;

  sqlSelect(sql1, params.toArray(new Object[params.size()]));

return((int)sqlNum("bndataCount"));
}

//************************************************************************
public void commDatacode01(String s1) throws Exception 
{
	commDatacode01(s1,0);
return;
}
//************************************************************************
public void commDatacode01(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " chin_name as column_chin_name "
         + " from vmkt_acct_type "
         + " where 1 = 1 "
         + " and   acct_type = ? "
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"data_code") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_chin_name"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}

//************************************************************************
public void commDataCode04(String s1) throws Exception 
{
	commDataCode04(s1,0);
return;
}
//************************************************************************
public void commDataCode04(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " group_name as column_group_name "
         + " from ptr_group_code "
         + " where 1 = 1 "
         + " and   group_code = ? "
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"data_code") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_group_name"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}


//************************************************************************
public void commDatacode07(String s1) throws Exception 
{
	commDatacode07(s1,0);
return;
}
//************************************************************************
public void commDatacode07(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mcc_remark as column_mcc_remark "
         + " from cca_mcc_risk "
         + " where 1 = 1 "
         + " and   mcc_code = ? "
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"data_code") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcc_remark"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}


//************************************************************************
public void commDatacode34(String s1) throws Exception 
{
	commDatacode34(s1,0);
return;
}
//************************************************************************
public void commDatacode34(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mcht_group_desc as column_mcht_group_desc "
         + " from mkt_mcht_gp "
         + " where 1 = 1 "
         + " and   mcht_group_id = ? "
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"data_code") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcht_group_desc"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}

//************************************************************************
public void commEntryMode(String s1) throws Exception 
{
  commEntryMode(s1,0);
return;
}
//************************************************************************
public void commEntryMode(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mode_desc as column_mode_desc "
         + " from cca_entry_mode "
         + " where 1 = 1 "
         + " and   entry_mode = ? "
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"data_code") });

    if (sqlRowNum > 0)
       columnData = columnData + sqlStr("column_mode_desc"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}
//************************************************************************
public void commOutfileType(String s1) throws Exception 
{
	commOutfileType(s1,0);
return;
}
//************************************************************************
public void commOutfileType(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
 {
  columnData="";
  sql1 = "select "
       + " wf_desc as column_wf_desc "
       + " from ptr_sys_parm "
       + " where 1 = 1 "
       + " and wf_parm='OUTFILE_PARM' AND wf_key = ? "
       ;
  if (wp.colStr(ii,befStr+"outfile_type").length()==0)
     {
      wp.colSet(ii, s1, columnData);
      continue;
     }
   sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"outfile_type") });

  if (sqlRowNum>0)
     columnData = columnData + sqlStr("column_wf_desc"); 
  wp.colSet(ii, s1, columnData);
 }
return;
}
//************************************************************************
public void dataReadR3() throws Exception
{
dataReadR3(0);
}
//************************************************************************
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
           + " and table_name  =  'MKT_CHANNEL_PARM' "
             ;
if (wp.respHtml.equals("mktp0850_mrcd"))
   wp.whereStr  += " and data_type  = '3' ";
String whereCnt = wp.whereStr;
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key", wp.itemStr("active_code"));
//whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
whereCnt += " and  data_key = ? ";
params.clear();
params.add(wp.itemStr("active_code"));
wp.whereStr  += " order by 4,5,6,7 ";
int cnt1=selectBndataCount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
    alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
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
public void dataReadR5() throws Exception
{
dataReadR5(0);
}
//************************************************************************
    public void dataReadR5(int fromType) throws Exception {
        String bnTable = "";

        if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
            alertErr("鍵值為空白或主檔未新增 ");
            return;
        }
        wp.selectCnt = 1;
        this.selectNoLimit();
        if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
            buttonOff("btnUpdate_disable");
            buttonOff("newDetail_disable");
            bnTable = "mkt_bn_cdata";
        } else {
            wp.colSet("btnUpdate_disable", "");
            wp.colSet("newDetail_disable", "");
            bnTable = "mkt_bn_cdata_t";
        }

        wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "0 as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user ";
        wp.daoTable = bnTable;
        wp.whereStr = "where 1=1" + " and table_name  =  'MKT_CHANNEL_PARM' ";
        if (wp.respHtml.equals("mktp0850_namc"))
            wp.whereStr += " and data_type  = 'A' ";
        if (wp.respHtml.equals("mktp0850_name"))
            wp.whereStr += " and data_type  = 'B' ";
        String whereCnt = wp.whereStr;
        wp.whereStr += " and  data_key = :data_key ";
        setString("data_key", wp.itemStr("active_code"));
        whereCnt += " and  data_key = '" + wp.itemStr("active_code") + "'";
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

} // End of class
