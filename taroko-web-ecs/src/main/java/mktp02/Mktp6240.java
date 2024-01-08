/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/09  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-07-17  V1.00.03   shiyuqi        rename tableName &FiledName         *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名       
* 110-11-08  V1.00.03  machao     SQL Injection   
* 111-08-03  V1.00.03  machao     頁面bug調整         
* 111-11-08  V1.00.03  machao     變量名稱調整                                                                   *    
* 112-03-24  V1.00.04  Zuwei Su       增匯入名單3個欄位，table name修訂                                                                   *    
* 112/06/21  V1.00.05   Zuwei Su     欄位new_hldr_sel沒有寫入db                *
***************************************************************************/
package mktp02;

import mktp02.Mktp6240Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6240 extends BaseProc {
  private String PROGNAME = "首刷禮活動回饋參數覆核處理程式108/10/09 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6240Func func = null;
  String rowid;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_fstp_parm_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];

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
    } else if (eqIgno(wp.buttonCode, "R2"))
    {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
    } else if (eqIgno(wp.buttonCode, "R4"))
    {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
    } else if (eqIgno(wp.buttonCode, "R3"))
    {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
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
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date");

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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.aud_type," + "a.active_code," + "a.active_name," + "a.issue_date_s,"
        + "a.issue_date_e," + "a.active_type," + "a.stop_date," + "a.record_cond," + "a.crt_user,"
        + "a.crt_date,"+ "a.mcht_seq_flag ";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code,a.crt_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCrtuser("comm_crt_user");
    commActiveType("comm_active_type");
    commfuncAudType("aud_type");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowid = itemKk("data_k1");
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
            + "a.aud_type,"
            + "a.active_code as active_code,"
            + "a.crt_user,"
            + "a.active_name,"
            + "a.stop_flag,"
            + "a.stop_date,"
            + "a.stop_desc,"
            + "a.issue_date_s,"
            + "a.issue_date_e,"
            + "a.effect_months,"
            + "a.purchase_days,"
            + "a.n1_days,"
            + "a.achieve_cond,"
            + "a.new_hldr_cond,"
            + "a.new_hldr_sel,"
            + "a.new_hldr_days,"
            + "a.new_group_cond,"
            + "a.new_hldr_card,"
            + "a.new_hldr_sup,"
            + "a.acct_type_sel,"
            + "a.group_code_sel,"
            + "a.source_code_sel,"
            + "a.card_type_sel,"
            + "a.promote_dept_sel,"
            + "a.list_cond,"
            + "a.list_flag,"
            + "a.list_use_sel,"
            + "a.mcc_code_sel,"
            + "a.merchant_sel,"
            + "a.mcht_group_sel,"
            + "a.mcht_in_amt,"
            + "a.in_merchant_sel,"
            + "a.in_mcht_group_sel,"
            + "a.pos_entry_sel,"
            + "a.ucaf_sel,"
            + "a.eci_sel,"
            + "a.bl_cond,"
            + "a.ca_cond,"
            + "a.it_cond,"
            + "a.it_flag,"
            + "a.id_cond,"
            + "a.ao_cond,"
            + "a.ot_cond,"
            + "a.linebc_cond,"
            + "a.banklite_cond,"
            + "a.anulfee_cond,"
            + "a.anulfee_days,"
            + "a.action_pay_cond,"
            + "a.action_pay_times,"
            + "a.selfdeduct_cond,"
            + "a.sms_nopurc_cond,"
            + "a.sms_nopurc_days,"
            + "a.nopurc_msg_id_g,"
            + "a.nopurc_msg_id_c,"
            + "a.sms_half_cond,"
            + "a.sms_half_days,"
            + "a.half_cnt_cond,"
            + "a.half_cnt,"
            + "a.half_andor_cond,"
            + "a.half_amt_cond,"
            + "a.half_amt,"
            + "a.half_msg_id_g,"
            + "a.half_msg_id_c,"
            + "a.sms_send_cond,"
            + "a.sms_send_days,"
            + "a.send_msg_id,"
            + "a.multi_fb_type,"
            + "a.record_cond,"
            + "a.record_group_no,"
            + "a.active_type,"
            + "a.bonus_type,"
            + "a.tax_flag,"
            + "a.fund_code,"
            + "a.group_type,"
            + "a.prog_code,"
            + "a.prog_s_date,"
            + "a.prog_e_date,"
            + "a.gift_no,"
            + "a.spec_gift_no,"
            + "a.per_amt_cond,"
            + "a.per_amt,"
            + "a.perday_cnt_cond,"
            + "a.perday_cnt,"
            + "a.sum_amt_cond,"
            + "a.sum_amt,"
            + "a.sum_cnt_cond,"
            + "a.sum_cnt,"
            + "a.purch_feed_type,"
            + "a.threshold_sel,"
            + "a.purchase_type_sel,"
            + "a.purchase_amt_s1,"
            + "a.purchase_amt_e1,"
            + "a.feedback_amt_1,"
            + "a.purchase_amt_s2,"
            + "a.purchase_amt_e2,"
            + "a.feedback_amt_2,"
            + "a.purchase_amt_s3,"
            + "a.purchase_amt_e3,"
            + "a.feedback_amt_3,"
            + "a.purchase_amt_s4,"
            + "a.purchase_amt_e4,"
            + "a.feedback_amt_4,"
            + "a.purchase_amt_s5,"
            + "a.purchase_amt_e5,"
            + "a.feedback_amt_5,"
            + "a.feedback_limit,"
            + "a.new_hldr_flag, "
            + "a.c_record_group_no, "
            + "a.mcht_in_cond,"
            + "a.nopurc_msg_pgm,"
            + "a.half_msg_pgm, "
            + "a.send_msg_pgm,"
            + "a.add_value_cond,"
            + "a.add_value,"
            + "a.mkt_fstp_gift_cond ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(activeCode, "a.active_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commAchieveCond("comm_achieve_cond");
    commTypeSel("comm_acct_type_sel");
    commGroupCodeSel("comm_group_code_sel");
    commSourceCodeSel("comm_source_code_sel");
    commCardTypeSel("comm_card_type_sel");
    commPromoteDeptSel("comm_promote_dept_sel");
    commListFlag("comm_list_flag");
    commListUse("comm_list_use_sel");
    commMccCodeSel("comm_mcc_code_sel");
    commMerchantSel("comm_merchant_sel");
    commMchtGroupSel("comm_mcht_group_sel");
    commInMerchantSel("comm_in_merchant_sel");
    commInMchtGroupSel("comm_in_mcht_group_sel");
    commPosEntrySel("comm_pos_entry_sel");
    commUcafSel("comm_ucaf_sel");
    commUciSel("comm_eci_sel");
    commMchtseq("comm_mcht_seq_flag");
    comm_record_group_no("comm_c_record_group_no");
    comm_record_group_no("comm_record_group_no");
    commItFlag("comm_it_flag");
    commHalfMgmPgm("comm_half_msg_pgm");
    commHalAndor("comm_half_andor_cond");
    commMultiFbType("comm_multi_fb_type");
    commActiveType("comm_active_type");
    commGroupType("comm_group_type");
    commNopurcMgmpgm("comm_nopurc_msg_pgm");
    commSendMgmpgm("comm_send_msg_pgm");
    commPurcFheedType("comm_purch_feed_type");
    commThresholdSel("comm_threshold_sel");
    commPurchaseTypeSel("comm_purchase_type_sel");
    commRecordGroupNo("comm_record_group_no");
    commFuncCode("comm_fund_code");
    commProgCode("comm_prog_code");
    commGiftNo("comm_gift_no");
    commSpecGiftNo("comm_spec_gift_no");
    commNewFlag("comm_new_hldr_flag");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    activeCode = wp.colStr("active_code");
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
    controlTabName = "mkt_fstp_parm";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code as active_code,"
            + "a.crt_user as bef_crt_user,"
            + "a.active_name as bef_active_name,"
            + "a.stop_flag as bef_stop_flag,"
            + "a.stop_date as bef_stop_date,"
            + "a.stop_desc as bef_stop_desc,"
            + "a.issue_date_s as bef_issue_date_s,"
            + "a.issue_date_e as bef_issue_date_e,"
            + "a.effect_months as bef_effect_months,"
            + "a.purchase_days as bef_purchase_days,"
            + "a.n1_days as bef_n1_days,"
            + "a.achieve_cond as bef_achieve_cond,"
            + "a.new_hldr_cond as bef_new_hldr_cond,"
            + "a.new_hldr_sel as bef_new_hldr_sel,"
            + "a.new_hldr_days as bef_new_hldr_days,"
            + "a.new_group_cond as bef_new_group_cond,"
            + "a.new_hldr_card as bef_new_hldr_card,"
            + "a.new_hldr_sup as bef_new_hldr_sup,"
            + "a.acct_type_sel as bef_acct_type_sel,"
            + "a.group_code_sel as bef_group_code_sel,"
            + "a.source_code_sel as bef_source_code_sel,"
            + "a.card_type_sel as bef_card_type_sel,"
            + "a.promote_dept_sel as bef_promote_dept_sel,"
            + "a.list_cond as bef_list_cond,"
            + "a.list_flag as bef_list_flag,"
            + "a.list_use_sel as bef_list_use_sel,"
            + "a.mcc_code_sel as bef_mcc_code_sel,"
            + "a.merchant_sel as bef_merchant_sel,"
            + "a.mcht_group_sel as bef_mcht_group_sel,"
            + "a.mcht_in_amt as bef_mcht_in_amt,"
            + "a.in_merchant_sel as bef_in_merchant_sel,"
            + "a.in_mcht_group_sel as bef_in_mcht_group_sel,"
            + "a.pos_entry_sel as bef_pos_entry_sel,"
            + "a.ucaf_sel as bef_ucaf_sel,"
            + "a.eci_sel as bef_eci_sel,"
            + "a.bl_cond as bef_bl_cond,"
            + "a.ca_cond as bef_ca_cond,"
            + "a.it_cond as bef_it_cond,"
            + "a.it_flag as bef_it_flag,"
            + "a.id_cond as bef_id_cond,"
            + "a.ao_cond as bef_ao_cond,"
            + "a.ot_cond as bef_ot_cond,"
            + "a.linebc_cond as bef_linebc_cond,"
            + "a.banklite_cond as bef_banklite_cond,"
            + "a.anulfee_cond as bef_anulfee_cond,"
            + "a.anulfee_days as bef_anulfee_days,"
            + "a.action_pay_cond as bef_action_pay_cond,"
            + "a.action_pay_times as bef_action_pay_times,"
            + "a.selfdeduct_cond as bef_selfdeduct_cond,"
            + "a.sms_nopurc_cond as bef_sms_nopurc_cond,"
            + "a.sms_nopurc_days as bef_sms_nopurc_days,"
            + "a.nopurc_msg_id_g as bef_nopurc_msg_id_g,"
            + "a.nopurc_msg_id_c as bef_nopurc_msg_id_c,"
            + "a.sms_half_cond as bef_sms_half_cond,"
            + "a.sms_half_days as bef_sms_half_days,"
            + "a.half_cnt_cond as bef_half_cnt_cond,"
            + "a.half_cnt as bef_half_cnt,"
            + "a.half_andor_cond as bef_half_andor_cond,"
            + "a.half_amt_cond as bef_half_amt_cond,"
            + "a.half_amt as bef_half_amt,"
            + "a.half_msg_id_g as bef_half_msg_id_g,"
            + "a.half_msg_id_c as bef_half_msg_id_c,"
            + "a.sms_send_cond as bef_sms_send_cond,"
            + "a.sms_send_days as bef_sms_send_days,"
            + "a.send_msg_id as bef_send_msg_id,"
            + "a.multi_fb_type as bef_multi_fb_type,"
            + "a.record_cond as bef_record_cond,"
            + "a.record_group_no as bef_record_group_no,"
            + "a.active_type as bef_active_type,"
            + "a.bonus_type as bef_bonus_type,"
            + "a.tax_flag as bef_tax_flag,"
            + "a.fund_code as bef_fund_code,"
            + "a.group_type as bef_group_type,"
            + "a.prog_code as bef_prog_code,"
            + "a.prog_s_date as bef_prog_s_date,"
            + "a.prog_e_date as bef_prog_e_date,"
            + "a.gift_no as bef_gift_no,"
            + "a.spec_gift_no as bef_spec_gift_no,"
            + "a.per_amt_cond as bef_per_amt_cond,"
            + "a.per_amt as bef_per_amt,"
            + "a.perday_cnt_cond as bef_perday_cnt_cond,"
            + "a.perday_cnt as bef_perday_cnt,"
            + "a.sum_amt_cond as bef_sum_amt_cond,"
            + "a.sum_amt as bef_sum_amt,"
            + "a.sum_cnt_cond as bef_sum_cnt_cond,"
            + "a.sum_cnt as bef_sum_cnt,"
            + "a.purch_feed_type as bef_purch_feed_type,"
            + "a.threshold_sel as bef_threshold_sel,"
            + "a.purchase_type_sel as bef_purchase_type_sel,"
            + "a.purchase_amt_s1 as bef_purchase_amt_s1,"
            + "a.purchase_amt_e1 as bef_purchase_amt_e1,"
            + "a.feedback_amt_1 as bef_feedback_amt_1,"
            + "a.purchase_amt_s2 as bef_purchase_amt_s2,"
            + "a.purchase_amt_e2 as bef_purchase_amt_e2,"
            + "a.feedback_amt_2 as bef_feedback_amt_2,"
            + "a.purchase_amt_s3 as bef_purchase_amt_s3,"
            + "a.purchase_amt_e3 as bef_purchase_amt_e3,"
            + "a.feedback_amt_3 as bef_feedback_amt_3,"
            + "a.purchase_amt_s4 as bef_purchase_amt_s4,"
            + "a.purchase_amt_e4 as bef_purchase_amt_e4,"
            + "a.feedback_amt_4 as bef_feedback_amt_4,"
            + "a.purchase_amt_s5 as bef_purchase_amt_s5,"
            + "a.purchase_amt_e5 as bef_purchase_amt_e5,"
            + "a.feedback_amt_5 as bef_feedback_amt_5,"
            + "a.feedback_limit as bef_feedback_limit,"
            + "a.new_hldr_flag as bef_new_hldr_flag,"
            + "a.mcht_seq_flag as bef_mcht_seq_flag,"
            + "a.c_record_cond as bef_c_record_cond,"
            + "a.c_record_group_no as bef_c_record_group_no, "
            + "a.mcht_in_cond as bef_mcht_in_cond, "
            + "a.nopurc_msg_pgm as bef_nopurc_msg_pgm, "
            + "a.half_msg_pgm as bef_half_msg_pgm, "
            + "a.send_msg_pgm as bef_send_msg_pgm,"
            + "a.add_value as bef_add_value,"
            + "a.mkt_fstp_gift_cond as bef_mkt_fstp_gift_cond ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAchieveCond("comm_achieve_cond");
    commTypeSel("comm_acct_type_sel");
    commGroupCodeSel("comm_group_code_sel");
    commSourceCodeSel("comm_source_code_sel");
    commCardTypeSel("comm_card_type_sel");
    commPromoteDeptSel("comm_promote_dept_sel");
    commListFlag("comm_list_flag");
    commListUse("comm_list_use_sel");
    commMccCodeSel("comm_mcc_code_sel");
    commNopurcMgmpgm("comm_nopurc_msg_pgm");
    commSendMgmpgm("comm_send_msg_pgm");
    commMerchantSel("comm_merchant_sel");
    commMchtGroupSel("comm_mcht_group_sel");
    commInMerchantSel("comm_in_merchant_sel");
    commInMchtGroupSel("comm_in_mcht_group_sel");
    commPosEntrySel("comm_pos_entry_sel");
    commUcafSel("comm_ucaf_sel");
    commUciSel("comm_eci_sel");
    commItFlag("comm_it_flag");
    commMchtseq("comm_mcht_seq_flag");
    commHalAndor("comm_half_andor_cond");
    commMultiFbType("comm_multi_fb_type");
    commRecordGroupNo("comm_record_group_no");
    commActiveType("comm_active_type");
    commFuncCode("comm_fund_code");
    commHalfMgmPgm("comm_half_msg_pgm");
    comm_record_group_no("comm_c_record_group_no");
    comm_record_group_no("comm_record_group_no");
    commGroupType("comm_group_type");
    commProgCode("comm_prog_code");
    commGiftNo("comm_gift_no");
    commSpecGiftNo("comm_spec_gift_no");
    commPurcFheedType("comm_purch_feed_type");
    commThresholdSel("comm_threshold_sel");
    commPurchaseTypeSel("comm_purchase_type_sel");
    commNewFlag("comm_new_hldr_flag");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("new_group_cond_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "G"));
    wp.colSet("acct_type_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "2"));
    wp.colSet("source_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "3"));
    wp.colSet("card_type_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "4"));
    wp.colSet("promote_dept_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "5"));
    wp.colSet("list_use_sel_cnt" , 
        listMktImchannelList("mkt_imfstp_list_t","mkt_imfstp_list",wp.colStr("active_code"),""));
    wp.colSet("mcc_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "6"));
    wp.colSet("merchant_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "7"));
    wp.colSet("mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "8"));
    wp.colSet("in_merchant_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "9"));
    wp.colSet("in_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "10"));
    wp.colSet("pos_entry_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "11"));
    wp.colSet("ucaf_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "12"));
    wp.colSet("eci_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "13"));
    wp.colSet("action_pay_cond_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "H"));
    wp.colSet("nopurc_msg_id_g_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "A"));
    wp.colSet("nopurc_msg_id_c_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "B"));
    wp.colSet("half_msg_id_g_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "C"));
    wp.colSet("send_msg_id_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "E"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
	
    if (!wp.colStr("send_msg_pgm").equals(wp.colStr("bef_send_msg_pgm")))
		wp.colSet("opt_send_msg_pgm","Y");
    commSendMgmpgm("comm_send_msg_pgm");
    commSendMgmpgm("comm_bef_send_msg_pgm",1);	  
	  
	if (!wp.colStr("half_msg_pgm").equals(wp.colStr("bef_half_msg_pgm")))
	    wp.colSet("opt_half_msg_pgm","Y");
	commHalfMgmPgm("comm_half_msg_pgm");
	commHalfMgmPgm("comm_bef_half_msg_pgm",1);
		  
	if (!wp.colStr("nopurc_msg_pgm").equals(wp.colStr("bef_nopurc_msg_pgm")))
	   wp.colSet("opt_nopurc_msg_pgm","Y");
	commNopurcMgmpgm("comm_nopurc_msg_pgm");
	commNopurcMgmpgm("comm_bef_nopurc_msg_pgm",1);
		  
	if (!wp.colStr("mcht_in_cond").equals(wp.colStr("bef_mcht_in_cond")))
	  wp.colSet("opt_mcht_in_cond","Y");
	  
    if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
      wp.colSet("opt_active_name", "Y");

    if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
      wp.colSet("opt_stop_flag", "Y");

    if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
      wp.colSet("opt_stop_date", "Y");

    if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
      wp.colSet("opt_stop_desc", "Y");

    if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
      wp.colSet("opt_issue_date_s", "Y");

    if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
      wp.colSet("opt_issue_date_e", "Y");

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months", "Y");

    if (!wp.colStr("purchase_days").equals(wp.colStr("bef_purchase_days")))
      wp.colSet("opt_purchase_days", "Y");

    if (!wp.colStr("n1_days").equals(wp.colStr("bef_n1_days")))
      wp.colSet("opt_n1_days", "Y");

    if (!wp.colStr("achieve_cond").equals(wp.colStr("bef_achieve_cond")))
      wp.colSet("opt_achieve_cond", "Y");
    commAchieveCond("comm_achieve_cond");
    commAchieveCond("comm_bef_achieve_cond");

    if (!wp.colStr("new_hldr_flag").equals(wp.colStr("bef_new_hldr_flag")))
        wp.colSet("opt_new_hldr_flag","Y");
    commNewFlag("comm_new_hldr_flag");
    commNewFlag("comm_bef_new_hldr_flag");
    
    if (!wp.colStr("c_record_cond").equals(wp.colStr("bef_c_record_cond")))
        wp.colSet("opt_c_record_cond","Y");   
   
    if (!wp.colStr("c_record_group_no").equals(wp.colStr("bef_c_record_group_no")))
        wp.colSet("opt_c_record_group_no","Y");
     comm_record_group_no("comm_c_record_group_no");
     comm_record_group_no("comm_bef_c_record_group_no",1);
  
     if (!wp.colStr("record_group_no").equals(wp.colStr("bef_record_group_no")))
         wp.colSet("opt_record_group_no","Y");
      comm_record_group_no("comm_record_group_no");
      comm_record_group_no("comm_bef_record_group_no",1);
      
    if (!wp.colStr("mcht_seq_flag").equals(wp.colStr("bef_mcht_seq_flag")))
        wp.colSet("opt_mcht_seq_flag","Y");
     commMchtseq("comm_mcht_seq_flag");
     commMchtseq("comm_bef_mcht_seq_flag");
     
    if (!wp.colStr("new_hldr_cond").equals(wp.colStr("bef_new_hldr_cond")))
      wp.colSet("opt_new_hldr_cond", "Y");

    if (!wp.colStr("new_hldr_days").equals(wp.colStr("bef_new_hldr_days")))
      wp.colSet("opt_new_hldr_days", "Y");

    if (!wp.colStr("new_group_cond").equals(wp.colStr("bef_new_group_cond")))
      wp.colSet("opt_new_group_cond", "Y");

    wp.colSet("bef_new_group_cond_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "G"));
    if (!wp.colStr("new_group_cond_cnt").equals(wp.colStr("bef_new_group_cond_cnt")))
      wp.colSet("opt_new_group_cond_cnt", "Y");

    if (!wp.colStr("new_hldr_card").equals(wp.colStr("bef_new_hldr_card")))
      wp.colSet("opt_new_hldr_card", "Y");

    if (!wp.colStr("new_hldr_sup").equals(wp.colStr("bef_new_hldr_sup")))
      wp.colSet("opt_new_hldr_sup", "Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commTypeSel("comm_acct_type_sel");
    commTypeSel("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "1"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCodeSel("comm_group_code_sel");
    commGroupCodeSel("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("source_code_sel").equals(wp.colStr("bef_source_code_sel")))
      wp.colSet("opt_source_code_sel", "Y");
    commSourceCodeSel("comm_source_code_sel");
    commSourceCodeSel("comm_bef_source_code_sel");

    wp.colSet("bef_source_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "3"));
    if (!wp.colStr("source_code_sel_cnt").equals(wp.colStr("bef_source_code_sel_cnt")))
      wp.colSet("opt_source_code_sel_cnt", "Y");

    if (!wp.colStr("card_type_sel").equals(wp.colStr("bef_card_type_sel")))
      wp.colSet("opt_card_type_sel", "Y");
    commCardTypeSel("comm_card_type_sel");
    commCardTypeSel("comm_bef_card_type_sel");

    wp.colSet("bef_card_type_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "4"));
    if (!wp.colStr("card_type_sel_cnt").equals(wp.colStr("bef_card_type_sel_cnt")))
      wp.colSet("opt_card_type_sel_cnt", "Y");

    if (!wp.colStr("promote_dept_sel").equals(wp.colStr("bef_promote_dept_sel")))
      wp.colSet("opt_promote_dept_sel", "Y");
    commPromoteDeptSel("comm_promote_dept_sel");
    commPromoteDeptSel("comm_bef_promote_dept_sel");

    wp.colSet("bef_promote_dept_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "5"));
    if (!wp.colStr("promote_dept_sel_cnt").equals(wp.colStr("bef_promote_dept_sel_cnt")))
      wp.colSet("opt_promote_dept_sel_cnt", "Y");

    if (!wp.colStr("list_use_sel").equals(wp.colStr("bef_list_use_sel")))
      wp.colSet("opt_list_use_sel", "Y");
    commListUse("comm_list_use_sel");
    commListUse("comm_bef_list_use_sel");
    
    wp.colSet("bef_list_use_sel_cnt" , listMktImchannelList("mkt_imfstp_list","mkt_imfstp_list",wp.colStr("active_code"),""));
    if (!wp.colStr("list_use_sel_cnt").equals(wp.colStr("bef_list_use_sel_cnt")))
       wp.colSet("opt_list_use_sel_cnt","Y");

    if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
      wp.colSet("opt_mcc_code_sel", "Y");
    commMccCodeSel("comm_mcc_code_sel");
    commMccCodeSel("comm_bef_mcc_code_sel");

    wp.colSet("bef_mcc_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "6"));
    if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
      wp.colSet("opt_mcc_code_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchantSel("comm_merchant_sel");
    commMerchantSel("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "7"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    commMchtGroupSel("comm_mcht_group_sel");
    commMchtGroupSel("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "8"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("mcht_in_amt").equals(wp.colStr("bef_mcht_in_amt")))
      wp.colSet("opt_mcht_in_amt", "Y");

    if (!wp.colStr("in_merchant_sel").equals(wp.colStr("bef_in_merchant_sel")))
      wp.colSet("opt_in_merchant_sel", "Y");
    commInMerchantSel("comm_in_merchant_sel");
    commInMerchantSel("comm_bef_in_merchant_sel");

    wp.colSet("bef_in_merchant_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "9"));
    if (!wp.colStr("in_merchant_sel_cnt").equals(wp.colStr("bef_in_merchant_sel_cnt")))
      wp.colSet("opt_in_merchant_sel_cnt", "Y");

    if (!wp.colStr("in_mcht_group_sel").equals(wp.colStr("bef_in_mcht_group_sel")))
      wp.colSet("opt_in_mcht_group_sel", "Y");
    commInMchtGroupSel("comm_in_mcht_group_sel");
    commInMchtGroupSel("comm_bef_in_mcht_group_sel");

    wp.colSet("bef_in_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "10"));
    if (!wp.colStr("in_mcht_group_sel_cnt").equals(wp.colStr("bef_in_mcht_group_sel_cnt")))
      wp.colSet("opt_in_mcht_group_sel_cnt", "Y");
    
    if (!wp.colStr("add_value").equals(wp.colStr("bef_add_value")))
    {
        wp.colSet("opt_add_value", "Y");
    }
    
    if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
      wp.colSet("opt_pos_entry_sel", "Y");
    commPosEntrySel("comm_pos_entry_sel");
    commPosEntrySel("comm_bef_pos_entry_sel");

    wp.colSet("bef_pos_entry_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "11"));
    if (!wp.colStr("pos_entry_sel_cnt").equals(wp.colStr("bef_pos_entry_sel_cnt")))
      wp.colSet("opt_pos_entry_sel_cnt", "Y");

    if (!wp.colStr("ucaf_sel").equals(wp.colStr("bef_ucaf_sel")))
      wp.colSet("opt_ucaf_sel", "Y");
    commUcafSel("comm_ucaf_sel");
    commUcafSel("comm_bef_ucaf_sel");

    wp.colSet("bef_ucaf_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "12"));
    if (!wp.colStr("ucaf_sel_cnt").equals(wp.colStr("bef_ucaf_sel_cnt")))
      wp.colSet("opt_ucaf_sel_cnt", "Y");

    if (!wp.colStr("eci_sel").equals(wp.colStr("bef_eci_sel")))
      wp.colSet("opt_eci_sel", "Y");
    commUciSel("comm_eci_sel");
    commUciSel("comm_bef_eci_sel");

    wp.colSet("bef_eci_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "13"));
    if (!wp.colStr("eci_sel_cnt").equals(wp.colStr("bef_eci_sel_cnt")))
      wp.colSet("opt_eci_sel_cnt", "Y");

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

    if (!wp.colStr("linebc_cond").equals(wp.colStr("bef_linebc_cond")))
      wp.colSet("opt_linebc_cond", "Y");

    if (!wp.colStr("banklite_cond").equals(wp.colStr("bef_banklite_cond")))
      wp.colSet("opt_banklite_cond", "Y");

    if (!wp.colStr("anulfee_cond").equals(wp.colStr("bef_anulfee_cond")))
      wp.colSet("opt_anulfee_cond", "Y");

    if (!wp.colStr("anulfee_days").equals(wp.colStr("bef_anulfee_days")))
      wp.colSet("opt_anulfee_days", "Y");

    if (!wp.colStr("action_pay_cond").equals(wp.colStr("bef_action_pay_cond")))
      wp.colSet("opt_action_pay_cond", "Y");

    if (!wp.colStr("action_pay_times").equals(wp.colStr("bef_action_pay_times")))
      wp.colSet("opt_action_pay_times", "Y");

    wp.colSet("bef_action_pay_cond_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "H"));
    if (!wp.colStr("action_pay_cond_cnt").equals(wp.colStr("bef_action_pay_cond_cnt")))
      wp.colSet("opt_action_pay_cond_cnt", "Y");

    if (!wp.colStr("selfdeduct_cond").equals(wp.colStr("bef_selfdeduct_cond")))
      wp.colSet("opt_selfdeduct_cond", "Y");

    if (!wp.colStr("sms_nopurc_cond").equals(wp.colStr("bef_sms_nopurc_cond")))
      wp.colSet("opt_sms_nopurc_cond", "Y");

    if (!wp.colStr("sms_nopurc_days").equals(wp.colStr("bef_sms_nopurc_days")))
      wp.colSet("opt_sms_nopurc_days", "Y");

    if (!wp.colStr("nopurc_msg_id_g").equals(wp.colStr("bef_nopurc_msg_id_g")))
      wp.colSet("opt_nopurc_msg_id_g", "Y");

    wp.colSet("bef_nopurc_msg_id_g_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "A"));
    if (!wp.colStr("nopurc_msg_id_g_cnt").equals(wp.colStr("bef_nopurc_msg_id_g_cnt")))
      wp.colSet("opt_nopurc_msg_id_g_cnt", "Y");

    if (!wp.colStr("nopurc_msg_id_c").equals(wp.colStr("bef_nopurc_msg_id_c")))
      wp.colSet("opt_nopurc_msg_id_c", "Y");

    wp.colSet("bef_nopurc_msg_id_c_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "B"));
    if (!wp.colStr("nopurc_msg_id_c_cnt").equals(wp.colStr("bef_nopurc_msg_id_c_cnt")))
      wp.colSet("opt_nopurc_msg_id_c_cnt", "Y");

    if (!wp.colStr("sms_half_cond").equals(wp.colStr("bef_sms_half_cond")))
      wp.colSet("opt_sms_half_cond", "Y");

    if (!wp.colStr("sms_half_days").equals(wp.colStr("bef_sms_half_days")))
      wp.colSet("opt_sms_half_days", "Y");

    if (!wp.colStr("half_cnt_cond").equals(wp.colStr("bef_half_cnt_cond")))
      wp.colSet("opt_half_cnt_cond", "Y");

    if (!wp.colStr("half_cnt").equals(wp.colStr("bef_half_cnt")))
      wp.colSet("opt_half_cnt", "Y");

    if (!wp.colStr("half_andor_cond").equals(wp.colStr("bef_half_andor_cond")))
      wp.colSet("opt_half_andor_cond", "Y");
    commHalAndor("comm_half_andor_cond");
    commHalAndor("comm_bef_half_andor_cond");

    if (!wp.colStr("half_amt_cond").equals(wp.colStr("bef_half_amt_cond")))
      wp.colSet("opt_half_amt_cond", "Y");

    if (!wp.colStr("half_amt").equals(wp.colStr("bef_half_amt")))
      wp.colSet("opt_half_amt", "Y");

    if (!wp.colStr("half_msg_id_g").equals(wp.colStr("bef_half_msg_id_g")))
      wp.colSet("opt_half_msg_id_g", "Y");

    wp.colSet("bef_half_msg_id_g_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "C"));
    if (!wp.colStr("half_msg_id_g_cnt").equals(wp.colStr("bef_half_msg_id_g_cnt")))
      wp.colSet("opt_half_msg_id_g_cnt", "Y");

    if (!wp.colStr("half_msg_id_c").equals(wp.colStr("bef_half_msg_id_c")))
      wp.colSet("opt_half_msg_id_c", "Y");

    if (!wp.colStr("sms_send_cond").equals(wp.colStr("bef_sms_send_cond")))
      wp.colSet("opt_sms_send_cond", "Y");

    if (!wp.colStr("mkt_fstp_gift_cond").equals(wp.colStr("bef_mkt_fstp_gift_cond")))
        wp.colSet("opt_mkt_fstp_gift_cond", "Y");    
    
    if (!wp.colStr("sms_send_days").equals(wp.colStr("bef_sms_send_days")))
      wp.colSet("opt_sms_send_days", "Y");

    if (!wp.colStr("send_msg_id").equals(wp.colStr("bef_send_msg_id")))
      wp.colSet("opt_send_msg_id", "Y");

    wp.colSet("bef_send_msg_id_cnt",
        listMktBnData("mkt_bn_data", "MKT_FSTP_PARM", wp.colStr("active_code"), "E"));
    if (!wp.colStr("send_msg_id_cnt").equals(wp.colStr("bef_send_msg_id_cnt")))
      wp.colSet("opt_send_msg_id_cnt", "Y");

    if (!wp.colStr("multi_fb_type").equals(wp.colStr("bef_multi_fb_type")))
      wp.colSet("opt_multi_fb_type", "Y");
    commMultiFbType("comm_multi_fb_type");
    commMultiFbType("comm_bef_multi_fb_type");

    if (!wp.colStr("record_cond").equals(wp.colStr("bef_record_cond")))
      wp.colSet("opt_record_cond", "Y");

    if (!wp.colStr("record_group_no").equals(wp.colStr("bef_record_group_no")))
      wp.colSet("opt_record_group_no", "Y");
    commRecordGroupNo("comm_record_group_no");
    commRecordGroupNo("comm_bef_record_group_no", 1);

    if (!wp.colStr("active_type").equals(wp.colStr("bef_active_type")))
      wp.colSet("opt_active_type", "Y");
    commActiveType("comm_active_type");
    commActiveType("comm_bef_active_type");

    if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
      wp.colSet("opt_bonus_type", "Y");

    if (!wp.colStr("tax_flag").equals(wp.colStr("bef_tax_flag")))
      wp.colSet("opt_tax_flag", "Y");

    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code", "Y");
    commFuncCode("comm_fund_code");
    commFuncCode("comm_bef_fund_code", 1);

    if (!wp.colStr("group_type").equals(wp.colStr("bef_group_type")))
      wp.colSet("opt_group_type", "Y");
    commGroupType("comm_group_type");
    commGroupType("comm_bef_group_type");

    if (!wp.colStr("prog_code").equals(wp.colStr("bef_prog_code")))
      wp.colSet("opt_prog_code", "Y");
    commProgCode("comm_prog_code");
    commProgCode("comm_bef_prog_code", 1);

    if (!wp.colStr("prog_s_date").equals(wp.colStr("bef_prog_s_date")))
      wp.colSet("opt_prog_s_date", "Y");

    if (!wp.colStr("prog_e_date").equals(wp.colStr("bef_prog_e_date")))
      wp.colSet("opt_prog_e_date", "Y");

    if (!wp.colStr("gift_no").equals(wp.colStr("bef_gift_no")))
      wp.colSet("opt_gift_no", "Y");
    commGiftNo("comm_gift_no");
    comGgiftNo("comm_bef_gift_no", 1);

    if (!wp.colStr("spec_gift_no").equals(wp.colStr("bef_spec_gift_no")))
      wp.colSet("opt_spec_gift_no", "Y");
    commSpecGiftNo("comm_spec_gift_no");
    commSpecGiftNo("comm_bef_spec_gift_no", 1);

    if (!wp.colStr("per_amt_cond").equals(wp.colStr("bef_per_amt_cond")))
      wp.colSet("opt_per_amt_cond", "Y");

    if (!wp.colStr("per_amt").equals(wp.colStr("bef_per_amt")))
      wp.colSet("opt_per_amt", "Y");

    if (!wp.colStr("perday_cnt_cond").equals(wp.colStr("bef_perday_cnt_cond")))
      wp.colSet("opt_perday_cnt_cond", "Y");

    if (!wp.colStr("perday_cnt").equals(wp.colStr("bef_perday_cnt")))
      wp.colSet("opt_perday_cnt", "Y");

    if (!wp.colStr("sum_amt_cond").equals(wp.colStr("bef_sum_amt_cond")))
      wp.colSet("opt_sum_amt_cond", "Y");

    if (!wp.colStr("sum_amt").equals(wp.colStr("bef_sum_amt")))
      wp.colSet("opt_sum_amt", "Y");

    if (!wp.colStr("sum_cnt_cond").equals(wp.colStr("bef_sum_cnt_cond")))
      wp.colSet("opt_sum_cnt_cond", "Y");

    if (!wp.colStr("sum_cnt").equals(wp.colStr("bef_sum_cnt")))
      wp.colSet("opt_sum_cnt", "Y");

    if (!wp.colStr("purch_feed_type").equals(wp.colStr("bef_purch_feed_type")))
      wp.colSet("opt_purch_feed_type", "Y");
    commPurcFheedType("comm_purch_feed_type");
    commPurcFheedType("comm_bef_purch_feed_type");

    if (!wp.colStr("threshold_sel").equals(wp.colStr("bef_threshold_sel")))
      wp.colSet("opt_threshold_sel", "Y");
    commThresholdSel("comm_threshold_sel");
    commThresholdSel("comm_bef_threshold_sel");

    if (!wp.colStr("purchase_type_sel").equals(wp.colStr("bef_purchase_type_sel")))
      wp.colSet("opt_purchase_type_sel", "Y");
    commPurchaseTypeSel("comm_purchase_type_sel");
    commPurchaseTypeSel("comm_bef_purchase_type_sel");

    if (!wp.colStr("purchase_amt_s1").equals(wp.colStr("bef_purchase_amt_s1")))
      wp.colSet("opt_purchase_amt_s1", "Y");

    if (!wp.colStr("purchase_amt_e1").equals(wp.colStr("bef_purchase_amt_e1")))
      wp.colSet("opt_purchase_amt_e1", "Y");

    if (!wp.colStr("feedback_amt_1").equals(wp.colStr("bef_feedback_amt_1")))
      wp.colSet("opt_feedback_amt_1", "Y");

    if (!wp.colStr("purchase_amt_s2").equals(wp.colStr("bef_purchase_amt_s2")))
      wp.colSet("opt_purchase_amt_s2", "Y");

    if (!wp.colStr("purchase_amt_e2").equals(wp.colStr("bef_purchase_amt_e2")))
      wp.colSet("opt_purchase_amt_e2", "Y");

    if (!wp.colStr("feedback_amt_2").equals(wp.colStr("bef_feedback_amt_2")))
      wp.colSet("opt_feedback_amt_2", "Y");

    if (!wp.colStr("purchase_amt_s3").equals(wp.colStr("bef_purchase_amt_s3")))
      wp.colSet("opt_purchase_amt_s3", "Y");

    if (!wp.colStr("purchase_amt_e3").equals(wp.colStr("bef_purchase_amt_e3")))
      wp.colSet("opt_purchase_amt_e3", "Y");

    if (!wp.colStr("feedback_amt_3").equals(wp.colStr("bef_feedback_amt_3")))
      wp.colSet("opt_feedback_amt_3", "Y");

    if (!wp.colStr("purchase_amt_s4").equals(wp.colStr("bef_purchase_amt_s4")))
      wp.colSet("opt_purchase_amt_s4", "Y");

    if (!wp.colStr("purchase_amt_e4").equals(wp.colStr("bef_purchase_amt_e4")))
      wp.colSet("opt_purchase_amt_e4", "Y");

    if (!wp.colStr("feedback_amt_4").equals(wp.colStr("bef_feedback_amt_4")))
      wp.colSet("opt_feedback_amt_4", "Y");

    if (!wp.colStr("purchase_amt_s5").equals(wp.colStr("bef_purchase_amt_s5")))
      wp.colSet("opt_purchase_amt_s5", "Y");

    if (!wp.colStr("purchase_amt_e5").equals(wp.colStr("bef_purchase_amt_e5")))
      wp.colSet("opt_purchase_amt_e5", "Y");

    if (!wp.colStr("feedback_amt_5").equals(wp.colStr("bef_feedback_amt_5")))
      wp.colSet("opt_feedback_amt_5", "Y");

    if (!wp.colStr("feedback_limit").equals(wp.colStr("bef_feedback_limit")))
      wp.colSet("opt_feedback_limit", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("send_msg_pgm","");	
      wp.colSet("half_msg_pgm","");
      wp.colSet("nopurc_msg_pgm","");	
      wp.colSet("mcht_in_cond","");	
      wp.colSet("c_record_group_no","");	
      wp.colSet("mcht_seq_flag","");
      wp.colSet("new_hldr_flag","");
      wp.colSet("active_name", "");
      wp.colSet("stop_flag", "");
      wp.colSet("stop_date", "");
      wp.colSet("stop_desc", "");
      wp.colSet("issue_date_s", "");
      wp.colSet("issue_date_e", "");
      wp.colSet("effect_months", "");
      wp.colSet("purchase_days", "");
      wp.colSet("n1_days", "");
      wp.colSet("achieve_cond", "");
      wp.colSet("new_hldr_cond", "");
      wp.colSet("new_hldr_sel", "");
      wp.colSet("add_value", "");
      wp.colSet("new_hldr_days", "");
      wp.colSet("new_group_cond", "");
      wp.colSet("new_group_cond_cnt", "");
      wp.colSet("new_hldr_card", "");
      wp.colSet("new_hldr_sup", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("source_code_sel", "");
      wp.colSet("source_code_sel_cnt", "");
      wp.colSet("card_type_sel", "");
      wp.colSet("card_type_sel_cnt", "");
      wp.colSet("promote_dept_sel", "");
      wp.colSet("promote_dept_sel_cnt", "");
      wp.colSet("list_use_sel", "");
      wp.colSet("list_use_sel_cnt", "");
      wp.colSet("mcc_code_sel", "");
      wp.colSet("mcc_code_sel_cnt", "");
      wp.colSet("merchant_sel", "");
      wp.colSet("merchant_sel_cnt", "");
      wp.colSet("mcht_group_sel", "");
      wp.colSet("mcht_group_sel_cnt", "");
      wp.colSet("mcht_in_amt", "");
      wp.colSet("in_merchant_sel", "");
      wp.colSet("in_merchant_sel_cnt", "");
      wp.colSet("in_mcht_group_sel", "");
      wp.colSet("in_mcht_group_sel_cnt", "");
      wp.colSet("pos_entry_sel", "");
      wp.colSet("pos_entry_sel_cnt", "");
      wp.colSet("ucaf_sel", "");
      wp.colSet("ucaf_sel_cnt", "");
      wp.colSet("eci_sel", "");
      wp.colSet("eci_sel_cnt", "");
      wp.colSet("bl_cond", "");
      wp.colSet("ca_cond", "");
      wp.colSet("it_cond", "");
      wp.colSet("it_flag", "");
      wp.colSet("id_cond", "");
      wp.colSet("ao_cond", "");
      wp.colSet("ot_cond", "");
      wp.colSet("linebc_cond", "");
      wp.colSet("banklite_cond", "");
      wp.colSet("anulfee_cond", "");
      wp.colSet("anulfee_days", "");
      wp.colSet("action_pay_cond", "");
      wp.colSet("action_pay_times", "");
      wp.colSet("action_pay_cond_cnt", "");
      wp.colSet("selfdeduct_cond", "");
      wp.colSet("sms_nopurc_cond", "");
      wp.colSet("sms_nopurc_days", "");
      wp.colSet("nopurc_msg_id_g", "");
      wp.colSet("nopurc_msg_id_g_cnt", "");
      wp.colSet("nopurc_msg_id_c", "");
      wp.colSet("nopurc_msg_id_c_cnt", "");
      wp.colSet("sms_half_cond", "");
      wp.colSet("sms_half_days", "");
      wp.colSet("half_cnt_cond", "");
      wp.colSet("half_cnt", "");
      wp.colSet("half_andor_cond", "");
      wp.colSet("half_amt_cond", "");
      wp.colSet("half_amt", "");
      wp.colSet("half_msg_id_g", "");
      wp.colSet("half_msg_id_g_cnt", "");
      wp.colSet("half_msg_id_c", "");
      wp.colSet("sms_send_cond", "");
      wp.colSet("mkt_fstp_gift_cond", "");
      wp.colSet("sms_send_days", "");
      wp.colSet("send_msg_id", "");
      wp.colSet("send_msg_id_cnt", "");
      wp.colSet("multi_fb_type", "");
      wp.colSet("record_cond", "");
      wp.colSet("record_group_no", "");
      wp.colSet("active_type", "");
      wp.colSet("bonus_type", "");
      wp.colSet("tax_flag", "");
      wp.colSet("fund_code", "");
      wp.colSet("group_type", "");
      wp.colSet("prog_code", "");
      wp.colSet("prog_s_date", "");
      wp.colSet("prog_e_date", "");
      wp.colSet("gift_no", "");
      wp.colSet("spec_gift_no", "");
      wp.colSet("per_amt_cond", "");
      wp.colSet("per_amt", "");
      wp.colSet("perday_cnt_cond", "");
      wp.colSet("perday_cnt", "");
      wp.colSet("sum_amt_cond", "");
      wp.colSet("sum_amt", "");
      wp.colSet("sum_cnt_cond", "");
      wp.colSet("sum_cnt", "");
      wp.colSet("purch_feed_type", "");
      wp.colSet("threshold_sel", "");
      wp.colSet("purchase_type_sel", "");
      wp.colSet("purchase_amt_s1", "");
      wp.colSet("purchase_amt_e1", "");
      wp.colSet("feedback_amt_1", "");
      wp.colSet("purchase_amt_s2", "");
      wp.colSet("purchase_amt_e2", "");
      wp.colSet("feedback_amt_2", "");
      wp.colSet("purchase_amt_s3", "");
      wp.colSet("purchase_amt_e3", "");
      wp.colSet("feedback_amt_3", "");
      wp.colSet("purchase_amt_s4", "");
      wp.colSet("purchase_amt_e4", "");
      wp.colSet("feedback_amt_4", "");
      wp.colSet("purchase_amt_s5", "");
      wp.colSet("purchase_amt_e5", "");
      wp.colSet("feedback_amt_5", "");
      wp.colSet("feedback_limit", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
	if (wp.colStr("send_msg_pgm").length()==0)
	   wp.colSet("opt_send_msg_pgm","Y");	  
	if (wp.colStr("half_msg_pgm").length()==0)
       wp.colSet("opt_half_msg_pgm","Y");	  
	if (wp.colStr("nopurc_msg_pgm").length()==0)
	   wp.colSet("opt_nopurc_msg_pgm","Y");	  
	if (wp.colStr("mcht_in_cond").length()==0)
	   wp.colSet("opt_mcht_in_cond","Y");	  
	if (wp.colStr("c_record_group_no").length()==0)
	   wp.colSet("opt_c_record_group_no","Y");
	  
	if (wp.colStr("c_record_cond").length()==0)
	  wp.colSet("opt_c_record_cond","Y");
	
	if (wp.colStr("mcht_seq_flag").length()==0)
	  wp.colSet("opt_mcht_seq_flag","Y");	  
	if (wp.colStr("mcht_seq_flag").length()==0)
	  wp.colSet("opt_mcht_seq_flag","Y");	  
	
	if (wp.colStr("new_hldr_flag").length()==0)
      wp.colSet("opt_new_hldr_flag","Y");
	  
    if (wp.colStr("active_name").length() == 0)
      wp.colSet("opt_active_name", "Y");

    if (wp.colStr("stop_flag").length() == 0)
      wp.colSet("opt_stop_flag", "Y");

    if (wp.colStr("stop_date").length() == 0)
      wp.colSet("opt_stop_date", "Y");

    if (wp.colStr("stop_desc").length() == 0)
      wp.colSet("opt_stop_desc", "Y");

    if (wp.colStr("issue_date_s").length() == 0)
      wp.colSet("opt_issue_date_s", "Y");

    if (wp.colStr("issue_date_e").length() == 0)
      wp.colSet("opt_issue_date_e", "Y");

    if (wp.colStr("effect_months").length() == 0)
      wp.colSet("opt_effect_months", "Y");

    if (wp.colStr("purchase_days").length() == 0)
      wp.colSet("opt_purchase_days", "Y");

    if (wp.colStr("n1_days").length() == 0)
      wp.colSet("opt_n1_days", "Y");

    if (wp.colStr("achieve_cond").length() == 0)
      wp.colSet("opt_achieve_cond", "Y");

    if (wp.colStr("new_hldr_cond").length() == 0)
      wp.colSet("opt_new_hldr_cond", "Y");

    if (wp.colStr("add_value").length() == 0)
        wp.colSet("opt_add_value", "Y");    
    
    if (wp.colStr("new_hldr_days").length() == 0)
      wp.colSet("opt_new_hldr_days", "Y");

    if (wp.colStr("new_group_cond").length() == 0)
      wp.colSet("opt_new_group_cond", "Y");


    if (wp.colStr("new_hldr_card").length() == 0)
      wp.colSet("opt_new_hldr_card", "Y");

    if (wp.colStr("new_hldr_sup").length() == 0)
      wp.colSet("opt_new_hldr_sup", "Y");

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("opt_acct_type_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("source_code_sel").length() == 0)
      wp.colSet("opt_source_code_sel", "Y");


    if (wp.colStr("card_type_sel").length() == 0)
      wp.colSet("opt_card_type_sel", "Y");


    if (wp.colStr("promote_dept_sel").length() == 0)
      wp.colSet("opt_promote_dept_sel", "Y");


    if (wp.colStr("list_use_sel").length() == 0)
      wp.colSet("opt_list_use_sel", "Y");


    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("opt_mcc_code_sel", "Y");


    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("opt_merchant_sel", "Y");


    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("opt_mcht_group_sel", "Y");


    if (wp.colStr("mcht_in_amt").length() == 0)
      wp.colSet("opt_mcht_in_amt", "Y");

    if (wp.colStr("in_merchant_sel").length() == 0)
      wp.colSet("opt_in_merchant_sel", "Y");


    if (wp.colStr("in_mcht_group_sel").length() == 0)
      wp.colSet("opt_in_mcht_group_sel", "Y");


    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("opt_pos_entry_sel", "Y");


    if (wp.colStr("ucaf_sel").length() == 0)
      wp.colSet("opt_ucaf_sel", "Y");


    if (wp.colStr("eci_sel").length() == 0)
      wp.colSet("opt_eci_sel", "Y");


    if (wp.colStr("bl_cond").length() == 0)
      wp.colSet("opt_bl_cond", "Y");

    if (wp.colStr("ca_cond").length() == 0)
      wp.colSet("opt_ca_cond", "Y");

    if (wp.colStr("it_cond").length() == 0)
      wp.colSet("opt_it_cond", "Y");

    if (wp.colStr("it_flag").length() == 0)
      wp.colSet("opt_it_flag", "Y");

    if (wp.colStr("id_cond").length() == 0)
      wp.colSet("opt_id_cond", "Y");

    if (wp.colStr("ao_cond").length() == 0)
      wp.colSet("opt_ao_cond", "Y");

    if (wp.colStr("ot_cond").length() == 0)
      wp.colSet("opt_ot_cond", "Y");

    if (wp.colStr("linebc_cond").length() == 0)
      wp.colSet("opt_linebc_cond", "Y");

    if (wp.colStr("banklite_cond").length() == 0)
      wp.colSet("opt_banklite_cond", "Y");

    if (wp.colStr("anulfee_cond").length() == 0)
      wp.colSet("opt_anulfee_cond", "Y");

    if (wp.colStr("anulfee_days").length() == 0)
      wp.colSet("opt_anulfee_days", "Y");

    if (wp.colStr("action_pay_cond").length() == 0)
      wp.colSet("opt_action_pay_cond", "Y");

    if (wp.colStr("action_pay_times").length() == 0)
      wp.colSet("opt_action_pay_times", "Y");


    if (wp.colStr("selfdeduct_cond").length() == 0)
      wp.colSet("opt_selfdeduct_cond", "Y");

    if (wp.colStr("sms_nopurc_cond").length() == 0)
      wp.colSet("opt_sms_nopurc_cond", "Y");

    if (wp.colStr("sms_nopurc_days").length() == 0)
      wp.colSet("opt_sms_nopurc_days", "Y");

    if (wp.colStr("nopurc_msg_id_g").length() == 0)
      wp.colSet("opt_nopurc_msg_id_g", "Y");


    if (wp.colStr("nopurc_msg_id_c").length() == 0)
      wp.colSet("opt_nopurc_msg_id_c", "Y");


    if (wp.colStr("sms_half_cond").length() == 0)
      wp.colSet("opt_sms_half_cond", "Y");

    if (wp.colStr("sms_half_days").length() == 0)
      wp.colSet("opt_sms_half_days", "Y");

    if (wp.colStr("half_cnt_cond").length() == 0)
      wp.colSet("opt_half_cnt_cond", "Y");

    if (wp.colStr("half_cnt").length() == 0)
      wp.colSet("opt_half_cnt", "Y");

    if (wp.colStr("half_andor_cond").length() == 0)
      wp.colSet("opt_half_andor_cond", "Y");

    if (wp.colStr("half_amt_cond").length() == 0)
      wp.colSet("opt_half_amt_cond", "Y");

    if (wp.colStr("half_amt").length() == 0)
      wp.colSet("opt_half_amt", "Y");

    if (wp.colStr("half_msg_id_g").length() == 0)
      wp.colSet("opt_half_msg_id_g", "Y");


    if (wp.colStr("half_msg_id_c").length() == 0)
      wp.colSet("opt_half_msg_id_c", "Y");

    if (wp.colStr("sms_send_cond").length() == 0)
      wp.colSet("opt_sms_send_cond", "Y");

    if (wp.colStr("mkt_fstp_gift_cond").length() == 0)
        wp.colSet("opt_mkt_fstp_gift_cond", "Y");    
    
    if (wp.colStr("sms_send_days").length() == 0)
      wp.colSet("opt_sms_send_days", "Y");

    if (wp.colStr("send_msg_id").length() == 0)
      wp.colSet("opt_send_msg_id", "Y");


    if (wp.colStr("multi_fb_type").length() == 0)
      wp.colSet("opt_multi_fb_type", "Y");

    if (wp.colStr("record_cond").length() == 0)
      wp.colSet("opt_record_cond", "Y");

    if (wp.colStr("record_group_no").length() == 0)
      wp.colSet("opt_record_group_no", "Y");

    if (wp.colStr("active_type").length() == 0)
      wp.colSet("opt_active_type", "Y");

    if (wp.colStr("bonus_type").length() == 0)
      wp.colSet("opt_bonus_type", "Y");

    if (wp.colStr("tax_flag").length() == 0)
      wp.colSet("opt_tax_flag", "Y");

    if (wp.colStr("fund_code").length() == 0)
      wp.colSet("opt_fund_code", "Y");

    if (wp.colStr("group_type").length() == 0)
      wp.colSet("opt_group_type", "Y");

    if (wp.colStr("prog_code").length() == 0)
      wp.colSet("opt_prog_code", "Y");

    if (wp.colStr("prog_s_date").length() == 0)
      wp.colSet("opt_prog_s_date", "Y");

    if (wp.colStr("prog_e_date").length() == 0)
      wp.colSet("opt_prog_e_date", "Y");

    if (wp.colStr("gift_no").length() == 0)
      wp.colSet("opt_gift_no", "Y");

    if (wp.colStr("spec_gift_no").length() == 0)
      wp.colSet("opt_spec_gift_no", "Y");

    if (wp.colStr("per_amt_cond").length() == 0)
      wp.colSet("opt_per_amt_cond", "Y");

    if (wp.colStr("per_amt").length() == 0)
      wp.colSet("opt_per_amt", "Y");

    if (wp.colStr("perday_cnt_cond").length() == 0)
      wp.colSet("opt_perday_cnt_cond", "Y");

    if (wp.colStr("perday_cnt").length() == 0)
      wp.colSet("opt_perday_cnt", "Y");

    if (wp.colStr("sum_amt_cond").length() == 0)
      wp.colSet("opt_sum_amt_cond", "Y");

    if (wp.colStr("sum_amt").length() == 0)
      wp.colSet("opt_sum_amt", "Y");

    if (wp.colStr("sum_cnt_cond").length() == 0)
      wp.colSet("opt_sum_cnt_cond", "Y");

    if (wp.colStr("sum_cnt").length() == 0)
      wp.colSet("opt_sum_cnt", "Y");

    if (wp.colStr("purch_feed_type").length() == 0)
      wp.colSet("opt_purch_feed_type", "Y");

    if (wp.colStr("threshold_sel").length() == 0)
      wp.colSet("opt_threshold_sel", "Y");

    if (wp.colStr("purchase_type_sel").length() == 0)
      wp.colSet("opt_purchase_type_sel", "Y");

    if (wp.colStr("purchase_amt_s1").length() == 0)
      wp.colSet("opt_purchase_amt_s1", "Y");

    if (wp.colStr("purchase_amt_e1").length() == 0)
      wp.colSet("opt_purchase_amt_e1", "Y");

    if (wp.colStr("feedback_amt_1").length() == 0)
      wp.colSet("opt_feedback_amt_1", "Y");

    if (wp.colStr("purchase_amt_s2").length() == 0)
      wp.colSet("opt_purchase_amt_s2", "Y");

    if (wp.colStr("purchase_amt_e2").length() == 0)
      wp.colSet("opt_purchase_amt_e2", "Y");

    if (wp.colStr("feedback_amt_2").length() == 0)
      wp.colSet("opt_feedback_amt_2", "Y");

    if (wp.colStr("purchase_amt_s3").length() == 0)
      wp.colSet("opt_purchase_amt_s3", "Y");

    if (wp.colStr("purchase_amt_e3").length() == 0)
      wp.colSet("opt_purchase_amt_e3", "Y");

    if (wp.colStr("feedback_amt_3").length() == 0)
      wp.colSet("opt_feedback_amt_3", "Y");

    if (wp.colStr("purchase_amt_s4").length() == 0)
      wp.colSet("opt_purchase_amt_s4", "Y");

    if (wp.colStr("purchase_amt_e4").length() == 0)
      wp.colSet("opt_purchase_amt_e4", "Y");

    if (wp.colStr("feedback_amt_4").length() == 0)
      wp.colSet("opt_feedback_amt_4", "Y");

    if (wp.colStr("purchase_amt_s5").length() == 0)
      wp.colSet("opt_purchase_amt_s5", "Y");

    if (wp.colStr("purchase_amt_e5").length() == 0)
      wp.colSet("opt_purchase_amt_e5", "Y");

    if (wp.colStr("feedback_amt_5").length() == 0)
      wp.colSet("opt_feedback_amt_5", "Y");

    if (wp.colStr("feedback_limit").length() == 0)
      wp.colSet("opt_feedback_limit", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp02.Mktp6240Func func = new mktp02.Mktp6240Func(wp);

    String[] lsActiveCode = wp.itemBuff("active_code");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsCrtUser = wp.itemBuff("crt_user");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      wp.colSet(rr, "ok_flag", "-");
    //  if (lsCrtUser[rr].equals(wp.loginUser)) {
    //    ilAuth++;
    //    wp.colSet(rr, "ok_flag", "F");
    //    continue;
    //  }

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
            rc = func.dbDeleteD4Dmlist();
          if (rc == 1)
            rc = func.dbDeleteD4TDmlist();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
    	  commCrtuser("comm_crt_user");
        commActiveType("comm_active_type");
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
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
	  String lsSql ="";
	  try {
	       if ((wp.respHtml.equals("mktp6240")))
	         {
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
	       if ((wp.respHtml.equals("mktp6240_gncd")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_group_code3"
	                 ,"ptr_group_code"
	                 ,"trim(group_code)"
	                 ,"trim(group_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_apay")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_group_code3"
	                 ,"ptr_group_code"
	                 ,"trim(group_code)"
	                 ,"trim(group_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_actp")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_acct_type"
	                 ,"ptr_acct_type"
	                 ,"trim(acct_type)"
	                 ,"trim(chin_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_gpcd")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_group_code3"
	                 ,"ptr_group_code"
	                 ,"trim(group_code)"
	                 ,"trim(group_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_srcd")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_source_code03"
	                 ,"ptr_src_code"
	                 ,"trim(source_code)"
	                 ,"trim(source_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_cdtp")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_datc_code02"
	                 ,"ptr_card_type"
	                 ,"trim(card_type)"
	                 ,"trim(name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_pmdp")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_data_code07"
	                 ,"cca_mcc_risk"
	                 ,"trim(mcc_code)"
	                 ,"trim(mcc_remark)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_mccd")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_data_code07"
	                 ,"cca_mcc_risk"
	                 ,"trim(mcc_code)"
	                 ,"trim(mcc_remark)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_aaa1")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_data_Code34"
	                 ,"mkt_mcht_gp"
	                 ,"trim(mcht_group_id)"
	                 ,"trim(mcht_group_desc)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_aaat")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_data_Code34"
	                 ,"mkt_mcht_gp"
	                 ,"trim(mcht_group_id)"
	                 ,"trim(mcht_group_desc)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_posn")))
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
	       if ((wp.respHtml.equals("mktp6240_smsa")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_group_code3"
	                 ,"ptr_group_code"
	                 ,"trim(group_code)"
	                 ,"trim(group_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_smsb")))
	         {
	          wp.initOption ="--";
	          wp.optionKey = "";
	          this.dddwList("dddw_lost_code4"
	                 ,"ptr_sys_idtab"
	                 ,"trim(wf_id)"
	                 ,"trim(wf_desc)"
	                 ," where wf_type= 'CARD_NOTE'");
	          wp.initOption ="--";
	          wp.optionKey = "";
	          this.dddwList("dddw_card_type"
	                 ,"ptr_card_type"
	                 ,"trim(card_type)"
	                 ,"trim(name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_smsc")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_group_code3"
	                 ,"ptr_group_code"
	                 ,"trim(group_code)"
	                 ,"trim(group_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("mktp6240_smsd")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          if (wp.colStr("kk_data_code").length()>0)
	             {
	             wp.optionKey = wp.colStr("kk_data_code");
	             wp.initOption ="";
	             }
	          this.dddwList("dddw_lost_code4"
	                 ,"ptr_sys_idtab"
	                 ,"trim(wf_id)"
	                 ,"trim(wf_desc)"
	                 ," where wf_type= 'CARD_NOTE'");
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_card_type"
	                 ,"ptr_card_type"
	                 ,"trim(card_type)"
	                 ,"trim(name)"
	                 ," where 1 = 1 ");
	         }
	      } catch(Exception ex){}	  
	  
	  
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
  public void commRecordGroupNo(String groupNo) throws Exception {
    commRecordGroupNo(groupNo, 0);
    return;
  }

  // ************************************************************************
  public void commRecordGroupNo(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " record_group_name as column_record_group_name "
          + " from web_record_group "  + " where 1 = 1 " 
//    	  + " and   record_group_no = '" + wp.colStr(ii, befStr + "record_group_no") + "'";
      	  + " and record_group_no = :record_group_no";
      	  setString("record_group_no",wp.colStr(ii, befStr + "record_group_no"));
      
      if (wp.colStr(ii, befStr + "record_group_no").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_record_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commFuncCode(String code) throws Exception {
    commFuncCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commFuncCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " fund_name as column_fund_name " + " from mkt_loan_parm "
          + " where 1 = 1 " 
//    	  + " and   fund_code = '" + wp.colStr(ii, befStr + "fund_code") + "'";
		  + " and fund_code = :fund_code";
  	      setString("fund_code",wp.colStr(ii, befStr + "fund_code"));
      if (wp.colStr(ii, befStr + "fund_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_fund_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commProgCode(String code) throws Exception {
    commProgCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commProgCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " prog_desc as column_prog_desc " + " from ibn_prog " + " where 1 = 1 "
//          + " and   prog_code = '" + wp.colStr(ii, befStr + "prog_code") + "'"
//          + " and   prog_s_date = '" + wp.colStr(ii, befStr + "prog_s_date") + "'";
      	  + " and prog_code = :prog_code"
          + " and prog_s_date = :prog_s_date";
	      setString("prog_code",wp.colStr(ii, befStr + "prog_code"));
	      setString("prog_s_date",wp.colStr(ii, befStr + "prog_s_date"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_prog_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commGiftNo(String giftNo) throws Exception {
    comGgiftNo(giftNo, 0);
    return;
  }

  // ************************************************************************
  public void comGgiftNo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from ibn_prog_gift "
          + " where 1 = 1 " 
//    	  + " and   prog_code = '" + wp.colStr(ii, befStr + "prog_code") + "'"
//          + " and   prog_s_date = '" + wp.colStr(ii, befStr + "prog_s_date") + "'"
//          + " and   gift_no = '" + wp.colStr(ii, befStr + "gift_no") + "'";
	      + " and prog_code = :prog_code"
	      + " and prog_s_date = :prog_s_date"
	      + " and gift_no = :gift_no";
	      setString("prog_code",wp.colStr(ii, befStr + "prog_code"));
	      setString("prog_s_date",wp.colStr(ii, befStr + "prog_s_date"));
	      setString("gift_no",wp.colStr(ii, befStr + "gift_no"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_gift_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commSpecGiftNo(String giftNo) throws Exception {
    commSpecGiftNo(giftNo, 0);
    return;
  }

  // ************************************************************************
  public void commSpecGiftNo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from mkt_spec_gift "
          + " where 1 = 1 " 
//    	  + " and   gift_no = '" + wp.colStr(ii, befStr + "spec_gift_no") + "'";
          + " and gift_no = :gift_no";
      	  setString("gift_no",wp.colStr(ii, befStr + "gift_no"));
      if (wp.colStr(ii, befStr + "spec_gift_no").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_gift_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commAchieveCond(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"符合就給", "期滿一次給"};
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
  public void commTypeSel(String cde1) throws Exception {
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
  public void commGroupCodeSel(String cde1) throws Exception {
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
  public void commSourceCodeSel(String cde1) throws Exception {
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
  public void commCardTypeSel(String cde1) throws Exception {
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
  public void commPromoteDeptSel(String cde1) throws Exception {
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
  public void commMccCodeSel(String cde1) throws Exception {
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
  public void commMerchantSel(String cde1) throws Exception {
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
  public void commMchtGroupSel(String cde1) throws Exception {
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
  public void commInMerchantSel(String cde1) throws Exception {
    String[] cde = {"0", "1"};
    String[] txt = {"全部", "指定"};
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
  public void commInMchtGroupSel(String cde1) throws Exception {
    String[] cde = {"0", "1"};
    String[] txt = {"全部", "指定"};
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
  public void commPosEntrySel(String cde1) throws Exception {
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
  public void commUcafSel(String cde1) throws Exception {
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
  public void commUciSel(String cde1) throws Exception {
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
  public void commHalAndor(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"且", "或"};
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
  public void commMultiFbType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"1.單一贈品(以本頁設定參數)", "2.多贈品回饋 (請至mktm6250維護)"};
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
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利點數", "2.現金回饋", "3.豐富點數", "4.贈品"};
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
  public void commGroupType(String cde1) throws Exception {
    String[] cde = {"group_type", "1", "2", "3", "4"};
    String[] txt = {"", "限信用卡兌換(限01,05,06)", "限 VD卡兌換(限90)", "全部任一卡片兌換(01,05,06,90)", "限特定卡號兌換"};
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
  public void commPurcFheedType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"by 單筆", "by 總和"};
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
  public void commThresholdSel(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"級距式", "條件式"};
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
  public void commPurchaseTypeSel(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"1.累積金額", "2.累積筆數"};
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
  public void initPage() {
    return;
  }

  // ************************************************************************
 public String listMktBnData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = '" + tableName + "'" + " and   data_key   = '" + dataKey + "'"
        + " and   data_type  = '" + dataType + "'";
    sqlSelect(sql1);

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }
 
//************************************************************************
String listMktImchannelList(String s1, String s2, String s3, String s4) throws Exception {
    String sql1 = "select "
            + " count(*) as column_data_cnt "
            + " from "
            + s1
            + " "
            + " where  active_code = ? ";
    sqlSelect(sql1, new Object[] {
            s3
    });

    if (sqlRowNum > 0)
        return (sqlStr("column_data_cnt"));

    return ("0");
}
//************************************************************************
public String procDynamicDddwCrtuser1(String string)  throws Exception
{
  String lsSql = "";

  lsSql = " select "
         + " b.crt_user as db_code, "
         + " max(b.crt_user||' '||a.usr_cname) as db_desc "
         + " from sec_user a,mkt_fstp_parm_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
}
//************************************************************************
public void commCrtuser(String user) throws Exception 
{
	commCrtuser(user,0);
return;
}
//************************************************************************
public void commCrtuser(String columnData1,int bef_type) throws Exception 
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
//         + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
         + " and usr_id = :usr_id "
         ;
    	 setString("usr_id",wp.colStr(ii, befStr + "usr_id"));
    if (wp.colStr(ii,befStr+"crt_user").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_usr_cname"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commNewFlag(String cde1) throws Exception 
{
String[] cde = {"1","2"};
String[] txt = {"全新卡友", "於核卡日前數日"};
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

//************************************************************************
public void commMchtseq(String cde1) throws Exception 
{
String[] cde = {"N","Y"};
String[] txt = {"N.本區判斷","Y.多贈品參數區判斷"};
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

//************************************************************************
public void comm_record_group_no(String groupNo) throws Exception 
{
comm_record_group_no(groupNo,0);
return;
}
//************************************************************************
public void comm_record_group_no(String columnData1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " record_group_name as column_record_group_name "
         + " from web_record_group "
         + " where 1 = 1 "
//         + " and   record_group_no = '"+wp.colStr(ii,befStr+"c_record_group_no")+"'"
         + " and record_group_no = :c_record_group_no "
         ;
    	 setString("c_record_group_no",wp.colStr(ii,befStr+"c_record_group_no"));
    if (wp.colStr(ii,befStr+"c_record_group_no").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_record_group_name"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commNopurcMgmpgm(String pgm) throws Exception 
{
	commNopurcMgmpgm(pgm,0);
return;
}
//************************************************************************
public void commNopurcMgmpgm(String columnData1,int bef_type) throws Exception 
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
//         + " and   wf_id = '"+wp.colStr(ii,befStr+"nopurc_msg_pgm")+"'"
         + " and wf_id = :nopurc_msg_pgm "
         + " and   wf_type = 'SMS_MSG_PGM' "
         ;
    	 setString("nopurc_msg_pgm",wp.colStr(ii,befStr+"nopurc_msg_pgm"));
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_wf_desc"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commHalfMgmPgm(String pgm) throws Exception 
{
commHalfMgmPgm(pgm,0);
return;
}
//************************************************************************
public void commHalfMgmPgm(String columnData1,int bef_type) throws Exception 
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
//         + " and   wf_id = '"+wp.colStr(ii,befStr+"half_msg_pgm")+"'"
         + " and wf_id = :half_msg_pgm "
         + " and   wf_type = 'SMS_MSG_PGM' "
         ;
    	 setString("half_msg_pgm",wp.colStr(ii,befStr+"half_msg_pgm"));
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_wf_desc"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}
//************************************************************************
public void commSendMgmpgm(String pgm) throws Exception 
{
	commSendMgmpgm(pgm,0);
return;
}
//************************************************************************
public void commSendMgmpgm(String columnData1,int bef_type) throws Exception 
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
//         + " and   wf_id = '"+wp.colStr(ii,befStr+"send_msg_pgm")+"'"
         + " and wf_id = :send_msg_pgm "
         + " and   wf_type = 'SMS_MSG_PGM' "
         ;
    	 setString("send_msg_pgm",wp.colStr(ii,befStr+"send_msg_pgm"));
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_wf_desc"); 
    wp.colSet(ii, columnData1, columnData);
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
           + " and table_name  =  'MKT_FSTP_PARM' "
             ;
if (wp.respHtml.equals("mktp6240_gncd"))
   wp.whereStr  += " and data_type  = 'G' ";
if (wp.respHtml.equals("mktp6240_actp"))
   wp.whereStr  += " and data_type  = '1' ";
if (wp.respHtml.equals("mktp6240_srcd"))
   wp.whereStr  += " and data_type  = '3' ";
if (wp.respHtml.equals("mktp6240_cdtp"))
   wp.whereStr  += " and data_type  = '4' ";
if (wp.respHtml.equals("mktp6240_pmdp"))
   wp.whereStr  += " and data_type  = '5' ";
if (wp.respHtml.equals("mktp6240_mccd"))
   wp.whereStr  += " and data_type  = '6' ";
if (wp.respHtml.equals("mktp6240_aaa1"))
   wp.whereStr  += " and data_type  = '8' ";
if (wp.respHtml.equals("mktp6240_aaat"))
   wp.whereStr  += " and data_type  = '10' ";
if (wp.respHtml.equals("mktp6240_ucaf"))
   wp.whereStr  += " and data_type  = '12' ";
if (wp.respHtml.equals("mktp6240_deci"))
   wp.whereStr  += " and data_type  = '13' ";
if (wp.respHtml.equals("mktp6240_apay"))
   wp.whereStr  += " and data_type  = 'H' ";
if (wp.respHtml.equals("mktp6240_smsa"))
   wp.whereStr  += " and data_type  = 'A' ";
if (wp.respHtml.equals("mktp6240_smsc"))
   wp.whereStr  += " and data_type  = 'C' ";
String whereCnt = wp.whereStr;
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key", wp.itemStr("active_code"));
//whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
whereCnt += " and data_key = :active_code";
setString("active_code",wp.itemStr("active_code"));
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
if (wp.respHtml.equals("mktp6240_gncd"))
 commDataCode04("comm_data_code");
if (wp.respHtml.equals("mktp6240_actp"))
 commDataCode01("comm_data_code");
if (wp.respHtml.equals("mktp6240_srcd"))
 commDataCode05("comm_data_code");
if (wp.respHtml.equals("mktp6240_cdtp"))
 commDataCode024("comm_data_code");
if (wp.respHtml.equals("mktp6240_pmdp"))
 commDataCode07("comm_data_code");
if (wp.respHtml.equals("mktp6240_mccd"))
 commDataCode07("comm_data_code");
if (wp.respHtml.equals("mktp6240_aaa1"))
 commDataCode34("comm_data_code");
if (wp.respHtml.equals("mktp6240_aaat"))
 commDataCode34("comm_data_code");
if (wp.respHtml.equals("mktp6240_apay"))
 commDataCode04("comm_data_code");
if (wp.respHtml.equals("mktp6240_smsa"))
 commDataCode04("comm_data_code");
if (wp.respHtml.equals("mktp6240_smsc"))
 commDataCode04("comm_data_code");
}

//************************************************************************
public void commDataCode34(String code34) throws Exception 
{
	commDataCode34(code34,0);
return;
}
//************************************************************************
public void commDataCode34(String columnData1,int bef_type) throws Exception 
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
//         + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + " and mcht_group_id = :data_code"
         ;
    	 setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcht_group_desc"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commDataCode07(String code) throws Exception 
{
	commDataCode07(code,0);
return;
}
//************************************************************************
public void commDataCode07(String columnData1,int bef_type) throws Exception 
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
//         + " and   mcc_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + " and mcc_code = :data_code"
         ;
    	 setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcc_remark"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commDataCode024(String code) throws Exception 
{
	commDataCode024(code,0);
return;
}
//************************************************************************
public void commDataCode024(String columnData1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " name as column_name "
         + " from ptr_card_type "
         + " where 1 = 1 "
//         + " and   card_type = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + " and card_type = :data_code "
         ;
    	 setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_name"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}
//************************************************************************
public void commDataCode05(String code) throws Exception 
{
	commDataCode05(code,0);
return;
}
//************************************************************************
public void commDataCode05(String columnData1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " source_name as column_source_name "
         + " from ptr_src_code "
         + " where 1 = 1 "
//         + " and   source_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + " and source_code = :data_code "
         ;
    	 setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_source_name"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public int selectBndataCount(String bndata_table,String whereStr ) throws Exception
{
String sql1 = "select count(*) as bndataCount"
            + " from " + bndata_table
            + " " + whereStr
            ;

sqlSelect(sql1);

return((int)sqlNum("bndataCount"));
}

//************************************************************************
public void commDataCode04(String ocde) throws Exception 
{
	commDataCode04(ocde,0);
return;
}
//************************************************************************
public void commDataCode04(String columnData1,int bef_type) throws Exception 
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
//         + " and   group_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + " and group_code = :data_code "
         ;
    	 setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_group_name"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commDataCode01(String code) throws Exception 
{
	commDataCode01(code,0);
return;
}
//************************************************************************
public void commDataCode01(String columnData1,int bef_type) throws Exception 
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
         + " from ptr_acct_type "
         + " where 1 = 1 "
//         + " and   acct_type = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + " and acct_type = :data_code"
         ;
         setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_chin_name"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void dataReadR4() throws Exception
{
	dataReadR4(0);
}
//************************************************************************
public void dataReadR4(int fromType) throws Exception
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
             + "data_code3, "
             + "mod_user as r2_mod_user "
             ;
wp.daoTable = bnTable ;
wp.whereStr = "where 1=1"
           + " and table_name  =  'MKT_FSTP_PARM' "
             ;
if (wp.respHtml.equals("mktp6240_gpcd"))
   wp.whereStr  += " and data_type  = '2' ";
if (wp.respHtml.equals("mktp6240_posn"))
   wp.whereStr  += " and data_type  = '11' ";
String whereCnt = wp.whereStr;
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key", wp.itemStr("active_code"));
//whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
whereCnt += " and data_key = :active_code";
setString("active_code",wp.itemStr("active_code"));
wp.whereStr  += " order by 4,5,6,7,8 ";
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
if (wp.respHtml.equals("mktp6240_gpcd"))
 commDataCode04("comm_data_code");
if (wp.respHtml.equals("mktp6240_posn"))
 commEntryMode("comm_data_code2");
}

//************************************************************************
public void commEntryMode(String mode) throws Exception 
{
	commEntryMode(mode,0);
return;
}
//************************************************************************
public void commEntryMode(String columnData1,int bef_type) throws Exception 
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
//         + " and   entry_mode = '"+wp.colStr(ii,befStr+"data_code2")+"'"
         + " and entry_mode = :data_code2"
         ;
    	 setString("data_code2",wp.colStr(ii,befStr+"data_code2"));
    if (wp.colStr(ii,befStr+"data_code2").length()==0)
       {
        wp.colSet(ii, columnData1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mode_desc"); 
    wp.colSet(ii, columnData1, columnData);
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
           + " and table_name  =  'MKT_FSTP_PARM' "
             ;
if (wp.respHtml.equals("mktp6240_mrcd"))
   wp.whereStr  += " and data_type  = '7' ";
if (wp.respHtml.equals("mktp6240_inmc"))
   wp.whereStr  += " and data_type  = '9' ";
String whereCnt = wp.whereStr;
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key", wp.itemStr("active_code"));
//whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
whereCnt += " and data_key = :active_code";
setString("active_code",wp.itemStr("active_code"));
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


}  // End of class
