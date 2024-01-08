/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-15  V1.00.00  yash       program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                             *
******************************************************************************/

package dbam01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Dbam0050Func extends FuncEdit {
  String rowid = "";
  String acctType = "", lostCode = "", groupCode = "";

  public Dbam0050Func(TarokoCommon wr) {
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
    rowid = wp.itemStr("rowid");
    acctType = wp.itemStr("acct_type");
    lostCode = wp.itemStr("lost_code");
    groupCode = wp.itemStr("group_code");

    if (isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from dba_lostgrp where acct_type = ? and lost_code = ? and group_code = ? ";
      Object[] param = new Object[] {acctType, lostCode, groupCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }
    } else {
      if (isUpdate()) {
        // 檢查修改資料是否重複(因為 group_code 可被修改)
        String lsSql =
            "select count(*) as tot_cnt from dba_lostgrp where acct_type = ? and lost_code = ? "
                + "and group_code = ? and hex(rowid) <> ? ";
        Object[] param = new Object[] {acctType, lostCode, groupCode, rowid};
        sqlSelect(lsSql, param);
        if (colNum("tot_cnt") > 0) {
          errmsg("團體代號已存在，無法修改");
          return;
        }
      }
      // -other modify-
      sqlWhere = " where hex(rowid) = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {rowid, wp.modSeqno()};
      isOtherModify("dba_lostgrp", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dba_lostgrp");
    sp.ppstr("acct_type", acctType);
    sp.ppstr("lost_code", lostCode);
    sp.ppstr("group_code", groupCode);
    sp.ppstr("description", wp.itemStr("tt_lost_code"));
    sp.ppnum("normal_major", wp.itemNum("normal_major"));
    sp.ppnum("normal_sub", wp.itemNum("normal_sub"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("apr_user", wp.itemStr("approval_user"));
    sp.addsql(", apr_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
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
    sp.sql2Update("dba_lostgrp");
    sp.ppstr("group_code", groupCode);
    sp.ppnum("normal_major", wp.itemNum("normal_major"));
    sp.ppnum("normal_sub", wp.itemNum("normal_sub"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("apr_user", wp.itemStr("approval_user"));
    sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid) = ? ", rowid);
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

    strSql = "delete dba_lostgrp " + sqlWhere;
    Object[] param = new Object[] {rowid, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    return rc;
  }

}
