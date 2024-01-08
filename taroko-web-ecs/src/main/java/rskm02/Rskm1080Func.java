/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskm1080Func extends FuncAction {
  String condCode = "", aprFlag = "";
  int iiCondDataSeq = 0;

  @Override
  public void dataCheck() {
    condCode = wp.itemStr("cond_code");
    aprFlag = wp.itemStr("apr_flag");

    if (empty(condCode)) {
      errmsg("評分條件代碼不可空白");
      return;
    }

    if (this.ibAdd)
      return;
    sqlWhere = " where 1=1 " + " and cond_code =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {condCode, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("RSK_SCORE_PARM", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
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
    insertScoreParm();
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = " update rsk_score_parm set " + " cond_seq_desc =:cond_seq_desc , "
        + " data_desc =:data_desc , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
        + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where cond_code =:cond_code and apr_flag ='Y' " + " and mod_seqno =:mod_seqno ";

    item2ParmStr("cond_seq_desc");
    item2ParmStr("data_desc");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("cond_code", condCode);
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  void insertScoreParm() {
    strSql = "insert into RSK_SCORE_PARM (" + " cond_code, " // 1
        + " cond_desc, " + " cond_seq_desc," + " data_from," + " data_type," + " data_desc,"
        + " exec_flag," + " apr_flag," + " apr_date," + " apr_user," + " crt_date," + " crt_user, "
        + " mod_time," + " mod_user," + " mod_pgm," + " mod_seqno" + " ) values (" + " :kk,"
        + " :cond_desc," + " :cond_seq_desc," + " :data_from," + " :data_type," + " :data_desc,"
        + " 'N'," + " 'Y'," + " to_char(sysdate,'yyyymmdd')," + " :apr_user,"
        + " to_char(sysdate,'yyyymmdd')," + ":crt_user, " + " sysdate," + " :mod_user,"
        + " :mod_pgm," + " 1" + " )";
    // -set ?value-
    try {
      setString("kk", condCode);
      item2ParmStr("asig_reason");
      item2ParmStr("cond_desc");
      item2ParmStr("cond_seq_desc");
      item2ParmStr("data_from");
      item2ParmStr("data_type", "ls_data_type");
      item2ParmStr("data_desc");
      setString("apr_user", wp.loginUser);
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
  }

  // --刪除明細
  public int deleteScoreDetl() {
    msgOK();
    strSql = " delete rsk_score_parmdtl where cond_code =:cond_code and apr_flag =:apr_flag "
        + " and trial_type =:trial_type and cond_data_seq =:cond_data_seq ";

    var2ParmStr("cond_code");
    var2ParmStr("apr_flag");
    var2ParmStr("trial_type");
    var2ParmInt("cond_data_seq");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(sqlErrtext);
    } else
      rc = 1;

    return rc;
  }

  // --新增明細
  public int insertScoreDetl() {
    msgOK();
    strSql = " delete rsk_score_parmdtl where cond_code =:cond_code and trial_type =:trial_type "
        + " and cond_data_seq =:cond_data_seq and apr_flag <> 'Y' ";

    var2ParmStr("cond_code");
    var2ParmStr("trial_type");
    var2ParmInt("cond_data_seq");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(sqlErrtext);
      return rc;
    } else
      rc = 1;

    strSql = " insert into rsk_score_parmdtl ( " + " cond_code ," + " cond_data_seq ,"
        + " trial_type ," + " cond_data_desc ," + " data_type ," + " data_char1 ," + " data_char2 ,"
        + " data_num1 ," + " data_num2 ," + " data_score ," + " dsp_color ," + " apr_flag ,"
        + " mod_user ," + " mod_time " + " ) values ( " + " :cond_code ," + " :cond_data_seq ,"
        + " :trial_type ," + " :cond_data_desc ," + " :data_type ," + " :data_char1 ,"
        + " :data_char2 ," + " :data_num1 ," + " :data_num2 ," + " :data_score ," + " :dsp_color ,"
        + " 'N' ," + " :mod_user ," + " sysdate " + " ) ";

    var2ParmStr("cond_code");
    var2ParmInt("cond_data_seq");
    var2ParmStr("trial_type");
    var2ParmStr("cond_data_desc");
    var2ParmStr("data_type");
    var2ParmStr("data_char1");
    var2ParmStr("data_char2");
    var2ParmStr("data_num1");
    var2ParmStr("data_num2");
    var2ParmStr("data_score");
    var2ParmStr("dsp_color");
    setString("mod_user", wp.loginUser);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    // --刪除明細
    strSql = " delete rsk_score_parmdtl where cond_code =:cond_code ";
    setString("cond_code", condCode);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(sqlErrtext);
      return rc;
    } else
      rc = 1;

    // --刪除主檔
    strSql = " delete rsk_score_parm where cond_code =:cond_code and mod_seqno =:mod_seqno ";
    setString("cond_code", condCode);
    item2ParmNum("mod_seqno");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    return rc;
  }

  @Override
  public int dataProc() {
    msgOK();

    // --讀取未覆核明細資料
    String sql1 = " select * from rsk_score_parmdtl where cond_code = ? and apr_flag <> 'Y' ";
    sqlSelect(sql1, new Object[] {varsStr("cond_code")});
    if (sqlRowNum <= 0) {
      errmsg("查無未覆核明細資料");
      return rc;
    }

    int ilSelectCnt = 0;
    ilSelectCnt = sqlRowNum;

    for (int ii = 0; ii < ilSelectCnt; ii++) {
      // --刪除已覆核資料
      strSql =
          " delete rsk_score_parmdtl where cond_code=:cond_code and cond_data_seq =:cond_data_seq "
              + " and trial_type =:trial_type and apr_flag = 'Y' ";

      setString("cond_code", colStr(ii, "cond_code"));
      setInt("cond_data_seq", colInt(ii, "cond_data_seq"));
      setString("trial_type", colStr(ii, "trial_type"));

      sqlExec(strSql);
      if (sqlRowNum < 0) {
        errmsg(sqlErrtext);
        return rc;
      } else
        rc = 1;

      // --將未覆核資料 Update 成 Y

      strSql = " update rsk_score_parmdtl set " + " apr_flag = 'Y' "
          + " where cond_code=:cond_code and cond_data_seq =:cond_data_seq "
          + " and trial_type =:trial_type and apr_flag <> 'Y' ";

      setString("cond_code", colStr(ii, "cond_code"));
      setInt("cond_data_seq", colInt(ii, "cond_data_seq"));
      setString("trial_type", colStr(ii, "trial_type"));

      sqlExec(strSql);

      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
        return rc;
      }

    }

    return rc;
  }

  public int copyProc() {
    msgOK();

    // --讀取欲複製資料
    String sql1 =
        " select * from rsk_score_parmdtl where cond_code = ? and trial_type = ? and apr_flag = 'Y' ";
    sqlSelect(sql1, new Object[] {varsStr("cond_code"), varsStr("trial_type")});

    if (sqlRowNum <= 0) {
      errmsg("讀取複製資料失敗");
      return rc;
    }

    int ilSelectCnt = 0;
    ilSelectCnt = sqlRowNum;

    strSql = " insert into rsk_score_parmdtl ( " + " cond_code ," + " cond_data_seq ,"
        + " trial_type ," + " cond_data_desc ," + " data_type ," + " data_char1 ," + " data_char2 ,"
        + " data_num1 ," + " data_num2 ," + " data_score ," + " dsp_color ," + " apr_flag ,"
        + " mod_user ," + " mod_time " + " ) values ( " + " :cond_code ," + " :cond_data_seq ,"
        + " :trial_type ," + " :cond_data_desc ," + " :data_type ," + " :data_char1 ,"
        + " :data_char2 ," + " :data_num1 ," + " :data_num2 ," + " :data_score ," + " :dsp_color ,"
        + " 'N' ," + " :mod_user ," + " sysdate " + " ) ";
    int liSeq = 0;
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      if (ii == 0) {
        liSeq = getMaxSeqno(colStr(ii, "cond_code"), colStr(ii, "trial_type"));
      } else {
        if (eqIgno(colStr(ii, "cond_code"), colStr(ii - 1, "cond_code")))
          liSeq++;
        else
          liSeq = getMaxSeqno(colStr(ii, "cond_code"), colStr(ii, "trial_type"));
      }

      setString("cond_code", colStr(ii, "cond_code"));
      setInt("cond_data_seq", liSeq);
      setString("trial_type", varsStr("trial_type_new"));
      setString("cond_data_desc", colStr(ii, "cond_data_desc"));
      setString("data_type", colStr(ii, "data_type"));
      setString("data_char1", colStr(ii, "data_char1"));
      setString("data_char2", colStr(ii, "data_char2"));
      setNumber("data_num1", colNum(ii, "data_num1"));
      setNumber("data_num2", colNum(ii, "data_num2"));
      setNumber("data_score", colNum(ii, "data_score"));
      setString("dsp_color", colStr(ii, "dsp_color"));
      setString("mod_user", wp.loginUser);

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
        return rc;
      }

    }

    return rc;
  }

  int getMaxSeqno(String condCode, String trialType) {
    String sql1 = " select max(cond_data_seq) as max_seqno from rsk_score_parmdtl "
        + " where cond_code = ? and apr_flag = 'Y' ";

    sqlSelect(sql1, new Object[] {condCode});

    return colInt("max_seqno") + 1;
  }

}
