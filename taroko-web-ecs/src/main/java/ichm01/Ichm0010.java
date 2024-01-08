/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;

public class Ichm0010 extends BaseAction {
  // --
  String binType = "", addYear = "", bankId = "", groupId = "", seqNo = "";

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ichm0010_detl")) {
        wp.optionKey = wp.colStr(0, "vendor_ich");
        dddwList("dddw_vendor_ich", "ptr_vendor_setting", "vendor", "vendor_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "";

    lsWhere = " where 1=1 " + sqlCol(wp.itemStr2("ex_add_year"), "add_year");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " bin_type ," + " add_year ," + " seq_no ," + " bank_id ," + " group_id ,"
        + " card_name ," + " card_code ," + " send_date ," + " crt_date ," + " crt_user ,"
        + " apr_date ," + " apr_user ";

    wp.daoTable = "ich_card_parm";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    binType = wp.itemStr2("data_k1");
    addYear = wp.itemStr2("data_k2");
    bankId = wp.itemStr2("data_k3");
    groupId = wp.itemStr2("data_k4");
    seqNo = wp.itemStr2("data_k5");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(binType))
      binType = itemkk("bin_type");
    if (empty(addYear))
      addYear = itemkk("add_year");
    if (empty(bankId))
      bankId = itemkk("bank_id");
    if (empty(groupId))
      groupId = itemkk("group_id");
    if (empty(seqNo))
      seqNo = itemkk("seq_no");

    if (empty(binType) || empty(addYear) || empty(bankId) || empty(groupId) || empty(seqNo)) {
      alertErr2("讀取條件不可空白");
      return;
    }

    wp.selectSQL = "" + " bin_type ," + " add_year ," + " seq_no ," + " bank_id ," + " group_id ,"
        + " card_name ," + " card_code ," + " send_date ," + " crt_date ," + " crt_user ,"
        + " apr_date ," + " apr_user ," + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " mod_pgm ," + " mod_seqno ,"
        + " hex(rowid) as rowid , " + " vendor_ich , " + " seq_no_curr ";

    wp.daoTable = " ich_card_parm ";
    wp.whereStr = " where 1=1 " + sqlCol(binType, "bin_type") + sqlCol(addYear, "add_year")
        + sqlCol(bankId, "bank_id") + sqlCol(groupId, "group_id") + sqlCol(seqNo, "seq_no");

    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    ichm01.Ichm0010Func func = new ichm01.Ichm0010Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);

    if (rc != 1) {
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
    if (eqIgno(wp.respHtml, "ichm0010_detl")) {
      btnModeAud();
      buttonOff("btOther_disable");
    }
  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("kk_group_id", "17");
      wp.colSet("seq_no_curr", "0");
    }

  }

}
