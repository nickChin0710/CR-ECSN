/** 
 * rsk_acnolog 公用程式 V.2018-1129.JH
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
* 2019-0625:  JH    p_xxx >>acno_pxxx
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
 * 
 * */
package table;

public class RskAcnolog extends table.BaseTable {

  public void acnoLimit() {
    String sql1 = "insert into rsk_acnolog ("
        + " kind_flag,"
        + " acno_p_seqno,"
        + " acct_type,"
        + " id_p_seqno,"
        + " corp_p_seqno,"
        + " log_date,"
        + " log_mode,"
        + " log_type,"
        + " log_reason,"
        + " bef_loc_amt,"
        + " aft_loc_amt,"
        + " adj_loc_flag,"
        + " fit_cond,"
        + " security_amt,"
        + " apr_flag,"
        + " apr_user,"
        + " apr_date,"
        + " emend_type,"
        + " fh_flag,"
        + " bef_loc_cash,"
        + " aft_loc_cash,"
        + " mod_time, mod_user, mod_pgm, mod_seqno"
        + " ) values ("
        + " 'A',"
        + " :p_seqno,"
        + " :acct_type,"
        + " :id_p_seqno,"
        + " :corp_p_seqno,"
        + " :log_date,"
        + " '2'," // log_mode
        + " '1'," // log_type
        + " :log_reason,"
        + " :bef_loc_amt,"
        + " :aft_loc_amt,"
        + " '2'," // adj_loc_flag
        + " 'Y',"
        + " 0,"
        + " 'Y'," // apr_flag
        + " :apr_user,"
        + " :apr_date,"
        + " '1'," // emend_type
        + " :fh_flag,"
        + " :bef_loc_cash,"
        + " :aft_loc_cash,"
        + " sysdate, :mod_user, :mod_pgm, 1"
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void acnoStop() {
    String sql1 = "insert into rsk_acnolog ("
        + " kind_flag ,"
        + " card_no,"
        + " acct_type ,"
        + " acno_p_seqno ,"
        + " id_p_seqno ,"
        + " corp_p_seqno ,"
        + " log_date ,"
        + " log_mode ,"
        + " log_type ,"
        + " log_reason ,"
        + " log_not_reason ,"
        + " fit_cond ,"
        + " mod_user, mod_time, mod_pgm, mod_seqno "
        + " ) values ("
        + " :kind_flag ,"
        + " :card_no,"
        + " :acct_type,"
        + " :p_seqno, "
        + " :id_p_seqno, "
        + " :corp_p_seqno, "
        + " :log_date, "
        + " '2' ," // log_mode
        + " '2' ," //
        + " :log_reason ,"
        + " :log_not_reason ,"
        + " 'N',  "
        + " :mod_user, sysdate, :mod_pgm, 1 "
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void unBlock() {
    String sql1 = "insert into rsk_acnolog ("
        + "  kind_flag"
        + ", card_no"
        + ", acno_p_seqno"
        + ", acct_type"
        + ", id_p_seqno"
        + ", corp_p_seqno"
        + ", log_date"
        + ", log_mode"
        + ", log_type"
        + ", log_reason"
        + ", log_not_reason"
        + ", log_remark"
        + ", block_reason"
        + ", block_reason2"
        + ", block_reason3"
        + ", block_reason4"
        + ", block_reason5"
        + ", spec_status"
        + ", spec_del_date"
        + ", fit_cond"
        + ", relate_code"
        + ", rela_p_seqno"
        + ", mod_user, mod_time, mod_pgm, mod_seqno"
        + " ) values ("
        + " :kind_flag"
        + ", :card_no"
        + ", :p_seqno"
        + ", :acct_type"
        + ", :id_p_seqno"
        + ", :corp_p_seqno"
        + ", :log_date"
        + ", '2'" // log_mode"
        + ", '4'" // :log_type:解凍
        + ", :log_reason"
        + ", :log_not_reason"
        + ", :log_remark"
        + ", :block_reason1"
        + ", :block_reason2"
        + ", :block_reason3"
        + ", :block_reason4"
        + ", :block_reason5"
        + ", :spec_status"
        + ", :spec_del_date"
        + ", 'N'" // fit_cond
        + ", :relate_code"
        + ", :rela_p_seqno"
        + ", :mod_user, sysdate, :mod_pgm, 1"
        + ")";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void block() {
    String sql1 = "insert into rsk_acnolog ("
        + "  kind_flag "
        + ", card_no "
        + ", acct_type "
        + ", acno_p_seqno "
        + ", id_p_seqno "
        + ", corp_p_seqno "
        + ", log_date "
        + ", log_mode "
        + ", log_type "
        + ", log_reason "
        + ", log_not_reason "
        + ", fit_cond "
        + ", log_remark "
        + ", block_reason "
        + ", block_reason2 "
        + ", block_reason3 "
        + ", block_reason4 "
        + ", block_reason5 "
        + ", spec_status "
        + ", spec_del_date "
        + ", relate_code "
        + ", rela_p_seqno "
        + ", from_seqno "
        + ", apr_flag, apr_user, apr_date "
        + ", mod_user, mod_time, mod_pgm, mod_seqno "
        + " ) values ("
        + "  :kind_flag "
        + ", :card_no "
        + ", :acct_type "
        + ", :p_seqno "
        + ", :id_p_seqno "
        + ", :corp_p_seqno "
        + ", :log_date "
        + ", :log_mode " // :log_mode
        + ", '3' " // log_type:3.凍結
        + ", :log_reason "
        + ", :log_not_reason "
        + ", 'ECS' " // fit_cond
        + ", :log_remark " // log_remark
        + ", :block_reason1"
        + ", :block_reason2 "
        + ", :block_reason3 "
        + ", :block_reason4 "
        + ", :block_reason5 "
        + ", :spec_status "
        + ", :spec_del_date "
        + ", :relate_code "
        + ", :rela_p_seqno "
        + ", :from_seqno "
        + ", 'Y' " // apr_flag "
        + ", 'SYSTEM' " // apr_user "
        + ", "
        + this.sqlYYmd // :apr_date "
        + ", :mod_user "
        + ", "
        + this.sqlDTime
        + ", :mod_pgm "
        + ", 1"
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void acnoBlock() {
    String sql1 = "insert into rsk_acnolog ("
        + "  kind_flag         "
        + ", card_no           "
        + ", acct_type         "
        + ", acno_p_seqno "
        + ", id_p_seqno        "
        + ", corp_p_seqno      "
        + ", log_date          "
        + ", log_mode          "
        + ", log_type          "
        + ", log_reason        "
        + ", log_not_reason    "
        + ", fit_cond          "
        + ", log_remark        "
        + ", block_reason      "
        + ", block_reason2     "
        + ", block_reason3     "
        + ", block_reason4     "
        + ", block_reason5     "
        + ", spec_status       "
        + ", spec_del_date"
        + ", relate_code       "
        + ", rela_p_seqno      "
        + ", from_seqno        "
        + ", apr_flag          "
        + ", apr_user          "
        + ", apr_date          "
        + ", mod_user          "
        + ", mod_time          "
        + ", mod_pgm           "
        + ", mod_seqno         "
        + " ) values ("
        + "  'A' "
        + ", ''"
        + ", :acct_type         "
        + ", :p_seqno           "
        + ", :id_p_seqno        "
        + ", :corp_p_seqno      "
        + ", :log_date          "
        + ", '2' " // log_mode:BATCH
        + ", '3' " // log_type:凍結
        + ", '' " // log_reason
        + ", '' " // log_not_reason
        + ", 'ECS' " // fit_cond
        + ", :log_remark " // log_remark
        + ", :block_reason1"
        + ", :block_reason2     "
        + ", :block_reason3     "
        + ", :block_reason4     "
        + ", :block_reason5 "
        + ", :spec_status "
        + ", :spec_del_date "
        + ", '' " // relate_code "
        + ", '' " // :rela_p_seqno "
        + ", :from_seqno " // :from_seqno "
        + ", 'Y' " // apr_flag "
        + ", 'SYSTEM' " // apr_user "
        + ", "
        + this.sqlYYmd // :apr_date "
        + ", :mod_user   "
        + ", "
        + this.sqlDTime
        + ", :mod_pgm    "
        + ", 1"
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void convInsert() {
    sqlInsert();
    initParms();
    if (nameSqlParm(sqlFrom)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void sqlInsert() {
    sqlFrom = "insert into rsk_acnolog ("
        + "  kind_flag"
        + ", card_no"
        + ", acno_p_seqno"
        + ", acct_type"
        + ", id_p_seqno"
        + ", corp_p_seqno"
        + ", param_no"
        + ", log_date"
        + ", log_mode"
        + ", log_type"
        + ", textfile_date"
        + ", log_reason"
        + ", log_not_reason"
        + ", bef_loc_amt"
        + ", aft_loc_amt"
        + ", adj_loc_flag"
        + ", fit_cond"
        + ", print_comp_yn"
        + ", mail_comp_yn"
        + ", security_amt"
        + ", log_remark"
        + ", block_reason"
        + ", block_reason2"
        + ", block_reason3"
        + ", block_reason4"
        + ", block_reason5"
        + ", spec_status"
        + ", spec_del_date"
        + ", bill_print"
        + ", upgrade_amt_white"
        + ", emend_type"
        + ", fh_flag"
        + ", bef_loc_cash"
        + ", aft_loc_cash"
        + ", send_ibm_flag"
        + ", send_ibm_date"
        + ", relate_code"
        + ", rela_p_seqno"
        + ", from_seqno"
        + ", class_code_bef"
        + ", class_code_aft"
        + ", class_valid_date"
        + ", ccas_mcode_bef"
        + ", ccas_mcode_aft"
        + ", mcode_valid_date"
        + ", sms_flag"
        + ", apr_flag"
        + ", apr_user"
        + ", apr_date"
        + ", mod_user"
        + ", mod_time"
        + ", mod_pgm"
        + ", mod_seqno"
        + " ) values ("
        + " :kind_flag"
        + ", :card_no"
        + ", :p_seqno"
        + ", :acct_type"
        + ", :id_p_seqno"
        + ", :corp_p_seqno"
        + ", :param_no"
        + ", :log_date"
        + ", :log_mode"
        + ", :log_type"
        + ", :textfile_date"
        + ", :log_reason"
        + ", :log_not_reason"
        + ", :bef_loc_amt"
        + ", :aft_loc_amt"
        + ", :adj_loc_flag"
        + ", :fit_cond"
        + ", :print_comp_yn"
        + ", :mail_comp_yn"
        + ", :security_amt"
        + ", :log_remark"
        + ", :block_reason1"
        + ", :block_reason2"
        + ", :block_reason3"
        + ", :block_reason4"
        + ", :block_reason5"
        + ", :spec_status"
        + ", :spec_del_date"
        + ", :bill_print"
        + ", :upgrade_amt_white"
        + ", :emend_type"
        + ", :fh_flag"
        + ", :bef_loc_cash"
        + ", :aft_loc_cash"
        + ", :send_ibm_flag"
        + ", :send_ibm_date"
        + ", :relate_code"
        + ", :rela_p_seqno"
        + ", :from_seqno"
        + ", :class_code_bef"
        + ", :class_code_aft"
        + ", :class_valid_date"
        + ", :ccas_mcode_bef"
        + ", :ccas_mcode_aft"
        + ", :mcode_valid_date"
        + ", :sms_flag"
        + ", :apr_flag"
        + ", :apr_user"
        + ", :apr_date"
        + ", :mod_user"
        + ", sysdate"
        + ", :mod_pgm"
        + ", 1"
        + " )";

    // parmKey_sort();
  }

  public void initParms() {
    // this.parmValue_clear();
    ppp("kind_flag", "");
    ppp("card_no", "");
    ppp("p_seqno", "");
    ppp("acct_type", "");
    ppp("id_p_seqno", "");
    ppp("corp_p_seqno", "");
    ppp("param_no", "");
    ppp("log_date", "");
    ppp("log_mode", "");
    ppp("log_type", "");
    ppp("textfile_date", "");
    ppp("log_reason", "");
    ppp("log_not_reason", "");
    ppp("bef_loc_amt", 0);
    ppp("aft_loc_amt", 0);
    ppp("adj_loc_flag", "");
    ppp("fit_cond", "");
    ppp("print_comp_yn", "N");
    ppp("mail_comp_yn", "N");
    ppp("security_amt", 0);
    ppp("log_remark", "");
    ppp("block_reason1", "");
    ppp("block_reason2", "");
    ppp("block_reason3", "");
    ppp("block_reason4", "");
    ppp("block_reason5", "");
    ppp("spec_status", "");
    ppp("spec_del_date", "");
    ppp("bill_print", "");
    ppp("upgrade_amt_white", 0);
    ppp("emend_type", "");
    ppp("fh_flag", "");
    ppp("bef_loc_cash", 0);
    ppp("aft_loc_cash", 0);
    ppp("send_ibm_flag", "N");
    ppp("send_ibm_date", "");
    ppp("relate_code", "");
    ppp("rela_p_seqno", "");
    ppp("from_seqno", "");
    ppp("class_code_bef", "");
    ppp("class_code_aft", "");
    ppp("class_valid_date", "");
    ppp("ccas_mcode_bef", "");
    ppp("ccas_mcode_aft", "");
    ppp("mcode_valid_date", "");
    ppp("sms_flag", "");
    ppp("apr_flag", "N");
    ppp("apr_user", "");
    ppp("apr_date", "");
    ppp("mod_user", "");
    // ppp("mod_time","");
    ppp("mod_pgm", "");
    // ppp("mod_seqno","");
    ppp("rowid", "");
  }

}
