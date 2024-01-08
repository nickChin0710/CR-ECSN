
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱								 *
* 109-06-01  V1.01  Alex     remove nccc_ftp_code                            *
******************************************************************************/
package ccam02;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;



public class Ccam5020 extends BaseEdit {
	Ccam5020Func func;
	
	String mccCode;
	@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      wp.colSet("risk_factor", "0");
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
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("risk_type");
        dddwList("ddw_risk_type", "Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_mcc_code"), "mcc_code", "like%")
        + sqlCol(wp.itemStr("ex_risk_type"), "risk_type");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        " mcc_code ," + " risk_type ," + " mcc_remark ," + " amount_rule ," 
            + " risk_factor ," + " mod_user ," + " to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "cca_mcc_risk";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by mcc_code";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    // wp.ddd("tot="+wp.totalRows+",data="+wp.dataCnt+", select="+wp.selectCnt);
    // list_wkdata();
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mccCode = wp.itemStr("data_K1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mccCode)) {
      mccCode = itemKk("mcc_code");
    }

    if (isEmpty(mccCode)) {
      alertErr("MCC Code 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "mcc_code ," + "risk_type ,"
        + "mcc_remark ," + "amount_rule ," + " risk_factor ," + "crt_user ,"
        + "crt_date ," + "mod_user ," + "uf_2ymd(mod_time) as mod_date ";
    wp.daoTable = "cca_mcc_risk";
    wp.whereStr = "where 1=1" + sqlCol(mccCode, "mcc_code");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + mccCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // if (!check_approve(wp.item_ss("approval_user"),wp.item_ss("approval_passwd"))) {
    // return;
    // }

    if (checkApproveZz() == false)
      return;

    func = new ccam02.Ccam5020Func(wp);

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
  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("amount_rule", "P");
    }
  }

}
