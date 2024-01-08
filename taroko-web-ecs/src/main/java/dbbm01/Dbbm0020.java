/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-25  V1.00.01   Justin      parameterize sql
******************************************************************************/
package dbbm01;

import ofcapp.BaseAction;

public class Dbbm0020 extends BaseAction {
  String rowid = "", mod_seqno = "";

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    rowid = wp.itemStr("rowid");
    mod_seqno = wp.itemStr("mod_seqno");

    wp.selectSQL = "" + " fees_fix_amt , " + " fix_rate , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_pgm , " + " mod_seqno , "
        + " hex(rowid) as rowid ";
    wp.daoTable = "dbb_markup";
    wp.whereStr = " where 1=1 " + sqlCol(mod_seqno, "mod_seqno");
    if (!empty(rowid)) {
      wp.whereStr += " and hex(rowid) = ? ";
      setString(rowid);
    }

    pageSelect();
    if (sqlNotFind()) {
      insertNewData();
    }
  }

  void insertNewData() throws Exception {
    Dbbm0020Func func = new Dbbm0020Func();
    func.setConn(wp);

    rc = func.dbInsert();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      dataRead();
  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false) {
      return;
    }

    Dbbm0020Func func = new Dbbm0020Func();
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
    if (eqIgno(wp.respHtml, "dbbm0020")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    try {
      dataRead();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
