/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0145 extends BaseEdit {
  Ptrm0145Func func;
  String acctType = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      // 轉換顯示畫面
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
  public void initPage() {
    dddwSelect();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("kk_acct_type");
      dddwList("d_dddw_accttype", "PTR_ACCT_TYPE", "acct_type", "chin_name", "where 1=1");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("acct_type"), "acct_type");
    wp.whereOrder = " order by db_sort1 ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();



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
    acctType = itemKk("acct_type");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "acct_type, " + "fix_penalty, "
        + "percent_penalty, " + "max_penalty, " + "min_penalty, " + "first_penalty, "
        + "second_penalty, " + "third_penalty, " + "first_month, " + "second_month, "
        + "third_month, " + "method, " + "pn_max_cnt, " + "crt_date," + "crt_user," + "mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "ptr_actpenalty";
    wp.whereStr = "where 1=1" + sqlCol(acctType, "acct_type");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctType);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {

    if (this.checkApproveZz() == false) {
      return;
    }

    this.updateRetrieve = true;

    func = new ptrm01.Ptrm0145Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {

    this.btnModeAud();

  }

}
