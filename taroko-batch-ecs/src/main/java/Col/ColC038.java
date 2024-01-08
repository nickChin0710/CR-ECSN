/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/04  V1.00.00    PhoPho     program initial                          *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC038 extends AccessDAO {
    private String progname = "更生送JCIC_KK4繳評異動處理程式   109/12/15  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hClrwId = "";
    String hClrwIdPSeqno = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayThisCloseDate = "";
    double hAcctAcctJrnlBal = 0;
    String hAjlgPaymentAmtRate = "";
    String hAjlgPaymentTimeRate = "";
    String hAjlgRowid = "";
    double hAjlgStmtPaymentAmt = 0;
    String hAcnoPSeqno = "";
    String hClrwRecvDate = "";
    String hClrwRenewStatus = "";
    int hTempProcMonths = 0;
    String hClrwRunRenewFlag = "";
    String hClrwCode = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoPaymentRate1 = "";
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
                comc.errExit("Usage : ColC038 [business_date] [callbatch_seqno]", "");
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

            selectColLiadRenew();

            showLogMessage("I", "", String.format("累計筆數 : [%d]",totcnt));
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
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-2 days,'yyyymmdd') ";
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
    void selectColLiadRenew() throws Exception {
//        sqlCmd = "select ";
//        sqlCmd += "a.id_no, ";
//        sqlCmd += "max(a.recv_date) h_clrw_recv_date ";
//        sqlCmd += "from col_liad_renew a,col_liab_param b ";
//        sqlCmd += "where a.renew_status = b.liab_status ";
//        sqlCmd += "and b.apr_date <> '' ";
//        sqlCmd += "and b.jcic_payrate_flag = 'Y' ";
//        sqlCmd += "and b.liab_type  = '3' ";
//        sqlCmd += "group by a.id_no, a.id_p_seqno ";
        
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno, ";
        sqlCmd += "max(a.recv_date) h_clrw_recv_date ";
        sqlCmd += "from col_liad_renew a,col_liab_param b ";
        sqlCmd += "where a.renew_status = b.liab_status ";
        sqlCmd += "and b.apr_date <> '' ";
        sqlCmd += "and b.jcic_payrate_flag = 'Y' ";
        sqlCmd += "and b.liab_type  = '3' ";
        sqlCmd += "group by a.id_p_seqno ";
        
        openCursor();
        while (fetchTable()) {
//            h_clrw_id = getValue("id_no");
        	hClrwIdPSeqno = getValue("id_p_seqno");
            hClrwRecvDate = getValue("h_clrw_recv_date");

            if (selectColLiadRenewA() != 0)
                continue;

            selectActAcct();
            if (hAcctAcctJrnlBal == 0)
                continue;

            selectActAcno();
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectColLiadRenewA() throws Exception {
        hClrwRenewStatus = "";
        hTempProcMonths = 0;
        hClrwRunRenewFlag = "";
        hClrwCode = "";

        sqlCmd = "select a.renew_status,";
//        sqlCmd += "months_between(to_date(?,'yyyymmdd'), ";  //空白轉'yyyymmdd'時會ERROR
//        sqlCmd += "to_date(decode(a.renew_status,'3', decode(run_renew_flag,'Y',";
//        sqlCmd += "a.renew_first_date, a.renew_damage_date), recv_date),'yyyymmdd')) proc_months,";
        sqlCmd += "months_between(to_date(?,'yyyymmdd'), ";
        sqlCmd += "to_date(decode(a.renew_status,'3', decode(run_renew_flag,'Y',";
        sqlCmd += "decode(a.renew_first_date,'',?,a.renew_first_date),";
        sqlCmd += "decode(a.renew_damage_date,'',?,a.renew_damage_date)), recv_date),'yyyymmdd')) proc_months,";
        sqlCmd += "run_renew_flag,";
        sqlCmd += "a.m_code ";
        sqlCmd += " from col_liad_renew a,col_liab_param b  ";
        sqlCmd += "where a.renew_status = b.liab_status  ";
        sqlCmd += "and b.apr_date <> '' ";
        sqlCmd += "and b.jcic_payrate_flag = 'Y'  ";
        sqlCmd += "and b.liab_type = '3'  ";
//        sqlCmd += "and a.id_no = ?  ";
        sqlCmd += "and a.id_p_seqno = ? ";
        sqlCmd += "and a.recv_date = ? ";
        sqlCmd += "order by a.renew_status desc ";
        setString(1, hWdayThisCloseDate);
        setString(2, hWdayThisCloseDate);
        setString(3, hWdayThisCloseDate);
//        setString(4, h_clrw_id);
        setString(4, hClrwIdPSeqno);
        setString(5, hClrwRecvDate);
        
        extendField = "col_liad_renew_a.";
        
        selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        hClrwRenewStatus = getValue("col_liad_renew_a.renew_status");
        hTempProcMonths = getValueInt("col_liad_renew_a.proc_months");
        hClrwRunRenewFlag = getValue("col_liad_renew_a.run_renew_flag");
        hClrwCode = getValue("col_liad_renew_a.m_code");

        return 0;
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;

//        sqlCmd = "select nvl(sum(a.acct_jrnl_bal), 0) h_acct_acct_jrnl_bal ";
//        sqlCmd += " from act_acct a, crd_idno b   ";
//        sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_no = ? ";
//        setString(1, h_clrw_id);
        
        sqlCmd = "select nvl(sum(a.acct_jrnl_bal), 0) h_acct_acct_jrnl_bal ";
        sqlCmd += " from act_acct a ";
        sqlCmd += "where a.id_p_seqno = ? ";
        setString(1, hClrwIdPSeqno);
        
        extendField = "act_acct.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", "", comcr.hCallBatchSeqno);
        }
        hAcctAcctJrnlBal = getValueDouble("act_acct.h_acct_acct_jrnl_bal");

        if (hAcctAcctJrnlBal < 0)
            hAcctAcctJrnlBal = 0;
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        String tmpstr = "";
        int int3i = 0;
        double int3d = 0;

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
//        setString(1, h_clrw_id);
      
        sqlCmd = "select ";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.payment_rate1 ";
        sqlCmd += "from act_acno a ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and a.id_p_seqno = ? ";
        setString(1, hClrwIdPSeqno);
      
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoPaymentRate1 = getValue("act_acno.payment_rate1", i);

            switch (comcr.str2int(hClrwRenewStatus)) {
            case 1:
                if (hClrwCode.equals("0A")) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "N";
                } else if (hClrwCode.equals("0B")) {
                    hAjlgPaymentAmtRate = "1";
                    hAjlgPaymentTimeRate = "0";
                } else if (hClrwCode.equals("0C")) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "N";
                } else if (hClrwCode.equals("0D")) {
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "0";
                } else if (hClrwCode.equals("0E")) {
                    hAjlgPaymentAmtRate = "X";
                    hAjlgPaymentTimeRate = "X";
                } else if ((hClrwCode.compareTo("01") >= 0) && (hClrwCode.compareTo("06") <= 0)) {
                    tmpstr = String.format("%1.1s", comc.getSubString(hClrwCode, 1));
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = tmpstr;
                } else {
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = "7";
                }
                break;

            case 3:
