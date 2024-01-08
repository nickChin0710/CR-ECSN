/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-05  V1.00.00  yash           program initial                        *
* 108-10-04  V1.00.01  Alex           page bug fixed                         *
* 109-02-05  V1.00.02  Zuwei         add condition for dddw_nccc_card        *
* 109-032-13  V1.00.03  Zuwei       rollback  add condition for dddw_nccc_card
* 109-04-20  V1.00.04  Tanwei       updated for project coding standard      *
* 112-01-02  V1.00.05  Wilson       不檢核CRD_CARD_ITEM                        *
******************************************************************************/

package ptrm01;

import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0200 extends BaseEdit {
  String exGroupCode = "";
  String exCardType = "";

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
    } else if (eqIgno(wp.buttonCode, "U2")) {
      /* 更新功能 */
      strAction = "U2";
      updateNcccCode();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      /* 更新功能 */
      strAction = "R2";
      readNcccData();
      // update_nccc_code();
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

  void showScreenOfDetl() {
    // -set new-
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  private boolean getWhereStr() throws Exception {
	sqlParm.clear(); 
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_group_code")) == false) {
      wp.whereStr += " and  group_code = :group_code ";
      setString("group_code", wp.itemStr("ex_group_code"));
    }
    if (empty(wp.itemStr("ex_card_type")) == false) {
      wp.whereStr += " and  card_type = :card_type ";
      setString("card_type", wp.itemStr("ex_card_type"));
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

    wp.selectSQL =
        " group_code" + ", card_type" + ", name" + ", org_cardno_flag" + ", service_type";

    wp.daoTable = "Ptr_group_card";
    wp.whereOrder = " order by group_code";
    getWhereStr();
    pageQuery();

    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setListCount(1);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    exGroupCode = wp.itemStr("group_code");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    exGroupCode = wp.itemStr("kk_group_code");
    exCardType = wp.itemStr("kk_card_type");
    if (empty(exGroupCode)) {
      exGroupCode = itemKk("data_k1");
    }
    if (empty(exCardType)) {
      exCardType = itemKk("data_k2");
    }

    if (empty(exGroupCode)) {
      exGroupCode = wp.colStr("group_code");
    }
    if (empty(exCardType)) {
      exCardType = wp.colStr("card_type");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", group_code " + ", card_type" + ", name"
        + ", card_mold_flag" + ", service_type" + ", org_cardno_flag" + ", remark"
        + ", cash_limit_rate * 100 as db_cash_limit_rate" + ", crt_date" + ", crt_user";
    wp.daoTable = "Ptr_group_card";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  group_code = :group_code ";
    setString("group_code", exGroupCode);
    wp.whereStr += " and  card_type = :card_type ";
    setString("card_type", exCardType);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, group_code=" + exGroupCode);
    }

    String sql = "select * from ptr_group_card_dtl where group_code= ? and card_type = ?";
    Object[] param = new Object[] {exGroupCode, exCardType};
    this.sqlSelect(sql, param);
    if (this.sqlNotFind()) {
      wp.alertMesg = "<script language='javascript'> alert('認同集團碼尚未新增!')</script>";
    }

  }

  /**
   * 裝載明細數據
   * 
   * @throws Exception
   */
  public void readNcccData() throws Exception {
    this.selectNoLimit();
    exGroupCode = wp.itemStr("group_code");
    exCardType = wp.itemStr("card_type");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", group_code " + ", card_type"
    // + ", seqno"
        + ", unit_code" + ", unit_code || card_type as card_item" + ", mod_seqno" + ", mod_user";
    wp.daoTable = "ptr_group_card_dtl";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  group_code = :group_code ";
    setString("group_code", exGroupCode);
    wp.whereStr += " and  card_type = :card_type ";
    setString("card_type", exCardType);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0200Func func = new Ptrm0200Func(wp);

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
    if (wp.respHtml.indexOf("_nccc") > 0) {
      this.btnModeAud();
      showScreenOfDetl();
    }

  }

  /**
   * 更新明細數據，先刪除后新增
   * 
   * @throws Exception
   */
  public void updateNcccCode() throws Exception {
    Ptrm0200Func func = new Ptrm0200Func(wp);
    int okCount = 0, errCount = 0;

    String[] unitCodes = wp.itemBuff("unit_code");
    String[] cardItems = wp.itemBuff("card_item");
    // String[] aa_seqno = wp.item_buff("seqno");
    String[] opts = wp.itemBuff("opt");
    wp.listCount[0] = unitCodes.length;
    wp.colSet("IND_NUM", "" + unitCodes.length);
    // -check duplication-
    for (int ll = 0; ll < unitCodes.length; ll++) {
      wp.colSet(ll, "ok_flag", "");

      if (checkBoxOptOn(ll, opts)) {
        continue;
      }

      if (ll != Arrays.asList(unitCodes).indexOf(unitCodes[ll])) {
        wp.colSet(ll, "ok_flag", "!");
        errCount++;
        continue;
      }

    }

    if (errCount > 0) {
      alertErr("資料值重複 : " + errCount);
      return;
    }

    // 2020/02/05 Zuwei 檢核"卡樣代碼"是否已存在CRD_CARD_ITEM
//    String notExitsItems = "";
//    for (int ll = 0; ll < cardItems.length; ll++) {
//      String lsSql =
//          "select card_item " + "from crd_card_item " + "where crd_card_item.card_item=:cardItem ";
//      this.setString("cardItem", cardItems[ll]);
//      sqlSelect(lsSql);
//      if (sqlRowNum > 0) {
//        continue;
//      } else {
//        notExitsItems += "，" + cardItems[ll];
//      }
//    }
//    if (notExitsItems.length() > 0) {
//      notExitsItems = notExitsItems.substring(1);
//      alertErr("卡樣代碼【" + notExitsItems + "】不存在於CRD_CARD_ITEM中");
//      return;
//    }

    // -delete no-approve-
    if (func.dbDelete2() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    for (int ll = 0; ll < unitCodes.length; ll++) {

      // -option-ON-
      if (checkBoxOptOn(ll, opts)) {
        continue;
      }

      // func.vars_set("seqno", aa_seqno[ll]);
      func.varsSet("unit_code", unitCodes[ll]);
      // func.vars_set("card_item", aa_item[ll]);

      if (func.dbInsert2() == 1) {
        okCount++;

      } else {
        errCount++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(okCount > 0 ? 1 : 0);
    }

    alertMsg("資料存檔處理完成; OK = " + okCount + ", ERR = " + errCount);
    // SAVE後 SELECT
    readNcccData();

  }

  @Override
  public void dddwSelect() {
    try {

      if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nccc") > 0)) {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("kk_group_code");
        this.dddwList("dddw_group_code", "Ptr_group_code", "group_code", "",
            "where 1=1 group by group_code order by group_code");
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("kk_card_type");
        this.dddwList("dddw_card_type", "Ptr_card_type", "card_type", "",
            "where 1=1 group by card_type order by card_type");
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("kt_crd_item_unit");
        this.dddwList("dddw_nccc_card", "crd_item_unit ", "unit_code", "","");
//            "where  card_type='" + wp.itemStr("card_type") + "'");

      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_group_code");
        this.dddwList("dddw_group_code", "Ptr_group_code", "group_code", "",
            "where 1=1 group by group_code order by group_code");
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_card_type");
        this.dddwList("dddw_card_type", "Ptr_card_type", "card_type", "",
            "where 1=1 group by card_type order by card_type");

      }

    } catch (Exception ex) {
    }
  }

}
