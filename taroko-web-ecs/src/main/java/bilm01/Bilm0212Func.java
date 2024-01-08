/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-02  V1.00.00  yash       program initial                            *
* 107-05-02  V1.00.01  Andy       Update                                     *
* 109-04-23  V1.00.02  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0212Func extends FuncEdit {
  String mKkRowid = "";
  String mKkModSeqno = "";

  public Bilm0212Func(TarokoCommon wr) {
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
    mKkRowid = wp.itemStr("rowid");
    mKkModSeqno = wp.itemStr("mod_seqno");
    if (this.isAdd()) {
    } else {
      // -other modify-
      sqlWhere = " where hex(rowid) = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkRowid, mKkModSeqno};
      isOtherModify("bil_othexp", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    String wkKeyNo = wp.itemStr("kk_key");
    if (empty(wkKeyNo)) {
      wkKeyNo = wp.itemStr("ex_key");
    }
    String wkAddItem = wp.itemStr("txn_code");
    if (empty(wkAddItem)) {
      wkAddItem = wp.itemStr("add_item");
    }

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_othexp");
    sp.ppstr("bill_type", wp.colStr("bill_type"));
    // sp.ppss("tx_code",wp.item_ss("txn_code"));
    sp.ppstr("tx_code", wkAddItem);
    sp.ppstr("post_flag", "N");
    // sp.ppss("key_no",wp.item_ss("kk_key"));
    sp.ppstr("key_no", wkKeyNo);
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("corp_no", wp.itemStr("corp_no"));
    // sp.ppss("add_item",wp.item_ss("txn_code"));
    sp.ppstr("error_code", wp.itemStr("error_code"));
    sp.ppstr("add_item", wkAddItem);
    sp.ppstr("curr_code", wp.itemStr("curr_code"));
    sp.ppnum("dc_dest_amt", wp.itemNum("dc_destination_amt"));
    sp.ppnum("dest_amt", wp.itemNum("dest_amt"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("bill_desc", wp.itemStr("bill_desc"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("bil_othexp");
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("corp_no", wp.itemStr("corp_no"));
    sp.ppstr("add_item", wp.itemStr("txn_code"));
    sp.ppstr("curr_code", wp.itemStr("curr_code"));
    sp.ppnum("dc_dest_amt", wp.itemNum("dc_destination_amt"));
    sp.ppnum("dest_amt", wp.itemNum("dest_amt"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("bill_desc", wp.itemStr("bill_desc"));
    sp.ppstr("apr_user", "");
    sp.ppstr("error_code", "");
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where hex(rowid)=?", mKkRowid);
    sp.sql2Where("and mod_seqno=?", mKkModSeqno);
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    // dataCheck();
    strSql =
        "delete bil_othexp " + "where hex(rowid) =:ls_rowid" + "and nvl(mod_seqno,0) =:mod_seqno ";
    setString("ls_rowid", wp.itemStr("rowid"));
    setString("mod_seqno", wp.itemStr("mod_seqno"));
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }
    return rc;
  }

}
