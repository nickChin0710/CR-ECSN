/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                         *
* 110-07-22  V1.00.04   YangBo        漢化查詢字段，添加查詢字段'pass_type'    *
* 110-11-19  V1.00.05  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package mktm02;

import mktm02.Mktm1020Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1020 extends BaseEdit {
  private String PROGNAME = "市區停車手KEY資料審核作業處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1020Func func = null;
  String rowid;
  String tranSeqno;
  String fstAprFlag = "";
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
  String upGroupType = "0";

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
      wp.itemSet("aud_type", "A");
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U3";
      updateFuncU3R();
    } else if (eqIgno(wp.buttonCode, "I")) {/* 單獨新鄒功能 */
      strAction = "I";
      /*
       * kk1 = item_kk("data_k1"); kk2 = item_kk("data_k2"); kk3 = item_kk("data_k3");
       */
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFuncD3R();
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
    wp.whereStr =
        "WHERE 1=1 "
            + sqlStrend(wp.itemStr("ex_park_date_s_s"), wp.itemStr("ex_park_date_s_e"),
                "a.park_date_s")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
            + sqlChkEx(wp.itemStr("ex_verify_flag"), "3", "")
            + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
            + sqlCol(wp.itemStr("ex_park_vendor"), "a.park_vendor", "like%")
            + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "")
            + " and a.pass_type in ('2', '3') ";

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
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.park_date_s," + "a.park_date_e," + "a.card_no," + "a.park_vendor," + "a.park_hr,"
        + "a.free_hr," + "a.charge_amt," + "a.use_point," + "a.act_use_point," + "a.act_charge_amt,"
        + "decode(a.verify_flag,'','尚未審核','0','尚未審核','1','正常交易','2','本行吸收','3','廠商吸收') as verify_flag,"
        + "a.crt_date," + "decode(a.pass_type,'1','1_系統','2','2_人工','3','3_匯入') as pass_type";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by imp_date desc,tran_date desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commVerifyFlag("comm_verify_flag");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

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
        + "a.tran_seqno as tran_seqno," + "a.verify_flag," + "a.verify_remark,"
        + "b.id_no as id_no," + "b.chi_name as chi_name," + "a.acct_type," + "a.card_no,"
        + "a.park_vendor," + "a.station_id," + "a.err_code," + "a.park_date_s," + "a.park_time_s,"
        + "a.park_date_e," + "a.park_time_e," + "a.park_hr," + "a.free_hr," + "a.use_bonus_hr,"
        + "a.use_point," + "a.act_use_point," + "a.act_charge_amt," + "a.imp_date," + "a.file_name,"
        + "a.manual_reason," + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable =
        controlTabName + " a " + "LEFT OUTER JOIN crd_idno b " + "ON a.id_p_seqno = b.id_p_seqno ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(tranSeqno, "a.tran_seqno");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    commErrCode("comm_err_code");
    commAcctType("comm_acct_type");
    commParkVendor("comm_park_vendor");
    checkButtonOff();
    tranSeqno = wp.colStr("tran_seqno");
    commfuncAudType("aud_type");
    dataReadR3R();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    String sql1 = "select " + " id_no, " + " chi_name " + " from crd_idno " + " where 1 = 1 "
//        + " and   id_p_seqno = '" + wp.colStr("id_p_seqno") + "'";
        + sqlCol(wp.colStr("id_p_seqno"), "id_p_seqno");
    setSelectLimit(99999);
    sqlSelect(sql1);

    if (sqlRowNum > 0) {
      wp.colSet("id_no", sqlStr("id_no"));
      wp.colSet("chi_name", sqlStr("chi_name"));
    }

    return;
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.tran_seqno as tran_seqno,"
        + "a.verify_flag as verify_flag," + "a.verify_remark as verify_remark,"
        + "b.id_no as id_no," + "b.chi_name as chi_name," + "a.acct_type as acct_type,"
        + "a.card_no as card_no," + "a.park_vendor as park_vendor," + "a.station_id as station_id,"
        + "a.err_code as err_code," + "a.park_date_s as park_date_s,"
        + "a.park_time_s as park_time_s," + "a.park_date_e as park_date_e,"
        + "a.park_time_e as park_time_e," + "a.park_hr as park_hr," + "a.free_hr as free_hr,"
        + "a.use_bonus_hr as use_bonus_hr," + "a.use_point as use_point,"
        + "a.act_use_point as act_use_point," + "a.act_charge_amt as act_charge_amt,"
        + "a.imp_date as imp_date," + "a.file_name as file_name,"
        + "a.manual_reason as manual_reason," + "a.crt_user as crt_user,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable =
        controlTabName + " a " + "LEFT OUTER JOIN crd_idno b " + "ON a.id_p_seqno = b.id_p_seqno ";
    wp.whereStr = "where 1=1 " + sqlCol(tranSeqno, "a.tran_seqno");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAcctType("comm_acct_type");
    commParkVendor("comm_park_vendor");
    commErrCode("comm_err_code");
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    tranSeqno = wp.itemStr("tran_seqno");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      tranSeqno = wp.itemStr("tran_seqno");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y")) {
        qFrom = 0;
        controlTabName = orgTabName;
      }
    } else {
      strAction = "A";
      wp.itemSet("aud_type", "D");
      insertFunc();
    }
    dataRead();
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void updateFuncU3R() throws Exception {
    qFrom = 0;
    tranSeqno = wp.itemStr("tran_seqno");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1) {
        dataReadR3R();;
        datareadWkdata();
      }
    } else {
      tranSeqno = wp.itemStr("tran_seqno");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm02.Mktm1020Func func = new mktm02.Mktm1020Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktm1020"))) {
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
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("3")) {
      if (wp.itemStr("ex_verify_flag").length() == 0)
        return "";
      if (wp.itemStr("ex_verify_flag").equals("0"))
        return " and (verify_flag = '0' or verify_flag = '')";
      else
//        return " and verify_flag = '" + wp.itemStr("ex_verify_flag") + "' ";
        return sqlCol(wp.itemStr("ex_verify_flag"), "verify_flag");
    }
    return "";
  }

  // ************************************************************************
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
//          + " where 1 = 1 " + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
          + " where 1 = 1 " + sqlCol(wp.colStr(ii, "acct_type"), "acct_type");
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
  public void commParkVendor(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " vendor_name as column_vendor_name " + " from mkt_park_parm "
//          + " where 1 = 1 " + " and   park_vendor = '" + wp.colStr(ii, "park_vendor") + "'";
          + " where 1 = 1 " + sqlCol(wp.colStr(ii, "park_vendor"), "park_vendor");
      if (wp.colStr(ii, "park_vendor").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_vendor_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commErrCode(String cde1) throws Exception {
    String[] cde = {"01", "02", "05", "06", "10", "20", "00", "99"};
    String[] txt = {"卡號不存在", "無需扣點", "重覆轉入", "團代卡種有誤", "當日多筆", "點數不足", "正常扣點", "等待人線上工處理"};
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
