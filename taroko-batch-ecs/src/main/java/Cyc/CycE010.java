/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/09/18  V1.00.00  Zuwei Su   program initial                            *
* 112/09/27  V1.00.01  Zuwei Su   查詢crd_card增加curr_code判斷和排序        *
*                                                                            *
******************************************************************************/
package Cyc;

import com.AccessDAO;
import com.CommFunction;
import com.CommRoutine;

public class CycE010 extends AccessDAO {
    private String progname = "現金回饋結餘加檔處理程式 112/09/18 V1.00.00";
    private CommFunction comm = new CommFunction();
    private CommRoutine comr = null;

    private String hBusinessDate = "";
    private String hFuncCode = "";
    private String hPSeqno = "";

    private int totalCnt = 0;
    private int cashbackCnt = 0;
    private int cycDcFund = 0;
    private int cycFundCnt = 0;
    private int bilSysexpCnt = 0;
    private String tranSeqno = null;

    // ************************************************************************
    public static void main(String[] args) throws Exception {
        CycE010 proc = new CycE010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
        return;
    }

    // ************************************************************************
    public int mainProcess(String[] args) {
        try {
            dateTime();
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length > 3) {
                showLogMessage("I", "", "請輸入參數:");
                showLogMessage("I", "", "PARM 1 : [hBusinessDate]");
                showLogMessage("I", "", "PARM 2 : [fundCode]");
                showLogMessage("I", "", "PARM 3 : [pSeqno]");
                return (1);
            }

            if (args.length > 0) {
                hBusinessDate = args[0];
            }
            if (args.length > 1) {
                hFuncCode = args[1];
            }
            if (args.length > 2) {
                hPSeqno = args[2];
            }

            if (!connectDataBase()) {
                return (1);
            }

            comr = new CommRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            int r = selectPtrWorkday();
            if (r != 0) {
                showLogMessage("I", "", "本日營業日非關帳日" + hBusinessDate + ",無需處理,結束程式 !");
                return (0);
            }

            // 3.   選取台幣基金資料計算處理
            selectMktFundDtl();
            // 4.   選取外幣基金資料計算處理
            selectCycDcFundDtl();

            // (end) ---------------------------------------------

            finalProcess();
            return (0);
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

    } // End of mainProcess

