/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-09-03  V1.00.00  Andy Liu    program initial                           *
* 108-12-03  V1.00.01  Amber	  Update     								 *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        * 
******************************************************************************/
package ptrm02;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm4010 extends BaseEdit {
  String kkGroupId = "";
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
    if (empty(wp.itemStr("ex_group_id")) == false) {
      wp.whereStr += sqlCol(wp.itemStr("ex_group_id"), "group_id");
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
    wp.selectSQL = "group_id, " + "group_name, " + "modify_date, "
        + "decode(stop_flag,'N','啟用','Y','停用') as stop_flag " + "";
    getWhereStr();
    wp.daoTable = "ptr_group_list";
    wp.whereOrder = " order by group_id ";

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
    kkGroupId = wp.itemStr("kk_group_id");
    if (empty(kkGroupId)) {
      kkGroupId = itemKk("data_k1");
    }
    if (empty(kkGroupId)) {
      kkGroupId = wp.itemStr("group_id");
    }

    wp.selectSQL = "hex(rowid) as rowid, " + "group_id, " + "group_name, " + "modify_date, "
        + "stop_flag " + "";
    wp.daoTable = "ptr_group_list";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and group_id = :group_id ";
    setString("group_id", kkGroupId);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, group_id= " + kkGroupId);
      return;
    }
    wp.colSet("kk_group_id", kkGroupId);

  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    String lsSql = "", isSql = "", usSql = "";
    kkGroupId = wp.itemStr("group_id");
    if (empty(kkGroupId)) {
      kkGroupId = wp.itemStr("kk_group_id");
    }
    mRowid = wp.itemStr("rowid");
    // if(of_validation() !=1){
    // err_alert(msg);
    // return;
    // }

    if (strAction.equals("A")) {
      lsSql = "select count(*) as ct from ptr_group_list where group_id =:group_id ";
      setString("group_id", kkGroupId);
      sqlSelect(lsSql);
      if (Integer.parseInt(sqlStr("ct")) > 0) {
        alertErr2("已有資料,無法新增!! 群組代號 : " + kkGroupId);
        return;
      }

      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Insert("ptr_group_list");
      sp.ppstr("group_id", kkGroupId);
      sp.ppstr("group_name", wp.itemStr("group_name"));
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
      sp.sql2Update("ptr_group_list");
      sp.ppstr("group_name", wp.itemStr("group_name"));
      sp.ppstr("stop_flag", wp.itemStr("stop_flag"));
      sp.addsql(", modify_date =sysdate");
      sp.sql2Where(" where group_id=?", kkGroupId);
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

  public int ofValidation() {
    return 1;
  }

  @Override
  public void deleteFunc() {
    String dsSql = "";
    kkGroupId = wp.itemStr("group_id");
    dsSql = "delete ptr_group_list " + "where group_id =:group_id ";
    setString("group_id", kkGroupId);
    sqlExec(dsSql);

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


