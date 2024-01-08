/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名   
* 110-11-02  V1.00.03  machao     SQL Injection                                                                                     *  
***************************************************************************/
package mktq02;

import mktq02.Mktq3210Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3210 extends BaseEdit {
  private String PROGNAME = "高鐵授權交易記錄查詢處理程式108/11/05 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq3210Func func = null;
  String rowid;
  String orgTabName = "cca_auth_txlog";
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
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
        + sqlStrend(wp.itemStr("ex_tx_date_s"), wp.itemStr("ex_tx_date_e"), "a.tx_date")
        + sqlCol(wp.itemStr("ex_mcc_code"), "a.mcc_code", "like%")
        + sqlCol(wp.itemStr("ex_auth_no"), "a.auth_no", "like%")
        + sqlChkEx(wp.itemStr("ex_card_no_4"), "3", "");

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
        + "a.card_no," + "a.tx_date," + "a.tx_time," + "a.auth_no," + "a.mcc_code," + "a.mcht_no,"
        + "a.mcht_name";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by card_no,tx_date,tx_time,auth_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commMccCode("comm_mcc_code");


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
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.card_no,"
        + "a.tx_date," + "a.tx_time," + "a.auth_no," + "a.mcc_code," + "a.mcht_no," + "a.mcht_name";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]");
      return;
    }
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq3210Func func = new mktq02.Mktq3210Func(wp);

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
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {}

  // ************************************************************************
  public int queryCheck() throws Exception {

    if ((wp.itemStr("ex_tx_date_s").length() == 0) || (wp.itemStr("ex_tx_date_e").length() == 0)) {
      alertErr2("授權日期不可空白");
      return (1);
    }

    if (wp.itemStr("ex_card_no_4").length() > 0) {
      if (wp.itemStr("ex_card_no_4").length() != 4) {
        alertErr2(" 必須輸入完整後四碼[ " + wp.itemStr("ex_card_no_4") + "] 資料");
        return (1);
      }
    }

    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    if ((wp.itemStr("ex_card_no_4").length() == 0) && (wp.itemStr("ex_auth_no").length() == 0)) {
      if (wp.itemStr("ex_tx_date_e")
          .compareTo(comm.nextNDate(wp.itemStr("ex_tx_date_s"), 10)) > 0) {
        alertErr2("授權起迄日期不可超過10天");
        return (1);
      }
    } else {
      if (wp.itemStr("ex_tx_date_e")
          .compareTo(comm.nextNDate(wp.itemStr("ex_tx_date_s"), 30)) > 0) {
        alertErr2("授權起迄日期不可超過30天");
        return (1);
      }
    }


    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_card_no_4")))
        return "";
      return " and card_no like '%" + wp.colStr("ex_card_no_4") + "' ";
    }


    return "";
  }

  // ************************************************************************
  public void commMccCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
//      sql1 = "select " + " mcc_remark as column_mcc_remark " + " from cca_mcc_risk "
//          + " where 1 = 1 " + " and   mcc_code = '" + wp.colStr(ii, "mcc_code") + "'";
      sql1 = "select " + " mcc_remark as column_mcc_remark " + " from cca_mcc_risk "
              + " where 1 = 1 " + sqlCol(wp.colStr(ii, "mcc_code"),"mcc_code");
      if (wp.colStr(ii, "mcc_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcc_remark");
      wp.colSet(ii, columnData1, columnData);
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

} // End of class