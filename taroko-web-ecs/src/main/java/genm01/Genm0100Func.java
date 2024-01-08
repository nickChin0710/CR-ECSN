/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-30  V1.00.00             program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package genm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Genm0100Func extends FuncEdit {
  String mKkStdVouchCd = "";
  String mKkStdVouchDesc = "";
  String mKkCurr = "";
  String mKkDbcr = "";
  String mKkAcNo = "";

  public Genm0100Func(TarokoCommon wr) {
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
    mKkStdVouchCd = wp.itemStr("kk_std_vouch_cd");
    mKkStdVouchDesc = wp.itemStr("kk_std_vouch_desc");
    mKkCurr = wp.itemStr("kk_curr");
    mKkDbcr = varsStr("aa_dbcr");
    mKkAcNo = varsStr("aa_ac_no");
    // -other modify-
    // sql_where = " where std_vouch_cd = ? and std_vouch_desc =? and curr =
    // ? and nvl(mod_seqno,0) = ?";
    // Object[] param = new Object[] {
    // m_kk_std_vouch_cd,m_kk_std_vouch_desc,m_kk_curr, wp.mod_seqno() };
    // other_modify("gen_std_vouch", sql_where, param);
    // check duplicate
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt " + "from gen_std_vouch "
          + "where std_vouch_cd = ? " + "and std_vouch_desc =? " + "and curr = ? " + "and dbcr = ? "
          + "and ac_no = ? " + "and nvl(mod_seqno,0) = ? ";
      Object[] param =
          new Object[] {mKkStdVouchCd, mKkStdVouchDesc, mKkCurr, mKkDbcr, mKkAcNo, wp.modSeqno()};
      sqlSelect(lsSql, param);
      if (sqlRowNum > 0) {
        if (colNum("tot_cnt") > 0) {
          errmsg("資料已存在，無法新增");
        }
      }
      return;
    }
  }

  @Override
  public int dbInsert() {
    msgOK();
    dataCheck();
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("gen_std_vouch");
    sp.ppstr("std_vouch_cd", mKkStdVouchCd);
    sp.ppstr("std_vouch_desc", mKkStdVouchDesc);
    sp.ppstr("curr", mKkCurr);
    sp.ppstr("dbcr", mKkDbcr);
    sp.ppstr("dbcr_seq", varsStr("aa_dbcr_seq"));
    sp.ppstr("ac_no", mKkAcNo);
    sp.ppstr("memo1", varsStr("aa_memo1"));
    sp.ppstr("memo2", varsStr("aa_memo2"));
    sp.ppstr("memo3", varsStr("aa_memo3"));
    sp.ppstr("memo3_kind", varsStr("aa_memo3_kind"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  @Override
  public int dbUpdate() {
    return 1;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    msgOK();
    mKkStdVouchCd = wp.itemStr("kk_std_vouch_cd");
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {mKkStdVouchCd};
    if (sqlRowcount("gen_std_vouch", "where std_vouch_cd = ?  ", param) <= 0)
      return 1;

    strSql = "delete gen_std_vouch where std_vouch_cd = ?  ";
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;
  }

}
