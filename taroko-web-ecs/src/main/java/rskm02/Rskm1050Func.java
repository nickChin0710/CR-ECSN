/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;
/** 2019-0619:  JH    p_xxx >>acno_pxxx
 * */
import busi.FuncAction;

public class Rskm1050Func extends FuncAction {

  @Override
  public void dataCheck() {
    if (!empty(wp.itemStr("A1_apr_date"))) {
      errmsg("資料已覆核, 不可再異動");
      return;
    }

    if (empty(wp.itemStr("A1_action_code"))) {
      errmsg("ACTION CODE 不可空白");
      return;
    }

    if (empty(wp.itemStr("A1_loan_flag"))) {
      errmsg("LOAN覆審決策 不可空白");
      return;
    }

    if (!empty(wp.itemStr("block_reason")) && !empty(wp.itemStr("block_reason5"))) {
      errmsg("凍結碼[4,5] 不可同時有值");
      return;
    }

    if (!empty(wp.itemStr("block_reason5")) && eqIgno(wp.itemStr("block_reason5"), "81")) {
      errmsg("凍結碼[5] 只可為 空白 or 81");
      return;
    }

    // --2019/09/27 User 修正
    if (eqIgno(wp.itemStr("A1_action_code"), "0")) {
      // --0.原額用卡>>凍結碼【4】、凍結碼【5】及戶特指不能有值，若有值請出現錯誤訊息。
      if (wp.itemEmpty("A1_block_reason") == false || wp.itemEmpty("A1_block_reason5") == false
          || wp.itemEmpty("A1_spec_status") == false) {
        errmsg("凍結碼【4】、凍結碼【5】及戶特指不能有值");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "1")) {
      // --1.調降額度-未降足額度者凍結>>檢視降額原因碼及凍結碼 不可為空白
      if ((wp.itemEmpty("A1_block_reason") && wp.itemEmpty("A1_block_reason5"))
          || wp.itemEmpty("A1_adj_credit_limit_reason")) {
        errmsg("降額原因碼及凍結碼 不可為空白");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "2")) {
      // --2.調降額度-未降足額度者維護特指>>檢視降額原因碼 不可為空白
      if (wp.itemEmpty("A1_adj_credit_limit_reason")) {
        errmsg("降額原因碼 不可為空白");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "3")) {
      // --3.調整額度>>檢視降額原因碼 不可為空白
      if (wp.itemEmpty("A1_adj_credit_limit_reason")) {
        errmsg("降額原因碼 不可為空白");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "4")) {
      // --4.調整額度-卡戶凍結(個繳)>>檢視降額原因碼及凍結碼 不可為空白
      if ((wp.itemEmpty("A1_block_reason") && wp.itemEmpty("A1_block_reason5"))
          || wp.itemEmpty("A1_adj_credit_limit_reason")) {
        errmsg("降額原因碼及凍結碼 不可為空白");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "5")) {
      // --5.調降額度-維護特指)>>檢視降額原因碼 不可為空白
      if (wp.itemEmpty("A1_adj_credit_limit_reason")) {
        errmsg("降額原因碼 不可為空白");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "6")) {
      // --6.卡戶凍結【4】>>更名：卡戶凍結【4】【5】>>凍結碼【4】、凍結碼【5】則一輸入，否則出現錯誤訊息＂凍結碼【4】或【5】不可為空白＂。
      if (wp.itemEmpty("A1_block_reason") && wp.itemEmpty("A1_block_reason5")) {
        errmsg("凍結碼【4】或【5】不可為空白");
        return;
      }
    } else if (eqIgno(wp.itemStr("A1_action_code"), "7")) {
      // --7.卡片維護特指
    } else if (eqIgno(wp.itemStr("A1_action_code"), "8")) {
      // --8.額度內用卡>>(1)戶特指自動代入81。(2)凍結碼【4】、凍結碼【5】不能有值。否則出現錯誤訊息＂凍結碼不可有值＂。＊＊原戶特指欄位空白才上81，否則不處理。
      if (wp.itemEmpty("A1_block_reason") == false || wp.itemEmpty("A1_block_reason5") == false) {
        errmsg("凍結碼不可有值");
        return;
      }

      if (wp.itemEmpty("A1_spec_status")) {
        wp.itemSet("A1_spec_status", "81");
      }

    }

    try {
      checkList();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update rsk_trial_list set " + " trial_type ='1' ,"
        + " trial_date =to_char(sysdate,'yyyymmdd') ," + " trial_user =:trial_user ,"
        + " action_code =:action_code ," + " action_date =to_char(sysdate,'yyyymmdd') ,"
        + " trial_remark =:trial_remark ," + " trial_remark2 =:trial_remark2 ,"
        + " trial_remark3 =:trial_remark3  ," + " adj_credit_limit_rate =:adj_credit_limit_rate ,"
        + " adj_credit_limit_reason =:adj_credit_limit_reason ,"
        + " adj_credit_limit_remain =:adj_credit_limit_remain ," + " block_reason =:block_reason ,"
        + " block_reason5 =:block_reason5 ," + " spec_status  =:spec_status ,"
        + " close_flag ='1' ," + " close_user =:close_user ,"
        + " close_date =to_char(sysdate,'yyyymmdd') ," + " loan_flag =:loan_flag ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where rowid =:rowid ";
    setString("trial_user", wp.loginUser);
    item2ParmStr("action_code", "A1_action_code");
    item2ParmStr("trial_remark", "A1_trial_remark");
    item2ParmStr("trial_remark2", "A1_trial_remark2");
    item2ParmStr("trial_remark3", "A1_trial_remark3");
    item2ParmNum("adj_credit_limit_rate", "A1_adj_credit_limit_rate");
    item2ParmStr("adj_credit_limit_reason", "A1_adj_credit_limit_reason");
    item2ParmNum("adj_credit_limit_remain", "A1_adj_credit_limit_remain");
    item2ParmStr("block_reason", "A1_block_reason");
    item2ParmStr("block_reason5", "A1_block_reason5");
    item2ParmStr("spec_status", "A1_spec_status");
    setString("close_user", wp.loginUser);
    item2ParmStr("loan_flag", "A1_loan_flag");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm1050");
    setRowId("rowid", wp.itemStr("A1_ROWID"));
    log("A:" + wp.itemStr("A1_rowid"));
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_list error");
      return rc;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    msgOK();
    cancelList();
    if (rc != 1)
      return rc;
    deleteLog();



    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  // --取消人工ACTION

  public int cancelList() {
    msgOK();

    strSql = " update rsk_trial_list set " + " trial_type = '' ," + " trial_date = '' ,"
        + " trial_user = '' ," + " action_code = '' ," + " trial_remark = '' ,"
        + " adj_credit_limit_rate = 0 ," + " adj_credit_limit_reason = '' ,"
        + " adj_credit_limit_remain = 0 ," + " block_reason = '' ," + " action_date = '' ,"
        + " close_flag = 'N' ," + " close_user = '' ," + " close_date = '' ,"
        + " block_reason5 = '' ," + " loan_flag = '0' ," + " spec_status = '' ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm ='rskm1050' ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where rowid =:rowid "
    // + " and mod_seqno =:mod_seqno "
    ;
    log("B:" + wp.itemStr("A1_rowid"));
    setString("mod_user", wp.loginUser);
    setRowId("rowid", wp.itemStr("A1_rowid"));
    // setString("mod_seqno",wp.item_ss("A1_mod_seqno"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("取消人工ACTION 失敗; err=" + this.sqlErrtext);
    }
    return rc;
  }

  public int deleteLog() {
    msgOK();

    strSql = " delete rsk_trial_action_log " + " where batch_no =:batch_no "
        + " and id_p_seqno =:id_p_seqno ";
    item2ParmStr("batch_no", "A1_batch_no");
    item2ParmStr("id_p_seqno", "A1_id_p_seqno");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete RSK_TRIAL_ACTION_LOG error");
    } else
      rc = 1;
    return rc;
  }

