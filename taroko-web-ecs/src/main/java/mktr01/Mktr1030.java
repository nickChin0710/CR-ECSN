/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change           
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      * 
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *    
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *               
* 110-07-20  V1.00.05   jiangyigndong  change alter message  
* 110-11-03  V1.00.03  machao     SQL Injection                *
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
public class Mktr1030 extends BaseAction implements InfaceExcel {
  private String PROGNAME = "市區免費停車統計表處理程式";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String dataKK1;
  String orgTabName = "mkt_dodo_resp";
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
  public void userAction() throws Exception {
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // * 查詢功能 */
        strAction = "R";
        dataRead();
        break;
      // case "A":
      // /* 新增功能 */
      // is_action = "A";
      // insertFunc();
      // break;
      // case "U":
      // is_action = "U";
      // updateFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // deleteFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "XLS":
        /* Excek- */
        strAction = "XLS";
        xlsPrint();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxFunc2();
        break;
      default:
        break;
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
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
            + sqlChkEx(wp.itemStr("ex_err_code_chk1"), "4", "")
            + sqlChkEx(wp.itemStr("ex_err_code_chk"), "3", "")
            + sqlCol(wp.itemStr("ex_pass_type"), "a.pass_type", "like%")
            + sqlStrend(wp.itemStr("ex_park_date_e_s"), wp.itemStr("ex_park_date_e_e"),
                "a.park_date_e")
            + sqlCol(wp.itemStr("ex_verify_flag"), "a.verify_flag", "like%")
            + sqlChkEx(wp.itemStr("ex_cond_flag"), "1", "");

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

    wp.selectSQL = " " + "a.park_vendor," + "a.pass_type," + "a.verify_flag,"
        + "count(*) as total_cnt," + "sum(free_hr) as free_hr,"
        + "sum(use_bonus_hr) as use_bonus_hr," + "sum(park_hr-free_hr-use_bonus_hr) as use_fee_hr,"
        + "sum(park_hr) as park_hr," + "sum(0) as free_charge_amt," + "sum(0) as use_charge_amt,"
        + "sum(0) as pay_charge_amt," + "sum(use_point) as use_point,"
        + "sum(act_charge_amt) as act_charge_amt";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " group by PARK_VENDOR,pass_type,verify_flag"
        + " order by PARK_VENDOR,pass_type,verify_flag";

    wp.pageCountSql =
        "select count(*) from ( " + " select distinct PARK_VENDOR,pass_type,verify_flag" + " from "
            + wp.daoTable + " " + wp.queryWhere + " )";

    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commParkVendor("comm_park_vendor");

    commPassType("comm_pass_type");
    commVerifyFlag("comm_verify_flag");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  void listWkdata() {
    double freeChargeAmt = 0;
    double useChargeAmt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      freeChargeAmt = wp.itemNum("ex_charge_hr_amt") * wp.colNum(ii, "free_hr");
      wp.colSet(ii, "free_charge_amt", String.format("%.2f", freeChargeAmt));

      useChargeAmt = wp.itemNum("ex_charge_hr_amt") * wp.colNum(ii, "use_bonus_hr");
      wp.colSet(ii, "use_charge_amt", String.format("%.2f", useChargeAmt));
      wp.colSet(ii, "pay_charge_amt", String.format("%.2f", useChargeAmt + freeChargeAmt));
    }

    return;

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
      if ((wp.respHtml.equals("mktr1030"))) {
        wp.initOption = "--";
        wp.optionKey = itemkk("ex_park_vendor");
        if (wp.colStr("ex_park_vendor").length() > 0) {
          wp.optionKey = wp.colStr("ex_park_vendor");
        }
        this.dddwList("dddw_park_vendow", "mkt_park_parm", "trim(park_vendor)", "trim(vendor_name)",
            " where 1 = 1 ");
        wp.initOption = "";
        wp.optionKey = itemkk("ex_charge_hr_amt");
        if (wp.colStr("ex_charge_hr_amt").length() > 0) {
          wp.optionKey = wp.colStr("ex_charge_hr_amt");
        }
        lsSql = "";
        lsSql = procDynamicDddwChargeHrAmt(wp.itemStr("ex_park_vendor"));

        wp.optionKey = wp.itemStr("ex_charge_hr_amt");
        dddwList("dddw_charge_hr_amt", lsSql);
        wp.colSet("ex_charge_hr_amt", "");

      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {

    if (wp.itemStr("ex_park_vendor").length() == 0) {
//      alertErr2("[每小時停車費用 ] 資料必須選取");
	  alertErr2("[ 廠商代碼 ] 資料必須選取");
      return (1);
    }
    if (wp.itemStr("ex_charge_hr_amt").length() == 0)
      wp.itemSet("ex_charge_hr_amt", "0");

    if (wp.itemNum("ex_charge_hr_amt") == 0) {
      alertErr2("[每小時停車費用]不可0為");
      return (1);
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_err_code_chk")))
        return "";
      if (wp.itemStr("ex_err_code_chk").equals("Y"))
        return " and proc_flag = 'Y' ";
      else if (wp.itemStr("ex_err_code_chk").equals("N"))
        return " and proc_flag = 'N' ";
      else if (wp.itemStr("ex_err_code_chk").equals("1"))
        return " and err_code ='00' ";
      else if (wp.itemStr("ex_err_code_chk").equals("2"))
        return " and err_code in ('10','20') ";
    }
    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_err_code_chk1")))
        return "";
      if (wp.itemStr("ex_err_code_chk1").equals("Y"))
        return " and proc_flag = 'Y' and ((verify_flag='1' and err_code in ('00','10','20')) or verify_flag!='1') ";
      else
        return " and proc_flag = 'Y' and verify_flag='1' and err_code not in ('00','10','20') ";
    }

    if (sqCond.equals("1")) {
//      return " and (pass_type = '1' or (pass_type='2' and verify_flag = '1')) ";
    	return " and (pass_type = '1' or (pass_type='2' and verify_flag = '1') or (pass_type='3' and verify_flag = '1')) ";
    }

    return "";
  }

  // ************************************************************************
  public void commParkVendor(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " vendor_name as column_vendor_name " + " from mkt_park_parm "
          + " where 1 = 1 " 
//    	  + " and   park_vendor = '" + wp.colStr(ii, "PARK_VENDOR") + "'";
          + sqlCol(wp.colStr(ii, "PARK_VENDOR"),"park_vendor");
      if (wp.colStr(ii, "PARK_VENDOR").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_vendor_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commPassType(String cde1) throws Exception {
//    String[] cde = {"1", "2"};
//    String[] txt = {"自動過卡", "手KEY"};
    String[] cde = {"1", "2", "3"};
    String[] txt = {"自動過卡", "手KEY", "匯入"};
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
  public void wfAjaxFunc2() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change
    String ajaxjChargeHrAmt = "";
    // super.wp = wr; // 20200102 updated for archit. change


    selectAjaxFunc20(wp.itemStr("ax_win_park_vendor"));

    if (rc != 1) {
      wp.addJSON("ajaxj_charge_hr_amt", "");
      wp.addJSON("ajaxj_db_desc", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_charge_hr_amt", sqlStr(ii, "charge_hr_amt"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_db_desc", sqlStr(ii, "db_desc"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String parkVendor) {
    wp.sqlCmd = " select " + " '1', " + " to_char(charge_hr_amt) as charge_hr_amt, "
        + " '生效日'||charge_hr_date||'起' as db_desc " + " from mkt_park_parm "
//        + " where park_vendor = '" + parkVendor + "' " 
        + "where 1 = 1" + sqlCol(parkVendor,"park_vendor")
        + " union " + " select " + " '2', "
        + " to_char(charge_bef_amt)  as charge_hr_amt, " + " '生效日'||charge_hr_date||'前' as db_desc "
        + " from mkt_park_parm " 
//        + " where park_vendor = '" + parkVendor + "' "
        + "where 1 = 1" + sqlCol(parkVendor,"park_vendor")
        + " and   charge_bef_amt!=0 " + " order by 1  ";


    this.sqlSelect();
    if (sqlRowNum <= 0)
      alertErr2("廠商代碼[" + parkVendor + "]查無資料");

    return;
  }

  // ************************************************************************
  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "mktr1030";
      String allStr = "";
      if (wp.itemStr("ex_park_vendor").length() > 0)
        allStr = allStr + "廠商代碼：" + wp.itemStr("ex_park_vendor");
      if (wp.itemStr("ex_card_no").length() > 0)
        allStr = allStr + "  卡號:" + wp.itemStr("ex_card_no");
      if (wp.itemStr("ex_err_code_chk1").length() > 0)
        allStr = allStr + "  處理狀況:" + wp.itemStr("ex_err_code_chk1");
      if (wp.itemStr("ex_charge_hr_amt").length() > 0)
        allStr = allStr + "  每小時停車費用 ：" + wp.itemStr("ex_charge_hr_amt");
      if (wp.itemStr("ex_err_code_chk").length() > 0)
        allStr = allStr + "  扣點加檔結果" + wp.itemStr("ex_err_code_chk");
      if (wp.itemStr("ex_pass_type").length() > 0)
        allStr = allStr + "  來原類別:" + wp.itemStr("ex_pass_type");
      if (wp.itemStr("ex_park_date_e").length() > 0)
        allStr = allStr + "  出場日期：" + wp.itemStr("ex_park_date_e");
      if (wp.itemStr("ex_verify_flag").length() > 0)
        allStr = allStr + "  審核結果：" + wp.itemStr("ex_verify_flag");
      wp.colSet("cond1", allStr);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "mktr1030.xlsx";
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
  String procDynamicDddwChargeHrAmt(String parkVendor) throws Exception {
    String lsSql = "";

    lsSql = " select " + " to_char(charge_hr_amt) as db_code, "
        + " charge_hr_amt||' 生效日'||charge_hr_date||'起' as db_desc " + " from mkt_park_parm "
//        + " where park_vendor = '" + parkVendor + "' "
        + "where 1 = 1" + sqlCol(parkVendor,"park_vendor")
        + " union " + " select "
        + " to_char(charge_bef_amt) as db_code, "
        + " charge_bef_amt||' 生效日'||charge_hr_date||'前' as db_desc " + " from mkt_park_parm "
//        + " where park_vendor = '" + parkVendor + "' ";
    	+ "where 1 = 1" + sqlCol(parkVendor,"park_vendor");
    return lsSql;
  }
  // ************************************************************************

  // ************************************************************************

} // End of class
