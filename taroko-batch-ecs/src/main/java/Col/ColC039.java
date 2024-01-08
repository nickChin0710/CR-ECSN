/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/05  V1.00.00    PhoPho     program initial                          *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC039 extends AccessDAO {
    private String progname = "清算送JCIC_KK4繳評異動處理程式 109/12/15  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hClleId = "";
    String hClleIdPSeqno = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayThisCloseDate = "";
    double hAcctAcctJrnlBal = 0;
    String hAjlgPaymentAmtRate = "";
    String hAjlgPaymentTimeRate = "";
    String hAjlgRowid = "";
    double hAjlgStmtPaymentAmt = 0;
    String hAcnoPSeqno = "";
    String hClleRecvDate = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoPaymentRate1 = "";
    String hClleMCode = "";
    String hClleLiquStatus = "";
    int hTempProcMonths = 0;
    int totcnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ColC039 [business_date] [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);
            

            hBusiBusinessDate = "";
            if ((args.length >= 1) && (args[0].length() == 8))
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            if (selectPtrWorkday() != 0) {
            	exceptExit = 0;
                comcr.errRtn(String.format("本程式為關帳日後二天執行，本日[%s] !", hBusiBusinessDate), "", comcr.hCallBatchSeqno);
            }

            selectColLiadLiquidate();

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "執行結束");
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

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        hWdayThisAcctMonth = "";
        hWdayThisCloseDate = "";

        sqlCmd = "select stmt_cycle,";
        sqlCmd += "this_acct_month,";
        sqlCmd += "this_close_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-2,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        hWdayStmtCycle = getValue("stmt_cycle");
        hWdayThisAcctMonth = getValue("this_acct_month");
        hWdayThisCloseDate = getValue("this_close_date");

        return 0;
    }

    /***********************************************************************/
    void selectColLiadLiquidate() throws Exception {
//        sqlCmd = "select ";
//        sqlCmd += "a.id_no,";
//        sqlCmd += "max(a.recv_date) h_clle_recv_date ";
//        sqlCmd += "from col_liad_liquidate a,col_liab_param b ";
//        sqlCmd += "where a.liqu_status = b.liab_status ";
//        sqlCmd += "and b.apr_date <> '' ";
//        sqlCmd += "and b.jcic_payrate_flag = 'Y' ";
//        sqlCmd += "and b.liab_type  = '4' ";
//        sqlCmd += "group by a.id_no ";
        
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "max(a.recv_date) h_clle_recv_date ";
        sqlCmd += "from col_liad_liquidate a,col_liab_param b ";
        sqlCmd += "where a.liqu_status = b.liab_status ";
        sqlCmd += "and b.apr_date <> '' ";
        sqlCmd += "and b.jcic_payrate_flag = 'Y' ";
        sqlCmd += "and b.liab_type  = '4' ";
        sqlCmd += "group by a.id_p_seqno ";

        openCursor();
        while (fetchTable()) {
//            h_clle_id = getValue("id_no");
        	hClleIdPSeqno = getValue("id_p_seqno");
            hClleRecvDate = getValue("h_clle_recv_date");

            if (selectColLiadLiquidateA() != 0)
                continue;

            selectActAcct();
            if (hAcctAcctJrnlBal == 0)
                continue;

            selectActAcno();
        }
        closeCursor();
    }

    /**********************************************************************/
    int selectColLiadLiquidateA() throws Exception {
        sqlCmd = "select a.liqu_status,";
        sqlCmd += "months_between(to_date(?,'yyyymmdd'), to_date(recv_date,'yyyymmdd')) proc_months,";
        sqlCmd += "a.m_code ";
        sqlCmd += " from col_liad_liquidate a,col_liab_param b  ";
        sqlCmd += "where a.liqu_status = b.liab_status  ";
        sqlCmd += "and b.apr_date <> '' ";
        sqlCmd += "and b.jcic_payrate_flag = 'Y'  ";
        sqlCmd += "and b.liab_type = '4'  ";
//        sqlCmd += "and a.id_no = ?  ";
        sqlCmd += "and a.id_p_seqno = ? ";
        sqlCmd += "and a.recv_date = ? ";
        sqlCmd += "order by a.liqu_status desc ";
        setString(1, hWdayThisCloseDate);
//        setString(2, h_clle_id);
        setString(2, hClleIdPSeqno);
        setString(3, hClleRecvDate);
        
        extendField = "col_liad_liquidate_a.";

        selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        hClleLiquStatus = getValue("col_liad_liquidate_a.liqu_status");
        hTempProcMonths = getValueInt("col_liad_liquidate_a.proc_months");
        hClleMCode = getValue("col_liad_liquidate_a.m_code");

        return 0;
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;

//        sqlCmd = "select sum(a.acct_jrnl_bal) h_acct_acct_jrnl_bal ";
//        sqlCmd += " from act_acct a, crd_idno b  ";
//        sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_no = ? ";
//        setString(1, h_clle_id);
        
        sqlCmd = "select nvl(sum(a.acct_jrnl_bal), 0) h_acct_acct_jrnl_bal ";
        sqlCmd += " from act_acct a ";
        sqlCmd += "where a.id_p_seqno = ? ";
        setString(1, hClleIdPSeqno);
        
        extendField = "act_acct.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", "", comcr.hCallBatchSeqno );
        }
        hAcctAcctJrnlBal = getValueDouble("act_acct.h_acct_acct_jrnl_bal");

        if (hAcctAcctJrnlBal <= 0)
            hAcctAcctJrnlBal = 0;
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int int3i;
        double int3d;
        String tmpstr = "";

