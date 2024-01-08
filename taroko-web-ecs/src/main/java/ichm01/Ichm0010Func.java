/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
*                                                                            *
******************************************************************************/
package ichm01;

import busi.FuncAction;

public class Ichm0010Func extends FuncAction {
  String binType = "", addYear = "", bankId = "", groupId = "", seqNo = "", lsCardCode = "", lsBin = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      binType = wp.itemStr2("kk_bin_type");
      addYear = wp.itemStr2("kk_add_year");
      bankId = wp.itemStr2("kk_bank_id");
      groupId = wp.itemStr2("kk_group_id");
      seqNo = wp.itemStr2("kk_seq_no");

    } else {
      binType = wp.itemStr2("bin_type");
      addYear = wp.itemStr2("add_year");
      bankId = wp.itemStr2("bank_id");
      groupId = wp.itemStr2("group_id");
      seqNo = wp.itemStr2("seq_no");
    }

    if (ibDelete) {
      if (!wp.itemEmpty("send_date")) {
        errmsg("已傳送iCash 不可刪除");
        return;
      }
      return;
    }

    if (empty(binType)) {
      errmsg("國際組織: 不可空白");
      return;
    }
    if (empty(addYear)) {
      errmsg("新增年度: 不可空白");
      return;
    }
    if (empty(bankId)) {
      errmsg("銀行別: 不可空白");
      return;
    }
    if (empty(groupId)) {
      errmsg("聯名代碼: 不可空白");
      return;
    }
    if (empty(seqNo)) {
      errmsg("序號: 不可空白");
      return;
    }

    if (isNumber(addYear) == false || addYear.length() != 4) {
      errmsg("新增年度: 輸入錯誤");
      return;
    }

    if (isNumber(seqNo) == false || seqNo.length() != 2) {
      errmsg("序號: 輸入錯誤");
      return;
    }

    if (eqIgno(binType, "V")) {
      lsBin = "1";
    } else if (eqIgno(binType, "M")) {
      lsBin = "2";
    } else if (eqIgno(binType, "J")) {
      lsBin = "3";
    }

    if (ibAdd) {
      lsCardCode = bankId + groupId + commString.mid(addYear, 2, 2) + lsBin + seqNo;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ich_card_parm ( " + " bin_type ," + " add_year ," + " seq_no ,"
        + " bank_id ," + " group_id ," + " card_name ," + " card_code ," + " send_date ,"
        + " vendor_ich ," + " seq_no_curr, " + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( "
        + " :bin_type ," + " :add_year ," + " :seq_no ," + " :bank_id ," + " :group_id ,"
        + " :card_name ," + " :card_code ," + " '' ," + " :vendor_ich ," + " :seq_no_curr , "
        + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    setString("bin_type", binType);
    setString("add_year", addYear);
    setString("seq_no", seqNo);
    setString("bank_id", bankId);
    setString("group_id", groupId);
    item2ParmStr("card_name");
    setString("card_code", lsCardCode);
    setString("seq_no_curr", wp.itemStr("seq_no_curr"));
    item2ParmStr("vendor_ich");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr2("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ich_card_parm error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql =
        " update ich_card_parm set " + " card_name =:card_name , " + " vendor_ich =:vendor_ich , " + " seq_no_curr = :seq_no_curr, "
            + " apr_date =to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user , "
            + " mod_time = sysdate , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
            + " mod_seqno =nvl(mod_seqno,0)+1 " + " where card_code =:card_code ";

    item2ParmStr("card_name");
    item2ParmStr("vendor_ich");
    setString("seq_no_curr", wp.itemStr("seq_no_curr"));
    setString("apr_user", wp.itemStr2("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmStr("card_code");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update ich_card_parm error !");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete ich_card_parm where card_code =:card_code ";
    item2ParmStr("card_code");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete ich_card_parm error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
