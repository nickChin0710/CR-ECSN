/**
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*
* */
package table;

public class GenVouch extends BaseTable {

  public void sqlInsert() {
    String sql1 = "insert into gen_vouch ("
        + "  brno"
        + ", tx_date"
        + ", dept"
        + ", depno"
        + ", curr"
        + ", refno"
        + ", seqno"
        + ", voucher_cnt"
        + ", ac_no"
        + ", dbcr"
        + ", sign_flag"
        + ", amt"
        + ", crt_user"
        + ", apr_user"
        + ", id_no"
        + ", memo1"
        + ", memo2"
        + ", memo3"
        + ", key_value"
        + ", sys_rem"
        + ", jrn_status"
        + ", post_flag"
        + ", ifrs_flag"
        + ", curr_code_dc"
        + ", mod_user"
        + ", mod_time"
        + ", mod_pgm"
        + ", mod_seqno"
        + " ) values ( "
        + "  :brno "
        + ", :tx_date "
        + ", :dept "
        + ", :depno "
        + ", :curr "
        + ", :refno "
        + ", :seqno "
        + ", :voucher_cnt "
        + ", :ac_no "
        + ", :dbcr "
        + ", :sign_flag "
        + ", :amt "
        + ", :crt_user "
        + ", :apr_user "
        + ", :id_no "
        + ", :memo1 "
        + ", :memo2 "
        + ", :memo3 "
        + ", :key_value "
        + ", :sys_rem "
        + ", :jrn_status "
        + ", :post_flag "
        + ", :ifrs_flag "
        + ", :curr_code_dc "
        + ", :mod_user "
        + ", sysdate "
        + ", :mod_pgm  "
        + ", 1 "
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void initParms() {
    this.parmValueClear();
    ppp("brno        ", "009"); // 分行別
    ppp("tx_date     ", ""); // 異動日期
    ppp("dept        ", ""); // 業務別
    ppp("depno       ", ""); // 部門別
    ppp("curr        ", ""); // 幣別代號
    ppp("refno       ", ""); // 套號
    ppp("seqno       ", 0); // 序號
    ppp("voucher_cnt ", 0); // 筆數
    ppp("ac_no       ", ""); // 科子細目
    ppp("dbcr        ", ""); // 借/貸方
    ppp("sign_flag   ", ""); // 正負號
    ppp("amt         ", 0.00); // 金額
    ppp("crt_user    ", ""); // 經辦人員
    ppp("apr_user    ", ""); // 覆核主管
    ppp("id_no       ", ""); // 存款帳號ID
    ppp("memo1       ", ""); // 摘要一
    ppp("memo2       ", ""); // 摘要二
    ppp("memo3       ", ""); // 摘要三
    ppp("key_value   ", ""); // 鍵值
    ppp("sys_rem     ", ""); // 系統備註
    ppp("jrn_status  ", ""); // 傳票狀態
    ppp("post_flag   ", ""); // 處理註記
    ppp("ifrs_flag   ", ""); // IFRS註記
    ppp("curr_code_dc", ""); // 外幣卡幣別
    ppp("mod_user", "");
    ppp("mod_pgm", "");
  }
}
