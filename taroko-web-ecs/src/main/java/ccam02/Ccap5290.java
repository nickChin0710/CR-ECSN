/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import ofcapp.BaseAction;

public class Ccap5290 extends BaseAction {

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
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 and apr_flag<>'Y' "
        + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")
        + sqlCol(wp.itemStr("ex_acq_id"), "acq_id") + sqlCol(wp.itemStr("ex_mod_user"), "crt_user");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " acq_id , " + " mcht_no , " + " mcht_name , " + " online_date , "
        + " stop_date ," + " crt_user , " + " crt_date ";
    wp.daoTable = "cca_mcht_notonline";
    wp.whereOrder = " order by acq_id, mcht_no ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
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
    Ccap5290Func func = new Ccap5290Func();
    func.setConn(wp);

    int isOk = 0;
    int isError = 0;
    int count = 0;
    String[] acqIdArray = wp.itemBuff("acq_id");
    String[] mchtNoArray = wp.itemBuff("mcht_no");

    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = acqIdArray.length;

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0) {
        continue;
      }
      count++;
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("acq_id", acqIdArray[rr]);
      func.varsSet("mcht_no", mchtNoArray[rr]);


      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        wp.colSet(rr, "ok_flag", "V");
        isOk++;
        continue;
      }
      isError++;
      wp.colSet(rr, "ok_flag", "X");
    }

    if (count == 0) {
      alertErr2("請勾選要覆核的資料");
      return;
    }

    alertMsg("覆核處理: 成功筆數=" + isOk + "; 失敗筆數=" + isError);

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
