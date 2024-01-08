/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-26  V1.00.00  David FU   program initial                            *
* 108-12-13  V1.00.01  Andy           update                                 *
* 109-01-21  V1.00.02  JustinWu    set curr_code = 901 and f_currency_flag = N
* 109-04-20  V1.00.03  Tanwei       updated for project coding standard      *
* 109-07-31  V1.00.03  yanghan       修改页面覆核栏位     *
******************************************************************************/

package ptrm01;

import java.util.ArrayList;
import java.util.List;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Ptrm0300Func extends FuncEdit {
  String mKkAcctType = "";
  String mKkRcUseFlag = "";

  public Ptrm0300Func(TarokoCommon wr) {
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
      mKkAcctType = wp.itemStr("kk_acct_type");
    } else {
      mKkAcctType = wp.itemStr("acct_type");
    }
    // 20191206 問題單1885 SA1521提出===>20191210停用
    // if(!wp.item_nvl("db_rc_use_flag", "N").equals("N")){
    // if(wp.item_ss("card_indicator").equals("1")){
    // m_kk_rc_use_flag = "1";
    // }
    // if(wp.item_ss("card_indicator").equals("2")){
    // m_kk_rc_use_flag = "3";
    // }
    // }else{
    // m_kk_rc_use_flag = "N";
    // }

    // 20191210 問題單1885 SA1521提出
    if (!wp.itemNvl("db_rc_use_flag", "N").equals("N")) {
      mKkRcUseFlag = "1";
    } else {
      mKkRcUseFlag = "3";
    }
    //

    if (this.isAdd()) {

      if (empty(mKkAcctType)) {
        errmsg("請輸入帳戶類別");
        return;
      }

      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_acct_type where acct_type = ?";
      Object[] param = new Object[] {mKkAcctType};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where acct_type = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkAcctType, wp.modSeqno()};
      isOtherModify("ptr_acct_type", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    // // 2020-01-21 JustinWu BEGIN//
    // is_sql = "insert into ptr_acct_type ("
    // + " acct_type "
    // + ", chin_name"
    // + ", curr_code"
    // + ", card_indicator"
    // + ", f_currency_flag"
    // + ", bonus_flag"
    // + ", rc_use_flag"
    // + ", car_service_flag"
    // + ", inssurance_flag"
    // + ", u_cycle_flag"
    // + ", stmt_cycle"
    // + ", no_collection_flag"
    // + ", atm_code"
    // + ", inst_crdtamt"
    // + ", inst_crdtrate"
    // + ", unon_flag"
    // + ", cashadv_loc_rate"
    // + ", cashadv_loc_maxamt"
    // + ", cashadv_loc_rate_old"
    // + ", breach_num_month"
    // + ", mod_time, mod_user, mod_pgm, mod_seqno"
    // + " ) values ("
    // + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
    // + ",?, ?, ?, ?, ?, ?, ?, ?, ? "
    // + ", sysdate,?,?,1"
    // + " )";
    //
    //
    //
    // //-set ?value-
    // List<Object> lstParam = new ArrayList<>();
    // lstParam.add(m_kk_acct_type);
    // lstParam.add(wp.item_ss("chin_name"));
    // lstParam.add(wp.item_ss("curr_code"));
    // lstParam.add(wp.item_ss("card_indicator"));
    // lstParam.add(wp.item_nvl("f_currency_flag", "N"));
    // lstParam.add(wp.item_nvl("bonus_flag", "N"));
    //// lstParam.add(wp.item_nvl("rc_use_flag", "N"));
    // lstParam.add(m_kk_rc_use_flag); //20191206
    // lstParam.add(wp.item_nvl("car_service_flag", "N"));
    // lstParam.add(wp.item_nvl("inssurance_flag", "N"));
    // lstParam.add(wp.item_nvl("u_cycle_flag", "N"));
    // if (wp.item_ss("u_cycle_flag").equals("Y"))
    // lstParam.add(wp.item_ss("stmt_cycle"));
    // else
    // lstParam.add("");
    // lstParam.add(wp.item_nvl("no_collection_flag","N"));
    // lstParam.add(wp.item_ss("atm_code"));
    // lstParam.add(wp.item_num("inst_crdtamt"));
    // lstParam.add(wp.item_num("inst_crdtrate"));
    // lstParam.add(wp.item_ss("unon_flag"));
    // lstParam.add(wp.item_num("cashadv_loc_rate"));
    // lstParam.add(wp.item_num("cashadv_loc_maxamt"));
    // lstParam.add(wp.item_num("cashadv_loc_rate_old"));
    // lstParam.add(wp.item_num("breach_num_month"));
    // lstParam.add(wp.loginUser);
    // lstParam.add(wp.item_ss("mod_pgm"));
    //
    // Object[] param = new Object[lstParam.size()];
    // param = lstParam.toArray(param);
    //
    // sqlExec(is_sql, param);

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("ptr_acct_type");
    sp.ppstr("acct_type", mKkAcctType);
    sp.ppstr("chin_name", wp.itemStr("chin_name"));
    // sp.ppss("curr_code", wp.item_ss("curr_code"));
    sp.ppstr("curr_code", "901"); // 20200121
    sp.ppstr("card_indicator", wp.itemStr("card_indicator"));
    // sp.ppss("f_currency_flag", wp.item_nvl("f_currency_flag", "N"));
    sp.ppstr("f_currency_flag", "N"); // 20200121
    sp.ppstr("bonus_flag", wp.itemNvl("bonus_flag", "N"));
    // sp.ppss("rc_use_flag", wp.item_nvl("rc_use_flag", "N"));
    sp.ppstr("rc_use_flag", mKkRcUseFlag); // 20191206
    sp.ppstr("car_service_flag", wp.itemNvl("car_service_flag", "N"));
    sp.ppstr("inssurance_flag", wp.itemNvl("inssurance_flag", "N"));
    sp.ppstr("u_cycle_flag", wp.itemNvl("u_cycle_flag", "N"));
    if (wp.itemStr("u_cycle_flag").equals("Y")) {
      sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
    } else {
      sp.ppstr("stmt_cycle", "");
    }
    sp.ppstr("no_collection_flag", wp.itemNvl("no_collection_flag", "N"));
    sp.ppstr("atm_code", wp.itemStr("atm_code"));
    sp.ppnum("inst_crdtamt", wp.itemNum("inst_crdtamt"));
    sp.ppnum("inst_crdtrate", wp.itemNum("inst_crdtrate"));
    sp.ppstr("unon_flag", wp.itemStr("unon_flag"));
    sp.ppnum("cashadv_loc_rate", wp.itemNum("cashadv_loc_rate"));
    sp.ppnum("cashadv_loc_maxamt", wp.itemNum("cashadv_loc_maxamt"));
    sp.ppnum("cashadv_loc_rate_old", wp.itemNum("cashadv_loc_rate_old"));
    sp.ppnum("breach_num_month", wp.itemNum("breach_num_month"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    // 2020-01-21 JustinWu END//

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int dbInsert2() {
    msgOK();

    strSql = "insert into ptr_prod_type (" + " acct_type, " // 1
        + " seqno, " + " group_code, " + " card_type, " + " crt_date, crt_user, "
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,"
        + " (select COALESCE(max(seqno),0)+1 from ptr_prod_type where acct_type = ?)," + " ?, ?"
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate, ?, ?, 1" + " )";;
    Object[] param = new Object[] {mKkAcctType // 1
        , mKkAcctType, varsStr("group_code"), varsStr("card_type"), wp.loginUser, wp.loginUser,
        wp.modPgm()};

    this.sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_prod_type error; " + getMsg());
      sqlCommit(0); // rollback
    } else {
      sqlCommit(1); // commit
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

    strSql = "update ptr_acct_type set " + " chin_name = ?" + ", curr_code = ?"
        + ", card_indicator = ?" + ", f_currency_flag = ?" + ", bonus_flag = ?"
        + ", rc_use_flag = ?" + ", car_service_flag = ?" + ", inssurance_flag = ?"
        + ", u_cycle_flag = ?" + ", stmt_cycle = ?" + ", no_collection_flag = ?" + ", atm_code = ?"
        + ", inst_crdtamt = ?" + ", inst_crdtrate = ?" + ", unon_flag = ?"
        + ", cashadv_loc_rate = ?" + ", cashadv_loc_maxamt = ?" + ", cashadv_loc_rate_old = ?"
        + ", breach_num_month = ?" + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
        + " , mod_seqno = nvl(mod_seqno,0) + 1 " + sqlWhere;

    List<Object> lstParam = new ArrayList<>();
    lstParam.add(wp.itemStr("chin_name"));
    // lstParam.add(wp.item_ss("curr_code"));
    lstParam.add("901"); // 20200121
    lstParam.add(wp.itemStr("card_indicator"));
    // lstParam.add(wp.item_nvl("f_currency_flag", "N"));
    lstParam.add("N"); // 20200121
    lstParam.add(wp.itemNvl("bonus_flag", "N"));
    // lstParam.add(wp.item_nvl("rc_use_flag", "N"));
    lstParam.add(mKkRcUseFlag); // 20191206
    lstParam.add(wp.itemNvl("car_service_flag", "N"));
    lstParam.add(wp.itemNvl("inssurance_flag", "N"));
    lstParam.add(wp.itemNvl("u_cycle_flag", "N"));
    if (wp.itemStr("u_cycle_flag").equals("Y"))
      lstParam.add(wp.itemStr("stmt_cycle"));
    else
      lstParam.add("");
    lstParam.add(wp.itemNvl("no_collection_flag", "N"));
    lstParam.add(wp.itemStr("atm_code"));
    lstParam.add(wp.itemNum("inst_crdtamt"));
    lstParam.add(wp.itemNum("inst_crdtrate"));
    lstParam.add(wp.itemStr("unon_flag"));
    lstParam.add(wp.itemNum("cashadv_loc_rate"));
    lstParam.add(wp.itemNum("cashadv_loc_maxamt"));
    lstParam.add(wp.itemNum("cashadv_loc_rate_old"));
    lstParam.add(wp.itemNum("breach_num_month"));
    lstParam.add(wp.loginUser);
    lstParam.add(wp.itemStr("mod_pgm"));

    lstParam.add(mKkAcctType);
    lstParam.add(wp.modSeqno());

    Object[] param = new Object[lstParam.size()];
    param = lstParam.toArray(param);

    rc = sqlExec(strSql, param);
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
    strSql = "delete ptr_acct_type " + sqlWhere;
    Object[] param = new Object[] {mKkAcctType, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }

    rc = dbDelete2();
    return rc;
  }

  public int dbDelete2() {
    msgOK();
    mKkAcctType = wp.itemStr("kk_acct_type");
    if (empty(mKkAcctType)) {
      mKkAcctType = wp.itemStr("acct_type");
    }

    // 如果沒有資料回傳成功
    Object[] param = new Object[] {mKkAcctType};
    if (sqlRowcount("ptr_prod_type", "where acct_type = ?", param) <= 0)
      return 1;

    strSql = "delete ptr_prod_type where acct_type = ?";
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;

  }

}
