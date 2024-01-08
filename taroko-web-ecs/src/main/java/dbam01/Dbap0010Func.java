/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package dbam01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Dbap0010Func extends FuncEdit {
  String acctType;
  int maxSeqno;

  public Dbap0010Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

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
    acctType = wp.itemStr("acct_type");

    if (this.isAdd()) {
      // 取得seqno
      String lsSql =
          "select nvl(max(seqno),0) as max_seqno from dbp_prod_type where acct_type = ? ";
      Object[] param = new Object[] {acctType};
      sqlSelect(lsSql, param);
      maxSeqno = (int) colNum("max_seqno") + 1;
      System.out.println(
          this.actionCode + ", kk1=" + acctType + ", kk2=" + maxSeqno + ", mod_seqno=" + wp.modSeqno());
    } else {
      maxSeqno = (int) wp.itemNum("seqno");
      sqlWhere = "where acct_type = ? " + "and seqno = ? " + "and mod_seqno = ? ";
      Object[] param = new Object[] {acctType, maxSeqno, wp.modSeqno()};
      if (isOtherModify("dbp_prod_type", sqlWhere, param)) {
        return;
      }
    }

    return;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dbp_prod_type");
    sp.ppstr("acct_type", acctType);
    sp.ppint("seqno", maxSeqno);
    sp.ppstr("group_code", wp.itemStr("group_code"));
    sp.ppstr("card_type", wp.itemStr("card_type"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "0");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("dbp_prod_type");
    sp.ppstr("group_code", wp.itemStr("group_code"));
    sp.ppstr("card_type", wp.itemStr("card_type"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where acct_type=?", acctType);
    sp.sql2Where(" and seqno=?", maxSeqno);
    sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete dbp_prod_type " + sqlWhere;
    Object[] param = new Object[] {acctType, maxSeqno, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    return rc;
  }

  public int updateDbpAcctType() {
    msgOK();
    acctType = wp.itemStr("acct_type");

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("dbp_acct_type");
    sp.ppstr("chin_name", wp.itemStr("chin_name"));
    sp.ppstr("curr_code", wp.itemStr("curr_code"));
    sp.ppstr("card_indicator", wp.itemStr("card_indicator"));
    sp.ppstr("f_currency_flag", wp.itemStr("f_currency_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("u_cycle_flag", wp.itemStr("u_cycle_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
    sp.ppnum("inst_crdtamt", wp.itemNum("inst_crdtamt"));
    sp.ppnum("inst_crdtrate", wp.itemNum("inst_crdtrate"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where acct_type = ?", acctType);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

  public int dbDelete_detl() {
    msgOK();
    acctType = varsStr("acct_type");
    maxSeqno = varsInt("seqno");

    strSql = "delete dbp_prod_type " + "where acct_type = ? and seqno = ? ";
    Object[] param = new Object[] {acctType, maxSeqno};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    return rc;
  }

}
