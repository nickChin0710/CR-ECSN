/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-09  V1.00.00  yash       program initial                            *
* 109-03-09  V1.10.00  yanghan    Add two rows in bil_dodo_parm              *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *    
* 109-11-18  V1.00.01  Kirin      bilm1110移至MKT02,並更名mktm1016           * 
******************************************************************************/

package mktm02;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Mktm1016Func extends FuncEdit {
  String actionCd = "";

  public Mktm1016Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      if (empty(wp.itemStr("kk_action_cd")) == true) {
        errmsg("請輸入活動代號");
        return;
      } else
        actionCd = wp.itemStr("kk_action_cd");
    } else {
      actionCd = wp.itemStr("action_cd");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from bil_dodo_parm where action_cd = ?";
      Object[] param = new Object[] {actionCd};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where action_cd = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {actionCd, wp.modSeqno()};
      if (isOtherModify("bil_dodo_parm", sqlWhere, param)) {
        errmsg("請重新查詢");
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
    String kkDateFm1 = "";
    String kkDateTo1 = "";
    double kkLimitDays1 = 0;
    String kkDateFm2 = "";
    String kkDateTo2 = "";
    double kkLimitDays2 = 0;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_dodo_parm");
    sp.ppstr("action_cd", actionCd);
    sp.ppstr("document_desc", wp.itemStr("document_desc"));
    sp.ppnum("car_hours", wp.itemNum("car_hours"));
    sp.ppnum("charge_amt", wp.itemNum("charge_amt"));
    sp.ppstr("ivr_flag", wp.itemStr("ivr_flag"));
    sp.ppstr("document", wp.itemStr("document"));
    sp.ppstr("group_code_flag", wp.itemStr("group_code_flag"));
    sp.ppnum("total_bonus", wp.itemNum("total_bonus"));
    sp.ppstr("ext_batch_no", wp.itemStr("ext_batch_no"));

    sp.ppstr("consume_method_1", wp.itemStr("consume_method_1"));
    sp.ppstr("consume_period_1", wp.itemStr("consume_period_1"));
    if (wp.itemStr("consume_period_1").equals("3")) {
      kkDateFm1 = wp.itemStr("date_fm_1");
      kkDateTo1 = wp.itemStr("date_to_1");
      kkLimitDays1 = wp.itemNum("limit_days_1");
    }
    // sp.ppss("date_fm_1",wp.item_ss("date_fm_1"));
    // sp.ppss("date_to_1",wp.item_ss("date_to_1"));
    // sp.ppnum("limit_days_1",wp.item_num("limit_days_1"));
    sp.ppstr("date_fm_1", kkDateFm1);
    sp.ppstr("date_to_1", kkDateTo1);
    sp.ppnum("limit_days_1", kkLimitDays1);
    sp.ppnum("consume_amt_fm_1", wp.itemNum("consume_amt_fm_1"));
    sp.ppnum("consume_amt_to_1", wp.itemNum("consume_amt_to_1"));
    sp.ppnum("consume_cnt_1", wp.itemNum("consume_cnt_1"));

    sp.ppstr("consume_method_2", wp.itemStr("consume_method_2"));
    sp.ppstr("consume_period_2", wp.itemStr("consume_period_2"));
    if (wp.itemStr("consume_period_2").equals("3")) {
      kkDateFm1 = wp.itemStr("date_fm_2");
      kkDateTo1 = wp.itemStr("date_to_2");
      kkLimitDays1 = wp.itemNum("limit_days_2");
    }
    sp.ppstr("date_fm_2", kkDateFm2);
    sp.ppstr("date_to_2", kkDateTo2);
    sp.ppnum("limit_days_2", kkLimitDays2);
    sp.ppnum("consume_amt_fm_2", wp.itemNum("consume_amt_fm_2"));
    sp.ppnum("consume_amt_to_2", wp.itemNum("consume_amt_to_2"));
    sp.ppnum("consume_cnt_2", wp.itemNum("consume_cnt_2"));

    sp.ppstr("card_type_flag", wp.itemStr("card_type_flag"));
    sp.ppstr("item_ename_bl_1",
        empty(wp.itemStr("item_ename_bl_1")) ? "N" : wp.itemStr("item_ename_bl_1"));
    sp.ppstr("item_ename_it_1",
        empty(wp.itemStr("item_ename_it_1")) ? "N" : wp.itemStr("item_ename_it_1"));
    sp.ppstr("item_ename_ca_1",
        empty(wp.itemStr("item_ename_ca_1")) ? "N" : wp.itemStr("item_ename_ca_1"));
    sp.ppstr("item_ename_id_1",
        empty(wp.itemStr("item_ename_id_1")) ? "N" : wp.itemStr("item_ename_id_1"));
    sp.ppstr("item_ename_ao_1",
        empty(wp.itemStr("item_ename_ao_1")) ? "N" : wp.itemStr("item_ename_ao_1"));
    sp.ppstr("item_ename_ot_1",
        empty(wp.itemStr("item_ename_ot_1")) ? "N" : wp.itemStr("item_ename_ot_1"));
    sp.ppstr("item_ename_bl_2",
        empty(wp.itemStr("item_ename_bl_2")) ? "N" : wp.itemStr("item_ename_bl_2"));
    sp.ppstr("item_ename_it_2",
        empty(wp.itemStr("item_ename_it_2")) ? "N" : wp.itemStr("item_ename_it_2"));
    sp.ppstr("item_ename_ca_2",
        empty(wp.itemStr("item_ename_ca_2")) ? "N" : wp.itemStr("item_ename_ca_2"));
    sp.ppstr("item_ename_id_2",
        empty(wp.itemStr("item_ename_id_2")) ? "N" : wp.itemStr("item_ename_id_2"));
    sp.ppstr("item_ename_ao_2",
        empty(wp.itemStr("item_ename_ao_2")) ? "N" : wp.itemStr("item_ename_ao_2"));
    sp.ppstr("item_ename_ot_2",
        empty(wp.itemStr("item_ename_ot_2")) ? "N" : wp.itemStr("item_ename_ot_2"));
    sp.ppstr("mcht_no_1", wp.itemStr("mcht_no_1"));
    sp.ppstr("mcht_no_2", wp.itemStr("mcht_no_2"));

    // 如未勾選[分期付款(IT)]，不需存入值,若有則存
    sp.ppstr("it_2_type", empty(wp.itemStr("item_ename_it_2")) ? "" : wp.itemStr("it_2_type"));
    sp.ppstr("it_1_type", empty(wp.itemStr("item_ename_it_1")) ? "" : wp.itemStr("it_1_type"));
    // System.out.println("it1"+wp.item_ss("it_1_type"));
    // System.out.println("it2"+wp.item_ss("it_2_type"));
    sp.ppstr("apr_user", wp.itemStr("approval_user"));
    sp.ppstr("apr_date", getSysDate());
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }


  public int dbInsert2() {

    actionInit("A");
    // dataCheck();

    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_dodo_bn_data");
    sp.ppstr("action_cd", wp.itemStr("card_kk1"));
    sp.ppstr("data_type", "01");
    sp.ppstr("data_code", varsStr("aa_data_code"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", "卡種");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int dbInsert3() {

    actionInit("A");
    // dataCheck();

    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_dodo_bn_data");
    sp.ppstr("action_cd", wp.itemStr("group_kk1"));
    sp.ppstr("data_type", "02");
    sp.ppstr("data_code", varsStr("aa_data_code2"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", "團體代號");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int dbInsert4() {

    actionInit("A");
    // dataCheck();

    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_dodo_bn_data");
    sp.ppstr("action_cd", wp.itemStr("mcht_kk1"));
    sp.ppstr("data_type", "03");
    sp.ppstr("data_code", varsStr("aa_data_code3"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", "特店代號");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int dbInsert5() {

    actionInit("A");
    // dataCheck();

    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_dodo_bn_data");
    sp.ppstr("action_cd", wp.itemStr("mcht2_kk1"));
    sp.ppstr("data_type", "04");
    sp.ppstr("data_code", varsStr("aa_data_code4"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", "特店代號");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sqlExec(sp.sqlStmt(), sp.sqlParm());

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
    sp.sql2Update("bil_dodo_parm");
    sp.ppstr("document_desc", wp.itemStr("document_desc"));
    sp.ppnum("car_hours", wp.itemNum("car_hours"));
    sp.ppnum("charge_amt", wp.itemNum("charge_amt"));
    sp.ppstr("ivr_flag", wp.itemStr("ivr_flag"));
    sp.ppstr("document", wp.itemStr("document"));
    sp.ppstr("group_code_flag", wp.itemStr("group_code_flag"));
    sp.ppnum("total_bonus", wp.itemNum("total_bonus"));
    sp.ppstr("ext_batch_no", wp.itemStr("ext_batch_no"));

    sp.ppstr("consume_method_1", wp.itemStr("consume_method_1"));
    sp.ppstr("consume_period_1", wp.itemStr("consume_period_1"));
    sp.ppstr("date_fm_1", wp.itemStr("date_fm_1"));
    sp.ppstr("date_to_1", wp.itemStr("date_to_1"));
    sp.ppnum("limit_days_1", wp.itemNum("limit_days_1"));
    sp.ppnum("consume_amt_fm_1", wp.itemNum("consume_amt_fm_1"));
    sp.ppnum("consume_amt_to_1", wp.itemNum("consume_amt_to_1"));
    sp.ppnum("consume_cnt_1", wp.itemNum("consume_cnt_1"));

    sp.ppstr("consume_method_2", wp.itemStr("consume_method_2"));
    sp.ppstr("consume_period_2", wp.itemStr("consume_period_2"));
    sp.ppstr("date_fm_2", wp.itemStr("date_fm_2"));
    sp.ppstr("date_to_2", wp.itemStr("date_to_2"));
    sp.ppnum("limit_days_2", wp.itemNum("limit_days_2"));
    sp.ppnum("consume_amt_fm_2", wp.itemNum("consume_amt_fm_2"));
    sp.ppnum("consume_amt_to_2", wp.itemNum("consume_amt_to_2"));
    sp.ppnum("consume_cnt_2", wp.itemNum("consume_cnt_2"));

    sp.ppstr("card_type_flag", wp.itemStr("card_type_flag"));

    sp.ppstr("item_ename_bl_1",
        empty(wp.itemStr("item_ename_bl_1")) ? "N" : wp.itemStr("item_ename_bl_1"));
    sp.ppstr("item_ename_it_1",
        empty(wp.itemStr("item_ename_it_1")) ? "N" : wp.itemStr("item_ename_it_1"));
    sp.ppstr("item_ename_ca_1",
        empty(wp.itemStr("item_ename_ca_1")) ? "N" : wp.itemStr("item_ename_ca_1"));
    sp.ppstr("item_ename_id_1",
        empty(wp.itemStr("item_ename_id_1")) ? "N" : wp.itemStr("item_ename_id_1"));
    sp.ppstr("item_ename_ao_1",
        empty(wp.itemStr("item_ename_ao_1")) ? "N" : wp.itemStr("item_ename_ao_1"));
    sp.ppstr("item_ename_ot_1",
        empty(wp.itemStr("item_ename_ot_1")) ? "N" : wp.itemStr("item_ename_ot_1"));
    sp.ppstr("item_ename_bl_2",
        empty(wp.itemStr("item_ename_bl_2")) ? "N" : wp.itemStr("item_ename_bl_2"));
    sp.ppstr("item_ename_it_2",
        empty(wp.itemStr("item_ename_it_2")) ? "N" : wp.itemStr("item_ename_it_2"));

    // 如未勾選[分期付款(IT)]，不需存入值,若有則存
    sp.ppstr("it_2_type", empty(wp.itemStr("item_ename_it_2")) ? "" : wp.itemStr("it_2_type"));
    sp.ppstr("it_1_type", empty(wp.itemStr("item_ename_it_1")) ? "" : wp.itemStr("it_1_type"));
    // System.out.println("it1="+wp.item_ss("it_1_type"));
    // System.out.println("it2="+wp.item_ss("it_2_type"));

    sp.ppstr("item_ename_ca_2",
        empty(wp.itemStr("item_ename_ca_2")) ? "N" : wp.itemStr("item_ename_ca_2"));
    sp.ppstr("item_ename_id_2",
        empty(wp.itemStr("item_ename_id_2")) ? "N" : wp.itemStr("item_ename_id_2"));
    sp.ppstr("item_ename_ao_2",
        empty(wp.itemStr("item_ename_ao_2")) ? "N" : wp.itemStr("item_ename_ao_2"));
    sp.ppstr("item_ename_ot_2",
        empty(wp.itemStr("item_ename_ot_2")) ? "N" : wp.itemStr("item_ename_ot_2"));
    sp.ppstr("mcht_no_1", wp.itemStr("mcht_no_1"));
    sp.ppstr("mcht_no_2", wp.itemStr("mcht_no_2"));

    sp.ppstr("apr_user", wp.itemStr("approval_user"));
    sp.ppstr("apr_date", getSysDate());
    sp.ppstr("apr_flag", "Y");

    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where action_cd=?", actionCd);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
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
    strSql = "delete bil_dodo_parm " + sqlWhere;
    Object[] param = new Object[] {actionCd, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

  public int dbDelete2() {

    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_dodo_bn_data  where hex(rowid) = ? ";
    Object[] param = new Object[] {varsStr("aa_rowid1")};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int dbDelete3() {

    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_dodo_bn_data  where hex(rowid) = ? ";
    Object[] param = new Object[] {varsStr("aa_rowid2")};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int dbDelete4() {

    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_dodo_bn_data  where hex(rowid) = ? ";
    Object[] param = new Object[] {varsStr("aa_rowid3")};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int dbDelete5() {

    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_dodo_bn_data  where hex(rowid) = ? ";
    Object[] param = new Object[] {varsStr("aa_rowid4")};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int dbDeleteUpload(String actionCd, String dataType) {

    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_dodo_bn_data  where action_cd = ? and data_type = ? ";
    Object[] param = new Object[] {actionCd, dataType};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int dbInsertCode(String actionCd, String dataType) {

    actionInit("A");
    msgOK();

    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_dodo_bn_data");
    sp.ppstr("action_cd", actionCd);
    sp.ppstr("data_type", dataType);
    sp.ppstr("data_code", varsStr("aa_data_code"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", "特店代號");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }


}
