/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-26  V1.00.00  yash       program initial                            *
* 109-04-20  v1.00.01  Andy       Update add throws Exception                *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
* 111-11-28  V1.00.03  Zuwei      Sync from mega                             *
******************************************************************************/
package genp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Genp0110Func extends FuncEdit {
  String mKkTxDate = "";
  String mKkRefno = "";
  String mKkDepno = "";

  public Genp0110Func(TarokoCommon wr) {
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
    mKkTxDate = wp.itemStr("ex_tx_date");
    mKkRefno = wp.itemStr("ex_refno");
    mKkDepno = wp.itemStr("ex_depno");
    if (isEmpty(mKkTxDate) || isEmpty(mKkRefno)) {
      errmsg("請重新查詢!");
      return;
    }

    if (this.ibAdd) {
    }

    // -other modify-
    sqlWhere = " where tx_date = ? and refno = ? " 
            + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKkTxDate, mKkRefno, wp.modSeqno()};
    if (this.isOtherModify("gen_vouch", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();

    strSql = "insert into gen_vouch ("
            + " brno, "
            + " tx_date, "
            + " dept, "
            + " depno, "
            + " curr, "
            + " refno, "
            + " seqno, "
            + " ac_no, "
            + " dbcr, "
            + " amt, "
            + " crt_user, "
            + " apr_user, "
            + " id_no, "
            + " memo1, "
            + " memo2, "
            + " mod_time, mod_user, mod_pgm, mod_seqno"
            + " ) values ("
            + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
            + ", sysdate, ?, ?, 1"
            + " )";

    Object[] param = new Object[] {
            wp.itemStr("ex_branch"), 
            mKkTxDate, 
            "UB",
            // vars_ss("aa_depno"),
            mKkDepno, 
            wp.itemStr("ex_curr"), 
            mKkRefno, 
            varsStr("aa_seqno"), 
            varsStr("aa_ac_no"),
            varsStr("aa_dbcr"), 
            varsStr("aa_amt"), 
            wp.loginUser, 
            varsStr("aa_apr_user"),
            varsStr("aa_id_no"), 
            varsStr("aa_memo1"), 
            varsStr("aa_memo2"), 
            wp.loginUser,
            wp.modPgm()};

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
    mKkTxDate = wp.itemStr("ex_tx_date");
    mKkRefno = wp.itemStr("ex_refno");
    // System.out.println("mKkTxDate:"+mKkTxDate);
    // System.out.println("mKkRefno:"+mKkRefno);
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {mKkTxDate, mKkRefno};
    if (sqlRowcount("gen_vouch", "where tx_date = ? and refno = ? ", param) <= 0)
      return 1;

    strSql = "delete gen_vouch where tx_date = ? and refno = ? ";
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;
  }

}
