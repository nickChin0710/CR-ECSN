/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package ichm01;

import busi.FuncAction;

public class Ichm0050Func extends FuncAction {
  String ichCardNo = "";

  @Override
  public void dataCheck() {
    if (ibAdd)
      ichCardNo = wp.itemStr2("kk_ich_card_no");
    else
      ichCardNo = wp.itemStr2("ich_card_no");

    if (checkIchCard() == false) {
      errmsg("愛金卡卡號不存在 !!");
      return;
    }

    if (!ibDelete) {
      if (!wp.itemEmpty("return_date")) {
        errmsg("已退卡 不可列黑名單");
        return;
      }

      if (!wp.itemEmpty("lock_date")) {
        errmsg("已鎖卡 不可列黑名單");
        return;
      }

    }

    if (ibDelete)
      return;


    if (wp.itemEq("black_flag", "1") || wp.itemEq("black_flag", "3")) {
      if (chkStrend(wp.itemStr2("send_date_s"), wp.itemStr2("send_date_e")) == -1) {
        errmsg("強制/不報送期間 輸入錯誤");
        return;
      }
    }

  }

  boolean checkIchCard() {

    String sql1 = " select count(*) as db_cnt from ich_card where ich_card_no = ? ";
    sqlSelect(sql1, new Object[] {ichCardNo});

    if (sqlRowNum <= 0 || colNum("db_cnt") <= 0)
      return false;

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ich_black_list ( " + " ich_card_no ," + " card_no ," + " black_date ,"
        + " black_user_id ," + " black_remark ," + " black_flag ," + " send_date_s ,"
        + " send_date_e ," + " from_type ," + " crt_user ," + " crt_date ," + " apr_date ,"
        + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno "
        + " ) values ( " + " :ich_card_no ," + " :card_no ," + " :black_date ,"
        + " :black_user_id ," + " :black_remark ," + " :black_flag ," + " :send_date_s ,"
        + " :send_date_e ," + " :from_type ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";

    setString("ich_card_no", ichCardNo);
    item2ParmStr("card_no");
    item2ParmStr("black_date");
    item2ParmStr("black_user_id");
    item2ParmStr("black_remark");
    item2ParmStr("black_flag");
    item2ParmStr("send_date_s");
    item2ParmStr("send_date_e");
    item2ParmStr("from_type");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr2("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert ich_black_list error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update ich_black_list set " + " black_flag =:black_flag , "
        + " send_date_s =:send_date_s , " + " send_date_e =:send_date_e , "
        + " black_remark =:black_remark , " + " apr_date =to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user , " + " mod_user =:mod_user , " + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where ich_card_no =:ich_card_no ";

    item2ParmStr("black_flag");
    item2ParmStr("send_date_s");
    item2ParmStr("send_date_e");
    item2ParmStr("black_remark");
    setString("apr_user", wp.itemStr2("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("ich_card_no", ichCardNo);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update ich_black_list error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete ich_black_list where ich_card_no =:ich_card_no ";
    setString("ich_card_no", ichCardNo);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete ich_black_list error ");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
