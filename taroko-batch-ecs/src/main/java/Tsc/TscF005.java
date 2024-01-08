/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-16  V1.00.01    tanwei    updated for project coding standard       *
*  112/05/09  V1.00.02    Wilson    修正總資料筆數                                                                                          *
*  112/12/19  V1.00.03    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*餘額轉置通知(BTRQ)媒體產生程式*/
public class TscF005 extends AccessDAO {
    private final String progname = "餘額轉置通知(BTRQ)媒體產生程式  112/12/19 V1.00.03";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hBtrqMediaCreateDate = "";
    String hBtrqMediaCreateTime = "";
    String hBtrqTscCardNo = "";
    String hBtrqCardNo = "";
    String hBtrqEmbossKind = "";
    String hBtrqCreateDate = "";
    String hBtrqBalanceDatePlan = "";
    String hBtrqBalanceDate = "";
    String hBtrqBalanceDateRtn = "";
    String hBtrqRowid = "";
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
                comc.errExit("Usage : TscF005 [notify_date] [force_flag (Y/N)]", "");
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

            showLogMessage("I", "", String.format(" Process Date =[%s]", hTnlgNotifyDate));

            tmpstr1 = String.format("BTRQ.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscBtrqLoga();
            }

            fileOpen();

            selectTscBtrqLog();

            hTnlgRecordCnt = totCnt;

            fileClose();
            showLogMessage("I", "", String.format("Process total records = [%d]", totCnt));
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
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_btrq_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_btrq_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hBtrqMediaCreateDate = getValue("h_btrq_media_create_date");
        hBtrqMediaCreateTime = getValue("h_btrq_media_create_time");
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
            showLogMessage("I", "", String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscBtrqLoga() throws Exception {
        daoTable = "tsc_btrq_log";
        updateSQL = "proc_flag = 'N',";
        updateSQL += " balance_date = ''";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_btrq_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("HBTRQ%8.8s%8.8s%6.6s%53.53s", comc.TSCC_BANK_ID8, hBtrqMediaCreateDate,
                hBtrqMediaCreateTime, " ");

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
    void selectTscBtrqLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "emboss_kind,";
        sqlCmd += "create_date,";
        sqlCmd += "balance_date_plan,";
        sqlCmd += "balance_date,";
        sqlCmd += "balance_date_rtn,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_btrq_log ";
        sqlCmd += "where balance_date = '' ";
        sqlCmd += "and balance_date_plan <= ? ";
        sqlCmd += "and decode(proc_flag,'','N', proc_flag) = 'N' ";
        sqlCmd += "and appr_user <> '' ";
        setString(1, hTnlgNotifyDate);
        openCursor();
        while(fetchTable()) {
            hBtrqTscCardNo = getValue("tsc_card_no");
            hBtrqCardNo = getValue("card_no");
            hBtrqEmbossKind = getValue("emboss_kind");
            hBtrqCreateDate = getValue("create_date");
            hBtrqBalanceDatePlan = getValue("balance_date_plan");
            hBtrqBalanceDate = getValue("balance_date");
            hBtrqBalanceDateRtn = getValue("balance_date_rtn");
            hBtrqRowid = getValue("rowid");

            writeRtn();

            updateTscBtrqLog();

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

        tmpstr = String.format("%-20.20s", hBtrqTscCardNo);
        detailSt.tscCardNo = tmpstr;

        tmpstr = String.format("%-13.13s", "0000000000000");
        detailSt.amt = tmpstr;

        tmpstr = String.format("%-13.13s", "0000000000000");
        detailSt.amtBal = tmpstr;

        tmpstr1 = String.format("%-80.80s", detailSt.allText());
        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        tmpstr = String.format("%-16.16s", tmpstr2);
        detailSt.hashValue = tmpstr;

        tmpstr = String.format("%-2.2s", endflag);
        detailSt.fillerEnd = tmpstr;

        tmpstr = String.format("%-2.2s", new String(endflag)); //換行
        detailSt.fillerEnd = tmpstr;

        String buf = detailSt.allText();
        writeTextFile(out, buf);

        return;
    }

    /***********************************************************************/
    void updateTscBtrqLog() throws Exception {
        daoTable = "tsc_btrq_log";
        updateSQL = "balance_date  = ?,";
        updateSQL += " media_crt_date = ?,";
        updateSQL += " media_crt_time = ?,";
        updateSQL += " notify_date  = ?,";
        updateSQL += " file_name   = ?,";
        updateSQL += " proc_flag   = 'Y',";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid    = ? ";
        setString(1, hBtrqMediaCreateDate);
        setString(2, hBtrqMediaCreateDate);
        setString(3, hBtrqMediaCreateTime);
        setString(4, hTnlgNotifyDate);
        setString(5, hTnlgFileName);
        setString(6, javaProgram);
        setRowId(7, hBtrqRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_btrq_log not found!", "", hCallBatchSeqno);
        }

        daoTable = "tsc_card";
        updateSQL = "balance_date  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where tsc_card_no  = ? ";
        setString(1, hBtrqMediaCreateDate);
        setString(2, javaProgram);
        setString(3, hBtrqTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_card not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF005 proc = new TscF005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String amt;
        String amtBal;
        String filler2;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(amt, 13);
            rtn += comc.fixLeft(amtBal, 13);
            rtn += comc.fixLeft(filler2, 31);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }
}
