/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package tscm01;

import busi.FuncAction;

public class Tscp2260Func extends FuncAction {

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

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
    msgOK();
    insertLog();
    if (rc != 1) {
      return rc;
    }
    updateChgBack();
    if (rc != 1) {
      return rc;
    }
    return rc;
  }

  public int insertLog() {
    msgOK();
    strSql = "insert into tsc_ecti_log (" + " crt_date ," + " crt_time ," + " tran_code ,"
        + " tsc_card_no ," + " tran_date ," + " tran_time ," + " tran_amt ," + " traff_code ,"
        + " place_code ," + " traff_subname ," + " place_subname ," + " chgback_reason ,"
        + " reference_no ," + " reference_seq ," + " proc_flag ," + " online_mark ," + " mod_time ,"
        + " mod_pgm " + " ) values (" + " to_char(sysdate,'yyyymmdd') ,"
        + " to_char(sysdate,'hh24miss') ," + " '7209' ," + " :tsc_card_no ," + " :tran_date ,"
        + " :tran_time ," + " :tran_amt ," + " :traff_code ," + " Lpad(:place_code,6,' ') ,"
        + " :traff_subname ," + " :place_subname ," + " :chgback_reason ," + " :reference_no ,"
        + " :reference_seq ," + " 'N' ," + " :online_mark ," + " sysdate ," + " :mod_pgm " + " )";

    var2ParmStr("tsc_card_no");
    var2ParmStr("tran_date");
    var2ParmStr("tran_time");
    var2ParmNum("tran_amt");
    var2ParmStr("traff_code");
    var2ParmStr("place_code");
    var2ParmStr("traff_subname");
    var2ParmStr("place_subname");
    var2ParmStr("chgback_reason");
    var2ParmStr("reference_no");
    var2ParmNum("reference_seq");
    var2ParmStr("online_mark");
    setString("mod_pgm", "tscp2260");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert tsc_ecti_log error, " + getMsg());
    }
    return rc;
  }

  public int updateChgBack() {
    msgOK();
    strSql =
        "update rsk_chgback set " + " send_flag = '0' ," + " fst_send_cnt = nvl(fst_send_cnt,0)+1 ,"
            + " fst_send_date = to_char(sysdate,'yyyymmdd') ," + " mod_time =sysdate ,"
            + " mod_pgm =:mod_pgm , " + " mod_seqno =nvl(mod_seqno,0)+1 "
            + " where reference_no =:reference_no " + " and reference_seq =:reference_seq ";
    setString("mod_pgm", "tscp2260");
    var2ParmStr("reference_no");
    var2ParmNum("reference_seq");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert rsk_chgback error, " + getMsg());
    }
    return rc;
  }

}
