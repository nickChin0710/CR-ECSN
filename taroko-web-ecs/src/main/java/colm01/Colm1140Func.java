/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109-05-06  V1.00.00  Aoyulan       updated for project coding standard     *
******************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm1140Func extends FuncEdit {

  public Colm1140Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TOD11111
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    /*
     * if (this.isAdd()) { //檢查新增資料是否重複 String lsSql =
     * "select count(*) as tot_cnt from ptr_actgeneral_n where 1=1 and acct_type= '"+wp.item_ss(
     * "kk_acct_type")+"' "; sqlSelect(lsSql, null); if (col_num("tot_cnt") > 0) {
     * errmsg("資料已存在，無法新增"); } return; } // -other modify- /*sql_where =
     * " where 1=1 and batchno = ?  and recno=?" + " and nvl(mod_seqno,0) = ?"; Object[] param = new
     * Object[] {wp.item_ss("batchno"),wp.item_ss("recno"), wp.item_ss("mod_seqno_a") }; if
     * (this.other_modify("crd_emboss", sql_where, param)) { errmsg("請重新查詢 !"); return; }
     */

  }

  void dataCheckColLiadZ60() {
    String lsSql =
        "select count(*) as tot_cnt from col_liad_z60 where 1=1 and liad_doc_no= :liad_doc_no ";
    setString("liad_doc_no", varsStr("liad_doc_no"));
    sqlSelect(lsSql, null);
    if (colNum("tot_cnt") > 0) {
      errmsg("資料已存在，無法新增");
    }
    return;
  }

  void dataCheckColLiadRenew() {
    String lsSql =
        "select count(*) as tot_cnt from col_liad_renew where 1=1 and liad_doc_no= :liad_doc_no ";
    setString("liad_doc_no", varsStr("liad_doc_no"));
    sqlSelect(lsSql, null);
    if (colNum("tot_cnt") > 0) {
      errmsg("資料已存在，無法新增");
    }
    return;
  }

  void dataCheckColLiadRenewCourt() {
    String lsSql =
        "select count(*) as tot_cnt from col_liad_renew_court where 1=1 and liad_doc_no= :liad_doc_no and liad_doc_seqno = :liad_doc_seqno";
    setString("liad_doc_no", varsStr("liad_doc_no"));
    setString("liad_doc_seqno", varsStr("liad_doc_seqno"));
    sqlSelect(lsSql, null);
    if (colNum("tot_cnt") > 0) {
      errmsg("資料已存在，無法新增");
    }
    return;
  }

  void dataCheckColLiadLiquidateCourt() {
    String lsSql =
        "select count(*) as tot_cnt from col_liad_liquidate_court where 1=1 and liad_doc_no= :liad_doc_no and liad_doc_seqno = :liad_doc_seqno";
    setString("liad_doc_no", varsStr("liad_doc_no"));
    setString("liad_doc_seqno", varsStr("liad_doc_seqno"));
    sqlSelect(lsSql, null);
    if (colNum("tot_cnt") > 0) {
      errmsg("資料已存在，無法新增");
    }
    return;
  }

  void dataCheckColLiadLiquidate() {
    String lsSql =
        "select count(*) as tot_cnt from col_liad_liquidate where 1=1 and liad_doc_no= :liad_doc_no ";
    setString("liad_doc_no", varsStr("liad_doc_no"));
    sqlSelect(lsSql, null);
    if (colNum("tot_cnt") > 0) {
      errmsg("資料已存在，無法新增");
    }
    return;
  }

  void dataCheckColLiadModTmp() {
    String lsSql = "select count(*) as tot_cnt from col_liad_mod_tmp where 1=1 "
        + " and data_type = '" + varsStr("data_type") + "' ";
    if (varsStr("data_type").equals("COLL-DATA") || varsStr("data_type").equals("COLL-CLOSE")) {
      lsSql += " and data_key = rpad( :id_no,10,' ')" + "||rpad( :case_letter,10,' ')";
      setString("id_no", wp.itemStr("liad_doc_no"));
      setString("case_letter", wp.itemStr("case_letter"));
    } else if (varsStr("data_type").equals("INST-MAST")) {
      lsSql += " and data_key = :data_key ";
      setString("data_key", varsStr("ls_idcase"));
    } else if (varsStr("data_type").equals("INST-DETL")) {
      lsSql += " and data_key = :data_key ";
      setString("data_key", varsStr("data_key"));
    } else {
      lsSql += " and data_key = :data_key ";
      setString("data_key", varsStr("liad_doc_no"));
    }
    sqlSelect(lsSql, null);
    if (colNum("tot_cnt") > 0) {
      errmsg("資料已存在，無法新增");
    }
    return;
  }


  @Override
  public int dbInsert() {

    return rc;

  }

  @Override
  public int dbUpdate() {
    return rc;

  }

  @Override
  public int dbDelete() {
    return rc;
  }

  //////////////////////// colm1150 update start////////////////////////////
  public int insertColLiadZ60() {
    dataCheckColLiadZ60();
    if (rc != 1)
      return -1;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liad_z60");
    sp.ppstr("liad_doc_no", varsStr("liad_doc_no"));
    sp.ppstr("id_no", wp.itemStr("id_no"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("recv_date", varsStr("recv_date"));
    sp.ppstr("acct_status", varsStr("acct_status"));
    sp.ppstr("m_code", varsStr("m_code"));
    sp.ppnum("bad_debt_amt", varsNum("bad_debt_amt"));
    sp.ppnum("demand_amt", varsNum("demand_amt"));
    sp.ppnum("debt_amt", varsNum("debt_amt"));
    sp.ppnum("card_num", varsNum("card_num"));
    sp.ppstr("credit_branch", wp.itemStr("credit_branch"));
    sp.ppstr("branch_comb_flag", wp.itemStr("branch_comb_flag"));
    sp.ppstr("court_id", wp.itemStr("court_id"));
    sp.ppstr("court_name", wp.itemStr("court_name"));
    sp.ppnum("case_year", wp.itemNum("case_year"));
    sp.ppstr("case_letter", wp.itemStr("case_letter"));
    sp.ppstr("case_no", wp.itemStr("case_no"));
    sp.ppstr("bullet_date", wp.itemStr("bullet_date"));
    sp.ppstr("bullet_desc", wp.itemStr("bullet_desc"));
    sp.ppstr("data_date", varsStr("data_date"));
    sp.ppstr("renew_flag", varsStr("renew_flag"));
    sp.ppstr("liqui_flag", varsStr("liqui_flag"));
    sp.ppstr("apr_flag", varsStr("apr_flag"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }


  public int deleteColLiadModTmpZ60() {
    strSql = "delete col_liad_mod_tmp where data_type ='Z60' and data_key = ? ";
    Object[] param = new Object[] {varsStr("liad_doc_no")};
    rc = sqlExec(strSql, param);

    return rc;
  }

  public int updateColLiadZ60() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_z60");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where liad_doc_no =?", varsStr("liad_doc_no"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }
  //////////////////////// colm1150 update end////////////////////////////

  //////////////////////// colm1160 update start////////////////////////////
  public int insertColLiadRenew() {
    dataCheckColLiadRenew();
    if (rc != 1)
      return -1;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liad_renew");
    sp.ppstr("liad_doc_no", varsStr("liad_doc_no"));
    sp.ppstr("id_no", wp.itemStr("id_no"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("recv_date", wp.itemStr("recv_date"));
    sp.ppstr("acct_status", varsStr("acct_status"));
    sp.ppstr("m_code", varsStr("m_code"));
    sp.ppnum("bad_debt_amt", varsNum("bad_debt_amt"));
    sp.ppnum("demand_amt", varsNum("demand_amt"));
    sp.ppnum("debt_amt", varsNum("debt_amt"));
    sp.ppnum("acct_num", varsNum("acct_num"));
    sp.ppnum("card_num", varsNum("card_num"));
    sp.ppstr("renew_status", wp.itemStr("renew_status"));
    sp.ppstr("credit_branch", wp.itemStr("credit_branch"));
    sp.ppstr("branch_comb_flag", wp.itemStr("branch_comb_flag"));
    sp.ppstr("max_bank_flag", wp.itemStr("max_bank_flag"));
    sp.ppnum("org_debt_amt", varsNum("org_debt_amt"));
    sp.ppnum("org_debt_amt_bef", varsNum("org_debt_amt_bef"));
    sp.ppnum("org_debt_amt_bef_base", wp.itemNum("org_debt_amt_bef_base"));
    sp.ppnum("renew_lose_amt", varsNum("renew_lose_amt"));
    sp.ppstr("court_id", wp.itemStr("court_id"));
    sp.ppstr("court_name", varsStr("court_name"));
    sp.ppstr("doc_chi_name", wp.itemStr("doc_chi_name"));
    sp.ppstr("court_dept", wp.itemStr("court_dept"));
    sp.ppnum("payoff_amt", wp.itemNum("payoff_amt"));
    sp.ppstr("payment_day", wp.itemStr("payment_day"));
    sp.ppstr("court_status", wp.itemStr("court_status"));
    sp.ppnum("case_year", wp.itemNum("case_year"));
    sp.ppstr("case_letter", wp.itemStr("case_letter"));
    sp.ppstr("case_letter_desc", wp.itemStr("case_letter_desc"));
    sp.ppstr("judic_date", wp.itemStr("judic_date"));
    sp.ppstr("judic_action_flag", wp.itemStr("judic_action_flag"));
    sp.ppstr("action_date_s", wp.itemStr("action_date_s"));
    sp.ppstr("judic_cancel_flag", wp.itemStr("judic_cancel_flag"));
    sp.ppstr("cancel_date", wp.itemStr("cancel_date"));
    sp.ppstr("renew_cancel_date", wp.itemStr("renew_cancel_date"));
    sp.ppstr("deliver_date", wp.itemStr("deliver_date"));
    sp.ppstr("renew_first_date", wp.itemStr("renew_first_date"));
    sp.ppstr("renew_last_date", wp.itemStr("renew_last_date"));
    sp.ppnum("renew_int", wp.itemNum("renew_int"));
    sp.ppnum("renew_rate", wp.itemNum("renew_rate"));
    sp.ppstr("confirm_date", wp.itemStr("confirm_date"));
    sp.ppstr("run_renew_flag", wp.itemStr("run_renew_flag"));
    sp.ppstr("renew_damage_date", wp.itemStr("renew_damage_date"));
    sp.ppstr("renew_accetp_no", varsStr("renew_accetp_no"));
    sp.ppstr("super_name", wp.itemStr("super_name"));
    sp.ppstr("apr_flag", varsStr("apr_flag"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int deleteColLiadModTmp() {
    strSql = "delete col_liad_mod_tmp where data_type ='RENEW' and data_key = ? ";
    Object[] param = new Object[] {varsStr("liad_doc_no")};
    rc = sqlExec(strSql, param);

    return rc;
  }

  public int updateColLiadRenew() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_renew");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where liad_doc_no =?", varsStr("liad_doc_no"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }
  //////////////////////// colm1160 update end////////////////////////////

  //////////////////////// colm1170 update start////////////////////////////
  public int insertColLiadLiquidate() {
    dataCheckColLiadLiquidate();
    if (rc != 1)
      return -1;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liad_liquidate");
    sp.ppstr("liad_doc_no", varsStr("liad_doc_no"));
    sp.ppstr("id_no", wp.itemStr("id_no"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("recv_date", wp.itemStr("recv_date"));
    sp.ppstr("acct_status", varsStr("acct_status"));
    sp.ppstr("m_code", varsStr("m_code"));
    sp.ppnum("bad_debt_amt", varsNum("bad_debt_amt"));
    sp.ppnum("demand_amt", varsNum("demand_amt"));
    sp.ppnum("debt_amt", varsNum("debt_amt"));
    sp.ppnum("acct_num", varsNum("acct_num"));
    sp.ppnum("card_num", varsNum("card_num"));
    sp.ppstr("liqu_status", wp.itemStr("liqu_status"));
    sp.ppstr("credit_branch", wp.itemStr("credit_branch"));
    sp.ppstr("branch_comb_flag", wp.itemStr("branch_comb_flag"));
    sp.ppstr("max_bank_flag", wp.itemStr("max_bank_flag"));
    sp.ppnum("org_debt_amt", varsNum("org_debt_amt"));
    sp.ppnum("org_debt_amt_bef", varsNum("org_debt_amt_bef"));
    sp.ppnum("org_debt_amt_bef_base", wp.itemNum("org_debt_amt_bef_base"));
    sp.ppnum("liqu_lose_amt ", varsNum("liqu_lose_amt"));
    sp.ppstr("court_id", wp.itemStr("court_id"));
    sp.ppstr("court_name", varsStr("court_name"));
    sp.ppstr("doc_chi_name", wp.itemStr("doc_chi_name"));
    sp.ppstr("court_dept", wp.itemStr("court_dept"));
    sp.ppstr("court_status", wp.itemStr("court_status"));
    sp.ppnum("case_year", wp.itemNum("case_year"));
    sp.ppstr("case_letter", wp.itemStr("case_letter"));
    sp.ppstr("case_letter_desc", wp.itemStr("case_letter_desc"));
    sp.ppstr("judic_avoid_flag", wp.itemStr("judic_avoid_flag"));
    sp.ppstr("judic_avoid_sure_flag", wp.itemStr("judic_avoid_sure_flag"));
    sp.ppstr("judic_avoid_no", varsStr("judic_avoid_no"));
    sp.ppstr("judic_date", wp.itemStr("judic_date"));
    sp.ppstr("judic_action_flag", wp.itemStr("judic_action_flag"));
    sp.ppstr("action_date_s", wp.itemStr("action_date_s"));
    sp.ppstr("judic_cancel_flag", wp.itemStr("judic_cancel_flag"));
    sp.ppstr("cancel_date", wp.itemStr("cancel_date"));
    sp.ppstr("law_133_flag", wp.itemStr("law_133_flag"));
    sp.ppstr("apr_flag", varsStr("apr_flag"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int deleteColLiadModTmp2() {
    strSql = "delete col_liad_mod_tmp where data_type ='LIQUIDATE' and data_key = ? ";
    Object[] param = new Object[] {varsStr("liad_doc_no")};
    rc = sqlExec(strSql, param);
    return rc;
  }

  public int updateColLiadLiquidate() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_liquidate");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where liad_doc_no =?", varsStr("liad_doc_no"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  //////////////////////// colm1170 update end////////////////////////////

  //////////////////////// colm1180 update start////////////////////////////


  public int deleteColLiadModTmpInstmast() {
    strSql = "delete col_liad_mod_tmp where data_type ='INST-MAST' and data_key = ? ";
    Object[] param = new Object[] {varsStr("ls_idcase")};
    rc = sqlExec(strSql, param);
    return rc;
  }
  public int deleteColLiadModTmpInstdetl() {
    strSql = "delete col_liad_mod_tmp where data_type ='INST-DETL' and data_key like ? ";
    Object[] param = new Object[] {varsStr("ls_idcase") + "%"};
    rc = sqlExec(strSql, param);
    return rc;
  }
  //////////////////////// colm1180 update end////////////////////////////

  //////////////////////// colm1190 update start////////////////////////////

  public int deleteColLiadModTmpColldata() {
    strSql = "delete  col_liad_mod_tmp " + "where data_type ='COLL-DATA' "
        + "and data_key = rpad (?, 10, ' ') || rpad (?, 10, ' ') ";
    Object[] param = new Object[] {wp.itemStr("id_no"), wp.itemStr("case_letter")};
    rc = sqlExec(strSql, param);

    return rc;
  }

  public int updateColLiad570() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_570");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where id_no =?", wp.itemStr("id_no"));
    sp.sql2Where(" and case_letter =?", wp.itemStr("case_letter"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }
  //////////////////////// colm1190 update end////////////////////////////

  //////////////////////// colm1192 update start////////////////////////////

  public int deleteColLiadModTmpCollclose() {
    strSql = "delete  col_liad_mod_tmp " + "where data_type ='COLL-CLOSE' "
        + "and data_key = rpad (?, 10, ' ') || rpad (?, 10, ' ') ";
    Object[] param = new Object[] {wp.itemStr("id_no"), wp.itemStr("case_letter")};
    rc = sqlExec(strSql, param);
    return rc;
  }
  
  public int updateColLiad570Close() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_570");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where id_no =?", wp.itemStr("id_no"));
    sp.sql2Where(" and case_letter =?", wp.itemStr("case_letter"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  //////////////////////// colm1192 update end////////////////////////////

  //////////////////////// colm1196 update start////////////////////////////
  public int insertColLiadRenewCourt() {
    dataCheckColLiadRenewCourt();
    if (rc != 1)
      return -1;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liad_renew_court");
    sp.ppstr("liad_doc_no", varsStr("liad_doc_no"));
    sp.ppstr("liad_doc_seqno", varsStr("liad_doc_seqno"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("unit_doc_no", varsStr("unit_doc_no"));
    sp.ppstr("recv_date", varsStr("recv_date"));
    sp.ppstr("key_note", varsStr("key_note"));
    sp.ppstr("case_letter_desc", varsStr("case_letter_desc"));
    sp.ppstr("case_date", varsStr("case_date"));
    sp.ppstr("apr_date", wp.sysDate);
    sp.ppstr("apr_user", wp.loginUser);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  public int deleteColLiadModTmpRenew() {
    strSql = "delete  col_liad_mod_tmp where data_type ='RENEWCOURT' and data_key = ? ";
    Object[] param = new Object[] {varsStr("liad_doc_no") + varsStr("liad_doc_seqno")};
    rc = sqlExec(strSql, param);

    return rc;
  }

  public int updateColLiadRenewCourt() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_renew_court");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where liad_doc_no =?", varsStr("liad_doc_no"));
    sp.sql2Where(" and liad_doc_seqno =?", varsStr("liad_doc_seqno"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  //////////////////////// colm1196 update end////////////////////////////

  //////////////////////// colm1197 update start////////////////////////////
  public int insertColLiadLiquidateCourt() {
    dataCheckColLiadLiquidateCourt();
    if (rc != 1)
      return -1;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liad_liquidate_court");
    sp.ppstr("liad_doc_no", varsStr("liad_doc_no"));
    sp.ppstr("liad_doc_seqno", varsStr("liad_doc_seqno"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("unit_doc_no", varsStr("unit_doc_no"));
    sp.ppstr("recv_date", varsStr("recv_date"));
    sp.ppstr("key_note", varsStr("key_note"));
    sp.ppstr("case_letter_desc", varsStr("case_letter_desc"));
    sp.ppstr("case_date", varsStr("case_date"));
    sp.ppstr("apr_date", wp.sysDate);
    sp.ppstr("apr_user", wp.loginUser);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  public int deleteColLiadModTmpLiquidate() {
    strSql = "delete  col_liad_mod_tmp where data_type ='LIQUICOURT' and data_key = ? ";
    Object[] param = new Object[] {varsStr("liad_doc_no") + varsStr("liad_doc_seqno")};
    rc = sqlExec(strSql, param);

    return rc;
  }

  public int updateColLiadLiquidateCourt() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liad_liquidate_court");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time =sysdate");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where liad_doc_no =?", varsStr("liad_doc_no"));
    sp.sql2Where(" and liad_doc_seqno =?", varsStr("liad_doc_seqno"));
    sp.sql2Where(" and mod_seqno =?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  //////////////////////// colm1197 update end////////////////////////////

  public int insertColLiadModTmp() {
    dataCheckColLiadModTmp();
    if (rc != 1)
      return -1;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liad_mod_tmp");
    sp.ppstr("data_type", varsStr("data_type"));
    if (varsStr("data_type").equals("COLL-DATA") || varsStr("data_type").equals("COLL-CLOSE")
        || varsStr("data_type").equals("INST-MAST")) {
      sp.addsql(",data_key", " ,rpad('" + wp.itemStr("id_no") + "',10,' ')" + "||rpad('"
          + wp.itemStr("case_letter") + "',10,' ')");
    }
    /*
     * else if(vars_ss("data_type").equals("INST-MAST")){
     * //sp.ppss("data_key",vars_ss("ls_idcase")); }
     */
    else if (varsStr("data_type").equals("INST-DETL")) {
      sp.ppstr("data_key", varsStr("data_key"));
    } else if (varsStr("data_type").equals("RENEWCOURT")
        || varsStr("data_type").equals("LIQUICOURT")) {
      sp.ppstr("data_key", varsStr("liad_doc_no") + varsStr("liad_doc_seqno"));
    } else {
      sp.ppstr("data_key", varsStr("liad_doc_no"));
    }
    sp.ppstr("aud_code", varsStr("aud_code"));
    sp.ppstr("mod_data", varsStr("mod_data"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("crt_date", wp.sysDate);

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }
}
