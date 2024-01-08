/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add OnlineApprove                          *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/

package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0080 extends BaseEdit {

  String stmtCycle = "";

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
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_stmt_cycle"), "stmt_cycle", ">=");
    wp.whereOrder = " order by stmt_cycle ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();



  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "stmt_cycle, " + "issue_s_day," + "issue_e_day, " + "cycle_flag," + "close_stand";
    wp.daoTable = "PTR_WORKDAY";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  void listWkdata() {

    for (int ii = 0; ii <= wp.selectCnt; ii++) {
      // ????
      if (wp.colStr(ii, "cycle_flag").equals("Y")) {
        wp.colSet(ii, "tt_cycle_flag", "開啟");
      } else {
        wp.colSet(ii, "tt_cycle_flag", "關閉");
      }
      wp.log("" + ii + ": flag=" + wp.colStr(ii, "cycle_flag") + ",tt="
          + wp.colStr(ii, "tt_cycle_flag"));
    }

  }

  @Override
  public void querySelect() throws Exception {
    stmtCycle = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(stmtCycle)) {
      stmtCycle = itemKk("stmt_cycle");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "stmt_cycle,   " + "issue_s_day, "
        + "issue_e_day, " + "this_acct_month," + "last_acct_month," + "next_acct_month,"
        + "ll_acct_month," + "this_close_date," + "last_close_date," + "next_close_date,"
        + "ll_close_date," + "this_billing_date," + "last_billing_date," + "next_billing_date,"
        + "ll_billing_date," + "this_interest_date," + "last_interest_date," + "next_interest_date,"
        + "ll_interest_date," + "this_lastpay_date," + "last_lastpay_date," + "next_lastpay_date,"
        + "ll_lastpay_date," + "this_delaypay_date," + "last_delaypay_date," + "next_delaypay_date,"
        + "ll_delaypay_date," + "t_1st_del_notice_date," + "l_1st_del_notice_date,"
        + "n_1st_del_notice_date," + "ll_1st_del_notice_date," + "t_2st_del_notice_date,"
        + "l_2st_del_notice_date," + "n_2st_del_notice_date," + "ll_2st_del_notice_date,"
        + "t_3th_del_notice_date," + "l_3th_del_notice_date," + "n_3th_del_notice_date,"
        + "ll_3th_del_notice_date," + "cycle_flag,"
        // + "file_date,"
        // + "file_time,"
        // + "userid,"
        + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date," + "mod_pgm,"
        // + "mod_ws,"
        + "mod_seqno,"
        // + "mod_log,"
        + "close_stand," + "interest_stand," + "lastpay_stand," + "delaypay_stand,"
        + "lastpay_next_n_days," + "crt_user," + "crt_date";
    wp.daoTable = "PTR_WORKDAY";
    wp.whereStr = " where 1=1" + sqlCol(stmtCycle, "stmt_cycle");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + stmtCycle);
      return;
    }
  }


  @Override
  public void saveFunc() throws Exception {

    if (this.checkApproveZz() == false)
      return;

    ptrm01.Ptrm0080Func func = new ptrm01.Ptrm0080Func();
    func.setConn(wp);
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
  }
}
