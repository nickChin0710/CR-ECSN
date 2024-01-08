/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/21  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *\
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名            
* 110-11-08  V1.00.03  machao     SQL Injection                                
* 112/04/21  V1.00.04  Ryan          增加名單匯入功能 ,增加LIST_COND,LIST_FLAG,LIST_USE_SEL欄位維護                                         *    
***************************************************************************/
package mktp02;

import mktp02.Mktp6250Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6250 extends BaseProc {
  private String PROGNAME = "首刷禮活動回饋參數處理程式108/08/21 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6250Func func = null;
  String rowid;// kk2;
  String activeCode, activeSeq;
  String fstAprFlag = "";
  String orgTabName = "mkt_fstp_parmseq_t";
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
    } else if (eqIgno(wp.buttonCode, "R3"))
    {// 明細查詢 -/
    	strAction = "R3";
        dataReadR3();
    } else if (eqIgno(wp.buttonCode, "R2"))
    {// 明細查詢 -/
    	strAction = "R2";
        dataReadR2();
    }   
    
    else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
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
        + "a.aud_type," + "a.active_code," + "a.active_seq," + "a.active_type," + "a.record_cond,"
        + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by active_code,active_Seq";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commActiveCode("comm_active_code");
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.active_code as active_code," + "a.active_seq as active_seq," + "a.crt_user,"
        + "a.level_seq," + "a.record_cond," + "a.record_group_no," + "a.active_type,"
        + "a.bonus_type," + "a.tax_flag," + "a.fund_code," + "a.group_type," + "a.prog_code,"
        + "a.prog_s_date," + "a.prog_e_date," + "a.gift_no," + "a.spec_gift_no," + "a.per_amt_cond,"
        + "a.per_amt," + "a.perday_cnt_cond," + "a.perday_cnt," + "a.sum_amt_cond," + "a.sum_amt,"
        + "a.sum_cnt_cond," + "a.sum_cnt," + "a.threshold_sel," + "a.purchase_type_sel,"
        + "a.purchase_amt_s1," + "a.purchase_amt_e1," + "a.feedback_amt_1," + "a.purchase_amt_s2,"
        + "a.purchase_amt_e2," + "a.feedback_amt_2," + "a.purchase_amt_s3," + "a.purchase_amt_e3,"
        + "a.feedback_amt_3," + "a.purchase_amt_s4," + "a.purchase_amt_e4," + "a.feedback_amt_4,"
        + "a.purchase_amt_s5," + "a.purchase_amt_e5," + "a.feedback_amt_5," + "a.feedback_limit,"
        + "a.stop_flag,"+ "a.stop_date,"+ "a.stop_desc,"
        + "a.pur_date_sel,"+ "a.purchase_days,"+ "a.merchant_sel,"
        + "a.mcht_group_sel," 
        + "decode(a.list_cond,'','N',a.list_cond) as list_cond,"
        + "a.list_flag,"
        + "decode(a.list_use_sel,'','0',a.list_use_sel) as list_use_sel"
        ;

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(activeCode, "a.active_code") + sqlCol(activeSeq, "a.active_seq");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    
    commMchtGroupsel("comm_mcht_group_sel");
    commMerchantsel("comm_merchant_sel");
    commPdatesel("comm_pur_date_sel");
    commActiveType1("comm_active_type");
    commThresholdSel("comm_threshold_sel");
    commPurchaseTypeSel("comm_purchase_type_sel");
    commActiveCode("comm_active_code");
    commRecordGroupNo("comm_record_group_no");
    commFuncCode("comm_fund_code");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    activeCode = wp.colStr("active_code");
    activeSeq = wp.colStr("active_seq");
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
    controlTabName = "mkt_fstp_parmseq";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.active_code as active_code," + "a.active_seq as active_seq,"
        + "a.crt_user as bef_crt_user," + "a.level_seq as bef_level_seq,"
        + "a.record_cond as bef_record_cond," + "a.record_group_no as bef_record_group_no,"
        + "a.active_type as bef_active_type," + "a.bonus_type as bef_bonus_type,"
        + "a.tax_flag as bef_tax_flag," + "a.fund_code as bef_fund_code,"
        + "a.group_type as bef_group_type," + "a.prog_code as bef_prog_code,"
        + "a.prog_s_date as bef_prog_s_date," + "a.prog_e_date as bef_prog_e_date,"
        + "a.gift_no as bef_gift_no," + "a.spec_gift_no as bef_spec_gift_no,"
        + "a.per_amt_cond as bef_per_amt_cond," + "a.per_amt as bef_per_amt,"
        + "a.perday_cnt_cond as bef_perday_cnt_cond," + "a.perday_cnt as bef_perday_cnt,"
        + "a.sum_amt_cond as bef_sum_amt_cond," + "a.sum_amt as bef_sum_amt,"
        + "a.sum_cnt_cond as bef_sum_cnt_cond," + "a.sum_cnt as bef_sum_cnt,"
        + "a.threshold_sel as bef_threshold_sel," + "a.purchase_type_sel as bef_purchase_type_sel,"
        + "a.purchase_amt_s1 as bef_purchase_amt_s1," + "a.purchase_amt_e1 as bef_purchase_amt_e1,"
        + "a.feedback_amt_1 as bef_feedback_amt_1," + "a.purchase_amt_s2 as bef_purchase_amt_s2,"
        + "a.purchase_amt_e2 as bef_purchase_amt_e2," + "a.feedback_amt_2 as bef_feedback_amt_2,"
        + "a.purchase_amt_s3 as bef_purchase_amt_s3," + "a.purchase_amt_e3 as bef_purchase_amt_e3,"
        + "a.feedback_amt_3 as bef_feedback_amt_3," + "a.purchase_amt_s4 as bef_purchase_amt_s4,"
        + "a.purchase_amt_e4 as bef_purchase_amt_e4," + "a.feedback_amt_4 as bef_feedback_amt_4,"
        + "a.purchase_amt_s5 as bef_purchase_amt_s5," + "a.purchase_amt_e5 as bef_purchase_amt_e5,"
        + "a.feedback_amt_5 as bef_feedback_amt_5," + "a.feedback_limit as bef_feedback_limit,"
        + "a.stop_flag as bef_stop_flag,"+ "a.stop_date as bef_stop_date,"
        + "a.stop_date as bef_stop_date,"
        + "a.stop_desc as bef_stop_desc,"
        + "a.pur_date_sel as bef_pur_date_sel," + "a.purchase_days as bef_purchase_days,"
        + "a.merchant_sel as bef_merchant_sel,"+ "a.mcht_group_sel as bef_mcht_group_sel, "
        + "decode(a.list_cond,'','N',a.list_cond) as bef_list_cond,"
        + "a.list_flag as bef_list_flag,"
        + "decode(a.list_use_sel,'','0',a.list_use_sel) as bef_list_use_sel";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code") + sqlCol(activeSeq, "a.active_seq");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commMchtGroupsel("comm_mcht_group_sel");
    commMerchantsel("comm_merchant_sel");
    commPdatesel("comm_pur_date_sel");
    commActiveCode("comm_active_code");
    commRecordGroupNo("comm_record_group_no");
    commActiveType1("comm_active_type");
    commFuncCode("comm_fund_code");
    commThresholdSel("comm_threshold_sel");
    commPurchaseTypeSel("comm_purchase_type_sel");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    commfuncAudType("aud_type");
    commListFlag("comm_list_flag");
    commListUse("comm_list_use_sel");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
	  wp.colSet("merchant_sel_cnt" , listMktbndata("mkt_bn_data_t","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"7"));
	  wp.colSet("mcht_group_sel_cnt" , listMktbndata("mkt_bn_data_t","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"8"));
	  wp.colSet("list_use_sel_cnt" , 
		        listMktImchannelList("mkt_imfstp_list_t","mkt_imfstp_list_t",wp.colStr("active_code"),wp.colStr("active_seq")));
	  
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("level_seq").equals(wp.colStr("bef_level_seq")))
      wp.colSet("opt_level_seq", "Y");

    if (!wp.colStr("stop_desc").equals(wp.colStr("bef_stop_desc")))
      wp.colSet("opt_stop_desc","Y");
    
    if (!wp.colStr("stop_flag").equals(wp.colStr("bef_stop_flag")))
    {  wp.colSet("opt_stop_flag","Y"); }
    
    if (!wp.colStr("stop_date").equals(wp.colStr("bef_stop_date")))
      wp.colSet("opt_stop_date","Y");    
    
    if (!wp.colStr("pur_date_sel").equals(wp.colStr("bef_pur_date_sel")))
      wp.colSet("opt_pur_date_sel","Y");
    commPdatesel("comm_pur_date_sel");
    commPdatesel("comm_bef_pur_date_sel");    
    
    
    if (!wp.colStr("purchase_days").equals(wp.colStr("bef_purchase_days")))
      wp.colSet("opt_purchase_days","Y");    
  
    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel","Y");
    commMerchantsel("comm_merchant_sel");
    commMerchantsel("comm_bef_merchant_sel");    
    
    wp.colSet("bef_merchant_sel_cnt" , listMktbndata("mkt_bn_data","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"7"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt","Y");    
    
    
    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel","Y");
    commMchtGroupsel("comm_mcht_group_sel");
    commMchtGroupsel("comm_bef_mcht_group_sel");  
    
    
    wp.colSet("bef_mcht_group_sel_cnt" , listMktbndata("mkt_bn_data","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"8"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt","Y");    
    
    if (!wp.colStr("record_cond").equals(wp.colStr("bef_record_cond")))
      wp.colSet("opt_record_cond", "Y");

    if (!wp.colStr("record_group_no").equals(wp.colStr("bef_record_group_no")))
      wp.colSet("opt_record_group_no", "Y");
    commRecordGroupNo("comm_record_group_no");
    commRecordGroupNo("comm_bef_record_group_no", 1);

    if (!wp.colStr("active_type").equals(wp.colStr("bef_active_type")))
      wp.colSet("opt_active_type", "Y");
    commActiveType1("comm_active_type");
    commActiveType1("comm_bef_active_type");

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

    if (!wp.colStr("prog_code").equals(wp.colStr("bef_prog_code")))
      wp.colSet("opt_prog_code", "Y");

    if (!wp.colStr("prog_s_date").equals(wp.colStr("bef_prog_s_date")))
      wp.colSet("opt_prog_s_date", "Y");

    if (!wp.colStr("prog_e_date").equals(wp.colStr("bef_prog_e_date")))
      wp.colSet("opt_prog_e_date", "Y");

    if (!wp.colStr("gift_no").equals(wp.colStr("bef_gift_no")))
      wp.colSet("opt_gift_no", "Y");

    if (!wp.colStr("spec_gift_no").equals(wp.colStr("bef_spec_gift_no")))
      wp.colSet("opt_spec_gift_no", "Y");

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

    if (!wp.colNvl("list_cond","N").equals(wp.colNvl("bef_list_cond","N")))
        wp.colSet("opt_list_cond", "Y");
      
    if (!wp.colStr("list_flag").equals(wp.colStr("bef_list_flag")))
          wp.colSet("opt_list_flag", "Y");
       commListFlag("comm_list_flag");
       commListFlag("comm_bef_list_flag");
        
    if (!wp.colNvl("list_use_sel","0").equals(wp.colNvl("bef_list_use_sel","0")))
         wp.colSet("opt_list_use_sel", "Y");
       commListUse("comm_list_use_sel");
       commListUse("comm_bef_list_use_sel");
    
    wp.colSet("bef_list_use_sel_cnt" , listMktImchannelList("mkt_imfstp_list","mkt_imfstp_list",wp.colStr("active_code"),wp.colStr("active_seq")));
    if (!wp.colStr("list_use_sel_cnt").equals(wp.colStr("bef_list_use_sel_cnt")))
       wp.colSet("opt_list_use_sel_cnt","Y");
    
    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("mcht_group_sel_cnt","");	
      wp.colSet("mcht_group_sel","");	
      wp.colSet("merchant_sel_cnt","");	
      wp.colSet("merchant_sel","");	
      wp.colSet("stop_desc","");	
      wp.colSet("stop_flag","");
      wp.colSet("stop_date","");
      wp.colSet("pur_date_sel","");
      wp.colSet("purchase_days","");
      wp.colSet("level_seq", "");
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
      wp.colSet("list_cond", "N");
      wp.colSet("list_flag", "");
      wp.colSet("list_use_sel", "0");
      wp.colSet("list_use_sel_cnt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
	  
	if (wp.colStr("mcht_group_sel").length()==0)
	  wp.colSet("opt_mcht_group_sel","Y");	  
	
	if (wp.colStr("merchant_sel").length()==0)
      wp.colSet("opt_merchant_sel","Y");
	
	if (wp.colStr("purchase_days").length()==0)
	  wp.colSet("opt_purchase_days","Y");	  
	
	if (wp.colStr("pur_date_sel").length()==0)
	  wp.colSet("opt_pur_date_sel","Y");
	  
	if (wp.colStr("stop_desc").length()==0)
	  wp.colSet("opt_stop_desc","Y");
	  
	if (wp.colStr("stop_date").length()==0)
      wp.colSet("opt_stop_date","Y");  
	  
	if (wp.colStr("stop_flag").length()==0)
	  wp.colSet("opt_stop_flag","Y");	
	
    if (wp.colStr("level_seq").length() == 0)
      wp.colSet("opt_level_seq", "Y");

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
    mktp02.Mktp6250Func func = new mktp02.Mktp6250Func(wp);

    String[] lsActiveCode = wp.itemBuff("active_code");
    String[] lsActiveSeq = wp.itemBuff("active_seq");
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
      func.varsSet("active_seq", lsActiveSeq[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc==1) rc = func.dbInsertA4bndata();
        if (rc==1) rc = func.dbDeleteD4Tbndata();
        if (rc==1) rc = func.dbInsertA4Dmlist();
        if (rc==1) rc = func.dbDeleteD4TDmlist();
      } else if (lsAudType[rr].equals("U"))
      {
        rc = func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4bndata();
        if (rc==1) rc  = func.dbInsertA4bndata();
        if (rc==1) rc = func.dbDeleteD4Tbndata();
        if (rc==1) rc = func.dbDeleteD4Dmlist();
        if (rc==1) rc = func.dbInsertA4Dmlist();
        if (rc==1) rc = func.dbDeleteD4TDmlist();
      } else if (lsAudType[rr].equals("D"))
      { 
    	rc = func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4bndata();
        if (rc==1) rc = func.dbDeleteD4Tbndata();
        if (rc==1) rc = func.dbDeleteD4TDmlist();
        if (rc==1) rc = func.dbDeleteD4Dmlist();
      }
      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commActiveCode("comm_active_code");
        commActiveType("comm_active_type");
        commfuncAudType("aud_type");
        commCrtuser("comm_crt_user");
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
      if ((wp.respHtml.equals("mktp6250"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code", "mkt_fstp_parm", "trim(active_code)", "trim(active_name)",
            " where stop_flag='N'");
      }
      
      if ((wp.respHtml.equals("mktp6250_aaa1")))
      {
       wp.initOption ="";
       wp.optionKey = "";
       this.dddwList("dddw_data_Code34"
              ,"mkt_mcht_gp"
              ,"trim(mcht_group_id)"
              ,"trim(mcht_group_desc)"
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
  public void commActiveCode(String code) throws Exception {
    commActiveCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commActiveCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_fstp_parm "
          + " where 1 = 1 " 
//    	  + " and   active_code = '" + wp.colStr(ii, befStr + "active_code") + "'"
    	  + " and active_code = :active_code ";
      	  setString("active_code",wp.colStr(ii, befStr + "active_code"));
      if (wp.colStr(ii, befStr + "active_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_active_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commRecordGroupNo(String groupNo) throws Exception {
    commRecordGroupNo(groupNo, 0);
    return;
  }

  // ************************************************************************
  public void commRecordGroupNo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " record_group_name as column_record_group_name "
          + " from web_record_group " + " where 1 = 1 " 
//    	  + " and   record_group_no = '"
//          + wp.colStr(ii, befStr + "record_group_no") + "'"
          + " and record_group_no = :record_group_no ";
      	  setString("record_group_no",wp.colStr(ii, befStr + "record_group_no"));
      if (wp.colStr(ii, befStr + "record_group_no").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_record_group_name");
        wp.colSet(ii, columnData1, columnData);
      }
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
//    	  + " and   fund_code = '" + wp.colStr(ii, befStr + "fund_code") + "'"
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
  public void commActiveType1(String cde1) throws Exception {
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
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"紅利", "現金回饋", "豐富點", "特殊贈品"};
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
    String[] cde = {"1", "2","3","4","5"};
    String[] txt = {"1.身分證號", "2.卡號" , "3.一卡通卡號" , "4.悠遊卡號" ,"5.愛金卡號"};
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
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************
//************************************************************************
public void commCrtuser(String s1) throws Exception 
{
	commCrtuser(s1,0);
 return;
}
//************************************************************************
public void commCrtuser(String s1,int bef_type) throws Exception 
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
//           + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
           + " and usr_id = :crt_user "
           ;
      	   setString("crt_user",wp.colStr(ii,befStr+"crt_user"));
      if (wp.colStr(ii,befStr+"crt_user").length()==0)
         {
          wp.colSet(ii, s1, columnData);
          continue;
         }
      sqlSelect(sql1);

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, s1, columnData);
     }
  return;
}

//************************************************************************
public void commPdatesel(String s1) throws Exception 
{
String[] cde = {"1","2"};
String[] txt = {"依原消費期間","發卡日次日起N日"};
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
public void commMerchantsel(String s1) throws Exception 
{
String[] cde = {"0","1","2"};
String[] txt = {"全部","指定","排除"};
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
public void commMchtGroupsel(String s1) throws Exception 
{
String[] cde = {"0","1","2"};
String[] txt = {"全部","指>定","排除"};
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


public String  listMktbndata(String s1,String s2,String s3,String s4) throws Exception
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
String listMktImchannelList(String s1, String s2, String s3, String s4) throws Exception {
  String sql1 = "select "
          + " count(*) as column_data_cnt "
          + " from "
          + s1
          + " "
          + " where  active_code = ? and active_seq = ? ";
  sqlSelect(sql1, new Object[] {
          s3,s4
  });

  if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

  return ("0");
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
           + " and table_name  =  'MKT_FSTP_PARMSEQ' "
             ;
if (wp.respHtml.equals("mktp6250_mrcd"))
   wp.whereStr  += " and data_type  = '7' ";
String whereCnt = wp.whereStr;
whereCnt += " and  data_key = '"+wp.itemStr("active_code")+wp.itemStr("active_seq")+"'";
wp.whereStr  += " and  data_key = :data_key ";
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key",wp.itemStr("active_code")+wp.itemStr("active_seq"));
wp.whereStr  += " order by 4,5,6 ";
int cnt1=selectBndataCount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
    alertMsg("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    return;
   }

pageQuery();
wp.setListCount(1);
wp.notFound = "";

wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
if (wp.respHtml.equals("mktp6250_mrcd"))
	commActiveCode2("comm_active_code");
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
           + " and table_name  =  'MKT_FSTP_PARMSEQ' "
             ;
if (wp.respHtml.equals("mktp6250_aaa1"))
   wp.whereStr  += " and data_type  = '8' ";
String whereCnt = wp.whereStr;
whereCnt += " and  data_key = '"+wp.itemStr("active_code")+wp.itemStr("active_seq")+"'";
wp.whereStr  += " and  data_key = :data_key ";
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key",wp.itemStr("active_code")+wp.itemStr("active_seq"));
wp.whereStr  += " order by 4,5 ";
int cnt1=selectBndataCount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
    alertMsg("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    return;
   }

pageQuery();
wp.setListCount(1);
wp.notFound = "";

wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
if (wp.respHtml.equals("mktp6250_aaa1"))
 commActiveCode2("comm_active_code");
if (wp.respHtml.equals("mktp6250_aaa1"))
 commDataCode34("comm_data_code");
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
public void commActiveCode2(String s1) throws Exception 
{
	commActiveCode2(s1,0);
return;
}
//************************************************************************
public void commActiveCode2(String s1,int bef_type) throws Exception 
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < 1; ii++)
   {
    columnData="";
    sql1 = "select "
         + " active_name as column_active_name "
         + " from mkt_fstp_parm "
         + " where 1 = 1 "
//         + " and   active_code = '"+wp.colStr(ii,befStr+"active_code")+"'"
         + " and active_code = :active_code "
         ;
    	 setString("active_code",wp.colStr(ii,befStr+"active_code"));
    if (wp.colStr(ii,befStr+"active_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_active_name"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}

//************************************************************************
public void commDataCode34(String s1) throws Exception 
{
	commDataCode34(s1,0);
return;
}
//************************************************************************
public void commDataCode34(String s1,int bef_type) throws Exception 
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
         + " and mcht_group_id = :data_code "
         ;
    	 setString("data_code",wp.colStr(ii,befStr+"data_code"));
    if (wp.colStr(ii,befStr+"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcht_group_desc"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}

}  // End of class
