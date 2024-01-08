/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/04  V1.00.00    PhoPho     program initial                          *
*  109/12/14  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC031 extends AccessDAO {
    private String progname = "更生帳戶繳款明細資料處理程式 109/12/14  V1.00.01  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hCljlId = "";
    String hCljlIdPSeqno = "";
    double hCljlTranAmtBal = 0;
    String hCljlRowid = "";
    double hClilActPerAmt = 0;
    String hCljlAcctDate = "";
    String hClilRowid = "";
    String hClrwRenewStatus = "";
    double hCdplActTotAmt = 0;
    String hClilInstSeq = "";
    String hCdplLiadType = "";
    String hClilCaseLetter = "";
    double hClilArPerAmt = 0;
    double hClilArTotAmt = 0;
    double hCdplActPerAmt = 0;
    double hCdplUnpayAmt = 0;
    double hCljlTranAmt = 0;

    double tempTxnAmt = 0;
    double tempInstAmt = 0;
    int totalCnt = 0;

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
                comc.errExit("Usage : ColC031 [business_date] [callbatch_seqno]", "");
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
            if (args.length >= 1 && args[0].length() == 8) {
                hBusiBusinessDate = args[0];
            }
            selectPtrBusinday();

            selectColLiadJrnl01();
            updateColiadPaymain1();

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
        selectSQL = "decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno );
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /***********************************************************************/
    void selectColLiadJrnl01() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "distinct id_no, id_p_seqno ";
        sqlCmd += "from col_liad_jrnl ";
        sqlCmd += "where tran_amt_bal > 0 ";

        openCursor();
        while (fetchTable()) {
            hCljlId = getValue("id_no");
            hCljlIdPSeqno = getValue("id_p_seqno");
            selectColLiadJrnl02();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColLiadJrnl02() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "acct_date,";
        sqlCmd += "tran_amt,";
        sqlCmd += "tran_amt_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liad_jrnl ";
        sqlCmd += "where tran_amt_bal > 0 ";
//        sqlCmd += "and id_no = ? ";
        sqlCmd += "and id_p_seqno = ? ";
        sqlCmd += " order by acct_date ";
//        setString(1, h_cljl_id);
        setString(1, hCljlIdPSeqno);

		extendField = "B.";
		
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCljlAcctDate = getValue("B.acct_date", i);
            hCljlTranAmt = getValueDouble("B.tran_amt", i);
            hCljlTranAmtBal = getValueDouble("B.tran_amt_bal", i);
            hCljlRowid = getValue("B.rowid", i);
            
            tempTxnAmt = hCljlTranAmtBal;
            hCdplLiadType = "2";
            selectColLiadPaymain();

            hCdplLiadType = "1";
            if (tempTxnAmt > 0)
                selectcolLiadInstall();

            hCljlTranAmtBal = tempTxnAmt;
            updateColLiadJrnl();

            totalCnt++;
            if (totalCnt % 1000 == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }

            if (tempTxnAmt > 0)
                break;
        }

    }

    /***********************************************************************/
    void selectColLiadPaymain() throws Exception {

//        sqlCmd = "select ";
//        sqlCmd += "a.case_letter,";
//        sqlCmd += "a.allocate_amt,";
//        sqlCmd += "a.liqu_pay_amt,";
//        sqlCmd += "a.rowid as rowid ";
//        sqlCmd += "from col_liad_paymain a,(select c.id_no,c.case_letter ";
//        sqlCmd += "from col_liad_liquidate c ";
//        sqlCmd += "where case_date = (select max(case_date) ";
//        sqlCmd += "from col_liad_liquidate d ";
//        sqlCmd += "where c.id_no = d.id_no ";
//        sqlCmd += "and c.case_letter = d.case_letter) ";
//        sqlCmd += "and c.liqu_status not in ('2','6','7') ";
//        sqlCmd += "group by c.id_no,c.case_letter) b ";
//        sqlCmd += "where a.holder_id = ? ";
//        sqlCmd += "and a.allocate_amt > 0 ";
//        sqlCmd += "and a.holder_id = b.id_no ";
//        sqlCmd += "and a.case_letter = b.case_letter ";
//        sqlCmd += "and a.allocate_amt > a.liqu_pay_amt ";
//        sqlCmd += " order by recv_date ";
//        setString(1, h_cljl_id);

        sqlCmd = "select ";
        sqlCmd += "a.case_letter,";
        sqlCmd += "a.allocate_amt,";
        sqlCmd += "a.liqu_pay_amt,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from col_liad_paymain a,(select c.id_p_seqno,c.case_letter ";
        sqlCmd += "from col_liad_liquidate c ";
        sqlCmd += "where case_date = (select max(case_date) ";
        sqlCmd += "from col_liad_liquidate d ";
        sqlCmd += "where c.id_p_seqno = d.id_p_seqno ";
        sqlCmd += "and c.case_letter = d.case_letter) ";
        sqlCmd += "and c.liqu_status not in ('2','6','7') ";
        sqlCmd += "group by c.id_p_seqno,c.case_letter) b ";
        sqlCmd += "where a.holder_id_p_seqno = ? ";
        sqlCmd += "and a.allocate_amt > 0 ";
        sqlCmd += "and a.holder_id_p_seqno = b.id_p_seqno ";
        sqlCmd += "and a.case_letter = b.case_letter ";
        sqlCmd += "and a.allocate_amt > a.liqu_pay_amt ";
        sqlCmd += " order by recv_date ";
        setString(1, hCljlIdPSeqno);
        
		extendField = "A.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hClilCaseLetter = getValue("A.case_letter", i);
            hClilArPerAmt = getValueDouble("A.allocate_amt", i);
            hClilActPerAmt = getValueDouble("A.liqu_pay_amt", i);
            hClilRowid = getValue("A.rowid", i);

            selectColLiadPaydetl();

            tempInstAmt = hClilArPerAmt - hClilActPerAmt;
            if (tempTxnAmt > tempInstAmt) {
                hClilActPerAmt = hClilArPerAmt;
                hCdplActPerAmt = tempInstAmt;
                hCdplActTotAmt = hCdplActTotAmt + hCdplActPerAmt;
                hCdplUnpayAmt = hClilArTotAmt - hCdplActTotAmt;
                tempTxnAmt = tempTxnAmt - tempInstAmt;
            } else {
                hClilActPerAmt = hClilActPerAmt + tempTxnAmt;
                hCdplActPerAmt = tempTxnAmt;
                hCdplActTotAmt = hCdplActTotAmt + hCdplActPerAmt;
                hCdplUnpayAmt = hClilArTotAmt - hCdplActTotAmt;
                tempTxnAmt = 0;
            }
            if (i == 0)
                selectColLiadLiquidate();
            insertColLiadPaydetl();
            updateColLiadPaymain();
            if (tempTxnAmt == 0)
                break;
        }
    }

    /***********************************************************************/
    void selectColLiadLiquidate() throws Exception {
        hClrwRenewStatus = "";
        sqlCmd = "select liqu_status ";
        sqlCmd += " from col_liad_liquidate  ";
//        sqlCmd += "where id_no = ?  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and case_letter = ?  ";
//        sqlCmd += "and recv_date =(select max(recv_date) from col_liad_liquidate where id_no = ? ";
        sqlCmd += "and recv_date =(select max(recv_date) from col_liad_liquidate where id_p_seqno = ? ";
        sqlCmd += "and case_letter = ?)  ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_cljl_id);
        setString(1, hCljlIdPSeqno);
        setString(2, hClilCaseLetter);
//        setString(3, h_cljl_id);
        setString(3, hCljlIdPSeqno);
        setString(4, hClilCaseLetter);
        
        extendField = "col_liad_liquidate.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_liqu_status duplicate!", "", comcr.hCallBatchSeqno);
        }
        hClrwRenewStatus = getValue("col_liad_liquidate.liqu_status");
    }

    /***********************************************************************/
    void insertColLiadPaydetl() throws Exception {
    	daoTable = "col_liad_paydetl";
    	extendField = daoTable + ".";
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"case_letter", hClilCaseLetter);
        setValue(extendField+"inst_seq", hClilInstSeq);
        setValue(extendField+"holder_id_p_seqno", hCljlIdPSeqno);
        setValue(extendField+"holder_id", hCljlId);
        setValue(extendField+"liad_type", hCdplLiadType);
        setValue(extendField+"renew_status", hClrwRenewStatus);
        setValueDouble(extendField+"ar_per_amt", hClilArPerAmt);
        setValueDouble(extendField+"ar_tot_amt", hClilArTotAmt);
        setValueDouble(extendField+"act_per_amt", hCdplActPerAmt);
        setValueDouble(extendField+"act_tot_amt", hCdplActTotAmt);
        setValueDouble(extendField+"unpay_amt", hCdplUnpayAmt);
        setValue(extendField+"pay_date", hCljlAcctDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liad_paydetl duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    /**********************************************************************/
    void updateColLiadPaymain() throws Exception {
        daoTable = "col_liad_paymain";
        updateSQL = "liqu_pay_amt = ?,";
        updateSQL += " payment_date_e = decode(sign(?-allocate_amt),-1,'',cast(? as varchar(8))),";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setDouble(1, hClilActPerAmt);
        setDouble(2, hClilActPerAmt);
        setString(3, hBusiBusinessDate);
        setString(4, javaProgram);
        setRowId(5, hClilRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liad_paymain not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /*********************************************************************/
    void selectColLiadPaydetl() throws Exception {
        hCdplActTotAmt = 0;
        sqlCmd = "select sum(act_per_amt) h_cdpl_act_tot_amt ";
        sqlCmd += " from col_liad_paydetl  ";
//        sqlCmd += "where holder_id = ?  ";
        sqlCmd += "where holder_id_p_seqno = ? ";
        sqlCmd += "and case_letter = ? ";
//        setString(1, h_cljl_id);
        setString(1, hCljlIdPSeqno);
        setString(2, hClilCaseLetter);
        
        extendField = "col_liad_paydetl.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_liad_paydetl not found!", "", comcr.hCallBatchSeqno);
        }
        hCdplActTotAmt = getValueDouble("col_liad_paydetl.h_cdpl_act_tot_amt");
    }

    /***********************************************************************/
    void selectcolLiadInstall() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "case_letter,";
        sqlCmd += "inst_seq,";
        sqlCmd += "ar_per_amt,";
        sqlCmd += "ar_tot_amt,";
        sqlCmd += "act_per_amt,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liad_install ";
//        sqlCmd += "where holder_id = ? ";
        sqlCmd += "where holder_id_p_seqno = ? ";
        sqlCmd += "and ar_per_amt > 0 ";
        sqlCmd += "and ar_per_amt > act_per_amt ";
        sqlCmd += " order by inst_date_e,payment_day ";
//        setString(1, h_cljl_id);
        setString(1, hCljlIdPSeqno);
        
        extendField = "col_liad_install.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hClilCaseLetter = getValue("col_liad_install.case_letter", i);
            hClilInstSeq = getValue("col_liad_install.inst_seq", i);
            hClilArPerAmt = getValueDouble("col_liad_install.ar_per_amt", i);
            hClilArTotAmt = getValueDouble("col_liad_install.ar_tot_amt", i);
            hClilActPerAmt = getValueDouble("col_liad_install.act_per_amt", i);
            hClilRowid = getValue("col_liad_install.rowid", i);

            selectColLiadPaydetl();

            tempInstAmt = hClilArPerAmt - hClilActPerAmt;
            if (tempTxnAmt >= tempInstAmt) {
                hClilActPerAmt = hClilArPerAmt;
                hCdplActPerAmt = tempInstAmt;
                hCdplActTotAmt = hCdplActTotAmt + hCdplActPerAmt;
                hCdplUnpayAmt = hClilArTotAmt - hCdplActTotAmt;
                tempTxnAmt = tempTxnAmt - tempInstAmt;
            } else {
                hClilActPerAmt = hClilActPerAmt + tempTxnAmt;
                hCdplActPerAmt = tempTxnAmt;
                hCdplActTotAmt = hCdplActTotAmt + hCdplActPerAmt;
                hCdplUnpayAmt = hClilArTotAmt - hCdplActTotAmt;
                tempTxnAmt = 0;
            }
            if (i == 0)
                selectColLiadRenew();
            insertColLiadPaydetl();
            updateColLiadInstall();
            updateColLiadInstall1();

            if (tempTxnAmt == 0)
                break;
        }
    }

    /***********************************************************************/
    void updateColLiadInstall() throws Exception {
        daoTable = "col_liad_install";
        updateSQL = "act_per_amt = ?,";
        updateSQL += " pay_date  = ?,";
        updateSQL += " unpay_amt = ar_per_amt - ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setDouble(1, hClilActPerAmt);
        setString(2, hCljlAcctDate);
        setDouble(3, hClilActPerAmt);
        setString(4, javaProgram);
        setRowId(5, hClilRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liad_install not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /**********************************************************************/
    void selectColLiadRenew() throws Exception {
        hClrwRenewStatus = "";
        sqlCmd = "select renew_status ";
        sqlCmd += " from col_liad_renew  ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and case_letter = ?  ";
//        sqlCmd += "and recv_date =(select max(recv_date) from col_liad_renew where id_no = ? ";
        sqlCmd += "and recv_date =(select max(recv_date) from col_liad_renew where id_p_seqno = ? ";
        sqlCmd += "and case_letter = ?)  ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_cljl_id);
        setString(1, hCljlIdPSeqno);
        setString(2, hClilCaseLetter);
//        setString(3, h_cljl_id);
        setString(3, hCljlIdPSeqno);
        setString(4, hClilCaseLetter);
        
        extendField = "col_liad_renew.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_liad_renew not found!", "", comcr.hCallBatchSeqno);
        }
        hClrwRenewStatus = getValue("col_liad_renew.renew_status");
    }

    /**********************************************************************/
    void updateColLiadInstall1() throws Exception {
        daoTable = "col_liad_install";
        updateSQL = "act_tot_amt = (select sum(act_per_amt) from col_liad_install ";
//        whereStr = "where holder_id = ? and case_letter = ?), ";
        whereStr = "where holder_id_p_seqno = ? and case_letter = ?), ";
        whereStr += " mod_pgm  = ?, mod_time  = sysdate where rowid = ? ";
//        setString(1, h_cljl_id);
        setString(1, hCljlIdPSeqno);
        setString(2, hClilCaseLetter);
        setString(3, javaProgram);
        setRowId(4, hClilRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liad_install not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /**********************************************************************/
    void updateColLiadJrnl() throws Exception {
        daoTable = "col_liad_jrnl";
        updateSQL = "tran_amt_bal = ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setDouble(1, hCljlTranAmtBal);
        setString(2, javaProgram);
        setRowId(3, hCljlRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liad_jrnl not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColiadPaymain1() throws Exception {
        daoTable = "col_liad_paymain";
        updateSQL = "payment_date_e = ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where payment_date_e = '' ";
        whereStr += "and liad_type = '1' ";
//        whereStr += "and (holder_id,case_letter) in ";
//        whereStr += "(select holder_id,case_letter from col_liad_install group by holder_id,case_letter having sum(act_per_amt)>=sum(ar_per_amt)) ";
        whereStr += "and (holder_id_p_seqno,case_letter) in ";
        whereStr += "(select holder_id_p_seqno,case_letter from col_liad_install group by holder_id_p_seqno,case_letter having sum(act_per_amt)>=sum(ar_per_amt)) ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        updateTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC031 proc = new ColC031();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
