/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/14  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110/11/15  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktp01;

import mktp01.Mktp0240Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktp0240 extends BaseProc {
  private String PROGNAME = "紅利積點兌換贈品登錄檔維護作業處理程式108/10/14 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0240Func func = null;
  String rowid;
  String giftNo;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no", "like%")
        + sqlCol(wp.itemStr("ex_gift_typeno"), "a.gift_typeno", "like%");

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
        + "a.aud_type," + "a.gift_no," + "a.gift_name," + "a.gift_typeno," + "a.gift_type,"
        + "a.effect_months," + "a.cash_value,"
        + "decode(gift_type,'3',max_limit_count,supply_count) as supply_count,"
        + "decode(gift_type,'3',use_limit_count,use_count) as use_count,"
        + "decode(gift_type,'3',net_limit_count,net_count) as net_count," + "a.web_sumcnt,"
        + "a.vendor_no," + "a.crt_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.gift_no,a.gift_typeno";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGiftTypefo("comm_gift_typeno");
    commCrtuser("comm_crt_user");
    commGiftType("comm_gift_type");
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
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.gift_no as gift_no," + "a.crt_user," + "a.bonus_type," + "a.gift_typeno,"
        + "a.gift_name," + "a.disable_flag," + "a.gift_type," + "a.effect_months," + "a.redem_days,"
        + "a.fund_code," + "a.air_type," + "a.cal_mile," + "a.cash_value," + "a.supply_count,"
        + "a.use_count," + "a.net_count," + "a.max_limit_count," + "a.use_limit_count,"
        + "a.net_limit_count," + "a.web_sumcnt," + "a.limit_last_date," + "a.vendor_no";

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
    commDusableFlag("comm_disable_flag");
    commGiftType("comm_gift_type");
    commGiftName("comm_gift_no");
    commBonusType("comm_bonus_type");
    commGiftTypefo("comm_gift_typeno");
    commAirType("comm_air_type");
    commVendorNo1("comm_vendor_no");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    giftNo = wp.colStr("gift_no");
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
    controlTabName = "MKT_GIFT";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.gift_no as gift_no," + "a.crt_user as bef_crt_user,"
        + "a.bonus_type as bef_bonus_type," + "a.gift_typeno as bef_gift_typeno,"
        + "a.gift_name as bef_gift_name," + "a.disable_flag as bef_disable_flag,"
        + "a.gift_type as bef_gift_type," + "a.effect_months as bef_effect_months,"
        + "a.redem_days as bef_redem_days," + "a.fund_code as bef_fund_code,"
        + "a.air_type as bef_air_type," + "a.cal_mile as bef_cal_mile,"
        + "a.cash_value as bef_cash_value," + "a.supply_count as bef_supply_count,"
        + "a.use_count as bef_use_count," + "a.net_count as bef_net_count,"
        + "a.max_limit_count as bef_max_limit_count," + "a.use_limit_count as bef_use_limit_count,"
        + "a.net_limit_count as bef_net_limit_count," + "a.web_sumcnt as bef_web_sumcnt,"
        + "a.limit_last_date as bef_limit_last_date," + "a.vendor_no as bef_vendor_no";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(giftNo, "a.gift_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commGiftName("comm_gift_no");
    commBonusType("comm_bonus_type");
    commGiftTypefo("comm_gift_typeno");
    commDusableFlag("comm_disable_flag");
    commGiftType("comm_gift_type");
    commAirType("comm_air_type");
    commVendorNo1("comm_vendor_no");
    commCrtuser("comm_crt_user");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("exchg_cnt",
        listMktGiftExchgdata("mkt_gift_exchgdata_t", "", wp.colStr("gift_no"), ""));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
      wp.colSet("opt_bonus_type", "Y");
    commBonusType("comm_bonus_type");
    commBonusType("comm_bef_bonus_type", 1);

    if (!wp.colStr("gift_typeno").equals(wp.colStr("bef_gift_typeno")))
      wp.colSet("opt_gift_typeno", "Y");
    commGiftTypefo("comm_gift_typeno");
    commGiftTypefo("comm_bef_gift_typeno", 1);

    if (!wp.colStr("gift_name").equals(wp.colStr("bef_gift_name")))
      wp.colSet("opt_gift_name", "Y");

    if (!wp.colStr("disable_flag").equals(wp.colStr("bef_disable_flag")))
      wp.colSet("opt_disable_flag", "Y");
    commDusableFlag("comm_disable_flag");
    commDusableFlag("comm_bef_disable_flag");

    if (!wp.colStr("gift_type").equals(wp.colStr("bef_gift_type")))
      wp.colSet("opt_gift_type", "Y");
    commGiftType("comm_gift_type");
    commGiftType("comm_bef_gift_type");

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months", "Y");

    if (!wp.colStr("redem_days").equals(wp.colStr("bef_redem_days")))
      wp.colSet("opt_redem_days", "Y");

    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code", "Y");

    if (!wp.colStr("air_type").equals(wp.colStr("bef_air_type")))
      wp.colSet("opt_air_type", "Y");
    commAirType("comm_air_type");
    commAirType("comm_bef_air_type", 1);

    if (!wp.colStr("cal_mile").equals(wp.colStr("bef_cal_mile")))
      wp.colSet("opt_cal_mile", "Y");

    if (!wp.colStr("cash_value").equals(wp.colStr("bef_cash_value")))
      wp.colSet("opt_cash_value", "Y");

    if (!wp.colStr("supply_count").equals(wp.colStr("bef_supply_count")))
      wp.colSet("opt_supply_count", "Y");

    if (!wp.colStr("use_count").equals(wp.colStr("bef_use_count")))
      wp.colSet("opt_use_count", "Y");

    if (!wp.colStr("net_count").equals(wp.colStr("bef_net_count")))
      wp.colSet("opt_net_count", "Y");

    if (!wp.colStr("max_limit_count").equals(wp.colStr("bef_max_limit_count")))
      wp.colSet("opt_max_limit_count", "Y");

    if (!wp.colStr("use_limit_count").equals(wp.colStr("bef_use_limit_count")))
      wp.colSet("opt_use_limit_count", "Y");

    if (!wp.colStr("net_limit_count").equals(wp.colStr("bef_net_limit_count")))
      wp.colSet("opt_net_limit_count", "Y");

    if (!wp.colStr("web_sumcnt").equals(wp.colStr("bef_web_sumcnt")))
      wp.colSet("opt_web_sumcnt", "Y");

    if (!wp.colStr("limit_last_date").equals(wp.colStr("bef_limit_last_date")))
      wp.colSet("opt_limit_last_date", "Y");

    wp.colSet("bef_exchg_cnt",
        listMktGiftExchgdata("mkt_gift_exchgdata", "", wp.colStr("gift_no"), ""));
    if (!wp.colStr("exchg_cnt").equals(wp.colStr("bef_exchg_cnt")))
      wp.colSet("opt_exchg_cnt", "Y");

    if (!wp.colStr("vendor_no").equals(wp.colStr("bef_vendor_no")))
      wp.colSet("opt_vendor_no", "Y");
    commVendorNo1("comm_vendor_no");
    commVendorNo1("comm_bef_vendor_no", 1);

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("bonus_type", "");
      wp.colSet("gift_typeno", "");
      wp.colSet("gift_name", "");
      wp.colSet("disable_flag", "");
      wp.colSet("gift_type", "");
      wp.colSet("effect_months", "");
      wp.colSet("redem_days", "");
      wp.colSet("fund_code", "");
      wp.colSet("air_type", "");
      wp.colSet("cal_mile", "");
      wp.colSet("cash_value", "");
      wp.colSet("supply_count", "");
      wp.colSet("use_count", "");
      wp.colSet("net_count", "");
      wp.colSet("max_limit_count", "");
      wp.colSet("use_limit_count", "");
      wp.colSet("net_limit_count", "");
      wp.colSet("web_sumcnt", "");
      wp.colSet("limit_last_date", "");
      wp.colSet("exchg_cnt", "");
      wp.colSet("vendor_no", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("bonus_type").length() == 0)
      wp.colSet("opt_bonus_type", "Y");

    if (wp.colStr("gift_typeno").length() == 0)
      wp.colSet("opt_gift_typeno", "Y");

    if (wp.colStr("gift_name").length() == 0)
      wp.colSet("opt_gift_name", "Y");

    if (wp.colStr("disable_flag").length() == 0)
      wp.colSet("opt_disable_flag", "Y");

    if (wp.colStr("gift_type").length() == 0)
      wp.colSet("opt_gift_type", "Y");

    if (wp.colStr("effect_months").length() == 0)
      wp.colSet("opt_effect_months", "Y");

    if (wp.colStr("redem_days").length() == 0)
      wp.colSet("opt_redem_days", "Y");

    if (wp.colStr("fund_code").length() == 0)
      wp.colSet("opt_fund_code", "Y");

    if (wp.colStr("air_type").length() == 0)
      wp.colSet("opt_air_type", "Y");

    if (wp.colStr("cal_mile").length() == 0)
      wp.colSet("opt_cal_mile", "Y");

    if (wp.colStr("cash_value").length() == 0)
      wp.colSet("opt_cash_value", "Y");

    if (wp.colStr("supply_count").length() == 0)
      wp.colSet("opt_supply_count", "Y");

    if (wp.colStr("use_count").length() == 0)
      wp.colSet("opt_use_count", "Y");

    if (wp.colStr("net_count").length() == 0)
      wp.colSet("opt_net_count", "Y");

    if (wp.colStr("max_limit_count").length() == 0)
      wp.colSet("opt_max_limit_count", "Y");

    if (wp.colStr("use_limit_count").length() == 0)
      wp.colSet("opt_use_limit_count", "Y");

    if (wp.colStr("net_limit_count").length() == 0)
      wp.colSet("opt_net_limit_count", "Y");

    if (wp.colStr("web_sumcnt").length() == 0)
      wp.colSet("opt_web_sumcnt", "Y");

    if (wp.colStr("limit_last_date").length() == 0)
      wp.colSet("opt_limit_last_date", "Y");


    if (wp.colStr("vendor_no").length() == 0)
      wp.colSet("opt_vendor_no", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp01.Mktp0240Func func = new mktp01.Mktp0240Func(wp);

    String[] lsGiftNo = wp.itemBuff("gift_no");
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
      if (lsCrtUser[rr].equals(wp.loginUser)) {
        ilAuth++;
        wp.colSet(rr, "ok_flag", "F");
        continue;
      }

      func.varsSet("gift_no", lsGiftNo[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbInsertA4Mgec();
        if (rc == 1)
          rc = func.dbDeleteD4TMgec();
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4Mgec();
        if (rc == 1)
          rc = func.dbInsertA4Mgec();
        if (rc == 1)
          rc = func.dbDeleteD4TMgec();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4Mgec();
        if (rc == 1)
          rc = func.dbDeleteD4TMgec();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commGiftTypefo("comm_gift_typeno");
        commGiftType("comm_gift_type");
        commfuncAudType("aud_type");
        commCrtuser("comm_crt_user");
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

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + ilAuth);
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
      if ((wp.respHtml.equals("mktp0240"))) {
    	  
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
          
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_gift_typeno").length() > 0) {
          wp.optionKey = wp.colStr("ex_gift_typeno");
        }
        this.dddwList("dddw_gift_typefo", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='GIFT_TYPENO'");
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
  public void commGiftName(String name) throws Exception {
    commGiftName(name, 0);
    return;
  }

  // ************************************************************************
  public void commGiftName(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from mkt_gift " + " where 1 = 1 "
          + " and   gift_no = ? ";
      if (wp.colStr(ii, befStr + "gift_no").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "gift_no") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_gift_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commBonusType(String type) throws Exception {
    commBonusType(type, 0);
    return;
  }

  // ************************************************************************
  public void commBonusType(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_type = 'BONUS_NAME' " + " and   wf_id = ? ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "bonus_type") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commGiftTypefo(String type) throws Exception {
    commGiftTypefo(type, 0);
    return;
  }

  // ************************************************************************
  public void commGiftTypefo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_id =  ? "
          + " and   wf_type = 'GIFT_TYPENO' ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "gift_typeno") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
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
          + " and   wf_id = ? "
          + " and   wf_type = 'GIFT_MILE' ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "air_TYPE") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commVendorNo1(String no) throws Exception {
    commVendorNo1(no, 0);
    return;
  }

  // ************************************************************************
  public void commVendorNo1(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " vendor_name as column_vendor_name " + " from mkt_vendor "
          + " where 1 = 1 " + " and   vendor_no = ? ";
      if (wp.colStr(ii, befStr + "vendor_no").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "vendor_no") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_vendor_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDusableFlag(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"已停用", "未停用"};
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
  public void commGiftType(String cde1) throws Exception {
    String[] cde = {"gift_type", "1", "2", "3"};
    String[] txt = {"", "商品", "基金", "電子商品"};
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

  public String listMktGiftExchgdata(String table, String tableName, String giftNo, String string) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   gift_no = '" + giftNo + "'";
    wp.log("@@@@@@@@@@@@[" + sql1 + "]");
    sqlSelect(sql1);

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }

  // ************************************************************************
//************************************************************************
public String procDynamicDddwCrtuser1(String string)  throws Exception
{
  String lsSql = "";

  lsSql = " select "
         + " b.crt_user as db_code, "
         + " max(b.crt_user||' '||a.usr_cname) as db_desc "
         + " from sec_user a,mkt_gift_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
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
         + " and   usr_id = ? "
         ;
    if (wp.colStr(ii,befStr+"crt_user").length()==0) continue;
     sqlSelect(sql1, new Object[] { wp.colStr(ii,befStr+"crt_user") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_usr_cname"); 
    wp.colSet(ii, columnData1, columnData);
   }
return;
}

}  // End of class
