/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-16  V1.00.00  ryan               program initial                    *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
******************************************************************************/

package mktm02;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Mktm4210Func extends FuncEdit {
 // String kk1 = "";
  //String kk2 = "";

  public Mktm4210Func(TarokoCommon wr) {
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
    // kk1 = vars_ss("aa_rowid");
    /*
     * // 檢查新增資料是否重複 String lsSql =
     * "select count(*) as tot_cnt from crd_mail_parm where emboss_source = ? and mail_type= ?" ;
     * Object[] param = new Object[] { kk1, kk2 }; sqlSelect(lsSql, param); if (col_num("tot_cnt") >
     * 0) { //errmsg("資料已存在，無法存檔"); rc = 0; }
     */

    // -other modify-
    // sql_where = " where hex(rowid)=? and nvl(mod_seqno,0) = ? ";
    // Object[] param2 = new Object[] { kk1, vars_ss("aa_mod_seqno") };
    // if (this.other_modify("crd_mail", sql_where, param2)) {
    // errmsg("請重新查詢 !");
    // }
  }

  @Override
  public int dbInsert() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("mkt_contri_parm_t");
    sp.ppstr("item_no", varsStr("a_item_no"));
    sp.ppstr("input_type", "2");
    sp.ppstr("key_data", varsStr("a_key_data"));
    sp.ppstr("key_type", varsStr("a_key_type"));
    sp.ppstr("cost_month", varsStr("a_cost_month"));
    sp.ppstr("cost_month2", varsStr("a_cost_month2"));
    sp.ppnum("cost_amt", varsNum("a_cost_amt"));
    sp.ppnum("exist_cost_months", varsNum("a_exist_cost_months"));
    sp.ppstr("crt_user", varsStr("a_crt_user"));
    sp.ppstr("crt_date", varsStr("a_crt_date"));
    sp.ppnum("purch_mm", varsNum("a_purch_mm"));
    sp.ppnum("service_amt", varsNum("a_service_amt"));
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
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
    sp.sql2Update("mkt_contri_parm_t");
    sp.ppstr("input_type", "2");
    sp.ppstr("cost_month2", varsStr("a_cost_month2"));
    sp.ppnum("cost_amt", varsNum("a_cost_amt"));
    sp.ppnum("exist_cost_months", varsNum("a_exist_cost_months"));
    sp.ppstr("crt_user", varsStr("a_crt_user"));
    sp.ppstr("crt_date", varsStr("a_crt_date"));
    sp.ppnum("purch_mm", varsNum("a_purch_mm"));
    sp.ppnum("service_amt", varsNum("a_service_amt"));
    sp.addsql(", mod_time = sysdate ", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where item_no=?", varsStr("a_item_no"));
    sp.sql2Where(" and key_data=?", varsStr("a_key_data"));
    sp.sql2Where(" and key_type=?", varsStr("a_key_type"));
    sp.sql2Where(" and cost_month=?", varsStr("a_cost_month"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }


  @Override
  public int dbDelete() {
    Object[] param = new Object[] {varsStr("a_rowid"), varsNum("a_mod_seqno")};

    strSql = "delete mkt_contri_parm_t where hex(rowid) = ? " + " and mod_seqno = ?";

    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

  public int dbDelete2() {
    Object[] param2 = new Object[] {varsStr("b_rowid"), varsNum("b_mod_seqno")};

    strSql = "delete mkt_contri_parm where hex(rowid) = ? " + " and mod_seqno = ?";

    rc = sqlExec(strSql, param2);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
