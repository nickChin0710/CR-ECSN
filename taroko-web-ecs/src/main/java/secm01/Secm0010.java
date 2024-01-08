/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import ofcapp.*;
import taroko.com.TarokoCommon;

public class Secm0010 extends BaseEdit {

  String seqNo;

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
      if (itemIsempty("rowid")) {
        strAction = "A";
        wp.buttonCode = "A";
        insertFunc();
      } else {
        /* 更新功能 */
        strAction = "U";
        wp.buttonCode = "U";
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

    // dddw_select();
    initButton();
  }


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud(); // rowid
    }
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(seqNo)) {
      seqNo = itemKk("seq_no");
    }
    if (isEmpty(seqNo)) {
      alertErr2("作業代碼: 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, 0 as mod_seqno, " + " proc_code, " + "parent_seq, "
        + "order_seq    ," + "seq_no       ," + "nvl(appl_id,'')  as appl_id ," + "request_html ,"
        + "program_id   ," + "method_name  ," + "package_name ," + "description as pgm_desc, "
        + "log_key_seq ";
    wp.daoTable = "comm_main_issure";
    wp.whereStr = "where proc_code ='UNIT'" + sqlCol(seqNo, "seq_no");
    this.ibDspSql = true;
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key=" + seqNo);
      return;
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE proc_code ='UNIT'" + sqlCol(wp.itemStr("ex_seq_no"), "seq_no", "like%")
        + sqlCol(wp.itemStr("ex_pgm_id"), "lower(program_id)", "like%")
        + sqlCol(wp.itemStr("ex_pack_name"), "package_name", "like%") + " order by 1";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + "seq_no       , " + " proc_code    , " + "parent_seq   , "
        + "order_seq    , " + "appl_id      , " + "request_html , " + "program_id   , "
        + "method_name  , " + "package_name , " + "description as pgm_desc  , " + "log_key_seq   ";
    wp.daoTable = "comm_main_issure";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    logSql();
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    seqNo = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void initPage() {
    if (posAny(wp.respHtml, "_detl") > 0) {
      wp.colSet("proc_code", "UNIT");
      wp.colSet("method_name", "showScreen");
    }
  }

  void dataCheck() {
    seqNo = wp.itemStr("seq_no");
    if (isAdd()) {
      seqNo = wp.itemStr("kk_seq_no");
    }
    if (empty(seqNo)) {
      alertErr2("作業代碼: 不可空白");
      return;
    }

  }

  @Override
  public void insertFunc() throws Exception {
    dataCheck();
    if (rc == -1)
      return;
    if (wp.itemEmpty("parent_seq")) {
      wp.itemSet("parent_seq", "0000");
    }
    if (wp.itemEmpty("order_seq")) {
      wp.itemSet("order_seq", seqNo);
    }
    String lsSql = "insert into comm_main_issure (" + " proc_code, " // 1
        + " parent_seq, " + " order_seq, " + " seq_no, " + " appl_id, " // 5
        + " request_html, " + " program_id, " + " method_name, " + " package_name, "
        + " description, " // 10
        + " log_key_seq" // 11
        + " ) values (" + " ?,?,?,?,?" + ",?,?,?,?,?" + ",?" + " )";
    Object[] param = new Object[] {"UNIT", wp.itemStr("parent_seq"), wp.itemNum("order_seq"), seqNo,
        wp.itemStr(" appl_id"), // 5
        wp.itemStr(" request_html"), wp.itemStr(" program_id"), wp.itemStr("method_name"),
        wp.itemStr("package_name"), wp.itemStr("pgm_desc"), wp.itemStr("log_key_seq")};
    this.sqlExec(lsSql, param);
    if (sqlRowNum != 1) {
      alertErr2("insert: " + this.sqlErrtext);
    }
    sqlCommit(rc);
    if (rc == 1) {
      clearFunc();
    }
  }

  @Override
  public void updateFunc() throws Exception {
    dataCheck();
    if (rc == -1)
      return;

    String lsSql = "update comm_main_issure set " + " parent_seq =?, " + " order_seq =?, "
        + " appl_id =?, " + " request_html =?, " + " program_id =?, " + " method_name =?, "
        + " package_name =?, " + " description =?, " + " log_key_seq =? "
        + " where proc_code='UNIT'" + sqlCol(seqNo, "seq_no");
    Object[] param =
        new Object[] {wp.itemStr("parent_seq"), wp.itemNum("order_seq"), wp.itemStr(" appl_id"),
            wp.itemStr(" request_html"), wp.itemStr(" program_id"), wp.itemStr("method_name"),
            wp.itemStr("package_name"), wp.itemStr("pgm_desc"), wp.itemStr("log_key_seq")};
    this.sqlExec(lsSql, param);
    if (sqlRowNum != 1) {
      alertErr2("update: " + this.sqlErrtext);
    }
    sqlCommit(rc);
  }

  @Override
  public void deleteFunc() throws Exception {
    dataCheck();
    if (rc == -1)
      return;

    String lsSql = "delete comm_main_issure " + " where proc_code='UNIT'" + sqlCol(seqNo, "seq_no");
    this.sqlExec(lsSql);
    if (sqlCode == -1 || sqlRowNum != 1) {
      alertErr2("delete: " + this.sqlErrtext);
    }
    sqlCommit(rc);
    if (rc == 1) {
      clearFunc();
    }
  }

  @Override
  public void saveFunc() throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

}
