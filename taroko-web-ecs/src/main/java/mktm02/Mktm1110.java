/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/12  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110-11-23  V1.00.05  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package mktm02;

import mktm02.Mktm1110Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1110 extends BaseEdit {
  private String PROGNAME = "新貴通貴賓室發行參數維護處理程式108/06/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1110Func func = null;
  String binType, groupCode, vipKind;
  String orgTabName = "mkt_ppcard_issue";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_bin_type"), "a.bin_type", "like%")
        + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%")
        + sqlCol(wp.itemStr("ex_vip_kind"), "a.vip_kind", "like%");

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
        + "a.bin_type," + "a.issue_seq," + "a.group_code," + "a.card_type," + "a.valid_month,"
        + "a.holder_amt," + "a.toget_amt," + "a.ppcard_bin_no," + "a.ppcard_ica_no," + "a.lost_fee,"
        + "a.vip_kind," + "a.make_fee";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by issue_seq";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGroupCode("comm_group_code");
    commCardType("comm_card_type");

    commVmjCode("comm_bin_type");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    binType = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (qFrom == 0)
      if (wp.itemStr("kk_bin_type").length() == 0) {
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
        + "a.bin_type as bin_type," + "a.group_code as group_code," + "a.issue_seq,"
        + "a.card_type," + "a.valid_month," + "a.holder_amt," + "a.toget_amt," + "a.ppcard_bin_no,"
        + "a.ppcard_ica_no," + "a.ppcard_seqno," + "a.lost_fee," + "a.vip_kind," + "a.make_fee,"
        + "a.crt_date," + "a.crt_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
        + "a.mod_user," + "a.apr_date," + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_bin_type"), "a.bin_type")
          + sqlCol(wp.itemStr("kk_group_code"), "a.group_code")
          + sqlCol(wp.itemStr("kk_vip_kind"), "a.vip_kind");
      binType = wp.itemStr("kk_bin_type");
      groupCode = wp.itemStr("kk_group_code");
      vipKind = wp.itemStr("kk_vip_kind");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(binType, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      if (qFrom == 0) {
        alertErr2("查無資料, key= " + "[" + binType + "]" + "[" + groupCode + "]" + "[" + vipKind + "]");
      } else {
        alertErr2("查無資料, key= " + "[" + binType + "]");
      }
      return;
    }
    commGroupCode("comm_group_code");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    mktm02.Mktm1110Func func = new mktm02.Mktm1110Func(wp);

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
      buttonOff("btOther_disable");
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktm1110_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_group_code").length() > 0) {
          wp.optionKey = wp.colStr("kk_group_code");
          wp.initOption = "";
        }
        if (wp.colStr("group_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_group_code1", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("card_type").length() > 0) {
          wp.optionKey = wp.colStr("card_type");
        }
        if (wp.colStr("card_type").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm1110"))) {
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
  public void commGroupCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 "
//              + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
          + sqlCol(wp.colStr(ii, "group_code"), "group_code");
      if (wp.colStr(ii, "group_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

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
//          + " and   card_type = '" + wp.colStr(ii, "card_type") + "'";
          + sqlCol(wp.colStr(ii, "card_type"), "card_type");
      if (wp.colStr(ii, "card_type").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commVmjCode(String cde1) throws Exception {
    String[] cde = {"V", "M", "J"};
    String[] txt = {"VISA", "MasterCard", "JCB"};
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
	if (eqIgno(wp.respHtml, "mktm1110_detl")) {
		wp.colSet("ppcard_seqno","0");
	}
    return;
  }
  // ************************************************************************

} // End of class
