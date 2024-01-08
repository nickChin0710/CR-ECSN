/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Ptrm3030Func extends FuncEdit {
  String stmtCycle;

  public Ptrm3030Func(TarokoCommon wr) {
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
      stmtCycle = wp.itemStr("kk_stmt_cycle");
    } else {
      stmtCycle = wp.itemStr("stmt_cycle");
    }
    if (isEmpty(stmtCycle)) {
      errmsg("資料鍵值 不可空白");
      return;
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from col_m0_parm where stmt_cycle = ? ";
      Object[] param = new Object[] {stmtCycle};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = "where stmt_cycle = ? ";
      Object[] param = new Object[] {stmtCycle};
      isOtherModify("col_m0_parm", sqlWhere, param);
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_m0_parm");
    sp.ppstr("stmt_cycle", stmtCycle);
    sp.ppstr("other_deduct_flag", wp.itemStr("other_deduct_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("self_deduct_flag", wp.itemStr("self_deduct_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("self_pay_flag", wp.itemStr("self_pay_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("af_flag", wp.itemStr("af_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("lf_flag", wp.itemStr("lf_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("pn_flag", wp.itemStr("pn_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("ri_flag", wp.itemStr("ri_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("pf_flag", wp.itemStr("pf_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("cf_flag", wp.itemStr("cf_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("min_mp_amt", empty(wp.itemStr("min_mp_amt")) ? "0" : wp.itemStr("min_mp_amt"));
    sp.ppstr("normal_months",
        empty(wp.itemStr("normal_months")) ? "0" : wp.itemStr("normal_months"));
    sp.ppstr("exceed_pay_days",
        empty(wp.itemStr("exceed_pay_days")) ? "0" : wp.itemStr("exceed_pay_days"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("apr_user", wp.itemStr("approval_user"));
    sp.addsql(", apr_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("apr_flag", "Y");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());

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
    sp.sql2Update("col_m0_parm");
    sp.ppstr("other_deduct_flag", wp.itemStr("other_deduct_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("self_deduct_flag", wp.itemStr("self_deduct_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("self_pay_flag", wp.itemStr("self_pay_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("af_flag", wp.itemStr("af_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("lf_flag", wp.itemStr("lf_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("pn_flag", wp.itemStr("pn_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("ri_flag", wp.itemStr("ri_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("pf_flag", wp.itemStr("pf_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("cf_flag", wp.itemStr("cf_flag").equals("Y") ? "Y" : "N");
    sp.ppstr("min_mp_amt", empty(wp.itemStr("min_mp_amt")) ? "0" : wp.itemStr("min_mp_amt"));
    sp.ppstr("normal_months",
        empty(wp.itemStr("normal_months")) ? "0" : wp.itemStr("normal_months"));
    sp.ppstr("exceed_pay_days",
        empty(wp.itemStr("exceed_pay_days")) ? "0" : wp.itemStr("exceed_pay_days"));
    sp.ppstr("apr_user", wp.itemStr("approval_user"));
    sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
    sp.ppstr("apr_flag", "Y");
    sp.addsql(", mod_time = sysdate ", "");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.sql2Where(" where stmt_cycle = ?", stmtCycle);

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

    strSql = "delete col_m0_parm " + sqlWhere;
    Object[] param = new Object[] {stmtCycle};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = -1;
    }
    return rc;
  }

}
