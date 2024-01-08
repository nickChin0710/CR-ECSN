/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/09  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110/11/15  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktp01;

import mktp01.Mktp0380Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0380 extends BaseProc {
  private String PROGNAME = "雙幣卡外幣刷卡金回饋參數檔維護處理程式108/09/09 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0380Func func = null;
  String rowid;
  String fundCode;
  String fstAprFlag = "";
  String orgTabName = "cyc_dc_fund_parm_t";
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
    } else if (eqIgno(wp.buttonCode, "R3"))
    {// 明細查詢 -/
        strAction = "R3";
        dataReadR3();
       }
    else if (eqIgno(wp.buttonCode, "R2"))
       {// 明細查詢 -/
        strAction = "R2";
        dataReadR2();
       }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_fund_code"), "a.fund_code", "like%")
        + sqlCol(wp.itemStr("ex_curr_code"), "a.curr_code", "like%")
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
        + "a.aud_type," + "a.fund_code," + "a.fund_name," + "a.curr_code," + "a.fund_crt_date_s,"
        + "a.stop_date," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by fund_code,curr_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCurrCode("comm_curr_code");

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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.fund_code as fund_code," + "a.crt_user," + "a.fund_name," + "a.fund_crt_date_s,"
        + "a.fund_crt_date_e," + "a.stop_date," + "a.stop_desc," + "a.curr_code,"
        + "a.feedback_month_s," + "a.feedback_month_e," + "a.effect_months," + "a.new_hldr_cond,"
        + "a.new_hldr_days," + "a.new_group_cond," + "a.new_hldr_card," + "a.new_hldr_sup,"
        + "a.source_code_sel," + "a.merchant_sel," + "a.mcht_group_sel," + "a.platform_kind_sel," + "a.group_card_sel,"
        + "a.group_code_sel," + "a.purchase_amt_s1," + "a.purchase_amt_e1," + "a.feedback_rate_1,"
        + "a.purchase_amt_s2," + "a.purchase_amt_e2," + "a.feedback_rate_2," + "a.purchase_amt_s3,"
        + "a.purchase_amt_e3," + "a.feedback_rate_3," + "a.purchase_amt_s4," + "a.purchase_amt_e4,"
        + "a.feedback_rate_4," + "a.purchase_amt_s5," + "a.purchase_amt_e5," + "a.feedback_rate_5,"
        + "a.feedback_lmt," + "a.issue_cond," + "a.issue_date_s," + "a.issue_date_e,"
        + "a.issue_num_1," + "a.issue_num_2," + "a.issue_num_3";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(fundCode, "a.fund_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    sourceCodeS("comm_source_code_sel");
    commMerchamt("comm_merchant_sel");
    mchtGroupS("comm_mcht_group_sel");
    mchtGroupS("comm_platform_kind_sel");
    groupCrdS("comm_group_card_sel");
    groupCodeS("comm_group_code_sel");
    checkButtonOff();
    fundCode = wp.colStr("fund_code");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataReadR3R();
    else {
      commfuncAudType("aud_type");
      listWkdataSpace();
    }
  }
  
  // ************************************************************************
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1);

    return ((int) sqlNum("bndataCount"));
  }
  
