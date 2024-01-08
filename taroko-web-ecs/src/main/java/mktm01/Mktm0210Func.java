/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-09  V1.00.01  Andy       program initial                            *
* 109-04-23  V1.00.02  YangFang   updated for project coding standard        *
* 112-12-27  V1.00.03  Zuwei Su   修改起迄值的迄值金額,訊息顯示成功,實際沒有更新成功        *
******************************************************************************/
package mktm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import ofcapp.AppMsg;
import taroko.com.TarokoCommon;

public class Mktm0210Func extends FuncEdit {
  String kk1Rowid = "", itemEnameIncl = "", itemEnameExcl = "", keyData = "", costAmt = "";
  String kkGroupCode = "";
  String kkProgramCode = "";

  public Mktm0210Func(TarokoCommon wr) {
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

    kk1Rowid = wp.itemStr("rowid");
    kkGroupCode = wp.itemStr("kk_group_code");
    if (empty(kkGroupCode)) {
      kkGroupCode = wp.colStr("group_code");
    }
    kkProgramCode = "mktm0210_" + kkGroupCode;
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from mkt_purc_gp_t " + "where group_code = ? ";
      Object[] param = new Object[] {kkGroupCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }
    } else {
      // -other modify-
      sqlWhere = " where 1=1 and hex(rowid) = ? ";
      Object[] param = new Object[] {kk1Rowid};
      if (this.isOtherModify("mkt_purc_gp_t", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("mkt_purc_gp_t");
    sp.ppstr("group_code", kkGroupCode);
    sp.ppstr("description", wp.itemStr("description"));
    sp.ppstr("reward_type", wp.itemStr("reward_type"));
    sp.ppnum("base_amt_1", wp.itemNum("base_amt_1"));
    sp.ppnum("purch_amt_s_a1", wp.itemNum("purch_amt_s_a1"));
    sp.ppnum("purch_amt_e_a1", wp.itemNum("purch_amt_e_a1"));
    sp.ppnum("rate_a1", wp.itemNum("rate_a1"));
    sp.ppnum("purch_amt_s_a2", wp.itemNum("purch_amt_s_a2"));
    sp.ppnum("purch_amt_e_a2", wp.itemNum("purch_amt_e_a2"));
    sp.ppnum("rate_a2", wp.itemNum("rate_a2"));
    sp.ppnum("purch_amt_s_a3", wp.itemNum("purch_amt_s_a3"));
    sp.ppnum("purch_amt_e_a3", wp.itemNum("purch_amt_e_a3"));
    sp.ppnum("rate_a3", wp.itemNum("rate_a3"));
    sp.ppnum("purch_amt_s_a4", wp.itemNum("purch_amt_s_a4"));
    sp.ppnum("purch_amt_e_a4", wp.itemNum("purch_amt_e_a4"));
    sp.ppnum("rate_a4", wp.itemNum("rate_a4"));
    sp.ppnum("purch_amt_s_a5", wp.itemNum("purch_amt_s_a5"));
    sp.ppnum("purch_amt_e_a5", wp.itemNum("purch_amt_e_a5"));
    sp.ppnum("rate_a5", wp.itemNum("rate_a5"));
    sp.ppnum("int_amt_s_1", wp.itemNum("int_amt_s_1"));
    sp.ppnum("int_amt_e_1", wp.itemNum("int_amt_e_1"));
    sp.ppnum("int_rate_1", wp.itemNum("int_rate_1"));
    sp.ppnum("int_amt_s_2", wp.itemNum("int_amt_s_2"));
    sp.ppnum("int_amt_e_2", wp.itemNum("int_amt_e_2"));
    sp.ppnum("int_rate_2", wp.itemNum("int_rate_2"));
    sp.ppnum("int_amt_s_3", wp.itemNum("int_amt_s_3"));
    sp.ppnum("int_amt_e_3", wp.itemNum("int_amt_e_3"));
    sp.ppnum("int_rate_3", wp.itemNum("int_rate_3"));
    sp.ppnum("int_amt_s_4", wp.itemNum("int_amt_s_4"));
    sp.ppnum("int_amt_e_4", wp.itemNum("int_amt_e_4"));
    sp.ppnum("int_rate_4", wp.itemNum("int_rate_4"));
    sp.ppnum("int_amt_s_5", wp.itemNum("int_amt_s_5"));
    sp.ppnum("int_amt_e_5", wp.itemNum("int_amt_e_5"));
    sp.ppnum("int_rate_5", wp.itemNum("int_rate_5"));
    sp.ppnum("out_amt_s_1", wp.itemNum("out_amt_s_1"));
    sp.ppnum("out_amt_e_1", wp.itemNum("out_amt_e_1"));
    sp.ppnum("out_rate_1", wp.itemNum("out_rate_1"));
    sp.ppnum("out_amt_s_2", wp.itemNum("out_amt_s_2"));
    sp.ppnum("out_amt_e_2", wp.itemNum("out_amt_e_2"));
    sp.ppnum("out_rate_2", wp.itemNum("out_rate_2"));
    sp.ppnum("out_amt_s_3", wp.itemNum("out_amt_s_3"));
    sp.ppnum("out_amt_e_3", wp.itemNum("out_amt_e_3"));
    sp.ppnum("out_rate_3", wp.itemNum("out_rate_3"));
    sp.ppnum("out_amt_s_4", wp.itemNum("out_amt_s_4"));
    sp.ppnum("out_amt_e_4", wp.itemNum("out_amt_e_4"));
    sp.ppnum("out_rate_4", wp.itemNum("out_rate_4"));
    sp.ppnum("out_amt_s_5", wp.itemNum("out_amt_s_5"));
    sp.ppnum("out_amt_e_5", wp.itemNum("out_amt_e_5"));
    sp.ppnum("out_rate_5", wp.itemNum("out_rate_5"));
    sp.ppstr("item_ename_bl", wp.itemStr("item_ename_bl"));
    sp.ppstr("item_ename_bl_in", wp.itemStr("item_ename_bl_in"));
    sp.ppstr("item_ename_bl_out", wp.itemStr("item_ename_bl_out"));
    sp.ppstr("item_ename_it", wp.itemStr("item_ename_it"));
    sp.ppstr("item_ename_it_in", wp.itemStr("item_ename_it_in"));
    sp.ppstr("item_ename_it_out", wp.itemStr("item_ename_it_out"));
    sp.ppstr("item_ename_ca", wp.itemStr("item_ename_ca"));
    sp.ppstr("item_ename_id", wp.itemStr("item_ename_id"));
    sp.ppstr("item_ename_ao", wp.itemStr("item_ename_ao"));
    sp.ppstr("item_ename_ot", wp.itemStr("item_ename_ot"));
    sp.ppstr("present_type", wp.itemStr("present_type"));
    sp.ppnum("rate_a12", wp.itemNum("rate_a12"));
    sp.ppnum("rate_a22", wp.itemNum("rate_a22"));
    sp.ppnum("rate_a32", wp.itemNum("rate_a32"));
    sp.ppnum("rate_a42", wp.itemNum("rate_a42"));
    sp.ppnum("rate_a52", wp.itemNum("rate_a52"));
    sp.ppnum("int_rate_12", wp.itemNum("int_rate_12"));
    sp.ppnum("int_rate_22", wp.itemNum("int_rate_22"));
    sp.ppnum("int_rate_32", wp.itemNum("int_rate_32"));
    sp.ppnum("int_rate_42", wp.itemNum("int_rate_42"));
    sp.ppnum("int_rate_52", wp.itemNum("int_rate_52"));
    sp.ppnum("out_rate_12", wp.itemNum("out_rate_12"));
    sp.ppnum("out_rate_22", wp.itemNum("out_rate_22"));
    sp.ppnum("out_rate_32", wp.itemNum("out_rate_32"));
    sp.ppnum("out_rate_42", wp.itemNum("out_rate_42"));
    sp.ppnum("out_rate_52", wp.itemNum("out_rate_52"));
    sp.ppstr("program_code", kkProgramCode);
    sp.ppstr("purch_date_type", wp.itemStr("purch_date_type"));
    sp.ppstr("run_time_dd", wp.itemStr("run_time_dd"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("crt_date", getSysDate());
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_user", "");
    sp.ppstr("apr_date", "");

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  public int dbInsert1() {
    String aaPgCode = varsStr("aa_program_code");
    String aaDataCode = varsStr("aa_data_code");
    String aaDataType = varsStr("aa_data_type");
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("ptr_bn_data_t");
    sp.ppstr("program_code", aaPgCode);
    sp.ppstr("data_type", aaDataType);
    sp.ppstr("data_code", aaDataCode);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("mkt_purc_gp_t");
    sp.ppstr("group_code", kkGroupCode);
    sp.ppstr("description", wp.itemStr("description"));
    sp.ppstr("reward_type", wp.itemStr("reward_type"));
    sp.ppnum("base_amt_1", wp.itemNum("base_amt_1"));
    sp.ppnum("purch_amt_s_a1", wp.itemNum("purch_amt_s_a1"));
    sp.ppnum("purch_amt_e_a1", wp.itemNum("purch_amt_e_a1"));
    sp.ppnum("rate_a1", wp.itemNum("rate_a1"));
    sp.ppnum("purch_amt_s_a2", wp.itemNum("purch_amt_s_a2"));
    sp.ppnum("purch_amt_e_a2", wp.itemNum("purch_amt_e_a2"));
    sp.ppnum("rate_a2", wp.itemNum("rate_a2"));
    sp.ppnum("purch_amt_s_a3", wp.itemNum("purch_amt_s_a3"));
    sp.ppnum("purch_amt_e_a3", wp.itemNum("purch_amt_e_a3"));
    sp.ppnum("rate_a3", wp.itemNum("rate_a3"));
    sp.ppnum("purch_amt_s_a4", wp.itemNum("purch_amt_s_a4"));
    sp.ppnum("purch_amt_e_a4", wp.itemNum("purch_amt_e_a4"));
    sp.ppnum("rate_a4", wp.itemNum("rate_a4"));
    sp.ppnum("purch_amt_s_a5", wp.itemNum("purch_amt_s_a5"));
    sp.ppnum("purch_amt_e_a5", wp.itemNum("purch_amt_e_a5"));
    sp.ppnum("rate_a5", wp.itemNum("rate_a5"));
    sp.ppnum("int_amt_s_1", wp.itemNum("int_amt_s_1"));
    sp.ppnum("int_amt_e_1", wp.itemNum("int_amt_e_1"));
    sp.ppnum("int_rate_1", wp.itemNum("int_rate_1"));
    sp.ppnum("int_amt_s_2", wp.itemNum("int_amt_s_2"));
    sp.ppnum("int_amt_e_2", wp.itemNum("int_amt_e_2"));
    sp.ppnum("int_rate_2", wp.itemNum("int_rate_2"));
    sp.ppnum("int_amt_s_3", wp.itemNum("int_amt_s_3"));
    sp.ppnum("int_amt_e_3", wp.itemNum("int_amt_e_3"));
    sp.ppnum("int_rate_3", wp.itemNum("int_rate_3"));
    sp.ppnum("int_amt_s_4", wp.itemNum("int_amt_s_4"));
    sp.ppnum("int_amt_e_4", wp.itemNum("int_amt_e_4"));
    sp.ppnum("int_rate_4", wp.itemNum("int_rate_4"));
    sp.ppnum("int_amt_s_5", wp.itemNum("int_amt_s_5"));
    sp.ppnum("int_amt_e_5", wp.itemNum("int_amt_e_5"));
    sp.ppnum("int_rate_5", wp.itemNum("int_rate_5"));
    sp.ppnum("out_amt_s_1", wp.itemNum("out_amt_s_1"));
    sp.ppnum("out_amt_e_1", wp.itemNum("out_amt_e_1"));
    sp.ppnum("out_rate_1", wp.itemNum("out_rate_1"));
    sp.ppnum("out_amt_s_2", wp.itemNum("out_amt_s_2"));
    sp.ppnum("out_amt_e_2", wp.itemNum("out_amt_e_2"));
    sp.ppnum("out_rate_2", wp.itemNum("out_rate_2"));
    sp.ppnum("out_amt_s_3", wp.itemNum("out_amt_s_3"));
    sp.ppnum("out_amt_e_3", wp.itemNum("out_amt_e_3"));
    sp.ppnum("out_rate_3", wp.itemNum("out_rate_3"));
    sp.ppnum("out_amt_s_4", wp.itemNum("out_amt_s_4"));
    sp.ppnum("out_amt_e_4", wp.itemNum("out_amt_e_4"));
    sp.ppnum("out_rate_4", wp.itemNum("out_rate_4"));
    sp.ppnum("out_amt_s_5", wp.itemNum("out_amt_s_5"));
    sp.ppnum("out_amt_e_5", wp.itemNum("out_amt_e_5"));
    sp.ppnum("out_rate_5", wp.itemNum("out_rate_5"));
    sp.ppstr("item_ename_bl", wp.itemStr("item_ename_bl"));
    sp.ppstr("item_ename_bl_in", wp.itemStr("item_ename_bl_in"));
    sp.ppstr("item_ename_bl_out", wp.itemStr("item_ename_bl_out"));
    sp.ppstr("item_ename_it", wp.itemStr("item_ename_it"));
    sp.ppstr("item_ename_it_in", wp.itemStr("item_ename_it_in"));
    sp.ppstr("item_ename_it_out", wp.itemStr("item_ename_it_out"));
    sp.ppstr("item_ename_ca", wp.itemStr("item_ename_ca"));
    sp.ppstr("item_ename_id", wp.itemStr("item_ename_id"));
    sp.ppstr("item_ename_ao", wp.itemStr("item_ename_ao"));
    sp.ppstr("item_ename_ot", wp.itemStr("item_ename_ot"));
    sp.ppstr("present_type", wp.itemStr("present_type"));
    sp.ppnum("rate_a12", wp.itemNum("rate_a12"));
    sp.ppnum("rate_a22", wp.itemNum("rate_a22"));
    sp.ppnum("rate_a32", wp.itemNum("rate_a32"));
    sp.ppnum("rate_a42", wp.itemNum("rate_a42"));
    sp.ppnum("rate_a52", wp.itemNum("rate_a52"));
    sp.ppnum("int_rate_12", wp.itemNum("int_rate_12"));
    sp.ppnum("int_rate_22", wp.itemNum("int_rate_22"));
    sp.ppnum("int_rate_32", wp.itemNum("int_rate_32"));
    sp.ppnum("int_rate_42", wp.itemNum("int_rate_42"));
    sp.ppnum("int_rate_52", wp.itemNum("int_rate_52"));
    sp.ppnum("out_rate_12", wp.itemNum("out_rate_12"));
    sp.ppnum("out_rate_22", wp.itemNum("out_rate_22"));
    sp.ppnum("out_rate_32", wp.itemNum("out_rate_32"));
    sp.ppnum("out_rate_42", wp.itemNum("out_rate_42"));
    sp.ppnum("out_rate_52", wp.itemNum("out_rate_52"));
    sp.ppstr("program_code", kkProgramCode);
    sp.ppstr("purch_date_type", wp.itemStr("purch_date_type"));
    sp.ppstr("run_time_dd", wp.itemStr("run_time_dd"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("crt_date", getSysDate());
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_user", "");
    sp.ppstr("apr_date", "");
    sp.sql2Where("where group_code=?", kkGroupCode);
    // System.out.println("sql_stmt() : "+sp.sql_stmt());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("Update mkt_purc_gp_t error !!");
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    strSql = "delete mkt_purc_gp_t " + "where hex(rowid) = :rowid ";
    setString("rowid", wp.itemStr("rowid"));
    rc = sqlExec(strSql);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

}
