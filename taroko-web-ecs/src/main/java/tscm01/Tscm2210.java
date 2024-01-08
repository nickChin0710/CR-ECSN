/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-02-27	                   JH	                 modify
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change
* 109-04-20  V1.00.03   shiyuqi       updated for project coding standard   *
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
* 110-08-02  V1.00.06  Bo Yang       添加Debit悠遊卡  
* 111-04-14  V1.00.07  machao     TSC畫面整合                          *
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Tscm2210 extends BaseAction {
  String cardNo = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        wp.colSet("IND_NUM", "" + 0);
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
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
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
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxKey();
        break;
      default:
        break;
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_black_date1"), wp.itemStr("ex_black_date2")) == false) {
      alertErr2("黑名單停掛日期 : 起迄錯誤");
      return;
    }


//    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_tsc_card_no"), "A.tsc_card_no")
//        + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
//        + sqlCol(wp.itemStr("ex_black_date1"), "A.black_date", ">=")
//        + sqlCol(wp.itemStr("ex_black_date2"), "A.black_date", "<=");
//
//    wp.whereStr = lsWhere;
//    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.sqlCmd = "SELECT A.tsc_card_no, A.card_no, A.black_date, A.black_user_id, " +
            "A.black_flag, decode(A.black_flag,'1','強制報送','2','黑名單','3','不報送','4','強制報送-已餘轉') as tt_black_flag, " +
            "A.send_date_s, A.send_date_e, B.current_code, B.new_end_date " +
            "FROM tsc_bkec_expt A join tsc_card B on A.tsc_card_no = B.tsc_card_no " +
            " where 1=1 " + sqlCol(wp.itemStr("ex_tsc_card_no"), "A.tsc_card_no")
            + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
            + sqlCol(wp.itemStr("ex_black_date1"), "A.black_date", ">=")
            + sqlCol(wp.itemStr("ex_black_date2"), "A.black_date", "<=") +
            "union " +
            "SELECT A.tsc_card_no, B.vd_card_no, A.black_date, A.black_user_id, " +
            "A.black_flag, decode(A.black_flag,'1','強制報送','2','黑名單','3','不報送','4','強制報送-已餘轉') as tt_black_flag, " +
            "A.send_date_s, A.send_date_e, B.current_code, B.new_end_date " +
            "FROM tsc_bkec_expt A join tsc_vd_card B on A.tsc_card_no = B.tsc_card_no " +
            " where 1=1 " + sqlCol(wp.itemStr("ex_tsc_card_no"), "A.tsc_card_no")
            + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
            + sqlCol(wp.itemStr("ex_black_date1"), "A.black_date", ">=")
            + sqlCol(wp.itemStr("ex_black_date2"), "A.black_date", "<=") +
            "ORDER BY 1";
//    wp.selectSQL = "" + " A.tsc_card_no , " + " A.card_no , " + " A.black_date , "
//        + " A.black_user_id , " + " A.black_flag , "
//        + " decode(A.black_flag,'1','強制報送','2','黑名單','3','不報送','4','強制報送-已餘轉') as tt_black_flag , "
//        + " A.send_date_s , " + " A.send_date_e , " + " B.current_code , " + " B.new_end_date ";
//    wp.daoTable = "tsc_bkec_expt A join tsc_card B on A.tsc_card_no = B.tsc_card_no ";
//    if (empty(wp.whereStr)) {
//      wp.whereStr = " ORDER BY 1";
//    }
//    wp.whereOrder = " order by 1 ";
    wp.pageCountSql = "select count(1) from (" + wp.sqlCmd + ")";
    logSql();
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo)) {
      cardNo = itemkk("tsc_card_no");
    }

