/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/08  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名          
* 110-11-02  V1.00.03  machao     SQL Injection      
* 111-06-28  V1.00.03  machao      bug修改     
* 111-08-30  V1.00.03  machao      版面調整、Bug處理                                                                       *  
***************************************************************************/
package mktq02;

import mktq02.Mktq3250Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3250 extends BaseEdit {
  private String PROGNAME = "高鐵車廂升等每日交易明細檔處理程式108/11/08 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq3250Func func = null;
  String rowid;
  String orgTabName = "mkt_thsr_uptxn";
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
    wp.whereStr = "WHERE 1=1 " + sqlChkEx(wp.itemStr("ex_file_type"), "3", "")
        + sqlCol(wp.itemStr("ex_trans_type"), "a.trans_type", "like%")
        + sqlChkEx(wp.itemStr("ex_branch_code"), "4", "")
        + sqlStrend(wp.itemStr("ex_proc_date_s"), wp.itemStr("ex_proc_date_e"), "a.proc_date")
        + " and upidno_seqno!=''     " + " and pay_type='3'    ";

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
        + "'' as file_type," + "'' as branch_code," + "a.trans_type," + "a.proc_date,"
        + "a.trans_date," + "'' as id_no," + "'' as chi_name," + "a.card_no," + "a.major_card_no,"
        + "a.card_mode," + "a.serial_no," + "a.refund_flag," + "a.id_p_seqno," + "a.upidno_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.trans_type,a.proc_date";

    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    comm_trans_type("comm_trans_type");
    commCardMode("comm_card_mode");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  void listWkdata() {
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sql1 = "select decode(file_type,'1','1.財管','2','2.分行') as file_type, "
          + "       a.branch_code||'.'||b.full_chi_name as branch_code "
          + "from mkt_thsr_upidno a,gen_brn b " + " where a.branch_code  = b.branch "
//          + " and   a.upidno_seqno = '" + wp.colStr(ii, "upidno_seqno") + "' "
          + sqlCol(wp.colStr(ii, "upidno_seqno"),"a.upidno_seqno");

      sqlSelect(sql1);
      sqlParm.clear();
      if (sqlRowNum <= 0)
        continue;

      wp.colSet(ii, "file_type", sqlStr("file_type"));
      wp.colSet(ii, "branch_code", sqlStr("branch_code"));
    }

    sql1 =
        "select count(*) as total_cnt, " + "       sum(decode(trans_type,'P',1,0)) as deduct_cnt, "
            + "       sum(decode(trans_type,'R',1,0)) as refund_cnt " + "from mkt_thsr_uptxn "
            + " where upidno_seqno !='' " + " and   pay_type = '3'  " ;

    if (wp.itemStr("ex_file_type").length() != 0) {
      sql1 = sql1 + " and upidno_seqno in " + " (select upidno_seqno " + "  from mkt_thsr_upidno "
//          + "  where file_type = '" + wp.itemStr("ex_file_type") + "') "
          + " where 1 = 1" + sqlCol(wp.itemStr("ex_file_type"),"file_type") + ")";
    }
    if (wp.itemStr("ex_branch_code").length() != 0) {
      sql1 = sql1 + " and upidno_seqno in " + " (select upidno_seqno " + "  from mkt_thsr_upidno "
//          + "  where branch_code = '" + wp.itemStr("ex_branch_code") + "') "
          +" where 1 = 1" + sqlCol(wp.itemStr("ex_branch_code"),"branch_code") + ")";
    }
    if (wp.itemStr("ex_proc_date_s").length() != 0) {
//      sql1 = sql1 + " and proc_Date between '" + wp.itemStr("ex_proc_Date_s") + "' "
//          + "               and     '" + wp.itemStr("ex_proc_Date_e") + "' ";
//    String 	proc_Date = wp.itemStr("ex_proc_Date_s");
      sql1 = sql1 + sqlStrend(wp.itemStr("ex_proc_Date_s"),wp.itemStr("ex_proc_Date_e"),"proc_Date");
    }
    if (wp.itemStr("ex_trans_type").length() != 0) {
//      sql1 = sql1 + " and trans_type = '" + wp.itemStr("ex_trans_type") + "' ";
      sql1 = sql1 + sqlCol(wp.itemStr("ex_trans_type"),"trans_type");
    }

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return;

    wp.colSet("ex_total_cnt", String.format("%d", (int) sqlNum("total_cnt")));
    wp.colSet("ex_deduct_cnt", String.format("%d", (int) sqlNum("deduct_cnt")));
    wp.colSet("ex_refund_cnt", String.format("%d", (int) sqlNum("refund_cnt")));
    wp.colSet("ex_sub_cnt",
        String.format("%d", (int) (sqlNum("deduct_cnt") - sqlNum("refund_cnt"))));

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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " id_p_seqno as id_p_seqno," + "a.serial_no," + "'' as file_type," + "'' as branch_code,"
        + "a.file_date," + "a.trans_type," + "a.trans_date," + "'' as id_no," + "'' as chi_name,"
        + "a.card_no," + "a.issue_date," + "a.major_card_no," + "a.acct_type," + "a.group_code,"
        + "a.card_type," + "a.authentication_code," + "a.card_mode," + "a.org_trans_date,"
        + "a.org_serial_no," + "a.upidno_seqno," + "a.refund_flag," + "a.refund_date,"
        + "a.error_code," + "a.error_desc," + "a.proc_date,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

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
    comm_trans_type("comm_trans_type");
    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    commAcctType("comm_acct_type");
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    commCardMode("comm_card_mode");
    checkButtonOff();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    String sql1 = "";

    sql1 = "select decode(file_type,'1','1.財管','2','2.分行') as file_type, "
        + "       a.branch_code||'.'||b.full_chi_name as branch_code "
        + "from mkt_thsr_upidno a,gen_brn b " + " where a.branch_code  = b.branch "
//        + " and   a.upidno_seqno = '" + wp.colStr("upidno_seqno") + "' "
        + sqlCol(wp.colStr("upidno_seqno"),"a.upidno_seqno");

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return;

    wp.colSet(0, "file_type", sqlStr("file_type"));
    wp.colSet(0, "branch_code", sqlStr("branch_code"));

  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq3250Func func = new mktq02.Mktq3250Func(wp);

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
      if ((wp.respHtml.equals("mktq3250"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_branch_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_branch_code");
        }
        this.dddwList("dddw_branch", "gen_brn", "trim(branch)", "trim(full_chi_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileCxt) {
    String sql1 = "";
    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_file_type")))
        return "";
      sql1 = " and upidno_seqno in " + " (select upidno_seqno " + "  from mkt_thsr_upidno "
//          + "  where file_type = '" + wp.itemStr("ex_file_type") + "') "
          + " where 1 = 1" + sqlCol(wp.itemStr("ex_file_type"),"file_type") + ")";
      return sql1;
    }
    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_branch_code")))
        return "";
      sql1 = " and upidno_seqno in " + " (select upidno_seqno " + "  from mkt_thsr_upidno "
//          + "  where branch_code = '" + wp.itemStr("ex_branch_code") + "') ";
      + " where 1 = 1" + sqlCol(wp.itemStr("ex_branch_code"),"branch_code") + ")";
      return sql1;
    }

    return "";
  }

  // ************************************************************************
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'"
          + sqlCol(wp.colStr(ii, "id_p_seqno"),"id_p_seqno") ;
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
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'"
          + sqlCol(wp.colStr(ii, "id_p_seqno"),"id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

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
//    		  + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'"
    		  + sqlCol(wp.colStr(ii, "acct_type"),"acct_type");
      if (wp.colStr(ii, "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

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
      sqlParm.clear();

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
//          + " and   card_type = '" + wp.colStr(ii, "card_type") + "'"
          + sqlCol(wp.colStr(ii, "card_type"),"card_type");
      if (wp.colStr(ii, "card_type").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

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
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mode_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void comm_trans_type(String cde1) throws Exception {
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
