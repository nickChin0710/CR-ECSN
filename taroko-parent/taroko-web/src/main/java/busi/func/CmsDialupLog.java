/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi.func;

import busi.FuncBase;

public class CmsDialupLog extends FuncBase {


  public int insertLog() {
    if (varEmpty("kk_id_no") && varEmpty("kk_card_no")) {
      return 0;
    }

    strSql =
        "insert into cms_dialup_log (" + " dial_date, " + " dial_seqno, " + " dial_time, "
            + " dial_user, " + " card_idno_type, " + " debit_flag " + " ) values ("
            + " to_char(sysdate,'yyyymmdd')" + ", ?" // seqno
            + ", to_char(sysdate,'hh24miss')" + ", ?" // -mod_user-
            + ", ?" // -card-idno-
            + ", ?" // debit_flag
            + " )";
    sqlSelect("select " + sqlID + "uf_case_seqno() as case_seqno from " + this.sqlDual);
    if (sqlRowNum <= 0) {
      errmsg("select cms_case.nextval error");
      return -1;
    }
    this.setString(1, colStr("case_seqno"));
    setString(2, this.modUser);
    if (this.varEmpty("kk_id_no") == false) {
      setString(3, "IDNO");
    } else if (this.varEmpty("kk_card_no") == false) {
      setString(3, "CARD");
    } else
      setString(3, "");
    setString(4, varsStr("debit_flag"));
    sqlExec(strSql);
    if (sqlRowNum == 1) {
      try {
        conn.commit();
      } catch (Exception ex) {
      }
    }
    this.varsSet("case_seqno", colStr("case_seqno"));
    return rc;
  }

}
