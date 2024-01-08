package secm01;
/**
 * 2019-1016   JH    modify
109-04-20  shiyuqi       updated for project coding standard     *
 *
 * */
import busi.FuncAction;

public class Secp2070Func extends FuncAction {
  String groupId = "";
  String userLevel = "";
  private String winid;
  private String autQuery;
  private String autUpdate;
  private String autApprove;
  private String autPrint;
  private String rowid;

  int iiRowNum = -1;

  @Override
  public void dataCheck() {
    groupId = wp.itemStr2("kk_group_id");
    userLevel = wp.itemStr2("kk_user_level");

    if (empty(groupId) || empty(userLevel)) {
      errmsg("子系統, 使用者層級: 不可空白");
    }
  }

  public boolean isOK() {
    return (rc == 1);
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  void selectAuthorityLog() {
    wp.logSql = false;
    strSql = "select * from sec_authority_log" + " where rowid =? and apr_flag<>'Y'";
    setRowId2(1, rowid);
    sqlSelect(strSql, false);
    if (sqlRowNum <= 0) {
      errmsg("sec_authority_log not find");
    }
  }

  @Override
  public int dataProc() {
    msgOK();

    int ll = iiRowNum;
    // _winid = wp.item_ss(ll, "wf_winid");
    // _aut_query = wp.item_ss(ll, "aut_query");
    // _aut_update = wp.item_ss(ll, "aut_update");
    // _aut_approve = wp.item_ss(ll, "aut_approve");
    // _aut_print = wp.item_ss(ll, "aut_print");
    rowid = wp.itemStr(ll, "log_rowid");
    // ii_row_num =ll;
    selectAuthorityLog();
    if (rc != 1)
      return rc;

    winid = colStr("wf_winid");
    autQuery = colStr("aut_query");
    autUpdate = colStr("aut_update");
    autApprove = colStr("aut_approve");
    boolean lbDel = colEq("mod_audcode", "D");

    deleteSecAuthority();
    if (rc != 1)
      return rc;

    if (lbDel == false) {
      insertSecAuthority();
      if (rc != 1)
        return rc;
    }

    strSql = "update sec_authority_log set" + " apr_flag ='Y'," + " apr_date =" + commSqlStr.sysYYmd
        + "," + " apr_user =?" + ", mod_user =?, mod_time=sysdate, mod_pgm =?" + " where rowid =?";

    setString2(1, modUser);
    setString(modUser);
    setString(modPgm);
    // -kk-
    this.setRowId(rowid);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update sec_authority_log err, kk[%s]", winid);
    }
    return rc;
  }

  void insertSecAuthority() {
    // -均無權限-
    if (!eq(autQuery, "Y") && !eq(autUpdate, "Y") && !eq(autApprove, "Y")) {
      return;
    }

    sql2Insert("sec_authority");
    addsqlParm(" ?", " group_id", groupId);
    addsqlParm(",?", ", user_level", userLevel);
    addsqlParm(",?", ", wf_winid", winid);
    addsqlParm(",?", ", aut_query", autQuery);
    addsqlParm(",?", ", aut_update", autUpdate);
    addsqlParm(",?", ", aut_approve", autApprove);
    addsqlParm(",?", ", crt_date", colStr("crt_date"));
    addsqlParm(",?", ", crt_user", colStr("crt_user"));
    addsqlYmd(", apr_date");
    addsqlParm(",?", ", apr_user", modUser);
    addsqlModXXX(modUser, modPgm);
    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert sec_authority error, kk[%s]", winid);
    }
  }

  void deleteSecAuthority() {
    strSql =
        "Delete sec_authority " + " where group_id =?" + " and user_level =?" + " and wf_winid =?";
    setString2(1, groupId);
    setString(userLevel);
    setString(winid);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_authority_log kk[%s]", winid);
    }
  }

}
