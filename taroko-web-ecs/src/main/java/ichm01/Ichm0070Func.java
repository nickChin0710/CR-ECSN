/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-28  V1.00.02  Justin         parameterize sql
******************************************************************************/

package ichm01;

import busi.FuncAction;

public class Ichm0070Func extends FuncAction {
  String isCardNo = "";

  @Override
  public void dataCheck() {
    String lsRowid = "";

    if (ibUpdate || ibDelete) {
      if (wp.itemEmpty("send_date") == false) {
        errmsg("資料己傳送, 不可異動");
        return;
      }

      lsRowid = wp.itemStr2("rowid");

      String sql1 = " select send_date from ich_refuse_log where 1=1 and hex(rowid) = ? " ;
      sqlSelect(sql1, new Object[] {lsRowid});

      if (colEmpty("send_date") == false) {
        errmsg("資料己傳送, 不可異動");
        return;
      }

    }

    if (ibDelete)
      return;

    if (ibAdd)
      isCardNo = wp.itemStr2("kk_card_no");
    else
      isCardNo = wp.itemStr2("card_no");

    if (ibAdd) {
      String sql2 =
          " select count(*) as db_cnt from ich_refuse_log where card_no = ? and send_date ='' ";
      sqlSelect(sql2, new Object[] {isCardNo});
      if (colNum("db_cnt") > 0) {
        errmsg("此卡號有資料未傳送, 不可再新增");
        return;
      }
    }

    if (wp.itemEq("secu_code", "3")) {
      if (wp.itemEq("db_tsc57", "N")) {
        errmsg("未送拒絕代行, 不須 [取消拒絕代行]");
        return;
      }
    } else if (wp.itemEq("secu_code", "4")) {
      if (wp.itemEq("db_tsc04", "N")) {
        errmsg("未送鎖卡, 不須 [取消鎖卡]");
        return;
      }
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ich_refuse_log ( " + " ich_card_no , " + " card_no ," + " secu_code ,"
        + " refuse_type ," + " risk_remark ," + " send_date ," + " from_type ," + " crt_date ,"
        + " crt_time ," + " crt_user ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " :ich_card_no , " + " :card_no ," + " :secu_code ,"
        + " :refuse_type ," + " :risk_remark ," + " :send_date ," + " '1' ,"
        + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " :crt_user ,"
        + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    item2ParmStr("ich_card_no");
    setString("card_no", isCardNo);
    item2ParmStr("secu_code");
    setString("refuse_type", wp.itemStr2("secu_code"));
    item2ParmStr("risk_remark");
    item2ParmStr("send_date");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update ich_refuse_log set " + " secu_code =:secu_code , "
        + " refuse_type =:refuse_type , " + " risk_remark =:risk_remark , "
        + " mod_user =:mod_user , " + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 and rowid =x:rowid ";

    item2ParmStr("secu_code");
    setString("refuse_type", wp.itemStr2("secu_code"));
    item2ParmStr("risk_remark");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("rowid", wp.itemStr2("rowid"));

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete ich_refuse_log where 1=1 and hex(rowid) = ? " ;

    sqlExec(strSql, new Object[] {wp.itemStr2("rowid")});

    if (sqlRowNum <= 0) {
      errmsg("");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int insertData() {
    msgOK();

    strSql = " insert into ich_refuse_log ( " + " card_no , " + " ich_card_no ," + " secu_code ,"
        + " refuse_type ," + " risk_remark ," + " send_date ," + " from_type ," + " crt_date ,"
        + " crt_time ," + " crt_user ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " :card_no ," + " :ich_card_no ," + " :secu_code ,"
        + " :refuse_type ," + " '' ," + " '' ," + " '1'," + " to_char(sysdate,'yyyymmdd') ,"
        + " to_char(sysdate,'hh24miss') ," + " :crt_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";

    item2ParmStr("card_no");
    item2ParmStr("ich_card_no");
    item2ParmStr("secu_code");
    item2ParmStr("refuse_type");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ich_refuse_log error");
    }

    return rc;
  }

}
