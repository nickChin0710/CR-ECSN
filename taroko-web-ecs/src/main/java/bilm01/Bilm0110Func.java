/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-07  V1.00.00  yash       program initial                            *
* 108-06-13  V1.00.03  Andy		  update : p_seqno ==> acno_p_seqno          *
* 109-04-23  V1.00.04  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0110Func extends FuncEdit {
  String kk1 = "";
  String kk2 = "";
  String lsIdPSeqno = "", lsPSeqno = "", lsGpNo = "";

  public Bilm0110Func(TarokoCommon wr) {
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

    if (this.isDelete()) {
      if (!wp.itemStr("forced_post_flag").equals("Y") && empty(wp.itemStr("apr_date")) == false) {
        errmsg("已放行, 無法刪除 1 !");
        return;
      }
      if (!wp.itemStr("forced_post_flag").equals("Y") && wp.itemStr("apr_flag").equals("Y")) {
        errmsg("已放行, 無法刪除 2 !");
        return;
      }
    }


    String lsSql1 = "select loan_flag,trans_flag from bil_merchant where mcht_no = :mcht_no";
    setString("mcht_no", wp.itemStr("mcht_no"));
    sqlSelect(lsSql1);
    if (colStr("loan_flag").equals("Y") && wp.itemNum("qty") > 1) {
      errmsg("貸款旗標Y 數量不可大於 1 !!");
      return;
    }
    if (wp.itemStr("acct_type").equals("05") && wp.itemNum("qty") > 1) {
      errmsg("05帳戶 數量不可大於 1 !!");
      return;
    }
    if (colStr("trans_flag").equals("Y") && wp.itemNum("qty") > 1) {
      errmsg("長期循環轉分期 數量不可大於 1 !!");
      return;
    }
    if (wp.itemNum("unit_price") < 0) {
      errmsg("每期金額1 小於 0  !!");
      return;
    }
    if (wp.itemStr("forced_post_flag").equals("Y") && empty(wp.itemStr("apr_date")) == true) {
      wp.colSet("conf_mesg", "Y");
    }

    if (this.isUpdate()) {
      if (!wp.itemStr("forced_post_flag").equals("Y") && empty(wp.itemStr("apr_date")) == false) {
        errmsg("已放行, 無法修改 1 !");
        return;
      }
      if (!wp.itemStr("forced_post_flag").equals("Y") && wp.itemStr("apr_flag").equals("Y")) {
        errmsg("已放行, 無法修改 2 !");
        return;
      }
    }

    String lsSql2 = "select id_p_seqno,acno_p_seqno,p_seqno from crd_card where card_no =:card_no ";
    setString("card_no", wp.itemStr("card_no"));
    sqlSelect(lsSql2);
    lsIdPSeqno = colStr("id_p_seqno");
    lsPSeqno = colStr("acno_p_seqno");
    lsGpNo = colStr("p_seqno");

    if (this.ibAdd) {
      kk1 = varsStr("aa_contract_no");
      kk2 = varsStr("aa_contract_seq_no");

    } else {
      kk1 = wp.itemStr("contract_no");
      kk2 = wp.itemStr("contract_seq_no");
    }
    if (this.isAdd()) {

      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from bil_contract where contract_no = ?and contract_seq_no=?";
      Object[] param = new Object[] {kk1, kk2};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }


      return;
    }
    // -other modify-
    sqlWhere = " where contract_no = ? and contract_seq_no=? and nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {kk1, kk2, wp.modSeqno()};
    isOtherModify("bil_contract", sqlWhere, param);
    if (this.isOtherModify("bil_contract", sqlWhere, param)) {
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

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_contract");
    sp.ppstr("contract_no", kk1);
    sp.ppstr("contract_seq_no", kk2);
    sp.ppstr("contract_kind", wp.itemStr("contract_kind"));
    sp.ppstr("redeem_kind", wp.itemStr("redeem_kind"));
    sp.ppstr("allocate_flag", wp.itemStr("allocate_flag"));
    sp.ppstr("mcht_no", wp.itemStr("mcht_no"));
    sp.ppstr("mcht_chi_name", wp.itemStr("mcht_chi_name"));
    sp.ppstr("product_no", wp.itemStr("product_no"));
    sp.ppstr("product_name", wp.itemStr("product_name"));
    sp.ppstr("cvv2", wp.itemStr("cvv2"));
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("limit_end_date", wp.itemStr("limit_end_date"));
    sp.ppstr("vip_code", wp.itemStr("vip_code"));
    sp.ppstr("first_post_date", wp.itemStr("first_post_date"));
    sp.ppnum("post_cycle_dd", wp.itemNum("post_cycle_dd"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
    sp.ppnum("install_curr_term", wp.itemNum("install_curr_term"));
    sp.ppstr("all_post_flag", wp.itemStr("all_post_flag"));
    sp.ppstr("forced_post_flag", wp.itemStr("forced_post_flag"));
    sp.ppstr("fee_flag", wp.itemStr("fee_flag"));
    sp.ppstr("reference_no", wp.itemStr("reference_no"));
    sp.ppstr("payment_type", wp.itemStr("payment_type"));
    sp.ppstr("batch_no", wp.itemStr("batch_no"));
    sp.ppstr("cps_flag", wp.itemStr("cps_flag"));
    sp.ppstr("film_no", wp.itemStr("film_no"));
    sp.ppstr("auth_code", varsStr("auth_code"));
    sp.ppstr("ccas_resp_code", varsStr("ccas_resp_code"));
    sp.ppnum("tot_amt", wp.itemNum("tot_amt"));
    sp.ppnum("qty", wp.itemNum("qty"));
    sp.ppnum("exchange_amt", wp.itemNum("exchange_amt"));
    sp.ppnum("install_tot_term", wp.itemNum("install_tot_term"));
    sp.ppnum("unit_price", wp.itemNum("unit_price"));
    sp.ppnum("redeem_amt", wp.itemNum("redeem_amt"));
    sp.ppnum("redeem_point", wp.itemNum("redeem_point"));
    sp.ppnum("first_remd_amt", wp.itemNum("first_remd_amt"));
    sp.ppnum("remd_amt", wp.itemNum("remd_amt"));
    sp.ppstr("auto_delv_flag", wp.itemStr("auto_delv_flag"));
    sp.ppnum("extra_fees", wp.itemNum("extra_fees"));
    sp.ppnum("fees_fix_amt", wp.itemNum("fees_fix_amt"));
    sp.ppnum("fees_rate", wp.itemNum("fees_rate"));
    sp.ppnum("clt_fees_amt", wp.itemNum("clt_fees_amt"));
    sp.ppnum("clt_unit_price", wp.itemNum("clt_unit_price"));
    sp.ppnum("clt_install_tot_term", wp.itemNum("clt_install_tot_term"));
    sp.ppnum("clt_remd_amt", wp.itemNum("clt_remd_amt"));
    sp.ppstr("zip_code", wp.itemStr("zip_code"));
    sp.ppstr("receive_address", wp.itemStr("receive_address"));
    sp.ppstr("receive_name", wp.itemStr("receive_name"));
    sp.ppstr("receive_tel", wp.itemStr("receive_tel"));
    sp.ppstr("receive_tel1", wp.itemStr("receive_tel1"));
    sp.ppstr("voucher_head", wp.itemStr("voucher_head"));
    sp.ppstr("uniform_no", wp.itemStr("uniform_no"));
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_date", "");
    sp.ppstr("delv_date", wp.itemStr("delv_date"));
    sp.ppstr("delv_batch_no", wp.itemStr("delv_batch_no"));
    sp.ppstr("register_no", wp.itemStr("register_no"));
    sp.ppstr("delv_confirm_flag", wp.itemStr("delv_confirm_flag"));
    sp.ppstr("delv_confirm_date", wp.itemStr("delv_confirm_date"));
    sp.ppstr("refund_flag", wp.itemStr("refund_flag"));
    sp.ppstr("refund_apr_date", wp.itemStr("refund_apr_date"));
    sp.ppnum("refund_qty", wp.itemNum("refund_qty"));
    sp.ppnum("install_back_term", wp.itemNum("install_back_term"));
    sp.ppstr("install_back_term_flag", wp.itemStr("install_back_term_flag"));
    sp.ppstr("back_card_no", wp.itemStr("back_card_no"));
    sp.ppstr("installment_kind", "N");
    sp.ppstr("new_it_flag", "Y");
    sp.ppstr("id_p_seqno", lsIdPSeqno);
    sp.ppstr("acno_p_seqno", lsPSeqno);
    sp.ppstr("p_seqno", lsGpNo);
    sp.ppstr("sale_emp_no", wp.itemStr("sale_emp_no"));
    sp.addsql(", mod_time ", ", sysdate");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);

    sqlExec(sp.sqlStmt(), sp.sqlParm());

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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("bil_contract");
    sp.ppstr("contract_kind", wp.itemStr("contract_kind"));
    sp.ppstr("redeem_kind", wp.itemStr("redeem_kind"));
    sp.ppstr("allocate_flag", wp.itemStr("allocate_flag"));
    sp.ppstr("mcht_no", wp.itemStr("mcht_no"));
    sp.ppstr("mcht_chi_name", wp.itemStr("mcht_chi_name"));
    sp.ppstr("product_no", wp.itemStr("product_no"));
    sp.ppstr("product_name", wp.itemStr("product_name"));
    sp.ppstr("cvv2", wp.itemStr("cvv2"));
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("limit_end_date", wp.itemStr("limit_end_date"));
    sp.ppstr("vip_code", wp.itemStr("vip_code"));
    sp.ppstr("first_post_date", wp.itemStr("first_post_date"));
    sp.ppnum("post_cycle_dd", wp.itemNum("post_cycle_dd"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
    sp.ppnum("install_curr_term", wp.itemNum("install_curr_term"));
    sp.ppstr("forced_post_flag", wp.itemStr("forced_post_flag"));
    sp.ppstr("fee_flag", wp.itemStr("fee_flag"));
    sp.ppstr("reference_no", wp.itemStr("reference_no"));
    sp.ppstr("payment_type", wp.itemStr("payment_type"));
    sp.ppnum("tot_amt", wp.itemNum("tot_amt"));
    sp.ppnum("qty", wp.itemNum("qty"));
    sp.ppnum("exchange_amt", wp.itemNum("exchange_amt"));
    sp.ppnum("install_tot_term", wp.itemNum("install_tot_term"));
    sp.ppnum("unit_price", wp.itemNum("unit_price"));
    sp.ppnum("redeem_amt", wp.itemNum("redeem_amt"));
    sp.ppnum("redeem_point", wp.itemNum("redeem_point"));
    sp.ppnum("first_remd_amt", wp.itemNum("first_remd_amt"));
    sp.ppnum("remd_amt", wp.itemNum("remd_amt"));
    sp.ppnum("extra_fees", wp.itemNum("extra_fees"));
    sp.ppnum("fees_fix_amt", wp.itemNum("fees_fix_amt"));
    sp.ppnum("fees_rate", wp.itemNum("fees_rate"));
    sp.ppnum("clt_fees_amt", wp.itemNum("clt_fees_amt"));
    sp.ppnum("clt_unit_price", wp.itemNum("clt_unit_price"));
    sp.ppnum("clt_install_tot_term", wp.itemNum("clt_install_tot_term"));
    sp.ppnum("clt_remd_amt", wp.itemNum("clt_remd_amt"));
    sp.ppstr("zip_code", wp.itemStr("zip_code"));
    sp.ppstr("receive_address", wp.itemStr("receive_address"));
    sp.ppstr("receive_name", wp.itemStr("receive_name"));
    sp.ppstr("receive_tel", wp.itemStr("receive_tel"));
    sp.ppstr("receive_tel1", wp.itemStr("receive_tel1"));
    sp.ppstr("voucher_head", wp.itemStr("voucher_head"));
    sp.ppstr("auth_code", varsStr("auth_code"));
    sp.ppstr("ccas_resp_code", varsStr("ccas_resp_code"));
    sp.ppstr("uniform_no", wp.itemStr("uniform_no"));
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_date", "");
    sp.ppstr("delv_date", wp.itemStr("delv_date"));
    sp.ppstr("delv_batch_no", wp.itemStr("delv_batch_no"));
    sp.ppstr("register_no", wp.itemStr("register_no"));
    sp.ppstr("delv_confirm_flag", wp.itemStr("delv_confirm_flag"));
    sp.ppstr("delv_confirm_date", wp.itemStr("delv_confirm_date"));
    sp.ppstr("refund_flag", wp.itemStr("refund_flag"));
    sp.ppstr("refund_apr_date", wp.itemStr("refund_apr_date"));
    sp.ppnum("refund_qty", wp.itemNum("refund_qty"));
    sp.ppnum("install_back_term", wp.itemNum("install_back_term"));
    sp.ppstr("install_back_term_flag", wp.itemStr("install_back_term_flag"));
    sp.ppstr("back_card_no", wp.itemStr("back_card_no"));
    sp.ppstr("id_p_seqno", lsIdPSeqno);
    sp.ppstr("acno_p_seqno", lsPSeqno);
    sp.ppstr("p_seqno", lsGpNo);
    sp.ppstr("sale_emp_no", wp.itemStr("sale_emp_no"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate  ");
    sp.sql2Where("where contract_no=?", kk1);
    sp.sql2Where("and contract_seq_no=?", kk2);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
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
    strSql = "delete bil_contract " + sqlWhere;
    Object[] param = new Object[] {kk1, kk2, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