    // ************************************************************************
    public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date, " + "rowid as rowid";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        selectTable();

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "select ptr_businday error!");
            exitProgram(1);
        }

        if (hBusinessDate.length() == 0) {
            hBusinessDate = getValue("BUSINESS_DATE");
        }

        showLogMessage("I", "", "本日營業日 : [" + hBusinessDate + "]");
    }

    // ************************************************************************
    public int selectPtrWorkday() throws Exception {
        extendField = "wday.";
        selectSQL = "";
        daoTable = "PTR_WORKDAY";
        whereStr = "WHERE next_close_date = ? ";
        setString(1, hBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            return (1);
        }

        return (0);
    }

    private void selectMktFundDtl() throws Exception {
        sqlCmd = " SELECT                                " 
                + "   '901' AS curr_code,                 "
                + "   sum(a.end_tran_amt) AS end_tran_amt, "
                + "   a.p_seqno ,                          "
                + "   a.fund_code ,                        "
                + "   max(a.fund_name) fund_name ,         "
                + "   max(a.acct_type) acct_type ,         "
                + "   max(a.id_p_seqno) id_p_seqno   "
                + " FROM                                  "
                + "   mkt_cashback_dtl a , act_acno b      "
                + " where a.p_seqno = b.p_seqno and b.stmt_cycle = ?  "
                + " and a.fund_code not in ( SELECT fund_code FROM mkt_loan_parm WHERE move_cond= 'Y' )  "                
                + " GROUP BY                              "
                + "   a.p_seqno ,                          "
                + "   a.fund_code                          "
                + " HAVING                                "
                + "   sum(a.end_tran_amt) < 0              "
                + " ORDER BY                              "
                + "   a.p_seqno ,                          "
                + "   a.fund_code                          ";

        setString(1,getValue("wday.stmt_cycle"));
        int cursorIndex = openCursor();
        totalCnt = 0;
        cashbackCnt = 0;
        cycFundCnt = 0;
        bilSysexpCnt = 0;
        while (fetchTable(cursorIndex)) {
            totalCnt++;
            String currCode = getValue("curr_code");
            String pSeqno = getValue("p_seqno");
            // Double endTranAmt = getValueDouble("end_tran_amt");
            // String fundCode = getValue("fund_code");
            // String fundName = getValue("fund_name");
            // String acctType = getValue("acct_type");
            // String idPSeqno = getValue("id_p_seqno");
            String cardNo = selectCardNo(currCode, pSeqno);
            if (cardNo == null) {
                showLogMessage("I", "", "p_seqno=" + pSeqno + "查無卡檔主卡資料.");
                continue;
            }

            insertMktCashbackDtl();
            insertCycFundDtl(cardNo, "901", Math.abs(getValueInt("end_tran_amt")));
            insertBilSysexp(cardNo, "901", Math.abs(getValueInt("end_tran_amt")));
        }
        closeCursor(cursorIndex);

        showLogMessage("I", "", "select mkt_cashback_dtl彙總筆數 , 計 " + totalCnt + " 筆");
        showLogMessage("I", "", "insert mkt_cashback_dtl 筆數 , 計 " + cashbackCnt + "  筆");
        showLogMessage("I", "", "insert cyc_fund_dtl 筆數 , 計 " + cycFundCnt + "  筆");
        showLogMessage("I", "", "insert bil_sysexp 筆數 , 計 " + bilSysexpCnt + "  筆");
    }

    private void selectCycDcFundDtl() throws Exception {
        sqlCmd = " SELECT                                 "
                + "   a.curr_code,                         "
                + "   sum(a.end_tran_amt) AS end_tran_amt, "
                + "   a.p_seqno,                           "
                + "   a.fund_code,                         "
                + "   max(a.fund_name) fund_name,          "
                + "   max(a.acct_type) acct_type,          "
                + "   max(a.id_p_seqno) id_p_seqno         "
                + " FROM                                   "
                + "   cyc_dc_fund_dtl a , act_acno b       "
                + " where a.p_seqno = b.p_seqno and b.stmt_cycle  = ? "
                + " GROUP BY                               "
                + "   a.p_seqno ,                          "
                + "   a.curr_code,                         "
                + "   a.fund_code                          "
                + " HAVING                                 "
                + "   sum(a.end_tran_amt) < 0              "
                + " ORDER BY                               "
                + "   a.p_seqno,                           "
                + "   a.curr_code,                         "
                + "   a.fund_code                          ";
        setString(1,getValue("wday.stmt_cycle"));
        int cursorIndex = openCursor();
        totalCnt = 0;
        cycDcFund = 0;
        cycFundCnt = 0;
        bilSysexpCnt = 0;
        while (fetchTable(cursorIndex)) {
            totalCnt++;
            String currCode = getValue("curr_code");
            String pSeqno = getValue("p_seqno");
            // Double endTranAmt = getValueDouble("end_tran_amt");
            // String fundCode = getValue("fund_code");
            // String fundName = getValue("fund_name");
            // String acctType = getValue("acct_type");
            // String idPSeqno = getValue("id_p_seqno");
            String cardNo = selectCardNo(currCode, pSeqno);
            if (cardNo == null) {
                showLogMessage("I", "", "p_seqno=" + pSeqno + "查無卡檔雙幣主卡資料.");
                continue;
            }

            insertCycDcFundDtl();
            insertCycFundDtl(cardNo, getValue("curr_code"),
                    Math.abs(getValueDouble("end_tran_amt")));
            insertBilSysexp(cardNo, getValue("curr_code"),
                    Math.abs(getValueDouble("end_tran_amt")));
        }
        closeCursor(cursorIndex);

        showLogMessage("I", "", "select cyc_dc_fund_dtl彙總筆數 , 計 " + totalCnt + " 筆");
        showLogMessage("I", "", "insert cyc_dc_fund_dtl 筆數 , 計 " + cycDcFund + "  筆");
        showLogMessage("I", "", "insert cyc_fund_dtl 筆數 , 計 " + cycFundCnt + "  筆");
        showLogMessage("I", "", "insert bil_sysexp 筆數 , 計 " + bilSysexpCnt + "  筆");
    }

    private String selectCardNo(String currCode, String pSeqno) throws Exception {
        sqlCmd = "select card_no from crd_card "
                + "where sup_flag = '0' "
                + "and curr_code = ? "
                + "and p_seqno = ? "
                + "order by current_code "
                + "FETCH FIRST ROW ONLY ";
        setString(1, currCode);
        setString(2, pSeqno);
        selectTable();
        if ("Y".equals(notFound)) {
            return null;
        }
        return getValue("card_no");
    }

    // insert 現金回饋明細檔
    int insertMktCashbackDtl() throws Exception {
        cashbackCnt++;
        tranSeqno = comr.getSeqno("mkt_modseq");
        extendField = "cashdtl.";
        dateTime();
        setValue("cashdtl.tran_date", sysDate);
        setValue("cashdtl.tran_time", sysTime);
        setValue("cashdtl.fund_code", getValue("fund_code"));
        setValue("cashdtl.fund_name", getValue("fund_name"));
        setValue("cashdtl.p_seqno", getValue("p_seqno"));
        setValue("cashdtl.acct_type", getValue("acct_type"));
        setValue("cashdtl.id_p_seqno", getValue("id_p_seqno"));
        setValue("cashdtl.tran_code", "3");
        setValue("cashdtl.mod_desc", "現金回饋沖銷");
        setValue("cashdtl.mod_memo", "");
        setValue("cashdtl.tran_pgm", javaProgram);
        setValueInt("cashdtl.beg_tran_amt", Math.abs(getValueInt("end_tran_amt")));
        setValueInt("cashdtl.end_tran_amt", Math.abs(getValueInt("end_tran_amt")));
        setValueInt("cashdtl.res_tran_amt", 0);
        setValueInt("cashdtl.res_total_cnt", 0);
        setValueInt("cashdtl.res_tran_cnt", 0);
        setValue("cashdtl.res_s_month", "");
        setValue("cashdtl.res_upd_date", "");
        setValueInt("cashdtl.EFFECT_MONTHS ", 0);
        setValue("cashdtl.effect_e_date", "");
        setValue("cashdtl.tran_seqno", tranSeqno);
        setValue("cashdtl.proc_month", hBusinessDate.substring(0, 6));
        setValue("cashdtl.acct_date", hBusinessDate);
        setValue("cashdtl.mod_reason", "");
        setValue("cashdtl.case_list_flag", "N");
        setValue("cashdtl.crt_user", javaProgram);
        setValue("cashdtl.crt_date", sysDate);
        setValue("cashdtl.apr_date", sysDate);
        setValue("cashdtl.apr_user", javaProgram);
        setValue("cashdtl.apr_flag", "Y");
        setValue("cashdtl.mod_user", javaProgram);
        setValue("cashdtl.mod_time", sysDate + sysTime);
        setValue("cashdtl.mod_pgm", javaProgram);
        daoTable = "mkt_cashback_dtl";

        try {
            insertTable();
            return (0);
        } catch (Exception ex) {
            showLogMessage("I", "",
                    String.format("insert MKT_CASHBACK_DTL err ,errmsg = [%s]", ex.getMessage()));
            return 1;
        }
    }

    // insert 基金分錄資料檔
    public int insertCycFundDtl(String cardNo, String currCode, double cashAmt) throws Exception {
        cycFundCnt++;
        dateTime();
        extendField = "fddtl.";
        setValue("fddtl.business_date", hBusinessDate);
        setValue("fddtl.curr_code" , currCode );
        setValue("fddtl.create_date", sysDate);
        setValue("fddtl.create_time", sysTime);
        setValue("fddtl.id_p_seqno", getValue("id_p_seqno"));
        setValue("fddtl.p_seqno", getValue("p_seqno"));
        setValue("fddtl.acct_type", getValue("acct_type"));
        setValue("fddtl.card_no", cardNo);
        setValue("fddtl.fund_code", getValue("fund_code").substring(0, 4));
        setValue("fddtl.vouch_type", "3");
//      setValue("fddtl.tran_code", "3");
        setValue("fddtl.tran_code", "7");        
        setValue("fddtl.cd_kind", "H003");
        setValue("fddtl.memo1_type", "1");
        if ("901".equals(currCode)) {
            setValueInt("fddtl.fund_amt", (int) cashAmt);
        } else {
            setValueDouble("fddtl.fund_amt", cashAmt);
        }

        setValueInt("fddtl.other_amt", 0);
        setValue("fddtl.proc_flag", "N");
        setValue("fddtl.proc_date", "");
        setValue("fddtl.execute_date", hBusinessDate);
        setValue("fddtl.fund_cnt", "1");
        setValue("fddtl.mod_user", javaProgram);
        setValue("fddtl.mod_time", sysDate + sysTime);
        setValue("fddtl.mod_pgm", javaProgram);
        daoTable = "cyc_fund_dtl";
        insertTable();
        return (0);
    }

    // insert cyc_dc_fund_dtl (現金回饋明細檔)
    int insertCycDcFundDtl() throws Exception {
        tranSeqno = comr.getSeqno("ecs_dbmseq");
        // comr = new new CommRoutine(getDBconnect(),getDBalias());
        extendField = "dcdtl.";
        dateTime();
        setValue("dcdtl.tran_date", sysDate);
        setValue("dcdtl.tran_time", sysTime);
        setValue("dcdtl.fund_code", getValue("fund_code"));
        setValue("dcdtl.fund_name", getValue("fund_name"));
        setValue("dcdtl.p_seqno", getValue("p_seqno"));
        setValue("dcdtl.acct_type", getValue("acct_type"));
        setValue("dcdtl.curr_code", getValue("curr_code"));
        setValue("dcdtl.id_p_seqno", getValue("id_p_seqno"));
        setValue("dcdtl.tran_code", "3");
        setValue("dcdtl.mod_desc", "現金回饋沖銷");
        setValue("dcdtl.mod_memo", "");
        setValue("dcdtl.tran_pgm", javaProgram);
        setValueDouble("dcdtl.beg_tran_amt", Math.abs(getValueDouble("end_tran_amt")));
        setValueDouble("dcdtl.end_tran_amt", Math.abs(getValueDouble("end_tran_amt")));
        setValue("dcdtl.effect_e_date", "");
        setValue("dcdtl.tran_seqno", tranSeqno);
        setValue("dcdtl.proc_month", hBusinessDate.substring(0, 6));
        setValue("dcdtl.acct_date", hBusinessDate);
        setValue("dcdtl.mod_reason", "");
        setValue("dcdtl.crt_user", javaProgram);
        setValue("dcdtl.crt_date", sysDate);
        setValue("dcdtl.apr_date", sysDate);
        setValue("dcdtl.apr_user", javaProgram);
        setValue("dcdtl.apr_flag", "Y");
        setValue("dcdtl.mod_user", javaProgram);
        setValue("dcdtl.mod_time", sysDate + sysTime);
        setValue("dcdtl.mod_pgm", javaProgram);
        daoTable = "cyc_dc_fund_dtl";
        insertTable();
        return (0);
    }


    // insert 其他系統費用檔
    int insertBilSysexp(String cardNo, String currCode, double cashAmt) throws Exception {
        bilSysexpCnt++;
        dateTime();
        extendField = "sysexp.";
        setValue("sysexp.card_no", cardNo);
        setValue("sysexp.acct_type", getValue("acct_type"));
        setValue("sysexp.p_seqno", getValue("p_seqno"));
        setValue("sysexp.bill_type", "OKOL");
        setValue("sysexp.txn_code", "HC");
        //setValue("sysexp.purchase_date", sysDate);
        setValue("sysexp.purchase_date", hBusinessDate);        
        setValue("sysexp.src_type", "");
        setValue("sysexp.dest_curr", currCode);
        setValue("sysexp.curr_code", currCode);
        if ("901".equals(currCode)) {
            setValueInt("sysexp.dest_amt", (int) cashAmt);
            setValueInt("sysexp.dc_dest_amt", (int) cashAmt);
            setValueInt("sysexp.src_amt", (int) cashAmt);
        } else {
            setValueDouble("sysexp.dest_amt", cashAmt);
            setValueDouble("sysexp.dc_dest_amt", cashAmt);
            setValueDouble("sysexp.src_amt", cashAmt);
        }

        setValue("sysexp.bill_desc", "現金回饋沖銷");
        setValue("sysexp.post_flag", "N");
        setValue("sysexp.ref_key", tranSeqno);
        setValue("sysexp.mod_user", javaProgram);
        setValue("sysexp.mod_time", sysDate + sysTime);
        setValue("sysexp.mod_pgm", javaProgram);
        daoTable = "bil_sysexp";
        int recCnt = insertTable();
        return (0);
    }
}
