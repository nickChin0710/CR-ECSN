/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-05-17  V1.00.00  yash       program initial                            *
*                                                                            *
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0025 extends BaseEdit {
  String mExCardNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and  card_no like :card_no ";
      setString("card_no", wp.itemStr("ex_card_no") + "%");
    }

    if (empty(wp.itemStr("ex_crt_date")) == false) {
      wp.whereStr += " and  crt_date = :crt_date ";
      setString("crt_date", wp.itemStr("ex_crt_date"));
    }

    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and  crt_user = :crt_user ";
      setString("crt_user", wp.itemStr("ex_crt_user"));
    }

    return true;
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

    wp.selectSQL = " card_no" + ", prohibit_remark" + ", crt_date" + ", crt_user";

    wp.daoTable = "crd_prohibit";
    wp.whereOrder = " order by card_no";
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
    mExCardNo = wp.itemStr("card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExCardNo = wp.itemStr("kk_card_no");
    if (empty(mExCardNo)) {
      mExCardNo = itemKk("data_k1");
    }

    if (empty(mExCardNo)) {
      mExCardNo = wp.colStr("card_no");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", card_no " + ", prohibit_remark"
        + ", crt_date" + ", crt_user";
    wp.daoTable = "crd_prohibit";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  card_no = :card_no ";
    setString("card_no", mExCardNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, card_no=" + mExCardNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Crdm0025Func func = new Crdm0025Func(wp);

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
