/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-26  V1.00.01  ryan       program initial                            *
* 109-04-23  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm0120 extends BaseEdit {
  Mktm0120Func func;
  int i = 0, ii_unit = 0;
  String kk1Batchno = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
    } else if (eqIgno(wp.buttonCode, "R1")) {
      // -資料讀取-
      strAction = "R1";
      // groupRead();
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

  }

  void getWhereStr() {

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_batchno")) == false) {
      wp.whereStr += " and  batch_no = :ex_batchno ";
      setString("ex_batchno", wp.itemStr("ex_batchno"));
    }
    if (empty(wp.itemStr("ex_crtdate1")) == false) {
      wp.whereStr += " and  file_date >= :ex_crtdate1 ";
      setString("ex_crtdate1", wp.itemStr("ex_crtdate1"));
    }
    if (empty(wp.itemStr("ex_crtdate2")) == false) {
      wp.whereStr += " and  file_date <= :ex_crtdate2 ";
      setString("ex_crtdate2", wp.itemStr("ex_crtdate2"));
    }
  }

  @Override
  public void queryFunc() throws Exception {

    String lsDate1 = wp.itemStr("ex_crtdate1");
    String lsDate2 = wp.itemStr("ex_crtdate2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hex(rowid) as rowid " + " ,batch_no " + " ,description " + " ,confirm_parm "
        + " ,file_date " + " ,employee_no " + " ,confirm_date ";

    wp.daoTable = " mkt_month_par ";
    wp.whereOrder = " order by file_date desc ";
    getWhereStr();
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // dddw_select();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    kk1Batchno = itemKk("data_k1");
    if (empty(kk1Batchno)) {
      kk1Batchno = wp.itemStr("kk_batchno");
    }
    wp.selectSQL = " hex(rowid) as rowid, mod_seqno " + " ,batch_no " + " ,description "
        + " ,confirm_parm " + " ,confirm_date " + " ,acct_type_1 " + " ,acct_type_2 "
        + " ,acct_type_3 " + " ,acct_type_4 " + " ,acct_type_5 " + " ,condition "
        + " ,new_not_purchase_month "
        + " ,decode(new_not_purchase_ym_ct_flag,'','E',new_not_purchase_ym_ct_flag) as new_not_purchase_ym_ct_flag "
        + " ,new_not_purchase_ym_ct "
        + " ,decode(new_not_purchase_ym_gc_flag,'','E',new_not_purchase_ym_gc_flag) as new_not_purchase_ym_gc_flag "
        + " ,new_not_purchase_ym_gc " + " ,old_not_bill_ym_s " + " ,old_not_bill_ym_e "
        + " ,decode(old_not_bill_ym_ct_flag,'','E',old_not_bill_ym_ct_flag) as old_not_bill_ym_ct_flag "
        + " ,old_not_bill_ym_ct "
        + " ,decode(old_not_bill_ym_gc_flag,'','E',old_not_bill_ym_gc_flag) as old_not_bill_ym_gc_flag "
        + " ,old_not_bill_ym_gc " + " ,not_bill_ym_s " + " ,not_bill_ym_e "
        + " ,decode(not_bill_ym_ct_flag,'','E',not_bill_ym_ct_flag) as not_bill_ym_ct_flag "
        + " ,not_bill_ym_ct "
        + " ,decode(not_bill_ym_gc_flag,'','E',not_bill_ym_gc_flag) as not_bill_ym_gc_flag "
        + " ,not_bill_ym_gc "
        + " ,decode(purch_ym_ct_flag,'','E',purch_ym_ct_flag) as purch_ym_ct_flag "
        + " ,purch_ym_ct "
        + " ,decode(purch_ym_gc_flag,'','E',purch_ym_gc_flag) as purch_ym_gc_flag "
        + " ,purch_ym_gc " + " ,exclude_foreigner_flag " + " ,exclude_staff_flag "
        + " ,exclude_mbullet_flag " + " ,exclude_call_sell_flag " + " ,exclude_sms_flag "
        + " ,exclude_dm_flag " + " ,exclude_e_news_flag " + " ,exclude_list_flag "
        + " ,exclude_list " + " ,acct_type_cnt1 " + " ,acct_type_cnt2 " + " ,acct_type_cnt3 "
        + " ,acct_type_cnt4 " + " ,acct_type_cnt5 " + " ,total_cnt " + " ,normal_card_cnt "
        + " ,no_purch_cnt " + " ,confirm_print " + " ,file_date " + " ,employee_no "
        + " ,commit_code " + " ,decode(all_card,'','1',all_card) as all_card " + " ,purch_month "
        + " ,purch_amt " + " ,process_code ";
    wp.daoTable = " MKT_MONTH_PAR ";
    wp.whereStr = " where 1=1 ";

    if (!empty(kk1Batchno)) {
      wp.whereStr += " and batch_no = :kk1_batchno ";
      setString("kk1_batchno", kk1Batchno);
    }
    pageSelect();
    listWkdata();
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Mktm0120Func(wp);
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    if (strAction.equals("A")) {
      String lsVal = (this.toInt(wp.sysDate) - 19110000) + "%";
      String sqlSelect =
          "select max(batch_no) as ls_val2 from mkt_month_par where batch_no like :ls_val ";
      setString("ls_val", lsVal);
      sqlSelect(sqlSelect);
      String lsVal2 = sqlStr("ls_val2");
      if (empty(lsVal2)) {
        lsVal2 = (this.toInt(wp.sysDate) - 19110000) + "001";
      } else {
        lsVal2 = (this.toInt(lsVal2) + 1) + "";
      }
      func.varsSet("batch_no", lsVal2);
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
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
  }

  @Override
  public void dddwSelect() {

    try {
      wp.optionKey = wp.colStr("acct_type_1");
      this.dddwList("dddw_acct_type_1", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

      wp.optionKey = wp.colStr("acct_type_2");
      this.dddwList("dddw_acct_type_2", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

      wp.optionKey = wp.colStr("acct_type_3");
      this.dddwList("dddw_acct_type_3", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

      wp.optionKey = wp.colStr("acct_type_4");
      this.dddwList("dddw_acct_type_4", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

      wp.optionKey = wp.colStr("acct_type_5");
      this.dddwList("dddw_acct_type_5", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

      wp.optionKey = "";
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 order by card_type");

      wp.optionKey = "";
      this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 order by group_code");

    } catch (Exception ex) {
    }
  }

  void listWkdata() {
    String lsCondition = wp.colStr("condition");
    if (lsCondition.length() == 3) {
      wp.colSet("condition1", lsCondition.substring(0, 1));
      wp.colSet("condition2", lsCondition.substring(1, 2));
      wp.colSet("condition3", lsCondition.substring(2, 3));
    }
    wp.colSet("new_not_purchase_ym_ct_desc",
        addCommaByEachTwoBytes(wp.colStr("new_not_purchase_ym_ct")));
    wp.colSet("old_not_bill_ym_ct_desc", addCommaByEachTwoBytes(wp.colStr("old_not_bill_ym_ct")));
    wp.colSet("not_bill_ym_ct_desc", addCommaByEachTwoBytes(wp.colStr("not_bill_ym_ct")));
    wp.colSet("purch_ym_ct_desc", addCommaByEachTwoBytes(wp.colStr("purch_ym_ct")));
    wp.colSet("new_not_purchase_ym_gc_desc",
        addCommaByEachTwoBytes2(wp.colStr("new_not_purchase_ym_gc")));
    wp.colSet("old_not_bill_ym_gc_desc", addCommaByEachTwoBytes2(wp.colStr("old_not_bill_ym_gc")));
    wp.colSet("not_bill_ym_gc_desc", addCommaByEachTwoBytes2(wp.colStr("not_bill_ym_gc")));
    wp.colSet("purch_ym_gc_desc", addCommaByEachTwoBytes2(wp.colStr("purch_ym_gc")));
    wp.colSet("exclude_list_desc", addCommaByEachTwoBytes3(wp.colStr("exclude_list")));
  }

  private String addCommaByEachTwoBytes(String data) {
    String buf = "";
    int cnt = 0;
    for (int i = 0; i < data.length(); i++) {
      if (i % 2 == 0 && i != 0) {
        buf = buf + ",";
        cnt++;
      }
      if (i == 0)
        cnt++;
      buf = buf + data.substring(i, i + 1);
    }

    return buf;
  }

  private String addCommaByEachTwoBytes2(String data) {
    String buf = "";
    int cnt = 0;
    for (int i = 0; i < data.length(); i++) {
      if (i % 4 == 0 && i != 0) {
        buf = buf + ",";
        cnt++;
      }
      if (i == 0)
        cnt++;
      buf = buf + data.substring(i, i + 1);
    }

    return buf;
  }

  private String addCommaByEachTwoBytes3(String data) {
    String buf = "";
    int cnt = 0;
    for (int i = 0; i < data.length(); i++) {
      if (i % 10 == 0 && i != 0) {
        buf = buf + ",";
        cnt++;
      }
      if (i == 0)
        cnt++;
      buf = buf + data.substring(i, i + 1);
    }

    return buf;
  }

  // void groupRead(){
  // StringBuilder group_select = new StringBuilder();
  // group_select.append("group_code_");
  // group_select.append(item_kk("data_k4"));
  //
  // StringBuilder group_chin = new StringBuilder();
  // group_chin.append("group_chin_");
  // group_chin.append(item_kk("data_k4"));
  //
  // String ls_sql="";
  // ls_sql = "select group_name from ptr_group_code where group_code =:ex_group_code ";
  // setString("ex_group_code",group_select.toString());
  // sqlSelect(ls_sql);
  // if(sql_nrow > 0){
  // wp.col_set(group_chin.toString(), sql_ss("group_name"));
  // }else{
  // wp.col_set(group_chin.toString(), "");
  // }
  // }
}
