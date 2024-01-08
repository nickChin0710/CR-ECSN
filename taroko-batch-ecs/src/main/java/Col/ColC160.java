/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
*  106/08/24  V1.01.01  phopho     Initial                                   *
*  109/12/15  V1.00.02  shiyuqi    updated for project coding standard       *
*  111/12/17  V1.00.03  sunny      fix hAcnoPSeqno 參數處理                                         *
*****************************************************************************/
package Col;

import com.*;

public class ColC160 extends AccessDAO {
    private String progname = "傳送CS(M0)C-產生M0基本資料處理程式 109/12/15  V1.00.03 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;
    String hCallErrorDesc = "";
    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";

    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoAcctHolderId = "";
    String hAcnoIdPSeqno = "";
    String hCmotPSeqno = "";
    String hCmotCorpPSeqno = "";
    String hCmotCorpNo = "";
    String hCmotIdPSeqno = "";
    String hCmotId = "";
    String hCmotFormType = "";
    String hCmotCorpOnFlag = "";
    double hCmotStmtOverDueAmt = 0;
    double hCmotTtlAmtBal = 0;
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayLastAcctMonth = "";
    String hWdayThisCloseDate = "";
    String hCmotDcCurrFlag = "";
    String hDebtCardNo="";
    double hDebtCardSumEndBal = 0;

