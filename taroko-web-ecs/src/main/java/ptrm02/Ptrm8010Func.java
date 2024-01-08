/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-24  V1.00.02  Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名
* 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *                                                                                            *    
******************************************************************************/
package ptrm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm8010Func extends FuncEdit {
  String wfParm = "", wfKey = "";

  public Ptrm8010Func(TarokoCommon wr) {
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
      wfParm = wp.itemStr("kk_wf_parm");
    } else {
      wfParm = wp.itemStr("wf_parm");
    }

    if (this.ibAdd) {
      wfKey = wp.itemStr("kk_wf_key");
    } else {
      wfKey = wp.itemStr("wf_key");
    }

    if (this.isAdd()) {
      if (empty(wfParm) || empty(wfKey)) {
        errmsg("參數類別, 鍵值: 不可空白");
      }
      return;
    }
    if (empty("wf_desc")) {
      errmsg("說明： 不可空白");
      return;
    }

    sqlWhere = " where wf_parm= ? and wf_key= ? and nvl(mod_seqno,0) = ? ";
    if (this.isOtherModify("ptr_sys_parm", sqlWhere, new Object[] {wfParm,wfKey, wp.modSeqno()})) {
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
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("ptr_sys_parm", wp);
    sp.ppstr("wf_parm", wfParm);
    sp.ppstr("wf_key", wfKey);
    sp.ppstr("wf_desc");
    sp.ppstr("wf_value");
    sp.ppstr("wf_value2");
    sp.ppstr("wf_value3");
    sp.ppstr("wf_value4");
    sp.ppstr("wf_value5");
    sp.ppnum("wf_value6");
    sp.ppnum("wf_value7");
    sp.ppnum("wf_value8");
    sp.ppnum("wf_value9");
    sp.ppnum("wf_value10");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    // sql2Exec();
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_sys_parm error");
    }
    // wp.ddd(sp.sql_stmt(),sp.sql_parm());

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Update("ptr_sys_parm", wp);
    sp.ppstr("wf_desc");
    sp.ppstr("wf_value");
    sp.ppstr("wf_value2");
    sp.ppstr("wf_value3");
    sp.ppstr("wf_value4");
    sp.ppstr("wf_value5");
    sp.ppnum("wf_value6");
    sp.ppnum("wf_value7");
    sp.ppnum("wf_value8");
    sp.ppnum("wf_value9");
    sp.ppnum("wf_value10");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    // sp.sql2Where("where rowid =?",commSqlStr.ss2rowid(wp.sss("rowid")));
    sp.sql2Where(" where wf_parm =?", wfParm);
    sp.sql2Where(" and wf_key =?", wfKey);
    // sql2Exec();
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("update ptr_sys_parm error");
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
    strSql = "delete ptr_sys_parm " + sqlWhere;
    log("del-sql=" + strSql);
    rc = sqlExec(strSql, new Object[] {wfParm,wfKey, wp.modSeqno()});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
