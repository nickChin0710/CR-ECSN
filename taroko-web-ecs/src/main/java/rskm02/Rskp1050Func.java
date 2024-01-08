/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;
/**
 * 2019-1230   JH    credit_limit: busi.func >>ecsfunc
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 */

import busi.FuncAction;
import busi.func.AcnoBlockReason;
import busi.func.AcnoCreditLimit;

public class Rskp1050Func extends FuncAction {
  private String reasonAft = "";
  private String batchNo = "";
  private String idPseqno = "";
  private String actionCode = "";

  public int iiCombo = 0;

  private AcnoBlockReason ooBlock = new AcnoBlockReason();
  private AcnoCreditLimit ooLimit = new AcnoCreditLimit();

  @Override
  public void dataCheck() {
    daoTid = "trli.";
    String sql2 =
        " select * " + " from rsk_trial_list " + " where batch_no = ? " + " and id_p_seqno = ? ";
    sqlSelect(sql2, new Object[] {batchNo, idPseqno});
    actionCode = colStr("trli.action_code");
    if (sqlRowNum <= 0) {
      errmsg("覆審名單己不存在");
      return;
    }

    if (colEq("trli.close_flag", "Y") && !colEmpty("trli.apr_date")) {
      errmsg("覆審名單己結案覆核");
      return;
    }

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    batchNo = wp.itemStr2("A1_batch_no");
    idPseqno = wp.itemStr2("A1_id_p_seqno");


    updateList1();
    if (rc != 1)
      return rc;
    updateLog1();
    if (rc != 1)
      return rc;
    wfUpdateTrialIdno1();
    if (rc != 1)
      return rc;
    wfUpdateTrialLoan();
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    msgOK();
    batchNo = varsStr("batch_no");
    idPseqno = varsStr("id_p_seqno");

    dataCheck();
    if (rc != 1)
      return rc;

    updateList();
    if (rc != 1)
      return rc;

    updateLog();
    if (rc != 1)
      return rc;

    wfUpdateTrialIdno1();
    if (rc != 1)
      return rc;

    wfUpdateTrialLoan();
    if (rc != 1)
      return rc;

    // --
    ooBlock.setConn(wp);
    ooLimit.setConn(wp);

    selectRskTrailActionLog();
    iiCombo += ooLimit.ilCombo;
    return rc;
  }
  //
  // public int updateProc() {
  //
  // }

  private void selectRskTrailActionLog() {
    daoTid = "trlog.";
    String sql1 =
        "select * from rsk_trial_action_log " + " where batch_no = ? " + " and id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {batchNo, idPseqno});
    if (sqlRowNum <= 0)
      return;

    int logRow = sqlRowNum;
    for (int ii = 0; ii < logRow; ii++) {
      String lsKkPseqno = colStr(ii, "trlog.acno_p_seqno");
      // -凍結-
      String lsBlock4 = colStr(ii, "trlog.block_reason4");
      String lsBlock5 = colStr(ii, "trlog.block_reason5");
      String lsSpec = colStr(ii, "trlog.spec_status");
      int liRc = 1;
      if (notEmpty(lsBlock4) || notEmpty(lsBlock5) || notEmpty(lsSpec)) {
        liRc = ooBlock.trialActionBlock(lsKkPseqno, lsBlock4, lsBlock5, lsSpec);
        if (liRc != 1) {
          errmsg(ooBlock.getMsg());
          break;
        }
      }

      // -調額-
      if (commString.strIn2(actionCode, ",1,2,3,4,5") == false)
        continue;
      double lmLimitBef = colNum(ii, "trlog.credit_limit_bef");
      double lmLimitAft = colNum(ii, "trlog.credit_limit_aft");
      if (lmLimitBef == lmLimitAft)
        continue;

      ooLimit.acnoPseqno = lsKkPseqno;
      ooLimit.imAmtBefore = lmLimitBef;
      ooLimit.imAmtAfter = lmLimitAft;
      liRc = ooLimit.trialAction();
      if (liRc == -1) {
        errmsg(ooLimit.getMsg());
        break;
      }
    }
  }

