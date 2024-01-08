/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-12-03  V1.00.00  yash       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                               *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0810Func extends FuncEdit {
  String mKkBankNo = "";
  String mKkMchtNo = "";

  public Bilm0810Func(TarokoCommon wr) {
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

      if (empty(wp.itemStr("kk_mcht_no"))) {
        errmsg("特店代號不可空白!!");
      }

      mKkBankNo = wp.itemStr("kk_bank_no");
      mKkMchtNo = wp.itemStr("kk_mcht_no");
    } else {
      mKkBankNo = wp.itemStr("bank_no");
      mKkMchtNo = wp.itemStr("mcht_no");
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from bil_no_installment where bank_no = ? and mcht_no=? ";
      Object[] param = new Object[] {mKkBankNo, mKkMchtNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where bank_no = ? and mcht_no =? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkBankNo, mKkMchtNo, wp.modSeqno()};
      isOtherModify("bil_no_installment", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into bil_no_installment (" + "  bank_no " + ", mcht_no" + ", remark_40"
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ?, ? "
        + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkBankNo // 1
        , mKkMchtNo, wp.itemStr("remark_40"), wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int dbInsert1() {
    actionInit("A");
    // dataCheck();
    // if (rc != 1){
    // return rc;
    // }
    strSql = "insert into bil_no_installment (" + "  bank_no " + ", mcht_no" + ", remark_40"
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ?, ? "
        + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {varsStr("bank_no") // 1
        , varsStr("mcht_no"), varsStr("remark_40"), wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update bil_no_installment set " + " remark_40 = ?"
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("remark_40"), wp.loginUser, wp.itemStr("mod_pgm"),
        mKkBankNo, mKkMchtNo, wp.modSeqno()};
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
    strSql = "delete bil_no_installment " + sqlWhere;
    Object[] param = new Object[] {mKkBankNo, mKkMchtNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
