/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-11  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ipsm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Ipsm0020Func extends FuncEdit {

  public Ipsm0020Func(TarokoCommon wr) {
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

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      /*
       * String lsSql =
       * "select count(*) as tot_cnt from crd_emboss_tmp where batchno = ? and recno = ? "; Object[]
       * param = new Object[] {wp.item_ss("batchno"),wp.item_ss("recno")}; sqlSelect(lsSql, param);
       * if (col_num("tot_cnt") > 0) { errmsg("資料已存在，無法新增,請從新查詢"); return; }
       */

    } else {
      // -other modify-
      sqlWhere = " where 1=1 and hex(rowid) = ? " + " and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {wp.itemStr("rowid"), wp.modSeqno()};
      if (this.isOtherModify("ips_autooff_log", sqlWhere, param)) {
        errmsg("請重新查詢 !");
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
    sp.sql2Insert("ips_autooff_log");
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("from_mark", "1");
    sp.ppstr("ips_card_no", wp.itemStr("ips_card_no"));
    sp.ppstr("autoload_flag", wp.itemStr("autoload_flag"));
    sp.ppstr("risk_remark", wp.itemStr("risk_remark"));
    sp.ppstr("send_date", wp.itemStr("send_date"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", crt_time ", ", to_char(sysdate,'hhmmss') ");
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
    sp.sql2Update("ips_autooff_log");
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("from_mark", wp.itemStr("from_mark"));
    sp.ppstr("ips_card_no", wp.itemStr("ips_card_no"));
    sp.ppstr("autoload_flag", wp.itemStr("autoload_flag"));
    sp.ppstr("risk_remark", wp.itemStr("risk_remark"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid)=?", wp.itemStr("rowid"));
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
    strSql = "delete ips_autooff_log where hex(rowid) = ? and mod_seqno = ? ";
    Object[] param = new Object[] {wp.itemStr("rowid"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = -1;
    }
    return rc;
  }

}
