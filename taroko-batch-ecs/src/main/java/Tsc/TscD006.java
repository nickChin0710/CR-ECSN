/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/06  V1.00.00    Wendy Lu                         program initial    *
*  112/12/19  V1.00.01    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;


public class TscD006 extends AccessDAO {
	private String PROGNAME = "悠遊VD卡簽帳資料檔(DCST)媒體產生程式  112/12/19 V1.00.01";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hRunDate = "";
    String hBusiBusinessDate = "";
    String hDcstMediaCreateDate = "";
    String hDcstMediaCreateTime = "";
    String hDcstTscCardNo = "";
    double hDcstDestinationAmt = 0;
    double hDcstFeedbackAmt = 0;
    String hDcstRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String hTfinRunDay = "";

    int forceFlag = 0;
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int diffDate = 0;
    int totalCnt = 0;
    double totalFeedbackAmt = 0;
    double totalAmt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr1 = "";
    String ftpStr = "";

    int out = -1;

    public int mainProcess(String[] args) throws Exception {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscD006 [notify_date] [forceFlag (Y/N)]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hRunDate = "";
            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hRunDate = sgArgs0;
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hRunDate = sgArgs0;
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();
            selectTscFileIden();
            if (!hRunDate.substring(6, 8).equals(hTfinRunDay)) {
                exceptExit = 0;
                String stderr = String.format("本程式限每月%s日執行 [%s]", hTfinRunDay, hRunDate);
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }
            tmpstr1 = String.format("DCST.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscDcstLoga();
            }

            fileOpen();
            selectTscDcstLog();
            hTnlgRecordCnt = totalCnt;
            fileClose();

            showLogMessage("I", "", String.format("Process records = [%d]\n", totalCnt));
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            if (out != -1) {
                closeOutputText(out);
                out = -1;
            }
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {

    	hBusiBusinessDate = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_Dcst_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_Dcst_media_create_time ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hRunDate = getValue("business_date");
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hDcstMediaCreateDate = getValue("h_Dcst_media_create_date");
        hDcstMediaCreateTime = getValue("h_Dcst_media_create_time");
    }

    /***********************************************************************/
    void selectTscFileIden() throws Exception {
        hTfinRunDay = "";

        sqlCmd = "select run_day ";
        sqlCmd += "from tsc_file_iden ";
        sqlCmd += "where file_iden = 'DCST' ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_file_iden not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTfinRunDay = getValue("run_day");
        }

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date,";
        sqlCmd += "ftp_send_date ";
        sqlCmd += "from tsc_notify_log ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_crt_date");
            hTnlgFtpSendDate = getValue("ftp_send_date");
        } else
            return (0);

        if (hTnlgFtpSendDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已FTP至TSCC, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscDcstLoga() throws Exception {
        daoTable = "tsc_dcst_log ";
        updateSQL = "proc_flag = 'N' ";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        comc.mkdirsFromFilenameWithPath(temstr1);
        out = openOutputText(temstr1, "big5");
        if (out == -1) {
            comcr.errRtn(String.format("產生檔案有錯誤[%s]", temstr1), "", hCallBatchSeqno);
        }
        tmpstr1 = String.format("HDCST%8.8s%8.8s%6.6s%53.53s", comc.TSCC_BANK_ID8, hDcstMediaCreateDate,
                hDcstMediaCreateTime, " ");
        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        String buf = String.format("%-80.80s%16.16s\r\n", tmpstr1, tmpstr2.toUpperCase());
        writeTextFile(out, buf);
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%015.0f%56.56s", totalCnt, totalAmt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-80.80s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void selectTscDcstLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "feedback_amt,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_dcst_log ";
        sqlCmd += "where proc_flag = 'N' ";
        /******  BECS1010625-052 簽帳回饋金為0元不用產生 ******/
        sqlCmd += "and dest_amt > 0  ";
        openCursor();
        while(fetchTable()) {
            hDcstTscCardNo = getValue("tsc_card_no");
            hDcstDestinationAmt = getValueDouble("dest_amt");
            hDcstFeedbackAmt = getValueDouble("feedback_amt");
            hDcstRowid = getValue("rowid");

            tmpstr1 = String.format("D01%-20.20s%013.0f%44.44s", hDcstTscCardNo
                                    , hDcstDestinationAmt, " ");
            byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
            tmpstr2 = new String(tmp);
            String buf = String.format("%-80.80s%16.16s\r\n", tmpstr1, tmpstr2.toUpperCase());
            writeTextFile(out, buf);
            updateTscDcstLog();
            totalAmt = totalAmt + hDcstDestinationAmt;
            totalFeedbackAmt = totalFeedbackAmt + hDcstFeedbackAmt;
            totalCnt++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateTscDcstLog() throws Exception {
        daoTable = "tsc_dcst_log ";
        updateSQL = "media_create_date = ?,";
        updateSQL += "media_create_time = ?,";
        updateSQL += "notify_date = ?,";
        updateSQL += "file_name = ?,";
        updateSQL += "proc_flag = 'Y',";
        updateSQL += "mod_pgm = ?,";
        updateSQL += "mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setString(1, hDcstMediaCreateDate);
        setString(2, hDcstMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hDcstRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcst_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscD006 proc = new TscD006();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
