/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Aoyulan       updated for project coding standard     *  
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm1100Func extends FuncEdit {
  CommString commString = new CommString();
  String liacSeqno;
  String recvDate;

  public Colm1100Func(TarokoCommon wr) {
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
    // if (this.ib_add) {
    // kk1 =wp.item_ss("liac_seqno");
    // }

    // ddd(this.actionCode+", kk1="+kk1+", mod_seqno="+wp.mod_seqno());
    // if (isEmpty(kk1)) {
    // errmsg("債務協商序號 不可空白");
    // return ;
    // }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      // String lsSql = "select count(*) as tot_cnt from col_jcic_data_fmt where format_id = ?";
      // Object[] param = new Object[] { kk1 };
      // sqlSelect(lsSql, param);
      // if (col_num("tot_cnt") > 0) {
      // errmsg("資料已存在，無法新增");
      // }
      // return;
    } else {
      // -other modify-
      // sql_where = "where liab_seqno = ? "
      // + "and nvl(mod_seqno,0) = ?";
      // Object[] param = new Object[] { kk1, wp.mod_seqno() };
      // other_modify("col_liab_nego", sql_where, param);
    }

  }

  @Override
  public int dbInsert() {
    // No use..
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    // dataCheck();
    if (rc != 1)
      return rc;

    liacSeqno = wp.itemStr("liac_seqno");
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liac_nego");
    sp.ppstr("reg_bank_no", wp.itemStr("reg_bank_no"));
    sp.ppstr("liac_remark", wp.itemStr("liac_remark"));
    sp.ppstr("liac_txn_code", wp.itemStr("liac_txn_code"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where liac_seqno = ?", liacSeqno);
    sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    // No use..
    return rc;
  }

  public int insertColLiacRmrecol() {
    msgOK();

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liac_rmrecol");
    sp.ppstr("id_no", varsStr("id_no"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("rmrecol_date", varsStr("rmrecol_date"));
    sp.ppstr("liac_seqno", varsStr("liac_seqno"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.ppstr("proc_date", varsStr("proc_date"));
    sp.ppstr("proc_flag", varsStr("proc_flag"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", wp.modPgm());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

  public int updateColLiacContract() {
    msgOK();

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liac_contract");
    sp.ppstr("file_date", wp.sysDate);
    sp.ppstr("liac_install_cnt", varsStr("liac_install_cnt"));
    sp.ppstr("liac_int_rate", varsStr("liac_int_rate"));
    sp.ppstr("install_s_date", varsStr("install_s_date"));
    sp.ppstr("month_pay_amt", varsStr("month_pay_amt"));
    sp.ppstr("credit_cont_amt", varsStr("credit_cont_amt"));
    sp.ppstr("cash_card_cont_amt", varsStr("cash_card_cont_amt"));
    sp.ppstr("credit_card_cont_amt", varsStr("credit_card_cont_amt"));
    sp.ppstr("total_cont_amt", varsStr("total_cont_amt"));
    sp.ppstr("per_allocate_amt", varsStr("per_allocate_amt"));
    sp.ppstr("m_no_credit_rate", varsStr("m_no_credit_rate"));
    sp.ppstr("pay_acct_no", varsStr("pay_acct_no"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid) = ?", varsStr("rowid"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

  public int insertColLiacContractHst() {
    msgOK();

    strSql = "insert into col_liac_contract_hst "
        + "select * from col_liac_contract where hex(rowid) = ? ";
    Object[] param = new Object[] {varsStr("rowid")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int updateColLiacReceipt() {
    msgOK();

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liac_receipt");
    sp.ppstr("install_s_date", varsStr("install_s_date"));
    sp.ppstr("proc_flag", varsStr("proc_flag"));
    sp.ppstr("crt_date", wp.sysDate);
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("apr_flag", varsStr("apr_flag"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid) = ?", varsStr("rowid"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

  public int doEndColLiacNego() {
    msgOK();

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liac_nego");
    sp.ppstr("end_date", varsStr("end_date"));
    sp.ppstr("end_reason", varsStr("end_reason"));
    sp.ppstr("end_remark", varsStr("end_remark"));
    sp.ppstr("end_user", varsStr("end_user"));
    sp.ppstr("apr_flag", varsStr("apr_flag"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    // sp.ppss("mod_ws", System.getenv("COMPUTERNAME")); //No Column!!
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where hex(rowid) = ?", varsStr("rowid"));
    sp.sql2Where(" and mod_seqno = ?", varsStr("mod_seqno"));

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

  public int doInsertColLiacCourt() {
    msgOK();

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_liac_court");
    sp.ppstr("liac_seqno", varsStr("liac_seqno"));
    sp.ppstr("apply_date", varsStr("apply_date"));
    sp.ppstr("id_no", varsStr("id_no"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("recv_date", varsStr("recv_date"));
    sp.ppstr("liac_doc_no", varsStr("liac_doc_no"));
    sp.ppstr("court_name", varsStr("court_name"));
    sp.ppstr("case_no", varsStr("case_no"));
    sp.ppstr("is_allow", varsStr("is_allow"));
    sp.ppstr("user_remark", varsStr("user_remark"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("crt_date", wp.sysDate);
    sp.ppstr("apr_flag", varsStr("apr_flag"));
    sp.ppstr("apr_date", varsStr("apr_date"));
    sp.ppstr("apr_user", varsStr("apr_user"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "0");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

  public int doDeleteColLiacCourt(String asType) {
    msgOK();
    liacSeqno = wp.itemStr("liac_seqno");
    recvDate = wp.itemStr("recv_date");
    String ssflag = "";
    if (eqIgno(asType, "U"))
      ssflag = " and apr_flag<>'Y' ";

    // 如果沒有資料回傳成功
    Object[] param = new Object[] {liacSeqno, recvDate};
    if (sqlRowcount("col_liac_court", "where liac_seqno = ? and recv_date = ? " + ssflag,
        param) <= 0)
      return 1;

    strSql = "delete col_liac_court where liac_seqno = ? and recv_date = ? " + ssflag;
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;
  }


  public int updateActAcno() {
    msgOK();
    double ldRevolveIntRate = 0;

    // 計算變更利率 revolve_int_rate
    String lsSql = "select round(decode(a.acct_status,'3',c.revolving_interest2,'4',"
        + "c.revolving_interest2,c.revolving_interest1) -" + " ? *100.0/365,3) revolve_int_rate "
        + "from act_acno a,ptr_actgeneral_n c " + "where a.acct_type = c.acct_type "
        + "and   a.id_p_seqno = ? ";
    Object[] param = new Object[] {commString.strToNum(varsStr("liac_int_rate")), varsStr("id_p_seqno")};
    sqlSelect(lsSql, param);
    if (sqlNotfind)
      return 0;

    ldRevolveIntRate = colNum("revolve_int_rate");

    // 更新act_acno.revolve_int_rate
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("act_acno");
    sp.ppdouble("revolve_int_rate", ldRevolveIntRate);
    sp.sql2Where(" where id_p_seqno = ? ", varsStr("id_p_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      rc = -1;
    }

    return rc;
  }

}