  private int updateList() {

    strSql = " update rsk_trial_list set " + " close_flag = 'Y' ," + " apr_user = :apr_user , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + commSqlStr.setModxxx(modUser, modPgm)
        + " where batch_no =:batch_no " + " and id_p_seqno =:id_p_seqno ";
    setString("apr_user", modUser);
    setString2("batch_no", batchNo);
    setString2("id_p_seqno", idPseqno);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_list error !");
    }
    return rc;
  }

  public int updateList1() {
    msgOK();
    strSql = " update rsk_trial_list set " + " close_flag = 'Y' ," + " apr_user = :apr_user , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " mod_user = :mod_user , "
        + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 , " + " mod_pgm =:mod_pgm "
        + " where batch_no =:batch_no " + " and id_p_seqno =:id_p_seqno ";
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskp1050");
    item2ParmStr("batch_no", "A1_batch_no");
    item2ParmStr("id_p_seqno", "A1_id_p_seqno");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_list error !");
    }
    return rc;
  }

  private int updateLog() {
    reasonAft = "";
    String sql1 = " select " + "B.block_reason1||" + "B.block_reason2||" + "B.block_reason3||"
        + "B.block_reason4||" + "B.block_reason5 as ls_reason_aft "
        + " from cca_card_acct B , rsk_trial_action_log A "
        + " where  B.acno_p_seqno = A.acno_p_seqno " + " and b.debit_flag<>'Y' "
        + " and A.batch_no = ? " + " and A.id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {batchNo, idPseqno});
    if (sqlRowNum <= 0) {
      return rc;
    }

    reasonAft = colStr("ls_reason_aft");
    strSql = " update rsk_trial_action_log set " + " close_flag ='Y' , " + " block_reason_aft =? , "
        + commSqlStr.setModxxx(modUser, modPgm) + " where batch_no =? " + " and id_p_seqno =? ";
    setString(1, reasonAft);
    setString2(2, batchNo);
    setString2(3, idPseqno);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_action_log error");
    }
    return rc;
  }

  public int updateLog1() {
    msgOK();
    reasonAft = "";
    String sql1 = " select " + "B.block_reason1||" + "B.block_reason2||" + "B.block_reason3||"
        + "B.block_reason4||" + "B.block_reason5 as ls_reason_aft "
        + " from cca_card_acct B , rsk_trial_action_log A "
        + " where  B.acno_p_seqno = A.acno_p_seqno " + " and b.debit_flag<>'Y' "
        + " and A.batch_no = ? " + " and A.id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("A1_batch_no"), wp.itemStr("A1_id_p_seqno")});
    if (sqlRowNum <= 0) {
      return rc;
    }

    reasonAft = colStr("ls_reason_aft");
    strSql = " update rsk_trial_action_log set " + " close_flag ='Y' , "
        + " block_reason_aft =:block_reason_aft , " + " mod_user = :mod_user , "
        + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 , " + " mod_pgm =:mod_pgm "
        + " where batch_no =:batch_no " + " and id_p_seqno =:id_p_seqno ";
    setString("block_reason_aft", reasonAft);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskp1050");
    item2ParmStr("batch_no", "A1_batch_no");
    item2ParmStr("id_p_seqno", "A1_id_p_seqno");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_action_log error");
    }
    return rc;
  }

  public int wfUpdateTrialIdno() {
    msgOK();
    // --
    daoTid = "trli.";
    String sql2 =
        " select * " + " from rsk_trial_list " + " where batch_no = ? " + " and id_p_seqno = ? ";
    sqlSelect(sql2, new Object[] {varsStr("batch_no"), varsStr("id_p_seqno")});

    // --

    String sql1 = " select * " + " from rsk_trial_idno " + " where id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {varsStr("id_p_seqno")});
    if (sqlRowNum <= 0) {
      insertRskTrialIdno();
      return rc;
    }

    if (this.chkStrend(this.getSysDate(), colStr("trial_date")) == 1) {
      rc = 1;
      return rc;
    }
    updateRskTrialIdno();

    return rc;
  }

  private int wfUpdateTrialIdno1() {
    // --

    // --
    daoTid = "idno.";
    String sql1 = " select * from rsk_trial_idno " + " where id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {idPseqno});
    if (sqlRowNum <= 0) {
      insertRskTrialIdno();
      return rc;
    }

    if (this.chkStrend(this.getSysDate(), colStr("idno.trial_date")) == 1) {
      rc = 1;
      return rc;
    }
    updateRskTrialIdno();

    return rc;
  }

  private int insertRskTrialIdno() {
    sql2Insert("rsk_trial_idno");
    addsqlParm("?", "id_p_seqno", idPseqno);
    addsqlParm(",?", ", trial_type", "2");
    addsqlParm(", case_from", ", ''");
    addsqlYmd(", trial_date");
    addsqlParm(",?", ", batch_no", batchNo);
    addsqlParm(",?", ", risk_group", colStr("trli.risk_group"));
    addsqlParm(",?", ", action_code", colStr("trli.action_code"));
    addsqlParm(",?", ", trial_remark", colStr("trli.trial_remark"));
    addsqlParm(",?", ", trial_remark2", colStr("trli.trial_remark2"));
    addsqlParm(",?", ", trial_remark3", colStr("trli.trial_remark3"));
    addsqlParm(",?", ", loan_flag", "");
    addsqlParm(",?", ", crt_user", colStr("trli.close_user"));
    addsqlParm(",?", ", crt_date", colStr("trli.close_date"));
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      sqlErr("insert rsk_trial_idno error");
    }

    return rc;
  }

  private int updateRskTrialIdno() {
    sql2Update("rsk_trial_idno");
    addsql2("trial_type ='2'");
    addsql2(", case_from =''");
    addsql2(", trial_date =" + commSqlStr.sysYYmd);
    addsqlParm(", batch_no =?", batchNo);
    addsqlParm(", risk_group =?", colStr("trli.risk_group"));
    addsqlParm(", action_code =?", colStr("trli.action_code"));
    addsqlParm(", trial_remark =?", colStr("trli.trial_remark"));
    addsqlParm(", trial_remark2 =?", colStr("trli.trial_remark2"));
    addsqlParm(", trial_remark3 =?", colStr("trli.trial_remark3"));
    addsqlParm(", crt_user =?", colStr("trli.close_user"));
    addsqlParm(", crt_date =?", colStr("trli.close_date"));
    addsqlModXXX(modUser, modPgm);
    sqlWhere("where id_p_seqno =?", idPseqno);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      sqlErr("update rsk_trial_idno error");
    }
    return rc;
  }

  public int wfUpdateTrialLoan() {
    msgOK();
    if (empty(colStr("trli.loan_flag")))
      return rc;
    daoTid = "loan.";
    String sql1 = " select " + " * " + " from rsk_trial_loan " + " where id_no = ? ";
    sqlSelect(sql1, new Object[] {colStr("trli.id_no")});
    if (sqlRowNum <= 0)
      return rc;

    if (colStr("loan.trial_date1").compareTo(this.getSysDate()) > 0) {
      return rc;
    }

    strSql = " update rsk_trial_loan set " + " loan_flag3 =:loan_flag3 , "
        + " trial_date3 =:trial_date3 , " + " loan_flag2 =:loan_flag2 , "
        + " trial_date2 =:trial_date2 , " + " loan_flag1 =:loan_flag1 , "
        + " trial_date1 =to_char(sysdate,'yyyymmdd') , " + " trial_mod_type ='1' , "
        + " trial_batch_no =:trial_batch_no " + " where id_no =:id_no ";

    setString("loan_flag3", colStr("loan.loan_flag2"));
    setString("trial_date2", colStr("loan.trial_date2"));
    setString("loan_flag1", colStr("loan.loan_flag1"));
    setString("trial_date1", colStr("loan.trial_date1"));
    setString("loan_flag1", colStr("trli.loan_flag"));
    setString("trial_batch_no", colStr("trli.batch_no"));
    setString("id_no", colStr("trli.id_no"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update RSK_TRIAL_LOAN error");
    }
    return rc;
  }


}
