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
import taroko.com.TarokoCommon;

public class Colm0040Func2 extends FuncEdit {
  String modSeqno, kkStatus;

  public Colm0040Func2(TarokoCommon wr) {
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
    // kk1 = wp.item_ss("kk_Accttype");
    // kk2 = wp.item_ss("kk_Acctkey");
    // chkInsert();
    // }
    // else {
    // kk1 =wp.item_ss("p_seqno");
    // }

    modSeqno = wp.itemStr("p_seqno");

    // String sts =wp.item_ss("acct_status");
    // if(sts.equals("4")){
    // errmsg(wp.item_ss("tt_acct_status")+" 呆帳戶不可轉暫不逾放");
    // return ;
    // }
    //
    // String ds1 =wp.item_ss("no_delinquent_s_date");
    // String de1 =wp.item_ss("no_delinquent_e_date");
    // if(isEmpty(ds1) && !isEmpty(de1)){
    // errmsg("[暫不逾放-有效期間] 起日不可空白");
    // return ;
    // }
    // if (chk_strend(ds1, de1)<0) {
    // errmsg("[暫不逾放-有效期間] 輸入錯誤");
    // return;
    // }
    // if(!isEmpty(ds1)){
    // nt = "Y";
    // }else{
    // nt = isEmpty(wp.item_ss("hno_delinquent_flag"))? "":"N";
    // }
    //
    // String ds2 =wp.item_ss("no_collection_s_date");
    // String de2 =wp.item_ss("no_collection_e_date");
    // if(isEmpty(ds2) && !isEmpty(de2)){
    // errmsg("[暫不催收-有效期間] 起日不可空白");
    // return ;
    // }
    // if (chk_strend(ds2, de2)<0) {
    // errmsg("[暫不催收-有效期間] 輸入錯誤");
    // return;
    // }
    // if(!isEmpty(ds2)){
    // ns = "Y";
    // }else{
    // ns = isEmpty(wp.item_ss("hno_collection_flag"))? "":"N";
    // }

    // if (this.isAdd()){
    // return;
    // }

    if (wp.itemStr("aud_code").equals("A"))
      return;
    // -other modify-
    sqlWhere = "where p_seqno = ? " + "and mod_seqno = ? ";
    Object[] param = new Object[] {modSeqno, wp.modSeqno()};
    if (isOtherModify("col_acno_t", sqlWhere, param)) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    // No use..
    return rc;
  }

  @Override
  public int dbUpdate() {
    dataCheck();
    if (rc != 1)
      return rc;

    if (wp.itemStr("aud_code").equals("A"))
      rc = insertFunc();
    else
      rc = updateFunc();

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete col_acno_t " + sqlWhere;
    Object[] param = new Object[] {modSeqno, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      rc = 0;
    }
    return rc;
  }

  int insertFunc() {
    actionInit("A");

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_acno_t");
    sp.ppstr("p_seqno", modSeqno);
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("chi_name", wp.itemStr("chi_name"));
    sp.ppstr("acct_status", wp.itemStr("acct_status"));
    sp.ppstr("no_delinquent_flag", varsStr("no_delinquent_flag"));
    sp.ppstr("no_delinquent_s_date", wp.itemStr("no_delinquent_s_date"));
    sp.ppstr("no_delinquent_e_date", wp.itemStr("no_delinquent_e_date"));
    sp.ppstr("no_collection_flag", varsStr("no_collection_flag"));
    sp.ppstr("no_collection_s_date", wp.itemStr("no_collection_s_date"));
    sp.ppstr("no_collection_e_date", wp.itemStr("no_collection_e_date"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "1"); // Insert 時mod_seqno給0的話, 再做修改存檔時會無法繼續
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  int updateFunc() {
    actionInit("U");

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_acno_t");
    sp.ppstr("no_delinquent_flag", varsStr("no_delinquent_flag"));
    sp.ppstr("no_delinquent_s_date", wp.itemStr("no_delinquent_s_date"));
    sp.ppstr("no_delinquent_e_date", wp.itemStr("no_delinquent_e_date"));
    sp.ppstr("no_collection_flag", varsStr("no_collection_flag"));
    sp.ppstr("no_collection_s_date", wp.itemStr("no_collection_s_date"));
    sp.ppstr("no_collection_e_date", wp.itemStr("no_collection_e_date"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where p_seqno=?", modSeqno);
    sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

}