  // -- 存檔

  public int procList() throws Exception {
    msgOK();

    strSql = " insert into rsk_trial_action_log (" + " batch_no ," + " id_p_seqno ,"
        + " acno_p_seqno ," + " action_code ," + " action_date ," + " close_flag ,"
        + " close_date ," + " credit_limit_bef ," + " credit_limit_aft ," + " block_reason4 ,"
        + " acct_type ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno ,"
        + " msg_flag ," + " block_reason5 ," + " spec_status ," + " card_curr_cnt ,"
        + " sup_curr_cnt " + " ) values ( " + " :batch_no ," + " :id_p_seqno ," + " :acno_p_seqno ,"
        + " :action_code ," + " to_char(sysdate,'yyyymmdd') ," + " '1' ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :credit_limit_bef ," + " :credit_limit_aft ,"
        + " :block_reason4 ," + " :acct_type ," + " :mod_user ," + " sysdate ," + " :mod_pgm ,"
        + " 1 ," + " '' ," + " :block_reason5 ," + " :spec_status ," + " :card_curr_cnt ,"
        + " :sup_curr_cnt " + " ) ";
    setString("batch_no", wp.itemStr("A1_batch_no"));
    setString("id_p_seqno", wp.itemStr("A1_id_p_seqno"));
    var2ParmStr("acno_p_seqno");
    var2ParmStr("action_code");
    var2ParmNum("credit_limit_bef");
    var2ParmNum("credit_limit_aft");
    var2ParmStr("block_reason4");
    var2ParmStr("acct_type");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm1050");
    var2ParmNum("card_curr_cnt");
    var2ParmNum("sup_curr_cnt");
    var2ParmStr("block_reason5");
    item2ParmStr("spec_status", "A1_spec_status");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }


  public int checkList() throws Exception {
    if (wp.itemNum("list_cnt") == 0) {
      errmsg("未顯示 卡人帳戶資料");
      return rc;
    }
    int ilRowCnt = (int) wp.itemNum("list_cnt");
    int lmBef = 0, lmAft = 0, lmLimitBef = 0, lmLimitAft = 0;

    for (int ii = 0; ii < ilRowCnt; ii++) {
      lmLimitBef = (int) wp.colNum(ii, "A2_credit_limit_bef");
      lmLimitAft = (int) wp.colNum(ii, "A2_credit_limit_aft");
      if (lmLimitBef <= 0) {
        errmsg("調整後額度 不可 <=0");
        return rc;
      }

      if (lmLimitAft > lmLimitBef) {
        errmsg("額度 不可調升");
        return rc;
      }

      if (pos("|1|2|3|4|5", wp.itemStr("A1_action_code")) == 0 && lmLimitAft != lmLimitBef) {
        errmsg("非調整額度, 原額度及調整後額度 不可不相等");
        return rc;
      }

      lmBef += lmLimitBef;
      lmAft += lmLimitAft;
    }

    if (pos("|1|2|3|4|5", wp.itemStr("A1_action_code")) > 0 && lmBef <= lmAft) {
      errmsg("額度調整: [調整後] 不可>= [調整前]");
      return rc;
    }


    return rc;
  }

}
