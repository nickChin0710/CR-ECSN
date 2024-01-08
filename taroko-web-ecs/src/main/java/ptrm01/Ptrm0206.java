/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0206 extends BaseEdit {
  String groupCode = "", cardType = "";

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
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("group_code");
        dddwList("d_dddw_groupcode", "ptr_group_code", "group_code", "group_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("card_type");
        dddwList("d_dddw_cardtype", "ptr_card_type", "card_type", "name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }


  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_group_code"), "group_code", "like%")
        + sqlCol(wp.itemStr("ex_card_type"), "card_type");
    wp.whereOrder = " order by group_code, card_type ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();



  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();
    wp.selectSQL = "A.group_code, " + "A.card_type," + "A.last_ttl_amt, " + "A.first_fee_rc,"
        + "A.other_fee_rc," + "A.first_fee_nrc," + "A.other_fee_nrc," + "mod_user,"
        + "mod_time as mod_date";
    wp.daoTable = "ptr_afee_group A";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
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
    groupCode = wp.itemStr("data_k1");
    cardType = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(groupCode)) {
      groupCode = wp.itemStr("group_code");
    }
    if (empty(cardType)) {
      cardType = wp.itemStr("card_type");
    }
    wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno, " + "A.group_code,   " + "A.card_type, "
        + "A.last_ttl_amt, " + "A.first_fee_rc," + "A.other_fee_rc," + "A.sup_rate_rc,"
        + "A.sup_end_month_rc," + "A.sup_end_rate_rc," + "A.first_fee_nrc," + "A.other_fee_nrc,"
        + "A.sup_rate_nrc," + "A.sup_end_month_nrc," + "A.sup_end_rate_nrc," + "A.crt_user,"
        + "A.crt_date," + "A.apr_user," + "A.apr_date," + "A.apr_flag,"
        /*
         * + "B.first_fee," + "B.other_fee," + "B.sup_rate," + "B.sup_end_month," +
         * "B.sup_end_rate,"
         */ + "A.mod_user," + "to_char(A.mod_time,'yyyymmdd') as mod_date";
    wp.daoTable =
        "ptr_afee_group A left join  ptr_group_card B on A.group_code = B.group_code and A.card_type = B.card_type ";
    wp.whereStr = " where 1=1" + sqlCol(groupCode, "A.group_code") + sqlCol(cardType, "A.card_type");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + groupCode + cardType);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

}
