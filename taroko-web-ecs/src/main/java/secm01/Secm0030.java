/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*                                                                            *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import ofcapp.BaseAction;

public class Secm0030 extends BaseAction {

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
      wp.optionKey = wp.itemStr("ex_group_id");
      this.dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_winid"), "wf_winid", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.colSet("kk_group_id", wp.itemStr("ex_group_id"));

    wp.selectSQL = " wf_winid , " + " wf_name , " + " decode(wf_update,'Y','Y','') as wf_update , "
        + " decode(wf_approve,'Y','Y','') as wf_approve , "
        + " decode(wf_print,'Y','Y','') as wf_print ";
    wp.daoTable = " sec_window ";
    pageQuery();

    if (sqlRowNum <= 0) {
      wp.colSet("db_cnt", "0");
      alertErr2("此條件查無資料");
      return;
    }
    wp.colSet("db_cnt", wp.selectCnt);
    queryAfter();
    wp.setListCount(0);
    wp.setPageValue();

  }

  void queryAfter() {
    wp.logSql = false;
    String sql1 =
        " select " + " count(*) as db_cnt " + " from sec_group_win " + " where wf_winid = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "wf_winid")});

      if (sqlRowNum <= 0 || sqlNum("db_cnt") <= 0)
        continue;
      wp.colSet(ii, "group_data", "checked");

    }
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
    Secm0030Func func = new Secm0030Func();
    func.setConn(wp);
    String lsGroupId = wp.itemStr("kk_group_id");
    String[] lsWfWinid = wp.itemBuff("wf_winid");
    String[] liSeqNo = wp.itemBuff("seq_no");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("wf_winid");

    func.varsSet("group_id", lsGroupId);
    for (int rr = 0; rr < wp.itemRows("wf_winid"); rr++) {


      func.varsSet("wf_winid", lsWfWinid[rr]);
      func.varsSet("seq_no", liSeqNo[rr]);

      if (func.delGroup() == -1) {
        this.dbRollback();
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }
      if (!checkBoxOptOn(rr, aaOpt))
        continue;
      if (func.insertGroup() == 1) {
        sqlCommit(1);
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
        continue;
      } else {
        this.dbRollback();
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }
    }

    alertMsg("成功:" + llOk + "  失敗:" + llErr);

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
