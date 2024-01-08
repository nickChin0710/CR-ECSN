package secm01;
/**
 * 2019-1210   JH    run(pgm,user)
 * 2019-1205   JH    安控
 109-04-20   shiyuqi       updated for project coding standard  
 * */
import ofcapp.BaseAction;

public class Secp1010 extends BaseAction {

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
      if (eqIgno(wp.respHtml, "secp1010")) {
        wp.optionKey = wp.colStr(0, "ex_pgm");
        dddwList("dddw_pgm", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'W_CALLBATCH01' and upper(wf_id) not between 'MKTA570' and 'MKTA590'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_date1"))) {
      alertErr2("啟動日期 (起) : 不可空白　!");
      return;
    }

    if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("啟動日期 : 起迄輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_pgm"), "program_code", "like%")
        + sqlCol(wp.itemStr("ex_user"), "user_id", "like%")
        + sqlCol(wp.itemStr("ex_date1"), "start_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "start_date", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " * ";
    wp.daoTable = " ptr_callbatch ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
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
    if (empty(wp.itemStr("ex_pgm"))) {
      alertErr2("程式代碼 : 不可空白");
      return;
    }

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    rc = batch.callBatch(wp.itemStr("ex_pgm") + " " + wp.itemStr("ex_parms"), wp.loginUser);
    if (rc != 1) {
      alertErr2("callBatch error; " + batch.getMesg());
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }
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
