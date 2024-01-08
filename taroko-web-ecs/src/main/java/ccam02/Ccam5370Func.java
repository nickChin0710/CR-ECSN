/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5370Func extends FuncEdit {
  String dataSource = "", mccCode = "", acqId = "", mchtNo = "";

  public Ccam5370Func(TarokoCommon wr) {
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
      dataSource = wp.itemStr("kk_data_source");
      mccCode = wp.itemStr("kk_mcc_code");
      acqId = wp.itemStr("kk_acq_id");
      mchtNo = wp.itemStr("kk_mcht_no");
    } else {
      dataSource = wp.itemStr("data_source");
      mccCode = wp.itemStr("mcc_code");
      acqId = wp.itemStr("acq_id");
      mchtNo = wp.itemStr("mcht_no");
    }

    if (empty(dataSource)) {
      errmsg("資料來源：不可空白");
      return;
    }
    if (empty(mccCode)) {
      errmsg("Mcc Code：不可空白");
      return;
    }

    if (empty(mchtNo)) {
      errmsg("特店代號：不可空白");
      return;
    }

    if (eqIgno(dataSource, "A")) {
      if (!empty(acqId)) {
        errmsg("資料來源為 IN HOUSE , 收單行須為空白");
        return;
      }
    } else if (eqIgno(dataSource, "C")) {
      if (empty(acqId)) {
        errmsg("資料來源為 NCCC , 收單行不可空白");
        return;
      }
    }



    if (this.isAdd()) {
      if (checkMcht() == false) {
        errmsg("資料已存在,不可新增");
      }
      return;
    }
    sqlWhere = " where data_source = ? and mcc_code = ? and acq_id = ? and mcht_no = ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {dataSource, mccCode,acqId,mchtNo, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("CCA_auth_active", sqlWhere,parms)) {
      return;
    }
  }

  boolean checkMcht() {
    String sql1 = " select count(*) as db_cnt " + " from cca_auth_active " + " where mcht_no = ? ";

    sqlSelect(sql1, new Object[] {mchtNo});

    if (colNum("db_cnt") != 0) {
      errmsg("資料已存在,不可新增");
      return false;
    }

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into CCA_auth_active (" + " data_source, " // 1
        + " mcc_code, " + " acq_id, " + " mcht_no, "// 4
        + " crt_date, crt_user, " + " apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {dataSource // 1
        , mccCode, acqId, mchtNo, wp.loginUser, wp.loginUser, wp.loginUser, wp.modPgm()};
    sqlExec(strSql, param);
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

    strSql = "update CCA_auth_active set " + " apr_user =?, apr_date=sysdate,"
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + " where data_source = ? and mcc_code = ? and acq_id = ? and mcht_no = ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {wp.itemStr("mod_user"), wp.itemStr("mod_user"), wp.itemStr("mod_pgm"),dataSource, mccCode,acqId,mchtNo, wp.itemNum("mod_seqno")};
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
    strSql = "delete CCA_auth_active where data_source = ? and mcc_code = ? and acq_id = ? and mcht_no = ? and nvl(mod_seqno,0) = ?  ";
    Object[] parms = new Object[] {dataSource, mccCode,acqId,mchtNo, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,parms);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
