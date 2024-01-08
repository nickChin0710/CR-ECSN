/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-11  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ipsm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Ipsm0030 extends BaseEdit {
  Ipsm0030Func func;
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
    wp.colSet("btnUpdate_disable", "disabled");
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.whereStr = " where 1=1 ";
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = " hex(rowid) as rowid " + " ,parm_type " + " ,seq_no " + " ,parm_desc "
        + " ,mcode_cond " + " ,payment_rate " + " ,mcode_amt " + " ,block_cond " + " ,block_codes "
        + " ,stop1_cond " + " ,stop1_days " + " ,stop2_cond " + " ,stop2_days " + " ,stop3_cond "
        + " ,stop3_days " + " ,imp_list_cond " + " ,apr_date " + " ,apr_user " + " ,mod_user "
        + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno ";

    wp.daoTable = " ips_comm_parm ";
    wp.whereOrder = " ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and parm_type='AUTOLOAD_OFF' and seq_no = 1 ";
    pageSelect();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();
    wp.colSet("block_codes_desc", addCommaByEachTwoBytes(wp.colStr("block_codes")));
    wp.colSet("btnUpdate_disable", "");
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ipsm0030Func(wp);
    if (ofValidation() != 1) {
      return;
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();

    }
    this.btnModeAud("XX");
  }

  @Override
  public void dddwSelect() {

  }

  void listWkdata() {
    wp.selectSQL = " hex(rowid) as rowid_black " + " ,stop1_cond as stop1_cond_black "
        + " ,stop1_days as stop1_days_black " + " ,stop2_cond as stop2_cond_black "
        + " ,stop2_days as stop2_days_black " + " ,stop3_cond as stop3_cond_black "
        + " ,stop3_days as stop3_days_black " + " ,mod_seqno as mod_seqno_black ";

    wp.daoTable = " ips_comm_parm ";
    wp.whereOrder = " ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and parm_type='BLACK_LIST' and seq_no =1 ";
    pageSelect();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
  }

  private String addCommaByEachTwoBytes(String data) {
    String buf = "";
    // int cnt = 0;
    for (int i = 0; i < data.length(); i++) {
      if (i % 2 == 0 && i != 0) {
        buf = buf + ",";
        // cnt++;
      }
      // if (i == 0) cnt++;
      buf = buf + data.substring(i, i + 1);
    }

    return buf;
  }

  int ofValidation() {
    // -AUTOOFF_OFF-
    if (wp.itemStr("stop1_cond").equals("Y")) {
      if (wp.itemNum("stop1_days") <= 0) {
        alertErr("自動加值: 停用卡片之天數須>0");
        return -1;
      }
    }
    if (wp.itemStr("stop2_cond").equals("Y")) {
      if (wp.itemNum("stop2_days") <= 0) {
        alertErr("自動加值: 毀損重製之天數須>0");
        return -1;
      }
    }
    if (wp.itemStr("mcode_cond").equals("Y")) {
      if (wp.itemNum("payment_rate") <= 0) {
        alertErr("自動加值: Mcode 須大於 0");
        return -1;
      }
      if (wp.itemNum("mcode_amt") <= 0) {
        alertErr("自動加值: 欠款本金 須大於 0");
        return -1;
      }
    }
    if (wp.itemStr("block_cond").equals("Y")) {
      if (empty(wp.itemStr("block_codes_desc"))) {
        alertErr("[自動加值凍結碼] 不可空白");
        return -1;
      }
    }
    // -黑名單-
    if (wp.itemStr("stop1_cond_black").equals("Y")) {
      if (wp.itemNum("stop1_days_black") <= 0) {
        alertErr("黑名單: 停用卡片之天數須>0");
        return -1;
      }
    }
    if (wp.itemStr("stop2_cond_black").equals("Y")) {
      if (wp.itemNum("stop2_days_black") <= 0) {
        alertErr("黑名單: 毀損重製之天數須>0");
        return -1;
      }
    }
    if (wp.itemStr("stop3_cond_black").equals("Y")) {
      if (wp.itemNum("stop3_days_black") <= 0) {
        alertErr("黑名單: 效期屆期之天數須>0");
        return -1;
      }
    }

    return 1;
  }
}
