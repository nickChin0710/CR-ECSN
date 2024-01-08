/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package ccam02;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5370 extends BaseEdit {
Ccam5370Func func;
String dataSource = "", mccCode = "", acqId = "", mchtNo = "";

@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    rc = 1;
    wp.logActive();

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
    wp.whereStr = " where 1=1"
    		+sqlCol(wp.itemStr("ex_data_source"),"data_source")
    		+sqlCol(wp.itemStr("ex_mcc_code"),"mcc_code","like%")
    		+sqlCol(wp.itemStr("ex_acq_id"),"acq_id")
    		+sqlCol(wp.itemStr("ex_mcht_no"),"mcht_no","like%")
    		;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "data_source,   " + "mcc_code, " + "acq_id," + "mcht_no,"
        + "to_char(mod_time,'yyyymmdd') as mod_date," + "mod_user," + "crt_date," + "crt_user";
    wp.daoTable = "cca_auth_active";
    wp.whereOrder = " order by data_source , mcc_code , acq_id , mcht_no";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "tt_data_source", dataSource(wp.colStr(ii, "data_source")));
    }
  }

  String dataSource(String decode) {
    String[] code = {"A", "B", "C"};
    String[] text = {"IN HOUSE", "CardPool", "NCCC"};
    return commString.decode(decode, code, text);
  }

  @Override
  public void querySelect() throws Exception {
    dataSource = wp.itemStr("data_k1");
    mccCode = wp.itemStr("data_k2");
    acqId = wp.itemStr("data_k3");
    mchtNo = wp.itemStr("data_k4");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(dataSource)) {
      dataSource = itemKk("data_source");
    }
    if (empty(mccCode)) {
      mccCode = itemKk("mcc_code");
    }
    if (empty(acqId)) {
      acqId = itemKk("acq_id");
    }
    if (empty(mchtNo)) {
      mchtNo = itemKk("mcht_no");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "data_source,   " + "mcc_code, "
        + "acq_id, " + "mcht_no," + "crt_date," + "mod_user, " + this.sqlModDate + ", crt_user";
    wp.daoTable = "cca_auth_active";
    wp.whereStr = "where 1=1" + sqlCol(dataSource, "data_source") + sqlCol(mccCode, "mcc_code")
        + sqlCol(acqId, "acq_id") + sqlCol(mchtNo, "mcht_no");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataSource + ", " + mccCode + "," + acqId + "," + mchtNo);
      return;
    }
    wp.colSet("tt_data_source", dataSource(wp.colStr("data_source")));
  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    func = new ccam02.Ccam5370Func(wp);
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

}
