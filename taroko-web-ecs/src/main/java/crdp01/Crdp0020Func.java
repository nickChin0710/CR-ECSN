/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-03  V1.00.00  ryan                program initial                            *
* 109-02-07  V1.00.01  JustinWu       modify primary key in where statement and column updated       *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/

package crdp01;

import busi.FuncProc;

import taroko.com.TarokoCommon;

public class Crdp0020Func extends FuncProc {
  String transNo;

  public Crdp0020Func(TarokoCommon wr) {
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
    transNo = varsStr("aa_trans_no");

    // -other modify-
    sqlWhere = " where trans_no=?  and nvl(mod_seqno,0) = ?";

    Object[] param = new Object[] {transNo, varsStr("aa_mod_seqno")};
    if (isOtherModify("crd_cardno_range", sqlWhere, param)) {

      return;
    }

  }

  @Override
  public int dataProc() {

    rc = updateFunc();

    return rc;
  }

  int updateFunc() {
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update crd_cardno_range set " + " charge_date = to_char(sysdate,'yyyymmdd'), "
        + " charge_id = ?, " + " post_flag = ?, " + " mod_user=?, " + " mod_time=sysdate, "
        + " mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1 " + " where trans_no=? ";

    Object[] param = new Object[] {wp.loginUser, "Y", wp.loginUser, wp.itemStr("mod_pgm"), transNo};

    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

}
