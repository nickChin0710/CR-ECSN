package secm01;
/* 子系統程式排序設定 V.2018-0905.jh
* updated for project coding standard V109-04-20    shiyuqi       
 * 
 * */
import busi.FuncAction;

public class Secm0060Func extends FuncAction {
  String groupId = "", wfWinid = "";

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    return 0;
  }

  @Override
  public int dbUpdate() {
    msgOK();
    groupId = wp.itemStr2("kk_group_id");
    wfWinid = varsStr("wf_winid");
    if (empty(groupId) || empty(wfWinid)) {
      errmsg("子系統, 程式代碼: 不可空白");
      return rc;
    }

    int liSort = varsInt("sort_seqno");

    deleteAuthpgmSort();
    if (rc != 1) {
      return rc;
    }
    insertAuthpgmSort(liSort);

    return rc;
  }

  void deleteAuthpgmSort() {
    strSql = "delete sec_authpgm_sort" + " where group_id =? and wf_winid =?";
    setString2(1, groupId);
    setString(wfWinid);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete sec_authpgm_sort error, kk[%s,%s]", groupId, wfWinid);
      return;
    }
  }

  void insertAuthpgmSort(int aiSort) {
    if (aiSort == 0)
      return;

    strSql = "insert into sec_authpgm_sort (" + " group_id, wf_winid, sort_seqno"
        + ", mod_user, mod_time " + " ) values (" + " :group_id, :wf_winid, :sort_seqno"
        + ", :mod_user, " + commSqlStr.sysdate + " )";
    setString2("group_id", groupId);
    setString2("wf_winid", wfWinid);
    setInt2("sort_seqno", aiSort);
    setString2("mod_user", modUser);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert sec_authpgm_sort error, kk[%s,%s]", groupId, wfWinid);
      return;
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

}
