/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/25  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名    
* 110-11-08  V1.00.03  machao     SQL Injection                                                                                  *  
***************************************************************************/
package mktq02;

import mktq02.Mktq1030Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq1030 extends BaseEdit {
  private String PROGNAME = "市區停車回饋媒體檔傳書記錄查詢處理程式108/11/25 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq1030Func func = null;
  String rowid;
  String orgTabName = "mkt_dodo_filectl";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_park_vendor"), "a.park_vendor", "like%")
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date");

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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "a.crt_date," + "a.park_vendor,"
        + "a.file_name," + "a.proc_flag," + "a.start_time," + "a.file_desc,"
        + "to_char(a.mod_time,'yyyymmddhh24miss') as mod_time," + "a.mod_pgm";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by crt_date desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commParkVendor("comm_park_vendor");

    commProcFlag("comm_proc_flag");

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
    wp.selectSQL = "hex(a.rowid) as rowid," + "a.park_vendor," + "'' as ref_ip_code,"
        + "a.crt_date," + "a.start_time," + "a.file_name," + "a.proc_flag," + "a.file_desc,"
        + "a.end_time," + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

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
    commProcFlag("comm_proc_flag");
    commParkVendor("comm_park_vendor");
    checkButtonOff();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    wp.sqlCmd = "select b.ref_ip_code||' - '||b.ref_name  as ref_ip_code "
        + "from mkt_park_parm a,ecs_ref_ip_addr b " + "where a.ref_ip_code = b.ref_ip_code "
//        + "and   a.park_vendor = '" + wp.colStr("park_vendor") + "' ";
    	+ " and a.park_vendor = :park_vendor ";
    	setString("park_vendor",wp.colStr("park_vendor"));
    this.sqlSelect();
    wp.colSet("ref_ip_code", sqlStr("ref_ip_code"));

  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq1030Func func = new mktq02.Mktq1030Func(wp);

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
      if ((wp.respHtml.equals("mktq1030"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_park_vendor").length() > 0) {
          wp.optionKey = wp.colStr("ex_park_vendor");
        }
        this.dddwList("dddw_park_vendow", "mkt_park_parm", "trim(park_vendor)", "trim(vendor_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commParkVendor(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " vendor_name as column_vendor_name " + " from mkt_park_parm "
          + " where 1 = 1 "
//    	  + " and   park_vendor = '" + wp.colStr(ii, "park_vendor") + "'";
      	  + " and park_vendor = :park_vendor ";
      	  setString("park_vendor",wp.colStr(ii,"park_vendor"));
      if (wp.colStr(ii, "park_vendor").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_vendor_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commProcFlag(String cde1) throws Exception {
    String[] cde = {"Y", "0", "4", "2", "3"};
    String[] txt = {"處理完成", "等待扣點加檔", "月檔資料", "無尾筆資料", "累計與尾筆金額不符"};
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

}  // End of class
