package colm05;
/** 凍結、解凍、額度調整、強停例外維護
 * 19-1206:   Alex  no_adj_loc_high_s_date fix
 * 19-1129:   Alex  all empty no reason
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * -V.2019-0408
 * 109-05-06  V1.00.04  Tanwei       updated for project coding standard
 * 110-01-05  V1.00.05  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *      
 *
 * */


public class Colm5910Func extends busi.FuncAction {

  // public Colm5910Func(taroko.com.TarokoCommon wr) {
  // this.wp = wr;
  // this.conn = wp.getConn();
  // mod_user =wp.loginUser;
  // mod_pgm =wp.mod_pgm();
  // }

  // public int dataSelect() {
  // String ls_p_seqno = wp.col_ss("p_seqno");
  // if (empty(ls_p_seqno)) {
  // errmsg("帳戶帳號: 不可空白");
  // return -1;
  // }
  // return 1;
  // }

  @Override
  public void dataCheck() {
    if (wp.itemEmpty("acct_type") || wp.itemEmpty("acct_key") || wp.itemEmpty("p_seqno")
        || wp.itemEmpty("acno_p_seqno")) {
      errmsg("[帳戶類別, 帳戶帳號] 不可空白");
      return;
    }

    String lsSysdate = wp.sysDate;
    boolean lbReason = false;
    if (wp.itemEq("ex_no_block", "Y")) {
      if (wp.itemEq("no_block_flag", "Y") || wp.itemEmpty("no_block_s_date") == false
          || wp.itemEmpty("no_block_e_date") == false)
        lbReason = true;
      if (wp.itemEq("no_block_flag", "Y")) {
        if (wp.itemEmpty("no_block_s_date") || wp.itemEmpty("no_block_e_date")) {
          errmsg("不可凍結:生效起迄日需有值");
          return;
        }
        if (chkStrend(lsSysdate, wp.itemStr2("no_block_s_date")) == -1) {
          errmsg("不可凍結:生效日期需大於等於建檔日期");
          return;
        }
        if (chkStrend(wp.itemStr2("no_block_s_date"), wp.itemStr2("no_block_e_date")) == -1) {
          errmsg("不可凍結:生效日期起迄錯誤");
          return;
        }
      }
    }

    if (wp.itemEq("ex_no_unblock", "Y")) {
      if (wp.itemEq("no_unblock_flag", "Y") || wp.itemEmpty("no_unblock_s_date") == false
          || wp.itemEmpty("no_unblock_e_date") == false)
        lbReason = true;
      if (wp.itemEq("no_unblock_flag", "Y")) {
        if (wp.itemEmpty("no_unblock_s_date") || wp.itemEmpty("no_unblock_e_date")) {
          errmsg("不可解凍:生效起迄日需有值");
          return;
        }
        if (chkStrend(lsSysdate, wp.itemStr2("no_unblock_s_date")) == -1) {
          errmsg("不可解凍:生效日期需大於等於建檔日期");
          return;
        }
        if (chkStrend(wp.itemStr2("no_unblock_s_date"), wp.itemStr2("no_unblock_e_date")) == -1) {
          errmsg("不可解凍:生效日期起迄錯誤");
          return;
        }
      }
    }

    if (wp.itemEq("ex_no_high", "Y")) {
      if (wp.itemEq("no_adj_loc_high", "Y") || wp.itemEmpty("no_adj_loc_high_s_date") == false
          || wp.itemEmpty("no_adj_loc_high_e_date") == false)
        lbReason = true;
      if (wp.itemEq("no_adj_loc_high", "Y")) {
        if (wp.itemEmpty("no_adj_loc_high_s_date") || wp.itemEmpty("no_adj_loc_high_e_date")) {
          errmsg("不可調高:生效起迄日需有值");
          return;
        }
        if (chkStrend(lsSysdate, wp.itemStr2("no_adj_loc_high_s_date")) == -1) {
          errmsg("不可調高:生效日期需大於等於建檔日期");
          return;
        }
        if (chkStrend(wp.itemStr2("no_adj_loc_high_s_date"),
            wp.itemStr2("no_adj_loc_high_e_date")) == -1) {
          errmsg("不可調高:生效日期起迄錯誤");
          return;
        }
      }
    }

    if (wp.itemEq("ex_no_low", "Y")) {
      if (wp.itemEq("no_adj_loc_low", "Y") || wp.itemEmpty("no_adj_loc_low_s_date") == false
          || wp.itemEmpty("no_adj_loc_low_e_date") == false)
        lbReason = true;
      if (wp.itemEq("no_adj_loc_low", "Y")) {
        if (wp.itemEmpty("no_adj_loc_low_s_date") || wp.itemEmpty("no_adj_loc_low_e_date")) {
          errmsg("不可調低:生效起迄日需有值");
          return;
        }
        if (chkStrend(lsSysdate, wp.itemStr2("no_adj_loc_low_s_date")) == -1) {
          errmsg("不可調低:生效日期需大於等於建檔日期");
          return;
        }
        if (chkStrend(wp.itemStr2("no_adj_loc_low_s_date"),
            wp.itemStr2("no_adj_loc_low_e_date")) == -1) {
          errmsg("不可調低:生效日期起迄錯誤");
          return;
        }
      }
    }

    if (wp.itemEq("ex_no_stop", "Y")) {
      if (wp.itemEq("no_f_stop_flag", "Y") || wp.itemEmpty("no_f_stop_s_date") == false
          || wp.itemEmpty("no_f_stop_e_date") == false)
        lbReason = true;
      if (wp.itemEq("no_f_stop_flag", "Y")) {
        if (wp.itemEmpty("no_f_stop_s_date") || wp.itemEmpty("no_f_stop_e_date")) {
          errmsg("不可強停:生效起迄日需有值");
          return;
        }
        if (chkStrend(lsSysdate, wp.itemStr2("no_f_stop_s_date")) == -1) {
          errmsg("不可強停:生效日期需大於等於建檔日期");
          return;
        }
        if (chkStrend(wp.itemStr2("no_f_stop_s_date"), wp.itemStr2("no_f_stop_e_date")) == -1) {
          errmsg("不可強停:生效日期起迄錯誤");
          return;
        }
      }
    }

    if (wp.itemEq("ex_no_high_cash", "Y")) {
      if (wp.itemEq("no_adj_h_cash", "Y") || wp.itemEmpty("no_adj_h_s_date_cash") == false
          || wp.itemEmpty("no_adj_h_e_date_cash") == false)
        lbReason = true;
      if (wp.itemEq("no_adj_h_cash", "Y")) {
        if (wp.itemEmpty("no_adj_h_s_date_cash") || wp.itemEmpty("no_adj_h_e_date_cash")) {
          errmsg("不可調高額度(預借現金):生效起迄日需有值");
          return;
        }
        if (chkStrend(lsSysdate, wp.itemStr2("no_adj_h_s_date_cash")) == -1) {
          errmsg("不可調高額度(預借現金):生效日期需大於等於建檔日期");
          return;
        }
        if (chkStrend(wp.itemStr2("no_adj_h_s_date_cash"),
            wp.itemStr2("no_adj_h_e_date_cash")) == -1) {
          errmsg("不可調高額度(預借現金):生效日期起迄錯誤");
          return;
        }
      }
    }

    if (lbReason && wp.itemEmpty("chg_reason")) {
      errmsg("原因碼: 不可空白");
      return;
    }

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    deleteActDualAcno();
    if (rc == -1)
      return rc;
    insertActDualAcno();
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    deleteActDualAcno();
    return rc;
  }

