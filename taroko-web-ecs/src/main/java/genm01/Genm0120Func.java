/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package genm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Genm0120Func extends FuncEdit {
  String mKkVouchCd = "";
  String mKkVouchDesc = "";

  public Genm0120Func(TarokoCommon wr) {
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
    mKkVouchCd = wp.itemStr("ex_std_vouch_cd");

    if (isEmpty(mKkVouchCd)) {
      errmsg("分錄代碼不可為空值 ,請重新輸入!");
      return;
    }

    if (this.ibAdd) {
      return;
    }

    // -other modify-
    sqlWhere = " where std_vouch_cd= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKkVouchCd, wp.modSeqno()};
    if (this.isOtherModify("gen_sys_vouch", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();

    strSql = "insert into gen_sys_vouch (" + " std_vouch_cd, " + " std_vouch_desc, " + " curr, "
        + " dbcr, " + " dbcr_seq, " + " ac_no," + " memo1, " + " memo2, " + " memo3, "
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,?"
        + ", sysdate, ?, ?, 1" + " )";;
    Object[] param = new Object[] {mKkVouchCd, // 1
        varsStr("h_std_vouch_desc"), "00", varsStr("aa_dbcr"), varsStr("aa_dbcr_seq"),
        varsStr("aa_ac_no"), varsStr("aa_memo1"), varsStr("aa_memo2"), varsStr("aa_memo3"),
        wp.loginUser, wp.modPgm()};

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    msgOK();
    mKkVouchCd = wp.itemStr("ex_std_vouch_cd");

    // 如果沒有資料回傳成功
    Object[] param = new Object[] {mKkVouchCd};
    if (sqlRowcount("gen_sys_vouch", "where std_vouch_cd = ? ", param) <= 0)
      return 1;

    strSql = "delete gen_sys_vouch where std_vouch_cd = ? ";
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;
  }


}
