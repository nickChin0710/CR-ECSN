/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/23  V1.00.00   Machao      Initial      
* 112/04/06  V1.00.01   Machao      bug調整                    *
* 112/04/06  V1.00.01   Zuwei Su      刷卡金代碼檢核, 存款餘額將擴長度到 DECIMAL (12,0)  
* 112-05-09  V1.00.03   Ryan    新增國內外消費欄位、ATM手續費回饋加碼欄位，特店中文名稱、特店英文名稱參數維護                            
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *
***************************************************************************/
package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6310 extends BaseEdit {
  private String PROGNAME = "COMBO現金回饋參數檔維護(覆核)程式112/03/23  V1.00.00";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6310Func func = null;
  String rowid;
  String funcCode;
  String fstAprFlag = "";
  String orgTabName = "PTR_COMBO_FUNDP";
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
    } else if (eqIgno(wp.buttonCode, "R5"))
    {// 明細查詢 -/
      strAction = "R5";
      dataReadR5();
    } else if (eqIgno(wp.buttonCode, "U5"))
    {/* 明細更新 */
      strAction = "U5";
      updateFuncU5();
    }else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD2")) {/* 匯入檔案 */
//      procUploadFile(2);
      checkButtonOff();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {/* nothing to do */
        strAction = "";
        switch (wp.itemStr("methodName")) {
            case "fundCodeChange":
                ajaxFundCodeChange();
                break;
            default:
                break;
        }
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_fund_code"), "a.fund_code", "like%")
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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.fund_code," + "a.fund_name," + "a.fund_crt_date_e," + "a.fund_crt_date_s," + "a.stop_date,"
        + "a.crt_date," + "a.apr_user," + "a.apr_date,"+ "a.crt_user ";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.fund_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCrtUser("comm_crt_user");
    commAprUser("comm_apr_user");

    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
	  fstAprFlag= wp.itemStr2("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N")) {
    	controlTabName = orgTabName + "_t";
    }
    rowid = itemKk("data_k1");
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
    
    
    if (wp.itemStr("ex_apr_flag").equals("N")) {
    	wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " 
    		    + " a.aud_type, "
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
    			   	    +  " a.apr_flag, " +  " a.apr_user, " +  " a.crt_date, " +  " a.crt_user, "
    					+  " a.foreign_code,"
    					+  " a.mcht_cname_sel,"
    					+  " '' as mcht_cname_sel_cnt,"
    					+  " a.mcht_ename_sel,"
    					+  " '' as mcht_ename_sel_cnt,"
    					+  " a.atmibwf_cond, "
    					+  " a.onlyaddon_calcond  ";

    }else {
    	wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " 
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
    			   	    +  " a.apr_flag, " +  " a.apr_user, " +  " a.crt_date, " +  " a.crt_user, "
						+  " a.foreign_code,"
						+  " a.mcht_cname_sel,"
						+  " '' as mcht_cname_sel_cnt,"
						+  " a.mcht_ename_sel,"
						+  " '' as mcht_ename_sel_cnt,"
						+  " a.atmibwf_cond, "
						+  " a.onlyaddon_calcond  ";
    }
    
    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(funcCode, "a.fund_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr2("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    checkButtonOff();
    funcCode = wp.colStr("fund_code");
    listWkdata();
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
	        + " a.aud_type as aud_type, " 
	    	+ "a.fund_code as fund_code," + "a.fund_name as fund_name,"
	        + "a.fund_crt_date_s as fund_crt_date_s," + "a.fund_crt_date_e as fund_crt_date_e,"
	        + "a.stop_flag as stop_flag," + "a.stop_date as stop_date," + "a.stop_desc as stop_desc,"
	        + "a.effect_months as effect_months,"
	        + "a.acct_type_sel as acct_type_sel,"
	        + "a.merchant_sel as merchant_sel," + "a.mcht_group_sel as mcht_group_sel," 
	        + "a.platform_kind_sel as platform_kind_sel,"
	        + "a.group_card_sel as group_card_sel," + "a.group_code_sel as group_code_sel,"
	        + "a.mcc_code_sel as mcc_code_sel,"
	        + "a.purchse_s_amt_1 as purchase_s_amt_1," + "a.purchse_e_amt_1 as purchase_e_amt_1,"
	        + "a.purchse_rate_1 as purchase_rate_1," + "a.purchse_s_amt_2 as purchase_s_amt_2,"
	        + "a.purchse_e_amt_2 as purchase_e_amt_2," + "a.purchse_rate_2 as purchase_rate_2,"
	        + "a.purchse_s_amt_3 as purchase_s_amt_3," + "a.purchse_e_amt_3 as purchase_e_amt_3,"
	        + "a.purchse_rate_3 as purchase_rate_3," + "a.purchse_s_amt_4 as purchase_s_amt_4,"
	        + "a.purchse_e_amt_4 as purchase_e_amt_4," + "a.purchse_rate_4 as purchase_rate_4,"
	        + "a.purchse_s_amt_5 as purchase_s_amt_5," + "a.purchse_e_amt_5 as purchase_e_amt_5,"
	        + "a.purchse_rate_5 as purchase_rate_5," 
	        + "a.save_s_amt_1 as save_s_amt_1," +  "a.save_e_amt_1 as save_e_amt_1," +  "a.save_rate_1 as save_rate_1," 
	        + "a.save_s_amt_2 as save_s_amt_2," +  "a.save_e_amt_2 as save_e_amt_2," +  "a.save_rate_2 as save_rate_2," 
	   	    + "a.save_s_amt_3 as save_s_amt_3," +  "a.save_e_amt_3 as save_e_amt_3," +  "a.save_rate_3 as save_rate_3," 
	        + "a.save_s_amt_4 as save_s_amt_4," +  "a.save_e_amt_4 as save_e_amt_4," +  "a.save_rate_4 as save_rate_4," 
	   	    + "a.save_s_amt_5 as save_s_amt_5," +  "a.save_e_amt_5 as save_e_amt_5," +  "a.save_rate_5 as save_rate_5," 
	   	    + "a.feedback_lmt as feedback_lmt,"
	        + "a.feedback_type as feedback_type," + "a.card_feed_run_day as card_feed_run_day,"
	        + "a.cancel_period as cancel_period," + "a.cancel_s_month as cancel_s_month,"
	        + "a.cancel_unbill_type as cancel_unbill_type," + "a.cancel_unbill_rate as cancel_unbill_rate,"
	        + "a.cancel_event as cancel_event," + "a.crt_user as crt_user," + "a.crt_date as crt_date,"
	        + "a.apr_user as apr_user," + "a.apr_date as apr_date,"
	        + "a.group_oppost_cond as group_oppost_cond, "
            + "a.bl_cond as bl_cond,"
            + "a.ca_cond as ca_cond,"
            + "a.id_cond as id_cond,"
            + "a.ao_cond as ao_cond,"
            + "a.it_cond as it_cond,"
            + "a.ot_cond as ot_cond,"
            + "a.foreign_code as foreign_code, "
            + "a.mcht_cname_sel as mcht_cname_sel,"
            + "'' as mcht_cname_sel_cnt,"
            + "a.mcht_ename_sel as mcht_ename_sel,"
            + "'' as mcht_ename_sel_cnt,"
    		+  " a.atmibwf_cond,"
    		+ " a.onlyaddon_calcond  ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(funcCode, "a.fund_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdataAft();
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    funcCode = wp.itemStr("fund_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      funcCode = wp.itemStr("fund_code");
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
    funcCode = wp.itemStr("fund_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      funcCode = wp.itemStr("fund_code");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void dataReadR2() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("fund_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_parm_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_parm_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'PTR_COMBO_FUNDP' ";
    if (wp.respHtml.equals("mktm6310_acty"))
      wp.whereStr += " and data_type  = '3' ";
    if (wp.respHtml.equals("mktm6310_mrch"))
      wp.whereStr += " and data_type  = '4' ";
    if (wp.respHtml.equals("mktm6310_aaa1"))
      wp.whereStr += " and data_type  = '6' ";
    if (wp.respHtml.equals("mktm6310_aaa2"))
        wp.whereStr += " and data_type  = 'P' ";
    if (wp.respHtml.equals("mktm6310_gpcd"))
        wp.whereStr += " and data_type  = '1' ";
    if (wp.respHtml.equals("mktm6310_grcd"))
      wp.whereStr += " and data_type  = '2' ";

    String whereCnt = wp.whereStr;
    whereCnt += sqlCol(wp.itemStr("fund_code"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("fund_code"));
    wp.whereStr += " order by 4,5,6 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm6310_acty"))
    	commAcctType("comm_data_code");
    if (wp.respHtml.equals("mktm6310_mrch"))
    	commDataType2("comm_data_code");
    if (wp.respHtml.equals("mktm6310_aaa1"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6310_aaa2"))
      commDataCode0P("comm_data_code");
    if (wp.respHtml.equals("mktm6310_grcd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6310_gpcd"))
      commDataCode04("comm_data_code");
//    if (wp.respHtml.equals("mktm6310_mccd"))
//    	commDataCode08("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm02.Mktm6310Func func = new mktm02.Mktm6310Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2Flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2Flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm]))) {
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
    dataReadR2();
  }

  // ************************************************************************
  public void dataReadR3() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("fund_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_parm_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_parm_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'PTR_COMBO_FUNDP' ";
    if (wp.respHtml.equals("mktm6310_acty"))
        wp.whereStr += " and data_type  = '3' ";
      if (wp.respHtml.equals("mktm6310_mrch"))
        wp.whereStr += " and data_type  = '4' ";
      if (wp.respHtml.equals("mktm6310_aaa1"))
        wp.whereStr += " and data_type  = '6' ";
      if (wp.respHtml.equals("mktm6310_aaa2"))
          wp.whereStr += " and data_type  = 'P' ";
      if (wp.respHtml.equals("mktm6310_gpcd"))
          wp.whereStr += " and data_type  = '1' ";
      if (wp.respHtml.equals("mktm6310_grcd"))
        wp.whereStr += " and data_type  = '2' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("fund_code") +
    whereCnt += sqlCol(wp.itemStr("fund_code"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("fund_code"));
    wp.whereStr += " order by 4,5,6,7 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm6310_acty"))
    	commAcctType("comm_data_code");
    if (wp.respHtml.equals("mktm6310_mrch"))
    	commDataType2("comm_data_code");
    if (wp.respHtml.equals("mktm6310_aaa1"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6310_aaa2"))
      commDataCode0P("comm_data_code");
    if (wp.respHtml.equals("mktm6310_grcd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6310_gpcd"))
    	commDataCode04("comm_data_code");
//    if (wp.respHtml.equals("mktm6310_mccd"))
//    	commDataCode08("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    mktm02.Mktm6310Func func = new mktm02.Mktm6310Func(wp);
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
    dataReadR3();
  }
//************************************************************************
public void dataReadR5() throws Exception
{
dataReadR5(0);
}
//************************************************************************
public void dataReadR5(int fromType) throws Exception
{
 String bnTable="";

 if ((wp.itemStr("fund_code").length()==0) || (wp.itemStr("aud_type").length() == 0))
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
     bnTable = "MKT_PARM_CDATA";
    }
 else
    {
     wp.colSet("btnUpdate_disable","");
     wp.colSet("newDetail_disable","");
     bnTable = "MKT_PARM_CDATA_T";
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
             + " and table_name  =  'PTR_COMBO_FUNDP' "
             ;
 if (wp.respHtml.equals("mktm6310_namc"))
    wp.whereStr  += " and data_type  = 'A' ";
 if (wp.respHtml.equals("mktm6310_name"))
    wp.whereStr  += " and data_type  = 'B' ";
 String whereCnt = wp.whereStr;
 wp.whereStr  += " and  data_key = :data_key ";
 setString("data_key", wp.itemStr("fund_code"));
 whereCnt += " and  data_key = '"+ wp.itemStr("fund_code") +  "'";
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
//************************************************************************
public void updateFuncU5() throws Exception
{
 mktm02.Mktm6310Func func =new mktm02.Mktm6310Func(wp);
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
    mktm02.Mktm6310Func func = new mktm02.Mktm6310Func(wp);
    if (wp.respHtml.indexOf("_detl") > 0)
        if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();
    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
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
    	if ((wp.respHtml.equals("mktm6310_acty"))) {
            wp.initOption = "";
            wp.optionKey = "";
            this.dddwList("dddw_acct_type", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
                " where 1 = 1 ");
          }
      if ((wp.respHtml.equals("mktm6310_gpcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6310_grcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6310_gncd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6310_aaa1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
            " where 1 = 1 and  platform_flag != '2' ");
      }
      if ((wp.respHtml.equals("mktm6310_aaa2"))) {
          wp.initOption = "";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
              " where 1 = 1 and  platform_flag = '2' ");
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
  public void commDataCode08(String s1) throws Exception 
  {
   String columnData="";
   String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
       {
        columnData="";
        sql1 = "select "
             + " mcc_remark as column_mcc_remark "
             + " from cca_mcc_risk "
             + " where 1 = 1 "
             + " and   mcc_code = '"+wp.colStr(ii,"data_code")+"'"
             ;
        if (wp.colStr(ii,"data_code").length()==0)
           {
            wp.colSet(ii, s1, columnData);
            continue;
           }
        sqlSelect(sql1);

        if (sqlRowNum>0)
           columnData = columnData + sqlStr("column_mcc_remark"); 
        wp.colSet(ii, s1, columnData);
       }
    return;
  }
//************************************************************************
public void commAcctType(String s1) throws Exception 
{
 String columnData="";
 String sql1 = "";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " chin_name as column_chin_name "
           + " from ptr_acct_type "
           + " where 1 = 1 "
           + " and   acct_type = '"+wp.colStr(ii,"data_code")+"'"
           ;
      if (wp.colStr(ii,"data_code").length()==0)
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
//************************************************************************
public void commDataType2(String s1) throws Exception 
{
String columnData="";
String sql1 = "";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " ica_desc as column_ica_desc "
         + " from mkt_rcv_bin "
         + " where 1 = 1 "
         + " and   bank_no = '"+wp.colStr(ii,"data_code2")+"'"
         + " and   bank_no != '' "
         ;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_ica_desc"); 
    wp.colSet(ii, s1, columnData);
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
              + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
//          + sqlCol(wp.colStr(ii, "data_code"), "group_code");
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
//  public void procUploadFile(int loadType) throws Exception {
//    if (wp.colStr(0, "ser_num").length() > 0)
//      wp.listCount[0] = wp.itemBuff("ser_num").length;
//    if (itemIsempty("zz_file_name")) {
//      alertErr2("上傳檔名: 不可空白");
//      return;
//    }
//
//    if (loadType == 2)
//      fileDataImp2();
//  }
//
//  // ************************************************************************
//  int fileUpLoad() {
//    TarokoUpload func = new TarokoUpload();
//    try {
//      func.actionFunction(wp);
//      wp.colSet("zz_file_name", func.fileName);
//    } catch (Exception ex) {
//      wp.log("file_upLoad: error=" + ex.getMessage());
//      return -1;
//    }
//
//    return func.rc;
//  }
//
//  // ************************************************************************
//  void fileDataImp2() throws Exception {
//    TarokoFileAccess tf = new TarokoFileAccess(wp);
//
//    String inputFile = wp.itemStr("zz_file_name");
//    int fi = tf.openInputText(inputFile, "MS950");
//
//    if (fi == -1)
//      return;
//
//    String sysUploadType = wp.itemStr("sys_upload_type");
//    String sysUploadAlias = wp.itemStr("sys_upload_alias");
//
//    if (sysUploadAlias.equals("aaa1")) {
//      // if has pre check procudure, write in here
//    }
//    mktm02.Mktm6310Func func = new mktm02.Mktm6310Func(wp);
//
//    if (sysUploadAlias.equals("aaa1"))
//      func.dbDeleteD2Aaa1("MKT_PARM_DATA_T");
//
//    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
//    comr.setConn(wp);
//    batchNo = comr.getSeqno("MKT_MODSEQ");
//
//    String tmpStr = "";
//    int llOk = 0, llCnt = 0, llErr = 0;
//    int lineCnt = 0;
//    while (true) {
//      tmpStr = tf.readTextFile(fi);
//      if (tf.endFile[fi].equals("Y"))
//        break;
//      lineCnt++;
//      if (sysUploadAlias.equals("aaa1")) {
//        if (lineCnt <= 0)
//          continue;
//        if (tmpStr.length() < 2)
//          continue;
//      }
//
//      llCnt++;
//
//      for (int inti = 0; inti < 10; inti++)
//        logMsg[inti] = "";
//      logMsg[10] = String.format("%02d", lineCnt);
//
//      if (sysUploadAlias.equals("aaa1"))
//        if (checkUploadfileAaa1(tmpStr) != 0)
//          continue;
//
//      if (errorCnt == 0) {
//        if (sysUploadAlias.equals("aaa1")) {
//          if (func.dbInsertI2Aaa1("MKT_PARM_DATA_T", uploadFileCol, uploadFileDat) == 1)
//            llOk++;
//          else
//            llErr++;
//        }
//      }
//    }
//
//    if (errorCnt > 0) {
//      if (sysUploadAlias.equals("aaa1"))
//        func.dbDeleteD2Aaa1("MKT_PARM_DATA_T");
//      func.dbInsertEcsNotifyLog(batchNo, errorCnt);
//    }
//
//    sqlCommit(1); // 1:commit else rollback
//
//    alertMsg("資料匯入處理筆數 : " + llCnt + ", 成功(" + llOk + "), 重複(" + llErr + "), 失敗("
//        + (llCnt - llOk - llErr) + ")");
//
//    tf.closeInputText(fi);
//    tf.deleteFile(inputFile);
//
//
//    return;
//  }
//
//  // ************************************************************************
//  int checkUploadfileAaa1(String tmpStr) throws Exception {
//    mktm02.Mktm6310Func func = new mktm02.Mktm6310Func(wp);
//
//    for (int inti = 0; inti < 50; inti++) {
//      uploadFileCol[inti] = "";
//      uploadFileDat[inti] = "";
//    }
//    // =========== [M]edia layout =============
//    uploadFileCol[0] = "data_code";
//    uploadFileCol[1] = "data_code2";
//
//    // ======== [I]nsert table column ========
//    uploadFileCol[2] = "table_name";
//    uploadFileCol[3] = "data_key";
//    uploadFileCol[4] = "data_type";
//    uploadFileCol[5] = "crt_date";
//    uploadFileCol[6] = "crt_user";
//
//    // ==== insert table content default =====
//    uploadFileDat[2] = "PTR_COMBO_FUNDP";
//    uploadFileDat[3] = wp.itemStr("fund_code");
//    uploadFileDat[4] = "4";
//    uploadFileDat[5] = wp.sysDate;
//    uploadFileDat[6] = wp.loginUser;
//
//    int okFlag = 0;
//    int errFlag = 0;
//    int[] begPos = {1};
//
//    for (int inti = 0; inti < 2; inti++) {
//      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
//      if (uploadFileDat[inti].length() != 0)
//        okFlag = 1;
//    }
//    if (okFlag == 0)
//      return (1);
//    // ******************************************************************
//    if ((uploadFileDat[1].length() != 0) && (uploadFileDat[1].length() < 8))
//
//      if (uploadFileDat[1].length() != 0)
//        uploadFileDat[1] =
//            "00000000".substring(0, 8 - uploadFileDat[1].length()) + uploadFileDat[1];
//
//
//    return 0;
//  }
//
  // ************************************************************************
  public void checkButtonOff() throws Exception {

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("acct_type_sel", "0");

    if (wp.colStr("acct_type_sel").equals("0")) {
      buttonOff("btnacty_disable");
    } else {
      wp.colSet("btnacty_disable", "");
    }

    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("merchant_sel", "0");

    if (wp.colStr("merchant_sel").equals("0")) {
      buttonOff("btnmrch_disable");
      buttonOff("uplaaa1_disable");
    } else {
      wp.colSet("btnmrch_disable", "");
      wp.colSet("uplaaa1_disable", "");
    }

    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("mcht_group_sel", "0");

    if (wp.colStr("mcht_group_sel").equals("0")) {
      buttonOff("btnaaa1_disable");
    } else {
      wp.colSet("btnaaa1_disable", "");
    }
    
    if (wp.colStr("platform_kind_sel").length() == 0)
        wp.colSet("platform_kind_sel", "0");

      if (wp.colStr("platform_kind_sel").equals("0")) {
        buttonOff("btnaaa2_disable");
      } else {
        wp.colSet("btnaaa2_disable", "");
      }

    if (wp.colStr("group_card_sel").length() == 0)
      wp.colSet("group_card_sel", "0");

    if (wp.colStr("group_card_sel").equals("0")) {
      buttonOff("btngpcd_disable");
    } else {
      wp.colSet("btngpcd_disable", "");
    }

    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("group_code_sel", "0");

    if (wp.colStr("group_code_sel").equals("0")) {
      buttonOff("btngrcd_disable");
    } else {
      wp.colSet("btngrcd_disable", "");
    }

//    if (wp.colStr("mcc_code_sel").length() == 0)
//      wp.colSet("mcc_code_sel", "0");
//
//    if (wp.colStr("mcc_code_sel").equals("0")) {
//      buttonOff("btnmccd_disable");
//    } else {
//      wp.colSet("btnmccd_disable", "");
//    }

    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplaaa1_disable");
    } else {
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnacty_disable");
    buttonOff("btnmrch_disable");
    buttonOff("btnaaa1_disable");
    buttonOff("btnaaa2_disable");
    buttonOff("btngpcd_disable");
    buttonOff("btngrcd_disable");
    buttonOff("btnmccd_disable");
    wp.colSet("cancel_unbill_rate", 100);
    return;
  }
//************************************************************************
void listWkdataAft() throws Exception
{
 wp.colSet("acct_type_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"3"));
 wp.colSet("merchant_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"4"));
 wp.colSet("mcht_group_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"6"));
 wp.colSet("platform_kind_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"P"));
 wp.colSet("group_code_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"2"));
 wp.colSet("group_card_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"1"));
// wp.colSet("mcc_code_sel_cnt" , listMktParmData("mkt_parm_data_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"8"));
 wp.colSet("mcht_cname_sel_cnt" , listMktBnCdata("mkt_parm_cdata_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"A"));
 wp.colSet("mcht_ename_sel_cnt" , listMktBnCdata("mkt_parm_cdata_t","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"B"));
}
//************************************************************************
void listWkdata() throws Exception
{
 wp.colSet("acct_type_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"3"));
 wp.colSet("merchant_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"4"));
 wp.colSet("mcht_group_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"6"));
 wp.colSet("platform_kind_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"P"));
 wp.colSet("group_code_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"2"));
 wp.colSet("group_card_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"1"));
// wp.colSet("mcc_code_sel_cnt" , listMktParmData("mkt_parm_data","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"8"));
 wp.colSet("mcht_cname_sel_cnt" , listMktBnCdata("mkt_parm_cdata","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"A"));
 wp.colSet("mcht_ename_sel_cnt" , listMktBnCdata("mkt_parm_cdata","PTR_COMBO_FUNDP",wp.colStr("fund_code"),"B"));
}
//************************************************************************
String listMktParmData(String s1, String s2, String s3, String s4) throws Exception
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
public void commCrtUser(String columnData1) throws Exception
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
           + " and   usr_id = :crt_user "
           ;
      setString("crt_user",wp.colStr(ii,"crt_user"));
      if (wp.colStr(ii,"crt_user").length()==0)
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
public void commAprUser(String columnData1) throws Exception
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
           + " and   usr_id = :apr_user "
           ;
      setString("apr_user",wp.colStr(ii,"apr_user"));
      if (wp.colStr(ii,"apr_user").length()==0)
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
    int ajaxFundCodeChange() throws Exception {
        String fundCode = wp.itemStr("ax_win_fund_code");
        if (fundCode.length() < 4) {
            alertErr("刷卡金代碼長度至少4碼!");
            return 1;
        }
    
        wp.sqlCmd = " select "
                + " a.payment_type as payment_type "
                + " from ptr_payment a "
                + " where a.payment_type = substr('"
                + fundCode
                + "',1,4) ";
    
        this.sqlSelect();
        if (sqlRowNum <= 0) {
            alertErr("刷卡金前4碼在ptrm0030繳款類別參數查無資料!");
            wp.addJSON("payment_type","");
            return 1;
        }
        wp.addJSON("payment_type",sqlStr("payment_type"));
    
        return 0;
    }
}  // End of class