//    wp.selectSQL = " hex(A.rowid) as rowid , " + " A.mod_seqno , " + " A.tsc_card_no , "
//        + " A.card_no , " + " A.black_date , " + " A.black_user_id , " + " A.black_remark , "
//        + " A.crt_user , " + " A.crt_date , " + " A.black_flag , " + " A.send_date_s , "
//        + " A.send_date_e , " + " A.from_type , " + " A.apr_date , " + " A.apr_user , "
//        + " B.current_code , " + " B.new_end_date ," + " A.mod_user ,"
//        + " to_char(A.mod_time,'yyyymmdd') as mod_date ," + " B.lock_date , "
//        + " decode(A.from_type,'1','人工','2','批次') as tt_from_type ";
//    wp.daoTable = "tsc_bkec_expt A join tsc_card B on A.tsc_card_no = B.tsc_card_no ";
//    wp.whereStr = "where 1=1" + sqlCol(cardNo, "A.tsc_card_no");

    wp.sqlCmd = "select hex(A.rowid) as rowid , " + " A.mod_seqno , " + " A.tsc_card_no , "
            + " A.card_no , " + " A.black_date , " + " A.black_user_id , " + " A.black_remark , "
            + " A.crt_user , " + " A.crt_date , " + " A.black_flag , " + " A.send_date_s , "
            + " A.send_date_e , " + " A.from_type , " + " A.apr_date , " + " A.apr_user , "
            + " B.current_code , " + " B.new_end_date ," + " A.mod_user ,"
            + " to_char(A.mod_time,'yyyymmdd') as mod_date ," + " B.lock_date , "
            + " decode(A.from_type,'1','人工','2','批次') as tt_from_type "
            + " from tsc_bkec_expt A join tsc_card B on A.tsc_card_no = B.tsc_card_no "
            + " where 1=1" + sqlCol(cardNo, "A.tsc_card_no")
            + " union "
            + "select hex(A.rowid) as rowid , " + " A.mod_seqno , " + " A.tsc_card_no , "
            + " B.vd_card_no , " + " A.black_date , " + " A.black_user_id , " + " A.black_remark , "
            + " A.crt_user , " + " A.crt_date , " + " A.black_flag , " + " A.send_date_s , "
            + " A.send_date_e , " + " A.from_type , " + " A.apr_date , " + " A.apr_user , "
            + " B.current_code , " + " B.new_end_date ," + " A.mod_user ,"
            + " to_char(A.mod_time,'yyyymmdd') as mod_date ," + " B.lock_date , "
            + " decode(A.from_type,'1','人工','2','批次') as tt_from_type "
            + " from tsc_bkec_expt A join tsc_vd_card B on A.tsc_card_no = B.tsc_card_no "
            + " where 1=1" + sqlCol(cardNo, "A.tsc_card_no");
    logSql();
    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("查無資料");
      return;
    }

    if (wp.colEq("current_code", "0")) {
      wp.colSet("tt_current_code", "0.正常");
    } else if (wp.colEq("current_code", "1")) {
      wp.colSet("tt_current_code", "1.申停");
    } else if (wp.colEq("current_code", "2")) {
      wp.colSet("tt_current_code", "2.掛失");
    } else if (wp.colEq("current_code", "3")) {
      wp.colSet("tt_current_code", "3.強制停用");
    } else if (wp.colEq("current_code", "4")) {
      wp.colSet("tt_current_code", "4.其他停用");
    } else if (wp.colEq("current_code", "5")) {
      wp.colSet("tt_current_code", "5.偽卡");
    }

  }

  @Override
  public void saveFunc() throws Exception {
//    String lsUser = wp.colStr("approval_user");
//    String lsPasswd = wp.colStr("approval_passwd");
    if (checkApproveZz() == false) {
      return;
    }


    tscm01.Tscm2210Func func = new tscm01.Tscm2210Func();
    func.setConn(wp);
//    if (this.isAdd() || this.isUpdate()) {
//      if (dataCheck() == false) {
//        wp.colSet("approval_user", lsUser);
//        wp.colSet("approval_passwd", lsPasswd);
//        return;
//      }
//    }
    rc = func.dbSave(strAction);

    sqlCommit(rc);

    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);
  }

  boolean dataCheck() {
    if (this.isAdd())
      cardNo = wp.itemStr("kk_tsc_card_no");
    if (this.isUpdate())
      cardNo = wp.itemStr("tsc_card_no");

    String sql1 = "select blacklt_s_date, blacklt_e_date from tsc_card where tsc_card_no = ? "
            + "union select blacklt_s_date, blacklt_e_date from tsc_vd_card where tsc_card_no = ? ";
    sqlSelect(sql1, new Object[] {cardNo, cardNo});

    if (!empty(sqlStr("blacklt_s_date")) && !empty("blacklt_e_date")) {
      if (itemEq("conf_flag", "Y") == false) {
        wp.respMesg = "悠遊卡已列黑名單, 是否再指定";
        wp.colSet("conf_mesg", " || 1==1 ");
        if (isAdd()) {
          wp.colSet("doit", "'A'");
        } else {
          wp.colSet("doit", "'U'");
        }
        return false;
      }
    }
    return true;
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "tscm2210_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  public void wfAjaxKey() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =
    selectData(wp.itemStr("ax_card_no"));
    if (rc != 1) {
      wp.addJSON("card_no", "");
      wp.addJSON("current_code", "");
      wp.addJSON("tt_current_code", "");
      wp.addJSON("new_end_date", "");
      return;
    }
    wp.addJSON("card_no", sqlStr("card_no"));
    wp.addJSON("current_code", sqlStr("current_code"));
    wp.addJSON("new_end_date", sqlStr("new_end_date"));
    if (eqIgno(sqlStr("current_code"), "0")) {
      wp.addJSON("tt_current_code", "0.正常");
    } else if (eqIgno(sqlStr("current_code"), "1")) {
      wp.addJSON("tt_current_code", "1.申停");
    } else if (eqIgno(sqlStr("current_code"), "2")) {
      wp.addJSON("tt_current_code", "2.掛失");
    } else if (eqIgno(sqlStr("current_code"), "3")) {
      wp.addJSON("tt_current_code", "3.強制停用");
    } else if (eqIgno(sqlStr("current_code"), "4")) {
      wp.addJSON("tt_current_code", "4.其他停用");
    } else if (eqIgno(sqlStr("current_code"), "5")) {
      wp.addJSON("tt_current_code", "5.偽卡");
    }

  }

  void selectData(String cardNo) {
    String sql1 = " select card_no , current_code from tsc_card where tsc_card_no = ? "
            + " union select vd_card_no , current_code from tsc_vd_card where tsc_card_no = ? ";

    sqlSelect(sql1, new Object[] {cardNo, cardNo});

    if (sqlRowNum <= 0) {
      alertErr2("悠遊卡卡號不存在:" + cardNo);
      return;
    }

    String sql2 = " select " + " new_end_date " + " from crd_card " + " where card_no = ? "
            + " union select " + " new_end_date " + " from dbc_card " + " where card_no = ? ";

    sqlSelect(sql2, new Object[] {sqlStr("card_no"), sqlStr("card_no")});

    if (sqlRowNum <= 0) {
      alertErr2("悠遊卡卡號不存在:" + cardNo);
      return;
    }

  }

}
