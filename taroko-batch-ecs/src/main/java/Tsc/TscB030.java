/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*  112-09-23  V1.00.02    Wilson    風險等級改讀tsc_rm_actauth的risk_class         *                                                                           *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡拒收代行(BKTI)資料處理程式*/
public class TscB030 extends AccessDAO {
    private final String progname = "悠遊卡拒收代行(BKTI)資料處理程式   112/09/23 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTbigCrtDate = "";
    String hTbigCrtTime = "";
    String hTrahRmSendDate = "";
    String hTrahRmSendTime = "";
    String hTrahNewEndDate = "";
    String hTrahTscCardNo = "";
    String hTrahRiskClass = "";
    String hTrahRestoreDate = "";
    String hTbhtRetrRefNo = "";
    String hTrahRowid = "";
    String hTrahRtSendDate = "";
    String hTbigTxnCode = "";
    String hTbigRiskClass = "";

    int totalCnt = 0;
    int tranCnt = 0;
    String tmpdata1 = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : TscB030 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            selectPtrBusinday();

            deleteTscBktiLog();

            selectTscRmActauth1();
            showLogMessage("I", "", String.format("累計新增拒絕授權 [%d]筆 傳送[%d]筆\n", totalCnt, tranCnt));

            totalCnt = tranCnt = 0;
            selectTscRmActauth2();
            showLogMessage("I", "", String.format("累計取消拒絕授權 [%d]筆 傳送[%d]筆\n", totalCnt, tranCnt));

            // ==============================================
            // 固定要做的
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
        sqlCmd = "select business_date, " + "to_char(sysdate,'yyyymmdd') as h_tbig_crt_date, "
                + "to_char(sysdate,'hh24miss') as h_tbig_crt_time ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                    : hBusiBusinessDate;
            hTbigCrtDate = getValue("h_tbig_crt_date");
            hTbigCrtTime = getValue("h_tbig_crt_time");
        }

    }

    /***********************************************************************/
    void deleteTscBktiLog() throws Exception {
        daoTable = "tsc_bkti_log";
        deleteTable();
    }

    /***********************************************************************/
    void selectTscRmActauth1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "risk_class,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from tsc_rm_actauth ";
        sqlCmd += "where send_reason in ('10','20','30','40') ";
        sqlCmd += "  and remove_date <> '' ";
        sqlCmd += "  and rm_send_date = '' ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTrahTscCardNo = getValue("tsc_card_no", i);
            hTrahRiskClass = getValue("risk_class", i);
            hTrahRowid       = getValue("rowid", i);

            totalCnt++;

            selectTscRmActautha();
            if (hTrahRmSendDate.equals("00000000") == false) {
                updateTscRmActautha();
                continue;
            }

            hTrahRmSendDate = hTbigCrtDate;
            hTrahRmSendTime = hTbigCrtTime;
            hTbigTxnCode = "A";
