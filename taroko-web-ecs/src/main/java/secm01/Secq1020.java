package secm01;
/** 待覆核事項查詢
 * 2019-1213   JH    安控
 *109-04-20   shiyuqi       updated for project coding standard  
 * */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Secq1020 extends BaseProc {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    try {
      queryFunc();
      wp.respCode = "00";
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE sql_table<>'' and sql_where <>''"
        + sqlCol(wp.itemStr("ex_winid"), "wf_winid", "like%")
        + sqlCol(wp.itemStr("ex_pgm_name"), "wf_name", "like%")
        + " and wf_winid in (select A.wf_winid from sec_authority A join sec_user B"
        + " on A.user_level=B.usr_level" + " where locate(lower(A.group_id),lower(B.usr_group)) >0"
        + sqlCol(wp.loginUser, "B.usr_id") + " )";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " uf_secq1020(sql_table,sql_where) as db_tot_cnt" + ", wf_winid, wf_name";

    wp.whereOrder = " order by 1 desc, wf_winid";
    wp.daoTable = "sec_window";
    // if (!empty(wp.whereStr)) {
    // wp.pageCount_sql ="select count(*) from sec_window "+wp.whereStr;
    // }

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    queryAfter(wp.listCount[0]);
    wp.setPageValue();
  }

  void queryAfter(int llNrow) throws Exception {
    String sql1 =
        "SELECT decode(max(A.aut_update),'Y','1','0')||decode(max(A.aut_approve),'Y','5','0')||'0' as db_auth"
            + " FROM sec_authority A JOIN sec_user B ON A.user_level = B.usr_level"
            + " WHERE  locate(lower(A.group_id), lower(B.usr_group)) > 0"
            + " AND B.usr_id=? and A.wf_winid=?";

    for (int ii = 0; ii <= llNrow; ii++) {
      String lsWinid = wp.colStr(ii, "wf_winid");
      wp.colSet(ii, "wk_aut_data", "000");
      if (empty(lsWinid)) {
        continue;
      }
      sqlSelect(sql1, new Object[] {wp.loginUser, lsWinid});
      if (llNrow <= 0)
        continue;
      wp.colSet(ii, "wk_aut_data", sqlStr("db_auth"));
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
  public void dataProcess() throws Exception {
    // TODO Auto-generated method stub

  }

}
