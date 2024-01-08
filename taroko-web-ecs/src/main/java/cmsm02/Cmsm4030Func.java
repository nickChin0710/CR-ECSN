package cmsm02;
/** cmsm4030;客服經辦D檔調整作業
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 2018-0727:	JH		modify
 * 109-04-27  shiyuqi       updated for project coding standard     *  
 * 109-07-16  Sunny         取消不必要的檢核-費用 D檔輸入備註                                                *
 * */

import busi.FuncAction;

public class Cmsm4030Func extends FuncAction {

  double ldc = 0.0;
  String isRefNo = "", isTable = "";
  String isRowid = "";
  busi.SqlPrepare sp = new busi.SqlPrepare();

  @Override
  public void dataCheck() {
    isRefNo = varsStr("reference_no");
    isTable = varsStr("db_table");
    isRowid = varsStr("rowid");
    if (eqIgno(isTable, "acaj")) {
      strSql = "select count(*) as db_cnt from cms_acaj" + " where rowid =?"
          + " and nvl(apr_date,'')<>''";
      setRowId2(1, isRowid);

      sqlSelect(strSql);
      if (colNum("db_cnt") > 0) {
        errmsg("此交易已主管覆核, 不可修改");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    this.actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    return insertActAcaj();
  }

  @Override
  public int dbUpdate() {
    // this.actionInit("U");
    // dataCheck();
    //
    // sp =new busi.SqlPrepare();
    // sp.sql2Update("cms_acaj");
    // sp.ppymd("crt_date");
    // sp.pptime("crt_time");
    // sp.ppnum("adj_amt",vars_num("db_d_amt"));
    // sp.ppss("adj_remark",vars_ss("adj_comment"));
    // sp.ppss("crt_user",mod_user);
    // sp.ppss("adj_check_memo",vars_ss("adj_check_memo"));
    // sp.ppss("adj_dept",vars_ss("adj_dept"));
    //
    // sp.ppss("curr_code",vars_ss("curr_code"));
    // sp.ppnum("beg_bal",vars_num("tw_beg_bal"));
    // sp.ppnum("end_bal",vars_num("tw_end_bal"));
    // sp.ppnum("d_available_bal",vars_num("tw_d_available_bal"));
    // sp.ppnum("dc_beg_bal",vars_num("beg_bal"));
    // sp.ppnum("dc_end_bal",vars_num("end_bal"));
    // sp.ppnum("dc_d_available_bal",vars_num("d_available_bal"));
    //
    // sp.mod_XXX(mod_user, mod_pgm);
    // sp.rowid2Where(vars_ss("rowid"));
    //
    // sqlExec(sp.sql_stmt(),sp.sql_parm());
    // if (sql_nrow<=0) {
    // errmsg("update CMS_ACAJ error, "+this.sql_errtext);
    // }
    return rc;
  }

  @Override
  public int dbDelete() {
    // -取消D檔-
    this.actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    dataCheckDel();
    if (rc != 1)
      return rc;

    strSql = " delete cms_acaj where rowid =:rowid ";
    this.setRowId2("rowid", isRowid);
    sqlExec(strSql);
    if (sqlRowNum != 1) {
      errmsg("delete cms_acaj error, kk[%s]", isRefNo);
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  void dataCheckDel() {
    String sql1 =
        " select count(*) as ll_cnt from cms_acaj " + " where rowid = ? " + " and apr_date <> ''";
    sqlSelect(sql1, new Object[] {commSqlStr.strToRowid(isRowid)});

    if (colNum("ll_cnt") > 0) {
      errmsg("此交易已主管覆核, 不可修改");
      return;
    }

  }


  void dataCheckInsert() {
    ldc = 0;
    // **是否 other user 輸入
    String sql1 = " select count(*) as ll_cnt from cms_acaj " + " where reference_no =? "
        + " and nvl(acct_post_flag,'N') ='N' " + " and nvl(debit_flag,'N') ='N' ";
    sqlSelect(sql1, new Object[] {varsStr("reference_no")});
    if (colNum("ll_cnt") > 0) {
      errmsg("此交易已調整未處理, 現在不可調整");
      return;
    }

    // ==已調整未整理--
    String sql2 = " select count(*) as act_cnt from act_acaj " + " where reference_no = ? "
        + " and nvl(process_flag,'N') <> 'Y' ";
    sqlSelect(sql2, new Object[] {varsStr("reference_no")});
    if (colNum("act_cnt") > 0) {
      errmsg("此交易有[act_acaj]調整未處理, 現在不可調整");
      return;
    }

    // **是否列問交
    String sql3 = " select nvl(prb_status,'') as ls_status " + " from rsk_problem "
        + " where reference_no = ? " + " and reference_seq = '1' ";
    sqlSelect(sql3, new Object[] {varsStr("reference_no")});
    if (sqlRowNum > 0 && !eqIgno(colStr("ls_status"), "80")) {
      errmsg("此交易已列問交卻未結案, 不可D檔");
      return;
    }

    // **D檔金額
    ldc = varsNum("db_damt");
    if (ldc < 0) {
      errmsg("請輸入D檔金額,須大於 0 ");
      return;
    }

    if (eqIgno(varsStr("curr_code"), "901")) {
      if (ldc % 1 != 0) {
        errmsg("D檔金額, 台幣須為整數");
        return;
      }
    }

    if (ldc > varsNum("d_avail_bal")) {
      errmsg("D檔金額 不可大於 可D數餘額");
      return;
    }

    // **利息 , 違約金--
    if (pos("|RI|AI|CI|PN|", varsStr("acct_code")) > 0) {
      ldc = ldc * varsNum("tw_beg_bal") / varsNum("beg_bal");
      if (ldc > 1000) {
        errmsg("D檔金額 : " + ldc + " 不可大於 (台幣) 1000 元");
        return;
      }
    }

 // 評估目前合庫不需要此判斷 @20200715 sunny mark	    
//    if (eqIgno(varsStr("acct_code"), "EF")) {
//      if (empty(varsStr("adj_comment"))) {
//        errmsg("費用 D檔 請輸入備註");
//        return;
//      }
//    }

//// @add 20200715 sunny 判斷備註要有值
//    if (empty(varsStr("adj_comment"))) {
//        errmsg("請輸入備註");
//        return;
//      }


  }

  int insertActAcaj() {
    dataCheckInsert();
    if (rc != 1)
      return rc;

    String sql1 = " select * from act_debt where rowid =? ";
    
    if (eqIgno(isTable, "debt_hst")) {
      sql1 = " select * from act_debt_hst where rowid =? ";
    }
    this.setRowId(1, isRowid);
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      //errmsg("select act_debt[_hst] not-find, kk[%s]", isRefNo);
    	errmsg("select [ act_"+ isTable +"] not find, kk[%s]", isRefNo); //@ 20200716 sunny mod
      return rc;
    }

    sp.sql2Insert("cms_acaj");
    sp.addsqlParm(" crt_date", commSqlStr.sysYYmd);
    sp.addsqlParm(", crt_time", ", " + commSqlStr.sysTime);
    sp.addsqlParm(", reference_no", ",?", colStr("reference_no"));
    sp.addsqlParm(", p_seqno", ",?", colStr("p_seqno"));
    sp.addsqlParm(", acct_type", ",?", colStr("acct_type"));
    sp.addsqlParm(", item_post_date", ",?", colStr("post_date"));
    sp.addsqlParm(", item_order_normal", ",?", colStr("item_order_normal"));
    sp.addsqlParm(", item_order_back_date", ",?", colStr("item_order_back_date"));
    sp.addsqlParm(", item_order_refund", ",?", colStr("item_order_refund"));
    sp.addsqlParm(", item_class_normal", ",?", colStr("item_class_normal"));
    sp.addsqlParm(", item_class_back_date", ", ?", colStr("item_class_back_date"));
    sp.addsqlParm(", item_class_refund", ",?", colStr("item_class_refund"));
    sp.addsqlParm(", acct_month", ",?", colStr("acct_month"));
    sp.addsqlParm(", stmt_cycle", ",?", colStr("stmt_cycle"));
    sp.addsqlParm(", bill_type", ",?", colStr("bill_type"));
    sp.addsqlParm(", txn_code", ",?", colStr("txn_code"));
    sp.addsqlParm(", beg_bal", ",?", colStr("beg_bal"));
    sp.addsqlParm(", end_bal", ",?", colStr("end_bal"));
    sp.addsqlParm(", d_avail_bal", ",?", colStr("d_avail_bal"));
    sp.addsqlParm(", card_no", ",?", colStr("card_no"));
    sp.addsqlParm(", acct_code", ",?", colStr("acct_code"));
    sp.addsqlParm(", interest_date", ",?", colStr("interest_date"));
    sp.addsqlParm(", purchase_date", ",?", colStr("purchase_date"));
    // sp.aaa(", acquire_date" , ",?", col_ss("acquire_date"));
    // sp.aaa(", film_no" , ",?", col_ss("film_no"));
    sp.addsqlParm(", mcht_no", ",?", colStr("mcht_no"));
    // sp.aaa(", prod_no" , ",?", col_ss("prod_no"));
    sp.addsqlParm(", dp_reference_no", ",?", colStr("dp_reference_no"));
    sp.addsqlParm(", interest_rs_date", ",?", colStr("interest_rs_date"));
    sp.addsqlParm(", ao_flag", ",?", colStr("ao_flag"));
    // --
    sp.addsqlParm(", adj_amt", ",?", varsNum("db_damt"));
    sp.addsqlParm(", adj_remark", ",?", varsStr("adj_comment"));
    sp.addsqlParm(", crt_user", ",?", modUser);
    sp.addsqlParm(", adj_check_memo", ",?", varsStr("adj_check_memo"));
    sp.addsqlParm(", adj_dept", ",?", wp.loginDeptNo);
    // -jh:R103012-
    sp.addsqlParm(", curr_code", ",?", colNvl("curr_code", "901"));
    sp.addsqlParm(", dc_beg_bal", ",?", colNum("dc_beg_bal"));
    sp.addsqlParm(", dc_end_bal", ",?", colNum("dc_end_bal"));
    sp.addsqlParm(", dc_d_avail_bal", ",?", colNum("dc_d_avail_bal"));
    sp.addsqlParm(", source_table", ",?", isTable);

    sp.addsqlParm(", mod_user", ",?", modUser);
    sp.addsqlParm(", mod_time", "," + commSqlStr.sysdate);
    sp.addsqlParm(", mod_pgm", ",?", modPgm);
    sp.addsqlParm(", mod_seqno", ",?", 1);

    this.sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("Insert cms_acaj error, " + getMsg());
    }

    return rc;
  }

}
