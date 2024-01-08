/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 109-04-21  V1.00.01  shiyuqi       updated for project coding standard     *                                                                            *
******************************************************************************/

package tscm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Tscq0010 extends BaseEdit {

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
      saveFunc();
      // updateFunc();
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
    String lsDate1 = wp.itemStr("ex_s_yyymm");
    String lsDate2 = wp.itemStr("ex_e_yyymm");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[帳務年月-起迄]  輸入錯誤");
      return false;
    }

    // wp.whereStr = " where acct_month >= :sdate and acct_month <= :edate ";
    // setString("sdate", wp.item_ss("ex_s_yyymm"));
    // setString("edate", wp.item_ss("ex_e_yyymm"));
    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("ex_s_yyymm")) == false) {
      wp.whereStr += " and acct_month >= :sdate";
      setString("sdate", wp.itemStr("ex_s_yyymm"));
    }
    if (empty(wp.itemStr("ex_e_yyymm")) == false) {
      wp.whereStr += " and acct_month <= :edate";
      setString("edate", wp.itemStr("ex_e_yyymm"));
    }

    wp.whereOrder = " ORDER BY acct_month ASC ";

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    // 設定queryRead() SQL條件
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = " acct_month, dest_amt as destination_amt, feedback_amt, '' as mydesc ";

    wp.daoTable = " tsc_stmt_hst ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();

  }

  void listWkdata() throws Exception {
    Double sumAmtA = 0D;
    Double sumAmtB = 0D;
    String myDesc = "自103年7月起本畫面回饋金額計算方式增加條件而以累計交易金額<br>" + "15,000為上限,悠遊卡公司通知108年9月起累計交易金額上限下調<br>"
        + "為12,000之後上限金額如有變動請提需求修改。";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "mydesc", myDesc);

      sumAmtA += wp.colNum(ii, "destination_amt");
      sumAmtB += wp.colNum(ii, "feedback_amt");
    }
    wp.colSet("tol_dest_amt", sumAmtA);
    wp.colSet("tol_feed_amt", sumAmtB);
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {}

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {

  }

}
