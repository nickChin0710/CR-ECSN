/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109-05-06  V1.00.00  Aoyulan       updated for project coding standard     *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm1210Func extends FuncEdit {
  String rowid;
  String modSeqno;

  public Colm1210Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      rowid = wp.itemStr("id_corp_no");
    } else {
      rowid = wp.itemStr("rowid");
      modSeqno = wp.modSeqno();
    }
    if (isEmpty(rowid)) {
      errmsg("資料鍵值 不可空白");
      return;
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from col_lgd_901 where id_corp_no = ? and from_type = '1' ";
      Object[] param = new Object[] {rowid};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = "where hex(rowid) = ? " + "and mod_seqno = ? ";
      Object[] param = new Object[] {rowid, modSeqno};
      isOtherModify("col_lgd_901", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_lgd_901");
    sp.ppstr("id_corp_no", rowid);
    sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno")); // phopho add
    sp.ppstr("id_corp_type", wp.itemStr("id_corp_type"));
    sp.ppstr("lgd_type", wp.itemStr("lgd_type"));
    sp.ppstr("lgd_reason", wp.itemStr("lgd_reason"));
    sp.ppstr("lgd_early_ym", wp.itemStr("lgd_early_ym"));
    sp.ppstr("from_type", wp.itemStr("from_type"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("close_flag", "N");
    sp.ppstr("revol_rate", "0");
    sp.ppstr("risk_amt", isEmpty(wp.itemStr("risk_amt")) ? "0" : wp.itemStr("risk_amt"));
    sp.ppstr("acct_type_s", varsStr("acct_type_s"));
    sp.ppstr("acct_status_s", varsStr("acct_status_s"));
    sp.ppstr("acno_stop_s", varsStr("acno_stop_s"));
    sp.ppstr("lgd_remark", wp.itemStr("lgd_remark"));
    sp.ppstr("overdue_ym", wp.itemStr("overdue_ym"));
    sp.ppstr("overdue_amt", isEmpty(wp.itemStr("overdue_amt")) ? "0" : wp.itemStr("overdue_amt"));
    sp.ppstr("coll_ym", wp.itemStr("coll_ym"));
    sp.ppstr("coll_amt", isEmpty(wp.itemStr("coll_amt")) ? "0" : wp.itemStr("coll_amt"));
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
    sp.sql2Update("col_lgd_901");
    sp.ppstr("lgd_early_ym", wp.itemStr("lgd_early_ym"));
    sp.ppstr("lgd_remark", wp.itemStr("lgd_remark"));
    if (varsStr("needSign").equals("Y")) {
      sp.ppstr("apr_date", "");
      sp.ppstr("apr_user", "");
    }
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid) = ?", rowid);
    sp.sql2Where(" and nvl(mod_seqno,0) = ?", modSeqno);
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

    strSql = "delete col_lgd_901 " + sqlWhere;
    Object[] param = new Object[] {rowid, modSeqno};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = -1;
    }
    return rc;
  }

}
