/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/25  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            
*  110-11-03  V1.00.03  machao     SQL Injection  *
***************************************************************************/
package mktr01;

import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktr1020 extends BaseAction implements InfaceExcel {
  private String PROGNAME = "市區免費停車手明細表處理程式108/11/25 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String dataKK1;
  String orgTabName = "mkt_dodo_resp";
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
  public void userAction() throws Exception {
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
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "XLS")) {/* Excek- */
      strAction = "XLS";
      xlsPrint();
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_park_vendor"), "a.park_vendor", "like%")
        + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
        + sqlStrend(wp.itemStr("ex_park_date_s_s"), wp.itemStr("ex_park_date_s_e"), "a.park_date_s")
        + sqlCol(wp.itemStr("ex_verify_flag"), "a.verify_flag", "like%");

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
        + "a.card_no," + "a.park_date_s," + "a.park_time_s," + "a.park_time_e," + "a.station_id,"
        + "a.park_hr," + "a.free_hr," + "a.charge_amt," + "a.use_point," + "a.verify_flag,"
        + "a.err_code";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.park_vendor,a.card_no,a.park_date_s,a.verify_flag";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commObjType("comm_err_code");

    commVerifyFlag("comm_verify_flag");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    dataKK1 = itemkk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {}

  // ************************************************************************
  public void saveFunc() throws Exception {
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
      if ((wp.respHtml.equals("mktr1020"))) {
        wp.initOption = "--";
        wp.optionKey = itemkk("ex_park_vendor");
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
  public int queryCheck() throws Exception {
    if ((itemkk("ex_card_no").length() == 0) && (itemkk("ex_park_date_s_s").length() == 0)
        && (itemkk("ex_park_date_s_e").length() == 0)) {
      alertErr2("卡號,停車日期起二者不可同時空白");
      return (1);
    }
    return (0);
  }

  // ************************************************************************
  public void commObjType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "err_code") + "'"
          + sqlCol(wp.colStr(ii, "err_code"),"wf_id")
          + " and   wf_type = 'DODO_ERRCODE' ";
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commVerifyFlag(String cde1) throws Exception {
    String[] cde = {"1", "2", "3"};
    String[] txt = {"正常交易", "本行吸收", "廠商吸收"};
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
  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "mktr1020";
      String allStr = "";
      if (wp.itemStr("ex_park_vendor").length() > 0)
        allStr = allStr + "廠商代碼：" + wp.itemStr("ex_park_vendor");
      if (wp.itemStr("ex_card_no").length() > 0)
        allStr = allStr + "  卡號：" + wp.itemStr("ex_card_no");
      if (wp.itemStr("ex_park_date_s").length() > 0)
        allStr = allStr + "  停車日期：" + wp.itemStr("ex_park_date_s");
      if (wp.itemStr("ex_verify_flag").length() > 0)
        allStr = allStr + "  審核結果：" + wp.itemStr("ex_verify_flag");
      wp.colSet("cond1", allStr);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "mktr1020.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      wp.setListCount(1);
//      queryFunc();
//      wp.listCount[1] = sqlRowNum;
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  // ************************************************************************
  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub
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
