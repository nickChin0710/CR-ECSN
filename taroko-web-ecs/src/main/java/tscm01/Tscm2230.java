/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1206  V1.00.01  Alex  add initButton
* 109-04-28  V1.00.02  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名             
* 111-04-14  V1.00.05  machao     TSC畫面整合                                                                         *  
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;

public class Tscm2230 extends BaseAction {

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
    wp.selectSQL =
        "hex(rowid) as rowid , mod_seqno," + " parm_type ," + " seq_no ," + " parm_desc ,"
            + " card_lost_cond ," + " card_oppo_cond ," + " mcode_cond ," + " bkec_block_cond ,"
            + " bkec_block_reason ," + " auto_block_cond ," + " auto_block_reason ," + " apr_date ,"
            + " apr_user ," + " payment_rate ," + " mcode_amt ," + " 'Y' as apr_flag ,"
            + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_user "

    ;
    wp.daoTable = "rsk_comm_parm";
    wp.whereStr = " where 1=1 and parm_type ='W_RSKM2230' and seq_no = 1";
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      tscm01.Tscm2230Func func = new tscm01.Tscm2230Func();
      func.setConn(wp);
      rc = func.dbInsert();
      sqlCommit(rc);
      if (rc != 1) {
        errmsg(func.getMsg());
      }
    }
    dataAfter1();
    dataAfter2();
    setBlockReason();
    setBlockReason2();
  }

  void dataAfter1() {
    int blockReasonLength = wp.colStr("bkec_block_reason").length() / 2;
    String rr = "";
    int cnt = 0;
    for (int ii = 0; ii < blockReasonLength; ii++) {
      if (ii == 0) {
        rr += wp.colStr("bkec_block_reason").substring(cnt, cnt + 2);
      } else {
        rr += "," + wp.colStr("bkec_block_reason").substring(cnt, cnt + 2);
      }
      cnt = cnt + 2;
    }
    wp.colSet("ttbkec_block_reason", rr);
  }

  void dataAfter2() {
    int blockReasonLength = wp.colStr("auto_block_reason").length() / 2;
    String rr = "";
    int cnt = 0;
    for (int ii = 0; ii < blockReasonLength; ii++) {
      if (ii == 0) {
        rr += wp.colStr("auto_block_reason").substring(cnt, cnt + 2);
      } else {
        rr += "," + wp.colStr("auto_block_reason").substring(cnt, cnt + 2);
      }
      cnt = cnt + 2;
    }
    wp.colSet("ttauto_block_reason", rr);
  }

  @Override
  public void saveFunc() throws Exception {
    /*
     * String[] ls_block_code = wp.item_buff("block_code"); String[] ls_block_code2 =
     * wp.item_buff("block_code2"); wp.listCount[0] = ls_block_code.length; wp.listCount[1] =
     * ls_block_code2.length;
     */
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    tscm01.Tscm2230Func func = new tscm01.Tscm2230Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    }

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "tscm2230")) {
      btnModeAud("XX");
    }

  }

  @Override
  public void initPage() {
    try {
      dataRead();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  void setBlockReason() {
    int blockReasonLength = wp.colStr("bkec_block_reason").length() / 2;
    wp.log("ss=" + blockReasonLength);
    int cnt = 0;
    for (int ii = 0; ii < blockReasonLength; ii++) {
      String rr = "";
      int num = 0;
      num = ii + 1;
      rr = wp.colStr("bkec_block_reason").substring(cnt, cnt + 2);
      wp.log("rr=" + rr);
      wp.colSet(ii, "block_code", rr);
      cnt = cnt + 2;
      if (num < 10) {
        wp.colSet(ii, "ser_num", "0" + num);
      } else {
        wp.colSet(ii, "ser_num", "" + num);
      }

    }
    wp.listCount[0] = blockReasonLength;
    wp.colSet("ind_num1", "" + blockReasonLength);
    wp.itemSet("ind_num1", "" + blockReasonLength);
    log("A:" + wp.colStr("ind_num1"));
  }

  void setBlockReason2() {
    int blockReasonLength = wp.colStr("auto_block_reason").length() / 2;
    wp.log("ss=" + blockReasonLength);
    int cnt = 0;
    for (int ii = 0; ii < blockReasonLength; ii++) {
      String rr = "";
      int num = 0;
      num = ii + 1;
      rr = wp.colStr("auto_block_reason").substring(cnt, cnt + 2);
      wp.log("rr=" + rr);
      wp.colSet(ii, "block_code2", rr);
      cnt = cnt + 2;
      if (num < 10) {
        wp.colSet(ii, "ser_num", "0" + num);
      } else {
        wp.colSet(ii, "ser_num", "" + num);
      }

    }
    wp.listCount[1] = blockReasonLength;
    wp.colSet("ind_num2", "" + blockReasonLength);
    wp.itemSet("ind_num2", "" + blockReasonLength);
    log("B:" + wp.colStr("ind_num2"));
  }


}
