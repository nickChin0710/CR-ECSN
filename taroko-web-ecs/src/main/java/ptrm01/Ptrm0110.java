/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-02  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0110 extends BaseEdit {
  Ptrm0110Func func;

  String deptCode = "";
  //String kk2 = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;


    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
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

    // dddw_select();
    initButton();
  }


  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_dept_code")) == false) {
      wp.whereStr += " and  dept_code = :dept_code ";
      setString("dept_code", wp.itemStr("ex_dept_code"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();// 執行SQL查詢
  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " dept_code, " + " dept_name, " + "gl_code," + "gl_code2," + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user ";

    wp.daoTable = "ptr_dept_code";
    wp.whereOrder = " order by dept_code";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    deptCode = wp.itemStr("dept_code");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    deptCode = wp.itemStr("KK_dept_code");
    if (empty(deptCode)) {
      deptCode = itemKk("data_k1");

    }


    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + " dept_code, " + " dept_name, "
        + " gl_code," + " gl_code2," + " crt_date," + " crt_user,"
        + " uf_2ymd(mod_time) as mod_date," + " mod_user";
    wp.daoTable = "ptr_dept_code";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  dept_code = :dept_code ";
    setString("dept_code", deptCode);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + deptCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    func = new Ptrm0110Func(wp);


    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }


  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("bin_no");
      this.dddwList("dddw_bin_no", "ptr_bintable", "bin_no", "", "where 1=1");
    } catch (Exception ex) {
    }
  }

}
