/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110/11/22  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktp01;

import mktp01.Mktp0620Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0620 extends BaseProc {
  private String PROGNAME = "網路辦卡專案人工登錄覆核作業處理程式108/09/03 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0620Func func = null;
  String rowid;
  String recordSeqno;
  String fstAprFlag = "";
  String orgTabName = "web_apply_idno_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int error_Cnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
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
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%") + sqlStrend(
            wp.itemStr("ex_record_date_s"), wp.itemStr("ex_record_date_e"), "a.record_date");

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
        + "a.aud_type," + "a.record_seqno," + "a.project_no," + "a.id_no," + "a.chi_name,"
        + "a.record_date," + "a.apply_type," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.project_no,a.crt_user,a.record_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commProjectNo("comm_project_no");
    commApplyType("comm_apply_type");

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
      if (wp.itemStr("kk_record_seqno").length() == 0) {
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
        + "a.record_seqno as record_seqno," + "a.crt_user," + "a.project_no," + "a.id_no,"
        + "a.record_date," + "a.chi_name," + "a.from_mark," + "a.birthday," + "a.sex,"
        + "a.office_area_code," + "a.office_tel_no," + "a.office_tel_ext," + "a.home_area_code,"
        + "a.home_tel_no," + "a.home_tel_ext," + "a.cellar_phone," + "a.e_mail_addr,"
        + "a.apply_type";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(recordSeqno, "a.record_seqno");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commFromMark("comm_from_mark");
    commSex("comm_sex");
    commProjectNo("comm_project_no");
    commApplyType("comm_apply_type");
    checkButtonOff();
    recordSeqno = wp.colStr("record_seqno");
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
    controlTabName = "web_apply_idno";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.record_seqno as record_seqno," + "a.crt_user as bef_crt_user,"
        + "a.project_no as bef_project_no," + "a.id_no as bef_id_no,"
        + "a.record_date as bef_record_date," + "a.chi_name as bef_chi_name,"
        + "a.from_mark as bef_from_mark," + "a.birthday as bef_birthday," + "a.sex as bef_sex,"
        + "a.office_area_code as bef_office_area_code," + "a.office_tel_no as bef_office_tel_no,"
        + "a.office_tel_ext as bef_office_tel_ext," + "a.home_area_code as bef_home_area_code,"
        + "a.home_tel_no as bef_home_tel_no," + "a.home_tel_ext as bef_home_tel_ext,"
        + "a.cellar_phone as bef_cellar_phone," + "a.e_mail_addr as bef_e_mail_addr,"
        + "a.apply_type as bef_apply_type";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(recordSeqno, "a.record_seqno");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commProjectNo("comm_project_no");
    commFromMark("comm_from_mark");
    commSex("comm_sex");
    commApplyType("comm_apply_type");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("project_no").equals(wp.colStr("bef_project_no")))
      wp.colSet("opt_project_no", "Y");
    commProjectNo("comm_project_no");
    commProjectNo("comm_bef_project_no", 1);

    if (!wp.colStr("id_no").equals(wp.colStr("bef_id_no")))
      wp.colSet("opt_id_no", "Y");

    if (!wp.colStr("record_date").equals(wp.colStr("bef_record_date")))
      wp.colSet("opt_record_date", "Y");

    if (!wp.colStr("chi_name").equals(wp.colStr("bef_chi_name")))
      wp.colSet("opt_chi_name", "Y");

    if (!wp.colStr("from_mark").equals(wp.colStr("bef_from_mark")))
      wp.colSet("opt_from_mark", "Y");
    commFromMark("comm_from_mark");
    commFromMark("comm_bef_from_mark");

    if (!wp.colStr("birthday").equals(wp.colStr("bef_birthday")))
      wp.colSet("opt_birthday", "Y");

    if (!wp.colStr("sex").equals(wp.colStr("bef_sex")))
      wp.colSet("opt_sex", "Y");
    commSex("comm_sex");
    commSex("comm_bef_sex");

    if (!wp.colStr("office_area_code").equals(wp.colStr("bef_office_area_code")))
      wp.colSet("opt_office_area_code", "Y");

    if (!wp.colStr("office_tel_no").equals(wp.colStr("bef_office_tel_no")))
      wp.colSet("opt_office_tel_no", "Y");

    if (!wp.colStr("office_tel_ext").equals(wp.colStr("bef_office_tel_ext")))
      wp.colSet("opt_office_tel_ext", "Y");

    if (!wp.colStr("home_area_code").equals(wp.colStr("bef_home_area_code")))
      wp.colSet("opt_home_area_code", "Y");

    if (!wp.colStr("home_tel_no").equals(wp.colStr("bef_home_tel_no")))
      wp.colSet("opt_home_tel_no", "Y");

    if (!wp.colStr("home_tel_ext").equals(wp.colStr("bef_home_tel_ext")))
      wp.colSet("opt_home_tel_ext", "Y");

    if (!wp.colStr("cellar_phone").equals(wp.colStr("bef_cellar_phone")))
      wp.colSet("opt_cellar_phone", "Y");

    if (!wp.colStr("e_mail_addr").equals(wp.colStr("bef_e_mail_addr")))
      wp.colSet("opt_e_mail_addr", "Y");

    if (!wp.colStr("apply_type").equals(wp.colStr("bef_apply_type")))
      wp.colSet("opt_apply_type", "Y");
    commApplyType("comm_apply_type");
    commApplyType("comm_bef_apply_type", 1);

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("project_no", "");
      wp.colSet("id_no", "");
      wp.colSet("record_date", "");
      wp.colSet("chi_name", "");
      wp.colSet("from_mark", "");
      wp.colSet("birthday", "");
      wp.colSet("sex", "");
      wp.colSet("office_area_code", "");
      wp.colSet("office_tel_no", "");
      wp.colSet("office_tel_ext", "");
      wp.colSet("home_area_code", "");
      wp.colSet("home_tel_no", "");
      wp.colSet("home_tel_ext", "");
      wp.colSet("cellar_phone", "");
      wp.colSet("e_mail_addr", "");
      wp.colSet("apply_type", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("project_no").length() == 0)
      wp.colSet("opt_project_no", "Y");

    if (wp.colStr("id_no").length() == 0)
      wp.colSet("opt_id_no", "Y");

    if (wp.colStr("record_date").length() == 0)
      wp.colSet("opt_record_date", "Y");

    if (wp.colStr("chi_name").length() == 0)
      wp.colSet("opt_chi_name", "Y");

    if (wp.colStr("from_mark").length() == 0)
      wp.colSet("opt_from_mark", "Y");

    if (wp.colStr("birthday").length() == 0)
      wp.colSet("opt_birthday", "Y");

    if (wp.colStr("sex").length() == 0)
      wp.colSet("opt_sex", "Y");

    if (wp.colStr("office_area_code").length() == 0)
      wp.colSet("opt_office_area_code", "Y");

    if (wp.colStr("office_tel_no").length() == 0)
      wp.colSet("opt_office_tel_no", "Y");

    if (wp.colStr("office_tel_ext").length() == 0)
      wp.colSet("opt_office_tel_ext", "Y");

    if (wp.colStr("home_area_code").length() == 0)
      wp.colSet("opt_home_area_code", "Y");

    if (wp.colStr("home_tel_no").length() == 0)
      wp.colSet("opt_home_tel_no", "Y");

    if (wp.colStr("home_tel_ext").length() == 0)
      wp.colSet("opt_home_tel_ext", "Y");

    if (wp.colStr("cellar_phone").length() == 0)
      wp.colSet("opt_cellar_phone", "Y");

    if (wp.colStr("e_mail_addr").length() == 0)
      wp.colSet("opt_e_mail_addr", "Y");

    if (wp.colStr("apply_type").length() == 0)
      wp.colSet("opt_apply_type", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp01.Mktp0620Func func = new mktp01.Mktp0620Func(wp);

    String[] lsRecordSeqno = wp.itemBuff("record_seqno");
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

      func.varsSet("record_seqno", lsRecordSeqno[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A"))
        rc = func.dbInsertA4();
      else if (lsAudType[rr].equals("U"))
        rc = func.dbUpdateU4();
      else if (lsAudType[rr].equals("D"))
        rc = func.dbDeleteD4();

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commProjectNo("comm_project_no");
        commApplyType("comm_apply_type");
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
      if ((wp.respHtml.equals("mktp0620"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_project_no").length() > 0) {
          wp.optionKey = wp.colStr("ex_project_no");
        }
        this.dddwList("dddw_proj_no_b", "web_apply_parm", "trim(project_no)", "trim(project_name)",
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
  public void commProjectNo(String projectNo) throws Exception {
    commProjectNo(projectNo, 0);
    return;
  }

  // ************************************************************************
  public void commProjectNo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " project_name as column_project_name " + " from web_apply_parm "
          + " where 1 = 1 " + " and   project_no = ? ";
      if (wp.colStr(ii, befStr + "project_no").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "project_no") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_project_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commApplyType(String type) throws Exception {
    commApplyType(type, 0);
    return;
  }

  // ************************************************************************
  public void commApplyType(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_id = ? "
          + " and   wf_type = 'WEB_APPLY_TYPE' ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "apply_type") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commFromMark(String cde1) throws Exception {
    String[] cde = {"W", "M"};
    String[] txt = {"網路登錄", "人工登錄"};
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
  public void commSex(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"男", "女"};
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

}  // End of class
