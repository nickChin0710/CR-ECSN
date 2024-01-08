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
import taroko.com.TarokoCommon;

public class Secm0032 extends BaseAction {
  String userLevel = "", winId = "";

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
      if (eqIgno(wp.respHtml, "secm0032")) {
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
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
    userLevel = wp.itemStr("ex_user_level");
    winId = wp.itemStr("ex_winid");

    if (empty(userLevel) || empty(winId)) {
      alertErr2("使用者層級 , 程式代號  不可空白");
      return;
    }

    wp.selectSQL = " group_id , " + " group_name ";
    wp.daoTable = " sec_workgroup ";
    wp.whereOrder = " order by group_id ";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(0);
    selectAuthority();
  }

  void selectAuthority() {
    wp.logSql = false;
    // --select sec_authority
    String sql1 = " select " + " user_level , " + " aut_query , " + " aut_update , "
        + " aut_approve , " + " aut_print " + " from sec_authority " + " where group_id = ? "
        + " and user_level = ? " + " and wf_winid = ? ";
    // --select user_desc
    String sql2 = " select " + " wf_desc as tt_user_level " + " from ptr_sys_idtab "
        + " where wf_type='SEC_USRLVL' " + " and wf_id = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // --此程式有無此功能
      if (wp.itemEq("wf_update", "N")) {
        wp.colSet(ii, "disp_update", "none");
        wp.colSet(ii, "wk_aut_update", "disabled");
      }
      if (wp.itemEq("wf_approve", "N")) {
        wp.colSet(ii, "disp_approve", "none");
        wp.colSet(ii, "wk_aut_approve", "disabled");
      }
      if (wp.itemEq("wf_print", "N")) {
        wp.colSet(ii, "disp_print", "none");
        wp.colSet(ii, "wk_aut_print", "disabled");
      }
      // --
      sqlSelect(sql2, new Object[] {sqlStr("user_level")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "user_level", userLevel);
        wp.colSet(ii, "tt_user_level", sqlStr("tt_user_level"));
      }
      // --
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "group_id"), userLevel, winId});
      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("aut_query"), "Y"))
          wp.colSet(ii, "wk_aut_query", "checked");
        if (eqIgno(sqlStr("aut_update"), "Y"))
          wp.colSet(ii, "wk_aut_update", "checked");
        if (eqIgno(sqlStr("aut_approve"), "Y"))
          wp.colSet(ii, "wk_aut_approve", "checked");
        if (eqIgno(sqlStr("aut_print"), "Y"))
          wp.colSet(ii, "wk_aut_print", "checked");
      }

      if (wp.colEq(ii, "wk_aut_query", "checked") || wp.colEq(ii, "wk_aut_update", "checked")
          || wp.colEq(ii, "wk_aut_approve", "checked") || wp.colEq(ii, "wk_aut_print", "checked")) {
        wp.colSet(ii, "opt_C", "checked");
      }

    }
  }

  @Override
  public void saveFunc() throws Exception {
    int llOk = 0, llErr = 0;
    Secm0032Func func = new Secm0032Func();
    func.setConn(wp);

    String lsUserLevel = wp.itemStr("ex_user_level");
    String lsWinid = wp.itemStr("ex_winid");
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsGroupId = wp.itemBuff("group_id");
    String[] lsAutQuery = wp.itemBuff("aut_query");
    String[] lsAutUpdate = wp.itemBuff("aut_update");
    String[] lsAutApprove = wp.itemBuff("aut_approve");
    String[] lsAutPrint = wp.itemBuff("aut_print");

    wp.listCount[0] = wp.itemRows("group_id");

    if (this.checkApproveZz() == false) {
      return;
    }

    func.varsSet("user_level", lsUserLevel);
    func.varsSet("wf_winid", lsWinid);

    if (func.delAuthority() == -1) {
      errmsg("delete sec_authority error !");
      return;
    }

    for (int ii = 0; ii < wp.itemRows("group_id"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;
      func.varsSet("group_id", lsGroupId[ii]);

      if (checkBoxOptOn(ii, lsAutQuery)) {
        func.varsSet("aut_query", "Y");
      } else {
        func.varsSet("aut_query", "N");
      }

      if (checkBoxOptOn(ii, lsAutUpdate)) {
        func.varsSet("aut_update", "Y");
      } else {
        func.varsSet("aut_update", "N");
      }

      if (checkBoxOptOn(ii, lsAutApprove)) {
        func.varsSet("aut_approve", "Y");
      } else {
        func.varsSet("aut_approve", "N");
      }

      if (checkBoxOptOn(ii, lsAutPrint)) {
        func.varsSet("aut_print", "Y");
      } else {
        func.varsSet("aut_print", "N");
      }

      if (func.insertAuthority() == 1) {
        llOk++;
        sqlCommit(1);
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        llErr++;
        dbRollback();
        wp.colSet(ii, "ok_flag", "X");
        continue;
      }
    }

    alertMsg("存檔完成 , 成功:" + llOk + " 失敗:" + llErr);

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

  void selectSecWindow(String wfWinid) throws Exception {
    wp.sqlCmd = "select wf_name, wf_update, wf_approve, wf_print " + " from sec_window "
//        + " where wf_winid ='" + wfWinid + "'"
    		+ " where 1=1 "+sqlCol(wfWinid,"wf_winid")
        ;
    this.sqlSelect();
    if (sqlRowNum <= 0) {
      alertErr2("查無程式代號: ID=" + wfWinid);
    }
    return;
  }

  public void wfAjaxWinid(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectSecWindow(wp.itemStr("ax_winid"));
    // jjj("ajax: winid="+wp.item_ss("ax_winid"));
    // jjj("wf_name="+sql_ss("wf_name"));
    if (rc != 1) {
      wp.addJSON("wf_name", "");
      wp.addJSON("wf_update", "");
      wp.addJSON("wf_approve", "");
      wp.addJSON("wf_print", "");
      return;
    }
    wp.addJSON("wf_name", sqlStr("wf_name"));
    wp.addJSON("wf_update", sqlStr("wf_update"));
    wp.addJSON("wf_approve", sqlStr("wf_approve"));
    wp.addJSON("wf_print", sqlStr("wf_print"));
  }

}
