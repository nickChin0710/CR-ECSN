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

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*拒絕授權名單檔(BKTI)媒體*/
public class TscF008 extends AccessDAO {
    private final String progname = "拒絕授權名單檔(BKTI)媒體   112/12/19 V1.00.02";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTbigMediaCreateDate = "";
    String hTbigMediaCreateTime = "";
    String hTbigCrtDate = "";
    String hTbigCrtTime = "";
    String hTbigTxnCode = "";
    String hTbigRiskClass = "";
    String hTbigTscCardNo = "";
    String hTbigNewEndDate = "";
    String hTbigRetrRefNo = "";
    String hTbigRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";

    int forceFlag = 0;
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int diffDate = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr1 = "";
    String ftpStr = "";

    int out = -1;
    Buf1 detailSt = new Buf1();

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
                comc.errExit("Usage : TscF008 [notify_date] [force_flag (Y/N)]", "");
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
            tmpstr1 = String.format("BKTI.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscBktiLoga();
            }

            fileOpen();

            selectTscBktiLog();

            hTnlgRecordCnt = totCnt;

            fileClose();

            showLogMessage("I", "", String.format("Process records = [%d]", totCnt));
            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_tbig_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tbig_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hTbigMediaCreateDate = getValue("h_tbig_media_create_date");
        hTbigMediaCreateTime = getValue("h_tbig_media_create_time");

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
            hTnlgMediaCreateDate = getValue("media_create_date");
            hTnlgFtpSendDate = getValue("ftp_send_date");
        } else
            return (0);

        if (hTnlgFtpSendDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已FTP至TSCC, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName));
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            showLogMessage("I", "", String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscBktiLoga() throws Exception {
        daoTable = "tsc_bkti_log";
        updateSQL = "proc_flag = 'N'";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bkti_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("HBKTI%8.8s%8.8s%6.6s%53.53s", comc.TSCC_BANK_ID8, hTbigMediaCreateDate,
                hTbigMediaCreateTime, " ");

        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        String buf = String.format("%-80.80s%-16.16s\r\n", tmpstr1, tmpstr2);
        writeTextFile(out, buf);
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%71.71s", totCnt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-80.80s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void selectTscBktiLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "txn_code,";
        sqlCmd += "risk_class,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "new_end_date,";
        sqlCmd += "retr_ref_no,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_bkti_log ";
        sqlCmd += "where 1 = 1 ";
        sqlCmd += "and proc_flag = 'N' ";
        openCursor();
        while(fetchTable()) {
            hTbigCrtDate = getValue("crt_date");
            hTbigCrtTime = getValue("crt_time");
            hTbigTxnCode = getValue("txn_code");
            hTbigRiskClass = getValue("risk_class");
            hTbigTscCardNo = getValue("tsc_card_no");
            hTbigNewEndDate = getValue("new_end_date");
            hTbigRetrRefNo = getValue("retr_ref_no");
            hTbigRowid = getValue("rowid");

            writeRtn();

            updateTscBktiLog();

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]", totCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void writeRtn() throws Exception {
        byte[] endflag = new byte[2];
        endflag[0] = 0x0D;
        endflag[1] = 0x0A;

        detailSt = new Buf1();
        detailSt.type = "D";
        detailSt.attri = "01";

        tmpstr = String.format("%-1.1s", hTbigTxnCode);
        detailSt.txType = tmpstr;

        tmpstr = String.format("%-20.20s", hTbigTscCardNo);
        detailSt.tscCardNo = tmpstr;

        tmpstr = String.format("%-4.4s", hTbigNewEndDate.substring(2));
        detailSt.effcYymm = tmpstr;

        tmpstr = String.format("%-8.8s", hTbigCrtDate);
        detailSt.crtDate = tmpstr;

        tmpstr = String.format("%-6.6s", hTbigCrtTime);
        detailSt.crtTime = tmpstr;

        tmpstr = String.format("%-2.2s", hTbigRiskClass);
        detailSt.riskClass = tmpstr;

        tmpstr1 = String.format("%-80.80s", detailSt.allText());
        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        tmpstr = String.format("%-16.16s", tmpstr2);
        detailSt.hashValue = tmpstr;

        tmpstr = String.format("%-2.2s", new String(endflag)); //換行
        detailSt.fillerEnd = tmpstr;

        String buf = detailSt.allText();
        writeTextFile(out, buf);

        return;
    }

    /***********************************************************************/
    void updateTscBktiLog() throws Exception {
        daoTable = "tsc_bkti_log";
        updateSQL = "media_crt_date = ?,";
        updateSQL += " media_crt_time = ?,";
        updateSQL += " notify_date  = ?,";
        updateSQL += " file_name   = ?,";
        updateSQL += " proc_flag   = 'Y',";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid    = ? ";
        setString(1, hTbigMediaCreateDate);
        setString(2, hTbigMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hTbigRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bkti_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF008 proc = new TscF008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String txType;
        String tscCardNo;
        String effcYymm;
        String crtDate;
        String crtTime;
        String riskClass;
        String filler1i;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(txType, 1);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(effcYymm, 4);
            rtn += comc.fixLeft(crtDate, 8);
            rtn += comc.fixLeft(crtTime, 6);
            rtn += comc.fixLeft(riskClass, 2);
            rtn += comc.fixLeft(filler1i, 36);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type = comc.subMS950String(bytes, 0, 1);
        detailSt.attri = comc.subMS950String(bytes, 1, 2);
        detailSt.txType = comc.subMS950String(bytes, 3, 1);
        detailSt.tscCardNo = comc.subMS950String(bytes, 4, 20);
        detailSt.effcYymm = comc.subMS950String(bytes, 24, 4);
        detailSt.crtDate = comc.subMS950String(bytes, 28, 8);
        detailSt.crtTime = comc.subMS950String(bytes, 36, 6);
        detailSt.riskClass = comc.subMS950String(bytes, 42, 2);
        detailSt.filler1i = comc.subMS950String(bytes, 44, 36);
        detailSt.hashValue = comc.subMS950String(bytes, 80, 16);
        detailSt.fillerEnd = comc.subMS950String(bytes, 96, 2);
    }

}
