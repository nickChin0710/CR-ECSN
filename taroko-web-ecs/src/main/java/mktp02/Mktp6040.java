/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/14  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *   
***************************************************************************/
package mktp02;

import mktp02.Mktp6040Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6040 extends BaseProc {
  private String PROGNAME = "專案免年費參數覆核作業處理程式108/05/14 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6040Func func = null;
  String rowid;
  String projectNo;
  String fstAprFlag = "";
  String orgTabName = "cyc_anul_project_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_project_no"), "a.project_no", "like%")
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
        + "a.aud_type," + "a.project_no," + "a.project_name," + "a.acct_type_sel,"
        + "a.group_code_sel," + "a.source_code_sel," + "a.free_fee_cnt," + "a.recv_s_date,"
        + "a.recv_e_date," + "a.issue_date_s," + "a.issue_date_e," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by project_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commSelfunc1("comm_acct_type_sel");
    commSelfunc2("comm_group_code_sel");
    commSelfunc3("comm_source_code_sel");
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
      if (wp.itemStr("kk_project_no").length() == 0) {
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
        + "a.project_no as project_no," + "a.crt_user," + "a.project_name," + "a.acct_type_sel,"
        + "a.group_code_sel," + "a.source_code_sel," + "a.recv_month_tag," + "a.recv_s_date,"
        + "a.recv_e_date," + "a.issue_date_tag," + "a.issue_date_s," + "a.issue_date_e,"
        + "a.mcard_cond," + "a.scard_cond," + "a.cnt_months_tag," + "a.cnt_months,"
        + "a.accumulate_cnt," + "a.average_amt," + "a.cnt_bl_flag," + "a.cnt_ca_flag,"
        + "a.cnt_it_flag," + "a.cnt_ao_flag," + "a.cnt_id_flag," + "a.cnt_ot_flag,"
        + "a.amt_months_tag," + "a.amt_months," + "a.accumulate_amt," + "a.amt_bl_flag,"
        + "a.amt_ca_flag," + "a.amt_it_flag," + "a.amt_ao_flag," + "a.amt_id_flag,"
        + "a.amt_ot_flag," + "a.adv_months_tag," + "a.adv_months," + "a.adv_cnt," + "a.adv_amt,"
        + "a.mcode," + "a.free_fee_cnt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(projectNo, "a.project_no");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commAcctType1("comm_acct_type_sel");
    commGroupCode1("comm_group_code_sel");
    commSourceCode1("comm_source_code_sel");
    checkButtonOff();
    projectNo = wp.colStr("project_no");
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
    controlTabName = "cyc_anul_project";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.project_no as project_no," + "a.crt_user as bef_crt_user,"
        + "a.project_name as bef_project_name," + "a.acct_type_sel as bef_acct_type_sel,"
        + "a.group_code_sel as bef_group_code_sel," + "a.source_code_sel as bef_source_code_sel,"
        + "a.recv_month_tag as bef_recv_month_tag," + "a.recv_s_date as bef_recv_s_date,"
        + "a.recv_e_date as bef_recv_e_date," + "a.issue_date_tag as bef_issue_date_tag,"
        + "a.issue_date_s as bef_issue_date_s," + "a.issue_date_e as bef_issue_date_e,"
        + "a.mcard_cond as bef_mcard_cond," + "a.scard_cond as bef_scard_cond,"
        + "a.cnt_months_tag as bef_cnt_months_tag," + "a.cnt_months as bef_cnt_months,"
        + "a.accumulate_cnt as bef_accumulate_cnt," + "a.average_amt as bef_average_amt,"
        + "a.cnt_bl_flag as bef_cnt_bl_flag," + "a.cnt_ca_flag as bef_cnt_ca_flag,"
        + "a.cnt_it_flag as bef_cnt_it_flag," + "a.cnt_ao_flag as bef_cnt_ao_flag,"
        + "a.cnt_id_flag as bef_cnt_id_flag," + "a.cnt_ot_flag as bef_cnt_ot_flag,"
        + "a.amt_months_tag as bef_amt_months_tag," + "a.amt_months as bef_amt_months,"
        + "a.accumulate_amt as bef_accumulate_amt," + "a.amt_bl_flag as bef_amt_bl_flag,"
        + "a.amt_ca_flag as bef_amt_ca_flag," + "a.amt_it_flag as bef_amt_it_flag,"
        + "a.amt_ao_flag as bef_amt_ao_flag," + "a.amt_id_flag as bef_amt_id_flag,"
        + "a.amt_ot_flag as bef_amt_ot_flag," + "a.adv_months_tag as bef_adv_months_tag,"
        + "a.adv_months as bef_adv_months," + "a.adv_cnt as bef_adv_cnt,"
        + "a.adv_amt as bef_adv_amt," + "a.mcode as bef_mcode,"
        + "a.free_fee_cnt as bef_free_fee_cnt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(projectNo, "a.project_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAcctType1("comm_acct_type_sel");
    commGroupCode1("comm_group_code_sel");
    commSourceCode1("comm_source_code_sel");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("acct_type_sel_cnt",
        listCycBnData("cyc_bn_data_t", "CYC_ANUL_PROJECT", wp.colStr("project_no"), "1"));
    wp.colSet("group_code_sel_cnt",
        listCycBnData("cyc_bn_data_t", "CYC_ANUL_PROJECT", wp.colStr("project_no"), "2"));
    wp.colSet("source_code_sel_cnt",
        listCycBnData("cyc_bn_data_t", "CYC_ANUL_PROJECT", wp.colStr("project_no"), "3"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("project_name").equals(wp.colStr("bef_project_name")))
      wp.colSet("opt_project_name", "Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctType1("comm_acct_type_sel");
    commAcctType1("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listCycBnData("cyc_bn_data", "CYC_ANUL_PROJECT", wp.colStr("project_no"), "1"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCode1("comm_group_code_sel");
    commGroupCode1("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listCycBnData("cyc_bn_data", "CYC_ANUL_PROJECT", wp.colStr("project_no"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("source_code_sel").equals(wp.colStr("bef_source_code_sel")))
      wp.colSet("opt_source_code_sel", "Y");
    commSourceCode1("comm_source_code_sel");
    commSourceCode1("comm_bef_source_code_sel");

    wp.colSet("bef_source_code_sel_cnt",
        listCycBnData("cyc_bn_data", "CYC_ANUL_PROJECT", wp.colStr("project_no"), "3"));
    if (!wp.colStr("source_code_sel_cnt").equals(wp.colStr("bef_source_code_sel_cnt")))
      wp.colSet("opt_source_code_sel_cnt", "Y");

    if (!wp.colStr("recv_month_tag").equals(wp.colStr("bef_recv_month_tag")))
      wp.colSet("opt_recv_month_tag", "Y");

    if (!wp.colStr("recv_s_date").equals(wp.colStr("bef_recv_s_date")))
      wp.colSet("opt_recv_s_date", "Y");

    if (!wp.colStr("recv_e_date").equals(wp.colStr("bef_recv_e_date")))
      wp.colSet("opt_recv_e_date", "Y");

    if (!wp.colStr("issue_date_tag").equals(wp.colStr("bef_issue_date_tag")))
      wp.colSet("opt_issue_date_tag", "Y");

    if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
      wp.colSet("opt_issue_date_s", "Y");

    if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
      wp.colSet("opt_issue_date_e", "Y");

    if (!wp.colStr("mcard_cond").equals(wp.colStr("bef_mcard_cond")))
      wp.colSet("opt_mcard_cond", "Y");

    if (!wp.colStr("scard_cond").equals(wp.colStr("bef_scard_cond")))
      wp.colSet("opt_scard_cond", "Y");

    if (!wp.colStr("cnt_months_tag").equals(wp.colStr("bef_cnt_months_tag")))
      wp.colSet("opt_cnt_months_tag", "Y");

    if (!wp.colStr("cnt_months").equals(wp.colStr("bef_cnt_months")))
      wp.colSet("opt_cnt_months", "Y");

    if (!wp.colStr("accumulate_cnt").equals(wp.colStr("bef_accumulate_cnt")))
      wp.colSet("opt_accumulate_cnt", "Y");

    if (!wp.colStr("average_amt").equals(wp.colStr("bef_average_amt")))
      wp.colSet("opt_average_amt", "Y");

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

    if (!wp.colStr("amt_months_tag").equals(wp.colStr("bef_amt_months_tag")))
      wp.colSet("opt_amt_months_tag", "Y");

    if (!wp.colStr("amt_months").equals(wp.colStr("bef_amt_months")))
      wp.colSet("opt_amt_months", "Y");

    if (!wp.colStr("accumulate_amt").equals(wp.colStr("bef_accumulate_amt")))
      wp.colSet("opt_accumulate_amt", "Y");

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

    if (!wp.colStr("adv_months_tag").equals(wp.colStr("bef_adv_months_tag")))
      wp.colSet("opt_adv_months_tag", "Y");

    if (!wp.colStr("adv_months").equals(wp.colStr("bef_adv_months")))
      wp.colSet("opt_adv_months", "Y");

    if (!wp.colStr("adv_cnt").equals(wp.colStr("bef_adv_cnt")))
      wp.colSet("opt_adv_cnt", "Y");

    if (!wp.colStr("adv_amt").equals(wp.colStr("bef_adv_amt")))
      wp.colSet("opt_adv_amt", "Y");

    if (!wp.colStr("mcode").equals(wp.colStr("bef_mcode")))
      wp.colSet("opt_mcode", "Y");

    if (!wp.colStr("free_fee_cnt").equals(wp.colStr("bef_free_fee_cnt")))
      wp.colSet("opt_free_fee_cnt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("project_name", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("source_code_sel", "");
      wp.colSet("source_code_sel_cnt", "");
      wp.colSet("recv_month_tag", "");
      wp.colSet("recv_s_date", "");
      wp.colSet("recv_e_date", "");
      wp.colSet("issue_date_tag", "");
      wp.colSet("issue_date_s", "");
      wp.colSet("issue_date_e", "");
      wp.colSet("mcard_cond", "");
      wp.colSet("scard_cond", "");
      wp.colSet("cnt_months_tag", "");
      wp.colSet("cnt_months", "");
      wp.colSet("accumulate_cnt", "");
      wp.colSet("average_amt", "");
      wp.colSet("cnt_bl_flag", "");
      wp.colSet("cnt_ca_flag", "");
      wp.colSet("cnt_it_flag", "");
      wp.colSet("cnt_ao_flag", "");
      wp.colSet("cnt_id_flag", "");
      wp.colSet("cnt_ot_flag", "");
      wp.colSet("amt_months_tag", "");
      wp.colSet("amt_months", "");
      wp.colSet("accumulate_amt", "");
      wp.colSet("amt_bl_flag", "");
      wp.colSet("amt_ca_flag", "");
      wp.colSet("amt_it_flag", "");
      wp.colSet("amt_ao_flag", "");
      wp.colSet("amt_id_flag", "");
      wp.colSet("amt_ot_flag", "");
      wp.colSet("adv_months_tag", "");
      wp.colSet("adv_months", "");
      wp.colSet("adv_cnt", "");
      wp.colSet("adv_amt", "");
      wp.colSet("mcode", "");
      wp.colSet("free_fee_cnt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("project_name").length() == 0)
      wp.colSet("opt_project_name", "Y");

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("opt_acct_type_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("source_code_sel").length() == 0)
      wp.colSet("opt_source_code_sel", "Y");


    if (wp.colStr("recv_month_tag").length() == 0)
      wp.colSet("opt_recv_month_tag", "Y");

    if (wp.colStr("recv_s_date").length() == 0)
      wp.colSet("opt_recv_s_date", "Y");

    if (wp.colStr("recv_e_date").length() == 0)
      wp.colSet("opt_recv_e_date", "Y");

    if (wp.colStr("issue_date_tag").length() == 0)
      wp.colSet("opt_issue_date_tag", "Y");

    if (wp.colStr("issue_date_s").length() == 0)
      wp.colSet("opt_issue_date_s", "Y");

    if (wp.colStr("issue_date_e").length() == 0)
      wp.colSet("opt_issue_date_e", "Y");

    if (wp.colStr("mcard_cond").length() == 0)
      wp.colSet("opt_mcard_cond", "Y");

    if (wp.colStr("scard_cond").length() == 0)
      wp.colSet("opt_scard_cond", "Y");

    if (wp.colStr("cnt_months_tag").length() == 0)
      wp.colSet("opt_cnt_months_tag", "Y");

    if (wp.colStr("cnt_months").length() == 0)
      wp.colSet("opt_cnt_months", "Y");

    if (wp.colStr("accumulate_cnt").length() == 0)
      wp.colSet("opt_accumulate_cnt", "Y");

    if (wp.colStr("average_amt").length() == 0)
      wp.colSet("opt_average_amt", "Y");

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

    if (wp.colStr("amt_months_tag").length() == 0)
      wp.colSet("opt_amt_months_tag", "Y");

    if (wp.colStr("amt_months").length() == 0)
      wp.colSet("opt_amt_months", "Y");

    if (wp.colStr("accumulate_amt").length() == 0)
      wp.colSet("opt_accumulate_amt", "Y");

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

    if (wp.colStr("adv_months_tag").length() == 0)
      wp.colSet("opt_adv_months_tag", "Y");

    if (wp.colStr("adv_months").length() == 0)
      wp.colSet("opt_adv_months", "Y");

    if (wp.colStr("adv_cnt").length() == 0)
      wp.colSet("opt_adv_cnt", "Y");

    if (wp.colStr("adv_amt").length() == 0)
      wp.colSet("opt_adv_amt", "Y");

    if (wp.colStr("mcode").length() == 0)
      wp.colSet("opt_mcode", "Y");

    if (wp.colStr("free_fee_cnt").length() == 0)
      wp.colSet("opt_free_fee_cnt", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp6040Func func = new mktp02.Mktp6040Func(wp);

    String[] lsProjectNo = wp.itemBuff("project_no");
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

      func.varsSet("project_no", lsProjectNo[rr]);
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
        commSelfunc1("comm_acct_type_sel");
        commSelfunc2("comm_group_code_sel");
        commSelfunc3("comm_source_code_sel");
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
  public void commAcctType1(String cde1) throws Exception {
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
  public void commGroupCode1(String cde1) throws Exception {
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
  public void commSourceCode1(String cde1) throws Exception {
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
  public void commSelfunc1(String cde1) throws Exception {
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
  public void commSelfunc2(String cde1) throws Exception {
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
  public void commSelfunc3(String cde1) throws Exception {
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
  String listCycBnData(String table, String tableName, String dataKey, String dataType) throws Exception {
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
