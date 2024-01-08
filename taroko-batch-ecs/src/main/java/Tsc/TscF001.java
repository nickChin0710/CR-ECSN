/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-16  V1.00.01    tanwei    updated for project coding standard       *
*  112/05/03  V1.00.02    Wilson    修正ok_flag                                *
*  112/12/19  V1.00.03    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡製卡回饋檔(CDRP)媒體產生程式*/
public class TscF001 extends AccessDAO {
    private final String progname = "悠遊卡製卡回饋檔(CDRP)媒體產生程式   112/12/19 V1.00.03";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    int debug = 0;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hCdrpMediaCreateDate = "";
    String hCdrpMediaCreateTime = "";
    String hCdrpTscCardNo = "";
    String hCdrpCardNo = "";
    String hCdrpTscEmbossRsn = "";
    String hCdrpTscVendorCd = "";
    String hCdrpUpperLmt = "";
    String hCdrpUpperLmtAcmm = "";
    String hCdrpVendorEmbossDate = "";
    String hCdrpVendorDateTo = "";
    String hCdrpVendorDateRtn = "";
    String hCdrpOkFlag = "";
    double hCdrpTscAmt = 0;
    double hCdrpTscPledgeAmt = 0;
    String hCdrpIcSeqNo = "";
    String hCdrpIsamSeqNo = "";
    String hCdrpIsamBatchNo = "";
    String hCdrpIsamBatchSeq = "";
    long   hCdrpAutoloadAmt = 0;
    String hTardOldTscCardNo = "";
    String hCdrpRowid = "";
    String tempX04 = "";
    String hTempX16 = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";

    int forceFlag = 0;
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int totCnt1 = 0;
    int recCnt = 0;
    int recAmt = 0;
    int errCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    // String tmpstr2 = "";
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
                comc.errExit("Usage : TscF001 [notify_date] [force_flag (Y/N)]", "");
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

                comcr.callbatch(0, 0, 1);
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
            tmpstr1 = String.format("CDRP.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscCdrpLoga();
            }

            fileOpen();

            selectTscCdrpLog();

            hTnlgRecordCnt = totCnt;

