/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-11  V1.00.00  Andy       program initial                            *
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm0710Func extends FuncEdit {
  String kkMchtGroupId = "";
  String kkMchtGroupDesc = "";
  String kkRowid = "";

  public Mktm0710Func(TarokoCommon wr) {
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
    kkMchtGroupId = varsStr("aa_mcht_group_id");
    kkMchtGroupDesc = varsStr("aa_mcht_group_desc");
    kkRowid = varsStr("aa_rowid");

    // 檢查新增資料是否重複
    String lsSql = "select count(*) as tot_cnt from mkt_mcht_group where mcht_group_id = ?";
    Object[] param = new Object[] {kkMchtGroupId};
    sqlSelect(lsSql, param);
    if (colNum("tot_cnt") > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料已存在，無法存檔')</script>";
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return -1;

    strSql = " insert into mkt_mcht_group ( " + " mcht_group_id, " + " mcht_group_desc, "
        + " apr_user, " + " apr_date, " + " mod_time, mod_user, mod_pgm, mod_seqno " + " ) "
        + " values( " + " ?,?,?,?" + " , sysdate, ?, ?, 1 " + " ) ";

    Object[] param = new Object[] {kkMchtGroupId, kkMchtGroupDesc, varsStr("aa_apr_user"),
        getSysDate(), wp.loginUser, wp.modPgm()};

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;

  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    kkMchtGroupId = varsStr("aa_mcht_group_id");
    kkMchtGroupDesc = varsStr("aa_mcht_group_desc");
    kkRowid = varsStr("aa_rowid");
    strSql = " update mkt_mcht_group set " + " mcht_group_id = ? " + " ,mcht_group_desc = ? "
        + " ,apr_user = ? " + " ,apr_date = ? " + " ,mod_time = sysdate " + " ,mod_user = ? "
        + " ,mod_pgm = ?" + " ,mod_seqno = nvl(mod_seqno,0)+1 " + " where hex(rowid) = ? ";

    // System.out.println(is_sql);
    Object[] param = new Object[] {kkMchtGroupId, kkMchtGroupDesc, varsStr("aa_apr_user"),
        getSysDate(), wp.loginUser, wp.modPgm(), kkRowid};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    // dataCheck();
    kkRowid = varsStr("aa_rowid");
    Object[] param = new Object[] {kkRowid};

    strSql = "delete mkt_mcht_group where hex(rowid) = ?";

    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;
  }

}

