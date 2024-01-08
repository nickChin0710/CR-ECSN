package secm01;
/** secp2060 子系統使用者層級權限設定
 * 2019-0822   JH    modify
   2019-0415:  JH    modify
 109-04-20  shiyuqi       updated for project coding standard  
 *
 * */

import busi.FuncAction;

public class Secp2060Func extends FuncAction {

  private String groupId = "";
  private String userLevel = "";
  private String winid = "";
  private String autQuery = "";
  private String autUpdate = "";
  private String autAppr = "";
  // private String _aut_print = "";

  @Override
  public void dataCheck() {
    String[] tt = new String[2];
    tt[0] = varsStr("data");
    tt[1] = ",";
    // --子系統
    groupId = commString.token(tt).trim();
    if (!wp.itemEq("ex_group_id", groupId)) {
      // errmsg("子系統代碼不符");
      errmsg("err=group_id ");
      return;
    }
    // --層級
    userLevel = commString.token(tt).trim();
    if (pos(",A,B,C", userLevel) <= 0) {
      errmsg("err=user_level");
      return;
    }
    if (!wp.itemEq("ex_all_level", "Y") && !wp.itemEq("ex_user_level", userLevel)) {
      // errmsg("使用者層級不符");
      errmsg("err=user_level");
      return;
    }
    // --程式代碼
    winid = commString.token(tt).trim();
    if (empty(winid)) {
      // errmsg("程式代碼不可空白");
      errmsg("err=pgm_id");
      return;
    }

    // --執行
    autQuery = commString.token(tt).trim();
    // --維護
    autUpdate = commString.token(tt).trim();
    // --覆核
    autAppr = commString.token(tt).trim();
    // --列印	
    // _aut_print = commString.token(tt).trim();
    if (!eq(autQuery, "Y") && !eq(autUpdate, "Y") && !eq(autAppr, "Y")
    // && !eq(_aut_print, "Y")
    ) {
      // errmsg("全部無權限");
      errmsg("err:no any auth");
      return;
    }

    strSql = "select count(*) as xx_cnt" + " from sec_window" + " where wf_winid =?";
    setString2(1, winid);
    sqlSelect(strSql);
    if (sqlRowNum <= 0 || colInt("xx_cnt") == 0) {
      // errmsg("程式代碼錯誤");
      errmsg("err=pgm_id");
      return;
    }

  }

  @Override
  public int dbInsert() {
    this.actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    // if (eq(_winid,kk_winid)) {
    //// errmsg("資料重複");
    // return rc;
    // }

    insertAuthorityLog();
    if (sqlRowNum <= 0) {
      errmsg("Insert error:" + getMsg());
    }

    return rc;
  }

  public int dataCheckUpdate() {
    this.actionInit("U");

    String lsAprFlag = wp.itemStr2("kk_apr_flag");
    groupId = wp.itemStr2("kk_group_id");
    userLevel = wp.itemStr2("kk_user_level");
    if (eq(lsAprFlag, "Y")) {
      strSql = "select count(*) as xx_cnt" + " from sec_authority_log"
          + " where group_id =? and user_level =?" + " and apr_flag <>'Y'";
      setString2(1, groupId);
      setString(userLevel);
      sqlSelect(strSql);
      if (sqlRowNum > 0 && colNum("xx_cnt") > 0) {
        errmsg("有待覆核資料, 不可修改");
        return rc;
      }
    }

    deleteAuthorityLog(groupId, userLevel, "");

    return rc;
  }

  @Override
  public int dbUpdate() {
    return 0;
  }

  public int procUpdate(String[] aaCode) {
    winid = aaCode[0];
    autQuery = aaCode[1];
    autUpdate = aaCode[2];
    autAppr = aaCode[3];
    // _aut_print =aa_code[4];

    return insertAuthorityLog();
  }

  int insertAuthorityLog() {
    // delete_authority_log(_group_id,_user_level,_winid);
    strSql = "select count(*) as log_cnt" + " from sec_authority_log"
        + " where group_id =? and user_level =?" + " and wf_winid =? and apr_date=''";
    setString2(1, groupId);
    setString(userLevel);
    setString(winid);
    sqlSelect(strSql);
    if (colInt("log_cnt") > 0)
      return 1;

    strSql = "insert into sec_authority_log (" + " group_id , user_level , "
        + " wf_winid , apr_flag , " + " aut_query , aut_update , aut_approve, aut_print , "
        + " crt_date , crt_user , " + " mod_audcode , " + " mod_time, mod_user, mod_pgm "
        + " ) values (" + " :group_id, :user_level, :wf_winid , " + " 'N' , "
        + " :aut_query, :aut_update, :aut_approve, 'N' , " + commSqlStr.sysYYmd + ", :crt_user , "
        + " :aud_code , " + commSqlStr.sysdate + ", :mod_user, :mod_pgm " + " )";
    setString2("group_id", groupId);
    setString2("user_level", userLevel);
    setString2("wf_winid", winid);
    setString2("aut_query", autQuery);
    setString2("aut_update", autUpdate);
    setString2("aut_approve", autAppr);
    // ppp("aut_print", _aut_print);
    setString2("crt_user", modUser);
    setString2("aud_code", this.actionCode);
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;
  }

