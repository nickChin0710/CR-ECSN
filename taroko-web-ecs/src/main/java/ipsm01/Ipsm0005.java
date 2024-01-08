/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-2-14  V1.00.01   Ryan        program initial                           * 
******************************************************************************/
package ipsm01;

import ofcapp.BaseAction;

public class Ipsm0005 extends BaseAction {
  // --
  String kkBankNo = "", kkCardKind = "";

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "";

    lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_card_kind"), "card_kind");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " BANK_NO ," + " CARD_KIND ," + " LPAD(SEQ_NO, 6, '0') SEQ_NO," + " SEQ_DIG ," + " CRT_USER ,"
        + " APR_USER" ;

    wp.daoTable = "IPS_CARD_SEQNO";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
    listWkdata(wp.selectCnt);
  }

  @Override
  public void querySelect() throws Exception {
	kkBankNo = wp.itemStr("data_k1");
	kkCardKind = wp.itemStr("data_k2");

    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(kkBankNo))
    	kkBankNo = itemkk("bank_no");
    if (empty(kkCardKind))
    	kkCardKind = itemkk("card_kind");


    if (empty(kkBankNo) || empty(kkCardKind)) {
      alertErr2("讀取條件不可空白");
      return;
    }

    wp.selectSQL = " hex(rowid) as rowid," + " bank_no ," + " card_kind ," + " LPAD(SEQ_NO, 6, '0') SEQ_NO ," 
        + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ," + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " mod_pgm ";

    wp.daoTable = " ips_card_seqno ";
    wp.whereStr = " where 1=1 " + sqlCol(kkBankNo, "bank_no") + sqlCol(kkCardKind, "card_kind");

    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }
    listWkdata(wp.selectCnt);
  }
  
  void listWkdata(int selectCnt) throws Exception {
	  for (int ii = 0; ii < selectCnt; ii++) {
		  wp.colSet(ii,"tt_card_kind", this.commString.decode(wp.colStr(ii,"card_kind"), ",1,2,3,7,9",",DEBIT卡,金融卡,GIFT卡,信用卡,SD卡"));
	  }
  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    ipsm01.Ipsm0005Func func = new ipsm01.Ipsm0005Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);

    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "ipsm0005_detl")) {
       btnModeAud();
       buttonOff("btOther_disable");
    }
  }

  @Override
  public void initPage() {
	if (eqIgno(wp.respHtml, "ipsm0005_detl")) {
       wp.colSet("seq_no", "0");
       wp.colSet("kk_bank_no", "006");
    }

  }

}
