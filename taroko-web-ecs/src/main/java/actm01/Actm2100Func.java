/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-22  V1.00.01  ryan       program initial                            *
*109-04-21  V1.00.02  shiyuqi       updated for project coding standard     *                                                                             *
******************************************************************************/
package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2100Func extends FuncEdit {
  String lsLog = "";

  public Actm2100Func(TarokoCommon wr) {
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
    lsLog = String.format("%-1s",
        isEmpty(wp.itemStr("ex_rc_use_b_adj")) ? 0 : wp.itemStr("ex_rc_use_b_adj"))
        + String.format("%-1s", wp.itemStr("ex_rc_use_indicator"))
        + String.format("%-8s", wp.itemStr("ex_rc_use_s_date"))
        + String.format("%-8s", wp.itemStr("ex_rc_use_e_date"))
        + String.format("%-1s", wp.itemStr("ex_acct_status"));
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from act_dual  where func_code = '0708' and dual_key=? ";
      Object[] param = new Object[] {varsStr("ls_dual_no")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增,請從新查詢");
        return;
      }

    } else {
      // -other modify-
      sqlWhere = " where 1=1 and dual_key = ? and func_code = '0708'  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {varsStr("ls_dual_no"), wp.itemStr("mod_seqno")};
      if (this.isOtherModify("act_dual", sqlWhere, param)) {
        errmsg("請重新查詢 !");
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
    sp.sql2Insert("act_dual");
    sp.ppstr("func_code", "0708");
    sp.ppstr("dual_key", varsStr("ls_dual_no"));
    sp.ppstr("aud_type", "u");
    sp.ppstr("log_data", lsLog);
    sp.ppstr("chg_user", wp.loginUser);
    sp.addsql(", chg_date ", ", to_char(sysdate,'yyyymmdd') ");
    // sp.addsql(", update_time ",", to_char(sysdate,'hhmmss') ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
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
    sp.sql2Update("act_dual");
    sp.ppstr("log_data", lsLog);
    sp.ppstr("chg_user", wp.loginUser);
    sp.addsql(", chg_date = to_char(sysdate,'yyyymmdd') ");
    // sp.addsql(", update_time = to_char(sysdate,'hhmmss') ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", ", mod_seqno =nvl(mod_seqno,0)+1 ");
    sp.sql2Where(" where dual_key=?", varsStr("ls_dual_no"));
    sp.sql2Where(" and func_code='0708'", "");
    sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno"));
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
    if (rc != 1) {
      return rc;
    }
    strSql = "delete act_dual " + sqlWhere;
    Object[] param = new Object[] {varsStr("ls_dual_no"), wp.itemStr("mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    return rc;
  }

}
