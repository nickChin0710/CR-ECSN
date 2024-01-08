/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/24  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *    
* 110/11/11  V1.00.05  jiangyingdong       sql injection                   * 
* 112/02/21  V1.00.06  Zuwei Su       Online增欄位:一般消費,查詢/修改/新增相關處理邏輯調整                   * 
***************************************************************************/
package mktm02;

import mktm02.Mktm6200Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6200 extends BaseEdit {

  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "特店群組代碼維護處理程式108/12/24 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6200Func func = null;
  String rowid;
  String mchtGroupId;
  String fstAprFlag = "";
  String orgTabName = "mkt_mcht_gp";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_mcht_group_id"), "a.mcht_group_id", "like%")
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

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.mcht_group_id,"
            + "a.mcht_group_desc,"
            + "a.platform_flag,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.mcht_group_id";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commPlatformFlagDesc();

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
    public void commPlatformFlagDesc() throws Exception {
        String[] cde = {
                "1", "2"
        };
        String[] txt = {
                "1.指定", "2.排除"
        };
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            for (int inti = 0; inti < cde.length; inti++) {
                if (wp.colStr(ii, "platform_flag").equals(cde[inti])) {
                    wp.colSet(ii, "platform_flag_desc", txt[inti]);
                    break;
                }
            }
        }
        return;
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
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.mcht_group_id as mcht_group_id,"
            + "a.mcht_group_desc,"
            + "a.platform_flag,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(mchtGroupId, "a.mcht_group_id");
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
    mchtGroupId = wp.colStr("mcht_group_id");
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + " a.aud_type as aud_type, "
            + "a.mcht_group_id as mcht_group_id,"
            + "a.mcht_group_desc as mcht_group_desc,"
            + "a.platform_flag as platform_flag,"
            + "a.crt_user as crt_user,"
            + "a.crt_date as crt_date,"
            + "a.apr_user as apr_user,"
            + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(mchtGroupId, "a.mcht_group_id");

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
    mchtGroupId = wp.itemStr("mcht_group_id");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      mchtGroupId = wp.itemStr("mcht_group_id");
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
    mchtGroupId = wp.itemStr("mcht_group_id");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      mchtGroupId = wp.itemStr("mcht_group_id");
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

    if ((wp.itemStr("mcht_group_id").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_mchtgp_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_mchtgp_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_MCHT_GP' ";
    if (wp.respHtml.equals("mktm6200_mrcd"))
      wp.whereStr += " and data_type  = '1' ";
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("mcht_group_id"));
//    whereCnt += " and  data_key = '" + wp.itemStr("mcht_group_id") + "'";
    params.clear();
    whereCnt += " and  data_key = ?";
    params.add(wp.itemStr("mcht_group_id"));
    wp.whereStr += " order by 4,5,6,7 ";
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
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm02.Mktm6200Func func = new mktm02.Mktm6200Func(wp);
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
    if (func.dbDeleteD2() < 0) {
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

    sqlSelect(sql1, params.toArray(new Object[params.size()]));

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm02.Mktm6200Func func = new mktm02.Mktm6200Func(wp);

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
  public void dddwSelect() {}

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

    if (sysUploadAlias.equals("mrcd")) {
      // if has pre check procudure, write in here
    }
    mktm02.Mktm6200Func func = new mktm02.Mktm6200Func(wp);

    if (sysUploadAlias.equals("mrcd"))
      func.dbDeleteD2Mrcd("MKT_MCHTGP_DATA_T");

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
      if (sysUploadAlias.equals("mrcd")) {
        if (lineCnt <= 0)
          continue;
        if (tmpStr.length() < 2)
          continue;
      }

      llCnt++;

      for (int inti = 0; inti < 10; inti++)
        logMsg[inti] = "";
      logMsg[10] = String.format("%02d", lineCnt);

      if (sysUploadAlias.equals("mrcd"))
        if (checkUploadfileMrcd(tmpStr) != 0)
          continue;

      if (errorCnt == 0) {
        if (sysUploadAlias.equals("mrcd")) {
          if (func.dbInsertI2Mrcd("MKT_MCHTGP_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
      }
    }

    if (errorCnt > 0) {
      if (sysUploadAlias.equals("mrcd"))
        func.dbDeleteD2Mrcd("MKT_MCHTGP_DATA_T");
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
  int checkUploadfileMrcd(String array) throws Exception {
    mktm02.Mktm6200Func func = new mktm02.Mktm6200Func(wp);

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
    uploadFileDat[2] = "MKT_MCHT_GP";
    uploadFileDat[3] = wp.itemStr("MCHT_GROUP_ID");
    uploadFileDat[4] = "1";
    uploadFileDat[5] = wp.sysDate;
    uploadFileDat[6] = wp.loginUser;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(array, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);
    // ******************************************************************
    if ((uploadFileDat[1].length() != 0) && (uploadFileDat[1].length() < 8))

      if (uploadFileDat[1].length() != 0)
        uploadFileDat[1] =
            "00000000".substring(0, 8 - uploadFileDat[1].length()) + uploadFileDat[1];


    return 0;
  }

  // ************************************************************************
  // ************************************************************************
  public void checkButtonOff() throws Exception {
    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplmrcd_disable");
    } else {
      wp.colSet("uplmrcd_disable", "");
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnmrcd_disable");
    buttonOff("uplmrcd_disable");

    return;
  }
  // ************************************************************************

} // End of class
