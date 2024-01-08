/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-29  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package smsm01;

import busi.FuncAction;

public class Smsm0010Func extends FuncAction {
  String msgPgm = "", isMsgDesc = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      msgPgm = wp.itemStr("kk_msg_pgm");
    } else {
      msgPgm = wp.itemStr("msg_pgm");
    }
    
    if(empty(msgPgm)) {
    	errmsg("使用程式: 不可空白");
    	return ;
    }
    
    if (wp.itemNum("msg_hour1") != 0 && wp.itemNum("msg_hour1") >= wp.itemNum("msg_hour2")) {
      errmsg("當日發送時點: 起迄錯誤");
      return;
    }

    if (wp.itemEmpty("msg_id")) {
      errmsg("簡訊代碼: 不可空白");
      return;
    }

    isMsgDesc = wp.itemStr("msg_desc1") + wp.itemStr("msg_desc2") + wp.itemStr("msg_desc3")
        + wp.itemStr("msg_desc4");

    if (empty(isMsgDesc)) {
      errmsg("簡訊內容: 不可空白");
      return;
    }
    
    if (wp.itemEmpty("send_eff_date1") || wp.itemEmpty("send_eff_date2")) {
    	errmsg("發送起迄日: 不可空白");
    	return ;
    }
    
    if(wp.itemStr("send_eff_date1").compareTo(wp.itemStr("send_eff_date2")) > 0) {
    	errmsg("發送起迄日: 起迄錯誤");
    	return ;
    }
    
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into sms_msg_id ( " + " msg_pgm , " + " msg_dept , " + " msg_serve , "
        + " msg_id , " + " msg_desc , " + " msg_send_flag , " + " acct_type_sel , "
        + " msg_userid , " + " msg_sel_amt01 , " + " msg_amt01 , " + " msg_run_day , "
        + " msg_hour1 , " + " msg_hour2 , " + " send_eff_date1 , " + " send_eff_date2 , "
        + " crt_date , " + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , "
        + " mod_pgm , " + " mod_time , " + " mod_seqno " + " ) values ( " + " :kk1 , "
        + " :msg_dept , " + " :msg_serve , " + " :msg_id , " + " :msg_desc , "
        + " :msg_send_flag , " + " :acct_type_sel , " + " :msg_userid , " + " :msg_sel_amt01 , "
        + " :msg_amt01 , " + " :msg_run_day , " + " :msg_hour1 , " + " :msg_hour2 , "
        + " :send_eff_date1 , " + " :send_eff_date2 , " + " to_char(sysdate,'yyyymmdd') , "
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , "
        + " :mod_pgm , " + " sysdate , " + " 1 " + " ) ";

    setString("kk1", msgPgm);
    item2ParmStr("msg_dept");
    item2ParmStr("msg_serve");
    item2ParmStr("msg_id");
    setString("msg_desc", isMsgDesc);
    item2ParmNvl("msg_send_flag", "N");
    item2ParmNvl("acct_type_sel", "0");
    item2ParmStr("msg_userid");
    item2ParmNvl("msg_sel_amt01", "N");
    item2ParmNum("msg_amt01");
    item2ParmNum("msg_run_day");
    item2ParmStr("msg_hour1");
    item2ParmStr("msg_hour2");
    item2ParmStr("send_eff_date1");
    item2ParmStr("send_eff_date2");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update sms_msg_id set " + " msg_dept =:msg_dept , " + " msg_serve =:msg_serve , "
        + " msg_id =:msg_id , " + " msg_desc =:msg_desc , " + " msg_send_flag =:msg_send_flag , "
        + " acct_type_sel =:acct_type_sel , " + " msg_userid =:msg_userid , "
        + " msg_sel_amt01 =:msg_sel_amt01 , " + " msg_amt01 =:msg_amt01 , "
        + " msg_run_day =:msg_run_day , " + " msg_hour1 =:msg_hour1 , "
        + " msg_hour2 =:msg_hour2 , " + " send_eff_date1 =:send_eff_date1 , "
        + " send_eff_date2 =:send_eff_date2 , " + " apr_date = to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user , " + " mod_user =:mod_user , " + " mod_time =sysdate , "
        + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 " + " where msg_pgm =:kk1 ";

    item2ParmStr("msg_dept");
    item2ParmStr("msg_serve");
    item2ParmStr("msg_id");
    setString("msg_desc", isMsgDesc);
    item2ParmNvl("msg_send_flag", "N");
    item2ParmNvl("acct_type_sel", "0");
    item2ParmStr("msg_userid");
    item2ParmNvl("msg_sel_amt01", "N");
    item2ParmNum("msg_amt01");
    item2ParmNum("msg_run_day");
    item2ParmStr("msg_hour1");
    item2ParmStr("msg_hour2");
    item2ParmStr("send_eff_date1");
    item2ParmStr("send_eff_date2");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk1", msgPgm);

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
    if (rc != 1)
      return rc;
    strSql = " delete sms_msg_id where msg_pgm =:kk1 ";
    setString("kk1", msgPgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int dbInsertDetl() {
    msgOK();
    strSql = " insert into sms_dtl_data ( " + " table_name , " + " data_key , " + " data_type , "
        + " data_code , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values ( " + " :table_name , " + " :data_key , " + " :data_type , " + " :data_code , "
        + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    item2ParmStr("table_name");
    setString("data_key", wp.itemStr("msg_pgm"));
    item2ParmStr("data_type");
    setString("data_code", wp.itemStr2("ex_data_code"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert sms_dtl_data error !");
    }

    return rc;
  }

  public int dbDeleteDetl() {
    msgOK();

    strSql = " delete sms_dtl_data where table_name = 'SMS_MSG_ID' and data_type = '1' "
        + " and data_key =:data_key and data_code =:data_code ";

    var2ParmStr("data_key");
    var2ParmStr("data_code");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete sms_dtl_data error !");
    }

    return rc;
  }

}
