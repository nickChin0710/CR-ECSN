/*卡戶月平均消費限額參數維護
 * */
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-23  V1.00.02  Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;

import ofcapp.BaseAction;
import taroko.base.CommSqlStr;

public class Ptrm0320 extends BaseAction {
  String acctType = "";

  @Override
  public void userAction() throws Exception {
    if (wp.buttonCode.equals("X")) {
      strAction = "new";
      clearFunc();
    } else if (wp.buttonCode.equals("R")) {
      strAction = "R";
      dataRead();
    } else if (wp.buttonCode.equals("A")) /* 新增 */
    {
      strAction = "A";
      this.saveFunc();
    } else if (wp.buttonCode.equals("U")) /* 修改 */
    {
      strAction = "U";
      this.saveFunc();
    } else if (wp.buttonCode.equals("D")) /* 刪除 */
    {
      strAction = "D";
      this.saveFunc();
    } else if (wp.buttonCode.equals("M")) /* 查詢skip-page */
    {
      queryRead();
    } else if (wp.buttonCode.equals("S")) /* 資料選取 */
    {
      querySelect();
    } else if (wp.buttonCode.equals("L")) /* 清除 */
    {
      strAction = "";
      clearFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("kk_acct_type");
      dddwList("ddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
    } catch (Exception ex) {
    }
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
    acctType = wp.itemStr("kk_acct_type");
    if (empty(acctType)) {
      acctType = wp.itemStr("acct_type");
    }
    if (empty(acctType)) {
      alertErr2("帳戶類別: 不可空白");
      return;
    }

    // -
    wp.selectSQL =
        "hex(rowid) as rowid, " + "nvl(to_char(mod_time,'yyyymmdd'),' ') as mod_date, " + "A.*";
    wp.daoTable = "ptr_pur_lmt A";
    wp.whereStr = "where 1=1" + sqlCol(acctType, "acct_type");
    this.pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料 key=" + acctType + ";");
      clearFunc();
    }

    return;
  }

  @Override
  public void saveFunc() throws Exception {
    ptrm02.Ptrm0320Func func = new ptrm02.Ptrm0320Func();
    func.setConn(wp);

    if (checkApproveZz() == false)
      return;

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
    this.btnModeAud();
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
