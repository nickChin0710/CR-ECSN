/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-10  V1.00.00  Alex       program initial                            *
* 109-04-29  V1.00.01  Tanwei       updated for project coding standard
* 109-12-31  V1.00.03   shiyuqi   coding standard, rename                    * 
******************************************************************************/
package smsm01;

import busi.FuncAction;

public class Smsm0020Func extends FuncAction {
  String modSeqno = "", isMsgContent = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      modSeqno = wp.itemStr("kk_msg_id");
    } else
      modSeqno = wp.itemStr("msg_id");

    isMsgContent = wp.itemStr("msg_content1") + wp.itemStr("msg_content2")
        + wp.itemStr("msg_content3") + wp.itemStr("msg_content4");

    if (ibAdd)
      return;

    sqlWhere = " where msg_id = ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {modSeqno, wp.itemNum("mod_seqno")};
    if (isOtherModify("sms_msg_content", sqlWhere, parms)) {
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into sms_msg_content ( " + " msg_id , " + " msg_desc , " + " msg_content , "
        + " apr_flag , " + " apr_user , " + " apr_date , " + " crt_user , " + " crt_date , "
        + " crt_time , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values ( " + " :kk1 , " + " :msg_desc , " + " :msg_content , " + " 'Y' , "
        + " :apr_user , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , " + " :mod_user , "
        + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    setString("kk1", modSeqno);
    item2ParmStr("msg_desc");
    setString("msg_content", isMsgContent);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert sms_msg_content error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update sms_msg_content set " + " msg_desc =:msg_desc , "
        + " msg_content =:msg_content , " + " apr_flag = 'Y' , " + " apr_user =:apr_user , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " mod_user =:mod_user , "
        + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where msg_id =:kk1 " + " and nvl(mod_seqno,0) = :mod_seqno ";

    item2ParmStr("msg_desc");
    setString("msg_content", isMsgContent);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk1", modSeqno);
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update sms_msg_content error !");
      return rc;
    }
    
    //--
    
    strSql = " update sms_msg_id set msg_desc =:msg_desc where msg_id =:msg_id ";
    setString("msg_desc",isMsgContent);
    setString("msg_id",modSeqno);
    
    sqlExec(strSql);
    if (sqlRowNum < 0) {    	
        errmsg("update sms_msg_id error !");
        return rc;
    }	else	rc =1;
            
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " delete sms_msg_content where msg_id =:kk1 and nvl(mod_seqno,0) = :mod_seqno ";
    setString("kk1", modSeqno);
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete sms_msg_content error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
