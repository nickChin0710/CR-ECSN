/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-09-09  V1.00.00  Andy Liu    program initial                           *
* 108-12-03  V1.00.01  Amber	  Update     								 *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm02;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm4040 extends BaseEdit {
  String kkDeptNo = "";
  String mRowid = "";
  String mModSeqno = "";
  String msg = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      strAction = "S2";
      saveFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();  
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_dept_no")) == false) {
      wp.whereStr += sqlCol(wp.itemStr("ex_dept_no"), "dept_no");
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    // select columns
    wp.selectSQL = "dept_no, " + "dept_name, " + "smtp, " + "email, " + "auth_id, "
        + "auth_passwd, " + "decode(stop_flag,'N','啟用','Y','停用') as stop_flag " + "";
    getWhereStr();
    wp.daoTable = "ptr_sender_list";
    wp.whereOrder = " order by dept_no ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kkDeptNo = wp.itemStr("kk_dept_no");
    if (empty(kkDeptNo)) {
      kkDeptNo = itemKk("data_k1");
    }
    if (empty(kkDeptNo)) {
      kkDeptNo= wp.itemStr("dept_no");
    }

    wp.selectSQL = "hex(rowid) as rowid, " + "dept_no, " + "dept_name, " + "smtp, " + "email, "
        + "auth_id, " + "auth_passwd, " + "stop_flag " + "";
    wp.daoTable = "ptr_sender_list";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and dept_no = :dept_no ";
    setString("dept_no", kkDeptNo);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, kk_dept_no= " + kkDeptNo);
      return;
    }
    wp.colSet("kk_dept_no", kkDeptNo);

  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    String ls_sql = "", is_sql = "", us_sql = "";
    kkDeptNo = wp.itemStr("dept_no");
    if (empty(kkDeptNo)) {
      kkDeptNo = wp.itemStr("kk_dept_no");
    }
    mRowid = wp.itemStr("rowid");
    // if(of_validation() !=1){
    // err_alert(msg);
    // return;
    // }

    if (strAction.equals("A")) {
      ls_sql = "select count(*) as ct from ptr_sender_list where dept_no =:dept_no ";
      setString("dept_no", kkDeptNo);
      sqlSelect(ls_sql);
      if (Integer.parseInt(sqlStr("ct")) > 0) {
        alertErr2("已有資料,無法新增!! 部門代號 : " + kkDeptNo);
        return;
      }

      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Insert("ptr_sender_list");
      sp.ppstr("dept_no", kkDeptNo);
      sp.ppstr("dept_name", wp.itemStr("dept_name"));
      sp.ppstr("smtp", wp.itemStr("smtp"));
      sp.ppstr("email", wp.itemStr("email"));
      sp.ppstr("auth_id", wp.itemStr("auth_id"));
      sp.ppstr("auth_passwd", wp.itemStr("auth_passwd"));
      sp.ppstr("stop_flag", wp.itemStr("stop_flag"));
      sp.addsql(", modify_date ", ", sysdate ");
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        alertErr("資料新增失敗!!");
        return;
      } else {
        sqlCommit(1);
        alertMsg("資料新增成功!!");
        return;
      }
    }
    if (strAction.equals("U")) {
      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Update("ptr_sender_list");
      sp.ppstr("dept_no", kkDeptNo);
      sp.ppstr("dept_name", wp.itemStr("dept_name"));
      sp.ppstr("smtp", wp.itemStr("smtp"));
      sp.ppstr("email", wp.itemStr("email"));
      sp.ppstr("auth_id", wp.itemStr("auth_id"));
      sp.ppstr("auth_passwd", wp.itemStr("auth_passwd"));
      sp.ppstr("stop_flag", wp.itemStr("stop_flag"));
      sp.addsql(", modify_date =sysdate");
      sp.sql2Where(" where dept_no=?", kkDeptNo);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        alertErr2("資料修改失敗!!");
        sqlCommit(0);
        return;
      } else {
        alertMsg("資料修改成功!!");
        sqlCommit(1);
      }
    }

  }

  public int of_validation() {
    return 1;
  }

  @Override
  public void deleteFunc() {
    String ds_sql = "";
    kkDeptNo = wp.itemStr("dept_no");
    ds_sql = "delete ptr_sender_list " + "where dept_no =:dept_no ";
    setString("dept_no", kkDeptNo);
    sqlExec(ds_sql);

    if (sqlRowNum <= 0) {
      alertErr2("資料刪除失敗!!");
      return;
    } else {
      alertMsg("資料刪除成功!!");
      sqlCommit(1);
      return;
    }
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
      // if (wp.respHtml.indexOf("_detl") > 0) {
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("rsecind_kind");
      // this.dddw_list("dddw_rsecind_kind", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and
      // wf_type like 'RSECIND_KIND' order by wf_id");
      //
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("rsecind_flag");
      // this.dddw_list("dddw_rsecind_flag", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and
      // wf_type like 'RSECIND_REASON%' order by wf_id");
      //
      // }
    } catch (Exception ex) {
    }
  }

}



