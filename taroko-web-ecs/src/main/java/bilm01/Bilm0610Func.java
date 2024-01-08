/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-19  V1.00.00             program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                               *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0610Func extends FuncEdit {
  String mKkIcaNo = "";

  public Bilm0610Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      mKkIcaNo = wp.itemStr("ica_no");
    } else {
      mKkIcaNo = wp.itemStr("ica_no");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from bil_auto_ica where ica_no  = ? ";
      Object[] param = new Object[] {mKkIcaNo};

      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("此ICA NO已被使用，無法新增");

      }
    } else {
      // -other modify-
      sqlWhere = " where ica_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkIcaNo, wp.modSeqno()};
      isOtherModify("bil_auto_ica", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    dataCheck();


    if (rc != 1) {
      return rc;
    }
    strSql = "insert into bil_auto_ica (" + " ica_no " + ", ica_desc " + ", bank_no "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,? " + ", sysdate,?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("ica_no"), wp.itemStr("ica_desc"),
        wp.itemStr("bank_no"), wp.loginUser, wp.itemStr("mod_pgm")};
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
    msgOK();
    if (rc != 1) {
      return rc;
    }

    strSql = "update bil_auto_ica set " + "   ica_no =? " + " , ica_desc =? " + " , bank_no =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + "  where hex(rowid)= ? and mod_seqno = ? ";
    Object[] param =
        new Object[] {varsStr("aa_ica_no"), varsStr("aa_ica_desc"), varsStr("aa_bank_no"),
            wp.loginUser, wp.itemStr("mod_pgm"), varsStr("aa_rowid"), varsStr("aa_mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {

    msgOK();

    if (rc != 1) {
      return rc;
    }

    strSql = "delete bil_auto_ica  where hex(rowid)= ? and mod_seqno = ? ";
    Object[] param = new Object[] {varsStr("aa_rowid"), varsStr("aa_mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
