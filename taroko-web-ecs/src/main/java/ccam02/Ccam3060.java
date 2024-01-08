package ccam02;
/*
 * 檢核ID特店資料維護作業  mcht_checkid
 * V00.1    Alex  2019-1202: init_button fix 
 * V00.0		XX		2017-0804: initial
 * V00.02   yanghan  2020-0420: 修改了變量名稱和方法名稱
 * */

import ofcapp.BaseAction;

public class Ccam3060 extends BaseAction {
  Ccam3060Func func;
  String bankId = "", voiceId = "";

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
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1  " + sqlCol(wp.itemStr("ex_voice_id"), "voice_id", "like%");
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "voice_id," + " acq_bank_id , " + " crt_date," + " crt_user";
    wp.daoTable = "cca_voice ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by voice_id ";
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    bankId = wp.itemStr("data_k1");
    voiceId = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(bankId)) {
      bankId = itemkk("acq_bank_id");
    }
    if (empty(voiceId)) {
      voiceId = itemkk("voice_id");
    }
    wp.selectSQL = "voice_id," + " acq_bank_id , " + " crt_date," + " crt_user,"
        + " uf_cca_mcht_name(voice_id) as mcht_name," + " hex(rowid) as rowid , mod_seqno , "
        + " apr_date , " + " apr_user"

    ;
    wp.daoTable = "cca_voice";
    wp.whereStr = " where 1=1" + sqlCol(bankId, "acq_bank_id") + sqlCol(voiceId, "voice_id");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + bankId);
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {

    if (this.checkApproveZz() == false)
      return;

    func = new Ccam3060Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    this.saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "ccam3060_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {

  }

}
