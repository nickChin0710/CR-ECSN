/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-23   V1.00.02 Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi   coding standard, rename                  * 
******************************************************************************/
package ptrm02;

import busi.FuncEdit;
import taroko.base.CommSqlStr;
import taroko.com.TarokoCommon;

public class Ptrm0320Func extends FuncEdit {
  String acctType = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
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
      acctType = wp.itemStr("kk_acct_type");
    } else {
      acctType = wp.itemStr("acct_type");
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where 1=1 and  acct_type = ? and nvl(mod_seqno,0) =nvl(?,0) " ;


    if (this.isOtherModify("PTR_PUR_LMT", sqlWhere, new Object[] {acctType, wp.modSeqno()})) {
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

    busi.SqlPrepare spp = new busi.SqlPrepare();
    spp.sql2Insert("ptr_pur_lmt");
    spp.ppstr("acct_type", acctType);
    spp.ppint("month_n", (int) wp.itemNum("month_n"));
    spp.ppnum("PERCENT_A", wp.itemNum("percent_a"));
    spp.ppnum("HFIX_A", wp.itemNum("hfix_a"));
    spp.ppnum("HPERC_A", wp.itemNum("hperc_a"));
    spp.ppnum("LFIX_A", wp.itemNum("lfix_a"));
    spp.ppnum("LPERC_A", wp.itemNum("lperc_a"));
    spp.ppnum("PERCENT_B", wp.itemNum("percent_b"));
    spp.ppnum("HFIX_B", wp.itemNum("hfix_b"));
    spp.ppnum("HPERC_B", wp.itemNum("hperc_b"));
    spp.ppnum("LFIX_B", wp.itemNum("lfix_b"));
    spp.ppnum("LPREC_B", wp.itemNum("lprec_b"));
    spp.ppnum("PERCENT_C", wp.itemNum("percent_c"));
    spp.ppnum("LFIX_C", wp.itemNum("lfix_c"));
    spp.modxxx(modUser, modPgm);

    sqlExec(spp.sqlStmt(), spp.sqlParm());
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
    strSql = "update PTR_PUR_LMT set " + " MONTH_N =?, " + " PERCENT_A =?, " + " HFIX_A =?, "
        + " HPERC_A =?, " + " LFIX_A =?, " + " LPERC_A =?, " + " PERCENT_B =?, " + " HFIX_B =?, "
        + " HPERC_B =?, " + " LFIX_B =?, " + " LPREC_B =?, " + " PERCENT_C =?, " + " LFIX_C =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemNum("MONTH_N"), wp.itemNum("PERCENT_A"),
        wp.itemNum("HFIX_A"), wp.itemNum("HPERC_A"), wp.itemNum("LFIX_A"), wp.itemNum("LPERC_A"),
        wp.itemNum("PERCENT_B"), wp.itemNum("HFIX_B"), wp.itemNum("HPERC_B"), wp.itemNum("LFIX_B"),
        wp.itemNum("LPREC_B"), wp.itemNum("PERCENT_C"), wp.itemNum("LFIX_C"), wp.loginUser,
        wp.itemStr("mod_pgm"),acctType, wp.modSeqno()};
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
    strSql = "delete PTR_PUR_LMT " + sqlWhere;
    // ddd("del-sql="+is_sql);
    rc = sqlExec(strSql, new Object[] {acctType, wp.modSeqno()});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
