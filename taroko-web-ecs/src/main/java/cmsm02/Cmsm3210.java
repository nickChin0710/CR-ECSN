package cmsm02;
/** 19-0614:   JH    p_xxx >>acno_p_xxx
 *	 19-1126:   Alex  code -> chinese , dataRead can't empty
 *  19-1230:   Alex  fix acct_key2
 ** 109-04-27   shiyuqi       updated for project coding standard     *  
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名    
* 111-11-04  V1.00.03   Machao        頁面bug調整                                                                                  *  
 * */
import ofcapp.BaseAction;

public class Cmsm3210 extends BaseAction {
  String cardNo = "";

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
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      strAction = "D";
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
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("建立日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_crt_user"), "crt_user");
    if (!empty(wp.itemStr("ex_idno"))) {
      lsWhere +=
          " and id_p_seqno = " + wp.sqlID + " uf_idno_pseqno('" + wp.itemStr("ex_idno") + "') ";
    }


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " crt_date ," + " card_no ," + " vd_flag ,"
        + " decode(vd_flag,'C','信用卡','D','VD金融卡') as tt_vd_flag ," + " id_p_seqno ,"
        + " e_mail_addr ," + " cellar_phone ," + " acct_type ,"
        + " uf_acno_key2(card_no,'') as acct_key ";
    wp.daoTable = "sms_einvo_cancel";
    wp.whereOrder = " order by 1,2 ";
    logSql();
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "_" + wp.colStr(ii, "acct_key"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo)) {
      cardNo = itemkk("card_no");
    }

    if (empty(cardNo)) {
      alertErr2("卡號:不可空白");
      return;
    }

    wp.selectSQL =
        " hex(A.rowid) as rowid , " + " A.* , " + " to_char(A.mod_time,'yyyymmdd') as mod_date ,"
            + " uf_acno_key2(card_no,'') as acct_key ,"
            + " decode(vd_flag,'C','信用卡','D','VD金融卡') as tt_vd_flag ";
    wp.daoTable = " sms_einvo_cancel A ";
    wp.whereStr = " where 1=1 " + sqlCol(cardNo, "A.card_no");
    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    cmsm02.Cmsm3210Func func = new cmsm02.Cmsm3210Func();
    func.setConn(wp);
    if (func.getDataCard(cardNo) <= 0) {
      errmsg(func.getMsg());
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    cmsm02.Cmsm3210Func func = new cmsm02.Cmsm3210Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
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
    if (eqIgno(wp.respHtml, "cmsm3210_detl")) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
