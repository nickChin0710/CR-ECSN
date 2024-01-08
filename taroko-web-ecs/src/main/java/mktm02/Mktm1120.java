/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/13  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110-11-23  V1.00.05  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package mktm02;

import mktm02.Mktm1120Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1120 extends BaseEdit {
  private String PROGNAME = "新貴通貴賓室申請參數維護處理程式108/06/13 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1120Func func = null;
  String rowid, dataKK2;
  String orgTabName = "mkt_ppcard_apply";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%")
        + sqlCol(wp.itemStr("ex_bin_type"), "a.bin_type", "like%")
        + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%")
        + sqlCol(wp.itemStr("ex_vip_kind"), "a.vip_kind", "like%")
        + sqlCol(wp.itemStr("ex_vip_group_code"), "a.vip_group_code", "like%");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if ("1".equals(wp.itemStr("apply_type"))) {
      controlTabName = "mkt_ppcard_apply_t";
    } else {
      controlTabName = "mkt_ppcard_apply";
    }

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.card_type," + "a.group_code," + "a.proj_desc," + "a.bin_type," + "a.major_flag,"
        + "a.first_cond," + "a.last_amt_cond," + "a.nofir_cond," + "a.vip_kind,"
        + "a.vip_group_code," + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.card_purch_code";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.card_type,a.bin_type,a.group_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCardType("comm_card_type");
    commGroupCode("comm_group_code");
    commVipKind("comm_vip_kind");
    commVipGroupCode("comm_vip_group_code");

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

    if ("1".equals(wp.itemStr("apply_type"))) {
      controlTabName = "mkt_ppcard_apply_t";
    } else {
      controlTabName = "mkt_ppcard_apply";
    }

    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.group_code as group_code," + "a.card_type as card_type," + "a.bin_type,"
        + "a.proj_desc," + "a.major_flag," + "a.first_cond," + "a.fir_purch_mm,"
        + "a.fir_item_ename_bl," + "a.fir_item_ename_ca," + "a.fir_item_ename_it,"
        + "a.fir_it_type," + "a.fir_item_ename_id," + "a.fir_item_ename_ao,"
        + "a.fir_item_ename_ot," + "a.fir_min_amt," + "a.fir_amt_cond," + "a.fir_tot_amt,"
        + "a.fir_cnt_cond," + "a.fir_tot_cnt," + "a.item_ename_bl," + "a.item_ename_ca,"
        + "a.item_ename_it," + "a.it_type," + "a.item_ename_id," + "a.item_ename_ao,"
        + "a.item_ename_ot," + "a.last_amt_cond," + "a.last_tot_amt," + "a.nofir_cond,"
        + "a.purch_mm," + "a.min_amt," + "a.amt_cond," + "a.tot_amt," + "a.cnt_cond," + "a.tot_cnt,"
        + "a.vip_kind," + "a.vip_group_code," + "a.card_purch_code," + "a.crt_date," + "a.crt_user,"
        + "a.apr_date," + "a.apr_user," + "a.card_purch_code";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_group_code"), "a.group_code")
          + sqlCol(wp.itemStr("kk_card_type"), "a.card_type")
          + sqlCol(wp.itemStr("kk_vip_kind"), "a.vip_kind")
          + sqlCol(wp.itemStr("kk_vip_group_code"), "a.vip_group_code");
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
    commBinType("comm_bin_type");
    commVipKind("comm_vip_kind");
    commVipGroupCode("comm_vip_group_code");
    checkButtonOff();
    wp.colSet("apply_type", wp.itemStr("apply_type"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    // if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) return;

    mktm02.Mktm1120Func func = new mktm02.Mktm1120Func(wp);

    // 已覆核
    if ("0".equals(wp.itemStr("apply_type"))) {
      strAction = "A";
      String cardType = wp.itemStr("card_type");
      String groupCode = wp.itemStr("group_code");
      String vipKind = wp.itemStr("vip_kind");
      String vipGroupCode = wp.itemStr("vip_group_code");
      String binType = wp.itemStr("bin_type");

//      String sql1 = "select * " + " from mkt_ppcard_apply_t " + " where 1=1 " + " and card_type = '"
//          + cardType + "'" + " and group_code = '" + groupCode + "'" + " and vip_kind = '" + vipKind
//          + "'" + " and vip_group_code = '" + vipGroupCode + "'" + " and bin_type = '" + binType
//          + "'";
      String sql1 = "select * " + " from mkt_ppcard_apply_t " + " where 1=1 "
          + sqlCol(cardType, "card_type")
          + sqlCol(groupCode, "group_code")
          + sqlCol(vipKind, "vip_kind")
          + sqlCol(vipGroupCode, "vip_group_code")
          + sqlCol(binType, "bin_type");
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        alertErr2("已有未覆核資料");
      }
    }

    if (rc == 1) {
      rc = func.dbSave(strAction);
      if (rc != 1)
        alertErr2(func.getMsg());
    }

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
      if ((wp.respHtml.equals("mktm1120_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_group_code").length() > 0) {
          wp.optionKey = wp.colStr("kk_group_code");
        }
        if (wp.colStr("group_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
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
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_vip_group_code").length() > 0) {
          wp.optionKey = wp.colStr("kk_vip_group_code");
        }
        if (wp.colStr("vip_group_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_vip_group_code", "ptr_group_code", "trim(group_code)",
            "trim(group_name)", " where group_code in (select group_code from mkt_ppcard_issue) ");
      }
      if ((wp.respHtml.equals("mktm1120"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_vip_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_vip_group_code");
        }
        this.dddwList("dddw_vip_group_code", "ptr_group_code", "trim(group_code)",
            "trim(group_name)", " where group_code in (select group_code from mkt_ppcard_issue) ");
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
//          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
          + " where 1 = 1 " + sqlCol(wp.colStr(ii, "group_code"), "group_code");
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
  public void commBinType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " card_desc as column_card_desc " + " from ptr_bintable e,ptr_card_type f "
          + " where 1 = 1 "
//          + " and   e.card_type = '" + wp.colStr(ii, "card_type") + "'"
          + sqlCol(wp.colStr(ii, "card_type"), "e.card_type")
          + " and   e.card_type = f.card_type ";
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_card_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commVipKind(String vipKind) throws Exception {
    if ("1".equals(wp.colStr("vip_kind"))) {
      wp.colSet(vipKind, "新貴通");
    } else {
      wp.colSet(vipKind, "龍騰卡");
    }
    return;
  }

  // ************************************************************************
  public void commVipGroupCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 "
//          + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
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
