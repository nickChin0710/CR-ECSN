/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/20  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名  
* 110-11-08  V1.00.03  machao     SQL Injection                                                                                   *   
***************************************************************************/
package mktp02;

import mktp02.Mktp6020Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6020 extends BaseProc {
  private String PROGNAME = "高階卡友參數維護處理程式108/08/20 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6020Func func = null;
  String rowid;// kk2;
  String groupCode, cardType;
  String fstAprFlag = "";
  String orgTabName = "cyc_anul_gp_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%")
        + sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%")
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
        + "a.aud_type," + "a.group_code," + "a.card_type," + "a.card_fee," + "a.sup_card_fee,"
        + "a.cnt_select," + "a.accumlate_amt," + "a.email_nopaper_flag," + "a.mcode,"
        + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by group_code,card_type";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGroupCode("comm_group_code");
    commCardType("comm_card_type");

    commCntSelect("comm_cnt_select");
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
      if (wp.itemStr("kk_group_code").length() == 0) {
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
        + "a.group_code as group_code," + "a.card_type as card_type," + "a.crt_user,"
        + "a.card_fee," + "a.sup_card_fee," + "a.mer_cond," + "a.mer_bl_flag," + "a.mer_ca_flag,"
        + "a.mer_it_flag," + "a.mer_ao_flag," + "a.mer_id_flag," + "a.mer_ot_flag,"
        + "a.major_flag," + "a.sub_flag," + "a.major_sub," + "a.a_merchant_sel,"
        + "a.a_mcht_group_sel," + "a.cnt_cond," + "a.cnt_select," + "a.month_cnt,"
        + "a.accumlate_cnt," + "a.cnt_bl_flag," + "a.cnt_ca_flag," + "a.cnt_it_flag,"
        + "a.cnt_ao_flag," + "a.cnt_id_flag," + "a.cnt_ot_flag," + "a.cnt_major_flag,"
        + "a.cnt_sub_flag," + "a.cnt_major_sub," + "a.b_mcc_code_sel," + "a.b_merchant_sel,"
        + "a.b_mcht_group_sel," + "a.amt_cond," + "a.accumlate_amt," + "a.amt_bl_flag,"
        + "a.amt_ca_flag," + "a.amt_it_flag," + "a.amt_ao_flag," + "a.amt_id_flag,"
        + "a.amt_ot_flag," + "a.amt_major_flag," + "a.amt_sub_flag," + "a.amt_major_sub,"
        + "a.c_mcc_code_sel," + "a.c_merchant_sel," + "a.c_mcht_group_sel," + "a.mcode,"
        + "a.email_nopaper_flag,"
        + "a.miner_half_flag,a.g_cond_flag,a.g_accumlate_amt,a.h_cond_flag,a.h_accumlate_amt" ;

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(groupCode, "a.group_code") + sqlCol(cardType, "a.card_type");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commMerchamt("comm_a_merchant_sel");
    commMchtGroup("comm_a_mcht_group_sel");
    commCntSelect("comm_cnt_select");
    commMccCodeb("comm_b_mcc_code_sel");
    commMerchantb("comm_b_merchant_sel");
    commMchtGroupb("comm_b_mcht_group_sel");
    commMccCodec("comm_c_mcc_code_sel");
    commMerchantc("comm_c_merchant_sel");
    commMchtGroupc("comm_c_mcht_group_sel");
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    checkButtonOff();
    groupCode = wp.colStr("group_code");
    cardType = wp.colStr("card_type");
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
    controlTabName = "cyc_anul_gp";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.group_code as group_code," + "a.card_type as card_type,"
        + "a.crt_user as bef_crt_user," + "a.card_fee as bef_card_fee,"
        + "a.sup_card_fee as bef_sup_card_fee," + "a.mer_cond as bef_mer_cond,"
        + "a.mer_bl_flag as bef_mer_bl_flag," + "a.mer_ca_flag as bef_mer_ca_flag,"
        + "a.mer_it_flag as bef_mer_it_flag," + "a.mer_ao_flag as bef_mer_ao_flag,"
        + "a.mer_id_flag as bef_mer_id_flag," + "a.mer_ot_flag as bef_mer_ot_flag,"
        + "a.major_flag as bef_major_flag," + "a.sub_flag as bef_sub_flag,"
        + "a.major_sub as bef_major_sub," + "a.a_merchant_sel as bef_a_merchant_sel,"
        + "a.a_mcht_group_sel as bef_a_mcht_group_sel," + "a.cnt_cond as bef_cnt_cond,"
        + "a.cnt_select as bef_cnt_select," + "a.month_cnt as bef_month_cnt,"
        + "a.accumlate_cnt as bef_accumlate_cnt," + "a.cnt_bl_flag as bef_cnt_bl_flag,"
        + "a.cnt_ca_flag as bef_cnt_ca_flag," + "a.cnt_it_flag as bef_cnt_it_flag,"
        + "a.cnt_ao_flag as bef_cnt_ao_flag," + "a.cnt_id_flag as bef_cnt_id_flag,"
        + "a.cnt_ot_flag as bef_cnt_ot_flag," + "a.cnt_major_flag as bef_cnt_major_flag,"
        + "a.cnt_sub_flag as bef_cnt_sub_flag," + "a.cnt_major_sub as bef_cnt_major_sub,"
        + "a.b_mcc_code_sel as bef_b_mcc_code_sel," + "a.b_merchant_sel as bef_b_merchant_sel,"
        + "a.b_mcht_group_sel as bef_b_mcht_group_sel," + "a.amt_cond as bef_amt_cond,"
        + "a.accumlate_amt as bef_accumlate_amt," + "a.amt_bl_flag as bef_amt_bl_flag,"
        + "a.amt_ca_flag as bef_amt_ca_flag," + "a.amt_it_flag as bef_amt_it_flag,"
        + "a.amt_ao_flag as bef_amt_ao_flag," + "a.amt_id_flag as bef_amt_id_flag,"
        + "a.amt_ot_flag as bef_amt_ot_flag," + "a.amt_major_flag as bef_amt_major_flag,"
        + "a.amt_sub_flag as bef_amt_sub_flag," + "a.amt_major_sub as bef_amt_major_sub,"
        + "a.c_mcc_code_sel as bef_c_mcc_code_sel," + "a.c_merchant_sel as bef_c_merchant_sel,"
        + "a.c_mcht_group_sel as bef_c_mcht_group_sel," + "a.mcode as bef_mcode,"
        + "a.email_nopaper_flag as bef_email_nopaper_flag,"
        + "a.miner_half_flag as bef_miner_half_flag,"
        + "a.g_cond_flag as bef_g_cond_flag,"
        + "a.g_accumlate_amt as bef_g_accumlate_amt,"
        + "a.h_cond_flag as bef_h_cond_flag,"
        + "a.h_accumlate_amt as bef_h_accumlate_amt " ;

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(groupCode, "a.group_code") + sqlCol(cardType, "a.card_type");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    commMerchamt("comm_a_merchant_sel");
    commMchtGroup("comm_a_mcht_group_sel");
    commCntSelect("comm_cnt_select");
    commMccCodeb("comm_b_mcc_code_sel");
    commMerchantb("comm_b_merchant_sel");
    commMchtGroupb("comm_b_mcht_group_sel");
    commMccCodec("comm_c_mcc_code_sel");
    commMerchantc("comm_c_merchant_sel");
    commMchtGroupc("comm_c_mcht_group_sel");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("a_merchant_sell_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "1"));
    wp.colSet("a_mcht_group_sel_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "2"));
    wp.colSet("b_mcc_sel_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "3"));
    wp.colSet("b_merchant_sell_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "4"));
    wp.colSet("b_mcht_group_sel_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "5"));
    wp.colSet("c_mcc_sel_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "6"));
    wp.colSet("c_merchant_sel_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "7"));
    wp.colSet("c_mcht_group_sel_cnt", listMktBnData("mkt_bn_data_t", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_typee"), "8"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("card_fee").equals(wp.colStr("bef_card_fee")))
      wp.colSet("opt_card_fee", "Y");

    if (!wp.colStr("sup_card_fee").equals(wp.colStr("bef_sup_card_fee")))
      wp.colSet("opt_sup_card_fee", "Y");

    if (!wp.colStr("mer_cond").equals(wp.colStr("bef_mer_cond")))
      wp.colSet("opt_mer_cond", "Y");

    if (!wp.colStr("mer_bl_flag").equals(wp.colStr("bef_mer_bl_flag")))
      wp.colSet("opt_mer_bl_flag", "Y");

    if (!wp.colStr("mer_ca_flag").equals(wp.colStr("bef_mer_ca_flag")))
      wp.colSet("opt_mer_ca_flag", "Y");

    if (!wp.colStr("mer_it_flag").equals(wp.colStr("bef_mer_it_flag")))
      wp.colSet("opt_mer_it_flag", "Y");

    if (!wp.colStr("mer_ao_flag").equals(wp.colStr("bef_mer_ao_flag")))
      wp.colSet("opt_mer_ao_flag", "Y");

    if (!wp.colStr("mer_id_flag").equals(wp.colStr("bef_mer_id_flag")))
      wp.colSet("opt_mer_id_flag", "Y");

    if (!wp.colStr("mer_ot_flag").equals(wp.colStr("bef_mer_ot_flag")))
      wp.colSet("opt_mer_ot_flag", "Y");

    if (!wp.colStr("major_flag").equals(wp.colStr("bef_major_flag")))
      wp.colSet("opt_major_flag", "Y");

    if (!wp.colStr("sub_flag").equals(wp.colStr("bef_sub_flag")))
      wp.colSet("opt_sub_flag", "Y");

    if (!wp.colStr("major_sub").equals(wp.colStr("bef_major_sub")))
      wp.colSet("opt_major_sub", "Y");

    if (!wp.colStr("a_merchant_sel").equals(wp.colStr("bef_a_merchant_sel")))
      wp.colSet("opt_a_merchant_sel", "Y");
    commMerchamt("comm_a_merchant_sel");
    commMerchamt("comm_bef_a_merchant_sel");

    wp.colSet("bef_a_merchant_sell_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "1"));
    if (!wp.colStr("a_merchant_sell_cnt").equals(wp.colStr("bef_a_merchant_sell_cnt")))
      wp.colSet("opt_a_merchant_sell_cnt", "Y");

    if (!wp.colStr("a_mcht_group_sel").equals(wp.colStr("bef_a_mcht_group_sel")))
      wp.colSet("opt_a_mcht_group_sel", "Y");
    commMchtGroup("comm_a_mcht_group_sel");
    commMchtGroup("comm_bef_a_mcht_group_sel");

    wp.colSet("bef_a_mcht_group_sel_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "2"));
    if (!wp.colStr("a_mcht_group_sel_cnt").equals(wp.colStr("bef_a_mcht_group_sel_cnt")))
      wp.colSet("opt_a_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("cnt_cond").equals(wp.colStr("bef_cnt_cond")))
      wp.colSet("opt_cnt_cond", "Y");

    if (!wp.colStr("cnt_select").equals(wp.colStr("bef_cnt_select")))
      wp.colSet("opt_cnt_select", "Y");
    commCntSelect("comm_cnt_select");
    commCntSelect("comm_bef_cnt_select");

    if (!wp.colStr("month_cnt").equals(wp.colStr("bef_month_cnt")))
      wp.colSet("opt_month_cnt", "Y");

    if (!wp.colStr("accumlate_cnt").equals(wp.colStr("bef_accumlate_cnt")))
      wp.colSet("opt_accumlate_cnt", "Y");

    if (!wp.colStr("cnt_bl_flag").equals(wp.colStr("bef_cnt_bl_flag")))
      wp.colSet("opt_cnt_bl_flag", "Y");

    if (!wp.colStr("cnt_ca_flag").equals(wp.colStr("bef_cnt_ca_flag")))
      wp.colSet("opt_cnt_ca_flag", "Y");

    if (!wp.colStr("cnt_it_flag").equals(wp.colStr("bef_cnt_it_flag")))
      wp.colSet("opt_cnt_it_flag", "Y");

    if (!wp.colStr("cnt_ao_flag").equals(wp.colStr("bef_cnt_ao_flag")))
      wp.colSet("opt_cnt_ao_flag", "Y");

    if (!wp.colStr("cnt_id_flag").equals(wp.colStr("bef_cnt_id_flag")))
      wp.colSet("opt_cnt_id_flag", "Y");

    if (!wp.colStr("cnt_ot_flag").equals(wp.colStr("bef_cnt_ot_flag")))
      wp.colSet("opt_cnt_ot_flag", "Y");

    if (!wp.colStr("cnt_major_flag").equals(wp.colStr("bef_cnt_major_flag")))
      wp.colSet("opt_cnt_major_flag", "Y");

    if (!wp.colStr("cnt_sub_flag").equals(wp.colStr("bef_cnt_sub_flag")))
      wp.colSet("opt_cnt_sub_flag", "Y");

    if (!wp.colStr("cnt_major_sub").equals(wp.colStr("bef_cnt_major_sub")))
      wp.colSet("opt_cnt_major_sub", "Y");

    if (!wp.colStr("b_mcc_code_sel").equals(wp.colStr("bef_b_mcc_code_sel")))
      wp.colSet("opt_b_mcc_code_sel", "Y");
    commMccCodeb("comm_b_mcc_code_sel");
    commMccCodeb("comm_bef_b_mcc_code_sel");

    wp.colSet("bef_b_mcc_sel_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "3"));
    if (!wp.colStr("b_mcc_sel_cnt").equals(wp.colStr("bef_b_mcc_sel_cnt")))
      wp.colSet("opt_b_mcc_sel_cnt", "Y");

    if (!wp.colStr("b_merchant_sel").equals(wp.colStr("bef_b_merchant_sel")))
      wp.colSet("opt_b_merchant_sel", "Y");
    commMerchantb("comm_b_merchant_sel");
    commMerchantb("comm_bef_b_merchant_sel");

    wp.colSet("bef_b_merchant_sell_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "4"));
    if (!wp.colStr("b_merchant_sell_cnt").equals(wp.colStr("bef_b_merchant_sell_cnt")))
      wp.colSet("opt_b_merchant_sell_cnt", "Y");

    if (!wp.colStr("b_mcht_group_sel").equals(wp.colStr("bef_b_mcht_group_sel")))
      wp.colSet("opt_b_mcht_group_sel", "Y");
    commMchtGroupb("comm_b_mcht_group_sel");
    commMchtGroupb("comm_bef_b_mcht_group_sel");

    wp.colSet("bef_b_mcht_group_sel_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "5"));
    if (!wp.colStr("b_mcht_group_sel_cnt").equals(wp.colStr("bef_b_mcht_group_sel_cnt")))
      wp.colSet("opt_b_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("amt_cond").equals(wp.colStr("bef_amt_cond")))
      wp.colSet("opt_amt_cond", "Y");

    if (!wp.colStr("accumlate_amt").equals(wp.colStr("bef_accumlate_amt")))
      wp.colSet("opt_accumlate_amt", "Y");

    if (!wp.colStr("amt_bl_flag").equals(wp.colStr("bef_amt_bl_flag")))
      wp.colSet("opt_amt_bl_flag", "Y");

    if (!wp.colStr("amt_ca_flag").equals(wp.colStr("bef_amt_ca_flag")))
      wp.colSet("opt_amt_ca_flag", "Y");

    if (!wp.colStr("amt_it_flag").equals(wp.colStr("bef_amt_it_flag")))
      wp.colSet("opt_amt_it_flag", "Y");

    if (!wp.colStr("amt_ao_flag").equals(wp.colStr("bef_amt_ao_flag")))
      wp.colSet("opt_amt_ao_flag", "Y");

    if (!wp.colStr("amt_id_flag").equals(wp.colStr("bef_amt_id_flag")))
      wp.colSet("opt_amt_id_flag", "Y");

    if (!wp.colStr("amt_ot_flag").equals(wp.colStr("bef_amt_ot_flag")))
      wp.colSet("opt_amt_ot_flag", "Y");

    if (!wp.colStr("amt_major_flag").equals(wp.colStr("bef_amt_major_flag")))
      wp.colSet("opt_amt_major_flag", "Y");

    if (!wp.colStr("amt_sub_flag").equals(wp.colStr("bef_amt_sub_flag")))
      wp.colSet("opt_amt_sub_flag", "Y");

    if (!wp.colStr("amt_major_sub").equals(wp.colStr("bef_amt_major_sub")))
      wp.colSet("opt_amt_major_sub", "Y");

    if (!wp.colStr("c_mcc_code_sel").equals(wp.colStr("bef_c_mcc_code_sel")))
      wp.colSet("opt_c_mcc_code_sel", "Y");
    commMccCodec("comm_c_mcc_code_sel");
    commMccCodec("comm_bef_c_mcc_code_sel");

    wp.colSet("bef_c_mcc_sel_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "6"));
    if (!wp.colStr("c_mcc_sel_cnt").equals(wp.colStr("bef_c_mcc_sel_cnt")))
      wp.colSet("opt_c_mcc_sel_cnt", "Y");

    if (!wp.colStr("c_merchant_sel").equals(wp.colStr("bef_c_merchant_sel")))
      wp.colSet("opt_c_merchant_sel", "Y");
    commMerchantc("comm_c_merchant_sel");
    commMerchantc("comm_bef_c_merchant_sel");

    wp.colSet("bef_c_merchant_sel_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_type"), "7"));
    if (!wp.colStr("c_merchant_sel_cnt").equals(wp.colStr("bef_c_merchant_sel_cnt")))
      wp.colSet("opt_c_merchant_sel_cnt", "Y");

    if (!wp.colStr("c_mcht_group_sel").equals(wp.colStr("bef_c_mcht_group_sel")))
      wp.colSet("opt_c_mcht_group_sel", "Y");
    commMchtGroupc("comm_c_mcht_group_sel");
    commMchtGroupc("comm_bef_c_mcht_group_sel");

    wp.colSet("bef_c_mcht_group_sel_cnt", listMktBnData("mkt_bn_data", "CYC_ANUL_GP",
        wp.colStr("group_code") + wp.colStr("card_typee"), "8"));
    if (!wp.colStr("c_mcht_group_sel_cnt").equals(wp.colStr("bef_c_mcht_group_sel_cnt")))
      wp.colSet("opt_c_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("mcode").equals(wp.colStr("bef_mcode")))
      wp.colSet("opt_mcode", "Y");

    if (!wp.colStr("email_nopaper_flag").equals(wp.colStr("bef_email_nopaper_flag")))
      wp.colSet("opt_email_nopaper_flag", "Y");
    
    if (!wp.colStr("miner_half_flag").equals(wp.colStr("bef_miner_half_flag")))
        wp.colSet("opt_miner_half_flag", "Y");
    
    if (!wp.colStr("g_cond_flag").equals(wp.colStr("bef_g_cond_flag")))
        wp.colSet("opt_g_cond_flag", "Y");
    
    if (!wp.colStr("g_accumlate_amt").equals(wp.colStr("bef_g_accumlate_amt")))
        wp.colSet("opt_g_accumlate_amt", "Y");
    
    if (!wp.colStr("h_cond_flag").equals(wp.colStr("bef_h_cond_flag")))
        wp.colSet("opt_h_cond_flag", "Y");
    
    if (!wp.colStr("h_accumlate_amt").equals(wp.colStr("bef_h_accumlate_amt")))
        wp.colSet("opt_h_accumlate_amt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("card_fee", "");
      wp.colSet("sup_card_fee", "");
      wp.colSet("mer_cond", "");
      wp.colSet("mer_bl_flag", "");
      wp.colSet("mer_ca_flag", "");
      wp.colSet("mer_it_flag", "");
      wp.colSet("mer_ao_flag", "");
      wp.colSet("mer_id_flag", "");
      wp.colSet("mer_ot_flag", "");
      wp.colSet("major_flag", "");
      wp.colSet("sub_flag", "");
      wp.colSet("major_sub", "");
      wp.colSet("a_merchant_sel", "");
      wp.colSet("a_merchant_sell_cnt", "");
      wp.colSet("a_mcht_group_sel", "");
      wp.colSet("a_mcht_group_sel_cnt", "");
      wp.colSet("cnt_cond", "");
      wp.colSet("cnt_select", "");
      wp.colSet("month_cnt", "");
      wp.colSet("accumlate_cnt", "");
      wp.colSet("cnt_bl_flag", "");
      wp.colSet("cnt_ca_flag", "");
      wp.colSet("cnt_it_flag", "");
      wp.colSet("cnt_ao_flag", "");
      wp.colSet("cnt_id_flag", "");
      wp.colSet("cnt_ot_flag", "");
      wp.colSet("cnt_major_flag", "");
      wp.colSet("cnt_sub_flag", "");
      wp.colSet("cnt_major_sub", "");
      wp.colSet("b_mcc_code_sel", "");
      wp.colSet("b_mcc_sel_cnt", "");
      wp.colSet("b_merchant_sel", "");
      wp.colSet("b_merchant_sell_cnt", "");
      wp.colSet("b_mcht_group_sel", "");
      wp.colSet("b_mcht_group_sel_cnt", "");
      wp.colSet("amt_cond", "");
      wp.colSet("accumlate_amt", "");
      wp.colSet("amt_bl_flag", "");
      wp.colSet("amt_ca_flag", "");
      wp.colSet("amt_it_flag", "");
      wp.colSet("amt_ao_flag", "");
      wp.colSet("amt_id_flag", "");
      wp.colSet("amt_ot_flag", "");
      wp.colSet("amt_major_flag", "");
      wp.colSet("amt_sub_flag", "");
      wp.colSet("amt_major_sub", "");
      wp.colSet("c_mcc_code_sel", "");
      wp.colSet("c_mcc_sel_cnt", "");
      wp.colSet("c_merchant_sel", "");
      wp.colSet("c_merchant_sel_cnt", "");
      wp.colSet("c_mcht_group_sel", "");
      wp.colSet("c_mcht_group_sel_cnt", "");
      wp.colSet("mcode", "");
      wp.colSet("email_nopaper_flag", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("card_fee").length() == 0)
      wp.colSet("opt_card_fee", "Y");

    if (wp.colStr("sup_card_fee").length() == 0)
      wp.colSet("opt_sup_card_fee", "Y");

    if (wp.colStr("mer_cond").length() == 0)
      wp.colSet("opt_mer_cond", "Y");

    if (wp.colStr("mer_bl_flag").length() == 0)
      wp.colSet("opt_mer_bl_flag", "Y");

    if (wp.colStr("mer_ca_flag").length() == 0)
      wp.colSet("opt_mer_ca_flag", "Y");

    if (wp.colStr("mer_it_flag").length() == 0)
      wp.colSet("opt_mer_it_flag", "Y");

    if (wp.colStr("mer_ao_flag").length() == 0)
      wp.colSet("opt_mer_ao_flag", "Y");

    if (wp.colStr("mer_id_flag").length() == 0)
      wp.colSet("opt_mer_id_flag", "Y");

    if (wp.colStr("mer_ot_flag").length() == 0)
      wp.colSet("opt_mer_ot_flag", "Y");

    if (wp.colStr("major_flag").length() == 0)
      wp.colSet("opt_major_flag", "Y");

    if (wp.colStr("sub_flag").length() == 0)
      wp.colSet("opt_sub_flag", "Y");

    if (wp.colStr("major_sub").length() == 0)
      wp.colSet("opt_major_sub", "Y");

    if (wp.colStr("a_merchant_sel").length() == 0)
      wp.colSet("opt_a_merchant_sel", "Y");


    if (wp.colStr("a_mcht_group_sel").length() == 0)
      wp.colSet("opt_a_mcht_group_sel", "Y");


    if (wp.colStr("cnt_cond").length() == 0)
      wp.colSet("opt_cnt_cond", "Y");

    if (wp.colStr("cnt_select").length() == 0)
      wp.colSet("opt_cnt_select", "Y");

    if (wp.colStr("month_cnt").length() == 0)
      wp.colSet("opt_month_cnt", "Y");

    if (wp.colStr("accumlate_cnt").length() == 0)
      wp.colSet("opt_accumlate_cnt", "Y");

    if (wp.colStr("cnt_bl_flag").length() == 0)
      wp.colSet("opt_cnt_bl_flag", "Y");

    if (wp.colStr("cnt_ca_flag").length() == 0)
      wp.colSet("opt_cnt_ca_flag", "Y");

    if (wp.colStr("cnt_it_flag").length() == 0)
      wp.colSet("opt_cnt_it_flag", "Y");

    if (wp.colStr("cnt_ao_flag").length() == 0)
      wp.colSet("opt_cnt_ao_flag", "Y");

    if (wp.colStr("cnt_id_flag").length() == 0)
      wp.colSet("opt_cnt_id_flag", "Y");

    if (wp.colStr("cnt_ot_flag").length() == 0)
      wp.colSet("opt_cnt_ot_flag", "Y");

    if (wp.colStr("cnt_major_flag").length() == 0)
      wp.colSet("opt_cnt_major_flag", "Y");

    if (wp.colStr("cnt_sub_flag").length() == 0)
      wp.colSet("opt_cnt_sub_flag", "Y");

    if (wp.colStr("cnt_major_sub").length() == 0)
      wp.colSet("opt_cnt_major_sub", "Y");

    if (wp.colStr("b_mcc_code_sel").length() == 0)
      wp.colSet("opt_b_mcc_code_sel", "Y");


    if (wp.colStr("b_merchant_sel").length() == 0)
      wp.colSet("opt_b_merchant_sel", "Y");


    if (wp.colStr("b_mcht_group_sel").length() == 0)
      wp.colSet("opt_b_mcht_group_sel", "Y");


    if (wp.colStr("amt_cond").length() == 0)
      wp.colSet("opt_amt_cond", "Y");

    if (wp.colStr("accumlate_amt").length() == 0)
      wp.colSet("opt_accumlate_amt", "Y");

    if (wp.colStr("amt_bl_flag").length() == 0)
      wp.colSet("opt_amt_bl_flag", "Y");

    if (wp.colStr("amt_ca_flag").length() == 0)
      wp.colSet("opt_amt_ca_flag", "Y");

    if (wp.colStr("amt_it_flag").length() == 0)
      wp.colSet("opt_amt_it_flag", "Y");

    if (wp.colStr("amt_ao_flag").length() == 0)
      wp.colSet("opt_amt_ao_flag", "Y");

    if (wp.colStr("amt_id_flag").length() == 0)
      wp.colSet("opt_amt_id_flag", "Y");

    if (wp.colStr("amt_ot_flag").length() == 0)
      wp.colSet("opt_amt_ot_flag", "Y");

    if (wp.colStr("amt_major_flag").length() == 0)
      wp.colSet("opt_amt_major_flag", "Y");

    if (wp.colStr("amt_sub_flag").length() == 0)
      wp.colSet("opt_amt_sub_flag", "Y");

    if (wp.colStr("amt_major_sub").length() == 0)
      wp.colSet("opt_amt_major_sub", "Y");

    if (wp.colStr("c_mcc_code_sel").length() == 0)
      wp.colSet("opt_c_mcc_code_sel", "Y");


    if (wp.colStr("c_merchant_sel").length() == 0)
      wp.colSet("opt_c_merchant_sel", "Y");


    if (wp.colStr("c_mcht_group_sel").length() == 0)
      wp.colSet("opt_c_mcht_group_sel", "Y");


    if (wp.colStr("mcode").length() == 0)
      wp.colSet("opt_mcode", "Y");

    if (wp.colStr("email_nopaper_flag").length() == 0)
      wp.colSet("opt_email_nopaper_flag", "Y");

    if (wp.colStr("miner_half_flag").length() == 0)
        wp.colSet("opt_miner_half_flag", "Y");
    
    if (wp.colStr("g_cond_flag").length() == 0)
        wp.colSet("opt_g_cond_flag", "Y");
    
    if (wp.colStr("h_cond_flag").length() == 0)
        wp.colSet("opt_h_cond_flag", "Y");
  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp6020Func func = new mktp02.Mktp6020Func(wp);

    String[] lsGroupCode = wp.itemBuff("group_code");
    String[] lsCardType = wp.itemBuff("card_type");
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

      func.varsSet("group_code", lsGroupCode[rr]);
      func.varsSet("card_type", lsCardType[rr]);
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
        commGroupCode("comm_group_code");
        commCardType("comm_card_type");
        commCntSelect("comm_cnt_select");
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
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktp6020"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
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
  public void commGroupCode(String groupCode) throws Exception {
    commGroupCode(groupCode, 0);
    return;
  }

  // ************************************************************************
  public void commGroupCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
//          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, befStr + "group_code") + "'";
      + " where 1 = 1 " + " and   group_code = :group_code ";
      setString("group_code",wp.colStr(ii, befStr + "group_code"));
      if (wp.colStr(ii, befStr + "group_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_group_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commCardType(String cardType) throws Exception {
    commCardType(cardType, 0);
    return;
  }

  // ************************************************************************
  public void commCardType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from PTR_CARD_TYPE " + " where 1 = 1 "
//          + " and   card_type = '" + wp.colStr(ii, befStr + "card_type") + "'";
			+ " and   card_type = :card_type ";
      		setString("card_type",wp.colStr(ii, befStr + "card_type"));
      if (wp.colStr(ii, befStr + "card_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commMerchamt(String cde1) throws Exception {
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
  public void commCntSelect(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"每月", "累積"};
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
  public void commMccCodeb(String cde1) throws Exception {
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
  public void commMerchantb(String cde1) throws Exception {
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
  public void commMchtGroupb(String cde1) throws Exception {
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
  public void commMccCodec(String cde1) throws Exception {
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
  public void commMerchantc(String cde1) throws Exception {
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
  public void commMchtGroupc(String cde1) throws Exception {
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

}  // End of class
