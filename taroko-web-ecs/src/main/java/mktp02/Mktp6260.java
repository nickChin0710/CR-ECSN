/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/15  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名       
* 110-11-08  V1.00.03  machao     SQL Injection                                                                              *    
***************************************************************************/
package mktp02;

import mktp02.Mktp6260Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6260 extends BaseProc {
  private String PROGNAME = "首刷禮活動回饋參數覆核處理程式108/08/15 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6260Func func = null;
  String rowid;
  String activeMode;
  String fstAprAlag = "";
  String orgTabName = "mkt_mcht_parm_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0;
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
        + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code,a.crt_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.active_code as active_code," + "a.crt_user," + "a.active_name," + "a.stop_flag,"
        + "a.stop_date," + "a.stop_desc," + "a.active_type," + "a.bonus_type," + "a.tax_flag,"
        + "a.fund_code," + "a.effect_months," + "a.purchase_date_s," + "a.purchase_date_e,"
        + "a.feedback_date," + "a.feedback_key_sel," + "a.issue_date_cond," + "a.issue_date_s,"
        + "a.issue_date_e," + "a.new_hldr_sel," + "a.new_hldr_days," + "a.new_group_cond,"
        + "a.new_hldr_card," + "a.new_hldr_sup," + "a.acct_type_sel," + "a.group_code_sel,"
        + "a.record_cond," + "a.record_group_no," + "a.record_purc_flag," + "a.record_n1_days,"
        + "a.record_n2_days," + "a.bl_cond," + "a.it_cond," + "a.merchant_sel,"
        + "a.mcht_group_sel," + "a.in_merchant_sel," + "a.in_mcht_group_sel," + "a.mcht_in_cond,"
        + "a.mcht_in_cnt," + "a.mcht_in_per_amt," + "a.mcht_in_amt," + "a.mcc_code_sel,"
        + "a.pos_entry_sel," + "a.per_amt_cond," + "a.per_amt," + "a.sum_cnt_cond," + "a.sum_cnt,"
        + "a.sum_amt_cond," + "a.sum_amt," + "a.feedback_rate," + "a.feedback_add_amt,"
        + "a.exchange_amt," + "a.feedback_lmtamt_cond," + "a.feedback_lmt_amt,"
        + "a.feedback_lmtcnt_cond," + "a.feedback_lmt_cnt," + "a.day_lmtamt_cond,"
        + "a.day_lmt_amt," + "a.day_lmtcnt_cond," + "a.day_lmt_cnt," + "a.times_lmtamt_cond,"
        + "a.times_lmt_amt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(activeMode, "a.active_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commActiveType("comm_active_type");
    commTaxFlag("comm_tax_flag");
    commFeedbackKeySel("comm_feedback_key_sel");
    commNewHldrSel("comm_new_hldr_sel");
    commAcctTypeSel("comm_acct_type_sel");
    commGroupCodeSel("comm_group_code_sel");
    commRecordPurcFlag("comm_record_purc_flag");
    commMerchantSel("comm_merchant_sel");
    commMchtGroupSel("comm_mcht_group_sel");
    commInMerchantSel("comm_in_merchant_sel");
    commInMchtGroupSel("comm_in_mcht_group_sel");
    commInCond("comm_mcht_in_cond");
    commMccCodeSel("comm_mcc_code_sel");
    commPosEntrySel("comm_pos_entry_sel");
    commFuncCode("comm_fund_code");
    checkButtonOff();
    activeMode = wp.colStr("active_code");
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
    controlTabName = "mkt_mcht_parm";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.active_code as active_code," + "a.crt_user as bef_crt_user,"
        + "a.active_name as bef_active_name," + "a.stop_flag as bef_stop_flag,"
        + "a.stop_date as bef_stop_date," + "a.stop_desc as bef_stop_desc,"
        + "a.active_type as bef_active_type," + "a.bonus_type as bef_bonus_type,"
        + "a.tax_flag as bef_tax_flag," + "a.fund_code as bef_fund_code,"
        + "a.effect_months as bef_effect_months," + "a.purchase_date_s as bef_purchase_date_s,"
        + "a.purchase_date_e as bef_purchase_date_e," + "a.feedback_date as bef_feedback_date,"
        + "a.feedback_key_sel as bef_feedback_key_sel,"
        + "a.issue_date_cond as bef_issue_date_cond," + "a.issue_date_s as bef_issue_date_s,"
        + "a.issue_date_e as bef_issue_date_e," + "a.new_hldr_sel as bef_new_hldr_sel,"
        + "a.new_hldr_days as bef_new_hldr_days," + "a.new_group_cond as bef_new_group_cond,"
        + "a.new_hldr_card as bef_new_hldr_card," + "a.new_hldr_sup as bef_new_hldr_sup,"
        + "a.acct_type_sel as bef_acct_type_sel," + "a.group_code_sel as bef_group_code_sel,"
        + "a.record_cond as bef_record_cond," + "a.record_group_no as bef_record_group_no,"
        + "a.record_purc_flag as bef_record_purc_flag," + "a.record_n1_days as bef_record_n1_days,"
        + "a.record_n2_days as bef_record_n2_days," + "a.bl_cond as bef_bl_cond,"
        + "a.it_cond as bef_it_cond," + "a.merchant_sel as bef_merchant_sel,"
        + "a.mcht_group_sel as bef_mcht_group_sel," + "a.in_merchant_sel as bef_in_merchant_sel,"
        + "a.in_mcht_group_sel as bef_in_mcht_group_sel," + "a.mcht_in_cond as bef_mcht_in_cond,"
        + "a.mcht_in_cnt as bef_mcht_in_cnt," + "a.mcht_in_per_amt as bef_mcht_in_per_amt,"
        + "a.mcht_in_amt as bef_mcht_in_amt," + "a.mcc_code_sel as bef_mcc_code_sel,"
        + "a.pos_entry_sel as bef_pos_entry_sel," + "a.per_amt_cond as bef_per_amt_cond,"
        + "a.per_amt as bef_per_amt," + "a.sum_cnt_cond as bef_sum_cnt_cond,"
        + "a.sum_cnt as bef_sum_cnt," + "a.sum_amt_cond as bef_sum_amt_cond,"
        + "a.sum_amt as bef_sum_amt," + "a.feedback_rate as bef_feedback_rate,"
        + "a.feedback_add_amt as bef_feedback_add_amt," + "a.exchange_amt as bef_exchange_amt,"
        + "a.feedback_lmtamt_cond as bef_feedback_lmtamt_cond,"
        + "a.feedback_lmt_amt as bef_feedback_lmt_amt,"
        + "a.feedback_lmtcnt_cond as bef_feedback_lmtcnt_cond,"
        + "a.feedback_lmt_cnt as bef_feedback_lmt_cnt,"
        + "a.day_lmtamt_cond as bef_day_lmtamt_cond," + "a.day_lmt_amt as bef_day_lmt_amt,"
        + "a.day_lmtcnt_cond as bef_day_lmtcnt_cond," + "a.day_lmt_cnt as bef_day_lmt_cnt,"
        + "a.times_lmtamt_cond as bef_times_lmtamt_cond," + "a.times_lmt_amt as bef_times_lmt_amt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeMode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commActiveType("comm_active_type");
    commTaxFlag("comm_tax_flag");
    commFuncCode("comm_fund_code");
    commFeedbackKeySel("comm_feedback_key_sel");
    commNewHldrSel("comm_new_hldr_sel");
    commAcctTypeSel("comm_acct_type_sel");
    commGroupCodeSel("comm_group_code_sel");
    commRecordPurcFlag("comm_record_purc_flag");
    commMerchantSel("comm_merchant_sel");
    commMchtGroupSel("comm_mcht_group_sel");
    commInMerchantSel("comm_in_merchant_sel");
    commInMchtGroupSel("comm_in_mcht_group_sel");
    commInCond("comm_mcht_in_cond");
    commMccCodeSel("comm_mcc_code_sel");
    commPosEntrySel("comm_pos_entry_sel");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("new_group_cond_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "F"));
    wp.colSet("acct_type_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "2"));
    wp.colSet("record_cond_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "H"));
    wp.colSet("merchant_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "7"));
    wp.colSet("mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "8"));
    wp.colSet("in_merchant_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "9"));
    wp.colSet("in_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "10"));
    wp.colSet("mcc_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "6"));
    wp.colSet("pos_entry_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_MCHT_PARM", wp.colStr("active_code"), "11"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
      wp.colSet("opt_active_name", "Y");

    if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
      wp.colSet("opt_stop_flag", "Y");

    if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
      wp.colSet("opt_stop_date", "Y");

    if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
      wp.colSet("opt_stop_desc", "Y");

    if (!wp.colStr("active_type").equals(wp.colStr("bef_active_type")))
      wp.colSet("opt_active_type", "Y");
    commActiveType("comm_active_type");
    commActiveType("comm_bef_active_type");

    if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
      wp.colSet("opt_bonus_type", "Y");

    if (!wp.colStr("tax_flag").equals(wp.colStr("bef_tax_flag")))
      wp.colSet("opt_tax_flag", "Y");
    commTaxFlag("comm_tax_flag");
    commTaxFlag("comm_bef_tax_flag");

    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code", "Y");
    commFuncCode("comm_fund_code");
    commFuncCode("comm_bef_fund_code", 1);

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months", "Y");

    if (!wp.colStr("purchase_date_s").equals(wp.colStr("bef_purchase_date_s")))
      wp.colSet("opt_purchase_date_s", "Y");

    if (!wp.colStr("purchase_date_e").equals(wp.colStr("bef_purchase_date_e")))
      wp.colSet("opt_purchase_date_e", "Y");

    if (!wp.colStr("feedback_date").equals(wp.colStr("bef_feedback_date")))
      wp.colSet("opt_feedback_date", "Y");

    if (!wp.colStr("feedback_key_sel").equals(wp.colStr("bef_feedback_key_sel")))
      wp.colSet("opt_feedback_key_sel", "Y");
    commFeedbackKeySel("comm_feedback_key_sel");
    commFeedbackKeySel("comm_bef_feedback_key_sel");

    if (!wp.colStr("issue_date_cond").equals(wp.colStr("bef_issue_date_cond")))
      wp.colSet("opt_issue_date_cond", "Y");

    if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
      wp.colSet("opt_issue_date_s", "Y");

    if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
      wp.colSet("opt_issue_date_e", "Y");

    if (!wp.colStr("new_hldr_sel").equals(wp.colStr("bef_new_hldr_sel")))
      wp.colSet("opt_new_hldr_sel", "Y");
    commNewHldrSel("comm_new_hldr_sel");
    commNewHldrSel("comm_bef_new_hldr_sel");

    if (!wp.colStr("new_hldr_days").equals(wp.colStr("bef_new_hldr_days")))
      wp.colSet("opt_new_hldr_days", "Y");

    if (!wp.colStr("new_group_cond").equals(wp.colStr("bef_new_group_cond")))
      wp.colSet("opt_new_group_cond", "Y");

    wp.colSet("bef_new_group_cond_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "F"));
    if (!wp.colStr("new_group_cond_cnt").equals(wp.colStr("bef_new_group_cond_cnt")))
      wp.colSet("opt_new_group_cond_cnt", "Y");

    if (!wp.colStr("new_hldr_card").equals(wp.colStr("bef_new_hldr_card")))
      wp.colSet("opt_new_hldr_card", "Y");

    if (!wp.colStr("new_hldr_sup").equals(wp.colStr("bef_new_hldr_sup")))
      wp.colSet("opt_new_hldr_sup", "Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctTypeSel("comm_acct_type_sel");
    commAcctTypeSel("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "1"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCodeSel("comm_group_code_sel");
    commGroupCodeSel("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("record_cond").equals(wp.colStr("bef_record_cond")))
      wp.colSet("opt_record_cond", "Y");

    if (!wp.colStr("record_group_no").equals(wp.colStr("bef_record_group_no")))
      wp.colSet("opt_record_group_no", "Y");

    wp.colSet("bef_record_cond_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "H"));
    if (!wp.colStr("record_cond_cnt").equals(wp.colStr("bef_record_cond_cnt")))
      wp.colSet("opt_record_cond_cnt", "Y");

    if (!wp.colStr("record_purc_flag").equals(wp.colStr("bef_record_purc_flag")))
      wp.colSet("opt_record_purc_flag", "Y");
    commRecordPurcFlag("comm_record_purc_flag");
    commRecordPurcFlag("comm_bef_record_purc_flag");

    if (!wp.colStr("record_n1_days").equals(wp.colStr("bef_record_n1_days")))
      wp.colSet("opt_record_n1_days", "Y");

    if (!wp.colStr("record_n2_days").equals(wp.colStr("bef_record_n2_days")))
      wp.colSet("opt_record_n2_days", "Y");

    if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
      wp.colSet("opt_bl_cond", "Y");

    if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
      wp.colSet("opt_it_cond", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchantSel("comm_merchant_sel");
    commMerchantSel("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "7"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    commMchtGroupSel("comm_mcht_group_sel");
    commMchtGroupSel("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "8"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("in_merchant_sel").equals(wp.colStr("bef_in_merchant_sel")))
      wp.colSet("opt_in_merchant_sel", "Y");
    commInMerchantSel("comm_in_merchant_sel");
    commInMerchantSel("comm_bef_in_merchant_sel");

    wp.colSet("bef_in_merchant_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "9"));
    if (!wp.colStr("in_merchant_sel_cnt").equals(wp.colStr("bef_in_merchant_sel_cnt")))
      wp.colSet("opt_in_merchant_sel_cnt", "Y");

    if (!wp.colStr("in_mcht_group_sel").equals(wp.colStr("bef_in_mcht_group_sel")))
      wp.colSet("opt_in_mcht_group_sel", "Y");
    commInMchtGroupSel("comm_in_mcht_group_sel");
    commInMchtGroupSel("comm_bef_in_mcht_group_sel");

    wp.colSet("bef_in_mcht_group_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "10"));
    if (!wp.colStr("in_mcht_group_sel_cnt").equals(wp.colStr("bef_in_mcht_group_sel_cnt")))
      wp.colSet("opt_in_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("mcht_in_cond").equals(wp.colStr("bef_mcht_in_cond")))
      wp.colSet("opt_mcht_in_cond", "Y");
    commInCond("comm_mcht_in_cond");
    commInCond("comm_bef_mcht_in_cond");

    if (!wp.colStr("mcht_in_cnt").equals(wp.colStr("bef_mcht_in_cnt")))
      wp.colSet("opt_mcht_in_cnt", "Y");

    if (!wp.colStr("mcht_in_per_amt").equals(wp.colStr("bef_mcht_in_per_amt")))
      wp.colSet("opt_mcht_in_per_amt", "Y");

    if (!wp.colStr("mcht_in_amt").equals(wp.colStr("bef_mcht_in_amt")))
      wp.colSet("opt_mcht_in_amt", "Y");

    if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
      wp.colSet("opt_mcc_code_sel", "Y");
    commMccCodeSel("comm_mcc_code_sel");
    commMccCodeSel("comm_bef_mcc_code_sel");

    wp.colSet("bef_mcc_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "6"));
    if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
      wp.colSet("opt_mcc_code_sel_cnt", "Y");

    if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
      wp.colSet("opt_pos_entry_sel", "Y");
    commPosEntrySel("comm_pos_entry_sel");
    commPosEntrySel("comm_bef_pos_entry_sel");

    wp.colSet("bef_pos_entry_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_MCHT_PARM", wp.colStr("active_code"), "11"));
    if (!wp.colStr("pos_entry_sel_cnt").equals(wp.colStr("bef_pos_entry_sel_cnt")))
      wp.colSet("opt_pos_entry_sel_cnt", "Y");

    if (!wp.colStr("per_amt_cond").equals(wp.colStr("bef_per_amt_cond")))
      wp.colSet("opt_per_amt_cond", "Y");

    if (!wp.colStr("per_amt").equals(wp.colStr("bef_per_amt")))
      wp.colSet("opt_per_amt", "Y");

    if (!wp.colStr("sum_cnt_cond").equals(wp.colStr("bef_sum_cnt_cond")))
      wp.colSet("opt_sum_cnt_cond", "Y");

    if (!wp.colStr("sum_cnt").equals(wp.colStr("bef_sum_cnt")))
      wp.colSet("opt_sum_cnt", "Y");

    if (!wp.colStr("sum_amt_cond").equals(wp.colStr("bef_sum_amt_cond")))
      wp.colSet("opt_sum_amt_cond", "Y");

    if (!wp.colStr("sum_amt").equals(wp.colStr("bef_sum_amt")))
      wp.colSet("opt_sum_amt", "Y");

    if (!wp.colStr("feedback_rate").equals(wp.colStr("bef_feedback_rate")))
      wp.colSet("opt_feedback_rate", "Y");

    if (!wp.colStr("feedback_add_amt").equals(wp.colStr("bef_feedback_add_amt")))
      wp.colSet("opt_feedback_add_amt", "Y");

    if (!wp.colStr("exchange_amt").equals(wp.colStr("bef_exchange_amt")))
      wp.colSet("opt_exchange_amt", "Y");

    if (!wp.colStr("feedback_lmtamt_cond").equals(wp.colStr("bef_feedback_lmtamt_cond")))
      wp.colSet("opt_feedback_lmtamt_cond", "Y");

    if (!wp.colStr("feedback_lmt_amt").equals(wp.colStr("bef_feedback_lmt_amt")))
      wp.colSet("opt_feedback_lmt_amt", "Y");

    if (!wp.colStr("feedback_lmtcnt_cond").equals(wp.colStr("bef_feedback_lmtcnt_cond")))
      wp.colSet("opt_feedback_lmtcnt_cond", "Y");

    if (!wp.colStr("feedback_lmt_cnt").equals(wp.colStr("bef_feedback_lmt_cnt")))
      wp.colSet("opt_feedback_lmt_cnt", "Y");

    if (!wp.colStr("day_lmtamt_cond").equals(wp.colStr("bef_day_lmtamt_cond")))
      wp.colSet("opt_day_lmtamt_cond", "Y");

    if (!wp.colStr("day_lmt_amt").equals(wp.colStr("bef_day_lmt_amt")))
      wp.colSet("opt_day_lmt_amt", "Y");

    if (!wp.colStr("day_lmtcnt_cond").equals(wp.colStr("bef_day_lmtcnt_cond")))
      wp.colSet("opt_day_lmtcnt_cond", "Y");

    if (!wp.colStr("day_lmt_cnt").equals(wp.colStr("bef_day_lmt_cnt")))
      wp.colSet("opt_day_lmt_cnt", "Y");

    if (!wp.colStr("times_lmtamt_cond").equals(wp.colStr("bef_times_lmtamt_cond")))
      wp.colSet("opt_times_lmtamt_cond", "Y");

    if (!wp.colStr("times_lmt_amt").equals(wp.colStr("bef_times_lmt_amt")))
      wp.colSet("opt_times_lmt_amt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("active_name", "");
      wp.colSet("stop_flag", "");
      wp.colSet("stop_date", "");
      wp.colSet("stop_desc", "");
      wp.colSet("active_type", "");
      wp.colSet("bonus_type", "");
      wp.colSet("tax_flag", "");
      wp.colSet("fund_code", "");
      wp.colSet("effect_months", "");
      wp.colSet("purchase_date_s", "");
      wp.colSet("purchase_date_e", "");
      wp.colSet("feedback_date", "");
      wp.colSet("feedback_key_sel", "");
      wp.colSet("issue_date_cond", "");
      wp.colSet("issue_date_s", "");
      wp.colSet("issue_date_e", "");
      wp.colSet("new_hldr_sel", "");
      wp.colSet("new_hldr_days", "");
      wp.colSet("new_group_cond", "");
      wp.colSet("new_group_cond_cnt", "");
      wp.colSet("new_hldr_card", "");
      wp.colSet("new_hldr_sup", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("record_cond", "");
      wp.colSet("record_group_no", "");
      wp.colSet("record_cond_cnt", "");
      wp.colSet("record_purc_flag", "");
      wp.colSet("record_n1_days", "");
      wp.colSet("record_n2_days", "");
      wp.colSet("bl_cond", "");
      wp.colSet("it_cond", "");
      wp.colSet("merchant_sel", "");
      wp.colSet("merchant_sel_cnt", "");
      wp.colSet("mcht_group_sel", "");
      wp.colSet("mcht_group_sel_cnt", "");
      wp.colSet("in_merchant_sel", "");
      wp.colSet("in_merchant_sel_cnt", "");
      wp.colSet("in_mcht_group_sel", "");
      wp.colSet("in_mcht_group_sel_cnt", "");
      wp.colSet("mcht_in_cond", "");
      wp.colSet("mcht_in_cnt", "");
      wp.colSet("mcht_in_per_amt", "");
      wp.colSet("mcht_in_amt", "");
      wp.colSet("mcc_code_sel", "");
      wp.colSet("mcc_code_sel_cnt", "");
      wp.colSet("pos_entry_sel", "");
      wp.colSet("pos_entry_sel_cnt", "");
      wp.colSet("per_amt_cond", "");
      wp.colSet("per_amt", "");
      wp.colSet("sum_cnt_cond", "");
      wp.colSet("sum_cnt", "");
      wp.colSet("sum_amt_cond", "");
      wp.colSet("sum_amt", "");
      wp.colSet("feedback_rate", "");
      wp.colSet("feedback_add_amt", "");
      wp.colSet("exchange_amt", "");
      wp.colSet("feedback_lmtamt_cond", "");
      wp.colSet("feedback_lmt_amt", "");
      wp.colSet("feedback_lmtcnt_cond", "");
      wp.colSet("feedback_lmt_cnt", "");
      wp.colSet("day_lmtamt_cond", "");
      wp.colSet("day_lmt_amt", "");
      wp.colSet("day_lmtcnt_cond", "");
      wp.colSet("day_lmt_cnt", "");
      wp.colSet("times_lmtamt_cond", "");
      wp.colSet("times_lmt_amt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("active_name").length() == 0)
      wp.colSet("opt_active_name", "Y");

    if (wp.colStr("stop_flag").length() == 0)
      wp.colSet("opt_stop_flag", "Y");

    if (wp.colStr("stop_date").length() == 0)
      wp.colSet("opt_stop_date", "Y");

    if (wp.colStr("stop_desc").length() == 0)
      wp.colSet("opt_stop_desc", "Y");

    if (wp.colStr("active_type").length() == 0)
      wp.colSet("opt_active_type", "Y");

    if (wp.colStr("bonus_type").length() == 0)
      wp.colSet("opt_bonus_type", "Y");

    if (wp.colStr("tax_flag").length() == 0)
      wp.colSet("opt_tax_flag", "Y");

    if (wp.colStr("fund_code").length() == 0)
      wp.colSet("opt_fund_code", "Y");

    if (wp.colStr("effect_months").length() == 0)
      wp.colSet("opt_effect_months", "Y");

    if (wp.colStr("purchase_date_s").length() == 0)
      wp.colSet("opt_purchase_date_s", "Y");

    if (wp.colStr("purchase_date_e").length() == 0)
      wp.colSet("opt_purchase_date_e", "Y");

    if (wp.colStr("feedback_date").length() == 0)
      wp.colSet("opt_feedback_date", "Y");

    if (wp.colStr("feedback_key_sel").length() == 0)
      wp.colSet("opt_feedback_key_sel", "Y");

    if (wp.colStr("issue_date_cond").length() == 0)
      wp.colSet("opt_issue_date_cond", "Y");

    if (wp.colStr("issue_date_s").length() == 0)
      wp.colSet("opt_issue_date_s", "Y");

    if (wp.colStr("issue_date_e").length() == 0)
      wp.colSet("opt_issue_date_e", "Y");

    if (wp.colStr("new_hldr_sel").length() == 0)
      wp.colSet("opt_new_hldr_sel", "Y");

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


    if (wp.colStr("record_cond").length() == 0)
      wp.colSet("opt_record_cond", "Y");

    if (wp.colStr("record_group_no").length() == 0)
      wp.colSet("opt_record_group_no", "Y");


    if (wp.colStr("record_purc_flag").length() == 0)
      wp.colSet("opt_record_purc_flag", "Y");

    if (wp.colStr("record_n1_days").length() == 0)
      wp.colSet("opt_record_n1_days", "Y");

    if (wp.colStr("record_n2_days").length() == 0)
      wp.colSet("opt_record_n2_days", "Y");

    if (wp.colStr("bl_cond").length() == 0)
      wp.colSet("opt_bl_cond", "Y");

    if (wp.colStr("it_cond").length() == 0)
      wp.colSet("opt_it_cond", "Y");

    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("opt_merchant_sel", "Y");


    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("opt_mcht_group_sel", "Y");


    if (wp.colStr("in_merchant_sel").length() == 0)
      wp.colSet("opt_in_merchant_sel", "Y");


    if (wp.colStr("in_mcht_group_sel").length() == 0)
      wp.colSet("opt_in_mcht_group_sel", "Y");


    if (wp.colStr("mcht_in_cond").length() == 0)
      wp.colSet("opt_mcht_in_cond", "Y");

    if (wp.colStr("mcht_in_cnt").length() == 0)
      wp.colSet("opt_mcht_in_cnt", "Y");

    if (wp.colStr("mcht_in_per_amt").length() == 0)
      wp.colSet("opt_mcht_in_per_amt", "Y");

    if (wp.colStr("mcht_in_amt").length() == 0)
      wp.colSet("opt_mcht_in_amt", "Y");

    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("opt_mcc_code_sel", "Y");


    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("opt_pos_entry_sel", "Y");


    if (wp.colStr("per_amt_cond").length() == 0)
      wp.colSet("opt_per_amt_cond", "Y");

    if (wp.colStr("per_amt").length() == 0)
      wp.colSet("opt_per_amt", "Y");

    if (wp.colStr("sum_cnt_cond").length() == 0)
      wp.colSet("opt_sum_cnt_cond", "Y");

    if (wp.colStr("sum_cnt").length() == 0)
      wp.colSet("opt_sum_cnt", "Y");

    if (wp.colStr("sum_amt_cond").length() == 0)
      wp.colSet("opt_sum_amt_cond", "Y");

    if (wp.colStr("sum_amt").length() == 0)
      wp.colSet("opt_sum_amt", "Y");

    if (wp.colStr("feedback_rate").length() == 0)
      wp.colSet("opt_feedback_rate", "Y");

    if (wp.colStr("feedback_add_amt").length() == 0)
      wp.colSet("opt_feedback_add_amt", "Y");

    if (wp.colStr("exchange_amt").length() == 0)
      wp.colSet("opt_exchange_amt", "Y");

    if (wp.colStr("feedback_lmtamt_cond").length() == 0)
      wp.colSet("opt_feedback_lmtamt_cond", "Y");

    if (wp.colStr("feedback_lmt_amt").length() == 0)
      wp.colSet("opt_feedback_lmt_amt", "Y");

    if (wp.colStr("feedback_lmtcnt_cond").length() == 0)
      wp.colSet("opt_feedback_lmtcnt_cond", "Y");

    if (wp.colStr("feedback_lmt_cnt").length() == 0)
      wp.colSet("opt_feedback_lmt_cnt", "Y");

    if (wp.colStr("day_lmtamt_cond").length() == 0)
      wp.colSet("opt_day_lmtamt_cond", "Y");

    if (wp.colStr("day_lmt_amt").length() == 0)
      wp.colSet("opt_day_lmt_amt", "Y");

    if (wp.colStr("day_lmtcnt_cond").length() == 0)
      wp.colSet("opt_day_lmtcnt_cond", "Y");

    if (wp.colStr("day_lmt_cnt").length() == 0)
      wp.colSet("opt_day_lmt_cnt", "Y");

    if (wp.colStr("times_lmtamt_cond").length() == 0)
      wp.colSet("opt_times_lmtamt_cond", "Y");

    if (wp.colStr("times_lmt_amt").length() == 0)
      wp.colSet("opt_times_lmt_amt", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp6260Func func = new mktp02.Mktp6260Func(wp);

    String[] lsActiveCode = wp.itemBuff("active_code");
    String[] lsAudType = wp.itemBuff("aud_type");
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
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
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

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
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
  public void dddwSelect() {}

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
  public void commFuncCode(String code) throws Exception {
    commFuncCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commFuncCode(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " fund_name as column_fund_name " + " from mkt_loan_parm "
          + " where 1 = 1 " 
//    	  + " and   fund_code = '" + wp.colStr(ii, befStr + "fund_code") + "'";
      	  + " and fund_code = :fund_code ";
      	  setString("fund_code",wp.colStr(ii, befStr + "fund_code"));
      if (wp.colStr(ii, befStr + "fund_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_fund_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"1.紅利點數", "2.基金"};
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
  public void commFeedbackKeySel(String cde1) throws Exception {
    String[] cde = {"2", "3", "4"};
    String[] txt = {"2:帳戶", "3:正卡", "4:卡片"};
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
  public void commNewHldrSel(String cde1) throws Exception {
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
  public void commRecordPurcFlag(String cde1) throws Exception {
    String[] cde = {"0", "1", "2", "3"};
    String[] txt = {"0.有消費及登錄", "1.消費日=登錄>日", "2.消費日>=登錄日", "3.消費日<=登錄日"};
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
    String[] txt = {"全部", ">指定"};
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
    String[] txt = {"全部", ">指定"};
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
  public void commInCond(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"前期", "本月"};
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
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }

  // ************************************************************************
  String listMktBnData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = '" + tableName + "'" + " and   data_key   = '" + dataKey + "'"
        + " and   data_type  = '" + dataType + "'";
    sqlSelect(sql1);

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }
  // ************************************************************************

} // End of class
