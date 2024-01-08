/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-03  V1.00.01  Ryan       program initial                            *
* 109-05-06  V1.00.02  Aoyulan       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Colm1230Func extends FuncEdit {
  String rowid = "", audCode = "";

  public Colm1230Func(TarokoCommon wr) {
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
    rowid = wp.itemStr("rowid");
    if (wp.itemStr("send_flag").equals("Y"))
      audCode = "C";
    else
      audCode = "A";
    // if (this.isAdd()){

    // }
    if (this.isUpdate()) {
      // -other modify-
      sqlWhere = " where hex(rowid)= ? and mod_seqno=?";
      Object[] param = new Object[] {rowid, wp.modSeqno()};
      if (this.isOtherModify("col_lgd_902", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    // dataCheck();
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into col_lgd_902 (" + "id_corp_p_seqno, " // phopho add
        + "id_corp_no, " + "lgd_seqno, " + "send_flag, " + "aud_code, " + "send_date, " + "send_ym,"
        + "from_type," + "lgd_type," + "early_ym," + "risk_amt," // 10
        + "overdue_ym," + "overdue_amt," + "coll_ym," + "coll_amt," + "crdt_charact,"
        + "assure_type," + "crdt_use_type," + "syn_loan_yn," + "syn_loan_date," + "fina_commit_yn,"// 20
        + "ecic_case_type," + "fina_commit_prct," + "card_rela_type," + "recv_trade_amt,"
        + "recv_collat_amt," + "recv_fina_amt," + "recv_self_amt," + "recv_rela_amt,"
        + "recv_oth_amt," + "costs_amt,"// 30
        + "costs_ym," + "revol_rate," + "close_reason," + "collat_yn," + "apr_user,"// 35
        + "apr_date," + "crt_date, " + "crt_user, " + "mod_pgm, " + "mod_user, " + "mod_time, "
        + "mod_seqno "// 42
        + " ) values ( "
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
        + ",to_char(sysdate,'yyyymmdd'),to_char(sysdate,'yyyymmdd'),?,?,?,sysdate,?" + " )";

    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("id_corp_p_seqno") // phopho add
        , wp.itemStr("id_corp_no"), wp.itemStr("lgd_seqno"), "", "C", "", "", "1",
        wp.itemStr("lgd_type"), wp.itemStr("early_ym"), wp.itemNum("risk_amt")// 10
        , wp.itemStr("overdue_ym"), wp.itemNum("overdue_amt"), wp.itemStr("coll_ym"),
        wp.itemNum("coll_amt"), wp.itemStr("crdt_charact"), wp.itemStr("assure_type"),
        wp.itemStr("crdt_use_type"), wp.itemStr("syn_loan_yn"), wp.itemStr("syn_loan_date"),
        wp.itemStr("fina_commit_yn")// 20
        , wp.itemStr("ecic_case_type"), wp.itemNum("fina_commit_prct"),
        wp.itemStr("card_rela_type"), wp.itemNum("recv_trade_amt"), wp.itemNum("recv_collat_amt"),
        wp.itemNum("recv_fina_amt"), wp.itemNum("recv_self_amt"), wp.itemNum("recv_rela_amt"),
        wp.itemNum("recv_oth_amt"), wp.itemNum("costs_amt")// 30
        , wp.itemStr("costs_ym"), wp.itemNum("revol_rate"), wp.itemStr("close_reason"),
        wp.itemStr("collat_yn"), wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm") // 37
        , wp.loginUser, wp.modSeqno()};

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

    strSql = "update col_lgd_902 set " + "send_ym=''," + "send_date=''," + "send_flag='N',"
        + "aud_code=?," + "early_ym=?," + "risk_amt=?," + "overdue_ym=?," + "overdue_amt=?,"
        + "coll_ym=?," + "coll_amt=?,"// 10
        + "crdt_use_type=?," + "card_rela_type=?," + "recv_self_amt=?," + "recv_rela_amt=?,"
        + "recv_oth_amt=?," + "costs_amt=?," + "costs_ym=?," + "revol_rate=?," + "close_reason=?,"
        + "apr_date=to_char(sysdate,'yyyymmdd'),"// 20
        + "apr_user=?," + "mod_time=sysdate, " + "mod_pgm =?, " + "mod_user=?, "
        + "mod_seqno=nvl(mod_seqno,0)+1 "// 25
        + sqlWhere;

    Object[] param = new Object[] {audCode, wp.itemStr("early_ym"), wp.itemNum("risk_amt"),
        wp.itemStr("overdue_ym"), wp.itemNum("overdue_amt"), wp.itemStr("coll_ym"),
        wp.itemNum("coll_amt"), wp.itemStr("crdt_use_type"), wp.itemStr("card_rela_type"),
        wp.itemNum("recv_self_amt") // 10
        , wp.itemNum("recv_rela_amt"), wp.itemNum("recv_oth_amt"), wp.itemNum("costs_amt"),
        wp.itemStr("costs_ym"), wp.itemNum("revol_rate"), wp.itemStr("close_reason"),
        wp.itemStr("approval_user"), wp.itemStr("mod_pgm"), wp.loginUser, rowid, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }


  @Override
  public int dbDelete() {
    actionInit("D");
    sqlWhere = " where hex(rowid)= ? and mod_seqno = ? and from_type ='1' and send_flag <> 'Y' ";
    strSql = "delete col_lgd_902" + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("rowid"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }
}
