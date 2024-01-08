/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/18  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名     
* 110-11-08  V1.00.03  machao     SQL Injection                                                                                 *  
***************************************************************************/
package mktq02;

import mktq02.Mktq1020Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq1020 extends BaseEdit {
  private String PROGNAME = "市區停車手KEY資料查詢處理程式108/11/18 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq1020Func func = null;
  String rowid;
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
        "WHERE 1=1 " + sqlCol(wp.itemStr("ex_park_vendor"), "a.park_vendor", "like%")
            + sqlChkEx(wp.itemStr("ex_proc_flag"), "1", "")
            + sqlCol(wp.itemStr("ex_pass_type"), "a.pass_type", "like%")
            + sqlStrend(wp.itemStr("ex_park_date_s_s"), wp.itemStr("ex_park_date_s_e"),
                "a.park_date_s")
            + sqlCol(wp.itemStr("ex_verify_flag"), "a.verify_flag", "like%")
            + sqlChkEx(wp.itemStr("ex_id_no"), "4", "")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%");

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
        + "a.park_vendor," + "'' as id_no," + "a.card_no," + "a.park_date_s," + "a.park_date_e,"
        + "a.park_hr," + "a.free_hr," + "(park_hr-free_hr-USE_BONUS_HR) as selfee_hr,"
        + "a.use_bonus_hr," + "a.use_point," + "a.act_use_point," + "a.act_charge_amt,"
        + "a.id_p_seqno," + "a.tran_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder =
        " " + " order by a.park_vendor,a.pass_type,a.park_date_s,a.verify_flag,a.card_no";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");


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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.verify_flag," + "a.crt_date," + "a.pass_type," + "a.crt_user," + "a.verify_remark,"
        + "a.manual_reason," + "b.id_no as id_no," + "b.chi_name as chi_name," + "a.acct_type,"
        + "a.card_no," + "a.park_vendor," + "a.station_id," + "a.park_date_s," + "a.park_time_s,"
        + "a.park_date_e," + "a.park_time_e," + "a.park_hr," + "a.free_hr," + "a.use_bonus_hr,"
        + "a.use_point," + "a.act_use_point," + "a.act_charge_amt," + "a.proc_flag," + "a.err_code,"
        + "a.imp_date," + "a.file_name," + "a.tran_seqno," + "a.proc_date," + "a.apr_date,"
        + "a.apr_user," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a " + "JOIN crd_idno b " + "ON a.id_p_seqno = b.id_p_seqno ";
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
    commVerifyFlag("comm_verify_flag");
    commPassType("comm_pass_type");
    commProcFlag("comm_proc_flag");
    commErrCode("comm_err_code");
    commAcctType("comm_acct_type");
    commParkVendor("comm_park_vendor");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq1020Func func = new mktq02.Mktq1020Func(wp);

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
      if ((wp.respHtml.equals("mktq1020"))) {
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
  public int queryCheck() throws Exception {

    if ((wp.itemStr("ex_id_no").length() != 10) && (wp.itemStr("ex_id_no").length() != 0)
        && (wp.itemStr("ex_id_no").length() != 11)) {
      alertErr2("身分證號10碼,帳戶查詢碼11碼");
      return (1);
    }

    String sql1 = "";
    if ((wp.itemStr("ex_id_no").length() == 10) || (wp.itemStr("ex_id_no").length() == 11)) {
      String id_no = wp.itemStr("ex_id_no").toUpperCase().substring(0, 10);
      String id_no_code = "0";
      if (wp.itemStr("ex_id_no").length() == 11)
        id_no_code = wp.itemStr("ex_id_no").toUpperCase().substring(10, 11);
      sql1 = "select a.id_p_seqno, " + "       a.chi_name " + "from crd_idno a "
//          + "where  id_no      =  '" + id_no + "' " + "and    id_no_code  = '" + id_no_code + "' ";
      	  + " where 1 = 1 " + " and id_no = :id_no " + " and id_no_code = :id_no_code ";
      	  setString("id_no",id_no);
      	  setString("id_no_code",id_no_code);

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      return (0);
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (Arrays.asList("Y", "N").contains(wp.itemStr("ex_proc_flag"))) {
        return " and proc_flag = '" + wp.itemStr("ex_proc_flag") + "' ";
      } else if (wp.itemStr("ex_proc_flag").equals("3")) {
        return " and proc_flag = 'Y' and act_use_point > 0 ";
      } else if (wp.itemStr("ex_proc_flag").equals("4")) {
        return " and proc_flag = 'Y' and act_charge_amt > 0 ";
      }
    }
    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
    }
    return "";
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    	  + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
          + " and acct_type = :acct_type ";
          setString("acct_type",wp.colStr(ii, "acct_type"));
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
  public void commParkVendor(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " vendor_name as column_vendor_name " + " from mkt_park_parm "
          + " where 1 = 1 " 
//    	  + " and   park_vendor = '" + wp.colStr(ii, "park_vendor") + "'";
      	  + " and park_vendor = :park_vendor ";
      	  setString("park_vendor",wp.colStr(ii, "park_vendor"));
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

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
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
  public void commPassType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"自動過卡", "手KEY"};
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
    String[] cde = {"Y", "N"};
    String[] txt = {"已處理", "尚未扣點加檔"};
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
  public void commErrCode(String cde1) throws Exception {
    String[] cde = {"01", "02", "05", "06", "10", "20", "00"};
    String[] txt = {"卡號不存在", "無需扣點", "重覆轉入", "團代卡種有誤", "當日多筆", "點數不足", "正常扣點"};
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
