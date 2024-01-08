package cmsm02;
/** 19-0614:   JH    p_xxx >>acno_pxxx
 *  110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
 * */
import ofcapp.BaseAction;

public class Cmsm3150 extends BaseAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

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
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsm3150")) {
        wp.optionKey = wp.colStr(0, "docu_code");
        dddwList("dddw_docu_code", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='VOICE_LIST'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(wp.itemStr("kk_card_no"))) {
      alertErr2("卡號: 不可空白");
      return;
    }

    wp.sqlCmd = " select " + " card_no , " + " 'N' as debit_flag , " + " acno_p_seqno , p_seqno, "
        + " id_p_seqno , " + " major_card_no , " + " current_code " + " from crd_card "
        + " where 1=1 " + sqlCol(wp.itemStr("kk_card_no"), "card_no") + " union " + " select "
        + " card_no , " + " 'Y' as debit_flag , " + " p_seqno as acno_p_seqno, p_seqno, "
        + " id_p_seqno , " + " major_card_no , " + " current_code " + " from dbc_card "
        + " where 1=1 " + sqlCol(wp.itemStr("kk_card_no"), "card_no");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("卡號 輸入錯誤 or 查無卡片資");
      return;
    }

    if (!eqIgno(wp.colStr("current_code"), "0")) {
      errmsg("此卡非正常卡，請確認 !");
      return;
    }
    if (eqIgno(wp.colStr("debit_flag"), "Y")) {
      selectDbc();
    } else {
      selectCrd();
    }
  }

  void selectDbc() {
    String sql1 = "select sex, chi_name, id_no " + " from dbc_idno " + " where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("id_no", sqlStr("id_no"));
  }

  void selectCrd() {
    String sql1 = "select sex, chi_name, id_no " + " from crd_idno " + " where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("id_no", sqlStr("id_no"));
  }

  @Override
  public void saveFunc() throws Exception {
    cmsm02.Cmsm3150Func func = new cmsm02.Cmsm3150Func();
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
    if (eqIgno(wp.respHtml, "cmsm3150")) {
      if (empty(wp.colStr("card_no"))) {
        this.btnOnAud(false, false, false);
      }
    }

  }

  @Override
  public void initPage() {
    wp.colSet("crt_date", commDate.sysDate());
    wp.colSet("crt_time", commDate.sysTime());

  }

}
