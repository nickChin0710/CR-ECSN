package secm01;
/**
 * 2019-1205   JH    安控
 * 2019-1016   JH    異動碼.update
 109-04-20   shiyuqi       updated for project coding standard  
 * */
import ofcapp.BaseAction;

public class Secp2062 extends BaseAction {

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
      if (eqIgno(wp.respHtml, "secp2062")) {
        wp.optionKey = wp.colStr(0, "ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1");
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
        wp.optionKey = wp.colStr(0, "ex_pkg_name");
        dddwList("dddw_pkg_name", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC-PKG-NAME'");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEmpty("ex_winid") && wp.itemEmpty("ex_pkg_name")) {
      alertErr2("程式名稱、程式目錄不可皆為空白");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_winid"), "wf_winid", "like%")
        + sqlCol(wp.itemStr2("ex_pkg_name"), "pkg_name");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.colSet("kk_group_id", wp.itemStr("ex_group_id"));
    wp.colSet("kk_user_level", wp.itemStr("ex_user_level"));
    wp.selectSQL = "" + " wf_winid , " + " wf_name , " + " wf_update , " + " wf_approve , "
        + " decode(wf_update,'Y','','none') as disp_update , "
        + " decode(wf_approve,'Y','','none') as disp_approve , "
        + " '' as mod_audcode, 'none' as disp_cancel," + " '' as rowid  ";

    wp.daoTable = "sec_window";
    wp.whereOrder = " order by wf_winid ";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() {
    String lsGroupId = "", lsUserLevel = "";
    int ilWpRows = 0;
    lsGroupId = wp.colStr("kk_group_id");
    lsUserLevel = wp.colStr("kk_user_level");
    ilWpRows = wp.selectCnt;
    // ddd("ii:"+il_wp_rows);
    // --先讀sec_authority_log的未覆核資料，若讀不到再去讀取sec_authority的權限
    // String sql1 = " select "
    // + " 'checked' as opt , "
    // + " decode(aut_query,'Y','checked','') as query_on , "
    // + " decode(aut_update,'Y','checked','') as update_on , "
    // + " decode(aut_approve,'Y','checked','') as approve_on , "
    // + " mod_audcode , "
    // + " hex(rowid) as rowid "
    // + " from sec_authority_log "
    // + " where wf_winid = ? and group_id = ? and user_level = ? and apr_flag <>'Y' " ;
    // String sql2 = " select "
    // + " decode(aut_query,'Y','checked','') as query_on , "
    // + " decode(aut_update,'Y','checked','') as update_on , "
    // + " decode(aut_approve,'Y','checked','') as approve_on "
    // + " from sec_authority where wf_winid = ? and group_id = ? and user_level = ? " ;
    String sql1 = "select 'checked' as opt ," + " decode(aut_query,'Y','checked','') as query_on ,"
        + " decode(aut_update,'Y','checked','') as update_on ,"
        + " decode(aut_approve,'Y','checked','') as approve_on ," + " mod_audcode ,"
        + " hex(rowid) as rowid" + " from sec_authority_log"
        + " where wf_winid =? and group_id =? and user_level =? and apr_flag <>'Y'"
        + " union select " + " '' as opt," + " decode(aut_query,'Y','checked','') as query_on ,"
        + " decode(aut_update,'Y','checked','') as update_on ,"
        + " decode(aut_approve,'Y','checked','') as approve_on," + " '' as mod_audcode,"
        + " '' as rowid" + " from sec_authority"
        + " where wf_winid =? and group_id =? and user_level =?" + " order by opt desc"
        + commSqlStr.rownum(1);

    for (int ii = 0; ii < ilWpRows; ii++) {

      String lsWinid = wp.colStr(ii, "wf_winid");
      sqlSelect(sql1, new Object[] {lsWinid, lsGroupId, lsUserLevel, lsWinid, lsGroupId,
          lsUserLevel});

      if (sqlRowNum > 0) {
        wp.colSet(ii, "opt_on", sqlStr("opt"));
        wp.colSet(ii, "query_on", sqlStr("query_on"));
        wp.colSet(ii, "update_on", sqlStr("update_on"));
        wp.colSet(ii, "approve_on", sqlStr("approve_on"));
        wp.colSet(ii, "mod_audcode", sqlStr("mod_audcode"));
        wp.colSet(ii, "rowid", sqlStr("rowid"));
        if (!empty(sqlStr("mod_audcode"))) {
          wp.colSet(ii, "disp_cancel", "");
        }
      }
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
    // -un-approve delete--
    int ilErr = 0;
    secm01.Secp2062Func func = new secm01.Secp2062Func();
    func.setConn(wp);

    wp.listCount[0] = wp.itemRows("rowid");
    keepOpt(wp.listCount[0]);

    int liOk = 0;
    for (int ii = 0; ii < wp.listCount[0]; ii++) {
      String lsRowid = wp.itemStr(ii, "rowid");
      if (empty(lsRowid))
        continue;

      optOkflag(ii);
      rc = 1;
      func.varsSet("rowid", lsRowid);
      rc = func.deleteLog();
      optOkflag(ii, rc);
      if (rc == -1)
        ilErr++;
      else
        liOk++;
    }
    sqlCommit(1);
    alertMsg("待覆核刪除筆數: 成功=" + liOk + " 失敗=" + ilErr);
  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;
    secm01.Secp2062Func func = new secm01.Secp2062Func();
    func.setConn(wp);

    String lsGroupId = wp.itemStr2("kk_group_id");
    String lsUserLevel = wp.itemStr2("kk_user_level");
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsWfWinid = wp.itemBuff("wf_winid");
    String[] lsAutQuery = wp.itemBuff("aut_query");
    String[] lsAutUpdate = wp.itemBuff("aut_update");
    String[] lsAutApprove = wp.itemBuff("aut_approve");
    String[] laAutCancel = wp.itemBuff("aut_cancel");
    String[] lsRowid = wp.itemBuff("rowid");

    wp.listCount[0] = wp.itemRows("wf_winid");

    keepOpt(wp.listCount[0]);

    func.varsSet("group_id", lsGroupId);
    func.varsSet("user_level", lsUserLevel);

    for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;

      // --塞值進vars_ss
      String lsAudcode = wp.itemStr(rr, "mod_audcode");
      func.varsSet("wf_winid", lsWfWinid[rr]);
      boolean lbRun = checkBoxOptOn(rr, lsAutQuery);
      boolean lbUpd = checkBoxOptOn(rr, lsAutUpdate);
      boolean lbApr = checkBoxOptOn(rr, lsAutApprove);
      boolean lbCancel = checkBoxOptOn(rr, laAutCancel);

      if (lbRun || lbUpd || lbApr) {
        func.varsSet("mod_audcode", "A");
      } else {
        func.varsSet("mod_audcode", "D");
      }

      func.varsSet("aut_query", (lbRun ? "Y" : "N"));
      func.varsSet("aut_update", (lbUpd ? "Y" : "N"));
      func.varsSet("aut_approve", (lbApr ? "Y" : "N"));

      rc = 1;
      if (empty(lsRowid[rr])) {
        rc = func.insertLog();
      } else {
        func.varsSet("rowid", lsRowid[rr]);
        if (lbCancel) {
          rc = func.deleteLog();
        } else {
          rc = func.updateLog();
        }
      }

      if (rc <= 0) {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        dbRollback();
        continue;
      } else {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
        sqlCommit(1);
        continue;
      }
    }

    alertMsg("資料存檔完成 , 成功:" + llOk + " 失敗:" + llErr);

  }

  void keepOpt(int ilCnt) {
    optNumKeep(ilCnt);
    optNumKeep(ilCnt, "aut_update", "update_on");
    optNumKeep(ilCnt, "aut_query", "query_on");
    optNumKeep(ilCnt, "aut_approve", "approve_on");
    optNumKeep(ilCnt, "aut_cancel", "cancel_on");
  }

  @Override
  public void initButton() {
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
