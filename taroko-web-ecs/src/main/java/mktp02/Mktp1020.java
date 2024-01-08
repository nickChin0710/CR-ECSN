/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名  
* 110-07-08  v1.00.04   MaChao        新增匯入檔案的邏輯處理 
* 110-11-16  V1.00.03   machao     SQL Injection                                          *   
***************************************************************************/
package mktp02;

import mktp02.Mktp1020Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1020 extends BaseProc {
  private String PROGNAME = "市區停車手KEY資料審核作業處理程式108/08/06 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp1020Func func = null;
  String rowid;
  String tranSeqno;
  String fstAprlag = "";
  String orgTabName = "mkt_dodo_resp_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, reCnt = 0, notifyCnt = 0;
  int[] datachk_cnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
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
      strAction = "C";
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
    wp.whereStr =
        "WHERE 1=1 "
            + sqlStrend(wp.itemStr("ex_park_date_s_s"), wp.itemStr("ex_park_date_s_e"),
                "a.park_date_s")
            + sqlCol(wp.itemStr("ex_verify_flag"), "a.verify_flag", "like%")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
            + sqlCol(wp.itemStr("ex_park_vendor"), "a.park_vendor", "like%")
            + " and a.verify_flag  !=  '' " + " and a.pass_type  in('2','3')";

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
        + "a.aud_type," + "a.crt_date," + "a.verify_flag," + "a.pass_type,"+"a.card_no," + "a.park_vendor,"
        + "a.park_hr," + "a.free_hr," + "a.charge_amt," + "a.use_point," + "a.act_use_point,"
        + "a.act_charge_amt," + "a.tran_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by crt_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commVerifyFlag("comm_verify_flag");
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
      if (wp.itemStr("kk_tran_seqno").length() == 0) {
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
        + "a.tran_seqno as tran_seqno," + "a.crt_user," + "b.id_no as id_no,"
        + "b.chi_name as chi_name," + "a.acct_type," + "a.card_no," + "a.park_vendor,"
        + "a.park_hr," + "a.free_hr," + "a.use_bonus_hr," + "a.use_point," + "a.manual_reason,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.verify_flag," + "a.verify_remark";

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
    commVerifyFlag("comm_verify_flag");
    commAcctType("comm_acct_type");
    commParkVendor("comm_park_vendor");
    checkButtonOff();
    tranSeqno = wp.colStr("tran_seqno");
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
    controlTabName = "mkt_dodo_resp";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.tran_seqno as tran_seqno," + "a.crt_user as bef_crt_user," + "b.id_no as bef_id_no,"
        + "b.chi_name as bef_chi_name," + "a.acct_type as bef_acct_type,"
        + "a.card_no as bef_card_no," + "a.park_vendor as bef_park_vendor,"
        + "a.park_hr as bef_park_hr," + "a.free_hr as bef_free_hr,"
        + "a.use_bonus_hr as bef_use_bonus_hr," + "a.use_point as bef_use_point,"
        + "a.manual_reason as bef_manual_reason," + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
        + "a.verify_flag as bef_verify_flag," + "a.verify_remark as bef_verify_remark";

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
    commVerifyFlag("comm_verify_flag");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("verify_flag").equals(wp.colStr("bef_verify_flag")))
      wp.colSet("opt_verify_flag", "Y");
    commVerifyFlag("comm_verify_flag");
    commVerifyFlag("comm_bef_verify_flag");

    if (!wp.colStr("verify_remark").equals(wp.colStr("bef_verify_remark")))
      wp.colSet("opt_verify_remark", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("verify_flag", "");
      wp.colSet("verify_remark", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("verify_flag").length() == 0)
      wp.colSet("opt_verify_flag", "Y");

    if (wp.colStr("verify_remark").length() == 0)
      wp.colSet("opt_verify_remark", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp1020Func func = new mktp02.Mktp1020Func(wp);

    String[] lsTranSeqno = wp.itemBuff("tran_seqno");
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

      func.varsSet("tran_seqno", lsTranSeqno[rr]);
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
        commVerifyFlag("comm_verify_flag");
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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktp1020"))) {
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
  public void commAcctType(String type) throws Exception {
    commAcctType(type, 0);
    return;
  }

  // ************************************************************************
  public void commAcctType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    	  + " and   acct_type = '" + wp.colStr(ii, befStr + "acct_type") + "'";
	      + " and   acct_type = :acct_type ";
	  	  setString("acct_type",wp.colStr(ii, befStr + "acct_type"));
      if (wp.colStr(ii, befStr + "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_chin_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commParkVendor(String parkVndor) throws Exception {
    commParkVndor(parkVndor, 0);
    return;
  }

  // ************************************************************************
  public void commParkVndor(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " vendor_name as column_vendor_name " + " from mkt_park_parm "
          + " where 1 = 1 " 
//    	  + " and   park_vendor = '" + wp.colStr(ii, befStr + "park_vendor") + "'";
	      + " and   park_vendor = :park_vendor ";
	  	  setString("park_vendor",wp.colStr(ii, befStr + "park_vendor"));
      if (wp.colStr(ii, befStr + "park_vendor").length() == 0)
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
  public void commVerifyFlag(String cde1) throws Exception {
    String[] cde = {"0", "1", "2", "3"};
    String[] txt = {"尚未審核", "正常交易", "本行吸收", "廠商吸收"};
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
