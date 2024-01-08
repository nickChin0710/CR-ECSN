package secm01;
/**
 * 2019-1204   JH    類別 VS 功能
 *  2019-1202   Alex  add ex_pkg dddw
 *  2019-1018   JH    ++批次作業
 *  2019-0417:  jh    PKG_NAME: dddw(wf_id_wf_desc)
 * 109-04-20   shiyuqi       updated for project coding standard
 * 2020-05-22  Yanghan     add table  a new field (sec_window---db4Dr_flag)
 * */

import taroko.com.ManualBean;
import taroko.com.TarokoCommon;

public class Secm0012 extends ofcapp.BaseEdit {

  String winID;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      // wp.initFlag ="Y";
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 新增功能 */
      if (itemIsempty("rowid")) {
        strAction = "A";
        insertFunc();
      } else {
        /* 更新功能 */
        strAction = "U";
        updateFunc();
      }
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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

  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "secm0012")) {
        wp.optionKey = wp.colStr("ex_pkg_name");
        dddwList("dddw_ex_pkg_name", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC-PKG-NAME'");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("pkg_name");
        dddwList("dddw_pkg_name", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC-PKG-NAME'");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // wp.whereStr = "WHERE 1=1"
    // + sql_col(wp.item_ss("ex_winid"), "wf_winid", "like%")
    // + sql_col(wp.item_ss("ex_pkg_name"), "pkg_name", "like%")
    // ;
    wp.whereStr = "WHERE 1=1";
    if (!wp.itemEmpty("ex_winid")) {
      wp.whereStr += " and wf_winid like :ex_winid ";
    }
    if (!wp.itemEmpty("ex_pkg_name")) {
      wp.whereStr += " and pkg_name like :ex_pkg_name ";
    }

    wp.whereOrder = " order by wf_winid";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        "" + "wf_winid, " + "wf_name, " + "wf_proctype, " + " wf_update, " + " wf_approve, "
        // + " wf_print, "
            + " req_html, " + " pgm_id, " + " pkg_name, " + " method_name ";

    wp.daoTable = "sec_window";
    // if (empty(wp.whereStr)) {
    // wp.whereStr = " ORDER BY 1";
    // }
    wp.whereOrder = " order by wf_winid";

    setString("ex_winid", wp.itemStr("ex_winid") + "%");
    setString("ex_pkg_name", wp.itemStr("ex_pkg_name") + "%");
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    // wp.totalRows = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    winID = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(winID)) {
      winID = itemKk("win_id");
    }
    if (isEmpty(winID)) {
      alertErr2("作業代碼: 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, nvl(mod_seqno,0) as mod_seqno, " + "wf_winid as win_id, "
        + " wf_name, " + " wf_proctype, " + " wf_update, wf_approve, wf_name as ori_wf_name , "
        // + " wf_print, "
        // +" wf_insert, wf_update, wf_delete, wf_approve, wf_print, "
        + " req_html, " + " pgm_id, method_name, pkg_name, " + " report_header1, report_header2, "
        +"db4Dr_flag as wf_db4Dr_flag, "
        + " report_footer1, report_footer2,  " + " pgm_host, pgm_url, " + " sql_table, sql_where, "
        + wp.sqlID + "uf_nvl(menu_flag,'Y') as menu_flag ";
    wp.daoTable = "sec_window";
    wp.whereStr = "where 1=1" + sqlCol(winID, "wf_winid");

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key=" + winID);
      return;
    }
  }

  void dataCheck() {
    winID = itemKk("win_id");
    if (empty(winID)) {
      alertErr2("作業代碼: 不可空白");
      return;
    }
    if (isAdd() == false) {
      if (isOtherModify("sec_window", "where wf_winid =? and nvl(mod_seqno,0)=?",
          new Object[] {winID, wp.itemNum("mod_seqno")})) {
        alertErr2(appMsg.otherModify);
        return;
      }
    }
    if (isDelete()) {
      return;
    }

    if (itemIsempty("pgm_id")) {
      alertErr("程式代碼: 不可空白");
      return;
    }
    boolean lbBatch = itemEq("menu_flag", "B");
    if (lbBatch) {
      wp.itemSet("pkg_name", "zbatch");
      wp.itemSet("req_html", "");
      wp.itemSet("method_name", "");
      wp.itemSet("wf_proctype", "P");
      wp.itemSet2("wf_update", "N");
      wp.itemSet2("wf_approve", "N");
      return;
    }


    if (wp.itemEmpty("req_html") || wp.itemEmpty("pgm_id") || wp.itemEmpty("pkg_name")) {
      alertErr2("顯示畫面, 程式代碼, 程式目錄: 不可空白");
      return;
    }
    // -OK-
    if (wp.itemEmpty("method_name")) {
      wp.itemSet2("method_name", "showScreen");
    }

    // -jh-191204-
    // <option value="M" ${wf_proctype-M}>維護</option>
    // <option value="P" ${wf_proctype-P}>處理</option>
    // <option value="Q" ${wf_proctype-Q}>查詢</option>
    // <option value="R" ${wf_proctype-R}>報表</option>
    // <option value="C" ${wf_proctype-C}>主管覆核</option>
    String lsType = wp.itemStr2("wf_proctype");
    boolean lbUpdate = wp.itemEq("wf_update", "Y");
    boolean lbAppr = wp.itemEq("wf_approve", "Y");
    if (eqIgno(lsType, "M")) {
      if (lbUpdate == false) {
        alertErr("類別=[維護], 須點選 維護功能");
        return;
      }
    } else if (commString.strIn2(lsType, ",P,Q,R,C")) {
      if (lbUpdate) {
        alertErr("類別: 不是[維護], 不可點選 維護功能");
        return;
      }
    }
    if (eqIgno(lsType, "C") && lbAppr) {
      alertErr("類別: 是[主管覆核]; 不可點選 維護,覆核功能");
      return;
    }
  }

  @Override
  public void insertFunc() throws Exception {
    dataCheck();
    if (rc == -1)
      return;

    String lsSql = "insert into sec_window (" + " wf_winid, " // 1
        + " wf_name, " + " wf_proctype, " + " wf_insert, " + " wf_update, " // 5
        + " wf_delete, " + " wf_approve, " + " req_html, " + " pgm_id, " // 10
        + " method_name, " // 11
        + " pkg_name, " + " report_header1, " + " report_header2, " + " report_footer1, " // 15
        + " report_footer2, " // 16
        + " pgm_host, pgm_url, " // 18
        + " sql_table, sql_where, " // 20
        + " menu_flag, " + " mod_user, mod_time, mod_pgm, mod_seqno ," + "db4Dr_flag"+" ) values ("
        + " :wf_winid, " // 1
        + " :wf_name, " + " :wf_proctype, " + " :wf_insert, " + " :wf_update, " // 5
        + " :wf_delete, " + " :wf_approve, "
        // + " :wf_print, "
        + " :req_html, " + " :pgm_id, " // 10
        + " :method_name, " // 11
        + " :pkg_name, " + " :report_header1, " + " :report_header2, " + " :report_footer1, " // 15
        + " :report_footer2, " // 16
        + " :pgm_host, " + " :pgm_url, " // 18
        + " :sql_table, " + " :sql_where, " // 20
        + " :menu_flag, " + " :mod_user, " + " sysdate, " + " :mod_pgm, " + " 1 ," + ":db4Dr_flag"+" )";
    // -setParm-
    setString("wf_winid", winID);
    item2ParmStr("wf_name");
    item2ParmStr("wf_proctype");
    setString("wf_insert", "N");
    item2ParmNvl("wf_update", "N");
    setString("wf_delete", "N");
    item2ParmNvl("wf_approve", "N");
    // item2Parm_nvl("wf_print", "N");
    item2ParmStr("req_html");
    item2ParmStr("pgm_id");
    item2ParmStr("method_name");
    item2ParmStr("pkg_name");
    item2ParmStr("report_header1");
    item2ParmStr("report_header2");
    item2ParmStr("report_footer1");
    item2ParmStr("report_footer2");
    item2ParmStr("pgm_host");
    item2ParmStr("pgm_url");
    item2ParmStr("sql_table");
    item2ParmStr("sql_where");
    item2ParmNvl("menu_flag", "Y");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("db4Dr_flag", wp.itemStr("wf_db4Dr_flag").equals("Y")?"Y":" "); //add  wf_db4Dr_flag

    this.sqlExec(lsSql);
    if (sqlRowNum != 1) {
      alertErr2("insert: " + this.sqlErrtext);
    }

    sqlCommit(rc);
    if (rc == 1) {
      clearFunc();
      ManualBean mBean = ManualBean.getInstance();
  	  mBean.setReloadMode(true);
    }
  }

  @Override
  public void updateFunc() throws Exception {
    dataCheck();
    if (rc == -1)
      return;

    String lsSql =
        "update sec_window set " + " wf_name =:wf_name, " + " wf_proctype =:wf_proctype, "
            + " wf_update =:wf_update , " + " wf_approve =:wf_approve, "
            // + " wf_print =:wf_print, "
            + " req_html =:req_html, " + " pgm_id =:pgm_id, " + " method_name =:method_name, "
            + " pkg_name =:pkg_name, " + " report_header1 =:report_header1, "
            + " report_header2 =:report_header2, " + " report_footer1 =:report_footer1, "
            + " report_footer2 =:report_footer2, " + " pgm_host =:pgm_host, "
            +"db4Dr_flag=:db4Dr_flag,"
            + " pgm_url =:pgm_url, " + " mod_user =:mod_user, mod_time=sysdate, mod_pgm=:mod_pgm,"
            + " mod_seqno =nvl(mod_seqno,0)+1, " + " sql_table =:sql_table, "
            + " sql_where =:sql_where, " + " menu_flag =:menu_flag " + " where 1=1"
            + " and wf_winid =:kk1 " + " and mod_seqno =:mod_seqno ";

    item2ParmStr("wf_name");
    item2ParmStr("wf_proctype");
    item2ParmNvl("wf_update", "N");
    item2ParmNvl("wf_approve", "N");
    // item2Parm_nvl("wf_print", "N");
    item2ParmStr("req_html");
    item2ParmStr("pgm_id");
    item2ParmStr("method_name");
    item2ParmStr("pkg_name");
    item2ParmStr("report_header1");
    item2ParmStr("report_header2");
    item2ParmStr("report_footer1");
    item2ParmStr("report_footer2");
    item2ParmStr("pgm_host");
    item2ParmStr("pgm_url");
    setString("mod_user", wp.loginUser);
    item2ParmStr("mod_pgm");
    item2ParmStr("sql_table");
    item2ParmStr("sql_where");
    item2ParmNvl("menu_flag", "Y");
    setString("kk1", winID);
    item2ParmNum("mod_seqno");
    setString("db4Dr_flag",wp.itemStr("wf_db4Dr_flag").equals("Y")?"Y":" ");
    // wp.ddd("user=" + wp.loginUser + ", mod_pgm=" + wp.mod_pgm());
    this.sqlExec(lsSql);
    if (sqlRowNum != 1) {
      alertErr2("update: SEC_WINDOW error; " + this.sqlErrtext);
    }

    sqlCommit(rc);
    this.modSeqnoAdd();
    
    if(wp.itemEq("wf_name", wp.itemStr("ori_wf_name"))==false) {
    	ManualBean mBean = ManualBean.getInstance();
    	mBean.setReloadMode(true);
    }
    
  }

  @Override
  public void deleteFunc() throws Exception {
    dataCheck();
    if (rc == -1)
      return;

    String lsSql = "delete sec_window " + " where 1=1" + " and wf_winid =? " + " and mod_seqno =? ";
    this.sqlExec(lsSql, new Object[] {winID, wp.modSeqno()});
    if (sqlCode == -1 || sqlRowNum != 1) {
      alertErr2("delete sec_window error; " + this.sqlErrtext);
    }
    sqlCommit(rc);
    if (rc == 1) {
      clearFunc();
    }
  }

  @Override
  public void saveFunc() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      // btnMode_aud(); //rowid
      this.btnModeUd();
    }
  }

  @Override
  public void initPage() {
    if (posAny(wp.respHtml, "_detl") > 0) {
      // wp.col_set("proc_code", "UNIT");
      wp.colSet("method_name", "showScreen");
      // wp.col_set("menu_flag", "Y");
    }
  }

}
