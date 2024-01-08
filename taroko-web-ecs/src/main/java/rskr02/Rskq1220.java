/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskr02;

import ofcapp.BaseAction;

public class Rskq1220 extends BaseAction {

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
    String lsWhere1 = " where 1=1 " + sqlCol(wp.itemStr("ex_batch_no"), "batch_no");
    
    sqlParm.setSqlParmNoClear(true);
    listSum(lsWhere1);
    
    String lsWhere2 = " where 1=1 " + sqlCol(wp.itemStr("ex_batch_no"), "batch_no");
    
    wp.whereStr = lsWhere1;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void listSum(String lsWhere) {
    String sql1 = " select " + " sum(decode(risk_group1,'',0,1)) as tl_group1_cnt , "
        + " sum(decode(risk_group2,'',0,1)) as tl_group2_cnt " + " from rsk_trcorp_list "
        + lsWhere;

    sqlSelect(sql1);

    wp.colSet("tl_group1_cnt", sqlStr("tl_group1_cnt"));
    wp.colSet("tl_group2_cnt", sqlStr("tl_group2_cnt"));

  }

  @Override
  public void queryRead() throws Exception {
    // wp.pageControl();

    wp.sqlCmd =
        " select batch_no , risk_group , sum(db_cnt1) as db_group1_cnt , sum(db_cnt2) as db_group2_cnt "
            + " from ( " + " select " + " batch_no , " + " risk_group1 as risk_group , "
            + " count(*) as db_cnt1 , " + " 0 as db_cnt2 " + " from rsk_trcorp_list " + wp.whereStr
            + " group by batch_no , risk_group1 " + " union " + " select " + " batch_no , "
            + " risk_group2 as risk_group , " + " 0 as db_cnt1 , " + " count(*) as db_cnt2 "
            + " from rsk_trcorp_list " + wp.whereStr + " group by batch_no , risk_group2 " + " ) "
            + " group by batch_no , risk_group ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    // wp.setPageValue();

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
