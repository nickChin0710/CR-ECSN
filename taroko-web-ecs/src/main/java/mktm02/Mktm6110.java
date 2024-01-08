/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/20  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 110/11/05  V1.00.04  jiangyingdong       sql injection                   * 
***************************************************************************/
package mktm02;

import mktm02.Mktm6110Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6110 extends BaseEdit {

  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "標準年費參數設定作業處理程式108/08/20 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6110Func func = null;
  String rowid, dataKK2;
  String orgTabName = "ptr_group_card";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%")
        + sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%");

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

    wp.selectSQL =
        " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, " + "a.group_code,"
            + "a.card_type," + "a.first_fee," + "a.other_fee," + "a.apr_date," + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by group_code,card_type";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGroupCode("comm_group_code");
    commCardType("comm_card_type");


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
      if (wp.itemStr("kk_group_code").length() == 0) {
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
        + "a.group_code as group_code," + "a.card_type as card_type," + "a.first_fee,"
        + "a.other_fee," + "a.sup_rate," + "a.sup_end_month," + "a.sup_end_rate," + "a.crt_user,"
        + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_group_code"), "a.group_code")
          + sqlCol(wp.itemStr("kk_card_type"), "a.card_type");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + dataKK2 + "]");
      return;
    }
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    mktm02.Mktm6110Func func = new mktm02.Mktm6110Func(wp);

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
      if ((wp.respHtml.equals("mktm6110_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_group_code").length() > 0) {
          wp.optionKey = wp.colStr("kk_group_code");
          wp.initOption = "";
        }
        if (wp.colStr("group_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_group_code", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
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
      }
      if ((wp.respHtml.equals("mktm6110"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type2", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commGroupCode(String columnData1) throws Exception {
	String columnData = "";
	String sql1 = "";
	for (int ii = 0; ii < wp.selectCnt; ii++) {
	  columnData = "";
	  sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
	//	        + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
	    	  + " where 1 = 1 " + " and   group_code = ?";
	  if (wp.colStr(ii, "group_code").length() == 0)
		continue;
      params.clear();
      params.add(wp.colStr(ii, "group_code"));
      sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));
	
      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_group_name");
        wp.colSet(ii, columnData1, columnData);
      }
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
//            + " and   card_type = '" + wp.colStr(ii, "card_type") + "'";
        	  + " and   card_type = ?";
      if (wp.colStr(ii, "card_type").length() == 0)
        continue;
      params.clear();
      params.add(wp.colStr(ii, "card_type"));
      sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_name");
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
