/** 
 * Table: onbat_2ccas公用程式 V.2018-0521-JH
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
* 2019-0625:  JH    p_xxx >>acno_p_xxx
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
* 109-08-03  V1.00.01  yanghan  "将栏位ICBC_RESP_CODE->BANK_RESP_CODE ICBC_RESP_DESC->BANK_RESP_DESC*
 *  109/12/22   V1.00.02   Justin      zz chg name
 * */
package table;

public class Onbat2Ccas extends BaseTable {
  com.CommSqlStr commSqlStr = new com.CommSqlStr();


  public void block() {
    String sql1 = "insert into onbat_2ccas ("
        + " trans_type,"
        + " to_which,"
        + " dog,"
        + " proc_mode,"
        + " proc_status,"
        + " card_catalog,"
        + " acct_type,"
        + " id_p_seqno,"
        + " acno_p_seqno,"
        + " card_no,"
        + " block_code_1,"
        + " block_code_2,"
        + " block_code_3,"
        + " block_code_4,"
        + " block_code_5,"
        + " match_flag,"
        + " match_date,"
        + " debit_flag "
        + " ) values ( "
        + "  '2'" // trans_type
        + ", 2" // to_which
        + ", sysdate " // dog
        + ", 'B'" // proc_mode
        + ", 0" // proc_status
        + ", :card_indicator"
        + ", :acct_type"
        + ", :id_p_seqno"
        + ", :acno_p_seqno"
        + ", :card_no"
        + ", :block_code_1"
        + ", :block_code_2"
        + ", :block_code_3"
        + ", :block_code_4"
        + ", :block_code_5"
        + ", :match_flag"
        + ","
        + commSqlStr.sysYYmd // match_date
        + ", 'N'" // debit_flag
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void cardStop() {
    String sql1 = "insert into onbat_2ccas ("
        + " trans_type,"
        + " to_which,"
        + " dog,"
        + " proc_mode,"
        + " proc_status,"
        + " card_no,"
        + " opp_type,"
        + " opp_reason,"
        + " opp_date"
        + " ) values ("
        + " '6',"
        + " 2,"
        + " sysdate,"
        + " 'B',"
        + " 0,"
        + " :card_no,"
        + " '1',"
        + " 'Q3',"
        + commSqlStr.sysYYmd
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void idnoInvalid() {
    String sql1 = "insert into onbat_2ccas ("
        + " trans_type,    "
        + " to_which,      "
        + " dog,           "
        + " proc_mode,  "
        + " proc_status,"
        + " card_catalog,  "
        + " acct_type,  "
        + " id_p_seqno,  "
        + " acno_p_seqno,  "
        + " card_no,       "
        + " block_code_2,  "
        + " match_flag,    "
        + " match_date,    "
        + " debit_flag "
        + " ) values ("
        + "  '2'" // 2',
        + ", 2" // 2,
        + ", "
        + this.sqlDTime
        + ", 'B'" // 'B',
        + ", 0" // 0,
        + ", :card_indicator" // decode(:h_int,0,:h_acno_card_indicator,null),
        + ", :acct_type" // :h_acno_acct_type,
        + ", :id_p_seqno" // :h_acno_acct_holder_id,
        + ", :acno_p_seqno" // :h_acno_acct_key,
        + ", :card_no" // decode(:h_int,0,null,:h_card_card_no),
        + ", :block_code_2" // rtrim(:h_riid_block_reason2),
        + ", '2'" // '2',
        + ", ''" // match_date
        + ", 'N'"
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void initParms() {
    ppp("trans_type", "");
    ppp("to_which", 0);
    ppp("dog", "");
    ppp("dop", "");
    ppp("proc_mode", "");
    ppp("proc_status", 0);
    ppp("card_indicator", "");
    ppp("payment_type", "");
    ppp("acct_type", "");
    // ppp("card_hldr_id","");
    ppp("id_p_seqno", "");
    // ppp("card_acct_id","");
    ppp("acno_p_seqno", "");
    ppp("card_no", "");
    ppp("old_card_no", "");
    ppp("credit_limit", 0);
    ppp("trans_date", "");
    ppp("trans_amt", 0);
    ppp("mcc_code", "");
    ppp("iso_resp_code", "");
    ppp("bank_resp_code", "");
    ppp("bank_resp_desc", "");
    ppp("card_valid_from", "");
    ppp("card_valid_to", "");
    ppp("opp_type", "");
    ppp("opp_reason", "");
    ppp("opp_date", "");
    ppp("is_renew", "");
    ppp("is_em", "");
    ppp("is_rc", "");
    ppp("cycle_credit_date", "");
    ppp("curr_tot_lost_amt", 0);
    ppp("proc_date", "");
    ppp("card_launch_type", "");
    ppp("card_launch_date", "");
    ppp("cvc2_code", "");
    ppp("active_pin", "");
    ppp("voice_pin", "");
    ppp("auth_no", "");
    ppp("trans_code", "");
    ppp("refe_no", "");
    ppp("match_flag", "");
    ppp("match_date", "");
    ppp("tele_no", "");
    ppp("contract_no", "");
    ppp("block_code_1", "");
    ppp("block_code_2", "");
    ppp("block_code_3", "");
    ppp("block_code_4", "");
    ppp("block_code_5", "");
    ppp("ibm_receive_amt", 0);
    ppp("credit_limit_cash", 0);
    ppp("acct_no", "");
    ppp("acct_no_old", "");
    ppp("mail_branch", "");
    ppp("lost_fee_flag", "");
    ppp("debit_flag", "");
  }

  public void sqlInsert() {
    String sql1 = "insert into onbat_2ccas ("
        + ", trans_type  "
        + ", to_which    "
        + ", dog         "
        + ", dop         "
        + ", proc_mode   "
        + ", proc_status "
        + ", card_catalog"
        + ", payment_type"
        + ", acct_type   "
        + ", card_hldr_id"
        + ", id_p_seqno  "
        + ", card_acct_id"
        + ", acno_p_seqno     "
        + ", card_no     "
        + ", old_card_no "
        + ", credit_limit"
        + ", trans_date  "
        + ", trans_amt   "
        + ", mcc_code    "
        + ", iso_resp_code  "
        + ", bank_resp_code "
        + ", bank_resp_desc "
        + ", card_valid_from"
        + ", card_valid_to  "
        + ", opp_type       "
        + ", opp_reason     "
        + ", opp_date       "
        + ", is_renew       "
        + ", is_em          "
        + ", is_rc          "
        + ", cycle_credit_date"
        + ", curr_tot_lost_amt"
        + ", proc_date        "
        + ", card_launch_type "
        + ", card_launch_date "
        + ", cvc2_code    "
        + ", active_pin   "
        + ", voice_pin    "
        + ", auth_no      "
        + ", trans_code   "
        + ", refe_no      "
        + ", match_flag   "
        + ", match_date   "
        + ", tele_no      "
        + ", contract_no  "
        + ", block_code_1 "
        + ", block_code_2 "
        + ", block_code_3 "
        + ", block_code_4 "
        + ", block_code_5 "
        + ", ibm_receive_amt "
        + ", credit_limit_cash"
        + ", acct_no"
        + ", acct_no_old"
        + ", mail_branch"
        + ", lost_fee_flag"
        + ", debit_flag"
        + " ) values ("
        + pmkk(" :trans_type")
        + pmkk(", :to_which ")
        + pmkk(", :dog ")
        + pmkk(", :dop ")
        + pmkk(", :proc_mode ")
        + pmkk(", :proc_status ")
        + pmkk(", :card_indicator ")
        + pmkk(", :payment_type ")
        + pmkk(", :acct_type ")
        + pmkk(", :card_hldr_id ")
        + pmkk(", :id_p_seqno ")
        + pmkk(", :card_acct_id ")
        + pmkk(", :acno_p_seqno ")
        + pmkk(", :card_no ")
        + pmkk(", :old_card_no ")
        + pmkk(", :credit_limit ")
        + pmkk(", :trans_date ")
        + pmkk(", :trans_amt ")
        + pmkk(", :mcc_code ")
        + pmkk(", :iso_resp_code ")
        + pmkk(", :bank_resp_code ")
        + pmkk(", :bank_resp_desc ")
        + pmkk(", :card_valid_from ")
        + pmkk(", :card_valid_to ")
        + pmkk(", :opp_type ")
        + pmkk(", :opp_reason ")
        + pmkk(", :opp_date ")
        + pmkk(", :is_renew ")
        + pmkk(", :is_em ")
        + pmkk(", :is_rc ")
        + pmkk(", :cycle_credit_date ")
        + pmkk(", :curr_tot_lost_amt ")
        + pmkk(", :proc_date ")
        + pmkk(", :card_launch_type ")
        + pmkk(", :card_launch_date ")
        + pmkk(", :cvc2_code ")
        + pmkk(", :active_pin ")
        + pmkk(", :voice_pin ")
        + pmkk(", :auth_no ")
        + pmkk(", :trans_code ")
        + pmkk(", :refe_no ")
        + pmkk(", :match_flag ")
        + pmkk(", :match_date ")
        + pmkk(", :tele_no ")
        + pmkk(", :contract_no ")
        + pmkk(", :block_code_1 ")
        + pmkk(", :block_code_2 ")
        + pmkk(", :block_code_3 ")
        + pmkk(", :block_code_4 ")
        + pmkk(", :block_code_5 ")
        + pmkk(", :ibm_receive_amt ")
        + pmkk(", :credit_limit_cash ")
        + pmkk(", :acct_no ")
        + pmkk(", :acct_no_old ")
        + pmkk(", :mail_branch ")
        + pmkk(", :lost_fee_flag ")
        + pmkk(", :debit_flag ")
        + ")";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

}
