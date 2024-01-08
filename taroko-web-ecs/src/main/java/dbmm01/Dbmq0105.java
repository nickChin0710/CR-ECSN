/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/16  V1.00.01   Ray Ho        Initial                              *
* 109-04-27 V1.00.02  yanghan  修改了變量名稱和方法名稱*
 * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *                                          *
* 110-10-29  V1.00.04  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package dbmm01;

import dbmm01.Dbmq0105Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmq0105 extends BaseEdit
{
  private final String PROGNAME = "VD bonus歷史資料檔維護處理程式108/08/16 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  dbmm01.Dbmq0105Func func = null;
  String rowid;
  String orgTabName = "dbm_month_stat";
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
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " + sqChkEx(wp.itemStr("ex_id_no"), "1", "")
        + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
        + sqlCol(wp.itemStr("ex_bonus_type"), "a.bonus_type", "like%");

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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "a.acct_month," + "a.last_month_bp,"
        + "a.new_bp," + "a.giv_bp," + "a.adj_bp," + "a.use_bp," + "a.rem_bp," + "a.inp_bp,"
        + "a.diff_bp," + "a.this_month_bp," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.id_p_seqno,acct_month desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }



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
    wp.selectSQL = "hex(a.rowid) as rowid," + "a.acct_type," + "''," + "'' as chi_name,"
        + "a.bonus_type," + "a.last_month_bp," + "a.new_bp," + "a.giv_bp," + "a.adj_bp,"
        + "a.use_bp," + "a.rem_bp," + "a.mov_bp," + "a.inp_bp," + "a.diff_bp," + "a.this_month_bp,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.id_p_seqno";

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
    commAcctType("comm_acct_type");
    commIdNoa("comm_''");
    commChiName("comm_chi_name");
    commBonusType("comm_bonus_type");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    dbmm01.Dbmq0105Func func = new dbmm01.Dbmq0105Func(wp);

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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("dbmq0105"))) {
        wp.initOption = "";
        wp.optionKey = itemKk("ex_acct_type");
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
        this.dddwList("dddw_acct_type1", "dbp_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
        wp.initOption = "";
        wp.optionKey = itemKk("ex_bonus_type");
        if (wp.colStr("ex_bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_bonus_type");
        }
        this.dddwList("dddw_bonus_type", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='BONUS_NAME'");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (itemKk("ex_id_no").length() == 0) {
      alertErr2("身份證號不可空白");
      return (1);
    }


    if (wp.itemStr("ex_id_no").length() > 0) {
      String idNo = wp.itemStr("ex_id_no");
      String idNoCode = "0";
      if (wp.itemStr("ex_id_no").length() > 10) {
        idNoCode = wp.itemStr("ex_id_no").substring(10, wp.itemStr("ex_id_no").length());
        idNo = wp.itemStr("ex_id_no").substring(0, 10);
      }
      String sql1 = "select id_p_seqno,chi_name " + "from   dbc_idno "
//          + "where  id_no      = '"
//          + idNo.toUpperCase() + "' " + "and    id_no_code = '" + idNoCode + "' ";
          + "where 1 = 1 "
          + sqlCol(idNo.toUpperCase(), "id_no")
          + sqlCol(idNoCode, "id_no_code");

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }

      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      return (0);
    }


    return (0);
  }

  // ************************************************************************
  public String sqChkEx(String exCol, String sqCond, String fileExt) {

    if (sqCond.equals("1")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
//      return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
      return sqlCol(wp.colStr("ex_id_p_seqno"), "a.id_p_seqno");
    }

    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_card_no")))
        return "";
//      return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
      return sqlCol(wp.colStr("ex_id_p_seqno"), "a.id_p_seqno");
    }

    return "";
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from dbp_acct_type "
          + " where 1 = 1 "
//              + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
          + sqlCol(wp.colStr(ii, "acct_type"), "acct_type");
      if (wp.colStr(ii, "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_chin_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commIdNoa(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from dbc_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
            + sqlCol(wp.colStr(ii, "id_p_seqno"), "id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_id_no");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commChiName(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chi_name as column_chi_name " + " from dbc_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
            + sqlCol(wp.colStr(ii, "id_p_seqno"), "id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_chi_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commBonusType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "bonus_TYPE") + "'"
          + sqlCol(wp.colStr(ii, "bonus_TYPE"), "wf_id")
          + " and   wf_type = 'BONUS_NAME' ";
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
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

} // End of class