    long totalCnt = 0;
    String hTempBusinessDate = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC160 proc = new ColC160();
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
            if (args.length > 1) {
                comc.errExit("Usage : ColC160  [business_date]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if (args.length == 1) {
                hBusiBusinessDate = args[0];
            }
            selectPtrBusinday();

            if (selectPtrWorkday() != 0) {
                exceptExit = 0;
                //comcr.errRtn("本日[" + hBusiBusinessDate + "]不需執行", "", hCallBatchSeqno);
                comc.errExit("本日[" + hBusiBusinessDate + "]不需執行", "");
            }

            showLogMessage("I", "", "處理商務卡開始...");
            totalCnt = 0;
            selectColM0Base1();
            showLogMessage("I", "", "累計公司戶      筆數 : [" + totalCnt + "]");

            totalCnt = 0;
            selectColM0Base2();
            showLogMessage("I", "", "累計公司個繳戶  筆數 : [" + totalCnt + "]");

            showLogMessage("I", "", "處理一般卡開始...");
            totalCnt = 0;
            selectColM0Base3();
            showLogMessage("I", "", "累計一般卡(正卡)筆數 : [" + totalCnt + "]");

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "程式執行結束");
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
        hTempBusinessDate = "";
        selectSQL = "decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date, "
                + "to_char(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))),'yyyymmdd')-1 days,'yyyymmdd') temp_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno );
        }
        hBusiBusinessDate = getValue("business_date");
        hTempBusinessDate = getValue("temp_date");

    }

    // ************************************************************************
    private int selectPtrWorkday() throws Exception {
        sqlCmd = "select a.stmt_cycle, ";
        sqlCmd += "a.this_acct_month, ";
        sqlCmd += "a.last_acct_month, ";
        sqlCmd += "a.this_close_date ";
        sqlCmd += "from ptr_workday a,col_m0_parm b ";
        sqlCmd += "where to_date(a.this_lastpay_date,'yyyymmdd')+b.exceed_pay_days = to_date(?,'YYYYMMDD') ";
        sqlCmd += "and   a.stmt_cycle = b.stmt_cycle ";
        setString(1, hTempBusinessDate);

        if (selectTable() > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayLastAcctMonth = getValue("last_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
        }

        if (notFound.equals("Y"))
            return 1;
        return 0;
    }

    // ************************************************************************
    private void selectColM0Base1() throws Exception {
        selectSQL = "a.corp_p_seqno, ";
        selectSQL += "a.acct_type, ";
        selectSQL += "max(decode(a.id_p_seqno,'','Y','N')) corp_on_flag ";
        daoTable = "col_m0_base a,ptr_acct_type b";
        whereStr = "where a.stmt_cycle = ? ";
        whereStr += "and   a.acct_type = b.acct_type ";
        whereStr += "and   b.card_indicator ='2' ";
        whereStr += "group by corp_p_seqno,a.acct_type ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hCmotCorpOnFlag = getValue("corp_on_flag");

            totalCnt++;
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");

            if (selectActAcno1() != 0)
                continue;
           //selectActAcctHst();
            selectActAcct();

            /* 商務卡總繳戶 */
            hCmotPSeqno = hAcnoPSeqno;
            hCmotCorpPSeqno = hAcnoCorpPSeqno;
            hCmotCorpNo = hAcnoCorpNo;
            hCmotId = "";
            hCmotIdPSeqno = "";
            hCmotFormType = "1";
            insertColM0Out();
        }
        closeCursor();
    }

    // ************************************************************************
    private int selectActAcno1() throws Exception {
        hAcnoPSeqno = "";
        hAcnoCorpNo = "";

//        sqlCmd = "select p_seqno, ";
        sqlCmd = "select acno_p_seqno, ";
        sqlCmd += "uf_corp_no(corp_p_seqno) as corp_no ";
        sqlCmd += "from act_acno ";
        sqlCmd += "where corp_p_seqno = ? ";
        sqlCmd += "and   acct_type    = ? ";
        sqlCmd += "and   id_p_seqno   = '' ";
        setString(1, hAcnoCorpPSeqno);
        setString(2, hAcnoAcctType);

        extendField = "act_acno_1.";
        
        if (selectTable() > 0) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("act_acno_1.acno_p_seqno");
            hAcnoCorpNo = getValue("act_acno_1.corp_no");
        }

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "corp_p_seqno[" + hAcnoCorpPSeqno + "] acct_type]" + hAcnoAcctType
                    + "] not exists act_acno");
            return 1;
        }
        return 0;
    }

    // ************************************************************************
    private void selectColM0Base2() throws Exception {
        selectSQL = "a.p_seqno, ";
        selectSQL += "a.corp_no, ";
        selectSQL += "a.id_p_seqno, ";
        selectSQL += "b.acct_key as acct_holder_id, ";
        selectSQL += "a.corp_p_seqno, ";
        selectSQL += "a.acct_type ";
        daoTable = "col_m0_base a,act_acno b " ;
        whereStr = "where a.stmt_cycle = ? ";
//        whereStr += "and   a.p_seqno = b.p_seqno ";
        whereStr += "and   a.p_seqno = b.acno_p_seqno ";
        whereStr += "and   b.card_indicator ='2' ";
        whereStr += "and   a.id_p_seqno <> '' ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            hAcnoPSeqno = getValue("p_seqno");
            hAcnoCorpNo = getValue("corp_no");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoAcctHolderId = getValue("acct_holder_id");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hCmotCorpOnFlag = "";

            totalCnt++;
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");

            //selectActAcctHst();
            selectActAcct();

            /* 商務卡個繳戶 */
            hCmotPSeqno = hAcnoPSeqno;
            hCmotCorpPSeqno = hAcnoCorpPSeqno;
            hCmotCorpNo = hAcnoCorpNo;
            hCmotId = hAcnoAcctHolderId;
            hCmotIdPSeqno = hAcnoIdPSeqno;
            hCmotFormType = "2";
            insertColM0Out();
        }
        closeCursor();
    }

    // ************************************************************************
    private void selectColM0Base3() throws Exception {
//        selectSQL = "a.id_p_seqno, ";
//        selectSQL += "c.id_no||c.id_no_code as acct_holder_id, ";
//        selectSQL += "a.p_seqno, ";
//        selectSQL += "b.acct_type ";
//        daoTable = "act_acno a,col_m0_base b, crd_idno c ";
//        whereStr = "where b.stmt_cycle = ? ";
//        whereStr += "and   b.id_p_seqno <> '' ";
//        whereStr += "and   a.p_seqno = b.p_seqno ";
//        whereStr += "and   c.id_p_seqno = b.id_p_seqno ";
//        whereStr += "and   a.p_seqno = a.gp_no ";
//        whereStr += "and   a.id_p_seqno <> '' ";
//        setString(1, h_wday_stmt_cycle);
        selectSQL = "a.id_p_seqno, ";
        selectSQL += "c.id_no||c.id_no_code as acct_holder_id, ";
        selectSQL += "a.acno_p_seqno, ";
        selectSQL += "b.acct_type ";
        daoTable = "act_acno a,col_m0_base b, crd_idno c ";
        whereStr = "where b.stmt_cycle = ? ";
        whereStr += "and   b.id_p_seqno <> '' ";
        whereStr += "and   a.acno_p_seqno = b.p_seqno ";
        whereStr += "and   c.id_p_seqno = b.id_p_seqno ";
        whereStr += "and   a.acno_flag <> 'Y' ";
        whereStr += "and   a.id_p_seqno <> '' ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoAcctHolderId = getValue("acct_holder_id");
            hAcnoPSeqno = getValue("acno_p_seqno");  //sunny fix
            hAcnoAcctType = getValue("acct_type");
            hCmotCorpOnFlag = "";

            totalCnt++;
            if (totalCnt % 2000 == 0)
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");

            selectActAcct();
            //selectActAcctHst();

            /* 一般卡 */
            hCmotPSeqno = hAcnoPSeqno;
            hCmotCorpPSeqno = "";
            hCmotCorpNo = "";
            hCmotId = hAcnoAcctHolderId;
            hCmotIdPSeqno = hAcnoIdPSeqno;
            hCmotFormType = "3";
            
            if (!hCmotDcCurrFlag.equals("Y")) {
                selectActAcctCurr();
            }                     
            
            insertColM0Out();
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hCmotDcCurrFlag = "N";
        sqlCmd = "select 1 ";
        sqlCmd += " from act_acct_curr  ";
        sqlCmd += "where curr_code != '901'  ";
        sqlCmd += "and p_seqno = ?  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hCmotPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCmotDcCurrFlag = "Y";
        }

    }
    
    // ************************************************************************
    private void insertColM0Out() throws Exception {
        dateTime();
        daoTable = "col_m0_out";
        extendField = daoTable + ".";
        setValue(extendField+"stmt_cycle", hWdayStmtCycle);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"p_seqno", hCmotPSeqno);
        setValue(extendField+"corp_p_seqno", hCmotCorpPSeqno);
        setValue(extendField+"corp_no", hCmotCorpNo);
        setValue(extendField+"id_no", hCmotId);
        setValue(extendField+"id_p_seqno", hCmotIdPSeqno);
        setValue(extendField+"form_type", hCmotFormType);
        setValue(extendField+"corp_on_flag", hCmotCorpOnFlag);
        setValue(extendField+"acct_month", hWdayThisAcctMonth);
        setValue(extendField+"create_month", hWdayThisAcctMonth);
        setValueDouble(extendField+"stmt_over_due_amt", hCmotStmtOverDueAmt);
        setValueDouble(extendField+"ttl_amt_bal", hCmotTtlAmtBal);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        setValue(extendField+"dc_curr_flag", hCmotDcCurrFlag); //sunny add 20221220

        insertTable();

        if (dupRecord.equals("Y")) {
            updateColM0Out();
        } 
    }

    // ************************************************************************
    private void updateColM0Out() throws Exception {
        daoTable = "col_m0_out";
        updateSQL = "acct_month = ?, stmt_over_due_amt = ?, ttl_amt_bal = ?, mod_time = sysdate , dc_curr_flag = ? ";
        whereStr = "WHERE p_seqno = ? ";
        setString(1, hWdayThisAcctMonth);
        setDouble(2, hCmotStmtOverDueAmt);
        setDouble(3, hCmotTtlAmtBal);
        setString(4, hCmotDcCurrFlag);  //sunny add 20221220
        setString(5, hCmotPSeqno);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_m0_out error!";
            String err2 = "stmt_cycle=[" + hWdayStmtCycle + "] [" + hCmotFormType + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }
    // ************************************************************************
    
    private void selectActAcct() throws Exception {
    	 hCmotStmtOverDueAmt = 0;
         hCmotTtlAmtBal = 0;
        sqlCmd = "select ttl_amt, ";
        sqlCmd += "min_pay ";
        sqlCmd += "from act_acct ";
        sqlCmd += "where p_seqno    = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct.";

        if (selectTable() > 0) {
        	hCmotStmtOverDueAmt = getValueDouble("act_acct.min_pay");
        	hCmotTtlAmtBal = getValueDouble("act_acct.ttl_amt");
        }
    }
    
