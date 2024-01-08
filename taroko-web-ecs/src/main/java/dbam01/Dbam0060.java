/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-03  V1.00.01  Justin   change ptr_sys_idtab into cca_opp_type_reason        *
******************************************************************************/
package dbam01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbam0060 extends BaseEdit {
  Dbam0060Func func;

  String rowid = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
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
    initButton();
  }

  @Override
  public void initPage() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("tt_lost_code", "毀損重置作業");
    } else {
      wp.colSet("exDesc", "毀損重置作業");
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("acct_type");
        dddwList("DbpAcctTypeList", "dbp_acct_type", "acct_type", "acct_type||' ['||chin_name||']'",
            "where 1=1 order by acct_type");

        // wp.optionKey = wp.col_ss("group_code");
        // dddw_list("getGroupCode","select group_code as db_code, group_code||'
        // ['||ptr_group_card.name||']' as db_desc from ptr_card_type, ptr_group_card " +
        // "where ptr_card_type.card_type = ptr_group_card.card_type and bin_no in (select bin_no
        // from ptr_bintable where debit_flag = 'Y') order by group_code");

        wp.optionKey = wp.colStr("group_code");
        dddwList("getGroupCode",
            "select dbc_card_type.group_code as db_code, dbc_card_type.group_code||' ['||ptr_group_card.name||']' as db_desc from dbc_card_type, ptr_group_card "
                + "where dbc_card_type.group_code = ptr_group_card.group_code GROUP BY dbc_card_type.group_code, ptr_group_card.name order by db_code ");
      } else {
        wp.optionKey = wp.itemStr("exActype");
        dddwList("DbpAcctTypeList", "dbp_acct_type", "acct_type", "acct_type||' ['||chin_name||']'",
            "where 1=1 order by acct_type");
      }
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {
    if (empty(wp.itemStr("exActype"))) {
      alertErr2("請輸入 帳戶類別");
      return false;
    }

    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("exActype")) == false) {
      wp.whereStr += " and acct_type = :acct_type ";
      setString("acct_type", wp.itemStr("exActype"));
    }
    wp.whereStr += " and lost_code = 'RT' ";

    wp.whereOrder = " order by acct_type, lost_code ";
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

    wp.selectSQL = "hex(rowid) as rowid, " + "acct_type, " + "lost_code, " + "group_code, "
        + "description, " + "normal_major, " + "normal_sub, " + "mod_seqno ";

    wp.daoTable = "dba_lostgrp";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String groupCode = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      groupCode = wp.colStr(ii, "group_code");
      wp.colSet(ii, "tt_group_code", wfgetGroupCode(groupCode));
    }
  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, " + "acct_type, " + "lost_code, " + "group_code, "
        + "description, " + "normal_major, " + "normal_sub, " + "mod_seqno ";

    wp.daoTable = "dba_lostgrp";

    wp.whereStr = "where 1=1" + " and hex(rowid) = :rowid ";
    setString("rowid", rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
    }
    detlWkdata();
  }

  void detlWkdata() throws Exception {
    // String ss=wp.col_ss("lost_code");
    // wp.col_set("tt_lost_code", wf_PtrSysIdtabDesc(ss));

    wp.colSet("tt_lost_code", "毀損重置作業");
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Dbam0060Func(wp);
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    if (ofValidation() < 0)
      return;

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  int ofValidation() throws Exception {
    long llNormalMajor = 0, llNormalSub = 0;

    if (strAction.equals("D"))
      return 1;

    if (empty(wp.itemStr("group_code"))) {
      alertErr("團體代號 不可空白");
      return -1;
    }

    llNormalMajor = (long) wp.itemNum("normal_major");
    llNormalSub = (long) wp.itemNum("normal_sub");

    if (llNormalMajor < 0 || llNormalSub < 0) {
      alertErr("數值需 >= 0");
      return -1;
    }
    return 1;
  }

  String wfPtrSysIdtabDesc(String oppStatus) throws Exception {
    String rtn = "";
    String lsSql =
        "select opp_remark from cca_opp_type_reason " + "where  opp_status = :oppStatus ";
    setString("oppStatus", oppStatus);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      rtn = sqlStr("opp_remark");

    return rtn;
  }

  String wfgetGroupCode(String idcode) throws Exception {
    String rtn = "";
    // String ls_sql = "select ptr_group_card.name as db_desc from ptr_card_type, ptr_group_card "
    // + "where ptr_card_type.card_type = ptr_group_card.card_type "
    // + "and bin_no in (select bin_no from ptr_bintable where debit_flag = 'Y') and group_code =
    // :group_code ";
    String ls_sql = "select ptr_group_card.name as db_desc from dbc_card_type, ptr_group_card "
        + "where dbc_card_type.group_code = ptr_group_card.group_code "
        + "and dbc_card_type.group_code = :group_code "
        + "group by dbc_card_type.group_code, ptr_group_card.name ";
    setString("group_code", idcode);
    sqlSelect(ls_sql);
    if (sqlRowNum > 0)
      rtn = sqlStr("db_desc");

    return rtn;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
