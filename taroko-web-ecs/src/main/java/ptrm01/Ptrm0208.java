/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-05  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0208 extends BaseEdit {
  String mExCorpNo = "";
  String mDaCardType = "";

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

  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_corp_no")) == false) {
      wp.whereStr += " and  f.corp_no = :corp_no ";
      setString("corp_no", wp.itemStr("ex_corp_no"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();// 執行SQL查詢
  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " f.corp_no" + ", c.chi_name" + ", f.corp_p_seqno" + ", f.card_type"
        + ", f.first_fee_amt" + ", f.other_fee_amt" + ", f.apr_date" + ", f.apr_user"
        + ", f.crt_date" + ", f.crt_user" + ", f.mod_time" + ", f.mod_user";

    wp.daoTable = "Ptr_corp_fee f left join crd_corp c on f.corp_no = c.corp_no ";
    wp.whereOrder = " order by f.corp_no";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExCorpNo = wp.itemStr("corp_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExCorpNo = wp.itemStr("kk_corp_no");
    mDaCardType = wp.itemStr("kk_card_type");
    if (empty(mExCorpNo)) {
      mExCorpNo = itemKk("data_k1");
      mDaCardType = itemKk("data_k2");
    }

    wp.selectSQL =
        "hex(f.rowid) as rowid, f.mod_seqno " + ", f.corp_no " + ", c.chi_name" + ", f.corp_p_seqno"
            + ", f.card_type" + ", f.first_fee_amt" + ", f.other_fee_amt" + ", f.apr_date"
            + ", f.apr_user" + ", f.crt_date" + ", f.crt_user" + ", f.mod_time" + ", f.mod_user";
    wp.daoTable = "Ptr_corp_fee f left join crd_corp c on f.corp_no = c.corp_no ";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  f.corp_no = :corp_no ";
    setString("corp_no", mExCorpNo);
    wp.whereStr += " and  f.card_type = :card_type ";
    setString("card_type", mDaCardType);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, corp_no=" + mExCorpNo + "  card_type=" + mDaCardType);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0208Func func = new Ptrm0208Func(wp);

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
        wp.optionKey = wp.itemStr("kk_corp_no");

        wp.initOption = "--";
        wp.optionKey = wp.itemStr("kk_card_type");
        this.dddwList("dddw_card_type", "Ptr_corp_fee", "card_type", "", "group by card_type");

      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_corp_no");
      }


      this.dddwList("dddw_corp_no", "crd_corp", "corp_no", "chi_name", "where 1=1 ");

    } catch (Exception ex) {
    }
  }

}
