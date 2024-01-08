/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-02  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-23   V1.00.02 Justin         comment duplicate code
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0235Func extends FuncEdit {
  String mKkCardProperty = "";
  String mKkCurrCode = "";

  public Ptrm0235Func(TarokoCommon wr) {
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
      mKkCardProperty = wp.itemStr("kk_card_property");
      mKkCurrCode = wp.itemStr("kk_curr_code");
    } else {
      mKkCardProperty = wp.itemStr("card_property");
      mKkCurrCode = wp.itemStr("curr_code");
    }

    if (this.isAdd()) {
      return;
    }

//    // -other modify-
//    sqlWhere = " where card_property= ? and curr_code= ? and nvl(mod_seqno,0) = ? ";
//
//    if (this.isOtherModify("ptr_foreign_fee", sqlWhere, new Object[] {mKkCardProperty, mKkCurrCode, wp.modSeqno()})) {
//      errmsg("請重新查詢 !");
//      return;
//    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from ptr_foreign_fee where card_property = ? and curr_code = ?";
      Object[] param = new Object[] {mKkCardProperty, mKkCurrCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where card_property = ? and curr_code = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkCardProperty, mKkCurrCode, wp.modSeqno()};
      isOtherModify("ptr_foreign_fee", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_foreign_fee (" + " card_property " + ", curr_code"
        + ", v_both_diff_rate" + ", v_country_diff_rate" + ", v_currency_diff_rate"
        + ", m_both_diff_rate" + ", m_country_diff_rate" + ", m_currency_diff_rate"
        + ", j_both_diff_rate" + ", j_country_diff_rate" + ", j_currency_diff_rate"
        + ", crt_date, crt_user " + ", apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ? " + ", ?, ?, ? "
        + ", ?, ?, ? " + ", ?, ?, ? " + ", to_char(sysdate,'yyyymmdd'), ?"
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkCardProperty // 1
        , mKkCurrCode,
        empty(wp.itemStr("v_both_diff_rate")) ? "0.00" : wp.itemStr("v_both_diff_rate"),
        empty(wp.itemStr("v_country_diff_rate")) ? "0.00" : wp.itemStr("v_country_diff_rate"),
        empty(wp.itemStr("v_currency_diff_rate")) ? "0.00" : wp.itemStr("v_currency_diff_rate"),
        empty(wp.itemStr("m_both_diff_rate")) ? "0.00" : wp.itemStr("m_both_diff_rate"),
        empty(wp.itemStr("m_country_diff_rate")) ? "0.00" : wp.itemStr("m_country_diff_rate"),
        empty(wp.itemStr("m_currency_diff_rate")) ? "0.00" : wp.itemStr("m_currency_diff_rate"),
        empty(wp.itemStr("j_both_diff_rate")) ? "0.00" : wp.itemStr("j_both_diff_rate"),
        empty(wp.itemStr("j_country_diff_rate")) ? "0.00" : wp.itemStr("j_country_diff_rate"),
        empty(wp.itemStr("j_currency_diff_rate")) ? "0.00" : wp.itemStr("j_currency_diff_rate"),
        wp.loginUser, wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_foreign_fee set " + "v_both_diff_rate = ? " + ", v_country_diff_rate = ? "
        + ", v_currency_diff_rate = ? " + ", m_both_diff_rate = ? " + ", m_country_diff_rate = ? "
        + ", m_currency_diff_rate = ? " + ", j_both_diff_rate = ? " + ", j_country_diff_rate = ? "
        + ", j_currency_diff_rate = ? " + ", apr_date=to_char(sysdate,'yyyymmdd'), apr_user=? "
        + ", mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {
        empty(wp.itemStr("v_both_diff_rate")) ? "0.00" : wp.itemStr("v_both_diff_rate"),
        empty(wp.itemStr("v_country_diff_rate")) ? "0.00" : wp.itemStr("v_country_diff_rate"),
        empty(wp.itemStr("v_currency_diff_rate")) ? "0.00" : wp.itemStr("v_currency_diff_rate"),
        empty(wp.itemStr("m_both_diff_rate")) ? "0.00" : wp.itemStr("m_both_diff_rate"),
        empty(wp.itemStr("m_country_diff_rate")) ? "0.00" : wp.itemStr("m_country_diff_rate"),
        empty(wp.itemStr("m_currency_diff_rate")) ? "0.00" : wp.itemStr("m_currency_diff_rate"),
        empty(wp.itemStr("j_both_diff_rate")) ? "0.00" : wp.itemStr("j_both_diff_rate"),
        empty(wp.itemStr("j_country_diff_rate")) ? "0.00" : wp.itemStr("j_country_diff_rate"),
        empty(wp.itemStr("j_currency_diff_rate")) ? "0.00" : wp.itemStr("j_currency_diff_rate"),
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"), mKkCardProperty,
        mKkCurrCode, wp.modSeqno()};
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
    strSql = "delete ptr_foreign_fee " + sqlWhere;
    Object[] param = new Object[] {mKkCardProperty, mKkCurrCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
