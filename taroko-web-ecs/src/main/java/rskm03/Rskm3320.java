/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm03;

import ofcapp.BaseAction;

public class Rskm3320 extends BaseAction {

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
  // TODO Auto-generated method stub

}

@Override
public void queryRead() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void querySelect() throws Exception {
  // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
  wp.selectSQL = " mcc_code_flag , " + " pos_flag , " + " country_flag , " + " black_mcht_flag , "
      + " black_card_flag , " + " card_risk_factor , " + " repeat_txn_flag , " + " repeat_factor , "
      + " in_vip_flag , " + " vip_factor , " + " amt_base_flag , " + " txn_amt_base , "
      + " crt_date , " + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , "
      + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_pgm , " + " mod_seqno , "
      + " hex(rowid) as rowid ";
  wp.daoTable = " rsk_factor_parm ";
  wp.whereStr = " where 1=1 ";
  pageSelect();
  if (sqlNotFind()) {
    alertErr2("尚未建立參數 !");
    return;
  }


}

@Override
public void saveFunc() throws Exception {

  if (checkApproveZz() == false)
    return;

  rskm03.Rskm3320Func func = new rskm03.Rskm3320Func();
  func.setConn(wp);

  rc = func.dbSave(strAction);
  sqlCommit(rc);
  if (rc != 1) {
    alertErr2(func.getMsg());
  } else
    saveAfter(false);

  if (isUpdate()) {
    wp.respMesg = "存檔完成";
  } else if (isDelete()) {
    wp.respMesg = "刪除完成";
  }

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
  try {
    dataRead();
  } catch (Exception e) {
  }
}

}
