/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-18  V1.00.00  ryan       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     * 
* 109-11-19  V1.00.02  Ryan       移除畫面部分欄位與邏輯     
* 111-08-11  V1.00.03  machao     特店代號之後增加”分期期數” 欄位                                                      *      
* 111-10-11  V1.00.04  Ryan     product_no->調整寫入DB時的資料型態
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0710Func extends FuncEdit {
 // String kk1 = "", 
  String lsDesc = "";

  public Bilm0710Func(TarokoCommon wr) {
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
    if (wp.itemStr("destination_amt_flag").equals("Y") && empty(wp.itemStr("destination_amt"))) {
      errmsg("請輸入單筆消費門檻");
      return;
    }
    if (wp.itemStr("payment_rate_flag").equals("Y") && empty(wp.itemStr("payment_rate"))) {
      errmsg("請輸入繳款記錄檢核期數");
      return;
    }
    if (wp.itemStr("payment_rate_flag").equals("Y") && wp.itemNum("payment_rate") > 25) {
      errmsg("繳款記錄檢核期數最多25");
      return;
    }
    if (wp.itemStr("rc_rate_flag").equals("Y") && empty(wp.itemStr("rc_rate"))) {
      errmsg("請輸入循環信用比例");
      return;
    }
    if (wp.itemStr("rc_rate_flag").equals("Y") && empty(wp.itemStr("credit_amt_rate"))) {
      errmsg("請輸入額度使用比例");
      return;
    }

    if (this.isAdd()) {
      if (empty(wp.itemStr("kk_mcht_no"))) {
        errmsg("請輸入特店代號");
        return;
      }
      
      if (empty(wp.itemStr("kk_product_no"))) {
          errmsg("請輸入分期期數");
          return;
        }
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from bil_auto_parm where mcht_no = ? and product_no = ? ";
      Object[] param = new Object[] {wp.itemStr("kk_mcht_no"),wp.itemStr("product_no")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      if (empty(wp.itemStr("mcht_no"))) {
        errmsg("請重新查詢");
        return;
      }
      // -other modify-
      sqlWhere = " where mcht_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param2 = new Object[] {wp.itemStr("mcht_no"), wp.modSeqno()};
      if (isOtherModify("bil_auto_parm", sqlWhere, param2)) {
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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_auto_parm");
    sp.ppstr("mcht_no", wp.itemStr("kk_mcht_no"));
    sp.ppstr("action_desc", wp.itemStr("action_desc"));
    sp.ppstr("effc_date_b", wp.itemStr("effc_date_b"));
    sp.ppstr("effc_date_e", wp.itemStr("effc_date_e"));
    sp.ppstr("product_no", wp.itemStr("kk_product_no"));

    if (empty(wp.itemStr("destination_amt_flag"))) {
      sp.ppstr("destination_amt_flag", "N");
    } else
      sp.ppstr("destination_amt_flag", "Y");

    sp.ppstr("destination_amt", wp.itemStr("destination_amt"));

    if (empty(wp.itemStr("payment_rate_flag"))) {
      sp.ppstr("payment_rate_flag", "N");
    } else
      sp.ppstr("payment_rate_flag", "Y");

    sp.ppstr("payment_rate", wp.itemStr("payment_rate"));

    if (empty(wp.itemStr("rc_rate_flag"))) {
      sp.ppstr("rc_rate_flag", "N");
    } else
      sp.ppstr("rc_rate_flag", "Y");

    sp.ppstr("rc_rate", wp.itemStr("rc_rate"));
    sp.ppstr("credit_amt_rate", wp.itemStr("credit_amt_rate"));

    if (empty(wp.itemStr("mcc_code_flag"))) {
      sp.ppstr("mcc_code_flag", "N");
    } else
      sp.ppstr("mcc_code_flag", "Y");

    if (empty(wp.itemStr("mcht_flag"))) {
      sp.ppstr("mcht_flag", "N");
    } else
      sp.ppstr("mcht_flag", "Y");

    if (empty(wp.itemStr("over_credit_amt_flag"))) {
      sp.ppstr("over_credit_amt_flag", "N");
    } else
      sp.ppstr("over_credit_amt_flag", "Y");

    if (empty(wp.itemStr("block_reason_flag"))) {
      sp.ppstr("block_reason_flag", "N");
    } else
      sp.ppstr("block_reason_flag", "Y");

    if (empty(wp.itemStr("spec_status_flag"))) {
      sp.ppstr("spec_status_flag", "N");
    } else
      sp.ppstr("spec_status_flag", "Y");

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
    sp.sql2Insert("bil_auto_parm_data");
    sp.ppstr("mcht_no", wp.itemStr("action_code"));
    sp.ppstr("product_no",wp.itemStr("product_no"));
    sp.ppstr("data_type", varsStr("data_type"));
    sp.ppstr("data_code", varsStr("data_code"));
    // sp.ppss("data_code2",vars_ss("data_code2"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", varsStr("type_desc"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    if (sqlRowNum < 0) {
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
    sp.sql2Insert("bil_auto_parm_data");
    sp.ppstr("mcht_no", wp.itemStr("action_code"));
    sp.ppstr("data_type", varsStr("data_type"));
    sp.ppstr("data_code", varsStr("data_code"));
    sp.ppstr("data_code2", varsStr("data_code2"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("type_desc", varsStr("type_desc"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("product_no",varsStr("product_no")); 
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    if (sqlRowNum < 0) {
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
    sp.sql2Update("bil_auto_parm");
    sp.ppstr("mcht_no", wp.itemStr("mcht_no"));
    sp.ppstr("action_desc", wp.itemStr("action_desc"));
    sp.ppstr("effc_date_b", wp.itemStr("effc_date_b"));
    sp.ppstr("effc_date_e", wp.itemStr("effc_date_e"));
//    sp.ppstr("product_no", wp.itemStr("product_no"));

    if (empty(wp.itemStr("destination_amt_flag"))) {
      sp.ppstr("destination_amt_flag", "N");
    } else
      sp.ppstr("destination_amt_flag", "Y");

    sp.ppstr("destination_amt", wp.itemStr("destination_amt"));

    if (empty(wp.itemStr("payment_rate_flag"))) {
      sp.ppstr("payment_rate_flag", "N");
      lsDesc = "指定繳款評等";
      dbDelete();
    } else
      sp.ppstr("payment_rate_flag", "Y");

    sp.ppstr("payment_rate", wp.itemStr("payment_rate"));

    if (empty(wp.itemStr("rc_rate_flag"))) {
      sp.ppstr("rc_rate_flag", "N");
    } else
      sp.ppstr("rc_rate_flag", "Y");

    sp.ppstr("rc_rate", wp.itemStr("rc_rate"));
    sp.ppstr("credit_amt_rate", wp.itemStr("credit_amt_rate"));

    if (empty(wp.itemStr("mcc_code_flag"))) {
      sp.ppstr("mcc_code_flag", "N");
      lsDesc = "排除MccCode";
      dbDelete();
    } else
      sp.ppstr("mcc_code_flag", "Y");

    if (empty(wp.itemStr("mcht_flag"))) {
      sp.ppstr("mcht_flag", "N");
      lsDesc = "排除特店代號";
      dbDelete();
    } else
      sp.ppstr("mcht_flag", "Y");

    if (empty(wp.itemStr("over_credit_amt_flag"))) {
      sp.ppstr("over_credit_amt_flag", "N");
    } else
      sp.ppstr("over_credit_amt_flag", "Y");

    if (empty(wp.itemStr("block_reason_flag"))) {
      sp.ppstr("block_reason_flag", "N");
      lsDesc = "不排除之凍結碼";
      dbDelete();
    } else
      sp.ppstr("block_reason_flag", "Y");

    if (empty(wp.itemStr("spec_status_flag"))) {
      sp.ppstr("spec_status_flag", "N");
      lsDesc = "不排除之特指戶";
      dbDelete();
    } else
      sp.ppstr("spec_status_flag", "Y");

    if (empty(wp.itemStr("rerisk_group_flag"))) {
      sp.ppstr("rerisk_group_flag", "N");
      lsDesc = "指定風險族群2";
      dbDelete();
    } else
      sp.ppstr("rerisk_group_flag", "Y");

    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where mcht_no=?", wp.itemStr("mcht_no"));
    sp.sql2Where(" and product_no = ? ", wp.itemStr("product_no"));
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_auto_parm_data  where mcht_no = ? and product_no = ? and type_desc = ? ";
    Object[] param = new Object[] {wp.itemStr("mcht_no"), wp.itemStr("product_no"), lsDesc};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
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
    strSql = "delete bil_auto_parm_data  where hex(rowid) = ? ";
    Object[] param = new Object[] {varsStr("rowid1")};
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
    strSql = "delete bil_auto_parm_data  where mcht_no = ? and product_no = ? ";
    Object[] param = new Object[] {wp.itemStr("action_code"),wp.itemStr("product_no")};

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
    strSql =
        "delete bil_auto_parm_data  where mcht_no=? and product_no = ? and data_type='10' and type_desc='排除特店代號' ";
    Object[] param = new Object[] {varsStr("data_mcht"),wp.itemStr("product_no")};
    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
