/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
******************************************************************************/
package colm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Colm0070Func extends FuncEdit {

  public Colm0070Func(TarokoCommon wr) {
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
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from col_param where 1=1";
      sqlSelect(lsSql, null);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = "where 1=1 " + "and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {wp.modSeqno()};
      isOtherModify("col_param", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into col_param (" + " m_code_ttl_e1 " + ", m_code_ttl_s1 " + ", exc_ttl_lmt_1 "
        + ", m_code_owe_s1 " + ", m_code_owe_e1 " + ", exc_owe_lmt_1 " + ", m_code_ttl_1 "
        + ", m_code_owe_1 " + ", m_code_ttl_2 " + ", exc_ttl_lmt_2 " + ", m_code_owe_2 "
        + ", exc_owe_lmt_2 " + ", m_code_ttl_3 " + ", m_code_owe_3 " + ", req_debt_lmt "
        + ", gen_cs_day " + ", trans_col_flag " + ", cycle_n_days " + ", trans_col_day "
        + ", trans_col_day2 " + ", trans_col_day3 " + ", trans_bad_debt_day " + ", terminate_amt1 "
        + ", terminate_year1 " + ", terminate_month1 " + ", terminate_year2 "
        + ", terminate_month2 " // 27
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,?,?, "
        + " ?,?,?,?,?,?,?,?,?,?, " + " ?,?,?,?,?,?,? " + ", sysdate,?,?,0" + " )";
    // -set ?value-
    Object[] param = new Object[] {strToNum(wp.itemStr("m_code_ttl_e1")),
        strToNum(wp.itemStr("m_code_ttl_s1")), strToNum(wp.itemStr("exc_ttl_lmt_1")),
        strToNum(wp.itemStr("m_code_owe_s1")), strToNum(wp.itemStr("m_code_owe_e1")),
        strToNum(wp.itemStr("exc_owe_lmt_1")), strToNum(wp.itemStr("m_code_ttl_1")),
        strToNum(wp.itemStr("m_code_owe_1")), strToNum(wp.itemStr("m_code_ttl_2")),
        strToNum(wp.itemStr("exc_ttl_lmt_2")), strToNum(wp.itemStr("m_code_owe_2")),
        strToNum(wp.itemStr("exc_owe_lmt_2")), strToNum(wp.itemStr("m_code_ttl_3")),
        strToNum(wp.itemStr("m_code_owe_3")), strToNum(wp.itemStr("req_debt_lmt")),
        strToNum(wp.itemStr("gen_cs_day")), wp.itemStr("trans_col_flag"),
        strToNum(wp.itemStr("cycle_n_days")), strToNum(wp.itemStr("trans_col_day")),
        strToNum(wp.itemStr("trans_col_day2")), strToNum(wp.itemStr("trans_col_day3")),
        strToNum(wp.itemStr("trans_bad_debt_day")), strToNum(wp.itemStr("terminate_amt1")),
        strToNum(wp.itemStr("terminate_year1")), strToNum(wp.itemStr("terminate_month1")),
        strToNum(wp.itemStr("terminate_year2")), strToNum(wp.itemStr("terminate_month2"))// 27
        , wp.loginUser, wp.itemStr("mod_pgm")};
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
    strSql = "update col_param set " + "  m_code_ttl_e1 =? " + ", m_code_ttl_s1 =? "
        + ", exc_ttl_lmt_1 =? " + ", m_code_owe_s1 =? " + ", m_code_owe_e1 =? "
        + ", exc_owe_lmt_1 =? " + ", m_code_ttl_1 =? " + ", m_code_owe_1 =? " + ", m_code_ttl_2 =? "
        + ", exc_ttl_lmt_2 =? " + ", m_code_owe_2 =? " + ", exc_owe_lmt_2 =? "
        + ", m_code_ttl_3 =? " + ", m_code_owe_3 =? " + ", req_debt_lmt =? " + ", gen_cs_day =? "
        + ", trans_col_flag =? " + ", cycle_n_days =? " + ", trans_col_day =? "
        + ", trans_col_day2 =? " + ", trans_col_day3 =? " + ", trans_bad_debt_day =? "
        + ", terminate_amt1 =? " + ", terminate_year1 =? " + ", terminate_month1 =? "
        + ", terminate_year2 =? " + ", terminate_month2 =? " // 27
        + ", mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {strToNum(wp.itemStr("m_code_ttl_e1")),
        strToNum(wp.itemStr("m_code_ttl_s1")), strToNum(wp.itemStr("exc_ttl_lmt_1")),
        strToNum(wp.itemStr("m_code_owe_s1")), strToNum(wp.itemStr("m_code_owe_e1")),
        strToNum(wp.itemStr("exc_owe_lmt_1")), strToNum(wp.itemStr("m_code_ttl_1")),
        strToNum(wp.itemStr("m_code_owe_1")), strToNum(wp.itemStr("m_code_ttl_2")),
        strToNum(wp.itemStr("exc_ttl_lmt_2")), strToNum(wp.itemStr("m_code_owe_2")),
        strToNum(wp.itemStr("exc_owe_lmt_2")), strToNum(wp.itemStr("m_code_ttl_3")),
        strToNum(wp.itemStr("m_code_owe_3")), strToNum(wp.itemStr("req_debt_lmt")),
        strToNum(wp.itemStr("gen_cs_day")), wp.itemStr("trans_col_flag"),
        strToNum(wp.itemStr("cycle_n_days")), strToNum(wp.itemStr("trans_col_day")),
        strToNum(wp.itemStr("trans_col_day2")), strToNum(wp.itemStr("trans_col_day3")),
        strToNum(wp.itemStr("trans_bad_debt_day")), strToNum(wp.itemStr("terminate_amt1")),
        strToNum(wp.itemStr("terminate_year1")), strToNum(wp.itemStr("terminate_month1")),
        strToNum(wp.itemStr("terminate_year2")), strToNum(wp.itemStr("terminate_month2"))// 27
        , wp.loginUser, wp.itemStr("mod_pgm"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    // No use..
    return rc;
  }

}
