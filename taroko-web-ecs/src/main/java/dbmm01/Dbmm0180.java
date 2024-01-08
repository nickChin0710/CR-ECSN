/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/02  V1.00.01   Allen Ho      Initial                              *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
* 110-10-22  V1.00.06  Yangbo       joint sql replace to parameters way *
* 111-12-27  V1.00.07  Zuwei Su       無法顯示輸入參數最新筆數             *
* 112-05-08  V1.00.08  Zuwei Su       特店群組來源條件修改，一般消費群組data_type寫入'P'              *
***************************************************************************/
package dbmm01;

import dbmm01.Dbmm0180Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmm0180 extends BaseEdit
{
  private final String PROGNAME = "VD紅利加贈參數檔-生日處理程式108/12/02 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  dbmm01.Dbmm0180Func func = null;
  String rowid;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "dbm_bpbir";
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
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD2")) {/* 匯入檔案 */
      procUploadFile(2);
      checkButtonOff();
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
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
        + "a.active_code," + "a.active_name," + "a.bp_amt," + "a.bp_pnt," + "a.add_times,"
        + "a.add_point," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCrtuser("comm_crt_user");

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
        + "a.active_code as active_code," + "a.active_name," + "a.give_code," + "a.tax_flag,"
        + "a.active_s_date," + "a.active_e_date," + "a.bir_month_bef," + "a.total_amt,"
        + "a.birth_flag," + "a.acct_type_sel," + "a.group_code_sel," + "a.merchant_sel,"
        + "a.mcht_group_sel," +"a.platform_kind_sel," + "a.mcc_code_sel," + "a.pos_entry_sel," + "a.bp_amt," + "a.bp_pnt,"
        + "a.add_times," + "a.add_point," + "a.crt_user," + "a.crt_date," + "a.apr_user,"
        + "a.apr_date";

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
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    checkButtonOff();
    activeCode = wp.colStr("active_code");
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    listWkdata();
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.active_code as active_code,"
        + "a.active_name as active_name," + "a.give_code as give_code," + "a.tax_flag as tax_flag,"
        + "a.active_s_date as active_s_date," + "a.active_e_date as active_e_date,"
        + "a.bir_month_bef as bir_month_bef," + "a.total_amt as total_amt,"
        + "a.birth_flag as birth_flag," + "a.acct_type_sel as acct_type_sel,"
        + "a.group_code_sel as group_code_sel," + "a.merchant_sel as merchant_sel,"
        + "a.mcht_group_sel as mcht_group_sel,"  + "a.platform_kind_sel as platform_kind_sel," + "a.mcc_code_sel as mcc_code_sel,"
        + "a.pos_entry_sel as pos_entry_sel," + "a.bp_amt as bp_amt," + "a.bp_pnt as bp_pnt,"
        + "a.add_times as add_times," + "a.add_point as add_point," + "a.crt_user as crt_user,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commfuncAudType("aud_type");
    listWkdataAft();
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    activeCode = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      activeCode = wp.itemStr("active_code");
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
    activeCode = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      activeCode = wp.itemStr("active_code");
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

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "dbm_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "dbm_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'DBM_BPBIR' ";
    if (wp.respHtml.equals("dbmm0180_acty"))
      wp.whereStr += " and data_type  = '3' ";
    if (wp.respHtml.equals("dbmm0180_grop"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("dbmm0180_aaa1"))
      wp.whereStr += " and data_type  = '6' ";
    if (wp.respHtml.equals("dbmm0180_platform"))
        wp.whereStr += " and data_type  = 'P' ";
    if (wp.respHtml.equals("dbmm0180_mccd"))
      wp.whereStr += " and data_type  = '5' ";
    if (wp.respHtml.equals("dbmm0180_enty"))
      wp.whereStr += " and data_type  = '4' ";
    String whereCnt = wp.whereStr;
    whereCnt += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("dbmm0180_acty"))
      commAcctType("comm_data_code");
    if (wp.respHtml.equals("dbmm0180_grop"))
      commGroupCode("comm_data_code");
    if (wp.respHtml.equals("dbmm0180_aaa1"))
      commMechtGp("comm_data_code");
    if (wp.respHtml.equals("dbmm0180_platform"))
        commMechtGp("comm_data_code");
    if (wp.respHtml.equals("dbmm0180_mccd"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("dbmm0180_enty"))
      commEntryMode("comm_data_code");
  }
  
  // ************************************************************************
  public void updateFuncU2() throws Exception {
    dbmm01.Dbmm0180Func func = new dbmm01.Dbmm0180Func(wp);
    int lsOk = 0, err = 0;

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
          err++;
          continue;
        }
    }

    if (err > 0) {
      alertErr("資料值重複 : " + err);
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
        lsOk++;
      else
        err++;

      // 有失敗rollback，無失敗commit
      sqlCommit(lsOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + lsOk + "), 失敗(" + err + ")");

    // SAVE後 SELECT
    dataReadR2();
  }

  // ************************************************************************
  public void dataReadR3() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "dbm_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "dbm_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'DBM_BPBIR' ";
    if (wp.respHtml.equals("dbmm0180_mcht"))
      wp.whereStr += " and data_type  = '1' ";
    String whereCnt = wp.whereStr;
    whereCnt += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6,7 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    dbmm01.Dbmm0180Func func = new dbmm01.Dbmm0180Func(wp);
    int lsOk = 0, err = 0;

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
          err++;
          continue;
        }
    }

    if (err > 0) {
      alertErr("資料值重複 : " + err);
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
        lsOk++;
      else
        err++;

      // 有失敗rollback，無失敗commit
      sqlCommit(lsOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + lsOk + "), 失敗(" + err + ")");

    // SAVE後 SELECT
    dataReadR3();
  }

  // ************************************************************************
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1);

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    dbmm01.Dbmm0180Func func = new dbmm01.Dbmm0180Func(wp);
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
      if ((wp.respHtml.equals("dbmm0180_nadd")) || (wp.respHtml.equals("dbmm0180_detl"))) {
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("give_code").length() > 0) {
          wp.optionKey = wp.colStr("give_code");
          wp.initOption = "";
        }
        this.dddwList("dddw_give_code", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='GIVE_CODE'");
      }
      if ((wp.respHtml.equals("dbmm0180_acty"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_acct_type", "dbp_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("dbmm0180_grop"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3",
            "select a.group_code as db_code,a.group_code||'-'||max(a.group_name) as db_desc from ptr_group_code a,dbc_card_type b where a.group_code=b.group_code group by a.group_code");
      }
      if ((wp.respHtml.equals("dbmm0180_mccd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("dbmm0180_enty"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_entry_mode", "cca_entry_mode", "trim(entry_mode)", "trim(mode_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("dbmm0180_aaa1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
            " where platform_flag != '2' ");
      }
      if ((wp.respHtml.equals("dbmm0180_platform")))
      {
          wp.initOption = "";
          wp.optionKey = "";
          this.dddwList("dddw_platform_group", "mkt_mcht_gp", "trim(mcht_group_id)",
              "trim(mcht_group_desc)", " where platform_flag='2' ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    return "";
  }

  // ************************************************************************
  void commfuncAudType(String audType) {
    if (audType == null || audType.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + audType, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, audType).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + audType, txt[inti]);
          break;
        }
    }
  }

  // ************************************************************************
  public void commAcctType(String commDataCode) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from dbp_acct_type "
          + " where 1 = 1 "
//              + " and   acct_type = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "acct_type");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, commDataCode, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commGroupCode(String commDataCode) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 "
//              + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "group_code");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, commDataCode, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode07(String commDataCode) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcc_remark as column_mcc_remark " + " from cca_mcc_risk "
          + " where 1 = 1 "
//              + " and   mcc_code = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "mcc_code");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcc_remark");
      wp.colSet(ii, commDataCode, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commEntryMode(String commDataCode) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mode_desc as column_mode_desc " + " from cca_entry_mode "
          + " where 1 = 1 "
//              + " and   entry_mode = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "entry_mode");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mode_desc");
      wp.colSet(ii, commDataCode, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commMechtGp(String commDataCode) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcht_group_desc as column_mcht_group_desc " + " from mkt_mcht_gp "
          + " where 1 = 1 "
//              + " and   mcht_group_id = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "mcht_group_id");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
      wp.colSet(ii, commDataCode, columnData);
    }
    return;
  }

  // ************************************************************************
  public void procUploadFile(int loadType) throws Exception {
    if (wp.colStr(0, "ser_num").length() > 0)
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    if (loadType == 2)
      fileDataImp2();
  }

  // ************************************************************************
  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
    } catch (Exception ex) {
      wp.log("file_upLoad: error=" + ex.getMessage());
      return -1;
    }

    return func.rc;
  }

  // ************************************************************************
  void fileDataImp2() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");

    if (fi == -1)
      return;

    String sysUploadType = wp.itemStr("sys_upload_type");
    String sysUploadAlias = wp.itemStr("sys_upload_alias");

    if (sysUploadAlias.equals("mcht")) {
      // if has pre check procudure, write in here
    }
    dbmm01.Dbmm0180Func func = new dbmm01.Dbmm0180Func(wp);

    if (sysUploadAlias.equals("mcht"))
      func.dbDeleteD2Mcht("DBM_BN_DATA_T");

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    tranSeqStr = comr.getSeqno("MKT_MODSEQ");

    String all = "";
    int isok = 0, cnt = 0, err = 0;
    int lineCnt = 0;
    while (true) {
      all = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y"))
        break;
      lineCnt++;
      if (sysUploadAlias.equals("mcht")) {
        if (lineCnt <= 0)
          continue;
        if (all.length() < 2)
          continue;
      }

      cnt++;

      for (int inti = 0; inti < 10; inti++)
        logMsg[inti] = "";
      logMsg[10] = String.format("%02d", lineCnt);

      if (sysUploadAlias.equals("mcht"))
        if (checkUploadfileMcht(all) != 0)
          continue;

      if (errorCnt == 0) {
        if (sysUploadAlias.equals("mcht")) {
          if (func.dbInsertI2Mcht("DBM_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            isok++;
          else
            err++;
        }
      }
    }

    if (errorCnt > 0) {
      if (sysUploadAlias.equals("mcht"))
        func.dbDeleteD2Mcht("DBM_BN_DATA_T");
      func.dbInsertEcsNotifyLog(tranSeqStr, errorCnt);
    }

    sqlCommit(1); // 1:commit else rollback

    alertMsg("資料匯入處理筆數 : " + cnt + ", 成功(" + isok + "), 重複(" + err + "), 失敗("
        + (cnt - isok - err) + ")");

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);


    return;
  }

  // ************************************************************************
  int checkUploadfileMcht(String all) throws Exception {
    dbmm01.Dbmm0180Func func = new dbmm01.Dbmm0180Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "data_code";
    uploadFileCol[1] = "data_code2";

    // ======== [I]nsert table column ========
    uploadFileCol[2] = "table_name";
    uploadFileCol[3] = "data_key";
    uploadFileCol[4] = "data_type";
    uploadFileCol[5] = "crt_date";
    uploadFileCol[6] = "crt_user";

    // ==== insert table content default =====
    uploadFileDat[2] = "DBM_BPBIR";
    uploadFileDat[3] = wp.itemStr("active_code");
    uploadFileDat[4] = "1";
    uploadFileDat[5] = wp.sysDate;
    uploadFileDat[6] = wp.loginUser;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(all, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);
    // ******************************************************************
    if ((uploadFileDat[1].length() != 0) && (uploadFileDat[1].length() < 8))
      uploadFileDat[1] = "00000000".substring(0, 8 - uploadFileDat[1].length()) + uploadFileDat[1];


    return 0;
  }

  // ************************************************************************
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
      buttonOff("btngrop_disable");
    } else {
      wp.colSet("btngrop_disable", "");
    }

    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("merchant_sel", "0");

    if (wp.colStr("merchant_sel").equals("0")) {
      buttonOff("btnmcht_disable");
      buttonOff("uplmcht_disable");
    } else {
      wp.colSet("btnmcht_disable", "");
      wp.colSet("uplmcht_disable", "");
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
        buttonOff("btnplatform_disable");
      } else {
        wp.colSet("btnplatform_disable", "");
      }

    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("mcc_code_sel", "0");

    if (wp.colStr("mcc_code_sel").equals("0")) {
      buttonOff("btnmccd_disable");
    } else {
      wp.colSet("btnmccd_disable", "");
    }

    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("pos_entry_sel", "0");

    if (wp.colStr("pos_entry_sel").equals("0")) {
      buttonOff("btnenty_disable");
    } else {
      wp.colSet("btnenty_disable", "");
    }

    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplmcht_disable");
    } else {
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnacty_disable");
    buttonOff("btngrop_disable");
    buttonOff("btnmcht_disable");
    buttonOff("btnaaa1_disable");
    buttonOff("btnplatform_disable");
    buttonOff("btnmccd_disable");
    buttonOff("btnenty_disable");
    return;
  }
