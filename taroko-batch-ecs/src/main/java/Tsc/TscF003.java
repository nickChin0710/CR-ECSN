/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-16  V1.00.01    tanwei    updated for project coding standard       *
*  112/12/19  V1.00.02    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡銀行回覆資料檔(ECTI)媒體產生程式*/
public class TscF003 extends AccessDAO {
    private final String progname = "悠遊卡銀行回覆資料檔(ECTI)媒體產生程式  112/12/19 V1.00.02";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hEctiMediaCreateDate = "";
    String hEctiMediaCreateTime = "";
    String hEctiTranCode = "";
    String hEctiTscCardNo = "";
    String hEctiTranDate = "";
    String hEctiTranTime = "";
    double hEctiTranAmt = 0;
    String hEctiTraffCode = "";
    String hEctiPlaceCode = "";
    String hEctiTraffSubname = "";
    String hEctiPlaceSubname = "";
    String hEctiChgbackReason = "";
    String hEctiOnlineMark = "";
    String hEctiRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";

    int forceFlag = 0;
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int totCnt1 = 0;
    double tempRranAmt = 0;
    int totalCnt = 0;
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
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscF003 [notify_date] [force_flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTnlgNotifyDate = sgArgs0;
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTnlgNotifyDate = sgArgs0;
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("ECTI.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscEctiLoga();
            }

            fileOpen();
            selectTscEctiLog();
            fileClose();

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
        sqlCmd = "select to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_ecti_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_ecti_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hEctiMediaCreateDate = getValue("h_ecti_media_create_date");
        hEctiMediaCreateTime = getValue("h_ecti_media_create_time");
    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date,";
        sqlCmd += "ftp_send_date ";
        sqlCmd += " from tsc_notify_log  ";
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
    void updateTscEctiLoga() throws Exception {
        daoTable   = "tsc_ecti_log";
        updateSQL  = "proc_flag = 'N'  ,  ";
        updateSQL += "mod_time  = sysdate ";
        whereStr   = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_ecti_log not found!", "", hCallBatchSeqno);
        }

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
        tmpstr1 = String.format("HECTI%8.8s%8.8s%6.6s%61.61s", comc.TSCC_BANK_ID8, hEctiMediaCreateDate, hEctiMediaCreateTime, " ");
        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        String buf = String.format("%-118.118s%16.16s%c%c", tmpstr1, tmpstr2, 13, 10);
        writeTextFile(out, buf);
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%015.0f%64.64s", totalCnt, tempRranAmt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-118.118s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }
    }

    /***********************************************************************/
    void selectTscEctiLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tran_code,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "tran_date,";
        sqlCmd += "tran_time,";
        sqlCmd += "tran_amt,";
        sqlCmd += "traff_code,";
        sqlCmd += "place_code,";
        sqlCmd += "traff_subname,";
        sqlCmd += "place_subname,";
        sqlCmd += "chgback_reason,";
        sqlCmd += "online_mark,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_ecti_log ";
        sqlCmd += "where proc_flag = 'N' or proc_flag = '' ";
        openCursor();
        while (fetchTable()) {
            hEctiTranCode = getValue("tran_code");
            hEctiTscCardNo = getValue("tsc_card_no");
            hEctiTranDate = getValue("tran_date");
            hEctiTranTime = getValue("tran_time");
            hEctiTranAmt = getValueDouble("tran_amt");
            hEctiTraffCode = getValue("traff_code");
            hEctiPlaceCode = getValue("place_code");
            hEctiTraffSubname = getValue("traff_subname");
            hEctiPlaceSubname = getValue("place_subname");
            hEctiChgbackReason = getValue("chgback_reason");
            hEctiOnlineMark = getValue("online_mark");
            hEctiRowid = getValue("rowid");

            tmpstr1 = String.format("D01%4.4s%-20.20s%-8.8s%-6.6s%013.0f%-8.8s%-6.6s%s%s%-4.4s%-1.1s%-5.5s",
                    hEctiTranCode, hEctiTscCardNo, hEctiTranDate, hEctiTranTime, 
                    hEctiTranAmt , hEctiTraffCode , hEctiPlaceCode, 
		    comc.fixLeft(hEctiTraffSubname, 20), 
                    comc.fixLeft(hEctiPlaceSubname, 20), 
		    hEctiChgbackReason, hEctiOnlineMark, " ");
            byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
            tmpstr2 = new String(tmp);
            String buf = String.format("%s%-16.16s\r\n", comc.fixLeft(tmpstr1, 118), tmpstr2);
            writeTextFile(out, buf);
            updateTscEctiLog();
            totalCnt++;
            tempRranAmt = tempRranAmt + hEctiTranAmt;
        }
        closeCursor();

    }

    /***********************************************************************/
    void updateTscEctiLog() throws Exception {
        daoTable   = "tsc_ecti_log";
        updateSQL  = " media_create_date = ?,";
        updateSQL += " media_create_time = ?,";
        updateSQL += " notify_date       = ?,";
        updateSQL += " file_name         = ?,";
        updateSQL += " proc_flag         = 'Y',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hEctiMediaCreateDate);
        setString(2, hEctiMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hEctiRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_ecti_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF003 proc = new TscF003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
