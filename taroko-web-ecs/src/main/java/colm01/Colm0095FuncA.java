/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/01/31  V1.00.00    Zuwei     program initial                           *
*  109/03/31  V1.00.01    YH     	在添加處新增檢查（欲轉崔呆金額）                           *
*  109-05-06  V1.00.02   Aoyulan       updated for project coding standard    *
*  109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                         *    
*  112-07-26  V1.00.04    Ryan      修正insert col_bad_trans bug               *                              *    
*  112-11-28  V1.00.05   sunny      加強商務卡區分acct_type的查詢條件                        *
******************************************************************************/

package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm0095FuncA extends FuncEdit {
  String pSeqno;
  // colm0080 新增、修改、刪除 --> COL_WAIT_TRANS 催收待轉檔 mod by phopho 2018.12.25
  // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
  String kkTransType;

  public Colm0095FuncA(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    return 0;
  }

  @Override
  public void dataCheck() {
    pSeqno = wp.itemStr("p_seqno");
    kkTransType = wp.itemStr("trans_type");

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

  // 檢查欲轉崔金額是否大於0
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

    rc = insertColWaitTrants();
    if (rc != 1) {
      return rc;
    }

    // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
    if (eqIgno(kkTransType, "4")) {
      rc = insertColBadTrans();
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    String corpSeqNo = wp.itemStr("corp_p_seqno");
    String acctType = wp.itemStr("acct_type");
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    rc = deleteColWaitTrans(corpSeqNo,acctType);
    if (rc != 1) {
      return rc;
    }

    rc = insertColWaitTrants();
    if (rc != 1) {
      return rc;
    }

    // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
    if (eqIgno(kkTransType, "4")) {
      rc = deleteColBadTrans(corpSeqNo,acctType);

      if (rc != 1) {
        return rc;
      }
      rc = insertColBadTrans();
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    String corpSeqNo = wp.itemStr("corp_p_seqno");
    String acctType = wp.itemStr("acct_type");
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    rc = deleteColWaitTrans(corpSeqNo,acctType);
    if (rc != 1) {
      return rc;
    }

    // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
    if (eqIgno(kkTransType, "4")) {
      rc = deleteColBadTrans(corpSeqNo,acctType);
    }

    return rc;
  }

  private int insertColWaitTrants() {
    String[] seqnos = wp.itemBuff("a-p_seqno");
    String[] acctStatuses = wp.itemBuff("a-acct_status");
    String[] dbNames = wp.itemBuff("a-db_cname");
    String[] badDebtAmts = wp.itemBuff("a-no_end_bal");
    String[] acnoFlags = wp.itemBuff("a-acno_flag");
    // String subSeqnos = wp.item_ss("sub_seqnos");
    if (seqnos == null || seqnos.length == 0) {
      rc = 1;
      return rc;
    }

    // String[] subSeqnoArray = subSeqnos.split(",");
    busi.SqlPrepare sp = new SqlPrepare();
    for (int i = 0; i < seqnos.length; i++) {
      String seqno = seqnos[i];
      String srcAcctStat = acctStatuses[i];
      String dbName = dbNames[i];
      String badDebtAmt = badDebtAmts[i];
      String acnoFlag = acnoFlags[i];

      sp.sql2Insert("col_wait_trans");
      sp.ppstr("p_seqno", seqno);
      sp.ppstr("acct_type", wp.itemStr("acct_type"));
      sp.ppstr("src_acct_stat", srcAcctStat); // 原始帳戶狀態
      sp.ppstr("chi_name", dbName); // 賬戶中文名
      sp.ppstr("trans_type", wp.itemStr("trans_type")); // 欲轉換類別
      sp.ppstr("alw_bad_date", wp.itemStr("alw_bad_date")); // 呆帳核准日
      sp.ppstr("paper_conf_date", wp.itemStr("paper_conf_date")); // 法律文件確認日期
      sp.ppstr("valid_cancel_date", wp.itemStr("valid_cancel_date"));
      sp.ppstr("paper_name", wp.itemStr("paper_name")); // 憑証名稱
      sp.ppnum("bad_debt_amt", strToNum(badDebtAmt)); // 欲轉催呆金額
      sp.ppstr("sys_trans_flag", wp.itemStr("sys_trans_flag"));
      sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
      sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_time ", ", sysdate ");
      sp.ppstr("mod_seqno", "0");
      sp.ppstr("acno_flag", acnoFlag);
      sp.ppstr("corp_p_seqno", wp.itemStr("corp_p_seqno"));
      sp.ppstr("corp_act_type", wp.itemStr("corp_act_type"));
      rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum == 0) {
        rc = -1;
        return rc;
      }
    }

    return rc;
  }

  private int deleteColWaitTrans(String corpSeqno,String acctType) {
    strSql = "delete col_wait_trans where corp_p_seqno = ? and acct_type = ?";
    Object[] param = new Object[] {corpSeqno,acctType};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    rc = 1;
    return rc;
  }

  // modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要insert、update、delete COL_BAD_TRANS。
  private int insertColBadTrans() {
    String[] seqnos = wp.itemBuff("a-p_seqno");
    String[] acctStatuses = wp.itemBuff("a-acct_status");
    String[] dbNames = wp.itemBuff("a-db_cname");
    String[] badDebtAmts = wp.itemBuff("a-no_end_bal");
    String[] acnoFlags = wp.itemBuff("a-acno_flag");
    String[] idSeqnos = wp.itemBuff("a-id_p_seqno");

    // String subSeqnos = wp.item_ss("sub_seqnos");
    if (seqnos == null || seqnos.length == 0) {
      rc = -1;
      return rc;
    }

    busi.SqlPrepare sp = new SqlPrepare();
    for (int i = 0; i < seqnos.length; i++) {
      String seqno = seqnos[i];
      String srcAcctStat = acctStatuses[i];
      String dbName = dbNames[i];
      String badDebtAmt = badDebtAmts[i];
      String acnoFlag = acnoFlags[i];
      String idSeqno = idSeqnos[i];

      sp.sql2Insert("col_bad_trans");
      sp.ppstr("p_seqno", seqno);
      sp.ppstr("id_p_seqno", "idSeqno"); // wp.item_ss("id_p_seqno"));
      sp.ppstr("acct_type", wp.itemStr("acct_type"));
      sp.ppstr("src_acct_stat", srcAcctStat); // vars_ss("src_acct_stat"));
      sp.ppstr("chi_name", dbName); // wp.item_ss("db_cname"));
      sp.ppstr("trans_type", wp.itemStr("trans_type"));
      sp.ppstr("alw_bad_date", wp.itemStr("alw_bad_date")); // 呆帳核准日
      sp.ppstr("paper_conf_date", wp.itemStr("paper_conf_date")); // 法律文件確認日期
      sp.ppstr("valid_cancel_date", wp.itemStr("valid_cancel_date"));
      sp.ppstr("paper_name", wp.itemStr("paper_name")); // 憑証名稱
      sp.ppnum("bad_debt_amt", strToNum(badDebtAmt)); // wp.item_ss("bad_debt_amt"));
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
        return rc;
      }
    }

    return rc;
  }

  private int deleteColBadTrans(String corpSeqno,String acctType) {
    strSql = "delete col_bad_trans "
        + "where exists ( select p_seqno from act_acno where corp_p_seqno = ? and act_acno.p_seqno = col_bad_trans.p_seqno and acct_type= ?  ) ";
    Object[] param = new Object[] {corpSeqno,acctType};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    rc = 1;
    return rc;
  }

}
