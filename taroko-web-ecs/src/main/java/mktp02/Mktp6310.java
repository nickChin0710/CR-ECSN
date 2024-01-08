/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/23  V1.00.00   machao      Initial                              *
* 112/04/18  V1.00.01   Zuwei Su      [一般消費群組 ]中文顯示錯誤        *
* 112-05-09  V1.00.03   Ryan    新增國內外消費欄位、ATM手續費回饋加碼欄位，特店中文名稱、特店英文名稱參數維護                           
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                         *
***************************************************************************/
package mktp02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6310 extends BaseProc {
  private final String PROGNAME = "COMBO現金回饋參數檔覆核112/03/23  V1.00.00";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6310Func func = null;
  String rowid;
  String fundCode;
  String orgTabName = "PTR_COMBO_FUNDP_T";
  String controlTabName = "";
  int qFrom = 0;

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
    else if (eqIgno(wp.buttonCode, "R5")) {
        strAction = "R5";
        dataReadR5();
    }
    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_fund_code"), "a.fund_code", "like%")
        ;

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
        + "a.aud_type," + "a.fund_code," + "a.fund_name," + "a.fund_crt_date_s," + "a.fund_crt_date_e,"
        + "a.stop_date," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by fund_code";

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
      if (wp.itemStr("fund_code").length() == 0) {
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
    		+  " a.fund_code, "+ " a.fund_name, " + " a.fund_crt_date_s, " + " a.fund_crt_date_e, "
	    	+ " a.stop_flag, "+ " a.stop_date, " + " a.stop_desc, "
	   		+  " a.effect_months, " + " a.acct_type_sel, " + " a.merchant_sel, " + " a.mcht_group_sel, " + " a.platform_kind_sel, "
	   	    +  " a.group_card_sel, " + " a.group_code_sel, " + " a.bl_cond, " + " a.ca_cond, " + " a.id_cond, " + " a.ao_cond, "
	   	    +  " a.it_cond, " + " a.ot_cond, " + " a.fund_feed_flag, " + " a.threshold_sel, " + " a.purchase_type_sel, "
			+ "a.purchse_s_amt_1 as purchase_s_amt_1," + "a.purchse_e_amt_1 as purchase_e_amt_1,"
			+ "a.purchse_rate_1 as purchase_rate_1," + "a.purchse_s_amt_2 as purchase_s_amt_2,"
			+ "a.purchse_e_amt_2 as purchase_e_amt_2," + "a.purchse_rate_2 as purchase_rate_2,"
			+ "a.purchse_s_amt_3 as purchase_s_amt_3," + "a.purchse_e_amt_3 as purchase_e_amt_3,"
			+ "a.purchse_rate_3 as purchase_rate_3," + "a.purchse_s_amt_4 as purchase_s_amt_4,"
			+ "a.purchse_e_amt_4 as purchase_e_amt_4," + "a.purchse_rate_4 as purchase_rate_4,"
			+ "a.purchse_s_amt_5 as purchase_s_amt_5," + "a.purchse_e_amt_5 as purchase_e_amt_5,"
			+ "a.purchse_rate_5 as purchase_rate_5," 
	   		+  " a.save_s_amt_1, " 
	   		+  " a.save_e_amt_1, " +  " a.save_rate_1, " +  " a.save_s_amt_2, " +  " a.save_e_amt_2, " +  " a.save_rate_2, " 
	   	    +  " a.save_s_amt_3, " +  " a.save_e_amt_3, " +  " a.save_rate_3, " +  " a.save_s_amt_4, " +  " a.save_e_amt_4, " 
	   		+  " a.save_rate_4, " +  " a.save_s_amt_5, " +  " a.save_e_amt_5, " +  " a.save_rate_5, " +  " a.feedback_lmt, " 
	   	    +  " a.feedback_type, " +  " a.card_feed_run_day, " +  " a.cancel_period, " +  " a.cancel_s_month, " 
	   		+  " a.cancel_unbill_type, " +  " a.cancel_unbill_rate, " +  " a.cancel_event, " +  " a.apr_date, " 
	   	    +  " a.apr_flag, " +  " a.apr_user, " +  " a.crt_date, " +  " a.crt_user,"
	   	    + "a.foreign_code,"
	   	    + "a.mcht_cname_sel,"
	   	    + "a.mcht_ename_sel,"
	   	    + "a.atmibwf_cond,"
	   	    + "a.onlyaddon_calcond ";
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
    commAcctType("comm_acct_type_sel");
    commMerchamt("comm_merchant_sel");
    mchtGroupS("comm_mcht_group_sel");
    mchtGroupS("comm_platform_kind_sel");
    groupCrdS("comm_group_card_sel");
    groupCodeS("comm_group_code_sel");
    commFundfeedFlag("comm_fund_feed_flag");
    commFeedbackType("comm_feedback_type");
    commCancelPeriod("comm_cancel_period");
    commCancelUt("comm_cancel_unbill_type");
    commForeignCode("comm_foreign_code");
    commMchtCname("comm_mcht_cname_sel");
    commMchtEname("comm_mcht_ename_sel");
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
   wp.whereStr = "where 1=1" + " and table_name  =  'PTR_COMBO_FUNDP' ";
   if (wp.respHtml.equals("mktp6310_acty"))
     wp.whereStr += " and data_type  = '3' ";
   if (wp.respHtml.equals("mktp6310_mrch"))
     wp.whereStr += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktp6310_aaa1"))
     wp.whereStr += " and data_type  = '6' ";
   if (wp.respHtml.equals("mktp6310_aaa2"))
       wp.whereStr += " and data_type  = 'P' ";
   if (wp.respHtml.equals("mktp6310_gpcd"))
	     wp.whereStr += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktp6310_grcd"))
     wp.whereStr += " and data_type  = '2' ";
   
   wp.whereStr += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("fund_code"));
   wp.whereStr += " order by 4,5,6 ";
   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
   if (wp.respHtml.equals("mktp6310_acty"))
     commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp6310_mrch"))
     commSrcCode("comm_data_code");
   if (wp.respHtml.equals("mktp6310_aaa1"))
     commDataCode07("comm_data_code");
   if (wp.respHtml.equals("mktp6310_aaa2"))
     commDataCode0P("comm_data_code");
   if (wp.respHtml.equals("mktp6310_gpcd"))
	     commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp6310_grcd"))
     commDataCode04("comm_data_code");
   String fundName = SelectFundpT(wp.itemStr("fund_code"));
   wp.colSet("fund_name",fundName);
 }
 
 public String SelectFundpT(String fundcode) {
	 String lsSql = " select fund_name from PTR_COMBO_FUNDP_t where fund_code = ? ";
     sqlSelect(lsSql, new Object[]{fundcode});
     
	return sqlStr("fund_name");
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
   wp.whereStr = "where 1=1" + " and table_name  =  'PTR_COMBO_FUNDP' ";
   if (wp.respHtml.equals("mktp6310_acty"))
	     wp.whereStr += " and data_type  = '3' ";
	   if (wp.respHtml.equals("mktp6310_mrch"))
	     wp.whereStr += " and data_type  = '4' ";
	   if (wp.respHtml.equals("mktp6310_aaa1"))
	     wp.whereStr += " and data_type  = '6' ";
	   if (wp.respHtml.equals("mktp6310_aaa2"))
	       wp.whereStr += " and data_type  = 'P' ";
	   if (wp.respHtml.equals("mktp6310_gpcd"))
		     wp.whereStr += " and data_type  = '1' ";
	   if (wp.respHtml.equals("mktp6310_grcd"))
	     wp.whereStr += " and data_type  = '2' ";

   wp.whereStr += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr("fund_code"));
   wp.whereStr += " order by 4,5,6,7 ";
   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
   if (wp.respHtml.equals("mktp6310_acty"))
	     commDataCode04("comm_data_code");
	   if (wp.respHtml.equals("mktp6310_mrch"))
	     commSrcCode("comm_data_code");
	   if (wp.respHtml.equals("mktp6310_aaa1"))
	     commDataCode07("comm_data_code");
	   if (wp.respHtml.equals("mktp6310_aaa2"))
	     commDataCode0P("comm_data_code");
	   if (wp.respHtml.equals("mktp6310_gpcd"))
		     commDataCode04("comm_data_code");
	   if (wp.respHtml.equals("mktp6310_grcd"))
	     commDataCode04("comm_data_code");
   
   String fundName = SelectFundpT(wp.itemStr("fund_code"));
   wp.colSet("fund_name",fundName);
 }
 
 public void dataReadR5() throws Exception
 {
 dataReadR5(0);
 }
 //************************************************************************
     public void dataReadR5(int fromType) throws Exception {
         String bnTable = "";

         if ((wp.itemStr("fund_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
             alertErr("鍵值為空白或主檔未新增 ");
             return;
         }
         wp.selectCnt = 1;
         this.selectNoLimit();
         if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
             buttonOff("btnUpdate_disable");
             buttonOff("newDetail_disable");
             bnTable = "mkt_parm_cdata";
         } else {
             wp.colSet("btnUpdate_disable", "");
             wp.colSet("newDetail_disable", "");
             bnTable = "mkt_parm_cdata_t";
         }

         wp.selectSQL = "hex(rowid) as r2_rowid, "
                 + "ROW_NUMBER()OVER() as ser_num, "
                 + "0 as r2_mod_seqno, "
                 + "data_key, "
                 + "data_code, "
                 + "mod_user as r2_mod_user ";
         wp.daoTable = bnTable;
         wp.whereStr = "where 1=1" + " and table_name  =  'PTR_COMBO_FUNDP' ";
         if (wp.respHtml.equals("mktp6310_namc"))
             wp.whereStr += " and data_type  = 'A' ";
         if (wp.respHtml.equals("mktp6310_name"))
             wp.whereStr += " and data_type  = 'B' ";
         String whereCnt = wp.whereStr;
         wp.whereStr += " and  data_key = :data_key ";
         setString("data_key", wp.itemStr("fund_code"));
         whereCnt += " and  data_key = '" + wp.itemStr("fund_code") + "'";
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

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "PTR_COMBO_FUNDP";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.fund_code as fund_code," + "a.crt_user as bef_crt_user,"
        + "a.fund_name as bef_fund_name," + "a.fund_crt_date_s as bef_fund_crt_date_s,"
        + "a.fund_crt_date_e as bef_fund_crt_date_e," + "a.stop_date as bef_stop_date,"
        + "a.stop_desc as bef_stop_desc," 
        + "a.effect_months as bef_effect_months,"
        + "a.acct_type_sel as bef_acct_type_sel,"
        + "a.merchant_sel as bef_merchant_sel," + "a.mcht_group_sel as bef_mcht_group_sel," + "a.platform_kind_sel as bef_platform_kind_sel,"
        + "a.group_card_sel as bef_group_card_sel," + "a.group_code_sel as bef_group_code_sel,"
        + "a.bl_cond as bef_bl_cond, " + " a.ca_cond as bef_ca_cond, " + " a.id_cond as bef_id_cond, " + " a.ao_cond as bef_ao_cond, "
   	    +  " a.it_cond as bef_it_cond, " + " a.ot_cond as bef_ot_cond, " + " a.fund_feed_flag as bef_fund_feed_flag, " 
        + " a.threshold_sel as bef_threshold_sel, " + " a.purchase_type_sel as bef_purchase_type_sel, "
   	    
        + "a.purchse_s_amt_1 as bef_purchase_s_amt_1," + "a.purchse_e_amt_1 as bef_purchase_e_amt_1," + "a.purchse_rate_1 as bef_purchase_rate_1," 
        + "a.purchse_s_amt_2 as bef_purchase_s_amt_2," + "a.purchse_e_amt_2 as bef_purchase_e_amt_2," + "a.purchse_rate_2 as bef_purchase_rate_2,"
        + "a.purchse_s_amt_3 as bef_purchase_s_amt_3," + "a.purchse_e_amt_3 as bef_purchase_e_amt_3," + "a.purchse_rate_3 as bef_purchase_rate_3," 
        + "a.purchse_s_amt_4 as bef_purchase_s_amt_4," + "a.purchse_e_amt_4 as bef_purchase_e_amt_4," + "a.purchse_rate_4 as bef_purchase_rate_4,"
        + "a.purchse_s_amt_5 as bef_purchase_s_amt_5," + "a.purchse_e_amt_5 as bef_purchase_e_amt_5," + "a.purchse_rate_5 as bef_purchase_rate_5," 
        + "a.save_s_amt_1 as bef_save_s_amt_1, " +  " a.save_e_amt_1 as bef_save_e_amt_1, " +  " a.save_rate_1 as bef_save_rate_1, " 
        + "a.save_s_amt_2 as bef_save_s_amt_2, " +  " a.save_e_amt_2 as bef_save_e_amt_2, " +  " a.save_rate_2 as bef_save_rate_2, " 
        + "a.save_s_amt_3 as bef_save_s_amt_3, " +  " a.save_e_amt_3 as bef_save_e_amt_3, " +  " a.save_rate_3 as bef_save_rate_3, "
        + "a.save_s_amt_4 as bef_save_s_amt_4, " +  " a.save_e_amt_4 as bef_save_e_amt_4, " +  " a.save_rate_4 as bef_save_rate_4, "
        + "a.save_s_amt_5 as bef_save_s_amt_5, " +  " a.save_e_amt_5 as bef_save_e_amt_5, " +  " a.save_rate_5 as bef_save_rate_5, "
        + "a.feedback_lmt as bef_feedback_lmt,"
        + "a.feedback_type as bef_feedback_type," + "a.card_feed_run_day as bef_card_feed_run_day,"
        + "a.cancel_period as bef_cancel_period," + "a.cancel_s_month as bef_cancel_s_month,"
        + "a.cancel_unbill_type as bef_cancel_unbill_type," + "a.cancel_unbill_rate as bef_cancel_unbill_rate," + "a.cancel_event as bef_cancel_event,"
        + "a.foreign_code as bef_foreign_code,"
        + "a.mcht_cname_sel as bef_mcht_cname_sel,"
        + "a.mcht_ename_sel as bef_mcht_ename_sel,"
        + "a.atmibwf_cond as bef_atmibwf_cond,"
        + "a.onlyaddon_calcond as bef_onlyaddon_calcond " ;

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(fundCode, "a.fund_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAcctType("comm_acct_type_sel");
    commMerchamt("comm_merchant_sel");
    mchtGroupS("comm_mcht_group_sel");
    mchtGroupS("comm_platform_kind_sel");
    groupCrdS("comm_group_card_sel");
    groupCodeS("comm_group_code_sel");
    commFundfeedFlag("comm_fund_feed_flag");
    commFeedbackType("comm_feedback_type");
    commCancelPeriod("comm_cancel_period");
    commCancelUt("comm_cancel_unbill_type");
    commForeignCode("comm_foreign_code");
    commMchtCname("comm_mcht_cname_sel");
    commMchtEname("comm_mcht_ename_sel");
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
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 "
//              + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "acct_type");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
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
          + sqlCol(wp.colStr(ii, "data_code"), "card_type");
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
    wp.colSet("acct_type_sel_cnt",
        listMktParmData("mkt_parm_data_t", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "3"));
    wp.colSet("merchant_sel_cnt",
        listMktParmData("mkt_parm_data_t", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "4"));
    wp.colSet("mcht_group_sel_cnt",
        listMktParmData("mkt_parm_data_t", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "6"));
    wp.colSet("platform_kind_sel_cnt",
        listMktParmData("mkt_parm_data_t", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "P"));
    wp.colSet("group_card_sel_cnt",
        listMktParmData("mkt_parm_data_t", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktParmData("mkt_parm_data_t", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "2"));
    wp.colSet("mcht_cname_sel_cnt" , listMktParmCData("mkt_parm_cdata_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"A"));
    wp.colSet("mcht_ename_sel_cnt" , listMktParmCData("mkt_parm_cdata_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"B"));
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

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months", "Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctType("comm_acct_type_sel");
    commAcctType("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listMktParmData("mkt_parm_data", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "3"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchamt("comm_merchant_sel");
    commMerchamt("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listMktParmData("mkt_parm_data", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "4"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    mchtGroupS("comm_mcht_group_sel");
    mchtGroupS("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listMktParmData("mkt_parm_data", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "6"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");
    
    if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
        wp.colSet("opt_platform_kind_sel", "Y");
      mchtGroupS("comm_platform_kind_sel");
      mchtGroupS("comm_bef_platform_kind_sel");

      wp.colSet("bef_platform_kind_sel_cnt",
          listMktParmData("mkt_parm_data", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "P"));
      if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
        wp.colSet("opt_platform_kind_sel_cnt", "Y");

    if (!wp.colStr("group_card_sel").equals(wp.colStr("bef_group_card_sel")))
      wp.colSet("opt_group_card_sel", "Y");
    groupCrdS("comm_group_card_sel");
    groupCrdS("comm_bef_group_card_sel");

    wp.colSet("bef_group_card_sel_cnt",
        listMktParmData("mkt_parm_data", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "1"));
    if (!wp.colStr("group_card_sel_cnt").equals(wp.colStr("bef_group_card_sel_cnt")))
      wp.colSet("opt_group_card_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    groupCodeS("comm_group_code_sel");
    groupCodeS("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktParmData("mkt_parm_data", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");
    
    if (!wp.colStr("bl_cond").equals(wp.colStr("bef_bl_cond")))
        wp.colSet("opt_bl_cond","Y");

     if (!wp.colStr("ca_cond").equals(wp.colStr("bef_ca_cond")))
        wp.colSet("opt_ca_cond","Y");

     if (!wp.colStr("id_cond").equals(wp.colStr("bef_id_cond")))
        wp.colSet("opt_id_cond","Y");

     if (!wp.colStr("ao_cond").equals(wp.colStr("bef_ao_cond")))
        wp.colSet("opt_ao_cond","Y");

     if (!wp.colStr("it_cond").equals(wp.colStr("bef_it_cond")))
        wp.colSet("opt_it_cond","Y");

     if (!wp.colStr("ot_cond").equals(wp.colStr("bef_ot_cond")))
        wp.colSet("opt_ot_cond","Y");

     if (!wp.colStr("fund_feed_flag").equals(wp.colStr("bef_fund_feed_flag")))
        wp.colSet("opt_fund_feed_flag","Y");
     commFundfeedFlag("comm_fund_feed_flag");
     commFundfeedFlag("comm_bef_fund_feed_flag");

     if (!wp.colStr("threshold_sel").equals(wp.colStr("bef_threshold_sel")))
        wp.colSet("opt_threshold_sel","Y");
     
     if (!wp.colStr("purchse_type_sel").equals(wp.colStr("bef_purchase_type_sel")))
         wp.colSet("opt_purchase_type_sel","Y");

    if (!wp.colStr("purchase_s_amt_1").equals(wp.colStr("bef_purchase_s_amt_1")))
      wp.colSet("opt_purchase_s_amt_1", "Y");

    if (!wp.colStr("purchase_e_amt_1").equals(wp.colStr("bef_purchase_e_amt_1")))
      wp.colSet("opt_purchase_e_amt_1", "Y");

    if (!wp.colStr("purchase_rate_1").equals(wp.colStr("bef_purchase_rate_1")))
      wp.colSet("opt_purchase_rate_1", "Y");

    if (!wp.colStr("purchase_s_amt_2").equals(wp.colStr("bef_purchase_s_amt_2")))
      wp.colSet("opt_purchase_s_amt_2", "Y");

    if (!wp.colStr("purchase_e_amt_2").equals(wp.colStr("bef_purchase_e_amt_2")))
      wp.colSet("opt_purchase_e_amt_2", "Y");

    if (!wp.colStr("purchase_rate_2").equals(wp.colStr("bef_purchase_rate_2")))
      wp.colSet("opt_purchase_rate_2", "Y");

    if (!wp.colStr("purchase_s_amt_3").equals(wp.colStr("bef_purchase_s_amt_3")))
      wp.colSet("opt_purchase_s_amt_3", "Y");

    if (!wp.colStr("purchase_e_amt_3").equals(wp.colStr("bef_purchase_e_amt_3")))
      wp.colSet("opt_purchase_e_amt_3", "Y");

    if (!wp.colStr("purchase_rate_3").equals(wp.colStr("bef_purchase_rate_3"))) 
      wp.colSet("opt_purchase_rate_3", "Y");

    if (!wp.colStr("purchase_s_amt_4").equals(wp.colStr("bef_purchase_s_amt_4")))
      wp.colSet("opt_purchase_s_amt_4", "Y");

    if (!wp.colStr("purchase_e_amt_4").equals(wp.colStr("bef_purchase_e_amt_4")))
      wp.colSet("opt_purchase_e_amt_4", "Y");

    if (!wp.colStr("purchase_rate_4").equals(wp.colStr("bef_purchase_rate_4")))
      wp.colSet("opt_purchase_rate_4", "Y");

    if (!wp.colStr("purchase_s_amt_5").equals(wp.colStr("bef_purchase_s_amt_5")))
      wp.colSet("opt_purchase_s_amt_5", "Y");

    if (!wp.colStr("purchase_e_amt_5").equals(wp.colStr("bef_purchase_e_amt_5")))
      wp.colSet("opt_purchase_e_amt_5", "Y");

    if (!wp.colStr("purchase_rate_5").equals(wp.colStr("bef_purchase_rate_5")))
      wp.colSet("opt_purchase_rate_5", "Y");
    
    if (!wp.colStr("save_s_amt_1").equals(wp.colStr("bef_save_s_amt_1")))
        wp.colSet("opt_save_s_amt_1", "Y");

      if (!wp.colStr("save_e_amt_1").equals(wp.colStr("bef_save_e_amt_1")))
        wp.colSet("opt_save_e_amt_1", "Y");

      if (!wp.colStr("save_rate_1").equals(wp.colStr("bef_save_rate_1")))
        wp.colSet("opt_save_rate_1", "Y");

          if (!wp.colStr("save_s_amt_2").equals(wp.colStr("bef_save_s_amt_2")))
        wp.colSet("opt_save_s_amt_2", "Y");

      if (!wp.colStr("save_e_amt_2").equals(wp.colStr("bef_save_e_amt_2")))
        wp.colSet("opt_save_e_amt_2", "Y");

      if (!wp.colStr("save_rate_2").equals(wp.colStr("bef_save_rate_2")))
        wp.colSet("opt_save_rate_2", "Y");

          if (!wp.colStr("save_s_amt_3").equals(wp.colStr("bef_save_s_amt_3")))
        wp.colSet("opt_save_s_amt_3", "Y");

      if (!wp.colStr("save_e_amt_3").equals(wp.colStr("bef_save_e_amt_3")))
        wp.colSet("opt_save_e_amt_3", "Y");

      if (!wp.colStr("save_rate_3").equals(wp.colStr("bef_save_rate_3")))
        wp.colSet("opt_save_rate_3", "Y");

          if (!wp.colStr("save_s_amt_4").equals(wp.colStr("bef_save_s_amt_4")))
        wp.colSet("opt_save_s_amt_4", "Y");

      if (!wp.colStr("save_e_amt_4").equals(wp.colStr("bef_save_e_amt_4")))
        wp.colSet("opt_save_e_amt_4", "Y");

      if (!wp.colStr("save_rate_4").equals(wp.colStr("bef_save_rate_4")))
        wp.colSet("opt_save_rate_4", "Y");

      if (!wp.colStr("save_s_amt_5").equals(wp.colStr("bef_save_s_amt_5")))
        wp.colSet("opt_save_s_amt_5", "Y");

      if (!wp.colStr("save_e_amt_5").equals(wp.colStr("bef_save_e_amt_5")))
        wp.colSet("opt_save_e_amt_5", "Y");

      if (!wp.colStr("save_rate_5").equals(wp.colStr("bef_save_rate_5")))
        wp.colSet("opt_save_rate_5", "Y");

    if (!wp.colStr("feedback_lmt").equals(wp.colStr("bef_feedback_lmt")))
      wp.colSet("opt_feedback_lmt", "Y");

    if (!wp.colStr("feedback_type").equals(wp.colStr("bef_feedback_type")))
      wp.colSet("opt_feedback_type", "Y");
    commFeedbackType("comm_feedback_type");
    commFeedbackType("comm_bef_feedback_type");

    if (!wp.colStr("card_feed_run_day").equals(wp.colStr("bef_card_feed_run_day")))
      wp.colSet("opt_card_feed_run_day", "Y");

    if (!wp.colStr("cancel_period").equals(wp.colStr("bef_cancel_period")))
      wp.colSet("opt_cancel_period", "Y");
    commCancelPeriod("comm_cancel_period");
    commCancelPeriod("comm_bef_cancel_period");

    if (!wp.colStr("cancel_s_month").equals(wp.colStr("bef_cancel_s_month")))
      wp.colSet("opt_cancel_s_month", "Y");

    if (!wp.colStr("cancel_unbill_type").equals(wp.colStr("bef_cancel_unbill_type")))
      wp.colSet("opt_cancel_unbill_type", "Y");
    commCancelUt("comm_cancel_unbill_type");
    commCancelUt("comm_bef_cancel_unbill_type");

    if (!wp.colStr("cancel_unbill_rate").equals(wp.colStr("bef_cancel_unbill_rate")))
      wp.colSet("opt_cancel_unbill_rate", "Y");
    
    if (!wp.colStr("cancel_event").equals(wp.colStr("bef_cancel_event")))
        wp.colSet("opt_cancel_event", "Y");
    
    if (!wp.colStr("foreign_code").equals(wp.colStr("bef_foreign_code")))
	     wp.colSet("opt_foreign_code","Y");
    commForeignCode("comm_foreign_code");
    commForeignCode("comm_bef_foreign_code");
    
    if (!wp.colStr("mcht_cname_sel").equals(wp.colStr("bef_mcht_cname_sel")))
        wp.colSet("opt_mcht_cname_sel", "Y");
    commMchtCname("comm_mcht_cname_sel");
    commMchtCname("comm_bef_mcht_cname_sel");

    wp.colSet("bef_mcht_cname_sel_cnt",
    		listMktParmCData("mkt_parm_cdata", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "A"));
    if (!wp.colStr("mcht_cname_sel_cnt").equals(wp.colStr("bef_mcht_cname_sel_cnt")))
        wp.colSet("opt_mcht_cname_sel_cnt", "Y");

    if (!wp.colStr("mcht_ename_sel").equals(wp.colStr("bef_mcht_ename_sel")))
        wp.colSet("opt_mcht_ename_sel", "Y");
    commMchtEname("comm_mcht_ename_sel");
    commMchtEname("comm_bef_mcht_ename_sel");

    wp.colSet("bef_mcht_ename_sel_cnt",
    		listMktParmCData("mkt_parm_cdata", "PTR_COMBO_FUNDP", wp.colStr("fund_code"), "B"));
    if (!wp.colStr("mcht_ename_sel_cnt").equals(wp.colStr("bef_mcht_ename_sel_cnt")))
        wp.colSet("opt_mcht_ename_sel_cnt", "Y");
    
    if (!wp.colStr("atmibwf_cond").equals(wp.colStr("bef_atmibwf_cond")))
        wp.colSet("opt_atmibwf_cond", "Y");
    
    if (!wp.colStr("onlyaddon_calcond").equals(wp.colStr("bef_onlyaddon_calcond")))
        wp.colSet("opt_onlyaddon_calcond", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("fund_name", "");
      wp.colSet("fund_crt_date_s", "");
      wp.colSet("fund_crt_date_e", "");
      wp.colSet("stop_date", "");
      wp.colSet("stop_desc", "");
      wp.colSet("curr_code", "");
      wp.colSet("effect_months", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
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
      wp.colSet("bl_cond","");
      wp.colSet("ca_cond","");
      wp.colSet("id_cond","");
      wp.colSet("ao_cond","");
      wp.colSet("it_cond","");
      wp.colSet("ot_cond","");
      wp.colSet("fund_feed_flag", "");
      wp.colSet("threshold_sel", "");
      wp.colSet("purchase_type_sel", "");
      wp.colSet("purchase_s_amt_1", "");
      wp.colSet("purchase_e_amt_1", "");
      wp.colSet("purchase_rate_1", "");
      wp.colSet("purchase_s_amt_2", "");
      wp.colSet("purchase_e_amt_2", "");
      wp.colSet("purchase_rate_2", "");
      wp.colSet("purchase_s_amt_3", "");
      wp.colSet("purchase_e_amt_3", "");
      wp.colSet("purchase_rate_3", "");
      wp.colSet("purchase_s_amt_4", "");
      wp.colSet("purchase_e_amt_4", "");
      wp.colSet("purchase_rate_4", "");
      wp.colSet("purchase_s_amt_5", "");
      wp.colSet("purchase_e_amt_5", "");
      wp.colSet("purchase_rate_5", "");
      wp.colSet("save_s_amt_1", "");
      wp.colSet("save_e_amt_1", "");
      wp.colSet("save_rate_1", "");
      wp.colSet("save_s_amt_2", "");
      wp.colSet("save_e_amt_2", "");
      wp.colSet("save_rate_2", "");
      wp.colSet("save_s_amt_3", "");
      wp.colSet("save_e_amt_3", "");
      wp.colSet("save_rate_3", "");
      wp.colSet("save_s_amt_4", "");
      wp.colSet("save_e_amt_4", "");
      wp.colSet("save_rate_4", "");
      wp.colSet("save_s_amt_5", "");
      wp.colSet("save_e_amt_5", "");
      wp.colSet("save_rate_5", "");
      wp.colSet("feedback_lmt", "");
      wp.colSet("feedback_type", "");
      wp.colSet("card_feed_run_day", "");
      wp.colSet("cancel_period", "");
      wp.colSet("cancel_s_month", "");
      wp.colSet("cancel_unbill_type", "");
      wp.colSet("cancel_unbill_rate", "");
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

    if (wp.colStr("stop_date").length() == 0)
      wp.colSet("opt_stop_date", "Y");

    if (wp.colStr("stop_desc").length() == 0)
      wp.colSet("opt_stop_desc", "Y");

    if (wp.colStr("effect_months").length() == 0)
      wp.colSet("opt_effect_months", "Y");

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("opt_acct_type_sel", "Y");

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
    
    if (wp.colStr("bl_cond").length()==0)
        wp.colSet("opt_bl_cond","Y");

     if (wp.colStr("ca_cond").length()==0)
        wp.colSet("opt_ca_cond","Y");

     if (wp.colStr("id_cond").length()==0)
        wp.colSet("opt_id_cond","Y");

     if (wp.colStr("ao_cond").length()==0)
        wp.colSet("opt_ao_cond","Y");

     if (wp.colStr("it_cond").length()==0)
        wp.colSet("opt_it_cond","Y");

     if (wp.colStr("ot_cond").length()==0)
        wp.colSet("opt_ot_cond","Y");

     if (wp.colStr("fund_feed_flag").length() == 0)
         wp.colSet("opt_fund_feed_flag", "Y");
   	  
   	if (wp.colStr("threshold_sel").length() == 0)
         wp.colSet("opt_threshold_sel", "Y");

    if (wp.colStr("purchase_type_sel").length() == 0)
         wp.colSet("opt_purchase_type_sel", "Y");


    if (wp.colStr("purchase_s_amt_1").length() == 0)
        wp.colSet("opt_purchase_s_amt_1", "Y");

      if (wp.colStr("purchase_e_amt_1").length() == 0)
        wp.colSet("opt_purchase_e_amt_1", "Y");

      if (wp.colStr("purchase_rate_1").length() == 0)
        wp.colSet("opt_purchase_rate_1", "Y");

      if (wp.colStr("purchase_s_amt_2").length() == 0)
        wp.colSet("opt_purchase_s_amt_2", "Y");

      if (wp.colStr("purchase_e_amt_2").length() == 0)
        wp.colSet("opt_purchase_e_amt_2", "Y");

      if (wp.colStr("purchase_rate_2").length() == 0)
        wp.colSet("opt_purchase_rate_2", "Y");

      if (wp.colStr("purchase_s_amt_3").length() == 0)
        wp.colSet("opt_purchase_s_amt_3", "Y");

      if (wp.colStr("purchase_e_amt_3").length() == 0)
        wp.colSet("opt_purchase_e_amt_3", "Y");

      if (wp.colStr("purchase_rate_3").length() == 0)
        wp.colSet("opt_purchase_rate_3", "Y");

      if (wp.colStr("purchase_s_amt_4").length() == 0)
        wp.colSet("opt_purchase_s_amt_4", "Y");

      if (wp.colStr("purchase_e_amt_4").length() == 0)
        wp.colSet("opt_purchase_e_amt_4", "Y");

      if (wp.colStr("purchase_rate_4").length() == 0)
        wp.colSet("opt_purchase_rate_4", "Y");

      if (wp.colStr("purchase_s_amt_5").length() == 0)
        wp.colSet("opt_purchase_s_amt_5", "Y");

      if (wp.colStr("purchase_e_amt_5").length() == 0)
        wp.colSet("opt_purchase_e_amt_5", "Y");

      if (wp.colStr("purchase_rate_5").length() == 0)
        wp.colSet("opt_purchase_rate_5", "Y");
      
      if (wp.colStr("save_s_amt_1").length() == 0)
          wp.colSet("opt_save_s_amt_1", "Y");

        if (wp.colStr("save_e_amt_1").length() == 0)
          wp.colSet("opt_save_e_amt_1", "Y");

        if (wp.colStr("save_rate_1").length() == 0)
          wp.colSet("opt_save_rate_1", "Y");

        if (wp.colStr("save_s_amt_2").length() == 0)
          wp.colSet("opt_save_s_amt_2", "Y");

        if (wp.colStr("save_e_amt_2").length() == 0)
          wp.colSet("opt_save_e_amt_2", "Y");

        if (wp.colStr("save_rate_2").length() == 0)
          wp.colSet("opt_save_rate_2", "Y");

        if (wp.colStr("save_s_amt_3").length() == 0)
          wp.colSet("opt_save_s_amt_3", "Y");

        if (wp.colStr("save_e_amt_3").length() == 0)
          wp.colSet("opt_save_e_amt_3", "Y");

        if (wp.colStr("save_rate_3").length() == 0)
          wp.colSet("opt_save_rate_3", "Y");

        if (wp.colStr("save_s_amt_4").length() == 0)
          wp.colSet("opt_save_s_amt_4", "Y");

        if (wp.colStr("save_e_amt_4").length() == 0)
          wp.colSet("opt_save_e_amt_4", "Y");

        if (wp.colStr("save_rate_4").length() == 0)
          wp.colSet("opt_save_rate_4", "Y");

        if (wp.colStr("save_s_amt_5").length() == 0)
          wp.colSet("opt_save_s_amt_5", "Y");

        if (wp.colStr("save_e_amt_5").length() == 0)
          wp.colSet("opt_save_e_amt_5", "Y");

        if (wp.colStr("save_rate_5").length() == 0)
          wp.colSet("opt_save_rate_5", "Y");

	    if (wp.colStr("feedback_lmt").length() == 0)
	      wp.colSet("opt_feedback_lmt", "Y");
	
	    if (wp.colStr("feedback_type").length() == 0)
	        wp.colSet("opt_feedback_type", "Y");

	      if (wp.colStr("card_feed_run_day").length() == 0)
	        wp.colSet("opt_card_feed_run_day", "Y");
	
	      if (wp.colStr("cancel_period").length() == 0)
	        wp.colSet("opt_cancel_period", "Y");
	
	      if (wp.colStr("cancel_s_month").length() == 0)
	        wp.colSet("opt_cancel_s_month", "Y");
	
	      if (wp.colStr("cancel_unbill_type").length() == 0)
	        wp.colSet("opt_cancel_unbill_type", "Y");
	
	      if (wp.colStr("cancel_unbill_rate").length() == 0)
	        wp.colSet("opt_cancel_unbill_rate", "Y");
  	  
	  	if (wp.colStr("cancel_event").length() == 0)
	        wp.colSet("opt_cancel_event", "Y");
	  	
		if (wp.colStr("foreign_code").length() == 0)
	        wp.colSet("opt_foreign_code", "Y");
		if (wp.colStr("mcht_cname_sel").length() == 0)
	        wp.colSet("opt_mcht_cname_sel", "Y");
		if (wp.colStr("mcht_ename_sel").length() == 0)
	        wp.colSet("opt_mcht_ename_sel", "Y");
		if (wp.colStr("atmibwf_cond").length() == 0)
	        wp.colSet("opt_atmibwf_cond", "Y");
		if (wp.colStr("onlyaddon_calcond").length() == 0)
	        wp.colSet("opt_onlyaddon_calcond", "Y");
  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp02.Mktp6310Func func = new mktp02.Mktp6310Func(wp);

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
        if (rc == 1)
        	rc = func.dbInsertA4BnCdata();
        if (rc == 1)
            rc = func.dbDeleteD4TBnCdata();
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
      if ((wp.respHtml.equals("mktp6310"))) {
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
  public void commAcctType(String cde1) throws Exception {
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
  
//************************************************************************
public void commForeignCode(String s1) throws Exception 
{
String[] cde = {"1","2","3"};
String[] txt = {"國內刷卡","國外刷卡","國內外刷卡"};
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

//************************************************************************
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

//************************************************************************
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
  
  public void commCancelUt(String cde1) {
	  String[] cde = {"1", "2"};
	    String[] txt = {"當期簽帳款", "全部簽帳款"};
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

  public void commCancelPeriod(String cde1) {
	String[] cde = {"1", "2","3","4"};
    String[] txt = {"每月", "每季	","每半年","每一年"};
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

  public void commFeedbackType(String cde1) {
	String[] cde = {"1", "2"};
    String[] txt = {"每月", "帳單週期	"};
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

  public void commFundfeedFlag(String cde1) {
	String[] cde = {"1", "2"};
    String[] txt = {"消費條件式回饋", "消費條件式回饋與存款餘額"};
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
  String listMktParmCData(String table, String tableName, String dataKey, String dataType) throws Exception {
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
         + " from sec_user a,ptr_combo_fundp_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
}
} // End of class
