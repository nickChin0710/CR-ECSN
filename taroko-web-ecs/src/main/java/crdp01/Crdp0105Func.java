/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-22  V1.00.00  ryan       program initial                            *
* 109-04-30  V1.00.01  YangFang   updated for project coding standard        *
* * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/

package crdp01;

import busi.FuncProc;

import taroko.com.TarokoCommon;

public class Crdp0105Func extends FuncProc {
  String batchno;
  String recno;
  String kk3;

  public Crdp0105Func(TarokoCommon wr) {
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
    batchno = varsStr("aa_batchno");
    recno = varsStr("aa_recno");
    sqlWhere = " where batchno = ? and recno=?  and mod_seqno = ?";

    Object[] param = new Object[] {batchno, recno, varsStr("aa_mod_seqno")};
    if (isOtherModify("crd_emboss_tmp", sqlWhere, param)) {

      return;
    }

  }

  @Override
  public int dataProc() {
    return rc;
  }

  public int updateFunc() {
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update crd_emboss_tmp set " + " confirm_date = to_char(sysdate,'yyyymmdd'), "
        + " confirm_user = ?, " + " mod_user=?,mod_time=sysdate, mod_pgm =?, "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;

    Object[] param = new Object[] {wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), batchno, recno,
        varsStr("aa_mod_seqno")};

    rc = sqlExec(strSql, param);
    return rc;
  }

  public int updateFunc2() {

    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update crd_emboss_tmp set " + " confirm_date = '', " + " confirm_user = '', "
        + " mod_user=?,mod_time=sysdate, mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;

    Object[] param =
        new Object[] {wp.loginUser, wp.itemStr("mod_pgm"), batchno, recno, varsStr("aa_mod_seqno")};
    rc = sqlExec(strSql, param);
    return rc;

  }

}
