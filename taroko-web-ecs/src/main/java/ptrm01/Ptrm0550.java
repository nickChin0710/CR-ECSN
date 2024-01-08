/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-07-31  V1.00.02  yanghan       修改页面覆核栏位     *
******************************************************************************/
package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0550 extends BaseEdit {
  String mExMsgValue = "";

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

    initButton();
  }

  // for query use only
  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 and msg_type = 'BUS_CODE'";

    if (empty(wp.itemStr("ex_msg_value")) == false) {
      wp.whereStr += " and  msg_value = :msg_value ";
      setString("msg_value", wp.itemStr("ex_msg_value"));
    }
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " msg_value " + ", map_value " + ", msg " + ", crt_date " + ", crt_user "
        + ", apr_date " + ", apr_user ";

    wp.daoTable = "crd_message";

    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExMsgValue = wp.itemStr("msg_value");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExMsgValue = wp.itemStr("kk_msg_value");
    if (empty(mExMsgValue)) {
      mExMsgValue = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", msg_value " + ", map_value" + ", msg"
        + ", crt_date" + ", crt_user" + ", apr_date" + ", apr_user";
    wp.daoTable = "crd_message";
    wp.whereStr = "where 1=1 ";
    wp.whereStr += " and msg_type = 'BUS_CODE'";
    wp.whereStr += " and msg_value = :msg_value";
    setString("msg_value", mExMsgValue);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, msg_value=" + mExMsgValue);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0550Func func = new Ptrm0550Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
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
