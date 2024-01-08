/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱								 *
* 109-06-04  V1.01  Alex     remove nccc_pickup								 *
* 111-10-25  V1.02  Ryan     讀取資料table Vcca_exception 改為 cca_sys_parm1
******************************************************************************/
package ccam02;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5110 extends BaseEdit {
  Ccam5110Func func;
  String binType = "", excCode = "";

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
	  
  }

  //111-10-25  V1.02  Ryan     讀取資料table Vcca_exception 改為 cca_sys_parm1
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 and sys_id in ('NCCC','VISA','MAST','JCB','FISC')" 
    		+ sqlCol(wp.itemStr("ex_bin_type"), "SYS_ID")
    		+ sqlCol(wp.itemStr("ex_exc_code"), "sys_key");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }
  
  //111-10-25  V1.02  Ryan     讀取資料table Vcca_exception 改為 cca_sys_parm1
  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "SYS_ID as bin_type,   " + "sys_key as exc_code, " + "sys_data1 as exc_desc,"
        + "uf_2ymd(mod_time) as mod_date," + " mod_user";
    wp.daoTable = "cca_sys_parm1";
    wp.whereOrder = " order by SYS_ID , sys_key";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    binType = wp.itemStr("data_k1");
    excCode = wp.itemStr("data_k2");
    dataRead();
  }
  
  //111-10-25  V1.02  Ryan     讀取資料table Vcca_exception 改為 cca_sys_parm1
  @Override
  public void dataRead() throws Exception {
    if (empty(binType)) {
      binType = itemKk("bin_type");
    }

    if (isEmpty(binType)) {
      alertErr("卡別：不可空白");
      return;
    }

    if (empty(excCode)) {
      excCode = itemKk("exc_code");
    }

    if (isEmpty(excCode)) {
      alertErr("原因碼： 不可空白");
      return;
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "sys_id as bin_type,   " + "sys_key as exc_code, " + "sys_data1 as exc_desc, "
        + "mod_user, " + "uf_2ymd(mod_time) as mod_date "
        + ", crt_user, crt_date";
    wp.daoTable = "cca_sys_parm1";
    wp.whereStr = "where 1=1" + sqlCol(binType, "sys_id") + sqlCol(excCode, "sys_key");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + binType + ", " + excCode);
      return;
    }

    if (!wp.colEq("bin_type", "FISC")) {
      wp.colSet("close_radio", "disabled");
    } else {
      wp.colSet("close_radio", "");
    }

  }

  @Override
  public void saveFunc() throws Exception {
    func = new ccam02.Ccam5110Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (isUpdate()) {
      dataRead();
    }

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
