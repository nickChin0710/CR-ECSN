/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/06  V1.00.00    phopho     program initial                          *
*  109/12/15  V1.00.01    shiyuqi    updated for project coding standard   *
*  112/08/22  V1.00.02    sunny      增加科目AF,CF,PF
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColD100 extends AccessDAO {
	public final boolean debug = true;
    private String progname = "act_debt 補呆帳帳務分類欄位資料處理程式 112/08/22  V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String stderr = "";
    String hCallBatchSeqno = "";

    String hBussBusinessDate = "";
    String hDebtOrgAcctCode = "";
    String hDebtAcctCodeType = "";
    String hDebtRowid = "";
    String hDebtPSeqno = "";
    String hDebtReferenceNo = "";
    double hDebtEndBal = 0;

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
            if (args.length > 1) {
                comc.errExit("Usage : ColD100 callbatch_seqno", "");
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

            hBussBusinessDate = "";
            selectPtrBusinday();

            totalCnt = 0;
            selectActDebt();

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
        sqlCmd += " from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBussBusinessDate);
        setString(2, hBussBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBussBusinessDate = getValue("business_date");

        stderr = String.format("Business_date[%s]", hBussBusinessDate);
        showLogMessage("I", "", stderr);
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "reference_no,";
        sqlCmd += "end_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from act_debt a ";
        sqlCmd += "where acct_code = 'DB' ";
        sqlCmd += "and acct_code_type = '' ";

        openCursor();
        while (fetchTable()) {
            hDebtPSeqno = getValue("p_seqno");
            hDebtReferenceNo = getValue("reference_no");
            hDebtEndBal = getValueDouble("end_bal");
            hDebtRowid = getValue("rowid");

            totalCnt++;

            if ((totalCnt % 10000) == 0) {
                stderr = String.format("Process record[%d]\n", totalCnt);
                showLogMessage("I", "", stderr);
            }

            selectColBadDetail();

            updateActDebt();

        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColBadDetail() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "trans_type,";
        sqlCmd += "decode(acct_code,'CB','BL','CI','RI','CC','PF',acct_code) h_debt_org_acct_code,";
        //sqlCmd += "decode(acct_code,'BL','B','CA','B','IT','B','ID','B','AO','B','OT','B','RI','I','PN','I','LF','C','SF','C','CB','B','CC','C','CI','I','AI','I','AF','C','SF','C','DB','B','X') h_debt_acct_code_type ";
        sqlCmd += "decode(acct_code,'BL','B','CA','B','IT','B','ID','B',";
        sqlCmd += "'AO','B','OT','B','RI','I','PN','I','LF','C','SF','S',";
        sqlCmd += "'CB','B','CC','C','CI','I','AI','I','AF','C','PF','C',";
        sqlCmd += "'CF','C','DB','B','X') h_debt_acct_code_type ";
        sqlCmd += "from col_bad_detail ";
        sqlCmd += "where trans_type ='3' ";
        sqlCmd += "and p_seqno = ? ";
        sqlCmd += "and reference_no = ? ";
        sqlCmd += "UNION ";
        sqlCmd += "select trans_type, ";
        sqlCmd += "decode(acct_code,'DB','BL','CB','BL','CI','RI','CC','PF',acct_code), ";
        sqlCmd += "decode(acct_code,'BL','B','CA','B','IT','B','ID','B', ";
        sqlCmd += "'AO','B','OT','B','RI','I','PN','I', ";
        sqlCmd += "'LF','C','SF','C','CB','B','CC','C', ";
        sqlCmd += "'CI','I','AI','I','AF','C','PF','C','CF','C','DB','B','X') ";
        sqlCmd += "from col_bad_detail ";
        sqlCmd += "where trans_type ='4' ";
        sqlCmd += "and p_seqno = ? ";
        sqlCmd += "and reference_no = ? ";
        sqlCmd += " order by trans_type ";
        setString(1, hDebtPSeqno);
        setString(2, hDebtReferenceNo);
        setString(3, hDebtPSeqno);
        setString(4, hDebtReferenceNo);
        
        extendField = "col_bad_detail.";
        
        int recordCnt1 = selectTable();
        if (recordCnt1 == 0) {
            stderr = String.format("p_seqno[%s] reference_no[%s] not found error", hDebtPSeqno,
                    hDebtReferenceNo);
            hDebtOrgAcctCode = "BL";
            hDebtAcctCodeType = "B";
            if (hDebtEndBal > 0)
                stderr = String.format("STEP 1 p_seqno[%s] reference_no[%s] orginal acct_code  error",
                        hDebtPSeqno, hDebtReferenceNo);

        } else {
            hDebtOrgAcctCode = getValue("col_bad_detail.h_debt_org_acct_code", 0);
            hDebtAcctCodeType = getValue("col_bad_detail.h_debt_acct_code_type", 0);
            if (((hDebtOrgAcctCode.equals("CB")) || (hDebtOrgAcctCode.equals("CC"))
                    || (hDebtOrgAcctCode.equals("CI")) || (hDebtOrgAcctCode.equals("DB")))
                    && (hDebtEndBal > 0))
                stderr = String.format("STEP 2 p_seqno[%s] reference_no[%s] orginal acct_code  error",
                        hDebtPSeqno, hDebtReferenceNo);
        }
    }

    /***********************************************************************/
    void updateActDebt() throws Exception {
        daoTable = "act_debt";
        updateSQL = "org_acct_code = ?,";
        updateSQL += " acct_code_type = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hDebtOrgAcctCode);
        setString(2, hDebtAcctCodeType);
        setString(3, javaProgram);
        setRowId(4, hDebtRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColD100 proc = new ColD100();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