  @Override
  public int dataProc() {
    return 0;
  }

  private void insertActDualAcno() {

    String lsItem = wp.itemNvl("ex_no_block", "N") + wp.itemNvl("ex_no_unblock", "N")
        + wp.colNvl("ex_no_high", "N") + wp.colNvl("ex_no_low", "N") + wp.colNvl("ex_no_stop", "N")
        + wp.colNvl("ex_no_high_cash", "N");

    sql2Insert("act_dual_acno");
    addsqlParm("?", "p_seqno", wp.itemStr2("acno_p_seqno"));
    addsqlParm(", func_code", ",'0800'");
    addsqlParm(",?", ", acct_type", wp.itemStr2("acct_type"));
    addsqlParm(",?", ", acct_key", wp.itemStr2("acct_key"));
    addsqlParm(", aud_type", ", 'A'");
    addsqlParm(",?", ", aud_item", lsItem);

    if (wp.itemEq("ex_no_block", "Y")) {
      addsqlParm(",?", ", no_block_flag", wp.itemNvl("no_block_flag", "N"));
      addsqlParm(",?", ", no_block_s_date", wp.itemStr2("no_block_s_date"));
      addsqlParm(",?", ", no_block_e_date", wp.itemStr2("no_block_e_date"));
    }
    if (wp.itemEq("ex_no_unblock", "Y")) {
      addsqlParm(",?", ", no_unblock_flag", wp.itemNvl("no_unblock_flag", "N"));
      addsqlParm(",?", ", no_unblock_s_date", wp.itemStr2("no_unblock_s_date"));
      addsqlParm(",?", ", no_unblock_e_date", wp.itemStr2("no_unblock_e_date"));
    }
    if (wp.itemEq("ex_no_high", "Y")) {
      addsqlParm(",?", ", no_adj_loc_high", wp.itemNvl("no_adj_loc_high", "N"));
      addsqlParm(",?", ", no_adj_loc_high_s_date", wp.itemStr2("no_adj_loc_high_s_date"));
      addsqlParm(",?", ", no_adj_loc_high_e_date", wp.itemStr2("no_adj_loc_high_e_date"));
    }
    if (wp.itemEq("ex_no_low", "Y")) {
      addsqlParm(",?", ", no_adj_loc_low", wp.itemNvl("no_adj_loc_low", "N"));
      addsqlParm(",?", ", no_adj_loc_low_s_date", wp.itemStr2("no_adj_loc_low_s_date"));
      addsqlParm(",?", ", no_adj_loc_low_e_date", wp.itemStr2("no_adj_loc_low_e_date"));
    }
    if (wp.itemEq("ex_no_stop", "Y")) {
      addsqlParm(",?", ", no_f_stop_flag", wp.itemNvl("no_f_stop_flag", "N"));
      addsqlParm(",?", ", no_f_stop_s_date", wp.itemStr2("no_f_stop_s_date"));
      addsqlParm(",?", ", no_f_stop_e_date", wp.itemStr2("no_f_stop_e_date"));
    }
    if (wp.itemEq("ex_no_high_cash", "Y")) {
      addsqlParm(",?", ", no_adj_h_cash", wp.itemNvl("no_adj_h_cash", "N"));
      addsqlParm(",?", ", no_adj_h_s_date_cash", wp.itemStr2("no_adj_h_s_date_cash"));
      addsqlParm(",?", ", no_adj_h_e_date_cash", wp.itemStr2("no_adj_h_e_date_cash"));
    }
    addsqlParm(",?", ", spec_reason", wp.itemStr2("chg_reason"));
    addsqlParm(",?", ", chg_remark", wp.itemStr2("chg_remark"));
    addsqlYmd(", chg_date");
    addsqlParm(",?", ", chg_user", modUser);
    addsqlModXXX(modUser, modPgm);
    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum != 1) {
      sqlErr("insert act_dual_acno");
    }
    // tt_dual.aaa(", func_code", ",'0800'");
    // tt_dual.aaa(",?", ", acct_type", wp.sss("acct_type"));
    // tt_dual.aaa(",?", ", acct_key", wp.sss("acct_key"));
    // tt_dual.aaa(", aud_type", ", 'A'");
    // tt_dual.aaa(",?", ", aud_item", ls_item);
    // if (wp.item_eq("ex_no_block", "Y")) {
    // tt_dual.aaa(",?", ", no_block_flag", wp.item_nvl("no_block_flag", "N"));
    // tt_dual.aaa(",?", ", no_block_s_date", wp.sss("no_block_s_date"));
    // tt_dual.aaa(",?", ", no_block_e_date", wp.sss("no_block_e_date"));
    // }
    // if (wp.item_eq("ex_no_unblock", "Y")) {
    // tt_dual.aaa(",?", ", no_unblock_flag", wp.item_nvl("no_unblock_flag", "N"));
    // tt_dual.aaa(",?", ", no_unblock_s_date", wp.sss("no_unblock_s_date"));
    // tt_dual.aaa(",?", ", no_unblock_e_date", wp.sss("no_unblock_e_date"));
    // }
    // if (wp.item_eq("ex_no_high", "Y")) {
    // tt_dual.aaa(",?", ", no_adj_loc_high", wp.item_nvl("no_adj_loc_high", "N"));
    // tt_dual.aaa(",?", ", no_adj_loc_high_s_date", wp.sss("no_adj_loc_high_s_date"));
    // tt_dual.aaa(",?", ", no_adj_loc_high_e_date", wp.sss("no_adj_loc_high_e_date"));
    // }
    // if (wp.item_eq("ex_no_low", "Y")) {
    // tt_dual.aaa(",?", ", no_adj_loc_low", wp.item_nvl("no_adj_loc_low", "N"));
    // tt_dual.aaa(",?", ", no_adj_loc_low_s_date", wp.sss("no_adj_loc_low_s_date"));
    // tt_dual.aaa(",?", ", no_adj_loc_low_e_date", wp.sss("no_adj_loc_low_e_date"));
    // }
    // if (wp.item_eq("ex_no_stop", "Y")) {
    // tt_dual.aaa(",?", ", no_f_stop_flag", wp.item_nvl("no_f_stop_flag", "N"));
    // tt_dual.aaa(",?", ", no_f_stop_s_date", wp.sss("no_f_stop_s_date"));
    // tt_dual.aaa(",?", ", no_f_stop_e_date", wp.sss("no_f_stop_e_date"));
    // }
    // if (wp.item_eq("ex_no_high_cash", "Y")) {
    // tt_dual.aaa(",?", ", no_adj_h_cash", wp.item_nvl("no_adj_h_cash", "N"));
    // tt_dual.aaa(",?", ", no_adj_h_s_date_cash", wp.sss("no_adj_h_s_date_cash"));
    // tt_dual.aaa(",?", ", no_adj_h_e_date_cash", wp.sss("no_adj_h_e_date_cash"));
    // }
    // tt_dual.aaa(",?", ", spec_reason", wp.sss("chg_reason"));
    // tt_dual.aaa(",?", ", chg_remark", wp.sss("chg_remark"));
    // tt_dual.aaa(", chg_date", "," + commSqlStr.sys_YYmd);
    // tt_dual.aaa(",?", ", chg_user", mod_user);
    // tt_dual.mod_XXX(mod_user, mod_pgm);
    // sqlExec(tt_dual.sql_stmt(), tt_dual.sql_parm());
    // if (sql_nrow != 1) {
    // sql_err("insert act_dual_acno");
    // }
  }

  private void deleteActDualAcno() {

    strSql = "Delete ACT_DUAL_ACNO" + " where func_code ='0800'" + " and p_seqno =?";
    setString2(1, wp.itemStr2("p_seqno"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ACT_DUAL_ACNO err; " + getMsg());
    }
    return;
  }


}
