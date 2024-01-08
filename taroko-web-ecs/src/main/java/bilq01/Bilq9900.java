/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-10-18  V1.00.00  JeffKung    program initial                           *
* 111-11-01  V1.00.01  JeffKung    modify display format-減項金額以負數表示        *     
******************************************************************************/

package bilq01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilq9900 extends BaseEdit {
  String mExReferenceNo = "";

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
    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no")) ) {
      alertErr("至少輸入一個查詢條件");
      return false;
    }
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_idno")) == false) {
      wp.whereStr += " and  c.major_id_p_seqno = :ex_idno ";
      setString("ex_idno", wp.itemStr("ex_idno"));
    }

    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and  c.card_no = :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no"));
    }

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and  c.billed_date >= :exDateS ";
      setString("exDateS", wp.itemStr("exDateS"));
    }

    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and  c.billed_date <= :exDateE ";
      setString("exDateE", wp.itemStr("exDateE"));
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

    //增加負向金額判斷及顯示
    wp.selectSQL = " c.billed_date, c.purchase_date, c.post_date, c.mcht_chi_name, "; 
    wp.selectSQL+= " c.mcht_city, c.mcht_country, c.curr_code,  ";
    wp.selectSQL+= " decode(c.sign_flag,'-',c.dc_dest_amt*-1,c.dc_dest_amt) dc_dest_amt , ";
    wp.selectSQL+= " decode(c.sign_flag,'-',c.dest_amt*-1,c.dest_amt) dest_amt , ";
    wp.selectSQL+= " c.p_seqno, c.card_no, c.source_curr, ";
    wp.selectSQL+= " decode(c.sign_flag,'-',c.source_amt*-1,c.source_amt) source_amt  ";
    

    wp.daoTable = " cardlink_bil_bill c ";

    wp.whereOrder = " order by c.billed_date, c.purchase_date, c.film_no, c.card_no";
    if (getWhereStr() != true)
      return;

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
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

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
      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_curr_code");
      // this.dddw_list("dddw_curr_code", "ptr_currcode", "curr_code", "curr_chi_name", "where 1=1
      // order by curr_code");
    } catch (Exception ex) {
    }
  }

}
