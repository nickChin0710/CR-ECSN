/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0360 extends BaseEdit {
  String mExUseSource = "";

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

    dddwSelect();
    initButton();
  }

  // for query use only
  private void getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_use_source")) == false) {
      wp.whereStr += " and  f.use_source = :use_source ";
      setString("use_source", wp.itemStr("ex_use_source"));
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

    wp.selectSQL = "  f.use_source" + ", u.chi_name ";

    wp.daoTable = " ptr_src_free f  left join ptr_comb_use u on f.use_source = u.code ";
    wp.whereOrder = " order by f.use_source";

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
    mExUseSource = wp.itemStr("use_source");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExUseSource = wp.itemStr("kk_use_source");
    if (empty(mExUseSource)) {
      mExUseSource = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", use_source " + ", apr_date" + ", apr_user"
        + ", crt_date" + ", crt_user";
    wp.daoTable = "ptr_src_free";
    wp.whereStr = "where 1=1 and use_source = :use_source";
    setString("use_source", mExUseSource);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, use_source=" + mExUseSource);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0360Func func = new Ptrm0360Func(wp);

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

  @Override
  public void dddwSelect() {
    try {

      if (wp.respHtml.indexOf("_detl") > 0) {
        // wp.initOption="--";
        // wp.optionKey = wp.item_ss("kk_use_source");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_use_source");
      }

      this.dddwList("dddw_use_source", "ptr_comb_use", "code", "chi_name", "where 1=1");
    } catch (Exception ex) {
    }
  }

}
