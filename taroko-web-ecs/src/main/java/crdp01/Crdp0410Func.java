/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-11  V1.00.00  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/

package crdp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdp0410Func extends FuncProc {
  String cardNo;
  String crtDate;
  String modSeqno;

  public Crdp0410Func(TarokoCommon wr) {
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
    cardNo = varsStr("aa_card_no");
    crtDate = varsStr("aa_crt_date");
    modSeqno = varsStr("aa_mod_seqno");

    // -other modify-
    sqlWhere = " where card_no=? and crt_date=? and mod_seqno = ? ";
    Object[] param = new Object[] {cardNo, crtDate, modSeqno};

    if (isOtherModify("crd_jcic_card", sqlWhere, param)) {
      return;
    }

    return;
  }

  @Override
  public int dataProc() {
    updateFunc();
    return rc;
  }

  public int updateFunc() {

    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_jcic_card");
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", apr_date =to_char(sysdate,'yyyymmdd')", ", mod_time = sysdate");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where card_no=?", varsStr("aa_card_no"));
    sp.sql2Where(" and crt_date=?", varsStr("aa_crt_date"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      // rc = 0;
    }
    return rc;
  }

}
