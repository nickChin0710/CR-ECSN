/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard  
* 111-04-14  V1.00.01  yangqinkai   TSC畫面整合                               *
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;

public class Tscr0050 extends BaseAction {

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
  // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
  if (chkStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) == false) {
    alertErr2("處理日期起訖錯誤");
    return;
  }

  String lsWhere = "";

  lsWhere = " where 1=1 and tran_code in ('7209','7229') "
      + sqlCol(wp.itemStr2("ex_date1"), "crt_date", ">=")
      + sqlCol(wp.itemStr2("ex_date2"), "crt_date", "<=")
      + sqlCol(wp.itemStr2("ex_resp_code"), "tscc_resp_code");

  if (wp.itemEq("ex_resp_flag", "1")) {
    lsWhere += " and tscc_resp_code = '0000' ";
  } else if (wp.itemEq("ex_resp_flag", "2")) {
    lsWhere += " and tscc_resp_code <> '0000' ";
  }

  wp.whereStr = lsWhere;
  wp.queryWhere = wp.whereStr;
  wp.setQueryMode();

  queryRead();

}

@Override
public void queryRead() throws Exception {
  wp.pageControl();

  wp.selectSQL = " * ";
  wp.daoTable = "tsc_eccb_log";
  wp.whereOrder = " order by tscc_resp_code Asc , crt_date Asc ";

  pageQuery();

  if (sqlNotFind()) {
    alertErr2("此條件查無資料");
    return;
  }

  wp.setListCount(0);
  wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void saveFunc() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void initButton() {
  // TODO Auto-generated method stub

}

@Override
public void initPage() {
  // TODO Auto-generated method stub

}

}