  void deleteAuthorityLog(String aGroup, String aLevel, String aWinid) {
    strSql =
        "delete sec_authority_log" + " where group_id =? and user_level =?" + " and apr_flag <>'Y'";
    setString2(1, aGroup);
    setString(aLevel);
    if (notEmpty(aWinid)) {
      strSql += " and wf_winid =?";
      setString(aWinid);
    }
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_authority_log error, kk[%s,%s]", aGroup, aLevel);
    }
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int delLog(String aGroupId, String aLevel) {
    msgOK();
    strSql = " Delete sec_authority_log " + " where group_id =? " + " and user_level like ? "
        + " and apr_flag <>'Y' ";
    setString2(1, aGroupId);
    setString(wp.colStr("kk_user_level"));

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_authority_log err=" + getMsg());
    }

    return rc;
  }

  public int copyData() {
    msgOK();

    // --
    String sql1 = " select count(*) as db_cnt"
        + " from sec_authority_log A join sec_window B on A.wf_winid =B.wf_winid"
        + " where A.group_id = ?  and A.user_level = ? and A.apr_flag <> 'Y' ";
    sqlSelect(sql1,
        new Object[] {wp.itemStr2("ex_copy_group_id"), wp.itemStr2("ex_copy_user_level")});
    if (sqlRowNum < 0) {
      errmsg("select sec_authority_log error !");
      return rc;
    }

    if (colNum("db_cnt") > 0) {
      errmsg("複製子系統有尚未覆核的資料 不可複製 !");
      return rc;
    }

    // --未複核讀取 sec_authority_log 已複核讀取 sec_authority
    String sql2 = "";
    if (wp.itemEq("ex_apr_flag", "Y")) {
      sql2 = " select A.* from sec_authority A join sec_window B" + " on A.wf_winid =B.wf_winid"
          + " where A.group_id = ?  and A.user_level = ? ";
    } else if (wp.itemEq("ex_apr_flag", "N")) {
      sql2 = " select A.* from sec_authority_log A join sec_window B on A.wf_winid =B.wf_winid"
          + " where A.group_id =? and A.user_level =? and A.apr_flag <> 'Y' ";
    }

    sqlSelect(sql2, new Object[] {wp.itemStr2("ex_group_id"), wp.itemStr2("ex_user_level")});
    if (sqlRowNum <= 0) {
      errmsg("此條件無資料可複製 !");
      return rc;
    }

    int ilSelectCnt = sqlRowNum;

    strSql = " insert into sec_authority_log ( " + " group_id ," + " user_level ," + " wf_winid ,"
        + " apr_flag ," + " aut_query ," + " aut_update ," + " aut_approve ," + " crt_date ,"
        + " crt_user ," + " mod_audcode ," + " mod_time ," + " mod_user ," + " mod_pgm "
        + " ) values ( " + " :group_id ," + " :user_level ," + " :wf_winid ," + " 'N' ,"
        + " :aut_query ," + " :aut_update ," + " :aut_approve ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :crt_user ," + " 'A' ," + " sysdate ," + " :mod_user ," + " :mod_pgm " + " ) ";

    for (int ii = 0; ii < ilSelectCnt; ii++) {

      setString2("group_id", wp.itemStr2("ex_copy_group_id"));
      setString2("user_level", wp.itemStr2("ex_copy_user_level"));
      setString2("wf_winid", colStr(ii, "wf_winid"));
      setString2("aut_query", colStr(ii, "aut_query"));
      setString2("aut_update", colStr(ii, "aut_update"));
      setString2("aut_approve", colStr(ii, "aut_approve"));
      setString2("crt_user", wp.loginUser);
      setString2("mod_user", wp.loginUser);
      setString2("mod_pgm", wp.modPgm());

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        sqlErr("copy sec_authority_log error !");
        return rc;
      }
    }
    return rc;
  }

}
