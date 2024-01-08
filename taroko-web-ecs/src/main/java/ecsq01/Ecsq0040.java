/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/09  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-12-28  V1.00.03  Justin       parameterize sql
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package ecsq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsq0040 extends BaseEdit
{
  private String progname = "檔案轉入錯誤紀錄處理程式109/12/28 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsq01.Ecsq0040Func func = null;
  String rowid;
  String orgTabName = "ecs_media_errlog";
  String orgTab2Name = "ecs_notify_log";
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
    } else if (eqIgno(wp.buttonCode, "T")) {/* 動態查詢 */
      querySelect1();
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
    wp.whereStr = "WHERE 1=1 "
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
        + sqlCol(wp.itemStr("ex_unit_code"), "a.unit_code", "like%")
        + sqlCol(wp.itemStr("ex_trans_seqno"), "a.trans_seqno", "like%")
        + " and a.obj_type  =  '3' ";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgTab2Name;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.trans_seqno, " + "a.crt_date," + "a.crt_time," + "a.unit_code," + "a.notify_head,"
        + "a.notify_name," + "a.mod_pgm," + "to_char(a.mod_time,'yyyymmddhh24miss') as mod_time";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by crt_date desc,crt_time desc,unit_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commDeptCode("comm_unit_code");


    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowid = itemKk("data_k1");
    qFrom = 1;
    dataRead1();
  }

  // ************************************************************************
  public void querySelect1() throws Exception {
    controlTabName = orgTabName;

    rowid = itemKk("data_k1");
    qFrom = 2;
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
        + " ROW_NUMBER()OVER() as ser_num, " + "a.crt_date," + "a.line_seq," + "a.column_seq,"
        + "a.column_desc," + "a.column_data," + "a.main_desc," + "a.error_desc," + "a.error_seq,"
        + "a.unit_code," + "a.crt_time," + "a.program_code,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm," + "a.file_name";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k2"), "a.trans_seqno")
          + " order by crt_time,line_seq,column_seq";
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commDeptCode("comm_unit_code");

    if (qFrom != 0) {
      commDeptCode("comm_unit_code");
    }
  }

  // ************************************************************************
  public void dataRead1() throws Exception {
    if (controlTabName.length() == 0)
      controlTabName = orgTabName;
    wp.selectSQL = "" + " ROW_NUMBER()OVER() as ser_num, " + "a.crt_date," + "a.line_seq,"
        + "a.column_seq," + "a.column_desc," + "a.column_data," + "a.main_desc," + "a.error_desc,"
        + "a.error_seq," + "a.unit_code," + "a.crt_time," + "a.program_code,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm," + "a.file_name";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    wp.whereStr = wp.whereStr;
    wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k2"), "a.trans_seqno")
        + " order by crt_time,line_seq,column_seq";

    pageSelect();
    wp.setListCount(1);
    commDeptCode("comm_unit_code");

    if (qFrom != 0) {
      commDeptCode("comm_unit_code");
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    ecsq01.Ecsq0040Func func = new ecsq01.Ecsq0040Func(wp);

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
    try {
      if ((wp.respHtml.equals("ecsq0040"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_unit_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_unit_code");
        }
        this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commDeptCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colStr(ii, "unit_code").length() == 0)
            continue;
      columnData = "";
      sql1 = "select " + " dept_name as column_dept_name " + " from ptr_dept_code "
          + " where 1 = 1 " + " and   dept_code = ? ";
      
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "unit_code")});

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_dept_name");
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
