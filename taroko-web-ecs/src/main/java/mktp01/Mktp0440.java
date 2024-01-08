/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *     
***************************************************************************/
package mktp01;

import mktp01.Mktp0440Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0440 extends BaseProc {
  private String PROGNAME = "航空哩程兌換商品參數覆核處理程式108/01/29 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0440Func func = null;
  String rowid;
  String giftNo;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift_mile_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batch_no = "";
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
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_air_type"), "a.air_type", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%");

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

    wp.selectSQL =
        " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
            + "a.air_type," + "a.gift_no," + "a.cal_mile," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.air_type,a.crt_user";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAirType("comm_air_type");
    commGiftNo("comm_gift_no");

    commfuncAudType("aud_type");

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
      if (wp.itemStr("kk_gift_no").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.gift_no as gift_no," + "a.crt_user," + "a.air_type," + "a.cal_mile";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(giftNo, "a.gift_no");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commAirType("comm_air_type");
    checkButtonOff();
    giftNo = wp.colStr("gift_no");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataReadR3R();
    else
      commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "mkt_gift_mile";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.gift_no as gift_no," + "a.crt_user as bef_crt_user," + "a.air_type as bef_air_type,"
        + "a.cal_mile as bef_cal_mile";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(giftNo, "a.gift_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAirType("comm_air_type");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("air_type").equals(wp.colStr("bef_air_type")))
      wp.colSet("opt_air_type", "Y");
    commAirType("comm_air_type");
    commAirType("comm_bef_air_type", 1);

    if (!wp.colStr("cal_mile").equals(wp.colStr("bef_cal_mile")))
      wp.colSet("opt_cal_mile", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("air_type", "");
      wp.colSet("cal_mile", "");
    }
  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp01.Mktp0440Func func = new mktp01.Mktp0440Func(wp);

    String[] lsGiftNo = wp.itemBuff("gift_no");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("gift_no", lsGiftNo[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A"))
        rc = func.dbInsertA4();
      else if (lsAudType[rr].equals("U"))
        rc = func.dbUpdateU4();
      else if (lsAudType[rr].equals("D"))
        rc = func.dbDeleteD4();

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commAirType("comm_air_type");
        commGiftNo("comm_gift_no");
        commfuncAudType("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        func.dbDelete();
        this.sqlCommit(rc);
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
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
      if ((wp.respHtml.equals("mktp0440"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_air_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_air_type");
        }
        this.dddwList("dddw_air_type_b", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='GIFT_MILE'");
      }
    } catch (Exception ex) {
    }
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
  public void commAirType(String type) throws Exception {
    commAirType(type, 0);
    return;
  }

  // ************************************************************************
  public void commAirType(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_id = '" + wp.colStr(ii, befStr + "air_TYPE") + "'"
          + " and   wf_type = 'GIFT_MILE' ";
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commGiftNo(String giftNo) throws Exception {
    commGiftNo(giftNo, 0);
    return;
  }

  // ************************************************************************
  public void commGiftNo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from mkt_gift " + " where 1 = 1 "
          + " and   gift_no = '" + wp.colStr(ii, befStr + "gift_no") + "'";
      if (wp.colStr(ii, "gift_no").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_gift_name");
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
