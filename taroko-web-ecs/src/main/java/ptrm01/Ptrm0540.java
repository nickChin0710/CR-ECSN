/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm01;



import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0540 extends BaseEdit {
  String mExReissueReason = "";
  String mExReissueCode = "";

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

  // for query use only
  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_reissue_reason")) == false) {
      wp.whereStr += " and  reissue_reason = :reissue_reason ";
      setString("reissue_reason", wp.itemStr("ex_reissue_reason"));
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

    wp.selectSQL = " reissue_reason " + ", reissue_code " + ", content " + ", crt_date "
        + ", crt_user " + ", apr_date " + ", apr_user ";

    wp.daoTable = "ptr_reissue_code";
    wp.whereOrder = " order by reissue_reason";

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
    mExReissueReason = wp.itemStr("reissue_reason");
    mExReissueCode = wp.itemStr("reissue_code");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExReissueReason = wp.itemStr("kk_reissue_reason");
    if (empty(mExReissueReason)) {
      mExReissueReason = itemKk("data_k1");
    }
    mExReissueCode = wp.itemStr("kk_reissue_code");
    if (empty(mExReissueCode)) {
      mExReissueCode = itemKk("data_k2");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", reissue_reason " + ", reissue_code"
        + ", content" + ", crt_date" + ", crt_user" + ", apr_date" + ", apr_user";
    wp.daoTable = "ptr_reissue_code";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and reissue_reason = :reissue_reason";
    wp.whereStr += " and reissue_code = :reissue_code";
    setString("reissue_reason", mExReissueReason);
    setString("reissue_code", mExReissueCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, reissue_reason=" + mExReissueReason + "  reissue_code=" + mExReissueCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0540Func func = new Ptrm0540Func(wp);

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
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("kk_reissue_reason");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_reissue_reason");
      }

      this.dddwList("dddw_reissue_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where wf_type = 'REISSUE_REASON'");
    } catch (Exception ex) {
    }
  }

}