            fileClose();

            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totCnt));
            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1);
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
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_cdrp_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_cdrp_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hCdrpMediaCreateDate = getValue("h_cdrp_media_create_date");
        hCdrpMediaCreateTime = getValue("h_cdrp_media_create_time");

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
        } else {
            return 0;
        }

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
    void updateTscCdrpLoga() throws Exception {
        daoTable   = "tsc_cdrp_log";
        updateSQL  = "proc_flag       = 'N'   , ";
        updateSQL += "mod_time        = sysdate ";
        whereStr   = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_cdrp_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("HCDRP%8.8s%8.8s%6.6s%203.203s", comc.TSCC_BANK_ID8
                , hCdrpMediaCreateDate, hCdrpMediaCreateTime, " ");

        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        /* 原160改230 */
        String buf = String.format("%-230.230s%-16.16s", tmpstr1, new String(tmpstr2, "big5"));
        writeTextFile(out, buf + "\r\n");
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        /* 原71改221 */
        tmpstr1 = String.format("T%08d%221.221s", totCnt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        /* 原160改230 */
        writeTextFile(out, String.format("%-230.230s%16.16s\r\n", tmpstr1
                         , new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void selectTscCdrpLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.tsc_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.tsc_emboss_rsn,";
        sqlCmd += "a.tsc_vendor_cd,";
        sqlCmd += "a.upper_lmt,";
        sqlCmd += "a.upper_lmt_acmm,";
        sqlCmd += "a.vendor_emboss_date,";
        sqlCmd += "a.vendor_date_to,";
        sqlCmd += "a.vendor_date_rtn,";
        sqlCmd += "a.ok_flag,";
        sqlCmd += "a.tsc_amt,";
        sqlCmd += "a.tsc_pledge_amt,";
        sqlCmd += "a.ic_seq_no,";
        sqlCmd += "a.isam_seq_no,";
        sqlCmd += "a.isam_batch_no,";
        sqlCmd += "a.isam_batch_seq,";
        sqlCmd += "a.autoload_amt,";
        sqlCmd += "b.old_tsc_card_no,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from tsc_cdrp_log a  left join tsc_card b on  b.tsc_card_no = a.tsc_card_no ";
        sqlCmd += "where a.proc_flag        = 'N' ";
        sqlCmd += "  and a.vendor_date_rtn <> '' ";
        sqlCmd += "order by a.tsc_emboss_rsn ";
        openCursor();
        while (fetchTable()) {
            hCdrpTscCardNo        = getValue("tsc_card_no");
            hCdrpCardNo            = getValue("card_no");
            hCdrpTscEmbossRsn     = getValue("tsc_emboss_rsn");
            hCdrpTscVendorCd      = getValue("tsc_vendor_cd");
            hCdrpUpperLmt          = getValue("upper_lmt");
            hCdrpUpperLmtAcmm     = getValue("upper_lmt_acmm");
            hCdrpVendorEmbossDate = getValue("vendor_emboss_date");
            hCdrpVendorDateTo     = getValue("vendor_date_to");
            hCdrpVendorDateRtn    = getValue("vendor_date_rtn");
            hCdrpOkFlag           = getValue("ok_flag");
            hCdrpTscAmt            = getValueDouble("tsc_amt");
            hCdrpTscPledgeAmt     = getValueDouble("tsc_pledge_amt");
            hCdrpIcSeqNo          = getValue("ic_seq_no");
            hCdrpIsamSeqNo        = getValue("isam_seq_no");
            hCdrpIsamBatchNo      = getValue("isam_batch_no");
            hCdrpIsamBatchSeq     = getValue("isam_batch_seq");
            hCdrpAutoloadAmt       = getValueLong("autoload_amt");
            hTardOldTscCardNo    = getValue("old_tsc_card_no");
            hCdrpRowid              = getValue("rowid");
            if (hCdrpOkFlag.length() == 0)
                hCdrpOkFlag = "Y";
            if (debug == 1)
                showLogMessage("I", "", "Process card=" + hCdrpCardNo + "," + hCdrpOkFlag);
            writeRtn();

            updateTscCdrpLog();

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

        detailSt.type    = "D";
        detailSt.attri   = "01";
        detailSt.txType = "A";

        tmpstr = String.format("%-20.20s", hCdrpTscCardNo);
        detailSt.tscCashNo = tmpstr;

        hTempX16 = hCdrpTscCardNo;
        tempX04 = "";
        sqlCmd = "select substr(new_end_date,3,4) temp_x04 ";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempX16);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempX04 = getValue("temp_x04");
        }
        if (tempX04.length() == 0) {
            sqlCmd = "select substr(new_end_date,3,4) temp_x04 ";
            sqlCmd += " from crd_card  ";
            sqlCmd += "where card_no = ? ";
            setString(1, hCdrpCardNo);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                tempX04 = getValue("temp_x04");
            }
        }
        tmpstr = String.format("%-4.4s", tempX04);
        detailSt.effcYymm = tmpstr;

        tmpstr = String.format("%-20.20s", hCdrpIcSeqNo);
        detailSt.chipSeq = tmpstr;

        tmpstr = String.format("%-20.20s", hCdrpTscCardNo);
        detailSt.tscCardNo = tmpstr;

        switch (comcr.str2int(hCdrpTscEmbossRsn)) {
        case 1:
        case 2:
            tmpstr = "N";
            break;
        case 3:
        case 4:
            tmpstr = "C";
            break;
        case 5:
            tmpstr = "R";
            break;
        }
        detailSt.txRsn = tmpstr;

        tmpstr = String.format("%-20.20s", hTardOldTscCardNo);
        detailSt.tscCashNoo = tmpstr;

        switch (hCdrpOkFlag.toCharArray()[0]) {
        case 'Y':
            tmpstr = String.format("%-10.10s", "OK");
            break;
        case 'E':
            tmpstr = String.format("%-10.10s", "EC Error");
            break;
        case 'S':
            tmpstr = String.format("%-10.10s", "SVC Error");
            break;
        case 'N':
            tmpstr = String.format("%-10.10s", "Error");
            break;
        }
        detailSt.cardStatus = tmpstr;

        if (hCdrpOkFlag.equals("Y"))
            tmpstr = String.format("%-1.1s", "0");
        else
            tmpstr = String.format("%-1.1s", "1");
        detailSt.embossStatus = tmpstr;

        tmpstr = String.format("%-2.2s", comc.TSCC_BANK_ID2);
        detailSt.bankNo2 = tmpstr;

        tmpstr = String.format("%-8.8s", hCdrpVendorEmbossDate);
        detailSt.embossDate = tmpstr;

        tmpstr = String.format("%05.0f", hCdrpTscAmt);
        detailSt.tscAmt = tmpstr;

        tmpstr = String.format("%05.0f", hCdrpTscPledgeAmt);
        detailSt.tscPledgeAmt = tmpstr;

        tmpstr = String.format("%-10.10s", hCdrpTscVendorCd);
        detailSt.vendorCd = tmpstr;

        tmpstr = String.format("%-8.8s", hCdrpIsamSeqNo);
        detailSt.isamSeqNo = tmpstr;

        tmpstr = String.format("%-10.10s", hCdrpIsamBatchNo);
        detailSt.isamBatchNo = tmpstr;

        tmpstr = String.format("%-5.5s", hCdrpIsamBatchSeq);
        detailSt.isamBatchSeq = tmpstr;

        tmpstr = String.format("%04d", hCdrpAutoloadAmt);
        detailSt.autoloadAmt = tmpstr;

        /* 原160改230 */
        tmpstr1 = String.format("%-230.230s", detailSt.allText());
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr = String.format("%-16.16s", new String(tmpstr2, "big5"));
        detailSt.hashValue = tmpstr;

        tmpstr = String.format("%-2.2s", new String(endflag)); // 換行
        detailSt.fillerEnd = tmpstr;

        String buf = String.format("%-248.248s", detailSt.allText());
        writeTextFile(out, buf);

        return;
    }

    /***********************************************************************/
    void updateTscCdrpLog() throws Exception {
        daoTable   = "tsc_cdrp_log";
        updateSQL  = "media_crt_date  = ?,";
        updateSQL += " media_crt_time = ?,";
        updateSQL += " notify_date    = ?,";
        updateSQL += " file_name      = ?,";
        updateSQL += " proc_flag      = 'Y',";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr   = "where rowid     = ? ";
        setString(1, hCdrpMediaCreateDate);
        setString(2, hCdrpMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hCdrpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_cdrp_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF001 proc = new TscF001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String txType;
        String tscCashNo;
        String effcYymm;
        String chipSeq;
        String tscCardNo;
        String txRsn;
        String tscCashNoo;
        String cardStatus;
        String embossStatus;
        String bankNo2;
        String embossDate;
        String tscAmt;
        String tscPledgeAmt;
        String vendorCd;
        String isamSeqNo;
        String isamBatchNo;
        String isamBatchSeq;
        String autoloadAmt;
        String filler1;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type          , 1);
            rtn += comc.fixLeft(attri         , 2);
            rtn += comc.fixLeft(txType       , 1);
            rtn += comc.fixLeft(tscCashNo   , 20);
            rtn += comc.fixLeft(effcYymm     , 4);
            rtn += comc.fixLeft(chipSeq      , 20);
            rtn += comc.fixLeft(tscCardNo   , 20);
            rtn += comc.fixLeft(txRsn        , 1);
            rtn += comc.fixLeft(tscCashNoo , 20);
            rtn += comc.fixLeft(cardStatus   , 10);
            rtn += comc.fixLeft(embossStatus , 1);
            rtn += comc.fixLeft(bankNo2     , 2);
            rtn += comc.fixLeft(embossDate   , 8);
            rtn += comc.fixLeft(tscAmt       , 5);
            rtn += comc.fixLeft(tscPledgeAmt, 5);
            rtn += comc.fixLeft(vendorCd     , 10);
            rtn += comc.fixLeft(isamSeqNo   , 8);
            rtn += comc.fixLeft(isamBatchNo , 10);
            rtn += comc.fixLeft(isamBatchSeq, 5);
            rtn += comc.fixLeft(autoloadAmt  , 4);
            rtn += comc.fixLeft(filler1      , 73);
            rtn += comc.fixLeft(hashValue    , 16);
            rtn += comc.fixLeft(fillerEnd    , 2);
            return rtn;
        }
    }

}
