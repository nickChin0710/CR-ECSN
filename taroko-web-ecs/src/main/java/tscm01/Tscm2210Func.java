/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                           *
* 110-08-02  V1.00.06  Bo Yang      添加Debit悠遊卡     
* 111-04-14  V1.00.07  machao     TSC畫面整合                       *
******************************************************************************/
package tscm01;

import busi.FuncAction;

public class Tscm2210Func extends FuncAction {
  String tscCardNo = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      tscCardNo = wp.itemStr("kk_tsc_card_no");
    } else {
      tscCardNo = wp.itemStr("tsc_card_no");
    }

    if (empty(tscCardNo)) {
      errmsg("悠遊卡號  : 不可空白!");
      return;
    }

    if (this.ibAdd) {
      if (checkTscCard() == false) {
        errmsg("悠遊卡號 : 不存在");
        return;
      }
    }

    if (selectTscCard() == false) {
      errmsg("悠遊卡號 : 不存在");
      return;
    }

    if (!ibDelete && !empty(colStr("return_date"))) {
      errmsg("已退卡 不可列黑名單");
      return;
    }

    if (this.ibDelete) {
      return;
    }

    if (wp.itemEq("black_flag", "1") || wp.itemEq("black_flag", "3")) {
      if (this.chkStrend(wp.itemStr("send_date_s"), wp.itemStr("send_date_e")) == -1) {
        errmsg("強制/不報送期間 輸入錯誤");
        return;
      }
    }

  }

  boolean selectTscCard() {
    String sql1 = "select " + " card_no," + " current_code," + " new_end_date," + " return_date,"
            + " lock_date," + " blacklt_s_date," + " blacklt_e_date " + " from tsc_card "
            + " where tsc_card_no = ? "
            + " union select " + " vd_card_no," + " current_code," + " new_end_date," + " return_date,"
            + " lock_date," + " blacklt_s_date," + " blacklt_e_date " + " from tsc_vd_card "
            + " where tsc_card_no = ? ";
    sqlSelect(sql1, new Object[] {tscCardNo, tscCardNo});
    if (sqlRowNum <= 0) {
      return false;
    }
    return true;
  }

  boolean checkTscCard() {
    String sql1 = "select card_no, current_code, new_end_date  " + " from tsc_card "
            + " where tsc_card_no = ? "
            + " union select vd_card_no, current_code, new_end_date  " + " from tsc_vd_card "
            + " where tsc_card_no = ? ";
    sqlSelect(sql1, new Object[] {tscCardNo, tscCardNo});
    if (sqlRowNum <= 0) {
      return false;
    }
    return true;
  }


  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into tsc_bkec_expt (" + " tsc_card_no , " + " card_no , " + " black_date , "
        + " black_user_id , " + " black_remark , " + " crt_user , " + " crt_date , "
        + " black_flag , " + " send_date_s , " + " send_date_e , " + " from_type , "
        + " apr_date , " + " apr_user , " + " mod_user , " + " mod_time , " + " mod_pgm , "
        + " mod_seqno " + " ) values (" + " :kk1 , " + " :card_no , "
        + " to_char(sysdate,'yyyymmdd') , " + " :black_user_id , " + " :black_remark , "
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " :black_flag , "
        + " :send_date_s , " + " :send_date_e , " + " '1' , " + " to_char(sysdate,'yyyymmdd') , "
        + " :apr_user , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " '1' " + " )";
    setString("kk1", tscCardNo);
    setString("card_no", colStr("card_no"));
    setString("black_user_id", wp.loginUser);
    item2ParmStr("black_remark");
    setString("crt_user", wp.loginUser);
    item2ParmNvl("black_flag", "1");
    item2ParmStr("send_date_s");
    item2ParmStr("send_date_e");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2210");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert tsc_bkec_expt error, " + sqlErrtext);
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
    strSql = "update tsc_bkec_expt set " + " black_date =to_char(sysdate,'yyyymmdd') ,"
        + " black_user_id =:black_user_id ," + " black_remark =:black_remark ,"
        + " black_flag =:black_flag ," + " send_date_s =:send_date_s ,"
        + " send_date_e =:send_date_e ," + " from_type ='1' ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where tsc_card_no =:kk1";

    setString("black_user_id", wp.loginUser);
    item2ParmStr("black_remark");
    item2ParmNvl("black_flag", "1");
    item2ParmStr("send_date_s");
    item2ParmStr("send_date_e");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2210");
    setString("kk1", tscCardNo);
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Update tsc_bkec_expt error, " + getMsg());
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
    strSql = "Delete tsc_bkec_expt" + " where tsc_card_no =:kk1";
    setString("kk1", tscCardNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete tsc_bkec_expt err=" + getMsg());
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