//************************************************************************
 public void dataReadR2() throws Exception {
   String bnTable = "";

   wp.selectCnt = 1;
   this.selectNoLimit();

   bnTable = "mkt_parm_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
       + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
   wp.daoTable = bnTable;
   wp.whereStr = "where 1=1" + " and table_name  =  'CYC_DC_FUND_PARM' ";
   if (wp.respHtml.equals("mktp0380_gncd"))
     wp.whereStr += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktp0380_srcd"))
     wp.whereStr += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktp0380_aaa1"))
     wp.whereStr += " and data_type  = '6' ";
   if (wp.respHtml.equals("mktp0380_aaa2"))
       wp.whereStr += " and data_type  = 'P' ";
   if (wp.respHtml.equals("mktp0380_grcd"))
     wp.whereStr += " and data_type  = '2' ";
   
   wp.whereStr += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("fund_code"));
   wp.whereStr += " order by 4,5,6 ";
   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
   if (wp.respHtml.equals("mktp0380_gncd"))
     commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0380_srcd"))
     commSrcCode("comm_data_code");
   if (wp.respHtml.equals("mktp0380_aaa1"))
     commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktp0380_aaa2"))
     commDataCode0P("comm_data_code");
   if (wp.respHtml.equals("mktp0380_grcd"))
     commDataCode04("comm_data_code");
 }
 
 // ************************************************************************
 public void dataReadR3() throws Exception {
   String bnTable = "";
   wp.selectCnt = 1;
   this.selectNoLimit();
   bnTable = "mkt_parm_data_t";
   
   wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
       + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
       + "mod_user as r2_mod_user ";
   wp.daoTable = bnTable;
   wp.whereStr = "where 1=1" + " and table_name  =  'CYC_DC_FUND_PARM' ";
   if (wp.respHtml.equals("mktp0380_mrch"))
     wp.whereStr += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktp0380_gpcd"))
     wp.whereStr += " and data_type  = '1' ";

   wp.whereStr += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("fund_code"));
   wp.whereStr += " order by 4,5,6,7 ";
   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
   if (wp.respHtml.equals("mktp0380_gpcd"))
     commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0380_gpcd"))
     commCardType("comm_data_code2");
 }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "CYC_DC_FUND_PARM";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.fund_code as fund_code," + "a.crt_user as bef_crt_user,"
        + "a.fund_name as bef_fund_name," + "a.fund_crt_date_s as bef_fund_crt_date_s,"
        + "a.fund_crt_date_e as bef_fund_crt_date_e," + "a.stop_date as bef_stop_date,"
        + "a.stop_desc as bef_stop_desc," + "a.curr_code as bef_curr_code,"
        + "a.feedback_month_s as bef_feedback_month_s,"
        + "a.feedback_month_e as bef_feedback_month_e," + "a.effect_months as bef_effect_months,"
        + "a.new_hldr_cond as bef_new_hldr_cond," + "a.new_hldr_days as bef_new_hldr_days,"
        + "a.new_group_cond as bef_new_group_cond," + "a.new_hldr_card as bef_new_hldr_card,"
        + "a.new_hldr_sup as bef_new_hldr_sup," + "a.source_code_sel as bef_source_code_sel,"
        + "a.merchant_sel as bef_merchant_sel," + "a.mcht_group_sel as bef_mcht_group_sel," + "a.platform_kind_sel as bef_platform_kind_sel,"
        + "a.group_card_sel as bef_group_card_sel," + "a.group_code_sel as bef_group_code_sel,"
        + "a.purchase_amt_s1 as bef_purchase_amt_s1," + "a.purchase_amt_e1 as bef_purchase_amt_e1,"
        + "a.feedback_rate_1 as bef_feedback_rate_1," + "a.purchase_amt_s2 as bef_purchase_amt_s2,"
        + "a.purchase_amt_e2 as bef_purchase_amt_e2," + "a.feedback_rate_2 as bef_feedback_rate_2,"
        + "a.purchase_amt_s3 as bef_purchase_amt_s3," + "a.purchase_amt_e3 as bef_purchase_amt_e3,"
        + "a.feedback_rate_3 as bef_feedback_rate_3," + "a.purchase_amt_s4 as bef_purchase_amt_s4,"
        + "a.purchase_amt_e4 as bef_purchase_amt_e4," + "a.feedback_rate_4 as bef_feedback_rate_4,"
        + "a.purchase_amt_s5 as bef_purchase_amt_s5," + "a.purchase_amt_e5 as bef_purchase_amt_e5,"
        + "a.feedback_rate_5 as bef_feedback_rate_5," + "a.feedback_lmt as bef_feedback_lmt,"
        + "a.issue_cond as bef_issue_cond," + "a.issue_date_s as bef_issue_date_s,"
        + "a.issue_date_e as bef_issue_date_e," + "a.issue_num_1 as bef_issue_num_1,"
        + "a.issue_num_2 as bef_issue_num_2," + "a.issue_num_3 as bef_issue_num_3";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(fundCode, "a.fund_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    sourceCodeS("comm_source_code_sel");
    commMerchamt("comm_merchant_sel");
    mchtGroupS("comm_mcht_group_sel");
    mchtGroupS("comm_platform_kind_sel");
    groupCrdS("comm_group_card_sel");
    groupCodeS("comm_group_code_sel");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }
  
  // ************************************************************************
  public void commSrcCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " source_name as column_source_name " + " from ptr_src_code "
          + " where 1 = 1 "
