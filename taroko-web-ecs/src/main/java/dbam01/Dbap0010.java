/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  yanghan  修改了變量名稱和方法名稱*
* 109-01-04  V1.00.02   shiyuqi       修改无意义命名  
* 110-01-06  V1.00.03  Justin       updated for XSS
* 110-01-29  V1.00.04  Justin       fix a bug in insertion page
******************************************************************************/
package dbam01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbap0010 extends BaseEdit {
  Dbap0010Func func;

  String actype = "";
  String acctType = "";
  String seqno = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    actype = wp.itemStr("acct_type");

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton(); // 增加 actype 的控制 //modfied by phopho
  }

  @Override
  public void initPage() {
    wp.colSet("pho_insert_disable", "disabled");
    wp.colSet("pho_update_disable", "disabled");
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("group_code");
        dddwList("PtrGroupCodeList", "ptr_group_code", "group_code",
            "group_code||' ['||group_name||']'", "where 1=1 order by group_code");

        wp.optionKey = wp.colStr("card_type");
        dddwList("PtrCardTypeList", "ptr_card_type", "card_type", "card_type||' ['||name||']'",
            "where 1=1 order by card_type");
      } else {
        wp.initOption = "";
        wp.optionKey = wp.itemStr("exAcctType");
        dddwList("DbpAcctTypeList", "dbp_acct_type", "acct_type", "acct_type||' ['||chin_name||']'",
            "where 1=1 order by acct_type");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("curr_code");
        dddwList("PtrCurrcodeList", "ptr_currcode", "curr_code",
            "curr_code||' ['||curr_chi_name||']'", "where 1=1 order by curr_code");

        wp.optionKey = wp.colStr("stmt_cycle");
        dddwList("PtrWorkdayCycleList", "ptr_workday", "stmt_cycle", "stmt_cycle",
            "where 1=1 order by stmt_cycle");
      }
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {

    wp.whereStr = "where 1=1 ";
    wp.whereStr += " and acct_type = :acct_type ";
    setString("acct_type", wp.itemStr("exAcctType"));

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "acct_type, " + "chin_name, " + "curr_code, " + "card_indicator, "
        + "f_currency_flag, " + "u_cycle_flag, " + "stmt_cycle, " + "card_type, " + "group_code, "
        + "reg_bank_no, " + "atm_code, " + "inst_crdtamt, " + "inst_crdtrate, " + "mod_user, "
        + "mod_time, " + "mod_pgm, " + "mod_seqno ";

    wp.daoTable = "dbp_acct_type";

    pageQuery();

    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.colSet("pho_insert_disable", "");
    wp.colSet("pho_update_disable", "");

    dddwSelect();
    queryReadDetl();
    // wp.setPageValue();
  }

  void queryReadDetl() throws Exception {
    this.selectNoLimit();

    daoTid = "A-";
    wp.selectSQL = "acct_type" + ", reg_bank_no" + ", group_code" + ", card_type" + ", mod_user"
        + ", mod_time" + ", mod_pgm" + ", mod_seqno" + ", seqno ";
    wp.daoTable = "dbp_prod_type";
    wp.whereStr = "where acct_type = :acct_type ";
    setString("acct_type", wp.itemStr("exAcctType"));
    wp.whereOrder = "order by seqno ";

    pageQuery();
    wp.setListCount(1);
    listWkdata();
  }

  void listWkdata() throws Exception {
    String param = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "a-group_code");
      wp.colSet(ii, "a-tt_group_code", wfgetGroupCode(param));

      param = wp.colStr(ii, "a-card_type");
      wp.colSet(ii, "a-tt_card_type", wfGetCardType(param));
    }
  }

  String wfGetAcctName(String idcode) throws Exception {
    String rtn = "";
    String lssql = "select chin_name from dbp_acct_type " + "where acct_type = :acct_type ";
    setString("acct_type", idcode);
    sqlSelect(lssql);
    if (sqlRowNum > 0)
      rtn = sqlStr("chin_name");

    return rtn;
  }

  String wfgetGroupCode(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select group_name from ptr_group_code " + "where group_code = :group_code ";
    setString("group_code", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      rtn = sqlStr("group_name");

    return rtn;
  }

  String wfGetCardType(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select name from ptr_card_type " + "where card_type = :card_type ";
    setString("card_type", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      rtn = sqlStr("name");

    return rtn;
  }

  @Override
  public void querySelect() throws Exception {
    acctType = wp.itemStr("data_k1");
    seqno = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, " + "acct_type, " + "seqno, " + "reg_bank_no, "
        + "group_code, " + "card_type, " + "crt_user, " + "crt_date, " + "apr_user, " + "apr_date, "
        + "mod_user, " + "mod_seqno ";

    wp.daoTable = "dbp_prod_type";
    wp.whereStr = "where 1=1 " + "and acct_type = :acct_type " + "and seqno = :seqno ";
    setString("acct_type", acctType);
    setString("seqno", seqno);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctType + " : " + seqno);
    }

    String actype = wp.itemStr("acct_type");
    wp.colSet("acct_name", wfGetAcctName(actype));
    dddwSelect();
  }

  public void dataProcess() throws Exception {
    func = new Dbap0010Func(wp);

    // Detail Control
    int rowcntaa = 0;
    String[] acctTypeArray = wp.itemBuff("a-acct_type");
    if (!(acctTypeArray == null) && !empty(acctTypeArray[0]))
      rowcntaa = acctTypeArray.length;
    wp.listCount[0] = rowcntaa;

    if (ofValidationMain() < 0)
      return;

    if (func.updateDbpAcctType() < 0) {
      alertErr("修改 帳戶類別資料 失敗");
      sqlCommit(0);
      return;
    }
    sqlCommit(1);
    wp.alertMesg = "<script language='javascript'> alert('修改 帳戶類別資料 成功 。')</script>";

    modSeqnoAdd();
    dataProcessDetail();
  }

  public void dataProcessDetail() throws Exception {
    String[] ddAcctType = wp.itemBuff("a-acct_type");
    String[] seqno = wp.itemBuff("a-seqno");
    String[] opt = wp.itemBuff("opt");

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0)
        continue;

      func.varsSet("acct_type", ddAcctType[rr]);
      func.varsSet("seqno", seqno[rr]);

      rc = func.dbDelete_detl();
      sqlCommit(rc);
    }

    queryReadDetl();
  }


  int ofValidationMain() throws Exception {
    String lsCard, lsCycle, lsStmt;
    double ldRate = 0;

    lsCard = wp.itemStr("card_indicator");
    if (eqIgno(lsCard, "2")) {
      alertErr("卡種為商務卡時，不可更改");
      return -1;
    }

    // --帳戶對帳單週期--
    lsCycle = wp.itemStr("u_cycle_flag");
    lsStmt = wp.itemStr("stmt_cycle");
    if (eqIgno(lsCycle, "Y")) {
      if (empty(lsStmt)) {
        alertErr("帳戶對帳單週期 不可空白");
        return -1;
      }
    } else {
      if (!empty(lsStmt)) {
        alertErr("不可指定 帳戶對帳單週期");
        return -1;
      }
    }

    // --分期付款--
    ldRate = wp.itemNum("inst_crdtrate");
    if (ldRate > 100) {
      alertErr("信用額度比率不可大於 100");
      return -1;
    }

    if (wfApr() != 1)
      return -1;

    return 1;
  }

  int ofValidationGroup() throws Exception {
    String lsAcctType, seqno;
    String lsGroupCode, lscardType;
    int ll_cnt = 0;

    if (eqIgno(strAction, "D")) {
      if (wfApr() != 1)
        return -1;
      return 1;
    }

    lsAcctType = wp.itemStr("acct_type");
    seqno = wp.itemStr("seqno");
    lsGroupCode = wp.itemStr("group_code");
    lscardType = wp.itemStr("card_type");
    // --all-value--
    if (!empty(lsGroupCode) && !empty(lscardType)) {
      alertErr("團體代號及卡種 不可同時輸入");
      return -1;
    }

    // --all-empty--
    if (empty(lsGroupCode) && empty(lscardType)) {
      alertErr("團體代號及卡種 不可同時為空白");
      return -1;
    }
    // --check duplication-----
    // --dupl-group_code--
    if (!empty(lsGroupCode)) {
      String lsSql = "select count(*) ll_cnt from dbp_prod_type " + "where acct_type = :acct_type "
          + "  and group_code = :group_code ";
      setString("acct_type", lsAcctType);
      setString("group_code", lsGroupCode);
      if (strAction.equals("U")) { // 修改時排除自己
        lsSql += " and seqno <> :seqno ";
        setString("seqno", seqno);
      }
      sqlSelect(lsSql);
      ll_cnt = (int) sqlNum("ll_cnt");
      if (ll_cnt > 0) {
        alertErr("團體代號不可重複");
        return -1;
      }
    }

    // --dupl-card_type--
    if (!empty(lscardType)) {
      String lsSql = "select count(*) ll_cnt from dbp_prod_type " + "where acct_type = :acct_type "
          + "  and card_type = :card_type ";
      setString("acct_type", lsAcctType);
      setString("card_type", lscardType);
      if (strAction.equals("U")) { // 修改時排除自己
        lsSql += " and seqno <> :seqno ";
        setString("seqno", seqno);
      }
      sqlSelect(lsSql);
      ll_cnt = (int) sqlNum("ll_cnt");
      if (ll_cnt > 0) {
        alertErr("卡種不可重複");
        return -1;
      }
    }

    if (wfApr() != 1)
      return -1;

    return 1;
  }

  // 說明:執行線上主管覆核。
  int wfApr() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return -1;
    }
    // 問題單 0000426: 2. 檢查線上主管審核，若通過，是否寫入apr_date、apr_user、apr_flag(若有此欄位)等欄位值。
    func.varsSet("apr_user", wp.itemStr("approval_user"));
    func.varsSet("apr_date", wp.sysDate);
    func.varsSet("apr_flag", "Y");
    return 1;
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Dbap0010Func(wp);

    if (ofValidationGroup() < 0)
      return;

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
    this.btnModeAud("XX");
    try {
      if (eqIgno(strAction, "new") || eqIgno(strAction, "A") || eqIgno(strAction, "D")) {
        wp.colSet("acct_type", actype);
        wp.colSet("acct_name", wfGetAcctName(actype));
      }
		if (wp.colEmpty("seqno")) {
			this.btnUpdateOn(false);
			this.btnAddOn(true);
		}
    } catch (Exception ex) {
    }
  }

}
