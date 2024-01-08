
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package dbsm01;

import busi.FuncAction;

public class Dbsp4040Func extends FuncAction {
  String isErrCode = "", lsAcctCode = "", lsBillType = "", lsAdjustType = "";

  @Override
  public void dataCheck() {
    wfCheckErrcode();

  }

  void wfCheckErrcode() {
    // --check act_acaj 已調整未處理--
    String sql1 = " select " + " count(*) as ll_cnt1 " + " from dba_acaj "
        + " where reference_no = ? " + " and nvl(process_flag,'N')<>'Y' ";

    sqlSelect(sql1, new Object[] {colStr("reference_no")});

    if (colNum("ll_cnt1") > 0) {
      isErrCode = "01"; // -帳務已調整, 批次未處理-
      return;
    }


    // -查無來源帳款---
    String sql2 =
        " select " + " count(*) as ll_cnt2 " + " from dba_debt " + " where reference_no = ? ";

    sqlSelect(sql2, new Object[] {colStr("reference_no")});

    if (colNum("ll_cnt2") > 0) {
      isErrCode = "02";
      return;
    }

    // --是否列問交--
    String sql3 = " select " + " count(*) as ll_cnt3 " + " from rsk_problem "
        + " where reference_no = ? " + " and prb_status not in ('80','85') ";

    sqlSelect(sql3, new Object[] {colStr("reference_no")});

    if (colNum("ll_cnt3") > 0) {
      isErrCode = "03"; // -此交易已列問交卻未結案, 不可D檔-
      return;
    }

    // -D檔金額-
    if (colNum("adj_amt") < 0) {
      isErrCode = "04"; // -D檔金額不可小 0-
      return;
    }

    // --目前可 D 數--
    if (colNum("adj_amt") > colNum("d_avail_bal")) {
      isErrCode = "05"; // -D檔金額大於可D數-
      return;
    }
    String tmpStr = "";
    lsAcctCode = colStr("acct_code");
    lsBillType = colStr("bill_type");

    if (eqIgno(lsAcctCode, "ID")) {
      tmpStr = commString.mid(lsBillType, 1, 1);
      if (eqIgno(tmpStr, "1"))
        lsAdjustType = "DE01";
      else if (eqIgno(tmpStr, "2"))
        lsAdjustType = "DE04";
      else
        lsAdjustType = "DE07";
    } else if (pos("BL|CB|CA|IT|AO|DB", lsAcctCode) > 0) {
      lsAdjustType = "DE08";
    } else if (pos("AF|LF|CF|PF|SF|CC", lsAcctCode) > 0) {
      lsAdjustType = "DE09";
    } else if (pos("RI|AI|CI", lsAcctCode) > 0) {
      lsAdjustType = "DE13";
    } else if (eqIgno("PN", lsAcctCode)) {
      lsAdjustType = "DE14";
    } else {
      isErrCode = "06"; // -調整類別比對不到-
    }


  }

  public int insertDbaAcaj() {
    msgOK();
    strSql = " insert into dba_acaj ( " + " p_seqno ," + " reference_no ," + " post_date ,"
        + " orginal_amt ," + " dr_amt ," + " cr_amt ," + " bef_amt ," + " bef_d_amt ,"
        + " acct_code ," + " func_code ," + " card_no ," + " interest_date ," + " adj_comment ,"
        + " acct_no ," + " proc_flag ," + " item_post_date ," + " purchase_date ," + " txn_code ,"
        + " aft_amt ," + " aft_d_amt ," + " value_type ," + " adj_reason_code ," + " debit_item ,"
        + " apr_flag ," + " chg_date ," + " chg_user ," + " crt_date ," + " crt_time ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( "
        + " :p_seqno ," + " :reference_no ," + " :post_date ," + " :orginal_amt ," + " :dr_amt ,"
        + " 0 ," + " :bef_amt ," + " :bef_d_amt ," + " :acct_code ," + " 'U' ," + " :card_no ,"
        + " :interest_date ," + " :adj_comment ," + " :acct_no ," + " 'N' ," + " :item_post_date ,"
        + " :purchase_date ," + " :txn_code ," + " :aft_amt ," + " :aft_d_amt ," + " '1' ,"
        + " '1' ," + " '14817000' ," + " 'N' ," + " to_char(sysdate,'yyyymmdd') ," + " :chg_user ,"
        + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " :mod_user ,"
        + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    setString("p_seqno", colStr("p_seqno"));
    setString("reference_no", colStr("reference_no"));
    setString("post_date", colStr("item_post_date"));
    setString("orginal_amt", "" + colNum("beg_bal"));
    setString("dr_amt", "" + colNum("adj_amt"));
    setString("bef_amt", "" + colNum("end_bal"));
    setString("bef_d_amt", "" + colNum("d_avail_bal"));
    setString("acct_code", colStr("acct_code"));
    setString("card_no", colStr("card_no"));
    setString("interest_date", colStr("interest_date"));
    setString("adj_comment", colStr("adj_remark"));
    setString("acct_no", colStr("acct_no"));
    setString("item_post_date", colStr("item_post_date"));
    setString("purchase_date", colStr("purchase_date"));
    setString("txn_code", colStr("txn_code"));
    setString("aft_amt", "" + (colNum("end_bal") - colNum("adj_amt")));
    setString("aft_d_amt", "" + (colNum("d_avail_bal") - colNum("adj_amt")));
    setString("chg_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "dbsp4040");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert dba_acaj error !");
    }

    return rc;
  }

  public int updateCmsAcaj() {
    msgOK();
    strSql = " update cms_acaj set " + " acct_post_flag = 'Y' , "
        + " acct_post_user =:acct_post_user , " + " acct_post_date = to_char(sysdate,'yyyymmdd') , "
        + " acct_errcode =:acct_errcode , " + " mod_user =:mod_user , " + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 "
        + commSqlStr.whereRowid(varsStr("rowid"));

    setString("acct_post_user", wp.loginUser);
    setString("acct_errcode", isErrCode);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "dbsp4040");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cms_acaj error ! ");
    }

    return rc;
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
    selectCmsAcaj();
    dataCheck();
    if (rc != 1)
      return rc;

    if (empty(isErrCode)) {
      insertDbaAcaj();
    }

    if (rc == 1) {
      updateCmsAcaj();
    }

    return rc;
  }

  void selectCmsAcaj() {
    String sql1 =
        " select " + " * " + " from cms_acaj " + " where 1=1 " + commSqlStr.whereRowid(varsStr("rowid"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      errmsg("D檔調整資料, 已不存在");
    }

  }

}
