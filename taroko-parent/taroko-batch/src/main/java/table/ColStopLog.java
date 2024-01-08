/**
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*
* */
package table;

public class ColStopLog extends BaseTable {


  public void sqlInsert() {
    String sql1 = "insert into col_stop_log ("
        + "  insert_date"
        + ", proc_date"
        + ", major_id_p_seqno"
        + ", id_p_seqno"
        + ", p_seqno "
        + ", major_id "
        + ", major_id_code "
        + ", id_no "
        + ", id_code "
        + ", card_no "
        + ", oppost_date "
        + ", proc_mark "
        + ", mod_time "
        + " ) values ("
        + "  :insert_date"
        + ", :proc_date"
        + ", :major_id_p_seqno"
        + ", :id_p_seqno"
        + ", :p_seqno "
        + ", :major_id "
        + ", :major_id_code "
        + ", :id_no "
        + ", :id_code "
        + ", :card_no "
        + ", :oppost_date "
        + ", :proc_mark "
        + ", "
        + this.sqlDTime
        + " )";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void initParms() {
    ppp("insert_date", "");
    ppp("insert_date", "");
    ppp("proc_date", "");
    ppp("major_id_p_seqno", "");
    ppp("id_p_seqno", "");
    ppp("p_seqno", "");
    ppp("major_id", "");
    ppp("major_id_code", "0");
    ppp("id_no", "");
    ppp("id_code", "0");
    ppp("card_no", "");
    ppp("oppost_date", "");
    ppp("proc_mark", "");
  }

}
