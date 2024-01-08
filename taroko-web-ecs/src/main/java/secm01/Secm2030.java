package secm01;
/**
 * 2019-0813   JH    modify
 *109-04-20 shiyuqi       updated for project coding standard     *
 * */
import ofcapp.BaseAction;

public class Secm2030 extends BaseAction {
  String wfPgm = "", wfHtml = "";

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr2("ex_wf_pgm"), "wf_pgm", "like%")
    // +sql_col(wp.sss("ex_wf_html"),"wf_html","like%")
    ;

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " wf_pgm , " + " wf_html , " + " wf_column , " + " wf_action as data_type , "
        + " debit_flag , " + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date ";

    wp.daoTable = " sec_idno_data ";
    wp.whereOrder = " order by 1 ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    wfPgm = wp.itemStr2("data_k1");
    wfHtml = wp.itemStr2("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(wfPgm))
      wfPgm = itemkk("wf_pgm");
    if (empty(wfHtml))
      wfHtml = itemkk("wf_html");
    if (empty(wfHtml))
      wfHtml = wfPgm;
    if (empty(wfPgm)) {
      alertErr2("程式代碼:不可空白");
      return;
    }

    if (empty(wfHtml)) {
      alertErr2("記錄頁面:不可空白");
      return;
    }

    wp.selectSQL = " wf_pgm , " + " wf_html , " + " wf_column , " + " wf_action as data_type , "
        + " debit_flag , " + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date , "
        + " crt_user , " + " crt_date , " + " mod_pgm , " + " mod_seqno , "
        + " hex(rowid) as rowid ";

    wp.daoTable = " sec_idno_data ";
    wp.whereStr = " where 1=1 " + sqlCol(wfPgm, "wf_pgm") + sqlCol(wfHtml, "wf_html");
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {

    secm01.Secm2030Func func = new secm01.Secm2030Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "secm2030_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
