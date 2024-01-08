/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *     
***************************************************************************/
package mktp01;

import mktp01.Mktp0610Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0610 extends BaseProc {
  private String PROGNAME = "網路辦卡專案活動代碼參數覆核處理程式108/09/03 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0610Func func = null;
  String rowid;
  String projectNo;
  String fstAprFlag = "";
  String orgTabName = "web_apply_parm_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_project_no"), "a.project_no", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%");

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
        + "a.aud_type," + "a.project_no," + "a.project_name," + "a.start_s_date,"
        + "a.start_e_date," + "a.bonus_cond," + "a.fund_cond," + "a.crt_user";

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
        + "a.project_no as project_no," + "a.crt_user," + "a.project_name," + "a.start_s_date,"
        + "a.start_e_date," + "a.project_desc," + "a.apply_type_cond";

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
    commTypeCond("comm_apply_type_cond");
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
    controlTabName = "web_apply_parm";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.project_no as project_no," + "a.crt_user as bef_crt_user,"
        + "a.project_name as bef_project_name," + "a.start_s_date as bef_start_s_date,"
        + "a.start_e_date as bef_start_e_date," + "a.project_desc as bef_project_desc,"
        + "a.apply_type_cond as bef_apply_type_cond";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(projectNo, "a.project_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commTypeCond("comm_apply_type_cond");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("apply_type_cond_cnt",
        listMktBnData("mkt_bn_data_t", "WEB_APPLY_PARM", wp.colStr("project_no"), "4"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("project_name").equals(wp.colStr("bef_project_name")))
      wp.colSet("opt_project_name", "Y");

    if (!wp.colStr("start_s_date").equals(wp.colStr("bef_start_s_date")))
      wp.colSet("opt_start_s_date", "Y");

    if (!wp.colStr("start_e_date").equals(wp.colStr("bef_start_e_date")))
      wp.colSet("opt_start_e_date", "Y");

    if (!wp.colStr("project_desc").equals(wp.colStr("bef_project_desc")))
      wp.colSet("opt_project_desc", "Y");

    if (!wp.colStr("apply_type_cond").equals(wp.colStr("bef_apply_type_cond")))
      wp.colSet("opt_apply_type_cond", "Y");
    commTypeCond("comm_apply_type_cond");
    commTypeCond("comm_bef_apply_type_cond");

    wp.colSet("bef_apply_type_cond_cnt",
        listMktBnData("mkt_bn_data", "WEB_APPLY_PARM", wp.colStr("project_no"), "4"));
    if (!wp.colStr("apply_type_cond_cnt").equals(wp.colStr("bef_apply_type_cond_cnt")))
      wp.colSet("opt_apply_type_cond_cnt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("project_name", "");
      wp.colSet("start_s_date", "");
      wp.colSet("start_e_date", "");
      wp.colSet("project_desc", "");
      wp.colSet("apply_type_cond", "");
      wp.colSet("apply_type_cond_cnt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("project_name").length() == 0)
      wp.colSet("opt_project_name", "Y");

    if (wp.colStr("start_s_date").length() == 0)
      wp.colSet("opt_start_s_date", "Y");

    if (wp.colStr("start_e_date").length() == 0)
      wp.colSet("opt_start_e_date", "Y");

    if (wp.colStr("project_desc").length() == 0)
      wp.colSet("opt_project_desc", "Y");

    if (wp.colStr("apply_type_cond").length() == 0)
      wp.colSet("opt_apply_type_cond", "Y");


  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp01.Mktp0610Func func = new mktp01.Mktp0610Func(wp);

    String[] lsProjectNo = wp.itemBuff("project_no");
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
  public void commTypeCond(String cde1) throws Exception {
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

} // End of class
