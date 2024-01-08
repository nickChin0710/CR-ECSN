/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-19  V1.00.00  yash       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                              *
******************************************************************************/

package bilm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0730 extends BaseEdit {
  String mExTxDate = "";

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

  @Override
  public void initPage() {
    String sysdate1 = "";
    sysdate1 = strMid(getSysDate(), 0, 8);
    wp.colSet("ex_tx_dateS", sysdate1);
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where b.card_no=c.card_no and b.apr_date_1 !=''   ";

    if (empty(wp.itemStr("ex_tx_dateS")) == false) {
      wp.whereStr += " and  b.tx_date >= :dateS ";
      setString("dateS", wp.itemStr("ex_tx_dateS"));
    }

    if (empty(wp.itemStr("ex_tx_dateE")) == false) {
      wp.whereStr += " and  b.tx_date <= :dateE ";
      setString("dateE", wp.itemStr("ex_tx_dateE"));
    }

    if (empty(wp.itemStr("ex_close_flag")) == false) {
      if (wp.itemStr("ex_close_flag").equals("Y")) {
        wp.whereStr += " and  b.close_flag = 'Y' ";
      } else if (wp.itemStr("ex_close_flag").equals("N")) {
        wp.whereStr += " and  b.close_flag = 'N' ";
      }
    }

    if (empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr += " and  UF_IDNO_ID(c.id_p_seqno) =  :ex_id  ";
      setString("ex_id", wp.itemStr("ex_id"));
    }

    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and  b.card_no  = :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no"));
    }

    if (empty(wp.itemStr("ex_user")) == false) {
      wp.whereStr += " and  b.crt_user  = :ex_user ";
      setString("ex_user", wp.itemStr("ex_user"));
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

    wp.selectSQL = " b.tx_date" + ", c.acct_type" + ", uf_idno_id(c.id_p_seqno) as idno"
        + ", b.card_no" + ", c.stmt_cycle" + ", b.crt_user" + ", b.trial_status" + ", b.close_flag"
        + ", b.sale_emp_no" + ", b.action_desc" + ", b.error_desc"
        + ", decode(b.trial_status,'1','待覆審','2','覆審中','3','已覆審',b.trial_status) as statusname"
        + ", uf_2ymd(b.mod_time) as mod_date";

    wp.daoTable = "bil_auto_tx b , crd_card c";
    // wp.whereOrder="Fetch First 50 Row Only";
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
    mExTxDate = wp.itemStr("tx_date");
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
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_tx_date");
      this.dddwList("dddw_apUser", "sec_user", "usr_id", "usr_cname", "where 1=1  order by usr_id");
    } catch (Exception ex) {
    }
  }

}
