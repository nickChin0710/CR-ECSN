/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/15  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名       
* 110-11-02  V1.00.04  machao     SQL Injection                                                                                *  
* 111/12/14  V1.00.05  machao         列表查詢不輸入任何條件,有error                                                                               *  
***************************************************************************/
package mktq02;

import mktq02.Mktq3200Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3200 extends BaseEdit {
  private String PROGNAME = "IBON商品資料查詢作業處理程式111/12/14 V1.00.05";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq3200Func func = null;
  String rowid;
  String orgTabName = "mkt_thsr_disc";
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
    wp.whereStr =
        "WHERE 1=1 "
            + sqlStrend(wp.itemStr("ex_file_date_s"), wp.itemStr("ex_file_date_e"), "a.file_date")
            + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
            + sqlCol(wp.itemStr("ex_deduct_type"), "a.deduct_type", "like%")
            + sqlStrend(wp.itemStr("ex_trans_date_s"), wp.itemStr("ex_trans_date_e"),
                "a.trans_date")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
            + sqlCol(wp.itemStr("ex_trans_type"), "a.trans_type", "like%")
            + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%")
            + sqlCol(wp.itemStr("ex_match_flag"), "a.match_flag", "like%")
            + " and proc_flag  in  ('0','1','Y') ";

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
        + "a.file_date," + "a.trans_date," + "a.trans_type," + "'' as id_no," + "a.major_card_no,"
        + "a.group_code," + "a.deduct_type,"
        + "decode(deduct_type,'1',deduct_bp,dest_amt) as deduct_bp," + "a.match_date,"
        + "a.proc_date," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by trans_date desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commGroupCode("comm_group_code");

    commTransType("comm_trans_type");
    commDeductType("comm_deduct_type");

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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.serial_no,"
        + "a.file_date," + "a.trans_type," + "a.trans_date," + "a.orig_trans_date,"
        + "a.id_p_seqno," + "'' as id_no," + "a.major_card_no," + "a.deduct_type," + "a.group_code,"
        + "a.acct_type," + "a.authentication_code," + "a.pnr," + "a.trans_amount,"
        + "a.depart_date," + "decode(deduct_type,'1','y','n') as DEDUCT_TYPE," + "a.discount_value,"
        + "a.deduct_bp," + "a.match_flag," + "a.match_date," + "a.proc_flag," + "a.proc_date,"
        + "a.error_desc," + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

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
    commTransType("comm_trans_type");
    commProcFlag("comm_proc_flag");
    commIdNo("comm_id_no");
    commGroupCode("comm_group_code");
    commAcctType("comm_acct_type");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq3200Func func = new mktq02.Mktq3200Func(wp);

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
      if ((wp.respHtml.equals("mktq3200"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (wp.itemStr("ex_id_no").length() > 0) {
//      String sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  =  '"
//          + wp.itemStr("ex_id_no").toUpperCase() + "'";
      String sql1 = "select id_p_seqno " + "from crd_idno " + "where 1 = 1 "
          +sqlCol(wp.itemStr("ex_id_no").toUpperCase(),"id_no");
      
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
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
//      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
          + sqlCol(wp.colStr(ii, "id_p_seqno"),"id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_id_no");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commGroupCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
//      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
//          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
              + " where 1 = 1 " + sqlCol(wp.colStr(ii, "group_code"),"group_code");
      if (wp.colStr(ii, "group_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_group_name");
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
//      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
//          + " where 1 = 1 " + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
              + " where 1 = 1 " + sqlCol(wp.colStr(ii, "acct_type"),"acct_type");
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
  public void commTransType(String cde1) throws Exception {
    String[] cde = {"P", "R"};
    String[] txt = {"購票", "退票"};
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
  public void commProcFlag(String cde1) throws Exception {
    String[] cde = {"1", "2", "Y", "X"};
    String[] txt = {"檢核確認", "扣紅利或金額確認", "已轉入帳單檔", "有問題無法處理"};
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
  public void commDeductType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"扣點", "扣款"};
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
