/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-03  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0160Func extends FuncEdit {
  String mKkBillType = "";
  String mKkTxnCode = "";

  public Ptrm0160Func(TarokoCommon wr) {
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
      mKkBillType = wp.itemStr("kk_bill_type");
      mKkTxnCode = wp.itemStr("kk_txn_code");

      if (empty(mKkBillType)) {
        errmsg("帳單來源 不可空白!!");
        return;
      }

      if (empty(mKkTxnCode)) {
        errmsg("交易別代碼 不可空白!!");
        return;
      }
    } else {
      mKkBillType = wp.itemStr("bill_type");
      mKkTxnCode = wp.itemStr("txn_code");
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from ptr_billtype where bill_type = ?  and  txn_code= ?";
      Object[] param = new Object[] {mKkBillType, mKkTxnCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;
    }

    // -other modify-
    sqlWhere = " where bill_type= ? and txn_code= ? " + " and nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {mKkBillType, mKkTxnCode, wp.modSeqno()};
    if (this.isOtherModify("ptr_billtype", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_billtype (" + "  bill_type " + ", inter_desc" + ", txn_code"
        + ", acct_code" + ", exter_desc" + ", cash_adv_state" + ", fees_state" + ", fees_fix_amt"
        + ", fees_percent" + ", fees_min" // 10
        + ", fees_max" + ", interest_mode" + ", adv_wkday" + ", auto_installment"
        + ", balance_state" + ", send_v_flag" + ", send_m_flag" + ", send_nccc_flag"
        + ", entry_acct" + ", chk_err_bill" // 20
        + ", double_chk" + ", format_chk" + ", block_rsn_x100" + ", sign_flag"
        + ", crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?,?,?,?,?" + ",?,?,?,? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkBillType // 1
        , wp.itemStr("inter_desc"), mKkTxnCode, wp.itemStr("acct_code"), wp.itemStr("exter_desc"),
        wp.itemStr("cash_adv_state").equals("Y") ? "Y" : "N",
        wp.itemStr("fees_state").equals("Y") ? "Y" : "N",
        wp.itemStr("fees_fix_amt").equals("") ? 0 : wp.itemStr("fees_fix_amt"),
        wp.itemStr("fees_percent").equals("") ? 0 : wp.itemStr("fees_percent"),
        wp.itemStr("fees_min").equals("") ? 0 : wp.itemStr("fees_min")// 10
        , wp.itemStr("fees_max").equals("") ? 0 : wp.itemStr("fees_max"),
        wp.itemStr("interest_mode"), wp.itemStr("adv_wkday"),
        wp.itemStr("auto_installment").equals("Y") ? "Y" : "N", "N",
        wp.itemStr("send_v_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("send_m_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("send_nccc_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("entry_acct").equals("Y") ? "Y" : "N",
        wp.itemStr("chk_err_bill").equals("Y") ? "Y" : "N"// 20
        , wp.itemStr("double_chk").equals("Y") ? "Y" : "N",
        wp.itemStr("format_chk").equals("Y") ? "Y" : "N", wp.itemStr("block_rsn_x100"),
        wp.itemStr("sign_flag").equals("-") ? "-" : "+", wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_billtype set " + "  inter_desc =?" + ", acct_code =?" + ", exter_desc =?"
        + ", cash_adv_state =?" + ", fees_state =?" + ", fees_fix_amt =?" + ", fees_percent =?"
        + ", fees_min =?" + ", fees_max =?" + ", interest_mode =?" + ", adv_wkday =?"
        + ", auto_installment =?" + ", balance_state=?" + ", send_v_flag=?" + ", send_m_flag =?"
        + ", send_nccc_flag=?" + ", entry_acct =?" + ", chk_err_bill =?" + ", double_chk =?"
        + ", format_chk =?" + ", block_rsn_x100 =?" + ", sign_flag =?"
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("inter_desc"), wp.itemStr("acct_code"),
        wp.itemStr("exter_desc"), wp.itemStr("cash_adv_state").equals("Y") ? "Y" : "N",
        wp.itemStr("fees_state").equals("Y") ? "Y" : "N",
        wp.itemStr("fees_fix_amt").equals("") ? 0 : wp.itemStr("fees_fix_amt"),
        wp.itemStr("fees_percent").equals("") ? 0 : wp.itemStr("fees_percent"),
        wp.itemStr("fees_min").equals("") ? 0 : wp.itemStr("fees_min"),
        wp.itemStr("fees_max").equals("") ? 0 : wp.itemStr("fees_max"), wp.itemStr("interest_mode"),
        wp.itemStr("adv_wkday"), wp.itemStr("auto_installment").equals("Y") ? "Y" : "N", "N",
        wp.itemStr("send_v_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("send_m_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("send_nccc_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("entry_acct").equals("Y") ? "Y" : "N",
        wp.itemStr("chk_err_bill").equals("Y") ? "Y" : "N",
        wp.itemStr("double_chk").equals("Y") ? "Y" : "N",
        wp.itemStr("format_chk").equals("Y") ? "Y" : "N", wp.itemStr("block_rsn_x100"),
        wp.itemStr("sign_flag").equals("-") ? "-" : "+", wp.loginUser, wp.itemStr("mod_pgm"),
        mKkBillType, mKkTxnCode, wp.modSeqno()};
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
    strSql = "delete ptr_billtype " + sqlWhere;
    Object[] param = new Object[] {mKkBillType, mKkTxnCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
