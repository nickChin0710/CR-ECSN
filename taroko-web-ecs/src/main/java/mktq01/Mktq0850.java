/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/27  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名     
* 112-04-25  V1.00.04   machao       明細增’活動說明’ 欄位                                                                                *   
* 112-08-02  V1.00.05  Zuwei Su       列表增姓名欄位                                                                                *   
***************************************************************************/
package mktq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0850 extends BaseEdit {
  private String PROGNAME = "通路活動卡人消費查詢作業處理程式108/08/27 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq01.Mktq0850Func func = null;
  String rowid;
  String orgtTabName = "mkt_channel_bill";
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
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1  and a.active_code=b.active_code " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
        + sqlCol(wp.itemStr("ex_error_code"), "a.error_code", "like%")
        + sqlChkEx(wp.itemStr("ex_active_status"), "3", "");

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
      controlTabName = orgtTabName;

    wp.pageControl();

    wp.selectSQL =
        " " + "hex(a.rowid) as rowid, " + "a.active_code," + "b.active_name," + "'' as id_no," + "a.acct_type,"
            + "a.card_no," + "decode(major_card_no,card_no,'',major_card_no) as major_card_no,"
            + "decode(ori_card_no,card_no,'',ori_card_no) as ori_card_no," + "a.acct_code,"
            + "a.purchase_date," + "a.dest_amt," + "a.error_code," + "a.vd_flag," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a , mkt_channel_parm b ";
//    wp.queryWhere = "a.active_code=b.ACTIVE_CODE";
    wp.whereOrder = " " + " order by vd_flag,id_p_seqno,purchase_date,reference_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    commAcctCode("comm_acct_code");

    commErrorCode("comm_error_code");

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
        controlTabName = orgtTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + "a.active_code," + "a.vd_flag," + "a.acct_type,"
        + "'' as id_no," + "'' as chi_name," + "a.card_no," + "a.major_card_no," + "a.ori_card_no,"
        + "a.ori_major_card_no," + "a.acct_code," + "a.purchase_date," + "a.dest_amt,"
        + "a.block_cond," + "a.oppost_cond," + "a.payment_rate_cond," + "a.error_code,"
        + "a.reference_no," + "a.proc_date," + "a.id_p_seqno";

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
    commVdFlag("comm_vd_flag");
    commErrorCode("comm_error_code");
    commActiveCode("comm_active_code");
    commAcctType("comm_acct_type");
    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    commAcctCode("comm_acct_code");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq01.Mktq0850Func func = new mktq01.Mktq0850Func(wp);

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
      if ((wp.respHtml.equals("mktq0850"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code", "mkt_channel_parm", "trim(active_code)",
            "trim(active_name)",
            " where active_code in (select active_code from mkt_channel_anal)");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if ((itemKk("ex_active_code").length() == 0) && (itemKk("ex_id_no").length() == 0)) {
      alertErr2("身份證號與活動代號二者不可同時空白");
      return (1);
    }

    if (wp.itemStr("ex_id_no").length() > 0) {
      String sql1 = "";

      sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  =  '"
          + wp.itemStr("ex_id_no").toUpperCase() + "'";
      sqlSelect(sql1);

      if (sqlRowNum <= 0)
        wp.colSet("ex_id_p_seqno", "");
      else
        wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));

      sql1 = "select id_p_seqno as vd_id_p_seqno  " + "from dbc_idno " + "where  id_no  =  '"
          + wp.itemStr("ex_id_no").toUpperCase() + "'";
      sqlSelect(sql1);
      if (sqlRowNum <= 0)
        wp.colSet("ex_vd_id_p_seqno", "");
      else
        wp.colSet("ex_vd_id_p_seqno", sqlStr("vd_id_p_seqno"));

      if ((sqlStr("vd_id_p_seqno").length() == 0) && (sqlStr("id_p_seqno").length() == 0)) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }

    }


    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String ex_col, String sq_cond, String file_ext) {
    if (sq_cond.equals("1")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      if (wp.colStr("ex_id_p_seqno").length() == 0)
        return " and id_p_seqno ='" + wp.colStr("ex_vd_id_p_seqno") + "' ";
      if (wp.colStr("ex_vd_id_p_seqno").length() == 0)
        return " and id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
      return " and id_p_seqno in ('" + wp.colStr("ex_id_p_seqno") + "','"
          + wp.colStr("ex_vd_id_p_seqno") + "')";
    }


    return "";
  }

  // ************************************************************************
  public void commActiveCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_channel_parm "
          + " where 1 = 1 " + " and   active_code = '" + wp.colStr(ii, "active_code") + "'";
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
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from vmkt_acct_type "
          + " where 1 = 1 " + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
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
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      if (wp.colStr(ii, "vd_flag").equals("N")) {
        sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
            + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      } else {
        sql1 = "select " + " id_no as column_id_no " + " from dbc_idno " + " where 1 = 1 "
            + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      }

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
      if (wp.colStr(ii, "vd_flag").equals("N")) {
        sql1 = "select " + " chi_name as column_chi_name " + " from crd_idno " + " where 1 = 1 "
            + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      } else {
        sql1 = "select " + " chi_name as column_chi_name " + " from dbc_idno " + " where 1 = 1 "
            + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      }

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
          + " where 1 = 1 " + " and   acct_code = '" + wp.colStr(ii, "acct_code") + "'";
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
  public void commVdFlag(String cde1) throws Exception {
    String[] cde = {"N", "Y"};
    String[] txt = {"信用卡", "Debit卡"};
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
  public void commErrorCode(String cde1) throws Exception {
    String[] cde = {"00", "01", "02", "03", "04"};
    String[] txt = {"符合條件", "卡片停用", "帳戶>凍結", "延滯繳款", "每日限量"};
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