//************************************************************************
  private String  listDbmBnData(String s1,String s2,String s3,String s4) throws Exception
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
void listWkdataAft() throws Exception
{
 wp.colSet("acct_type_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"3"));
 wp.colSet("group_code_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"2"));
 wp.colSet("merchant_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"1"));
 wp.colSet("mcht_group_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"6"));
 wp.colSet("platform_kind_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"P"));
 wp.colSet("mcc_code_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"5"));
 wp.colSet("pos_entry_sel_cnt" , listDbmBnData("dbm_bn_data_t","DBM_BPBIR",wp.colStr("active_code"),"4"));
}
//************************************************************************
void listWkdata() throws Exception
{
 wp.colSet("acct_type_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"3"));
 wp.colSet("group_code_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"2"));
 wp.colSet("merchant_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"1"));
 wp.colSet("mcht_group_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"6"));
 wp.colSet("platform_kind_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"P"));
 wp.colSet("mcc_code_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"5"));
 wp.colSet("pos_entry_sel_cnt" , listDbmBnData("dbm_bn_data","DBM_BPBIR",wp.colStr("active_code"),"4"));
}
//************************************************************************
public void commCrtuser(String columnData1) throws Exception
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
//           + " and   usr_id = '"+wp.colStr(ii,"crt_user")+"'"
           + sqlCol(wp.colStr(ii,"crt_user"), "usr_id");
      if (wp.colStr(ii,"crt_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1);

      sqlParm.clear();
      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname");
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************
public void commApruser(String columnData1) throws Exception
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
//           + " and   usr_id = '"+wp.colStr(ii,"apr_user")+"'"
           + sqlCol(wp.colStr(ii,"apr_user"), "usr_id");
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

} // End of class
