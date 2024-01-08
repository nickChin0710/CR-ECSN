/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-11  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/
package ipsm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Ipsm0030Func extends FuncEdit {

  public Ipsm0030Func(TarokoCommon wr) {
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
      if (this.isOtherModify("ips_comm_parm", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }

      sqlWhere = " where 1=1 and hex(rowid) = ? " + " and nvl(mod_seqno,0) = ?";
      Object[] param2 = new Object[] {wp.itemStr("rowid_black"), wp.itemStr("mod_seqno_black")};
      if (this.isOtherModify("ips_comm_parm", sqlWhere, param2)) {
        errmsg("請重新查詢 !");
        return;
      }
    }

  }

  @Override
  public int dbInsert() {

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    // -AUTOOFF_OFF-
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("ips_comm_parm");
    sp.ppstr("stop1_cond", wp.itemStr("stop1_cond"));
    sp.ppnum("stop1_days", wp.itemNum("stop1_days"));
    sp.ppstr("stop2_cond", wp.itemStr("stop2_cond"));
    sp.ppnum("stop2_days", wp.itemNum("stop2_days"));
    sp.ppstr("mcode_cond", wp.itemStr("mcode_cond"));
    sp.ppstr("payment_rate", wp.itemStr("payment_rate"));
    sp.ppnum("mcode_amt", wp.itemNum("mcode_amt"));
    sp.ppstr("block_cond", wp.itemStr("block_cond"));
    sp.ppstr("block_codes", dropComma(wp.itemStr("block_codes")));
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", ", apr_date = to_char(sysdate,'YYYYMMDD')");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid)=?", wp.itemStr("rowid"));
    sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
      return rc;
    }
    // -黑名單-
    sp.sql2Update("ips_comm_parm");
    sp.ppstr("stop1_cond", wp.itemStr("stop1_cond_black"));
    sp.ppnum("stop1_days", wp.itemNum("stop1_days_black"));
    sp.ppstr("stop2_cond", wp.itemStr("stop2_cond_black"));
    sp.ppnum("stop2_days", wp.itemNum("stop2_days_black"));
    sp.ppstr("stop3_cond", wp.itemStr("stop3_cond_black"));
    sp.ppnum("stop3_days", wp.itemNum("stop3_days_black"));
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", ", apr_date = to_char(sysdate,'YYYYMMDD')");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid)=?", wp.itemStr("rowid_black"));
    sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno_black"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
      return rc;
    }
    return rc;
  }

  @Override
  public int dbDelete() {

    return rc;
  }


  private String dropComma(String data) {
    String buf = "";
    String[] datas = data.split(",");
    for (String dat : datas) {
      buf = buf + dat;
    }
    return buf;
  }
}
