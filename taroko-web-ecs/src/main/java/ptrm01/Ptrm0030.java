/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei     updated for project coding standard        *
* 109-12-31  V1.00.03  shiyuqi    修改无意义命名                             *    
* 112-12-13  V1.00.04  Simon      新增時檢核繳款類別不可為空白               *    
******************************************************************************/
package ptrm01;

import ofcapp.*;

public class Ptrm0030 extends BaseAction {
Ptrm0030Func func;
String paymentType = "";

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
  }
}

@Override
public void queryFunc() throws Exception {
  wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_payment_type"), "payment_type", "like%")
      + sqlCol(wp.itemStr2("ex_fund_flag"), "fund_flag")
      + sqlCol(wp.itemStr2("ex_bill_flag"), "bill_flag");

  wp.queryWhere = wp.whereStr;
  wp.setQueryMode();

  queryRead();

}

@Override
public void queryRead() throws Exception {
  wp.pageControl();
  wp.selectSQL = "payment_type, " + "chi_name," + "bill_desc, " + "pay_note, " + " fund_flag, "
      + " bill_flag ";
  wp.daoTable = "ptr_payment";
  // if (empty(wp.whereStr)) {
  // wp.whereStr = " ORDER BY 1";
  // }
  wp.whereOrder = " order by payment_type ";
  pageQuery();

  wp.setListCount(1);
  if (sqlNotFind()) {
    alertErr(appMsg.errCondNodata);
    return;
  }

  // wp.totalRows = wp.dataCnt;
  wp.listCount[1] = wp.dataCnt;
  wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
  paymentType = wp.itemStr("data_k1");
  dataRead();
}

@Override
public void dataRead() throws Exception {
  if (empty(paymentType)) {
    paymentType = itemkk("payment_type");
  }
  wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "payment_type,   " + "chi_name, "
      + "bill_desc, " + "pay_note," + "bill_flag," + "fund_flag," + "crt_date," + "crt_user,"
      + "mod_user," + sqlModDate;
  wp.daoTable = "ptr_payment";
  wp.whereStr = " where 1=1" + sqlCol(paymentType, "payment_type");

  pageSelect();
  if (sqlNotFind()) {
    alertErr("查無資料, key=" + paymentType);
    return;
  }
}

@Override
public void saveFunc() throws Exception {
	if (pageCheck()!=1)
		return;

  func = new ptrm01.Ptrm0030Func();
  func.setConn(wp);

  if (checkApproveZz() == false)
    return;

  rc = func.dbSave(strAction);
  if (rc != 1) {
    alertErr2(func.getMsg());
  }
  this.sqlCommit(rc);
  this.saveAfter(false);
}

int pageCheck() throws Exception {
	if (isAdd()) {
		if (wp.itemEmpty("kk_payment_type")) {
			alertErr2("繳款類別：不可空白");
		}
	}

	return rc;
}

@Override
public void initButton() {
  if (wp.respHtml.indexOf("_detl") > 0) {
    this.btnModeAud();
  }
}

@Override
public void procFunc() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void initPage() {}

@Override
public void dddwSelect() {
  // TODO Auto-generated method stub

}
}
