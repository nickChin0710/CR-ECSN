/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110/11/15  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktp01;

import mktp01.Mktp0110Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0110 extends BaseProc {
  private String PROGNAME = "DM參數資料檔覆核作業處理程式108/01/29 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0110Func func = null;
  String rowid;
  String batchNo;
  String fstAprFlag = "";
  String orgTabName = "mkt_dm_parm_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchN_no = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0;
  int[] datachk_cnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
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
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_list_batch_no"), "a.list_batch_no", "like%")
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.aud_type," + "a.list_batch_no," + "a.list_desc," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.LIST_BATCH_NO";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commfunc_aud_type("aud_type");

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
      if (wp.itemStr("kk_list_batch_no").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.list_batch_no as list_batch_no," + "a.crt_user," + "a.list_desc," + "a.list_sel,"
        + "a.acct_type_sel," + "a.vd_flag," + "a.card_type_sel," + "a.bin_type_sel,"
        + "a.group_code_sel," + "a.id_dup_cond," + "a.source_code_sel," + "a.valid_card_flag,"
        + "a.valid_check_cond," + "a.valid_stop_days," + "a.excl_card_cond," + "a.sup_check_flag,"
        + "a.expire_chg_cond," + "a.apply_date_cond," + "a.apply_date_s," + "a.apply_date_e,"
        + "a.apply_excl_cond," + "a.apply_renew_cond," + "a.rcv_date_cond," + "a.rcv_date_s,"
        + "a.rcv_date_e," + "a.new_hldr_cond," + "a.new_hldr_days," + "a.new_hldr_card,"
        + "a.new_hldr_sup," + "a.activate_chk_flag," + "a.bir_mm_cond," + "a.bir_mm01,"
        + "a.bir_mm02," + "a.bir_mm03," + "a.bir_mm04," + "a.bir_mm05," + "a.bir_mm06,"
        + "a.bir_mm07," + "a.bir_mm08," + "a.bir_mm09," + "a.bir_mm10," + "a.bir_mm11,"
        + "a.bir_mm12," + "a.age_cond," + "a.age_s," + "a.age_e," + "a.sex_flag,"
        + "a.credit_limit_cond," + "a.credit_limit_s," + "a.credit_limit_e," + "a.use_limit_cond,"
        + "a.use_limit_s," + "a.use_limit_e," + "a.rc_credit_bal_cond," + "a.rc_credit_bal_s,"
        + "a.rc_credit_bal_e," + "a.rc_bal_rate_cond," + "a.rc_bal_rate_s," + "a.rc_bal_rate_e,"
        + "a.bonus_cond," + "a.bonus_bp_s," + "a.bonus_bp_e," + "a.fund_cond," + "a.fund_amt_s,"
        + "a.fund_amt_e," + "a.owe_amt_cond," + "a.owe_amt_months," + "a.owe_amt_condition,"
        + "a.owe_amt," + "a.credit_cond," + "a.credit_month," + "a.credit_type,"
        + "a.credit_condition," + "a.credit_mcode," + "a.block_code_sel," + "a.block_code_cond,"
        + "a.class_code_sel," + "a.addr_area_sel," + "a.purch_date_cond," + "a.purch_date_s,"
        + "a.purch_date_e," + "a.purch_issue_mm," + "a.system_date_mm," + "a.purch_issue_dd,"
        + "a.bl_cond," + "a.ca_cond," + "a.it_cond," + "a.id_cond," + "a.ao_cond," + "a.ot_cond,"
        + "a.dest_amt_cond," + "a.dest_amt_type," + "a.dest_amt_s," + "a.dest_amt_e,"
        + "a.dest_time_cond," + "a.purch_times_s," + "a.purch_times_e," + "a.ucaf_sel,"
        + "a.record_group_no," + "a.excl_foreigner_cond," + "a.excl_no_tm_cond,"
        + "a.excl_no_dm_cond," + "a.excl_bank_emp_cond," + "a.excl_no_edm_cond,"
        + "a.excl_no_sms_cond," + "a.excl_no_mbullet_cond," + "a.excl_list_cond,"
        + "a.excl_chgphone_cond," + "a.chgphone_date_s," + "a.chgphone_date_e";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(batchNo, "a.list_batch_no");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commListSel("comm_list_sel");
    commAcctTypeSel("comm_acct_type_sel");
    commCardYpeSel("comm_card_type_sel");
    commBinTypeSel("comm_bin_type_sel");
    commGroupCodeSel("comm_group_code_sel");
    commSourceCodeSel("comm_source_code_sel");
    commValidCardFlag("comm_valid_card_flag");
    commSupCheckFlag("comm_sup_check_flag");
    commACTIVATE("comm_activate_chk_flag");
    commSex("comm_sex_flag");
    commOweAmt("comm_owe_amt_condition");
    commCreditType("comm_credit_type");
    commCreditCond("comm_credit_condition");
    commBlockSel("comm_block_code_sel");
    commBlockCond("comm_block_code_cond");
    commClassCode("comm_class_code_sel");
    commAddrArea("comm_addr_area_sel");
    commPurchDate("comm_purch_date_cond");
    commDestType("comm_dest_amt_type");
    commUcaf("comm_ucaf_sel");
    checkButtonOff();
    batchNo = wp.colStr("list_batch_no");
    list_wkdata_aft();
    if (!wp.colStr("aud_type").equals("A"))
      dataRead_R3R();
    else
      commfunc_aud_type("aud_type");
  }

  // ************************************************************************
  public void dataRead_R3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_DM_PARM";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.list_batch_no as list_batch_no," + "a.crt_user as bef_crt_user,"
        + "a.list_desc as bef_list_desc," + "a.list_sel as bef_list_sel,"
        + "a.acct_type_sel as bef_acct_type_sel," + "a.vd_flag as bef_vd_flag,"
        + "a.card_type_sel as bef_card_type_sel," + "a.bin_type_sel as bef_bin_type_sel,"
        + "a.group_code_sel as bef_group_code_sel," + "a.id_dup_cond as bef_id_dup_cond,"
        + "a.source_code_sel as bef_source_code_sel," + "a.valid_card_flag as bef_valid_card_flag,"
        + "a.valid_check_cond as bef_valid_check_cond,"
        + "a.valid_stop_days as bef_valid_stop_days," + "a.excl_card_cond as bef_excl_card_cond,"
        + "a.sup_check_flag as bef_sup_check_flag," + "a.expire_chg_cond as bef_expire_chg_cond,"
        + "a.apply_date_cond as bef_apply_date_cond," + "a.apply_date_s as bef_apply_date_s,"
        + "a.apply_date_e as bef_apply_date_e," + "a.apply_excl_cond as bef_apply_excl_cond,"
        + "a.apply_renew_cond as bef_apply_renew_cond," + "a.rcv_date_cond as bef_rcv_date_cond,"
        + "a.rcv_date_s as bef_rcv_date_s," + "a.rcv_date_e as bef_rcv_date_e,"
        + "a.new_hldr_cond as bef_new_hldr_cond," + "a.new_hldr_days as bef_new_hldr_days,"
        + "a.new_hldr_card as bef_new_hldr_card," + "a.new_hldr_sup as bef_new_hldr_sup,"
        + "a.activate_chk_flag as bef_activate_chk_flag," + "a.bir_mm_cond as bef_bir_mm_cond,"
        + "a.bir_mm01 as bef_bir_mm01," + "a.bir_mm02 as bef_bir_mm02,"
        + "a.bir_mm03 as bef_bir_mm03," + "a.bir_mm04 as bef_bir_mm04,"
        + "a.bir_mm05 as bef_bir_mm05," + "a.bir_mm06 as bef_bir_mm06,"
        + "a.bir_mm07 as bef_bir_mm07," + "a.bir_mm08 as bef_bir_mm08,"
        + "a.bir_mm09 as bef_bir_mm09," + "a.bir_mm10 as bef_bir_mm10,"
        + "a.bir_mm11 as bef_bir_mm11," + "a.bir_mm12 as bef_bir_mm12,"
        + "a.age_cond as bef_age_cond," + "a.age_s as bef_age_s," + "a.age_e as bef_age_e,"
        + "a.sex_flag as bef_sex_flag," + "a.credit_limit_cond as bef_credit_limit_cond,"
        + "a.credit_limit_s as bef_credit_limit_s," + "a.credit_limit_e as bef_credit_limit_e,"
        + "a.use_limit_cond as bef_use_limit_cond," + "a.use_limit_s as bef_use_limit_s,"
        + "a.use_limit_e as bef_use_limit_e," + "a.rc_credit_bal_cond as bef_rc_credit_bal_cond,"
        + "a.rc_credit_bal_s as bef_rc_credit_bal_s," + "a.rc_credit_bal_e as bef_rc_credit_bal_e,"
        + "a.rc_bal_rate_cond as bef_rc_bal_rate_cond," + "a.rc_bal_rate_s as bef_rc_bal_rate_s,"
        + "a.rc_bal_rate_e as bef_rc_bal_rate_e," + "a.bonus_cond as bef_bonus_cond,"
        + "a.bonus_bp_s as bef_bonus_bp_s," + "a.bonus_bp_e as bef_bonus_bp_e,"
        + "a.fund_cond as bef_fund_cond," + "a.fund_amt_s as bef_fund_amt_s,"
        + "a.fund_amt_e as bef_fund_amt_e," + "a.owe_amt_cond as bef_owe_amt_cond,"
        + "a.owe_amt_months as bef_owe_amt_months,"
        + "a.owe_amt_condition as bef_owe_amt_condition," + "a.owe_amt as bef_owe_amt,"
        + "a.credit_cond as bef_credit_cond," + "a.credit_month as bef_credit_month,"
        + "a.credit_type as bef_credit_type," + "a.credit_condition as bef_credit_condition,"
        + "a.credit_mcode as bef_credit_mcode," + "a.block_code_sel as bef_block_code_sel,"
        + "a.block_code_cond as bef_block_code_cond," + "a.class_code_sel as bef_class_code_sel,"
        + "a.addr_area_sel as bef_addr_area_sel," + "a.purch_date_cond as bef_purch_date_cond,"
        + "a.purch_date_s as bef_purch_date_s," + "a.purch_date_e as bef_purch_date_e,"
        + "a.purch_issue_mm as bef_purch_issue_mm," + "a.system_date_mm as bef_system_date_mm,"
        + "a.purch_issue_dd as bef_purch_issue_dd," + "a.bl_cond as bef_bl_cond,"
        + "a.ca_cond as bef_ca_cond," + "a.it_cond as bef_it_cond," + "a.id_cond as bef_id_cond,"
        + "a.ao_cond as bef_ao_cond," + "a.ot_cond as bef_ot_cond,"
        + "a.dest_amt_cond as bef_dest_amt_cond," + "a.dest_amt_type as bef_dest_amt_type,"
        + "a.dest_amt_s as bef_dest_amt_s," + "a.dest_amt_e as bef_dest_amt_e,"
        + "a.dest_time_cond as bef_dest_time_cond," + "a.purch_times_s as bef_purch_times_s,"
        + "a.purch_times_e as bef_purch_times_e," + "a.ucaf_sel as bef_ucaf_sel,"
        + "a.record_group_no as bef_record_group_no,"
        + "a.excl_foreigner_cond as bef_excl_foreigner_cond,"
        + "a.excl_no_tm_cond as bef_excl_no_tm_cond," + "a.excl_no_dm_cond as bef_excl_no_dm_cond,"
        + "a.excl_bank_emp_cond as bef_excl_bank_emp_cond,"
        + "a.excl_no_edm_cond as bef_excl_no_edm_cond,"
        + "a.excl_no_sms_cond as bef_excl_no_sms_cond,"
        + "a.excl_no_mbullet_cond as bef_excl_no_mbullet_cond,"
        + "a.excl_list_cond as bef_excl_list_cond,"
        + "a.excl_chgphone_cond as bef_excl_chgphone_cond,"
        + "a.chgphone_date_s as bef_chgphone_date_s," + "a.chgphone_date_e as bef_chgphone_date_e";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(batchNo, "a.list_batch_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commListSel("comm_list_sel");
    commAcctTypeSel("comm_acct_type_sel");
    commCardYpeSel("comm_card_type_sel");
    commBinTypeSel("comm_bin_type_sel");
    commGroupCodeSel("comm_group_code_sel");
    commSourceCodeSel("comm_source_code_sel");
    commValidCardFlag("comm_valid_card_flag");
    commSupCheckFlag("comm_sup_check_flag");
    commACTIVATE("comm_activate_chk_flag");
    commSex("comm_sex_flag");
    commOweAmt("comm_owe_amt_condition");
    commCreditType("comm_credit_type");
    commCreditCond("comm_credit_condition");
    commBlockSel("comm_block_code_sel");
    commBlockCond("comm_block_code_cond");
    commClassCode("comm_class_code_sel");
    commAddrArea("comm_addr_area_sel");
    commPurchDate("comm_purch_date_cond");
    commDestType("comm_dest_amt_type");
    commUcaf("comm_ucaf_sel");
    checkButtonOff();
    commfunc_aud_type("aud_type");
    list_wkdata();
  }

  // ************************************************************************
  void list_wkdata_aft() throws Exception {
    wp.colSet("acct_type_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "1"));
    wp.colSet("card_type_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "2"));
    wp.colSet("bin_type_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "3"));
    wp.colSet("group_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "4"));
    wp.colSet("source_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "5"));
    wp.colSet("block_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "6"));
    wp.colSet("class_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "7"));
    wp.colSet("addr_area_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "8"));
    wp.colSet("ucaf_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data_t", "MKT_DM_PARM", wp.colStr("list_batch_no"), "9"));
  }

  // ************************************************************************
  void list_wkdata() throws Exception {
    if (!wp.colStr("list_desc").equals(wp.colStr("bef_list_desc")))
      wp.colSet("opt_list_desc", "Y");

    if (!wp.colStr("list_sel").equals(wp.colStr("bef_list_sel")))
      wp.colSet("opt_list_sel", "Y");
    commListSel("comm_list_sel");
    commListSel("comm_bef_list_sel");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctTypeSel("comm_acct_type_sel");
    commAcctTypeSel("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "1"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("vd_flag").equals(wp.colStr("bef_vd_flag")))
      wp.colSet("opt_vd_flag", "Y");

    if (!wp.colStr("card_type_sel").equals(wp.colStr("bef_card_type_sel")))
      wp.colSet("opt_card_type_sel", "Y");
    commCardYpeSel("comm_card_type_sel");
    commCardYpeSel("comm_bef_card_type_sel");

    wp.colSet("bef_card_type_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "2"));
    if (!wp.colStr("card_type_sel_cnt").equals(wp.colStr("bef_card_type_sel_cnt")))
      wp.colSet("opt_card_type_sel_cnt", "Y");

    if (!wp.colStr("bin_type_sel").equals(wp.colStr("bef_bin_type_sel")))
      wp.colSet("opt_bin_type_sel", "Y");
    commBinTypeSel("comm_bin_type_sel");
    commBinTypeSel("comm_bef_bin_type_sel");

    wp.colSet("bef_bin_type_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "3"));
    if (!wp.colStr("bin_type_sel_cnt").equals(wp.colStr("bef_bin_type_sel_cnt")))
      wp.colSet("opt_bin_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCodeSel("comm_group_code_sel");
    commGroupCodeSel("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "4"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("id_dup_cond").equals(wp.colStr("bef_id_dup_cond")))
      wp.colSet("opt_id_dup_cond", "Y");

    if (!wp.colStr("source_code_sel").equals(wp.colStr("bef_source_code_sel")))
      wp.colSet("opt_source_code_sel", "Y");
    commSourceCodeSel("comm_source_code_sel");
    commSourceCodeSel("comm_bef_source_code_sel");

    wp.colSet("bef_source_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "5"));
    if (!wp.colStr("source_code_sel_cnt").equals(wp.colStr("bef_source_code_sel_cnt")))
      wp.colSet("opt_source_code_sel_cnt", "Y");

    if (!wp.colStr("valid_card_flag").equals(wp.colStr("bef_valid_card_flag")))
      wp.colSet("opt_valid_card_flag", "Y");
    commValidCardFlag("comm_valid_card_flag");
    commValidCardFlag("comm_bef_valid_card_flag");

    if (!wp.colStr("valid_check_cond").equals(wp.colStr("bef_valid_check_cond")))
      wp.colSet("opt_valid_check_cond", "Y");

    if (!wp.colStr("valid_stop_days").equals(wp.colStr("bef_valid_stop_days")))
      wp.colSet("opt_valid_stop_days", "Y");

    if (!wp.colStr("excl_card_cond").equals(wp.colStr("bef_excl_card_cond")))
      wp.colSet("opt_excl_card_cond", "Y");

    if (!wp.colStr("sup_check_flag").equals(wp.colStr("bef_sup_check_flag")))
      wp.colSet("opt_sup_check_flag", "Y");
    commSupCheckFlag("comm_sup_check_flag");
    commSupCheckFlag("comm_bef_sup_check_flag");

    if (!wp.colStr("expire_chg_cond").equals(wp.colStr("bef_expire_chg_cond")))
      wp.colSet("opt_expire_chg_cond", "Y");

    if (!wp.colStr("apply_date_cond").equals(wp.colStr("bef_apply_date_cond")))
      wp.colSet("opt_apply_date_cond", "Y");

    if (!wp.colStr("apply_date_s").equals(wp.colStr("bef_apply_date_s")))
      wp.colSet("opt_apply_date_s", "Y");

    if (!wp.colStr("apply_date_e").equals(wp.colStr("bef_apply_date_e")))
      wp.colSet("opt_apply_date_e", "Y");

    if (!wp.colStr("apply_excl_cond").equals(wp.colStr("bef_apply_excl_cond")))
      wp.colSet("opt_apply_excl_cond", "Y");

    if (!wp.colStr("apply_renew_cond").equals(wp.colStr("bef_apply_renew_cond")))
      wp.colSet("opt_apply_renew_cond", "Y");

    if (!wp.colStr("rcv_date_cond").equals(wp.colStr("bef_rcv_date_cond")))
      wp.colSet("opt_rcv_date_cond", "Y");

    if (!wp.colStr("rcv_date_s").equals(wp.colStr("bef_rcv_date_s")))
      wp.colSet("opt_rcv_date_s", "Y");

    if (!wp.colStr("rcv_date_e").equals(wp.colStr("bef_rcv_date_e")))
      wp.colSet("opt_rcv_date_e", "Y");

    if (!wp.colStr("new_hldr_cond").equals(wp.colStr("bef_new_hldr_cond")))
      wp.colSet("opt_new_hldr_cond", "Y");

    if (!wp.colStr("new_hldr_days").equals(wp.colStr("bef_new_hldr_days")))
      wp.colSet("opt_new_hldr_days", "Y");

    if (!wp.colStr("new_hldr_card").equals(wp.colStr("bef_new_hldr_card")))
      wp.colSet("opt_new_hldr_card", "Y");

    if (!wp.colStr("new_hldr_sup").equals(wp.colStr("bef_new_hldr_sup")))
      wp.colSet("opt_new_hldr_sup", "Y");

    if (!wp.colStr("activate_chk_flag").equals(wp.colStr("bef_activate_chk_flag")))
      wp.colSet("opt_activate_chk_flag", "Y");
    commACTIVATE("comm_activate_chk_flag");
    commACTIVATE("comm_bef_activate_chk_flag");

    if (!wp.colStr("bir_mm_cond").equals(wp.colStr("bef_bir_mm_cond")))
      wp.colSet("opt_bir_mm_cond", "Y");

    if (!wp.colStr("bir_mm01").equals(wp.colStr("bef_bir_mm01")))
      wp.colSet("opt_bir_mm01", "Y");

    if (!wp.colStr("bir_mm02").equals(wp.colStr("bef_bir_mm02")))
      wp.colSet("opt_bir_mm02", "Y");

    if (!wp.colStr("bir_mm03").equals(wp.colStr("bef_bir_mm03")))
      wp.colSet("opt_bir_mm03", "Y");

    if (!wp.colStr("bir_mm04").equals(wp.colStr("bef_bir_mm04")))
      wp.colSet("opt_bir_mm04", "Y");

    if (!wp.colStr("bir_mm05").equals(wp.colStr("bef_bir_mm05")))
      wp.colSet("opt_bir_mm05", "Y");

    if (!wp.colStr("bir_mm06").equals(wp.colStr("bef_bir_mm06")))
      wp.colSet("opt_bir_mm06", "Y");

    if (!wp.colStr("bir_mm07").equals(wp.colStr("bef_bir_mm07")))
      wp.colSet("opt_bir_mm07", "Y");

    if (!wp.colStr("bir_mm08").equals(wp.colStr("bef_bir_mm08")))
      wp.colSet("opt_bir_mm08", "Y");

    if (!wp.colStr("bir_mm09").equals(wp.colStr("bef_bir_mm09")))
      wp.colSet("opt_bir_mm09", "Y");

    if (!wp.colStr("bir_mm10").equals(wp.colStr("bef_bir_mm10")))
      wp.colSet("opt_bir_mm10", "Y");

    if (!wp.colStr("bir_mm11").equals(wp.colStr("bef_bir_mm11")))
      wp.colSet("opt_bir_mm11", "Y");

    if (!wp.colStr("bir_mm12").equals(wp.colStr("bef_bir_mm12")))
      wp.colSet("opt_bir_mm12", "Y");

    if (!wp.colStr("age_cond").equals(wp.colStr("bef_age_cond")))
      wp.colSet("opt_age_cond", "Y");

    if (!wp.colStr("age_s").equals(wp.colStr("bef_age_s")))
      wp.colSet("opt_age_s", "Y");

    if (!wp.colStr("age_e").equals(wp.colStr("bef_age_e")))
      wp.colSet("opt_age_e", "Y");

    if (!wp.colStr("sex_flag").equals(wp.colStr("bef_sex_flag")))
      wp.colSet("opt_sex_flag", "Y");
    commSex("comm_sex_flag");
    commSex("comm_bef_sex_flag");

    if (!wp.colStr("credit_limit_cond").equals(wp.colStr("bef_credit_limit_cond")))
      wp.colSet("opt_credit_limit_cond", "Y");

    if (!wp.colStr("credit_limit_s").equals(wp.colStr("bef_credit_limit_s")))
      wp.colSet("opt_credit_limit_s", "Y");

    if (!wp.colStr("credit_limit_e").equals(wp.colStr("bef_credit_limit_e")))
      wp.colSet("opt_credit_limit_e", "Y");

    if (!wp.colStr("use_limit_cond").equals(wp.colStr("bef_use_limit_cond")))
      wp.colSet("opt_use_limit_cond", "Y");

    if (!wp.colStr("use_limit_s").equals(wp.colStr("bef_use_limit_s")))
      wp.colSet("opt_use_limit_s", "Y");

    if (!wp.colStr("use_limit_e").equals(wp.colStr("bef_use_limit_e")))
      wp.colSet("opt_use_limit_e", "Y");

    if (!wp.colStr("rc_credit_bal_cond").equals(wp.colStr("bef_rc_credit_bal_cond")))
      wp.colSet("opt_rc_credit_bal_cond", "Y");

    if (!wp.colStr("rc_credit_bal_s").equals(wp.colStr("bef_rc_credit_bal_s")))
      wp.colSet("opt_rc_credit_bal_s", "Y");

    if (!wp.colStr("rc_credit_bal_e").equals(wp.colStr("bef_rc_credit_bal_e")))
      wp.colSet("opt_rc_credit_bal_e", "Y");

    if (!wp.colStr("rc_bal_rate_cond").equals(wp.colStr("bef_rc_bal_rate_cond")))
      wp.colSet("opt_rc_bal_rate_cond", "Y");

    if (!wp.colStr("rc_bal_rate_s").equals(wp.colStr("bef_rc_bal_rate_s")))
      wp.colSet("opt_rc_bal_rate_s", "Y");

    if (!wp.colStr("rc_bal_rate_e").equals(wp.colStr("bef_rc_bal_rate_e")))
      wp.colSet("opt_rc_bal_rate_e", "Y");

    if (!wp.colStr("bonus_cond").equals(wp.colStr("bef_bonus_cond")))
      wp.colSet("opt_bonus_cond", "Y");

    if (!wp.colStr("bonus_bp_s").equals(wp.colStr("bef_bonus_bp_s")))
      wp.colSet("opt_bonus_bp_s", "Y");

    if (!wp.colStr("bonus_bp_e").equals(wp.colStr("bef_bonus_bp_e")))
      wp.colSet("opt_bonus_bp_e", "Y");

    if (!wp.colStr("fund_cond").equals(wp.colStr("bef_fund_cond")))
      wp.colSet("opt_fund_cond", "Y");

    if (!wp.colStr("fund_amt_s").equals(wp.colStr("bef_fund_amt_s")))
      wp.colSet("opt_fund_amt_s", "Y");

    if (!wp.colStr("fund_amt_e").equals(wp.colStr("bef_fund_amt_e")))
      wp.colSet("opt_fund_amt_e", "Y");

    if (!wp.colStr("owe_amt_cond").equals(wp.colStr("bef_owe_amt_cond")))
      wp.colSet("opt_owe_amt_cond", "Y");

    if (!wp.colStr("owe_amt_months").equals(wp.colStr("bef_owe_amt_months")))
      wp.colSet("opt_owe_amt_months", "Y");

    if (!wp.colStr("owe_amt_condition").equals(wp.colStr("bef_owe_amt_condition")))
      wp.colSet("opt_owe_amt_condition", "Y");
    commOweAmt("comm_owe_amt_condition");
    commOweAmt("comm_bef_owe_amt_condition");

    if (!wp.colStr("owe_amt").equals(wp.colStr("bef_owe_amt")))
      wp.colSet("opt_owe_amt", "Y");

    if (!wp.colStr("credit_cond").equals(wp.colStr("bef_credit_cond")))
      wp.colSet("opt_credit_cond", "Y");

    if (!wp.colStr("credit_month").equals(wp.colStr("bef_credit_month")))
      wp.colSet("opt_credit_month", "Y");

    if (!wp.colStr("credit_type").equals(wp.colStr("bef_credit_type")))
      wp.colSet("opt_credit_type", "Y");
    commCreditType("comm_credit_type");
    commCreditType("comm_bef_credit_type");

    if (!wp.colStr("credit_condition").equals(wp.colStr("bef_credit_condition")))
      wp.colSet("opt_credit_condition", "Y");
    commCreditCond("comm_credit_condition");
    commCreditCond("comm_bef_credit_condition");

    if (!wp.colStr("credit_mcode").equals(wp.colStr("bef_credit_mcode")))
      wp.colSet("opt_credit_mcode", "Y");

    if (!wp.colStr("block_code_sel").equals(wp.colStr("bef_block_code_sel")))
      wp.colSet("opt_block_code_sel", "Y");
    commBlockSel("comm_block_code_sel");
    commBlockSel("comm_bef_block_code_sel");

    wp.colSet("bef_block_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "6"));
    if (!wp.colStr("block_code_sel_cnt").equals(wp.colStr("bef_block_code_sel_cnt")))
      wp.colSet("opt_block_code_sel_cnt", "Y");

    if (!wp.colStr("block_code_cond").equals(wp.colStr("bef_block_code_cond")))
      wp.colSet("opt_block_code_cond", "Y");
    commBlockCond("comm_block_code_cond");
    commBlockCond("comm_bef_block_code_cond");

    if (!wp.colStr("class_code_sel").equals(wp.colStr("bef_class_code_sel")))
      wp.colSet("opt_class_code_sel", "Y");
    commClassCode("comm_class_code_sel");
    commClassCode("comm_bef_class_code_sel");

    wp.colSet("bef_class_code_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "7"));
    if (!wp.colStr("class_code_sel_cnt").equals(wp.colStr("bef_class_code_sel_cnt")))
      wp.colSet("opt_class_code_sel_cnt", "Y");

    if (!wp.colStr("addr_area_sel").equals(wp.colStr("bef_addr_area_sel")))
      wp.colSet("opt_addr_area_sel", "Y");
    commAddrArea("comm_addr_area_sel");
    commAddrArea("comm_bef_addr_area_sel");

    wp.colSet("bef_addr_area_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "8"));
    if (!wp.colStr("addr_area_sel_cnt").equals(wp.colStr("bef_addr_area_sel_cnt")))
      wp.colSet("opt_addr_area_sel_cnt", "Y");

    if (!wp.colStr("purch_date_cond").equals(wp.colStr("bef_purch_date_cond")))
      wp.colSet("opt_purch_date_cond", "Y");
    commPurchDate("comm_purch_date_cond");
    commPurchDate("comm_bef_purch_date_cond");

    if (!wp.colStr("purch_date_s").equals(wp.colStr("bef_purch_date_s")))
      wp.colSet("opt_purch_date_s", "Y");

    if (!wp.colStr("purch_date_e").equals(wp.colStr("bef_purch_date_e")))
      wp.colSet("opt_purch_date_e", "Y");

    if (!wp.colStr("purch_issue_mm").equals(wp.colStr("bef_purch_issue_mm")))
      wp.colSet("opt_purch_issue_mm", "Y");

    if (!wp.colStr("system_date_mm").equals(wp.colStr("bef_system_date_mm")))
      wp.colSet("opt_system_date_mm", "Y");

    if (!wp.colStr("purch_issue_dd").equals(wp.colStr("bef_purch_issue_dd")))
      wp.colSet("opt_purch_issue_dd", "Y");

    if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
      wp.colSet("opt_bl_cond", "Y");

    if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
      wp.colSet("opt_ca_cond", "Y");

    if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
      wp.colSet("opt_it_cond", "Y");

    if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
      wp.colSet("opt_id_cond", "Y");

    if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
      wp.colSet("opt_ao_cond", "Y");

    if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
      wp.colSet("opt_ot_cond", "Y");

    if (!wp.colStr("dest_amt_cond").equals(wp.colStr("bef_dest_amt_cond")))
      wp.colSet("opt_dest_amt_cond", "Y");

    if (!wp.colStr("dest_amt_type").equals(wp.colStr("bef_dest_amt_type")))
      wp.colSet("opt_dest_amt_type", "Y");
    commDestType("comm_dest_amt_type");
    commDestType("comm_bef_dest_amt_type");

    if (!wp.colStr("dest_amt_s").equals(wp.colStr("bef_dest_amt_s")))
      wp.colSet("opt_dest_amt_s", "Y");

    if (!wp.colStr("dest_amt_e").equals(wp.colStr("bef_dest_amt_e")))
      wp.colSet("opt_dest_amt_e", "Y");

    if (!wp.colStr("dest_time_cond").equals(wp.colStr("bef_dest_time_cond")))
      wp.colSet("opt_dest_time_cond", "Y");

    if (!wp.colStr("purch_times_s").equals(wp.colStr("bef_purch_times_s")))
      wp.colSet("opt_purch_times_s", "Y");

    if (!wp.colStr("purch_times_e").equals(wp.colStr("bef_purch_times_e")))
      wp.colSet("opt_purch_times_e", "Y");

    if (!wp.colStr("ucaf_sel").equals(wp.colStr("bef_ucaf_sel")))
      wp.colSet("opt_ucaf_sel", "Y");
    commUcaf("comm_ucaf_sel");
    commUcaf("comm_bef_ucaf_sel");

    wp.colSet("bef_ucaf_sel_cnt",
        listMktDmBnData("mkt_dm_bn_data", "MKT_DM_PARM", wp.colStr("list_batch_no"), "9"));
    if (!wp.colStr("ucaf_sel_cnt").equals(wp.colStr("bef_ucaf_sel_cnt")))
      wp.colSet("opt_ucaf_sel_cnt", "Y");

    if (!wp.colStr("record_group_no").equals(wp.colStr("bef_record_group_no")))
      wp.colSet("opt_record_group_no", "Y");

    if (!wp.colStr("excl_foreigner_cond").equals(wp.colStr("bef_excl_foreigner_cond")))
      wp.colSet("opt_excl_foreigner_cond", "Y");

    if (!wp.colStr("excl_no_tm_cond").equals(wp.colStr("bef_excl_no_tm_cond")))
      wp.colSet("opt_excl_no_tm_cond", "Y");

    if (!wp.colStr("excl_no_dm_cond").equals(wp.colStr("bef_excl_no_dm_cond")))
      wp.colSet("opt_excl_no_dm_cond", "Y");

    if (!wp.colStr("excl_bank_emp_cond").equals(wp.colStr("bef_excl_bank_emp_cond")))
      wp.colSet("opt_excl_bank_emp_cond", "Y");

    if (!wp.colStr("excl_no_edm_cond").equals(wp.colStr("bef_excl_no_edm_cond")))
      wp.colSet("opt_excl_no_edm_cond", "Y");

    if (!wp.colStr("excl_no_sms_cond").equals(wp.colStr("bef_excl_no_sms_cond")))
      wp.colSet("opt_excl_no_sms_cond", "Y");

    if (!wp.colStr("excl_no_mbullet_cond").equals(wp.colStr("bef_excl_no_mbullet_cond")))
      wp.colSet("opt_excl_no_mbullet_cond", "Y");

    if (!wp.colStr("excl_list_cond").equals(wp.colStr("bef_excl_list_cond")))
      wp.colSet("opt_excl_list_cond", "Y");

    if (!wp.colStr("excl_chgphone_cond").equals(wp.colStr("bef_excl_chgphone_cond")))
      wp.colSet("opt_excl_chgphone_cond", "Y");

    if (!wp.colStr("chgphone_date_s").equals(wp.colStr("bef_chgphone_date_s")))
      wp.colSet("opt_chgphone_date_s", "Y");

    if (!wp.colStr("chgphone_date_e").equals(wp.colStr("bef_chgphone_date_e")))
      wp.colSet("opt_chgphone_date_e", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("list_desc", "");
      wp.colSet("list_sel", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
      wp.colSet("vd_flag", "");
      wp.colSet("card_type_sel", "");
      wp.colSet("card_type_sel_cnt", "");
      wp.colSet("bin_type_sel", "");
      wp.colSet("bin_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("id_dup_cond", "");
      wp.colSet("source_code_sel", "");
      wp.colSet("source_code_sel_cnt", "");
      wp.colSet("valid_card_flag", "");
      wp.colSet("valid_check_cond", "");
      wp.colSet("valid_stop_days", "");
      wp.colSet("excl_card_cond", "");
      wp.colSet("sup_check_flag", "");
      wp.colSet("expire_chg_cond", "");
      wp.colSet("apply_date_cond", "");
      wp.colSet("apply_date_s", "");
      wp.colSet("apply_date_e", "");
      wp.colSet("apply_excl_cond", "");
      wp.colSet("apply_renew_cond", "");
      wp.colSet("rcv_date_cond", "");
      wp.colSet("rcv_date_s", "");
      wp.colSet("rcv_date_e", "");
      wp.colSet("new_hldr_cond", "");
      wp.colSet("new_hldr_days", "");
      wp.colSet("new_hldr_card", "");
      wp.colSet("new_hldr_sup", "");
      wp.colSet("activate_chk_flag", "");
      wp.colSet("bir_mm_cond", "");
      wp.colSet("bir_mm01", "");
      wp.colSet("bir_mm02", "");
      wp.colSet("bir_mm03", "");
      wp.colSet("bir_mm04", "");
      wp.colSet("bir_mm05", "");
      wp.colSet("bir_mm06", "");
      wp.colSet("bir_mm07", "");
      wp.colSet("bir_mm08", "");
      wp.colSet("bir_mm09", "");
      wp.colSet("bir_mm10", "");
      wp.colSet("bir_mm11", "");
      wp.colSet("bir_mm12", "");
      wp.colSet("age_cond", "");
      wp.colSet("age_s", "");
      wp.colSet("age_e", "");
      wp.colSet("sex_flag", "");
      wp.colSet("credit_limit_cond", "");
      wp.colSet("credit_limit_s", "");
      wp.colSet("credit_limit_e", "");
      wp.colSet("use_limit_cond", "");
      wp.colSet("use_limit_s", "");
      wp.colSet("use_limit_e", "");
      wp.colSet("rc_credit_bal_cond", "");
      wp.colSet("rc_credit_bal_s", "");
      wp.colSet("rc_credit_bal_e", "");
      wp.colSet("rc_bal_rate_cond", "");
      wp.colSet("rc_bal_rate_s", "");
      wp.colSet("rc_bal_rate_e", "");
      wp.colSet("bonus_cond", "");
      wp.colSet("bonus_bp_s", "");
      wp.colSet("bonus_bp_e", "");
      wp.colSet("fund_cond", "");
      wp.colSet("fund_amt_s", "");
      wp.colSet("fund_amt_e", "");
      wp.colSet("owe_amt_cond", "");
      wp.colSet("owe_amt_months", "");
      wp.colSet("owe_amt_condition", "");
      wp.colSet("owe_amt", "");
      wp.colSet("credit_cond", "");
      wp.colSet("credit_month", "");
      wp.colSet("credit_type", "");
      wp.colSet("credit_condition", "");
      wp.colSet("credit_mcode", "");
      wp.colSet("block_code_sel", "");
      wp.colSet("block_code_sel_cnt", "");
      wp.colSet("block_code_cond", "");
      wp.colSet("class_code_sel", "");
      wp.colSet("class_code_sel_cnt", "");
      wp.colSet("addr_area_sel", "");
      wp.colSet("addr_area_sel_cnt", "");
      wp.colSet("purch_date_cond", "");
      wp.colSet("purch_date_s", "");
      wp.colSet("purch_date_e", "");
      wp.colSet("purch_issue_mm", "");
      wp.colSet("system_date_mm", "");
      wp.colSet("purch_issue_dd", "");
      wp.colSet("bl_cond", "");
      wp.colSet("ca_cond", "");
      wp.colSet("it_cond", "");
      wp.colSet("id_cond", "");
      wp.colSet("ao_cond", "");
      wp.colSet("ot_cond", "");
      wp.colSet("dest_amt_cond", "");
      wp.colSet("dest_amt_type", "");
      wp.colSet("dest_amt_s", "");
      wp.colSet("dest_amt_e", "");
      wp.colSet("dest_time_cond", "");
      wp.colSet("purch_times_s", "");
      wp.colSet("purch_times_e", "");
      wp.colSet("ucaf_sel", "");
      wp.colSet("ucaf_sel_cnt", "");
      wp.colSet("record_group_no", "");
      wp.colSet("excl_foreigner_cond", "");
      wp.colSet("excl_no_tm_cond", "");
      wp.colSet("excl_no_dm_cond", "");
      wp.colSet("excl_bank_emp_cond", "");
      wp.colSet("excl_no_edm_cond", "");
      wp.colSet("excl_no_sms_cond", "");
      wp.colSet("excl_no_mbullet_cond", "");
      wp.colSet("excl_list_cond", "");
      wp.colSet("excl_chgphone_cond", "");
      wp.colSet("chgphone_date_s", "");
      wp.colSet("chgphone_date_e", "");
    }
  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int il_ok = 0;
    int il_err = 0;
    mktp01.Mktp0110Func func = new mktp01.Mktp0110Func(wp);

    String[] ls_list_batch_no = wp.itemBuff("list_batch_no");
    String[] ls_aud_type = wp.itemBuff("aud_type");
    String[] ls_rowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = ls_aud_type.length;

    int rr = -1;
    wp.selectCnt = ls_aud_type.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + ls_rowid[rr]);

      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("list_batch_no", ls_list_batch_no[rr]);
      func.varsSet("aud_type", ls_aud_type[rr]);
      func.varsSet("rowid", ls_rowid[rr]);
      wp.itemSet("wprowid", ls_rowid[rr]);
      if (ls_aud_type[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
        if (rc == 1)
          rc = func.dbInsertA4Dmlist();
        if (rc == 1)
          rc = func.dbDeleteD4TDmlist();
      } else if (ls_aud_type[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4bBndata();
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
      } else if (ls_aud_type[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4bBndata();
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
        commfunc_aud_type("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        il_ok++;
        func.dbDelete();
        func.dbDeleteD4bBndata();
        func.dbDeleteD4Dmlist();
        this.sqlCommit(rc);
        continue;
      }
      il_err++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
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
    try {
      if ((wp.respHtml.equals("mktp0110"))) {
        if (wp.colStr("record_group_no").length() > 0) {
          wp.optionKey = wp.colStr("record_group_no");
        }
        this.dddwList("dddw_record_gp", "web_record_group", "trim(record_group_no)",
            "trim(record_group_name)", " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  void commfunc_aud_type(String cde1) {
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
  public void commListSel(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4", "5", "6", "7"};
    String[] txt = {"id", "p_seqno", "統編", "卡號", "TPAN卡號", "悠遊卡卡號+回饋金額", "一卡通卡號+回饋金額"};
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
  public void commAcctTypeSel(String cde1) throws Exception {
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
  public void commCardYpeSel(String cde1) throws Exception {
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
  public void commBinTypeSel(String cde1) throws Exception {
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
  public void commValidCardFlag(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "有效卡", "無效卡"};
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
  public void commSupCheckFlag(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "正卡", "附卡"};
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
  public void commACTIVATE(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "未開卡", "已開卡"};
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
  public void commSex(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "男", "女"};
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
  public void commOweAmt(String cde1) throws Exception {
    String[] cde = {"1", "2", "3"};
    String[] txt = {">=", "<=", "="};
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
  public void commCreditType(String cde1) throws Exception {
    String[] cde = {"=1", "2"};
    String[] txt = {"全部符合", "任一符合"};
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
  public void commCreditCond(String cde1) throws Exception {
    String[] cde = {"=1", "2", "3"};
    String[] txt = {">=", "<=", "="};
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
  public void commBlockSel(String cde1) throws Exception {
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
  public void commBlockCond(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"有凍結碼", "無凍結碼"};
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
  public void commClassCode(String cde1) throws Exception {
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
  public void commAddrArea(String cd1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "指定", "排除"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = cd1.substring(5, cd1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, cd1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commPurchDate(String cde1) throws Exception {
    String[] cde = {"=0", "1", "2", "3", "4"};
    String[] txt = {"不考慮消費條件", "消費期間", "發卡後", "最近", "發卡後"};
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
  public void commDestType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"單筆金額", "累積金額"};
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
  public void commUcaf(String cde1) throws Exception {
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
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }

  // ************************************************************************
  String listMktDmBnData(String table, String tableName, String dataKey, String dataType) throws Exception {
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
