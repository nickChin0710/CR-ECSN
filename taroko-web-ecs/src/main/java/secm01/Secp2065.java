package secm01;
/**
 * 2019-1210   JH    安控
 * 2019-1205-2   JH    安控
 *  2019-0429:  JH    modify
   2018-0828:	JH		modify
109-04-20  shiyuqi       updated for project coding standard   
 * 
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Secp2065 extends BaseAction {
  String winid = "", userLevel = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxWind();
        break;
      default:
        break;
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "secp2065")) {
        wp.optionKey = wp.itemStr2("ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
        wp.optionKey = wp.itemStr2("ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where apr_date<>''");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    winid = wp.itemStr2("ex_winid");
    userLevel = wp.itemStr2("ex_user_level");
    wp.colSet("kk_winid", winid);
    wp.colSet("kk_user_level", userLevel);

    selectSecWindow(winid);
    if (rc != 1) {
      alertErr("查無程式代碼, kk[%s]", winid);
      return;
    }

    String lsUpdate = sqlStr("wf_update");
    String lsAppr = sqlStr("wf_approve");

    wp.colSet("ex_win_name", sqlStr("wf_name"));
    wp.colSet("wf_update", lsUpdate);
    wp.colSet("wf_approve", lsAppr);

    // if ( !eq_igno(ls_update,"Y"))
    // wp.col_set("update_disable","disabled");
    // if ( !eq_igno(ls_appr,"Y"))
    // wp.col_set("approve_disable","disabled");
    // if ( !eq_igno(sql_ss("wf_print"),"Y"))
    // wp.col_set("print_disable","disabled");

    // wp.col_set("wf_print",sql_ss("wf_print"));

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.sqlCmd = "select A.*" + ", decode(B.wf_update,'Y','','disabled') as disp_update"
        + ", decode(B.wf_approve,'Y','','disabled') as disp_approve"
        // +", decode(B.wf_print,'Y','','none') as disp_print"
        + ", 'checked' as opt_on" + ", decode(A.aut_query,'Y','checked','') as on_query"
        + ", decode(A.aut_update,'Y','checked','') as on_update"
        + ", decode(A.aut_approve,'Y','checked','') as on_approve"
        // +", decode(A.aut_print,'Y','checked','disabled') as print_on"
        + ", (select C.group_name from sec_workgroup C where C.group_id = A.group_id) as tt_group_id "
        + " from sec_authority A join sec_window B on A.wf_winid =B.wf_winid" + " where 1=1"
        + " and A.wf_winid =? and A.user_level =?" + " order by A.group_id";
    setString2(1, winid);
    setString(userLevel);

    pageQuery();
    wp.setListCount(1);
    wp.colSet("IND_NUM", sqlRowNum);
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
    int rr = wp.itemRows("group_id");
    wp.listCount[0] = rr;
    if (rr <= 0) {
      alertErr2("輸入完後, 請點選[新增明細]");
      return;
    }

    this.optNumKeep(rr, "opt", "opt_on");
    this.optNumKeep(rr, "aut_query", "on_query");
    this.optNumKeep(rr, "aut_update", "on_update");
    this.optNumKeep(rr, "aut_approve", "on_approve");
    // this.opt_numKeep(rr, "aut_print","print_on");
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    procFunc();

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;
    int ii = 0;
    Secp2065Func func = new Secp2065Func();
    func.setConn(wp);
    String[] lsGroupId = wp.itemBuff("group_id");
    String[] lsAutQuery = wp.itemBuff("aut_query");
    String[] lsAutUpdate = wp.itemBuff("aut_update");
    String[] lsAutApprove = wp.itemBuff("aut_approve");
    // String[] ls_aut_print = wp.item_buff("aut_print");
    String[] aaOpt = wp.itemBuff("opt");
    func.varsSet("wf_winid", wp.itemStr("ex_winid"));
    func.varsSet("user_level", wp.itemStr("ex_user_level"));
    for (int rr = 0; rr < wp.itemRows("group_id"); rr++) {
      if (checkBoxOptOn(rr, aaOpt) == false)
        continue;

      func.varsSet("group_id", lsGroupId[rr]);
      if (this.checkBoxOptOn(rr, lsAutQuery)) {
        func.varsSet("aut_query", "Y");
      } else {
        func.varsSet("aut_query", "N");
      }

      if (this.checkBoxOptOn(rr, lsAutUpdate)) {
        func.varsSet("aut_update", "Y");
      } else {
        func.varsSet("aut_update", "N");
      }

      if (this.checkBoxOptOn(rr, lsAutApprove)) {
        func.varsSet("aut_approve", "Y");
      } else {
        func.varsSet("aut_approve", "N");
      }

      // if(this.checkBox_opt_on(rr, ls_aut_print)){
      // func.vars_set("aut_print", "Y");
      // } else {
      // func.vars_set("aut_print", "N");
      // }

      if (func.delAuthority() != 1) {
        llErr++;
        dbRollback();
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }

      if (this.checkBoxOptOn(rr, lsAutQuery) || this.checkBoxOptOn(rr, lsAutUpdate)
          || this.checkBoxOptOn(rr, lsAutApprove)
      // ||this.checkBox_opt_on(rr, ls_aut_print)
      ) {
        if (func.insertAuthority() != 1) {
          llErr++;
          dbRollback();
          wp.colSet(rr, "ok_flag", "X");
          continue;
        }
      }

      if (func.insertLog() == 1) {
        llOk++;
        sqlCommit(1);
        wp.colSet(rr, "ok_flag", "V");
        continue;
      } else {
        llErr++;
        dbRollback();
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }
    }

    alertMsg("執行完畢 , 成功:" + llOk + " 失敗:" + llErr);

  }

  @Override
  public void initButton() {
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void initPage() {
    if (wp.itemEmpty("ind_num")) {
      wp.colSet("ind_num", "0");
    }

  }

  public void wfAjaxWind() throws Exception {

    // String ls_winid =
    selectSecWindow(wp.itemStr("ax_wind"));
    if (rc != 1) {
      wp.addJSON("ax_win_name", "");
      wp.addJSON("ax_update", "N");
      wp.addJSON("ax_approve", "N");
      return;
    }
    wp.addJSON("ax_win_name", sqlStr("wf_name"));
    wp.addJSON("ax_update", sqlStr("wf_update"));
    wp.addJSON("ax_approve", sqlStr("wf_approve"));
    // wp.addJSON("wf_query",sql_ss("wf_query"));
  }

  void selectSecWindow(String aWinid) {
    String lsSql = "select *" + " from sec_window " + " where wf_winid =?";
    setString2(1, aWinid);
    this.sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertErr2("查無 程式: ex_winid=" + aWinid);
    }
    return;
  }

}
