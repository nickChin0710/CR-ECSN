/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *   
***************************************************************************/
package mktp02;

import mktp02.Mktp4060Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp4060 extends BaseProc {
  private String PROGNAME = "發卡/續卡/停卡參數維護處理程式108/09/03 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp4060Func func = null;
  String rowid;
  String batchNo1;
  String fstAprFlag = "";
  String orgTabName = "mkt_rept_par_t";
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
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 "
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
            + "a.batch_no," + "a.description," + "a.acct_type_sel," + "a.group_code_sel,"
            + "a.source_code_sel," + "a.carddate_sel," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by batch_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commAcctTypeS("comm_acct_type_sel");
    commGroupCodeS("comm_group_code_sel");
    commSourceCodeS("comm_source_code_sel");
    commCardDate("comm_carddate_sel");
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
      if (wp.itemStr("kk_batch_no").length() == 0) {
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
        + "a.batch_no as batch_no," + "a.crt_user," + "a.description," + "a.filename_beg,"
        + "a.filename_end," + "a.acct_type_sel," + "a.group_code_sel," + "a.source_code_sel,"
        + "a.carddate_sel," + "a.issue_date_s," + "a.issue_date_e," + "a.change_date_s,"
        + "a.change_date_e," + "a.stop_date_s," + "a.stop_date_e," + "a.apply_date_s,"
        + "a.apply_date_e," + "a.subissue_date_s," + "a.subissue_date_e," + "a.reissue_date_s,"
        + "a.reissue_date_e," + "a.text_form," + "a.validate_card," + "a.invalidate_card,"
        + "a.format_form," + "a.zip_passwd";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(batchNo1, "a.batch_no");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commAcctType("comm_acct_type_sel");
    commGroupCode("comm_group_code_sel");
    commSourceCode("comm_source_code_sel");
    commCsrddate("comm_carddate_sel");
    commTextForm("comm_text_form");
    checkButtonOff();
    batchNo1 = wp.colStr("batch_no");
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
    controlTabName = "mkt_rept_par";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.batch_no as batch_no," + "a.crt_user as bef_crt_user,"
        + "a.description as bef_description," + "a.filename_beg as bef_filename_beg,"
        + "a.filename_end as bef_filename_end," + "a.acct_type_sel as bef_acct_type_sel,"
        + "a.group_code_sel as bef_group_code_sel," + "a.source_code_sel as bef_source_code_sel,"
        + "a.carddate_sel as bef_carddate_sel," + "a.issue_date_s as bef_issue_date_s,"
        + "a.issue_date_e as bef_issue_date_e," + "a.change_date_s as bef_change_date_s,"
        + "a.change_date_e as bef_change_date_e," + "a.stop_date_s as bef_stop_date_s,"
        + "a.stop_date_e as bef_stop_date_e," + "a.apply_date_s as bef_apply_date_s,"
        + "a.apply_date_e as bef_apply_date_e," + "a.subissue_date_s as bef_subissue_date_s,"
        + "a.subissue_date_e as bef_subissue_date_e," + "a.reissue_date_s as bef_reissue_date_s,"
        + "a.reissue_date_e as bef_reissue_date_e," + "a.text_form as bef_text_form,"
        + "a.validate_card as bef_validate_card," + "a.invalidate_card as bef_invalidate_card,"
        + "a.format_form as bef_format_form," + "a.zip_passwd as bef_zip_passwd";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(batchNo1, "a.batch_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAcctType("comm_acct_type_sel");
    commGroupCode("comm_group_code_sel");
    commSourceCode("comm_source_code_sel");
    commCsrddate("comm_carddate_sel");
    commTextForm("comm_text_form");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("acct_type_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_REPT_PAR", wp.colStr("batch_no"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_REPT_PAR", wp.colStr("batch_no"), "2"));
    wp.colSet("source_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_REPT_PAR", wp.colStr("batch_no"), "3"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("description").equals(wp.colStr("bef_description")))
      wp.colSet("opt_description", "Y");

    if (!wp.colStr("filename_beg").equals(wp.colStr("bef_filename_beg")))
      wp.colSet("opt_filename_beg", "Y");

    if (!wp.colStr("filename_end").equals(wp.colStr("bef_filename_end")))
      wp.colSet("opt_filename_end", "Y");

    if (!wp.colStr("acct_type_sel").equals(wp.colStr("bef_acct_type_sel")))
      wp.colSet("opt_acct_type_sel", "Y");
    commAcctType("comm_acct_type_sel");
    commAcctType("comm_bef_acct_type_sel");

    wp.colSet("bef_acct_type_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_REPT_PAR", wp.colStr("batch_no"), "1"));
    if (!wp.colStr("acct_type_sel_cnt").equals(wp.colStr("bef_acct_type_sel_cnt")))
      wp.colSet("opt_acct_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCode("comm_group_code_sel");
    commGroupCode("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_REPT_PAR", wp.colStr("batch_no"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("source_code_sel").equals(wp.colStr("bef_source_code_sel")))
      wp.colSet("opt_source_code_sel", "Y");
    commSourceCode("comm_source_code_sel");
    commSourceCode("comm_bef_source_code_sel");

    wp.colSet("bef_source_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_REPT_PAR", wp.colStr("batch_no"), "3"));
    if (!wp.colStr("source_code_sel_cnt").equals(wp.colStr("bef_source_code_sel_cnt")))
      wp.colSet("opt_source_code_sel_cnt", "Y");

    if (!wp.colStr("carddate_sel").equals(wp.colStr("bef_carddate_sel")))
      wp.colSet("opt_carddate_sel", "Y");
    commCsrddate("comm_carddate_sel");
    commCsrddate("comm_bef_carddate_sel");

    if (!wp.colStr("issue_date_s").equals(wp.colStr("bef_issue_date_s")))
      wp.colSet("opt_issue_date_s", "Y");

    if (!wp.colStr("issue_date_e").equals(wp.colStr("bef_issue_date_e")))
      wp.colSet("opt_issue_date_e", "Y");

    if (!wp.colStr("change_date_s").equals(wp.colStr("bef_change_date_s")))
      wp.colSet("opt_change_date_s", "Y");

    if (!wp.colStr("change_date_e").equals(wp.colStr("bef_change_date_e")))
      wp.colSet("opt_change_date_e", "Y");

    if (!wp.colStr("stop_date_s").equals(wp.colStr("bef_stop_date_s")))
      wp.colSet("opt_stop_date_s", "Y");

    if (!wp.colStr("stop_date_e").equals(wp.colStr("bef_stop_date_e")))
      wp.colSet("opt_stop_date_e", "Y");

    if (!wp.colStr("apply_date_s").equals(wp.colStr("bef_apply_date_s")))
      wp.colSet("opt_apply_date_s", "Y");

    if (!wp.colStr("apply_date_e").equals(wp.colStr("bef_apply_date_e")))
      wp.colSet("opt_apply_date_e", "Y");

    if (!wp.colStr("subissue_date_s").equals(wp.colStr("bef_subissue_date_s")))
      wp.colSet("opt_subissue_date_s", "Y");

    if (!wp.colStr("subissue_date_e").equals(wp.colStr("bef_subissue_date_e")))
      wp.colSet("opt_subissue_date_e", "Y");

    if (!wp.colStr("reissue_date_s").equals(wp.colStr("bef_reissue_date_s")))
      wp.colSet("opt_reissue_date_s", "Y");

    if (!wp.colStr("reissue_date_e").equals(wp.colStr("bef_reissue_date_e")))
      wp.colSet("opt_reissue_date_e", "Y");

    if (!wp.colStr("text_form").equals(wp.colStr("bef_text_form")))
      wp.colSet("opt_text_form", "Y");
    commTextForm("comm_text_form");
    commTextForm("comm_bef_text_form");

    if (!wp.colStr("validate_card").equals(wp.colStr("bef_validate_card")))
      wp.colSet("opt_validate_card", "Y");

    if (!wp.colStr("invalidate_card").equals(wp.colStr("bef_invalidate_card")))
      wp.colSet("opt_invalidate_card", "Y");

    if (!wp.colStr("format_form").equals(wp.colStr("bef_format_form")))
      wp.colSet("opt_format_form", "Y");

    if (!wp.colStr("zip_passwd").equals(wp.colStr("bef_zip_passwd")))
      wp.colSet("opt_zip_passwd", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("description", "");
      wp.colSet("filename_beg", "");
      wp.colSet("filename_end", "");
      wp.colSet("acct_type_sel", "");
      wp.colSet("acct_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("source_code_sel", "");
      wp.colSet("source_code_sel_cnt", "");
      wp.colSet("carddate_sel", "");
      wp.colSet("issue_date_s", "");
      wp.colSet("issue_date_e", "");
      wp.colSet("change_date_s", "");
      wp.colSet("change_date_e", "");
      wp.colSet("stop_date_s", "");
      wp.colSet("stop_date_e", "");
      wp.colSet("apply_date_s", "");
      wp.colSet("apply_date_e", "");
      wp.colSet("subissue_date_s", "");
      wp.colSet("subissue_date_e", "");
      wp.colSet("reissue_date_s", "");
      wp.colSet("reissue_date_e", "");
      wp.colSet("text_form", "");
      wp.colSet("validate_card", "");
      wp.colSet("invalidate_card", "");
      wp.colSet("format_form", "");
      wp.colSet("zip_passwd", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("description").length() == 0)
      wp.colSet("opt_description", "Y");

    if (wp.colStr("filename_beg").length() == 0)
      wp.colSet("opt_filename_beg", "Y");

    if (wp.colStr("filename_end").length() == 0)
      wp.colSet("opt_filename_end", "Y");

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("opt_acct_type_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("source_code_sel").length() == 0)
      wp.colSet("opt_source_code_sel", "Y");


    if (wp.colStr("carddate_sel").length() == 0)
      wp.colSet("opt_carddate_sel", "Y");

    if (wp.colStr("issue_date_s").length() == 0)
      wp.colSet("opt_issue_date_s", "Y");

    if (wp.colStr("issue_date_e").length() == 0)
      wp.colSet("opt_issue_date_e", "Y");

    if (wp.colStr("change_date_s").length() == 0)
      wp.colSet("opt_change_date_s", "Y");

    if (wp.colStr("change_date_e").length() == 0)
      wp.colSet("opt_change_date_e", "Y");

    if (wp.colStr("stop_date_s").length() == 0)
      wp.colSet("opt_stop_date_s", "Y");

    if (wp.colStr("stop_date_e").length() == 0)
      wp.colSet("opt_stop_date_e", "Y");

    if (wp.colStr("apply_date_s").length() == 0)
      wp.colSet("opt_apply_date_s", "Y");

    if (wp.colStr("apply_date_e").length() == 0)
      wp.colSet("opt_apply_date_e", "Y");

    if (wp.colStr("subissue_date_s").length() == 0)
      wp.colSet("opt_subissue_date_s", "Y");

    if (wp.colStr("subissue_date_e").length() == 0)
      wp.colSet("opt_subissue_date_e", "Y");

    if (wp.colStr("reissue_date_s").length() == 0)
      wp.colSet("opt_reissue_date_s", "Y");

    if (wp.colStr("reissue_date_e").length() == 0)
      wp.colSet("opt_reissue_date_e", "Y");

    if (wp.colStr("text_form").length() == 0)
      wp.colSet("opt_text_form", "Y");

    if (wp.colStr("validate_card").length() == 0)
      wp.colSet("opt_validate_card", "Y");

    if (wp.colStr("invalidate_card").length() == 0)
      wp.colSet("opt_invalidate_card", "Y");

    if (wp.colStr("format_form").length() == 0)
      wp.colSet("opt_format_form", "Y");

    if (wp.colStr("zip_passwd").length() == 0)
      wp.colSet("opt_zip_passwd", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp4060Func func = new mktp02.Mktp4060Func(wp);

    String[] lsBatchNo = wp.itemBuff("batch_no");
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
        ilErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }

      func.varsSet("batch_no", lsBatchNo[rr]);
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
        commAcctTypeS("comm_acct_type_sel");
        commGroupCodeS("comm_group_code_sel");
        commSourceCodeS("comm_source_code_sel");
        commCardDate("comm_carddate_sel");
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
      if ((wp.respHtml.equals("mktp4060"))) {
        if (wp.colStr("format_form").length() > 0) {
          wp.optionKey = wp.colStr("format_form");
        }
        this.dddwList("dddw_data_code", "mkt_rept_parcode", "trim(data_type)", "",
            " group by data_type");
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
  public void commSourceCode(String cde1) throws Exception {
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
  public void commCsrddate(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4", "5", "6"};
    String[] txt = {"發卡期間", "續卡期間", "停卡期間", "進件期間", "附卡人發卡期間", "重製期間"};
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
  public void commTextForm(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", ""};
    String[] txt = {"ID", "帳戶", "卡號", ""};
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
  public void commAcctTypeS(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "指定", ">排除"};
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
  public void commGroupCodeS(String cde1) throws Exception {
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
  public void commSourceCodeS(String cde1) throws Exception {
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
  public void commCardDate(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4", "5", "6"};
    String[] txt = {"發卡期間", "續卡期間", "停卡期間", "進件期間", "附卡人發卡期間", "重製期間"};
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
