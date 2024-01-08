/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/07/14  V1.01.01  phopho     Initial                                    *
*  109/12/10  V1.00.01    shiyuqi       updated for project coding standard   *
*****************************************************************************/
package Col;

import com.*;

public class ColA418 extends AccessDAO {
    private String progname = "前置協商送JCIC_KK4處理程式 109/12/10  V1.00.01 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debug1 = 0;
    String hBusiBusinessDate = "";

    String hAcctLastPaymentDate = "";
    String hAjlgPaymentAmtRate = "";
    String hAjlgPaymentTimeRate = "";
    double hAjlgStmtPaymentAmt = 0;
    String hAjlgRowid = "";
    String hClnoLiacStatus = "";
    String hClnoNotifyDate = "";
    String hClnoId = "";
    String hClnoIdPSeqno = "";
    String hClnoApplyDate = "";
    String hClnoRecolReason = "";
    String hCcddPaymentRate1 = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayThisCloseDate = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoPaymentRate1 = "";
    String hCallBatchSeqno = "";

    long totalCnt = 0;
    double hTempProcMonths;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColA418 proc = new ColA418();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // 檢查參數
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ColA418 [business_date]", "");
            }
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if (args.length == 1 && args[0].length() == 8) {
                hBusiBusinessDate = args[0];
            }

            selectPtrBusinday();
            if (selectPtrWorkday() != 0) {
                exceptExit = 0;
                comcr.errRtn("本程式為關帳日後二天換日執行，本日 : [" + hBusiBusinessDate + "]", "", hCallBatchSeqno);
            }

            selectColLiacNego();

            showLogMessage("I", "", "程式執行結束,累計筆數 : [" + totalCnt + "]");

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    // ************************************************************************
    private void selectColLiacNego() throws Exception {
        selectSQL = "a.id_p_seqno, a.id_no, a.apply_date, a.notify_date, a.liac_status, recol_reason, "
                + "months_between(to_date(?,'yyyymmdd'),"
                + "to_date(decode(a.liac_status,'3',c.install_s_date,a.notify_date),'yyyymmdd')) as proc_months ";
        daoTable = "col_liab_param b,col_liac_nego a LEFT JOIN col_liac_contract c ON c.liac_seqno = a.liac_seqno "
                + "AND decode(c.file_date,'','x',c.file_date) = (select decode(min(file_date),'','x',min(file_date)) from col_liac_contract h where  h.liac_seqno = a.liac_seqno)";
        whereStr = "where a.liac_status = b.liab_status and b.apr_date <> '' "
                + "and b.jcic_payrate_flag = 'Y' and b.liab_type = '2'";
        setString(1, hWdayThisCloseDate);

        openCursor();
        while (fetchTable()) {
            hClnoIdPSeqno = getValue("id_p_seqno");
            hClnoId = getValue("id_no");
            hClnoApplyDate = getValue("apply_date");
            hClnoNotifyDate = getValue("notify_date");
            hClnoLiacStatus = getValue("liac_status");
            hClnoRecolReason = getValue("recol_reason");
            hTempProcMonths = getValueDouble("proc_months");

            selectActAcno();
        }

        closeCursor();
    }

    // ************************************************************************
    private void selectActAcno() throws Exception {
        int int3i = 0;
        double int3d = 0;
//        selectSQL = "a.p_seqno, a.acct_type, a.acct_key, a.payment_rate1, "
//                + "b.payment_rate1 ccdd_payment_rate1 ";
//        daoTable = "act_acno a,col_liac_debt_dtl b";
//        whereStr = "where a.p_seqno        = b.p_seqno and   b.id_no          = ? and   b.apply_date     = ? "
//                + "and   a.stmt_cycle     = ? ";
        selectSQL = "a.acno_p_seqno, a.acct_type, a.acct_key, a.payment_rate1, "
                  + "b.payment_rate1 ccdd_payment_rate1 ";
        daoTable = "act_acno a, col_liac_debt_dtl b";
        whereStr = "where a.acno_p_seqno = b.p_seqno and b.id_p_seqno = ? "
                 + "and b.apply_date = ? and a.stmt_cycle = ? ";
//        setString(1, h_clno_id);
        setString(1, hClnoIdPSeqno);
        setString(2, hClnoApplyDate);
        setString(3, hWdayStmtCycle);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoPaymentRate1 = getValue("act_acno.payment_rate1", i);
            hCcddPaymentRate1 = getValue("act_acno.ccdd_payment_rate1", i);

            switch (hClnoLiacStatus) {
            case "1":
            case "2":
                if (hCcddPaymentRate1.compareTo("0A") == 0) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "N";
                } else if (hCcddPaymentRate1.compareTo("0B") == 0) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "0";
                } else if (hCcddPaymentRate1.compareTo("0C") == 0) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "N";
                } else if (hCcddPaymentRate1.compareTo("0D") == 0) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "0";
                } else if (hCcddPaymentRate1.compareTo("0E") == 0) {
                    hAjlgPaymentAmtRate = "X";
                    hAjlgPaymentTimeRate = "X";
                } else if ((hCcddPaymentRate1.compareTo("01") >= 0) && (hCcddPaymentRate1.compareTo("06") <= 0)) {
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = hCcddPaymentRate1.substring(1, 2);
                } else {
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = "7";
                }
                break;
            case "3":
                hAjlgPaymentAmtRate = "2";
                hAjlgPaymentTimeRate = "N";
                if ((hTempProcMonths >= 2) || (hCcddPaymentRate1.compareTo("04") < 0))
                    break;
                if (hCcddPaymentRate1.compareTo("0A") == 0) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "N";
                } else if (hCcddPaymentRate1.compareTo("0B") == 0) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "0";
                } else if (hCcddPaymentRate1.compareTo("0C") == 0) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "N";
                } else if (hCcddPaymentRate1.compareTo("0D") == 0) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "0";
                } else if (hCcddPaymentRate1.compareTo("0E") == 0) {
                    hAjlgPaymentAmtRate = "X";
                    hAjlgPaymentTimeRate = "X";
                } else if ((hCcddPaymentRate1.compareTo("01") >= 0) && (hCcddPaymentRate1.compareTo("06") <= 0)) {
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = hCcddPaymentRate1.substring(1, 2);
                } else {
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = "7";
                }
                break;
            case "4":
                selectActAcct();
                if (hAcctLastPaymentDate.compareTo(hClnoNotifyDate) >= 0)
                    continue;
                if (hClnoRecolReason.compareTo("00") == 0) {
                    int3d = hTempProcMonths + 1;
                    int3i = (int) int3d;
                    if (int3i > 7)
                        int3i = 7;
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = String.format("%1d", int3i);
                } else {
                    int3d = hTempProcMonths + 1; /* & recol_reason!='00' */
                    int3i = (int) int3d;
                    int3i = int3i + comcr.str2int(hCcddPaymentRate1);
                    if (int3i > 7)
                        int3i = 7;
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = String.valueOf(int3i);
                }
                break;
            default:
                continue;
            }
            if (selectActJcicLog() != 0)
                continue;

            if (hAcnoPaymentRate1.compareTo("0E") == 0) {
                hAjlgPaymentAmtRate = "X";
                hAjlgPaymentTimeRate = "X";
            }
            updateActJcicLog();
            totalCnt++;
            if (totalCnt % 10000 == 0)
                showLogMessage("I", "", "處理筆數 : [" + totalCnt + "]");
        }
    }

    // ************************************************************************
    private int selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        hWdayThisAcctMonth = "";
        hWdayThisCloseDate = "";
        sqlCmd = "select stmt_cycle, this_acct_month, this_close_date ";
        sqlCmd += "from ptr_workday where this_close_date = to_char(to_date(?,'yyyymmdd')-2,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);

        if (selectTable() > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
        }

        if (notFound.equals("Y"))
            return (1);
        return (0);
    }

    // ************************************************************************
    private void selectActAcct() throws Exception {
        hAcctLastPaymentDate = "";
        sqlCmd = "select last_payment_date ";
        sqlCmd += "from act_acct where p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct.";

        if (selectTable() > 0) {
            hAcctLastPaymentDate = getValue("act_acct.last_payment_date");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }

    }

    // ************************************************************************
    private void updateActJcicLog() throws Exception {
        updateSQL = "payment_amt_rate = ?, payment_time_rate = ?, mod_time = sysdate, mod_pgm = ? ";
        daoTable = "act_jcic_log";
        whereStr = "WHERE rowid = ? ";
        setString(1, hAjlgPaymentAmtRate);
        setString(2, hAjlgPaymentTimeRate);
        setString(3, javaProgram);
        setRowId(4, hAjlgRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_log error!", "rowid=[" + hAjlgRowid + "]", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private int selectActJcicLog() throws Exception {
        sqlCmd = "select stmt_payment_amt, rowid as rowid ";
        sqlCmd += "from act_jcic_log where p_seqno = ? ";
        sqlCmd += "and  log_type = 'A' and acct_month = ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hWdayThisAcctMonth);
        
        extendField = "act_jcic_log.";

        if (selectTable() > 0) {
            hAjlgStmtPaymentAmt = getValueDouble("act_jcic_log.stmt_payment_amt");
            hAjlgRowid = getValue("act_jcic_log.rowid");
        }

        if (notFound.equals("Y"))
            return 1;
        return 0;
    }
    // ************************************************************************
}