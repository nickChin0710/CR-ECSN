/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0070 extends BaseEdit {
  Ptrm0070Func func;

  String cardType = "";
// / String kk2 = "";

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
	sqlParm.clear();
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_card_type")) == false) {
      wp.whereStr += " and  card_type = :card_type";
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
    queryRead();
  }



  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " card_type, " + " name, " + "card_note," + " rds_pcard," + " card_note_jcic,"
        + " neg_card_type," + " out_going_type," + " sort_type," + " crt_date, " + " crt_user, "
        + " mod_time, " + " mod_user ";

    wp.daoTable = "ptr_card_type";
    wp.whereOrder = " order by card_type";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    cardType = wp.itemStr("card_type");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    cardType = wp.itemStr("KK_card_type");
    if (empty(cardType)) {
      cardType = itemKk("data_k1");

    }

    if (empty(cardType)) {
      cardType = wp.colStr("card_type");

    }

    // if (isEmpty(wp.item_ss("bin_no"))){
    // alert_err("BIN NO : 不可空白");
    // return;
    // }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + " card_type, " + "name, " + "card_note,"
        + " rds_pcard," + " card_note_jcic," + " neg_card_type," + " out_going_type,"
        + " sort_type," + " crt_date," + " crt_user," + " uf_2ymd(mod_time) as mod_date,"
        + " mod_user";
    wp.daoTable = "ptr_card_type";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  card_type = :card_type ";
    setString("card_type", cardType);


    pageSelect();
    wp.optionKey = wp.colStr("bin_no");
    if (sqlNotFind()) {
      alertErr("查無資料, card_type=" + cardType);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    func = new Ptrm0070Func(wp);


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
        wp.optionKey = wp.colStr("card_note");
        this.dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='CARD_NOTE'");
      }

    } catch (Exception ex) {
    }
  }

}
