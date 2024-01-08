/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-30  V1.00.01  Alex       program initial						
* 109-04-28  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03  shiyuqi   coding standard, rename                     *   
******************************************************************************/
package rskm03;

import busi.FuncAction;

public class Rskm3310Func extends FuncAction {
  String cardNo = "";

  @Override
  public void dataCheck() {
    if (ibAdd)
      cardNo = wp.itemStr("kk_card_no");
    else
      cardNo = wp.itemStr("card_no");
    
    if(empty(cardNo)) {
    	errmsg("黑名單卡號: 不可空白");
    	return ;
    }
    
    
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    insertBlockCard(cardNo, wp.itemStr("card_remark"), "1");
    if (rc != 1)
      return rc;
    insertBlockLog(cardNo, wp.itemStr("card_remark"), "1", "A");

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update rsk_block_card set " + " card_remark =:card_remark , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user = :apr_user , "
        + " mod_user = :mod_user , " + " mod_time = sysdate , " + " mod_pgm = :mod_pgm "
        + " where card_no =:kk1 ";

    item2ParmStr("card_remark");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk1", cardNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    insertBlockLog(cardNo, wp.itemStr("card_remark"), "1", "U");

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete rsk_block_card where card_no =:kk1 ";
    setString("kk1", cardNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    insertBlockLog(cardNo, wp.itemStr("card_remark"), "1", "D");

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int dataUpload() {
    msgOK();
    insertBlockCard(varsStr("card_no"), varsStr("card_remark"), "2");
    if (rc != 1)
      return rc;
    insertBlockLog(varsStr("card_no"), varsStr("card_remark"), "2", "A");
    return rc;
  }

  public int dataDelete() {
    msgOK();
    deleteBlockCard();
    if (rc != 1)
      return rc;
    insertBlockLog(varsStr("card_no"), varsStr("card_remark"), varsStr("add_type"), "D");
    return rc;
  }

  public int insertBlockCard(String lsCardNo, String lsCardRemark, String lsAddType) {
    msgOK();

    strSql = " insert into rsk_block_card ( " + " card_no , " + " add_type , " + " card_remark , "
        + " apr_date , " + " apr_user , " + " crt_date , " + " crt_user , " + " mod_time , "
        + " mod_user , " + " mod_pgm ) values ( " + " :card_no , "
        + " :add_type , " + " :card_remark , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " sysdate , " + " :mod_user , "
        + " :mod_pgm ) ";

    setString("card_no", lsCardNo);
    setString("card_remark", lsCardRemark);
    setString("add_type", lsAddType);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int deleteBlockCard() {
    msgOK();

    strSql = "delete rsk_block_card where card_no =:card_no ";
    var2ParmStr("card_no");
    sqlExec(strSql);
    if (rc != 1) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int insertBlockLog(String lsCardNo, String lsCardRemark, String lsAddType,
      String lsModAudcode) {
    msgOK();

    strSql = " insert into rsk_block_card_log ( " + " card_no , " + " add_type , "
        + " card_remark , " + " mod_time , " + " mod_user , " + " mod_pgm , " + " mod_audcode "
        + " ) values ( " + " :card_no , " + " :add_type , " + " :card_remark , " + " sysdate , "
        + " :mod_user , " + " :mod_pgm , " + " :mod_audcode" + " ) ";

    setString("card_no", lsCardNo);
    setString("card_remark", lsCardRemark);
    setString("add_type", lsAddType);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("mod_audcode", lsModAudcode);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

}
