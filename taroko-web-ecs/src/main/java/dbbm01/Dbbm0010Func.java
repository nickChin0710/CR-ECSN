/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-23  V1.00.01  Justin        parameterize sql
******************************************************************************/
package dbbm01;

import busi.FuncAction;

public class Dbbm0010Func extends FuncAction {
  String keyNo = "", lsIdPSeqno = "", lsChiName = "", lsCardNo = "";
  String lsBillType = "", lsAcctCode = "", lsChiDesc = "", lsPurchaseDate = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      keyNo = wp.itemStr("kk_key_no");
      if (empty(keyNo)) {
        errmsg("登錄批號: 不可空白");
        return;
      }
    } else {
      keyNo = wp.itemStr("rowid");
    }

    if (ibDelete && wp.itemEq("apr_flag", "Y")) {
      errmsg("已放行: 不可刪除");
      return;
    }

    if (ibDelete)
      return;

    lsCardNo = wp.itemStr("card_no");
    lsChiName = wp.itemStr("chi_name");

    if (wfCheckId(wp.itemStr("id_no")) == -1) {
      errmsg("此身分證號不存在");
      return;
    }

    if (wfCheckMajidno() == -1) {
      errmsg("此身分證號不存在");
      return;
    }

    if (wfCheckCardno() == -1) {
      errmsg("此卡為無效卡");
      return;
    } else if (wfCheckCardno() == -2) {
      errmsg("此卡號不是 Debit Card");
      return;
    }

    if (wfCheckId1(lsCardNo) == -1) {
      errmsg("此卡號與身分證號不符");
      return;
    }

    /*
    if (!wp.itemEq("add_item", "OI") && !wp.itemEq("add_item", "LF") && !wp.itemEq("add_item", "UC")
        && !wp.itemEq("add_item", "RF") && !wp.itemEq("add_item", "RR")
        && !wp.itemEq("add_item", "HC")) {
      errmsg("必需輸入 OI, LF , UC , RF , RR , HC!");
      return;
    }

    if (wp.itemEq("add_item", "RR") && wp.itemEmpty("card_no")) {
      errmsg("調單費必需輸入卡號 !");
      return;
    }
    
    */
    
    //所有加檔交易皆必須輸入卡號
    if ( wp.itemEmpty("card_no")) {
        errmsg("必需輸入卡號 !");
        return;
      }

    String lsBillDesc = wp.itemStr("bill_desc").trim();

    if (empty(lsBillDesc) && (wp.itemEq("add_item", "05") || wp.itemEq("add_item", "OI")
        || wp.itemEq("add_item", "HC"))) {
      errmsg("[其他費用]或[本金]或[其他應收款]必需輸入[對帳單文字]!");
      return;
    }

    if (!empty(lsBillDesc) && (!wp.itemEq("add_item", "05") && !wp.itemEq("add_item", "OI")
        && !wp.itemEq("add_item", "HC"))) {
      errmsg("非[其他費用]及[本金]及[其他應收款] ,不能輸入[對帳單文字]!");
      return;
    }


    // -----------------------------------------------------------------------
    lsBillType = "OKOL";
    
    String sql1 = " select " + " acct_code, exter_desc " + " from ptr_billtype " + " where bill_type = 'OKOL' and txn_code = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("add_item")});
    if (sqlRowNum > 0) {
      lsChiDesc = colStr("exter_desc");
      lsAcctCode = colStr("acct_code");
    }

    lsPurchaseDate = wp.itemStr("purchase_date");
    if (empty(lsPurchaseDate))
      lsPurchaseDate = getSysDate();

    if (wp.itemNum("dest_amt") == 0) {
      errmsg("金額不可<=0");
      return;
    }

    if (ibAdd)
      return;

    sqlWhere = " where nvl(mod_seqno,0) =? and and hex(rowid) = ? " ;

    Object[] parms = new Object[] {wp.itemNum("mod_seqno"), keyNo};
    if (this.isOtherModify("dbb_othexp", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }


  }

  int wfCheckId(String ls_id_no) {
    String sql1 =
        " select " + " id_p_seqno , " + " chi_name " + " from dbc_idno " + " where id_no = ? ";

    sqlSelect(sql1, new Object[] {ls_id_no});

    if (sqlRowNum <= 0)
      return -1;

    lsIdPSeqno = colStr("id_p_seqno");
    if (empty(lsChiName))
      lsChiName = colStr("chi_name");

    return 0;
  }

  int wfCheckMajidno() {
    String sql1 = " select " + " card_no " + " from dbc_card " + " where id_p_seqno = ? "
        + " and acct_type ='90' " + " and current_code ='0' ";

    sqlSelect(sql1, new Object[] {lsIdPSeqno});

    if (sqlRowNum <= 0)
      return -1;
    if (empty(lsCardNo))
      lsCardNo = colStr("card_no");

    return 0;
  }

  int wfCheckCardno() {

    String sql1 = " select " + " current_code " + " from dbc_card " + " where card_no = ? ";

    sqlSelect(sql1, new Object[] {lsCardNo});

    if (sqlRowNum <= 0) {
      return -1;
    }

    if (!colEq("current_code", "0")) {
      return -1;
    }

    String sql2 = " select " + " debit_flag " + " from ptr_bintable " + " where bin_no =? ";

    sqlSelect(sql2, new Object[] {commString.mid(lsCardNo, 0, 6)});

    if (!colEq("debit_flag", "Y"))
      return -2;

    return 0;
  }

  int wfCheckId1(String ls_card_no) {

    String sql1 = " select " + " card_no " + " from dbc_card " + " where id_p_seqno = ? "
        + " and acct_type ='90' " + " and card_no = ? ";

    sqlSelect(sql1, new Object[] {lsIdPSeqno, ls_card_no});

    if (sqlRowNum <= 0)
      return -1;

    return 0;
  }


  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into dbb_othexp (" + " bill_type , " + " txn_code , " + " add_item , "
        + " card_no , " + " acct_type , " + " dest_amt , " + " dest_curr , " + " purchase_date , "
        + " chi_desc , " + " bill_desc , " + " id_p_seqno , " + " key_no , " + " process_flag , "
        + " apr_flag , " + " apr_user , " + " mod_user , " + " mod_time , " + " mod_pgm , "
        + " mod_seqno " + " ) values ( " + " :bill_type , " + " :txn_code , " + " :add_item , "
        + " :card_no , " + " :acct_type , " + " :dest_amt , " + " '901' , " + " :purchase_date , "
        + " :chi_desc , " + " :bill_desc , " + " :id_p_seqno , " + " :key_no , " + " 'N' , "
        + " 'N' , " + " '' , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    setString("bill_type", lsBillType);
    setString("txn_code", wp.itemStr("add_item"));
    setString("add_item", wp.itemStr("add_item"));
    setString("card_no", lsCardNo);
    item2ParmStr("acct_type");
    item2ParmNum("dest_amt");
    setString("purchase_date", lsPurchaseDate);
    setString("chi_desc", lsChiDesc);
    item2ParmStr("bill_desc");
    setString("id_p_seqno", lsIdPSeqno);
    setString("key_no", keyNo);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert dbb_othexp error !");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update dbb_othexp set " + " bill_type =:bill_type , " + " txn_code =:txn_code , "
        + " add_item =:add_item , " + " card_no =:card_no , " + " acct_type =:acct_type , "
        + " dest_amt =:dest_amt , " + " purchase_date =:purchase_date , "
        + " chi_desc =:chi_desc , " + " bill_desc =:bill_desc , " + " process_flag = 'N' , "
        + " apr_flag = 'N' , " + " apr_user = '' , " + " mod_user =:mod_user , "
        + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where mod_seqno =:mod_seqno and rowid =x:rowid " ;

    setString("bill_type", lsBillType);
    setString("txn_code", wp.itemStr("add_item"));
    setString("add_item", wp.itemStr("add_item"));
    setString("card_no", lsCardNo);
    item2ParmStr("acct_type");
    item2ParmNum("dest_amt");
    setString("purchase_date", lsPurchaseDate);
    setString("chi_desc", lsChiDesc);
    item2ParmStr("bill_desc");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmNum("mod_seqno");
    setString("rowid", keyNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update dbb_othexp error !");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete dbb_othexp where 1=1 and hex(rowid) = ? " ;
    setString(keyNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete dbb_othexp error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    msgOK();
    // 前面已有檢核
    // dataInputCheck();
    // if(rc!=1) return rc;

    strSql = " insert into dbb_othexp (" + " key_no ," + " bill_type , "+ " id_p_seqno , " + " acct_type , "
        + " dest_amt , " + " card_no , " + " add_item , " + " purchase_date , " + " bill_desc , "
        + " process_flag , " + " error_code , " + " txn_code , " + " dest_curr , " + " p_seqno , "
        + " apr_flag , " + " apr_user , " + " mod_user , " + " mod_time , " + " mod_pgm , "
        + " mod_seqno " + " ) values ( " + " :key_no ," + " :bill_type , " + " :id_p_seqno , " + " '90' , "
        + " :dest_amt , " + " :card_no , " + " :add_item , " + " :purchase_date , "
        + " :bill_desc , " + " 'N' , " + " :error_code , " + " :txn_code , " + " '901' , "
        + " :p_seqno , " + " 'N' , " + " '' , " + " :mod_user , " + " sysdate , " + " :mod_pgm , "
        + " 1 " + " ) ";
    var2ParmStr("key_no");
    var2ParmStr("bill_type");
    var2ParmStr("id_p_seqno");
    var2ParmNum("dest_amt");
    var2ParmStr("card_no");
    var2ParmStr("add_item");
    var2ParmStr("purchase_date");
    var2ParmStr("bill_desc");
    var2ParmStr("error_code");
    var2ParmStr("txn_code");
    var2ParmStr("p_seqno");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("file input error !");
    }
    return rc;
  }

  void dataInputCheck() {
    msgOK();

    if (wfCheckId(varsStr("id_no")) == -1) {
      errmsg("此身分證號不存在");
      return;
    }

    if (wfCheckId1(varsStr("card_no")) == -1) {
      errmsg("此卡號與身分證號不符");
      return;
    }

  }

  public int deleteProc() {
    msgOK();

    strSql = "delete dbb_othexp where 1=1 and hex(rowid) = ? " ;
    setString(varsStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete dbb_othexp error");
      return rc;
    }

    return rc;
  }

}
