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

public class Ccam5120 extends BaseEdit {
  Ccam5120Func func;
  String binType = "", regnCode = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_bin_type"), "bin_type")
        + sqlCol(wp.itemStr("ex_regn_code"), "regn_code");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "bin_type,   " + "regn_code, " + "regn_desc, to_char(mod_time,'yyyymmdd') as mod_date , " + "mod_user";
    wp.daoTable = "Vcca_region";
    wp.whereOrder = " order by bin_type , regn_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    binType = wp.itemStr("data_k1");
    regnCode = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(binType)) {
      binType = itemKk("bin_type");
    }

    if (isEmpty(binType)) {
      alertErr("卡別：不可空白");
      return;
    }

    if (empty(regnCode)) {
      regnCode = itemKk("regn_code");
    }

    if (isEmpty(regnCode)) {
      alertErr("Region代碼： 不可空白");
      return;
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "bin_type,   " + "regn_code, "
        + "regn_desc, " + "crt_date," + "mod_user, to_char(mod_time,'yyyymmdd') as mod_date , " + "crt_user ";
    wp.daoTable = "Vcca_region";
    wp.whereStr = "where 1=1" + sqlCol(binType, "bin_type") + sqlCol(regnCode, "regn_code");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + binType + ", " + regnCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ccam5120Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    this.sqlCommit(rc);

    if (rc != 1) {
      alertErr2(func.getMsg());
    }
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
