/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-08-13  V1.00.00  yash       program initial                            *
*                                                                            *
******************************************************************************/
package bilm01;

import busi.FuncAction;
import busi.SqlPrepare;

public class Bilm0320Func extends FuncAction {

  String kk1 = "", kk2 = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      kk1 = wp.itemStr("rowid");
    } else {
      kk1 = wp.itemStr("rowid");
    }



    if (this.isDelete())
      return;

    // if (wp.item_num("delay_action_day") < 0) {
    // errmsg("延遲執行天數 不可小於 0");
    // return;
    // }
    //
    // if (wp.item_eq("delay_msg_flag", "Y") && wp.item_num("delay_action_day") <= 1) {
    // errmsg("發送延遲簡訊時, 延遲執行天數 須大於 1");
    // return;
    // }
    //
    //
    // if (!wp.item_empty("block_reason4") && !wp.item_empty("block_reason5")) {
    // errmsg("凍結碼[4],[5] 不可同時有值");
    // return;
    // }

    if (this.ibAdd) {
      return;
    }


    sqlWhere = " where 1=1 " + " and hex(rowid) =?";
    Object[] parms = new Object[] {kk1};
    if (this.isOtherModify("bil_fraud_report", sqlWhere, parms)) {
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

    busi.SqlPrepare spi = new SqlPrepare();
    spi.sql2Insert("bil_fraud_report");
    spi.ppstr("function_code", wp.itemStr("function_code"));
    spi.ppstr("bin_type", wp.itemStr("bin_type"));
    spi.ppstr("card_no", wp.itemStr("card_no"));
    spi.ppstr("film_no", wp.itemStr("film_no"));
    spi.ppstr("purchase_date", wp.itemStr("purchase_date"));
    spi.ppstr("fraud_type", wp.itemStr("fraud_type"));
    spi.ppnum("fraud_amt", wp.itemNum("fraud_amt"));
    spi.ppstr("mcht_category", wp.itemStr("mcht_category"));
    spi.ppstr("mcht_eng_name", wp.itemStr("mcht_eng_name"));
    spi.ppstr("mcht_city", wp.itemStr("mcht_city"));
    spi.ppstr("mcht_zip", wp.itemStr("mcht_zip"));
    spi.ppstr("pos_entry_mode", wp.itemStr("pos_entry_mode"));
    spi.ppstr("crt_date", getSysDate());
    spi.ppstr("mod_user", wp.loginUser);
    spi.addsql(", mod_time ", ", sysdate ");
    spi.ppstr("mod_pgm", wp.modPgm());
    spi.ppnum("mod_seqno", 1);

    sqlExec(spi.sqlStmt(), spi.sqlParm());

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
    sp.sql2Update("bil_fraud_report");
    sp.ppstr("function_code", wp.itemStr("function_code"));
    sp.ppstr("bin_type", wp.itemStr("bin_type"));
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("film_no", wp.itemStr("film_no"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("fraud_type", wp.itemStr("fraud_type"));
    sp.ppnum("fraud_amt", wp.itemNum("fraud_amt"));
    sp.ppstr("mcht_category", wp.itemStr("mcht_category"));
    sp.ppstr("mcht_eng_name", wp.itemStr("mcht_eng_name"));
    sp.ppstr("mcht_city", wp.itemStr("mcht_city"));
    sp.ppstr("mcht_zip", wp.itemStr("mcht_zip"));
    sp.ppstr("pos_entry_mode", wp.itemStr("pos_entry_mode"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
    sp.sql2Where(" where hex(rowid)=?", kk1);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    strSql = "delete bil_fraud_report " + " where hex(rowid) =:rowid ";
    item2ParmStr("rowid");


    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;

  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