//              + " and   source_code = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "source_code");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_source_name");
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
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 "
//              + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "group_code");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commCardType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
//          + " and   card_type = '" + wp.colStr(ii, "data_code2") + "'";
          + sqlCol(wp.colStr(ii, "data_code2"), "card_type");
      if (wp.colStr(ii, "data_code2").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_name");
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
      sql1 = "select " + " mcht_group_desc as column_mcht_group_desc " + " from mkt_mcht_gp "
          + " where 1 = 1 and  platform_flag != '2' "
//              + " and   mcht_group_id = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "mcht_group_id");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }
  
  // ************************************************************************
  public void commDataCode0P(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcht_group_desc as column_mcht_group_desc " + " from mkt_mcht_gp "
          + " where 1 = 1 and  platform_flag = '2' "
//              + " and   mcht_group_id = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "mcht_group_id");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("source_code_sel_cnt",
        listMktParmData("mkt_parm_data_t", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "3"));
    wp.colSet("merchant_sel_cnt",
        listMktParmData("mkt_parm_data_t", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "4"));
    wp.colSet("mcht_group_sel_cnt",
        listMktParmData("mkt_parm_data_t", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "6"));
    wp.colSet("platform_kind_sel_cnt",
        listMktParmData("mkt_parm_data_t", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "P"));
    wp.colSet("group_card_sel_cnt",
        listMktParmData("mkt_parm_data_t", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktParmData("mkt_parm_data_t", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "2"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("fund_name").equals(wp.colStr("bef_fund_name")))
      wp.colSet("opt_fund_name", "Y");

    if (!wp.colStr("fund_crt_date_s").equals(wp.colStr("bef_fund_crt_date_s")))
      wp.colSet("opt_fund_crt_date_s", "Y");

    if (!wp.colStr("fund_crt_date_e").equals(wp.colStr("bef_fund_crt_date_e")))
      wp.colSet("opt_fund_crt_date_e", "Y");

    if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
      wp.colSet("opt_stop_date", "Y");

    if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
      wp.colSet("opt_stop_desc", "Y");

    if (!wp.colStr("curr_code").equals(wp.colStr("bef_curr_code")))
      wp.colSet("opt_curr_code", "Y");

    if (!wp.colStr("feedback_month_s").equals(wp.colStr("bef_feedback_month_s")))
      wp.colSet("opt_feedback_month_s", "Y");

    if (!wp.colStr("feedback_month_e").equals(wp.colStr("bef_feedback_month_e")))
      wp.colSet("opt_feedback_month_e", "Y");

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months", "Y");

    if (!wp.colStr("new_hldr_cond").equals(wp.colStr("bef_new_hldr_cond")))
      wp.colSet("opt_new_hldr_cond", "Y");

    if (!wp.colStr("new_hldr_days").equals(wp.colStr("bef_new_hldr_days")))
      wp.colSet("opt_new_hldr_days", "Y");

    if (!wp.colStr("new_group_cond").equals(wp.colStr("bef_new_group_cond")))
      wp.colSet("opt_new_group_cond", "Y");

    if (!wp.colStr("new_hldr_card").equals(wp.colStr("bef_new_hldr_card")))
      wp.colSet("opt_new_hldr_card", "Y");

    if (!wp.colStr("new_hldr_sup").equals(wp.colStr("bef_new_hldr_sup")))
      wp.colSet("opt_new_hldr_sup", "Y");

    if (!wp.colStr("source_code_sel").equals(wp.colStr("bef_source_code_sel")))
      wp.colSet("opt_source_code_sel", "Y");
    sourceCodeS("comm_source_code_sel");
    sourceCodeS("comm_bef_source_code_sel");

    wp.colSet("bef_source_code_sel_cnt",
        listMktParmData("mkt_parm_data", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "3"));
    if (!wp.colStr("source_code_sel_cnt").equals(wp.colStr("bef_source_code_sel_cnt")))
      wp.colSet("opt_source_code_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchamt("comm_merchant_sel");
    commMerchamt("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listMktParmData("mkt_parm_data", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "4"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    mchtGroupS("comm_mcht_group_sel");
    mchtGroupS("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listMktParmData("mkt_parm_data", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "6"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");
    
    if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
        wp.colSet("opt_platform_kind_sel", "Y");
      mchtGroupS("comm_platform_kind_sel");
      mchtGroupS("comm_bef_platform_kind_sel");

      wp.colSet("bef_platform_kind_sel_cnt",
          listMktParmData("mkt_parm_data", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "P"));
      if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
        wp.colSet("opt_platform_kind_sel_cnt", "Y");

    if (!wp.colStr("group_card_sel").equals(wp.colStr("bef_group_card_sel")))
      wp.colSet("opt_group_card_sel", "Y");
    groupCrdS("comm_group_card_sel");
    groupCrdS("comm_bef_group_card_sel");

    wp.colSet("bef_group_card_sel_cnt",
        listMktParmData("mkt_parm_data", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "1"));
    if (!wp.colStr("group_card_sel_cnt").equals(wp.colStr("bef_group_card_sel_cnt")))
      wp.colSet("opt_group_card_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    groupCodeS("comm_group_code_sel");
    groupCodeS("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktParmData("mkt_parm_data", "CYC_DC_FUND_PARM", wp.colStr("fund_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("purchase_amt_s1").equals(wp.colStr("bef_purchase_amt_s1")))
      wp.colSet("opt_purchase_amt_s1", "Y");

    if (!wp.colStr("purchase_amt_e1").equals(wp.colStr("bef_purchase_amt_e1")))
      wp.colSet("opt_purchase_amt_e1", "Y");

    if (!wp.colStr("feedback_rate_1").equals(wp.colStr("bef_feedback_rate_1")))
      wp.colSet("opt_feedback_rate_1", "Y");

    if (!wp.colStr("purchase_amt_s2").equals(wp.colStr("bef_purchase_amt_s2")))
      wp.colSet("opt_purchase_amt_s2", "Y");

    if (!wp.colStr("purchase_amt_e2").equals(wp.colStr("bef_purchase_amt_e2")))
      wp.colSet("opt_purchase_amt_e2", "Y");

    if (!wp.colStr("feedback_rate_2").equals(wp.colStr("bef_feedback_rate_2")))
      wp.colSet("opt_feedback_rate_2", "Y");

    if (!wp.colStr("purchase_amt_s3").equals(wp.colStr("bef_purchase_amt_s3")))
      wp.colSet("opt_purchase_amt_s3", "Y");

    if (!wp.colStr("purchase_amt_e3").equals(wp.colStr("bef_purchase_amt_e3")))
      wp.colSet("opt_purchase_amt_e3", "Y");

    if (!wp.colStr("feedback_rate_3").equals(wp.colStr("bef_feedback_rate_3")))
      wp.colSet("opt_feedback_rate_3", "Y");

    if (!wp.colStr("purchase_amt_s4").equals(wp.colStr("bef_purchase_amt_s4")))
      wp.colSet("opt_purchase_amt_s4", "Y");

    if (!wp.colStr("purchase_amt_e4").equals(wp.colStr("bef_purchase_amt_e4")))
      wp.colSet("opt_purchase_amt_e4", "Y");

    if (!wp.colStr("feedback_rate_4").equals(wp.colStr("bef_feedback_rate_4")))
      wp.colSet("opt_feedback_rate_4", "Y");

    if (!wp.colStr("purchase_amt_s5").equals(wp.colStr("bef_purchase_amt_s5")))
      wp.colSet("opt_purchase_amt_s5", "Y");

    if (!wp.colStr("purchase_amt_e5").equals(wp.colStr("bef_purchase_amt_e5")))
      wp.colSet("opt_purchase_amt_e5", "Y");

    if (!wp.colStr("feedback_rate_5").equals(wp.colStr("bef_feedback_rate_5")))
      wp.colSet("opt_feedback_rate_5", "Y");

    if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
      wp.colSet("opt_feedback_lmt", "Y");

    if (!wp.colStr("issue_cond").equals(wp.colStr("bef_issue_cond")))
      wp.colSet("opt_issue_cond", "Y");

    if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
      wp.colSet("opt_issue_date_s", "Y");

    if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
      wp.colSet("opt_issue_date_e", "Y");

    if (!wp.colStr("issue_num_1").equals(wp.colStr("bef_issue_num_1")))
      wp.colSet("opt_issue_num_1", "Y");

    if (!wp.colStr("issue_num_2").equals(wp.colStr("bef_issue_num_2")))
      wp.colSet("opt_issue_num_2", "Y");

    if (!wp.colStr("issue_num_3").equals(wp.colStr("bef_issue_num_3")))
      wp.colSet("opt_issue_num_3", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("fund_name", "");
      wp.colSet("fund_crt_date_s", "");
      wp.colSet("fund_crt_date_e", "");
      wp.colSet("stop_date", "");
      wp.colSet("stop_desc", "");
      wp.colSet("curr_code", "");
      wp.colSet("feedback_month_s", "");
      wp.colSet("feedback_month_e", "");
      wp.colSet("effect_months", "");
      wp.colSet("new_hldr_cond", "");
      wp.colSet("new_hldr_days", "");
      wp.colSet("new_group_cond", "");
      wp.colSet("new_hldr_card", "");
      wp.colSet("new_hldr_sup", "");
      wp.colSet("source_code_sel", "");
      wp.colSet("source_code_sel_cnt", "");
      wp.colSet("merchant_sel", "");
      wp.colSet("merchant_sel_cnt", "");
      wp.colSet("mcht_group_sel", "");
      wp.colSet("mcht_group_sel_cnt", "");
      wp.colSet("platform_kind_sel", "");
      wp.colSet("platform_kind_sel_cnt", "");
      wp.colSet("group_card_sel", "");
      wp.colSet("group_card_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("purchase_amt_s1", "");
      wp.colSet("purchase_amt_e1", "");
      wp.colSet("feedback_rate_1", "");
      wp.colSet("purchase_amt_s2", "");
      wp.colSet("purchase_amt_e2", "");
      wp.colSet("feedback_rate_2", "");
      wp.colSet("purchase_amt_s3", "");
      wp.colSet("purchase_amt_e3", "");
      wp.colSet("feedback_rate_3", "");
      wp.colSet("purchase_amt_s4", "");
      wp.colSet("purchase_amt_e4", "");
      wp.colSet("feedback_rate_4", "");
      wp.colSet("purchase_amt_s5", "");
      wp.colSet("purchase_amt_e5", "");
      wp.colSet("feedback_rate_5", "");
      wp.colSet("feedback_lmt", "");
      wp.colSet("issue_cond", "");
      wp.colSet("issue_date_s", "");
      wp.colSet("issue_date_e", "");
      wp.colSet("issue_num_1", "");
      wp.colSet("issue_num_2", "");
      wp.colSet("issue_num_3", "");
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

    if (wp.colStr("stop_date").length() == 0)
      wp.colSet("opt_stop_date", "Y");

    if (wp.colStr("stop_desc").length() == 0)
      wp.colSet("opt_stop_desc", "Y");

    if (wp.colStr("curr_code").length() == 0)
      wp.colSet("opt_curr_code", "Y");

    if (wp.colStr("feedback_month_s").length() == 0)
      wp.colSet("opt_feedback_month_s", "Y");

    if (wp.colStr("feedback_month_e").length() == 0)
      wp.colSet("opt_feedback_month_e", "Y");

    if (wp.colStr("effect_months").length() == 0)
      wp.colSet("opt_effect_months", "Y");

    if (wp.colStr("new_hldr_cond").length() == 0)
      wp.colSet("opt_new_hldr_cond", "Y");

    if (wp.colStr("new_hldr_days").length() == 0)
      wp.colSet("opt_new_hldr_days", "Y");

    if (wp.colStr("new_group_cond").length() == 0)
      wp.colSet("opt_new_group_cond", "Y");

    if (wp.colStr("new_hldr_card").length() == 0)
      wp.colSet("opt_new_hldr_card", "Y");

    if (wp.colStr("new_hldr_sup").length() == 0)
      wp.colSet("opt_new_hldr_sup", "Y");

    if (wp.colStr("source_code_sel").length() == 0)
      wp.colSet("opt_source_code_sel", "Y");


    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("opt_merchant_sel", "Y");


    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("opt_mcht_group_sel", "Y");

    
    if (wp.colStr("platform_kind_sel").length() == 0)
        wp.colSet("opt_platform_kind_sel", "Y");

    if (wp.colStr("group_card_sel").length() == 0)
      wp.colSet("opt_group_card_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("purchase_amt_s1").length() == 0)
      wp.colSet("opt_purchase_amt_s1", "Y");

    if (wp.colStr("purchase_amt_e1").length() == 0)
      wp.colSet("opt_purchase_amt_e1", "Y");

    if (wp.colStr("feedback_rate_1").length() == 0)
      wp.colSet("opt_feedback_rate_1", "Y");

    if (wp.colStr("purchase_amt_s2").length() == 0)
      wp.colSet("opt_purchase_amt_s2", "Y");

    if (wp.colStr("purchase_amt_e2").length() == 0)
      wp.colSet("opt_purchase_amt_e2", "Y");

    if (wp.colStr("feedback_rate_2").length() == 0)
      wp.colSet("opt_feedback_rate_2", "Y");

    if (wp.colStr("purchase_amt_s3").length() == 0)
      wp.colSet("opt_purchase_amt_s3", "Y");

    if (wp.colStr("purchase_amt_e3").length() == 0)
      wp.colSet("opt_purchase_amt_e3", "Y");

    if (wp.colStr("feedback_rate_3").length() == 0)
      wp.colSet("opt_feedback_rate_3", "Y");

    if (wp.colStr("purchase_amt_s4").length() == 0)
      wp.colSet("opt_purchase_amt_s4", "Y");

    if (wp.colStr("purchase_amt_e4").length() == 0)
      wp.colSet("opt_purchase_amt_e4", "Y");

    if (wp.colStr("feedback_rate_4").length() == 0)
      wp.colSet("opt_feedback_rate_4", "Y");

    if (wp.colStr("purchase_amt_s5").length() == 0)
      wp.colSet("opt_purchase_amt_s5", "Y");

    if (wp.colStr("purchase_amt_e5").length() == 0)
      wp.colSet("opt_purchase_amt_e5", "Y");

    if (wp.colStr("feedback_rate_5").length() == 0)
      wp.colSet("opt_feedback_rate_5", "Y");

    if (wp.colStr("feedback_lmt").length() == 0)
      wp.colSet("opt_feedback_lmt", "Y");

    if (wp.colStr("issue_cond").length() == 0)
      wp.colSet("opt_issue_cond", "Y");

    if (wp.colStr("issue_date_s").length() == 0)
      wp.colSet("opt_issue_date_s", "Y");

    if (wp.colStr("issue_date_e").length() == 0)
      wp.colSet("opt_issue_date_e", "Y");

    if (wp.colStr("issue_num_1").length() == 0)
      wp.colSet("opt_issue_num_1", "Y");

    if (wp.colStr("issue_num_2").length() == 0)
      wp.colSet("opt_issue_num_2", "Y");

    if (wp.colStr("issue_num_3").length() == 0)
      wp.colSet("opt_issue_num_3", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp01.Mktp0380Func func = new mktp01.Mktp0380Func(wp);

    String[] lsFundCode = wp.itemBuff("fund_code");
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
      //if (lsCrtUser[rr].equals(wp.loginUser)) {
      //  ilAuth++;
      //  wp.colSet(rr, "ok_flag", "F");
      //  continue;
     // }

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
        commCurrCode("comm_curr_code");
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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktp0380"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_curr_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_curr_code");
        }
        this.dddwList("dddw_curr_code", "ptr_currcode", "trim(curr_code)", "trim(curr_chi_name)",
            " where bill_sort_seq !=''");
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
  public void commCurrCode(String code) throws Exception {
    commCurrCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commCurrCode(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " curr_chi_name as column_curr_chi_name " + " from ptr_currcode "
          + " where 1 = 1 " + " and   curr_code = ? ";
      if (wp.colStr(ii, befStr + "curr_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "curr_code") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_curr_chi_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void sourceCodeS(String cde1) throws Exception {
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
  public void mchtGroupS(String cde1) throws Exception {
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
  public void groupCrdS(String cde1) throws Exception {
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
  public void groupCodeS(String cde1) throws Exception {
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
  String listMktParmData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = ? " + " and   data_key   = ? "
        + " and   data_type  = ? ";
    sqlSelect(sql1, new Object[] { tableName, dataKey, dataType });

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }


  // ************************************************************************
//************************************************************************
public String procDynamicDddwCrtuser1(String string)  throws Exception
{
  String lsSql = "";

  lsSql = " select "
         + " b.crt_user as db_code, "
         + " max(b.crt_user||' '||a.usr_cname) as db_desc "
         + " from sec_user a,cyc_dc_fund_parm_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
}
} // End of class
