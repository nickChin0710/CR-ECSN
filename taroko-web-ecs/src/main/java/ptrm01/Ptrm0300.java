/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-26  V1.00.00  David FU   program initial                            *
* 108-12-06  V1.00.01  Andy Liu   De Bug                                     *
* 108-12-13  V1.00.01  Andy       update                                     *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-07-31  V1.00.02  yanghan       修改页面覆核栏位     *
******************************************************************************/

package ptrm01;

import java.util.Arrays;


import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0300 extends BaseEdit {
  String mExAcctType = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (strAction) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        deleteFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
    }

    dddwSelect();
    initButton();

  }

  void showScreenDetl() {
    // -set new-
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  // for query use only
  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_acct_type")) == false) {
      wp.whereStr += " and  acct_type = :acct_type ";
      setString("acct_type", wp.itemStr("ex_acct_type"));
    }
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " acct_type" + ", chin_name" + ", curr_code" + ", card_indicator"
        + ", f_currency_flag" + ", bonus_flag" + ", rc_use_flag" + ", car_service_flag"
        + ", inssurance_flag" + ", u_cycle_flag" + ", stmt_cycle" + ", atm_code" + ", inst_crdtamt"
        + ", inst_crdtrate" + ", unon_flag" + ", cashadv_loc_rate" + ", cashadv_loc_maxamt"
        + ", cashadv_loc_rate_old" + ", breach_num_month";

    wp.daoTable = "ptr_acct_type";
    wp.whereOrder = " order by acct_type";

    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExAcctType = wp.itemStr("acct_type");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExAcctType = wp.itemStr("kk_acct_type");
    if (empty(mExAcctType)) {
      mExAcctType = itemKk("data_k1");
    }
    if (empty(mExAcctType)) {
      mExAcctType = wp.itemStr("acct_type");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", acct_type " + ", chin_name"
        + ", curr_code" + ", card_indicator" + ", f_currency_flag" + ", bonus_flag"
        + ", rc_use_flag" + ", decode(rc_use_flag,'1','Y','') db_rc_use_flag " // 20191206 1885
                                                                               // SA1521提出
        + ", car_service_flag" + ", inssurance_flag" + ", u_cycle_flag" + ", stmt_cycle"
        + ", atm_code" + ", inst_crdtamt" + ", inst_crdtrate" + ", unon_flag" + ", cashadv_loc_rate"
        + ", cashadv_loc_maxamt" + ", cashadv_loc_rate_old" + ", breach_num_month"
        + ", no_collection_flag";
    wp.daoTable = "ptr_acct_type";
    wp.whereStr = "where 1=1 and acct_type = :acct_type ";
    setString("acct_type", mExAcctType);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, acct_type=" + mExAcctType);
    }

    dataReadDetl();
  }

  // read relation table
  void dataReadDetl() throws Exception {
    this.selectNoLimit();
    wp.daoTable = "ptr_prod_type";
    wp.selectSQL = "hex(rowid) as b_rowid" + ", group_code" + ", card_type" + ", seqno";
    wp.whereStr = "WHERE 1=1 and acct_type = :acct_type order by seqno";
    setString("acct_type", mExAcctType);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    int llOk = 0, llErr = 0;

    Ptrm0300Func func = new Ptrm0300Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (rc != 1) {
      return;
    }


    if (strAction.equals("D") == false) {
      String[] aaId = wp.itemBuff("group_code");
      String[] aaDesc = wp.itemBuff("card_type");
      String[] aaOpt = wp.itemBuff("opt");

      wp.listCount[0] = aaId.length;
      wp.colSet("IND_NUM", "" + aaId.length);


      // delete



      // -check duplication-
      for (int ll = 0; ll < aaId.length; ll++) {
        wp.colSet(ll, "ok_flag", "");

        // aa_id[ll]與aa_desc[ll]不可同時為空字串
        if (empty(aaId[ll]) && empty(aaDesc[ll])) {
          wp.colSet(ll, "ok_flag", "!");
          continue;
        }

        // -option-ON-
        if (checkBoxOptOn(ll, aaOpt)) {
          continue;
        }


      }

      // -delete no-approve-
      if (func.dbDelete2() < 0) {
        alertErr(func.getMsg());
        sqlCommit(0); // rollback
        return;
      }

      // -insert-
      for (int ll = 0; ll < aaId.length; ll++) {
        // aa_id[ll]與aa_desc[ll]不可同時為空字串
        if (empty(aaId[ll]) && empty(aaDesc[ll]))
          continue;

        if (!empty(aaId[ll]) & !empty(aaDesc[ll])) {
          wp.colSet(ll, "ok_flag", "!");
          wp.colSet(ll, "wk_err", "團體代號及卡種 不可同時輸入");
          llErr++;
          continue;
        }

        if (!empty(aaId[ll]) && ll != Arrays.asList(aaId).indexOf(aaId[ll])) {
          wp.colSet(ll, "ok_flag", "!");
          wp.colSet(ll, "wk_err", "團體代號不可重複");
          continue;
        }
        if (!empty(aaDesc[ll]) && ll != Arrays.asList(aaDesc).indexOf(aaDesc[ll])) {
          wp.colSet(ll, "ok_flag", "!");
          wp.colSet(ll, "wk_err", "卡種不可重複");
          continue;
        }

        // -option-ON-
        if (checkBoxOptOn(ll, aaOpt)) {
          continue;
        }

        func.varsSet("group_code", aaId[ll]);
        func.varsSet("card_type", aaDesc[ll]);
        func.varsSet("seqno", String.valueOf(ll));

        if (func.dbInsert2() == 1) {
          llOk++;
        } else {
          llErr++;
        }
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }

    if (wp.itemNum("inst_crdtrate") > 100) {
      // errmsg("信用額度比率不可大於 100");
      alertErr("信用額度比率不可大於 100");
      return;
    }

    if (wp.itemStr("card_indicator").equals("2")) {
      if (!wp.itemStr("unon_flag").equals("N")) {
        // errmsg("為商務卡型時，共用分期付款註記必須為 N");
        alertErr("為商務卡型時，共用分期付款註記必須為 N");
        return;
      }
    }

    if (wp.itemStr("u_cycle_flag").equals("Y")) {
      if (empty(wp.itemStr("stmt_cycle"))) {
        alertErr("帳戶對帳單週期 不可空白");
      }
    }

    alertMsg("資料存檔處理完成");
    if (strAction.equals("D") == false) {
      // dataRead();
    }
  }

  @Override
  public void initButton() {

    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
      showScreenDetl();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("curr_code");
      this.dddwList("dddw_curr_code", "ptr_currcode", "curr_code", "curr_chi_name", "where 1=1");

      wp.optionKey = wp.itemStr("stmt_cycle");
      this.dddwList("dddw_stmt_cycle", "ptr_workday", "stmt_cycle", "",
          "where 1=1 order by stmt_cycle");

      wp.optionKey = "";
      this.dddwList("dddw_sel_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 and not exists (select group_code from PTR_PROD_TYPE where PTR_PROD_TYPE.GROUP_CODE=PTR_GROUP_CODE.GROUP_CODE)");

      wp.optionKey = "";
      this.dddwList("dddw_sel_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 and not exists (select card_type from PTR_PROD_TYPE where PTR_PROD_TYPE.CARD_TYPE=PTR_CARD_TYPE.CARD_TYPE)");
    } catch (Exception ex) {
    }
  }

}
