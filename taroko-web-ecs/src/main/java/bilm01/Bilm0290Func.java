/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-10  V1.00.00  yash       program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *  
* 109-08-05  V1.00.02  JeffKung update mchtNo to required field  
******************************************************************************/

package bilm01;


import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0290Func extends FuncEdit {
  String mKkMerchantNo = "";
  String mKkFeesBillType = "";
  String mKkFeesTxnCode = "";

  public Bilm0290Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      mKkMerchantNo = wp.itemStr("kk_merchant_no");
      if (empty(mKkMerchantNo)) {
    	  errmsg("特店代號不可為空值!");
          return;
      }
      mKkFeesBillType = wp.itemStr("fees_bill_type");
      mKkFeesTxnCode = wp.itemStr("fees_txn_code");
    } else {
      mKkMerchantNo = wp.itemStr("merchant_no");
      mKkFeesBillType = wp.itemStr("fees_bill_type");
      mKkFeesTxnCode = wp.itemStr("fees_txn_code");
    }

    if (!this.isDelete()) {
      String lsSql =
          "select count(*) as tot_cnt1 from ptr_billtype where bill_type = ? and  txn_code = ? ";
      Object[] param = new Object[] {wp.itemStr("fees_bill_type"), wp.itemStr("fees_txn_code")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt1") <= 0) {
        errmsg("帳單交易別參數錯誤!");
        return;
      }
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from ptr_prepaidfee_m where merchant_no = ? and fees_bill_type = ? and fees_txn_code= ? ";
      Object[] param = new Object[] {mKkMerchantNo, mKkFeesBillType, mKkFeesTxnCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere =
          " where merchant_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkMerchantNo, wp.modSeqno()};
      isOtherModify("ptr_prepaidfee_m", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    dateTime();

    strSql = "insert into ptr_prepaidfee_m (" + " merchant_no " + ", fees_bill_type "
        + ", fees_txn_code " + ", nor_amt " + ", nor_fix_amt " + ", nor_percent " + ", dom_fix_amt "
        + ", dom_percent " + ", dom_min_amt " + ", dom_max_amt " + ", tx_date_f " + ", tx_date_e "
        + ", spe_amt " + ", spe_fix_amt " + ", spe_percent " + ", int_fix_amt " + ", int_percent "
        + ", int_min_amt " + ", int_max_amt " + ", file_date " + ", file_time "
        + ", mod_time, crt_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? " + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkMerchantNo // 1
        , mKkFeesBillType, mKkFeesTxnCode,
        wp.itemStr("nor_amt").equals("") ? 0 : wp.itemNum("nor_amt"),
        wp.itemStr("nor_fix_amt").equals("") ? 0 : wp.itemNum("nor_fix_amt"),
        wp.itemStr("nor_percent").equals("") ? 0 : wp.itemNum("nor_percent"),
        wp.itemStr("dom_fix_amt").equals("") ? 0 : wp.itemNum("dom_fix_amt"),
        wp.itemStr("dom_percent").equals("") ? 0 : wp.itemNum("dom_percent"),
        wp.itemStr("dom_min_amt").equals("") ? 0 : wp.itemNum("dom_min_amt"),
        wp.itemStr("dom_max_amt").equals("") ? 0 : wp.itemNum("dom_max_amt"),
        wp.itemStr("tx_date_f"), wp.itemStr("tx_date_e"),
        wp.itemStr("spe_amt").equals("") ? 0 : wp.itemNum("spe_amt"),
        wp.itemStr("spe_fix_amt").equals("") ? 0 : wp.itemNum("spe_fix_amt"),
        wp.itemStr("spe_percent").equals("") ? 0 : wp.itemNum("spe_percent"),
        wp.itemStr("int_fix_amt").equals("") ? 0 : wp.itemNum("int_fix_amt"),
        wp.itemStr("int_percent").equals("") ? 0 : wp.itemNum("int_percent"),
        wp.itemStr("int_min_amt").equals("") ? 0 : wp.itemNum("int_min_amt"),
        wp.itemStr("int_max_amt").equals("") ? 0 : wp.itemNum("int_max_amt"), sysDate, sysTime,
        wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    dateTime();

    strSql = "update ptr_prepaidfee_m set " + " nor_amt =? " + " ,nor_fix_amt =? "
        + " ,nor_percent =? " + " ,dom_fix_amt =? " + " ,dom_percent =? " + " ,dom_min_amt =? "
        + " ,dom_max_amt =? " + " ,tx_date_f =? " + " ,tx_date_e =? " + " ,spe_amt =? "
        + " ,spe_fix_amt =? " + " ,spe_percent =? " + " ,int_fix_amt =? " + " ,int_percent =? "
        + " ,int_min_amt =? " + " ,int_max_amt =? " + " ,file_date =? " + " ,file_time =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + " , fees_bill_type =? , fees_txn_code = ? "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemNum("nor_amt"), wp.itemNum("nor_fix_amt"),
        wp.itemNum("nor_percent"), wp.itemNum("dom_fix_amt"), wp.itemNum("dom_percent"),
        wp.itemNum("dom_min_amt"), wp.itemNum("dom_max_amt"), wp.itemStr("tx_date_f"),
        wp.itemStr("tx_date_e"), wp.itemNum("spe_amt"), wp.itemNum("spe_fix_amt"),
        wp.itemNum("spe_percent"), wp.itemNum("int_fix_amt"), wp.itemNum("int_percent"),
        wp.itemNum("int_min_amt"), wp.itemNum("int_max_amt"), sysDate, sysTime, wp.loginUser,
        wp.itemStr("mod_pgm"), mKkFeesBillType, mKkFeesTxnCode,mKkMerchantNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete ptr_prepaidfee_m " + sqlWhere;
    Object[] param = new Object[] {mKkMerchantNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }



}
