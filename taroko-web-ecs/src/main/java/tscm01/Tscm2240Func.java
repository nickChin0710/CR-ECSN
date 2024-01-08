/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
* 110/01/19  V1.00.04  Wilson        order by調整      
* 111-04-14  V1.00.05  machao     TSC畫面整合                                                                           *
******************************************************************************/
package tscm01;

import busi.FuncAction;

public class Tscm2240Func extends FuncAction {
  String cardNo = "", rowid = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardNo = wp.itemStr("kk_card_no");
    } else {
      cardNo = wp.itemStr("card_no");
    }

    if (this.ibAdd) {
      rowid = "";
    } else {
      rowid = wp.itemStr("rowid");
    }

    if (empty(cardNo)) {
      errmsg("信用卡卡號 : 不可空白");
      return;
    }


    if (!empty(wp.itemStr("send_date"))) {
      errmsg("資料己傳送, 不可異動");
      return;
    }
    if (ibUpdate || ibDelete) {
      if (checkSendDate() == false) {
        errmsg("資料己傳送, 不可異動");
        return;
      }
    }

    if (this.ibDelete)
      return;

    if (checkTscCard() == false) {
      errmsg("悠遊卡 不存在 ");
      return;
    }

    if (this.ibAdd) {
      if (checkLog() == false) {
        errmsg("此卡號有資料未傳送, 不可再新增");
        return;
      }
    }

    if (wp.itemEq("secu_code", "3")) {
      if (eqIgno(wp.colStr("db_tsc57"), "N")) {
        errmsg("未送拒絕代行, 不須 [取消拒絕代行]");
        return;
      }
    } else if (wp.itemEq("secu_code", "4")) {
      if (eqIgno(wp.colStr("db_tsc04"), "N")) {
        errmsg("未送鎖卡, 不須 [取消鎖卡]");
        return;
      }
    } else if (empty(wp.itemStr("secu_code"))) {
      errmsg("請指定 [傳送類別]");
      return;
    }

  }

  boolean checkSendDate() {
    String sql1 = "select " + wp.sqlID + " uf_nvl(send_date,'') as ls_send_date "
        + " from tsc_refuse_log " + " where 1=1 " + commSqlStr.whereRowid(cardNo);
    sqlSelect(sql1);

    if (!empty(colStr("ls_send_date")))
      return false;
    return true;

  }

  boolean checkTscCard() {

    String sql2 = "select "
        + " decode(sum(decode(risk_class,'57',1,0)),null,0,sum(decode(risk_class,'57',1,0))) as ll_cnt57 , "
        + " decode(sum(decode(risk_class,'04',1,0)),null,0,sum(decode(risk_class,'04',1,0))) as ll_cnt04  "
        + " from tsc_rm_actauth " + " where card_no =?" + " and risk_class in ('04','57') ";
    sqlSelect(sql2, new Object[] {cardNo});

    if (colNum("ll_cnt57") > 0) {
      wp.colSet("db_tsc57", "Y");
    } else {
      wp.colSet("db_tsc57", "N");
    }

    if (colNum("ll_cnt04") > 0) {
      wp.colSet("db_tsc04", "Y");
    } else {
      wp.colSet("db_tsc04", "N");
    }

    String sql1 = "select tsc_card_no, current_code, new_end_date " + " from tsc_card "
        + " where card_no =? " + " order by new_end_date,current_code desc fetch first 1 row only ";
    sqlSelect(sql1, new Object[] {cardNo});

    if (sqlRowNum <= 0) {
      return false;
    }
    return true;
  }

  boolean checkLog() {
    String sql1 = "select count(*) as db_cnt " + " from tsc_refuse_log " + " where card_no =? "
        + " and send_date=''";
    sqlSelect(sql1, new Object[] {cardNo});
    if (colNum("db_cnt") > 0)
      return false;
    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into tsc_refuse_log (" + " crt_date ," + " crt_time ," + " card_no ,"
        + " secu_code ," + " risk_remark ," + " crt_user ," + " send_date ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ("
        + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " :kk1 ,"
        + " :secu_code ," + " :risk_remark ," + " :crt_user ," + " '' ," + " :mod_user ,"
        + " sysdate ," + " :mod_pgm ," + " 1 " + " )";

    setString("kk1", cardNo);
    item2ParmStr("secu_code");
    item2ParmStr("risk_remark");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2240");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert tsc_refuse_log error, " + getMsg());
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update tsc_refuse_log set " + " secu_code =:secu_code ,"
        + " risk_remark =:risk_remark ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 "
        + " and hex(rowid) =:rowid" + " and mod_seqno =:mod_seqno ";

    item2ParmStr("secu_code");
    item2ParmStr("risk_remark");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2240");
    item2ParmStr("rowid");
    item2ParmNum("mod_seqno");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Update tsc_refuse_log error, " + getMsg());
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "Delete tsc_refuse_log" + " where hex(rowid) =:kk2 " + " and mod_seqno=:mod_seqno ";
    setString("kk2", rowid);
    item2ParmNum("mod_seqno");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete tsc_refuse_log err=" + getMsg());
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