//            hTbigRiskClass = "57";
            hTbigRiskClass = hTrahRiskClass;
            insertTscBktiLog();
            updateTscRmActautha();
            insertTscBktiHst();
        }

    }

    /***********************************************************************/
    void selectTscRmActautha() throws Exception {
        hTrahRmSendDate = "";
        hTrahRmSendTime = "";
        hTrahNewEndDate = "";
        sqlCmd = "select max(decode(rm_send_date,'','00000000',rm_send_date)) h_trah_rm_send_date,";
        sqlCmd += "max(rm_send_time) h_trah_rm_send_time,";
        sqlCmd += "max(new_end_date) h_trah_new_end_date ";
        sqlCmd += " from tsc_rm_actauth  ";
        sqlCmd += "where tsc_card_no = ?  ";
//        sqlCmd += "  and risk_class  = '57' ";
        setString(1, hTrahTscCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_rm_actauth not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTrahRmSendDate = getValue("h_trah_rm_send_date");
            hTrahRmSendTime = getValue("h_trah_rm_send_time");
            hTrahNewEndDate = getValue("h_trah_new_end_date");
        }

    }

    /***********************************************************************/
    void insertTscBktiLog() throws Exception {
        tmpdata1 = String.format("%2.2s%010d",hBusiBusinessDate.substring(2),comcr.getModSeq());
        hTbhtRetrRefNo = tmpdata1;
        tranCnt++;

        setValue("crt_date"    , hTbigCrtDate);
        setValue("crt_time"    , hTbigCrtTime);
        setValue("txn_code"    , hTbigTxnCode);
        setValue("tsc_card_no" , hTrahTscCardNo);
        setValue("risk_class"  , hTbigRiskClass);
        setValue("new_end_date", hTrahNewEndDate);
        setValue("retr_ref_no" , hTbhtRetrRefNo);
        setValue("mod_time"    , sysDate + sysTime);
        setValue("mod_pgm"     , javaProgram);
        daoTable = "tsc_bkti_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_bkti_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscRmActautha() throws Exception {
        daoTable   = "tsc_rm_actauth";
        updateSQL  = " rm_send_date  = ?,";
        updateSQL += " rm_send_time  = ?,";
        updateSQL += " retr_ref_no_1 = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where rowid    = ? ";
        setString(1, hTrahRmSendDate);
        setString(2, hTrahRmSendTime);
        setString(3, hTbhtRetrRefNo);
        setString(4, javaProgram);
        setRowId(5, hTrahRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_rm_actauth not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertTscBktiHst() throws Exception {
        sqlCmd = "insert into tsc_bkti_hst ";
        sqlCmd += "(crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "txn_code,";
        sqlCmd += "remove_date,";
        sqlCmd += "remove_time,";
        sqlCmd += "risk_class,";
        sqlCmd += "card_no,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "current_code,";
        sqlCmd += "new_end_date,";
        sqlCmd += "retr_ref_no,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.current_code,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from crd_card a,tsc_card b where a.card_no  = b.card_no and b.tsc_card_no = ? ";
        setString(1, hTbigCrtDate);
        setString(2, hTbigCrtTime);
        setString(3, hTrahTscCardNo);
        setString(4, hTbigTxnCode);
        setString(5, hTrahRmSendDate);
        setString(6, hTrahRmSendTime);
        setString(7, hTbigRiskClass);
        setString(8, hTrahNewEndDate);
        setString(9, hTbhtRetrRefNo);
        setString(10, javaProgram);
        setString(11, hTrahTscCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectTscRmActauth2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "risk_class,";
        sqlCmd += "rowid ";
        sqlCmd += " from tsc_rm_actauth ";
        sqlCmd += "where send_reason in ('10','20','30','40') ";
        sqlCmd += "  and restore_date <> '' ";
        sqlCmd += "  and rt_send_date  = '' ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTrahTscCardNo = getValue("tsc_card_no", i);
            hTrahRiskClass = getValue("risk_class", i);
            hTrahRowid = getValue("rowid", i);

            totalCnt++;

            selectTscRmActauthb();
            if ((hTrahRestoreDate.equals("00000000")) || 
                (hTrahRestoreDate.compareTo(hTbigCrtDate) > 0))
                continue;

            hTrahRtSendDate = hTbigCrtDate;
            hTbigTxnCode     = "D";
//            hTbigRiskClass   = "57";
            hTbigRiskClass = hTrahRiskClass;
            insertTscBktiLog();
            updateTscRmActauthb();
            insertTscBktiHst();
        }

    }

    /***********************************************************************/
    void selectTscRmActauthb() throws Exception {
        hTrahRmSendDate = "";
        hTrahRmSendTime = "";
        hTrahRestoreDate = "";
        hTrahNewEndDate = "";
        sqlCmd = "select max(decode(rm_send_date,'','00000000',rm_send_date)) h_trah_rm_send_date,";
        sqlCmd += "max(rm_send_time) h_trah_rm_send_time,";
        sqlCmd += "min(nvl(restore_date,'00000000')) h_trah_restore_date,";
        sqlCmd += "max(new_end_date) h_trah_new_end_date ";
        sqlCmd += " from tsc_rm_actauth  ";
        sqlCmd += "where tsc_card_no = ?  ";
//        sqlCmd += "and risk_class = '57' ";
        setString(1, hTrahTscCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_rm_actauth b not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTrahRmSendDate = getValue("h_trah_rm_send_date");
            hTrahRmSendTime = getValue("h_trah_rm_send_time");
            hTrahRestoreDate = getValue("h_trah_restore_date");
            hTrahNewEndDate = getValue("h_trah_new_end_date");
        }

    }

    /***********************************************************************/
    void updateTscRmActauthb() throws Exception {
        daoTable   = "tsc_rm_actauth";
        updateSQL  = " rt_send_date  = ?,";
        updateSQL += " retr_ref_no_2 = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where tsc_card_no  = ?  ";
//        whereStr  += "  and risk_class   = '57'  ";
        whereStr  += "  and rt_send_date = '' ";
        setString(1, hTrahRtSendDate);
        setString(2, hTbhtRetrRefNo);
        setString(3, javaProgram);
        setString(4, hTrahTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_rm_actauth b not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB030 proc = new TscB030();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
