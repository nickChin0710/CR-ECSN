/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*                        V1.00.00                          program initial                            *
* 109-01-03  V1.00.01   Justin Wu    updated for archit.  change
* 109-04-20  V1.00.02  shiyuqi       updated for project coding standard     * 
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Ichm0050 extends BaseAction {
  String ichCardNo = "";

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
        // AJAX 20200102 updated for archit. change
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
    if (chkStrend(wp.itemStr2("ex_black_date1"), wp.itemStr2("ex_black_date2")) == false) {
      alertErr2("黑名單停掛日期: 起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr2("ex_ich_card_no"), "A.ich_card_no")
        + sqlCol(wp.itemStr2("ex_card_no"), "A.card_no")
        + sqlCol(wp.itemStr2("ex_black_date1"), "A.black_date", ">=")
        + sqlCol(wp.itemStr2("ex_black_date2"), "A.black_date", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " A.ich_card_no ," + " A.card_no ," + " A.black_date ,"
        + " A.black_user_id ," + " B.current_code ," + " B.new_end_date ," + " A.black_flag ,"
        + " A.send_date_s ," + " A.send_date_e ,"
        + " decode(A.black_flag,'1','強制報送','2','黑名單','3','不報送','4','強制報送-已餘轉') as tt_black_flag ";
    wp.daoTable = " ich_black_list A join ich_card B on A.ich_card_no = B.ich_card_no ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    ichCardNo = wp.itemStr2("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(ichCardNo))
      ichCardNo = itemkk("ich_card_no");

    if (empty(ichCardNo)) {
      alertErr2("愛金卡卡號: 不可空白");
      return;
    }

    wp.selectSQL = "" + " A.ich_card_no ," + " A.card_no ," + " A.black_date ,"
        + " A.black_user_id ," + " A.black_remark ," + " A.crt_user ," + " A.crt_date ,"
        + " A.mod_user ," + " A.mod_time ," + " A.mod_pgm ," + " A.mod_seqno," + " B.current_code ,"
        + " B.new_end_date ," + " A.rowid ," + " A.black_flag ," + " A.send_date_s ,"
        + " A.send_date_e ," + " A.from_type ," + " A.apr_date ," + " A.apr_user ,"
        + " B.return_date , " + " B.lock_date , " + " B.blacklt_s_date , " + " B.blacklt_e_date , "
        + " hex(A.rowid) as rowid "

    ;

    wp.daoTable = " ich_black_list A join ich_card B on A.ich_card_no = B.ich_card_no ";

    wp.whereStr = " where 1=1 " + sqlCol(ichCardNo, "A.ich_card_no");

    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("查無資料");
      return;
    }
    dataReadAfter();
  }

  void dataReadAfter() {
    wp.colSet("ajax_code", "N");
    wp.colSet("black_date", getSysDate());
    wp.colSet("black_user_id", wp.loginUser);

    if (wp.colEq("from_type", "1")) {
      wp.colSet("tt_from_type", "人工");
    } else {
      wp.colSet("tt_from_type", "批次");
    }
  }

  @Override
  public void saveFunc() throws Exception {
    ichm01.Ichm0050Func func = new ichm01.Ichm0050Func();
    func.setConn(wp);

    if (checkApproveZz() == false)
      return;

    rc = func.dbSave(strAction);
    sqlCommit(rc);

    if (rc <= 0) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }


  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "ichm0050_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("ajax_code", "");
      wp.colSet("black_date", getSysDate());
      wp.colSet("black_user_id", wp.loginUser);
      wp.colSet("from_type", "1");
      wp.colSet("tt_from_type", "人工");
    }

  }

  public void wfAjaxKey() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =
    selectData(wp.itemStr("ax_ich_card_no"));
    if (rc != 1) {
      wp.addJSON("card_no", "");
      wp.addJSON("current_code", "");
      wp.addJSON("new_end_date", "");
      wp.addJSON("return_date", "");
      wp.addJSON("lock_date", "");
      wp.addJSON("blacklt_s_date", "");
      wp.addJSON("blacklt_e_date", "");
      wp.addJSON("no_ich_card_no", "N");
      return;
    }
    wp.addJSON("card_no", sqlStr("card_no"));
    wp.addJSON("current_code", sqlStr("current_code"));
    wp.addJSON("new_end_date", sqlStr("new_end_date"));
    wp.addJSON("return_date", sqlStr("return_date"));
    wp.addJSON("lock_date", sqlStr("lock_date"));
    wp.addJSON("blacklt_s_date", sqlStr("blacklt_s_date"));
    wp.addJSON("blacklt_e_date", sqlStr("blacklt_e_date"));
    wp.addJSON("no_ich_card_no", "");

  }

  void selectData(String cardNo) {
    String sql1 = " select " + " card_no , " + " return_date , " + " lock_date , "
        + " blacklt_s_date , " + " blacklt_e_date " + " from ich_card " + " where ich_card_no = ? ";

    sqlSelect(sql1, new Object[] {cardNo});

    if (sqlRowNum <= 0) {
      alertErr2("愛金卡號不存在:" + cardNo);
      return;
    }

    String sql2 = " select current_code , new_end_date from crd_card where card_no = ? "
        + " union all " + " select current_code , new_end_date from dbc_card where card_no = ? ";

    sqlSelect(sql2, new Object[] {sqlStr("card_no"), sqlStr("card_no")});

    if (sqlRowNum <= 0) {
      alertErr2("卡號不存在:" + sqlStr("card_no"));
      return;
    }

  }

}
