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

import java.util.Arrays;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Mktm0120Func extends FuncEdit {

  String lsCondition = "";

  public Mktm0120Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TOD11111
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    String[] ary = new String[5];
    lsCondition += empty(wp.itemStr("condition1")) ? "N" : "Y";
    lsCondition += empty(wp.itemStr("condition2")) ? "N" : "Y";
    lsCondition += empty(wp.itemStr("condition3")) ? "N" : "Y";
    ary[0] = wp.itemStr("acct_type_1");
    if (empty(ary[0])) {
      ary[0] = "acct_type_1";
    }
    ary[1] = wp.itemStr("acct_type_2");
    if (empty(ary[1])) {
      ary[1] = "acct_type_2";
    }
    ary[2] = wp.itemStr("acct_type_3");
    if (empty(ary[2])) {
      ary[2] = "acct_type_3";
    }
    ary[3] = wp.itemStr("acct_type_4");
    if (empty(ary[3])) {
      ary[3] = "acct_type_4";
    }
    ary[4] = wp.itemStr("acct_type_5");
    if (empty(ary[4])) {
      ary[4] = "acct_type_5";
    }

    if (dropComma(wp.itemStr("new_not_purchase_ym_ct")).length() > 200) {
      errmsg("卡種資料過長，無法新增");
      wp.colSet("errmsg1", "卡種資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("old_not_bill_ym_ct")).length() > 200) {
      errmsg("卡種資料過長，無法新增");
      wp.colSet("errmsg2", "卡種資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("not_bill_ym_ct")).length() > 200) {
      errmsg("卡種資料過長，無法新增");
      wp.colSet("errmsg3", "卡種資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("new_not_purchase_ym_gc")).length() > 200) {
      errmsg("團體代號資料過長，無法新增");
      wp.colSet("errmsg4", "團體代號資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("old_not_bill_ym_gc")).length() > 200) {
      errmsg("團體代號資料過長，無法新增");
      wp.colSet("errmsg5", "團體代號資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("not_bill_ym_gc")).length() > 200) {
      errmsg("團體代號資料過長，無法新增");
      wp.colSet("errmsg6", "團體代號資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("exclude_list_desc")).length() > 200) {
      errmsg("排除名單資料過長，無法新增");
      wp.colSet("errmsg7", "排除名單資料過長，無法新增");
      return;
    }
    if (dropComma(wp.itemStr("purch_ym_ct")).length() > 200) {
      errmsg("卡種資料過長，無法新增");
      wp.colSet("errmsg8", "卡種資料過長，無法新增");
      return;
    }
    if (!this.isDelete()) {
      for (int i = 0; i < ary.length; i++) {
        if (i != Arrays.asList(ary).indexOf(ary[i])) {
          errmsg("帳戶類別,資料不可重複");
          return;
        }
      }
      if (wp.itemStr("all_card").equals("2")) {
        if (wp.itemNum("purch_month") <= 0) {
          errmsg("消費金額，需大於0個月");
          return;
        }
      }
      if (wp.itemStr("condition1").equals("Y")) {
        if (wp.itemNum("new_not_purchase_month") <= 0) {
          errmsg("需大於0個月未消費 ");
          return;
        }
      }
      /*
       * if(wp.item_ss("confirm_parm").equals("Y")){ errmsg("資料主管已覆核, 不可修改"); return; }
       */
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from mkt_month_par where batch_no = ? ";
      Object[] param = new Object[] {varsStr("batch_no")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增,請從新查詢");
        return;
      }

    } else {
      // -other modify-
      sqlWhere = " where 1=1 and batch_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {wp.itemStr("batch_no"), wp.modSeqno()};
      if (this.isOtherModify("mkt_month_par", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("mkt_month_par");
    sp.ppstr("batch_no", varsStr("batch_no"));
    sp.ppstr("confirm_parm", "Y");
    sp.ppstr("confirm_date", wp.sysDate);
    sp.ppstr("description", wp.itemStr("description"));
    sp.ppstr("acct_type_1", wp.itemStr("acct_type_1"));
    sp.ppstr("acct_type_2", wp.itemStr("acct_type_2"));
    sp.ppstr("acct_type_3", wp.itemStr("acct_type_3"));
    sp.ppstr("acct_type_4", wp.itemStr("acct_type_4"));
    sp.ppstr("acct_type_5", wp.itemStr("acct_type_5"));
    sp.ppstr("condition", lsCondition);
    sp.ppnum("new_not_purchase_month", wp.itemNum("new_not_purchase_month"));
    sp.ppstr("new_not_purchase_ym_ct_flag", wp.itemStr("new_not_purchase_ym_ct_flag"));
    sp.ppstr("new_not_purchase_ym_ct", dropComma(wp.itemStr("new_not_purchase_ym_ct")));
    sp.ppstr("new_not_purchase_ym_gc_flag", wp.itemStr("new_not_purchase_ym_gc_flag"));
    sp.ppstr("new_not_purchase_ym_gc", dropComma(wp.itemStr("new_not_purchase_ym_gc")));
    sp.ppstr("old_not_bill_ym_s", wp.itemStr("old_not_bill_ym_s"));
    sp.ppstr("old_not_bill_ym_e", wp.itemStr("old_not_bill_ym_e"));
    sp.ppstr("old_not_bill_ym_ct_flag", wp.itemStr("old_not_bill_ym_ct_flag"));
    sp.ppstr("old_not_bill_ym_ct", dropComma(wp.itemStr("old_not_bill_ym_ct")));
    sp.ppstr("old_not_bill_ym_gc_flag", wp.itemStr("old_not_bill_ym_gc_flag"));
    sp.ppstr("old_not_bill_ym_gc", dropComma(wp.itemStr("old_not_bill_ym_gc")));
    sp.ppstr("not_bill_ym_s", wp.itemStr("not_bill_ym_s"));
    sp.ppstr("not_bill_ym_e", wp.itemStr("not_bill_ym_e"));
    sp.ppstr("not_bill_ym_ct_flag", wp.itemStr("not_bill_ym_ct_flag"));
    sp.ppstr("not_bill_ym_ct", dropComma(wp.itemStr("not_bill_ym_ct")));
    sp.ppstr("not_bill_ym_gc_flag", wp.itemStr("not_bill_ym_gc_flag"));
    sp.ppstr("not_bill_ym_gc", dropComma(wp.itemStr("not_bill_ym_gc")));
    sp.ppstr("purch_ym_ct_flag", wp.itemStr("purch_ym_ct_flag"));
    sp.ppstr("purch_ym_ct", dropComma(wp.itemStr("purch_ym_ct")));
    sp.ppstr("purch_ym_gc_flag", wp.itemStr("purch_ym_gc_flag"));
    sp.ppstr("purch_ym_gc", dropComma(wp.itemStr("purch_ym_gc")));
    sp.ppstr("exclude_foreigner_flag", empty(wp.itemStr("exclude_foreigner_flag")) ? "N" : "Y");
    sp.ppstr("exclude_staff_flag", empty(wp.itemStr("exclude_staff_flag")) ? "N" : "Y");
    sp.ppstr("exclude_mbullet_flag", empty(wp.itemStr("exclude_mbullet_flag")) ? "N" : "Y");
    sp.ppstr("exclude_call_sell_flag", empty(wp.itemStr("exclude_call_sell_flag")) ? "N" : "Y");
    sp.ppstr("exclude_sms_flag", empty(wp.itemStr("exclude_sms_flag")) ? "N" : "Y");
    sp.ppstr("exclude_dm_flag", empty(wp.itemStr("exclude_dm_flag")) ? "N" : "Y");
    sp.ppstr("exclude_e_news_flag", empty(wp.itemStr("exclude_e_news_flag")) ? "N" : "Y");
    sp.ppstr("exclude_list_flag", empty(wp.itemStr("exclude_list_flag")) ? "N" : "Y");
    sp.ppstr("exclude_list", dropComma(wp.itemStr("exclude_list")));
    sp.ppstr("all_card", wp.itemStr("all_card"));
    sp.ppnum("purch_month", empty(wp.itemStr("purch_month")) ? 0 : wp.itemNum("purch_month"));
    sp.ppnum("purch_amt", empty(wp.itemStr("purch_amt")) ? 0 : wp.itemNum("purch_amt"));
    sp.ppstr("file_date", wp.sysDate);
    sp.ppstr("employee_no", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum < 0) {
      errmsg(getMsg());
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("mkt_month_par");
    sp.ppstr("batch_no", wp.itemStr("batch_no"));
    sp.ppstr("confirm_parm", "Y");
    sp.ppstr("confirm_date", wp.sysDate);
    sp.ppstr("description", wp.itemStr("description"));
    sp.ppstr("acct_type_1", wp.itemStr("acct_type_1"));
    sp.ppstr("acct_type_2", wp.itemStr("acct_type_2"));
    sp.ppstr("acct_type_3", wp.itemStr("acct_type_3"));
    sp.ppstr("acct_type_4", wp.itemStr("acct_type_4"));
    sp.ppstr("acct_type_5", wp.itemStr("acct_type_5"));
    sp.ppstr("condition", lsCondition);
    sp.ppnum("new_not_purchase_month", wp.itemNum("new_not_purchase_month"));
    sp.ppstr("new_not_purchase_ym_ct_flag", wp.itemStr("new_not_purchase_ym_ct_flag"));
    sp.ppstr("new_not_purchase_ym_ct", dropComma(wp.itemStr("new_not_purchase_ym_ct")));
    sp.ppstr("new_not_purchase_ym_gc_flag", wp.itemStr("new_not_purchase_ym_gc_flag"));
    sp.ppstr("new_not_purchase_ym_gc", dropComma(wp.itemStr("new_not_purchase_ym_gc")));
    sp.ppstr("old_not_bill_ym_s", wp.itemStr("old_not_bill_ym_s"));
    sp.ppstr("old_not_bill_ym_e", wp.itemStr("old_not_bill_ym_e"));
    sp.ppstr("old_not_bill_ym_ct_flag", wp.itemStr("old_not_bill_ym_ct_flag"));
    sp.ppstr("old_not_bill_ym_ct", dropComma(wp.itemStr("old_not_bill_ym_ct")));
    sp.ppstr("old_not_bill_ym_gc_flag", wp.itemStr("old_not_bill_ym_gc_flag"));
    sp.ppstr("old_not_bill_ym_gc", dropComma(wp.itemStr("old_not_bill_ym_gc")));
    sp.ppstr("not_bill_ym_s", wp.itemStr("not_bill_ym_s"));
    sp.ppstr("not_bill_ym_e", wp.itemStr("not_bill_ym_e"));
    sp.ppstr("not_bill_ym_ct_flag", wp.itemStr("not_bill_ym_ct_flag"));
    sp.ppstr("not_bill_ym_ct", dropComma(wp.itemStr("not_bill_ym_ct")));
    sp.ppstr("not_bill_ym_gc_flag", wp.itemStr("not_bill_ym_gc_flag"));
    sp.ppstr("not_bill_ym_gc", dropComma(wp.itemStr("not_bill_ym_gc")));
    sp.ppstr("purch_ym_ct_flag", wp.itemStr("purch_ym_ct_flag"));
    sp.ppstr("purch_ym_ct", dropComma(wp.itemStr("purch_ym_ct")));
    sp.ppstr("purch_ym_gc_flag", wp.itemStr("purch_ym_gc_flag"));
    sp.ppstr("purch_ym_gc", dropComma(wp.itemStr("purch_ym_gc")));
    sp.ppstr("exclude_foreigner_flag", empty(wp.itemStr("exclude_foreigner_flag")) ? "N" : "Y");
    sp.ppstr("exclude_staff_flag", empty(wp.itemStr("exclude_staff_flag")) ? "N" : "Y");
    sp.ppstr("exclude_mbullet_flag", empty(wp.itemStr("exclude_mbullet_flag")) ? "N" : "Y");
    sp.ppstr("exclude_call_sell_flag", empty(wp.itemStr("exclude_call_sell_flag")) ? "N" : "Y");
    sp.ppstr("exclude_sms_flag", empty(wp.itemStr("exclude_sms_flag")) ? "N" : "Y");
    sp.ppstr("exclude_dm_flag", empty(wp.itemStr("exclude_dm_flag")) ? "N" : "Y");
    sp.ppstr("exclude_e_news_flag", empty(wp.itemStr("exclude_e_news_flag")) ? "N" : "Y");
    sp.ppstr("exclude_list_flag", empty(wp.itemStr("exclude_list_flag")) ? "N" : "Y");
    sp.ppstr("exclude_list", dropComma(wp.itemStr("exclude_list")));
    sp.ppstr("all_card", wp.itemStr("all_card"));
    sp.ppnum("purch_month", empty(wp.itemStr("purch_month")) ? 0 : wp.itemNum("purch_month"));
    sp.ppnum("purch_amt", empty(wp.itemStr("purch_amt")) ? 0 : wp.itemNum("purch_amt"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where batch_no=?", wp.itemStr("batch_no"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum < 0) {
      errmsg(getMsg());
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "delete mkt_month_par " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("batch_no"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum < 0) {
      errmsg(getMsg());
    }
    return rc;
  }

  private String dropComma(String data) {
    String buf = "";
    data = data.replaceAll("\n|\r", "");
    String[] datas = data.split(",");
    for (String dat : datas) {
      buf = buf + dat;
    }
    return buf;
  }
}
