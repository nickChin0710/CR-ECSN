/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add initButton                             *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0460 extends BaseEdit {
  Ptrm0460Func func;

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
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "paper2email_mm,   " + "email2paper_mm, "
        + "crt_user, " + "crt_date," + "apr_flag," + "apr_date," + "apr_user," + "mod_user, "
        + "uf_2ymd(mod_time) as mod_date ";
    wp.daoTable = "ptr_stat_coexist";
    wp.whereStr = "where 1=1";
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=");
    }
  }

  @Override
  public void saveFunc() throws Exception {

    if (checkApproveZz() == false)
      return;

    this.updateRetrieve = true;

    func = new ptrm02.Ptrm0460Func(wp);
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    btnModeAud();

  }

}
