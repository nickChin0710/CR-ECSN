/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Secm1010 extends BaseEdit {
  Secm1010Func func;
  String bultDtime = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    this.msgOK();

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_crt_user"), "crt_user")
        + sqlCol(wp.itemStr("ex_eff_date"), "eff_date1", "<=")
        + sqlCol(wp.itemStr("ex_eff_date"), "eff_date2", ">=");
    wp.whereOrder = " order by eff_date1 ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "bult_dtime, " + "bult_subject," + "eff_date1, " + "eff_date2," + "crt_date," + "crt_user";
    wp.daoTable = "SEC_BULLETIN";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    bultDtime = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(bultDtime)) {
      bultDtime = wp.itemStr("bult_dtime");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "bult_dtime,   " + "bult_subject, "
        + "bult_mesg, " + "eff_date1," + "eff_date2," + "crt_date," + "crt_user," + "mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_date," + "mod_pgm," + "mod_seqno";
    wp.daoTable = "SEC_BULLETIN";
    wp.whereStr = " where 1=1" + sqlCol(bultDtime, "bult_dtime");
    // this.sql_ddd();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + bultDtime);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Secm1010Func(wp);
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
