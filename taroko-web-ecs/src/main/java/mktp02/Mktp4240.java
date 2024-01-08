/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名   
* 110-11-08  V1.00.03  machao     SQL Injection                                                                                       *   
***************************************************************************/
package mktp02;

import mktp02.Mktp4240Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp4240 extends BaseProc {
  private String PROGNAME = "影城訂票商店參數檔處理程式108/08/06 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp4240Func func = null;
  String rowId;// kk2, kk3;
  String activeCode, storeNo, storeDate;
  String fstAprFlag = "";
  String orgTabName = "mkt_ticket_parm2_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlCol(wp.itemStr("ex_store_no"), "a.store_no", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%");

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
        + "a.aud_type," + "a.active_code," + "a.store_no," + "a.store_name," + "a.store_date_s,"
        + "a.store_date_e," + "a.ez_purchase_amt," + "a.p_deduct_bp," + "a.p_deduct_amt,"
        + "a.selfpay_amt," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code,a.store_no,a.crt_user";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commfuncAudType("aud_type");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowId = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (qFrom == 0)
      if (wp.itemStr("kk_active_code").length() == 0) {
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.active_code as active_code," + "c.active_date_s as active_date_s,"
        + "c.active_date_e as active_date_e," + "a.store_no as store_no,"
        + "a.store_date_s as store_date_s," + "a.crt_user," + "a.store_date_e," + "a.store_name,"
        + "a.ez_purchase_amt," + "a.origin_amt," + "a.selfpay_amt," + "a.p_deduct_bp,"
        + "a.p_deduct_amt";

    wp.daoTable =
        controlTabName + " a " + "JOIN mkt_ticket_parm1 c " + "ON a.active_code = c.active_code ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(activeCode, "a.active_code") + sqlCol(storeNo, "a.store_no")
          + sqlCol(storeDate, "a.store_date_s");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowId, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commActiveCode("comm_active_code");
    checkButtonOff();
    activeCode = wp.colStr("active_code");
    storeNo = wp.colStr("store_no");
    storeDate = wp.colStr("store_date_s");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataReadR3R();
    else {
      commfuncAudType("aud_type");
      listWkdataSpace();
    }
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "mkt_ticket_parm2";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.active_code as active_code," + "c.active_date_s as bef_active_date_s,"
        + "c.active_date_e as bef_active_date_e," + "a.store_no as store_no,"
        + "a.store_date_s as store_date_s," + "a.crt_user as bef_crt_user,"
        + "a.store_date_e as bef_store_date_e," + "a.store_name as bef_store_name,"
        + "a.ez_purchase_amt as bef_ez_purchase_amt," + "a.origin_amt as bef_origin_amt,"
        + "a.selfpay_amt as bef_selfpay_amt," + "a.p_deduct_bp as bef_p_deduct_bp,"
        + "a.p_deduct_amt as bef_p_deduct_amt";

    wp.daoTable =
        controlTabName + " a " + "JOIN mkt_ticket_parm1 c " + "ON a.active_code = c.active_code ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code") + sqlCol(storeNo, "a.store_no")
        + sqlCol(storeDate, "a.store_date_s");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commActiveCode("comm_active_code");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("store_date_e").equals(wp.colStr("bef_store_date_e")))
      wp.colSet("opt_store_date_e", "Y");

    if (!wp.colStr("store_name").equals(wp.colStr("bef_store_name")))
      wp.colSet("opt_store_name", "Y");

    if (!wp.colStr("ez_purchase_amt").equals(wp.colStr("bef_ez_purchase_amt")))
      wp.colSet("opt_ez_purchase_amt", "Y");

    if (!wp.colStr("origin_amt").equals(wp.colStr("bef_origin_amt")))
      wp.colSet("opt_origin_amt", "Y");

    if (!wp.colStr("selfpay_amt").equals(wp.colStr("bef_selfpay_amt")))
      wp.colSet("opt_selfpay_amt", "Y");

    if (!wp.colStr("p_deduct_bp").equals(wp.colStr("bef_p_deduct_bp")))
      wp.colSet("opt_p_deduct_bp", "Y");

    if (!wp.colStr("p_deduct_amt").equals(wp.colStr("bef_p_deduct_amt")))
      wp.colSet("opt_p_deduct_amt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("store_date_e", "");
      wp.colSet("store_name", "");
      wp.colSet("ez_purchase_amt", "");
      wp.colSet("origin_amt", "");
      wp.colSet("selfpay_amt", "");
      wp.colSet("p_deduct_bp", "");
      wp.colSet("p_deduct_amt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("store_date_e").length() == 0)
      wp.colSet("opt_store_date_e", "Y");

    if (wp.colStr("store_name").length() == 0)
      wp.colSet("opt_store_name", "Y");

    if (wp.colStr("ez_purchase_amt").length() == 0)
      wp.colSet("opt_ez_purchase_amt", "Y");

    if (wp.colStr("origin_amt").length() == 0)
      wp.colSet("opt_origin_amt", "Y");

    if (wp.colStr("selfpay_amt").length() == 0)
      wp.colSet("opt_selfpay_amt", "Y");

    if (wp.colStr("p_deduct_bp").length() == 0)
      wp.colSet("opt_p_deduct_bp", "Y");

    if (wp.colStr("p_deduct_amt").length() == 0)
      wp.colSet("opt_p_deduct_amt", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp4240Func func = new mktp02.Mktp4240Func(wp);

    String[] lsActiveCode = wp.itemBuff("active_code");
    String[] lsStoreNo = wp.itemBuff("store_no");
    String[] lsStoreDateS = wp.itemBuff("store_date_s");
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

      func.varsSet("active_code", lsActiveCode[rr]);
      func.varsSet("store_no", lsStoreNo[rr]);
      func.varsSet("store_date_s", lsStoreDateS[rr]);
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
  public void dddwSelect() {}

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
  public void commActiveCode(String actoveCode) throws Exception {
    commActiveCode(actoveCode, 0);
    return;
  }

  // ************************************************************************
  public void commActiveCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_ticket_parm1 "
//          + " where 1 = 1 " + " and   active_code = '" + wp.colStr(ii, befStr + "active_code")
//          + "'";
			+ "where 1=1 " + " and active_code = :active_code ";
      setString("active_code",wp.colStr(ii, befStr + "active_code"));
      if (wp.colStr(ii, befStr + "active_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_active_name");
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
