/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi.func;

import busi.FuncBase;

public class BilSysexp extends FuncBase {

  public String cardNo = ""; // 消費卡號
  public String billType = ""; // 帳單類別(請款類別)
  public String txnCode = ""; // 交易別
  public String purchaseDate = ""; // 消費日期
  public String srcType = ""; // 來源種類
  public String mchtNo = ""; // 特店代號
  public double destAmt = 0; // 目的地金額
  public String destCurr = ""; // 目的地幣別
  public double srcAmt = 0; // 原使金額
  public String billDesc = ""; // 對帳單上中文摘要
  public String postFlag = ""; // 已過帳單否
  public String aoFlag = ""; // AO註記
  public String authCode = ""; // 授權碼
  public String mchtCategory = ""; // 特店種類
  public String mergeFlag = ""; // 轉卡註記
  public String installmentKind = ""; // 分期付款種類
  public String ptrMchtNo = ""; // 分期付款參數特店代號
  public String currCode = ""; // 幣別
  public double dcDestAmt = 0; // 外幣目的地金額

  int errSql(String s1) {
    errmsg(s1 + " BIL_SYSEXP error; " + sqlErrtext);
    return -1;
  }

  public void dataReset() {
    cardNo = "";
    billType = "";
    txnCode = "";
    purchaseDate = "";
    srcType = "";
    mchtNo = "";
    destAmt = 0;
    destCurr = "901";
    srcAmt = 0;
    billDesc = "";
    postFlag = "N";
    aoFlag = "";
    authCode = "";
    mchtCategory = "";
    mergeFlag = "";
    installmentKind = "";
    ptrMchtNo = "";
    currCode = "";
    dcDestAmt = 0;
  }

  public int dataInsert() {
    msgOK();
    strSql =
        "insert into bil_sysexp (" + "  card_no " + ", bill_type " + ", txn_code "
            + ", purchase_date " + ", src_type" + ", mcht_no" + ", dest_amt" + ", dest_curr "
            + ", src_amt " + ", bill_desc " + ", post_flag " + ", ao_flag " + ", auth_code "
            + ", mcht_category " + ", merge_flag " + ", installment_kind " + ", ptr_mcht_no "
            + ", curr_code " + ", dc_dest_amt " + ", mod_user " + ", mod_time " + ", mod_pgm "
            + ", mod_seqno " + " ) values ( " + "  :card_no " + ", :bill_type " + ", :txn_code "
            + ", :purchase_date " + ", :src_type " + ", :mcht_no " + ", :dest_amt "
            + ", :dest_curr " + ", :src_amt " + ", :bill_desc " + ", :post_flag " + ", :ao_flag "
            + ", :auth_code " + ", :mcht_category " + ", :merge_flag " + ", :installment_kind "
            + ", :ptr_mcht_no " + ", :curr_code " + ", :dc_dest_amt " + ", :mod_user "
            + ", sysdate " + ", :mod_pgm " + ", 1 " + " )";
    setString("card_no", cardNo);
    setString("bill_type", billType);
    setString("txn_code", txnCode);
    setString("purchase_date", purchaseDate);
    setString("src_type", srcType);
    setString("mcht_no", mchtNo);
    setDouble("dest_amt", destAmt);
    setString("dest_curr", destCurr);
    setDouble("src_amt", srcAmt);
    setString("bill_desc", billDesc);
    setString("post_flag", postFlag);
    setString("ao_flag", aoFlag);
    setString("auth_code", authCode);
    setString("mcht_category", mchtCategory);
    setString("merge_flag", mergeFlag);
    setString("installment_kind", installmentKind);
    setString("ptr_mcht_no", ptrMchtNo);
    setString("curr_code", currCode);
    setDouble("dc_dest_amt", dcDestAmt);
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      return errSql("insert: ");
    }

    return rc;
  }

}
