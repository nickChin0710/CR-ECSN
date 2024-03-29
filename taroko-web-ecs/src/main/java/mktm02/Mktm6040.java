/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
* 110/11/05  V1.00.04  jiangyingdong       sql injection                   * 
***************************************************************************/
package mktm02;

import mktm02.Mktm6040Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.xmlbeans.impl.jam.internal.parser.ParamStructPool;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6040 extends BaseEdit {

  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "專案免年費參數檔維護處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6040Func func = null;
  String rowid;
  String projectNo;
  String fstAprFlag = "";
  String orgTabName = "cyc_anul_project";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];
  String upGroupType = "0";

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
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_project_no"), "a.project_no", "like%")
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
        + "a.project_no," + "a.project_name," + "a.acct_type_sel," + "a.group_code_sel,"
        + "a.source_code_sel," + "a.recv_s_date," + "a.recv_e_date," + "a.apr_date," + "a.apr_user";

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

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.project_no as project_no," + "a.project_name," + "a.acct_type_sel,"
        + "a.group_code_sel," + "a.source_code_sel," + "a.recv_month_tag," + "a.recv_s_date,"
        + "a.recv_e_date," + "a.issue_date_tag," + "a.issue_date_s," + "a.issue_date_e,"
        + "a.mcard_cond," + "a.scard_cond," + "a.cnt_months_tag," + "a.cnt_months,"
        + "a.accumulate_cnt," + "a.average_amt," + "a.cnt_bl_flag," + "a.cnt_ca_flag,"
        + "a.cnt_it_flag," + "a.cnt_ao_flag," + "a.cnt_id_flag," + "a.cnt_ot_flag,"
        + "a.amt_months_tag," + "a.amt_months," + "a.accumulate_amt," + "a.amt_bl_flag,"
        + "a.amt_ca_flag," + "a.amt_it_flag," + "a.amt_ao_flag," + "a.amt_id_flag,"
        + "a.amt_ot_flag," + "a.adv_months_tag," + "a.adv_months," + "a.adv_cnt," + "a.adv_amt,"
        + "a.mcode," + "a.free_fee_cnt," + "a.crt_user," + "a.crt_date," + "a.apr_user,"
        + "a.apr_date";

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
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    checkButtonOff();
    projectNo = wp.colStr("project_no");
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.project_no as project_no,"
        + "a.project_name as project_name," + "a.acct_type_sel as acct_type_sel,"
        + "a.group_code_sel as group_code_sel," + "a.source_code_sel as source_code_sel,"
        + "a.recv_month_tag as recv_month_tag," + "a.recv_s_date as recv_s_date,"
        + "a.recv_e_date as recv_e_date," + "a.issue_date_tag as issue_date_tag,"
        + "a.issue_date_s as issue_date_s," + "a.issue_date_e as issue_date_e,"
        + "a.mcard_cond as mcard_cond," + "a.scard_cond as scard_cond,"
        + "a.cnt_months_tag as cnt_months_tag," + "a.cnt_months as cnt_months,"
        + "a.accumulate_cnt as accumulate_cnt," + "a.average_amt as average_amt,"
        + "a.cnt_bl_flag as cnt_bl_flag," + "a.cnt_ca_flag as cnt_ca_flag,"
        + "a.cnt_it_flag as cnt_it_flag," + "a.cnt_ao_flag as cnt_ao_flag,"
        + "a.cnt_id_flag as cnt_id_flag," + "a.cnt_ot_flag as cnt_ot_flag,"
        + "a.amt_months_tag as amt_months_tag," + "a.amt_months as amt_months,"
        + "a.accumulate_amt as accumulate_amt," + "a.amt_bl_flag as amt_bl_flag,"
        + "a.amt_ca_flag as amt_ca_flag," + "a.amt_it_flag as amt_it_flag,"
        + "a.amt_ao_flag as amt_ao_flag," + "a.amt_id_flag as amt_id_flag,"
        + "a.amt_ot_flag as amt_ot_flag," + "a.adv_months_tag as adv_months_tag,"
        + "a.adv_months as adv_months," + "a.adv_cnt as adv_cnt," + "a.adv_amt as adv_amt,"
        + "a.mcode as mcode," + "a.free_fee_cnt as free_fee_cnt," + "a.crt_user as crt_user,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(projectNo, "a.project_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    projectNo = wp.itemStr("project_no");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      projectNo = wp.itemStr("project_no");
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
    projectNo = wp.itemStr("project_no");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      projectNo = wp.itemStr("project_no");
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

    if ((wp.itemStr("project_no").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "cyc_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "cyc_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'CYC_ANUL_PROJECT' ";
    if (wp.respHtml.equals("mktm6040_acty"))
      wp.whereStr += " and data_type  = '1' ";
    if (wp.respHtml.equals("mktm6040_gpcd"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("mktm6040_srcd"))
      wp.whereStr += " and data_type  = '3' ";
    params.clear();
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("project_no"));
//    whereCnt += " and  data_key = '" + wp.itemStr("project_no") + "'";
    whereCnt += " and  data_key = ?";
    wp.whereStr += " order by 4,5,6 ";
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm6040_acty"))
      commDataCode01("comm_data_code");
    if (wp.respHtml.equals("mktm6040_gpcd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6040_srcd"))
      commDataCode3("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm02.Mktm6040Func func = new mktm02.Mktm6040Func(wp);
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
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm02.Mktm6040Func func = new mktm02.Mktm6040Func(wp);

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
      if ((wp.respHtml.equals("mktm6040_acty"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_acct_type", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6040_gpcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6040_srcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_source_code3", "ptr_src_code", "trim(source_code)", "trim(source_name)",
            " where 1 = 1 ");
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
  public void commDataCode01(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
//          + " where 1 = 1 " + " and   acct_type = '" + wp.colStr(ii, "data_code") + "'";
      		+ " where 1 = 1 " + " and   acct_type = ?";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      params.clear();
      params.add(wp.colStr(ii, "data_code"));
      sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
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
//          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
		    + " where 1 = 1 " + " and   group_code = ?";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      params.clear();
      params.add(wp.colStr(ii, "data_code"));
      sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode3(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " source_name as column_source_name " + " from ptr_src_code "
//          + " where 1 = 1 " + " and   source_code = '" + wp.colStr(ii, "data_code") + "'";
  		  + " where 1 = 1 " + " and   source_code = ?";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      params.clear();
      params.add(wp.colStr(ii, "data_code"));
      sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_source_name");
      wp.colSet(ii, columnData1, columnData);
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
    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("acct_type_sel", "0");

    if (wp.colStr("acct_type_sel").equals("0")) {
      buttonOff("btnacty_disable");
    } else {
      wp.colSet("btnacty_disable", "");
    }

    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("group_code_sel", "0");

    if (wp.colStr("group_code_sel").equals("0")) {
      buttonOff("btngpcd_disable");
    } else {
      wp.colSet("btngpcd_disable", "");
    }

    if (wp.colStr("source_code_sel").length() == 0)
      wp.colSet("source_code_sel", "0");

    if (wp.colStr("source_code_sel").equals("0")) {
      buttonOff("btnsrcd_disable");
    } else {
      wp.colSet("btnsrcd_disable", "");
    }

    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    wp.colSet("free_fee_cnt", "1");

    buttonOff("btnacty_disable");
    buttonOff("btngpcd_disable");
    buttonOff("btnsrcd_disable");
    return;
  }
  // ************************************************************************

} // End of class