//                if (h_clrw_run_renew_flag.substring(0, 1).equals("Y")) {  /* "是否依更生條件履行" */
                if (comc.getSubString(hClrwRunRenewFlag,0,1).equals("Y")) {  /* "是否依更生條件履行" */
                    hAjlgPaymentAmtRate = "2";
                    hAjlgPaymentTimeRate = "N";
                    if ((hTempProcMonths >= 2) || (hClrwCode.compareTo("04") < 0))
                        break;
                    if (hClrwCode.equals("0A")) {
                        hAjlgPaymentAmtRate = "1";
                        hAjlgPaymentTimeRate = "N";
                    } else if (hClrwCode.equals("0B")) {
                        hAjlgPaymentAmtRate = "1";
                        hAjlgPaymentTimeRate = "0";
                    } else if (hClrwCode.equals("0C")) {
                        hAjlgPaymentAmtRate = "2";
                        hAjlgPaymentTimeRate = "N";
                    } else if (hClrwCode.equals("0C")) {
                        hAjlgPaymentAmtRate = "2";
                        hAjlgPaymentTimeRate = "N";
                    } else if (hClrwCode.equals("0D")) {
                        hAjlgPaymentAmtRate = "2";
                        hAjlgPaymentTimeRate = "0";
                    } else if (hClrwCode.equals("0E")) {
                        hAjlgPaymentAmtRate = "X";
                        hAjlgPaymentTimeRate = "X";
                    } else if ((hClrwCode.compareTo("01") >= 0) && (hClrwCode.compareTo("06") <= 0)) {
                        tmpstr = String.format("%1.1s", comc.getSubString(hClrwCode, 1));
                        hAjlgPaymentAmtRate = "4";
                        hAjlgPaymentTimeRate = tmpstr;
                    } else {
                        hAjlgPaymentAmtRate = "4";
                        hAjlgPaymentTimeRate = "7";
                    }
                } else {
                    int3d = hTempProcMonths + 1;
                    int3i = (int) int3d;
                    if (int3i > 7)
                        int3i = 7;
                    tmpstr = String.format("%1d", int3i);
                    hAjlgPaymentAmtRate = "4";
                    hAjlgPaymentTimeRate = tmpstr;
                }
                break;
            case 2:
            case 7:
                int3d = hTempProcMonths + 1;
                int3i = (int) int3d;
                tmpstr = String.format("%2.2s", hClrwCode);
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

    /***********************************************************************/
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
            comcr.errRtn("update_act_jcic_log not found!", "", comcr.hCallBatchSeqno);
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
        if (notFound.equals("Y")) return 1;
        
        hAjlgStmtPaymentAmt = getValueDouble("act_jcic_log.stmt_payment_amt");
        hAjlgRowid = getValue("act_jcic_log.rowid");

        return 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC038 proc = new ColC038();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
