/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/06  V1.00.00    Wendy Lu                         program initial    *
*  112/05/09  V1.00.01    Wilson    修正總資料筆數                                                                                          *
*  112/05/10  V1.00.02    Wilson    update tsc_vd_card                        *
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


public class TscD005 extends AccessDAO {
	private String PROGNAME = "悠遊VD卡餘額轉置通知檔(DCBQ)媒體產生程式  112/12/19 V1.00.03";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hDcbqMediaCreateDate = "";
    String hDcbqMediaCreateTime = "";
    String hDcbqTscCardNo = "";
    String hDcbqCardNo = "";
    String hDcbqEmbossKind = "";
    String hDcbqCreateDate = "";
    String hDcbqBalanceDatePlan = "";
    String hDcbqBalanceDate = "";
    String hDcbqBalanceDateRtn = "";
    String hDcbqRowid = "";
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
    buf1 detailSt = new buf1();

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
                comc.errExit("Usage : TscD005 [notify_date] [forceFlag (Y/N)]", "");
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

            tmpstr1 = String.format("DCBQ.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscDcbqLoga();
            }

            fileOpen();

            selectTscDcbqLog();

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
        sqlCmd += "to_char(sysdate,'yyyymmdd') hDcbqMediaCreateDate,";
        sqlCmd += "to_char(sysdate,'hh24miss') hDcbqMediaCreateTime ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hDcbqMediaCreateDate = getValue("hDcbqMediaCreateDate");
        hDcbqMediaCreateTime = getValue("hDcbqMediaCreateTime");
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
            showLogMessage("I", "", String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscDcbqLoga() throws Exception {
        daoTable = "tsc_dcbq_log ";
        updateSQL = "proc_flag = 'N', ";
        updateSQL += "balance_date = '' ";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcbq_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("HDCBQ%8.8s%8.8s%6.6s%53.53s", comc.TSCC_BANK_ID8, hDcbqMediaCreateDate,
                hDcbqMediaCreateTime, " ");

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
    void selectTscDcbqLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no, ";
        sqlCmd += "card_no, ";
        sqlCmd += "emboss_kind, ";
        sqlCmd += "create_date, ";
        sqlCmd += "balance_date_plan, ";
        sqlCmd += "balance_date, ";
        sqlCmd += "balance_date_rtn, ";
        sqlCmd += "rowid as rowid  ";
        sqlCmd += "from tsc_dcbq_log ";
        sqlCmd += "where balance_date = '' ";
        sqlCmd += "and balance_date_plan <= ? ";
        sqlCmd += "and decode(proc_flag,'','N', proc_flag) = 'N' ";
        sqlCmd += "and appr_user <> '' ";
        setString(1, hTnlgNotifyDate);
        openCursor();
        while(fetchTable()) {
            hDcbqTscCardNo = getValue("tsc_card_no");
            hDcbqCardNo = getValue("card_no");
            hDcbqEmbossKind = getValue("emboss_kind");
            hDcbqCreateDate = getValue("create_date");
            hDcbqBalanceDatePlan = getValue("balance_date_plan");
            hDcbqBalanceDate = getValue("balance_date");
            hDcbqBalanceDateRtn = getValue("balance_date_rtn");
            hDcbqRowid = getValue("rowid");

            writeRtn();

            updateTscDcbqLog();

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

        detailSt = new buf1();
        detailSt.type = "D";
        detailSt.attri = "01";

        tmpstr = String.format("%-20.20s", hDcbqTscCardNo);
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
    void updateTscDcbqLog() throws Exception {
        daoTable = "tsc_dcbq_log ";
        updateSQL = "balance_date = ?, ";
        updateSQL += "media_crt_date = ?, ";
        updateSQL += " media_crt_time = ?,";
        updateSQL += "notify_date = ?, ";
        updateSQL += "file_name = ?, ";
        updateSQL += "proc_flag = 'Y', ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where rowid = ? ";
        setString(1, hDcbqMediaCreateDate);
        setString(2, hDcbqMediaCreateDate);
        setString(3, hDcbqMediaCreateTime);
        setString(4, hTnlgNotifyDate);
        setString(5, hTnlgFileName);
        setString(6, javaProgram);
        setRowId(7, hDcbqRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_Dcbq_log not found!", "", hCallBatchSeqno);
        }

        daoTable = "tsc_vd_card ";
        updateSQL = "balance_date = ?, ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where tsc_card_no = ? ";
        setString(1, hDcbqMediaCreateDate);
        setString(2, javaProgram);
        setString(3, hDcbqTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscD005 proc = new TscD005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class buf1 {
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
    
    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type = comc.subMS950String(bytes, 0, 1);
        detailSt.attri = comc.subMS950String(bytes, 1, 2);
        detailSt.tscCardNo = comc.subMS950String(bytes, 3, 20);
        detailSt.amt = comc.subMS950String(bytes, 23, 13);
        detailSt.amtBal = comc.subMS950String(bytes, 36, 13);
        detailSt.filler2 = comc.subMS950String(bytes, 49, 31);
        detailSt.hashValue = comc.subMS950String(bytes, 80, 16);
        detailSt.fillerEnd = comc.subMS950String(bytes, 96, 2);
    }
}
