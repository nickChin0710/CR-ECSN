/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*
 * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *                                                         *
* 110-10-29  V1.00.04  Yangbo       joint sql replace to parameters way    *
* 112-05-08  V1.00.05  Zuwei Su       增加"一般消費群組"    *
***************************************************************************/
package dbmm01;

import dbmm01.Dbmp0190Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmp0190 extends BaseProc
{
  private final String PROGNAME = "Visa金融卡紅利加贈參數覆核-新發卡處理程式108/08/06 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  dbmm01.Dbmp0190Func func = null;
  String rowid;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "dbm_bpis_t";
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
    } else if (eqIgno(wp.buttonCode, "R2"))
    {// 明細查詢 -/
    	strAction = "R2";
       dataRead_R2();
    } else if (eqIgno(wp.buttonCode, "R3"))
    {// 明細查詢 -/
    	strAction = "R3";
       dataRead_R3();
    }

    else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
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

    wp.selectSQL =
        " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
            + "a.active_code," + "a.active_name," + "a.active_s_date," + "a.active_e_date,"
            + "a.bp_amt," + "a.add_times," + "a.add_point," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code,a.crt_date";

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
        + "a.active_code as active_code," + "a.crt_user," + "a.active_name," + "a.give_code,"
        + "a.new_card_cond," + "a.active_s_date," + "a.active_e_date," + "a.re_months,"
        + "a.acct_type_sel," + "a.group_code_sel," + "a.merchant_sel," + "a.mcht_group_sel," + "a.platform_kind_sel,"
        + "a.mcc_code_sel," + "a.pos_entry_sel," + "a.tax_flag," + "a.bp_amt," + "a.bp_pnt,"
        + "a.add_times," + "a.add_point";

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
    commAcctTypeSel("comm_acct_type_sel");
    commGroupSel("comm_group_code_sel");
    commMerchantSel("comm_merchant_sel");
    commMchtGroupSel("comm_mcht_group_sel");
    commMchtGroupSel("comm_platform_kind_sel");
    commMccCodeSel("comm_mcc_code_sel");
    commPosEntrySel("comm_pos_entry_sel");
    commGiveCode("comm_give_code");
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
    controlTabName = "dbm_bpis";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.active_code as active_code," + "a.crt_user as bef_crt_user,"
        + "a.active_name as bef_active_name," + "a.give_code as bef_give_code,"
        + "a.new_card_cond as bef_new_card_cond," + "a.active_s_date as bef_active_s_date,"
        + "a.active_e_date as bef_active_e_date," + "a.re_months as bef_re_months,"
        + "a.acct_type_sel as bef_acct_type_sel," + "a.group_code_sel as bef_group_code_sel,"
        + "a.merchant_sel as bef_merchant_sel," + "a.mcht_group_sel as bef_mcht_group_sel," + "a.platform_kind_sel as bef_platform_kind_sel,"
        + "a.mcc_code_sel as bef_mcc_code_sel," + "a.pos_entry_sel as bef_pos_entry_sel,"
        + "a.tax_flag as bef_tax_flag," + "a.bp_amt as bef_bp_amt," + "a.bp_pnt as bef_bp_pnt,"
        + "a.add_times as bef_add_times," + "a.add_point as bef_add_point";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commGiveCode("comm_give_code");
    commAcctTypeSel("comm_acct_type_sel");
    commGroupSel("comm_group_code_sel");
    commMerchantSel("comm_merchant_sel");
    commMchtGroupSel("comm_mcht_group_sel");
    commMchtGroupSel("comm_platform_kind_sel");
    commMccCodeSel("comm_mcc_code_sel");
    commPosEntrySel("comm_pos_entry_sel");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("acct_type_sel_cnt",
        listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "3"));
    wp.colSet("group_code_sel_cnt",
        listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "2"));
    wp.colSet("merchant_sel_cnt",
        listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "1"));
    wp.colSet("mcht_group_sel_cnt",
        listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "6"));
    wp.colSet("platform_kind_sel_cnt",
            listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "P"));
    wp.colSet("mcc_code_sel_cnt",
        listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "5"));
    wp.colSet("pos_entry_sel_cnt",
        listDbmBnData("dbm_bn_data_t", "DBM_BPIS", wp.colStr("active_code"), "4"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
      wp.colSet("opt_active_name", "Y");

    if (!wp.colStr("give_code").equals(wp.colStr("bef_give_code")))
      wp.colSet("opt_give_code", "Y");
    commGiveCode("comm_give_code");
    commGiveCode("comm_bef_give_code", 1);

    if (!wp.colStr("new_card_cond").equals(wp.colStr("bef_new_card_cond")))
      wp.colSet("opt_new_card_cond", "Y");

    if (!wp.colStr("active_s_date").equals(wp.colStr("bef_active_s_date")))
      wp.colSet("opt_active_s_date", "Y");

    if (!wp.colStr("active_e_date").equals(wp.colStr("bef_active_e_date")))
      wp.colSet("opt_active_e_date", "Y");

    if (!wp.colStr("re_months").equals(wp.colStr("bef_re_months")))
      wp.colSet("opt_re_months", "Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctTypeSel("comm_acct_type_sel");
    commAcctTypeSel("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "3"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupSel("comm_group_code_sel");
    commGroupSel("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commMerchantSel("comm_merchant_sel");
    commMerchantSel("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "1"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    commMchtGroupSel("comm_mcht_group_sel");
    commMchtGroupSel("comm_bef_mcht_group_sel");

    wp.colSet("bef_mcht_group_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "6"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");

    if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
      wp.colSet("opt_platform_kind_sel", "Y");
    commMchtGroupSel("comm_platform_kind_sel");
    commMchtGroupSel("comm_bef_platform_kind_sel");

    wp.colSet("bef_platform_kind_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "P"));
    if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
      wp.colSet("opt_platform_kind_sel_cnt", "Y");

    if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
      wp.colSet("opt_mcc_code_sel", "Y");
    commMccCodeSel("comm_mcc_code_sel");
    commMccCodeSel("comm_bef_mcc_code_sel");

    wp.colSet("bef_mcc_code_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "5"));
    if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
      wp.colSet("opt_mcc_code_sel_cnt", "Y");

    if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
      wp.colSet("opt_pos_entry_sel", "Y");
    commPosEntrySel("comm_pos_entry_sel");
    commPosEntrySel("comm_bef_pos_entry_sel");

    wp.colSet("bef_pos_entry_sel_cnt",
        listDbmBnData("dbm_bn_data", "DBM_BPIS", wp.colStr("active_code"), "4"));
    if (!wp.colStr("pos_entry_sel_cnt").equals(wp.colStr("bef_pos_entry_sel_cnt")))
      wp.colSet("opt_pos_entry_sel_cnt", "Y");

    if (!wp.colStr("tax_flag").equals(wp.colStr("bef_tax_flag")))
      wp.colSet("opt_tax_flag", "Y");

    if (!wp.colStr("bp_amt").equals(wp.colStr("bef_bp_amt")))
      wp.colSet("opt_bp_amt", "Y");

    if (!wp.colStr("bp_pnt").equals(wp.colStr("bef_bp_pnt")))
      wp.colSet("opt_bp_pnt", "Y");

    if (!wp.colStr("add_times").equals(wp.colStr("bef_add_times")))
      wp.colSet("opt_add_times", "Y");

    if (!wp.colStr("add_point").equals(wp.colStr("bef_add_point")))
      wp.colSet("opt_add_point", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("active_name", "");
      wp.colSet("give_code", "");
      wp.colSet("new_card_cond", "");
      wp.colSet("active_s_date", "");
      wp.colSet("active_e_date", "");
      wp.colSet("re_months", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("merchant_sel", "");
      wp.colSet("merchant_sel_cnt", "");
      wp.colSet("mcht_group_sel", "");
      wp.colSet("mcht_group_sel_cnt", "");
      wp.colSet("platform_kind_sel", "");
      wp.colSet("platform_kind_sel_cnt", "");
      wp.colSet("mcc_code_sel", "");
      wp.colSet("mcc_code_sel_cnt", "");
      wp.colSet("pos_entry_sel", "");
      wp.colSet("pos_entry_sel_cnt", "");
      wp.colSet("tax_flag", "");
      wp.colSet("bp_amt", "");
      wp.colSet("bp_pnt", "");
      wp.colSet("add_times", "");
      wp.colSet("add_point", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("active_name").length() == 0)
      wp.colSet("opt_active_name", "Y");

    if (wp.colStr("give_code").length() == 0)
      wp.colSet("opt_give_code", "Y");

    if (wp.colStr("new_card_cond").length() == 0)
      wp.colSet("opt_new_card_cond", "Y");

    if (wp.colStr("active_s_date").length() == 0)
      wp.colSet("opt_active_s_date", "Y");

    if (wp.colStr("active_e_date").length() == 0)
      wp.colSet("opt_active_e_date", "Y");

    if (wp.colStr("re_months").length() == 0)
      wp.colSet("opt_re_months", "Y");

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("opt_acct_type_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("opt_merchant_sel", "Y");


    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("opt_mcht_group_sel", "Y");


    if (wp.colStr("platform_kind_sel").length() == 0)
      wp.colSet("opt_platform_kind_sel", "Y");


    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("opt_mcc_code_sel", "Y");


    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("opt_pos_entry_sel", "Y");


    if (wp.colStr("tax_flag").length() == 0)
      wp.colSet("opt_tax_flag", "Y");

    if (wp.colStr("bp_amt").length() == 0)
      wp.colSet("opt_bp_amt", "Y");

    if (wp.colStr("bp_pnt").length() == 0)
      wp.colSet("opt_bp_pnt", "Y");

    if (wp.colStr("add_times").length() == 0)
      wp.colSet("opt_add_times", "Y");

    if (wp.colStr("add_point").length() == 0)
      wp.colSet("opt_add_point", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ok = 0;
    int err = 0;
    dbmm01.Dbmp0190Func func = new dbmm01.Dbmp0190Func(wp);

    String[] lsActivecCode = wp.itemBuff("active_code");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] rowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + rowid[rr]);

      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("active_code", lsActivecCode[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", rowid[rr]);
      wp.itemSet("wprowid", rowid[rr]);
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
        ok++;
        func.dbDelete();
        this.sqlCommit(rc);
        continue;
      }
      err++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ok + "; 失敗筆數=" + err);
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

	       if ((wp.respHtml.equals("dbmp0190_acty")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_acct_type"
	                 ,"dbp_acct_type"
	                 ,"trim(acct_type)"
	                 ,"trim(chin_name)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("dbmp0190_grop")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_group_code3"
	                 ,"select a.group_code as db_code,a.group_code||'-'||max(a.group_name) as db_desc from ptr_group_code a,dbc_card_type b where a.group_code=b.group_code group by a.group_code"
	                        );
	         }
	       if ((wp.respHtml.equals("dbmp0190_mccd")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_data_code07"
	                 ,"cca_mcc_risk"
	                 ,"trim(mcc_code)"
	                 ,"trim(mcc_remark)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("dbmp0190_enty")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_entry_mode"
	                 ,"cca_entry_mode"
	                 ,"trim(entry_mode)"
	                 ,"trim(mode_desc)"
	                 ," where 1 = 1 ");
	         }
	       if ((wp.respHtml.equals("dbmp0190_aaa1")))
	         {
	          wp.initOption ="";
	          wp.optionKey = "";
	          this.dddwList("dddw_mcht_gp"
	                 ,"mkt_mcht_gp"
	                 ,"trim(mcht_group_id)"
	                 ,"trim(mcht_group_desc)"
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
  public void commGiveCode(String code) throws Exception {
    commGiveCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commGiveCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, befStr + "give_code") + "'"
          + sqlCol(wp.colStr(ii, befStr + "give_code"), "wf_id")
          + " and   wf_type = 'GIVE_CODE' ";
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
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
  String listDbmBnData(String table, String tableName, String dateKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 "
//        + " and   table_name = '" + tableName + "'" + " and   data_key   = '" + dateKey + "'"
//        + " and   data_type  = '" + dataType + "'";
        + sqlCol(tableName, "table_name")
        + sqlCol(dateKey, "data_key")
        + sqlCol(dataType, "data_type");
    sqlSelect(sql1);

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }

//************************************************************************
public void dataRead_R2() throws Exception
{
  String bnTable="";

  wp.selectCnt=1;
  this.selectNoLimit();
  bnTable = "dbm_bn_data_t";

  wp.selectSQL = "hex(rowid) as r2_rowid, "
               + "ROW_NUMBER()OVER() as ser_num, "
               + "mod_seqno as r2_mod_seqno, "
               + "data_key, "
               + "data_code, "
               + "mod_user as r2_mod_user "
               ;
  wp.daoTable = bnTable ;
  wp.whereStr = "where 1=1"
             + " and table_name  =  'DBM_BPIS' "
               ;
  if (wp.respHtml.equals("dbmp0190_acty"))
     wp.whereStr  += " and data_type  = '3' ";
  if (wp.respHtml.equals("dbmp0190_grop"))
     wp.whereStr  += " and data_type  = '2' ";
  if (wp.respHtml.equals("dbmp0190_aaa1"))
     wp.whereStr  += " and data_type  = '6' ";
  if (wp.respHtml.equals("dbmp0190_platform"))
      wp.whereStr  += " and data_type  = 'P' ";
  if (wp.respHtml.equals("dbmp0190_mccd"))
     wp.whereStr  += " and data_type  = '5' ";
  if (wp.respHtml.equals("dbmp0190_enty"))
     wp.whereStr  += " and data_type  = '4' ";
  String whereCnt = wp.whereStr;
//  whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
  whereCnt += sqlCol(wp.itemStr("active_code"), "data_key");

  int cnt1=selectBndataCount(wp.daoTable,whereCnt);
  if (cnt1>300)
     {
      alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
     }

  wp.whereStr  += " and  data_key = :data_key ";
  setString("data_key", wp.itemStr("active_code"));
  wp.whereStr  += " order by 4,5,6 ";

  pageQuery();
  wp.setListCount(1);
  wp.notFound = "";

  wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
  if (wp.respHtml.equals("dbmp0190_acty"))
   commAccttype("comm_data_code");
  if (wp.respHtml.equals("dbmp0190_grop"))
   commGroupcode("comm_data_code");
  if (wp.respHtml.equals("dbmp0190_aaa1"))
   commMechtgp("comm_data_code");
  if (wp.respHtml.equals("dbmp0190_platform"))
      commMechtgp("comm_data_code");
  if (wp.respHtml.equals("dbmp0190_mccd"))
   commDatacode07("comm_data_code");
  if (wp.respHtml.equals("dbmp0190_enty"))
   commEntrymode("comm_data_code");
 }
//************************************************************************
public void dataRead_R3() throws Exception
{
  String bnTable="";

  wp.selectCnt=1;
  this.selectNoLimit();
  bnTable = "dbm_bn_data_t";

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
             + " and table_name  =  'DBM_BPIS' "
               ;
  if (wp.respHtml.equals("dbmp0190_mcht"))
     wp.whereStr  += " and data_type  = '1' ";
  String whereCnt = wp.whereStr;
//  whereCnt += " and  data_key = '"+ wp.itemStr("active_code") +  "'";
  whereCnt += sqlCol(wp.itemStr("active_code"), "data_key");

  int cnt1=selectBndataCount(wp.daoTable,whereCnt);
  if (cnt1>300)
     {
      alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
     }

  wp.whereStr  += " and  data_key = :data_key ";
  setString("data_key", wp.itemStr("active_code"));
  wp.whereStr  += " order by 4,5,6,7 ";

  pageQuery();
  wp.setListCount(1);
  wp.notFound = "";

  wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
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
public void commAccttype(String s1) throws Exception
{
	commAccttype(s1,0);
return;
}
//************************************************************************
public void commAccttype(String s1,int bef_type) throws Exception
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
         + " from dbp_acct_type "
         + " where 1 = 1 "
//         + " and   acct_type = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "acct_type")
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_chin_name");
    wp.colSet(ii, s1, columnData);
   }
return;
}

//************************************************************************
public void commGroupcode(String s1) throws Exception
{
	commGroupcode(s1,0);
return;
}
//************************************************************************
public void commGroupcode(String s1,int bef_type) throws Exception
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
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "group_code")
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_group_name");
    wp.colSet(ii, s1, columnData);
   }
return;
}


//************************************************************************
public void commMechtgp(String s1) throws Exception
{
	commMechtgp(s1,0);
return;
}
//************************************************************************
public void commMechtgp(String s1,int bef_type) throws Exception
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
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "mcht_group_id")
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcht_group_desc");
    wp.colSet(ii, s1, columnData);
   }
return;
}
//************************************************************************
public void commDatacode07(String s1) throws Exception
{
	commDatacode07(s1,0);
return;
}
//************************************************************************
public void commDatacode07(String s1,int bef_type) throws Exception
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
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "mcc_code")
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcc_remark");
    wp.colSet(ii, s1, columnData);
   }
return;
}


//************************************************************************
public void commEntrymode(String s1) throws Exception
{
	commEntrymode(s1,0);
return;
}
//************************************************************************
public void commEntrymode(String s1,int bef_type) throws Exception
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
//         + " and   entry_mode = '"+wp.colStr(ii,befStr+"data_code")+"'"
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "entry_mode")
         ;
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mode_desc");
    wp.colSet(ii, s1, columnData);
   }
return;
}

} // End of class