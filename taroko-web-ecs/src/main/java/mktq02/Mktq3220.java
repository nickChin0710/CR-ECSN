/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/11  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名    
* 110-11-02  V1.00.03  machao     SQL Injection   
* 111-06-24  V1.00.03  machao       bug处理                                                                                *  
***************************************************************************/
package mktq02;

import mktq02.Mktq3220Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3220 extends BaseEdit {
  private String PROGNAME = "高鐵車廂升等每月超出限額扣款明細檔處理程式108/11/11 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq3220Func func = null;
  String rowid;
  String orgTabName = "mkt_thsr_updeduct";
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
    if (queryCheck() != 0)
      return;
    wp.whereStr =
        "WHERE 1=1 "
            + sqlStrend(wp.itemStr("ex_proc_month_s"), wp.itemStr("ex_proc_month_e"),
                "a.proc_month")
            + sqlChkEx(wp.itemStr("ex_pay_type"), "3", "")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
            + sqlChkEx(wp.itemStr("ex_id_no"), "1", "");

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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "a.proc_month," + "'' as id_no,"
        + "'' as chi_name," + "a.card_no," + "a.card_cnt," + "a.ticket_pnt_cnt,"
        + "a.deduct_bp_cnt," + "a.deduct_amt_cnt," + "a.sub_cnt," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by proc_month";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commChiName("comm_chi_name");


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
    wp.selectSQL = "hex(a.rowid) as rowid," + " id_p_seqno as id_p_seqno," + "a.proc_month,"
        + "'' as id_no," + "'' as chi_name," + "a.acct_type," + "a.card_no," + "a.major_card_no,"
        + "a.group_code," + "a.card_type," + "a.card_mode," + "a.card_cnt," + "a.ticket_pnt_cnt,"
        + "a.deduct_bp_cnt," + "a.deduct_bp," + "a.deduct_amt_cnt," + "a.deduct_amt," + "a.sub_cnt,"
        + "a.mod_pgm," + "to_char(a.mod_time,'yyyymmdd') as mod_time";

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
    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    commAcctType("comm_acct_type");
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    commCardMode("comm_card_mode");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq3220Func func = new mktq02.Mktq3220Func(wp);

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
  public int queryCheck() throws Exception {
    if (wp.itemStr("ex_id_no").length() > 0) {
//      String sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  =  '"
//          + wp.itemStr("ex_id_no").toUpperCase() + "'";
      String sql1 = "select id_p_seqno " + "from crd_idno " + "where 1 = 1 "
          + sqlCol(wp.itemStr("ex_id_no").toUpperCase(),"id_no");

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
    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_pay_type")))
        return "";
      if (wp.itemStr("ex_pay_type").equals("1"))
        return " and deduct_bp_cnt > 0 ";
      else if (wp.itemStr("ex_pay_type").equals("2"))
        return " and deduct_amt_cnt > 0 ";
      else
        return " and sub_cnt > 0 ";
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

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
      wp.colSet(ii, columnData1, columnData);
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
      	  + sqlCol(wp.colStr(ii, "id_p_seqno"),"id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chi_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    	  + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'"
    	  + sqlCol(wp.colStr(ii, "acct_type"),"acct_type");
      if (wp.colStr(ii, "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commGroupCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 " 
//    		  + " and   group_code = '" + wp.colStr(ii, "group_code") + "'"
    		  + sqlCol(wp.colStr(ii, "group_code"),"group_code");
      if (wp.colStr(ii, "group_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commCardType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
 //         + " and   card_type = '" + wp.colStr(ii, "card_type") + "'"
          + sqlCol(wp.colStr(ii, "card_type"),"card_type");
      if (wp.colStr(ii, "card_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commCardMode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mode_desc as column_mode_desc " + " from mkt_thsr_upmode "
          + " where 1 = 1 " 
//    		  + " and   card_mode = '" + wp.colStr(ii, "card_mode") + "'"
    		  + sqlCol(wp.colStr(ii, "card_mode"),"card_mode");
      if (wp.colStr(ii, "card_mode").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mode_desc");
      wp.colSet(ii, columnData1, columnData);
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
