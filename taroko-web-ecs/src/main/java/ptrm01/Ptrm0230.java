/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/07/22  V1.00.01   Ray Ho        Initial                              *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-23  V1.00.03   Justin         parameterize sql
* 109-12-30  V1.00.04  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package ptrm01;

import ptrm01.Ptrm0230Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ptrm0230 extends BaseEdit {
  private String PROGNAME = "預借現金手續費參數處理程式108/07/22 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ptrm01.Ptrm0230Func func = null;
  String rowid, dataKK2;
  String orgTabName = "ptr_prepaidfee";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%")
        + sqlCol(wp.itemStr("ex_curr_code"), "a.curr_code", "like%");

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
        + "a.card_type," + "a.curr_code," + "a.dom_fix_amt," + "a.dom_percent," + "a.int_fix_amt,"
        + "a.int_percent," + "a.swap_fix_amt," + "a.swap_percent";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.card_type,a.curr_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCardType("comm_card_type");
    commCurrCode("comm_curr_code");


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
      if (wp.itemStr("kk_card_type").length() == 0) {
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
        + "a.card_type as card_type," + "a.curr_code as curr_code," + "a.fees_txn_code,"
        + "a.fees_bill_type," + "a.dom_fix_amt," + "a.dom_percent," + "a.dom_min_amt,"
        + "a.dom_max_amt," + "a.int_fix_amt," + "a.int_percent," + "a.int_min_amt,"
        + "a.int_max_amt," + "a.swap_fix_amt," + "a.swap_percent," + "a.crt_date," + "a.crt_user,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_user," + "a.apr_date,"
        + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_card_type"), "a.card_type")
          + sqlCol(wp.itemStr("kk_curr_code"), "a.curr_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + dataKK2 + "]");
      return;
    }
    commCardType("comm_card_type");
    commCurrCode("comm_curr_code");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    ptrm01.Ptrm0230Func func = new ptrm01.Ptrm0230Func(wp);

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
      if ((wp.respHtml.equals("ptrm0230_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_card_type").length() > 0) {
          wp.optionKey = wp.colStr("kk_card_type");
        }
        if (wp.colStr("card_type").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_curr_code").length() > 0) {
          wp.optionKey = wp.colStr("kk_curr_code");
        }
        if (wp.colStr("curr_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_curr_code", "ptr_currcode", "trim(curr_code)", "trim(curr_chi_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("ptrm0230"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_curr_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_curr_code");
        }
        this.dddwList("dddw_curr_code", "ptr_currcode", "trim(curr_code)", "trim(curr_chi_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commCardType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      if (wp.colStr(ii, "card_type").length() == 0)
          continue;
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
          + " and   card_type = ? ";
      
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_type")});

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commCurrCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " curr_chi_name as column_curr_chi_name " + " from ptr_currcode "
          + " where 1 = 1 " + " and   curr_code = ? "
          + " and   bill_sort_seq != '' ";
     
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "curr_code")});

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_curr_chi_name");
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
