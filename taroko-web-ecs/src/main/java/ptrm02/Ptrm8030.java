/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-12  V1.00.00  Andy Liu    program initial                           *
* 108-12-03  V1.00.01  Amber	  Update     								 *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm8030 extends BaseEdit {
  String kkPrgName = "";
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
    if (empty(wp.itemStr("ex_prg_name")) == false) {
      wp.whereStr += sqlCol(wp.itemStr("ex_prg_name"), "prg_name");
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
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "prg_name, " + "subject, "
        + "html_file_name, " + "attach_flag, " + "attach_file_name1, " + "attach_file_name2 ";
    getWhereStr();
    wp.daoTable = "bhu_parameter";
    wp.whereOrder = " order by prg_name ";

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

    kkPrgName = itemKk("data_k1");
    if (empty(kkPrgName)) {
      kkPrgName = wp.itemStr("kk_prg_name");
    }
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "prg_name, " + "subject, "
        + "html_file_name, " + "attach_flag, " + "attach_file_name1, " + "attach_file_name2 ";
    wp.daoTable = "bhu_parameter";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and prg_name = :prg_name ";
    setString("prg_name", kkPrgName);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, prg_name= " + kkPrgName);
      return;
    }
    wp.colSet("kk_prg_name", kkPrgName);

  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    String lsSql = "", isSql = "", usSql = "";
    kkPrgName = wp.itemStr("kk_prg_name");
    mRowid = wp.itemStr("rowid");
    mModSeqno = wp.itemStr("mod_seqno");
    if (ofValidation() != 1) {
      alertErr2(msg);
      return;
    }

    if (strAction.equals("A")) {
      lsSql = "select count(*) as ct from bhu_parameter where prg_name =:prg_name ";
      setString("prg_name", kkPrgName);
      sqlSelect(lsSql);
      if (Integer.parseInt(sqlStr("ct")) > 0) {
        alertErr2("已有資料,無法新增!! 產生程式 : " + kkPrgName);
        return;
      }

      isSql = "insert into bhu_parameter ( "
          + "prg_name, subject, html_file_name, attach_flag, attach_file_name1, "
          + "attach_file_name2, mod_user, mod_time, mod_pgm, mod_seqno" + ") values ("
          + ":prg_name, :subject, :html_file_name, :attach_flag, :attach_file_name1,"
          + ":attach_file_name2, :mod_user, sysdate, 'bhum0010', '0'" + ")";
      setString("prg_name", kkPrgName);
      setString("subject", wp.itemStr("subject"));
      setString("html_file_name", wp.itemStr("html_file_name"));
      setString("attach_flag", wp.itemStr("attach_flag"));
      setString("attach_file_name1", wp.itemStr("attach_file_name1"));
      setString("attach_file_name2", wp.itemStr("attach_file_name2"));
      setString("mod_user", wp.loginUser);
      sqlExec(isSql);
      System.out.println(isSql);
      if (sqlRowNum <= 0) {
        alertErr2("新增失敗!!");
        return;
      } else {
        alertMsg("新增成功!!");
        sqlCommit(1);
      }
    }
    if (strAction.equals("U")) {
      int m_seqno = Integer.parseInt(mModSeqno) + 1;
      usSql = "update bhu_parameter set " + "prg_name =:prg_name, " + "subject =:subject, "
          + "html_file_name =:html_file_name, " + "attach_flag =:attach_flag, "
          + "attach_file_name1 =:attach_file_name1, " + "attach_file_name2 =:attach_file_name2, "
          + "mod_user =:mod_user, " + "mod_time = sysdate, " + "mod_pgm = 'bhum0010', "
          + "mod_seqno =:mod_seqno " + "where hex(rowid) =:m_rowid "
          + "and mod_seqno =:m_mod_seqno ";
      setString("prg_name", kkPrgName);
      setString("subject", wp.itemStr("subject"));
      setString("html_file_name", wp.itemStr("html_file_name"));
      setString("attach_flag", wp.itemStr("attach_flag"));
      setString("attach_file_name1", wp.itemStr("attach_file_name1"));
      setString("attach_file_name2", wp.itemStr("attach_file_name2"));
      setString("mod_user", wp.loginUser);
      setString("mod_seqno", m_seqno + "");
      setString("m_rowid", mRowid);
      setString("m_mod_seqno", mModSeqno);

      sqlExec(usSql);
      if (sqlRowNum <= 0) {
        alertErr2("修改資料失敗!!");
        return;
      } else {
        alertMsg("修改資料成功!!");
        sqlCommit(1);
      }
    }

  }

  public int ofValidation() {
    if (wp.itemStr("attach_flag").equals("1") && empty(wp.itemStr("attach_file_name1"))) {
      msg = "附加檔案數為1，檔名一須輸入!";
      return -1;
    }
    if (wp.itemStr("attach_flag").equals("2")) {
      if (empty(wp.itemStr("attach_file_name1")) || empty(wp.itemStr("attach_file_name2"))) {
        msg = "附加檔案數為2，檔名一二皆須輸入!!";
        return -1;
      }
    }
    return 1;
  }

  @Override
  public void deleteFunc() {
    String dsSql = "";
    mRowid = wp.itemStr("rowid");
    mModSeqno = wp.itemStr("mod_seqno");
    dsSql =
        "delete bhu_parameter " + "where hex(rowid) =:m_rowid " + "and mod_seqno =:m_mod_seqno ";
    setString("m_rowid", mRowid);
    setString("m_mod_seqno", mModSeqno);
    sqlExec(dsSql);

    if (sqlRowNum <= 0) {
      alertErr2("刪除資料失敗!!");
    } else {
      alertMsg("刪除資料成功!!");
      sqlCommit(1);
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



