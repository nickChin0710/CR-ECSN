/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/27  V1.00.01   Ray Ho        Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change           
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *    
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                 *
* 110/11/11  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktm02;

import mktm02.Mktm6205Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6205 extends BaseEdit {
  private ArrayList<Object> params =  new ArrayList<Object>();
  private String PROGNAME = "特店群組代碼維護處理程式108/05/27 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6205Func func = null;
  String rowid, dataKK2, dataKK1;
  String orgTabName = "mkt_mchtgp_data";
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
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        strAction = "A";
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        strAction = "U";
        updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        deleteFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxFunc2();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_data_key"), "a.data_key", "like%")
        + sqlCol(wp.itemStr("ex_data_code"), "a.data_code", "like%")
        + sqlCol(wp.itemStr("ex_data_code2"), "a.data_code2", "like%")
        + " and table_name  =  'MKT_MCHT_GP' " + " and data_type  =  '1' ";

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
        + "a.data_key," + "a.data_code," + "a.data_code2";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by table_name,data_key,data_code,data_code2";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGpId("comm_data_key");


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
      if (wp.itemStr("kk_data_key").length() == 0) {
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
        + "a.data_key as data_key," + "a.data_code as data_code," + "a.data_code2 as data_code2,"
        + "a.crt_date," + "a.crt_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_data_key"), "a.data_key")
          + sqlCol(wp.itemStr("kk_data_code"), "a.data_code")
          + sqlCol(wp.itemStr("kk_data_code2"), "a.data_code2");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + dataKK2 + "]" + "[" + dataKK1 + "]");
      return;
    }
    commGpId("comm_data_key");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    mktm02.Mktm6205Func func = new mktm02.Mktm6205Func(wp);

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
  public void dddwSelect() {
    String ls_sql = "";
    try {
      if ((wp.respHtml.equals("mktm6205_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_data_key").length() > 0) {
          wp.optionKey = wp.colStr("kk_data_key");
        }
        if (wp.colStr("data_key").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_data_key", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6205"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_data_key").length() > 0) {
          wp.optionKey = wp.colStr("ex_data_key");
        }
        wp.initOption = "--";
        wp.optionKey = "";
        ls_sql = procDynamicDddw(wp.itemStr("ex_query_table"));

        wp.optionKey = wp.itemStr("ex_data_key");
        dddwList("dddw_gp_id", ls_sql);
        wp.colSet("ex_data_key", "");

      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (wp.itemStr("ex_query_table").equals("1"))
      orgTabName = "mkt_mchtgp_data_t";
    else
      orgTabName = "mkt_mchtgp_data";

    controlTabName = orgTabName.toUpperCase();
    wp.colSet("control_tab_name", controlTabName);
    wp.colSet("org_tab_name", controlTabName);


    return (0);
  }

  // ************************************************************************
  public void commGpId(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";

      columnData = "";
      sql1 = "select " + " MCHT_GROUP_DESC as column_MCHT_GROUP_DESC ";
      if (wp.itemStr("ex_query_table").equals("1"))
        sql1 = sql1 + " from mkt_mcht_gp_t";
      else
        sql1 = sql1 + " from mkt_mcht_gp";
//      sql1 = sql1 + " where 1 = 1 " + " and   MCHT_GROUP_ID = '" + wp.colStr(ii, "data_key") + "'";
      sql1 = sql1 + " where 1 = 1 " + " and   MCHT_GROUP_ID = ? ";
      params.clear();
      params.add(wp.colStr(ii, "data_key"));

      sqlSelect(sql1, params.toArray(new Object[params.size()]));

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_MCHT_GROUP_DESC");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void wfAjaxFunc2() throws Exception {
    // super.wp = wr; // AJAX 20200106 updated for archit. change
    String ajaxjDataKey = "";
    // super.wp = wr; // AJAX 20200106 updated for archit. change


    selectAjaxFunc20(wp.itemStr("ax_win_query_table"));

    if (rc != 1) {
      wp.addJSON("ajaxj_data_key", "");
      wp.addJSON("ajaxj_dynamiccolumnname", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_data_key", sqlStr(ii, "data_key"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_dynamiccolumnname", sqlStr(ii, "dynamiccolumnname"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String mchtGroupId) {
    String tableName = "mkt_mcht_gp";

    if (mchtGroupId.equals("1"))
      tableName = "mkt_mcht_gp_t";

    wp.sqlCmd = " select " + " mcht_group_id as data_key ,mcht_group_desc as dynamiccolumnname "
        + " from " + tableName + " a";

    this.sqlSelect();
    if (sqlRowNum <= 0)
      alertErr2("特店規屬[" + mchtGroupId + "]查無資料");

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
  String procDynamicDddw(String num) throws Exception {
    String lsSql = "";
    String tableName = "mkt_mcht_gp";

    if (num.equals("1"))
      tableName = "mkt_mcht_gp_t";
    lsSql = " select " + " mcht_group_id as db_code, "
        + " mcht_group_id||' '||mcht_group_desc as db_desc " + " from " + tableName + " a";

    return lsSql;
  }

  // ************************************************************************

} // End of class
