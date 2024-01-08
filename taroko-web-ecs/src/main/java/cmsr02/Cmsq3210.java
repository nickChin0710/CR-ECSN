/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-26  V1.00.01  Alex       code -> chinese                            *
* 109-04-27  V1.00.02  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package cmsr02;

import ofcapp.BaseAction;

public class Cmsq3210 extends BaseAction {

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
    try {
      if (eqIgno(wp.respHtml, "cmsq3210")) {
        wp.optionKey = wp.colStr(0, "ex_sec_flag");
        dddwList("dddw_sec_flag", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SMS_SEC_FLAG'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_date1")) && empty(wp.itemStr("ex_date2"))
        && empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_sec_flag"))
        && empty(wp.itemStr("ex_cell_phone")) && empty(wp.itemStr("ex_email_addr"))) {
      alertErr2("請輸入查詢條件");
      return;
    }


    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("發送日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 and proc_date <>'' "
        + sqlCol(wp.itemStr("ex_cell_phone"), "cellar_phone", "like%")
        + sqlCol(wp.itemStr("ex_sec_flag"), "apr_sec_flag")
        + sqlCol(wp.itemStr("ex_date1"), "send_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "send_date", "<=")
        + sqlCol(wp.itemStr("ex_email_addr"), "e_mail_addr", "like%")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        "" + " card_no , " + " vd_flag , " + " decode(vd_flag,'C','信用卡','D','VD卡') as tt_vd_flag , "
            + " e_mail_addr , " + " cellar_phone , " + " apr_sec_flag , " + " send_date , "
            + " send_time , " + wp.sqlID + " uf_TT_idtab('SMS_SEC_FLAG',apr_sec_flag) as tt_xxx ";
    wp.daoTable = " sms_einvo_cancel ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " ORDER BY send_date, send_time ";
    logSql();
    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();


  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
