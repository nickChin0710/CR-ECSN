/**
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*
* */
package table;

public class CrdJcic extends BaseTable {

  public void stopBatch() {
    String sql1 = "insert into crd_jcic ("
        + "  card_no "
        + ", trans_type "
        + ", current_code "
        + ", oppost_reason "
        + ", oppost_date "
        + ", is_rc "
        + ", crt_date "
        + ", crt_user "
        // +", apr_user "
        // +", apr_date "
        + ", mod_user "
        + ", mod_time "
        + ", mod_pgm "
        + ", mod_seqno "
        + " ) values ("
        + "  :card_no"
        + ", 'C'" // :trans_type"
        + ", '3'" // :current_code"
        + ", 'J2'" // :oppost_reason"
        + ", :oppost_date"
        + ", :is_rc"
        + ", "
        + this.sqlYYmd
        + ", :crt_user"
        + ", :mod_user"
        + ","
        + this.sqlDTime
        + ", :mod_pgm"
        + ", 1 )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void sqlInsert() {
    String sql1 = "insert into crd_jcic ("
        + "  card_no "
        + ", trans_type "
        + ", current_code "
        + ", oppost_reason "
        + ", oppost_date "
        + ", is_rc "
        + ", payment_date "
        + ", risk_amt "
        + ", error_code "
        + ", to_jcic_date "
        + ", e_globe_date "
        + ", snd_acs_flag "
        + ", proc_acs_date "
        + ", acs_error_code "
        + ", kk4_note "
        + ", crt_date "
        + ", crt_user "
        + ", apr_user "
        + ", apr_date "
        + ", mod_user "
        + ", mod_time "
        + ", mod_pgm "
        + ", mod_seqno "
        + " ) values ("
        + "  :card_no"
        + ", :trans_type"
        + ", :current_code"
        + ", :oppost_reason"
        + ", :oppost_date"
        + ", :is_rc"
        + ", :payment_date"
        + ", :risk_amt"
        + ", :error_code"
        + ", :to_jcic_date"
        + ", :e_globe_date"
        + ", :snd_acs_flag"
        + ", :proc_acs_date"
        + ", :acs_error_code"
        + ", :kk4_note"
        + ", :crt_date"
        + ", :crt_user"
        + ", :apr_user"
        + ", :apr_date"
        + ", :mod_user"
        + ","
        + this.sqlDTime
        + ", :mod_pgm"
        + ", 1 )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }

  }

  public void initParms() {
    ppp("card_no", "");
    ppp("trans_type", "");
    ppp("current_code", "");
    ppp("oppost_reason", "");
    ppp("oppost_date", "");
    ppp("is_rc", "");
    ppp("payment_date", "");
    ppp("risk_amt", 0);
    ppp("error_code", "");
    ppp("to_jcic_date", "");
    ppp("e_globe_date", "");
    ppp("snd_acs_flag", "");
    ppp("proc_acs_date", "");
    ppp("acs_error_code", "");
    ppp("kk4_note", "");
    ppp("crt_date", "");
    ppp("crt_user", "");
    ppp("apr_user", "");
    ppp("apr_date", "");
    ppp("mod_user", "");
    // ppp("mod_time","");
    ppp("mod_pgm", "");
    // ppp("mod_seqno","");
  }

}