//        sqlCmd = "select ";
////        sqlCmd += "a.p_seqno,";
//        sqlCmd += "a.acno_p_seqno,";
//        sqlCmd += "a.acct_type,";
//        sqlCmd += "a.acct_key,";
//        sqlCmd += "a.payment_rate1 ";
//        sqlCmd += "from act_acno a, crd_idno b ";
////        sqlCmd += "where a.p_seqno = a.gp_no ";
//        sqlCmd += "where a.acno_flag <> 'Y' ";
//        sqlCmd += "and b.id_p_seqno = a.id_p_seqno ";
//        sqlCmd += "and b.id_no = ? ";
//        setString(1, h_clle_id);
        
        sqlCmd = "select ";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.payment_rate1 ";
        sqlCmd += "from act_acno a ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and a.id_p_seqno = ? ";
        setString(1, hClleIdPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoPaymentRate1 = getValue("act_acno.payment_rate1", i);

            switch (comcr.str2int(hClleLiquStatus)) {
            case 1:
                if (hClleMCode.equals("0A")) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "N";
                } else if (hClleMCode.equals("0B")) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "0";
                } else if (hClleMCode.equals("0C")) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "N";
                } else if (hClleMCode.equals("0D")) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "0";
                } else if (hClleMCode.equals("0E")) {
                    hAjlgPaymentAmtRate = "X";
                    hAjlgPaymentTimeRate = "X";
                } else if ((hClleMCode.compareTo("01") >= 0) && (hClleMCode.compareTo("06") <= 0)) {
                    tmpstr = String.format("%1.1s", comc.getSubString(hClleMCode, 1));
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = tmpstr;
                } else {
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = "7";
                }
                break;
            case 3:
            case 6:
            case 7:
                int3d = hTempProcMonths + 1;
                int3i = (int) int3d;
                tmpstr = String.format("%2.2s", hClleMCode);
                int3i = int3i + comcr.str2int(tmpstr);
                if (int3i > 7)
                    int3i = 7;
                tmpstr = String.format("%1d", int3i);
                hAjlgPaymentAmtRate = "4";
                hAjlgPaymentTimeRate = tmpstr;
                break;
            default:
                continue;
            }
            if (selectActJcicLog() != 0)
                continue;

            if (hAcnoPaymentRate1.equals("0E")) {
                hAjlgPaymentAmtRate = "X";
                hAjlgPaymentTimeRate = "X";
            }
            updateActJcicLog();
            totcnt++;
            if (totcnt % 10000 == 0) {
                showLogMessage("I", "", String.format("處理筆數 : [%d]", totcnt));
            }
        }
    }

    /**********************************************************************/
    void updateActJcicLog() throws Exception {
        daoTable = "act_jcic_log";
        updateSQL = "payment_amt_rate = ?,";
        updateSQL += " payment_time_rate = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hAjlgPaymentAmtRate);
        setString(2, hAjlgPaymentTimeRate);
        setString(3, javaProgram);
        setRowId(4, hAjlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int selectActJcicLog() throws Exception {
        sqlCmd = "select stmt_payment_amt,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from act_jcic_log  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and log_type = 'A'  ";
        sqlCmd += "and acct_month = ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hWdayThisAcctMonth);
        
        extendField = "act_jcic_log.";
        
        selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        hAjlgStmtPaymentAmt = getValueDouble("act_jcic_log.stmt_payment_amt");
        hAjlgRowid = getValue("act_jcic_log.rowid");

        return 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC039 proc = new ColC039();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
