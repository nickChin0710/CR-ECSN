/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/22  V1.00.01    Brian     error correction                          *
 *  109/11/19  V1.00.02    shiyuqi   updated for project coding standard       *
 *  111/10/25  V1.00.03    Simon     sync codes with mega                      *
 ******************************************************************************/

package Act;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*欠款結清通知JCIC處理*/
public class ActN005 extends AccessDAO {

    public static final boolean debug = false;

    private String progname = "欠款結清通知JCIC處理   111/10/25  V1.00.03 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN005";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    int recordCnt = 0;
    String essServer = "";

    String hBusiBusinessDate = "";
    String hAcnoPSeqno = "";
    String hAcnoIdPSeqno = "";
    String hAcnoRcUseBAdj = "";
    String hAcnoRcUseIndicator = "";
    String hAcnoLastPayDate = "";
    String hAcnoRcUseSDate = "";
    String hAcnoRcUseEDate = "";
    int hCount = 0;
    String hCardCardNo = "";
    String hCardCreateDate = "";
    String hCardCreateId = "";
    String hCardCurrentCode = "";
    String hCardOppostReason = "";
    String hCardOppostDate = "";
    String hCardRowid = "";
    String hCdjcRowid = "";
    String hCdjcIsRc = "";
    String hCardModUser = "";
    String hCardModPgm = "";
    String hCardModSeqno = "";
    int hCnt = 0;
    int recAcno = 0;
    int recCard = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                exceptExit = -1;
                comc.errExit("Usage : ActN005", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hModUser = comc.commGetUserID();
            hCardModUser = hModUser;
            hCardModPgm = javaProgram;

            selectPtrBusinday();

            selectActAcno();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectActAcno() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " rc_use_b_adj,";
        sqlCmd += " rc_use_indicator,";
        sqlCmd += " last_pay_date,";
        sqlCmd += " decode(rc_use_s_date,'','00000101',rc_use_s_date) h_acno_rc_use_s_date,";
        sqlCmd += " decode(rc_use_e_date,'','99991231',rc_use_e_date) h_acno_rc_use_e_date ";
        sqlCmd += " from act_acno ";
        sqlCmd += "where stop_status     = 'Y'  "; /* 強制停用旗標 */
        sqlCmd += "  and debt_close_date = '' ";
        sqlCmd += "  and acno_flag <> 'Y' ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoRcUseBAdj = getValue("rc_use_b_adj");
            hAcnoRcUseIndicator = getValue("rc_use_indicator");
            hAcnoLastPayDate = getValue("last_pay_date");
            hAcnoRcUseSDate = getValue("h_acno_rc_use_s_date");
            hAcnoRcUseEDate = getValue("h_acno_rc_use_e_date");
            recAcno++;
            if (debug)
                if (recAcno == 1 || recAcno % 20000 == 0)
                    showLogMessage("I", "", "" + recAcno);
          //if (hAcnoRcUseIndicator.substring(0, 1).equals("1")) {
            if (comc.getSubString(hAcnoRcUseIndicator, 0,1).equals("1")) {
                hCdjcIsRc = hAcnoRcUseIndicator;
            } else {
                if (hBusiBusinessDate.compareTo(hAcnoRcUseSDate) < 0) {
                    hCdjcIsRc = hAcnoRcUseBAdj;
                } else {
                    hCdjcIsRc = hAcnoRcUseIndicator;
                }
            }

            /*** 若註記扣薪戶則暫不處理 ***/
            if (selectCrdIdno() != 0)
                continue;
            /*** 若餘額小於等於 0 ***/
            if (checkActAcct() == 1) {
                updateActAcno();
                selectCrdJcic();
            }
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    int selectCrdIdno() throws Exception {
        hCnt = 0;

        sqlCmd = "select sum(decode(decode(salary_holdin_flag, '', 'N', salary_holdin_flag),'Y', 1,'y', 1, 0)) h_cnt ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }

        return (hCnt);
    }

    /***********************************************************************/
    int checkActAcct() throws Exception {
        hCnt = 0;
        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from act_acct  ";
        sqlCmd += "where p_seqno        = ?  ";
        sqlCmd += "  and acct_jrnl_bal <= 0 "; /* 帳戶目前總餘額 */
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
        }

        if (hCount > 0)
            return 1;
        else
            return 0;
    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = " act_acno";
        updateSQL = " debt_close_date = ?,";
        updateSQL += " stop_reason     = 'U1'";
        whereStr = "where acno_p_seqno = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hAcnoPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCrdJcic() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " card_no,";
        sqlCmd += " crt_date,";
        sqlCmd += " crt_user,";
        sqlCmd += " current_code,";
        sqlCmd += " oppost_reason,";
        sqlCmd += " oppost_date,";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from crd_card ";
        sqlCmd += "where acno_p_seqno = ? ";
        sqlCmd += "  and current_code = '3' ";
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardCardNo = getValue("card_no", i);
            hCardCreateDate = getValue("crt_date", i);
            hCardCreateId = getValue("crt_user", i);
            hCardCurrentCode = getValue("current_code", i);
            hCardOppostReason = getValue("oppost_reason", i);
            hCardOppostDate = getValue("oppost_date", i);
            hCardRowid = getValue("rowid", i);

            recCard++;
            if (checkCrdJcic() == 1) {
                updateCrdJcic();
            } else {
                insertCrdJcic();
            }

            updateCrdCard();
        }

    }

    /***********************************************************************/
    int checkCrdJcic() throws Exception {
        hCdjcRowid = "";

        sqlCmd = "select rowid rowid ";
        sqlCmd += " from crd_jcic  ";
        sqlCmd += "where card_no  = ?  ";
        sqlCmd += "  and current_code = '3'  ";
        sqlCmd += "  and to_jcic_date = '' ";
        setString(1, hCardCardNo);
        int recordCnt1 = selectTable();
        if (recordCnt1 > 0) {
            hCdjcRowid = getValue("rowid");
            return 1;
        } else
            return 0;
    }

    /***********************************************************************/
    void updateCrdJcic() throws Exception {
        daoTable = " crd_jcic";
        updateSQL = " current_code  = ?,";
        updateSQL += " oppost_reason = 'U',";
        updateSQL += " is_rc         = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " payment_date  = ?";
        whereStr = "where rowid    = ? ";
        setString(1, hCardCurrentCode);
        setString(2, hCdjcIsRc);
        setString(3, hBusiBusinessDate);
        setRowId(4, hCdjcRowid);
        updateTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("update_crd_jcic not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertCrdJcic() throws Exception {
        hModSeqno = comcr.getModSeq();

        setValue("card_no", hCardCardNo);
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_user", hCardCreateId);
        setValue("trans_type", "C");
        setValue("current_code", hCardCurrentCode);
        setValue("oppost_reason", "U");
        setValue("oppost_date", hCardOppostDate);
        setValue("is_rc", hCdjcIsRc);
        setValue("payment_date", hBusiBusinessDate);
        setValue("mod_user", hCardModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hCardModPgm);
        // setValue("mod_ws" , h_card_mod_ws);
        setValue("mod_seqno", hCardModSeqno);
        // setValue("mod_log" , "");
        daoTable = "crd_jcic";
        insertTable();
        if (dupRecord.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("insert_crd_jcic duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {
        daoTable = "crd_card";
        updateSQL = "oppost_reason = 'U1'";
        whereStr = "where rowid   = ? ";
        setRowId(1, hCardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("update_crd_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN005 proc = new ActN005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
