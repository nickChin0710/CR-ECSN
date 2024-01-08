/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-17  V1.00.00  yash       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                              *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0820Func extends FuncEdit {
  String mKkMchtNo = "";

  public Bilm0820Func(TarokoCommon wr) {
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
      mKkMchtNo = wp.itemStr("kk_mcht_no");
    } else {
      mKkMchtNo = wp.itemStr("mcht_no");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from bil_merchant_fd where mcht_no = ?";
      Object[] param = new Object[] {mKkMchtNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where mcht_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkMchtNo, wp.modSeqno()};
      isOtherModify("bil_merchant_fd", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    msgOK();

    strSql = "insert into bil_merchant_fd (" + "  data_type " + ", mcht_no " + ", uniform_no "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,? " + ", sysdate,?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {varsStr("aa_data_type") // 1
        , varsStr("aa_mcht_no"), varsStr("aa_uniform_no"), wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    msgOK();

    strSql = "update bil_merchant_fd set " + "  data_type =? " + " ,mcht_no =? "
        + " ,uniform_no =? " + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
        + " , mod_seqno =nvl(mod_seqno,0)+1 " + " where hex(rowid) = ?  and mod_seqno = ? ";
    Object[] param =
        new Object[] {varsStr("aa_data_type"), varsStr("aa_mcht_no"), varsStr("aa_uniform_no"),
            wp.loginUser, wp.itemStr("mod_pgm"), varsStr("aa_rowid"), varsStr("aa_mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }


  @Override
  public int dbDelete() {
    actionInit("D");
    msgOK();
    strSql = "delete bil_merchant_fd where hex(rowid) = ?  and mod_seqno = ?  ";
    Object[] param = new Object[] {varsStr("aa_rowid"), varsStr("aa_mod_seqno")};

    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;
  }

}
