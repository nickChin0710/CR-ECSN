/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-28   V1.00.02 Justin         parameterize sql
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;

public class Ichq0100 extends BaseAction {

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
    if (chkStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) == false) {
      alertErr2("通知日期:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 "
        + sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "notify_date")
        + sqlCol(wp.itemStr2("ex_file_iden"), "file_iden")
        + sqlCol(wp.itemStr2("ex_tran_type"), "tran_type");

    if (wp.itemEq("ex_ok_flag", "1")) {
      lsWhere += " and record_fail = 0 ";
    } else if (wp.itemEq("ex_ok_flag", "2")) {
      lsWhere += " and record_fail > 0 ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " file_iden ," + " file_name ," + " tran_type ," + " notify_date ,"
        + " resp_code ," + " record_cnt ," + " record_fail ," + " proc_flag ," + " check_code ,"
        + " perform_flag ";

    wp.daoTable = " ich_notify_log ";
    wp.whereOrder = " order by notify_date Asc , file_iden Asc ";

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
