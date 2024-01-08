/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
*  110-11-12  V1.00.06  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package mktm01;

import mktm01.Mktm0660Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0660 extends BaseEdit {
  private String PROGNAME = "WEB登錄代碼群組檔處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0660Func func = null;
  String rowid;
  String groupNo;
  String fstAprFlag = "";
  String orgTabName = "web_record_group";
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
    } else if (eqIgno(wp.buttonCode, "R4")) {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
    } else if (eqIgno(wp.buttonCode, "U4")) {/* 明細更新 */
      strAction = "U4";
      updateFuncU4();
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
    wp.whereStr =
        "WHERE 1=1 " + sqlCol(wp.itemStr("ex_record_group_no"), "a.record_group_no", "like%")
            + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "")
            + sqlChkEx(wp.itemStr("ex_record_group_name"), "2", "");

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
        + "a.record_group_no," + "a.record_group_name," + "a.voice_record_sel,"
        + "a.web_record_sel," + "a.record_cnt_cond," + "a.record_cnt," + "a.record_id_cond,"
        + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by record_group_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commVoiceSel("comm_voice_record_sel");
    commWebSel("comm_web_record_sel");

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
        + "a.record_group_no as record_group_no," + "a.record_group_name," + "a.active_date_s,"
        + "a.active_date_e," + "a.voice_record_sel," + "a.web_record_sel," + "a.merchant_sel,"
        + "a.mcht_group_sel," + "a.record_cnt_cond," + "a.record_cnt," + "a.record_id_cond,"
        + "a.purchase_cond," + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(groupNo, "a.record_group_no");
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
    groupNo = wp.colStr("record_group_no");
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.record_group_no as record_group_no,"
        + "a.record_group_name as record_group_name," + "a.active_date_s as active_date_s,"
        + "a.active_date_e as active_date_e," + "a.voice_record_sel as voice_record_sel,"
        + "a.web_record_sel as web_record_sel," + "a.merchant_sel as merchant_sel,"
        + "a.mcht_group_sel as mcht_group_sel," + "a.record_cnt_cond as record_cnt_cond,"
        + "a.record_cnt as record_cnt," + "a.record_id_cond as record_id_cond,"
        + "a.purchase_cond as purchase_cond," + "a.crt_user as crt_user,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(groupNo, "a.record_group_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    groupNo = wp.itemStr("record_group_no");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      groupNo = wp.itemStr("record_group_no");
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
    groupNo = wp.itemStr("record_group_no");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      groupNo = wp.itemStr("record_group_no");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void dataReadR4() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("record_group_no").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "data_code3, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'WEB_RECORD_GROUP' ";
    if (wp.respHtml.equals("mktm0660_vore"))
      wp.whereStr += " and data_type  = '1' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("record_group_no") + "'";
    whereCnt += sqlCol(wp.itemStr("record_group_no"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("record_group_no"));
    wp.whereStr += " order by 4,5,6,7,8 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm0660_vore"))
      commDtaCode01("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU4() throws Exception {
    mktm01.Mktm0660Func func = new mktm01.Mktm0660Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");
    String[] key2Data = wp.itemBuff("data_code2");
    String[] key3Data = wp.itemBuff("data_code3");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2Flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2Flag = 0;
      wp.colSet(ll, "ok_flag", "");
      if ((empty(key1Data[ll])) || (empty(key2Data[ll])) || (empty(key3Data[ll])))
        continue;

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
    if (func.dbDeleteD4() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    int deleteFlag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      deleteFlag = 0;
      // KEY 不可同時為空字串
      if ((empty(key1Data[ll])) && (empty(key2Data[ll])) && (empty(key3Data[ll])))
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
      func.varsSet("data_code3", key3Data[ll]);

      if (func.dbInsertI4() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR4();
  }

  // ************************************************************************
  public void dataReadR2() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("record_group_no").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'WEB_RECORD_GROUP' ";
    if (wp.respHtml.equals("mktm0660_were"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("mktm0660_aaa1"))
      wp.whereStr += " and data_type  = '4' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("record_group_no") + "'";
    whereCnt += sqlCol(wp.itemStr("record_group_no"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("record_group_no"));
    wp.whereStr += " order by 4,5,6 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm0660_were"))
      commWebRecord("comm_data_code");
    if (wp.respHtml.equals("mktm0660_aaa1"))
      commDataCode34("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm01.Mktm0660Func func = new mktm01.Mktm0660Func(wp);
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
      if ((empty(key1Data[ll])))
        continue;

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

    if ((wp.itemStr("record_group_no").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'WEB_RECORD_GROUP' ";
    if (wp.respHtml.equals("mktm0660_mrch"))
      wp.whereStr += " and data_type  = '3' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("record_group_no") + "'";
    whereCnt += sqlCol(wp.itemStr("record_group_no"), "data_key");
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("record_group_no"));
    wp.whereStr += " order by 4,5,6,7 ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    mktm01.Mktm0660Func func = new mktm01.Mktm0660Func(wp);
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

  // ************************************************************************
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1);

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm01.Mktm0660Func func = new mktm01.Mktm0660Func(wp);

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
      if ((wp.respHtml.equals("mktm0660_vore"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_voice_record", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='VOICE_LIST'");
      }
      if ((wp.respHtml.equals("mktm0660_were"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_web_record", "web_activity_parm", "trim(WEB_RECORD_NO)",
            "trim(RECORD_NAME)",
            " where decode(web_date_e,'','30001231',web_date_e)>=to_char(sysdate,'yyyymmdd') order by web_record_no,WEB_DATE_E desc");
      }
      if ((wp.respHtml.equals("mktm0660_aaa1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("2")) {
//      return " and record_group_name like  '%" + wp.itemStr("ex_record_group_name") + "%'";
      return sqlCol(wp.itemStr("ex_record_group_name"), "record_group_name", "%like%");
    }

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
  public void commDtaCode01(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "data_code") + "'"
          + sqlCol(wp.colStr(ii, "data_code"), "wf_id")
          + " and   wf_type = 'VOICE_LIST' ";
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commWebRecord(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " RECORD_NAME as column_RECORD_NAME " + " from web_activity_parm "
          + " where 1 = 1 "
//          + " and   WEB_RECORD_NO = '" + wp.colStr(ii, "data_code") + "'";
          + sqlCol(wp.colStr(ii, "data_code"), "WEB_RECORD_NO");
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_RECORD_NAME");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode34(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcht_group_desc as column_mcht_group_desc " + " from mkt_mcht_gp "
          + " where 1 = 1 "
//          + " and   mcht_group_id = '" + wp.colStr(ii, "data_code") + "'";
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
  public void commVoiceSel(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "指定", "排除"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt2 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, txt2).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commWebSel(String cde1) throws Exception {
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
  public void procUploadFile(int loadType) throws Exception {
    if (wp.colStr(0, "ser_num").length() > 0)
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    if (loadType == 2)
      file_dataImp2();
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
  void file_dataImp2() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");

    if (fi == -1)
      return;

    String sysUploadType = wp.itemStr("sys_upload_type");
    String sysUploadAlias = wp.itemStr("sys_upload_alias");

    if (sysUploadAlias.equals("aaa1")) {
      // if has pre check procudure, write in here
    }
    mktm01.Mktm0660Func func = new mktm01.Mktm0660Func(wp);

    if (sysUploadAlias.equals("aaa1"))
      func.dbDeleteD2Aaa1("MKT_BN_DATA_T");

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    tranSeqStr = comr.getSeqno("MKT_MODSEQ");

    String tmpStr = "";
    int llOk = 0, llCnt = 0, llErr = 0;
    int lineCnt = 0;
    while (true) {
      tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y"))
        break;
      lineCnt++;
      if (sysUploadAlias.equals("aaa1")) {
        if (lineCnt <= 0)
          continue;
        if (tmpStr.length() < 2)
          continue;
      }

      llCnt++;

      for (int inti = 0; inti < 10; inti++)
        logMsg[inti] = "";
      logMsg[10] = String.format("%02d", lineCnt);

      if (sysUploadAlias.equals("aaa1"))
        if (checkUploadfileAaa1(tmpStr) != 0)
          continue;

      if (errorCnt == 0) {
        if (sysUploadAlias.equals("aaa1")) {
          if (func.dbInsertI2Aaa1("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
      }
    }

    if (errorCnt > 0) {
      if (sysUploadAlias.equals("aaa1"))
        func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
      func.dbInsertEcsNotifyLog(tranSeqStr, errorCnt);
    }

    sqlCommit(1); // 1:commit else rollback

    alertMsg("資料匯入處理筆數 : " + llCnt + ", 成功(" + llOk + "), 重複(" + llErr + "), 失敗("
        + (llCnt - llOk - llErr) + ")");

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);


    return;
  }

  // ************************************************************************
  int checkUploadfileAaa1(String tmpStr) throws Exception {
    mktm01.Mktm0660Func func = new mktm01.Mktm0660Func(wp);

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
    uploadFileDat[2] = "WEB_RECORD_GROUP";
    uploadFileDat[3] = wp.itemStr("record_group_no");
    uploadFileDat[4] = "3";
    uploadFileDat[5] = wp.sysDate;
    uploadFileDat[6] = wp.loginUser;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
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
    if (wp.colStr("voice_record_sel").length() == 0)
      wp.colSet("voice_record_sel", "0");

    if (wp.colStr("voice_record_sel").equals("0")) {
      buttonOff("btnvore_disable");
    } else {
      wp.colSet("btnvore_disable", "");
    }

    if (wp.colStr("web_record_sel").length() == 0)
      wp.colSet("web_record_sel", "0");

    if (wp.colStr("web_record_sel").equals("0")) {
      buttonOff("btnwere_disable");
    } else {
      wp.colSet("btnwere_disable", "");
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

    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplaaa1_disable");
    } else {
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnvore_disable");
    buttonOff("btnwere_disable");
    buttonOff("btnmrch_disable");
    buttonOff("btnaaa1_disable");
    return;
  }
  // ************************************************************************
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
           + sqlCol(wp.colStr(ii, "crt_user"), "usr_id")
           ;
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
//         + " and   usr_id = '"+wp.colStr(ii,"apr_user")+"'"
         + sqlCol(wp.colStr(ii,"apr_user"), "usr_id")
         ;
    if (wp.colStr(ii,"apr_user").length()==0)
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

}  // End of class
