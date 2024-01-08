/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/01/31  V1.00.00    Zuwei     program initial                          *
*  109/03/31  V1.00.01    YH     	在添加處新增檢查（欲轉崔呆金額）          *
*  109-05-06  V1.00.02   Aoyulan       updated for project coding standard   *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
*  112-07-26  V1.00.04    Ryan      修正insert col_bad_trans bug                                                                                      *    
******************************************************************************/

package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm0090FuncA extends FuncEdit {
  String pSeqno;
  // colm0080 新增、修改、刪除 --> COL_WAIT_TRANS 催收待轉檔 mod by phopho 2018.12.25
  // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
  String kkTransType;

  public Colm0090FuncA(TarokoCommon wr) {
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
    pSeqno = wp.itemStr("p_seqno");
    kkTransType = wp.itemStr("trans_type");
    if(this.isUpdate()) {
    	if (!eqIgno(kkTransType, "4")) {
    		errmsg("欲轉換類別不為4.呆帳，無法修改");
    	    return;
    	}
    }
    // System.out.println(this.actionCode+", kk1="+kk1+", mod_seqno="+wp.mod_seqno());
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from col_wait_trans where p_seqno = ? ";
      Object[] param = new Object[] {pSeqno};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }
    } else {
      // -other modify-
      sqlWhere = "where p_seqno = ? " + "and mod_seqno = ? ";
      Object[] param = new Object[] {pSeqno, wp.modSeqno()};
      if (isOtherModify("col_wait_trans", sqlWhere, param)) {
        return;
      }
    }

    return;
  }

  // 檢查欲轉催金額是否大於0
  public int checkNoEndBal() {
    String noEndBalStr = wp.itemStr("no_end_bal");
    int noEndBal = Integer.parseInt(noEndBalStr);
    if ((eqIgno(kkTransType, "3") || eqIgno(kkTransType, "4")) && noEndBal <= 0) {
      errmsg("轉催呆金額須大於0，始可轉催呆");
      return -1;

    } else {
      return 1;
    }
  }


  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    rc = checkNoEndBal();
    if (rc != 1) {
      return rc;
    }

    rc = insertFunc();

    // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
    if (eqIgno(kkTransType, "4")) {
      if (rc != 1) {
        return rc;
      }
      rc = insertColBadTrans();
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    rc = deleteFunc();
    if (rc != 1)
      return rc;

    rc = insertFunc();

    // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
    if (eqIgno(kkTransType, "4")) {
      if (rc != 1)
        return rc;
      rc = deleteColBadTrans();

      if (rc != 1)
        return rc;
      rc = insertColBadTrans();
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    rc = deleteFunc();

    // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
    if (eqIgno(kkTransType, "4")) {
      if (rc != 1)
        return rc;
      rc = deleteColBadTrans();
    }

    return rc;
  }

  int insertFunc() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_wait_trans");
    sp.ppstr("p_seqno", pSeqno);
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("src_acct_stat", varsStr("src_acct_stat"));
    sp.ppstr("chi_name", wp.itemStr("db_cname"));
    sp.ppstr("trans_type", wp.itemStr("trans_type"));
    sp.ppstr("alw_bad_date", wp.itemStr("alw_bad_date"));
    sp.ppstr("paper_conf_date", wp.itemStr("paper_conf_date"));
    sp.ppstr("valid_cancel_date", wp.itemStr("valid_cancel_date"));
    sp.ppstr("paper_name", wp.itemStr("paper_name"));
    sp.ppnum("bad_debt_amt", wp.itemNum("bad_debt_amt"));
    sp.ppstr("sys_trans_flag", wp.itemStr("sys_trans_flag"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "0");
    sp.ppstr("acno_flag", wp.itemStr("acno_flag"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm() ,true);
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  int deleteFunc() {

    strSql = "delete col_wait_trans " + sqlWhere;
    Object[] param = new Object[] {pSeqno, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    return rc;
  }

  // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
  int insertColBadTrans() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_bad_trans");
    sp.ppstr("p_seqno", pSeqno);
    sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("src_acct_stat", varsStr("src_acct_stat"));
    sp.ppstr("chi_name", wp.itemStr("db_cname"));
    sp.ppstr("trans_type", wp.itemStr("trans_type"));
    sp.ppstr("alw_bad_date", wp.itemStr("alw_bad_date"));
    sp.ppstr("paper_conf_date", wp.itemStr("paper_conf_date"));
    sp.ppstr("valid_cancel_date", wp.itemStr("valid_cancel_date"));
    sp.ppstr("paper_name", wp.itemStr("paper_name"));
    sp.ppnum("bad_debt_amt", wp.itemNum("bad_debt_amt"));
    sp.ppstr("sys_trans_flag", wp.itemStr("sys_trans_flag"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "0");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  int deleteColBadTrans() {

    strSql = "delete col_bad_trans " + "where p_seqno = ? ";
    Object[] param = new Object[] {pSeqno};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    return rc;
  }

}
