/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;
/*2018-0119:	JH		modify
 * 
 * */
import busi.FuncAction;

public class Rskm1090Func extends FuncAction {

  @Override
  public void dataCheck() {
    if (wp.itemEmpty("ex_score_type") || wp.itemEmpty("ex_score_desc")) {
      errmsg("評分類別及說明 不可空白");
      return;
    }

    if (checkTrialType() == false) {
      errmsg("評分項目 未定義[覆審類別 項目]");
      return;
    }

  }


  boolean checkTrialType() {
    String sql1 = "select count(*) as db_cnt " + " from rsk_score_parmdtl " + " where cond_code=? "
        + " and trial_type = ? ";
    sqlSelect(sql1, new Object[] {varsStr("cond_code"), wp.itemStr("ex_trial_type")});
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    insertIdno();
    return rc;
  }

  public int deleteIdno() {
    strSql = "delete RSK_SCORE_TYPE " + " where score_type =:ex_score_type ";
    var2ParmStr("ex_score_type");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else {
      rc = 1;
    }
    return rc;
  }

  public int insertIdno() {
    strSql = "insert into RSK_SCORE_TYPE (" + " score_type, " + " cond_code, "
        + " score_type_desc, " + " score_flag, " + " trial_type, " + " crt_user, " + " crt_date, "
        + " apr_flag, " + " apr_date, " + " apr_user, " + " mod_user, " + " mod_time, "
        + " mod_pgm, " + " mod_seqno " + " ) values (" + " :ex_score_type ," + " :cond_code ,"
        + " :ex_score_desc ," + " 'Y'," + " :ex_trial_type ," + " :crt_user ,"
        + " to_char(sysdate,'yyyymmdd')," + " 'Y'," + " to_char(sysdate,'yyyymmdd'),"
        + " :approval_user," + " :mod_user," + " sysdate," + " :mod_pgm," + " 1" + " )";
    // -set ?value-
    try {
      var2ParmStr("ex_score_type");
      var2ParmStr("cond_code");
      var2ParmStr("ex_score_desc");
      var2ParmStr("ex_trial_type");
      setString("crt_user", wp.loginUser);
      var2ParmStr("approval_user");
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "rskm1090");
    } catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    log("rc 3: " + rc);
    return rc;
  }

}
