/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-22  V1.00.00  Andy       program initial                            *
* 106-11-16            Andy       update  table  gen_acct_m==>gen_acct_m_t   *
* 107-01-05  V1.00.02  ryan       Update  							         *
* 109-04-21  V1.00.03  YangFang   updated for project coding standard        *
******************************************************************************/

package genm01;

import busi.FuncEdit; 
import taroko.com.TarokoCommon;

public class Genm0020Func extends FuncEdit {
  String mKkAcNo = "";


  public Genm0020Func(TarokoCommon wr) {
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
    mKkAcNo = wp.itemStr("ac_no");
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from gen_acct_m_t where ac_no = ?";
      Object[] param = new Object[] {mKkAcNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    }

    // -other modify-
    sqlWhere = " where ac_no= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKkAcNo, wp.modSeqno()};
    if (this.isOtherModify("gen_acct_m_t", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    // is_sql = "insert into gen_acct_m ("
    // ***20171116 update gen_acct_m==>gen_acct_m_t
    strSql = "insert into gen_acct_m_t (" + " ac_no " + " , ac_full_name" + " , ac_brief_name"
        + " , memo3_flag" + " , memo3_kind" + " , dr_flag" + " , cr_flag" + " , brn_rpt_flag"
        + " , mod_user, mod_time , mod_pgm , mod_seqno " + " ) values (" + " ?,?,?,?,?,?,?,? "
        + ",?,sysdate,?,1" + " )";

    Object[] param = new Object[] {mKkAcNo // 1
        , wp.itemStr("ac_full_name"), wp.itemStr("ac_brief_name"),
        wp.itemStr("memo3_flag").equals("Y") ? "Y" : "N", wp.itemStr("memo3_kind"),
        wp.itemStr("dr_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("cr_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("brn_rpt_flag").equals("Y") ? "Y" : "N", wp.loginUser, wp.itemStr("mod_pgm")};
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

    // is_sql = "update gen_acct_m set "
    // ***20171116 update
    strSql = "update gen_acct_m_t set " + "  ac_full_name =?" + " , ac_brief_name =?"
        + " , memo3_flag =?" + " , memo3_kind =?" + " , dr_flag =?" + " , cr_flag =?"
        + " , brn_rpt_flag =?" + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
        + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("ac_full_name"), wp.itemStr("ac_brief_name"),
        wp.itemStr("memo3_flag"), wp.itemStr("memo3_kind"), wp.itemStr("dr_flag"),
        wp.itemStr("cr_flag"), wp.itemStr("brn_rpt_flag"), wp.loginUser, wp.itemStr("mod_pgm"),
        mKkAcNo, wp.modSeqno()};
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
    // is_sql = "delete gen_acct_m "
    // ***20171116 update
    strSql = "delete gen_acct_m_t " + sqlWhere;
    Object[] param = new Object[] {mKkAcNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
