/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;

public class Ichm0060 extends BaseAction {

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

    wp.selectSQL = "" + " hex(rowid) as rowid ," + " parm_type ," + " seq_no ," + " parm_desc ,"
        + " block_cond ," + " block_reason ," + " spec_cond , " + " spec_status , " + " crt_date ,"
        + " crt_user ," + " apr_date ," + " apr_user ," + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " mod_pgm ," + " mod_seqno ";

    wp.daoTable = "ich_00_parm";
    wp.whereStr = " where 1=1 " + " and parm_type = 'ICHM0060' " + " and seq_no = 0 ";

    pageSelect();

    if (sqlRowNum <= 0) {
      wp.listCount[0] = 0;
      wp.colSet("ind_num1", "" + 0);
      wp.listCount[1] = 0;
      wp.colSet("ind_num2", "" + 0);
      errmsg("查無資料");
      return;
    }

    setBlockReason();
    setSpecStatuc();
  }

  void setBlockReason() {
    int reasonLength = 0;

    if (wp.colStr("block_reason").length() % 3 != 0) {
      reasonLength = wp.colStr("block_reason").length() / 3 + 1;
    } else {
      reasonLength = wp.colStr("block_reason").length() / 3;
    }

    int count = 0;
    for (int ii = 0; ii < reasonLength; ii++) {
      String rr = "";
      int num = 0;
      num = ii + 1;
      rr = commString.mid(wp.colStr("block_reason"), count, 2);
      // rr=wp.col_ss("block_reason").substring(tt, tt+2);
      wp.log("rr=" + rr);
      wp.colSet(ii, "block_code", rr);
      count = count + 3;
      if (num <= 9) {
        wp.colSet(ii, "ser_num", "0" + num);
      } else {
        wp.colSet(ii, "ser_num", "" + num);
      }

    }
    wp.listCount[0] = reasonLength;
    wp.colSet("ind_num1", "" + reasonLength);
  }

  void setSpecStatuc() {
    int statuslength = 0;

    if (wp.colStr("spec_status").length() % 3 != 0) {
      statuslength = wp.colStr("spec_status").length() / 3 + 1;
    } else {
      statuslength = wp.colStr("spec_status").length() / 3;
    }

    int count = 0;
    for (int ii = 0; ii < statuslength; ii++) {
      String rr = "";
      int num = 0;
      num = ii + 1;
      rr = commString.mid(wp.colStr("spec_status"), count, 2);
      // rr=wp.col_ss("block_reason").substring(tt, tt+2);
      wp.log("rr=" + rr);
      wp.colSet(ii, "spec_code", rr);
      count = count + 3;
      if (num <= 9) {
        wp.colSet(ii, "ser_num", "0" + num);
      } else {
        wp.colSet(ii, "ser_num", "" + num);
      }

    }
    wp.listCount[1] = statuslength;
    wp.colSet("ind_num2", "" + statuslength);
  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    ichm01.Ichm0060Func func = new ichm01.Ichm0060Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);

    if (rc <= 0) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    btnModeAud();


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

}
