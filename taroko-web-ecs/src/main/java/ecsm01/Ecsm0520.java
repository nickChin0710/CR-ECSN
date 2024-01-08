/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-27 V1.00.02  yanghan       修改了變量名稱和方法名稱* 
* 109-12-28  V1.00.03 Justin            parameterize sql
 * 109-12-30  V1.00.04  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0520 extends BaseEdit {
  private String progname = "處理程式109/12/28 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0520Func func = null;
  String rowid, news;
  String orgTabName = "sys_db2_tables";
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
    wp.whereStr =
        "WHERE 1=1 " + sqlCol(wp.itemStr("ex_ecs_table_name"), "a.ecs_table_name", "like%")
            + sqlCol(wp.itemStr("ex_new_table_flag"), "a.new_table_flag", "like%")
            + sqlCol(wp.itemStr("ex_db2_table_name"), "a.db2_table_name", "like%")
            + sqlChkEx(wp.itemStr("ex_tran_cnt"), "1", "");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "a.db2_table_name," + "a.ecs_table_name,"
        + "a.db2_table_chi_name," + "'' as new_table_a," + "'' as isused_flag_a," + "a.comp_flag,"
        + "a.table_records";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.ecs_table_name,a.new_table_flag,a.db2_table_name";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commNewTableFlag("comm_new_table_a");
    commIsusedFlag("comm_isused_flag_a");

    commCompFlag("comm_comp_flag");

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
      if (wp.itemStr("kk_ecs_table_name").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + "a.ecs_table_name as ecs_table_name,"
        + "a.db2_table_name as db2_table_name," + "a.db2_table_chi_name," + "a.data_flag,"
        + "a.comp_flag," + "a.table_owner," + "a.new_table_flag," + "a.no_drop_flag,"
        + "a.isused_flag," + "a.crnoind_flag," + "a.depend_table," + "a.use_compgm_flag,"
        + "a.addon_pgm," + "a.table_comment";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_ecs_table_name"), "a.ecs_table_name")
          + sqlCol(wp.itemStr("kk_db2_table_name"), "a.db2_table_name");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + news + "]");
      return;
    }
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    ecsm01.Ecsm0520Func func = new ecsm01.Ecsm0520Func(wp);

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
  public String sqlChkEx(String ex_col, String sq_cond, String file_ext) {
    if (sq_cond.equals("1")) {
      if (wp.itemStr("ex_tran_cnt").equals("1"))
        return " and table_records <=3000000 ";
      else if (wp.itemStr("ex_tran_cnt").equals("2"))
        return " and table_records between 3000001 and 5000000 ";
      else if (wp.itemStr("ex_tran_cnt").equals("3"))
        return " and table_records between 5000001 and 8000000 ";
      else if (wp.itemStr("ex_tran_cnt").equals("4"))
        return " and table_records between 8000001 and 12000000 ";
      else if (wp.itemStr("ex_tran_cnt").equals("5"))
        return " and table_records between 12000001 and 16000000 ";
      else if (wp.itemStr("ex_tran_cnt").equals("6"))
        return " and table_records between 16000001 and 20000000 ";
      else if (wp.itemStr("ex_tran_cnt").equals("7"))
        return " and table_records >20000000 ";
      return "";
    }
    return "";
  }

  // ************************************************************************
  public void commNewTableFlag(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = " select " + " new_table_flag as column_new_table_a " + " from  sys_db2_userdef "
          + " where ecs_table_name = ? "
          + " and   db2_table_name = ? ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "ecs_table_name"), wp.colStr(ii, "db2_table_name")});

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_new_table_a");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commIsusedFlag(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = " select " + " isused_flag as column_isused_flag_a " + " from  sys_db2_userdef "
          + " where ecs_table_name = ? "
          + " and   db2_table_name = ? ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "ecs_table_name"), wp.colStr(ii, "db2_table_name")});

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_isused_flag_a");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commCompFlag(String cde1) throws Exception {
    String[] cde = {"N", "E", "C"};
    String[] txt = {"新檔", "ECS", "CCAS"};
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

} // End of class
