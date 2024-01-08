package secm01;
/** 使用者資料維護 V.2018-0430
 * 2019-1128    JH             ++bank_unitno
 * 109-04-20   shiyuqi       updated for project coding standard     * 
 * 2020-0918 JustinWu      check user_id cannot be null
 * 2022-0616 Ryan          修改資料排序
 * 2022-0711 Ryan          usr_id修改為模糊查詢
 * */
import ofcapp.BaseAction;

public class Secm0050 extends BaseAction {
  String userId = "";


  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("usr_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'SEC_USRLVL'");

        wp.optionKey = wp.colStr("usr_deptno");
        dddwList("dddw_dept_no", "ptr_dept_code", "dept_code", "dept_code||'_'||dept_name",
            "where 1=1");

        wp.optionKey = wp.colStr("usr_amtlevel");
        dddwList("dddw_user_amtlevel", "sec_amtlimit", "al_level", "al_level||'_'||al_desc",
            "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
//    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_usr_id"), "usr_id", ">=");
	  wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_usr_id"), "usr_id", "like%");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "usr_id, " + "usr_cname," + "usr_empno, " + "usr_deptno," + "usr_type,"
        + "usr_level," + "usr_group, bank_unitno, " + "usr_indate," + "usr_intime";
    wp.daoTable = "sec_user";
//    wp.whereOrder = " order by usr_id ";
    wp.whereOrder = " order by bank_unitno,usr_level,usr_id ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    userId = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(userId)) {
      userId = itemkk("usr_id");
      if (userId.isEmpty()) {
    	  alertErr("使用者代號不可為空");
          return;
	  }
    }
    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno, " + "usr_id,   " + "usr_cname, " + "usr_password, "
            + "usr_type," + "usr_empno," + "usr_deptno," + "usr_level," + "usr_group, bank_unitno, "
            + " usr_amtlevel, cellar_phone, " + "to_char(mod_time,'yyyymmdd') as mod_date,"
            + "mod_user," + "usr_indate," + "usr_intime," + "crt_date ," + "crt_user ";
    wp.daoTable = "sec_user";
    wp.whereStr = " where 1=1" + sqlCol(userId, "usr_id");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + userId);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    Secm0050Func func = new Secm0050Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else
      saveAfter(false);


  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void userAction() throws Exception {
    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
      strAction = "A";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      strAction = "D";
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
    }
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }
}
