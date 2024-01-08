/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-01-11  V1.00.00  Yang Bo      program initial                          *
 ******************************************************************************/
package colm01;

import busi.FuncEdit;

public class Colm6020Func extends FuncEdit {
    @Override
    public int querySelect() {
        return 0;
    }

    @Override
    public int dataSelect() {
        return 0;
    }

    @Override
    public void dataCheck() {

    }

    @Override
    public int dbInsert() {
        dateTime();
        actionCode = "A";
        // -案件存檔-
        dataCheck();
        if (rc != 1) {
            return rc;
        }

        busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("col_cs_cslog", wp);
        sp.ppymd("crt_date");
        sp.pptime("crt_time");
        sp.ppstr("id_p_seqno");
        sp.ppstr("p_seqno");
        sp.ppstr("curr_code");
        sp.ppstr("corp_p_seqno");
        sp.ppstr("acct_type");
        sp.ppstr("proc_type", "2");
        sp.ppstr("proc_user");
        sp.ppstr("proc_user_deptno");
        sp.ppstr("proc_code");
        sp.ppstr("proc_code_desc");
        sp.ppstr("callout_tel");
        sp.ppstr("proc_desc");
        sp.addsql(", mod_time ", ", sysdate ");
        sp.ppstr("mod_pgm", modPgm);

        sqlExec(sp.sqlStmt(), sp.sqlParm());
        if (sqlRowNum != 1) {
            errmsg("insert COL_CS_CSLOG [紀錄存檔] error");
            return -1;
        }

        return rc;
    }

    @Override
    public int dbUpdate() {
        return 0;
    }

    @Override
    public int dbDelete() {
        return 0;
    }
}
