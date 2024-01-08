/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/15  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *   
***************************************************************************/
package mktp02;

import mktp02.Mktp3850Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp3850 extends BaseProc {
  private String PROGNAME = "指定繳款方式基金參數維護處理程式108/08/15 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp3850Func func = null;
  String rowid;
  String funCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_nfc_parm_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_fund_code"), "a.fund_code", "like%")
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
        + "a.aud_type," + "a.fund_code," + "a.fund_name," + "a.fund_crt_date_s,"
        + "a.fund_crt_date_e," + "a.stop_date," + "a.effect_months," + "a.crt_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by fund_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


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
      if (wp.itemStr("kk_fund_code").length() == 0) {
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "aud_type,"
        + "a.fund_code as fund_code," + "a.crt_user," + "a.fund_name," + "a.fund_crt_date_s,"
        + "a.fund_crt_date_e," + "a.stop_flag," + "a.stop_date," + "a.stop_desc,"
        + "a.effect_months," + "a.group_card_sel," + "a.group_code_sel," + "a.payment_sel,"
        + "a.merchant_sel," + "a.mcht_group_sel," + "a.mcc_code_sel," + "a.bl_cond," + "a.it_cond,"
        + "a.ca_cond," + "a.id_cond," + "a.ao_cond," + "a.ot_cond," + "a.feedback_rate,"
        + "a.feedback_lmt," + "a.cancel_period," + "a.cancel_scope," + "a.cancel_event";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(funCode, "a.fund_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commGroupCrd("comm_group_card_sel");
    commGroupCode("comm_group_code_sel");
    commPaymentSel("comm_payment_sel");
    commMerchantSel("comm_merchant_sel");
    commGroupSel("comm_mcht_group_sel");
    commCodeSel("comm_mcc_code_sel");
    cancelPer("comm_cancel_period");
    cancelScope("comm_cancel_scope");
    cancelEvent("comm_cancel_event");
    checkButtonOff();
    funCode = wp.colStr("fund_code");
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
    controlTabName = "MKT_NFC_PARM";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.fund_code as fund_code," + "a.crt_user as bef_crt_user,"
        + "a.fund_name as bef_fund_name," + "a.fund_crt_date_s as bef_fund_crt_date_s,"
        + "a.fund_crt_date_e as bef_fund_crt_date_e," + "a.stop_flag as bef_stop_flag,"
        + "a.stop_date as bef_stop_date," + "a.stop_desc as bef_stop_desc,"
        + "a.effect_months as bef_effect_months," + "a.group_card_sel as bef_group_card_sel,"
        + "a.group_code_sel as bef_group_code_sel," + "a.payment_sel as bef_payment_sel,"
        + "a.merchant_sel as bef_merchant_sel," + "a.mcht_group_sel as bef_mcht_group_sel,"
        + "a.mcc_code_sel as bef_mcc_code_sel," + "a.bl_cond as bef_bl_cond,"
        + "a.it_cond as bef_it_cond," + "a.ca_cond as bef_ca_cond," + "a.id_cond as bef_id_cond,"
        + "a.ao_cond as bef_ao_cond," + "a.ot_cond as bef_ot_cond,"
        + "a.feedback_rate as bef_feedback_rate," + "a.feedback_lmt as bef_feedback_lmt,"
        + "a.cancel_period as bef_cancel_period," + "a.cancel_scope as bef_cancel_scope,"
        + "a.cancel_event as bef_cancel_event";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(funCode, "a.fund_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commGroupCrd("comm_group_card_sel");
    commGroupCode("comm_group_code_sel");
    commPaymentSel("comm_payment_sel");
    commMerchantSel("comm_merchant_sel");
    commGroupSel("comm_mcht_group_sel");
    commCodeSel("comm_mcc_code_sel");
    cancelPer("comm_cancel_period");
    cancelScope("comm_cancel_scope");
    cancelEvent("comm_cancel_event");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("group_card_sel_cnt",
        listMktParmData("mkt_parm_data_t", "MKT_NFC_PARM", wp.colStr("fund_code"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktParmData("mkt_parm_data_t", "MKT_NFC_PARM", wp.colStr("fund_code"), "2"));
    wp.colSet("payment_sel_cnt",
        listMktParmData("mkt_parm_data_t", "MKT_NFC_PARM", wp.colStr("fund_code"), "3"));
    wp.colSet("merchant_sel_cnt",
        listMktParmData("mkt_parm_data_t", "MKT_NFC_PARM", wp.colStr("fund_code"), "4"));
    wp.colSet("mcht_group_sel_cnt",
        listMktParmData("mkt_parm_data_t", "MKT_NFC_PARM", wp.colStr("fund_code"), "6"));
    wp.colSet("mcc_code_sel_cnt",
        listMktParmData("mkt_parm_data_t", "MKT_NFC_PARM", wp.colStr("fund_code"), "5"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("fund_name").equals(wp.colStr("bef_fund_name")))
      wp.colSet("opt_fund_name", "Y");

    if (!wp.colStr("fund_crt_date_s").equals(wp.colStr("bef_fund_crt_date_s")))
      wp.colSet("opt_fund_crt_date_s", "Y");

    if (!wp.colStr("fund_crt_date_e").equals(wp.colStr("bef_fund_crt_date_e")))
      wp.colSet("opt_fund_crt_date_e", "Y");

    if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
      wp.colSet("opt_stop_flag", "Y");

    if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
      wp.colSet("opt_stop_date", "Y");

    if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
      wp.colSet("opt_stop_desc", "Y");

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months", "Y");

    if (!wp.colStr("group_card_sel").equals(wp.colStr("bef_group_card_sel")))
      wp.colSet("opt_group_card_sel", "Y");
    commGroupCrd("comm_group_card_sel");
    commGroupCrd("comm_bef_group_card_sel");

    wp.colSet("bef_group_card_sel_cnt",
        listMktParmData("mkt_parm_data", "MKT_NFC_PARM", wp.colStr("fund_code"), "1"));
    if (!wp.colStr("group_card_sel_cnt").equals(wp.colStr("bef_group_card_sel_cnt")))
      wp.colSet("opt_group_card_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCode("comm_group_code_sel");
    commGroupCode("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktParmData("mkt_parm_data", "MKT_NFC_PARM", wp.colStr("fund_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("payment_sel").equals(wp.colStr("bef_payment_sel")))
      wp.colSet("opt_payment_sel", "Y");
    commPaymentSel("comm_payment_sel");
    commPaymentSel("comm_bef_payment_sel");

    wp.colSet("bef_payment_sel_cnt",
        listMktParmData("mkt_parm_data", "MKT_NFC_PARM", wp.colStr("fund_code"), "3"));
    if (!wp.colStr("payment_sel_cnt").equals(wp.colStr("bef_payment_sel_cnt")))
      wp.colSet("opt_payment_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchantSel("comm_merchant_sel");
    commMerchantSel("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listMktParmData("mkt_parm_data", "MKT_NFC_PARM", wp.colStr("fund_code"), "4"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    commGroupSel("comm_mcht_group_sel");
    commGroupSel("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listMktParmData("mkt_parm_data", "MKT_NFC_PARM", wp.colStr("fund_code"), "6"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
      wp.colSet("opt_mcc_code_sel", "Y");
    commCodeSel("comm_mcc_code_sel");
    commCodeSel("comm_bef_mcc_code_sel");

    wp.colSet("bef_mcc_code_sel_cnt",
        listMktParmData("mkt_parm_data", "MKT_NFC_PARM", wp.colStr("fund_code"), "5"));
    if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
      wp.colSet("opt_mcc_code_sel_cnt", "Y");

    if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
      wp.colSet("opt_bl_cond", "Y");

    if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
      wp.colSet("opt_it_cond", "Y");

    if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
      wp.colSet("opt_ca_cond", "Y");

    if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
      wp.colSet("opt_id_cond", "Y");

    if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
      wp.colSet("opt_ao_cond", "Y");

    if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
      wp.colSet("opt_ot_cond", "Y");

    if (!wp.colStr("feedback_rate").equals(wp.colStr("bef_feedback_rate")))
      wp.colSet("opt_feedback_rate", "Y");

    if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
      wp.colSet("opt_feedback_lmt", "Y");

    if (!wp.colStr("cancel_period").equals(wp.colStr("bef_cancel_period")))
      wp.colSet("opt_cancel_period", "Y");
    cancelPer("comm_cancel_period");
    cancelPer("comm_bef_cancel_period");

    if (!wp.colStr("cancel_scope").equals(wp.colStr("bef_cancel_scope")))
      wp.colSet("opt_cancel_scope", "Y");
    cancelScope("comm_cancel_scope");
    cancelScope("comm_bef_cancel_scope");

    if (!wp.colStr("cancel_event").equals(wp.colStr("bef_cancel_event")))
      wp.colSet("opt_cancel_event", "Y");
    cancelEvent("comm_cancel_event");
    cancelEvent("comm_bef_cancel_event");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("fund_name", "");
      wp.colSet("fund_crt_date_s", "");
      wp.colSet("fund_crt_date_e", "");
      wp.colSet("stop_flag", "");
      wp.colSet("stop_date", "");
      wp.colSet("stop_desc", "");
      wp.colSet("effect_months", "");
      wp.colSet("group_card_sel", "");
      wp.colSet("group_card_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("payment_sel", "");
      wp.colSet("payment_sel_cnt", "");
      wp.colSet("merchant_sel", "");
      wp.colSet("merchant_sel_cnt", "");
      wp.colSet("mcht_group_sel", "");
      wp.colSet("mcht_group_sel_cnt", "");
      wp.colSet("mcc_code_sel", "");
      wp.colSet("mcc_code_sel_cnt", "");
      wp.colSet("bl_cond", "");
      wp.colSet("it_cond", "");
      wp.colSet("ca_cond", "");
      wp.colSet("id_cond", "");
      wp.colSet("ao_cond", "");
      wp.colSet("ot_cond", "");
      wp.colSet("feedback_rate", "");
      wp.colSet("feedback_lmt", "");
      wp.colSet("cancel_period", "");
      wp.colSet("cancel_scope", "");
      wp.colSet("cancel_event", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("fund_name").length() == 0)
      wp.colSet("opt_fund_name", "Y");

    if (wp.colStr("fund_crt_date_s").length() == 0)
      wp.colSet("opt_fund_crt_date_s", "Y");

    if (wp.colStr("fund_crt_date_e").length() == 0)
      wp.colSet("opt_fund_crt_date_e", "Y");

    if (wp.colStr("stop_flag").length() == 0)
      wp.colSet("opt_stop_flag", "Y");

    if (wp.colStr("stop_date").length() == 0)
      wp.colSet("opt_stop_date", "Y");

    if (wp.colStr("stop_desc").length() == 0)
      wp.colSet("opt_stop_desc", "Y");

    if (wp.colStr("effect_months").length() == 0)
      wp.colSet("opt_effect_months", "Y");

    if (wp.colStr("group_card_sel").length() == 0)
      wp.colSet("opt_group_card_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("payment_sel").length() == 0)
      wp.colSet("opt_payment_sel", "Y");


    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("opt_merchant_sel", "Y");


    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("opt_mcht_group_sel", "Y");


    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("opt_mcc_code_sel", "Y");


    if (wp.colStr("bl_cond").length() == 0)
      wp.colSet("opt_bl_cond", "Y");

    if (wp.colStr("it_cond").length() == 0)
      wp.colSet("opt_it_cond", "Y");

    if (wp.colStr("ca_cond").length() == 0)
      wp.colSet("opt_ca_cond", "Y");

    if (wp.colStr("id_cond").length() == 0)
      wp.colSet("opt_id_cond", "Y");

    if (wp.colStr("ao_cond").length() == 0)
      wp.colSet("opt_ao_cond", "Y");

    if (wp.colStr("ot_cond").length() == 0)
      wp.colSet("opt_ot_cond", "Y");

    if (wp.colStr("feedback_rate").length() == 0)
      wp.colSet("opt_feedback_rate", "Y");

    if (wp.colStr("feedback_lmt").length() == 0)
      wp.colSet("opt_feedback_lmt", "Y");

    if (wp.colStr("cancel_period").length() == 0)
      wp.colSet("opt_cancel_period", "Y");

    if (wp.colStr("cancel_scope").length() == 0)
      wp.colSet("opt_cancel_scope", "Y");

    if (wp.colStr("cancel_event").length() == 0)
      wp.colSet("opt_cancel_event", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp3850Func func = new mktp02.Mktp3850Func(wp);

    String[] lsFundCode = wp.itemBuff("fund_code");
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

      func.varsSet("fund_code", lsFundCode[rr]);
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
	  String lsSql ="";
	  try {
	       if ((wp.respHtml.equals("mktp3850")))
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
  public void commGroupCrd(String cde1) throws Exception {
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
  public void commPaymentSel(String cde1) throws Exception {
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
  public void commGroupSel(String cde1) throws Exception {
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
  public void commCodeSel(String cde1) throws Exception {
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
  public void cancelPer(String cde1) throws Exception {
    String[] cde = {"0"};
    String[] txt = {"每月"};
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
  public void cancelScope(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"當期簽帳款", "所有簽帳款"};
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
  public void cancelEvent(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"不限定", "有有效卡"};
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
  String listMktParmData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = '" + tableName + "'" + " and   data_key   = '" + dataKey + "'"
        + " and   data_type  = '" + dataType + "'";
    sqlSelect(sql1);

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
         + " from sec_user a,mkt_nfc_parm_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
}

  // ************************************************************************

}  // End of class
