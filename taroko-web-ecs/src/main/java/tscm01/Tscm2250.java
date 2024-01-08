/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-06  V1.00.02  Alex       add initButton 									  *
* 108-11-27  V1.00.01  Alex       add checkDup                               *
* 109-04-28  V1.00.03  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名     
* 111-04-14  V1.00.05  machao     TSC畫面整合                                                                                 *  
******************************************************************************/
package tscm01;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Tscm2250 extends BaseAction {

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
    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " parm_type , " + " seq_no , "
        + " parm_desc , " + " bkec_block_cond , " + " bkec_block_reason , " + " auto_block_cond , "
        + " auto_block_reason , " + " apr_date , " + " apr_user , " + " mod_user , "
        + " mod_time , " + " mod_pgm , " + " mod_seqno "

    ;
    wp.daoTable = "rsk_comm_parm";
    wp.whereStr = " where 1=1 and parm_type = 'W_TSCM2250' and seq_no = '10'";
    pageSelect();

    if (sqlNotFind()) {
      tscm01.Tscm2250Func func = new tscm01.Tscm2250Func();
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
    setAutoBlock();
  }

  void dataAfter1() {
    int blockReason = wp.colStr("bkec_block_reason").length() / 2;
    String rr = "";
    int num = 0;
    for (int ii = 0; ii < blockReason; ii++) {
      if (ii == 0) {
        rr += wp.colStr("bkec_block_reason").substring(num, num + 2);
      } else {
        rr += "," + wp.colStr("bkec_block_reason").substring(num, num + 2);
      }
      num = num + 2;
    }
    wp.colSet("ttbkec_block_reason", rr);
  }

  void dataAfter2() {
    int blockReason = wp.colStr("auto_block_reason").length() / 2;
    String rr = "";
    int num = 0;
    for (int ii = 0; ii < blockReason; ii++) {
      if (ii == 0) {
        rr += wp.colStr("auto_block_reason").substring(num, num + 2);
      } else {
        rr += "," + wp.colStr("auto_block_reason").substring(num, num + 2);
      }
      num = num + 2;
    }
    wp.colSet("ttauto_block_reason", rr);
  }

  @Override
  public void saveFunc() throws Exception {

    // --塞值
    setBlockReason();
    setAutoBlock();

    if (checkDup() == false) {
      alertErr2("參數為空值或有重覆值 !");
      return;
    }

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    tscm01.Tscm2250Func func = new tscm01.Tscm2250Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    }

  }

  boolean checkDup() {
   // String aa = "";

    int ii = 0, llErr = 0;
    String[] lsBlockReason = wp.colStr("ttbkec_block_reason").split(",");
    String[] lsAutoBlock = wp.colStr("ttauto_block_reason").split(",");
    // --凍結碼
    if (this.eqIgno(wp.itemNvl("bkec_block_cond", "N"), "Y")) {
      if (wp.itemEmpty("bkec_block_reason"))
        return false;
      ii = -1;
      for (String tmpStr : lsBlockReason) {
        ii++;
        log("A:" + tmpStr);
        if (ii != Arrays.asList(lsBlockReason).indexOf(tmpStr)) {
          wp.colSet(ii, "ok_flag", "!");
          llErr++;
        }
      }
      if (llErr > 0)
        return false;
    }

    if (this.eqIgno(wp.itemNvl("auto_block_cond", "N"), "Y")) {
      if (wp.itemEmpty("auto_block_reason"))
        return false;
      ii = -1;
      for (String tmpStr : lsAutoBlock) {
        ii++;
        if (ii != Arrays.asList(lsAutoBlock).indexOf(tmpStr)) {
          wp.colSet(ii, "ok_flag", "!");
          llErr++;
        }
      }
      if (llErr > 0)
        return false;
    }

    return true;
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    this.btnModeAud("xx");

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
    int blockReason = wp.colStr("bkec_block_reason").length() / 2;
    int num = 0;
    for (int ii = 0; ii < blockReason; ii++) {
      String rr = "";
      int nn = 0;
      nn = ii + 1;
      rr = wp.colStr("bkec_block_reason").substring(num, num + 2);
      wp.colSet(ii, "block_code", rr);
      num = num + 2;
      if (nn < 10) {
        wp.colSet(ii, "ser_num", "0" + nn);
      } else {
        wp.colSet(ii, "ser_num", "" + nn);
      }

    }
    wp.listCount[0] = blockReason;
    wp.colSet("ind_num1", "" + blockReason);
    wp.itemSet("ind_num1", "" + blockReason);

  }

  void setAutoBlock() {
    int blockReason = wp.colStr("auto_block_reason").length() / 2;
    int num = 0;
    for (int ii = 0; ii < blockReason; ii++) {
      String rr = "";
      int nn = 0;
      nn = ii + 1;
      rr = wp.colStr("auto_block_reason").substring(num, num + 2);
      wp.colSet(ii, "auto_block", rr);
      num = num + 2;
      if (nn < 10) {
        wp.colSet(ii, "ser_num", "0" + nn);
      } else {
        wp.colSet(ii, "ser_num", "" + nn);
      }

    }
    wp.listCount[1] = blockReason;
    wp.colSet("ind_num2", "" + blockReason);
    wp.itemSet("ind_num2", "" + blockReason);
  }
}
