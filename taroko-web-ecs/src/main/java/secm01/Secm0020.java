package secm01;
/** 子系統資料維護
 * 2019-1007   JH    approve
 * 109-04-20  shiyuqi       updated for project coding standard
 */

import taroko.com.TarokoCommon;

public class Secm0020 extends ofcapp.BaseEditMulti {

  String isRowid = "", isGroupId = "", isGroupName = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (empty(strAction)) {
      alertErr2("PROGRAM ACTION CODE is empty");
      // } else if (eq_igno(wp.buttonCode, "X")) {
      // /* 轉換顯示畫面 */
      // is_action = "new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 新增/修改功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
    // showScreen_detl();
  }

  @Override
  public void initPage() {
    int rr = 0;
    for (int ii = 0; ii < 10; ii++) {
      rr++;
      wp.colSet(ii, "ser_num", commString.numFormat(rr, "00"));
      // wp.col_set(ii, "db_optcode", "");
      wp.colSet(ii, "group_id", "");
      wp.colSet(ii, "group_name", "");
      wp.colSet(ii, "mod_date", "");
      wp.colSet(ii, "mod_user", "");
      wp.colSet(ii, "kk_readonly", "");
    }
    wp.listCount[0] = 10;
  }

  @Override
  public void dataRead() throws Exception {
    wp.whereStr = " where 1=1" + this.sqlCol(wp.itemStr("ex_group_id"), "group_id", "like%")
        + sqlCol(wp.itemStr("ex_group_name"), "group_name", "like%");

    wp.selectSQL = sqlRowid + ", mod_seqno " + ", group_id" + ", group_name" + ", mod_user, "
        + wp.sqlID + "uf_2ymd(mod_time) as mod_date" + ", 'readonly' as kk_readonly";
    wp.daoTable = "sec_workgroup";
    wp.whereOrder = " order by group_id";
    pageQuery();
    wp.listCount[0] = wp.selectCnt;
    log("row=" + wp.selectCnt);
    // wp.setListCount(1);
    // wp.notFound = "";
  }

  @Override
  public void saveFunc() throws Exception {
    String[] groupId = wp.itemBuff("group_id");
    String[] groupName = wp.itemBuff("group_name");
    String[] modSeqno = wp.itemBuff("mod_seqno");
    String[] rowid = wp.itemBuff("rowid");
    String lsOpt = "";
    int llOk = 0, llErr = 0;
    wp.listCount[0] = wp.itemRows("rowid");

    if (this.checkApproveZz() == false)
      return;

    for (int ii = 0; ii < rowid.length; ii++) {
      rc = 1;
      lsOpt = wp.itemStr("optK-" + ii);
      if (empty(rowid[ii]) == false) {
        wp.colSet(ii, "kk_readonly", "readonly");
        deleteSecWorkgroup(ii);
        if (rc == -1) {
          wp.colSet(ii, "ok_flag", "X");
          llErr++;
          continue;
        }
      }
      if (eqIgno(lsOpt, "1"))
        continue;
      // -update-
      rc = dataCheck(ii);
      if (rc == 0) {
        continue;
      }
      if (rc == -1) {
        wp.colSet(ii, "ok_flag", "X");
        llErr++;
        continue;
      }

      insertSecWorkgroup(ii);
      if (rc != 1) {
        wp.colSet(ii, "ok_flag", "X");
        llErr++;
        continue;
      }
      this.sqlCommit(rc);
      wp.colSet(ii, "ok_flag", "V");
      llOk++;
    }
    wp.listCount[0] = rowid.length;
    if (llErr == 0) {
      clearFunc();
    }
    errmsg("資料存檔處理完成; 成功=" + llOk + ", 失敗=" + llErr);
  }

  int dataCheck(int rr) {
    String lsGroupId = wp.itemStr(rr, "group_id");
    String lsGroupName = wp.itemStr(rr, "group_name");
    if (empty(lsGroupId) && empty(lsGroupName)) {
      return 0;
    }
    if (empty(lsGroupId) || empty(lsGroupName)) {
      return -1;
    }

    return 1;
  }

  void insertSecWorkgroup(int rr) throws Exception {
    String lsSql = "insert into sec_workgroup (" + " group_id, group_name , apr_user , apr_date "
        + ", mod_user, mod_time, mod_pgm, mod_seqno " + " ) values ( "
        + " ?, ?, ? , to_char(sysdate,'yyyymmdd'),?, sysdate, 'secm0020', 1" + " )";
    Object[] param = new Object[] {wp.itemStr(rr, "group_id"), wp.itemStr(rr, "group_name"),
        wp.itemStr("approval_user"), wp.loginUser};
    this.sqlExec(lsSql, param);
    if (rc != 1 || sqlRowNum != 1) {
      rc = -1;
    }
  }

  void deleteSecWorkgroup(int rowId) throws Exception {
    String lsRowid = wp.itemStr(rowId, "rowid");
    if (empty(lsRowid))
      return;
    // -delete-
    String lsSql = "delete sec_workgroup " + " where rowid =?";

    setRowid(1, lsRowid);
    this.sqlExec(lsSql);
  }

  @Override
  public void deleteFunc() throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

  @Override
  public void initButton() {
    return;
  }

}
