/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 2019/11/18 V1.00.01  Ru Chen        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名  
* 110-11-16  V1.00.03   machao     SQL Injection                                           
* 112-04-19  V1.00.04   Ryan          增加權限訊息                                                                             *   
***************************************************************************/
package mktp02;

import mktp02.Mktp1120Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1120 extends BaseProc {
  private String PROGNAME = "新貴通/龍騰卡貴賓室申請參數覆核2023/04/19 V1.00.04";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp1120Func func = null;
  String rowid;// kk2;
  String groupCode, cardType;
  String fstAprFlag = "";
  String orgTabName = "mkt_ppcard_apply_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, reCnt = 0, notifyCnt = 0, colNum = 0;
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%")
        + sqlCol(wp.itemStr("ex_bin_type"), "a.bin_type", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
        + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%")
        + sqlCol(wp.itemStr("ex_vip_kind"), "a.vip_kind", "like%")
        + sqlCol(wp.itemStr("ex_vip_group_code"), "a.vip_group_code", "like%");

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
        + "a.aud_type," + "a.card_type," + "a.group_code," + "a.proj_desc," + "a.bin_type,"
        + "a.major_flag," + "a.first_cond," + "a.last_amt_cond," + "a.nofir_cond," + "a.vip_kind,"
        + "a.vip_group_code," + "a.crt_user," + "a.crt_date," + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.card_type,a.bin_type,a.group_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCardType("comm_card_type");
    commGroupCode("comm_group_code");

    commVmjCode("comm_bin_type");
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
        + "a.bin_type," + "a.proj_desc," + "a.end_date," + "a.card_purch_code," + "a.run_mm_flag01,"
        + "a.run_mm_flag02," + "a.run_mm_flag03," + "a.run_mm_flag04," + "a.run_mm_flag05,"
        + "a.run_mm_flag06," + "a.run_mm_flag07," + "a.run_mm_flag08," + "a.run_mm_flag09,"
        + "a.run_mm_flag10," + "a.run_mm_flag11," + "a.run_mm_flag12," + "a.use_cnt_cond,"
        + "a.use_max_cnt," + "a.first_cond," + "a.fir_purch_mm," + "a.fir_item_ename_bl,"
        + "a.fir_item_ename_ca," + "a.fir_item_ename_it," + "a.fir_it_type,"
        + "a.fir_item_ename_id," + "a.fir_item_ename_ao," + "a.fir_item_ename_ot,"
        + "a.fir_min_amt," + "a.fir_amt_cond," + "a.fir_tot_amt," + "a.fir_cnt_cond,"
        + "a.fir_tot_cnt," + "a.fir_free_use_cnt," + "a.fir_spec_mcc_flag,"
        + "a.fir_spec_mcht_flag," + "a.fir_spec_cond," + "a.fir_spec_purch_mm,"
        + "a.fir_spec_amt_cond1," + "a.fir_spec_tot_amt1," + "a.fir_spec_amt_cond2,"
        + "a.fir_spec_tot_amt2," + "a.fir_spec_use_cnt," + "a.item_ename_bl," + "a.item_ename_ca,"
        + "a.item_ename_it," + "a.it_type," + "a.item_ename_id," + "a.item_ename_ao,"
        + "a.item_ename_ot," + "a.last_amt_cond," + "a.last_tot_amt," + "a.no_first_cond,"
        + "a.purch_mm," + "a.min_amt," + "a.amt_cond," + "a.tot_amt," + "a.cnt_cond," + "a.tot_cnt,"
        + "a.free_use_cnt," + "a.spec_cond," + "a.spec_purch_mm," + "a.spec_amt_cond1,"
        + "a.spec_tot_amt1," + "a.spec_amt_cond2," + "a.spec_tot_amt2," + "a.spec_use_cnt,"
        + "a.spec_mcc_flag," + "a.spec_mcht_flag";

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
    commPurcCode("comm_card_purch_code");
    commCntCond("comm_use_cnt_cond");
    commItType1("comm_fir_it_type");
    commMccCode1("comm_fir_spec_mcc_flag");
    commMcht1("comm_fir_spec_mcht_flag");
    commItType2("comm_it_type");
    commMccCode2("comm_spec_mcc_flag");
    commMcht2("comm_spec_mcht_flag");
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    commBinType("comm_bin_type");
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
    controlTabName = "mkt_ppcard_free";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.group_code as group_code," + "a.card_type as card_type,"
        + "a.crt_user as bef_crt_user," + "a.bin_type as bef_bin_type,"
        + "a.proj_desc as bef_proj_desc," + "a.end_date as bef_end_date,"
        + "a.card_purch_code as bef_card_purch_code," + "a.run_mm_flag01 as bef_run_mm_flag01,"
        + "a.run_mm_flag02 as bef_run_mm_flag02," + "a.run_mm_flag03 as bef_run_mm_flag03,"
        + "a.run_mm_flag04 as bef_run_mm_flag04," + "a.run_mm_flag05 as bef_run_mm_flag05,"
        + "a.run_mm_flag06 as bef_run_mm_flag06," + "a.run_mm_flag07 as bef_run_mm_flag07,"
        + "a.run_mm_flag08 as bef_run_mm_flag08," + "a.run_mm_flag09 as bef_run_mm_flag09,"
        + "a.run_mm_flag10 as bef_run_mm_flag10," + "a.run_mm_flag11 as bef_run_mm_flag11,"
        + "a.run_mm_flag12 as bef_run_mm_flag12," + "a.use_cnt_cond as bef_use_cnt_cond,"
        + "a.use_max_cnt as bef_use_max_cnt," + "a.first_cond as bef_first_cond,"
        + "a.fir_purch_mm as bef_fir_purch_mm," + "a.fir_item_ename_bl as bef_fir_item_ename_bl,"
        + "a.fir_item_ename_ca as bef_fir_item_ename_ca,"
        + "a.fir_item_ename_it as bef_fir_item_ename_it," + "a.fir_it_type as bef_fir_it_type,"
        + "a.fir_item_ename_id as bef_fir_item_ename_id,"
        + "a.fir_item_ename_ao as bef_fir_item_ename_ao,"
        + "a.fir_item_ename_ot as bef_fir_item_ename_ot," + "a.fir_min_amt as bef_fir_min_amt,"
        + "a.fir_amt_cond as bef_fir_amt_cond," + "a.fir_tot_amt as bef_fir_tot_amt,"
        + "a.fir_cnt_cond as bef_fir_cnt_cond," + "a.fir_tot_cnt as bef_fir_tot_cnt,"
        + "a.fir_free_use_cnt as bef_fir_free_use_cnt,"
        + "a.fir_spec_mcc_flag as bef_fir_spec_mcc_flag,"
        + "a.fir_spec_mcht_flag as bef_fir_spec_mcht_flag,"
        + "a.fir_spec_cond as bef_fir_spec_cond," + "a.fir_spec_purch_mm as bef_fir_spec_purch_mm,"
        + "a.fir_spec_amt_cond1 as bef_fir_spec_amt_cond1,"
        + "a.fir_spec_tot_amt1 as bef_fir_spec_tot_amt1,"
        + "a.fir_spec_amt_cond2 as bef_fir_spec_amt_cond2,"
        + "a.fir_spec_tot_amt2 as bef_fir_spec_tot_amt2,"
        + "a.fir_spec_use_cnt as bef_fir_spec_use_cnt," + "a.item_ename_bl as bef_item_ename_bl,"
        + "a.item_ename_ca as bef_item_ename_ca," + "a.item_ename_it as bef_item_ename_it,"
        + "a.it_type as bef_it_type," + "a.item_ename_id as bef_item_ename_id,"
        + "a.item_ename_ao as bef_item_ename_ao," + "a.item_ename_ot as bef_item_ename_ot,"
        + "a.last_amt_cond as bef_last_amt_cond," + "a.last_tot_amt as bef_last_tot_amt,"
        + "a.no_first_cond as bef_no_first_cond," + "a.purch_mm as bef_purch_mm,"
        + "a.min_amt as bef_min_amt," + "a.amt_cond as bef_amt_cond," + "a.tot_amt as bef_tot_amt,"
        + "a.cnt_cond as bef_cnt_cond," + "a.tot_cnt as bef_tot_cnt,"
        + "a.free_use_cnt as bef_free_use_cnt," + "a.spec_cond as bef_spec_cond,"
        + "a.spec_purch_mm as bef_spec_purch_mm," + "a.spec_amt_cond1 as bef_spec_amt_cond1,"
        + "a.spec_tot_amt1 as bef_spec_tot_amt1," + "a.spec_amt_cond2 as bef_spec_amt_cond2,"
        + "a.spec_tot_amt2 as bef_spec_tot_amt2," + "a.spec_use_cnt as bef_spec_use_cnt,"
        + "a.spec_mcc_flag as bef_spec_mcc_flag," + "a.spec_mcht_flag as bef_spec_mcht_flag";

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
    commBinType("comm_bin_type");
    commPurcCode("comm_card_purch_code");
    commCntCond("comm_use_cnt_cond");
    commItType1("comm_fir_it_type");
    commMccCode1("comm_fir_spec_mcc_flag");
    commMcht1("comm_fir_spec_mcht_flag");
    commItType2("comm_it_type");
    commMccCode2("comm_spec_mcc_flag");
    commMcht2("comm_spec_mcht_flag");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("fir_spec_mcc_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl_t", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "1"));
    wp.colSet("fir_spec_mcht_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl_t", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "2"));
    wp.colSet("mch1_cnt", listMktPpcardDtl("mkt_ppcard_dtl_t", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "1"));
    wp.colSet("spec_mcc_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl_t", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "3"));
    wp.colSet("spec_mcht_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl_t", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "4"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("proj_desc").equals(wp.colStr("bef_proj_desc")))
      wp.colSet("opt_proj_desc", "Y");

    if (!wp.colStr("end_date").equals(wp.colStr("bef_end_date")))
      wp.colSet("opt_end_date", "Y");

    if (!wp.colStr("card_purch_code").equals(wp.colStr("bef_card_purch_code")))
      wp.colSet("opt_card_purch_code", "Y");
    commPurcCode("comm_card_purch_code");
    commPurcCode("comm_bef_card_purch_code");

    if (!wp.colStr("run_mm_flag01").equals(wp.colStr("bef_run_mm_flag01")))
      wp.colSet("opt_run_mm_flag01", "Y");

    if (!wp.colStr("run_mm_flag02").equals(wp.colStr("bef_run_mm_flag02")))
      wp.colSet("opt_run_mm_flag02", "Y");

    if (!wp.colStr("run_mm_flag03").equals(wp.colStr("bef_run_mm_flag03")))
      wp.colSet("opt_run_mm_flag03", "Y");

    if (!wp.colStr("run_mm_flag04").equals(wp.colStr("bef_run_mm_flag04")))
      wp.colSet("opt_run_mm_flag04", "Y");

    if (!wp.colStr("run_mm_flag05").equals(wp.colStr("bef_run_mm_flag05")))
      wp.colSet("opt_run_mm_flag05", "Y");

    if (!wp.colStr("run_mm_flag06").equals(wp.colStr("bef_run_mm_flag06")))
      wp.colSet("opt_run_mm_flag06", "Y");

    if (!wp.colStr("run_mm_flag07").equals(wp.colStr("bef_run_mm_flag07")))
      wp.colSet("opt_run_mm_flag07", "Y");

    if (!wp.colStr("run_mm_flag08").equals(wp.colStr("bef_run_mm_flag08")))
      wp.colSet("opt_run_mm_flag08", "Y");

    if (!wp.colStr("run_mm_flag09").equals(wp.colStr("bef_run_mm_flag09")))
      wp.colSet("opt_run_mm_flag09", "Y");

    if (!wp.colStr("run_mm_flag10").equals(wp.colStr("bef_run_mm_flag10")))
      wp.colSet("opt_run_mm_flag10", "Y");

    if (!wp.colStr("run_mm_flag11").equals(wp.colStr("bef_run_mm_flag11")))
      wp.colSet("opt_run_mm_flag11", "Y");

    if (!wp.colStr("run_mm_flag12").equals(wp.colStr("bef_run_mm_flag12")))
      wp.colSet("opt_run_mm_flag12", "Y");

    if (!wp.colStr("use_cnt_cond").equals(wp.colStr("bef_use_cnt_cond")))
      wp.colSet("opt_use_cnt_cond", "Y");
    commCntCond("comm_use_cnt_cond");
    commCntCond("comm_bef_use_cnt_cond");

    if (!wp.colStr("use_max_cnt").equals(wp.colStr("bef_use_max_cnt")))
      wp.colSet("opt_use_max_cnt", "Y");

    if (!wp.colStr("first_cond").equals(wp.colStr("bef_first_cond")))
      wp.colSet("opt_first_cond", "Y");

    if (!wp.colStr("fir_purch_mm").equals(wp.colStr("bef_fir_purch_mm")))
      wp.colSet("opt_fir_purch_mm", "Y");

    if (!wp.colStr("fir_item_ename_bl").equals(wp.colStr("bef_fir_item_ename_bl")))
      wp.colSet("opt_fir_item_ename_bl", "Y");

    if (!wp.colStr("fir_item_ename_ca").equals(wp.colStr("bef_fir_item_ename_ca")))
      wp.colSet("opt_fir_item_ename_ca", "Y");

    if (!wp.colStr("fir_item_ename_it").equals(wp.colStr("bef_fir_item_ename_it")))
      wp.colSet("opt_fir_item_ename_it", "Y");

    if (!wp.colStr("fir_it_type").equals(wp.colStr("bef_fir_it_type")))
      wp.colSet("opt_fir_it_type", "Y");
    commItType1("comm_fir_it_type");
    commItType1("comm_bef_fir_it_type");

    if (!wp.colStr("fir_item_ename_id").equals(wp.colStr("bef_fir_item_ename_id")))
      wp.colSet("opt_fir_item_ename_id", "Y");

    if (!wp.colStr("fir_item_ename_ao").equals(wp.colStr("bef_fir_item_ename_ao")))
      wp.colSet("opt_fir_item_ename_ao", "Y");

    if (!wp.colStr("fir_item_ename_ot").equals(wp.colStr("bef_fir_item_ename_ot")))
      wp.colSet("opt_fir_item_ename_ot", "Y");

    if (!wp.colStr("fir_min_amt").equals(wp.colStr("bef_fir_min_amt")))
      wp.colSet("opt_fir_min_amt", "Y");

    if (!wp.colStr("fir_amt_cond").equals(wp.colStr("bef_fir_amt_cond")))
      wp.colSet("opt_fir_amt_cond", "Y");

    if (!wp.colStr("fir_tot_amt").equals(wp.colStr("bef_fir_tot_amt")))
      wp.colSet("opt_fir_tot_amt", "Y");

    if (!wp.colStr("fir_cnt_cond").equals(wp.colStr("bef_fir_cnt_cond")))
      wp.colSet("opt_fir_cnt_cond", "Y");

    if (!wp.colStr("fir_tot_cnt").equals(wp.colStr("bef_fir_tot_cnt")))
      wp.colSet("opt_fir_tot_cnt", "Y");

    if (!wp.colStr("fir_free_use_cnt").equals(wp.colStr("bef_fir_free_use_cnt")))
      wp.colSet("opt_fir_free_use_cnt", "Y");

    if (!wp.colStr("fir_spec_mcc_flag").equals(wp.colStr("bef_fir_spec_mcc_flag")))
      wp.colSet("opt_fir_spec_mcc_flag", "Y");
    commMccCode1("comm_fir_spec_mcc_flag");
    commMccCode1("comm_bef_fir_spec_mcc_flag");

    wp.colSet("bef_fir_spec_mcc_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "1"));
    if (!wp.colStr("fir_spec_mcc_flag_cnt").equals(wp.colStr("bef_fir_spec_mcc_flag_cnt")))
      wp.colSet("opt_fir_spec_mcc_flag_cnt", "Y");

    if (!wp.colStr("fir_spec_mcht_flag").equals(wp.colStr("bef_fir_spec_mcht_flag")))
      wp.colSet("opt_fir_spec_mcht_flag", "Y");
    commMcht1("comm_fir_spec_mcht_flag");
    commMcht1("comm_bef_fir_spec_mcht_flag");

    wp.colSet("bef_fir_spec_mcht_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "2"));
    if (!wp.colStr("fir_spec_mcht_flag_cnt").equals(wp.colStr("bef_fir_spec_mcht_flag_cnt")))
      wp.colSet("opt_fir_spec_mcht_flag_cnt", "Y");

    if (!wp.colStr("fir_spec_cond").equals(wp.colStr("bef_fir_spec_cond")))
      wp.colSet("opt_fir_spec_cond", "Y");

    if (!wp.colStr("fir_spec_purch_mm").equals(wp.colStr("bef_fir_spec_purch_mm")))
      wp.colSet("opt_fir_spec_purch_mm", "Y");

    if (!wp.colStr("fir_spec_amt_cond1").equals(wp.colStr("bef_fir_spec_amt_cond1")))
      wp.colSet("opt_fir_spec_amt_cond1", "Y");

    if (!wp.colStr("fir_spec_tot_amt1").equals(wp.colStr("bef_fir_spec_tot_amt1")))
      wp.colSet("opt_fir_spec_tot_amt1", "Y");

    if (!wp.colStr("fir_spec_amt_cond2").equals(wp.colStr("bef_fir_spec_amt_cond2")))
      wp.colSet("opt_fir_spec_amt_cond2", "Y");

    if (!wp.colStr("fir_spec_tot_amt2").equals(wp.colStr("bef_fir_spec_tot_amt2")))
      wp.colSet("opt_fir_spec_tot_amt2", "Y");

    if (!wp.colStr("fir_spec_use_cnt").equals(wp.colStr("bef_fir_spec_use_cnt")))
      wp.colSet("opt_fir_spec_use_cnt", "Y");

    if (!wp.colStr("item_ename_bl").equals(wp.colStr("bef_item_ename_bl")))
      wp.colSet("opt_item_ename_bl", "Y");

    if (!wp.colStr("item_ename_ca").equals(wp.colStr("bef_item_ename_ca")))
      wp.colSet("opt_item_ename_ca", "Y");

    if (!wp.colStr("item_ename_it").equals(wp.colStr("bef_item_ename_it")))
      wp.colSet("opt_item_ename_it", "Y");

    if (!wp.colStr("it_type").equals(wp.colStr("bef_it_type")))
      wp.colSet("opt_it_type", "Y");
    commItType2("comm_it_type");
    commItType2("comm_bef_it_type");

    if (!wp.colStr("item_ename_id").equals(wp.colStr("bef_item_ename_id")))
      wp.colSet("opt_item_ename_id", "Y");

    if (!wp.colStr("item_ename_ao").equals(wp.colStr("bef_item_ename_ao")))
      wp.colSet("opt_item_ename_ao", "Y");

    if (!wp.colStr("item_ename_ot").equals(wp.colStr("bef_item_ename_ot")))
      wp.colSet("opt_item_ename_ot", "Y");

    if (!wp.colStr("last_amt_cond").equals(wp.colStr("bef_last_amt_cond")))
      wp.colSet("opt_last_amt_cond", "Y");

    if (!wp.colStr("last_tot_amt").equals(wp.colStr("bef_last_tot_amt")))
      wp.colSet("opt_last_tot_amt", "Y");

    if (!wp.colStr("no_first_cond").equals(wp.colStr("bef_no_first_cond")))
      wp.colSet("opt_no_first_cond", "Y");

    if (!wp.colStr("purch_mm").equals(wp.colStr("bef_purch_mm")))
      wp.colSet("opt_purch_mm", "Y");

    if (!wp.colStr("min_amt").equals(wp.colStr("bef_min_amt")))
      wp.colSet("opt_min_amt", "Y");

    if (!wp.colStr("amt_cond").equals(wp.colStr("bef_amt_cond")))
      wp.colSet("opt_amt_cond", "Y");

    if (!wp.colStr("tot_amt").equals(wp.colStr("bef_tot_amt")))
      wp.colSet("opt_tot_amt", "Y");

    if (!wp.colStr("cnt_cond").equals(wp.colStr("bef_cnt_cond")))
      wp.colSet("opt_cnt_cond", "Y");

    if (!wp.colStr("tot_cnt").equals(wp.colStr("bef_tot_cnt")))
      wp.colSet("opt_tot_cnt", "Y");

    if (!wp.colStr("free_use_cnt").equals(wp.colStr("bef_free_use_cnt")))
      wp.colSet("opt_free_use_cnt", "Y");

    if (!wp.colStr("spec_cond").equals(wp.colStr("bef_spec_cond")))
      wp.colSet("opt_spec_cond", "Y");

    wp.colSet("bef_mch1_cnt", listMktPpcardDtl("mkt_ppcard_dtl", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "1"));
    if (!wp.colStr("mch1_cnt").equals(wp.colStr("bef_mch1_cnt")))
      wp.colSet("opt_mch1_cnt", "Y");

    if (!wp.colStr("spec_purch_mm").equals(wp.colStr("bef_spec_purch_mm")))
      wp.colSet("opt_spec_purch_mm", "Y");

    if (!wp.colStr("spec_amt_cond1").equals(wp.colStr("bef_spec_amt_cond1")))
      wp.colSet("opt_spec_amt_cond1", "Y");

    if (!wp.colStr("spec_tot_amt1").equals(wp.colStr("bef_spec_tot_amt1")))
      wp.colSet("opt_spec_tot_amt1", "Y");

    if (!wp.colStr("spec_amt_cond2").equals(wp.colStr("bef_spec_amt_cond2")))
      wp.colSet("opt_spec_amt_cond2", "Y");

    if (!wp.colStr("spec_tot_amt2").equals(wp.colStr("bef_spec_tot_amt2")))
      wp.colSet("opt_spec_tot_amt2", "Y");

    if (!wp.colStr("spec_use_cnt").equals(wp.colStr("bef_spec_use_cnt")))
      wp.colSet("opt_spec_use_cnt", "Y");

    if (!wp.colStr("spec_mcc_flag").equals(wp.colStr("bef_spec_mcc_flag")))
      wp.colSet("opt_spec_mcc_flag", "Y");
    commMccCode2("comm_spec_mcc_flag");
    commMccCode2("comm_bef_spec_mcc_flag");

    wp.colSet("bef_spec_mcc_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "3"));
    if (!wp.colStr("spec_mcc_flag_cnt").equals(wp.colStr("bef_spec_mcc_flag_cnt")))
      wp.colSet("opt_spec_mcc_flag_cnt", "Y");

    if (!wp.colStr("spec_mcht_flag").equals(wp.colStr("bef_spec_mcht_flag")))
      wp.colSet("opt_spec_mcht_flag", "Y");
    commMcht2("comm_spec_mcht_flag");
    commMcht2("comm_bef_spec_mcht_flag");

    wp.colSet("bef_spec_mcht_flag_cnt", listMktPpcardDtl("mkt_ppcard_dtl", "MKT_PPCARD_FREE",
        wp.colStr("group_code") + wp.colStr("card_type"), "4"));
    if (!wp.colStr("spec_mcht_flag_cnt").equals(wp.colStr("bef_spec_mcht_flag_cnt")))
      wp.colSet("opt_spec_mcht_flag_cnt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("proj_desc", "");
      wp.colSet("end_date", "");
      wp.colSet("card_purch_code", "");
      wp.colSet("run_mm_flag01", "");
      wp.colSet("run_mm_flag02", "");
      wp.colSet("run_mm_flag03", "");
      wp.colSet("run_mm_flag04", "");
      wp.colSet("run_mm_flag05", "");
      wp.colSet("run_mm_flag06", "");
      wp.colSet("run_mm_flag07", "");
      wp.colSet("run_mm_flag08", "");
      wp.colSet("run_mm_flag09", "");
      wp.colSet("run_mm_flag10", "");
      wp.colSet("run_mm_flag11", "");
      wp.colSet("run_mm_flag12", "");
      wp.colSet("use_cnt_cond", "");
      wp.colSet("use_max_cnt", "");
      wp.colSet("first_cond", "");
      wp.colSet("fir_purch_mm", "");
      wp.colSet("fir_item_ename_bl", "");
      wp.colSet("fir_item_ename_ca", "");
      wp.colSet("fir_item_ename_it", "");
      wp.colSet("fir_it_type", "");
      wp.colSet("fir_item_ename_id", "");
      wp.colSet("fir_item_ename_ao", "");
      wp.colSet("fir_item_ename_ot", "");
      wp.colSet("fir_min_amt", "");
      wp.colSet("fir_amt_cond", "");
      wp.colSet("fir_tot_amt", "");
      wp.colSet("fir_cnt_cond", "");
      wp.colSet("fir_tot_cnt", "");
      wp.colSet("fir_free_use_cnt", "");
      wp.colSet("fir_spec_mcc_flag", "");
      wp.colSet("fir_spec_mcc_flag_cnt", "");
      wp.colSet("fir_spec_mcht_flag", "");
      wp.colSet("fir_spec_mcht_flag_cnt", "");
      wp.colSet("fir_spec_cond", "");
      wp.colSet("fir_spec_purch_mm", "");
      wp.colSet("fir_spec_amt_cond1", "");
      wp.colSet("fir_spec_tot_amt1", "");
      wp.colSet("fir_spec_amt_cond2", "");
      wp.colSet("fir_spec_tot_amt2", "");
      wp.colSet("fir_spec_use_cnt", "");
      wp.colSet("item_ename_bl", "");
      wp.colSet("item_ename_ca", "");
      wp.colSet("item_ename_it", "");
      wp.colSet("it_type", "");
      wp.colSet("item_ename_id", "");
      wp.colSet("item_ename_ao", "");
      wp.colSet("item_ename_ot", "");
      wp.colSet("last_amt_cond", "");
      wp.colSet("last_tot_amt", "");
      wp.colSet("no_first_cond", "");
      wp.colSet("purch_mm", "");
      wp.colSet("min_amt", "");
      wp.colSet("amt_cond", "");
      wp.colSet("tot_amt", "");
      wp.colSet("cnt_cond", "");
      wp.colSet("tot_cnt", "");
      wp.colSet("free_use_cnt", "");
      wp.colSet("spec_cond", "");
      wp.colSet("mch1_cnt", "");
      wp.colSet("spec_purch_mm", "");
      wp.colSet("spec_amt_cond1", "");
      wp.colSet("spec_tot_amt1", "");
      wp.colSet("spec_amt_cond2", "");
      wp.colSet("spec_tot_amt2", "");
      wp.colSet("spec_use_cnt", "");
      wp.colSet("spec_mcc_flag", "");
      wp.colSet("spec_mcc_flag_cnt", "");
      wp.colSet("spec_mcht_flag", "");
      wp.colSet("spec_mcht_flag_cnt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("proj_desc").length() == 0)
      wp.colSet("opt_proj_desc", "Y");

    if (wp.colStr("end_date").length() == 0)
      wp.colSet("opt_end_date", "Y");

    if (wp.colStr("card_purch_code").length() == 0)
      wp.colSet("opt_card_purch_code", "Y");

    if (wp.colStr("run_mm_flag01").length() == 0)
      wp.colSet("opt_run_mm_flag01", "Y");

    if (wp.colStr("run_mm_flag02").length() == 0)
      wp.colSet("opt_run_mm_flag02", "Y");

    if (wp.colStr("run_mm_flag03").length() == 0)
      wp.colSet("opt_run_mm_flag03", "Y");

    if (wp.colStr("run_mm_flag04").length() == 0)
      wp.colSet("opt_run_mm_flag04", "Y");

    if (wp.colStr("run_mm_flag05").length() == 0)
      wp.colSet("opt_run_mm_flag05", "Y");

    if (wp.colStr("run_mm_flag06").length() == 0)
      wp.colSet("opt_run_mm_flag06", "Y");

    if (wp.colStr("run_mm_flag07").length() == 0)
      wp.colSet("opt_run_mm_flag07", "Y");

    if (wp.colStr("run_mm_flag08").length() == 0)
      wp.colSet("opt_run_mm_flag08", "Y");

    if (wp.colStr("run_mm_flag09").length() == 0)
      wp.colSet("opt_run_mm_flag09", "Y");

    if (wp.colStr("run_mm_flag10").length() == 0)
      wp.colSet("opt_run_mm_flag10", "Y");

    if (wp.colStr("run_mm_flag11").length() == 0)
      wp.colSet("opt_run_mm_flag11", "Y");

    if (wp.colStr("run_mm_flag12").length() == 0)
      wp.colSet("opt_run_mm_flag12", "Y");

    if (wp.colStr("use_cnt_cond").length() == 0)
      wp.colSet("opt_use_cnt_cond", "Y");

    if (wp.colStr("use_max_cnt").length() == 0)
      wp.colSet("opt_use_max_cnt", "Y");

    if (wp.colStr("first_cond").length() == 0)
      wp.colSet("opt_first_cond", "Y");

    if (wp.colStr("fir_purch_mm").length() == 0)
      wp.colSet("opt_fir_purch_mm", "Y");

    if (wp.colStr("fir_item_ename_bl").length() == 0)
      wp.colSet("opt_fir_item_ename_bl", "Y");

    if (wp.colStr("fir_item_ename_ca").length() == 0)
      wp.colSet("opt_fir_item_ename_ca", "Y");

    if (wp.colStr("fir_item_ename_it").length() == 0)
      wp.colSet("opt_fir_item_ename_it", "Y");

    if (wp.colStr("fir_it_type").length() == 0)
      wp.colSet("opt_fir_it_type", "Y");

    if (wp.colStr("fir_item_ename_id").length() == 0)
      wp.colSet("opt_fir_item_ename_id", "Y");

    if (wp.colStr("fir_item_ename_ao").length() == 0)
      wp.colSet("opt_fir_item_ename_ao", "Y");

    if (wp.colStr("fir_item_ename_ot").length() == 0)
      wp.colSet("opt_fir_item_ename_ot", "Y");

    if (wp.colStr("fir_min_amt").length() == 0)
      wp.colSet("opt_fir_min_amt", "Y");

    if (wp.colStr("fir_amt_cond").length() == 0)
      wp.colSet("opt_fir_amt_cond", "Y");

    if (wp.colStr("fir_tot_amt").length() == 0)
      wp.colSet("opt_fir_tot_amt", "Y");

    if (wp.colStr("fir_cnt_cond").length() == 0)
      wp.colSet("opt_fir_cnt_cond", "Y");

    if (wp.colStr("fir_tot_cnt").length() == 0)
      wp.colSet("opt_fir_tot_cnt", "Y");

    if (wp.colStr("fir_free_use_cnt").length() == 0)
      wp.colSet("opt_fir_free_use_cnt", "Y");

    if (wp.colStr("fir_spec_mcc_flag").length() == 0)
      wp.colSet("opt_fir_spec_mcc_flag", "Y");


    if (wp.colStr("fir_spec_mcht_flag").length() == 0)
      wp.colSet("opt_fir_spec_mcht_flag", "Y");


    if (wp.colStr("fir_spec_cond").length() == 0)
      wp.colSet("opt_fir_spec_cond", "Y");

    if (wp.colStr("fir_spec_purch_mm").length() == 0)
      wp.colSet("opt_fir_spec_purch_mm", "Y");

    if (wp.colStr("fir_spec_amt_cond1").length() == 0)
      wp.colSet("opt_fir_spec_amt_cond1", "Y");

    if (wp.colStr("fir_spec_tot_amt1").length() == 0)
      wp.colSet("opt_fir_spec_tot_amt1", "Y");

    if (wp.colStr("fir_spec_amt_cond2").length() == 0)
      wp.colSet("opt_fir_spec_amt_cond2", "Y");

    if (wp.colStr("fir_spec_tot_amt2").length() == 0)
      wp.colSet("opt_fir_spec_tot_amt2", "Y");

    if (wp.colStr("fir_spec_use_cnt").length() == 0)
      wp.colSet("opt_fir_spec_use_cnt", "Y");

    if (wp.colStr("item_ename_bl").length() == 0)
      wp.colSet("opt_item_ename_bl", "Y");

    if (wp.colStr("item_ename_ca").length() == 0)
      wp.colSet("opt_item_ename_ca", "Y");

    if (wp.colStr("item_ename_it").length() == 0)
      wp.colSet("opt_item_ename_it", "Y");

    if (wp.colStr("it_type").length() == 0)
      wp.colSet("opt_it_type", "Y");

    if (wp.colStr("item_ename_id").length() == 0)
      wp.colSet("opt_item_ename_id", "Y");

    if (wp.colStr("item_ename_ao").length() == 0)
      wp.colSet("opt_item_ename_ao", "Y");

    if (wp.colStr("item_ename_ot").length() == 0)
      wp.colSet("opt_item_ename_ot", "Y");

    if (wp.colStr("last_amt_cond").length() == 0)
      wp.colSet("opt_last_amt_cond", "Y");

    if (wp.colStr("last_tot_amt").length() == 0)
      wp.colSet("opt_last_tot_amt", "Y");

    if (wp.colStr("no_first_cond").length() == 0)
      wp.colSet("opt_no_first_cond", "Y");

    if (wp.colStr("purch_mm").length() == 0)
      wp.colSet("opt_purch_mm", "Y");

    if (wp.colStr("min_amt").length() == 0)
      wp.colSet("opt_min_amt", "Y");

    if (wp.colStr("amt_cond").length() == 0)
      wp.colSet("opt_amt_cond", "Y");

    if (wp.colStr("tot_amt").length() == 0)
      wp.colSet("opt_tot_amt", "Y");

    if (wp.colStr("cnt_cond").length() == 0)
      wp.colSet("opt_cnt_cond", "Y");

    if (wp.colStr("tot_cnt").length() == 0)
      wp.colSet("opt_tot_cnt", "Y");

    if (wp.colStr("free_use_cnt").length() == 0)
      wp.colSet("opt_free_use_cnt", "Y");

    if (wp.colStr("spec_cond").length() == 0)
      wp.colSet("opt_spec_cond", "Y");


    if (wp.colStr("spec_purch_mm").length() == 0)
      wp.colSet("opt_spec_purch_mm", "Y");

    if (wp.colStr("spec_amt_cond1").length() == 0)
      wp.colSet("opt_spec_amt_cond1", "Y");

    if (wp.colStr("spec_tot_amt1").length() == 0)
      wp.colSet("opt_spec_tot_amt1", "Y");

    if (wp.colStr("spec_amt_cond2").length() == 0)
      wp.colSet("opt_spec_amt_cond2", "Y");

    if (wp.colStr("spec_tot_amt2").length() == 0)
      wp.colSet("opt_spec_tot_amt2", "Y");

    if (wp.colStr("spec_use_cnt").length() == 0)
      wp.colSet("opt_spec_use_cnt", "Y");

    if (wp.colStr("spec_mcc_flag").length() == 0)
      wp.colSet("opt_spec_mcc_flag", "Y");


    if (wp.colStr("spec_mcht_flag").length() == 0)
      wp.colSet("opt_spec_mcht_flag", "Y");


  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int accessErr = 0;
    mktp02.Mktp1120Func func = new mktp02.Mktp1120Func(wp);

    String[] lsGroupCode = wp.itemBuff("group_code");
    String[] lsCardType = wp.itemBuff("card_type");
    String[] lsBinType = wp.itemBuff("bin_type");
    String[] lsVipKind = wp.itemBuff("vip_kind");
    String[] lsVipGroupCode = wp.itemBuff("vip_group_code");
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
      if (lsCrtUser[rr].equals(wp.loginUser)) {
    	  accessErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }

      func.varsSet("group_code", lsGroupCode[rr]);
      func.varsSet("card_type", lsCardType[rr]);
      func.varsSet("bin_type", lsBinType[rr]);
      func.varsSet("vip_kind", lsVipKind[rr]);
      func.varsSet("vip_group_code", lsVipGroupCode[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbDeleteD4T();
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4T();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4T();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commCardType("comm_card_type");
        commGroupCode("comm_group_code");
        commVmjCode("comm_bin_type");
        commfuncAudType("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + accessErr);
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String ls_sql = "";
    try {
      if ((wp.respHtml.equals("mktp1120"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_vip_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_vip_group_code");
        }
        this.dddwList("dddw_vip_group_code", "ptr_group_code", "trim(group_code)",
            "trim(group_name)", " where group_code in (select group_code from mkt_ppcard_issue) ");
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
          + " where 1 = 1 " 
//    	  + " and   group_code = '" + wp.colStr(ii, befStr + "group_code") + "'";
      	  + " and group_code = :group_code ";
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
  public void commCardType(String type) throws Exception {
    commCardType(type, 0);
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
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
//          + " and   card_type = '" + wp.colStr(ii, befStr + "card_type") + "'";
	        + " and card_type = :card_type ";
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
  public void commBinType(String binType) throws Exception {
    commBinType(binType, 0);
    return;
  }

  // ************************************************************************
  public void commBinType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " card_desc as column_card_desc " + " from ptr_bintable e,ptr_card_type f "
          + " where 1 = 1 " 
//    	  + " and   e.card_type = '" + wp.colStr(ii, befStr + "card_type") + "'"
    	  + " and e.card_type = :card_type "
          + " and   e.card_type = f.card_type ";
      	  setString("card_type",wp.colStr(ii, befStr + "card_type"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_card_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commPurcCode(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"1.正附卡合併計算", "2.正附卡分開計算"};
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
  public void commCntCond(String cde1) throws Exception {
    String[] cde = {"Y", "S", "M"};
    String[] txt = {"年", "季", "月"};
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
  public void commItType1(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"每期金額", "總金額"};
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
  public void commMccCode1(String cde1) throws Exception {
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
  public void commMcht1(String scde1) throws Exception {
    String[] cde = {"0", "1"};
    String[] txt = {"全部", "指定"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = scde1.substring(5, scde1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, scde1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commItType2(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"每期金額", "總金額"};
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
  public void commMccCode2(String cde1) throws Exception {
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
  public void commMcht2(String cde1) throws Exception {
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
  public void commVmjCode(String cde1) throws Exception {
    String[] cde = {"V", "M", "J"};
    String[] txt = {"VISA", "MasterCard", "JCB"};
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
  String listMktPpcardDtl(String table, String tableName, String dataKey, String dataType) throws Exception {
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
