/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/08  V1.00.01   Allen Ho      Initial                              *
* 109-04-24 V1.00.02  yanghan      修改了變量名稱和方法名稱*
* 109-12-28  V1.00.03  Justin           parameterize sql
 * 109-12-30  V1.00.04  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0100 extends BaseEdit {
  private String progname = "系統刪除資料庫表格參數維護處理程式109/12/28 V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0100Func func = null;
  String rowid, news;
  String orgTabName = "ecs_rmtab_parm";
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
    } else if (eqIgno(wp.buttonCode, "procMethod_COPY")) {/* 複製 */
      strAction = "U";
      procMethodCopy();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "R5")) {// 明細查詢 -/
      strAction = "R5";
      dataReadR5();
    } else if (eqIgno(wp.buttonCode, "U5")) {/* 明細更新 */
      strAction = "U5";
      updateFuncU5();
    } else if (eqIgno(wp.buttonCode, "R3")) {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
    } else if (eqIgno(wp.buttonCode, "U3")) {/* 明細更新 */
      strAction = "U3";
      updateFuncU3();
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
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " 
        + sqlChkEx(wp.itemStr("ex_map_table_name"), "1", "")
        + sqlCol(wp.itemStr("ex_rmtab_mode"), "a.rmtab_mode", "like%")
        + sqlCol(wp.itemStr("ex_table_name"), "a.table_name", "like%");

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
        + "a.table_name," + "a.rmtab_mode," + "a.hst_table_name," + "a.rmtab_desc,"
        + "a.rmtab_mode," + "a.stop_date," + "a.rmtime_type," + "a.crt_user,"
        + "to_char(a.mod_time,'yyyymmddhh24miss') as mod_time";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.rmtab_mode,a.table_name";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commRmtabMode("comm_rmtab_mode");
    commRmtabMode("comm_rmtab_mode");
    commRmtimeType("comm_rmtime_type");

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
      if (wp.itemStr("kk_table_name").length() == 0) {
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.table_name as table_name," + "a.rmtab_mode as rmtab_mode," + "a.rmtab_desc,"
        + "a.hst_table_name," + "a.stop_date," + "a.stop_desc," + "a.rmtime_type,"
        + "a.cycle_day_flag," + "a.date_type," + "a.avoid_cycle," + "a.commit_flag,"
        + "a.commit_rows," + "a.rmtab_where," + "a.crt_date," + "a.crt_user," + "a.apr_date,"
        + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_table_name"), "a.table_name")
          + sqlCol(wp.itemStr("kk_rmtab_mode"), "a.rmtab_mode");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + news + "]");
      return;
    }
    commRmtabMode("comm_rmtab_mode");
    checkButtonOff();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    if (wp.colStr("rmtab_mode").equals("2"))
      buttonOff("btnsame_disable");

  }

  // ************************************************************************
  public void dataReadR5() throws Exception {
    String bnTable = "";

    if (wp.itemStr("table_name").length() == 0)
      if (wp.itemStr("kk_table_name").length() == 0) {
        alertErr2("鍵值不可為空白 ");
        return;
      }
    if (wp.colStr("table_name").length() == 0) {
      wp.itemSet("table_name", wp.itemStr("kk_table_name"));
      wp.colSet("table_name", wp.itemStr("kk_table_name"));
    }
    if (wp.colStr("rmtab_mode").length() == 0) {
      wp.itemSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
      wp.colSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
    }

    wp.selectCnt = 1;
    commRmtabMode("comm_rmtab_mode");
    this.selectNoLimit();
    bnTable = "mkt_bn_data";

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'ECS_RMTAB_PARM' ";
    if (wp.respHtml.equals("ecsm0100_same"))
      wp.whereStr += " and data_type  = '6' ";
    String whereCnt = wp.whereStr;
    whereCnt += " and  data_key = ? ";
    wp.whereStr += " and  data_key = :data_key ";
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("table_name") + wp.itemStr("rmtab_mode"));
    wp.whereStr += " order by 4,5 ";
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
  public void updateFuncU5() throws Exception {
    ecsm01.Ecsm0100Func func = new ecsm01.Ecsm0100Func(wp);
    int llOk = 0, llErr = 0;


    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-
    if (wp.respHtml.equals("ecsm0100_same"))
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
        return;

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
    if (func.dbDeleteD5() < 0) {
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

      if (func.dbInsertI5() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR5();
  }

  // ************************************************************************
  public void dataReadR3() throws Exception {
    String bnTable = "";

    if (wp.itemStr("table_name").length() == 0)
      if (wp.itemStr("kk_table_name").length() == 0) {
        alertErr2("鍵值不可為空白 ");
        return;
      }
    if (wp.colStr("table_name").length() == 0) {
      wp.itemSet("table_name", wp.itemStr("kk_table_name"));
      wp.colSet("table_name", wp.itemStr("kk_table_name"));
    }
    if (wp.colStr("rmtab_mode").length() == 0) {
      wp.itemSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
      wp.colSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
    }

    wp.selectCnt = 1;
    commRmtabMode("comm_rmtab_mode");
    this.selectNoLimit();
    bnTable = "mkt_bn_data";

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'ECS_RMTAB_PARM' ";
    if (wp.respHtml.equals("ecsm0100_week"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("ecsm0100_mont"))
      wp.whereStr += " and data_type  = '3' ";
    String whereCnt = wp.whereStr;
    whereCnt += " and  data_key = ? ";
    wp.whereStr += " and  data_key = :data_key ";
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("table_name") + wp.itemStr("rmtab_mode"));
    wp.whereStr += " order by 4,5 ";
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
    if (wp.respHtml.equals("ecsm0100_week"))
      commDataCodeb("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    ecsm01.Ecsm0100Func func = new ecsm01.Ecsm0100Func(wp);
    int llOk = 0, llErr = 0;


    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-
    if (wp.respHtml.equals("ecsm0100_week"))
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
        return;
    if (wp.respHtml.equals("ecsm0100_mont"))
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
        return;

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
    if (func.dbDeleteD3() < 0) {
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
  public void dataReadR4() throws Exception {
    String bnTable = "";

    if (wp.itemStr("table_name").length() == 0)
      if (wp.itemStr("kk_table_name").length() == 0) {
        alertErr2("鍵值不可為空白 ");
        return;
      }
    if (wp.colStr("table_name").length() == 0) {
      wp.itemSet("table_name", wp.itemStr("kk_table_name"));
      wp.colSet("table_name", wp.itemStr("kk_table_name"));
    }
    if (wp.colStr("rmtab_mode").length() == 0) {
      wp.itemSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
      wp.colSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
    }

    wp.selectCnt = 1;
    commRmtabMode("comm_rmtab_mode");
    this.selectNoLimit();
    bnTable = "mkt_bn_data";

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'ECS_RMTAB_PARM' ";
    if (wp.respHtml.equals("ecsm0100_year"))
      wp.whereStr += " and data_type  = '4' ";
    String whereCnt = wp.whereStr;
    whereCnt += " and  data_key = ? ";
    wp.whereStr += " and  data_key = :data_key ";
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("table_name") + wp.itemStr("rmtab_mode"));
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
  }

  // ************************************************************************
  public void updateFuncU4() throws Exception {
    ecsm01.Ecsm0100Func func = new ecsm01.Ecsm0100Func(wp);
    int llOk = 0, llErr = 0;


    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");
    String[] key2Data = wp.itemBuff("data_code2");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-
    if (wp.respHtml.equals("ecsm0100_year"))
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
        return;

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
    if (func.dbDeleteD4() < 0) {
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

    if (wp.itemStr("table_name").length() == 0)
      if (wp.itemStr("kk_table_name").length() == 0) {
        alertErr2("鍵值不可為空白 ");
        return;
      }
    if (wp.colStr("table_name").length() == 0) {
      wp.itemSet("table_name", wp.itemStr("kk_table_name"));
      wp.colSet("table_name", wp.itemStr("kk_table_name"));
    }
    if (wp.colStr("rmtab_mode").length() == 0) {
      wp.itemSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
      wp.colSet("rmtab_mode", wp.itemStr("kk_rmtab_mode"));
    }

    wp.selectCnt = 1;
    commRmtabMode("comm_rmtab_mode");
    this.selectNoLimit();
    bnTable = "mkt_bn_data";

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "data_code3, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'ECS_RMTAB_PARM' ";
    if (wp.respHtml.equals("ecsm0100_varn"))
      wp.whereStr += " and data_type  = '1' ";
    String whereCnt = wp.whereStr;
    whereCnt += " and  data_key = ? ";
    wp.whereStr += " and  data_key = :data_key ";
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("table_name") + wp.itemStr("rmtab_mode"));
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
    if (wp.respHtml.equals("ecsm0100_varn"))
      commDataCode11("comm_data_code");
    if (wp.respHtml.equals("ecsm0100_varn"))
      commDataCode21("comm_data_code2");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    ecsm01.Ecsm0100Func func = new ecsm01.Ecsm0100Func(wp);
    int llOk = 0, llErr = 0;


    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");
    String[] key2Data = wp.itemBuff("data_code2");
    String[] key3Data = wp.itemBuff("data_code3");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-
    if (wp.respHtml.equals("ecsm0100_varn"))
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
        return;

    int del2_flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2_flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm])) && (key2Data[ll].equals(key2Data[intm]))
            && (key3Data[ll].equals(key3Data[intm]))) {
          for (int intx = 0; intx < optData.length; intx++) {
            if (optData[intx].length() != 0)
              if (((ll + 1) == Integer.valueOf(optData[intx]))
                  || ((intm + 1) == Integer.valueOf(optData[intx]))) {
                del2_flag = 1;
                break;
              }
          }
          if (del2_flag == 1)
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
  public int selectBndataCount(String bndata_table, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndata_table + " " + whereStr;

    sqlSelect(sql1, new Object[] {wp.itemStr("table_name") + wp.itemStr("rmtab_mode")});

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    ecsm01.Ecsm0100Func func = new ecsm01.Ecsm0100Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
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
  public int queryCheck() throws Exception {
    if (wp.itemStr("ex_map_table_name").length() > 0) {
      String sql1 = "select data_key " + "from mkt_bn_data "
          + "where  table_name  =  'ECS_RMTAB_PARM' " + "and data_type  =  '6' "
          + "and data_code  =  ? ";

      sqlSelect(sql1, new Object[] {wp.itemStr("ex_map_table_name").toUpperCase()});
      if (sqlRowNum <= 0) {
        alertErr2("比照處理表格無 " + wp.itemStr("ex_map_table_name").toUpperCase() + " 資料");
        return (1);
      }
    }
    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (empty(wp.itemStr("ex_map_table_name")))
        return "";

      String sql1 = "select data_key " + "from mkt_bn_data "
          + "where  table_name  =  'ECS_RMTAB_PARM' " + "and data_type  =  '6' "
          + "and data_code  =  ? ";

      sqlSelect(sql1, new Object[] {wp.itemStr("ex_map_table_name").toUpperCase()});
      if (sqlRowNum <= 0)
        return "";

      String andStr = " and (";
      for (int inti = 0; inti < sqlRowNum; inti++) {
        andStr = andStr + "table_name = ? ";
        setString(sqlStr(inti, "data_key").substring(0, sqlStr(inti, "data_key").length() - 1));
        if (inti != sqlRowNum - 1)
          andStr = andStr + " or ";
      }

      andStr = andStr + " ) ";
      return andStr;
    }
    return "";
  }

  // ************************************************************************
  public void commRmtabMode(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"直接移除", "轉歷史檔案"};
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
  public void commRmtimeType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"每日", "每週", "每月", "每年"};
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
  public void commDataCode11(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4", "5", "6", "7", "8"};
    String[] txt = {"變數 1", "變數 2", "變數 3", "變數 4", "變數 5", "變數 6", "變數 7", "變數 8"};
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
  public void commDataCode21(String cde1) throws Exception {
    String[] cde = {"01", "02", "03", "11", "12", "13", "21", "T1", "N1", "D1"};
    String[] txt = {"系統日前(日=>日)", "系統日前(月=>日)", "系統月前(月=>月)", "營業日前(日=>日)", "營業日前(月=>日)",
        "營業日月份前(月=>月)", "關帳月前(月=>月)", "文字", "數字", "日期"};
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
  public void commDataCodeb(String cde1) throws Exception {
    String[] cde = {"0", "1", "2", "3", "4", "5", "6"};
    String[] txt = {"星期日", "星期一", "星期二", "星期>三", "星期四", "星期五", "星期六"};
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
  public void procMethodCopy() throws Exception {

    saveFunc();
    wp.itemSet("copy_table_name", wp.itemStr("copy_table_name").toUpperCase());
    ecsm01.Ecsm0100Func func = new ecsm01.Ecsm0100Func(wp);
    func.dbInsertCopy();
    if (wp.itemStr("rmtab_mode").equals("2"))
      func.dbInsertCopyBnData(6);
    else
      func.dbInsertCopyBnData(0);

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

} // End of class
