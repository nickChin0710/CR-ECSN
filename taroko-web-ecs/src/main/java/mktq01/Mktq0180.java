/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名      
* 110-11-08  V1.00.03  machao     SQL Injection                                                                               *  
***************************************************************************/
package mktq01;

import mktq01.Mktq0180Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0180 extends BaseEdit {
  private String PROGNAME = "處理程式108/01/29 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq01.Mktq0180Func func = null;
  String rowid;// kk2;
  String orgTabName = "mkt_bpnw_refund";
  String orgTab2Name = "mkt_bpnw_mlist";
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
    if (queryCheck() != 0)
      return;
    wp.whereStr =
        "WHERE 1=1 "
            + sqlStrend(wp.itemStr("ex_refund_date_s"), wp.itemStr("ex_refund_date_e"),
                "a.refund_date")
            + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
            + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%");

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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "a.active_code, " + "a.card_no, "
        + "'' as id_no," + "'' as chi_name," + "a.sup_flag," + "a.acct_month_s," + "a.acct_month_e,"
        + "a.purchase_amt," + "a.ori_feedback_bp," + "a.feedback_bp," + "a.refund_amt,"
        + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.refund_date,a.active_code,a.card_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commActiveCode("comm_active_code");
    commIdNo("comm_id_no");
    commChiName("comm_chi_name");

    commSupFlag("comm_sup_flag");

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
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " ROW_NUMBER()OVER() as ser_num, " + "a.active_code,"
        + "'' as id_no," + "'' as chi_name," + "a.card_no," + "a.purchase_date," + "a.reference_no,"
        + "a.acq_member_id," + "a.mcht_no," + "a.acct_code," + "a.dest_amt," + "a.post_date,"
        + "a.bill_type," + "a.txn_code," + "a.acct_month," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k2"), "a.active_code")
          + sqlCol(itemKk("data_k3"), "a.card_no") + " order by purchase_Date,reference_no";
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commActiveCode("comm_active_code");
    commAcctCode("comm_acct_code");

    if (qFrom != 0) {
      commActiveCode("comm_active_code");
      commIdNo("comm_id_no");
      commChiName("comm_chi_name");
      commAcctCode("comm_acct_code");
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq01.Mktq0180Func func = new mktq01.Mktq0180Func(wp);

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
      if ((wp.respHtml.equals("mktq0180"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code", "mkt_bpnw", "trim(active_code)", "trim(active_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (wp.itemStr("ex_id_no").length() > 0) {
      String sql1 = "select id_p_seqno " + "from crd_idno " 
//    	  + "where  id_no  =  '" + wp.itemStr("ex_id_no").toUpperCase() + "'";
          + " where 1 = 1 " + " and id_no = :ex_id_no";
          setString("ex_id_no",wp.itemStr("ex_id_no").toUpperCase());
      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      return " and id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
    }

    return "";
  }

  // ************************************************************************
  public void commActiveCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_bpnw " + " where 1 = 1 "
//          + " and   active_code = '" + wp.colStr(ii, "active_code") + "'";
      	  + " and active_code = :active_code ";
      	  setString("active_code",wp.colStr(ii, "active_code"));
      if (wp.colStr(ii, "active_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_active_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      	  + " and id_p_seqno = :id_p_seqno ";
      	  setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
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
      sql1 = "select " + " chi_name as column_chi_name " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
			+ " and id_p_seqno = :id_p_seqno ";
      		setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
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
  public void commAcctCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chi_short_name as column_chi_short_name " + " from ptr_actcode "
          + " where 1 = 1 " 
//    	  + " and   acct_code = '" + wp.colStr(ii, "acct_code") + "'";
      	  + " and acct_code = :acct_code ";
      	  setString("acct_code",wp.colStr(ii, "acct_code"));
      if (wp.colStr(ii, "acct_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_chi_short_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commSupFlag(String cde1) throws Exception {
    String[] cde = {"0", "1"};
    String[] txt = {"正卡", "附卡"};
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
  public void check_button_off() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
