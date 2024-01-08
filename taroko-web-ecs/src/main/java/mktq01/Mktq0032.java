/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-11-26  V1.00.03  Ryan       updated for project coding               
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名        
* 110-11-08  V1.00.03  machao     SQL Injection                                                                             *  
***************************************************************************/
package mktq01;

import mktq01.Mktq0032Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0032 extends BaseEdit {
  private String PROGNAME = "紅利積點區間查詢處理程式109/04/20 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq01.Mktq0032Func func = null;
  String rowid;// kk2;
  String orgTabName = "mkt_bonus_stat3";
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
    }else if (eqIgno(wp.buttonCode, "NILL")){/* nothing to do */
    	strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
        + sqlStrend(wp.itemStr("ex_stat_month_s"), wp.itemStr("ex_stat_month_e"), "a.stat_month");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "a.stat_month, " + "sum(acct_cnt_a) as acct_cnt_a,"
        + "sum(bonus_cnt_a) as bonus_cnt_a," + "sum(acct_cnt_b) as acct_cnt_b,"
        + "sum(bonus_cnt_b) as bonus_cnt_b";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " group by stat_month" + " order by stat_month desc";

    wp.pageCountSql = "select count(*) from ( " + " select distinct stat_month" + " from "
        + wp.daoTable + " " + wp.queryWhere + " )";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }



    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowid = itemKk("data_k1");
    qFrom = 1;
    dataRead1();
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
    if (qFrom == 0)
      if (wp.itemStr("kk_acct_type").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
        if (wp.colStr("control_tab_name").length()!=0)
           controlTabName=wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " ROW_NUMBER()OVER() as ser_num, "
            + "a.acct_type as acct_type,"
            + "a.stat_month as stat_month,"
            + "a.bp_area_s,"
            + "a.bp_area_e,"
            + "sum(acct_cnt_a) as acct_cnt_a,"
            + "round(sum(acct_rate_a),2) as acct_rate_a,"
            + "sum(bonus_cnt_a) as bonus_cnt_a,"
            + "round(sum(bonus_rate_a),2) as acct_rate_a,"
            + "sum(acct_cnt_b) as acct_cnt_b,"
            + "round(sum(acct_rate_b),2) as acct_rate_b,"
            + "sum(bonus_cnt_b) as bonus_cnt_b,"
            + "round(sum(bonus_rate_b),2) as acct_rate_b";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k1"), "a.acct_type");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr 
    	 + sqlCol(wp.colStr("bb_stat_month"), "a.stat_month")
          + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
          + sqlStrend(wp.itemStr("ex_stat_month_s"), wp.itemStr("ex_stat_month_e"), "a.stat_month")
          + " group by stat_month,BP_AREA_S,BP_AREA_e"
          + " order by stat_month,BP_AREA_S,BP_AREA_e";
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commAcctType("comm_acct_type");

    if (qFrom != 0) {
      commAcctType("comm_acct_type");
    }
  }

  // ************************************************************************
  public void dataRead1() throws Exception {
    if (controlTabName.length() == 0)
      controlTabName = orgTabName;
    wp.selectSQL = ""
            + " ROW_NUMBER()OVER() as ser_num, "
            + "a.stat_month as stat_month,"
            + "a.bp_area_s,"
            + "a.bp_area_e,"
            + "sum(acct_cnt_a) as acct_cnt_a,"
            + "round(sum(acct_rate_a),2) as acct_rate_a,"
            + "sum(bonus_cnt_a) as bonus_cnt_a,"
            + "round(sum(bonus_rate_a),2) as acct_rate_a,"
            + "sum(acct_cnt_b) as acct_cnt_b,"
            + "round(sum(acct_rate_b),2) as acct_rate_b,"
            + "sum(bonus_cnt_b) as bonus_cnt_b,"
            + "round(sum(bonus_rate_b),2) as acct_rate_b";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    wp.whereStr = wp.whereStr;
    wp.whereStr = wp.whereStr 
    	+ sqlCol(itemKk("data_k2"), "a.stat_month")
        + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type")
        + " group by stat_month,BP_AREA_S,BP_AREA_e" 
        + " order by stat_month,BP_AREA_S,BP_AREA_e";

    pageSelect();
    wp.setListCount(1);
    wp.colSet("acct_type", wp.itemStr("ex_acct_type"));
    commAcctType("comm_acct_type");

    if (qFrom != 0) {
      commAcctType("comm_acct_type");
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq01.Mktq0032Func func = new mktq01.Mktq0032Func(wp);

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
      if ((wp.respHtml.equals("mktq0032"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
//        this.dddwList("dddw_acct_type1", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
//            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
//    String columnData = "";
//    String sql1 = "";
//    for (int ii = 0; ii < wp.selectCnt; ii++) {
//      columnData = "";
//      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
//          + " where 1 = 1 " 
////    	  + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
//		  + " and acct_type = :acct_type ";
//      	  setString("acct_type",wp.colStr(ii, "acct_type"));
//      if (wp.colStr(ii, "acct_type").length() == 0)
//        continue;
//      sqlSelect(sql1);
//
//      if (sqlRowNum > 0) {
//        columnData = columnData + sqlStr("column_chin_name");
//        wp.colSet(ii, columnData1, columnData);
//      }
//    }
	  String[] cde = {"01", "03" ,"06" ,"90"};
	  String[] txt = {"一般卡", "商務卡" ,"政府網路採購卡" ,"Visa Debit卡"};
	  String columnData = "";
	    for (int ii = 0; ii < wp.selectCnt; ii++) {
	      for (int inti = 0; inti < cde.length; inti++) {
	        String txt1 = columnData1.substring(5, columnData1.length());
	        if (wp.colStr(ii, txt1).equals(cde[inti])) {
	          wp.colSet(ii, columnData1, txt[inti]);
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
