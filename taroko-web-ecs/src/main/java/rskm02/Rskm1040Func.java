/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskm1040Func extends FuncAction {

  String batchNo = "", riskGroup = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      batchNo = wp.itemStr("kk_batch_no");
    } else {
      batchNo = wp.itemStr("batch_no");
    }

    if (this.ibAdd) {
      riskGroup = wp.itemStr("kk_risk_group");
    } else {
      riskGroup = wp.itemStr("risk_group");
    }
    if (ibDelete) {
      if (wp.itemEq("apr_flag", "Y")) {
        errmsg("已覆核不可異動");
        return;
      }
    }

    if (wp.itemNum("delay_action_day") < 0) {
      errmsg("延遲執行天數 不可小於 0");
      return;
    }

    if (wp.itemEq("delay_msg_flag", "Y") && wp.itemNum("delay_action_day") <= 1) {
      errmsg("發送延遲簡訊時, 延遲執行天數 須大於 1");
      return;
    }

    if (wp.itemEq("action_code", "0") || wp.itemEq("action_code", "7")) {
      wp.itemSet("adj_limit_rate", "" + 0);
      wp.itemSet("adj_limit_reason", "");
      wp.itemSet("block_reason4", "");
      wp.itemSet("block_reason5", "");
    } else if (wp.itemEq("action_code", "1") || wp.itemEq("action_code", "4")) {
      if (wp.itemNum("adj_limit_rate") == 0 || wp.itemEmpty("adj_limit_reason")
          || wp.itemEmpty("block_reason4")) {
        errmsg("降額比例, 原因碼及凍結碼 不可為 0 或 空白");
        return;
      }
    } else if (wp.itemEq("action_code", "2") || wp.itemEq("action_code", "3")
        || wp.itemEq("action_code", "5")) {
      if (wp.itemNum("adj_limit_rate") == 0 || wp.itemEmpty("adj_limit_reason")) {
        errmsg("降額比例, 原因碼 不可為 0 或 空白");
        return;
      }
    } else if (wp.itemEq("action_code", "6")) {
      if (wp.itemEmpty("block_reason4")) {
        errmsg("凍結碼[4] 不可為空白");
        return;
      }
    } else if (wp.itemEq("action_code", "8")) {
      if (wp.itemEmpty("spec_status")) {
        errmsg("戶特指 不可為空白");
        return;
      }
    }
    if (wp.itemEmpty("block_reason4") == false && wp.itemEmpty("block_reason5") == false) {
      errmsg("凍結碼[4],[5] 不可同時有值");
      return;
    }

    if (this.ibAdd) {
      return;
    }

    sqlWhere =
        " where 1=1 " + " and batch_no =?" + " and risk_group =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {batchNo, riskGroup, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("RSK_TRIAL_ACTION", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    insertData();
    if (rc != 1)
      return rc;
    actionDesc();
    return rc;
  }

  public int insertData() {
    strSql = "insert into RSK_TRIAL_ACTION (" + "batch_no," + "risk_group," + "action_code,"
        + "adj_limit_rate," + "adj_limit_reason," + "block_reason4," + "msg_flag,"
        + "delay_action_day," + "delay_msg_flag," + "block_reason5," + "loan_flag," + "spec_status,"
        + "crt_user," + "crt_date," + "apr_flag," + "apr_date," + "apr_user," + "mod_user,"
        + "mod_time," + "mod_pgm," + "mod_seqno" + " ) values (" + " :kk1," + " :kk2,"
        + ":action_code," + ":adj_limit_rate," + ":adj_limit_reason," + ":block_reason4,"
        + ":msg_flag," + ":delay_action_day," + ":delay_msg_flag," + ":block_reason5,"
        + ":loan_flag," + ":spec_status," + ":crt_user," + "to_char(sysdate,'yyyymmdd')," + "'N',"
        + "''," + "''," + ":mod_user," + "sysdate," + ":mod_pgm," + "1" + " )";
    // -set ?value-
    try {
      setString("kk1", batchNo);
      setString("kk2", riskGroup);
      item2ParmStr("action_code");
      item2ParmNum("adj_limit_rate");
      item2ParmStr("adj_limit_reason");
      item2ParmStr("block_reason4");
      item2ParmStr("msg_flag");
      item2ParmNum("delay_action_day");
      item2ParmStr("delay_msg_flag");
      item2ParmStr("block_reason5");
      item2ParmStr("loan_flag");
      item2ParmStr("spec_status");
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.log("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;

  }

  public int deleteData() {
    strSql = "delete RSK_TRIAL_ACTION " + " where batch_no =:kk1 " + " and risk_group =:kk2"
        + " and apr_flag='N'" + " and nvl(mod_seqno,0) =:mod_seqno ";
    setString("kk1", batchNo);
    setString("kk2", riskGroup);
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else {
      rc = 1;
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
    deleteData();
    if (rc != 1)
      return rc;
    insertData();
    if (rc != 1)
      return rc;
    actionDesc();
    return rc;
  }

  void actionDesc() {
    String sql1 = " select " + " count(*) as db_cnt " + " from ptr_sys_idtab "
        + " where wf_type = 'RSK_ACTION_VERSION' " + " and wf_id = ? ";

    sqlSelect(sql1, new Object[] {batchNo});

    if (sqlRowNum < 0) {
      errmsg("版本說明新增錯誤!");
      return;
    }

    if (sqlRowNum == 0 || colNum("db_cnt") == 0) {
      inertActionDesc();
    } else {
      updateActionDesc();
    }

  }

  int inertActionDesc() {
    msgOK();
    strSql = " insert into ptr_sys_idtab (" + " wf_type , " + " wf_id , " + " wf_desc , "
        + " wf_useredit , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values ( " + " 'RSK_ACTION_VERSION' , " + " :wf_id , " + " :wf_desc , " + " 'Y' , "
        + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    setString("wf_id", batchNo);
    setString("wf_desc", wp.itemStr("action_desc"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm1040");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ptr_sys_idtab error !");
    }
    return rc;
  }

  int updateActionDesc() {
    msgOK();
    strSql = " update ptr_sys_idtab set " + " wf_desc =:wf_desc , " + " mod_user =:mod_user , "
        + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where wf_type ='RSK_ACTION_VERSION' " + " and wf_id =:wf_id ";

    setString("wf_desc", wp.itemStr("action_desc"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm1040");
    setString("wf_id", batchNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_sys_idtab error !");
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
    deleteData();
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
