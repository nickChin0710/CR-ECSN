/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskp2030Func extends FuncAction {

  @Override
  public void dataCheck() {
    if (ibDelete) {
      if (varsNum("imp_jcic_rows") > 0) {
        errmsg("資料已轉入 JCIC 查詢不可刪除");
        return;
      }
    }

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
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "delete rsk_trial_list " + " where batch_no =:batch_no ";
    var2ParmStr("batch_no");
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    procTrialParm();
    return rc;
  }

  public int procTrialParm() {
    if (varEq("regist_type", "1")) {
      strSql = "delete rsk_trial_parm " + " where batch_no =:batch_no " + " and regist_type='1' ";
      var2ParmStr("batch_no");
    } else if (varEq("regist_type", "2")) {
      strSql = "update rsk_trial_parm set " + " list_crt_date = '', " + " list_crt_rows = '0', "
          + " mod_time=sysdate," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where batch_no =:batch_no "
          + " and regist_type='2' ";;
      var2ParmStr("batch_no");
    }
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

  public int updateTrialParm() {
    msgOK();
    strSql = " update rsk_trial_parm set " + " batch_seqno = :batch_seqno "
        + " where batch_no =:batch_no ";
    var2ParmNum("batch_seqno");
    var2ParmStr("batch_no");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_parm error !");
    }

    return rc;
  }

  public int updateTrialParmType() {
    msgOK();
    strSql = " update rsk_trial_parm set " + " trans_type =? " + " where batch_no =? ";
    setString2(1, varsStr2("trans_type"));
    setString(varsStr2("batch_no"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_trial_parm(trans_type) error");
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