// ************************************************************************
    
//    private void selectActDebt() throws Exception {
//    	 hCmotStmtOverDueAmt = 0;
//         hCmotTtlAmtBal = 0;
//        sqlCmd = "select ttl_amt, ";
//        sqlCmd += "min_pay ";
//        sqlCmd += "from act_acct ";
//        sqlCmd += "where p_seqno = ? ";
//        setString(1, hAcnoPSeqno);
//        
//        extendField = "act_acct.";
//
//        if (selectTable() > 0) {
//        	hCmotStmtOverDueAmt = getValueDouble("act_acct.min_pay");
//        	hCmotTtlAmtBal = getValueDouble("act_acct.ttl_amt");
//        }
//    }

    // ************************************************************************
//    private void selectActAcctHst() throws Exception {
//        hCmotStmtOverDueAmt = 0;
//        hCmotTtlAmtBal = 0;
//        sqlCmd = "select stmt_over_due_amt, ";
//        sqlCmd += "ttl_amt_bal ";
//        sqlCmd += "from act_acct_hst ";
//        sqlCmd += "where acct_month = ? ";
//        sqlCmd += "and   p_seqno    = ? ";
//        setString(1, hWdayLastAcctMonth);
//        setString(2, hAcnoPSeqno);
//        
//        extendField = "act_acct_hst.";
//
//        if (selectTable() > 0) {
//            hCmotStmtOverDueAmt = getValueDouble("act_acct_hst.stmt_over_due_amt");
//            hCmotTtlAmtBal = getValueDouble("act_acct_hst.ttl_amt_bal");
//        }
//    }
    // ************************************************************************
}