/**
 * 授權outgoing公用程式 V.2018-1128
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  2018-1128:  JH    initial
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*
* */

package table;

public class CcaOutgoing extends table.BaseTable {

  public void cardBlock() {
    String sql1 = "insert into cca_outgoing ("
        + "  crt_date, crt_time  "
        + ", card_no   "
        + ", key_value, key_table "
        // +", bitmap "
        + ", proc_flag " // N
        // +", send_times"
        + ", act_code  " // A,U
        + ", data_from " // 2
        // +", resp_code "
        + ", data_type "
        + ", bin_type  "
        // +", reason_code "
        // +", del_date "
        + ", bank_acct_no "
        // +", vmj_regn_data "
        // +", vip_amt "
        + ", block_code "
        + ", spec_status "
        + ", crt_user "
        // +", proc_date, proc_time, proc_user"
        + ", mod_time, mod_pgm"
        + " ) values ("
        + " to_char(sysdate,'yyyymmdd')"
        + ", to_char(sysdate,'hh24miss')"
        + ", :card_no "
        + ", 'NCCC' "
        + ", 'CARD_BASE_SPEC' "
        // +", bitmap "
        + ", 'N'" // :proc_flag "
        // +", send_times "
        + ", :act_code "
        + ", '2'" // data_from "
        // +", resp_code "
        + ", 'BLOCK'" // data_type "
        + ", :bin_type "
        // +", reason_code"
        // +", del_date "
        + ", :bank_acct_no "
        // +", vmj_regn_data"
        // +", vip_amt "
        + ", :block_code, :spec_status"
        + ", :crt_user  "
        // +", proc_date "
        // +", proc_time "
        // +", proc_user "
        + ", sysdate, :mod_pgm "
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void cardStop() {
    String sql1 = "insert into cca_outgoing ("
        + "  crt_date, crt_time  "
        + ", card_no   "
        + ", key_value, key_table "
        // +", bitmap "
        + ", proc_flag " // N
        // +", send_times"
        + ", act_code  " // A,U
        + ", data_from " // 2
        // +", resp_code "
        + ", data_type "
        + ", bin_type  "
        // +", reason_code "
        + ", del_date "
        + ", bank_acct_no "
        // +", vmj_regn_data "
        // +", vip_amt "
        + ", block_code "
        + ", spec_status "
        + ", crt_user "
        // +", proc_date, proc_time, proc_user"
        + ", mod_time, mod_pgm"
        + " ) values ("
        + " to_char(sysdate,'yyyymmdd')"
        + ", to_char(sysdate,'hh24miss')"
        + ", :card_no "
        + ", 'NCCC' "
        + ", 'OPPOSITION' "
        // +", bitmap "
        + ", 'N'" // :proc_flag "
        // +", send_times "
        + ", :act_code "
        + ", '2'" // data_from "
        // +", resp_code "
        + ", 'OPPO'" // data_type "
        + ", :bin_type "
        // +", reason_code"
        + ", del_date   "
        + ", :bank_acct_no "
        // +", vmj_regn_data"
        // +", vip_amt "
        + ", :block_code, :spec_status"
        + ", :crt_user  "
        // +", proc_date "
        // +", proc_time "
        // +", proc_user "
        + ", sysdate, :mod_pgm "
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }


  public void initParms() {
    ppp("crt_date     ", "");
    ppp("crt_time     ", "");
    ppp("card_no      ", "");
    ppp("key_value    ", "");
    ppp("key_table    ", "");
    ppp("bitmap       ", "");
    ppp("proc_flag    ", "");
    ppp("send_times   ", 0);
    ppp("act_code     ", "");
    ppp("data_from    ", "");
    ppp("resp_code    ", "");
    ppp("data_type    ", "");
    ppp("bin_type     ", "");
    ppp("reason_code  ", "");
    ppp("del_date     ", "");
    ppp("bank_acct_no ", "");
    ppp("vmj_regn_data", "");
    ppp("vip_amt      ", 0);
    ppp("block_code   ", "");
    ppp("spec_status  ", "");
    ppp("crt_user     ", "");
    ppp("proc_date    ", "");
    ppp("proc_time    ", "");
    ppp("proc_user    ", "");
    ppp("mod_time     ", "");
    ppp("mod_pgm      ", "");

    ppp("rowid", "");
  }

}
