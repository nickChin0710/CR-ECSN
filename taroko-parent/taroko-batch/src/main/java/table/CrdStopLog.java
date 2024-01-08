/**
 * Table公用程式: [crd_stop_log] V.2018-0521
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*
* */
package table;
public class CrdStopLog extends table.BaseTable {
com.CommSqlStr commSqlStr=new com.CommSqlStr();

  public void unBlockBatch() {
    String sql1 = "insert into crd_stop_log ("
        + " proc_seqno,"
        + " crt_time,"
        + " card_no,"
        + " current_code,"
        + " oppost_reason,"
        + " oppost_date,"
        + " trans_type,"
        + " send_type,"
        // +" stop_source,"
        // +" to_ibm_date,"
        // +" proc_code,"
        // +" proc_date,"
        // +" bank_actno,"
        // +" map_proc_seqno,"
        // +" map_proc_code,"
        + " mod_user, mod_time, mod_pgm, mod_seqno"
        + " ) values ("
        + commSqlStr.seqEcsStop
        + ","
        + this.sqlYYmd
        + ","
        + " :card_no,"
        + " :current_code,"
        + " :oppost_reason,"
        + " :oppost_date,"
        + " '10'," // -解凍-
        + " '2'," // MQUEUE
        // +" :stop_source,"
        // +" :to_ibm_date,"
        // +" :proc_code,"
        // +" :proc_date,"
        // +" :bank_actno,"
        // +" :map_proc_seqno,"
        // +" :map_proc_code,"
        + " :mod_user,"
        + this.sqlDTime
        + ","
        + " :mod_pgm,"
        + " 1 "
        + ")";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }
  }

  public void stopBatch() {
    String sql1 = "insert into crd_stop_log ("
        + "  proc_seqno "
        + ", crt_time"
        + ", card_no "
        + ", current_code "
        + ", oppost_reason "
        + ", oppost_date "
        + ", trans_type "
        + ", send_type "
        + ", mod_user "
        + ", mod_time "
        + ", mod_pgm "
        + ", mod_seqno "
        + " ) value ("
        + "  "
        + commSqlStr.seqEcsStop
        + ", to_char(sysdate,'yyyymmddhh24miss')"
        + ", :card_no "
        + ", '3' "
        + ", 'J2' "
        + ", :oppost_date "
        + ", '03' "
        + ", '2' "
        + ", :mod_user "
        + ", "
        + this.sqlDTime
        + ", :mod_pgm "
        + ", 1"
        + ")";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }

  }

  public void sqlInsert() {
    String sql1 = "insert into crd_stop_log ("
        + "  proc_seqno "
        + ", crt_time"
        + ", card_no "
        + ", current_code "
        + ", oppost_reason "
        + ", oppost_date "
        + ", trans_type "
        + ", send_type "
        + ", stop_source "
        + ", to_ibm_date "
        + ", proc_code "
        + ", proc_date "
        + ", bank_actno "
        + ", map_proc_seqno "
        + ", map_proc_code "
        + ", mod_user "
        + ", mod_time "
        + ", mod_pgm "
        + ", mod_seqno "
        + " ) values ("
        + "  "
        + commSqlStr.seqEcsStop
        + ", to_char(sysdate,'yyyymmddhh24miss')"
        + ", :card_no "
        + ", :current_code "
        + ", :oppost_reason "
        + ", :oppost_date "
        + ", :trans_type "
        + ", :send_type "
        + ", :stop_source "
        + ", :to_ibm_date "
        + ", :proc_code "
        + ", :proc_date "
        + ", :bank_actno "
        + ", :map_proc_seqno "
        + ", :map_proc_code "
        + ", :mod_user "
        + ", "
        + this.sqlDTime
        + ", :mod_pgm "
        + ", 1"
        + ")";
    initParms();
    if (this.nameSqlParm(sql1)) {
      sqlFrom = this.getConvSQL();
    }

  }

  public void initParms() {
    ppp("proc_seqno", "");
    ppp("crt_time", "");
    ppp("card_no", "");
    ppp("current_code", "");
    ppp("oppost_reason", "");
    ppp("oppost_date", "");
    ppp("trans_type", "");
    ppp("send_type", "");
    ppp("stop_source", "");
    ppp("to_ibm_date", "");
    ppp("proc_code", "");
    ppp("proc_date", "");
    ppp("bank_actno", "");
    ppp("map_proc_seqno", "");
    ppp("map_proc_code", "");
    ppp("mod_user", "");
    ppp("mod_pgm", "");

  }

}
