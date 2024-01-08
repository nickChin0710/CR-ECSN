/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-09-11  V1.00.00  yash       program initial                            *
*                                                                            *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0295Func extends FuncEdit {

  public Bilm0295Func(TarokoCommon wr) {
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

    if (this.isAdd()) {

    } else {
      if (empty(wp.itemStr("fees_txn_code"))) {
        errmsg("請先查詢!!");
        return;
      }

      // -other modify-
      sqlWhere = " where fees_txn_code = ?  ";
      Object[] param = new Object[] {wp.itemStr("fees_txn_code")};
      isOtherModify("ptr_prepaidfee_m", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
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

    strSql = "update ptr_prepaidfee_m set " + "  nor_fix_amt=?" + ", nor_percent=?"
        + ", spe_fix_amt=?" + ", spe_percent=?" + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
        + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemNum("new_nor_fix_amt"), wp.itemNum("new_nor_percent"),
        wp.itemNum("new_spe_fix_amt"), wp.itemNum("new_spe_percent"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemStr("fees_txn_code")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    return rc;
  }



}
