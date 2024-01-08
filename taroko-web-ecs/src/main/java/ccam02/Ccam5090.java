
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5090 extends BaseEdit {
Ccam5090Func func;
String respCode;

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
  public void queryFunc() throws Exception {
    wp.whereStr = "where 1=1 " + sqlCol(wp.itemStr("ex_resp_code"), "resp_code", "like%")
        + sqlCol(wp.itemStr("ex_remark"), "resp_remark", "%like%")
        + sqlCol(wp.colStr("ex_p39"), "nccc_p39");
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        " resp_code	," + " resp_remark ," + " nccc_p38 ," + " nccc_p39 ," + " resp_status ,"
            + " visa_p39 ," + " mast_p39 ," + " jcb_p39 ," + " abnorm_flag ," + " mod_user ,"
            + " to_char(mod_time,'yyyymmdd') as mod_date , " + " crt_date , " + " crt_user ";
    wp.daoTable = "cca_resp_code";
    wp.whereOrder = "order by resp_code";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    respCode = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(respCode)) {
      respCode = itemKk("resp_code");
    }
    if (empty(respCode)) {
      alertErr("回覆碼 不可空白");
      return;
    }
    wp.selectSQL = " hex(rowid) as rowid , " + " mod_seqno , " + " resp_code , " + " resp_remark , "
        + " nccc_p38 , " + " nccc_p39 , " + " resp_status , " + " visa_p39 , " + " mast_p39 , "
        + " jcb_p39 , " + " abnorm_flag , " + " crt_date , " + " crt_user , " + " mod_user , "
        + " uf_2ymd(mod_time) as mod_date";
    wp.daoTable = "cca_resp_code";
    wp.whereStr = "where 1=1" + sqlCol(respCode, "resp_code");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + respCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ccam5090Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
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
  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("abnorm_flag", "Y");
    }
  }

}
