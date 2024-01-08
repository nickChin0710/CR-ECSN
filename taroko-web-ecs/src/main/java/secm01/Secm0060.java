/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import ofcapp.BaseAction;

public class Secm0060 extends BaseAction {

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
      strAction = "U";
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
      wp.optionKey = wp.itemStr("ex_group_id");
      this.dddwList("dddw_group_id", "sec_authority", "distinct group_id", "where 1=1");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_group_id")) {
      alertErr2("子系統: 不可空白");
      return;
    }
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.sqlCmd = "select distinct A.wf_winid, B.wf_name"
        + ", (select sort_seqno from sec_authpgm_sort where group_id=A.group_id and wf_winid=A.wf_winid) as sort_seqno"
        + " from sec_authority A left join sec_window B on A.wf_winid=B.wf_winid"
        + " where A.group_id =?" + " order by A.wf_winid";
    setString2(1, wp.itemStr2("ex_group_id"));
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.listCount[0] = sqlRowNum;
    wp.colSet("kk_group_id", wp.itemStr2("ex_group_id"));
    wp.colSet("db_tot_cnt", sqlRowNum);

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
    int llOk = 0, llErr = 0;
    secm01.Secm0060Func func = new secm01.Secm0060Func();
    func.setConn(wp);

    wp.listCount[0] = wp.itemRows("wf_winid");

    if (wp.itemEmpty("kk_group_id")) {
      alertErr2("子系統: 不可空白");
      return;
    }

    int llNrow = wp.listCount[0];

    for (int rr = 0; rr < llNrow; rr++) {

      func.varsSet("wf_winid", wp.itemStr(rr, "wf_winid"));
      func.varsSet("sort_seqno", wp.itemStr(rr, "sort_seqno"));

      rc = func.dbUpdate();
      if (rc != 1) {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }
      llOk++;
      wp.colSet(rr, "ok_flag", "V");
    }

    sqlCommit(1);
    alertMsg("成功:" + llOk + "  失敗:" + llErr);
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void initPage() {}

}
