/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/26  V1.00.01   Allen Ho      Initial                              *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*
 * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *                                              *
* 110-10-29  V1.00.04  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package dbmm01;

import dbmm01.Dbmp0080Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Dbmp0080 extends BaseProc {
  private final String PROGNAME = "紅利積點兌換參數覆核處理程式108/11/26 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  dbmm01.Dbmp0080Func func = null;
  String rowid/* , kk2, kk3 */;
  String years, acctType, itemCode;
//  String fst_apr_flag = "";
  String orgTabName = "dbm_bpid_t";
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
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
    }
    else if (eqIgno(wp.buttonCode, "R2"))
    {// 明細查詢 -/
    	strAction = "R2";
    	dataReadR2();
    }
    else if (eqIgno(wp.buttonCode, "R3"))
    {// 明細查詢 -/
    	strAction = "R3";
    	dataReadR3();
    }
    else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_years"), "a.years", "like%")
        + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
        + sqlCol(wp.itemStr("ex_item_code"), "a.item_code", "like%")
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
        + "a.aud_type," + "a.years," + "a.acct_type," + "a.item_code," + "a.bp_type," + "a.give_bp,"
        + "a.bp_amt," + "a.bp_pnt," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.years,a.acct_type,a.item_code,a.crt_user";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commCrtuser("comm_crt_user");
    commItemCode("comm_item_code");
    commBpType("comm_bp_type");
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
      if (wp.itemStr("kk_years").length() == 0) {
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
        + "a.years as years," + "a.acct_type as acct_type," + "a.item_code as item_code,"
        + "a.crt_user," + "a.bp_type," + "a.give_bp," + "a.bp_amt," + "a.bp_pnt,"
        + "a.pos_entry_sel," + "a.group_code_sel," + "a.mcc_code_sel," + "a.merchant_sel,"
        + "a.mcht_group_sel," + "a.platform_kind_sel";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(years, "a.years") + sqlCol(acctType, "a.acct_type")
          + sqlCol(itemCode, "a.item_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commItemCodep("comm_item_code");
    commBpType1("comm_bp_type");
    commSelect4("comm_pos_entry_sel");
    commSelect2("comm_group_code_sel");
    commSelect5("comm_mcc_code_sel");
    commSelect1("comm_merchant_sel");
    commSelect6("comm_mcht_group_sel");
    commSelect6("comm_platform_kind_sel");
    commAcctType1("comm_acct_type");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    years = wp.colStr("years");
    acctType = wp.colStr("acct_type");
    itemCode = wp.colStr("item_code");
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
    controlTabName = "dbm_bpid";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.years as years," + "a.acct_type as acct_type," + "a.item_code as item_code,"
        + "a.crt_user as bef_crt_user," + "a.bp_type as bef_bp_type," + "a.give_bp as bef_give_bp,"
        + "a.bp_amt as bef_bp_amt," + "a.bp_pnt as bef_bp_pnt,"
        + "a.pos_entry_sel as bef_pos_entry_sel," + "a.group_code_sel as bef_group_code_sel,"
        + "a.mcc_code_sel as bef_mcc_code_sel," + "a.merchant_sel as bef_merchant_sel,"
        + "a.mcht_group_sel as bef_mcht_group_sel," + "a.platform_kind_sel as bef_platform_kind_sel";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(years, "a.years") + sqlCol(acctType, "a.acct_type")
        + sqlCol(itemCode, "a.item_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAcctType1("comm_acct_type");
    commItemCodep("comm_item_code");
    commBpType1("comm_bp_type");
    commSelect4("comm_pos_entry_sel");
    commSelect2("comm_group_code_sel");
    commSelect5("comm_mcc_code_sel");
    commSelect1("comm_merchant_sel");
    commSelect6("comm_mcht_group_sel");
    commSelect6("comm_platform_kind_sel");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("pos_entry_cnt", listDbmBnData("dbm_bn_data_t", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "4"));
    wp.colSet("group_code_sel_cnt", listDbmBnData("dbm_bn_data_t", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "2"));
    wp.colSet("mcc_code_sel_cnt", listDbmBnData("dbm_bn_data_t", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "5"));
    wp.colSet("merchant_sel_cnt", listDbmBnData("dbm_bn_data_t", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "1"));
    wp.colSet("mcht_group_sel_cnt", listDbmBnData("dbm_bn_data_t", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "6"));
    wp.colSet("platform_kind_sel_cnt", listDbmBnData("dbm_bn_data_t", "DBM_BPID",
            wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "P"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("bp_type").equals(wp.colStr("bef_bp_type")))
      wp.colSet("opt_bp_type", "Y");
    commBpType1("comm_bp_type");
    commBpType1("comm_bef_bp_type");

    if (!wp.colStr("give_bp").equals(wp.colStr("bef_give_bp")))
      wp.colSet("opt_give_bp", "Y");

    if (!wp.colStr("bp_amt").equals(wp.colStr("bef_bp_amt")))
      wp.colSet("opt_bp_amt", "Y");

    if (!wp.colStr("bp_pnt").equals(wp.colStr("bef_bp_pnt")))
      wp.colSet("opt_bp_pnt", "Y");

    if (!wp.colStr("pos_entry_sel").equals(wp.colStr("bef_pos_entry_sel")))
      wp.colSet("opt_pos_entry_sel", "Y");
    commSelect4("comm_pos_entry_sel");
    commSelect4("comm_bef_pos_entry_sel");

    wp.colSet("bef_pos_entry_cnt", listDbmBnData("dbm_bn_data", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "4"));
    if (!wp.colStr("pos_entry_cnt").equals(wp.colStr("bef_pos_entry_cnt")))
      wp.colSet("opt_pos_entry_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commSelect2("comm_group_code_sel");
    commSelect2("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt", listDbmBnData("dbm_bn_data", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("mcc_code_sel").equals(wp.colStr("bef_mcc_code_sel")))
      wp.colSet("opt_mcc_code_sel", "Y");
    commSelect5("comm_mcc_code_sel");
    commSelect5("comm_bef_mcc_code_sel");

    wp.colSet("bef_mcc_code_sel_cnt", listDbmBnData("dbm_bn_data", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "5"));
    if (!wp.colStr("mcc_code_sel_cnt").equals(wp.colStr("bef_mcc_code_sel_cnt")))
      wp.colSet("opt_mcc_code_sel_cnt", "Y");

    if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
      wp.colSet("opt_merchant_sel", "Y");
    commSelect1("comm_merchant_sel");
    commSelect1("comm_bef_merchant_sel");

    wp.colSet("bef_merchant_sel_cnt", listDbmBnData("dbm_bn_data", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "1"));
    if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
      wp.colSet("opt_merchant_sel_cnt", "Y");

    if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
      wp.colSet("opt_mcht_group_sel", "Y");
    commSelect6("comm_mcht_group_sel");
    commSelect6("comm_bef_mcht_group_sel");
    
    if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
        wp.colSet("opt_platform_kind_sel", "Y");
      commSelect6("comm_platform_kind_sel");
      commSelect6("comm_bef_platform_kind_sel");
    
    wp.colSet("bef_mcht_group_sel_cnt", listDbmBnData("dbm_bn_data", "DBM_BPID",
        wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "6"));
    if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
      wp.colSet("opt_mcht_group_sel_cnt", "Y");
    
    wp.colSet("bef_platform_kind_sel_cnt", listDbmBnData("dbm_bn_data", "DBM_BPID",
            wp.colStr("years") + wp.colStr("acct_type") + wp.colStr("item_code"), "P"));
        if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
     wp.colSet("opt_platform_kind_sel_cnt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("bp_type", "");
      wp.colSet("give_bp", "");
      wp.colSet("bp_amt", "");
      wp.colSet("bp_pnt", "");
      wp.colSet("pos_entry_sel", "");
      wp.colSet("pos_entry_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("mcc_code_sel", "");
      wp.colSet("mcc_code_sel_cnt", "");
      wp.colSet("merchant_sel", "");
      wp.colSet("merchant_sel_cnt", "");
      wp.colSet("mcht_group_sel", "");
      wp.colSet("mcht_group_sel_cnt", "");
      wp.colSet("platform_kind_sel", "");
      wp.colSet("platform_kind_sel_cnt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("bp_type").length() == 0)
      wp.colSet("opt_bp_type", "Y");

    if (wp.colStr("give_bp").length() == 0)
      wp.colSet("opt_give_bp", "Y");

    if (wp.colStr("bp_amt").length() == 0)
      wp.colSet("opt_bp_amt", "Y");

    if (wp.colStr("bp_pnt").length() == 0)
      wp.colSet("opt_bp_pnt", "Y");

    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("opt_pos_entry_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("opt_mcc_code_sel", "Y");


    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("opt_merchant_sel", "Y");


    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("opt_mcht_group_sel", "Y");


    if (wp.colStr("platform_kind_sel").length() == 0)
      wp.colSet("opt_platform_kind_sel", "Y");
    
  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int isok = 0;
    int err = 0;
    int ilAuth = 0;
    dbmm01.Dbmp0080Func func = new dbmm01.Dbmp0080Func(wp);

    String[] lsYears = wp.itemBuff("years");
    String[] lsAcctType = wp.itemBuff("acct_type");
    String[] lsItemCode = wp.itemBuff("item_code");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsCrtUser = wp.itemBuff("crt_user");
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
      //if (lsCrtUser[rr].equals(wp.loginUser)) {
      //  ilAuth++;
      //  wp.colSet(rr, "ok_flag", "F");
      //  continue;
      // }

      func.varsSet("years", lsYears[rr]);
      func.varsSet("acct_type", lsAcctType[rr]);
      func.varsSet("item_code", lsItemCode[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commAcctType("comm_acct_type");
        commCrtuser("comm_crt_user");
        commItemCode("comm_item_code");
        commBpType("comm_bp_type");
        commfuncAudType("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        isok++;
        func.dbDelete();
        this.sqlCommit(rc);
        continue;
      }
      err++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + isok + "; 失敗筆數=" + err + "; 權限問題=" + ilAuth);
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
      if ((wp.respHtml.equals("dbmp0080"))) {
        wp.initOption = "";
        wp.optionKey = itemKk("ex_acct_type");
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
        this.dddwList("dddw_acct_type1", "dbp_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_crt_user").length()>0)
           {
           wp.optionKey = wp.colStr("ex_crt_user");
           }
        lsSql = "";
        lsSql =  procDynamicDddwCrtuser1(wp.colStr("ex_crt_user"));
        wp.optionKey = wp.colStr("ex_crt_user");
        dddwList("dddw_crt_user_1", lsSql);

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
  public void commAcctType1(String acctType) throws Exception {
    commAcctType1(acctType, 0);
    return;
  }

  // ************************************************************************
  public void commAcctType1(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from dbp_acct_type "
          + " where 1 = 1 "
//              + " and   acct_type = '" + wp.colStr(ii, befStr + "acct_type") + "'";
          + sqlCol(wp.colStr(ii, befStr + "acct_type"), "acct_type");
      if (wp.colStr(ii, befStr + "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commAcctType(String acctType) throws Exception {
    commAcctType(acctType, 0);
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
      sql1 = "select " + " chin_name as column_chin_name   " + " from dbp_acct_type "
          + " where 1 = 1 "
//              + " and   acct_type = '" + wp.colStr(ii, befStr + "acct_type") + "'";
          + sqlCol(wp.colStr(ii, befStr + "acct_type"), "acct_type");
      if (wp.colStr(ii, befStr + "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commItemCodep(String cde1) throws Exception {
    String[] cde = {"1", "2", "3"};
    String[] txt = {"國內刷卡", "國外刷卡", "國>外提款"};
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
  public void commBpType1(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"每筆交易", "交易金額"};
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
  public void commSelect4(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部","指定","排除"};
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
  public void commSelect2(String cde1) throws Exception {
	String[] cde = {"0", "1", "2"};
	String[] txt = {"全部","指定","排除"};
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
  public void commSelect5(String txt1) throws Exception {
	String[] cde = {"0", "1", "2"};
	String[] txt = {"全部","指定","排除"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String cde1 = txt1.substring(5, txt1.length());
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, txt1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commSelect1(String txt1) throws Exception {
	String[] cde = {"0", "1", "2"};
	String[] txt = {"全部","指定","排除"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String cde1 = txt1.substring(5, txt1.length());
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, txt1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commSelect6(String cde1) throws Exception {
	String[] cde = {"0", "1", "2"};
	String[] txt = {"全部","指定","排除"};
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
  public void commItemCode(String cde1) throws Exception {
    String[] cde = {"1", "2", "3"};
    String[] txt = {"國內刷卡", "國外刷卡", "國>外提款"};
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
  public void commBpType(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"每筆交易", "交易金額"};
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
  String listDbmBnData(String table, String tablename, String dataKey, String datatype) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 "
//            + " and   table_name = '" + tablename + "'"
        + sqlCol(tablename, "table_name")
//            + " and   data_key   = '" + dataKey + "'"
        + sqlCol(dataKey, "data_key")
//        + " and   data_type  = '" + datatype + "'";
        + sqlCol(datatype, "data_type");
    sqlSelect(sql1);

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }

//************************************************************************
public String procDynamicDddwCrtuser1(String int1)  throws Exception
{
  String lsSql = "";

  lsSql = " select "
         + " b.crt_user as db_code, "
         + " max(b.crt_user||' '||a.usr_cname) as db_desc "
         + " from sec_user a,dbm_bpid_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
}

//************************************************************************
public int selectBndataCount(String bndata_table,String whereStr ) throws Exception
{
String sql1 = "select count(*) as bndataCount"
            + " from " + bndata_table
            + " " + whereStr
            ;

sqlSelect(sql1);

return((int)sqlNum("bndataCount"));
}

//************************************************************************
public void commEntryMode(String mode) throws Exception
{
	commEntryMode(mode,0);
return;
}
//************************************************************************
public void commEntryMode(String columnData1,int bef_type) throws Exception
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mode_desc as column_mode_desc "
         + " from cca_entry_mode "
         + " where 1 = 1 "
//         + " and   entry_mode = '"+wp.colStr(ii,befStr+"data_code")+"'"
//         ;
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "entry_mode");
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mode_desc");
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commGroupcode(String groupCode) throws Exception
{
	commGroupcode(groupCode,0);
return;
}
//************************************************************************
public void commGroupcode(String columnData1,int bef_type) throws Exception
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " group_name as column_group_name "
         + " from ptr_group_code "
         + " where 1 = 1 "
//         + " and   group_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
//         ;
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "group_code");
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_group_name");
    wp.colSet(ii, columnData1, columnData);
   }
return;
}
//************************************************************************
public void commDatacode07(String dataCode) throws Exception
{
	commDatacode07(dataCode,0);
return;
}
//************************************************************************
public void commDatacode07(String columnData1,int bef_type) throws Exception
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mcc_remark as column_mcc_remark "
         + " from cca_mcc_risk "
         + " where 1 = 1 "
//         + " and   mcc_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
//         ;
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "mcc_code");
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcc_remark");
    wp.colSet(ii, columnData1, columnData);
   }
return;
}
//************************************************************************
public void commMechtgp(String mechtGp) throws Exception
{
	commMechtgp(mechtGp,0);
return;
}
//************************************************************************
public void commMechtgp(String columnData1,int bef_type) throws Exception
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mcht_group_desc as column_mcht_group_desc "
         + " from mkt_mcht_gp "
         + " where 1 = 1 and platform_flag != '2' "
//         + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
//         ;
         + sqlCol(wp.colStr(ii,befStr+"data_code"), "mcht_group_id");
    if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcht_group_desc");
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

//************************************************************************
public void commMechtgp2(String mechtGp) throws Exception
{
	commMechtgp2(mechtGp,0);
return;
}
//************************************************************************
public void commMechtgp2(String columnData1,int bef_type) throws Exception
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
 {
  columnData="";
  sql1 = "select "
       + " mcht_group_desc as column_mcht_group_desc "
       + " from mkt_mcht_gp "
       + " where 1 = 1 and platform_flag = '2'"
//       + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
//       ;
       + sqlCol(wp.colStr(ii,befStr+"data_code"), "mcht_group_id");
  if (wp.colStr(ii,befStr+"data_code").length()==0) continue;
  sqlSelect(sql1);

  if (sqlRowNum>0)
     columnData = columnData + sqlStr("column_mcht_group_desc");
  wp.colSet(ii, columnData1, columnData);
 }
return;
}

//************************************************************************
public void dataReadR2() throws Exception
{


	String bnTable="";
	wp.selectCnt=1;
	commAcctType("comm_acct_type");
	commItemCode("comm_item_code");
	this.selectNoLimit();
	bnTable = "dbm_bn_data_t";

	wp.selectSQL = "hex(rowid) as r2_rowid, "
	             + "ROW_NUMBER()OVER() as ser_num, "
	             + "mod_seqno as r2_mod_seqno, "
	             + "data_key, "
	             + "data_code, "
	             + "mod_user as r2_mod_user "
	             ;
	wp.daoTable = bnTable ;
	wp.whereStr = "where 1=1"
	           + " and table_name  =  'DBM_BPID' "
	             ;
	if (wp.respHtml.equals("dbmp0080_enty"))
	   wp.whereStr  += " and data_type  = '4' ";
	if (wp.respHtml.equals("dbmp0080_grop"))
	   wp.whereStr  += " and data_type  = '2' ";
	if (wp.respHtml.equals("dbmp0080_mccd"))
	   wp.whereStr  += " and data_type  = '5' ";
	if (wp.respHtml.equals("dbmp0080_aaa1"))
	   wp.whereStr  += " and data_type  = '6' ";
	if (wp.respHtml.equals("dbmp0080_aaa2"))
	   wp.whereStr  += " and data_type  = 'P' ";
	String whereCnt = wp.whereStr;
//	whereCnt += " and  data_key = '"+wp.itemStr("years")+wp.itemStr("acct_type")+wp.itemStr("item_code")+"'";
    whereCnt += sqlCol(wp.itemStr("years")+wp.itemStr("acct_type")+wp.itemStr("item_code"), "data_key");




	int cnt1=selectBndataCount(wp.daoTable,whereCnt);
	if (cnt1>300)
	   {
		alertMsg("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
	    buttonOff("btnUpdate_disable");
	    buttonOff("newDetail_disable");
	    return;
	   }

	wp.whereStr  += " and  data_key = :data_key ";
	wp.whereStr  += " and  data_key = :data_key ";
	setString("data_key",wp.itemStr("years")+wp.itemStr("acct_type")+wp.itemStr("item_code"));
	wp.whereStr  += " order by 4,5 ";

	pageQuery();
	wp.setListCount(1);
	wp.notFound = "";

	wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
	if (wp.respHtml.equals("dbmp0080_enty"))
		commEntryMode("comm_data_code");
	if (wp.respHtml.equals("dbmp0080_grop"))
		commGroupcode("comm_data_code");
	if (wp.respHtml.equals("dbmp0080_mccd"))
		commDatacode07("comm_data_code");
	if (wp.respHtml.equals("dbmp0080_aaa1"))
		commMechtgp("comm_data_code");
	if (wp.respHtml.equals("dbmp0080_aaa2"))
		commMechtgp2("comm_data_code");
}

//************************************************************************
public void dataReadR3() throws Exception
{
String bnTable="";

wp.selectCnt=1;
commAcctType("comm_acct_type");
commItemCode("comm_item_code");
this.selectNoLimit();
bnTable = "dbm_bn_data_t";

wp.selectSQL = "hex(rowid) as r2_rowid, "
             + "ROW_NUMBER()OVER() as ser_num, "
             + "mod_seqno as r2_mod_seqno, "
             + "data_key, "
             + "data_code, "
             + "data_code2, "
             + "mod_user as r2_mod_user "
             ;
wp.daoTable = bnTable ;
wp.whereStr = "where 1=1"
           + " and table_name  =  'DBM_BPID' "
             ;
if (wp.respHtml.equals("dbmp0080_mcht"))
   wp.whereStr  += " and data_type  = '1' ";
String whereCnt = wp.whereStr;
//whereCnt += " and  data_key = '"+wp.itemStr("years")+wp.itemStr("acct_type")+wp.itemStr("item_code")+"'";
  whereCnt += sqlCol(wp.itemStr("years")+wp.itemStr("acct_type")+wp.itemStr("item_code"), "data_key");
int cnt1=selectBndataCount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
	alertMsg("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    return;
   }

wp.whereStr  += " and  data_key = :data_key ";
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key",wp.itemStr("years")+wp.itemStr("acct_type")+wp.itemStr("item_code"));
wp.whereStr  += " order by 4,5,6 ";

pageQuery();
wp.setListCount(1);
wp.notFound = "";

wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
}

//************************************************************************
public void commCrtuser(String user) throws Exception
{
	commCrtuser(user,0);
return;
}
//************************************************************************
public void commCrtuser(String columnData1,int bef_type) throws Exception
{
String columnData="";
String sql1 = "";
String befStr="";
if (bef_type==1) befStr="bef_";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " usr_cname as column_usr_cname "
         + " from sec_user "
         + " where 1 = 1 "
//         + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
//         ;
         + sqlCol(wp.colStr(ii,befStr+"crt_user"), "usr_id");
    if (wp.colStr(ii,befStr+"crt_user").length()==0) continue;
    sqlSelect(sql1);

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_usr_cname");
    wp.colSet(ii, columnData1, columnData);
   }
return;
}
} // End of class
