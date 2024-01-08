/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*產生代行資料至授權作業*/
public class TscA001 extends AccessDAO {
    private final String progname = "產生代行資料至授權作業   109/11/13 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hSystemDate = "";
    String hSystemDtime = "";
    String hStadCrtDate = "";
    String hStadCrtTime = "";
    String hStadTscCardNo = "";
    String hStadCardNo = "";
    String hStadProcessCode = "";
    String hStadPurchaseDate = "";
    String hStadPurchaseTime = "";
    double hStadTranAmt = 0;
    String hStadTranStan = "";
    String hStadTranRrn = "";
    String hStadAcqId = "";
    String hStadTermId = "";
    String hStadMchtId = "";
    String hStadRespCode = "";
    String hStadRowid = "";
    String hStadTsccDataSeqno = "";
    String hBusinessDate = "";
    int totCnt = 0;

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
                comc.errExit("Usage : TscA001 [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {

                comcr.hCallBatchSeqno = hCallBatchSeqno;
                comcr.hCallRProgramCode = javaProgram;

                comcr.callbatch(0, 0, 0);
                sqlCmd = "select user_id ";
                sqlCmd += " from ptr_callbatch  ";
                sqlCmd += "where batch_seqno = ? ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            commonRtn();

            selectTscStadLog();

            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totCnt));
            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1);
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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_dtime ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemDtime = getValue("h_system_dtime");
        }
    }

    /***********************************************************************/
    void selectTscStadLog() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "process_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "purchase_time,";
        sqlCmd += "tran_amt,";
        sqlCmd += "tran_stan,";
        sqlCmd += "tran_rrn,";
        sqlCmd += "acq_id,";
        sqlCmd += "term_id,";
        sqlCmd += "mcht_id,";
        sqlCmd += "resp_code,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from tsc_stad_log ";
        sqlCmd += "where post_flag != 'Y' ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hStadCrtDate      = getValue("crt_date", i);
            hStadCrtTime      = getValue("crt_time", i);
            hStadTscCardNo   = getValue("tsc_card_no", i);
            hStadCardNo       = getValue("card_no", i);
            hStadProcessCode  = getValue("process_code", i);
            hStadPurchaseDate = getValue("purchase_date", i);
            hStadPurchaseTime = getValue("purchase_time", i);
            hStadTranAmt      = getValueDouble("tran_amt", i);
            hStadTranStan     = getValue("tran_stan", i);
            hStadTranRrn      = getValue("tran_rrn", i);
            hStadAcqId        = getValue("acq_id", i);
            hStadTermId       = getValue("term_id", i);
            hStadMchtId       = getValue("mcht_id", i);
            hStadRespCode     = getValue("resp_code", i);
            hStadRowid         = getValue("rowid", i);
            totCnt++;
            if (totCnt % 5000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("tsc_a001 Process record=[%d]", totCnt));

if(debug == 1)
   showLogMessage("I","","888 Card=["+hStadCardNo+"]"+hStadRespCode   +"," + totCnt);

            if (hStadRespCode.equals("00")) {
                insertTscActauthCcas();
            } else {
                insertCcaAuthTxlog();
            }

            daoTable = "tsc_stad_log";
            updateSQL = "post_flag  = 'Y'";
            whereStr = "where rowid   = ? ";
            setRowId(1, hStadRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_tsc_stad_log not found!", "", hCallBatchSeqno);
            }
        }

    }

    /***********************************************************************/
    void insertTscActauthCcas() throws Exception {
        setValue("crt_date"          , hStadCrtDate);
        setValue("crt_time"          , hStadCrtTime);
        setValue("tsc_card_no"       , hStadTscCardNo);
        setValue("process_code"      , hStadProcessCode);
        setValue("tran_date"         , hStadPurchaseDate);
        setValue("tran_time"         , hStadPurchaseTime);
        setValueDouble("trans_amount", hStadTranAmt);
        setValue("trace_no"          , hStadTranStan);
        setValue("retr_ref_no"       , hStadTranRrn);
        setValue("iden_code"         , hStadAcqId);
        setValue("term_id"           , hStadTermId);
        setValue("merchant_id"       , hStadMchtId);
        setValue("resp_code"         , hStadRespCode);
        setValue("proc_flag"         , "N");
        setValue("mod_time"          , sysDate + sysTime);
        setValue("mod_pgm"           , javaProgram);
        daoTable = "tsc_actauth_ccas";
        insertTable();

    }

    /***********************************************************************/
    void insertCcaAuthTxlog() throws Exception {

        setValue("crt_date"       , hStadCrtDate);
        setValue("crt_time"       , hStadCrtTime);
     // setValue("tsc_card_no"    , h_stad_tsc_card_no);
        setValue("card_no"        , hStadCardNo);
        setValue("proc_code"      , hStadProcessCode);
     // setValue("purchase_date"  , h_stad_purchase_date);
     // setValue("purchase_time"  , h_stad_purchase_time);
     // setValueDouble("tran_amt" , h_stad_tran_amt);
     // setValue("tran_stan"      , h_stad_tran_stan);
     // setValue("tran_rrn"       , h_stad_tran_rrn);
     // setValue("acq_id"         , h_stad_acq_id);
        setValue("term_id"        , hStadTermId);
        setValue("mcht_no"        , hStadMchtId);
        setValue("iso_resp_code"  , hStadRespCode);
     // setValue("tscc_data_seqno", h_stad_tscc_data_seqno);
     // setValue("rpt_flag"       , h_stad_rpt_flag);
     // setValue("rpt_resp_code"  , h_stad_rpt_resp_code);
     // setValue("process_flag"   , "N");
        setValue("mod_time"       , sysDate + sysTime);
        setValue("mod_pgm"        , javaProgram);
        daoTable = "cca_auth_txlog";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cca_auth_txlog duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscA001 proc = new TscA001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
