/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23   V1.00.01  Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0080Func extends FuncEdit {
  String stmtCycle = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      stmtCycle = wp.itemStr("kk_stmt_cycle");
    } else {
      stmtCycle = wp.itemStr("stmt_cycle");
    }

    if (empty(stmtCycle)) {
      errmsg("對帳單期別：不可空白");
      return;
    }
    if (wp.itemEmpty("issue_s_day")) {
      errmsg("發卡起迄日：不可空白");
      return;
    }
    if (wp.itemEmpty("issue_e_day")) {
      errmsg("發卡起迄日：不可空白");
      return;
    }
    if (wp.itemEmpty("close_stand")) {
      errmsg("關帳標準日：不可空白");
      return;
    }
    if (wp.itemEmpty("interest_stand")) {
      errmsg("利息起算標準日：不可空白");
      return;
    }
    if (wp.itemEmpty("lastpay_stand")) {
      errmsg("繳款截止標準日：不可空白");
      return;
    }
    if (wp.itemEmpty("this_acct_month")) {
      errmsg("帳務月份-本次：不可空白");
      return;
    }
    if (wp.itemEmpty("last_acct_month")) {
      errmsg("帳務月份-上次：不可空白");
      return;
    }
    if (wp.itemEmpty("next_acct_month")) {
      errmsg("帳務月份-下次：不可空白");
      return;
    }
    if (wp.itemEmpty("ll_acct_month")) {
      errmsg("帳務月份-上上次：不可空白");
      return;
    }
    if (wp.itemEmpty("this_close_date")) {
      errmsg("關帳日期-本次：不可空白");
      return;
    }
    if (wp.itemEmpty("last_close_date")) {
      errmsg("關帳日期-上次：不可空白");
      return;
    }
    if (wp.itemEmpty("next_close_date")) {
      errmsg("關帳日期-下次：不可空白");
      return;
    }
    if (wp.itemEmpty("ll_close_date")) {
      errmsg("關帳日期-上上次：不可空白");
      return;
    }
    if (wp.itemEmpty("this_interest_date")) {
      errmsg("利息起算日期-本次：不可空白");
      return;
    }
    if (wp.itemEmpty("last_interest_date")) {
      errmsg("利息起算日期-上次：不可空白");
      return;
    }
    if (wp.itemEmpty("next_interest_date")) {
      errmsg("利息起算日期-下次：不可空白");
      return;
    }
    if (wp.itemEmpty("ll_interest_date")) {
      errmsg("利息起算日期-上上次：不可空白");
      return;
    }
    if (wp.itemEmpty("this_lastpay_date")) {
      errmsg("繳款截止日期-本次：不可空白");
      return;
    }
    if (wp.itemEmpty("last_lastpay_date")) {
      errmsg("繳款截止日期-上次：不可空白");
      return;
    }
    if (wp.itemEmpty("next_lastpay_date")) {
      errmsg("繳款截止日期-下次：不可空白");
      return;
    }
    if (wp.itemEmpty("ll_lastpay_date")) {
      errmsg("繳款截止日期-上上次：不可空白");
      return;
    }
    if (wp.itemEmpty("this_delaypay_date")) {
      errmsg("繳款寬延日期-本次：不可空白");
      return;
    }
    if (wp.itemEmpty("last_delaypay_date")) {
      errmsg("繳款寬延日期-上次：不可空白");
      return;
    }
    if (wp.itemEmpty("next_delaypay_date")) {
      errmsg("繳款寬延日期-下次：不可空白");
      return;
    }
    if (wp.itemEmpty("ll_delaypay_date")) {
      errmsg("繳款寬延日期-上上次：不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where stmt_cycle= ? and nvl(mod_seqno,0) = ? " ;
    log("sql-where=" + sqlWhere);
    if (this.isOtherModify("PTR_WORKDAY", sqlWhere, new Object[] {stmtCycle, wp.modSeqno()})) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into ptr_workday (" + " stmt_cycle ," // 1
        + " issue_s_day ," + " issue_e_day ," + " close_stand ," + " interest_stand ," // 5
        + " lastpay_stand ," + " delaypay_stand ," + " this_acct_month ," + " last_acct_month ,"
        + " next_acct_month ," // 10
        + " ll_acct_month ," + " this_close_date ," + " last_close_date ," + " next_close_date ,"
        + " ll_close_date ," // 15
        + " this_billing_date ," + " last_billing_date ," + " next_billing_date ,"
        + " ll_billing_date ," + " this_interest_date ," // 20
        + " last_interest_date ," + " next_interest_date ," + " ll_interest_date ,"
        + " this_lastpay_date ," + " last_lastpay_date ," // 25
        + " next_lastpay_date ," + " ll_lastpay_date ," + " this_delaypay_date ,"
        + " last_delaypay_date ," + " next_delaypay_date ," // 30
        + " lastpay_next_n_days ," + " ll_delaypay_date ," + " t_1st_del_notice_date ,"
        + " l_1st_del_notice_date ," + " n_1st_del_notice_date ," // 35
        + " ll_1st_del_notice_date ," + " t_2st_del_notice_date ," + " l_2st_del_notice_date ,"
        + " n_2st_del_notice_date ," + " ll_2st_del_notice_date ," // 40
        + " t_3th_del_notice_date ," + " l_3th_del_notice_date ," + " n_3th_del_notice_date ,"
        + " ll_3th_del_notice_date ," + " cycle_flag ," // 45
        + " crt_date ," + " crt_user ," + " mod_user ," // 50
        + " mod_time ," + " mod_pgm ," + " mod_seqno " // 53
        + " ) values ( " + " :stmt_cycle ," // 1
        + " :issue_s_day ," + " :issue_e_day ," + " :close_stand ," + " :interest_stand ," // 5
        + " :lastpay_stand ," + " :delaypay_stand ," + " :this_acct_month ," + " :last_acct_month ,"
        + " :next_acct_month ," // 10
        + " :ll_acct_month ," + " :this_close_date ," + " :last_close_date ,"
        + " :next_close_date ," + " :ll_close_date ," // 15
        + " :this_billing_date ," + " :last_billing_date ," + " :next_billing_date ,"
        + " :ll_billing_date ," + " :this_interest_date ," // 20
        + " :last_interest_date ," + " :next_interest_date ," + " :ll_interest_date ,"
        + " :this_lastpay_date ," + " :last_lastpay_date ," // 25
        + " :next_lastpay_date ," + " :ll_lastpay_date ," + " :this_delaypay_date ,"
        + " :last_delaypay_date ," + " :next_delaypay_date ," // 30
        + " :lastpay_next_n_days ," + " :ll_delaypay_date ," + " :t_1st_del_notice_date ,"
        + " :l_1st_del_notice_date ," + " :n_1st_del_notice_date ," // 35
        + " :ll_1st_del_notice_date ," + " :t_2st_del_notice_date ," + " :l_2st_del_notice_date ,"
        + " :n_2st_del_notice_date ," + " :ll_2st_del_notice_date ," // 40
        + " :t_3th_del_notice_date ," + " :l_3th_del_notice_date ," + " :n_3th_del_notice_date ,"
        + " :ll_3th_del_notice_date ," + " :cycle_flag ," // 45
        + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," // 50
        + " 1 " + " )";

    setString("stmt_cycle", stmtCycle);
    item2ParmStr("issue_s_day");
    item2ParmStr("issue_e_day");
    item2ParmStr("close_stand");
    item2ParmStr("interest_stand");
    item2ParmStr("lastpay_stand");
    item2ParmStr("delaypay_stand");
    item2ParmStr("this_acct_month");
    item2ParmStr("last_acct_month");
    item2ParmStr("next_acct_month");
    item2ParmStr("ll_acct_month");
    item2ParmStr("this_close_date");
    item2ParmStr("last_close_date");
    item2ParmStr("next_close_date");
    item2ParmStr("ll_close_date");
    item2ParmStr("this_billing_date");
    item2ParmStr("last_billing_date");
    item2ParmStr("next_billing_date");
    item2ParmStr("ll_billing_date");
    item2ParmStr("this_interest_date");
    item2ParmStr("last_interest_date");
    item2ParmStr("next_interest_date");
    item2ParmStr("ll_interest_date");
    item2ParmStr("this_lastpay_date");
    item2ParmStr("last_lastpay_date");
    item2ParmStr("next_lastpay_date");
    item2ParmStr("ll_lastpay_date");
    item2ParmStr("this_delaypay_date");
    item2ParmStr("last_delaypay_date");
    item2ParmStr("next_delaypay_date");
    item2ParmNum("lastpay_next_n_days");
    item2ParmStr("ll_delaypay_date");
    item2ParmStr("t_1st_del_notice_date");
    item2ParmStr("l_1st_del_notice_date");
    item2ParmStr("n_1st_del_notice_date");
    item2ParmStr("ll_1st_del_notice_date");
    item2ParmStr("t_2st_del_notice_date");
    item2ParmStr("l_2st_del_notice_date");
    item2ParmStr("n_2st_del_notice_date");
    item2ParmStr("ll_2st_del_notice_date");
    item2ParmStr("t_3th_del_notice_date");
    item2ParmStr("l_3th_del_notice_date");
    item2ParmStr("n_3th_del_notice_date");
    item2ParmStr("ll_3th_del_notice_date");
    item2ParmStr("cycle_flag");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ptrm0080");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    strSql = " update ptr_workday set " + " issue_s_day =:issue_s_day ,"
        + " issue_e_day =:issue_e_day ," + " close_stand =:close_stand ,"
        + " interest_stand =:interest_stand ," + " lastpay_stand =:lastpay_stand ,"
        + " delaypay_stand =:delaypay_stand ," + " this_acct_month =:this_acct_month ,"
        + " last_acct_month =:last_acct_month ," + " next_acct_month =:next_acct_month ,"
        + " ll_acct_month =:ll_acct_month ," + " this_close_date =:this_close_date ,"
        + " last_close_date =:last_close_date ," + " next_close_date =:next_close_date ,"
        + " ll_close_date =:ll_close_date ," + " this_billing_date =:this_billing_date ,"
        + " last_billing_date =:last_billing_date ," + " next_billing_date =:next_billing_date ,"
        + " ll_billing_date =:ll_billing_date ," + " this_interest_date =:this_interest_date ,"
        + " last_interest_date =:last_interest_date ,"
        + " next_interest_date =:next_interest_date ," + " ll_interest_date =:ll_interest_date ,"
        + " this_lastpay_date =:this_lastpay_date ," + " last_lastpay_date =:last_lastpay_date ,"
        + " next_lastpay_date =:next_lastpay_date ," + " ll_lastpay_date =:ll_lastpay_date ,"
        + " this_delaypay_date =:this_delaypay_date ,"
        + " last_delaypay_date =:last_delaypay_date ,"
        + " next_delaypay_date =:next_delaypay_date ,"
        + " lastpay_next_n_days =:lastpay_next_n_days ," + " ll_delaypay_date =:ll_delaypay_date ,"
        + " t_1st_del_notice_date =:t_1st_del_notice_date ,"
        + " l_1st_del_notice_date =:l_1st_del_notice_date ,"
        + " n_1st_del_notice_date =:n_1st_del_notice_date ,"
        + " ll_1st_del_notice_date =:ll_1st_del_notice_date ,"
        + " t_2st_del_notice_date =:t_2st_del_notice_date ,"
        + " l_2st_del_notice_date =:l_2st_del_notice_date ,"
        + " n_2st_del_notice_date =:n_2st_del_notice_date ,"
        + " ll_2st_del_notice_date =:ll_2st_del_notice_date ,"
        + " t_3th_del_notice_date =:t_3th_del_notice_date ,"
        + " l_3th_del_notice_date =:l_3th_del_notice_date ,"
        + " n_3th_del_notice_date =:n_3th_del_notice_date ,"
        + " ll_3th_del_notice_date =:ll_3th_del_notice_date ," + " cycle_flag =:cycle_flag ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where stmt_cycle =:kk1 ";

    setString("kk1", stmtCycle);
    item2ParmStr("issue_s_day");
    item2ParmStr("issue_e_day");
    item2ParmStr("close_stand");
    item2ParmStr("interest_stand");
    item2ParmStr("lastpay_stand");
    item2ParmStr("delaypay_stand");
    item2ParmStr("this_acct_month");
    item2ParmStr("last_acct_month");
    item2ParmStr("next_acct_month");
    item2ParmStr("ll_acct_month");
    item2ParmStr("this_close_date");
    item2ParmStr("last_close_date");
    item2ParmStr("next_close_date");
    item2ParmStr("ll_close_date");
    item2ParmStr("this_billing_date");
    item2ParmStr("last_billing_date");
    item2ParmStr("next_billing_date");
    item2ParmStr("ll_billing_date");
    item2ParmStr("this_interest_date");
    item2ParmStr("last_interest_date");
    item2ParmStr("next_interest_date");
    item2ParmStr("ll_interest_date");
    item2ParmStr("this_lastpay_date");
    item2ParmStr("last_lastpay_date");
    item2ParmStr("next_lastpay_date");
    item2ParmStr("ll_lastpay_date");
    item2ParmStr("this_delaypay_date");
    item2ParmStr("last_delaypay_date");
    item2ParmStr("next_delaypay_date");
    item2ParmNum("lastpay_next_n_days");
    item2ParmStr("ll_delaypay_date");
    item2ParmStr("t_1st_del_notice_date");
    item2ParmStr("l_1st_del_notice_date");
    item2ParmStr("n_1st_del_notice_date");
    item2ParmStr("ll_1st_del_notice_date");
    item2ParmStr("t_2st_del_notice_date");
    item2ParmStr("l_2st_del_notice_date");
    item2ParmStr("n_2st_del_notice_date");
    item2ParmStr("ll_2st_del_notice_date");
    item2ParmStr("t_3th_del_notice_date");
    item2ParmStr("l_3th_del_notice_date");
    item2ParmStr("n_3th_del_notice_date");
    item2ParmStr("ll_3th_del_notice_date");
    item2ParmStr("cycle_flag");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ptrm0080");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
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
    strSql = "delete PTR_WORKDAY " + sqlWhere;
    log("del-sql=" + strSql);
    rc = sqlExec(strSql, new Object[] {stmtCycle, wp.modSeqno()});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
