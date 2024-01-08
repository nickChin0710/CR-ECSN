/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex 		 add initButton , Online Approve         *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0260 extends BaseEdit {
  Ptrm0260Func func;
  String rowid = "";

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
  public void initPage() {
    try {
      dataRead();
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
    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno, " + "business_date,   " + "online_date, " + "vouch_date, "
            + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date," + "crt_user," + "crt_date"

    ;
    wp.daoTable = "ptr_businday";
    wp.whereStr = " where 1=1" + sqlCol(rowid, "rowid");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {

    if (checkApproveZz() == false)
      return;

    func = new ptrm01.Ptrm0260Func(wp);
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
