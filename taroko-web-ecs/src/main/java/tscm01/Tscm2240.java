/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-01-06  V1.00.01   Justin Wu    updated for archit.  change
* 109-04-28  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
* 110/01/19  V1.00.06  Wilson        ecsfunc.DeCodeCrd.electronicCurrentCode 
* 111-04-14  V1.00.07  machao     TSC畫面整合*
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Tscm2240 extends BaseAction {
  String cardNO = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
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
    if (!empty(wp.itemStr("ex_card_no"))) {
      if (wp.itemStr("ex_card_no").length() < 12) {
        alertErr2("卡號  至少要  12  碼");
        return;
      }
    }
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建檔日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "crt_user");
    if (wp.itemEq("ex_send_flag", "1")) {
      lsWhere += " and send_date='' ";
    } else if (wp.itemEq("ex_send_flag", "2")) {
      lsWhere += " and send_date<>'' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " crt_date , " + " crt_time , " + " card_no , " + " secu_code , "
        + " decode(secu_code,'1','拒絕代行','2','鎖卡','3','取消拒絕代行','4','取消鎖卡') as tt_secu_code , "
        + " risk_remark , " + " crt_user , " + " send_date , " + " hex(rowid) as rowid "

    ;
    wp.daoTable = "tsc_refuse_log ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by crt_date Desc, crt_time Desc, card_no ";
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
    cardNO = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNO)) {
      cardNO = itemkk("card_no");
    }
    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " crt_date , " + " crt_time , "
        + " card_no , " + " secu_code , " + " risk_remark , " + " crt_user , " + " send_date , "
        + " '' as db_tsc_card_no , " + " '0' as db_curr_code , " + " '' as db_end_date , "
        + " 'N' as db_tsc_57 , " + " 'N' as db_tsc_04 , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "tsc_refuse_log";
    wp.whereStr = " where 1=1 " + sqlCol(cardNO, "card_no");
    wp.whereOrder = " order by crt_date desc, crt_time desc fetch first 1 row only ";
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNO);
      return;
    }
    dataAfter();
  }

  void dataAfter() {

    String sql1 = "select tsc_card_no, current_code, new_end_date  " + " from tsc_card "
        + " where card_no =? " + " order by new_end_date desc,current_code fetch first 1 row only ";
    sqlSelect(sql1, new Object[] {cardNO});

    if (sqlRowNum <= 0) {
      wp.colSet("db_tsc_card_no", "");
      wp.colSet("db_curr_code", "");
      wp.colSet("db_end_date", "");
    } else {
      wp.colSet("db_tsc_card_no", sqlStr("tsc_card_no"));
      wp.colSet("db_curr_code", ecsfunc.DeCodeCrd.electronicCurrentCode(sqlStr("current_code")));
      wp.colSet("db_end_date", sqlStr("new_end_date"));
      
    }

    String sql2 = "select sum(decode(risk_class,'57',1,0)) as ll_cnt57 ,"
        + " sum(decode(risk_class,'04',1,0)) as ll_cnt04 " + " from tsc_rm_actauth "
        + " where card_no =?" + " and risk_class in ('04','57') ";
    sqlSelect(sql2, new Object[] {cardNO});

    if (sqlNum("ll_cnt57") > 0) {
      wp.colSet("db_tsc57", "Y");
    } else {
      wp.colSet("db_tsc57", "N");
    }

    if (sqlNum("ll_cnt04") > 0) {
      wp.colSet("db_tsc04", "Y");
    } else {
      wp.colSet("db_tsc04", "N");
    }
    log("A:" + wp.colStr("db_tsc57"));
    log("B:" + wp.colStr("db_tsc04"));

  }

  @Override
  public void saveFunc() throws Exception {
    tscm01.Tscm2240Func func = new tscm01.Tscm2240Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
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
      wp.addJSON("db_tsc_card_no", "");
      wp.addJSON("db_curr_code", "");
      wp.addJSON("db_end_date", "");
      return;
    }
    wp.addJSON("db_tsc_card_no", sqlStr("tsc_card_no"));
    wp.addJSON("db_curr_code", sqlStr("current_code"));
    wp.addJSON("db_end_date", sqlStr("new_end_date"));
    wp.addJSON("tt_curr_code", ecsfunc.DeCodeCrd.electronicCurrentCode(sqlStr("current_code")));
 
  }

  void selectData(String cardNo) {
    String sql1 = " select " + " tsc_card_no , " + " current_code , " + " new_end_date "
        + " from tsc_card " + " where card_no = ? " + " order by new_end_date desc,current_code fetch first 1 row only ";

    sqlSelect(sql1, new Object[] {cardNo});

    if (sqlRowNum <= 0) {
      alertErr2("卡號不存在:" + cardNo);
      return;
    }
  }

}
