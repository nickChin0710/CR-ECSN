/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-03  V1.00.00  yash       program initial                            *
* 108-12-13  V1.00.03  Andy       Update                                     *
* 109-04-23  V1.00.04  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0100Func extends FuncEdit {
  String mKkSeqNo = "";

  public Bilm0100Func(TarokoCommon wr) {
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
      String lsSql = " select seq_no  from ptr_assign_installment order by seq_no desc  ";
      sqlSelect(lsSql);

      mKkSeqNo = numToStr(colNum("seq_no") + 1, "");
    } else {
      mKkSeqNo = wp.itemStr("seq_no");
    }
    if (empty(colStr("seq_no"))) {
      rc = 1;
    }


    if (this.isAdd()) {
      // 檢查新增資料是否重複
      // String lsSql = "select count(*) as tot_cnt from ptr_assign_installment where seq_no = ?";
      // Object[] param = new Object[] { m_kk_seq_no };
      // sqlSelect(lsSql, param);
      // if (col_num("tot_cnt") > 0)
      // {
      // errmsg("資料已存在，無法新增");
      // }
      return;
    } else {
      // -other modify-
      sqlWhere = " where seq_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkSeqNo, wp.modSeqno()};
      isOtherModify("ptr_assign_installment", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into ptr_assign_installment (" + "  seq_no " + ", start_date "
        + ", reserve_type " + ", end_date " + ", amt_from " + ", amt_to " + ", mcht_no "
        + ", deny_mx " + ", twd_limit_flag " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkSeqNo // 1
        , wp.itemStr("start_date"), wp.itemStr("reserve_type"), wp.itemStr("end_date"),
        wp.itemStr("amt_from"), wp.itemStr("amt_to"), wp.itemStr("mcht_no"), wp.itemStr("deny_mx"),
        !wp.itemStr("twd_limit_flag").equals("Y") ? "N" : wp.itemStr("twd_limit_flag"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_assign_installment set " + " start_date =? " + " , end_date =? "
        + " , amt_from =? " + " , amt_to =? " + " , mcht_no =? " + " , deny_mx =? "
        + " , reserve_type =? " + " , twd_limit_flag =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("start_date"), wp.itemStr("end_date"),
        wp.itemStr("amt_from"), wp.itemStr("amt_to"), wp.itemStr("mcht_no"), wp.itemStr("deny_mx"),
        wp.itemStr("reserve_type"), wp.itemStr("twd_limit_flag"), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkSeqNo, wp.modSeqno()};
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
    strSql = "delete ptr_assign_installment " + sqlWhere;
    Object[] param = new Object[] {mKkSeqNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
