/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-15  V1.00.00  yash           program initial                        *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard
* 111-04-14  V1.00.02  machao     TSC畫面整合        *
******************************************************************************/

package tscm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Tscm0030 extends BaseEdit {
  String mExEmbossKind = "";

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
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_emboss_kind")) == false) {
      wp.whereStr += " and  emboss_kind = :emboss_kind ";
      setString("emboss_kind", wp.itemStr("ex_emboss_kind"));
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

    wp.selectSQL = " emboss_kind"
        + ", decode(emboss_kind,'E','毀損補發','1','申請停卡','2','掛失','3','強制停卡','4','效期屆滿且續卡','5','偽卡','6','效期屆滿且不續卡'"
        + ",'7','爭議，不可歸責','8','系統進行餘額轉置') as emboss_kind_desc" + ", days_to_tsc"
        + ", expire_fee_flag" + ", charge_fee" + ", month_times" + ", use_times" + ", month_money"
        + ", use_money" + ", apr_user" + ", apr_date";

    wp.daoTable = "tsc_fee_parm";
    wp.whereOrder = " order by emboss_kind";
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
    mExEmbossKind = wp.itemStr("emboss_kind");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExEmbossKind = wp.itemStr("kk_emboss_kind");
    if (empty(mExEmbossKind)) {
      mExEmbossKind = itemKk("data_k1");
    }
    if (empty(mExEmbossKind)) {
      mExEmbossKind = wp.colStr("emboss_kind");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", emboss_kind "
        + ", decode(emboss_kind,'E','毀損補發','1','申請停卡','2','掛失','3','強制停卡','4','效期屆滿且續卡','5','偽卡','6','效期屆滿且不續卡'"
        + ",'7','爭議，不可歸責','8','系統進行餘額轉置') as emboss_kind_desc" + ", days_to_tsc"
        + ", expire_fee_flag" + ", charge_fee" + ", month_times" + ", use_times" + ", month_money"
        + ", use_money" + ", mod_user" + ", uf_2ymd(mod_time) as mod_date";
    wp.daoTable = "tsc_fee_parm";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  emboss_kind = :emboss_kind ";
    setString("emboss_kind", mExEmbossKind);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, emboss_kind=" + mExEmbossKind);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    Tscm0030Func func = new Tscm0030Func(wp);

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
      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.itemStr("kk_emboss_kind");
      else
        wp.optionKey = wp.itemStr("ex_emboss_kind");
      this.dddwList("dddw_emboss_kind", "tsc_fee_parm", "emboss_kind", "",
          "where 1=1 group by emboss_kind order by emboss_kind");
    } catch (Exception ex) {
    }
  }

}
