/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
******************************************************************************/
package table;

public class ActAcaj extends BaseTable {

  public void sqlInsert() {
    String sql1 = "insert into act_acaj ("
        + " p_seqno,"
        + " acct_type,"
        + " adjust_type,"
        + " reference_no,"
        + " post_date,"
        + " orginal_amt,"
        + " dr_amt,"
        + " cr_amt,"
        + " bef_amt,"
        + " aft_amt,"
        + " bef_d_amt,"
        + " aft_d_amt,"
        + " acct_code,"
        + " function_code,"
        + " card_no,"
        + " cash_type,"
        + " value_type,"
        + " trans_acct_type,"
        + " trans_acct_key,"
        + " interest_date,"
        + " adj_reason_code,"
        + " adj_comment,"
        + " c_debt_key,"
        + " debit_item,"
        + " jrnl_date,"
        + " jrnl_time,"
        + " payment_type,"
        + " batch_no_new,"
        + " process_flag,"
        + " job_code,"
        + " vouch_job_code,"
        + " mcht_no,"
        + " curr_code,"
        + " dc_orginal_amt,"
        + " dc_dr_amt,"
        + " dc_cr_amt,"
        + " dc_bef_amt,"
        + " dc_aft_amt,"
        + " dc_bef_d_amt,"
        + " dc_aft_d_amt,"
        + " crt_date,"
        + " crt_time,"
        + " crt_user,"
        + " update_date,"
        + " update_user,"
        + " apr_flag,"
        + " apr_date,"
        + " apr_user,"
        + " rsk_ctrl_seqno,"
        + " mod_user, mod_time, mod_pgm, mod_seqno"
        + " ) values ("
        + " :p_seqno,"
        + " :acct_type,"
        + " :adjust_type,"
        + " :reference_no,"
        + " :post_date,"
        + " :orginal_amt,"
        + " :dr_amt,"
        + " :cr_amt,"
        + " :bef_amt,"
        + " :aft_amt,"
        + " :bef_d_amt,"
        + " :aft_d_amt,"
        + " :acct_code,"
        + " :function_code,"
        + " :card_no,"
        + " :cash_type,"
        + " :value_type,"
        + " :trans_acct_type,"
        + " :trans_acct_key,"
        + " :interest_date,"
        + " :adj_reason_code,"
        + " :adj_comment,"
        + " :c_debt_key,"
        + " :debit_item,"
        + " :jrnl_date,"
        + " :jrnl_time,"
        + " :payment_type,"
        + " :batch_no_new,"
        + " :process_flag,"
        + " :job_code,"
        + " :vouch_job_code,"
        + " :mcht_no,"
        + " :curr_code,"
        + " :dc_orginal_amt,"
        + " :dc_dr_amt,"
        + " :dc_cr_amt,"
        + " :dc_bef_amt,"
        + " :dc_aft_amt,"
        + " :dc_bef_d_amt,"
        + " :dc_aft_d_amt,"
        + " :crt_date,"
        + " :crt_time,"
        + " :crt_user,"
        + " :update_date,"
        + " :update_user,"
        + " :apr_flag,"
        + " :apr_date,"
        + " :apr_user,"
        + " :rsk_ctrl_seqno,"
        + " :mod_user, sysdate, :mod_pgm, 1"
        + " )";

    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void initParms() {
    ppp("p_seqno", "");
    ppp("acct_type", "");
    ppp("adjust_type", "");
    ppp("reference_no", "");
    ppp("post_date", "");
    ppp("orginal_amt", 0);
    ppp("dr_amt", 0);
    ppp("cr_amt", 0);
    ppp("bef_amt", 0);
    ppp("aft_amt", 0);
    ppp("bef_d_amt", 0);
    ppp("aft_d_amt", 0);
    ppp("acct_code", "");
    ppp("function_code", "");
    ppp("card_no", "");
    ppp("cash_type", "");
    ppp("value_type", "");
    ppp("trans_acct_type", "");
    ppp("trans_acct_key", "");
    ppp("interest_date", "");
    ppp("adj_reason_code", "");
    ppp("adj_comment", "");
    ppp("c_debt_key", "");
    ppp("debit_item", "");
    ppp("jrnl_date", "");
    ppp("jrnl_time", "");
    ppp("payment_type", "");
    ppp("batch_no_new", "");
    ppp("process_flag", "");
    ppp("job_code", "");
    ppp("vouch_job_code", "");
    ppp("mcht_no", "");
    ppp("curr_code", "");
    ppp("dc_orginal_amt", 0);
    ppp("dc_dr_amt", 0);
    ppp("dc_cr_amt", 0);
    ppp("dc_bef_amt", 0);
    ppp("dc_aft_amt", 0);
    ppp("dc_bef_d_amt", 0);
    ppp("dc_aft_d_amt", 0);
    ppp("crt_date", "");
    ppp("crt_time", "");
    ppp("crt_user", "");
    ppp("update_date", "");
    ppp("update_user", "");
    ppp("apr_flag", "");
    ppp("apr_date", "");
    ppp("apr_user", "");
    ppp("rsk_ctrl_seqno", "");
    ppp("mod_user", "");
    // ppp("mod_time","");
    ppp("mod_pgm", "");
    // ppp("mod_seqno",0);
  }

}
