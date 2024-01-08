package colm05;
/** 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.01  Tanwei       updated for project coding standard
 * * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 * */
import busi.FuncAction;

public class Colp5910Func extends busi.FuncAction {

  String acnopSeqno = "";

  @Override
  public void dataCheck() {
    int ll = varsInt("proc_row");
    if (ll < 0) {
      errmsg("未指定覆核資料");
      return;
    }

    acnopSeqno = wp.itemStr(ll, "acno_p_seqno");
    double lmModSeqno = wp.itemNum(ll, "mod_seqno");

    strSql = "select * from act_dual_acno" + " where p_seqno =? and func_code ='0800'"
        + " and mod_seqno =?";
    setString2(1, acnopSeqno);
    setDouble(lmModSeqno);
    sqlSelect(strSql);
    if (sqlRowNum != 1) {
      errmsg("資料不存在 OR 已被修改");
      return;
    }
  }

  @Override
  public int dbInsert() {
    return 0;
  }

  @Override
  public int dbUpdate() {
    return 0;
  }

  @Override
  public int dbDelete() {
    return 0;
  }

  @Override
  public int dataProc() {
    msgOK();
    dataCheck();
    if (rc != 1)
      return rc;

    String lsItem = colStr("aud_item");
    sql2Update("act_acno");
    addsqlModXXX(colStr("chg_user"), modPgm);
    if (eq(lsItem.substring(0, 1), "Y")) {
      addsqlParm(", no_block_flag =?", colStr("no_block_flag"));
      addsqlParm(", no_block_s_date =?", colStr("no_block_s_date"));
      addsqlParm(", no_block_e_date =?", colStr("no_block_e_date"));
    }
    if (eq(lsItem.substring(1, 2), "Y")) {
      addsqlParm(", no_unblock_flag =?", colStr("no_unblock_flag"));
      addsqlParm(", no_unblock_s_date =?", colStr("no_unblock_s_date"));
      addsqlParm(", no_unblock_e_date =?", colStr("no_unblock_e_date"));
    }
    if (eq(lsItem.substring(2, 3), "Y")) {
      addsqlParm(", no_adj_loc_high =?", colStr("no_adj_loc_high"));
      addsqlParm(", no_adj_loc_high_s_date =?", colStr("no_adj_loc_high_s_date"));
      addsqlParm(", no_adj_loc_high_e_date =?", colStr("no_adj_loc_high_e_date"));
    }
    if (eq(lsItem.substring(3, 4), "Y")) {
      addsqlParm(", no_adj_loc_low =?", colStr("no_adj_loc_low"));
      addsqlParm(", no_adj_loc_low_s_date =?", colStr("no_adj_loc_low_s_date"));
      addsqlParm(", no_adj_loc_low_e_date =?", colStr("no_adj_loc_low_e_date"));
    }
    if (eq(lsItem.substring(4, 5), "Y")) {
      addsqlParm(", no_f_stop_flag =?", colStr("no_f_stop_flag"));
      addsqlParm(", no_f_stop_s_date =?", colStr("no_f_stop_s_date"));
      addsqlParm(", no_f_stop_e_date =?", colStr("no_f_stop_e_date"));
    }
    if (eq(lsItem.substring(5, 6), "Y")) {
      addsqlParm(", no_adj_h_cash =?", colStr("no_adj_h_cash"));
      addsqlParm(", no_adj_h_s_date_cash =?", colStr("no_adj_h_s_date_cash"));
      addsqlParm(", no_adj_h_e_date_cash =?", colStr("no_adj_h_e_date_cash"));
    }
    sqlWhere(" where acno_p_seqno =?", acnopSeqno);
    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("update ACT_ACNO error; " + this.getMsg());
    }
    if (rc != 1)
      return rc;
    updateActAcnoExt();
    if (rc != 1)
      return rc;

    dbDeleteDual();
    return rc;
  }

  // boolean select_act_acno_ext() {
  // String sql1 = " select "
  // + " count(*) as db_cnt "
  // + " from act_acno_ext "
  // + " where p_seqno = ? ";
  // sqlSelect(sql1, new Object[]{vars_ss("p_seqno")});
  //
  // if (sql_nrow == 0 || col_num("db_cnt") == 0) return false;
  //
  // return true;
  // }

  void insertActAcnoExt() {
    strSql = " insert into act_acno_ext ( " + " p_seqno , " + " spec_reason , " + " spec_remark , "
        + " mod_time , " + " mod_pgm " + " ) values ( " + " ?, ?, ? " + ", sysdate, ?" + " ) ";

    setString2(1, acnopSeqno);
    setString(colStr("spec_reason"));
    setString(colStr("chg_remark"));
    setString(modPgm);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert act_acno_ext error !");
    }
    return;
  }

  void updateActAcnoExt() {
    strSql = " update act_acno_ext set " + " spec_reason =? , " + " spec_remark =? , "
        + " mod_time = sysdate , " + " mod_pgm =? " + " where p_seqno =? ";
    setString2(1, colStr("spec_reason"));
    setString(colStr("chg_remark"));
    setString(modPgm);
    setString(acnopSeqno);

    sqlExec(strSql);
    if (sqlRowNum == 0) {
      insertActAcnoExt();
    }
    return;
  }

  public int dbDeleteDual() {
    msgOK();

    dataCheck();

    strSql = "Delete ACT_DUAL_ACNO" + " where func_code ='0800'" + " and p_seqno =?";
    setString2(1, acnopSeqno);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ACT_DUAL_ACNO err; " + getMsg());
    }

    return rc;
  }

}
